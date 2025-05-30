
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
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 基于链接节点的无界线程安全 {@linkplain Queue 队列}。
 * 该队列按 FIFO（先进先出）顺序排列元素。
 * 队列的 <em>头</em> 是在队列中停留时间最长的元素。
 * 队列的 <em>尾</em> 是在队列中停留时间最短的元素。新元素
 * 被插入到队列的尾部，而队列的检索操作则从队列的头部获取元素。
 * 当许多线程需要共享访问一个公共集合时，{@code ConcurrentLinkedQueue} 是一个合适的选择。
 * 像大多数其他并发集合实现一样，此类不允许使用 {@code null} 元素。
 *
 * <p>此实现采用了一种高效的 <em>非阻塞</em>
 * 算法，基于 Maged M. Michael 和 Michael L. Scott 在 <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html"> Simple,
 * Fast, and Practical Non-Blocking and Blocking Concurrent Queue
 * Algorithms</a> 中描述的算法。
 *
 * <p>迭代器是 <i>弱一致的</i>，返回反映在创建迭代器时或之后队列状态的元素。
 * 它们 <em>不会</em> 抛出 {@link java.util.ConcurrentModificationException}，并且可以与
 * 其他操作并发进行。自创建迭代器以来包含在队列中的元素将恰好返回一次。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法
 * <em>不是</em> 常量时间操作。由于这些队列的异步性质，确定当前元素数量需要遍历元素，
 * 因此如果在遍历期间修改此集合，可能会报告不准确的结果。
 * 此外，{@code addAll}、{@code removeAll}、{@code retainAll}、{@code containsAll}、
 * {@code equals} 和 {@code toArray} 等批量操作 <em>不是</em> 原子操作。例如，与
 * {@code addAll} 操作并发进行的迭代器可能会只看到部分新增的元素。
 *
 * <p>此类及其迭代器实现了 {@link Queue} 和 {@link Iterator} 接口中的所有 <em>可选</em>
 * 方法。
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入
 * {@code ConcurrentLinkedQueue} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程从 {@code ConcurrentLinkedQueue} 中访问或移除该元素的操作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable {
    private static final long serialVersionUID = 196745693267521676L;

    /*
     * 这是 Michael & Scott 算法的修改版本，适应了垃圾回收环境，并支持
     * 内部节点删除（以支持 remove(Object)）。有关解释，请阅读论文。
     *
     * 请注意，像本包中的大多数非阻塞算法一样，此实现依赖于垃圾回收系统中
     * 没有因节点回收而导致的 ABA 问题，因此不需要使用“计数指针”或在非 GC 环境中
     * 使用的其他技术。
     *
     * 基本不变量是：
     * - 恰好有一个（最后一个）Node 具有 null 的 next 引用，这是在入队时 CAS 的。
     *   该最后一个 Node 可以在 O(1) 时间内从 tail 达到，但 tail 仅是一个优化——
     *   从 head 也可以在 O(N) 时间内到达。
     * - 队列中包含的元素是从 head 可达的非 null 节点中的元素。
     *   将 Node 的 item 引用 CAS 为 null 可以原子地将其从队列中移除。
     *   即使在并发修改导致 head 前进的情况下，从 head 可达所有元素的条件也必须保持不变。
     *   一个已出队的 Node 可能由于创建了一个 Iterator 或者仅仅是由于 poll() 操作
     *   丢失了时间片而无限期地保持使用。
     *
     * 上述内容可能暗示所有 Node 都是从一个已出队的 Node 可达的。这会导致两个问题：
     * - 允许一个恶意的 Iterator 导致无界的内存保留
     * - 如果一个 Node 在活跃时被提升到老年代，会导致老 Node 与新 Node 之间的
     *   跨代链接，这会使分代 GC 难以处理，导致重复的全量收集。
     * 然而，只有非删除的 Node 需要从已出队的 Node 可达，且可达性不一定需要是
     * 垃圾回收器理解的那种。我们使用将刚出队的 Node 链接到自身的技巧。
     * 这样的自链接隐式地表示前进到 head。
     *
     * head 和 tail 都允许滞后。事实上，每次可以更新时都不更新它们是一个重要的优化
     * （减少 CAS 操作）。与 LinkedTransferQueue（参见该类的内部文档）一样，我们使用
     * 2 的松弛阈值；也就是说，当当前指针看起来与第一个/最后一个节点相距两个或更多步时，
     * 我们更新 head/tail。
     *
     * 由于 head 和 tail 是并发且独立地更新的，因此 tail 可能滞后于 head（为什么不可以呢？）。
     *
     * 将 Node 的 item 引用 CAS 为 null 可以原子地从队列中移除该元素。迭代器会跳过
     * item 为 null 的 Node。此类的早期实现中存在一个竞态条件，即 poll() 和 remove(Object)
 * 之间，同一个元素可能会被两个并发操作成功移除。remove(Object) 方法还会惰性地
 * 解链已删除的 Node，但这仅是一个优化。
 *
 * 在构造 Node（在入队之前）时，我们避免了对 item 的 volatile 写操作，而是使用
 * Unsafe.putObject，这样可以将入队的成本降低到“一个半”CAS 操作。
 *
 * head 和 tail 可能指向一个 item 为非 null 的 Node，也可能不指向。如果队列为空，
 * 所有 item 必须当然是 null。在创建时，head 和 tail 都指向一个 item 为 null 的
 * 哑 Node。head 和 tail 仅使用 CAS 更新，因此它们永远不会退步，尽管这仅是一个优化。
     */

    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;

        /**
         * 构造一个新的节点。使用宽松写操作，因为 item 只能在通过 casNext 发布后被看到。
         */
        Node(E item) {
            UNSAFE.putObject(this, itemOffset, item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        // Unsafe 机制

        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * 一个节点，从该节点可以 O(1) 时间内到达第一个活（非删除）节点（如果有）。
     * 不变量：
     * - 所有活节点都可以从 head 通过 succ() 到达
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * 非不变量：
     * - head.item 可能为 null，也可能不为 null。
     * - 允许 tail 落后于 head，即 tail 可能不可从 head 到达！
     */
    private transient volatile Node<E> head;

    /**
     * 一个节点，从该节点可以 O(1) 时间内到达列表上的最后一个节点（即唯一一个
     * node.next == null 的节点）。
     * 不变量：
     * - 最后一个节点总是可以从 tail 通过 succ() 到达
     * - tail != null
     * 非不变量：
     * - tail.item 可能为 null，也可能不为 null。
     * - 允许 tail 落后于 head，即 tail 可能不可从 head 到达！
     * - tail.next 可能指向 tail 本身，也可能不指向。
     */
    private transient volatile Node<E> tail;

    /**
     * 创建一个最初为空的 {@code ConcurrentLinkedQueue}。
     */
    public ConcurrentLinkedQueue() {
        head = tail = new Node<E>(null);
    }

    /**
     * 创建一个 {@code ConcurrentLinkedQueue}，最初包含给定集合中的元素，
     * 按照集合迭代器的遍历顺序添加。
     *
     * @param c 要最初包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        Node<E> h = null, t = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    // 必须重写以更新 Javadoc

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会抛出
     * {@link IllegalStateException} 或返回 {@code false}。
     *
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 尝试将 head CAS 为 p。如果成功，将旧 head 指向自身
     * 作为 succ() 的哨兵。
     */
    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p))
            h.lazySetNext(h);
    }

    /**
     * 返回 p 的后继节点，如果 p.next 已链接到自身，则返回头节点，
     * 这只会在使用过时指针且现在已脱离列表时为真。
     */
    final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会返回 {@code false}。
     *
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            if (q == null) {
                // p 是最后一个节点
                if (p.casNext(null, newNode)) {
                    // 成功的 CAS 是 e 成为此队列元素的线性化点，
                    // 也是 newNode 变为“活跃”的线性化点。
                    if (p != t) // 一次跳两步
                        casTail(t, newNode);  // 失败也没关系。
                    return true;
                }
                // 输掉了 CAS 竞争，重新读取 next
            }
            else if (p == q)
                // 我们已经脱离列表。如果 tail 未改变，它
                // 也将脱离列表，此时我们需要跳到 head，从 head
                // 可以到达所有活节点。否则，新的 tail 是更好的选择。
                p = (t != (t = tail)) ? t : head;
            else
                // 在两次跳跃后检查 tail 更新。
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }

    public E poll() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;

                if (item != null && p.casItem(item, null)) {
                    // 成功的 CAS 是 item 从此队列中移除的线性化点。
                    if (p != h) // 一次跳两步
                        updateHead(h, ((q = p.next) != null) ? q : p);
                    return item;
                }
                else if ((q = p.next) == null) {
                    updateHead(h, p);
                    return null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }


                public E peek() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;
                if (item != null || (q = p.next) == null) {
                    updateHead(h, p);
                    return item;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }

    /**
     * 返回列表中的第一个活动（非删除）节点，如果没有则返回 null。
     * 这是 poll/peek 的另一个变体；这里返回第一个节点，而不是元素。我们可以通过 first() 包装 peek()，
     * 但这会增加一次 volatile 读取 item 的开销，并且需要添加一个重试循环来处理并发 poll() 时可能丢失竞争的情况。
     */
    Node<E> first() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                boolean hasItem = (p.item != null);
                if (hasItem || (q = p.next) == null) {
                    updateHead(h, p);
                    return hasItem ? p : null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }

    /**
     * 如果此队列不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此队列不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return first() == null;
    }

    /**
     * 返回此队列中的元素数量。如果此队列包含的元素超过 {@code Integer.MAX_VALUE}，则返回
     * {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，与大多数集合不同，此方法不是常量时间操作。由于这些队列的异步性质，确定当前元素数量需要 O(n) 遍历。
     * 此外，如果在执行此方法时添加或删除了元素，返回的结果可能不准确。因此，此方法在并发应用程序中通常不太有用。
     *
     * @return 此队列中的元素数量
     */
    public int size() {
        int count = 0;
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                // Collection.size() 规范要求在此处设置最大值
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
    }

    /**
     * 如果此队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果且仅当此队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此队列中的对象
     * @return 如果此队列包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item))
                return true;
        }
        return false;
    }

    /**
     * 如果此队列包含指定的元素，则移除一个该元素的实例。更正式地说，移除一个元素 {@code e} 使得
     * {@code o.equals(e)}，如果此队列包含一个或多个这样的元素。
     * 如果此队列包含指定的元素（或等效地，如果此队列因调用而改变），则返回 {@code true}。
     *
     * @param o 如果存在，则要从队列中移除的元素
     * @return 如果此队列因调用而改变，则返回 {@code true}
     */
    public boolean remove(Object o) {
        if (o != null) {
            Node<E> next, pred = null;
            for (Node<E> p = first(); p != null; pred = p, p = next) {
                boolean removed = false;
                E item = p.item;
                if (item != null) {
                    if (!o.equals(item)) {
                        next = succ(p);
                        continue;
                    }
                    removed = p.casItem(item, null);
                }

                next = succ(p);
                if (pred != null && next != null) // 解链
                    pred.casNext(p, next);
                if (removed)
                    return true;
            }
        }
        return false;
    }

    /**
     * 将指定集合中的所有元素按指定集合的迭代器返回的顺序追加到此队列的末尾。尝试将队列添加到自身会导致
     * {@code IllegalArgumentException}。
     *
     * @param c 要插入到此队列中的元素
     * @return 如果此队列因调用而改变，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     * @throws IllegalArgumentException 如果集合是此队列
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == this)
            // 按照 AbstractQueue#addAll 中的历史规范
            throw new IllegalArgumentException();

        // 将 c 复制到一个私有的 Node 链中
        Node<E> beginningOfTheEnd = null, last = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (beginningOfTheEnd == null)
                beginningOfTheEnd = last = newNode;
            else {
                last.lazySetNext(newNode);
                last = newNode;
            }
        }
        if (beginningOfTheEnd == null)
            return false;

        // 原子地将链追加到此集合的尾部
        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            if (q == null) {
                // p 是最后一个节点
                if (p.casNext(null, beginningOfTheEnd)) {
                    // 成功的 CAS 是所有元素被添加到此队列的线性化点。
                    if (!casTail(t, last)) {
                        // 尝试更努力地更新尾部，
                        // 因为我们可能要添加许多元素。
                        t = tail;
                        if (last.next == null)
                            casTail(t, last);
                    }
                    return true;
                }
                // 输掉了 CAS 竞争，重新读取 next
            }
            else if (p == q)
                // 我们已经脱离列表。如果尾部未改变，它也将脱离列表，此时我们需要跳到头部，
                // 因为所有活动节点总是可以从头部到达。否则，新的尾部是更好的选择。
                p = (t != (t = tail)) ? t : head;
            else
                // 在两次跳跃后检查尾部更新。
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }

    /**
     * 返回一个包含此队列中所有元素的数组，顺序正确。
     *
     * <p>返回的数组是“安全的”，即此队列不维护对它的任何引用。（换句话说，此方法必须分配一个新数组）。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此队列中所有元素的数组
     */
    public Object[] toArray() {
        // 使用 ArrayList 来处理重新调整大小。
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray();
    }

    /**
     * 返回一个包含此队列中所有元素的数组，顺序正确；返回数组的运行时类型是指定数组的运行时类型。如果队列适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为此队列大小的新数组。
     *
     * <p>如果此队列适合指定的数组且有剩余空间（即，数组的元素多于此队列的元素），则数组中紧跟在队列末尾的元素被设置为
     * {@code null}。
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的队列。以下代码可以用于将队列转储到一个新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 请注意，{@code toArray(new Object[0])} 与 {@code toArray()} 功能相同。
     *
     * @param a 如果足够大，则将队列的元素存储到此数组中；否则，为此目的分配一个相同运行时类型的新数组
     * @return 一个包含此队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // 尝试使用传入的数组
        int k = 0;
        Node<E> p;
        for (p = first(); p != null && k < a.length; p = succ(p)) {
            E item = p.item;
            if (item != null)
                a[k++] = (T)item;
        }
        if (p == null) {
            if (k < a.length)
                a[k] = null;
            return a;
        }

        // 如果不适合，使用 ArrayList 版本
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> q = first(); q != null; q = succ(q)) {
            E item = q.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray(a);
    }

    /**
     * 返回一个按正确顺序遍历此队列中元素的迭代器。元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * @return 一个按正确顺序遍历此队列中元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /**
         * 下一个要返回项目的节点。
         */
        private Node<E> nextNode;

        /**
         * nextItem 保存 item 字段，因为在 hasNext() 中声明一个元素存在后，即使在调用 hasNext() 时该元素正在被移除，也必须在下一个 next() 调用中返回它。
         */
        private E nextItem;

        /**
         * 上一个返回项目的节点，以支持 remove。
         */
        private Node<E> lastRet;

        Itr() {
            advance();
        }

        /**
         * 移动到下一个有效的节点并返回要返回的项目，如果没有则返回 null。
         */
        private E advance() {
            lastRet = nextNode;
            E x = nextItem;

            Node<E> pred, p;
            if (nextNode == null) {
                p = first();
                pred = null;
            } else {
                pred = nextNode;
                p = succ(nextNode);
            }

            for (;;) {
                if (p == null) {
                    nextNode = null;
                    nextItem = null;
                    return x;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    return x;
                } else {
                    // 跳过 null
                    Node<E> next = succ(p);
                    if (pred != null && next != null)
                        pred.casNext(p, next);
                    p = next;
                }
            }
        }

        public boolean hasNext() {
            return nextNode != null;
        }

        public E next() {
            if (nextNode == null) throw new NoSuchElementException();
            return advance();
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            // 依赖于未来的遍历来重新链接。
            l.item = null;
            lastRet = null;
        }
    }

    /**
     * 将此队列保存到流中（即序列化它）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 所有元素（每个都是 {@code E}）按正确顺序排列，最后是一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        // 写出任何隐藏的内容
        s.defaultWriteObject();

        // 按正确顺序写出所有元素。
        for (Node<E> p = first(); p != null; p = succ(p)) {
            Object item = p.item;
            if (item != null)
                s.writeObject(item);
        }

        // 使用尾部的 null 作为哨兵
        s.writeObject(null);
    }

    /**
     * 从流中重新构建此队列（即反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // 读取元素直到找到尾部的 null 哨兵
        Node<E> h = null, t = null;
        Object item;
        while ((item = s.readObject()) != null) {
            @SuppressWarnings("unchecked")
            Node<E> newNode = new Node<E>((E) item);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    /** 一个定制的 Spliterators.IteratorSpliterator 变体 */
    static final class CLQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // 批处理数组的最大大小
        final ConcurrentLinkedQueue<E> queue;
        Node<E> current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批处理大小
        boolean exhausted;  // 如果没有更多节点，则为 true
        CLQSpliterator(ConcurrentLinkedQueue<E> queue) {
            this.queue = queue;
        }


        public Spliterator<E> trySplit() {
            Node<E> p;
            final ConcurrentLinkedQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    if ((a[i] = p.item) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.first();
                } while (p != null && i < n);
                if ((current = p) == null)
                    exhausted = true;
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
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                exhausted = true;
                do {
                    E e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                E e;
                do {
                    e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                } while (e == null && p != null);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public long estimateSize() { return Long.MAX_VALUE; }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * 返回一个遍历此队列中元素的 {@link Spliterator}。
     *
     * <p>返回的 {@code Spliterator} 是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}、
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 实现了 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new CLQSpliterator<E>(this);
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

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    // Unsafe 机制

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentLinkedQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
