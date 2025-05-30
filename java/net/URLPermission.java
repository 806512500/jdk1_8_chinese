
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.security.Permission;

/**
 * 表示对由给定 URL 定义的资源或资源集的访问权限，以及给定的一组用户可设置的请求方法和请求头。
 * 权限的 <i>名称</i> 是 URL 字符串。 <i>操作</i> 字符串是请求方法和头的连接。
 * 本类不限制方法和头名称的范围。
 * <p><b>URL</b><p>
 * URL 字符串具有以下预期结构。
 * <pre>
 *     scheme : // authority [ / path ]
 * </pre>
 * <i>scheme</i> 通常是 http 或 https，但不受本类限制。
 * <i>authority</i> 指定为：
 * <pre>
 *     authority = [ userinfo @ ] hostrange [ : portrange ]
 *     portrange = portnumber | -portnumber | portnumber-[portnumber] | *
 *     hostrange = ([*.] dnsname) | IPv4address | IPv6address
 * </pre>
 * <i>dnsname</i> 是标准的 DNS 主机或域名，即一个或多个由 "." 分隔的标签。
 * <i>IPv4address</i> 是标准的 IPv4 地址，<i>IPv6address</i> 是在 <a href="http://www.ietf.org/rfc/rfc2732.txt">
 * RFC 2732</a> 中定义的。字面 IPv6 地址必须用 '[]' 字符括起来。
 * <i>dnsname</i> 规范可以以 "*." 开头，这意味着该名称将匹配任何右域名标签与此名称相同的主机名。
 * 例如，"*.oracle.com" 匹配 "foo.bar.oracle.com"
 * <p>
 * <i>portrange</i> 用于指定端口号，或此权限适用的有界或无界端口范围。
 * 如果 portrange 缺失或无效，则假设默认端口号为 http (默认 80) 或 https (默认 443)。
 * 对于其他方案，不假设默认值。可以指定通配符，表示所有端口。
 * <p>
 * <i>userinfo</i> 是可选的。如果存在 userinfo 组件，在创建 URLPermission 时将被忽略，对本类定义的任何其他方法没有影响。
 * <p>
 * <i>path</i> 组件由一系列路径段组成，用 '/' 字符分隔。<i>path</i> 也可以为空。
 * 路径的指定方式类似于 {@link java.io.FilePermission} 中的路径。有三种不同的方式，如下例所示：
 * <table border>
 * <caption>URL 示例</caption>
 * <tr><th>示例 URL</th><th>描述</th></tr>
 * <tr><td style="white-space:nowrap;">http://www.oracle.com/a/b/c.html</td>
 *   <td>一个标识特定（单个）资源的 URL</td>
 * </tr>
 * <tr><td>http://www.oracle.com/a/b/*</td>
 *   <td>'*' 字符表示同一 "目录" 中的所有资源 - 换句话说，所有具有相同数量的路径组件，并且仅在最终路径组件中不同的资源，由 '*' 表示。
 *   </td>
 * </tr>
 * <tr><td>http://www.oracle.com/a/b/-</td>
 *   <td>'-' 字符表示所有递归位于前一路径下的资源（例如，http://www.oracle.com/a/b/c/d/e.html 匹配此示例）。
 *   </td>
 * </tr>
 * </table>
 * <p>
 * '*' 和 '-' 只能在路径的最后一个段中指定，并且必须是该段中的唯一字符。构造 URLPermissions 时，URL 的任何查询或片段组件都将被忽略。
 * <p>
 * 作为一种特殊情况，形式为 "scheme:*" 的 URL 被接受，表示任何给定方案的 URL。
 * <p>
 * URL 字符串的 <i>scheme</i> 和 <i>authority</i> 组件不区分大小写处理。这意味着 {@link #equals(Object)}、
 * {@link #hashCode()} 和 {@link #implies(Permission)} 在这些组件上是不区分大小写的。如果 <i>authority</i> 包含字面 IP 地址，
 * 则地址将被规范化以进行比较。路径组件是区分大小写的。
 * <p><b>操作字符串</b><p>
 * URLPermission 的操作字符串是 <i>方法列表</i> 和 <i>请求头列表</i> 的连接。这些列表分别是权限允许的请求方法和允许的请求头的列表。
 * 两个列表由冒号 ':' 字符分隔，每个列表的元素由逗号分隔。一些示例如下：
 * <pre>
 *         "POST,GET,DELETE"
 *         "GET:X-Foo-Request,X-Bar-Request"
 *         "POST,GET:Header1,Header2"
 * </pre>
 * 第一个示例指定了方法：POST、GET 和 DELETE，但没有请求头。
 * 第二个示例指定了一个请求方法和两个头。第三个示例指定了两个请求方法和两个头。
 * <p>
 * 如果请求头列表为空，则冒号分隔符可以省略。操作字符串中不允许有空格。提供给 URLPermission 构造函数的操作字符串是不区分大小写的，并通过将方法名称转换为大写
 * 和将头名称转换为 RFC2616 定义的形式（每个单词的首字母大写，其余小写）进行规范化。任一列表可以包含通配符 '*'，表示所有请求方法或头。
 * <p>
 * 注意。根据使用上下文，某些请求方法和头可能始终被允许，而其他方法和头可能始终不被允许。例如，HTTP 协议处理程序可能不允许应用程序代码设置某些头，
 * 例如 Content-Length，无论当前的安全策略是否允许。
 *
 * @since 1.8
 */
