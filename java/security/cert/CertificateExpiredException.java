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
 * 证书过期异常。当当前的 {@code Date} 或指定的 {@code Date} 在证书有效期内指定的
 * {@code notAfter} 日期/时间之后时，将抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateExpiredException extends CertificateException {

    private static final long serialVersionUID = 9071001339691533771L;

    /**
     * 构造一个没有详细消息的 CertificateExpiredException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CertificateExpiredException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 CertificateExpiredException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateExpiredException(String message) {
        super(message);
    }
}
