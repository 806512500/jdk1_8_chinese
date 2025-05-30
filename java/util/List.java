
/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.UnaryOperator;

/**
 * 一个有序集合（也称为<i>序列</i>）。此接口的用户可以精确控制列表中每个元素的插入位置。用户可以通过整数索引（列表中的位置）访问元素，并在列表中搜索元素。<p>
 *
 * 与集合不同，列表通常允许重复元素。更正式地说，列表通常允许存在两个元素<tt>e1</tt>和<tt>e2</tt>，使得<tt>e1.equals(e2)</tt>，并且如果允许null元素，通常允许多个null元素。可以想象，有人可能希望实现一个禁止重复元素的列表，当用户尝试插入重复元素时抛出运行时异常，但我们预计这种用法很少见。<p>
 *
 * <tt>List</tt>接口在<tt>iterator</tt>、<tt>add</tt>、<tt>remove</tt>、<tt>equals</tt>和<tt>hashCode</tt>方法的合同中，除了<tt>Collection</tt>接口指定的内容外，还增加了额外的规定。为了方便起见，这里也包括了其他继承方法的声明。<p>
 *
 * <tt>List</tt>接口提供了四种方法，用于按位置（索引）访问列表元素。列表（如Java数组）是从零开始的。请注意，对于某些实现，这些操作的执行时间可能与索引值成正比（例如<tt>LinkedList</tt>类）。因此，如果调用者不知道实现，通常遍历列表中的元素比通过索引访问它更可取。<p>
 *
 * <tt>List</tt>接口提供了一种特殊的迭代器，称为<tt>ListIterator</tt>，除了<tt>Iterator</tt>接口提供的正常操作外，还允许元素插入和替换，以及双向访问。提供了一种方法，可以从指定位置开始获取列表迭代器。<p>
 *
 * <tt>List</tt>接口提供了两种方法来搜索指定的对象。从性能角度来看，应谨慎使用这些方法。在许多实现中，它们将执行代价高昂的线性搜索。<p>
 *
 * <tt>List</tt>接口提供了两种方法，可以高效地在列表的任意位置插入和删除多个元素。<p>
 *
 * 注意：虽然列表可以包含自身作为元素，但强烈建议谨慎使用：在这种列表上的<tt>equals</tt>和<tt>hashCode</tt>方法将不再良好定义。
 *
 * <p>某些列表实现对其可以包含的元素有限制。例如，某些实现禁止null元素，某些实现对其元素的类型有限制。尝试添加不合格的元素会抛出未检查的异常，通常是<tt>NullPointerException</tt>或<tt>ClassCastException</tt>。尝试查询不合格元素的存在可能会抛出异常，或者可能简单地返回false；某些实现将表现出前一种行为，而某些实现将表现出后一种行为。更一般地说，尝试对不合格元素执行不会导致将不合格元素插入列表的操作，可能会抛出异常，也可能成功，具体取决于实现。此类异常在本接口的规范中被标记为“可选”。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>的成员。
 *
 * @param <E> 此列表中的元素类型
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see Set
 * @see ArrayList
 * @see LinkedList
 * @see Vector
 * @see Arrays#asList(Object[])
 * @see Collections#nCopies(int, Object)
 * @see Collections#EMPTY_LIST
 * @see AbstractList
 * @see AbstractSequentialList
 * @since 1.2
 */

public interface List<E> extends Collection<E> {
    // 查询操作

    /**
     * 返回此列表中的元素数量。如果此列表包含的元素数量超过<tt>Integer.MAX_VALUE</tt>，则返回<tt>Integer.MAX_VALUE</tt>。
     *
     * @return 此列表中的元素数量
     */
    int size();

    /**
     * 如果此列表不包含任何元素，则返回<tt>true</tt>。
     *
     * @return 如果此列表不包含任何元素，则返回<tt>true</tt>
     */
    boolean isEmpty();

