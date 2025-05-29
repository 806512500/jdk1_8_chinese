
/*
 * 版权所有 (c) 2000, 2020，Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Enumeration;
import java.util.Arrays;

/**
 * 本类表示互联网协议版本 6 (IPv6) 地址。
 * 由 <a href="http://www.ietf.org/rfc/rfc2373.txt">
 * <i>RFC&nbsp;2373: IP 版本 6 地址体系结构</i></a> 定义。
 *
 * <h3> <A NAME="format">IP 地址的文本表示</a> </h3>
 *
 * 用作方法输入的 IPv6 地址的文本表示形式如下：
 *
 * <ol>
 *   <li><p> <A NAME="lform">首选形式</a> 是 x:x:x:x:x:x:x:x，
 *   其中 'x's 是地址的八个 16 位部分的十六进制值。这是完整形式。例如，
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code 1080:0:0:0:8:800:200C:417A}<td></tr>
 *   </table></blockquote>
 *
 *   <p> 注意，不必在每个字段中写前导零。但是，每个字段中必须至少有一个数字，除非以下情况另有说明。</li>
 *
 *   <li><p> 由于分配某些类型的 IPv6 地址的方法，地址中包含长串零位的情况将很常见。为了使写入包含零位的地址更容易，提供了一种特殊的语法来压缩零。使用 "::" 表示多个 16 位零组。"::" 在地址中只能出现一次。"::" 也可以用于压缩地址的前导和/或尾随零。例如，
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code 1080::8:800:200C:417A}<td></tr>
 *   </table></blockquote>
 *
 *   <li><p> 在处理 IPv4 和 IPv6 节点的混合环境时，有时使用另一种形式更方便，即 x:x:x:x:x:x:d.d.d.d，其中 'x's 是地址的六个高位 16 位部分的十六进制值，'d's 是标准 IPv4 表示地址的四个低位 8 位部分的十进制值，例如，
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code ::FFFF:129.144.52.38}<td></tr>
 *   <tr><td>{@code ::129.144.52.38}<td></tr>
 *   </table></blockquote>
 *
 *   <p> 其中 "::FFFF:d.d.d.d" 和 "::d.d.d.d" 分别是 IPv4 映射的 IPv6 地址和 IPv4 兼容的 IPv6 地址的一般形式。注意，IPv4 部分必须是 "d.d.d.d" 形式。以下形式无效：
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code ::FFFF:d.d.d}<td></tr>
 *   <tr><td>{@code ::FFFF:d.d}<td></tr>
 *   <tr><td>{@code ::d.d.d}<td></tr>
 *   <tr><td>{@code ::d.d}<td></tr>
 *   </table></blockquote>
 *
 *   <p> 以下形式：
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code ::FFFF:d}<td></tr>
 *   </table></blockquote>
 *
 *   <p> 是有效的，但它是 IPv4 兼容的 IPv6 地址的非常规表示形式，
 *
 *   <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 *   <tr><td>{@code ::255.255.0.d}<td></tr>
 *   </table></blockquote>
 *
 *   <p> 而 "::d" 对应于一般 IPv6 地址
 *   "0:0:0:0:0:0:0:d"。</li>
 * </ol>
 *
 * <p> 对于返回文本表示作为输出值的方法，使用完整形式。Inet6Address 将返回完整形式，因为它在与其他文本数据结合使用时是明确的。
 *
 * <h4> 特殊 IPv6 地址 </h4>
 *
 * <blockquote>
 * <table cellspacing=2 summary="IPv4 映射地址的描述">
 * <tr><th valign=top><i>IPv4 映射地址</i></th>
 *         <td>形式为 ::ffff:w.x.y.z 的 IPv6 地址用于表示 IPv4 地址。它允许本机程序在与 IPv4 和 IPv6 节点通信时使用相同地址数据结构和相同的套接字。
 *
 *         <p>在 InetAddress 和 Inet6Address 中，它用于内部表示；它没有功能作用。Java 永远不会返回 IPv4 映射地址。这些类可以接受 IPv4 映射地址作为输入，无论是字节数组形式还是文本表示形式。但是，它将被转换为 IPv4 地址。</td></tr>
 * </table></blockquote>
 *
 * <h4><A NAME="scoped">IPv6 范围地址的文本表示</a></h4>
 *
 * <p> 上述 IPv6 地址的文本表示可以扩展以指定 IPv6 范围地址。这一基本地址体系结构的扩展在 [draft-ietf-ipngwg-scoping-arch-04.txt] 中描述。
 *
 * <p> 由于链路本地地址和站点本地地址是非全局的，不同的主机可能具有相同的目标地址，并且可以通过同一发起系统的不同接口到达。在这种情况下，发起系统被认为连接到同一范围的多个区域。为了消除歧义，可以在 IPv6 地址后附加一个区域标识符（或 <i>scope_id</i>）。
 *
 * <p> 指定 <i>scope_id</i> 的一般格式如下：
 *
 * <blockquote><i>IPv6-address</i>%<i>scope_id</i></blockquote>
 * <p> IPv6-address 是上述描述的字面 IPv6 地址。
 * <i>scope_id</i> 指向本地系统上的接口，可以以两种方式指定。
 * <ol><li><i>作为数字标识符。</i> 必须是一个正整数，标识系统理解的特定接口和范围。通常，可以通过系统上的管理工具确定数字值。每个接口可能有多个值，每个范围一个。如果未指定范围，则使用零作为默认值。</li>
 * <li><i>作为字符串。</i> 必须是 {@link java.net.NetworkInterface#getName()} 为特定接口返回的确切字符串。当以这种方式创建 Inet6Address 时，通过查询相关 NetworkInterface 在创建对象时确定数字范围 ID。</li></ol>
 *
 * <p> 还要注意，可以从 NetworkInterface 类返回的 Inet6Address 实例中检索数字 <i>scope_id</i>。这可以用来找出系统上当前配置的范围 ID。
 * @since 1.4
 */


