
/*
 * Copyright (c) 1995, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
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
import sun.security.action.*;
import sun.net.InetAddressCachePolicy;
import sun.net.util.IPAddressUtil;
import sun.net.spi.nameservice.*;

/**
 * 该类表示一个互联网协议 (IP) 地址。
 *
 * <p> IP 地址是一个 32 位或 128 位的无符号数字，由 IP 使用，IP 是 UDP 和 TCP 等协议所依赖的低级协议。IP 地址架构由 <a
 * href="http://www.ietf.org/rfc/rfc790.txt"><i>RFC 790: 已分配数字</i></a>、<a
 * href="http://www.ietf.org/rfc/rfc1918.txt"> <i>RFC 1918: 私有互联网的地址分配</i></a>、<a
 * href="http://www.ietf.org/rfc/rfc2365.txt"><i>RFC 2365: 管理范围的 IP 组播</i></a> 和 <a
 * href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC 2373: IP 版本 6 地址架构</i></a> 定义。InetAddress 的实例包含一个 IP 地址，可能还包括其对应的主机名（取决于它是用主机名构造的还是已经完成了反向主机名解析）。
 *
 * <h3> 地址类型 </h3>
 *
 * <blockquote><table cellspacing=2 summary="单播和组播地址类型的描述">
 *   <tr><th valign=top><i>单播</i></th>
 *       <td>单个接口的标识符。发送到单播地址的数据包将传递到该地址标识的接口。
 *
 *         <p> 未指定地址 -- 也称为 anylocal 或通配符地址。它绝不能分配给任何节点。它表示地址的缺失。一个使用示例是作为 bind 的目标，允许服务器在任何接口上接受客户端连接，以防服务器主机有多个接口。
 *
 *         <p> 未指定地址不得用作 IP 数据包的目标地址。
 *
 *         <p> <i>回环</i> 地址 -- 分配给回环接口的地址。发送到此 IP 地址的任何内容都会在本地主机上回环并成为 IP 输入。此地址通常用于测试客户端。</td></tr>
 *   <tr><th valign=top><i>组播</i></th>
 *       <td>一组接口（通常属于不同的节点）的标识符。发送到组播地址的数据包将传递到该地址标识的所有接口。</td></tr>
 * </table></blockquote>
 *
 * <h4> IP 地址范围 </h4>
 *
 * <p> <i>链路本地</i> 地址设计用于单个链路上的寻址，例如自动地址配置、邻居发现或没有路由器时的情况。
 *
 * <p> <i>站点本地</i> 地址设计用于站点内的寻址，无需全局前缀。
 *
 * <p> <i>全局</i> 地址在互联网上是唯一的。
 *
 * <h4> IP 地址的文本表示 </h4>
 *
 * IP 地址的文本表示是特定于地址族的。
 *
 * <p>
 *
 * 有关 IPv4 地址格式，请参阅 <A
 * HREF="Inet4Address.html#format">Inet4Address#format</A>；有关 IPv6
 * 地址格式，请参阅 <A
 * HREF="Inet6Address.html#format">Inet6Address#format</A>。
 *
 * <P>有 <a href="doc-files/net-properties.html#Ipv4IPv6">几个
 * 系统属性</a> 影响如何使用 IPv4 和 IPv6 地址。</P>
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
 * <p> 默认情况下，当安装了安全经理时，为了防止 DNS 欺骗攻击，成功的主机名解析结果将被永久缓存。当没有安装安全经理时，默认行为是将条目缓存一段时间（具体时间取决于实现）。不成功的主机名解析结果将被缓存非常短的时间（10
 * 秒）以提高性能。
 *
 * <p> 如果不希望使用默认行为，可以设置一个 Java 安全属性，将其设置为不同的正向缓存生存时间 (TTL) 值。同样，系统管理员可以在需要时配置不同的负向缓存 TTL 值。
 *
 * <p> 两个 Java 安全属性控制用于正向和负向主机名解析缓存的 TTL 值：
 *
 * <blockquote>
 * <dl>
 * <dt><b>networkaddress.cache.ttl</b></dt>
 * <dd>指示从名称服务成功查找名称的缓存策略。值指定为整数，表示成功查找的缓存时间（以秒为单位）。默认设置是缓存一段时间（具体时间取决于实现）。
 * <p>
 * 值 -1 表示“永久缓存”。
 * </dd>
 * <dt><b>networkaddress.cache.negative.ttl</b> (默认: 10)</dt>
 * <dd>指示从名称服务未成功查找名称的缓存策略。值指定为整数，表示未成功查找的缓存时间（以秒为单位）。
 * <p>
 * 值 0 表示“从不缓存”。
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
     * 指定地址族：Internet 协议版本 6
     * @since 1.4
     */
    static final int IPv6 = 2;

    /* 指定地址族偏好 */
    static transient boolean preferIPv6Address = false;

    static class InetAddressHolder {
        /**
         * 保留应用程序指定的原始主机名。
         *
         * 原始主机名对于基于域的端点识别（参见 RFC 2818 和 RFC 6125）非常有用。如果使用原始 IP 地址创建地址，
         * 反向名称查找可能会通过 DNS 伪造引入端点识别安全问题。
         *
         * Oracle JSSE 提供者通过 sun.misc.JavaNetAccess 使用此原始主机名进行 SSL/TLS 端点识别。
         *
         * 注意：如有必要，将来可能会定义一个新的公共方法。
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
     * 创建一个空的 InetAddress，由 accept() 方法填充。但是，此 InetAddress 不会放入地址缓存中，
     * 因为它不是通过名称创建的。
     */
    InetAddress() {
        holder = new InetAddressHolder();
    }

    /**
     * 用 Inet4Address 对象替换反序列化的对象。
     *
     * @return 用于替换反序列化对象的替代对象。
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
     * 实用程序例程，用于检查多播地址是否具有链路范围。
     *
     * @return 一个 {@code boolean} 值，指示该地址是否为链路本地范围的多播地址，
     *         如果它不是链路本地范围或不是多播地址，则返回 false
     * @since 1.4
     */
    public boolean isMCLinkLocal() {
        return false;
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有站点范围。
     *
     * @return 一个 {@code boolean} 值，指示该地址是否为站点本地范围的多播地址，
     *         如果它不是站点本地范围或不是多播地址，则返回 false
     * @since 1.4
     */
    public boolean isMCSiteLocal() {
        return false;
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有组织范围。
     *
     * @return 一个 {@code boolean} 值，指示该地址是否为组织本地范围的多播地址，
     *         如果它不是组织本地范围或不是多播地址，则返回 false
     * @since 1.4
     */
    public boolean isMCOrgLocal() {
        return false;
    }


    /**
     * 测试该地址是否可达。实现会尽力尝试到达主机，但防火墙和服务器配置可能会阻止请求，
     * 导致不可达状态，而某些特定端口可能仍然可访问。
     * 一个典型的实现如果可以获得权限，将使用 ICMP ECHO 请求，否则它将尝试在目标主机的端口 7（回声）上建立 TCP 连接。
     * <p>
     * 超时值（以毫秒为单位）表示尝试的最大持续时间。如果操作在得到回答之前超时，主机将被视为不可达。负值将导致抛出 IllegalArgumentException。
     *
     * @param   timeout 调用前的超时时间，以毫秒为单位
     * @return 一个 {@code boolean} 值，指示地址是否可达。
     * @throws IOException 如果发生网络错误
     * @throws  IllegalArgumentException 如果 {@code timeout} 为负。
     * @since 1.5
     */
    public boolean isReachable(int timeout) throws IOException {
        return isReachable(null, 0 , timeout);
    }

    /**
     * 测试该地址是否可达。实现会尽力尝试到达主机，但防火墙和服务器配置可能会阻止请求，
     * 导致不可达状态，而某些特定端口可能仍然可访问。
     * 一个典型的实现如果可以获得权限，将使用 ICMP ECHO 请求，否则它将尝试在目标主机的端口 7（回声）上建立 TCP 连接。
     * <p>
     * {@code network interface} 和 {@code ttl} 参数
     * 允许调用者指定测试将通过的网络接口以及数据包应通过的最大跳数。
     * 如果 {@code ttl} 为负值，将抛出 IllegalArgumentException。
     * <p>
     * 超时值（以毫秒为单位）表示尝试的最大持续时间。如果操作在得到回答之前超时，主机将被视为不可达。负值将导致抛出 IllegalArgumentException。
     *
     * @param   netif   测试将通过的网络接口，或 null 表示任何接口
     * @param   ttl     尝试的最大跳数或 0 表示默认值
     * @param   timeout 调用前的超时时间，以毫秒为单位
     * @throws  IllegalArgumentException 如果 {@code timeout}
     *                          或 {@code ttl} 为负。
     * @return 一个 {@code boolean} 值，指示地址是否可达。
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
     * 获取此 IP 地址的主机名。
     *
     * <p>如果使用主机名创建了此 InetAddress，则将记住并返回该主机名；
     * 否则，将执行反向名称查找，并根据系统配置的名称查找服务返回结果。如果需要查找名称服务，
     * 请调用 {@link #getCanonicalHostName() getCanonicalHostName}。
     *
     * <p>如果有安全管理者，其
     * {@code checkConnect} 方法首先被调用
     * 以主机名和 {@code -1}
     * 作为其参数，以查看是否允许此操作。如果操作不允许，它将返回
     * IP 地址的文本表示。
     *
     * @return 该 IP 地址的主机名，或如果安全检查不允许此操作，则返回 IP 地址的文本表示。
     *
     * @see InetAddress#getCanonicalHostName
     * @see SecurityManager#checkConnect
     */
    public String getHostName() {
        return getHostName(true);
    }

    /**
     * 返回此地址的主机名。
     * 如果主机等于 null，则此地址引用本地机器的任何可用网络地址。
     * 这是包私有的，以便 SocketPermission 可以在此处进行调用而无需安全检查。
     *
     * <p>如果有安全管理者，此方法首先
     * 调用其 {@code checkConnect} 方法
     * 以主机名和 {@code -1}
     * 作为其参数，以查看调用代码是否允许知道此 IP 地址的主机名，即连接到主机。
     * 如果操作不允许，它将返回
     * IP 地址的文本表示。
     *
     * @return 该 IP 地址的主机名，或如果安全检查不允许此操作，则返回 IP 地址的文本表示。
     *
     * @param check 如果为 true，则进行安全检查
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
     * 获取此 IP 地址的完全限定域名。
     * 尽力而为的方法，意味着根据底层系统配置，我们可能无法返回
     * 完全限定域名。
     *
     * <p>如果有安全管理者，此方法首先
     * 调用其 {@code checkConnect} 方法
     * 以主机名和 {@code -1}
     * 作为参数，以检查调用代码是否允许知道
     * 此 IP 地址的主机名，即，是否允许连接到主机。
     * 如果操作不允许，它将返回
     * IP 地址的文本表示形式。
     *
     * @return  此 IP 地址的完全限定域名，
     *    或者如果安全检查不允许操作，
     *    则返回 IP 地址的文本表示形式。
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
     * 调用其 {@code checkConnect} 方法
     * 以主机名和 {@code -1}
     * 作为参数，以检查调用代码是否允许知道
     * 此 IP 地址的主机名，即，是否允许连接到主机。
     * 如果操作不允许，它将返回
     * IP 地址的文本表示形式。
     *
     * @return  此 IP 地址的主机名，或者如果安全检查不允许操作，
     *    则返回 IP 地址的文本表示形式。
     *
     * @param check 如果为 true，则进行安全检查
     *
     * @see SecurityManager#checkConnect
     */
    private static String getHostFromNameService(InetAddress addr, boolean check) {
        String host = null;
        for (NameService nameService : nameServices) {
            try {
                // 首先查找主机名
                host = nameService.getHostByAddr(addr.getAddress());

                /* 检查调用代码是否允许知道
                 * 此 IP 地址的主机名，即，是否允许连接到主机
                 */
                if (check) {
                    SecurityManager sec = System.getSecurityManager();
                    if (sec != null) {
                        sec.checkConnect(host, -1);
                    }
                }

                /* 现在获取此主机名的所有 IP 地址，
                 * 并确保其中一个与原始 IP 地址匹配。我们这样做是为了尽量防止欺骗。
                 */

                InetAddress[] arr = InetAddress.getAllByName0(host, check);
                boolean ok = false;

                if(arr != null) {
                    for(int i = 0; !ok && i < arr.length; i++) {
                        ok = addr.equals(arr[i]);
                    }
                }

                //XXX: 如果看起来像是欺骗，直接返回地址？
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
     * 返回此 {@code InetAddress} 对象的原始 IP 地址。结果为网络字节顺序：地址的最高字节位于 {@code getAddress()[0]}。
     *
     * @return  此对象的原始 IP 地址。
     */
    public byte[] getAddress() {
        return null;
    }

    /**
     * 返回 IP 地址的文本表示形式。
     *
     * @return  原始 IP 地址的字符串格式。
     * @since   JDK1.0.2
     */
    public String getHostAddress() {
        return null;
     }

    /**
     * 返回此 IP 地址的哈希码。
     *
     * @return  此 IP 地址的哈希码值。
     */
    public int hashCode() {
        return -1;
    }

    /**
     * 将此对象与指定对象进行比较。
     * 结果为 {@code true} 当且仅当参数
     * 不为 {@code null} 并且它表示与
     * 此对象相同的 IP 地址。
     * <p>
     * 两个 {@code InetAddress} 实例表示相同的 IP
     * 地址，如果它们的 {@code getAddress} 返回的字节数组长度相同，并且每个
     * 字节数组的组件都相同。
     *
     * @param   obj   要比较的对象。
     * @return  {@code true} 如果对象相同；
     *          {@code false} 否则。
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * 将此 IP 地址转换为 {@code String}。返回的字符串形式为：主机名 / 原始 IP 地址。
     *
     * 如果主机名未解析，不会执行反向名称服务查找。主机名部分将表示为空字符串。
     *
     * @return  此 IP 地址的字符串表示形式。
     */
    public String toString() {
        String hostName = holder().getHostName();
        return ((hostName != null) ? hostName : "")
            + "/" + getHostAddress();
    }

    /*
     * 缓存地址 - 我们自己的小 nis，不是吗！
     */
    private static Cache addressCache = new Cache(Cache.Type.Positive);

    private static Cache negativeCache = new Cache(Cache.Type.Negative);

    private static boolean addressCacheInit = false;

    static InetAddress[]    unknown_array; // 将此放入缓存

    static InetAddressImpl  impl;

    private static final HashMap<String, Void> lookupTable = new HashMap<>();

    /**
     * 表示缓存条目
     */
    static final class CacheEntry {


                    CacheEntry(InetAddress[] addresses, long expiration) {
            this.addresses = addresses;
            this.expiration = expiration;
        }

        InetAddress[] addresses;
        long expiration;
    }

    /**
     * 一个根据创建时指定的策略管理条目的缓存。
     */
    static final class Cache {
        private LinkedHashMap<String, CacheEntry> cache;
        private Type type;

        enum Type {Positive, Negative};

        /**
         * 创建缓存
         */
        public Cache(Type type) {
            this.type = type;
            cache = new LinkedHashMap<String, CacheEntry>();
        }

        private int getPolicy() {
            if (type == Type.Positive) {
                return InetAddressCachePolicy.get();
            } else {
                return InetAddressCachePolicy.getNegative();
            }
        }

        /**
         * 向缓存中添加一个条目。如果已经存在该主机的条目，则该条目将被替换。
         */
        public Cache put(String host, InetAddress[] addresses) {
            int policy = getPolicy();
            if (policy == InetAddressCachePolicy.NEVER) {
                return this;
            }

            // 清除任何已过期的条目

            if (policy != InetAddressCachePolicy.FOREVER) {

                // 由于我们按插入顺序迭代，因此当找到一个未过期的条目时可以终止。
                LinkedList<String> expired = new LinkedList<>();
                long now = System.currentTimeMillis();
                for (String key : cache.keySet()) {
                    CacheEntry entry = cache.get(key);

                    if (entry.expiration >= 0 && entry.expiration < now) {
                        expired.add(key);
                    } else {
                        break;
                    }
                }

                for (String key : expired) {
                    cache.remove(key);
                }
            }

            // 创建新条目并将其添加到缓存中
            // -- 由于 HashMap 会替换现有条目，因此我们不需要显式检查该主机是否已存在条目。
            long expiration;
            if (policy == InetAddressCachePolicy.FOREVER) {
                expiration = -1;
            } else {
                expiration = System.currentTimeMillis() + (policy * 1000);
            }
            CacheEntry entry = new CacheEntry(addresses, expiration);
            cache.put(host, entry);
            return this;
        }

        /**
         * 查询特定主机的缓存。如果找到，则返回其 CacheEntry，否则返回 null。
         */
        public CacheEntry get(String host) {
            int policy = getPolicy();
            if (policy == InetAddressCachePolicy.NEVER) {
                return null;
            }
            CacheEntry entry = cache.get(host);

            // 检查条目是否已过期
            if (entry != null && policy != InetAddressCachePolicy.FOREVER) {
                if (entry.expiration >= 0 &&
                    entry.expiration < System.currentTimeMillis()) {
                    cache.remove(host);
                    entry = null;
                }
            }

            return entry;
        }
    }

    /*
     * 如果需要，初始化缓存并将 anyLocalAddress 插入到 unknown 数组中，且不设置过期时间。
     */
    private static void cacheInitIfNeeded() {
        assert Thread.holdsLock(addressCache);
        if (addressCacheInit) {
            return;
        }
        unknown_array = new InetAddress[1];
        unknown_array[0] = impl.anyLocalAddress();

        addressCache.put(impl.anyLocalAddress().getHostName(),
                         unknown_array);

        addressCacheInit = true;
    }

    /*
     * 缓存给定的主机名和地址。
     */
    private static void cacheAddresses(String hostname,
                                       InetAddress[] addresses,
                                       boolean success) {
        hostname = hostname.toLowerCase();
        synchronized (addressCache) {
            cacheInitIfNeeded();
            if (success) {
                addressCache.put(hostname, addresses);
            } else {
                negativeCache.put(hostname, addresses);
            }
        }
    }

    /*
     * 在缓存（正缓存和负缓存）中查找主机名。如果找到则返回地址，否则返回 null。
     */
    private static InetAddress[] getCachedAddresses(String hostname) {
        hostname = hostname.toLowerCase();

        // 搜索正缓存和负缓存

        synchronized (addressCache) {
            cacheInitIfNeeded();

            CacheEntry entry = addressCache.get(hostname);
            if (entry == null) {
                entry = negativeCache.get(hostname);
            }

            if (entry != null) {
                return entry.addresses;
            }
        }

        // 未找到
        return null;
    }

    private static NameService createNSProvider(String provider) {
        if (provider == null)
            return null;

        NameService nameService = null;
        if (provider.equals("default")) {
            // 初始化默认名称服务
            nameService = new NameService() {
                public InetAddress[] lookupAllHostAddr(String host)
                    throws UnknownHostException {
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
                                            "Cannot create name service:"
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

        // 如果提供了并且请求了名称服务，则获取名称服务
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

        // 如果没有指定任何名称服务提供者，则创建一个默认的
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
     * <p> 也不对主机名进行有效性检查。
     *
     * <p> 如果addr指定的是IPv4地址，则返回Inet4Address的实例；
     * 否则，返回Inet6Address的实例。
     *
     * <p> IPv4地址字节数组必须是4字节长，IPv6地址字节数组必须是16字节长
     *
     * @param host 指定的主机
     * @param addr 网络字节顺序的原始IP地址
     * @return 从原始IP地址创建的InetAddress对象。
     * @exception  UnknownHostException 如果IP地址长度非法
     * @since 1.4
     */
    public static InetAddress getByAddress(String host, byte[] addr)
        throws UnknownHostException {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[') {
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
     * "{@code java.sun.com}"，或者是其IP地址的文本表示。如果提供的是IP地址的文本表示，
     * 只检查地址格式的有效性。
     *
     * <p> 对于以字面IPv6地址形式指定的{@code host}，
     * 接受RFC 2732定义的形式或RFC 2373定义的字面IPv6地址格式。也支持IPv6范围地址。
     * 有关IPv6范围地址的描述，请参见<a href="Inet6Address.html#scoped">这里</a>。
     *
     * <p> 如果主机为{@code null}，则返回表示回环接口地址的{@code InetAddress}。
     * 请参见<a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * 第2节和<a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * 第2.5.3节。</p>
     *
     * @param      host   指定的主机，或{@code null}。
     * @return     给定主机名的IP地址。
     * @exception  UnknownHostException  如果找不到指定主机的IP地址，或者为全局IPv6地址指定了scope_id。
     * @exception  SecurityException 如果存在安全管理者，且其checkConnect方法不允许该操作
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
     * "{@code java.sun.com}"，或者是其IP地址的文本表示。如果提供的是IP地址的文本表示，
     * 只检查地址格式的有效性。
     *
     * <p> 对于以<i>字面IPv6地址</i>形式指定的{@code host}，
     * 接受RFC 2732定义的形式或RFC 2373定义的字面IPv6地址格式。字面IPv6地址也可以通过附加范围区域标识符或scope_id来限定。
     * 范围标识符的语法和用法描述请参见
     * <a href="Inet6Address.html#scoped">这里</a>。
     * <p> 如果主机为{@code null}，则返回表示回环接口地址的{@code InetAddress}。
     * 请参见<a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * 第2节和<a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * 第2.5.3节。</p>
     *
     * <p> 如果存在安全管理者且{@code host}不为null且{@code host.length()}不等于零，
     * 则调用安全管理者的
     * {@code checkConnect}方法
     * 以主机名和{@code -1}作为参数，以检查操作是否允许。
     *
     * @param      host   主机名，或{@code null}。
     * @return     给定主机名的所有IP地址数组。
     *
     * @exception  UnknownHostException  如果找不到指定主机的IP地址，或者为全局IPv6地址指定了scope_id。
     * @exception  SecurityException  如果存在安全管理者，且其
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

        if (host == null || host.isEmpty()) {
            InetAddress[] ret = new InetAddress[1];
            ret[0] = impl.loopbackAddress();
            return ret;
        }

        boolean ipv6Expected = false;
        if (host.charAt(0) == '[') {
            // 这应该是一个IPv6字面量
            if (host.length() > 2 && host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
                ipv6Expected = true;
            } else {
                // 这应该是IPv6地址，但不是！
                throw new UnknownHostException(host + ": invalid IPv6 address");
            }
        }

        // 如果主机是IP地址，我们不会进一步查找
        if (IPAddressUtil.digit(host.charAt(0), 16) != -1
            || (host.charAt(0) == ':')) {
            byte[] addr;
            int numericZone = -1;
            String ifname = null;
            // 查看是否为IPv4地址
            try {
                addr = IPAddressUtil.validateNumericFormatV4(host);
            } catch (IllegalArgumentException iae) {
                UnknownHostException uhe = new UnknownHostException(host);
                uhe.initCause(iae);
                throw uhe;
            }
            if (addr == null) {
                // 这应该是一个IPv6字面量
                // 检查是否包含数字或字符串区域ID
                int pos;
                if ((pos=host.indexOf ("%")) != -1) {
                    numericZone = checkNumericZone (host);
                    if (numericZone == -1) { /* 剩余字符串必须是接口名称 */
                        ifname = host.substring (pos+1);
                    }
                }
                if ((addr = IPAddressUtil.textToNumericFormatV6(host)) == null && host.contains(":")) {
                    throw new UnknownHostException(host + ": invalid IPv6 address");
                }
            } else if (ipv6Expected) {
                // 意味着在括号内的IPv4字面量！
                throw new UnknownHostException("["+host+"]");
            }
            InetAddress[] ret = new InetAddress[1];
            if(addr != null) {
                if (addr.length == Inet4Address.INADDRSZ) {
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
            // 我们期望的是IPv6字面量，但得到了其他东西
            throw new UnknownHostException("["+host+"]");
        }
        return getAllByName0(host, reqAddr, true);
    }

    /**
     * 返回回环地址。
     * <p>
     * 返回的InetAddress将表示IPv4
     * 回环地址，127.0.0.1，或IPv6
     * 回环地址，::1。返回的IPv4回环地址
     * 只是127.*.*.*形式中的一个。
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
        if (percent == -1) {
            return -1;
        }
        for (int i=percent+1; i<slen; i++) {
            char c = s.charAt(i);
            if (c == ']') {
                if (i == percent+1) {
                    /* 空百分号字段 */
                    return -1;
                }
                break;
            }
            if ((digit = Character.digit (c, 10)) < 0) {
                return -1;
            }
            zone = (zone * 10) + digit;
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
        return getAllByName0 (host, null, check);
    }

    private static InetAddress[] getAllByName0 (String host, InetAddress reqAddr, boolean check)
        throws UnknownHostException  {

        /* 如果到达这里，假定它是一个主机名 */
        /* Cache.get可以返回：null、unknownAddress或InetAddress[] */

        /* 在提供主机名之前，确保连接到主机是允许的 */
        if (check) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, -1);
            }
        }

        InetAddress[] addresses = getCachedAddresses(host);

        /* 如果缓存中没有条目，则执行主机查找 */
        if (addresses == null) {
            addresses = getAddressesFromNameService(host, reqAddr);
        }

        if (addresses == unknown_array)
            throw new UnknownHostException(host);

        return addresses.clone();
    }

    private static InetAddress[] getAddressesFromNameService(String host, InetAddress reqAddr)
        throws UnknownHostException
    {
        InetAddress[] addresses = null;
        boolean success = false;
        UnknownHostException ex = null;

        // 检查主机是否在查找表中。
        // 1) 如果在调用checkLookupTable()时主机不在查找表中，
        //    checkLookupTable()会将主机添加到查找表中并
        //    返回null。所以我们将会进行查找。
        // 2) 如果在调用checkLookupTable()时主机在查找表中，
        //    当前线程会被阻塞，直到主机从查找表中移除。
        //    然后这个线程应该尝试查找地址缓存。
        //     i) 如果在地址缓存中找到了地址，
        //        checkLookupTable()会返回地址。
        //     ii) 如果由于任何原因在地址缓存中没有找到地址，
        //         它应该将主机添加到查找表中并返回null，
        //         以便以下代码自行进行查找。
        if ((addresses = checkLookupTable(host)) == null) {
            try {
                // 这是第一个查找该主机地址的线程
                // 或者该主机的缓存条目已过期，所以这个线程应该进行查找。
                for (NameService nameService : nameServices) {
                    try {
                        /*
                         * 不要在构造函数中调用lookup()。
                         * 如果你这样做，即使查找失败，你仍然会分配空间。
                         */


                                    addresses = nameService.lookupAllHostAddr(host);
                        success = true;
                        break;
                    } catch (UnknownHostException uhe) {
                        if (host.equalsIgnoreCase("localhost")) {
                            InetAddress[] local = new InetAddress[] { impl.loopbackAddress() };
                            addresses = local;
                            success = true;
                            break;
                        }
                        else {
                            addresses = unknown_array;
                            success = false;
                            ex = uhe;
                        }
                    }
                }

                // 还有其他操作？
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
                // 缓存地址。
                cacheAddresses(host, addresses, success);

                if (!success && ex != null)
                    throw ex;

            } finally {
                // 从查找表中删除主机并通知
                // 所有等待查找表监视器的线程。
                updateLookupTable(host);
            }
        }

        return addresses;
    }


    private static InetAddress[] checkLookupTable(String host) {
        synchronized (lookupTable) {
            // 如果主机不在查找表中，将其添加到
            // 查找表并返回 null。调用者应该
            // 进行查找。
            if (lookupTable.containsKey(host) == false) {
                lookupTable.put(host, null);
                return null;
            }

            // 如果主机在查找表中，这意味着另一个
            // 线程正在尝试查找该主机的地址。
            // 这个线程应该等待。
            while (lookupTable.containsKey(host)) {
                try {
                    lookupTable.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // 另一个线程已经完成了对主机地址的查找。
        // 这个线程应该重试从地址缓存中获取地址。
        // 如果它没有从缓存中获取到地址，它将尝试自己查找地址。
        InetAddress[] addresses = getCachedAddresses(host);
        if (addresses == null) {
            synchronized (lookupTable) {
                lookupTable.put(host, null);
                return null;
            }
        }

        return addresses;
    }

    private static void updateLookupTable(String host) {
        synchronized (lookupTable) {
            lookupTable.remove(host);
            lookupTable.notifyAll();
        }
    }

    /**
     * 给定原始 IP 地址，返回一个 {@code InetAddress} 对象。
     * 参数是以网络字节顺序排列的：地址的最高字节在 {@code getAddress()[0]} 中。
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

    private static InetAddress cachedLocalHost = null;
    private static long cacheTime = 0;
    private static final long maxCacheTime = 5000L;
    private static final Object cacheLock = new Object();

    /**
     * 返回本地主机的地址。这是通过从系统中检索主机名，然后将该名称解析为
     * 一个 {@code InetAddress} 来实现的。
     *
     * <P>注意：解析的地址可能会在短时间内被缓存。
     * </P>
     *
     * <p>如果有安全管理者，其
     * {@code checkConnect} 方法将被调用
     * 以本地主机名和 {@code -1} 作为参数，以检查操作是否允许。
     * 如果操作不允许，将返回一个表示环回地址的 InetAddress。
     *
     * @return 本地主机的地址。
     *
     * @exception  UnknownHostException 如果本地主机名无法解析为地址。
     *
     * @see SecurityManager#checkConnect
     * @see java.net.InetAddress#getByName(java.lang.String)
     */
    public static InetAddress getLocalHost() throws UnknownHostException {

        SecurityManager security = System.getSecurityManager();
        try {
            String local = impl.getLocalHostName();

            if (security != null) {
                security.checkConnect(local, -1);
            }

            if (local.equals("localhost")) {
                return impl.loopbackAddress();
            }

            InetAddress ret = null;
            synchronized (cacheLock) {
                long now = System.currentTimeMillis();
                if (cachedLocalHost != null) {
                    if ((now - cacheTime) < maxCacheTime) // 小于 5 秒？
                        ret = cachedLocalHost;
                    else
                        cachedLocalHost = null;
                }


                            // 我们直接调用getAddressesFromNameService
                // 以避免从缓存中获取本地主机
                if (ret == null) {
                    InetAddress[] localAddrs;
                    try {
                        localAddrs =
                            InetAddress.getAddressesFromNameService(local, null);
                    } catch (UnknownHostException uhe) {
                        // 重新抛出带有更多详细错误信息的异常。
                        UnknownHostException uhe2 =
                            new UnknownHostException(local + ": " +
                                                     uhe.getMessage());
                        uhe2.initCause(uhe);
                        throw uhe2;
                    }
                    cachedLocalHost = localAddrs[0];
                    cacheTime = now;
                    ret = localAddrs[0];
                }
            }
            return ret;
        } catch (java.lang.SecurityException e) {
            return impl.loopbackAddress();
        }
    }

    /**
     * 执行类加载时的初始化。
     */
    private static native void init();


    /*
     * 返回表示anyLocalAddress的InetAddress
     * （通常是0.0.0.0或::0）
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
         * 属性 "impl.prefix" 将被附加到我们实例化的实现对象的类名前，
         * 我们将委托实际工作（如本地方法）给该对象。此属性可以在
         * 不同的java类实现中有所不同。默认值为空字符串 ""。
         */
        String prefix = AccessController.doPrivileged(
                      new GetPropertyAction("impl.prefix", ""));
        try {
            impl = Class.forName("java.net." + prefix + implName).newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: java.net." + prefix +
                               implName + ":\n检查属性文件中的impl.prefix属性。");
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate: java.net." + prefix +
                               implName + ":\n检查属性文件中的impl.prefix属性。");
        } catch (IllegalAccessException e) {
            System.err.println("Cannot access class: java.net." + prefix +
                               implName + ":\n检查属性文件中的impl.prefix属性。");
        }

        if (impl == null) {
            try {
                impl = Class.forName(implName).newInstance();
            } catch (Exception e) {
                throw new Error("系统属性impl.prefix不正确");
            }
        }

        return (InetAddressImpl) impl;
    }

    private void readObjectNoData (ObjectInputStream s) throws
                         IOException, ClassNotFoundException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException ("无效的地址类型");
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
            throw new SecurityException ("无效的地址类型");
        }
        GetField gf = s.readFields();
        String host = (String)gf.get("hostName", null);
        int address = gf.get("address", 0);
        int family = gf.get("family", 0);
        if (family != IPv4 && family != IPv6) {
            throw new InvalidObjectException("无效的地址族类型: " + family);
        }
        InetAddressHolder h = new InetAddressHolder(host, address, family);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    /* 需要因为序列化字段不再存在 */

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
            throw new SecurityException ("无效的地址类型");
        }
        PutField pf = s.putFields();
        pf.put("hostName", holder().getHostName());
        pf.put("address", holder().getAddress());
        pf.put("family", holder().getFamily());
        s.writeFields();
    }
}

/*
 * 简单的工厂类来创建实现
 */
class InetAddressImplFactory {

    static InetAddressImpl create() {
        return InetAddress.loadImpl(isIPv6Supported() ?
                                    "Inet6AddressImpl" : "Inet4AddressImpl");
    }

    static native boolean isIPv6Supported();
}
