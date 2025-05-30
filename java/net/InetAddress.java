
/*
 * Copyright (c) 1995, 2023, Oracle and/or its affiliates. All rights reserved.
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

import java.util.NavigableSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.security.AccessController;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import sun.security.action.*;
import sun.net.InetAddressCachePolicy;
import sun.net.util.IPAddressUtil;
import sun.net.spi.nameservice.*;

/**
 * 该类表示一个互联网协议 (IP) 地址。
 *
 * <p> IP 地址是一个 32 位或 128 位的无符号数字，用于 IP 协议，这是一个较低级别的协议，UDP 和 TCP 等协议都是基于它构建的。IP 地址架构由 <a
 * href="http://www.ietf.org/rfc/rfc790.txt"><i>RFC&nbsp;790:
 * 已分配的数字</i></a>、<a
 * href="http://www.ietf.org/rfc/rfc1918.txt"> <i>RFC&nbsp;1918:
 * 私有互联网的地址分配</i></a>、<a
 * href="http://www.ietf.org/rfc/rfc2365.txt"><i>RFC&nbsp;2365:
 * 管理范围内的 IP 组播</i></a> 和 <a
 * href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC&nbsp;2373: IP
 * 第 6 版地址架构</i></a> 定义。一个 InetAddress 实例由一个 IP 地址及其对应的主机名（如果在构造时提供了主机名或已经进行了反向主机名解析）组成。
 *
 * <h3> 地址类型 </h3>
 *
 * <blockquote><table cellspacing=2 summary="单播和组播地址类型的描述">
 *   <tr><th valign=top><i>单播</i></th>
 *       <td>单个接口的标识符。发送到单播地址的数据包将被传递到该地址标识的接口。
 *
 *         <p> 未指定地址 -- 也称为 anylocal 或通配符地址。它不能分配给任何节点。它表示地址的缺失。一个示例用法是在绑定时作为目标地址，允许服务器在任何接口上接受客户端连接，前提是服务器主机有多个接口。
 *
 *         <p> 未指定地址不得用作 IP 数据包的目标地址。
 *
 *         <p> <i>回环</i> 地址 -- 这是分配给回环接口的地址。发送到此 IP 地址的任何内容都会在本地主机上循环并成为 IP 输入。此地址通常用于测试客户端。</td></tr>
 *   <tr><th valign=top><i>组播</i></th>
 *       <td>一组接口（通常属于不同的节点）的标识符。发送到组播地址的数据包将被传递到该地址标识的所有接口。</td></tr>
 * </table></blockquote>
 *
 * <h4> IP 地址范围 </h4>
 *
 * <p> <i>链路本地</i> 地址设计用于单个链路上的地址分配，例如自动地址配置、邻居发现或没有路由器时的使用。
 *
 * <p> <i>站点本地</i> 地址设计用于站点内部的地址分配，而无需全局前缀。
 *
 * <p> <i>全局</i> 地址在互联网上是唯一的。
 *
 * <h4> IP 地址的文本表示 </h4>
 *
 * IP 地址的文本表示是地址族特定的。
 *
 * <p>
 *
 * 对于 IPv4 地址格式，请参阅 <A
 * HREF="Inet4Address.html#format">Inet4Address#format</A>；对于 IPv6
 * 地址格式，请参阅 <A
 * HREF="Inet6Address.html#format">Inet6Address#format</A>。
 *
 * <P>有一些 <a href="doc-files/net-properties.html#Ipv4IPv6">系统属性</a> 影响 IPv4 和 IPv6 地址的使用。</P>
 *
 * <h4> 主机名解析 </h4>
 *
 * 主机名到 IP 地址的 <i>解析</i> 通过使用本地机器配置信息和网络命名服务（如域名系统 (DNS) 和网络信息服务 (NIS)）的组合来完成。使用的特定命名服务默认为本地机器配置的命名服务。对于任何主机名，都会返回其对应的 IP 地址。
 *
 * <p> <i>反向名称解析</i> 意味着对于任何 IP 地址，都会返回与该 IP 地址关联的主机。
 *
 * <p> InetAddress 类提供了将主机名解析为其 IP 地址以及反之的方法。
 *
 * <h4> InetAddress 缓存 </h4>
 *
 * InetAddress 类有一个缓存，用于存储成功的和不成功的主机名解析结果。
 *
 * <p> 默认情况下，当安装了安全经理时，为了防止 DNS 欺骗攻击，成功的主机名解析结果将被永久缓存。当未安装安全经理时，默认行为是缓存条目一段时间（具体时间由实现决定）。不成功的主机名解析结果将被缓存很短的时间（10
 * 秒）以提高性能。
 *
 * <p> 如果不希望使用默认行为，可以通过设置 Java 安全属性来指定不同的正向缓存生存时间 (TTL) 值。同样，系统管理员可以在需要时配置不同的负向缓存 TTL 值。
 *
 * <p> 两个 Java 安全属性控制正向和负向主机名解析缓存的 TTL 值：
 *
 * <blockquote>
 * <dl>
 * <dt><b>networkaddress.cache.ttl</b></dt>
 * <dd>指示从名称服务成功查找名称的缓存策略。值指定为整数，表示成功查找的缓存时间（以秒为单位）。默认设置是缓存一段时间，具体时间由实现决定。
 * <p>
 * 值 -1 表示“永久缓存”。
 * </dd>
 * <dt><b>networkaddress.cache.negative.ttl</b> (默认: 10)</dt>
 * <dd>指示从名称服务不成功查找名称的缓存策略。值指定为整数，表示不成功查找的缓存时间（以秒为单位）。
 * <p>
 * 值 0 表示“永不缓存”。
 * 值 -1 表示“永久缓存”。
 * </dd>
 * </dl>
 * </blockquote>
 *
 * @author  Chris Warth
 * @see     java.net.InetAddress#getByAddress(byte[])
 * @see     java.net.InetAddress#getByAddress(java.lang.String, byte[])
 * @see     java.net.InetAddress#getAllByName(java.lang.String)
 * @see     java.net.InetAddress#getByName(java.lang.String)
 * @see     java.net.InetAddress#getLocalHost()
 * @since JDK1.0
 */
