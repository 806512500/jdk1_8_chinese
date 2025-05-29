
/*
 * Copyright (c) 1995, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import sun.security.util.SecurityConstants;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;

/**
 * 该类实现了客户端套接字（也称为“套接字”）。套接字是两台机器之间通信的端点。
 * <p>
 * 套接字的实际工作由 {@code SocketImpl} 类的实例完成。应用程序通过更改创建套接字实现的套接字工厂，
 * 可以配置自己以创建适合本地防火墙的套接字。
 *
 * @author 未署名
 * @see java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
 * @see java.net.SocketImpl
 * @see java.nio.channels.SocketChannel
 * @since JDK1.0
 */
public
class Socket implements java.io.Closeable {
    /**
     * 该套接字的各种状态。
     */
    private boolean created = false;
    private boolean bound = false;
    private boolean connected = false;
    private boolean closed = false;
    private Object closeLock = new Object();
    private boolean shutIn = false;
    private boolean shutOut = false;

    /**
     * 该套接字的实现。
     */
    SocketImpl impl;

    /**
     * 是否使用较旧的 SocketImpl？
     */
    private boolean oldImpl = false;

    /**
     * 创建一个未连接的套接字，使用系统默认类型的 SocketImpl。
     *
     * @since JDK1.1
     * @revised 1.4
     */
    public Socket() {
        setImpl();
    }

    /**
     * 创建一个未连接的套接字，指定应使用的代理类型（如果有），
     * 无论其他设置如何。
     * <P>
     * 如果存在安全经理，其 {@code checkConnect} 方法将使用代理主机地址和端口号作为参数调用。
     * 这可能导致 SecurityException。
     * <P>
     * 示例：
     * <UL> <LI>{@code Socket s = new Socket(Proxy.NO_PROXY);} 将创建一个忽略任何其他代理配置的普通套接字。</LI>
     * <LI>{@code Socket s = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("socks.mydom.com", 1080)));}
     * 将创建一个通过指定的 SOCKS 代理服务器连接的套接字。</LI>
     * </UL>
     *
     * @param proxy 一个 {@link java.net.Proxy Proxy} 对象，指定应使用的代理类型。
     * @throws IllegalArgumentException 如果代理类型无效或为 {@code null}。
     * @throws SecurityException 如果存在安全经理，并且连接到代理的权限被拒绝。
     * @see java.net.ProxySelector
     * @see java.net.Proxy
     *
     * @since 1.5
     */
    public Socket(Proxy proxy) {
        // 作为安全措施创建代理的副本
        if (proxy == null) {
            throw new IllegalArgumentException("Invalid Proxy");
        }
        Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY
                                          : sun.net.ApplicationProxy.create(proxy);
        Proxy.Type type = p.type();
        if (type == Proxy.Type.SOCKS || type == Proxy.Type.HTTP) {
            SecurityManager security = System.getSecurityManager();
            InetSocketAddress epoint = (InetSocketAddress) p.address();
            if (epoint.getAddress() != null) {
                checkAddress (epoint.getAddress(), "Socket");
            }
            if (security != null) {
                if (epoint.isUnresolved())
                    epoint = new InetSocketAddress(epoint.getHostName(), epoint.getPort());
                if (epoint.isUnresolved())
                    security.checkConnect(epoint.getHostName(), epoint.getPort());
                else
                    security.checkConnect(epoint.getAddress().getHostAddress(),
                                  epoint.getPort());
            }
            impl = type == Proxy.Type.SOCKS ? new SocksSocketImpl(p)
                                            : new HttpConnectSocketImpl(p);
            impl.setSocket(this);
        } else {
            if (p == Proxy.NO_PROXY) {
                if (factory == null) {
                    impl = new PlainSocketImpl(false);
                    impl.setSocket(this);
                } else
                    setImpl();
            } else
                throw new IllegalArgumentException("Invalid Proxy");
        }
    }

    /**
     * 创建一个未连接的套接字，使用用户指定的 SocketImpl。
     * <P>
     * @param impl 一个 <B>SocketImpl</B> 实例，子类希望在套接字上使用。
     *
     * @exception SocketException 如果底层协议中发生错误，例如 TCP 错误。
     * @since JDK1.1
     */
    protected Socket(SocketImpl impl) throws SocketException {
        this(checkPermission(impl), impl);
    }

    private Socket(Void ignore, SocketImpl impl) {
        if (impl != null) {
            this.impl = impl;
            checkOldImpl();
            impl.setSocket(this);
        }
    }

