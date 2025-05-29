/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
 * <b>注意：</b> 授予 AllPermission 应当极其谨慎，
 * 因为它隐含所有其他权限。因此，它授予代码以禁用安全性的能力
 * 在授予此类权限之前应极其谨慎。此权限仅应在测试期间使用，
 * 或在极少数情况下，应用程序或小程序完全可信，且向策略添加必要的权限
 * 过于繁琐。
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
     * 创建一个新的 AllPermission 对象。此
     * 构造函数用于 {@code Policy} 对象
     * 实例化新的 Permission 对象。
     *
     * @param name 忽略
     * @param actions 忽略。
     */
    public AllPermission(String name, String actions) {
        this();
    }

    /**
     * 检查指定的权限是否被
     * 此对象“隐含”。此方法始终返回 true。
     *
     * @param p 要检查的权限。
     *
     * @return 返回
     */
    public boolean implies(Permission p) {
         return true;
    }

    /**
     * 检查两个 AllPermission 对象是否相等。两个 AllPermission
     * 对象始终相等。
     *
     * @param obj 要测试与本对象相等的对象。
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
     * 返回动作的规范字符串表示。
     *
     * @return 动作。
     */
    public String getActions() {
        return "<all actions>";
    }

    /**
     * 返回一个用于存储 AllPermission
     * 对象的新 PermissionCollection 对象。
     * <p>
     *
     * @return 一个适合存储 AllPermissions 的新 PermissionCollection 对象。
     */
    public PermissionCollection newPermissionCollection() {
        return new AllPermissionCollection();
    }

}

/**
 * AllPermissionCollection 存储一个
 * AllPermission 权限的集合。AllPermission 对象
 * 必须以允许它们按任意顺序插入的方式存储，但使 implies 函数能够以高效（且一致）的方式
 * 评估 implies 方法。
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

    // 使用 JDK 1.2.2 的 serialVersionUID 以确保互操作性
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
     * 向 AllPermissions 添加一个权限。哈希的键是
     * permission.path。
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception IllegalArgumentException - 如果权限不是
     *                                       AllPermission
     *
     * @exception SecurityException - 如果此 AllPermissionCollection 对象
     *                                已被标记为只读
     */

    public void add(Permission permission) {
        if (! (permission instanceof AllPermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException("尝试向只读 PermissionCollection 添加权限");

        all_allowed = true; // 无需同步；陈旧性可接受
    }

    /**
     * 检查并查看此权限集是否隐含
     * “permission”中表达的权限。
     *
     * @param permission 要比较的 Permission 对象
     *
     * @return 始终返回 true。
     */

    public boolean implies(Permission permission) {
        return all_allowed; // 无需同步；陈旧性可接受
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
