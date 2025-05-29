/*
 * 版权所有 (c) 1995, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 当 Java 虚拟机中发生未知但严重的异常时抛出。
 *
 * @author 未署名
 * @since   JDK1.0
 */
public
class UnknownError extends VirtualMachineError {
    private static final long serialVersionUID = 2524784860676771849L;

    /**
     * 构造一个没有详细消息的 <code>UnknownError</code>。
     */
    public UnknownError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>UnknownError</code>。
     *
     * @param   s   详细消息。
     */
    public UnknownError(String s) {
        super(s);
    }
}