/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.net.*;
import java.util.Formatter;
import java.util.Locale;
import sun.net.util.IPAddressUtil;

/**
 * 解析包含主机/域名和端口范围的字符串
 */
class HostPortrange {

    String hostname;
    String scheme;
    int[] portrange;

    boolean wildcard;
    boolean literal;
    boolean ipv6, ipv4;
    static final int PORT_MIN = 0;
    static final int PORT_MAX = (1 << 16) -1;

    boolean equals(HostPortrange that) {
        return this.hostname.equals(that.hostname)
            && this.portrange[0] == that.portrange[0]
            && this.portrange[1] == that.portrange[1]
            && this.wildcard == that.wildcard
            && this.literal == that.literal;
    }

    public int hashCode() {
        return hostname.hashCode() + portrange[0] + portrange[1];
    }

    HostPortrange(String scheme, String str) {
        // 解析主机名。名称最多可以有三个部分：主机名、端口号或两个表示端口范围的数字。
        // "www.sun.com:8080-9090" 是一个有效的主机名。

        // IPv6 地址可以是 2010:836B:4179::836B:4179
        // IPv6 地址需要用 [] 包围
        // 例如：[2010:836B:4179::836B:4179]:8080-9090
        // 请参阅 RFC 2732 以获取更多信息。

        // 首先将字符串分为两个字段：hoststr, portstr
        String hoststr, portstr = null;
        this.scheme = scheme;

        // 检查是否为 IPv6 地址
        if (str.charAt(0) == '[') {
            ipv6 = literal = true;
            int rb = str.indexOf(']');
            if (rb != -1) {
                hoststr = str.substring(1, rb);
            } else {
                throw new IllegalArgumentException("无效的 IPv6 地址: " + str);
            }
            int sep = str.indexOf(':', rb + 1);
            if (sep != -1 && str.length() > sep) {
                portstr = str.substring(sep + 1);
            }
            // 需要规范化 hoststr
            byte[] ip = IPAddressUtil.textToNumericFormatV6(hoststr);
            if (ip == null) {
                throw new IllegalArgumentException("非法的 IPv6 地址");
            }
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            formatter.format("%02x%02x:%02x%02x:%02x%02x:%02x"
                    + "%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x",
                    ip[0], ip[1], ip[2], ip[3], ip[4], ip[5], ip[6], ip[7], ip[8],
                    ip[9], ip[10], ip[11], ip[12], ip[13], ip[14], ip[15]);
            hostname = sb.toString();
        } else {
            // 不是 IPv6，因此 ':' 是端口分隔符

            int sep = str.indexOf(':');
            if (sep != -1 && str.length() > sep) {
                hoststr = str.substring(0, sep);
                portstr = str.substring(sep + 1);
            } else {
                hoststr = sep == -1 ? str : str.substring(0, sep);
            }
            // 这是一个域名通配符规范吗？
            if (hoststr.lastIndexOf('*') > 0) {
                throw new IllegalArgumentException("无效的主机通配符规范");
            } else if (hoststr.startsWith("*")) {
                wildcard = true;
                if (hoststr.equals("*")) {
                    hoststr = "";
                } else if (hoststr.startsWith("*.")) {
                    hoststr = toLowerCase(hoststr.substring(1));
                } else {
                    throw new IllegalArgumentException("无效的主机通配符规范");
                }
            } else {
                // 检查是否为 IPv4（如果最右边的标签是数字）
                // 指定 IPv4 的常规方法是 4 个十进制标签
                // 但实际上三个、两个或单个标签格式也是有效的
                // 因此，我们通过测试最右边的标签是否为数字来识别 IPv4
                int lastdot = hoststr.lastIndexOf('.');
                if (lastdot != -1 && (hoststr.length() > 1)) {
                    boolean ipv4 = true;

                    for (int i = lastdot + 1, len = hoststr.length(); i < len; i++) {
                        char c = hoststr.charAt(i);
                        if (c < '0' || c > '9') {
                            ipv4 = false;
                            break;
                        }
                    }
                    this.ipv4 = this.literal = ipv4;
                    if (ipv4) {
                        byte[] ip = IPAddressUtil.validateNumericFormatV4(hoststr);
                        if (ip == null) {
                            throw new IllegalArgumentException("非法的 IPv4 地址");
                        }
                        StringBuilder sb = new StringBuilder();
                        Formatter formatter = new Formatter(sb, Locale.US);
                        formatter.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
                        hoststr = sb.toString();
                    } else {
                        // 普通域名
                        hoststr = toLowerCase(hoststr);
                    }
                }
            }
            hostname = hoststr;
        }

        try {
            portrange = parsePort(portstr);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的端口范围: " + portstr);
        }
    }

    static final int CASE_DIFF = 'A' - 'a';

    /**
     * 转换为小写，并检查所有字符是否为 ASCII 字母数字、'-' 或 '.'。
     */
    static String toLowerCase(String s) {
        int len = s.length();
        StringBuilder sb = null;

        for (int i=0; i<len; i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c == '.')) {
                if (sb != null)
                    sb.append(c);
            } else if ((c >= '0' && c <= '9') || (c == '-')) {
                if (sb != null)
                    sb.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(s, 0, i);
                }
                sb.append((char)(c - CASE_DIFF));
            } else {
                throw new IllegalArgumentException("主机名中包含无效字符");
            }
        }
        return sb == null ? s : sb.toString();
    }


    public boolean literal() {
        return literal;
    }

    public boolean ipv4Literal() {
        return ipv4;
    }

    public boolean ipv6Literal() {
        return ipv6;
    }

    public String hostname() {
        return hostname;
    }

    public int[] portrange() {
        return portrange;
    }

    /**
     * 如果主机名部分以 * 开头，则返回 true。
     * hostname 返回主机组件的剩余部分
     * 例如 "*.foo.com" -> ".foo.com" 或 "*" -> ""
     *
     * @return
     */
    public boolean wildcard() {
        return wildcard;
    }

    // 这些不应该泄露到实现之外
    final static int[] HTTP_PORT = {80, 80};
    final static int[] HTTPS_PORT = {443, 443};
    final static int[] NO_PORT = {-1, -1};

    int[] defaultPort() {
        if (scheme.equals("http")) {
            return HTTP_PORT;
        } else if (scheme.equals("https")) {
            return HTTPS_PORT;
        }
        return NO_PORT;
    }

    int[] parsePort(String port)
    {

        if (port == null || port.equals("")) {
            return defaultPort();
        }

        if (port.equals("*")) {
            return new int[] {PORT_MIN, PORT_MAX};
        }

        try {
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
                if (l < 0 || h < 0 || h<l) {
                    return defaultPort();
                }
                return new int[] {l, h};
             }
        } catch (IllegalArgumentException e) {
            return defaultPort();
        }
    }
}
