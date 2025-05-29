
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.FileDescriptor;
import sun.misc.SharedSecrets;
import sun.misc.JavaIOFileDescriptorAccess;

/**
 * 该类定义了在 Windows Vista 及更高版本的 Windows 平台上使用的普通 SocketImpl。
 * 这些平台具有双层 TCP/IP 堆栈，可以通过单个文件描述符处理 IPv4 和 IPv6。
 *
 * @author Chris Hegarty
 */

class DualStackPlainSocketImpl extends AbstractPlainSocketImpl
{
    static JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();


    // 如果此套接字是独占绑定的，则为 true
    private final boolean exclusiveBind;

    // 当 exclusiveBind 为 true 时模拟 SO_REUSEADDR
    private boolean isReuseAddress;

    public DualStackPlainSocketImpl(boolean exclBind, boolean isServer) {
        super(isServer);
        exclusiveBind = exclBind;
    }

    void socketCreate(boolean stream) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");

        int newfd = socket0(stream, false /*v6 Only*/);

        fdAccess.set(fd, newfd);
    }

    void socketConnect(InetAddress address, int port, int timeout)
        throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("inet address argument is null.");

        int connectResult;
        if (timeout <= 0) {
            connectResult = connect0(nativefd, address, port);
        } else {
            configureBlocking(nativefd, false);
            try {
                connectResult = connect0(nativefd, address, port);
                if (connectResult == WOULDBLOCK) {
                    waitForConnect(nativefd, timeout);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        /*
         * 需要设置本地端口字段。如果在连接之前调用了 bind（由客户端调用），则本地端口字段
         * 已经设置。
         */
        if (localport == 0)
            localport = localPort0(nativefd);
    }

    void socketBind(InetAddress address, int port) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("inet address argument is null.");

        bind0(nativefd, address, port, exclusiveBind);
        if (port == 0) {
            localport = localPort0(nativefd);
        } else {
            localport = port;
        }

        this.address = address;
    }

    void socketListen(int backlog) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        listen0(nativefd, backlog);
    }

    void socketAccept(SocketImpl s) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (s == null)
            throw new NullPointerException("socket is null");

        int newfd = -1;
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        if (timeout <= 0) {
            newfd = accept0(nativefd, isaa);
        } else {
            configureBlocking(nativefd, false);
            try {
                waitForNewConnection(nativefd, timeout);
                newfd = accept0(nativefd, isaa);
                if (newfd != -1) {
                    configureBlocking(newfd, true);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        /* 更新 (SocketImpl)s 的 fd */
        fdAccess.set(s.fd, newfd);
        /* 更新 socketImpl 的远程端口、地址和本地端口 */
        InetSocketAddress isa = isaa[0];
        s.port = isa.getPort();
        s.address = isa.getAddress();
        s.localport = localport;
    }

    int socketAvailable() throws IOException {
        int nativefd = checkAndReturnNativeFD();
        return available0(nativefd);
    }

    void socketClose0(boolean useDeferredClose/*unused*/) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");

        if (!fd.valid())
            return;

        final int nativefd = fdAccess.get(fd);
        fdAccess.set(fd, -1);
        close0(nativefd);
    }

    void socketShutdown(int howto) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        shutdown0(nativefd, howto);
    }

    // 在 SO_REUSEADDR 之后有意的穿透
    @SuppressWarnings("fallthrough")
    void socketSetOption(int opt, boolean on, Object value)
        throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        if (opt == SO_TIMEOUT) {  // 超时通过 select 实现
            return;
        }

        int optionValue = 0;

        switch(opt) {
            case SO_REUSEADDR :
                if (exclusiveBind) {
                    // 使用独占绑定时模拟 SO_REUSEADDR
                    isReuseAddress = on;
                    return;
                }
                // 有意的穿透
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
                optionValue = on ? 1 : 0;
                break;
            case SO_SNDBUF :
            case SO_RCVBUF :
            case IP_TOS :
                optionValue = ((Integer)value).intValue();
                break;
            case SO_LINGER :
                if (on) {
                    optionValue =  ((Integer)value).intValue();
                } else {
                    optionValue = -1;
                }
                break;
            default :/* 不应到达这里 */
                throw new SocketException("Option not supported");
        }

        setIntOption(nativefd, opt, optionValue);
    }

    int socketGetOption(int opt, Object iaContainerObj) throws SocketException {
        int nativefd = checkAndReturnNativeFD();


                    // SO_BINDADDR 不是一个套接字选项。
        if (opt == SO_BINDADDR) {
            localAddress(nativefd, (InetAddressContainer)iaContainerObj);
            return 0;  // 返回值无关紧要。
        }

        // 当使用独占绑定时，模拟 SO_REUSEADDR
        if (opt == SO_REUSEADDR && exclusiveBind)
            return isReuseAddress? 1 : -1;

        int value = getIntOption(nativefd, opt);

        switch (opt) {
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
            case SO_REUSEADDR :
                return (value == 0) ? -1 : 1;
        }
        return value;
    }

    void socketSendUrgentData(int data) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        sendOOB(nativefd, data);
    }

    private int checkAndReturnNativeFD() throws SocketException {
        if (fd == null || !fd.valid())
            throw new SocketException("套接字已关闭");

        return fdAccess.get(fd);
    }

    static final int WOULDBLOCK = -2;       // 无数据可用（非阻塞）

    static {
        initIDs();
    }

    /* 本地方法 */

    static native void initIDs();

    static native int socket0(boolean stream, boolean v6Only) throws IOException;

    static native void bind0(int fd, InetAddress localAddress, int localport,
                             boolean exclBind)
        throws IOException;

    static native int connect0(int fd, InetAddress remote, int remotePort)
        throws IOException;

    static native void waitForConnect(int fd, int timeout) throws IOException;

    static native int localPort0(int fd) throws IOException;

    static native void localAddress(int fd, InetAddressContainer in) throws SocketException;

    static native void listen0(int fd, int backlog) throws IOException;

    static native int accept0(int fd, InetSocketAddress[] isaa) throws IOException;

    static native void waitForNewConnection(int fd, int timeout) throws IOException;

    static native int available0(int fd) throws IOException;

    static native void close0(int fd) throws IOException;

    static native void shutdown0(int fd, int howto) throws IOException;

    static native void setIntOption(int fd, int cmd, int optionValue) throws SocketException;

    static native int getIntOption(int fd, int cmd) throws SocketException;

    static native void sendOOB(int fd, int data) throws IOException;

    static native void configureBlocking(int fd, boolean blocking) throws IOException;
}
