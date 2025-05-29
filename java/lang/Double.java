/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款限制。
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
import sun.misc.FpUtils;
import sun.misc.DoubleConsts;

/**
 * {@code Double} 类将基本类型 {@code double} 的值封装在一个对象中。类型为
 * {@code Double} 的对象包含一个类型为 {@code double} 的单一字段。
 *
 * <p>此外，该类提供了几种方法，用于将 {@code double} 转换为 {@code String}，
 * 以及将 {@code String} 转换为 {@code double}，还有其他在处理
 * {@code double} 时有用的常量和方法。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Double extends Number implements Comparable<Double> {
    /**
     * 保存类型为 {@code double} 的正无穷大的常量。它等于
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)} 返回的值。
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * 保存类型为 {@code double} 的负无穷大的常量。它等于
     * {@code Double.longBitsToDouble(0xfff0000000000000L)} 返回的值。
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * 保存类型为 {@code double} 的非数字 (NaN) 值的常量。它等价于
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)} 返回的值。
     */
    public static final double NaN = 0.0d / 0.0;

    /**
     * 保存类型为 {@code double} 的最大正有限值的常量，
     * (2-2<sup>-52</sup>)·2<sup>1023</sup>。它等于
     * 十六进制浮点字面量 {@code 0x1.fffffffffffffP+1023}，也等于
     * {@code Double.longBitsToDouble(0x7fefffffffffffffL)}。
     */
    public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308

    /**
     * 保存类型为 {@code double} 的最小正规范值的常量，2<sup>-1022</sup>。
     * 它等于十六进制浮点字面量 {@code 0x1.0p-1022}，也等于
     * {@code Double.longBitsToDouble(0x0010000000000000L)}。
     *
     * @since 1.6
     */
    public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308

    /**
     * 保存类型为 {@code double} 的最小正非零值的常量，2<sup>-1074</sup>。
     * 它等于十六进制浮点字面量 {@code 0x0.0000000000001P-1022}，也等于
     * {@code Double.longBitsToDouble(0x1L)}。
     */
    public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324

    /**
     * 有限 {@code double} 变量可能具有的最大指数。
     * 它等于 {@code Math.getExponent(Double.MAX_VALUE)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 1023;

    /**
     * 规范化的 {@code double} 变量可能具有的最小指数。
     * 它等于 {@code Math.getExponent(Double.MIN_NORMAL)} 返回的值。
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -1022;

    /**
     * 用于表示 {@code double} 值的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * 用于表示 {@code double} 值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 表示基本类型 {@code double} 的 {@code Class} 实例。
     *
     * @since JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE = (Class<Double>) Class.getPrimitiveClass("double");

    /**
     * 返回 {@code double} 参数的字符串表示形式。以下提到的所有字符均为 ASCII 字符。
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数符号和大小（绝对值）的字符串。如果符号为负，
     * 结果的第一个字符是 '{@code -}' ({@code '\u005Cu002D'})；如果符号为正，
     * 结果中不出现符号字符。对于大小 <i>m</i>：
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符串 {@code "Infinity"} 表示；
     * 因此，正无穷大产生结果 {@code "Infinity"}，负无穷大产生结果
     * {@code "-Infinity"}。
     *
     * <li>如果 <i>m</i> 是零，它由字符串 {@code "0.0"} 表示；
     * 因此，负零产生结果 {@code "-0.0"}，正零产生结果 {@code "0.0"}。
     *
     * <li>如果 <i>m</i> 大于或等于 10<sup>-3</sup> 但小于 10<sup>7</sup>，
     * 则它表示为 <i>m</i> 的整数部分（以十进制形式，无前导零），
     * 后跟 '{@code .}' ({@code '\u005Cu002E'})，再后跟一个或多个表示
     * <i>m</i> 小数部分的十进制数字。
     *
     * <li>如果 <i>m</i> 小于 10<sup>-3</sup> 或大于或等于 10<sup>7</sup>，
     * 则它以所谓的“计算机化科学记数法”表示。设 <i>n</i> 为唯一的整数，
     * 使得 10<sup><i>n</i></sup> ≤ <i>m</i> {@literal <} 10<sup><i>n</i>+1</sup>；
     * 然后让 <i>a</i> 为 <i>m</i> 与 10<sup><i>n</i></sup> 的数学精确商，
     * 使得 1 ≤ <i>a</i> {@literal <} 10。然后大小表示为 <i>a</i> 的整数部分，
     * 以单个十进制数字表示，后跟 '{@code .}' ({@code '\u005Cu002E'})，
     * 再后跟表示 <i>a</i> 小数部分的十进制数字，再后跟字母 '{@code E}'
     * ({@code '\u005Cu0045'})，最后是 <i>n</i> 作为十进制整数的表示，
     * 如同通过方法 {@link Integer#toString(int)} 产生。
     * </ul>
     * </ul>
     * 对于 <i>m</i> 或 <i>a</i> 的小数部分，必须打印多少位数字？
     * 必须至少有一位数字来表示小数部分，并且除此之外，打印尽可能多的数字，
     * 但仅限于唯一区分参数值与相邻类型 {@code double} 值所需的数字。
     * 也就是说，假设 <i>x</i> 是由该方法为有限非零参数 <i>d</i> 产生的十进制表示
     * 所表示的精确数学值。那么 <i>d</i> 必须是最接近 <i>x</i> 的
     * {@code double} 值；或者如果两个 {@code double} 值与 <i>x</i> 的距离相等，
     * 则 <i>d</i> 必须是其中之一，并且 <i>d</i> 的有效数字的最低有效位必须为
     * {@code 0}。
     *
     * <p>要创建浮点值的本地化字符串表示，请使用 {@link java.text.NumberFormat} 的子类。
     *
     * @param   d   要转换的 {@code double}。
     * @return 参数的字符串表示。
     */
    public static String toString(double d) {
        return FloatingDecimal.toJavaFormatString(d);
    }

    /**
     * 返回 {@code double} 参数的十六进制字符串表示形式。以下提到的所有字符均为 ASCII 字符。
     *
     * <ul>
     * <li>如果参数是 NaN，结果是字符串 "{@code NaN}"。
     * <li>否则，结果是一个表示参数符号和大小的字符串。如果符号为负，
     * 结果的第一个字符是 '{@code -}' ({@code '\u005Cu002D'})；
     * 如果符号为正，结果中不出现符号字符。对于大小 <i>m</i>：
     *
     * <ul>
     * <li>如果 <i>m</i> 是无穷大，它由字符串 {@code "Infinity"} 表示；
     * 因此，正无穷大产生结果 {@code "Infinity"}，负无穷大产生结果
     * {@code "-Infinity"}。
     *
     * <li>如果 <i>m</i> 是零，它由字符串 {@code "0x0.0p0"} 表示；
     * 因此，负零产生结果 {@code "-0x0.0p0"}，正零产生结果
     * {@code "0x0.0p0"}。
     *
     * <li>如果 <i>m</i> 是具有规范表示的 {@code double} 值，
     * 使用子字符串表示有效数字和指数字段。有效数字由字符 {@code "0x1."}
     * 表示，后跟有效数字其余部分的 lowercase 十六进制表示。
     * 十六进制表示中的尾随零被移除，除非所有数字均为零，在这种情况下使用单个零。
     * 接下来，指数由 {@code "p"} 表示，后跟无偏指数的十进制字符串，
     * 如同通过 {@link Integer#toString(int) Integer.toString} 调用产生。
     *
     * <li>如果 <i>m</i> 是具有次规范表示的 {@code double} 值，
     * 有效数字由字符 {@code "0x0."} 表示，后跟有效数字其余部分的十六进制表示。
     * 十六进制表示中的尾随零被移除。接下来，指数由 {@code "p-1022"} 表示。
     * 注意，次规范有效数字中必须至少有一个非零数字。
     *
     * </ul>
     *
     * </ul>
     *
     * <table border>
     * <caption>示例</caption>
     * <tr><th>浮点值</th><th>十六进制字符串</th>
     * <tr><td>{@code 1.0}</td> <td>{@code 0x1.0p0}</td>
     * <tr><td>{@code -1.0}</td> <td>{@code -0x1.0p0}</td>
     * <tr><td>{@code 2.0}</td> <td>{@code 0x1.0p1}</td>
     * <tr><td>{@code 3.0}</td> <td>{@code 0x1.8p1}</td>
     * <tr><td>{@code 0.5}</td> <td>{@code 0x1.0p-1}</td>
     * <tr><td>{@code 0.25}</td> <td>{@code 0x1.0p-2}</td>
     * <tr><td>{@code Double.MAX_VALUE}</td>
     *     <td>{@code 0x1.fffffffffffffp1023}</td>
     * <tr><td>{@code Minimum Normal Value}</td>
     *     <td>{@code 0x1.0p-1022}</td>
     * <tr><td>{@code Maximum Subnormal Value}</td>
     *     <td>{@code 0x0.fffffffffffffp-1022}</td>
     * <tr><td>{@code Double.MIN_VALUE}</td>
     *     <td>{@code 0x0.0000000000001p-1022}</td>
     * </table>
     * @param   d   要转换的 {@code double}。
     * @return 参数的十六进制字符串表示。
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public static String toHexString(double d) {
        /*
         * 模仿 C99 中第 7.19.6.1 节的 "a" 转换说明符；
         * 然而，该方法的输出更加严格地指定。
         */
        if (!isFinite(d) )
            // 对于无穷大和 NaN，使用十进制输出。
            return Double.toString(d);
        else {
            // 初始化为输出的最大大小。
            StringBuilder answer = new StringBuilder(24);

            if (Math.copySign(1.0, d) == -1.0)    // 值是负的，
                answer.append("-");                  // 因此追加符号信息

            answer.append("0x");

            d = Math.abs(d);

            if(d == 0.0) {
                answer.append("0.0p0");
            } else {
                boolean subnormal = (d < DoubleConsts.MIN_NORMAL);

                // 隔离有效数字位并 OR 入一个高阶位，
                // 以便字符串表示具有已知的长度。
                long signifBits = (Double.doubleToLongBits(d)
                                   & DoubleConsts.SIGNIF_BIT_MASK) |
                    0x1000000000000000L;

                // 次规范值具有 0 隐式位；规范值具有 1 隐式位。
                answer.append(subnormal ? "0." : "1.");

                // 隔离十六进制表示的低阶 13 位数字。
                // 如果所有数字均为零，则替换为单个 0；
                // 否则，移除所有尾随零。
                String signif = Long.toHexString(signifBits).substring(3,16);
                answer.append(signif.equals("0000000000000") ? // 13 个零
                              "0":
                              signif.replaceFirst("0{1,12}$", ""));

                answer.append('p');
                // 如果值是次规范的，使用 double 的 E_min 指数值；
                // 否则，提取并报告 d 的指数（次规范表示使用 E_min -1）。
                answer.append(subnormal ?
                              DoubleConsts.MIN_EXPONENT:
                              Math.getExponent(d));
            }
            return answer.toString();
        }
    }

    /**
     * 返回保存由参数字符串 {@code s} 表示的 {@code double} 值的
     * {@code Double} 对象。
     *
     * <p>如果 {@code s} 为 {@code null}，则抛出
     * {@code NullPointerException}。
     *
     * <p>忽略 {@code s} 中的前导和尾随空白字符。
     * 空白字符的移除如同通过 {@link String#trim} 方法；
     * 即移除 ASCII 空格和控制字符。{@code s} 的其余部分应构成
     * <i>FloatValue</i>，如词法语法规则所述：
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
     * <i>FloatTypeSuffix</i> 如
     * <cite>Java™ 语言规范</cite> 的词法结构部分定义，
     * 但数字之间不接受下划线。
     * 如果 {@code s} 不具有 <i>FloatValue</i> 的形式，
     * 则抛出 {@code NumberFormatException}。
     * 否则，{@code s} 被视为表示通常的“计算机化科学记数法”中的精确十进制值，
     * 或精确的十六进制值；然后将此精确数值概念上转换为“无限精确”的二进制值，
     * 再根据 IEEE 754 浮点运算的通常四舍五入规则四舍五入为类型 {@code double}，
     * 包括保留零值的符号。
     *
     * 注意，四舍五入规则也意味着溢出和下溢行为；
     * 如果 {@code s} 的精确值在绝对值上足够大
     * （大于或等于 ({@link #MAX_VALUE} + {@link Math#ulp(double) ulp(MAX_VALUE)}/2)），
     * 四舍五入到 {@code double} 将导致无穷大；
     * 如果 {@code s} 的精确值在绝对值上足够小
     * （小于或等于 {@link #MIN_VALUE}/2），
     * 四舍五入到 float 将导致零。
     *
     * 最后，返回表示此 {@code double} 值的 {@code Double} 对象。
     *
     * <p>要解释浮点值的本地化字符串表示，请使用 {@link java.text.NumberFormat} 的子类。
     *
     * <p>注意，尾随格式说明符（指定浮点字面量类型的说明符，
     * 如 {@code 1.0f} 是 {@code float} 值；
     * {@code 1.0d} 是 {@code double} 值），
     * <em>不会</em> 影响此方法的结果。换句话说，输入字符串的数值直接转换为目标浮点类型。
     * 字符串到 {@code float} 再到 {@code double} 的两步转换序列，
     * <em>不</em> 等价于直接将字符串转换为 {@code double}。
     * 例如，{@code float} 字面量 {@code 0.1f} 等于 {@code double} 值
     * {@code 0.10000000149011612}；{@code float} 字面量
     * {@code 0.1f} 表示的数值与 {@code double} 字面量
     * {@code 0.1} 不同。（数值 0.1 无法在二进制浮点数中精确表示。）
     *
     * <p>为避免在无效字符串上调用此方法而抛出 {@code NumberFormatException}，
     * 可使用以下正则表达式筛选输入字符串：
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
     *       // 表示有限正数的十进制浮点字符串（无前导符号）最多有五个基本部分：
     *       // Digits . Digits ExponentPart FloatTypeSuffix
     *       //
     *       // 由于此方法允许仅整数的字符串作为输入，
     *       // 除了浮点字面量的字符串外，以下两个子模式是
     *       // 《Java 语言规范》第 3.10.2 节的语法产生式的简化。
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
     *      // 执行适当的替代操作
     *  }
     * }</pre>
     *
     * @param      s   要解析的字符串。
     * @return     保存由 {@code String} 参数表示的值的 {@code Double} 对象。
     * @throws     NumberFormatException  如果字符串不包含可解析的数字。
     */
    public static Double valueOf(String s) throws NumberFormatException {
        return new Double(parseDouble(s));
    }

    /**
     * 返回表示指定 {@code double} 值的 {@code Double} 实例。
     * 如果不需要新的 {@code Double} 实例，通常应优先使用此方法，
     * 而不是构造函数 {@link #Double(double)}，因为此方法通过缓存常用值，
     * 可能会显著提高空间和时间性能。
     *
     * @param  d 一个 double 值。
     * @return 表示 {@code d} 的 {@code Double} 实例。
     * @since  1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * 返回初始化为由指定 {@code String} 表示的值的新 {@code double}，
     * 如同通过类 {@code Double} 的 {@code valueOf} 方法执行。
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
     * 如果指定数字是非数字 (NaN) 值，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   v   要测试的值。
     * @return  如果参数的值是 NaN，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public static boolean isNaN(double v) {
        return (v != v);
    }

    /**
     * 如果指定数字在绝对值上是无限大的，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param   v   要测试的值。
     * @return  如果参数的值是正无穷大或负无穷大，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public static boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * 如果参数是有限浮点值，则返回 {@code true}；
     * 否则返回 {@code false}（对于 NaN 和无穷大参数）。
     *
     * @param d 要测试的 {@code double} 值
     * @return 如果参数是有限浮点值，则返回 {@code true}，否则返回 {@code false}。
     * @since 1.8
     */
    public static boolean isFinite(double d) {
        return Math.abs(d) <= DoubleConsts.MAX_VALUE;
    }

    /**
     * Double 的值。
     *
     * @serial
     */
    private final double value;

    /**
     * 构造一个新分配的 {@code Double} 对象，表示基本类型 {@code double} 参数。
     *
     * @param   value   由 {@code Double} 表示的值。
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * 构造一个新分配的 {@code Double} 对象，表示由字符串表示的类型为
     * {@code double} 的浮点值。字符串如同通过 {@code valueOf} 方法转换为
     * {@code double} 值。
     *
     * @param  s  要转换为 {@code Double} 的字符串。
     * @throws    NumberFormatException  如果字符串不包含可解析的数字。
     * @see       java.lang.Double#valueOf(java.lang.String)
     */
    public Double(String s) throws NumberFormatException {
        value = parseDouble(s);
    }

    /**
     * 如果此 {@code Double} 值是非数字 (NaN)，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return  如果此对象表示的值是 NaN，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * 如果此 {@code Double} 值在绝对值上是无限大的，则返回 {@code true}，
     * 否则返回 {@code false}。
     *
     * @return  如果此对象表示的值是正无穷大或负无穷大，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * 返回此 {@code Double} 对象的字符串表示。
     * 此对象表示的基本 {@code double} 值被转换为字符串，
     * 如同通过单参数的 {@code toString} 方法转换。
     *
     * @return  此对象的 {@code String} 表示。
     * @see java.lang.Double#toString(double)
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Double} 的值作为 {@code byte}。
     *
     * @return  此对象表示的 {@code double} 值转换为类型 {@code byte}
     * @jls 5.1.3 窄化基本类型转换
     * @since JDK1.1
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Double} 的值作为 {@code short}。
     *
     * @return  此对象表示的 {@code double} 值转换为类型 {@code short}
     * @jls 5.1.3 窄化基本类型转换
     * @since JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Double} 的值作为 {@code int}。
     * @jls 5.1.3 窄化基本类型转换
     *
     * @return  此对象表示的 {@code double} 值转换为类型 {@code int}
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Double} 的值作为 {@code long}。
     *
     * @return  此对象表示的 {@code double} 值转换为类型 {@code long}
     * @jls 5.1.3 窄化基本类型转换
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Double} 的值作为 {@code float}。
     *
     * @return  此对象表示的 {@code double} 值转换为类型 {@code float}
     * @jls 5.1.3 窄化基本类型转换
     * @since JDK1.0
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Double} 对象的 {@code double} 值。
     *
     * @return 此对象表示的 {@code double} 值
     */
    public double doubleValue() {
        return value;
    }

    /**
     * 为此 {@code Double} 对象返回一个哈希码。
     * 结果是此对象表示的基本 {@code double} 值的
     * {@code long} 整数位表示的两半的异或，
     * 如同通过方法 {@link #doubleToLongBits(double)} 产生。
     * 也就是说，哈希码是以下表达式的值：
     *
     * <blockquote>
     *  {@code (int)(v^(v>>>32))}
     * </blockquote>
     *
     * 其中 {@code v} 定义为：
     *
     * <blockquote>
     *  {@code long v = Double.doubleToLongBits(this.doubleValue());}
     * </blockquote>
     *
     * @return  此对象的哈希码值。
     */
    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    /**
     * 为 {@code double} 值返回一个哈希码；与 {@code Double.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return {@code double} 值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(double value) {
        long bits = doubleToLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }

    /**
     * 将此对象与指定对象进行比较。
     * 仅当参数不为 {@code null} 且是表示与此对象表示的
     * {@code double} 值相同的 {@code Double} 对象时，结果为 {@code true}。
     * 为此，两个 {@code double} 值仅在方法 {@link #doubleToLongBits(double)}
     * 应用于每个值时返回相同的 {@code long} 值时才被视为相同。
     *
     * <p>请注意，在大多数情况下，对于类 {@code Double} 的两个实例
     * {@code d1} 和 {@code d2}，当且仅当
     *
     * <blockquote>
     *  {@code d1.doubleValue() == d2.doubleValue()}
     * </blockquote>
     *
     * 值为 {@code true} 时，{@code d1.equals(d2)} 的值也为 {@code true}。
     * 然而，有两个例外：
     * <ul>
     * <li>如果 {@code d1} 和 {@code d2} 都表示 {@code Double.NaN}，
     * 则 {@code equals} 方法返回 {@code true}，
     * 即使 {@code Double.NaN==Double.NaN} 的值为 {@code false}。
     * <li>如果 {@code d1} 表示 {@code +0.0} 而 {@code d2} 表示 {@code -0.0}，
     * 或反之，则 {@code equal} 测试的值为 {@code false}，
     * 即使 {@code +0.0==-0.0} 的值为 {@code true}。
     * </ul>
     * 此定义允许哈希表正常运行。
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
     * 根据 IEEE 754 浮点“double 格式”位布局，
     * 返回指定浮点值的表示。
     *
     * <p>第 63 位（由掩码 {@code 0x8000000000000000L} 选择的位）表示浮点数的符号。
     * 第 62-52 位（由掩码 {@code 0x7ff0000000000000L} 选择的位）表示指数。
     * 第 51-0 位（由掩码 {@code 0x000fffffffffffffL} 选择的位）表示浮点数的有效数字
     * （有时称为尾数）。
     *
     * <p>如果参数是正无穷大，结果是 {@code 0x7ff0000000000000L}。
     *
     * <p>如果参数是负无穷大，结果是 {@code 0xfff0000000000000L}。
     *
     * <p>如果参数是 NaN，结果是 {@code 0x7ff8000000000000L}。
     *
     * <p>在所有情况下，结果是一个 {@code long} 整数，当提供给
     * {@link #longBitsToDouble(long)} 方法时，将产生与
     * {@code doubleToLongBits} 的参数相同的浮点值
     * （除了所有 NaN 值被折叠为单个“规范” NaN 值）。
     *
     * @param   value   一个 {@code double} 精度的浮点数。
     * @return 表示浮点数的位。
     */
    public static long doubleToLongBits(double value) {
        long result = doubleToRawLongBits(value);
        // 根据位字段的值检查 NaN，最大指数和非零有效数字。
        if ( ((result & DoubleConsts.EXP_BIT_MASK) ==
              DoubleConsts.EXP_BIT_MASK) &&
             (result & DoubleConsts.SIGNIF_BIT_MASK) != 0L)
            result = 0x7ff8000000000000L;
        return result;
    }

    /**
     * 根据 IEEE 754 浮点“double 格式”位布局，
     * 返回指定浮点值的表示，保留非数字 (NaN) 值。
     *
     * <p>第 63 位（由掩码 {@code 0x8000000000000000L} 选择的位）表示浮点数的符号。
     * 第 62-52 位（由掩码 {@code 0x7ff0000000000000L} 选择的位）表示指数。
     * 第 51-0 位（由掩码 {@code 0x000fffffffffffffL} 选择的位）表示浮点数的有效数字
     * （有时称为尾数）。
     *
     * <p>如果参数是正无穷大，结果是 {@code 0x7ff0000000000000L}。
     *
     * <p>如果参数是负无穷大，结果是 {@code 0xfff0000000000000L}。
     *
     * <p>如果参数是 NaN，结果是表示实际 NaN 值的 {@code long} 整数。
     * 与 {@code doubleToLongBits} 方法不同，
     * {@code doubleToRawLongBits} 不会将所有编码 NaN 的位模式折叠为
     * 单个“规范” NaN 值。
     *
     * <p>在所有情况下，结果是一个 {@code long} 整数，当提供给
     * {@link #longBitsToDouble(long)} 方法时，将产生与
     * {@code doubleToRawLongBits} 的参数相同的浮点值。
     *
     * @param   value   一个 {@code double} 精度的浮点数。
     * @return 表示浮点数的位。
     * @since 1.3
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * 返回与给定位表示对应的 {@code double} 值。
     * 参数被视为根据 IEEE 754 浮点“double 格式”位布局的浮点值表示。
     *
     * <p>如果参数是 {@code 0x7ff0000000000000L}，结果是正无穷大。
     *
     * <p>如果参数是 {@code 0xfff0000000000000L}，结果是负无穷大。
     *
     * <p>如果参数是范围 {@code 0x7ff0000000000001L} 到
     * {@code 0x7fffffffffffffffL} 或范围 {@code 0xfff0000000000001L} 到
     * {@code 0xffffffffffffffffL} 中的任何值，结果是 NaN。
     * Java 提供的 IEEE 754 浮点运算无法区分具有不同位模式的同一类型的两个 NaN 值。
     * 不同的 NaN 值只能通过使用 {@code Double.doubleToRawLongBits} 方法区分。
     *
     * <p>在所有其他情况下，设 <i>s</i>、<i>e</i> 和 <i>m</i> 为可从参数计算的三个值：
     *
     * <blockquote><pre>{@code
     * int s = ((bits >> 63) == 0) ? 1 : -1;
     * int e = (int)((bits >> 52) & 0x7ffL);
     * long m = (e == 0) ?
     *                 (bits & 0xfffffffffffffL) << 1 :
     *                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
     * }</pre></blockquote>
     *
     * 然后浮点结果等于数学表达式 <i>s</i>·<i>m</i>·2<sup><i>e</i>-1075</sup> 的值。
     *
     * <p>请注意，此方法可能无法返回与 {@code long} 参数完全相同位模式的
     * {@code double} NaN。IEEE 754 区分两种 NaN：安静 NaN 和 <i>信令 NaN</i>。
     * 这两种 NaN 之间的差异在 Java 中通常不可见。对信令 NaN 的算术运算会将它们
     * 转换为具有不同但通常相似位模式的安静 NaN。然而，在某些处理器上，仅复制
     * 信令 NaN 也会执行该转换。特别是，将信令 NaN 复制以返回给调用方法可能会执行此转换。
     * 因此，{@code longBitsToDouble} 可能无法返回具有信令 NaN 位模式的
     * {@code double}。因此，对于某些 {@code long} 值，
     * {@code doubleToRawLongBits(longBitsToDouble(start))} 可能
     * <i>不</i> 等于 {@code start}。此外，表示信令 NaN 的特定位模式依赖于平台；
     * 尽管所有 NaN 位模式（安静或信令）都必须在上述 NaN 范围内。
     *
     * @param   bits   任何 {@code long} 整数。
     * @return  具有相同位模式的 {@code double} 浮点值。
     */
    public static native double longBitsToDouble(long bits);

    /**
     * 在数值上比较两个 {@code Double} 对象。
     * 此方法的比较方式与 Java 语言数值比较运算符
     * ({@code <, <=, ==, >=, >}) 应用于基本 {@code double} 值时的比较方式有两个不同之处：
     * <ul><li>
     *          此方法认为 {@code Double.NaN} 等于自身，
     *          并且大于所有其他 {@code double} 值（包括
     *          {@code Double.POSITIVE_INFINITY}）。
     * <li>
     *          此方法认为 {@code 0.0d} 大于 {@code -0.0d}。
     * </ul>
     * 这确保了此方法强加的 {@code Double} 对象的<i>自然排序</i>与 equals <i>一致</i>。
     *
     * @param   anotherDouble   要比较的 {@code Double}。
     * @return  如果 {@code anotherDouble} 在数值上等于此 {@code Double}，则返回 {@code 0}；
     *          如果此 {@code Double} 在数值上小于 {@code anotherDouble}，则返回小于 {@code 0} 的值；
     *          如果此 {@code Double} 在数值上大于 {@code anotherDouble}，则返回大于 {@code 0} 的值。
     *
     * @since   1.2
     */
    public int compareTo(Double anotherDouble) {
        return Double.compare(value, anotherDouble.value);
    }

    /**
     * 比较两个指定的 {@code double} 值。
     * 返回的整数值的符号与以下调用的返回整数相同：
     * <pre>
     *    new Double(d1).compareTo(new Double(d2))
     * </pre>
     *
     * @param   d1        要比较的第一个 {@code double}
     * @param   d2        要比较的第二个 {@code double}
     * @return  如果 {@code d1} 在数值上等于 {@code d2}，则返回 {@code 0}；
     *          如果 {@code d1} 在数值上小于 {@code d2}，则返回小于 {@code 0} 的值；
     *          如果 {@code d1} 在数值上大于 {@code d2}，则返回大于 {@code 0} 的值。
     * @since 1.4
     */
    public static int compare(double d1, double d2) {
        if (d1 < d2)
            return -1;           // 两个值都不是 NaN，此值较小
        if (d1 > d2)
            return 1;            // 两个值都不是 NaN，此值较大

        // 不能使用 doubleToRawLongBits，因为可能存在 NaN。
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
     * 返回两个 {@code double} 值中较大的一个，
     * 如同调用 {@link Math#max(double, double) Math.max}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较大的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code double} 值中较小的一个，
     * 如同调用 {@link Math#min(double, double) Math.min}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较小的一个。
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    /** 为互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -9172774392245257468L;
}