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
 * 运行时异常，当找不到所需类型的提供者时抛出。
 */

public class ProviderNotFoundException
    extends RuntimeException
{
    static final long serialVersionUID = -1880012509822920354L;

    /**
     * 构造此类的一个实例。
     */
    public ProviderNotFoundException() {
    }

    /**
     * 构造此类的一个实例。
     *
     * @param   msg
     *          详细消息
     */
    public ProviderNotFoundException(String msg) {
        super(msg);
    }
}
