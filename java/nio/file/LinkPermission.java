/*
 * 版权所有 (c) 2007, 2020, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

import java.security.BasicPermission;

/**
 * 用于链接创建操作的 {@code Permission} 类。
 *
 * <p> 下表提供了权限允许的内容的简要描述，并讨论了授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5
 *        summary="表格显示权限目标名称、权限允许的内容以及相关风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的内容</th>
 * <th>允许此权限的风险</th>
 * </tr>
 * <tr>
 *   <td>hard</td>
 *   <td> 将现有文件添加到目录的能力。这有时被称为创建链接或硬链接。 </td>
 *   <td> 授予此权限时应极其小心。它允许链接到文件系统中的任何文件或目录，从而允许攻击者访问所有文件。 </td>
 * </tr>
 * <tr>
 *   <td>symbolic</td>
 *   <td> 创建符号链接的能力。 </td>
 *   <td> 授予此权限时应极其小心。它允许链接到文件系统中的任何文件或目录，从而允许攻击者访问所有文件。 </td>
 * </tr>
 * </table>
 *
 * @since 1.7
 *
 * @see Files#createLink
 * @see Files#createSymbolicLink
 */
public final class LinkPermission extends BasicPermission {
    static final long serialVersionUID = -1441492453772213220L;

    private void checkName(String name) {
        if (!name.equals("hard") && !name.equals("symbolic")) {
            throw new IllegalArgumentException("name: " + name);
        }
    }

    /**
     * 使用指定的名称构造一个 {@code LinkPermission}。
     *
     * @param   name
     *          权限的名称。必须是 "hard" 或 "symbolic"。
     *
     * @throws  IllegalArgumentException
     *          如果名称为空或无效
     */
    public LinkPermission(String name) {
        super(name);
        checkName(name);
    }

    /**
     * 使用指定的名称和操作构造一个 {@code LinkPermission}。
     *
     * @param   name
     *          权限的名称；必须是 "hard" 或 "symbolic"。
     * @param   actions
     *          权限的操作；必须是空字符串或 {@code null}。
     *
     * @throws  IllegalArgumentException
     *          如果名称为空或无效，或操作是非空字符串
     */
    public LinkPermission(String name, String actions) {
        super(name);
        checkName(name);
        if (actions != null && !actions.isEmpty()) {
            throw new IllegalArgumentException("actions: " + actions);
        }
    }
}
