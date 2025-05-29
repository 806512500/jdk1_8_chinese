/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 当 {@code doPrivileged(PrivilegedExceptionAction)} 和
 * {@code doPrivileged(PrivilegedExceptionAction,
 * AccessControlContext context)} 方法检测到执行的操作抛出了一个检查异常时，会抛出此异常。通过调用
 * {@code getException} 方法可以获取操作抛出的异常。实际上，
 * {@code PrivilegedActionException} 是一个“包装器”，
 * 用于包装特权操作抛出的异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链接机制。构造时提供的“特权计算抛出的异常”
 * 以及通过 {@link #getException()} 方法访问的异常现在被称为
 * <i>原因</i>，可以通过 {@link Throwable#getCause()}
 * 方法以及上述“遗留方法”访问。
 *
 * @see PrivilegedExceptionAction
 * @see AccessController#doPrivileged(PrivilegedExceptionAction)
 * @see AccessController#doPrivileged(PrivilegedExceptionAction,AccessControlContext)
 */
public class PrivilegedActionException extends Exception {
    // 为了互操作性，使用 JDK 1.2.2 的 serialVersionUID
    private static final long serialVersionUID = 4724086851538908602L;

    /**
     * @serial
     */
    private Exception exception;

    /**
     * 构造一个新的 PrivilegedActionException “包装”
     * 特定的异常。
     *
     * @param exception 抛出的异常
     */
    public PrivilegedActionException(Exception exception) {
        super((Throwable)null);  // 禁止 initCause
        this.exception = exception;
    }

    /**
     * 返回导致此 {@code PrivilegedActionException} 的特权计算抛出的异常。
     *
     * <p>此方法早于通用异常链接机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方法。
     *
     * @return 导致此 {@code PrivilegedActionException} 的特权计算抛出的异常。
     * @see PrivilegedExceptionAction
     * @see AccessController#doPrivileged(PrivilegedExceptionAction)
     * @see AccessController#doPrivileged(PrivilegedExceptionAction,
     *                                            AccessControlContext)
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 返回此异常的原因（导致此
     * {@code PrivilegedActionException} 的特权计算抛出的异常）。
     *
     * @return 此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return exception;
    }

    public String toString() {
        String s = getClass().getName();
        return (exception != null) ? (s + ": " + exception.toString()) : s;
    }
}