    /**
     * 如果此列表包含指定的元素，则返回<tt>true</tt>。更正式地说，如果且仅当此列表包含至少一个元素<tt>e</tt>，使得<tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回<tt>true</tt>。
     *
     * @param o 要测试其在此列表中是否存在性的元素
     * @return 如果此列表包含指定的元素，则返回<tt>true</tt>
     * @throws ClassCastException 如果指定元素的类型与此列表不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为null，而此列表不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean contains(Object o);


                /**
     * 返回一个迭代器，按正确顺序遍历此列表中的元素。
     *
     * @return 一个迭代器，按正确顺序遍历此列表中的元素
     */
    Iterator<E> iterator();

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个元素到最后一个元素）。
     *
     * <p>返回的数组是“安全的”，即此列表中没有任何引用指向它。（换句话说，即使此列表由数组支持，此方法也必须分配一个新数组）。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的API之间的桥梁。
     *
     * @return 一个包含此列表中所有元素的数组，按正确顺序排列
     * @see Arrays#asList(Object[])
     */
    Object[] toArray();

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个元素到最后一个元素）；返回数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则返回该数组。否则，将分配一个运行时类型为指定数组且大小为此列表大小的新数组。
     *
     * <p>如果列表适合指定的数组并且有剩余空间（即，数组中的元素多于列表中的元素），则数组中紧接列表末尾的元素被设置为<tt>null</tt>。（这仅在调用者知道列表不包含任何null元素时，用于确定列表的长度。）
     *
     * <p>与{@link #toArray()}方法一样，此方法充当基于数组和基于集合的API之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设<tt>x</tt>是一个已知只包含字符串的列表。以下代码可以用来将列表转储到一个新分配的<tt>String</tt>数组中：
     *
     * <pre>{@code
     *     String[] y = x.toArray(new String[0]);
     * }</pre>
     *
     * 注意，<tt>toArray(new Object[0])</tt>与<tt>toArray()</tt>的功能相同。
     *
     * @param a 如果足够大，用于存储此列表元素的数组；否则，为此目的分配一个相同运行时类型的新数组。
     * @return 包含此列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为null
     */
    <T> T[] toArray(T[] a);


    // 修改操作

    /**
     * 将指定的元素添加到此列表的末尾（可选操作）。
     *
     * <p>支持此操作的列表可能对可以添加到此列表的元素施加限制。特别是，某些列表将拒绝添加null元素，而其他列表将对可以添加的元素类型施加限制。列表类应在文档中明确说明对可以添加的元素的任何限制。
     *
     * @param e 要添加到此列表的元素
     * @return <tt>true</tt>（如{@link Collection#add}所指定）
     * @throws UnsupportedOperationException 如果此列表不支持<tt>add</tt>操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定的元素为null且此列表不允许null元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此列表
     */
    boolean add(E e);

    /**
     * 如果此列表包含指定的元素，则移除其第一次出现（可选操作）。如果此列表不包含该元素，则保持不变。更正式地说，移除索引<tt>i</tt>最低的元素，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * （如果存在这样的元素）。如果此列表包含指定的元素（或等效地，如果此列表因调用而改变），则返回<tt>true</tt>。
     *
     * @param o 要从列表中移除的元素，如果存在
     * @return <tt>true</tt> 如果此列表包含指定的元素
     * @throws ClassCastException 如果指定元素的类型与此列表不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为null且此列表不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws UnsupportedOperationException 如果此列表不支持<tt>remove</tt>操作
     */
    boolean remove(Object o);


    // 批量修改操作

    /**
     * 如果此列表包含指定集合中的所有元素，则返回<tt>true</tt>。
     *
     * @param  c 要检查是否包含在此列表中的集合
     * @return <tt>true</tt> 如果此列表包含指定集合中的所有元素
     * @throws ClassCastException 如果指定集合中的一个或多个元素的类型与此列表不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的集合包含一个或多个null元素且此列表不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，或者指定的集合为null
     * @see #contains(Object)
     */
    boolean containsAll(Collection<?> c);


                /**
     * 将指定集合中的所有元素按指定集合的迭代器返回的顺序添加到此列表的末尾（可选操作）。如果在操作进行过程中修改了指定的集合，则此操作的行为是未定义的。（注意，如果指定的集合是此列表，并且它不是空的，这种情况会发生。）
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而改变，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>addAll</tt> 操作
     * @throws ClassCastException 如果指定集合中元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定集合包含一个或多个 null 元素且此列表不允许 null 元素，或者指定集合为 null
     * @throws IllegalArgumentException 如果指定集合中元素的某些属性阻止其被添加到此列表
     * @see #add(Object)
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * 将指定集合中的所有元素插入到此列表的指定位置（可选操作）。将当前位于该位置（如果有）及其后续的所有元素向右移动（增加它们的索引）。新元素将按指定集合的迭代器返回的顺序出现在此列表中。如果在操作进行过程中修改了指定的集合，则此操作的行为是未定义的。（注意，如果指定的集合是此列表，并且它不是空的，这种情况会发生。）
     *
     * @param index 指定集合中的第一个元素要插入的位置
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而改变，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>addAll</tt> 操作
     * @throws ClassCastException 如果指定集合中元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定集合包含一个或多个 null 元素且此列表不允许 null 元素，或者指定集合为 null
     * @throws IllegalArgumentException 如果指定集合中元素的某些属性阻止其被添加到此列表
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    boolean addAll(int index, Collection<? extends E> c);

    /**
     * 从此列表中移除包含在指定集合中的所有元素（可选操作）。
     *
     * @param c 包含要从此列表中移除的元素的集合
     * @return 如果此列表因调用而改变，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>removeAll</tt> 操作
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>)，
     *         或者指定集合为 null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean removeAll(Collection<?> c);

    /**
     * 仅保留此列表中包含在指定集合中的元素（可选操作）。换句话说，从此列表中移除所有不包含在指定集合中的元素。
     *
     * @param c 包含要在此列表中保留的元素的集合
     * @return 如果此列表因调用而改变，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>retainAll</tt> 操作
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>)，
     *         或者指定集合为 null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean retainAll(Collection<?> c);

    /**
     * 将此列表中的每个元素替换为应用操作符后得到的结果。操作符抛出的错误或运行时异常将传递给调用者。
     *
     * @implSpec
     * 默认实现等效于，对于此 {@code list}：
     * <pre>{@code
     *     final ListIterator<E> li = list.listIterator();
     *     while (li.hasNext()) {
     *         li.set(operator.apply(li.next()));
     *     }
     * }</pre>
     *
     * 如果列表的列表迭代器不支持 {@code set} 操作，则在替换第一个元素时将抛出 {@code UnsupportedOperationException}。
     *
     * @param operator 要应用于每个元素的操作符
     * @throws UnsupportedOperationException 如果此列表不可修改。实现可能在元素无法被替换或通常不支持修改时抛出此异常
     * @throws NullPointerException 如果指定的操作符为 null 或操作符结果为 null 值且此列表不允许 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     * @since 1.8
     */
    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }


