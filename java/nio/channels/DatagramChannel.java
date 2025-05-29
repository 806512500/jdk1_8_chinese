/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.channels;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.net.DatagramSocket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * 用于数据报套接字的可选择通道。
 *
 * <p> 通过调用此类的 {@link #open open} 方法之一创建数据报通道。不可能为任意预先存在的数据报套接字创建通道。新创建的数据报通道是打开的，但未连接。数据报通道无需连接即可使用 {@link #send
 * send} 和 {@link #receive receive} 方法。通过调用其 {@link #connect connect} 方法，可以连接数据报通道，以避免每次发送和接收操作时执行的安全检查开销。为了使用 {@link #read(java.nio.ByteBuffer) read} 和 {@link
 * #write(java.nio.ByteBuffer) write} 方法，数据报通道必须连接，因为这些方法不接受或返回套接字地址。
 *
 * <p> 一旦连接，数据报通道将保持连接状态，直到断开连接或关闭。是否连接数据报通道可以通过调用其 {@link #isConnected isConnected} 方法来确定。
 *
 * <p> 使用 {@link #setOption(SocketOption,Object)
 * setOption} 方法配置套接字选项。到 Internet 协议套接字的数据报通道支持以下选项：
 * <blockquote>
 * <table border summary="套接字选项">
 *   <tr>
 *     <th>选项名称</th>
 *     <th>描述</th>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_SNDBUF SO_SNDBUF} </td>
 *     <td> 套接字发送缓冲区的大小 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_RCVBUF SO_RCVBUF} </td>
 *     <td> 套接字接收缓冲区的大小 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> 重用地址 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_BROADCAST SO_BROADCAST} </td>
 *     <td> 允许传输广播数据报 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_TOS IP_TOS} </td>
 *     <td> Internet 协议 (IP) 头中的服务类型 (ToS) 字节 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_IF IP_MULTICAST_IF} </td>
 *     <td> Internet 协议 (IP) 多播数据报的网络接口 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_TTL
 *       IP_MULTICAST_TTL} </td>
 *     <td> Internet 协议 (IP) 多播数据报的 <em>生存时间</em> </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_LOOP
 *       IP_MULTICAST_LOOP} </td>
 *     <td> Internet 协议 (IP) 多播数据报的回环 </td>
 *   </tr>
 * </table>
 * </blockquote>
 * 还可能支持其他（实现特定的）选项。
 *
 * <p> 数据报通道对多个并发线程是安全的。它们支持并发读写，但任何时候最多只能有一个线程在读取，最多只能有一个线程在写入。 </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class DatagramChannel
    extends AbstractSelectableChannel
    implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, MulticastChannel
{

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此通道的提供者
     */
    protected DatagramChannel(SelectorProvider provider) {
        super(provider);
    }

