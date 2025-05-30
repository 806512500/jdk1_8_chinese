
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

/**
 * 定义 <em>标准</em> 套接字选项。
 *
 * <p> 本类定义的每个套接字选项的 {@link SocketOption#name 名称} 是其字段名称。
 *
 * <p> 在此版本中，这里定义的套接字选项由 {@link
 * java.nio.channels.NetworkChannel 网络} 通道在 {@link
 * java.nio.channels 通道} 包中使用。
 *
 * @since 1.7
 */

public final class StandardSocketOptions {
    private StandardSocketOptions() { }

    // -- SOL_SOCKET --

    /**
     * 允许传输广播数据报。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。该选项特定于发送到 {@link java.net.Inet4Address IPv4}
     * 广播地址的面向数据报的套接字。当套接字选项启用时，套接字可以用于发送 <em>广播数据报</em>。
     *
     * <p> 该套接字选项的初始值为 {@code FALSE}。该套接字选项可以在任何时候启用或禁用。某些操作系统可能需要以实现特定的权限启动 Java 虚拟机，以启用此选项或发送广播数据报。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc919.txt">RFC&nbsp;929:
     * Broadcasting Internet Datagrams</a>
     * @see DatagramSocket#setBroadcast
     */
    public static final SocketOption<Boolean> SO_BROADCAST =
        new StdSocketOption<Boolean>("SO_BROADCAST", Boolean.class);

    /**
     * 保持连接活跃。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。当 {@code SO_KEEPALIVE}
     * 选项启用时，操作系统可能会使用 <em>保持活跃</em> 机制定期探测连接的另一端，当连接处于空闲状态时。保持活跃机制的具体语义是系统依赖的，因此未指定。
     *
     * <p> 该套接字选项的初始值为 {@code FALSE}。该套接字选项可以在任何时候启用或禁用。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC&nbsp;1122
     * Requirements for Internet Hosts -- Communication Layers</a>
     * @see Socket#setKeepAlive
     */
    public static final SocketOption<Boolean> SO_KEEPALIVE =
        new StdSocketOption<Boolean>("SO_KEEPALIVE", Boolean.class);

    /**
     * 套接字发送缓冲区的大小。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，表示套接字发送缓冲区的大小（以字节为单位）。套接字发送缓冲区是网络实现使用的输出缓冲区。对于高流量连接，可能需要增加其大小。套接字选项的值是实现大小的 <em>提示</em>，实际大小可能有所不同。可以通过查询套接字选项来检索实际大小。
     *
     * <p> 对于面向数据报的套接字，发送缓冲区的大小可能限制了套接字可以发送的数据报的大小。是否发送大于缓冲区大小的数据报或丢弃这些数据报是系统依赖的。
     *
     * <p> 套接字发送缓冲区的初始/默认大小和允许值的范围是系统依赖的，尽管不允许负值。尝试将套接字发送缓冲区设置为大于其最大值时，它将被设置为其最大值。
     *
     * <p> 实现允许在套接字绑定或连接之前设置此套接字选项。实现是否允许在套接字绑定后更改套接字发送缓冲区是系统依赖的。
     *
     * @see Socket#setSendBufferSize
     */
    public static final SocketOption<Integer> SO_SNDBUF =
        new StdSocketOption<Integer>("SO_SNDBUF", Integer.class);

    /**
     * 套接字接收缓冲区的大小。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，表示套接字接收缓冲区的大小（以字节为单位）。套接字接收缓冲区是网络实现使用的输入缓冲区。对于高流量连接，可能需要增加其大小，或者减少以限制可能的未处理数据积压。套接字选项的值是实现大小的 <em>提示</em>，实际大小可能有所不同。
     *
     * <p> 对于面向数据报的套接字，接收缓冲区的大小可能限制了可以接收的数据报的大小。是否可以接收大于缓冲区大小的数据报是系统依赖的。在数据报以比处理速度更快的速率到达的情况下，增加套接字接收缓冲区可能很重要。
     *
     * <p> 在面向流的套接字和 TCP/IP 协议的情况下，套接字接收缓冲区的大小可能用于向远程对等方通告 TCP 接收窗口的大小。
     *
     * <p> 套接字接收缓冲区的初始/默认大小和允许值的范围是系统依赖的，尽管不允许负值。尝试将套接字接收缓冲区设置为大于其最大值时，它将被设置为其最大值。
     *
     * <p> 实现允许在套接字绑定或连接之前设置此套接字选项。实现是否允许在套接字绑定后更改套接字接收缓冲区是系统依赖的。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1323.txt">RFC&nbsp;1323: TCP
     * Extensions for High Performance</a>
     * @see Socket#setReceiveBufferSize
     * @see ServerSocket#setReceiveBufferSize
     */
    public static final SocketOption<Integer> SO_RCVBUF =
        new StdSocketOption<Integer>("SO_RCVBUF", Integer.class);

