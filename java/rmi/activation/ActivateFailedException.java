/*
 * 版权所有 (c) 1998, 1999, Oracle 和/或其附属公司。保留所有权利。
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
 * 当激活失败时，RMI 运行时在对可激活对象的远程调用期间抛出此异常。
 *
 * @author      Ann Wollrath
 * @since       1.2
 */
public class ActivateFailedException extends java.rmi.RemoteException {

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = 4863550261346652506L;

    /**
     * 使用指定的详细消息构造 <code>ActivateFailedException</code>。
     *
     * @param s 详细消息
     * @since 1.2
     */
    public ActivateFailedException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ActivateFailedException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since 1.2
     */
    public ActivateFailedException(String s, Exception ex) {
        super(s, ex);
    }
}
