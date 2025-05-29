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
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

/**
 * 当任务无法被接受执行时，由 {@link Executor} 抛出的异常。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class RejectedExecutionException extends RuntimeException {
    private static final long serialVersionUID = -375805702767069545L;

    /**
     * 构造一个没有详细消息的 {@code RejectedExecutionException}。
     * 原因未初始化，可以通过调用 {@link #initCause(Throwable) initCause} 来初始化。
     */
    public RejectedExecutionException() { }

    /**
     * 使用指定的详细消息构造一个 {@code RejectedExecutionException}。原因未初始化，可以通过调用 {@link
     * #initCause(Throwable) initCause} 来初始化。
     *
     * @param message 详细消息
     */
    public RejectedExecutionException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code RejectedExecutionException}。
     *
     * @param  message 详细消息
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public RejectedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code RejectedExecutionException}。详细消息设置为 {@code (cause ==
     * null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public RejectedExecutionException(Throwable cause) {
        super(cause);
    }
}