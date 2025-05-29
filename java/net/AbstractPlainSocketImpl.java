
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import sun.net.ExtendedSocketOptions;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;

/**
 * 默认的 Socket 实现。此实现不执行任何安全检查。
 * 注意，此类应<b>不</b>是公共的。
 *
 * @author  Steven B. Byrne
 */
abstract class AbstractPlainSocketImpl extends SocketImpl
{
    /* SO_TIMEOUT 的实例变量 */
    int timeout;   // 超时时间，以毫秒为单位
    // 交通类别
    private int trafficClass;

    private boolean shut_rd = false;
    private boolean shut_wr = false;

    private SocketInputStream socketInputStream = null;
    private SocketOutputStream socketOutputStream = null;

    /* 使用 FileDescriptor 的线程数 */
    protected int fdUseCount = 0;

    /* 在增加/减少 fdUseCount 时的锁 */
    protected final Object fdLock = new Object();

    /* 表示文件描述符上有待关闭的状态 */
    protected boolean closePending = false;

    /* 表示连接重置状态 */
    private int CONNECTION_NOT_RESET = 0;
    private int CONNECTION_RESET_PENDING = 1;
    private int CONNECTION_RESET = 2;
    private int resetState;
    private final Object resetLock = new Object();

   /* 表示此 Socket 是否为流（TCP）套接字或非流（UDP）套接字
    */
    protected boolean stream;

    /* 表示此实例是否为服务器 */
    final boolean isServer;

    /**
     * 将网络库加载到运行时。
     */
    static {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });
    }

    AbstractPlainSocketImpl(boolean isServer) {
        this.isServer = isServer;
    }

    /**
     * 创建一个布尔值指定此套接字是否为流套接字（true）或未连接的 UDP 套接字（false）。
     */
    protected synchronized void create(boolean stream) throws IOException {
        this.stream = stream;
        if (!stream) {
            ResourceManager.beforeUdpCreate();
            // 只有在确定可以创建套接字后才创建 fd
            fd = new FileDescriptor();
            try {
                socketCreate(false);
            } catch (IOException ioe) {
                ResourceManager.afterUdpClose();
                fd = null;
                throw ioe;
            }
        } else {
            fd = new FileDescriptor();
            socketCreate(true);
        }
        if (socket != null)
            socket.setCreated();
        if (serverSocket != null)
            serverSocket.setCreated();
    }

    /**
     * 创建一个套接字并将其连接到指定主机上的指定端口。
     * @param host 指定的主机
     * @param port 指定的端口
     */
    protected void connect(String host, int port)
        throws UnknownHostException, IOException
    {
        boolean connected = false;
        try {
            InetAddress address = InetAddress.getByName(host);
            this.port = port;
            this.address = address;

            connectToAddress(address, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                    /* 什么都不做。如果 connect 抛出异常，则
                       它将传递到调用堆栈 */
                }
            }
        }
    }

    /**
     * 创建一个套接字并将其连接到指定地址上的指定端口。
     * @param address 地址
     * @param port 指定的端口
     */
    protected void connect(InetAddress address, int port) throws IOException {
        this.port = port;
        this.address = address;

        try {
            connectToAddress(address, port, timeout);
            return;
        } catch (IOException e) {
            // 一切失败
            close();
            throw e;
        }
    }

    /**
     * 创建一个套接字并将其连接到指定地址上的指定端口。
     * @param address 地址
     * @param timeout 超时值，以毫秒为单位，或零表示无超时。
     * @throws IOException 如果连接失败
     * @throws  IllegalArgumentException 如果地址为 null 或是此套接字不支持的 SocketAddress 子类
     * @since 1.4
     */
    protected void connect(SocketAddress address, int timeout)
            throws IOException {
        boolean connected = false;
        try {
            if (address == null || !(address instanceof InetSocketAddress))
                throw new IllegalArgumentException("不支持的地址类型");
            InetSocketAddress addr = (InetSocketAddress) address;
            if (addr.isUnresolved())
                throw new UnknownHostException(addr.getHostName());
            this.port = addr.getPort();
            this.address = addr.getAddress();

            connectToAddress(this.address, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                    /* 什么都不做。如果 connect 抛出异常，则
                       它将传递到调用堆栈 */
                }
            }
        }
    }

    private void connectToAddress(InetAddress address, int port, int timeout) throws IOException {
        if (address.isAnyLocalAddress()) {
            doConnect(InetAddress.getLocalHost(), port, timeout);
        } else {
            doConnect(address, port, timeout);
        }
    }


