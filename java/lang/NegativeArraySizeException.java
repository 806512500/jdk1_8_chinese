/*
 * 版权所有 (c) 1994, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 如果应用程序尝试创建负大小的数组，则抛出此异常。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NegativeArraySizeException extends RuntimeException {
    private static final long serialVersionUID = -8960118058596991861L;

    /**
     * 构造一个没有详细消息的 <code>NegativeArraySizeException</code>。
     */
    public NegativeArraySizeException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NegativeArraySizeException</code>。
     *
     * @param   s   详细消息。
     */
    public NegativeArraySizeException(String s) {
        super(s);
    }
}
