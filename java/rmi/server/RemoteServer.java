/*
 * Copyright (c) 1996, 2002, Oracle and/or its affiliates. All rights reserved.
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

import java.rmi.*;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.runtime.Log;

/**
 * <code>RemoteServer</code> 类是服务器实现的公共超类，提供了支持广泛远程引用语义的框架。具体来说，创建和导出远程对象（即使其远程可用）所需的功能由 <code>RemoteServer</code> 抽象地提供，并由其子类具体实现。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public abstract class RemoteServer extends RemoteObject
{
    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -4100238210092549637L;

    /**
     * 构造 <code>RemoteServer</code>。
     * @since JDK1.1
     */
    protected RemoteServer() {
        super();
    }

    /**
     * 使用给定的引用类型构造 <code>RemoteServer</code>。
     *
     * @param ref 远程引用
     * @since JDK1.1
     */
    protected RemoteServer(RemoteRef ref) {
        super(ref);
    }

    /**
     * 返回当前线程中正在处理的远程方法调用的客户端主机的字符串表示。
     *
     * @return  客户端主机的字符串表示
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
     * <p>如果有安全经理，其 <code>checkPermission</code> 方法将被调用，权限为 <code>java.util.logging.LoggingPermission("control")</code>；这可能会导致 <code>SecurityException</code>。
     *
     * @param   out 应记录 RMI 调用的输出流
     * @throws  SecurityException  如果有安全经理且其 <code>checkPermission</code> 方法调用失败
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
