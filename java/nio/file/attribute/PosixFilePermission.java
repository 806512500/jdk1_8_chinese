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

/**
 * 定义用于 {@link PosixFileAttributes#permissions() 权限} 属性的位。
 *
 * <p> {@link PosixFilePermissions} 类定义了用于操作权限集的方法。
 *
 * @since 1.7
 */

public enum PosixFilePermission {

    /**
     * 所有者的读取权限。
     */
    OWNER_READ,

    /**
     * 所有者的写入权限。
     */
    OWNER_WRITE,

    /**
     * 所有者的执行/搜索权限。
     */
    OWNER_EXECUTE,

    /**
     * 组的读取权限。
     */
    GROUP_READ,

    /**
     * 组的写入权限。
     */
    GROUP_WRITE,

    /**
     * 组的执行/搜索权限。
     */
    GROUP_EXECUTE,

    /**
     * 其他人的读取权限。
     */
    OTHERS_READ,

    /**
     * 其他人的写入权限。
     */
    OTHERS_WRITE,

    /**
     * 其他人的执行/搜索权限。
     */
    OTHERS_EXECUTE;
}
