/*
 * 版权所有 (c) 2009, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 核心反射操作中抛出的异常的公共超类。
 *
 * @see LinkageError
 * @since 1.7
 */
public class ReflectiveOperationException extends Exception {
    static final long serialVersionUID = 123456789L;

    /**
     * 构造一个详细信息消息为 {@code null} 的新异常。原因未初始化，可以随后通过调用 {@link #initCause} 进行初始化。
     */
    public ReflectiveOperationException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个新异常。原因未初始化，可以随后通过调用 {@link #initCause} 进行初始化。
     *
     * @param   message   详细信息消息。详细信息消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public ReflectiveOperationException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细信息消息和原因构造一个新异常。
     *
     * <p>请注意，与 {@code cause} 关联的详细信息消息 <em>不会</em> 自动包含在本异常的详细信息消息中。
     *
     * @param  message 详细信息消息（稍后将由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后将由 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     */
    public ReflectiveOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细信息消息 {@code (cause==null ? null : cause.toString())} 构造一个新异常（通常包含 {@code cause} 的类和详细信息消息）。
     *
     * @param  cause 原因（稍后将由 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     */
    public ReflectiveOperationException(Throwable cause) {
        super(cause);
    }
}
