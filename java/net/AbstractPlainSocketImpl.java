
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;

import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;

/**
 * 默认的 Socket 实现。此实现不执行任何安全检查。
 * 注意此类不应是公共的。
 *
 * @author  Steven B. Byrne
 */
abstract class AbstractPlainSocketImpl extends SocketImpl
{
    /* SO_TIMEOUT 的实例变量 */
    int timeout;   // 超时时间（毫秒）
    // 流量类别
    private int trafficClass;

    private boolean shut_rd = false;
    private boolean shut_wr = false;

    private SocketInputStream socketInputStream = null;
    private SocketOutputStream socketOutputStream = null;

    /* 使用 FileDescriptor 的线程数 */
    protected int fdUseCount = 0;

    /* 在增加/减少 fdUseCount 时的锁 */
    protected final Object fdLock = new Object();

    /* 表示文件描述符上有一个关闭操作待处理 */
    protected boolean closePending = false;

    /* 表示连接重置状态 */
    private int CONNECTION_NOT_RESET = 0;
    private int CONNECTION_RESET_PENDING = 1;
    private int CONNECTION_RESET = 2;
    private int resetState;
    private final Object resetLock = new Object();

   /* 表示此 Socket 是流（TCP）Socket 还是（UDP）Socket
    */
    protected boolean stream;

