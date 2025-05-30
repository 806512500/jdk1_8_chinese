
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.io.IOException;

/**
 * <p>哈希表和链表实现的 {@code Map} 接口，具有可预测的迭代顺序。此实现与 {@code HashMap} 不同，因为它维护一个贯穿所有条目的双向链表。这个链表定义了迭代顺序，通常是键插入到映射中的顺序（<i>插入顺序</i>）。注意，如果键被<i>重新插入</i>到映射中，插入顺序不会受到影响。（当调用 {@code m.put(k, v)} 时，如果 {@code m.containsKey(k)} 会立即返回 {@code true}，则认为键 {@code k} 被重新插入到映射 {@code m} 中。）
 *
 * <p>此实现避免了 {@link HashMap}（和 {@link Hashtable}）提供的未指定的、通常混乱的顺序，而不会增加与 {@link TreeMap} 相关的成本。它可以用于生成一个具有与原始映射相同顺序的映射副本，无论原始映射的实现如何：
 * <pre>
 *     void foo(Map m) {
 *         Map copy = new LinkedHashMap(m);
 *         ...
 *     }
 * </pre>
 * 这种技术特别有用，如果一个模块在输入时接受一个映射，复制它，并在稍后返回结果，其顺序由副本的顺序决定。（客户通常希望以相同的顺序返回结果。）
 *
 * <p>提供了一个特殊的 {@link #LinkedHashMap(int,float,boolean) 构造函数}，用于创建一个迭代顺序为最后访问顺序（从最近最少访问到最近最多访问<i>访问顺序</i>）的链式哈希映射。这种映射非常适合构建 LRU 缓存。调用 {@code put}、{@code putIfAbsent}、{@code get}、{@code getOrDefault}、{@code compute}、{@code computeIfAbsent}、{@code computeIfPresent} 或 {@code merge} 方法会导致对相应条目的访问（假设在调用完成之后条目存在）。只有当值被替换时，{@code replace} 方法才会导致条目的访问。{@code putAll} 方法会为指定映射中的每个映射生成一次条目访问，顺序由指定映射的条目集迭代器提供的键值映射顺序决定。<i>其他方法不会生成条目访问。</i> 特别是，集合视图上的操作<i>不会</i>影响支持映射的迭代顺序。
 *
 * <p>{@link #removeEldestEntry(Map.Entry)} 方法可以被重写，以在添加新映射时自动删除陈旧的映射。
 *
 * <p>此类提供了所有可选的 {@code Map} 操作，并允许空元素。像 {@code HashMap} 一样，它为基本操作（{@code add}、{@code contains} 和 {@code remove}）提供了常数时间性能，假设哈希函数将元素适当地分散到桶中。性能可能会略低于 {@code HashMap}，因为维护链表会增加额外的开销，但有一个例外：迭代 {@code LinkedHashMap} 的集合视图所需的时间与映射的<i>大小</i>成正比，而不是其容量。迭代 {@code HashMap} 可能会更昂贵，所需时间与<i>容量</i>成正比。
 *
 * <p>链式哈希映射有两个影响其性能的参数：初始容量和加载因子。它们的定义与 {@code HashMap} 完全相同。但是，选择过高的初始容量对这个类的惩罚比对 {@code HashMap} 的惩罚要轻，因为这个类的迭代时间不受容量的影响。
 *
 * <p><strong>请注意，此实现不是同步的。</strong> 如果多个线程同时访问一个链式哈希映射，并且至少有一个线程修改映射的结构，则必须从外部进行同步。这通常通过在自然封装映射的某个对象上进行同步来实现。
 *
 * 如果没有这样的对象，应该使用 {@link Collections#synchronizedMap Collections.synchronizedMap} 方法将映射“包装”起来。最好在创建时这样做，以防止意外的未同步访问映射：<pre>
 *   Map m = Collections.synchronizedMap(new LinkedHashMap(...));</pre>
 *
 * 结构性修改是指任何添加或删除一个或多个映射的操作，或者在访问顺序链式哈希映射中影响迭代顺序的操作。在插入顺序链式哈希映射中，仅更改已包含在映射中的键的关联值不是结构性修改。<strong>在访问顺序链式哈希映射中，仅使用 {@code get} 查询映射就是结构性修改。</strong>
 *
 * <p>此类的所有集合视图方法返回的集合的 {@code iterator} 方法返回的迭代器是<em>快速失败</em>的：如果在创建迭代器后，以任何方式对映射进行结构性修改，除了通过迭代器自己的 {@code remove} 方法，迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，迭代器会快速且干净地失败，而不是冒险在未来某个不确定的时间出现任意的、不确定的行为。
 *
 * <p>请注意，迭代器的快速失败行为无法保证，因为通常来说，在存在未同步的并发修改的情况下，无法做出任何硬性保证。快速失败的迭代器在最佳努力的基础上抛出 {@code ConcurrentModificationException}。因此，编写依赖于此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类的所有集合视图方法返回的集合的 {@code spliterator} 方法返回的拆分器是<em><a href="Spliterator.html#binding">晚绑定</a></em>的，<em>快速失败</em>的，并且报告 {@link Spliterator#ORDERED}。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @implNote
 * 此类的所有集合视图方法返回的集合的 {@code spliterator} 方法返回的拆分器是从相应集合的迭代器创建的。
 *
 * @param <K> 此映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author  Josh Bloch
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     HashMap
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.4
 */
