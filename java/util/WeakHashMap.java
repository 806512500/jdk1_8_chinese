
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;


/**
 * 基于哈希表实现的 <tt>Map</tt> 接口，具有 <em>弱键</em>。
 * <tt>WeakHashMap</tt> 中的条目在键不再被普通使用时将自动被移除。更具体地说，给定键的映射不会阻止该键被垃圾收集器丢弃，即，被标记为可终结、终结，然后回收。
 * 当一个键被丢弃时，其条目实际上从映射中移除，因此这个类的行为与其他 <tt>Map</tt> 实现有所不同。
 *
 * <p> 既支持 null 值也支持 null 键。这个类的性能特征与 <tt>HashMap</tt> 类类似，并具有相同的效率参数 <em>初始容量</em> 和 <em>加载因子</em>。
 *
 * <p> 像大多数集合类一样，这个类不是同步的。
 * 可以使用 {@link Collections#synchronizedMap Collections.synchronizedMap} 方法构造一个同步的 <tt>WeakHashMap</tt>。
 *
 * <p> 这个类主要用于键对象的 <tt>equals</tt> 方法使用 <tt>==</tt> 运算符测试对象身份的情况。一旦这样的键被丢弃，它就永远无法被重新创建，因此在稍后的时间点在 <tt>WeakHashMap</tt> 中查找该键时，不会惊讶地发现其条目已被移除。
 * 这个类也可以很好地处理 <tt>equals</tt> 方法不基于对象身份的键对象，例如 <tt>String</tt> 实例。但是，对于这样的可重新创建的键对象，<tt>WeakHashMap</tt> 条目的自动移除可能会令人困惑。
 *
 * <p> <tt>WeakHashMap</tt> 类的行为部分取决于垃圾收集器的行为，因此一些熟悉的（尽管不是必需的）<tt>Map</tt> 不变量对于这个类并不成立。因为垃圾收集器可能随时丢弃键，所以 <tt>WeakHashMap</tt> 可能表现得好像有一个未知线程在默默地移除条目。
 * 特别是，即使你对 <tt>WeakHashMap</tt> 实例进行同步并且不调用其任何修改方法，<tt>size</tt> 方法返回的值也可能随时间变小，<tt>isEmpty</tt> 方法可能先返回 <tt>false</tt> 然后返回 <tt>true</tt>，
 * <tt>containsKey</tt> 方法可能先返回 <tt>true</tt> 然后返回 <tt>false</tt>，<tt>get</tt> 方法可能先为给定键返回一个值然后返回 <tt>null</tt>，
 * <tt>put</tt> 方法可能返回 <tt>null</tt>，<tt>remove</tt> 方法可能返回 <tt>false</tt>，对于先前似乎在映射中的键，以及对键集、值集和条目集的连续检查可能产生越来越少的元素。
 *
 * <p> <tt>WeakHashMap</tt> 中的每个键对象都是间接地作为弱引用的引用对象存储的。因此，只有在垃圾收集器清除了指向该键的所有弱引用（包括映射内部和外部的引用）后，键才会被自动移除。
 *
 * <p> <strong>实现说明：</strong> <tt>WeakHashMap</tt> 中的值对象是通过普通的强引用持有的。因此，应谨慎确保值对象不会直接或间接地强引用其键，因为这将阻止键被丢弃。
 * 注意，值对象可以通过 <tt>WeakHashMap</tt> 本身间接引用其键；也就是说，一个值对象可以强引用另一个键对象，而该键对象的关联值对象反过来又强引用第一个值对象的键。
 * 如果映射中的值不依赖于映射对它们的强引用，一种处理方法是在插入之前将值本身包装在 <tt>WeakReference</tt> 中，如 <tt>m.put(key, new WeakReference(value))</tt>，
 * 然后在每次 <tt>get</tt> 时解包。
 *
 * <p> 由这个类的所有“集合视图方法”返回的集合的 <tt>iterator</tt> 方法返回的迭代器是 <i>快速失败</i> 的：如果在迭代器创建后以任何方式对映射进行结构修改（除了通过迭代器自己的 <tt>remove</tt> 方法），
 * 迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，迭代器会快速且干净地失败，而不是冒险在未来的某个不确定时间点出现任意的、不确定的行为。
 *
 * <p> 应注意，迭代器的快速失败行为不能保证，因为通常不可能在存在未同步的并发修改的情况下做出任何硬性保证。快速失败的迭代器在尽力的基础上抛出 <tt>ConcurrentModificationException</tt>。
 * 因此，编写依赖于此异常正确性的程序是错误的：迭代器的快速失败行为应仅用于检测错误。
 *
 * <p> 这个类是 <a href="{@docRoot}/../technotes/guides/collections/index.html">Java Collections Framework</a> 的成员。
 *
 * @param <K> 由这个映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author      Doug Lea
 * @author      Josh Bloch
 * @author      Mark Reinhold
 * @since       1.2
 * @see         java.util.HashMap
 * @see         java.lang.ref.WeakReference
 */