                /**
     * 按照指定的 {@link Comparator} 引导的顺序对列表进行排序。
     *
     * <p>此列表中的所有元素必须使用指定的比较器 <i>相互可比较</i>（也就是说，对于列表中的任何元素 {@code e1} 和 {@code e2}，{@code c.compare(e1, e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>如果指定的比较器为 {@code null}，则此列表中的所有元素必须实现 {@link Comparable} 接口，并且应使用元素的 {@linkplain Comparable 自然顺序}。
     *
     * <p>此列表必须是可修改的，但不必是可调整大小的。
     *
     * @implSpec
     * 默认实现获取包含此列表中所有元素的数组，对数组进行排序，并迭代此列表，从数组中的相应位置重置每个元素。（这避免了尝试就地对链表进行排序时可能导致的 n<sup>2</sup> log(n) 性能。）
     *
     * @implNote
     * 此实现是一个稳定的、自适应的、迭代的归并排序，当输入数组部分排序时，所需的比较次数远少于 n lg(n)，而在输入数组随机排序时，提供传统归并排序的性能。如果输入数组接近排序，实现所需的比较次数约为 n。临时存储需求从接近排序的输入数组的小常数到随机排序的输入数组的 n/2 个对象引用不等。
     *
     * <p>该实现同样利用输入数组中的升序和降序，并且可以利用同一输入数组中不同部分的升序和降序。它非常适合合并两个或多个已排序的数组：只需将数组连接起来并排序结果数组。
     *
     * <p>该实现改编自 Tim Peters 为 Python 列表排序的算法
     * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>)。它使用了 Peter McIlroy 的 "Optimistic Sorting and Information Theoretic Complexity" 中的技术，该文发表于第四届年度 ACM-SIAM 离散算法研讨会论文集，第 467-474 页，1993 年 1 月。
     *
     * @param c 用于比较列表元素的 {@code Comparator}。{@code null} 值表示应使用元素的 {@linkplain Comparable 自然顺序}
     * @throws ClassCastException 如果列表包含使用指定比较器 <i>相互不可比较</i> 的元素
     * @throws UnsupportedOperationException 如果列表的列表迭代器不支持 {@code set} 操作
     * @throws IllegalArgumentException
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     *         如果发现比较器违反了 {@link Comparator} 合约
     * @since 1.8
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }

    /**
     * 从此列表中移除所有元素（可选操作）。此调用返回后，列表将为空。
     *
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>clear</tt> 操作
     */
    void clear();


