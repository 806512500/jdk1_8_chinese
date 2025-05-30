/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi;

import java.rmi.registry.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <code>Naming</code> 类提供了在远程对象注册表中存储和获取远程对象引用的方法。该类的每个方法都接受一个名称作为参数，该名称是一个没有方案部分的 URL 格式的 <code>java.lang.String</code>，形式如下：
 *
 * <PRE>
 *    //host:port/name
 * </PRE>
 *
 * <P>其中 <code>host</code> 是注册表所在的主机（远程或本地），<code>port</code> 是注册表接受调用的端口号，<code>name</code> 是注册表不解释的简单字符串。<code>host</code> 和 <code>port</code> 都是可选的。如果省略 <code>host</code>，则默认为主机本地。如果省略 <code>port</code>，则默认为 1099，这是 RMI 的注册表 <code>rmiregistry</code> 使用的“知名”端口。
 *
 * <P><em>绑定</em> 是为远程对象注册一个名称，以便将来可以使用该名称查找该远程对象。可以使用 <code>Naming</code> 类的 <code>bind</code> 或 <code>rebind</code> 方法将远程对象与名称关联。
 *
 * <P>一旦远程对象在本地主机的 RMI 注册表中注册（绑定），远程（或本地）主机上的调用者可以通过名称查找远程对象，获取其引用，然后调用远程对象的方法。注册表可以由主机上的所有服务器共享，或者单个服务器进程可以创建并使用自己的注册表（有关详细信息，请参阅 <code>java.rmi.registry.LocateRegistry.createRegistry</code> 方法）。
 *
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see     java.rmi.registry.Registry
 * @see     java.rmi.registry.LocateRegistry
 * @see     java.rmi.registry.LocateRegistry#createRegistry(int)
 */
public final class Naming {
    /**
     * 禁止创建此类的实例
     */
    private Naming() {}

    /**
     * 返回与指定 <code>name</code> 关联的远程对象的引用（存根）。
     *
     * @param name 一个没有方案部分的 URL 格式的名称
     * @return 远程对象的引用
     * @exception NotBoundException 如果名称未绑定
     * @exception RemoteException 如果无法联系注册表
     * @exception AccessException 如果此操作不被允许
     * @exception MalformedURLException 如果名称不是适当格式的 URL
     * @since JDK1.1
     */
    public static Remote lookup(String name)
        throws NotBoundException,
            java.net.MalformedURLException,
            RemoteException
    {
        ParsedNamingURL parsed = parseURL(name);
        Registry registry = getRegistry(parsed);

        if (parsed.name == null)
            return registry;
        return registry.lookup(parsed.name);
    }

    /**
     * 将指定的 <code>name</code> 绑定到远程对象。
     *
     * @param name 一个没有方案部分的 URL 格式的名称
     * @param obj 远程对象的引用（通常是存根）
     * @exception AlreadyBoundException 如果名称已绑定
     * @exception MalformedURLException 如果名称不是适当格式的 URL
     * @exception RemoteException 如果无法联系注册表
     * @exception AccessException 如果此操作不被允许（例如，如果来自非本地主机）
     * @since JDK1.1
     */
    public static void bind(String name, Remote obj)
        throws AlreadyBoundException,
            java.net.MalformedURLException,
            RemoteException
    {
        ParsedNamingURL parsed = parseURL(name);
        Registry registry = getRegistry(parsed);

        if (obj == null)
            throw new NullPointerException("不能绑定到 null");

        registry.bind(parsed.name, obj);
    }

    /**
     * 销毁与指定名称关联的远程对象的绑定。
     *
     * @param name 一个没有方案部分的 URL 格式的名称
     * @exception NotBoundException 如果名称未绑定
     * @exception MalformedURLException 如果名称不是适当格式的 URL
     * @exception RemoteException 如果无法联系注册表
     * @exception AccessException 如果此操作不被允许（例如，如果来自非本地主机）
     * @since JDK1.1
     */
    public static void unbind(String name)
        throws RemoteException,
            NotBoundException,
            java.net.MalformedURLException
    {
        ParsedNamingURL parsed = parseURL(name);
        Registry registry = getRegistry(parsed);

        registry.unbind(parsed.name);
    }

    /**
     * 将指定名称重新绑定到新的远程对象。任何现有的绑定将被替换。
     *
     * @param name 一个没有方案部分的 URL 格式的名称
     * @param obj 要与名称关联的新远程对象
     * @exception MalformedURLException 如果名称不是适当格式的 URL
     * @exception RemoteException 如果无法联系注册表
     * @exception AccessException 如果此操作不被允许（例如，如果来自非本地主机）
     * @since JDK1.1
     */
    public static void rebind(String name, Remote obj)
        throws RemoteException, java.net.MalformedURLException
    {
        ParsedNamingURL parsed = parseURL(name);
        Registry registry = getRegistry(parsed);

        if (obj == null)
            throw new NullPointerException("不能绑定到 null");

        registry.rebind(parsed.name, obj);
    }

