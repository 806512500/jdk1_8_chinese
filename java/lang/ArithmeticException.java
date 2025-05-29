
/*
 * 版权所有 (c) 1994, 2011，Oracle 及/或其关联公司。保留所有权利。
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
 * 当发生异常的算术条件时抛出。例如，整数“除以零”会抛出此类的一个实例。
 *
 * {@code ArithmeticException} 对象可能由虚拟机构造，如同 {@linkplain Throwable#Throwable(String,
 * Throwable, boolean, boolean) 禁用了抑制和/或堆栈跟踪不可写} 一样。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class ArithmeticException extends RuntimeException {
    private static final long serialVersionUID = 2256477558314496007L;

    /**
     * 构造一个没有详细消息的 {@code ArithmeticException}。
     */
    public ArithmeticException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code ArithmeticException}。
     *
     * @param   s   详细消息。
     */
    public ArithmeticException(String s) {
        super(s);
    }
}