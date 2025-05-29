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
 * 当尝试获取一个文件区域的锁时，如果该区域与同一Java虚拟机已经锁定的区域重叠，或者另一个线程正在等待锁定同一文件的重叠区域时，抛出的未检查异常。
 *
 * @since 1.4
 */

public class OverlappingFileLockException
    extends IllegalStateException
{

    private static final long serialVersionUID = 2047812138163068433L;

    /**
     * 构造此类的一个实例。
     */
    public OverlappingFileLockException() { }

}
