/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 与文件系统中的文件关联的基本属性。
 *
 * <p> 基本文件属性是许多文件系统中常见的属性，包括由本接口定义的强制性和可选文件属性。
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
     * 返回最后一次修改的时间。
     *
     * <p> 如果文件系统实现不支持时间戳来表示最后一次修改的时间，则此方法返回一个实现特定的默认值，通常是表示纪元时间（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示文件最后一次修改时间的 {@code FileTime}
     */
    FileTime lastModifiedTime();

    /**
     * 返回最后一次访问的时间。
     *
     * <p> 如果文件系统实现不支持时间戳来表示最后一次访问的时间，则此方法返回一个实现特定的默认值，通常是 {@link
     * #lastModifiedTime() 最后修改时间} 或表示纪元时间（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示最后一次访问时间的 {@code FileTime}
     */
    FileTime lastAccessTime();

    /**
     * 返回创建时间。创建时间是文件被创建的时间。
     *
     * <p> 如果文件系统实现不支持时间戳来表示文件创建的时间，则此方法返回一个实现特定的默认值，通常是 {@link
     * #lastModifiedTime() 最后修改时间} 或表示纪元时间（1970-01-01T00:00:00Z）的 {@code FileTime}。
     *
     * @return 一个表示文件创建时间的 {@code FileTime}
     */
    FileTime creationTime();

    /**
     * 告诉文件是否是一个具有不透明内容的常规文件。
     *
     * @return 如果文件是一个具有不透明内容的常规文件，则返回 {@code true}
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
     * 告诉文件是否是常规文件、目录或符号链接之外的其他类型。
     *
     * @return 如果文件是常规文件、目录或符号链接之外的其他类型，则返回 {@code true}
     */
    boolean isOther();

    /**
     * 返回文件的大小（以字节为单位）。由于压缩、稀疏文件支持或其他原因，实际大小可能与文件系统上的大小不同。对于不是 {@link
     * #isRegularFile 常规} 文件的文件，其大小是实现特定的，因此未指定。
     *
     * @return 文件大小，以字节为单位
     */
    long size();

    /**
     * 返回一个唯一标识给定文件的对象，如果没有可用的文件键，则返回 {@code
     * null}。在某些平台或文件系统中，可以使用标识符或标识符组合来唯一标识文件。这样的标识符对于支持 <a
     * href="../package-summary.html#links">符号链接</a> 的文件系统或允许文件成为多个目录条目的文件系统中的文件树遍历等操作非常重要。例如，在 UNIX 文件系统中，<em>设备 ID</em> 和 <em>inode</em> 常用于此类目的。
     *
     * <p> 仅当文件系统和文件保持静态时，此方法返回的文件键才能保证唯一。文件系统在文件被删除后是否重用标识符是实现特定的，因此未指定。
     *
     * <p> 通过此方法返回的文件键可以进行相等性比较，并且适合在集合中使用。如果文件系统和文件保持静态，并且两个文件是 {@link java.nio.file.Files#isSameFile 相同} 的，且它们的文件键非 {@code null}，则它们的文件键相等。
     *
     * @return 唯一标识给定文件的对象，或 {@code null}
     *
     * @see java.nio.file.Files#walkFileTree
     */
    Object fileKey();
}