public final class URLPermission extends Permission {

    private static final long serialVersionUID = -2702463814894478682L;

    private transient String scheme;
    private transient String ssp;                 // 方案特定部分
    private transient String path;
    private transient List<String> methods;
    private transient List<String> requestHeaders;
    private transient Authority authority;

    // 序列化字段
    private String actions;

    /**
     * 从 URL 字符串创建一个新的 URLPermission，并允许给定的请求方法和用户可设置的请求头。
     * 权限的名称是创建时使用的 URL 字符串。仅使用 URL 的方案、权威部分和路径组件。任何片段或查询组件都将被忽略。
     * 权限的操作字符串如上所述。
     *
     * @param url URL 字符串
     *
     * @param actions 操作字符串
     *
     * @exception IllegalArgumentException 如果 URL 无效或操作字符串包含空格。
     */
    public URLPermission(String url, String actions) {
        super(url);
        init(actions);
    }

    private void init(String actions) {
        parseURI(getName());
        int colon = actions.indexOf(':');
        if (actions.lastIndexOf(':') != colon) {
            throw new IllegalArgumentException(
                "无效的操作字符串: \"" + actions + "\"");
        }

        String methods, headers;
        if (colon == -1) {
            methods = actions;
            headers = "";
        } else {
            methods = actions.substring(0, colon);
            headers = actions.substring(colon+1);
        }

        List<String> l = normalizeMethods(methods);
        Collections.sort(l);
        this.methods = Collections.unmodifiableList(l);

        l = normalizeHeaders(headers);
        Collections.sort(l);
        this.requestHeaders = Collections.unmodifiableList(l);

        this.actions = actions();
    }

    /**
     * 通过调用两个参数的构造函数创建具有给定 URL 字符串和无限制的方法和请求头的 URLPermission，如下所示：URLPermission(url, "*:*")
     *
     * @param url URL 字符串
     *
     * @throws    IllegalArgumentException 如果 URL 不能生成有效的 {@link URI}
     */
    public URLPermission(String url) {
        this(url, "*:*");
    }

    /**
     * 返回规范化的方法列表和请求头列表，形式如下：
     * <pre>
     *      "方法名称 : 头名称"
     * </pre>
     * <p>
     * 其中方法名称是用逗号分隔的方法列表，头名称是用逗号分隔的允许头列表。
     * 返回的字符串中没有空格。如果头名称为空，则不会出现冒号分隔符。
     */
    public String getActions() {
        return actions;
    }

