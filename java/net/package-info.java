/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 提供实现网络应用程序的类。
 *
 * <p> java.net 包可以大致分为两部分：</p>
 * <ul>
 *     <li><p><i>低级 API</i>，处理以下抽象：</p>
 *     <ul>
 *       <li><p><i>地址</i>，即网络标识符，如 IP 地址。</p></li>
 *       <li><p><i>套接字</i>，即基本的双向数据通信机制。</p></li>
 *       <li><p><i>接口</i>，描述网络接口。</p></li>
 *     </ul></li>
 *     <li> <p><i>高级 API</i>，处理以下抽象：</p>
 *     <ul>
 *       <li><p><i>URI</i>，表示通用资源标识符。</p></li>
 *       <li><p><i>URL</i>，表示通用资源定位符。</p></li>
 *       <li><p><i>连接</i>，表示与 <i>URL</i> 指向的资源的连接。</p></li>
 *       </ul></li>
 * </ul>
 * <h2>地址</h2>
 * <p>地址在 java.net API 中用作主机标识符或套接字端点标识符。</p>
 * <p>{@link java.net.InetAddress} 类是表示 IP（互联网协议）地址的抽象类。它有两个子类：
 * <ul>
 *       <li>{@link java.net.Inet4Address} 用于 IPv4 地址。</li>
 *       <li>{@link java.net.Inet6Address} 用于 IPv6 地址。</li>
 * </ul>
 * <p>但在大多数情况下，不需要直接处理子类，因为 InetAddress 抽象应该涵盖所需的大部分功能。</p>
 * <h3><b>关于 IPv6</b></h3>
 * <p>并非所有系统都支持 IPv6 协议，虽然 Java 网络堆栈会尝试在可用时检测并透明地使用它，但也可以通过系统属性禁用其使用。
 * 在 IPv6 不可用或显式禁用的情况下，Inet6Address 不再是大多数网络操作的有效参数。虽然像 {@link java.net.InetAddress#getByName} 这样的方法在查找主机名时保证不会返回 Inet6Address，
 * 但通过传递字面量，可以创建这样的对象。在这种情况下，大多数方法在使用 Inet6Address 调用时会抛出异常。</p>
 * <h2>套接字</h2>
 * <p>套接字是通过网络在机器之间建立通信链接的手段。java.net 包提供了 4 种套接字：</p>
 * <ul>
 *       <li>{@link java.net.Socket} 是 TCP 客户端 API，通常用于 {@linkplain java.net.Socket#connect(SocketAddress)
 *            连接到远程主机}。</li>
 *       <li>{@link java.net.ServerSocket} 是 TCP 服务器 API，通常用于 {@linkplain java.net.ServerSocket#accept 接受}
 *            客户端套接字的连接。</li>
 *       <li>{@link java.net.DatagramSocket} 是 UDP 端点 API，用于 {@linkplain java.net.DatagramSocket#send 发送} 和
 *            {@linkplain java.net.DatagramSocket#receive 接收} {@linkplain java.net.DatagramPacket 数据报包}。</li>
 *       <li>{@link java.net.MulticastSocket} 是 {@code DatagramSocket} 的子类，用于处理多播组。</li>
 * </ul>
 * <p>通过 TCP 套接字发送和接收数据是通过 {@link java.net.Socket#getInputStream} 和
 *    {@link java.net.Socket#getOutputStream} 方法获取的 InputStream 和 OutputStream 完成的。</p>
 * <h2>接口</h2>
 * <p>{@link java.net.NetworkInterface} 类提供了浏览和查询本地机器上所有网络接口（如以太网连接或 PPP 端点）的 API。
 * 通过该类可以检查本地接口是否配置为支持 IPv6。</p>
 * <p>注意，所有符合标准的实现必须支持至少一个 {@code NetworkInterface} 对象，该对象必须连接到网络，或者是一个只能与同一机器上的实体通信的“回环”接口。</p>
 *
 * <h2>高级 API</h2>
 * <p>java.net 包中的许多类提供了更高层次的抽象，允许轻松访问网络上的资源。这些类包括：
 * <ul>
 *       <li>{@link java.net.URI} 是表示通用资源标识符的类，如 RFC 2396 所指定。
 *            顾名思义，这只是一个标识符，不直接提供访问资源的手段。</li>
 *       <li>{@link java.net.URL} 是表示通用资源定位符的类，它既是 URI 的一个较旧的概念，也是访问资源的手段。</li>
 *       <li>{@link java.net.URLConnection} 是从 URL 创建的，用于访问 URL 指向的资源的通信链接。这个抽象类将大部分工作委托给底层的协议处理程序，如 http 或 https。</li>
 *       <li>{@link java.net.HttpURLConnection} 是 URLConnection 的子类，提供了一些特定于 HTTP 协议的额外功能。</li>
 * </ul>
 * <p>建议的用法是使用 {@link java.net.URI} 识别资源，然后在需要访问资源时将其转换为 {@link java.net.URL}。从该 URL，您可以获取 {@link java.net.URLConnection} 以进行细粒度控制，或直接获取 InputStream。
 * <p>以下是一个示例：</p>
 * <pre>
 * URI uri = new URI("http://java.sun.com/");
 * URL url = uri.toURL();
 * InputStream in = url.openStream();
 * </pre>
 * <h2>协议处理程序</h2>
 * 如前所述，URL 和 URLConnection 依赖于必须存在的协议处理程序，否则将抛出异常。这是与 URI 的主要区别，URI 仅用于标识资源，因此不需要访问协议处理程序。因此，虽然可以使用任何协议方案（例如 {@code myproto://myhost.mydomain/resource/}）创建 URI，
 * 但类似的 URL 将尝试实例化指定协议的处理程序；如果不存在，则会抛出异常。
 * <p>默认情况下，协议处理程序是从默认位置动态加载的。但是，可以通过设置 {@code java.protocol.handler.pkgs} 系统属性来添加搜索路径。例如，如果将其设置为 {@code myapp.protocols}，则在处理 http 时，URL 代码将首先尝试加载 {@code myapp.protocols.http.Handler}，如果失败，则从默认位置加载 {@code http.Handler}。
 * <p>注意，Handler 类 <b>必须</b> 是抽象类 {@link java.net.URLStreamHandler} 的子类。</p>
 * <h2>附加规范</h2>
 * <ul>
 *       <li><a href="doc-files/net-properties.html">
 *            网络系统属性</a></li>
 * </ul>
 *
 * @since JDK1.0
 */
package java.net;
