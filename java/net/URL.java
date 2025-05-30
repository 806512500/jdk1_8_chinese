
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
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.ObjectInputStream.GetField;
import java.util.Hashtable;
import java.util.StringTokenizer;
import sun.misc.VM;
import sun.net.util.IPAddressUtil;
import sun.security.util.SecurityConstants;

/**
 * 类 {@code URL} 表示统一资源定位符，指向万维网上的“资源”。资源可以是像文件或目录这样简单的东西，也可以是更复杂对象的引用，例如数据库查询或搜索引擎的引用。更多关于 URL 类型及其格式的信息可以在以下链接中找到：
 * <a href=
 * "http://web.archive.org/web/20051219043731/http://archive.ncsa.uiuc.edu/SDG/Software/Mosaic/Demo/url-primer.html">
 * <i>URL 类型</i></a>
 * <p>
 * 通常，URL 可以分为几个部分。考虑以下示例：
 * <blockquote><pre>
 *     http://www.example.com/docs/resource1.html
 * </pre></blockquote>
 * <p>
 * 上述 URL 表示使用的协议是 {@code http}（超文本传输协议），信息位于名为
 * {@code www.example.com} 的主机上。该主机上的信息名为 {@code /docs/resource1.html}。该名称在主机上的确切含义既依赖于协议也依赖于主机。信息通常存储在文件中，但也可能是动态生成的。URL 的这一部分称为 <i>路径</i> 组件。
 * <p>
 * URL 可以选择性地指定一个“端口”，这是在远程主机上建立 TCP 连接的端口号。如果未指定端口，则使用该协议的默认端口。例如，{@code http} 的默认端口是 {@code 80}。可以指定替代端口，例如：
 * <blockquote><pre>
 *     http://www.example.com:1080/docs/resource1.html
 * </pre></blockquote>
 * <p>
 * {@code URL} 的语法由 <a
 * href="http://www.ietf.org/rfc/rfc2396.txt"><i>RFC&nbsp;2396: 统一资源标识符 (URI)：通用语法</i></a> 定义，并由 <a
 * href="http://www.ietf.org/rfc/rfc2732.txt"><i>RFC&nbsp;2732: URL 中的 IPv6 地址的文本格式</i></a> 修正。文本 IPv6 地址格式还支持 scope_ids。scope_ids 的语法和用法在此处描述
 * <a href="Inet6Address.html#scoped">这里</a>。
 * <p>
 * URL 可能附加了一个“片段”，也称为“ref”或“参考”。片段由井号字符 "#" 后跟更多字符表示。例如：
 * <blockquote><pre>
 *     http://java.sun.com/index.html#chapter1
 * </pre></blockquote>
 * <p>
 * 这个片段不是 URL 的技术部分。相反，它表示在检索指定资源后，应用程序特别感兴趣的是文档中带有 {@code chapter1} 标签的部分。标签的含义是资源特定的。
 * <p>
 * 应用程序还可以指定一个“相对 URL”，其中仅包含相对于另一个 URL 到达资源所需的信息。相对 URL 在 HTML 页面中经常使用。例如，如果 URL：
 * <blockquote><pre>
 *     http://java.sun.com/index.html
 * </pre></blockquote>
 * 包含相对 URL：
 * <blockquote><pre>
 *     FAQ.html
 * </pre></blockquote>
 * 它将是以下 URL 的简写：
 * <blockquote><pre>
 *     http://java.sun.com/FAQ.html
 * </pre></blockquote>
 * <p>
 * 相对 URL 不必指定 URL 的所有组件。如果未指定协议、主机名或端口号，则从完全指定的 URL 继承这些值。文件组件必须指定。可选的片段不会继承。
 * <p>
 * URL 类本身不会根据 RFC2396 中定义的转义机制对任何 URL 组件进行编码或解码。调用者有责任在调用 URL 之前对需要转义的字段进行编码，并解码 URL 返回的任何转义字段。此外，因为 URL 不了解 URL 转义，所以它不会识别编码形式和解码形式的相同 URL 之间的等效性。例如，以下两个 URL：<br>
 * <pre>    http://foo.com/hello world/ 和 http://foo.com/hello%20world</pre>
 * 将被视为不相等。
 * <p>
 * 注意，{@link java.net.URI} 类在某些情况下会对其组件字段进行转义。管理 URL 编码和解码的推荐方法是使用 {@link java.net.URI}，并使用 {@link #toURI()} 和
 * {@link URI#toURL()} 在这两个类之间进行转换。
 * <p>
 * 也可以使用 {@link URLEncoder} 和 {@link URLDecoder} 类，但仅限于 HTML 表单编码，这与 RFC2396 中定义的编码方案不同。
 *
 * @author  James Gosling
 * @since JDK1.0
 */
public final class URL implements java.io.Serializable {

    static final String BUILTIN_HANDLERS_PREFIX = "sun.net.www.protocol";
    static final long serialVersionUID = -7627629688361524110L;

    /**
     * 指定要扫描以查找协议处理程序的包前缀列表的属性。此属性（如果有）的值应该是以竖线分隔的包名称列表，用于查找要加载的协议处理程序。此类的策略是所有协议处理程序都将在名为 <protocolname>.Handler 的类中，列表中的每个包依次检查匹配的处理程序。如果未找到匹配项（或未指定属性），则使用默认包前缀 sun.net.www.protocol。搜索从列表中的第一个包开始，到最后一个包结束，并在找到匹配项时停止。
     */
    private static final String protocolPathProp = "java.protocol.handler.pkgs";

    /**
     * 要使用的协议（ftp、http、nntp 等）。
     * @serial
     */
    private String protocol;

    /**
     * 要连接的主机名。
     * @serial
     */
    private String host;

