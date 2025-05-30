/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 该类表示网络接口地址。简而言之，它是一个 IP 地址、一个子网掩码和一个广播地址（当地址是 IPv4 时）。对于 IPv6 地址，则是一个 IP 地址和一个网络前缀长度。
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
     * @return 该地址的 {@code InetAddress}。
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * 返回此 InterfaceAddress 的广播地址的 {@code InetAddress}。
     * <p>
     * 只有 IPv4 网络有广播地址，因此在 IPv6 网络中，将返回 {@code null}。
     *
     * @return 代表广播地址的 {@code InetAddress} 或如果不存在广播地址则返回 {@code null}。
     */
    public InetAddress getBroadcast() {
        return broadcast;
    }

    /**
     * 返回此地址的网络前缀长度。在 IPv4 地址的上下文中，这通常被称为子网掩码。
     * 典型的 IPv4 值为 8 (255.0.0.0)、16 (255.255.0.0) 或 24 (255.255.255.0)。 <p>
     * 典型的 IPv6 值为 128 (::1/128) 或 10 (fe80::203:baff:fe27:1243/10)
     *
     * @return 代表该地址子网的前缀长度的 {@code short}。
     */
     public short getNetworkPrefixLength() {
        return maskLength;
    }

    /**
     * 将此对象与指定的对象进行比较。
     * 结果为 {@code true} 当且仅当参数不为 {@code null} 并且表示与该对象相同的接口地址。
     * <p>
     * 两个 {@code InterfaceAddress} 实例表示相同的地址，当且仅当它们的 InetAddress、前缀长度和广播地址都相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同则返回 {@code true}；否则返回 {@code false}。
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
     * @return 该接口地址的哈希码值。
     */
    public int hashCode() {
        return address.hashCode() + ((broadcast != null) ? broadcast.hashCode() : 0) + maskLength;
    }

    /**
     * 将此接口地址转换为 {@code String}。返回的字符串形式为：InetAddress / 前缀长度 [ 广播地址 ]。
     *
     * @return 该接口地址的字符串表示形式。
     */
    public String toString() {
        return address + "/" + maskLength + " [" + broadcast + "]";
    }

}
