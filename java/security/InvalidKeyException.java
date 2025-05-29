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


package java.security;

/**
 * 这是用于无效密钥（编码无效、长度错误、未初始化等）的异常。
 *
 * @author Benjamin Renaud
 */

public class InvalidKeyException extends KeyException {

    private static final long serialVersionUID = 5698479920593359816L;

    /**
     * 构造一个没有详细消息的 InvalidKeyException。详细消息是一个描述此特定异常的字符串。
     */
    public InvalidKeyException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 InvalidKeyException。详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public InvalidKeyException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code InvalidKeyException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null} 值，表示原因不存在或未知。
     * @since 1.5
     */
    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 创建一个 {@code InvalidKeyException}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null} 值，表示原因不存在或未知。
     * @since 1.5
     */
    public InvalidKeyException(Throwable cause) {
        super(cause);
    }
}