public void setOption(int opt, Object val) throws SocketException {
    if (isClosedOrPending()) {
        throw new SocketException("Socket Closed");
    }
    boolean on = true;
    switch (opt) {
        /* 检查类型安全，然后再调用本地方法。这些检查应该永远不会失败，
         * 因为只有 java.Socket* 可以访问 PlainSocketImpl.setOption()。
         */
    case SO_LINGER:
        if (val == null || (!(val instanceof Integer) && !(val instanceof Boolean)))
            throw new SocketException("Bad parameter for option");
        if (val instanceof Boolean) {
            /* 仅在禁用时为 true - 启用时应为 Integer */
            on = false;
        }
        break;
    case SO_TIMEOUT:
        if (val == null || (!(val instanceof Integer)))
            throw new SocketException("Bad parameter for SO_TIMEOUT");
        int tmp = ((Integer) val).intValue();
        if (tmp < 0)
            throw new IllegalArgumentException("timeout < 0");
        timeout = tmp;
        break;
    case IP_TOS:
         if (val == null || !(val instanceof Integer)) {
             throw new SocketException("bad argument for IP_TOS");
         }
         trafficClass = ((Integer)val).intValue();
         break;
    case SO_BINDADDR:
        throw new SocketException("Cannot re-bind socket");
    case TCP_NODELAY:
        if (val == null || !(val instanceof Boolean))
            throw new SocketException("bad parameter for TCP_NODELAY");
        on = ((Boolean)val).booleanValue();
        break;
    case SO_SNDBUF:
    case SO_RCVBUF:
        if (val == null || !(val instanceof Integer) ||
            !(((Integer)val).intValue() > 0)) {
            throw new SocketException("bad parameter for SO_SNDBUF " +
                                      "or SO_RCVBUF");
        }
        break;
    case SO_KEEPALIVE:
        if (val == null || !(val instanceof Boolean))
            throw new SocketException("bad parameter for SO_KEEPALIVE");
        on = ((Boolean)val).booleanValue();
        break;
    case SO_OOBINLINE:
        if (val == null || !(val instanceof Boolean))
            throw new SocketException("bad parameter for SO_OOBINLINE");
        on = ((Boolean)val).booleanValue();
        break;
    case SO_REUSEADDR:
        if (val == null || !(val instanceof Boolean))
            throw new SocketException("bad parameter for SO_REUSEADDR");
        on = ((Boolean)val).booleanValue();
        break;
    default:
        throw new SocketException("unrecognized TCP option: " + opt);
    }
    socketSetOption(opt, on, val);
}

public Object getOption(int opt) throws SocketException {
    if (isClosedOrPending()) {
        throw new SocketException("Socket Closed");
    }
    if (opt == SO_TIMEOUT) {
        return new Integer(timeout);
    }
    int ret = 0;
    /*
     * 本地 socketGetOption() 方法了解 3 个选项。
     * 它返回的 32 位值将根据我们询问的内容进行解释。返回 -1 表示它理解该选项但已关闭。
     * 如果 "opt" 不是它理解的选项，它将引发 SocketException。
     */

    switch (opt) {
    case TCP_NODELAY:
        ret = socketGetOption(opt, null);
        return Boolean.valueOf(ret != -1);
    case SO_OOBINLINE:
        ret = socketGetOption(opt, null);
        return Boolean.valueOf(ret != -1);
    case SO_LINGER:
        ret = socketGetOption(opt, null);
        return (ret == -1) ? Boolean.FALSE: (Object)(new Integer(ret));
    case SO_REUSEADDR:
        ret = socketGetOption(opt, null);
        return Boolean.valueOf(ret != -1);
    case SO_BINDADDR:
        InetAddressContainer in = new InetAddressContainer();
        ret = socketGetOption(opt, in);
        return in.addr;
    case SO_SNDBUF:
    case SO_RCVBUF:
        ret = socketGetOption(opt, null);
        return new Integer(ret);
    case IP_TOS:
        try {
            ret = socketGetOption(opt, null);
            if (ret == -1) { // ipv6 tos
                return trafficClass;
            } else {
                return ret;
            }
        } catch (SocketException se) {
            // TODO - 应该更努力地读取 TOS 或 TCLASS
            return trafficClass; // ipv6 tos
        }
    case SO_KEEPALIVE:
        ret = socketGetOption(opt, null);
        return Boolean.valueOf(ret != -1);
    // 不应该到达这里
    default:
        return null;
    }
}

