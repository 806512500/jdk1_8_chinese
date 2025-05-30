
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

/**
 * 一个 {@link Set}，进一步为其中的元素提供 <i>全序</i>。
 * 元素使用其 {@linkplain Comparable 自然排序} 或在创建排序集时通常提供的 {@link Comparator} 进行排序。
 * 集合的迭代器将按升序遍历集合中的元素。提供了几种额外的操作以利用排序。
 * （此接口是 {@link SortedMap} 的集合类比。）
 *
 * <p>插入到排序集中的所有元素都必须实现 <tt>Comparable</tt> 接口（或被指定的比较器接受）。
 * 此外，所有这样的元素必须是 <i>互相比对的</i>：对于排序集中的任何元素 <tt>e1</tt> 和 <tt>e2</tt>，
 * <tt>e1.compareTo(e2)</tt>（或 <tt>comparator.compare(e1, e2)</tt>）不得抛出 <tt>ClassCastException</tt>。
 * 尝试违反此限制将导致违规的方法或构造函数调用抛出 <tt>ClassCastException</tt>。
 *
 * <p>请注意，排序集维护的排序（无论是否提供了显式的比较器）必须与 <i>equals 一致</i>，
 * 以确保排序集正确实现 <tt>Set</tt> 接口。（请参阅 <tt>Comparable</tt> 接口或 <tt>Comparator</tt> 接口，
 * 以获取 <i>与 equals 一致</i> 的精确定义。）这是因为 <tt>Set</tt> 接口是根据 <tt>equals</tt> 操作定义的，
 * 但排序集使用其 <tt>compareTo</tt>（或 <tt>compare</tt>）方法进行所有元素比较，
 * 因此，根据此方法被视为相等的两个元素，从排序集的角度来看，是相等的。
 * 即使排序与 equals 不一致，排序集的行为也是明确定义的；它只是不遵守 <tt>Set</tt> 接口的一般契约。
 *
 * <p>所有通用的排序集实现类都应提供四个“标准”构造函数：
 * 1) 一个无参数（void）构造函数，创建一个根据其元素的自然排序排序的空排序集。
 * 2) 一个带有 <tt>Comparator</tt> 类型单个参数的构造函数，创建一个根据指定比较器排序的空排序集。
 * 3) 一个带有 <tt>Collection</tt> 类型单个参数的构造函数，创建一个具有与参数相同元素的排序集，
 *    根据元素的自然排序进行排序。
 * 4) 一个带有 <tt>SortedSet</tt> 类型单个参数的构造函数，创建一个具有与输入排序集相同元素和相同排序的排序集。
 * 无法强制执行此建议，因为接口不能包含构造函数。
 *
 * <p>注意：几个方法返回具有受限范围的子集。这些范围是 <i>半开的</i>，即包括低端点但不包括高端点（如适用）。
 * 如果您需要一个 <i>闭区间</i>（包括两个端点），并且元素类型允许计算给定值的后继值，
 * 请请求从 <tt>lowEndpoint</tt> 到 <tt>successor(highEndpoint)</tt> 的子范围。
 * 例如，假设 <tt>s</tt> 是一个字符串的排序集。以下惯用法获取 <tt>s</tt> 中从 <tt>low</tt> 到 <tt>high</tt>（包括）的所有字符串：<pre>
 *   SortedSet&lt;String&gt; sub = s.subSet(low, high+"\0");</pre>
 *
 * 可以使用类似的技术生成一个 <i>开区间</i>（不包括两个端点）。以下惯用法获取 <tt>s</tt> 中从 <tt>low</tt> 到 <tt>high</tt>（不包括）的所有字符串：<pre>
 *   SortedSet&lt;String&gt; sub = s.subSet(low+"\0", high);</pre>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 此集合维护的元素类型
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
     * 返回用于对集合中的元素进行排序的比较器，如果此集合使用元素的 {@linkplain Comparable 自然排序}，则返回 <tt>null</tt>。
     *
     * @return 用于对集合中的元素进行排序的比较器，如果此集合使用自然排序，则返回 <tt>null</tt>
     */
    Comparator<? super E> comparator();

    /**
     * 返回一个视图，该视图包含此集合中从 <tt>fromElement</tt>（包括）到 <tt>toElement</tt>（不包括）的元素。
     * （如果 <tt>fromElement</tt> 和 <tt>toElement</tt> 相等，则返回的集合为空。）
     * 返回的集合由此集合支持，因此返回的集合中的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param fromElement 返回集合的低端点（包括）
     * @param toElement 返回集合的高端点（不包括）
     * @return 一个视图，该视图包含此集合中从 <tt>fromElement</tt>（包括）到 <tt>toElement</tt>（不包括）的元素
     * @throws ClassCastException 如果 <tt>fromElement</tt> 和 <tt>toElement</tt> 无法使用此集合的比较器（或如果集合没有比较器，则使用自然排序）进行比较。
     *         实现可以但不要求在 <tt>fromElement</tt> 或 <tt>toElement</tt> 无法与集合中的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>fromElement</tt> 或 <tt>toElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果 <tt>fromElement</tt> 大于 <tt>toElement</tt>；或者此集合本身具有受限范围，
     *         且 <tt>fromElement</tt> 或 <tt>toElement</tt> 超出范围的边界
     */
    SortedSet<E> subSet(E fromElement, E toElement);


                /**
     * 返回此集合中小于 <tt>toElement</tt> 的元素部分的视图。返回的集合由此集合支持，因此返回的集合中的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param toElement 返回集合的高端点（不包括）
     * @return 返回此集合中小于 <tt>toElement</tt> 的元素部分的视图
     * @throws ClassCastException 如果 <tt>toElement</tt> 与此集合的比较器不兼容（或如果集合没有比较器，如果 <tt>toElement</tt> 不实现 {@link Comparable}）。
     *         实现可以但不要求在 <tt>toElement</tt> 无法与集合中当前的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>toElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身具有受限范围，且 <tt>toElement</tt> 超出范围的边界
     */
    SortedSet<E> headSet(E toElement);

    /**
     * 返回此集合中大于或等于 <tt>fromElement</tt> 的元素部分的视图。返回的集合由此集合支持，因此返回的集合中的更改会反映在此集合中，反之亦然。返回的集合支持此集合支持的所有可选集合操作。
     *
     * <p>尝试插入范围之外的元素时，返回的集合将抛出 <tt>IllegalArgumentException</tt>。
     *
     * @param fromElement 返回集合的低端点（包括）
     * @return 返回此集合中大于或等于 <tt>fromElement</tt> 的元素部分的视图
     * @throws ClassCastException 如果 <tt>fromElement</tt> 与此集合的比较器不兼容（或如果集合没有比较器，如果 <tt>fromElement</tt> 不实现 {@link Comparable}）。
     *         实现可以但不要求在 <tt>fromElement</tt> 无法与集合中当前的元素进行比较时抛出此异常。
     * @throws NullPointerException 如果 <tt>fromElement</tt> 为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果此集合本身具有受限范围，且 <tt>fromElement</tt> 超出范围的边界
     */
    SortedSet<E> tailSet(E fromElement);

    /**
     * 返回此集合中当前的第一个（最低）元素。
     *
     * @return 返回此集合中当前的第一个（最低）元素
     * @throws NoSuchElementException 如果此集合为空
     */
    E first();

    /**
     * 返回此集合中当前的最后一个（最高）元素。
     *
     * @return 返回此集合中当前的最后一个（最高）元素
     * @throws NoSuchElementException 如果此集合为空
     */
    E last();

    /**
     * 创建一个遍历此排序集合中元素的 {@code Spliterator}。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#DISTINCT}、{@link Spliterator#SORTED} 和 {@link Spliterator#ORDERED}。
     * 实现应记录额外的特征值。
     *
     * <p>如果排序集合的比较器（参见 {@link #comparator()}）为 {@code null}，则 spliterator 的比较器（参见
     * {@link java.util.Spliterator#getComparator()}）必须为 {@code null}。否则，spliterator 的比较器必须与排序集合的比较器相同或施加相同的总顺序。
     *
     * @implSpec
     * 默认实现从排序集合的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em> spliterator。spliterator 继承集合迭代器的
     * <em>快速失败</em> 属性。spliterator 的比较器与排序集合的比较器相同。
     * <p>
     * 创建的 {@code Spliterator} 额外报告 {@link Spliterator#SIZED}。
     *
     * @implNote
     * 创建的 {@code Spliterator} 额外报告 {@link Spliterator#SUBSIZED}。
     *
     * @return 一个遍历此排序集合中元素的 {@code Spliterator}
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
