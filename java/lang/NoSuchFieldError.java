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
 * 如果应用程序尝试访问或修改对象的指定字段，而该对象不再具有该字段时抛出此异常。
 * <p>
 * 通常，此错误会被编译器捕获；只有在类的定义不兼容地更改时，此错误才可能在运行时发生。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NoSuchFieldError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -3456430195886129035L;

    /**
     * 构造一个没有详细消息的 <code>NoSuchFieldError</code>。
     */
    public NoSuchFieldError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NoSuchFieldError</code>。
     *
     * @param   s   详细消息。
     */
    public NoSuchFieldError(String s) {
        super(s);
    }
}
