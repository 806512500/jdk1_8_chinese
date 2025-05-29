/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在已连接的数据报上接收到了 ICMP 端口不可达消息。
 *
 * @since   1.4
 */

public class PortUnreachableException extends SocketException {
    private static final long serialVersionUID = 8462541992376507323L;

    /**
     * 使用详细消息构造新的 {@code PortUnreachableException}。
     * @param msg 详细消息
     */
    public PortUnreachableException(String msg) {
        super(msg);
    }

    /**
     * 构造一个新的 {@code PortUnreachableException}，没有详细消息。
     */
    public PortUnreachableException() {}
}
