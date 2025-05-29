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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址的解释发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.Collection;
import java.util.Queue;

/**
 * 一个 {@link java.util.Queue}，还支持在检索元素时等待队列变为非空的操作，以及在存储元素时等待队列中有可用空间的操作。
 *
 * <p>{@code BlockingQueue} 方法有四种形式，以不同的方式处理无法立即满足但将来可能满足的操作：
 * 一种抛出异常，第二种返回一个特殊值（根据操作不同，可能是 {@code null} 或 {@code false}），第三种无限期地阻塞当前线程直到操作可以成功，
 * 第四种在放弃前阻塞给定的最大时间限制。这些方法总结在下表中：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>BlockingQueue 方法概览</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>阻塞</em></td>
 *    <td ALIGN=CENTER><em>超时</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link #add add(e)}</td>
 *    <td>{@link #offer offer(e)}</td>
 *    <td>{@link #put put(e)}</td>
 *    <td>{@link #offer(Object, long, TimeUnit) offer(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link #remove remove()}</td>
 *    <td>{@link #poll poll()}</td>
 *    <td>{@link #take take()}</td>
 *    <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link #element element()}</td>
 *    <td>{@link #peek peek()}</td>
 *    <td><em>不适用</em></td>
 *    <td><em>不适用</em></td>
 *  </tr>
 * </table>
 *
 * <p>{@code BlockingQueue} 不接受 {@code null} 元素。实现尝试 {@code add}、{@code put} 或 {@code offer} 一个 {@code null} 时会抛出 {@code NullPointerException}。
 * 一个 {@code null} 用作哨兵值，表示 {@code poll} 操作失败。
 *
 * <p>{@code BlockingQueue} 可能是容量受限的。在任何给定时间，它可能有一个 {@code remainingCapacity}，超过这个容量后，无法在不阻塞的情况下添加更多元素。
 * 没有任何内在容量限制的 {@code BlockingQueue} 始终报告剩余容量为 {@code Integer.MAX_VALUE}。
 *
 * <p>{@code BlockingQueue} 实现主要设计用于生产者-消费者队列，但也支持 {@link java.util.Collection} 接口。因此，例如，可以使用 {@code remove(x)} 从队列中移除任意元素。
 * 然而，这样的操作通常 <em>不</em> 非常高效，仅用于偶尔使用，如当排队的消息被取消时。
 *
 * <p>{@code BlockingQueue} 实现是线程安全的。所有队列方法都使用内部锁或其他形式的并发控制来原子地实现其效果。然而，除非在实现中另有说明，否则 <em>批量</em>
 * 集合操作 {@code addAll}、{@code containsAll}、{@code retainAll} 和 {@code removeAll} <em>不</em> 必定原子地执行。
 * 因此，例如，{@code addAll(c)} 可能会失败（抛出异常），但在添加 {@code c} 中的某些元素后。
 *
 * <p>{@code BlockingQueue} <em>不</em> 本质上支持任何类型的“关闭”或“关闭”操作，以表示不再添加项目。这种功能的需求和使用往往是实现依赖的。
 * 例如，常见的策略是生产者插入特殊的 <em>流结束</em> 或 <em>毒药</em> 对象，这些对象在被消费者取出时被相应地解释。
 *
 * <p>
 * 基于典型的生产者-消费者场景的使用示例。
 * 请注意，一个 {@code BlockingQueue} 可以安全地与多个生产者和多个消费者一起使用。
 *  <pre> {@code
 * class Producer implements Runnable {
 *   private final BlockingQueue queue;
 *   Producer(BlockingQueue q) { queue = q; }
 *   public void run() {
 *     try {
 *       while (true) { queue.put(produce()); }
 *     } catch (InterruptedException ex) { ... handle ...}
 *   }
 *   Object produce() { ... }
 * }
 *
 * class Consumer implements Runnable {
 *   private final BlockingQueue queue;
 *   Consumer(BlockingQueue q) { queue = q; }
 *   public void run() {
 *     try {
 *       while (true) { consume(queue.take()); }
 *     } catch (InterruptedException ex) { ... handle ...}
 *   }
 *   void consume(Object x) { ... }
 * }
 *
 * class Setup {
 *   void main() {
 *     BlockingQueue q = new SomeQueueImplementation();
 *     Producer p = new Producer(q);
 *     Consumer c1 = new Consumer(q);
 *     Consumer c2 = new Consumer(q);
 *     new Thread(p).start();
 *     new Thread(c1).start();
 *     new Thread(c2).start();
 *   }
 * }}</pre>
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入 {@code BlockingQueue} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程从 {@code BlockingQueue} 中访问或移除该元素后的操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 该集合中元素的类型
 */
public interface BlockingQueue<E> extends Queue<E> {
    /**
     * 如果可以立即插入指定元素而不违反容量限制，则将其插入此队列，成功时返回 {@code true}，如果当前没有可用空间则抛出
     * {@code IllegalStateException}。使用容量受限的队列时，通常更倾向于使用 {@link #offer(Object) offer}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果由于容量限制当前无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列
     */
    boolean add(E e);


