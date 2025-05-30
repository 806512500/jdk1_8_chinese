
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import sun.misc.FloatingDecimal;
import sun.misc.FloatConsts;
import sun.misc.DoubleConsts;

/**
 * {@code Float} 类将原始类型 {@code float} 的值包装在一个对象中。一个 {@code Float} 类型的对象包含一个类型为 {@code float} 的单个字段。
 *
 * <p>此外，该类还提供了几种将 {@code float} 转换为 {@code String} 和将 {@code String} 转换为 {@code float} 的方法，以及其他在处理 {@code float} 时有用的常量和方法。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Float extends Number implements Comparable<Float> {
    /**
     * 一个常量，表示类型为 {@code float} 的正无穷大。它等于 {@code Float.intBitsToFloat(0x7f800000)} 返回的值。
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的负无穷大。它等于 {@code Float.intBitsToFloat(0xff800000)} 返回的值。
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的非数字（NaN）值。它等于 {@code Float.intBitsToFloat(0x7fc00000)} 返回的值。
     */
    public static final float NaN = 0.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的最大正有限值，(2-2<sup>-23</sup>)&middot;2<sup>127</sup>。它等于十六进制浮点字面量
     * {@code 0x1.fffffeP+127f}，也等于 {@code Float.intBitsToFloat(0x7f7fffff)}。
     */
    public static final float MAX_VALUE = 0x1.fffffeP+127f; // 3.4028235e+38f

    /**
     * 一个常量，表示类型为 {@code float} 的最小正正规值，2<sup>-126</sup>。它等于十六进制浮点字面量 {@code 0x1.0p-126f}，也等于
     * {@code Float.intBitsToFloat(0x00800000)}。
     *
     * @since 1.6
     */
    public static final float MIN_NORMAL = 0x1.0p-126f; // 1.17549435E-38f

    /**
     * 一个常量，表示类型为 {@code float} 的最小正非零值，2<sup>-149</sup>。它等于十六进制浮点字面量 {@code 0x0.000002P-126f}
     * 也等于 {@code Float.intBitsToFloat(0x1)}。
     */
    public static final float MIN_VALUE = 0x0.000002P-126f; // 1.4e-45f

    /**
     * 一个常量，表示类型为 {@code float} 的有限值可以具有的最大指数。它等于 {@code Math.getExponent(Float.MAX_VALUE)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 127;

    /**
     * 一个常量，表示类型为 {@code float} 的正规值可以具有的最小指数。它等于 {@code Math.getExponent(Float.MIN_NORMAL)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -126;

    /**
     * 一个常量，表示表示 {@code float} 值所需的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 32;

    /**
     * 一个常量，表示表示 {@code float} 值所需的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 表示原始类型 {@code float} 的 {@code Class} 实例。
     *
     * @since JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE = (Class<Float>) Class.getPrimitiveClass("float");

    /**
     * 返回 {@code float} 参数的字符串表示形式。所有提到的字符都是 ASCII 字符。
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数的符号和大小（绝对值）的字符串。如果符号为负，结果的第一个字符是 '{@code -}' ({@code '\u005Cu002D'})；
     *     如果符号为正，则结果中不出现符号字符。至于大小 <i>m</i>：
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符 {@code "Infinity"} 表示；因此，正无穷大产生结果 {@code "Infinity"}，负无穷大产生结果 {@code "-Infinity"}。
     * <li>如果 <i>m</i> 是零，它由字符 {@code "0.0"} 表示；因此，负零产生结果 {@code "-0.0"}，正零产生结果 {@code "0.0"}。
     * <li>如果 <i>m</i> 大于或等于 10<sup>-3</sup> 但小于 10<sup>7</sup>，则它表示为 <i>m</i> 的整数部分，以十进制形式表示，没有前导零，后跟 '{@code .}'
     *     ({@code '\u005Cu002E'})，后跟一个或多个表示 <i>m</i> 小数部分的十进制数字。
     * <li>如果 <i>m</i> 小于 10<sup>-3</sup> 或大于或等于 10<sup>7</sup>，则它以所谓的“计算机科学记数法”表示。设 <i>n</i> 是唯一的整数，使得
     *     10<sup><i>n</i> </sup>&le; <i>m</i> {@literal <} 10<sup><i>n</i>+1</sup>；则设 <i>a</i> 是 <i>m</i> 和 10<sup><i>n</i></sup> 的数学精确商，使得 1 &le; <i>a</i> {@literal <} 10。
     *     大小表示为 <i>a</i> 的整数部分，以单个十进制数字表示，后跟 '{@code .}' ({@code '\u005Cu002E'})，后跟表示 <i>a</i> 小数部分的十进制数字，后跟字母 '{@code E}'
     *     ({@code '\u005Cu0045'})，后跟 <i>n</i> 的十进制表示形式，由方法 {@link java.lang.Integer#toString(int)} 生成。
     *
     * </ul>
     * </ul>
     * <i>m</i> 或 <i>a</i> 的小数部分必须打印多少位？必须至少有一位表示小数部分，除此之外，还需要尽可能多的位数，但仅需足够区分参数值与类型
     * {@code float} 的相邻值。也就是说，假设 <i>x</i> 是由该方法为有限非零参数 <i>f</i> 生成的十进制表示形式所表示的精确数学值。那么 <i>f</i> 必须是与 <i>x</i> 最接近的
     * {@code float} 值；或者，如果两个 {@code float} 值与 <i>x</i> 距离相等，则 <i>f</i> 必须是其中之一，且 <i>f</i> 的尾数的最低有效位必须为 {@code 0}。
     *
     * <p>要创建浮点值的本地化字符串表示形式，请使用 {@link java.text.NumberFormat} 的子类。
     *
     * @param   f   要转换的浮点数。
     * @return 参数的字符串表示形式。
     */
    public static String toString(float f) {
        return FloatingDecimal.toJavaFormatString(f);
    }

    /**
     * 返回 {@code float} 参数的十六进制字符串表示形式。所有提到的字符都是 ASCII 字符。
     *
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数的符号和大小（绝对值）的字符串。如果符号为负，结果的第一个字符是 '{@code -}'
     *     ({@code '\u005Cu002D'})；如果符号为正，则结果中不出现符号字符。至于大小 <i>m</i>：
     *
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符串 {@code "Infinity"} 表示；因此，正无穷大产生结果 {@code "Infinity"}，负无穷大产生结果 {@code "-Infinity"}。
     *
     * <li>如果 <i>m</i> 是零，它由字符串 {@code "0x0.0p0"} 表示；因此，负零产生结果 {@code "-0x0.0p0"}，正零产生结果 {@code "0x0.0p0"}。
     *
     * <li>如果 <i>m</i> 是具有正规表示形式的 {@code float} 值，则使用子字符串表示尾数和指数字段。尾数由字符 {@code "0x1."}
     *     后跟尾数其余部分的小写十六进制表示形式表示。移除尾数的十六进制表示形式中的尾随零，除非所有数字都是零，在这种情况下使用单个零。接下来，指数由
     *     {@code "p"} 后跟无偏指数的十进制字符串表示，该字符串由对指数值的调用 {@link Integer#toString(int) Integer.toString} 生成。
     *
     * <li>如果 <i>m</i> 是具有非正规表示形式的 {@code float} 值，则尾数由字符 {@code "0x0."} 后跟尾数其余部分的
     *     十六进制表示形式表示。移除尾数的十六进制表示形式中的尾随零。接下来，指数由
     *     {@code "p-126"} 表示。注意，非正规尾数中必须至少有一个非零数字。
     *
     * </ul>
     *
     * </ul>
     *
     * <table border>
     * <caption>示例</caption>
     * <tr><th>浮点值</th><th>十六进制字符串</th>
     * <tr><td>{@code 1.0}</td> <td>{@code 0x1.0p0}</td>
     * <tr><td>{@code -1.0}</td>        <td>{@code -0x1.0p0}</td>
     * <tr><td>{@code 2.0}</td> <td>{@code 0x1.0p1}</td>
     * <tr><td>{@code 3.0}</td> <td>{@code 0x1.8p1}</td>
     * <tr><td>{@code 0.5}</td> <td>{@code 0x1.0p-1}</td>
     * <tr><td>{@code 0.25}</td>        <td>{@code 0x1.0p-2}</td>
     * <tr><td>{@code Float.MAX_VALUE}</td>
     *     <td>{@code 0x1.fffffep127}</td>
     * <tr><td>{@code 最小正规值}</td>
     *     <td>{@code 0x1.0p-126}</td>
     * <tr><td>{@code 最大非正规值}</td>
     *     <td>{@code 0x0.fffffep-126}</td>
     * <tr><td>{@code Float.MIN_VALUE}</td>
     *     <td>{@code 0x0.000002p-126}</td>
     * </table>
     * @param   f   要转换的浮点数。
     * @return 参数的十六进制字符串表示形式。
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public static String toHexString(float f) {
        if (Math.abs(f) < FloatConsts.MIN_NORMAL
            &&  f != 0.0f ) {// 浮点数非正规
            // 调整指数以创建非正规双精度数，然后
            // 用非正规浮点数的指数替换非正规双精度数的指数
            String s = Double.toHexString(Math.scalb((double)f,
                                                     /* -1022+126 */
                                                     DoubleConsts.MIN_EXPONENT-
                                                     FloatConsts.MIN_EXPONENT));
            return s.replaceFirst("p-1022$", "p-126");
        }
        else // 双精度字符串将与浮点字符串相同
            return Double.toHexString(f);
    }

    /**
     * 返回一个包含由字符串 {@code s} 表示的 {@code float} 值的 {@code Float} 对象。
     *
     * <p>如果 {@code s} 为 {@code null}，则抛出 {@code NullPointerException}。
     *
     * <p>忽略 {@code s} 中的前导和尾随空白字符。空白字符的处理方式与 {@link String#trim} 方法相同；即，同时移除 ASCII 空格和控制字符。其余的 {@code s} 应该
     * 构成一个 <i>FloatValue</i>，如词法语法规则所述：
     *
     * <blockquote>
     * <dl>
     * <dt><i>FloatValue:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code NaN}
     * <dd><i>Sign<sub>opt</sub></i> {@code Infinity}
     * <dd><i>Sign<sub>opt</sub> FloatingPointLiteral</i>
     * <dd><i>Sign<sub>opt</sub> HexFloatingPointLiteral</i>
     * <dd><i>SignedInteger</i>
     * </dl>
     *
     * <dl>
     * <dt><i>HexFloatingPointLiteral</i>:
     * <dd> <i>HexSignificand BinaryExponent FloatTypeSuffix<sub>opt</sub></i>
     * </dl>
     *
     * <dl>
     * <dt><i>HexSignificand:</i>
     * <dd><i>HexNumeral</i>
     * <dd><i>HexNumeral</i> {@code .}
     * <dd>{@code 0x} <i>HexDigits<sub>opt</sub>
     *     </i>{@code .}<i> HexDigits</i>
     * <dd>{@code 0X}<i> HexDigits<sub>opt</sub>
     *     </i>{@code .} <i>HexDigits</i>
     * </dl>
     *
     * <dl>
     * <dt><i>BinaryExponent:</i>
     * <dd><i>BinaryExponentIndicator SignedInteger</i>
     * </dl>
     *
     * <dl>
     * <dt><i>BinaryExponentIndicator:</i>
     * <dd>{@code p}
     * <dd>{@code P}
     * </dl>
     *
     * </blockquote>
     *
     * 其中 <i>Sign</i>、<i>FloatingPointLiteral</i>、<i>HexNumeral</i>、<i>HexDigits</i>、<i>SignedInteger</i> 和
     * <i>FloatTypeSuffix</i> 的定义见
     * <cite>The Java&trade; Language Specification</cite> 的词法结构部分，但不接受数字之间的下划线。
     * 如果 {@code s} 不具有 <i>FloatValue</i> 的形式，则抛出 {@code NumberFormatException}。否则，认为 {@code s} 表示一个精确的十进制值，通常以
     * “计算机科学记数法”或精确的十六进制值表示；该精确数值概念上转换为“无限精确”的二进制值，然后通过 IEEE 754 浮点算术的通常的就近舍入规则舍入为类型 {@code float}，
     * 包括保留零值的符号。
     *
     * 注意，就近舍入规则还意味着溢出和下溢行为；如果 {@code s} 的精确值的大小足够大（大于或等于 ({@link
     * #MAX_VALUE} + {@link Math#ulp(float) ulp(MAX_VALUE)}/2)，
     * 舍入到 {@code float} 将产生无穷大；如果 {@code s} 的精确值的大小足够小（小于或等于 {@link #MIN_VALUE}/2），舍入到浮点数将产生零。
     *
     * 最后，舍入后返回一个表示此 {@code float} 值的 {@code Float} 对象。
     *
     * <p>要解释浮点值的本地化字符串表示形式，请使用 {@link
     * java.text.NumberFormat} 的子类。
     *
     * <p>注意，尾随格式说明符，确定浮点字面量类型的说明符
     * ({@code 1.0f} 是一个 {@code float} 值；
     * {@code 1.0d} 是一个 {@code double} 值)，不
     * 影响此方法的结果。换句话说，输入字符串的数值直接转换为目标浮点类型。通常，将字符串转换为 {@code double}，然后再转换为
     * {@code float} 的两步序列转换，与直接将字符串转换为
     * {@code float} 不等价。例如，如果先转换为中间的
     * {@code double}，然后再转换为
     * {@code float}，字符串<br>
     * {@code "1.00000017881393421514957253748434595763683319091796875001d"}<br>
     * 产生的 {@code float} 值为
     * {@code 1.0000002f}；如果直接转换为
     * {@code float}，结果为 <code>1.000000<b>1</b>f</code>。
     *
     * <p>为了避免在无效字符串上调用此方法并抛出 {@code NumberFormatException}，{@link Double#valueOf Double.valueOf} 的文档列出了一个正则表达式，可以用来筛选输入。
     *
     * @param   s   要解析的字符串。
     * @return  一个包含由 {@code String} 参数表示的值的 {@code Float} 对象。
     * @throws  NumberFormatException  如果字符串不包含可解析的数字。
     */
    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(parseFloat(s));
    }


