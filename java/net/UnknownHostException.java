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
 * 抛出以指示无法确定主机的 IP 地址。
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class UnknownHostException extends IOException {
    private static final long serialVersionUID = -4639126076052875403L;

    /**
     * 使用指定的详细消息构造一个新的 {@code UnknownHostException}。
     *
     * @param   host   详细消息。
     */
    public UnknownHostException(String host) {
        super(host);
    }

    /**
     * 构造一个新的 {@code UnknownHostException}，不带详细消息。
     */
    public UnknownHostException() {
    }
}
