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
 * 当 <code>java.rmi.Naming</code> 类的某些方法（具体为 <code>bind</code>,
 * <code>rebind</code> 和 <code>unbind</code>）以及 <code>java.rmi.activation.ActivationSystem</code>
 * 接口的方法表示调用者没有权限执行方法调用请求的操作时，将抛出 <code>AccessException</code>。
 * 如果方法是从非本地主机调用的，则抛出 <code>AccessException</code>。
 *
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see     java.rmi.Naming
 * @see     java.rmi.activation.ActivationSystem
 */
public class AccessException extends java.rmi.RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
     private static final long serialVersionUID = 6314925228044966088L;

    /**
     * 使用指定的详细消息构造 <code>AccessException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public AccessException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>AccessException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public AccessException(String s, Exception ex) {
        super(s, ex);
    }
}
