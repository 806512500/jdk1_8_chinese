/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.lang.annotation.Native;

/**
 * 获取/设置套接字选项的方法接口。此接口由 <B>SocketImpl</B> 和 <B>DatagramSocketImpl</B> 实现。
 * 这些类的子类应重写此接口的方法以支持它们自己的选项。
 * <P>
 * 此接口中指定选项的方法和常量仅用于实现。如果你不继承 SocketImpl 或
 * DatagramSocketImpl，<B>你将不会直接使用这些。</B> 在 Socket、ServerSocket、
 * DatagramSocket 和 MulticastSocket 中有类型安全的方法来获取/设置这些选项。
 * <P>
 * @作者 David Brown
 */


public interface SocketOptions {

    /**
     * 启用/禁用由 <I>optID</I> 指定的选项。如果要启用该选项，并且它需要一个特定于选项的“值”，
     * 则此值通过 <I>value</I> 传递。值的实际类型是特定于选项的，传递非预期类型的值是错误的：
     * <BR><PRE>
     * SocketImpl s;
     * ...
     * s.setOption(SO_LINGER, new Integer(10));
     *    // OK - 设置 SO_LINGER，超时时间为 10 秒。
     * s.setOption(SO_LINGER, new Double(10));
     *    // ERROR - 需要 java.lang.Integer
     *</PRE>
     * 如果请求的选项是二进制的，可以使用 java.lang.Boolean 通过此方法设置：
     * <BR><PRE>
     * s.setOption(TCP_NODELAY, new Boolean(true));
     *    // OK - 启用 TCP_NODELAY，一个二进制选项
     * </PRE>
     * <BR>
     * 任何选项都可以通过使用 Boolean(false) 通过此方法禁用：
     * <BR><PRE>
     * s.setOption(TCP_NODELAY, new Boolean(false));
     *    // OK - 禁用 TCP_NODELAY
     * s.setOption(SO_LINGER, new Boolean(false));
     *    // OK - 禁用 SO_LINGER
     * </PRE>
     * <BR>
     * 对于具有开启和关闭概念且需要非布尔参数的选项，将其值设置为除
     * <I>Boolean(false)</I> 以外的任何值都会隐式启用它。
     * <BR>
     * 如果选项未被识别、套接字已关闭或发生低级错误，则抛出 SocketException
     * <BR>
     * @param optID 识别选项
     * @param value 套接字选项的参数
     * @throws SocketException 如果选项未被识别、套接字已关闭或发生低级错误
     * @see #getOption(int)
     */
    public void
        setOption(int optID, Object value) throws SocketException;

    /**
     * 获取选项的值。
     * 二进制选项如果启用将返回 java.lang.Boolean(true)，如果禁用将返回 java.lang.Boolean(false)，例如：
     * <BR><PRE>
     * SocketImpl s;
     * ...
     * Boolean noDelay = (Boolean)(s.getOption(TCP_NODELAY));
     * if (noDelay.booleanValue()) {
     *     // 如果 TCP_NODELAY 已启用则为真...
     * ...
     * }
     * </PRE>
     * <P>
     * 对于需要特定类型参数的选项，getOption(int) 将返回参数的值，否则
     * 它将返回 java.lang.Boolean(false)：
     * <PRE>
     * Object o = s.getOption(SO_LINGER);
     * if (o instanceof Integer) {
     *     System.out.print("延迟时间为 " + ((Integer)o).intValue());
     * } else {
     *   // o 的真实类型是 java.lang.Boolean(false);
     * }
     * </PRE>
     *
     * @param optID 一个 {@code int} 识别要获取的选项
     * @return 选项的值
     * @throws SocketException 如果套接字已关闭
     * @throws SocketException 如果 <I>optID</I> 在协议栈（包括 SocketImpl）中未知
     * @see #setOption(int, java.lang.Object)
     */
    public Object getOption(int optID) throws SocketException;

    /**
     * Java 支持的 BSD 风格的选项。
     */

    /**
     * 禁用此连接的 Nagle 算法。写入网络的数据不会因先前写入的数据未被确认而被缓冲。
     *<P>
     * 仅适用于 TCP：SocketImpl。
     *
     * @see Socket#setTcpNoDelay
     * @see Socket#getTcpNoDelay
     */

    @Native public final static int TCP_NODELAY = 0x0001;

    /**
     * 获取套接字的本地地址绑定（此选项不能“设置”，只能“获取”，因为套接字在创建时绑定，
     * 因此本地绑定的地址不能更改）。套接字的默认本地地址是 INADDR_ANY，表示多宿主主机上的任何本地地址。
     * 多宿主主机可以使用此选项仅接受其一个地址的连接（在 ServerSocket 或 DatagramSocket 的情况下），
     * 或指定其返回地址给对等方（对于 Socket 或 DatagramSocket）。此选项的参数是一个 InetAddress。
     * <P>
     * 此选项 <B>必须</B> 在构造函数中指定。
     * <P>
     * 适用于：SocketImpl, DatagramSocketImpl
     *
     * @see Socket#getLocalAddress
     * @see DatagramSocket#getLocalAddress
     */

    @Native public final static int SO_BINDADDR = 0x000F;

    /** 为套接字设置 SO_REUSEADDR。此选项仅在 Java 中用于 MulticastSockets，
     * 并且默认为 MulticastSockets 设置。
     * <P>
     * 适用于：DatagramSocketImpl
     */

    @Native public final static int SO_REUSEADDR = 0x04;

    /**
     * 为套接字设置 SO_BROADCAST。此选项启用和禁用进程发送广播消息的能力。它仅支持
     * 数据报套接字，并且仅在网络支持广播消息概念（例如以太网、令牌环等）的情况下支持，
     * 并且默认为 DatagramSockets 设置。
     * @since 1.4
     */