    /**
     * 打开数据报通道。
     *
     * <p> 通过调用系统范围默认的 {@link
     * java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel()
     * openDatagramChannel} 方法创建新的通道。该通道将不会连接。
     *
     * <p> 通道套接字的 {@link ProtocolFamily ProtocolFamily} 是平台（可能还有配置）依赖的，因此未指定。{@link #open(ProtocolFamily) open} 允许在打开数据报通道时选择协议族，应用于打开用于 Internet 协议多播的数据报通道。
     *
     * @return  新的数据报通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }

    /**
     * 打开数据报通道。
     *
     * <p> 使用 {@code family} 参数指定 {@link
     * ProtocolFamily}。如果数据报通道用于 IP 多播，则应对应于此通道将加入的多播组的地址类型。
     *
     * <p> 通过调用系统范围默认的 {@link
     * java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel(ProtocolFamily)
     * openDatagramChannel} 方法创建新的通道。该通道将不会连接。
     *
     * @param   family
     *          协议族
     *
     * @return  新的数据报通道
     *
     * @throws  UnsupportedOperationException
     *          如果指定的协议族不受支持。例如，如果参数指定为 {@link
     *          java.net.StandardProtocolFamily#INET6 StandardProtocolFamily.INET6}
     *          但平台未启用 IPv6。
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since   1.7
     */
    public static DatagramChannel open(ProtocolFamily family) throws IOException {
        return SelectorProvider.provider().openDatagramChannel(family);
    }

}


                /**
     * 返回一个标识此通道支持的操作的操作集。
     *
     * <p> 数据报通道支持读取和写入，因此此方法返回 <tt>(</tt>{@link SelectionKey#OP_READ} <tt>|</tt>&nbsp;{@link
     * SelectionKey#OP_WRITE}<tt>)</tt>.  </p>
     *
     * @return  有效操作集
     */
    public final int validOps() {
        return (SelectionKey.OP_READ
                | SelectionKey.OP_WRITE);
    }


    // -- 套接字特定操作 --

    /**
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装了安全管理者并且其 {@link
     *          SecurityManager#checkListen checkListen} 方法拒绝此操作
     *
     * @since 1.7
     */
    public abstract DatagramChannel bind(SocketAddress local)
        throws IOException;

    /**
     * @throws  UnsupportedOperationException           {@inheritDoc}
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     *
     * @since 1.7
     */
    public abstract <T> DatagramChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * 获取与此通道关联的数据报套接字。
     *
     * <p> 返回的对象不会声明任何未在 {@link java.net.DatagramSocket} 类中声明的公共方法。  </p>
     *
     * @return  与此通道关联的数据报套接字
     */
    public abstract DatagramSocket socket();

    /**
     * 告知此通道的套接字是否已连接。
     *
     * @return  如果且仅当此通道的套接字 {@link #isOpen 已打开} 并已连接时返回 {@code true}
     */
    public abstract boolean isConnected();

    /**
     * 连接此通道的套接字。
     *
     * <p> 配置通道的套接字，使其仅从给定的远程 <i>对等方</i> 地址接收数据报，并向其发送数据报。一旦连接，数据报将不能从或发送到任何其他地址。数据报套接字保持连接状态，直到它被显式断开连接或关闭。
     *
     * <p> 此方法执行与 {@link java.net.DatagramSocket#connect connect} 方法完全相同的权限检查。也就是说，如果已安装了安全管理者，那么此方法会验证其 {@link
     * java.lang.SecurityManager#checkAccept checkAccept} 和 {@link
     * java.lang.SecurityManager#checkConnect checkConnect} 方法是否允许从给定的远程地址接收和发送数据报。
     *
     * <p> 此方法可以在任何时候调用。在调用此方法时，它不会对正在进行的读取或写入操作产生任何影响。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到一个自动分配的地址，就像调用 {@link #bind bind} 方法并传入参数 {@code null} 一样。 </p>
     *
     * @param  remote
     *         此通道要连接的远程地址
     *
     * @return  此数据报通道
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在连接操作进行时另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在连接操作进行时另一个线程中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  SecurityException
     *          如果已安装了安全管理者并且它不允许访问给定的远程地址
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract DatagramChannel connect(SocketAddress remote)
        throws IOException;

    /**
     * 断开此通道的套接字连接。
     *
     * <p> 配置通道的套接字，使其可以从任何远程地址接收数据报，并向其发送数据报，只要安全管理者（如果已安装）允许。
     *
     * <p> 此方法可以在任何时候调用。在调用此方法时，它不会对正在进行的读取或写入操作产生任何影响。
     *
     * <p> 如果此通道的套接字未连接，或者通道已关闭，则调用此方法不会产生任何效果。 </p>
     *
     * @return  此数据报通道
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract DatagramChannel disconnect() throws IOException;

    /**
     * 返回此通道的套接字连接的远程地址。
     *
     * @return  远程地址；如果通道的套接字未连接，则返回 {@code null}
     *
     * @throws  ClosedChannelException
     *          如果通道已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since 1.7
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    /**
     * 通过此通道接收数据报。
     *
     * <p> 如果数据报立即可用，或者此通道处于阻塞模式并且最终变得可用，则数据报将被复制到给定的字节缓冲区，并返回其源地址。如果此通道处于非阻塞模式并且数据报未立即可用，则此方法立即返回 <tt>null</tt>。
     *
     * <p> 数据报从给定的字节缓冲区的当前位置开始传输，就像通过常规的 {@link
     * ReadableByteChannel#read(java.nio.ByteBuffer) read} 操作一样。如果缓冲区中剩余的字节数少于存储数据报所需的字节数，则数据报的其余部分将被静默丢弃。
     *
     * <p> 此方法执行与 {@link java.net.DatagramSocket#receive receive} 方法完全相同的权限检查。也就是说，如果套接字未连接到特定的远程地址并且已安装了安全管理者，则对于每个接收到的数据报，此方法会验证源的地址和端口号是否被安全管理者的 {@link
     * java.lang.SecurityManager#checkAccept checkAccept} 方法允许。通过首先通过 {@link #connect connect} 方法连接套接字可以避免此权限检查的开销。
     *
     * <p> 此方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了读取操作，则此方法的调用将阻塞，直到第一个操作完成。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到一个自动分配的地址，就像调用 {@link #bind bind} 方法并传入参数 {@code null} 一样。 </p>
     *
     * @param  dst
     *         数据报要传输到的缓冲区
     *
     * @return  数据报的源地址，
     *          或者如果此通道处于非阻塞模式并且没有数据报立即可用，则返回 <tt>null</tt>
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在读取操作进行时另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在读取操作进行时另一个线程中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  SecurityException
     *          如果已安装了安全管理者并且它不允许从数据报的发送者接收数据报
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract SocketAddress receive(ByteBuffer dst) throws IOException;


                /**
     * 通过此通道发送数据报。
     *
     * <p> 如果此通道处于非阻塞模式并且底层输出缓冲区有足够的空间，或者此通道处于阻塞模式并且有足够的空间可用，则将给定缓冲区中的剩余字节作为单个数据报传输到给定的目标地址。
     *
     * <p> 数据报从字节缓冲区传输的方式与常规的 {@link WritableByteChannel#write(java.nio.ByteBuffer) write} 操作相同。
     *
     * <p> 此方法执行与 {@link java.net.DatagramSocket#send send} 方法相同的权限检查。也就是说，如果套接字未连接到特定的远程地址并且已安装了安全管理者，则对于每个发送的数据报，此方法将验证目标地址和端口号是否被安全管理者的方法 {@link java.lang.SecurityManager#checkConnect checkConnect} 允许。通过首先使用 {@link #connect connect} 方法连接套接字可以避免此安全检查的开销。
     *
     * <p> 该方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了写操作，则此方法的调用将阻塞，直到第一个操作完成。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到一个自动分配的地址，就像调用带有参数为 {@code null} 的 {@link #bind bind} 方法一样。</p>
     *
     * @param  src
     *         包含要发送的数据报的缓冲区
     *
     * @param  target
     *         数据报要发送到的地址
     *
     * @return   发送的字节数，这将是调用此方法时源缓冲区中剩余的字节数，或者，如果此通道是非阻塞的，则可能为零，如果底层输出缓冲区中没有足够的空间容纳数据报。
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在读操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在读操作进行时中断当前线程，从而关闭通道并设置当前线程的中断状态
     *
     * @throws  SecurityException
     *          如果已安装了安全管理者，并且它不允许向给定地址发送数据报
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract int send(ByteBuffer src, SocketAddress target)
        throws IOException;


    // -- ByteChannel 操作 --

    /**
     * 从此通道读取数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它仅接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ReadableByteChannel} 接口中的规定完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * 从此通道读取数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它仅接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ScatteringByteChannel} 接口中的规定完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * 从此通道读取数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它仅接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ScatteringByteChannel} 接口中的规定完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * 向此通道写入数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接向套接字的对等方发送数据报。否则，其行为与 {@link WritableByteChannel} 接口中的规定完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * 向此通道写入数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接向套接字的对等方发送数据报。否则，其行为与 {@link GatheringByteChannel} 接口中的规定完全相同。 </p>
     *
     * @return   发送的字节数，这将是调用此方法时源缓冲区中剩余的字节数，或者，如果此通道是非阻塞的，则可能为零，如果底层输出缓冲区中没有足够的空间容纳数据报。
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

                /**
     * 将数据报写入此通道。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接将数据报发送到套接字的对等方。否则，其行为完全如 {@link
     * GatheringByteChannel} 接口所指定。 </p>
     *
     * @return   发送的字节数，这将是调用此方法时源缓冲区中剩余的字节数，或者，如果此通道是非阻塞的，如果底层输出缓冲区中没有足够的空间容纳数据报，则可能为零
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果设置了安全管理器，将调用其 {@code checkConnect} 方法，使用本地地址和 {@code -1} 作为参数，以检查操作是否被允许。如果操作不被允许，
     * 则返回一个表示 {@link java.net.InetAddress#getLoopbackAddress 循环回送} 地址和通道套接字的本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，或者如果被安全管理器拒绝，则返回表示循环回送地址的 {@code SocketAddress}，
     *          或者如果通道的套接字未绑定，则返回 {@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
