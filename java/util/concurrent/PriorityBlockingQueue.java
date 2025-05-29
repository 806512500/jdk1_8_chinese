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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * 一个无界 {@linkplain BlockingQueue 阻塞队列}，使用与 {@link PriorityQueue} 类相同的排序规则，并提供阻塞检索操作。虽然这个队列在逻辑上是无界的，但由于资源耗尽（导致 {@code OutOfMemoryError}），尝试添加元素可能会失败。此类不允许 {@code null} 元素。依赖于 {@linkplain
 * Comparable 自然排序} 的优先级队列也不允许插入不可比较的对象（这样做会导致
 * {@code ClassCastException}）。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link
 * Iterator} 接口的所有 <em>可选</em> 方法。方法 {@link
 * #iterator()} 提供的迭代器 <em>不</em> 保证以任何特定顺序遍历 PriorityBlockingQueue 的元素。如果需要有序遍历，可以考虑使用
 * {@code Arrays.sort(pq.toArray())}。此外，方法 {@code drainTo}
 * 可用于 <em>移除</em> 一些或所有元素，并按优先级顺序将它们放入另一个集合中。
 *
 * <p>此类上的操作不对具有相同优先级的元素的顺序做出任何保证。如果需要强制执行顺序，可以定义自定义类或比较器，使用次要键来打破主要优先级值的平局。例如，以下是一个类，它对可比较元素应用先进先出的平局解决策略。要使用它，您需要插入一个 {@code new FIFOEntry(anEntry)} 而不是一个普通的条目对象。
 *
 *  <pre> {@code
 * class FIFOEntry<E extends Comparable<? super E>>
 *     implements Comparable<FIFOEntry<E>> {
 *   static final AtomicLong seq = new AtomicLong(0);
 *   final long seqNum;
 *   final E entry;
 *   public FIFOEntry(E entry) {
 *     seqNum = seq.getAndIncrement();
 *     this.entry = entry;
 *   }
 *   public E getEntry() { return entry; }
 *   public int compareTo(FIFOEntry<E> other) {
 *     int res = entry.compareTo(other.entry);
 *     if (res == 0 && other.entry != this.entry)
 *       res = (seqNum < other.seqNum ? -1 : 1);
 *     return res;
 *   }
 * }}</pre>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
@SuppressWarnings("unchecked")
public class PriorityBlockingQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = 5595510919245408276L;

    /*
     * 实现使用基于数组的二叉堆，公共操作由单个锁保护。然而，调整大小期间的分配使用一个简单的自旋锁（仅在不持有主锁时使用），以便允许与分配操作并发地进行取操作。这避免了等待消费者的重复延迟和随之而来的元素堆积。分配过程中需要从锁中退后，这使得不可能简单地在锁内包装委托的
     * java.util.PriorityQueue 操作，正如此类的早期版本所做的那样。为了保持互操作性，序列化期间仍然使用一个普通的 PriorityQueue，这以暂时增加开销为代价维持了兼容性。
     */

    /**
     * 默认数组容量。
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * 可分配的最大数组大小。
     * 一些虚拟机在数组中保留一些头部字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过 VM 限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 以平衡二叉堆形式表示的优先级队列：队列[n] 的两个子节点分别是 queue[2*n+1] 和 queue[2*(n+1)]。优先级队列由比较器排序，或者如果比较器为 null，则使用元素的自然排序：对于堆中的每个节点 n 和 n 的每个后代 d，n <= d。假设队列非空，值最小的元素位于 queue[0]。
     */
    private transient Object[] queue;

    /**
     * 优先级队列中的元素数量。
     */
    private transient int size;

    /**
     * 比较器，如果优先级队列使用元素的自然排序，则为 null。
     */
    private transient Comparator<? super E> comparator;

    /**
     * 用于所有公共操作的锁
     */
    private final ReentrantLock lock;

    /**
     * 空时阻塞的条件
     */
    private final Condition notEmpty;

    /**
     * 用于分配的自旋锁，通过 CAS 获取。
     */
    private transient volatile int allocationSpinLock;

    /**
     * 仅用于序列化，以保持与此类早期版本的兼容性。仅在序列化/反序列化期间非空。
     */
    private PriorityQueue<E> q;

    /**
     * 创建一个具有默认初始容量（11）的 {@code PriorityBlockingQueue}，其元素按其 {@linkplain Comparable 自然排序} 排序。
     */
    public PriorityBlockingQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }


    /**
     * 创建一个具有指定初始容量的 {@code PriorityBlockingQueue}，其元素按照它们的
     * {@linkplain Comparable 自然顺序} 排序。
     *
     * @param initialCapacity 此优先级队列的初始容量
     * @throws IllegalArgumentException 如果 {@code initialCapacity} 小于 1
     */
    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * 创建一个具有指定初始容量的 {@code PriorityBlockingQueue}，其元素按照指定的
     * 比较器排序。
     *
     * @param initialCapacity 此优先级队列的初始容量
     * @param  comparator 用于对优先级队列进行排序的比较器。如果为 {@code null}，则使用元素的
     *         {@linkplain Comparable 自然顺序}。
     * @throws IllegalArgumentException 如果 {@code initialCapacity} 小于 1
     */
    public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.comparator = comparator;
        this.queue = new Object[initialCapacity];
    }

    /**
     * 创建一个包含指定集合中元素的 {@code PriorityBlockingQueue}。如果指定的集合是
     * {@link SortedSet} 或 {@link PriorityQueue}，则此优先级队列将按照相同的顺序进行排序。
     * 否则，此优先级队列将按照其元素的
     * {@linkplain Comparable 自然顺序} 进行排序。
     *
     * @param  c 要放入此优先级队列的集合的元素
     * @throws ClassCastException 如果根据优先级队列的顺序无法比较指定集合的元素
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public PriorityBlockingQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        boolean heapify = true; // 如果不知道是否为堆序，则为 true
        boolean screen = true;  // 如果必须筛选 null，则为 true
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            heapify = false;
        }
        else if (c instanceof PriorityBlockingQueue<?>) {
            PriorityBlockingQueue<? extends E> pq =
                (PriorityBlockingQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            screen = false;
            if (pq.getClass() == PriorityBlockingQueue.class) // 完全匹配
                heapify = false;
        }
        Object[] a = c.toArray();
        int n = a.length;
        if (c.getClass() != java.util.ArrayList.class)
            a = Arrays.copyOf(a, n, Object[].class);
        if (screen && (n == 1 || this.comparator != null)) {
            for (int i = 0; i < n; ++i)
                if (a[i] == null)
                    throw new NullPointerException();
        }
        this.queue = a;
        this.size = n;
        if (heapify)
            heapify();
    }

    /**
     * 尝试将数组扩展以至少容纳一个更多元素（但通常扩展约 50%），在竞争时放弃（允许重试）
     * （我们期望这种情况很少发生）。仅在持有锁时调用。
     *
     * @param array 堆数组
     * @param oldCap 数组的长度
     */
    private void tryGrow(Object[] array, int oldCap) {
        lock.unlock(); // 必须释放并重新获取主锁
        Object[] newArray = null;
        if (allocationSpinLock == 0 &&
            UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset,
                                     0, 1)) {
            try {
                int newCap = oldCap + ((oldCap < 64) ?
                                       (oldCap + 2) : // 如果容量小，则更快增长
                                       (oldCap >> 1));
                if (newCap - MAX_ARRAY_SIZE > 0) {    // 可能溢出
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError();
                    newCap = MAX_ARRAY_SIZE;
                }
                if (newCap > oldCap && queue == array)
                    newArray = new Object[newCap];
            } finally {
                allocationSpinLock = 0;
            }
        }
        if (newArray == null) // 如果另一个线程正在分配，则退后
            Thread.yield();
        lock.lock();
        if (newArray != null && queue == array) {
            queue = newArray;
            System.arraycopy(array, 0, newArray, 0, oldCap);
        }
    }

    /**
     * poll() 的内部机制。仅在持有锁时调用。
     */
    private E dequeue() {
        int n = size - 1;
        if (n < 0)
            return null;
        else {
            Object[] array = queue;
            E result = (E) array[0];
            E x = (E) array[n];
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(0, x, array, n);
            else
                siftDownUsingComparator(0, x, array, n, cmp);
            size = n;
            return result;
        }
    }

    /**
     * 在位置 k 插入项 x，通过将 x 逐级向上提升，直到它大于或等于其父节点或成为根节点，以维护堆不变性。
     *
     * 为了简化和加速转换和比较，Comparable 和 Comparator 版本被分开为不同的方法，这些方法在其他方面是相同的。（siftDown 也是如此。）
     * 这些方法是静态的，以堆状态作为参数，以简化在可能的比较器异常情况下的使用。
     *
     * @param k 要填充的位置
     * @param x 要插入的项
     * @param array 堆数组
     */
    private static <T> void siftUpComparable(int k, T x, Object[] array) {
        Comparable<? super T> key = (Comparable<? super T>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (key.compareTo((T) e) >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = key;
    }


    private static <T> void siftUpUsingComparator(int k, T x, Object[] array,
                                       Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (cmp.compare(x, (T) e) >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    /**
     * 在位置 k 插入元素 x，通过在树中反复降级 x 直到它小于或等于其子节点或成为叶子节点来保持堆的不变性。
     *
     * @param k 要填充的位置
     * @param x 要插入的元素
     * @param array 堆数组
     * @param n 堆的大小
     */
    private static <T> void siftDownComparable(int k, T x, Object[] array,
                                               int n) {
        if (n > 0) {
            Comparable<? super T> key = (Comparable<? super T>)x;
            int half = n >>> 1;           // 当非叶子节点时循环
            while (k < half) {
                int child = (k << 1) + 1; // 假设左子节点是最小的
                Object c = array[child];
                int right = child + 1;
                if (right < n &&
                    ((Comparable<? super T>) c).compareTo((T) array[right]) > 0)
                    c = array[child = right];
                if (key.compareTo((T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = key;
        }
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] array,
                                                    int n,
                                                    Comparator<? super T> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((T) c, (T) array[right]) > 0)
                    c = array[child = right];
                if (cmp.compare(x, (T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    /**
     * 在整个树中建立堆的不变性（如上所述），假设调用前元素的顺序没有任何特定要求。
     */
    private void heapify() {
        Object[] array = queue;
        int n = size;
        int half = (n >>> 1) - 1;
        Comparator<? super E> cmp = comparator;
        if (cmp == null) {
            for (int i = half; i >= 0; i--)
                siftDownComparable(i, (E) array[i], array, n);
        }
        else {
            for (int i = half; i >= 0; i--)
                siftDownUsingComparator(i, (E) array[i], array, n, cmp);
        }
    }

    /**
     * 将指定的元素插入到此优先队列中。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws ClassCastException 如果根据优先队列的排序规则，指定的元素无法与优先队列中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 将指定的元素插入到此优先队列中。
     * 由于队列是无界的，此方法永远不会返回 {@code false}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws ClassCastException 如果根据优先队列的排序规则，指定的元素无法与优先队列中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n, cap;
        Object[] array;
        while ((n = size) >= (cap = (array = queue).length))
            tryGrow(array, cap);
        try {
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftUpComparable(n, e, array);
            else
                siftUpUsingComparator(n, e, array, cmp);
            size = n + 1;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 将指定的元素插入到此优先队列中。
     * 由于队列是无界的，此方法永远不会阻塞。
     *
     * @param e 要添加的元素
     * @throws ClassCastException 如果根据优先队列的排序规则，指定的元素无法与优先队列中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void put(E e) {
        offer(e); // 从不阻塞
    }

    /**
     * 将指定的元素插入到此优先队列中。
     * 由于队列是无界的，此方法永远不会阻塞或返回 {@code false}。
     *
     * @param e 要添加的元素
     * @param timeout 此参数被忽略，因为方法从不阻塞
     * @param unit 此参数被忽略，因为方法从不阻塞
     * @return {@code true}（如
     *  {@link BlockingQueue#offer(Object,long,TimeUnit) BlockingQueue.offer} 所指定）
     * @throws ClassCastException 如果根据优先队列的排序规则，指定的元素无法与优先队列中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e); // 从不阻塞
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return dequeue();
        } finally {
            lock.unlock();
        }
    }


    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null)
                notEmpty.await();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (size == 0) ? null : (E) queue[0];
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回用于对队列中的元素进行排序的比较器，
     * 如果队列使用元素的 {@linkplain Comparable 自然排序}，则返回 {@code null}。
     *
     * @return 用于对队列中的元素进行排序的比较器，
     *         如果队列使用元素的自然排序，则返回 {@code null}
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 始终返回 {@code Integer.MAX_VALUE}，因为
     * {@code PriorityBlockingQueue} 不受容量限制。
     * @return 始终返回 {@code Integer.MAX_VALUE}
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 从队列中移除第 i 个元素。
     */
    private void removeAt(int i) {
        Object[] array = queue;
        int n = size - 1;
        if (n == i) // 移除最后一个元素
            array[i] = null;
        else {
            E moved = (E) array[n];
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(i, moved, array, n);
            else
                siftDownUsingComparator(i, moved, array, n, cmp);
            if (array[i] == moved) {
                if (cmp == null)
                    siftUpComparable(i, moved, array);
                else
                    siftUpUsingComparator(i, moved, array, cmp);
            }
        }
        size = n;
    }

    /**
     * 如果队列中包含一个或多个这样的元素，则移除指定元素的一个实例。
     * 更正式地说，移除一个元素 {@code e} 使得 {@code o.equals(e)} 为真。
     * 如果且仅当队列包含指定的元素（或等效地，如果队列因调用而改变）时，返回 {@code true}。
     *
     * @param o 要从队列中移除的元素，如果存在
     * @return 如果队列因调用而改变，则返回 {@code true}
     */
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1)
                return false;
            removeAt(i);
            return true;
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
            Object[] array = queue;
            for (int i = 0, n = size; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果且仅当队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)} 为真时，返回 {@code true}。
     *
     * @param o 要检查是否包含在队列中的对象
     * @return 如果队列包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个包含队列中所有元素的数组。
     * 返回的数组元素没有特定的顺序。
     *
     * <p>返回的数组是“安全的”，即没有对它的引用被此队列保留。（换句话说，此方法必须分配一个新的数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含队列中所有元素的数组
     */
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return Arrays.copyOf(queue, size);
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
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
            int n = Math.min(size, maxElements);
            for (int i = 0; i < n; i++) {
                c.add((E) queue[0]); // In this order, in case add() throws.
                dequeue();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 原子地从队列中移除所有元素。
     * 在此调用返回后，队列将为空。
     */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            int n = size;
            size = 0;
            for (int i = 0; i < n; i++)
                array[i] = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个包含此队列中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。
     * 返回的数组元素没有特定的顺序。
     * 如果队列适合指定的数组，则返回该数组。
     * 否则，将分配一个具有指定数组的运行时类型和此队列大小的新数组。
     *
     * <p>如果此队列适合指定的数组且有剩余空间
     * （即，数组中的元素比队列中的多），则数组中紧接队列末尾的元素将被设置为
     * {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。
     * 此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知只包含字符串的队列。
     * 以下代码可以用来将队列转储到一个新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 与 {@code toArray()} 的功能相同。
     *
     * @param a 如果足够大，则将队列的元素存储在此数组中；否则，为此目的分配一个相同运行时类型的新数组
     * @return 包含此队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (a.length < n)
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(queue, size, a.getClass());
            System.arraycopy(queue, 0, a, 0, n);
            if (a.length > n)
                a[n] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回一个迭代器，用于遍历此队列中的元素。迭代器不会以任何特定的顺序返回元素。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 一个迭代器，用于遍历此队列中的元素
     */
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    /**
     * 基于底层 q 数组的副本工作的快照迭代器。
     */
    final class Itr implements Iterator<E> {
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

    /**
     * 将此队列保存到流中（即序列化它）。
     *
     * 为了与此类的早期版本兼容，元素首先被复制到一个 java.util.PriorityQueue，然后进行序列化。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        lock.lock();
        try {
            // avoid zero capacity argument
            q = new PriorityQueue<E>(Math.max(size, 1), comparator);
            q.addAll(this);
            s.defaultWriteObject();
        } finally {
            q = null;
            lock.unlock();
        }
    }

    /**
     * 从流中重新构造此队列（即反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        try {
            s.defaultReadObject();
            int sz = q.size();
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, sz);
            this.queue = new Object[sz];
            comparator = q.comparator();
            addAll(q);
        } finally {
            q = null;
        }
    }


    // 类似于 Collections.ArraySnapshotSpliterator，但避免了在需要时才转换为数组
    static final class PBQSpliterator<E> implements Spliterator<E> {
        final PriorityBlockingQueue<E> queue;
        Object[] array;
        int index;
        int fence;

        PBQSpliterator(PriorityBlockingQueue<E> queue, Object[] array,
                       int index, int fence) {
            this.queue = queue;
            this.array = array;
            this.index = index;
            this.fence = fence;
        }

        final int getFence() {
            int hi;
            if ((hi = fence) < 0)
                hi = fence = (array = queue.toArray()).length;
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PBQSpliterator<E>(queue, array, lo, index = mid);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Object[] a; int i, hi; // 提取循环中的访问和检查
            if (action == null)
                throw new NullPointerException();
            if ((a = array) == null)
                fence = (a = queue.toArray()).length;
            if ((hi = fence) <= a.length &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept((E)a[i]); } while (++i < hi);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            if (getFence() > index && index >= 0) {
                @SuppressWarnings("unchecked") E e = (E) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() { return (long)(getFence() - index); }

        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * 返回一个遍历此队列中元素的 {@link Spliterator}。
     *
     * <p>返回的 spliterator 是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 还报告 {@link Spliterator#SUBSIZED}。
     *
     * @return 一个遍历此队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new PBQSpliterator<E>(this, null, 0, -1);
    }

    // 不安全机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long allocationSpinLockOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = PriorityBlockingQueue.class;
            allocationSpinLockOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("allocationSpinLock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
