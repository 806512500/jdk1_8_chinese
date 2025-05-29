/*
 * 版权所有 (c) 2003, 2012, Oracle 和/或其关联公司。保留所有权利。
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
 * 当格式说明符对应的参数类型不兼容时抛出的未经检查的异常。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此类中的任何方法或构造函数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class IllegalFormatConversionException extends IllegalFormatException {

    private static final long serialVersionUID = 17000126L;

    private char c;
    private Class<?> arg;

    /**
     * 使用不匹配的转换和对应的参数类构造此类的实例。
     *
     * @param  c
     *         不适用的转换
     *
     * @param  arg
     *         不匹配参数的类
     */
    public IllegalFormatConversionException(char c, Class<?> arg) {
        if (arg == null)
            throw new NullPointerException();
        this.c = c;
        this.arg = arg;
    }

    /**
     * 返回不适用的转换。
     *
     * @return  不适用的转换
     */
    public char getConversion() {
        return c;
    }

    /**
     * 返回不匹配参数的类。
     *
     * @return   不匹配参数的类
     */
    public Class<?> getArgumentClass() {
        return arg;
    }

    // 从 Throwable.java 继承的 javadoc
    public String getMessage() {
        return String.format("%c != %s", c, arg.getName());
    }
}