    /**
     * 要连接的协议端口。
     * @serial
     */
    private int port = -1;

    /**
     * 该主机上的指定文件名。{@code file} 定义为 {@code path[?query]}
     * @serial
     */
    private String file;

    /**
     * 该 URL 的查询部分。
     */
    private transient String query;

    /**
     * 该 URL 的权威部分。
     * @serial
     */
    private String authority;

    /**
     * 该 URL 的路径部分。
     */
    private transient String path;

    /**
     * 该 URL 的用户信息部分。
     */
    private transient String userInfo;

    /**
     * # 引用。
     * @serial
     */
    private String ref;

    /**
     * 用于 equals 和 hashCode 的主机 IP 地址。未初始化或未知的主机地址为 null。
     */
    private transient InetAddress hostAddress;

    /**
     * 该 URL 的 URLStreamHandler。
     */
    transient URLStreamHandler handler;

    /* 我们的哈希码。
     * @serial
     */
    private int hashCode = -1;

    private transient UrlDeserializedState tempState;

    /**
     * 从指定的 {@code protocol}、{@code host}、{@code port}
     * 编号和 {@code file} 创建一个 {@code URL} 对象。<p>
     *
     * {@code host} 可以表示为主机名或字面 IP 地址。如果使用 IPv6 字面地址，则应将其括在方括号（{@code '['} 和 {@code ']'}）中，如 <a
     * href="http://www.ietf.org/rfc/rfc2732.txt">RFC&nbsp;2732</a> 所指定；但是，<a
     * href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC&nbsp;2373: IP
     * 版本 6 地址架构</i></a> 中定义的字面 IPv6 地址格式也被接受。<p>
     *
     * 指定 {@code port} 编号为 {@code -1}
     * 表示 URL 应使用协议的默认端口。<p>
     *
     * 如果这是使用指定协议创建的第一个 URL 对象，则为该协议创建一个 <i>流协议处理程序</i> 对象，即 {@code URLStreamHandler} 类的实例：
     * <ol>
     * <li>如果应用程序已设置了一个 {@code URLStreamHandlerFactory} 实例作为流处理程序工厂，
     *     则调用该实例的 {@code createURLStreamHandler} 方法，以协议字符串作为参数创建流协议处理程序。
     * <li>如果尚未设置 {@code URLStreamHandlerFactory}，
     *     或者工厂的 {@code createURLStreamHandler} 方法返回 {@code null}，则构造函数查找系统属性：
     *     <blockquote><pre>
     *         java.protocol.handler.pkgs
     *     </pre></blockquote>
     *     如果该系统属性的值不为 {@code null}，
     *     则将其解释为以竖线字符 '{@code |}' 分隔的包列表。构造函数尝试加载名为：
     *     <blockquote><pre>
     *         &lt;<i>package</i>&gt;.&lt;<i>protocol</i>&gt;.Handler
     *     </pre></blockquote>
     *     的类，其中 &lt;<i>package</i>&gt; 替换为包名，&lt;<i>protocol</i>&gt; 替换为协议名。
     *     如果此类不存在，或者此类存在但不是 {@code URLStreamHandler} 的子类，则尝试列表中的下一个包。
     * <li>如果上一步未能找到协议处理程序，则构造函数尝试从系统默认包加载。
     *     <blockquote><pre>
     *         &lt;<i>system default package</i>&gt;.&lt;<i>protocol</i>&gt;.Handler
     *     </pre></blockquote>
     *     如果此类不存在，或者此类存在但不是 {@code URLStreamHandler} 的子类，则抛出
     *     {@code MalformedURLException}。
     * </ol>
     *
     * <p>以下协议的协议处理程序保证在搜索路径上存在：
     * <blockquote><pre>
     *     http, https, file, 和 jar
     * </pre></blockquote>
     * 可能还存在其他协议的协议处理程序。
     *
     * <p>此构造函数不验证输入。
     *
     * @param      protocol   要使用的协议名。
     * @param      host       主机名。
     * @param      port       主机上的端口号。
     * @param      file       主机上的文件
     * @exception  MalformedURLException  如果指定了未知协议。
     * @see        java.lang.System#getProperty(java.lang.String)
     * @see        java.net.URL#setURLStreamHandlerFactory(
     *                  java.net.URLStreamHandlerFactory)
     * @see        java.net.URLStreamHandler
     * @see        java.net.URLStreamHandlerFactory#createURLStreamHandler(
     *                  java.lang.String)
     */
    public URL(String protocol, String host, int port, String file)
        throws MalformedURLException
    {
        this(protocol, host, port, file, null);
    }

    /**
     * 从指定的 {@code protocol}
     * 名称、{@code host} 名称和 {@code file} 名称创建一个 URL。使用指定协议的默认端口。
     * <p>
     * 此方法等同于调用四参数构造函数，参数为 {@code protocol}、
     * {@code host}、{@code -1} 和 {@code file}。
     *
     * 此构造函数不验证输入。
     *
     * @param      protocol   要使用的协议名。
     * @param      host       主机名。
     * @param      file       主机上的文件。
     * @exception  MalformedURLException  如果指定了未知协议。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *                  int, java.lang.String)
     */
    public URL(String protocol, String host, String file)
            throws MalformedURLException {
        this(protocol, host, -1, file);
    }

