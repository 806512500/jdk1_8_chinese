
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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;

/**
 * 基于 {@link ConcurrentSkipListMap} 的可扩展并发 {@link NavigableSet} 实现。集合中的元素根据它们的
 * {@linkplain Comparable 自然顺序} 或在创建集合时提供的 {@link Comparator} 进行排序，具体取决于使用哪个构造函数。
 *
 * <p>此实现提供了 {@code contains}、{@code add} 和 {@code remove} 操作及其变体的预期平均 <i>log(n)</i> 时间成本。
 * 插入、删除和访问操作可以由多个线程安全地并发执行。
 *
 * <p>迭代器和拆分器是
 * <a href="package-summary.html#Weakly"><i>弱一致性</i></a> 的。
 *
 * <p>升序有序视图及其迭代器比降序视图更快。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法 <em>不是</em> 常量时间操作。由于这些集合的异步性质，确定当前元素数量需要遍历所有元素，因此如果在遍历过程中修改了此集合，可能会报告不准确的结果。
 * 此外，{@code addAll}、{@code removeAll}、{@code retainAll}、{@code containsAll}、{@code equals} 和 {@code toArray} 等批量操作 <em>不是</em> 原子执行的。
 * 例如，与 {@code addAll} 操作并发运行的迭代器可能只会看到部分添加的元素。
 *
 * <p>此类及其迭代器实现了 {@link Set} 和 {@link Iterator} 接口的所有 <em>可选</em> 方法。与其他大多数并发集合实现一样，此集合不允许使用 {@code null} 元素，因为 {@code null} 参数和返回值不能可靠地区分于元素的缺失。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Doug Lea
 * @param <E> 由该集合维护的元素类型
 * @since 1.6
 */
