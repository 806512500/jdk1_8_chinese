/*
 * 版权所有 (c) 1994, 2011, Oracle 和/或其附属公司。保留所有权利。
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
 * 抽象类 {@code Number} 是表示可转换为原始类型 {@code byte}，{@code double}，{@code float}，
 * {@code int}，{@code long} 和 {@code short} 的数值的平台类的超类。
 *
 * 从特定的 {@code Number} 实现的数值到给定原始类型的转换的具体语义由该 {@code Number} 实现定义。
 *
 * 对于平台类，转换通常类似于 <cite>The Java&trade; Language Specification</cite> 中定义的
 * 原始类型之间的扩展原始转换或缩小原始转换。因此，转换可能会丢失数值的整体大小信息，可能会丢失精度，甚至可能会返回与输入符号不同的结果。
 *
 * 有关转换详情，请参阅给定的 {@code Number} 实现的文档。
 *
 * @author      Lee Boynton
 * @author      Arthur van Hoff
 * @jls 5.1.2 扩展原始转换
 * @jls 5.1.3 缩小原始转换
 * @since   JDK1.0
 */
public abstract class Number implements java.io.Serializable {
    /**
     * 返回指定数字的值作为 {@code int}，这可能涉及舍入或截断。
     *
     * @return  转换为类型 {@code int} 后，此对象表示的数值。
     */
    public abstract int intValue();

    /**
     * 返回指定数字的值作为 {@code long}，这可能涉及舍入或截断。
     *
     * @return  转换为类型 {@code long} 后，此对象表示的数值。
     */
    public abstract long longValue();

    /**
     * 返回指定数字的值作为 {@code float}，这可能涉及舍入。
     *
     * @return  转换为类型 {@code float} 后，此对象表示的数值。
     */
    public abstract float floatValue();

    /**
     * 返回指定数字的值作为 {@code double}，这可能涉及舍入。
     *
     * @return  转换为类型 {@code double} 后，此对象表示的数值。
     */
    public abstract double doubleValue();

    /**
     * 返回指定数字的值作为 {@code byte}，这可能涉及舍入或截断。
     *
     * <p>此实现返回 {@link #intValue} 转换为 {@code byte} 的结果。
     *
     * @return  转换为类型 {@code byte} 后，此对象表示的数值。
     * @since   JDK1.1
     */
    public byte byteValue() {
        return (byte)intValue();
    }

    /**
     * 返回指定数字的值作为 {@code short}，这可能涉及舍入或截断。
     *
     * <p>此实现返回 {@link #intValue} 转换为 {@code short} 的结果。
     *
     * @return  转换为类型 {@code short} 后，此对象表示的数值。
     * @since   JDK1.1
     */
    public short shortValue() {
        return (short)intValue();
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -8742448824652078965L;
}
