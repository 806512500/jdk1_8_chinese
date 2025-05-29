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
 * 当尝试在一个已关闭的监视服务上执行操作时抛出的未检查异常。
 */

public class ClosedWatchServiceException
    extends IllegalStateException
{
    static final long serialVersionUID = 1853336266231677732L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedWatchServiceException() {
    }
}
