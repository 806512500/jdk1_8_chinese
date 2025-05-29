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
 * 这是所有涉及密钥管理操作的一般密钥管理异常。开发人员可能会创建的
 * KeyManagementException 的子类，以提供更详细的信息，例如：
 *
 * <ul>
 * <li>KeyIDConflictException
 * <li>KeyAuthorizationFailureException
 * <li>ExpiredKeyException
 * </ul>
 *
 * @author Benjamin Renaud
 *
 * @see Key
 * @see KeyException
 */

public class KeyManagementException extends KeyException {

    private static final long serialVersionUID = 947674216157062695L;

    /**
     * 构造一个没有详细消息的 KeyManagementException。详细消息是一个
     * 描述此特定异常的字符串。
     */
    public KeyManagementException() {
        super();
    }

     /**
     * 构造一个具有指定详细消息的 KeyManagementException。详细消息是一个
     * 描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
   public KeyManagementException(String msg) {
        super(msg);
    }

    /**
     * 创建一个具有指定详细消息和原因的 {@code KeyManagementException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public KeyManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个具有指定原因和详细消息的 {@code KeyManagementException}，详细消息为
     * {@code (cause==null ? null : cause.toString())}（通常包含
     * {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public KeyManagementException(Throwable cause) {
        super(cause);
    }
}
