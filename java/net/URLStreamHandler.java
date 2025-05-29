
/*
 * Copyright (c) 1995, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;
import sun.net.util.IPAddressUtil;
import sun.net.www.ParseUtil;

/**
 * 抽象类 {@code URLStreamHandler} 是所有流协议处理器的公共
 * 超类。流协议处理器知道如何为特定的协议类型（如 {@code http} 或 {@code https}）建立连接。
 * <p>
 * 在大多数情况下，应用程序不会直接创建 {@code URLStreamHandler} 子类的实例。相反，当构造
 * {@code URL} 时首次遇到协议名称时，会自动加载适当的流协议处理器。
 *
 * @author  James Gosling
 * @see     java.net.URL#URL(java.lang.String, java.lang.String, int, java.lang.String)
 * @since   JDK1.0
 */
public abstract class URLStreamHandler {
    /**
     * 打开一个连接到由 {@code URL} 参数引用的对象。
     * 该方法应由子类覆盖。
     *
     * <p>如果处理器的协议（如 HTTP 或 JAR）存在一个属于以下包或其子包的公共、专用的 URLConnection 子类：
     * java.lang, java.io, java.util, java.net，则返回的连接将是该子类。例如，对于 HTTP 将返回
     * HttpURLConnection，对于 JAR 将返回 JarURLConnection。
     *
     * @param      u   连接到的 URL。
     * @return     用于 {@code URL} 的 {@code URLConnection} 对象。
     * @exception  IOException  如果在打开连接时发生 I/O 错误。
     */
    abstract protected URLConnection openConnection(URL u) throws IOException;

