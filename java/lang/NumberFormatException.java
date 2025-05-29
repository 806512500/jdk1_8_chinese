/*
 * 版权所有 (c) 1994, 2012, Oracle 和/或其附属公司。保留所有权利。
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
 * 抛出以指示应用程序尝试将字符串转换为数字类型之一，但该字符串没有适当的格式。
 *
 * @author  未署名
 * @see     java.lang.Integer#parseInt(String)
 * @since   JDK1.0
 */
public
class NumberFormatException extends IllegalArgumentException {
    static final long serialVersionUID = -2848938806368998894L;

    /**
     * 构造一个没有详细消息的 <code>NumberFormatException</code>。
     */
    public NumberFormatException () {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NumberFormatException</code>。
     *
     * @param   s   详细消息。
     */
    public NumberFormatException (String s) {
        super (s);
    }

    /**
     * 用于创建 <code>NumberFormatException</code> 的工厂方法，给定导致错误的指定输入。
     *
     * @param   s   导致错误的输入
     */
    static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }
}
