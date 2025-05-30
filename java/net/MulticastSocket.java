
/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Enumeration;

/**
 * 多播数据报套接字类用于发送和接收 IP 多播数据包。MulticastSocket 是一个 (UDP) DatagramSocket，具有加入“组”的其他多播主机的能力。
 * <P>
 * 多播组由一个 D 类 IP 地址和一个标准 UDP 端口号指定。D 类 IP 地址范围为 <CODE>224.0.0.0</CODE> 到 <CODE>239.255.255.255</CODE>，包括两端。
 * 地址 224.0.0.0 是保留的，不应使用。
 * <P>
 * 通过首先创建一个具有所需端口的 MulticastSocket，然后调用 <CODE>joinGroup(InetAddress groupAddr)</CODE> 方法，可以加入多播组：
 * <PRE>
 * // 加入多播组并发送组问候
 * ...
 * String msg = "Hello";
 * InetAddress group = InetAddress.getByName("228.5.6.7");
 * MulticastSocket s = new MulticastSocket(6789);
 * s.joinGroup(group);
 * DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
 *                             group, 6789);
 * s.send(hi);
 * // 获取他们的响应！
 * byte[] buf = new byte[1000];
 * DatagramPacket recv = new DatagramPacket(buf, buf.length);
 * s.receive(recv);
 * ...
 * // 好了，我谈完了 - 离开组...
 * s.leaveGroup(group);
 * </PRE>
 *
 * 当向多播组发送消息时，<B>所有</B>订阅该主机和端口的接收者都会收到消息（在数据包的生存时间范围内，见下文）。套接字不必是多播组的成员即可向其发送消息。
 * <P>
 * 当套接字订阅多播组/端口时，它会接收其他主机发送到该组/端口的数据报，所有其他组和端口的成员也会收到这些数据报。套接字通过 leaveGroup(InetAddress addr) 方法放弃组成员资格。
 * <B>多个 MulticastSocket</B> 可以同时订阅多播组和端口，并且它们都会收到组数据报。
 * <P>
 * 当前，小程序不允许使用多播套接字。
 *
 * @author Pavani Diwanji
 * @since  JDK1.1
 */
public
class MulticastSocket extends DatagramSocket {

    /**
     * 用于记录某些平台上此套接字是否已设置出站接口。
     */
    private boolean interfaceSet;

    /**
     * 创建一个多播套接字。
     *
     * <p>如果有安全经理，
     * 其 {@code checkListen} 方法首先被调用
     * 以 0 作为其参数，以确保操作被允许。
     * 这可能导致 SecurityException。
     * <p>
     * 创建套接字时，
     * {@link DatagramSocket#setReuseAddress(boolean)} 方法被
     * 调用以启用 SO_REUSEADDR 套接字选项。
     *
     * @exception IOException 如果在创建 MulticastSocket 时发生 I/O 异常
     * @exception  SecurityException  如果存在安全经理且其
     *             {@code checkListen} 方法不允许操作。
     * @see SecurityManager#checkListen
     * @see java.net.DatagramSocket#setReuseAddress(boolean)
     */
    public MulticastSocket() throws IOException {
        this(new InetSocketAddress(0));
    }

    /**
     * 创建一个多播套接字并将其绑定到特定端口。
     *
     * <p>如果有安全经理，
     * 其 {@code checkListen} 方法首先被调用
     * 以 {@code port} 参数
     * 作为其参数，以确保操作被允许。
     * 这可能导致 SecurityException。
     * <p>
     * 创建套接字时，
     * {@link DatagramSocket#setReuseAddress(boolean)} 方法被
     * 调用以启用 SO_REUSEADDR 套接字选项。
     *
     * @param port 要使用的端口
     * @exception IOException 如果在创建 MulticastSocket 时发生 I/O 异常
     * @exception  SecurityException  如果存在安全经理且其
     *             {@code checkListen} 方法不允许操作。
     * @see SecurityManager#checkListen
     * @see java.net.DatagramSocket#setReuseAddress(boolean)
     */
    public MulticastSocket(int port) throws IOException {
        this(new InetSocketAddress(port));
    }

