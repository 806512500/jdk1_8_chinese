/*
 * 版权所有 (c) 1995, 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 由安全管理器抛出，表示安全违规。
 *
 * @author 未指定
 * @see java.lang.SecurityManager
 * @since JDK1.0
 */
public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 6878364983674394167L;

    /**
     * 构造一个没有详细信息消息的 <code>SecurityException</code>。
     */
    public SecurityException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>SecurityException</code>。
     *
     * @param   s   详细信息消息。
     */
    public SecurityException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细信息消息和原因创建一个 <code>SecurityException</code>。
     *
     * @param message 详细信息消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 <tt>null</tt> 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细信息消息 <tt>(cause==null ? null : cause.toString())</tt> 创建一个 <code>SecurityException</code>
     * （通常包含 <tt>cause</tt> 的类和详细信息消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 <tt>null</tt> 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public SecurityException(Throwable cause) {
        super(cause);
    }
}
