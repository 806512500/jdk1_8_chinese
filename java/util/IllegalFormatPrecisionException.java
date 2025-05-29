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
 * 当精度是一个除 <tt>-1</tt> 以外的负值，转换不支持精度，或者值以其他方式不受支持时抛出的未检查异常。
 *
 * @since 1.5
 */
public class IllegalFormatPrecisionException extends IllegalFormatException {

    private static final long serialVersionUID = 18711008L;

    private int p;

    /**
     * 使用指定的精度构造此类的一个实例。
     *
     * @param  p
     *         精度
     */
    public IllegalFormatPrecisionException(int p) {
        this.p = p;
    }

    /**
     * 返回精度
     *
     * @return  精度
     */
    public int getPrecision() {
        return p;
    }

    public String getMessage() {
        return Integer.toString(p);
    }
}