public class LinkedHashMap<K,V>
    extends HashMap<K,V>
    implements Map<K,V>
{


                /*
     * Implementation note.  A previous version of this class was
     * internally structured a little differently. Because superclass
     * HashMap now uses trees for some of its nodes, class
     * LinkedHashMap.Entry is now treated as intermediary node class
     * that can also be converted to tree form. The name of this
     * class, LinkedHashMap.Entry, is confusing in several ways in its
     * current context, but cannot be changed.  Otherwise, even though
     * it is not exported outside this package, some existing source
     * code is known to have relied on a symbol resolution corner case
     * rule in calls to removeEldestEntry that suppressed compilation
     * errors due to ambiguous usages. So, we keep the name to
     * preserve unmodified compilability.
     *
     * The changes in node classes also require using two fields
     * (head, tail) rather than a pointer to a header node to maintain
     * the doubly-linked before/after list. This class also
     * previously used a different style of callback methods upon
     * access, insertion, and removal.
     */

    /**
     * HashMap.Node 子类，用于正常的 LinkedHashMap 条目。
     */
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }

    private static final long serialVersionUID = 3801124242820219131L;

    /**
     * 双向链表的头（最老的）。
     */
    transient LinkedHashMap.Entry<K,V> head;

    /**
     * 双向链表的尾（最年轻的）。
     */
    transient LinkedHashMap.Entry<K,V> tail;

    /**
     * 此链接哈希映射的迭代顺序方法：<tt>true</tt>
     * 表示访问顺序，<tt>false</tt> 表示插入顺序。
     *
     * @serial
     */
    final boolean accessOrder;

    // 内部工具

    // 链接到列表末尾
    private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
        LinkedHashMap.Entry<K,V> last = tail;
        tail = p;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
    }

    // 将 src 的链接应用到 dst
    private void transferLinks(LinkedHashMap.Entry<K,V> src,
                               LinkedHashMap.Entry<K,V> dst) {
        LinkedHashMap.Entry<K,V> b = dst.before = src.before;
        LinkedHashMap.Entry<K,V> a = dst.after = src.after;
        if (b == null)
            head = dst;
        else
            b.after = dst;
        if (a == null)
            tail = dst;
        else
            a.before = dst;
    }

    // 覆盖 HashMap 钩子方法

    void reinitialize() {
        super.reinitialize();
        head = tail = null;
    }

    Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p =
            new LinkedHashMap.Entry<K,V>(hash, key, value, e);
        linkNodeLast(p);
        return p;
    }

    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
        LinkedHashMap.Entry<K,V> t =
            new LinkedHashMap.Entry<K,V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        TreeNode<K,V> p = new TreeNode<K,V>(hash, key, value, next);
        linkNodeLast(p);
        return p;
    }

    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
        TreeNode<K,V> t = new TreeNode<K,V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    void afterNodeRemoval(Node<K,V> e) { // 解链
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.before = p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a == null)
            tail = b;
        else
            a.before = b;
    }

    void afterNodeInsertion(boolean evict) { // 可能移除最老的节点
        LinkedHashMap.Entry<K,V> first;
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }

    void afterNodeAccess(Node<K,V> e) { // 将节点移动到最后
        LinkedHashMap.Entry<K,V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }

    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }

    /**
     * 构造一个空的插入顺序 <tt>LinkedHashMap</tt> 实例，
     * 具有指定的初始容量和加载因子。
     *
     * @param  initialCapacity 初始容量
     * @param  loadFactor      加载因子
     * @throws IllegalArgumentException 如果初始容量为负数
     *         或加载因子为非正数
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    /**
     * 构造一个空的插入顺序 <tt>LinkedHashMap</tt> 实例，
     * 具有指定的初始容量和默认加载因子（0.75）。
     *
     * @param  initialCapacity 初始容量
     * @throws IllegalArgumentException 如果初始容量为负数
     */
    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }


                /**
     * 构造一个空的插入顺序的 <tt>LinkedHashMap</tt> 实例
     * 默认初始容量为 16，加载因子为 0.75。
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
    }

    /**
     * 构造一个插入顺序的 <tt>LinkedHashMap</tt> 实例，包含与指定映射相同的映射。
     * 该 <tt>LinkedHashMap</tt> 实例的初始容量足以容纳指定映射中的映射，默认加载因子为 0.75。
     *
     * @param  m 要放置在此映射中的映射的映射
     * @throws NullPointerException 如果指定的映射为 null
     */
    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        super();
        accessOrder = false;
        putMapEntries(m, false);
    }

    /**
     * 构造一个空的 <tt>LinkedHashMap</tt> 实例，指定初始容量、加载因子和排序模式。
     *
     * @param  initialCapacity 初始容量
     * @param  loadFactor      加载因子
     * @param  accessOrder     排序模式 - <tt>true</tt> 表示访问顺序，<tt>false</tt> 表示插入顺序
     * @throws IllegalArgumentException 如果初始容量为负或加载因子为非正
     */
    public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }


    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>。
     *
     * @param value 要测试其在此映射中是否存在该值
     * @return <tt>true</tt> 如果此映射将一个或多个键映射到指定值
     */
    public boolean containsValue(Object value) {
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
            V v = e.value;
            if (v == value || (value != null && value.equals(v)))
                return true;
        }
        return false;
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得 {@code (key==null ? k==null :
     * key.equals(k))}，则此方法返回 {@code v}；否则返回 {@code null}。 (最多只能有一个这样的映射。)
     *
     * <p>返回值为 {@code null} 并不 <i>一定</i> 表示映射中没有该键的映射；也可能是映射显式地将键映射到 {@code null}。
     * 可以使用 {@link #containsKey containsKey} 操作来区分这两种情况。
     */
    public V get(Object key) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) == null)
            return null;
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    /**
     * {@inheritDoc}
     */
    public V getOrDefault(Object key, V defaultValue) {
       Node<K,V> e;
       if ((e = getNode(hash(key), key)) == null)
           return defaultValue;
       if (accessOrder)
           afterNodeAccess(e);
       return e.value;
   }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        super.clear();
        head = tail = null;
    }

    /**
     * 如果此映射应移除其最老的条目，则返回 <tt>true</tt>。
     * 此方法在将新条目插入映射后由 <tt>put</tt> 和 <tt>putAll</tt> 调用。它为实现者提供了每次添加新条目时移除最老条目的机会。
     * 这在映射表示缓存时很有用：它允许映射通过删除陈旧条目来减少内存消耗。
     *
     * <p>示例用法：此重写将允许映射增长到 100 个条目，然后在每次添加新条目时删除最老的条目，保持 100 个条目的稳定状态。
     * <pre>
     *     private static final int MAX_ENTRIES = 100;
     *
     *     protected boolean removeEldestEntry(Map.Entry eldest) {
     *        return size() &gt; MAX_ENTRIES;
     *     }
     * </pre>
     *
     * <p>此方法通常不会以任何方式修改映射，而是允许映射根据其返回值自行修改。允许此方法直接修改映射，但如果这样做，它 <i>必须</i> 返回
     * <tt>false</tt>（表示映射不应尝试进一步修改）。在从该方法内部修改映射后返回 <tt>true</tt> 的效果是不确定的。
     *
     * <p>此实现仅返回 <tt>false</tt>（因此此映射像普通映射一样行为 - 最老的条目从不被移除）。
     *
     * @param    eldest 映射中最不最近插入的条目，或者如果是访问顺序映射，则是最不最近访问的条目。如果此方法返回 <tt>true</tt>，这将是将被移除的条目。如果映射在导致此调用的 <tt>put</tt> 或 <tt>putAll</tt> 调用之前为空，这将是刚刚插入的条目；换句话说，如果映射包含一个条目，最老的条目也是最新的。
     * @return   <tt>true</tt> 如果最老的条目应从映射中移除；<tt>false</tt> 如果应保留。
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }

    /**
     * 返回此映射中包含的键的 {@link Set} 视图。
     * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改映射（通过迭代器自身的 <tt>remove</tt> 操作除外），迭代的结果是未定义的。该集合支持元素删除，这会从映射中删除相应的映射，通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、
     * <tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     * 其 {@link Spliterator} 通常提供更快的顺序性能，但并行性能远低于 {@code HashMap}。
     *
     * @return 此映射中包含的键的集合视图
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new LinkedKeySet();
            keySet = ks;
        }
        return ks;
    }


/**
 * 返回此映射中包含的值的 {@link Collection} 视图。
 * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射
 * （除了通过迭代器自身的 <tt>remove</tt> 操作外），迭代的结果是不确定的。该集合支持元素移除，
 * 通过 <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、
 * <tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。它不支持 <tt>add</tt> 或
 * <tt>addAll</tt> 操作。其 {@link Spliterator} 通常提供更快的顺序性能，但并行性能远不如 {@code HashMap}。
 *
 * @return 此映射中包含的值的视图
 */