    /**
     * 创建一个绑定到指定套接字地址的 MulticastSocket。
     * <p>
     * 或者，如果地址为 {@code null}，则创建一个未绑定的套接字。
     *
     * <p>如果有安全经理，
     * 其 {@code checkListen} 方法首先被调用
     * 以 SocketAddress 端口作为其参数，以确保操作被允许。
     * 这可能导致 SecurityException。
     * <p>
     * 创建套接字时，
     * {@link DatagramSocket#setReuseAddress(boolean)} 方法被
     * 调用以启用 SO_REUSEADDR 套接字选项。
     *
     * @param bindaddr 要绑定的套接字地址，或 {@code null} 用于
     *                 未绑定的套接字。
     * @exception IOException 如果在创建 MulticastSocket 时发生 I/O 异常
     * @exception  SecurityException  如果存在安全经理且其
     *             {@code checkListen} 方法不允许操作。
     * @see SecurityManager#checkListen
     * @see java.net.DatagramSocket#setReuseAddress(boolean)
     *
     * @since 1.4
     */
    public MulticastSocket(SocketAddress bindaddr) throws IOException {
        super((SocketAddress) null);

        // 在绑定之前启用 SO_REUSEADDR
        setReuseAddress(true);

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
     * 套接字的 TTL 锁。用于 set/getTTL 和
     * send(packet,ttl)。
     */
    private Object ttlLock = new Object();

    /**
     * 套接字接口的锁 - 用于 setInterface
     * 和 getInterface
     */
    private Object infLock = new Object();

    /**
     * 通过 setInterface 在此 MulticastSocket 上设置的“最后一个”接口
     */
    private InetAddress infAddress = null;


    /**
     * 设置此 {@code MulticastSocket} 发送的多播数据包的默认生存时间，以控制多播的范围。
     *
     * <p>TTL 是一个 <b>无符号</b> 8 位量，因此 <B>必须</B>
     * 在 {@code 0 <= ttl <= 0xFF } 范围内。
     *
     * @param ttl 生存时间
     * @exception IOException 如果在设置默认生存时间值时发生 I/O 异常
     * @deprecated 使用 setTimeToLive 方法代替，该方法使用
     * <b>int</b> 而不是 <b>byte</b> 作为 ttl 的类型。
     * @see #getTTL()
     */
    @Deprecated
    public void setTTL(byte ttl) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setTTL(ttl);
    }

    /**
     * 设置此 {@code MulticastSocket} 发送的多播数据包的默认生存时间，以控制多播的范围。
     *
     * <P> TTL <B>必须</B> 在 {@code  0 <= ttl <=
     * 255} 范围内，否则将抛出 {@code IllegalArgumentException}。
     * 使用 TTL 为 {@code 0} 发送的多播数据包不会在网络上传输，但可能会本地交付。
     *
     * @param  ttl
     *         生存时间
     *
     * @throws  IOException
     *          如果在设置默认生存时间值时发生 I/O 异常
     *
     * @see #getTimeToLive()
     */
    public void setTimeToLive(int ttl) throws IOException {
        if (ttl < 0 || ttl > 255) {
            throw new IllegalArgumentException("ttl out of range");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setTimeToLive(ttl);
    }

    /**
     * 获取从套接字发送的多播数据包的默认生存时间。
     *
     * @exception IOException 如果在获取默认生存时间值时发生 I/O 异常
     * @return 默认生存时间值
     * @deprecated 使用 getTimeToLive 方法代替，该方法返回
     * 一个 <b>int</b> 而不是一个 <b>byte</b>。
     * @see #setTTL(byte)
     */
    @Deprecated
    public byte getTTL() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return getImpl().getTTL();
    }

