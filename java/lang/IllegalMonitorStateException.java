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
 * 抛出以指示线程试图在对象的监视器上等待或通知等待对象监视器的其他线程，而没有拥有指定的监视器。
 *
 * @author  未署名
 * @see     java.lang.Object#notify()
 * @see     java.lang.Object#notifyAll()
 * @see     java.lang.Object#wait()
 * @see     java.lang.Object#wait(long)
 * @see     java.lang.Object#wait(long, int)
 * @since   JDK1.0
 */
public
class IllegalMonitorStateException extends RuntimeException {
    private static final long serialVersionUID = 3713306369498869069L;

    /**
     * 构造一个没有详细消息的 <code>IllegalMonitorStateException</code>。
     */
    public IllegalMonitorStateException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalMonitorStateException</code>。
     *
     * @param   s   详细消息。
     */
    public IllegalMonitorStateException(String s) {
        super(s);
    }
}
