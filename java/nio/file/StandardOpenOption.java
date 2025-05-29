/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

/**
 * 定义标准的打开选项。
 *
 * @since 1.7
 */

public enum StandardOpenOption implements OpenOption {
    /**
     * 以读取访问方式打开。
     */
    READ,

    /**
     * 以写入访问方式打开。
     */
    WRITE,

    /**
     * 如果文件以 {@link #WRITE} 访问方式打开，则字节将写入文件末尾而不是开头。
     *
     * <p> 如果文件被其他程序以写入访问方式打开，则写入文件末尾是否为原子操作取决于文件系统。
     */
    APPEND,

    /**
     * 如果文件已存在且以 {@link #WRITE} 访问方式打开，则其长度将被截断为 0。如果文件仅以 {@link #READ} 访问方式打开，则此选项将被忽略。
     */
    TRUNCATE_EXISTING,

    /**
     * 如果文件不存在，则创建新文件。
     * 如果设置了 {@link #CREATE_NEW} 选项，则此选项将被忽略。
     * 检查文件是否存在以及如果文件不存在则创建文件的操作相对于其他文件系统操作是原子的。
     */
    CREATE,

    /**
     * 如果文件已存在，则创建新文件并失败。
     * 检查文件是否存在以及如果文件不存在则创建文件的操作相对于其他文件系统操作是原子的。
     */
    CREATE_NEW,

    /**
     * 关闭时删除。当此选项存在时，实现会尽力在调用适当的 {@code close} 方法时删除文件。如果未调用 {@code close} 方法，则在 Java 虚拟机终止时（无论是正常终止，如 Java 语言规范所定义，还是在可能的情况下异常终止）尽力删除文件。
     * 此选项主要用于仅由单个 Java 虚拟机实例使用的临时文件。不建议在其他实体同时打开文件时使用此选项。许多关于何时以及如何删除文件的细节是实现特定的，因此未指定。特别是，实现可能无法保证在文件被攻击者替换时删除预期的文件。因此，安全敏感的应用程序在使用此选项时应谨慎。
     *
     * <p> 由于安全原因，此选项可能意味着 {@link
     * LinkOption#NOFOLLOW_LINKS} 选项。换句话说，如果在打开现有文件时存在此选项，而该文件是一个符号链接，则可能会失败（抛出 {@link java.io.IOException}）。
     */
    DELETE_ON_CLOSE,

    /**
     * 稀疏文件。与 {@link #CREATE_NEW} 选项一起使用时，此选项提供了一个提示，即新文件将是稀疏的。如果文件系统不支持创建稀疏文件，则忽略此选项。
     */
    SPARSE,

    /**
     * 要求将文件内容或元数据的每次更新都同步写入底层存储设备。
     *
     * @see <a href="package-summary.html#integrity">同步 I/O 文件完整性</a>
     */
    SYNC,

    /**
     * 要求将文件内容的每次更新都同步写入底层存储设备。
     *
     * @see <a href="package-summary.html#integrity">同步 I/O 文件完整性</a>
     */
    DSYNC;
}
