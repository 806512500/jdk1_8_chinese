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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所述发布到公共领域。
 */

package java.util.concurrent;

/**
 * 在完成结果或任务的过程中遇到错误或其他异常时抛出的异常。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class CompletionException extends RuntimeException {
    private static final long serialVersionUID = 7830266012832686185L;

    /**
     * 构造一个没有详细消息的 {@code CompletionException}。
     * 原因未初始化，可以通过调用 {@link #initCause(Throwable) initCause} 方法来初始化。
     */
    protected CompletionException() { }

    /**
     * 使用指定的详细消息构造一个 {@code CompletionException}。
     * 原因未初始化，可以通过调用 {@link #initCause(Throwable) initCause} 方法来初始化。
     *
     * @param message 详细消息
     */
    protected CompletionException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code CompletionException}。
     *
     * @param  message 详细消息
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public CompletionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code CompletionException}。
     * 详细消息设置为 {@code (cause == null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public CompletionException(Throwable cause) {
        super(cause);
    }
}