/**
 * 返回表示指定的 {@code float} 值的 {@code Float} 实例。
 * 如果不需要新的 {@code Float} 实例，通常应优先使用此方法，而不是构造函数
 * {@link #Float(float)}，因为此方法通过缓存经常请求的值，可能会显著提高空间和时间性能。
 *
 * @param  f 一个浮点值。
 * @return 表示 {@code f} 的 {@code Float} 实例。
 * @since  1.5
 */
public static Float valueOf(float f) {
    return new Float(f);
}

/**
 * 返回一个新的 {@code float}，其值由指定的 {@code String} 表示，如类 {@code Float} 的
 * {@code valueOf} 方法所执行的。
 *
 * @param  s 要解析的字符串。
 * @return 由字符串参数表示的 {@code float} 值。
 * @throws NullPointerException  如果字符串为 null
 * @throws NumberFormatException 如果字符串不包含可解析的 {@code float}。
 * @see    java.lang.Float#valueOf(String)
 * @since 1.2
 */
public static float parseFloat(String s) throws NumberFormatException {
    return FloatingDecimal.parseFloat(s);
}

/**
 * 如果指定的数字是 Not-a-Number (NaN) 值，则返回 {@code true}，否则返回 {@code false}。
 *
 * @param   v   要测试的值。
 * @return  如果参数是 NaN，则返回 {@code true}；否则返回 {@code false}。
 */
