/*
 * 版权所有 (c) 1996, 2013，Oracle 和/或其附属公司。保留所有权利。
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
 * {@link ExportException} 的一个过时子类。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 该类已过时。请改用 {@link ExportException}。
 */
@Deprecated
public class SocketSecurityException extends ExportException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -7622072999407781979L;

    /**
     * 使用指定的详细消息构造一个 <code>SocketSecurityException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public SocketSecurityException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造一个 <code>SocketSecurityException</code>。
     *
     * @param s 详细消息。
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public SocketSecurityException(String s, Exception ex) {
        super(s, ex);
    }

}