public final
class Inet6Address extends InetAddress {
    final static int INADDRSZ = 16;

    /*
     * 缓存的 scope_id - 仅用于链路本地地址。
     */
    private transient int cached_scope_id;  // 0

    private class Inet6AddressHolder {

        private Inet6AddressHolder() {
            ipaddress = new byte[INADDRSZ];
        }

        private Inet6AddressHolder(
            byte[] ipaddress, int scope_id, boolean scope_id_set,
            NetworkInterface ifname, boolean scope_ifname_set)
        {
            this.ipaddress = ipaddress;
            this.scope_id = scope_id;
            this.scope_id_set = scope_id_set;
            this.scope_ifname_set = scope_ifname_set;
            this.scope_ifname = ifname;
        }

        /**
         * 保存一个 128 位（16 字节）的 IPv6 地址。
         */
        byte[] ipaddress;

        /**
         * scope_id。对象创建时指定的范围。如果对象是使用接口名称创建的，则在需要时才确定 scope_id。
         */
        int scope_id;  // 0

        /**
         * 当 scope_id 字段包含有效的整数 scope_id 时，此字段将被设置为 true。
         */
        boolean scope_id_set;  // false

        /**
         * 范围接口。scope_id 从此接口的第一个地址的范围派生，该地址的范围与此地址相同。
         */
        NetworkInterface scope_ifname;  // null

        /**
         * 如果对象是使用范围接口而不是数值范围 ID 构建的，则此字段将被设置。
         */
        boolean scope_ifname_set; // false;

        void setAddr(byte addr[]) {
            if (addr.length == INADDRSZ) { // 正常的 IPv6 地址
                System.arraycopy(addr, 0, ipaddress, 0, INADDRSZ);
            }
        }

        void init(byte addr[], int scope_id) {
            setAddr(addr);

            if (scope_id >= 0) {
                this.scope_id = scope_id;
                this.scope_id_set = true;
            }
        }

