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

package java.lang.management;

/**
 * 当运行带有 SecurityManager 的代码调用 Java 平台管理接口中定义的方法时，SecurityManager 将检查的权限。
 * <P>
 * 下表提供了该权限允许的操作的简要描述，以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="表显示权限目标名称、权限允许的操作和相关风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>control</td>
 *   <td>控制 Java 虚拟机的运行时特性，例如，启用和禁用类加载或内存系统的详细输出，设置内存池的阈值，以及启用和禁用线程竞争监视支持。此权限控制的一些操作可能会泄露有关运行应用程序的信息，如 -verbose:class 标志。
 *   </td>
 *   <td>这允许攻击者控制 Java 虚拟机的运行时特性并导致系统行为异常。攻击者还可以访问有关运行应用程序的一些信息。
 *   </td>
 * </tr>
 * <tr>
 *   <td>monitor</td>
 *   <td>检索有关 Java 虚拟机的运行时信息，如线程堆栈跟踪、所有已加载类名列表和传递给 Java 虚拟机的输入参数。</td>
 *   <td>这允许恶意代码监视运行时信息并发现漏洞。</td>
 * </tr>
 *
 * </table>
 *
 * <p>
 * 程序员通常不会直接创建 ManagementPermission 对象。相反，它们是由安全策略代码根据读取的安全策略文件创建的。
 *
 * @author  Mandy Chung
 * @since   1.5
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 */

public final class ManagementPermission extends java.security.BasicPermission {
    private static final long serialVersionUID = 1897496590799378737L;

    /**
     * 构造一个具有指定名称的 ManagementPermission。
     *
     * @param name 权限名称。必须是 "monitor" 或 "control"。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空或无效。
     */
    public ManagementPermission(String name) {
        super(name);
        if (!name.equals("control") && !name.equals("monitor")) {
            throw new IllegalArgumentException("name: " + name);
        }
    }

    /**
     * 构造一个新的 ManagementPermission 对象。
     *
     * @param name 权限名称。必须是 "monitor" 或 "control"。
     * @param actions 必须为 null 或空字符串。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空或
     * 参数无效。
     */
    public ManagementPermission(String name, String actions)
        throws IllegalArgumentException {
        super(name);
        if (!name.equals("control") && !name.equals("monitor")) {
            throw new IllegalArgumentException("name: " + name);
        }
        if (actions != null && actions.length() > 0) {
            throw new IllegalArgumentException("actions: " + actions);
        }
    }
}
