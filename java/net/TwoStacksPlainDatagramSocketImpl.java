/*
 * Copyright (c) 2007, 2015, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 该类定义了用于所有低于Vista版本的Windows的普通DatagramSocketImpl。它在这些平台支持IPv6（如果可用）。
 *
 * 为了向后兼容，没有IPv6支持的Windows平台也使用此实现，并且在创建套接字时将fd1设置为null。
 *
 * @author Chris Hegarty
 */

class TwoStacksPlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    /* 仅在Windows上用于IPv6 */
    private FileDescriptor fd1;

    /*
     * 在Windows上需要用于IPv6，因为我们需要知道
     * 当调用者请求时，套接字是否绑定到::0或0.0.0.0。在这种情况下，两个套接字都被使用，但我们
     * 不知道调用者请求的是::0还是0.0.0.0，需要在这里记住它。
     */
    private InetAddress anyLocalBoundAddr=null;

    private int fduse=-1; /* 在peek()和receive()调用之间保存 */

    /* 在两个套接字同时检测到数据时，在连续的receive调用之间保存。为了确保一个套接字不会
     * 被饿死，它们使用此字段进行轮换
     */
    private int lastfd=-1;

    static {
        init();
    }

    // 如果此套接字是独占绑定的，则为true
    private final boolean exclusiveBind;

    /*
     * 如果在套接字绑定后设置了SO_REUSEADDR，则设置为true
     * 表示SO_REUSEADDR正在被模拟
     */
    private boolean reuseAddressEmulated;

    // 当exclusiveBind为true且套接字已绑定时模拟SO_REUSEADDR
    private boolean isReuseAddress;

    TwoStacksPlainDatagramSocketImpl(boolean exclBind) {
        exclusiveBind = exclBind;
    }

    protected synchronized void create() throws SocketException {
        fd1 = new FileDescriptor();
        try {
            super.create();
        } catch (SocketException e) {
            fd1 = null;
            throw e;
        }
    }

    protected synchronized void bind(int lport, InetAddress laddr)
        throws SocketException {
        super.bind(lport, laddr);
        if (laddr.isAnyLocalAddress()) {
            anyLocalBoundAddr = laddr;
        }
    }

    @Override
    protected synchronized void bind0(int lport, InetAddress laddr)
        throws SocketException
    {
        bind0(lport, laddr, exclusiveBind);

    }

    protected synchronized void receive(DatagramPacket p)
        throws IOException {
        try {
            receive0(p);
        } finally {
            fduse = -1;
        }
    }

    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }

        if (optID == SO_BINDADDR) {
            if ((fd != null && fd1 != null) && !connected) {
                return anyLocalBoundAddr;
            }
            int family = connectedAddress == null ? -1 : connectedAddress.holder().getFamily();
            return socketLocalAddress(family);
        } else if (optID == SO_REUSEADDR && reuseAddressEmulated) {
            return isReuseAddress;
        } else {
            return super.getOption(optID);
        }
    }

    protected void socketSetOption(int opt, Object val)
        throws SocketException
    {
        if (opt == SO_REUSEADDR && exclusiveBind && localPort != 0)  {
            // 套接字已绑定，模拟
            reuseAddressEmulated = true;
            isReuseAddress = (Boolean)val;
        } else {
            socketNativeSetOption(opt, val);
        }

    }

    protected boolean isClosed() {
        return (fd == null && fd1 == null) ? true : false;
    }

    protected void close() {
        if (fd != null || fd1 != null) {
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            fd = null;
            fd1 = null;
        }
    }

    /* 本地方法 */

    protected synchronized native void bind0(int lport, InetAddress laddr,
                                             boolean exclBind)
        throws SocketException;

    protected native void send(DatagramPacket p) throws IOException;

    protected synchronized native int peek(InetAddress i) throws IOException;

    protected synchronized native int peekData(DatagramPacket p) throws IOException;

    protected synchronized native void receive0(DatagramPacket p)
        throws IOException;

    protected native void setTimeToLive(int ttl) throws IOException;

    protected native int getTimeToLive() throws IOException;

    @Deprecated
    protected native void setTTL(byte ttl) throws IOException;

    @Deprecated
    protected native byte getTTL() throws IOException;

    protected native void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    protected native void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    protected native void datagramSocketCreate() throws SocketException;

    protected native void datagramSocketClose();

    protected native void socketNativeSetOption(int opt, Object val)
        throws SocketException;

    protected native Object socketGetOption(int opt) throws SocketException;

    protected native void connect0(InetAddress address, int port) throws SocketException;

    protected native Object socketLocalAddress(int family) throws SocketException;

    protected native void disconnect0(int family);

    native int dataAvailable();

    /**
     * 执行类加载时的初始化。
     */
    private native static void init();
}
