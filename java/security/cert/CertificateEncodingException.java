/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其关联公司。保留所有权利。
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

/**
 * 证书编码异常。当尝试对证书进行编码时发生错误时抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateEncodingException extends CertificateException {

    private static final long serialVersionUID = 6219492851589449162L;

    /**
     * 构造一个没有详细消息的 CertificateEncodingException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CertificateEncodingException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 CertificateEncodingException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateEncodingException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertificateEncodingException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息创建一个 {@code CertificateEncodingException}，
     * 详细消息为 {@code (cause==null ? null : cause.toString())}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateEncodingException(Throwable cause) {
        super(cause);
    }
}
