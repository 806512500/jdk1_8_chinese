/*
 * 版权所有 (c) 1994, 2008，Oracle 及/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 抛出此异常表示数组被非法索引访问。索引要么是负数，要么大于或等于数组的大小。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -5116101128118950844L;

    /**
     * 构造一个没有详细消息的 <code>ArrayIndexOutOfBoundsException</code>。
     */
    public ArrayIndexOutOfBoundsException() {
        super();
    }

    /**
     * 使用一个表示非法索引的参数构造一个新的 <code>ArrayIndexOutOfBoundsException</code> 类。
     *
     * @param   index   非法索引。
     */
    public ArrayIndexOutOfBoundsException(int index) {
        super("数组索引超出范围: " + index);
    }

    /**
     * 使用指定的详细消息构造一个 <code>ArrayIndexOutOfBoundsException</code> 类。
     *
     * @param   s   详细消息。
     */
    public ArrayIndexOutOfBoundsException(String s) {
        super(s);
    }
}