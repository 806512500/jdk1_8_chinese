/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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


package java.util.logging;

import java.io.*;
import java.net.*;

/**
 * 简单的网络日志处理程序。
 * <p>
 * <tt>LogRecords</tt> 被发布到网络流连接。默认情况下，使用 <tt>XMLFormatter</tt> 类进行格式化。
 * <p>
 * <b>配置：</b>
 * 默认情况下，每个 <tt>SocketHandler</tt> 使用以下 <tt>LogManager</tt> 配置属性进行初始化，其中 <tt>&lt;handler-name&gt;</tt>
 * 是处理程序的完全限定类名。如果属性未定义
 * （或具有无效值），则使用指定的默认值。
 * <ul>
 * <li>   &lt;handler-name&gt;.level
 *        指定 <tt>Handler</tt> 的默认级别
 *        （默认为 <tt>Level.ALL</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.filter
 *        指定要使用的 <tt>Filter</tt> 类
 *        （默认为无 <tt>Filter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.formatter
 *        指定要使用的 <tt>Formatter</tt> 类
 *        （默认为 <tt>java.util.logging.XMLFormatter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.encoding
 *        要使用的字符集编码名称（默认为
 *        平台默认编码）。 </li>
 * <li>   &lt;handler-name&gt;.host
 *        指定要连接的目标主机名（无默认值）。 </li>
 * <li>   &lt;handler-name&gt;.port
 *        指定要使用的目标 TCP 端口（无默认值）。 </li>
 * </ul>
 * <p>
 * 例如，<tt>SocketHandler</tt> 的属性为：
 * <ul>
 * <li>   java.util.logging.SocketHandler.level=INFO </li>
 * <li>   java.util.logging.SocketHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * 对于自定义处理程序，例如 com.foo.MyHandler，属性为：
 * <ul>
 * <li>   com.foo.MyHandler.level=INFO </li>
 * <li>   com.foo.MyHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * 输出 IO 流是缓冲的，但在每个
 * <tt>LogRecord</tt> 写入后都会刷新。
 *
 * @since 1.4
 */

public class SocketHandler extends StreamHandler {
    private Socket sock;
    private String host;
    private int port;

    // 私有方法，用于从 LogManager 属性和/或类
    // javadoc 中指定的默认值配置 SocketHandler。
    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        setLevel(manager.getLevelProperty(cname +".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname +".filter", null));
        setFormatter(manager.getFormatterProperty(cname +".formatter", new XMLFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname +".encoding", null));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception ex2) {
                // 使用 null 进行 setEncoding 应该总是有效的。
                // assert false;
            }
        }
        port = manager.getIntProperty(cname + ".port", 0);
        host = manager.getStringProperty(cname + ".host", null);
    }


    /**
     * 创建一个 <tt>SocketHandler</tt>，仅使用 <tt>LogManager</tt> 属性
     * （或其默认值）。
     * @throws IllegalArgumentException 如果主机或端口无效或
     *          未作为 LogManager 属性指定。
     * @throws IOException 如果无法连接到目标
     *         主机和端口。
     */
    public SocketHandler() throws IOException {
        // 我们将使用日志记录的默认值。
        sealed = false;
        configure();

        try {
            connect();
        } catch (IOException ix) {
            System.err.println("SocketHandler: connect failed to " + host + ":" + port);
            throw ix;
        }
        sealed = true;
    }

    /**
     * 使用指定的主机和端口构造一个 <tt>SocketHandler</tt>。
     *
     * <tt>SocketHandler</tt> 基于 <tt>LogManager</tt>
     * 属性（或其默认值）进行配置，但使用给定的目标主机
     * 和端口参数。如果主机参数为空字符串，则使用本地主机。
     *
     * @param host 目标主机。
     * @param port 目标端口。
     *
     * @throws IllegalArgumentException 如果主机或端口无效。
     * @throws IOException 如果无法连接到目标
     *         主机和端口。
     */
    public SocketHandler(String host, int port) throws IOException {
        sealed = false;
        configure();
        sealed = true;
        this.port = port;
        this.host = host;
        connect();
    }

    private void connect() throws IOException {
        // 检查参数是否有效。
        if (port == 0) {
            throw new IllegalArgumentException("Bad port: " + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("Null host name: " + host);
        }

        // 尝试打开一个新的套接字。
        sock = new Socket(host, port);
        OutputStream out = sock.getOutputStream();
        BufferedOutputStream bout = new BufferedOutputStream(out);
        setOutputStream(bout);
    }

    /**
     * 关闭此输出流。
     *
     * @exception  SecurityException  如果存在安全管理者，并且
     *             调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    @Override
    public synchronized void close() throws SecurityException {
        super.close();
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ix) {
                // 忽略。
            }
        }
        sock = null;
    }

    /**
     * 格式化并发布一个 <tt>LogRecord</tt>。
     *
     * @param  record  日志事件的描述。如果记录为 null，则
     *                 静默忽略并不会发布。
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        super.publish(record);
        flush();
    }
}
