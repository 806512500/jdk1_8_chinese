
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

package java.nio.channels;

import java.nio.channels.spi.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.io.IOException;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * 异步流导向连接套接字的通道。
 *
 * <p> 异步套接字通道可以通过两种方式之一创建。通过调用此类定义的 {@link
 * #open open} 方法创建一个新的 {@code AsynchronousSocketChannel}。新创建的通道是打开的，但尚未连接。当与 {@link AsynchronousServerSocketChannel}
 * 的套接字建立连接时，会创建一个已连接的 {@code AsynchronousSocketChannel}。不可能为任意的、预先存在的 {@link java.net.Socket 套接字} 创建异步套接字通道。
 *
 * <p> 通过调用其 {@link #connect connect} 方法连接新创建的通道；一旦连接，通道将保持连接状态，直到关闭。通过调用其 {@link
 * #getRemoteAddress getRemoteAddress} 方法可以确定套接字通道是否已连接。尝试在未连接的通道上执行 I/O 操作将导致抛出 {@link NotYetConnectedException}。
 *
 * <p> 此类的通道可以由多个并发线程安全使用。它们支持并发读写，但任何时候最多只能有一个读操作和一个写操作处于挂起状态。
 * 如果一个线程在前一个读操作完成之前尝试启动读操作，则会抛出 {@link ReadPendingException}。同样，如果在前一个写操作完成之前尝试启动写操作，则会抛出 {@link WritePendingException}。
 *
 * <p> 使用 {@link #setOption(SocketOption,Object) setOption} 方法配置套接字选项。异步套接字通道支持以下选项：
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
 *     <td> {@link java.net.StandardSocketOptions#SO_KEEPALIVE SO_KEEPALIVE} </td>
 *     <td> 保持连接活动 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} </td>
 *     <td> 重用地址 </td>
 *   </tr>
 *   <tr>
 *     <td> {@link java.net.StandardSocketOptions#TCP_NODELAY TCP_NODELAY} </td>
 *     <td> 禁用 Nagle 算法 </td>
 *   </tr>
 * </table>
 * </blockquote>
 * 还可能支持其他（实现特定的）选项。
 *
 * <h2>超时</h2>
 *
 * <p> 本类定义的 {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read}
 * 和 {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write} 方法允许在启动读或写操作时指定超时。如果超时在操作完成之前发生，则操作将以 {@link
 * InterruptedByTimeoutException} 异常完成。超时可能会使通道或底层连接处于不一致状态。如果实现不能保证没有从通道读取字节，则它会将通道置于实现特定的 <em>错误状态</em>。随后尝试启动 {@code read} 操作将导致抛出未指定的运行时异常。同样，如果 {@code write} 操作超时且实现不能保证没有将字节写入通道，则进一步尝试向通道 {@code write} 将导致抛出未指定的运行时异常。当超时发生时，用于 I/O 操作的 {@link ByteBuffer} 或缓冲区序列的状态未定义。应丢弃缓冲区，或至少应确保在通道保持打开状态时不要访问缓冲区。所有接受超时参数的方法都将小于或等于零的值视为 I/O 操作不会超时。
 *
 * @since 1.7
 */

public abstract class AsynchronousSocketChannel
    implements AsynchronousByteChannel, NetworkChannel
{
    private final AsynchronousChannelProvider provider;

    /**
     * 初始化此类的新实例。
     *
     * @param  provider
     *         创建此通道的提供者
     */
    protected AsynchronousSocketChannel(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    /**
     * 返回创建此通道的提供者。
     *
     * @return  创建此通道的提供者
     */
    public final AsynchronousChannelProvider provider() {
        return provider;
    }

    /**
     * 打开一个异步套接字通道。
     *
     * <p> 通过调用创建组的 {@link
     * AsynchronousChannelProvider} 的 {@link
     * AsynchronousChannelProvider#openAsynchronousSocketChannel
     * openAsynchronousSocketChannel} 方法来创建新通道。如果组参数为 {@code null}，则通过系统范围的默认提供者创建结果通道，并绑定到 <em>默认组</em>。
     *
     * @param   group
     *          新创建的通道应绑定的组，或 {@code null} 以表示默认组
     *
     * @return  一个新的异步套接字通道
     *
     * @throws  ShutdownChannelGroupException
     *          如果通道组已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static AsynchronousSocketChannel open(AsynchronousChannelGroup group)
        throws IOException
    {
        AsynchronousChannelProvider provider = (group == null) ?
            AsynchronousChannelProvider.provider() : group.provider();
        return provider.openAsynchronousSocketChannel(group);
    }

    /**
     * 打开一个异步套接字通道。
     *
     * <p> 此方法返回一个绑定到 <em>默认组</em> 的异步套接字通道。此方法等同于评估以下表达式：
     * <blockquote><pre>
     * open((AsynchronousChannelGroup)null);
     * </pre></blockquote>
     *
     * @return  一个新的异步套接字通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static AsynchronousSocketChannel open()
        throws IOException
    {
        return open(null);
    }


    // -- 套接字选项及相关 --

    /**
     * @throws  ConnectionPendingException
     *          如果此通道上已经有一个连接操作在进行中
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装了安全经理，且其
     *          {@link SecurityManager#checkListen checkListen} 方法拒绝了该操作
     */
    @Override
    public abstract AsynchronousSocketChannel bind(SocketAddress local)
        throws IOException;

    /**
     * @throws  IllegalArgumentException                {@inheritDoc}
     * @throws  ClosedChannelException                  {@inheritDoc}
     * @throws  IOException                             {@inheritDoc}
     */
    @Override
    public abstract <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value)
        throws IOException;

    /**
     * 关闭通道的读取连接，但不关闭通道。
     *
     * <p> 一旦关闭读取连接，对通道的进一步读取将返回 {@code -1}，表示流的结束。如果连接的输入端已经关闭，则调用此方法没有效果。
     * 对挂起的读操作的影响是系统依赖的，因此未指定。当有未读取的数据在套接字接收缓冲区中，或有数据随后到达时的影响也是系统依赖的。
     *
     * @return  通道
     *
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract AsynchronousSocketChannel shutdownInput() throws IOException;

    /**
     * 关闭通道的写入连接，但不关闭通道。
     *
     * <p> 一旦关闭写入连接，对通道的进一步写入尝试将抛出 {@link ClosedChannelException}。如果连接的输出端已经关闭，则调用此方法没有效果。
     * 对挂起的写操作的影响是系统依赖的，因此未指定。
     *
     * @return  通道
     *
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract AsynchronousSocketChannel shutdownOutput() throws IOException;

    // -- 状态 --

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
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    // -- 异步操作 --

    /**
     * 连接此通道。
     *
     * <p> 此方法启动一个操作以连接此通道。{@code handler} 参数是一个完成处理程序，当连接成功建立或无法建立连接时调用。如果无法建立连接，则关闭通道。
     *
     * <p> 此方法执行与 {@link
     * java.net.Socket} 类完全相同的权限检查。也就是说，如果已安装了安全经理，则此方法会验证其 {@link
     * java.lang.SecurityManager#checkConnect checkConnect} 方法是否允许连接到给定远程端点的地址和端口号。
     *
     * @param   <A>
     *          附件的类型
     * @param   remote
     *          此通道要连接的远程地址
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理程序
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     * @throws  AlreadyConnectedException
     *          如果此通道已连接
     * @throws  ConnectionPendingException
     *          如果此通道上已经有一个连接操作在进行中
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     * @throws  SecurityException
     *          如果已安装了安全经理
     *          且其不允许访问给定的远程端点
     *
     * @see #getRemoteAddress
     */
    public abstract <A> void connect(SocketAddress remote,
                                     A attachment,
                                     CompletionHandler<Void,? super A> handler);

    /**
     * 连接此通道。
     *
     * <p> 此方法启动一个操作以连接此通道。此方法的行为与 {@link
     * #connect(SocketAddress, Object, CompletionHandler)} 方法完全相同，只是不指定完成处理程序，而是返回一个表示挂起结果的 {@code
     * Future}。{@code Future} 的 {@link
     * Future#get() get} 方法在成功完成时返回 {@code null}。
     *
     * @param   remote
     *          此通道要连接的远程地址
     *
     * @return  一个表示挂起结果的 {@code Future} 对象
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     * @throws  AlreadyConnectedException
     *          如果此通道已连接
     * @throws  ConnectionPendingException
     *          如果此通道上已经有一个连接操作在进行中
     * @throws  SecurityException
     *          如果已安装了安全经理
     *          且其不允许访问给定的远程端点
     */
    public abstract Future<Void> connect(SocketAddress remote);

    /**
     * 从此通道读取一系列字节到给定的缓冲区。
     *
     * <p> 此方法启动一个异步读操作，从此通道读取一系列字节到给定的缓冲区。{@code
     * handler} 参数是一个完成处理程序，当读操作完成（或失败）时调用。传递给完成处理程序的结果是读取的字节数，或 {@code -1} 表示由于通道已到达流的结束而无法读取任何字节。
     *
     * <p> 如果指定了超时，且超时在操作完成之前发生，则操作将以 {@link
     * InterruptedByTimeoutException} 异常完成。当发生超时，且实现不能保证没有从通道读取字节，或不会从通道读取字节到给定的缓冲区时，进一步尝试从通道读取将导致抛出未指定的运行时异常。
     *
     * <p> 否则，此方法的工作方式与 {@link
     * AsynchronousByteChannel#read(ByteBuffer,Object,CompletionHandler)}
     * 方法相同。
     *
     * @param   <A>
     *          附件的类型
     * @param   dst
     *          要传输字节的缓冲区
     * @param   timeout
     *          I/O 操作的最大完成时间
     * @param   unit
     *          {@code timeout} 参数的时间单位
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理程序
     *
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果此通道上已经有一个读操作在进行中
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    public abstract <A> void read(ByteBuffer dst,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Integer,? super A> handler);


                /**
     * @throws  IllegalArgumentException        {@inheritDoc}
     * @throws  ReadPendingException            {@inheritDoc}
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    @Override
    public final <A> void read(ByteBuffer dst,
                               A attachment,
                               CompletionHandler<Integer,? super A> handler)
    {
        read(dst, 0L, TimeUnit.MILLISECONDS, attachment, handler);
    }

    /**
     * @throws  IllegalArgumentException        {@inheritDoc}
     * @throws  ReadPendingException            {@inheritDoc}
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    @Override
    public abstract Future<Integer> read(ByteBuffer dst);

    /**
     * 从该通道读取一系列字节到给定缓冲区的子序列中。此操作有时称为<em>分散读取</em>，在实现将数据分组为一个或多个固定长度的标头后跟可变长度主体的网络协议时通常很有用。{@code handler}参数是一个完成处理程序，当读取操作完成（或失败）时被调用。传递给完成处理程序的结果是读取的字节数，或者如果由于通道已到达流末而无法读取字节，则为{@code -1}。
     *
     * <p> 此方法尝试从该通道读取最多<i>r</i>个字节，其中<i>r</i>是给定缓冲区数组中指定子序列中剩余的总字节数，即
     *
     * <blockquote><pre>
     * dsts[offset].remaining()
     *     + dsts[offset+1].remaining()
     *     + ... + dsts[offset+length-1].remaining()</pre></blockquote>
     *
     * 在尝试读取时。
     *
     * <p> 假设读取了一个长度为<i>n</i>的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前<tt>dsts[offset].remaining()</tt>个字节最多会被传输到缓冲区<tt>dsts[offset]</tt>，接下来的<tt>dsts[offset+1].remaining()</tt>个字节最多会被传输到缓冲区<tt>dsts[offset+1]</tt>，依此类推，直到整个字节序列都被传输到给定的缓冲区。尽可能多的字节会被传输到每个缓冲区，因此除了最后一个更新的缓冲区外，每个更新的缓冲区的最终位置都保证等于该缓冲区的限制。底层操作系统可能会对可用于I/O操作的缓冲区数量施加限制。当缓冲区（有剩余字节）的数量超过此限制时，I/O操作将使用操作系统允许的最大缓冲区数量执行。
     *
     * <p> 如果指定了超时时间并且超时时间在操作完成之前已过期，则操作将以{@link
     * InterruptedByTimeoutException}异常完成。当发生超时，且实现无法保证没有字节已被读取，或不会从给定的缓冲区中读取字节时，进一步尝试从通道读取将导致抛出未指定的运行时异常。
     *
     * @param   <A>
     *          附件的类型
     * @param   dsts
     *          要传输字节的缓冲区
     * @param   offset
     *          缓冲区数组中第一个要传输字节的缓冲区的偏移量；必须是非负数且不大于
     *          {@code dsts.length}
     * @param   length
     *          要访问的最大缓冲区数量；必须是非负数且不大于 {@code dsts.length - offset}
     * @param   timeout
     *          I/O操作的最大完成时间
     * @param   unit
     *          {@code timeout}参数的时间单位
     * @param   attachment
     *          要附加到I/O操作的对象；可以为{@code null}
     * @param   handler
     *          用于消费结果的处理程序
     *
     * @throws  IndexOutOfBoundsException
     *          如果{@code offset} 和 {@code length}参数的预条件不满足
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果此通道上已经有读取操作正在进行
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    public abstract <A> void read(ByteBuffer[] dsts,
                                  int offset,
                                  int length,
                                  long timeout,
                                  TimeUnit unit,
                                  A attachment,
                                  CompletionHandler<Long,? super A> handler);

    /**
     * 将一系列字节从给定缓冲区写入此通道。
     *
     * <p> 此方法启动一个异步写入操作，从给定缓冲区写入一系列字节到此通道。{@code
     * handler}参数是一个完成处理程序，当写入操作完成（或失败）时被调用。传递给完成处理程序的结果是写入的字节数。
     *
     * <p> 如果指定了超时时间并且超时时间在操作完成之前已过期，则操作将以{@link
     * InterruptedByTimeoutException}异常完成。当发生超时，且实现无法保证没有字节已被写入，或不会从给定的缓冲区写入字节时，进一步尝试写入通道将导致抛出未指定的运行时异常。
     *
     * <p> 否则，此方法的工作方式与{@link
     * AsynchronousByteChannel#write(ByteBuffer,Object,CompletionHandler)}
     * 方法相同。
     *
     * @param   <A>
     *          附件的类型
     * @param   src
     *          要从中检索字节的缓冲区
     * @param   timeout
     *          I/O操作的最大完成时间
     * @param   unit
     *          {@code timeout}参数的时间单位
     * @param   attachment
     *          要附加到I/O操作的对象；可以为{@code null}
     * @param   handler
     *          用于消费结果的处理程序
     *
     * @throws  WritePendingException
     *          如果此通道上已经有写入操作正在进行
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    public abstract <A> void write(ByteBuffer src,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Integer,? super A> handler);

    /**
     * @throws  WritePendingException          {@inheritDoc}
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    @Override
    public final <A> void write(ByteBuffer src,
                                A attachment,
                                CompletionHandler<Integer,? super A> handler)

    {
        write(src, 0L, TimeUnit.MILLISECONDS, attachment, handler);
    }

    /**
     * @throws  WritePendingException       {@inheritDoc}
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     */
    @Override
    public abstract Future<Integer> write(ByteBuffer src);

    /**
     * 从给定缓冲区的子序列中将一系列字节写入此通道。此操作有时称为<em>聚集写入</em>，在实现将数据分组为一个或多个固定长度的标头后跟可变长度主体的网络协议时通常很有用。{@code handler}参数是一个完成处理程序，当写入操作完成（或失败）时被调用。传递给完成处理程序的结果是写入的字节数。
     *
     * <p> 此方法尝试向此通道写入最多<i>r</i>个字节，其中<i>r</i>是给定缓冲区数组中指定子序列中剩余的总字节数，即
     *
     * <blockquote><pre>
     * srcs[offset].remaining()
     *     + srcs[offset+1].remaining()
     *     + ... + srcs[offset+length-1].remaining()</pre></blockquote>
     *
     * 在尝试写入时。
     *
     * <p> 假设写入了一个长度为<i>n</i>的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前<tt>srcs[offset].remaining()</tt>个字节最多会被写入缓冲区<tt>srcs[offset]</tt>，接下来的<tt>srcs[offset+1].remaining()</tt>个字节最多会被写入缓冲区<tt>srcs[offset+1]</tt>，依此类推，直到整个字节序列都被写入。尽可能多的字节会被写入每个缓冲区，因此除了最后一个更新的缓冲区外，每个更新的缓冲区的最终位置都保证等于该缓冲区的限制。底层操作系统可能会对可用于I/O操作的缓冲区数量施加限制。当缓冲区（有剩余字节）的数量超过此限制时，I/O操作将使用操作系统允许的最大缓冲区数量执行。
     *
     * <p> 如果指定了超时时间并且超时时间在操作完成之前已过期，则操作将以{@link
     * InterruptedByTimeoutException}异常完成。当发生超时，且实现无法保证没有字节已被写入，或不会从给定的缓冲区写入字节时，进一步尝试写入通道将导致抛出未指定的运行时异常。
     *
     * @param   <A>
     *          附件的类型
     * @param   srcs
     *          要从中检索字节的缓冲区
     * @param   offset
     *          缓冲区数组中第一个要检索字节的缓冲区的偏移量；必须是非负数且不大于
     *          {@code srcs.length}
     * @param   length
     *          要访问的最大缓冲区数量；必须是非负数且不大于 {@code srcs.length - offset}
     * @param   timeout
     *          I/O操作的最大完成时间
     * @param   unit
     *          {@code timeout}参数的时间单位
     * @param   attachment
     *          要附加到I/O操作的对象；可以为{@code null}
     * @param   handler
     *          用于消费结果的处理程序
     *
     * @throws  IndexOutOfBoundsException
     *          如果{@code offset} 和 {@code length}参数的预条件不满足
     * @throws  WritePendingException
     *          如果此通道上已经有写入操作正在进行
     * @throws  NotYetConnectedException
     *          如果此通道尚未连接
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     */
    public abstract <A> void write(ByteBuffer[] srcs,
                                   int offset,
                                   int length,
                                   long timeout,
                                   TimeUnit unit,
                                   A attachment,
                                   CompletionHandler<Long,? super A> handler);

    /**
     * {@inheritDoc}
     * <p>
     * 如果设置了安全经理，其{@code checkConnect}方法将被调用，参数为本地地址和{@code -1}，以检查操作是否允许。如果操作不允许，
     * 一个表示
     * {@link java.net.InetAddress#getLoopbackAddress 回环}地址和通道套接字的本地端口的{@code SocketAddress}将被返回。
     *
     * @return  套接字绑定到的{@code SocketAddress}，或者如果被安全经理拒绝，则返回表示回环地址的{@code SocketAddress}，或者如果
     *          通道的套接字未绑定，则返回{@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    public abstract SocketAddress getLocalAddress() throws IOException;
}
