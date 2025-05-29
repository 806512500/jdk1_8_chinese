
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * 该类表示用于发送和接收数据报包的套接字。
 *
 * <p>数据报套接字是数据包传输服务的发送或接收点。在数据报套接字上发送或接收的每个数据包都是单独寻址和路由的。从一台机器发送到另一台机器的多个数据包可能会通过不同的路由传输，并且可能会以任何顺序到达。
 *
 * <p>如果可能，新构造的 {@code DatagramSocket} 会启用 {@link SocketOptions#SO_BROADCAST SO_BROADCAST} 套接字选项，以允许传输广播数据报。为了接收广播数据包，DatagramSocket 应该绑定到通配符地址。在某些实现中，当 DatagramSocket 绑定到更具体的地址时，也可能接收到广播数据包。
 * <p>
 * 示例：
 * {@code
 *              DatagramSocket s = new DatagramSocket(null);
 *              s.bind(new InetSocketAddress(8888));
 * }
 * 这等同于：
 * {@code
 *              DatagramSocket s = new DatagramSocket(8888);
 * }
 * 两种情况下都会创建一个能够在 UDP 端口 8888 上接收广播的 DatagramSocket。
 *
 * @author  Pavani Diwanji
 * @see     java.net.DatagramPacket
 * @see     java.nio.channels.DatagramChannel
 * @since JDK1.0
 */
public
class DatagramSocket implements java.io.Closeable {
    /**
     * 此套接字的各种状态。
     */
    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private Object closeLock = new Object();

    /*
     * 此 DatagramSocket 的实现。
     */
    DatagramSocketImpl impl;

    /**
     * 是否使用较旧的 DatagramSocketImpl？
     */
    boolean oldImpl = false;

    /**
     * 当套接字为 ST_CONNECTED 状态时设置，直到确定在调用 connect() 之前可能已接收但未被应用程序读取的任何数据包已被读取。在此期间，我们检查所有接收到的数据包的源地址，以确保它们来自连接的目标。其他数据包会被读取但静默丢弃。
     */
    private boolean explicitFilter = false;
    private int bytesLeftToFilter;
    /*
     * 连接状态：
     * ST_NOT_CONNECTED = 套接字未连接
     * ST_CONNECTED = 套接字已连接
     * ST_CONNECTED_NO_IMPL = 套接字已连接但未在实现级别连接
     */
    static final int ST_NOT_CONNECTED = 0;
    static final int ST_CONNECTED = 1;
    static final int ST_CONNECTED_NO_IMPL = 2;

    int connectState = ST_NOT_CONNECTED;

    /*
     * 连接的地址和端口
     */
    InetAddress connectedAddress = null;
    int connectedPort = -1;

    /**
     * 将此套接字连接到远程套接字地址（IP 地址 + 端口号）。如果尚未绑定套接字，则绑定套接字。
     * <p>
     * @param   address 远程地址。
     * @param   port    远程端口
     * @throws  SocketException 如果绑定套接字失败。
     */
    private synchronized void connectInternal(InetAddress address, int port) throws SocketException {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("connect: " + port);
        }
        if (address == null) {
            throw new IllegalArgumentException("connect: null address");
        }
        checkAddress (address, "connect");
        if (isClosed())
            return;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (address.isMulticastAddress()) {
                security.checkMulticast(address);
            } else {
                security.checkConnect(address.getHostAddress(), port);
                security.checkAccept(address.getHostAddress(), port);
            }
        }

        if (!isBound())
          bind(new InetSocketAddress(0));

        // 旧实现不支持连接/断开连接
        if (oldImpl || (impl instanceof AbstractPlainDatagramSocketImpl &&
             ((AbstractPlainDatagramSocketImpl)impl).nativeConnectDisabled())) {
            connectState = ST_CONNECTED_NO_IMPL;
        } else {
            try {
                getImpl().connect(address, port);

                // 套接字现在已通过实现连接
                connectState = ST_CONNECTED;
                // 是否需要过滤一些数据包？
                int avail = getImpl().dataAvailable();
                if (avail == -1) {
                    throw new SocketException();
                }
                explicitFilter = avail > 0;
                if (explicitFilter) {
                    bytesLeftToFilter = getReceiveBufferSize();
                }
            } catch (SocketException se) {

                // 连接将由 DatagramSocket 模拟
                connectState = ST_CONNECTED_NO_IMPL;
            }
        }

        connectedAddress = address;
        connectedPort = port;
    }


    /**
     * 构造一个数据报套接字并将其绑定到本地主机上的任何可用端口。套接字将绑定到 {@link InetAddress#isAnyLocalAddress 通配符} 地址，这是一个由内核选择的 IP 地址。
     *
     * <p>如果有安全经理，首先调用其 {@code checkListen} 方法，参数为 0，以确保操作被允许。这可能会导致 SecurityException。
     *
     * @exception  SocketException  如果无法打开套接字，或者套接字无法绑定到指定的本地端口。
     * @exception  SecurityException  如果存在安全经理，且其 {@code checkListen} 方法不允许操作。
     *
     * @see SecurityManager#checkListen
     */
    public DatagramSocket() throws SocketException {
        this(new InetSocketAddress(0));
    }


                /**
     * 创建一个未绑定的数据报套接字，使用指定的 DatagramSocketImpl。
     *
     * @param impl 一个 <B>DatagramSocketImpl</B> 的实例，子类希望在 DatagramSocket 上使用。
     * @since   1.4
     */
    protected DatagramSocket(DatagramSocketImpl impl) {
        if (impl == null)
            throw new NullPointerException();
        this.impl = impl;
        checkOldImpl();
    }

    /**
     * 创建一个数据报套接字，并绑定到指定的本地套接字地址。
     * <p>
     * 如果地址为 {@code null}，则创建一个未绑定的套接字。
     *
     * <p>如果有安全经理，
     * 首先调用其 {@code checkListen} 方法
     * 以确保操作被允许，使用套接字地址的端口作为参数。
     * 这可能会导致 SecurityException。
     *
     * @param bindaddr 要绑定的本地套接字地址，或 {@code null} 表示未绑定的套接字。
     *
     * @exception  SocketException  如果无法打开套接字，或套接字无法绑定到指定的本地端口。
     * @exception  SecurityException  如果存在安全经理，且其 {@code checkListen} 方法不允许操作。
     *
     * @see SecurityManager#checkListen
     * @since   1.4
     */
    public DatagramSocket(SocketAddress bindaddr) throws SocketException {
        // 创建一个数据报套接字。
        createImpl();
        if (bindaddr != null) {
            try {
                bind(bindaddr);
            } finally {
                if (!isBound())
                    close();
            }
        }
    }

    /**
     * 构造一个数据报套接字并将其绑定到本地主机上的指定端口。
     * 套接字将绑定到 {@link InetAddress#isAnyLocalAddress 通配符} 地址，
     * 由内核选择的 IP 地址。
     *
     * <p>如果有安全经理，
     * 首先调用其 {@code checkListen} 方法
     * 以确保操作被允许，使用 {@code port} 参数作为其参数。
     * 这可能会导致 SecurityException。
     *
     * @param      port 要使用的端口。
     * @exception  SocketException  如果无法打开套接字，或套接字无法绑定到指定的本地端口。
     * @exception  SecurityException  如果存在安全经理，且其 {@code checkListen} 方法不允许操作。
     *
     * @see SecurityManager#checkListen
     */
    public DatagramSocket(int port) throws SocketException {
        this(port, null);
    }

    /**
     * 创建一个数据报套接字，并绑定到指定的本地地址。
     * 本地端口必须在 0 到 65535 之间（包括 0 和 65535）。
     * 如果 IP 地址为 0.0.0.0，套接字将绑定到
     * {@link InetAddress#isAnyLocalAddress 通配符} 地址，
     * 由内核选择的 IP 地址。
     *
     * <p>如果有安全经理，
     * 首先调用其 {@code checkListen} 方法
     * 以确保操作被允许，使用 {@code port} 参数作为其参数。
     * 这可能会导致 SecurityException。
     *
     * @param port 要使用的本地端口
     * @param laddr 要绑定的本地地址
     *
     * @exception  SocketException  如果无法打开套接字，或套接字无法绑定到指定的本地端口。
     * @exception  SecurityException  如果存在安全经理，且其 {@code checkListen} 方法不允许操作。
     *
     * @see SecurityManager#checkListen
     * @since   JDK1.1
     */
    public DatagramSocket(int port, InetAddress laddr) throws SocketException {
        this(new InetSocketAddress(laddr, port));
    }

    private void checkOldImpl() {
        if (impl == null)
            return;
        // DatagramSocketImpl.peekdata() 是一个受保护的方法，因此我们需要使用
        // getDeclaredMethod，因此我们需要访问成员的权限
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Void>() {
                    public Void run() throws NoSuchMethodException {
                        Class<?>[] cl = new Class<?>[1];
                        cl[0] = DatagramPacket.class;
                        impl.getClass().getDeclaredMethod("peekData", cl);
                        return null;
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            oldImpl = true;
        }
    }

    static Class<?> implClass = null;

    void createImpl() throws SocketException {
        if (impl == null) {
            if (factory != null) {
                impl = factory.createDatagramSocketImpl();
                checkOldImpl();
            } else {
                boolean isMulticast = (this instanceof MulticastSocket) ? true : false;
                impl = DefaultDatagramSocketImplFactory.createDatagramSocketImpl(isMulticast);

                checkOldImpl();
            }
        }
        // 创建一个 UDP 套接字
        impl.create();
        impl.setDatagramSocket(this);
        created = true;
    }

    /**
     * 获取与此套接字关联的 {@code DatagramSocketImpl}，必要时创建它。
     *
     * @return 与此 DatagramSocket 关联的 {@code DatagramSocketImpl}
     * @throws SocketException 如果创建失败。
     * @since 1.4
     */
    DatagramSocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl();
        return impl;
    }

    /**
     * 将此 DatagramSocket 绑定到特定的地址和端口。
     * <p>
     * 如果地址为 {@code null}，则系统将选择一个临时端口和一个有效的本地地址来绑定套接字。
     * <p>
     * @param   addr 要绑定的地址和端口。
     * @throws  SocketException 如果绑定过程中发生任何错误，或套接字已绑定。
     * @throws  SecurityException  如果存在安全经理，且其 {@code checkListen} 方法不允许操作。
     * @throws IllegalArgumentException 如果 addr 是此套接字不支持的 SocketAddress 子类。
     * @since 1.4
     */
    public synchronized void bind(SocketAddress addr) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (isBound())
            throw new SocketException("already bound");
        if (addr == null)
            addr = new InetSocketAddress(0);
        if (!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type!");
        InetSocketAddress epoint = (InetSocketAddress) addr;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        InetAddress iaddr = epoint.getAddress();
        int port = epoint.getPort();
        checkAddress(iaddr, "bind");
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkListen(port);
        }
        try {
            getImpl().bind(port, iaddr);
        } catch (SocketException e) {
            getImpl().close();
            throw e;
        }
        bound = true;
    }


