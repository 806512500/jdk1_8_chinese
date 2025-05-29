/*
 * 版权所有 (c) 1996, 2013, Oracle和/或其附属公司。保留所有权利。
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

/*
 * 版权所有 (C) 1996, 1997 - Taligent, Inc. 保留所有权利
 * 版权所有 (C) 1996 - 1998 - IBM Corp. 保留所有权利
 *
 *   本源代码和文档的原始版本受版权保护并归 Taligent, Inc. 所有，它是 IBM 的全资子公司。这些
 * 材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受多项美国和国际专利保护。此通知和对 Taligent 的归属不得移除。
 *   Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

/**
 * 表示在解析过程中意外遇到了错误。
 * @see java.lang.Exception
 * @see java.text.Format
 * @see java.text.FieldPosition
 * @author      Mark Davis
 */
public
class ParseException extends Exception {

    private static final long serialVersionUID = 2703218443322787634L;

    /**
     * 使用指定的详细消息和偏移量构造一个 ParseException。
     * 详细消息是一个描述此特定异常的字符串。
     *
     * @param s 详细消息
     * @param errorOffset 在解析过程中发现错误的位置。
     */
    public ParseException(String s, int errorOffset) {
        super(s);
        this.errorOffset = errorOffset;
    }

    /**
     * 返回发现错误的位置。
     *
     * @return 发现错误的位置
     */
    public int getErrorOffset () {
        return errorOffset;
    }

    //============ 私有成员 ============
    /**
     * 在解析过程中发现错误的字符串中的零基字符偏移量。
     * @serial
     */
    private int errorOffset;
}