public static boolean isNaN(float v) {
    return (v != v);
}

/**
 * 如果指定的数字在数值上是无限的，则返回 {@code true}，否则返回 {@code false}。
 *
 * @param   v   要测试的值。
 * @return  如果参数是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
 */
public static boolean isInfinite(float v) {
    return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
}


/**
 * 如果参数是有限的浮点值，则返回 {@code true}；否则返回 {@code false}（对于 NaN 和无穷大参数）。
 *
 * @param f 要测试的 {@code float} 值
 * @return 如果参数是有限的浮点值，则返回 {@code true}，否则返回 {@code false}。
 * @since 1.8
 */
public static boolean isFinite(float f) {
    return Math.abs(f) <= FloatConsts.MAX_VALUE;
}

/**
 * 浮点数的值。
 *
 * @serial
 */
private final float value;

/**
 * 构造一个新的分配的 {@code Float} 对象，表示原始的 {@code float} 参数。
 *
 * @param   value   要表示的 {@code float} 值。
 */
public Float(float value) {
    this.value = value;
}

/**
 * 构造一个新的分配的 {@code Float} 对象，表示转换为类型 {@code float} 的参数。
 *
 * @param   value   要表示的值。
 */
public Float(double value) {
    this.value = (float)value;
}

/**
 * 构造一个新的分配的 {@code Float} 对象，表示由字符串表示的浮点值。字符串被转换为
 * {@code float} 值，就像 {@code valueOf} 方法所做的那样。
 *
 * @param      s   要转换为 {@code Float} 的字符串。
 * @throws  NumberFormatException  如果字符串不包含可解析的数字。
 * @see        java.lang.Float#valueOf(java.lang.String)
 */
