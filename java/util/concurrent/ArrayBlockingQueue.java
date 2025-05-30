
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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.ref.WeakReference;
import java.util.Spliterators;
import java.util.Spliterator;

/**
 * 一个由数组支持的有界 {@linkplain BlockingQueue 阻塞队列}。此队列按 FIFO（先进先出）顺序排列元素。队列的
 * <em>头</em> 是在队列中停留时间最长的元素。队列的 <em>尾</em> 是在队列中停留时间最短的元素。新元素插入队列的尾部，
 * 而队列的检索操作则从队列的头部获取元素。
 *
 * <p>这是一个经典的“有界缓冲区”，其中固定大小的数组保存生产者插入的元素和消费者提取的元素。创建后，容量不能更改。
 * 尝试将元素放入已满的队列中将导致操作阻塞；尝试从空队列中取元素也将阻塞。
 *
 * <p>此类支持可选的公平策略来确定等待的生产者和消费者线程的顺序。默认情况下，此顺序不保证。但是，将公平性设置为
 * {@code true} 构造的队列将按 FIFO 顺序授予线程访问权限。公平性通常会降低吞吐量，但减少可变性并避免饥饿。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口中所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {

    /**
     * 序列化ID。此类依赖于默认序列化，即使数组为空，也会默认序列化，否则不能声明为 final，这是此处必需的。
     */
    private static final long serialVersionUID = -817911632652898426L;

    /** 队列中的项目 */
    final Object[] items;

    /** 下一个 take, poll, peek 或 remove 的项目索引 */
    int takeIndex;

    /** 下一个 put, offer 或 add 的项目索引 */
    int putIndex;

    /** 队列中的元素数量 */
    int count;

    /*
     * 并发控制使用经典两条件算法，可以在任何教科书中找到。
     */

    /** 保护所有访问的主要锁 */
    final ReentrantLock lock;

    /** 等待 take 的条件 */
    private final Condition notEmpty;

    /** 等待 put 的条件 */
    private final Condition notFull;

    /**
     * 当前活动迭代器的共享状态，如果没有已知的迭代器则为 null。允许队列操作更新迭代器状态。
     */
    transient Itrs itrs = null;

    // 内部辅助方法

    /**
     * 循环递减 i。
     */
    final int dec(int i) {
        return ((i == 0) ? items.length : i) - 1;
    }

    /**
     * 返回索引 i 处的项目。
     */
    @SuppressWarnings("unchecked")
    final E itemAt(int i) {
        return (E) items[i];
    }

    /**
     * 如果参数为 null，则抛出 NullPointerException。
     *
     * @param v 元素
     */
    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    /**
     * 在当前 put 位置插入元素，前进并发出信号。仅在持有锁时调用。
     */
    private void enqueue(E x) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        final Object[] items = this.items;
        items[putIndex] = x;
        if (++putIndex == items.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

    /**
     * 从当前 take 位置提取元素，前进并发出信号。仅在持有锁时调用。
     */
    private E dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        final Object[] items = this.items;
        @SuppressWarnings("unchecked")
        E x = (E) items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length)
            takeIndex = 0;
        count--;
        if (itrs != null)
            itrs.elementDequeued();
        notFull.signal();
        return x;
    }

    /**
     * 删除数组索引 removeIndex 处的项目。用于 remove(Object) 和 iterator.remove。
     * 仅在持有锁时调用。
     */
    void removeAt(final int removeIndex) {
        // assert lock.getHoldCount() == 1;
        // assert items[removeIndex] != null;
        // assert removeIndex >= 0 && removeIndex < items.length;
        final Object[] items = this.items;
        if (removeIndex == takeIndex) {
            // 删除前端项目；只需前进
            items[takeIndex] = null;
            if (++takeIndex == items.length)
                takeIndex = 0;
            count--;
            if (itrs != null)
                itrs.elementDequeued();
        } else {
            // 内部删除

            // 将所有其他项目滑动到 putIndex。
            final int putIndex = this.putIndex;
            for (int i = removeIndex;;) {
                int next = i + 1;
                if (next == items.length)
                    next = 0;
                if (next != putIndex) {
                    items[i] = items[next];
                    i = next;
                } else {
                    items[i] = null;
                    this.putIndex = i;
                    break;
                }
            }
            count--;
            if (itrs != null)
                itrs.removedAt(removeIndex);
        }
        notFull.signal();
    }

    /**
     * 创建一个具有给定（固定）容量和默认访问策略的 {@code ArrayBlockingQueue}。
     *
     * @param capacity 此队列的容量
     * @throws IllegalArgumentException 如果 {@code capacity < 1}
     */
    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    /**
     * 创建一个具有给定（固定）容量和指定访问策略的 {@code ArrayBlockingQueue}。
     *
     * @param capacity 此队列的容量
     * @param fair 如果 {@code true} 则队列访问对于阻塞在插入或移除上的线程按 FIFO 顺序处理；
     *        如果 {@code false} 则访问顺序未指定。
     * @throws IllegalArgumentException 如果 {@code capacity < 1}
     */
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }

    /**
     * 创建一个具有给定（固定）容量、指定访问策略并最初包含给定集合的元素的 {@code ArrayBlockingQueue}，
     * 按照集合迭代器的遍历顺序添加元素。
     *
     * @param capacity 此队列的容量
     * @param fair 如果 {@code true} 则队列访问对于阻塞在插入或移除上的线程按 FIFO 顺序处理；
     *        如果 {@code false} 则访问顺序未指定。
     * @param c 初始包含的集合
     * @throws IllegalArgumentException 如果 {@code capacity} 小于 {@code c.size()}，或小于 1。
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public ArrayBlockingQueue(int capacity, boolean fair,
                              Collection<? extends E> c) {
        this(capacity, fair);

        final ReentrantLock lock = this.lock;
        lock.lock(); // 仅用于可见性，而不是互斥
        try {
            int i = 0;
            try {
                for (E e : c) {
                    checkNotNull(e);
                    items[i++] = e;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException();
            }
            count = i;
            putIndex = (i == capacity) ? 0 : i;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果可以立即插入指定元素而不超过队列的容量，则将其插入队列的尾部，成功时返回 {@code true}，
     * 如果队列已满则抛出 {@code IllegalStateException}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果此队列已满
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return super.add(e);
    }

    /**
     * 如果可以立即插入指定元素而不超过队列的容量，则将其插入队列的尾部，成功时返回 {@code true}，
     * 如果队列已满则返回 {@code false}。此方法通常优于方法 {@link #add}，因为后者只能通过抛出异常来失败。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count == items.length)
                return false;
            else {
                enqueue(e);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果队列已满，则等待空间可用后将指定元素插入队列的尾部。
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length)
                notFull.await();
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果队列已满，则等待指定的等待时间，直到空间可用后将指定元素插入队列的尾部。
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {

        checkNotNull(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (count == 0) ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0)
                notEmpty.await();
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return itemAt(takeIndex); // 队列为空时为 null
        } finally {
            lock.unlock();
        }
    }

    // 此文档注释被重写以删除对大于 Integer.MAX_VALUE 的集合的引用
    /**
     * 返回此队列中的元素数量。
     *
     * @return 此队列中的元素数量
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    // 此文档注释是继承的文档注释的修改副本，不包含对无限制队列的引用。
    /**
     * 返回此队列在不阻塞的情况下（在没有内存或资源限制的情况下）可以理想地接受的额外元素数量。这始终等于此队列的初始容量
     * 减去此队列的当前 {@code size}。
     *
     * <p>请注意，您 <em>不能</em> 通过检查 {@code remainingCapacity} 来确定插入元素是否总是成功，因为可能有其他线程
     * 即将插入或移除元素。
     */
    public int remainingCapacity() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return items.length - count;
        } finally {
            lock.unlock();
        }
    }


                /**
     * 从这个队列中移除指定元素的一个实例（如果存在）。更正式地说，移除一个元素 {@code e} 使得
     * {@code o.equals(e)}，如果这个队列包含一个或多个这样的元素。
     * 如果这个队列包含指定的元素（或者等价地，如果这个队列因为这个调用而改变），则返回 {@code true}。
     *
     * <p>在基于循环数组的队列中移除内部元素是一个本质上缓慢且破坏性的操作，因此应该仅在特殊情况下进行，
     * 理想情况下，只有当队列已知不被其他线程访问时才进行。
     *
     * @param o 要从这个队列中移除的元素，如果存在
     * @return {@code true} 如果这个队列因为这个调用而改变
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    if (o.equals(items[i])) {
                        removeAt(i);
                        return true;
                    }
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果这个队列包含指定的元素，则返回 {@code true}。更正式地说，如果且仅当这个队列包含
     * 至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在这个队列中的对象
     * @return {@code true} 如果这个队列包含指定的元素
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    if (o.equals(items[i]))
                        return true;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个包含这个队列中所有元素的数组，按正确的顺序排列。
     *
     * <p>返回的数组是“安全的”，即没有对它的引用被这个队列维护。（换句话说，这个方法必须分配一个新的数组）。
     * 因此，调用者可以自由地修改返回的数组。
     *
     * <p>这个方法充当基于数组和基于集合的API之间的桥梁。
     *
     * @return 一个包含这个队列中所有元素的数组
     */
    public Object[] toArray() {
        Object[] a;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            a = new Object[count];
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
        } finally {
            lock.unlock();
        }
        return a;
    }

    /**
     * 返回一个包含这个队列中所有元素的数组，按正确的顺序排列；返回数组的运行时类型是指定数组的运行时类型。
     * 如果队列适合指定的数组，则返回该数组。否则，分配一个具有指定数组的运行时类型和这个队列大小的新数组。
     *
     * <p>如果这个队列适合指定的数组且有剩余空间（即，数组的元素比这个队列多），则数组中紧接队列末尾的元素被设置为
     * {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，这个方法充当基于数组和基于集合的API之间的桥梁。此外，这个方法允许
     * 精确控制输出数组的运行时类型，并且在某些情况下可以节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知只包含字符串的队列。以下代码可以用于将队列转储到一个新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意 {@code toArray(new Object[0])} 与 {@code toArray()} 的功能相同。
     *
     * @param a 要存储队列元素的数组，如果它足够大；否则，为此目的分配一个具有相同运行时类型的新数组
     * @return 一个包含这个队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是这个队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            final int len = a.length;
            if (len < count)
                a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), count);
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
            if (len > count)
                a[count] = null;
        } finally {
            lock.unlock();
        }
        return a;
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k == 0)
                return "[]";

            final Object[] items = this.items;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = takeIndex; ; ) {
                Object e = items[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (--k == 0)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
                if (++i == items.length)
                    i = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从这个队列中原子地移除所有元素。调用返回后，队列将为空。
     */
    public void clear() {
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    items[i] = null;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
                takeIndex = putIndex;
                count = 0;
                if (itrs != null)
                    itrs.queueIsEmpty();
                for (; k > 0 && lock.hasWaiters(notFull); k--)
                    notFull.signal();
            }
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
        checkNotNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(maxElements, count);
            int take = takeIndex;
            int i = 0;
            try {
                while (i < n) {
                    @SuppressWarnings("unchecked")
                    E x = (E) items[take];
                    c.add(x);
                    items[take] = null;
                    if (++take == items.length)
                        take = 0;
                    i++;
                }
                return n;
            } finally {
                // 如果 c.add() 抛出异常，恢复不变性
                if (i > 0) {
                    count -= i;
                    takeIndex = take;
                    if (itrs != null) {
                        if (count == 0)
                            itrs.queueIsEmpty();
                        else if (i > take)
                            itrs.takeIndexWrapped();
                    }
                    for (; i > 0 && lock.hasWaiters(notFull); i--)
                        notFull.signal();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个按正确顺序遍历这个队列元素的迭代器。元素将按从头（第一个）到尾（最后一个）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * @return 一个按正确顺序遍历这个队列元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 迭代器和队列之间的共享数据，允许队列修改更新迭代器，当元素被移除时。
     *
     * 这增加了处理一些不常见操作的复杂性，但组合使用循环数组和内部移除（即，不在头部的移除）会导致迭代器有时
     * 丢失位置和/或（重新）报告它们不应该报告的元素。为了避免这种情况，当队列有一个或多个迭代器时，它通过以下方式
     * 保持迭代器状态的一致性：
     *
     * (1) 跟踪“周期”数，即 takeIndex 包绕回 0 的次数。
     * (2) 每当内部元素被移除（从而其他元素可能被移位）时，通过回调 removedAt 通知所有迭代器。
     *
     * 这些措施足以消除迭代器的不一致性，但不幸的是增加了维护迭代器列表的次要责任。我们通过一个简单的链表（仅在
     * 队列的锁被持有时访问）来跟踪所有活动的迭代器，链表中的元素是 Itr 的弱引用。列表通过以下三种机制进行清理：
     *
     * (1) 每当创建新迭代器时，进行一些 O(1) 检查以查找过时的列表元素。
     * (2) 每当 takeIndex 包绕回 0 时，检查已超过一个包绕周期未使用的迭代器。
     * (3) 每当队列变为空时，通知所有迭代器并丢弃整个数据结构。
     *
     * 因此，除了为正确性必要的 removedAt 回调外，迭代器还有 shutdown 和 takeIndexWrapped 回调，这些回调帮助
     * 从列表中移除过时的迭代器。
     *
     * 每当检查列表元素时，如果 GC 确定迭代器已被丢弃，或者迭代器报告它已“分离”（不再需要任何状态更新），则
     * 该元素将被清除。当 takeIndex 从未前进，迭代器在耗尽前被丢弃，且所有移除都是内部移除时，开销最大，这种情况下
     * 所有过时的迭代器将由 GC 发现。但即使在这种情况下，我们也不会增加摊销复杂度。
     *
     * 必须小心避免列表清理方法重新进入另一个这样的方法，以防止细微的损坏错误。
     */
    class Itrs {

        /**
         * 链表中弱迭代器引用的节点。
         */
        private class Node extends WeakReference<Itr> {
            Node next;

            Node(Itr iterator, Node next) {
                super(iterator);
                this.next = next;
            }
        }

        /** 每当 takeIndex 包绕回 0 时递增 */
        int cycles = 0;

        /** 链表中的弱迭代器引用 */
        private Node head;

        /** 用于清除过时的迭代器 */
        private Node sweeper = null;

        private static final int SHORT_SWEEP_PROBES = 4;
        private static final int LONG_SWEEP_PROBES = 16;

        Itrs(Itr initial) {
            register(initial);
        }

        /**
         * 清除 itrs，查找并清除过时的迭代器。
         * 如果至少找到一个，则更努力地查找更多。
         * 仅从迭代线程调用。
         *
         * @param tryHarder 是否以更努力的模式开始，因为已知至少有一个迭代器需要收集
         */
        void doSomeSweeping(boolean tryHarder) {
            // assert lock.getHoldCount() == 1;
            // assert head != null;
            int probes = tryHarder ? LONG_SWEEP_PROBES : SHORT_SWEEP_PROBES;
            Node o, p;
            final Node sweeper = this.sweeper;
            boolean passedGo;   // 限制搜索到一次完整的扫描

            if (sweeper == null) {
                o = null;
                p = head;
                passedGo = true;
            } else {
                o = sweeper;
                p = o.next;
                passedGo = false;
            }

            for (; probes > 0; probes--) {
                if (p == null) {
                    if (passedGo)
                        break;
                    o = null;
                    p = head;
                    passedGo = true;
                }
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.isDetached()) {
                    // 找到一个已丢弃/耗尽的迭代器
                    probes = LONG_SWEEP_PROBES; // "更努力"
                    // 解链 p
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        head = next;
                        if (next == null) {
                            // 没有更多需要跟踪的迭代器；退休
                            itrs = null;
                            return;
                        }
                    }
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }


                        this.sweeper = (p == null) ? null : o;
        }

        /**
         * 向跟踪迭代器的链表中添加一个新的迭代器。
         */
        void register(Itr itr) {
            // assert lock.getHoldCount() == 1;
            head = new Node(itr, head);
        }

        /**
         * 在 takeIndex 滚动到 0 时调用。
         *
         * 通知所有迭代器，并清除任何已过时的迭代器。
         */
        void takeIndexWrapped() {
            // assert lock.getHoldCount() == 1;
            cycles++;
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.takeIndexWrapped()) {
                    // 解链 p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            if (head == null)   // 没有更多要跟踪的迭代器
                itrs = null;
        }

        /**
         * 在队列中发生内部移除（不在 takeIndex 处）时调用。
         *
         * 通知所有迭代器，并清除任何已过时的迭代器。
         */
        void removedAt(int removedIndex) {
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.removedAt(removedIndex)) {
                    // 解链 p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            if (head == null)   // 没有更多要跟踪的迭代器
                itrs = null;
        }

        /**
         * 在队列变为空时调用。
         *
         * 通知所有活动的迭代器队列为空，清除所有弱引用，并解除 itrs 数据结构的链接。
         */
        void queueIsEmpty() {
            // assert lock.getHoldCount() == 1;
            for (Node p = head; p != null; p = p.next) {
                Itr it = p.get();
                if (it != null) {
                    p.clear();
                    it.shutdown();
                }
            }
            head = null;
            itrs = null;
        }

        /**
         * 在元素出队（在 takeIndex 处）时调用。
         */
        void elementDequeued() {
            // assert lock.getHoldCount() == 1;
            if (count == 0)
                queueIsEmpty();
            else if (takeIndex == 0)
                takeIndexWrapped();
        }
    }

    /**
     * ArrayBlockingQueue 的迭代器。
     *
     * 为了保持与 put 和 take 操作的弱一致性，我们提前读取一个槽位，以避免报告 hasNext 为 true 但随后没有元素返回的情况。
     *
     * 当所有索引为负数或 hasNext 返回 false 时，我们切换到“分离”模式（允许在没有 GC 帮助的情况下从 itrs 中快速解除链接）。这允许迭代器完全准确地跟踪并发更新，除了用户在 hasNext() 返回 false 后调用 Iterator.remove() 的情况。即使在这种情况下，我们通过跟踪预期要移除的元素（在 lastItem 中）来确保不会移除错误的元素。是的，如果在分离模式下由于交错的内部移除操作导致 lastItem 移动，我们可能会失败。
     */
    private class Itr implements Iterator<E> {
        /** 用于查找下一个 nextItem 的索引；在结束时为 NONE */
        private int cursor;

        /** 下一次调用 next() 时要返回的元素；如果没有则为 null */
        private E nextItem;

        /** nextItem 的索引；如果没有则为 NONE，如果已被其他地方移除则为 REMOVED */
        private int nextIndex;

        /** 最后返回的元素；如果没有或未分离则为 null。 */
        private E lastItem;

        /** lastItem 的索引，如果没有则为 NONE，如果已被其他地方移除则为 REMOVED */
        private int lastRet;

        /** 上一次 takeIndex 的值，或在分离模式下为 DETACHED */
        private int prevTakeIndex;

        /** 上一次 itrs.cycles 的值 */
        private int prevCycles;

        /** 特殊索引值，表示“不可用”或“未定义” */
        private static final int NONE = -1;

        /**
         * 特殊索引值，表示“被其他地方移除”，即，被除 this.remove() 之外的某些操作移除。
         */
        private static final int REMOVED = -2;

        /** prevTakeIndex 的特殊值，表示“分离模式” */
        private static final int DETACHED = -3;

        Itr() {
            // assert lock.getHoldCount() == 0;
            lastRet = NONE;
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (count == 0) {
                    // assert itrs == null;
                    cursor = NONE;
                    nextIndex = NONE;
                    prevTakeIndex = DETACHED;
                } else {
                    final int takeIndex = ArrayBlockingQueue.this.takeIndex;
                    prevTakeIndex = takeIndex;
                    nextItem = itemAt(nextIndex = takeIndex);
                    cursor = incCursor(takeIndex);
                    if (itrs == null) {
                        itrs = new Itrs(this);
                    } else {
                        itrs.register(this); // 顺序很重要
                        itrs.doSomeSweeping(false);
                    }
                    prevCycles = itrs.cycles;
                    // assert takeIndex >= 0;
                    // assert prevTakeIndex == takeIndex;
                    // assert nextIndex >= 0;
                    // assert nextItem != null;
                }
            } finally {
                lock.unlock();
            }
        }

        boolean isDetached() {
            // assert lock.getHoldCount() == 1;
            return prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            // assert lock.getHoldCount() == 1;
            if (++index == items.length)
                index = 0;
            if (index == putIndex)
                index = NONE;
            return index;
        }

        /**
         * 如果从 prevTakeIndex 开始的给定数量的出队操作使索引失效，则返回 true。
         */
        private boolean invalidated(int index, int prevTakeIndex,
                                    long dequeues, int length) {
            if (index < 0)
                return false;
            int distance = index - prevTakeIndex;
            if (distance < 0)
                distance += length;
            return dequeues > distance;
        }

        /**
         * 调整索引以包含自上次操作以来的所有出队操作。仅从迭代线程调用。
         */
        private void incorporateDequeues() {
            // assert lock.getHoldCount() == 1;
            // assert itrs != null;
            // assert !isDetached();
            // assert count > 0;

            final int cycles = itrs.cycles;
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;

            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
                final int len = items.length;
                // 从上次操作到现在的出队数量
                long dequeues = (cycles - prevCycles) * len
                    + (takeIndex - prevTakeIndex);

                // 检查索引是否失效
                if (invalidated(lastRet, prevTakeIndex, dequeues, len))
                    lastRet = REMOVED;
                if (invalidated(nextIndex, prevTakeIndex, dequeues, len))
                    nextIndex = REMOVED;
                if (invalidated(cursor, prevTakeIndex, dequeues, len))
                    cursor = takeIndex;

                if (cursor < 0 && nextIndex < 0 && lastRet < 0)
                    detach();
                else {
                    this.prevCycles = cycles;
                    this.prevTakeIndex = takeIndex;
                }
            }
        }

        /**
         * 当 itrs 应该停止跟踪此迭代器时调用，原因可能是没有更多的索引要更新（cursor < 0 && nextIndex < 0 && lastRet < 0），或者作为一个特殊情况，当 lastRet >= 0 时，因为 hasNext() 即将首次返回 false。仅从迭代线程调用。
         */
        private void detach() {
            // 切换到分离模式
            // assert lock.getHoldCount() == 1;
            // assert cursor == NONE;
            // assert nextIndex < 0;
            // assert lastRet < 0 || nextItem == null;
            // assert lastRet < 0 ^ lastItem != null;
            if (prevTakeIndex >= 0) {
                // assert itrs != null;
                prevTakeIndex = DETACHED;
                // 尝试从 itrs 中解除链接（但不要太努力）
                itrs.doSomeSweeping(true);
            }
        }

        /**
         * 为了性能原因，我们希望在 hasNext 中不获取锁。为此，我们只访问不会被队列修改操作触发的更新操作修改的字段（即 nextItem）。
         */
        public boolean hasNext() {
            // assert lock.getHoldCount() == 0;
            if (nextItem != null)
                return true;
            noNext();
            return false;
        }

        private void noNext() {
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                // assert cursor == NONE;
                // assert nextIndex == NONE;
                if (!isDetached()) {
                    // assert lastRet >= 0;
                    incorporateDequeues(); // 可能会更新 lastRet
                    if (lastRet >= 0) {
                        lastItem = itemAt(lastRet);
                        // assert lastItem != null;
                        detach();
                    }
                }
                // assert isDetached();
                // assert lastRet < 0 ^ lastItem != null;
            } finally {
                lock.unlock();
            }
        }

        public E next() {
            // assert lock.getHoldCount() == 0;
            final E x = nextItem;
            if (x == null)
                throw new NoSuchElementException();
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached())
                    incorporateDequeues();
                // assert nextIndex != NONE;
                // assert lastItem == null;
                lastRet = nextIndex;
                final int cursor = this.cursor;
                if (cursor >= 0) {
                    nextItem = itemAt(nextIndex = cursor);
                    // assert nextItem != null;
                    this.cursor = incCursor(cursor);
                } else {
                    nextIndex = NONE;
                    nextItem = null;
                }
            } finally {
                lock.unlock();
            }
            return x;
        }

        public void remove() {
            // assert lock.getHoldCount() == 0;
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached())
                    incorporateDequeues(); // 可能会更新 lastRet 或分离
                final int lastRet = this.lastRet;
                this.lastRet = NONE;
                if (lastRet >= 0) {
                    if (!isDetached())
                        removeAt(lastRet);
                    else {
                        final E lastItem = this.lastItem;
                        // assert lastItem != null;
                        this.lastItem = null;
                        if (itemAt(lastRet) == lastItem)
                            removeAt(lastRet);
                    }
                } else if (lastRet == NONE)
                    throw new IllegalStateException();
                // else lastRet == REMOVED，最后返回的元素之前已被其他操作异步移除，所以没有什么可做的。

                if (cursor < 0 && nextIndex < 0)
                    detach();
            } finally {
                lock.unlock();
                // assert lastRet == NONE;
                // assert lastItem == null;
            }
        }

        /**
         * 通知迭代器队列为空，或已远远落后，因此应放弃进一步的迭代，除非可能返回一个更多元素的 next()，如 hasNext() 返回 true 所承诺的那样。
         */
        void shutdown() {
            // assert lock.getHoldCount() == 1;
            cursor = NONE;
            if (nextIndex >= 0)
                nextIndex = REMOVED;
            if (lastRet >= 0) {
                lastRet = REMOVED;
                lastItem = null;
            }
            prevTakeIndex = DETACHED;
            // 不要将 nextItem 设置为 null，因为我们必须继续能够在 next() 上返回它。
            //
            // 调用者会在方便时从 itrs 中解除链接。
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0)
                distance += length;
            return distance;
        }

        /**
         * 在队列中发生内部移除（不在 takeIndex 处）时调用。
         *
         * @return 如果此迭代器应从 itrs 中解除链接，则返回 true
         */
        boolean removedAt(int removedIndex) {
            // assert lock.getHoldCount() == 1;
            if (isDetached())
                return true;

            final int cycles = itrs.cycles;
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;
            final int len = items.length;
            int cycleDiff = cycles - prevCycles;
            if (removedIndex < takeIndex)
                cycleDiff++;
            final int removedDistance =
                (cycleDiff * len) + (removedIndex - prevTakeIndex);
            // assert removedDistance >= 0;
            int cursor = this.cursor;
            if (cursor >= 0) {
                int x = distance(cursor, prevTakeIndex, len);
                if (x == removedDistance) {
                    if (cursor == putIndex)
                        this.cursor = cursor = NONE;
                }
                else if (x > removedDistance) {
                    // assert cursor != prevTakeIndex;
                    this.cursor = cursor = dec(cursor);
                }
            }
            int lastRet = this.lastRet;
            if (lastRet >= 0) {
                int x = distance(lastRet, prevTakeIndex, len);
                if (x == removedDistance)
                    this.lastRet = lastRet = REMOVED;
                else if (x > removedDistance)
                    this.lastRet = lastRet = dec(lastRet);
            }
            int nextIndex = this.nextIndex;
            if (nextIndex >= 0) {
                int x = distance(nextIndex, prevTakeIndex, len);
                if (x == removedDistance)
                    this.nextIndex = nextIndex = REMOVED;
                else if (x > removedDistance)
                    this.nextIndex = nextIndex = dec(nextIndex);
            }
            else if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
                this.prevTakeIndex = DETACHED;
                return true;
            }
            return false;
        }


                    /**
         * 当 takeIndex 缠绕回零时调用。
         *
         * @return 如果此迭代器应从 itrs 中断开连接，则返回 true
         */
        boolean takeIndexWrapped() {
            // assert lock.getHoldCount() == 1;
            if (isDetached())
                return true;
            if (itrs.cycles - prevCycles > 1) {
                // 所有在上次操作时存在的元素都已消失，因此放弃进一步的迭代。
                shutdown();
                return true;
            }
            return false;
        }

