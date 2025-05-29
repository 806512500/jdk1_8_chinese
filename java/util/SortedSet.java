
/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个进一步提供其元素 <i>全序</i> 的 {@link Set}。
 * 元素使用它们的 {@linkplain Comparable 自然排序} 或者在创建排序集时通常提供的 {@link Comparator} 进行排序。
 * 集合的迭代器将按升序遍历集合。提供了几种额外的操作以利用排序。（此接口是 {@link SortedMap} 的集合类比。）
 *
 * <p>所有插入到排序集中的元素必须实现 <tt>Comparable</tt> 接口（或被指定的比较器接受）。
 * 此外，所有这样的元素必须是 <i>相互可比较的</i>：<tt>e1.compareTo(e2)</tt>（或 <tt>comparator.compare(e1, e2)</tt>）
 * 对于排序集中的任何元素 <tt>e1</tt> 和 <tt>e2</tt> 都不应抛出 <tt>ClassCastException</tt>。
 * 违反此限制的尝试将导致违规的方法或构造函数调用抛出 <tt>ClassCastException</tt>。
 *
 * <p>请注意，排序集维护的排序（无论是否提供了显式比较器）必须与 <i>equals 一致</i>，
 * 如果排序集要正确实现 <tt>Set</tt> 接口的话。（参见 <tt>Comparable</tt> 接口或 <tt>Comparator</tt> 接口
 * 对 <i>与 equals 一致</i> 的精确定义。）这是因为 <tt>Set</tt> 接口是根据 <tt>equals</tt> 操作定义的，
 * 但排序集使用其 <tt>compareTo</tt>（或 <tt>compare</tt>）方法执行所有元素比较，
 * 因此，根据此方法被视为相等的两个元素，从排序集的角度来看，是相等的。
 * 即使排序与 equals 不一致，排序集的行为也是明确定义的；它只是不遵守 <tt>Set</tt> 接口的一般契约。
 *
 * <p>所有通用的排序集实现类都应提供四个“标准”构造函数：
 * 1) 一个无参数（void）构造函数，创建一个根据其元素的自然排序排序的空排序集。
 * 2) 一个类型为 <tt>Comparator</tt> 的单参数构造函数，创建一个根据指定比较器排序的空排序集。
 * 3) 一个类型为 <tt>Collection</tt> 的单参数构造函数，创建一个具有与参数相同元素的新排序集，
 *    根据元素的自然排序进行排序。
 * 4) 一个类型为 <tt>SortedSet</tt> 的单参数构造函数，创建一个具有与输入排序集相同元素和相同排序的新排序集。
 * 没有办法强制执行此建议，因为接口不能包含构造函数。
 *
 * <p>注意：几个方法返回具有受限范围的子集。这样的范围是 <i>半开的</i>，即，它们包括它们的低端点但不包括高端点（如果适用）。
 * 如果你需要一个 <i>闭区间</i>（包括两个端点），并且元素类型允许计算给定值的后继值，只需请求从 <tt>lowEndpoint</tt>
 * 到 <tt>successor(highEndpoint)</tt> 的子范围。例如，假设 <tt>s</tt> 是一个字符串的排序集。
 * 以下惯用法获取包含 <tt>s</tt> 中从 <tt>low</tt> 到 <tt>high</tt>（包括）的所有字符串的视图：<pre>
 *   SortedSet&lt;String&gt; sub = s.subSet(low, high+"\0");</pre>
 *
 * 可以使用类似的技术生成 <i>开区间</i>（不包括两个端点）。以下惯用法获取包含 <tt>s</tt> 中从 <tt>low</tt>
 * 到 <tt>high</tt>（不包括）的所有字符串的视图：<pre>
 *   SortedSet&lt;String&gt; sub = s.subSet(low+"\0", high);</pre>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @param <E> 由此集维护的元素类型
 *
 * @author  Josh Bloch
 * @see Set
 * @see TreeSet
 * @see SortedMap
 * @see Collection
 * @see Comparable
 * @see Comparator
 * @see ClassCastException
 * @since 1.2
 */

public interface SortedSet<E> extends Set<E> {
    /**
     * 返回用于对集合中的元素进行排序的比较器，如果此集合使用元素的 {@linkplain Comparable
     * 自然排序}，则返回 <tt>null</tt>。
     *
     * @return 用于对集合中的元素进行排序的比较器，如果此集合使用自然排序，则返回 <tt>null</tt>
     */
    Comparator<? super E> comparator();