    /**
     * 重用地址。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。该套接字选项的确切语义是套接字类型和系统依赖的。
     *
     * <p> 在面向流的套接字的情况下，此套接字选项通常决定当涉及该套接字地址的先前连接处于 <em>TIME_WAIT</em> 状态时，套接字是否可以绑定到该套接字地址。在实现语义不同的情况下，如果不需要启用此选项以在先前连接处于该状态时绑定套接字，则实现可以选择忽略此选项。
     *
     * <p> 对于面向数据报的套接字，该套接字选项用于允许多个程序绑定到同一地址。当套接字用于 Internet 协议 (IP) 组播时，应启用此选项。
     *
     * <p> 实现允许在套接字绑定或连接之前设置此套接字选项。更改此套接字选项的值后不会生效。此套接字选项的默认值是系统依赖的。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc793.txt">RFC&nbsp;793: Transmission
     * Control Protocol</a>
     * @see ServerSocket#setReuseAddress
     */
    public static final SocketOption<Boolean> SO_REUSEADDR =
        new StdSocketOption<Boolean>("SO_REUSEADDR", Boolean.class);

    /**
     * 如果存在未发送的数据，则在关闭时等待。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，控制在套接字上有未发送的数据排队且调用关闭套接字的方法时采取的行动。如果套接字选项的值为零或更大，则表示一个超时值（以秒为单位），称为 <em>等待间隔</em>。等待间隔是 {@code close} 方法在操作系统尝试传输未发送的数据或决定无法传输数据时阻塞的超时。如果套接字选项的值小于零，则该选项被禁用。在这种情况下，{@code close} 方法不会等待未发送的数据传输；如果可能，操作系统将在关闭连接之前尝试传输任何未发送的数据。
     *
     * <p> 该套接字选项仅适用于配置为 {@link java.nio.channels.SelectableChannel#isBlocking() 阻塞} 模式的套接字。当此选项在非阻塞套接字上启用时，{@code close} 方法的行为未定义。
     *
     * <p> 该套接字选项的初始值为负值，表示该选项被禁用。可以在任何时候启用该选项，或更改等待间隔。等待间隔的最大值是系统依赖的。将等待间隔设置为大于其最大值时，它将被设置为其最大值。
     *
     * @see Socket#setSoLinger
     */
    public static final SocketOption<Integer> SO_LINGER =
        new StdSocketOption<Integer>("SO_LINGER", Integer.class);

    // -- IPPROTO_IP --

    /**
     * 互联网协议 (IP) 数据包头部的类型服务 (ToS) 字节。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，表示由面向 {@link
     * StandardProtocolFamily#INET IPv4} 套接字发送的数据包的 ToS 字节的值。ToS 字节的解释是网络特定的，不由此类定义。有关 ToS 字节的更多信息，请参见 <a
     * href="http://www.ietf.org/rfc/rfc1349.txt">RFC&nbsp;1349</a> 和 <a
     * href="http://www.ietf.org/rfc/rfc2474.txt">RFC&nbsp;2474</a>。套接字选项的值是一个 <em>提示</em>。实现可能会忽略该值，或忽略特定的值。
     *
     * <p> ToS 字节中 TOS 字段的初始/默认值是实现特定的，但通常为 {@code 0}。对于面向数据报的套接字，可以在套接字绑定后任何时候配置该选项。发送后续数据报时将使用新的字节值。是否可以在绑定套接字之前查询或更改此选项是系统依赖的。
     *
     * <p> 该套接字选项在面向流的套接字或 {@link StandardProtocolFamily#INET6 IPv6} 套接字上的行为在此版本中未定义。
     *
     * @see DatagramSocket#setTrafficClass
     */
    public static final SocketOption<Integer> IP_TOS =
        new StdSocketOption<Integer>("IP_TOS", Integer.class);