    /**
     * 从指定的 {@code protocol}、{@code host}、{@code port}
     * 编号、{@code file} 和 {@code handler} 创建一个 {@code URL} 对象。指定
     * {@code port} 编号为 {@code -1} 表示
     * URL 应使用协议的默认端口。指定
     * {@code handler} 为 {@code null} 表示 URL
     * 应使用协议的默认流处理程序，如以下方法所述：
     *     java.net.URL#URL(java.lang.String, java.lang.String, int,
     *                      java.lang.String)
     *
     * <p>如果处理程序不为 null 且存在安全管理器，
     * 则调用安全管理器的 {@code checkPermission}
     * 方法，参数为
     * {@code NetPermission("specifyStreamHandler")} 权限。
     * 这可能导致 SecurityException。
     *
     * 此构造函数不验证输入。
     *
     * @param      protocol   要使用的协议名。
     * @param      host       主机名。
     * @param      port       主机上的端口号。
     * @param      file       主机上的文件
     * @param      handler    URL 的流处理程序。
     * @exception  MalformedURLException  如果指定了未知协议。
     * @exception  SecurityException
     *        如果存在安全管理器且其
     *        {@code checkPermission} 方法不允许
     *        显式指定流处理程序。
     * @see        java.lang.System#getProperty(java.lang.String)
     * @see        java.net.URL#setURLStreamHandlerFactory(
     *                  java.net.URLStreamHandlerFactory)
     * @see        java.net.URLStreamHandler
     * @see        java.net.URLStreamHandlerFactory#createURLStreamHandler(
     *                  java.lang.String)
     * @see        SecurityManager#checkPermission
     * @see        java.net.NetPermission
     */
    public URL(String protocol, String host, int port, String file,
               URLStreamHandler handler) throws MalformedURLException {
        if (handler != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // 检查指定处理程序的权限
                checkSpecifyHandler(sm);
            }
        }


                    protocol = protocol.toLowerCase();
        this.protocol = protocol;
        if (host != null) {

            /**
             * 如果主机是IPv6地址的字面量，
             * 我们将使其符合RFC 2732
             */
            if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
                host = "["+host+"]";
            }
            this.host = host;

