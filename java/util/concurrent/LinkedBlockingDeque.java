
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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 一个基于链接节点的可选有界 {@linkplain BlockingDeque 阻塞双端队列}。
 *
 * <p>可选的容量边界构造参数用于防止过度扩展。如果未指定容量，则默认为 {@link Integer#MAX_VALUE}。除非插入操作会使双端队列超出容量，否则会动态创建链接节点。
 *
 * <p>大多数操作在常数时间内运行（忽略阻塞所花费的时间）。例外包括 {@link #remove(Object) remove}、
 * {@link #removeFirstOccurrence removeFirstOccurrence}、{@link #removeLastOccurrence removeLastOccurrence}、
 * {@link #contains contains}、{@link #iterator iterator.remove()} 和批量操作，这些操作在线性时间内运行。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口中的所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.6
 * @author  Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class LinkedBlockingDeque<E>
    extends AbstractQueue<E>
    implements BlockingDeque<E>, java.io.Serializable {

    /*
     * 实现为一个由单个锁保护的简单双向链表，并使用条件来管理阻塞。
     *
     * 为了实现弱一致性迭代器，似乎需要保持从已出队节点到其前驱节点的 GC 可达性。
     * 这会导致两个问题：
     * - 允许恶意迭代器导致无界内存保留
     * - 如果一个节点在活动时被提升到老年代，会导致老节点与新节点之间的跨代链接，这会使分代垃圾收集器难以处理，导致重复的全量收集。
     * 然而，只有未删除的节点需要从前驱已出队的节点可达，且可达性不一定必须是 GC 理解的那种。我们使用将刚刚出队的节点链接到自身的技巧。
     * 这样的自链接隐式地意味着跳转到“first”（对于 next 链接）或“last”（对于 prev 链接）。
     */

    /*
     * 我们有“菱形”多重接口/抽象类继承，这引入了歧义。通常我们希望结合 BlockingDeque 的 Javadoc 和 AbstractQueue 的实现，
     * 因此许多方法规范在这里重复。
     */

    private static final long serialVersionUID = -387911632671998426L;

    /** 双向链表节点类 */
    static final class Node<E> {
        /**
         * 项目，如果此节点已被移除，则为 null。
         */
        E item;

        /**
         * 以下之一：
         * - 真实的前驱节点
         * - 本节点，意味着前驱是尾节点
         * - null，意味着没有前驱
         */
        Node<E> prev;

        /**
         * 以下之一：
         * - 真实的后继节点
         * - 本节点，意味着后继是头节点
         * - null，意味着没有后继
         */
        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

    /**
     * 指向第一个节点的指针。
     * 不变量：(first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * 指向最后一个节点的指针。
     * 不变量：(first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

    /** 双端队列中的元素数量 */
    private transient int count;

    /** 双端队列的最大容量 */
    private final int capacity;

    /** 主锁，保护所有访问 */
    final ReentrantLock lock = new ReentrantLock();

    /** 等待取操作的条件 */
    private final Condition notEmpty = lock.newCondition();

    /** 等待放操作的条件 */
    private final Condition notFull = lock.newCondition();

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 {@code LinkedBlockingDeque}。
     */
    public LinkedBlockingDeque() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 创建一个具有给定（固定）容量的 {@code LinkedBlockingDeque}。
     *
     * @param capacity 此双端队列的容量
     * @throws IllegalArgumentException 如果 {@code capacity} 小于 1
     */
    public LinkedBlockingDeque(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
    }

    /**
     * 创建一个容量为 {@link Integer#MAX_VALUE} 的 {@code LinkedBlockingDeque}，初始包含给定集合的元素，
     * 按照集合迭代器的遍历顺序添加。
     *
     * @param c 初始包含的集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public LinkedBlockingDeque(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        final ReentrantLock lock = this.lock;
        lock.lock(); // 从未争用，但对可见性是必要的
        try {
            for (E e : c) {
                if (e == null)
                    throw new NullPointerException();
                if (!linkLast(new Node<E>(e)))
                    throw new IllegalStateException("Deque full");
            }
        } finally {
            lock.unlock();
        }
    }


    // 基本链接和取消链接操作，仅在持有锁时调用

    /**
     * 将节点链接为第一个元素，或在已满时返回 false。
     */
    private boolean linkFirst(Node<E> node) {
        // assert lock.isHeldByCurrentThread();
        if (count >= capacity)
            return false;
        Node<E> f = first;
        node.next = f;
        first = node;
        if (last == null)
            last = node;
        else
            f.prev = node;
        ++count;
        notEmpty.signal();
        return true;
    }

    /**
     * 将节点链接为最后一个元素，或在已满时返回 false。
     */
    private boolean linkLast(Node<E> node) {
        // assert lock.isHeldByCurrentThread();
        if (count >= capacity)
            return false;
        Node<E> l = last;
        node.prev = l;
        last = node;
        if (first == null)
            first = node;
        else
            l.next = node;
        ++count;
        notEmpty.signal();
        return true;
    }

    /**
     * 移除并返回第一个元素，或在为空时返回 null。
     */
    private E unlinkFirst() {
        // assert lock.isHeldByCurrentThread();
        Node<E> f = first;
        if (f == null)
            return null;
        Node<E> n = f.next;
        E item = f.item;
        f.item = null;
        f.next = f; // 帮助 GC
        first = n;
        if (n == null)
            last = null;
        else
            n.prev = null;
        --count;
        notFull.signal();
        return item;
    }

    /**
     * 移除并返回最后一个元素，或在为空时返回 null。
     */
    private E unlinkLast() {
        // assert lock.isHeldByCurrentThread();
        Node<E> l = last;
        if (l == null)
            return null;
        Node<E> p = l.prev;
        E item = l.item;
        l.item = null;
        l.prev = l; // 帮助 GC
        last = p;
        if (p == null)
            first = null;
        else
            p.next = null;
        --count;
        notFull.signal();
        return item;
    }

    /**
     * 取消链接 x。
     */
    void unlink(Node<E> x) {
        // assert lock.isHeldByCurrentThread();
        Node<E> p = x.prev;
        Node<E> n = x.next;
        if (p == null) {
            unlinkFirst();
        } else if (n == null) {
            unlinkLast();
        } else {
            p.next = n;
            n.prev = p;
            x.item = null;
            // 不要修改 x 的链接。它们可能仍被迭代器使用。
            --count;
            notFull.signal();
        }
    }

    // BlockingDeque 方法

    /**
     * @throws IllegalStateException 如果此双端队列已满
     * @throws NullPointerException {@inheritDoc}
     */
    public void addFirst(E e) {
        if (!offerFirst(e))
            throw new IllegalStateException("Deque full");
    }

    /**
     * @throws IllegalStateException 如果此双端队列已满
     * @throws NullPointerException  {@inheritDoc}
     */
    public void addLast(E e) {
        if (!offerLast(e))
            throw new IllegalStateException("Deque full");
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offerFirst(E e) {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return linkFirst(node);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offerLast(E e) {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return linkLast(node);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public void putFirst(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (!linkFirst(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public void putLast(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (!linkLast(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public boolean offerFirst(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (!linkFirst(node)) {
                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public boolean offerLast(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<E> node = new Node<E>(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (!linkLast(node)) {
                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeFirst() {
        E x = pollFirst();
        if (x == null) throw new NoSuchElementException();
        return x;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeLast() {
        E x = pollLast();
        if (x == null) throw new NoSuchElementException();
        return x;
    }

    public E pollFirst() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return unlinkFirst();
        } finally {
            lock.unlock();
        }
    }

    public E pollLast() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return unlinkLast();
        } finally {
            lock.unlock();
        }
    }

    public E takeFirst() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E x;
            while ( (x = unlinkFirst()) == null)
                notEmpty.await();
            return x;
        } finally {
            lock.unlock();
        }
    }

    public E takeLast() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E x;
            while ( (x = unlinkLast()) == null)
                notEmpty.await();
            return x;
        } finally {
            lock.unlock();
        }
    }

    public E pollFirst(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            E x;
            while ( (x = unlinkFirst()) == null) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return x;
        } finally {
            lock.unlock();
        }
    }


                public E pollLast(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            E x;
            while ( (x = unlinkLast()) == null) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getFirst() {
        E x = peekFirst();
        if (x == null) throw new NoSuchElementException();
        return x;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getLast() {
        E x = peekLast();
        if (x == null) throw new NoSuchElementException();
        return x;
    }

    public E peekFirst() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (first == null) ? null : first.item;
        } finally {
            lock.unlock();
        }
    }

    public E peekLast() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (last == null) ? null : last.item;
        } finally {
            lock.unlock();
        }
    }

    public boolean removeFirstOccurrence(Object o) {
        if (o == null) return false;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean removeLastOccurrence(Object o) {
        if (o == null) return false;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = last; p != null; p = p.prev) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // BlockingQueue 方法

    /**
     * 在此双端队列的末尾插入指定的元素，除非这会违反容量限制。当使用容量受限的双端队列时，
     * 通常更可取的是使用方法 {@link #offer(Object) offer}。
     *
     * <p>此方法等同于 {@link #addLast}。
     *
     * @throws IllegalStateException 如果此双端队列已满
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        putLast(e);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        return offerLast(e, timeout, unit);
    }

    /**
     * 检索并移除此双端队列表示的队列的头部。此方法与 {@link #poll poll} 的区别在于，
     * 如果此双端队列为空，则此方法会抛出异常。
     *
     * <p>此方法等同于 {@link #removeFirst() removeFirst}。
     *
     * @return 此双端队列表示的队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    public E take() throws InterruptedException {
        return takeFirst();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    /**
     * 检索但不移除此双端队列表示的队列的头部。此方法与 {@link #peek peek} 的区别在于，
     * 如果此双端队列为空，则此方法会抛出异常。
     *
     * <p>此方法等同于 {@link #getFirst() getFirst}。
     *
     * @return 此双端队列表示的队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    /**
     * 返回此双端队列在不阻塞的情况下可以理想地（在没有内存或资源限制的情况下）接受的额外元素的数量。
     * 这始终等于此双端队列的初始容量减去此双端队列的当前 {@code size}。
     *
     * <p>请注意，您 <em>不能</em> 仅通过检查 {@code remainingCapacity} 来确定插入元素是否总是会成功，
     * 因为可能有其他线程即将插入或移除元素。
     */
    public int remainingCapacity() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return capacity - count;
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
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(maxElements, count);
            for (int i = 0; i < n; i++) {
                c.add(first.item);   // 以这种方式，以防 add() 抛出异常。
                unlinkFirst();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    // 栈方法

    /**
     * @throws IllegalStateException 如果此双端队列已满
     * @throws NullPointerException {@inheritDoc}
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E pop() {
        return removeFirst();
    }

    // 集合方法

    /**
     * 从此双端队列中移除指定元素的第一个匹配项。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * <p>此方法等同于
     * {@link #removeFirstOccurrence(Object) removeFirstOccurrence}。
     *
     * @param o 如果存在，则从此双端队列中移除的元素
     * @return 如果此双端队列因调用而改变，则返回 {@code true}
     */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * 返回此双端队列中的元素数量。
     *
     * @return 此双端队列中的元素数量
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

    /**
     * 如果此双端队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果此双端队列包含至少一个满足 {@code o.equals(e)} 的元素，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此双端队列中的对象
     * @return 如果此双端队列包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = first; p != null; p = p.next)
                if (o.equals(p.item))
                    return true;
            return false;
        } finally {
            lock.unlock();
        }
    }

    /*
     * TODO: 添加对更高效批量操作的支持。
     *
     * 我们不希望在每次迭代时都获取锁，但我们也要给其他线程一个与集合交互的机会，
     * 尤其是在 count 接近容量时。
     */

//     /**
//      * 将指定集合中的所有元素添加到此队列中。尝试将队列添加到自身会导致
//      * {@code IllegalArgumentException}。此外，如果在操作进行过程中修改了指定的集合，
//      * 则此操作的行为是未定义的。
//      *
//      * @param c 包含要添加到此队列中的元素的集合
//      * @return 如果此队列因调用而改变，则返回 {@code true}
//      * @throws ClassCastException            {@inheritDoc}
//      * @throws NullPointerException          {@inheritDoc}
//      * @throws IllegalArgumentException      {@inheritDoc}
//      * @throws IllegalStateException 如果此双端队列已满
//      * @see #add(Object)
//      */
//     public boolean addAll(Collection<? extends E> c) {
//         if (c == null)
//             throw new NullPointerException();
//         if (c == this)
//             throw new IllegalArgumentException();
//         final ReentrantLock lock = this.lock;
//         lock.lock();
//         try {
//             boolean modified = false;
//             for (E e : c)
//                 if (linkLast(e))
//                     modified = true;
//             return modified;
//         } finally {
//             lock.unlock();
//         }
//     }

    /**
     * 返回一个包含此双端队列中所有元素的数组（从第一个元素到最后一个元素）。
     *
     * <p>返回的数组是“安全的”，即此双端队列不保留对它的任何引用。（换句话说，此方法必须分配一个新数组）。
     * 因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 包含此双端队列中所有元素的数组
     */
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] a = new Object[count];
            int k = 0;
            for (Node<E> p = first; p != null; p = p.next)
                a[k++] = p.item;
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个包含此双端队列中所有元素的数组，顺序正确（从第一个元素到最后一个元素）；
     * 返回数组的运行时类型是指定数组的运行时类型。如果双端队列适合指定的数组，则返回该数组。
     * 否则，分配一个具有指定数组的运行时类型和此双端队列大小的新数组。
     *
     * <p>如果此双端队列适合指定的数组并且有剩余空间（即，数组的元素比此双端队列多），
     * 则数组中紧跟在双端队列末尾的元素被设置为 {@code null}。
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。
     * 此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下可以节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的双端队列。以下代码可以用于将双端队列转储到新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 请注意，{@code toArray(new Object[0])} 与 {@code toArray()} 的功能相同。
     *
     * @param a 如果足够大，则将双端队列中的元素存储在其中的数组；否则，为此目的分配一个具有相同运行时类型的新数组
     * @return 包含此双端队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此双端队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (a.length < count)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), count);

            int k = 0;
            for (Node<E> p = first; p != null; p = p.next)
                a[k++] = (T)p.item;
            if (a.length > k)
                a[k] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Node<E> p = first;
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
            lock.unlock();
        }
    }

    /**
     * 从双端队列中原子地移除所有元素。调用返回后，双端队列将为空。
     */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> f = first; f != null; ) {
                f.item = null;
                Node<E> n = f.next;
                f.prev = null;
                f.next = null;
                f = n;
            }
            first = last = null;
            count = 0;
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个按正确顺序遍历此双端队列中元素的迭代器。元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * @return 一个按正确顺序遍历此双端队列中元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }


                /**
     * 返回一个迭代器，用于按逆序遍历此双端队列中的元素。元素将按从最后一个（尾部）到第一个（头部）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>的。
     *
     * @return 一个按逆序遍历此双端队列中元素的迭代器
     */
    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    /**
     * 用于 LinkedBlockingDeque 的迭代器的基类
     */
    private abstract class AbstractItr implements Iterator<E> {
        /**
         * 在 next() 中要返回的下一个节点
         */
        Node<E> next;

        /**
         * nextItem 保存 item 字段，因为在 hasNext() 中声明一个元素存在后，即使该元素在调用 hasNext() 时正在被删除，也必须在锁下（在 advance() 中）返回读取的项。
         */
        E nextItem;

        /**
         * 最近一次调用 next() 返回的节点。在调用 remove() 删除此元素时需要。如果此元素被删除，则重置为 null。
         */
        private Node<E> lastRet;

        abstract Node<E> firstNode();
        abstract Node<E> nextNode(Node<E> n);

        AbstractItr() {
            // 设置初始位置
            final ReentrantLock lock = LinkedBlockingDeque.this.lock;
            lock.lock();
            try {
                next = firstNode();
                nextItem = (next == null) ? null : next.item;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 返回给定非空但可能已被删除的节点的后继节点。
         */
        private Node<E> succ(Node<E> n) {
            // 如果多个内部节点被删除，可能会出现以 null 或自链接结尾的已删除节点链。
            for (;;) {
                Node<E> s = nextNode(n);
                if (s == null)
                    return null;
                else if (s.item != null)
                    return s;
                else if (s == n)
                    return firstNode();
                else
                    n = s;
            }
        }

        /**
         * 前进 next。
         */
        void advance() {
            final ReentrantLock lock = LinkedBlockingDeque.this.lock;
            lock.lock();
            try {
                // assert next != null;
                next = succ(next);
                nextItem = (next == null) ? null : next.item;
            } finally {
                lock.unlock();
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public E next() {
            if (next == null)
                throw new NoSuchElementException();
            lastRet = next;
            E x = nextItem;
            advance();
            return x;
        }

        public void remove() {
            Node<E> n = lastRet;
            if (n == null)
                throw new IllegalStateException();
            lastRet = null;
            final ReentrantLock lock = LinkedBlockingDeque.this.lock;
            lock.lock();
            try {
                if (n.item != null)
                    unlink(n);
            } finally {
                lock.unlock();
            }
        }
    }

    /** 前向迭代器 */
    private class Itr extends AbstractItr {
        Node<E> firstNode() { return first; }
        Node<E> nextNode(Node<E> n) { return n.next; }
    }

    /** 逆向迭代器 */
    private class DescendingItr extends AbstractItr {
        Node<E> firstNode() { return last; }
        Node<E> nextNode(Node<E> n) { return n.prev; }
    }

    /** Spliterators.IteratorSpliterator 的自定义变体 */
    static final class LBDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // 最大批处理数组大小
        final LinkedBlockingDeque<E> queue;
        Node<E> current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批处理大小
        boolean exhausted;  // 无更多节点时为 true
        long est;           // 大小估计
        LBDSpliterator(LinkedBlockingDeque<E> queue) {
            this.queue = queue;
            this.est = queue.size();
        }

        public long estimateSize() { return est; }

        public Spliterator<E> trySplit() {
            Node<E> h;
            final LinkedBlockingDeque<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((h = current) != null || (h = q.first) != null) &&
                h.next != null) {
                Object[] a = new Object[n];
                final ReentrantLock lock = q.lock;
                int i = 0;
                Node<E> p = current;
                lock.lock();
                try {
                    if (p != null || (p = q.first) != null) {
                        do {
                            if ((a[i] = p.item) != null)
                                ++i;
                        } while ((p = p.next) != null && i < n);
                    }
                } finally {
                    lock.unlock();
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
            final LinkedBlockingDeque<E> q = this.queue;
            final ReentrantLock lock = q.lock;
            if (!exhausted) {
                exhausted = true;
                Node<E> p = current;
                do {
                    E e = null;
                    lock.lock();
                    try {
                        if (p == null)
                            p = q.first;
                        while (p != null) {
                            e = p.item;
                            p = p.next;
                            if (e != null)
                                break;
                        }
                    } finally {
                        lock.unlock();
                    }
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingDeque<E> q = this.queue;
            final ReentrantLock lock = q.lock;
            if (!exhausted) {
                E e = null;
                lock.lock();
                try {
                    if (current == null)
                        current = q.first;
                    while (current != null) {
                        e = current.item;
                        current = current.next;
                        if (e != null)
                            break;
                    }
                } finally {
                    lock.unlock();
                }
                if (current == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * 返回一个遍历此双端队列中元素的 {@link Spliterator}。
     *
     * <p>返回的 {@code Spliterator} 是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>的。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}、
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 实现 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此双端队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LBDSpliterator<E>(this);
    }

    /**
     * 将此双端队列保存到流中（即序列化它）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 容量（int），后跟按正确顺序排列的元素（每个都是 {@code Object}），最后是一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 写出容量和任何隐藏的内容
            s.defaultWriteObject();
            // 按正确顺序写出所有元素。
            for (Node<E> p = first; p != null; p = p.next)
                s.writeObject(p.item);
            // 使用尾部的 null 作为哨兵
            s.writeObject(null);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从流中恢复此双端队列（即反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果找不到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        count = 0;
        first = null;
        last = null;
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
