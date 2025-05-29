
/*
 * 版权所有 (c) 2003, 2012, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Map.Entry;
import sun.misc.SharedSecrets;

/**
 * 一个专门用于枚举类型键的 {@link Map} 实现。枚举映射中的所有键都必须来自在创建映射时显式或隐式指定的单个枚举类型。枚举映射在内部以数组形式表示。这种表示非常紧凑和高效。
 *
 * <p>枚举映射按其键的<i>自然顺序</i>（枚举常量声明的顺序）维护。这反映在集合视图（{@link #keySet()}、{@link #entrySet()} 和 {@link #values()}）返回的迭代器中。
 *
 * <p>集合视图返回的迭代器是<i>弱一致的</i>：它们永远不会抛出 {@link ConcurrentModificationException}，并且它们可能或可能不会显示在迭代进行期间对映射所做的任何修改的效果。
 *
 * <p>不允许使用空键。尝试插入空键将抛出 {@link NullPointerException}。然而，尝试测试空键的存在或移除空键将正常工作。允许使用空值。
 *
 * <P>像大多数集合实现一样，<tt>EnumMap</tt> 不是同步的。如果多个线程同时访问枚举映射，并且至少有一个线程修改映射，应从外部进行同步。这通常通过在自然封装枚举映射的对象上进行同步来实现。如果不存在这样的对象，应使用 {@link Collections#synchronizedMap} 方法“包装”映射。最好在创建时完成此操作，以防止意外的未同步访问：
 *
 * <pre>
 *     Map&lt;EnumKey, V&gt; m
 *         = Collections.synchronizedMap(new EnumMap&lt;EnumKey, V&gt;(...));
 * </pre>
 *
 * <p>实现说明：所有基本操作都在常数时间内执行。它们可能（但不保证）比 {@link HashMap} 的对应操作更快。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author Josh Bloch
 * @see EnumSet
 * @since 1.5
 */
