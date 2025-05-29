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
 * 由 Doug Lea 和 Martin Buchholz 在 JCP JSR-166 专家小组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样发布到公共领域。
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
 * 该队列以 FIFO（先进先出）方式对元素进行排序。
 * 队列的 <em>头部</em> 是已经在队列中最长时间的元素。
 * 队列的 <em>尾部</em> 是已经在队列中最短时间的元素。新元素
 * 在队列的尾部插入，队列的检索操作从队列的头部获取元素。
 * 当许多线程将共享对一个公共集合的访问时，{@code ConcurrentLinkedQueue} 是一个合适的选择。
 * 与其他大多数并发集合实现一样，此类不允许使用 {@code null} 元素。
 *
 * <p>此实现采用了一种高效的 <em>非阻塞</em>
 * 算法，基于 Maged M. Michael 和 Michael L. Scott 在 <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html">简单、快速且实用的非阻塞和阻塞并发队列算法</a> 中描述的算法。
 *
 * <p>迭代器是 <i>弱一致的</i>，返回反映自迭代器创建以来队列状态的元素。
 * 它们 <em>不会</em> 抛出 {@link java.util.ConcurrentModificationException}，并且可以与其它操作并发进行。
 * 自迭代器创建以来队列中包含的元素将恰好返回一次。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法
 * <em>不是</em> 常数时间操作。由于这些队列的异步性质，确定当前元素数量需要遍历元素，因此如果在遍历过程中修改此集合，可能会报告不准确的结果。
 * 此外，批量操作 {@code addAll}、
 * {@code removeAll}、{@code retainAll}、{@code containsAll}、
 * {@code equals} 和 {@code toArray} <em>不保证</em>
 * 原子执行。例如，与 {@code addAll} 操作并发运行的迭代器可能只能看到部分添加的元素。
 *
 * <p>此类及其迭代器实现了 {@link Queue} 和 {@link Iterator} 接口的所有 <em>可选</em>
 * 方法。
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入
 * {@code ConcurrentLinkedQueue} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程从 {@code ConcurrentLinkedQueue} 访问或移除该元素后的操作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable {
    private static final long serialVersionUID = 196745693267521676L;

    /*
     * 这是 Michael & Scott 算法的修改版本，适应于垃圾回收环境，并支持
     * 内部节点删除（以支持 remove(Object)）。要了解详细信息，请阅读该论文。
     *
     * 请注意，像本包中的大多数非阻塞算法一样，此实现依赖于在垃圾回收系统中，由于节点不会被回收，因此不会出现 ABA 问题，因此不需要使用“计数指针”或在非 GC 环境中使用的相关技术。
     *
     * 基本不变量是：
     * - 恰好有一个（最后一个）节点的下一个引用为 null，这是在入队时通过 CAS 操作设置的。这个最后一个节点可以在 O(1) 时间内从尾部到达，但尾部只是一个优化——它总是可以从头部在 O(N) 时间内到达。
     * - 队列中包含的元素是非空节点中的非空项。通过 CAS 操作将节点的项引用设置为 null 可以原子地将其从队列中移除。即使在并发修改导致头部前进的情况下，从头部到达所有元素的可达性也必须保持为真。一个已出队的节点可能会由于创建了迭代器或仅仅是一个失去了时间片的 poll() 而无限期地保持使用。
     *
     * 上述内容可能暗示所有节点都是从一个已出队的节点 GC 可达的。这会导致两个问题：
     * - 允许一个恶意的迭代器导致无界内存保留
     * - 如果一个节点在活跃时被提升到老年代，这会导致老节点与新节点之间的跨代链接，这对分代垃圾回收器来说很难处理，导致重复的重大回收。
     * 然而，只有非删除节点需要从已出队的节点可达，而且可达性不一定必须是 GC 理解的那种。我们使用将刚刚出队的节点链接到自身的技巧。这种自链接隐含地意味着前进到头部。
     *
     * 头部和尾部都允许滞后。实际上，每次都可以不更新它们，这是一个重要的优化
     * （减少 CAS 操作）。与 LinkedTransferQueue（参见该类的内部文档）一样，我们使用两个的松弛阈值；
     * 即，当当前指针看起来与第一个/最后一个节点相距两个或更多步时，我们更新头部/尾部。
     *
     * 由于头部和尾部是并发且独立地更新的，因此尾部可能落后于头部（为什么不可以呢？）。
     *
     * 通过 CAS 操作将节点的项引用设置为 null 可以原子地从队列中移除元素。迭代器会跳过项为 null 的节点。此类的早期实现中存在 poll() 和 remove(Object) 之间的竞争条件，其中同一个元素可能会被两个并发操作成功移除。remove(Object) 方法还会懒惰地取消链接已删除的节点，但这只是一个优化。
     *
     * 在构造节点（在入队之前）时，我们通过使用 Unsafe.putObject 而不是普通写入来避免对项进行易失性写入。
     * 这允许入队的成本为“一个半”CAS 操作。
     *
     * 头部和尾部可能指向一个项为非空的节点，也可能不指向。如果队列为空，所有项当然都必须为 null。在创建时，头部和尾部都指向一个项为 null 的虚拟节点。头部和尾部仅使用 CAS 更新，因此它们永远不会退步，尽管这只是一个优化。
     */
    
    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;

        /**
         * 构造一个新的节点。使用宽松写入，因为 item 只能在通过 casNext 发布后被看到。
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
     * 一个可以从其到达第一个活动（非删除）节点（如果有的话）的节点，时间复杂度为 O(1)。
     * 不变量：
     * - 所有活动节点都可以通过 head 的 succ() 方法到达
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * 非不变量：
     * - head.item 可能为空，也可能不为空。
     * - 允许 tail 落后于 head，即 tail 可能无法从 head 到达！
     */
    private transient volatile Node<E> head;

    /**
     * 一个可以从其到达列表中最后一个节点（即 node.next == null 的唯一节点）的节点，时间复杂度为 O(1)。
     * 不变量：
     * - 最后一个节点总是可以通过 tail 的 succ() 方法到达
     * - tail != null
     * 非不变量：
     * - tail.item 可能为空，也可能不为空。
     * - 允许 tail 落后于 head，即 tail 可能无法从 head 到达！
     * - tail.next 可能指向 tail 本身，也可能不指向。
     */
    private transient volatile Node<E> tail;

    /**
     * 创建一个初始为空的 {@code ConcurrentLinkedQueue}。
     */
    public ConcurrentLinkedQueue() {
        head = tail = new Node<E>(null);
    }

    /**
     * 创建一个初始包含给定集合中元素的 {@code ConcurrentLinkedQueue}，
     * 元素的添加顺序为集合迭代器的遍历顺序。
     *
     * @param c 初始包含的元素集合
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
     * 在队列尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会抛出 {@link IllegalStateException} 或返回 {@code false}。
     *
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 尝试将 head CAS 为 p。如果成功，将旧 head 指向自身作为 succ() 下面的哨兵。
     */
    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p))
            h.lazySetNext(h);
    }

    /**
     * 返回 p 的后继节点，如果 p.next 已链接到自身，则返回头节点，这只有在使用已过期的指针遍历时才会为真，此时指针已不在列表中。
     */
    final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * 在队列尾部插入指定的元素。
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
                    // 也是 newNode 成为“活动”节点的线性化点。
                    if (p != t) // 一次跳两节点
                        casTail(t, newNode);  // 失败也没关系。
                    return true;
                }
                // 输掉了 CAS 竞争，重新读取 next
            }
            else if (p == q)
                // 我们已经脱离了列表。如果尾部未改变，它也会脱离列表，
                // 这时我们需要跳到头节点，所有活动节点总是可以从头节点到达。
                // 否则新的尾部是一个更好的选择。
                p = (t != (t = tail)) ? t : head;
            else
                // 在两次跳跃后检查尾部更新。
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
                    if (p != h) // 一次跳两节点
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
     * 返回列表中的第一个活跃（未删除）节点，如果没有则返回 null。
     * 这是 poll/peek 的另一个变体；这里返回的是第一个节点，而不是元素。
     * 我们可以将 peek() 包装为 first()，但这会增加一次 volatile 读取 item 的开销，
     * 并且需要添加一个重试循环来处理并发 poll() 时可能失去竞争的情况。
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
     * 返回此队列中的元素数量。如果此队列包含的元素多于 {@code Integer.MAX_VALUE}，则返回
     * {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，与大多数集合不同，此方法 <em>不是</em> 常数时间操作。由于这些队列的异步性质，
     * 确定当前元素数量需要 O(n) 的遍历。此外，如果在此方法执行期间添加或删除了元素，
     * 返回的结果可能不准确。因此，此方法在并发应用程序中通常不太有用。
     *
     * @return 此队列中的元素数量
     */
    public int size() {
        int count = 0;
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                // Collection.size() 规范要求在此处达到最大值
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
    }

    /**
     * 如果此队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果此队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
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
     * 如果此队列包含指定的元素，则从此队列中移除一个该元素的实例。
     * 更正式地说，如果此队列包含一个或多个这样的元素 {@code e} 使得 {@code o.equals(e)}，
     * 则移除一个这样的元素。如果此队列包含指定的元素（或等效地，如果此队列因调用而改变），则返回 {@code true}。
     *
     * @param o 如果存在，则要从此队列中移除的元素
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
     * 将指定集合中的所有元素按其迭代器返回的顺序添加到此队列的末尾。
     * 尝试将队列自身添加到自身中会导致 {@code IllegalArgumentException}。
     *
     * @param c 要插入到此队列中的元素
     * @return 如果此队列因调用而改变，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     * @throws IllegalArgumentException 如果集合是此队列
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == this)
            // 按照 AbstractQueue#addAll 中的历史规定
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
                        // 尝试更努力地更新尾部，因为我们可能正在添加许多元素。
                        t = tail;
                        if (last.next == null)
                            casTail(t, last);
                    }
                    return true;
                }
                // 输给了另一个线程的 CAS 竞争；重新读取 next
            }
            else if (p == q)
                // 我们已经脱离了列表。如果尾部未改变，它也将脱离列表，
                // 在这种情况下，我们需要跳到头部，因为所有活跃节点总是可以从头部到达。
                // 否则，新的尾部是一个更好的选择。
                p = (t != (t = tail)) ? t : head;
            else
                // 在两次跳跃后检查尾部更新。
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }


    /**
     * 返回一个包含此队列中所有元素的数组，按正确的顺序排列。
     *
     * <p>返回的数组是“安全的”，即此队列中没有任何对此数组的引用。（换句话说，此方法必须分配一个新数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的API之间的桥梁。
     *
     * @return 包含此队列中所有元素的数组
     */
    public Object[] toArray() {
        // 使用ArrayList来处理重新调整大小的问题。
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray();
    }

    /**
     * 返回一个包含此队列中所有元素的数组，按正确的顺序排列；返回数组的运行时类型是指定数组的运行时类型。如果队列适合指定的数组，则返回该数组。否则，将分配一个具有指定数组的运行时类型和此队列大小的新数组。
     *
     * <p>如果此队列适合指定的数组且有剩余空间（即，数组中的元素比队列中的多），则数组中紧随队列末尾的元素被设置为{@code null}。
     *
     * <p>像{@link #toArray()}方法一样，此方法充当基于数组和基于集合的API之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设{@code x}是一个已知仅包含字符串的队列。以下代码可用于将队列转储到新分配的{@code String}数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])}在功能上与{@code toArray()}相同。
     *
     * @param a 如果足够大，则将队列的元素存储在此数组中；否则，为此目的分配一个相同运行时类型的新数组
     * @return 包含此队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为null
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

        // 如果不适合，使用ArrayList版本
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> q = first(); q != null; q = succ(q)) {
            E item = q.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray(a);
    }

    /**
     * 返回一个按正确顺序遍历此队列元素的迭代器。元素将按从第一个（头）到最后一个（尾）的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>的。
     *
     * @return 按正确顺序遍历此队列元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /**
         * 下一个要返回项的节点。
         */
        private Node<E> nextNode;

        /**
         * nextItem 保存项字段，因为在hasNext()中声明一个元素存在后，即使在调用hasNext()时该元素正在被移除，也必须在下一个next()调用中返回它。
         */
        private E nextItem;

        /**
         * 最后返回项的节点，以支持remove操作。
         */
        private Node<E> lastRet;

        Itr() {
            advance();
        }

        /**
         * 移动到下一个有效的节点并返回要返回的项，如果没有则返回null。
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
                    // 跳过null
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
            // 依赖未来的遍历来重新链接。
            l.item = null;
            lastRet = null;
        }
    }

    /**
     * 将此队列保存到流中（即序列化它）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生I/O错误
     * @serialData 所有元素（每个都是{@code E}）按正确的顺序，后面跟一个null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {


        // 写出任何隐藏的内容
        s.defaultWriteObject();

        // 按正确的顺序写出所有元素。
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
     * @throws ClassNotFoundException 如果找不到序列化对象的类
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
        static final int MAX_BATCH = 1 << 25;  // 最大批量数组大小
        final ConcurrentLinkedQueue<E> queue;
        Node<E> current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批量大小
        boolean exhausted;  // 没有更多节点时为 true
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
     * <p>返回的 spliterator 是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
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
