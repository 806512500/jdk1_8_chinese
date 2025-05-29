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
 * 由 Doug Lea 与 JCP JSR-166 专家小组成员合作编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的发布到公共领域。
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.Date;

/**
 * {@code Condition} 将 {@code Object} 监视器方法（{@link Object#wait() wait}，{@link Object#notify notify}
 * 和 {@link Object#notifyAll notifyAll}）分解为不同的对象，以实现每个对象具有多个等待集的效果，
 * 通过将它们与任意 {@link Lock} 实现结合使用。其中，{@code Lock} 用于替代 {@code synchronized} 方法
 * 和语句，而 {@code Condition} 用于替代 Object 监视器方法。
 *
 * <p>条件（也称为 <em>条件队列</em> 或 <em>条件变量</em>）提供了一种机制，使一个线程可以暂停执行（“等待”）
 * 直到被另一个线程通知某些状态条件可能为真。由于对共享状态信息的访问发生在不同的线程中，因此必须加以保护，
 * 因此条件与某种形式的锁相关联。等待条件提供的关键属性是它 <em>原子地</em> 释放关联的锁并挂起当前线程，
 * 就像 {@code Object.wait} 一样。
 *
 * <p>{@code Condition} 实例与锁内在地绑定。要为特定的 {@link Lock} 实例获取 {@code Condition} 实例，
 * 请使用其 {@link Lock#newCondition newCondition()} 方法。
 *
 * <p>例如，假设我们有一个支持 {@code put} 和 {@code take} 方法的有界缓冲区。如果尝试对空缓冲区进行
 * {@code take} 操作，则线程将阻塞，直到有项目可用；如果尝试对满缓冲区进行 {@code put} 操作，则线程将阻塞，
 * 直到有空间可用。我们希望将等待的 {@code put} 线程和 {@code take} 线程分开，以便在缓冲区中有项目或空间可用时，
 * 可以使用每次只通知一个线程的优化。这可以通过使用两个 {@link Condition} 实例来实现。
 * <pre>
 * class BoundedBuffer {
 *   <b>final Lock lock = new ReentrantLock();</b>
 *   final Condition notFull  = <b>lock.newCondition(); </b>
 *   final Condition notEmpty = <b>lock.newCondition(); </b>
 *
 *   final Object[] items = new Object[100];
 *   int putptr, takeptr, count;
 *
 *   public void put(Object x) throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == items.length)
 *         <b>notFull.await();</b>
 *       items[putptr] = x;
 *       if (++putptr == items.length) putptr = 0;
 *       ++count;
 *       <b>notEmpty.signal();</b>
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 *
 *   public Object take() throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == 0)
 *         <b>notEmpty.await();</b>
 *       Object x = items[takeptr];
 *       if (++takeptr == items.length) takeptr = 0;
 *       --count;
 *       <b>notFull.signal();</b>
 *       return x;
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 * }
 * </pre>
 *
 * （{@link java.util.concurrent.ArrayBlockingQueue} 类提供了此功能，因此没有理由实现此示例用法类。）
 *
 * <p>{@code Condition} 实现可以提供与 {@code Object} 监视器方法不同的行为和语义，例如
 * 保证通知的顺序，或在执行通知时不需要持有锁。如果实现提供了此类专用语义，则必须记录这些语义。
 *
 * <p>请注意，{@code Condition} 实例只是普通对象，可以用作 {@code synchronized} 语句的目标，
 * 并且可以调用其自己的监视器 {@link Object#wait wait} 和 {@link Object#notify notification} 方法。
 * 获取 {@code Condition} 实例的监视器锁或使用其监视器方法与获取与此 {@code Condition} 关联的
 * {@link Lock} 或使用其 {@linkplain #await 等待} 和 {@linkplain #signal 通知} 方法没有指定的关系。
 * 建议为了避免混淆，除了可能在其自身实现中，不要以这种方式使用 {@code Condition} 实例。
 *
 * <p>除非另有说明，传递任何参数的 {@code null} 值将导致抛出 {@link NullPointerException}。
 *
 * <h3>实现注意事项</h3>
 *
 * <p>当等待 {@code Condition} 时，允许发生“<em>虚假唤醒</em>”，通常作为对底层平台语义的让步。
 * 这对大多数应用程序程序的实际影响很小，因为 {@code Condition} 应该始终在循环中等待，
 * 测试正在等待的状态谓词。实现可以自由地消除虚假唤醒的可能性，但建议应用程序程序员始终假设它们可能发生，
 * 因此始终在循环中等待。
 *
 * <p>条件等待的三种形式（可中断、不可中断和定时）在某些平台上实现的难易程度和性能特性可能不同。
 * 特别是，可能难以提供这些功能并保持特定的语义，如顺序保证。此外，实际挂起线程的能力可能不总是能够在所有平台上实现。
 *
 * <p>因此，实现不要求为所有三种形式的等待定义完全相同的保证或语义，也不要求支持实际挂起线程的中断。
 *
 * <p>实现必须清楚地记录每个等待方法提供的语义和保证，并且当实现确实支持线程挂起的中断时，
 * 必须遵守此接口中定义的中断语义。
 *
 * <p>由于中断通常意味着取消，并且中断检查通常不频繁，实现可以优先响应中断而不是正常方法返回。
 * 即使可以证明中断发生在可能解除线程阻塞的其他操作之后，也是如此。实现应记录此行为。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Condition {


                /**
     * 使当前线程等待，直到它被信号唤醒或被中断。
     *
     * <p>与该 {@code Condition} 关联的锁将原子地释放，当前线程将被禁用，以便进行线程调度
     * 并处于休眠状态，直到以下四种情况之一发生：
     * <ul>
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signal} 方法，且当前线程恰好被选为
     * 要唤醒的线程；或
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signalAll} 方法；或
     * <li>其他某个线程 {@linkplain Thread#interrupt 中断} 了当前线程，且支持线程挂起的中断；或
     * <li>发生了一个“<em>虚假唤醒</em>”。
     * </ul>
     *
     * <p>在所有情况下，当前线程必须重新获取与此条件关联的锁，才能使此方法返回。当线程返回时，它<em>保证</em>持有此锁。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或
     * <li>在等待时被 {@linkplain Thread#interrupt 中断} 且支持线程挂起的中断，
     * </ul>
     * 则抛出 {@link InterruptedException}，并且清除当前线程的中断状态。在第一种情况下，不指定是否在释放锁之前进行中断测试。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>假定在调用此方法时，当前线程持有与此 {@code Condition} 关联的锁。
     * 由实现来确定是否确实如此，如果不是，如何响应。通常，会抛出异常（如 {@link IllegalMonitorStateException}），并且实现必须记录这一事实。
     *
     * <p>实现可以优先响应中断而不是正常的方法返回响应信号。在这种情况下，实现必须确保信号被重定向到另一个等待的线程，如果有的话。
     *
     * @throws InterruptedException 如果当前线程被中断（且支持线程挂起的中断）
     */
    void await() throws InterruptedException;

    /**
     * 使当前线程等待，直到它被信号唤醒。
     *
     * <p>与该条件关联的锁将原子地释放，当前线程将被禁用，以便进行线程调度
     * 并处于休眠状态，直到以下三种情况之一发生：
     * <ul>
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signal} 方法，且当前线程恰好被选为
     * 要唤醒的线程；或
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signalAll} 方法；或
     * <li>发生了一个“<em>虚假唤醒</em>”。
     * </ul>
     *
     * <p>在所有情况下，当前线程必须重新获取与此条件关联的锁，才能使此方法返回。当线程返回时，它<em>保证</em>持有此锁。
     *
     * <p>如果当前线程在进入此方法时其中断状态已设置，或在等待时被 {@linkplain Thread#interrupt 中断}，
     * 它将继续等待直到被信号唤醒。当它最终从该方法返回时，其中断状态仍将设置。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>假定在调用此方法时，当前线程持有与此 {@code Condition} 关联的锁。
     * 由实现来确定是否确实如此，如果不是，如何响应。通常，会抛出异常（如 {@link IllegalMonitorStateException}），并且实现必须记录这一事实。
     */
    void awaitUninterruptibly();

    /**
     * 使当前线程等待，直到它被信号唤醒或被中断，或指定的等待时间已过。
     *
     * <p>与该条件关联的锁将原子地释放，当前线程将被禁用，以便进行线程调度
     * 并处于休眠状态，直到以下五种情况之一发生：
     * <ul>
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signal} 方法，且当前线程恰好被选为
     * 要唤醒的线程；或
     * <li>其他某个线程调用了此 {@code Condition} 的 {@link #signalAll} 方法；或
     * <li>其他某个线程 {@linkplain Thread#interrupt 中断} 了当前线程，且支持线程挂起的中断；或
     * <li>指定的等待时间已过；或
     * <li>发生了一个“<em>虚假唤醒</em>”。
     * </ul>
     *
     * <p>在所有情况下，当前线程必须重新获取与此条件关联的锁，才能使此方法返回。当线程返回时，它<em>保证</em>持有此锁。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或
     * <li>在等待时被 {@linkplain Thread#interrupt 中断} 且支持线程挂起的中断，
     * </ul>
     * 则抛出 {@link InterruptedException}，并且清除当前线程的中断状态。在第一种情况下，不指定是否在释放锁之前进行中断测试。
     *
     * <p>该方法返回一个估计值，表示给定的 {@code nanosTimeout} 值在方法返回时剩余的等待时间，或如果超时，则返回小于或等于零的值。
     * 此值可用于确定是否以及在等待返回但等待的条件仍然不满足的情况下重新等待多长时间。此方法的典型用法如下：
     *
     *  <pre> {@code
     * boolean aMethod(long timeout, TimeUnit unit) {
     *   long nanos = unit.toNanos(timeout);
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (nanos <= 0L)
     *         return false;
     *       nanos = theCondition.awaitNanos(nanos);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * <p>设计说明：此方法需要一个纳秒参数，以避免报告剩余时间时的精度损失。
     * 这样的精度损失会使程序员难以确保在重新等待发生时，总等待时间不会系统性地短于指定的时间。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>假定在调用此方法时，当前线程持有与此 {@code Condition} 关联的锁。
     * 由实现来确定是否确实如此，如果不是，如何响应。通常，会抛出异常（如 {@link IllegalMonitorStateException}），并且实现必须记录这一事实。
     *
     * <p>实现可以优先响应中断而不是正常的方法返回响应信号，或优先于指示指定的等待时间已过。在任何情况下，实现都必须确保信号被重定向到另一个等待的线程，如果有的话。
     *
     * @param nanosTimeout 最大等待时间，以纳秒为单位
     * @return 一个估计值，表示 {@code nanosTimeout} 值减去方法返回时的等待时间。
     *         正值可以用作后续调用此方法的参数，以完成等待所需的时间。小于或等于零的值表示没有剩余时间。
     * @throws InterruptedException 如果当前线程被中断（且支持线程挂起的中断）
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;


/**
 * 使当前线程等待，直到它被信号或中断，或者指定的等待时间过去。此方法的行为等同于：
 *  <pre> {@code awaitNanos(unit.toNanos(time)) > 0}</pre>
 *
 * @param time 等待的最大时间
 * @param unit {@code time} 参数的时间单位
 * @return 如果等待时间在方法返回前明显过去，则返回 {@code false}，否则返回 {@code true}
 * @throws InterruptedException 如果当前线程被中断（并且支持线程挂起的中断）
 */
