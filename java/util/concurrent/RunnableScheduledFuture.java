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
 * 一个 {@link ScheduledFuture}，它是 {@link Runnable}。成功执行 {@code run} 方法会导致 {@code Future} 的完成并允许访问其结果。
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> 此 Future 的 {@code get} 方法返回的结果类型
 */
public interface RunnableScheduledFuture<V> extends RunnableFuture<V>, ScheduledFuture<V> {

    /**
     * 如果此任务是周期性的，则返回 {@code true}。周期性任务可能会根据某个时间表重新运行。非周期性任务只能运行一次。
     *
     * @return 如果此任务是周期性的，则返回 {@code true}
     */
    boolean isPeriodic();
}