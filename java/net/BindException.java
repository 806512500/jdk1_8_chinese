/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

/**
 * 表示在尝试将套接字绑定到本地地址和端口时发生错误。通常，端口正在使用中，或者请求的本地地址无法分配。
 *
 * @since   JDK1.1
 */

public class BindException extends SocketException {
    private static final long serialVersionUID = -5945005768251722951L;

    /**
     * 使用指定的详细消息构造一个新的 BindException，说明绑定错误的原因。
     * 详细消息是一个描述此错误的特定原因的字符串。
     * @param msg 详细消息
     */
    public BindException(String msg) {
        super(msg);
    }

    /**
     * 构造一个没有详细消息的新 BindException。
     */
    public BindException() {}
}
