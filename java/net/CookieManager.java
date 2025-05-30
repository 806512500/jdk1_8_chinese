
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

import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.io.IOException;
import sun.util.logging.PlatformLogger;

/**
 * CookieManager 提供了 {@link CookieHandler} 的具体实现，
 * 将 cookie 的存储与接受和拒绝 cookie 的策略分开。CookieManager 初始化时使用一个 {@link CookieStore}
 * 来管理存储，并使用一个 {@link CookiePolicy} 对象来做出 cookie 接受/拒绝的策略决策。
 *
 * <p> java.net 包中的 HTTP cookie 管理如下所示：
 * <blockquote>
 * <pre>{@code
 *                  use
 * CookieHandler <------- HttpURLConnection
 *       ^
 *       | impl
 *       |         use
 * CookieManager -------> CookiePolicy
 *             |   use
 *             |--------> HttpCookie
 *             |              ^
 *             |              | use
 *             |   use        |
 *             |--------> CookieStore
 *                            ^
 *                            | impl
 *                            |
 *                  Internal in-memory implementation
 * }</pre>
 * <ul>
 *   <li>
 *     CookieHandler 是 cookie 管理的核心。用户可以调用 CookieHandler.setDefault 来设置一个具体的 CookieHandler 实现。
 *   </li>
 *   <li>
 *     CookiePolicy.shouldAccept 将由 CookieManager.put 调用，以确定是否接受某个 cookie 并将其放入 cookie 存储中。用户可以使用
 *     三个预定义的 CookiePolicy 之一，即 ACCEPT_ALL、ACCEPT_NONE 和 ACCEPT_ORIGINAL_SERVER，或者用户可以定义自己的 CookiePolicy 实现
 *     并告诉 CookieManager 使用它。
 *   </li>
 *   <li>
 *     CookieStore 是存储任何接受的 HTTP cookie 的地方。如果在创建时未指定，CookieManager 实例将使用内部内存实现。或者用户可以实现一个
 *     并告诉 CookieManager 使用它。
 *   </li>
 *   <li>
 *     目前，CookieManager 仅使用 CookieStore.add(URI, HttpCookie) 和 CookieStore.get(URI)。其他方法是为了完整性而提供的，可能需要
 *     一个更复杂的 CookieStore 实现，例如 NetscapeCookieStore。
 *   </li>
 * </ul>
 * </blockquote>
 *
 * <p>用户可以通过多种方式挂钩自己的 HTTP cookie 管理行为，例如：
 * <blockquote>
 * <ul>
 *   <li>使用 CookieHandler.setDefault 设置一个新的 {@link CookieHandler} 实现
 *   <li>让 CookieManager 成为默认的 {@link CookieHandler} 实现，但实现用户自己的 {@link CookieStore} 和 {@link CookiePolicy}
 *       并告诉默认的 CookieManager 使用它们：
 *     <blockquote><pre>
 *       // 这应该在 HTTP 会话开始时完成
 *       CookieHandler.setDefault(new CookieManager(new MyCookieStore(), new MyCookiePolicy()));
 *     </pre></blockquote>
 *   <li>让 CookieManager 成为默认的 {@link CookieHandler} 实现，但使用自定义的 {@link CookiePolicy}：
 *     <blockquote><pre>
 *       // 这应该在 HTTP 会话开始时完成
 *       CookieHandler.setDefault(new CookieManager());
 *       // 这可以在 HTTP 会话的任何时间点完成
 *       ((CookieManager)CookieHandler.getDefault()).setCookiePolicy(new MyCookiePolicy());
 *     </pre></blockquote>
 * </ul>
 * </blockquote>
 *
 * <p>该实现符合 <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a> 的第 3.3 节。
 *
 * @see CookiePolicy
 * @author Edward Wang
 * @since 1.6
 */
public class CookieManager extends CookieHandler
{
    /* ---------------- Fields -------------- */

    private CookiePolicy policyCallback;


    private CookieStore cookieJar = null;


    /* ---------------- Ctors -------------- */

    /**
     * 创建一个新的 cookie 管理器。
     *
     * <p>此构造函数将创建一个新的 cookie 管理器，默认的 cookie 存储和接受策略。效果与
     * {@code CookieManager(null, null)} 相同。
     */
    public CookieManager() {
        this(null, null);
    }


