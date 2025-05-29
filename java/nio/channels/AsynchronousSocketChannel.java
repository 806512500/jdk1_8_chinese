
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

package java.nio.channels;

import java.nio.channels.spi.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.io.IOException;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * 用于流导向连接套接字的异步通道。
 *
 * <p> 异步套接字通道可以通过以下两种方式之一创建。通过调用此类定义的 {@code AsynchronousSocketChannel} 的 {@link
 * #open open} 方法创建一个新的异步套接字通道。新创建的通道是打开的，但尚未连接。当与 {@link AsynchronousServerSocketChannel} 的套接字建立连接时，会创建一个已连接的 {@code AsynchronousSocketChannel}。
 * 无法为任意的、预先存在的 {@link java.net.Socket socket} 创建异步套接字通道。
 *
 * <p> 通过调用其 {@link #connect connect} 方法连接新创建的通道；一旦连接，通道将保持连接状态，直到关闭。通过调用其 {@link
 * #getRemoteAddress getRemoteAddress} 方法可以确定套接字通道是否已连接。尝试在未连接的通道上调用 I/O 操作将导致抛出 {@link NotYetConnectedException}。
 *
 * <p> 此类型的通道可以安全地由多个并发线程使用。它们支持并发读写，但任何时候最多只能有一个读操作和一个写操作处于未完成状态。
 * 如果一个线程在前一个读操作完成之前启动读操作，则会抛出 {@link ReadPendingException}。同样，如果在前一个写操作完成之前尝试启动写操作，将抛出 {@link WritePendingException}。
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
 * <p> 由此类定义的 {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read}
 * 和 {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write} 方法允许在启动读或写操作时指定超时。如果在操作完成之前超时，则操作将以 {@link
 * InterruptedByTimeoutException} 异常完成。超时可能会使通道或底层连接处于不一致状态。如果实现不能保证没有从通道读取字节，则它将通道置于实现特定的 <em>错误状态</em>。随后尝试启动 {@code read} 操作将导致抛出未指定的运行时异常。同样，如果 {@code write} 操作超时，且实现不能保证没有向通道写入字节，则进一步尝试向通道 {@code write} 将导致抛出未指定的运行时异常。当超时时，用于 I/O 操作的 {@link ByteBuffer} 或缓冲区序列的状态未定义。应丢弃缓冲区，或者至少应确保在通道保持打开状态时不要访问缓冲区。所有接受超时参数的方法都将小于或等于零的值视为 I/O 操作不会超时。
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
     * openAsynchronousSocketChannel} 方法创建新的通道。如果组参数为 {@code null}，则通过系统范围的默认提供者创建结果通道，并绑定到 <em>默认组</em>。
     *
     * @param   group
     *          新创建的通道应绑定的组，或为 {@code null} 以使用默认组
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
     *          如果此通道上已有一个连接操作正在进行
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装了安全经理，并且其
     *          {@link SecurityManager#checkListen checkListen} 方法拒绝了操作
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
     * <p> 一旦读取连接被关闭，对通道的进一步读取将返回 {@code -1}，表示流的结束。如果连接的输入侧已经关闭，则调用此方法没有效果。
     * 对正在进行的读取操作的影响取决于系统，因此未指定。当套接字接收缓冲区中有未读数据，或后续数据到达时的影响，也取决于系统。
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
     * <p> 一旦写入连接被关闭，对通道的进一步写入将抛出 {@link ClosedChannelException}。如果连接的输出侧已经关闭，则调用此方法没有效果。
     * 对正在进行的写入操作的影响取决于系统，因此未指定。
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
     * <p> 此方法启动一个操作以连接此通道。{@code handler} 参数是一个完成处理器，当连接成功建立或无法建立连接时被调用。
     * 如果无法建立连接，则通道将被关闭。
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
     *          用于处理结果的处理器
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     * @throws  AlreadyConnectedException
     *          如果此通道已连接
     * @throws  ConnectionPendingException
     *          如果此通道上已有一个连接操作正在进行
     * @throws  ShutdownChannelGroupException
     *          如果通道组已终止
     * @throws  SecurityException
     *          如果已安装了安全经理
     *          并且它不允许访问给定的远程端点
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
     * #connect(SocketAddress, Object, CompletionHandler)} 方法完全相同，只是不指定完成处理器，而是返回一个表示待处理结果的 {@code
     * Future}。如果成功完成，{@code Future} 的 {@link
     * Future#get() get} 方法返回 {@code null}。
     *
     * @param   remote
     *          要连接的远程地址
     *
     * @return  一个表示待处理结果的 {@code Future} 对象
     *
     * @throws  UnresolvedAddressException
     *          如果给定的远程地址未完全解析
     * @throws  UnsupportedAddressTypeException
     *          如果给定的远程地址类型不受支持
     * @throws  AlreadyConnectedException
     *          如果此通道已连接
     * @throws  ConnectionPendingException
     *          如果此通道上已有一个连接操作正在进行
     * @throws  SecurityException
     *          如果已安装了安全管理器
     *          并且它不允许访问给定的远程端点
     */
    public abstract Future<Void> connect(SocketAddress remote);

    /**
     * 从此通道读取一系列字节到给定的缓冲区。
     *
     * <p> 此方法启动一个异步读取操作，从此通道读取一系列字节到给定的缓冲区。{@code
     * handler} 参数是一个完成处理器，当读取操作完成（或失败）时被调用。传递给完成处理器的结果是读取的字节数，或者如果通道已到达流末尾而无法读取字节，则返回 {@code -1}。
     *
     * <p> 如果指定了超时时间并且超时时间在操作完成之前已过期，则操作将以 {@link
     * InterruptedByTimeoutException} 异常完成。如果发生超时，并且实现不能保证没有字节已被读取或不会从通道读取到给定的缓冲区，则进一步尝试从通道读取将导致抛出未指定的运行时异常。
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
     *          I/O 操作的最大时间
     * @param   unit
     *          {@code timeout} 参数的时间单位
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果此通道上已有一个读取操作正在进行
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
     * 从此通道读取一系列字节到给定缓冲区的子序列。此操作有时称为 <em>分散读取</em>，在实现将数据分组为一个或多个固定长度的头部和一个可变长度的主体的网络协议时通常很有用。{@code handler} 参数是一个完成处理器，当读取操作完成（或失败）时被调用。传递给完成处理器的结果是读取的字节数，或者如果通道已到达流末尾而无法读取字节，则返回 {@code -1}。
     *
     * <p> 此方法尝试从通道读取最多 <i>r</i> 个字节，其中 <i>r</i> 是给定缓冲区数组中指定子序列的剩余字节总数，即
     *
     * <blockquote><pre>
     * dsts[offset].remaining()
     *     + dsts[offset+1].remaining()
     *     + ... + dsts[offset+length-1].remaining()</pre></blockquote>
     *
     * 在尝试读取时。
     *
     * <p> 假设读取了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前 <tt>dsts[offset].remaining()</tt> 个字节将传输到缓冲区 <tt>dsts[offset]</tt>，接下来的
     * <tt>dsts[offset+1].remaining()</tt> 个字节将传输到缓冲区 <tt>dsts[offset+1]</tt>，依此类推，直到整个字节序列传输到给定的缓冲区。尽可能多的字节将传输到每个缓冲区，因此除了最后一个更新的缓冲区外，每个更新的缓冲区的最终位置保证等于该缓冲区的限制。操作系统可能对 I/O 操作中使用的缓冲区数量施加限制。当缓冲区（有剩余字节的）数量超过此限制时，I/O 操作将使用操作系统允许的最大缓冲区数量执行。
     *
     * <p> 如果指定了超时时间并且超时时间在操作完成之前已过期，则操作将以 {@link
     * InterruptedByTimeoutException} 异常完成。如果发生超时，并且实现不能保证没有字节已被读取或不会从通道读取到给定的缓冲区，则进一步尝试从通道读取将导致抛出未指定的运行时异常。
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
     *          I/O 操作的最大时间
     * @param   unit
     *          {@code timeout} 参数的时间单位
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数的预条件不满足
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果此通道上已有一个读取操作正在进行
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
     * 将给定缓冲区中的字节序列写入此通道。
     *
     * <p> 此方法启动一个异步写操作，从给定缓冲区中写入字节序列到此通道。{@code
     * handler} 参数是一个完成处理器，当写操作完成（或失败）时被调用。传递给完成处理器的结果是已写入的字节数。
     *
     * <p> 如果指定了超时时间且超时时间在操作完成之前已过期，则操作会以 {@link
     * InterruptedByTimeoutException} 异常完成。当发生超时，且实现不能保证没有字节已被写入，或不会从给定缓冲区写入到通道时，进一步尝试写入通道将导致抛出未指定的运行时异常。
     *
     * <p> 否则，此方法的工作方式与 {@link
     * AsynchronousByteChannel#write(ByteBuffer,Object,CompletionHandler)}
     * 方法相同。
     *
     * @param   <A>
     *          附件的类型
     * @param   src
     *          从中获取字节的缓冲区
     * @param   timeout
     *          I/O 操作的最大完成时间
     * @param   unit
     *          {@code timeout} 参数的时间单位
     * @param   attachment
     *          附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  WritePendingException
     *          如果此通道上已经有写操作正在进行
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
     * 从给定缓冲区的子序列中将字节序列写入此通道。此操作有时称为 <em>聚集写</em>，在实现将数据分组为一个或多个固定长度的头部后跟一个可变长度的主体的网络协议时，通常非常有用。{@code handler} 参数是一个完成处理器，当写操作完成（或失败）时被调用。传递给完成处理器的结果是已写入的字节数。
     *
     * <p> 此方法尝试从指定缓冲区数组的子序列中写入最多 <i>r</i> 个字节到此通道，其中 <i>r</i> 是指定缓冲区数组子序列中剩余的总字节数，即
     *
     * <blockquote><pre>
     * srcs[offset].remaining()
     *     + srcs[offset+1].remaining()
     *     + ... + srcs[offset+length-1].remaining()</pre></blockquote>
     *
     * 在尝试写入时的值。
     *
     * <p> 假设写入了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前 <tt>srcs[offset].remaining()</tt> 个字节从缓冲区 <tt>srcs[offset]</tt> 写入，接下来的
     * <tt>srcs[offset+1].remaining()</tt> 个字节从缓冲区 <tt>srcs[offset+1]</tt> 写入，依此类推，直到整个字节序列被写入。尽可能多的字节从每个缓冲区中写入，因此除了最后一个更新的缓冲区外，每个更新的缓冲区的最终位置都保证等于该缓冲区的限制。操作系统可能会对 I/O 操作中可以使用的缓冲区数量施加限制。当缓冲区（有剩余字节）的数量超过此限制时，I/O 操作将使用操作系统允许的最大缓冲区数量执行。
     *
     * <p> 如果指定了超时时间且超时时间在操作完成之前已过期，则操作会以 {@link
     * InterruptedByTimeoutException} 异常完成。当发生超时，且实现不能保证没有字节已被写入，或不会从给定缓冲区写入到通道时，进一步尝试写入通道将导致抛出未指定的运行时异常。
     *
     * @param   <A>
     *          附件的类型
     * @param   srcs
     *          从中获取字节的缓冲区
     * @param   offset
     *          缓冲区数组中第一个缓冲区的偏移量，从中获取字节；必须是非负数且不大于 {@code srcs.length}
     * @param   length
     *          要访问的最大缓冲区数量；必须是非负数且不大于 {@code srcs.length - offset}
     * @param   timeout
     *          I/O 操作的最大完成时间
     * @param   unit
     *          {@code timeout} 参数的时间单位
     * @param   attachment
     *          附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          用于处理结果的处理器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code offset} 和 {@code length} 参数的预条件不满足
     * @throws  WritePendingException
     *          如果此通道上已经有写操作正在进行
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
     * 如果设置了安全管理者，将调用其 {@code checkConnect} 方法，使用本地地址和 {@code -1} 作为参数来检查操作是否被允许。如果操作不被允许，
     * 则返回一个表示 {@link java.net.InetAddress#getLoopbackAddress 回环} 地址和通道套接字本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，或者如果被安全管理者拒绝，则返回表示回环地址的 {@code SocketAddress}，
     *          或者如果通道的套接字未绑定，则返回 {@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    public abstract SocketAddress getLocalAddress() throws IOException;
}