    /**
     * 获取从套接字发送的多播数据包的默认生存时间。
     * @exception IOException 如果在获取默认生存时间值时发生 I/O 异常
     * @return 默认生存时间值
     * @see #setTimeToLive(int)
     */
    public int getTimeToLive() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return getImpl().getTimeToLive();
    }

    /**
     * 加入多播组。其行为可能受
     * {@code setInterface} 或 {@code setNetworkInterface} 的影响。
     *
     * <p>如果有安全经理，此方法首先
     * 调用其 {@code checkMulticast} 方法
     * 以 {@code mcastaddr} 参数
     * 作为其参数。
     *
     * @param mcastaddr 要加入的多播地址
     *
     * @exception IOException 如果在加入时发生错误
     * 或地址不是多播地址。
     * @exception  SecurityException  如果存在安全经理且其
     * {@code checkMulticast} 方法不允许加入。
     *
     * @see SecurityManager#checkMulticast(InetAddress)
     */
    public void joinGroup(InetAddress mcastaddr) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        checkAddress(mcastaddr, "joinGroup");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(mcastaddr);
        }

        if (!mcastaddr.isMulticastAddress()) {
            throw new SocketException("Not a multicast address");
        }

        /**
         * 对于某些平台，需要先设置接口才能加入组。
         */
        NetworkInterface defaultInterface = NetworkInterface.getDefault();

        if (!interfaceSet && defaultInterface != null) {
            setNetworkInterface(defaultInterface);
        }

        getImpl().join(mcastaddr);
    }

    /**
     * 离开多播组。其行为可能受
     * {@code setInterface} 或 {@code setNetworkInterface} 的影响。
     *
     * <p>如果有安全经理，此方法首先
     * 调用其 {@code checkMulticast} 方法
     * 以 {@code mcastaddr} 参数
     * 作为其参数。
     *
     * @param mcastaddr 要离开的多播地址
     * @exception IOException 如果在离开时发生错误
     * 或地址不是多播地址。
     * @exception  SecurityException  如果存在安全经理且其
     * {@code checkMulticast} 方法不允许操作。
     *
     * @see SecurityManager#checkMulticast(InetAddress)
     */
    public void leaveGroup(InetAddress mcastaddr) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        checkAddress(mcastaddr, "leaveGroup");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(mcastaddr);
        }

        if (!mcastaddr.isMulticastAddress()) {
            throw new SocketException("Not a multicast address");
        }

        getImpl().leave(mcastaddr);
    }

    /**
     * 加入指定的多播组和指定的接口。
     *
     * <p>如果有安全经理，此方法首先
     * 调用其 {@code checkMulticast} 方法
     * 以 {@code mcastaddr} 参数
     * 作为其参数。
     *
     * @param mcastaddr 要加入的多播地址
     * @param netIf 指定接收多播数据报包的本地接口，或 <i>null</i> 以使用
     *       {@link MulticastSocket#setInterface(InetAddress)} 或
     *       {@link MulticastSocket#setNetworkInterface(NetworkInterface)} 设置的接口
     *
     * @exception IOException 如果在加入时发生错误
     * 或地址不是多播地址。
     * @exception  SecurityException  如果存在安全经理且其
     * {@code checkMulticast} 方法不允许加入。
     * @throws  IllegalArgumentException 如果 mcastaddr 为 null 或是此套接字不支持的 SocketAddress 子类
     *
     * @see SecurityManager#checkMulticast(InetAddress)
     * @since 1.4
     */
    public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");

        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");

        if (oldImpl)
            throw new UnsupportedOperationException();

        checkAddress(((InetSocketAddress)mcastaddr).getAddress(), "joinGroup");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(((InetSocketAddress)mcastaddr).getAddress());
        }

        if (!((InetSocketAddress)mcastaddr).getAddress().isMulticastAddress()) {
            throw new SocketException("Not a multicast address");
        }

        getImpl().joinGroup(mcastaddr, netIf);
    }

    /**
     * 在指定的本地接口上离开多播组。
     *
     * <p>如果有安全经理，此方法首先
     * 调用其 {@code checkMulticast} 方法
     * 以 {@code mcastaddr} 参数
     * 作为其参数。
     *
     * @param mcastaddr 要离开的多播地址
     * @param netIf 指定的本地接口或 <i>null</i> 以使用
     *             {@link MulticastSocket#setInterface(InetAddress)} 或
     *             {@link MulticastSocket#setNetworkInterface(NetworkInterface)} 设置的接口
     * @exception IOException 如果在离开时发生错误
     * 或地址不是多播地址。
     * @exception  SecurityException  如果存在安全经理且其
     * {@code checkMulticast} 方法不允许操作。
     * @throws  IllegalArgumentException 如果 mcastaddr 为 null 或是此套接字不支持的 SocketAddress 子类
     *
     * @see SecurityManager#checkMulticast(InetAddress)
     * @since 1.4
     */
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");


/**
 * 指定用于发送多播数据报的网络接口。
 *
 * @param netIf 接口
 * @exception SocketException 如果底层协议发生错误，例如TCP错误。
 * @see #getNetworkInterface()
 * @since 1.4
 */
public void setNetworkInterface(NetworkInterface netIf)
    throws SocketException {

    synchronized (infLock) {
        getImpl().setOption(SocketOptions.IP_MULTICAST_IF2, netIf);
        infAddress = null;
        interfaceSet = true;
    }
}

/**
 * 获取设置的多播网络接口。
 *
 * @exception SocketException 如果底层协议发生错误，例如TCP错误。
 * @return 当前设置的多播 {@code NetworkInterface}
 * @see #setNetworkInterface(NetworkInterface)
 * @since 1.4
 */
public NetworkInterface getNetworkInterface() throws SocketException {
    NetworkInterface ni
        = (NetworkInterface)getImpl().getOption(SocketOptions.IP_MULTICAST_IF2);
    if ((ni.getIndex() == 0) || (ni.getIndex() == -1)) {
        InetAddress[] addrs = new InetAddress[1];
        addrs[0] = InetAddress.anyLocalAddress();
        return new NetworkInterface(addrs[0].getHostName(), 0, addrs);
    } else {
        return ni;
    }
}

