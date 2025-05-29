/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.ObjectStreamException;

/**
 * 该类表示互联网协议版本 4 (IPv4) 地址。
 * 由 <a href="http://www.ietf.org/rfc/rfc790.txt">
 * <i>RFC&nbsp;790: 已分配数字</i></a>,
 * <a href="http://www.ietf.org/rfc/rfc1918.txt">
 * <i>RFC&nbsp;1918: 私有互联网的地址分配</i></a>,
 * 和 <a href="http://www.ietf.org/rfc/rfc2365.txt"><i>RFC&nbsp;2365:
 * 管理范围的 IP 组播</i></a> 定义。
 *
 * <h3> <A NAME="format">IP 地址的文本表示</a> </h3>
 *
 * 作为方法输入的 IPv4 地址的文本表示采用以下形式之一：
 *
 * <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 * <tr><td>{@code d.d.d.d}</td></tr>
 * <tr><td>{@code d.d.d}</td></tr>
 * <tr><td>{@code d.d}</td></tr>
 * <tr><td>{@code d}</td></tr>
 * </table></blockquote>
 *
 * <p> 当指定四个部分时，每个部分都被解释为一个字节的数据，并从左到右分配给 IPv4 地址的四个字节。
 *
 * <p> 当指定三个部分的地址时，最后一个部分被解释为一个 16 位的数量，并放置在网络地址的最右边两个字节中。这使得三部分地址格式便于指定 B 类网络地址，如 128.网.主机。
 *
 * <p> 当提供两个部分的地址时，最后一个部分被解释为一个 24 位的数量，并放置在网络地址的最右边三个字节中。这使得两部分地址格式便于指定 A 类网络地址，如 网.主机。
 *
 * <p> 当只给出一个部分时，该值直接存储在网络地址中，没有任何字节重新排列。
 *
 * <p> 对于返回文本表示作为输出值的方法，使用第一种形式，即点分四组字符串。
 *
 * <h4> 组播地址的范围 </h4>
 *
 * 传统上，IPv4 TTL 字段在 IP 头中同时作为组播范围字段：TTL 为 0 表示节点本地，1 表示链路本地，直到 32 表示站点本地，直到 64 表示区域本地，直到 128 表示大陆本地，直到 255 为全球。然而，更推荐使用管理范围。请参阅 <a href="http://www.ietf.org/rfc/rfc2365.txt">
 * <i>RFC&nbsp;2365: 管理范围的 IP 组播</i></a>
 * @since 1.4
 */

public final
class Inet4Address extends InetAddress {
    final static int INADDRSZ = 4;

    /** 使用 InetAddress 的 serialVersionUID，但在序列化之前总是用一个 InetAddress 实例替换 Inet4Address 实例 */
    private static final long serialVersionUID = 3286316764910316507L;

    /*
     * 执行初始化。
     */
    static {
        init();
    }

    Inet4Address() {
        super();
        holder().hostName = null;
        holder().address = 0;
        holder().family = IPv4;
    }

    Inet4Address(String hostName, byte addr[]) {
        holder().hostName = hostName;
        holder().family = IPv4;
        if (addr != null) {
            if (addr.length == INADDRSZ) {
                int address  = addr[3] & 0xFF;
                address |= ((addr[2] << 8) & 0xFF00);
                address |= ((addr[1] << 16) & 0xFF0000);
                address |= ((addr[0] << 24) & 0xFF000000);
                holder().address = address;
            }
        }
        holder().originalHostName = hostName;
    }
    Inet4Address(String hostName, int address) {
        holder().hostName = hostName;
        holder().family = IPv4;
        holder().address = address;
        holder().originalHostName = hostName;
    }

    /**
     * 用一个 InetAddress 对象替换要序列化的对象。
     *
     * @return 要序列化的替代对象。
     *
     * @throws ObjectStreamException 如果不能创建替换此对象的新对象
     */
    private Object writeReplace() throws ObjectStreamException {
        // 将要序列化的 'this' 对象替换
        InetAddress inet = new InetAddress();
        inet.holder().hostName = holder().getHostName();
        inet.holder().address = holder().getAddress();

        /**
         * 在 1.4 之前，InetAddress 是根据平台 AF_INET 值（通常是 2）创建的。
         * 为了兼容性，我们必须用这个家族的 InetAddress 写入。
         */
        inet.holder().family = 2;

        return inet;
    }

    /**
     * 检查 InetAddress 是否为 IP 组播地址的实用程序例程。IP 组播地址是 D 类
     * 地址，即地址的前四位是 1110。
     * @return 一个 {@code boolean} 表示 InetAddress 是否为
     * IP 组播地址
     * @since   JDK1.1
     */
    public boolean isMulticastAddress() {
        return ((holder().getAddress() & 0xf0000000) == 0xe0000000);
    }

    /**
     * 检查 InetAddress 是否为通配符地址的实用程序例程。
     * @return 一个 {@code boolean} 表示 Inetaddress 是否为
     * 通配符地址。
     * @since 1.4
     */
    public boolean isAnyLocalAddress() {
        return holder().getAddress() == 0;
    }

    /**
     * 检查 InetAddress 是否为回环地址的实用程序例程。
     *
     * @return 一个 {@code boolean} 表示 InetAddress 是否为
     * 回环地址；否则为 false。
     * @since 1.4
     */
    public boolean isLoopbackAddress() {
        /* 127.x.x.x */
        byte[] byteAddr = getAddress();
        return byteAddr[0] == 127;
    }