public Collection<V> values() {
    Collection<V> vs = values;
    if (vs == null) {
        vs = new LinkedValues();
        values = vs;
    }
    return vs;
}

final class LinkedValues extends AbstractCollection<V> {
    public final int size()                 { return size; }
    public final void clear()               { LinkedHashMap.this.clear(); }
    public final Iterator<V> iterator() {
        return new LinkedValueIterator();
    }
    public final boolean contains(Object o) { return containsValue(o); }
    public final Spliterator<V> spliterator() {
        return Spliterators.spliterator(this, Spliterator.SIZED |
                                        Spliterator.ORDERED);
    }
    public final void forEach(Consumer<? super V> action) {
        if (action == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            action.accept(e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }
}

/**
 * 返回此映射中包含的映射的 {@link Set} 视图。
 * 该集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射
 * （除了通过迭代器自身的 <tt>remove</tt> 操作或通过迭代器返回的映射条目的 <tt>setValue</tt> 操作外），
 * 迭代的结果是不确定的。该集合支持元素移除，通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、
 * <tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。它不支持
 * <tt>add</tt> 或 <tt>addAll</tt> 操作。其 {@link Spliterator} 通常提供更快的顺序性能，但并行性能远不如 {@code HashMap}。
 *
 * @return 此映射中包含的映射的视图
 */
public Set<Map.Entry<K,V>> entrySet() {
    Set<Map.Entry<K,V>> es;
    return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
}

final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>> {
    public final int size()                 { return size; }
    public final void clear()               { LinkedHashMap.this.clear(); }
    public final Iterator<Map.Entry<K,V>> iterator() {
        return new LinkedEntryIterator();
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
        return Spliterators.spliterator(this, Spliterator.SIZED |
                                        Spliterator.ORDERED |
                                        Spliterator.DISTINCT);
    }
    public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
        if (action == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            action.accept(e);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }
}


                // Map 方法重写

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            action.accept(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            e.value = function.apply(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    // 迭代器

    abstract class LinkedHashIterator {
        LinkedHashMap.Entry<K,V> next;
        LinkedHashMap.Entry<K,V> current;
        int expectedModCount;

        LinkedHashIterator() {
            next = head;
            expectedModCount = modCount;
            current = null;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final LinkedHashMap.Entry<K,V> nextNode() {
            LinkedHashMap.Entry<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            current = e;
            next = e.after;
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

    final class LinkedKeyIterator extends LinkedHashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().getKey(); }
    }

    final class LinkedValueIterator extends LinkedHashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class LinkedEntryIterator extends LinkedHashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }


}
