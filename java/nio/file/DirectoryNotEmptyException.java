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
 * 当文件系统操作因目录非空而失败时抛出的检查异常。
 *
 * @since 1.7
 */

public class DirectoryNotEmptyException
    extends FileSystemException
{
    static final long serialVersionUID = 3056667871802779003L;

    /**
     * 构造此类的一个实例。
     *
     * @param   dir
     *          一个标识目录的字符串，如果未知则为 {@code null}
     */
    public DirectoryNotEmptyException(String dir) {
        super(dir);
    }
}
