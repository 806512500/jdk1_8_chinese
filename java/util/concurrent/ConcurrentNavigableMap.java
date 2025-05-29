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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所述发布到公共领域。
 */

package java.util.concurrent;
import java.util.*;

/**
 * 支持 {@link NavigableMap} 操作的 {@link ConcurrentMap}，并且其导航子映射也递归支持这些操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author Doug Lea
 * @param <K> 由此映射维护的键的类型
 * @param <V> 映射值的类型
 * @since 1.6
 */
public interface ConcurrentNavigableMap<K,V>
    extends ConcurrentMap<K,V>, NavigableMap<K,V>
{
    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                       K toKey,   boolean toInclusive);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> headMap(K toKey, boolean inclusive);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> tailMap(K fromKey, boolean inclusive);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> subMap(K fromKey, K toKey);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> headMap(K toKey);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableMap<K,V> tailMap(K fromKey);

    /**
     * 返回此映射中包含的映射的逆序视图。
     * 逆序映射由此映射支持，因此映射的更改会反映在逆序映射中，反之亦然。
     *
     * <p>返回的映射具有等效于
     * {@link Collections#reverseOrder(Comparator) Collections.reverseOrder}{@code (comparator())} 的排序。
     * 表达式 {@code m.descendingMap().descendingMap()} 返回一个与 {@code m} 基本等效的视图。
     *
     * @return 此映射的逆序视图
     */
    ConcurrentNavigableMap<K,V> descendingMap();

    /**
     * 返回此映射中包含的键的 {@link NavigableSet} 视图。
     * 集合的迭代器按升序返回键。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素
     * 移除，这会通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作从映射中移除相应的映射。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 此映射中键的可导航集合视图
     */
    public NavigableSet<K> navigableKeySet();

    /**
     * 返回此映射中包含的键的 {@link NavigableSet} 视图。
     * 集合的迭代器按升序返回键。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素
     * 移除，这会通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作从映射中移除相应的映射。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * <p>此方法等效于方法 {@code navigableKeySet}。
     *
     * @return 此映射中键的可导航集合视图
     */
    NavigableSet<K> keySet();

    /**
     * 返回此映射中包含的键的逆序 {@link NavigableSet} 视图。
     * 集合的迭代器按降序返回键。
     * 集合由映射支持，因此映射的更改会反映在集合中，反之亦然。集合支持元素
     * 移除，这会通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作从映射中移除相应的映射。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 此映射中键的逆序可导航集合视图
     */
    public NavigableSet<K> descendingKeySet();
}