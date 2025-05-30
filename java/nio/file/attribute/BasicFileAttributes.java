/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 与文件系统中的文件关联的基本属性。
 *
 * <p> 基本文件属性是许多文件系统中常见的属性，由本接口定义的强制性和可选文件属性组成。
 *
 * <p> <b>使用示例：</b>
 * <pre>
 *    Path file = ...
 *    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
 * </pre>
 *
 * @since 1.7
 *
 * @see BasicFileAttributeView
 */

public interface BasicFileAttributes {

    /**
     * 返回上次修改的时间。
     *
     * <p> 如果文件系统实现不支持表示上次修改时间的时间戳，则此方法返回一个实现特定的默认值，通常是一个表示纪元（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示文件上次修改时间的 {@code FileTime}
     */
    FileTime lastModifiedTime();

    /**
     * 返回上次访问的时间。
     *
     * <p> 如果文件系统实现不支持表示上次访问时间的时间戳，则此方法返回一个实现特定的默认值，通常是 {@link #lastModifiedTime() 上次修改时间} 或表示纪元（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示文件上次访问时间的 {@code FileTime}
     */
    FileTime lastAccessTime();

    /**
     * 返回创建时间。创建时间是文件被创建的时间。
     *
     * <p> 如果文件系统实现不支持表示文件创建时间的时间戳，则此方法返回一个实现特定的默认值，通常是 {@link #lastModifiedTime() 上次修改时间} 或表示纪元（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示文件创建时间的 {@code FileTime}
     */
    FileTime creationTime();

    /**
     * 告诉文件是否是一个具有不透明内容的普通文件。
     *
     * @return 如果文件是一个具有不透明内容的普通文件，则返回 {@code true}
     */
    boolean isRegularFile();

    /**
     * 告诉文件是否是一个目录。
     *
     * @return 如果文件是一个目录，则返回 {@code true}
     */
    boolean isDirectory();

    /**
     * 告诉文件是否是一个符号链接。
     *
     * @return 如果文件是一个符号链接，则返回 {@code true}
     */
    boolean isSymbolicLink();

    /**
     * 告诉文件是否是普通文件、目录或符号链接以外的其他类型。
     *
     * @return 如果文件是普通文件、目录或符号链接以外的其他类型，则返回 {@code true}
     */
    boolean isOther();

    /**
     * 返回文件的大小（以字节为单位）。由于压缩、支持稀疏文件或其他原因，实际大小可能与文件系统上的大小不同。对于不是 {@link #isRegularFile 普通} 文件的文件，其大小是实现特定的，因此未指定。
     *
     * @return 文件的大小，以字节为单位
     */
    long size();

    /**
     * 返回一个唯一标识给定文件的对象，如果没有可用的文件键，则返回 {@code null}。在某些平台或文件系统中，可以使用标识符或标识符组合来唯一标识文件。这样的标识符对于支持 <a
     * href="../package-summary.html#links">符号链接</a> 的文件系统或允许文件成为多个目录条目的文件系统中的文件树遍历等操作非常重要。例如，在 UNIX 文件系统中，<em>设备 ID</em> 和 <em>inode</em> 常用于此类目的。
     *
     * <p> 如果文件系统和文件保持静态，此方法返回的文件键可以保证唯一。文件系统在文件被删除后是否重用标识符是实现特定的，因此未指定。
     *
     * <p> 通过此方法返回的文件键可以进行相等性比较，并且适合在集合中使用。如果文件系统和文件保持静态，并且两个文件是 {@link java.nio.file.Files#isSameFile 相同} 的且文件键非 {@code null}，则它们的文件键相等。
     *
     * @return 唯一标识给定文件的对象，或 {@code null}
     *
     * @see java.nio.file.Files#walkFileTree
     */
    Object fileKey();
}
