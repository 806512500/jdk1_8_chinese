/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 用于编码错误操作的类型安全枚举。
 *
 * <p> 该类的实例用于指定如何处理字符集 <a
 * href="CharsetDecoder.html#cae">解码器</a> 和 <a
 * href="CharsetEncoder.html#cae">编码器</a> 中的错误输入和无法映射的字符错误。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public class CodingErrorAction {

    private String name;

    private CodingErrorAction(String name) {
        this.name = name;
    }

    /**
     * 表示编码错误将通过丢弃错误输入并继续编码操作来处理。
     */
    public static final CodingErrorAction IGNORE
        = new CodingErrorAction("IGNORE");

    /**
     * 表示编码错误将通过丢弃错误输入，将编码器的替换值附加到输出缓冲区，并继续编码操作来处理。
     */
    public static final CodingErrorAction REPLACE
        = new CodingErrorAction("REPLACE");

    /**
     * 表示编码错误将通过返回一个 {@link CoderResult} 对象或抛出一个 {@link
     * CharacterCodingException} 来报告，具体取决于实现编码过程的方法。
     */
    public static final CodingErrorAction REPORT
        = new CodingErrorAction("REPORT");

    /**
     * 返回描述此操作的字符串。
     *
     * @return  描述字符串
     */
    public String toString() {
        return name;
    }

}
