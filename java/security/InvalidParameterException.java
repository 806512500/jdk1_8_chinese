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
 * 该异常设计用于 JCA/JCE 引擎类，
 * 当方法传递了无效参数时抛出此异常。
 *
 * @author Benjamin Renaud
 */

public class InvalidParameterException extends IllegalArgumentException {

    private static final long serialVersionUID = -857968536935667808L;

    /**
     * 构造一个没有详细信息消息的 InvalidParameterException。
     * 详细信息消息是一个描述此特定异常的字符串。
     */
    public InvalidParameterException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 InvalidParameterException。
     * 详细信息消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细信息消息。
     */
    public InvalidParameterException(String msg) {
        super(msg);
    }
}
