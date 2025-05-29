
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.io.Serializable;

/**
 * 一个将键映射到值的对象。映射不能包含重复的键；每个键最多只能映射到一个值。
 *
 * <p>此接口取代了 <tt>Dictionary</tt> 类，后者是一个完全抽象的类而不是接口。
 *
 * <p><tt>Map</tt> 接口提供了三个 <i>集合视图</i>，允许将映射的内容视为键的集合、值的集合或键值映射的集合。映射的 <i>顺序</i> 定义为映射的集合视图上的迭代器返回其元素的顺序。某些映射实现（如 <tt>TreeMap</tt> 类）对其顺序有特定的保证；而其他实现（如 <tt>HashMap</tt> 类）则没有。
 *
 * <p>注意：如果使用可变对象作为映射键，必须非常小心。如果在对象作为映射中的键时，其值以影响 <tt>equals</tt> 比较的方式发生变化，则映射的行为是未指定的。此禁令的一个特例是映射不能包含自身作为键。虽然映射可以包含自身作为值，但必须极其谨慎：这样的映射上的 <tt>equals</tt> 和 <tt>hashCode</tt> 方法将不再定义良好。
 *
 * <p>所有通用映射实现类都应提供两个“标准”构造函数：一个无参（无参数）构造函数，用于创建一个空映射，以及一个参数类型为 <tt>Map</tt> 的构造函数，用于创建一个具有与参数相同的键值映射的新映射。实际上，后一个构造函数允许用户复制任何映射，生成所需类的等效映射。虽然无法强制执行此建议（因为接口不能包含构造函数），但 JDK 中的所有通用映射实现都遵守此建议。
 *
 * <p>此接口中包含的“破坏性”方法，即修改其操作的映射的方法，如果此映射不支持该操作，则指定抛出 <tt>UnsupportedOperationException</tt>。如果这是这种情况，这些方法可能会，但不要求，如果调用对映射没有影响，则抛出 <tt>UnsupportedOperationException</tt>。例如，如果要“叠加”其映射的映射为空，则在不可修改的映射上调用 {@link #putAll(Map)} 方法可能会，但不要求，抛出异常。
 *
 * <p>某些映射实现对其可以包含的键和值有限制。例如，某些实现禁止 null 键和值，而其他实现则对键的类型有限制。尝试插入不合格的键或值会抛出未检查的异常，通常是 <tt>NullPointerException</tt> 或 <tt>ClassCastException</tt>。尝试查询不合格的键或值的存在可能会抛出异常，或者可能简单地返回 false；某些实现将表现出前者的行为，而其他实现将表现出后者的行为。更一般地，尝试对不合格的键或值执行不会导致将不合格的元素插入映射的操作可能会抛出异常或成功，具体取决于实现。此类异常在本接口的规范中被标记为“可选”。
 *
 * <p>许多集合框架接口中的方法都是根据 {@link Object#equals(Object) equals} 方法定义的。例如，{@link #containsKey(Object) containsKey(Object key)} 方法的规范说：“如果且仅当此映射包含一个键 <tt>k</tt> 使得 <tt>(key==null ? k==null : key.equals(k))</tt> 时，返回 <tt>true</tt>。”此规范不应被解释为调用 <tt>Map.containsKey</tt> 时，使用非 null 参数 <tt>key</tt> 会调用 <tt>key.equals(k)</tt>。实现可以自由地实现优化，以避免调用 <tt>equals</tt>，例如，通过首先比较两个键的哈希码。（{@link Object#hashCode()} 规范保证两个具有不同哈希码的对象不能相等。）更一般地，集合框架接口的各种实现可以自由地利用底层 {@link Object} 方法的指定行为，只要实现者认为合适。
 *
 * <p>执行映射递归遍历的某些映射操作可能会因自引用实例而失败，这些实例直接或间接地包含自身。这包括 {@code clone()}、{@code equals()}、{@code hashCode()} 和 {@code toString()} 方法。实现可以选择处理自引用场景，但大多数当前实现不这样做。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @param <K> 此映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author  Josh Bloch
 * @see HashMap
 * @see TreeMap
 * @see Hashtable
 * @see SortedMap
 * @see Collection
 * @see Set
 * @since 1.2
 */
