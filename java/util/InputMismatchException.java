```
/*
 * 版权所有 (c) 2003, 2008, Oracle 和/或其关联公司。保留所有权利。
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

package java.util;

/**
 * 由 <code>Scanner</code> 抛出，表示检索到的令牌与预期类型的模式不匹配，或者令牌超出预期类型的范围。
 *
 * @author  未署名
 * @see     java.util.Scanner
 * @since   1.5
 */
public
class InputMismatchException extends NoSuchElementException {
    private static final long serialVersionUID = 8811230760997066428L;

    /**
     * 构造一个 <code>InputMismatchException</code>，其错误消息字符串为 <tt>null</tt>。
     */
    public InputMismatchException() {
        super();
    }

    /**
     * 构造一个 <code>InputMismatchException</code>，保存错误消息字符串 <tt>s</tt> 以供 <tt>getMessage</tt> 方法稍后检索。
     *
     * @param   s   详细消息。
     */
    public InputMismatchException(String s) {
        super(s);
    }
}