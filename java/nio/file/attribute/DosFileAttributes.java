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
 * 与支持传统“DOS”属性的文件系统中的文件关联的文件属性。
 *
 * <p> <b>使用示例：</b>
 * <pre>
 *    Path file = ...
 *    DosFileAttributes attrs = Files.readAttributes(file, DosFileAttributes.class);
 * </pre>
 *
 * @since 1.7
 */

public interface DosFileAttributes
    extends BasicFileAttributes
{
    /**
     * 返回只读属性的值。
     *
     * <p> 此属性通常用作简单的访问控制机制，以防止文件被删除或更新。文件系统或平台是否执行任何操作以防止<em>只读</em>文件被更新是实现特定的。
     *
     * @return  只读属性的值
     */
    boolean isReadOnly();

    /**
     * 返回隐藏属性的值。
     *
     * <p> 此属性通常用于指示文件是否对用户可见。
     *
     * @return  隐藏属性的值
     */
    boolean isHidden();

    /**
     * 返回存档属性的值。
     *
     * <p> 此属性通常由备份程序使用。
     *
     * @return  存档属性的值
     */
    boolean isArchive();

    /**
     * 返回系统属性的值。
     *
     * <p> 此属性通常用于指示文件是操作系统的一部分。
     *
     * @return  系统属性的值
     */
    boolean isSystem();
}
