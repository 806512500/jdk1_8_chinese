/*
 * 版权所有 (c) 1995, 2011, Oracle 和/或其附属公司。保留所有权利。
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
 * {@code Error} 是 {@code Throwable} 的一个子类，
 * 表示合理应用程序不应尝试捕获的严重问题。大多数此类错误都是异常情况。
 * {@code ThreadDeath} 错误，尽管是“正常”情况，
 * 也是 {@code Error} 的子类，因为大多数应用程序不应尝试捕获它。
 * <p>
 * 一个方法不需要在其 {@code throws} 子句中声明在方法执行期间可能抛出但未捕获的
 * {@code Error} 的任何子类，因为这些错误是不应发生的异常情况。
 *
 * 即，为了编译时检查异常的目的，{@code Error} 及其子类被视为未检查的异常。
 *
 * @author  Frank Yellin
 * @see     java.lang.ThreadDeath
 * @jls 11.2 编译时异常检查
 * @since   JDK1.0
 */
public class Error extends Throwable {
    static final long serialVersionUID = 4980196508277280342L;

    /**
     * 构造一个新的错误，其详细消息为 {@code null}。
     * 原因未初始化，可以通过调用 {@link #initCause} 随后初始化。
     */
    public Error() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个新的错误。原因未初始化，可以通过调用
     * {@link #initCause} 随后初始化。
     *
     * @param   message   详细消息。详细消息保存以供
     *          后续通过 {@link #getMessage()} 方法检索。
     */
    public Error(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的错误。注意，与
     * {@code cause} 关联的详细消息 <i>不会</i> 自动包含在
     * 此错误的详细消息中。
     *
     * @param  message 详细消息（保存以供后续通过
     *         {@link #getMessage()} 方法检索）。
     * @param  cause 原因（保存以供后续通过
     *         {@link #getCause()} 方法检索）。允许为 {@code null}，
     *         表示原因不存在或未知。
     * @since  1.4
     */
    public Error(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 构造一个新的错误
     * （通常包含 {@code cause} 的类和详细消息）。
     * 当错误主要是其他可抛出对象的包装器时，此构造函数很有用。
     *
     * @param  cause 原因（保存以供后续通过
     *         {@link #getCause()} 方法检索）。允许为 {@code null}，
     *         表示原因不存在或未知。
     * @since  1.4
     */
    public Error(Throwable cause) {
        super(cause);
    }

    /**
     * 使用指定的详细消息、原因、是否启用抑制以及是否启用可写堆栈跟踪
     * 构造一个新的错误。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null}，
     * 和表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     *
     * @since 1.7
     */
    protected Error(String message, Throwable cause,
                    boolean enableSuppression,
                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
