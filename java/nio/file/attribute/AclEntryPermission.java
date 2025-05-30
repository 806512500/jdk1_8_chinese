/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 定义了用于 ACL 条目权限组件的权限。
 *
 * @since 1.7
 */

public enum AclEntryPermission {

    /**
     * 读取文件数据的权限。
     */
    READ_DATA,

    /**
     * 修改文件数据的权限。
     */
    WRITE_DATA,

    /**
     * 向文件追加数据的权限。
     */
    APPEND_DATA,

    /**
     * 读取文件的命名属性的权限。
     *
     * <p> <a href="http://www.ietf.org/rfc/rfc3530.txt">RFC&nbsp;3530: Network
     * File System (NFS) version 4 Protocol</a> 定义 <em>命名属性</em>
     * 为与文件系统中的文件关联的不透明文件。
     */
    READ_NAMED_ATTRS,

    /**
     * 写入文件的命名属性的权限。
     *
     * <p> <a href="http://www.ietf.org/rfc/rfc3530.txt">RFC&nbsp;3530: Network
     * File System (NFS) version 4 Protocol</a> 定义 <em>命名属性</em>
     * 为与文件系统中的文件关联的不透明文件。
     */
    WRITE_NAMED_ATTRS,

    /**
     * 执行文件的权限。
     */
    EXECUTE,

    /**
     * 在目录中删除文件或目录的权限。
     */
    DELETE_CHILD,

    /**
     * 读取（非 ACL）文件属性的能力。
     */
    READ_ATTRIBUTES,

    /**
     * 写入（非 ACL）文件属性的能力。
     */
    WRITE_ATTRIBUTES,

    /**
     * 删除文件的权限。
     */
    DELETE,

    /**
     * 读取 ACL 属性的权限。
     */
    READ_ACL,

    /**
     * 写入 ACL 属性的权限。
     */
    WRITE_ACL,

    /**
     * 更改所有者的权限。
     */
    WRITE_OWNER,

    /**
     * 以同步读写方式在服务器上本地访问文件的权限。
     */
    SYNCHRONIZE;

    /**
     * 列出目录条目的权限（等同于 {@link #READ_DATA}）。
     */
    public static final AclEntryPermission LIST_DIRECTORY = READ_DATA;

    /**
     * 向目录中添加新文件的权限（等同于 {@link #WRITE_DATA}）。
     */
    public static final AclEntryPermission ADD_FILE = WRITE_DATA;

    /**
     * 在目录中创建子目录的权限（等同于 {@link #APPEND_DATA}）。
     */
    public static final AclEntryPermission ADD_SUBDIRECTORY = APPEND_DATA;
}