    /**
     * 与 openConnection(URL) 相同，但连接将通过指定的代理；不支持代理的协议处理器将忽略代理参数并进行正常连接。
     *
     * 调用此方法将抢占系统的默认 ProxySelector 设置。
     *
     * @param      u   连接到的 URL。
     * @param      p   用于连接的代理。如果需要直接连接，应指定 Proxy.NO_PROXY。
     * @return     用于 {@code URL} 的 {@code URLConnection} 对象。
     * @exception  IOException  如果在打开连接时发生 I/O 错误。
     * @exception  IllegalArgumentException 如果 u 或 p 为 null，或 p 类型错误。
     * @exception  UnsupportedOperationException 如果实现协议的子类不支持此方法。
     * @since      1.5
     */
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    /**
     * 解析 {@code URL} 的字符串表示形式为 {@code URL} 对象。
     * <p>
     * 如果有任何继承的上下文，那么它已经被复制到 {@code URL} 参数中。
     * <p>
     * {@code URLStreamHandler} 的 {@code parseURL} 方法将字符串表示形式解析为
     * {@code http} 规范。大多数 URL 协议族有类似的解析。具有不同语法的协议的流协议处理器必须覆盖此例程。
     *
     * @param   u       接收解析结果的 {@code URL}。
     * @param   spec    需要解析的表示 URL 的 {@code String}。
     * @param   start   开始解析的字符索引。这是 '{@code :}'（如果有）之后的位置，
     *                  用于指定协议名称。
     * @param   limit   停止解析的字符位置。这是字符串的末尾或 "{@code #}" 字符的位置，如果存在的话。所有
     *                  锚点信息都在尖号之后。
     */
    protected void parseURL(URL u, String spec, int start, int limit) {
        // 如果这是相对 URL，这些字段可能接收上下文内容
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
        // 解析任何的权威部分
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
                    // 如果 authority 中有多个 '@'，则这不是基于服务器的
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
                // 如果 host 被 [] 包围，则它是一个 IPv6 地址，如 RFC2732 所指定
                if (host.length()>0 && (host.charAt(0) == '[')) {
                    if ((ind = host.indexOf(']')) > 2) {

                        String nhost = host ;
                        host = nhost.substring(0,ind+1);
                        if (!IPAddressUtil.
                            isIPv6LiteralAddress(host.substring(1, ind))) {
                            throw new IllegalArgumentException(
                                "无效的主机: "+ host);
                        }

                        port = -1 ;
                        if (nhost.length() > ind+1) {
                            if (nhost.charAt(ind+1) == ':') {
                                ++ind ;
                                // 端口可以为空，根据 RFC2396
                                if (nhost.length() > (ind + 1)) {
                                    port = Integer.parseInt(nhost.substring(ind+1));
                                }
                            } else {
                                throw new IllegalArgumentException(
                                    "无效的 authority 字段: " + authority);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException(
                            "无效的 authority 字段: " + authority);
                    }
                } else {
                    ind = host.indexOf(':');
                    port = -1;
                    if (ind >= 0) {
                        // 端口可以为空，根据 RFC2396
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
                throw new IllegalArgumentException("无效的端口号 :" +
                                                   port);
            start = i;
            // 如果定义了 authority，则路径仅由 spec 定义；参见 RFC 2396 第 5.2.4 节。
            if (authority != null && !authority.isEmpty())
                path = "";
        }

        if (host == null) {
            host = "";
        }

        // 解析文件路径（如果有）
        if (start < limit) {
            if (spec.charAt(start) == '/') {
                path = spec.substring(start, limit);
            } else if (path != null && !path.isEmpty()) {
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
     * 返回此处理器解析的 URL 的默认端口。此方法旨在由具有默认端口号的处理器覆盖。
     * @return 由此处理器解析的 {@code URL} 的默认端口。
     * @since 1.3
     */
    protected int getDefaultPort() {
        return -1;
    }

    /**
     * 提供默认的 equals 计算。可以由其他协议的处理器覆盖，以满足不同的 equals() 要求。
     * 此方法要求其所有参数均不为 null。这是由 java.net.URL 类调用的事实保证的。
     * @param u1 一个 URL 对象
     * @param u2 一个 URL 对象
     * @return 如果两个 URL 被认为相等，则返回 {@code true}，即它们引用同一文件的同一片段。
     * @since 1.3
     */
    protected boolean equals(URL u1, URL u2) {
        String ref1 = u1.getRef();
        String ref2 = u2.getRef();
        return (ref1 == ref2 || (ref1 != null && ref1.equals(ref2))) &&
               sameFile(u1, u2);
    }

                /**
     * 提供默认的哈希计算。可以被其他协议的处理程序覆盖，以满足不同的哈希码计算需求。
     * @param u 一个 URL 对象
     * @return 一个适合哈希表索引的 {@code int}
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
     * 比较两个 URL 以查看它们是否引用相同的文件，即具有相同的协议、主机、端口和路径。
     * 此方法要求其所有参数均不为 null。这是通过仅由 java.net.URL 类间接调用此方法来保证的。
     * @param u1 一个 URL 对象
     * @param u2 一个 URL 对象
     * @return 如果 u1 和 u2 引用相同的文件，则返回 true
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
        port2 = (u2.getPort() != 1) ? u2.getPort() : u2.handler.getDefaultPort();
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
     * 比较两个 URL 的主机组件。
     * @param u1 要比较的第一个主机的 URL
     * @param u2 要比较的第二个主机的 URL
     * @return 如果且仅如果它们相等，则返回 {@code true}，否则返回 {@code false}。
     * @since 1.3
     */
    protected boolean hostsEqual(URL u1, URL u2) {
        InetAddress a1 = getHostAddress(u1);
        InetAddress a2 = getHostAddress(u2);
        // 如果两个都有互联网地址，则比较它们
        if (a1 != null && a2 != null) {
            return a1.equals(a2);
        // 否则，如果两个都有主机名，则比较它们
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
        if (u.getAuthority() != null && !u.getAuthority().isEmpty())
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
        if (u.getAuthority() != null && !u.getAuthority().isEmpty()) {
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
     * 仅从 URLStreamHandler 派生的类能够使用此方法来设置 URL 字段的值。
     *
     * @param   u         要修改的 URL。
     * @param   protocol  协议名称。
     * @param   host      URL 的远程主机值。
     * @param   port      远程机器上的端口。
     * @param   authority URL 的权威部分。
     * @param   userInfo  URL 的用户信息部分。
     * @param   path      URL 的路径组件。
     * @param   query     URL 的查询部分。
     * @param   ref       引用。
     * @exception       SecurityException       如果 URL 的协议处理程序与此处理程序不同
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
     * 设置 {@code URL} 参数的字段为指定的值。
     * 仅从 URLStreamHandler 派生的类能够使用此方法来设置 URL 字段的值。
     *
     * @param   u         要修改的 URL。
     * @param   protocol  协议名称。此值自 1.2 版本起被忽略。
     * @param   host      URL 的远程主机值。
     * @param   port      远程机器上的端口。
     * @param   file      文件。
     * @param   ref       引用。
     * @exception       SecurityException       如果 URL 的协议处理器
     *                                  与此处理器不同
     * @deprecated 使用 setURL(URL, String, String, int, String, String, String,
     *             String);
     */
    @Deprecated
    protected void setURL(URL u, String protocol, String host, int port,
                          String file, String ref) {
        /*
         * 仅旧的 URL 处理器会调用此方法，因此假设主机
         * 字段可能包含 "user:passwd@host"。如有必要，进行修正。
         */
        String authority = null;
        String userInfo = null;
        if (host != null && !host.isEmpty()) {
            authority = (port == -1) ? host : host + ":" + port;
            int at = host.lastIndexOf('@');
            if (at != -1) {
                userInfo = host.substring(0, at);
                host = host.substring(at+1);
            }
        }

        /*
         * 假设文件可能包含查询部分。如有必要，进行修正。
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
