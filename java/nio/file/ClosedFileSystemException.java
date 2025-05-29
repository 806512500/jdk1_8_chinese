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
 * 当尝试对文件进行操作而文件系统已关闭时抛出的未检查异常。
 */

public class ClosedFileSystemException
    extends IllegalStateException
{
    static final long serialVersionUID = -8158336077256193488L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedFileSystemException() {
    }
}
