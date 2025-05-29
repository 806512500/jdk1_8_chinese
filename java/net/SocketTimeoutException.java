/*
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在套接字读取或接受时发生了超时。
 *
 * @since   1.4
 */

public class SocketTimeoutException extends java.io.InterruptedIOException {
    private static final long serialVersionUID = -8846654841826352300L;

    /**
     * 使用详细消息构造一个新的 SocketTimeoutException。
     * @param msg 详细消息
     */
    public SocketTimeoutException(String msg) {
        super(msg);
    }

    /**
     * 构造一个新的 SocketTimeoutException，没有详细消息。
     */
    public SocketTimeoutException() {}
}
