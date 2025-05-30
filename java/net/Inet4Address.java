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

import java.io.ObjectStreamException;

/**
 * 此类表示互联网协议版本4 (IPv4) 地址。
 * 定义参见 <a href="http://www.ietf.org/rfc/rfc790.txt">
 * <i>RFC&nbsp;790: 已分配号码</i></a>,
 * <a href="http://www.ietf.org/rfc/rfc1918.txt">
 * <i>RFC&nbsp;1918: 私有互联网地址分配</i></a>,
 * 和 <a href="http://www.ietf.org/rfc/rfc2365.txt"><i>RFC&nbsp;2365:
 * 管理范围的IP多播</i></a>
 *
 * <h3> <A NAME="format">IP地址的文本表示</a> </h3>
 *
 * 作为方法输入的IPv4地址的文本表示形式如下：
 *
 * <blockquote><table cellpadding=0 cellspacing=0 summary="layout">
 * <tr><td>{@code d.d.d.d}</td></tr>
 * <tr><td>{@code d.d.d}</td></tr>
 * <tr><td>{@code d.d}</td></tr>
 * <tr><td>{@code d}</td></tr>
 * </table></blockquote>
 *
 * <p> 当指定四个部分时，每个部分都被解释为一个字节的数据，并从左到右分配给IPv4地址的四个字节。
 *
 * <p> 当指定三个部分的地址时，最后一部分被解释为一个16位的量，并放置在网络地址的最右边两个字节中。这使得三部分地址格式便于指定B类网络地址，如128.net.host。
 *
 * <p> 当提供两个部分的地址时，最后一部分被解释为一个24位的量，并放置在网络地址的最右边三个字节中。这使得两部分地址格式便于指定A类网络地址，如net.host。
 *
 * <p> 当只给出一个部分时，该值直接存储在网络地址中，不进行任何字节重新排列。
 *
 * <p> 对于返回文本表示作为输出值的方法，使用第一种形式，即点分四段字符串。
 *
 * <h4> 多播地址的范围 </h4>
 *
 * 历史上，IPv4 TTL字段在IP头中同时作为多播范围字段：TTL为0表示节点本地，1表示链路本地，直到32表示站点本地，直到64表示区域本地，直到128表示大陆本地，直到255表示全球。然而，管理范围是首选的。
 * 请参阅 <a href="http://www.ietf.org/rfc/rfc2365.txt">
 * <i>RFC&nbsp;2365: 管理范围的IP多播</i></a>
 * @since 1.4
 */

public final
class Inet4Address extends InetAddress {
    final static int INADDRSZ = 4;

    /** 使用InetAddress的serialVersionUID，但在序列化之前，Inet4Address实例总是被一个InetAddress实例替换 */
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
     * 用一个InetAddress对象替换要序列化的对象。
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
         * 在1.4之前，InetAddress是基于平台的AF_INET值（通常为2）创建的。
         * 为了兼容性，因此必须用这个家族写入InetAddress。
         */
        inet.holder().family = 2;

        return inet;
    }

    /**
     * 检查InetAddress是否为IP多播地址。IP多播地址是D类地址，即地址的前四位为1110。
     * @return 一个 {@code boolean} 表示InetAddress是否为IP多播地址
     * @since   JDK1.1
     */
    public boolean isMulticastAddress() {
        return ((holder().getAddress() & 0xf0000000) == 0xe0000000);
    }

    /**
     * 检查InetAddress是否为通配符地址。
     * @return 一个 {@code boolean} 表示InetAddress是否为通配符地址。
     * @since 1.4
     */
    public boolean isAnyLocalAddress() {
        return holder().getAddress() == 0;
    }

    /**
     * 检查InetAddress是否为环回地址。
     *
     * @return 一个 {@code boolean} 表示InetAddress是否为环回地址；否则返回false。
     * @since 1.4
     */
    public boolean isLoopbackAddress() {
        /* 127.x.x.x */
        byte[] byteAddr = getAddress();
        return byteAddr[0] == 127;
    }

    /**
     * 检查InetAddress是否为链路本地地址。
     *
     * @return 一个 {@code boolean} 表示InetAddress是否为链路本地地址；否则返回false，表示地址不是链路本地单播地址。
     * @since 1.4
     */
    public boolean isLinkLocalAddress() {
        // 链路本地单播在IPv4中 (169.254.0.0/16)
        // 定义参见 "记录已注册的特殊用途IPv4地址块" 由Bill Manning
        // draft-manning-dsua-06.txt
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 169)
            && (((address >>> 16) & 0xFF) == 254);
    }

    /**
     * 检查InetAddress是否为站点本地地址。
     *
     * @return 一个 {@code boolean} 表示InetAddress是否为站点本地地址；否则返回false，表示地址不是站点本地单播地址。
     * @since 1.4
     */
    public boolean isSiteLocalAddress() {
        // 参见RFC 1918
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
     * 检查多播地址是否具有全球范围。
     *
     * @return 一个 {@code boolean} 表示地址是否为全球范围的多播地址，如果不是全球范围或不是多播地址则返回false
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
     * 检查多播地址是否具有节点范围。
     *
     * @return 一个 {@code boolean} 表示地址是否为节点本地范围的多播地址，如果不是节点本地范围或不是多播地址则返回false
     * @since 1.4
     */
    public boolean isMCNodeLocal() {
        // 除非 ttl == 0
        return false;
    }

    /**
     * 检查多播地址是否具有链路范围。
     *
     * @return 一个 {@code boolean} 表示地址是否为链路本地范围的多播地址，如果不是链路本地范围或不是多播地址则返回false
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
     * 检查多播地址是否具有站点范围。
     *
     * @return 一个 {@code boolean} 表示地址是否为站点本地范围的多播地址，如果不是站点本地范围或不是多播地址则返回false
     * @since 1.4
     */
    public boolean isMCSiteLocal() {
        // 239.255/16 前缀或 ttl < 32
        int address = holder().getAddress();
        return (((address >>> 24) & 0xFF) == 239)
            && (((address >>> 16) & 0xFF) == 255);
    }

    /**
     * 检查多播地址是否具有组织范围。
     *
     * @return 一个 {@code boolean} 表示地址是否为组织本地范围的多播地址，如果不是组织本地范围或不是多播地址则返回false
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
     * 返回此 {@code InetAddress} 对象的原始IP地址。结果是网络字节顺序：地址的最高字节在 {@code getAddress()[0]} 中。
     *
     * @return  此对象的原始IP地址。
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
     * @return  原始IP地址的字符串格式。
     * @since   JDK1.0.2
     */
    public String getHostAddress() {
        return numericToTextFormat(getAddress());
    }

    /**
     * 返回此IP地址的哈希码。
     *
     * @return  此IP地址的哈希码值。
     */
    public int hashCode() {
        return holder().getAddress();
    }

    /**
     * 比较此对象与指定对象。结果为 {@code true} 当且仅当参数不为 {@code null} 并且它表示与
     * 此对象相同的IP地址。
     * <p>
     * 两个 {@code InetAddress} 实例表示相同的IP地址，如果 {@code getAddress} 返回的字节数组长度相同，且每个数组组件相同。
     *
     * @param   obj   要比较的对象。
     * @return  {@code true} 如果对象相同；否则返回 {@code false}。
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Inet4Address) &&
            (((InetAddress)obj).holder().getAddress() == holder().getAddress());
    }

    // 工具方法
    /*
     * 将IPv4二进制地址转换为适合显示的字符串。
     *
     * @param src 一个表示IPv4数字地址的字节数组
     * @return 一个表示IPv4地址的文本格式的字符串
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
