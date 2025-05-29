/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * 这是用于表示访问控制列表（ACL）中一个条目的接口。<p>
 *
 * ACL 可以被认为是一个包含多个 ACL 条目对象的数据结构。每个 ACL 条目对象包含与特定主体（principal）相关联的一组权限。（主体表示一个实体，如个人用户或组）。此外，每个 ACL 条目被指定为正向或负向。如果是正向的，权限将被授予相关主体。如果是负向的，权限将被拒绝。每个主体最多可以有一个正向的 ACL 条目和一个负向的条目；也就是说，不允许任何主体有多个正向或负向的 ACL 条目。
 *
 * 注意：ACL 条目默认为正向。只有当调用
 * {@link #setNegativePermissions() setNegativePermissions}
 * 方法时，条目才会变成负向的。
 *
 * @see java.security.acl.Acl
 *
 * @author      Satish Dharmaraj
 */
public interface AclEntry extends Cloneable {

    /**
     * 指定此 ACL 条目授予或拒绝权限的主体。如果此 ACL 条目已经设置了一个主体，则返回 false，否则返回 true。
     *
     * @param user 要为此条目设置的主体。
     *
     * @return 如果主体已设置，则返回 true，如果此条目已经设置了一个主体，则返回 false。
     *
     * @see #getPrincipal
     */
    public boolean setPrincipal(Principal user);

    /**
     * 返回此 ACL 条目授予或拒绝权限的主体。如果此条目尚未设置主体，则返回 null。
     *
     * @return 与此条目关联的主体。
     *
     * @see #setPrincipal
     */
    public Principal getPrincipal();

    /**
     * 将此 ACL 条目设置为负向。也就是说，关联的主体（例如，用户或组）将被拒绝条目中指定的权限集。
     *
     * 注意：ACL 条目默认为正向。只有当调用此 {@code setNegativePermissions}
     * 方法时，条目才会变成负向的。
     */
    public void setNegativePermissions();

    /**
     * 如果这是负向的 ACL 条目（拒绝关联主体条目中的权限集），则返回 true，否则返回 false。
     *
     * @return 如果这是负向的 ACL 条目，则返回 true，如果不是，则返回 false。
     */
    public boolean isNegative();

    /**
     * 向此 ACL 条目添加指定的权限。注意：一个条目可以有多个权限。
     *
     * @param permission 要与此条目中的主体关联的权限。
     *
     * @return 如果权限已添加，则返回 true，如果权限已经是此条目权限集的一部分，则返回 false。
     */
    public boolean addPermission(Permission permission);

    /**
     * 从此 ACL 条目中移除指定的权限。
     *
     * @param permission 要从此条目中移除的权限。
     *
     * @return 如果权限已移除，则返回 true，如果权限不是此条目权限集的一部分，则返回 false。
     */
    public boolean removePermission(Permission permission);

    /**
     * 检查指定的权限是否是此条目权限集的一部分。
     *
     * @param permission 要检查的权限。
     *
     * @return 如果权限是此条目权限集的一部分，则返回 true，否则返回 false。
     */
    public boolean checkPermission(Permission permission);

    /**
     * 返回此 ACL 条目中的权限枚举。
     *
     * @return 此 ACL 条目中的权限枚举。
     */
    public Enumeration<Permission> permissions();

    /**
     * 返回此 ACL 条目内容的字符串表示形式。
     *
     * @return 内容的字符串表示形式。
     */
    public String toString();

    /**
     * 克隆此 ACL 条目。
     *
     * @return 此 ACL 条目的克隆。
     */
    public Object clone();
}
