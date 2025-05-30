/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.attribute;

import java.util.Set;

/**
 * 与实现 Portable Operating System Interface (POSIX) 标准系列的操作系统文件系统中的文件关联的文件属性。
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
     * 返回文件的权限。文件权限作为一组 {@link PosixFilePermission} 元素返回。返回的集合是文件权限的副本，可以修改。这允许对结果进行修改，并传递给 {@link PosixFileAttributeView#setPermissions
     * setPermissions} 方法以更新文件的权限。
     *
     * @return  文件权限
     *
     * @see PosixFileAttributeView#setPermissions
     */
    Set<PosixFilePermission> permissions();
}
