/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 所有特定于对象流类的异常的超类。
 *
 * @author  未指定
 * @since   JDK1.1
 */
public abstract class ObjectStreamException extends IOException {

    private static final long serialVersionUID = 7260898174833392607L;

    /**
     * 使用指定的参数创建一个 ObjectStreamException。
     *
     * @param classname 用于异常的详细消息
     */
    protected ObjectStreamException(String classname) {
        super(classname);
    }

    /**
     * 创建一个 ObjectStreamException。
     */
    protected ObjectStreamException() {
        super();
    }
}
