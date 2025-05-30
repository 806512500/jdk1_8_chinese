
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

import java.util.List;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * 一个 HttpCookie 对象表示一个 HTTP cookie，它在服务器和用户代理之间携带状态信息。Cookie 被广泛用于创建有状态的会话。
 *
 * <p> 有 3 个 HTTP cookie 规范：
 * <blockquote>
 *   Netscape 草案<br>
 *   RFC 2109 - <a href="http://www.ietf.org/rfc/rfc2109.txt">
 * <i>http://www.ietf.org/rfc/rfc2109.txt</i></a><br>
 *   RFC 2965 - <a href="http://www.ietf.org/rfc/rfc2965.txt">
 * <i>http://www.ietf.org/rfc/rfc2965.txt</i></a>
 * </blockquote>
 *
 * <p> HttpCookie 类可以接受这 3 种形式的语法。
 *
 * @author Edward Wang
 * @since 1.6
 */
public final class HttpCookie implements Cloneable {
    // ---------------- Fields --------------

    // cookie 的值。
    private final String name;  // NAME= ... "$Name" 样式保留
    private String value;       // NAME 的值

    // 编码在头的 cookie 字段中的属性。
    private String comment;     // Comment=VALUE ... 描述 cookie 的用途
    private String commentURL;  // CommentURL="http URL" ... 描述 cookie 的用途
    private boolean toDiscard;  // Discard ... 无条件丢弃 cookie
    private String domain;      // Domain=VALUE ... 可见 cookie 的域
    private long maxAge = MAX_AGE_UNSPECIFIED;  // Max-Age=VALUE ... cookie 自动过期
    private String path;        // Path=VALUE ... 可见 cookie 的 URL
    private String portlist;    // Port[="portlist"] ... 可返回 cookie 的端口
    private boolean secure;     // Secure ... 例如使用 SSL
    private boolean httpOnly;   // HttpOnly ... 即不可通过脚本访问
    private int version = 1;    // Version=1 ... RFC 2965 样式

    // 如果 cookie 是通过解析头构建的，则保存构建此 cookie 的原始头，否则为 null。
    private final String header;

    // 保存 HTTP cookie 的创建时间（以秒为单位），以便稍后进行过期计算
    private final long whenCreated;

    // 由于正数和零 max-age 有其含义，
    // 此值作为 '未指定 max-age' 的提示
    private final static long MAX_AGE_UNSPECIFIED = -1;

    // Netscape cookie 草案使用的日期格式
    // 以及在各种站点上看到的格式
    private final static String[] COOKIE_DATE_FORMATS = {
        "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
        "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
        "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
        "EEE',' dd-MMM-yy HH:mm:ss 'GMT'",
        "EEE',' dd MMM yy HH:mm:ss 'GMT'",
        "EEE MMM dd yy HH:mm:ss 'GMT'Z"
    };

    // 表示 set-cookie 头标记的常量字符串
    private final static String SET_COOKIE = "set-cookie:";
    private final static String SET_COOKIE2 = "set-cookie2:";

    // ---------------- Ctors --------------

    /**
     * 构造一个具有指定名称和值的 cookie。
     *
     * <p> 名称必须符合 RFC 2965。这意味着它只能包含 ASCII 字母数字字符，不能包含逗号、分号或空格，也不能以 $ 字符开头。cookie 的名称在创建后不能更改。
     *
     * <p> 值可以是服务器选择发送的任何内容。其值可能只对服务器有意义。可以使用 {@code setValue} 方法在创建后更改 cookie 的值。
     *
     * <p> 默认情况下，cookie 是根据 RFC 2965 cookie 规范创建的。可以使用 {@code setVersion} 方法更改版本。
     *
     *
     * @param  name
     *         一个 {@code String} 指定 cookie 的名称
     *
     * @param  value
     *         一个 {@code String} 指定 cookie 的值
     *
     * @throws  IllegalArgumentException
     *          如果 cookie 名称包含非法字符
     * @throws  NullPointerException
     *          如果 {@code name} 为 {@code null}
     *
     * @see #setValue
     * @see #setVersion
     */
    public HttpCookie(String name, String value) {
        this(name, value, null /*header*/);
    }