    /**
     * 检查此 URLPermission 是否隐含给定的权限。
     * 具体来说，以下检查按以下顺序进行：
     * <ul>
     * <li>如果 'p' 不是 URLPermission 的实例，则返回 false</li>
     * <li>如果 p 的任何方法不在此方法列表中，并且此方法列表不等于 "*"，则返回 false。</li>
     * <li>如果 p 的任何头不在此请求头列表中，并且此请求头列表不等于 "*"，则返回 false。</li>
     * <li>如果此 URL 方案不等于 p 的 URL 方案，则返回 false</li>
     * <li>如果此 URL 的方案特定部分为 '*'，则返回 true</li>
     * <li>如果 p 的 URL 定义的主机集不是此 URL 定义的主机集的子集，则返回 false。例如，"*.foo.oracle.com"
     *     是 "*.oracle.com" 的子集。"foo.bar.oracle.com" 不是 "*.foo.oracle.com" 的子集</li>
     * <li>如果 p 的 URL 定义的端口范围不是此 URL 定义的端口范围的子集，则返回 false。
     * <li>如果 p 的 URL 指定的路径或路径集包含在此 URL 指定的路径集中，则返回 true
     * <li>否则，返回 false</li>
     * </ul>
     * <p>以下是一些路径匹配的示例：
     * <table border>
     * <caption>路径匹配示例</caption>
     * <tr><th>此路径</th><th>p 的路径</th><th>匹配</th></tr>
     * <tr><td>/a/b</td><td>/a/b</td><td>是</td></tr>
     * <tr><td>/a/b/*</td><td>/a/b/c</td><td>是</td></tr>
     * <tr><td>/a/b/*</td><td>/a/b/c/d</td><td>否</td></tr>
     * <tr><td>/a/b/-</td><td>/a/b/c/d</td><td>是</td></tr>
     * <tr><td>/a/b/-</td><td>/a/b/c/d/e</td><td>是</td></tr>
     * <tr><td>/a/b/-</td><td>/a/b/c/*</td><td>是</td></tr>
     * <tr><td>/a/b/*</td><td>/a/b/c/-</td><td>否</td></tr>
     * </table>
     */
    public boolean implies(Permission p) {
        if (! (p instanceof URLPermission)) {
            return false;
        }

        URLPermission that = (URLPermission)p;

        if (!this.methods.get(0).equals("*") &&
                Collections.indexOfSubList(this.methods, that.methods) == -1) {
            return false;
        }

        if (this.requestHeaders.isEmpty() && !that.requestHeaders.isEmpty()) {
            return false;
        }

        if (!this.requestHeaders.isEmpty() &&
            !this.requestHeaders.get(0).equals("*") &&
             Collections.indexOfSubList(this.requestHeaders,
                                        that.requestHeaders) == -1) {
            return false;
        }

        if (!this.scheme.equals(that.scheme)) {
            return false;
        }

        if (this.ssp.equals("*")) {
            return true;
        }

        if (!this.authority.implies(that.authority)) {
            return false;
        }

        if (this.path == null) {
            return that.path == null;
        }
        if (that.path == null) {
            return false;
        }

        if (this.path.endsWith("/-")) {
            String thisprefix = this.path.substring(0, this.path.length() - 1);
            return that.path.startsWith(thisprefix);
            }

        if (this.path.endsWith("/*")) {
            String thisprefix = this.path.substring(0, this.path.length() - 1);
            if (!that.path.startsWith(thisprefix)) {
                return false;
            }
            String thatsuffix = that.path.substring(thisprefix.length());
            // 后缀不能包含 '/' 字符
            if (thatsuffix.indexOf('/') != -1) {
                return false;
            }
            if (thatsuffix.equals("-")) {
                return false;
            }
            return true;
        }
        return this.path.equals(that.path);
    }


    /**
     * 如果 this.getActions().equals(p.getActions()) 并且 p 的 URL 等于此 URL，则返回 true。否则返回 false。
     */
    public boolean equals(Object p) {
        if (!(p instanceof URLPermission)) {
            return false;
        }
        URLPermission that = (URLPermission)p;
        if (!this.scheme.equals(that.scheme)) {
            return false;
        }
        if (!this.getActions().equals(that.getActions())) {
            return false;
        }
        if (!this.authority.equals(that.authority)) {
            return false;
        }
        if (this.path != null) {
            return this.path.equals(that.path);
        } else {
            return that.path == null;
        }
    }

    /**
     * 返回从操作字符串和 URL 字符串的哈希码计算出的哈希码。
     */
    public int hashCode() {
        return getActions().hashCode()
            + scheme.hashCode()
            + authority.hashCode()
            + (path == null ? 0 : path.hashCode());
    }