    /**
     * 使用指定的 cookie 存储和 cookie 策略创建一个新的 cookie 管理器。
     *
     * @param store     由 cookie 管理器使用的 {@code CookieStore}。
     *                  如果为 {@code null}，cookie 管理器将使用默认的内存 CookieStore 实现。
     * @param cookiePolicy      由 cookie 管理器作为策略回调使用的 {@code CookiePolicy} 实例。
     *                          如果为 {@code null}，将使用 ACCEPT_ORIGINAL_SERVER。
     */
    public CookieManager(CookieStore store,
                         CookiePolicy cookiePolicy)
    {
        // 如果未指定，则使用默认的 cookie 策略
        policyCallback = (cookiePolicy == null) ? CookiePolicy.ACCEPT_ORIGINAL_SERVER
                                                : cookiePolicy;

        // 如果未指定要使用的 CookieStore，则使用默认的
        if (store == null) {
            cookieJar = new InMemoryCookieStore();
        } else {
            cookieJar = store;
        }
    }


    /* ---------------- Public operations -------------- */

    /**
     * 设置此 cookie 管理器的 cookie 策略。
     *
     * <p> {@code CookieManager} 的实例默认具有 ACCEPT_ORIGINAL_SERVER 策略。用户可以随时调用此方法设置另一个 cookie 策略。
     *
     * @param cookiePolicy      cookie 策略。可以为 {@code null}，对当前 cookie 策略没有影响。
     */
    public void setCookiePolicy(CookiePolicy cookiePolicy) {
        if (cookiePolicy != null) policyCallback = cookiePolicy;
    }


    /**
     * 获取当前的 cookie 存储。
     *
     * @return  当前由 cookie 管理器使用的 cookie 存储。
     */
    public CookieStore getCookieStore() {
        return cookieJar;
    }


    public Map<String, List<String>>
        get(URI uri, Map<String, List<String>> requestHeaders)
        throws IOException
    {
        // 前提条件检查
        if (uri == null || requestHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        Map<String, List<String>> cookieMap =
                        new java.util.HashMap<String, List<String>>();
        // 如果没有默认的 CookieStore，我们无法获取任何 cookie
        if (cookieJar == null)
            return Collections.unmodifiableMap(cookieMap);

        boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());
        List<HttpCookie> cookies = new java.util.ArrayList<HttpCookie>();
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        for (HttpCookie cookie : cookieJar.get(uri)) {
            // 应用路径匹配规则（RFC 2965 第 3.3.4 节）
            // 并检查可能的 "secure" 标签（即不要通过不安全的链接发送 'secure' cookie）
            if (pathMatches(path, cookie.getPath()) &&
                    (secureLink || !cookie.getSecure())) {
                // 强制执行 httponly 属性
                if (cookie.isHttpOnly()) {
                    String s = uri.getScheme();
                    if (!"http".equalsIgnoreCase(s) && !"https".equalsIgnoreCase(s)) {
                        continue;
                    }
                }
                // 如果存在授权端口列表，则检查
                String ports = cookie.getPortlist();
                if (ports != null && !ports.isEmpty()) {
                    int port = uri.getPort();
                    if (port == -1) {
                        port = "https".equals(uri.getScheme()) ? 443 : 80;
                    }
                    if (isInPortList(ports, port)) {
                        cookies.add(cookie);
                    }
                } else {
                    cookies.add(cookie);
                }
            }
        }

        // 应用排序规则（RFC 2965 第 3.3.4 节）
        List<String> cookieHeader = sortByPath(cookies);

        cookieMap.put("Cookie", cookieHeader);
        return Collections.unmodifiableMap(cookieMap);
    }

