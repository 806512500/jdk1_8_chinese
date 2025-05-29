
/*
 * 版权所有 (c) 2003, 2017, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;

import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * 基于优先级堆的无界优先级 {@linkplain Queue 队列}。
 * 优先级队列中的元素根据它们的 {@linkplain Comparable 自然顺序} 或在队列构造时提供的 {@link Comparator} 进行排序，
 * 具体取决于使用了哪个构造函数。优先级队列不允许 {@code null} 元素。
 * 依赖自然顺序的优先级队列也不允许插入不可比较的对象（这样做可能导致 {@code ClassCastException}）。
 *
 * <p>此队列的 <em>头部</em> 是相对于指定顺序的 <em>最小</em> 元素。
 * 如果有多个元素的值相同，头部将是其中的一个元素——平局将任意打破。队列检索操作 {@code poll}、
 * {@code remove}、{@code peek} 和 {@code element} 访问队列头部的元素。
 *
 * <p>优先级队列是无界的，但有一个内部 <i>容量</i>，用于存储队列元素的数组的大小。
 * 它始终至少与队列大小一样大。随着元素被添加到优先级队列中，其容量会自动增长。增长策略的细节未指定。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口的所有 <em>可选</em> 方法。
 * 方法 {@link #iterator()} 提供的迭代器 <em>不保证</em> 按任何特定顺序遍历优先级队列的元素。
 * 如果需要有序遍历，请考虑使用 {@code Arrays.sort(pq.toArray())}。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果任何线程修改队列，多个线程不应同时访问 {@code PriorityQueue} 实例。
 * 相反，使用线程安全的 {@link java.util.concurrent.PriorityBlockingQueue} 类。
 *
 * <p>实现说明：此实现提供了 O(log(n)) 时间的入队和出队方法
 * ({@code offer}、{@code poll}、{@code remove()} 和 {@code add}）；
 * {@code remove(Object)} 和 {@code contains(Object)} 方法的线性时间；
 * 以及检索方法 ({@code peek}、{@code element} 和 {@code size}) 的常数时间。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Josh Bloch, Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {

    private static final long serialVersionUID = -7720805057305804111L;

    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * 优先级队列表示为平衡二叉堆：队列[n] 的两个子节点分别是队列[2*n+1] 和队列[2*(n+1)]。
     * 优先级队列由比较器排序，或者如果比较器为 null，则按元素的自然顺序排序：对于堆中的每个节点 n 和 n 的每个后代 d，n <= d。
     * 假设队列非空，值最小的元素位于队列[0] 中。
     */
    transient Object[] queue; // 非私有以简化嵌套类访问

    /**
     * 优先级队列中的元素数量。
     */
    private int size = 0;

    /**
     * 比较器，如果优先级队列使用元素的自然顺序，则为 null。
     */
    private final Comparator<? super E> comparator;

    /**
     * 优先级队列被 <i>结构修改</i> 的次数。有关详细信息，请参阅 AbstractList。
     */
    transient int modCount = 0; // 非私有以简化嵌套类访问

    /**
     * 创建一个具有默认初始容量（11）的 {@code PriorityQueue}，其元素根据它们的
     * {@linkplain Comparable 自然顺序} 排序。
     */
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * 创建一个具有指定初始容量的 {@code PriorityQueue}，其元素根据它们的
     * {@linkplain Comparable 自然顺序} 排序。
     *
     * @param initialCapacity 此优先级队列的初始容量
     * @throws IllegalArgumentException 如果 {@code initialCapacity} 小于 1
     */
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * 创建一个具有默认初始容量且元素根据指定比较器排序的 {@code PriorityQueue}。
     *
     * @param  comparator 将用于对此优先级队列进行排序的比较器。如果为 {@code null}，则使用元素的 {@linkplain Comparable 自然顺序}。
     * @since 1.8
     */
    public PriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    /**
     * 创建一个具有指定初始容量且元素根据指定比较器排序的 {@code PriorityQueue}。
     *
     * @param  initialCapacity 此优先级队列的初始容量
     * @param  comparator 将用于对此优先级队列进行排序的比较器。如果为 {@code null}，则使用元素的 {@linkplain Comparable 自然顺序}。
     * @throws IllegalArgumentException 如果 {@code initialCapacity} 小于 1
     */
    public PriorityQueue(int initialCapacity,
                         Comparator<? super E> comparator) {
        // 注意：至少需要一个的限制实际上并不需要，
        // 但为了 1.5 兼容性而继续保留
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

                /**
     * 创建一个包含指定集合中元素的 {@code PriorityQueue}。如果指定的集合是 {@link SortedSet} 或另一个 {@code PriorityQueue}，
     * 则此优先队列将根据相同的顺序进行排序。否则，此优先队列将根据其元素的 {@linkplain Comparable 自然顺序} 进行排序。
     *
     * @param  c 要放入此优先队列的集合
     * @throws ClassCastException 如果根据优先队列的顺序，指定集合的元素无法相互比较
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            initElementsFromCollection(ss);
        }
        else if (c instanceof PriorityQueue<?>) {
            PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            initFromPriorityQueue(pq);
        }
        else {
            this.comparator = null;
            initFromCollection(c);
        }
    }

    /**
     * 创建一个包含指定优先队列中元素的 {@code PriorityQueue}。此优先队列将根据给定优先队列的相同顺序进行排序。
     *
     * @param  c 要放入此优先队列的优先队列
     * @throws ClassCastException 如果根据 {@code c} 的顺序，{@code c} 的元素无法相互比较
     * @throws NullPointerException 如果指定的优先队列或其任何元素为 null
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(PriorityQueue<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        initFromPriorityQueue(c);
    }

    /**
     * 创建一个包含指定排序集中元素的 {@code PriorityQueue}。此优先队列将根据给定排序集的相同顺序进行排序。
     *
     * @param  c 要放入此优先队列的排序集
     * @throws ClassCastException 如果根据排序集的顺序，指定排序集的元素无法相互比较
     * @throws NullPointerException 如果指定的排序集或其任何元素为 null
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(SortedSet<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        initElementsFromCollection(c);
    }

    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        if (c.getClass() == PriorityQueue.class) {
            this.queue = c.toArray();
            this.size = c.size();
        } else {
            initFromCollection(c);
        }
    }

    private void initElementsFromCollection(Collection<? extends E> c) {
        Object[] a = c.toArray();
        if (c.getClass() != ArrayList.class)
            a = Arrays.copyOf(a, a.length, Object[].class);
        int len = a.length;
        if (len == 1 || this.comparator != null)
            for (int i = 0; i < len; i++)
                if (a[i] == null)
                    throw new NullPointerException();
        this.queue = a;
        this.size = a.length;
    }

    /**
     * 使用来自给定集合的元素初始化队列数组。
     *
     * @param c 集合
     */
    private void initFromCollection(Collection<? extends E> c) {
        initElementsFromCollection(c);
        heapify();
    }

    /**
     * 可分配数组的最大大小。
     * 某些虚拟机在数组中预留了一些头部字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过虚拟机限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加数组的容量。
     *
     * @param minCapacity 所需的最小容量
     */
    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        // 如果容量较小则翻倍；否则增加 50%
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // 防止溢出的代码
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        queue = Arrays.copyOf(queue, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 将指定元素插入此优先队列。
     *
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws ClassCastException 如果根据优先队列的顺序，指定元素无法与当前在优先队列中的元素比较
     * @throws NullPointerException 如果指定元素为 null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 将指定元素插入此优先队列。
     *
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws ClassCastException 如果根据优先队列的顺序，指定元素无法与当前在优先队列中的元素比较
     * @throws NullPointerException 如果指定元素为 null
     */
    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        modCount++;
        int i = size;
        if (i >= queue.length)
            grow(i + 1);
        size = i + 1;
        if (i == 0)
            queue[0] = e;
        else
            siftUp(i, e);
        return true;
    }


@SuppressWarnings("unchecked")
public E peek() {
    return (size == 0) ? null : (E) queue[0];
}

private int indexOf(Object o) {
    if (o != null) {
        for (int i = 0; i < size; i++)
            if (o.equals(queue[i]))
                return i;
    }
    return -1;
}

/**
 * 从队列中移除指定元素的一个实例（如果存在）。更正式地说，移除一个元素 {@code e} 使得 {@code o.equals(e)}，
 * 如果队列中存在一个或多个这样的元素。如果且仅当队列包含指定元素（或等效地，如果队列因调用而改变）时返回 {@code true}。
 *
 * @param o 要从队列中移除的元素（如果存在）
 * @return 如果队列因调用而改变则返回 {@code true}
 */
public boolean remove(Object o) {
    int i = indexOf(o);
    if (i == -1)
        return false;
    else {
        removeAt(i);
        return true;
    }
}

/**
 * 使用引用相等性而非 equals 方法的 remove 版本。由 iterator.remove 需要。
 *
 * @param o 要从队列中移除的元素（如果存在）
 * @return 如果移除成功则返回 {@code true}
 */
boolean removeEq(Object o) {
    for (int i = 0; i < size; i++) {
        if (o == queue[i]) {
            removeAt(i);
            return true;
        }
    }
    return false;
}

/**
 * 检查队列是否包含指定元素。更正式地说，如果且仅当队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)} 时返回 {@code true}。
 *
 * @param o 要检查是否包含在队列中的对象
 * @return 如果队列包含指定元素则返回 {@code true}
 */
public boolean contains(Object o) {
    return indexOf(o) != -1;
}

/**
 * 返回一个包含队列中所有元素的数组。元素的顺序没有特定规则。
 *
 * <p>返回的数组是“安全的”，即队列不保留对它的任何引用。（换句话说，此方法必须分配一个新数组）。调用者因此可以自由地修改返回的数组。
 *
 * <p>此方法作为基于数组和基于集合的 API 之间的桥梁。
 *
 * @return 包含队列中所有元素的数组
 */
public Object[] toArray() {
    return Arrays.copyOf(queue, size);
}

/**
 * 返回一个包含队列中所有元素的数组，返回数组的运行时类型是指定数组的运行时类型。元素的顺序没有特定规则。
 * 如果队列适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为队列大小的新数组。
 *
 * <p>如果队列适合指定数组且有剩余空间（即数组的元素多于队列），则数组中紧接队列末尾的元素被设置为 {@code null}。
 *
 * <p>像 {@link #toArray()} 方法一样，此方法作为基于数组和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，
 * 并且在某些情况下可以节省分配成本。
 *
 * <p>假设 {@code x} 是一个已知仅包含字符串的队列。以下代码可以用于将队列转储到新分配的 {@code String} 数组中：
 *
 *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
 *
 * 注意，{@code toArray(new Object[0])} 与 {@code toArray()} 的功能相同。
 *
 * @param a 队列的元素将存储到的数组，如果它足够大；否则，为此目的分配一个相同运行时类型的新数组。
 * @return 包含队列中所有元素的数组
 * @throws ArrayStoreException 如果指定数组的运行时类型不是队列中每个元素的运行时类型的超类型
 * @throws NullPointerException 如果指定数组为 null
 */
@SuppressWarnings("unchecked")
public <T> T[] toArray(T[] a) {
    final int size = this.size;
    if (a.length < size)
        // 分配一个运行时类型为 a 的新数组，但内容为我的：
        return (T[]) Arrays.copyOf(queue, size, a.getClass());
    System.arraycopy(queue, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}

/**
 * 返回一个迭代器，用于遍历队列中的元素。迭代器不以任何特定顺序返回元素。
 *
 * @return 一个迭代器，用于遍历队列中的元素
 */
public Iterator<E> iterator() {
    return new Itr();
}

private final class Itr implements Iterator<E> {
    /**
     * 下一次调用 next 时要返回的元素（在队列数组中的索引）。
     */
    private int cursor = 0;

    /**
     * 最近一次调用 next 返回的元素的索引，除非该元素来自 forgetMeNot 列表。
     * 如果元素被 remove 调用删除，则设置为 -1。
     */
    private int lastRet = -1;

    /**
     * 一个队列，包含在迭代过程中由于“不幸”的元素移除而从未访问部分移动到已访问部分的元素。
     * （“不幸”的元素移除是指那些需要 siftup 而不是 siftdown 的移除。）我们必须访问此列表中的所有元素以完成迭代。
     * 我们在完成“正常”迭代后完成此操作。
     *
     * 我们期望大多数迭代，即使涉及移除，也不需要在此字段中存储元素。
     */
    private ArrayDeque<E> forgetMeNot = null;


                    /**
         * 如果最近一次调用 next 返回的元素是从 forgetMeNot 列表中抽取的，则返回该元素。
         */
        private E lastRetElt = null;

        /**
         * 迭代器认为支持的 Queue 应该具有的 modCount 值。如果这个期望被违反，迭代器
         * 检测到并发修改。
         */
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = PriorityQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

    public int size() {
        return size;
    }

    /**
     * 从此优先队列中移除所有元素。
     * 调用此方法后，队列将为空。
     */
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            queue[i] = null;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public E poll() {
        if (size == 0)
            return null;
        int s = --size;
        modCount++;
        E result = (E) queue[0];
        E x = (E) queue[s];
        queue[s] = null;
        if (s != 0)
            siftDown(0, x);
        return result;
    }

    /**
     * 从队列中移除第 i 个元素。
     *
     * 通常情况下，此方法会保留 i-1 之前的元素（包括 i-1）。在这种情况下，它返回
     * null。偶尔，为了维护堆的不变性，它必须将列表中比 i 更后面的元素与 i 之前的元素交换。
     * 在这种情况下，此方法返回之前位于列表末尾且现在位于 i 之前的某个位置的元素。这一事实被
     * iterator.remove 用来避免遗漏遍历元素。
     */
    @SuppressWarnings("unchecked")
    private E removeAt(int i) {
        // assert i >= 0 && i < size;
        modCount++;
        int s = --size;
        if (s == i) // 移除最后一个元素
            queue[i] = null;
        else {
            E moved = (E) queue[s];
            queue[s] = null;
            siftDown(i, moved);
            if (queue[i] == moved) {
                siftUp(i, moved);
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    /**
     * 在位置 k 插入元素 x，通过将 x 逐级提升到树中，直到它大于或等于其父节点或成为根节点，以保持堆的不变性。
     *
     * 为了简化和加速强制转换和比较，Comparable 和 Comparator 版本被分开为不同的方法，这些方法在其他方面是相同的。（siftDown 也是如此。）
     *
     * @param k 要填充的位置
     * @param x 要插入的元素
     */
    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (key.compareTo((E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    /**
     * 在位置 k 插入元素 x，通过将 x 逐级降级到树中，直到它小于或等于其子节点或成为叶子节点，以保持堆的不变性。
     *
     * @param k 要填充的位置
     * @param x 要插入的元素
     */
    private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>)x;
        int half = size >>> 1;        // 当非叶子节点时循环
        while (k < half) {
            int child = (k << 1) + 1; // 假设左子节点是最小的
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
                c = queue[child = right];
            if (key.compareTo((E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = key;
    }


                @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                comparator.compare((E) c, (E) queue[right]) > 0)
                c = queue[child = right];
            if (comparator.compare(x, (E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    /**
     * 建立整个树的堆不变性，假设在调用之前元素的顺序是任意的。
     */
    @SuppressWarnings("unchecked")
    private void heapify() {
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }

    /**
     * 返回用于对队列中的元素进行排序的比较器，如果队列是根据其元素的
     * {@linkplain Comparable 自然顺序}进行排序的，则返回 {@code null}。
     *
     * @return 用于对队列进行排序的比较器，或者如果队列是根据其元素的
     *         自然顺序进行排序的，则返回 {@code null}
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * 将此队列保存到流中（即，序列化它）。
     *
     * @serialData 支持实例的数组的长度（int）被发出，然后是所有元素
     *             （每个都是 {@code Object}）按正确顺序发出。
     * @param s 流
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // 写出元素数量，以及任何隐藏的信息
        s.defaultWriteObject();

        // 写出数组长度，以兼容 1.5 版本
        s.writeInt(Math.max(2, size + 1));

        // 按正确顺序写出所有元素。
        for (int i = 0; i < size; i++)
            s.writeObject(queue[i]);
    }

    /**
     * 从流中重新构建 {@code PriorityQueue} 实例（即，反序列化它）。
     *
     * @param s 流
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 读取大小，以及任何隐藏的信息
        s.defaultReadObject();

        // 读取（并丢弃）数组长度
        s.readInt();

        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, size);
        queue = new Object[size];

        // 读取所有元素。
        for (int i = 0; i < size; i++)
            queue[i] = s.readObject();

        // 元素保证按“正确顺序”排列，但规范从未解释这可能是什么。
        heapify();
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，遍历此队列中的元素。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#SIZED}，
     * {@link Spliterator#SUBSIZED} 和 {@link Spliterator#NONNULL}。
     * 覆盖实现应记录额外的特性值。
     *
     * @return 一个遍历此队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator<E>(this, 0, -1, 0);
    }

    static final class PriorityQueueSpliterator<E> implements Spliterator<E> {
        /*
         * 这与 ArrayList Spliterator 非常相似，只是多了空检查。
         */
        private final PriorityQueue<E> pq;
        private int index;            // 当前索引，在前进/拆分时修改
        private int fence;            // 直到第一次使用时为 -1
        private int expectedModCount; // 在设置围栏时初始化

        /** 创建新的 spliterator 覆盖给定范围 */
        PriorityQueueSpliterator(PriorityQueue<E> pq, int origin, int fence,
                             int expectedModCount) {
            this.pq = pq;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // 在第一次使用时将围栏初始化为大小
            int hi;
            if ((hi = fence) < 0) {
                expectedModCount = pq.modCount;
                hi = fence = pq.size;
            }
            return hi;
        }

        public PriorityQueueSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PriorityQueueSpliterator<E>(pq, lo, index = mid,
                                                expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // 从循环中提升访问和检查
            PriorityQueue<E> q; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((q = pq) != null && (a = q.queue) != null) {
                if ((hi = fence) < 0) {
                    mc = q.modCount;
                    hi = q.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (E e;; ++i) {
                        if (i < hi) {
                            if ((e = (E) a[i]) == null) // 必须是 CME
                                break;
                            action.accept(e);
                        }
                        else if (q.modCount != mc)
                            break;
                        else
                            return;
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), lo = index;
            if (lo >= 0 && lo < hi) {
                index = lo + 1;
                @SuppressWarnings("unchecked") E e = (E)pq.queue[lo];
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
                if (pq.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }
}
