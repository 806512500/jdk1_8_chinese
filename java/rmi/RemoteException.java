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

package java.rmi;

/**
 * <code>RemoteException</code> 是多个可能在远程方法调用期间发生的与通信相关的异常的公共超类。每个远程接口（扩展 <code>java.rmi.Remote</code> 的接口）的方法都必须在其 throws 子句中列出 <code>RemoteException</code>。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链接机制。构造时可能提供的“包装的远程异常”以及通过公共 {@link #detail} 字段访问的异常现在被称为<i>原因</i>，并且可以通过 {@link Throwable#getCause()} 方法以及上述“遗留字段”访问。
 *
 * <p>在 <code>RemoteException</code> 的实例上调用 {@link Throwable#initCause(Throwable)} 方法总是会抛出 {@link IllegalStateException}。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class RemoteException extends java.io.IOException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -5148567311918794206L;

    /**
     * 远程异常的原因。
     *
     * <p>此字段早于通用异常链接机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Throwable detail;

    /**
     * 构造一个 <code>RemoteException</code>。
     */
    public RemoteException() {
        initCause(null);  // 禁止后续 initCause
    }

    /**
     * 使用指定的详细消息构造一个 <code>RemoteException</code>。
     *
     * @param s 详细消息
     */
    public RemoteException(String s) {
        super(s);
        initCause(null);  // 禁止后续 initCause
    }

    /**
     * 使用指定的详细消息和原因构造一个 <code>RemoteException</code>。此构造函数将 {@link #detail} 字段设置为指定的 <code>Throwable</code>。
     *
     * @param s 详细消息
     * @param cause 原因
     */
    public RemoteException(String s, Throwable cause) {
        super(s);
        initCause(null);  // 禁止后续 initCause
        detail = cause;
    }

    /**
     * 返回详细消息，包括此异常的任何原因的消息。
     *
     * @return 详细消息
     */
    public String getMessage() {
        if (detail == null) {
            return super.getMessage();
        } else {
            return super.getMessage() + "; 嵌套异常是: \n\t" +
                detail.toString();
        }
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
