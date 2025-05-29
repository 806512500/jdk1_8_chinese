/*
 * 版权所有 (c) 1996, 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示方法在非法或不适当的时间被调用。换句话说，Java 环境或
 * Java 应用程序不在请求操作的适当状态。
 *
 * @author  Jonni Kanerva
 * @since   JDK1.1
 */
public
class IllegalStateException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 IllegalStateException。
     * 详细消息是一个描述此特定异常的字符串。
     */
    public IllegalStateException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 IllegalStateException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param s 包含详细消息的字符串
     */
    public IllegalStateException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的异常。
     *
     * <p>请注意，与 <code>cause</code> 关联的详细消息
     * <i>不会</i> 自动包含在此异常的详细消息中。
     *
     * @param  message 详细消息（由 {@link Throwable#getMessage()} 方法稍后检索）。
     * @param  cause 原因（由 {@link Throwable#getCause()} 方法稍后检索）。 （允许 <tt>null</tt> 值，表示原因不存在或未知）。
     * @since 1.5
     */
    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt> 构造一个新的异常（通常包含 <tt>cause</tt> 的类和详细消息）。
     * 此构造函数对于基本上是其他可抛出对象的包装器的异常非常有用（例如，{@link
     * java.security.PrivilegedActionException}）。
     *
     * @param  cause 原因（由 {@link Throwable#getCause()} 方法稍后检索）。 （允许 <tt>null</tt> 值，表示原因不存在或未知）。
     * @since  1.5
     */
    public IllegalStateException(Throwable cause) {
        super(cause);
    }

    static final long serialVersionUID = -1848914673093119416L;
}
