/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 该类用于各种网络权限。
 * NetPermission 包含一个名称（也称为“目标名称”），但没有操作列表；你要么拥有命名的权限，要么没有。
 * <P>
 * 目标名称是网络权限的名称（见下文）。命名约定遵循层次属性命名约定。
 * 此外，名称的末尾可以出现一个星号，星号可以出现在“.”之后，或者单独出现，以表示通配符匹配。例如：“foo.*”和“*”表示通配符匹配，而“*foo”和“a*b”则不表示。
 * <P>
 * 下表列出了所有可能的 NetPermission 目标名称，并为每个名称提供了权限允许的操作以及授予代码该权限的风险的描述。
 *
 * <table border=1 cellpadding=5 summary="Permission target name, what the permission allows, and associated risks">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 * <tr>
 *   <td>allowHttpTrace</td>
 *   <td>使用 HttpURLConnection 中的 HTTP TRACE 方法的能力。</td>
 *   <td>恶意代码使用 HTTP TRACE 可能会访问 HTTP 标头中的安全敏感信息（如 cookie），而这些信息它本来无法访问。</td>
 *   </tr>
 *
 * <tr>
 *   <td>getCookieHandler</td>
 *   <td>获取处理 Http 会话中高度安全敏感的 cookie 信息的 cookie 处理程序的能力。</td>
 *   <td>恶意代码可以获取 cookie 处理程序以访问高度安全敏感的 cookie 信息。某些 Web 服务器使用 cookie 保存用户的私有信息，如访问控制信息，或跟踪用户的浏览习惯。</td>
 *   </tr>
 *
 * <tr>
 *  <td>getNetworkInformation</td>
 *  <td>检索有关本地网络接口的所有信息的能力。</td>
 *  <td>恶意代码可以读取有关网络硬件（如 MAC 地址）的信息，这些信息可用于构建本地 IPv6 地址。</td>
 * </tr>
 *
 * <tr>
 *   <td>getProxySelector</td>
 *   <td>获取用于在网络连接时决定使用哪些代理的代理选择器的能力。</td>
 *   <td>恶意代码可以获取 ProxySelector 以发现内部网络上的代理主机和端口，这些主机和端口可能成为攻击的目标。</td>
 * </tr>
 *
 * <tr>
 *   <td>getResponseCache</td>
 *   <td>获取提供对本地响应缓存访问的响应缓存的能力。</td>
 *   <td>恶意代码获取对本地响应缓存的访问权限后，可以访问安全敏感信息。</td>
 *   </tr>
 *
 * <tr>
 *   <td>requestPasswordAuthentication</td>
 *   <td>请求系统注册的身份验证器提供密码的能力。</td>
 *   <td>恶意代码可能会窃取此密码。</td>
 * </tr>
 *
 * <tr>
 *   <td>setCookieHandler</td>
 *   <td>设置处理 Http 会话中高度安全敏感的 cookie 信息的 cookie 处理程序的能力。</td>
 *   <td>恶意代码可以设置 cookie 处理程序以访问高度安全敏感的 cookie 信息。某些 Web 服务器使用 cookie 保存用户的私有信息，如访问控制信息，或跟踪用户的浏览习惯。</td>
 *   </tr>
 *
 * <tr>
 *   <td>setDefaultAuthenticator</td>
 *   <td>设置当代理或 HTTP 服务器请求身份验证时检索身份验证信息的方式的能力。</td>
 *   <td>恶意代码可以设置一个监视并窃取用户身份验证输入的身份验证器，因为它从用户那里检索输入。</td>
 * </tr>
 *
 * <tr>
 *   <td>setProxySelector</td>
 *   <td>设置用于在网络连接时决定使用哪些代理的代理选择器的能力。</td>
 *   <td>恶意代码可以设置一个将网络流量定向到任意网络主机的 ProxySelector。</td>
 * </tr>
 *
 * <tr>
 *   <td>setResponseCache</td>
 *   <td>设置提供对本地响应缓存访问的响应缓存的能力。</td>
 *   <td>恶意代码获取对本地响应缓存的访问权限后，可以访问安全敏感信息，或在响应缓存中创建虚假条目。</td>
 *   </tr>
 *
 * <tr>
 *   <td>specifyStreamHandler</td>
 *   <td>在构造 URL 时指定流处理程序的能力。</td>
 *   <td>恶意代码可能创建一个它通常无法访问的资源的 URL（如 file:/foo/fum/），并指定一个从它有访问权限的地方获取实际字节的流处理程序。因此，它可能能够欺骗系统为一个类创建 ProtectionDomain/CodeSource，即使该类实际上并不是来自该位置。</td>
 * </tr>
 *
 * <tr>
 *   <th scope="row">setSocketImpl</th>
 *   <td>创建具有用户指定的 SocketImpl 的 Socket 或 ServerSocket 子类的能力。</td>
 *   <td>恶意用户定义的 SocketImpl 可以通过访问 SocketImpl 的受保护字段，以令人惊讶的方式改变 Socket 和 ServerSocket 的行为。</td>
 *   </tr>
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class NetPermission extends BasicPermission {
    private static final long serialVersionUID = -8343910153355041693L;

    /**
     * 创建一个具有指定名称的新 NetPermission。
     * 名称是 NetPermission 的符号名称，例如 "setDefaultAuthenticator" 等。名称的末尾可以出现一个星号，星号可以出现在“.”之后，或者单独出现，以表示通配符匹配。
     *
     * @param name NetPermission 的名称。
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */

    public NetPermission(String name)
    {
        super(name);
    }

    /**
     * 创建一个具有指定名称的新 NetPermission 对象。
     * 名称是 NetPermission 的符号名称，操作字符串目前未使用，应为 null。
     *
     * @param name NetPermission 的名称。
     * @param actions 应为 null。
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */

    public NetPermission(String name, String actions)
    {
        super(name, actions);
    }
}
