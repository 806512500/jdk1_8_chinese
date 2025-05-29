/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 当 Java 虚拟机尝试读取一个类文件并确定文件中的主版本号和次版本号不受支持时抛出。
 *
 * @since   1.2
 */
public
class UnsupportedClassVersionError extends ClassFormatError {
    private static final long serialVersionUID = -7123279212883497373L;

    /**
     * 构造一个没有详细消息的 <code>UnsupportedClassVersionError</code>。
     */
    public UnsupportedClassVersionError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>UnsupportedClassVersionError</code>。
     *
     * @param   s   详细消息。
     */
    public UnsupportedClassVersionError(String s) {
        super(s);
    }
}
