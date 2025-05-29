
/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import sun.misc.FloatingDecimal;
import sun.misc.FloatConsts;
import sun.misc.DoubleConsts;

/**
 * {@code Float} 类将基本类型 {@code float} 的值包装在一个对象中。一个类型为
 * {@code Float} 的对象包含一个类型为 {@code float} 的单个字段。
 *
 * <p>此外，该类提供了将 {@code float} 转换为 {@code String} 和将
 * {@code String} 转换为 {@code float} 的几种方法，以及其他在处理
 * {@code float} 时有用的常量和方法。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Float extends Number implements Comparable<Float> {
    /**
     * 一个常量，表示类型为 {@code float} 的正无穷大。它等于
     * {@code Float.intBitsToFloat(0x7f800000)} 返回的值。
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的负无穷大。它等于
     * {@code Float.intBitsToFloat(0xff800000)} 返回的值。
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的非数字 (NaN) 值。它等同于
     * {@code Float.intBitsToFloat(0x7fc00000)} 返回的值。
     */
    public static final float NaN = 0.0f / 0.0f;

    /**
     * 一个常量，表示类型为 {@code float} 的最大正有限值，(2-2<sup>-23</sup>)&middot;2<sup>127</sup>。
     * 它等于十六进制浮点文字 {@code 0x1.fffffeP+127f}，也等于
     * {@code Float.intBitsToFloat(0x7f7fffff)}。
     */
    public static final float MAX_VALUE = 0x1.fffffeP+127f; // 3.4028235e+38f

    /**
     * 一个常量，表示类型为 {@code float} 的最小正正规值，2<sup>-126</sup>。它等于
     * 十六进制浮点文字 {@code 0x1.0p-126f}，也等于
     * {@code Float.intBitsToFloat(0x00800000)}。
     *
     * @since 1.6
     */
    public static final float MIN_NORMAL = 0x1.0p-126f; // 1.17549435E-38f

    /**
     * 一个常量，表示类型为 {@code float} 的最小正非零值，2<sup>-149</sup>。它等于
     * 十六进制浮点文字 {@code 0x0.000002P-126f}，也等于
     * {@code Float.intBitsToFloat(0x1)}。
     */
    public static final float MIN_VALUE = 0x0.000002P-126f; // 1.4e-45f

    /**
     * 一个有限的 {@code float} 变量可能具有的最大指数。它等于
     * {@code Math.getExponent(Float.MAX_VALUE)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 127;

    /**
     * 一个正规的 {@code float} 变量可能具有的最小指数。它等于
     * {@code Math.getExponent(Float.MIN_NORMAL)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -126;

    /**
     * 用于表示 {@code float} 值的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 32;

    /**
     * 用于表示 {@code float} 值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 表示基本类型 {@code float} 的 {@code Class} 实例。
     *
     * @since JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE = (Class<Float>) Class.getPrimitiveClass("float");

    /**
     * 返回 {@code float} 参数的字符串表示形式。以下提到的所有字符都是 ASCII 字符。
     * <ul>
     * <li>如果参数是 NaN，结果是字符串
     * "{@code NaN}"。
     * <li>否则，结果是一个表示参数的符号和
     *     幅度（绝对值）的字符串。如果符号是
     *     负数，结果的第一个字符是
     *     '{@code -}' ({@code '\u005Cu002D'}); 如果符号是
     *     正数，结果中不会出现符号字符。至于
     *     幅度 <i>m</i>：
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符
     *     {@code "Infinity"} 表示；因此，正无穷大产生
     *     结果 {@code "Infinity"}，负无穷大
     *     产生结果 {@code "-Infinity"}。
     * <li>如果 <i>m</i> 是零，它由字符
     *     {@code "0.0"} 表示；因此，负零产生结果
     *     {@code "-0.0"}，正零产生结果
     *     {@code "0.0"}。
     * <li> 如果 <i>m</i> 大于或等于 10<sup>-3</sup> 但
     *      小于 10<sup>7</sup>，则它表示为 <i>m</i> 的整数部分，
     *      以十进制形式表示，没有前导零，后跟 '{@code .}'
     *      ({@code '\u005Cu002E'})，后跟一个或多个
     *      表示 <i>m</i> 小数部分的十进制数字。
     * <li> 如果 <i>m</i> 小于 10<sup>-3</sup> 或大于或
     *      等于 10<sup>7</sup>，则它以所谓的“计算机科学记数法”表示。设 <i>n</i>
     *      是唯一的整数，使得 10<sup><i>n</i> </sup>&le;
     *      <i>m</i> {@literal <} 10<sup><i>n</i>+1</sup>；则设 <i>a</i>
     *      是 <i>m</i> 和 10<sup><i>n</i></sup> 的数学精确商，使得 1 &le; <i>a</i> {@literal <} 10。
     *      幅度则表示为 <i>a</i> 的整数部分，作为一个十进制数字，后跟
     *      '{@code .}' ({@code '\u005Cu002E'})，后跟
     *      表示 <i>a</i> 小数部分的十进制数字，后跟字母 '{@code E}'
     *      ({@code '\u005Cu0045'})，后跟
     *      由方法 {@link java.lang.Integer#toString(int)} 生成的 <i>n</i> 的十进制整数表示。
     *
     * </ul>
     * </ul>
     * 必须为 <i>m</i> 或 <i>a</i> 的小数部分打印多少位数字？必须至少有一位
     * 以表示小数部分，除此之外，还需要尽可能多的位数，但仅需足够的位数来唯一区分
     * 参数值与类型为 {@code float} 的相邻值。也就是说，假设 <i>x</i> 是
     * 由该方法为有限非零参数 <i>f</i> 生成的十进制表示形式所表示的精确数学值。那么 <i>f</i> 必须是
     * 最接近 <i>x</i> 的 {@code float} 值；或者，如果有两个 {@code float} 值
     * 同等接近 <i>x</i>，那么 <i>f</i> 必须是其中之一，并且 <i>f</i> 的尾数的最低有效位
     * 必须是 {@code 0}。
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
 * 返回 {@code float} 参数的十六进制字符串表示。以下提到的所有字符都是
 * ASCII 字符。
 *
 * <ul>
 * <li>如果参数是 NaN，结果是字符串
 *     "{@code NaN}"。
 * <li>否则，结果是一个表示参数的符号和
 *     幅度（绝对值）的字符串。如果符号为负，则结果的第一个字符是 '{@code -}'
 *     ({@code '\u005Cu002D'}); 如果符号为正，则结果中不会出现符号字符。对于幅度 <i>m</i>：
 *
 * <ul>
 * <li>如果 <i>m</i> 是无穷大，它由字符串
 * {@code "Infinity"} 表示；因此，正无穷大产生结果 {@code "Infinity"} 而负无穷大产生
 * 结果 {@code "-Infinity"}。
 *
 * <li>如果 <i>m</i> 是零，它由字符串
 * {@code "0x0.0p0"} 表示；因此，负零产生结果
 * {@code "-0x0.0p0"} 而正零产生结果
 * {@code "0x0.0p0"}。
 *
 * <li>如果 <i>m</i> 是具有
 * 规范化表示的 {@code float} 值，使用子字符串表示尾数和指数字段。尾数
 * 由字符 {@code "0x1."}
 * 后跟尾数其余部分的十六进制表示形式表示。尾数的十六进制表示形式中的尾随零被删除，除非所有数字
 * 都是零，此时使用单个零。接下来，指数由 {@code "p"} 后跟
 * 无偏指数的十进制字符串表示，就像调用 {@link Integer#toString(int) Integer.toString} 产生的结果一样。
 *
 * <li>如果 <i>m</i> 是具有次正规
 * 表示的 {@code float} 值，尾数由字符
 * {@code "0x0."} 后跟
 * 尾数其余部分的十六进制表示形式表示。尾数的十六进制表示形式中的尾随零被删除。接下来，指数由
 * {@code "p-126"} 表示。注意，次正规尾数中必须至少有一个非零数字。
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
 * <tr><td>{@code 最大次正规值}</td>
 *     <td>{@code 0x0.fffffep-126}</td>
 * <tr><td>{@code Float.MIN_VALUE}</td>
 *     <td>{@code 0x0.000002p-126}</td>
 * </table>
 * @param   f   要转换的 {@code float}。
 * @return 参数的十六进制字符串表示。
 * @since 1.5
 * @author Joseph D. Darcy
 */