    private List<String> normalizeMethods(String methods) {
        List<String> l = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        for (int i=0; i<methods.length(); i++) {
            char c = methods.charAt(i);
            if (c == ',') {
                String s = b.toString();
                if (s.length() > 0)
                    l.add(s);
                b = new StringBuilder();
            } else if (c == ' ' || c == '\t') {
                throw new IllegalArgumentException(
                    "方法中不允许有空格: \"" + methods + "\"");
            } else {
                if (c >= 'a' && c <= 'z') {
                    c += 'A' - 'a';
                }
                b.append(c);
            }
        }
        String s = b.toString();
        if (s.length() > 0)
            l.add(s);
        return l;
    }


                private List<String> normalizeHeaders(String headers) {
        List<String> l = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i=0; i<headers.length(); i++) {
            char c = headers.charAt(i);
            if (c >= 'a' && c <= 'z') {
                if (capitalizeNext) {
                    c += 'A' - 'a';
                    capitalizeNext = false;
                }
                b.append(c);
            } else if (c == ' ' || c == '\t') {
                throw new IllegalArgumentException(
                    "标头中不允许有空格: \"" + headers + "\"");
            } else if (c == '-') {
                    capitalizeNext = true;
                b.append(c);
            } else if (c == ',') {
                String s = b.toString();
                if (s.length() > 0)
                    l.add(s);
                b = new StringBuilder();
                capitalizeNext = true;
            } else {
                capitalizeNext = false;
                b.append(c);
            }
        }
        String s = b.toString();
        if (s.length() > 0)
            l.add(s);
        return l;
    }

    private void parseURI(String url) {
        int len = url.length();
        int delim = url.indexOf(':');
        if (delim == -1 || delim + 1 == len) {
            throw new IllegalArgumentException(
                "无效的URL字符串: \"" + url + "\"");
        }
        scheme = url.substring(0, delim).toLowerCase();
        this.ssp = url.substring(delim + 1);

        if (!ssp.startsWith("//")) {
            if (!ssp.equals("*")) {
                throw new IllegalArgumentException(
                    "无效的URL字符串: \"" + url + "\"");
            }
            this.authority = new Authority(scheme, "*");
            return;
        }
        String authpath = ssp.substring(2);

        delim = authpath.indexOf('/');
        String auth;
        if (delim == -1) {
            this.path = "";
            auth = authpath;
        } else {
            auth = authpath.substring(0, delim);
            this.path = authpath.substring(delim);
        }
        this.authority = new Authority(scheme, auth.toLowerCase());
    }

    private String actions() {
        StringBuilder b = new StringBuilder();
        for (String s : methods) {
            b.append(s);
        }
        b.append(":");
        for (String s : requestHeaders) {
            b.append(s);
        }
        return b.toString();
    }

    /**
     * 从流中恢复此对象的状态
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        String actions = (String)fields.get("actions", null);

        init(actions);
    }

    static class Authority {
        HostPortrange p;

        Authority(String scheme, String authority) {
            int at = authority.indexOf('@');
            if (at == -1) {
                    p = new HostPortrange(scheme, authority);
            } else {
                    p = new HostPortrange(scheme, authority.substring(at+1));
            }
        }

        boolean implies(Authority other) {
            return impliesHostrange(other) && impliesPortrange(other);
        }

        private boolean impliesHostrange(Authority that) {
            String thishost = this.p.hostname();
            String thathost = that.p.hostname();

            if (p.wildcard() && thishost.equals("")) {
                // 此 "*" 暗示所有其他
                return true;
            }
            if (that.p.wildcard() && thathost.equals("")) {
                // 那个 "*" 只能被此 "*" 暗示
                return false;
            }
            if (thishost.equals(thathost)) {
                // 覆盖所有字面量IP地址和固定域名的情况
                return true;
            }
            if (this.p.wildcard()) {
                // 此 "*.foo.com" 暗示 "bub.bar.foo.com"
                return thathost.endsWith(thishost);
            }
            return false;
        }

        private boolean impliesPortrange(Authority that) {
            int[] thisrange = this.p.portrange();
            int[] thatrange = that.p.portrange();
            if (thisrange[0] == -1) {
                /* 非http/s URL未指定端口 */
                return true;
            }
            return thisrange[0] <= thatrange[0] &&
                        thisrange[1] >= thatrange[1];
        }

        boolean equals(Authority that) {
            return this.p.equals(that.p);
        }

        public int hashCode() {
            return p.hashCode();
        }
    }
}
