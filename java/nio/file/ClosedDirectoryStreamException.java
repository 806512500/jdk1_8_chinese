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
 * 当尝试在一个已关闭的目录流上调用操作时抛出的未经检查的异常。
 *
 * @since 1.7
 */

public class ClosedDirectoryStreamException
    extends IllegalStateException
{
    static final long serialVersionUID = 4228386650900895400L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedDirectoryStreamException() {
    }
}
