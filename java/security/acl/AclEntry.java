/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.acl;

import java.util.Enumeration;
import java.security.Principal;

/**
 * 用于表示访问控制列表 (ACL) 中的一个条目的接口。<p>
 *
 * ACL 可以被认为是一个包含多个 ACL 条目对象的数据结构。每个 ACL 条目对象包含与特定主体相关的一组权限。（主体表示一个实体，如单个用户或组）。此外，每个 ACL 条目被指定为正或负。如果为正，则权限将授予相关主体。如果为负，则权限将被拒绝。每个主体最多可以有一个正的 ACL 条目和一个负的条目；也就是说，不允许任何主体有多个正的或负的 ACL 条目。
 *
 * 注意：ACL 条目默认为正。只有当调用
 * {@link #setNegativePermissions() setNegativePermissions}
 * 方法时，条目才会变为负条目。
 *
 * @see java.security.acl.Acl
 *
 * @author      Satish Dharmaraj
 */
public interface AclEntry extends Cloneable {

    /**
     * 指定此 ACL 条目授予或拒绝权限的主体。如果此 ACL 条目已经设置了主体，则返回 false，否则返回 true。
     *
     * @param user 要为此条目设置的主体。
     *
     * @return 如果主体已设置，则返回 true；如果此条目已经设置了主体，则返回 false。
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
     * 将此 ACL 条目设置为负条目。也就是说，将拒绝与此条目关联的主体（例如，用户或组）在条目中指定的权限集。
     *
     * 注意：ACL 条目默认为正。只有当调用此 {@code setNegativePermissions}
     * 方法时，条目才会变为负条目。
     */
    public void setNegativePermissions();

    /**
     * 如果这是拒绝关联主体权限集的负 ACL 条目，则返回 true，否则返回 false。
     *
     * @return 如果这是负 ACL 条目，则返回 true，否则返回 false。
     */
    public boolean isNegative();

    /**
     * 将指定的权限添加到此 ACL 条目中。注意：一个条目可以有多个权限。
     *
     * @param permission 要与此条目中的主体关联的权限。
     *
     * @return 如果权限已添加，则返回 true；如果权限已经是此条目权限集的一部分，则返回 false。
     */
    public boolean addPermission(Permission permission);

    /**
     * 从此 ACL 条目中移除指定的权限。
     *
     * @param permission 要从此条目中移除的权限。
     *
     * @return 如果权限已移除，则返回 true；如果权限不是此条目权限集的一部分，则返回 false。
     */
    public boolean removePermission(Permission permission);

    /**
     * 检查指定的权限是否是此条目权限集的一部分。
     *
     * @param permission 要检查的权限。
     *
     * @return 如果权限是此条目权限集的一部分，则返回 true；否则返回 false。
     */
    public boolean checkPermission(Permission permission);

    /**
     * 返回此 ACL 条目中的权限枚举。
     *
     * @return 此 ACL 条目中的权限枚举。
     */
    public Enumeration<Permission> permissions();

    /**
     * 返回此 ACL 条目的内容的字符串表示形式。
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
