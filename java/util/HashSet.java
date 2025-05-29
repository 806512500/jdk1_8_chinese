
/*
 * 版权所有 (c) 1997, 2021, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.InvalidObjectException;
import sun.misc.SharedSecrets;

/**
 * 该类实现了 <tt>Set</tt> 接口，由哈希表（实际上是 <tt>HashMap</tt> 实例）支持。
 * 它不对集合的迭代顺序做出任何保证；特别是，它不保证顺序将随着时间保持不变。该类允许 <tt>null</tt> 元素。
 *
 * <p>对于基本操作（<tt>add</tt>、<tt>remove</tt>、<tt>contains</tt> 和 <tt>size</tt>），
 * 该类提供了常数时间性能，前提是哈希函数将元素适当地分散到桶中。遍历此集合所需的时间与 <tt>HashSet</tt> 实例的大小（元素数量）
 * 加上支持的 <tt>HashMap</tt> 实例的“容量”（桶的数量）成正比。因此，如果迭代性能很重要，则初始容量不应设置得太高（或加载因子太低）。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果多个线程同时访问哈希集，并且至少有一个线程修改了集合，则必须从外部同步。这通常通过在自然封装集合的某个对象上同步来实现。
 *
 * 如果没有这样的对象，应该使用 {@link Collections#synchronizedSet Collections.synchronizedSet} 方法“包装”该集合。
 * 最好在创建时这样做，以防止集合的意外未同步访问：<pre>
 *   Set s = Collections.synchronizedSet(new HashSet(...));</pre>
 *
 * <p>此类的 <tt>iterator</tt> 方法返回的迭代器是 <i>快速失败</i> 的：如果在创建迭代器后以任何方式修改集合，
 * 除了通过迭代器自己的 <tt>remove</tt> 方法，迭代器将抛出 {@link ConcurrentModificationException}。
 * 因此，面对并发修改时，迭代器会快速且干净地失败，而不是在未来某个不确定的时间冒任意、不确定的行为风险。
 *
 * <p>请注意，迭代器的快速失败行为无法保证，因为通常不可能在存在未同步的并发修改的情况下做出任何硬性保证。
 * 快速失败迭代器在尽力而为的基础上抛出 <tt>ConcurrentModificationException</tt>。
 * 因此，编写依赖此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 该集合维护的元素类型
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     Set
 * @see     TreeSet
 * @see     HashMap
 * @since   1.2
 */

