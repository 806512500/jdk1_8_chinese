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
 * 由 Doug Lea 和 Josh Bloch 编写，并在 JCP JSR-166 专家小组成员的帮助下完成，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util;

/**
 * 一个扩展了导航方法的 {@link SortedSet}，用于报告给定搜索目标的最接近匹配。方法 {@code lower}，
 * {@code floor}，{@code ceiling}，和 {@code higher} 分别返回小于、小于或等于、大于或等于、
 * 和大于给定元素的元素，如果不存在这样的元素，则返回 {@code null}。一个 {@code NavigableSet}
 * 可以按升序或降序访问和遍历。{@code descendingSet} 方法返回一个集合视图，其中所有关系和方向方法的含义反转。
 * 升序操作和视图的性能可能比降序操作和视图的性能更快。此接口还定义了 {@code pollFirst} 和
 * {@code pollLast} 方法，这些方法返回并移除最低和最高元素（如果存在），否则返回 {@code null}。
 * 方法 {@code subSet}，{@code headSet}，和 {@code tailSet} 与同名的 {@code SortedSet} 方法不同，
 * 它们接受额外的参数描述下限和上限是包含的还是排除的。任何 {@code NavigableSet} 的子集都必须实现
 * {@code NavigableSet} 接口。
 *
 * <p> 在允许 {@code null} 元素的实现中，导航方法的返回值可能是模糊的。但是，即使在这种情况下，
 * 也可以通过检查 {@code contains(null)} 来消除歧义。为了防止此类问题，建议此接口的实现
 * <em>不要</em> 允许插入 {@code null} 元素。（注意，{@link Comparable} 元素的排序集本质上不允许
 * {@code null}。）
 *
 * <p>方法
 * {@link #subSet(Object, Object) subSet(E, E)}，
 * {@link #headSet(Object) headSet(E)}，和
 * {@link #tailSet(Object) tailSet(E)}
 * 被指定为返回 {@code SortedSet} 以允许现有的 {@code SortedSet} 实现兼容地改造为实现
 * {@code NavigableSet}，但此接口的扩展和实现被鼓励覆盖这些方法以返回
 * {@code NavigableSet}。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @param <E> 由该集合维护的元素类型
 * @since 1.6
 */
public interface NavigableSet<E> extends SortedSet<E> {
    /**
     * 返回此集合中严格小于给定元素的最大元素，如果没有这样的元素，则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 小于 {@code e} 的最大元素，
     *         如果没有这样的元素，则返回 {@code null}
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集合不允许 null 元素
     */
    E lower(E e);

    /**
     * 返回此集合中小于或等于给定元素的最大元素，如果没有这样的元素，则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 小于或等于 {@code e} 的最大元素，
     *         如果没有这样的元素，则返回 {@code null}
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集合不允许 null 元素
     */
    E floor(E e);

    /**
     * 返回此集合中大于或等于给定元素的最小元素，如果没有这样的元素，则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 大于或等于 {@code e} 的最小元素，
     *         如果没有这样的元素，则返回 {@code null}
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集合不允许 null 元素
     */
    E ceiling(E e);

    /**
     * 返回此集合中严格大于给定元素的最小元素，如果没有这样的元素，则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 大于 {@code e} 的最小元素，
     *         如果没有这样的元素，则返回 {@code null}
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集合不允许 null 元素
     */
    E higher(E e);

    /**
     * 检索并移除第一个（最小的）元素，
     * 如果此集合为空，则返回 {@code null}。
     *
     * @return 第一个元素，如果此集合为空，则返回 {@code null}
     */
    E pollFirst();

    /**
     * 检索并移除最后一个（最大的）元素，
     * 如果此集合为空，则返回 {@code null}。
     *
     * @return 最后一个元素，如果此集合为空，则返回 {@code null}
     */
    E pollLast();

    /**
     * 返回一个按升序排列的此集合元素的迭代器。
     *
     * @return 一个按升序排列的此集合元素的迭代器
     */
    Iterator<E> iterator();