    /**
     * 如果可以在不违反容量限制的情况下立即将指定元素插入此队列，则插入该元素，并在成功时返回
     * {@code true}，如果当前没有可用空间则返回 {@code false}。当使用容量受限的队列时，此方法
     * 通常优于 {@link #add}，因为后者只能通过抛出异常来失败插入元素。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此队列，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列
     */
    boolean offer(E e);

    /**
     * 将指定元素插入此队列，必要时等待空间可用。
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果在等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列
     */
    void put(E e) throws InterruptedException;

    /**
     * 将指定元素插入此队列，必要时等待指定的等待时间，直到空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 如果成功，则返回 {@code true}，如果指定的等待时间在空间可用之前已过期，则返回 {@code false}
     * @throws InterruptedException 如果在等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列
     */
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除此队列的头部，必要时等待直到元素可用。
     *
     * @return 此队列的头部
     * @throws InterruptedException 如果在等待时被中断
     */
    E take() throws InterruptedException;

    /**
     * 检索并移除此队列的头部，必要时等待指定的等待时间，直到元素可用。
     *
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 此队列的头部，如果指定的等待时间在元素可用之前已过期，则返回 {@code null}
     * @throws InterruptedException 如果在等待时被中断
     */
    E poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 返回此队列在不阻塞的情况下（在没有内存或资源限制的情况下）可以理想地接受的额外元素数量，或
     * 如果没有内在限制，则返回 {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，您<em>不能</em>总是通过检查 {@code remainingCapacity} 来判断插入元素的尝试是否会成功，因为
     * 可能有另一个线程即将插入或移除一个元素。
     *
     * @return 剩余容量
     */
    int remainingCapacity();

    /**
     * 如果此队列包含指定元素的一个实例，则移除该实例。更正式地说，移除一个元素 {@code e}，使得
     * {@code o.equals(e)}，如果此队列包含一个或多个这样的元素。
     * 如果此队列包含指定的元素（或等效地，如果此队列因调用而改变），则返回 {@code true}。
     *
     * @param o 如果存在，则从队列中移除的元素
     * @return 如果此队列因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选的</a>）
     * @throws NullPointerException 如果指定元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选的</a>）
     */
    boolean remove(Object o);

    /**
     * 如果此队列包含指定的元素，则返回 {@code true}。更正式地说，如果此队列包含至少一个元素
     * {@code e}，使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此队列中的对象
     * @return 如果此队列包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此队列不兼容
     *         （<a href="../Collection.html#optional-restrictions">可选的</a>）
     * @throws NullPointerException 如果指定元素为 null
     *         （<a href="../Collection.html#optional-restrictions">可选的</a>）
     */
    public boolean contains(Object o);

    /**
     * 从此队列中移除所有可用元素并将其添加到给定的集合中。此操作可能比反复从队列中轮询元素更高效。
     * 在尝试将元素添加到集合 {@code c} 时，如果遇到失败，可能会导致元素既不在任何集合中，也不在任一集合中，或同时在两个集合中。
     * 尝试将队列排空到自身会导致 {@code IllegalArgumentException}。此外，如果在操作进行过程中修改了指定的集合，
     * 则此操作的行为是未定义的。
     *
     * @param c 要转移元素到的集合
     * @return 转移的元素数量
     * @throws UnsupportedOperationException 如果指定集合不支持添加元素
     * @throws ClassCastException 如果此队列中元素的类阻止其被添加到指定集合
     * @throws NullPointerException 如果指定的集合为 null
     * @throws IllegalArgumentException 如果指定的集合是此队列，或此队列中元素的某些属性阻止其被添加到指定集合
     */
    int drainTo(Collection<? super E> c);


    /**
     * 从该队列中移除最多给定数量的可用元素，并将它们添加到给定的集合中。在尝试将元素添加到集合 {@code c} 时，
     * 如果遇到失败，可能会导致元素既不在原队列也不在目标集合中，或者同时存在于两个集合中，当相关异常被抛出时。
     * 尝试将队列中的元素排空到队列本身会导致 {@code IllegalArgumentException}。此外，如果在操作进行过程中
     * 指定的集合被修改，此操作的行为是未定义的。
     *
     * @param c 要将元素转移进的集合
     * @param maxElements 要转移的最大元素数量
     * @return 转移的元素数量
     * @throws UnsupportedOperationException 如果指定集合不支持添加元素
     * @throws ClassCastException 如果此队列中元素的类阻止其被添加到指定集合
     * @throws NullPointerException 如果指定的集合为 null
     * @throws IllegalArgumentException 如果指定的集合是此队列，或者此队列中某个元素的属性阻止其被添加到指定集合
     */
    int drainTo(Collection<? super E> c, int maxElements);
}