            if (port < -1) {
                throw new MalformedURLException("无效的端口号 :" +
                                                    port);
            }
            this.port = port;
            authority = (port == -1) ? host : host + ":" + port;
        }

        Parts parts = new Parts(file);
        path = parts.getPath();
        query = parts.getQuery();

        if (query != null) {
            this.file = path + "?" + query;
        } else {
            this.file = path;
        }
        ref = parts.getRef();

        // 注意：我们在这里不进行URL的完整验证。现在改变风险太大，但值得在未来考虑。-br
        if (handler == null &&
            (handler = getURLStreamHandler(protocol)) == null) {
            throw new MalformedURLException("未知协议: " + protocol);
        }
        this.handler = handler;
        if (host != null && isBuiltinStreamHandler(handler)) {
            String s = IPAddressUtil.checkExternalForm(this);
            if (s != null) {
                throw new MalformedURLException(s);
            }
        }
        if ("jar".equalsIgnoreCase(protocol)) {
            if (handler instanceof sun.net.www.protocol.jar.Handler) {
                // URL.openConnection() 会抛出一个令人困惑的异常
                // 因此在这里生成一个更好的异常。
                String s = ((sun.net.www.protocol.jar.Handler) handler).checkNestedProtocol(file);
                if (s != null) {
                    throw new MalformedURLException(s);
                }
            }
        }
    }

    /**
     * 从字符串表示创建一个 {@code URL} 对象。
     * <p>
     * 此构造函数等效于带有 {@code null} 第一个参数的双参数构造函数。
     *
     * @param      spec   要解析为URL的字符串。
     * @exception  MalformedURLException  如果未指定协议，或找到未知协议，或 {@code spec} 为 {@code null}。
     * @see        java.net.URL#URL(java.net.URL, java.lang.String)
     */
    public URL(String spec) throws MalformedURLException {
        this(null, spec);
    }

    /**
     * 在指定上下文中解析给定的spec来创建URL。
     *
     * 新的URL是根据给定的上下文URL和spec参数创建的，如
     * RFC2396 &quot;Uniform Resource Identifiers : Generic * Syntax&quot; 所述：
     * <blockquote><pre>
     *          &lt;scheme&gt;://&lt;authority&gt;&lt;path&gt;?&lt;query&gt;#&lt;fragment&gt;
     * </pre></blockquote>
     * 引用被解析为方案、权限、路径、查询和片段部分。如果路径组件为空且方案、权限和查询组件未定义，则新的URL是对当前文档的引用。否则，spec中的片段和查询部分将用于新的URL。
     * <p>
     * 如果spec中定义了方案组件且与上下文的方案不匹配，则新的URL将基于spec单独创建为绝对URL。否则，方案组件将从上下文URL继承。
     * <p>
     * 如果spec中存在权限组件，则spec被视为绝对路径，spec的权限和路径将替换上下文的权限和路径。如果spec中缺少权限组件，则新URL的权限将从上下文继承。
     * <p>
     * 如果spec的路径组件以斜杠字符 &quot;/&quot; 开头，则路径被视为绝对路径，spec路径将替换上下文路径。
     * <p>
     * 否则，路径被视为相对路径并附加到上下文路径，如RFC2396所述。此外，在这种情况下，
     * 通过删除 &quot;..&quot; 和 &quot;.&quot; 发生的目录更改来规范化路径。
     * <p>
     * 有关URL解析的更详细描述，请参阅RFC2396。
     *
     * @param      context   解析规范的上下文。
     * @param      spec      要解析为URL的字符串。
     * @exception  MalformedURLException  如果未指定协议，或找到未知协议，或 {@code spec} 为 {@code null}。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *                  int, java.lang.String)
     * @see        java.net.URLStreamHandler
     * @see        java.net.URLStreamHandler#parseURL(java.net.URL,
     *                  java.lang.String, int, int)
     */
    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, null);
    }

    /**
     * 在指定上下文中使用指定的处理程序解析给定的spec来创建URL。如果处理程序为null，则解析过程与双参数构造函数相同。
     *
     * @param      context   解析规范的上下文。
     * @param      spec      要解析为URL的字符串。
     * @param      handler   URL的流处理程序。
     * @exception  MalformedURLException  如果未指定协议，或找到未知协议，或 {@code spec} 为 {@code null}。
     * @exception  SecurityException
     *        如果存在安全管理器且其
     *        {@code checkPermission} 方法不允许
     *        指定流处理程序。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *                  int, java.lang.String)
     * @see        java.net.URLStreamHandler
     * @see        java.net.URLStreamHandler#parseURL(java.net.URL,
     *                  java.lang.String, int, int)
     */
    public URL(URL context, String spec, URLStreamHandler handler)
        throws MalformedURLException
    {
        String original = spec;
        int i, limit, c;
        int start = 0;
        String newProtocol = null;
        boolean aRef=false;
        boolean isRelative = false;

        // 检查指定处理程序的权限
        if (handler != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                checkSpecifyHandler(sm);
            }
        }

        try {
            limit = spec.length();
            while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
                limit--;        // 消除尾部空白
            }
            while ((start < limit) && (spec.charAt(start) <= ' ')) {
                start++;        // 消除前导空白
            }

            if (spec.regionMatches(true, start, "url:", 0, 4)) {
                start += 4;
            }
            if (start < spec.length() && spec.charAt(start) == '#') {
                /* 假设这是相对于上下文URL的引用。
                 * 这意味着协议不能以 '#' 开头，但我们必须解析
                 * 像 "hello:there" 这样的引用URL，其中包含 ':'。
                 */
                aRef=true;
            }
            for (i = start ; !aRef && (i < limit) &&
                     ((c = spec.charAt(i)) != '/') ; i++) {
                if (c == ':') {

                    String s = spec.substring(start, i).toLowerCase();
                    if (isValidProtocol(s)) {
                        newProtocol = s;
                        start = i + 1;
                    }
                    break;
                }
            }

            // 只有在协议匹配时才使用上下文。
            protocol = newProtocol;
            if ((context != null) && ((newProtocol == null) ||
                            newProtocol.equalsIgnoreCase(context.protocol))) {
                // 如果未指定给构造函数，则从上下文继承协议处理程序
                if (handler == null) {
                    handler = context.handler;
                }

                // 如果上下文是分层URL方案且spec包含匹配的方案，则保持向后兼容性
                // 并将其视为spec不包含方案；参见RFC2396 5.2.3
                if (context.path != null && context.path.startsWith("/"))
                    newProtocol = null;

                if (newProtocol == null) {
                    protocol = context.protocol;
                    authority = context.authority;
                    userInfo = context.userInfo;
                    host = context.host;
                    port = context.port;
                    file = context.file;
                    path = context.path;
                    isRelative = true;
                }
            }

            if (protocol == null) {
                throw new MalformedURLException("没有协议: "+original);
            }

            // 如果未指定或上下文的协议不能使用，则获取协议处理程序
            if (handler == null &&
                (handler = getURLStreamHandler(protocol)) == null) {
                throw new MalformedURLException("未知协议: "+protocol);
            }

            this.handler = handler;

            i = spec.indexOf('#', start);
            if (i >= 0) {
                ref = spec.substring(i + 1, limit);
                limit = i;
            }

            /*
             * 处理RFC2396第5.2.2节隐含的查询和片段的特殊继承情况。
             */
            if (isRelative && start == limit) {
                query = context.query;
                if (ref == null) {
                    ref = context.ref;
                }
            }

            handler.parseURL(this, spec, start, limit);

        } catch(MalformedURLException e) {
            throw e;
        } catch(Exception e) {
            MalformedURLException exception = new MalformedURLException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /*
     * 如果指定的字符串是有效的协议名称，则返回true。
     */
    private boolean isValidProtocol(String protocol) {
        int len = protocol.length();
        if (len < 1)
            return false;
        char c = protocol.charAt(0);
        if (!Character.isLetter(c))
            return false;
        for (int i = 1; i < len; i++) {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' &&
                c != '-') {
                return false;
            }
        }
        return true;
    }

    /*
     * 检查指定流处理程序的权限。
     */
    private void checkSpecifyHandler(SecurityManager sm) {
        sm.checkPermission(SecurityConstants.SPECIFY_HANDLER_PERMISSION);
    }

    /**
     * 设置URL的字段。这不是公共方法，因此只有URLStreamHandlers可以修改URL字段。URL在其他方面是常量。
     *
     * @param protocol 要使用的协议名称
     * @param host 主机名称
     * @param port 主机上的端口号
     * @param file 主机上的文件
     * @param ref URL中的内部引用
     */
    void set(String protocol, String host, int port,
             String file, String ref) {
        synchronized (this) {
            this.protocol = protocol;
            this.host = host;
            authority = port == -1 ? host : host + ":" + port;
            this.port = port;
            this.file = file;
            this.ref = ref;
            /* 这非常重要。必须在URL更改后重新计算。 */
            hashCode = -1;
            hostAddress = null;
            int q = file.lastIndexOf('?');
            if (q != -1) {
                query = file.substring(q+1);
                path = file.substring(0, q);
            } else
                path = file;
        }
    }

    /**
     * 设置URL的8个指定字段。这不是公共方法，因此只有URLStreamHandlers可以修改URL字段。URL在其他方面是常量。
     *
     * @param protocol 要使用的协议名称
     * @param host 主机名称
     * @param port 主机上的端口号
     * @param authority URL的权限部分
     * @param userInfo 用户名和密码
     * @param path 主机上的文件
     * @param ref URL中的内部引用
     * @param query 本URL的查询部分
     * @since 1.3
     */
    void set(String protocol, String host, int port,
             String authority, String userInfo, String path,
             String query, String ref) {
        synchronized (this) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.file = query == null ? path : path + "?" + query;
            this.userInfo = userInfo;
            this.path = path;
            this.ref = ref;
            /* 这非常重要。必须在URL更改后重新计算。 */
            hashCode = -1;
            hostAddress = null;
            this.query = query;
            this.authority = authority;
        }
    }

    /**
     * 返回此URL表示的主机地址。
     * 获取主机地址时发生 {@link SecurityException} 或 {@link UnknownHostException} 将导致此方法返回 {@code null}
     *
     * @return 一个表示主机的 {@link InetAddress}
     */
    synchronized InetAddress getHostAddress() {
        if (hostAddress != null) {
            return hostAddress;
        }

        if (host == null || host.isEmpty()) {
            return null;
        }
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException | SecurityException ex) {
            return null;
        }
        return hostAddress;
    }


    /**
     * 获取此 {@code URL} 的查询部分。
     *
     * @return  此 {@code URL} 的查询部分，
     * 或 <CODE>null</CODE> 如果不存在
     * @since 1.3
     */
    public String getQuery() {
        return query;
    }

    /**
     * 获取此 {@code URL} 的路径部分。
     *
     * @return  此 {@code URL} 的路径部分，或一个
     * 空字符串 如果不存在
     * @since 1.3
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取此 {@code URL} 的userInfo部分。
     *
     * @return  此 {@code URL} 的userInfo部分，或
     * <CODE>null</CODE> 如果不存在
     * @since 1.3
     */
    public String getUserInfo() {
        return userInfo;
    }


                /**
     * 获取此 {@code URL} 的权限部分。
     *
     * @return  此 {@code URL} 的权限部分
     * @since 1.3
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * 获取此 {@code URL} 的端口号。
     *
     * @return  端口号，如果未设置端口则返回 -1
     */
    public int getPort() {
        return port;
    }

    /**
     * 获取与此 {@code URL} 关联的协议的默认端口号。如果 URL 方案或 URLStreamHandler
     * 未定义默认端口号，则返回 -1。
     *
     * @return  端口号
     * @since 1.4
     */
    public int getDefaultPort() {
        return handler.getDefaultPort();
    }

    /**
     * 获取此 {@code URL} 的协议名称。
     *
     * @return  此 {@code URL} 的协议名称。
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 获取此 {@code URL} 的主机名（如果适用）。主机格式符合 RFC 2732，即对于
     * 字面 IPv6 地址，此方法将返回包含在方括号（{@code '['} 和 {@code ']'}）中的 IPv6 地址。
     *
     * @return  此 {@code URL} 的主机名。
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取此 {@code URL} 的文件名。返回的文件部分将与 <CODE>getPath()</CODE> 相同，
     * 并附加 <CODE>getQuery()</CODE> 的值（如果有）。如果没有查询部分，此方法和 <CODE>getPath()</CODE>
     * 将返回相同的结果。
     *
     * @return  此 {@code URL} 的文件名，
     * 或者如果不存在则返回空字符串
     */
    public String getFile() {
        return file;
    }

    /**
     * 获取此 {@code URL} 的锚点（也称为“参考”）。
     *
     * @return  此 {@code URL} 的锚点（也称为“参考”），如果不存在则返回 <CODE>null</CODE>
     */
    public String getRef() {
        return ref;
    }

    /**
     * 比较此 URL 与另一个对象是否相等。<p>
     *
     * 如果给定的对象不是 URL，则此方法立即返回 {@code false}。<p>
     *
     * 两个 URL 对象相等，如果它们具有相同的协议、等效的主机、相同的主机端口号、相同的文件和文件片段。<p>
     *
     * 两个主机被认为是等效的，如果两个主机名可以解析为相同的 IP 地址；否则，如果任何一个主机名无法解析，
     * 主机名必须在不区分大小写的情况下相等；或者两个主机名都为 null。<p>
     *
     * 由于主机比较需要名称解析，此操作是一个阻塞操作。 <p>
     *
     * 注意：定义的 {@code equals} 行为已知与 HTTP 中的虚拟主机不一致。
     *
     * @param   obj   要比较的 URL。
     * @return  {@code true} 如果对象相同；
     *          {@code false} 否则。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof URL))
            return false;
        URL u2 = (URL)obj;

        return handler.equals(this, u2);
    }

    /**
     * 创建一个适合哈希表索引的整数。<p>
     *
     * 哈希码基于所有与 URL 比较相关的 URL 组件。因此，此操作是一个阻塞操作。<p>
     *
     * @return  此 {@code URL} 的哈希码。
     */
    public synchronized int hashCode() {
        if (hashCode != -1)
            return hashCode;

        hashCode = handler.hashCode(this);
        return hashCode;
    }

    /**
     * 比较两个 URL，不包括片段组件。<p>
     *
     * 如果此 {@code URL} 和 {@code other} 参数在不考虑片段组件的情况下相等，则返回 {@code true}。
     *
     * @param   other   要比较的 {@code URL}。
     * @return  {@code true} 如果它们引用相同的远程对象；
     *          {@code false} 否则。
     */
    public boolean sameFile(URL other) {
        return handler.sameFile(this, other);
    }

    /**
     * 构造此 {@code URL} 的字符串表示形式。字符串通过调用此对象的流协议处理程序的 {@code toExternalForm}
     * 方法创建。
     *
     * @return  此对象的字符串表示形式。
     * @see     java.net.URL#URL(java.lang.String, java.lang.String, int,
     *                  java.lang.String)
     * @see     java.net.URLStreamHandler#toExternalForm(java.net.URL)
     */
    public String toString() {
        return toExternalForm();
    }

    /**
     * 构造此 {@code URL} 的字符串表示形式。字符串通过调用此对象的流协议处理程序的 {@code toExternalForm}
     * 方法创建。
     *
     * @return  此对象的字符串表示形式。
     * @see     java.net.URL#URL(java.lang.String, java.lang.String,
     *                  int, java.lang.String)
     * @see     java.net.URLStreamHandler#toExternalForm(java.net.URL)
     */
    public String toExternalForm() {
        return handler.toExternalForm(this);
    }

    /**
     * 返回与此 URL 等效的 {@link java.net.URI}。此方法的工作方式与 {@code new URI (this.toString())} 相同。
     * <p>注意，任何符合 RFC 2396 的 URL 实例都可以转换为 URI。然而，一些不符合严格规范的 URL 无法转换为 URI。
     *
     * @exception URISyntaxException 如果此 URL 的格式不符合 RFC2396 且无法转换为 URI。
     *
     * @return    与此 URL 等效的 URI 实例。
     * @since 1.5
     */
    public URI toURI() throws URISyntaxException {
        URI uri = new URI(toString());
        if (authority != null && isBuiltinStreamHandler(handler)) {
            String s = IPAddressUtil.checkAuthority(this);
            if (s != null) throw new URISyntaxException(authority, s);
        }
        return uri;
    }

    /**
     * 返回一个表示与此 {@code URL} 引用的远程对象的连接的 {@link java.net.URLConnection} 实例。
     *
     * <P>每次调用协议处理程序的 {@linkplain java.net.URLStreamHandler#openConnection(URL)
     * URLStreamHandler.openConnection(URL)} 方法时，都会创建一个新的 {@linkplain java.net.URLConnection URLConnection} 实例。</P>
     *
     * <P>需要注意的是，URLConnection 实例在创建时不会建立实际的网络连接。这将在调用 {@linkplain java.net.URLConnection#connect() URLConnection.connect()} 时发生。</P>
     *
     * <P>如果 URL 的协议（如 HTTP 或 JAR）存在一个属于以下包或其子包的公共、专用的 URLConnection 子类：
     * java.lang, java.io, java.util, java.net，则返回的连接将是该子类。例如，对于 HTTP 将返回一个
     * HttpURLConnection，对于 JAR 将返回一个 JarURLConnection。</P>
     *
     * @return     一个链接到 URL 的 {@link java.net.URLConnection}。
     * @exception  IOException  如果发生 I/O 异常。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *             int, java.lang.String)
     */
    public URLConnection openConnection() throws java.io.IOException {
        return handler.openConnection(this);
    }

    /**
     * 与 {@link #openConnection()} 相同，但连接将通过指定的代理进行；不支持代理的协议处理程序将忽略代理参数并建立正常连接。
     *
     * 调用此方法将抢占系统的默认 ProxySelector 设置。
     *
     * @param      proxy  通过此代理进行连接。如果需要直接连接，
     *             应指定 Proxy.NO_PROXY。
     * @return     一个 {@code URLConnection} 到 URL。
     * @exception  IOException  如果发生 I/O 异常。
     * @exception  SecurityException 如果存在安全管理者且调用者没有权限连接
     *             到代理。
     * @exception  IllegalArgumentException 如果代理为 null，
     *             或代理类型错误
     * @exception  UnsupportedOperationException 如果实现协议处理程序的子类
     *             不支持此方法。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *             int, java.lang.String)
     * @see        java.net.URLConnection
     * @see        java.net.URLStreamHandler#openConnection(java.net.URL,
     *             java.net.Proxy)
     * @since      1.5
     */
    public URLConnection openConnection(Proxy proxy)
        throws java.io.IOException {
        if (proxy == null) {
            throw new IllegalArgumentException("proxy can not be null");
        }

        // 作为安全措施创建代理的副本
        Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY : sun.net.ApplicationProxy.create(proxy);
        SecurityManager sm = System.getSecurityManager();
        if (p.type() != Proxy.Type.DIRECT && sm != null) {
            InetSocketAddress epoint = (InetSocketAddress) p.address();
            if (epoint.isUnresolved())
                sm.checkConnect(epoint.getHostName(), epoint.getPort());
            else
                sm.checkConnect(epoint.getAddress().getHostAddress(),
                                epoint.getPort());
        }
        return handler.openConnection(this, p);
    }

    /**
     * 打开与此 {@code URL} 的连接并返回一个用于从此连接读取的 {@code InputStream}。此方法是以下代码的简写：
     * <blockquote><pre>
     *     openConnection().getInputStream()
     * </pre></blockquote>
     *
     * @return     用于从 URL 连接读取的输入流。
     * @exception  IOException  如果发生 I/O 异常。
     * @see        java.net.URL#openConnection()
     * @see        java.net.URLConnection#getInputStream()
     */
    public final InputStream openStream() throws java.io.IOException {
        return openConnection().getInputStream();
    }

    /**
     * 获取此 URL 的内容。此方法是以下代码的简写：
     * <blockquote><pre>
     *     openConnection().getContent()
     * </pre></blockquote>
     *
     * @return     此 URL 的内容。
     * @exception  IOException  如果发生 I/O 异常。
     * @see        java.net.URLConnection#getContent()
     */
    public final Object getContent() throws java.io.IOException {
        return openConnection().getContent();
    }

    /**
     * 获取此 URL 的内容。此方法是以下代码的简写：
     * <blockquote><pre>
     *     openConnection().getContent(Class[])
     * </pre></blockquote>
     *
     * @param classes 一个 Java 类型数组
     * @return     此 URL 的内容对象，该对象是 classes 数组中指定类型的第一个匹配项。
     *               如果没有请求的类型支持，则返回 null。
     * @exception  IOException  如果发生 I/O 异常。
     * @see        java.net.URLConnection#getContent(Class[])
     * @since 1.3
     */
    public final Object getContent(Class[] classes)
    throws java.io.IOException {
        return openConnection().getContent(classes);
    }

    /**
     * URLStreamHandler 工厂。
     */
    static URLStreamHandlerFactory factory;

    /**
     * 设置应用程序的 {@code URLStreamHandlerFactory}。此方法在一个给定的 Java 虚拟机中最多只能调用一次。
     *
     *<p> {@code URLStreamHandlerFactory} 实例用于从协议名称构造流协议处理程序。
     *
     * <p> 如果存在安全管理者，此方法首先调用安全管理者的 {@code checkSetFactory} 方法
     * 以确保操作被允许。这可能导致 SecurityException。
     *
     * @param      fac   所需的工厂。
     * @exception  Error  如果应用程序已设置工厂。
     * @exception  SecurityException  如果存在安全管理者且其
     *             {@code checkSetFactory} 方法不允许
     *             操作。
     * @see        java.net.URL#URL(java.lang.String, java.lang.String,
     *             int, java.lang.String)
     * @see        java.net.URLStreamHandlerFactory
     * @see        SecurityManager#checkSetFactory
     */
    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
        synchronized (streamHandlerLock) {
            if (factory != null) {
                throw new Error("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            handlers.clear();
            factory = fac;
        }
    }

    /**
     * 协议处理程序表。
     */
    static Hashtable<String,URLStreamHandler> handlers = new Hashtable<>();
    private static Object streamHandlerLock = new Object();

    /**
     * 返回流处理程序。
     * @param protocol 要使用的协议
     */
    static URLStreamHandler getURLStreamHandler(String protocol) {

        URLStreamHandler handler = handlers.get(protocol);
        if (handler == null) {

            boolean checkedWithFactory = false;

            // 使用工厂（如果有）
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
                checkedWithFactory = true;
            }

            // 尝试 Java 协议处理程序
            if (handler == null) {
                String packagePrefixList = null;

                packagePrefixList
                    = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction(
                        protocolPathProp,""));
                if (packagePrefixList != "") {
                    packagePrefixList += "|";
                }

                // REMIND: 决定是否允许“null”类前缀
                // 或不。
                packagePrefixList += "sun.net.www.protocol";

                StringTokenizer packagePrefixIter =
                    new StringTokenizer(packagePrefixList, "|");

                while (handler == null &&
                       packagePrefixIter.hasMoreTokens()) {

                    String packagePrefix =
                      packagePrefixIter.nextToken().trim();
                    try {
                        String clsName = packagePrefix + "." + protocol +
                          ".Handler";
                        Class<?> cls = null;
                        try {
                            cls = Class.forName(clsName);
                        } catch (ClassNotFoundException e) {
                            ClassLoader cl = ClassLoader.getSystemClassLoader();
                            if (cl != null) {
                                cls = cl.loadClass(clsName);
                            }
                        }
                        if (cls != null) {
                            handler  =
                              (URLStreamHandler)cls.newInstance();
                        }
                    } catch (Exception e) {
                        // 任何数量的异常都可能在此处抛出
                    }
                }
            }


                        synchronized (streamHandlerLock) {

                URLStreamHandler handler2 = null;

                // 再次检查哈希表，以防另一个线程在我们上次检查后创建了处理程序
                handler2 = handlers.get(protocol);

                if (handler2 != null) {
                    return handler2;
                }

                // 检查工厂是否在我们上次检查后设置了工厂
                if (!checkedWithFactory && factory != null) {
                    handler2 = factory.createURLStreamHandler(protocol);
                }

                if (handler2 != null) {
                    // 工厂提供的处理程序必须具有更高的优先级。丢弃此线程创建的默认处理程序。
                    handler = handler2;
                }

                // 将此处理程序插入哈希表
                if (handler != null) {
                    handlers.put(protocol, handler);
                }

            }
        }

        return handler;

    }

    /**
     * @serialField    protocol String
     *
     * @serialField    host String
     *
     * @serialField    port int
     *
     * @serialField    authority String
     *
     * @serialField    file String
     *
     * @serialField    ref String
     *
     * @serialField    hashCode int
     *
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("protocol", String.class),
        new ObjectStreamField("host", String.class),
        new ObjectStreamField("port", int.class),
        new ObjectStreamField("authority", String.class),
        new ObjectStreamField("file", String.class),
        new ObjectStreamField("ref", String.class),
        new ObjectStreamField("hashCode", int.class), };

    /**
     * WriteObject 被调用来将 URL 的状态保存到 ObjectOutputStream。处理程序不会被保存，因为它特定于这个系统。
     *
     * @serialData 默认的写对象值。当读回时，读取者必须确保使用协议变量调用 getURLStreamHandler 返回一个有效的 URLStreamHandler，并在不返回时抛出 IOException。
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject(); // 写入字段
    }

    /**
     * readObject 被调用来从流中恢复 URL 的状态。它读取 URL 的组件并找到本地流处理程序。
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        GetField gf = s.readFields();
        String protocol = (String)gf.get("protocol", null);
        if (getURLStreamHandler(protocol) == null) {
            throw new IOException("未知协议: " + protocol);
        }
        String host = (String)gf.get("host", null);
        int port = gf.get("port", -1);
        String authority = (String)gf.get("authority", null);
        String file = (String)gf.get("file", null);
        String ref = (String)gf.get("ref", null);
        int hashCode = gf.get("hashCode", -1);
        if (authority == null
                && ((host != null && host.length() > 0) || port != -1)) {
            if (host == null)
                host = "";
            authority = (port == -1) ? host : host + ":" + port;
        }
        tempState = new UrlDeserializedState(protocol, host, port, authority,
               file, ref, hashCode);
    }

    /**
     * 用反序列化状态创建一个新的 URL 对象来替换反序列化的对象。
     *
     * @return 从反序列化状态创建的新对象。
     *
     * @throws ObjectStreamException 如果无法创建替换此对象的新对象
     */

   private Object readResolve() throws ObjectStreamException {

        URLStreamHandler handler = null;
        // 在 readObject 中已经检查过
        handler = getURLStreamHandler(tempState.getProtocol());

        URL replacementURL = null;
        if (isBuiltinStreamHandler(handler.getClass().getName())) {
            replacementURL = fabricateNewURL();
        } else {
            replacementURL = setDeserializedFields(handler);
        }
        return replacementURL;
    }

    private URL setDeserializedFields(URLStreamHandler handler) {
        URL replacementURL;
        String userInfo = null;
        String protocol = tempState.getProtocol();
        String host = tempState.getHost();
        int port = tempState.getPort();
        String authority = tempState.getAuthority();
        String file = tempState.getFile();
        String ref = tempState.getRef();
        int hashCode = tempState.getHashCode();


        // 构建 authority 部分
        if (authority == null
            && ((host != null && host.length() > 0) || port != -1)) {
            if (host == null)
                host = "";
            authority = (port == -1) ? host : host + ":" + port;

            // 处理包含 userInfo 的主机
            int at = host.lastIndexOf('@');
            if (at != -1) {
                userInfo = host.substring(0, at);
                host = host.substring(at+1);
            }
        } else if (authority != null) {
            // 构建 user info 部分
            int ind = authority.indexOf('@');
            if (ind != -1)
                userInfo = authority.substring(0, ind);
        }

        // 构建路径和查询部分
        String path = null;
        String query = null;
        if (file != null) {
            // 修复：仅在分层时执行此操作？
            int q = file.lastIndexOf('?');
            if (q != -1) {
                query = file.substring(q+1);
                path = file.substring(0, q);
            } else
                path = file;
        }

        // 设置对象字段。
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.file = file;
        this.authority = authority;
        this.ref = ref;
        this.hashCode = hashCode;
        this.handler = handler;
        this.query = query;
        this.path = path;
        this.userInfo = userInfo;
        replacementURL = this;
        return replacementURL;
    }

    private URL fabricateNewURL()
                throws InvalidObjectException {
        // 从反序列化对象创建 URL 字符串
        URL replacementURL = null;
        String urlString = tempState.reconstituteUrlString();

        try {
            replacementURL = new URL(urlString);
        } catch (MalformedURLException mEx) {
            resetState();
            InvalidObjectException invoEx = new InvalidObjectException(
                    "格式错误的 URL: " + urlString);
            invoEx.initCause(mEx);
            throw invoEx;
        }
        replacementURL.setSerializedHashCode(tempState.getHashCode());
        resetState();
        return replacementURL;
    }

    boolean isBuiltinStreamHandler(URLStreamHandler handler) {
        Class<?> handlerClass = handler.getClass();
        return isBuiltinStreamHandler(handlerClass.getName())
                || VM.isSystemDomainLoader(handlerClass.getClassLoader());
    }

    private boolean isBuiltinStreamHandler(String handlerClassName) {
        return (handlerClassName.startsWith(BUILTIN_HANDLERS_PREFIX));
    }

    private void resetState() {
        this.protocol = null;
        this.host = null;
        this.port = -1;
        this.file = null;
        this.authority = null;
        this.ref = null;
        this.hashCode = -1;
        this.handler = null;
        this.query = null;
        this.path = null;
        this.userInfo = null;
        this.tempState = null;
    }

    private void setSerializedHashCode(int hc) {
        this.hashCode = hc;
    }
}

