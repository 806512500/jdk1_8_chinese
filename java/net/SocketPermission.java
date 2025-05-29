
/*
 * Copyright (c) 1997, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Enumeration;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.Security;
import java.io.Serializable;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import sun.net.util.IPAddressUtil;
import sun.net.RegisteredDomain;
import sun.net.PortConfig;
import sun.security.util.SecurityConstants;
import sun.security.util.Debug;


/**
 * 该类表示通过套接字访问网络。
 * SocketPermission 由一个主机规范和一组指定连接到该主机方式的“操作”组成。主机规范为：
 * <pre>
 *    host = (hostname | IPv4address | iPv6reference) [:portrange]
 *    portrange = portnumber | -portnumber | portnumber-[portnumber]
 * </pre>
 * 主机可以表示为 DNS 名称、数字 IP 地址或“localhost”（本地机器）。
 * 通配符“*”可以在 DNS 名称主机规范中使用一次。如果使用，它必须位于最左侧，例如“*.sun.com”。
 * <p>
 * IPv6reference 的格式应遵循 <a
 * href="http://www.ietf.org/rfc/rfc2732.txt"><i>RFC&nbsp;2732: URL 中的字面 IPv6 地址格式</i></a>：
 * <pre>
 *    ipv6reference = "[" IPv6address "]"
 *</pre>
 * 例如，可以构建一个 SocketPermission 实例如下：
 * <pre>
 *    String hostAddress = inetaddress.getHostAddress();
 *    if (inetaddress instanceof Inet6Address) {
 *        sp = new SocketPermission("[" + hostAddress + "]:" + port, action);
 *    } else {
 *        sp = new SocketPermission(hostAddress + ":" + port, action);
 *    }
 * </pre>
 * 或
 * <pre>
 *    String host = url.getHost();
 *    sp = new SocketPermission(host + ":" + port, action);
 * </pre>
 * <p>
 * <A HREF="Inet6Address.html#lform">完整的未压缩形式</A>的 IPv6 字面地址也是有效的。
 * <p>
 * 端口或端口范围是可选的。形式为 "N-" 的端口规范，其中 <i>N</i> 是一个端口号，表示所有编号为 <i>N</i> 及以上的端口，而形式为 "-N" 的规范表示所有编号为 <i>N</i> 及以下的端口。
 * 特殊端口值 {@code 0} 指的是整个 <i>临时</i> 端口范围。这是系统可能用于分配动态端口的固定范围。实际范围可能因系统而异。
 * <p>
 * 连接到主机的可能方式为：
 * <pre>
 * accept
 * connect
 * listen
 * resolve
 * </pre>
 * “listen” 操作仅在与 “localhost” 一起使用时有意义，表示绑定到指定端口的能力。
 * “resolve” 操作在其他任何操作存在时隐含。操作 “resolve” 指的是主机/IP 名称服务查询。
 * <P>
 * 操作字符串在处理前转换为小写。
 * <p>作为 SocketPermissions 创建和含义的一个例子，
 * 注意，如果授予以下权限：
 *
 * <pre>
 *   p1 = new SocketPermission("puffin.eng.sun.com:7777", "connect,accept");
 * </pre>
 *
 * 它允许代码连接到 {@code puffin.eng.sun.com} 的 7777 端口，并在该端口上接受连接。
 *
 * <p>同样，如果授予以下权限：
 *
 * <pre>
 *   p2 = new SocketPermission("localhost:1024-", "accept,connect,listen");
 * </pre>
 *
 * 它允许代码在本地主机的 1024 到 65535 之间的任何端口上接受连接、连接或监听。
 *
 * <p>注意：授予代码接受或连接到远程主机的权限可能是危险的，因为恶意代码可以更容易地在可能没有数据访问权限的各方之间传输和共享机密数据。
 *
 * @see java.security.Permissions
 * @see SocketPermission
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 *
 * @serial exclude
 */

