/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.attribute;

import java.util.Set;

/**
 * 与使用 Portable Operating System Interface (POSIX) 标准系列的操作系统中的文件系统上的文件关联的文件属性。
 *
 * <p> 通过调用 {@link
 * PosixFileAttributeView} 的 {@link
 * PosixFileAttributeView#readAttributes readAttributes} 方法来检索文件的 POSIX 属性。
 *
 * @since 1.7
 */

public interface PosixFileAttributes
    extends BasicFileAttributes
{
    /**
     * 返回文件的所有者。
     *
     * @return  文件所有者
     *
     * @see PosixFileAttributeView#setOwner
     */
    UserPrincipal owner();

    /**
     * 返回文件的组所有者。
     *
     * @return  文件组所有者
     *
     * @see PosixFileAttributeView#setGroup
     */
    GroupPrincipal group();

    /**
     * 返回文件的权限。文件权限作为 {@link PosixFilePermission} 元素的集合返回。返回的集合是文件权限的副本，并且可以修改。这允许修改结果并将其传递给 {@link PosixFileAttributeView#setPermissions
     * setPermissions} 方法以更新文件的权限。
     *
     * @return  文件权限
     *
     * @see PosixFileAttributeView#setPermissions
     */
    Set<PosixFilePermission> permissions();
}
