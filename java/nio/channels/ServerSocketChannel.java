
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
import java.net.ServerSocket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * 用于面向流的监听套接字的可选择通道。
 *
 * <p> 通过调用此类的 {@link #open() open} 方法创建服务器套接字通道。不可能为任意预先存在的 {@link ServerSocket} 创建通道。新创建的服务器套接字通道是打开的，但尚未绑定。尝试调用未绑定的服务器套接字通道的 {@link #accept() accept} 方法将导致抛出 {@link NotYetBoundException}。通过调用此类定义的 {@link #bind(java.net.SocketAddress,int) bind} 方法之一可以绑定服务器套接字通道。
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
 * 还可能支持其他（实现特定的）选项。
 *
 * <p> 服务器套接字通道可以安全地被多个并发线程使用。
 * </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
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
     * <p> 通过调用系统范围的默认 {@link java.nio.channels.spi.SelectorProvider} 对象的 {@link
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
     *          要绑定套接字的本地地址，或 {@code null} 以绑定到自动分配的套接字地址
     *
     * @return  此通道
     *
     * @throws  AlreadyBoundException               {@inheritDoc}
     * @throws  UnsupportedAddressTypeException     {@inheritDoc}
     * @throws  ClosedChannelException              {@inheritDoc}
     * @throws  IOException                         {@inheritDoc}
     * @throws  SecurityException
     *          如果已安装安全经理，且其 {@link
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
     * <p> 此方法用于在套接字和本地地址之间建立关联。一旦建立了关联，套接字将保持绑定状态，直到通道关闭。
     *
     * <p> {@code backlog} 参数是套接字上的最大待处理连接数。其确切语义是实现特定的。特别是，实现可能施加最大长度，或可能选择完全忽略该参数。如果 {@code backlog} 参数的值为 {@code 0} 或负值，则使用实现特定的默认值。
     *
     * @param   local
     *          要绑定套接字的地址，或 {@code null} 以绑定到自动分配的套接字地址
     * @param   backlog
     *          最大待处理连接数
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
     *          如果已安装安全经理，且其 {@link
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
     * 获取与此通道关联的服务器套接字。
     *
     * <p> 返回的对象将不会声明任何未在 {@link java.net.ServerSocket} 类中声明的公共方法。 </p>
     *
     * @return  与此通道关联的服务器套接字
     */
    public abstract ServerSocket socket();

    /**
     * 接受与此通道的套接字建立的连接。
     *
     * <p> 如果此通道处于非阻塞模式，则如果没有任何待处理的连接，此方法将立即返回 <tt>null</tt>。
     * 否则，它将无限期地阻塞，直到有新的连接可用或发生 I/O 错误。
     *
     * <p> 由该方法返回的套接字通道（如果有）将处于阻塞模式，无论此通道的阻塞模式如何。
     *
     * <p> 此方法执行与 {@link java.net.ServerSocket#accept accept} 方法完全相同的安全部检查。
     * 也就是说，如果安装了安全管理者，则对于每个新连接，此方法将验证连接的远程端点的地址和端口号是否被安全管理者
     * 的 {@link java.lang.SecurityManager#checkAccept checkAccept} 方法允许。 </p>
     *
     * @return  新连接的套接字通道，
     *          或者如果此通道处于非阻塞模式且没有可用的连接，则返回 <tt>null</tt>
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在 accept 操作进行过程中另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在 accept 操作进行过程中另一个线程中断了当前线程，
     *          从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  NotYetBoundException
     *          如果此通道的套接字尚未绑定
     *
     * @throws  SecurityException
     *          如果安装了安全管理者
     *          并且它不允许访问新连接的远程端点
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public abstract SocketChannel accept() throws IOException;

    /**
     * {@inheritDoc}
     * <p>
     * 如果设置了安全管理者，其 {@code checkConnect} 方法将被调用，参数为本地地址和 {@code -1}，以检查操作是否允许。
     * 如果操作不允许，将返回一个表示 {@link java.net.InetAddress#getLoopbackAddress loopback} 地址和
     * 通道套接字的本地端口的 {@code SocketAddress}。
     *
     * @return  套接字绑定到的 {@code SocketAddress}，或者如果被安全管理者拒绝，则返回表示 loopback 地址的
     *          {@code SocketAddress}，或者如果通道的套接字未绑定，则返回 {@code null}
     *
     * @throws  ClosedChannelException     {@inheritDoc}
     * @throws  IOException                {@inheritDoc}
     */
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
