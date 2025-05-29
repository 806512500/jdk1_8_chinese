/*
 * 版权所有 (c) 2004, 2010, Oracle 和/或其附属公司。保留所有权利。
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
 * FomattableFlags 被传递给 {@link Formattable#formatTo Formattable.formatTo()} 方法，并修改 {@linkplain
 * Formattable Formattables} 的输出格式。 {@link Formattable} 的实现负责解释和验证任何标志。
 *
 * @since  1.5
 */
public class FormattableFlags {

    // 禁止显式实例化此类。
    private FormattableFlags() {}

    /**
     * 左对齐输出。将在转换值的末尾添加空格 (<tt>'&#92;u0020'</tt>) 以填充字段的最小宽度。如果未设置此标志，则输出将右对齐。
     *
     * <p> 此标志对应于格式说明符中的 <tt>'-'</tt> (<tt>'&#92;u002d'</tt>)。
     */
    public static final int LEFT_JUSTIFY = 1<<0; // '-'

    /**
     * 根据创建 <tt>formatter</tt> 参数时提供的 {@linkplain java.util.Locale 语言环境} 的规则将输出转换为大写。输出应等效于以下调用 {@link String#toUpperCase(java.util.Locale)}
     *
     * <pre>
     *     out.toUpperCase() </pre>
     *
     * <p> 此标志对应于格式说明符中的 <tt>'S'</tt> (<tt>'&#92;u0053'</tt>)。
     */
    public static final int UPPERCASE = 1<<1;    // 'S'

    /**
     * 要求输出使用替代形式。形式的定义由 <tt>Formattable</tt> 指定。
     *
     * <p> 此标志对应于格式说明符中的 <tt>'#'</tt> (<tt>'&#92;u0023'</tt>)。
     */
    public static final int ALTERNATE = 1<<2;    // '#'
}