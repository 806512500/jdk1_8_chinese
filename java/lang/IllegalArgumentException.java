/*
 * 版权所有 (c) 1994, 2012, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 抛出以指示方法已传递非法或不适当的参数。
 *
 * @author 未署名
 * @since JDK1.0
 */
public
class IllegalArgumentException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 <code>IllegalArgumentException</code>。
     */
    public IllegalArgumentException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalArgumentException</code>。
     *
     * @param   s   详细消息。
     */
    public IllegalArgumentException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的异常。
     *
     * <p>请注意，与 <code>cause</code> 关联的详细消息
     * <i>不会</i> 自动包含在此异常的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link Throwable#getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。  （允许 <tt>null</tt> 值，表示原因不存在或未知。）
     * @since 1.5
     */
    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt> 构造一个新的异常（通常包含 <tt>cause</tt> 的类和详细消息）。
     * 此构造函数对于异常只是其他可抛出对象的简单包装器的情况非常有用（例如，{@link
     * java.security.PrivilegedActionException}）。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。  （允许 <tt>null</tt> 值，表示原因不存在或未知。）
     * @since  1.5
     */
    public IllegalArgumentException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = -5365630128856068164L;
}
