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
 * 当请求特定的加密算法但在环境中不可用时，将抛出此异常。
 *
 * @author Benjamin Renaud
 */

public class NoSuchAlgorithmException extends GeneralSecurityException {

    private static final long serialVersionUID = -7443947487218346562L;

    /**
     * 构造一个没有详细信息消息的 NoSuchAlgorithmException。详细信息消息是一个描述此
     * 特定异常的字符串。
     */
    public NoSuchAlgorithmException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 NoSuchAlgorithmException。详细信息消息是一个描述
     * 此特定异常的字符串，例如，可以指定哪个算法不可用。
     *
     * @param msg 详细信息消息。
     */
    public NoSuchAlgorithmException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细信息消息和原因创建一个 {@code NoSuchAlgorithmException}。
     *
     * @param message 详细信息消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public NoSuchAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细信息消息 {@code (cause==null ? null : cause.toString())}
     * 创建一个 {@code NoSuchAlgorithmException}（通常包含 {@code cause} 的类和详细信息消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public NoSuchAlgorithmException(Throwable cause) {
        super(cause);
    }
}
