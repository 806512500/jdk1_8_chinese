
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

/**
 * 该类表示一个数据报包。
 * <p>
 * 数据报包用于实现无连接的数据包传输服务。每个消息的路由仅基于该数据包中包含的信息，从一台机器路由到另一台机器。
 * 从一台机器发送到另一台机器的多个数据包可能会通过不同的路由，并且可能以任何顺序到达。数据包传输不保证。
 *
 * @author  Pavani Diwanji
 * @author  Benjamin Renaud
 * @since   JDK1.0
 */
public final
class DatagramPacket {

    /**
     * 执行类初始化
     */
    static {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });
        init();
    }

    /*
     * 该类的字段是包私有的，因为 DatagramSocketImpl 类需要访问它们。
     */
    byte[] buf;
    int offset;
    int length;
    int bufLength;
    InetAddress address;
    int port;

    /**
     * 构造一个用于接收长度为 {@code length} 的数据包的 {@code DatagramPacket}，并指定缓冲区中的偏移量。
     * <p>
     * {@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      用于保存传入数据报的缓冲区。
     * @param   offset   缓冲区的偏移量
     * @param   length   要读取的字节数。
     *
     * @since 1.2
     */
    public DatagramPacket(byte buf[], int offset, int length) {
        setData(buf, offset, length);
        this.address = null;
        this.port = -1;
    }

    /**
     * 构造一个用于接收长度为 {@code length} 的数据包的 {@code DatagramPacket}。
     * <p>
     * {@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      用于保存传入数据报的缓冲区。
     * @param   length   要读取的字节数。
     */
    public DatagramPacket(byte buf[], int length) {
        this (buf, 0, length);
    }

    /**
     * 构造一个用于发送数据包的 {@code DatagramPacket}，数据包长度为 {@code length}，偏移量为 {@code ioffset}，发送到指定主机上的指定端口号。{@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      数据包数据。
     * @param   offset   数据包数据偏移量。
     * @param   length   数据包数据长度。
     * @param   address  目标地址。
     * @param   port     目标端口号。
     * @see java.net.InetAddress
     *
     * @since 1.2
     */
    public DatagramPacket(byte buf[], int offset, int length,
                          InetAddress address, int port) {
        setData(buf, offset, length);
        setAddress(address);
        setPort(port);
    }

    /**
     * 构造一个用于发送数据包的 {@code DatagramPacket}，数据包长度为 {@code length}，偏移量为 {@code ioffset}，发送到指定主机上的指定端口号。{@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      数据包数据。
     * @param   offset   数据包数据偏移量。
     * @param   length   数据包数据长度。
     * @param   address  目标套接字地址。
     * @throws  IllegalArgumentException 如果地址类型不受支持
     * @see java.net.InetAddress
     *
     * @since 1.4
     */
    public DatagramPacket(byte buf[], int offset, int length, SocketAddress address) {
        setData(buf, offset, length);
        setSocketAddress(address);
    }

    /**
     * 构造一个用于发送数据包的 {@code DatagramPacket}，数据包长度为 {@code length}，发送到指定主机上的指定端口号。{@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      数据包数据。
     * @param   length   数据包长度。
     * @param   address  目标地址。
     * @param   port     目标端口号。
     * @see     java.net.InetAddress
     */
    public DatagramPacket(byte buf[], int length,
                          InetAddress address, int port) {
        this(buf, 0, length, address, port);
    }

    /**
     * 构造一个用于发送数据包的 {@code DatagramPacket}，数据包长度为 {@code length}，发送到指定主机上的指定端口号。{@code length} 参数必须小于或等于 {@code buf.length}。
     *
     * @param   buf      数据包数据。
     * @param   length   数据包长度。
     * @param   address  目标地址。
     * @throws  IllegalArgumentException 如果地址类型不受支持
     * @since 1.4
     * @see     java.net.InetAddress
     */
    public DatagramPacket(byte buf[], int length, SocketAddress address) {
        this(buf, 0, length, address);
    }

    /**
     * 返回此数据报将发送到或接收到的机器的 IP 地址。
     *
     * @return  此数据报将发送到或接收到的机器的 IP 地址。
     * @see     java.net.InetAddress
     * @see #setAddress(java.net.InetAddress)
     */
    public synchronized InetAddress getAddress() {
        return address;
    }

    /**
     * 返回此数据报将发送到或接收到的远程主机上的端口号。
     *
     * @return  此数据报将发送到或接收到的远程主机上的端口号。
     * @see #setPort(int)
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * 返回数据缓冲区。接收到的数据或要发送的数据从缓冲区中的 {@code offset} 开始，长度为 {@code length}。
     *
     * @return  用于接收或发送数据的缓冲区
     * @see #setData(byte[], int, int)
     */
    public synchronized byte[] getData() {
        return buf;
    }

                /**
     * 返回要发送的数据或接收到的数据的偏移量。
     *
     * @return  要发送的数据或接收到的数据的偏移量。
     *
     * @since 1.2
     */
    public synchronized int getOffset() {
        return offset;
    }

    /**
     * 返回要发送的数据或接收到的数据的长度。
     *
     * @return  要发送的数据或接收到的数据的长度。
     * @see #setLength(int)
     */
    public synchronized int getLength() {
        return length;
    }

    /**
     * 为该数据包设置数据缓冲区。这会设置数据包的数据、长度和偏移量。
     *
     * @param buf 为该数据包设置的缓冲区
     *
     * @param offset 数据中的偏移量
     *
     * @param length 数据的长度
     *       和/或用于接收数据的缓冲区的长度
     *
     * @exception NullPointerException 如果参数为 null
     *
     * @see #getData
     * @see #getOffset
     * @see #getLength
     *
     * @since 1.2
     */
    public synchronized void setData(byte[] buf, int offset, int length) {
        /* 这将检查 buf 是否为 null */
        if (length < 0 || offset < 0 ||
            (length + offset) < 0 ||
            ((length + offset) > buf.length)) {
            throw new IllegalArgumentException("非法的长度或偏移量");
        }
        this.buf = buf;
        this.length = length;
        this.bufLength = length;
        this.offset = offset;
    }

    /**
     * 设置此数据报要发送到的机器的 IP 地址。
     * @param iaddr {@code InetAddress}
     * @since   JDK1.1
     * @see #getAddress()
     */
    public synchronized void setAddress(InetAddress iaddr) {
        address = iaddr;
    }

    /**
     * 设置此数据报要发送到的远程主机的端口号。
     * @param iport 端口号
     * @since   JDK1.1
     * @see #getPort()
     */
    public synchronized void setPort(int iport) {
        if (iport < 0 || iport > 0xFFFF) {
            throw new IllegalArgumentException("端口超出范围:" + iport);
        }
        port = iport;
    }

    /**
     * 设置此数据报要发送到的远程主机的 SocketAddress（通常是 IP 地址 + 端口号）。
     *
     * @param address {@code SocketAddress}
     * @throws  IllegalArgumentException 如果地址为 null 或是此套接字不支持的 SocketAddress 子类
     *
     * @since 1.4
     * @see #getSocketAddress
     */
    public synchronized void setSocketAddress(SocketAddress address) {
        if (address == null || !(address instanceof InetSocketAddress))
            throw new IllegalArgumentException("不支持的地址类型");
        InetSocketAddress addr = (InetSocketAddress) address;
        if (addr.isUnresolved())
            throw new IllegalArgumentException("未解析的地址");
        setAddress(addr.getAddress());
        setPort(addr.getPort());
    }

    /**
     * 获取此数据包要发送到或来自的远程主机的 SocketAddress（通常是 IP 地址 + 端口号）。
     *
     * @return {@code SocketAddress}
     * @since 1.4
     * @see #setSocketAddress
     */
    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }

    /**
     * 为该数据包设置数据缓冲区。将此 DatagramPacket 的偏移量设置为 0，长度设置为
     * {@code buf} 的长度。
     *
     * @param buf 为该数据包设置的缓冲区。
     *
     * @exception NullPointerException 如果参数为 null。
     *
     * @see #getLength
     * @see #getData
     *
     * @since JDK1.1
     */
    public synchronized void setData(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("空的数据包缓冲区");
        }
        this.buf = buf;
        this.offset = 0;
        this.length = buf.length;
        this.bufLength = buf.length;
    }

    /**
     * 为该数据包设置长度。数据包的长度是从数据包的数据缓冲区中发送的字节数，或用于接收数据的数据包的数据缓冲区中的字节数。长度必须小于或等于数据包缓冲区的偏移量加上长度。
     *
     * @param length 为该数据包设置的长度。
     *
     * @exception IllegalArgumentException 如果长度为负数
     * 或长度大于数据包的数据缓冲区长度。
     *
     * @see #getLength
     * @see #setData
     *
     * @since JDK1.1
     */
    public synchronized void setLength(int length) {
        if ((length + offset) > buf.length || length < 0 ||
            (length + offset) < 0) {
            throw new IllegalArgumentException("非法的长度");
        }
        this.length = length;
        this.bufLength = this.length;
    }

    /**
     * 执行类加载时的初始化。
     */
    private native static void init();
}
