
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
 * <p> 通过调用此类的 {@link #open open} 方法之一来创建数据报通道。无法为任意预先存在的数据报套接字创建通道。新创建的数据报通道是打开的，但未连接。数据报通道无需连接即可使用 {@link #send
 * send} 和 {@link #receive receive} 方法。可以通过调用其 {@link #connect connect} 方法来连接数据报通道，以避免每次发送和接收操作时进行的安全检查。为了使用 {@link #read(java.nio.ByteBuffer) read} 和 {@link
 * #write(java.nio.ByteBuffer) write} 方法，因为这些方法不接受或返回套接字地址，数据报通道必须连接。
 *
 * <p> 一旦连接，数据报通道将保持连接状态，直到断开连接或关闭。可以通过调用其 {@link #isConnected isConnected} 方法来确定数据报通道是否已连接。
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
 *     <td> 允许发送广播数据报 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_TOS IP_TOS} </td>
 *     <td> Internet 协议 (IP) 头中的服务类型 (ToS) 字节 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_IF IP_MULTICAST_IF} </td>
 *     <td> 用于 Internet 协议 (IP) 组播数据报的网络接口 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_TTL
 *       IP_MULTICAST_TTL} </td>
 *     <td> Internet 协议 (IP) 组播数据报的 <em>生存时间</em> </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#IP_MULTICAST_LOOP
 *       IP_MULTICAST_LOOP} </td>
 *     <td> Internet 协议 (IP) 组播数据报的回环 </td>
 *   </tr>
 * </table>
 * </blockquote>
 * 可能还支持其他（实现特定的）选项。
 *
 * <p> 数据报通道对多个并发线程是安全的。它们支持并发读取和写入，但任何时候最多只有一个线程可以读取，最多只有一个线程可以写入。 </p>
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
     * <p> 通过调用系统默认 {@link
     * java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel()
     * openDatagramChannel} 方法来创建新通道。通道将不会连接。
     *
     * <p> 通道的套接字的 {@link ProtocolFamily ProtocolFamily} 取决于平台（可能是配置）并且未指定。{@link #open(ProtocolFamily) open} 允许在打开数据报通道时选择协议族，应用于打算进行 Internet 协议组播的数据报通道。
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
     * <p> {@code family} 参数用于指定 {@link
     * ProtocolFamily}。如果数据报通道将用于 IP 组播，则应与该通道将加入的组播组的地址类型相对应。
     *
     * <p> 通过调用系统默认 {@link
     * java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openDatagramChannel(ProtocolFamily)
     * openDatagramChannel} 方法来创建新通道。通道将不会连接。
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

    /**
     * 返回一个操作集，标识此通道支持的操作。
     *
     * <p> 数据报通道支持读取和写入，因此此方法返回 <tt>(</tt>{@link SelectionKey#OP_READ} <tt>|</tt>&nbsp;{@link
     * SelectionKey#OP_WRITE}<tt>)</tt>。 </p>
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
     *          如果已安装了安全经理且其 {@link
     *          SecurityManager#checkListen checkListen} 方法拒绝该操作
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
     * <p> 返回的对象不会声明任何未在 {@link java.net.DatagramSocket} 类中声明的公共方法。 </p>
     *
     * @return  与此通道关联的数据报套接字
     */
    public abstract DatagramSocket socket();

    /**
     * 告诉此通道的套接字是否已连接。
     *
     * @return  如果且仅如果此通道的套接字 {@link #isOpen open} 且已连接，则返回 {@code true}
     */
    public abstract boolean isConnected();

    /**
     * 连接此通道的套接字。
     *
     * <p> 配置通道的套接字，使其仅从给定的远程 <i>对等</i> 地址接收数据报，并仅向该地址发送数据报。一旦连接，数据报将不能从或向任何其他地址接收或发送。数据报套接字将保持连接状态，直到显式断开连接或关闭。
     *
     * <p> 此方法执行与 {@link
     * java.net.DatagramSocket#connect connect} 方法完全相同的安全检查。也就是说，如果已安装了安全经理，则此方法验证其 {@link
     * java.lang.SecurityManager#checkAccept checkAccept} 和 {@link
     * java.lang.SecurityManager#checkConnect checkConnect} 方法是否允许从和向给定的远程地址接收和发送数据报。
     *
     * <p> 此方法可以在任何时候调用。它不会对调用时正在进行的读取或写入操作产生任何影响。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到一个自动分配的地址，就像调用 {@link #bind bind} 方法并传入参数 {@code null} 一样。 </p>
     *
     * @param  remote
     *         要连接到的远程地址
     *
     * @return  此数据报通道
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在连接操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在连接操作进行时中断当前线程，从而关闭通道并设置当前线程的中断状态
     *
     * @throws  SecurityException
     *          如果已安装了安全经理且其不允许访问给定的远程地址
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract DatagramChannel connect(SocketAddress remote)
        throws IOException;

    /**
     * 断开此通道的套接字连接。
     *
     * <p> 配置通道的套接字，使其可以从任何远程地址接收数据报并向其发送数据报，只要安全经理（如果已安装）允许。
     *
     * <p> 此方法可以在任何时候调用。它不会对调用时正在进行的读取或写入操作产生任何影响。
     *
     * <p> 如果此通道的套接字未连接，或者通道已关闭，则调用此方法不会产生任何影响。 </p>
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
     * <p> 如果数据报立即可用，或者此通道处于阻塞模式且最终有一个数据报可用，则将数据报复制到给定的字节缓冲区并返回其源地址。如果此通道处于非阻塞模式且数据报未立即可用，则此方法立即返回
     * <tt>null</tt>。
     *
     * <p> 数据报从给定的字节缓冲区的当前位置开始传输，就像通过常规 {@link
     * ReadableByteChannel#read(java.nio.ByteBuffer) read} 操作一样。如果缓冲区中剩余的字节数少于存储数据报所需的字节数，则数据报的其余部分将被静默丢弃。
     *
     * <p> 此方法执行与 {@link
     * java.net.DatagramSocket#receive receive} 方法完全相同的安全检查。也就是说，如果套接字未连接到特定的远程地址且已安装了安全经理，则对于每个接收到的数据报，此方法验证源的地址和端口号是否被安全经理的 {@link
     * java.lang.SecurityManager#checkAccept checkAccept} 方法允许。通过首先通过 {@link #connect connect} 方法连接套接字可以避免此安全检查的开销。
     *
     * <p> 此方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了读取操作，则此方法的调用将阻塞，直到第一个操作完成。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到一个自动分配的地址，就像调用 {@link #bind bind} 方法并传入参数 {@code null} 一样。 </p>
     *
     * @param  dst
     *         要传输数据报的缓冲区
     *
     * @return  数据报的源地址，
     *          或 <tt>null</tt> 如果此通道处于非阻塞模式且没有数据报立即可用
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在读取操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在读取操作进行时中断当前线程，从而关闭通道并设置当前线程的中断状态
     *
     * @throws  SecurityException
     *          如果已安装了安全经理且其不允许从数据报的发送者接收数据报
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract SocketAddress receive(ByteBuffer dst) throws IOException;


                /**
     * 通过此通道发送数据报。
     *
     * <p> 如果此通道处于非阻塞模式并且底层输出缓冲区有足够的空间，或者此通道处于阻塞模式并且有足够的空间可用，则将给定缓冲区中的剩余字节作为单个数据报发送到给定的目标地址。
     *
     * <p> 数据报从字节缓冲区传输，就像进行常规的 {@link WritableByteChannel#write(java.nio.ByteBuffer) write} 操作一样。
     *
     * <p> 此方法执行与 {@link java.net.DatagramSocket#send send} 方法相同的权限检查。也就是说，如果套接字未连接到特定的远程地址并且已安装了安全经理，则对于每个发送的数据报，此方法会验证安全经理的 {@link java.lang.SecurityManager#checkConnect checkConnect} 方法是否允许目标地址和端口号。通过首先使用 {@link #connect connect} 方法连接套接字，可以避免此安全检查的开销。
     *
     * <p> 该方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了写操作，则此方法的调用将阻塞，直到第一个操作完成。如果此通道的套接字未绑定，则此方法将首先使套接字绑定到自动分配的地址，就像调用带有 {@code null} 参数的 {@link #bind bind} 方法一样。 </p>
     *
     * @param  src
     *         包含要发送的数据报的缓冲区
     *
     * @param  target
     *         要发送数据报的目标地址
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
     *          如果已安装了安全经理，并且它不允许向给定地址发送数据报
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
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它只接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ReadableByteChannel} 接口中指定的行为完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * 从此通道读取数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它只接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ScatteringByteChannel} 接口中指定的行为完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * 从此通道读取数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，且它只接受来自套接字对等方的数据报。如果数据报中的字节数多于给定缓冲区中的剩余空间，则数据报的剩余部分将被静默丢弃。否则，此方法的行为与 {@link ScatteringByteChannel} 接口中指定的行为完全相同。 </p>
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
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接向套接字的对等方发送数据报。否则，它的行为与 {@link WritableByteChannel} 接口中指定的行为完全相同。 </p>
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * 向此通道写入数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接向套接字的对等方发送数据报。否则，它的行为与 {@link GatheringByteChannel} 接口中指定的行为完全相同。 </p>
     *
     * @return   发送的字节数，这将是调用此方法时源缓冲区中剩余的字节数，或者，如果此通道是非阻塞的，则可能为零，如果底层输出缓冲区中没有足够的空间容纳数据报。
     *
     * @throws  NotYetConnectedException
     *          如果此通道的套接字未连接
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * 向此通道写入数据报。
     *
     * <p> 仅当此通道的套接字已连接时，才能调用此方法，此时它直接向套接字的对等方发送数据报。否则，它的行为与 {@link GatheringByteChannel} 接口中指定的行为完全相同。 </p>
     *
     * @return   发送的字节数，这将是调用此方法时源缓冲区中剩余的字节数，或者，如果此通道是非阻塞的，则可能为零，如果底层输出缓冲区中没有足够的空间容纳数据报。
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
     * 如果设置了安全经理，将调用其 {@code checkConnect} 方法，以本地地址和 {@code -1} 作为参数，以检查操作是否允许。如果操作不允许，将返回一个表示 {@link java.net.InetAddress#getLoopbackAddress loopback} 地址和通道套接字本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，或者如果被安全经理拒绝，则返回表示环回地址的 {@code SocketAddress}，或者如果通道的套接字未绑定，则返回 {@code null}。
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
