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
 * 当文件系统操作失败，因为文件不是一个符号链接时抛出的检查异常。
 *
 * @since 1.7
 */

public class NotLinkException
    extends FileSystemException
{
    static final long serialVersionUID = -388655596416518021L;

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，或如果未知则为 {@code null}
     */
    public NotLinkException(String file) {
        super(file);
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   file
     *          一个标识文件的字符串，或如果未知则为 {@code null}
     * @param   other
     *          一个标识另一个文件的字符串，或如果未知则为 {@code null}
     * @param   reason
     *          包含额外信息的原因消息，或为 {@code null}
     */
    public NotLinkException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