public class WeakHashMap<K,V>
    extends AbstractMap<K,V>
    implements Map<K,V> {


                /**
     * 默认初始容量 -- 必须是2的幂。
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * 最大容量，如果构造函数的参数隐式指定了更高的值，则使用此值。
     * 必须是 <= 1<<30 的2的幂。
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 当构造函数中未指定时使用的负载因子。
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 表，根据需要调整大小。长度必须始终是2的幂。
     */
    Entry<K,V>[] table;

    /**
     * 此弱哈希映射中包含的键值映射的数量。
     */
    private int size;

    /**
     * 下一个调整大小的大小值（容量 * 负载因子）。
     */
    private int threshold;

    /**
     * 哈希表的负载因子。
     */
    private final float loadFactor;

    /**
     * 已清除的WeakEntries的引用队列。
     */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    /**
     * 此WeakHashMap结构修改的次数。
     * 结构修改是指改变映射数量或以其他方式修改其内部结构
     * （例如，重新哈希）。此字段用于使映射的集合视图上的迭代器快速失败。
     *
     * @see ConcurrentModificationException
     */
    int modCount;

    @SuppressWarnings("unchecked")
    private Entry<K,V>[] newTable(int n) {
        return (Entry<K,V>[]) new Entry<?,?>[n];
    }

    /**
     * 构造一个新的、空的 <tt>WeakHashMap</tt>，具有给定的初始容量和负载因子。
     *
     * @param  initialCapacity <tt>WeakHashMap</tt> 的初始容量
     * @param  loadFactor      <tt>WeakHashMap</tt> 的负载因子
     * @throws IllegalArgumentException 如果初始容量为负数，或负载因子为非正数。
     */
    public WeakHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法初始容量: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("非法负载因子: " +
                                               loadFactor);
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;
        table = newTable(capacity);
        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
    }

    /**
     * 构造一个新的、空的 <tt>WeakHashMap</tt>，具有给定的初始容量和默认负载因子（0.75）。
     *
     * @param  initialCapacity <tt>WeakHashMap</tt> 的初始容量
     * @throws IllegalArgumentException 如果初始容量为负数
     */
    public WeakHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造一个新的、空的 <tt>WeakHashMap</tt>，具有默认初始容量（16）和负载因子（0.75）。
     */
    public WeakHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造一个新的 <tt>WeakHashMap</tt>，具有与指定映射相同的映射。 <tt>WeakHashMap</tt>
     * 使用默认负载因子（0.75）和初始容量，足以容纳指定映射中的映射。
     *
     * @param   m 要放置在此映射中的映射
     * @throws  NullPointerException 如果指定的映射为 null
     * @since   1.3
     */
    public WeakHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                DEFAULT_INITIAL_CAPACITY),
             DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    // 内部工具

    /**
     * 表内表示 null 键的值。
     */
    private static final Object NULL_KEY = new Object();

    /**
     * 如果键为 null，则使用 NULL_KEY。
     */
    private static Object maskNull(Object key) {
        return (key == null) ? NULL_KEY : key;
    }

    /**
     * 将内部表示的 null 键返回给调用者为 null。
     */
    static Object unmaskNull(Object key) {
        return (key == NULL_KEY) ? null : key;
    }

    /**
     * 检查非 null 引用 x 和可能为 null 的 y 是否相等。默认使用 Object.equals。
     */
    private static boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    /**
     * 获取对象的哈希码，并对结果哈希应用补充哈希函数，以防御低质量的哈希函数。这是关键的，因为 HashMap 使用2的幂长度的哈希表，否则对于在低位不同的哈希码会遇到碰撞。
     */
    final int hash(Object k) {
        int h = k.hashCode();

        // 此函数确保仅在每个位位置上相差常数倍的哈希码具有有限的碰撞次数（在默认负载因子下大约为8次）。
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * 返回哈希码 h 的索引。
     */
    private static int indexFor(int h, int length) {
        return h & (length-1);
    }

    /**
     * 从表中清除过时的条目。
     */
    private void expungeStaleEntries() {
        for (Object x; (x = queue.poll()) != null; ) {
            synchronized (queue) {
                @SuppressWarnings("unchecked")
                    Entry<K,V> e = (Entry<K,V>) x;
                int i = indexFor(e.hash, table.length);

                Entry<K,V> prev = table[i];
                Entry<K,V> p = prev;
                while (p != null) {
                    Entry<K,V> next = p.next;
                    if (p == e) {
                        if (prev == e)
                            table[i] = next;
                        else
                            prev.next = next;
                        // 不得将 e.next 置为 null；
                        // 过时的条目可能正在被 HashIterator 使用
                        e.value = null; // 帮助垃圾回收
                        size--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }


                /**
     * 返回在清除陈旧条目后的表。
     */
    private Entry<K,V>[] getTable() {
        expungeStaleEntries();
        return table;
    }

    /**
     * 返回此映射中的键值映射数量。
     * 此结果是一个快照，可能不会反映在下次访问前因不再被引用而将被移除的未处理条目。
     */
    public int size() {
        if (size == 0)
            return 0;
        expungeStaleEntries();
        return size;
    }

    /**
     * 如果此映射不包含键值映射，则返回 <tt>true</tt>。
     * 此结果是一个快照，可能不会反映在下次访问前因不再被引用而将被移除的未处理条目。
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得 {@code (key==null ? k==null :
     * key.equals(k))}，则此方法返回 {@code v}；否则返回 {@code null}。 （最多只能有一个这样的映射。）
     *
     * <p>返回值为 {@code null} 并不 <i>一定</i> 表示映射中没有该键的映射；也可能是映射显式地将该键映射到 {@code null}。
     * 可以使用 {@link #containsKey containsKey} 操作来区分这两种情况。
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K,V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K,V> e = tab[index];
        while (e != null) {
            if (e.hash == h && eq(k, e.get()))
                return e.value;
            e = e.next;
        }
        return null;
    }

    /**
     * 如果此映射包含指定键的映射，则返回 <tt>true</tt>。
     *
     * @param  key   要测试其在映射中是否存在性的键
     * @return <tt>true</tt> 如果有 <tt>key</tt> 的映射；<tt>false</tt> 否则
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * 返回与此映射中指定键关联的条目。如果映射中没有此键的映射，则返回 null。
     */
    Entry<K,V> getEntry(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K,V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K,V> e = tab[index];
        while (e != null && !(e.hash == h && eq(k, e.get())))
            e = e.next;
        return e;
    }

    /**
     * 将指定值与此映射中的指定键关联。如果映射之前包含此键的映射，则旧值将被替换。
     *
     * @param key 要与指定值关联的键。
     * @param value 要与指定键关联的值。
     * @return 之前与 <tt>key</tt> 关联的值，或 <tt>null</tt> 如果没有 <tt>key</tt> 的映射。
     *         （返回 <tt>null</tt> 也可能表示映射之前将 <tt>null</tt> 与 <tt>key</tt> 关联。）
     */
    public V put(K key, V value) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K,V>[] tab = getTable();
        int i = indexFor(h, tab.length);

        for (Entry<K,V> e = tab[i]; e != null; e = e.next) {
            if (h == e.hash && eq(k, e.get())) {
                V oldValue = e.value;
                if (value != oldValue)
                    e.value = value;
                return oldValue;
            }
        }

        modCount++;
        Entry<K,V> e = tab[i];
        tab[i] = new Entry<>(k, value, queue, h, e);
        if (++size >= threshold)
            resize(tab.length * 2);
        return null;
    }

    /**
     * 将此映射的内容重新散列到一个具有更大容量的新数组中。当此映射中的键数量达到其阈值时，此方法将自动调用。
     *
     * 如果当前容量为 MAXIMUM_CAPACITY，此方法不会调整映射的大小，而是将阈值设置为 Integer.MAX_VALUE。
     * 这样可以防止未来的调用。
     *
     * @param newCapacity 新容量，必须是 2 的幂；必须大于当前容量，除非当前容量为 MAXIMUM_CAPACITY（在这种情况下，值无关紧要）。
     */
    void resize(int newCapacity) {
        Entry<K,V>[] oldTable = getTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry<K,V>[] newTable = newTable(newCapacity);
        transfer(oldTable, newTable);
        table = newTable;

        /*
         * 如果忽略 null 元素和处理引用队列导致大量收缩，则恢复旧表。这种情况应很少见，但可以避免垃圾填充表的无限扩展。
         */
        if (size >= threshold / 2) {
            threshold = (int)(newCapacity * loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table = oldTable;
        }
    }

    /** 将所有条目从 src 转移到 dest 表 */
    private void transfer(Entry<K,V>[] src, Entry<K,V>[] dest) {
        for (int j = 0; j < src.length; ++j) {
            Entry<K,V> e = src[j];
            src[j] = null;
            while (e != null) {
                Entry<K,V> next = e.next;
                Object key = e.get();
                if (key == null) {
                    e.next = null;  // 帮助垃圾回收
                    e.value = null; //  "   "
                    size--;
                } else {
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }


                /**
     * 将指定映射中的所有映射复制到此映射中。
     * 这些映射将替换此映射中当前存在于指定映射中的任何键的映射。
     *
     * @param m 要存储在此映射中的映射。
     * @throws  NullPointerException 如果指定的映射为 null。
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        /*
         * 如果要添加的映射数大于或等于阈值，则扩展映射。这是保守的；明显的条件是 (m.size() + size) >= threshold，但此条件可能导致映射的容量是适当容量的两倍，
         * 如果要添加的键与此映射中已存在的键重叠。通过使用保守的计算，我们最多只会进行一次额外的调整大小。
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * 从此弱哈希映射中移除键的映射（如果存在）。
     * 更正式地说，如果此映射包含从键 <tt>k</tt> 到值 <tt>v</tt> 的映射，使得 <code>(key==null ?  k==null :
     * key.equals(k))</code>，则移除该映射。（映射中最多只能包含一个这样的映射。）
     *
     * <p>返回此映射之前与键关联的值，如果映射中没有键的映射，则返回 <tt>null</tt>。返回值为 <tt>null</tt> 并不 <i>一定</i> 表示
     * 映射中没有键的映射；也有可能映射显式地将键映射到 <tt>null</tt>。
     *
     * <p>调用返回后，映射将不再包含指定键的映射。
     *
     * @param key 要从映射中移除的键
     * @return 之前与 <tt>key</tt> 关联的值，或者如果映射中没有 <tt>key</tt> 的映射，则返回 <tt>null</tt>
     */
    public V remove(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K,V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K,V> prev = tab[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (h == e.hash && eq(k, e.get())) {
                modCount++;
                size--;
                if (prev == e)
                    tab[i] = next;
                else
                    prev.next = next;
                return e.value;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    /** Entry 集合所需的特殊版本的 remove 方法 */
    boolean removeMapping(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Entry<K,V>[] tab = getTable();
        Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
        Object k = maskNull(entry.getKey());
        int h = hash(k);
        int i = indexFor(h, tab.length);
        Entry<K,V> prev = tab[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (h == e.hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    tab[i] = next;
                else
                    prev.next = next;
                return true;
            }
            prev = e;
            e = next;
        }

        return false;
    }

    /**
     * 从此映射中移除所有映射。
     * 调用返回后，映射将为空。
     */
    public void clear() {
        // 清除引用队列。由于表将被清除，因此不需要清除条目。
        while (queue.poll() != null)
            ;

        modCount++;
        Arrays.fill(table, null);
        size = 0;

        // 数组的分配可能导致 GC，这可能导致其他条目过期。从引用队列中移除这些条目将使它们有资格被回收。
        while (queue.poll() != null)
            ;
    }

    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>。
     *
     * @param value 要测试在此映射中是否存在该值
     * @return 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>
     */
    public boolean containsValue(Object value) {
        if (value==null)
            return containsNullValue();

        Entry<K,V>[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
            for (Entry<K,V> e = tab[i]; e != null; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * 带 null 参数的 containsValue 的特殊情况代码
     */
    private boolean containsNullValue() {
        Entry<K,V>[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
            for (Entry<K,V> e = tab[i]; e != null; e = e.next)
                if (e.value==null)
                    return true;
        return false;
    }

    /**
     * 此哈希表中的条目扩展了 WeakReference，使用其主引用字段作为键。
     */
    private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V> {
        V value;
        final int hash;
        Entry<K,V> next;

        /**
         * 创建新的条目。
         */
        Entry(Object key, V value,
              ReferenceQueue<Object> queue,
              int hash, Entry<K,V> next) {
            super(key, queue);
            this.value = value;
            this.hash  = hash;
            this.next  = next;
        }


                    @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) WeakHashMap.unmaskNull(get());
        }

        public V getValue() {
            return value;
        }

        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            K k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                V v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public int hashCode() {
            K k = getKey();
            V v = getValue();
            return Objects.hashCode(k) ^ Objects.hashCode(v);
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract class HashIterator<T> implements Iterator<T> {
        private int index;
        private Entry<K,V> entry;
        private Entry<K,V> lastReturned;
        private int expectedModCount = modCount;

        /**
         * 需要强引用以避免在 hasNext 和 next 之间键消失
         */
        private Object nextKey;

        /**
         * 需要强引用以避免在 nextEntry() 和使用条目之间键消失
         */
        private Object currentKey;

        HashIterator() {
            index = isEmpty() ? 0 : table.length;
        }

        public boolean hasNext() {
            Entry<K,V>[] t = table;

            while (nextKey == null) {
                Entry<K,V> e = entry;
                int i = index;
                while (e == null && i > 0)
                    e = t[--i];
                entry = e;
                index = i;
                if (e == null) {
                    currentKey = null;
                    return false;
                }
                nextKey = e.get(); // 保持强引用以防止键消失
                if (nextKey == null)
                    entry = entry.next;
            }
            return true;
        }

        /** 不同类型的迭代器中 next() 的公共部分 */
        protected Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextKey == null && !hasNext())
                throw new NoSuchElementException();

            lastReturned = entry;
            entry = entry.next;
            currentKey = nextKey;
            nextKey = null;
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            WeakHashMap.this.remove(currentKey);
            expectedModCount = modCount;
            lastReturned = null;
            currentKey = null;
        }

    }

    private class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends HashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    // 视图

    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * 返回此映射中包含的键的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射
     * （除了通过迭代器自身的 <tt>remove</tt> 操作），迭代的结果是未定义的。该集合支持元素删除，
     * 通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt>
     * 和 <tt>clear</tt> 操作删除映射中的相应映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
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
            return WeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            if (containsKey(o)) {
                WeakHashMap.this.remove(o);
                return true;
            }
            else
                return false;
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator<>(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * 返回此映射中包含的值的 {@link Collection} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射
     * （除了通过迭代器自身的 <tt>remove</tt> 操作），迭代的结果是未定义的。该集合支持元素删除，
     * 通过 <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt>
     * 和 <tt>clear</tt> 操作删除映射中的相应映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }


    /**
     * 返回此映射中包含的映射项的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射（通过迭代器自身的 <tt>remove</tt> 操作或通过迭代器返回的映射项的 <tt>setValue</tt> 操作除外），迭代的结果是未定义的。该集合支持元素移除，这将从映射中移除相应的映射项，通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            Entry<K,V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o);
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        private List<Map.Entry<K,V>> deepCopy() {
            List<Map.Entry<K,V>> list = new ArrayList<>(size());
            for (Map.Entry<K,V> e : this)
                list.add(new AbstractMap.SimpleEntry<>(e));
            return list;
        }

        public Object[] toArray() {
            return deepCopy().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return deepCopy().toArray(a);
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    action.accept((K)WeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();;
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    entry.value = function.apply((K)WeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * 与其他哈希 Spliterator 类似，但跳过无效的元素。
     */
    static class WeakHashMapSpliterator<K,V> {
        final WeakHashMap<K,V> map;
        WeakHashMap.Entry<K,V> current; // 当前节点
        int index;             // 当前索引，在前进/拆分时修改
        int fence;             // -1 直到首次使用；然后是一次最后一个索引
        int est;               // 大小估计
        int expectedModCount;  // 用于一致性检查

        WeakHashMapSpliterator(WeakHashMap<K,V> m, int origin,
                            int fence, int est,
                            int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // 在首次使用时初始化 fence 和大小
            int hi;
            if ((hi = fence) < 0) {
                WeakHashMap<K,V> m = map;
                est = m.size();
                expectedModCount = m.modCount;
                hi = fence = m.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // 强制初始化
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends WeakHashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                    int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new KeySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }
    }


    public void forEachRemaining(Consumer<? super K> action) {
        int i, hi, mc;
        if (action == null)
            throw new NullPointerException();
        WeakHashMap<K,V> m = map;
        WeakHashMap.Entry<K,V>[] tab = m.table;
        if ((hi = fence) < 0) {
            mc = expectedModCount = m.modCount;
            hi = fence = tab.length;
        }
        else
            mc = expectedModCount;
        if (tab.length >= hi && (i = index) >= 0 &&
            (i < (index = hi) || current != null)) {
            WeakHashMap.Entry<K,V> p = current;
            current = null; // 用尽
            do {
                if (p == null)
                    p = tab[i++];
                else {
                    Object x = p.get();
                    p = p.next;
                    if (x != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K) WeakHashMap.unmaskNull(x);
                        action.accept(k);
                    }
                }
            } while (p != null || i < hi);
        }
        if (m.modCount != mc)
            throw new ConcurrentModificationException();
    }

    public boolean tryAdvance(Consumer<? super K> action) {
        int hi;
        if (action == null)
            throw new NullPointerException();
        WeakHashMap.Entry<K,V>[] tab = map.table;
        if (tab.length >= (hi = getFence()) && index >= 0) {
            while (current != null || index < hi) {
                if (current == null)
                    current = tab[index++];
                else {
                    Object x = current.get();
                    current = current.next;
                    if (x != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K) WeakHashMap.unmaskNull(x);
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int characteristics() {
        return Spliterator.DISTINCT;
    }
    }

    static final class ValueSpliterator<K,V>
        extends WeakHashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new ValueSpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            WeakHashMap<K,V> m = map;
            WeakHashMap.Entry<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                (i < (index = hi) || current != null)) {
                WeakHashMap.Entry<K,V> p = current;
                current = null; // 用尽
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        V v = p.value;
                        p = p.next;
                        if (x != null)
                            action.accept(v);
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            WeakHashMap.Entry<K,V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        V v = current.value;
                        current = current.next;
                        if (x != null) {
                            action.accept(v);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 0;
        }
    }

static final class EntrySpliterator<K,V>
    extends WeakHashMapSpliterator<K,V>
    implements Spliterator<Map.Entry<K,V>> {
    EntrySpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                    int expectedModCount) {
        super(m, origin, fence, est, expectedModCount);
    }

    public EntrySpliterator<K,V> trySplit() {
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null :
            new EntrySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
    }


    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        int i, hi, mc;
        if (action == null)
            throw new NullPointerException();
        WeakHashMap<K,V> m = map;
        WeakHashMap.Entry<K,V>[] tab = m.table;
        if ((hi = fence) < 0) {
            mc = expectedModCount = m.modCount;
            hi = fence = tab.length;
        }
        else
            mc = expectedModCount;
        if (tab.length >= hi && (i = index) >= 0 &&
            (i < (index = hi) || current != null)) {
            WeakHashMap.Entry<K,V> p = current;
            current = null; // 用尽
            do {
                if (p == null)
                    p = tab[i++];
                else {
                    Object x = p.get();
                    V v = p.value;
                    p = p.next;
                    if (x != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K) WeakHashMap.unmaskNull(x);
                        action.accept
                            (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));
                    }
                }
            } while (p != null || i < hi);
        }
        if (m.modCount != mc)
            throw new ConcurrentModificationException();
    }


    public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
        int hi;
        if (action == null)
            throw new NullPointerException();
        WeakHashMap.Entry<K,V>[] tab = map.table;
        if (tab.length >= (hi = getFence()) && index >= 0) {
            while (current != null || index < hi) {
                if (current == null)
                    current = tab[index++];
                else {
                    Object x = current.get();
                    V v = current.value;
                    current = current.next;
                    if ( x != null) {
                        @SuppressWarnings("unchecked") K k =
                            (K) WeakHashMap.unmaskNull(x);
                        action.accept
                            (new AbstractMap.SimpleImmutableEntry<K,V>(k, v));
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int characteristics() {
        return Spliterator.DISTINCT;
    }
}

