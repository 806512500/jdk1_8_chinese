
/*
 * Copyright (c) 1995, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;
import sun.net.util.IPAddressUtil;
import sun.net.www.ParseUtil;

/**
 * 抽象类 {@code URLStreamHandler} 是所有流协议处理程序的公共
 * 超类。流协议处理程序知道如何为特定的协议类型（如 {@code http} 或 {@code https}）
 * 建立连接。
 * <p>
 * 在大多数情况下，应用程序不会直接创建 {@code URLStreamHandler}
 * 子类的实例。相反，当在构造 {@code URL} 时首次遇到协议名称时，
 * 适当的流协议处理程序将自动加载。
 *
 * @author  James Gosling
 * @see     java.net.URL#URL(java.lang.String, java.lang.String, int, java.lang.String)
 * @since   JDK1.0
 */
public abstract class URLStreamHandler {
    /**
     * 打开与 {@code URL} 参数引用的对象的连接。
     * 此方法应由子类覆盖。
     *
     * <p>如果处理程序的协议（如 HTTP 或 JAR）存在属于以下包或其子包的公共、专用的
     * {@code URLConnection} 子类：java.lang, java.io, java.util, java.net，
     * 则返回的连接将是该子类。例如，对于 HTTP 将返回 {@code HttpURLConnection}，
     * 对于 JAR 将返回 {@code JarURLConnection}。
     *
     * @param      u   连接的 URL。
     * @return     用于 {@code URL} 的 {@code URLConnection} 对象。
     * @exception  IOException  在打开连接时发生 I/O 错误。
     */
    abstract protected URLConnection openConnection(URL u) throws IOException;

