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
 * 一个处理无法由 {@link ThreadPoolExecutor} 执行的任务的处理器。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface RejectedExecutionHandler {

    /**
     * 当 {@link ThreadPoolExecutor} 无法接受任务时可能被调用的方法。
     * 这可能发生在没有更多线程或队列插槽可用时，因为它们的边界将被超过，或者在执行器关闭时。
     *
     * <p>如果没有其他选择，该方法可能会抛出一个未检查的 {@link RejectedExecutionException}，这将传播给 {@code execute} 的调用者。
     *
     * @param r 请求执行的可运行任务
     * @param executor 尝试执行此任务的执行器
     * @throws RejectedExecutionException 如果没有解决办法
     */
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}