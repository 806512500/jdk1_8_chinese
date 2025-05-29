/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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
package java.io;

/**
 * 不支持的字符编码。
 *
 * @author  Asmus Freytag
 * @since   JDK1.1
 */
public class UnsupportedEncodingException
    extends IOException
{
    private static final long serialVersionUID = -4274276298326136670L;

    /**
     * 构造一个没有详细信息的 UnsupportedEncodingException。
     */
    public UnsupportedEncodingException() {
        super();
    }

    /**
     * 使用详细信息构造一个 UnsupportedEncodingException。
     * @param s 描述异常原因。
     */
    public UnsupportedEncodingException(String s) {
        super(s);
    }
}
