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
 * 当尝试在一个文件系统提供者创建的对象上调用方法时，如果参数是由不同的文件系统提供者创建的，则抛出此未检查异常。
 */
public class ProviderMismatchException
    extends java.lang.IllegalArgumentException
{
    static final long serialVersionUID = 4990847485741612530L;

    /**
     * 构造此类的一个实例。
     */
    public ProviderMismatchException() {
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   msg
     *          详细消息
     */
    public ProviderMismatchException(String msg) {
        super(msg);
    }
}