boolean await(long time, TimeUnit unit) throws InterruptedException;

/**
 * 使当前线程等待，直到它被信号或中断，或者指定的截止时间过去。
 *
 * <p>与此条件关联的锁将原子地释放，当前线程将被禁用，以进行线程调度，并处于休眠状态，直到以下五种情况之一发生：
 * <ul>
 * <li>其他某个线程调用此 {@code Condition} 的 {@link #signal} 方法，且当前线程恰好被选为要唤醒的线程；或
 * <li>其他某个线程调用此 {@code Condition} 的 {@link #signalAll} 方法；或
 * <li>其他某个线程 {@linkplain Thread#interrupt 中断} 当前线程，且支持线程挂起的中断；或
 * <li>指定的截止时间过去；或
 * <li>发生“<em>虚假唤醒</em>”。
 * </ul>
 *
 * <p>在所有情况下，当前线程必须重新获取与此条件关联的锁，然后此方法才能返回。当线程返回时，它<em>保证</em>持有此锁。
 *
 *
 * <p>如果当前线程：
 * <ul>
 * <li>在进入此方法时其中断状态已设置；或
 * <li>在等待时被 {@linkplain Thread#interrupt 中断} 且支持线程挂起的中断，
 * </ul>
 * 则抛出 {@link InterruptedException}，并且清除当前线程的中断状态。在第一种情况下，未指定中断测试是否在释放锁之前发生。
 *
 *
 * <p>返回值指示截止时间是否已过期，可以如下使用：
 *  <pre> {@code
 * boolean aMethod(Date deadline) {
 *   boolean stillWaiting = true;
 *   lock.lock();
 *   try {
 *     while (!conditionBeingWaitedFor()) {
 *       if (!stillWaiting)
 *         return false;
 *       stillWaiting = theCondition.awaitUntil(deadline);
 *     }
 *     // ...
 *   } finally {
 *     lock.unlock();
 *   }
 * }}</pre>
 *
 * <p><b>实现注意事项</b>
 *
 * <p>假定在调用此方法时，当前线程持有与此 {@code Condition} 关联的锁。
 * 由实现确定是否确实如此，如果不是，则如何响应。通常，将抛出异常（如 {@link IllegalMonitorStateException}），并且实现必须记录这一事实。
 *
 * <p>实现可以优先响应中断而不是正常方法返回以响应信号，或者优先于指示指定的截止时间已过。在任何情况下，实现都必须确保信号被重定向到另一个等待的线程，如果有的话。
 *
 * @param deadline 等待的绝对时间
 * @return 如果截止时间在返回时已过期，则返回 {@code false}，否则返回 {@code true}
 * @throws InterruptedException 如果当前线程被中断（并且支持线程挂起的中断）
 */
boolean awaitUntil(Date deadline) throws InterruptedException;

/**
 * 唤醒一个等待的线程。
 *
 * <p>如果有线程在等待此条件，则选择一个线程唤醒。该线程必须重新获取锁，然后才能从 {@code await} 返回。
 *
 * <p><b>实现注意事项</b>
 *
 * <p>实现可能（并且通常会）要求当前线程在调用此方法时持有与此 {@code Condition} 关联的锁。实现必须记录此前提条件以及如果未持有锁将采取的任何行动。通常，将抛出诸如 {@link IllegalMonitorStateException} 之类的异常。
 */
void signal();

/**
 * 唤醒所有等待的线程。
 *
 * <p>如果有线程在等待此条件，则它们都将被唤醒。每个线程必须重新获取锁，然后才能从 {@code await} 返回。
 *
 * <p><b>实现注意事项</b>
 *
 * <p>实现可能（并且通常会）要求当前线程在调用此方法时持有与此 {@code Condition} 关联的锁。实现必须记录此前提条件以及如果未持有锁将采取的任何行动。通常，将抛出诸如 {@link IllegalMonitorStateException} 之类的异常。
 */
void signalAll();
