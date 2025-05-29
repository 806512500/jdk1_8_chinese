/*
 * 版权所有 (c) 1997, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.activation;

/**
 * 由激活接口使用的通用异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链机制。在构造时可以提供的“详细异常”并通过公共
 * {@link #detail} 字段访问的异常现在被称为<i>原因</i>，也可以通过 {@link Throwable#getCause()} 方法访问，
 * 以及上述“遗留字段”。
 *
 * <p>在 <code>ActivationException</code> 的实例上调用 {@link Throwable#initCause(Throwable)} 方法
 * 始终会抛出 {@link IllegalStateException}。
 *
 * @author      Ann Wollrath
 * @since       1.2
 */
public class ActivationException extends Exception {

    /**
     * 激活异常的原因。
     *
     * <p>此字段早于通用异常链机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Throwable detail;

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -4320118837291406071L;

    /**
     * 构造一个 <code>ActivationException</code>。
     */
    public ActivationException() {
        initCause(null);  // 禁止后续 initCause
    }

    /**
     * 使用指定的详细消息构造一个 <code>ActivationException</code>。
     *
     * @param s 详细消息
     */
    public ActivationException(String s) {
        super(s);
        initCause(null);  // 禁止后续 initCause
    }

    /**
     * 使用指定的详细消息和原因构造一个 <code>ActivationException</code>。此构造函数将 {@link #detail}
     * 字段设置为指定的 <code>Throwable</code>。
     *
     * @param s 详细消息
     * @param cause 原因
     */
    public ActivationException(String s, Throwable cause) {
        super(s);
        initCause(null);  // 禁止后续 initCause
        detail = cause;
    }

    /**
     * 返回详细消息，包括此异常的任何原因的消息。
     *
     * @return 详细消息
     */
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else
            return super.getMessage() +
                "; 嵌套异常是: \n\t" +
                detail.toString();
    }

    /**
     * 返回此异常的原因。此方法返回 {@link #detail} 字段的值。
     *
     * @return 原因，可能是 <tt>null</tt>。
     * @since   1.4
     */
    public Throwable getCause() {
        return detail;
    }
}
