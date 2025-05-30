
/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea and Josh Bloch with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util;

/**
 * 一个扩展了导航方法的 {@link SortedSet}，用于报告给定搜索目标的最接近匹配。方法 {@code lower}、
 * {@code floor}、{@code ceiling} 和 {@code higher} 分别返回小于、小于或等于、大于或等于、
 * 和大于给定元素的元素，如果不存在这样的元素则返回 {@code null}。一个 {@code NavigableSet}
 * 可以按升序或降序访问和遍历。方法 {@code descendingSet} 返回一个视图，该视图反转了所有关系和方向方法的含义。
 * 升序操作和视图的性能可能比降序操作和视图更快。此接口还定义了方法 {@code pollFirst} 和 {@code pollLast}，
 * 这些方法返回并移除最低和最高元素（如果存在），否则返回 {@code null}。方法 {@code subSet}、
 * {@code headSet} 和 {@code tailSet} 与同名的 {@code SortedSet} 方法不同，接受额外的参数描述
 * 下限和上限是包含的还是排除的。任何 {@code NavigableSet} 的子集都必须实现 {@code NavigableSet} 接口。
 *
 * <p> 在允许 {@code null} 元素的实现中，导航方法的返回值可能有歧义。然而，即使在这种情况下，
 * 也可以通过检查 {@code contains(null)} 来消除歧义。为了避免这些问题，建议实现此接口的类
 * <em>不要</em> 允许插入 {@code null} 元素。（注意，{@link Comparable} 元素的排序集本身就不允许 {@code null}。）
 *
 * <p> 方法
 * {@link #subSet(Object, Object) subSet(E, E)}、
 * {@link #headSet(Object) headSet(E)} 和
 * {@link #tailSet(Object) tailSet(E)}
 * 被指定为返回 {@code SortedSet}，以允许现有的 {@code SortedSet} 实现兼容地改造为实现 {@code NavigableSet}，
 * 但此接口的扩展和实现应鼓励重写这些方法以返回 {@code NavigableSet}。
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
     * 返回此集合中严格小于给定元素的最大元素，如果不存在这样的元素则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 小于 {@code e} 的最大元素，如果不存在这样的元素则返回 {@code null}
     * @throws ClassCastException 如果指定的元素无法与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     */
    E lower(E e);

    /**
     * 返回此集合中小于或等于给定元素的最大元素，如果不存在这样的元素则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 小于或等于 {@code e} 的最大元素，如果不存在这样的元素则返回 {@code null}
     * @throws ClassCastException 如果指定的元素无法与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     */
    E floor(E e);

    /**
     * 返回此集合中大于或等于给定元素的最小元素，如果不存在这样的元素则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 大于或等于 {@code e} 的最小元素，如果不存在这样的元素则返回 {@code null}
     * @throws ClassCastException 如果指定的元素无法与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     */
    E ceiling(E e);

    /**
     * 返回此集合中严格大于给定元素的最小元素，如果不存在这样的元素则返回 {@code null}。
     *
     * @param e 要匹配的值
     * @return 大于 {@code e} 的最小元素，如果不存在这样的元素则返回 {@code null}
     * @throws ClassCastException 如果指定的元素无法与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     */
    E higher(E e);


                /**
     * 获取并移除第一个（最小）元素，
     * 或者如果此集合为空，则返回 {@code null}。
     *
     * @return 第一个元素，或者如果此集合为空则返回 {@code null}
     */
    E pollFirst();

    /**
     * 获取并移除最后一个（最大）元素，
     * 或者如果此集合为空，则返回 {@code null}。
     *
     * @return 最后一个元素，或者如果此集合为空则返回 {@code null}
     */
    E pollLast();

    /**
     * 返回一个按升序排列的此集合的迭代器。
     *
     * @return 一个按升序排列的此集合的迭代器
     */
    Iterator<E> iterator();

    /**
     * 返回一个包含此集合元素的降序视图。
     * 降序集合由此集合支持，因此对集合的更改会反映在降序集合中，反之亦然。如果在迭代任一集合时对任一集合进行修改（除了通过迭代器的 {@code remove} 操作），迭代的结果是未定义的。
     *
     * <p>返回的集合的排序等同于
     * <tt>{@link Collections#reverseOrder(Comparator) Collections.reverseOrder}(comparator())</tt>。
     * 表达式 {@code s.descendingSet().descendingSet()} 返回一个与 {@code s} 基本等效的视图。
     *
     * @return 此集合的降序视图
     */
    NavigableSet<E> descendingSet();

    /**
     * 返回一个按降序排列的此集合的迭代器。
     * 效果等同于 {@code descendingSet().iterator()}。
     *
     * @return 一个按降序排列的此集合的迭代器
     */
    Iterator<E> descendingIterator();

    /**
     * 返回一个视图，该视图包含此集合中从 {@code fromElement} 到 {@code toElement} 范围内的元素。如果 {@code fromElement} 和 {@code toElement} 相等，则返回的集合为空，除非 {@code fromInclusive} 和 {@code toInclusive} 都为 true。返回的集合由此集合支持，因此对返回的集合的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param fromElement 返回集合的低端点
     * @param fromInclusive 如果低端点应包含在返回的视图中，则为 {@code true}
     * @param toElement 返回集合的高端点
     * @param toInclusive 如果高端点应包含在返回的视图中，则为 {@code true}
     * @return 一个视图，包含此集合中从 {@code fromElement}（包含）到 {@code toElement}（不包含）范围内的元素
     * @throws ClassCastException 如果 {@code fromElement} 和 {@code toElement} 不能使用此集合的比较器（或如果集合没有比较器，则使用自然排序）进行比较。实现可以但不要求在 {@code fromElement} 或 {@code toElement} 不能与集合中的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromElement} 或 {@code toElement} 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果 {@code fromElement} 大于 {@code toElement}；或者如果此集合本身有受限范围，且 {@code fromElement} 或 {@code toElement} 超出范围。
     */
    NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                           E toElement,   boolean toInclusive);

    /**
     * 返回一个视图，该视图包含此集合中小于（如果 {@code inclusive} 为 true，则包括）{@code toElement} 的元素。返回的集合由此集合支持，因此对返回的集合的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param toElement 返回集合的高端点
     * @param inclusive 如果高端点应包含在返回的视图中，则为 {@code true}
     * @return 一个视图，包含此集合中小于（如果 {@code inclusive} 为 true，则包括）{@code toElement} 的元素
     * @throws ClassCastException 如果 {@code toElement} 与此集合的比较器不兼容（或如果集合没有比较器，则 {@code toElement} 不实现 {@link Comparable}）。实现可以但不要求在 {@code toElement} 不能与集合中的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code toElement} 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身有受限范围，且 {@code toElement} 超出范围
     */
    NavigableSet<E> headSet(E toElement, boolean inclusive);

    /**
     * 返回一个视图，该视图包含此集合中大于（如果 {@code inclusive} 为 true，则包括）{@code fromElement} 的元素。返回的集合由此集合支持，因此对返回的集合的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 {@code IllegalArgumentException}。
     *
     * @param fromElement 返回集合的低端点
     * @param inclusive 如果低端点应包含在返回的视图中，则为 {@code true}
     * @return 一个视图，包含此集合中大于或等于 {@code fromElement} 的元素
     * @throws ClassCastException 如果 {@code fromElement} 与此集合的比较器不兼容（或如果集合没有比较器，则 {@code fromElement} 不实现 {@link Comparable}）。实现可以但不要求在 {@code fromElement} 不能与集合中的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 {@code fromElement} 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身有受限范围，且 {@code fromElement} 超出范围
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
