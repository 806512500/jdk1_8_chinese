
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
import sun.misc.SharedSecrets;
import sun.misc.JavaIOFileDescriptorAccess;

/**
 * 该类定义了在 Windows Vista 及以上版本的 Windows 平台上使用的普通 DatagramSocketImpl。这些平台具有双层 TCP/IP 堆栈，可以通过单个文件描述符处理 IPv4 和 IPv6。
 * <p>
 * 注意：在双层 TCP/IP 堆栈上进行多播时，总是使用 TwoStacksPlainDatagramSocketImpl。这是为了克服 RFC 对双层套接字上多播行为定义的缺失。
 *
 * @author Chris Hegarty
 */

class DualStackPlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    static JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();

    static {
        initIDs();
    }

    // 如果此套接字是独占绑定的，则为 true
    private final boolean exclusiveBind;

    /*
     * 如果在套接字绑定后设置了 SO_REUSEADDR，则设置为 true，以表示 SO_REUSEADDR 正在被模拟
     */
    private boolean reuseAddressEmulated;

    // 当 exclusiveBind 为 true 且套接字已绑定时，模拟 SO_REUSEADDR
    private boolean isReuseAddress;

    DualStackPlainDatagramSocketImpl(boolean exclBind) {
        exclusiveBind = exclBind;
    }

    protected void datagramSocketCreate() throws SocketException {
        if (fd == null)
            throw new SocketException("Socket closed");

        int newfd = socketCreate(false /* v6Only */);

        fdAccess.set(fd, newfd);
    }

    protected synchronized void bind0(int lport, InetAddress laddr)
        throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        if (laddr == null)
            throw new NullPointerException("argument address");

        socketBind(nativefd, laddr, lport, exclusiveBind);
        if (lport == 0) {
            localPort = socketLocalPort(nativefd);
        } else {
            localPort = lport;
        }
    }

    protected synchronized int peek(InetAddress address) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("Null address in peek()");

        // 使用 peekData()
        DatagramPacket peekPacket = new DatagramPacket(new byte[1], 1);
        int peekPort = peekData(peekPacket);
        address = peekPacket.getAddress();
        return peekPort;
    }

    protected synchronized int peekData(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (p == null)
            throw new NullPointerException("packet");
        if (p.getData() == null)
            throw new NullPointerException("packet buffer");

        return socketReceiveOrPeekData(nativefd, p, timeout, connected, true /*peek*/);
    }

    protected synchronized void receive0(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (p == null)
            throw new NullPointerException("packet");
        if (p.getData() == null)
            throw new NullPointerException("packet buffer");

        socketReceiveOrPeekData(nativefd, p, timeout, connected, false /*receive*/);
    }

    protected void send(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();

        if (p == null)
            throw new NullPointerException("null packet");

        if (p.getAddress() == null || p.getData() == null)
            throw new NullPointerException("null address || null buffer");

        socketSend(nativefd, p.getData(), p.getOffset(), p.getLength(),
                   p.getAddress(), p.getPort(), connected);
    }

    protected void connect0(InetAddress address, int port) throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        if (address == null)
            throw new NullPointerException("address");

        socketConnect(nativefd, address, port);
    }

    protected void disconnect0(int family /*unused*/) {
        if (fd == null || !fd.valid())
            return;   // disconnect doesn't throw any exceptions

        socketDisconnect(fdAccess.get(fd));
    }

    protected void datagramSocketClose() {
        if (fd == null || !fd.valid())
            return;   // close doesn't throw any exceptions

        socketClose(fdAccess.get(fd));
        fdAccess.set(fd, -1);
    }

    @SuppressWarnings("fallthrough")
    protected void socketSetOption(int opt, Object val) throws SocketException {
        int nativefd = checkAndReturnNativeFD();

        int optionValue = 0;

        switch(opt) {
            case IP_TOS :
            case SO_RCVBUF :
            case SO_SNDBUF :
                optionValue = ((Integer)val).intValue();
                break;
            case SO_REUSEADDR :
                if (exclusiveBind && localPort != 0)  {
                    // 套接字已绑定，模拟 SO_REUSEADDR
                    reuseAddressEmulated = true;
                    isReuseAddress = (Boolean)val;
                    return;
                }
                // 故意不加 break
            case SO_BROADCAST :
                optionValue = ((Boolean)val).booleanValue() ? 1 : 0;
                break;
            default: /* 不应该到这里 */
                throw new SocketException("Option not supported");
        }

        socketSetIntOption(nativefd, opt, optionValue);
    }

    protected Object socketGetOption(int opt) throws SocketException {
        int nativefd = checkAndReturnNativeFD();

         // SO_BINDADDR 不是套接字选项。
        if (opt == SO_BINDADDR) {
            return socketLocalAddress(nativefd);
        }
        if (opt == SO_REUSEADDR && reuseAddressEmulated)
            return isReuseAddress;

        int value = socketGetIntOption(nativefd, opt);
        Object returnValue = null;

        switch (opt) {
            case SO_REUSEADDR :
            case SO_BROADCAST :
                returnValue =  (value == 0) ? Boolean.FALSE : Boolean.TRUE;
                break;
            case IP_TOS :
            case SO_RCVBUF :
            case SO_SNDBUF :
                returnValue = new Integer(value);
                break;
            default: /* 不应该到这里 */
                throw new SocketException("Option not supported");
        }


                    return returnValue;
    }

    /* Multicast specific methods.
     * Multicasting on a dual layer TCP/IP stack is always done with
     * TwoStacksPlainDatagramSocketImpl. This is to overcome the lack
     * of behavior defined for multicasting over a dual layer socket by the RFC.
     */
    protected void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected void setTimeToLive(int ttl) throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected int getTimeToLive() throws IOException {
        throw new IOException("Method not implemented!");
    }

    @Deprecated
    protected void setTTL(byte ttl) throws IOException {
        throw new IOException("Method not implemented!");
    }

    @Deprecated
    protected byte getTTL() throws IOException {
        throw new IOException("Method not implemented!");
    }
    /* END Multicast specific methods */

    private int checkAndReturnNativeFD() throws SocketException {
        if (fd == null || !fd.valid())
            throw new SocketException("Socket closed");

        return fdAccess.get(fd);
    }

    /* Native methods */

    private static native void initIDs();

    private static native int socketCreate(boolean v6Only);

    private static native void socketBind(int fd, InetAddress localAddress,
            int localport, boolean exclBind) throws SocketException;

    private static native void socketConnect(int fd, InetAddress address, int port)
        throws SocketException;

    private static native void socketDisconnect(int fd);

    private static native void socketClose(int fd);

    private static native int socketLocalPort(int fd) throws SocketException;

    private static native Object socketLocalAddress(int fd) throws SocketException;

    private static native int socketReceiveOrPeekData(int fd, DatagramPacket packet,
        int timeout, boolean connected, boolean peek) throws IOException;

    private static native void socketSend(int fd, byte[] data, int offset, int length,
        InetAddress address, int port, boolean connected) throws IOException;

    private static native void socketSetIntOption(int fd, int cmd,
        int optionValue) throws SocketException;

    private static native int socketGetIntOption(int fd, int cmd) throws SocketException;

    native int dataAvailable();
}
