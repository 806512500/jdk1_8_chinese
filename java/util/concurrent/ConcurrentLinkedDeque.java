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
 * 由 Doug Lea 和 Martin Buchholz 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 基于链接节点的无界并发 {@linkplain Deque 双端队列}。
 * 并发插入、移除和访问操作可以在多个线程上安全执行。
 * 当许多线程将共享对公共集合的访问时，{@code ConcurrentLinkedDeque} 是一个合适的选择。
 * 与其他大多数并发集合实现一样，此类不允许使用 {@code null} 元素。
 *
 * <p>迭代器和拆分器是
 * <a href="package-summary.html#Weakly"><i>弱一致性</i></a> 的。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法
 * <em>不是</em> 常数时间操作。由于这些双端队列的异步性质，确定当前元素数量需要遍历元素，因此如果在遍历时修改此集合，可能会报告不准确的结果。
 * 此外，{@code addAll}、
 * {@code removeAll}、{@code retainAll}、{@code containsAll}、
 * {@code equals} 和 {@code toArray} 等批量操作 <em>不</em> 保证原子执行。例如，与 {@code addAll} 操作并发运行的迭代器可能只能看到部分添加的元素。
 *
 * <p>此类及其迭代器实现了 {@link Deque} 和 {@link Iterator} 接口的所有 <em>可选</em> 方法。
 *
 * <p>内存一致性效果：与其他并发集合一样，将对象放入
 * {@code ConcurrentLinkedDeque} 的线程中的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 在另一个线程中访问或移除该元素的操作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.7
 * @author Doug Lea
 * @author Martin Buchholz
 * @param <E> 此集合中持有的元素类型
 */
