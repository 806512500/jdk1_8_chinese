/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 抛出以指示代码尝试将对象强制转换为其不是实例的子类。例如，以下代码生成一个 <code>ClassCastException</code>：
 * <blockquote><pre>
 *     Object x = new Integer(0);
 *     System.out.println((String)x);
 * </pre></blockquote>
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class ClassCastException extends RuntimeException {
    private static final long serialVersionUID = -9223365651070458532L;

    /**
     * 构造一个没有详细消息的 <code>ClassCastException</code>。
     */
    public ClassCastException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>ClassCastException</code>。
     *
     * @param   s   详细消息。
     */
    public ClassCastException(String s) {
        super(s);
    }
}
