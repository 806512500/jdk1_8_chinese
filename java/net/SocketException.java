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

package java.net;

import java.io.IOException;

/**
 * 抛出以指示在创建或访问 Socket 时发生错误。
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class SocketException extends IOException {
    private static final long serialVersionUID = -5935874303556886934L;

    /**
     * 使用指定的详细消息构造一个新的 {@code SocketException}。
     *
     * @param msg 详细消息。
     */
    public SocketException(String msg) {
        super(msg);
    }

    /**
     * 构造一个新的 {@code SocketException}，不带详细消息。
     */
    public SocketException() {
    }
}
