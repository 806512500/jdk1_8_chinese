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
import java.net.ServerSocket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * 用于面向流的监听套接字的可选择通道。
 *
 * <p> 通过调用此类的 {@link #open() open} 方法创建一个服务器套接字通道。不可能为任意预先存在的 {@link ServerSocket} 创建一个通道。新创建的服务器套接字通道是打开的，但尚未绑定。尝试调用未绑定的服务器套接字通道的 {@link #accept() accept} 方法将导致抛出 {@link NotYetBoundException}。可以通过调用此类定义的 {@link #bind(java.net.SocketAddress,int) bind} 方法之一来绑定服务器套接字通道。
 *
 * <p> 使用 {@link #setOption(SocketOption,Object) setOption} 方法配置套接字选项。服务器套接字通道支持以下选项：
 * <blockquote>
 * <table border summary="套接字选项">
 *   <tr>
 *     <th>选项名称</th>
 *     <th>描述</th>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_RCVBUF SO_RCVBUF} </td>
 *     <td> 套接字接收缓冲区的大小 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> 重用地址 </td>
 *   </tr>
 * </table>
 * </blockquote>
 * 可能还支持其他（实现特定的）选项。
 *
 * <p> 服务器套接字通道可以由多个并发线程安全使用。
 * </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class ServerSocketChannel
    extends AbstractSelectableChannel
    implements NetworkChannel
{

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此通道的提供者
     */
    protected ServerSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    /**
     * 打开一个服务器套接字通道。
     *
     * <p> 通过调用系统默认 {@link java.nio.channels.spi.SelectorProvider} 对象的 {@link
     * java.nio.channels.spi.SelectorProvider#openServerSocketChannel
     * openServerSocketChannel} 方法创建新的通道。
     *
     * <p> 新通道的套接字最初未绑定；必须通过其套接字的 {@link
     * java.net.ServerSocket#bind(SocketAddress) bind} 方法之一将其绑定到特定地址，才能接受连接。 </p>
     *
     * @return  一个新的套接字通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static ServerSocketChannel open() throws IOException {
        return SelectorProvider.provider().openServerSocketChannel();
    }

    /**
     * 返回一个操作集，标识此通道支持的操作。
     *
     * <p> 服务器套接字通道仅支持接受新连接，因此此方法返回 {@link SelectionKey#OP_ACCEPT}。
     * </p>
     *
     * @return  有效的操作集
     */
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }


    // -- 服务器套接字特定的操作 --

    /**
     * 将通道的套接字绑定到本地地址并配置套接字以监听连接。
     *
     * <p> 调用此方法等同于以下操作：
     * <blockquote><pre>
     * bind(local, 0);
     * </pre></blockquote>
     *
     * @param   local
     *          要绑定的本地地址，或 {@code null} 以绑定到自动分配的套接字地址
     *
     * @return  此通道
     *
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装了安全经理，并且其 {@link
     *          SecurityManager#checkListen checkListen} 方法拒绝此操作
     *
     * @since 1.7
     */
    public final ServerSocketChannel bind(SocketAddress local)
        throws IOException
    {
        return bind(local, 0);
    }

    /**
     * 将通道的套接字绑定到本地地址并配置套接字以监听连接。
     *
     * <p> 此方法用于在套接字和本地地址之间建立关联。一旦建立关联，套接字将保持绑定状态，直到通道关闭。
     *
     * <p> {@code backlog} 参数是套接字上待处理连接的最大数量。其确切语义是实现特定的。特别是，实现可能会施加最大长度，或者选择忽略此参数。如果 {@code backlog} 参数的值为 {@code 0} 或负值，则使用实现特定的默认值。
     *
     * @param   local
     *          要绑定的地址，或 {@code null} 以绑定到自动分配的套接字地址
     * @param   backlog
     *          待处理连接的最大数量
     *
     * @return  此通道
     *
     * @throws  AlreadyBoundException
     *          如果套接字已绑定
     * @throws  UnsupportedAddressTypeException
     *          如果给定地址的类型不受支持
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     * @throws  SecurityException
     *          如果已安装了安全经理，并且其 {@link
     *          SecurityManager#checkListen checkListen} 方法拒绝此操作
     *
     * @since 1.7
     */
    public abstract ServerSocketChannel bind(SocketAddress local, int backlog)
        throws IOException;

    /**
     * @throws  UnsupportedOperationException           {@inheritDoc}
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     *
     * @since 1.7
     */
    public abstract <T> ServerSocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * 检索与此通道关联的服务器套接字。
     *
     * <p> 返回的对象不会声明任何未在 {@link java.net.ServerSocket} 类中声明的公共方法。 </p>
     *
     * @return  与此通道关联的服务器套接字
     */
    public abstract ServerSocket socket();

    /**
     * 接受与此通道的套接字建立的连接。
     *
     * <p> 如果此通道处于非阻塞模式，则如果没有任何待处理的连接，此方法将立即返回 <tt>null</tt>。否则，它将无限期阻塞，直到有新的连接可用或发生 I/O 错误。
     *
     * <p> 由此方法返回的套接字通道（如果有）将处于阻塞模式，无论此通道的阻塞模式如何。
     *
     * <p> 此方法执行与 {@link java.net.ServerSocket} 类的 {@link
     * java.net.ServerSocket#accept accept} 方法完全相同的安全性检查。也就是说，如果已安装了安全经理，则对于每个新连接，此方法都会验证连接的远程端点的地址和端口号是否被安全经理的 {@link
     * java.lang.SecurityManager#checkAccept checkAccept} 方法允许。 </p>
     *
     * @return  新连接的套接字通道，
     *          或 <tt>null</tt> 如果此通道处于非阻塞模式
     *          并且没有可用的连接可以接受
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在 accept 操作进行中另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在 accept 操作进行中另一个线程中断了当前线程，
     *          从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  NotYetBoundException
     *          如果此通道的套接字尚未绑定
     *
     * @throws  SecurityException
     *          如果已安装了安全经理
     *          并且它不允许访问新连接的远程端点
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract SocketChannel accept() throws IOException;

    /**
     * {@inheritDoc}
     * <p>
     * 如果已设置安全经理，其 {@code checkConnect} 方法将被调用，参数为本地地址和 {@code -1}，以检查操作是否允许。如果操作不允许，
     * 则返回一个表示 {@link java.net.InetAddress#getLoopbackAddress loopback} 地址和通道套接字的本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，或如果被安全经理拒绝，则返回表示 loopback 地址的 {@code SocketAddress}，或如果通道的套接字未绑定，则返回 {@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
