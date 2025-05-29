/*
 * 版权所有 (c) 1997, 2006, Oracle 和/或其附属公司。保留所有权利。
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
 * 抛出以指示请求的操作不受支持。<p>
 *
 * 本类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author  Josh Bloch
 * @since   1.2
 */
public class UnsupportedOperationException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 UnsupportedOperationException。
     */
    public UnsupportedOperationException() {
    }

    /**
     * 使用指定的详细消息构造一个 UnsupportedOperationException。
     *
     * @param message 详细消息
     */
    public UnsupportedOperationException(String message) {
        super(message);
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
    public UnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt>（通常包含 <tt>cause</tt> 的类和详细消息）构造一个新的异常。
     * 当异常只是其他可抛出对象的简单包装器时，此构造函数非常有用（例如，{@link
     * java.security.PrivilegedActionException}）。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。  （允许 <tt>null</tt> 值，表示原因不存在或未知。）
     * @since  1.5
     */
    public UnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    static final long serialVersionUID = -1242599979055084673L;
}
