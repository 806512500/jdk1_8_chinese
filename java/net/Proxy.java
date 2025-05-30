/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 该类表示一个代理设置，通常包括类型（http、socks）和套接字地址。
 * {@code Proxy} 是一个不可变对象。
 *
 * @see     java.net.ProxySelector
 * @author Yingxian Wang
 * @author Jean-Christophe Collet
 * @since   1.5
 */
public class Proxy {

    /**
     * 表示代理类型。
     *
     * @since 1.5
     */
    public enum Type {
        /**
         * 表示直接连接，或没有代理。
         */
        DIRECT,
        /**
         * 表示用于高级协议（如 HTTP 或 FTP）的代理。
         */
        HTTP,
        /**
         * 表示 SOCKS（V4 或 V5）代理。
         */
        SOCKS
    };

    private Type type;
    private SocketAddress sa;

    /**
     * 表示一个 {@code DIRECT} 连接的代理设置，
     * 基本上告诉协议处理程序不要使用任何代理。
     * 例如，用于创建绕过其他全局代理设置（如 SOCKS）的套接字：
     * <P>
     * {@code Socket s = new Socket(Proxy.NO_PROXY);}
     *
     */
    public final static Proxy NO_PROXY = new Proxy();

    // 创建表示 {@code DIRECT} 连接的代理。
    private Proxy() {
        type = Type.DIRECT;
        sa = null;
    }

    /**
     * 创建一个表示代理连接的条目。
     * 某些组合是非法的。例如，对于类型 Http 和 Socks，必须提供一个 SocketAddress。
     * <P>
     * 使用 {@code Proxy.NO_PROXY} 常量表示直接连接。
     *
     * @param type 代理的类型
     * @param sa 该代理的套接字地址
     * @throws IllegalArgumentException 当类型和地址不兼容时抛出
     */
    public Proxy(Type type, SocketAddress sa) {
        if ((type == Type.DIRECT) || !(sa instanceof InetSocketAddress))
            throw new IllegalArgumentException("type " + type + " is not compatible with address " + sa);
        this.type = type;
        this.sa = sa;
    }

    /**
     * 返回代理类型。
     *
     * @return 一个表示代理类型的 Type
     */
    public Type type() {
        return type;
    }

    /**
     * 返回代理的套接字地址，如果是直接连接则返回 {@code null}。
     *
     * @return 一个表示代理套接字端点的 {@code SocketAddress}
     */
    public SocketAddress address() {
        return sa;
    }

    /**
     * 构造此代理的字符串表示形式。
     * 该字符串通过调用其类型和地址的 toString() 方法构造，如果类型不是 {@code DIRECT}，则在类型和地址之间添加 " @ "。
     *
     * @return 此对象的字符串表示形式。
     */
    public String toString() {
        if (type() == Type.DIRECT)
            return "DIRECT";
        return type() + " @ " + address();
    }

        /**
     * 将此对象与指定对象进行比较。
     * 结果为 {@code true} 当且仅当参数不为 {@code null} 并且表示与
     * 此对象相同的代理。
     * <p>
     * 两个 {@code Proxy} 实例表示相同的地址，如果它们的 SocketAddresses 和类型都相等。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同则返回 {@code true}；否则返回 {@code false}。
     * @see java.net.InetSocketAddress#equals(java.lang.Object)
     */
    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Proxy))
            return false;
        Proxy p = (Proxy) obj;
        if (p.type() == type()) {
            if (address() == null) {
                return (p.address() == null);
            } else
                return address().equals(p.address());
        }
        return false;
    }

    /**
     * 返回此 Proxy 的哈希码。
     *
     * @return 一个表示此 Proxy 的哈希码值。
     */
    public final int hashCode() {
        if (address() == null)
            return type().hashCode();
        return type().hashCode() + address().hashCode();
    }
}
