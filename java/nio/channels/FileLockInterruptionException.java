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
 * 当一个线程在等待获取文件锁时被另一个线程中断，该线程会接收到的检查异常。在抛出此异常之前，
 * 被阻塞的线程的中断状态将被设置。
 *
 * @since 1.4
 */

public class FileLockInterruptionException
    extends java.io.IOException
{

    private static final long serialVersionUID = 7104080643653532383L;

    /**
     * 构造此类的一个实例。
     */
    public FileLockInterruptionException() { }

}
