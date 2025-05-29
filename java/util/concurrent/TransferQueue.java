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
 * http://creativecommons.org/publicdomain/zero/1.0/ 的解释发布到公共领域。
 */

package java.util.concurrent;

/**
 * 一个 {@link BlockingQueue}，其中生产者可以等待消费者接收元素。例如，在消息传递应用程序中，
 * 生产者有时（使用方法 {@link #transfer}）等待消费者调用 {@code take} 或 {@code poll} 接收元素，
 * 而在其他时候则通过方法 {@code put} 将元素入队而不等待接收。
 * 也有 {@linkplain #tryTransfer(Object) 非阻塞} 和
 * {@linkplain #tryTransfer(Object,long,TimeUnit) 超时} 版本的 {@code tryTransfer}。
 * 可以通过 {@link #hasWaitingConsumer} 查询 {@code TransferQueue} 是否有线程在等待项目，
 * 这与 {@code peek} 操作是相反的类比。
 *
 * <p>像其他阻塞队列一样，{@code TransferQueue} 可能是容量受限的。如果是这样，尝试传输操作可能最初会阻塞等待可用空间，
 * 和/或随后阻塞等待消费者接收。请注意，在零容量的队列中，例如 {@link SynchronousQueue}，
 * {@code put} 和 {@code transfer} 实际上是同义的。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public interface TransferQueue<E> extends BlockingQueue<E> {
    /**
     * 如果可能，立即将元素传输给等待的消费者。
     *
     * <p>更准确地说，如果存在已经等待接收它的消费者（在 {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传输，否则不将元素入队并返回 {@code false}。
     *
     * @param e 要传输的元素
     * @return 如果元素被传输，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列中
     */
    boolean tryTransfer(E e);

    /**
     * 等待必要的时间将元素传输给消费者。
     *
     * <p>更准确地说，如果存在已经等待接收它的消费者（在 {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传输，否则等待直到消费者接收该元素。
     *
     * @param e 要传输的元素
     * @throws InterruptedException 如果在等待过程中被中断，则元素不会留在队列中
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列中
     */
    void transfer(E e) throws InterruptedException;

    /**
     * 如果在超时前可以传输元素，则将其传输给消费者。
     *
     * <p>更准确地说，如果存在已经等待接收它的消费者（在 {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传输，否则等待直到消费者接收该元素，如果指定的等待时间在元素可以传输之前耗尽，则返回 {@code false}。
     *
     * @param e 要传输的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 确定如何解释 {@code timeout} 参数的 {@code TimeUnit}
     * @return 如果成功，则返回 {@code true}，如果指定的等待时间在完成前耗尽，则返回 {@code false}，此时元素不会留在队列中
     * @throws InterruptedException 如果在等待过程中被中断，则元素不会留在队列中
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此队列中
     */
    boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 如果至少有一个消费者通过 {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 等待接收元素，则返回 {@code true}。
     * 返回值表示一个瞬时状态。
     *
     * @return 如果至少有一个等待的消费者，则返回 {@code true}
     */
    boolean hasWaitingConsumer();

    /**
     * 返回通过 {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 等待接收元素的消费者的估计数量。
     * 返回值是一个瞬时状态的近似值，如果消费者已完成或放弃等待，可能不准确。
     * 该值对于监控和启发式分析可能有用，但不适合用于同步控制。此方法的实现可能明显慢于 {@link #hasWaitingConsumer} 的实现。
     *
     * @return 等待接收元素的消费者数量
     */
    int getWaitingConsumerCount();
}