    private HttpCookie(String name, String value, String header) {
        name = name.trim();
        if (name.length() == 0 || !isToken(name) || name.charAt(0) == '$') {
            throw new IllegalArgumentException("非法 cookie 名称");
        }

        this.name = name;
        this.value = value;
        toDiscard = false;
        secure = false;

        whenCreated = System.currentTimeMillis();
        portlist = null;
        this.header = header;
    }

    /**
     * 从 set-cookie 或 set-cookie2 头字符串构造 cookie。
     * RFC 2965 第 3.2.2 节 set-cookie2 语法表明一个头行可能包含多个 cookie 定义，因此这是一个静态实用方法而不是另一个构造函数。
     *
     * @param  header
     *         一个 {@code String} 指定 set-cookie 头。头应以 "set-cookie" 或 "set-cookie2" 标记开头；或者它根本没有前导标记。
     *
     * @return  从头行字符串解析的 cookie 列表
     *
     * @throws  IllegalArgumentException
     *          如果头字符串违反 cookie 规范的语法或 cookie 名称包含非法字符。
     * @throws  NullPointerException
     *          如果头字符串为 {@code null}
     */
    public static List<HttpCookie> parse(String header) {
        return parse(header, false);
    }

    // 私有版本的 parse()，将用于创建 cookie 的原始头存储在 cookie 本身中。这可以用于使用此类定义的内部解析逻辑过滤 Set-Cookie[2] 头。
    private static List<HttpCookie> parse(String header, boolean retainHeader) {

        int version = guessCookieVersion(header);

        // 如果头以 set-cookie 或 set-cookie2 开头，则将其剥离
        if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            header = header.substring(SET_COOKIE2.length());
        } else if (startsWithIgnoreCase(header, SET_COOKIE)) {
            header = header.substring(SET_COOKIE.length());
        }

        List<HttpCookie> cookies = new java.util.ArrayList<>();
        // Netscape cookie 的过期属性中可能包含逗号，而逗号是 rfc 2965/2109 cookie 头字符串的分隔符。
        // 因此解析逻辑略有不同
        if (version == 0) {
            // Netscape 草案 cookie
            HttpCookie cookie = parseInternal(header, retainHeader);
            cookie.setVersion(0);
            cookies.add(cookie);
        } else {
            // rfc2965/2109 cookie
            // 如果头字符串包含多个 cookie，
            // 它们将用逗号分隔
            List<String> cookieStrings = splitMultiCookies(header);
            for (String cookieStr : cookieStrings) {
                HttpCookie cookie = parseInternal(cookieStr, retainHeader);
                cookie.setVersion(1);
                cookies.add(cookie);
            }
        }