        void init(byte addr[], NetworkInterface nif)
            throws UnknownHostException
        {
            setAddr(addr);

            if (nif != null) {
                this.scope_id = deriveNumericScope(ipaddress, nif);
                this.scope_id_set = true;
                this.scope_ifname = nif;
                this.scope_ifname_set = true;
            }
        }

        String getHostAddress() {
            String s = numericToTextFormat(ipaddress);
            if (scope_ifname != null) { /* 必须先检查这个 */
                s = s + "%" + scope_ifname.getName();
            } else if (scope_id_set) {
                s = s + "%" + scope_id;
            }
            return s;
        }

        public boolean equals(Object o) {
            if (! (o instanceof Inet6AddressHolder)) {
                return false;
            }
            Inet6AddressHolder that = (Inet6AddressHolder)o;

            return Arrays.equals(this.ipaddress, that.ipaddress);
        }

        public int hashCode() {
            if (ipaddress != null) {

                int hash = 0;
                int i=0;
                while (i<INADDRSZ) {
                    int j=0;
                    int component=0;
                    while (j<4 && i<INADDRSZ) {
                        component = (component << 8) + ipaddress[i];
                        j++;
                        i++;
                    }
                    hash += component;
                }
                return hash;

            } else {
                return 0;
            }
        }

        boolean isIPv4CompatibleAddress() {
            if ((ipaddress[0] == 0x00) && (ipaddress[1] == 0x00) &&
                (ipaddress[2] == 0x00) && (ipaddress[3] == 0x00) &&
                (ipaddress[4] == 0x00) && (ipaddress[5] == 0x00) &&
                (ipaddress[6] == 0x00) && (ipaddress[7] == 0x00) &&
                (ipaddress[8] == 0x00) && (ipaddress[9] == 0x00) &&
                (ipaddress[10] == 0x00) && (ipaddress[11] == 0x00))  {
                return true;
            }
            return false;
        }

        boolean isMulticastAddress() {
            return ((ipaddress[0] & 0xff) == 0xff);
        }

        boolean isAnyLocalAddress() {
            byte test = 0x00;
            for (int i = 0; i < INADDRSZ; i++) {
                test |= ipaddress[i];
            }
            return (test == 0x00);
        }

        boolean isLoopbackAddress() {
            byte test = 0x00;
            for (int i = 0; i < 15; i++) {
                test |= ipaddress[i];
            }
            return (test == 0x00) && (ipaddress[15] == 0x01);
        }

        boolean isLinkLocalAddress() {
            return ((ipaddress[0] & 0xff) == 0xfe
                    && (ipaddress[1] & 0xc0) == 0x80);
        }


        boolean isSiteLocalAddress() {
            return ((ipaddress[0] & 0xff) == 0xfe
                    && (ipaddress[1] & 0xc0) == 0xc0);
        }

        boolean isMCGlobal() {
            return ((ipaddress[0] & 0xff) == 0xff
                    && (ipaddress[1] & 0x0f) == 0x0e);
        }

        boolean isMCNodeLocal() {
            return ((ipaddress[0] & 0xff) == 0xff
                    && (ipaddress[1] & 0x0f) == 0x01);
        }

        boolean isMCLinkLocal() {
            return ((ipaddress[0] & 0xff) == 0xff
                    && (ipaddress[1] & 0x0f) == 0x02);
        }

        boolean isMCSiteLocal() {
            return ((ipaddress[0] & 0xff) == 0xff
                    && (ipaddress[1] & 0x0f) == 0x05);
        }

        boolean isMCOrgLocal() {
            return ((ipaddress[0] & 0xff) == 0xff
                    && (ipaddress[1] & 0x0f) == 0x08);
        }
    }

    private final transient Inet6AddressHolder holder6;

    private static final long serialVersionUID = 6880410070516793377L;

    // 执行本机初始化
    static { init(); }

