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
 * 当格式宽度为负值且不等于 <tt>-1</tt> 或其他不受支持的值时抛出的未检查异常。
 *
 * @since 1.5
 */
public class IllegalFormatWidthException extends IllegalFormatException {

    private static final long serialVersionUID = 16660902L;

    private int w;

    /**
     * 使用指定的宽度构造此类的实例。
     *
     * @param  w
     *         宽度
     */
    public IllegalFormatWidthException(int w) {
        this.w = w;
    }

    /**
     * 返回宽度
     *
     * @return  宽度
     */
    public int getWidth() {
        return w;
    }

    public String getMessage() {
        return Integer.toString(w);
    }
}