public interface Map<K,V> {
    // 查询操作

    /**
     * 返回此映射中的键值映射数。如果映射包含的元素多于 <tt>Integer.MAX_VALUE</tt>，则返回 <tt>Integer.MAX_VALUE</tt>。
     *
     * @return 此映射中的键值映射数
     */
    int size();

    /**
     * 如果此映射不包含任何键值映射，则返回 <tt>true</tt>。
     *
     * @return 如果此映射不包含任何键值映射，则返回 <tt>true</tt>
     */
    boolean isEmpty();

                /**
     * 如果此映射包含指定键的映射，则返回 <tt>true</tt>。更正式地说，当且仅当此映射包含一个键 <tt>k</tt> 的映射，使得
     * <tt>(key==null ? k==null : key.equals(k))</tt> 时，返回 <tt>true</tt>。（最多只能有一个这样的映射。）
     *
     * @param key 要测试其是否存在于此映射中的键
     * @return 如果此映射包含指定键的映射，则返回 <tt>true</tt>
     * @throws ClassCastException 如果键的类型对于此映射不合适
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键为 null 且此映射不允许 null 键
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     */
    boolean containsKey(Object key);

    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>。更正式地说，当且仅当此映射包含至少一个映射到值 <tt>v</tt> 的映射，使得
     * <tt>(value==null ? v==null : value.equals(v))</tt> 时，返回 <tt>true</tt>。对于 <tt>Map</tt> 接口的大多数实现，此操作可能需要与映射大小成线性的时间。
     *
     * @param value 要测试其是否存在于此映射中的值
     * @return 如果此映射将一个或多个键映射到指定值，则返回 <tt>true</tt>
     * @throws ClassCastException 如果值的类型对于此映射不合适
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的值为 null 且此映射不允许 null 值
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     */
    boolean containsValue(Object value);

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含一个从键 {@code k} 到值 {@code v} 的映射，使得 {@code (key==null ? k==null :
     * key.equals(k))}，则此方法返回 {@code v}；否则返回 {@code null}。（最多只能有一个这样的映射。）
     *
     * <p>如果此映射允许 null 值，那么返回值为 {@code null} 并不<i>一定</i>表示映射中没有该键的映射；也可能是映射显式地将该键映射到 {@code null}。可以使用 {@link #containsKey
     * containsKey} 操作来区分这两种情况。
     *
     * @param key 要返回其关联值的键
     * @return 指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}
     * @throws ClassCastException 如果键的类型对于此映射不合适
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键为 null 且此映射不允许 null 键
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     */
    V get(Object key);

    // 修改操作

    /**
     * 将指定值与此映射中的指定键关联（可选操作）。如果映射之前包含该键的映射，则旧值将被指定值替换。（如果且仅当 {@link #containsKey(Object) m.containsKey(k)} 返回
     * <tt>true</tt> 时，才说映射 <tt>m</tt> 包含键 <tt>k</tt> 的映射。）
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 之前与 <tt>key</tt> 关联的值，如果没有 <tt>key</tt> 的映射，则返回
     *         <tt>null</tt>。（返回 <tt>null</tt> 也可能表示映射之前将 <tt>null</tt> 与 <tt>key</tt> 关联，如果实现支持 <tt>null</tt> 值的话。）
     * @throws UnsupportedOperationException 如果此映射不支持 <tt>put</tt> 操作
     * @throws ClassCastException 如果指定的键或值的类阻止其被存储在此映射中
     * @throws NullPointerException 如果指定的键或值为 null 且此映射不允许 null 键或值
     * @throws IllegalArgumentException 如果指定的键或值的某些属性阻止其被存储在此映射中
     */
    V put(K key, V value);

    /**
     * 如果存在，则从此映射中移除指定键的映射（可选操作）。更正式地说，如果此映射包含从键 <tt>k</tt> 到值 <tt>v</tt> 的映射，使得
     * <code>(key==null ?  k==null : key.equals(k))</code>，则移除该映射。（映射最多只能包含一个这样的映射。）
     *
     * <p>返回此映射之前与键关联的值，如果没有该键的映射，则返回 <tt>null</tt>。
     *
     * <p>如果此映射允许 null 值，那么返回值为 <tt>null</tt> 并不<i>一定</i>表示映射中没有该键的映射；也可能是映射显式地将该键映射到 <tt>null</tt>。
     *
     * <p>调用返回后，映射将不再包含指定键的映射。
     *
     * @param key 要从映射中移除其映射的键
     * @return 之前与 <tt>key</tt> 关联的值，如果没有 <tt>key</tt> 的映射，则返回
     *         <tt>null</tt>。
     * @throws UnsupportedOperationException 如果此映射不支持 <tt>remove</tt> 操作
     * @throws ClassCastException 如果键的类型对于此映射不合适
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键为 null 且此映射不允许 null 键
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     */
    V remove(Object key);

    // 批量操作

    /**
     * 将指定映射中的所有映射复制到此映射中（可选操作）。此调用的效果等同于对指定映射中的每个从键 <tt>k</tt> 到值 <tt>v</tt> 的映射调用一次 {@link #put(Object,Object) put(k, v)}。如果在操作进行过程中修改了指定的映射，则此操作的行为是未定义的。
     *
     * @param m 要存储在此映射中的映射
     * @throws UnsupportedOperationException 如果此映射不支持 <tt>putAll</tt> 操作
     * @throws ClassCastException 如果指定映射中的键或值的类阻止其存储在此映射中
     * @throws NullPointerException 如果指定的映射为 null，或者此映射不允许 null 键或值，而指定的映射包含 null 键或值
     * @throws IllegalArgumentException 如果指定映射中的键或值的某些属性阻止其存储在此映射中
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * 从此映射中移除所有映射（可选操作）。此调用返回后，映射将为空。
     *
     * @throws UnsupportedOperationException 如果此映射不支持 <tt>clear</tt> 操作
     */
    void clear();


    // 视图

    /**
     * 返回包含在此映射中的键的 {@link Set} 视图。该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射（除了通过迭代器自身的 <tt>remove</tt> 操作），迭代的结果是未定义的。集合支持元素移除，这会通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含在此映射中的键的集合视图
     */
    Set<K> keySet();

    /**
     * 返回包含在此映射中的值的 {@link Collection} 视图。该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射（除了通过迭代器自身的 <tt>remove</tt> 操作），迭代的结果是未定义的。集合支持元素移除，这会通过 <tt>Iterator.remove</tt>、<tt>Collection.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含在此映射中的值的集合视图
     */
    Collection<V> values();

    /**
     * 返回包含在此映射中的映射的 {@link Set} 视图。该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射（除了通过迭代器自身的 <tt>remove</tt> 操作或通过迭代器返回的映射条目的 <tt>setValue</tt> 操作），迭代的结果是未定义的。集合支持元素移除，这会通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 包含在此映射中的映射的集合视图
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * 映射条目（键值对）。<tt>Map.entrySet</tt> 方法返回映射的集合视图，其元素是此类的对象。获取映射条目的唯一方法是从该集合视图的迭代器中获取。这些 <tt>Map.Entry</tt> 对象仅在迭代期间有效；更正式地说，如果在迭代器返回条目后修改了支持映射（除了通过映射条目的 <tt>setValue</tt> 操作），则映射条目的行为是未定义的。
     *
     * @see Map#entrySet()
     * @since 1.2
     */
    interface Entry<K,V> {
        /**
         * 返回与此条目对应的键。
         *
         * @return 与此条目对应的键
         * @throws IllegalStateException 实现可以但不要求在条目已被从支持映射中移除时抛出此异常。
         */
        K getKey();

        /**
         * 返回与此条目对应的值。如果映射已被从支持映射中移除（通过迭代器的 <tt>remove</tt> 操作），此调用的结果是未定义的。
         *
         * @return 与此条目对应的值
         * @throws IllegalStateException 实现可以但不要求在条目已被从支持映射中移除时抛出此异常。
         */
        V getValue();

        /**
         * 将与此条目对应的值替换为指定值（可选操作）。（写入支持映射。）如果映射已被从映射中移除（通过迭代器的 <tt>remove</tt> 操作），此调用的行为是未定义的。
         *
         * @param value 要存储在此条目中的新值
         * @return 条目的旧值
         * @throws UnsupportedOperationException 如果支持映射不支持 <tt>put</tt> 操作
         * @throws ClassCastException 如果指定值的类阻止其存储在支持映射中
         * @throws NullPointerException 如果支持映射不允许 null 值，而指定的值为 null
         * @throws IllegalArgumentException 如果此值的某些属性阻止其存储在支持映射中
         * @throws IllegalStateException 实现可以但不要求在条目已被从支持映射中移除时抛出此异常。
         */
        V setValue(V value);


                    /**
         * 将指定对象与此条目进行比较，以确定是否相等。
         * 如果给定对象也是一个映射条目，并且两个条目表示相同的映射，则返回 <tt>true</tt>。 更正式地说，两个
         * 条目 <tt>e1</tt> 和 <tt>e2</tt> 表示相同的映射
         * 如果<pre>
         *     (e1.getKey()==null ?
         *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &amp;&amp;
         *     (e1.getValue()==null ?
         *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre>
         * 这确保了 <tt>equals</tt> 方法在不同的 <tt>Map.Entry</tt> 接口实现之间能够正常工作。
         *
         * @param o 要与此映射条目进行相等性比较的对象
         * @return 如果指定对象等于此映射条目，则返回 <tt>true</tt>
         */
        boolean equals(Object o);

        /**
         * 返回此映射条目的哈希码值。映射条目 <tt>e</tt> 的哈希码定义为： <pre>
         *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *     (e.getValue()==null ? 0 : e.getValue().hashCode())
         * </pre>
         * 这确保了 <tt>e1.equals(e2)</tt> 意味着
         * <tt>e1.hashCode()==e2.hashCode()</tt> 对于任何两个条目
         * <tt>e1</tt> 和 <tt>e2</tt>，这是由 <tt>Object.hashCode</tt> 的一般合同所要求的。
         *
         * @return 此映射条目的哈希码值
         * @see Object#hashCode()
         * @see Object#equals(Object)
         * @see #equals(Object)
         */
        int hashCode();

        /**
         * 返回一个比较器，该比较器按键的自然顺序比较 {@link Map.Entry}。
         *
         * <p>返回的比较器是可序列化的，并且在比较具有空键的条目时会抛出 {@link
         * NullPointerException}。
         *
         * @param  <K> 映射键的 {@link Comparable} 类型
         * @param  <V> 映射值的类型
         * @return 按键的自然顺序比较 {@link Map.Entry} 的比较器。
         * @see Comparable
         * @since 1.8
         */
        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K,V>> comparingByKey() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getKey().compareTo(c2.getKey());
        }

        /**
         * 返回一个比较器，该比较器按值的自然顺序比较 {@link Map.Entry}。
         *
         * <p>返回的比较器是可序列化的，并且在比较具有空值的条目时会抛出 {@link
         * NullPointerException}。
         *
         * @param <K> 映射键的类型
         * @param <V> 映射值的 {@link Comparable} 类型
         * @return 按值的自然顺序比较 {@link Map.Entry} 的比较器。
         * @see Comparable
         * @since 1.8
         */
        public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K,V>> comparingByValue() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getValue().compareTo(c2.getValue());
        }

        /**
         * 返回一个使用给定 {@link Comparator} 按键比较 {@link Map.Entry} 的比较器。
         *
         * <p>如果指定的比较器也是可序列化的，则返回的比较器是可序列化的。
         *
         * @param  <K> 映射键的类型
         * @param  <V> 映射值的类型
         * @param  cmp 键的 {@link Comparator}
         * @return 按键比较 {@link Map.Entry} 的比较器。
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
        }

        /**
         * 返回一个使用给定 {@link Comparator} 按值比较 {@link Map.Entry} 的比较器。
         *
         * <p>如果指定的比较器也是可序列化的，则返回的比较器是可序列化的。
         *
         * @param  <K> 映射键的类型
         * @param  <V> 映射值的类型
         * @param  cmp 值的 {@link Comparator}
         * @return 按值比较 {@link Map.Entry} 的比较器。
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
        }
    }

    // 比较和哈希

    /**
     * 将指定对象与此映射进行比较，以确定是否相等。 如果给定对象也是一个映射，并且两个映射表示相同的映射，则返回
     * <tt>true</tt>。 更正式地说，两个映射 <tt>m1</tt> 和
     * <tt>m2</tt> 表示相同的映射
     * 如果 <tt>m1.entrySet().equals(m2.entrySet())</tt>。 这确保了
     * <tt>equals</tt> 方法在不同的 <tt>Map</tt> 接口实现之间能够正常工作。
     *
     * @param o 要与此映射进行相等性比较的对象
     * @return 如果指定对象等于此映射，则返回 <tt>true</tt>
     */
    boolean equals(Object o);

    /**
     * 返回此映射的哈希码值。映射的哈希码定义为映射的 <tt>entrySet()</tt> 视图中每个条目的哈希码之和。 这确保了 <tt>m1.equals(m2)</tt>
     * 意味着 <tt>m1.hashCode()==m2.hashCode()</tt> 对于任何两个映射
     * <tt>m1</tt> 和 <tt>m2</tt>，这是由 {@link Object#hashCode} 的一般合同所要求的。
     *
     * @return 此映射的哈希码值
     * @see Map.Entry#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    int hashCode();

                // Defaultable methods

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射关系，则返回
     * {@code defaultValue}。
     *
     * @implSpec
     * 默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param key 要返回其关联值的键
     * @param defaultValue 键的默认映射
     * @return 指定键所映射的值，如果此映射不包含该键的映射关系，则返回
     * {@code defaultValue}
     * @throws ClassCastException 如果键的类型对于此映射不适当
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键为 null 且此映射不允许 null 键
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @since 1.8
     */
    default V getOrDefault(Object key, V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key))
            ? v
            : defaultValue;
    }

    /**
     * 对此映射中的每个条目执行给定的操作，直到所有条目都已处理或操作抛出异常。除非
     * 实现类另有指定，否则操作按条目集迭代的顺序执行（如果指定了迭代顺序）。
     * 操作抛出的异常将传递给调用者。
     *
     * @implSpec
     * 默认实现等同于，对于此 {@code map}：
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     *
     * 默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param action 要对每个条目执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     * @throws ConcurrentModificationException 如果在迭代过程中发现条目被移除
     * @since 1.8
     */
    default void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // 这通常意味着条目已不再存在于映射中。
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    /**
     * 使用给定函数的结果替换每个条目的值，直到所有条目都已处理或函数抛出异常。函数抛出的异常将传递给调用者。
     *
     * @implSpec
     * <p>默认实现等同于，对于此 {@code map}：
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     entry.setValue(function.apply(entry.getKey(), entry.getValue()));
     * }</pre>
     *
     * <p>默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param function 要应用于每个条目的函数
     * @throws UnsupportedOperationException 如果此映射的条目集迭代器不支持 {@code set} 操作。
     * @throws ClassCastException 如果替换值的类阻止其存储在此映射中
     * @throws NullPointerException 如果指定的函数为 null，或指定的替换值为 null，且此映射不允许 null 值
     * @throws ClassCastException 如果替换值的类型对于此映射不适当
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果函数或替换值为 null，且此映射不允许 null 键或值
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws IllegalArgumentException 如果替换值的某些属性阻止其存储在此映射中
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ConcurrentModificationException 如果在迭代过程中发现条目被移除
     * @since 1.8
     */
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // 这通常意味着条目已不再存在于映射中。
                throw new ConcurrentModificationException(ise);
            }

            // 函数抛出的 ise 不是 cme。
            v = function.apply(k, v);

            try {
                entry.setValue(v);
            } catch(IllegalStateException ise) {
                // 这通常意味着条目已不再存在于映射中。
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    /**
     * 如果指定的键尚未与值关联（或映射到 {@code null}），则将其与给定值关联并返回
     * {@code null}，否则返回当前值。
     *
     * @implSpec
     * 默认实现等同于，对于此 {@code
     * map}：
     *
     * <pre> {@code
     * V v = map.get(key);
     * if (v == null)
     *     v = map.put(key, value);
     *
     * return v;
     * }</pre>
     *
     * <p>默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与指定键关联的先前值，如果该键没有映射关系，则返回
     *         {@code null}。
     *         （如果实现支持 null 值，{@code null} 返回值也可以表示映射之前将 {@code null} 与键关联。）
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果键或值对于此映射的类型不适当
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键或值为 null，且此映射不允许 null 键或值
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在此映射中
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @since 1.8
     */
    default V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null) {
            v = put(key, value);
        }


                    return v;
    }

    /**
     * 仅当指定的键当前映射到指定的值时，移除此条目。
     *
     * @implSpec
     * 默认实现等同于，对于此 {@code map}：
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.remove(key);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * <p>默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param key 与指定值关联的键
     * @param value 预期与指定键关联的值
     * @return 如果值被移除，则返回 {@code true}
     * @throws UnsupportedOperationException 如果此映射不支持 {@code remove} 操作
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果键或值的类型不适合此映射
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键或值为 null，而此映射不允许 null 键或值
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @since 1.8
     */
    default boolean remove(Object key, Object value) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, value) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        remove(key);
        return true;
    }

    /**
     * 仅当当前映射到指定值时，替换指定键的条目。
     *
     * @implSpec
     * 默认实现等同于，对于此 {@code map}：
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * 默认实现不会因为 oldValue 为 null 而抛出 NullPointerException，除非 newValue 也为 null。
     *
     * <p>默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param key 与指定值关联的键
     * @param oldValue 预期与指定键关联的值
     * @param newValue 要与指定键关联的新值
     * @return 如果值被替换，则返回 {@code true}
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果指定键或值的类阻止其存储在此映射中
     * @throws NullPointerException 如果指定的键或 newValue 为 null，而此映射不允许 null 键或值
     * @throws NullPointerException 如果 oldValue 为 null 而此映射不允许 null 值
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在此映射中
     * @since 1.8
     */
    default boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    /**
     * 仅当当前映射到某个值时，替换指定键的条目。
     *
     * @implSpec
     * 默认实现等同于，对于此 {@code map}：
     *
     * <pre> {@code
     * if (map.containsKey(key)) {
     *     return map.put(key, value);
     * } else
     *     return null;
     * }</pre>
     *
     * <p>默认实现对此方法的同步或原子性属性不作任何保证。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。
     *
     * @param key 与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与指定键关联的先前值，如果键没有映射，则返回 {@code null}。
     *         （返回 {@code null} 也可以表示映射之前将 {@code null} 与键关联，如果实现支持 null 值。）
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果指定键或值的类阻止其存储在此映射中
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键或值为 null，而此映射不允许 null 键或值
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在此映射中
     * @since 1.8
     */
    default V replace(K key, V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }


