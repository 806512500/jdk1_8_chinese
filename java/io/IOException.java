/*
 * 版权所有 (c) 1994, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 表示某种 I/O 异常已发生。此类是由于失败或中断的 I/O 操作而产生的异常的通用类。
 *
 * @author 未署名
 * @see java.io.InputStream
 * @see java.io.OutputStream
 * @since JDK1.0
 */
public
class IOException extends Exception {
    static final long serialVersionUID = 7818375828146090155L;

    /**
     * 构造一个带有 {@code null} 作为其错误详细信息消息的 {@code IOException}。
     */
    public IOException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 {@code IOException}。
     *
     * @param message
     *        详细信息消息（稍后通过 {@link #getMessage()} 方法检索）
     */
    public IOException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细信息消息和原因构造一个 {@code IOException}。
     *
     * <p> 请注意，与 {@code cause} 相关联的详细信息消息
     * <i>不会</i> 自动合并到此异常的详细信息消息中。
     *
     * @param message
     *        详细信息消息（稍后通过 {@link #getMessage()} 方法检索）
     *
     * @param cause
     *        原因（稍后通过 {@link #getCause()} 方法检索）。 （允许 null 值，
     *        表示原因不存在或未知。）
     *
     * @since 1.6
     */
    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细信息消息 {@code (cause==null ? null : cause.toString())}
     * （通常包含 {@code cause} 的类和详细信息消息）构造一个 {@code IOException}。
     * 此构造函数对于那些几乎只是其他可抛出对象的包装器的 I/O 异常非常有用。
     *
     * @param cause
     *        原因（稍后通过 {@link #getCause()} 方法检索）。 （允许 null 值，
     *        表示原因不存在或未知。）
     *
     * @since 1.6
     */
    public IOException(Throwable cause) {
        super(cause);
    }
}
