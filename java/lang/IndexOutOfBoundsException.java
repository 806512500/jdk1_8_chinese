/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 抛出以指示某种索引（如数组、字符串或向量的索引）超出范围。
 * <p>
 * 应用程序可以继承此类以指示类似的异常。
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
public
class IndexOutOfBoundsException extends RuntimeException {
    private static final long serialVersionUID = 234122996006267687L;

    /**
     * 构造一个没有详细消息的 <code>IndexOutOfBoundsException</code>。
     */
    public IndexOutOfBoundsException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IndexOutOfBoundsException</code>。
     *
     * @param   s   详细消息。
     */
    public IndexOutOfBoundsException(String s) {
        super(s);
    }
}
