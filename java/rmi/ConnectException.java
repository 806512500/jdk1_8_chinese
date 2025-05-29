/*
 * 版权所有 (c) 1996, 1998, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi;

/**
 * 如果在远程方法调用时连接被远程主机拒绝，则抛出 <code>ConnectException</code>。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class ConnectException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
     private static final long serialVersionUID = 4863550261346652506L;

    /**
     * 使用指定的详细消息构造 <code>ConnectException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public ConnectException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ConnectException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public ConnectException(String s, Exception ex) {
        super(s, ex);
    }
}
