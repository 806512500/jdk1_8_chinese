
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.net;

/**
 * 定义 <em>标准</em> 套接字选项。
 *
 * <p> 本类定义的每个套接字选项的 {@link SocketOption#name 名称} 是其字段名称。
 *
 * <p> 在此版本中，此处定义的套接字选项由 {@link
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
     * <p> 此套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。此选项特定于发送到 {@link java.net.Inet4Address IPv4}
     * 广播地址的面向数据报的套接字。当套接字选项启用时，套接字可以用于发送 <em>广播数据报</em>。
     *
     * <p> 此套接字选项的初始值为 {@code FALSE}。此套接字选项可以在任何时候启用或禁用。某些操作系统可能需要 Java 虚拟机以实现特定的权限启动，以启用此选项或发送广播数据报。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc919.txt">RFC&nbsp;929:
     * 广播 Internet 数据报</a>
     * @see DatagramSocket#setBroadcast
     */
    public static final SocketOption<Boolean> SO_BROADCAST =
        new StdSocketOption<Boolean>("SO_BROADCAST", Boolean.class);

    /**
     * 保持连接活动。
     *
     * <p> 此套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。当 {@code SO_KEEPALIVE}
     * 选项启用时，操作系统可能会使用 <em>保持活动</em> 机制定期探测连接的另一端，当连接处于空闲状态时。保持活动机制的确切语义是系统依赖的，因此未指定。
     *
     * <p> 此套接字选项的初始值为 {@code FALSE}。此套接字选项可以在任何时候启用或禁用。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC&nbsp;1122
     * Internet 主机要求 -- 通信层</a>
     * @see Socket#setKeepAlive
     */
    public static final SocketOption<Boolean> SO_KEEPALIVE =
        new StdSocketOption<Boolean>("SO_KEEPALIVE", Boolean.class);

    /**
     * 套接字发送缓冲区的大小。
     *
     * <p> 此套接字选项的值是一个 {@code Integer}，表示套接字发送缓冲区的大小（以字节为单位）。套接字发送缓冲区是网络实现使用的输出缓冲区。对于高流量连接，可能需要增加其大小。套接字选项的值是实现大小的 <em>提示</em>，实际大小可能不同。可以查询套接字选项以检索实际大小。
     *
     * <p> 对于面向数据报的套接字，发送缓冲区的大小可能限制套接字可以发送的数据报的大小。是否发送大于缓冲区大小的数据报是系统依赖的。
     *
     * <p> 套接字发送缓冲区的初始/默认大小和允许值的范围是系统依赖的，尽管不允许负大小。尝试将套接字发送缓冲区设置为大于其最大大小时，它将被设置为其最大大小。
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
     * <p> 此套接字选项的值是一个 {@code Integer}，表示套接字接收缓冲区的大小（以字节为单位）。套接字接收缓冲区是网络实现使用的输入缓冲区。对于高流量连接或限制可能的传入数据积压，可能需要增加其大小。套接字选项的值是实现大小的 <em>提示</em>，实际大小可能不同。
     *
     * <p> 对于面向数据报的套接字，接收缓冲区的大小可能限制可以接收的数据报的大小。是否可以接收大于缓冲区大小的数据报是系统依赖的。在数据报以比处理速度更快的速度到达的情况下，增加套接字接收缓冲区可能很重要。
     *
     * <p> 在面向流的套接字和 TCP/IP 协议的情况下，套接字接收缓冲区的大小可能在向远程对等方通告 TCP 接收窗口的大小时使用。
     *
     * <p> 套接字接收缓冲区的初始/默认大小和允许值的范围是系统依赖的，尽管不允许负大小。尝试将套接字接收缓冲区设置为大于其最大大小时，它将被设置为其最大大小。
     *
     * <p> 实现允许在套接字绑定或连接之前设置此套接字选项。实现是否允许在套接字绑定后更改套接字接收缓冲区是系统依赖的。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1323.txt">RFC&nbsp;1323: TCP
     * 高性能扩展</a>
     * @see Socket#setReceiveBufferSize
     * @see ServerSocket#setReceiveBufferSize
     */
    public static final SocketOption<Integer> SO_RCVBUF =
        new StdSocketOption<Integer>("SO_RCVBUF", Integer.class);


                /**
     * 重用地址。
     *
     * <p> 该套接字选项的值是一个 {@code Boolean}，表示该选项是否启用或禁用。此套接字选项的确切语义取决于套接字类型和系统。
     *
     * <p> 对于流导向的套接字，此套接字选项通常决定当涉及该套接字地址的先前连接处于 <em>TIME_WAIT</em> 状态时，套接字是否可以绑定到套接字地址。在语义不同的实现中，如果不需要启用此套接字选项即可在先前连接处于此状态时绑定套接字，则实现可以选择忽略此选项。
     *
     * <p> 对于数据报导向的套接字，该套接字选项用于允许多个程序绑定到同一地址。当套接字用于 Internet Protocol (IP) 组播时，应启用此选项。
     *
     * <p> 实现允许在套接字绑定或连接之前设置此套接字选项。在套接字绑定后更改此套接字选项的值无效。此套接字选项的默认值取决于系统。
     *
     * @see <a href="http://www.ietf.org/rfc/rfc793.txt">RFC&nbsp;793: 传输控制协议</a>
     * @see ServerSocket#setReuseAddress
     */
    public static final SocketOption<Boolean> SO_REUSEADDR =
        new StdSocketOption<Boolean>("SO_REUSEADDR", Boolean.class);

    /**
     * 如果存在数据，则在关闭时逗留。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，用于控制在套接字上有未发送的数据排队且调用方法关闭套接字时采取的行动。如果套接字选项的值为零或更大，则它表示一个超时值（以秒为单位），称为 <em>逗留间隔</em>。逗留间隔是 {@code close} 方法阻塞的超时时间，操作系统在此期间尝试传输未发送的数据或决定无法传输数据。如果套接字选项的值小于零，则该选项被禁用。在这种情况下，{@code close} 方法不会等待未发送的数据传输；如果可能，操作系统将在关闭连接之前尝试传输任何未发送的数据。
     *
     * <p> 该套接字选项仅用于配置为 {@link java.nio.channels.SelectableChannel#isBlocking() 阻塞} 模式的套接字。当此选项在非阻塞套接字上启用时，{@code close} 方法的行为未定义。
     *
     * <p> 该套接字选项的初始值为负值，表示该选项被禁用。可以在任何时候启用该选项或更改逗留间隔。逗留间隔的最大值取决于系统。将逗留间隔设置为大于其最大值的值会导致逗留间隔被设置为其最大值。
     *
     * @see Socket#setSoLinger
     */
    public static final SocketOption<Integer> SO_LINGER =
        new StdSocketOption<Integer>("SO_LINGER", Integer.class);


    // -- IPPROTO_IP --

    /**
     * Internet Protocol (IP) 头中的服务类型 (ToS) 八位字节。
     *
     * <p> 该套接字选项的值是一个 {@code Integer}，表示由套接字发送到 {@link
     * StandardProtocolFamily#INET IPv4} 套接字的 IP 数据包中的 ToS 八位字节的值。ToS 八位字节的解释是网络特定的，不由此类定义。有关 ToS 八位字节的更多信息，可以在 <a
     * href="http://www.ietf.org/rfc/rfc1349.txt">RFC&nbsp;1349</a> 和 <a
     * href="http://www.ietf.org/rfc/rfc2474.txt">RFC&nbsp;2474</a> 中找到。套接字选项的值是一个 <em>提示</em>。实现可能会忽略该值，或忽略特定值。
     *
     * <p> ToS 八位字节中的 TOS 字段的初始/默认值由实现特定，但通常为 {@code 0}。对于数据报导向的套接字，可以在套接字绑定后任何时候配置该选项。新的八位字节值在发送后续数据报时使用。是否可以在绑定套接字之前查询或更改此选项取决于系统。
     *
     * <p> 在流导向的套接字或 {@link StandardProtocolFamily#INET6 IPv6} 套接字上，此套接字选项的行为在本发行版中未定义。
     *
     * @see DatagramSocket#setTrafficClass
     */
    public static final SocketOption<Integer> IP_TOS =
        new StdSocketOption<Integer>("IP_TOS", Integer.class);

    /**
     * 用于 Internet Protocol (IP) 组播数据报的网络接口。
     *
     * <p> 该套接字选项的值是一个 {@link NetworkInterface}，表示由数据报导向的套接字发送的组播数据报的出站接口。对于 {@link StandardProtocolFamily#INET6 IPv6} 套接字，设置此选项是否也设置发送到 IPv4 地址的组播数据报的出站接口取决于系统。
     *
     * <p> 该套接字选项的初始/默认值可能为 {@code null}，表示出站接口将由操作系统选择，通常基于网络路由表。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改此选项取决于系统。
     *
     * @see java.nio.channels.MulticastChannel
     * @see MulticastSocket#setInterface
     */
    public static final SocketOption<NetworkInterface> IP_MULTICAST_IF =
        new StdSocketOption<NetworkInterface>("IP_MULTICAST_IF", NetworkInterface.class);

    /**
     * 用于 Internet Protocol (IP) 组播数据报的 <em>生存时间</em>。
     *
     * <p> 该套接字选项的值是一个范围在 {@code 0 <= value <= 255} 的 {@code Integer}。它用于控制由数据报导向的套接字发送的组播数据报的范围。
     * 对于 {@link StandardProtocolFamily#INET IPv4} 套接字，该选项是发送的组播数据报的生存时间 (TTL)。TTL 为零的数据报不会在网络上传输，但可能会本地交付。对于 {@link
     * StandardProtocolFamily#INET6 IPv6} 套接字，该选项是 <em>跳数限制</em>，即数据报在网络上传输前可以经过的 <em>跳数</em>。对于 IPv6 套接字，设置此选项是否也设置发送到 IPv4 地址的组播数据报的 <em>生存时间</em> 取决于系统。
     *
     * <p> 生存时间设置的初始/默认值通常为 {@code 1}。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改此选项取决于系统。
     *
     * @see java.nio.channels.MulticastChannel
     * @see MulticastSocket#setTimeToLive
     */
    public static final SocketOption<Integer> IP_MULTICAST_TTL =
        new StdSocketOption<Integer>("IP_MULTICAST_TTL", Integer.class);

                /**
     * 互联网协议 (IP) 组播数据报的回环。
     *
     * <p> 此套接字选项的值是一个 {@code Boolean}，用于控制组播数据报的 <em>回环</em>。套接字选项的值表示该选项是启用还是禁用。
     *
     * <p> 此套接字选项的确切语义是系统依赖的。特别是，回环是否适用于从套接字发送或接收的组播数据报是系统依赖的。
     * 对于 {@link StandardProtocolFamily#INET6 IPv6} 套接字，它是否也适用于发送到 IPv4 地址的组播数据报是系统依赖的。
     *
     * <p> 此套接字选项的初始/默认值为 {@code TRUE}。实现允许在套接字绑定后设置此套接字选项。是否可以在绑定套接字之前查询或更改套接字选项是系统依赖的。
     *
     * @see java.nio.channels.MulticastChannel
     *  @see MulticastSocket#setLoopbackMode
     */
    public static final SocketOption<Boolean> IP_MULTICAST_LOOP =
        new StdSocketOption<Boolean>("IP_MULTICAST_LOOP", Boolean.class);


    // -- IPPROTO_TCP --

    /**
     * 禁用 Nagle 算法。
     *
     * <p> 此套接字选项的值是一个 {@code Boolean}，表示该选项是启用还是禁用。此套接字选项特定于使用 TCP/IP 协议的流导向套接字。TCP/IP 使用一种称为 <em>Nagle 算法</em> 的算法来合并短段并提高网络效率。
     *
     * <p> 此套接字选项的默认值为 {@code FALSE}。只有在已知合并影响性能的情况下，才应启用此套接字选项。此套接字选项可以在任何时候启用。换句话说，可以禁用 Nagle 算法。一旦启用该选项，是否可以随后禁用它是系统依赖的。如果不能，则调用 {@code setOption} 方法来禁用该选项将不会产生任何效果。
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