/**
 * 禁用/启用多播数据报的本地回环。
 * 该选项用于提示平台的网络代码，设置多播数据是否将回环到本地套接字。
 *
 * <p>因为这是一个提示，应用程序如果想验证回环模式的设置，应该调用
 * {@link #getLoopbackMode()}
 * @param disable {@code true} 禁用回环模式
 * @throws SocketException 如果设置值时发生错误
 * @since 1.4
 * @see #getLoopbackMode
 */
public void setLoopbackMode(boolean disable) throws SocketException {
    getImpl().setOption(SocketOptions.IP_MULTICAST_LOOP, Boolean.valueOf(disable));
}

/**
 * 获取多播数据报的本地回环设置。
 *
 * @throws SocketException 如果获取值时发生错误
 * @return 如果回环模式已禁用，则返回 true
 * @since 1.4
 * @see #setLoopbackMode
 */
public boolean getLoopbackMode() throws SocketException {
    return ((Boolean)getImpl().getOption(SocketOptions.IP_MULTICAST_LOOP)).booleanValue();
}

/**
 * 将数据报包发送到目的地，使用不同于套接字默认值的TTL（生存时间）。
 * 仅在需要特定TTL的情况下才需要使用此方法；否则，最好在套接字上设置一次TTL，
 * 并使用该默认TTL发送所有数据包。此方法不会更改套接字的默认TTL。其行为可能受 {@code setInterface} 影响。
 *
 * <p>如果有安全经理，此方法首先执行一些安全检查。首先，如果 {@code p.getAddress().isMulticastAddress()}
 * 为 true，此方法将调用安全经理的 {@code checkMulticast} 方法
 * 并将 {@code p.getAddress()} 和 {@code ttl} 作为参数。如果该表达式的评估结果为 false，
 * 此方法将调用安全经理的 {@code checkConnect} 方法，参数为
 * {@code p.getAddress().getHostAddress()} 和 {@code p.getPort()}。每次调用安全经理方法
 * 都可能导致 SecurityException，如果操作不允许的话。
 *
 * @param p 要发送的数据包。数据包应包含目标多播IP地址和要发送的数据。
 * 无需成为组的成员即可向目标多播地址发送数据包。
 * @param ttl 多播数据包的可选生存时间。默认TTL为1。
 *
 * @exception IOException 如果发生错误，例如设置TTL时出错。
 * @exception  SecurityException 如果存在安全经理，并且其
 *             {@code checkMulticast} 或 {@code checkConnect}
 *             方法不允许发送。
 *
 * @deprecated 请改用以下代码或其等效代码：
 *  ......
 *  int ttl = mcastSocket.getTimeToLive();
 *  mcastSocket.setTimeToLive(newttl);
 *  mcastSocket.send(p);
 *  mcastSocket.setTimeToLive(ttl);
 *  ......
 *
 * @see DatagramSocket#send
 * @see DatagramSocket#receive
 * @see SecurityManager#checkMulticast(java.net.InetAddress, byte)
 * @see SecurityManager#checkConnect
 */
@Deprecated
public void send(DatagramPacket p, byte ttl)
    throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        checkAddress(p.getAddress(), "send");
        synchronized(ttlLock) {
            synchronized(p) {
                if (connectState == ST_NOT_CONNECTED) {
                    // 安全经理确保多播地址
                    // 是允许的，并且使用的ttl小于
                    // 允许的最大ttl。
                    SecurityManager security = System.getSecurityManager();
                    if (security != null) {
                        if (p.getAddress().isMulticastAddress()) {
                            security.checkMulticast(p.getAddress(), ttl);
                        } else {
                            security.checkConnect(p.getAddress().getHostAddress(),
                                                  p.getPort());
                        }
                    }
                } else {
                    // 已连接
                    InetAddress packetAddress = null;
                    packetAddress = p.getAddress();
                    if (packetAddress == null) {
                        p.setAddress(connectedAddress);
                        p.setPort(connectedPort);
                    } else if ((!packetAddress.equals(connectedAddress)) ||
                               p.getPort() != connectedPort) {
                        throw new SecurityException("connected address and packet address" +
                                                    " differ");
                    }
                }
                byte dttl = getTTL();
                try {
                    if (ttl != dttl) {
                        // 设置ttl
                        getImpl().setTTL(ttl);
                    }
                    // 调用数据报方法发送
                    getImpl().send(p);
                } finally {
                    // 恢复默认值
                    if (ttl != dttl) {
                        getImpl().setTTL(dttl);
                    }
                }
            } // synch p
        }  //synch ttl
    } //method
}