    // 比较和哈希

    /**
     * 将指定对象与此列表进行相等性比较。仅当指定对象也是列表，两个列表具有相同的大小，并且两个列表中所有对应元素对都是 <i>相等的</i> 时，返回 <tt>true</tt>。（两个元素 <tt>e1</tt> 和 <tt>e2</tt> <i>相等</i> 如果 <tt>(e1==null ? e2==null : e1.equals(e2))</tt>。）换句话说，如果两个列表包含相同顺序的相同元素，则定义它们相等。此定义确保了 equals 方法在不同实现的 <tt>List</tt> 接口中正确工作。
     *
     * @param o 要与此列表进行相等性比较的对象
     * @return 如果指定对象等于此列表，则返回 <tt>true</tt>
     */
    boolean equals(Object o);

    /**
     * 返回此列表的哈希码值。列表的哈希码定义为以下计算的结果：
     * <pre>{@code
     *     int hashCode = 1;
     *     for (E e : list)
     *         hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
     * }</pre>
     * 这确保了对于任何两个列表 <tt>list1</tt> 和 <tt>list2</tt>，如果 <tt>list1.equals(list2)</tt>，则 <tt>list1.hashCode()==list2.hashCode()</tt>，符合 {@link Object#hashCode} 的一般合约。
     *
     * @return 此列表的哈希码值
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    int hashCode();


    // 位置访问操作

    /**
     * 返回此列表中指定位置的元素。
     *
     * @param index 要返回的元素的索引
     * @return 此列表中指定位置的元素
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    E get(int index);

    /**
     * 用指定元素替换此列表中指定位置的元素（可选操作）。
     *
     * @param index 要替换的元素的索引
     * @param element 要存储在指定位置的元素
     * @return 之前位于指定位置的元素
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>set</tt> 操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定元素为 null 且此列表不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此列表
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    E set(int index, E element);


                /**
     * 在此列表的指定位置插入指定的元素（可选操作）。将当前位置的元素（如果有）和后续元素向右移动（索引加一）。
     *
     * @param index 要插入指定元素的索引
     * @param element 要插入的元素
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>add</tt> 操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定元素为 null 且此列表不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此列表
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    void add(int index, E element);

    /**
     * 从此列表中移除指定位置的元素（可选操作）。将后续元素向左移动（索引减一）。返回被移除的元素。
     *
     * @param index 要移除的元素的索引
     * @return 指定位置之前的元素
     * @throws UnsupportedOperationException 如果此列表不支持 <tt>remove</tt> 操作
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    E remove(int index);


    // 查找操作

    /**
     * 返回此列表中指定元素第一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最低索引 <tt>i</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，
     * 如果没有这样的索引，则返回 -1。
     *
     * @param o 要查找的元素
     * @return 指定元素在列表中第一次出现的索引，如果此列表不包含该元素，则返回 -1
     * @throws ClassCastException 如果指定元素的类型与此列表不兼容
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定元素为 null 且此列表不允许 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    int indexOf(Object o);

    /**
     * 返回此列表中指定元素最后一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最高索引 <tt>i</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，
     * 如果没有这样的索引，则返回 -1。
     *
     * @param o 要查找的元素
     * @return 指定元素在列表中最后一次出现的索引，如果此列表不包含该元素，则返回 -1
     * @throws ClassCastException 如果指定元素的类型与此列表不兼容
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定元素为 null 且此列表不允许 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    int lastIndexOf(Object o);


    // 列表迭代器

    /**
     * 返回此列表中元素的列表迭代器（按正确顺序）。
     *
     * @return 此列表中元素的列表迭代器（按正确顺序）
     */
    ListIterator<E> listIterator();

