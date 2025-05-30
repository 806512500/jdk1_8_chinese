
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
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * 用于流导向连接套接字的可选择通道。
 *
 * <p> 通过调用此类的 {@link #open open} 方法之一来创建套接字通道。无法为任意预先存在的套接字创建通道。新创建的套接字通道是打开的，但尚未连接。
 * 尝试在未连接的通道上调用 I/O 操作将导致抛出 {@link NotYetConnectedException}。可以通过调用其 {@link #connect connect} 方法来连接套接字通道；
 * 一旦连接，套接字通道将保持连接状态，直到关闭。可以通过调用其 {@link #isConnected isConnected} 方法来确定套接字通道是否已连接。
 *
 * <p> 套接字通道支持 <i>非阻塞连接：</i>&nbsp;可以创建一个套接字通道，并通过 {@link #connect connect} 方法启动与远程套接字的连接，
 * 然后通过 {@link #finishConnect finishConnect} 方法完成连接。可以通过调用 {@link #isConnectionPending isConnectionPending} 方法来确定是否正在进行连接操作。
 *
 * <p> 套接字通道支持 <i>异步关闭，</i> 这类似于 {@link Channel} 类中指定的异步关闭操作。如果一个线程关闭了套接字的输入端，而另一个线程在套接字的通道上阻塞在读操作中，
 * 则阻塞线程中的读操作将完成而不读取任何字节，并返回 <tt>-1</tt>。如果一个线程关闭了套接字的输出端，而另一个线程在套接字的通道上阻塞在写操作中，
 * 则阻塞线程将收到 {@link AsynchronousCloseException}。
 *
 * <p> 套接字选项使用 {@link #setOption(SocketOption,Object) setOption} 方法进行配置。套接字通道支持以下选项：
 * <blockquote>
 * <table border summary="Socket options">
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
 *     <td> {@link java.net.StandardSocketOptions#SO_KEEPALIVE SO_KEEPALIVE} </td>
 *     <td> 保持连接存活 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> 重用地址 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_LINGER SO_LINGER} </td>
 *     <td> 在关闭时如果有数据存在则延迟（仅在配置为阻塞模式时） </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#TCP_NODELAY TCP_NODELAY} </td>
 *     <td> 禁用 Nagle 算法 </td>
 *   </tr>
 * </table>
 * </blockquote>
 * 可能还支持其他（实现特定的）选项。
 *
 * <p> 套接字通道可以由多个并发线程安全使用。它们支持并发读写，但任何时候最多只有一个线程可以读取，最多只有一个线程可以写入。
 * {@link #connect connect} 和 {@link #finishConnect finishConnect} 方法是相互同步的，尝试在这些方法的调用正在进行时启动读或写操作将被阻塞，直到该调用完成。 </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class SocketChannel
    extends AbstractSelectableChannel
    implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel
{

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此通道的提供者
     */
    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    /**
     * 打开一个套接字通道。
     *
     * <p> 通过调用系统范围的默认 {@link java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openSocketChannel openSocketChannel} 方法来创建新的通道。 </p>
     *
     * @return  一个新的套接字通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    /**
     * 打开一个套接字通道并将其连接到远程地址。
     *
     * <p> 此便捷方法的作用类似于调用 {@link #open()} 方法，然后调用 {@link #connect(SocketAddress) connect} 方法，
     * 将其传递给 <tt>remote</tt>，然后返回该通道。 </p>
     *
     * @param  remote
     *         要连接的新通道的远程地址
     *
     * @return  一个新的且已连接的套接字通道
     *
     * @throws  AsynchronousCloseException
     *          如果在连接操作进行中另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在连接操作进行中另一个线程中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     *
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     *
     * @throws  SecurityException
     *          如果已安装了安全管理器并且它不允许访问给定的远程端点
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public static SocketChannel open(SocketAddress remote)
        throws IOException
    {
        SocketChannel sc = open();
        try {
            sc.connect(remote);
        } catch (Throwable x) {
            try {
                sc.close();
            } catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        assert sc.isConnected();
        return sc;
    }

    /**
     * 返回一个操作集，标识此通道支持的操作。
     *
     * <p> 套接字通道支持连接、读取和写入，因此此方法返回 <tt>(</tt>{@link SelectionKey#OP_CONNECT}
     * <tt>|</tt>&nbsp;{@link SelectionKey#OP_READ} <tt>|</tt>&nbsp;{@link
     * SelectionKey#OP_WRITE}<tt>)</tt>。 </p>
     *
     * @return  有效操作集
     */
    public final int validOps() {
        return (SelectionKey.OP_READ
                | SelectionKey.OP_WRITE
                | SelectionKey.OP_CONNECT);
    }


    // -- 套接字特定操作 --

    /**
     * @throws  ConnectionPendingException
     *          如果在此通道上已经有一个非阻塞连接操作正在进行
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装了安全管理器并且其
     *          {@link SecurityManager#checkListen checkListen} 方法拒绝该操作
     *
     * @since 1.7
     */
    @Override
    public abstract SocketChannel bind(SocketAddress local)
        throws IOException;

    /**
     * @throws  UnsupportedOperationException           {@inheritDoc}
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     *
     * @since 1.7
     */
    @Override
    public abstract <T> SocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * 关闭连接的读取端而不关闭通道。
     *
     * <p> 一旦关闭读取端，对通道的进一步读取将返回 {@code -1}，表示流的结束。如果连接的输入端已经关闭，则调用此方法不会产生任何效果。
     *
     * @return  通道
     *
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @since 1.7
     */
    public abstract SocketChannel shutdownInput() throws IOException;

    /**
     * 关闭连接的写入端而不关闭通道。
     *
     * <p> 一旦关闭写入端，对通道的进一步写入将抛出 {@link ClosedChannelException}。如果连接的输出端已经关闭，则调用此方法不会产生任何效果。
     *
     * @return  通道
     *
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     *
     * @since 1.7
     */
    public abstract SocketChannel shutdownOutput() throws IOException;

    /**
     * 获取与此通道关联的套接字。
     *
     * <p> 返回的对象不会声明任何未在 {@link java.net.Socket} 类中声明的公共方法。 </p>
     *
     * @return  与此通道关联的套接字
     */
    public abstract Socket socket();

    /**
     * 告知此通道的网络套接字是否已连接。
     *
     * @return  <tt>true</tt>，如果且仅如果此通道的网络套接字 {@link #isOpen open} 且已连接
     */
    public abstract boolean isConnected();

    /**
     * 告知在此通道上是否正在进行连接操作。
     *
     * @return  <tt>true</tt>，如果且仅如果在此通道上已启动连接操作但尚未通过调用 {@link #finishConnect finishConnect} 方法完成
     */
    public abstract boolean isConnectionPending();

    /**
     * 连接此通道的套接字。
     *
     * <p> 如果此通道处于非阻塞模式，则调用此方法将启动一个非阻塞连接操作。如果连接立即建立，如本地连接的情况，此方法将返回 <tt>true</tt>。
     * 否则，此方法将返回 <tt>false</tt>，并且必须通过调用 {@link #finishConnect finishConnect} 方法来完成连接操作。
     *
     * <p> 如果此通道处于阻塞模式，则调用此方法将阻塞，直到连接建立或发生 I/O 错误。
     *
     * <p> 此方法执行与 {@link java.net.Socket} 类完全相同的安全检查。也就是说，如果已安装了安全管理器，则此方法会验证其 {@link
     * java.lang.SecurityManager#checkConnect checkConnect} 方法是否允许连接到给定远程端点的地址和端口号。
     *
     * <p> 此方法可以在任何时候调用。如果在此方法调用进行中调用了此通道上的读或写操作，则该操作将首先阻塞，直到此调用完成。如果连接尝试启动但失败，
     * 即如果此方法抛出了一个检查异常，则通道将被关闭。 </p>
     *
     * @param  remote
     *         要连接的远程地址
     *
     * @return  <tt>true</tt>，如果已建立连接，<tt>false</tt>，如果此通道处于非阻塞模式且连接操作正在进行
     *
     * @throws  AlreadyConnectedException
     *          如果此通道已连接
     *
     * @throws  ConnectionPendingException
     *          如果在此通道上已经有一个非阻塞连接操作正在进行
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在连接操作进行中另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在连接操作进行中另一个线程中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     *
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     *
     * @throws  SecurityException
     *          如果已安装了安全管理器并且它不允许访问给定的远程端点
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract boolean connect(SocketAddress remote) throws IOException;


                /**
     * 完成连接套接字通道的过程。
     *
     * <p> 通过将套接字通道置于非阻塞模式并调用其 {@link #connect
     * connect} 方法，可以启动非阻塞连接操作。 一旦连接建立，或尝试失败，套接字通道将变得可连接，可以调用此方法来完成连接序列。 如果连接操作失败，则调用此方法将导致抛出适当的
     * {@link java.io.IOException}。
     *
     * <p> 如果此通道已连接，则此方法将不会阻塞，并将立即返回 <tt>true</tt>。 如果此通道处于非阻塞模式，则如果连接过程尚未完成，此方法将返回 <tt>false</tt>。 如果此通道处于阻塞模式，则此方法将阻塞，直到连接完成或失败，并且总是返回 <tt>true</tt> 或抛出描述失败的检查异常。
     *
     * <p> 可以随时调用此方法。 如果在调用此方法时调用此通道的读取或写入操作，则该操作将首先阻塞，直到此调用完成。 如果连接尝试失败，即如果此方法抛出检查异常，则通道将被关闭。 </p>
     *
     * @return  <tt>true</tt> 如果且仅当此通道的套接字已连接
     *
     * @throws  NoConnectionPendingException
     *          如果此通道未连接且未启动连接操作
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
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract boolean finishConnect() throws IOException;

    /**
     * 返回此通道的套接字连接的远程地址。
     *
     * <p> 如果通道绑定并连接到 Internet 协议套接字地址，则此方法的返回值为 {@link
     * java.net.InetSocketAddress} 类型。
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

    // -- ByteChannel 操作 --

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public abstract long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public abstract long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;

    /**
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果设置了安全经理，其 {@code checkConnect} 方法将被调用，参数为本地地址和 {@code -1}，以检查操作是否允许。 如果操作不允许，
     * 返回一个表示 {@link java.net.InetAddress#getLoopbackAddress loopback} 地址和通道套接字的本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，如果被安全经理拒绝，则返回表示 loopback 地址的 {@code SocketAddress}，如果通道的套接字未绑定，则返回 {@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
