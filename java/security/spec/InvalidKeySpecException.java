/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.spec;

import java.security.GeneralSecurityException;

/**
 * 这是用于无效密钥规范的异常。
 *
 * @author Jan Luehe
 *
 *
 * @see KeySpec
 *
 * @since 1.2
 */

public class InvalidKeySpecException extends GeneralSecurityException {

    private static final long serialVersionUID = 3546139293998810778L;

    /**
     * 构造一个没有详细消息的 InvalidKeySpecException。详细消息是一个描述此特定异常的字符串。
     */
    public InvalidKeySpecException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 InvalidKeySpecException。详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public InvalidKeySpecException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code InvalidKeySpecException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since 1.5
     */
    public InvalidKeySpecException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）创建一个 {@code InvalidKeySpecException}。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since 1.5
     */
    public InvalidKeySpecException(Throwable cause) {
        super(cause);
    }
}
