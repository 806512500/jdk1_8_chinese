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
import java.util.List;
import sun.security.util.SecurityConstants;

/**
 * 选择连接到由 URL 引用的网络资源时使用的代理服务器。代理选择器是此类的具体子类，并通过调用
 * {@link java.net.ProxySelector#setDefault setDefault} 方法注册。当前注册的代理选择器可以通过调用
 * {@link java.net.ProxySelector#getDefault getDefault} 方法获取。
 *
 * <p> 当注册代理选择器时，例如，URLConnection 类的子类应该为每个 URL 请求调用 {@link #select select}
 * 方法，以便代理选择器可以决定是使用直接连接还是代理连接。{@link #select select} 方法返回一个集合的迭代器，
 * 该集合包含首选的连接方式。
 *
 * <p> 如果无法连接到代理（PROXY 或 SOCKS）服务器，则调用者应调用代理选择器的
 * {@link #connectFailed connectFailed} 方法，通知代理选择器该代理服务器不可用。 </p>
 *
 * <P>默认代理选择器强制执行与代理设置相关的
 * <a href="doc-files/net-properties.html#Proxies">一组系统属性</a>。</P>
 *
 * @author Yingxian Wang
 * @author Jean-Christophe Collet
 * @since 1.5
 */
public abstract class ProxySelector {
    /**
     * 选择连接到由 URL 引用的远程对象时使用的系统范围的代理选择器。
     *
     * @see #setDefault(ProxySelector)
     */
    private static ProxySelector theProxySelector;

    static {
        try {
            Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector");
            if (c != null && ProxySelector.class.isAssignableFrom(c)) {
                theProxySelector = (ProxySelector) c.newInstance();
            }
        } catch (Exception e) {
            theProxySelector = null;
        }
    }

    /**
     * 获取系统范围的代理选择器。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     * {@link NetPermission}{@code ("getProxySelector")}
     * @see #setDefault(ProxySelector)
     * @return 系统范围的 {@code ProxySelector}
     * @since 1.5
     */
    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PROXYSELECTOR_PERMISSION);
        }
        return theProxySelector;
    }

    /**
     * 设置（或取消设置）系统范围的代理选择器。
     *
     * 注意：非标准协议处理程序可能会忽略此设置。
     *
     * @param ps HTTP 代理选择器，或
     *          {@code null} 以取消设置代理选择器。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     * {@link NetPermission}{@code ("setProxySelector")}
     *
     * @see #getDefault()
     * @since 1.5
     */
    public static void setDefault(ProxySelector ps) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_PROXYSELECTOR_PERMISSION);
        }
        theProxySelector = ps;
    }

    /**
     * 根据访问资源的协议和访问资源的目标地址选择所有适用的代理。
     * URI 的格式定义如下：
     * <UL>
     * <LI>http URI 用于 http 连接</LI>
     * <LI>https URI 用于 https 连接</LI>
     * <LI>{@code socket://host:port}<br>
     *     用于 tcp 客户端套接字连接</LI>
     * </UL>
     *
     * @param   uri
     *          需要连接的 URI
     *
     * @return  代理的 List。List 中的每个元素都是
     *          {@link java.net.Proxy Proxy} 类型；
     *          当没有可用代理时，列表将包含一个
     *          {@link java.net.Proxy Proxy} 类型的元素，
     *          该元素表示直接连接。
     * @throws IllegalArgumentException 如果参数为 null
     */
    public abstract List<Proxy> select(URI uri);

    /**
     * 调用此方法表示无法建立到代理/socks 服务器的连接。此方法的实现可以临时移除代理或重新排序
     * {@link #select(URI)} 返回的代理序列，使用尝试连接时捕获的地址和 IOException。
     *
     * @param   uri
     *          代理在 sa 处无法服务的 URI。
     * @param   sa
     *          代理/SOCKS 服务器的套接字地址
     *
     * @param   ioe
     *          连接失败时抛出的 I/O 异常。
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public abstract void connectFailed(URI uri, SocketAddress sa, IOException ioe);
}
