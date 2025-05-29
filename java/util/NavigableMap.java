
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
 * 由 Doug Lea 和 Josh Bloch 编写，并在 JCP
 * JSR-166 专家小组成员的帮助下发布到公共领域，如在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util;

/**
 * 一个扩展了导航方法的 {@link SortedMap}，返回与给定搜索目标最接近的匹配项。方法
 * {@code lowerEntry}, {@code floorEntry}, {@code ceilingEntry},
 * 和 {@code higherEntry} 返回与分别小于、小于或等于、大于或等于、大于给定键的键关联的
 * {@code Map.Entry} 对象，如果没有这样的键，则返回 {@code null}。类似地，方法
 * {@code lowerKey}, {@code floorKey}, {@code ceilingKey}, 和
 * {@code higherKey} 仅返回关联的键。所有这些方法都设计用于定位，而不是遍历条目。
 *
 * <p>{@code NavigableMap} 可以按升序或降序键顺序访问和遍历。{@code descendingMap}
 * 方法返回一个视图，该视图反转所有关系和方向方法的意义。升序操作和视图的性能可能比降序的要快。
 * 方法 {@code subMap}, {@code headMap},
 * 和 {@code tailMap} 与同名的 {@code
 * SortedMap} 方法不同，接受额外的参数描述下限和上限是包含的还是排除的。
 * 任何 {@code NavigableMap} 的子映射都必须实现 {@code
 * NavigableMap} 接口。
 *
 * <p>此接口还定义了方法 {@code firstEntry},
 * {@code pollFirstEntry}, {@code lastEntry}, 和
 * {@code pollLastEntry}，这些方法返回和/或移除最小和最大的映射（如果存在），否则返回 {@code null}。
 *
 * <p>实现条目返回方法的预期是返回表示生成时映射快照的 {@code Map.Entry} 对，因此通常
 * <em>不</em> 支持可选的 {@code Entry.setValue} 方法。但是请注意，可以使用
 * 方法 {@code put} 更改关联映射中的映射。
 *
 * <p>方法
 * {@link #subMap(Object, Object) subMap(K, K)},
 * {@link #headMap(Object) headMap(K)}, 和
 * {@link #tailMap(Object) tailMap(K)}
 * 被指定为返回 {@code SortedMap} 以允许现有的 {@code SortedMap} 实现与
 * {@code NavigableMap} 兼容地改造，但鼓励此接口的扩展和实现覆盖这些方法以返回
 * {@code NavigableMap}。类似地，
 * {@link #keySet()} 可以被覆盖以返回 {@code NavigableSet}。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @param <K> 由该映射维护的键的类型
 * @param <V> 映射值的类型
 * @since 1.6
 */
public interface NavigableMap<K,V> extends SortedMap<K,V> {
    /**
     * 返回与严格小于给定键的最大键关联的键值映射，如果没有这样的键，则返回
     * {@code null}。
     *
     * @param key 键
     * @return 与小于 {@code key} 的最大键关联的条目，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的当前键比较
     * @throws NullPointerException 如果指定的键为 null
     *         且此映射不允许 null 键
     */
    Map.Entry<K,V> lowerEntry(K key);

    /**
     * 返回严格小于给定键的最大键，如果没有这样的键，则返回
     * {@code null}。
     *
     * @param key 键
     * @return 小于 {@code key} 的最大键，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的当前键比较
     * @throws NullPointerException 如果指定的键为 null
     *         且此映射不允许 null 键
     */
    K lowerKey(K key);

    /**
     * 返回与小于或等于给定键的最大键关联的键值映射，如果没有这样的键，则返回
     * {@code null}。
     *
     * @param key 键
     * @return 与小于或等于 {@code key} 的最大键关联的条目，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的当前键比较
     * @throws NullPointerException 如果指定的键为 null
     *         且此映射不允许 null 键
     */
    Map.Entry<K,V> floorEntry(K key);

