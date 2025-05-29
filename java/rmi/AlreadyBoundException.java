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
 * 如果尝试在注册表中将对象绑定到已经存在关联绑定的名称时，将抛出<code>AlreadyBoundException</code>。
 *
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @see     java.rmi.Naming#bind(String, java.rmi.Remote)
 * @see     java.rmi.registry.Registry#bind(String, java.rmi.Remote)
 */
public class AlreadyBoundException extends java.lang.Exception {

    /* 表示与JDK 1.1.x版本类的兼容性 */
    private static final long serialVersionUID = 9218657361741657110L;

    /**
     * 构造一个没有指定详细消息的<code>AlreadyBoundException</code>。
     * @since JDK1.1
     */
    public AlreadyBoundException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个<code>AlreadyBoundException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public AlreadyBoundException(String s) {
        super(s);
    }
}
