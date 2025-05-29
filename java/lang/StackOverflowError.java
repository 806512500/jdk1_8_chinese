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
 * 当应用程序递归太深导致堆栈溢出时抛出。
 *
 * @author 未署名
 * @since   JDK1.0
 */
public
class StackOverflowError extends VirtualMachineError {
    private static final long serialVersionUID = 8609175038441759607L;

    /**
     * 构造一个没有详细消息的 <code>StackOverflowError</code>。
     */
    public StackOverflowError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>StackOverflowError</code>。
     *
     * @param   s   详细消息。
     */
    public StackOverflowError(String s) {
        super(s);
    }
}