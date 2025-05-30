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

import java.util.Map;
import java.util.List;
import java.io.IOException;
import sun.security.util.SecurityConstants;

/**
 * CookieHandler 对象提供了一种回调机制，用于将 HTTP 状态管理策略实现与 HTTP 协议处理程序挂钩。HTTP 状态管理机制指定了通过 HTTP 请求和响应创建有状态会话的方法。
 *
 * <p>可以通过调用 CookieHandler.setDefault(CookieHandler) 注册一个系统范围的 CookieHandler，HTTP 协议处理程序将使用它。当前注册的 CookieHandler 可以通过调用 CookieHandler.getDefault() 获取。
 *
 * 有关 HTTP 状态管理的更多信息，请参见 <a
 * href="http://www.ietf.org/rfc/rfc2965.txt"><i>RFC&nbsp;2965: HTTP
 * 状态管理机制</i></a>
 *
 * @author Yingxian Wang
 * @since 1.5
 */
public abstract class CookieHandler {
    /**
     * 系统范围的 Cookie 处理程序，用于将 Cookie 应用到请求头并管理响应头中的 Cookie。
     *
     * @see setDefault(CookieHandler)
     * @see getDefault()
     */
    private static CookieHandler cookieHandler;

    /**
     * 获取系统范围的 Cookie 处理程序。
     *
     * @return 系统范围的 Cookie 处理程序；如果当前未设置系统范围的 Cookie 处理程序，则返回 null。
     * @throws SecurityException
     *       如果已安装了安全经理，并且它拒绝了
     * {@link NetPermission}{@code ("getCookieHandler")}
     * @see #setDefault(CookieHandler)
     */
    public synchronized static CookieHandler getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_COOKIEHANDLER_PERMISSION);
        }
        return cookieHandler;
    }

    /**
     * 设置（或取消设置）系统范围的 Cookie 处理程序。
     *
     * 注意：非标准的 HTTP 协议处理程序可能会忽略此设置。
     *
     * @param cHandler HTTP Cookie 处理程序，或
     *       {@code null} 以取消设置。
     * @throws SecurityException
     *       如果已安装了安全经理，并且它拒绝了
     * {@link NetPermission}{@code ("setCookieHandler")}
     * @see #getDefault()
     */
    public synchronized static void setDefault(CookieHandler cHandler) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_COOKIEHANDLER_PERMISSION);
        }
        cookieHandler = cHandler;
    }

    /**
     * 从指定 URI 的请求头中获取所有适用的 Cookie。
     *
     * <P>传递的 {@code URI} 参数指定了 Cookie 的预期用途。特别是，方案应反映 Cookie 是否将通过 HTTP、HTTPS 或其他上下文（如 JavaScript）发送。主机部分应反映 Cookie 的目标或其在 JavaScript 中的来源。</P>
     * <P>实现应考虑 {@code URI} 和 Cookie 的属性及安全设置，以确定应返回哪些 Cookie。</P>
     *
     * <P>HTTP 协议实现者应确保在添加所有与选择 Cookie 相关的请求头后，且在发送请求之前调用此方法。</P>
     *
     * @param uri 一个 {@code URI}，表示 Cookie 的预期用途
     * @param requestHeaders - 从请求头字段名称到字段值列表的映射，表示当前请求头
     * @return 一个不可变的映射，从状态管理头（字段名称为 "Cookie" 或 "Cookie2"）到包含状态信息的 Cookie 列表
     *
     * @throws IOException 如果发生 I/O 错误
     * @throws IllegalArgumentException 如果任一参数为 null
     * @see #put(URI, Map)
     */
    public abstract Map<String, List<String>>
        get(URI uri, Map<String, List<String>> requestHeaders)
        throws IOException;

    /**
     * 将所有适用的 Cookie 设置到 Cookie 缓存中，例如响应头中名为 Set-Cookie2 的字段。
     *
     * @param uri Cookie 来源的 {@code URI}
     * @param responseHeaders 从字段名称到字段值列表的不可变映射，表示返回的响应头字段
     * @throws  IOException 如果发生 I/O 错误
     * @throws  IllegalArgumentException 如果任一参数为 null
     * @see #get(URI, Map)
     */
    public abstract void
        put(URI uri, Map<String, List<String>> responseHeaders)
        throws IOException;
}
