
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * <p>哈希表和链表实现的 <tt>Set</tt> 接口，具有可预测的迭代顺序。此实现与 <tt>HashSet</tt> 不同之处在于它维护了一个贯穿所有条目的双向链表。此链表定义了迭代顺序，即元素插入到集合中的顺序（<i>插入顺序</i>）。请注意，如果元素被<i>重新插入</i>到集合中，插入顺序<i>不会</i>受到影响。（当调用 <tt>s.add(e)</tt> 时，如果 <tt>s.contains(e)</tt> 会立即返回 <tt>true</tt>，则认为元素 <tt>e</tt> 被重新插入到集合 <tt>s</tt> 中。）
 *
 * <p>此实现使其客户端免受 {@link HashSet} 提供的未指定的、通常混乱的顺序的影响，而不会带来与 {@link TreeSet} 相关的额外成本。它可以用于生成一个具有与原始集合相同顺序的副本，无论原始集合的实现如何：
 * <pre>
 *     void foo(Set s) {
 *         Set copy = new LinkedHashSet(s);
 *         ...
 *     }
 * </pre>
 * 如果一个模块接收一个集合作为输入，复制它，并在稍后返回结果，而结果的顺序由副本的顺序决定，此技术特别有用。（客户端通常希望以相同的顺序返回事物。）
 *
 * <p>此类提供了所有可选的 <tt>Set</tt> 操作，并允许 null 元素。像 <tt>HashSet</tt> 一样，它为基本操作（<tt>add</tt>、<tt>contains</tt> 和 <tt>remove</tt>）提供了常数时间性能，前提是哈希函数在桶中均匀分布元素。性能可能略低于 <tt>HashSet</tt>，因为维护链表需要额外的开销，但有一个例外：迭代 <tt>LinkedHashSet</tt> 的时间与集合的<i>大小</i>成正比，而与容量无关。迭代 <tt>HashSet</tt> 可能更昂贵，需要的时间与<i>容量</i>成正比。
 *
 * <p>链式哈希集有两个影响其性能的参数：<i>初始容量</i>和<i>加载因子</i>。它们的定义与 <tt>HashSet</tt> 完全相同。但是，选择过高的初始容量值对这个类的惩罚比对 <tt>HashSet</tt> 的惩罚要轻，因为此类的迭代时间不受容量的影响。
 *
 * <p><strong>请注意，此实现不是同步的。</strong> 如果多个线程同时访问链式哈希集，并且至少有一个线程修改了集合，它<i>必须</i>从外部同步。这通常通过在自然封装集合的某个对象上同步来完成。
 *
 * 如果没有这样的对象，应该使用 {@link Collections#synchronizedSet Collections.synchronizedSet} 方法将集合“包装”起来。最好在创建时这样做，以防止集合被意外地非同步访问：<pre>
 *   Set s = Collections.synchronizedSet(new LinkedHashSet(...));</pre>
 *
 * <p>此类的 <tt>iterator</tt> 方法返回的迭代器是<em>快速失败</em>的：如果在迭代器创建后以任何方式修改集合，除了通过迭代器自己的 <tt>remove</tt> 方法，迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，迭代器会快速且干净地失败，而不是在未来的某个不确定时间冒任意、非确定性行为的风险。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为通常来说，在存在未同步的并发修改的情况下，无法做出任何硬性保证。快速失败的迭代器在尽力而为的基础上抛出 <tt>ConcurrentModificationException</tt>。因此，编写依赖于此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @param <E> 由该集合维护的元素类型
 *
 * @author  Josh Bloch
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Set
 * @see     HashSet
 * @see     TreeSet
 * @see     Hashtable
 * @since   1.4
 */

public class LinkedHashSet<E>
    extends HashSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2851667679971038690L;

    /**
     * 构造一个新的、空的链式哈希集，具有指定的初始容量和加载因子。
     *
     * @param      initialCapacity 链式哈希集的初始容量
     * @param      loadFactor      链式哈希集的加载因子
     * @throws     IllegalArgumentException  如果初始容量小于零，或加载因子非正
     */
    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    /**
     * 构造一个新的、空的链式哈希集，具有指定的初始容量和默认加载因子（0.75）。
     *
     * @param   initialCapacity   链式哈希集的初始容量
     * @throws  IllegalArgumentException 如果初始容量小于零
     */
    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, .75f, true);
    }

    /**
     * 构造一个新的、空的链式哈希集，具有默认的初始容量（16）和加载因子（0.75）。
     */
    public LinkedHashSet() {
        super(16, .75f, true);
    }

                /**
     * 构造一个包含指定集合相同元素的新链接哈希集。此链接哈希集的初始容量足以容纳指定集合中的元素，并且具有默认负载因子（0.75）。
     *
     * @param c  要放置到此集合中的元素的集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public LinkedHashSet(Collection<? extends E> c) {
        super(Math.max(2*c.size(), 11), .75f, true);
        addAll(c);
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@code Spliterator}，用于遍历此集合中的元素。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED}，
     * {@link Spliterator#DISTINCT} 和 {@code ORDERED}。实现应记录额外的特征值。
     *
     * @implNote
     * 实现从集合的 {@code Iterator} 创建一个
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em> 的 spliterator。
     * 该 spliterator 继承了集合迭代器的 <em>快速失败</em> 属性。
     * 创建的 {@code Spliterator} 还报告 {@link Spliterator#SUBSIZED}。
     *
     * @return 一个遍历此集合中元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
    }
}
