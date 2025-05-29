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
 * 如果远程方法调用的客户端接收到一个未在远程接口方法的 <code>throws</code> 子句中声明的检查异常，
 * 则抛出 <code>UnexpectedException</code>。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 */
public class UnexpectedException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 1800467484195073863L;

    /**
     * 使用指定的详细消息构造 <code>UnexpectedException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public UnexpectedException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>UnexpectedException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public UnexpectedException(String s, Exception ex) {
        super(s, ex);
    }
}
