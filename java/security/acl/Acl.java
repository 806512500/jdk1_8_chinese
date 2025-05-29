/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.acl;

import java.util.Enumeration;
import java.security.Principal;

/**
 * 表示访问控制列表 (ACL) 的接口。访问控制列表是一种用于保护资源访问的数据结构。<p>
 *
 * ACL 可以被视为包含多个 ACL 条目的数据结构。每个 ACL 条目，类型为 AclEntry 接口，包含与特定主体相关联的一组权限。（主体表示一个实体，如单个用户或组）。此外，每个 ACL 条目被指定为正或负。如果为正，则权限将授予相关主体。如果为负，则权限将被拒绝。<p>
 *
 * 每个 ACL 中的 ACL 条目遵循以下规则：
 *
 * <ul> <li>每个主体最多可以有一个正的 ACL 条目和一个负的条目；也就是说，不允许任何主体有多个正的或负的 ACL 条目。每个条目指定要授予（如果是正的）或拒绝（如果是负的）的权限集。
 *
 * <li>如果特定主体没有条目，则认为该主体具有空（空）权限集。
 *
 * <li>如果有一个正的条目授予主体特定权限，同时有一个负的条目拒绝该主体相同的权限，结果就像该权限从未被授予或拒绝一样。
 *
 * <li>个人权限总是优先于该个人所属的组的权限。也就是说，个人的否定权限（特定权限的拒绝）优先于组的正权限。而个人的正权限优先于组的否定权限。
 *
 * </ul>
 *
 * {@code  java.security.acl } 包提供了 ACL 及相关数据结构（ACL 条目、组、权限等）的接口，而 {@code  sun.security.acl } 类提供了接口的默认实现。例如，{@code  java.security.acl.Acl } 提供了 ACL 的接口，而 {@code  sun.security.acl.AclImpl } 类提供了接口的默认实现。<p>
 *
 * {@code  java.security.acl.Acl } 接口扩展了 {@code  java.security.acl.Owner } 接口。Owner 接口用于维护每个 ACL 的所有者列表。只有所有者才允许修改 ACL。例如，只有所有者可以调用 ACL 的 {@code addEntry} 方法向 ACL 添加新的 ACL 条目。
 *
 * @see java.security.acl.AclEntry
 * @see java.security.acl.Owner
 * @see java.security.acl.Acl#getPermissions
 *
 * @author Satish Dharmaraj
 */

public interface Acl extends Owner {

    /**
     * 设置此 ACL 的名称。
     *
     * @param caller 调用此方法的主体。必须是此 ACL 的所有者。
     *
     * @param name 要赋予此 ACL 的名称。
     *
     * @exception NotOwnerException 如果调用者主体不是此 ACL 的所有者。
     *
     * @see #getName
     */
    public void setName(Principal caller, String name)
      throws NotOwnerException;

    /**
     * 返回此 ACL 的名称。
     *
     * @return 此 ACL 的名称。
     *
     * @see #setName
     */
    public String getName();

    /**
     * 向此 ACL 添加一个 ACL 条目。条目将主体（例如，个人或组）与一组权限关联。每个主体最多可以有一个正的 ACL 条目（指定要授予主体的权限）和一个负的 ACL 条目（指定要拒绝的权限）。如果 ACL 中已经存在相同类型的条目（负或正），则返回 false。
     *
     * @param caller 调用此方法的主体。必须是此 ACL 的所有者。
     *
     * @param entry 要添加到此 ACL 的 ACL 条目。
     *
     * @return 成功时返回 true，如果已经存在相同类型的条目（正或负）则返回 false。
     *
     * @exception NotOwnerException 如果调用者主体不是此 ACL 的所有者。
     */
    public boolean addEntry(Principal caller, AclEntry entry)
      throws NotOwnerException;

    /**
     * 从此 ACL 中移除一个 ACL 条目。
     *
     * @param caller 调用此方法的主体。必须是此 ACL 的所有者。
     *
     * @param entry 要从此 ACL 中移除的 ACL 条目。
     *
     * @return 成功时返回 true，如果条目不是此 ACL 的一部分则返回 false。
     *
     * @exception NotOwnerException 如果调用者主体不是此 ACL 的所有者。
     */
    public boolean removeEntry(Principal caller, AclEntry entry)
          throws NotOwnerException;

    /**
     * 返回指定主体（表示一个实体，如个人或组）的允许权限集的枚举。此允许权限集的计算方法如下：
     *
     * <ul>
     *
     * <li>如果此访问控制列表中没有指定主体的条目，则返回空权限集。
     *
     * <li>否则，确定主体的组权限集。（主体可以属于一个或多个组，其中组是一组主体，由 Group 接口表示。）组正权限集是主体所属的每个组的所有正权限的并集。组负权限集是主体所属的每个组的所有负权限的并集。如果某个特定权限同时出现在正权限集和负权限集中，则从两个集中移除该权限。<p>
     *
     * 也确定个人的正权限集和负权限集。正权限集包含 ACL 中主体的正条目（如果有）中指定的权限。同样，负权限集包含 ACL 中主体的负条目（如果有）中指定的权限。如果此 ACL 中没有主体的正（负）ACL 条目，则认为个人的正（负）权限集为空。<p>
     *
     * 然后使用简单的规则计算授予主体的权限集，即个人权限总是优先于组权限。也就是说，主体的个人负权限集（特定权限的拒绝）优先于组的正权限集，而主体的个人正权限集优先于组的负权限集。
     *
     * </ul>
     *
     * @param user 要返回权限集的主体。
     *
     * @return 指定主体允许的权限集。
     */
    public Enumeration<Permission> getPermissions(Principal user);

                /**
     * 返回此 ACL 中的条目枚举。枚举中的每个元素都是 AclEntry 类型。
     *
     * @return 此 ACL 中的条目枚举。
     */
    public Enumeration<AclEntry> entries();

    /**
     * 检查指定的主体是否具有指定的权限。如果具有，返回 true，否则返回 false。
     *
     * 更具体地说，此方法检查传递的权限是否是指定主体的允许权限集的成员。
     * 允许的权限集是通过与 {@code getPermissions} 方法使用的相同算法确定的。
     *
     * @param principal 假定为有效的已验证主体。
     *
     * @param permission 要检查的权限。
     *
     * @return 如果主体具有指定的权限，返回 true，否则返回 false。
     *
     * @see #getPermissions
     */
    public boolean checkPermission(Principal principal, Permission permission);

    /**
     * 返回 ACL 内容的字符串表示形式。
     *
     * @return ACL 内容的字符串表示形式。
     */
    public String toString();
}