    /**
     * 加载 net 库到运行时。
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

    /**
     * 创建一个布尔值指定的 Socket，该布尔值表示这是一个流 Socket（true）还是一个未连接的 UDP Socket（false）。
     */
    protected synchronized void create(boolean stream) throws IOException {
        this.stream = stream;
        if (!stream) {
            ResourceManager.beforeUdpCreate();
            // 只有在确定可以创建 Socket 后才创建 fd
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
     * 创建一个 Socket 并将其连接到指定主机上的指定端口。
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
                    /* 如果 connect 抛出异常，则将其传递给调用栈 */
                }
            }
        }
    }

    /**
     * 创建一个 Socket 并将其连接到指定地址上的指定端口。
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
     * 创建一个 Socket 并将其连接到指定地址上的指定端口。
     * @param address 地址
     * @param timeout 超时值（毫秒），或零表示无超时。
     * @throws IOException 如果连接失败
     * @throws  IllegalArgumentException 如果 address 为 null 或是不支持的 SocketAddress 子类
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
                    /* 如果 connect 抛出异常，则将其传递给调用栈 */
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
            throw new SocketException("Socket 已关闭");
        }
        boolean on = true;
        switch (opt) {
            /* 在调用本地方法前检查类型安全。这些检查应该永远不会失败，因为只有 java.Socket* 可以访问 PlainSocketImpl.setOption()。 */
        case SO_LINGER:
            if (val == null || (!(val instanceof Integer) && !(val instanceof Boolean)))
                throw new SocketException("选项的参数错误");
            if (val instanceof Boolean) {
                /* 仅在禁用时为 true - 启用时应为 Integer */
                on = false;
            }
            break;
        case SO_TIMEOUT:
            if (val == null || (!(val instanceof Integer)))
                throw new SocketException("SO_TIMEOUT 的参数错误");
            int tmp = ((Integer) val).intValue();
            if (tmp < 0)
                throw new IllegalArgumentException("超时 < 0");
            timeout = tmp;
            break;
        case IP_TOS:
             if (val == null || !(val instanceof Integer)) {
                 throw new SocketException("IP_TOS 的参数错误");
             }
             trafficClass = ((Integer)val).intValue();
             break;
        case SO_BINDADDR:
            throw new SocketException("不能重新绑定 Socket");
        case TCP_NODELAY:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("TCP_NODELAY 的参数错误");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_SNDBUF:
        case SO_RCVBUF:
            if (val == null || !(val instanceof Integer) ||
                !(((Integer)val).intValue() > 0)) {
                throw new SocketException("SO_SNDBUF 或 SO_RCVBUF 的参数错误");
            }
            break;
        case SO_KEEPALIVE:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("SO_KEEPALIVE 的参数错误");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_OOBINLINE:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("SO_OOBINLINE 的参数错误");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_REUSEADDR:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("SO_REUSEADDR 的参数错误");
            on = ((Boolean)val).booleanValue();
            break;
        default:
            throw new SocketException("未识别的 TCP 选项: " + opt);
        }
        socketSetOption(opt, on, val);
    }
    public Object getOption(int opt) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket 已关闭");
        }
        if (opt == SO_TIMEOUT) {
            return new Integer(timeout);
        }
        int ret = 0;
        /*
         * 本地 socketGetOption() 知道 3 个选项。
         * 返回的 32 位值将根据我们询问的内容进行解释。
         * 返回 -1 表示它理解该选项但已关闭。如果 "opt" 不是它理解的选项，它将引发 SocketException。
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
                // TODO - 应该更好地尝试读取 TOS 或 TCLASS
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

    /**
     * 连接操作的主要工作。尝试多次建立与给定 <host, port> 的连接。如果失败，抛出一个 IOException 表示出了什么问题。
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
                /* socket 可能在 poll/select 期间被关闭 */
                synchronized (fdLock) {
                    if (closePending) {
                        throw new SocketException ("Socket 已关闭");
                    }
                }
                // 如果我们有 Socket 的引用，则将 created, bound & connected 标志设置为 true。
                // 通常在 Socket.connect() 中完成，但 Socket 的某些子类可能会直接调用 impl.connect()！
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
     * 将 Socket 绑定到指定本地端口的指定地址。
     * @param address 地址
     * @param lport 端口
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
     * 监听指定时间的连接。
     * @param count 监听连接的时间
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
     * 获取此 Socket 的 InputStream。
     */
    protected synchronized InputStream getInputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket 已关闭");
            if (shut_rd)
                throw new IOException("Socket 输入已关闭");
            if (socketInputStream == null)
                socketInputStream = new SocketInputStream(this);
        }
        return socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        socketInputStream = in;
    }

    /**
     * 获取此 Socket 的 OutputStream。
     */
    protected synchronized OutputStream getOutputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket 已关闭");
            if (shut_wr)
                throw new IOException("Socket 输出已关闭");
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
            throw new IOException("流已关闭。");
        }

        /*
         * 如果连接已重置或关闭输入，则返回 0
         * 表示没有缓冲的字节。
         */
        if (isConnectionReset() || shut_rd) {
            return 0;
        }

        /*
         * 如果没有可用字节且我们之前被通知
         * 连接已重置，则我们移动到重置状态。
         *
         * 如果我们被通知连接已重置，则再次检查
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
                     * 我们分两步关闭文件描述符 - 首先进行“预关闭”，
                     * 关闭套接字但不释放底层文件描述符。此操作
                     * 可能由于未传输的数据和长时间的逗留间隔而耗时较长。
                     * 一旦预关闭完成，我们再进行实际的套接字关闭以释放文件描述符。
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
                     * 如果线程已获取文件描述符且关闭
                     * 不是待处理的，则使用延迟关闭。
                     * 同时减少 fdUseCount 以信号最后一个
                     * 释放文件描述符的线程关闭它。
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
     * 关闭套接字连接的读取部分。
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
     * 关闭套接字连接的写入部分。
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
            throw new IOException("套接字已关闭");
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
     * “获取”并返回此实现的文件描述符
     *
     * 需要一个对应的 releaseFD 来“释放”
     * 文件描述符。
     */
    FileDescriptor acquireFD() {
        synchronized (fdLock) {
            fdUseCount++;
            return fd;
        }
    }

    /*
     * “释放”此实现的文件描述符。
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
     * 如果已关闭或关闭待处理，则返回 true
     */
    public boolean isClosedOrPending() {
        /*
         * 锁定 fdLock 以确保我们在
         * 关闭进行时等待。
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
     * 返回 SO_TIMEOUT 的当前值
     */
    public int getTimeout() {
        return timeout;
    }

    /*
     * 通过复制文件描述符“预关闭”套接字 - 这允许
     * 套接字关闭而不释放文件描述符。
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
