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
 * 如果尝试在远程虚拟机中已不存在的对象上调用方法，则抛出 <code>NoSuchObjectException</code>。
 * 如果在尝试调用远程对象的方法时发生 <code>NoSuchObjectException</code>，调用可以重新传输并仍然保持 RMI 的“最多一次”调用语义。
 *
 * <code>NoSuchObjectException</code> 也由方法 <code>java.rmi.server.RemoteObject.toStub</code> 以及
 * <code>java.rmi.server.UnicastRemoteObject</code> 和 <code>java.rmi.activation.Activatable</code> 的
 * <code>unexportObject</code> 方法抛出。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see     java.rmi.server.RemoteObject#toStub(Remote)
 * @see     java.rmi.server.UnicastRemoteObject#unexportObject(Remote,boolean)
 * @see     java.rmi.activation.Activatable#unexportObject(Remote,boolean)
 */
public class NoSuchObjectException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 6619395951570472985L;

    /**
     * 使用指定的详细消息构造 <code>NoSuchObjectException</code>。
     *
     * @param s 详细消息
     * @since   JDK1.1
     */
    public NoSuchObjectException(String s) {
        super(s);
    }
}
