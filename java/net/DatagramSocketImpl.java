/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.InterruptedIOException;

/**
 * 抽象数据报和多播套接字实现基类。
 * @author Pavani Diwanji
 * @since  JDK1.1
 */

public abstract class DatagramSocketImpl implements SocketOptions {

    /**
     * 本地端口号。
     */
    protected int localPort;

    /**
     * 文件描述符对象。
     */
    protected FileDescriptor fd;

    int dataAvailable() {
        // 默认实现返回零，这将禁用调用功能
        return 0;
    }

    /**
     * 拥有此实现的 DatagramSocket 或 MulticastSocket
     */
    DatagramSocket socket;

    void setDatagramSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    DatagramSocket getDatagramSocket() {
        return socket;
    }

    /**
     * 创建数据报套接字。
     * @exception SocketException 如果底层协议中出现错误，例如 TCP 错误。
     */
    protected abstract void create() throws SocketException;

    /**
     * 将数据报套接字绑定到本地端口和地址。
     * @param lport 本地端口
     * @param laddr 本地地址
     * @exception SocketException 如果底层协议中出现错误，例如 TCP 错误。
     */
    protected abstract void bind(int lport, InetAddress laddr) throws SocketException;

    /**
     * 发送数据报包。包包含数据和要发送包的目标地址。
     * @param p 要发送的包。
     * @exception IOException 如果在发送数据报包时发生 I/O 异常。
     * @exception  PortUnreachableException 如果套接字连接到当前无法到达的目标，则可能抛出此异常。请注意，没有保证会抛出此异常。
     */
    protected abstract void send(DatagramPacket p) throws IOException;

    /**
     * 将数据报套接字连接到远程目标。这将远程地址与本地套接字关联起来，以便数据报只能发送到此目标并从该目标接收。这可以被重写以调用本机系统连接。
     *
     * <p>如果套接字连接到的目标不存在或无法到达，并且已收到针对该地址的 ICMP 目标不可达包，则后续调用 send 或 receive 可能会抛出 PortUnreachableException。
     * 请注意，没有保证会抛出此异常。
     * @param address 要连接到的远程 InetAddress
     * @param port 远程端口号
     * @exception   SocketException 如果套接字无法连接到远程目标，则可能抛出此异常
     * @since 1.4
     */
    protected void connect(InetAddress address, int port) throws SocketException {}

    /**
     * 断开数据报套接字与远程目标的连接。
     * @since 1.4
     */
    protected void disconnect() {}

    /**
     * 查看包的来源。更新指定的 {@code InetAddress} 为包的来源地址。
     * @param i 一个 InetAddress 对象
     * @return 包的来源端口号。
     * @exception IOException 如果发生 I/O 异常
     * @exception  PortUnreachableException 如果套接字连接到当前无法到达的目标，则可能抛出此异常。请注意，没有保证会抛出此异常。
     */
    protected abstract int peek(InetAddress i) throws IOException;

    /**
     * 查看包的来源。数据被复制到指定的 {@code DatagramPacket} 中。数据被返回，但不被消耗，因此后续的 peekData/receive 操作将看到相同的数据。
     * @param p 接收到的包。
     * @return 包的来源端口号。
     * @exception IOException 如果发生 I/O 异常
     * @exception  PortUnreachableException 如果套接字连接到当前无法到达的目标，则可能抛出此异常。请注意，没有保证会抛出此异常。
     * @since 1.4
     */
    protected abstract int peekData(DatagramPacket p) throws IOException;
    /**
     * 接收数据报包。
     * @param p 接收到的包。
     * @exception IOException 如果在接收数据报包时发生 I/O 异常。
     * @exception  PortUnreachableException 如果套接字连接到当前无法到达的目标，则可能抛出此异常。请注意，没有保证会抛出此异常。
     */
    protected abstract void receive(DatagramPacket p) throws IOException;

    /**
     * 设置 TTL（生存时间）选项。
     * @param ttl 一个字节，指定 TTL 值
     *
     * @deprecated 使用 setTimeToLive 代替。
     * @exception IOException 如果在设置生存时间选项时发生 I/O 异常。
     * @see #getTTL()
     */
    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    /**
     * 检索 TTL（生存时间）选项。
     *
     * @exception IOException 如果在检索生存时间选项时发生 I/O 异常
     * @deprecated 使用 getTimeToLive 代替。
     * @return 一个字节，表示 TTL 值
     * @see #setTTL(byte)
     */
    @Deprecated
    protected abstract byte getTTL() throws IOException;

    /**
     * 设置 TTL（生存时间）选项。
     * @param ttl 一个 {@code int}，指定生存时间值
     * @exception IOException 如果在设置生存时间选项时发生 I/O 异常。
     * @see #getTimeToLive()
     */
    protected abstract void setTimeToLive(int ttl) throws IOException;

    /**
     * 检索 TTL（生存时间）选项。
     * @exception IOException 如果在检索生存时间选项时发生 I/O 异常
     * @return 一个 {@code int}，表示生存时间值
     * @see #setTimeToLive(int)
     */
    protected abstract int getTimeToLive() throws IOException;

    /**
     * 加入多播组。
     * @param inetaddr 要加入的多播地址。
     * @exception IOException 如果在加入多播组时发生 I/O 异常。
     */
    protected abstract void join(InetAddress inetaddr) throws IOException;

    /**
     * 离开多播组。
     * @param inetaddr 要离开的多播地址。
     * @exception IOException 如果在离开多播组时发生 I/O 异常。
     */
    protected abstract void leave(InetAddress inetaddr) throws IOException;

    /**
     * 加入多播组。
     * @param mcastaddr 要加入的地址。
     * @param netIf 指定接收多播数据报包的本地接口
     * @throws IOException 如果在加入多播组时发生 I/O 异常
     * @since 1.4
     */
    protected abstract void joinGroup(SocketAddress mcastaddr,
                                      NetworkInterface netIf)
        throws IOException;

    /**
     * 离开多播组。
     * @param mcastaddr 要离开的地址。
     * @param netIf 指定离开组的本地接口
     * @throws IOException 如果在离开多播组时发生 I/O 异常
     * @since 1.4
     */
    protected abstract void leaveGroup(SocketAddress mcastaddr,
                                       NetworkInterface netIf)
        throws IOException;

    /**
     * 关闭套接字。
     */
    protected abstract void close();

    /**
     * 获取本地端口。
     * @return 一个 {@code int}，表示本地端口值
     */
    protected int getLocalPort() {
        return localPort;
    }

    <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_SNDBUF) {
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(SocketOptions.SO_REUSEADDR, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_IF &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            setOption(SocketOptions.IP_MULTICAST_IF2, value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_TTL &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            if (! (value instanceof Integer)) {
                throw new IllegalArgumentException("not an integer");
            }
            setTimeToLive((Integer)value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            setOption(SocketOptions.IP_MULTICAST_LOOP, value);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_SNDBUF) {
            return (T) getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T) getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T) getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T) getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.IP_MULTICAST_IF &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            return (T) getOption(SocketOptions.IP_MULTICAST_IF2);
        } else if (name == StandardSocketOptions.IP_MULTICAST_TTL &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            Integer ttl = getTimeToLive();
            return (T)ttl;
        } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP &&
            (getDatagramSocket() instanceof MulticastSocket)) {
            return (T) getOption(SocketOptions.IP_MULTICAST_LOOP);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    /**
     * 获取数据报套接字文件描述符。
     * @return 一个 {@code FileDescriptor} 对象，表示数据报套接字文件描述符
     */
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }
}
