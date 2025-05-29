
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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import sun.misc.SharedSecrets;

/**
 * 基于哈希表的 <tt>Map</tt> 接口实现。此实现提供了所有可选的映射操作，并允许 <tt>null</tt> 值和 <tt>null</tt> 键。
 * （<tt>HashMap</tt> 类大致相当于 <tt>Hashtable</tt>，但它是不同步的，并允许 null 值。）此类不对映射的顺序做出任何保证；
 * 特别是，它不保证顺序会随着时间保持不变。
 *
 * <p>此实现为基本操作（<tt>get</tt> 和 <tt>put</tt>）提供了常数时间性能，前提是哈希函数将元素适当地分散到桶中。
 * 遍历集合视图需要的时间与 <tt>HashMap</tt> 实例的“容量”（桶的数量）及其大小（键值映射的数量）成正比。
 * 因此，如果迭代性能很重要，设置初始容量过高（或负载因子过低）是非常重要的。
 *
 * <p><tt>HashMap</tt> 实例有两个影响其性能的参数：<i>初始容量</i> 和 <i>负载因子</i>。
 * <i>容量</i> 是哈希表中的桶数，初始容量只是创建哈希表时的容量。
 * <i>负载因子</i> 是哈希表在自动增加容量之前允许填充的程度的度量。当哈希表中的条目数超过负载因子和当前容量的乘积时，
 * 哈希表将被 <i>重新哈希</i>（即内部数据结构被重建），使得哈希表的桶数大约翻倍。
 *
 * <p>一般来说，默认负载因子（0.75）在时间和空间成本之间提供了良好的平衡。较高的值会减少空间开销，但会增加查找成本
 * （反映在 <tt>HashMap</tt> 类的大多数操作中，包括 <tt>get</tt> 和 <tt>put</tt>）。
 * 在设置初始容量时，应考虑映射中预期的条目数及其负载因子，以尽量减少重新哈希操作的次数。如果初始容量大于最大条目数除以负载因子，
 * 则永远不会发生重新哈希操作。
 *
 * <p>如果要在 <tt>HashMap</tt> 实例中存储许多映射，创建时设置足够大的容量将比让其根据需要自动重新哈希以增长表更高效。
 * 注意，使用许多具有相同 {@code hashCode()} 的键肯定会降低任何哈希表的性能。为了减轻影响，当键是 {@link Comparable} 时，
 * 该类可能会使用键之间的比较顺序来帮助打破平局。
 *
 * <p><strong>请注意，此实现不同步。</strong> 如果多个线程同时访问哈希映射，并且至少有一个线程结构化地修改映射，
 * 则必须从外部进行同步。（结构化修改是任何添加或删除一个或多个映射的操作；仅更改实例中已包含的键关联的值不是结构化修改。）
 * 通常通过同步某个自然封装映射的对象来实现这一点。
 *
 * 如果没有这样的对象，应使用 {@link Collections#synchronizedMap Collections.synchronizedMap} 方法“包装”映射。
 * 最好在创建时这样做，以防止意外的未同步访问映射：<pre>
 *   Map m = Collections.synchronizedMap(new HashMap(...));</pre>
 *
 * <p>此类的所有“集合视图方法”返回的迭代器都是 <i>快速失败的</i>：如果在创建迭代器后以任何方式结构化地修改映射，
 * 除非通过迭代器自身的 <tt>remove</tt> 方法，迭代器将抛出 {@link ConcurrentModificationException}。
 * 因此，在并发修改的情况下，迭代器会快速且干净地失败，而不是在未来某个不确定的时间点冒险出现任意的、不确定的行为。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为通常情况下，在存在未同步的并发修改时，无法做出任何硬性保证。
 * 快速失败迭代器在尽力的基础上抛出 <tt>ConcurrentModificationException</tt>。因此，编写依赖此异常正确性的程序是错误的：
 * <i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <K> 由此映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.2
 */
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * 实现说明。
     *
     * 该映射通常充当分箱（分桶）哈希表，但当桶变得太大时，它们会转换为由
     * java.util.TreeMap 中类似的 TreeNode 组成的桶。大多数方法尝试使用普通桶，但在适用时
     * 会委托给 TreeNode 方法（只需检查节点的 instanceof 即可）。当桶中的 TreeNode 过多时，
     * 可以更快地查找。然而，由于在正常使用中绝大多数桶不会过度填充，因此在表方法中检查树桶的存在可能会延迟。
     *
     * 树桶（即，所有元素都是 TreeNode 的桶）主要按 hashCode 排序，但在发生冲突时，如果两个元素
     * 是“类 C 实现 Comparable<C>”类型，则使用它们的 compareTo 方法进行排序。（我们通过反射保守地检查泛型类型以验证这一点
     * ——参见方法 comparableClassFor）。树桶的额外复杂性在提供最坏情况下的 O(log n) 操作时是值得的，
     * 当键具有不同的哈希值或可排序时。因此，性能在以下情况下降得较为优雅：当 hashCode() 方法返回分布不良的值时，
     * 以及当许多键共享一个 hashCode 时，只要它们也是 Comparable 的。（如果这两者都不适用，我们可能会在时间和空间上浪费大约两倍的开销，
     * 但已知的唯一情况源于用户编程实践不佳，这些实践已经非常慢，以至于这一点几乎没有区别。）
     *
     * 由于 TreeNode 的大小大约是普通节点的两倍，因此我们仅在桶包含足够多的节点时使用它们（参见 TREEIFY_THRESHOLD）。
     * 当它们变得太小（由于删除或调整大小）时，它们会转换回普通桶。在用户 hashCode 分布良好的情况下，树桶很少使用。
     * 理想情况下，在随机 hashCode 下，桶中节点的频率遵循泊松分布
     * (http://en.wikipedia.org/wiki/Poisson_distribution)，对于默认的 0.75 的调整大小阈值，平均参数约为 0.5，
     * 尽管由于调整大小的粒度，方差较大。忽略方差，k 个节点的预期出现次数为 (exp(-0.5) * pow(0.5, k) / factorial(k))。
     * 前几个值是：
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: 小于一千万分之一
     *
     * 树桶的根通常是其第一个节点。然而，有时（目前仅在 Iterator.remove 时），根可能在其他地方，但可以通过跟随父链接
     * （方法 TreeNode.root()）恢复。
     *
     * 所有适用的内部方法接受一个哈希码作为参数（通常从公共方法提供），允许它们相互调用而无需重新计算用户哈希码。
     * 大多数内部方法还接受一个“tab”参数，通常是当前表，但在调整大小或转换时可能是新表或旧表。
     *
     * 当桶列表被树化、拆分或取消树化时，我们保持它们相同的相对访问/遍历顺序（即，字段 Node.next）以更好地保持局部性，
 * 并稍微简化处理拆分和遍历时调用 iterator.remove 的情况。在插入时使用比较器时，为了在重新平衡时保持总顺序（或此处所需的程度），
 * 我们使用类和 identityHashCodes 作为决胜器。
 *
 * 平凡模式与树模式的使用和转换因 LinkedHashMap 子类的存在而变得复杂。参见下面定义的在插入、删除和访问时调用的钩子方法，
 * 以允许 LinkedHashMap 内部保持独立于这些机制。（这也要求将映射实例传递给可能创建新节点的某些实用方法。）
 *
 * 类似于并发编程的 SSA 基于的编码风格有助于避免所有扭曲的指针操作中的别名错误。
 */

                /**
     * 默认初始容量 - 必须是2的幂。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 即16

    /**
     * 最大容量，如果构造函数的参数隐式指定了更高的值，则使用此值。
     * 必须是2的幂且 <= 1<<30。
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 当构造函数中未指定时使用的负载因子。
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 使用树而不是列表的bin计数阈值。
     * 当向具有至少这么多节点的bin中添加元素时，bin将转换为树。该值必须大于2，并且至少为8，以符合树移除时关于转换回普通bin的假设。
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 在调整大小操作期间取消树化的bin计数阈值。应小于TREEIFY_THRESHOLD，并且最多为6，以符合移除时缩小检测的假设。
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * bin可以被树化的最小表容量。（否则，如果bin中的节点过多，表将被调整大小。）
     * 应至少为4 * TREEIFY_THRESHOLD，以避免调整大小和树化阈值之间的冲突。
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 基本的哈希bin节点，用于大多数条目。（参见下面的TreeNode子类，以及LinkedHashMap中的Entry子类。）
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- 静态工具 -------------- */

    /**
     * 计算key.hashCode()并传播（异或）哈希的较高位到较低位。由于表使用2的幂的掩码，仅在当前掩码以上位不同的哈希集将始终发生冲突。（已知的例子包括在小表中持有连续整数的Float键集。）因此，我们应用一个变换，将较高位的影响向下传播。在速度、效用和位传播质量之间存在权衡。因为许多常见的哈希集已经分布合理（因此不会从传播中受益），并且因为我们使用树来处理bin中的大量冲突，我们只是以最便宜的方式异或一些移位的位，以减少系统损失，同时也将原本永远不会用于索引计算的最高位的影响纳入其中，因为受到表边界的限制。
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 如果x的形式为“class C implements Comparable<C>”，则返回x的Class，否则返回null。
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // 绕过检查
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // 类型参数是c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * 如果x匹配kc（k的筛选后的Comparable类），则返回k.compareTo(x)，否则返回0。
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // 用于转换为Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * 返回给定目标容量的2的幂大小。
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- 字段 -------------- */

    /**
     * 表，首次使用时初始化，并根据需要调整大小。分配时，长度始终是2的幂。（我们还容忍某些操作中的长度为零，以允许当前不需要的引导机制。）
     */
    transient Node<K,V>[] table;

    /**
     * 存储缓存的entrySet()。注意，AbstractMap字段用于keySet()和values()。
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * 此映射中包含的键值映射的数量。
     */
    transient int size;

                /**
     * 此 HashMap 被结构修改的次数
     * 结构修改是指改变 HashMap 中映射数量或以其他方式修改其内部结构（例如，
     * 重新散列）。此字段用于使 HashMap 的 Collection 视图上的迭代器快速失败。 （参见 ConcurrentModificationException）。
     */
    transient int modCount;

    /**
     * 要调整大小的下一个大小值（容量 * 负载因子）。
     *
     * @serial
     */
    // （在序列化时，javadoc 描述是正确的。
    // 此外，如果表数组尚未分配，此
    // 字段保存初始数组容量，或零表示
    // 默认初始容量。）
    int threshold;

    /**
     * 散列表的负载因子。
     *
     * @serial
     */
    final float loadFactor;

    /* ---------------- 公共操作 -------------- */

    /**
     * 使用指定的初始容量和负载因子构造一个空的 <tt>HashMap</tt>。
     *
     * @param  initialCapacity 初始容量
     * @param  loadFactor      负载因子
     * @throws IllegalArgumentException 如果初始容量为负
     *         或负载因子为非正
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法初始容量: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("非法负载因子: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 使用指定的初始容量和默认负载因子（0.75）构造一个空的 <tt>HashMap</tt>。
     *
     * @param  initialCapacity 初始容量。
     * @throws IllegalArgumentException 如果初始容量为负。
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 使用默认初始容量（16）和默认负载因子（0.75）构造一个空的 <tt>HashMap</tt>。
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // 所有其他字段默认
    }

    /**
     * 构造一个新的 <tt>HashMap</tt>，其映射与指定的 <tt>Map</tt> 相同。 <tt>HashMap</tt> 以
     * 默认负载因子（0.75）和足够的初始容量创建，以
     * 持有指定 <tt>Map</tt> 中的映射。
     *
     * @param   m 要放置在此映射中的映射的 map
     * @throws  NullPointerException 如果指定的 map 为 null
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * 实现 Map.putAll 和 Map 构造函数。
     *
     * @param m map
     * @param evict 当最初构造此映射时为 false，否则为 true（传递给方法 afterNodeInsertion）。
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // 预分配大小
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * 返回此映射中的键值映射数量。
     *
     * @return 此映射中的键值映射数量
     */
    public int size() {
        return size;
    }

    /**
     * 如果此映射不包含键值映射，则返回 <tt>true</tt>。
     *
     * @return 如果此映射不包含键值映射，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回指定键映射的值，
     * 或如果此映射不包含键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键
     * {@code k} 到值 {@code v} 的映射，使得 {@code (key==null ? k==null :
     * key.equals(k))}，则此方法返回 {@code v}；否则
     * 它返回 {@code null}。 （最多可以有一个这样的映射。）
     *
     * <p>返回值为 {@code null} 并不<i>一定</i>
     * 表示映射中没有键的映射；也可能是映射显式地将键映射到 {@code null}。
     * 可以使用 {@link #containsKey containsKey} 操作
     * 区分这两种情况。
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 实现 Map.get 及相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @return 节点，或如果没有则返回 null
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // 始终检查第一个节点
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }


                /**
     * 如果此映射包含指定键的映射，则返回 <tt>true</tt>。
     *
     * @param   key   要测试其是否存在于此映射中的键
     * @return 如果此映射包含指定键的映射，则返回 <tt>true</tt>。
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 将指定的值与此映射中的指定键关联。如果此映射之前包含该键的映射，则旧值将被替换。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 之前与此 <tt>key</tt> 关联的值，如果此 <tt>key</tt> 没有映射，则返回 <tt>null</tt>。
     *         （返回 <tt>null</tt> 也可能表示此映射之前将 <tt>null</tt> 与此 <tt>key</tt> 关联。）
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 实现 Map.put 及相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @param value 要放入的值
     * @param onlyIfAbsent 如果为 true，则不更改现有值
     * @param evict 如果为 false，则表处于创建模式。
     * @return 之前的值，如果没有则返回 null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // 现有键的映射
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * 初始化或加倍表的大小。如果为 null，则根据字段 threshold 中的初始容量目标分配。
     * 否则，由于我们使用的是二的幂扩展，每个 bin 中的元素必须保持在相同的索引，或在新表中移动
     * 一个二的幂的偏移量。
     *
     * @return 表
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // 初始容量被放在 threshold 中
            newCap = oldThr;
        else {               // 零初始阈值表示使用默认值
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // 保持顺序
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

                /**
     * 用给定的哈希值索引处的链表中的所有节点替换为红黑树，除非表太小，在这种情况下会进行扩容。
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

    /**
     * 将指定映射中的所有映射复制到此映射中。
     * 这些映射将替换此映射中当前存在于指定映射中的任何键的映射。
     *
     * @param m 要存储在此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * 如果存在，则从映射中移除指定键的映射。
     *
     * @param  key 要从映射中移除的键
     * @return 与 <tt>key</tt> 关联的前一个值，如果没有 <tt>key</tt> 的映射，则返回
     *         <tt>null</tt>。（<tt>null</tt> 的返回值也可能表示映射之前将 <tt>null</tt> 与 <tt>key</tt> 关联。）
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    /**
     * 实现 Map.remove 和相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @param value 如果 matchValue 为 true，则仅在值相等时移除，否则忽略
     * @param matchValue 如果为 true，则仅在值相等时移除
     * @param movable 如果为 false，则在移除时不要移动其他节点
     * @return 节点，如果没有则返回 null
     */
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    /**
     * 从此映射中移除所有映射。
     * 该映射在调用此方法后将为空。
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>。
     *
     * @param value 要测试其在此映射中的存在的值
     * @return 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回一个包含此映射中的键的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改映射（通过迭代器自身的 <tt>remove</tt> 操作除外），则迭代结果是不确定的。该集合支持元素移除，这会通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作移除映射中的相应映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含此映射中的键的集合视图
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }


                /**
     * 返回一个包含此映射中所有值的 {@link Collection} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （除通过迭代器自身的 <tt>remove</tt> 操作外），迭代的结果是未定义的。该集合支持元素删除，
     * 通过 <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、
     * <tt>retainAll</tt> 和 <tt>clear</tt> 操作删除对应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含此映射中所有值的视图
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回一个包含此映射中所有映射项的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （除通过迭代器自身的 <tt>remove</tt> 操作或通过迭代器返回的映射项的 <tt>setValue</tt> 操作外），
     * 迭代的结果是未定义的。该集合支持元素删除，通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、
     * <tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作删除对应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含此映射中所有映射项的视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // 覆盖 JDK8 Map 扩展方法

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }


                public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
        }
        else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization

    /**
     * 返回此 <tt>HashMap</tt> 实例的浅拷贝：键和值本身不会被克隆。
     *
     * @return 此映射的浅拷贝
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }


                // 这些方法在序列化 HashSet 时也会使用
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * 将 <tt>HashMap</tt> 实例的状态保存到流中（即，序列化它）。
     *
     * @serialData HashMap 的 <i>容量</i>（桶数组的长度）被发出（int），然后是
     *             <i>大小</i>（一个 int，键值对的数量），然后是每个键值对的键（Object）和值（Object）。
     *             键值对的发出顺序没有特定要求。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // 写出阈值、加载因子和任何隐藏的信息
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * 从流中重新构建此映射（即，反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws IOException 如果发生 I/O 错误
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();

        // 读取加载因子（忽略阈值）
        float lf = fields.get("loadFactor", 0.75f);
        if (lf <= 0 || Float.isNaN(lf))
            throw new InvalidObjectException("非法的加载因子: " + lf);

        lf = Math.min(Math.max(0.25f, lf), 4.0f);
        HashMap.UnsafeHolder.putLoadFactor(this, lf);

        reinitialize();

        s.readInt();                // 读取并忽略桶的数量
        int mappings = s.readInt(); // 读取映射的数量（大小）
        if (mappings < 0) {
            throw new InvalidObjectException("非法的映射数量: " + mappings);
        } else if (mappings == 0) {
            // 使用默认值
        } else if (mappings > 0) {
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);

            // 检查 Map.Entry[].class，因为它是实际创建的最近的公共类型。
            SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // 读取键和值，并将映射放入 HashMap 中
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    // 支持在反序列化期间重置最终字段
    private static final class UnsafeHolder {
        private UnsafeHolder() { throw new InternalError(); }
        private static final sun.misc.Unsafe unsafe
                = sun.misc.Unsafe.getUnsafe();
        private static final long LF_OFFSET;
        static {
            try {
                LF_OFFSET = unsafe.objectFieldOffset(HashMap.class.getDeclaredField("loadFactor"));
            } catch (NoSuchFieldException e) {
                throw new InternalError();
            }
        }
        static void putLoadFactor(HashMap<?, ?> map, float lf) {
            unsafe.putFloat(map, LF_OFFSET, lf);
        }
    }

    /* ------------------------------------------------------------ */
    // 迭代器

    abstract class HashIterator {
        Node<K,V> next;        // 下一个要返回的条目
        Node<K,V> current;     // 当前条目
        int expectedModCount;  // 用于快速失败
        int index;             // 当前槽位

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // 前进到第一个条目
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }


                /* ------------------------------------------------------------ */
    // 分割器

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // 当前节点
        int index;                  // 当前索引，在前进/分割时修改
        int fence;                  // 最后一个索引加一
        int est;                    // 大小估计
        int expectedModCount;       // 用于一致性检查

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // 在首次使用时初始化fence和大小
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // 强制初始化
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }
    }


                    public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support


    /*
     * 以下包保护的方法旨在被 LinkedHashMap 覆盖，但不被任何其他子类覆盖。
     * 几乎所有其他内部方法也都是包保护的，但被声明为 final，因此可以被 LinkedHashMap、视图类和 HashSet 使用。
     */

    // 创建一个常规（非树）节点
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 用于将 TreeNodes 转换为普通节点
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // 创建一个树节点
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 用于 treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * 重置为初始默认状态。由 clone 和 readObject 调用。
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // 回调以允许 LinkedHashMap 执行后操作
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // 仅从 writeObject 调用，以确保兼容的顺序。
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // 树节点

    /**
     * 用于树节点的条目。扩展了 LinkedHashMap.Entry（它又扩展了 Node），因此可以作为常规节点或链接节点的扩展使用。
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // 红黑树链接
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // 删除时需要取消链接 next
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * 返回包含此节点的树的根。
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * 确保给定的根是其 bin 的第一个节点。
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }


                    /**
         * 从根节点 p 开始查找具有给定哈希值和键的节点。
         * kc 参数在首次使用时缓存 comparableClassFor(key) 以比较键。
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * 调用根节点的 find 方法。
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * 当哈希码相等且键不可比较时，用于排序插入的决斗工具。我们不要求完全的顺序，只需要一个一致的插入规则来保持
         * 在重新平衡期间的等价性。进一步的决斗简化了测试。
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * 形成从该节点链接的节点树。
         */
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * 返回一个列表，用非 TreeNode 替换从该节点链接的节点。
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                Node<K,V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * 树版本的 putVal。
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }


                    /**
         * 移除给定的节点，该节点必须在调用前存在。
         * 这比典型的红黑树删除代码更复杂，因为我们不能交换内部节点与叶子后继节点的内容，
         * 因为这些叶子后继节点可能被“next”指针独立访问。所以，我们交换树的链接。
         * 如果当前树的节点数量看起来太少，该桶将被转换回普通桶。
         * （测试触发点在2到6个节点之间，具体取决于树的结构）。
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null
                || (movable
                    && (root.right == null
                        || (rl = root.left) == null
                        || rl.left == null))) {
                tab[index] = first.untreeify(map);  // 节点太少
                return;
            }
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // 查找后继节点
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // 交换颜色
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p 是 s 的直接父节点
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // 断开连接
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * 将树桶中的节点拆分为较低和较高的树桶，如果现在节点太少则取消树化。
         * 仅在调整大小时调用；有关拆分位和索引的讨论，请参见上述内容。
         *
         * @param map 地图
         * @param tab 用于记录桶头的表
         * @param index 正在拆分的表的索引
         * @param bit 用于拆分的哈希位
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // 重新链接到低和高列表中，保留顺序
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // （否则已经是树化的）
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }


                    /* ------------------------------------------------------------ */
        // 红黑树方法，全部改编自CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // 对称
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }


                    /**
         * 递归不变性检查
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
