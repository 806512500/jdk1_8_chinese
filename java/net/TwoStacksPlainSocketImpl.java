
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
import sun.net.ResourceManager;

/*
 * 该类定义了用于所有低于Vista版本的Windows的普通SocketImpl。它在这些平台支持IPv6的情况下添加了对IPv6的支持。
 *
 * 为了向后兼容，没有IPv6支持的Windows平台也使用此实现，并且在创建套接字时将fd1设置为null。
 *
 * @author Chris Hegarty
 */

class TwoStacksPlainSocketImpl extends AbstractPlainSocketImpl
{
    /* 第二个文件描述符，仅在Windows上用于IPv6。
     * fd1 用于监听器和客户端套接字的初始化
     * 直到套接字连接。到这一点为止，fd 始终引用 IPv4 套接字，而 fd1 引用 IPv6 套接字。套接字连接后，
     * fd 始终引用已连接的套接字（v4 或 v6），而 fd1 被关闭。
     *
     * 对于 ServerSockets，fd 始终引用 v4 监听器，而 fd1 引用 v6 监听器。
     */
    private FileDescriptor fd1;

    /*
     * 在Windows上需要用于IPv6，因为我们需要知道
     * 当调用者询问时，套接字是否绑定到 ::0 或 0.0.0.0。否则我们不知道询问哪个套接字。
     */
    private InetAddress anyLocalBoundAddr = null;

    /* 为了防止在两个套接字上监听时发生饥饿，这用于保存上次接受连接的套接字的id。*/
    private int lastfd = -1;

    // 如果此套接字是独占绑定的，则为true
    private final boolean exclusiveBind;

    // 当 exclusiveBind 为 true 时模拟 SO_REUSEADDR
    private boolean isReuseAddress;

    static {
        initProto();
    }

    public TwoStacksPlainSocketImpl(boolean exclBind, boolean isServer) {
        super(isServer);
        exclusiveBind = exclBind;
    }

    /**
     * 创建一个布尔值指定的套接字，该布尔值指示这是流套接字（true）还是未连接的UDP套接字（false）。
     */
    protected synchronized void create(boolean stream) throws IOException {
        fd1 = new FileDescriptor();
        try {
            super.create(stream);
        } catch (IOException e) {
            fd1 = null;
            throw e;
        }
    }

     /**
     * 将套接字绑定到指定本地端口的指定地址。
     * @param address 地址
     * @param port 端口
     */
    protected synchronized void bind(InetAddress address, int lport)
        throws IOException
    {
        super.bind(address, lport);
        if (address.isAnyLocalAddress()) {
            anyLocalBoundAddr = address;
        }
    }

    public Object getOption(int opt) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        if (opt == SO_BINDADDR) {
            if (fd != null && fd1 != null ) {
                /* 必须未绑定或绑定到 anyLocal */
                return anyLocalBoundAddr;
            }
            InetAddressContainer in = new InetAddressContainer();
            socketGetOption(opt, in);
            return in.addr;
        } else if (opt == SO_REUSEADDR && exclusiveBind) {
            // 使用独占绑定时模拟 SO_REUSEADDR
            return isReuseAddress;
        } else
            return super.getOption(opt);
    }

    @Override
    void socketBind(InetAddress address, int port) throws IOException {
        socketBind(address, port, exclusiveBind);
    }

    @Override
    void socketSetOption(int opt, boolean on, Object value)
        throws SocketException
    {
        // 使用独占绑定时模拟 SO_REUSEADDR
        if (opt == SO_REUSEADDR && exclusiveBind)
            isReuseAddress = on;
        else
            socketNativeSetOption(opt, on, value);
    }

    /**
     * 关闭套接字。
     */
    @Override
    protected void close() throws IOException {
        synchronized(fdLock) {
            if (fd != null || fd1 != null) {
                if (!stream) {
                    ResourceManager.afterUdpClose();
                }
                if (fdUseCount == 0) {
                    if (closePending) {
                        return;
                    }
                    closePending = true;
                    socketClose();
                    fd = null;
                    fd1 = null;
                    return;
                } else {
                    /*
                     * 如果一个线程已经获取了 fd 并且没有待处理的关闭请求，则使用延迟关闭。
                     * 同时减少 fdUseCount 以信号最后一个释放 fd 的线程关闭它。
                     */
                    if (!closePending) {
                        closePending = true;
                        fdUseCount--;
                        socketClose();
                    }
                }
            }
        }
    }

    @Override
    void reset() throws IOException {
        if (fd != null || fd1 != null) {
            socketClose();
        }
        fd = null;
        fd1 = null;
        super.reset();
    }

    /*
     * 如果已关闭或关闭待处理，则返回 true
     */
    @Override
    public boolean isClosedOrPending() {
        /*
         * 锁定 fdLock 以确保我们在关闭进行时等待。
         */
        synchronized (fdLock) {
            if (closePending || (fd == null && fd1 == null)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /* 本地方法 */

    static native void initProto();

    native void socketCreate(boolean isServer) throws IOException;

    native void socketConnect(InetAddress address, int port, int timeout)
        throws IOException;

    native void socketBind(InetAddress address, int port, boolean exclBind)
        throws IOException;

                native void socketListen(int count) throws IOException;
    // 本机方法，用于监听指定数量的连接请求

    native void socketAccept(SocketImpl s) throws IOException;
    // 本机方法，用于接受一个连接请求，并将其绑定到给定的 SocketImpl 对象

    native int socketAvailable() throws IOException;
    // 本机方法，返回可读取的字节数

    native void socketClose0(boolean useDeferredClose) throws IOException;
    // 本机方法，用于关闭套接字，可以选择是否延迟关闭

    native void socketShutdown(int howto) throws IOException;
    // 本机方法，用于关闭套接字的读取、写入或两者

    native void socketNativeSetOption(int cmd, boolean on, Object value)
        throws SocketException;
    // 本机方法，用于设置套接字选项

    native int socketGetOption(int opt, Object iaContainerObj) throws SocketException;
    // 本机方法，用于获取套接字选项

    native void socketSendUrgentData(int data) throws IOException;
    // 本机方法，用于发送紧急数据
}