/**
 * 如果指定的键尚未与值关联（或映射到 {@code null}），则尝试使用给定的映射函数计算其值，并将其输入此映射，除非计算结果为 {@code null}。
 *
 * <p>如果函数返回 {@code null}，则不会记录映射。如果函数本身抛出（未检查的）异常，则重新抛出该异常，并且不会记录映射。最常见的用法是构造一个新对象作为初始映射值或缓存结果，例如：
 *
 * <pre> {@code
 * map.computeIfAbsent(key, k -> new Value(f(k)));
 * }</pre>
 *
 * <p>或者实现一个多值映射，{@code Map<K,Collection<V>>}，支持每个键有多个值：
 *
 * <pre> {@code
 * map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
 * }</pre>
 *
 *
 * @implSpec
 * 默认实现等效于对这个 {@code map} 执行以下步骤，然后返回当前值或如果当前值不存在则返回 {@code null}：
 *
 * <pre> {@code
 * if (map.get(key) == null) {
 *     V newValue = mappingFunction.apply(key);
 *     if (newValue != null)
 *         map.put(key, newValue);
 * }
 * }</pre>
 *
 * <p>默认实现不保证此方法的同步或原子性。任何提供原子性保证的实现必须重写此方法并记录其并发属性。特别是，所有实现子接口 {@link java.util.concurrent.ConcurrentMap} 的类必须记录函数是否仅在值不存在时原子性地应用。
 *
 * @param key 与指定值关联的键
 * @param mappingFunction 用于计算值的函数
 * @return 与指定键关联的当前（现有或计算的）值，如果计算值为 null，则返回 null
 * @throws NullPointerException 如果指定的键为 null 且此映射不支持 null 键，或 mappingFunction 为 null
 * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @throws ClassCastException 如果指定键或值的类阻止其存储在映射中
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @since 1.8
 */