    /**
     * 返回注册表中绑定的名称数组。名称是 URL 格式（没有方案部分）的字符串。数组包含调用时注册表中名称的快照。
     *
     * @param   name 一个没有方案部分的 URL 格式的注册表名称
     * @return  注册表中绑定的名称数组（格式适当）
     * @exception MalformedURLException 如果名称不是适当格式的 URL
     * @exception RemoteException 如果无法联系注册表。
     * @since JDK1.1
     */
    public static String[] list(String name)
        throws RemoteException, java.net.MalformedURLException
    {
        ParsedNamingURL parsed = parseURL(name);
        Registry registry = getRegistry(parsed);

        String prefix = "";
        if (parsed.port > 0 || !parsed.host.equals(""))
            prefix += "//" + parsed.host;
        if (parsed.port > 0)
            prefix += ":" + parsed.port;
        prefix += "/";

        String[] names = registry.list();
        for (int i = 0; i < names.length; i++) {
            names[i] = prefix + names[i];
        }
        return names;
    }

    /**
     * 从 URL 信息中获取注册表引用。
     */
    private static Registry getRegistry(ParsedNamingURL parsed)
        throws RemoteException
    {
        return LocateRegistry.getRegistry(parsed.host, parsed.port);
    }

    /**
     * 解析命名 URL 字符串以获取引用的主机、端口和对象名称。
     *
     * @return 一个包含上述各部分的对象。
     *
     * @exception MalformedURLException 如果给定的 URL 字符串格式错误
     */
    private static ParsedNamingURL parseURL(String str)
        throws MalformedURLException
    {
        try {
            return intParseURL(str);
        } catch (URISyntaxException ex) {
            /* 使用 RFC 3986 URI 处理，'rmi://:<port>' 和
             * '//:<port>' 形式将导致 URI 语法异常
             * 将权限转换为 localhost:<port> 形式
             */
            MalformedURLException mue = new MalformedURLException(
                "无效的 URL 字符串: " + str);
            mue.initCause(ex);
            int indexSchemeEnd = str.indexOf(':');
            int indexAuthorityBegin = str.indexOf("//:");
            if (indexAuthorityBegin < 0) {
                throw mue;
            }
            if ((indexAuthorityBegin == 0) ||
                    ((indexSchemeEnd > 0) &&
                    (indexAuthorityBegin == indexSchemeEnd + 1))) {
                int indexHostBegin = indexAuthorityBegin + 2;
                String newStr = str.substring(0, indexHostBegin) +
                                "localhost" +
                                str.substring(indexHostBegin);
                try {
                    return intParseURL(newStr);
                } catch (URISyntaxException inte) {
                    throw mue;
                } catch (MalformedURLException inte) {
                    throw inte;
                }
            }
            throw mue;
        }
    }

    private static ParsedNamingURL intParseURL(String str)
        throws MalformedURLException, URISyntaxException
    {
        URI uri = new URI(str);
        if (uri.isOpaque()) {
            throw new MalformedURLException(
                "不是分层 URL: " + str);
        }
        if (uri.getFragment() != null) {
            throw new MalformedURLException(
                "URL 名称中包含无效字符，'#': " + str);
        } else if (uri.getQuery() != null) {
            throw new MalformedURLException(
                "URL 名称中包含无效字符，'?': " + str);
        } else if (uri.getUserInfo() != null) {
            throw new MalformedURLException(
                "URL 主机中包含无效字符，'@': " + str);
        }
        String scheme = uri.getScheme();
        if (scheme != null && !scheme.equals("rmi")) {
            throw new MalformedURLException("无效的 URL 方案: " + str);
        }

        String name = uri.getPath();
        if (name != null) {
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (name.length() == 0) {
                name = null;
            }
        }

        String host = uri.getHost();
        if (host == null) {
            host = "";
            try {
                /*
                 * 使用 2396 URI 处理，'rmi://host:bar'
                 * 或 'rmi://:<port>' 形式将解析为基于注册表的权限。我们只允许基于服务器的命名权限。
                 */
                uri.parseServerAuthority();
            } catch (URISyntaxException use) {
                // 检查权限是否为 ':<port>' 形式
                String authority = uri.getAuthority();
                if (authority != null && authority.startsWith(":")) {
                    // 将权限转换为 'localhost:<port>' 形式
                    authority = "localhost" + authority;
                    try {
                        uri = new URI(null, authority, null, null, null);
                        // 确保现在解析为有效的基于服务器的命名权限
                        uri.parseServerAuthority();
                    } catch (URISyntaxException use2) {
                        throw new
                            MalformedURLException("无效的权限: " + str);
                    }
                } else {
                    throw new
                        MalformedURLException("无效的权限: " + str);
                }
            }
        }
        int port = uri.getPort();
        if (port == -1) {
            port = Registry.REGISTRY_PORT;
        }
        return new ParsedNamingURL(host, port, name);
    }

    /**
     * 简单类，用于启用多个 URL 返回值。
     */
    private static class ParsedNamingURL {
        String host;
        int port;
        String name;

        ParsedNamingURL(String host, int port, String name) {
            this.host = host;
            this.port = port;
            this.name = name;
        }
    }
}