    /**
     * 返回一个视图，该视图包含此集合中从 <tt>fromElement</tt>（包括）到 <tt>toElement</tt>
     * （不包括）的元素范围。（如果 <tt>fromElement</tt> 和 <tt>toElement</tt> 相等，则返回的集合为空。）
     * 返回的集合由此集合支持，因此返回的集合中的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>如果尝试插入范围之外的元素，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param fromElement 返回集合的低端点（包括）
     * @param toElement 返回集合的高端点（不包括）
     * @return 一个视图，该视图包含此集合中从 <tt>fromElement</tt>（包括）到 <tt>toElement</tt>（不包括）的元素范围
     * @throws ClassCastException 如果 <tt>fromElement</tt> 和 <tt>toElement</tt> 不能使用此集合的比较器（或如果集合没有比较器，则使用自然排序）进行比较。
     *         实现可以但不要求在 <tt>fromElement</tt> 或 <tt>toElement</tt> 不能与集合中当前的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>fromElement</tt> 或 <tt>toElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果 <tt>fromElement</tt> 大于 <tt>toElement</tt>；或者如果此集合本身具有受限范围，
     *         且 <tt>fromElement</tt> 或 <tt>toElement</tt> 超出范围的界限
     */
    SortedSet<E> subSet(E fromElement, E toElement);

                /**
     * 返回此集合中小于 <tt>toElement</tt> 的元素部分的视图。返回的集合由该集合支持，因此返回的集合中的更改
     * 会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围外的元素时，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param toElement 返回集合的高端点（不包括）
     * @return 返回此集合中小于 <tt>toElement</tt> 的元素部分的视图
     * @throws ClassCastException 如果 <tt>toElement</tt> 与此集合的比较器不兼容（或如果集合没有比较器，
     *         如果 <tt>toElement</tt> 不实现 {@link Comparable}）。实现可以但不要求在 <tt>toElement</tt>
     *         无法与集合中当前的元素比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>toElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身具有受限范围，且 <tt>toElement</tt> 超出范围的界限
     */
    SortedSet<E> headSet(E toElement);

    /**
     * 返回此集合中大于或等于 <tt>fromElement</tt> 的元素部分的视图。返回的集合由该集合支持，因此返回的集合中的更改
     * 会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围外的元素时，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param fromElement 返回集合的低端点（包括）
     * @return 返回此集合中大于或等于 <tt>fromElement</tt> 的元素部分的视图
     * @throws ClassCastException 如果 <tt>fromElement</tt> 与此集合的比较器不兼容（或如果集合没有比较器，
     *         如果 <tt>fromElement</tt> 不实现 {@link Comparable}）。实现可以但不要求在 <tt>fromElement</tt>
     *         无法与集合中当前的元素比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>fromElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身具有受限范围，且 <tt>fromElement</tt> 超出范围的界限
     */
    SortedSet<E> tailSet(E fromElement);

    /**
     * 返回当前在此集合中的第一个（最低）元素。
     *
     * @return 当前在此集合中的第一个（最低）元素
     * @throws NoSuchElementException 如果此集合为空
     */
    E first();

    /**
     * 返回当前在此集合中的最后一个（最高）元素。
     *
     * @return 当前在此集合中的最后一个（最高）元素
     * @throws NoSuchElementException 如果此集合为空
     */
    E last();

    /**
     * 创建一个遍历此排序集合元素的 {@code Spliterator}。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#DISTINCT}，
     * {@link Spliterator#SORTED} 和 {@link Spliterator#ORDERED}。
     * 实现应记录额外的特征值。
     *
     * <p>如果排序集合的比较器（见 {@link #comparator()}）为 {@code null}，则 spliterator 的比较器（见
     * {@link java.util.Spliterator#getComparator()}）必须为 {@code null}。否则，spliterator 的比较器必须与或
     * 强加与排序集合的比较器相同的总排序。
     *
     * @implSpec
     * 默认实现从排序集合的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em> spliterator。
     * spliterator 继承了集合迭代器的 <em>快速失败</em> 特性。spliterator 的比较器与排序集合的比较器相同。
     * <p>
     * 创建的 {@code Spliterator} 额外报告 {@link Spliterator#SIZED}。
     *
     * @implNote
     * 创建的 {@code Spliterator} 额外报告 {@link Spliterator#SUBSIZED}。
     *
     * @return 遍历此排序集合元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return new Spliterators.IteratorSpliterator<E>(
                this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED) {
            @Override
            public Comparator<? super E> getComparator() {
                return SortedSet.this.comparator();
            }
        };
    }
}