static final ExtendedSocketOptions extendedOptions =
        ExtendedSocketOptions.getInstance();

private static final Set<SocketOption<?>> clientSocketOptions = clientSocketOptions();
private static final Set<SocketOption<?>> serverSocketOptions = serverSocketOptions();

private static Set<SocketOption<?>> clientSocketOptions() {
    HashSet<SocketOption<?>> options = new HashSet<>();
    options.add(StandardSocketOptions.SO_KEEPALIVE);
    options.add(StandardSocketOptions.SO_SNDBUF);
    options.add(StandardSocketOptions.SO_RCVBUF);
    options.add(StandardSocketOptions.SO_REUSEADDR);
    options.add(StandardSocketOptions.SO_LINGER);
    options.add(StandardSocketOptions.IP_TOS);
    options.add(StandardSocketOptions.TCP_NODELAY);
    options.addAll(ExtendedSocketOptions.clientSocketOptions());
    return Collections.unmodifiableSet(options);
}

private static Set<SocketOption<?>> serverSocketOptions() {
    HashSet<SocketOption<?>> options = new HashSet<>();
    options.add(StandardSocketOptions.SO_RCVBUF);
    options.add(StandardSocketOptions.SO_REUSEADDR);
    options.add(StandardSocketOptions.IP_TOS);
    options.addAll(ExtendedSocketOptions.serverSocketOptions());
    return Collections.unmodifiableSet(options);
}


                protected Set<SocketOption<?>> supportedOptions() {
        if (isServer)
            return serverSocketOptions;
        else
            return clientSocketOptions;
    }

    @Override
    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");

        if (!name.type().isInstance(value))
            throw new IllegalArgumentException("Invalid value '" + value + "'");

        if (isClosedOrPending())
            throw new SocketException("Socket closed");

        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            setOption(SocketOptions.SO_KEEPALIVE, value);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            if (((Integer)value).intValue() < 0)
                throw new IllegalArgumentException("Invalid send buffer size:" + value);
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            if (((Integer)value).intValue() < 0)
                throw new IllegalArgumentException("Invalid recv buffer size:" + value);
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(SocketOptions.SO_REUSEADDR, value);
        } else if (name == StandardSocketOptions.SO_LINGER ) {
            if (((Integer)value).intValue() < 0)
                setOption(SocketOptions.SO_LINGER, false);
            else
                setOption(SocketOptions.SO_LINGER, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            int i = ((Integer)value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid IP_TOS value: " + value);
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            setOption(SocketOptions.TCP_NODELAY, value);
        } else if (extendedOptions.isOptionSupported(name)) {
            extendedOptions.setOption(fd, name, value);
        } else {
            throw new AssertionError("unknown option: " + name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");

        if (isClosedOrPending())
            throw new SocketException("Socket closed");

        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            return (T)getOption(SocketOptions.SO_KEEPALIVE);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            return (T)getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T)getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T)getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            Object value = getOption(SocketOptions.SO_LINGER);
            if (value instanceof Boolean) {
                assert ((Boolean)value).booleanValue() == false;
                value = -1;
            }
            return (T)value;
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T)getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            return (T)getOption(SocketOptions.TCP_NODELAY);
        } else if (extendedOptions.isOptionSupported(name)) {
            return (T) extendedOptions.getOption(fd, name);
        } else {
            throw new AssertionError("unknown option: " + name);
        }
    }

    /**
     * The workhorse of the connection operation.  Tries several times to
     * establish a connection to the given <host, port>.  If unsuccessful,
     * throws an IOException indicating what went wrong.
     */

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        synchronized (fdLock) {
            if (!closePending && (socket == null || !socket.isBound())) {
                NetHooks.beforeTcpConnect(fd, address, port);
            }
        }
        try {
            acquireFD();
            try {
                socketConnect(address, port, timeout);
                /* socket may have been closed during poll/select */
                synchronized (fdLock) {
                    if (closePending) {
                        throw new SocketException ("Socket closed");
                    }
                }
                // If we have a ref. to the Socket, then sets the flags
                // created, bound & connected to true.
                // This is normally done in Socket.connect() but some
                // subclasses of Socket may call impl.connect() directly!
                if (socket != null) {
                    socket.setBound();
                    socket.setConnected();
                }
            } finally {
                releaseFD();
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Binds the socket to the specified address of the specified local port.
     * @param address the address
     * @param lport the port
     */
    protected synchronized void bind(InetAddress address, int lport)
        throws IOException
    {
       synchronized (fdLock) {
            if (!closePending && (socket == null || !socket.isBound())) {
                NetHooks.beforeTcpBind(fd, address, lport);
            }
        }
        socketBind(address, lport);
        if (socket != null)
            socket.setBound();
        if (serverSocket != null)
            serverSocket.setBound();
    }

    /**
     * Listens, for a specified amount of time, for connections.
     * @param count the amount of time to listen for connections
     */
    protected synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

                /**
     * 接受连接。
     * @param s 连接
     */
    protected void accept(SocketImpl s) throws IOException {
        acquireFD();
        try {
            socketAccept(s);
        } finally {
            releaseFD();
        }
    }

    /**
     * 获取此套接字的 InputStream。
     */
    protected synchronized InputStream getInputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_rd)
                throw new IOException("Socket input is shutdown");
            if (socketInputStream == null)
                socketInputStream = new SocketInputStream(this);
        }
        return socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        socketInputStream = in;
    }

    /**
     * 获取此套接字的 OutputStream。
     */
    protected synchronized OutputStream getOutputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_wr)
                throw new IOException("Socket output is shutdown");
            if (socketOutputStream == null)
                socketOutputStream = new SocketOutputStream(this);
        }
        return socketOutputStream;
    }

    void setFileDescriptor(FileDescriptor fd) {
        this.fd = fd;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setLocalPort(int localport) {
        this.localport = localport;
    }

    /**
     * 返回可以不阻塞读取的字节数。
     */
    protected synchronized int available() throws IOException {
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        }

        /*
         * 如果连接已重置或关闭输入，则返回 0
         * 表示没有缓冲的字节。
         */
        if (isConnectionReset() || shut_rd) {
            return 0;
        }

        /*
         * 如果没有可用的字节并且我们之前被通知
         * 连接重置，则我们移动到重置状态。
         *
         * 如果我们被通知连接重置，则检查
         * 套接字上是否有缓冲的字节。
         */
        int n = 0;
        try {
            n = socketAvailable();
            if (n == 0 && isConnectionResetPending()) {
                setConnectionReset();
            }
        } catch (ConnectionResetException exc1) {
            setConnectionResetPending();
            try {
                n = socketAvailable();
                if (n == 0) {
                    setConnectionReset();
                }
            } catch (ConnectionResetException exc2) {
            }
        }
        return n;
    }

    /**
     * 关闭套接字。
     */
    protected void close() throws IOException {
        synchronized(fdLock) {
            if (fd != null) {
                if (!stream) {
                    ResourceManager.afterUdpClose();
                }
                if (fdUseCount == 0) {
                    if (closePending) {
                        return;
                    }
                    closePending = true;
                    /*
                     * 我们分两步关闭 FileDescriptor - 首先是“预关闭”，
                     * 关闭套接字但不释放底层文件描述符。此操作可能由于未传输的数据和长时间的
                     * 滞留间隔而耗时较长。一旦预关闭完成，我们执行实际的套接字关闭以释放 fd。
                     */
                    try {
                        socketPreClose();
                    } finally {
                        socketClose();
                    }
                    fd = null;
                    return;
                } else {
                    /*
                     * 如果一个线程已经获取了 fd 且关闭
                     * 不是待处理的，则使用延迟关闭。
                     * 同时减少 fdUseCount 以信号最后一个
                     * 释放 fd 的线程关闭它。
                     */
                    if (!closePending) {
                        closePending = true;
                        fdUseCount--;
                        socketPreClose();
                    }
                }
            }
        }
    }

    void reset() throws IOException {
        if (fd != null) {
            socketClose();
        }
        fd = null;
        super.reset();
    }


    /**
     * 关闭套接字连接的读取部分；
     */
    protected void shutdownInput() throws IOException {
      if (fd != null) {
          socketShutdown(SHUT_RD);
          if (socketInputStream != null) {
              socketInputStream.setEOF(true);
          }
          shut_rd = true;
      }
    }

    /**
     * 关闭套接字连接的写入部分；
     */
    protected void shutdownOutput() throws IOException {
      if (fd != null) {
          socketShutdown(SHUT_WR);
          shut_wr = true;
      }
    }

    protected boolean supportsUrgentData () {
        return true;
    }

    protected void sendUrgentData (int data) throws IOException {
        if (fd == null) {
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData (data);
    }

    /**
     * 如果用户忘记关闭它，则进行清理。
     */
    protected void finalize() throws IOException {
        close();
    }

    /*
     * “获取”并返回此实现的 FileDescriptor
     *
     * 需要一个对应的 releaseFD 来“释放”
     * FileDescriptor。
     */
    FileDescriptor acquireFD() {
        synchronized (fdLock) {
            fdUseCount++;
            return fd;
        }
    }

    /*
     * “释放”此实现的 FileDescriptor。
     *
     * 如果使用计数变为 -1，则关闭套接字。
     */
    void releaseFD() {
        synchronized (fdLock) {
            fdUseCount--;
            if (fdUseCount == -1) {
                if (fd != null) {
                    try {
                        socketClose();
                    } catch (IOException e) {
                    } finally {
                        fd = null;
                    }
                }
            }
        }
    }


                public boolean isConnectionReset() {
        synchronized (resetLock) {
            return (resetState == CONNECTION_RESET);
        }
    }

    public boolean isConnectionResetPending() {
        synchronized (resetLock) {
            return (resetState == CONNECTION_RESET_PENDING);
        }
    }

    public void setConnectionReset() {
        synchronized (resetLock) {
            resetState = CONNECTION_RESET;
        }
    }

    public void setConnectionResetPending() {
        synchronized (resetLock) {
            if (resetState == CONNECTION_NOT_RESET) {
                resetState = CONNECTION_RESET_PENDING;
            }
        }

    }

    /*
     * 返回 true 如果已经关闭或关闭正在等待
     */
    public boolean isClosedOrPending() {
        /*
         * 锁定 fdLock 以确保我们在关闭进行时等待。
         */
        synchronized (fdLock) {
            if (closePending || (fd == null)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * 返回当前的 SO_TIMEOUT 值
     */
    public int getTimeout() {
        return timeout;
    }

    /*
     * 通过复制文件描述符来“预关闭”套接字 - 这使得套接字可以在不释放文件描述符的情况下关闭。
     */
    private void socketPreClose() throws IOException {
        socketClose0(true);
    }

    /*
     * 关闭套接字（并释放文件描述符）。
     */
    protected void socketClose() throws IOException {
        socketClose0(false);
    }

    abstract void socketCreate(boolean isServer) throws IOException;
    abstract void socketConnect(InetAddress address, int port, int timeout)
        throws IOException;
    abstract void socketBind(InetAddress address, int port)
        throws IOException;
    abstract void socketListen(int count)
        throws IOException;
    abstract void socketAccept(SocketImpl s)
        throws IOException;
    abstract int socketAvailable()
        throws IOException;
    abstract void socketClose0(boolean useDeferredClose)
        throws IOException;
    abstract void socketShutdown(int howto)
        throws IOException;
    abstract void socketSetOption(int cmd, boolean on, Object value)
        throws SocketException;
    abstract int socketGetOption(int opt, Object iaContainerObj) throws SocketException;
    abstract void socketSendUrgentData(int data)
        throws IOException;

    public final static int SHUT_RD = 0;
    public final static int SHUT_WR = 1;
}