    /**
     * 检查 InetAddress 是否为链路本地地址的实用程序例程。
     *
     * @return 一个 {@code boolean} 表示 InetAddress 是否为
     * 链路本地地址；或如果不是链路本地单播地址，则为 false。
     * @since 1.4
     */
    public boolean isLinkLocalAddress() {
        // IPv4 链路本地单播 (169.254.0.0/16)
        // 由 Bill Manning 在 "记录已注册 IANA 的特殊用途 IPv4 地址块" 中定义
        // draft-manning-dsua-06.txt
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 169)
            && (((address >>> 16) & 0xFF) == 254);
    }


                /**
     * 实用程序例程，用于检查InetAddress是否为站点本地地址。
     *
     * @return 一个 {@code boolean} 值，指示InetAddress是否为
     * 站点本地地址；如果不是站点本地单播地址，则返回false。
     * @since 1.4
     */
    public boolean isSiteLocalAddress() {
        // 参考 RFC 1918
        // 10/8 前缀
        // 172.16/12 前缀
        // 192.168/16 前缀
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 10)
            || ((((address >>> 24) & 0xFF) == 172)
                && (((address >>> 16) & 0xF0) == 16))
            || ((((address >>> 24) & 0xFF) == 192)
                && (((address >>> 16) & 0xFF) == 168));
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有全局范围。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为
     *         具有全局范围的多播地址，如果不是全局范围或不是多播地址，则返回false
     * @since 1.4
     */
    public boolean isMCGlobal() {
        // 224.0.1.0 到 238.255.255.255
        byte[] byteAddr = getAddress();
        return ((byteAddr[0] & 0xff) >= 224 && (byteAddr[0] & 0xff) <= 238 ) &&
            !((byteAddr[0] & 0xff) == 224 && byteAddr[1] == 0 &&
              byteAddr[2] == 0);
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有节点范围。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为
     *         具有节点本地范围的多播地址，如果不是节点本地范围或不是多播地址，则返回false
     * @since 1.4
     */
    public boolean isMCNodeLocal() {
        // 除非 ttl == 0
        return false;
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有链接范围。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为
     *         具有链接本地范围的多播地址，如果不是链接本地范围或不是多播地址，则返回false
     * @since 1.4
     */
    public boolean isMCLinkLocal() {
        // 224.0.0/24 前缀且 ttl == 1
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 224)
            && (((address >>> 16) & 0xFF) == 0)
            && (((address >>> 8) & 0xFF) == 0);
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有站点范围。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为
     *         具有站点本地范围的多播地址，如果不是站点本地范围或不是多播地址，则返回false
     * @since 1.4
     */
    public boolean isMCSiteLocal() {
        // 239.255/16 前缀或 ttl < 32
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 239)
            && (((address >>> 16) & 0xFF) == 255);
    }

    /**
     * 实用程序例程，用于检查多播地址是否具有组织范围。
     *
     * @return 一个 {@code boolean} 值，指示地址是否为
     *         具有组织本地范围的多播地址，如果不是组织本地范围或不是多播地址，则返回false
     * @since 1.4
     */
    public boolean isMCOrgLocal() {
        // 239.192 - 239.195
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 239)
            && (((address >>> 16) & 0xFF) >= 192)
            && (((address >>> 16) & 0xFF) <= 195);
    }

    /**
     * 返回此 {@code InetAddress} 对象的原始IP地址。结果为网络字节顺序：地址的最高位字节在 {@code getAddress()[0]} 中。
     *
     * @return 此对象的原始IP地址。
     */
    public byte[] getAddress() {
        int address = holder().getAddress();
        byte[] addr = new byte[INADDRSZ];

        addr[0] = (byte) ((address >>> 24) & 0xFF);
        addr[1] = (byte) ((address >>> 16) & 0xFF);
        addr[2] = (byte) ((address >>> 8) & 0xFF);
        addr[3] = (byte) (address & 0xFF);
        return addr;
    }

    /**
     * 返回IP地址的文本表示形式。
     *
     * @return 原始IP地址的字符串格式。
     * @since   JDK1.0.2
     */
    public String getHostAddress() {
        return numericToTextFormat(getAddress());
    }

    /**
     * 返回此IP地址的哈希码。
     *
     * @return 此IP地址的哈希码值。
     */
    public int hashCode() {
        return holder().getAddress();
    }

    /**
     * 将此对象与指定对象进行比较。
     * 结果为 {@code true} 当且仅当参数不为 {@code null} 并且它表示与
     * 此对象相同的IP地址。
     * <p>
     * 两个 {@code InetAddress} 实例表示相同的IP地址，如果
     * 由 {@code getAddress} 返回的字节数组长度相同，并且每个
     * 字节数组的组件相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Inet4Address) &&
            (((InetAddress)obj).holder().getAddress() == holder().getAddress());
    }

    // 实用程序
    /*
     * 将IPv4二进制地址转换为适合显示的字符串。
     *
     * @param src 一个表示IPv4数值地址的字节数组
     * @return 一个表示IPv4地址的字符串
     *         文本表示格式
     * @since 1.4
     */

    static String numericToTextFormat(byte[] src)
    {
        return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
    }

    /**
     * 执行类加载时的初始化。
     */
    private static native void init();
}