    /**
     * 与 openConnection(URL) 相同，但连接将通过指定的代理进行；
     * 不支持代理的协议处理程序将忽略代理参数并进行正常连接。
     *
     * 调用此方法将抢占系统的默认 ProxySelector 设置。
     *
     * @param      u   连接的 URL。
     * @param      p   用于连接的代理。如果需要直接连接，应指定 Proxy.NO_PROXY。
     * @return     用于 {@code URL} 的 {@code URLConnection} 对象。
     * @exception  IOException  在打开连接时发生 I/O 错误。
     * @exception  IllegalArgumentException 如果 u 或 p 为 null，或 p 类型错误。
     * @exception  UnsupportedOperationException 如果实现协议的子类不支持此方法。
     * @since      1.5
     */
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    /**
     * 解析 {@code URL} 的字符串表示形式，生成一个 {@code URL} 对象。
     * <p>
     * 如果有任何继承的上下文，则已将其复制到 {@code URL} 参数中。
     * <p>
     * {@code URLStreamHandler} 的 {@code parseURL} 方法将字符串表示形式解析为
     * {@code http} 规范。大多数 URL 协议族的解析方式相似。具有不同语法的流协议处理程序
     * 必须覆盖此例程。
     *
     * @param   u       接收解析结果的 {@code URL}。
     * @param   spec    需要解析的表示 URL 的 {@code String}。
     * @param   start   开始解析的字符索引。这是紧跟在 '{@code :}' 之后的位置
     *                  （如果有的话），用于确定协议名称。
     * @param   limit   停止解析的字符位置。这是字符串的末尾或 "{@code #}" 字符的位置，
     *                  如果存在的话。所有在 sharp 符号之后的信息表示锚点。
     */
    protected void parseURL(URL u, String spec, int start, int limit) {
        // 如果这是相对 URL，这些字段可能包含上下文内容
        String protocol = u.getProtocol();
        String authority = u.getAuthority();
        String userInfo = u.getUserInfo();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        String query = u.getQuery();

        // 这个字段已经解析
        String ref = u.getRef();

        boolean isRelPath = false;
        boolean queryOnly = false;

// FIX: should not assume query if opaque
        // 去掉查询部分
        if (start < limit) {
            int queryStart = spec.indexOf('?');
            queryOnly = queryStart == start;
            if ((queryStart != -1) && (queryStart < limit)) {
                query = spec.substring(queryStart+1, limit);
                if (limit > queryStart)
                    limit = queryStart;
                spec = spec.substring(0, queryStart);
            }
        }

        int i = 0;
        // 解析权威部分（如果有）
        boolean isUNCName = (start <= limit - 4) &&
                        (spec.charAt(start) == '/') &&
                        (spec.charAt(start + 1) == '/') &&
                        (spec.charAt(start + 2) == '/') &&
                        (spec.charAt(start + 3) == '/');
        if (!isUNCName && (start <= limit - 2) && (spec.charAt(start) == '/') &&
            (spec.charAt(start + 1) == '/')) {
            start += 2;
            i = spec.indexOf('/', start);
            if (i < 0 || i > limit) {
                i = spec.indexOf('?', start);
                if (i < 0 || i > limit)
                    i = limit;
            }

            host = authority = spec.substring(start, i);

            int ind = authority.indexOf('@');
            if (ind != -1) {
                if (ind != authority.lastIndexOf('@')) {
                    // 权威部分中包含多个 '@'。这不是基于服务器的
                    userInfo = null;
                    host = null;
                } else {
                    userInfo = authority.substring(0, ind);
                    host = authority.substring(ind+1);
                }
            } else {
                userInfo = null;
            }
            if (host != null) {
                // 如果主机被 [ 和 ] 包围，则它是 RFC2732 指定的 IPv6 文字地址
                if (host.length()>0 && (host.charAt(0) == '[')) {
                    if ((ind = host.indexOf(']')) > 2) {

                        String nhost = host ;
                        host = nhost.substring(0,ind+1);
                        if (!IPAddressUtil.
                            isIPv6LiteralAddress(host.substring(1, ind))) {
                            throw new IllegalArgumentException(
                                "Invalid host: "+ host);
                        }

                        port = -1 ;
                        if (nhost.length() > ind+1) {
                            if (nhost.charAt(ind+1) == ':') {
                                ++ind ;
                                // 根据 RFC2396，端口可以为空
                                if (nhost.length() > (ind + 1)) {
                                    port = Integer.parseInt(nhost.substring(ind+1));
                                }
                            } else {
                                throw new IllegalArgumentException(
                                    "Invalid authority field: " + authority);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException(
                            "Invalid authority field: " + authority);
                    }
                } else {
                    ind = host.indexOf(':');
                    port = -1;
                    if (ind >= 0) {
                        // 根据 RFC2396，端口可以为空
                        if (host.length() > (ind + 1)) {
                            port = Integer.parseInt(host.substring(ind + 1));
                        }
                        host = host.substring(0, ind);
                    }
                }
            } else {
                host = "";
            }
            if (port < -1)
                throw new IllegalArgumentException("Invalid port number :" +
                                                   port);
            start = i;
            // 如果权威部分已定义，则路径仅由规范定义；参见 RFC 2396 第 5.2.4 节。
            if (authority != null && authority.length() > 0)
                path = "";
        }

        if (host == null) {
            host = "";
        }

        // 解析文件路径（如果有）
        if (start < limit) {
            if (spec.charAt(start) == '/') {
                path = spec.substring(start, limit);
            } else if (path != null && path.length() > 0) {
                isRelPath = true;
                int ind = path.lastIndexOf('/');
                String seperator = "";
                if (ind == -1 && authority != null)
                    seperator = "/";
                path = path.substring(0, ind + 1) + seperator +
                         spec.substring(start, limit);

            } else {
                String seperator = (authority != null) ? "/" : "";
                path = seperator + spec.substring(start, limit);
            }
        } else if (queryOnly && path != null) {
            int ind = path.lastIndexOf('/');
            if (ind < 0)
                ind = 0;
            path = path.substring(0, ind) + "/";
        }
        if (path == null)
            path = "";

        if (isRelPath) {
            // 移除嵌入的 /./
            while ((i = path.indexOf("/./")) >= 0) {
                path = path.substring(0, i) + path.substring(i + 2);
            }
            // 如果可能，移除嵌入的 /../
            i = 0;
            while ((i = path.indexOf("/../", i)) >= 0) {
                /*
                 * 一个 "/../" 将取消前一个段和它本身，
                 * 除非该段本身是 "/../"
                 * 例如，"/a/b/../c" 变为 "/a/c"
                 * 但 "/../../a" 应保持不变
                 */
                if (i > 0 && (limit = path.lastIndexOf('/', i - 1)) >= 0 &&
                    (path.indexOf("/../", limit) != 0)) {
                    path = path.substring(0, limit) + path.substring(i + 3);
                    i = 0;
                } else {
                    i = i + 3;
                }
            }
            // 如果可能，移除尾部的 ..
            while (path.endsWith("/..")) {
                i = path.indexOf("/..");
                if ((limit = path.lastIndexOf('/', i - 1)) >= 0) {
                    path = path.substring(0, limit+1);
                } else {
                    break;
                }
            }
            // 移除开头的 .
            if (path.startsWith("./") && path.length() > 2)
                path = path.substring(2);

            // 移除尾部的 .
            if (path.endsWith("/."))
                path = path.substring(0, path.length() -1);
        }

        setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }

    /**
     * 返回此处理程序解析的 URL 的默认端口。此方法旨在由具有默认端口号的处理程序覆盖。
     * @return 由此处理程序解析的 {@code URL} 的默认端口。
     * @since 1.3
     */
    protected int getDefaultPort() {
        return -1;
    }

    /**
     * 提供默认的 equals 计算。可以由其他协议的处理程序覆盖，以满足不同的 equals() 要求。
     * 此方法要求其所有参数均不为 null。这是由它仅由 java.net.URL 类调用的事实保证的。
     * @param u1 一个 URL 对象
     * @param u2 一个 URL 对象
     * @return 如果两个 URL 被认为相等，则返回 {@code true}，即它们引用同一个文件的同一片段。
     * @since 1.3
     */
    protected boolean equals(URL u1, URL u2) {
        String ref1 = u1.getRef();
        String ref2 = u2.getRef();
        return (ref1 == ref2 || (ref1 != null && ref1.equals(ref2))) &&
               sameFile(u1, u2);
    }

    /**
     * 提供默认的哈希计算。可以由其他协议的处理程序覆盖，以满足不同的 hashCode 计算要求。
     * @param u 一个 URL 对象
     * @return 适合哈希表索引的 {@code int}
     * @since 1.3
     */
    protected int hashCode(URL u) {
        int h = 0;

        // 生成协议部分。
        String protocol = u.getProtocol();
        if (protocol != null)
            h += protocol.hashCode();

        // 生成主机部分。
        InetAddress addr = getHostAddress(u);
        if (addr != null) {
            h += addr.hashCode();
        } else {
            String host = u.getHost();
            if (host != null)
                h += host.toLowerCase().hashCode();
        }

        // 生成文件部分。
        String file = u.getFile();
        if (file != null)
            h += file.hashCode();

        // 生成端口部分。
        if (u.getPort() == -1)
            h += getDefaultPort();
        else
            h += u.getPort();

        // 生成引用部分。
        String ref = u.getRef();
        if (ref != null)
            h += ref.hashCode();

        return h;
    }

    /**
     * 比较两个 URL 以查看它们是否引用同一个文件，即具有相同的协议、主机、端口和路径。
     * 此方法要求其所有参数均不为 null。这是由它仅由 java.net.URL 类间接调用的事实保证的。
     * @param u1 一个 URL 对象
     * @param u2 一个 URL 对象
     * @return 如果 u1 和 u2 引用同一个文件，则返回 true
     * @since 1.3
     */
    protected boolean sameFile(URL u1, URL u2) {
        // 比较协议。
        if (!((u1.getProtocol() == u2.getProtocol()) ||
              (u1.getProtocol() != null &&
               u1.getProtocol().equalsIgnoreCase(u2.getProtocol()))))
            return false;


                    // 比较文件。
        if (!(u1.getFile() == u2.getFile() ||
              (u1.getFile() != null && u1.getFile().equals(u2.getFile()))))
            return false;

        // 比较端口。
        int port1, port2;
        port1 = (u1.getPort() != -1) ? u1.getPort() : u1.handler.getDefaultPort();
        port2 = (u2.getPort() != -1) ? u2.getPort() : u2.handler.getDefaultPort();
        if (port1 != port2)
            return false;

        // 比较主机。
        if (!hostsEqual(u1, u2))
            return false;

        return true;
    }

    /**
     * 获取主机的 IP 地址。空主机字段或 DNS 失败将导致返回 null。
     *
     * @param u 一个 URL 对象
     * @return 一个表示主机 IP 地址的 {@code InetAddress}。
     * @since 1.3
     */
    protected InetAddress getHostAddress(URL u) {
        return u.getHostAddress();
    }

    /**
     * 比较两个 URL 的主机部分。
     * @param u1 要比较的第一个主机的 URL
     * @param u2 要比较的第二个主机的 URL
     * @return 如果且仅如果它们相等，则返回 {@code true}，否则返回 {@code false}。
     * @since 1.3
     */
    protected boolean hostsEqual(URL u1, URL u2) {
        InetAddress a1 = getHostAddress(u1);
        InetAddress a2 = getHostAddress(u2);
        // 如果两个都有互联网地址，比较它们
        if (a1 != null && a2 != null) {
            return a1.equals(a2);
        // 否则，如果两个都有主机名，比较它们
        } else if (u1.getHost() != null && u2.getHost() != null)
            return u1.getHost().equalsIgnoreCase(u2.getHost());
         else
            return u1.getHost() == null && u2.getHost() == null;
    }

    /**
     * 将特定协议的 {@code URL} 转换为 {@code String}。
     *
     * @param   u   URL。
     * @return  {@code URL} 参数的字符串表示形式。
     */
    protected String toExternalForm(URL u) {

        // 预计算 StringBuffer 的长度
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0)
            len += 2 + u.getAuthority().length();
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += 1 + u.getQuery().length();
        }
        if (u.getRef() != null)
            len += 1 + u.getRef().length();

        StringBuffer result = new StringBuffer(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(u.getPath());
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(u.getQuery());
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }

    /**
     * 将 {@code URL} 参数的字段设置为指定的值。
     * 只有从 URLStreamHandler 派生的类才能使用此方法来设置 URL 字段的值。
     *
     * @param   u         要修改的 URL。
     * @param   protocol  协议名称。
     * @param   host      URL 的远程主机值。
     * @param   port      远程机器上的端口。
     * @param   authority URL 的权威部分。
     * @param   userInfo  URL 的 userInfo 部分。
     * @param   path      URL 的路径组件。
     * @param   query     URL 的查询部分。
     * @param   ref       引用。
     * @exception       SecurityException       如果 URL 的协议处理器与此不同
     * @see     java.net.URL#set(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)
     * @since 1.3
     */
    protected void setURL(URL u, String protocol, String host, int port,
                             String authority, String userInfo, String path,
                             String query, String ref) {
        if (this != u.handler) {
            throw new SecurityException("handler for url different from " +
                                        "this handler");
        } else if (host != null && u.isBuiltinStreamHandler(this)) {
            String s = IPAddressUtil.checkHostString(host);
            if (s != null) throw new IllegalArgumentException(s);
        }
        // 确保没有人可以重置给定 URL 的协议。
        u.set(u.getProtocol(), host, port, authority, userInfo, path, query, ref);
    }

    /**
     * 将 {@code URL} 参数的字段设置为指定的值。
     * 只有从 URLStreamHandler 派生的类才能使用此方法来设置 URL 字段的值。
     *
     * @param   u         要修改的 URL。
     * @param   protocol  协议名称。自 1.2 起忽略此值。
     * @param   host      URL 的远程主机值。
     * @param   port      远程机器上的端口。
     * @param   file      文件。
     * @param   ref       引用。
     * @exception       SecurityException       如果 URL 的协议处理器与此不同
     * @deprecated 使用 setURL(URL, String, String, int, String, String, String,
     *             String);
     */
    @Deprecated
    protected void setURL(URL u, String protocol, String host, int port,
                          String file, String ref) {
        /*
         * 只有旧的 URL 处理器会调用此方法，因此假设主机字段可能包含 "user:passwd@host"。必要时进行修正。
         */
        String authority = null;
        String userInfo = null;
        if (host != null && host.length() != 0) {
            authority = (port == -1) ? host : host + ":" + port;
            int at = host.lastIndexOf('@');
            if (at != -1) {
                userInfo = host.substring(0, at);
                host = host.substring(at+1);
            }
        }

        /*
         * 假设文件可能包含查询部分。必要时进行修正。
         */
        String path = null;
        String query = null;
        if (file != null) {
            int q = file.lastIndexOf('?');
            if (q != -1) {
                query = file.substring(q+1);
                path = file.substring(0, q);
            } else
                path = file;
        }
        setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }
}
