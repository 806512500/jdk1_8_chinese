
/*
 * 版权所有 (c) 2000, 2021，Oracle 和/或其附属公司。保留所有权利。
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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * 该类使用哈希表实现 <tt>Map</tt> 接口，并在比较键（和值）时使用引用相等性而不是对象相等性。换句话说，在 <tt>IdentityHashMap</tt> 中，两个键
 * <tt>k1</tt> 和 <tt>k2</tt> 仅当 <tt>(k1==k2)</tt> 时才被视为相等。（在正常的 <tt>Map</tt> 实现（如 <tt>HashMap</tt>）中，两个键
 * <tt>k1</tt> 和 <tt>k2</tt> 仅当 <tt>(k1==null ? k2==null : k1.equals(k2))</tt> 时才被视为相等。）
 *
 * <p><b>该类不是通用的 <tt>Map</tt> 实现！虽然该类实现了 <tt>Map</tt> 接口，但它有意违反了 <tt>Map</tt> 的通用契约，该契约规定在比较对象时使用 <tt>equals</tt> 方法。该类仅设计用于需要引用相等性语义的少数情况。</b>
 *
 * <p>该类的一个典型用例是 <i>拓扑保持对象图转换</i>，例如序列化或深复制。为了执行此类转换，程序必须维护一个“节点表”，用于跟踪已经处理的所有对象引用。节点表不得将不同的对象视为相等，即使它们碰巧相等。另一个典型的用例是维护 <i>代理对象</i>。例如，调试设施可能希望为程序中调试的每个对象维护一个代理对象。
 *
 * <p>该类提供了所有可选的映射操作，并允许 <tt>null</tt> 值和 <tt>null</tt> 键。该类不对映射的顺序做出任何保证；特别是，它不保证顺序将随时间保持不变。
 *
 * <p>该类为基本操作（<tt>get</tt> 和 <tt>put</tt>）提供了常数时间性能，前提是系统身份哈希函数（{@link System#identityHashCode(Object)}）
 * 能够将元素适当地分散到各个桶中。
 *
 * <p>该类有一个调优参数（影响性能但不影响语义）：<i>预期最大大小</i>。此参数是映射预期持有的键值对的最大数量。内部，此参数用于确定哈希表最初包含的桶的数量。预期最大大小与桶的数量之间的精确关系未指定。
 *
 * <p>如果映射的大小（键值对的数量）显著超过预期最大大小，桶的数量将增加。增加桶的数量（“重新哈希”）可能相当昂贵，因此创建具有足够大的预期最大大小的身份哈希映射是有益的。另一方面，遍历集合视图所需的时间与哈希表中的桶数量成正比，因此如果特别关心遍历性能或内存使用，不要将预期最大大小设置得太高。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果多个线程同时访问身份哈希映射，并且至少有一个线程对映射进行结构修改，则必须从外部进行同步。（结构修改是任何添加或删除一个或多个映射的操作；仅更改实例已包含的键关联的值不是结构修改。）这通常通过同步某个自然封装映射的对象来实现。
 *
 * 如果没有这样的对象，应使用 {@link Collections#synchronizedMap Collections.synchronizedMap}
 * 方法“包装”映射。最好在创建时进行此操作，以防止意外的未同步访问映射：<pre>
 *   Map m = Collections.synchronizedMap(new IdentityHashMap(...));</pre>
 *
 * <p>该类的“集合视图方法”返回的集合的 <tt>iterator</tt> 方法返回的迭代器是 <i>快速失败的</i>：如果在迭代器创建后以任何方式对映射进行结构修改（除了通过迭代器自身的 <tt>remove</tt> 方法），迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，迭代器会快速且干净地失败，而不是在未来某个不确定的时间点表现出任意的、非确定性的行为。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为通常来说，在存在未同步的并发修改的情况下，无法做出任何硬性保证。快速失败迭代器在尽力的基础上抛出 <tt>ConcurrentModificationException</tt>。
 * 因此，编写依赖此异常正确性的程序是错误的：<i>快速失败迭代器应仅用于检测错误。</i>
 *
 * <p>实现说明：这是一个简单的 <i>线性探测</i> 哈希表，例如在 Sedgewick 和 Knuth 的文本中描述的那样。数组交替存储键和值。（对于大型表，这比使用单独的数组具有更好的局部性。）对于许多 JRE 实现和操作混合，该类将比
 * {@link HashMap}（使用 <i>链表</i> 而不是线性探测）提供更好的性能。
 *
 * <p>该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @see     System#identityHashCode(Object)
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     HashMap
 * @see     TreeMap
 * @author  Doug Lea 和 Josh Bloch
 * @since   1.4
 */