public Float(String s) throws NumberFormatException {
    value = parseFloat(s);
}

/**
 * 如果此 {@code Float} 值是 Not-a-Number (NaN)，则返回 {@code true}，否则返回 {@code false}。
 *
 * @return  如果此对象表示的值是 NaN，则返回 {@code true}；否则返回 {@code false}。
 */
public boolean isNaN() {
    return isNaN(value);
}

/**
 * 如果此 {@code Float} 值在数值上是无限的，则返回 {@code true}，否则返回 {@code false}。
 *
 * @return  如果此对象表示的值是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
 */
public boolean isInfinite() {
    return isInfinite(value);
}

/**
 * 返回此 {@code Float} 对象的字符串表示形式。此对象表示的原始 {@code float} 值被转换为
 * {@code String}，就像带有一个参数的 {@code toString} 方法所做的那样。
 *
 * @return  此对象的 {@code String} 表示形式。
 * @see java.lang.Float#toString(float)
 */
public String toString() {
    return Float.toString(value);
}

/**
 * 返回此 {@code Float} 值经过缩小原始转换后的 {@code byte} 值。
 *
 * @return  此对象表示的 {@code float} 值转换为类型 {@code byte}。
 * @jls 5.1.3 Narrowing Primitive Conversions
 */
public byte byteValue() {
    return (byte)value;
}

