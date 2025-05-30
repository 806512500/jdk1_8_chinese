/*
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个简单的内存中的 java.net.CookieStore 实现
 *
 * @author Edward Wang
 * @since 1.6
 */
class InMemoryCookieStore implements CookieStore {
    // 内存中的 cookie 表示
    private List<HttpCookie> cookieJar = null;

    // cookie 通过其域和关联的 URI（如果存在）进行索引
    // 注意：当 cookie 从主数据结构（即 cookieJar）中移除时，
    //       它不会从 domainIndex & uriIndex 中清除。在从索引存储中检索 cookie 时，需要双重检查 cookie 的存在。
    private Map<String, List<HttpCookie>> domainIndex = null;
    private Map<URI, List<HttpCookie>> uriIndex = null;

    // 使用 ReentrantLock 而不是 synchronized 以提高可扩展性
    private ReentrantLock lock = null;


    /**
     * 默认构造函数
     */
    public InMemoryCookieStore() {
        cookieJar = new ArrayList<HttpCookie>();
        domainIndex = new HashMap<String, List<HttpCookie>>();
        uriIndex = new HashMap<URI, List<HttpCookie>>();

        lock = new ReentrantLock(false);
    }

    /**
     * 将一个 cookie 添加到 cookie 存储中。
     */
    public void add(URI uri, HttpCookie cookie) {
        // 前提条件：参数不能为 null
        if (cookie == null) {
            throw new NullPointerException("cookie is null");
        }


        lock.lock();
        try {
            // 如果已经存在一个相同的 cookie，则移除旧的 cookie
            cookieJar.remove(cookie);

            // 如果 cookie 的 max-age 不为 0，则添加新的 cookie
            if (cookie.getMaxAge() != 0) {
                cookieJar.add(cookie);
                // 并将其添加到域索引中
                if (cookie.getDomain() != null) {
                    addIndex(domainIndex, cookie.getDomain(), cookie);
                }
                if (uri != null) {
                    // 也添加到 URI 索引中
                    addIndex(uriIndex, getEffectiveURI(uri), cookie);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * 获取所有满足以下条件的 cookie：
     *  1) 与给定的 URI 域匹配，或在添加到 cookie 存储时与给定的 URI 关联。
     *  3) 未过期。
     * 详情请参见 RFC 2965 sec. 3.3.4。
     */
    public List<HttpCookie> get(URI uri) {
        // 参数不能为 null
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }

        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());
        lock.lock();
        try {
            // 首先检查 domainIndex
            getInternal1(cookies, domainIndex, uri.getHost(), secureLink);
            // 然后检查 uriIndex
            getInternal2(cookies, uriIndex, getEffectiveURI(uri), secureLink);
        } finally {
            lock.unlock();
        }

        return cookies;
    }

    /**
     * 获取 cookie 存储中的所有 cookie，不包括已过期的 cookie
     */
    public List<HttpCookie> getCookies() {
        List<HttpCookie> rt;

        lock.lock();
        try {
            Iterator<HttpCookie> it = cookieJar.iterator();
            while (it.hasNext()) {
                if (it.next().hasExpired()) {
                    it.remove();
                }
            }
        } finally {
            rt = Collections.unmodifiableList(cookieJar);
            lock.unlock();
        }

        return rt;
    }

    /**
     * 获取与此 cookie 存储中的至少一个 cookie 关联的所有 URI
     */
    public List<URI> getURIs() {
        List<URI> uris = new ArrayList<URI>();

        lock.lock();
        try {
            Iterator<URI> it = uriIndex.keySet().iterator();
            while (it.hasNext()) {
                URI uri = it.next();
                List<HttpCookie> cookies = uriIndex.get(uri);
                if (cookies == null || cookies.size() == 0) {
                    // 没有 cookie 列表或与该 URI 条目关联的列表为空，删除它
                    it.remove();
                }
            }
        } finally {
            uris.addAll(uriIndex.keySet());
            lock.unlock();
        }

        return uris;
    }


    /**
     * 从存储中移除一个 cookie
     */
    public boolean remove(URI uri, HttpCookie ck) {
        // 参数不能为 null
        if (ck == null) {
            throw new NullPointerException("cookie is null");
        }

        boolean modified = false;
        lock.lock();
        try {
            modified = cookieJar.remove(ck);
        } finally {
            lock.unlock();
        }

        return modified;
    }


    /**
     * 移除此 cookie 存储中的所有 cookie。
     */
    public boolean removeAll() {
        lock.lock();
        try {
            if (cookieJar.isEmpty()) {
                return false;
            }
            cookieJar.clear();
            domainIndex.clear();
            uriIndex.clear();
        } finally {
            lock.unlock();
        }

        return true;
    }


    /* ---------------- 私有操作 -------------- */


