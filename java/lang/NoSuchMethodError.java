/*
 * 版权所有 (c) 1994, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 如果应用程序尝试调用类的指定方法（无论是静态的还是实例的），而该类不再有该方法的定义时抛出。
 * <p>
 * 通常，此错误会被编译器捕获；此错误只有在类的定义不兼容地更改时，才可能在运行时发生。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NoSuchMethodError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -3765521442372831335L;

    /**
     * 构造一个没有详细消息的 <code>NoSuchMethodError</code>。
     */
    public NoSuchMethodError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NoSuchMethodError</code>。
     *
     * @param   s   详细消息。
     */
    public NoSuchMethodError(String s) {
        super(s);
    }
}
