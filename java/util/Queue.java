/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释发布到公共领域。
 */

package java.util;

/**
 * 一个用于在处理前保存元素的集合。
 * 除了基本的 {@link java.util.Collection Collection} 操作外，
 * 队列还提供了额外的插入、提取和检查操作。每个这些方法都存在两种形式：一种在操作失败时抛出异常，
 * 另一种返回一个特殊值（根据操作的不同，可能是 {@code null} 或 {@code false}）。
 * 后一种插入操作的形式特别设计用于容量受限的 {@code Queue} 实现；在大多数实现中，插入操作不会失败。
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>队列方法概览</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>返回特殊值</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link Queue#add add(e)}</td>
 *    <td>{@link Queue#offer offer(e)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link Queue#remove remove()}</td>
 *    <td>{@link Queue#poll poll()}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link Queue#element element()}</td>
 *    <td>{@link Queue#peek peek()}</td>
 *  </tr>
 * </table>
 *
 * <p>队列通常，但不一定，以 FIFO（先进先出）的方式对元素进行排序。例外情况包括优先队列，它根据提供的比较器或元素的自然排序对元素进行排序，
 * 以及 LIFO 队列（或堆栈），它以 LIFO（后进先出）的方式对元素进行排序。无论使用何种排序方式，队列的<em>头部</em>都是通过调用 {@link #remove() }
 * 或 {@link #poll()} 将被移除的元素。在 FIFO 队列中，所有新元素都被插入到队列的<em>尾部</em>。其他类型的队列可能使用不同的放置规则。
 * 每个 {@code Queue} 实现都必须指定其排序属性。
 *
 * <p>{@link #offer offer} 方法在可能的情况下插入一个元素，否则返回 {@code false}。这与 {@link
 * java.util.Collection#add Collection.add} 方法不同，后者只能通过抛出一个未检查的异常来失败。{@code offer} 方法设计用于失败是正常情况，
 * 而不是异常情况，例如，在固定容量（或“有界”）队列中。
 *
 * <p>{@link #remove()} 和 {@link #poll()} 方法移除并返回队列的头部。
 * 究竟移除队列中的哪个元素取决于队列的排序策略，这在不同的实现中有所不同。{@code remove()} 和
 * {@code poll()} 方法的区别在于队列为空时的行为：{@code remove()} 方法抛出异常，
 * 而 {@code poll()} 方法返回 {@code null}。
 *
 * <p>{@link #element()} 和 {@link #peek()} 方法返回但不移除队列的头部。
 *
 * <p>{@code Queue} 接口没有定义并发编程中常见的<em>阻塞队列方法</em>。这些方法，
 * 会等待元素出现或空间变得可用，是在 {@link java.util.concurrent.BlockingQueue} 接口中定义的，
 * 该接口扩展了此接口。
 *
 * <p>{@code Queue} 实现通常不允许插入 {@code null} 元素，尽管某些实现，如
 * {@link LinkedList}，并不禁止插入 {@code null}。即使在允许插入的实现中，也不应将 {@code null}
 * 插入到 {@code Queue} 中，因为 {@code null} 也被用作 {@code poll} 方法的特殊返回值，以指示队列中没有元素。
 *
 * <p>{@code Queue} 实现通常不定义基于元素的方法 {@code equals} 和
 * {@code hashCode}，而是继承自类 {@code Object} 的基于身份的版本，因为对于具有相同元素但不同排序属性的队列，基于元素的相等性并不总是明确定义的。
 *
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @see java.util.Collection
 * @see LinkedList
 * @see PriorityQueue
 * @see java.util.concurrent.LinkedBlockingQueue
 * @see java.util.concurrent.BlockingQueue
 * @see java.util.concurrent.ArrayBlockingQueue
 * @see java.util.concurrent.LinkedBlockingQueue
 * @see java.util.concurrent.PriorityBlockingQueue
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public interface Queue<E> extends Collection<E> {
    /**
     * 如果可以在不违反容量限制的情况下立即插入指定的元素，则将其插入到此队列中，
     * 插入成功返回 {@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果由于容量限制当前无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定的元素为 null 且此队列不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此队列
     */
    boolean add(E e);

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定的元素，则将其插入到此队列中。
     * 当使用容量受限的队列时，此方法通常优于 {@link #add}，因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此队列中，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定的元素为 null 且此队列不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此队列
     */
    boolean offer(E e);

                /**
     * 获取并移除此队列的头部。此方法与 {@link #poll poll} 的唯一区别在于，如果队列为空，它会抛出异常。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    E remove();

    /**
     * 获取并移除此队列的头部，如果此队列为空，则返回 {@code null}。
     *
     * @return 此队列的头部，如果此队列为空则返回 {@code null}
     */
    E poll();

    /**
     * 获取但不移除此队列的头部。此方法与 {@link #peek peek} 的唯一区别在于，如果队列为空，它会抛出异常。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    E element();

    /**
     * 获取但不移除此队列的头部，如果此队列为空，则返回 {@code null}。
     *
     * @return 此队列的头部，如果此队列为空则返回 {@code null}
     */
    E peek();
}
