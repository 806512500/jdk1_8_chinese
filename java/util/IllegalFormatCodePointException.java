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
 * 当传递给 {@link Formatter} 的字符具有 {@link Character#isValidCodePoint} 定义的无效 Unicode 代码点时，抛出的未检查异常。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此类中的任何方法或构造函数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class IllegalFormatCodePointException extends IllegalFormatException {

    private static final long serialVersionUID = 19080630L;

    private int c;

    /**
     * 根据 {@link Character#isValidCodePoint} 定义的指定非法代码点构造此类的实例。
     *
     * @param  c
     *         非法的 Unicode 代码点
     */
    public IllegalFormatCodePointException(int c) {
        this.c = c;
    }

    /**
     * 返回由 {@link Character#isValidCodePoint} 定义的非法代码点。
     *
     * @return  非法的 Unicode 代码点
     */
    public int getCodePoint() {
        return c;
    }

    public String getMessage() {
        return String.format("Code point = %#x", c);
    }
}