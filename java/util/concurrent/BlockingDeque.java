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
 * http://creativecommons.org/publicdomain/zero/1.0/ 的说明发布到公共领域。
 */

package java.util.concurrent;
import java.util.*;

/**
 * 一个 {@link Deque}，另外支持在检索元素时等待双端队列变为非空的阻塞操作，以及在存储元素时等待双端队列中有可用空间的阻塞操作。
 *
 * <p>{@code BlockingDeque} 方法有四种形式，它们以不同的方式处理无法立即满足但将来可能满足的操作：
 * 一种抛出异常，第二种返回一个特殊值（根据操作的不同，可能是 {@code null} 或 {@code false}），第三种无限期地阻塞当前线程直到操作可以成功，
 * 第四种仅阻塞给定的最大时间限制，然后放弃。这些方法总结在下表中：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>BlockingDeque 方法概览</caption>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 5> <b>第一个元素（头部）</b></td>
 *  </tr>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>阻塞</em></td>
 *    <td ALIGN=CENTER><em>超时</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link #addFirst addFirst(e)}</td>
 *    <td>{@link #offerFirst(Object) offerFirst(e)}</td>
 *    <td>{@link #putFirst putFirst(e)}</td>
 *    <td>{@link #offerFirst(Object, long, TimeUnit) offerFirst(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link #removeFirst removeFirst()}</td>
 *    <td>{@link #pollFirst pollFirst()}</td>
 *    <td>{@link #takeFirst takeFirst()}</td>
 *    <td>{@link #pollFirst(long, TimeUnit) pollFirst(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link #getFirst getFirst()}</td>
 *    <td>{@link #peekFirst peekFirst()}</td>
 *    <td><em>不适用</em></td>
 *    <td><em>不适用</em></td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 5> <b>最后一个元素（尾部）</b></td>
 *  </tr>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>阻塞</em></td>
 *    <td ALIGN=CENTER><em>超时</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link #addLast addLast(e)}</td>
 *    <td>{@link #offerLast(Object) offerLast(e)}</td>
 *    <td>{@link #putLast putLast(e)}</td>
 *    <td>{@link #offerLast(Object, long, TimeUnit) offerLast(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link #removeLast() removeLast()}</td>
 *    <td>{@link #pollLast() pollLast()}</td>
 *    <td>{@link #takeLast takeLast()}</td>
 *    <td>{@link #pollLast(long, TimeUnit) pollLast(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link #getLast getLast()}</td>
 *    <td>{@link #peekLast peekLast()}</td>
 *    <td><em>不适用</em></td>
 *    <td><em>不适用</em></td>
 *  </tr>
 * </table>
 *
 * <p>像任何 {@link BlockingQueue} 一样，一个 {@code BlockingDeque} 是线程安全的，不允许 null 元素，并且可能是（或可能不是）容量受限的。
 *
 * <p>一个 {@code BlockingDeque} 实现可以直接用作 FIFO {@code BlockingQueue}。从
 * {@code BlockingQueue} 接口继承的方法与 {@code BlockingDeque} 方法完全等效，如下表所示：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>BlockingQueue 和 BlockingDeque 方法对比</caption>
 *  <tr>
 *    <td ALIGN=CENTER> <b>{@code BlockingQueue} 方法</b></td>
 *    <td ALIGN=CENTER> <b>等效的 {@code BlockingDeque} 方法</b></td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>插入</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #add(Object) add(e)}</td>
 *    <td>{@link #addLast(Object) addLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #offer(Object) offer(e)}</td>
 *    <td>{@link #offerLast(Object) offerLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #put(Object) put(e)}</td>
 *    <td>{@link #putLast(Object) putLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #offer(Object, long, TimeUnit) offer(e, time, unit)}</td>
 *    <td>{@link #offerLast(Object, long, TimeUnit) offerLast(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>移除</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #remove() remove()}</td>
 *    <td>{@link #removeFirst() removeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #poll() poll()}</td>
 *    <td>{@link #pollFirst() pollFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #take() take()}</td>
 *    <td>{@link #takeFirst() takeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
 *    <td>{@link #pollFirst(long, TimeUnit) pollFirst(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>检查</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #element() element()}</td>
 *    <td>{@link #getFirst() getFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #peek() peek()}</td>
 *    <td>{@link #peekFirst() peekFirst()}</td>
 *  </tr>
 * </table>
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入 {@code BlockingDeque} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程访问或移除该元素后发生的操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @since 1.6
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E> {
    /*
     * 这里有“菱形”多重接口继承，这引入了歧义。根据 javadoc 选择的分支，方法可能会有不同的规范。因此这里复制了很多方法的规范。
     */

    /**
     * 如果在不违反容量限制的情况下可以立即插入指定元素，则将其插入到此双端队列的前端，
     * 如果当前没有可用空间，则抛出一个 {@code IllegalStateException}。当使用容量受限的双端队列时，
     * 通常建议使用 {@link #offerFirst(Object) offerFirst}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void addFirst(E e);

    /**
     * 如果在不违反容量限制的情况下可以立即插入指定元素，则将其插入到此双端队列的末尾，
     * 如果当前没有可用空间，则抛出一个 {@code IllegalStateException}。当使用容量受限的双端队列时，
     * 通常建议使用 {@link #offerLast(Object) offerLast}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void addLast(E e);

    /**
     * 如果在不违反容量限制的情况下可以立即插入指定元素，则将其插入到此双端队列的前端，
     * 插入成功返回 {@code true}，如果当前没有可用空间，则返回 {@code false}。
     * 当使用容量受限的双端队列时，此方法通常优于 {@link #addFirst(Object) addFirst} 方法，
     * 因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    boolean offerFirst(E e);

    /**
     * 如果在不违反容量限制的情况下可以立即插入指定元素，则将其插入到此双端队列的末尾，
     * 插入成功返回 {@code true}，如果当前没有可用空间，则返回 {@code false}。
     * 当使用容量受限的双端队列时，此方法通常优于 {@link #addLast(Object) addLast} 方法，
     * 因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    boolean offerLast(E e);

    /**
     * 将指定元素插入到此双端队列的前端，必要时等待空间可用。
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果在等待过程中被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    void putFirst(E e) throws InterruptedException;

    /**
     * 将指定元素插入到此双端队列的末尾，必要时等待空间可用。
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果在等待过程中被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    void putLast(E e) throws InterruptedException;

    /**
     * 将指定元素插入到此双端队列的前端，必要时等待指定的等待时间以使空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 确定如何解释 {@code timeout} 参数的 {@code TimeUnit}
     * @return 如果成功则返回 {@code true}，如果在指定的等待时间内没有空间可用则返回 {@code false}
     * @throws InterruptedException 如果在等待过程中被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean offerFirst(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 将指定元素插入到此双端队列的末尾，必要时等待指定的等待时间以使空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 确定如何解释 {@code timeout} 参数的 {@code TimeUnit}
     * @return 如果成功则返回 {@code true}，如果在指定的等待时间内没有空间可用则返回 {@code false}
     * @throws InterruptedException 如果在等待过程中被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean offerLast(E e, long timeout, TimeUnit unit)
        throws InterruptedException;


    /**
     * 获取并移除此双端队列的第一个元素，如果必要则等待，直到有元素可用。
     *
     * @return 此双端队列的头部
     * @throws InterruptedException 如果在等待过程中被中断
     */
    E takeFirst() throws InterruptedException;

    /**
     * 获取并移除此双端队列的最后一个元素，如果必要则等待，直到有元素可用。
     *
     * @return 此双端队列的尾部
     * @throws InterruptedException 如果在等待过程中被中断
     */
    E takeLast() throws InterruptedException;

    /**
     * 获取并移除此双端队列的第一个元素，如果必要则等待指定的最长时间，直到有元素可用。
     *
     * @param timeout 等待的时间长度，单位为 {@code unit}
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 此双端队列的头部，或如果指定的等待时间已过但没有元素可用，则返回 {@code null}
     * @throws InterruptedException 如果在等待过程中被中断
     */
    E pollFirst(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 获取并移除此双端队列的最后一个元素，如果必要则等待指定的最长时间，直到有元素可用。
     *
     * @param timeout 等待的时间长度，单位为 {@code unit}
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 此双端队列的尾部，或如果指定的等待时间已过但没有元素可用，则返回 {@code null}
     * @throws InterruptedException 如果在等待过程中被中断
     */
    E pollLast(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 从此双端队列中移除指定元素的第一个出现。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * @param o 要从此双端队列中移除的元素，如果存在
     * @return 如果因调用而移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     * @throws NullPointerException 如果指定的元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     */
    boolean removeFirstOccurrence(Object o);

    /**
     * 从此双端队列中移除指定元素的最后一个出现。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除最后一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * @param o 要从此双端队列中移除的元素，如果存在
     * @return 如果因调用而移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     * @throws NullPointerException 如果指定的元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     */
    boolean removeLastOccurrence(Object o);

    // *** BlockingQueue 方法 ***

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，插入到此双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     * 当使用容量受限的双端队列时，通常更倾向于使用 {@link #offer(Object) offer} 方法。
     *
     * <p>此方法等同于 {@link #addLast(Object) addLast}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean add(E e);

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，插入到此双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则返回 {@code false}。当使用容量受限的双端队列时，此方法通常优于
     * {@link #add} 方法，因为后者只能通过抛出异常来失败。
     *
     * <p>此方法等同于 {@link #offerLast(Object) offerLast}。
     *
     * @param e 要添加的元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean offer(E e);

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，插入到此双端队列的尾部），如果必要则等待空间可用。
     *
     * <p>此方法等同于 {@link #putLast(Object) putLast}。
     *
     * @param e 要添加的元素
     * @throws InterruptedException {@inheritDoc}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    void put(E e) throws InterruptedException;


    /**
     * 将指定的元素插入到由这个双端队列表示的队列中（换句话说，插入到这个双端队列的尾部），如果必要的话，等待指定的时间以腾出空间。
     *
     * <p>此方法等同于 {@link #offerLast(Object,long,TimeUnit) offerLast}。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到这个双端队列中，则返回 {@code true}，否则返回 {@code false}
     * @throws InterruptedException 如果在等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到这个双端队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到这个双端队列中
     */
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素）。
     * 该方法与 {@link #poll poll} 的区别在于，如果这个双端队列为空，它会抛出异常。
     *
     * <p>此方法等同于 {@link #removeFirst() removeFirst}。
     *
     * @return 由这个双端队列表示的队列的头部
     * @throws NoSuchElementException 如果这个双端队列为空
     */
    E remove();

    /**
     * 检索并移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素），如果这个双端队列为空，则返回 {@code null}。
     *
     * <p>此方法等同于 {@link #pollFirst()}。
     *
     * @return 这个双端队列的头部，如果这个双端队列为空，则返回 {@code null}
     */
    E poll();

    /**
     * 检索并移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素），如果必要的话，等待直到有元素可用。
     *
     * <p>此方法等同于 {@link #takeFirst() takeFirst}。
     *
     * @return 这个双端队列的头部
     * @throws InterruptedException 如果在等待时被中断
     */
    E take() throws InterruptedException;

    /**
     * 检索并移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素），如果必要的话，等待指定的时间直到有元素可用。
     *
     * <p>此方法等同于 {@link #pollFirst(long,TimeUnit) pollFirst}。
     *
     * @return 这个双端队列的头部，如果指定的等待时间到期前没有元素可用，则返回 {@code null}
     * @throws InterruptedException 如果在等待时被中断
     */
    E poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索但不移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素）。
     * 该方法与 {@link #peek peek} 的区别在于，如果这个双端队列为空，它会抛出异常。
     *
     * <p>此方法等同于 {@link #getFirst() getFirst}。
     *
     * @return 这个双端队列的头部
     * @throws NoSuchElementException 如果这个双端队列为空
     */
    E element();

    /**
     * 检索但不移除由这个双端队列表示的队列的头部（换句话说，这个双端队列的第一个元素），如果这个双端队列为空，则返回 {@code null}。
     *
     * <p>此方法等同于 {@link #peekFirst() peekFirst}。
     *
     * @return 这个双端队列的头部，如果这个双端队列为空，则返回 {@code null}
     */
    E peek();

    /**
     * 从这个双端队列中移除指定元素的第一个出现。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果这个双端队列包含指定的元素（或等效地，如果这个双端队列因调用而改变），则返回 {@code true}。
     *
     * <p>此方法等同于 {@link #removeFirstOccurrence(Object) removeFirstOccurrence}。
     *
     * @param o 要从这个双端队列中移除的元素，如果存在
     * @return 如果这个双端队列因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与这个双端队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     * @throws NullPointerException 如果指定的元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     */
    boolean remove(Object o);

    /**
     * 如果这个双端队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果这个双端队列至少包含一个满足 {@code o.equals(e)} 的元素，则返回 {@code true}。
     *
     * @param o 要检查是否包含在双端队列中的对象
     * @return 如果这个双端队列包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与这个双端队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     * @throws NullPointerException 如果指定的元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选</a>）
     */
    public boolean contains(Object o);

    /**
     * 返回这个双端队列中的元素数量。
     *
     * @return 这个双端队列中的元素数量
     */
    public int size();

    /**
     * 返回一个按适当顺序遍历这个双端队列中元素的迭代器。
     * 元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * @return 一个按适当顺序遍历这个双端队列中元素的迭代器
     */
    Iterator<E> iterator();

    // *** 栈方法 ***

    /**
     * 将一个元素压入由这个双端队列表示的栈中（换句话说，插入到这个双端队列的头部），如果可以立即插入而不违反容量限制，则执行插入操作，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     *
     * <p>此方法等同于 {@link #addFirst(Object) addFirst}。
     *
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void push(E e);
}
