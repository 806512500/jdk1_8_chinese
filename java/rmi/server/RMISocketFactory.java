/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.rmi.server;

import java.io.*;
import java.net.*;

/**
 * 一个 <code>RMISocketFactory</code> 实例用于 RMI 运行时，以便为 RMI 调用获取客户端和服务器套接字。应用程序可以使用
 * <code>setSocketFactory</code> 方法请求 RMI 运行时使用其套接字工厂实例，而不是默认实现。
 *
 * <p>默认的套接字工厂实现执行一个三层的方法来创建客户端套接字。首先，尝试直接连接到远程 VM。如果失败（由于防火墙），运行时使用带有显式端口号的 HTTP。
 * 如果防火墙不允许这种通信，则使用 HTTP 到服务器上的 cgi-bin 脚本来 POST RMI 调用。HTTP 隧道机制默认是禁用的。这种行为由 {@code java.rmi.server.disableHttp}
 * 属性控制，默认值为 {@code true}。将此属性的值设置为 {@code false} 将启用 HTTP 隧道机制。
 *
 * <p><strong>已弃用：HTTP 隧道。</strong> <em>上述描述的 HTTP 隧道机制，特别是带有显式端口的 HTTP 和到 cgi-bin 脚本的 HTTP，已弃用。这些 HTTP 隧道机制
 * 可能在平台的未来版本中被移除。</em>
 *
 * <p>默认的套接字工厂实现创建的服务器套接字绑定到通配符地址，接受来自所有网络接口的请求。
 *
 * @implNote
 * <p>您可以使用 {@code RMISocketFactory} 类创建一个绑定到特定地址的服务器套接字，限制请求的来源。例如，以下代码实现了一个将服务器套接字绑定到 IPv4
 * 回环地址的套接字工厂。这限制了 RMI 仅处理来自本地主机的请求。
 *
 * <pre>{@code
 *     class LoopbackSocketFactory extends RMISocketFactory {
 *         public ServerSocket createServerSocket(int port) throws IOException {
 *             return new ServerSocket(port, 5, InetAddress.getByName("127.0.0.1"));
 *         }
 *
 *         public Socket createSocket(String host, int port) throws IOException {
 *             // 只调用默认的客户端套接字工厂
 *             return RMISocketFactory.getDefaultSocketFactory()
 *                                    .createSocket(host, port);
 *         }
 *     }
 *
 *     // ...
 *
 *     RMISocketFactory.setSocketFactory(new LoopbackSocketFactory());
 * }</pre>
 *
 * 设置 {@code java.rmi.server.hostname} 系统属性为 {@code 127.0.0.1} 以确保生成的存根连接到正确的网络接口。
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   JDK1.1
 */
public abstract class RMISocketFactory
        implements RMIClientSocketFactory, RMIServerSocketFactory
{

    /** RMI 运行时使用的客户端/服务器套接字工厂 */
    private static RMISocketFactory factory = null;
    /** 本 RMI 实现使用的默认套接字工厂 */
    private static RMISocketFactory defaultSocketFactory;
    /** 套接字创建失败的处理程序 */
    private static RMIFailureHandler handler = null;

    /**
     * 构造一个 <code>RMISocketFactory</code>。
     * @since JDK1.1
     */
    public RMISocketFactory() {
        super();
    }

    /**
     * 创建一个连接到指定主机和端口的客户端套接字。
     * @param  host   主机名
     * @param  port   端口号
     * @return 连接到指定主机和端口的套接字。
     * @exception IOException 如果在创建套接字时发生 I/O 错误
     * @since JDK1.1
     */
    public abstract Socket createSocket(String host, int port)
        throws IOException;

    /**
     * 在指定端口上创建一个服务器套接字（端口 0 表示匿名端口）。
     * @param  port 端口号
     * @return 指定端口上的服务器套接字
     * @exception IOException 如果在创建服务器套接字时发生 I/O 错误
     * @since JDK1.1
     */
    public abstract ServerSocket createServerSocket(int port)
        throws IOException;

    /**
     * 设置 RMI 获取套接字的全局套接字工厂（如果远程对象未关联特定的客户端和/或服务器套接字工厂）。RMI 套接字工厂只能设置一次。注意：只有当前安全管理器允许设置套接字工厂时，
     * 才能设置 RMISocketFactory；如果被禁止，将抛出 SecurityException。
     * @param fac 套接字工厂
     * @exception IOException 如果 RMI 套接字工厂已设置
     * @exception  SecurityException  如果存在安全管理器且其 <code>checkSetFactory</code> 方法不允许此操作。
     * @see #getSocketFactory
     * @see java.lang.SecurityManager#checkSetFactory()
     * @since JDK1.1
     */
    public synchronized static void setSocketFactory(RMISocketFactory fac)
        throws IOException
    {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    /**
     * 返回由 <code>setSocketFactory</code> 方法设置的套接字工厂。如果未设置套接字工厂，则返回 <code>null</code>。
     * @return 套接字工厂
     * @see #setSocketFactory(RMISocketFactory)
     * @since JDK1.1
     */
    public synchronized static RMISocketFactory getSocketFactory()
    {
        return factory;
    }

    /**
     * 返回此 RMI 实现使用的默认套接字工厂。当 <code>getSocketFactory</code> 返回 <code>null</code> 时，RMI 运行时将使用此工厂。
     * @return 默认的 RMI 套接字工厂
     * @since JDK1.1
     */
    public synchronized static RMISocketFactory getDefaultSocketFactory() {
        if (defaultSocketFactory == null) {
            defaultSocketFactory =
                new sun.rmi.transport.proxy.RMIMasterSocketFactory();
        }
        return defaultSocketFactory;
    }

    /**
     * 设置在服务器套接字创建失败时由 RMI 运行时调用的失败处理程序。默认情况下，如果没有安装失败处理程序且服务器套接字创建失败，RMI 运行时将尝试重新创建服务器套接字。
     *
     * <p>如果有安全管理器，此方法首先调用安全管理器的 <code>checkSetFactory</code> 方法以确保允许此操作。
     * 这可能导致 <code>SecurityException</code>。
     *
     * @param fh 失败处理程序
     * @throws  SecurityException  如果存在安全管理器且其 <code>checkSetFactory</code> 方法不允许此操作。
     * @see #getFailureHandler
     * @see java.rmi.server.RMIFailureHandler#failure(Exception)
     * @since JDK1.1
     */
    public synchronized static void setFailureHandler(RMIFailureHandler fh)
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        handler = fh;
    }

    /**
     * 返回由 <code>setFailureHandler</code> 方法设置的套接字创建失败处理程序。
     * @return 失败处理程序
     * @see #setFailureHandler(RMIFailureHandler)
     * @since JDK1.1
     */
    public synchronized static RMIFailureHandler getFailureHandler()
    {
        return handler;
    }
}