default V computeIfAbsent(K key,
        Function<? super K, ? extends V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    V v;
    if ((v = get(key)) == null) {
        V newValue;
        if ((newValue = mappingFunction.apply(key)) != null) {
            put(key, newValue);
            return newValue;
        }
    }

    return v;
}

/**
 * 如果指定键的值存在且不为 null，则尝试根据键及其当前映射值计算新的映射。
 *
 * <p>如果函数返回 {@code null}，则移除映射。如果函数本身抛出（未检查的）异常，则重新抛出该异常，并且当前映射保持不变。
 *
 * @implSpec
 * 默认实现等效于对这个 {@code map} 执行以下步骤，然后返回当前值或如果当前值不存在则返回 {@code null}：
 *
 * <pre> {@code
 * if (map.get(key) != null) {
 *     V oldValue = map.get(key);
 *     V newValue = remappingFunction.apply(key, oldValue);
 *     if (newValue != null)
 *         map.put(key, newValue);
 *     else
 *         map.remove(key);
 * }
 * }</pre>
 *
 * <p>默认实现不保证此方法的同步或原子性。任何提供原子性保证的实现必须重写此方法并记录其并发属性。特别是，所有实现子接口 {@link java.util.concurrent.ConcurrentMap} 的类必须记录函数是否仅在值不存在时原子性地应用。
 *
 * @param key 与指定值关联的键
 * @param remappingFunction 用于计算值的函数
 * @return 与指定键关联的新值，如果没有则返回 null
 * @throws NullPointerException 如果指定的键为 null 且此映射不支持 null 键，或 remappingFunction 为 null
 * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @throws ClassCastException 如果指定键或值的类阻止其存储在映射中
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @since 1.8
 */
