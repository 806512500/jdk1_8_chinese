/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 当请求特定的安全提供者但在环境中不可用时，将抛出此异常。
 *
 * @author Benjamin Renaud
 */

public class NoSuchProviderException extends GeneralSecurityException {

    private static final long serialVersionUID = 8488111756688534474L;

    /**
     * 构造一个没有详细消息的 NoSuchProviderException。详细消息是一个描述此特定异常的字符串。
     */
    public NoSuchProviderException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 NoSuchProviderException。详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public NoSuchProviderException(String msg) {
        super(msg);
    }
}
