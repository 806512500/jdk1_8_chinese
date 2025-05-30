/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.security.AccessController;
import sun.net.ResourceManager;

/**
 * 抽象数据报和多播套接字实现基类。
 * 注意：这不是一个公共类，因此应用程序不能直接调用实现，从而不能绕过 DatagramSocket 和 MulticastSocket
 * 类中的安全检查。
 *
 * @author Pavani Diwanji
 */

abstract class AbstractPlainDatagramSocketImpl extends DatagramSocketImpl
{
    /* 用于接收的超时值 */
    int timeout = 0;
    boolean connected = false;
    private int trafficClass = 0;
    protected InetAddress connectedAddress = null;
    private int connectedPort = -1;

    private static final String os = AccessController.doPrivileged(
        new sun.security.action.GetPropertyAction("os.name")
    );

    /**
     * 标志，如果禁用原生 connect() 调用
     */
    private final static boolean connectDisabled = os.contains("OS X");

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
     * 创建数据报套接字
     */
    protected synchronized void create() throws SocketException {
        ResourceManager.beforeUdpCreate();
        fd = new FileDescriptor();
        try {
            datagramSocketCreate();
        } catch (SocketException ioe) {
            ResourceManager.afterUdpClose();
            fd = null;
            throw ioe;
        }
    }

    /**
     * 将数据报套接字绑定到本地端口。
     */
    protected synchronized void bind(int lport, InetAddress laddr)
        throws SocketException {
        bind0(lport, laddr);
    }

    protected abstract void bind0(int lport, InetAddress laddr)
        throws SocketException;

    /**
     * 发送数据报包。包包含数据和要发送包的目标地址。
     * @param p 要发送的包。
     */
    protected abstract void send(DatagramPacket p) throws IOException;

    /**
     * 将数据报套接字连接到远程目标。这将远程地址与本地套接字关联，以便数据报只能发送到此目标并从该目标接收。
     * @param address 要连接的远程 InetAddress
     * @param port 远程端口号
     */
    protected void connect(InetAddress address, int port) throws SocketException {
        connect0(address, port);
        connectedAddress = address;
        connectedPort = port;
        connected = true;
    }

    /**
     * 断开先前连接的套接字。如果套接字尚未连接，则不执行任何操作。
     */
    protected void disconnect() {
        disconnect0(connectedAddress.holder().getFamily());
        connected = false;
        connectedAddress = null;
        connectedPort = -1;
    }

    /**
     * 查看包的发送者。
     * @param i 用于填充发送者地址的地址
     */
    protected abstract int peek(InetAddress i) throws IOException;
    protected abstract int peekData(DatagramPacket p) throws IOException;
    /**
     * 接收数据报包。
     * @param p 接收数据的包
     */
    protected synchronized void receive(DatagramPacket p)
        throws IOException {
        receive0(p);
    }

    protected abstract void receive0(DatagramPacket p)
        throws IOException;

    /**
     * 设置 TTL（生存时间）选项。
     * @param ttl 要设置的 TTL。
     */
    protected abstract void setTimeToLive(int ttl) throws IOException;

    /**
     * 获取 TTL（生存时间）选项。
     */
    protected abstract int getTimeToLive() throws IOException;

    /**
     * 设置 TTL（生存时间）选项。
     * @param ttl 要设置的 TTL。
     */
    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    /**
     * 获取 TTL（生存时间）选项。
     */
    @Deprecated
    protected abstract byte getTTL() throws IOException;

    /**
     * 加入多播组。
     * @param inetaddr 要加入的多播地址。
     */
    protected void join(InetAddress inetaddr) throws IOException {
        join(inetaddr, null);
    }

