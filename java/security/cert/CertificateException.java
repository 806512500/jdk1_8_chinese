/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * 此异常表示各种证书问题之一。
 *
 * @author Hemma Prafullchandra
 * @see Certificate
 */
public class CertificateException extends GeneralSecurityException {

    private static final long serialVersionUID = 3192535253797119798L;

    /**
     * 构造一个没有详细消息的证书异常。详细消息是一个描述此特定异常的字符串。
     */
    public CertificateException() {
        super();
    }

    /**
     * 使用给定的详细消息构造一个证书异常。详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public CertificateException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertificateException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 创建一个 {@code CertificateException}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateException(Throwable cause) {
        super(cause);
    }
}
