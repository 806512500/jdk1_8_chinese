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
 * 当尝试创建一个文件或目录时，如果该名称的文件已存在，则抛出此检查异常。
 *
 * @since 1.7
 */

public class FileAlreadyExistsException
    extends FileSystemException
{
    static final long serialVersionUID = 7579540934498831181L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，如果未知则为 {@code null}
     */
    public FileAlreadyExistsException(String file) {
        super(file);
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，如果未知则为 {@code null}
     * @param   other
     *          一个标识另一个文件的字符串，如果未知则为 {@code null}
     * @param   reason
     *          一个包含额外信息的原因消息，或 {@code null}
     */
    public FileAlreadyExistsException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