    @Native public final static int SO_BROADCAST = 0x0020;

    /** 设置发送多播数据包的出站接口。
     * 在具有多个网络接口的主机上非常有用，应用程序希望使用系统默认以外的接口。接受/返回一个 InetAddress。
     * <P>
     * 适用于多播：DatagramSocketImpl
     *
     * @see MulticastSocket#setInterface(InetAddress)
     * @see MulticastSocket#getInterface()
     */


                @Native public final static int IP_MULTICAST_IF = 0x10;

    /** 与上述相同。引入此选项是为了保持与 IP_MULTICAST_IF 的行为一致，同时
     *  新选项可以支持设置具有 IPv4 和 IPv6 地址的出站接口。
     *
     *  注意：确保没有与此选项的冲突
     * @see MulticastSocket#setNetworkInterface(NetworkInterface)
     * @see MulticastSocket#getNetworkInterface()
     * @since 1.4
     */
    @Native public final static int IP_MULTICAST_IF2 = 0x1f;

    /**
     * 此选项启用或禁用多播数据报的本地回环。
     * 此选项默认为 Multicast Sockets 启用。
     * @since 1.4
     */

    @Native public final static int IP_MULTICAST_LOOP = 0x12;

    /**
     * 此选项设置 TCP 或 UDP 套接字 IP 标头中的服务类型或流量类别字段。
     * @since 1.4
     */

    @Native public final static int IP_TOS = 0x3;

    /**
     * 指定一个延迟关闭超时。此选项禁用/启用 TCP 套接字的 <B>close()</B> 立即返回。启用
     * 此选项并设置非零整数 <I>超时</I> 意味着 <B>close()</B> 将在所有数据传输并由对等方确认后阻塞，
     * 此时套接字将 <I>优雅地</I> 关闭。达到超时后，套接字将 <I>强制地</I> 关闭，带有 TCP RST。使用零超时启用此选项将立即强制关闭。如果指定的超时值超过 65,535，它将被减少到 65,535。
     * <P>
     * 仅适用于 TCP: SocketImpl
     *
     * @see Socket#setSoLinger
     * @see Socket#getSoLinger
     */
    @Native public final static int SO_LINGER = 0x0080;

    /** 设置阻塞套接字操作的超时：
     * <PRE>
     * ServerSocket.accept();
     * SocketInputStream.read();
     * DatagramSocket.receive();
     * </PRE>
     *
     * <P> 必须在进入阻塞操作之前设置此选项才能生效。如果超时到期且操作将继续阻塞，
     * <B>java.io.InterruptedIOException</B> 将被抛出。在这种情况下，套接字不会关闭。
     *
     * <P> 适用于所有套接字：SocketImpl, DatagramSocketImpl
     *
     * @see Socket#setSoTimeout
     * @see ServerSocket#setSoTimeout
     * @see DatagramSocket#setSoTimeout
     */
    @Native public final static int SO_TIMEOUT = 0x1006;

    /**
     * 设置平台用于传出网络 I/O 的底层缓冲区大小的提示。当用于设置时，这是应用程序给内核的关于要通过套接字发送的数据使用的缓冲区大小的建议。当用于获取时，这必须返回平台在发送数据时实际使用的缓冲区大小。
     *
     * 适用于所有套接字：SocketImpl, DatagramSocketImpl
     *
     * @see Socket#setSendBufferSize
     * @see Socket#getSendBufferSize
     * @see DatagramSocket#setSendBufferSize
     * @see DatagramSocket#getSendBufferSize
     */
    @Native public final static int SO_SNDBUF = 0x1001;

    /**
     * 设置平台用于传入网络 I/O 的底层缓冲区大小的提示。当用于设置时，这是应用程序给内核的关于要通过套接字接收的数据使用的缓冲区大小的建议。当用于获取时，这必须返回平台在接收数据时实际使用的缓冲区大小。
     *
     * 适用于所有套接字：SocketImpl, DatagramSocketImpl
     *
     * @see Socket#setReceiveBufferSize
     * @see Socket#getReceiveBufferSize
     * @see DatagramSocket#setReceiveBufferSize
     * @see DatagramSocket#getReceiveBufferSize
     */
    @Native public final static int SO_RCVBUF = 0x1002;

    /**
     * 当为 TCP 套接字设置了 keepalive 选项，并且在两个方向上都没有数据交换 2 小时（注意：实际值取决于实现）时，
     * TCP 会自动向对等方发送一个 keepalive 探针。此探针是一个对等方必须响应的 TCP 段。
     * 预期的三种响应之一：
     * 1. 对等方以预期的 ACK 响应。应用程序不会收到通知（因为一切正常）。TCP 将在另一个 2 小时的不活动后发送另一个探针。
     * 2. 对等方以 RST 响应，这告诉本地 TCP 对等主机已崩溃并重新启动。套接字将关闭。
     * 3. 没有来自对等方的响应。套接字将关闭。
     *
     * 此选项的目的是检测对等主机是否崩溃。
     *
     * 仅适用于 TCP 套接字：SocketImpl
     *
     * @see Socket#setKeepAlive
     * @see Socket#getKeepAlive
     */
    @Native public final static int SO_KEEPALIVE = 0x0008;

    /**
     * 当设置了 OOBINLINE 选项时，通过套接字接收到的任何 TCP 紧急数据将通过套接字输入流接收。
     * 当禁用此选项（这是默认设置）时，紧急数据将被静默丢弃。
     *
     * @see Socket#setOOBInline
     * @see Socket#getOOBInline
     */
    @Native public final static int SO_OOBINLINE = 0x1003;
}
