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
 * 由 Doug Lea 编写，并在 JCP JSR-166
 * 专家小组成员的帮助下发布到公共领域，如在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent;

/**
 * 一个可取消的延迟结果承载动作。
 * 通常，计划的未来是使用 {@link ScheduledExecutorService} 调度任务的结果。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 由该 Future 返回的结果类型
 */
public interface ScheduledFuture<V> extends Delayed, Future<V> {
}