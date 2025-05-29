/*
 * 版权所有 (c) 1995, 2011，Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款的约束。
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
 * {@code RuntimeException} 是那些在 Java 虚拟机正常运行期间可以抛出的异常的超类。
 *
 * <p>{@code RuntimeException} 及其子类是 <em>未检查异常</em>。未检查异常 <em>不需要</em>
 * 在方法或构造器的 {@code throws} 子句中声明，即使它们可以由方法或构造器的执行抛出并传播到方法或构造器的边界之外。
 *
 * @author  Frank Yellin
 * @jls 11.2 编译时异常检查
 * @since   JDK1.0
 */
public class RuntimeException extends Exception {
    static final long serialVersionUID = -7034897190745766939L;

    /** 构造一个带有 {@code null} 作为其详细消息的新运行时异常。原因未初始化，可以随后通过调用 {@link #initCause} 进行初始化。 */
    public RuntimeException() {
        super();
    }

    /** 构造一个带有指定详细消息的新运行时异常。原因未初始化，可以随后通过调用 {@link #initCause} 进行初始化。
     *
     * @param   message   详细消息。详细消息将被保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public RuntimeException(String message) {
        super(message);
    }

    /**
     * 构造一个带有指定详细消息和原因的新运行时异常。注意，与 {@code cause} 关联的详细消息 <i>不会</i> 自动包含在
     * 此运行时异常的详细消息中。
     *
     * @param  message 详细消息（稍后由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt> 值，表示原因不存在或未知。
     * @since  1.4
     */
    public RuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 构造一个带有指定原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt> 的新运行时异常
     * （通常包含 <tt>cause</tt> 的类和详细消息）。此构造器对于基本上只是其他可抛出对象的包装器的运行时异常很有用。
     *
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt> 值，表示原因不存在或未知。
     * @since  1.4
     */
    public RuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有指定详细消息、原因、是否启用抑制以及是否启用可写堆栈跟踪的新运行时异常。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null} 值，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     *
     * @since 1.7
     */
    protected RuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
