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
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.*;

/**
 * 一个支持 {@link NavigableMap} 操作的 {@link ConcurrentMap}，并且其导航子映射也递归地支持这些操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Doug Lea
 * @param <K> 此映射维护的键的类型
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
     * 逆序映射由此映射支持，因此对映射的更改会反映在逆序映射中，反之亦然。
     *
     * <p>返回的映射的顺序等同于
     * {@link Collections#reverseOrder(Comparator) Collections.reverseOrder}{@code (comparator())}。
     * 表达式 {@code m.descendingMap().descendingMap()} 返回一个与 {@code m} 基本等效的视图。
     *
     * @return 此映射的逆序视图
     */
    ConcurrentNavigableMap<K,V> descendingMap();

    /**
     * 返回此映射中包含的键的 {@link NavigableSet} 视图。
     * 集合的迭代器按升序返回键。
     * 集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。集合支持元素删除，这会从映射中删除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作。它不支持 {@code add} 或 {@code addAll}
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
     * 集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。集合支持元素删除，这会从映射中删除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * <p>此方法等同于方法 {@code navigableKeySet}。
     *
     * @return 此映射中键的可导航集合视图
     */
    NavigableSet<K> keySet();

    /**
     * 返回此映射中包含的键的逆序 {@link NavigableSet} 视图。
     * 集合的迭代器按降序返回键。
     * 集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。集合支持元素删除，这会从映射中删除相应的映射，
     * 通过 {@code Iterator.remove}、{@code Set.remove}、
     * {@code removeAll}、{@code retainAll} 和 {@code clear}
     * 操作。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 此映射中键的逆序可导航集合视图
     */
    public NavigableSet<K> descendingKeySet();
}
