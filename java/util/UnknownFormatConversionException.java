/*
 * 版权所有 (c) 2003, 2005, Oracle 和/或其关联公司。保留所有权利。
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
 * 当给定未知的转换时抛出的未检查异常。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此类的任何方法或构造函数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class UnknownFormatConversionException extends IllegalFormatException {

    private static final long serialVersionUID = 19060418L;

    private String s;

    /**
     * 使用未知的转换构造此类的实例。
     *
     * @param  s
     *         未知的转换
     */
    public UnknownFormatConversionException(String s) {
        if (s == null)
            throw new NullPointerException();
        this.s = s;
    }

    /**
     * 返回未知的转换。
     *
     * @return  未知的转换。
     */
    public String getConversion() {
        return s;
    }

    // 从 Throwable.java 继承的 javadoc
    public String getMessage() {
        return String.format("Conversion = '%s'", s);
    }
}