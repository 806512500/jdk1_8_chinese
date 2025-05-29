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

/**
 * 证书解析异常。当解析无效的 DER 编码证书或在证书中发现不支持的 DER 特性时抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateParsingException extends CertificateException {

    private static final long serialVersionUID = -7989222416793322029L;

    /**
     * 构造一个没有详细消息的 CertificateParsingException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CertificateParsingException() {
        super();
    }

    /**
     * 使用指定的详细消息构造 CertificateParsingException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateParsingException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertificateParsingException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}
     * 创建一个 {@code CertificateParsingException}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateParsingException(Throwable cause) {
        super(cause);
    }
}
