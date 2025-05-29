
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个不包含重复元素的集合。更正式地说，集合不包含任何元素对 <code>e1</code> 和 <code>e2</code> 使得
 * <code>e1.equals(e2)</code>，并且最多包含一个 null 元素。正如其名称所暗示的，此接口建模了数学上的 <i>集合</i> 抽象。
 *
 * <p><tt>Set</tt> 接口在所有构造函数以及 <tt>add</tt>、<tt>equals</tt> 和 <tt>hashCode</tt> 方法的契约上，
 * 除了继承自 <tt>Collection</tt> 接口的契约外，还附加了额外的限制。为了方便起见，这里也包括了其他继承方法的声明。
 * （这些声明的规范已经针对 <tt>Set</tt> 接口进行了调整，但没有包含任何额外的限制。）
 *
 * <p>构造函数的额外限制是，所有构造函数必须创建一个不包含重复元素（如上所述）的集合。
 *
 * <p>注意：如果使用可变对象作为集合元素，必须非常小心。如果在对象作为集合元素时，其值以影响 <tt>equals</tt> 比较的方式发生变化，
 * 则集合的行为是未指定的。这一禁止的特殊情况是，集合不允许包含自身作为元素。
 *
 * <p>某些集合实现对其可能包含的元素有特定的限制。例如，某些实现禁止 null 元素，而某些实现对其元素的类型有特定的限制。
 * 尝试添加不合格的元素会抛出一个未检查的异常，通常是 <tt>NullPointerException</tt> 或 <tt>ClassCastException</tt>。
 * 尝试查询不合格元素的存在可能会抛出异常，或者可能简单地返回 false；某些实现会表现出前者的行为，而某些实现会表现出后者的行为。
 * 更一般地，尝试对不合格元素执行不会导致将不合格元素插入集合的操作可能会抛出异常，或者可能会成功，具体取决于实现。
 * 这样的异常在本接口的规范中被标记为“可选”。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @param <E> 此集合维护的元素类型
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see List
 * @see SortedSet
 * @see HashSet
 * @see TreeSet
 * @see AbstractSet
 * @see Collections#singleton(java.lang.Object)
 * @see Collections#EMPTY_SET
 * @since 1.2
 */

public interface Set<E> extends Collection<E> {
    // 查询操作

    /**
     * 返回此集合中的元素数量（其基数）。如果此集合包含的元素数量超过 <tt>Integer.MAX_VALUE</tt>，则返回
     * <tt>Integer.MAX_VALUE</tt>。
     *
     * @return 此集合中的元素数量（其基数）
     */
    int size();

    /**
     * 如果此集合不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此集合不包含任何元素，则返回 <tt>true</tt>
     */
    boolean isEmpty();

    /**
     * 如果此集合包含指定的元素，则返回 <tt>true</tt>。更正式地说，如果且仅当此集合包含一个元素 <tt>e</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 <tt>true</tt>。
     *
     * @param o 要测试其是否存在于此集合中的元素
     * @return 如果此集合包含指定的元素，则返回 <tt>true</tt>
     * @throws ClassCastException 如果指定元素的类型与此集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此集合不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean contains(Object o);

    /**
     * 返回一个迭代器，用于遍历此集合中的元素。元素的返回顺序没有特定的顺序（除非此集合是某个提供保证的类的实例）。
     *
     * @return 一个迭代器，用于遍历此集合中的元素
     */
    Iterator<E> iterator();

    /**
     * 返回一个包含此集合中所有元素的数组。如果此集合对其迭代器返回元素的顺序有任何保证，则此方法必须按相同的顺序返回元素。
     *
     * <p>返回的数组是“安全的”，即此集合不会维护对它的任何引用。（换句话说，此方法必须分配一个新的数组，即使此集合是由数组支持的。）
     * 因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此集合中所有元素的数组
     */
    Object[] toArray();

    /**
     * 返回一个包含此集合中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。如果集合适合指定的数组，则返回该数组。
     * 否则，分配一个新的数组，其运行时类型是指定数组的运行时类型，大小为此集合的大小。
     *
     * <p>如果此集合适合指定的数组且有剩余空间（即，数组的元素多于此集合的元素），则数组中紧接集合末尾的元素被设置为
     * <tt>null</tt>。（这仅在调用者知道此集合不包含任何 null 元素时，用于确定此集合的长度。）
     *
     * <p>如果此集合对其迭代器返回元素的顺序有任何保证，则此方法必须按相同的顺序返回元素。
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，
 * 并且在某些情况下，可以用于节省分配成本。
     *
     * <p>假设 <tt>x</tt> 是一个已知只包含字符串的集合。以下代码可以用于将集合转储到新分配的 <tt>String</tt> 数组中：
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * 注意，<tt>toArray(new Object[0])</tt> 在功能上等同于 <tt>toArray()</tt>。
     *
     * @param a 如果足够大，则用于存储此集合元素的数组；否则，为此目的分配一个相同运行时类型的数组。
     * @return 一个包含此集合中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此集合中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    <T> T[] toArray(T[] a);

    // 修改操作

    /**
     * 如果指定的元素尚未存在，则将其添加到此集合中（可选操作）。更正式地说，如果集合中没有元素<tt>e2</tt>使得
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>，则将指定的元素<tt>e</tt>添加到此集合中。
     * 如果此集合已包含该元素，则调用将使集合保持不变并返回<tt>false</tt>。结合对构造函数的限制，这确保集合永远不会包含重复的元素。
     *
     * <p>上述规定并不意味着集合必须接受所有元素；集合可以拒绝添加任何特定元素，包括<tt>null</tt>，并抛出异常，如
     * {@link Collection#add Collection.add}的规范所述。各个集合实现应明确记录它们可能包含的元素的任何限制。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果此集合之前不包含指定的元素，则返回<tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持<tt>add</tt>操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此集合中
     * @throws NullPointerException 如果指定的元素为null且此集合不允许null元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此集合中
     */
    boolean add(E e);


