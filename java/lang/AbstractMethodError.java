/*
 * 版权所有 (c) 1994, 2008，Oracle 及/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款的约束。
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
 * 当应用程序尝试调用抽象方法时抛出。
 * 通常，这个错误会被编译器捕获；只有在当前执行的方法自上次编译以来，某个类的定义发生了不兼容的更改时，这个错误才会在运行时出现。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class AbstractMethodError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -1654391082989018462L;

    /**
     * 构造一个没有详细信息消息的 <code>AbstractMethodError</code>。
     */
    public AbstractMethodError() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>AbstractMethodError</code>。
     *
     * @param   s   详细信息消息。
     */
    public AbstractMethodError(String s) {
        super(s);
    }
}