void checkAddress (InetAddress addr, String op) {
    if (addr == null) {
        return;
    }
    if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
        throw new IllegalArgumentException(op + ": invalid address type");
    }
}

/**
 * 将套接字连接到远程地址。当套接字连接到远程地址时，数据包只能发送到或从该地址接收。默认情况下，数据报套接字未连接。
 *
 * <p>如果套接字连接的远程目标不存在或无法到达，并且已收到该地址的ICMP目标不可达数据包，则后续的发送或接收调用可能会抛出PortUnreachableException。请注意，不保证会抛出该异常。
 *
 * <p>如果安装了安全管理器，则会调用它来检查对远程地址的访问。具体来说，如果给定的{@code address}是{@link InetAddress#isMulticastAddress 多播地址}，
 * 则会调用安全管理器的{@link java.lang.SecurityManager#checkMulticast(InetAddress) checkMulticast}方法，传入给定的{@code address}。
 * 否则，将调用安全管理器的{@link java.lang.SecurityManager#checkConnect(String,int) checkConnect}和
 * {@link java.lang.SecurityManager#checkAccept checkAccept}方法，传入给定的{@code address}和{@code port}，以验证是否允许发送和接收数据报。
 *
 * <p>当套接字连接时，{@link #receive receive}和{@link #send send} <b>不会对传入和传出的数据包执行任何安全检查</b>，
 * 除了匹配数据包和套接字的地址和端口。在发送操作中，如果数据包的地址已设置且数据包的地址与套接字的地址不匹配，则将抛出{@code IllegalArgumentException}。
 * 连接到多播地址的套接字只能用于发送数据包。
 *
 * @param address 套接字的远程地址
 *
 * @param port 套接字的远程端口
 *
 * @throws IllegalArgumentException
 *         如果地址为null，或端口超出范围
 *
 * @throws SecurityException
 *         如果安装了安全管理器且不允许访问给定的远程地址
 *
 * @see #disconnect
 */
