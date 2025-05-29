/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 表示在输入过程中意外地达到了文件末尾或流末尾。
 * <p>
 * 此异常主要由数据输入流用于指示流结束。请注意，许多其他输入操作在流结束时返回一个特殊值，而不是抛出异常。
 *
 * @author  Frank Yellin
 * @see     java.io.DataInputStream
 * @see     java.io.IOException
 * @since   JDK1.0
 */
public
class EOFException extends IOException {
    private static final long serialVersionUID = 6433858223774886977L;

    /**
     * 构造一个 <code>EOFException</code>，其错误详细信息消息为 <code>null</code>。
     */
    public EOFException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>EOFException</code>。字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code> 方法从类 <code>java.lang.Throwable</code> 中检索。
     *
     * @param   s   详细信息消息。
     */
    public EOFException(String s) {
        super(s);
    }
}
