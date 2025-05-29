/*
 * 版权所有 (c) 2000, 2003, Oracle 和/或其附属公司。保留所有权利。
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


package java.util.logging;

import java.security.*;

/**
 * 当运行带有 SecurityManager 的代码调用日志控制方法（如 Logger.setLevel）时，
 * SecurityManager 将检查的权限。
 * <p>
 * 目前只有一个名为 LoggingPermission 的权限。这是 "control"，它授予控制日志配置的能力，
 * 例如通过添加或删除 Handlers，通过添加或删除 Filters，或通过更改日志级别。
 * <p>
 * 程序员通常不会直接创建 LoggingPermission 对象。相反，它们是由安全策略代码基于读取
 * 安全策略文件创建的。
 *
 *
 * @since 1.4
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 */

public final class LoggingPermission extends java.security.BasicPermission {

    private static final long serialVersionUID = 63564341580231582L;

    /**
     * 创建一个新的 LoggingPermission 对象。
     *
     * @param name 权限名称。必须是 "control"。
     * @param actions 必须是 null 或空字符串。
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空或参数无效。
     */
    public LoggingPermission(String name, String actions) throws IllegalArgumentException {
        super(name);
        if (!name.equals("control")) {
            throw new IllegalArgumentException("name: " + name);
        }
        if (actions != null && actions.length() > 0) {
            throw new IllegalArgumentException("actions: " + actions);
        }
    }
}