    /**
     * 互联网协议 (IP) 组播数据报的网络接口。
     *
     * <p> 该套接字选项的值是一个 {@link NetworkInterface}，表示由面向数据报的套接字发送的组播数据报的出站接口。对于 {@link StandardProtocolFamily#INET6 IPv6}
     * 套接字，设置此选项是否也设置发送到 IPv4 地址的组播数据报的出站接口是系统依赖的。
     *
     * <p> 该套接字选项的初始/默认值可能为 {@code null}，表示出站接口将由操作系统选择，通常基于网络路由表。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改此选项是系统依赖的。
     *
     * @see java.nio.channels.MulticastChannel
     * @see MulticastSocket#setInterface
     */
    public static final SocketOption<NetworkInterface> IP_MULTICAST_IF =
        new StdSocketOption<NetworkInterface>("IP_MULTICAST_IF", NetworkInterface.class);

    /**
     * 互联网协议 (IP) 组播数据报的 <em>生存时间</em>。
     *
     * <p> 该套接字选项的值是一个范围在 {@code 0 <= value <= 255} 的 {@code Integer}。它用于控制由面向数据报的套接字发送的组播数据报的范围。
     * 在 {@link StandardProtocolFamily#INET IPv4} 套接字的情况下，该选项是发送的数据报的生存时间 (TTL)。生存时间为零的数据报不会在网络上传输，但可能会本地交付。在 {@link
     * StandardProtocolFamily#INET6 IPv6} 套接字的情况下，该选项是 <em>跳数限制</em>，即数据报在网络上传输前可以经过的 <em>跳数</em>。对于 IPv6 套接字，设置此选项是否也设置发送到 IPv4 地址的组播数据报的 <em>生存时间</em> 是系统依赖的。
     *
     * <p> 生存时间设置的初始/默认值通常为 {@code 1}。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改此选项是系统依赖的。
     *
     * @see java.nio.channels.MulticastChannel
     * @see MulticastSocket#setTimeToLive
     */
    public static final SocketOption<Integer> IP_MULTICAST_TTL =
        new StdSocketOption<Integer>("IP_MULTICAST_TTL", Integer.class);

    /**
     * 互联网协议 (IP) 组播数据报的 <em>回环</em>。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，控制组播数据报的 <em>回环</em>。套接字选项的值表示该选项是启用还是禁用。
     *
     * <p> 该套接字选项的确切语义是系统依赖的。特别是，回环是否适用于从套接字发送的组播数据报或由套接字接收的组播数据报是系统依赖的。对于 {@link StandardProtocolFamily#INET6 IPv6} 套接字，设置此选项是否也适用于发送到 IPv4 地址的组播数据报是系统依赖的。
     *
     * <p> 该套接字选项的初始/默认值为 {@code TRUE}。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改此选项是系统依赖的。
     *
     * @see java.nio.channels.MulticastChannel
     * @see MulticastSocket#setLoopbackMode
     */
    public static final SocketOption<Boolean> IP_MULTICAST_LOOP =
        new StdSocketOption<Boolean>("IP_MULTICAST_LOOP", Boolean.class);


    // -- IPPROTO_TCP --

    /**
     * 禁用 Nagle 算法。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。该套接字选项特定于使用 TCP/IP 协议的流导向套接字。TCP/IP 使用一种称为 <em>Nagle 算法</em> 的算法来合并短段，提高网络效率。
     *
     * <p> 该套接字选项的默认值为 {@code FALSE}。只有在已知合并会影响性能的情况下，才应启用该套接字选项。该套接字选项可以在任何时候启用。换句话说，可以禁用 Nagle 算法。一旦启用该选项，是否可以随后禁用则取决于系统。如果不能禁用，则调用 {@code setOption} 方法来禁用该选项将不起作用。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC&nbsp;1122:
     * 要求互联网主机 -- 通信层</a>
     * @see Socket#setTcpNoDelay
     */
    public static final SocketOption<Boolean> TCP_NODELAY =
        new StdSocketOption<Boolean>("TCP_NODELAY", Boolean.class);


    private static class StdSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;
        StdSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override public String name() { return name; }
        @Override public Class<T> type() { return type; }
        @Override public String toString() { return name; }
    }
}