class Parts {
    String path, query, ref;

    Parts(String file) {
        int ind = file.indexOf('#');
        ref = ind < 0 ? null: file.substring(ind + 1);
        file = ind < 0 ? file: file.substring(0, ind);
        int q = file.lastIndexOf('?');
        if (q != -1) {
            query = file.substring(q+1);
            path = file.substring(0, q);
        } else {
            path = file;
        }
    }

    String getPath() {
        return path;
    }

    String getQuery() {
        return query;
    }

    String getRef() {
        return ref;
    }
}

final class UrlDeserializedState {
    private final String protocol;
    private final String host;
    private final int port;
    private final String authority;
    private final String file;
    private final String ref;
    private final int hashCode;

    public UrlDeserializedState(String protocol,
                                String host, int port,
                                String authority, String file,
                                String ref, int hashCode) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.authority = authority;
        this.file = file;
        this.ref = ref;
        this.hashCode = hashCode;
    }

    String getProtocol() {
        return protocol;
    }

    String getHost() {
        return host;
    }

    String getAuthority () {
        return authority;
    }

    int getPort() {
        return port;
    }

    String getFile () {
        return file;
    }

    String getRef () {
        return ref;
    }

    int getHashCode () {
        return hashCode;
    }

    String reconstituteUrlString() {

        // 预计算 StringBuilder 的长度
        int len = protocol.length() + 1;
        if (authority != null && authority.length() > 0)
            len += 2 + authority.length();
        if (file != null) {
            len += file.length();
        }
        if (ref != null)
            len += 1 + ref.length();
        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(":");
        if (authority != null && authority.length() > 0) {
            result.append("//");
            result.append(authority);
        }
        if (file != null) {
            result.append(file);
        }
        if (ref != null) {
            result.append("#");
            result.append(ref);
        }
        return result.toString();
    }
}
