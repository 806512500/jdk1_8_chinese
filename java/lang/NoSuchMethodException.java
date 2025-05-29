/*
 * 版权所有 (c) 1995, 2008，Oracle和/或其附属公司。保留所有权利。
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
 * 当无法找到特定方法时抛出。
 *
 * @author     未署名
 * @since      JDK1.0
 */
public
class NoSuchMethodException extends ReflectiveOperationException {
    private static final long serialVersionUID = 5034388446362600923L;

    /**
     * 构造一个没有详细消息的 <code>NoSuchMethodException</code>。
     */
    public NoSuchMethodException() {
        super();
    }

    /**
     * 使用详细消息构造一个 <code>NoSuchMethodException</code>。
     *
     * @param      s   详细消息。
     */
    public NoSuchMethodException(String s) {
        super(s);
    }
}
