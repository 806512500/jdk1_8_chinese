/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import sun.security.util.SecurityConstants;

/**
 * AllPermission 是一个隐含所有其他权限的权限。
 * <p>
 * <b>注意：</b>授予 AllPermission 时应极其谨慎，因为它隐含所有其他权限。因此，它授予代码在没有安全性的状态下运行的能力。
 * 在授予此类权限时应极其谨慎。此权限仅应在测试期间或在极其罕见的情况下使用，即应用程序或小程序完全可信，并且将必要的权限添加到策略中过于繁琐。
 *
 * @see java.security.Permission
 * @see java.security.AccessController
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Roland Schemers
 *
 * @serial exclude
 */

public final class AllPermission extends Permission {

    private static final long serialVersionUID = -2916474571451318075L;

    /**
     * 创建一个新的 AllPermission 对象。
     */
    public AllPermission() {
        super("<all permissions>");
    }


    /**
     * 创建一个新的 AllPermission 对象。此构造函数用于由 {@code Policy} 对象实例化新的 Permission 对象。
     *
     * @param name 忽略
     * @param actions 忽略。
     */
    public AllPermission(String name, String actions) {
        this();
    }

    /**
     * 检查指定的权限是否被此对象“隐含”。此方法始终返回 true。
     *
     * @param p 要检查的权限。
     *
     * @return 返回
     */
    public boolean implies(Permission p) {
         return true;
    }

    /**
     * 检查两个 AllPermission 对象是否相等。两个 AllPermission 对象始终相等。
     *
     * @param obj 要测试是否与此对象相等的对象。
     * @return 如果 <i>obj</i> 是一个 AllPermission，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        return (obj instanceof AllPermission);
    }

    /**
     * 返回此对象的哈希码值。
     *
     * @return 此对象的哈希码值。
     */

    public int hashCode() {
        return 1;
    }

    /**
     * 返回动作的规范字符串表示形式。
     *
     * @return 动作。
     */
    public String getActions() {
        return "<all actions>";
    }

    /**
     * 返回一个用于存储 AllPermission 对象的新 PermissionCollection 对象。
     * <p>
     *
     * @return 一个适合存储 AllPermissions 的新 PermissionCollection 对象。
     */
    public PermissionCollection newPermissionCollection() {
        return new AllPermissionCollection();
    }

}

/**
 * AllPermissionCollection 存储一组 AllPermission 权限。AllPermission 对象必须以允许它们按任何顺序插入的方式存储，
 * 但使 implies 函数能够高效（且一致）地评估 implies 方法。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 *
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class AllPermissionCollection
    extends PermissionCollection
    implements java.io.Serializable
{

    // 为了互操作性，使用 JDK 1.2.2 的 serialVersionUID
    private static final long serialVersionUID = -4023755556366636806L;

    private boolean all_allowed; // 如果已添加任何 all 权限，则为 true

    /**
     * 创建一个空的 AllPermissions 对象。
     *
     */

    public AllPermissionCollection() {
        all_allowed = false;
    }

    /**
     * 向 AllPermissions 中添加一个权限。哈希的键是 permission.path。
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception IllegalArgumentException - 如果权限不是 AllPermission
     *
     * @exception SecurityException - 如果此 AllPermissionCollection 对象已被标记为只读
     */

    public void add(Permission permission) {
        if (! (permission instanceof AllPermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("尝试向只读 PermissionCollection 添加权限");

        all_allowed = true; // 不同步；陈旧性可接受
    }

    /**
     * 检查并查看此权限集是否隐含“permission”中表达的权限。
     *
     * @param permission 要比较的 Permission 对象。
     *
     * @return 始终返回 true。
     */

    public boolean implies(Permission permission) {
        return all_allowed; // 不同步；陈旧性可接受
    }

    /**
     * 返回容器中所有 AllPermission 对象的枚举。
     *
     * @return 所有 AllPermission 对象的枚举。
     */
    public Enumeration<Permission> elements() {
        return new Enumeration<Permission>() {
            private boolean hasMore = all_allowed;

            public boolean hasMoreElements() {
                return hasMore;
            }

            public Permission nextElement() {
                hasMore = false;
                return SecurityConstants.ALL_PERMISSION;
            }
        };
    }
}
