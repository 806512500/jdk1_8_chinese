/*
 * 版权所有 (c) 2003, 2013, Oracle 及/或其附属公司。保留所有权利。
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

import java.io.IOException;

/**
 * 任何需要使用 {@link java.util.Formatter} 的 <tt>'s'</tt> 转换说明符执行自定义格式化的类都必须实现 <tt>Formattable</tt> 接口。此接口允许对任意对象进行基本的格式化控制。
 *
 * 例如，以下类根据标志和长度约束打印出股票名称的不同表示形式：
 *
 * {@code
 *   import java.nio.CharBuffer;
 *   import java.util.Formatter;
 *   import java.util.Formattable;
 *   import java.util.Locale;
 *   import static java.util.FormattableFlags.*;
 *
 *  ...
 *
 *   public class StockName implements Formattable {
 *       private String symbol, companyName, frenchCompanyName;
 *       public StockName(String symbol, String companyName,
 *                        String frenchCompanyName) {
 *           ...
 *       }
 *
 *       ...
 *
 *       public void formatTo(Formatter fmt, int f, int width, int precision) {
 *           StringBuilder sb = new StringBuilder();
 *
 *           // 决定名称的形式
 *           String name = companyName;
 *           if (fmt.locale().equals(Locale.FRANCE))
 *               name = frenchCompanyName;
 *           boolean alternate = (f & ALTERNATE) == ALTERNATE;
 *           boolean usesymbol = alternate || (precision != -1 && precision < 10);
 *           String out = (usesymbol ? symbol : name);
 *
 *           // 应用精度
 *           if (precision == -1 || out.length() < precision) {
 *               // 写入全部
 *               sb.append(out);
 *           } else {
 *               sb.append(out.substring(0, precision - 1)).append('*');
 *           }
 *
 *           // 应用宽度和对齐
 *           int len = sb.length();
 *           if (len < width)
 *               for (int i = 0; i < width - len; i++)
 *                   if ((f & LEFT_JUSTIFY) == LEFT_JUSTIFY)
 *                       sb.append(' ');
 *                   else
 *                       sb.insert(0, ' ');
 *
 *           fmt.format(sb.toString());
 *       }
 *
 *       public String toString() {
 *           return String.format("%s - %s", symbol, companyName);
 *       }
 *   }
 * }
 *
 * <p> 与 {@link java.util.Formatter} 结合使用时，上述类对于各种格式字符串会产生以下输出。
 *
 * {@code
 *   Formatter fmt = new Formatter();
 *   StockName sn = new StockName("HUGE", "Huge Fruit, Inc.",
 *                                "Fruit Titanesque, Inc.");
 *   fmt.format("%s", sn);                   //   -> "Huge Fruit, Inc."
 *   fmt.format("%s", sn.toString());        //   -> "HUGE - Huge Fruit, Inc."
 *   fmt.format("%#s", sn);                  //   -> "HUGE"
 *   fmt.format("%-10.8s", sn);              //   -> "HUGE      "
 *   fmt.format("%.12s", sn);                //   -> "Huge Fruit,*"
 *   fmt.format(Locale.FRANCE, "%25s", sn);  //   -> "   Fruit Titanesque, Inc."
 * }
 *
 * <p> Formattable 对象不一定支持多线程访问。线程安全性是可选的，可以由扩展和实现此接口的类来强制执行。
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此接口中的任何方法将导致抛出 {@link
 * NullPointerException}。
 *
 * @since  1.5
 */
public interface Formattable {

    /**
     * 使用提供的 {@link Formatter formatter} 格式化对象。
     *
     * @param  formatter
     *         {@link Formatter formatter}。实现类可以调用 {@link Formatter#out() formatter.out()} 或 {@link
     *         Formatter#locale() formatter.locale()} 以获取此 <tt>formatter</tt> 使用的 {@link
     *         Appendable} 或 {@link Locale}。
     *
     * @param  flags
     *         标志修改输出格式。值被解释为位掩码。可以设置以下标志的任意组合：{@link FormattableFlags#LEFT_JUSTIFY}，{@link
     *         FormattableFlags#UPPERCASE}，和 {@link
     *         FormattableFlags#ALTERNATE}。如果没有设置任何标志，则应用实现类的默认格式。
     *
     * @param  width
     *         输出中要写入的最小字符数。如果转换值的长度小于 <tt>width</tt>，则输出将通过 <tt>'&nbsp;&nbsp;'</tt> 填充，直到总字符数等于宽度。默认情况下，填充在开头。如果设置了 {@link FormattableFlags#LEFT_JUSTIFY} 标志，则填充将在末尾。如果 <tt>width</tt> 是 <tt>-1</tt>，则没有最小值。
     *
     * @param  precision
     *         输出中要写入的最大字符数。精度在宽度之前应用，因此即使 <tt>width</tt> 大于 <tt>precision</tt>，输出也将被截断为 <tt>precision</tt> 个字符。如果 <tt>precision</tt> 是 <tt>-1</tt>，则字符数没有明确的限制。
     *
     * @throws  IllegalFormatException
     *          如果任何参数无效。有关所有可能的格式错误的说明，请参阅格式化器类规范的 <a
     *          href="../util/Formatter.html#detail">详细</a> 部分。
     */
    void formatTo(Formatter formatter, int flags, int width, int precision);
}
