
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import sun.security.util.SecurityConstants;

/**
 * 该类实现服务器套接字。服务器套接字等待网络上的请求。它根据该请求执行某些操作，然后可能将结果返回给请求者。
 * <p>
 * 服务器套接字的实际工作由 {@code SocketImpl} 类的实例完成。应用程序可以更改创建套接字实现的套接字工厂，
 * 以配置自己创建适合本地防火墙的套接字。
 *
 * @author 未署名
 * @see     java.net.SocketImpl
 * @see     java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
 * @see     java.nio.channels.ServerSocketChannel
 * @since   JDK1.0
 */
public
class ServerSocket implements java.io.Closeable {
    /**
     * 该套接字的各种状态。
     */
    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private Object closeLock = new Object();

    /**
     * 该套接字的实现。
     */
    private SocketImpl impl;

    /**
     * 是否使用较旧的 SocketImpl？
     */
    private boolean oldImpl = false;

    /**
     * 包私有构造函数，用于创建与给定 SocketImpl 关联的 ServerSocket。
     *
     * @throws     SecurityException 如果设置了安全管理器，并且其 {@code checkPermission} 方法不允许
     *             {@code NetPermission("setSocketImpl")}。
     */
    ServerSocket(SocketImpl impl) {
        checkPermission();
        this.impl = impl;
        impl.setServerSocket(this);
    }

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_SOCKETIMPL_PERMISSION);
        }
        return null;
    }

    /**
     * 创建一个未绑定的服务器套接字。
     *
     * @exception IOException 当打开套接字时发生 I/O 错误。
     * @revised 1.4
     */
    public ServerSocket() throws IOException {
        setImpl();
    }

    /**
     * 创建一个绑定到指定端口的服务器套接字。端口号为 {@code 0} 表示端口号自动分配，通常从一个临时端口范围中分配。
     * 可以通过调用 {@link #getLocalPort getLocalPort} 获取此端口号。
     * <p>
     * 进入连接请求的最大队列长度设置为 {@code 50}。如果连接请求到达时队列已满，连接将被拒绝。
     * <p>
     * 如果应用程序指定了服务器套接字工厂，则调用该工厂的 {@code createSocketImpl} 方法来创建实际的套接字实现。
     * 否则创建一个“普通”套接字。
     * <p>
     * 如果存在安全管理器，将调用其 {@code checkListen} 方法，以确保操作被允许。
     * 这可能会导致 SecurityException。
     *
     * @param      port  端口号，或 {@code 0} 表示使用自动分配的端口号。
     *
     * @exception  IOException  当打开套接字时发生 I/O 错误。
     * @exception  SecurityException
     * 如果存在安全管理器，且其 {@code checkListen} 方法不允许操作。
     * @exception  IllegalArgumentException 如果端口参数超出有效端口值的指定范围，即 0 到 65535（包括）。
     *
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port) throws IOException {
        this(port, 50, null);
    }

    /**
     * 创建一个服务器套接字并将其绑定到指定的本地端口号，具有指定的积压。
     * 端口号为 {@code 0} 表示端口号自动分配，通常从一个临时端口范围中分配。
     * 可以通过调用 {@link #getLocalPort getLocalPort} 获取此端口号。
     * <p>
     * 进入连接请求的最大队列长度设置为 {@code backlog} 参数。如果连接请求到达时队列已满，连接将被拒绝。
     * <p>
     * 如果应用程序指定了服务器套接字工厂，则调用该工厂的 {@code createSocketImpl} 方法来创建实际的套接字实现。
     * 否则创建一个“普通”套接字。
     * <p>
     * 如果存在安全管理器，将调用其 {@code checkListen} 方法，以确保操作被允许。
     * 这可能会导致 SecurityException。
     *
     * {@code backlog} 参数是套接字上待处理连接的最大数量。其确切语义是实现特定的。
     * 特别是，实现可能会施加最大长度，或者选择忽略该参数。提供的值应大于 {@code 0}。
     * 如果它小于或等于 {@code 0}，则使用实现特定的默认值。
     * <P>
     *
     * @param      port     端口号，或 {@code 0} 表示使用自动分配的端口号。
     * @param      backlog  请求的待处理连接队列的最大长度。
     *
     * @exception  IOException  当打开套接字时发生 I/O 错误。
     * @exception  SecurityException
     * 如果存在安全管理器，且其 {@code checkListen} 方法不允许操作。
     * @exception  IllegalArgumentException 如果端口参数超出有效端口值的指定范围，即 0 到 65535（包括）。
     *
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, null);
    }

    /**
     * 创建一个具有指定端口、监听积压和本地 IP 地址的服务器。如果 <i>bindAddr</i> 为 null，则默认接受
     * 所有本地地址的连接。端口号必须在 0 到 65535（包括）之间。
     * 端口号为 {@code 0} 表示端口号自动分配，通常从一个临时端口范围中分配。
     * 可以通过调用 {@link #getLocalPort getLocalPort} 获取此端口号。
     *
     * <P>如果存在安全管理器，此方法将调用其 {@code checkListen} 方法，
     * 以确保操作被允许。这可能会导致 SecurityException。
     *
     * {@code backlog} 参数是套接字上待处理连接的最大数量。其确切语义是实现特定的。
     * 特别是，实现可能会施加最大长度，或者选择忽略该参数。提供的值应大于 {@code 0}。
     * 如果它小于或等于 {@code 0}，则使用实现特定的默认值。
     * <P>
     * @param port  端口号，或 {@code 0} 表示使用自动分配的端口号。
     * @param backlog 请求的待处理连接队列的最大长度。
     * @param bindAddr 服务器将绑定到的本地 InetAddress。
     *
     * @throws  SecurityException 如果存在安全管理器，且其 {@code checkListen} 方法不允许操作。
     *
     * @throws  IOException 当打开套接字时发生 I/O 错误。
     * @exception  IllegalArgumentException 如果端口参数超出有效端口值的指定范围，即 0 到 65535（包括）。
     *
     * @see SocketOptions
     * @see SocketImpl
     * @see SecurityManager#checkListen
     * @since   JDK1.1
     */
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        setImpl();
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException(
                       "Port value out of range: " + port);
        if (backlog < 1)
          backlog = 50;
        try {
            bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch(SecurityException e) {
            close();
            throw e;
        } catch(IOException e) {
            close();
            throw e;
        }
    }

    /**
     * 获取附加到该套接字的 {@code SocketImpl}，必要时创建它。
     *
     * @return  附加到该 ServerSocket 的 {@code SocketImpl}。
     * @throws SocketException 如果创建失败。
     * @since 1.4
     */
    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl();
        return impl;
    }

    private void checkOldImpl() {
        if (impl == null)
            return;
        // SocketImpl.connect() 是一个受保护的方法，因此我们需要使用 getDeclaredMethod，因此我们需要访问成员的权限
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Void>() {
                    public Void run() throws NoSuchMethodException {
                        impl.getClass().getDeclaredMethod("connect",
                                                          SocketAddress.class,
                                                          int.class);
                        return null;
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            oldImpl = true;
        }
    }

    private void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            // 无需在这里执行 checkOldImpl()，我们知道它是一个最新的 SocketImpl！
            impl = new SocksSocketImpl();
        }
        if (impl != null)
            impl.setServerSocket(this);
    }

    /**
     * 创建套接字实现。
     *
     * @throws IOException 如果创建失败
     * @since 1.4
     */
    void createImpl() throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(true);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    /**
     *
     * 将 {@code ServerSocket} 绑定到特定地址（IP 地址和端口号）。
     * <p>
     * 如果地址为 {@code null}，则系统将选择一个临时端口和一个有效的本地地址来绑定套接字。
     * <p>
     * @param   endpoint        要绑定到的 IP 地址和端口号。
     * @throws  IOException 如果绑定操作失败，或者套接字已绑定。
     * @throws  SecurityException       如果存在 {@code SecurityManager}，并且其 {@code checkListen} 方法不允许操作。
     * @throws  IllegalArgumentException 如果 endpoint 是此套接字不支持的 SocketAddress 子类
     * @since 1.4
     */
    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, 50);
    }

    /**
     *
     * 将 {@code ServerSocket} 绑定到特定地址（IP 地址和端口号）。
     * <p>
     * 如果地址为 {@code null}，则系统将选择一个临时端口和一个有效的本地地址来绑定套接字。
     * <P>
     * {@code backlog} 参数是套接字上待处理连接的最大数量。其确切语义是实现特定的。
     * 特别是，实现可能会施加最大长度，或者选择忽略该参数。提供的值应大于 {@code 0}。
     * 如果它小于或等于 {@code 0}，则使用实现特定的默认值。
     * @param   endpoint        要绑定到的 IP 地址和端口号。
     * @param   backlog         请求的待处理连接队列的最大长度。
     * @throws  IOException 如果绑定操作失败，或者套接字已绑定。
     * @throws  SecurityException       如果存在 {@code SecurityManager}，并且其 {@code checkListen} 方法不允许操作。
     * @throws  IllegalArgumentException 如果 endpoint 是此套接字不支持的 SocketAddress 子类
     * @since 1.4
     */
    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!oldImpl && isBound())
            throw new SocketException("Already bound");
        if (endpoint == null)
            endpoint = new InetSocketAddress(0);
        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if (backlog < 1)
          backlog = 50;
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null)
                security.checkListen(epoint.getPort());
            getImpl().bind(epoint.getAddress(), epoint.getPort());
            getImpl().listen(backlog);
            bound = true;
        } catch(SecurityException e) {
            bound = false;
            throw e;
        } catch(IOException e) {
            bound = false;
            throw e;
        }
    }


                /**
     * 返回此服务器套接字的本地地址。
     * <p>
     * 如果套接字在关闭之前已经绑定，
     * 那么即使在套接字关闭后，此方法仍将继续返回本地地址。
     * <p>
     * 如果设置了安全管理器，其 {@code checkConnect} 方法将
     * 用本地地址和 {@code -1} 作为参数调用，以检查操作是否允许。如果操作不被允许，
     * 则返回 {@link InetAddress#getLoopbackAddress loopback} 地址。
     *
     * @return  此套接字绑定的地址，
     *          如果被安全管理器拒绝，则返回回环地址，
     *          如果套接字未绑定，则返回 {@code null}。
     *
     * @see SecurityManager#checkConnect
     */
    public InetAddress getInetAddress() {
        if (!isBound())
            return null;
        try {
            InetAddress in = getImpl().getInetAddress();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkConnect(in.getHostAddress(), -1);
            return in;
        } catch (SecurityException e) {
            return InetAddress.getLoopbackAddress();
        } catch (SocketException e) {
            // nothing
            // 如果已绑定，impl 已创建
            // 所以不应该到这里
        }
        return null;
    }

    /**
     * 返回此套接字正在监听的端口号。
     * <p>
     * 如果套接字在关闭之前已经绑定，
     * 那么即使在套接字关闭后，此方法仍将继续返回端口号。
     *
     * @return  此套接字正在监听的端口号，
     *          如果套接字尚未绑定，则返回 -1。
     */
    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
            // nothing
            // 如果已绑定，impl 已创建
            // 所以不应该到这里
        }
        return -1;
    }

    /**
     * 返回此套接字绑定到的端点的地址。
     * <p>
     * 如果套接字在关闭之前已经绑定，
     * 那么即使在套接字关闭后，此方法仍将继续返回端点的地址。
     * <p>
     * 如果设置了安全管理器，其 {@code checkConnect} 方法将
     * 用本地地址和 {@code -1} 作为参数调用，以检查操作是否允许。如果操作不被允许，
     * 则返回一个表示 {@link InetAddress#getLoopbackAddress loopback} 地址和本地端口的 {@code SocketAddress}。
     *
     * @return  一个表示此套接字本地端点的 {@code SocketAddress}，
     *          或者如果被安全管理器拒绝，则返回一个表示回环地址的 {@code SocketAddress}，
     *          或者如果套接字尚未绑定，则返回 {@code null}。
     *
     * @see #getInetAddress()
     * @see #getLocalPort()
     * @see #bind(SocketAddress)
     * @see SecurityManager#checkConnect
     * @since 1.4
     */

    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    /**
     * 监听并接受连接到此套接字的连接。该方法会阻塞，直到连接建立。
     *
     * <p>创建一个新的 Socket {@code s}，如果设置了安全管理器，
     * 则调用安全管理器的 {@code checkAccept} 方法，用 {@code s.getInetAddress().getHostAddress()} 和
     * {@code s.getPort()} 作为参数，以确保操作允许。
     * 这可能会导致 SecurityException。
     *
     * @exception  IOException  如果在等待连接时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理器且其
     *             {@code checkAccept} 方法不允许操作。
     * @exception  SocketTimeoutException 如果之前通过 setSoTimeout 设置了超时，
     *             并且超时已到达。
     * @exception  java.nio.channels.IllegalBlockingModeException
     *             如果此套接字有相关通道，且通道处于非阻塞模式，
     *             并且没有连接准备好接受。
     *
     * @return 新的 Socket
     * @see SecurityManager#checkAccept
     * @revised 1.4
     * @spec JSR-51
     */
    public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        if (!isBound())
            throw new SocketException("套接字尚未绑定");
        Socket s = new Socket((SocketImpl) null);
        implAccept(s);
        return s;
    }

    /**
     * ServerSocket 的子类使用此方法覆盖 accept()
     * 以返回其自己的套接字子类。因此，FooServerSocket
     * 通常会将一个 <i>空的</i> FooSocket 传递给此方法。从 implAccept 返回时，
     * FooSocket 将连接到客户端。
     *
     * @param s 套接字
     * @throws java.nio.channels.IllegalBlockingModeException
     *         如果此套接字有相关通道，
     *         且通道处于非阻塞模式
     * @throws IOException 如果在等待连接时发生 I/O 错误。
     * @since   JDK1.1
     * @revised 1.4
     * @spec JSR-51
     */
    protected final void implAccept(Socket s) throws IOException {
        SocketImpl si = null;
        try {
            if (s.impl == null)
              s.setImpl();
            else {
                s.impl.reset();
            }
            si = s.impl;
            s.impl = null;
            si.address = new InetAddress();
            si.fd = new FileDescriptor();
            getImpl().accept(si);

            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkAccept(si.getInetAddress().getHostAddress(),
                                     si.getPort());
            }
        } catch (IOException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        } catch (SecurityException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        }
        s.impl = si;
        s.postAccept();
    }

    /**
     * 关闭此套接字。
     *
     * 任何当前在 {@link #accept()} 中阻塞的线程将抛出
     * 一个 {@link SocketException}。
     *
     * <p> 如果此套接字有相关通道，则通道也会关闭。
     *
     * @exception  IOException  如果在关闭套接字时发生 I/O 错误。
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized(closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }

    /**
     * 返回与此套接字关联的唯一 {@link java.nio.channels.ServerSocketChannel} 对象，如果有的话。
     *
     * <p> 一个服务器套接字将有通道，当且仅当该通道本身是通过 {@link
     * java.nio.channels.ServerSocketChannel#open ServerSocketChannel.open}
     * 方法创建的。
     *
     * @return  与此套接字关联的服务器套接字通道，
     *          或者如果此套接字不是为通道创建的，则返回 {@code null}。
     *
     * @since 1.4
     * @spec JSR-51
     */
    public ServerSocketChannel getChannel() {
        return null;
    }

    /**
     * 返回 ServerSocket 的绑定状态。
     *
     * @return 如果 ServerSocket 成功绑定到地址，则返回 true
     * @since 1.4
     */
    public boolean isBound() {
        // 在 1.3 之前，ServerSockets 在创建时总是绑定的
        return bound || oldImpl;
    }

    /**
     * 返回 ServerSocket 的关闭状态。
     *
     * @return 如果套接字已关闭，则返回 true
     * @since 1.4
     */
    public boolean isClosed() {
        synchronized(closeLock) {
            return closed;
        }
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT}，指定超时时间，以毫秒为单位。
     * 设置此选项为非零超时后，对此 ServerSocket 的 accept() 调用将仅阻塞此段时间。
     * 如果超时到期，将抛出 <B>java.net.SocketTimeoutException</B>，但
     * ServerSocket 仍然有效。必须在进入阻塞操作之前启用此选项才能生效。
     * 超时必须 {@code > 0}。
     * 超时为零表示无限超时。
     * @param timeout 指定的超时时间，以毫秒为单位
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @since   JDK1.1
     * @see #getSoTimeout()
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    /**
     * 检索 {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT} 设置。
     * 返回 0 表示该选项已禁用（即超时为无穷大）。
     * @return {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT} 值
     * @exception IOException 如果发生 I/O 错误
     * @since   JDK1.1
     * @see #setSoTimeout(int)
     */
    public synchronized int getSoTimeout() throws IOException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* 额外的类型安全性 */
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 套接字选项。
     * <p>
     * 当 TCP 连接关闭时，连接可能会进入超时状态一段时间（通常称为 {@code TIME_WAIT}
     * 状态或 {@code 2MSL} 等待状态）。
     * 对于使用知名套接字地址或端口的应用程序，
     * 如果有涉及该套接字地址或端口的连接处于超时状态，
     * 则可能无法将套接字绑定到所需的 {@code SocketAddress}。
     * <p>
     * 在使用 {@link #bind(SocketAddress)} 绑定套接字之前启用
     * {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 可以使套接字即使有连接处于超时状态也能绑定。
     * <p>
     * 创建 {@code ServerSocket} 时，{@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 的初始设置是未定义的。
     * 应用程序可以使用 {@link #getReuseAddress()} 来确定
     * {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 的初始设置。
     * <p>
     * 在套接字绑定后（参见 {@link #isBound()}）启用或禁用
     * {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 的行为是未定义的。
     *
     * @param on  是否启用或禁用套接字选项
     * @exception SocketException 如果在启用或禁用
     *            {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 套接字选项时发生错误，或套接字已关闭。
     * @since 1.4
     * @see #getReuseAddress()
     * @see #bind(SocketAddress)
     * @see #isBound()
     * @see #isClosed()
     */
    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }

    /**
     * 测试 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 是否启用。
     *
     * @return 一个 {@code boolean}，指示
     *         {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 是否启用。
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @since   1.4
     * @see #setReuseAddress(boolean)
     */
    public boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        return ((Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }

    /**
     * 返回此套接字的实现地址和实现端口的字符串表示形式。
     * <p>
     * 如果设置了安全管理器，其 {@code checkConnect} 方法将
     * 用本地地址和 {@code -1} 作为参数调用，以检查操作是否允许。如果操作不被允许，
     * 则返回一个表示 {@link InetAddress#getLoopbackAddress loopback} 地址的 {@code InetAddress} 作为实现地址。
     *
     * @return 此套接字的字符串表示形式。
     */
    public String toString() {
        if (!isBound())
            return "ServerSocket[未绑定]";
        InetAddress in;
        if (System.getSecurityManager() != null)
            in = InetAddress.getLoopbackAddress();
        else
            in = impl.getInetAddress();
        return "ServerSocket[addr=" + in +
                ",localport=" + impl.getLocalPort()  + "]";
    }

    void setBound() {
        bound = true;
    }

    void setCreated() {
        created = true;
    }

    /**
     * 所有服务器套接字的工厂。
     */
    private static SocketImplFactory factory = null;

    /**
     * 为应用程序设置服务器套接字实现工厂。工厂只能设置一次。
     * <p>
     * 当应用程序创建新的服务器套接字时，将调用套接字实现工厂的
     * {@code createSocketImpl} 方法来创建实际的套接字实现。
     * <p>
     * 如果工厂尚未设置，传递 {@code null} 到此方法是无操作的。
     * <p>
     * 如果设置了安全管理器，此方法首先调用
     * 安全管理器的 {@code checkSetFactory} 方法
     * 以确保操作允许。这可能会导致 SecurityException。
     *
     * @param      fac   所需的工厂。
     * @exception  IOException  如果在设置套接字工厂时发生 I/O 错误。
     * @exception  SocketException  如果工厂已定义。
     * @exception  SecurityException  如果存在安全管理器且其
     *             {@code checkSetFactory} 方法不允许操作。
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkSetFactory
     */
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
        if (factory != null) {
            throw new SocketException("工厂已定义");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }


                /**
     * 为从这个 {@code ServerSocket} 接受的套接字设置
     * {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项的默认建议值。实际设置在
     * 接受的套接字中的值必须通过调用
     * {@link Socket#getReceiveBufferSize()} 在套接字
     * 由 {@link #accept()} 返回后确定。
     * <p>
     * {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 的值用于
     * 设置内部套接字接收缓冲区的大小，以及设置向远程对等方通告的
     * TCP 接收窗口的大小。
     * <p>
     * 以后可以通过调用
     * {@link Socket#setReceiveBufferSize(int)} 更改该值。但是，如果应用程序
     * 希望允许大于 64K 字节的接收窗口，如 RFC1323 定义的那样，则必须在
     * ServerSocket <B>绑定到本地地址之前</B>
     * 设置建议值。这意味着，必须使用无参数构造函数创建 ServerSocket，然后调用 setReceiveBufferSize()，
     * 最后通过调用 bind() 将 ServerSocket 绑定到地址。
     * <p>
     * 未能这样做不会导致错误，缓冲区大小可能会设置为
     * 请求的值，但从这个 ServerSocket 接受的套接字的
     * TCP 接收窗口将不超过 64K 字节。
     *
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     *
     * @param size 要设置的接收缓冲区大小。此值必须大于 0。
     *
     * @exception IllegalArgumentException 如果值为 0 或为负数。
     *
     * @since 1.4
     * @see #getReceiveBufferSize
     */
     public synchronized void setReceiveBufferSize (int size) throws SocketException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
    }

    /**
     * 获取此 {@code ServerSocket} 的 {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项的值，
     * 即将用于从这个 {@code ServerSocket} 接受的套接字的建议缓冲区大小。
     *
     * <p>请注意，实际设置在接受的套接字中的值通过
     * 调用 {@link Socket#getReceiveBufferSize()} 确定。
     * @return 此 {@code Socket} 的 {@link SocketOptions#SO_RCVBUF SO_RCVBUF}
     *         选项的值。
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @see #setReceiveBufferSize(int)
     * @since 1.4
     */
    public synchronized int getReceiveBufferSize()
    throws SocketException{
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer)o).intValue();
        }
        return result;
    }

    /**
     * 为这个 ServerSocket 设置性能偏好。
     *
     * <p>套接字默认使用 TCP/IP 协议。某些实现可能提供具有不同于 TCP/IP 的性能
     * 特性的替代协议。此方法允许应用程序表达其对这些权衡的偏好
     * 当实现选择可用协议时。
     *
     * <p>性能偏好由三个整数描述
     * 其值表示短连接时间、低延迟和高带宽的相对重要性。整数的绝对值
     * 是无关紧要的；为了选择协议，这些值只是进行比较，较大的值表示较强的偏好。例如，
     * 如果应用程序优先考虑短连接时间而不是低延迟和高带宽，
     * 那么它可以使用值 {@code (1, 0, 0)} 调用此方法。如果应用程序优先考虑高带宽高于低
     * 延迟，而低延迟又高于短连接时间，那么它可以
     * 使用值 {@code (0, 1, 2)} 调用此方法。
     *
     * <p>在套接字已绑定后调用此方法
     * 将不起作用。这意味着，为了使用此功能
     * 需要使用无参数构造函数创建套接字。
     *
     * @param  connectionTime
     *         一个 {@code int} 表示短连接时间的相对重要性
     *
     * @param  latency
     *         一个 {@code int} 表示低延迟的相对重要性
     *
     * @param  bandwidth
     *         一个 {@code int} 表示高带宽的相对重要性
     *
     * @since 1.5
     */
    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth)
    {
        /* Not implemented yet */
    }

}
