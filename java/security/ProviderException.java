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
 * 一个用于提供者异常（如配置错误或不可恢复的内部错误）的运行时异常，
 * 提供者可以继承此类以抛出特定的、提供者特定的运行时错误。
 *
 * @author Benjamin Renaud
 */
public class ProviderException extends RuntimeException {

    private static final long serialVersionUID = 5256023526693665674L;

    /**
     * 构造一个没有详细消息的 ProviderException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public ProviderException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 ProviderException。详细消息是一个描述此
     * 特定异常的字符串。
     *
     * @param s 详细消息。
     */
    public ProviderException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code ProviderException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}
     * 创建一个 {@code ProviderException}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public ProviderException(Throwable cause) {
        super(cause);
    }
}
