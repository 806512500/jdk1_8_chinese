/*
 * 版权所有 (c) 1998, 2001, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.net.*;

/**
 * <code>RMIClientSocketFactory</code> 的实例由 RMI 运行时使用，以获取 RMI 调用的客户端套接字。远程对象可以在通过
 * <code>java.rmi.server.UnicastRemoteObject</code> 和 <code>java.rmi.activation.Activatable</code> 的构造函数或
 * <code>exportObject</code> 方法创建/导出时与 <code>RMIClientSocketFactory</code> 关联。
 *
 * <p>与远程对象关联的 <code>RMIClientSocketFactory</code> 实例将在远程对象的引用在 RMI 调用中传输时下载到客户端。此
 * <code>RMIClientSocketFactory</code> 将用于为远程方法调用创建与远程对象的连接。
 *
 * <p><code>RMIClientSocketFactory</code> 实例也可以与远程对象注册表关联，以便客户端可以使用自定义套接字通信与远程对象注册表进行通信。
 *
 * <p>此接口的实现应是可序列化的，并且应实现 {@link Object#equals} 以在传递表示相同（功能等效）的客户端套接字工厂实例时返回
 * <code>true</code>，否则返回 <code>false</code>（并且还应与其实现的 <code>Object.equals</code> 一致地实现
 * {@link Object#hashCode}）。
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   1.2
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.registry.LocateRegistry
 */
public interface RMIClientSocketFactory {

    /**
     * 创建连接到指定主机和端口的客户端套接字。
     * @param  host   主机名
     * @param  port   端口号
     * @return 连接到指定主机和端口的套接字。
     * @exception IOException 如果在创建套接字时发生 I/O 错误
     * @since 1.2
     */
    public Socket createSocket(String host, int port)
        throws IOException;
}
