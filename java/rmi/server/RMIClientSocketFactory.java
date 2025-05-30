/*
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
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
 * <code>RMIClientSocketFactory</code> 实例用于 RMI 运行时，以便为 RMI 调用获取客户端套接字。远程对象可以在创建/导出时与 <code>RMIClientSocketFactory</code> 关联，通过 <code>java.rmi.server.UnicastRemoteObject</code> 和 <code>java.rmi.activation.Activatable</code> 的构造函数或 <code>exportObject</code> 方法。
 *
 * <p>与远程对象关联的 <code>RMIClientSocketFactory</code> 实例将在远程对象的引用通过 RMI 调用传输时下载到客户端。此 <code>RMIClientSocketFactory</code> 将用于为远程方法调用创建连接到远程对象的连接。
 *
 * <p><code>RMIClientSocketFactory</code> 实例也可以与远程对象注册表关联，以便客户端可以使用自定义套接字通信与远程对象注册表进行通信。
 *
 * <p>此接口的实现应该是可序列化的，并且应该实现 {@link Object#equals} 以在传递一个表示相同（功能等效）的客户端套接字工厂实例时返回 <code>true</code>，否则返回 <code>false</code>（并且还应与 <code>Object.equals</code> 实现一致地实现 {@link Object#hashCode}）。
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
     * 创建一个连接到指定主机和端口的客户端套接字。
     * @param  host   主机名
     * @param  port   端口号
     * @return 连接到指定主机和端口的套接字。
     * @exception IOException 如果在创建套接字时发生 I/O 错误
     * @since 1.2
     */
    public Socket createSocket(String host, int port)
        throws IOException;
}
