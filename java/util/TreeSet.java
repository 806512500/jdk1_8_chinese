
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
 * 基于 {@link TreeMap} 的 {@link NavigableSet} 实现。
 * 元素使用其 {@linkplain Comparable 自然顺序} 进行排序，或者在创建集合时提供一个 {@link Comparator} 进行排序，具体取决于使用哪个构造函数。
 *
 * <p>此实现保证了基本操作（{@code add}、{@code remove} 和 {@code contains}）的时间成本为 log(n)。
 *
 * <p>请注意，集合维护的顺序（无论是否提供了显式的比较器）必须与 {@code equals} <i>一致</i>，以正确实现 {@code Set} 接口。（参见 {@code Comparable}
 * 或 {@code Comparator} 以获取 <i>与 equals 一致</i> 的精确定义。）这是因为 {@code Set} 接口是根据 {@code equals} 操作定义的，
 * 但 {@code TreeSet} 实例使用其 {@code compareTo}（或 {@code compare}）方法执行所有元素比较，因此通过此方法认为相等的两个元素
 * 从集合的角度来看是相等的。即使集合的顺序与 equals 不一致，集合的行为也是明确定义的；它只是未能遵守 {@code Set} 接口的一般约定。
 *
 * <p><strong>请注意，此实现未同步。</strong>
 * 如果多个线程同时访问一个树集，并且至少有一个线程修改了集合，则 <i>必须</i> 从外部进行同步。
 * 通常通过在自然封装集合的某个对象上进行同步来实现。如果不存在这样的对象，应该使用
 * {@link Collections#synchronizedSortedSet Collections.synchronizedSortedSet} 方法“包装”集合。
 * 最好在创建时进行，以防止集合的意外未同步访问： <pre>
 *   SortedSet s = Collections.synchronizedSortedSet(new TreeSet(...));</pre>
 *
 * <p>此类的 {@code iterator} 方法返回的迭代器是 <i>快速失败</i> 的：如果在创建迭代器后以任何方式修改集合（除了通过迭代器自身的 {@code remove}
 * 方法），迭代器将抛出 {@link ConcurrentModificationException}。因此，在并发修改的情况下，迭代器会快速且干净地失败，而不是在未来的某个不确定时间
 * 冒险出现任意的、不确定的行为。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为通常来说，在未同步的并发修改存在的情况下，无法做出任何硬性保证。快速失败迭代器在尽力的基础上抛出
 * {@code ConcurrentModificationException}。因此，编写依赖此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 由该集合维护的元素类型
 *
 * @author  Josh Bloch
 * @see     Collection
 * @see     Set
 * @see     HashSet
 * @see     Comparable
 * @see     Comparator
 * @see     TreeMap
 * @since   1.2
 */

public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    /**
     * 支撑的映射。
     */
    private transient NavigableMap<E,Object> m;

    // 与支撑映射中的对象关联的虚拟值
    private static final Object PRESENT = new Object();

    /**
     * 构造由指定的可导航映射支持的集合。
     */
    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

    /**
     * 构造一个新的、空的树集，根据其元素的自然顺序进行排序。插入到集合中的所有元素都必须实现 {@link Comparable} 接口。
     * 此外，所有这些元素必须是 <i>相互可比较的</i>：对于集合中的任何元素 {@code e1} 和 {@code e2}，{@code e1.compareTo(e2)} 不得抛出
     * {@code ClassCastException}。如果用户尝试添加一个违反此约束的元素（例如，用户尝试将一个字符串元素添加到元素为整数的集合中），则 {@code add} 调用将抛出
     * {@code ClassCastException}。
     */
    public TreeSet() {
        this(new TreeMap<E,Object>());
    }

    /**
     * 构造一个新的、空的树集，根据指定的比较器进行排序。插入到集合中的所有元素都必须由指定的比较器 <i>相互可比较的</i>：
     * 对于集合中的任何元素 {@code e1} 和 {@code e2}，{@code comparator.compare(e1, e2)} 不得抛出 {@code ClassCastException}。
     * 如果用户尝试添加一个违反此约束的元素，则 {@code add} 调用将抛出 {@code ClassCastException}。
     *
     * @param comparator 将用于对集合进行排序的比较器。如果为 {@code null}，则使用元素的 {@linkplain Comparable 自然顺序}。
     */
    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }

    /**
     * 构造一个新的树集，包含指定集合中的元素，根据其元素的 <i>自然顺序</i> 进行排序。插入到集合中的所有元素都必须实现
     * {@link Comparable} 接口。此外，所有这些元素必须是 <i>相互可比较的</i>：对于集合中的任何元素 {@code e1} 和 {@code e2}，
     * {@code e1.compareTo(e2)} 不得抛出 {@code ClassCastException}。
     *
     * @param c 其元素将组成新集合的集合
     * @throws ClassCastException 如果 {@code c} 中的元素不是 {@link Comparable}，或者不是相互可比较的
     * @throws NullPointerException 如果指定的集合为 null
     */
    public TreeSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }


                /**
     * 构造一个新的树集，包含与指定的排序集相同的元素，并使用相同的排序。
     *
     * @param s 排序集，其元素将组成新集
     * @throws NullPointerException 如果指定的排序集为 null
     */
    public TreeSet(SortedSet<E> s) {
        this(s.comparator());
        addAll(s);
    }

    /**
     * 返回一个迭代器，按升序遍历此集中的元素。
     *
     * @return 一个迭代器，按升序遍历此集中的元素
     */
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }

    /**
     * 返回一个迭代器，按降序遍历此集中的元素。
     *
     * @return 一个迭代器，按降序遍历此集中的元素
     * @since 1.6
     */
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }

    /**
     * @since 1.6
     */
    public NavigableSet<E> descendingSet() {
        return new TreeSet<>(m.descendingMap());
    }

    /**
     * 返回此集中的元素数量（其基数）。
     *
     * @return 此集中的元素数量（其基数）
     */
    public int size() {
        return m.size();
    }

    /**
     * 如果此集不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此集不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /**
     * 如果此集包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果且仅当此集包含一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此集中的对象
     * @return 如果此集包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定的对象不能与当前集中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集使用自然排序，或者其比较器不允许 null 元素
     */
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    /**
     * 如果指定的元素尚未存在，则将其添加到此集中。
     * 更正式地说，如果此集不包含一个元素 {@code e2} 使得
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>，则将指定的元素 {@code e} 添加到此集中。
     * 如果此集已经包含该元素，则调用将使集保持不变并返回 {@code false}。
     *
     * @param e 要添加到此集中的元素
     * @return 如果此集之前不包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定的对象不能与当前集中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集使用自然排序，或者其比较器不允许 null 元素
     */
    public boolean add(E e) {
        return m.put(e, PRESENT)==null;
    }

    /**
     * 如果此集中存在指定的元素，则将其移除。
     * 更正式地说，移除一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，
     * 如果此集包含这样的元素。如果此集包含该元素（或者等效地，如果此集因调用而改变），则返回 {@code true}。
     * （调用返回后，此集将不再包含该元素。）
     *
     * @param o 要从此集中移除的对象（如果存在）
     * @return 如果此集包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定的对象不能与当前集中的元素进行比较
     * @throws NullPointerException 如果指定的元素为 null
     *         并且此集使用自然排序，或者其比较器不允许 null 元素
     */
    public boolean remove(Object o) {
        return m.remove(o)==PRESENT;
    }

    /**
     * 从此集中移除所有元素。
     * 调用返回后，此集将为空。
     */
    public void clear() {
        m.clear();
    }

    /**
     * 将指定集合中的所有元素添加到此集中。
     *
     * @param c 包含要添加到此集中的元素的集合
     * @return 如果此集因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果提供的元素不能与当前集中的元素进行比较
     * @throws NullPointerException 如果指定的集合为 null 或
     *         任何元素为 null 并且此集使用自然排序，或者其比较器不允许 null 元素
     */
    public  boolean addAll(Collection<? extends E> c) {
        // 如果适用，使用线性时间版本
        if (m.size()==0 && c.size() > 0 &&
            c instanceof SortedSet &&
            m instanceof TreeMap) {
            SortedSet<? extends E> set = (SortedSet<? extends E>) c;
            TreeMap<E,Object> map = (TreeMap<E, Object>) m;
            Comparator<?> cc = set.comparator();
            Comparator<? super E> mc = map.comparator();
            if (cc==mc || (cc != null && cc.equals(mc))) {
                map.addAllForTreeSet(set, PRESENT);
                return true;
            }
        }
        return super.addAll(c);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 或 {@code toElement}
     *         为 null 并且此集使用自然排序，或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement,   boolean toInclusive) {
        return new TreeSet<>(m.subMap(fromElement, fromInclusive,
                                       toElement,   toInclusive));
    }

                /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toElement} 为 null 并且
     *         该集合使用自然排序，或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new TreeSet<>(m.headMap(toElement, inclusive));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 为 null 并且
     *         该集合使用自然排序，或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSet<>(m.tailMap(fromElement, inclusive));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 或
     *         {@code toElement} 为 null 并且该集合使用自然排序，
     *         或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toElement} 为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromElement} 为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    public Comparator<? super E> comparator() {
        return m.comparator();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E first() {
        return m.firstKey();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E last() {
        return m.lastKey();
    }

    // NavigableSet API 方法

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @since 1.6
     */
    public E lower(E e) {
        return m.lowerKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @since 1.6
     */
    public E floor(E e) {
        return m.floorKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @since 1.6
     */
    public E ceiling(E e) {
        return m.ceilingKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     *         并且该集合使用自然排序，或者其比较器不允许 null 元素
     * @since 1.6
     */
    public E higher(E e) {
        return m.higherKey(e);
    }

    /**
     * @since 1.6
     */
    public E pollFirst() {
        Map.Entry<E,?> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    /**
     * @since 1.6
     */
    public E pollLast() {
        Map.Entry<E,?> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }

    /**
     * 返回此 {@code TreeSet} 实例的浅拷贝。（元素本身不会被克隆。）
     *
     * @return 此集合的浅拷贝
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        TreeSet<E> clone;
        try {
            clone = (TreeSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        clone.m = new TreeMap<>(m);
        return clone;
    }

    /**
     * 将 {@code TreeSet} 实例的状态保存到流中（即，序列化它）。
     *
     * @serialData 发出用于对此集合进行排序的比较器，或者如果它遵循元素的自然排序则为
     *             {@code null}（Object），接着是集合的大小（集合包含的元素数量）（int），
     *             然后是所有元素（每个都是 Object）按顺序（由集合的 Comparator 确定，或者如果集合没有 Comparator，
     *             则由元素的自然排序确定）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // 写出任何隐藏的内容
        s.defaultWriteObject();

        // 写出比较器
        s.writeObject(m.comparator());

        // 写出大小
        s.writeInt(m.size());

        // 按正确的顺序写出所有元素。
        for (E e : m.keySet())
            s.writeObject(e);
    }

    /**
     * 从流中重新构造 {@code TreeSet} 实例（即，反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 读取任何隐藏的内容
        s.defaultReadObject();

        // 读取比较器
        @SuppressWarnings("unchecked")
            Comparator<? super E> c = (Comparator<? super E>) s.readObject();

        // 创建支持的 TreeMap
        TreeMap<E,Object> tm = new TreeMap<>(c);
        m = tm;
    }


                    // 读取大小
        int size = s.readInt();

        tm.readTreeSet(size, s, PRESENT);
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，遍历此集合中的元素。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#SIZED}，
     * {@link Spliterator#DISTINCT}，{@link Spliterator#SORTED} 和
     * {@link Spliterator#ORDERED}。覆盖实现应记录额外的特性值。
     *
     * <p>如果树集的比较器（见 {@link #comparator()}）为 {@code null}，
     * 则 spliterator 的比较器（见
     * {@link java.util.Spliterator#getComparator()}) 也为 {@code null}。
     * 否则，spliterator 的比较器与树集的比较器相同或施加相同的总顺序。
     *
     * @return 一个遍历此集合中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return TreeMap.keySpliteratorFor(m);
    }

    private static final long serialVersionUID = -2479143000061671589L;
}
