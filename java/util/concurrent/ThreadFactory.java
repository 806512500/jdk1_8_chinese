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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

/**
 * 一个根据需要创建新线程的对象。使用线程工厂可以避免硬编码调用 {@link Thread#Thread(Runnable) new Thread}，
 * 使应用程序能够使用特殊的线程子类、优先级等。
 *
 * <p>
 * 此接口的最简单实现如下：
 *  <pre> {@code
 * class SimpleThreadFactory implements ThreadFactory {
 *   public Thread newThread(Runnable r) {
 *     return new Thread(r);
 *   }
 * }}</pre>
 *
 * {@link Executors#defaultThreadFactory} 方法提供了一个更有用的简单实现，该实现在线程创建后设置线程上下文为已知值。
 * @since 1.5
 * @author Doug Lea
 */
public interface ThreadFactory {

    /**
     * 构造一个新的 {@code Thread}。实现还可以初始化优先级、名称、守护状态、{@code ThreadGroup} 等。
     *
     * @param r 由新线程实例执行的可运行对象
     * @return 构造的线程，如果创建线程的请求被拒绝，则返回 {@code null}
     */
    Thread newThread(Runnable r);
}