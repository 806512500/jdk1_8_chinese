/*
 * 版权所有 (c) 1995, 2011，Oracle 和/或其附属公司。保留所有权利。
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
 * 抛出以指示 Java 虚拟机已损坏或已耗尽继续运行所需的资源。
 *
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
abstract public class VirtualMachineError extends Error {
    private static final long serialVersionUID = 4161983926571568670L;

    /**
     * 构造一个没有详细消息的 <code>VirtualMachineError</code>。
     */
    public VirtualMachineError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>VirtualMachineError</code>。
     *
     * @param   message   详细消息。
     */
    public VirtualMachineError(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code VirtualMachineError}。 <p>请注意，与 {@code cause} 关联的详细消息
     * <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。 （允许 {@code null} 值，表示原因不存在或未知。）
     * @since  1.8
     */
    public VirtualMachineError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 构造一个 {@code VirtualMachineError}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。 （允许 {@code null} 值，表示原因不存在或未知。）
     * @since  1.8
     */
    public VirtualMachineError(Throwable cause) {
        super(cause);
    }
}