/**
 * 返回此 {@code Float} 值经过缩小原始转换后的 {@code short} 值。
 *
 * @return  此对象表示的 {@code float} 值转换为类型 {@code short}。
 * @jls 5.1.3 Narrowing Primitive Conversions
 * @since JDK1.1
 */
public short shortValue() {
    return (short)value;
}

/**
 * 返回此 {@code Float} 值经过缩小原始转换后的 {@code int} 值。
 *
 * @return  此对象表示的 {@code float} 值转换为类型 {@code int}。
 * @jls 5.1.3 Narrowing Primitive Conversions
 */
public int intValue() {
    return (int)value;
}

/**
 * 返回此 {@code Float} 值经过缩小原始转换后的 {@code long} 值。
 *
 * @return  此对象表示的 {@code float} 值转换为类型 {@code long}。
 * @jls 5.1.3 Narrowing Primitive Conversions
 */
public long longValue() {
    return (long)value;
}

/**
 * 返回此 {@code Float} 对象的 {@code float} 值。
 *
 * @return 此对象表示的 {@code float} 值
 */
public float floatValue() {
    return value;
}

/**
 * 返回此 {@code Float} 值经过扩展原始转换后的 {@code double} 值。
 *
 * @return 此对象表示的 {@code float} 值转换为类型 {@code double}。
 * @jls 5.1.2 Widening Primitive Conversions
 */
public double doubleValue() {
    return (double)value;
}

/**
 * 返回此 {@code Float} 对象的哈希码。结果是此 {@code Float} 对象表示的原始
 * {@code float} 值的整数位表示，完全由方法 {@link #floatToIntBits(float)} 生成。
 *
 * @return 此对象的哈希码值。
 */
@Override
public int hashCode() {
    return Float.hashCode(value);
}

/**
 * 返回 {@code float} 值的哈希码；与 {@code Float.hashCode()} 兼容。
 *
 * @param value 要哈希的值
 * @return 一个 {@code float} 值的哈希码值。
 * @since 1.8
 */
public static int hashCode(float value) {
    return floatToIntBits(value);
}

