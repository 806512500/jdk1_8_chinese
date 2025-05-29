/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 该类用于 Serializable 权限。SerializablePermission 包含一个名称（也称为“目标名称”），但
 * 没有操作列表；你要么拥有命名的权限，要么没有。
 *
 * <P>
 * 目标名称是 Serializable 权限的名称（见下文）。
 *
 * <P>
 * 下表列出了所有可能的 SerializablePermission 目标名称，并为每个名称提供了一个描述，说明该权限允许的内容
 * 以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="权限目标名称，权限允许的内容，以及相关风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的内容</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>enableSubclassImplementation</td>
 *   <td>ObjectOutputStream 或 ObjectInputStream 的子类实现
 * 覆盖对象的默认序列化或反序列化</td>
 *   <td>代码可以利用此权限以故意恶意的方式序列化或
 * 反序列化类。例如，在序列化过程中，恶意代码可以故意将
 * 机密的私有字段数据存储在攻击者容易访问的方式中。或者，在反序列化过程中，它可以将
 * 所有私有字段清零的类反序列化。</td>
 * </tr>
 *
 * <tr>
 *   <td>enableSubstitution</td>
 *   <td>在序列化或反序列化过程中
 * 用一个对象替换另一个对象</td>
 *   <td>这很危险，因为恶意代码
 * 可以用一个包含错误或恶意数据的对象替换实际对象。</td>
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
     * 创建一个具有指定名称的新 SerializablePermission。
     * 名称是 SerializablePermission 的符号名称，例如
     * "enableSubstitution" 等。
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
     * 创建一个具有指定名称的新 SerializablePermission 对象。
     * 名称是 SerializablePermission 的符号名称，而
     * 操作字符串目前未使用，应设置为 null。
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
