/*
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 *
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
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio.channels;


/**
 * 当尝试在一个已关闭的选择器上调用 I/O 操作时抛出的未检查异常。
 *
 * @since 1.4
 */

public class ClosedSelectorException
    extends IllegalStateException
{

    private static final long serialVersionUID = 6466297122317847835L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedSelectorException() { }

}
