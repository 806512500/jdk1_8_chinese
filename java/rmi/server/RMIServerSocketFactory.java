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
 * <code>RMIServerSocketFactory</code> 实例用于 RMI 运行时，以便为 RMI 调用获取服务器套接字。远程对象在通过
 * <code>java.rmi.server.UnicastRemoteObject</code> 和 <code>java.rmi.activation.Activatable</code> 的构造函数或
 * <code>exportObject</code> 方法创建/导出时，可以与 <code>RMIServerSocketFactory</code> 关联。
 *
 * <p>与远程对象关联的 <code>RMIServerSocketFactory</code> 实例用于获取用于接受客户端传入调用的 <code>ServerSocket</code>。
 *
 * <p><code>RMIServerSocketFactory</code> 实例也可以与远程对象注册表关联，以便客户端可以使用自定义套接字与远程对象注册表进行通信。
 *
 * <p>此接口的实现应实现 {@link Object#equals}，当传递的实例表示相同（功能等效）的服务器套接字工厂时返回 <code>true</code>，否则返回 <code>false</code>
 * （并且还应与其 <code>Object.equals</code> 实现一致地实现 {@link Object#hashCode}）。
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   1.2
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.registry.LocateRegistry
 */
public interface RMIServerSocketFactory {

    /**
     * 在指定的端口上创建服务器套接字（端口 0 表示匿名端口）。
     * @param  port 端口号
     * @return 指定端口上的服务器套接字
     * @exception IOException 如果在创建服务器套接字时发生 I/O 错误
     * @since 1.2
     */
    public ServerSocket createServerSocket(int port)
        throws IOException;
}
