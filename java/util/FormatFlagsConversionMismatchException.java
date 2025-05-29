/*
 * 版权所有 (c) 2003, Oracle 和/或其关联公司。保留所有权利。
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
 * 当转换和标志不兼容时抛出的未经检查的异常。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此类中的任何方法或构造函数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class FormatFlagsConversionMismatchException
    extends IllegalFormatException
{
    private static final long serialVersionUID = 19120414L;

    private String f;

    private char c;

    /**
     * 使用指定的标志和转换构造此类的实例。
     *
     * @param  f
     *         标志
     *
     * @param  c
     *         转换
     */
    public FormatFlagsConversionMismatchException(String f, char c) {
        if (f == null)
            throw new NullPointerException();
        this.f = f;
        this.c = c;
    }

    /**
     * 返回不兼容的标志。
     *
     * @return  标志
     */
     public String getFlags() {
        return f;
    }

    /**
     * 返回不兼容的转换。
     *
     * @return  转换
     */
    public char getConversion() {
        return c;
    }

    public String getMessage() {
        return "Conversion = " + c + ", Flags = " + f;
    }
}