/*
 * 版权所有 (c) 2001, 2013，Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 当输入字符（或字节）序列有效但无法映射到输出字节（或字符）序列时抛出的检查异常。
 *
 * @since 1.4
 */

public class UnmappableCharacterException
    extends CharacterCodingException
{

    private static final long serialVersionUID = -7026962371537706123L;

    private int inputLength;

    /**
     * 使用给定长度构造一个 {@code UnmappableCharacterException}。
     * @param inputLength 输入的长度
     */
    public UnmappableCharacterException(int inputLength) {
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
        return "输入长度 = " + inputLength;
    }

}
