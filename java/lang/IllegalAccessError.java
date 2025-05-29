/*
 * 版权所有 (c) 1995, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 如果应用程序尝试访问或修改其没有访问权限的字段，或调用其没有访问权限的方法时抛出。
 * <p>
 * 通常，此错误会被编译器捕获；只有在类的定义不兼容地更改时，此错误才可能在运行时发生。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class IllegalAccessError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -8988904074992417891L;

    /**
     * 构造一个没有详细消息的 <code>IllegalAccessError</code>。
     */
    public IllegalAccessError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalAccessError</code>。
     *
     * @param   s   详细消息。
     */
    public IllegalAccessError(String s) {
        super(s);
    }
}
