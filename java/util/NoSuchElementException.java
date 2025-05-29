/*
 * 版权所有 (c) 1994, 2012，Oracle和/或其附属公司。保留所有权利。
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
 * 由各种访问方法抛出，以指示请求的元素不存在。
 *
 * @author  未署名
 * @see     java.util.Enumeration#nextElement()
 * @see     java.util.Iterator#next()
 * @since   JDK1.0
 */
public
class NoSuchElementException extends RuntimeException {
    private static final long serialVersionUID = 6769829250639411880L;

    /**
     * 构造一个错误消息字符串为 <tt>null</tt> 的 <code>NoSuchElementException</code>。
     */
    public NoSuchElementException() {
        super();
    }

    /**
     * 构造一个 <code>NoSuchElementException</code>，保存错误消息字符串 <tt>s</tt> 的引用，
     * 以便稍后通过 <tt>getMessage</tt> 方法检索。
     *
     * @param   s   详细消息。
     */
    public NoSuchElementException(String s) {
        super(s);
    }
}