
/*
 * 版权所有 (c) 1998, 2011, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 一个进一步提供其键的<em>全序</em>的 {@link Map}。
 * 该映射根据其键的 {@linkplain Comparable 自然排序} 或者在创建排序映射时通常提供的 {@link Comparator} 进行排序。
 * 此顺序在迭代排序映射的集合视图（由 {@code entrySet}、{@code keySet} 和 {@code values} 方法返回）时反映出来。
 * 提供了几个额外的操作以利用排序。（此接口是 {@link SortedSet} 的映射对应物。）
 *
 * <p>插入到排序映射中的所有键都必须实现 {@code Comparable} 接口（或被指定的比较器接受）。
 * 此外，所有这样的键必须是<em>相互可比较的</em>：对于排序映射中的任何键 {@code k1} 和 {@code k2}，{@code k1.compareTo(k2)}（或 {@code comparator.compare(k1, k2)}）都不得抛出 {@code ClassCastException}。
 * 尝试违反此限制将导致违规的方法或构造函数调用抛出 {@code ClassCastException}。
 *
 * <p>请注意，排序映射维护的顺序（无论是否提供了显式的比较器）必须与 {@code equals} <em>一致</em>，如果排序映射要正确实现 {@code Map} 接口。（请参阅 {@code Comparable} 接口或 {@code Comparator} 接口以获取 <em>与 equals 一致</em> 的精确定义。）
 * 这是因为 {@code Map} 接口是根据 {@code equals} 操作定义的，但排序映射使用其 {@code compareTo}（或 {@code compare}）方法进行所有键比较，因此，根据此方法被视为相等的两个键，在排序映射的角度来看，是相等的。
 * 即使树映射的顺序与 equals 不一致，其行为也是明确定义的；它只是不遵守 {@code Map} 接口的一般约定。
 *
 * <p>所有通用的排序映射实现类都应提供四个“标准”构造函数。虽然无法强制执行此建议，因为接口不能指定必需的构造函数。所有排序映射实现的预期“标准”构造函数是：
 * <ol>
 *   <li>一个无参数（void）构造函数，创建一个根据其键的自然顺序排序的空排序映射。</li>
 *   <li>一个带有 {@code Comparator} 类型单个参数的构造函数，创建一个根据指定比较器排序的空排序映射。</li>
 *   <li>一个带有 {@code Map} 类型单个参数的构造函数，创建一个具有与其参数相同的键值映射的新映射，根据键的自然顺序排序。</li>
 *   <li>一个带有 {@code SortedMap} 类型单个参数的构造函数，创建一个具有与输入排序映射相同的键值映射和相同顺序的新排序映射。</li>
 * </ol>
 *
 * <p><strong>注意</strong>：几个方法返回具有受限键范围的子映射。这样的范围是<em>半开的</em>，即它们包括低端点但不包括高端点（如果适用）。
 * 如果您需要一个<em>闭区间</em>（包括两个端点），并且键类型允许计算给定键的后继者，只需请求从 {@code lowEndpoint} 到 {@code successor(highEndpoint)} 的子范围。
 * 例如，假设 {@code m} 是一个键为字符串的映射。以下惯用法获取包含 {@code m} 中所有键值映射的视图，其键在 {@code low} 和 {@code high} 之间（包括两端）：<pre>
 *   SortedMap&lt;String, V&gt; sub = m.subMap(low, high+"\0");</pre>
 *
 * 可以使用类似的技术生成一个<em>开区间</em>（不包含任何端点）。以下惯用法获取包含 {@code m} 中所有键值映射的视图，其键在 {@code low} 和 {@code high} 之间（不包括两端）：<pre>
 *   SortedMap&lt;String, V&gt; sub = m.subMap(low+"\0", high);</pre>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <K> 该映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author  Josh Bloch
 * @see Map
 * @see TreeMap
 * @see SortedSet
 * @see Comparator
 * @see Comparable
 * @see Collection
 * @see ClassCastException
 * @since 1.2
 */

public interface SortedMap<K,V> extends Map<K,V> {
    /**
     * 返回用于对映射中的键进行排序的比较器，如果此映射使用其键的 {@linkplain Comparable 自然排序}，则返回 {@code null}。
     *
     * @return 用于对映射中的键进行排序的比较器，如果此映射使用自然排序，则返回 {@code null}
     */
    Comparator<? super K> comparator();

