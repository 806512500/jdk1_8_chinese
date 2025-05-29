/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.charset;


/**
 * 当输入字节序列对于给定的字符集不合法，或者输入字符序列不是合法的十六位Unicode序列时抛出的检查异常。
 *
 * @since 1.4
 */

public class MalformedInputException
    extends CharacterCodingException
{

    private static final long serialVersionUID = -3438823399834806194L;

    private int inputLength;

    /**
     * 使用给定的长度构造一个 {@code MalformedInputException}。
     * @param inputLength 输入的长度
     */
    public MalformedInputException(int inputLength) {
        this.inputLength = inputLength;
    }

    /**
     * 返回输入的长度。
     * @return 输入的长度
     */
    public int getInputLength() {
        return inputLength;
    }

    /**
     * 返回消息。
     * @return 消息
     */
    public String getMessage() {
        return "Input length = " + inputLength;
    }

}
