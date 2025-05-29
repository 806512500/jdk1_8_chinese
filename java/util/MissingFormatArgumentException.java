/*
 * 版权所有 (c) 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 当存在没有对应参数的格式说明符，或者参数索引引用了不存在的参数时抛出的未检查异常。
 *
 * <p>除非另有说明，向此类中的任何方法或构造函数传递 <tt>null</tt> 参数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class MissingFormatArgumentException extends IllegalFormatException {

    private static final long serialVersionUID = 19190115L;

    private String s;

    /**
     * 使用不匹配的格式说明符构造此类的实例。
     *
     * @param  s
     *         没有对应参数的格式说明符
     */
    public MissingFormatArgumentException(String s) {
        if (s == null)
            throw new NullPointerException();
        this.s = s;
    }

    /**
     * 返回不匹配的格式说明符。
     *
     * @return  不匹配的格式说明符
     */
    public String getFormatSpecifier() {
        return s;
    }

    public String getMessage() {
        return "格式说明符 '" + s + "'";
    }
}