    /**
     * 返回小于或等于给定键的最大键，如果没有这样的键，则返回
     * {@code null}。
     *
     * @param key 键
     * @return 小于或等于 {@code key} 的最大键，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的当前键比较
     * @throws NullPointerException 如果指定的键为 null
     *         且此映射不允许 null 键
     */
    K floorKey(K key);

    /**
     * 返回与大于或等于给定键的最小键关联的键值映射，如果没有这样的键，则返回
     * {@code null}。
     *
     * @param key 键
     * @return 与大于或等于 {@code key} 的最小键关联的条目，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的当前键比较
     * @throws NullPointerException 如果指定的键为 null
     *         且此映射不允许 null 键
     */
    Map.Entry<K,V> ceilingEntry(K key);

                /**
     * 返回大于或等于给定键的最小键，如果没有这样的键，则返回 {@code null}。
     *
     * @param key 键
     * @return 大于或等于 {@code key} 的最小键，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     *         并且此映射不允许 null 键
     */
    K ceilingKey(K key);

    /**
     * 返回严格大于给定键的最小键的键值映射，如果没有这样的键，则返回 {@code null}。
     *
     * @param key 键
     * @return 大于 {@code key} 的最小键的条目，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     *         并且此映射不允许 null 键
     */
    Map.Entry<K,V> higherEntry(K key);

    /**
     * 返回严格大于给定键的最小键，如果没有这样的键，则返回 {@code null}。
     *
     * @param key 键
     * @return 大于 {@code key} 的最小键，
     *         如果没有这样的键，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     *         并且此映射不允许 null 键
     */
    K higherKey(K key);

    /**
     * 返回此映射中最小键的键值映射，如果映射为空，则返回 {@code null}。
     *
     * @return 最小键的条目，
     *         如果此映射为空，则返回 {@code null}
     */
    Map.Entry<K,V> firstEntry();

    /**
     * 返回此映射中最大键的键值映射，如果映射为空，则返回 {@code null}。
     *
     * @return 最大键的条目，
     *         如果此映射为空，则返回 {@code null}
     */
    Map.Entry<K,V> lastEntry();

    /**
     * 移除并返回与此映射中最小键关联的键值映射，如果映射为空，则返回 {@code null}。
     *
     * @return 被移除的此映射中的第一个条目，
     *         如果此映射为空，则返回 {@code null}
     */
    Map.Entry<K,V> pollFirstEntry();

    /**
     * 移除并返回与此映射中最大键关联的键值映射，如果映射为空，则返回 {@code null}。
     *
     * @return 被移除的此映射中的最后一个条目，
     *         如果此映射为空，则返回 {@code null}
     */
    Map.Entry<K,V> pollLastEntry();

    /**
     * 返回此映射中包含的映射的逆序视图。
     * 逆序映射由此映射支持，因此对映射的更改会反映在逆序映射中，反之亦然。如果在迭代任一映射的集合视图时修改了任一映射（通过迭代器自身的 {@code remove}
     * 操作除外），迭代的结果是不确定的。
     *
     * <p>返回的映射的排序等同于
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>。
     * 表达式 {@code m.descendingMap().descendingMap()} 返回一个与 {@code m} 基本等效的视图。
     *
     * @return 此映射的逆序视图
     */
    NavigableMap<K,V> descendingMap();

    /**
     * 返回此映射中包含的键的 {@link NavigableSet} 视图。
     * 集合的迭代器以升序返回键。
     * 集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射（通过迭代器自身的 {@code
     * remove} 操作除外），迭代的结果是不确定的。集合支持元素移除，这会移除映射中的相应映射，通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * @return 此映射中键的可导航集合视图
     */
    NavigableSet<K> navigableKeySet();

    /**
     * 返回此映射中包含的键的逆序 {@link NavigableSet} 视图。
     * 集合的迭代器以降序返回键。
     * 集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。如果在迭代集合时修改了映射（通过迭代器自身的 {@code
     * remove} 操作除外），迭代的结果是不确定的。集合支持元素移除，这会移除映射中的相应映射，通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或 {@code addAll} 操作。
     *
     * @return 此映射中键的逆序可导航集合视图
     */
    NavigableSet<K> descendingKeySet();