/**
 * 将此对象与指定的对象进行比较。结果为 {@code true} 当且仅当参数不为
 * {@code null} 且是一个表示与此对象表示的 {@code float} 值相同的 {@code float} 的
 * {@code Float} 对象。为此，两个 {@code float} 值被认为是相同的当且仅当
 * 方法 {@link #floatToIntBits(float)} 应用于每个值时返回相同的 {@code int} 值。
 *
 * <p>注意，对于类 {@code Float} 的两个实例，{@code f1} 和 {@code f2}，值
 * {@code f1.equals(f2)} 为 {@code true} 当且仅当
 *
 * <blockquote><pre>
 *   f1.floatValue() == f2.floatValue()
 * </pre></blockquote>
 *
 * <p>也具有值 {@code true}。但是，有两个例外：
 * <ul>
 * <li>如果 {@code f1} 和 {@code f2} 都表示 {@code Float.NaN}，则 {@code equals} 方法返回
 *     {@code true}，即使 {@code Float.NaN==Float.NaN} 的值为 {@code false}。
 * <li>如果 {@code f1} 表示 {@code +0.0f} 而 {@code f2} 表示 {@code -0.0f}，或反之亦然，
 *     则 {@code equal} 测试的值为 {@code false}，即使 {@code 0.0f==-0.0f} 的值为 {@code true}。
 * </ul>
 *
 * 此定义允许哈希表正常工作。
 *
 * @param obj 要比较的对象
 * @return  如果对象相同，则返回 {@code true}；否则返回 {@code false}。
 * @see java.lang.Float#floatToIntBits(float)
 */
public boolean equals(Object obj) {
    return (obj instanceof Float)
           && (floatToIntBits(((Float)obj).value) == floatToIntBits(value));
}

/**
 * 返回指定浮点值的 IEEE 754 浮点 "单精度格式" 位布局表示。
 *
 * <p>位 31（由掩码 {@code 0x80000000} 选择的位）表示浮点数的符号。
 * 位 30-23（由掩码 {@code 0x7f800000} 选择的位）表示指数。
 * 位 22-0（由掩码 {@code 0x007fffff} 选择的位）表示浮点数的尾数（有时称为尾数）。
 *
 * <p>如果参数是正无穷大，结果是 {@code 0x7f800000}。
 *
 * <p>如果参数是负无穷大，结果是 {@code 0xff800000}。
 *
 * <p>如果参数是 NaN，结果是 {@code 0x7fc00000}。
 *
 * <p>在所有情况下，结果是一个整数，当传递给 {@link #intBitsToFloat(int)} 方法时，将生成一个
 * 与传递给 {@code floatToIntBits} 的参数相同的浮点值（所有 NaN 值都折叠为一个“规范”NaN 值）。
 *
 * @param   value   一个浮点数。
 * @return 表示浮点数的位。
 */
public static int floatToIntBits(float value) {
    int result = floatToRawIntBits(value);
    // 根据位字段值、最大指数和非零尾数检查 NaN。
    if ( ((result & FloatConsts.EXP_BIT_MASK) ==
          FloatConsts.EXP_BIT_MASK) &&
         (result & FloatConsts.SIGNIF_BIT_MASK) != 0)
        result = 0x7fc00000;
    return result;
}

/**
 * 返回指定浮点值的 IEEE 754 浮点 "单精度格式" 位布局表示，保留 Not-a-Number (NaN) 值。
 *
 * <p>位 31（由掩码 {@code 0x80000000} 选择的位）表示浮点数的符号。
 * 位 30-23（由掩码 {@code 0x7f800000} 选择的位）表示指数。
 * 位 22-0（由掩码 {@code 0x007fffff} 选择的位）表示浮点数的尾数（有时称为尾数）。
 *
 * <p>如果参数是正无穷大，结果是 {@code 0x7f800000}。
 *
 * <p>如果参数是负无穷大，结果是 {@code 0xff800000}。
 *
 * <p>如果参数在范围 {@code 0x7f800001} 到 {@code 0x7fffffff} 或
 * 范围 {@code 0xff800001} 到 {@code 0xffffffff} 之间，结果是一个 NaN。没有 IEEE 754
 * 浮点操作可以区分相同类型的两个 NaN 值的不同位模式。不同的 NaN 值只能通过使用
 * {@code Float.floatToRawIntBits} 方法来区分。
 *
 * <p>在所有其他情况下，可以从参数计算出三个值 <i>s</i>、<i>e</i> 和 <i>m</i>：
 *
 * <blockquote><pre>{@code
 * int s = ((bits >> 31) == 0) ? 1 : -1;
 * int e = ((bits >> 23) & 0xff);
 * int m = (e == 0) ?
 *                 (bits & 0x7fffff) << 1 :
 *                 (bits & 0x7fffff) | 0x800000;
 * }</pre></blockquote>
 *
 * 那么浮点结果等于数学表达式 <i>s</i>&middot;<i>m</i>&middot;2<sup><i>e</i>-150</sup>。
 *
 * <p>请注意，此方法可能无法返回一个具有与 {@code int} 参数完全相同位模式的
 * {@code float} NaN。IEEE 754 区分两种 NaN，安静 NaN 和 <i>信号 NaN</i>。两种 NaN 之间的
 * 区别在 Java 中通常不可见。对信号 NaN 的算术运算会将其转换为具有不同但通常相似位模式的安静 NaN。
 * 然而，在某些处理器上，仅仅复制一个信号 NaN 也会执行这种转换。特别是，将信号 NaN 复制以返回给调用方法
 * 可能会执行这种转换。因此，{@code intBitsToFloat} 可能无法返回一个具有信号 NaN 位模式的
 * {@code float}。因此，对于某些 {@code int} 值，{@code floatToRawIntBits(intBitsToFloat(start))}
 * 可能 <i>不</i> 等于 {@code start}。此外，表示信号 NaN 的特定位模式是平台相关的；尽管所有 NaN 位模式，
 * 无论是安静的还是信号的，都必须在上述 NaN 范围内。
 *
 * @param   bits   一个整数。
 * @return  具有相同位模式的 {@code float} 浮点值。
 */