    /*
     * 几乎与 HttpCookie.domainMatches 相同，但有一个区别：它不会拒绝当域的 'H' 部分包含点 ('.') 时的 cookie。
     * 例如：RFC 2965 第 3.3.2 节说，如果主机是 x.y.domain.com
     * 而 cookie 域是 .domain.com，则应拒绝该 cookie。
     * 但这不是现实世界中的情况。浏览器不会拒绝，一些网站（如 yahoo.com）实际上期望这些 cookie 被传递。
     * 并且应该用于 '旧' 样式的 cookie（即 Netscape 类型的 cookie）
     */
    private boolean netscapeDomainMatches(String domain, String host)
    {
        if (domain == null || host == null) {
            return false;
        }

        // 如果域中没有嵌入的点且域不是 .local
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf('.');
        if (embeddedDotInDomain == 0) {
            embeddedDotInDomain = domain.indexOf('.', 1);
        }
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1)) {
            return false;
        }

        // 如果主机名中没有点且域名是 .local
        int firstDotInHost = host.indexOf('.');
        if (firstDotInHost == -1 && isLocalDomain) {
            return true;
        }

        int domainLength = domain.length();
        int lengthDiff = host.length() - domainLength;
        if (lengthDiff == 0) {
            // 如果主机名和域名只是字符串比较相等
            return host.equalsIgnoreCase(domain);
        } else if (lengthDiff > 0) {
            // 需要检查 H & D 组件
            String H = host.substring(0, lengthDiff);
            String D = host.substring(lengthDiff);

            return (D.equalsIgnoreCase(domain));
        } else if (lengthDiff == -1) {
            // 如果域实际上是 .host
            return (domain.charAt(0) == '.' &&
                    host.equalsIgnoreCase(domain.substring(1)));
        }

        return false;
    }

    private void getInternal1(List<HttpCookie> cookies, Map<String, List<HttpCookie>> cookieIndex,
            String host, boolean secureLink) {
        // 使用单独的列表来处理需要移除的 cookie，以避免与迭代器冲突
        ArrayList<HttpCookie> toRemove = new ArrayList<HttpCookie>();
        for (Map.Entry<String, List<HttpCookie>> entry : cookieIndex.entrySet()) {
            String domain = entry.getKey();
            List<HttpCookie> lst = entry.getValue();
            for (HttpCookie c : lst) {
                if ((c.getVersion() == 0 && netscapeDomainMatches(domain, host)) ||
                        (c.getVersion() == 1 && HttpCookie.domainMatches(domain, host))) {
                    if ((cookieJar.indexOf(c) != -1)) {
                        // cookie 仍在主 cookie 存储中
                        if (!c.hasExpired()) {
                            // 不要重复添加并确保它是适当的安全级别
                            if ((secureLink || !c.getSecure()) &&
                                    !cookies.contains(c)) {
                                cookies.add(c);
                            }
                        } else {
                            toRemove.add(c);
                        }
                    } else {
                        // cookie 已从主存储中移除，
                        // 因此也从域索引存储中移除
                        toRemove.add(c);
                    }
                }
            }
            // 清除需要移除的 cookie
            for (HttpCookie c : toRemove) {
                lst.remove(c);
                cookieJar.remove(c);

            }
            toRemove.clear();
        }
    }

    // @param cookies           [OUT] 包含找到的 cookie
    // @param cookieIndex       索引
    // @param comparator        用于决定索引中的 cookie 是否应该返回的预测
    private <T> void getInternal2(List<HttpCookie> cookies,
                                Map<T, List<HttpCookie>> cookieIndex,
                                Comparable<T> comparator, boolean secureLink)
    {
        for (T index : cookieIndex.keySet()) {
            if (comparator.compareTo(index) == 0) {
                List<HttpCookie> indexedCookies = cookieIndex.get(index);
                // 检查与此域关联的 cookie 列表
                if (indexedCookies != null) {
                    Iterator<HttpCookie> it = indexedCookies.iterator();
                    while (it.hasNext()) {
                        HttpCookie ck = it.next();
                        if (cookieJar.indexOf(ck) != -1) {
                            // cookie 仍在主 cookie 存储中
                            if (!ck.hasExpired()) {
                                // 不要重复添加
                                if ((secureLink || !ck.getSecure()) &&
                                        !cookies.contains(ck))
                                    cookies.add(ck);
                            } else {
                                it.remove();
                                cookieJar.remove(ck);
                            }
                        } else {
                            // cookie 已从主存储中移除，
                            // 因此也从域索引存储中移除
                            it.remove();
                        }
                    }
                } // end of indexedCookies != null
            } // end of comparator.compareTo(index) == 0
        } // end of cookieIndex iteration
    }

    // 将 'cookie' 通过 'index' 添加到 'indexStore'
    private <T> void addIndex(Map<T, List<HttpCookie>> indexStore,
                              T index,
                              HttpCookie cookie)
    {
        if (index != null) {
            List<HttpCookie> cookies = indexStore.get(index);
            if (cookies != null) {
                // 可能已经存在相同的 cookie，因此首先移除它
                cookies.remove(cookie);

                cookies.add(cookie);
            } else {
                cookies = new ArrayList<HttpCookie>();
                cookies.add(cookie);
                indexStore.put(index, cookies);
            }
        }
    }


    //
    // 对于 cookie，有效的 URI 应仅为 http://host
    // 路径匹配算法应用时会考虑路径
    //
    private URI getEffectiveURI(URI uri) {
        URI effectiveURI = null;
        try {
            effectiveURI = new URI("http",
                                   uri.getHost(),
                                   null,  // 路径组件
                                   null,  // 查询组件
                                   null   // 片段组件
                                  );
        } catch (URISyntaxException ignored) {
            effectiveURI = uri;
        }

        return effectiveURI;
    }
}
