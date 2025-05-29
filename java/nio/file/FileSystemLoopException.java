/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
 * 当文件系统中遇到循环或循环引用时抛出的检查异常。
 *
 * @since 1.7
 * @see Files#walkFileTree
 */

public class FileSystemLoopException
    extends FileSystemException
{
    private static final long serialVersionUID = 4843039591949217617L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识导致循环的文件的字符串，如果未知则为 {@code null}
     */
    public FileSystemLoopException(String file) {
        super(file);
    }
}
