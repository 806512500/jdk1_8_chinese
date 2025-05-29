
/*
 * 版权所有 (c) 1994, 2021，Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.NoSuchFieldException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import sun.misc.SharedSecrets;

/**
 * 该类实现了一个哈希表，将键映射到值。任何非<code>null</code>对象都可以用作键或值。 <p>
 *
 * 要成功地在哈希表中存储和检索对象，用作键的对象必须实现<code>hashCode</code>
 * 方法和<code>equals</code>方法。 <p>
 *
 * <code>Hashtable</code>的实例有两个影响其性能的参数：<i>初始容量</i>和<i>负载因子</i>。 <i>容量</i>是哈希表中的<i>桶</i>的数量，而<i>初始容量</i>只是创建哈希表时的容量。 注意，哈希表是<i>开放的</i>：在“哈希冲突”的情况下，单个桶存储多个条目，这些条目必须按顺序搜索。 <i>负载因子</i>是衡量哈希表允许填充到多满才会自动增加其容量的指标。 初始容量和负载因子参数只是实现的提示。 调用重新散列方法的确切细节（何时以及是否调用）取决于实现。<p>
 *
 * 通常，默认负载因子（.75）在时间和空间成本之间提供了良好的折衷。 较高的值会减少空间开销，但会增加查找条目的时间成本（这反映在大多数<tt>Hashtable</tt>操作中，包括<tt>get</tt>和<tt>put</tt>）。<p>
 *
 * 初始容量控制着空间浪费和<code>rehash</code>操作需求之间的权衡，这些操作耗时较长。
 * 如果初始容量大于哈希表将包含的最大条目数除以其负载因子，则永远不会发生<code>rehash</code>操作。 然而，将初始容量设置得过高会浪费空间。<p>
 *
 * 如果要在<code>Hashtable</code>中插入许多条目，使用足够大的容量创建它可能比让哈希表根据需要自动重新散列以增长表更高效。<p>
 *
 * 以下示例创建了一个数字的哈希表。它使用数字的名称作为键：
 * <pre>   {@code
 *   Hashtable<String, Integer> numbers
 *     = new Hashtable<String, Integer>();
 *   numbers.put("one", 1);
 *   numbers.put("two", 2);
 *   numbers.put("three", 3);}</pre>
 *
 * <p>要检索一个数字，使用以下代码：
 * <pre>   {@code
 *   Integer n = numbers.get("two");
 *   if (n != null) {
 *     System.out.println("two = " + n);
 *   }}</pre>
 *
 * <p>此类的“集合视图方法”返回的集合的<tt>iterator</tt>方法返回的迭代器是
 * <em>快速失败的</em>：如果在迭代器创建后以任何方式对哈希表进行结构修改（除了通过迭代器自己的
 * <tt>remove</tt>方法），迭代器将抛出{@link
 * ConcurrentModificationException}。 因此，面对并发修改时，迭代器会快速且干净地失败，而不是冒险在未来的某个不确定时间发生任意的、非确定性行为。
 * 哈希表的<tt>keys</tt>和<tt>elements</tt>方法返回的枚举不是快速失败的。
 *
 * <p>需要注意的是，迭代器的快速失败行为无法保证
 * 通常情况下，在存在未同步的并发修改的情况下，很难做出任何硬性保证。 快速失败迭代器在尽力的基础上抛出<tt>ConcurrentModificationException</tt>。
 * 因此，编写依赖此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>自 Java 2 平台 v1.2 起，此类被改造以实现{@link Map}接口，使其成为
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 *
 * Java 集合框架</a>的成员。 与新的集合实现不同，{@code Hashtable}是同步的。 如果不需要线程安全的实现，建议使用
 * {@link HashMap}代替{@code Hashtable}。 如果需要高度并发的线程安全实现，则建议使用
 * {@link java.util.concurrent.ConcurrentHashMap}代替
 * {@code Hashtable}。
 *
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Object#equals(java.lang.Object)
 * @see     Object#hashCode()
 * @see     Hashtable#rehash()
 * @see     Collection
 * @see     Map
 * @see     HashMap
 * @see     TreeMap
 * @since JDK1.0
 */