public static String toHexString(float f) {
    if (Math.abs(f) < FloatConsts.MIN_NORMAL
        &&  f != 0.0f ) {// float 次正规
        // 调整指数以创建次正规 double，然后
        // 用次正规 float 指数替换次正规 double 指数
        String s = Double.toHexString(Math.scalb((double)f,
                                                 /* -1022+126 */
                                                 DoubleConsts.MIN_EXPONENT-
                                                 FloatConsts.MIN_EXPONENT));
        return s.replaceFirst("p-1022$", "p-126");
    }
    else // double 字符串将与 float 字符串相同
        return Double.toHexString(f);
}

/**
 * 返回一个持有由参数字符串
 * {@code s} 表示的 {@code float} 值的 {@code Float} 对象。
 *
 * <p>如果 {@code s} 是 {@code null}，则抛出
 * {@code NullPointerException}。
 *
 * <p>忽略 {@code s} 中的前导和尾随空白字符。空白字符的处理方式如同调用 {@link
 * String#trim} 方法；也就是说，同时删除 ASCII 空格和控制字符。{@code s} 的其余部分应
 * 构成一个 <i>FloatValue</i>，如以下词法规则所述：
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
 * 其中 <i>Sign</i>、<i>FloatingPointLiteral</i>、
 * <i>HexNumeral</i>、<i>HexDigits</i>、<i>SignedInteger</i> 和
 * <i>FloatTypeSuffix</i> 是按照
 * <cite>The Java&trade; Language Specification</cite> 的词法规则定义的，
 * 但不允许在数字之间使用下划线。
 * 如果 {@code s} 不具有
 * <i>FloatValue</i> 的形式，则抛出 {@code NumberFormatException}。否则，将 {@code s} 视为
 * 通常的“计算机科学记数法”中的精确十进制值或精确的十六进制值；这个精确的数值概念上被转换为一个“无限精确”
 * 的二进制值，然后按照 IEEE 754 浮点算术的常规就近舍入规则（包括保留零值的符号）舍入为 {@code float} 类型。
 *
 * 注意，就近舍入规则也意味着溢出和下溢行为；如果 {@code s} 的精确值的绝对值足够大
 * （大于或等于 ({@link
 * #MAX_VALUE} + {@link Math#ulp(float) ulp(MAX_VALUE)}/2)，
 * 舍入到 {@code float} 将导致无穷大；如果 {@code s} 的精确值的绝对值足够小
 * （小于或等于 {@link #MIN_VALUE}/2），舍入到 float 将导致零。
 *
 * 最后，舍入后返回一个表示此 {@code float} 值的 {@code Float} 对象。
 *
 * <p>要解释浮点值的本地化字符串表示，使用 {@link
 * java.text.NumberFormat} 的子类。
 *
 * <p>注意，尾随格式说明符，确定浮点字面量类型的说明符
 * （{@code 1.0f} 是一个 {@code float} 值；
 * {@code 1.0d} 是一个 {@code double} 值），<em>不</em>
 * 影响此方法的结果。换句话说，输入字符串的数值直接转换为目标浮点类型。通常，将字符串转换为
 * {@code double} 然后再转换为 {@code float} 的两步转换序列
 * <em>不等于</em> 直接将字符串转换为 {@code float}。例如，如果先转换为中间的
 * {@code double}，然后再转换为
 * {@code float}，字符串<br>
 * {@code "1.00000017881393421514957253748434595763683319091796875001d"}<br>
 * 将产生 {@code float} 值
 * {@code 1.0000002f}；如果字符串直接转换为
 * {@code float}，<code>1.000000<b>1</b>f</code> 将产生结果。
 *
 * <p>为了避免对无效字符串调用此方法并抛出
 * {@code NumberFormatException}，{@link Double#valueOf Double.valueOf} 的文档列出了一个正则表达式
 * 可用于筛选输入。
 *
 * @param   s   要解析的字符串。
 * @return  一个持有由 {@code String} 参数表示的值的
 *          {@code Float} 对象。
 * @throws  NumberFormatException  如果字符串不包含
 *          可解析的数字。
 */
