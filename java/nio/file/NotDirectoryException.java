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
 * 当文件系统操作（预期针对目录）失败，因为文件不是一个目录时，抛出的检查异常。
 *
 * @since 1.7
 */

public class NotDirectoryException
    extends FileSystemException
{
    private static final long serialVersionUID = -9011457427178200199L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          识别文件的字符串，如果未知则为 {@code null}
     */
    public NotDirectoryException(String file) {
        super(file);
    }
}
