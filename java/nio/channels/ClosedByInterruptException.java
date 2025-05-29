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
 * 当一个线程在通道上的 I/O 操作被阻塞时，如果另一个线程中断了它，则该线程会收到此检查异常。
 * 在抛出此异常之前，通道将被关闭，并且先前被阻塞的线程的中断状态将被设置。
 *
 * @since 1.4
 */

public class ClosedByInterruptException
    extends AsynchronousCloseException
{

    private static final long serialVersionUID = -4488191543534286750L;

    /**
     * 构造此类的一个实例。
     */
    public ClosedByInterruptException() { }

}