        return cookies;
    }

    // ---------------- Public operations --------------

    /**
     * 报告此 HTTP cookie 是否已过期。
     *
     * @return  {@code true} 表示此 HTTP cookie 已过期；
     *          否则，{@code false}
     */
    public boolean hasExpired() {
        if (maxAge == 0) return true;

        // 如果未指定 max-age，当用户代理关闭时，此 cookie 应该被丢弃，但
        // 它尚未过期。
        if (maxAge == MAX_AGE_UNSPECIFIED) return false;

        long deltaSecond = (System.currentTimeMillis() - whenCreated) / 1000;
        if (deltaSecond > maxAge)
            return true;
        else
            return false;
    }

    /**
     * 指定描述 cookie 用途的注释。
     * 如果浏览器向用户显示 cookie，注释将非常有用。Netscape 版本 0 cookie 不支持注释。
     *
     * @param  purpose
     *         一个 {@code String} 指定要显示给用户的注释
     *
     * @see  #getComment
     */
    public void setComment(String purpose) {
        comment = purpose;
    }

    /**
     * 返回描述此 cookie 用途的注释，或
     * 如果 cookie 没有注释，则返回 {@code null}。
     *
     * @return  一个 {@code String} 包含注释，或 {@code null} 如果没有
     *
     * @see  #setComment
     */
    public String getComment() {
        return comment;
    }

    /**
     * 指定描述 cookie 用途的注释 URL。
     * 如果浏览器向用户显示 cookie，注释 URL 将非常有用。注释 URL 仅限 RFC 2965。
     *
     * @param  purpose
     *         一个 {@code String} 指定要显示给用户的注释 URL
     *
     * @see  #getCommentURL
     */
    public void setCommentURL(String purpose) {
        commentURL = purpose;
    }

    /**
     * 返回描述此 cookie 用途的注释 URL，或
     * 如果 cookie 没有注释 URL，则返回 {@code null}。
     *
     * @return  一个 {@code String} 包含注释 URL，或 {@code null}
     *          如果没有
     *
     * @see  #setCommentURL
     */
    public String getCommentURL() {
        return commentURL;
    }

    /**
     * 指定用户代理是否应无条件丢弃 cookie。
     * 这是 RFC 2965 仅有的属性。
     *
     * @param  discard
     *         {@code true} 表示无条件丢弃 cookie
     *
     * @see  #getDiscard
     */
    public void setDiscard(boolean discard) {
        toDiscard = discard;
    }

    /**
     * 返回 cookie 的 discard 属性
     *
     * @return  一个 {@code boolean} 表示此 cookie 的 discard 属性
     *
     * @see  #setDiscard
     */
    public boolean getDiscard() {
        return toDiscard;
    }

    /**
     * 指定 cookie 的端口列表，限制 cookie 可以在 Cookie 头中返回的端口。
     *
     * @param  ports
     *         一个 {@code String} 指定端口列表，由逗号分隔的数字序列
     *
     * @see  #getPortlist
     */
    public void setPortlist(String ports) {
        portlist = ports;
    }

    /**
     * 返回 cookie 的端口列表属性
     *
     * @return  一个 {@code String} 包含端口列表或 {@code null} 如果没有
     *
     * @see  #setPortlist
     */
    public String getPortlist() {
        return portlist;
    }

    /**
     * 指定此 cookie 应呈现的域。
     *
     * <p> 域名的格式由 RFC 2965 指定。域名以点（{@code .foo.com}）开头，表示 cookie 对指定的域名系统（DNS）区域（例如 {@code www.foo.com}，但不包括 {@code a.b.foo.com}）可见。默认情况下，cookie 仅返回给发送它们的服务器。
     *
     * @param  pattern
     *         一个 {@code String} 包含此 cookie 可见的域名；格式符合 RFC 2965
     *
     * @see  #getDomain
     */
    public void setDomain(String pattern) {
        if (pattern != null)
            domain = pattern.toLowerCase();
        else
            domain = pattern;
    }

    /**
     * 返回为此 cookie 设置的域名。域名的格式由 RFC 2965 指定。
     *
     * @return  一个 {@code String} 包含域名
     *
     * @see  #setDomain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 设置 cookie 的最大生存时间（以秒为单位）。
     *
     * <p> 正值表示 cookie 将在多少秒后过期。请注意，该值是 cookie 过期的最大时间，而不是 cookie 的当前时间。
     *
     * <p> 负值表示 cookie 不会持久存储，并将在 Web 浏览器退出时被删除。零值表示 cookie 应立即被删除。
     *
     * @param  expiry
     *         一个整数，指定 cookie 的最大生存时间（以秒为单位）；
     *         如果为零，cookie 应立即被丢弃；否则，cookie 的最大生存时间未指定。
     *
     * @see  #getMaxAge
     */
    public void setMaxAge(long expiry) {
        maxAge = expiry;
    }

    /**
     * 返回 cookie 的最大生存时间，以秒为单位。默认情况下，
     * {@code -1} 表示 cookie 将持续到浏览器关闭。
     *
     * @return  一个整数，指定 cookie 的最大生存时间（以秒为单位）
     *
     * @see  #setMaxAge
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * 指定客户端应返回 cookie 的路径。
     *
     * <p> cookie 对指定目录及其所有子目录中的所有页面可见。
     * cookie 的路径必须包括设置 cookie 的 servlet，例如 <i>/catalog</i>，这使得 cookie
     * 对服务器上 <i>/catalog</i> 下的所有目录可见。
     *
     * <p> 有关为 cookie 设置路径名的更多信息，请参阅 RFC 2965（可在 Internet 上找到）。
     *
     * @param  uri
     *         一个 {@code String} 指定路径
     *
     * @see  #getPath
     */
    public void setPath(String uri) {
        path = uri;
    }


                /**
     * 返回浏览器返回此 cookie 时在服务器上的路径。
     * 该 cookie 对服务器上的所有子路径可见。
     *
     * @return  一个 {@code String} 指定包含 servlet 名称的路径，例如 <i>/catalog</i>
     *
     * @see  #setPath
     */
    public String getPath() {
        return path;
    }

    /**
     * 指示 cookie 是否应仅使用安全协议（如 HTTPS 或 SSL）发送。
     *
     * <p> 默认值为 {@code false}。
     *
     * @param  flag
     *         如果为 {@code true}，则 cookie 只能通过安全协议（如 HTTPS）发送。如果为 {@code false}，则可以通过任何协议发送。
     *
     * @see  #getSecure
     */
    public void setSecure(boolean flag) {
        secure = flag;
    }

    /**
     * 如果发送此 cookie 应限制为安全协议，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return  如果 cookie 可以通过任何标准协议发送，则返回 {@code false}；否则，返回 {@code true}
     *
     * @see  #setSecure
     */
    public boolean getSecure() {
        return secure;
    }

    /**
     * 返回 cookie 的名称。名称在创建后不能更改。
     *
     * @return  一个 {@code String} 指定 cookie 的名称
     */
    public String getName() {
        return name;
    }

    /**
     * 在创建 cookie 后为其分配新值。
     * 如果使用二进制值，您可能希望使用 BASE64 编码。
     *
     * <p> 对于版本 0 的 cookie，值不应包含空格、括号、括号、等号、逗号、双引号、斜杠、问号、@ 符号、冒号和分号。空值在所有浏览器上的行为可能不一致。
     *
     * @param  newValue
     *         一个 {@code String} 指定新值
     *
     * @see  #getValue
     */
    public void setValue(String newValue) {
        value = newValue;
    }

    /**
     * 返回 cookie 的值。
     *
     * @return  一个包含 cookie 当前值的 {@code String}
     *
     * @see  #setValue
     */
    public String getValue() {
        return value;
    }

    /**
     * 返回此 cookie 遵循的协议版本。版本 1 遵循 RFC 2965/2109，版本 0 遵循原始的 Netscape 规范。浏览器提供的 cookie 使用并标识浏览器的 cookie 版本。
     *
     * @return  如果 cookie 遵循原始的 Netscape 规范，则返回 0；如果 cookie 遵循 RFC 2965/2109，则返回 1
     *
     * @see  #setVersion
     */
    public int getVersion() {
        return version;
    }

    /**
     * 设置此 cookie 遵循的 cookie 协议版本。版本 0 遵循原始的 Netscape cookie 规范。版本 1 遵循 RFC 2965/2109。
     *
     * @param  v
     *         如果 cookie 应遵循原始的 Netscape 规范，则为 0；如果 cookie 应遵循 RFC 2965/2109，则为 1
     *
     * @throws  IllegalArgumentException
     *          如果 {@code v} 既不是 0 也不是 1
     *
     * @see  #getVersion
     */
    public void setVersion(int v) {
        if (v != 0 && v != 1) {
            throw new IllegalArgumentException("cookie version should be 0 or 1");
        }

        version = v;
    }

    /**
     * 如果此 cookie 包含 <i>HttpOnly</i> 属性，则返回 {@code true}。这意味着 cookie 不应被脚本引擎（如 javascript）访问。
     *
     * @return  如果此 cookie 应被视为 HTTPOnly，则返回 {@code true}
     *
     * @see  #setHttpOnly(boolean)
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * 指示 cookie 是否应被视为 HTTP Only。如果设置为 {@code true}，则意味着 cookie 不应被脚本引擎（如 javascript）访问。
     *
     * @param  httpOnly
     *         如果为 {@code true}，则使 cookie HTTP only，即仅作为 HTTP 请求的一部分可见。
     *
     * @see  #isHttpOnly()
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * 检查主机名是否在域中的工具方法。
     *
     * <p> 这个概念在 cookie 规范中描述。为了理解这个概念，需要首先定义一些术语：
     * <blockquote>
     * 有效主机名 = 如果主机名包含点，则为主机名<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;否则为 hostname.local
     * </blockquote>
     * <p> 如果主机 A 的名称与主机 B 的名称字符串比较相等；或者
     * <blockquote><ul>
     *   <li>它们的主机名字符串比较相等；或者</li>
     *   <li>A 是一个 HDN 字符串，形式为 NB，其中 N 是一个非空名称字符串，B 的形式为 .B'，且 B' 是一个 HDN 字符串。 （因此，x.y.com 与 .Y.com 匹配，但不与 Y.com 匹配。）</li>
     * </ul></blockquote>
     *
     * <p> 如果主机不在域中（RFC 2965 sec. 3.3.2）：
     * <blockquote><ul>
     *   <li>域属性值中没有嵌入的点，且值不是 .local。</li>
     *   <li>从请求主机派生的有效主机名与域属性不匹配。</li>
     *   <li>请求主机是 HDN（不是 IP 地址），形式为 HD，其中 D 是域属性的值，H 是包含一个或多个点的字符串。</li>
     * </ul></blockquote>
     *
     * <p> 示例：
     * <blockquote><ul>
     *   <li>从请求主机 y.x.foo.com 设置的 Domain=.foo.com 的 Set-Cookie2 将被拒绝，因为 H 是 y.x 且包含点。</li>
     *   <li>从请求主机 x.foo.com 设置的 Domain=.foo.com 的 Set-Cookie2 将被接受。</li>
     *   <li>设置 Domain=.com 或 Domain=.com. 的 Set-Cookie2 将总是被拒绝，因为没有嵌入的点。</li>
     *   <li>从请求主机 example 设置的 Domain=.local 的 Set-Cookie2 将被接受，因为请求主机的有效主机名是 example.local，且 example.local 与 .local 匹配。</li>
     * </ul></blockquote>
     *
     * @param  domain
     *         要检查主机名的域名
     *
     * @param  host
     *         要检查的主机名
     *
     * @return  如果它们匹配，则返回 {@code true}；否则返回 {@code false}
     */
    public static boolean domainMatches(String domain, String host) {
        if (domain == null || host == null)
            return false;

        // 如果域中没有嵌入的点且域不是 .local
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf('.');
        if (embeddedDotInDomain == 0)
            embeddedDotInDomain = domain.indexOf('.', 1);
        if (!isLocalDomain
            && (embeddedDotInDomain == -1 ||
                embeddedDotInDomain == domain.length() - 1))
            return false;

        // 如果主机名中没有点且域名为 .local 或 host.local
        int firstDotInHost = host.indexOf('.');
        if (firstDotInHost == -1 &&
            (isLocalDomain ||
             domain.equalsIgnoreCase(host + ".local"))) {
            return true;
        }

        int domainLength = domain.length();
        int lengthDiff = host.length() - domainLength;
        if (lengthDiff == 0) {
            // 如果主机名和域名只是字符串比较相等
            return host.equalsIgnoreCase(domain);
        }
        else if (lengthDiff > 0) {
            // 需要检查 H & D 组件
            String H = host.substring(0, lengthDiff);
            String D = host.substring(lengthDiff);

            return (H.indexOf('.') == -1 && D.equalsIgnoreCase(domain));
        }
        else if (lengthDiff == -1) {
            // 如果域实际上是 .host
            return (domain.charAt(0) == '.' &&
                        host.equalsIgnoreCase(domain.substring(1)));
        }

        return false;
    }

    /**
     * 构造此 cookie 的头字符串表示形式，该表示形式遵循相应的 cookie 规范，但不包含前导的 "Cookie:" 标记。
     *
     * @return  cookie 的字符串形式。字符串具有定义的格式
     */
    @Override
    public String toString() {
        if (getVersion() > 0) {
            return toRFC2965HeaderString();
        } else {
            return toNetscapeHeaderString();
        }
    }

    /**
     * 测试两个 HTTP cookie 是否相等。
     *
     * <p> 只有当两个 cookie 来自相同的域（不区分大小写）、具有相同的名称（不区分大小写）且具有相同的路径（区分大小写）时，结果才为 {@code true}。
     *
     * @return  如果两个 HTTP cookie 相等，则返回 {@code true}；否则返回 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof HttpCookie))
            return false;
        HttpCookie other = (HttpCookie)obj;

        // 如果两个 HTTP cookie（RFC 2965 sec. 3.3.3）：
        //   1. 来自相同的域（不区分大小写），
        //   2. 具有相同的名称（不区分大小写），
        //   3. 且具有相同的路径（区分大小写），
        // 则它们相等。
        return equalsIgnoreCase(getName(), other.getName()) &&
               equalsIgnoreCase(getDomain(), other.getDomain()) &&
               Objects.equals(getPath(), other.getPath());
    }

    /**
     * 返回此 HTTP cookie 的哈希码。结果是此 cookie 的三个重要组件（名称、域和路径）的哈希码值之和。也就是说，哈希码是以下表达式的值：
     * <blockquote>
     * getName().toLowerCase().hashCode()<br>
     * + getDomain().toLowerCase().hashCode()<br>
     * + getPath().hashCode()
     * </blockquote>
     *
     * @return  此 HTTP cookie 的哈希码
     */
    @Override
    public int hashCode() {
        int h1 = name.toLowerCase().hashCode();
        int h2 = (domain!=null) ? domain.toLowerCase().hashCode() : 0;
        int h3 = (path!=null) ? path.hashCode() : 0;

        return h1 + h2 + h3;
    }

    /**
     * 创建并返回此对象的副本。
     *
     * @return  此 HTTP cookie 的克隆
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // ---------------- 私有操作 --------------

    // 注意 -- 目前禁用以允许完全的 Netscape 兼容性
    // 从 RFC 2068，token 特殊字符
    //
    // private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";
    private static final String tspecials = ",; ";  // 故意包含空格

    /*
     * 测试字符串并返回 true 如果字符串被视为 token。
     *
     * @param  value
     *         要测试的 {@code String}
     *
     * @return  如果 {@code String} 是 token，则返回 {@code true}；否则返回 {@code false}
     */
    private static boolean isToken(String value) {
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    /*
     * 解析头字符串为 cookie 对象。
     *
     * @param  header
     *         头字符串；应仅包含一个 NAME=VALUE 对
     *
     * @return  提取的 HttpCookie
     *
     * @throws  IllegalArgumentException
     *          如果头字符串违反 cookie 规范
     */
    private static HttpCookie parseInternal(String header,
                                            boolean retainHeader)
    {
        HttpCookie cookie = null;
        String namevaluePair = null;

        StringTokenizer tokenizer = new StringTokenizer(header, ";");

        // 应该始终至少有一个名称-值对；
        // 它是 cookie 的名称
        try {
            namevaluePair = tokenizer.nextToken();
            int index = namevaluePair.indexOf('=');
            if (index != -1) {
                String name = namevaluePair.substring(0, index).trim();
                String value = namevaluePair.substring(index + 1).trim();
                if (retainHeader)
                    cookie = new HttpCookie(name,
                                            stripOffSurroundingQuote(value),
                                            header);
                else
                    cookie = new HttpCookie(name,
                                            stripOffSurroundingQuote(value));
            } else {
                // 名称-值对中没有 "="；这是一个错误
                throw new IllegalArgumentException("Invalid cookie name-value pair");
            }
        } catch (NoSuchElementException ignored) {
            throw new IllegalArgumentException("Empty cookie header string");
        }

        // 剩余的名称-值对是 cookie 的属性
        while (tokenizer.hasMoreTokens()) {
            namevaluePair = tokenizer.nextToken();
            int index = namevaluePair.indexOf('=');
            String name, value;
            if (index != -1) {
                name = namevaluePair.substring(0, index).trim();
                value = namevaluePair.substring(index + 1).trim();
            } else {
                name = namevaluePair.trim();
                value = null;
            }

            // 为 cookie 分配属性
            assignAttribute(cookie, name, value);
        }

        return cookie;
    }

    /*
     * 为属性名称分配 cookie 属性值；
     * 使用 map 模拟方法调度
     */
    static interface CookieAttributeAssignor {
            public void assign(HttpCookie cookie,
                               String attrName,
                               String attrValue);
    }
    static final java.util.Map<String, CookieAttributeAssignor> assignors =
            new java.util.HashMap<>();
    static {
        assignors.put("comment", new CookieAttributeAssignor() {
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getComment() == null)
                        cookie.setComment(attrValue);
                }
            });
        assignors.put("commenturl", new CookieAttributeAssignor() {
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getCommentURL() == null)
                        cookie.setCommentURL(attrValue);
                }
            });
        assignors.put("discard", new CookieAttributeAssignor() {
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    cookie.setDiscard(true);
                }
            });
        assignors.put("domain", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getDomain() == null)
                        cookie.setDomain(attrValue);
                }
            });
        assignors.put("max-age", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    try {
                        long maxage = Long.parseLong(attrValue);
                        if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED)
                            cookie.setMaxAge(maxage);
                    } catch (NumberFormatException ignored) {
                        throw new IllegalArgumentException(
                                "Illegal cookie max-age attribute");
                    }
                }
            });
        assignors.put("path", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getPath() == null)
                        cookie.setPath(attrValue);
                }
            });
        assignors.put("port", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getPortlist() == null)
                        cookie.setPortlist(attrValue == null ? "" : attrValue);
                }
            });
        assignors.put("secure", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    cookie.setSecure(true);
                }
            });
        assignors.put("httponly", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    cookie.setHttpOnly(true);
                }
            });
        assignors.put("version", new CookieAttributeAssignor(){
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    try {
                        int version = Integer.parseInt(attrValue);
                        cookie.setVersion(version);
                    } catch (NumberFormatException ignored) {
                        // 忽略错误版本，它将默认为 0 或 1
                    }
                }
            });
        assignors.put("expires", new CookieAttributeAssignor(){ // 仅 Netscape
                public void assign(HttpCookie cookie,
                                   String attrName,
                                   String attrValue) {
                    if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED) {
                        cookie.setMaxAge(cookie.expiryDate2DeltaSeconds(attrValue));
                    }
                }
            });
    }
    private static void assignAttribute(HttpCookie cookie,
                                        String attrName,
                                        String attrValue)
    {
        // 去除周围的引号（如果有）
        attrValue = stripOffSurroundingQuote(attrValue);


                    CookieAttributeAssignor assignor = assignors.get(attrName.toLowerCase());
        if (assignor != null) {
            assignor.assign(cookie, attrName, attrValue);
        } else {
            // 忽略该属性，遵循 RFC 2965
        }
    }

    static {
        sun.misc.SharedSecrets.setJavaNetHttpCookieAccess(
            new sun.misc.JavaNetHttpCookieAccess() {
                public List<HttpCookie> parse(String header) {
                    return HttpCookie.parse(header, true);
                }

                public String header(HttpCookie cookie) {
                    return cookie.header;
                }
            }
        );
    }

    /*
     * 返回此 cookie 构造时的原始头，如果它是通过解析头构造的，则返回该头，否则返回 null。
     */
    private String header() {
        return header;
    }

    /*
     * 构造此 cookie 的字符串表示形式。字符串格式符合 Netscape 规范，但不包含前导的 "Cookie:" 标记。
     */
    private String toNetscapeHeaderString() {
        return getName() + "=" + getValue();
    }

    /*
     * 构造此 cookie 的字符串表示形式。字符串格式符合 RFC 2965/2109，但不包含前导的 "Cookie:" 标记。
     */
    private String toRFC2965HeaderString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append("=\"").append(getValue()).append('"');
        if (getPath() != null)
            sb.append(";$Path=\"").append(getPath()).append('"');
        if (getDomain() != null)
            sb.append(";$Domain=\"").append(getDomain()).append('"');
        if (getPortlist() != null)
            sb.append(";$Port=\"").append(getPortlist()).append('"');

        return sb.toString();
    }

    static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /*
     * @param  dateString
     *         一个符合 Netscape cookie 规范的日期字符串
     *
     * @return 从创建此 cookie 的时间到 dateString 指定的时间之间的秒数差
     */
    private long expiryDate2DeltaSeconds(String dateString) {
        Calendar cal = new GregorianCalendar(GMT);
        for (int i = 0; i < COOKIE_DATE_FORMATS.length; i++) {
            SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i],
                                                       Locale.US);
            cal.set(1970, 0, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try {
                cal.setTime(df.parse(dateString));
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    // 按照 RFC 6265 标准设置两位数年份
                    int year = cal.get(Calendar.YEAR);
                    year %= 100;
                    if (year < 70) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    cal.set(Calendar.YEAR, year);
                }
                return (cal.getTimeInMillis() - whenCreated) / 1000;
            } catch (Exception e) {
                // 忽略，尝试下一个日期格式
            }
        }
        return 0;
    }

    /*
     * 通过 set-cookie 头字符串尝试猜测 cookie 版本
     */
    private static int guessCookieVersion(String header) {
        int version = 0;

        header = header.toLowerCase();
        if (header.indexOf("expires=") != -1) {
            // 只有 Netscape cookie 使用 'expires'
            version = 0;
        } else if (header.indexOf("version=") != -1) {
            // 版本是 RFC 2965/2109 cookie 的强制属性
            version = 1;
        } else if (header.indexOf("max-age") != -1) {
            // RFC 2965/2109 使用 'max-age'
            version = 1;
        } else if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            // 只有 RFC 2965 cookie 以 'set-cookie2' 开头
            version = 1;
        }

        return version;
    }

    private static String stripOffSurroundingQuote(String str) {
        if (str != null && str.length() > 2 &&
            str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return str.substring(1, str.length() - 1);
        }
        if (str != null && str.length() > 2 &&
            str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'') {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static boolean equalsIgnoreCase(String s, String t) {
        if (s == t) return true;
        if ((s != null) && (t != null)) {
            return s.equalsIgnoreCase(t);
        }
        return false;
    }

    private static boolean startsWithIgnoreCase(String s, String start) {
        if (s == null || start == null) return false;

        if (s.length() >= start.length() &&
                start.equalsIgnoreCase(s.substring(0, start.length()))) {
            return true;
        }

        return false;
    }

    /*
     * 根据 RFC 2965 分割 cookie 头字符串：
     *   1) 在逗号处分割；
     *   2) 但不包括被双引号包围的逗号，这些逗号可能在端口列表或嵌入的 URI 中。
     *
     * @param  header
     *         要分割的 cookie 头字符串
     *
     * @return  字符串列表；从不为 null
     */
    private static List<String> splitMultiCookies(String header) {
        List<String> cookies = new java.util.ArrayList<String>();
        int quoteCount = 0;
        int p, q;

        for (p = 0, q = 0; p < header.length(); p++) {
            char c = header.charAt(p);
            if (c == '"') quoteCount++;
            if (c == ',' && (quoteCount % 2 == 0)) {
                // 是逗号且不被双引号包围
                cookies.add(header.substring(q, p));
                q = p + 1;
            }
        }

        cookies.add(header.substring(q));

        return cookies;
    }
}
