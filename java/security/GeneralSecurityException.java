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

package java.security;

/**
 * {@code GeneralSecurityException} 类是一个通用的安全异常类，为所有从它派生的安全相关异常类提供类型安全。
 *
 * @author Jan Luehe
 */

public class GeneralSecurityException extends Exception {

    private static final long serialVersionUID = 894798122053539237L;

    /**
     * 构造一个没有详细消息的 GeneralSecurityException。
     */
    public GeneralSecurityException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 GeneralSecurityException。
     * 详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public GeneralSecurityException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code GeneralSecurityException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public GeneralSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 创建一个 {@code GeneralSecurityException}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public GeneralSecurityException(Throwable cause) {
        super(cause);
    }
}
