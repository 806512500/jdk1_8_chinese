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

package java.security;

import java.util.*;

/**
 * 表示一组 Permission 对象的抽象类。
 *
 * <p>使用 PermissionCollection，您可以：
 * <UL>
 * <LI> 使用 {@code add} 方法将权限添加到集合中。
 * <LI> 使用 {@code implies} 方法检查特定权限是否隐含在集合中。
 * <LI> 使用 {@code elements} 方法枚举所有权限。
 * </UL>
 *
 * <p>当需要将多个相同类型的 Permission 对象分组时，应首先调用该特定类型 Permission 对象的
 * {@code newPermissionCollection} 方法。默认行为（来自 Permission 类）是简单地返回 null。
 * Permission 类的子类会覆盖该方法，如果它们需要将权限存储在特定的 PermissionCollection 对象中
 * 以在调用 {@code PermissionCollection.implies} 方法时提供正确的语义。
 * 如果返回非 null 值，则必须使用该 PermissionCollection。
 * 如果返回 null，则 {@code newPermissionCollection} 的调用者可以自由选择
 * 存储给定类型的权限的任何 PermissionCollection（使用 Hashtable，使用 Vector 等）。
 *
 * <p>由 {@code Permission.newPermissionCollection} 方法返回的 PermissionCollection
 * 是一个同质集合，仅存储给定 Permission 类型的 Permission 对象。PermissionCollection 也可以是异质的。
 * 例如，Permissions 是 PermissionCollection 的一个子类，表示一组 PermissionCollection。
 * 即，其成员每个都是一个同质的 PermissionCollection。
 * 例如，Permissions 对象可能有一个 FilePermissionCollection 用于所有 FilePermission 对象，
 * 一个 SocketPermissionCollection 用于所有 SocketPermission 对象，等等。其 {@code add} 方法
 * 将权限添加到适当的集合中。
 *
 * <p>每当向异质 PermissionCollection（如 Permissions）添加权限时，如果 PermissionCollection 尚未包含
 * 指定权限类型的 PermissionCollection，PermissionCollection 应调用权限类的
 * {@code newPermissionCollection} 方法，以查看是否需要特殊的 PermissionCollection。
 * 如果 {@code newPermissionCollection} 返回 null，PermissionCollection
 * 可以自由选择将权限存储在任何类型的 PermissionCollection 中
 * （使用 Hashtable，使用 Vector 等）。例如，Permissions 对象使用默认的 PermissionCollection 实现，
 * 该实现将权限对象存储在 Hashtable 中。
 *
 * <p> PermissionCollection 的子类实现应假设它们可能同时从多个线程调用，
 * 因此应正确同步。此外，通过 {@code elements} 方法返回的枚举不是 <em>快速失败</em> 的。
 * 不应在枚举集合时对集合进行修改。
 *
 * @see Permission
 * @see Permissions
 *
 *
 * @author Roland Schemers
 */

public abstract class PermissionCollection implements java.io.Serializable {

    private static final long serialVersionUID = -6727011328946861783L;

    // 当设置时，add 将抛出异常。
    private volatile boolean readOnly;

    /**
     * 将权限对象添加到当前权限对象集合中。
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception SecurityException - 如果此 PermissionCollection 对象
     *                                 已被标记为只读
     * @exception IllegalArgumentException - 如果此 PermissionCollection
     *                对象是同质集合且权限不是正确类型。
     */
    public abstract void add(Permission permission);

    /**
     * 检查指定的权限是否被此 PermissionCollection 中持有的权限对象集合隐含。
     *
     * @param permission 要比较的 Permission 对象。
     *
     * @return 如果 "permission" 被集合中的权限隐含，则返回 true，否则返回 false。
     */
    public abstract boolean implies(Permission permission);

    /**
     * 返回集合中所有 Permission 对象的枚举。
     *
     * @return 所有权限的枚举。
     */
    public abstract Enumeration<Permission> elements();

    /**
     * 将此 PermissionCollection 对象标记为“只读”。一旦
     * PermissionCollection 对象被标记为只读，就不能再使用 {@code add} 向其添加新的 Permission 对象。
     */
    public void setReadOnly() {
        readOnly = true;
    }

    /**
     * 如果此 PermissionCollection 对象被标记为只读，则返回 true。
     * 如果是只读的，就不能再使用 {@code add} 向其添加新的 Permission 对象。
     *
     * <p>默认情况下，对象 <i>不是</i> 只读的。可以通过调用 {@code setReadOnly} 将其设置为只读。
     *
     * @return 如果此 PermissionCollection 对象被标记为只读，则返回 true，否则返回 false。
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * 返回描述此 PermissionCollection 对象的字符串，
     * 提供关于它包含的所有权限的信息。格式为：
     * <pre>
     * super.toString() (
     *   // 枚举所有 Permission
     *   // 对象并调用 toString() 方法，
     *   // 每个对象一行..
     * )</pre>
     *
     * {@code super.toString} 是对此对象的超类（即 Object）的 {@code toString}
     * 方法的调用。结果是此 PermissionCollection 的类型名称加上此对象的哈希码，
     * 从而允许客户端区分不同的 PermissionCollections 对象，即使它们包含相同的权限。
     *
     * @return 如上所述的关于此 PermissionCollection 对象的信息。
     *
     */
    public String toString() {
        Enumeration<Permission> enum_ = elements();
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()+" (\n");
        while (enum_.hasMoreElements()) {
            try {
                sb.append(" ");
                sb.append(enum_.nextElement().toString());
                sb.append("\n");
            } catch (NoSuchElementException e){
                // 忽略
            }
        }
        sb.append(")\n");
        return sb.toString();
    }
}
