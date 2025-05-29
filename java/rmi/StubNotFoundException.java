/*
 * 版权所有 (c) 1996, 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 如果在导出远程对象时找不到有效的存根类，则抛出 <code>StubNotFoundException</code>。
 * 当通过 <code>java.rmi.activation.Activatable.register</code> 方法注册可激活对象时，
 * 也可能抛出 <code>StubNotFoundException</code>。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 */
public class StubNotFoundException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -7088199405468872373L;

    /**
     * 使用指定的详细消息构造 <code>StubNotFoundException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public StubNotFoundException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>StubNotFoundException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public StubNotFoundException(String s, Exception ex) {
        super(s, ex);
    }
}
