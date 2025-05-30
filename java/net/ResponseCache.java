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

import java.io.IOException;
import java.util.Map;
import java.util.List;
import sun.security.util.SecurityConstants;

/**
 * 表示 URLConnection 缓存的实现。此类的实例可以通过调用
 * ResponseCache.setDefault(ResponseCache) 注册到系统中，系统将调用
 * 此对象以执行以下操作：
 *
 *    <ul><li>将从外部源检索的资源数据存储到缓存中</li>
 *         <li>尝试从缓存中获取可能已存储的请求资源</li>
 *    </ul>
 *
 * ResponseCache 实现决定哪些资源应该被缓存，以及它们应该缓存多长时间。如果无法从缓存中检索请求资源，
 * 则协议处理程序将从其原始位置获取资源。
 *
 * URLConnection#useCaches 的设置控制是否允许协议使用缓存响应。
 *
 * 有关 HTTP 缓存的更多信息，请参阅 <a
 * href="http://www.ietf.org/rfc/rfc2616.txt"><i>RFC&nbsp;2616: Hypertext
 * Transfer Protocol -- HTTP/1.1</i></a>
 *
 * @author Yingxian Wang
 * @since 1.5
 */
public abstract class ResponseCache {

    /**
     * 提供对 URL 缓存机制的系统范围访问的缓存。
     *
     * @see #setDefault(ResponseCache)
     * @see #getDefault()
     */
    private static ResponseCache theResponseCache;

    /**
     * 获取系统范围的响应缓存。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     * {@link NetPermission}{@code ("getResponseCache")}
     *
     * @see #setDefault(ResponseCache)
     * @return 系统范围的 {@code ResponseCache}
     * @since 1.5
     */
    public synchronized  static ResponseCache getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_RESPONSECACHE_PERMISSION);
        }
        return theResponseCache;
    }

    /**
     * 设置（或取消设置）系统范围的缓存。
     *
     * 注意：非标准协议处理程序可能会忽略此设置。
     *
     * @param responseCache 响应缓存，或
     *          {@code null} 以取消设置缓存。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     * {@link NetPermission}{@code ("setResponseCache")}
     *
     * @see #getDefault()
     * @since 1.5
     */
    public synchronized static void setDefault(ResponseCache responseCache) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_RESPONSECACHE_PERMISSION);
        }
        theResponseCache = responseCache;
    }

    /**
     * 根据请求的 URI、请求方法和请求头检索缓存的响应。通常此方法由协议处理程序在发送请求
     * 获取网络资源之前调用。如果返回了缓存的响应，则使用该资源。
     *
     * @param uri 用于引用请求的网络资源的 {@code URI}
     * @param rqstMethod 表示请求方法的 {@code String}
     * @param rqstHeaders - 从请求头字段名称到字段值列表的映射，表示当前请求头
     * @return 如果缓存中可用，则返回 {@code CacheResponse} 实例，否则返回 null
     * @throws  IOException 如果发生 I/O 错误
     * @throws  IllegalArgumentException 如果任何参数为 null
     *
     * @see     java.net.URLConnection#setUseCaches(boolean)
     * @see     java.net.URLConnection#getUseCaches()
     * @see     java.net.URLConnection#setDefaultUseCaches(boolean)
     * @see     java.net.URLConnection#getDefaultUseCaches()
     */
    public abstract CacheResponse
        get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders)
        throws IOException;

    /**
     * 资源被检索后，协议处理程序调用此方法，ResponseCache 必须决定是否将资源存储在其缓存中。如果资源要被缓存，
     * 则 put() 必须返回一个 CacheRequest 对象，该对象包含一个 OutputStream，协议处理程序将使用该 OutputStream
     * 将资源写入缓存。如果资源不被缓存，则 put 必须返回 null。
     *
     * @param uri 用于引用请求的网络资源的 {@code URI}
     * @param conn - 用于获取要缓存的响应的 URLConnection 实例
     * @return 用于记录要缓存的响应的 {@code CacheRequest}。null 返回值表示调用者不打算缓存响应。
     * @throws IOException 如果发生 I/O 错误
     * @throws IllegalArgumentException 如果任何参数为 null
     */
    public abstract CacheRequest put(URI uri, URLConnection conn)  throws IOException;
}
