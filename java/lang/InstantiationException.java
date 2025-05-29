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
 * 当应用程序尝试使用 {@code Class} 类中的 {@code newInstance} 方法创建一个类的实例，
 * 但指定的类对象无法实例化时抛出。实例化可能因多种原因失败，包括但不限于：
 *
 * <ul>
 * <li> 类对象表示抽象类、接口、数组类、基本类型或 {@code void}
 * <li> 类没有无参构造函数
 *</ul>
 *
 * @author  未署名
 * @see     java.lang.Class#newInstance()
 * @since   JDK1.0
 */
public
class InstantiationException extends ReflectiveOperationException {
    private static final long serialVersionUID = -8441929162975509110L;

    /**
     * 构造一个没有详细信息消息的 {@code InstantiationException}。
     */
    public InstantiationException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 {@code InstantiationException}。
     *
     * @param   s   详细信息消息。
     */
    public InstantiationException(String s) {
        super(s);
    }
}
