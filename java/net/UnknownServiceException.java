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
 * 抛出以指示发生了一个未知的服务异常。URL 连接返回的 MIME 类型没有意义，或者应用程序试图向只读的 URL 连接写入。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class UnknownServiceException extends IOException {
    private static final long serialVersionUID = -4169033248853639508L;

    /**
     * 构造一个没有详细消息的新 {@code UnknownServiceException}。
     */
    public UnknownServiceException() {
    }

    /**
     * 使用指定的详细消息构造一个新的 {@code UnknownServiceException}。
     *
     * @param   msg   详细消息。
     */
    public UnknownServiceException(String msg) {
        super(msg);
    }
}
