
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
import sun.misc.FpUtils;
import sun.misc.DoubleConsts;

/**
 * {@code Double} 类将原始类型 {@code double} 的值包装在一个对象中。一个 {@code Double} 类型的对象包含一个类型为 {@code double} 的单个字段。
 *
 * <p>此外，此类提供了将 {@code double} 转换为 {@code String} 和将 {@code String} 转换为 {@code double} 的几种方法，以及其他在处理 {@code double} 时有用的常量和方法。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Double extends Number implements Comparable<Double> {
    /**
     * 一个常量，表示类型为 {@code double} 的正无穷大。它等于 {@code Double.longBitsToDouble(0x7ff0000000000000L)} 返回的值。
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * 一个常量，表示类型为 {@code double} 的负无穷大。它等于 {@code Double.longBitsToDouble(0xfff0000000000000L)} 返回的值。
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * 一个常量，表示类型为 {@code double} 的非数字 (NaN) 值。它等于 {@code Double.longBitsToDouble(0x7ff8000000000000L)} 返回的值。
     */
    public static final double NaN = 0.0d / 0.0;

    /**
     * 一个常量，表示类型为 {@code double} 的最大正有限值，(2-2<sup>-52</sup>)&middot;2<sup>1023</sup>。它等于十六进制浮点字面量
     * {@code 0x1.fffffffffffffP+1023}，也等于 {@code Double.longBitsToDouble(0x7fefffffffffffffL)}。
     */
    public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308

    /**
     * 一个常量，表示类型为 {@code double} 的最小正正规值，2<sup>-1022</sup>。它等于十六进制浮点字面量 {@code 0x1.0p-1022}，也等于
     * {@code Double.longBitsToDouble(0x0010000000000000L)}。
     *
     * @since 1.6
     */
    public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308

    /**
     * 一个常量，表示类型为 {@code double} 的最小正非零值，2<sup>-1074</sup>。它等于十六进制浮点字面量
     * {@code 0x0.0000000000001P-1022}，也等于 {@code Double.longBitsToDouble(0x1L)}。
     */
    public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324

    /**
     * 一个常量，表示有限 {@code double} 变量可能具有的最大指数。它等于 {@code Math.getExponent(Double.MAX_VALUE)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 1023;

    /**
     * 一个常量，表示正规 {@code double} 变量可能具有的最小指数。它等于 {@code Math.getExponent(Double.MIN_NORMAL)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -1022;

    /**
     * 一个常量，表示表示 {@code double} 值所需的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * 一个常量，表示表示 {@code double} 值所需的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 一个常量，表示原始类型 {@code double} 的 {@code Class} 实例。
     *
     * @since JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double>   TYPE = (Class<Double>) Class.getPrimitiveClass("double");

    /**
     * 返回 {@code double} 参数的字符串表示形式。所有提到的字符都是 ASCII 字符。
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数的符号和大小（绝对值）的字符串。如果符号为负，则结果的第一个字符是 '{@code -}'
     * ({@code '\u005Cu002D'})；如果符号为正，则结果中不出现符号字符。对于大小 <i>m</i>：
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符 {@code "Infinity"} 表示；因此，正无穷大产生结果
     * {@code "Infinity"}，负无穷大产生结果 {@code "-Infinity"}。
     *
     * <li>如果 <i>m</i> 是零，它由字符 {@code "0.0"} 表示；因此，负零产生结果
     * {@code "-0.0"}，正零产生结果 {@code "0.0"}。
     *
     * <li>如果 <i>m</i> 大于或等于 10<sup>-3</sup> 但小于 10<sup>7</sup>，则它表示为 <i>m</i> 的整数部分
     * （十进制形式，没有前导零），后跟 '{@code .}' ({@code '\u005Cu002E'})，再后跟一个或多个表示 <i>m</i> 小数部分的十进制数字。
     *
     * <li>如果 <i>m</i> 小于 10<sup>-3</sup> 或大于或等于 10<sup>7</sup>，则它以所谓的“计算机科学记数法”表示。设 <i>n</i> 是唯一满足
     * 10<sup><i>n</i></sup> &le; <i>m</i> {@literal <} 10<sup><i>n</i>+1</sup> 的整数；设 <i>a</i> 是 <i>m</i> 和
     * 10<sup><i>n</i></sup> 的数学精确商，使得 1 &le; <i>a</i> {@literal <} 10。大小则表示为 <i>a</i> 的整数部分，
     * 作为单个十进制数字，后跟 '{@code .}' ({@code '\u005Cu002E'})，再后跟表示 <i>a</i> 小数部分的十进制数字，再后跟字母 '{@code E}'
     * ({@code '\u005Cu0045'})，再后跟 <i>n</i> 的十进制表示，由方法 {@link Integer#toString(int)} 生成。
     * </ul>
     * </ul>
     * 必须打印多少位以表示 <i>m</i> 或 <i>a</i> 的小数部分？必须至少有一位表示小数部分，除此之外，还需要尽可能多但仅尽可能多的位数，以唯一区分参数值与类型
     * {@code double} 的相邻值。也就是说，设 <i>x</i> 是由该方法为有限非零参数 <i>d</i> 生成的十进制表示所表示的确切数学值。则 <i>d</i> 必须是
     * 最接近 <i>x</i> 的 {@code double} 值；或者如果两个 {@code double} 值都同样接近 <i>x</i>，则 <i>d</i> 必须是其中之一，并且 <i>d</i> 的尾数的最低有效位必须为
     * {@code 0}。
     *
     * <p>要创建浮点值的本地化字符串表示形式，请使用 {@link java.text.NumberFormat} 的子类。
     *
     * @param   d   要转换的 {@code double}。
     * @return 参数的字符串表示形式。
     */
    public static String toString(double d) {
        return FloatingDecimal.toJavaFormatString(d);
    }

    /**
     * 返回 {@code double} 参数的十六进制字符串表示形式。所有提到的字符都是 ASCII 字符。
     *
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数的符号和大小的字符串。如果符号为负，则结果的第一个字符是 '{@code -}'
     * ({@code '\u005Cu002D'})；如果符号为正，则结果中不出现符号字符。对于大小 <i>m</i>：
     *
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符串 {@code "Infinity"} 表示；因此，正无穷大产生结果
     * {@code "Infinity"}，负无穷大产生结果 {@code "-Infinity"}。
     *
     * <li>如果 <i>m</i> 是零，它由字符串 {@code "0x0.0p0"} 表示；因此，负零产生结果
     * {@code "-0x0.0p0"}，正零产生结果 {@code "0x0.0p0"}。
     *
     * <li>如果 <i>m</i> 是一个具有正规表示形式的 {@code double} 值，使用子字符串表示尾数和指数字段。尾数由字符 {@code "0x1."}
     * 后跟小写的十六进制表示形式的其余尾数部分表示。移除尾部的零，除非所有数字都是零，此时使用单个零。接下来，指数由 {@code "p"} 后跟
     * 无偏指数的十进制字符串表示，该字符串由对指数值的调用 {@link Integer#toString(int) Integer.toString} 生成。
     *
     * <li>如果 <i>m</i> 是一个具有次正规表示形式的 {@code double} 值，尾数由字符 {@code "0x0."} 后跟
     * 十六进制表示形式的其余尾数部分表示。移除尾部的零。接下来，指数由 {@code "p-1022"} 表示。注意，次正规尾数中必须至少有一个非零数字。
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
     * <tr><td>{@code Double.MAX_VALUE}</td>
     *     <td>{@code 0x1.fffffffffffffp1023}</td>
     * <tr><td>{@code 最小正规值}</td>
     *     <td>{@code 0x1.0p-1022}</td>
     * <tr><td>{@code 最大次正规值}</td>
     *     <td>{@code 0x0.fffffffffffffp-1022}</td>
     * <tr><td>{@code Double.MIN_VALUE}</td>
     *     <td>{@code 0x0.0000000000001p-1022}</td>
     * </table>
     * @param   d   要转换的 {@code double}。
     * @return 参数的十六进制字符串表示形式。
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public static String toHexString(double d) {
        /*
         * 模仿 C99 第 7.19.6.1 节中的 "a" 转换说明符；但是，此方法的输出更为严格。
         */
        if (!isFinite(d) )
            // 对于无穷大和 NaN，使用十进制输出。
            return Double.toString(d);
        else {
            // 初始化为输出的最大长度。
            StringBuilder answer = new StringBuilder(24);

            if (Math.copySign(1.0, d) == -1.0)    // 值为负，
                answer.append("-");                  // 所以追加符号信息

            answer.append("0x");

            d = Math.abs(d);

            if(d == 0.0) {
                answer.append("0.0p0");
            } else {
                boolean subnormal = (d < DoubleConsts.MIN_NORMAL);

                // 隔离尾数位并 OR 一个高阶位
                // 使得字符串表示具有已知长度。
                long signifBits = (Double.doubleToLongBits(d)
                                   & DoubleConsts.SIGNIF_BIT_MASK) |
                    0x1000000000000000L;

                // 次正规值有一个 0 隐含位；正规值有一个 1 隐含位。
                answer.append(subnormal ? "0." : "1.");

                // 隔离十六进制表示的低 13 位。如果所有数字都是零，
                // 用单个 0 替换；否则，移除所有尾部零。
                String signif = Long.toHexString(signifBits).substring(3,16);
                answer.append(signif.equals("0000000000000") ? // 13 个零
                              "0":
                              signif.replaceFirst("0{1,12}$", ""));

                answer.append('p');
                // 如果值为次正规，使用双精度的 E_min 指数值；
                // 否则，提取并报告 d 的指数（次正规值的表示使用 E_min -1）。
                answer.append(subnormal ?
                              DoubleConsts.MIN_EXPONENT:
                              Math.getExponent(d));
            }
            return answer.toString();
        }
    }

    /**
     * 返回一个包含由字符串参数 {@code s} 表示的 {@code double} 值的 {@code Double} 对象。
     *
     * <p>如果 {@code s} 为 {@code null}，则抛出 {@code NullPointerException}。
     *
     * <p>忽略 {@code s} 中的前导和尾随空白字符。空白字符的处理方式与 {@link
     * String#trim} 方法相同；即，同时移除 ASCII 空格和控制字符。其余部分的 {@code s} 应构成一个 <i>FloatValue</i>，如以下词法规则所述：
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
     * <cite>The Java&trade; Language Specification</cite> 的词法规则部分，
     * 但不接受数字之间的下划线。如果 {@code s} 不具有
     * <i>FloatValue</i> 的形式，则抛出 {@code NumberFormatException}。否则，将 {@code s} 视为
     * 通常的“计算机科学记数法”或精确的十六进制值；此精确数值概念上转换为“无限精确”的二进制值，然后通过 IEEE 754 浮点运算的通常就近舍入规则
     * 转换为 {@code double} 类型，包括保留零值的符号。
     *
     * 注意，就近舍入规则还意味着溢出和下溢行为；如果 {@code s} 的精确值的大小足够大（大于或等于 ({@link
     * #MAX_VALUE} + {@link Math#ulp(double) ulp(MAX_VALUE)}/2)，
     * 舍入到 {@code double} 将导致无穷大；如果 {@code s} 的精确值的大小足够小（小于或等于 {@link #MIN_VALUE}/2），舍入到双精度将导致零。
     *
     * 最后，舍入后返回一个表示此 {@code double} 值的 {@code Double} 对象。
     *
     * <p>要解释浮点值的本地化字符串表示形式，请使用 {@link
     * java.text.NumberFormat} 的子类。
     *
     * <p>注意，尾随格式说明符，确定浮点字面量类型的说明符
     * ({@code 1.0f} 是一个 {@code float} 值；
     * {@code 1.0d} 是一个 {@code double} 值)，不会影响此方法的结果。换句话说，输入字符串直接转换为目标浮点类型。从字符串到
     * {@code float} 的两步转换序列，然后从 {@code float} 到 {@code double} 的转换，与直接从字符串转换到
     * {@code double} 不等价。例如，{@code float}
     * 字面量 {@code 0.1f} 等于 {@code double}
     * 值 {@code 0.10000000149011612}；{@code float}
     * 字面量 {@code 0.1f} 表示的数值与 {@code double} 字面量
     * {@code 0.1} 表示的数值不同。（数值 0.1 不能在二进制浮点数中精确表示。）
     *
     * <p>为了避免在无效字符串上调用此方法并抛出 {@code NumberFormatException}，可以使用以下正则表达式来筛选输入字符串：
     *
     * <pre>{@code
     *  final String Digits     = "(\\p{Digit}+)";
     *  final String HexDigits  = "(\\p{XDigit}+)";
     *  // 指数是 'e' 或 'E' 后跟一个可选的带符号十进制整数。
     *  final String Exp        = "[eE][+-]?"+Digits;
     *  final String fpRegex    =
     *      ("[\\x00-\\x20]*"+  // 可选的前导“空白”
     *       "[+-]?(" + // 可选的符号字符
     *       "NaN|" +           // "NaN" 字符串
     *       "Infinity|" +      // "Infinity" 字符串
     *
     *       // 一个表示有限正数的十进制浮点字符串，没有前导符号，最多有五个基本部分：
     *       // Digits . Digits ExponentPart FloatTypeSuffix
     *       //
     *       // 由于此方法允许整数字符串作为输入
     *       // 除了浮点字面量字符串，以下两个子模式是
     *       // 《Java 语言规范》第 3.10.2 节中语法生成的简化。
     *
     *       // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
     *       "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+
     *
     *       // . Digits ExponentPart_opt FloatTypeSuffix_opt
     *       "(\\.("+Digits+")("+Exp+")?)|"+
     *
     *       // 十六进制字符串
     *       "((" +
     *        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
     *        "(0[xX]" + HexDigits + "(\\.)?)|" +
     *
     *        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
     *        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
     *
     *        ")[pP][+-]?" + Digits + "))" +
     *       "[fFdD]?))" +
     *       "[\\x00-\\x20]*");// 可选的尾随“空白”
     *
     *  if (Pattern.matches(fpRegex, myString))
     *      Double.valueOf(myString); // 不会抛出 NumberFormatException
     *  else {
     *      // 执行合适的替代操作
     *  }
     * }</pre>
     *
     * @param      s   要解析的字符串。
     * @return     一个包含由 {@code String} 参数表示的值的 {@code Double} 对象。
     * @throws     NumberFormatException  如果字符串不包含可解析的数字。
     */
    public static Double valueOf(String s) throws NumberFormatException {
        return new Double(parseDouble(s));
    }


                /**
     * 返回一个表示指定 {@code double} 值的 {@code Double} 实例。
     * 如果不需要新的 {@code Double} 实例，通常应优先使用此方法，而不是构造函数
     * {@link #Double(double)}，因为此方法通过缓存频繁请求的值，可能会显著提高空间和时间性能。
     *
     * @param  d 一个 double 值。
     * @return 一个表示 {@code d} 的 {@code Double} 实例。
     * @since  1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * 返回一个初始化为指定 {@code String} 表示的值的新 {@code double}。
     * 该字符串的解析方式与 {@code Double} 类的 {@code valueOf} 方法相同。
     *
     * @param  s   要解析的字符串。
     * @return 由字符串参数表示的 {@code double} 值。
     * @throws NullPointerException  如果字符串为 null
     * @throws NumberFormatException 如果字符串不包含可解析的 {@code double}。
     * @see    java.lang.Double#valueOf(String)
     * @since 1.2
     */
    public static double parseDouble(String s) throws NumberFormatException {
        return FloatingDecimal.parseDouble(s);
    }

    /**
     * 如果指定的数字是 Not-a-Number (NaN) 值，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   v   要测试的值。
     * @return  如果参数的值是 NaN，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isNaN(double v) {
        return (v != v);
    }

    /**
     * 如果指定的数字在数值上是无限的，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   v   要测试的值。
     * @return  如果参数的值是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * 如果参数是一个有限的浮点值，则返回 {@code true}；否则返回 {@code false}（对于 NaN 和无穷大参数）。
     *
     * @param d 要测试的 {@code double} 值
     * @return 如果参数是一个有限的浮点值，则返回 {@code true}，否则返回 {@code false}。
     * @since 1.8
     */
    public static boolean isFinite(double d) {
        return Math.abs(d) <= DoubleConsts.MAX_VALUE;
    }

    /**
     * 该 Double 的值。
     *
     * @serial
     */
    private final double value;

    /**
     * 构造一个新的分配的 {@code Double} 对象，表示原始的 {@code double} 参数。
     *
     * @param   value   要表示的 {@code Double}。
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * 构造一个新的分配的 {@code Double} 对象，表示字符串表示的浮点值。
     * 该字符串转换为 {@code double} 值的方式与 {@code valueOf} 方法相同。
     *
     * @param  s  要转换的字符串。
     * @throws    NumberFormatException  如果字符串不包含可解析的数字。
     * @see       java.lang.Double#valueOf(java.lang.String)
     */
    public Double(String s) throws NumberFormatException {
        value = parseDouble(s);
    }

    /**
     * 如果此 {@code Double} 值是 Not-a-Number (NaN)，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return  如果此对象表示的值是 NaN，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * 如果此 {@code Double} 值在数值上是无限的，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return  如果此对象表示的值是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * 返回此 {@code Double} 对象的字符串表示形式。
     * 由该对象表示的原始 {@code double} 值转换为字符串的方式与带有一个参数的 {@code toString} 方法相同。
     *
     * @return  此对象的字符串表示形式。
     * @see java.lang.Double#toString(double)
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 返回此 {@code Double} 值经过缩小原始转换后的 {@code byte} 值。
     *
     * @return  由该对象表示的 {@code double} 值转换为类型 {@code byte}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     * @since JDK1.1
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此 {@code Double} 值经过缩小原始转换后的 {@code short} 值。
     *
     * @return  由该对象表示的 {@code double} 值转换为类型 {@code short}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     * @since JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Double} 值经过缩小原始转换后的 {@code int} 值。
     * @jls 5.1.3 Narrowing Primitive Conversions
     *
     * @return  由该对象表示的 {@code double} 值转换为类型 {@code int}。
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回此 {@code Double} 值经过缩小原始转换后的 {@code long} 值。
     *
     * @return  由该对象表示的 {@code double} 值转换为类型 {@code long}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回此 {@code Double} 值经过缩小原始转换后的 {@code float} 值。
     *
     * @return  由该对象表示的 {@code double} 值转换为类型 {@code float}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     * @since JDK1.0
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Double} 对象的 {@code double} 值。
     *
     * @return  由该对象表示的 {@code double} 值。
     */
    public double doubleValue() {
        return value;
    }

    /**
     * 返回此 {@code Double} 对象的哈希码。结果是 {@code long} 整数位表示的两个部分的异或结果，该表示方式与
     * {@link #doubleToLongBits(double)} 方法产生的结果完全相同，表示由该 {@code Double} 对象表示的原始 {@code double} 值。
     * 即，哈希码是以下表达式的值：
     *
     * <blockquote>
     *  {@code (int)(v^(v>>>32))}
     * </blockquote>
     *
     * 其中 {@code v} 由以下表达式定义：
     *
     * <blockquote>
     *  {@code long v = Double.doubleToLongBits(this.doubleValue());}
     * </blockquote>
     *
     * @return  该对象的哈希码。
     */
    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    /**
     * 返回一个 {@code double} 值的哈希码；与 {@code Double.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return  一个 {@code double} 值的哈希码。
     * @since 1.8
     */
    public static int hashCode(double value) {
        long bits = doubleToLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }

    /**
     * 将此对象与指定对象进行比较。结果为 {@code true} 当且仅当参数不为
     * {@code null} 并且是一个表示与该对象表示的 {@code double} 值相同的 {@code Double} 对象。
     * 为此，两个 {@code double} 值被认为是相同的，当且仅当对每个值应用 {@link
     * #doubleToLongBits(double)} 方法时返回相同的 {@code long} 值。
     *
     * <p>注意，在大多数情况下，对于两个 {@code Double} 类的实例
     * {@code d1} 和 {@code d2}，表达式
     *
     * <blockquote>
     *  {@code d1.equals(d2)}
     * </blockquote>
     *
     * <p>的值为 {@code true} 当且仅当
     *
     * <blockquote>
     *  {@code d1.doubleValue() == d2.doubleValue()}
     * </blockquote>
     *
     * <p>的值也为 {@code true}。但是，有两个例外：
     * <ul>
     * <li>如果 {@code d1} 和 {@code d2} 都表示
     *     {@code Double.NaN}，则 {@code equals} 方法
     *     返回 {@code true}，即使
     *     {@code Double.NaN==Double.NaN} 的值为
     *     {@code false}。
     * <li>如果 {@code d1} 表示 {@code +0.0} 而
     *     {@code d2} 表示 {@code -0.0}，反之亦然，
     *     则 {@code equal} 测试的值为 {@code false}，
     *     即使 {@code +0.0==-0.0} 的值为 {@code true}。
     * </ul>
     * 此定义允许哈希表正常工作。
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see java.lang.Double#doubleToLongBits(double)
     */
    public boolean equals(Object obj) {
        return (obj instanceof Double)
               && (doubleToLongBits(((Double)obj).value) ==
                      doubleToLongBits(value));
    }

    /**
     * 根据 IEEE 754 浮点数 "double
     * 格式" 位布局返回指定浮点值的表示。
     *
     * <p>位 63（由掩码
     * {@code 0x8000000000000000L} 选择的位）表示浮点数的符号。位
     * 62-52（由掩码
     * {@code 0x7ff0000000000000L} 选择的位）表示指数。位 51-0
     * （由掩码
     * {@code 0x000fffffffffffffL} 选择的位）表示浮点数的尾数
     * （有时称为尾数）。
     *
     * <p>如果参数是正无穷大，结果是
     * {@code 0x7ff0000000000000L}。
     *
     * <p>如果参数是负无穷大，结果是
     * {@code 0xfff0000000000000L}。
     *
     * <p>如果参数是 NaN，结果是
     * {@code 0x7ff8000000000000L}。
     *
     * <p>在所有情况下，结果是一个 {@code long} 整数，当将其传递给
     * {@link #longBitsToDouble(long)} 方法时，将产生一个与
     * {@code doubleToLongBits} 的参数相同的浮点值（所有 NaN 值都折叠为一个“标准”NaN 值）。
     *
     * @param   value   一个 {@code double} 精度的浮点数。
     * @return  表示浮点数的位。
     */
    public static long doubleToLongBits(double value) {
        long result = doubleToRawLongBits(value);
        // 根据位字段值、最大指数和非零尾数检查 NaN。
        if ( ((result & DoubleConsts.EXP_BIT_MASK) ==
              DoubleConsts.EXP_BIT_MASK) &&
             (result & DoubleConsts.SIGNIF_BIT_MASK) != 0L)
            result = 0x7ff8000000000000L;
        return result;
    }

    /**
     * 根据 IEEE 754 浮点数 "double
     * 格式" 位布局返回指定浮点值的表示，保留 Not-a-Number (NaN) 值。
     *
     * <p>位 63（由掩码
     * {@code 0x8000000000000000L} 选择的位）表示浮点数的符号。位
     * 62-52（由掩码
     * {@code 0x7ff0000000000000L} 选择的位）表示指数。位 51-0
     * （由掩码
     * {@code 0x000fffffffffffffL} 选择的位）表示浮点数的尾数
     * （有时称为尾数）。
     *
     * <p>如果参数是正无穷大，结果是
     * {@code 0x7ff0000000000000L}。
     *
     * <p>如果参数是负无穷大，结果是
     * {@code 0xfff0000000000000L}。
     *
     * <p>如果参数在范围
     * {@code 0x7ff0000000000001L} 到
     * {@code 0x7fffffffffffffffL} 或
     * {@code 0xfff0000000000001L} 到
     * {@code 0xffffffffffffffffL} 之间，结果是一个 NaN。Java 提供的 IEEE
     * 754 浮点运算无法区分相同类型的两个 NaN 值的不同位模式。只有使用
     * {@code Double.doubleToRawLongBits} 方法才能区分不同的 NaN 值。
     *
     * <p>在所有其他情况下，可以从参数计算出三个值 <i>s</i>、<i>e</i> 和 <i>m</i>：
     *
     * <blockquote><pre>{@code
     * int s = ((bits >> 63) == 0) ? 1 : -1;
     * int e = (int)((bits >> 52) & 0x7ffL);
     * long m = (e == 0) ?
     *                 (bits & 0xfffffffffffffL) << 1 :
     *                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
     * }</pre></blockquote>
     *
     * 那么浮点结果等于数学表达式 <i>s</i>&middot;<i>m</i>&middot;2<sup><i>e</i>-1075</sup> 的值。
     *
     * <p>注意，此方法可能无法返回一个具有与
     * {@code long} 参数完全相同位模式的 {@code double} NaN。IEEE 754 区分两种 NaN，安静 NaN 和
     * <i>信号 NaN</i>。这两种 NaN 之间的差异在 Java 中通常不可见。对信号 NaN 的算术运算会将其转换为具有不同但通常相似位模式的安静 NaN。然而，在某些处理器上，仅仅复制一个信号 NaN 也会执行这种转换。特别是，将信号 NaN 复制以返回给调用方法可能会执行这种转换。因此，对于某些
     * {@code long} 值，
     * {@code doubleToRawLongBits(longBitsToDouble(start))} 可能
     * <i>不</i> 等于 {@code start}。此外，表示信号 NaN 的特定位模式是平台相关的；尽管所有 NaN 位模式，安静或信号，
     * 必须在上述 NaN 范围内。
     *
     * @param   bits   任何 {@code long} 整数。
     * @return  具有相同位模式的 {@code double} 浮点值。
     */
    public static native double longBitsToDouble(long bits);


                /**
     * 比较两个 {@code Double} 对象的数值。此方法执行的比较与使用 Java 语言数值比较运算符
     * ({@code <, <=, ==, >=, >}) 对原始 {@code double} 值进行的比较有两个不同之处：
     * <ul><li>
     *          {@code Double.NaN} 被认为等于自身且大于所有其他
     *          {@code double} 值（包括
     *          {@code Double.POSITIVE_INFINITY}）。
     * <li>
     *          {@code 0.0d} 被认为大于 {@code -0.0d}。
     * </ul>
     * 这确保了此方法施加的 {@code Double} 对象的 <i>自然顺序</i> 与 <i>equals 一致</i>。
     *
     * @param   anotherDouble   要比较的 {@code Double}。
     * @return  如果 {@code anotherDouble} 数值上等于此 {@code Double}，则返回值为 {@code 0}；
     *          如果此 {@code Double} 数值上小于 {@code anotherDouble}，则返回值小于 {@code 0}；
     *          如果此 {@code Double} 数值上大于 {@code anotherDouble}，则返回值大于 {@code 0}。
     *
     * @since   1.2
     */
    public int compareTo(Double anotherDouble) {
        return Double.compare(value, anotherDouble.value);
    }

    /**
     * 比较指定的两个 {@code double} 值。返回的整数值的符号与调用以下方法返回的整数的符号相同：
     * <pre>
     *    new Double(d1).compareTo(new Double(d2))
     * </pre>
     *
     * @param   d1        第一个要比较的 {@code double}
     * @param   d2        第二个要比较的 {@code double}
     * @return  如果 {@code d1} 数值上等于 {@code d2}，则返回值为 {@code 0}；
     *          如果 {@code d1} 数值上小于 {@code d2}，则返回值小于 {@code 0}；
     *          如果 {@code d1} 数值上大于 {@code d2}，则返回值大于 {@code 0}。
     * @since 1.4
     */
    public static int compare(double d1, double d2) {
        if (d1 < d2)
            return -1;           // 两个值都不是 NaN，此值较小
        if (d1 > d2)
            return 1;            // 两个值都不是 NaN，此值较大

        // 不能使用 doubleToRawLongBits，因为可能会有 NaN。
        long thisBits    = Double.doubleToLongBits(d1);
        long anotherBits = Double.doubleToLongBits(d2);

        return (thisBits == anotherBits ?  0 : // 值相等
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) 或 (!NaN, NaN)
                 1));                          // (0.0, -0.0) 或 (NaN, !NaN)
    }

    /**
     * 按照 + 运算符将两个 {@code double} 值相加。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 的和
     * @jls 4.2.4 浮点运算
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static double sum(double a, double b) {
        return a + b;
    }

    /**
     * 返回两个 {@code double} 值中较大的一个，如同调用了 {@link Math#max(double, double) Math.max}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 较大的 {@code a} 和 {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code double} 值中较小的一个，如同调用了 {@link Math#min(double, double) Math.min}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 较小的 {@code a} 和 {@code b}。
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -9172774392245257468L;
}