    Inet6Address() {
        super();
        holder.init(null, IPv6);
        holder6 = new Inet6AddressHolder();
    }


                /* 检查 scope_id 的值应由调用者完成
     * scope_id 必须 >= 0，或为 -1 表示未设置
     */
    Inet6Address(String hostName, byte addr[], int scope_id) {
        holder.init(hostName, IPv6);
        holder6 = new Inet6AddressHolder();
        holder6.init(addr, scope_id);
    }

    Inet6Address(String hostName, byte addr[]) {
        holder6 = new Inet6AddressHolder();
        try {
            initif (hostName, addr, null);
        } catch (UnknownHostException e) {} /* 如果 ifname 为 null，则不会发生 */
    }

    Inet6Address (String hostName, byte addr[], NetworkInterface nif)
        throws UnknownHostException
    {
        holder6 = new Inet6AddressHolder();
        initif (hostName, addr, nif);
    }

    Inet6Address (String hostName, byte addr[], String ifname)
        throws UnknownHostException
    {
        holder6 = new Inet6AddressHolder();
        initstr (hostName, addr, ifname);
    }

    /**
     * 以与 {@link
     * InetAddress#getByAddress(String,byte[])} 完全相同的方式创建一个 Inet6Address，但 IPv6 scope_id
     * 设置为与给定接口和地址类型对应的值。如果给定接口没有为给定地址类型（如链路本地或站点本地）分配数字 scope_id，
     * 则调用将因 UnknownHostException 而失败。
     * 有关 IPv6 范围地址的描述，请参阅 <a href="Inet6Address.html#scoped">这里</a>。
     *
     * @param host 指定的主机
     * @param addr 网络字节顺序的原始 IP 地址
     * @param nif 此地址必须关联的接口。
     * @return 从原始 IP 地址创建的 Inet6Address 对象。
     * @throws  UnknownHostException
     *          如果 IP 地址长度非法，或者接口没有为给定地址类型分配数字 scope_id。
     *
     * @since 1.5
     */
    public static Inet6Address getByAddress(String host, byte[] addr,
                                            NetworkInterface nif)
        throws UnknownHostException
    {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[') {
            if (host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, nif);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    /**
     * 以与 {@link
     * InetAddress#getByAddress(String,byte[])} 完全相同的方式创建一个 Inet6Address，但 IPv6 scope_id
     * 设置为给定的数值。不检查 scope_id 是否对应于系统上的任何接口。
     * 有关 IPv6 范围地址的描述，请参阅 <a href="Inet6Address.html#scoped">这里</a>。
     *
     * @param host 指定的主机
     * @param addr 网络字节顺序的原始 IP 地址
     * @param scope_id 地址的数字 scope_id。
     * @return 从原始 IP 地址创建的 Inet6Address 对象。
     * @throws  UnknownHostException  如果 IP 地址长度非法。
     *
     * @since 1.5
     */
    public static Inet6Address getByAddress(String host, byte[] addr,
                                            int scope_id)
        throws UnknownHostException
    {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[') {
            if (host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, scope_id);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    private void initstr(String hostName, byte addr[], String ifname)
        throws UnknownHostException
    {
        try {
            NetworkInterface nif = NetworkInterface.getByName (ifname);
            if (nif == null) {
                throw new UnknownHostException ("no such interface " + ifname);
            }
            initif (hostName, addr, nif);
        } catch (SocketException e) {
            throw new UnknownHostException ("SocketException thrown" + ifname);
        }
    }

    private void initif(String hostName, byte addr[], NetworkInterface nif)
        throws UnknownHostException
    {
        int family = -1;
        holder6.init(addr, nif);

        if (addr.length == INADDRSZ) { // 正常的 IPv6 地址
            family = IPv6;
        }
        holder.init(hostName, family);
    }

    /* 检查两个 IPv6 地址，如果它们都是非全局地址类型，但类型不同（即一个是站点本地，另一个是链路本地），则返回 false。
     * 否则返回 true。
     */

    private static boolean isDifferentLocalAddressType(
        byte[] thisAddr, byte[] otherAddr) {

        if (Inet6Address.isLinkLocalAddress(thisAddr) &&
                !Inet6Address.isLinkLocalAddress(otherAddr)) {
            return false;
        }
        if (Inet6Address.isSiteLocalAddress(thisAddr) &&
                !Inet6Address.isSiteLocalAddress(otherAddr)) {
            return false;
        }
        return true;
    }

    private static int deriveNumericScope (byte[] thisAddr, NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (!(addr instanceof Inet6Address)) {
                continue;
            }
            Inet6Address ia6_addr = (Inet6Address)addr;
            /* 检查站点或链路本地前缀是否匹配 */
            if (!isDifferentLocalAddressType(thisAddr, ia6_addr.getAddress())){
                /* 类型不同，继续搜索 */
                continue;
            }
            /* 找到匹配的地址 - 返回其 scope_id */
            return ia6_addr.getScopeId();
        }
        throw new UnknownHostException ("no scope_id found");
    }


                private int deriveNumericScope (String ifname) throws UnknownHostException {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new UnknownHostException ("无法枚举本地网络接口");
        }
        while (en.hasMoreElements()) {
            NetworkInterface ifc = en.nextElement();
            if (ifc.getName().equals (ifname)) {
                return deriveNumericScope(holder6.ipaddress, ifc);
            }
        }
        throw new UnknownHostException ("未找到与接口匹配的地址 : " +ifname);
    }

    /**
     * @serialField ipaddress byte[]
     * @serialField scope_id int
     * @serialField scope_id_set boolean
     * @serialField scope_ifname_set boolean
     * @serialField ifname String
     */

    private static final ObjectStreamField[] serialPersistentFields = {
         new ObjectStreamField("ipaddress", byte[].class),
         new ObjectStreamField("scope_id", int.class),
         new ObjectStreamField("scope_id_set", boolean.class),
         new ObjectStreamField("scope_ifname_set", boolean.class),
         new ObjectStreamField("ifname", String.class)
    };

    private static final long FIELDS_OFFSET;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(
                    Inet6Address.class.getDeclaredField("holder6"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    /**
     * 从流中恢复此对象的状态
     * 包括作用域信息，仅当
     * 作用域接口名称在此系统上有效时
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        NetworkInterface scope_ifname = null;

        if (getClass().getClassLoader() != null) {
            throw new SecurityException ("无效的地址类型");
        }

        ObjectInputStream.GetField gf = s.readFields();
        byte[] ipaddress = (byte[])gf.get("ipaddress", null);
        int scope_id = (int)gf.get("scope_id", -1);
        boolean scope_id_set = (boolean)gf.get("scope_id_set", false);
        boolean scope_ifname_set = (boolean)gf.get("scope_ifname_set", false);
        String ifname = (String)gf.get("ifname", null);

        if (ifname != null && !ifname.isEmpty()) {
            try {
                scope_ifname = NetworkInterface.getByName(ifname);
                if (scope_ifname == null) {
                    /* 该接口在此系统上不存在，因此我们完全清除
                     * 作用域信息 */
                    scope_id_set = false;
                    scope_ifname_set = false;
                    scope_id = 0;
                } else {
                    scope_ifname_set = true;
                    try {
                        scope_id = deriveNumericScope (ipaddress, scope_ifname);
                    } catch (UnknownHostException e) {
                        // 通常不应该发生，但可能是
                        // 用于反序列化的机器具有相同的接口名称但未配置IPv6。
                    }
                }
            } catch (SocketException e) {}
        }

        /* 如果未提供ifname，则使用数字信息 */

        ipaddress = ipaddress.clone();

        // 检查我们的不变量是否满足
        if (ipaddress.length != INADDRSZ) {
            throw new InvalidObjectException("无效的地址长度: "+
                                             ipaddress.length);
        }

        if (holder.getFamily() != IPv6) {
            throw new InvalidObjectException("无效的地址族类型");
        }

        Inet6AddressHolder h = new Inet6AddressHolder(
            ipaddress, scope_id, scope_id_set, scope_ifname, scope_ifname_set
        );

        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    /**
     * 默认行为被覆盖，以便将
     * scope_ifname 字段作为字符串写入，而不是不可序列化的 NetworkInterface
     */
    private synchronized void writeObject(ObjectOutputStream s)
        throws IOException
    {
            String ifname = null;

        if (holder6.scope_ifname != null) {
            ifname = holder6.scope_ifname.getName();
            holder6.scope_ifname_set = true;
        }
        ObjectOutputStream.PutField pfields = s.putFields();
        pfields.put("ipaddress", holder6.ipaddress);
        pfields.put("scope_id", holder6.scope_id);
        pfields.put("scope_id_set", holder6.scope_id_set);
        pfields.put("scope_ifname_set", holder6.scope_ifname_set);
        pfields.put("ifname", ifname);
        s.writeFields();
    }

    /**
     * 检查 InetAddress 是否为 IP 组播地址的实用程序例程。地址开头的 11111111 标识该地址为组播地址。
     *
     * @return 一个 {@code boolean}，表示 InetAddress 是否为 IP 组播地址
     *
     * @since JDK1.1
     */
    @Override
    public boolean isMulticastAddress() {
        return holder6.isMulticastAddress();
    }

    /**
     * 检查 InetAddress 是否为通配符地址的实用程序例程。
     *
     * @return 一个 {@code boolean}，表示 Inetaddress 是否为通配符地址。
     *
     * @since 1.4
     */
    @Override
    public boolean isAnyLocalAddress() {
        return holder6.isAnyLocalAddress();
    }

    /**
     * 检查 InetAddress 是否为回环地址的实用程序例程。
     *
     * @return 一个 {@code boolean}，表示 InetAddress 是否为回环地址；否则为 false。
     *
     * @since 1.4
     */
    @Override
    public boolean isLoopbackAddress() {
        return holder6.isLoopbackAddress();
    }

                /**
     * 实用程序例程，用于检查InetAddress是否为链路本地地址。
     *
     * @return 一个 {@code boolean}，表示InetAddress是否为链路本地地址；
     *         如果地址不是链路本地单播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isLinkLocalAddress() {
        return holder6.isLinkLocalAddress();
    }

    /* 上面的静态版本 */
    static boolean isLinkLocalAddress(byte[] ipaddress) {
        return ((ipaddress[0] & 0xff) == 0xfe
                && (ipaddress[1] & 0xc0) == 0x80);
    }

    /**
     * 实用程序例程，用于检查InetAddress是否为站点本地地址。
     *
     * @return 一个 {@code boolean}，表示InetAddress是否为站点本地地址；
     *         如果地址不是站点本地单播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isSiteLocalAddress() {
        return holder6.isSiteLocalAddress();
    }

    /* 上面的静态版本 */
    static boolean isSiteLocalAddress(byte[] ipaddress) {
        return ((ipaddress[0] & 0xff) == 0xfe
                && (ipaddress[1] & 0xc0) == 0xc0);
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有全局范围。
     *
     * @return 一个 {@code boolean}，表示地址是否为具有全局范围的多播地址，
     *         如果它不是全局范围或多播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isMCGlobal() {
        return holder6.isMCGlobal();
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有节点范围。
     *
     * @return 一个 {@code boolean}，表示地址是否为具有节点本地范围的多播地址，
     *         如果它不是节点本地范围或多播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isMCNodeLocal() {
        return holder6.isMCNodeLocal();
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有链路范围。
     *
     * @return 一个 {@code boolean}，表示地址是否为具有链路本地范围的多播地址，
     *         如果它不是链路本地范围或多播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isMCLinkLocal() {
        return holder6.isMCLinkLocal();
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有站点范围。
     *
     * @return 一个 {@code boolean}，表示地址是否为具有站点本地范围的多播地址，
     *         如果它不是站点本地范围或多播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isMCSiteLocal() {
        return holder6.isMCSiteLocal();
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有组织范围。
     *
     * @return 一个 {@code boolean}，表示地址是否为具有组织本地范围的多播地址，
     *         如果它不是组织本地范围或多播地址，则返回false。
     *
     * @since 1.4
     */
    @Override
    public boolean isMCOrgLocal() {
        return holder6.isMCOrgLocal();
    }
    /**
     * 返回此 {@code InetAddress} 对象的原始IP地址。结果为网络字节顺序：地址的最高有效字节位于
     * {@code getAddress()[0]}。
     *
     * @return 此对象的原始IP地址。
     */
    @Override
    public byte[] getAddress() {
        return holder6.ipaddress.clone();
    }

    /**
     * 如果此实例与接口相关联，则返回数字范围ID。如果没有设置范围ID，则返回值为零。
     *
     * @return 范围ID，或未设置时返回零。
     *
     * @since 1.5
     */
     public int getScopeId() {
        return holder6.scope_id;
     }

    /**
     * 如果此实例是在指定范围接口的情况下创建的，则返回范围接口。
     *
     * @return 范围接口，或未设置时返回null。
     * @since 1.5
     */
     public NetworkInterface getScopedInterface() {
        return holder6.scope_ifname;
     }

    /**
     * 返回IP地址的文本表示字符串。如果实例是在指定范围标识符的情况下创建的，则范围ID将附加到IP地址前，
     * 以"%"（百分号）字符为前缀。这可以是数字值或字符串，具体取决于创建实例时使用的值。
     *
     * @return 原始IP地址的字符串格式。
     */
    @Override
    public String getHostAddress() {
        return holder6.getHostAddress();
    }

    /**
     * 返回此IP地址的哈希码。
     *
     * @return 此IP地址的哈希码值。
     */
    @Override
    public int hashCode() {
        return holder6.hashCode();
    }

    /**
     * 将此对象与指定对象进行比较。结果为 {@code true} 当且仅当参数不为 {@code null} 并且它表示与
     * 此对象相同的IP地址。
     *
     * <p> 两个 {@code InetAddress} 实例表示相同的IP地址，如果它们的 {@code getAddress} 返回的字节数组
     * 长度相同，并且字节数组的每个组件都相同。
     *
     * @param   obj   要比较的对象。
     *
     * @return  如果对象相同，则返回 {@code true}；否则返回 {@code false}。
     *
     * @see     java.net.InetAddress#getAddress()
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Inet6Address))
            return false;

        Inet6Address inetAddr = (Inet6Address)obj;

        return holder6.equals(inetAddr.holder6);
    }

    /**
     * 实用程序例程，用于检查InetAddress是否为IPv4兼容的IPv6地址。
     *
     * @return 一个 {@code boolean}，表示InetAddress是否为IPv4兼容的IPv6地址；
     *         如果地址是IPv4地址，则返回false。
     *
     * @since 1.4
     */
    public boolean isIPv4CompatibleAddress() {
        return holder6.isIPv4CompatibleAddress();
    }

                // 工具类
    private final static int INT16SZ = 2;

    /*
     * 将IPv6二进制地址转换为表示（可打印）格式。
     *
     * @param src 表示IPv6数值地址的字节数组
     * @return 一个字符串，表示IPv6地址的
     *         文本表示格式
     * @since 1.4
     */
    static String numericToTextFormat(byte[] src) {
        StringBuilder sb = new StringBuilder(39);
        for (int i = 0; i < (INADDRSZ / INT16SZ); i++) {
            sb.append(Integer.toHexString(((src[i<<1]<<8) & 0xff00)
                                          | (src[(i<<1)+1] & 0xff)));
            if (i < (INADDRSZ / INT16SZ) -1 ) {
               sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * 执行类加载时的初始化。
     */
    private static native void init();
}