public static Float valueOf(String s) throws NumberFormatException {
    return new Float(parseFloat(s));
}

                    /**
     * 返回一个表示指定 {@code float} 值的 {@code Float} 实例。
     * 如果不需要新的 {@code Float} 实例，通常应优先使用此方法，而不是构造函数
     * {@link #Float(float)}，因为此方法通过缓存经常请求的值，可能会显著提高空间和时间性能。
     *
     * @param  f 一个浮点值。
     * @return 一个表示 {@code f} 的 {@code Float} 实例。
     * @since  1.5
     */
    public static Float valueOf(float f) {
        return new Float(f);
    }

    /**
     * 返回一个初始化为指定 {@code String} 表示的值的新 {@code float}，该操作由
     * {@code Float} 类的 {@code valueOf} 方法执行。
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
     * @param   v  要测试的值。
     * @return  如果参数是 NaN，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isNaN(float v) {
        return (v != v);
    }

    /**
     * 如果指定的数字在数值上是无限大，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   v  要测试的值。
     * @return  如果参数是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean isInfinite(float v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }


    /**
     * 如果参数是一个有限的浮点值，则返回 {@code true}；否则返回 {@code false}（对于 NaN 和无穷大参数）。
     *
     * @param f 要测试的 {@code float} 值
     * @return 如果参数是一个有限的浮点值，则返回 {@code true}，否则返回 {@code false}。
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
     * 构造一个新的分配的 {@code Float} 对象，该对象表示原始的 {@code float} 参数。
     *
     * @param   value  要由 {@code Float} 表示的值。
     */
    public Float(float value) {
        this.value = value;
    }

    /**
     * 构造一个新的分配的 {@code Float} 对象，该对象表示转换为 {@code float} 类型的参数。
     *
     * @param   value  要由 {@code Float} 表示的值。
     */
    public Float(double value) {
        this.value = (float)value;
    }

    /**
     * 构造一个新的分配的 {@code Float} 对象，该对象表示由字符串表示的浮点值。字符串转换为
     * {@code float} 值，就像通过 {@code valueOf} 方法一样。
     *
     * @param      s  要转换为 {@code Float} 的字符串。
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
     * 如果此 {@code Float} 值在数值上是无限大，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return  如果此对象表示的值是正无穷大或负无穷大，则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * 返回此 {@code Float} 对象的字符串表示形式。此对象表示的原始 {@code float} 值
     * 转换为 {@code String}，就像通过一个参数的方法 {@code toString} 一样。
     *
     * @return  此对象的 {@code String} 表示形式。
     * @see java.lang.Float#toString(float)
     */
    public String toString() {
        return Float.toString(value);
    }

    /**
     * 返回此 {@code Float} 作为 {@code byte} 类型的值，经过缩小的原始转换。
     *
     * @return  由此对象表示的 {@code float} 值转换为 {@code byte} 类型。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此 {@code Float} 作为 {@code short} 类型的值，经过缩小的原始转换。
     *
     * @return  由此对象表示的 {@code float} 值转换为 {@code short} 类型。
     * @jls 5.1.3 Narrowing Primitive Conversions
     * @since JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Float} 作为 {@code int} 类型的值，经过缩小的原始转换。
     *
     * @return  由此对象表示的 {@code float} 值转换为 {@code int} 类型。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public int intValue() {
        return (int)value;
    }

                    /**
     * 返回此 {@code Float} 的值，经过缩小原始转换后转换为 {@code long} 类型。
     *
     * @return  由该对象表示的 {@code float} 值转换为 {@code long} 类型
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回此 {@code Float} 对象的 {@code float} 值。
     *
     * @return 由该对象表示的 {@code float} 值
     */
    public float floatValue() {
        return value;
    }

    /**
     * 返回此 {@code Float} 的值，经过扩展原始转换后转换为 {@code double} 类型。
     *
     * @return 由该对象表示的 {@code float} 值转换为 {@code double} 类型
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回此 {@code Float} 对象的哈希码。结果是该对象表示的原始 {@code float} 值的整数位表示，
     * 完全由 {@link #floatToIntBits(float)} 方法产生。
     *
     * @return 该对象的哈希码值。
     */
    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    /**
     * 返回 {@code float} 值的哈希码；与 {@code Float.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return {@code float} 值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(float value) {
        return floatToIntBits(value);
    }

    /**

     * 将此对象与指定的对象进行比较。结果为 {@code true} 当且仅当参数不是
     * {@code null} 并且是一个表示与该对象表示的 {@code float} 值相同的
     * {@code Float} 对象。为此，两个 {@code float} 值被认为是相同的当且仅当
     * 将 {@link #floatToIntBits(float)} 方法应用于每个值时返回相同的 {@code int} 值。
     *
     * <p>注意，对于 {@code Float} 类的两个实例，{@code f1} 和 {@code f2}，
     * {@code f1.equals(f2)} 的值为 {@code true} 当且仅当
     *
     * <blockquote><pre>
     *   f1.floatValue() == f2.floatValue()
     * </pre></blockquote>
     *
     * <p>也具有 {@code true} 的值。但是，有两个例外：
     * <ul>
     * <li>如果 {@code f1} 和 {@code f2} 都表示
     *     {@code Float.NaN}，则 {@code equals} 方法返回
     *     {@code true}，即使 {@code Float.NaN==Float.NaN}
     *     的值为 {@code false}。
     * <li>如果 {@code f1} 表示 {@code +0.0f} 而
     *     {@code f2} 表示 {@code -0.0f}，反之亦然，
     *     则 {@code equal} 测试的值为
     *     {@code false}，即使 {@code 0.0f==-0.0f}
     *     的值为 {@code true}。
     * </ul>
     *
     * 此定义允许哈希表正常工作。
     *
     * @param obj 要比较的对象
     * @return  如果对象相同则为 {@code true}；
     *          否则为 {@code false}。
     * @see java.lang.Float#floatToIntBits(float)
     */
    public boolean equals(Object obj) {
        return (obj instanceof Float)
               && (floatToIntBits(((Float)obj).value) == floatToIntBits(value));
    }

    /**
     * 返回根据 IEEE 754 浮点数 "单精度格式" 位布局表示的指定浮点值。
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
     * <p>在所有情况下，结果是一个整数，当将其传递给 {@link #intBitsToFloat(int)} 方法时，
     * 将生成与传递给 {@code floatToIntBits} 的参数相同的浮点值（所有 NaN 值都折叠为一个
     * "规范" 的 NaN 值）。
     *
     * @param   value   一个浮点数。
     * @return  表示浮点数的位。
     */
    public static int floatToIntBits(float value) {
        int result = floatToRawIntBits(value);
        // 检查 NaN，基于位字段的值，最大指数和非零尾数。
        if ( ((result & FloatConsts.EXP_BIT_MASK) ==
              FloatConsts.EXP_BIT_MASK) &&
             (result & FloatConsts.SIGNIF_BIT_MASK) != 0)
            result = 0x7fc00000;
        return result;
    }

    /**
     * 返回根据 IEEE 754 浮点数 "单精度格式" 位布局表示的指定浮点值，保留 Not-a-Number (NaN) 值。
     *
     * <p>位 31（由掩码 {@code 0x80000000} 选择的位）表示浮点数的符号。
     * 位 30-23（由掩码 {@code 0x7f800000} 选择的位）表示指数。
     * 位 22-0（由掩码 {@code 0x007fffff} 选择的位）表示浮点数的尾数（有时称为尾数）。
     *
     * <p>如果参数是正无穷大，结果是 {@code 0x7f800000}。
     *
     * <p>如果参数是负无穷大，结果是 {@code 0xff800000}。
     *
     * <p>如果参数是 NaN，结果是表示实际 NaN 值的整数。与 {@code floatToIntBits}
     * 方法不同，{@code floatToRawIntBits} 不会将所有编码 NaN 的位模式折叠为一个
     * "规范" 的 NaN 值。
     *
     * <p>在所有情况下，结果是一个整数，当将其传递给 {@link #intBitsToFloat(int)} 方法时，
     * 将生成与传递给 {@code floatToRawIntBits} 的参数相同的浮点值。
     *
     * @param   value   一个浮点数。
     * @return  表示浮点数的位。
     * @since 1.3
     */
    public static native int floatToRawIntBits(float value);

                    /**
     * 返回与给定位表示相对应的 {@code float} 值。
     * 参数被视为根据 IEEE 754 浮点数“单精度格式”位布局表示的浮点值。
     *
     * <p>如果参数是 {@code 0x7f800000}，结果是正无穷大。
     *
     * <p>如果参数是 {@code 0xff800000}，结果是负无穷大。
     *
     * <p>如果参数是范围 {@code 0x7f800001} 到 {@code 0x7fffffff} 或
     * 范围 {@code 0xff800001} 到 {@code 0xffffffff} 中的任何值，结果是一个 NaN。Java 提供的任何 IEEE 754
     * 浮点操作都无法区分相同类型但位模式不同的两个 NaN 值。只有通过使用 {@code Float.floatToRawIntBits} 方法
     * 才能区分不同值的 NaN。
     *
     * <p>在所有其他情况下，设 <i>s</i>、<i>e</i> 和 <i>m</i> 是可以从参数计算出的三个值：
     *
     * <blockquote><pre>{@code
     * int s = ((bits >> 31) == 0) ? 1 : -1;
     * int e = ((bits >> 23) & 0xff);
     * int m = (e == 0) ?
     *                 (bits & 0x7fffff) << 1 :
     *                 (bits & 0x7fffff) | 0x800000;
     * }</pre></blockquote>
     *
     * 那么浮点结果等于数学表达式 <i>s</i>&middot;<i>m</i>&middot;2<sup><i>e</i>-150</sup> 的值。
     *
     * <p>请注意，此方法可能无法返回与 {@code int} 参数具有完全相同位模式的 {@code float} NaN。IEEE 754 区分两种 NaN，
     * 即静默 NaN 和 <i>信号 NaN</i>。这两种 NaN 之间的差异在 Java 中通常不可见。对信号 NaN 的算术运算会将其转换为具有不同但通常类似的位模式的静默 NaN。
     * 然而，在某些处理器上，仅复制信号 NaN 也会执行此转换。特别是，将信号 NaN 复制以返回给调用方法可能会执行此转换。因此，对于某些 {@code int} 值，
     * {@code floatToRawIntBits(intBitsToFloat(start))} 可能 <i>不</i> 等于 {@code start}。此外，哪些特定的位模式表示信号 NaN 是平台相关的；
     * 尽管所有 NaN 位模式，无论是静默的还是信号的，都必须在上述识别的 NaN 范围内。
     *
     * @param   bits   一个整数。
     * @return  具有相同位模式的 {@code float} 浮点值。
     */
    public static native float intBitsToFloat(int bits);

    /**
     * 按数值比较两个 {@code Float} 对象。此方法执行的比较与 Java 语言数值比较运算符
     * ({@code <, <=, ==, >=, >}) 应用于原始 {@code float} 值时的比较有两点不同：
     *
     * <ul><li>
     *          {@code Float.NaN} 被此方法视为等于自身且大于所有其他
     *          {@code float} 值
     *          （包括 {@code Float.POSITIVE_INFINITY}）。
     * <li>
     *          {@code 0.0f} 被此方法视为大于 {@code -0.0f}。
     * </ul>
     *
     * 这确保了此方法施加的 {@code Float} 对象的 <i>自然排序</i> 与 <i>equals 一致</i>。
     *
     * @param   anotherFloat   要比较的 {@code Float}。
     * @return  值 {@code 0} 如果 {@code anotherFloat} 数值上等于此 {@code Float}；
     *          小于 {@code 0} 的值 如果此 {@code Float} 数值上小于 {@code anotherFloat}；
     *          大于 {@code 0} 的值 如果此 {@code Float} 数值上大于
     *          {@code anotherFloat}。
     *
     * @since   1.2
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Float anotherFloat) {
        return Float.compare(value, anotherFloat.value);
    }

    /**
     * 比较两个指定的 {@code float} 值。返回的整数值的符号与调用以下方法返回的整数的符号相同：
     * <pre>
     *    new Float(f1).compareTo(new Float(f2))
     * </pre>
     *
     * @param   f1        要比较的第一个 {@code float}。
     * @param   f2        要比较的第二个 {@code float}。
     * @return  值 {@code 0} 如果 {@code f1} 数值上等于 {@code f2}；
     *          小于 {@code 0} 的值 如果 {@code f1} 数值上小于
     *          {@code f2}；大于 {@code 0} 的值
     *          如果 {@code f1} 数值上大于
     *          {@code f2}。
     * @since 1.4
     */
    public static int compare(float f1, float f2) {
        if (f1 < f2)
            return -1;           // 两个值都不是 NaN，此值较小
        if (f1 > f2)
            return 1;            // 两个值都不是 NaN，此值较大

        // 不能使用 floatToRawIntBits，因为可能会有 NaN。
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
     * 返回两个 {@code float} 值中较大的一个，如同调用 {@link Math#max(float, float) Math.max} 一样。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较大的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code float} 值中较小的一个，如同调用 {@link Math#min(float, float) Math.min} 一样。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较小的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /** 使用来自 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -2671257302660747028L;
}