public class ConcurrentSkipListSet<E>
    extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2479143111061671589L;

    /**
     * 底层映射。使用 Boolean.TRUE 作为每个元素的值。为了线程安全，此字段被声明为 final，这在 clone() 中带来了一些丑陋的处理。
     */
    private final ConcurrentNavigableMap<E,Object> m;

    /**
     * 构造一个新的空集合，该集合根据元素的 {@linkplain Comparable 自然顺序} 进行排序。
     */
    public ConcurrentSkipListSet() {
        m = new ConcurrentSkipListMap<E,Object>();
    }

    /**
     * 构造一个新的空集合，该集合根据指定的比较器对元素进行排序。
     *
     * @param comparator 将用于对集合进行排序的比较器。如果为 {@code null}，则使用元素的 {@linkplain Comparable 自然顺序}。
     */
    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        m = new ConcurrentSkipListMap<E,Object>(comparator);
    }

    /**
     * 构造一个新的集合，包含指定集合中的元素，并根据元素的 {@linkplain Comparable 自然顺序} 进行排序。
     *
     * @param c 将组成新集合的元素
     * @throws ClassCastException 如果 {@code c} 中的元素不是 {@link Comparable}，或者不是相互可比较的
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public ConcurrentSkipListSet(Collection<? extends E> c) {
        m = new ConcurrentSkipListMap<E,Object>();
        addAll(c);
    }

    /**
     * 构造一个新的集合，包含指定有序集合中的相同元素，并使用相同的排序。
     *
     * @param s 有序集合，其元素将组成新集合
     * @throws NullPointerException 如果指定的有序集合或其任何元素为 null
     */
    public ConcurrentSkipListSet(SortedSet<E> s) {
        m = new ConcurrentSkipListMap<E,Object>(s.comparator());
        addAll(s);
    }

    /**
     * 供子映射使用
     */
    ConcurrentSkipListSet(ConcurrentNavigableMap<E,Object> m) {
        this.m = m;
    }

    /**
     * 返回此 {@code ConcurrentSkipListSet} 实例的浅拷贝。（元素本身不会被克隆。）
     *
     * @return 此集合的浅拷贝
     */
    public ConcurrentSkipListSet<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ConcurrentSkipListSet<E> clone =
                (ConcurrentSkipListSet<E>) super.clone();
            clone.setMap(new ConcurrentSkipListMap<E,Object>(m));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /* ---------------- Set operations -------------- */

    /**
     * 返回此集合中的元素数量。如果此集合包含的元素多于 {@code Integer.MAX_VALUE}，则返回 {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，与大多数集合不同，此方法 <em>不是</em> 常量时间操作。由于这些集合的异步性质，确定当前元素数量需要遍历所有元素。
     * 此外，此方法执行期间集合的大小可能会改变，因此返回的结果可能不准确。因此，此方法在并发应用程序中通常不是很有用。
     *
     * @return 此集合中的元素数量
     */
    public int size() {
        return m.size();
    }

    /**
     * 如果此集合不包含任何元素，则返回 {@code true}。
     * @return 如果此集合不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /**
     * 如果此集合包含指定的元素，则返回 {@code true}。更正式地说，如果此集合包含一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此集合中的对象
     * @return 如果此集合包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定的元素不能与当前集合中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    /**
     * 如果指定的元素尚未存在，则将其添加到此集合中。更正式地说，如果此集合不包含任何元素 {@code e2} 使得 {@code e.equals(e2)}，则将指定的元素 {@code e} 添加到此集合中。
     * 如果此集合已经包含该元素，则调用将使集合保持不变并返回 {@code false}。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果此集合之前不包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果 {@code e} 不能与当前集合中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        return m.putIfAbsent(e, Boolean.TRUE) == null;
    }

    /**
     * 如果此集合包含指定的元素，则将其从集合中移除。更正式地说，移除一个元素 {@code e} 使得 {@code o.equals(e)}，如果此集合包含这样的元素。
     * 如果此集合包含该元素（或等效地，如果此调用导致集合发生变化），则返回 {@code true}。（此调用返回时，此集合将不再包含该元素。）
     *
     * @param o 要从集合中移除的对象，如果存在
     * @return 如果此集合包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果 {@code o} 不能与当前集合中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean remove(Object o) {
        return m.remove(o, Boolean.TRUE);
    }

    /**
     * 从此集合中移除所有元素。
     */
    public void clear() {
        m.clear();
    }

    /**
     * 返回一个按升序遍历此集合元素的迭代器。
     *
     * @return 一个按升序遍历此集合元素的迭代器
     */
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }

    /**
     * 返回一个按降序遍历此集合元素的迭代器。
     *
     * @return 一个按降序遍历此集合元素的迭代器
     */
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }


    /* ---------------- AbstractSet Overrides -------------- */

    /**
     * 将指定的对象与此集合进行比较以确定相等性。如果指定的对象也是一个集合，两个集合具有相同的大小，并且指定集合的每个成员都包含在此集合中（或等效地，此集合的每个成员都包含在指定集合中），则返回 {@code true}。
     * 此定义确保 equals 方法在集合接口的不同实现之间正确工作。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定的对象与此集合相等，则返回 {@code true}
     */
    public boolean equals(Object o) {
        // 覆盖 AbstractSet 版本以避免调用 size()
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        try {
            return containsAll(c) && c.containsAll(this);
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * 从此集合中移除所有包含在指定集合中的元素。如果指定的集合也是一个集合，此操作实际上会修改此集合，使其值为两个集合的 <i>非对称集合差</i>。
     *
     * @param  c 包含要从此集合中移除的元素的集合
     * @return 如果此集合因调用而发生变化，则返回 {@code true}
     * @throws ClassCastException 如果此集合中的一个或多个元素与指定集合的类型不兼容
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public boolean removeAll(Collection<?> c) {
        // 覆盖 AbstractSet 版本以避免不必要的调用 size()
        boolean modified = false;
        for (Object e : c)
            if (remove(e))
                modified = true;
        return modified;
    }

    /* ---------------- Relational operations -------------- */

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public E lower(E e) {
        return m.lowerKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public E floor(E e) {
        return m.floorKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public E ceiling(E e) {
        return m.ceilingKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public E higher(E e) {
        return m.higherKey(e);
    }

    public E pollFirst() {
        Map.Entry<E,Object> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    public E pollLast() {
        Map.Entry<E,Object> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }


    /* ---------------- SortedSet operations -------------- */


    public Comparator<? super E> comparator() {
        return m.comparator();
    }

    /**
     * @throws java.util.NoSuchElementException {@inheritDoc}
     */
    public E first() {
        return m.firstKey();
    }

    /**
     * @throws java.util.NoSuchElementException {@inheritDoc}
     */
    public E last() {
        return m.lastKey();
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 或 {@code toElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> subSet(E fromElement,
                                  boolean fromInclusive,
                                  E toElement,
                                  boolean toInclusive) {
        return new ConcurrentSkipListSet<E>
            (m.subMap(fromElement, fromInclusive,
                      toElement,   toInclusive));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.headMap(toElement, inclusive));
    }


                /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.tailMap(fromElement, inclusive));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 或
     *         {@code toElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    /**
     * 返回此集合中包含的元素的逆序视图。
     * 逆序集合由此集合支持，因此对集合的更改会反映在逆序集合中，反之亦然。
     *
     * <p>返回的集合的顺序等同于
     * {@link Collections#reverseOrder(Comparator) Collections.reverseOrder}{@code (comparator())}。
     * 表达式 {@code s.descendingSet().descendingSet()} 返回一个
     * 与 {@code s} 基本等效的视图。
     *
     * @return 此集合的逆序视图
     */
    public NavigableSet<E> descendingSet() {
        return new ConcurrentSkipListSet<E>(m.descendingMap());
    }

    /**
     * 返回一个遍历此集合中元素的 {@link Spliterator}。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#NONNULL}，{@link Spliterator#DISTINCT}，
     * {@link Spliterator#SORTED} 和 {@link Spliterator#ORDERED}，其遍历顺序为升序。
     * 覆盖实现应记录额外的特征值。
     *
     * <p>Spliterator 的比较器（参见
     * {@link java.util.Spliterator#getComparator()}) 如果集合的比较器（参见 {@link #comparator()}）
     * 为 {@code null}，则为 {@code null}。否则，Spliterator 的比较器与集合的比较器相同或施加相同的全序。
     *
     * @return 遍历此集合中元素的 {@code Spliterator}
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public Spliterator<E> spliterator() {
        if (m instanceof ConcurrentSkipListMap)
            return ((ConcurrentSkipListMap<E,?>)m).keySpliterator();
        else
            return (Spliterator<E>)((ConcurrentSkipListMap.SubMap<E,?>)m).keyIterator();
    }

    // 用于在克隆中重置映射的支持
    private void setMap(ConcurrentNavigableMap<E,Object> map) {
        UNSAFE.putObjectVolatile(this, mapOffset, map);
    }

    private static final sun.misc.Unsafe UNSAFE;
    private static final long mapOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentSkipListSet.class;
            mapOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("m"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