    /**
     * 返回此映射中键范围从 {@code fromKey} 到 {@code toKey} 的部分视图。如果 {@code fromKey} 和
     * {@code toKey} 相等，返回的映射为空，除非 {@code fromInclusive} 和 {@code toInclusive} 都为 true。返回的映射由此映射支持，因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>如果尝试插入范围之外的键，或构建的子映射的任一端点位于其范围之外，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param fromKey 返回映射中的键的低端点
     * @param fromInclusive 如果低端点应包含在返回的视图中，则为 {@code true}
     * @param toKey 返回映射中的键的高端点
     * @param toInclusive 如果高端点应包含在返回的视图中，则为 {@code true}
     * @return 键范围从 {@code fromKey} 到 {@code toKey} 的此映射的部分视图
     * @throws ClassCastException 如果 {@code fromKey} 和 {@code toKey}
     *         不能使用此映射的比较器（或如果映射没有比较器，则使用自然排序）进行比较。
     *         实现可以但不是必须在 {@code fromKey} 或 {@code toKey}
     *         不能与映射中的键进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromKey} 或 {@code toKey}
     *         为 null 并且此映射不允许 null 键
     * @throws IllegalArgumentException 如果 {@code fromKey} 大于
     *         {@code toKey}；或者如果此映射本身有受限的范围，而 {@code fromKey} 或 {@code toKey} 位于范围之外
     */
    NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                             K toKey,   boolean toInclusive);

                /**
     * 返回此映射中键小于（如果 {@code inclusive} 为 true，则包括）{@code toKey} 的部分视图。返回的映射由
     * 此映射支持，因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>尝试插入范围之外的键时，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param toKey 返回映射中键的高终点
     * @param inclusive 如果高终点应包含在返回的视图中，则为 {@code true}
     * @return 返回此映射中键小于（如果 {@code inclusive} 为 true，则包括）{@code toKey} 的部分视图
     * @throws ClassCastException 如果 {@code toKey} 与此映射的比较器不兼容（或如果映射没有比较器，
     *         如果 {@code toKey} 不实现 {@link Comparable}）。实现可以但不要求在 {@code toKey} 无法与映射中的键进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code toKey} 为 null 且此映射不允许 null 键
     * @throws IllegalArgumentException 如果此映射本身具有受限范围，且 {@code toKey} 超出范围的界限
     */
    NavigableMap<K,V> headMap(K toKey, boolean inclusive);

    /**
     * 返回此映射中键大于（如果 {@code inclusive} 为 true，则包括）{@code fromKey} 的部分视图。返回的映射由
     * 此映射支持，因此返回的映射中的更改会反映在此映射中，反之亦然。返回的映射支持此映射支持的所有可选映射操作。
     *
     * <p>尝试插入范围之外的键时，返回的映射将抛出 {@code IllegalArgumentException}。
     *
     * @param fromKey 返回映射中键的低终点
     * @param inclusive 如果低终点应包含在返回的视图中，则为 {@code true}
     * @return 返回此映射中键大于（如果 {@code inclusive} 为 true，则包括）{@code fromKey} 的部分视图
     * @throws ClassCastException 如果 {@code fromKey} 与此映射的比较器不兼容（或如果映射没有比较器，
     *         如果 {@code fromKey} 不实现 {@link Comparable}）。实现可以但不要求在 {@code fromKey} 无法与映射中的键进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromKey} 为 null 且此映射不允许 null 键
     * @throws IllegalArgumentException 如果此映射本身具有受限范围，且 {@code fromKey} 超出范围的界限
     */
    NavigableMap<K,V> tailMap(K fromKey, boolean inclusive);

    /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code subMap(fromKey, true, toKey, false)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

    /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code headMap(toKey, false)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code tailMap(fromKey, true)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap<K,V> tailMap(K fromKey);
}
