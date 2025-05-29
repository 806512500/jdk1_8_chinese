/*
 * 版权所有 (c) 1996, 2002, Oracle 和/或其附属公司。保留所有权利。
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

import java.rmi.*;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.runtime.Log;

/**
 * <code>RemoteServer</code> 类是服务器实现的公共超类，提供了支持广泛远程引用语义的框架。具体来说，<code>RemoteServer</code> 抽象地提供了创建和导出远程对象（即使它们可以远程访问）所需的功能，而这些功能由其子类具体实现。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public abstract class RemoteServer extends RemoteObject
{
    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -4100238210092549637L;

    /**
     * 构造一个 <code>RemoteServer</code>。
     * @since JDK1.1
     */
    protected RemoteServer() {
        super();
    }

    /**
     * 使用给定的引用类型构造一个 <code>RemoteServer</code>。
     *
     * @param ref 远程引用
     * @since JDK1.1
     */
    protected RemoteServer(RemoteRef ref) {
        super(ref);
    }

    /**
     * 返回当前线程中正在处理的远程方法调用的客户端主机的字符串表示形式。
     *
     * @return 客户端主机的字符串表示形式
     *
     * @throws  ServerNotActiveException 如果当前线程中没有远程方法调用正在处理
     *
     * @since   JDK1.1
     */
    public static String getClientHost() throws ServerNotActiveException {
        return sun.rmi.transport.tcp.TCPTransport.getClientHost();
    }

    /**
     * 将 RMI 调用记录到输出流 <code>out</code>。如果 <code>out</code> 为 <code>null</code>，则关闭调用记录。
     *
     * <p>如果有安全管理者，其 <code>checkPermission</code> 方法将被调用，权限为 <code>java.util.logging.LoggingPermission("control")</code>；这可能导致 <code>SecurityException</code>。
     *
     * @param   out RMI 调用应记录到的输出流
     * @throws  SecurityException 如果存在安全管理者，并且其 <code>checkPermission</code> 方法调用失败
     * @see #getLog
     * @since JDK1.1
     */
    public static void setLog(java.io.OutputStream out)
    {
        logNull = (out == null);
        UnicastServerRef.callLog.setOutputStream(out);
    }

    /**
     * 返回 RMI 调用日志的流。
     * @return 调用日志
     * @see #setLog
     * @since JDK1.1
     */
    public static java.io.PrintStream getLog()
    {
        return (logNull ? null : UnicastServerRef.callLog.getPrintStream());
    }

    // 初始化日志状态
    private static boolean logNull = !UnicastServerRef.logCalls;
}
