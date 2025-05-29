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

package java.rmi.server;

/**
 * <code>ServerNotActiveException</code> 是一个在调用 <code>RemoteServer.getClientHost</code> 时抛出的 <code>Exception</code>，
 * 如果在服务远程方法调用之外调用了 getClientHost 方法，则会抛出此异常。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see java.rmi.server.RemoteServer#getClientHost()
 */
public class ServerNotActiveException extends java.lang.Exception {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 4687940720827538231L;

    /**
     * 构造一个没有指定详细消息的 <code>ServerNotActiveException</code>。
     * @since JDK1.1
     */
    public ServerNotActiveException() {}

    /**
     * 使用指定的详细消息构造一个 <code>ServerNotActiveException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public ServerNotActiveException(String s)
    {
        super(s);
    }
}