public class EnumMap<K extends Enum<K>, V> extends AbstractMap<K, V>
    implements java.io.Serializable, Cloneable
{
    /**
     * 此映射中所有键的枚举类型的 <tt>Class</tt> 对象。
     *
     * @serial
     */
    private final Class<K> keyType;

    /**
     * 组成 K 的所有值。（为了性能而缓存。）
     */
    private transient K[] keyUniverse;

    /**
     * 此映射的数组表示。第 i 个元素是 universe[i] 当前映射到的值，如果未映射到任何值，则为 null，如果映射到 null，则为 NULL。
     */
    private transient Object[] vals;

    /**
     * 此映射中的映射数。
     */
    private transient int size = 0;

    /**
     * 用于表示非空值的特殊值。
     */
    private static final Object NULL = new Object() {
        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "java.util.EnumMap.NULL";
        }
    };

    private Object maskNull(Object value) {
        return (value == null ? NULL : value);
    }

    @SuppressWarnings("unchecked")
    private V unmaskNull(Object value) {
        return (V)(value == NULL ? null : value);
    }

    private static final Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum<?>[0];

    /**
     * 使用指定的键类型创建一个空的枚举映射。
     *
     * @param keyType 此枚举映射的键类型的类对象
     * @throws NullPointerException 如果 <tt>keyType</tt> 为 null
     */
    public EnumMap(Class<K> keyType) {
        this.keyType = keyType;
        keyUniverse = getKeyUniverse(keyType);
        vals = new Object[keyUniverse.length];
    }

    /**
     * 创建一个与指定的枚举映射具有相同键类型的枚举映射，最初包含相同的映射（如果有）。
     *
     * @param m 用于初始化此枚举映射的枚举映射
     * @throws NullPointerException 如果 <tt>m</tt> 为 null
     */
    public EnumMap(EnumMap<K, ? extends V> m) {
        keyType = m.keyType;
        keyUniverse = m.keyUniverse;
        vals = m.vals.clone();
        size = m.size;
    }

    /**
     * 从指定的映射初始化一个枚举映射。如果指定的映射是一个 <tt>EnumMap</tt> 实例，此构造函数的行为与 {@link #EnumMap(EnumMap)} 相同。否则，指定的映射必须包含至少一个映射（以确定新枚举映射的键类型）。
     *
     * @param m 用于初始化此枚举映射的映射
     * @throws IllegalArgumentException 如果 <tt>m</tt> 不是 <tt>EnumMap</tt> 实例且不包含任何映射
     * @throws NullPointerException 如果 <tt>m</tt> 为 null
     */
    public EnumMap(Map<K, ? extends V> m) {
        if (m instanceof EnumMap) {
            EnumMap<K, ? extends V> em = (EnumMap<K, ? extends V>) m;
            keyType = em.keyType;
            keyUniverse = em.keyUniverse;
            vals = em.vals.clone();
            size = em.size;
        } else {
            if (m.isEmpty())
                throw new IllegalArgumentException("指定的映射为空");
            keyType = m.keySet().iterator().next().getDeclaringClass();
            keyUniverse = getKeyUniverse(keyType);
            vals = new Object[keyUniverse.length];
            putAll(m);
        }
    }

    // 查询操作

    /**
     * 返回此映射中的键值映射数。
     *
     * @return 此映射中的键值映射数
     */
    public int size() {
        return size;
    }

    /**
     * 如果此映射将一个或多个键映射到指定的值，则返回 <tt>true</tt>。
     *
     * @param value 要测试其在映射中的存在的值
     * @return 如果此映射将一个或多个键映射到此值，则返回 <tt>true</tt>
     */
    public boolean containsValue(Object value) {
        value = maskNull(value);


                    for (Object val : vals)
            if (value.equals(val))
                return true;

        return false;
    }

    /**
     * 如果此映射包含指定键的映射，则返回 <tt>true</tt>。
     *
     * @param key 要测试其是否存在于此映射中的键
     * @return 如果此映射包含指定键的映射，则返回 <tt>true</tt>
     */
    public boolean containsKey(Object key) {
        return isValidKey(key) && vals[((Enum<?>)key).ordinal()] != null;
    }

    private boolean containsMapping(Object key, Object value) {
        return isValidKey(key) &&
            maskNull(value).equals(vals[((Enum<?>)key).ordinal()]);
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得 {@code (key == k)}，
     * 则此方法返回 {@code v}；否则返回 {@code null}。 （最多只能有一个这样的映射。）
     *
     * <p>返回值为 {@code null} 并不<i>一定</i>表示映射中没有该键的映射；也可能是映射显式地将该键映射到 {@code null}。
     * 可以使用 {@link #containsKey containsKey} 操作来区分这两种情况。
     */
    public V get(Object key) {
        return (isValidKey(key) ?
                unmaskNull(vals[((Enum<?>)key).ordinal()]) : null);
    }

    // 修改操作

    /**
     * 将指定的值与此映射中的指定键关联。如果此映射之前包含此键的映射，则旧值将被替换。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     *
     * @return 与指定键关联的先前值，如果此键没有映射，则返回 <tt>null</tt>。 （返回 <tt>null</tt> 也表示此映射之前将 <tt>null</tt> 与指定键关联。）
     * @throws NullPointerException 如果指定的键为 null
     */
    public V put(K key, V value) {
        typeCheck(key);

        int index = key.ordinal();
        Object oldValue = vals[index];
        vals[index] = maskNull(value);
        if (oldValue == null)
            size++;
        return unmaskNull(oldValue);
    }

    /**
     * 如果存在，则从此映射中移除此键的映射。
     *
     * @param key 要从映射中移除映射的键
     * @return 与指定键关联的先前值，如果此键没有条目，则返回 <tt>null</tt>。 （返回 <tt>null</tt> 也表示此映射之前将 <tt>null</tt> 与指定键关联。）
     */
    public V remove(Object key) {
        if (!isValidKey(key))
            return null;
        int index = ((Enum<?>)key).ordinal();
        Object oldValue = vals[index];
        vals[index] = null;
        if (oldValue != null)
            size--;
        return unmaskNull(oldValue);
    }

    private boolean removeMapping(Object key, Object value) {
        if (!isValidKey(key))
            return false;
        int index = ((Enum<?>)key).ordinal();
        if (maskNull(value).equals(vals[index])) {
            vals[index] = null;
            size--;
            return true;
        }
        return false;
    }

    /**
     * 如果键是此枚举映射中的合适键类型，则返回 true。
     */
    private boolean isValidKey(Object key) {
        if (key == null)
            return false;

        // 比较 Enum 后跟 getDeclaringClass 更便宜
        Class<?> keyClass = key.getClass();
        return keyClass == keyType || keyClass.getSuperclass() == keyType;
    }

    // 批量操作

    /**
     * 将指定映射中的所有映射复制到此映射中。这些映射将替换此映射中当前在指定映射中的任何键的任何映射。
     *
     * @param m 要存储在此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null，或者指定的映射中的一个或多个键为 null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m instanceof EnumMap) {
            EnumMap<?, ?> em = (EnumMap<?, ?>)m;
            if (em.keyType != keyType) {
                if (em.isEmpty())
                    return;
                throw new ClassCastException(em.keyType + " != " + keyType);
            }

            for (int i = 0; i < keyUniverse.length; i++) {
                Object emValue = em.vals[i];
                if (emValue != null) {
                    if (vals[i] == null)
                        size++;
                    vals[i] = emValue;
                }
            }
        } else {
            super.putAll(m);
        }
    }

    /**
     * 从此映射中移除所有映射。
     */
    public void clear() {
        Arrays.fill(vals, null);
        size = 0;
    }

    // 视图

    /**
     * 此字段在第一次请求此视图时初始化，包含一个条目集视图的实例。该视图是无状态的，因此没有理由创建多个实例。
     */
    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * 返回此映射中包含的键的 {@link Set} 视图。返回的集合遵循在 {@link Map#keySet()} 中概述的一般约定。集合的迭代器将按自然顺序（枚举常量声明的顺序）返回键。
     *
     * @return 此枚举映射中包含的键的集合视图
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
            EnumMap.this.remove(o);
            return size != oldSize;
        }
        public void clear() {
            EnumMap.this.clear();
        }
    }

                /**
     * 返回一个包含此映射中所有值的 {@link Collection} 视图。
     * 返回的集合遵循在 {@link Map#values()} 中概述的一般契约。集合的迭代器将按其对应键在映射中出现的顺序返回值，
     * 这是它们的自然顺序（枚举常量声明的顺序）。
     *
     * @return 包含此映射中所有值的集合视图
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
            o = maskNull(o);

            for (int i = 0; i < vals.length; i++) {
                if (o.equals(vals[i])) {
                    vals[i] = null;
                    size--;
                    return true;
                }
            }
            return false;
        }
        public void clear() {
            EnumMap.this.clear();
        }
    }

    /**
     * 返回一个包含此映射中所有映射的 {@link Set} 视图。
     * 返回的集合遵循在 {@link Map#keySet()} 中概述的一般契约。集合的迭代器将按其键在映射中出现的顺序返回映射，
     * 这是它们的自然顺序（枚举常量声明的顺序）。
     *
     * @return 包含此枚举映射中所有映射的集合视图
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
            EnumMap.this.clear();
        }
        public Object[] toArray() {
            return fillEntryArray(new Object[size]);
        }
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array
                    .newInstance(a.getClass().getComponentType(), size);
            if (a.length > size)
                a[size] = null;
            return (T[]) fillEntryArray(a);
        }
        private Object[] fillEntryArray(Object[] a) {
            int j = 0;
            for (int i = 0; i < vals.length; i++)
                if (vals[i] != null)
                    a[j++] = new AbstractMap.SimpleEntry<>(
                        keyUniverse[i], unmaskNull(vals[i]));
            return a;
        }
    }

    private abstract class EnumMapIterator<T> implements Iterator<T> {
        // 下一个要返回的元素的索引下限
        int index = 0;

        // 最后返回的元素的索引，如果没有则为 -1
        int lastReturnedIndex = -1;

        public boolean hasNext() {
            while (index < vals.length && vals[index] == null)
                index++;
            return index != vals.length;
        }

        public void remove() {
            checkLastReturnedIndex();

            if (vals[lastReturnedIndex] != null) {
                vals[lastReturnedIndex] = null;
                size--;
            }
            lastReturnedIndex = -1;
        }

        private void checkLastReturnedIndex() {
            if (lastReturnedIndex < 0)
                throw new IllegalStateException();
        }
    }

    private class KeyIterator extends EnumMapIterator<K> {
        public K next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturnedIndex = index++;
            return keyUniverse[lastReturnedIndex];
        }
    }

    private class ValueIterator extends EnumMapIterator<V> {
        public V next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturnedIndex = index++;
            return unmaskNull(vals[lastReturnedIndex]);
        }
    }

    private class EntryIterator extends EnumMapIterator<Map.Entry<K,V>> {
        private Entry lastReturnedEntry;

        public Map.Entry<K,V> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturnedEntry = new Entry(index++);
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

            public K getKey() {
                checkIndexForEntryUse();
                return keyUniverse[index];
            }

            public V getValue() {
                checkIndexForEntryUse();
                return unmaskNull(vals[index]);
            }

            public V setValue(V value) {
                checkIndexForEntryUse();
                V oldValue = unmaskNull(vals[index]);
                vals[index] = maskNull(value);
                return oldValue;
            }

            private void checkIndexForEntryUse() {
                if (index < 0 || index >= vals.length)
                    throw new IllegalStateException();
            }
        }
    }


                        public boolean equals(Object o) {
                if (index < 0)
                    return o == this;

                if (!(o instanceof Map.Entry))
                    return false;

                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                V ourValue = unmaskNull(vals[index]);
                Object hisValue = e.getValue();
                return (e.getKey() == keyUniverse[index] &&
                        (ourValue == hisValue ||
                         (ourValue != null && ourValue.equals(hisValue))));
            }

            public int hashCode() {
                if (index < 0)
                    return super.hashCode();

                return entryHashCode(index);
            }

            public String toString() {
                if (index < 0)
                    return super.toString();

                return keyUniverse[index] + "="
                    + unmaskNull(vals[index]);
            }

            private void checkIndexForEntryUse() {
                if (index < 0)
                    throw new IllegalStateException("Entry was removed");
            }
        }
    }

    // 比较和哈希

    /**
     * 将指定对象与此映射进行比较以确定相等性。如果给定对象也是一个映射，并且两个映射表示相同的映射关系，则返回
     * <tt>true</tt>，具体如 {@link Map#equals(Object)} 合约中所述。
     *
     * @param o 要与此映射进行相等性比较的对象
     * @return 如果指定对象与此映射相等，则返回 <tt>true</tt>
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof EnumMap)
            return equals((EnumMap<?,?>)o);
        if (!(o instanceof Map))
            return false;

        Map<?,?> m = (Map<?,?>)o;
        if (size != m.size())
            return false;

        for (int i = 0; i < keyUniverse.length; i++) {
            if (null != vals[i]) {
                K key = keyUniverse[i];
                V value = unmaskNull(vals[i]);
                if (null == value) {
                    if (!((null == m.get(key)) && m.containsKey(key)))
                       return false;
                } else {
                   if (!value.equals(m.get(key)))
                      return false;
                }
            }
        }

        return true;
    }

    private boolean equals(EnumMap<?,?> em) {
        if (em.keyType != keyType)
            return size == 0 && em.size == 0;

        // 键类型匹配，比较每个值
        for (int i = 0; i < keyUniverse.length; i++) {
            Object ourValue =    vals[i];
            Object hisValue = em.vals[i];
            if (hisValue != ourValue &&
                (hisValue == null || !hisValue.equals(ourValue)))
                return false;
        }
        return true;
    }

    /**
     * 返回此映射的哈希码值。映射的哈希码定义为映射中每个条目的哈希码之和。
     */
    public int hashCode() {
        int h = 0;

        for (int i = 0; i < keyUniverse.length; i++) {
            if (null != vals[i]) {
                h += entryHashCode(i);
            }
        }

        return h;
    }

    private int entryHashCode(int index) {
        return (keyUniverse[index].hashCode() ^ vals[index].hashCode());
    }

    /**
     * 返回此枚举映射的浅拷贝。（值本身不会被克隆。）
     *
     * @return 此枚举映射的浅拷贝
     */
    @SuppressWarnings("unchecked")
    public EnumMap<K, V> clone() {
        EnumMap<K, V> result = null;
        try {
            result = (EnumMap<K, V>) super.clone();
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
        result.vals = result.vals.clone();
        result.entrySet = null;
        return result;
    }

    /**
     * 如果 e 不是此枚举集的正确类型，则抛出异常。
     */
    private void typeCheck(K key) {
        Class<?> keyClass = key.getClass();
        if (keyClass != keyType && keyClass.getSuperclass() != keyType)
            throw new ClassCastException(keyClass + " != " + keyType);
    }

    /**
     * 返回组成 K 的所有值。
     * 结果未克隆，缓存并由所有调用者共享。
     */
    private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
        return SharedSecrets.getJavaLangAccess()
                                        .getEnumConstantsShared(keyType);
    }

    private static final long serialVersionUID = 458661240069192865L;

    /**
     * 将 <tt>EnumMap</tt> 实例的状态保存到流中（即，序列化它）。
     *
     * @serialData 枚举映射的 <i>大小</i>（键值映射的数量）被发出（int），然后是每个键值映射的键（Object）
     *             和值（Object）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException
    {
        // 写出键类型和任何隐藏的内容
        s.defaultWriteObject();

        // 写出大小（映射的数量）
        s.writeInt(size);

        // 写出键和值（交替）
        int entriesToBeWritten = size;
        for (int i = 0; entriesToBeWritten > 0; i++) {
            if (null != vals[i]) {
                s.writeObject(keyUniverse[i]);
                s.writeObject(unmaskNull(vals[i]));
                entriesToBeWritten--;
            }
        }
    }

    /**
     * 从流中重新构建 <tt>EnumMap</tt> 实例（即，反序列化它）。
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException
    {
        // 读取键类型和任何隐藏的内容
        s.defaultReadObject();

        keyUniverse = getKeyUniverse(keyType);
        vals = new Object[keyUniverse.length];

        // 读取大小（映射的数量）
        int size = s.readInt();

        // 读取键和值，并将映射放入 HashMap
        for (int i = 0; i < size; i++) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            put(key, value);
        }
    }
}