public class IdentityHashMap<K,V>
    extends AbstractMap<K,V>
    implements Map<K,V>, java.io.Serializable, Cloneable
{
    /**
     * 无参数构造函数使用的初始容量。
     * 必须是2的幂。值32对应于给定负载因子2/3的（指定）预期最大大小21。
     */
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * 如果构造函数的参数隐式指定了较小的值，则使用最小容量。
     * 值4对应于给定负载因子2/3的预期最大大小2。
     * 必须是2的幂。
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * 如果构造函数的参数隐式指定了较大的值，则使用最大容量。
     * 必须是2的幂且 <= 1<<29。
     *
     * 实际上，映射不能容纳超过 MAXIMUM_CAPACITY-1 个项
     * 因为它必须至少有一个 key == null 的槽
     * 以避免在 get()、put()、remove() 中出现无限循环
     */
    private static final int MAXIMUM_CAPACITY = 1 << 29;

    /**
     * 必要时调整大小的表。长度必须始终是2的幂。
     */
    transient Object[] table; // non-private to simplify nested class access

    /**
     * 此标识哈希映射中包含的键值映射的数量。
     *
     * @serial
     */
    int size;

    /**
     * 修改次数，以支持快速失败迭代器
     */
    transient int modCount;

    /**
     * 表内表示 null 键的值。
     */
    static final Object NULL_KEY = new Object();

    /**
     * 如果键为 null，则使用 NULL_KEY。
     */
    private static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    /**
     * 将内部表示的 null 键返回给调用者为 null。
     */
    static final Object unmaskNull(Object key) {
        return (key == NULL_KEY ? null : key);
    }

    /**
     * 构造一个新的、空的标识哈希映射，具有默认的预期最大大小（21）。
     */
    public IdentityHashMap() {
        init(DEFAULT_CAPACITY);
    }

    /**
     * 构造一个新的、空的映射，具有指定的预期最大大小。
     * 将比预期数量更多的键值映射放入映射中可能会导致内部数据结构增长，这可能会消耗一些时间。
     *
     * @param expectedMaxSize 映射的预期最大大小
     * @throws IllegalArgumentException 如果 <tt>expectedMaxSize</tt> 为负
     */
    public IdentityHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: "
                                               + expectedMaxSize);
        init(capacity(expectedMaxSize));
    }

    /**
     * 返回给定预期最大大小的适当容量。
     * 返回介于 MINIMUM_CAPACITY 和 MAXIMUM_CAPACITY 之间（包括）的最小2的幂，如果存在这样的数，则返回大于 (3 *
     * expectedMaxSize)/2 的数。否则返回 MAXIMUM_CAPACITY。
     */
    private static int capacity(int expectedMaxSize) {
        // assert expectedMaxSize >= 0;
        return
            (expectedMaxSize > MAXIMUM_CAPACITY / 3) ? MAXIMUM_CAPACITY :
            (expectedMaxSize <= 2 * MINIMUM_CAPACITY / 3) ? MINIMUM_CAPACITY :
            Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1));
    }

    /**
     * 初始化对象为空映射，具有指定的初始容量，该容量假定为2的幂，介于
     * MINIMUM_CAPACITY 和 MAXIMUM_CAPACITY 之间（包括）。
     */
    private void init(int initCapacity) {
        // assert (initCapacity & -initCapacity) == initCapacity; // power of 2
        // assert initCapacity >= MINIMUM_CAPACITY;
        // assert initCapacity <= MAXIMUM_CAPACITY;

        table = new Object[2 * initCapacity];
    }

    /**
     * 构造一个新的标识哈希映射，包含指定映射中的键值映射。
     *
     * @param m 要放置到此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null
     */
    public IdentityHashMap(Map<? extends K, ? extends V> m) {
        // 允许一些增长
        this((int) ((1 + m.size()) * 1.1));
        putAll(m);
    }

    /**
     * 返回此标识哈希映射中的键值映射数量。
     *
     * @return 此映射中的键值映射数量
     */
    public int size() {
        return size;
    }

    /**
     * 如果此标识哈希映射不包含任何键值映射，则返回 <tt>true</tt>。
     *
     * @return 如果此标识哈希映射不包含任何键值映射，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回 Object x 的索引。
     */
    private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        // 乘以 -127，并左移以使用最低位作为哈希的一部分
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    /**
     * 循环遍历大小为 len 的表。
     */
    private static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键
     * {@code k} 到值 {@code v} 的映射，使得 {@code (key == k)}，
     * 则此方法返回 {@code v}；否则返回
     * {@code null}。 （最多只能有一个这样的映射。）
     *
     * <p>返回值为 {@code null} 并不<i>一定</i>
     * 表示映射不包含该键的映射；也有可能映射显式地将键映射到 {@code null}。
     * 可以使用 {@link #containsKey containsKey} 操作来区分这两种情况。
     *
     * @see #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return (V) tab[i + 1];
            if (item == null)
                return null;
            i = nextKeyIndex(i, len);
        }
    }


    /**
     * 测试指定的对象引用是否是此身份哈希映射中的键。
     *
     * @param   key   可能的键
     * @return  <code>true</code> 如果指定的对象引用是此映射中的键
     * @see     #containsValue(Object)
     */
    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return true;
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * 测试指定的对象引用是否是此身份哈希映射中的值。
     *
     * @param value 要测试其在映射中是否存在引用的值
     * @return <tt>true</tt> 如果此映射将一个或多个键映射到指定的对象引用
     * @see     #containsKey(Object)
     */
    public boolean containsValue(Object value) {
        Object[] tab = table;
        for (int i = 1; i < tab.length; i += 2)
            if (tab[i] == value && tab[i - 1] != null)
                return true;

        return false;
    }

    /**
     * 测试指定的键值映射是否在映射中。
     *
     * @param   key   可能的键
     * @param   value 可能的值
     * @return  <code>true</code> 如果且仅如果指定的键值映射在映射中
     */
    private boolean containsMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k)
                return tab[i + 1] == value;
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * 将指定的值与指定的键关联到此身份哈希映射中。如果映射之前包含键的映射，则旧值将被替换。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与 <tt>key</tt> 关联的前一个值，如果没有 <tt>key</tt> 的映射，则返回
     *         <tt>null</tt>。（<tt>null</tt> 返回值也可以表示映射之前将 <tt>null</tt> 与 <tt>key</tt> 关联。）
     * @see     Object#equals(Object)
     * @see     #get(Object)
     * @see     #containsKey(Object)
     */
    public V put(K key, V value) {
        final Object k = maskNull(key);

        retryAfterResize: for (;;) {
            final Object[] tab = table;
            final int len = tab.length;
            int i = hash(k, len);

            for (Object item; (item = tab[i]) != null;
                 i = nextKeyIndex(i, len)) {
                if (item == k) {
                    @SuppressWarnings("unchecked")
                        V oldValue = (V) tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                }
            }

            final int s = size + 1;
            // 使用 3 * s 的优化形式。
            // 下一个容量是 len，当前容量的两倍。
            if (s + (s << 1) > len && resize(len))
                continue retryAfterResize;

            modCount++;
            tab[i] = k;
            tab[i + 1] = value;
            size = s;
            return null;
        }
    }

    /**
     * 如果必要，调整表的大小以容纳给定的容量。
     *
     * @param newCapacity 新的容量，必须是2的幂。
     * @return 是否确实进行了调整大小
     */
    private boolean resize(int newCapacity) {
        // assert (newCapacity & -newCapacity) == newCapacity; // 2的幂
        int newLength = newCapacity * 2;

        Object[] oldTable = table;
        int oldLength = oldTable.length;
        if (oldLength == 2 * MAXIMUM_CAPACITY) { // 无法进一步扩展
            if (size == MAXIMUM_CAPACITY - 1)
                throw new IllegalStateException("Capacity exhausted.");
            return false;
        }
        if (oldLength >= newLength)
            return false;

        Object[] newTable = new Object[newLength];

        for (int j = 0; j < oldLength; j += 2) {
            Object key = oldTable[j];
            if (key != null) {
                Object value = oldTable[j+1];
                oldTable[j] = null;
                oldTable[j+1] = null;
                int i = hash(key, newLength);
                while (newTable[i] != null)
                    i = nextKeyIndex(i, newLength);
                newTable[i] = key;
                newTable[i + 1] = value;
            }
        }
        table = newTable;
        return true;
    }

    /**
     * 将指定映射中的所有映射复制到此映射中。
     * 这些映射将替换此映射中任何当前在指定映射中的键的映射。
     *
     * @param m 要存储在此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        int n = m.size();
        if (n == 0)
            return;
        if (n > size)
            resize(capacity(n)); // 保守地预先扩展

        for (Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * 如果存在，则从此映射中移除此键的映射。
     *
     * @param key 要从此映射中移除映射的键
     * @return 与 <tt>key</tt> 关联的前一个值，如果没有 <tt>key</tt> 的映射，则返回
     *         <tt>null</tt>。（<tt>null</tt> 返回值也可以表示映射之前将 <tt>null</tt> 与 <tt>key</tt> 关联。）
     */
    public V remove(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);


                    while (true) {
            Object item = tab[i];
            if (item == k) {
                modCount++;
                size--;
                @SuppressWarnings("unchecked")
                    V oldValue = (V) tab[i + 1];
                tab[i + 1] = null;
                tab[i] = null;
                closeDeletion(i);
                return oldValue;
            }
            if (item == null)
                return null;
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * 从映射中移除指定的键值对（如果存在）。
     *
     * @param   key   可能的键
     * @param   value 可能的值
     * @return  如果且仅当指定的键值对在映射中存在时返回 <code>true</code>
     */
    private boolean removeMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);

        while (true) {
            Object item = tab[i];
            if (item == k) {
                if (tab[i + 1] != value)
                    return false;
                modCount++;
                size--;
                tab[i] = null;
                tab[i + 1] = null;
                closeDeletion(i);
                return true;
            }
            if (item == null)
                return false;
            i = nextKeyIndex(i, len);
        }
    }

    /**
     * 在删除后重新哈希所有可能冲突的条目。这保留了 get、put 等方法所需的线性探测冲突属性。
     *
     * @param d 新空删除槽的索引
     */
    private void closeDeletion(int d) {
        // 从 Knuth 第 6.4 节算法 R 适应而来
        Object[] tab = table;
        int len = tab.length;

        // 从删除后立即跟随的索引开始查找要交换到新空槽的项目，
        // 并继续直到看到一个空槽，表示可能冲突的键的结束。
        Object item;
        for (int i = nextKeyIndex(d, len); (item = tab[i]) != null;
             i = nextKeyIndex(i, len) ) {
            // 以下测试如果索引 i 处的项目（哈希到槽 r）应该占据 d 空出的槽。
            // 如果是这样，我们将其交换进来，然后继续在新空出的 i 处进行 d。此过程将在我们到达此运行的空槽时终止。
            // 测试很复杂，因为我们使用的是循环表。
            int r = hash(item, len);
            if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {
                tab[d] = item;
                tab[d + 1] = tab[i + 1];
                tab[i] = null;
                tab[i + 1] = null;
                d = i;
            }
        }
    }

    /**
     * 从此映射中移除所有映射。此调用返回后，映射将为空。
     */
    public void clear() {
        modCount++;
        Object[] tab = table;
        for (int i = 0; i < tab.length; i++)
            tab[i] = null;
        size = 0;
    }

    /**
     * 将指定的对象与此映射进行相等性比较。如果给定的对象也是一个映射，并且两个映射表示相同的对象引用映射，则返回 <tt>true</tt>。
     * 更正式地说，当且仅当 <tt>this.entrySet().equals(m.entrySet())</tt> 时，此映射等于另一个映射 <tt>m</tt>。
     *
     * <p><b>由于此映射基于引用相等性，如果此映射与普通映射进行比较，可能会违反 <tt>Object.equals</tt> 合约的对称性和传递性要求。
     * 但是，<tt>IdentityHashMap</tt> 实例之间的 <tt>Object.equals</tt> 合约是保证的。</b>
     *
     * @param  o 要与此映射进行相等性比较的对象
     * @return 如果指定的对象等于此映射，则返回 <tt>true</tt>
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof IdentityHashMap) {
            IdentityHashMap<?,?> m = (IdentityHashMap<?,?>) o;
            if (m.size() != size)
                return false;

            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i+=2) {
                Object k = tab[i];
                if (k != null && !containsMapping(k, tab[i + 1]))
                    return false;
            }
            return true;
        } else if (o instanceof Map) {
            Map<?,?> m = (Map<?,?>)o;
            return entrySet().equals(m.entrySet());
        } else {
            return false;  // o 不是 Map
        }
    }

    /**
     * 返回此映射的哈希码值。映射的哈希码定义为映射的 <tt>entrySet()</tt> 视图中每个条目的哈希码之和。
     * 这确保了对于任何两个 <tt>IdentityHashMap</tt> 实例 <tt>m1</tt> 和 <tt>m2</tt>，<tt>m1.equals(m2)</tt>
     * 意味着 <tt>m1.hashCode()==m2.hashCode()</tt>，这是由 {@link Object#hashCode} 的一般合约要求的。
     *
     * <p><b>由于此映射的 <tt>entrySet</tt> 方法返回的集合中的 <tt>Map.Entry</tt> 实例基于引用相等性，
     * 如果两个被比较的对象之一是 <tt>IdentityHashMap</tt> 实例，另一个是普通映射，可能会违反 <tt>Object.hashCode</tt>
     * 合约中提到的合约要求。</b>
     *
     * @return 此映射的哈希码值
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        int result = 0;
        Object[] tab = table;
        for (int i = 0; i < tab.length; i +=2) {
            Object key = tab[i];
            if (key != null) {
                Object k = unmaskNull(key);
                result += System.identityHashCode(k) ^
                          System.identityHashCode(tab[i + 1]);
            }
        }
        return result;
    }


                /**
     * 返回此身份哈希映射的浅拷贝：键和值本身不会被克隆。
     *
     * @return 此映射的浅拷贝
     */
    public Object clone() {
        try {
            IdentityHashMap<?,?> m = (IdentityHashMap<?,?>) super.clone();
            m.entrySet = null;
            m.table = table.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private abstract class IdentityHashMapIterator<T> implements Iterator<T> {
        int index = (size != 0 ? 0 : table.length); // 当前槽位。
        int expectedModCount = modCount; // 支持快速失败
        int lastReturnedIndex = -1;      // 用于移除
        boolean indexValid; // 避免不必要的 next 计算
        Object[] traversalTable = table; // 引用主表或副本

        public boolean hasNext() {
            Object[] tab = traversalTable;
            for (int i = index; i < tab.length; i+=2) {
                Object key = tab[i];
                if (key != null) {
                    index = i;
                    return indexValid = true;
                }
            }
            index = tab.length;
            return false;
        }

        protected int nextIndex() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (!indexValid && !hasNext())
                throw new NoSuchElementException();

            indexValid = false;
            lastReturnedIndex = index;
            index += 2;
            return lastReturnedIndex;
        }

        public void remove() {
            if (lastReturnedIndex == -1)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            expectedModCount = ++modCount;
            int deletedSlot = lastReturnedIndex;
            lastReturnedIndex = -1;
            // 回退索引以重新访问删除后的内容
            index = deletedSlot;
            indexValid = false;

            // 删除代码的执行类似于 closeDeletion，但必须捕获罕见的情况，即已经看到的元素被交换到稍后由此迭代器遍历的空槽中。我们不能允许未来的 next() 调用再次返回它。在 2/3 负载因子下，这种情况发生的可能性非常小，但当它确实发生时，我们必须为剩余的遍历制作表的副本。由于这通常发生在表的末尾附近，即使在这些罕见的情况下，这在时间和空间上也不会非常昂贵。

            Object[] tab = traversalTable;
            int len = tab.length;

            int d = deletedSlot;
            Object key = tab[d];
            tab[d] = null;        // 空出槽位
            tab[d + 1] = null;

            // 如果遍历的是副本，则在实际表中删除。
            // 我们可以跳过副本中的空隙闭合。
            if (tab != IdentityHashMap.this.table) {
                IdentityHashMap.this.remove(key);
                expectedModCount = modCount;
                return;
            }

            size--;

            Object item;
            for (int i = nextKeyIndex(d, len); (item = tab[i]) != null;
                 i = nextKeyIndex(i, len)) {
                int r = hash(item, len);
                // 请参阅 closeDeletion 以了解此条件的解释
                if ((i < r && (r <= d || d <= i)) ||
                    (r <= d && d <= i)) {

                    // 如果我们即将将已经看到的元素交换到可能由 next() 返回的槽位中，
                    // 那么为未来的 next() 调用复制表的其余部分。我们的副本在“错误”的地方有一个空隙是可以的，因为无论如何它都不会用于搜索。

                    if (i < deletedSlot && d >= deletedSlot &&
                        traversalTable == IdentityHashMap.this.table) {
                        int remaining = len - deletedSlot;
                        Object[] newTable = new Object[remaining];
                        System.arraycopy(tab, deletedSlot,
                                         newTable, 0, remaining);
                        traversalTable = newTable;
                        index = 0;
                    }

                    tab[d] = item;
                    tab[d + 1] = tab[i + 1];
                    tab[i] = null;
                    tab[i + 1] = null;
                    d = i;
                }
            }
        }
    }

    private class KeyIterator extends IdentityHashMapIterator<K> {
        @SuppressWarnings("unchecked")
        public K next() {
            return (K) unmaskNull(traversalTable[nextIndex()]);
        }
    }

    private class ValueIterator extends IdentityHashMapIterator<V> {
        @SuppressWarnings("unchecked")
        public V next() {
            return (V) traversalTable[nextIndex() + 1];
        }
    }

    private class EntryIterator
        extends IdentityHashMapIterator<Map.Entry<K,V>>
    {
        private Entry lastReturnedEntry;

        public Map.Entry<K,V> next() {
            lastReturnedEntry = new Entry(nextIndex());
            return lastReturnedEntry;
        }

        public void remove() {
            lastReturnedIndex =
                ((null == lastReturnedEntry) ? -1 : lastReturnedEntry.index);
            super.remove();
            lastReturnedEntry.index = lastReturnedIndex;
            lastReturnedEntry = null;
        }

        private class Entry implements Map.Entry<K,V> {
            private int index;

            private Entry(int index) {
                this.index = index;
            }

            @SuppressWarnings("unchecked")
            public K getKey() {
                checkIndexForEntryUse();
                return (K) unmaskNull(traversalTable[index]);
            }


                        @SuppressWarnings("unchecked")
            public V getValue() {
                checkIndexForEntryUse();
                return (V) traversalTable[index+1];
            }

            @SuppressWarnings("unchecked")
            public V setValue(V value) {
                checkIndexForEntryUse();
                V oldValue = (V) traversalTable[index+1];
                traversalTable[index+1] = value;
                // 如果存在影子副本，强制写入主表
                if (traversalTable != IdentityHashMap.this.table)
                    put((K) traversalTable[index], value);
                return oldValue;
            }

            public boolean equals(Object o) {
                if (index < 0)
                    return super.equals(o);

                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                return (e.getKey() == unmaskNull(traversalTable[index]) &&
                       e.getValue() == traversalTable[index+1]);
            }

            public int hashCode() {
                if (lastReturnedIndex < 0)
                    return super.hashCode();

                return (System.identityHashCode(unmaskNull(traversalTable[index])) ^
                       System.identityHashCode(traversalTable[index+1]));
            }

            public String toString() {
                if (index < 0)
                    return super.toString();

                return (unmaskNull(traversalTable[index]) + "="
                        + traversalTable[index+1]);
            }

            private void checkIndexForEntryUse() {
                if (index < 0)
                    throw new IllegalStateException("条目已被移除");
            }
        }
    }

    // 视图

    /**
     * 该字段在第一次请求此视图时初始化，包含一个条目集视图的实例。该视图是无状态的，
     * 因此没有理由创建多个实例。
     */
    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * 返回此映射中包含的键的基于身份的集合视图。该集合由映射支持，因此对映射的更改会反映在集合中，
     * 反之亦然。如果在迭代集合时修改映射，则迭代的结果是未定义的。该集合支持元素删除，
     * 通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、
     * <tt>retainAll</tt> 和 <tt>clear</tt> 方法删除映射中的相应映射。它不支持 <tt>add</tt>
     * 或 <tt>addAll</tt> 方法。
     *
     * <p><b>虽然此方法返回的对象实现了 <tt>Set</tt> 接口，但它并不遵守 <tt>Set</tt> 的通用契约。
     * 与支持它的映射一样，此方法返回的集合将元素相等性定义为引用相等性，而不是对象相等性。
     * 这会影响其 <tt>contains</tt>、<tt>remove</tt>、<tt>containsAll</tt>、
     * <tt>equals</tt> 和 <tt>hashCode</tt> 方法的行为。</b>
     *
     * <p><b>返回的集合的 <tt>equals</tt> 方法仅在指定对象是一个包含与返回集合完全相同的对象引用的集合时返回 <tt>true</tt>。
     * 如果将此方法返回的集合与普通集合进行比较，可能会违反 <tt>Object.equals</tt> 合约的对称性和传递性要求。
     * 但是，此方法返回的集合之间的 <tt>Object.equals</tt> 合约是保证的。</b>
     *
     * <p>返回的集合的 <tt>hashCode</tt> 方法返回集合中元素的 <i>身份哈希码</i> 之和，而不是它们的哈希码之和。
     * 这是由 <tt>equals</tt> 方法语义的改变所决定的，以强制执行此方法返回的集合之间的 <tt>Object.hashCode</tt> 方法的通用合约。
     *
     * @return 此映射中包含的键的基于身份的集合视图
     * @see Object#equals(Object)
     * @see System#identityHashCode(Object)
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            int oldSize = size;
            IdentityHashMap.this.remove(o);
            return size != oldSize;
        }
        /*
         * 必须从 AbstractSet 的实现恢复到 AbstractCollection 的实现，因为前者包含一个优化，
         * 当 c 是一个较小的“普通”（非基于身份的）集合时，会导致不正确的行为。
         */
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            for (Iterator<K> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        public int hashCode() {
            int result = 0;
            for (K key : this)
                result += System.identityHashCode(key);
            return result;
        }
        public Object[] toArray() {
            return toArray(new Object[0]);
        }
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key;
                if ((key = tab[si]) != null) { // 键存在？
                    // 元素比预期多 -> 其他线程并发修改
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) unmaskNull(key); // 取消掩码键
                }
            }
            // 元素比预期少或检测到其他线程并发修改
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // 按照规范添加最终的 null 标记
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }


                    public Spliterator<K> spliterator() {
            return new KeySpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * 返回一个包含此映射中所有值的 {@link Collection} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射，
     * 则迭代的结果是不确定的。该集合支持元素删除，这会从映射中删除相应的映射，通过
     * <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、
     * <tt>retainAll</tt> 和 <tt>clear</tt> 方法。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 方法。
     *
     * <p><b>虽然此方法返回的对象实现了 <tt>Collection</tt> 接口，但它并不遵循 <tt>Collection</tt> 的通用契约。
     * 与支持它的映射一样，此方法返回的集合将元素相等性定义为引用相等性而不是对象相等性。这会影响其
     * <tt>contains</tt>、<tt>remove</tt> 和 <tt>containsAll</tt> 方法的行为。</b>
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public boolean remove(Object o) {
            for (Iterator<V> i = iterator(); i.hasNext(); ) {
                if (i.next() == o) {
                    i.remove();
                    return true;
                }
            }
            return false;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        public Object[] toArray() {
            return toArray(new Object[0]);
        }
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                if (tab[si] != null) { // key present ?
                    // more elements than expected -> concurrent modification from other thread
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) tab[si+1]; // copy value
                }
            }
            // fewer elements than expected or concurrent modification from other thread detected
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // final null marker as per spec
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * 返回一个包含此映射中所有映射的 {@link Set} 视图。
     * 返回的集合中的每个元素都是基于引用相等性的 <tt>Map.Entry</tt>。该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。
     * 如果在迭代集合时修改了映射，则迭代的结果是不确定的。该集合支持元素删除，这会从映射中删除相应的映射，通过
     * <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt>
     * 方法。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 方法。
     *
     * <p>与支持它的映射一样，此方法返回的集合中的 <tt>Map.Entry</tt> 对象将键和值的相等性定义为引用相等性而不是对象相等性。这影响了这些
     * <tt>Map.Entry</tt> 对象的 <tt>equals</tt> 和 <tt>hashCode</tt> 方法的行为。一个基于引用相等性的 <tt>Map.Entry e</tt>
     * 与对象 <tt>o</tt> 相等当且仅当 <tt>o</tt> 是一个 <tt>Map.Entry</tt> 并且 <tt>e.getKey()==o.getKey() &amp;&amp;
     * e.getValue()==o.getValue()</tt>。为了适应这些相等性语义，<tt>hashCode</tt> 方法返回
     * <tt>System.identityHashCode(e.getKey()) ^
     * System.identityHashCode(e.getValue())</tt>。
     *
     * <p><b>由于此方法返回的集合中的 <tt>Map.Entry</tt> 实例基于引用相等性语义，如果集合中的任何条目与普通的映射条目进行比较，或者
     * 如果此方法返回的集合与普通的映射条目集合（如在普通映射上调用此方法返回的集合）进行比较，可能会违反 {@link Object#equals(Object)}
     * 合约的对称性和传递性要求。然而，<tt>Object.equals</tt> 合约在基于身份的映射条目之间以及这些条目的集合之间是保证成立的。</b>
     *
     * @return 一个包含此映射中所有基于身份的映射的集合视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        if (es != null)
            return es;
        else
            return entrySet = new EntrySet();
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            return containsMapping(entry.getKey(), entry.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            return removeMapping(entry.getKey(), entry.getValue());
        }
        public int size() {
            return size;
        }
        public void clear() {
            IdentityHashMap.this.clear();
        }
        /*
         * 必须从 AbstractSet 的实现恢复到 AbstractCollection 的实现，因为前者的优化会导致在 c 是一个较小的“普通”（非基于身份的）集合时行为不正确。
         */
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            for (Iterator<Map.Entry<K,V>> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }


                    public Object[] toArray() {
            return toArray(new Object[0]);
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int expectedModCount = modCount;
            int size = size();
            if (a.length < size)
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            Object[] tab = table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key;
                if ((key = tab[si]) != null) { // key present ?
                    // more elements than expected -> concurrent modification from other thread
                    if (ti >= size) {
                        throw new ConcurrentModificationException();
                    }
                    a[ti++] = (T) new AbstractMap.SimpleEntry<>(unmaskNull(key), tab[si + 1]);
                }
            }
            // fewer elements than expected or concurrent modification from other thread detected
            if (ti < size || expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            // final null marker as per spec
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(IdentityHashMap.this, 0, -1, 0, 0);
        }
    }


    private static final long serialVersionUID = 8188218128353913216L;

    /**
     * 将 <tt>IdentityHashMap</tt> 实例的状态保存到流中（即序列化它）。
     *
     * @serialData HashMap 的 <i>大小</i>（键值映射的数量）（<tt>int</tt>），后跟每个由 IdentityHashMap 表示的键值映射的键（Object）和值（Object）。
     *          键值映射的顺序没有特定的顺序。
     */
    private void writeObject(ObjectOutputStream s)
        throws java.io.IOException  {
        // 写出大小（映射数量）和任何隐藏的数据
        s.defaultWriteObject();

        // 再次写出大小（为了向后兼容）
        s.writeInt(size);

        // 写出键和值（交替）
        Object[] tab = table;
        for (int i = 0; i < tab.length; i += 2) {
            Object key = tab[i];
            if (key != null) {
                s.writeObject(unmaskNull(key));
                s.writeObject(tab[i + 1]);
            }
        }
    }

    /**
     * 从流中重新构建 <tt>IdentityHashMap</tt> 实例（即反序列化它）。
     */
    private void readObject(ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException  {
        // 大小（映射数量）写入流两次
        // 读取第一个大小值并忽略它
        s.readFields();

        // 读取第二个大小值，验证并赋值给 size 字段
        int size = s.readInt();
        if (size < 0)
            throw new java.io.StreamCorruptedException
                ("非法的映射数量: " + size);
        int cap = capacity(size);
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, cap*2);
        this.size = size;
        init(cap);

        // 读取键和值，并将映射放入表中
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    /**
     * 用于 readObject 的 put 方法。它不会调整表的大小，更新 modCount 等。
     */
    private void putForCreate(K key, V value)
        throws java.io.StreamCorruptedException
    {
        Object k = maskNull(key);
        Object[] tab = table;
        int len = tab.length;
        int i = hash(k, len);

        Object item;
        while ( (item = tab[i]) != null) {
            if (item == k)
                throw new java.io.StreamCorruptedException();
            i = nextKeyIndex(i, len);
        }
        tab[i] = k;
        tab[i + 1] = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Object[] t = table;
        for (int index = 0; index < t.length; index += 2) {
            Object k = t[index];
            if (k != null) {
                action.accept((K) unmaskNull(k), (V) t[index + 1]);
            }

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Object[] t = table;
        for (int index = 0; index < t.length; index += 2) {
            Object k = t[index];
            if (k != null) {
                t[index + 1] = function.apply((K) unmaskNull(k), (V) t[index + 1]);
            }

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 与基于数组的 Spliterators 类似，但跳过空白元素，并估计每次拆分时大小减半。
     */
    static class IdentityHashMapSpliterator<K,V> {
        final IdentityHashMap<K,V> map;
        int index;             // 当前索引，前进/拆分时修改
        int fence;             // 直到首次使用时为 -1；然后为最后一个索引加一
        int est;               // 大小估计
        int expectedModCount;  // 在设置围栏时初始化

        IdentityHashMapSpliterator(IdentityHashMap<K,V> map, int origin,
                                   int fence, int est, int expectedModCount) {
            this.map = map;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }


                    final int getFence() { // 在首次使用时初始化fence和size
            int hi;
            if ((hi = fence) < 0) {
                est = map.size;
                expectedModCount = map.modCount;
                hi = fence = map.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // 强制初始化
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(IdentityHashMap<K,V> map, int origin, int fence, int est,
                       int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new KeySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc; Object key;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    if ((key = a[i]) != null)
                        action.accept((K)unmaskNull(key));
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                index += 2;
                if (key != null) {
                    action.accept((K)unmaskNull(key));
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0) | Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(IdentityHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new ValueSpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    if (a[i] != null) {
                        @SuppressWarnings("unchecked") V v = (V)a[i+1];
                        action.accept(v);
                    }
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                @SuppressWarnings("unchecked") V v = (V)a[index+1];
                index += 2;
                if (key != null) {
                    action.accept(v);
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0);
        }

    }

    static final class EntrySpliterator<K,V>
        extends IdentityHashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(IdentityHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = ((lo + hi) >>> 1) & ~1;
            return (lo >= mid) ? null :
                new EntrySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            int i, hi, mc;
            IdentityHashMap<K,V> m; Object[] a;
            if ((m = map) != null && (a = m.table) != null &&
                (i = index) >= 0 && (index = hi = getFence()) <= a.length) {
                for (; i < hi; i += 2) {
                    Object key = a[i];
                    if (key != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K)unmaskNull(key);
                        @SuppressWarnings("unchecked") V v = (V)a[i+1];
                        action.accept
                            (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));


                                }
                }
                if (m.modCount == expectedModCount)
                    return;
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null)
                throw new NullPointerException();
            Object[] a = map.table;
            int hi = getFence();
            while (index < hi) {
                Object key = a[index];
                @SuppressWarnings("unchecked") V v = (V)a[index+1];
                index += 2;
                if (key != null) {
                    @SuppressWarnings("unchecked") K k =
                        (K)unmaskNull(key);
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));
                    if (map.modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? SIZED : 0) | Spliterator.DISTINCT;
        }
    }

}