public void connect(InetAddress address, int port) {
    try {
        connectInternal(address, port);
    } catch (SocketException se) {
        throw new Error("connect failed", se);
    }
}

/**
 * 将此套接字连接到远程套接字地址（IP地址+端口号）。
 *
 * <p>如果给定的是{@link InetSocketAddress InetSocketAddress}，此方法的行为就像调用
 * {@link #connect(InetAddress,int) connect(InetAddress,int)}，传入给定的套接字地址的IP地址和端口号。
 *
 * @param   addr    远程地址
 *
 * @throws  SocketException
 *          如果连接失败
 *
 * @throws IllegalArgumentException
 *         如果{@code addr}为{@code null}，或{@code addr}是此套接字不支持的SocketAddress子类
 *
 * @throws SecurityException
 *         如果安装了安全管理器且不允许访问给定的远程地址
 *
 * @since 1.4
 */
public void connect(SocketAddress addr) throws SocketException {
    if (addr == null)
        throw new IllegalArgumentException("Address can't be null");
    if (!(addr instanceof InetSocketAddress))
        throw new IllegalArgumentException("Unsupported address type");
    InetSocketAddress epoint = (InetSocketAddress) addr;
    if (epoint.isUnresolved())
        throw new SocketException("Unresolved address");
    connectInternal(epoint.getAddress(), epoint.getPort());
}

