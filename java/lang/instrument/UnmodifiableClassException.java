/*
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.instrument;

/**
 * 当指定的类之一无法被修改时，由
 * {@link java.lang.instrument.Instrumentation#redefineClasses Instrumentation.redefineClasses}
 * 方法的实现抛出。
 *
 * @see     java.lang.instrument.Instrumentation#redefineClasses
 * @since   1.5
 */
public class UnmodifiableClassException extends Exception {
    private static final long serialVersionUID = 1716652643585309178L;

    /**
     * 构造一个没有详细消息的 <code>UnmodifiableClassException</code>。
     */
    public
    UnmodifiableClassException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>UnmodifiableClassException</code>。
     *
     * @param   s   详细消息。
     */
    public
    UnmodifiableClassException(String s) {
        super(s);
    }
}
