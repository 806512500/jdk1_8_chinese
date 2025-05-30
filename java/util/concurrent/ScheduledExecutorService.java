/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个可以安排在给定延迟后运行命令，或者定期执行的 {@link ExecutorService}。
 *
 * <p>{@code schedule} 方法创建具有各种延迟的任务，并返回一个可以用于取消或检查执行的任务对象。{@code scheduleAtFixedRate} 和
 * {@code scheduleWithFixedDelay} 方法创建并执行定期运行的任务，直到取消。
 *
 * <p>使用 {@link Executor#execute(Runnable)}
 * 和 {@link ExecutorService} {@code submit} 方法提交的命令被安排在零延迟后执行。零和负延迟（但不是周期）在 {@code schedule} 方法中也是允许的，
 * 并被视为立即执行的请求。
 *
 * <p>所有 {@code schedule} 方法接受 <em>相对</em> 延迟和周期作为参数，而不是绝对时间或日期。将表示为 {@link
 * java.util.Date} 的绝对时间转换为所需形式是简单的。例如，要在某个未来的 {@code date} 安排任务，可以使用：{@code schedule(task,
 * date.getTime() - System.currentTimeMillis(),
 * TimeUnit.MILLISECONDS)}。但是，需要注意的是，相对延迟的到期时间可能不会与当前 {@code Date} 任务启用时的时间一致，因为网络时间同步协议、时钟漂移或其他因素的影响。
 *
 * <p>{@link Executors} 类提供了方便的工厂方法，用于创建本包中提供的 ScheduledExecutorService 实现。
 *
 * <h3>使用示例</h3>
 *
 * 以下是一个类，其中包含一个方法，设置一个 ScheduledExecutorService 每十分钟响一次蜂鸣声，持续一小时：
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
     * 创建并执行一个在给定延迟后启用的一次性动作。
     *
     * @param command 要执行的任务
     * @param delay 从现在起延迟执行的时间
     * @param unit 延迟参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法将在任务完成时返回
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
     * @param delay 从现在起延迟执行的时间
     * @param unit 延迟参数的时间单位
     * @param <V> callable 结果的类型
     * @return 一个可以用于提取结果或取消的 ScheduledFuture
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 callable 为 null
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit);

    /**
     * 创建并执行一个在给定初始延迟后首次启用，随后以给定周期执行的周期性动作。
     * 如果任务的任何执行遇到异常，后续执行将被抑制。否则，任务将仅通过取消或执行器终止来终止。如果此任务的任何执行时间超过其周期，
     * 则后续执行可能会延迟，但不会并发执行。
     *
     * @param command 要执行的任务
     * @param initialDelay 首次执行的延迟时间
     * @param period 相继执行之间的周期
     * @param unit 初始延迟和周期参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法在取消时将抛出异常
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 command 为 null
     * @throws IllegalArgumentException 如果周期小于或等于零
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);

    /**
     * 创建并执行一个在给定初始延迟后首次启用，随后在一次执行终止和下一次执行开始之间以给定延迟执行的周期性动作。
     * 如果任务的任何执行遇到异常，后续执行将被抑制。否则，任务将仅通过取消或执行器终止来终止。
     *
     * @param command 要执行的任务
     * @param initialDelay 首次执行的延迟时间
     * @param delay 一次执行终止和下一次执行开始之间的延迟
     * @param unit 初始延迟和延迟参数的时间单位
     * @return 一个表示任务待完成的 ScheduledFuture，其 {@code get()} 方法在取消时将抛出异常
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果 command 为 null
     * @throws IllegalArgumentException 如果延迟小于或等于零
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);

}
