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

package java.rmi.server;

/**
 * 如果在克隆 <code>UnicastRemoteObject</code> 期间发生远程异常，则抛出 <code>ServerCloneException</code>。
 *
 * <p>从 1.4 版本开始，此异常已被重新设计以符合通用的异常链接机制。在构造时可以提供的“嵌套异常”以及通过公共
 * {@link #detail} 字段访问的异常现在被称为 <i>原因</i>，也可以通过 {@link Throwable#getCause()} 方法访问，
 * 以及上述“遗留字段”。
 *
 * <p>在 <code>ServerCloneException</code> 的实例上调用 {@link Throwable#initCause(Throwable)} 方法总是会抛出 {@link
 * IllegalStateException}。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see     java.rmi.server.UnicastRemoteObject#clone()
 */
public class ServerCloneException extends CloneNotSupportedException {

    /**
     * 异常的原因。
     *
     * <p>此字段早于通用异常链接机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Exception detail;

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 6617456357664815945L;

    /**
     * 使用指定的详细消息构造 <code>ServerCloneException</code>。
     *
     * @param s 详细消息。
     */
    public ServerCloneException(String s) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 使用指定的详细消息和原因构造 <code>ServerCloneException</code>。
     *
     * @param s 详细消息。
     * @param cause 原因
     */
    public ServerCloneException(String s, Exception cause) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
        detail = cause;
    }

    /**
     * 返回此异常的详细消息，包括原因（如果有）的消息。
     *
     * @return 详细消息
     */
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else
            return super.getMessage() +
                "; 嵌套异常是: \n\t" +
                detail.toString();
    }

    /**
     * 返回此异常的原因。此方法返回 {@link #detail} 字段的值。
     *
     * @return 原因，可能是 <tt>null</tt>。
     * @since   1.4
     */
    public Throwable getCause() {
        return detail;
    }
}
