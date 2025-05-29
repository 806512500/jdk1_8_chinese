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
 * 检查异常，当尝试调用或完成一个已关闭的通道上的 I/O 操作时抛出，或者至少该操作已关闭。此异常的抛出并不一定意味着通道完全关闭。例如，一个写入端已关闭的套接字通道可能仍然可以读取。
 *
 * @since 1.4
 */

public class ClosedChannelException
    extends java.io.IOException
{

    private static final long serialVersionUID = 882777185433553857L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedChannelException() { }

}