    /**
     * 返回一个包含在此集合中的元素的逆序视图。
     * 逆序集由此集合支持，因此对集合的更改会反映在逆序集中，反之亦然。如果在遍历任一集合的过程中
     * 修改了任一集合（通过迭代器自身的 {@code remove} 操作除外），则遍历的结果是不确定的。
     *
     * <p>返回的集合的排序等同于
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>。
     * 表达式 {@code s.descendingSet().descendingSet()} 返回一个与 {@code s} 基本等效的视图。
     *
     * @return 此集合的逆序视图
     */
    NavigableSet<E> descendingSet();

                /**
     * 返回一个迭代器，按照降序顺序遍历此集合中的元素。
     * 效果等同于 {@code descendingSet().iterator()}。
     *
     * @return 一个迭代器，按照降序顺序遍历此集合中的元素
     */
    Iterator<E> descendingIterator();

    /**
     * 返回一个视图，包含此集合中元素范围从 {@code fromElement} 到 {@code toElement} 的部分。如果 {@code fromElement} 和
     * {@code toElement} 相等，则返回的集合为空，除非 {@code fromInclusive} 和 {@code toInclusive} 都为 true。返回的集合
     * 由这个集合支持，因此返回集合中的更改会反映在这个集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param fromElement 返回集合的低端点
     * @param fromInclusive 如果低端点应包含在返回视图中，则为 {@code true}
     * @param toElement 返回集合的高端点
     * @param toInclusive 如果高端点应包含在返回视图中，则为 {@code true}
     * @return 一个视图，包含此集合中元素范围从 {@code fromElement}（包含）到 {@code toElement}（不包含）的部分
     * @throws ClassCastException 如果 {@code fromElement} 和 {@code toElement} 无法使用此集合的比较器（或，如果集合没有比较器，使用
     *         自然排序）进行比较。实现可以但不要求在 {@code fromElement} 或 {@code toElement} 无法与集合中的元素比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromElement} 或 {@code toElement} 为 null，而此集合不允许 null 元素
     * @throws IllegalArgumentException 如果 {@code fromElement} 大于 {@code toElement}；或者此集合本身有受限范围，而 {@code fromElement} 或
     *         {@code toElement} 超出了范围的界限。
     */
    NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                           E toElement,   boolean toInclusive);

    /**
     * 返回一个视图，包含此集合中元素小于（如果 {@code inclusive} 为 true，则包含等于）{@code toElement} 的部分。返回的集合
     * 由这个集合支持，因此返回集合中的更改会反映在这个集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param toElement 返回集合的高端点
     * @param inclusive 如果高端点应包含在返回视图中，则为 {@code true}
     * @return 一个视图，包含此集合中元素小于（如果 {@code inclusive} 为 true，则包含等于）{@code toElement} 的部分
     * @throws ClassCastException 如果 {@code toElement} 与此集合的比较器不兼容（或，如果集合没有比较器，如果 {@code toElement} 不实现 {@link Comparable}）。
     *         实现可以但不要求在 {@code toElement} 无法与集合中的元素比较时抛出此异常。
     * @throws NullPointerException 如果 {@code toElement} 为 null，而此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身有受限范围，而 {@code toElement} 超出了范围的界限
     */
    NavigableSet<E> headSet(E toElement, boolean inclusive);

    /**
     * 返回一个视图，包含此集合中元素大于（如果 {@code inclusive} 为 true，则包含等于）{@code fromElement} 的部分。返回的集合
     * 由这个集合支持，因此返回集合中的更改会反映在这个集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param fromElement 返回集合的低端点
     * @param inclusive 如果低端点应包含在返回视图中，则为 {@code true}
     * @return 一个视图，包含此集合中元素大于或等于 {@code fromElement} 的部分
     * @throws ClassCastException 如果 {@code fromElement} 与此集合的比较器不兼容（或，如果集合没有比较器，如果 {@code fromElement} 不实现 {@link Comparable}）。
     *         实现可以但不要求在 {@code fromElement} 无法与集合中的元素比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromElement} 为 null，而此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身有受限范围，而 {@code fromElement} 超出了范围的界限
     */
    NavigableSet<E> tailSet(E fromElement, boolean inclusive);

    /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code subSet(fromElement, true, toElement, false)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedSet<E> subSet(E fromElement, E toElement);

    /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code headSet(toElement, false)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedSet<E> headSet(E toElement);

                /**
     * {@inheritDoc}
     *
     * <p>等同于 {@code tailSet(fromElement, true)}。
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedSet<E> tailSet(E fromElement);
}
