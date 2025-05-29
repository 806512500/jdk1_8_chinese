/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi;

/**
 * 当尝试在注册表中查找或解除绑定一个没有关联绑定的名称时，抛出 <code>NotBoundException</code>。
 *
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @see     java.rmi.Naming#lookup(String)
 * @see     java.rmi.Naming#unbind(String)
 * @see     java.rmi.registry.Registry#lookup(String)
 * @see     java.rmi.registry.Registry#unbind(String)
 */
public class NotBoundException extends java.lang.Exception {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -1857741824849069317L;

    /**
     * 构造一个没有指定详细消息的 <code>NotBoundException</code>。
     * @since JDK1.1
     */
    public NotBoundException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NotBoundException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public NotBoundException(String s) {
        super(s);
    }
}