    private static Void checkPermission(SocketImpl impl) {
        if (impl == null) {
            return null;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_SOCKETIMPL_PERMISSION);
        }
        return null;
    }

    /**
     * 创建一个流套接字并将其连接到命名主机上的指定端口号。
     * <p>
     * 如果指定的主机为 {@code null}，则等同于将地址指定为
     * {@link java.net.InetAddress#getByName InetAddress.getByName}{@code (null)}。
     * 换句话说，等同于指定回环接口的地址。 </p>
     * <p>
     * 如果应用程序指定了一个服务器套接字工厂，该工厂的 {@code createSocketImpl} 方法将被调用以创建实际的套接字实现。
     * 否则将创建一个“普通”套接字。
     * <p>
     * 如果存在安全经理，其 {@code checkConnect} 方法将使用主机地址和 {@code port} 作为参数调用。
     * 这可能导致 SecurityException。
     *
     * @param      host   主机名，或 {@code null} 表示回环地址。
     * @param      port   端口号。
     *
     * @exception  UnknownHostException 如果无法确定主机的 IP 地址。
     *
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全经理，并且其 {@code checkConnect} 方法不允许该操作。
     * @exception  IllegalArgumentException 如果端口参数超出指定的有效端口值范围，即 0 到 65535（包括）。
     * @see        java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkConnect
     */
    public Socket(String host, int port)
        throws UnknownHostException, IOException
    {
        this(host != null ? new InetSocketAddress(host, port) :
             new InetSocketAddress(InetAddress.getByName(null), port),
             (SocketAddress) null, true);
    }

                /**
     * 创建一个流套接字并将其连接到指定 IP 地址的指定端口号。
     * <p>
     * 如果应用程序指定了一个套接字工厂，那么该工厂的
     * {@code createSocketImpl} 方法将被调用来创建实际的套接字实现。否则将创建一个“普通”套接字。
     * <p>
     * 如果存在安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以主机地址和 {@code port}
     * 作为其参数。这可能导致 SecurityException。
     *
     * @param      address   IP 地址。
     * @param      port      端口号。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkConnect} 方法不允许该操作。
     * @exception  IllegalArgumentException 如果端口参数超出
     *             指定的有效端口值范围，该范围包括 0 到 65535。
     * @exception  NullPointerException 如果 {@code address} 为 null。
     * @see        java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkConnect
     */
    public Socket(InetAddress address, int port) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null,
             (SocketAddress) null, true);
    }

    /**
     * 创建一个套接字并将其连接到指定的远程主机上的指定远程端口。套接字还将绑定到提供的本地地址和端口。
     * <p>
     * 如果指定的主机为 {@code null}，则相当于
     * 指定地址为
     * {@link java.net.InetAddress#getByName InetAddress.getByName}{@code (null)}。
     * 换句话说，相当于指定了回环接口的地址。 </p>
     * <p>
     * 本地端口号为 {@code zero} 将允许系统在 {@code bind} 操作中选择一个空闲端口。</p>
     * <p>
     * 如果存在安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以主机地址和 {@code port}
     * 作为其参数。这可能导致 SecurityException。
     *
     * @param host 远程主机的名称，或 {@code null} 以表示回环地址。
     * @param port 远程端口
     * @param localAddr 套接字绑定到的本地地址，或
     *        {@code null} 以表示 {@code anyLocal} 地址。
     * @param localPort 套接字绑定到的本地端口，或
     *        {@code zero} 以表示系统选择的空闲端口。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkConnect} 方法不允许连接到目标，或其 {@code checkListen} 方法
     *             不允许绑定到本地端口。
     * @exception  IllegalArgumentException 如果端口参数或 localPort
     *             参数超出指定的有效端口值范围，该范围包括 0 到 65535。
     * @see        SecurityManager#checkConnect
     * @since   JDK1.1
     */
    public Socket(String host, int port, InetAddress localAddr,
                  int localPort) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) :
               new InetSocketAddress(InetAddress.getByName(null), port),
             new InetSocketAddress(localAddr, localPort), true);
    }

    /**
     * 创建一个套接字并将其连接到指定的远程地址上的指定远程端口。套接字还将绑定到提供的本地地址和端口。
     * <p>
     * 如果指定的本地地址为 {@code null}，则相当于
     * 指定地址为任意本地地址
     * (参见 {@link java.net.InetAddress#isAnyLocalAddress InetAddress.isAnyLocalAddress}{@code ()})。
     * <p>
     * 本地端口号为 {@code zero} 将允许系统在 {@code bind} 操作中选择一个空闲端口。</p>
     * <p>
     * 如果存在安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以主机地址和 {@code port}
     * 作为其参数。这可能导致 SecurityException。
     *
     * @param address 远程地址
     * @param port 远程端口
     * @param localAddr 套接字绑定到的本地地址，或
     *        {@code null} 以表示 {@code anyLocal} 地址。
     * @param localPort 套接字绑定到的本地端口或
     *        {@code zero} 以表示系统选择的空闲端口。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkConnect} 方法不允许连接到目标，或其 {@code checkListen} 方法
     *             不允许绑定到本地端口。
     * @exception  IllegalArgumentException 如果端口参数或 localPort
     *             参数超出指定的有效端口值范围，该范围包括 0 到 65535。
     * @exception  NullPointerException 如果 {@code address} 为 null。
     * @see        SecurityManager#checkConnect
     * @since   JDK1.1
     */
    public Socket(InetAddress address, int port, InetAddress localAddr,
                  int localPort) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null,
             new InetSocketAddress(localAddr, localPort), true);
    }

    /**
     * 创建一个流套接字并将其连接到命名主机上的指定端口号。
     * <p>
     * 如果指定的主机为 {@code null}，则相当于
     * 指定地址为
     * {@link java.net.InetAddress#getByName InetAddress.getByName}{@code (null)}。
     * 换句话说，相当于指定了回环接口的地址。 </p>
     * <p>
     * 如果流参数为 {@code true}，这将创建一个流套接字。如果流参数为 {@code false}，它
     * 将创建一个数据报套接字。
     * <p>
     * 如果应用程序指定了一个服务器套接字工厂，那么该工厂的
     * {@code createSocketImpl} 方法将被调用来创建实际的套接字实现。否则将创建一个“普通”套接字。
     * <p>
     * 如果存在安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以主机地址和 {@code port}
     * 作为其参数。这可能导致 SecurityException。
     * <p>
     * 如果使用 UDP 套接字，TCP/IP 相关的套接字选项将不适用。
     *
     * @param      host     主机名，或 {@code null} 以表示回环地址。
     * @param      port     端口号。
     * @param      stream   一个 {@code boolean}，指示这是一个流套接字还是数据报套接字。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkConnect} 方法不允许该操作。
     * @exception  IllegalArgumentException 如果端口参数超出
     *             指定的有效端口值范围，该范围包括 0 到 65535。
     * @see        java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkConnect
     * @deprecated 使用 DatagramSocket 代替 UDP 传输。
     */
    @Deprecated
    public Socket(String host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) :
               new InetSocketAddress(InetAddress.getByName(null), port),
             (SocketAddress) null, stream);
    }


                /**
     * 创建一个套接字并将其连接到指定 IP 地址的指定端口号。
     * <p>
     * 如果 stream 参数为 {@code true}，则创建一个流套接字。如果 stream 参数为 {@code false}，
     * 则创建一个数据报套接字。
     * <p>
     * 如果应用程序指定了一个服务器套接字工厂，那么将调用该工厂的 {@code createSocketImpl} 方法来创建
     * 实际的套接字实现。否则将创建一个“普通”套接字。
     *
     * <p>如果有安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以 {@code host.getHostAddress()} 和 {@code port}
     * 作为其参数。这可能导致 SecurityException。
     * <p>
     * 如果使用 UDP 套接字，TCP/IP 相关的套接字选项将不适用。
     *
     * @param      host     IP 地址。
     * @param      port      端口号。
     * @param      stream    如果 {@code true}，创建一个流套接字；
     *                       否则，创建一个数据报套接字。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkConnect} 方法不允许此操作。
     * @exception  IllegalArgumentException 如果端口参数超出
     *             指定的有效端口值范围，该范围为 0 到 65535（包括）。
     * @exception  NullPointerException 如果 {@code host} 为 null。
     * @see        java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkConnect
     * @deprecated 使用 DatagramSocket 代替 UDP 传输。
     */
    @Deprecated
    public Socket(InetAddress host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) : null,
             new InetSocketAddress(0), stream);
    }

    private Socket(SocketAddress address, SocketAddress localAddr,
                   boolean stream) throws IOException {
        setImpl();

        // 向后兼容
        if (address == null)
            throw new NullPointerException();

        try {
            createImpl(stream);
            if (localAddr != null)
                bind(localAddr);
            connect(address);
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            try {
                close();
            } catch (IOException ce) {
                e.addSuppressed(ce);
            }
            throw e;
        }
    }

    /**
     * 创建套接字实现。
     *
     * @param stream 一个 {@code boolean} 值：{@code true} 表示 TCP 套接字，
     *               {@code false} 表示 UDP。
     * @throws IOException 如果创建失败
     * @since 1.4
     */
     void createImpl(boolean stream) throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(stream);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    private void checkOldImpl() {
        if (impl == null)
            return;
        // SocketImpl.connect() 是一个受保护的方法，因此我们需要使用
        // getDeclaredMethod，因此我们需要访问成员的权限

        oldImpl = AccessController.doPrivileged
                                (new PrivilegedAction<Boolean>() {
            public Boolean run() {
                Class<?> clazz = impl.getClass();
                while (true) {
                    try {
                        clazz.getDeclaredMethod("connect", SocketAddress.class, int.class);
                        return Boolean.FALSE;
                    } catch (NoSuchMethodException e) {
                        clazz = clazz.getSuperclass();
                        // java.net.SocketImpl 类将始终具有此抽象方法。
                        // 如果在层次结构中尚未找到它，则它不存在，我们是一个旧风格的实现。
                        if (clazz.equals(java.net.SocketImpl.class)) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        });
    }

    /**
     * 将 impl 设置为系统默认类型的 SocketImpl。
     * @since 1.4
     */
    void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            // 这里不需要进行 checkOldImpl()，我们知道它是一个最新的
            // SocketImpl！
            impl = new SocksSocketImpl();
        }
        if (impl != null)
            impl.setSocket(this);
    }


    /**
     * 获取附加到此套接字的 {@code SocketImpl}，必要时创建它。
     *
     * @return  附加到该 ServerSocket 的 {@code SocketImpl}。
     * @throws SocketException 如果创建失败
     * @since 1.4
     */
    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl(true);
        return impl;
    }

    /**
     * 将此套接字连接到服务器。
     *
     * @param   endpoint {@code SocketAddress}
     * @throws  IOException 如果在连接过程中发生错误
     * @throws  java.nio.channels.IllegalBlockingModeException
     *          如果此套接字有一个关联的通道，
     *          并且通道处于非阻塞模式
     * @throws  IllegalArgumentException 如果 endpoint 为 null 或是
     *          SocketAddress 子类，但不被此套接字支持
     * @since 1.4
     * @spec JSR-51
     */
    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    /**
     * 使用指定的超时值将此套接字连接到服务器。
     * 超时值为零表示无限超时。连接将一直阻塞，直到建立连接或发生错误。
     *
     * @param   endpoint {@code SocketAddress}
     * @param   timeout  要使用的超时值（以毫秒为单位）。
     * @throws  IOException 如果在连接过程中发生错误
     * @throws  SocketTimeoutException 如果超时到期前未连接
     * @throws  java.nio.channels.IllegalBlockingModeException
     *          如果此套接字有一个关联的通道，
     *          并且通道处于非阻塞模式
     * @throws  IllegalArgumentException 如果 endpoint 为 null 或是
     *          SocketAddress 子类，但不被此套接字支持
     * @since 1.4
     * @spec JSR-51
     */
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null)
            throw new IllegalArgumentException("connect: The address can't be null");


                    if (timeout < 0)
          throw new IllegalArgumentException("connect: 超时时间不能为负数");

        if (isClosed())
            throw new SocketException("套接字已关闭");

        if (!oldImpl && isConnected())
            throw new SocketException("已连接");

        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("不支持的地址类型");

        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        InetAddress addr = epoint.getAddress ();
        int port = epoint.getPort();
        checkAddress(addr, "connect");

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (epoint.isUnresolved())
                security.checkConnect(epoint.getHostName(), port);
            else
                security.checkConnect(addr.getHostAddress(), port);
        }
        if (!created)
            createImpl(true);
        if (!oldImpl)
            impl.connect(epoint, timeout);
        else if (timeout == 0) {
            if (epoint.isUnresolved())
                impl.connect(addr.getHostName(), port);
            else
                impl.connect(addr, port);
        } else
            throw new UnsupportedOperationException("SocketImpl.connect(addr, timeout)");
        connected = true;
        /*
         * 如果在连接之前套接字未绑定，则现在已绑定，因为
         * 内核将选择一个临时端口和一个本地地址来绑定套接字
         */
        bound = true;
    }

    /**
     * 将套接字绑定到本地地址。
     * <P>
     * 如果地址为 {@code null}，则系统将选择一个临时端口和一个有效的本地地址来绑定套接字。
     *
     * @param   bindpoint 要绑定的 {@code SocketAddress}
     * @throws  IOException 如果绑定操作失败，或套接字已绑定。
     * @throws  IllegalArgumentException 如果 bindpoint 是此套接字不支持的 SocketAddress 子类
     * @throws  SecurityException  如果存在安全经理，且其
     *          {@code checkListen} 方法不允许绑定到本地端口。
     *
     * @since   1.4
     * @see #isBound
     */
    public void bind(SocketAddress bindpoint) throws IOException {
        if (isClosed())
            throw new SocketException("套接字已关闭");
        if (!oldImpl && isBound())
            throw new SocketException("已绑定");

        if (bindpoint != null && (!(bindpoint instanceof InetSocketAddress)))
            throw new IllegalArgumentException("不支持的地址类型");
        InetSocketAddress epoint = (InetSocketAddress) bindpoint;
        if (epoint != null && epoint.isUnresolved())
            throw new SocketException("未解析的地址");
        if (epoint == null) {
            epoint = new InetSocketAddress(0);
        }
        InetAddress addr = epoint.getAddress();
        int port = epoint.getPort();
        checkAddress (addr, "bind");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(port);
        }
        getImpl().bind (addr, port);
        bound = true;
    }

    private void checkAddress (InetAddress addr, String op) {
        if (addr == null) {
            return;
        }
        if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
            throw new IllegalArgumentException(op + ": 无效的地址类型");
        }
    }

    /**
     * 在 accept() 调用后设置标志。
     */
    final void postAccept() {
        connected = true;
        created = true;
        bound = true;
    }

    void setCreated() {
        created = true;
    }

    void setBound() {
        bound = true;
    }

    void setConnected() {
        connected = true;
    }

    /**
     * 返回套接字连接的地址。
     * <p>
     * 如果套接字在关闭前已连接，则此方法在套接字关闭后仍将继续返回连接的地址。
     *
     * @return  此套接字连接的远程 IP 地址，
     *          或者如果套接字未连接，则返回 {@code null}。
     */
    public InetAddress getInetAddress() {
        if (!isConnected())
            return null;
        try {
            return getImpl().getInetAddress();
        } catch (SocketException e) {
        }
        return null;
    }

    /**
     * 获取套接字绑定的本地地址。
     * <p>
     * 如果设置了安全经理，其 {@code checkConnect} 方法将被调用，参数为本地地址和 {@code -1}，以检查操作是否允许。如果操作不允许，
     * 则返回 {@link InetAddress#getLoopbackAddress loopback} 地址。
     *
     * @return 套接字绑定的本地地址，
     *         如果被安全经理拒绝，则返回回环地址，或者如果套接字已关闭或尚未绑定，则返回通配符地址。
     * @since   JDK1.1
     *
     * @see SecurityManager#checkConnect
     */
    public InetAddress getLocalAddress() {
        // 这是为了向后兼容
        if (!isBound())
            return InetAddress.anyLocalAddress();
        InetAddress in = null;
        try {
            in = (InetAddress) getImpl().getOption(SocketOptions.SO_BINDADDR);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkConnect(in.getHostAddress(), -1);
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
        } catch (SecurityException e) {
            in = InetAddress.getLoopbackAddress();
        } catch (Exception e) {
            in = InetAddress.anyLocalAddress(); // "0.0.0.0"
        }
        return in;
    }

    /**
     * 返回此套接字连接的远程端口号。
     * <p>
     * 如果套接字在关闭前已连接，则此方法在套接字关闭后仍将继续返回连接的端口号。
     *
     * @return  此套接字连接的远程端口号，或者如果套接字尚未连接，则返回 0。
     */
    public int getPort() {
        if (!isConnected())
            return 0;
        try {
            return getImpl().getPort();
        } catch (SocketException e) {
            // 不应发生，因为我们已连接
        }
        return -1;
    }


                /**
     * 返回此套接字绑定到的本地端口号。
     * <p>
     * 如果套接字在关闭之前已绑定，
     * 则此方法在套接字关闭后仍将继续返回本地端口号。
     *
     * @return  此套接字绑定到的本地端口号，如果套接字尚未绑定，则返回 -1。
     */
    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch(SocketException e) {
            // 不应该发生，因为我们已绑定
        }
        return -1;
    }

    /**
     * 返回此套接字连接到的端点的地址，如果未连接，则返回
     * {@code null}。
     * <p>
     * 如果套接字在关闭之前已连接，
     * 则此方法在套接字关闭后仍将继续返回连接地址。
     *

     * @return 一个表示此套接字的远程端点的 {@code SocketAddress}，如果尚未连接，则返回 {@code null}。
     * @see #getInetAddress()
     * @see #getPort()
     * @see #connect(SocketAddress, int)
     * @see #connect(SocketAddress)
     * @since 1.4
     */
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected())
            return null;
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    /**
     * 返回此套接字绑定到的端点的地址。
     * <p>
     * 如果一个绑定到由 {@code InetSocketAddress} 表示的端点的套接字被 {@link #close 关闭}，
     * 则此方法在套接字关闭后仍将继续返回一个 {@code InetSocketAddress}。在这种情况下，返回的
     * {@code InetSocketAddress} 的地址是 {@link InetAddress#isAnyLocalAddress 通配符} 地址，
     * 其端口是它绑定到的本地端口。
     * <p>
     * 如果设置了安全管理器，其 {@code checkConnect} 方法将被调用，参数为本地地址和 {@code -1}，
     * 以检查操作是否允许。如果操作不允许，
     * 则返回一个表示 {@link InetAddress#getLoopbackAddress 回环} 地址和此套接字绑定到的本地端口的 {@code SocketAddress}。
     *
     * @return 一个表示此套接字的本地端点的 {@code SocketAddress}，如果安全管理器拒绝，则返回一个表示回环地址的 {@code SocketAddress}，
     *         或者如果套接字尚未绑定，则返回 {@code null}。
     *
     * @see #getLocalAddress()
     * @see #getLocalPort()
     * @see #bind(SocketAddress)
     * @see SecurityManager#checkConnect
     * @since 1.4
     */

    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    /**
     * 返回与此套接字关联的唯一 {@link java.nio.channels.SocketChannel SocketChannel} 对象，如果有的话。
     *
     * <p> 一个套接字将有一个通道，当且仅当该通道本身是通过 {@link java.nio.channels.SocketChannel#open
     * SocketChannel.open} 或 {@link
     * java.nio.channels.ServerSocketChannel#accept ServerSocketChannel.accept}
     * 方法创建的。
     *
     * @return 与此套接字关联的套接字通道，
     *          或者如果此套接字不是为通道创建的，则返回 {@code null}。
     *
     * @since 1.4
     * @spec JSR-51
     */
    public SocketChannel getChannel() {
        return null;
    }

    /**
     * 返回此套接字的输入流。
     *
     * <p> 如果此套接字有一个关联的通道，则返回的输入流将委托其所有操作给该通道。如果通道处于非阻塞模式，
     * 则输入流的 {@code read} 操作将抛出一个 {@link java.nio.channels.IllegalBlockingModeException}。
     *
     * <p>在异常情况下，远程主机或网络软件（例如 TCP 连接的连接重置）可能会中断底层连接。当网络软件检测到连接中断时，
     * 以下规则适用于返回的输入流：
     *
     * <ul>
     *
     *   <li><p>网络软件可能会丢弃套接字缓冲的字节。未被网络软件丢弃的字节可以使用 {@link java.io.InputStream#read read} 读取。
     *
     *   <li><p>如果套接字上没有缓冲的字节，或者所有缓冲的字节已被
     *   {@link java.io.InputStream#read read} 消耗，则所有后续的
     *   {@link java.io.InputStream#read read} 调用将抛出一个
     *   {@link java.io.IOException IOException}。
     *
     *   <li><p>如果套接字上没有缓冲的字节，并且套接字尚未使用 {@link #close close} 关闭，
     *   则 {@link java.io.InputStream#available available} 将返回 {@code 0}。
     *
     * </ul>
     *
     * <p> 关闭返回的 {@link java.io.InputStream InputStream} 将关闭关联的套接字。
     *
     * @return 用于从此套接字读取字节的输入流。
     * @exception  IOException  如果在创建输入流时发生 I/O 错误，套接字已关闭，套接字未连接，或者使用 {@link #shutdownInput()}
     *             关闭了套接字输入。
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public InputStream getInputStream() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isInputShutdown())
            throw new SocketException("Socket input is shutdown");
        final Socket s = this;
        InputStream is = null;
        try {
            is = AccessController.doPrivileged(
                new PrivilegedExceptionAction<InputStream>() {
                    public InputStream run() throws IOException {
                        return impl.getInputStream();
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        return is;
    }

                /**
     * 返回此套接字的输出流。
     *
     * <p> 如果此套接字有一个关联的通道，则结果输出流将委托其所有操作给该通道。如果通道处于非阻塞模式，则输出流的 {@code write}
     * 操作将抛出一个 {@link
     * java.nio.channels.IllegalBlockingModeException}。
     *
     * <p> 关闭返回的 {@link java.io.OutputStream OutputStream}
     * 将关闭关联的套接字。
     *
     * @return     一个用于向此套接字写入字节的输出流。
     * @exception  IOException  如果在创建输出流时发生 I/O 错误或套接字未连接。
     * @revised 1.4
     * @spec JSR-51
     */
    public OutputStream getOutputStream() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isOutputShutdown())
            throw new SocketException("Socket output is shutdown");
        final Socket s = this;
        OutputStream os = null;
        try {
            os = AccessController.doPrivileged(
                new PrivilegedExceptionAction<OutputStream>() {
                    public OutputStream run() throws IOException {
                        return impl.getOutputStream();
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        return os;
    }

    /**
     * 启用/禁用 {@link SocketOptions#TCP_NODELAY TCP_NODELAY}
     * （禁用/启用 Nagle 算法）。
     *
     * @param on {@code true} 启用 TCP_NODELAY，
     * {@code false} 禁用。
     *
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     *
     * @since   JDK1.1
     *
     * @see #getTcpNoDelay()
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
    }

    /**
     * 测试是否启用了 {@link SocketOptions#TCP_NODELAY TCP_NODELAY}。
     *
     * @return 一个 {@code boolean}，指示是否启用了
     *         {@link SocketOptions#TCP_NODELAY TCP_NODELAY}。
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @since   JDK1.1
     * @see #setTcpNoDelay(boolean)
     */
    public boolean getTcpNoDelay() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.TCP_NODELAY)).booleanValue();
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_LINGER SO_LINGER} 并指定逗留时间（以秒为单位）。最大超时值取决于平台。
     *
     * 该设置仅影响套接字关闭。
     *
     * @param on     是否逗留。
     * @param linger 如果 on 为 true，则逗留多长时间。
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @exception IllegalArgumentException 如果逗留值为负数。
     * @since JDK1.1
     * @see #getSoLinger()
     */
    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!on) {
            getImpl().setOption(SocketOptions.SO_LINGER, new Boolean(on));
        } else {
            if (linger < 0) {
                throw new IllegalArgumentException("invalid value for SO_LINGER");
            }
            if (linger > 65535)
                linger = 65535;
            getImpl().setOption(SocketOptions.SO_LINGER, new Integer(linger));
        }
    }

    /**
     * 返回 {@link SocketOptions#SO_LINGER SO_LINGER} 的设置。
     * -1 表示该选项已禁用。
     *
     * 该设置仅影响套接字关闭。
     *
     * @return {@link SocketOptions#SO_LINGER SO_LINGER} 的设置。
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     * @since   JDK1.1
     * @see #setSoLinger(boolean, int)
     */
    public int getSoLinger() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_LINGER);
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return -1;
        }
    }

    /**
     * 在套接字上发送一个字节的紧急数据。要发送的字节是数据参数的最低八位。紧急字节
     * 在任何先前写入套接字 OutputStream 的数据之后发送，并在任何未来的写入 OutputStream 之前发送。
     * @param data 要发送的数据字节
     * @exception IOException 如果发送数据时发生错误。
     * @since 1.4
     */
    public void sendUrgentData (int data) throws IOException  {
        if (!getImpl().supportsUrgentData ()) {
            throw new SocketException ("Urgent data not supported");
        }
        getImpl().sendUrgentData (data);
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_OOBINLINE SO_OOBINLINE}
     * （接收 TCP 紧急数据）
     *
     * 默认情况下，此选项已禁用，套接字接收到的 TCP 紧急数据将被静默丢弃。如果用户希望接收紧急数据，则
     * 必须启用此选项。启用后，紧急数据将与正常数据一起接收。
     * <p>
     * 注意，仅提供有限的支持来处理传入的紧急数据。特别是，不提供传入紧急数据的通知，
     * 除非由更高层的协议提供，否则没有区分正常数据和紧急数据的能力。
     *
     * @param on {@code true} 启用
     *           {@link SocketOptions#SO_OOBINLINE SO_OOBINLINE}，
     *           {@code false} 禁用。
     *
     * @exception SocketException 如果底层协议发生错误，例如 TCP 错误。
     *
     * @since   1.4
     *
     * @see #getOOBInline()
     */
    public void setOOBInline(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_OOBINLINE, Boolean.valueOf(on));
    }

                /**
     * 测试 {@link SocketOptions#SO_OOBINLINE SO_OOBINLINE} 是否已启用。
     *
     * @return 一个 {@code boolean} 值，指示 {@link SocketOptions#SO_OOBINLINE SO_OOBINLINE} 是否已启用。
     *
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     * @since   1.4
     * @see #setOOBInline(boolean)
     */
    public boolean getOOBInline() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.SO_OOBINLINE)).booleanValue();
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT}，指定超时时间，以毫秒为单位。设置此选项为非零超时时间后，
     * 与该 Socket 关联的 InputStream 上的 read() 调用将仅阻塞此段时间。如果超时时间到期，将抛出 <B>java.net.SocketTimeoutException</B>，
     * 但 Socket 仍然有效。必须在进入阻塞操作之前启用此选项才能生效。超时时间必须 {@code > 0}。
     * 超时时间为零表示无限超时。
     *
     * @param timeout 指定的超时时间，以毫秒为单位。
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     * @since   JDK 1.1
     * @see #getSoTimeout()
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (timeout < 0)
          throw new IllegalArgumentException("timeout can't be negative");

        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    /**
     * 返回 {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT} 的设置。返回 0 表示该选项已禁用（即无限超时）。
     *
     * @return {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT} 的设置
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     *
     * @since   JDK1.1
     * @see #setSoTimeout(int)
     */
    public synchronized int getSoTimeout() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* 额外的类型安全 */
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }

    /**
     * 将此 {@code Socket} 的 {@link SocketOptions#SO_SNDBUF SO_SNDBUF} 选项设置为指定值。
     * {@link SocketOptions#SO_SNDBUF SO_SNDBUF} 选项用于作为平台网络代码设置底层网络 I/O 缓冲区大小的提示。
     *
     * <p>因为 {@link SocketOptions#SO_SNDBUF SO_SNDBUF} 是一个提示，希望验证缓冲区大小的应用程序应调用 {@link #getSendBufferSize()}。
     *
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     *
     * @param size 要设置的发送缓冲区大小。此值必须大于 0。
     *
     * @exception IllegalArgumentException 如果值为 0 或为负数。
     *
     * @see #getSendBufferSize()
     * @since 1.2
     */
    public synchronized void setSendBufferSize(int size)
    throws SocketException{
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative send size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_SNDBUF, new Integer(size));
    }

    /**
     * 获取此 {@code Socket} 的 {@link SocketOptions#SO_SNDBUF SO_SNDBUF} 选项的值，即平台用于此 {@code Socket} 输出的缓冲区大小。
     * @return 此 {@code Socket} 的 {@link SocketOptions#SO_SNDBUF SO_SNDBUF} 选项的值。
     *
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     *
     * @see #setSendBufferSize(int)
     * @since 1.2
     */
    public synchronized int getSendBufferSize() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_SNDBUF);
        if (o instanceof Integer) {
            result = ((Integer)o).intValue();
        }
        return result;
    }

    /**
     * 将此 {@code Socket} 的 {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项设置为指定值。{@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项
     * 用于作为平台网络代码设置底层网络 I/O 缓冲区大小的提示。
     *
     * <p>增加接收缓冲区大小可以提高高流量连接的网络 I/O 性能，而减少它可以帮助减少传入数据的积压。
     *
     * <p>因为 {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 是一个提示，希望验证缓冲区大小的应用程序应调用 {@link #getReceiveBufferSize()}。
     *
     * <p>{@link SocketOptions#SO_RCVBUF SO_RCVBUF} 的值还用于设置向远程对等体通告的 TCP 接收窗口。通常，窗口大小可以在任何时候修改，当一个套接字连接时。但是，如果需要大于 64K 的接收窗口，则必须在套接字连接到远程对等体之前请求。有两种情况需要注意：
     * <ol>
     * <li>对于从 ServerSocket 接受的套接字，必须在 ServerSocket 绑定到本地地址之前调用 {@link ServerSocket#setReceiveBufferSize(int)}。<p></li>
     * <li>对于客户端套接字，必须在连接套接字到其远程对等体之前调用 setReceiveBufferSize()。</li></ol>
     * @param size 要设置的接收缓冲区大小。此值必须大于 0。
     *
     * @exception IllegalArgumentException 如果值为 0 或为负数。
     *
     * @exception SocketException 如果底层协议（如TCP错误）中出现错误。
     *
     * @see #getReceiveBufferSize()
     * @see ServerSocket#setReceiveBufferSize(int)
     * @since 1.2
     */
    public synchronized void setReceiveBufferSize(int size)
    throws SocketException{
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
    }


                /**
     * 获取此 {@code Socket} 的 {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项的值，
     * 即平台为此 {@code Socket} 的输入使用的缓冲区大小。
     *
     * @return 此 {@code Socket} 的 {@link SocketOptions#SO_RCVBUF SO_RCVBUF} 选项的值。
     * @exception SocketException 如果底层协议（如 TCP 错误）中发生错误。
     * @see #setReceiveBufferSize(int)
     * @since 1.2
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
     * 启用/禁用 {@link SocketOptions#SO_KEEPALIVE SO_KEEPALIVE}。
     *
     * @param on  是否开启 socket 保持活动。
     * @exception SocketException 如果底层协议（如 TCP 错误）中发生错误。
     * @since 1.3
     * @see #getKeepAlive()
     */
    public void setKeepAlive(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_KEEPALIVE, Boolean.valueOf(on));
    }

    /**
     * 测试是否启用了 {@link SocketOptions#SO_KEEPALIVE SO_KEEPALIVE}。
     *
     * @return 一个 {@code boolean} 值，指示是否启用了 {@link SocketOptions#SO_KEEPALIVE SO_KEEPALIVE}。
     * @exception SocketException 如果底层协议（如 TCP 错误）中发生错误。
     * @since   1.3
     * @see #setKeepAlive(boolean)
     */
    public boolean getKeepAlive() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.SO_KEEPALIVE)).booleanValue();
    }

    /**
     * 设置从该 Socket 发送的数据包的 IP 头中的流量类别或服务类型字节。
     * 由于底层网络实现可能忽略此值，应用程序应将其视为提示。
     *
     * <P> tc <B>必须</B> 在范围 {@code 0 <= tc <= 255} 内，否则将抛出 IllegalArgumentException。
     * <p>注意：
     * <p>对于 Internet Protocol v4，该值由一个 {@code integer} 组成，其最低有效 8 位表示
     * 由套接字发送的数据包的 IP 头中的 TOS 字节。
     * RFC 1349 定义了 TOS 值如下：
     *
     * <UL>
     * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
     * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
     * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
     * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
     * </UL>
     * 最后一个最低位总是被忽略，因为它对应于 MBZ（必须为零）位。
     * <p>
     * 在优先级字段中设置位可能会导致 SocketException，表示该操作不被允许。
     * <p>
     * 根据 RFC 1122 第 4.2.4.2 节，一个合规的 TCP 实现应允许，但不要求应用程序在连接生命周期内更改 TOS 字段。
     * 因此，是否可以在 TCP 连接建立后更改服务类型字段取决于底层平台的实现。应用程序不应假设它们可以在连接后更改 TOS 字段。
     * <p>
     * 对于 Internet Protocol v6，tc 是将被放置在 IP 头的 sin6_flowinfo 字段中的值。
     *
     * @param tc        一个 {@code int} 值，用于位集。
     * @throws SocketException 如果设置流量类别或服务类型时发生错误。
     * @since 1.4
     * @see #getTrafficClass
     * @see SocketOptions#IP_TOS
     */
    public void setTrafficClass(int tc) throws SocketException {
        if (tc < 0 || tc > 255)
            throw new IllegalArgumentException("tc is not in range 0 -- 255");

        if (isClosed())
            throw new SocketException("Socket is closed");
        try {
            getImpl().setOption(SocketOptions.IP_TOS, tc);
        } catch (SocketException se) {
            // 如果套接字已连接，则不支持此操作
            // Solaris 在这种情况下会返回错误
            if(!isConnected())
                throw se;
        }
    }

    /**
     * 获取从该 Socket 发送的数据包的 IP 头中的流量类别或服务类型。
     * <p>
     * 由于底层网络实现可能忽略使用 {@link #setTrafficClass(int)} 设置的流量类别或服务类型，
     * 此方法返回的值可能与之前使用 {@link #setTrafficClass(int)} 方法在此 Socket 上设置的值不同。
     *
     * @return 已设置的流量类别或服务类型。
     * @throws SocketException 如果获取流量类别或服务类型值时发生错误。
     * @since 1.4
     * @see #setTrafficClass(int)
     * @see SocketOptions#IP_TOS
     */
    public int getTrafficClass() throws SocketException {
        return ((Integer) (getImpl().getOption(SocketOptions.IP_TOS))).intValue();
    }

    /**
     * 启用/禁用 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 套接字选项。
     * <p>
     * 当 TCP 连接关闭时，连接可能会在关闭后的一段时间内保持在超时状态（通常称为 {@code TIME_WAIT} 状态
     * 或 {@code 2MSL} 等待状态）。对于使用知名套接字地址或端口的应用程序，如果存在涉及该套接字地址或端口的超时状态的连接，
     * 可能无法将套接字绑定到所需的 {@code SocketAddress}。
     * <p>
     * 在使用 {@link #bind(SocketAddress)} 绑定套接字之前启用 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR}，
     * 即使存在处于超时状态的先前连接，也可以将套接字绑定。
     * <p>
     * 创建套接字时，{@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 的初始设置是禁用的。
     * <p>
     * 在套接字绑定后（参见 {@link #isBound()}）启用或禁用 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 的行为未定义。
     *
     * @param on  是否启用或禁用套接字选项。
     * @exception SocketException 如果启用或禁用 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 套接字选项时发生错误，或套接字已关闭。
     * @since 1.4
     * @see #getReuseAddress()
     * @see #bind(SocketAddress)
     * @see #isClosed()
     * @see #isBound()
     */
    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }


                /**
     * 测试 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 是否已启用。
     *
     * @return 一个 {@code boolean} 值，指示 {@link SocketOptions#SO_REUSEADDR SO_REUSEADDR} 是否已启用。
     * @exception SocketException 如果底层协议（如 TCP 错误）发生错误。
     * @since   1.4
     * @see #setReuseAddress(boolean)
     */
    public boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }

    /**
     * 关闭此套接字。
     * <p>
     * 任何当前在此套接字上阻塞的 I/O 操作的线程
     * 将抛出一个 {@link SocketException}。
     * <p>
     * 一旦套接字被关闭，它将不再可用于进一步的网络操作
     * （即不能重新连接或重新绑定）。需要创建一个新的套接字。
     *
     * <p> 关闭此套接字还将关闭套接字的
     * {@link java.io.InputStream InputStream} 和
     * {@link java.io.OutputStream OutputStream}。
     *
     * <p> 如果此套接字有一个关联的通道，则该通道也会被关闭。
     *
     * @exception  IOException  如果关闭此套接字时发生 I/O 错误。
     * @revised 1.4
     * @spec JSR-51
     * @see #isClosed
     */
    public synchronized void close() throws IOException {
        synchronized(closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }

    /**
     * 将此套接字的输入流置于“流结束”状态。
     * 发送到套接字输入流的任何数据都将被确认
     * 并默默地丢弃。
     * <p>
     * 如果在调用此方法后从套接字输入流读取数据，
     * 流的 {@code available} 方法将返回 0，其
     * {@code read} 方法将返回 {@code -1}（流结束）。
     *
     * @exception IOException 如果关闭此套接字时发生 I/O 错误。
     *
     * @since 1.3
     * @see java.net.Socket#shutdownOutput()
     * @see java.net.Socket#close()
     * @see java.net.Socket#setSoLinger(boolean, int)
     * @see #isInputShutdown
     */
    public void shutdownInput() throws IOException
    {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isInputShutdown())
            throw new SocketException("Socket input is already shutdown");
        getImpl().shutdownInput();
        shutIn = true;
    }

    /**
     * 禁用此套接字的输出流。
     * 对于 TCP 套接字，任何先前写入的数据都将被发送
     * 然后是 TCP 的正常连接终止序列。
     *
     * 如果在调用 shutdownOutput() 后写入套接字输出流，
     * 流将抛出一个 IOException。
     *
     * @exception IOException 如果关闭此套接字时发生 I/O 错误。
     *
     * @since 1.3
     * @see java.net.Socket#shutdownInput()
     * @see java.net.Socket#close()
     * @see java.net.Socket#setSoLinger(boolean, int)
     * @see #isOutputShutdown
     */
    public void shutdownOutput() throws IOException
    {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isOutputShutdown())
            throw new SocketException("Socket output is already shutdown");
        getImpl().shutdownOutput();
        shutOut = true;
    }

    /**
     * 将此套接字转换为 {@code String}。
     *
     * @return 此套接字的字符串表示形式。
     */
    public String toString() {
        try {
            if (isConnected())
                return "Socket[addr=" + getImpl().getInetAddress() +
                    ",port=" + getImpl().getPort() +
                    ",localport=" + getImpl().getLocalPort() + "]";
        } catch (SocketException e) {
        }
        return "Socket[unconnected]";
    }

    /**
     * 返回套接字的连接状态。
     * <p>
     * 注意：关闭套接字不会清除其连接状态，这意味着
     * 如果套接字在关闭前已成功连接到服务器，此方法将返回 {@code true}
     * （参见 {@link #isClosed()}）。
     *
     * @return 如果套接字已成功连接到服务器，则返回 true
     * @since 1.4
     */
    public boolean isConnected() {
        // 在 1.3 之前，套接字在创建时总是已连接
        return connected || oldImpl;
    }

    /**
     * 返回套接字的绑定状态。
     * <p>
     * 注意：关闭套接字不会清除其绑定状态，这意味着
     * 如果套接字在关闭前已成功绑定到地址，此方法将返回 {@code true}
     * （参见 {@link #isClosed()}）。
     *
     * @return 如果套接字已成功绑定到地址，则返回 true
     * @since 1.4
     * @see #bind
     */
    public boolean isBound() {
        // 在 1.3 之前，套接字在创建时总是已绑定
        return bound || oldImpl;
    }

    /**
     * 返回套接字的关闭状态。
     *
     * @return 如果套接字已关闭，则返回 true
     * @since 1.4
     * @see #close
     */
    public boolean isClosed() {
        synchronized(closeLock) {
            return closed;
        }
    }

    /**
     * 返回套接字连接的读取部分是否已关闭。
     *
     * @return 如果套接字的输入已关闭，则返回 true
     * @since 1.4
     * @see #shutdownInput
     */
    public boolean isInputShutdown() {
        return shutIn;
    }

    /**
     * 返回套接字连接的写入部分是否已关闭。
     *
     * @return 如果套接字的输出已关闭，则返回 true
     * @since 1.4
     * @see #shutdownOutput
     */
    public boolean isOutputShutdown() {
        return shutOut;
    }


                /**
     * 所有客户端套接字的工厂。
     */
    private static SocketImplFactory factory = null;

    /**
     * 为应用程序设置客户端套接字实现工厂。工厂只能指定一次。
     * <p>
     * 当应用程序创建新的客户端套接字时，将调用套接字实现工厂的 {@code createSocketImpl} 方法来创建实际的套接字实现。
     * <p>
     * 如果工厂已经设置，传递 {@code null} 到该方法将不会执行任何操作。
     * <p>如果有安全管理者，此方法首先调用安全管理者的 {@code checkSetFactory} 方法
     * 以确保操作被允许。这可能导致 SecurityException。
     *
     * @param      fac   所需的工厂。
     * @exception  IOException  如果在设置套接字工厂时发生 I/O 错误。
     * @exception  SocketException  如果工厂已定义。
     * @exception  SecurityException  如果存在安全管理者且其 {@code checkSetFactory} 方法不允许操作。
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkSetFactory
     */
    public static synchronized void setSocketImplFactory(SocketImplFactory fac)
        throws IOException
    {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    /**
     * 为该套接字设置性能偏好。
     *
     * <p> 套接字默认使用 TCP/IP 协议。某些实现可能提供具有与 TCP/IP 不同性能特征的替代协议。此方法允许应用程序表达其对这些权衡的偏好，
     * 当实现从可用协议中选择时，这些偏好将被考虑。
     *
     * <p> 性能偏好由三个整数描述，这些整数的值表示短连接时间、低延迟和高带宽的相对重要性。这些整数的绝对值无关紧要；为了选择协议，
     * 只需比较这些值，较大的值表示更强的偏好。负值的优先级低于正值。例如，如果应用程序优先考虑短连接时间而不是低延迟和高带宽，
     * 则可以使用值 {@code (1, 0, 0)} 调用此方法。如果应用程序优先考虑高带宽高于低延迟，低延迟高于短连接时间，
     * 则可以使用值 {@code (0, 1, 2)} 调用此方法。
     *
     * <p> 在此套接字已连接后调用此方法将不会产生任何效果。
     *
     * @param  connectionTime
     *         一个 {@code int} 表示短连接时间的相对重要性。
     *
     * @param  latency
     *         一个 {@code int} 表示低延迟的相对重要性。
     *
     * @param  bandwidth
     *         一个 {@code int} 表示高带宽的相对重要性。
     *
     * @since 1.5
     */
    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth)
    {
        /* 尚未实现 */
    }
}
