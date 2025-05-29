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
 * 抛出以指示线程未处于请求操作的适当状态。例如，参见
 * <code>suspend</code> 和 <code>resume</code> 方法在类
 * <code>Thread</code> 中的使用。
 *
 * @author  未署名
 * @see     java.lang.Thread#resume()
 * @see     java.lang.Thread#suspend()
 * @since   JDK1.0
 */
public class IllegalThreadStateException extends IllegalArgumentException {
    private static final long serialVersionUID = -7626246362397460174L;

    /**
     * 构造一个没有详细消息的 <code>IllegalThreadStateException</code>。
     */
    public IllegalThreadStateException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalThreadStateException</code>。
     *
     * @param   s   详细消息。
     */
    public IllegalThreadStateException(String s) {
        super(s);
    }
}
