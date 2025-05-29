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
 * 字符转换异常的基类。
 *
 * @author      Asmus Freytag
 * @since       JDK1.1
 */
public class CharConversionException
    extends java.io.IOException
{
    private static final long serialVersionUID = -8680016352018427031L;

    /**
     * 不提供详细消息。
     */
    public CharConversionException() {
    }
    /**
     * 提供详细消息。
     *
     * @param s 与异常关联的详细消息。
     */
    public CharConversionException(String s) {
        super(s);
    }
}