public
class InetAddress implements java.io.Serializable {
    /**
     * 指定地址族：互联网协议第 4 版
     * @since 1.4
     */
    static final int IPv4 = 1;

    /**
     * 指定地址族：互联网协议第 6 版
     * @since 1.4
     */
    static final int IPv6 = 2;

    /* 指定地址族偏好 */
    static transient boolean preferIPv6Address = false;

    static class InetAddressHolder {
        /**
         * 保留应用程序指定的原始主机名。
         *
         * 原始主机名对于基于域的端点识别（参见 RFC 2818 和 RFC 6125）很有用。如果地址是使用原始 IP 地址创建的，反向名称查找可能会通过 DNS 伪造引入端点识别安全问题。
         *
         * Oracle JSSE 提供商通过 sun.misc.JavaNetAccess 使用此原始主机名进行 SSL/TLS 端点识别。
         *
         * 注意：如果需要，将来可能会定义一个新的公共方法。
         */
        String originalHostName;

        InetAddressHolder() {}

        InetAddressHolder(String hostName, int address, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            this.address = address;
            this.family = family;
        }

        void init(String hostName, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            if (family != -1) {
                this.family = family;
            }
        }

        String hostName;

        String getHostName() {
            return hostName;
        }

        String getOriginalHostName() {
            return originalHostName;
        }

        /**
         * 存储 32 位 IPv4 地址。
         */
        int address;

        int getAddress() {
            return address;
        }

        /**
         * 指定地址族类型，例如，'1' 表示 IPv4 地址，'2' 表示 IPv6 地址。
         */
        int family;

        int getFamily() {
            return family;
        }
    }

    /* 用于存储 InetAddress 的可序列化字段 */
    final transient InetAddressHolder holder;

    InetAddressHolder holder() {
        return holder;
    }

    /* 用于存储名称服务提供者 */
    private static List<NameService> nameServices = null;

    /* 用于存储最佳可用主机名 */
    private transient String canonicalHostName = null;

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = 3286316764910316507L;