default V computeIfPresent(K key,
        BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    V oldValue;
    if ((oldValue = get(key)) != null) {
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            put(key, newValue);
            return newValue;
        } else {
            remove(key);
            return null;
        }
    } else {
        return null;
    }
}

/**
 * 尝试为指定键及其当前映射值（如果当前没有映射则为 {@code null}）计算映射。例如，要创建或追加一个 {@code String} 消息到值映射中：
 *
 * <pre> {@code
 * map.compute(key, (k, v) -> (v == null) ? msg : v.concat(msg))}</pre>
 * （方法 {@link #merge merge()} 通常用于此类目的更简单。）
 *
 * <p>如果函数返回 {@code null}，则移除映射（或如果最初不存在则保持不存在）。如果函数本身抛出（未检查的）异常，则重新抛出该异常，并且当前映射保持不变。
 *
 * @implSpec
 * 默认实现等效于对这个 {@code map} 执行以下步骤，然后返回当前值或如果当前值不存在则返回 {@code null}：
 *
 * <pre> {@code
 * V oldValue = map.get(key);
 * V newValue = remappingFunction.apply(key, oldValue);
 * if (oldValue != null ) {
 *    if (newValue != null)
 *       map.put(key, newValue);
 *    else
 *       map.remove(key);
 * } else {
 *    if (newValue != null)
 *       map.put(key, newValue);
 *    else
 *       return null;
 * }
 * }</pre>
 *
 * <p>默认实现不保证此方法的同步或原子性。任何提供原子性保证的实现必须重写此方法并记录其并发属性。特别是，所有实现子接口 {@link java.util.concurrent.ConcurrentMap} 的类必须记录函数是否仅在值不存在时原子性地应用。
 *
 * @param key 与指定值关联的键
 * @param remappingFunction 用于计算值的函数
 * @return 与指定键关联的新值，如果没有则返回 null
 * @throws NullPointerException 如果指定的键为 null 且此映射不支持 null 键，或 remappingFunction 为 null
 * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @throws ClassCastException 如果指定键或值的类阻止其存储在映射中
 *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
 * @since 1.8
 */
