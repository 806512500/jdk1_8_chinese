
/*
 * 版权所有 (c) 1995, 2013，Oracle 及/或其附属公司。保留所有权利。
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
 * 抛出此异常表示尝试将错误类型的对象存储到对象数组中。例如，以下代码会生成一个 <code>ArrayStoreException</code>：
 * <blockquote><pre>
 *     Object x[] = new String[3];
 *     x[0] = new Integer(0);
 * </pre></blockquote>
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class ArrayStoreException extends RuntimeException {
    private static final long serialVersionUID = -4522193890499838241L;

    /**
     * 构造一个没有详细消息的 <code>ArrayStoreException</code>。
     */
    public ArrayStoreException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>ArrayStoreException</code>。
     *
     * @param   s   详细消息。
     */
    public ArrayStoreException(String s) {
        super(s);
    }
}