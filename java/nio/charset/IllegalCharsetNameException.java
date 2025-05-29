/*
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 *
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
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio.charset;


/**
 * 当用作此类的字符串不是一个<a href=Charset.html#names>合法的字符集名称</a>时，抛出的未经检查的异常。
 *
 * @since 1.4
 */

public class IllegalCharsetNameException
    extends IllegalArgumentException
{

    private static final long serialVersionUID = 1457525358470002989L;

    private String charsetName;

    /**
     * 构造此类的一个实例。
     *
     * @param  charsetName
     *         非法的字符集名称
     */
    public IllegalCharsetNameException(String charsetName) {
        super(String.valueOf(charsetName));
	this.charsetName = charsetName;
    }

    /**
     * 获取非法的字符集名称。
     *
     * @return  非法的字符集名称
     */
    public String getCharsetName() {
        return charsetName;
    }

}
