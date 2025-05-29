/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;


/**
 * 当反射方法在需要解释类型、方法或构造函数的泛型签名信息时遇到语法错误的签名属性时抛出。
 *
 * @since 1.5
 */
public class GenericSignatureFormatError extends ClassFormatError {
    private static final long serialVersionUID = 6709919147137911034L;

    /**
     * 构造一个新的 {@code GenericSignatureFormatError}。
     *
     */
    public GenericSignatureFormatError() {
        super();
    }

    /**
     * 使用指定的消息构造一个新的 {@code GenericSignatureFormatError}。
     *
     * @param message 详细消息，可以为 {@code null}
     */
    public GenericSignatureFormatError(String message) {
        super(message);
    }
}
