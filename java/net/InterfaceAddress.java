/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 此类表示网络接口地址。简而言之，它是一个 IP 地址、一个子网掩码和一个广播地址（当地址是 IPv4 时）。对于 IPv6 地址，则是一个 IP 地址和一个网络前缀长度。
 *
 * @see java.net.NetworkInterface
 * @since 1.6
 */
public class InterfaceAddress {
    private InetAddress address = null;
    private Inet4Address broadcast = null;
    private short        maskLength = 0;

    /*
     * 包私有构造函数。不能直接构建，实例通过 NetworkInterface 类获得。
     */
    InterfaceAddress() {
    }

    /**
     * 返回此地址的 {@code InetAddress}。
     *
     * @return 此地址的 {@code InetAddress}。
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * 返回此 InterfaceAddress 的广播地址的 {@code InetAddress}。
     * <p>
     * 仅 IPv4 网络有广播地址，因此，对于 IPv6 网络，将返回 {@code null}。
     *
     * @return 表示广播地址的 {@code InetAddress} 或如果不存在广播地址则返回 {@code null}。
     */
    public InetAddress getBroadcast() {
        return broadcast;
    }

    /**
     * 返回此地址的网络前缀长度。在 IPv4 地址的上下文中，这通常被称为子网掩码。
     * 典型的 IPv4 值为 8 (255.0.0.0)，16 (255.255.0.0)
     * 或 24 (255.255.255.0)。 <p>
     * 典型的 IPv6 值为 128 (::1/128) 或 10 (fe80::203:baff:fe27:1243/10)
     *
     * @return 一个表示该地址子网前缀长度的 {@code short}。
     */
     public short getNetworkPrefixLength() {
        return maskLength;
    }

    /**
     * 将此对象与指定对象进行比较。
     * 结果为 {@code true} 当且仅当参数不为 {@code null} 并且它表示与
     * 此对象相同的接口地址。
     * <p>
     * 两个 {@code InterfaceAddress} 实例表示相同的地址，如果它们的 InetAddress、前缀长度和广播地址都相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     java.net.InterfaceAddress#hashCode()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof InterfaceAddress)) {
            return false;
        }
        InterfaceAddress cmp = (InterfaceAddress) obj;
        if ( !(address == null ? cmp.address == null : address.equals(cmp.address)) )
            return false;
        if ( !(broadcast  == null ? cmp.broadcast == null : broadcast.equals(cmp.broadcast)) )
            return false;
        if (maskLength != cmp.maskLength)
            return false;
        return true;
    }

    /**
     * 返回此接口地址的哈希码。
     *
     * @return 此接口地址的哈希码值。
     */
    public int hashCode() {
        return address.hashCode() + ((broadcast != null) ? broadcast.hashCode() : 0) + maskLength;
    }

    /**
     * 将此接口地址转换为 {@code String}。返回的字符串形式为：InetAddress / 前缀长度 [ 广播地址 ]。
     *
     * @return 此接口地址的字符串表示形式。
     */
    public String toString() {
        return address + "/" + maskLength + " [" + broadcast + "]";
    }

}