    /**
     * 返回一个视图，该视图包含此映射中键从 {@code fromKey}（包括）到 {@code toKey}（不包括）的部分。（如果 {@code fromKey} 和 {@code toKey} 相等，返回的映射为空。）
     * 返回的映射由此映射支持，因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>如果尝试插入一个超出其范围的键，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param fromKey 返回映射中键的低端点（包括）
     * @param toKey 返回映射中键的高端点（不包括）
     * @return 一个视图，该视图包含此映射中键从 {@code fromKey}（包括）到 {@code toKey}（不包括）的部分
     * @throws ClassCastException 如果 {@code fromKey} 和 {@code toKey} 不能使用此映射的比较器（或如果映射没有比较器，使用自然排序）相互比较。
     *         实现可以但不要求在 {@code fromKey} 或 {@code toKey} 不能与映射中当前的键比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromKey} 或 {@code toKey} 为 null 且此映射不允许 null 键
     * @throws IllegalArgumentException 如果 {@code fromKey} 大于 {@code toKey}；或者如果此映射本身具有受限范围，且 {@code fromKey} 或 {@code toKey} 超出范围
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

                /**
     * 返回此映射中键严格小于 {@code toKey} 的部分的视图。返回的映射由该映射支持，
     * 因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>尝试插入范围外的键时，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param toKey 返回映射中的键的高端点（不包括）
     * @return 一个视图，表示此映射中键严格小于 {@code toKey} 的部分
     * @throws ClassCastException 如果 {@code toKey} 与此映射的比较器不兼容（或如果映射没有比较器，
     *         如果 {@code toKey} 不实现 {@link Comparable}）。实现可以但不要求在 {@code toKey} 无法与映射中的当前键比较时抛出此异常。
     * @throws NullPointerException 如果 {@code toKey} 为 null 且此映射不允许 null 键
     * @throws IllegalArgumentException 如果此映射本身具有受限范围，且 {@code toKey} 超出范围的界限
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * 返回此映射中键大于或等于 {@code fromKey} 的部分的视图。返回的映射由该映射支持，
     * 因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>尝试插入范围外的键时，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param fromKey 返回映射中的键的低端点（包括）
     * @return 一个视图，表示此映射中键大于或等于 {@code fromKey} 的部分
     * @throws ClassCastException 如果 {@code fromKey} 与此映射的比较器不兼容（或如果映射没有比较器，
     *         如果 {@code fromKey} 不实现 {@link Comparable}）。实现可以但不要求在 {@code fromKey} 无法与映射中的当前键比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromKey} 为 null 且此映射不允许 null 键
     * @throws IllegalArgumentException 如果此映射本身具有受限范围，且 {@code fromKey} 超出范围的界限
     */
    SortedMap<K,V> tailMap(K fromKey);

    /**
     * 返回当前在此映射中的第一个（最小）键。
     *
     * @return 当前在此映射中的第一个（最小）键
     * @throws NoSuchElementException 如果此映射为空
     */
    K firstKey();

    /**
     * 返回当前在此映射中的最后一个（最大）键。
     *
     * @return 当前在此映射中的最后一个（最大）键
     * @throws NoSuchElementException 如果此映射为空
     */
    K lastKey();

    /**
     * 返回包含在此映射中的键的 {@link Set} 视图。集合的迭代器按升序返回键。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （除了通过迭代器自身的 {@code remove} 操作），迭代的结果是不确定的。集合支持元素移除，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、{@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作移除映射中的对应映射。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * @return 包含在此映射中的键的集合视图，按升序排序
     */
    Set<K> keySet();

    /**
     * 返回包含在此映射中的值的 {@link Collection} 视图。集合的迭代器按对应键的升序返回值。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （除了通过迭代器自身的 {@code remove} 操作），迭代的结果是不确定的。集合支持元素移除，
     * 通过 {@code Iterator.remove}、{@code Collection.remove}、{@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作移除映射中的对应映射。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * @return 包含在此映射中的值的集合视图，按键的升序排序
     */
    Collection<V> values();

    /**
     * 返回包含在此映射中的映射的 {@link Set} 视图。集合的迭代器按键的升序返回条目。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。如果在迭代集合的过程中修改了映射
     * （除了通过迭代器自身的 {@code remove} 操作，或通过迭代器返回的映射条目的 {@code setValue} 操作），迭代的结果是不确定的。集合支持元素移除，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、{@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作移除映射中的对应映射。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * @return 包含在此映射中的映射的集合视图，按键的升序排序
     */
    Set<Map.Entry<K, V>> entrySet();
}
