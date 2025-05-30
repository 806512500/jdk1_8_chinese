
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合层次结构中的根接口。集合表示一组对象，称为其元素。某些集合允许重复元素，而其他集合不允许。某些集合是有序的，而其他集合是无序的。JDK 不直接提供此接口的实现：它通过其子接口（如 <tt>Set</tt> 和 <tt>List</tt>）提供实现。此接口通常用于传递集合并在需要最大通用性的情况下操作它们。
 *
 * <p><i>包</i> 或 <i>多重集</i>（允许包含重复元素的无序集合）应直接实现此接口。
 *
 * <p>所有通用 <tt>Collection</tt> 实现类（通常通过其子接口间接实现 <tt>Collection</tt>）应提供两个“标准”构造函数：一个无参数的构造函数，用于创建一个空集合，以及一个带有 <tt>Collection</tt> 类型单个参数的构造函数，用于创建一个与参数具有相同元素的新集合。实际上，后一个构造函数允许用户复制任何集合，生成一个等效的所需实现类型的集合。虽然无法强制执行此约定（因为接口不能包含构造函数），但 Java 平台库中的所有通用 <tt>Collection</tt> 实现都遵守此约定。
 *
 * <p>此接口中包含的“破坏性”方法，即修改其操作的集合的方法，如果此集合不支持该操作，则指定抛出 <tt>UnsupportedOperationException</tt>。如果这是这种情况，这些方法可能会，但不要求在调用对集合没有影响的情况下抛出 <tt>UnsupportedOperationException</tt>。例如，对不可修改的集合调用 {@link #addAll(Collection)} 方法时，如果要添加的集合为空，可能会，但不要求抛出该异常。
 *
 * <p><a name="optional-restrictions">
 * 一些集合实现对其可以包含的元素有限制。</a> 例如，某些实现禁止 null 元素，而其他实现对其元素的类型有限制。尝试添加不合格的元素会抛出一个未检查的异常，通常是 <tt>NullPointerException</tt> 或 <tt>ClassCastException</tt>。尝试查询不合格元素的存在可能会抛出异常，或者可能简单地返回 false；某些实现将表现出前一种行为，而其他实现将表现出后一种行为。更一般地说，尝试对不合格元素执行不会导致将不合格元素插入集合的操作可能会抛出异常或成功，具体取决于实现。此类异常在该接口的规范中标记为“可选”。
 *
 * <p>每个集合确定其自己的同步策略。除非实现提供了更强的保证，否则在另一个线程正在修改集合时调用集合上的任何方法可能会导致未定义的行为；这包括直接调用、将集合传递给可能执行调用的方法以及使用现有迭代器检查集合。
 *
 * <p>许多集合框架接口中的方法都是根据 {@link Object#equals(Object) equals} 方法定义的。例如，{@link #contains(Object) contains(Object o)} 方法的规范说：“如果且仅当此集合至少包含一个元素 <tt>e</tt> 使得 <tt>(o==null ? e==null : o.equals(e))</tt> 时返回 <tt>true</tt>。”此规范不应被解释为调用 <tt>Collection.contains</tt> 时会使用非 null 参数 <tt>o</tt> 调用 <tt>o.equals(e)</tt>。实现可以自由地实现优化，以避免调用 <tt>equals</tt>，例如，首先比较两个元素的哈希码。（{@link Object#hashCode()} 规范保证两个哈希码不相等的对象不能相等。）更一般地说，集合框架接口的各种实现可以自由地利用底层 {@link Object} 方法的指定行为，只要实现者认为合适。
 *
 * <p>一些执行集合递归遍历的集合操作可能因自引用实例而失败，其中集合直接或间接包含自身。这包括 {@code clone()}、{@code equals()}、{@code hashCode()} 和 {@code toString()} 方法。实现可以选择处理自引用场景，但大多数当前实现不这样做。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @implSpec
 * 默认方法实现（继承或其他方式）不应用任何同步协议。如果 {@code Collection} 实现有特定的同步协议，则必须重写默认实现以应用该协议。
 *
 * @param <E> 此集合中的元素类型
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Set
 * @see     List
 * @see     Map
 * @see     SortedSet
 * @see     SortedMap
 * @see     HashSet
 * @see     TreeSet
 * @see     ArrayList
 * @see     LinkedList
 * @see     Vector
 * @see     Collections
 * @see     Arrays
 * @see     AbstractCollection
 * @since 1.2
 */


public interface Collection<E> extends Iterable<E> {
    // 查询操作

    /**
     * 返回此集合中的元素数量。如果此集合包含的元素数量超过 <tt>Integer.MAX_VALUE</tt>，则返回 <tt>Integer.MAX_VALUE</tt>。
     *
     * @return 此集合中的元素数量
     */
    int size();

    /**
     * 如果此集合不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此集合不包含任何元素，则返回 <tt>true</tt>
     */
    boolean isEmpty();

    /**
     * 如果此集合包含指定的元素，则返回 <tt>true</tt>。更正式地说，当且仅当此集合包含至少一个元素 <tt>e</tt> 使得 <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt> 时，返回 <tt>true</tt>。
     *
     * @param o 要测试其在此集合中是否存在性的元素
     * @return 如果此集合包含指定的元素，则返回 <tt>true</tt>
     * @throws ClassCastException 如果指定元素的类型与此集合不兼容
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     *         (<a href="#optional-restrictions">可选</a>)
     */
    boolean contains(Object o);

    /**
     * 返回一个迭代器，用于遍历此集合中的元素。不保证元素的返回顺序（除非此集合是某个提供保证的类的实例）。
     *
     * @return 一个遍历此集合中元素的 <tt>Iterator</tt>
     */
    Iterator<E> iterator();

    /**
     * 返回一个包含此集合中所有元素的数组。如果此集合对其迭代器返回的元素顺序有保证，此方法必须按相同的顺序返回元素。
     *
     * <p>返回的数组是“安全的”，即此集合不保留对它的任何引用。（换句话说，此方法必须分配一个新数组，即使此集合是由数组支持的）。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此集合中所有元素的数组
     */
    Object[] toArray();

    /**
     * 返回一个包含此集合中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。如果集合适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为此集合大小的新数组。
     *
     * <p>如果此集合适合指定的数组并且有剩余空间（即，数组的元素多于此集合的元素），则紧接在集合末尾之后的数组元素被设置为 <tt>null</tt>。（这仅在调用者知道此集合不包含任何 <tt>null</tt> 元素时，用于确定此集合的长度。）
     *
     * <p>如果此集合对其迭代器返回的元素顺序有保证，此方法必须按相同的顺序返回元素。
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用于节省分配成本。
     *
     * <p>假设 <tt>x</tt> 是一个已知只包含字符串的集合。以下代码可以用于将集合转储到一个新分配的 <tt>String</tt> 数组中：
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * 注意，<tt>toArray(new Object[0])</tt> 与 <tt>toArray()</tt> 的功能相同。
     *
     * @param <T> 输出数组的运行时类型
     * @param a 如果足够大，则将此集合的元素存储到此数组中；否则，为此目的分配一个相同运行时类型的新数组。
     * @return 一个包含此集合中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此集合中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    <T> T[] toArray(T[] a);

    // 修改操作

    /**
     * 确保此集合包含指定的元素（可选操作）。如果此调用导致集合发生变化，则返回 <tt>true</tt>。（如果此集合不允许重复并且已经包含指定的元素，则返回 <tt>false</tt>。）<p>
     *
     * 支持此操作的集合可能对可以添加到此集合的元素施加限制。特别是，某些集合将拒绝添加 <tt>null</tt> 元素，而其他集合将对可以添加的元素类型施加限制。集合类应在文档中明确说明对可以添加的元素的任何限制。<p>
     *
     * 如果集合由于任何原因拒绝添加特定元素（除了它已经包含该元素），它 <i>必须</i> 抛出异常（而不是返回 <tt>false</tt>）。这保留了集合在调用返回后始终包含指定元素的不变性。
     *
     * @param e 要确保其存在于此集合中的元素
     * @return 如果此调用导致集合发生变化，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>add</tt> 操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此集合
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     * @throws IllegalArgumentException 如果元素的某些属性阻止其被添加到此集合
     * @throws IllegalStateException 如果由于插入限制，元素此时无法被添加
     */
    boolean add(E e);

    /**
     * 从此集合中移除指定元素的一个实例（可选操作），如果该元素存在。更正式地说，移除一个元素 <tt>e</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，如果此集合包含一个或多个这样的元素。如果
     * 此集合包含指定的元素（或等效地，如果此集合因调用而改变），则返回 <tt>true</tt>。
     *
     * @param o 要从此集合中移除的元素，如果存在
     * @return <tt>true</tt> 如果由于此调用移除了一个元素
     * @throws ClassCastException 如果指定元素的类型与此集合不兼容
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>remove</tt> 操作
     */
    boolean remove(Object o);


    // 批量操作

    /**
     * 如果此集合包含指定集合中的所有元素，则返回 <tt>true</tt>。
     *
     * @param  c 要检查是否包含在此集合中的集合
     * @return <tt>true</tt> 如果此集合包含指定集合中的所有元素
     * @throws ClassCastException 如果指定集合中的一个或多个元素的类型与此集合不兼容
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定集合包含一个或多个 null 元素且此集合不允许 null 元素
     *         (<a href="#optional-restrictions">可选</a>)，
     *         或指定集合为 null。
     * @see    #contains(Object)
     */
    boolean containsAll(Collection<?> c);

    /**
     * 将指定集合中的所有元素添加到此集合中（可选操作）。如果在操作进行过程中指定的集合被修改，则此操作的行为是未定义的。
     * （这意味着，如果指定的集合是此集合，并且此集合非空，则此调用的行为是未定义的。）
     *
     * @param c 包含要添加到此集合中的元素的集合
     * @return <tt>true</tt> 如果此集合因调用而改变
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>addAll</tt> 操作
     * @throws ClassCastException 如果指定集合中的元素的类阻止其被添加到此集合
     * @throws NullPointerException 如果指定集合包含一个 null 元素且此集合不允许 null 元素，
     *         或指定集合为 null
     * @throws IllegalArgumentException 如果指定集合中的元素的某个属性阻止其被添加到此集合
     * @throws IllegalStateException 如果由于插入限制，不是所有元素都能在此时被添加
     * @see #add(Object)
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * 移除此集合中也包含在指定集合中的所有元素（可选操作）。此调用返回后，此集合将不再包含与指定集合共有的任何元素。
     *
     * @param c 包含要从此集合中移除的元素的集合
     * @return <tt>true</tt> 如果此集合因调用而改变
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>removeAll</tt> 方法
     * @throws ClassCastException 如果此集合中的一个或多个元素的类型与指定集合不兼容
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此集合包含一个或多个 null 元素且指定集合不支持 null 元素
     *         (<a href="#optional-restrictions">可选</a>)，
     *         或指定集合为 null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean removeAll(Collection<?> c);

    /**
     * 移除此集合中满足给定谓词的所有元素。迭代或谓词抛出的错误或运行时异常将传递给调用者。
     *
     * @implSpec
     * 默认实现使用其 {@link #iterator} 遍历集合的所有元素。每个匹配的元素使用
     * {@link Iterator#remove()} 移除。如果集合的迭代器不支持移除，则在第一个匹配的元素上将抛出
     * {@code UnsupportedOperationException}。
     *
     * @param filter 一个谓词，对于要移除的元素返回 {@code true}
     * @return {@code true} 如果移除了任何元素
     * @throws NullPointerException 如果指定的过滤器为 null
     * @throws UnsupportedOperationException 如果无法从此集合中移除元素。实现可能在无法移除匹配元素或通常不支持移除时抛出此异常。
     * @since 1.8
     */
    default boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }


                /**
     * 保留此集合中包含在指定集合中的元素（可选操作）。换句话说，从
     * 此集合中移除所有不在指定集合中的元素。
     *
     * @param c 包含要在此集合中保留的元素的集合
     * @return 如果此集合因调用而发生更改，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>retainAll</tt> 操作
     * @throws ClassCastException 如果此集合中的一个或多个元素类型与指定
     *         集合不兼容
     *         (<a href="#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此集合包含一个或多个 null 元素，而指定集合不允许 null
     *         元素
     *         (<a href="#optional-restrictions">可选</a>)，
     *         或者指定的集合为 null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean retainAll(Collection<?> c);

    /**
     * 从此集合中移除所有元素（可选操作）。调用此方法后，集合将为空。
     *
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>clear</tt> 操作
     */
    void clear();


    // 比较和哈希

    /**
     * 将指定对象与此集合进行相等性比较。 <p>
     *
     * 虽然 <tt>Collection</tt> 接口没有对 <tt>Object.equals</tt> 的通用契约添加任何限制，
     * 但直接实现 <tt>Collection</tt> 接口（换句话说，创建一个类是 <tt>Collection</tt> 但不是 <tt>Set</tt>
     * 或 <tt>List</tt>）的程序员在选择重写 <tt>Object.equals</tt> 时必须小心。不需要这样做，最简单的做法是依赖 <tt>Object</tt> 的实现，
     * 但实现者可能希望实现“值比较”而不是默认的“引用比较”。（<tt>List</tt> 和 <tt>Set</tt> 接口要求进行这样的值比较。）<p>
     *
     * <tt>Object.equals</tt> 方法的通用契约规定，equals 必须是对称的（换句话说，<tt>a.equals(b)</tt> 当且仅当 <tt>b.equals(a)</tt>）。
     * <tt>List.equals</tt> 和 <tt>Set.equals</tt> 的契约规定，列表仅与其他列表相等，集合仅与其他集合相等。因此，既不实现 <tt>List</tt> 也不实现
     * <tt>Set</tt> 接口的集合类的自定义 <tt>equals</tt> 方法在将此集合与任何列表或集合进行比较时必须返回 <tt>false</tt>。（同理，不可能正确实现同时实现 <tt>Set</tt> 和 <tt>List</tt> 接口的类。）
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象与此集合相等，则返回 <tt>true</tt>
     *
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     * @see List#equals(Object)
     */
    boolean equals(Object o);

    /**
     * 返回此集合的哈希码值。虽然 <tt>Collection</tt> 接口没有对 <tt>Object.hashCode</tt> 方法的通用契约添加任何限制，
     * 但程序员应注意，任何重写 <tt>Object.equals</tt> 方法的类也必须重写 <tt>Object.hashCode</tt> 方法，以满足 <tt>Object.hashCode</tt> 方法的通用契约。
     * 特别是，<tt>c1.equals(c2)</tt> 意味着 <tt>c1.hashCode()==c2.hashCode()</tt>。
     *
     * @return 此集合的哈希码值
     *
     * @see Object#hashCode()
     * @see Object#equals(Object)
     */
    int hashCode();

    /**
     * 创建一个遍历此集合元素的 {@link Spliterator}。
     *
     * 实现应记录 spliterator 报告的特征值。如果 spliterator 报告 {@link Spliterator#SIZED} 且此集合不包含任何元素，
     * 则不要求报告这些特征值。
     *
     * <p>子类应覆盖默认实现，以返回更高效的 spliterator。为了
     * 保留 {@link #stream()} 和 {@link #parallelStream()} 方法的预期惰性行为，spliterators 应具有
     * {@code IMMUTABLE} 或 {@code CONCURRENT} 特征，或为 <em><a href="Spliterator.html#binding">延迟绑定</a></em>。
     * 如果这些都不实际，覆盖类应描述 spliterator 的绑定和结构干扰的文档策略，并覆盖 {@link #stream()} 和 {@link #parallelStream()}
     * 方法，使用 spliterator 的 {@code Supplier} 创建流，如下所示：
     * <pre>{@code
     *     Stream<E> s = StreamSupport.stream(() -> spliterator(), spliteratorCharacteristics)
     * }</pre>
     * <p>这些要求确保由 {@link #stream()} 和 {@link #parallelStream()} 方法生成的流将反映终端流操作启动时集合的内容。
     *
     * @implSpec
     * 默认实现从集合的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em> spliterator。该 spliterator 继承了集合迭代器的
     * <em>快速失败</em> 特性。
     * <p>
     * 创建的 {@code Spliterator} 报告 {@link Spliterator#SIZED}。
     *
     * @implNote
     * 创建的 {@code Spliterator} 还报告 {@link Spliterator#SUBSIZED}。
     *
     * <p>如果 spliterator 不覆盖任何元素，则报告额外的特征值，除了 {@code SIZED} 和 {@code SUBSIZED} 之外，不会帮助客户端控制、专门化或简化计算。
     * 但是，这确实允许空集合共享一个不可变且空的 spliterator 实例（参见 {@link Spliterators#emptySpliterator()}），并允许客户端确定这样的 spliterator 是否不覆盖任何元素。
     *
     * @return 一个遍历此集合元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }


                /**
     * 返回以此集合为源的顺序 {@code Stream}。
     *
     * <p>当 {@link #spliterator()} 方法不能返回一个 {@code IMMUTABLE}、
     * {@code CONCURRENT} 或 <em>延迟绑定</em> 的 spliterator 时，应覆盖此方法。 (参见 {@link #spliterator()}
     * 了解详情。)
     *
     * @implSpec
     * 默认实现从集合的 {@code Spliterator} 创建一个顺序 {@code Stream}。
     *
     * @return 以此集合中的元素为源的顺序 {@code Stream}
     * @since 1.8
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * 返回以此集合为源的可能并行的 {@code Stream}。此方法可以返回一个顺序流。
     *
     * <p>当 {@link #spliterator()} 方法不能返回一个 {@code IMMUTABLE}、
     * {@code CONCURRENT} 或 <em>延迟绑定</em> 的 spliterator 时，应覆盖此方法。 (参见 {@link #spliterator()}
     * 了解详情。)
     *
     * @implSpec
     * 默认实现从集合的 {@code Spliterator} 创建一个并行 {@code Stream}。
     *
     * @return 以此集合中的元素为源的可能并行的 {@code Stream}
     * @since 1.8
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
