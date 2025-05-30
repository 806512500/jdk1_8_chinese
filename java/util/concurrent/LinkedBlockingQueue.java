
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 基于链接节点的可选有界 {@linkplain BlockingQueue 阻塞队列}。
 * 该队列按 FIFO（先进先出）顺序排列元素。
 * 队列的 <em>头部</em> 是在队列中停留时间最长的元素。
 * 队列的 <em>尾部</em> 是在队列中停留时间最短的元素。新元素
 * 插入到队列的尾部，而队列的检索操作则从队列的头部获取元素。
 * 链接队列通常比基于数组的队列具有更高的吞吐量，但在大多数并发应用程序中性能更不可预测。
 *
 * <p>可选的容量限制构造参数用于防止队列过度扩展。如果未指定容量，
 * 则容量等于 {@link Integer#MAX_VALUE}。除非这会使队列超出容量，否则每次插入时都会动态创建链接节点。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link
 * Iterator} 接口中的所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 该集合中元素的类型
 */
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -6903933977591709194L;

    /*
     * 一种“双锁队列”算法的变体。putLock 控制进入 put（和 offer）的入口，并有一个与之关联的条件用于等待的 put。同样地，takeLock 也是如此。两者依赖的“count”字段被维护为一个原子变量，以避免在大多数情况下需要获取两个锁。此外，为了减少 put 需要获取 takeLock 和 vice-versa 的需求，使用了级联通知。当一个 put 发现它已经启用了至少一个 take 时，它会通知 taker。该 taker 又会通知其他 taker，如果在通知后有更多元素被插入。同样地，take 也会通知 put。诸如 remove(Object) 和迭代器等操作会获取两个锁。
     *
     * 读写者之间的可见性由以下方式提供：
     *
     * 每当一个元素被入队时，会获取 putLock 并更新 count。后续的读取者通过获取 putLock（通过 fullyLock）或获取 takeLock，然后读取 n = count.get() 来保证对入队 Node 的可见性。这提供了对前 n 个项目的可见性。
     *
     * 为了实现弱一致性迭代器，似乎需要保持所有 Node 的 GC 可达性，从一个已出队的 Node 的前驱开始。这会导致两个问题：
     * - 允许一个恶意的 Iterator 造成无限制的内存保留
     * - 如果一个 Node 在活动时被老化，会导致旧 Node 和新 Node 之间的跨代链接，这使得分代 GC 很难处理，导致重复的重大收集。
     * 然而，只有非删除的 Node 需要从已出队的 Node 可达，而且可达性不一定需要是 GC 理解的那种。我们使用将一个刚出队的 Node 链接到自身的技巧。这样的自链接隐式地意味着前进到 head.next。
     */

    /**
     * 链接列表节点类
     */
    static class Node<E> {
        E item;

        /**
         * 以下之一：
         * - 真实的后继 Node
         * - 本 Node，意味着后继是 head.next
         * - null，意味着没有后继（这是最后一个节点）
         */
        Node<E> next;

        Node(E x) { item = x; }
    }

    /** 容量上限，如果没有则为 Integer.MAX_VALUE */
    private final int capacity;

    /** 当前元素数量 */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * 链接列表的头部。
     * 不变量：head.item == null
     */
    transient Node<E> head;

    /**
     * 链接列表的尾部。
     * 不变量：last.next == null
     */
    private transient Node<E> last;

    /** 由 take, poll 等持有的锁 */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** 等待的 take 的等待队列 */
    private final Condition notEmpty = takeLock.newCondition();

    /** 由 put, offer 等持有的锁 */
    private final ReentrantLock putLock = new ReentrantLock();

    /** 等待的 put 的等待队列 */
    private final Condition notFull = putLock.newCondition();

    /**
     * 通知一个等待的 take。仅从 put/offer 调用（这些方法通常不会锁定 takeLock）。
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 通知一个等待的 put。仅从 take/poll 调用。
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * 在队列末尾链接节点。
     *
     * @param node 节点
     */
    private void enqueue(Node<E> node) {
        // assert putLock.isHeldByCurrentThread();
        // assert last.next == null;
        last = last.next = node;
    }

    /**
     * 从队列头部移除节点。
     *
     * @return 节点
     */
    private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        // assert head.item == null;
        Node<E> h = head;
        Node<E> first = h.next;
        h.next = h; // 帮助 GC
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * 锁定以防止 put 和 take。
     */
    void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * 解锁以允许 put 和 take。
     */
    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

