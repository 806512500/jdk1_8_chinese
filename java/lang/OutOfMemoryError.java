/*
 * 版权所有 (c) 1994, 2011, Oracle 和/或其附属公司。保留所有权利。
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
 * 当 Java 虚拟机无法分配对象，因为它内存不足，并且垃圾回收器无法提供更多的内存时抛出。
 *
 * {@code OutOfMemoryError} 对象可能由虚拟机构建，就像 {@linkplain Throwable#Throwable(String, Throwable,
 * boolean, boolean) 抑制被禁用和/或堆栈跟踪不可写} 一样。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class OutOfMemoryError extends VirtualMachineError {
    private static final long serialVersionUID = 8228564086184010517L;

    /**
     * 构造一个没有详细消息的 {@code OutOfMemoryError}。
     */
    public OutOfMemoryError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code OutOfMemoryError}。
     *
     * @param   s   详细消息。
     */
    public OutOfMemoryError(String s) {
        super(s);
    }
}