//         /** Uncomment for debugging. */
//         public String toString() {
//             return ("cursor=" + cursor + " " +
//                     "nextIndex=" + nextIndex + " " +
//                     "lastRet=" + lastRet + " " +
//                     "nextItem=" + nextItem + " " +
//                     "lastItem=" + lastItem + " " +
//                     "prevCycles=" + prevCycles + " " +
//                     "prevTakeIndex=" + prevTakeIndex + " " +
//                     "size()=" + size() + " " +
//                     "remainingCapacity()=" + remainingCapacity());
//         }
    }

    /**
     * 返回一个 {@link Spliterator}，遍历此队列中的元素。
     *
     * <p>返回的 spliterator 是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 实现 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个 {@code Spliterator}，遍历此队列中的元素
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (this, Spliterator.ORDERED | Spliterator.NONNULL |
             Spliterator.CONCURRENT);
    }

    /**
     * 反序列化此队列，然后检查一些不变量。
     *
     * @param s 输入流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.InvalidObjectException 如果不变量被违反
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        // 读取 items 数组和各种字段
        s.defaultReadObject();

        // 检查 count 和索引字段的不变量。注意
        // 如果 putIndex==takeIndex，count 可以是 0 或 items.length。
        if (items.length == 0 ||
            takeIndex < 0 || takeIndex >= items.length ||
            putIndex  < 0 || putIndex  >= items.length ||
            count < 0     || count     >  items.length ||
            Math.floorMod(putIndex - takeIndex, items.length) !=
            Math.floorMod(count, items.length)) {
            throw new java.io.InvalidObjectException("invariants violated");
        }
    }
}