/**
 * 断开套接字连接。如果套接字已关闭或未连接，则此方法无效。
 *
 * @see #connect
 */
public void disconnect() {
    synchronized (this) {
        if (isClosed())
            return;
        if (connectState == ST_CONNECTED) {
            impl.disconnect();
        }
        connectedAddress = null;
        connectedPort = -1;
        connectState = ST_NOT_CONNECTED;
        explicitFilter = false;
    }
}

/**
 * 返回套接字的绑定状态。
 * <p>
 * 如果套接字在关闭之前已绑定，则此方法在套接字关闭后将继续返回{@code true}。
 *
 * @return 如果套接字成功绑定到地址，则返回true
 * @since 1.4
 */
public boolean isBound() {
    return bound;
}

/**
 * 返回套接字的连接状态。
 * <p>
 * 如果套接字在关闭之前已连接，则此方法在套接字关闭后将继续返回{@code true}。
 *
 * @return 如果套接字成功连接到服务器，则返回true
 * @since 1.4
 */
public boolean isConnected() {
    return connectState != ST_NOT_CONNECTED;
}

/**
 * 返回此套接字连接的地址。如果套接字未连接，则返回{@code null}。
 * <p>
 * 如果套接字在关闭之前已连接，则此方法在套接字关闭后将继续返回连接的地址。
 *
 * @return 此套接字连接的地址
 */
public InetAddress getInetAddress() {
    return connectedAddress;
}

/**
 * 返回此套接字连接的端口号。如果套接字未连接，则返回{@code -1}。
 * <p>
 * 如果套接字在关闭之前已连接，则此方法在套接字关闭后将继续返回连接的端口号。
 *
 * @return 此套接字连接的端口号
 */