default V compute(K key,
        BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    V oldValue = get(key);


                    V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            // 删除映射
            if (oldValue != null || containsKey(key)) {
                // 有东西需要移除
                remove(key);
                return null;
            } else {
                // 什么都不做。保持原样。
                return null;
            }
        } else {
            // 添加或替换旧映射
            put(key, newValue);
            return newValue;
        }
    }

    /**
     * 如果指定的键尚未与任何值关联或与 null 关联，则将其与给定的非 null 值关联。
     * 否则，使用给定的重映射函数的结果替换关联的值，如果结果为 {@code null} 则移除该映射。
     * 当需要为键组合多个映射值时，此方法可能有用。
     * 例如，要创建或追加一个 {@code String msg} 到一个值映射中：
     *
     * <pre> {@code
     * map.merge(key, msg, String::concat)
     * }</pre>
     *
     * <p>如果函数返回 {@code null}，则映射将被移除。如果函数本身抛出（未检查的）异常，则重新抛出该异常，当前映射保持不变。
     *
     * @implSpec
     * 默认实现等同于对这个 {@code map} 执行以下步骤，然后返回当前值或如果不存在则返回 {@code null}：
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * <p>默认实现不保证此方法的同步或原子性。任何提供原子性保证的实现必须覆盖此方法并记录其并发属性。特别是，所有实现子接口 {@link java.util.concurrent.ConcurrentMap} 的类必须记录函数是否仅在值不存在时原子地应用。
     *
     * @param key 与结果值关联的键
     * @param value 要与现有值合并的非 null 值，或者如果无现有值或与键关联的值为 null，则与键关联的值
     * @param remappingFunction 用于重新计算值的函数
     * @return 与指定键关联的新值，或如果没有值与键关联则返回 null
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果指定的键或值的类阻止其存储在此映射中
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的键为 null 且此映射不支持 null 键，或值或 remappingFunction 为 null
     * @since 1.8
     */
    default V merge(K key, V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                   remappingFunction.apply(oldValue, value);
        if(newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }
}