public final class SocketPermission extends Permission
    implements java.io.Serializable
{
    private static final long serialVersionUID = -7204263841984476862L;

    /**
     * 连接到主机:端口
     */
    private final static int CONNECT    = 0x1;

    /**
     * 在主机:端口上监听
     */
    private final static int LISTEN     = 0x2;

    /**
     * 接受来自主机:端口的连接
     */
    private final static int ACCEPT     = 0x4;

    /**
     * 解析 DNS 查询
     */
    private final static int RESOLVE    = 0x8;

    /**
     * 无操作
     */
    private final static int NONE               = 0x0;

    /**
     * 所有操作
     */
    private final static int ALL        = CONNECT|LISTEN|ACCEPT|RESOLVE;

    // 各种端口常量
    private static final int PORT_MIN = 0;
    private static final int PORT_MAX = 65535;
    private static final int PRIV_PORT_MAX = 1023;
    private static final int DEF_EPH_LOW = 49152;

    // 操作掩码
    private transient int mask;

    /**
     * 操作字符串。
     *
     * @serial
     */

    private String actions; // 尽可能长时间保持为空，然后
                            // 在 getAction 函数中创建并重用。

    // 传递的主机名部分
    private transient String hostname;

    // 主机的规范名称
    // 在“*.foo.com”的情况下，cname 是“.foo.com”。

    private transient String cname;

    // 主机的所有 IP 地址
    private transient InetAddress[] addresses;

    // 如果主机名是通配符（例如“*.sun.com”），则为 true
    private transient boolean wildcard;

    // 如果使用单个数字 IP 地址初始化，则为 true
    private transient boolean init_with_ip;


                // 如果此 SocketPermission 表示无效/未知的主机，则为 true
    // 用于 implies，当延迟查找已失败时使用
    private transient boolean invalid;

    // 主机上的端口范围
    private transient int[] portrange;

    private transient boolean defaultDeny = false;

    // 如果此 SocketPermission 表示一个主机名
    // 该主机名未能通过我们的反向映射启发式测试，则为 true
    private transient boolean untrusted;
    private transient boolean trusted;

    // 如果 sun.net.trustNameService 系统属性已设置，则为 true
    private static boolean trustNameService;

    private static Debug debug = null;
    private static boolean debugInit = false;

    // 懒初始化器
    private static class EphemeralRange {
        static final int low = initEphemeralPorts("low", DEF_EPH_LOW);
        static final int high = initEphemeralPorts("high", PORT_MAX);
    };

    static {
        Boolean tmp = java.security.AccessController.doPrivileged(
                new sun.security.action.GetBooleanAction("sun.net.trustNameService"));
        trustNameService = tmp.booleanValue();
    }

    private static synchronized Debug getDebug() {
        if (!debugInit) {
            debug = Debug.getInstance("access");
            debugInit = true;
        }
        return debug;
    }

    /**
     * 使用指定的操作创建一个新的 SocketPermission 对象。
     * 主机表示为 DNS 名称或数字 IP 地址。
     * 可选地，可以提供一个端口或端口范围（与 DNS 名称或 IP 地址用冒号分隔）。
     * <p>
     * 要指定本地机器，使用 "localhost" 作为 <i>主机</i>。
     * 还要注意：空的 <i>主机</i> 字符串 ("") 等同于 "localhost"。
     * <p>
     * <i>操作</i> 参数包含一个以逗号分隔的列表，表示为指定的主机（和端口）授予的操作。可能的操作有
     * "connect", "listen", "accept", "resolve"，或这些操作的任意组合。当指定了其他三个操作中的任何一个时，"resolve" 会自动添加。
     * <p>
     * 以下是一些 SocketPermission 实例化的示例：
     * <pre>
     *    nr = new SocketPermission("www.catalog.com", "connect");
     *    nr = new SocketPermission("www.sun.com:80", "connect");
     *    nr = new SocketPermission("*.sun.com", "connect");
     *    nr = new SocketPermission("*.edu", "resolve");
     *    nr = new SocketPermission("204.160.241.0", "connect");
     *    nr = new SocketPermission("localhost:1024-65535", "listen");
     *    nr = new SocketPermission("204.160.241.0:1024-65535", "connect");
     * </pre>
     *
     * @param host 计算机的主机名或 IP 地址，可选地包括一个冒号后跟一个端口或端口范围。
     * @param action 操作字符串。
     */
    public SocketPermission(String host, String action) {
        super(getHost(host));
        // name 初始化为 getHost(host); 在 getHost() 中检测到 NPE
        init(getName(), getMask(action));
    }


    SocketPermission(String host, int mask) {
        super(getHost(host));
        // name 初始化为 getHost(host); 在 getHost() 中检测到 NPE
        init(getName(), mask);
    }

    private void setDeny() {
        defaultDeny = true;
    }

    private static String getHost(String host) {
        if (host.equals("")) {
            return "localhost";
        } else {
            /* 在此上下文中使用的 IPv6 文字地址应遵循
             * RFC 2732 中指定的格式；
             * 如果不是，我们尝试解决所有无歧义的情况
             */
            int ind;
            if (host.charAt(0) != '[') {
                if ((ind = host.indexOf(':')) != host.lastIndexOf(':')) {
                    /* 多于一个 ":", 意味着 IPv6 地址不是
                     * RFC 2732 格式；
                     * 我们将为所有无歧义的情况纠正用户错误
                     */
                    StringTokenizer st = new StringTokenizer(host, ":");
                    int tokens = st.countTokens();
                    if (tokens == 9) {
                        // IPv6 地址后跟端口
                        ind = host.lastIndexOf(':');
                        host = "[" + host.substring(0, ind) + "]" +
                            host.substring(ind);
                    } else if (tokens == 8 && host.indexOf("::") == -1) {
                        // 仅 IPv6 地址，不后跟端口
                        host = "[" + host + "]";
                    } else {
                        // 可能有歧义
                        throw new IllegalArgumentException("Ambiguous"+
                                                           " hostport part");
                    }
                }
            }
            return host;
        }
    }

    private int[] parsePort(String port)
        throws Exception
    {

        if (port == null || port.equals("") || port.equals("*")) {
            return new int[] {PORT_MIN, PORT_MAX};
        }

        int dash = port.indexOf('-');

        if (dash == -1) {
            int p = Integer.parseInt(port);
            return new int[] {p, p};
        } else {
            String low = port.substring(0, dash);
            String high = port.substring(dash+1);
            int l,h;

            if (low.equals("")) {
                l = PORT_MIN;
            } else {
                l = Integer.parseInt(low);
            }

            if (high.equals("")) {
                h = PORT_MAX;
            } else {
                h = Integer.parseInt(high);
            }
            if (l < 0 || h < 0 || h<l)
                throw new IllegalArgumentException("invalid port range");

            return new int[] {l, h};
        }
    }

    /**
     * 如果权限指定了零
     * 作为其值（或下限），表示临时范围，则返回 true
     */
    private boolean includesEphemerals() {
        return portrange[0] == 0;
    }

    /**
     * 初始化 SocketPermission 对象。此时我们不做任何 DNS 查找，
     * 而是等到 implies 方法被调用时再进行。
     */
    private void init(String host, int mask) {
        // 设置表示操作的整数掩码


                    if ((mask & ALL) != mask)
            throw new IllegalArgumentException("无效的操作掩码");

        // 如果允许任何其他操作，则始终 OR 进 RESOLVE
        this.mask = mask | RESOLVE;

        // 解析主机名。名称最多可以有三个部分，主机名、端口号，或表示端口范围的两个数字。
        // "www.sun.com:8080-9090" 是一个有效的主机名。

        // IPv6 地址可以是 2010:836B:4179::836B:4179
        // IPv6 地址需要用方括号括起来
        // 例如：[2010:836B:4179::836B:4179]:8080-9090
        // 请参阅 RFC 2732 以获取更多信息。

        int rb = 0 ;
        int start = 0, end = 0;
        int sep = -1;
        String hostport = host;
        if (host.charAt(0) == '[') {
            start = 1;
            rb = host.indexOf(']');
            if (rb != -1) {
                host = host.substring(start, rb);
            } else {
                throw new
                    IllegalArgumentException("无效的主机/端口: "+host);
            }
            sep = hostport.indexOf(':', rb+1);
        } else {
            start = 0;
            sep = host.indexOf(':', rb);
            end = sep;
            if (sep != -1) {
                host = host.substring(start, end);
            }
        }

        if (sep != -1) {
            String port = hostport.substring(sep+1);
            try {
                portrange = parsePort(port);
            } catch (Exception e) {
                throw new
                    IllegalArgumentException("无效的端口范围: "+port);
            }
        } else {
            portrange = new int[] { PORT_MIN, PORT_MAX };
        }

        hostname = host;

        // 这是一个域通配符规范吗
        if (host.lastIndexOf('*') > 0) {
            throw new
               IllegalArgumentException("无效的主机通配符规范");
        } else if (host.startsWith("*")) {
            wildcard = true;
            if (host.equals("*")) {
                cname = "";
            } else if (host.startsWith("*.")) {
                cname = host.substring(1).toLowerCase();
            } else {
              throw new
               IllegalArgumentException("无效的主机通配符规范");
            }
            return;
        } else {
            if (!host.isEmpty()) {
                // 查看是否使用 IP 地址进行初始化。
                char ch = host.charAt(0);
                if (ch == ':' || IPAddressUtil.digit(ch, 16) != -1) {
                    byte ip[] = IPAddressUtil.textToNumericFormatV4(host);
                    if (ip == null) {
                        ip = IPAddressUtil.textToNumericFormatV6(host);
                    }
                    if (ip != null) {
                        try {
                            addresses =
                                new InetAddress[]
                                {InetAddress.getByAddress(ip) };
                            init_with_ip = true;
                        } catch (UnknownHostException uhe) {
                            // 这不应该发生
                            invalid = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * 将操作字符串转换为整数操作掩码。
     *
     * @param action 操作字符串
     * @return 操作掩码
     */
    private static int getMask(String action) {

        if (action == null) {
            throw new NullPointerException("操作不能为 null");
        }

        if (action.equals("")) {
            throw new IllegalArgumentException("操作不能为空");
        }

        int mask = NONE;

        // 使用对象身份比较已知的字符串以提高性能（这些值在 JDK 中频繁使用）。
        if (action == SecurityConstants.SOCKET_RESOLVE_ACTION) {
            return RESOLVE;
        } else if (action == SecurityConstants.SOCKET_CONNECT_ACTION) {
            return CONNECT;
        } else if (action == SecurityConstants.SOCKET_LISTEN_ACTION) {
            return LISTEN;
        } else if (action == SecurityConstants.SOCKET_ACCEPT_ACTION) {
            return ACCEPT;
        } else if (action == SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION) {
            return CONNECT|ACCEPT;
        }

        char[] a = action.toCharArray();

        int i = a.length - 1;
        if (i < 0)
            return mask;

        while (i != -1) {
            char c;

            // 跳过空白字符
            while ((i!=-1) && ((c = a[i]) == ' ' ||
                               c == '\r' ||
                               c == '\n' ||
                               c == '\f' ||
                               c == '\t'))
                i--;

            // 检查已知字符串
            int matchlen;

            if (i >= 6 && (a[i-6] == 'c' || a[i-6] == 'C') &&
                          (a[i-5] == 'o' || a[i-5] == 'O') &&
                          (a[i-4] == 'n' || a[i-4] == 'N') &&
                          (a[i-3] == 'n' || a[i-3] == 'N') &&
                          (a[i-2] == 'e' || a[i-2] == 'E') &&
                          (a[i-1] == 'c' || a[i-1] == 'C') &&
                          (a[i] == 't' || a[i] == 'T'))
            {
                matchlen = 7;
                mask |= CONNECT;

            } else if (i >= 6 && (a[i-6] == 'r' || a[i-6] == 'R') &&
                                 (a[i-5] == 'e' || a[i-5] == 'E') &&
                                 (a[i-4] == 's' || a[i-4] == 'S') &&
                                 (a[i-3] == 'o' || a[i-3] == 'O') &&
                                 (a[i-2] == 'l' || a[i-2] == 'L') &&
                                 (a[i-1] == 'v' || a[i-1] == 'V') &&
                                 (a[i] == 'e' || a[i] == 'E'))
            {
                matchlen = 7;
                mask |= RESOLVE;

            } else if (i >= 5 && (a[i-5] == 'l' || a[i-5] == 'L') &&
                                 (a[i-4] == 'i' || a[i-4] == 'I') &&
                                 (a[i-3] == 's' || a[i-3] == 'S') &&
                                 (a[i-2] == 't' || a[i-2] == 'T') &&
                                 (a[i-1] == 'e' || a[i-1] == 'E') &&
                                 (a[i] == 'n' || a[i] == 'N'))
            {
                matchlen = 6;
                mask |= LISTEN;


                        } else if (i >= 5 && (a[i-5] == 'a' || a[i-5] == 'A') &&
                                 (a[i-4] == 'c' || a[i-4] == 'C') &&
                                 (a[i-3] == 'c' || a[i-3] == 'C') &&
                                 (a[i-2] == 'e' || a[i-2] == 'E') &&
                                 (a[i-1] == 'p' || a[i-1] == 'P') &&
                                 (a[i] == 't' || a[i] == 'T'))
            {
                matchlen = 6;
                mask |= ACCEPT;

            } else {
                // 解析错误
                throw new IllegalArgumentException(
                        "无效的权限: " + action);
            }

            // 确保我们没有匹配到一个单词的尾部
            // 比如 "ackbarfaccept"。同时，跳到逗号。
            boolean seencomma = false;
            while (i >= matchlen && !seencomma) {
                switch(a[i-matchlen]) {
                case ',':
                    seencomma = true;
                    break;
                case ' ': case '\r': case '\n':
                case '\f': case '\t':
                    break;
                default:
                    throw new IllegalArgumentException(
                            "无效的权限: " + action);
                }
                i--;
            }

            // 将 i 指向逗号的位置减一（或 -1）。
            i -= matchlen;
        }

        return mask;
    }

    private boolean isUntrusted()
        throws UnknownHostException
    {
        if (trusted) return false;
        if (invalid || untrusted) return true;
        try {
            if (!trustNameService && (defaultDeny ||
                sun.net.www.URLConnection.isProxiedHost(hostname))) {
                if (this.cname == null) {
                    this.getCanonName();
                }
                if (!match(cname, hostname)) {
                    // 最后一次机会
                    if (!authorized(hostname, addresses[0].getAddress())) {
                        untrusted = true;
                        Debug debug = getDebug();
                        if (debug != null && Debug.isOn("failure")) {
                            debug.println("socket 访问限制: 代理主机 " + "(" + addresses[0] + ")" + " 不匹配 " + cname + " 从反向查找");
                        }
                        return true;
                    }
                }
                trusted = true;
            }
        } catch (UnknownHostException uhe) {
            invalid = true;
            throw uhe;
        }
        return false;
    }

    /**
     * 尝试获取完全限定域名
     *
     */
    void getCanonName()
        throws UnknownHostException
    {
        if (cname != null || invalid || untrusted) return;

        // 尝试获取规范名称

        try {
            // 首先获取 IP 地址，如果我们还没有它们
            // 这是因为我们需要 IP 地址来获取
            // FQDN。
            if (addresses == null) {
                getIP();
            }

            // 我们必须进行此检查，否则我们可能无法
            // 获取完全限定域名
            if (init_with_ip) {
                cname = addresses[0].getHostName(false).toLowerCase();
            } else {
             cname = InetAddress.getByName(addresses[0].getHostAddress()).
                                              getHostName(false).toLowerCase();
            }
        } catch (UnknownHostException uhe) {
            invalid = true;
            throw uhe;
        }
    }

    private transient String cdomain, hdomain;

    private boolean match(String cname, String hname) {
        String a = cname.toLowerCase();
        String b = hname.toLowerCase();
        if (a.startsWith(b)  &&
            ((a.length() == b.length()) || (a.charAt(b.length()) == '.')))
            return true;
        if (cdomain == null) {
            cdomain = RegisteredDomain.getRegisteredDomain(a);
        }
        if (hdomain == null) {
            hdomain = RegisteredDomain.getRegisteredDomain(b);
        }

        return !cdomain.isEmpty() && !hdomain.isEmpty() && cdomain.equals(hdomain);
    }

    private boolean authorized(String cname, byte[] addr) {
        if (addr.length == 4)
            return authorizedIPv4(cname, addr);
        else if (addr.length == 16)
            return authorizedIPv6(cname, addr);
        else
            return false;
    }

    private boolean authorizedIPv4(String cname, byte[] addr) {
        String authHost = "";
        InetAddress auth;

        try {
            authHost = "auth." +
                        (addr[3] & 0xff) + "." + (addr[2] & 0xff) + "." +
                        (addr[1] & 0xff) + "." + (addr[0] & 0xff) +
                        ".in-addr.arpa";
            // 以下检查似乎没有必要
            // auth = InetAddress.getAllByName0(authHost, false)[0];
            authHost = hostname + '.' + authHost;
            auth = InetAddress.getAllByName0(authHost, false)[0];
            if (auth.equals(InetAddress.getByAddress(addr))) {
                return true;
            }
            Debug debug = getDebug();
            if (debug != null && Debug.isOn("failure")) {
                debug.println("socket 访问限制: IP 地址 " + auth + " != " + InetAddress.getByAddress(addr));
            }
        } catch (UnknownHostException uhe) {
            Debug debug = getDebug();
            if (debug != null && Debug.isOn("failure")) {
                debug.println("socket 访问限制: 正向查找失败 " + authHost);
            }
        }
        return false;
    }

    private boolean authorizedIPv6(String cname, byte[] addr) {
        String authHost = "";
        InetAddress auth;

        try {
            StringBuffer sb = new StringBuffer(39);

            for (int i = 15; i >= 0; i--) {
                sb.append(Integer.toHexString(((addr[i]) & 0x0f)));
                sb.append('.');
                sb.append(Integer.toHexString(((addr[i] >> 4) & 0x0f)));
                sb.append('.');
            }
            authHost = "auth." + sb.toString() + "IP6.ARPA";
            //auth = InetAddress.getAllByName0(authHost, false)[0];
            authHost = hostname + '.' + authHost;
            auth = InetAddress.getAllByName0(authHost, false)[0];
            if (auth.equals(InetAddress.getByAddress(addr)))
                return true;
            Debug debug = getDebug();
            if (debug != null && Debug.isOn("failure")) {
                debug.println("socket 访问限制: IP 地址 " + auth + " != " + InetAddress.getByAddress(addr));
            }
        } catch (UnknownHostException uhe) {
            Debug debug = getDebug();
            if (debug != null && Debug.isOn("failure")) {
                debug.println("socket 访问限制: 正向查找失败 " + authHost);
            }
        }
        return false;
    }

    /**
     * 获取 IP 地址。如果无法获取它们，则将无效设置为 true。
     *
     */
    void getIP()
        throws UnknownHostException
    {
        if (addresses != null || wildcard || invalid) return;

        try {
            // 现在获取所有 IP 地址
            String host;
            if (getName().charAt(0) == '[') {
                // 字面 IPv6 地址
                host = getName().substring(1, getName().indexOf(']'));
            } else {
                int i = getName().indexOf(":");
                if (i == -1)
                    host = getName();
                else {
                    host = getName().substring(0,i);
                }
            }

            addresses =
                new InetAddress[] {InetAddress.getAllByName0(host, false)[0]};

        } catch (UnknownHostException uhe) {
            invalid = true;
            throw uhe;
        }  catch (IndexOutOfBoundsException iobe) {
            invalid = true;
            throw new UnknownHostException(getName());
        }
    }

    /**
     * 检查此套接字权限对象是否“隐含”指定的权限。
     * <P>
     * 更具体地说，此方法首先确保以下所有条件都为真（如果任何条件不为真，则返回 false）：
     * <ul>
     * <li> <i>p</i> 是 SocketPermission 的实例，
     * <li> <i>p</i> 的操作是此对象操作的适当子集，以及
     * <li> <i>p</i> 的端口范围包含在此端口范围内。注意：当 p 仅包含 'resolve' 操作时，端口范围被忽略。
     * </ul>
     *
     * 然后 {@code implies} 按顺序检查以下每一项，并且如果所述条件为真，则返回 true：
     * <ul>
     * <li> 如果此对象使用单个 IP 地址初始化，并且 <i>p</i> 的 IP 地址之一等于此对象的 IP 地址。
     * <li> 如果此对象是通配符域（如 *.sun.com），并且 <i>p</i> 的规范名称（没有前面的 *）以该对象的规范主机名结尾。例如，*.sun.com 隐含 *.eng.sun.com。
     * <li> 如果此对象未使用单个 IP 地址初始化，并且此对象的 IP 地址之一等于 <i>p</i> 的 IP 地址之一。
     * <li> 如果此规范名称等于 <i>p</i> 的规范名称。
     * </ul>
     *
     * 如果以上任何一项都不为真，{@code implies} 返回 false。
     * @param p 要检查的权限。
     *
     * @return 如果指定的权限由该对象隐含，则返回 true，否则返回 false。
     */
    public boolean implies(Permission p) {
        int i,j;

        if (!(p instanceof SocketPermission))
            return false;

        if (p == this)
            return true;

        SocketPermission that = (SocketPermission) p;

        return ((this.mask & that.mask) == that.mask) &&
                                        impliesIgnoreMask(that);
    }

    /**
     * 检查传入的权限的操作是否是此对象操作的适当子集。
     * <P>
     * 按以下顺序检查：
     * <ul>
     * <li> 检查 "p" 是否是 SocketPermission 的实例
     * <li> 检查 "p" 的操作是否是当前对象操作的适当子集。
     * <li> 检查 "p" 的端口范围是否包含在此端口范围内
     * <li> 如果此对象使用 IP 地址初始化，检查 "p" 的 IP 地址之一是否等于此对象的 IP 地址。
     * <li> 如果任一对象是通配符域（即，"*.sun.com"），尝试基于通配符进行匹配。
     * <li> 如果此对象未使用 IP 地址初始化，尝试基于两个对象中的 IP 地址进行匹配。
     * <li> 尝试基于两个对象的规范主机名进行匹配。
     * </ul>
     * @param that 传入的权限请求
     *
     * @return 如果 "permission" 是当前对象的适当子集，则返回 true，否则返回 false。
     */
    boolean impliesIgnoreMask(SocketPermission that) {
        int i,j;

        if ((that.mask & RESOLVE) != that.mask) {

            // 检查简单的端口范围
            if ((that.portrange[0] < this.portrange[0]) ||
                    (that.portrange[1] > this.portrange[1])) {

                // 如果任一包括临时范围，执行完整检查
                if (this.includesEphemerals() || that.includesEphemerals()) {
                    if (!inRange(this.portrange[0], this.portrange[1],
                                     that.portrange[0], that.portrange[1]))
                    {
                                return false;
                    }
                } else {
                    return false;
                }
            }
        }

        // 允许 "*" 通配符始终匹配任何内容
        if (this.wildcard && "".equals(this.cname))
            return true;

        // 如果任一这些 NetPerm 对象无效，则返回...
        if (this.invalid || that.invalid) {
            return compareHostnames(that);
        }

        try {
            if (this.init_with_ip) { // 我们只检查 IP 地址
                if (that.wildcard)
                    return false;

                if (that.init_with_ip) {
                    return (this.addresses[0].equals(that.addresses[0]));
                } else {
                    if (that.addresses == null) {
                        that.getIP();
                    }
                    for (i=0; i < that.addresses.length; i++) {
                        if (this.addresses[0].equals(that.addresses[i]))
                            return true;
                    }
                }
                // 由于 "this" 使用 IP 地址初始化，我们
                // 不检查其他情况
                return false;
            }

            // 检查是否有任何通配符...
            if (this.wildcard || that.wildcard) {
                // 如果它们都是通配符，返回 true 当且仅当
                // that 的 cname 以 this 的 cname 结尾（即，*.sun.com
                // 隐含 *.eng.sun.com）
                if (this.wildcard && that.wildcard)
                    return (that.cname.endsWith(this.cname));


                            // 一个非通配符不能隐含一个通配符
                if (that.wildcard)
                    return false;

                // 这是一个通配符，让我们看看 that 的规范名称是否以
                // 它的规范名称结尾...
                if (that.cname == null) {
                    that.getCanonName();
                }
                return (that.cname.endsWith(this.cname));
            }

            // 比较 IP 地址
            if (this.addresses == null) {
                this.getIP();
            }

            if (that.addresses == null) {
                that.getIP();
            }

            if (!(that.init_with_ip && this.isUntrusted())) {
                for (j = 0; j < this.addresses.length; j++) {
                    for (i=0; i < that.addresses.length; i++) {
                        if (this.addresses[j].equals(that.addresses[i]))
                            return true;
                    }
                }

                // XXX: 如果所有其他方法都失败了，比较主机名？
                // 我们真的想要这样做吗？
                if (this.cname == null) {
                    this.getCanonName();
                }

                if (that.cname == null) {
                    that.getCanonName();
                }

                return (this.cname.equalsIgnoreCase(that.cname));
            }

        } catch (UnknownHostException uhe) {
            return compareHostnames(that);
        }

        // 确保在这里做的第一件事是返回
        // false。如果不是，请取消上面 catch 中的 return false 的注释。

        return false;
    }

    private boolean compareHostnames(SocketPermission that) {
        // 我们检查传入的原始名称/IP 是否相等。

        String thisHost = hostname;
        String thatHost = that.hostname;

        if (thisHost == null) {
            return false;
        } else if (this.wildcard) {
            final int cnameLength = this.cname.length();
            return thatHost.regionMatches(true,
                                          (thatHost.length() - cnameLength),
                                          this.cname, 0, cnameLength);
        } else {
            return thisHost.equalsIgnoreCase(thatHost);
        }
    }

    /**
     * 检查两个 SocketPermission 对象是否相等。
     * <P>
     * @param obj 要测试是否与该对象相等的对象。
     *
     * @return 如果 <i>obj</i> 是一个 SocketPermission，并且具有与该
     *  SocketPermission 对象相同的主机名、端口范围和操作，则返回 true。但是，如果 <i>obj</i> 仅包含 'resolve' 操作，则在比较中将忽略端口范围。
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof SocketPermission))
            return false;

        SocketPermission that = (SocketPermission) obj;

        // 这个（过于？）复杂！

        // 首先检查掩码
        if (this.mask != that.mask) return false;

        if ((that.mask & RESOLVE) != that.mask) {
            // 现在检查端口范围...
            if ((this.portrange[0] != that.portrange[0]) ||
                (this.portrange[1] != that.portrange[1])) {
                return false;
            }
        }

        // 快捷方式。这可以捕获：
        //  "crypto" 等于 "crypto"，或者
        // "1.2.3.4" 等于 "1.2.3.4."，或者
        //  "*.edu" 等于 "*.edu"，但
        //  它不能捕获 "crypto" 等于
        // "crypto.eng.sun.com"。

        if (this.getName().equalsIgnoreCase(that.getName())) {
            return true;
        }

        // 现在尝试获取规范（FQDN）名称并
        // 比较它。如果这失败了，我们能做的几乎就是返回
        // false。

        try {
            this.getCanonName();
            that.getCanonName();
        } catch (UnknownHostException uhe) {
            return false;
        }

        if (this.invalid || that.invalid)
            return false;

        if (this.cname != null) {
            return this.cname.equalsIgnoreCase(that.cname);
        }

        return false;
    }

    /**
     * 返回该对象的哈希码值。
     *
     * @return 该对象的哈希码值。
     */

    public int hashCode() {
        /*
         * 如果这个 SocketPermission 是用 IP 地址
         * 或通配符初始化的，使用 getName().hashCode()，否则使用
         * 从 java.net.InetAddress.getHostName 方法返回的主机名的
         * hashCode()。
         */

        if (init_with_ip || wildcard) {
            return this.getName().hashCode();
        }

        try {
            getCanonName();
        } catch (UnknownHostException uhe) {

        }

        if (invalid || cname == null)
            return this.getName().hashCode();
        else
            return this.cname.hashCode();
    }

    /**
     * 返回当前的操作掩码。
     *
     * @return 操作掩码。
     */

    int getMask() {
        return mask;
    }

    /**
     * 返回指定掩码中操作的“规范字符串表示”。
     * 始终按以下顺序返回存在的操作：
     * connect, listen, accept, resolve。
     *
     * @param mask 要转换为字符串的特定整数操作掩码
     * @return 操作的规范字符串表示
     */
    private static String getActions(int mask)
    {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;

        if ((mask & CONNECT) == CONNECT) {
            comma = true;
            sb.append("connect");
        }

        if ((mask & LISTEN) == LISTEN) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("listen");
        }

        if ((mask & ACCEPT) == ACCEPT) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("accept");
        }


        if ((mask & RESOLVE) == RESOLVE) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("resolve");
        }


                    return sb.toString();
    }

    /**
     * 返回操作的规范字符串表示形式。
     * 始终按以下顺序返回存在的操作：
     * connect, listen, accept, resolve。
     *
     * @return 操作的规范字符串表示形式。
     */
    public String getActions()
    {
        if (actions == null)
            actions = getActions(this.mask);

        return actions;
    }

    /**
     * 返回一个用于存储 SocketPermission 对象的新 PermissionCollection 对象。
     * <p>
     * SocketPermission 对象必须以允许它们按任何顺序插入集合的方式存储，但同时也必须能够以高效（且一致）的方式实现 PermissionCollection 的 {@code implies}
     * 方法。
     *
     * @return 一个适合存储 SocketPermissions 的新 PermissionCollection 对象。
     */

    public PermissionCollection newPermissionCollection() {
        return new SocketPermissionCollection();
    }

    /**
     * WriteObject 被调用以将 SocketPermission 的状态保存到流中。操作被序列化，超类负责名称。
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        // 写出操作。超类负责名称
        // 调用 getActions 以确保 actions 字段已初始化
        if (actions == null)
            getActions();
        s.defaultWriteObject();
    }

    /**
     * readObject 被调用以从流中恢复 SocketPermission 的状态。
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        // 读取操作，然后初始化其余部分
        s.defaultReadObject();
        init(getName(),getMask(actions));
    }

    /**
     * 检查系统/安全属性以获取此系统的临时端口范围。后缀为 "high" 或 "low"
     */
    private static int initEphemeralPorts(String suffix, int defval) {
        return AccessController.doPrivileged(
            new PrivilegedAction<Integer>(){
                public Integer run() {
                    int val = Integer.getInteger(
                            "jdk.net.ephemeralPortRange."+suffix, -1
                    );
                    if (val != -1) {
                        return val;
                    } else {
                        return suffix.equals("low") ?
                            PortConfig.getLower() : PortConfig.getUpper();
                    }
                }
            }
        );
    }

    /**
     * 检查目标范围是否在策略范围和此平台的临时范围（如果策略包括临时范围）内
     */
    private static boolean inRange(
        int policyLow, int policyHigh, int targetLow, int targetHigh
    )
    {
        final int ephemeralLow = EphemeralRange.low;
        final int ephemeralHigh = EphemeralRange.high;

        if (targetLow == 0) {
            // 检查策略是否包括临时范围
            if (!inRange(policyLow, policyHigh, ephemeralLow, ephemeralHigh)) {
                return false;
            }
            if (targetHigh == 0) {
                // 没有其他要做的
                return true;
            }
            // 继续检查第一个实际端口号
            targetLow = 1;
        }

        if (policyLow == 0 && policyHigh == 0) {
            // 仅临时范围
            return targetLow >= ephemeralLow && targetHigh <= ephemeralHigh;
        }

        if (policyLow != 0) {
            // 仅检查策略
            return targetLow >= policyLow && targetHigh <= policyHigh;
        }

        // policyLow == 0 表示可能需要检查两个范围

        // 首先检查策略和临时范围是否重叠/连续

        if (policyHigh >= ephemeralLow - 1) {
            return targetHigh <= ephemeralHigh;
        }

        // 策略和临时范围不重叠

        // 目标范围必须完全位于策略范围或临时范围内

        return  (targetLow <= policyHigh && targetHigh <= policyHigh) ||
                (targetLow >= ephemeralLow && targetHigh <= ephemeralHigh);
    }
    /*
    public String toString()
    {
        StringBuffer s = new StringBuffer(super.toString() + "\n" +
            "cname = " + cname + "\n" +
            "wildcard = " + wildcard + "\n" +
            "invalid = " + invalid + "\n" +
            "portrange = " + portrange[0] + "," + portrange[1] + "\n");
        if (addresses != null) for (int i=0; i<addresses.length; i++) {
            s.append( addresses[i].getHostAddress());
            s.append("\n");
        } else {
            s.append("(no addresses)\n");
        }

        return s.toString();
    }

    public static void main(String args[]) throws Exception {
        SocketPermission this_ = new SocketPermission(args[0], "connect");
        SocketPermission that_ = new SocketPermission(args[1], "connect");
        System.out.println("-----\n");
        System.out.println("this.implies(that) = " + this_.implies(that_));
        System.out.println("-----\n");
        System.out.println("this = "+this_);
        System.out.println("-----\n");
        System.out.println("that = "+that_);
        System.out.println("-----\n");

        SocketPermissionCollection nps = new SocketPermissionCollection();
        nps.add(this_);
        nps.add(new SocketPermission("www-leland.stanford.edu","connect"));
        nps.add(new SocketPermission("www-sun.com","connect"));
        System.out.println("nps.implies(that) = " + nps.implies(that_));
        System.out.println("-----\n");
    }
    */
}

/**

如果使用 IP 初始化，键是 IP 的字符串形式
如果是通配符，则是通配符
否则是 cname？

 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */


final class SocketPermissionCollection extends PermissionCollection
    implements Serializable
{
    // 不序列化；请参阅类末尾的序列化部分
    private transient List<SocketPermission> perms;

    /**
     * 创建一个空的 SocketPermissions 对象。
     *
     */

    public SocketPermissionCollection() {
        perms = new ArrayList<SocketPermission>();
    }

    /**
     * 向 SocketPermissions 添加一个权限。哈希的键是通配符情况下的名称，或所有 IP 地址。
     *
     * @param permission 要添加的权限对象。
     *
     * @exception IllegalArgumentException - 如果权限不是 SocketPermission
     *
     * @exception SecurityException - 如果此 SocketPermissionCollection 对象已被标记为只读
     */
    public void add(Permission permission) {
        if (! (permission instanceof SocketPermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException(
                "尝试向只读 PermissionCollection 添加权限");

        // 优化以确保最可能被测试的权限出现在前面（4301064）
        synchronized (this) {
            perms.add(0, (SocketPermission)permission);
        }
    }

    /**
     * 检查并查看此权限集合是否隐含 "permission" 中表达的权限。
     *
     * @param permission 要比较的权限对象
     *
     * @return 如果 "permission" 是权限集合中的一个权限的适当子集，则返回 true，否则返回 false。
     */

    public boolean implies(Permission permission)
    {
        if (! (permission instanceof SocketPermission))
                return false;

        SocketPermission np = (SocketPermission) permission;

        int desired = np.getMask();
        int effective = 0;
        int needed = desired;

        synchronized (this) {
            int len = perms.size();
            //System.out.println("implies "+np);
            for (int i = 0; i < len; i++) {
                SocketPermission x = perms.get(i);
                //System.out.println("  trying "+x);
                if (((needed & x.getMask()) != 0) && x.impliesIgnoreMask(np)) {
                    effective |=  x.getMask();
                    if ((effective & desired) == desired)
                        return true;
                    needed = (desired ^ effective);
                }
            }
        }
        return false;
    }

    /**
     * 返回容器中所有 SocketPermission 对象的枚举。
     *
     * @return 所有 SocketPermission 对象的枚举。
     */

    @SuppressWarnings("unchecked")
    public Enumeration<Permission> elements() {
        // 将 Iterator 转换为 Enumeration
        synchronized (this) {
            return Collections.enumeration((List<Permission>)(List)perms);
        }
    }

    private static final long serialVersionUID = 2787186408602843674L;

    // 需要保持与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：

    //
    // 此集的 SocketPermissions。
    // @serial
    //
    // private Vector permissions;

    /**
     * @serialField permissions java.util.Vector
     *     此集的 SocketPermissions 列表。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Vector.class),
    };

    /**
     * @serialData "permissions" 字段（包含 SocketPermissions 的 Vector）。
     */
    /*
     * 将 perms 字段的内容作为 Vector 写出，以与早期版本的序列化兼容。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 写出 Vector
        Vector<SocketPermission> permissions = new Vector<>(perms.size());

        synchronized (this) {
            permissions.addAll(perms);
        }

        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    /*
     * 读取 SocketPermissions 的 Vector 并将其保存在 perms 字段中。
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // 不调用 in.defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取我们需要的字段
        @SuppressWarnings("unchecked")
        Vector<SocketPermission> permissions = (Vector<SocketPermission>)gfields.get("permissions", null);
        perms = new ArrayList<SocketPermission>(permissions.size());
        perms.addAll(permissions);
    }
}