    /*
     * 加载 net 库到运行时，并执行初始化。
     */
    static {
        preferIPv6Address = java.security.AccessController.doPrivileged(
            new GetBooleanAction("java.net.preferIPv6Addresses")).booleanValue();
        AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });
        init();
    }

    /**
     * 用于 Socket.accept() 方法的构造函数。
     * 这会创建一个空的 InetAddress，由 accept() 方法填充。但是，此 InetAddress 不会被放入地址缓存中，因为它不是通过名称创建的。
     */
    InetAddress() {
        holder = new InetAddressHolder();
    }

    /**
     * 用 Inet4Address 对象替换反序列化的对象。
     *
     * @return 替换反序列化对象的备用对象。
     *
     * @throws ObjectStreamException 如果无法创建替换此对象的新对象
     */
    private Object readResolve() throws ObjectStreamException {
        // 将替换反序列化的 'this' 对象
        return new Inet4Address(holder().getHostName(), holder().getAddress());
    }

    /**
     * 检查 InetAddress 是否为 IP 组播地址的实用程序例程。
     * @return 一个 {@code boolean} 值，指示 InetAddress 是否为 IP 组播地址
     * @since   JDK1.1
     */
    public boolean isMulticastAddress() {
        return false;
    }

    /**
     * 检查 InetAddress 是否为通配符地址的实用程序例程。
     * @return 一个 {@code boolean} 值，指示 InetAddress 是否为通配符地址。
     * @since 1.4
     */
    public boolean isAnyLocalAddress() {
        return false;
    }

    /**
     * 检查 InetAddress 是否为回环地址的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示 InetAddress 是否为回环地址；否则返回 false。
     * @since 1.4
     */
    public boolean isLoopbackAddress() {
        return false;
    }

    /**
     * 检查 InetAddress 是否为链路本地地址的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示 InetAddress 是否为链路本地地址；或如果地址不是链路本地单播地址，则返回 false。
     * @since 1.4
     */
    public boolean isLinkLocalAddress() {
        return false;
    }

    /**
     * 检查 InetAddress 是否为站点本地地址的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示 InetAddress 是否为站点本地地址；或如果地址不是站点本地单播地址，则返回 false。
     * @since 1.4
     */
    public boolean isSiteLocalAddress() {
        return false;
    }

    /**
     * 检查组播地址是否具有全局范围的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为全局范围的组播地址，如果不是全局范围或不是组播地址，则返回 false。
     * @since 1.4
     */
    public boolean isMCGlobal() {
        return false;
    }

    /**
     * 检查组播地址是否具有节点范围的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为节点本地范围的组播地址，如果不是节点本地范围或不是组播地址，则返回 false。
     * @since 1.4
     */
    public boolean isMCNodeLocal() {
        return false;
    }

    /**
     * 检查组播地址是否具有链路范围的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为链路本地范围的组播地址，如果不是链路本地范围或不是组播地址，则返回 false。
     * @since 1.4
     */
    public boolean isMCLinkLocal() {
        return false;
    }

    /**
     * 检查组播地址是否具有站点范围的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为站点本地范围的组播地址，如果不是站点本地范围或不是组播地址，则返回 false。
     * @since 1.4
     */
    public boolean isMCSiteLocal() {
        return false;
    }

    /**
     * 检查组播地址是否具有组织范围的实用程序例程。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为组织本地范围的组播地址，如果不是组织本地范围或不是组播地址，则返回 false。
     * @since 1.4
     */
    public boolean isMCOrgLocal() {
        return false;
    }


    /**
     * 测试该地址是否可达。实现会尽力尝试到达主机，但防火墙和服务器配置可能会阻止请求，导致不可达状态，
     * 而某些特定端口可能仍然可访问。
     * 典型的实现如果可以获得权限，将使用ICMP ECHO REQUEST；否则，它将尝试在目标主机的端口7（Echo）上建立TCP连接。
     * <p>
     * 超时值（以毫秒为单位）表示尝试的最大持续时间。如果操作在超时前未得到响应，主机将被视为不可达。负值将导致抛出IllegalArgumentException。
     *
     * @param   timeout 调用超时前的时间，以毫秒为单位
     * @return 一个表示地址是否可达的{@code boolean}值。
     * @throws IOException 如果发生网络错误
     * @throws  IllegalArgumentException 如果{@code timeout}为负数。
     * @since 1.5
     */
    public boolean isReachable(int timeout) throws IOException {
        return isReachable(null, 0 , timeout);
    }

    /**
     * 测试该地址是否可达。实现会尽力尝试到达主机，但防火墙和服务器配置可能会阻止请求，导致不可达状态，
     * 而某些特定端口可能仍然可访问。
     * 典型的实现如果可以获得权限，将使用ICMP ECHO REQUEST；否则，它将尝试在目标主机的端口7（Echo）上建立TCP连接。
     * <p>
     * {@code network interface}和{@code ttl}参数
     * 允许调用者指定测试将通过的网络接口和数据包应通过的最大跳数。
     * 负值的{@code ttl}将导致抛出IllegalArgumentException。
     * <p>
     * 超时值（以毫秒为单位）表示尝试的最大持续时间。如果操作在超时前未得到响应，主机将被视为不可达。负值将导致抛出IllegalArgumentException。
     *
     * @param   netif   测试将通过的网络接口，或null表示任何接口
     * @param   ttl     最大跳数或0表示默认值
     * @param   timeout 调用超时前的时间，以毫秒为单位
     * @throws  IllegalArgumentException 如果{@code timeout}或{@code ttl}为负数。
     * @return 一个表示地址是否可达的{@code boolean}值。
     * @throws IOException 如果发生网络错误
     * @since 1.5
     */
    public boolean isReachable(NetworkInterface netif, int ttl,
                               int timeout) throws IOException {
        if (ttl < 0)
            throw new IllegalArgumentException("ttl can't be negative");
        if (timeout < 0)
            throw new IllegalArgumentException("timeout can't be negative");

        return impl.isReachable(this, timeout, netif, ttl);
    }

    /**
     * 获取此IP地址的主机名。
     *
     * <p>如果此InetAddress是使用主机名创建的，此主机名将被记住并返回；
     * 否则，将执行反向名称查找，并根据系统配置的名称查找服务返回结果。如果需要查找名称服务，
     * 调用{@link #getCanonicalHostName() getCanonicalHostName}。
     *
     * <p>如果有安全管理者，其
     * {@code checkConnect}方法将首先被调用
     * 以主机名和{@code -1}作为参数，以查看操作是否被允许。
     * 如果操作不被允许，它将返回IP地址的文本表示形式。
     *
     * @return  此IP地址的主机名，或如果安全检查不允许操作，则返回IP地址的文本表示形式。
     *
     * @see InetAddress#getCanonicalHostName
     * @see SecurityManager#checkConnect
     */
    public String getHostName() {
        return getHostName(true);
    }

    /**
     * 返回此地址的主机名。
     * 如果主机等于null，则此地址引用本地机器上的任何可用网络地址。
     * 此方法是包私有的，以便SocketPermission可以在此处进行调用而无需安全检查。
     *
     * <p>如果有安全管理者，此方法首先
     * 调用其{@code checkConnect}方法
     * 以主机名和{@code -1}作为参数，以查看调用代码是否被允许知道此IP地址的主机名，即连接到主机。
     * 如果操作不被允许，它将返回IP地址的文本表示形式。
     *
     * @return  此IP地址的主机名，或如果操作不被允许，则返回IP地址的文本表示形式。
     *
     * @param check 如果为true，则进行安全检查
     *
     * @see SecurityManager#checkConnect
     */
    String getHostName(boolean check) {
        if (holder().getHostName() == null) {
            holder().hostName = InetAddress.getHostFromNameService(this, check);
        }
        return holder().getHostName();
    }

    /**
     * 获取此IP地址的完全限定域名。
     * 最佳努力方法，意味着我们可能无法根据底层系统配置返回FQDN。
     *
     * <p>如果有安全管理者，此方法首先
     * 调用其{@code checkConnect}方法
     * 以主机名和{@code -1}作为参数，以查看调用代码是否被允许知道此IP地址的主机名，即连接到主机。
     * 如果操作不被允许，它将返回IP地址的文本表示形式。
     *
     * @return  此IP地址的完全限定域名，或如果操作不被允许，则返回IP地址的文本表示形式。
     *
     * @see SecurityManager#checkConnect
     *
     * @since 1.4
     */
    public String getCanonicalHostName() {
        if (canonicalHostName == null) {
            canonicalHostName =
                InetAddress.getHostFromNameService(this, true);
        }
        return canonicalHostName;
    }

    /**
     * 返回此地址的主机名。
     *
     * <p>如果有安全管理者，此方法首先
     * 调用其{@code checkConnect}方法
     * 以主机名和{@code -1}作为参数，以查看调用代码是否被允许知道此IP地址的主机名，即连接到主机。
     * 如果操作不被允许，它将返回IP地址的文本表示形式。
     *
     * @return  此IP地址的主机名，或如果操作不被允许，则返回IP地址的文本表示形式。
     *
     * @param check 如果为true，则进行安全检查
     *
     * @see SecurityManager#checkConnect
     */
    private static String getHostFromNameService(InetAddress addr, boolean check) {
        String host = null;
        for (NameService nameService : nameServices) {
            try {
                // 首先查找主机名
                host = nameService.getHostByAddr(addr.getAddress());

                /* 检查调用代码是否被允许知道
                 * 此IP地址的主机名，即连接到主机
                 */
                if (check) {
                    SecurityManager sec = System.getSecurityManager();
                    if (sec != null) {
                        sec.checkConnect(host, -1);
                    }
                }

                /* 现在获取此主机名的所有IP地址，
                 * 并确保其中一个与原始IP地址匹配。我们这样做是为了尽量防止欺骗。
                 */

                InetAddress[] arr = InetAddress.getAllByName0(host, check);
                boolean ok = false;

                if(arr != null) {
                    for(int i = 0; !ok && i < arr.length; i++) {
                        ok = addr.equals(arr[i]);
                    }
                }

                //XXX: 如果看起来像是欺骗，只返回地址？
                if (!ok) {
                    host = addr.getHostAddress();
                    return host;
                }

                break;

            } catch (SecurityException e) {
                host = addr.getHostAddress();
                break;
            } catch (UnknownHostException e) {
                host = addr.getHostAddress();
                // 让下一个提供者解析主机名
            }
        }

        return host;
    }

    /**
     * 返回此{@code InetAddress}对象的原始IP地址。结果是网络字节顺序：地址的最高字节在{@code getAddress()[0]}中。
     *
     * @return  此对象的原始IP地址。
     */
    public byte[] getAddress() {
        return null;
    }

    /**
     * 返回文本表示形式的IP地址字符串。
     *
     * @return  原始IP地址的字符串格式。
     * @since   JDK1.0.2
     */
    public String getHostAddress() {
        return null;
     }

    /**
     * 返回此IP地址的哈希码。
     *
     * @return  此IP地址的哈希码值。
     */
    public int hashCode() {
        return -1;
    }

    /**
     * 将此对象与指定对象进行比较。
     * 结果为{@code true}当且仅当参数不为{@code null}且它表示与
     * 此对象相同的IP地址。
     * <p>
     * 两个{@code InetAddress}实例表示相同的IP地址，如果
     * 由{@code getAddress}返回的字节数组的长度相同，并且每个字节数组的每个组件都相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回{@code true}；否则返回{@code false}。
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * 将此IP地址转换为{@code String}。返回的字符串形式为：主机名 / 文本IP地址。
     *
     * 如果主机名未解析，不会执行反向名称服务查找。主机名部分将由空字符串表示。
     *
     * @return  此IP地址的字符串表示形式。
     */
    public String toString() {
        String hostName = holder().getHostName();
        return ((hostName != null) ? hostName : "")
            + "/" + getHostAddress();
    }

    // 从主机名到地址的映射 - 可能是NameServiceAddresses（仍在通过NameService查找）或CachedAddresses（缓存时）
    private static final ConcurrentMap<String, Addresses> cache =
        new ConcurrentHashMap<>();

    // 需要过期的CachedAddresses保持有序，每次访问时扫描
    private static final NavigableSet<CachedAddresses> expirySet =
        new ConcurrentSkipListSet<>();

    // 公共接口
    private interface Addresses {
        InetAddress[] get() throws UnknownHostException;
    }

    // 用于缓存地址的持有者，包含必要的元数据
    private static final class CachedAddresses  implements Addresses, Comparable<CachedAddresses> {
        private static final AtomicLong seq = new AtomicLong();
        final String host;
        final InetAddress[] inetAddresses;
        final long expiryTime; // 过期时间（以System.nanoTime()为单位）
        final long id = seq.incrementAndGet(); // 每个实例都是唯一的

        CachedAddresses(String host, InetAddress[] inetAddresses, long expiryTime) {
            this.host = host;
            this.inetAddresses = inetAddresses;
            this.expiryTime = expiryTime;
        }

        @Override
        public InetAddress[] get() throws UnknownHostException {
            if (inetAddresses == null) {
                throw new UnknownHostException(host);
            }
            return inetAddresses;
        }

        @Override
        public int compareTo(CachedAddresses other) {
            // 自然顺序是过期时间 -
            // 比较过期时间的差异而不是直接比较过期时间，以避免可能的溢出。
            // （参见System.nanoTime()的建议...）
            long diff = this.expiryTime - other.expiryTime;
            if (diff < 0L) return -1;
            if (diff > 0L) return 1;
            // 通过唯一ID打破平局
            return Long.compare(this.id, other.id);
        }
    }

    // 基于名称服务查找的Addresses实现，当结果获得时替换自身
    private static final class NameServiceAddresses implements Addresses {
        private final String host;
        private final InetAddress reqAddr;

        NameServiceAddresses(String host, InetAddress reqAddr) {
            this.host = host;
            this.reqAddr = reqAddr;
        }

        @Override
        public InetAddress[] get() throws UnknownHostException {
            Addresses addresses;
            // 任何时间只有一个线程为特定主机查找名称服务
            synchronized (this) {
                // 重新检查我们是否仍然是我们自己 + 如果插槽为空则重新安装我们自己
                addresses = cache.putIfAbsent(host, this);
                if (addresses == null) {
                    // 当我们在其他线程中被CachedAddresses替换，然后CachedAddresses过期并从缓存中移除时，这可能会发生
                    addresses = this;
                }
                // 仍然是我们自己？
                if (addresses == this) {
                    // 查找名称服务
                    InetAddress[] inetAddresses;
                    UnknownHostException ex;
                    int cachePolicy;
                    try {
                        inetAddresses = getAddressesFromNameService(host, reqAddr);
                        ex = null;
                        cachePolicy = InetAddressCachePolicy.get();
                    } catch (UnknownHostException uhe) {
                        inetAddresses = null;
                        ex = uhe;
                        cachePolicy = InetAddressCachePolicy.getNegative();
                    }
                    // 根据cachePolicy移除或替换我们自己
                    if (cachePolicy == InetAddressCachePolicy.NEVER) {
                        cache.remove(host, this);
                    } else {
                        CachedAddresses cachedAddresses = new CachedAddresses(
                            host,
                            inetAddresses,
                            cachePolicy == InetAddressCachePolicy.FOREVER
                            ? 0L
                            // cachePolicy以秒为单位 - 我们需要纳秒
                            : System.nanoTime() + 1000_000_000L * cachePolicy
                        );
                        if (cache.replace(host, this, cachedAddresses) &&
                            cachePolicy != InetAddressCachePolicy.FOREVER) {
                            // 安排过期
                            expirySet.add(cachedAddresses);
                        }
                    }
                    if (inetAddresses == null) {
                        throw ex == null ? new UnknownHostException(host) : ex;
                    }
                    return inetAddresses;
                }
                // 否则 addresses != this
            }
            // 当我们已经被替换时委托给不同的地址
            // 但避免在同步块外死锁
            return addresses.get();
        }
    }


                static InetAddressImpl  impl;

    private static NameService createNSProvider(String provider) {
        if (provider == null)
            return null;

        NameService nameService = null;
        if (provider.equals("default")) {
            // 初始化默认名称服务
            nameService = new NameService() {
                public InetAddress[] lookupAllHostAddr(String host)
                    throws UnknownHostException {
                    validate(host);
                    return impl.lookupAllHostAddr(host);
                }
                public String getHostByAddr(byte[] addr)
                    throws UnknownHostException {
                    return impl.getHostByAddr(addr);
                }
            };
        } else {
            final String providerName = provider;
            try {
                nameService = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<NameService>() {
                        public NameService run() {
                            Iterator<NameServiceDescriptor> itr =
                                ServiceLoader.load(NameServiceDescriptor.class)
                                    .iterator();
                            while (itr.hasNext()) {
                                NameServiceDescriptor nsd = itr.next();
                                if (providerName.
                                    equalsIgnoreCase(nsd.getType()+","
                                        +nsd.getProviderName())) {
                                    try {
                                        return nsd.createNameService();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.err.println(
                                            "无法创建名称服务:"
                                             +providerName+": " + e);
                                    }
                                }
                            }

                            return null;
                        }
                    }
                );
            } catch (java.security.PrivilegedActionException e) {
            }
        }

        return nameService;
    }

    static {
        // 创建实现
        impl = InetAddressImplFactory.create();

        // 如果提供了名称服务提供者并请求，则获取名称服务
        String provider = null;;
        String propPrefix = "sun.net.spi.nameservice.provider.";
        int n = 1;
        nameServices = new ArrayList<NameService>();
        provider = AccessController.doPrivileged(
                new GetPropertyAction(propPrefix + n));
        while (provider != null) {
            NameService ns = createNSProvider(provider);
            if (ns != null)
                nameServices.add(ns);

            n++;
            provider = AccessController.doPrivileged(
                    new GetPropertyAction(propPrefix + n));
        }

        // 如果没有指定任何名称服务提供者，则创建默认的
        if (nameServices.size() == 0) {
            NameService ns = createNSProvider("default");
            nameServices.add(ns);
        }
    }

    /**
     * 根据提供的主机名和IP地址创建一个InetAddress。
     * 不检查地址的有效性。
     *
     * <p> 主机名可以是机器名，例如
     * "{@code java.sun.com}"，或者是其IP地址的文本表示。
     * <p> 不对主机名进行有效性检查。
     *
     * <p> 如果addr指定的是IPv4地址，则返回Inet4Address的实例；
     * 否则，返回Inet6Address的实例。
     *
     * <p> IPv4地址字节数组必须是4字节长，IPv6字节数组必须是16字节长
     *
     * @param host 指定的主机
     * @param addr 网络字节顺序的原始IP地址
     * @return 从原始IP地址创建的InetAddress对象。
     * @exception  UnknownHostException 如果IP地址长度非法
     * @since 1.4
     */
    public static InetAddress getByAddress(String host, byte[] addr)
        throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet4Address.INADDRSZ) {
                return new Inet4Address(host, addr);
            } else if (addr.length == Inet6Address.INADDRSZ) {
                byte[] newAddr
                    = IPAddressUtil.convertFromIPv4MappedAddress(addr);
                if (newAddr != null) {
                    return new Inet4Address(host, newAddr);
                } else {
                    return new Inet6Address(host, addr);
                }
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }


    /**
     * 给定主机名，确定其IP地址。
     *
     * <p> 主机名可以是机器名，例如
     * "{@code java.sun.com}"，或者是其IP地址的文本表示。如果提供了字面IP地址，仅检查地址格式的有效性。
     *
     * <p> 对于以字面IPv6地址形式指定的{@code host}，
     * 接受RFC 2732定义的形式或RFC 2373定义的字面IPv6地址格式。还支持IPv6范围地址。有关IPv6范围地址的描述，请参见<a href="Inet6Address.html#scoped">这里</a>。
     *
     * <p> 如果{@code host}为{@code null}，则返回表示回环接口地址的{@code InetAddress}。
     * 请参见<a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * 第2节和<a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * 第2.5.3节。 </p>
     *
     * @param      host   指定的主机，或{@code null}。
     * @return     给定主机名的IP地址。
     * @exception  UnknownHostException  如果无法找到{@code host}的IP地址，或者为全局IPv6地址指定了scope_id。
     * @exception  SecurityException 如果存在安全管理者，并且其checkConnect方法不允许该操作
     */
    public static InetAddress getByName(String host)
        throws UnknownHostException {
        return InetAddress.getAllByName(host)[0];
    }

    // 由部署缓存管理器调用
    private static InetAddress getByName(String host, InetAddress reqAddr)
        throws UnknownHostException {
        return InetAddress.getAllByName(host, reqAddr)[0];
    }

    /**
     * 给定主机名，返回其IP地址数组，基于系统配置的名称服务。
     *
     * <p> 主机名可以是机器名，例如
     * "{@code java.sun.com}"，或者是其IP地址的文本表示。如果提供了字面IP地址，仅检查地址格式的有效性。
     *
     * <p> 对于以<i>字面IPv6地址</i>形式指定的{@code host}，
     * 接受RFC 2732定义的形式或RFC 2373定义的字面IPv6地址格式。字面IPv6地址也可以通过附加范围区域标识符或scope_id来限定。
     * 范围标识符的语法和用法描述请参见<a href="Inet6Address.html#scoped">这里</a>。
     * <p> 如果{@code host}为{@code null}，则返回表示回环接口地址的{@code InetAddress}。
     * 请参见<a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * 第2节和<a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * 第2.5.3节。 </p>
     *
     * <p> 如果存在安全管理者且{@code host}不为
     * null且{@code host.length() } 不等于零，安全管理者的
     * {@code checkConnect}方法将被调用
     * 以主机名和{@code -1}
     * 作为其参数，以查看是否允许该操作。
     *
     * @param      host   主机名，或{@code null}。
     * @return     给定主机名的所有IP地址数组。
     *
     * @exception  UnknownHostException  如果无法找到{@code host}的IP地址，或者为全局IPv6地址指定了scope_id。
     * @exception  SecurityException  如果存在安全管理者且其
     *               {@code checkConnect}方法不允许该操作。
     *
     * @see SecurityManager#checkConnect
     */
    public static InetAddress[] getAllByName(String host)
        throws UnknownHostException {
        return getAllByName(host, null);
    }

    private static InetAddress[] getAllByName(String host, InetAddress reqAddr)
        throws UnknownHostException {

        if (host == null || host.length() == 0) {
            InetAddress[] ret = new InetAddress[1];
            ret[0] = impl.loopbackAddress();
            return ret;
        }

        validate(host);
        boolean ipv6Expected = false;
        if (host.charAt(0) == '[') {
            // 这应该是一个IPv6字面量
            if (host.length() > 2 && host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
                ipv6Expected = true;
            } else {
                // 这应该是IPv6字面量，但不是
                throw invalidIPv6LiteralException(host, false);
            }
        }

        // 检查并尝试将主机字符串解析为IP地址字面量
        if (IPAddressUtil.digit(host.charAt(0), 16) != -1
            || (host.charAt(0) == ':')) {
            byte[] addr = null;
            int numericZone = -1;
            String ifname = null;

            if (!ipv6Expected) {
                // 如果主机未被'[]'包围，则检查是否为IPv4地址
                try {
                    addr = IPAddressUtil.validateNumericFormatV4(host);
                } catch (IllegalArgumentException iae) {
                    UnknownHostException uhe = new UnknownHostException(host);
                    uhe.initCause(iae);
                    throw uhe;
                }
            }
            if (addr == null) {
                // 尝试将主机字符串解析为IPv6字面量
                // 首先检查是否包含数字或字符串范围标识符
                int pos;
                if ((pos = host.indexOf('%')) != -1) {
                    numericZone = checkNumericZone(host);
                    if (numericZone == -1) { /* 剩余字符串必须是接口名称 */
                        ifname = host.substring(pos + 1);
                    }
                }
                if ((addr = IPAddressUtil.textToNumericFormatV6(host)) == null &&
                        (host.contains(":") || ipv6Expected)) {
                    throw invalidIPv6LiteralException(host, ipv6Expected);
                }
            }
            if(addr != null) {
                InetAddress[] ret = new InetAddress[1];
                if (addr.length == Inet4Address.INADDRSZ) {
                    if (numericZone != -1 || ifname != null) {
                        // IPv4-mapped地址不得包含范围标识符
                        throw new UnknownHostException(host + ": 无效的IPv4-mapped地址");
                    }
                    ret[0] = new Inet4Address(null, addr);
                } else {
                    if (ifname != null) {
                        ret[0] = new Inet6Address(null, addr, ifname);
                    } else {
                        ret[0] = new Inet6Address(null, addr, numericZone);
                    }
                }
                return ret;
            }
        } else if (ipv6Expected) {
            // 由于主机字符串以方括号开始和结束，我们期望一个IPv6字面量，但得到了其他内容。
            throw invalidIPv6LiteralException(host, true);
        }
        return getAllByName0(host, reqAddr, true, true);
    }

    private static UnknownHostException invalidIPv6LiteralException(String host, boolean wrapInBrackets) {
        String hostString = wrapInBrackets ? "[" + host + "]" : host;
        return new UnknownHostException(hostString + ": 无效的IPv6地址字面量");
    }

    /**
     * 返回回环地址。
     * <p>
     * 返回的InetAddress将表示IPv4
     * 回环地址，127.0.0.1，或IPv6
     * 回环地址，::1。返回的IPv4回环地址
     * 只是127.*.*.*形式中的一个
     *
     * @return  InetAddress回环实例。
     * @since 1.7
     */
    public static InetAddress getLoopbackAddress() {
        return impl.loopbackAddress();
    }


    /**
     * 检查字面地址字符串是否附加了%nn
     * 如果没有，返回-1，否则返回数字值。
     *
     * %nn也可以是表示当前可用网络接口的displayName的字符串。
     */
    private static int checkNumericZone (String s) throws UnknownHostException {
        int percent = s.indexOf ('%');
        int slen = s.length();
        int digit, zone=0;
        int multmax = Integer.MAX_VALUE / 10; // 用于检测int溢出
        if (percent == -1) {
            return -1;
        }
        for (int i=percent+1; i<slen; i++) {
            char c = s.charAt(i);
            if ((digit = IPAddressUtil.parseAsciiDigit(c, 10)) < 0) {
                return -1;
            }
            if (zone > multmax) {
                return -1;
            }
            zone = (zone * 10) + digit;
            if (zone < 0) {
                return -1;
            }

        }
        return zone;
    }

    private static InetAddress[] getAllByName0 (String host)
        throws UnknownHostException
    {
        return getAllByName0(host, true);
    }

    /**
     * 包私有，以便SocketPermission可以调用它
     */
    static InetAddress[] getAllByName0 (String host, boolean check)
        throws UnknownHostException  {
        return getAllByName0 (host, null, check, true);
    }

    /**
     * 指定的查找方法。
     *
     * @param host 要查找的主机名
     * @param reqAddr 请求的地址，作为返回数组中的第一个
     * @param check 执行安全检查
     * @param useCache 如果未过期则使用缓存值，否则始终
     *                 执行名称服务查找（并缓存结果）
     * @return InetAddress数组
     * @throws UnknownHostException 如果主机名未找到
     */
    private static InetAddress[] getAllByName0(String host,
                                               InetAddress reqAddr,
                                               boolean check,
                                               boolean useCache)
        throws UnknownHostException  {


                    /* If it gets here it is presumed to be a hostname */

        /* 确保在提供主机名之前，与主机的连接是允许的 */
        if (check) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, -1);
            }
        }

        // 从缓存中移除过期的地址 - expirySet 保持它们按过期时间排序，因此我们只需要遍历 NavigableSet 的前缀...
        long now = System.nanoTime();
        for (CachedAddresses caddrs : expirySet) {
            // 比较时间实例的差值而不是直接比较时间实例，以避免可能的溢出。
            // (参见 System.nanoTime() 的建议...)
            if ((caddrs.expiryTime - now) < 0L) {
                // ConcurrentSkipListSet 使用弱一致性迭代器，
                // 因此在迭代时移除是安全的...
                if (expirySet.remove(caddrs)) {
                    // ... 从缓存中移除
                    cache.remove(caddrs.host, caddrs);
                }
            } else {
                // 遇到了第一个在未来过期的元素
                break;
            }
        }

        // 查找或从缓存中移除
        Addresses addrs;
        if (useCache) {
            addrs = cache.get(host);
        } else {
            addrs = cache.remove(host);
            if (addrs != null) {
                if (addrs instanceof CachedAddresses) {
                    // 如果是 CachedAddresses，尝试从 expirySet 中移除
                    expirySet.remove(addrs);
                }
                addrs = null;
            }
        }

        if (addrs == null) {
            // 创建一个 NameServiceAddresses 实例，该实例将查找名称服务并将其安装在缓存中...
            Addresses oldAddrs = cache.putIfAbsent(
                host,
                addrs = new NameServiceAddresses(host, reqAddr)
            );
            if (oldAddrs != null) { // 输掉了 putIfAbsent 竞争
                addrs = oldAddrs;
            }
        }

        // 要求 Addresses 获取一个 InetAddress 数组并克隆它
        return addrs.get().clone();
    }

    static InetAddress[] getAddressesFromNameService(String host, InetAddress reqAddr)
        throws UnknownHostException
    {
        InetAddress[] addresses = null;
        UnknownHostException ex = null;

        for (NameService nameService : nameServices) {
            try {
                addresses = nameService.lookupAllHostAddr(host);
                break;
            } catch (UnknownHostException uhe) {
                if (host.equalsIgnoreCase("localhost")) {
                    addresses = new InetAddress[] { impl.loopbackAddress() };
                    break;
                }
                else {
                    ex = uhe;
                }
            }
        }

        if (addresses == null) {
            throw ex == null ? new UnknownHostException(host) : ex;
        }

        // 还有更多工作要做？
        if (reqAddr != null && addresses.length > 1 && !addresses[0].equals(reqAddr)) {
            // 找到了？
            int i = 1;
            for (; i < addresses.length; i++) {
                if (addresses[i].equals(reqAddr)) {
                    break;
                }
            }
            // 旋转
            if (i < addresses.length) {
                InetAddress tmp, tmp2 = reqAddr;
                for (int j = 0; j < i; j++) {
                    tmp = addresses[j];
                    addresses[j] = tmp2;
                    tmp2 = tmp;
                }
                addresses[i] = tmp2;
            }
        }

        return addresses;
    }

    /**
     * 给定原始 IP 地址，返回一个 {@code InetAddress} 对象。
     * 参数是以网络字节顺序排列的：地址的最高字节位于 {@code getAddress()[0]}。
     *
     * <p>此方法不会阻塞，即不会执行反向名称服务查找。
     *
     * <p>IPv4 地址字节数组必须是 4 个字节长，IPv6 字节数组必须是 16 个字节长。
     *
     * @param addr 以网络字节顺序排列的原始 IP 地址
     * @return 从原始 IP 地址创建的 InetAddress 对象。
     * @exception  UnknownHostException 如果 IP 地址长度非法
     * @since 1.4
     */
    public static InetAddress getByAddress(byte[] addr)
        throws UnknownHostException {
        return getByAddress(null, addr);
    }

    private static final class CachedLocalHost {
        final String host;
        final InetAddress addr;
        final long expiryTime = System.nanoTime() + 5000_000_000L; // 现在 + 5秒;

        CachedLocalHost(String host, InetAddress addr) {
            this.host = host;
            this.addr = addr;
        }
    }

    private static volatile CachedLocalHost cachedLocalHost;

    /**
     * 返回本地主机的地址。这是通过从系统中检索主机名，然后将该名称解析为
     * {@code InetAddress} 来实现的。
     *
     * <P>注意：解析的地址可能会被缓存一段时间。
     * </P>
     *
     * <p>如果有安全经理，其
     * {@code checkConnect} 方法将被调用
     * 以本地主机名和 {@code -1}
     * 作为其参数，以查看操作是否允许。
     * 如果操作不允许，将返回一个表示
     * 回环地址的 InetAddress。
     *
     * @return 本地主机的地址。
     *
     * @exception  UnknownHostException 如果无法将本地主机名解析为地址。
     *
     * @see SecurityManager#checkConnect
     * @see java.net.InetAddress#getByName(java.lang.String)
     */
    public static InetAddress getLocalHost() throws UnknownHostException {

        SecurityManager security = System.getSecurityManager();
        try {
            // 缓存的数据是否仍然有效？
            CachedLocalHost clh = cachedLocalHost;
            if (clh != null && (clh.expiryTime - System.nanoTime()) >= 0L) {
                if (security != null) {
                    security.checkConnect(clh.host, -1);
                }
                return clh.addr;
            }

            String local = impl.getLocalHostName();

            if (security != null) {
                security.checkConnect(local, -1);
            }

            InetAddress localAddr;
            if (local.equals("localhost")) {
                // 为 "localhost" 主机名提供快捷方式
                localAddr = impl.loopbackAddress();
            } else {
                // 不进行安全检查且不使用缓存数据调用 getAllByName0
                try {
                    localAddr = getAllByName0(local, null, false, false)[0];
                } catch (UnknownHostException uhe) {
                    // 重新抛出带有更详细错误消息的异常。
                    UnknownHostException uhe2 =
                        new UnknownHostException(local + ": " +
                                                 uhe.getMessage());
                    uhe2.initCause(uhe);
                    throw uhe2;
                }
            }
            cachedLocalHost = new CachedLocalHost(local, localAddr);
            return localAddr;
        } catch (java.lang.SecurityException e) {
            return impl.loopbackAddress();
        }
    }

    /**
     * 执行类加载时的初始化。
     */
    private static native void init();


    /*
     * 返回表示 anyLocalAddress 的 InetAddress
     * (通常是 0.0.0.0 或 ::0)
     */
    static InetAddress anyLocalAddress() {
        return impl.anyLocalAddress();
    }

    /*
     * 加载并实例化底层实现类
     */
    static InetAddressImpl loadImpl(String implName) {
        Object impl = null;

        /*
         * 属性 "impl.prefix" 将被添加到我们实例化的实现对象的类名前，
         * 我们将委托实际工作（如本地方法）给该对象。此属性
         * 可以在不同的 java 实现中有所不同。默认值为空字符串 ""。
         */
        String prefix = AccessController.doPrivileged(
                      new GetPropertyAction("impl.prefix", ""));
        try {
            impl = Class.forName("java.net." + prefix + implName).newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        } catch (IllegalAccessException e) {
            System.err.println("Cannot access class: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        }

        if (impl == null) {
            try {
                impl = Class.forName(implName).newInstance();
            } catch (Exception e) {
                throw new Error("System property impl.prefix incorrect");
            }
        }

        return (InetAddressImpl) impl;
    }

    private void readObjectNoData (ObjectInputStream s) throws
                         IOException, ClassNotFoundException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException ("invalid address type");
        }
    }

    private static final long FIELDS_OFFSET;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(
                InetAddress.class.getDeclaredField("holder")
            );
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private void readObject (ObjectInputStream s) throws
                         IOException, ClassNotFoundException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException ("invalid address type");
        }
        GetField gf = s.readFields();
        String host = (String)gf.get("hostName", null);
        int address = gf.get("address", 0);
        int family = gf.get("family", 0);
        if (family != IPv4 && family != IPv6) {
            throw new InvalidObjectException("invalid address family type: " + family);
        }
        InetAddressHolder h = new InetAddressHolder(host, address, family);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    /* 因为序列化字段不再存在，所以需要这个 */

    /**
     * @serialField hostName String
     * @serialField address int
     * @serialField family int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("hostName", String.class),
        new ObjectStreamField("address", int.class),
        new ObjectStreamField("family", int.class),
    };

    private void writeObject (ObjectOutputStream s) throws
                        IOException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException ("invalid address type");
        }
        PutField pf = s.putFields();
        pf.put("hostName", holder().getHostName());
        pf.put("address", holder().getAddress());
        pf.put("family", holder().getFamily());
        s.writeFields();
    }

    private static void validate(String host) throws UnknownHostException {
        if (host.indexOf(0) != -1) {
            throw new UnknownHostException("主机名中不允许出现 NUL 字符");
        }
    }
}

/*
 * 创建实现的简单工厂
 */
class InetAddressImplFactory {

    static InetAddressImpl create() {
        return InetAddress.loadImpl(isIPv6Supported() ?
                                    "Inet6AddressImpl" : "Inet4AddressImpl");
    }

    static native boolean isIPv6Supported();
}