public class Hashtable<K,V>
    extends Dictionary<K,V>
    implements Map<K,V>, Cloneable, java.io.Serializable {

    /**
     * 哈希表数据。
     */
    private transient Entry<?,?>[] table;

    /**
     * 哈希表中的条目总数。
     */
    private transient int count;

    /**
     * 当哈希表的大小超过此阈值时，将重新散列。 （此字段的值为 (int)(capacity * loadFactor)。）
     *
     * @serial
     */
    private int threshold;

    /**
     * 哈希表的负载因子。
     *
     * @serial
     */
    private float loadFactor;

    /**
     * 此 Hashtable 结构修改的次数
     * 结构修改是指改变 Hashtable 中条目数量或以其他方式修改其内部结构（例如，重新散列）的修改。 此字段用于使 Hashtable 的集合视图的迭代器快速失败。 （参见 ConcurrentModificationException）。
     */
    private transient int modCount = 0;


                /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = 1421746759512286392L;

    /**
     * 构造一个新的、空的哈希表，具有指定的初始容量和指定的加载因子。
     *
     * @param      initialCapacity   哈希表的初始容量。
     * @param      loadFactor        哈希表的加载因子。
     * @exception  IllegalArgumentException  如果初始容量小于零，或者加载因子非正。
     */
    public Hashtable(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法容量: "+
                                               initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("非法加载因子: "+loadFactor);

        if (initialCapacity==0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry<?,?>[initialCapacity];
        threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
    }

    /**
     * 构造一个新的、空的哈希表，具有指定的初始容量和默认加载因子（0.75）。
     *
     * @param     initialCapacity   哈希表的初始容量。
     * @exception IllegalArgumentException 如果初始容量小于零。
     */
    public Hashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * 构造一个新的、空的哈希表，具有默认的初始容量（11）和加载因子（0.75）。
     */
    public Hashtable() {
        this(11, 0.75f);
    }

    /**
     * 构造一个新的哈希表，具有与给定 Map 相同的映射。哈希表的初始容量足以容纳给定 Map 中的映射，并具有默认加载因子（0.75）。
     *
     * @param t 要放置在此映射中的映射的 Map。
     * @throws NullPointerException 如果指定的 Map 为 null。
     * @since   1.2
     */
    public Hashtable(Map<? extends K, ? extends V> t) {
        this(Math.max(2*t.size(), 11), 0.75f);
        putAll(t);
    }

    /**
     * 返回此哈希表中的键数。
     *
     * @return  此哈希表中的键数。
     */
    public synchronized int size() {
        return count;
    }

    /**
     * 测试此哈希表是否不映射任何键到值。
     *
     * @return  如果此哈希表不映射任何键到值，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     */
    public synchronized boolean isEmpty() {
        return count == 0;
    }

    /**
     * 返回此哈希表中的键的枚举。
     *
     * @return  此哈希表中的键的枚举。
     * @see     Enumeration
     * @see     #elements()
     * @see     #keySet()
     * @see     Map
     */
    public synchronized Enumeration<K> keys() {
        return this.<K>getEnumeration(KEYS);
    }

    /**
     * 返回此哈希表中的值的枚举。使用返回对象上的枚举方法按顺序获取元素。
     *
     * @return  此哈希表中的值的枚举。
     * @see     java.util.Enumeration
     * @see     #keys()
     * @see     #values()
     * @see     Map
     */
    public synchronized Enumeration<V> elements() {
        return this.<V>getEnumeration(VALUES);
    }

    /**
     * 测试此哈希表中是否有某个键映射到指定的值。此操作比 {@link #containsKey
     * containsKey} 方法更昂贵。
     *
     * <p>请注意，此方法在功能上与 {@link #containsValue containsValue} 相同，
     * （这是集合框架中的 {@link Map} 接口的一部分）。
     *
     * @param      value   要搜索的值
     * @return     如果此哈希表中某个键映射到 <code>value</code> 参数，根据 <tt>equals</tt> 方法确定，则返回 <code>true</code>；
     *             否则返回 <code>false</code>。
     * @exception  NullPointerException  如果值为 <code>null</code>
     */
    public synchronized boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        Entry<?,?> tab[] = table;
        for (int i = tab.length ; i-- > 0 ;) {
            for (Entry<?,?> e = tab[i] ; e != null ; e = e.next) {
                if (e.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果此哈希表将一个或多个键映射到此值，则返回 true。
     *
     * <p>请注意，此方法在功能上与 {@link
     * #contains contains} 相同（后者早于 {@link Map} 接口）。
     *
     * @param value 要测试其在哈希表中是否存在性的值
     * @return <tt>true</tt> 如果此映射将一个或多个键映射到指定的值
     * @throws NullPointerException  如果值为 <code>null</code>
     * @since 1.2
     */
    public boolean containsValue(Object value) {
        return contains(value);
    }

    /**
     * 测试指定的对象是否是此哈希表中的键。
     *
     * @param   key   可能的键
     * @return  如果根据 <tt>equals</tt> 方法确定，指定的对象是此哈希表中的键，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @throws  NullPointerException  如果键为 <code>null</code>
     * @see     #contains(Object)
     */
    public synchronized boolean containsKey(Object key) {
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                return true;
            }
        }
        return false;
    }


                /**
     * 返回与此键关联的值，
     * 如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键
     * {@code k} 到值 {@code v} 的映射，使得 {@code (key.equals(k))}，
     * 则此方法返回 {@code v}；否则返回
     * {@code null}。 （最多只能有一个这样的映射。）
     *
     * @param key 要返回其关联值的键
     * @return 与此键关联的值，或
     *         如果此映射不包含该键的映射，则返回 {@code null}
     * @throws NullPointerException 如果指定的键为 null
     * @see     #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public synchronized V get(Object key) {
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                return (V)e.value;
            }
        }
        return null;
    }

    /**
     * 可分配数组的最大大小。
     * 一些虚拟机在数组中预留了一些头部字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过 VM 限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加此哈希表的容量并重新组织其内部结构，
     * 以便更有效地容纳和访问其条目。当哈希表中的键数
     * 超过此哈希表的容量和加载因子时，此方法将自动调用。
     */
    @SuppressWarnings("unchecked")
    protected void rehash() {
        int oldCapacity = table.length;
        Entry<?,?>[] oldMap = table;

        // 防止溢出的代码
        int newCapacity = (oldCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity == MAX_ARRAY_SIZE)
                // 保持使用 MAX_ARRAY_SIZE 个桶
                return;
            newCapacity = MAX_ARRAY_SIZE;
        }
        Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];

        modCount++;
        threshold = (int)Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry<K,V> old = (Entry<K,V>)oldMap[i] ; old != null ; ) {
                Entry<K,V> e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = (Entry<K,V>)newMap[index];
                newMap[index] = e;
            }
        }
    }

    private void addEntry(int hash, K key, V value, int index) {
        modCount++;

        Entry<?,?> tab[] = table;
        if (count >= threshold) {
            // 如果超过阈值，则重新哈希表
            rehash();

            tab = table;
            hash = key.hashCode();
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        // 创建新条目。
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>) tab[index];
        tab[index] = new Entry<>(hash, key, value, e);
        count++;
    }

    /**
     * 在此哈希表中将指定的 <code>key</code> 映射到指定的
     * <code>value</code>。键和值都不能为 <code>null</code>。 <p>
     *
     * 可以通过调用带有与原始键相等的键的 <code>get</code> 方法来检索值。
     *
     * @param      key     哈希表的键
     * @param      value   值
     * @return     此哈希表中指定键的先前值，
     *             或者如果没有则返回 <code>null</code>
     * @exception  NullPointerException  如果键或值为
     *               <code>null</code>
     * @see     Object#equals(Object)
     * @see     #get(Object)
     */
    public synchronized V put(K key, V value) {
        // 确保值不为 null
        if (value == null) {
            throw new NullPointerException();
        }

        // 确保键不在哈希表中。
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> entry = (Entry<K,V>)tab[index];
        for(; entry != null ; entry = entry.next) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
        }

        addEntry(hash, key, value, index);
        return null;
    }

    /**
     * 从此哈希表中移除键（及其对应的值）。如果键不在哈希表中，此方法不执行任何操作。
     *
     * @param   key   需要移除的键
     * @return  此哈希表中键映射的值，
     *          或者如果键没有映射则返回 <code>null</code>
     * @throws  NullPointerException  如果键为 <code>null</code>
     */
    public synchronized V remove(Object key) {
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for(Entry<K,V> prev = null ; e != null ; prev = e, e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                modCount++;
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }

    /**
     * 将指定映射中的所有映射复制到此哈希表中。
     * 这些映射将替换此哈希表中任何当前在指定映射中的键的映射。
     *
     * @param t 要存储在此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null
     * @since 1.2
     */
    public synchronized void putAll(Map<? extends K, ? extends V> t) {
        for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
            put(e.getKey(), e.getValue());
    }


                /**
     * 清除此哈希表，使其不包含任何键。
     */
    public synchronized void clear() {
        Entry<?,?> tab[] = table;
        modCount++;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }

    /**
     * 创建此哈希表的浅拷贝。哈希表本身的结构被复制，但键和值不会被克隆。
     * 这是一个相对昂贵的操作。
     *
     * @return  哈希表的克隆
     */
    public synchronized Object clone() {
        try {
            Hashtable<?,?> t = (Hashtable<?,?>)super.clone();
            t.table = new Entry<?,?>[table.length];
            for (int i = table.length ; i-- > 0 ; ) {
                t.table[i] = (table[i] != null)
                    ? (Entry<?,?>) table[i].clone() : null;
            }
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            t.modCount = 0;
            return t;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }
    }

    /**
     * 返回此 <tt>Hashtable</tt> 对象的字符串表示形式，形式为一组条目，用大括号括起来，并用 ASCII 字符 "<tt>,&nbsp;</tt>"（逗号和空格）分隔。
     * 每个条目都表示为键、等号 <tt>=</tt> 和关联的元素，其中使用 <tt>toString</tt> 方法将键和元素转换为字符串。
     *
     * @return  此哈希表的字符串表示形式
     */
    public synchronized String toString() {
        int max = size() - 1;
        if (max == -1)
            return "{}";

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<K,V>> it = entrySet().iterator();

        sb.append('{');
        for (int i = 0; ; i++) {
            Map.Entry<K,V> e = it.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key   == this ? "(this Map)" : key.toString());
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value.toString());

            if (i == max)
                return sb.append('}').toString();
            sb.append(", ");
        }
    }


    private <T> Enumeration<T> getEnumeration(int type) {
        if (count == 0) {
            return Collections.emptyEnumeration();
        } else {
            return new Enumerator<>(type, false);
        }
    }

    private <T> Iterator<T> getIterator(int type) {
        if (count == 0) {
            return Collections.emptyIterator();
        } else {
            return new Enumerator<>(type, true);
        }
    }

    // 视图

    /**
     * 这些字段在第一次请求此视图时初始化，包含适当视图的实例。视图是无状态的，因此没有理由创建多个实例。
     */
    private transient volatile Set<K> keySet;
    private transient volatile Set<Map.Entry<K,V>> entrySet;
    private transient volatile Collection<V> values;

    /**
     * 返回此映射中包含的键的 {@link Set} 视图。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改映射（通过迭代器自身的 <tt>remove</tt> 操作除外），迭代的结果是不确定的。
     * 集合支持元素删除，这会通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中删除相应的映射。
     * 它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @since 1.2
     */
    public Set<K> keySet() {
        if (keySet == null)
            keySet = Collections.synchronizedSet(new KeySet(), this);
        return keySet;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return getIterator(KEYS);
        }
        public int size() {
            return count;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            return Hashtable.this.remove(o) != null;
        }
        public void clear() {
            Hashtable.this.clear();
        }
    }

    /**
     * 返回此映射中包含的映射的 {@link Set} 视图。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改映射（通过迭代器自身的 <tt>remove</tt> 操作或通过迭代器返回的映射条目的 <tt>setValue</tt> 操作除外），迭代的结果是不确定的。
     * 集合支持元素删除，这会通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中删除相应的映射。
     * 它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @since 1.2
     */
    public Set<Map.Entry<K,V>> entrySet() {
        if (entrySet==null)
            entrySet = Collections.synchronizedSet(new EntrySet(), this);
        return entrySet;
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return getIterator(ENTRIES);
        }

        public boolean add(Map.Entry<K,V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            Object key = entry.getKey();
            Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;


                        for (Entry<?,?> e = tab[index]; e != null; e = e.next)
                if (e.hash==hash && e.equals(entry))
                    return true;
            return false;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object key = entry.getKey();
            Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;

            @SuppressWarnings("unchecked")
            Entry<K,V> e = (Entry<K,V>)tab[index];
            for(Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
                if (e.hash==hash && e.equals(entry)) {
                    modCount++;
                    if (prev != null)
                        prev.next = e.next;
                    else
                        tab[index] = e.next;

                    count--;
                    e.value = null;
                    return true;
                }
            }
            return false;
        }

        public int size() {
            return count;
        }

        public void clear() {
            Hashtable.this.clear();
        }
    }

    /**
     * 返回一个包含此映射中所有值的 {@link Collection} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （通过迭代器自身的 <tt>remove</tt> 操作除外），迭代的结果是不确定的。该集合支持元素移除，
     * 通过 <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、
     * <tt>retainAll</tt> 和 <tt>clear</tt> 操作移除映射中的相应映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @since 1.2
     */
    public Collection<V> values() {
        if (values==null)
            values = Collections.synchronizedCollection(new ValueCollection(),
                                                        this);
        return values;
    }

    private class ValueCollection extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return getIterator(VALUES);
        }
        public int size() {
            return count;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            Hashtable.this.clear();
        }
    }

    // 比较和哈希

    /**
     * 根据 Map 接口中的定义，将指定的对象与此映射进行相等性比较。
     *
     * @param  o 要与此哈希表进行相等性比较的对象
     * @return 如果指定的对象等于此映射，则返回 true
     * @see Map#equals(Object)
     * @since 1.2
     */
    public synchronized boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?,?> t = (Map<?,?>) o;
        if (t.size() != size())
            return false;

        try {
            Iterator<Map.Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(t.get(key)==null && t.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(t.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    /**
     * 根据 Map 接口中的定义，返回此映射的哈希码值。
     *
     * @see Map#hashCode()
     * @since 1.2
     */
    public synchronized int hashCode() {
        /*
         * 此代码检测由计算自引用哈希表的哈希码引起的递归，并防止由此导致的堆栈溢出。
         * 这允许某些 1.1 时代的具有自引用哈希表的应用程序工作。此代码滥用 loadFactor 字段，
         * 以双重用途作为哈希码计算中的标志，以避免恶化空间性能。负的 load factor 表示哈希码计算正在进行中。
         */
        int h = 0;
        if (count == 0 || loadFactor < 0)
            return h;  // 返回零

        loadFactor = -loadFactor;  // 标记哈希码计算正在进行中
        Entry<?,?>[] tab = table;
        for (Entry<?,?> entry : tab) {
            while (entry != null) {
                h += entry.hashCode();
                entry = entry.next;
            }
        }

        loadFactor = -loadFactor;  // 标记哈希码计算完成

        return h;
    }

    @Override
    public synchronized V getOrDefault(Object key, V defaultValue) {
        V result = get(key);
        return (null == result) ? defaultValue : result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);     // 显式检查，以防表为空。
        final int expectedModCount = modCount;

        Entry<?, ?>[] tab = table;
        for (Entry<?, ?> entry : tab) {
            while (entry != null) {
                action.accept((K)entry.key, (V)entry.value);
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);     // 显式检查，以防表为空。
        final int expectedModCount = modCount;


                    Entry<K, V>[] tab = (Entry<K, V>[])table;
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                entry.value = Objects.requireNonNull(
                    function.apply(entry.key, entry.value));
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @Override
    public synchronized V putIfAbsent(K key, V value) {
        Objects.requireNonNull(value);

        // 确保键不在哈希表中。
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> entry = (Entry<K,V>)tab[index];
        for (; entry != null; entry = entry.next) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                if (old == null) {
                    entry.value = value;
                }
                return old;
            }
        }

        addEntry(hash, key, value, index);
        return null;
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        Objects.requireNonNull(value);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
            if ((e.hash == hash) && e.key.equals(key) && e.value.equals(value)) {
                modCount++;
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;
                e.value = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean replace(K key, V oldValue, V newValue) {
        Objects.requireNonNull(oldValue);
        Objects.requireNonNull(newValue);
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (; e != null; e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                if (e.value.equals(oldValue)) {
                    e.value = newValue;
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized V replace(K key, V value) {
        Objects.requireNonNull(value);
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (; e != null; e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                // 哈希表不接受 null 值
                return e.value;
            }
        }

        V newValue = mappingFunction.apply(key);
        if (newValue != null) {
            addEntry(hash, key, newValue, index);
        }

        return newValue;
    }

    @Override
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
        }
        return null;
    }

    @Override
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash && Objects.equals(e.key, key)) {
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
        }

        V newValue = remappingFunction.apply(key, null);
        if (newValue != null) {
            addEntry(hash, key, newValue, index);
        }


                    return newValue;
    }

    @Override
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                V newValue = remappingFunction.apply(e.value, value);
                if (newValue == null) {
                    modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
        }

        if (value != null) {
            addEntry(hash, key, value, index);
        }

        return value;
    }

    /**
     * 将 Hashtable 的状态保存到流中（即序列化）。
     *
     * @serialData 哈希表的 <i>容量</i>（桶数组的长度）被发出（int），接着是哈希表的 <i>大小</i>（键值映射的数量），然后是哈希表表示的每个键值映射的键（Object）和值（Object）
     *             键值映射的发出顺序没有特定顺序。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        Entry<Object, Object> entryStack = null;

        synchronized (this) {
            // 写出阈值和负载因子
            s.defaultWriteObject();

            // 写出元素的长度和数量
            s.writeInt(table.length);
            s.writeInt(count);

            // 将表中的条目堆栈复制
            for (int index = 0; index < table.length; index++) {
                Entry<?,?> entry = table[index];

                while (entry != null) {
                    entryStack =
                        new Entry<>(0, entry.key, entry.value, entryStack);
                    entry = entry.next;
                }
            }
        }

        // 从堆栈条目中写出键/值对象
        while (entryStack != null) {
            s.writeObject(entryStack.key);
            s.writeObject(entryStack.value);
            entryStack = entryStack.next;
        }
    }

    /**
     * 从流中重建哈希表（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        ObjectInputStream.GetField fields = s.readFields();

        // 读取并验证负载因子（忽略阈值 - 它将被重新计算）
        float lf = fields.get("loadFactor", 0.75f);
        if (lf <= 0 || Float.isNaN(lf))
            throw new StreamCorruptedException("非法负载因子: " + lf);
        lf = Math.min(Math.max(0.25f, lf), 4.0f);

        // 读取数组的原始长度和元素数量
        int origlength = s.readInt();
        int elements = s.readInt();

        // 验证元素数量
        if (elements < 0)
            throw new StreamCorruptedException("非法元素数量: " + elements);

        // 使原始长度大于 elements / loadFactor
        // （这是自动增长时强制执行的不变量）
        origlength = Math.max(origlength, (int)(elements / lf) + 1);

        // 计算新长度，留出 5% + 3 的增长空间，但不超过原始长度。如果长度足够大，使其为奇数，这有助于分布条目。
        // 防止长度最终为零，这是无效的。
        int length = (int)((elements + elements / 20) / lf) + 3;
        if (length > elements && (length & 1) == 0)
            length--;
        length = Math.min(length, origlength);

        if (length < 0) { // 溢出
            length = origlength;
        }

        // 检查 Map.Entry[].class，因为它是我们实际创建的最近的公共类型。
        SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, length);
        Hashtable.UnsafeHolder.putLoadFactor(this, lf);
        table = new Entry<?,?>[length];
        threshold = (int)Math.min(length * lf, MAX_ARRAY_SIZE + 1);
        count = 0;

        // 读取元素数量，然后读取所有键/值对象
        for (; elements > 0; elements--) {
            @SuppressWarnings("unchecked")
                K key = (K)s.readObject();
            @SuppressWarnings("unchecked")
                V value = (V)s.readObject();
            // 为了性能，取消同步
            reconstitutionPut(table, key, value);
        }
    }

    // 支持在反序列化期间重置最终字段
    private static final class UnsafeHolder {
        private UnsafeHolder() { throw new InternalError(); }

        private static final sun.misc.Unsafe unsafe
                = sun.misc.Unsafe.getUnsafe();

        private static final long LF_OFFSET = getLoadFactorOffset();

        static void putLoadFactor(Hashtable<?, ?> table, float lf) {
            unsafe.putFloat(table, LF_OFFSET, lf);
        }

        static long getLoadFactorOffset() {
            try {
                return unsafe.objectFieldOffset(Hashtable.class.getDeclaredField("loadFactor"));
            } catch (NoSuchFieldException e) {
                throw new InternalError(e);
            }
        }
    }

    /**
     * 由 readObject 使用的 put 方法。提供此方法是因为 put 是可重写的，不应在 readObject 中调用，因为子类尚未初始化。
     *
     * <p>此方法与常规 put 方法有几个不同之处。不需要检查重新散列，因为最初表中的元素数量是已知的。modCount 不增加，也没有同步，因为我们正在创建一个新实例。
     * 另外，不需要返回值。
     */
    private void reconstitutionPut(Entry<?,?>[] tab, K key, V value)
        throws StreamCorruptedException
    {
        if (value == null) {
            throw new java.io.StreamCorruptedException();
        }
        // 确保键不在哈希表中。
        // 在反序列化版本中不应发生这种情况。
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                throw new java.io.StreamCorruptedException();
            }
        }
        // 创建新条目。
        @SuppressWarnings("unchecked")
            Entry<K,V> e = (Entry<K,V>)tab[index];
        tab[index] = new Entry<>(hash, key, value, e);
        count++;
    }

                /**
     * 哈希表桶冲突列表条目
     */
    private static class Entry<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Entry<K,V> next;

        protected Entry(int hash, K key, V value, Entry<K,V> next) {
            this.hash = hash;
            this.key =  key;
            this.value = value;
            this.next = next;
        }

        @SuppressWarnings("unchecked")
        protected Object clone() {
            return new Entry<>(hash, key, value,
                                  (next==null ? null : (Entry<K,V>) next.clone()));
        }

        // Map.Entry 操作

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            if (value == null)
                throw new NullPointerException();

            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

            return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
               (value==null ? e.getValue()==null : value.equals(e.getValue()));
        }

        public int hashCode() {
            return hash ^ Objects.hashCode(value);
        }

        public String toString() {
            return key.toString()+"="+value.toString();
        }
    }

    // 枚举/迭代的类型
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    /**
     * 一个哈希表枚举器类。此类实现了 Enumeration 和 Iterator 接口，但可以创建单独的实例来禁用 Iterator 方法。这是必要的，以避免无意中通过传递一个 Enumeration 而增加授予用户的权限。
     */
    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
        Entry<?,?>[] table = Hashtable.this.table;
        int index = table.length;
        Entry<?,?> entry;
        Entry<?,?> lastReturned;
        int type;

        /**
         * 指示此枚举器是否作为 Iterator 或 Enumeration 服务。 (true -> Iterator)。
         */
        boolean iterator;

        /**
         * 迭代器认为支持的哈希表应具有的 modCount 值。如果此期望被违反，迭代器检测到并发修改。
         */
        protected int expectedModCount = modCount;

        Enumerator(int type, boolean iterator) {
            this.type = type;
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            Entry<?,?> e = entry;
            int i = index;
            Entry<?,?>[] t = table;
            /* 使用局部变量以加快循环迭代速度 */
            while (e == null && i > 0) {
                e = t[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        @SuppressWarnings("unchecked")
        public T nextElement() {
            Entry<?,?> et = entry;
            int i = index;
            Entry<?,?>[] t = table;
            /* 使用局部变量以加快循环迭代速度 */
            while (et == null && i > 0) {
                et = t[--i];
            }
            entry = et;
            index = i;
            if (et != null) {
                Entry<?,?> e = lastReturned = entry;
                entry = e.next;
                return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
            }
            throw new NoSuchElementException("Hashtable Enumerator");
        }

        // Iterator 方法
        public boolean hasNext() {
            return hasMoreElements();
        }

        public T next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return nextElement();
        }

        public void remove() {
            if (!iterator)
                throw new UnsupportedOperationException();
            if (lastReturned == null)
                throw new IllegalStateException("Hashtable Enumerator");
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            synchronized(Hashtable.this) {
                Entry<?,?>[] tab = Hashtable.this.table;
                int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

                @SuppressWarnings("unchecked")
                Entry<K,V> e = (Entry<K,V>)tab[index];
                for(Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
                    if (e == lastReturned) {
                        modCount++;
                        expectedModCount++;
                        if (prev == null)
                            tab[index] = e.next;
                        else
                            prev.next = e.next;
                        count--;
                        lastReturned = null;
                        return;
                    }
                }
                throw new ConcurrentModificationException();
            }
        }
    }
}