    public void
        put(URI uri, Map<String, List<String>> responseHeaders)
        throws IOException
    {
        // 前提条件检查
        if (uri == null || responseHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        }


        // 如果没有默认的 CookieStore，不需要记住任何 cookie
        if (cookieJar == null)
            return;

    PlatformLogger logger = PlatformLogger.getLogger("java.net.CookieManager");
        for (String headerKey : responseHeaders.keySet()) {
            // RFC 2965 3.2.2，键必须是 'Set-Cookie2'
            // 为了向后兼容，这里也接受 'Set-Cookie'
            if (headerKey == null
                || !(headerKey.equalsIgnoreCase("Set-Cookie2")
                     || headerKey.equalsIgnoreCase("Set-Cookie")
                    )
                )
            {
                continue;
            }

            for (String headerValue : responseHeaders.get(headerKey)) {
                try {
                    List<HttpCookie> cookies;
                    try {
                        cookies = HttpCookie.parse(headerValue);
                    } catch (IllegalArgumentException e) {
                        // 无效的头，创建一个空列表并记录错误
                        cookies = java.util.Collections.emptyList();
                        if (logger.isLoggable(PlatformLogger.Level.SEVERE)) {
                            logger.severe("Invalid cookie for " + uri + ": " + headerValue);
                        }
                    }
                    for (HttpCookie cookie : cookies) {
                        if (cookie.getPath() == null) {
                            // 如果未指定路径，则默认路径是页面/文档的目录
                            String path = uri.getPath();
                            if (!path.endsWith("/")) {
                                int i = path.lastIndexOf("/");
                                if (i > 0) {
                                    path = path.substring(0, i + 1);
                                } else {
                                    path = "/";
                                }
                            }
                            cookie.setPath(path);
                        }

                        // 根据 RFC 2965，第 3.3.1 节：
                        // Domain  默认为有效的请求主机。 （注意，因为有效的请求主机开头没有点，
                        // 默认的 Domain 只能与自身匹配。）
                        if (cookie.getDomain() == null) {
                            String host = uri.getHost();
                            if (host != null && !host.contains("."))
                                host += ".local";
                            cookie.setDomain(host);
                        }
                        String ports = cookie.getPortlist();
                        if (ports != null) {
                            int port = uri.getPort();
                            if (port == -1) {
                                port = "https".equals(uri.getScheme()) ? 443 : 80;
                            }
                            if (ports.isEmpty()) {
                                // 空端口列表意味着这应该限制在传入 URI 的端口
                                cookie.setPortlist("" + port );
                                if (shouldAcceptInternal(uri, cookie)) {
                                    cookieJar.add(uri, cookie);
                                }
                            } else {
                                // 仅当 URI 端口在列表中时存储带有端口列表的 cookie，根据
                                // RFC 2965 第 3.3.2 节
                                if (isInPortList(ports, port) &&
                                        shouldAcceptInternal(uri, cookie)) {
                                    cookieJar.add(uri, cookie);
                                }
                            }
                        } else {
                            if (shouldAcceptInternal(uri, cookie)) {
                                cookieJar.add(uri, cookie);
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // 无效的 set-cookie 头字符串
                    // 无操作
                }
            }
        }
    }


    /* ---------------- Private operations -------------- */

    // 确定是否接受此 cookie
    private boolean shouldAcceptInternal(URI uri, HttpCookie cookie) {
        try {
            return policyCallback.shouldAccept(uri, cookie);
        } catch (Exception ignored) { // 保护恶意回调
            return false;
        }
    }


    static private boolean isInPortList(String lst, int port) {
        int i = lst.indexOf(",");
        int val = -1;
        while (i > 0) {
            try {
                val = Integer.parseInt(lst.substring(0, i));
                if (val == port) {
                    return true;
                }
            } catch (NumberFormatException numberFormatException) {
            }
            lst = lst.substring(i+1);
            i = lst.indexOf(",");
        }
        if (!lst.isEmpty()) {
            try {
                val = Integer.parseInt(lst);
                if (val == port) {
                    return true;
                }
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return false;
    }


                /*
     * path-matches algorithm, as defined by RFC 2965
     */
    private boolean pathMatches(String path, String pathToMatchWith) {
        if (path == pathToMatchWith)
            return true;
        if (path == null || pathToMatchWith == null)
            return false;
        if (path.startsWith(pathToMatchWith))
            return true;

        return false;
    }


    /*
     * 按照路径对 cookie 进行排序：具有更具体 Path 属性的 cookie 优先于具有较不具体 Path 属性的 cookie，如 RFC 2965 第 3.3.4 节所定义
     */
    private List<String> sortByPath(List<HttpCookie> cookies) {
        Collections.sort(cookies, new CookiePathComparator());

        List<String> cookieHeader = new java.util.ArrayList<String>();
        for (HttpCookie cookie : cookies) {
            // Netscape cookie 规范和 RFC 2965 对 Cookie 头的格式有不同的要求；RFC 2965 要求在前面加上 $Version="1" 字符串，而 Netscape 则不需要。
            // 这里的解决方法是提前添加 $Version="1" 字符串
            if (cookies.indexOf(cookie) == 0 && cookie.getVersion() > 0) {
                cookieHeader.add("$Version=\"1\"");
            }

            cookieHeader.add(cookie.toString());
        }
        return cookieHeader;
    }


    static class CookiePathComparator implements Comparator<HttpCookie> {
        public int compare(HttpCookie c1, HttpCookie c2) {
            if (c1 == c2) return 0;
            if (c1 == null) return -1;
            if (c2 == null) return 1;

            // 路径规则仅适用于名称相同的 cookie
            if (!c1.getName().equals(c2.getName())) return 0;

            // 具有更具体 Path 属性的 cookie 优先于具有较不具体 Path 属性的 cookie
            if (c1.getPath().startsWith(c2.getPath()))
                return -1;
            else if (c2.getPath().startsWith(c1.getPath()))
                return 1;
            else
                return 0;
        }
    }
}
