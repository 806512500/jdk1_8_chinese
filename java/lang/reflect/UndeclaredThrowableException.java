/*
 * 版权所有 (c) 1999, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.reflect;

/**
 * 当代理实例上的方法调用时，如果其调用处理器的 {@link InvocationHandler#invoke invoke} 方法抛出一个
 * 检查异常（一个不能分配给 {@code RuntimeException} 或 {@code Error} 的 {@code Throwable}），
 * 且该异常不能分配给代理实例上调用的方法的 {@code throws} 子句中声明的任何异常类型，则抛出此异常。
 *
 * <p>{@code UndeclaredThrowableException} 实例包含由调用处理器抛出的未声明的检查异常，
 * 可以通过 {@code getUndeclaredThrowable()} 方法检索。
 * {@code UndeclaredThrowableException} 继承自 {@code RuntimeException}，
 * 因此它是一个包装检查异常的未检查异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链机制。
 * 在构造时提供的“由调用处理器抛出的未声明的检查异常”（可以通过 {@link #getUndeclaredThrowable()} 方法访问）
 * 现在被称为 <i>原因</i>，并且可以通过 {@link Throwable#getCause()} 方法以及上述“遗留方法”访问。
 *
 * @author      Peter Jones
 * @see         InvocationHandler
 * @since       1.3
 */
public class UndeclaredThrowableException extends RuntimeException {
    static final long serialVersionUID = 330127114055056639L;

    /**
     * 被抛出的未声明的检查异常
     * @serial
     */
    private Throwable undeclaredThrowable;

    /**
     * 使用指定的 {@code Throwable} 构造一个 {@code UndeclaredThrowableException}。
     *
     * @param   undeclaredThrowable 被抛出的未声明的检查异常
     */
    public UndeclaredThrowableException(Throwable undeclaredThrowable) {
        super((Throwable) null);  // 禁止 initCause
        this.undeclaredThrowable = undeclaredThrowable;
    }

    /**
     * 使用指定的 {@code Throwable} 和详细消息构造一个 {@code UndeclaredThrowableException}。
     *
     * @param   undeclaredThrowable 被抛出的未声明的检查异常
     * @param   s 详细消息
     */
    public UndeclaredThrowableException(Throwable undeclaredThrowable,
                                        String s)
    {
        super(s, null);  // 禁止 initCause
        this.undeclaredThrowable = undeclaredThrowable;
    }

    /**
     * 返回包装在此 {@code UndeclaredThrowableException} 中的 {@code Throwable} 实例，可能为 {@code null}。
     *
     * <p>此方法早于通用异常链机制。
     * 现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @return 被抛出的未声明的检查异常
     */
    public Throwable getUndeclaredThrowable() {
        return undeclaredThrowable;
    }

    /**
     * 返回此异常的原因（包装在此 {@code UndeclaredThrowableException} 中的 {@code Throwable} 实例，可能为 {@code null}）。
     *
     * @return  此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return undeclaredThrowable;
    }
}
