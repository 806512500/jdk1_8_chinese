/*
 * 版权所有 (c) 1994, 2011, Oracle 和/或其附属公司。保留所有权利。
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
 * 类 {@code Exception} 及其子类是 {@code Throwable} 的一种形式，表示合理应用可能希望捕获的条件。
 *
 * <p>类 {@code Exception} 以及不是 {@link RuntimeException} 子类的任何子类是 <em>检查异常</em>。
 * 如果方法或构造器的执行可能抛出检查异常，并且异常传播到方法或构造器边界之外，则需要在方法或构造器的 {@code throws} 子句中声明这些异常。
 *
 * @author  Frank Yellin
 * @see     java.lang.Error
 * @jls 11.2 异常的编译时检查
 * @since   JDK1.0
 */
public class Exception extends Throwable {
    static final long serialVersionUID = -3387516993124229948L;

    /**
     * 构造一个新的异常，其详细消息为 {@code null}。原因未初始化，可以通过调用 {@link #initCause} 随后初始化。
     */
    public Exception() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个新的异常。原因未初始化，可以通过调用 {@link #initCause} 随后初始化。
     *
     * @param   message   详细消息。详细消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public Exception(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的异常。注意，与 {@code cause} 关联的详细消息 <i>不会</i> 自动包含在
     * 此异常的详细消息中。
     *
     * @param  message 详细消息（稍后由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt> 值，表示原因不存在或未知。
     * @since  1.4
     */
    public Exception(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt>（通常包含 <tt>cause</tt> 的类和详细消息）
     * 构造一个新的异常。此构造器对于基本上只是其他可抛出对象的包装器的异常（例如，{@link
     * java.security.PrivilegedActionException}）非常有用。
     *
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt> 值，表示原因不存在或未知。
     * @since  1.4
     */
    public Exception(Throwable cause) {
        super(cause);
    }

    /**
     * 使用指定的详细消息、原因、是否启用抑制以及是否启用可写堆栈跟踪构造一个新的异常。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null} 值，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     * @since 1.7
     */
    protected Exception(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
