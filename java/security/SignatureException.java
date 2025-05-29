/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

/**
 * 这是一个通用的签名异常。
 *
 * @author Benjamin Renaud
 */

public class SignatureException extends GeneralSecurityException {

    private static final long serialVersionUID = 7509989324975124438L;

    /**
     * 构造一个没有详细消息的 SignatureException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public SignatureException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 SignatureException。详细消息是一个描述此
     * 特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public SignatureException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code SignatureException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}
     * 创建一个 {@code SignatureException}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public SignatureException(Throwable cause) {
        super(cause);
    }
}
