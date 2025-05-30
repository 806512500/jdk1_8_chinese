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
 * <code>RMIServerSocketFactory</code> 实例由 RMI 运行时使用，以获取 RMI 调用的服务器套接字。远程对象可以在通过
 * <code>java.rmi.server.UnicastRemoteObject</code> 和 <code>java.rmi.activation.Activatable</code> 的构造函数或
 * <code>exportObject</code> 方法创建/导出时与 <code>RMIServerSocketFactory</code> 关联。
 *
 * <p>与远程对象关联的 <code>RMIServerSocketFactory</code> 实例用于获取用于接受客户端传入调用的 <code>ServerSocket</code>。
 *
 * <p><code>RMIServerSocketFactory</code> 实例也可以与远程对象注册表关联，以便客户端可以使用自定义套接字与远程对象注册表进行通信。
 *
 * <p>此接口的实现应实现 {@link Object#equals} 以在传递的实例表示相同（功能等效）的服务器套接字工厂时返回 <code>true</code>，
 * 否则返回 <code>false</code>（并且还应与 <code>Object.equals</code> 实现一致地实现 {@link Object#hashCode}）。
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
     * 在指定的端口（端口 0 表示匿名端口）上创建服务器套接字。
     * @param  port 端口号
     * @return 指定端口上的服务器套接字
     * @exception IOException 如果在创建服务器套接字期间发生 I/O 错误
     * @since 1.2
     */
    public ServerSocket createServerSocket(int port)
        throws IOException;
}
