/*
 * 版权所有 (c) 1996, 2000, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示在静态初始化器中发生了意外异常。
 * <code>ExceptionInInitializerError</code> 被抛出以指示在静态初始化器或静态变量的初始化器评估期间发生了异常。
 *
 * <p>自 1.4 版本起，此异常已被改造以符合通用的异常链接机制。在构造时可能提供的“保存的可抛出对象”以及通过
 * {@link #getException()} 方法访问的现在被称为 <i>原因</i>，也可以通过 {@link Throwable#getCause()} 方法访问，
 * 以及上述“遗留方法”。
 *
 * @author  Frank Yellin
 * @since   JDK1.1
 */
public class ExceptionInInitializerError extends LinkageError {
    /**
     * 为了互操作性，使用 JDK 1.1.X 的 serialVersionUID
     */
    private static final long serialVersionUID = 1521711792217232256L;

    /**
     * 如果使用了 ExceptionInInitializerError(Throwable thrown) 构造器来实例化对象，则此字段保存异常
     *
     * @serial
     *
     */
    private Throwable exception;

    /**
     * 构造一个 <code>ExceptionInInitializerError</code>，其详细消息字符串为 <code>null</code>，并且没有保存的
     * 可抛出对象。详细消息是一个描述此特定异常的字符串。
     */
    public ExceptionInInitializerError() {
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 通过保存一个 <code>Throwable</code> 对象的引用来构造一个新的 <code>ExceptionInInitializerError</code> 类，
     * 该引用可以通过 {@link #getException()} 方法稍后检索。详细消息字符串设置为 <code>null</code>。
     *
     * @param thrown 抛出的异常
     */
    public ExceptionInInitializerError(Throwable thrown) {
        initCause(null);  // 禁止后续的 initCause
        this.exception = thrown;
    }

    /**
     * 使用指定的详细消息字符串构造一个 ExceptionInInitializerError。详细消息是一个描述此特定异常的字符串。
     * 详细消息字符串将保存以供 {@link Throwable#getMessage()} 方法稍后检索。没有保存的可抛出对象。
     *
     *
     * @param s 详细消息
     */
    public ExceptionInInitializerError(String s) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 返回在静态初始化期间发生的导致此错误创建的异常。
     *
     * <p>此方法早于通用的异常链接设施。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方法。
     *
     * @return 此 <code>ExceptionInInitializerError</code> 的保存的可抛出对象，或 <code>null</code>
     *         如果此 <code>ExceptionInInitializerError</code> 没有保存的可抛出对象。
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 返回此错误的原因（在静态初始化期间发生的导致此错误创建的异常）。
     *
     * @return  此错误的原因或 <code>null</code>，如果原因不存在或未知。
     * @since   1.4
     */
    public Throwable getCause() {
        return exception;
    }
}