    /**
     * 返回此列表中元素的列表迭代器（按正确顺序），从列表的指定位置开始。
     * 指定的索引表示通过调用 {@link ListIterator#next next} 返回的第一个元素。
     * 初始调用 {@link ListIterator#previous previous} 将返回指定索引减一的元素。
     *
     * @param index 列表迭代器返回的第一个元素的索引（通过调用 {@link ListIterator#next next}）
     * @return 此列表中元素的列表迭代器（按正确顺序），从列表的指定位置开始
     * @throws IndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index > size()})
     */
    ListIterator<E> listIterator(int index);

    // 视图

    /**
     * 返回此列表中从指定的 <tt>fromIndex</tt>（包括）到 <tt>toIndex</tt>（不包括）部分的视图。
     * （如果 <tt>fromIndex</tt> 和 <tt>toIndex</tt> 相等，则返回的列表为空。）
     * 返回的列表由此列表支持，因此返回列表中的非结构更改会反映在此列表中，反之亦然。
     * 返回的列表支持此列表支持的所有可选列表操作。<p>
     *
     * 此方法消除了显式范围操作的需要（通常数组中存在此类操作）。
     * 任何期望列表的操作都可以通过传递子列表视图而不是整个列表来用作范围操作。
     * 例如，以下惯用法从列表中移除一个范围的元素：
     * <pre>{@code
     *      list.subList(from, to).clear();
     * }</pre>
     * 类似的惯用法可以构建用于 <tt>indexOf</tt> 和 <tt>lastIndexOf</tt>，
     * 并且 <tt>Collections</tt> 类中的所有算法都可以应用于子列表。<p>
     *
     * 如果此列表（即支持列表）以任何方式被 <i>结构修改</i>，则返回列表的语义变得未定义。
     * （结构修改是指改变此列表的大小，或以其他方式扰动此列表，使得正在进行的迭代可能产生不正确的结果。）
     *
     * @param fromIndex 子列表的低端点（包括）
     * @param toIndex 子列表的高端点（不包括）
     * @return 此列表中指定范围的视图
     * @throws IndexOutOfBoundsException 如果端点索引值非法
     *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *         fromIndex &gt; toIndex</tt>)
     */
    List<E> subList(int fromIndex, int toIndex);


                /**
     * 创建一个 {@link Spliterator}，用于遍历此列表中的元素。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#ORDERED}。实现应记录额外特性值的报告。
     *
     * @implSpec
     * 默认实现从列表的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em> 的 spliterator。
     * 该 spliterator 继承了列表迭代器的 <em>快速失败</em> 特性。
     *
     * @implNote
     * 创建的 {@code Spliterator} 额外报告
     * {@link Spliterator#SUBSIZED}。
     *
     * @return 一个 {@code Spliterator}，用于遍历此列表中的元素
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
