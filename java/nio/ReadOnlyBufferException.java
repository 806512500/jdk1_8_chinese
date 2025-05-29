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

package java.nio;


/**
 * 当在只读缓冲区上调用内容修改方法（如 <tt>put</tt> 或 <tt>compact</tt>）时抛出的未检查异常。
 *
 * @since 1.4
 */

public class ReadOnlyBufferException
    extends UnsupportedOperationException
{

    private static final long serialVersionUID = -1210063976496234090L;

    /**
     * 构造此类的一个实例。
     */
    public ReadOnlyBufferException() { }

}
