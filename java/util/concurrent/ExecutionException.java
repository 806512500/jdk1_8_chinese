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
 * 当尝试检索因抛出异常而中止的任务的结果时抛出的异常。可以使用 {@link #getCause()} 方法检查此异常。
 *
 * @see Future
 * @since 1.5
 * @author Doug Lea
 */
public class ExecutionException extends Exception {
    private static final long serialVersionUID = 7830266012832686185L;

    /**
     * 构造一个没有详细消息的 {@code ExecutionException}。原因未初始化，可以随后通过调用 {@link #initCause(Throwable) initCause} 方法初始化。
     */
    protected ExecutionException() { }

    /**
     * 使用指定的详细消息构造一个 {@code ExecutionException}。原因未初始化，可以随后通过调用 {@link #initCause(Throwable) initCause} 方法初始化。
     *
     * @param message 详细消息
     */
    protected ExecutionException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code ExecutionException}。
     *
     * @param  message 详细消息
     * @param  cause 通过 {@link #getCause()} 方法稍后检索的原因
     */
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code ExecutionException}。详细消息设置为 {@code (cause == null ? null :
     * cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param  cause 通过 {@link #getCause()} 方法稍后检索的原因
     */
    public ExecutionException(Throwable cause) {
        super(cause);
    }
}