public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    static final long serialVersionUID = -5024744406713321676L;

    private transient HashMap<E,Object> map;

    // 与支持映射中的对象关联的虚拟值
    private static final Object PRESENT = new Object();

    /**
     * 构造一个新的空集合；支持的 <tt>HashMap</tt> 实例具有默认的初始容量（16）和加载因子（0.75）。
     */
    public HashSet() {
        map = new HashMap<>();
    }

    /**
     * 构造一个包含指定集合中的元素的新集合。创建的 <tt>HashMap</tt> 具有默认的加载因子（0.75）
     * 和足够的初始容量以包含指定集合中的元素。
     *
     * @param c 要放置到此集合中的集合的元素
     * @throws NullPointerException 如果指定的集合为 null
     */
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }

    /**
     * 构造一个新的空集合；支持的 <tt>HashMap</tt> 实例具有指定的初始容量和加载因子。
     *
     * @param      initialCapacity   哈希映射的初始容量
     * @param      loadFactor        哈希映射的加载因子
     * @throws     IllegalArgumentException 如果初始容量小于零，或加载因子非正
     */
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 构造一个新的空集合；支持的 <tt>HashMap</tt> 实例具有指定的初始容量和默认加载因子（0.75）。
     *
     * @param      initialCapacity   哈希表的初始容量
     * @throws     IllegalArgumentException 如果初始容量小于零
     */
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * 构造一个新的空链接哈希集。（此包私有构造函数仅由 LinkedHashSet 使用。）
     * 支持的 HashMap 实例是具有指定初始容量和加载因子的 LinkedHashMap。
     *
     * @param      initialCapacity   哈希映射的初始容量
     * @param      loadFactor        哈希映射的加载因子
     * @param      dummy             忽略（区分此构造函数与其他 int, float 构造函数。）
     * @throws     IllegalArgumentException 如果初始容量小于零，或加载因子非正
     */
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }


                /**
     * 返回此集合中元素的迭代器。元素返回的顺序是不确定的。
     *
     * @return 一个迭代器，用于遍历此集合中的元素
     * @see ConcurrentModificationException
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * 返回此集合中的元素数量（即集合的基数）。
     *
     * @return 此集合中的元素数量（即集合的基数）
     */
    public int size() {
        return map.size();
    }

    /**
     * 如果此集合不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此集合不包含任何元素，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 如果此集合包含指定的元素，则返回 <tt>true</tt>。
     * 更正式地说，当且仅当此集合包含一个元素 <tt>e</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt> 时，返回 <tt>true</tt>。
     *
     * @param o 要测试其是否存在于此集合中的元素
     * @return 如果此集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 如果指定的元素尚未存在于此集合中，则将其添加到此集合中。
     * 更正式地说，如果此集合不包含一个元素 <tt>e2</tt> 使得
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>，则将指定的元素 <tt>e</tt> 添加到此集合中。
     * 如果此集合已经包含该元素，则调用将集合保持不变并返回 <tt>false</tt>。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果此集合之前不包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    /**
     * 如果指定的元素存在于此集合中，则将其从此集合中移除。
     * 更正式地说，移除一个元素 <tt>e</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，
     * 如果此集合包含这样的元素。如果此集合包含该元素（或等效地，如果此集合因调用而改变），则返回 <tt>true</tt>。
     * （调用返回后，此集合将不再包含该元素。）
     *
     * @param o 如果存在，则要从此集合中移除的对象
     * @return 如果集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }

    /**
     * 从此集合中移除所有元素。
     * 调用返回后，集合将为空。
     */
    public void clear() {
        map.clear();
    }

    /**
     * 返回此 <tt>HashSet</tt> 实例的浅拷贝：元素本身不会被克隆。
     *
     * @return 此集合的浅拷贝
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 将此 <tt>HashSet</tt> 实例的状态保存到流中（即，序列化它）。
     *
     * @serialData 序列化数据包括支持 <tt>HashMap</tt> 实例的容量（int）和负载因子（float），
     *             集合的大小（即它包含的元素数量）（int），以及所有元素（每个都是 Object），
     *             顺序不确定。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // 写出任何隐藏的序列化魔术
        s.defaultWriteObject();

        // 写出 HashMap 容量和负载因子
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // 写出大小
        s.writeInt(map.size());

        // 以正确的顺序写出所有元素。
        for (E e : map.keySet())
            s.writeObject(e);
    }

    /**
     * 从流中恢复 <tt>HashSet</tt> 实例（即，反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 消耗并忽略流字段（目前为零）。
        s.readFields();
        // 读取容量并验证非负。
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("非法容量: " +
                                             capacity);
        }

        // 读取负载因子并验证为正且非 NaN。
        float loadFactor = s.readFloat();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("非法负载因子: " +
                                             loadFactor);
        }
        // 将负载因子限制在 0.25...4.0 的范围内。
        loadFactor = Math.min(Math.max(0.25f, loadFactor), 4.0f);

        // 读取大小并验证非负。
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("非法大小: " + size);
        }
        // 根据大小和负载因子设置容量，确保 HashMap 至少有 25% 的填充率，但限制在最大容量内。
        capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                HashMap.MAXIMUM_CAPACITY);

        // 构造支持映射时将延迟创建数组，因此在构造之前进行检查。调用 HashMap.tableSizeFor 计算实际分配大小。
        // 检查 Map.Entry[].class，因为它是最接近实际创建的公共类型。

        SharedSecrets.getJavaOISAccess()
                     .checkArray(s, Map.Entry[].class, HashMap.tableSizeFor(capacity));

        // 创建支持 HashMap
        map = (((HashSet<?>)this) instanceof LinkedHashSet ?
               new LinkedHashMap<E,Object>(capacity, loadFactor) :
               new HashMap<E,Object>(capacity, loadFactor));


                    // 以正确的顺序读取所有元素。
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
                E e = (E) s.readObject();
            map.put(e, PRESENT);
        }
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，用于遍历此集合中的元素。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#DISTINCT}。 覆盖实现应记录额外的特征值。
     *
     * @return 一个遍历此集合中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new HashMap.KeySpliterator<E,Object>(map, 0, -1, 0, 0);
    }
}