    /**
     * 如果指定的元素存在，则从此集合中移除（可选操作）。更正式地说，移除一个元素<tt>e</tt>，使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，如果此集合包含这样的元素。如果此集合包含该元素（或等效地，如果此调用导致集合发生变化），则返回<tt>true</tt>。（此调用返回后，此集合将不再包含该元素。）
     *
     * @param o 如果存在，要从此集合中移除的对象
     * @return 如果此集合包含指定的元素，则返回<tt>true</tt>
     * @throws ClassCastException 如果指定元素的类型与此集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为null且此集合不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws UnsupportedOperationException 如果此集合不支持<tt>remove</tt>操作
     */
    boolean remove(Object o);


    // 批量操作

    /**
     * 如果此集合包含指定集合中的所有元素，则返回<tt>true</tt>。如果指定的集合也是一个集合，此方法返回<tt>true</tt>，如果它是此集合的<i>子集</i>。
     *
     * @param  c 要检查是否包含在此集合中的集合
     * @return 如果此集合包含指定集合中的所有元素，则返回<tt>true</tt>
     * @throws ClassCastException 如果指定集合中的一个或多个元素的类型与此集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定集合包含一个或多个null元素且此集合不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，或者指定的集合为null
     * @see    #contains(Object)
     */
    boolean containsAll(Collection<?> c);

    /**
     * 如果它们尚未存在，则将指定集合中的所有元素添加到此集合中（可选操作）。如果指定的集合也是一个集合，<tt>addAll</tt>操作实际上
     * 会修改此集合，使其值为两个集合的<i>并集</i>。如果在操作进行过程中指定的集合被修改，此操作的行为是未定义的。
     *
     * @param  c 包含要添加到此集合中的元素的集合
     * @return 如果此调用导致集合发生变化，则返回<tt>true</tt>
     *
     * @throws UnsupportedOperationException 如果此集合不支持<tt>addAll</tt>操作
     * @throws ClassCastException 如果指定集合中的元素的类阻止其被添加到此集合中
     * @throws NullPointerException 如果指定集合包含一个或多个null元素且此集合不允许null元素，或者指定的集合为null
     * @throws IllegalArgumentException 如果指定集合中的元素的某些属性阻止其被添加到此集合中
     * @see #add(Object)
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * 仅保留此集合中包含在指定集合中的元素（可选操作）。换句话说，从此集合中移除所有不包含在指定集合中的元素。如果指定的集合也是一个集合，
     * 此操作实际上会修改此集合，使其值为两个集合的<i>交集</i>。
     *
     * @param  c 包含要在此集合中保留的元素的集合
     * @return 如果此调用导致集合发生变化，则返回<tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持<tt>retainAll</tt>操作
     * @throws ClassCastException 如果此集合中的元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此集合包含一个null元素且指定集合不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，或者指定的集合为null
     * @see #remove(Object)
     */
    boolean retainAll(Collection<?> c);

                /**
     * 从该集合中移除所有包含在指定集合中的元素（可选操作）。如果指定的集合也是一个集合，此操作实际上会修改此集合，
     * 使其值为两个集合的<i>非对称集合差</i>。
     *
     * @param  c 包含要从此集合中移除的元素的集合
     * @return 如果此集合因调用而改变，则返回<tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持<tt>removeAll</tt>操作
     * @throws ClassCastException 如果此集合中的元素类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此集合包含null元素而指定集合不允许null元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，或者指定的集合为null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean removeAll(Collection<?> c);

    /**
     * 从此集合中移除所有元素（可选操作）。此调用返回后，集合将为空。
     *
     * @throws UnsupportedOperationException 如果此集合不支持<tt>clear</tt>方法
     */
    void clear();


    // 比较和哈希

    /**
     * 将指定对象与此集合进行相等性比较。如果指定对象也是一个集合，两个集合大小相同，且指定集合中的每个成员都包含在此集合中
     * （或等效地，此集合中的每个成员都包含在指定集合中），则返回<tt>true</tt>。此定义确保equals方法在集合接口的不同实现之间正常工作。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象等于此集合，则返回<tt>true</tt>
     */
    boolean equals(Object o);

    /**
     * 返回此集合的哈希码值。集合的哈希码定义为集合中元素的哈希码之和，其中<tt>null</tt>元素的哈希码定义为零。
     * 这确保了对于任何两个集合<tt>s1</tt>和<tt>s2</tt>，<tt>s1.equals(s2)</tt>意味着
     * <tt>s1.hashCode()==s2.hashCode()</tt>，这是由{@link Object#hashCode}的一般契约要求的。
     *
     * @return 此集合的哈希码值
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    int hashCode();

    /**
     * 创建一个遍历此集合元素的{@code Spliterator}。
     *
     * <p>该{@code Spliterator}报告{@link Spliterator#DISTINCT}。实现应记录额外的特性值。
     *
     * @implSpec
     * 默认实现从集合的{@code Iterator}创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>的spliterator。该spliterator继承了集合迭代器的
     * <em>快速失败</em>特性。
     * <p>
     * 创建的{@code Spliterator}还报告
     * {@link Spliterator#SIZED}。
     *
     * @implNote
     * 创建的{@code Spliterator}还报告
     * {@link Spliterator#SUBSIZED}。
     *
     * @return 一个遍历此集合元素的{@code Spliterator}
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT);
    }
}