//     /**
//      * 告诉当前线程是否持有两个锁。
//      */
//     boolean isFullyLocked() {
//         return (putLock.isHeldByCurrentThread() &&
//                 takeLock.isHeldByCurrentThread());
//     }

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 {@code LinkedBlockingQueue}。
     */
    public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 创建一个具有给定（固定）容量的 {@code LinkedBlockingQueue}。
     *
     * @param capacity 该队列的容量
     * @throws IllegalArgumentException 如果 {@code capacity} 不大于零
     */
    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 {@code LinkedBlockingQueue}，最初包含给定集合中的元素，
     * 按照集合迭代器的遍历顺序添加。
     *
     * @param c 初始包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public LinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        final ReentrantLock putLock = this.putLock;
        putLock.lock(); // 从未争用，但对可见性是必要的
        try {
            int n = 0;
            for (E e : c) {
                if (e == null)
                    throw new NullPointerException();
                if (n == capacity)
                    throw new IllegalStateException("Queue full");
                enqueue(new Node<E>(e));
                ++n;
            }
            count.set(n);
        } finally {
            putLock.unlock();
        }
    }

    // 该文档注释被重写以删除对大于 Integer.MAX_VALUE 的集合的引用
    /**
     * 返回此队列中的元素数量。
     *
     * @return 此队列中的元素数量
     */
    public int size() {
        return count.get();
    }

    // 该文档注释是继承的文档注释的修改副本，删除了对无限队列的引用
    /**
     * 返回此队列在没有内存或资源限制的情况下可以理想地（在没有阻塞的情况下）接受的额外元素数量。这始终等于此队列的初始容量减去此队列的当前 {@code size}。
     *
     * <p>请注意，您 <em>不能</em> 通过检查 {@code remainingCapacity} 来确定插入元素是否成功，因为可能有其他线程正在插入或移除元素。
     */
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * 将指定的元素插入此队列的尾部，必要时等待空间可用。
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        // 注意：所有 put/take 等方法的约定是预先设置一个局部变量
        // 持有 count 负值，表示失败，除非设置。
        int c = -1;
        Node<E> node = new Node<E>(e);
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * 注意，即使 count 未受锁保护，它仍在等待条件中使用。这是因为在此点上 count 只能减少（所有其他 put 被锁排除），并且如果它从容量变为其他值，它（或某些其他等待的 put）将被通知。同样适用于所有其他等待条件中的 count 使用。
             */
            while (count.get() == capacity) {
                notFull.await();
            }
            enqueue(node);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
    }

    /**
     * 将指定的元素插入此队列的尾部，必要时等待指定的等待时间以使空间可用。
     *
     * @return 如果成功则返回 {@code true}，如果指定的等待时间过去后没有空间可用则返回 {@code false}
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {

        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {
                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(new Node<E>(e));
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return true;
    }

    /**
     * 如果可以立即插入指定的元素而不超过队列的容量，则将其插入此队列的尾部，
     * 成功时返回 {@code true}，如果此队列已满则返回 {@code false}。
     * 当使用容量受限的队列时，此方法通常优于方法 {@link BlockingQueue#add add}，因为后者只能通过抛出异常来失败。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final AtomicInteger count = this.count;
        if (count.get() == capacity)
            return false;
        int c = -1;
        Node<E> node = new Node<E>(e);
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                enqueue(node);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                    notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return c >= 0;
    }

    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }


    /**
     * 从队列中移除内部节点 p，其前驱为 trail。
     */
    void unlink(Node<E> p, Node<E> trail) {
        // assert isFullyLocked();
        // 不改变 p.next，以允许遍历 p 的迭代器保持其弱一致性保证。
        p.item = null;
        trail.next = p.next;
        if (last == p)
            last = trail;
        if (count.getAndDecrement() == capacity)
            notFull.signal();
    }

    /**
     * 如果队列中存在指定元素的一个实例，则移除该实例。更正式地说，移除一个元素 {@code e} 使得 {@code o.equals(e)} 成立，
     * 如果队列包含一个或多个这样的元素。
     * 如果队列包含指定的元素（或等效地，如果此调用导致队列发生变化），则返回 {@code true}。
     *
     * @param o 要从队列中移除的元素，如果存在
     * @return {@code true} 如果此调用导致队列发生变化
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> trail = head, p = trail.next;
                 p != null;
                 trail = p, p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p, trail);
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 如果队列包含指定的元素，则返回 {@code true}。更正式地说，如果且仅当队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)} 成立时，
     * 返回 {@code true}。
     *
     * @param o 要检查是否包含在队列中的对象
     * @return {@code true} 如果队列包含指定的元素
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> p = head.next; p != null; p = p.next)
                if (o.equals(p.item))
                    return true;
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 返回一个包含队列中所有元素的数组，按正确顺序排列。
     *
     * <p>返回的数组是“安全的”，即没有对它的引用被此队列维护。（换句话说，此方法必须分配一个新的数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含队列中所有元素的数组
     */
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = p.item;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 返回一个包含队列中所有元素的数组，按正确顺序排列；返回数组的运行时类型是指定数组的运行时类型。如果队列适合指定的数组，则返回该数组。
     * 否则，分配一个具有指定数组的运行时类型和队列大小的新数组。
     *
     * <p>如果队列适合指定的数组且有剩余空间（即，数组的元素多于队列的元素），则数组中紧随队列末尾的元素被设置为 {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，
     * 并可能在某些情况下节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知只包含字符串的队列。以下代码可用于将队列转储到新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 在功能上等同于 {@code toArray()}。
     *
     * @param a 要存储队列元素的数组，如果它足够大；否则，为此目的分配一个具有相同运行时类型的新数组
     * @return 一个包含队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);

            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = (T)p.item;
            if (a.length > k)
                a[k] = null;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    public String toString() {
        fullyLock();
        try {
            Node<E> p = head.next;
            if (p == null)
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                E e = p.item;
                sb.append(e == this ? "(this Collection)" : e);
                p = p.next;
                if (p == null)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 原子地移除队列中的所有元素。调用此方法返回后，队列将为空。
     */
    public void clear() {
        fullyLock();
        try {
            for (Node<E> p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = last;
            // assert head.item == null && head.next == null;
            if (count.getAndSet(0) == capacity)
                notFull.signal();
        } finally {
            fullyUnlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
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
        boolean signalNotFull = false;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            int n = Math.min(maxElements, count.get());
            // count.get 提供对前 n 个节点的可见性
            Node<E> h = head;
            int i = 0;
            try {
                while (i < n) {
                    Node<E> p = h.next;
                    c.add(p.item);
                    p.item = null;
                    h.next = h;
                    h = p;
                    ++i;
                }
                return n;
            } finally {
                // 即使 c.add() 抛出异常，也要恢复不变性
                if (i > 0) {
                    // assert h.item == null;
                    head = h;
                    signalNotFull = (count.getAndAdd(-i) == capacity);
                }
            }
        } finally {
            takeLock.unlock();
            if (signalNotFull)
                signalNotFull();
        }
    }

    /**
     * 返回一个按正确顺序遍历队列元素的迭代器。元素将按从头（head）到尾（tail）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * @return 一个按正确顺序遍历队列元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /*
         * 基本的弱一致迭代器。始终持有下一个要返回的项，以便如果 hasNext() 报告为 true，即使与 take 等操作发生竞争，我们仍然有项可以返回。
         */

        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;

        Itr() {
            fullyLock();
            try {
                current = head.next;
                if (current != null)
                    currentElement = current.item;
            } finally {
                fullyUnlock();
            }
        }

        public boolean hasNext() {
            return current != null;
        }

        /**
         * 返回 p 的下一个活节点，如果没有则返回 null。
         *
         * 与其他遍历方法不同，迭代器需要处理：
         * - 已出队的节点（p.next == p）
         * - （可能多个）内部已移除的节点（p.item == null）
         */
        private Node<E> nextNode(Node<E> p) {
            for (;;) {
                Node<E> s = p.next;
                if (s == p)
                    return head.next;
                if (s == null || s.item != null)
                    return s;
                p = s;
            }
        }

        public E next() {
            fullyLock();
            try {
                if (current == null)
                    throw new NoSuchElementException();
                E x = currentElement;
                lastRet = current;
                current = nextNode(current);
                currentElement = (current == null) ? null : current.item;
                return x;
            } finally {
                fullyUnlock();
            }
        }

        public void remove() {
            if (lastRet == null)
                throw new IllegalStateException();
            fullyLock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                for (Node<E> trail = head, p = trail.next;
                     p != null;
                     trail = p, p = p.next) {
                    if (p == node) {
                        unlink(p, trail);
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
        }
    }

    /** 一个定制的 Spliterators.IteratorSpliterator 变体 */
    static final class LBQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // 最大批处理数组大小；
        final LinkedBlockingQueue<E> queue;
        Node<E> current;    // 当前节点；直到初始化前为 null
        int batch;          // 用于拆分的批处理大小
        boolean exhausted;  // 如果没有更多节点，则为 true
        long est;           // 大小估计
        LBQSpliterator(LinkedBlockingQueue<E> queue) {
            this.queue = queue;
            this.est = queue.size();
        }

        public long estimateSize() { return est; }

        public Spliterator<E> trySplit() {
            Node<E> h;
            final LinkedBlockingQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((h = current) != null || (h = q.head.next) != null) &&
                h.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                Node<E> p = current;
                q.fullyLock();
                try {
                    if (p != null || (p = q.head.next) != null) {
                        do {
                            if ((a[i] = p.item) != null)
                                ++i;
                        } while ((p = p.next) != null && i < n);
                    }
                } finally {
                    q.fullyUnlock();
                }
                if ((current = p) == null) {
                    est = 0L;
                    exhausted = true;
                }
                else if ((est -= i) < 0L)
                    est = 0L;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingQueue<E> q = this.queue;
            if (!exhausted) {
                exhausted = true;
                Node<E> p = current;
                do {
                    E e = null;
                    q.fullyLock();
                    try {
                        if (p == null)
                            p = q.head.next;
                        while (p != null) {
                            e = p.item;
                            p = p.next;
                            if (e != null)
                                break;
                        }
                    } finally {
                        q.fullyUnlock();
                    }
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }


        }

    }

    /**
     * 返回一个遍历此队列中元素的 {@link Spliterator}。
     *
     * <p>返回的 {@code Spliterator} 是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a> 的。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 实现了 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LBQSpliterator<E>(this);
    }

    /**
     * 将此队列保存到流中（即，序列化它）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 容量被输出（int），然后是所有元素（每个都是 {@code Object}）按正确顺序，
     *             最后是一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        fullyLock();
        try {
            // 写出任何隐藏的内容，加上容量
            s.defaultWriteObject();

            // 按正确顺序写出所有元素。
            for (Node<E> p = head.next; p != null; p = p.next)
                s.writeObject(p.item);

            // 使用尾部 null 作为哨兵
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * 从流中恢复此队列（即，反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 读取容量，以及任何隐藏的内容
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // 读取所有元素并放入队列
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E)s.readObject();
            if (item == null)
                break;
            add(item);
        }
    }
}
