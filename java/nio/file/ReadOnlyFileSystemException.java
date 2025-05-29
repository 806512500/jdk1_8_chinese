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
 * 当尝试更新与 {@link FileSystem#isReadOnly() 只读} {@code FileSystem} 关联的对象时抛出的未检查异常。
 */

public class ReadOnlyFileSystemException
    extends UnsupportedOperationException
{
    static final long serialVersionUID = -6822409595617487197L;

    /**
     * 构造此类的一个实例。
     */
    public ReadOnlyFileSystemException() {
    }
}