public static native float intBitsToFloat(int bits);


                /**
     * 比较两个 {@code Float} 对象的数值。此方法执行的比较与 Java 语言数值比较
     * 运算符（{@code <, <=, ==, >=, >}）应用于原始 {@code float} 值时的比较
     * 有两个不同之处：
     *
     * <ul><li>
     *          {@code Float.NaN} 在此方法中被视为等于自身且大于所有其他
     *          {@code float} 值
     *          （包括 {@code Float.POSITIVE_INFINITY}）。
     * <li>
     *          {@code 0.0f} 在此方法中被视为大于 {@code -0.0f}。
     * </ul>
     *
     * 这确保了此方法施加的 {@code Float} 对象的 <i>自然顺序</i> 与 <i>equals 一致</i>。
     *
     * @param   anotherFloat   要比较的 {@code Float}。
     * @return  如果 {@code anotherFloat} 数值上等于此 {@code Float}，则返回值为 {@code 0}；
     *          如果此 {@code Float} 数值上小于 {@code anotherFloat}，则返回值小于 {@code 0}；
     *          如果此 {@code Float} 数值上大于 {@code anotherFloat}，则返回值大于 {@code 0}。
     *
     * @since   1.2
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Float anotherFloat) {
        return Float.compare(value, anotherFloat.value);
    }

    /**
     * 比较两个指定的 {@code float} 值。返回的整数值的符号与以下调用返回的整数的符号相同：
     * <pre>
     *    new Float(f1).compareTo(new Float(f2))
     * </pre>
     *
     * @param   f1        第一个要比较的 {@code float}。
     * @param   f2        第二个要比较的 {@code float}。
     * @return  如果 {@code f1} 数值上等于 {@code f2}，则返回值为 {@code 0}；
     *          如果 {@code f1} 数值上小于 {@code f2}，则返回值小于 {@code 0}；
     *          如果 {@code f1} 数值上大于 {@code f2}，则返回值大于 {@code 0}。
     * @since 1.4
     */
    public static int compare(float f1, float f2) {
        if (f1 < f2)
            return -1;           // 两个值都不是 NaN，此值较小
        if (f1 > f2)
            return 1;            // 两个值都不是 NaN，此值较大

        // 不能使用 floatToRawIntBits，因为可能存在 NaN。
        int thisBits    = Float.floatToIntBits(f1);
        int anotherBits = Float.floatToIntBits(f2);

        return (thisBits == anotherBits ?  0 : // 值相等
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) 或 (!NaN, NaN)
                 1));                          // (0.0, -0.0) 或 (NaN, !NaN)
    }

    /**
     * 按照 + 运算符将两个 {@code float} 值相加。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 的和
     * @jls 4.2.4 浮点运算
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static float sum(float a, float b) {
        return a + b;
    }

    /**
     * 返回两个 {@code float} 值中较大的一个，如同调用 {@link Math#max(float, float) Math.max}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 较大的 {@code a} 或 {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code float} 值中较小的一个，如同调用 {@link Math#min(float, float) Math.min}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 较小的 {@code a} 或 {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -2671257302660747028L;
}
