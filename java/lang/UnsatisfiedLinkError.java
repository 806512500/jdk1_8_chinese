/*
 * Copyright (c) 1994, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 如果 Java 虚拟机无法找到声明为 <code>native</code> 的方法的适当本地语言定义，则抛出此异常。
 *
 * @author 未署名
 * @see     java.lang.Runtime
 * @since   JDK1.0
 */
public
class UnsatisfiedLinkError extends LinkageError {
    private static final long serialVersionUID = -4019343241616879428L;

    /**
     * 构造一个没有详细消息的 <code>UnsatisfiedLinkError</code>。
     */
    public UnsatisfiedLinkError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>UnsatisfiedLinkError</code>。
     *
     * @param   s   详细消息。
     */
    public UnsatisfiedLinkError(String s) {
        super(s);
    }
}
