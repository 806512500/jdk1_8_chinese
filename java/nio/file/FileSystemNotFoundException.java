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
 * 当找不到文件系统时抛出的运行时异常。
 */

public class FileSystemNotFoundException
    extends RuntimeException
{
    static final long serialVersionUID = 7999581764446402397L;

    /**
     * 构造此类的一个实例。
     */
    public FileSystemNotFoundException() {
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   msg
     *          详细消息
     */
    public FileSystemNotFoundException(String msg) {
        super(msg);
    }
}
