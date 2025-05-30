/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;

/**
 *
 * 该类实现了一个 IP 套接字地址（IP 地址 + 端口号）
 * 它也可以是一对（主机名 + 端口号），在这种情况下将尝试解析主机名。如果解析失败，则该地址被认为是 <I>未解析的</I>，但在某些情况下（如通过代理连接）仍然可以使用。
 * <p>
 * 它提供了一个不可变对象，用于套接字的绑定、连接或作为返回值。
 * <p>
 * <i>通配符</i>是一个特殊的本地 IP 地址。它通常表示“任何”，只能用于 {@code bind} 操作。
 *
 * @see java.net.Socket
 * @see java.net.ServerSocket
 * @since 1.4
 */
public class InetSocketAddress
    extends SocketAddress
{
    // 私有实现类，由所有公共方法指向。
    private static class InetSocketAddressHolder {
        // 套接字地址的主机名
        private String hostname;
        // 套接字地址的 IP 地址
        private InetAddress addr;
        // 套接字地址的端口号
        private int port;

        private InetSocketAddressHolder(String hostname, InetAddress addr, int port) {
            this.hostname = hostname;
            this.addr = addr;
            this.port = port;
        }

        private int getPort() {
            return port;
        }

        private InetAddress getAddress() {
            return addr;
        }

        private String getHostName() {
            if (hostname != null)
                return hostname;
            if (addr != null)
                return addr.getHostName();
            return null;
        }

        private String getHostString() {
            if (hostname != null)
                return hostname;
            if (addr != null) {
                if (addr.holder().getHostName() != null)
                    return addr.holder().getHostName();
                else
                    return addr.getHostAddress();
            }
            return null;
        }

        private boolean isUnresolved() {
            return addr == null;
        }

        @Override
        public String toString() {
            if (isUnresolved()) {
                return hostname + ":" + port;
            } else {
                return addr.toString() + ":" + port;
            }
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null || !(obj instanceof InetSocketAddressHolder))
                return false;
            InetSocketAddressHolder that = (InetSocketAddressHolder)obj;
            boolean sameIP;
            if (addr != null)
                sameIP = addr.equals(that.addr);
            else if (hostname != null)
                sameIP = (that.addr == null) &&
                    hostname.equalsIgnoreCase(that.hostname);
            else
                sameIP = (that.addr == null) && (that.hostname == null);
            return sameIP && (port == that.port);
        }

        @Override
        public final int hashCode() {
            if (addr != null)
                return addr.hashCode() + port;
            if (hostname != null)
                return hostname.toLowerCase().hashCode() + port;
            return port;
        }
    }

    private final transient InetSocketAddressHolder holder;

    private static final long serialVersionUID = 5076001401234631237L;

    private static int checkPort(int port) {
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException("端口超出范围:" + port);
        return port;
    }

    private static String checkHost(String hostname) {
        if (hostname == null)
            throw new IllegalArgumentException("主机名不能为 null");
        return hostname;
    }

    /**
     * 创建一个套接字地址，其中 IP 地址是通配符地址，端口号是指定的值。
     * <p>
     * 有效的端口值在 0 到 65535 之间。
     * 端口号为 {@code 零} 将在 {@code bind} 操作中让系统选择一个临时端口。
     * <p>
     * @param   port    端口号
     * @throws IllegalArgumentException 如果端口参数超出指定的有效端口值范围。
     */
    public InetSocketAddress(int port) {
        this(InetAddress.anyLocalAddress(), port);
    }

    /**
     *
     * 从 IP 地址和端口号创建一个套接字地址。
     * <p>
     * 有效的端口值在 0 到 65535 之间。
     * 端口号为 {@code 零} 将在 {@code bind} 操作中让系统选择一个临时端口。
     * <P>
     * {@code null} 地址将分配 <i>通配符</i> 地址。
     * <p>
     * @param   addr    IP 地址
     * @param   port    端口号
     * @throws IllegalArgumentException 如果端口参数超出指定的有效端口值范围。
     */
    public InetSocketAddress(InetAddress addr, int port) {
        holder = new InetSocketAddressHolder(
                        null,
                        addr == null ? InetAddress.anyLocalAddress() : addr,
                        checkPort(port));
    }

    /**
     *
     * 从主机名和端口号创建一个套接字地址。
     * <p>
     * 将尝试将主机名解析为 InetAddress。如果该尝试失败，则该地址将被标记为 <I>未解析的</I>。
     * <p>
     * 如果存在安全经理，其 {@code checkConnect} 方法将被调用，以主机名为参数，以检查解析它的权限。这可能导致 SecurityException。
     * <P>
     * 有效的端口值在 0 到 65535 之间。
     * 端口号为 {@code 零} 将在 {@code bind} 操作中让系统选择一个临时端口。
     * <P>
     * @param   hostname 主机名
     * @param   port    端口号
     * @throws IllegalArgumentException 如果端口参数超出有效端口值范围，或者主机名参数为 <TT>null</TT>。
     * @throws SecurityException 如果存在安全经理，并且拒绝解析主机名的权限。
     * @see     #isUnresolved()
     */
    public InetSocketAddress(String hostname, int port) {
        checkHost(hostname);
        InetAddress addr = null;
        String host = null;
        try {
            addr = InetAddress.getByName(hostname);
        } catch(UnknownHostException e) {
            host = hostname;
        }
        holder = new InetSocketAddressHolder(host, addr, checkPort(port));
    }

    // 用于创建未解析实例的私有构造函数
    private InetSocketAddress(int port, String hostname) {
        holder = new InetSocketAddressHolder(hostname, null, port);
    }

    /**
     *
     * 从主机名和端口号创建一个未解析的套接字地址。
     * <p>
     * 不会尝试将主机名解析为 InetAddress。
     * 该地址将被标记为 <I>未解析的</I>。
     * <p>
     * 有效的端口值在 0 到 65535 之间。
     * 端口号为 {@code 零} 将在 {@code bind} 操作中让系统选择一个临时端口。
     * <P>
     * @param   host    主机名
     * @param   port    端口号
     * @throws IllegalArgumentException 如果端口参数超出有效端口值范围，或者主机名参数为 <TT>null</TT>。
     * @see     #isUnresolved()
     * @return  代表未解析套接字地址的 {@code InetSocketAddress}
     * @since 1.5
     */
    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(checkPort(port), checkHost(host));
    }

    /**
     * @serialField hostname String
     * @serialField addr InetAddress
     * @serialField port int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
         new ObjectStreamField("hostname", String.class),
         new ObjectStreamField("addr", InetAddress.class),
         new ObjectStreamField("port", int.class)};

    private void writeObject(ObjectOutputStream out)
        throws IOException
    {
        // 不调用 defaultWriteObject()
         ObjectOutputStream.PutField pfields = out.putFields();
         pfields.put("hostname", holder.hostname);
         pfields.put("addr", holder.addr);
         pfields.put("port", holder.port);
         out.writeFields();
     }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // 不调用 defaultReadObject()
        ObjectInputStream.GetField oisFields = in.readFields();
        final String oisHostname = (String)oisFields.get("hostname", null);
        final InetAddress oisAddr = (InetAddress)oisFields.get("addr", null);
        final int oisPort = oisFields.get("port", -1);

        // 检查我们的不变性是否满足
        checkPort(oisPort);
        if (oisHostname == null && oisAddr == null)
            throw new InvalidObjectException("hostname 和 addr " +
                                             "不能同时为 null");

        InetSocketAddressHolder h = new InetSocketAddressHolder(oisHostname,
                                                                oisAddr,
                                                                oisPort);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    private void readObjectNoData()
        throws ObjectStreamException
    {
        throw new InvalidObjectException("需要流数据");
    }

    private static final long FIELDS_OFFSET;
    private static final sun.misc.Unsafe UNSAFE;
    static {
        try {
            sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(
                    InetSocketAddress.class.getDeclaredField("holder"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    /**
     * 获取端口号。
     *
     * @return 端口号。
     */
    public final int getPort() {
        return holder.getPort();
    }

    /**
     *
     * 获取 {@code InetAddress}。
     *
     * @return InetAdress 或 {@code null} 如果未解析。
     */
    public final InetAddress getAddress() {
        return holder.getAddress();
    }

    /**
     * 获取 {@code hostname}。
     * 注意：如果地址是使用字面 IP 地址创建的，此方法可能会触发名称服务反向查找。
     *
     * @return 地址的主机名部分。
     */
    public final String getHostName() {
        return holder.getHostName();
    }

    /**
     * 返回主机名，或如果地址没有主机名（它是使用字面值创建的），则返回地址的字符串形式。
     * 这样做的好处是 <b>不</b> 尝试反向查找。
     *
     * @return 主机名，或地址的字符串表示形式。
     * @since 1.7
     */
    public final String getHostString() {
        return holder.getHostString();
    }

    /**
     * 检查地址是否已解析。
     *
     * @return 如果主机名无法解析为 {@code InetAddress}，则返回 {@code true}。
     */
    public final boolean isUnresolved() {
        return holder.isUnresolved();
    }

    /**
     * 构造此 InetSocketAddress 的字符串表示形式。
     * 该字符串通过调用 InetAddress 的 toString() 方法并连接端口号（用冒号分隔）来构造。如果地址未解析，则冒号前的部分仅包含主机名。
     *
     * @return 该对象的字符串表示形式。
     */
    @Override
    public String toString() {
        return holder.toString();
    }

    /**
     * 将此对象与指定的对象进行比较。
     * 结果为 {@code true} 当且仅当参数不为 {@code null} 并且表示与
     * 此对象相同的地址。
     * <p>
     * 两个 {@code InetSocketAddress} 实例表示相同的地址，当且仅当 InetAddresses（如果未解析则为主机名）和端口号相等。
     * 如果两个地址都未解析，则比较主机名和端口号。
     *
     * 注意：主机名不区分大小写。例如 "FooBar" 和 "foobar" 被认为是相等的。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see java.net.InetAddress#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InetSocketAddress))
            return false;
        return holder.equals(((InetSocketAddress) obj).holder);
    }

    /**
     * 返回此套接字地址的哈希码。
     *
     * @return 该套接字地址的哈希码值。
     */
    @Override
    public final int hashCode() {
        return holder.hashCode();
    }
}