    /**
     * 离开多播组。
     * @param inetaddr 要离开的多播地址。
     */
    protected void leave(InetAddress inetaddr) throws IOException {
        leave(inetaddr, null);
    }
    /**
     * 加入多播组。
     * @param mcastaddr 要加入的多播地址。
     * @param netIf 指定接收多播数据包的本地接口
     * @throws  IllegalArgumentException 如果 mcastaddr 为 null 或是此套接字不支持的 SocketAddress 子类
     * @since 1.4
     */

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        join(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    protected abstract void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    /**
     * 离开多播组。
     * @param mcastaddr  要离开的多播地址。
     * @param netIf 指定离开组的本地接口
     * @throws  IllegalArgumentException 如果 mcastaddr 为 null 或是此套接字不支持的 SocketAddress 子类
     * @since 1.4
     */
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        leave(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    protected abstract void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    /**
     * 关闭套接字。
     */
    protected void close() {
        if (fd != null) {
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            fd = null;
        }
    }

    protected boolean isClosed() {
        return (fd == null) ? true : false;
    }

    protected void finalize() {
        close();
    }

    /**
     * 设置一个值 - 由于我们只支持（设置）二进制选项，因此 o 必须是 Boolean
     */

     public void setOption(int optID, Object o) throws SocketException {
         if (isClosed()) {
             throw new SocketException("Socket Closed");
         }
         switch (optID) {
            /* 在调用原生方法之前检查类型安全。这些检查不应该失败，因为只有 java.Socket* 有权限访问
             * PlainSocketImpl.setOption()。
             */
         case SO_TIMEOUT:
             if (o == null || !(o instanceof Integer)) {
                 throw new SocketException("bad argument for SO_TIMEOUT");
             }
             int tmp = ((Integer) o).intValue();
             if (tmp < 0)
                 throw new IllegalArgumentException("timeout < 0");
             timeout = tmp;
             return;
         case IP_TOS:
             if (o == null || !(o instanceof Integer)) {
                 throw new SocketException("bad argument for IP_TOS");
             }
             trafficClass = ((Integer)o).intValue();
             break;
         case SO_REUSEADDR:
             if (o == null || !(o instanceof Boolean)) {
                 throw new SocketException("bad argument for SO_REUSEADDR");
             }
             break;
         case SO_BROADCAST:
             if (o == null || !(o instanceof Boolean)) {
                 throw new SocketException("bad argument for SO_BROADCAST");
             }
             break;
         case SO_BINDADDR:
             throw new SocketException("Cannot re-bind Socket");
         case SO_RCVBUF:
         case SO_SNDBUF:
             if (o == null || !(o instanceof Integer) ||
                 ((Integer)o).intValue() < 0) {
                 throw new SocketException("bad argument for SO_SNDBUF or " +
                                           "SO_RCVBUF");
             }
             break;
         case IP_MULTICAST_IF:
             if (o == null || !(o instanceof InetAddress))
                 throw new SocketException("bad argument for IP_MULTICAST_IF");
             break;
         case IP_MULTICAST_IF2:
             if (o == null || !(o instanceof NetworkInterface))
                 throw new SocketException("bad argument for IP_MULTICAST_IF2");
             break;
         case IP_MULTICAST_LOOP:
             if (o == null || !(o instanceof Boolean))
                 throw new SocketException("bad argument for IP_MULTICAST_LOOP");
             break;
         default:
             throw new SocketException("invalid option: " + optID);
         }
         socketSetOption(optID, o);
     }

    /*
     * 获取选项的状态 - 已设置或未设置
     */

    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }

        Object result;

        switch (optID) {
            case SO_TIMEOUT:
                result = new Integer(timeout);
                break;

            case IP_TOS:
                result = socketGetOption(optID);
                if ( ((Integer)result).intValue() == -1) {
                    result = new Integer(trafficClass);
                }
                break;

            case SO_BINDADDR:
            case IP_MULTICAST_IF:
            case IP_MULTICAST_IF2:
            case SO_RCVBUF:
            case SO_SNDBUF:
            case IP_MULTICAST_LOOP:
            case SO_REUSEADDR:
            case SO_BROADCAST:
                result = socketGetOption(optID);
                break;

            default:
                throw new SocketException("invalid option: " + optID);
        }

        return result;
    }

    protected abstract void datagramSocketCreate() throws SocketException;
    protected abstract void datagramSocketClose();
    protected abstract void socketSetOption(int opt, Object val)
        throws SocketException;
    protected abstract Object socketGetOption(int opt) throws SocketException;

    protected abstract void connect0(InetAddress address, int port) throws SocketException;
    protected abstract void disconnect0(int family);

    protected boolean nativeConnectDisabled() {
        return connectDisabled;
    }

    abstract int dataAvailable();
}
