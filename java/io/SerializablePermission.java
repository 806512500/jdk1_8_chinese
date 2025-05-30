/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 该类用于 Serializable 权限。一个 SerializablePermission 包含一个名称（也称为“目标名称”），但没有动作列表；你要么拥有命名的权限，要么没有。
 *
 * <P>
 * 目标名称是 Serializable 权限的名称（见下文）。
 *
 * <P>
 * 下表列出了所有可能的 SerializablePermission 目标名称，并为每个名称提供了一个描述，说明该权限允许什么，以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="Permission target name, what the permission allows, and associated risks">
 * <tr>
 * <th>权限目标名称</th>
 * <th>该权限允许的内容</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>enableSubclassImplementation</td>
 *   <td>子类实现 ObjectOutputStream 或 ObjectInputStream 以分别覆盖对象的默认序列化或反序列化</td>
 *   <td>代码可以利用此权限以故意恶意的方式序列化或反序列化类。例如，在序列化过程中，恶意代码可以故意以攻击者容易访问的方式存储机密的私有字段数据。或者，在反序列化过程中，它可以将类的所有私有字段反序列化为零。</td>
 * </tr>
 *
 * <tr>
 *   <td>enableSubstitution</td>
 *   <td>在序列化或反序列化过程中用一个对象替换另一个对象</td>
 *   <td>这是危险的，因为恶意代码可以将实际对象替换为包含错误或恶意数据的对象。</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Joe Fialli
 * @since 1.2
 */

/* 代码最初是从 java.lang.RuntimePermission 借用的。 */

public final class SerializablePermission extends BasicPermission {

    private static final long serialVersionUID = 8537212141160296410L;

    /**
     * @serial
     */
    private String actions;

    /**
     * 创建一个具有指定名称的新 SerializablePermission。名称是 SerializablePermission 的符号名称，例如 "enableSubstitution" 等。
     *
     * @param name SerializablePermission 的名称。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */
    public SerializablePermission(String name)
    {
        super(name);
    }

    /**
     * 创建一个具有指定名称的新 SerializablePermission 对象。名称是 SerializablePermission 的符号名称，动作字符串目前未使用，应为 null。
     *
     * @param name SerializablePermission 的名称。
     * @param actions 目前未使用，必须设置为 null
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public SerializablePermission(String name, String actions)
    {
        super(name, actions);
    }
}