public int getPort() {
    return connectedPort;
}

                /**
     * 返回此套接字连接到的端点的地址，如果未连接，则返回
     * {@code null}。
     * <p>
     * 如果套接字在关闭前已连接，
     * 则此方法在套接字关闭后将继续返回已连接的地址。
     *
     * @return 一个表示此套接字远程端点的 {@code SocketAddress}，
     *         如果尚未连接，则返回 {@code null}。
     * @see #getInetAddress()
     * @see #getPort()
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
     *
     * @return 一个表示此套接字本地端点的 {@code SocketAddress}，
     *         如果已关闭或尚未绑定，则返回 {@code null}。
     * @see #getLocalAddress()
     * @see #getLocalPort()
     * @see #bind(SocketAddress)
     * @since 1.4
     */

    public SocketAddress getLocalSocketAddress() {
        if (isClosed())
            return null;
        if (!isBound())
            return null;
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    /**
     * 从此套接字发送一个数据报包。该
     * {@code DatagramPacket} 包含指示要发送的数据、其长度、远程主机的 IP 地址
     * 和远程主机上的端口号的信息。
     *
     * <p>如果存在安全经理，并且套接字当前未连接到远程地址，此方法首先执行一些
     * 安全检查。首先，如果 {@code p.getAddress().isMulticastAddress()}
     * 为 true，此方法将调用安全经理的
     * {@code checkMulticast} 方法
     * 将 {@code p.getAddress()} 作为其参数。
     * 如果该表达式的评估结果为 false，
     * 此方法将调用安全经理的
     * {@code checkConnect} 方法，参数为
     * {@code p.getAddress().getHostAddress()} 和
     * {@code p.getPort()}。每次调用安全经理方法
     * 如果操作不允许，可能会导致 SecurityException。
     *
     * @param      p   要发送的 {@code DatagramPacket}。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @exception  SecurityException  如果存在安全经理，且其
     *             {@code checkMulticast} 或 {@code checkConnect}
     *             方法不允许发送。
     * @exception  PortUnreachableException 如果套接字连接到
     *             当前不可达的目的地，可能会抛出此异常。请注意，不能保证会抛出此异常。
     * @exception  java.nio.channels.IllegalBlockingModeException
     *             如果此套接字有相关联的通道，
     *             且通道处于非阻塞模式。
     * @exception  IllegalArgumentException 如果套接字已连接，
     *             且连接地址和数据包地址不同。
     *
     * @see        java.net.DatagramPacket
     * @see        SecurityManager#checkMulticast(InetAddress)
     * @see        SecurityManager#checkConnect
     * @revised 1.4
     * @spec JSR-51
     */
    public void send(DatagramPacket p) throws IOException  {
        InetAddress packetAddress = null;
        synchronized (p) {
            if (isClosed())
                throw new SocketException("Socket is closed");
            checkAddress (p.getAddress(), "send");
            if (connectState == ST_NOT_CONNECTED) {
                // 每次发送时检查地址是否符合安全经理的要求。
                SecurityManager security = System.getSecurityManager();

                // 同步数据报包的原因是不希望小程序在尝试发送数据包时
                // 更改地址，例如在安全检查之后但在发送之前。
                if (security != null) {
                    if (p.getAddress().isMulticastAddress()) {
                        security.checkMulticast(p.getAddress());
                    } else {
                        security.checkConnect(p.getAddress().getHostAddress(),
                                              p.getPort());
                    }
                }
            } else {
                // 已连接
                packetAddress = p.getAddress();
                if (packetAddress == null) {
                    p.setAddress(connectedAddress);
                    p.setPort(connectedPort);
                } else if ((!packetAddress.equals(connectedAddress)) ||
                           p.getPort() != connectedPort) {
                    throw new IllegalArgumentException("connected address " +
                                                       "and packet address" +
                                                       " differ");
                }
            }
            // 检查套接字是否已绑定
            if (!isBound())
                bind(new InetSocketAddress(0));
            // 调用方法发送
            getImpl().send(p);
        }
    }

    /**
     * 从此套接字接收一个数据报包。当此方法
     * 返回时，{@code DatagramPacket} 的缓冲区将被
     * 接收到的数据填充。数据报包还包含发送者的
     * IP 地址和发送者机器上的端口号。
     * <p>
     * 此方法会阻塞，直到接收到数据报。数据报包对象的
     * {@code length} 字段包含接收到的消息的长度。如果消息长度超过
     * 数据报包的长度，消息将被截断。
     * <p>
     * 如果存在安全经理，如果安全经理的 {@code checkAccept} 方法
     * 不允许，则无法接收数据包。
     *
     * @param      p   用于放置传入数据的 {@code DatagramPacket}。
     * @exception  IOException  如果发生 I/O 错误。
     * @exception  SocketTimeoutException  如果之前调用了 setSoTimeout
     *                 并且超时已过期。
     * @exception  PortUnreachableException 如果套接字连接到
     *             当前不可达的目的地，可能会抛出此异常。请注意，不能保证会抛出此异常。
     * @exception  java.nio.channels.IllegalBlockingModeException
     *             如果此套接字有相关联的通道，
     *             且通道处于非阻塞模式。
     * @see        java.net.DatagramPacket
     * @see        java.net.DatagramSocket
     * @revised 1.4
     * @spec JSR-51
     */
    public synchronized void receive(DatagramPacket p) throws IOException {
        synchronized (p) {
            if (!isBound())
                bind(new InetSocketAddress(0));
            if (connectState == ST_NOT_CONNECTED) {
                // 每次接收前检查地址是否符合安全经理的要求。
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    while(true) {
                        String peekAd = null;
                        int peekPort = 0;
                        // 查看数据包来自哪里。
                        if (!oldImpl) {
                            // 我们可以使用新的 peekData() API
                            DatagramPacket peekPacket = new DatagramPacket(new byte[1], 1);
                            peekPort = getImpl().peekData(peekPacket);
                            peekAd = peekPacket.getAddress().getHostAddress();
                        } else {
                            InetAddress adr = new InetAddress();
                            peekPort = getImpl().peek(adr);
                            peekAd = adr.getHostAddress();
                        }
                        try {
                            security.checkAccept(peekAd, peekPort);
                            // 安全检查成功 - 现在中断
                            // 并接收数据包。
                            break;
                        } catch (SecurityException se) {
                            // 通过在临时缓冲区中消费
                            // 丢弃违规数据包。
                            DatagramPacket tmp = new DatagramPacket(new byte[1], 1);
                            getImpl().receive(tmp);


                                        // 静默丢弃违规数据包
                            // 并继续：未知/恶意
                            // 网络上的实体不应该导致
                            // 运行时抛出安全异常并
                            // 通过发送随机
                            // 数据报数据包来干扰小程序。
                            continue;
                        }
                    } // while 结束
                }
            }
            DatagramPacket tmp = null;
            if ((connectState == ST_CONNECTED_NO_IMPL) || explicitFilter) {
                // 由于本地实现不支持连接或通过实现连接失败，或者 .. "explicitFilter" 可能在通过实现连接的套接字上设置，
                // 在一段时间内，来自其他来源的数据包可能会在套接字上排队。
                boolean stop = false;
                while (!stop) {
                    InetAddress peekAddress = null;
                    int peekPort = -1;
                    // 查看数据包以查看它来自哪里。
                    if (!oldImpl) {
                        // 我们可以使用新的 peekData() API
                        DatagramPacket peekPacket = new DatagramPacket(new byte[1], 1);
                        peekPort = getImpl().peekData(peekPacket);
                        peekAddress = peekPacket.getAddress();
                    } else {
                        // 此 API 仅适用于 IPv4
                        peekAddress = new InetAddress();
                        peekPort = getImpl().peek(peekAddress);
                    }
                    if ((!connectedAddress.equals(peekAddress)) ||
                        (connectedPort != peekPort)) {
                        // 丢弃数据包并静默继续
                        tmp = new DatagramPacket(
                                                new byte[1024], 1024);
                        getImpl().receive(tmp);
                        if (explicitFilter) {
                            if (checkFiltering(tmp)) {
                                stop = true;
                            }
                        }
                    } else {
                        stop = true;
                    }
                }
            }
            // 如果安全检查成功，或者数据报已连接，则接收数据包
            getImpl().receive(p);
            if (explicitFilter && tmp == null) {
                // 数据包未被过滤，此处进行处理
                checkFiltering(p);
            }
        }
    }

    private boolean checkFiltering(DatagramPacket p) throws SocketException {
        bytesLeftToFilter -= p.getLength();
        if (bytesLeftToFilter <= 0 || getImpl().dataAvailable() <= 0) {
            explicitFilter = false;
            return true;
        }
        return false;
    }

    /**
     * 获取套接字绑定的本地地址。
     *
     * <p>如果有安全经理，首先调用其
     * {@code checkConnect} 方法
     * 用主机地址和 {@code -1}
     * 作为参数来检查操作是否允许。
     *
     * @see SecurityManager#checkConnect
     * @return  套接字绑定的本地地址，
     *          如果套接字已关闭，则返回 {@code null}，
     *          或者如果套接字未绑定，或者安全经理 {@code checkConnect}
     *          方法不允许操作，则返回表示
     *          {@link InetAddress#isAnyLocalAddress 通配符}
     *          地址的 {@code InetAddress}
     * @since   1.1
     */
    public InetAddress getLocalAddress() {
        if (isClosed())
            return null;
        InetAddress in = null;
        try {
            in = (InetAddress) getImpl().getOption(SocketOptions.SO_BINDADDR);
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkConnect(in.getHostAddress(), -1);
            }
        } catch (Exception e) {
            in = InetAddress.anyLocalAddress(); // "0.0.0.0"
        }
        return in;
    }

    /**
     * 返回此套接字在本地主机上绑定的端口号。
     *
     * @return  本地主机上此套接字绑定的端口号，
                如果套接字已关闭，则返回 {@code -1}，
                或者如果尚未绑定，则返回 {@code 0}。
     */
    public int getLocalPort() {
        if (isClosed())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (Exception e) {
            return 0;
        }
    }

    /** 启用/禁用指定超时时间（以毫秒为单位）的 SO_TIMEOUT。设置此选项为非零超时，
     *  对此 DatagramSocket 的 receive() 调用
     *  将仅阻塞此时间段。如果超时到期，
     *  将引发 <B>java.net.SocketTimeoutException</B>，尽管
     *  DatagramSocket 仍然有效。必须在进入阻塞操作之前启用此选项才能生效。超时必须 {@code > 0}。
     *  超时为零表示无限超时。
     *
     * @param timeout 指定的超时时间（以毫秒为单位）。
     * @throws SocketException 如果底层协议（如 UDP 错误）出错。
     * @since   JDK1.1
     * @see #getSoTimeout()
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    /**
     * 检索 SO_TIMEOUT 设置。0 返回值表示该选项已禁用（即无限超时）。
     *
     * @return SO_TIMEOUT 的设置
     * @throws SocketException 如果底层协议（如 UDP 错误）出错。
     * @since   JDK1.1
     * @see #setSoTimeout(int)
     */
    public synchronized int getSoTimeout() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (getImpl() == null)
            return 0;
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* 额外的类型安全 */
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }


                /**
     * 为这个 {@code DatagramSocket} 设置 SO_SNDBUF 选项为指定的值。SO_SNDBUF 选项用于网络实现作为调整底层
     * 网络 I/O 缓冲区大小的提示。SO_SNDBUF 设置也可能被网络实现用于确定可以通过此套接字发送的数据包的最大大小。
     * <p>
     * 由于 SO_SNDBUF 是一个提示，应用程序如果想要验证缓冲区的大小，应该调用 {@link #getSendBufferSize()}。
     * <p>
     * 增加缓冲区大小可能允许在网络实现中排队多个出站数据包，当发送速率很高时。
     * <p>
     * 注意：如果使用 {@link #send(DatagramPacket)} 发送一个大于 SO_SNDBUF 设置的 {@code DatagramPacket}，
     * 则具体实现会决定该数据包是被发送还是被丢弃。
     *
     * @param size 设置发送缓冲区大小的值。此值必须大于 0。
     *
     * @exception SocketException 如果底层协议（如 UDP 错误）出错。
     * @exception IllegalArgumentException 如果值为 0 或为负数。
     * @see #getSendBufferSize()
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
     * 获取此 {@code DatagramSocket} 的 SO_SNDBUF 选项的值，即平台为此 {@code DatagramSocket} 输出使用的缓冲区大小。
     *
     * @return 此 {@code DatagramSocket} 的 SO_SNDBUF 选项的值
     * @exception SocketException 如果底层协议（如 UDP 错误）出错。
     * @see #setSendBufferSize
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
     * 为这个 {@code DatagramSocket} 设置 SO_RCVBUF 选项为指定的值。SO_RCVBUF 选项用于网络实现作为调整底层
     * 网络 I/O 缓冲区大小的提示。SO_RCVBUF 设置也可能被网络实现用于确定可以通过此套接字接收的数据包的最大大小。
     * <p>
     * 由于 SO_RCVBUF 是一个提示，应用程序如果想要验证缓冲区的大小，应该调用 {@link #getReceiveBufferSize()}。
     * <p>
     * 增加 SO_RCVBUF 可能允许网络实现缓冲多个数据包，当数据包到达速度比使用 {@link #receive(DatagramPacket)} 接收的速度快时。
     * <p>
     * 注意：具体实现决定是否可以接收大于 SO_RCVBUF 的数据包。
     *
     * @param size 设置接收缓冲区大小的值。此值必须大于 0。
     *
     * @exception SocketException 如果底层协议（如 UDP 错误）出错。
     * @exception IllegalArgumentException 如果值为 0 或为负数。
     * @see #getReceiveBufferSize()
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
     * 获取此 {@code DatagramSocket} 的 SO_RCVBUF 选项的值，即平台为此 {@code DatagramSocket} 输入使用的缓冲区大小。
     *
     * @return 此 {@code DatagramSocket} 的 SO_RCVBUF 选项的值
     * @exception SocketException 如果底层协议（如 UDP 错误）出错。
     * @see #setReceiveBufferSize(int)
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
     * 启用/禁用 SO_REUSEADDR 套接字选项。
     * <p>
     * 对于 UDP 套接字，可能需要将多个套接字绑定到相同的套接字地址。这通常是为了接收多播数据包
     * （参见 {@link java.net.MulticastSocket}）。如果在使用 {@link #bind(SocketAddress)} 绑定套接字之前启用了
     * {@code SO_REUSEADDR} 套接字选项，则允许将多个套接字绑定到相同的套接字地址。
     * <p>
     * 注意：此功能不是所有现有平台都支持的，因此具体实现可能会忽略此选项或不会忽略。但是，如果不支持，
     * 则 {@link #getReuseAddress()} 始终返回 {@code false}。
     * <p>
     * 当创建一个 {@code DatagramSocket} 时，初始设置的 {@code SO_REUSEADDR} 是禁用的。
     * <p>
     * 在套接字绑定后（参见 {@link #isBound()}）启用或禁用 {@code SO_REUSEADDR} 的行为未定义。
     *
     * @param on  是否启用或禁用
     * @exception SocketException 如果启用或禁用 {@code SO_RESUEADDR} 套接字选项时出错，或套接字已关闭。
     * @since 1.4
     * @see #getReuseAddress()
     * @see #bind(SocketAddress)
     * @see #isBound()
     * @see #isClosed()
     */
    public synchronized void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        // 为了与较旧的 DatagramSocketImpl 兼容，使用 Integer 而不是 Boolean
        if (oldImpl)
            getImpl().setOption(SocketOptions.SO_REUSEADDR, new Integer(on?-1:0));
        else
            getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }

                /**
     * 测试是否启用了 SO_REUSEADDR。
     *
     * @return 一个 {@code boolean} 值，指示是否启用了 SO_REUSEADDR。
     * @exception SocketException 如果底层协议（如 UDP 错误）发生错误。
     * @since   1.4
     * @see #setReuseAddress(boolean)
     */
    public synchronized boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_REUSEADDR);
        return ((Boolean)o).booleanValue();
    }

    /**
     * 启用/禁用 SO_BROADCAST。
     *
     * <p> 一些操作系统可能需要以实现特定的权限启动 Java 虚拟机，以启用此选项或发送广播数据报。
     *
     * @param  on
     *         是否开启广播。
     *
     * @throws  SocketException
     *          如果底层协议（如 UDP 错误）发生错误。
     *
     * @since 1.4
     * @see #getBroadcast()
     */
    public synchronized void setBroadcast(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_BROADCAST, Boolean.valueOf(on));
    }

    /**
     * 测试是否启用了 SO_BROADCAST。
     * @return 一个 {@code boolean} 值，指示是否启用了 SO_BROADCAST。
     * @exception SocketException 如果底层协议（如 UDP 错误）发生错误。
     * @since 1.4
     * @see #setBroadcast(boolean)
     */
    public synchronized boolean getBroadcast() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean)(getImpl().getOption(SocketOptions.SO_BROADCAST))).booleanValue();
    }

    /**
     * 设置从这个 DatagramSocket 发送的数据报的 IP 数据报头中的流量类别或服务类型字节。
     * 由于底层网络实现可能忽略此值，应用程序应将其视为提示。
     *
     * <P> tc <B>必须</B> 在范围 {@code 0 <= tc <= 255} 内，否则将抛出 IllegalArgumentException。
     * <p>注意：
     * <p>对于 Internet 协议 v4，该值由一个 {@code integer} 组成，其最低有效 8 位表示
     * 由套接字发送的 IP 数据包中的 TOS 字节。
     * RFC 1349 定义了 TOS 值如下：
     *
     * <UL>
     * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
     * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
     * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
     * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
     * </UL>
     * 最后一个最低位总是被忽略，因为这对应于 MBZ（必须为零）位。
     * <p>
     * 设置优先级字段中的位可能导致 SocketException，表示该操作不被允许。
     * <p>
     * 对于 Internet 协议 v6，{@code tc} 是将被放置到 IP 头的 sin6_flowinfo 字段中的值。
     *
     * @param tc        一个 {@code int} 值，用于位集。
     * @throws SocketException 如果设置流量类别或服务类型时发生错误
     * @since 1.4
     * @see #getTrafficClass
     */
    public synchronized void setTrafficClass(int tc) throws SocketException {
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
     * 获取从这个 DatagramSocket 发送的数据包的 IP 数据报头中的流量类别或服务类型。
     * <p>
     * 由于底层网络实现可能忽略使用 {@link #setTrafficClass(int)} 设置的流量类别或服务类型，
     * 此方法可能返回与之前使用 {@link #setTrafficClass(int)} 方法在此 DatagramSocket 上设置的值不同的值。
     *
     * @return 已设置的流量类别或服务类型
     * @throws SocketException 如果获取流量类别或服务类型值时发生错误。
     * @since 1.4
     * @see #setTrafficClass(int)
     */
    public synchronized int getTrafficClass() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Integer)(getImpl().getOption(SocketOptions.IP_TOS))).intValue();
    }

    /**
     * 关闭此数据报套接字。
     * <p>
     * 当前在 {@link #receive} 上阻塞的任何线程将抛出 {@link SocketException}。
     *
     * <p> 如果此套接字有关联的通道，则该通道也将被关闭。
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() {
        synchronized(closeLock) {
            if (isClosed())
                return;
            impl.close();
            closed = true;
        }
    }

    /**
     * 返回套接字是否已关闭。
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
     * 返回与此数据报套接字关联的唯一 {@link java.nio.channels.DatagramChannel} 对象，如果有的话。
     *
     * <p> 仅当通道本身是通过 {@link java.nio.channels.DatagramChannel#open DatagramChannel.open} 方法创建时，
     * 数据报套接字才会有一个通道。
     *
     * @return 与此数据报套接字关联的数据报通道，如果此套接字不是为通道创建的，则返回 {@code null}。
     *
     * @since 1.4
     * @spec JSR-51
     */
    public DatagramChannel getChannel() {
        return null;
    }

                /**
     * 用户定义的所有数据报套接字的工厂。
     */
    static DatagramSocketImplFactory factory;

    /**
     * 为应用程序设置数据报套接字实现工厂。工厂只能指定一次。
     * <p>
     * 当应用程序创建新的数据报套接字时，将调用套接字实现工厂的 {@code createDatagramSocketImpl} 方法来创建实际的数据报套接字实现。
     * <p>
     * 如果工厂已经设置，传递 {@code null} 给该方法将不起作用。
     *
     * <p>如果存在安全管理器，此方法首先调用安全管理器的 {@code checkSetFactory} 方法
     * 以确保操作被允许。这可能会导致 SecurityException。
     *
     * @param      fac   所需的工厂。
     * @exception  IOException  如果在设置数据报套接字工厂时发生 I/O 错误。
     * @exception  SocketException  如果工厂已定义。
     * @exception  SecurityException  如果存在安全管理器且其
     *             {@code checkSetFactory} 方法不允许该操作。
     * @see
     java.net.DatagramSocketImplFactory#createDatagramSocketImpl()
     * @see       SecurityManager#checkSetFactory
     * @since 1.3
     */
    public static synchronized void
    setDatagramSocketImplFactory(DatagramSocketImplFactory fac)
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
}
