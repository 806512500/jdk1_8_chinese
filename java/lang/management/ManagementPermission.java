/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其关联公司。保留所有权利。
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

package java.lang.management;

/**
 * 当运行带有 SecurityManager 的代码调用 Java 平台管理接口中定义的方法时，SecurityManager 将检查的权限。
 * <P>
 * 下表提供了该权限允许的操作的简要描述，并讨论了授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="表格显示权限目标名称、权限允许的操作及关联风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>control</td>
 *   <td>控制 Java 虚拟机的运行时特性，例如，启用和禁用类加载或内存系统的详细输出，设置内存池的阈值，以及启用和禁用线程竞争监视支持。受此权限控制的一些操作可以披露关于运行应用程序的信息，如 -verbose:class 标志。
 *   </td>
 *   <td>这允许攻击者控制 Java 虚拟机的运行时特性并导致系统行为异常。攻击者还可以访问与运行应用程序相关的一些信息。
 *   </td>
 * </tr>
 * <tr>
 *   <td>monitor</td>
 *   <td>获取关于 Java 虚拟机的运行时信息，如线程堆栈跟踪、所有已加载类名列表和 Java 虚拟机的输入参数。</td>
 *   <td>这允许恶意代码监控运行时信息并发现漏洞。</td>
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
     * 使用指定的名称构造 ManagementPermission。
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
