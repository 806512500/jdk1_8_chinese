/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * CRL (证书撤销列表) 异常。
 *
 * @author Hemma Prafullchandra
 */
public class CRLException extends GeneralSecurityException {

    private static final long serialVersionUID = -6694728944094197147L;

   /**
     * 构造一个没有详细消息的 CRLException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CRLException() {
        super();
    }

    /**
     * 使用指定的详细消息构造 CRLException。详细消息是一个描述此
     * 特定异常的字符串。
     *
     * @param message 详细消息。
     */
    public CRLException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CRLException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CRLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}
     * 创建一个 {@code CRLException}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CRLException(Throwable cause) {
        super(cause);
    }
}
