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
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;

/**
 * 一个可以安排命令在给定延迟后运行，或定期执行的 {@link ExecutorService}。
 *
 * <p>{@code schedule} 方法创建具有各种延迟的任务，并返回一个可以用于取消或检查执行的任务对象。{@code scheduleAtFixedRate} 和
 * {@code scheduleWithFixedDelay} 方法创建并执行定期运行的任务，直到被取消。
 *
 * <p>使用 {@link Executor#execute(Runnable)}
 * 和 {@link ExecutorService} {@code submit} 方法提交的命令将被安排在请求的零延迟后执行。在 {@code schedule} 方法中也允许零和负延迟（但不是周期），并被视为立即执行的请求。
 *
 * <p>所有 {@code schedule} 方法接受 <em>相对</em> 延迟和周期作为参数，而不是绝对时间或日期。将表示为 {@link
 * java.util.Date} 的绝对时间转换为所需形式是简单的。例如，要在某个未来的 {@code date} 安排任务，可以使用：{@code schedule(task,
 * date.getTime() - System.currentTimeMillis(),
 * TimeUnit.MILLISECONDS)}。但是请注意，相对延迟的到期时间可能不会与任务启用时的当前 {@code Date} 一致，这是由于网络时间同步协议、时钟漂移或其他因素。
 *
 * <p>{@link Executors} 类提供了方便的工厂方法，用于创建本包中提供的 ScheduledExecutorService 实现。
 *
 * <h3>使用示例</h3>
 *
 * 以下是一个类，其中包含一个方法，用于设置一个 ScheduledExecutorService，使其每十分钟发出一次蜂鸣声，持续一小时：
 *
 *  <pre> {@code
 * import static java.util.concurrent.TimeUnit.*;
 * class BeeperControl {
 *   private final ScheduledExecutorService scheduler =
 *     Executors.newScheduledThreadPool(1);
 *
 *   public void beepForAnHour() {
 *     final Runnable beeper = new Runnable() {
 *       public void run() { System.out.println("beep"); }
 *     };
 *     final ScheduledFuture<?> beeperHandle =
 *       scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
 *     scheduler.schedule(new Runnable() {
 *       public void run() { beeperHandle.cancel(true); }
 *     }, 60 * 60, SECONDS);
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ScheduledExecutorService extends ExecutorService {

    /**
     * 创建并执行一个一次性动作，该动作在给定延迟后启用。
     *
     * @param command 要执行的任务
     * @param delay 从现在开始到延迟执行的时间
     * @param unit 延迟参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法将在完成时返回
     *         {@code null}
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 command 为 null
     */
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit);

    /**
     * 创建并执行一个在给定延迟后启用的 ScheduledFuture。
     *
     * @param callable 要执行的函数
     * @param delay 从现在开始到延迟执行的时间
     * @param unit 延迟参数的时间单位
     * @param <V> callable 结果的类型
     * @return 一个可以用于提取结果或取消的 ScheduledFuture
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 callable 为 null
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit);

    /**
     * 创建并执行一个周期性动作，该动作首先在给定的初始延迟后启用，然后以给定的周期重复执行；即执行将在
     * {@code initialDelay} 后开始，然后在 {@code initialDelay+period}，然后
     * {@code initialDelay + 2 * period}，等等。
     * 如果任务的任何执行遇到异常，后续执行将被抑制。
     * 否则，任务将仅通过取消或执行器终止来终止。如果此任务的任何执行时间超过其周期，则后续执行
     * 可能会延迟开始，但不会并发执行。
     *
     * @param command 要执行的任务
     * @param initialDelay 第一次执行的延迟时间
     * @param period 相继执行之间的周期
     * @param unit initialDelay 和 period 参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法将在取消时抛出异常
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 command 为 null
     * @throws IllegalArgumentException 如果 period 小于或等于零
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);

    /**
     * 创建并执行一个周期性动作，该动作首先在给定的初始延迟后启用，然后在一次执行终止与下一次执行开始之间以给定的延迟重复执行。如果任务的任何执行遇到异常，后续执行将被抑制。
     * 否则，任务将仅通过取消或执行器终止来终止。
     *
     * @param command 要执行的任务
     * @param initialDelay 第一次执行的延迟时间
     * @param delay 一次执行终止与下一次执行开始之间的延迟
     * @param unit initialDelay 和 delay 参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法将在取消时抛出异常
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 command 为 null
     * @throws IllegalArgumentException 如果 delay 小于或等于零
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);

}
