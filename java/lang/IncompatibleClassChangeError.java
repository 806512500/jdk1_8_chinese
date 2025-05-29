/*
 * 版权所有 (c) 1994, 2008，Oracle和/或其附属公司。保留所有权利。
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
 * 当某些类定义发生不兼容的类更改时抛出。
 * 当前执行方法所依赖的某个类的定义已更改。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class IncompatibleClassChangeError extends LinkageError {
    private static final long serialVersionUID = -4914975503642802119L;

    /**
     * 构造一个没有详细消息的 <code>IncompatibleClassChangeError</code>。
     */
    public IncompatibleClassChangeError () {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IncompatibleClassChangeError</code>。
     *
     * @param   s   详细消息。
     */
    public IncompatibleClassChangeError(String s) {
        super(s);
    }
}
