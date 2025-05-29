/*
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * 一个无界 {@linkplain BlockingQueue 阻塞队列}，其中元素为
 * {@code Delayed} 类型，只有当其延迟到期时才能被取出。队列的
 * <em>头部</em> 是延迟到期时间最久的 {@code Delayed} 元素。如果没有任何延迟到期，
 * 则队列没有头部，{@code poll} 将返回 {@code null}。当元素的
 * {@code getDelay(TimeUnit.NANOSECONDS)} 方法返回值小于或等于零时，延迟到期。
 * 即使未到期的元素不能使用 {@code take} 或 {@code poll} 移除，它们仍然被视为正常元素。
 * 例如，{@code size} 方法返回已到期和未到期元素的总数。此队列不允许 null 元素。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link
 * Iterator} 接口的所有 <em>可选</em> 方法。方法 {@link
 * #iterator()} 提供的迭代器 <em>不</em> 保证以任何特定顺序遍历
 * DelayQueue 的元素。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
    implements BlockingQueue<E> {

    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue<E> q = new PriorityQueue<E>();

    /**
     * 被指定等待队列头部元素的线程。这种变体的 Leader-Follower 模式
     * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) 旨在最小化不必要的定时等待。
     * 当一个线程成为领导者时，它只等待下一个延迟到期，但其他线程无限期地等待。
     * 领导者线程在从 take() 或 poll(...) 返回之前必须通知其他线程，除非在此期间有其他线程成为领导者。
     * 每当队列头部被替换为具有更早到期时间的元素时，通过将领导者字段重置为 null 来使领导者字段失效，并通知某个等待线程，但不一定是当前领导者。
     * 因此，等待线程必须准备好在等待期间获取和失去领导权。
     */
    private Thread leader = null;

    /**
     * 当队列头部出现更新的元素或需要新的线程成为领导者时被通知的条件。
     */
    private final Condition available = lock.newCondition();

    /**
     * 创建一个最初为空的新 {@code DelayQueue}。
     */
    public DelayQueue() {}

    /**
     * 创建一个最初包含给定 {@link Delayed} 实例集合的元素的 {@code DelayQueue}。
     *
     * @param c 要最初包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public DelayQueue(Collection<? extends E> c) {
        this.addAll(c);
    }

    /**
     * 将指定的元素插入此延迟队列。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 将指定的元素插入此延迟队列。
     *
     * @param e 要添加的元素
     * @return {@code true}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            q.offer(e);
            if (q.peek() == e) {
                leader = null;
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定的元素插入此延迟队列。由于队列无界，此方法将永远不会阻塞。
     *
     * @param e 要添加的元素
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) {
        offer(e);
    }

    /**
     * 将指定的元素插入此延迟队列。由于队列无界，此方法将永远不会阻塞。
     *
     * @param e 要添加的元素
     * @param timeout 此参数被忽略，因为该方法永远不会阻塞
     * @param unit 此参数被忽略，因为该方法永远不会阻塞
     * @return {@code true}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    /**
     * 检索并移除此队列的头部，如果此队列没有任何延迟已到期的元素，则返回 {@code null}。
     *
     * @return 此队列的头部，或如果此队列没有任何延迟已到期的元素，则返回 {@code null}
     */
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E first = q.peek();
            if (first == null || first.getDelay(NANOSECONDS) > 0)
                return null;
            else
                return q.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检索并移除此队列的头部，必要时等待，直到此队列上有延迟已到期的元素可用。
     *
     * @return 此队列的头部
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                E first = q.peek();
                if (first == null)
                    available.await();
                else {
                    long delay = first.getDelay(NANOSECONDS);
                    if (delay <= 0)
                        return q.poll();
                    first = null; // 等待时不要保留引用
                    if (leader != null)
                        available.await();
                    else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            available.awaitNanos(delay);
                        } finally {
                            if (leader == thisThread)
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null)
                available.signal();
            lock.unlock();
        }
    }

}


    /**
     * 获取并移除此队列的头部，必要时等待，直到此队列上有延迟已过期的元素可用，
     * 或者指定的等待时间到期。
     *
     * @return 此队列的头部，如果指定的等待时间在延迟已过期的元素可用之前到期，则返回 {@code null}
     * @throws InterruptedException 如果线程被中断，则抛出此异常
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                E first = q.peek();
                if (first == null) {
                    if (nanos <= 0)
                        return null;
                    else
                        nanos = available.awaitNanos(nanos);
                } else {
                    long delay = first.getDelay(NANOSECONDS);
                    if (delay <= 0)
                        return q.poll();
                    if (nanos <= 0)
                        return null;
                    first = null; // 等待时不要保留引用
                    if (nanos < delay || leader != null)
                        nanos = available.awaitNanos(nanos);
                    else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            long timeLeft = available.awaitNanos(delay);
                            nanos -= delay - timeLeft;
                        } finally {
                            if (leader == thisThread)
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null)
                available.signal();
            lock.unlock();
        }
    }

    /**
     * 获取但不移除此队列的头部，如果此队列为空，则返回 {@code null}。与
     * {@code poll} 不同，如果队列中没有延迟已过期的元素，此方法返回下一个将过期的元素（如果存在）。
     *
     * @return 此队列的头部，如果此队列为空，则返回 {@code null}
     */
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.peek();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 仅当元素已过期时返回第一个元素。
     * 仅由 drainTo 使用。调用时必须持有锁。
     */
    private E peekExpired() {
        // assert lock.isHeldByCurrentThread();
        E first = q.peek();
        return (first == null || first.getDelay(NANOSECONDS) > 0) ?
            null : first;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            for (E e; (e = peekExpired()) != null;) {
                c.add(e);       // 以这种顺序，以防 add() 抛出异常。
                q.poll();
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            for (E e; n < maxElements && (e = peekExpired()) != null;) {
                c.add(e);       // 以这种顺序，以防 add() 抛出异常。
                q.poll();
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从这个延迟队列中原子地移除所有元素。
     * 此调用返回后，队列将为空。未过期的元素不会等待；它们将从队列中简单地移除。
     */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            q.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 始终返回 {@code Integer.MAX_VALUE}，因为 {@code DelayQueue} 没有容量限制。
     *
     * @return {@code Integer.MAX_VALUE}
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    /**
     * 返回一个包含此队列中所有元素的数组。返回的数组元素没有特定的顺序。
     *
     * <p>返回的数组是“安全的”，即此队列不维护对其的任何引用。（换句话说，此方法必须分配一个新数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此队列中所有元素的数组
     */
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray();
        } finally {
            lock.unlock();
        }
    }


    /**
     * 返回一个包含此队列中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。
     * 返回的数组元素没有特定的顺序。
     * 如果队列适合指定的数组，则返回该数组。
     * 否则，将分配一个新的数组，其运行时类型是指定数组的运行时类型，大小为队列的大小。
     *
     * <p>如果队列适合指定的数组，并且数组中还有剩余空间
     * （即，数组中的元素比队列中的元素多），则数组中队列末尾紧随其后的元素将被设置为
     * {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。
     * 此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>以下代码可用于将延迟队列转储到新分配的 {@code Delayed} 数组中：
     *
     * <pre> {@code Delayed[] a = q.toArray(new Delayed[0]);}</pre>
     *
     * 请注意，{@code toArray(new Object[0])} 在功能上与 {@code toArray()} 相同。
     *
     * @param a 如果足够大，则用于存储队列元素的数组；否则，将为此目的分配一个相同运行时类型的数组
     * @return 包含此队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果指定的元素存在，无论是否已过期，从队列中移除该元素的一个实例。
     */
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.remove(o);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用于 Itr.remove 的基于身份的版本
     */
    void removeEQ(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Iterator<E> it = q.iterator(); it.hasNext(); ) {
                if (o == it.next()) {
                    it.remove();
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个迭代器，遍历队列中的所有元素（包括已过期和未过期的元素）。迭代器不按特定顺序返回元素。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * @return 一个遍历此队列中元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    /**
     * 基于底层 q 数组副本的快照迭代器。
     */
    private class Itr implements Iterator<E> {
        final Object[] array; // 所有元素的数组
        int cursor;           // 下一个要返回的元素的索引
        int lastRet;          // 上一个返回的元素的索引，如果没有则为 -1

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E)array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet = -1;
        }
    }

}
