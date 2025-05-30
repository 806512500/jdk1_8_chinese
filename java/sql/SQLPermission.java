/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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


package java.sql;

import java.security.*;

/**
 * 当启用了 <code>SecurityManager</code> 的应用程序代码调用
 * {@code DriverManager.deregisterDriver} 方法、
 * <code>DriverManager.setLogWriter</code> 方法、
 * <code>DriverManager.setLogStream</code>（已弃用）方法、
 * {@code SyncFactory.setJNDIContext} 方法、
 * {@code SyncFactory.setLogger} 方法、
 * {@code Connection.setNetworktimeout} 方法或
 * <code>Connection.abort</code> 方法时，
 * <code>SecurityManager</code> 将检查的权限。
 * 如果没有 <code>SQLPermission</code> 对象，这些方法将抛出
 * <code>java.lang.SecurityException</code> 运行时异常。
 * <P>
 * <code>SQLPermission</code> 对象包含一个名称（也称为“目标名称”），但没有操作列表；
 * 要么存在命名权限，要么不存在。
 * 目标名称是权限的名称（见下文）。命名约定遵循分层属性命名约定。
 * 此外，名称末尾可以出现一个星号，紧跟在“.”之后，或单独出现，表示通配符匹配。
 * 例如： <code>loadLibrary.*</code> 和 <code>*</code> 表示通配符匹配，
 * 而 <code>*loadLibrary</code> 和 <code>a*b</code> 则不表示通配符匹配。
 * <P>
 * 下表列出了所有可能的 <code>SQLPermission</code> 目标名称。
 * 表格描述了权限允许的操作以及授予代码此权限的风险。
 *
 *
 * <table border=1 cellpadding=5 summary="permission target name, what the permission allows, and associated risks">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>setLog</td>
 *   <td>设置日志流</td>
 *   <td>这是一个危险的权限。日志内容可能包含用户名和密码、
 * SQL 语句和 SQL 数据。</td>
 * </tr>
 * <tr>
 * <td>callAbort</td>
 *   <td>允许调用 {@code Connection} 方法 {@code abort}</td>
 *   <td>允许应用程序终止与数据库的物理连接。</td>
 * </tr>
 * <tr>
 * <td>setSyncFactory</td>
 *   <td>允许调用 {@code SyncFactory} 方法 {@code setJNDIContext} 和 {@code setLogger}</td>
 *   <td>允许应用程序指定可以从其中检索 {@code SyncProvider} 实现的 JNDI 上下文以及
 * {@code SyncProvider} 实现使用的日志对象。</td>
 * </tr>
 *
 * <tr>
 * <td>setNetworkTimeout</td>
 *   <td>允许调用 {@code Connection} 方法 {@code setNetworkTimeout}</td>
 *   <td>允许应用程序指定 <code>Connection</code> 或
 * 从 <code>Connection</code> 创建的对象等待数据库响应任何单个请求的最大时间。</td>
 * <tr>
 * <td>deregisterDriver</td>
 *   <td>允许调用 {@code DriverManager} 方法 {@code deregisterDriver}</td>
 *   <td>允许应用程序从已注册的驱动程序列表中移除 JDBC 驱动程序并释放其资源。</td>
 * </tr>
 * </table>
 *<p>
 * @since 1.3
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 */

public final class SQLPermission extends BasicPermission {

    /**
     * 创建一个具有指定名称的新 <code>SQLPermission</code> 对象。
     * 名称是 <code>SQLPermission</code> 的符号名称。
     *
     * @param name 此 <code>SQLPermission</code> 对象的名称，必须是
     * {@code  setLog}、{@code callAbort}、{@code setSyncFactory}、
     *  {@code deregisterDriver} 或 {@code setNetworkTimeout}
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public SQLPermission(String name) {
        super(name);
    }

    /**
     * 创建一个具有指定名称的新 <code>SQLPermission</code> 对象。
     * 名称是 <code>SQLPermission</code> 的符号名称；操作 <code>String</code>
     * 当前未使用，应为 <code>null</code>。
     *
     * @param name 此 <code>SQLPermission</code> 对象的名称，必须是
     * {@code  setLog}、{@code callAbort}、{@code setSyncFactory}、
     *  {@code deregisterDriver} 或 {@code setNetworkTimeout}
     * @param actions 应为 <code>null</code>
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public SQLPermission(String name, String actions) {
        super(name, actions);
    }

    /**
     * 私有序列化版本唯一 ID，以确保序列化兼容性。
     */
    static final long serialVersionUID = -1439323187199563495L;

}
