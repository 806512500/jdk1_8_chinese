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
 * 当文件无法作为原子文件系统操作进行移动时抛出的检查异常。
 *
 * @since 1.7
 */

public class AtomicMoveNotSupportedException
    extends FileSystemException
{
    static final long serialVersionUID = 5402760225333135579L;

    /**
     * 构造此类的一个实例。
     *
     * @param   source
     *          一个标识源文件的字符串，如果未知则为 {@code null}
     * @param   target
     *          一个标识目标文件的字符串，如果未知则为 {@code null}
     * @param   reason
     *          包含额外信息的原因消息
     */
    public AtomicMoveNotSupportedException(String source,
                                           String target,
                                           String reason)
    {
        super(source, target, reason);
    }
}
