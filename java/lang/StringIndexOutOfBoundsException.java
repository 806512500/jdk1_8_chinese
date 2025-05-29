/*
 * Copyright (c) 1994, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * 当 {@code String} 方法中的索引为负数或大于字符串的大小时抛出此异常。
 * 对于某些方法（如 charAt 方法），当索引等于字符串的大小时，也会抛出此异常。
 *
 * @author  未署名
 * @see     java.lang.String#charAt(int)
 * @since   JDK1.0
 */
public
class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -6762910422159637258L;

    /**
     * 构造一个没有详细消息的 {@code StringIndexOutOfBoundsException}。
     *
     * @since   JDK1.0.
     */
    public StringIndexOutOfBoundsException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code StringIndexOutOfBoundsException}。
     *
     * @param   s   详细消息。
     */
    public StringIndexOutOfBoundsException(String s) {
        super(s);
    }

    /**
     * 使用一个参数指示非法索引，构造一个新的 {@code StringIndexOutOfBoundsException}。
     *
     * @param   index   非法索引。
     */
    public StringIndexOutOfBoundsException(int index) {
        super("String index out of range: " + index);
    }
}