public class ConcurrentLinkedDeque<E>
    extends AbstractCollection<E>
    implements Deque<E>, java.io.Serializable {

    /*
     * 这是一个支持内部移除但不支持内部插入的并发无锁双端队列实现，
     * 以支持整个 Deque 接口的要求。
     *
     * 我们扩展了在 ConcurrentLinkedQueue 和 LinkedTransferQueue 中开发的技术（参见这些类的内部文档）。
     * 理解 ConcurrentLinkedQueue 的实现是理解此类实现的先决条件。
     *
     * 数据结构是一个对称的双向“GC-健壮”链接节点列表。我们使用两种技术最小化 volatile 写操作：
     * 通过单个 CAS 操作前进多个跳步，以及混合对同一内存位置的 volatile 和非 volatile 写操作。
     *
     * 节点包含预期的 E（“item”）和前驱（“prev”）和后继（“next”）节点的链接：
     *
     * class Node<E> { volatile Node<E> prev, next; volatile E item; }
     *
     * 如果节点包含非 null 的 item (p.item != null)，则该节点被认为是“活跃”的。当 item 通过 CAS 操作设置为 null 时，该 item 从集合中逻辑上原子地删除。
     *
     * 任何时候，都有一个“第一个”节点，其 prev 引用为空，终止从活跃节点开始的任何 prev 引用链。
     * 同样，也有一个“最后一个”节点，终止从活跃节点开始的任何 next 引用链。第一个和最后一个节点可能是活跃的，也可能不是。
     * 第一个和最后一个节点始终是相互可达的。
     *
     * 通过 CAS 操作将第一个或最后一个节点中的 null prev 或 next 引用设置为包含元素的新节点，可以原子地添加新元素。此时，元素的节点原子地变为“活跃”。
     *
     * 如果节点是活跃节点，或者是第一个或最后一个节点，则该节点被认为是“活动”的。活动节点不能被解除链接。
     *
     * “自链接”是指 next 或 prev 引用指向同一个节点：
     *   p.prev == p  或  p.next == p
     * 自链接用于节点解除链接过程中。活动节点永远不会具有自链接。
     *
     * 节点 p 是活动的当且仅当：
     *
     * p.item != null ||
     * (p.prev == null && p.next != p) ||
     * (p.next == null && p.prev != p)
     *
     * 双端队列对象有两个节点引用，“head”和“tail”。
     * head 和 tail 仅是双端队列第一个和最后一个节点的近似值。第一个节点总是可以通过从 head 跟随 prev 指针找到；同样适用于 tail。但是，允许 head 和 tail 指向已删除且已解除链接的节点，因此可能无法从任何活跃节点到达。
     *
     * 节点删除有三个阶段；
     * “逻辑删除”，“解除链接”，和“GC-解除链接”。
     *
     * 1. 通过 CAS 操作将 item 设置为 null 来进行“逻辑删除”，原子地从集合中移除元素，并使包含该节点的节点有资格进行解除链接。
     *
     * 2. “解除链接”使已删除的节点无法从活动节点到达，从而最终可由 GC 回收。解除链接的节点可能无限期地从迭代器中可达。
     *
     * 物理节点解除链接仅是一种优化（尽管是关键的），因此可以在我们方便时进行。任何时候，通过 prev 和 next 链接维护的活跃节点集是相同的，即，从第一个节点通过 next 链接找到的活跃节点等于从最后一个节点通过 prev 链接找到的元素。但是，对于已经逻辑删除的节点，这不是真的 - 这样的节点可能仅在一个方向上可达。
     *
     * 3. “GC-解除链接”通过使已删除的节点无法从活动节点到达，使活动节点更容易被 GC 回收。这一步使数据结构“GC-健壮”，如 Boehm 首次详细描述的那样
     * (http://portal.acm.org/citation.cfm?doid=503272.503282)。
     *
     * GC-解除链接的节点可能无限期地从迭代器中可达，但与解除链接的节点不同，它们从 head 或 tail 中永远不可达。
     *
     * 使数据结构 GC-健壮将消除保守 GC 下无界内存保留的风险，并可能提高分代 GC 的性能。
     *
     * 当节点在任一端被出队时，例如通过 poll()，我们希望打破该节点对活动节点的任何引用。我们进一步发展了在其他并发集合类中非常有效的自链接的使用。想法是用特殊值替换 prev 和 next 指针，这些值表示在某端脱离列表。这些是近似值，但足以保持我们遍历所需的属性，例如，我们保证遍历不会两次访问同一个元素，但我们不保证在遍历结束时是否能够看到更多元素，即使在该端进行了入队操作。安全地进行 GC-解除链接特别棘手，因为任何节点都可能无限期地使用（例如由迭代器）。我们必须确保 head/tail 指向的节点永远不会被 GC-解除链接，因为 head/tail 是其他节点“回到正轨”所需的。GC-解除链接占用了大部分实现复杂性。
     *
     * 由于解除链接和 GC-解除链接都不是正确性所必需的，因此在这些操作的频率（急切程度）方面有许多实现选择。由于 volatile 读取可能比 CAS 操作便宜得多，通过一次解除链接多个相邻节点来节省 CAS 操作可能是有利的。
     * GC-解除链接可以很少执行，仍然有效，因为最重要的是偶尔打破长链的已删除节点。
     *
     * 我们使用的实际表示是 p.next == p 表示转到第一个节点（通过从 head 跟随 prev 指针到达），p.next == null && p.prev == p 表示迭代结束，p 是一个（静态最终）虚拟节点，NEXT_TERMINATOR，而不是最后一个活动节点。
     * 当遇到这样的 TERMINATOR 时结束迭代对于只读遍历已经足够，因此这样的遍历可以使用 p.next == null 作为终止条件。当我们需要找到最后一个（活动）节点，用于入队新节点时，我们需要检查是否到达了 TERMINATOR 节点；如果是，则从 tail 重新开始遍历。
     *
     * 实现完全是对称的，除了大多数遍历列表的公共方法遵循 next 指针（“正向”方向）。
     *
     * 我们认为（没有完全证明）所有单元素双端队列操作（例如，addFirst, peekLast, pollLast）都是线性化的（参见 Herlihy 和 Shavit 的书）。然而，一些操作组合已知不是线性化的。特别是，当 addFirst(A) 与 pollFirst() 移除 B 竞争时，观察者遍历元素时可能会观察到 A B C，随后观察到 A C，即使从未执行过内部移除。尽管如此，迭代器的行为是合理的，提供了“弱一致性”保证。
     *
     * 经验上，微型基准测试表明，此类相对于 ConcurrentLinkedQueue 增加了约 40% 的开销，这感觉是我们可以期望的最好结果。
     */


                    private static final long serialVersionUID = 876323262645176354L;

    /**
     * 一个节点，从该节点可以 O(1) 时间内到达列表中的第一个节点（即唯一的节点 p
     * 满足 p.prev == null && p.next != p）。
     * 不变量：
     * - 第一个节点总是可以通过 prev 链接从 head 在 O(1) 时间内到达
     * - 所有活跃节点都可以通过 succ() 从第一个节点到达
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * - head 永远不会被垃圾回收解除链接（但可能会被解除链接）
     * 非不变量：
     * - head.item 可能为 null 或不为 null
     * - head 可能无法从第一个或最后一个节点，或从 tail 到达
     */
    private transient volatile Node<E> head;

    /**
     * 一个节点，从该节点可以 O(1) 时间内到达列表中的最后一个节点（即唯一的节点 p
     * 满足 p.next == null && p.prev != p）。
     * 不变量：
     * - 最后一个节点总是可以通过 next 链接从 tail 在 O(1) 时间内到达
     * - 所有活跃节点都可以通过 pred() 从最后一个节点到达
     * - tail != null
     * - tail 永远不会被垃圾回收解除链接（但可能会被解除链接）
     * 非不变量：
     * - tail.item 可能为 null 或不为 null
     * - tail 可能无法从第一个或最后一个节点，或从 head 到达
     */
    private transient volatile Node<E> tail;

    private static final Node<Object> PREV_TERMINATOR, NEXT_TERMINATOR;

    @SuppressWarnings("unchecked")
    Node<E> prevTerminator() {
        return (Node<E>) PREV_TERMINATOR;
    }

    @SuppressWarnings("unchecked")
    Node<E> nextTerminator() {
        return (Node<E>) NEXT_TERMINATOR;
    }

    static final class Node<E> {
        volatile Node<E> prev;
        volatile E item;
        volatile Node<E> next;

        Node() {  // 默认构造函数用于 NEXT_TERMINATOR, PREV_TERMINATOR
        }

        /**
         * 构造一个新的节点。使用宽松写入，因为 item 只能在通过 casNext 或 casPrev 发布后被看到。
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

        void lazySetPrev(Node<E> val) {
            UNSAFE.putOrderedObject(this, prevOffset, val);
        }

        boolean casPrev(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, prevOffset, cmp, val);
        }

        // Unsafe 机制

        private static final sun.misc.Unsafe UNSAFE;
        private static final long prevOffset;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                prevOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("prev"));
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
     * 将 e 链接为第一个元素。
     */
    private void linkFirst(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        restartFromHead:
        for (;;)
            for (Node<E> h = head, p = h, q;;) {
                if ((q = p.prev) != null &&
                    (q = (p = q).prev) != null)
                    // 每隔一个节点检查 head 更新。
                    // 如果 p == q，我们确定会跟随 head 而不是 q。
                    p = (h != (h = head)) ? h : q;
                else if (p.next == p) // PREV_TERMINATOR
                    continue restartFromHead;
                else {
                    // p 是第一个节点
                    newNode.lazySetNext(p); // CAS 搭便车
                    if (p.casPrev(null, newNode)) {
                        // 成功的 CAS 是 e 成为此双端队列元素的线性化点，
                        // 也是 newNode 成为“活跃”的线性化点。
                        if (p != h) // 每次跳跃两个节点
                            casHead(h, newNode);  // 失败是可以接受的。
                        return;
                    }
                    // 输给了另一个线程的 CAS 竞争；重新读取 prev
                }
            }
    }

    /**
     * 将 e 链接为最后一个元素。
     */
    private void linkLast(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        restartFromTail:
        for (;;)
            for (Node<E> t = tail, p = t, q;;) {
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // 每隔一个节点检查 tail 更新。
                    // 如果 p == q，我们确定会跟随 tail 而不是 q。
                    p = (t != (t = tail)) ? t : q;
                else if (p.prev == p) // NEXT_TERMINATOR
                    continue restartFromTail;
                else {
                    // p 是最后一个节点
                    newNode.lazySetPrev(p); // CAS 搭便车
                    if (p.casNext(null, newNode)) {
                        // 成功的 CAS 是 e 成为此双端队列元素的线性化点，
                        // 也是 newNode 成为“活跃”的线性化点。
                        if (p != t) // 每次跳跃两个节点
                            casTail(t, newNode);  // 失败是可以接受的。
                        return;
                    }
                    // 输给了另一个线程的 CAS 竞争；重新读取 next
                }
            }
    }

    private static final int HOPS = 2;

    /**
     * 解除非空节点 x 的链接。
     */
    void unlink(Node<E> x) {
        // assert x != null;
        // assert x.item == null;
        // assert x != PREV_TERMINATOR;
        // assert x != NEXT_TERMINATOR;


                        final Node<E> prev = x.prev;
        final Node<E> next = x.next;
        if (prev == null) {
            unlinkFirst(x, next);
        } else if (next == null) {
            unlinkLast(x, prev);
        } else {
            // 断开内部节点。
            //
            // 这是常见的情况，因为同一端的一系列 poll 操作通常是“内部”移除，除了可能第一次之外，因为端节点不能被断开。
            //
            // 在任何时候，所有活动节点都可以通过跟随一系列的 next 或 prev 指针相互到达。
            //
            // 我们的策略是找到 x 的唯一活动前驱和后继。尝试修复它们的链接，使它们指向彼此，从而使 x 无法从活动节点到达。
            // 如果成功，并且 x 没有活动的前驱/后继，我们还会尝试 gc 断开，使活动节点无法从 x 到达，通过重新检查前驱和后继的状态是否未改变，
            // 并确保 x 无法从尾部/头部到达，然后将 x 的 prev/next 链接设置为它们的逻辑近似替代，即 self/TERMINATOR。
            Node<E> activePred, activeSucc;
            boolean isFirst, isLast;
            int hops = 1;

            // 查找活动前驱
            for (Node<E> p = prev; ; ++hops) {
                if (p.item != null) {
                    activePred = p;
                    isFirst = false;
                    break;
                }
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next == p)
                        return;
                    activePred = p;
                    isFirst = true;
                    break;
                }
                else if (p == q)
                    return;
                else
                    p = q;
            }

            // 查找活动后继
            for (Node<E> p = next; ; ++hops) {
                if (p.item != null) {
                    activeSucc = p;
                    isLast = false;
                    break;
                }
                Node<E> q = p.next;
                if (q == null) {
                    if (p.prev == p)
                        return;
                    activeSucc = p;
                    isLast = true;
                    break;
                }
                else if (p == q)
                    return;
                else
                    p = q;
            }

            // TODO: 更好的 HOP 启发式
            if (hops < HOPS
                // 始终挤压出内部已删除的节点
                && (isFirst | isLast))
                return;

            // 挤压出活动前驱和活动后继之间的已删除节点，包括 x。
            skipDeletedSuccessors(activePred);
            skipDeletedPredecessors(activeSucc);

            // 尝试 gc 断开，如果可能
            if ((isFirst | isLast) &&

                // 重新检查前驱和后继的预期状态
                (activePred.next == activeSucc) &&
                (activeSucc.prev == activePred) &&
                (isFirst ? activePred.prev == null : activePred.item != null) &&
                (isLast  ? activeSucc.next == null : activeSucc.item != null)) {

                updateHead(); // 确保 x 无法从头部到达
                updateTail(); // 确保 x 无法从尾部到达

                // 最后，实际进行 gc 断开
                x.lazySetPrev(isFirst ? prevTerminator() : x);
                x.lazySetNext(isLast  ? nextTerminator() : x);
            }
        }
    }

    /**
     * 断开非空的第一个节点。
     */
    private void unlinkFirst(Node<E> first, Node<E> next) {
        // assert first != null;
        // assert next != null;
        // assert first.item == null;
        for (Node<E> o = null, p = next, q;;) {
            if (p.item != null || (q = p.next) == null) {
                if (o != null && p.prev != p && first.casNext(next, p)) {
                    skipDeletedPredecessors(p);
                    if (first.prev == null &&
                        (p.next == null || p.item != null) &&
                        p.prev == first) {

                        updateHead(); // 确保 o 无法从头部到达
                        updateTail(); // 确保 o 无法从尾部到达

                        // 最后，实际进行 gc 断开
                        o.lazySetNext(o);
                        o.lazySetPrev(prevTerminator());
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    /**
     * 断开非空的最后一个节点。
     */
    private void unlinkLast(Node<E> last, Node<E> prev) {
        // assert last != null;
        // assert prev != null;
        // assert last.item == null;
        for (Node<E> o = null, p = prev, q;;) {
            if (p.item != null || (q = p.prev) == null) {
                if (o != null && p.next != p && last.casPrev(prev, p)) {
                    skipDeletedSuccessors(p);
                    if (last.next == null &&
                        (p.prev == null || p.item != null) &&
                        p.next == last) {

                        updateHead(); // 确保 o 无法从头部到达
                        updateTail(); // 确保 o 无法从尾部到达

                        // 最后，实际进行 gc 断开
                        o.lazySetPrev(o);
                        o.lazySetNext(nextTerminator());
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    /**
     * 保证在调用此方法之前已断开的任何节点在返回后都无法从头部到达。
     * 不保证消除松弛，只保证头部将指向一个在调用此方法时处于活动状态的节点。
     */
    private final void updateHead() {
        // 要么头部已经指向一个活动节点，要么我们不断尝试将其 cas 到第一个节点，直到它指向活动节点。
        Node<E> h, p, q;
        restartFromHead:
        while ((h = head).item == null && (p = h.prev) != null) {
            for (;;) {
                if ((q = p.prev) == null ||
                    (q = (p = q).prev) == null) {
                    // p 可能是 PREV_TERMINATOR，
                    // 但如果是这样，CAS 保证会失败。
                    if (casHead(h, p))
                        return;
                    else
                        continue restartFromHead;
                }
                else if (h != head)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }


                    /**
     * 保证在调用此方法之前已解除链接的任何节点，在此方法返回后将无法从尾部到达。
     * 不保证消除松弛，只保证尾部将指向一个在本方法运行期间处于活动状态的节点。
     */
    private final void updateTail() {
        // 尾部已经指向一个活动节点，或者我们不断尝试将其cas到最后一个节点，直到它指向活动节点。
        Node<E> t, p, q;
        restartFromTail:
        while ((t = tail).item == null && (p = t.next) != null) {
            for (;;) {
                if ((q = p.next) == null ||
                    (q = (p = q).next) == null) {
                    // p 可能是 NEXT_TERMINATOR，
                    // 但如果是这样，CAS 保证会失败。
                    if (casTail(t, p))
                        return;
                    else
                        continue restartFromTail;
                }
                else if (t != tail)
                    continue restartFromTail;
                else
                    p = q;
            }
        }
    }

    private void skipDeletedPredecessors(Node<E> x) {
        whileActive:
        do {
            Node<E> prev = x.prev;
            // assert prev != null;
            // assert x != NEXT_TERMINATOR;
            // assert x != PREV_TERMINATOR;
            Node<E> p = prev;
            findActive:
            for (;;) {
                if (p.item != null)
                    break findActive;
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next == p)
                        continue whileActive;
                    break findActive;
                }
                else if (p == q)
                    continue whileActive;
                else
                    p = q;
            }

            // 找到活动的 CAS 目标
            if (prev == p || x.casPrev(prev, p))
                return;

        } while (x.item != null || x.next == null);
    }

    private void skipDeletedSuccessors(Node<E> x) {
        whileActive:
        do {
            Node<E> next = x.next;
            // assert next != null;
            // assert x != NEXT_TERMINATOR;
            // assert x != PREV_TERMINATOR;
            Node<E> p = next;
            findActive:
            for (;;) {
                if (p.item != null)
                    break findActive;
                Node<E> q = p.next;
                if (q == null) {
                    if (p.prev == p)
                        continue whileActive;
                    break findActive;
                }
                else if (p == q)
                    continue whileActive;
                else
                    p = q;
            }

            // 找到活动的 CAS 目标
            if (next == p || x.casNext(next, p))
                return;

        } while (x.item != null || x.prev == null);
    }

    /**
     * 返回 p 的后继节点，或者如果 p.next 已链接到自身，则返回第一个节点，这只有在使用已过时的指针遍历时才会为真。
     */
    final Node<E> succ(Node<E> p) {
        // TODO: 我们是否应该在这里跳过已删除的节点？
        Node<E> q = p.next;
        return (p == q) ? first() : q;
    }

    /**
     * 返回 p 的前驱节点，或者如果 p.prev 已链接到自身，则返回最后一个节点，这只有在使用已过时的指针遍历时才会为真。
     */
    final Node<E> pred(Node<E> p) {
        Node<E> q = p.prev;
        return (p == q) ? last() : q;
    }

    /**
     * 返回第一个节点，即唯一满足以下条件的节点 p：
     *     p.prev == null && p.next != p
     * 返回的节点可能已被逻辑删除。
     * 保证将头指针设置为返回的节点。
     */
    Node<E> first() {
        restartFromHead:
        for (;;)
            for (Node<E> h = head, p = h, q;;) {
                if ((q = p.prev) != null &&
                    (q = (p = q).prev) != null)
                    // 每隔一个跳过检查头指针更新。
                    // 如果 p == q，我们确保跟随头指针。
                    p = (h != (h = head)) ? h : q;
                else if (p == h
                         // p 可能是 PREV_TERMINATOR，
                         // 但如果是这样，CAS 保证会失败。
                         || casHead(h, p))
                    return p;
                else
                    continue restartFromHead;
            }
    }

    /**
     * 返回最后一个节点，即唯一满足以下条件的节点 p：
     *     p.next == null && p.prev != p
     * 返回的节点可能已被逻辑删除。
     * 保证将尾指针设置为返回的节点。
     */
    Node<E> last() {
        restartFromTail:
        for (;;)
            for (Node<E> t = tail, p = t, q;;) {
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // 每隔一个跳过检查尾指针更新。
                    // 如果 p == q，我们确保跟随尾指针。
                    p = (t != (t = tail)) ? t : q;
                else if (p == t
                         // p 可能是 NEXT_TERMINATOR，
                         // 但如果是这样，CAS 保证会失败。
                         || casTail(t, p))
                    return p;
                else
                    continue restartFromTail;
            }
    }

    // 辅助工具

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
     * 如果元素为 null，则抛出 NoSuchElementException，否则返回该元素。
     *
     * @param v 元素
     * @return 元素
     */
    private E screenNullResult(E v) {
        if (v == null)
            throw new NoSuchElementException();
        return v;
    }

    /**
     * 创建一个数组列表并用此列表的元素填充它。
     * 用于 toArray。
     *
     * @return 数组列表
     */
    private ArrayList<E> toArrayList() {
        ArrayList<E> list = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                list.add(item);
        }
        return list;
    }


                    /**
     * 构造一个空的双端队列。
     */
    public ConcurrentLinkedDeque() {
        head = tail = new Node<E>(null);
    }

    /**
     * 构造一个最初包含给定集合元素的双端队列，这些元素按照集合迭代器的遍历顺序添加。
     *
     * @param c 最初包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        // 将 c 复制到一个私有的 Node 链中
        Node<E> h = null, t = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        initHeadTail(h, t);
    }

    /**
     * 初始化头和尾，确保不变量成立。
     */
    private void initHeadTail(Node<E> h, Node<E> t) {
        if (h == t) {
            if (h == null)
                h = t = new Node<E>(null);
            else {
                // 避免单个 Node 项非空的边缘情况。
                Node<E> newNode = new Node<E>(null);
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        head = h;
        tail = t;
    }

    /**
     * 在此双端队列的前端插入指定的元素。
     * 由于双端队列无界，此方法永远不会抛出 {@link IllegalStateException}。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * 在此双端队列的末尾插入指定的元素。
     * 由于双端队列无界，此方法永远不会抛出 {@link IllegalStateException}。
     *
     * <p>此方法等同于 {@link #add}。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * 在此双端队列的前端插入指定的元素。
     * 由于双端队列无界，此方法永远不会返回 {@code false}。
     *
     * @return {@code true}（如 {@link Deque#offerFirst} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offerFirst(E e) {
        linkFirst(e);
        return true;
    }

    /**
     * 在此双端队列的末尾插入指定的元素。
     * 由于双端队列无界，此方法永远不会返回 {@code false}。
     *
     * <p>此方法等同于 {@link #add}。
     *
     * @return {@code true}（如 {@link Deque#offerLast} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offerLast(E e) {
        linkLast(e);
        return true;
    }

    public E peekFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    public E peekLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getFirst() {
        return screenNullResult(peekFirst());
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getLast() {
        return screenNullResult(peekLast());
    }

    public E pollFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E pollLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeFirst() {
        return screenNullResult(pollFirst());
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeLast() {
        return screenNullResult(pollLast());
    }

    // *** 队列和栈方法 ***

    /**
     * 在此双端队列的尾部插入指定的元素。
     * 由于双端队列无界，此方法永远不会返回 {@code false}。
     *
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * 在此双端队列的尾部插入指定的元素。
     * 由于双端队列无界，此方法永远不会抛出 {@link IllegalStateException} 或返回 {@code false}。
     *
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll()           { return pollFirst(); }
    public E peek()           { return peekFirst(); }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E remove()         { return removeFirst(); }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E pop()            { return removeFirst(); }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E element()        { return getFirst(); }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public void push(E e)     { addFirst(e); }

    /**
     * 移除第一个满足 {@code o.equals(e)} 的元素，如果此双端队列中存在这样的元素。
     * 如果双端队列不包含该元素，则保持不变。
     *
     * @param o 要从双端队列中移除的元素，如果存在
     * @return {@code true} 如果双端队列包含指定的元素
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean removeFirstOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }


                    /**
     * 移除此双端队列中存在的最后一个元素 {@code e}，使得 {@code o.equals(e)}。
     * 如果双端队列不包含该元素，则保持不变。
     *
     * @param o 要从此双端队列中移除的元素（如果存在）
     * @return 如果双端队列包含指定的元素，则返回 {@code true}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean removeLastOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    /**
     * 如果此双端队列至少包含一个元素 {@code e}，使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要测试其在此双端队列中是否存在性的元素
     * @return 如果此双端队列包含指定的元素，则返回 {@code true}
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
     * 如果此集合不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此集合不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return peekFirst() == null;
    }

    /**
     * 返回此双端队列中的元素数量。如果此双端队列包含的元素超过 {@code Integer.MAX_VALUE}，则返回 {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，与大多数集合不同，此方法<em>不是</em>常量时间操作。由于这些双端队列的异步性质，确定当前元素数量需要遍历所有元素以计数。
     * 此外，在执行此方法期间，大小可能会发生变化，导致返回的结果不准确。因此，此方法在并发应用程序中通常不太有用。
     *
     * @return 此双端队列中的元素数量
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
     * 移除此双端队列中存在的第一个元素 {@code e}，使得 {@code o.equals(e)}。
     * 如果双端队列不包含该元素，则保持不变。
     *
     * @param o 要从此双端队列中移除的元素（如果存在）
     * @return 如果双端队列包含指定的元素，则返回 {@code true}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * 将指定集合中的所有元素按指定集合的迭代器返回的顺序添加到此双端队列的末尾。尝试将双端队列添加到自身将导致 {@code IllegalArgumentException}。
     *
     * @param c 要插入到此双端队列中的元素
     * @return 如果此双端队列因调用而改变，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     * @throws IllegalArgumentException 如果集合是此双端队列
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
                newNode.lazySetPrev(last);
                last = newNode;
            }
        }
        if (beginningOfTheEnd == null)
            return false;

        // 原子地将链附加到此集合的尾部
        restartFromTail:
        for (;;)
            for (Node<E> t = tail, p = t, q;;) {
                if ((q = p.next) != null &&
                    (q = (p = q).next) != null)
                    // 每跳两次检查尾部更新。
                    // 如果 p == q，我们确定会跟随尾部。
                    p = (t != (t = tail)) ? t : q;
                else if (p.prev == p) // NEXT_TERMINATOR
                    continue restartFromTail;
                else {
                    // p 是最后一个节点
                    beginningOfTheEnd.lazySetPrev(p); // CAS piggyback
                    if (p.casNext(null, beginningOfTheEnd)) {
                        // 成功的 CAS 是所有元素被添加到此双端队列的线性化点。
                        if (!casTail(t, last)) {
                            // 尝试更努力地更新尾部，
                            // 因为我们可能正在添加许多元素。
                            t = tail;
                            if (last.next == null)
                                casTail(t, last);
                        }
                        return true;
                    }
                    // 输给了另一个线程的 CAS 竞争；重新读取下一个
                }
            }
    }

    /**
     * 从此双端队列中移除所有元素。
     */
    public void clear() {
        while (pollFirst() != null)
            ;
    }

    /**
     * 返回一个数组，其中包含此双端队列中的所有元素，按正确的顺序（从第一个元素到最后一个元素）。
     *
     * <p>返回的数组将是“安全的”，即此双端队列不会维护对其的任何引用。（换句话说，此方法必须分配一个新数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此双端队列中所有元素的数组
     */
    public Object[] toArray() {
        return toArrayList().toArray();
    }


                    /**
     * 返回一个包含此双端队列中所有元素的数组，按正确的顺序（从第一个元素到最后一个元素）；返回数组的运行时类型是指定数组的运行时类型。如果
     * 双端队列适合指定的数组，则返回该数组。否则，将分配一个运行时类型为指定数组类型且大小为此双端队列大小的新数组。
     *
     * <p>如果此双端队列适合指定的数组且有剩余空间
     * （即，数组的元素比双端队列多），则紧跟在双端队列末尾的数组元素被设置为
     * {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，
     * 可以用于节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的双端队列。以下代码可用于将双端队列转储到新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 在功能上与
     * {@code toArray()} 相同。
     *
     * @param a 存储双端队列元素的数组，如果足够大则使用；否则，为此目的分配一个相同运行时类型的数组
     * @return 包含此双端队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此双端队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        return toArrayList().toArray(a);
    }

    /**
     * 返回一个按正确顺序遍历此双端队列中元素的迭代器。元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 按正确顺序遍历此双端队列中元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 返回一个按逆序遍历此双端队列中元素的迭代器。元素将按从最后一个（尾部）到第一个（头部）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 按逆序遍历此双端队列中元素的迭代器
     */
    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    private abstract class AbstractItr implements Iterator<E> {
        /**
         * 下一个要返回元素的节点。
         */
        private Node<E> nextNode;

        /**
         * nextItem 保存 item 字段，因为在 hasNext() 中声明一个元素存在后，即使在调用 hasNext() 时该元素正在被删除，也必须在接下来的 next() 调用中返回它。
         */
        private E nextItem;

        /**
         * 最近一次调用 next() 返回的节点。remove 方法需要。如果此元素被 remove 调用删除，则重置为 null。
         */
        private Node<E> lastRet;

        abstract Node<E> startNode();
        abstract Node<E> nextNode(Node<E> p);

        AbstractItr() {
            advance();
        }

        /**
         * 将 nextNode 和 nextItem 设置为下一个有效的节点，如果没有则设置为 null。
         */
        private void advance() {
            lastRet = nextNode;

            Node<E> p = (nextNode == null) ? startNode() : nextNode(nextNode);
            for (;; p = nextNode(p)) {
                if (p == null) {
                    // p 可能是活动的末端或 TERMINATOR 节点；两者都可以
                    nextNode = null;
                    nextItem = null;
                    break;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    break;
                }
            }
        }

        public boolean hasNext() {
            return nextItem != null;
        }

        public E next() {
            E item = nextItem;
            if (item == null) throw new NoSuchElementException();
            advance();
            return item;
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            l.item = null;
            unlink(l);
            lastRet = null;
        }
    }

    /** 前向迭代器 */
    private class Itr extends AbstractItr {
        Node<E> startNode() { return first(); }
        Node<E> nextNode(Node<E> p) { return succ(p); }
    }

    /** 逆向迭代器 */
    private class DescendingItr extends AbstractItr {
        Node<E> startNode() { return last(); }
        Node<E> nextNode(Node<E> p) { return pred(p); }
    }

    /** Spliterators.IteratorSpliterator 的自定义变体 */
    static final class CLDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // 最大批量数组大小；
        final ConcurrentLinkedDeque<E> queue;
        Node<E> current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批量大小
        boolean exhausted;  // 没有更多节点时为 true
        CLDSpliterator(ConcurrentLinkedDeque<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node<E> p;
            final ConcurrentLinkedDeque<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                if (p.item == null && p == (p = p.next))
                    current = p = q.first();
                if (p != null && p.next != null) {
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
            }
            return null;
        }


                        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedDeque<E> q = this.queue;
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
            final ConcurrentLinkedDeque<E> q = this.queue;
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
     * 返回一个遍历此双端队列元素的 {@link Spliterator}。
     *
     * <p>返回的遍历器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * 该 {@code Spliterator} 实现了 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此双端队列元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new CLDSpliterator<E>(this);
    }

    /**
     * 将此双端队列保存到流中（即序列化）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 按正确顺序的所有元素（每个都是 {@code E}），后跟一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        // 写出任何隐藏的数据
        s.defaultWriteObject();

        // 按正确顺序写出所有元素。
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                s.writeObject(item);
        }

        // 使用尾部的 null 作为哨兵
        s.writeObject(null);
    }

    /**
     * 从流中重新构造此双端队列（即反序列化）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // 读取元素，直到找到尾部的 null 哨兵
        Node<E> h = null, t = null;
        Object item;
        while ((item = s.readObject()) != null) {
            @SuppressWarnings("unchecked")
            Node<E> newNode = new Node<E>((E) item);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        initHeadTail(h, t);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    // Unsafe 机制

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        PREV_TERMINATOR = new Node<Object>();
        PREV_TERMINATOR.next = PREV_TERMINATOR;
        NEXT_TERMINATOR = new Node<Object>();
        NEXT_TERMINATOR.prev = NEXT_TERMINATOR;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentLinkedDeque.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
