
/*
 * 版权所有 (c) 1996, 2020, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * (C) 版权所有 Taligent, Inc. 1996, 1997 - 保留所有权利
 * (C) 版权所有 IBM Corp. 1996 - 1998 - 保留所有权利
 *
 *   本源代码和文档的原始版本受版权保护并归 Taligent, Inc. 所有，Taligent, Inc. 是 IBM 的全资子公司。这些
 * 材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受多项美国和国际专利保护。此通知和对 Taligent 的
 * 致谢不得删除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.spi.NumberFormatProvider;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

/**
 * <code>DecimalFormat</code> 是 <code>NumberFormat</code> 的具体子类，用于格式化十进制数字。它具有多种
 * 功能，旨在使解析和格式化任何语言环境中的数字成为可能，包括支持西方、阿拉伯和印度数字。它还支持不同类型的数字，
 * 包括整数（123）、定点数（123.4）、科学记数法（1.23E4）、百分比（12%）和货币金额（$123）。所有这些都可以本地化。
 *
 * <p>要为特定语言环境（包括默认语言环境）获取 <code>NumberFormat</code>，请调用 <code>NumberFormat</code> 的工厂方法之一，
 * 例如 <code>getInstance()</code>。通常不要直接调用 <code>DecimalFormat</code> 构造函数，因为 <code>NumberFormat</code>
 * 工厂方法可能返回其他子类而不是 <code>DecimalFormat</code>。如果需要自定义格式对象，可以这样做：
 *
 * <blockquote><pre>
 * NumberFormat f = NumberFormat.getInstance(loc);
 * if (f instanceof DecimalFormat) {
 *     ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(true);
 * }
 * </pre></blockquote>
 *
 * <p><code>DecimalFormat</code> 包含一个 <em>模式</em> 和一组 <em>符号</em>。模式可以直接使用
 * <code>applyPattern()</code> 设置，也可以通过 API 方法间接设置。符号存储在 <code>DecimalFormatSymbols</code>
 * 对象中。使用 <code>NumberFormat</code> 工厂方法时，模式和符号从本地化的 <code>ResourceBundle</code> 中读取。
 *
 * <h3>模式</h3>
 *
 * <code>DecimalFormat</code> 模式具有以下语法：
 * <blockquote><pre>
 * <i>Pattern:</i>
 *         <i>PositivePattern</i>
 *         <i>PositivePattern</i> ; <i>NegativePattern</i>
 * <i>PositivePattern:</i>
 *         <i>Prefix<sub>opt</sub></i> <i>Number</i> <i>Suffix<sub>opt</sub></i>
 * <i>NegativePattern:</i>
 *         <i>Prefix<sub>opt</sub></i> <i>Number</i> <i>Suffix<sub>opt</sub></i>
 * <i>Prefix:</i>
 *         除 &#92;uFFFE, &#92;uFFFF 和特殊字符外的任何 Unicode 字符
 * <i>Suffix:</i>
 *         除 &#92;uFFFE, &#92;uFFFF 和特殊字符外的任何 Unicode 字符
 * <i>Number:</i>
 *         <i>Integer</i> <i>Exponent<sub>opt</sub></i>
 *         <i>Integer</i> . <i>Fraction</i> <i>Exponent<sub>opt</sub></i>
 * <i>Integer:</i>
 *         <i>MinimumInteger</i>
 *         #
 *         # <i>Integer</i>
 *         # , <i>Integer</i>
 * <i>MinimumInteger:</i>
 *         0
 *         0 <i>MinimumInteger</i>
 *         0 , <i>MinimumInteger</i>
 * <i>Fraction:</i>
 *         <i>MinimumFraction<sub>opt</sub></i> <i>OptionalFraction<sub>opt</sub></i>
 * <i>MinimumFraction:</i>
 *         0 <i>MinimumFraction<sub>opt</sub></i>
 * <i>OptionalFraction:</i>
 *         # <i>OptionalFraction<sub>opt</sub></i>
 * <i>Exponent:</i>
 *         E <i>MinimumExponent</i>
 * <i>MinimumExponent:</i>
 *         0 <i>MinimumExponent<sub>opt</sub></i>
 * </pre></blockquote>
 *
 * <p><code>DecimalFormat</code> 模式包含一个正模式和一个负模式，例如 <code>"#,##0.00;(#,##0.00)"</code>。每个
 * 子模式都有前缀、数字部分和后缀。负模式是可选的；如果省略，则使用带有本地化减号（大多数语言环境中的 <code>'-'</code>）
 * 的正模式作为负模式。也就是说，<code>"0.00"</code> 等同于 <code>"0.00;-0.00"</code>。如果指定了显式的负模式，它仅用于
 * 指定负前缀和后缀；数字位数、最小位数和其他特性与正模式相同。这意味着 <code>"#,##0.0#;(#)"</code> 产生的行为与
 * <code>"#,##0.0#;(#,##0.0#)"</code> 完全相同。
 *
 * <p>前缀、后缀以及用于无穷大、数字、千位分隔符、小数点等的符号可以设置为任意值，并在格式化期间正确显示。然而，必须注意这些
 * 符号和字符串不能冲突，否则解析将不可靠。例如，正前缀和负前缀或后缀必须是不同的，以便 <code>DecimalFormat.parse()</code>
 * 能够区分正数和负数。（如果它们相同，则 <code>DecimalFormat</code> 会表现得好像没有指定负模式一样。）另一个例子是小数点和
 * 千位分隔符应该是不同的字符，否则解析将不可能。
 *
 * <p>分组分隔符通常用于千位，但在某些国家/地区，它用于万位。分组大小是分组字符之间的常数位数，例如 3 用于 100,000,000 或 4 用于
 * 1,0000,0000。如果提供了一个带有多个分组字符的模式，则使用最后一个分组字符和整数末尾之间的间隔。因此 <code>"#,##,###,####"</code>
 * == <code>"######,####"</code> == <code>"##,####,####"</code>。
 *
 * <h4>特殊模式字符</h4>
 *
 * <p>模式中的许多字符都被直接使用；它们在解析时匹配，并在格式化时不变地输出。特殊字符代表其他字符、字符串或字符类。如果它们要作为
 * 前缀或后缀中的文字出现，必须进行引用，除非另有说明。
 *
 * <p>以下字符用于非本地化的模式。本地化模式使用此格式化器的 <code>DecimalFormatSymbols</code> 对象中相应的字符，这些字符失去
 * 其特殊状态。两个例外是货币符号和引号，它们不会本地化。
 *
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="显示符号、位置、是否本地化和含义的图表。">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>符号
 *          <th align=left>位置
 *          <th align=left>是否本地化
 *          <th align=left>含义
 *     <tr valign=top>
 *          <td><code>0</code>
 *          <td>数字
 *          <td>是
 *          <td>数字
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>#</code>
 *          <td>数字
 *          <td>是
 *          <td>数字，零显示为缺失
 *     <tr valign=top>
 *          <td><code>.</code>
 *          <td>数字
 *          <td>是
 *          <td>小数点或货币小数点
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>-</code>
 *          <td>数字
 *          <td>是
 *          <td>减号
 *     <tr valign=top>
 *          <td><code>,</code>
 *          <td>数字
 *          <td>是
 *          <td>分组分隔符
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>E</code>
 *          <td>数字
 *          <td>是
 *          <td>科学记数法中的尾数和指数之间的分隔符。
 *              <em>在前缀或后缀中不需要引用。</em>
 *     <tr valign=top>
 *          <td><code>;</code>
 *          <td>子模式边界
 *          <td>是
 *          <td>分隔正负子模式
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>%</code>
 *          <td>前缀或后缀
 *          <td>是
 *          <td>乘以 100 并显示为百分比
 *     <tr valign=top>
 *          <td><code>&#92;u2030</code>
 *          <td>前缀或后缀
 *          <td>是
 *          <td>乘以 1000 并显示为千分比值
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>&#164;</code> (<code>&#92;u00A4</code>)
 *          <td>前缀或后缀
 *          <td>否
 *          <td>货币符号，被货币符号替换。如果加倍，则被国际货币符号替换。
 *              如果在模式中出现，则使用货币小数点而不是小数点。
 *     <tr valign=top>
 *          <td><code>'</code>
 *          <td>前缀或后缀
 *          <td>否
 *          <td>用于在前缀或后缀中引用特殊字符，例如，<code>"'#'#"</code> 将 123 格式化为
 *              <code>"#123"</code>。要创建单引号本身，请使用两个连续的引号：<code>"# o''clock"</code>。
 * </table>
 * </blockquote>
 *
 * <h4>科学记数法</h4>
 *
 * <p>科学记数法中的数字表示为尾数和 10 的幂的乘积，例如，1234 可以表示为 1.234 x 10^3。尾数通常在 1.0 &le; x {@literal <} 10.0 范围内，
 * 但不一定如此。<code>DecimalFormat</code> 可以通过模式指示格式化和解析科学记数法；目前没有创建科学记数法格式的工厂方法。在模式中，指数字符
 * 紧跟一个或多个数字字符表示科学记数法。例如：<code>"0.###E0"</code> 将数字 1234 格式化为 <code>"1.234E3"</code>。
 *
 * <ul>
 * <li>指数字符后的数字字符数表示最小指数位数。没有最大值。负指数使用本地化的减号格式化，<em>而不是</em>模式中的前缀和后缀。这允许使用
 * 模式，例如 <code>"0.###E0 m/s"</code>。
 *
 * <li>最小和最大整数位数一起解释：
 *
 * <ul>
 * <li>如果最大整数位数大于最小整数位数且大于 1，则强制指数为最大整数位数的倍数，并将最小整数位数解释为 1。最常见的用法是生成
 * <em>工程记数法</em>，其中指数为三的倍数，例如 <code>"##0.#####E0"</code>。使用此模式，数字 12345 格式化为 <code>"12.345E3"</code>，
 * 而 123456 格式化为 <code>"123.456E3"</code>。
 *
 * <li>否则，通过调整指数来实现最小整数位数。例如：0.00123 用 <code>"00.###E0"</code> 格式化后得到 <code>"12.3E-4"</code>。
 * </ul>
 *
 * <li>尾数中的有效位数是 <em>最小整数</em> 和 <em>最大小数</em> 位数的总和，不受最大整数位数的影响。例如，12345 用 <code>"##0.##E0"</code>
 * 格式化为 <code>"12.3E3"</code>。要显示所有数字，将有效位数设置为零。有效位数不影响解析。
 *
 * <li>指数模式中不能包含分组分隔符。
 * </ul>
 *
 * <h4>舍入</h4>
 *
 * <code>DecimalFormat</code> 提供了在 {@link java.math.RoundingMode} 中定义的舍入模式用于格式化。默认情况下，它使用
 * {@link java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}。
 *
 * <h4>数字</h4>
 *
 * 对于格式化，<code>DecimalFormat</code> 使用 <code>DecimalFormatSymbols</code> 对象中定义的本地化零数字开始的十个连续字符作为数字。
 * 对于解析，这些数字以及所有 Unicode 十进制数字，如 {@link Character#digit Character.digit} 所定义，都会被识别。
 *
 * <h4>特殊值</h4>
 *
 * <p><code>NaN</code> 被格式化为字符串，通常只有一个字符 <code>&#92;uFFFD</code>。此字符串由 <code>DecimalFormatSymbols</code>
 * 对象确定。这是唯一一个不使用前缀和后缀的值。
 *
 * <p>无穷大被格式化为字符串，通常只有一个字符 <code>&#92;u221E</code>，并应用正或负前缀和后缀。无穷大字符串由
 * <code>DecimalFormatSymbols</code> 对象确定。
 *
 * <p>负零 (<code>"-0"</code>) 解析为
 * <ul>
 * <li><code>BigDecimal(0)</code> 如果 <code>isParseBigDecimal()</code> 为 true，
 * <li><code>Long(0)</code> 如果 <code>isParseBigDecimal()</code> 为 false 且 <code>isParseIntegerOnly()</code> 为 true，
 * <li><code>Double(-0.0)</code> 如果 <code>isParseBigDecimal()</code> 和 <code>isParseIntegerOnly()</code> 都为 false。
 * </ul>
 *
 * <h4><a name="synchronization">同步</a></h4>
 *
 * <p>
 * 十进制格式通常不是同步的。建议为每个线程创建单独的格式实例。如果多个线程同时访问格式，必须在外部进行同步。
 *
 * <h4>示例</h4>
 *
 * <blockquote><pre>{@code
 * <strong>// 打印出每个语言环境的本地化数字、整数、货币和百分比格式的数字</strong>
 * Locale[] locales = NumberFormat.getAvailableLocales();
 * double myNumber = -1234.56;
 * NumberFormat form;
 * for (int j = 0; j < 4; ++j) {
 *     System.out.println("FORMAT");
 *     for (int i = 0; i < locales.length; ++i) {
 *         if (locales[i].getCountry().length() == 0) {
 *            continue; // 跳过只有语言的语言环境
 *         }
 *         System.out.print(locales[i].getDisplayName());
 *         switch (j) {
 *         case 0:
 *             form = NumberFormat.getInstance(locales[i]); break;
 *         case 1:
 *             form = NumberFormat.getIntegerInstance(locales[i]); break;
 *         case 2:
 *             form = NumberFormat.getCurrencyInstance(locales[i]); break;
 *         default:
 *             form = NumberFormat.getPercentInstance(locales[i]); break;
 *         }
 *         if (form instanceof DecimalFormat) {
 *             System.out.print(": " + ((DecimalFormat) form).toPattern());
 *         }
 *         System.out.print(" -> " + form.format(myNumber));
 *         try {
 *             System.out.println(" -> " + form.parse(form.format(myNumber)));
 *         } catch (ParseException e) {}
 *     }
 * }
 * }</pre></blockquote>
 *
 * @see          <a href="https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html">Java 教程</a>
 * @see          NumberFormat
 * @see          DecimalFormatSymbols
 * @see          ParsePosition
 * @author       Mark Davis
 * @author       Alan Liu
 */
public class DecimalFormat extends NumberFormat {

                /**
     * 使用默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的默认模式和符号创建一个 DecimalFormat。
     * 当国际化不是主要关注点时，这是一种方便获取 DecimalFormat 的方式。
     * <p>
     * 要为给定的本地获取标准格式，请使用 NumberFormat 上的工厂方法，如 getNumberInstance。这些工厂将
     * 返回给定本地最合适的 NumberFormat 子类。
     *
     * @see java.text.NumberFormat#getInstance
     * @see java.text.NumberFormat#getNumberInstance
     * @see java.text.NumberFormat#getCurrencyInstance
     * @see java.text.NumberFormat#getPercentInstance
     */
    public DecimalFormat() {
        // 获取默认本地的模式。
        Locale def = Locale.getDefault(Locale.Category.FORMAT);
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class, def);
        if (!(adapter instanceof ResourceBundleBasedAdapter)) {
            adapter = LocaleProviderAdapter.getResourceBundleBased();
        }
        String[] all = adapter.getLocaleResources(def).getNumberPatterns();

        // 始终在设置符号后应用模式
        this.symbols = DecimalFormatSymbols.getInstance(def);
        applyPattern(all[0], false);
    }


    /**
     * 使用给定的模式和默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的符号创建一个 DecimalFormat。
     * 当国际化不是主要关注点时，这是一种方便获取 DecimalFormat 的方式。
     * <p>
     * 要为给定的本地获取标准格式，请使用 NumberFormat 上的工厂方法，如 getNumberInstance。这些工厂将
     * 返回给定本地最合适的 NumberFormat 子类。
     *
     * @param pattern 非本地化的模式字符串。
     * @exception NullPointerException 如果 <code>pattern</code> 为 null
     * @exception IllegalArgumentException 如果给定的模式无效。
     * @see java.text.NumberFormat#getInstance
     * @see java.text.NumberFormat#getNumberInstance
     * @see java.text.NumberFormat#getCurrencyInstance
     * @see java.text.NumberFormat#getPercentInstance
     */
    public DecimalFormat(String pattern) {
        // 始终在设置符号后应用模式
        this.symbols = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        applyPattern(pattern, false);
    }


    /**
     * 使用给定的模式和符号创建一个 DecimalFormat。
     * 当你需要完全自定义格式的行为时，使用此构造函数。
     * <p>
     * 要为给定的本地获取标准格式，请使用 NumberFormat 上的工厂方法，如
     * getInstance 或 getCurrencyInstance。如果你只需要对标准格式进行轻微调整，
     * 可以修改由 NumberFormat 工厂方法返回的格式。
     *
     * @param pattern 非本地化的模式字符串
     * @param symbols 要使用的符号集
     * @exception NullPointerException 如果给定的任何参数为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     * @see java.text.NumberFormat#getInstance
     * @see java.text.NumberFormat#getNumberInstance
     * @see java.text.NumberFormat#getCurrencyInstance
     * @see java.text.NumberFormat#getPercentInstance
     * @see java.text.DecimalFormatSymbols
     */
    public DecimalFormat (String pattern, DecimalFormatSymbols symbols) {
        // 始终在设置符号后应用模式
        this.symbols = (DecimalFormatSymbols)symbols.clone();
        applyPattern(pattern, false);
    }


    // 覆盖
    /**
     * 格式化一个数字，并将结果文本附加到给定的字符串缓冲区。
     * 数字可以是 {@link java.lang.Number} 的任何子类。
     * <p>
     * 此实现使用允许的最大精度。
     * @param number     要格式化的数字
     * @param toAppendTo 要附加格式化文本的 <code>StringBuffer</code>
     * @param pos        输入：对齐字段，如果需要的话。
     *                   输出：对齐字段的偏移量。
     * @return           传递的 <code>toAppendTo</code> 值
     * @exception        IllegalArgumentException 如果 <code>number</code> 为 null 或不是 <code>Number</code> 的实例。
     * @exception        NullPointerException 如果 <code>toAppendTo</code> 或 <code>pos</code> 为 null
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see              java.text.FieldPosition
     */
    @Override
    public final StringBuffer format(Object number,
                                     StringBuffer toAppendTo,
                                     FieldPosition pos) {
        if (number instanceof Long || number instanceof Integer ||
                   number instanceof Short || number instanceof Byte ||
                   number instanceof AtomicInteger ||
                   number instanceof AtomicLong ||
                   (number instanceof BigInteger &&
                    ((BigInteger)number).bitLength () < 64)) {
            return format(((Number)number).longValue(), toAppendTo, pos);
        } else if (number instanceof BigDecimal) {
            return format((BigDecimal)number, toAppendTo, pos);
        } else if (number instanceof BigInteger) {
            return format((BigInteger)number, toAppendTo, pos);
        } else if (number instanceof Number) {
            return format(((Number)number).doubleValue(), toAppendTo, pos);
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Number");
        }
    }

    /**
     * 格式化一个 double 以生成一个字符串。
     * @param number    要格式化的 double
     * @param result    文本要附加的位置
     * @param fieldPosition    输入：对齐字段，如果需要的话。
     * 输出：对齐字段的偏移量。
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @return 格式化的数字字符串
     * @see java.text.FieldPosition
     */
    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
        // 如果 fieldPosition 是 DontCareFieldPosition 实例，我们可以
        // 尝试使用快速路径代码。
        boolean tryFastPath = false;
        if (fieldPosition == DontCareFieldPosition.INSTANCE)
            tryFastPath = true;
        else {
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(0);
        }


                    if (tryFastPath) {
            String tempResult = fastFormat(number);
            if (tempResult != null) {
                result.append(tempResult);
                return result;
            }
        }

        // 如果快速路径无法工作，我们回退到标准代码。
        return format(number, result, fieldPosition.getFieldDelegate());
    }

    /**
     * 将 double 格式化以生成字符串。
     * @param number    要格式化的 double
     * @param result    文本将被追加到的位置
     * @param delegate 通知子字段的位置
     * @exception       如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY，则抛出 ArithmeticException
     * @return 格式化的数字字符串
     */
    private StringBuffer format(double number, StringBuffer result,
                                FieldDelegate delegate) {
        if (Double.isNaN(number) ||
           (Double.isInfinite(number) && multiplier == 0)) {
            int iFieldStart = result.length();
            result.append(symbols.getNaN());
            delegate.formatted(INTEGER_FIELD, Field.INTEGER, Field.INTEGER,
                               iFieldStart, result.length(), result);
            return result;
        }

        /* 检测 double 是否为负数相对简单，但 -0.0 除外。这是一个具有零尾数（和指数）但负号位的 double。
         * 它在语义上与正号位的零不同，这种区别对某些类型的计算很重要。然而，检测起来有点棘手，因为 (-0.0 == 0.0) 且 !(-0.0 < 0.0)。
         * 那么，你可能会问，它如何与 +0.0 表现不同？嗯，1/(-0.0) == -Infinity。正确检测 -0.0 是解决 bug 4106658、4106667 和 4147706 提出的问题所必需的。Liu 7/6/98。
         */
        boolean isNegative = ((number < 0.0) || (number == 0.0 && 1/number < 0.0)) ^ (multiplier < 0);

        if (multiplier != 1) {
            number *= multiplier;
        }

        if (Double.isInfinite(number)) {
            if (isNegative) {
                append(result, negativePrefix, delegate,
                       getNegativePrefixFieldPositions(), Field.SIGN);
            } else {
                append(result, positivePrefix, delegate,
                       getPositivePrefixFieldPositions(), Field.SIGN);
            }

            int iFieldStart = result.length();
            result.append(symbols.getInfinity());
            delegate.formatted(INTEGER_FIELD, Field.INTEGER, Field.INTEGER,
                               iFieldStart, result.length(), result);

            if (isNegative) {
                append(result, negativeSuffix, delegate,
                       getNegativeSuffixFieldPositions(), Field.SIGN);
            } else {
                append(result, positiveSuffix, delegate,
                       getPositiveSuffixFieldPositions(), Field.SIGN);
            }

            return result;
        }

        if (isNegative) {
            number = -number;
        }

        // 此时我们保证得到一个非负有限数字。
        assert(number >= 0 && !Double.isInfinite(number));

        synchronized(digitList) {
            int maxIntDigits = super.getMaximumIntegerDigits();
            int minIntDigits = super.getMinimumIntegerDigits();
            int maxFraDigits = super.getMaximumFractionDigits();
            int minFraDigits = super.getMinimumFractionDigits();

            digitList.set(isNegative, number, useExponentialNotation ?
                          maxIntDigits + maxFraDigits : maxFraDigits,
                          !useExponentialNotation);
            return subformat(result, delegate, isNegative, false,
                       maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    /**
     * 将 long 格式化以生成字符串。
     * @param number    要格式化的 long
     * @param result    文本将被追加到的位置
     * @param fieldPosition    输入：对齐字段，如果需要的话。
     * 输出：对齐字段的偏移量。
     * @exception       如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY，则抛出 ArithmeticException
     * @return 格式化的数字字符串
     * @see java.text.FieldPosition
     */
    @Override
    public StringBuffer format(long number, StringBuffer result,
                               FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);

        return format(number, result, fieldPosition.getFieldDelegate());
    }

    /**
     * 将 long 格式化以生成字符串。
     * @param number    要格式化的 long
     * @param result    文本将被追加到的位置
     * @param delegate 通知子字段的位置
     * @return 格式化的数字字符串
     * @exception        如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY，则抛出 ArithmeticException
     * @see java.text.FieldPosition
     */
    private StringBuffer format(long number, StringBuffer result,
                               FieldDelegate delegate) {
        boolean isNegative = (number < 0);
        if (isNegative) {
            number = -number;
        }

        // 一般来说，long 值总是表示实际的有限数字，因此我们不必检查 +/- Infinity 或 NaN。但是，
        // 有一种情况需要小心：乘数可能会将接近 MIN_VALUE 或 MAX_VALUE 的数字推到合法范围之外。我们在乘以之前检查这一点，如果发生这种情况，我们使用 BigInteger。
        boolean useBigInteger = false;
        if (number < 0) { // 这只能在 number == Long.MIN_VALUE 时发生。
            if (multiplier != 0) {
                useBigInteger = true;
            }
        } else if (multiplier != 1 && multiplier != 0) {
            long cutoff = Long.MAX_VALUE / multiplier;
            if (cutoff < 0) {
                cutoff = -cutoff;
            }
            useBigInteger = (number > cutoff);
        }


                    if (useBigInteger) {
            if (isNegative) {
                number = -number;
            }
            BigInteger bigIntegerValue = BigInteger.valueOf(number);
            return format(bigIntegerValue, result, delegate, true);
        }

        number *= multiplier;
        if (number == 0) {
            isNegative = false;
        } else {
            if (multiplier < 0) {
                number = -number;
                isNegative = !isNegative;
            }
        }

        synchronized(digitList) {
            int maxIntDigits = super.getMaximumIntegerDigits();
            int minIntDigits = super.getMinimumIntegerDigits();
            int maxFraDigits = super.getMaximumFractionDigits();
            int minFraDigits = super.getMinimumFractionDigits();

            digitList.set(isNegative, number,
                     useExponentialNotation ? maxIntDigits + maxFraDigits : 0);

            return subformat(result, delegate, isNegative, true,
                       maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    /**
     * 格式化 BigDecimal 以生成字符串。
     * @param number    要格式化的 BigDecimal
     * @param result    文本将被追加到的地方
     * @param fieldPosition    输入：如果需要，对齐字段。
     * 输出：对齐字段的偏移量。
     * @return 格式化的数字字符串
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
     */
    private StringBuffer format(BigDecimal number, StringBuffer result,
                                FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        return format(number, result, fieldPosition.getFieldDelegate());
    }

    /**
     * 格式化 BigDecimal 以生成字符串。
     * @param number    要格式化的 BigDecimal
     * @param result    文本将被追加到的地方
     * @param delegate 通知子字段的位置
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @return 格式化的数字字符串
     */
    private StringBuffer format(BigDecimal number, StringBuffer result,
                                FieldDelegate delegate) {
        if (multiplier != 1) {
            number = number.multiply(getBigDecimalMultiplier());
        }
        boolean isNegative = number.signum() == -1;
        if (isNegative) {
            number = number.negate();
        }

        synchronized(digitList) {
            int maxIntDigits = getMaximumIntegerDigits();
            int minIntDigits = getMinimumIntegerDigits();
            int maxFraDigits = getMaximumFractionDigits();
            int minFraDigits = getMinimumFractionDigits();
            int maximumDigits = maxIntDigits + maxFraDigits;

            digitList.set(isNegative, number, useExponentialNotation ?
                ((maximumDigits < 0) ? Integer.MAX_VALUE : maximumDigits) :
                maxFraDigits, !useExponentialNotation);

            return subformat(result, delegate, isNegative, false,
                maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    /**
     * 格式化 BigInteger 以生成字符串。
     * @param number    要格式化的 BigInteger
     * @param result    文本将被追加到的地方
     * @param fieldPosition    输入：如果需要，对齐字段。
     * 输出：对齐字段的偏移量。
     * @return 格式化的数字字符串
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
     */
    private StringBuffer format(BigInteger number, StringBuffer result,
                               FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);

        return format(number, result, fieldPosition.getFieldDelegate(), false);
    }

    /**
     * 格式化 BigInteger 以生成字符串。
     * @param number    要格式化的 BigInteger
     * @param result    文本将被追加到的地方
     * @param delegate 通知子字段的位置
     * @return 格式化的数字字符串
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
     */
    private StringBuffer format(BigInteger number, StringBuffer result,
                               FieldDelegate delegate, boolean formatLong) {
        if (multiplier != 1) {
            number = number.multiply(getBigIntegerMultiplier());
        }
        boolean isNegative = number.signum() == -1;
        if (isNegative) {
            number = number.negate();
        }

        synchronized(digitList) {
            int maxIntDigits, minIntDigits, maxFraDigits, minFraDigits, maximumDigits;
            if (formatLong) {
                maxIntDigits = super.getMaximumIntegerDigits();
                minIntDigits = super.getMinimumIntegerDigits();
                maxFraDigits = super.getMaximumFractionDigits();
                minFraDigits = super.getMinimumFractionDigits();
                maximumDigits = maxIntDigits + maxFraDigits;
            } else {
                maxIntDigits = getMaximumIntegerDigits();
                minIntDigits = getMinimumIntegerDigits();
                maxFraDigits = getMaximumFractionDigits();
                minFraDigits = getMinimumFractionDigits();
                maximumDigits = maxIntDigits + maxFraDigits;
                if (maximumDigits < 0) {
                    maximumDigits = Integer.MAX_VALUE;
                }
            }

            digitList.set(isNegative, number,
                          useExponentialNotation ? maximumDigits : 0);

            return subformat(result, delegate, isNegative, true,
                maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }


                /**
     * 格式化一个对象，生成一个 <code>AttributedCharacterIterator</code>。
     * 可以使用返回的 <code>AttributedCharacterIterator</code>
     * 构建结果字符串，以及确定结果字符串的信息。
     * <p>
     * AttributedCharacterIterator 的每个属性键的类型为
     * <code>NumberFormat.Field</code>，属性值与属性键相同。
     *
     * @exception NullPointerException 如果 obj 为 null。
     * @exception IllegalArgumentException 当 Format 无法格式化给定对象时。
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY 时。
     * @param obj 要格式化的对象
     * @return 描述格式化值的 AttributedCharacterIterator。
     * @since 1.4
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        CharacterIteratorFieldDelegate delegate =
                         new CharacterIteratorFieldDelegate();
        StringBuffer sb = new StringBuffer();

        if (obj instanceof Double || obj instanceof Float) {
            format(((Number)obj).doubleValue(), sb, delegate);
        } else if (obj instanceof Long || obj instanceof Integer ||
                   obj instanceof Short || obj instanceof Byte ||
                   obj instanceof AtomicInteger || obj instanceof AtomicLong) {
            format(((Number)obj).longValue(), sb, delegate);
        } else if (obj instanceof BigDecimal) {
            format((BigDecimal)obj, sb, delegate);
        } else if (obj instanceof BigInteger) {
            format((BigInteger)obj, sb, delegate, false);
        } else if (obj == null) {
            throw new NullPointerException(
                "formatToCharacterIterator 必须传递非空对象");
        } else {
            throw new IllegalArgumentException(
                "无法将给定对象格式化为数字");
        }
        return delegate.getIterator(sb.toString());
    }

    // ==== Begin fast-path formating logic for double =========================

    /* 快速路径格式化将在满足一系列条件时（参见 checkAndSetFastPathStatus()）用于 format(double ...) 方法：
     * - 仅当实例属性满足预定义条件时。
     * - 要格式化的 double 的绝对值 <= Integer.MAX_VALUE。
     *
     * 基本方法是将 double 值的二进制到十进制转换分为两个阶段：
     * * double 的整数部分的转换。
     * * double 的小数部分的转换（限制为两到三位数字）。
     *
     * 隔离和转换 double 的整数部分是直接的。小数部分的转换更为微妙，依赖于 double 到特定十进制精度的舍入属性。使用 BigDecimal 的术语，当 double 值的大小小于 Integer.MAX_VALUE 且舍入模式为最近偶数且目标格式有两到三位 *scale*（小数点后的数字）时，应用此快速路径算法。
     *
     * 在最近偶数的舍入策略下，返回的结果是目标格式（在这种情况下为十进制）中与输入值（在这种情况下为二进制）的确切数值最接近的数字字符串。如果两个目标格式数字距离相等，则返回最后一个数字为偶数的那个。为了计算这样一个正确舍入的值，需要咨询返回数字位置之外的一些信息。
     *
     * 通常，返回数字位置之外需要一个保护位、一个舍入位和一个粘性 *位*。如果丢弃的输入部分足够大，返回的数字字符串将递增。在最近偶数的舍入中，这个递增的阈值发生在数字之间的一半附近。粘性位记录新格式中确切输入值是否有任何剩余的尾随数字；粘性位仅在接近一半的舍入情况下被咨询。
     *
     * 给定数字和位值的计算，舍入就变成了一个查找表问题。对于十进制，偶数/奇数情况如下所示：
     *
     * 最后一位   舍入位   粘性位
     * 6          5       0      => 6   // 恰好一半，返回偶数位。
     * 6          5       1      => 7   // 稍微超过一半，向上舍入。
     * 7          5       0      => 8   // 恰好一半，向上舍入到偶数。
     * 7          5       1      => 8   // 稍微超过一半，向上舍入。
     * 其他偶数和奇数最后返回位的条目类似。
     *
     * 然而，小于 0.5 的十进制负 5 的幂 *不能* 精确表示为二进制分数。特别是，0.005（两位小数的舍入限制）和 0.0005（三位小数的舍入限制）不能表示。因此，对于接近这些情况的输入值，粘性位已知被设置，这将舍入逻辑简化为：
     *
     * 最后一位   舍入位   粘性位
     * 6          5       1      => 7   // 稍微超过一半，向上舍入。
     * 7          5       1      => 8   // 稍微超过一半，向上舍入。
     *
     * 换句话说，如果舍入位是 5，粘性位已知被设置。如果舍入位不是 5，粘性位不相关。因此，关于是否递增目标 *十进制* 值的一些逻辑可以基于对 *二进制* 输入数字的二进制计算的测试。
     */

    /**
     * 检查此实例使用快速路径的有效性。如果快速路径对此实例有效，将快速路径状态设置为 true 并根据需要初始化快速路径实用字段。
     *
     * 该方法应很少调用，否则将破坏快速路径性能。这意味着避免频繁更改实例的属性，因为对于大多数属性，每次更改发生时，下次格式化调用时都需要调用此方法。
     *
     * 快速路径规则：
     *  与默认的 DecimalFormat 实例化情况类似。
     *  更精确地说：
     *  - HALF_EVEN 舍入模式，
     *  - isGroupingUsed() 为 true，
     *  - 分组大小为 3，
     *  - 倍数为 1，
     *  - 小数点分隔符不是必需的，
     *  - 不使用指数表示法，
     *  - 最小整数位数恰好为 1，最大整数位数至少为 10
     *  - 对于小数位数，使用默认情况中的确切值：
     *     货币：min = max = 2。
     *     十进制：min = 0, max = 3。
     *
     */
    private boolean checkAndSetFastPathStatus() {


                    boolean fastPathWasOn = isFastPath;

        if ((roundingMode == RoundingMode.HALF_EVEN) &&
            (isGroupingUsed()) &&
            (groupingSize == 3) &&
            (multiplier == 1) &&
            (!decimalSeparatorAlwaysShown) &&
            (!useExponentialNotation)) {

            // 快速路径算法对 minimumIntegerDigits 和 maximumIntegerDigits 有部分硬编码。
            isFastPath = ((minimumIntegerDigits == 1) &&
                          (maximumIntegerDigits >= 10));

            // 快速路径算法对 minimumFractionDigits 和 maximumFractionDigits 有硬编码。
            if (isFastPath) {
                if (isCurrencyFormat) {
                    if ((minimumFractionDigits != 2) ||
                        (maximumFractionDigits != 2))
                        isFastPath = false;
                } else if ((minimumFractionDigits != 0) ||
                           (maximumFractionDigits != 3))
                    isFastPath = false;
            }
        } else
            isFastPath = false;

        resetFastPathData(fastPathWasOn);
        fastPathCheckNeeded = false;

        /*
         * 在成功检查快速路径条件并设置快速路径数据后返回 true。返回值用于 fastFormat() 方法决定是否调用 resetFastPathData
         * 方法重新初始化快速路径数据，或者是否已经在本方法中初始化。
         */
        return true;
    }

    private void resetFastPathData(boolean fastPathWasOn) {
        // 由于某些实例属性可能在仍然符合快速路径的情况下发生变化，因此无论如何都需要重新初始化 fastPathData。
        if (isFastPath) {
            // 如果尚未创建 fastPathData，则需要实例化它。
            if (fastPathData == null) {
                fastPathData = new FastPathData();
            }

            // 设置格式化时使用的特定于区域设置的常量。'0' 是我们表示零的默认形式。
            fastPathData.zeroDelta = symbols.getZeroDigit() - '0';
            fastPathData.groupingChar = symbols.getGroupingSeparator();

            // 设置与货币/小数模式相关的分数常量。
            fastPathData.fractionalMaxIntBound = (isCurrencyFormat)
                    ? 99 : 999;
            fastPathData.fractionalScaleFactor = (isCurrencyFormat)
                    ? 100.0d : 1000.0d;

            // 记录是否需要添加前缀或后缀
            fastPathData.positiveAffixesRequired
                    = !positivePrefix.isEmpty() || !positiveSuffix.isEmpty();
            fastPathData.negativeAffixesRequired
                    = !negativePrefix.isEmpty() || !negativeSuffix.isEmpty();

            // 创建一个用于结果的最大可能大小的缓存字符容器。
            int maxNbIntegralDigits = 10;
            int maxNbGroups = 3;
            int containerSize
                    = Math.max(positivePrefix.length(), negativePrefix.length())
                    + maxNbIntegralDigits + maxNbGroups + 1
                    + maximumFractionDigits
                    + Math.max(positiveSuffix.length(), negativeSuffix.length());

            fastPathData.fastPathContainer = new char[containerSize];

            // 设置前缀和后缀字符数组常量。
            fastPathData.charsPositiveSuffix = positiveSuffix.toCharArray();
            fastPathData.charsNegativeSuffix = negativeSuffix.toCharArray();
            fastPathData.charsPositivePrefix = positivePrefix.toCharArray();
            fastPathData.charsNegativePrefix = negativePrefix.toCharArray();

            // 设置整数和小数位的固定索引位置。
            // 在缓存结果容器中设置小数点。
            int longestPrefixLength
                    = Math.max(positivePrefix.length(),
                            negativePrefix.length());
            int decimalPointIndex
                    = maxNbIntegralDigits + maxNbGroups + longestPrefixLength;

            fastPathData.integralLastIndex = decimalPointIndex - 1;
            fastPathData.fractionalFirstIndex = decimalPointIndex + 1;
            fastPathData.fastPathContainer[decimalPointIndex]
                    = isCurrencyFormat
                            ? symbols.getMonetaryDecimalSeparator()
                            : symbols.getDecimalSeparator();

        } else if (fastPathWasOn) {
            // 之前的状态是快速路径，但现在不再是。
            // 重置缓存的数组常量。
            fastPathData.fastPathContainer = null;
            fastPathData.charsPositiveSuffix = null;
            fastPathData.charsNegativeSuffix = null;
            fastPathData.charsPositivePrefix = null;
            fastPathData.charsNegativePrefix = null;
        }
    }

    /**
     * 如果需要对 {@code scaledFractionalPartAsInt} 进行四舍五入，则返回 true，否则返回 false。
     *
     * 这是一个实用方法，用于在缩放小数点（货币情况下为 2 位，小数情况下为 3 位）后的近似小数部分恰好为 0.5d 时，对传递的小数值
     * {@code fractionalPart} 进行正确的半偶舍入决策。这是通过精确计算 {@code fractionalPart} 浮点值来完成的。
     *
     * 该方法仅应由私有的 {@code fastDoubleFormat} 方法调用。
     *
     * 用于精确计算的算法包括：
     *
     * <b><i>FastTwoSum</i></b> 算法，由 T.J.Dekker 提出，描述在论文 "<i>A  Floating-Point   Technique  for  Extending  the  Available
     * Precision</i>" 和 "<i>Adaptive  Precision Floating-Point Arithmetic and Fast Robust Geometric Predicates</i>" 中。
     *
     * <b><i>Sum2S</i></b> 级联求和的修改版本，描述在 "<i>Accurate Sum and Dot Product</i>" 中。正如 Ogita 在论文中所说，这是
     * Kahan-Babuska 求和算法的等价物，因为我们在求和之前按大小对项进行排序。因此，我们可以使用 <i>FastTwoSum</i> 算法，而不是更昂贵的
     * Knuth 的 <i>TwoSum</i>。
     *
     * 我们这样做是为了避免使用更昂贵的精确 "<i>TwoProduct</i>" 算法，如 Shewchuk 的上述论文中所述。请参阅代码中的注释。
     *
     * @param  fractionalPart 我们需要进行舍入决策的小数值。
     * @param scaledFractionalPartAsInt 缩放小数值的整数部分。
     *
     * @return 需要采取的半偶舍入决策。
     */
    private boolean exactRoundUp(double fractionalPart,
                                 int scaledFractionalPartAsInt) {


                    /* exactRoundUp() 方法仅由 fastDoubleFormat() 调用。
         * 传递的参数预期满足的前置条件是：
         * scaledFractionalPartAsInt ==
         *     (int) (fractionalPart * fastPathData.fractionalScaleFactor)。
         * 这由 fastDoubleFormat() 代码确保。
         */

        /* 我们首先计算 fastDoubleFormat() 在缩放的小数部分上产生的舍入误差。我们通过
         * 对传递的小数部分进行精确计算来完成这一点。然后根据舍入误差做出舍入决定。
         */

        /* ---- TwoProduct(fractionalPart, 缩放因子（即 1000.0d 或 100.0d））。
         *
         * 以下是传递的小数部分与缩放因子进行精确“TwoProduct”计算的优化版本，使用
         * Ogita 的 Sum2S 级联求和，通过使用 FastTwoSum（更快）而不是 Knuth 的 TwoSum
         * 适应为 Kahan-Babuska 等效版本。
         *
         * 我们可以这样做，因为我们按从小到大的顺序进行求和，因此可以使用 FastTwoSum
         * 而不引入任何额外的误差。
         *
         * “TwoProduct” 精确计算需要 17 个浮点运算。我们用 FastTwoSum 计算的级联求和
         * 替换它，每个计算涉及一个 2 的幂的精确乘法。
         *
         * 这样做比使用传统的“TwoProduct”节省了 4 次乘法和 1 次加法。
         *
         * 缩放因子要么是 100（货币情况），要么是 1000（十进制情况）。
         * - 当为 1000 时，我们用 (1024 - 16 - 8) = 1000 替换它。
         * - 当为 100 时，我们用 (128 - 32 + 4) = 100 替换它。
         * 每次乘以 2 的幂（1024, 128, 32, 16, 8, 4）都是精确的。
         *
         */
        double approxMax;    // 始终为正。
        double approxMedium; // 始终为负。
        double approxMin;

        double fastTwoSumApproximation = 0.0d;
        double fastTwoSumRoundOff = 0.0d;
        double bVirtual = 0.0d;

        if (isCurrencyFormat) {
            // 缩放因子为 100 = 128 - 32 + 4。
            // 乘以 2**n 是移位操作。没有舍入误差。没有误差。
            approxMax    = fractionalPart * 128.00d;
            approxMedium = - (fractionalPart * 32.00d);
            approxMin    = fractionalPart * 4.00d;
        } else {
            // 缩放因子为 1000 = 1024 - 16 - 8。
            // 乘以 2**n 是移位操作。没有舍入误差。没有误差。
            approxMax    = fractionalPart * 1024.00d;
            approxMedium = - (fractionalPart * 16.00d);
            approxMin    = - (fractionalPart * 8.00d);
        }

        // Shewchuk/Dekker 的 FastTwoSum(approxMedium, approxMin)。
        assert(-approxMedium >= Math.abs(approxMin));
        fastTwoSumApproximation = approxMedium + approxMin;
        bVirtual = fastTwoSumApproximation - approxMedium;
        fastTwoSumRoundOff = approxMin - bVirtual;
        double approxS1 = fastTwoSumApproximation;
        double roundoffS1 = fastTwoSumRoundOff;

        // Shewchuk/Dekker 的 FastTwoSum(approxMax, approxS1)。
        assert(approxMax >= Math.abs(approxS1));
        fastTwoSumApproximation = approxMax + approxS1;
        bVirtual = fastTwoSumApproximation - approxMax;
        fastTwoSumRoundOff = approxS1 - bVirtual;
        double roundoff1000 = fastTwoSumRoundOff;
        double approx1000 = fastTwoSumApproximation;
        double roundoffTotal = roundoffS1 + roundoff1000;

        // Shewchuk/Dekker 的 FastTwoSum(approx1000, roundoffTotal)。
        assert(approx1000 >= Math.abs(roundoffTotal));
        fastTwoSumApproximation = approx1000 + roundoffTotal;
        bVirtual = fastTwoSumApproximation - approx1000;

        // 现在我们得到了缩放小数部分的舍入误差。
        double scaledFractionalRoundoff = roundoffTotal - bVirtual;

        // ---- TwoProduct(fractionalPart, 缩放因子（即 1000.0d 或 100.0d）) 结束。

        /* ---- 做出舍入决定
         *
         * 我们根据舍入误差和半偶舍入规则做出舍入决定。
         *
         * 上面的 TwoProduct 给出了缩放小数部分的精确舍入误差，我们知道这个近似值
         * 恰好是 0.5d，因为这已经由调用者（fastDoubleFormat）测试过了。
         *
         * 决定首先来自计算的精确舍入误差的符号。
         * - 由于是精确舍入误差，它不能在缩放小数部分小于 0.5d 时为正，也不能在
         *   缩放小数部分大于 0.5d 时为负。这留下了以下 3 种情况。
         * - 正数，因此缩放小数部分 == 0.500....0fff ==> 向上舍入。
         * - 负数，因此缩放小数部分 == 0.499....9fff ==> 不向上舍入。
         * - 为零，因此缩放小数部分 == 0.5 ==> 应用半偶舍入：
         *    仅当缩放小数部分的整数部分为奇数时才向上舍入。
         *
         */
        if (scaledFractionalRoundoff > 0.0) {
            return true;
        } else if (scaledFractionalRoundoff < 0.0) {
            return false;
        } else if ((scaledFractionalPartAsInt & 1) != 0) {
            return true;
        }

        return false;

        // ---- 做出舍入决定结束
    }

    /**
     * 从传递的 {@code number} 中收集整数位，同时根据需要设置分组字符。相应地更新 {@code firstUsedIndex}。
     *
     * 从 {@code backwardIndex} 位置（包括）开始向下循环。
     *
     * @param number  从中收集数字的 int 值。
     * @param digitsBuffer 存储数字和分组字符的 char 数组容器。
     * @param backwardIndex 从 digitsBuffer 中开始存储数字的位置。
     *
     */
    private void collectIntegralDigits(int number,
                                       char[] digitsBuffer,
                                       int backwardIndex) {
        int index = backwardIndex;
        int q;
        int r;
        while (number > 999) {
            // 每次迭代生成 3 位数字。
            q = number / 1000;
            r = number - (q << 10) + (q << 4) + (q << 3); // -1024 +16 +8 = 1000。
            number = q;


                        digitsBuffer[index--] = DigitArrays.DigitOnes1000[r];
            digitsBuffer[index--] = DigitArrays.DigitTens1000[r];
            digitsBuffer[index--] = DigitArrays.DigitHundreds1000[r];
            digitsBuffer[index--] = fastPathData.groupingChar;
        }

        // 收集最后3个或更少的数字。
        digitsBuffer[index] = DigitArrays.DigitOnes1000[number];
        if (number > 9) {
            digitsBuffer[--index]  = DigitArrays.DigitTens1000[number];
            if (number > 99)
                digitsBuffer[--index]   = DigitArrays.DigitHundreds1000[number];
        }

        fastPathData.firstUsedIndex = index;
    }

    /**
     * 从传递的 {@code number} 中收集2位（货币）或3位（小数）的分数位，从 {@code startIndex} 位置开始（包含）。
     * 这里没有设置标点符号（没有分组字符）。
     * 相应地更新 {@code fastPathData.lastFreeIndex}。
     *
     *
     * @param number  从中收集数字的int值。
     * @param digitsBuffer 存储数字的char数组容器。
     * @param startIndex 我们开始在 digitsBuffer 中存储数字的位置。
     *
     */
    private void collectFractionalDigits(int number,
                                         char[] digitsBuffer,
                                         int startIndex) {
        int index = startIndex;

        char digitOnes = DigitArrays.DigitOnes1000[number];
        char digitTens = DigitArrays.DigitTens1000[number];

        if (isCurrencyFormat) {
            // 货币情况。始终收集分数位。
            digitsBuffer[index++] = digitTens;
            digitsBuffer[index++] = digitOnes;
        } else if (number != 0) {
            // 小数情况。百位始终会被收集
            digitsBuffer[index++] = DigitArrays.DigitHundreds1000[number];

            // 结尾的零不会被收集。
            if (digitOnes != '0') {
                digitsBuffer[index++] = digitTens;
                digitsBuffer[index++] = digitOnes;
            } else if (digitTens != '0')
                digitsBuffer[index++] = digitTens;

        } else
            // 这是小数模式，分数部分为零。
            // 我们必须从结果中移除小数点。
            index--;

        fastPathData.lastFreeIndex = index;
    }

    /**
     * 内部工具。
     * 将传递的 {@code prefix} 和 {@code suffix} 添加到 {@code container} 中。
     *
     * @param container  要前置/后缀的char数组容器。
     * @param prefix     作为前缀前置的char序列。
     * @param suffix     作为后缀后缀的char序列。
     *
     */
    //    private void addAffixes(boolean isNegative, char[] container) {
    private void addAffixes(char[] container, char[] prefix, char[] suffix) {

        // 仅在需要时（前缀长度 > 0）添加前缀。
        int pl = prefix.length;
        int sl = suffix.length;
        if (pl != 0) prependPrefix(prefix, pl, container);
        if (sl != 0) appendSuffix(suffix, sl, container);

    }

    /**
     * 将传递的 {@code prefix} 字符前置到给定的结果 {@code container} 中。相应地更新 {@code fastPathData.firstUsedIndex}。
     *
     * @param prefix 要前置到结果的前缀字符。
     * @param len 要前置的字符数。
     * @param container 要前置前缀的char数组容器
     */
    private void prependPrefix(char[] prefix,
                               int len,
                               char[] container) {

        fastPathData.firstUsedIndex -= len;
        int startIndex = fastPathData.firstUsedIndex;

        // 如果要前置的前缀只有1个字符长，直接分配这个字符。
        // 如果前缀小于或等于4，我们使用一种专门的算法，该算法比 System.arraycopy 更快。
        // 如果多于4，我们使用 System.arraycopy。
        if (len == 1)
            container[startIndex] = prefix[0];
        else if (len <= 4) {
            int dstLower = startIndex;
            int dstUpper = dstLower + len - 1;
            int srcUpper = len - 1;
            container[dstLower] = prefix[0];
            container[dstUpper] = prefix[srcUpper];

            if (len > 2)
                container[++dstLower] = prefix[1];
            if (len == 4)
                container[--dstUpper] = prefix[2];
        } else
            System.arraycopy(prefix, 0, container, startIndex, len);
    }

    /**
     * 将传递的 {@code suffix} 字符后缀到给定的结果 {@code container} 中。相应地更新 {@code fastPathData.lastFreeIndex}。
     *
     * @param suffix 要后缀到结果的后缀字符。
     * @param len 要后缀的字符数。
     * @param container 要后缀后缀的char数组容器
     */
    private void appendSuffix(char[] suffix,
                              int len,
                              char[] container) {

        int startIndex = fastPathData.lastFreeIndex;

        // 如果要后缀的后缀只有1个字符长，直接分配这个字符。
        // 如果后缀小于或等于4，我们使用一种专门的算法，该算法比 System.arraycopy 更快。
        // 如果多于4，我们使用 System.arraycopy。
        if (len == 1)
            container[startIndex] = suffix[0];
        else if (len <= 4) {
            int dstLower = startIndex;
            int dstUpper = dstLower + len - 1;
            int srcUpper = len - 1;
            container[dstLower] = suffix[0];
            container[dstUpper] = suffix[srcUpper];

            if (len > 2)
                container[++dstLower] = suffix[1];
            if (len == 4)
                container[--dstUpper] = suffix[2];
        } else
            System.arraycopy(suffix, 0, container, startIndex, len);

        fastPathData.lastFreeIndex += len;
    }

    /**
     * 将 {@code digitsBuffer} 中的数字字符转换为当前区域设置。
     *
     * 必须在添加前缀后调用，因为我们引用了 {@code fastPathData.firstUsedIndex} 和 {@code fastPathData.lastFreeIndex}，
     * 并且不支持前缀（出于速度考虑）。
     *
     * 我们从 {@code fastPathData} 中最后使用的索引开始向后循环。
     *
     * @param digitsBuffer 存储数字的char数组容器。
     */
    private void localizeDigits(char[] digitsBuffer) {


                    // 我们将仅使用 groupingSize 定位数字，
        // 并考虑小数部分。

        // 首先考虑小数部分。
        int digitsCounter =
            fastPathData.lastFreeIndex - fastPathData.fractionalFirstIndex;

        // 当没有小数位时的情况。
        if (digitsCounter < 0)
            digitsCounter = groupingSize;

        // 仅剩余的数字需要定位。
        for (int cursor = fastPathData.lastFreeIndex - 1;
             cursor >= fastPathData.firstUsedIndex;
             cursor--) {
            if (digitsCounter != 0) {
                // 这是一个数字字符，我们需要对其进行定位。
                digitsBuffer[cursor] += fastPathData.zeroDelta;
                digitsCounter--;
            } else {
                // 小数分隔符或分组字符。仅重新初始化计数器。
                digitsCounter = groupingSize;
            }
        }
    }

    /**
     * 这是快速路径格式化算法的主要入口点。
     *
     * 在这一点上，我们确信已经满足了运行它的预期条件。
     * 该算法构建格式化的结果，并将其放入专用的
     * {@code fastPathData.fastPathContainer} 中。
     *
     * @param d 要格式化的 double 值。
     * @param negative 指定 {@code d} 是否为负数的标志。
     */
    private void fastDoubleFormat(double d,
                                  boolean negative) {

        char[] container = fastPathData.fastPathContainer;

        /*
         * 该算法的原理是：
         * - 将传递的 double 分解为其整数部分和小数部分
         *    并将其转换为整数。
         * - 然后根据半偶舍入规则决定是否需要进行舍入，首先使用近似缩放的小数部分。
         * - 对于困难的情况（近似缩放的小数部分正好为 0.5d），我们通过调用
         *    exactRoundUp 实用方法来细化舍入决策，该方法既计算近似值的精确舍入，
         *    又做出正确的舍入决策。
         * - 如果需要，我们对小数部分进行舍入，如果遇到缩放小数部分的“全九”情况，
         *    可能会将舍入传播到整数部分。
         * - 然后我们从结果的整数部分和小数部分收集数字，并即时设置所需的分组字符。
         * - 然后根据需要对收集的数字进行本地化，
         * - 最后，如果需要，添加前缀/后缀。
         */

        // d 的精确整数部分。
        int integralPartAsInt = (int) d;

        // d 的精确小数部分（因为我们减去了它的整数部分）。
        double exactFractionalPart = d - (double) integralPartAsInt;

        // d 的近似缩放小数部分（由于乘法）。
        double scaledFractional =
            exactFractionalPart * fastPathData.fractionalScaleFactor;

        // 上述缩放小数部分的精确整数部分。
        int fractionalPartAsInt = (int) scaledFractional;

        // 上述缩放小数部分的精确小数部分。
        scaledFractional = scaledFractional - (double) fractionalPartAsInt;

        // 只有当 scaledFractional 恰好为 0.5d 时，我们才需要进行精确计算并做出细粒度的舍入决策，
        // 因为上述近似结果可能导致错误的决策。
        // 否则，与 0.5d 进行比较（严格大于或小于）是可以的。
        boolean roundItUp = false;
        if (scaledFractional >= 0.5d) {
            if (scaledFractional == 0.5d)
                // 舍入需要细粒度的决策。
                roundItUp = exactRoundUp(exactFractionalPart, fractionalPartAsInt);
            else
                roundItUp = true;

            if (roundItUp) {
                // 对小数部分（必要时也对整数部分）进行舍入。
                if (fractionalPartAsInt < fastPathData.fractionalMaxIntBound) {
                    fractionalPartAsInt++;
                } else {
                    // 由于“全九”情况，将舍入传播到整数部分。
                    fractionalPartAsInt = 0;
                    integralPartAsInt++;
                }
            }
        }

        // 收集数字。
        collectFractionalDigits(fractionalPartAsInt, container,
                                fastPathData.fractionalFirstIndex);
        collectIntegralDigits(integralPartAsInt, container,
                              fastPathData.integralLastIndex);

        // 本地化数字。
        if (fastPathData.zeroDelta != 0)
            localizeDigits(container);

        // 添加前缀和后缀。
        if (negative) {
            if (fastPathData.negativeAffixesRequired)
                addAffixes(container,
                           fastPathData.charsNegativePrefix,
                           fastPathData.charsNegativeSuffix);
        } else if (fastPathData.positiveAffixesRequired)
            addAffixes(container,
                       fastPathData.charsPositivePrefix,
                       fastPathData.charsPositiveSuffix);
    }

    /**
     * 由 NumberFormat 或 format(double, ...) 公共方法调用的快速路径格式化(double)的快捷方式。
     *
     * 如果实例可以应用快速路径且传递的 double 不是 NaN 或
     * 无穷大，并且在整数范围内，我们在必要时将 {@code d} 更改为正数后调用 {@code fastDoubleFormat}。
     *
     * 否则，按照约定返回 null，因为快速路径无法执行。
     *
     * @param d 要格式化的 double 值
     *
     * @return 格式化后的结果，作为字符串。
     */
    String fastFormat(double d) {
        boolean isDataSet = false;
        // 如有必要，（重新）评估快速路径状态。
        if (fastPathCheckNeeded) {
            isDataSet = checkAndSetFastPathStatus();
        }

        if (!isFastPath )
            // DecimalFormat 实例不在快速路径状态。
            return null;


                    if (!Double.isFinite(d))
            // 不应为无穷大或 NaN 使用快速路径。
            return null;

        // 提取并记录 double 值的符号，可能将其更改为正数，然后再调用 fastDoubleFormat()。
        boolean negative = false;
        if (d < 0.0d) {
            negative = true;
            d = -d;
        } else if (d == 0.0d) {
            negative = (Math.copySign(1.0d, d) == -1.0d);
            d = +0.0d;
        }

        if (d > MAX_INT_AS_DOUBLE)
            // 过滤掉超出预期快速路径范围的值。
            return null;
        else {
            if (!isDataSet) {
                /*
                 * 如果快速路径数据未通过 checkAndSetFastPathStatus() 设置，并且满足
                 * 快速路径条件，则直接通过 resetFastPathData() 重置数据。
                 */
                resetFastPathData(isFastPath);
            }
            fastDoubleFormat(d, negative);

        }


        // 从更新的 fastPathContainer 返回新字符串。
        return new String(fastPathData.fastPathContainer,
                          fastPathData.firstUsedIndex,
                          fastPathData.lastFreeIndex - fastPathData.firstUsedIndex);

    }

    // ======== 结束 double 的快速路径格式化逻辑 =========================

    /**
     * 完成有限数字的格式化。进入时，digitList 必须用正确的数字填充。
     */
    private StringBuffer subformat(StringBuffer result, FieldDelegate delegate,
                                   boolean isNegative, boolean isInteger,
                                   int maxIntDigits, int minIntDigits,
                                   int maxFraDigits, int minFraDigits) {
        // 注意：这不再需要，因为 DigitList 会处理这个问题。
        //
        //  // 负指数表示小数点和第一个非零数字之间的前导零的数量，对于值 < 0.1（例如，对于 0.00123，-fExponent == 2）。
        //  // 如果这比最大小数位数多，则打印表示形式会发生下溢。我们在这里识别这种情况，并在这种情况中将 DigitList 表示形式设置为零。
        //
        //  if (-digitList.decimalAt >= getMaximumFractionDigits())
        //  {
        //      digitList.count = 0;
        //  }

        char zero = symbols.getZeroDigit();
        int zeroDelta = zero - '0'; // '0' 是 DigitList 中零的表示形式
        char grouping = symbols.getGroupingSeparator();
        char decimal = isCurrencyFormat ?
            symbols.getMonetaryDecimalSeparator() :
            symbols.getDecimalSeparator();

        /* 根据 bug 4147706，DecimalFormat 必须尊重格式化为零的数字的符号。这允许合理的计算并保留
         * 关系，例如 signum(1/x) = signum(x)，其中 x 是 +Infinity 或 -Infinity。在修复此问题之前，
         * 我们总是将零值格式化为正数。Liu 7/6/98。
         */
        if (digitList.isZero()) {
            digitList.decimalAt = 0; // 规范化
        }

        if (isNegative) {
            append(result, negativePrefix, delegate,
                   getNegativePrefixFieldPositions(), Field.SIGN);
        } else {
            append(result, positivePrefix, delegate,
                   getPositivePrefixFieldPositions(), Field.SIGN);
        }

        if (useExponentialNotation) {
            int iFieldStart = result.length();
            int iFieldEnd = -1;
            int fFieldStart = -1;

            // 最小整数位数在指数形式中通过调整指数来处理。例如，0.01234 有 3 个最小整数位数，表示为 "123.4E-4"。

            // 最大整数位数被解释为表示重复范围。这对于工程表示法很有用，其中指数限制为 3 的倍数。例如，0.01234 有 3 个最大整数位数，表示为 "12.34e-3"。
            // 如果最大整数位数 > 1 并且大于最小整数位数，则忽略最小整数位数。
            int exponent = digitList.decimalAt;
            int repeat = maxIntDigits;
            int minimumIntegerDigits = minIntDigits;
            if (repeat > 1 && repeat > minIntDigits) {
                // 定义了重复范围；如下调整。
                // 如果 repeat == 3，我们有 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
                // -3,-4,-5=>-6 等。这考虑到了这里的指数比我们预期的少一；
                // 它是格式 0.MMMMMx10^n 的。
                if (exponent >= 1) {
                    exponent = ((exponent - 1) / repeat) * repeat;
                } else {
                    // 整数除法向零舍入
                    exponent = ((exponent - repeat) / repeat) * repeat;
                }
                minimumIntegerDigits = 1;
            } else {
                // 未定义重复范围；使用最小整数位数。
                exponent -= minimumIntegerDigits;
            }

            // 现在输出最小数量的数字，如果有更多数字，则输出更多，直到最大数量的数字。我们在“整数”数字后放置小数点，
            // 这些数字是前 (decimalAt - exponent) 个数字。
            int minimumDigits = minIntDigits + minFraDigits;
            if (minimumDigits < 0) {    // 溢出？
                minimumDigits = Integer.MAX_VALUE;
            }

            // 如果数字为零，则特别处理整数位数，因为那时可能没有数字。
            int integerDigits = digitList.isZero() ? minimumIntegerDigits :
                    digitList.decimalAt - exponent;
            if (minimumDigits < integerDigits) {
                minimumDigits = integerDigits;
            }
            int totalDigits = digitList.count;
            if (minimumDigits > totalDigits) {
                totalDigits = minimumDigits;
            }
            boolean addedDecimalSeparator = false;


                        for (int i=0; i<totalDigits; ++i) {
                if (i == integerDigits) {
                    // 记录字段信息以供调用者使用。
                    iFieldEnd = result.length();

                    result.append(decimal);
                    addedDecimalSeparator = true;

                    // 记录字段信息以供调用者使用。
                    fFieldStart = result.length();
                }
                result.append((i < digitList.count) ?
                              (char)(digitList.digits[i] + zeroDelta) :
                              zero);
            }

            if (decimalSeparatorAlwaysShown && totalDigits == integerDigits) {
                // 记录字段信息以供调用者使用。
                iFieldEnd = result.length();

                result.append(decimal);
                addedDecimalSeparator = true;

                // 记录字段信息以供调用者使用。
                fFieldStart = result.length();
            }

            // 记录字段信息
            if (iFieldEnd == -1) {
                iFieldEnd = result.length();
            }
            delegate.formatted(INTEGER_FIELD, Field.INTEGER, Field.INTEGER,
                               iFieldStart, iFieldEnd, result);
            if (addedDecimalSeparator) {
                delegate.formatted(Field.DECIMAL_SEPARATOR,
                                   Field.DECIMAL_SEPARATOR,
                                   iFieldEnd, fFieldStart, result);
            }
            if (fFieldStart == -1) {
                fFieldStart = result.length();
            }
            delegate.formatted(FRACTION_FIELD, Field.FRACTION, Field.FRACTION,
                               fFieldStart, result.length(), result);

            // 指数使用模式指定的最小指数位数输出。指数位数没有最大限制，因为截断指数会导致不可接受的不准确。
            int fieldStart = result.length();

            result.append(symbols.getExponentSeparator());

            delegate.formatted(Field.EXPONENT_SYMBOL, Field.EXPONENT_SYMBOL,
                               fieldStart, result.length(), result);

            // 对于零值，我们将指数强制为零。我们必须在这里而不是更早地这样做，因为该值用于确定上面的整数位数。
            if (digitList.isZero()) {
                exponent = 0;
            }

            boolean negativeExponent = exponent < 0;
            if (negativeExponent) {
                exponent = -exponent;
                fieldStart = result.length();
                result.append(symbols.getMinusSign());
                delegate.formatted(Field.EXPONENT_SIGN, Field.EXPONENT_SIGN,
                                   fieldStart, result.length(), result);
            }
            digitList.set(negativeExponent, exponent);

            int eFieldStart = result.length();

            for (int i=digitList.decimalAt; i<minExponentDigits; ++i) {
                result.append(zero);
            }
            for (int i=0; i<digitList.decimalAt; ++i) {
                result.append((i < digitList.count) ?
                          (char)(digitList.digits[i] + zeroDelta) : zero);
            }
            delegate.formatted(Field.EXPONENT, Field.EXPONENT, eFieldStart,
                               result.length(), result);
        } else {
            int iFieldStart = result.length();

            // 输出整数部分。这里的 'count' 是我们将显示的整数位数的总数，包括满足 getMinimumIntegerDigits 所需的前导零和数字中的实际位数。
            int count = minIntDigits;
            int digitIndex = 0; // 指向 digitList.fDigits[] 的索引
            if (digitList.decimalAt > 0 && count < digitList.decimalAt) {
                count = digitList.decimalAt;
            }

            // 处理 getMaximumIntegerDigits() 小于实际整数位数的情况。如果是这样，我们输出最不重要的最大整数位数。例如，值 1997 以 2 位最大整数位数打印为 "97"。
            if (count > maxIntDigits) {
                count = maxIntDigits;
                digitIndex = digitList.decimalAt - count;
            }

            int sizeBeforeIntegerPart = result.length();
            for (int i=count-1; i>=0; --i) {
                if (i < digitList.decimalAt && digitIndex < digitList.count) {
                    // 输出实际数字
                    result.append((char)(digitList.digits[digitIndex++] + zeroDelta));
                } else {
                    // 输出前导零
                    result.append(zero);
                }

                // 必要时输出分组分隔符。但是不要在 i==0 时输出分组分隔符；这是整数部分的末尾。
                if (isGroupingUsed() && i>0 && (groupingSize != 0) &&
                    (i % groupingSize == 0)) {
                    int gStart = result.length();
                    result.append(grouping);
                    delegate.formatted(Field.GROUPING_SEPARATOR,
                                       Field.GROUPING_SEPARATOR, gStart,
                                       result.length(), result);
                }
            }

            // 确定是否有任何可打印的小数位数。如果我们用完了数字，那么就没有了。
            boolean fractionPresent = (minFraDigits > 0) ||
                (!isInteger && digitIndex < digitList.count);

            // 如果没有小数部分，并且我们没有打印任何整数位数，那么打印一个零。否则我们将不会打印任何数字，我们将无法解析这个字符串。
            if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
                result.append(zero);
            }


                        delegate.formatted(INTEGER_FIELD, Field.INTEGER, Field.INTEGER,
                               iFieldStart, result.length(), result);

            // 如果总是需要输出小数点，则输出小数点。
            int sStart = result.length();
            if (decimalSeparatorAlwaysShown || fractionPresent) {
                result.append(decimal);
            }

            if (sStart != result.length()) {
                delegate.formatted(Field.DECIMAL_SEPARATOR,
                                   Field.DECIMAL_SEPARATOR,
                                   sStart, result.length(), result);
            }
            int fFieldStart = result.length();

            for (int i=0; i < maxFraDigits; ++i) {
                // 这里是退出循环的地方。当输出的最大小数位数（在上面的 for 表达式中指定）时退出。
                // 当输出的最小位数并且满足以下任一条件时也停止：
                // 我们有一个整数，因此没有小数部分需要显示，或者我们已经没有有效位数。
                if (i >= minFraDigits &&
                    (isInteger || digitIndex >= digitList.count)) {
                    break;
                }

                // 输出前导小数零。这些零出现在小数点之后但在任何有效位数之前。这些零仅在 abs(被格式化的数字) < 1.0 时输出。
                if (-1-i > (digitList.decimalAt-1)) {
                    result.append(zero);
                    continue;
                }

                // 如果还有精度，则输出一个数字，否则输出一个零。我们不希望输出噪声数字。
                if (!isInteger && digitIndex < digitList.count) {
                    result.append((char)(digitList.digits[digitIndex++] + zeroDelta));
                } else {
                    result.append(zero);
                }
            }

            // 为调用者记录字段信息。
            delegate.formatted(FRACTION_FIELD, Field.FRACTION, Field.FRACTION,
                               fFieldStart, result.length(), result);
        }

        if (isNegative) {
            append(result, negativeSuffix, delegate,
                   getNegativeSuffixFieldPositions(), Field.SIGN);
        } else {
            append(result, positiveSuffix, delegate,
                   getPositiveSuffixFieldPositions(), Field.SIGN);
        }

        return result;
    }

    /**
     * 将字符串 <code>string</code> 追加到 <code>result</code>。
     * <code>delegate</code> 会被通知 <code>positions</code> 中的所有 <code>FieldPosition</code>。
     * <p>
     * 如果 <code>positions</code> 中的一个 <code>FieldPosition</code> 识别出一个 <code>SIGN</code> 属性，
     * 则将其映射到 <code>signAttribute</code>。这用于在必要时将 <code>SIGN</code> 属性映射到 <code>EXPONENT</code> 属性。
     * <p>
     * 这由 <code>subformat</code> 使用来添加前缀/后缀。
     */
    private void append(StringBuffer result, String string,
                        FieldDelegate delegate,
                        FieldPosition[] positions,
                        Format.Field signAttribute) {
        int start = result.length();

        if (!string.isEmpty()) {
            result.append(string);
            for (int counter = 0, max = positions.length; counter < max;
                 counter++) {
                FieldPosition fp = positions[counter];
                Format.Field attribute = fp.getFieldAttribute();

                if (attribute == Field.SIGN) {
                    attribute = signAttribute;
                }
                delegate.formatted(attribute, attribute,
                                   start + fp.getBeginIndex(),
                                   start + fp.getEndIndex(), result);
            }
        }
    }

    /**
     * 从字符串中解析文本以生成一个 <code>Number</code>。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为使用后的最后一个字符的索引（解析不一定使用到字符串的末尾的所有字符），并返回解析的数字。
     * 更新后的 <code>pos</code> 可用于指示下次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会改变，<code>pos</code> 的错误索引将设置为发生错误的字符的索引，并返回 null。
     * <p>
     * 返回的子类取决于 {@link #isParseBigDecimal} 的值以及被解析的字符串。
     * <ul>
     *   <li>如果 <code>isParseBigDecimal()</code> 为 false（默认值），大多数整数值将作为 <code>Long</code> 对象返回，无论它们如何书写：<code>"17"</code> 和
     *       <code>"17.000"</code> 都解析为 <code>Long(17)</code>。
     *       无法放入 <code>Long</code> 的值将作为 <code>Double</code> 返回。这包括有小数部分的值、无穷大值、<code>NaN</code> 和值 -0.0。
     *       <code>DecimalFormat</code> <em>不会</em> 根据源字符串中是否存在小数点来决定返回 <code>Double</code> 还是 <code>Long</code>。这样做会阻止像 <code>"-9,223,372,036,854,775,808.00"</code> 这样的整数（超出 double 的尾数范围）被准确解析。
     *       <p>
     *       调用者可以使用 <code>Number</code> 方法 <code>doubleValue</code>、<code>longValue</code> 等来获取所需类型。
     *   <li>如果 <code>isParseBigDecimal()</code> 为 true，则值将作为 <code>BigDecimal</code> 对象返回。这些值是由 {@link java.math.BigDecimal#BigDecimal(String)}
     *       为相应字符串在与区域无关的格式中构造的值。特殊值负无穷大、正无穷大和 NaN 将作为持有相应 <code>Double</code> 常量值的 <code>Double</code> 实例返回。
     * </ul>
     * <p>
     * <code>DecimalFormat</code> 解析所有表示十进制数字的 Unicode 字符，这些字符由 <code>Character.digit()</code> 定义。此外，<code>DecimalFormat</code> 还将
     * <code>DecimalFormatSymbols</code> 对象中定义的本地化零数字开始的十个连续字符识别为数字。
     *
     * @param text 要解析的字符串
     * @param pos  一个包含索引和错误索引信息的 <code>ParsePosition</code> 对象，如上所述。
     * @return     解析的值，如果解析失败则返回 <code>null</code>
     * @exception  NullPointerException 如果 <code>text</code> 或 <code>pos</code> 为 null。
     */
    @Override
    public Number parse(String text, ParsePosition pos) {
        // 特殊情况 NaN
        if (text.regionMatches(pos.index, symbols.getNaN(), 0, symbols.getNaN().length())) {
            pos.index = pos.index + symbols.getNaN().length();
            return new Double(Double.NaN);
        }


                    boolean[] status = new boolean[STATUS_LENGTH];
        if (!subparse(text, pos, positivePrefix, negativePrefix, digitList, false, status)) {
            return null;
        }

        // 特殊情况 INFINITY
        if (status[STATUS_INFINITE]) {
            if (status[STATUS_POSITIVE] == (multiplier >= 0)) {
                return new Double(Double.POSITIVE_INFINITY);
            } else {
                return new Double(Double.NEGATIVE_INFINITY);
            }
        }

        if (multiplier == 0) {
            if (digitList.isZero()) {
                return new Double(Double.NaN);
            } else if (status[STATUS_POSITIVE]) {
                return new Double(Double.POSITIVE_INFINITY);
            } else {
                return new Double(Double.NEGATIVE_INFINITY);
            }
        }

        if (isParseBigDecimal()) {
            BigDecimal bigDecimalResult = digitList.getBigDecimal();

            if (multiplier != 1) {
                try {
                    bigDecimalResult = bigDecimalResult.divide(getBigDecimalMultiplier());
                }
                catch (ArithmeticException e) {  // 非终止小数扩展
                    bigDecimalResult = bigDecimalResult.divide(getBigDecimalMultiplier(), roundingMode);
                }
            }

            if (!status[STATUS_POSITIVE]) {
                bigDecimalResult = bigDecimalResult.negate();
            }
            return bigDecimalResult;
        } else {
            boolean gotDouble = true;
            boolean gotLongMinimum = false;
            double  doubleResult = 0.0;
            long    longResult = 0;

            // 最后，让 DigitList 解析数字为一个值。
            if (digitList.fitsIntoLong(status[STATUS_POSITIVE], isParseIntegerOnly())) {
                gotDouble = false;
                longResult = digitList.getLong();
                if (longResult < 0) {  // 获得 Long.MIN_VALUE
                    gotLongMinimum = true;
                }
            } else {
                doubleResult = digitList.getDouble();
            }

            // 除以乘数。我们在这里要小心，不要进行不必要的 double 和 long 之间的转换。
            if (multiplier != 1) {
                if (gotDouble) {
                    doubleResult /= multiplier;
                } else {
                    // 如果可以，避免转换为 double
                    if (longResult % multiplier == 0) {
                        longResult /= multiplier;
                    } else {
                        doubleResult = ((double)longResult) / multiplier;
                        gotDouble = true;
                    }
                }
            }

            if (!status[STATUS_POSITIVE] && !gotLongMinimum) {
                doubleResult = -doubleResult;
                longResult = -longResult;
            }

            // 在这一点上，如果我们通过乘数除以结果，结果可能适合一个 long。我们检查这种情况并在可能的情况下返回一个 long。
            // 我们必须在应用负号（如果适用）之后执行此操作，以处理 LONG_MIN 的情况；否则，如果我们用正值 -LONG_MIN 进行此操作，double > 0，但 long < 0。我们还必须在 -0.0 的情况下保留一个 double，这将与 long 0 转换为 double (bug 4162852) 进行比较。
            if (multiplier != 1 && gotDouble) {
                longResult = (long)doubleResult;
                gotDouble = ((doubleResult != (double)longResult) ||
                            (doubleResult == 0.0 && 1/doubleResult < 0.0)) &&
                            !isParseIntegerOnly();
            }

            return gotDouble ?
                (Number)new Double(doubleResult) : (Number)new Long(longResult);
        }
    }

    /**
     * 返回一个 BigInteger 乘数。
     */
    private BigInteger getBigIntegerMultiplier() {
        if (bigIntegerMultiplier == null) {
            bigIntegerMultiplier = BigInteger.valueOf(multiplier);
        }
        return bigIntegerMultiplier;
    }
    private transient BigInteger bigIntegerMultiplier;

    /**
     * 返回一个 BigDecimal 乘数。
     */
    private BigDecimal getBigDecimalMultiplier() {
        if (bigDecimalMultiplier == null) {
            bigDecimalMultiplier = new BigDecimal(multiplier);
        }
        return bigDecimalMultiplier;
    }
    private transient BigDecimal bigDecimalMultiplier;

    private static final int STATUS_INFINITE = 0;
    private static final int STATUS_POSITIVE = 1;
    private static final int STATUS_LENGTH   = 2;

    /**
     * 解析给定的文本为一个数字。从 parsePosition 开始解析文本，直到遇到无法解析的字符。
     * @param text 要解析的字符串。
     * @param parsePosition 开始解析的位置。返回时，第一个无法解析的字符。
     * @param digits 要设置为解析值的 DigitList。
     * @param isExponent 如果为 true，解析一个指数。这意味着没有无限值且仅解析整数。
     * @param status 返回时包含布尔状态标志，指示值是否为无限值以及是否为正数。
     */
    private final boolean subparse(String text, ParsePosition parsePosition,
                   String positivePrefix, String negativePrefix,
                   DigitList digits, boolean isExponent,
                   boolean status[]) {
        int position = parsePosition.index;
        int oldStart = parsePosition.index;
        int backup;
        boolean gotPositive, gotNegative;

        // 检查 positivePrefix；取最长的
        gotPositive = text.regionMatches(position, positivePrefix, 0,
                                         positivePrefix.length());
        gotNegative = text.regionMatches(position, negativePrefix, 0,
                                         negativePrefix.length());

        if (gotPositive && gotNegative) {
            if (positivePrefix.length() > negativePrefix.length()) {
                gotNegative = false;
            } else if (positivePrefix.length() < negativePrefix.length()) {
                gotPositive = false;
            }
        }


                    if (gotPositive) {
            position += positivePrefix.length();
        } else if (gotNegative) {
            position += negativePrefix.length();
        } else {
            parsePosition.errorIndex = position;
            return false;
        }

        // 处理数字或 Inf，找到小数点位置
        status[STATUS_INFINITE] = false;
        if (!isExponent && text.regionMatches(position, symbols.getInfinity(), 0,
                          symbols.getInfinity().length())) {
            position += symbols.getInfinity().length();
            status[STATUS_INFINITE] = true;
        } else {
            // 现在我们有一个可能带有分组符号和小数点的数字字符串。我们希望将这些处理成一个 DigitList。
            // 我们不希望将一堆前导零放入 DigitList，所以我们跟踪小数点的位置，
            // 只将重要的数字放入 DigitList，并根据需要调整指数。

            digits.decimalAt = digits.count = 0;
            char zero = symbols.getZeroDigit();
            char decimal = isCurrencyFormat ?
                symbols.getMonetaryDecimalSeparator() :
                symbols.getDecimalSeparator();
            char grouping = symbols.getGroupingSeparator();
            String exponentString = symbols.getExponentSeparator();
            boolean sawDecimal = false;
            boolean sawExponent = false;
            boolean sawDigit = false;
            int exponent = 0; // 如果有指数值，则设置为该值

            // 我们必须自己跟踪 digitCount，因为 digits.count 在达到最大允许数字时会固定。
            int digitCount = 0;

            backup = -1;
            for (; position < text.length(); ++position) {
                char ch = text.charAt(position);

                /* 我们识别所有数字范围，而不仅仅是拉丁数字范围 '0'..'9'。我们通过使用 Character.digit() 方法，
                 * 将有效的 Unicode 数字转换为 0..9 范围。
                 *
                 * 字符 'ch' 可能是一个数字。如果是这样，将其值从 0 到 9 放入 'digit'。首先尝试使用区域数字，
                 * 这可能是或可能不是标准的 Unicode 数字范围。如果失败，尝试使用标准的 Unicode 数字范围
                 * 通过调用 Character.digit()。如果这也失败，digit 将有一个不在 0..9 范围内的值。
                 */
                int digit = ch - zero;
                if (digit < 0 || digit > 9) {
                    digit = Character.digit(ch, 10);
                }

                if (digit == 0) {
                    // 取消备份设置（参见分组处理程序下方）
                    backup = -1; // 在下面的 continue 语句之前执行！！！
                    sawDigit = true;

                    // 处理前导零
                    if (digits.count == 0) {
                        // 忽略数字整数部分的前导零。
                        if (!sawDecimal) {
                            continue;
                        }

                        // 如果我们已经看到了小数点，但还没有看到重要的数字，
                        // 那么我们通过将 digits.decimalAt 减少到负值来计算前导零。
                        --digits.decimalAt;
                    } else {
                        ++digitCount;
                        digits.append((char)(digit + '0'));
                    }
                } else if (digit > 0 && digit <= 9) { // [sic] digit==0 已在上方处理
                    sawDigit = true;
                    ++digitCount;
                    digits.append((char)(digit + '0'));

                    // 取消备份设置（参见分组处理程序下方）
                    backup = -1;
                } else if (!isExponent && ch == decimal) {
                    // 如果我们只解析整数，或者我们已经看到了小数点，则不要解析这个小数点。
                    if (isParseIntegerOnly() || sawDecimal) {
                        break;
                    }
                    digits.decimalAt = digitCount; // 不是 digits.count!
                    sawDecimal = true;
                } else if (!isExponent && ch == grouping && isGroupingUsed()) {
                    if (sawDecimal) {
                        break;
                    }
                    // 如果我们使用分组字符，则忽略它们，但要求它们后面跟一个数字。否则我们备份并重新处理它们。
                    backup = position;
                } else if (!isExponent && text.regionMatches(position, exponentString, 0, exponentString.length())
                             && !sawExponent) {
                    // 通过递归调用此方法处理指数。
                     ParsePosition pos = new ParsePosition(position + exponentString.length());
                    boolean[] stat = new boolean[STATUS_LENGTH];
                    DigitList exponentDigits = new DigitList();

                    if (subparse(text, pos, "", Character.toString(symbols.getMinusSign()), exponentDigits, true, stat) &&
                        exponentDigits.fitsIntoLong(stat[STATUS_POSITIVE], true)) {
                        position = pos.index; // 跳过指数
                        exponent = (int)exponentDigits.getLong();
                        if (!stat[STATUS_POSITIVE]) {
                            exponent = -exponent;
                        }
                        sawExponent = true;
                    }
                    break; // 无论成功还是失败，我们都退出这个循环
                } else {
                    break;
                }
            }

            if (backup != -1) {
                position = backup;
            }


                        // 如果没有小数点，那么我们有一个整数
            if (!sawDecimal) {
                digits.decimalAt = digitCount; // 不是 digits.count!
            }

            // 调整指数，如果有
            digits.decimalAt += exponent;

            // 如果文本字符串没有任何部分被识别。例如，解析
            // "x" 使用模式 "#0.00"（返回索引和错误索引均为0）
            // 解析 "$" 使用模式 "$#0.00"。（返回索引0和错误
            // 索引1）。
            if (!sawDigit && digitCount == 0) {
                parsePosition.index = oldStart;
                parsePosition.errorIndex = oldStart;
                return false;
            }
        }

        // 检查后缀
        if (!isExponent) {
            if (gotPositive) {
                gotPositive = text.regionMatches(position, positiveSuffix, 0,
                                                 positiveSuffix.length());
            }
            if (gotNegative) {
                gotNegative = text.regionMatches(position, negativeSuffix, 0,
                                                 negativeSuffix.length());
            }

        // 如果两者都匹配，取最长的
        if (gotPositive && gotNegative) {
            if (positiveSuffix.length() > negativeSuffix.length()) {
                gotNegative = false;
            } else if (positiveSuffix.length() < negativeSuffix.length()) {
                gotPositive = false;
            }
        }

        // 如果两者都不匹配或都匹配
        if (gotPositive == gotNegative) {
            parsePosition.errorIndex = position;
            return false;
        }

        parsePosition.index = position +
            (gotPositive ? positiveSuffix.length() : negativeSuffix.length()); // 标记成功！
        } else {
            parsePosition.index = position;
        }

        status[STATUS_POSITIVE] = gotPositive;
        if (parsePosition.index == oldStart) {
            parsePosition.errorIndex = position;
            return false;
        }
        return true;
    }

    /**
     * 返回一个副本的十进制格式符号，这通常不会被程序员或用户更改。
     * @return 返回所需的 DecimalFormatSymbols 的副本
     * @see java.text.DecimalFormatSymbols
     */
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            // 不允许多个引用
            return (DecimalFormatSymbols) symbols.clone();
        } catch (Exception foo) {
            return null; // 应该永远不会发生
        }
    }


    /**
     * 设置十进制格式符号，这通常不会被程序员或用户更改。
     * @param newSymbols 所需的 DecimalFormatSymbols
     * @see java.text.DecimalFormatSymbols
     */
    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        try {
            // 不允许多个引用
            symbols = (DecimalFormatSymbols) newSymbols.clone();
            expandAffixes();
            fastPathCheckNeeded = true;
        } catch (Exception foo) {
            // 应该永远不会发生
        }
    }

    /**
     * 获取正数前缀。
     * <P>示例：+123, $123, sFr123
     *
     * @return 正数前缀
     */
    public String getPositivePrefix () {
        return positivePrefix;
    }

    /**
     * 设置正数前缀。
     * <P>示例：+123, $123, sFr123
     *
     * @param newValue 新的正数前缀
     */
    public void setPositivePrefix (String newValue) {
        positivePrefix = newValue;
        posPrefixPattern = null;
        positivePrefixFieldPositions = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于正数前缀的字段位置。如果用户已通过 <code>setPositivePrefix</code> 显式设置
     * 正数前缀，则不使用此方法。这是懒惰创建的。
     *
     * @return 正数前缀中的字段位置
     */
    private FieldPosition[] getPositivePrefixFieldPositions() {
        if (positivePrefixFieldPositions == null) {
            if (posPrefixPattern != null) {
                positivePrefixFieldPositions = expandAffix(posPrefixPattern);
            } else {
                positivePrefixFieldPositions = EmptyFieldPositionArray;
            }
        }
        return positivePrefixFieldPositions;
    }

    /**
     * 获取负数前缀。
     * <P>示例：-123, ($123)（带有负数后缀），sFr-123
     *
     * @return 负数前缀
     */
    public String getNegativePrefix () {
        return negativePrefix;
    }

    /**
     * 设置负数前缀。
     * <P>示例：-123, ($123)（带有负数后缀），sFr-123
     *
     * @param newValue 新的负数前缀
     */
    public void setNegativePrefix (String newValue) {
        negativePrefix = newValue;
        negPrefixPattern = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于负数前缀的字段位置。如果用户已通过 <code>setNegativePrefix</code> 显式设置
     * 负数前缀，则不使用此方法。这是懒惰创建的。
     *
     * @return 正数前缀中的字段位置
     */
    private FieldPosition[] getNegativePrefixFieldPositions() {
        if (negativePrefixFieldPositions == null) {
            if (negPrefixPattern != null) {
                negativePrefixFieldPositions = expandAffix(negPrefixPattern);
            } else {
                negativePrefixFieldPositions = EmptyFieldPositionArray;
            }
        }
        return negativePrefixFieldPositions;
    }

    /**
     * 获取正数后缀。
     * <P>示例：123%
     *
     * @return 正数后缀
     */
    public String getPositiveSuffix () {
        return positiveSuffix;
    }

    /**
     * 设置正数后缀。
     * <P>示例：123%
     *
     * @param newValue 新的正数后缀
     */
    public void setPositiveSuffix (String newValue) {
        positiveSuffix = newValue;
        posSuffixPattern = null;
        fastPathCheckNeeded = true;
    }


                /**
     * 返回用于正数后缀的字段的 FieldPositions。如果用户已通过 <code>setPositiveSuffix</code> 明确设置了正数后缀，则不会使用此方法。这是惰性创建的。
     *
     * @return 正数前缀中的 FieldPositions
     */
    private FieldPosition[] getPositiveSuffixFieldPositions() {
        if (positiveSuffixFieldPositions == null) {
            if (posSuffixPattern != null) {
                positiveSuffixFieldPositions = expandAffix(posSuffixPattern);
            } else {
                positiveSuffixFieldPositions = EmptyFieldPositionArray;
            }
        }
        return positiveSuffixFieldPositions;
    }

    /**
     * 获取负数后缀。
     * <P>示例：-123%，（$123）（带有正数后缀）
     *
     * @return 负数后缀
     */
    public String getNegativeSuffix () {
        return negativeSuffix;
    }

    /**
     * 设置负数后缀。
     * <P>示例：123%
     *
     * @param newValue 新的负数后缀
     */
    public void setNegativeSuffix (String newValue) {
        negativeSuffix = newValue;
        negSuffixPattern = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于负数后缀的字段的 FieldPositions。如果用户已通过 <code>setNegativeSuffix</code> 明确设置了负数后缀，则不会使用此方法。这是惰性创建的。
     *
     * @return 正数前缀中的 FieldPositions
     */
    private FieldPosition[] getNegativeSuffixFieldPositions() {
        if (negativeSuffixFieldPositions == null) {
            if (negSuffixPattern != null) {
                negativeSuffixFieldPositions = expandAffix(negSuffixPattern);
            } else {
                negativeSuffixFieldPositions = EmptyFieldPositionArray;
            }
        }
        return negativeSuffixFieldPositions;
    }

    /**
     * 获取用于百分比、千分比等格式的乘数。
     *
     * @return 乘数
     * @see #setMultiplier(int)
     */
    public int getMultiplier () {
        return multiplier;
    }

    /**
     * 设置用于百分比、千分比等格式的乘数。
     * 对于百分比格式，将乘数设置为 100，并将后缀设置为包含 '%'（对于阿拉伯语，使用阿拉伯百分号）。
     * 对于千分比格式，将乘数设置为 1000，并将后缀设置为包含 '&#92;u2030'。
     *
     * <P>示例：乘数为 100 时，1.23 被格式化为 "123"，"123" 被解析为 1.23。
     *
     * @param newValue 新的乘数
     * @see #getMultiplier
     */
    public void setMultiplier (int newValue) {
        multiplier = newValue;
        bigDecimalMultiplier = null;
        bigIntegerMultiplier = null;
        fastPathCheckNeeded = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupingUsed(boolean newValue) {
        super.setGroupingUsed(newValue);
        fastPathCheckNeeded = true;
    }

    /**
     * 返回分组大小。分组大小是指数字整数部分中分组分隔符之间的数字位数。例如，在数字 "123,456.78" 中，分组大小为 3。
     *
     * @return 分组大小
     * @see #setGroupingSize
     * @see java.text.NumberFormat#isGroupingUsed
     * @see java.text.DecimalFormatSymbols#getGroupingSeparator
     */
    public int getGroupingSize () {
        return groupingSize;
    }

    /**
     * 设置分组大小。分组大小是指数字整数部分中分组分隔符之间的数字位数。例如，在数字 "123,456.78" 中，分组大小为 3。
     * <br>
     * 传入的值将转换为字节，可能会丢失信息。
     *
     * @param newValue 新的分组大小
     * @see #getGroupingSize
     * @see java.text.NumberFormat#setGroupingUsed
     * @see java.text.DecimalFormatSymbols#setGroupingSeparator
     */
    public void setGroupingSize (int newValue) {
        groupingSize = (byte)newValue;
        fastPathCheckNeeded = true;
    }

    /**
     * 允许获取整数中小数点的行为。（小数点在小数中总是显示。）
     * <P>示例：小数点开启：12345 → 12345.；关闭：12345 → 12345
     *
     * @return 如果小数点总是显示，则返回 {@code true}；否则返回 {@code false}
     */
    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    /**
     * 允许设置整数中小数点的行为。（小数点在小数中总是显示。）
     * <P>示例：小数点开启：12345 → 12345.；关闭：12345 → 12345
     *
     * @param newValue 如果小数点总是显示，则返回 {@code true}；否则返回 {@code false}
     */
    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        decimalSeparatorAlwaysShown = newValue;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回 {@link #parse(java.lang.String, java.text.ParsePosition)}
     * 方法是否返回 <code>BigDecimal</code>。默认值为 false。
     *
     * @return 如果解析方法返回 BigDecimal，则返回 {@code true}；否则返回 {@code false}
     * @see #setParseBigDecimal
     * @since 1.5
     */
    public boolean isParseBigDecimal() {
        return parseBigDecimal;
    }

    /**
     * 设置 {@link #parse(java.lang.String, java.text.ParsePosition)}
     * 方法是否返回 <code>BigDecimal</code>。
     *
     * @param newValue 如果解析方法返回 BigDecimal，则返回 {@code true}；否则返回 {@code false}
     * @see #isParseBigDecimal
     * @since 1.5
     */
    public void setParseBigDecimal(boolean newValue) {
        parseBigDecimal = newValue;
    }

    /**
     * 标准重写；语义无变化。
     */
    @Override
    public Object clone() {
        DecimalFormat other = (DecimalFormat) super.clone();
        other.symbols = (DecimalFormatSymbols) symbols.clone();
        other.digitList = (DigitList) digitList.clone();


                    // 快速路径几乎是无状态的算法。唯一的逻辑状态是
        // isFastPath 标志。此外，fastPathCheckNeeded 是一个哨兵标志
        // 当设置为 true 时，会强制重新计算所有快速路径字段。
        //
        // 因此，无需克隆所有快速路径字段。
        // 克隆时，只需将 fastPathCheckNeeded 设置为 true，
        // 并将 fastPathData 初始化为 null，就像它是一个全新的实例一样。
        // 每个快速路径字段将在下次使用快速路径算法时（仅一次）重新计算。
        other.fastPathCheckNeeded = true;
        other.isFastPath = false;
        other.fastPathData = null;

        return other;
    }

    /**
     * 重写 equals 方法
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false; // 超类执行类检查
        DecimalFormat other = (DecimalFormat) obj;
        return ((posPrefixPattern == other.posPrefixPattern &&
                 positivePrefix.equals(other.positivePrefix))
                || (posPrefixPattern != null &&
                    posPrefixPattern.equals(other.posPrefixPattern)))
            && ((posSuffixPattern == other.posSuffixPattern &&
                 positiveSuffix.equals(other.positiveSuffix))
                || (posSuffixPattern != null &&
                    posSuffixPattern.equals(other.posSuffixPattern)))
            && ((negPrefixPattern == other.negPrefixPattern &&
                 negativePrefix.equals(other.negativePrefix))
                || (negPrefixPattern != null &&
                    negPrefixPattern.equals(other.negPrefixPattern)))
            && ((negSuffixPattern == other.negSuffixPattern &&
                 negativeSuffix.equals(other.negativeSuffix))
                || (negSuffixPattern != null &&
                    negSuffixPattern.equals(other.negSuffixPattern)))
            && multiplier == other.multiplier
            && groupingSize == other.groupingSize
            && decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown
            && parseBigDecimal == other.parseBigDecimal
            && useExponentialNotation == other.useExponentialNotation
            && (!useExponentialNotation ||
                minExponentDigits == other.minExponentDigits)
            && maximumIntegerDigits == other.maximumIntegerDigits
            && minimumIntegerDigits == other.minimumIntegerDigits
            && maximumFractionDigits == other.maximumFractionDigits
            && minimumFractionDigits == other.minimumFractionDigits
            && roundingMode == other.roundingMode
            && symbols.equals(other.symbols);
    }

    /**
     * 重写 hashCode 方法
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 37 + positivePrefix.hashCode();
        // 只需足够的字段以获得合理的分布
    }

    /**
     * 合成一个表示此 Format 对象当前状态的模式字符串。
     *
     * @return 一个模式字符串
     * @see #applyPattern
     */
    public String toPattern() {
        return toPattern( false );
    }

    /**
     * 合成一个表示此 Format 对象当前状态的本地化模式字符串。
     *
     * @return 一个本地化模式字符串
     * @see #applyPattern
     */
    public String toLocalizedPattern() {
        return toPattern( true );
    }

    /**
     * 将前缀模式字符串扩展为扩展的前缀字符串。如果任何前缀模式字符串为 null，则不进行扩展。每当符号或前缀模式发生变化时，应调用此方法以保持扩展的前缀字符串的更新。
     */
    private void expandAffixes() {
        // 重用一个 StringBuffer 以提高性能
        StringBuffer buffer = new StringBuffer();
        if (posPrefixPattern != null) {
            positivePrefix = expandAffix(posPrefixPattern, buffer);
            positivePrefixFieldPositions = null;
        }
        if (posSuffixPattern != null) {
            positiveSuffix = expandAffix(posSuffixPattern, buffer);
            positiveSuffixFieldPositions = null;
        }
        if (negPrefixPattern != null) {
            negativePrefix = expandAffix(negPrefixPattern, buffer);
            negativePrefixFieldPositions = null;
        }
        if (negSuffixPattern != null) {
            negativeSuffix = expandAffix(negSuffixPattern, buffer);
            negativeSuffixFieldPositions = null;
        }
    }

    /**
     * 将前缀模式扩展为前缀字符串。模式中的所有字符都是字面量，除非以 QUOTE 开头。QUOTE 之后识别以下字符：PATTERN_PERCENT, PATTERN_PER_MILLE,
     * PATTERN_MINUS, 和 CURRENCY_SIGN。如果 CURRENCY_SIGN 重复出现（QUOTE + CURRENCY_SIGN + CURRENCY_SIGN），则解释为 ISO 4217 货币代码。QUOTE 之后的任何其他字符表示其本身。
     * QUOTE 必须后跟另一个字符；QUOTE 不能单独出现在模式的末尾。
     *
     * @param pattern 非空的、可能为空的模式
     * @param buffer 一个临时的 StringBuffer；其内容将丢失
     * @return 模式的扩展等效字符串
     */
    private String expandAffix(String pattern, StringBuffer buffer) {
        buffer.setLength(0);
        for (int i=0; i<pattern.length(); ) {
            char c = pattern.charAt(i++);
            if (c == QUOTE) {
                c = pattern.charAt(i++);
                switch (c) {
                case CURRENCY_SIGN:
                    if (i<pattern.length() &&
                        pattern.charAt(i) == CURRENCY_SIGN) {
                        ++i;
                        buffer.append(symbols.getInternationalCurrencySymbol());
                    } else {
                        buffer.append(symbols.getCurrencySymbol());
                    }
                    continue;
                case PATTERN_PERCENT:
                    c = symbols.getPercent();
                    break;
                case PATTERN_PER_MILLE:
                    c = symbols.getPerMill();
                    break;
                case PATTERN_MINUS:
                    c = symbols.getMinusSign();
                    break;
                }
            }
            buffer.append(c);
        }
        return buffer.toString();
    }


                /**
     * 将一个前缀或后缀模式扩展为一个描述模式如何扩展的 FieldPosition 数组。
     * 模式中的所有字符都是字面量，除非以 QUOTE 开头。在 QUOTE 之后识别以下字符：
     * PATTERN_PERCENT, PATTERN_PER_MILLE, PATTERN_MINUS, 和 CURRENCY_SIGN。
     * 如果 CURRENCY_SIGN 重复出现（QUOTE + CURRENCY_SIGN + CURRENCY_SIGN），则解释为 ISO 4217 货币代码。
     * 任何其他在 QUOTE 之后的字符代表其本身。QUOTE 必须跟随另一个字符；QUOTE 不能单独出现在模式的末尾。
     *
     * @param pattern 非空的，可能为空的模式
     * @return 结果字段的 FieldPosition 数组。
     */
    private FieldPosition[] expandAffix(String pattern) {
        ArrayList<FieldPosition> positions = null;
        int stringIndex = 0;
        for (int i=0; i<pattern.length(); ) {
            char c = pattern.charAt(i++);
            if (c == QUOTE) {
                int field = -1;
                Format.Field fieldID = null;
                c = pattern.charAt(i++);
                switch (c) {
                case CURRENCY_SIGN:
                    String string;
                    if (i<pattern.length() &&
                        pattern.charAt(i) == CURRENCY_SIGN) {
                        ++i;
                        string = symbols.getInternationalCurrencySymbol();
                    } else {
                        string = symbols.getCurrencySymbol();
                    }
                    if (!string.isEmpty()) {
                        if (positions == null) {
                            positions = new ArrayList<>(2);
                        }
                        FieldPosition fp = new FieldPosition(Field.CURRENCY);
                        fp.setBeginIndex(stringIndex);
                        fp.setEndIndex(stringIndex + string.length());
                        positions.add(fp);
                        stringIndex += string.length();
                    }
                    continue;
                case PATTERN_PERCENT:
                    c = symbols.getPercent();
                    field = -1;
                    fieldID = Field.PERCENT;
                    break;
                case PATTERN_PER_MILLE:
                    c = symbols.getPerMill();
                    field = -1;
                    fieldID = Field.PERMILLE;
                    break;
                case PATTERN_MINUS:
                    c = symbols.getMinusSign();
                    field = -1;
                    fieldID = Field.SIGN;
                    break;
                }
                if (fieldID != null) {
                    if (positions == null) {
                        positions = new ArrayList<>(2);
                    }
                    FieldPosition fp = new FieldPosition(fieldID, field);
                    fp.setBeginIndex(stringIndex);
                    fp.setEndIndex(stringIndex + 1);
                    positions.add(fp);
                }
            }
            stringIndex++;
        }
        if (positions != null) {
            return positions.toArray(EmptyFieldPositionArray);
        }
        return EmptyFieldPositionArray;
    }

    /**
     * 将一个前缀或后缀模式附加到给定的 StringBuffer 中，必要时引用特殊字符。
     * 使用内部前缀或后缀模式（如果存在），或者如果内部前缀或后缀模式为 null，则使用字面量前缀或后缀。
     * 附加的字符串在传递给 toPattern() 时将生成相同的前缀或后缀模式（或字面量前缀或后缀）。
     *
     * @param buffer 前缀或后缀字符串附加到此
     * @param affixPattern 一个模式，如 posPrefixPattern；可以为 null
     * @param expAffix 对应的扩展前缀或后缀，如 positivePrefix。如果 affixPattern 为 null，则忽略。如果 affixPattern 为 null，则将 expAffix 作为字面量前缀或后缀附加。
     * @param localized 如果附加的模式应包含本地化的模式字符，则为 true；否则，附加非本地化的模式字符。
     */
    private void appendAffix(StringBuffer buffer, String affixPattern,
                             String expAffix, boolean localized) {
        if (affixPattern == null) {
            appendAffix(buffer, expAffix, localized);
        } else {
            int i;
            for (int pos=0; pos<affixPattern.length(); pos=i) {
                i = affixPattern.indexOf(QUOTE, pos);
                if (i < 0) {
                    appendAffix(buffer, affixPattern.substring(pos), localized);
                    break;
                }
                if (i > pos) {
                    appendAffix(buffer, affixPattern.substring(pos, i), localized);
                }
                char c = affixPattern.charAt(++i);
                ++i;
                if (c == QUOTE) {
                    buffer.append(c);
                    // 通过下面的代码再附加一个 QUOTE
                } else if (c == CURRENCY_SIGN &&
                           i<affixPattern.length() &&
                           affixPattern.charAt(i) == CURRENCY_SIGN) {
                    ++i;
                    buffer.append(c);
                    // 通过下面的代码再附加一个 CURRENCY_SIGN
                } else if (localized) {
                    switch (c) {
                    case PATTERN_PERCENT:
                        c = symbols.getPercent();
                        break;
                    case PATTERN_PER_MILLE:
                        c = symbols.getPerMill();
                        break;
                    case PATTERN_MINUS:
                        c = symbols.getMinusSign();
                        break;
                    }
                }
                buffer.append(c);
            }
        }
    }

    /**
     * 将一个前缀或后缀附加到给定的 StringBuffer 中，如果存在特殊字符则使用引号。
     * 单引号本身必须在任何情况下转义。
     */
    private void appendAffix(StringBuffer buffer, String affix, boolean localized) {
        boolean needQuote;
        if (localized) {
            needQuote = affix.indexOf(symbols.getZeroDigit()) >= 0
                || affix.indexOf(symbols.getGroupingSeparator()) >= 0
                || affix.indexOf(symbols.getDecimalSeparator()) >= 0
                || affix.indexOf(symbols.getPercent()) >= 0
                || affix.indexOf(symbols.getPerMill()) >= 0
                || affix.indexOf(symbols.getDigit()) >= 0
                || affix.indexOf(symbols.getPatternSeparator()) >= 0
                || affix.indexOf(symbols.getMinusSign()) >= 0
                || affix.indexOf(CURRENCY_SIGN) >= 0;
        } else {
            needQuote = affix.indexOf(PATTERN_ZERO_DIGIT) >= 0
                || affix.indexOf(PATTERN_GROUPING_SEPARATOR) >= 0
                || affix.indexOf(PATTERN_DECIMAL_SEPARATOR) >= 0
                || affix.indexOf(PATTERN_PERCENT) >= 0
                || affix.indexOf(PATTERN_PER_MILLE) >= 0
                || affix.indexOf(PATTERN_DIGIT) >= 0
                || affix.indexOf(PATTERN_SEPARATOR) >= 0
                || affix.indexOf(PATTERN_MINUS) >= 0
                || affix.indexOf(CURRENCY_SIGN) >= 0;
        }
        if (needQuote) buffer.append('\'');
        if (affix.indexOf('\'') < 0) buffer.append(affix);
        else {
            for (int j=0; j<affix.length(); ++j) {
                char c = affix.charAt(j);
                buffer.append(c);
                if (c == '\'') buffer.append(c);
            }
        }
        if (needQuote) buffer.append('\'');
    }


                /**
     * 实际生成模式的工作。  */
    private String toPattern(boolean localized) {
        StringBuffer result = new StringBuffer();
        for (int j = 1; j >= 0; --j) {
            if (j == 1)
                appendAffix(result, posPrefixPattern, positivePrefix, localized);
            else appendAffix(result, negPrefixPattern, negativePrefix, localized);
            int i;
            int digitCount = useExponentialNotation
                        ? getMaximumIntegerDigits()
                        : Math.max(groupingSize, getMinimumIntegerDigits())+1;
            for (i = digitCount; i > 0; --i) {
                if (i != digitCount && isGroupingUsed() && groupingSize != 0 &&
                    i % groupingSize == 0) {
                    result.append(localized ? symbols.getGroupingSeparator() :
                                  PATTERN_GROUPING_SEPARATOR);
                }
                result.append(i <= getMinimumIntegerDigits()
                    ? (localized ? symbols.getZeroDigit() : PATTERN_ZERO_DIGIT)
                    : (localized ? symbols.getDigit() : PATTERN_DIGIT));
            }
            if (getMaximumFractionDigits() > 0 || decimalSeparatorAlwaysShown)
                result.append(localized ? symbols.getDecimalSeparator() :
                              PATTERN_DECIMAL_SEPARATOR);
            for (i = 0; i < getMaximumFractionDigits(); ++i) {
                if (i < getMinimumFractionDigits()) {
                    result.append(localized ? symbols.getZeroDigit() :
                                  PATTERN_ZERO_DIGIT);
                } else {
                    result.append(localized ? symbols.getDigit() :
                                  PATTERN_DIGIT);
                }
            }
        if (useExponentialNotation)
        {
            result.append(localized ? symbols.getExponentSeparator() :
                  PATTERN_EXPONENT);
        for (i=0; i<minExponentDigits; ++i)
                    result.append(localized ? symbols.getZeroDigit() :
                                  PATTERN_ZERO_DIGIT);
        }
            if (j == 1) {
                appendAffix(result, posSuffixPattern, positiveSuffix, localized);
                if ((negSuffixPattern == posSuffixPattern && // n == p == null
                     negativeSuffix.equals(positiveSuffix))
                    || (negSuffixPattern != null &&
                        negSuffixPattern.equals(posSuffixPattern))) {
                    if ((negPrefixPattern != null && posPrefixPattern != null &&
                         negPrefixPattern.equals("'-" + posPrefixPattern)) ||
                        (negPrefixPattern == posPrefixPattern && // n == p == null
                         negativePrefix.equals(symbols.getMinusSign() + positivePrefix)))
                        break;
                }
                result.append(localized ? symbols.getPatternSeparator() :
                              PATTERN_SEPARATOR);
            } else appendAffix(result, negSuffixPattern, negativeSuffix, localized);
        }
        return result.toString();
    }

    /**
     * 将给定的模式应用到此 Format 对象。模式是各种格式化属性的简写规范。
     * 这些属性也可以通过各种 setter 方法单独更改。
     * <p>
     * 该例程不设置整数位数的限制，因为这是典型的最终用户需求；
     * 如果您想设置实际值，请使用 setMaximumInteger。
     * 对于负数，使用第二个模式，用分号分隔
     * <P>示例 <code>"#,#00.0#"</code> &rarr; 1,234.56
     * <P>这意味着最少 2 位整数，1 位小数，最多 2 位小数。
     * <p>示例：对于负数使用 <code>"#,#00.0#;(#,#00.0#)"</code>，用括号表示。
     * <p>在负数模式中，最小和最大计数被忽略；
     * 假设这些已在正数模式中设置。
     *
     * @param pattern 新模式
     * @exception NullPointerException 如果 <code>pattern</code> 为 null
     * @exception IllegalArgumentException 如果给定的模式无效。
     */
    public void applyPattern(String pattern) {
        applyPattern(pattern, false);
    }

    /**
     * 将给定的模式应用到此 Format 对象。假设模式是本地化的表示形式。模式是各种格式化属性的简写规范。
     * 这些属性也可以通过各种 setter 方法单独更改。
     * <p>
     * 该例程不设置整数位数的限制，因为这是典型的最终用户需求；
     * 如果您想设置实际值，请使用 setMaximumInteger。
     * 对于负数，使用第二个模式，用分号分隔
     * <P>示例 <code>"#,#00.0#"</code> &rarr; 1,234.56
     * <P>这意味着最少 2 位整数，1 位小数，最多 2 位小数。
     * <p>示例：对于负数使用 <code>"#,#00.0#;(#,#00.0#)"</code>，用括号表示。
     * <p>在负数模式中，最小和最大计数被忽略；
     * 假设这些已在正数模式中设置。
     *
     * @param pattern 新模式
     * @exception NullPointerException 如果 <code>pattern</code> 为 null
     * @exception IllegalArgumentException 如果给定的模式无效。
     */
    public void applyLocalizedPattern(String pattern) {
        applyPattern(pattern, true);
    }

    /**
     * 应用模式的实际工作。
     */
    private void applyPattern(String pattern, boolean localized) {
        char zeroDigit         = PATTERN_ZERO_DIGIT;
        char groupingSeparator = PATTERN_GROUPING_SEPARATOR;
        char decimalSeparator  = PATTERN_DECIMAL_SEPARATOR;
        char percent           = PATTERN_PERCENT;
        char perMill           = PATTERN_PER_MILLE;
        char digit             = PATTERN_DIGIT;
        char separator         = PATTERN_SEPARATOR;
        String exponent          = PATTERN_EXPONENT;
        char minus             = PATTERN_MINUS;
        if (localized) {
            zeroDigit         = symbols.getZeroDigit();
            groupingSeparator = symbols.getGroupingSeparator();
            decimalSeparator  = symbols.getDecimalSeparator();
            percent           = symbols.getPercent();
            perMill           = symbols.getPerMill();
            digit             = symbols.getDigit();
            separator         = symbols.getPatternSeparator();
            exponent          = symbols.getExponentSeparator();
            minus             = symbols.getMinusSign();
        }
        boolean gotNegative = false;
        decimalSeparatorAlwaysShown = false;
        isCurrencyFormat = false;
        useExponentialNotation = false;


                    // 两个变量用于记录第一阶段（phase 1）占用的模式子范围。这在处理
        // 第二个模式（表示负数的模式）时使用，以确保两个模式之间的第一阶段没有偏差。
        int phaseOneStart = 0;
        int phaseOneLength = 0;

        int start = 0;
        for (int j = 1; j >= 0 && start < pattern.length(); --j) {
            boolean inQuote = false;
            StringBuffer prefix = new StringBuffer();
            StringBuffer suffix = new StringBuffer();
            int decimalPos = -1;
            int multiplier = 1;
            int digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0;
            byte groupingCount = -1;

            // 阶段范围从 0 到 2。阶段 0 是前缀。阶段 1 是包含数字、小数分隔符、
            // 分组字符的模式部分。阶段 2 是后缀。在阶段 0 和 2 中，识别并转换
            // 百分号、千分号和货币符号。字符严格按阶段分离；例如，如果阶段 1 的字符
            // 需要出现在后缀中，它们必须被引用。
            int phase = 0;

            // 附缀是前缀或后缀。
            StringBuffer affix = prefix;

            for (int pos = start; pos < pattern.length(); ++pos) {
                char ch = pattern.charAt(pos);
                switch (phase) {
                case 0:
                case 2:
                    // 处理前缀/后缀字符
                    if (inQuote) {
                        // 引号内的引号表示要么是闭合引号，要么是两个引号，即引号字面量。也就是说，
                        // 我们有 'do' 或 'don''t' 中的第二个引号。
                        if (ch == QUOTE) {
                            if ((pos+1) < pattern.length() &&
                                pattern.charAt(pos+1) == QUOTE) {
                                ++pos;
                                affix.append("''"); // 'don''t'
                            } else {
                                inQuote = false; // 'do'
                            }
                            continue;
                        }
                    } else {
                        // 处理在前缀或后缀阶段看到的未引用字符。
                        if (ch == digit ||
                            ch == zeroDigit ||
                            ch == groupingSeparator ||
                            ch == decimalSeparator) {
                            phase = 1;
                            if (j == 1) {
                                phaseOneStart = pos;
                            }
                            --pos; // 重新处理此字符
                            continue;
                        } else if (ch == CURRENCY_SIGN) {
                            // 使用前瞻确定货币符号是否加倍。
                            boolean doubled = (pos + 1) < pattern.length() &&
                                pattern.charAt(pos + 1) == CURRENCY_SIGN;
                            if (doubled) { // 跳过加倍的字符
                             ++pos;
                            }
                            isCurrencyFormat = true;
                            affix.append(doubled ? "'\u00A4\u00A4" : "'\u00A4");
                            continue;
                        } else if (ch == QUOTE) {
                            // 引号外的引号表示要么是开引号，要么是两个引号，即引号字面量。也就是说，
                            // 我们有 'do' 或 o''clock 中的第一个引号。
                            if (ch == QUOTE) {
                                if ((pos+1) < pattern.length() &&
                                    pattern.charAt(pos+1) == QUOTE) {
                                    ++pos;
                                    affix.append("''"); // o''clock
                                } else {
                                    inQuote = true; // 'do'
                                }
                                continue;
                            }
                        } else if (ch == separator) {
                            // 在看到阶段 1 的数字字符之前不允许分隔符，也不允许在第二个模式（j == 0）中使用分隔符。
                            if (phase == 0 || j == 0) {
                                throw new IllegalArgumentException("Unquoted special character '" +
                                    ch + "' in pattern \"" + pattern + '"');
                            }
                            start = pos + 1;
                            pos = pattern.length();
                            continue;
                        }

                        // 接下来处理直接追加的字符。
                        else if (ch == percent) {
                            if (multiplier != 1) {
                                throw new IllegalArgumentException("Too many percent/per mille characters in pattern \"" +
                                    pattern + '"');
                            }
                            multiplier = 100;
                            affix.append("'%");
                            continue;
                        } else if (ch == perMill) {
                            if (multiplier != 1) {
                                throw new IllegalArgumentException("Too many percent/per mille characters in pattern \"" +
                                    pattern + '"');
                            }
                            multiplier = 1000;
                            affix.append("'\u2030");
                            continue;
                        } else if (ch == minus) {
                            affix.append("'-");
                            continue;
                        }
                    }
                    // 注意，如果我们在引号内，或者这是一个未引用的非特殊字符，那么我们通常会在这里通过。
                    affix.append(ch);
                    break;


                            case 1:
                    // 第一阶段在两个子模式中必须相同。我们通过直接比较来强制执行这一点。在处理第一个子模式时，我们只记录其长度。在处理第二个子模式时，我们比较字符。
                    if (j == 1) {
                        ++phaseOneLength;
                    } else {
                        if (--phaseOneLength == 0) {
                            phase = 2;
                            affix = suffix;
                        }
                        continue;
                    }

                    // 处理数字、小数点和分组字符。我们记录五条信息。我们期望数字出现在模式 ####0000.#### 中，我们记录左侧数字的数量、零（中心）数字的数量和右侧数字的数量。记录最后一个分组字符的位置（应在前两个字符块中的某个位置），以及小数点的位置（如果有的话，应在零数字中）。如果没有小数点，则不应有右侧数字。
                    if (ch == digit) {
                        if (zeroDigitCount > 0) {
                            ++digitRightCount;
                        } else {
                            ++digitLeftCount;
                        }
                        if (groupingCount >= 0 && decimalPos < 0) {
                            ++groupingCount;
                        }
                    } else if (ch == zeroDigit) {
                        if (digitRightCount > 0) {
                            throw new IllegalArgumentException("模式 \"" +
                                pattern + "\" 中意外出现 '0'");
                        }
                        ++zeroDigitCount;
                        if (groupingCount >= 0 && decimalPos < 0) {
                            ++groupingCount;
                        }
                    } else if (ch == groupingSeparator) {
                        groupingCount = 0;
                    } else if (ch == decimalSeparator) {
                        if (decimalPos >= 0) {
                            throw new IllegalArgumentException("模式 \"" +
                                pattern + "\" 中出现多个小数点");
                        }
                        decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
                    } else if (pattern.regionMatches(pos, exponent, 0, exponent.length())){
                        if (useExponentialNotation) {
                            throw new IllegalArgumentException("模式 \"" + pattern + "\" 中出现多个指数符号");
                        }
                        useExponentialNotation = true;
                        minExponentDigits = 0;

                        // 使用前瞻来解析模式的指数部分，然后跳转到第二阶段。
                        pos = pos+exponent.length();
                         while (pos < pattern.length() &&
                               pattern.charAt(pos) == zeroDigit) {
                            ++minExponentDigits;
                            ++phaseOneLength;
                            ++pos;
                        }

                        if ((digitLeftCount + zeroDigitCount) < 1 ||
                            minExponentDigits < 1) {
                            throw new IllegalArgumentException("模式 \"" + pattern + "\" 中指数部分格式错误");
                        }

                        // 转到第二阶段
                        phase = 2;
                        affix = suffix;
                        --pos;
                        continue;
                    } else {
                        phase = 2;
                        affix = suffix;
                        --pos;
                        --phaseOneLength;
                        continue;
                    }
                    break;
                }
            }

            // 处理没有 '0' 模式字符的模式。这些模式是合法的，但必须进行解释。 "##.###" -> "#0.###"。 ".###" -> ".0##"。
            /* 我们允许 "####" 形式的模式产生 zeroDigitCount 为零的情况（明白了吗？）；尽管这似乎可能会导致 format() 生成空字符串，但 format() 会检查这种情况并在此情况下输出一个零数字。
             * zeroDigitCount 为零会产生最小整数位数为零，这允许正确的往返模式。也就是说，我们不希望 "#" 在调用 toPattern() 时变成 "#0"（即使这在语义上是正确的）。
             */
            if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
                // 处理 "###.###" 和 "###." 和 ".###"
                int n = decimalPos;
                if (n == 0) { // 处理 ".###"
                    ++n;
                }
                digitRightCount = digitLeftCount - n;
                digitLeftCount = n - 1;
                zeroDigitCount = 1;
            }

            // 对数字进行语法检查。
            if ((decimalPos < 0 && digitRightCount > 0) ||
                (decimalPos >= 0 && (decimalPos < digitLeftCount ||
                 decimalPos > (digitLeftCount + zeroDigitCount))) ||
                 groupingCount == 0 || inQuote) {
                throw new IllegalArgumentException("模式 \"" +
                    pattern + "\" 格式错误");
            }

            if (j == 1) {
                posPrefixPattern = prefix.toString();
                posSuffixPattern = suffix.toString();
                negPrefixPattern = posPrefixPattern;   // 暂时假设这些
                negSuffixPattern = posSuffixPattern;
                int digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
                /* effectiveDecimalPos 是小数点的位置，或者如果没有小数点，则是小数点应该在的位置。注意，如果 decimalPos<0，
                 * 则 digitTotalCount == digitLeftCount + zeroDigitCount。
                 */
                int effectiveDecimalPos = decimalPos >= 0 ?
                    decimalPos : digitTotalCount;
                setMinimumIntegerDigits(effectiveDecimalPos - digitLeftCount);
                setMaximumIntegerDigits(useExponentialNotation ?
                    digitLeftCount + getMinimumIntegerDigits() :
                    MAXIMUM_INTEGER_DIGITS);
                setMaximumFractionDigits(decimalPos >= 0 ?
                    (digitTotalCount - decimalPos) : 0);
                setMinimumFractionDigits(decimalPos >= 0 ?
                    (digitLeftCount + zeroDigitCount - decimalPos) : 0);
                setGroupingUsed(groupingCount > 0);
                this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
                this.multiplier = multiplier;
                setDecimalSeparatorAlwaysShown(decimalPos == 0 ||
                    decimalPos == digitTotalCount);
            } else {
                negPrefixPattern = prefix.toString();
                negSuffixPattern = suffix.toString();
                gotNegative = true;
            }
        }


                    if (pattern.isEmpty()) {
            posPrefixPattern = posSuffixPattern = "";
            setMinimumIntegerDigits(0);
            setMaximumIntegerDigits(MAXIMUM_INTEGER_DIGITS);
            setMinimumFractionDigits(0);
            setMaximumFractionDigits(MAXIMUM_FRACTION_DIGITS);
        }

        // 如果没有负数模式，或者负数模式与正数模式相同，则在正数模式前添加负号以形成负数模式。
        if (!gotNegative ||
            (negPrefixPattern.equals(posPrefixPattern)
             && negSuffixPattern.equals(posSuffixPattern))) {
            negSuffixPattern = posSuffixPattern;
            negPrefixPattern = "'-" + posPrefixPattern;
        }

        expandAffixes();
    }

    /**
     * 设置数字整数部分允许的最大位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和 309 中的较小值。负输入值将被替换为 0。
     * @see NumberFormat#setMaximumIntegerDigits
     */
    @Override
    public void setMaximumIntegerDigits(int newValue) {
        maximumIntegerDigits = Math.min(Math.max(0, newValue), MAXIMUM_INTEGER_DIGITS);
        super.setMaximumIntegerDigits((maximumIntegerDigits > DOUBLE_INTEGER_DIGITS) ?
            DOUBLE_INTEGER_DIGITS : maximumIntegerDigits);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            minimumIntegerDigits = maximumIntegerDigits;
            super.setMinimumIntegerDigits((minimumIntegerDigits > DOUBLE_INTEGER_DIGITS) ?
                DOUBLE_INTEGER_DIGITS : minimumIntegerDigits);
        }
        fastPathCheckNeeded = true;
    }

    /**
     * 设置数字整数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和 309 中的较小值。负输入值将被替换为 0。
     * @see NumberFormat#setMinimumIntegerDigits
     */
    @Override
    public void setMinimumIntegerDigits(int newValue) {
        minimumIntegerDigits = Math.min(Math.max(0, newValue), MAXIMUM_INTEGER_DIGITS);
        super.setMinimumIntegerDigits((minimumIntegerDigits > DOUBLE_INTEGER_DIGITS) ?
            DOUBLE_INTEGER_DIGITS : minimumIntegerDigits);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            maximumIntegerDigits = minimumIntegerDigits;
            super.setMaximumIntegerDigits((maximumIntegerDigits > DOUBLE_INTEGER_DIGITS) ?
                DOUBLE_INTEGER_DIGITS : maximumIntegerDigits);
        }
        fastPathCheckNeeded = true;
    }

    /**
     * 设置数字小数部分允许的最大位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和 340 中的较小值。负输入值将被替换为 0。
     * @see NumberFormat#setMaximumFractionDigits
     */
    @Override
    public void setMaximumFractionDigits(int newValue) {
        maximumFractionDigits = Math.min(Math.max(0, newValue), MAXIMUM_FRACTION_DIGITS);
        super.setMaximumFractionDigits((maximumFractionDigits > DOUBLE_FRACTION_DIGITS) ?
            DOUBLE_FRACTION_DIGITS : maximumFractionDigits);
        if (minimumFractionDigits > maximumFractionDigits) {
            minimumFractionDigits = maximumFractionDigits;
            super.setMinimumFractionDigits((minimumFractionDigits > DOUBLE_FRACTION_DIGITS) ?
                DOUBLE_FRACTION_DIGITS : minimumFractionDigits);
        }
        fastPathCheckNeeded = true;
    }

    /**
     * 设置数字小数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和 340 中的较小值。负输入值将被替换为 0。
     * @see NumberFormat#setMinimumFractionDigits
     */
    @Override
    public void setMinimumFractionDigits(int newValue) {
        minimumFractionDigits = Math.min(Math.max(0, newValue), MAXIMUM_FRACTION_DIGITS);
        super.setMinimumFractionDigits((minimumFractionDigits > DOUBLE_FRACTION_DIGITS) ?
            DOUBLE_FRACTION_DIGITS : minimumFractionDigits);
        if (minimumFractionDigits > maximumFractionDigits) {
            maximumFractionDigits = minimumFractionDigits;
            super.setMaximumFractionDigits((maximumFractionDigits > DOUBLE_FRACTION_DIGITS) ?
                DOUBLE_FRACTION_DIGITS : maximumFractionDigits);
        }
        fastPathCheckNeeded = true;
    }

    /**
     * 获取数字整数部分允许的最大位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用返回值和 309 中的较小值。
     * @see #setMaximumIntegerDigits
     */
    @Override
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }

    /**
     * 获取数字整数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用返回值和 309 中的较小值。
     * @see #setMinimumIntegerDigits
     */
    @Override
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    /**
     * 获取数字小数部分允许的最大位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用返回值和 340 中的较小值。
     * @see #setMaximumFractionDigits
     */
    @Override
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * 获取数字小数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的数字，使用返回值和 340 中的较小值。
     * @see #setMinimumFractionDigits
     */
    @Override
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

                /**
     * 获取此十进制格式在格式化货币值时使用的货币。
     * 通过调用 {@link DecimalFormatSymbols#getCurrency DecimalFormatSymbols.getCurrency}
     * 获取此数字格式的符号中的货币。
     *
     * @return 此十进制格式使用的货币，或 <code>null</code>
     * @since 1.4
     */
    @Override
    public Currency getCurrency() {
        return symbols.getCurrency();
    }

    /**
     * 设置此数字格式在格式化货币值时使用的货币。这不会更新数字格式的小数位数。
     * 通过调用 {@link DecimalFormatSymbols#setCurrency DecimalFormatSymbols.setCurrency}
     * 设置此数字格式的符号中的货币。
     *
     * @param currency 此十进制格式将使用的新的货币
     * @exception NullPointerException 如果 <code>currency</code> 为 null
     * @since 1.4
     */
    @Override
    public void setCurrency(Currency currency) {
        if (currency != symbols.getCurrency()) {
            symbols.setCurrency(currency);
            if (isCurrencyFormat) {
                expandAffixes();
            }
        }
        fastPathCheckNeeded = true;
    }

    /**
     * 获取此 DecimalFormat 使用的 {@link java.math.RoundingMode}。
     *
     * @return 用于此 DecimalFormat 的 <code>RoundingMode</code>。
     * @see #setRoundingMode(RoundingMode)
     * @since 1.6
     */
    @Override
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    /**
     * 设置此 DecimalFormat 使用的 {@link java.math.RoundingMode}。
     *
     * @param roundingMode 要使用的 <code>RoundingMode</code>
     * @see #getRoundingMode()
     * @exception NullPointerException 如果 <code>roundingMode</code> 为 null。
     * @since 1.6
     */
    @Override
    public void setRoundingMode(RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException();
        }

        this.roundingMode = roundingMode;
        digitList.setRoundingMode(roundingMode);
        fastPathCheckNeeded = true;
    }

    /**
     * 从流中读取默认的可序列化字段，并对较旧的序列化版本执行验证和调整。验证和调整包括：
     * <ol>
     * <li>
     * 验证超类的数字计数字段是否正确反映了对格式化数字（不包括
     * <code>BigInteger</code> 和 <code>BigDecimal</code> 对象）的限制。这些限制存储在超类中，以保持与较旧版本的序列化兼容性，而 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的限制则保存在本类中。
     * 如果在超类中，最小或最大整数位数大于 <code>DOUBLE_INTEGER_DIGITS</code> 或最小或最大小数位数大于 <code>DOUBLE_FRACTION_DIGITS</code>，则流数据无效，此方法将抛出 <code>InvalidObjectException</code>。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 4，则将 <code>roundingMode</code> 初始化为 {@link java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}。此字段是版本 4 中新增的。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 3，则使用超类的相应 getter 的值调用最小和最大整数和小数位数的 setter，以初始化本类中的字段。这些字段是版本 3 中新增的。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 1，表示流是由 JDK 1.1 编写的，则将 <code>useExponentialNotation</code> 初始化为 false，因为它在 JDK 1.1 中不存在。
     * <li>
     * 将 <code>serialVersionOnStream</code> 设置为允许的最大值，以便如果再次流化此对象，可以正确地进行默认序列化。
     * </ol>
     *
     * <p>版本 2 之前的流将没有前缀模式变量 <code>posPrefixPattern</code> 等。因此，它们将被初始化为 <code>null</code>，这意味着前缀字符串将被视为字面值。这正是我们想要的，因为它对应于版本 2 之前的行为。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        digitList = new DigitList();

        // 当实例被反序列化时，我们强制进行完整的快速路径重新初始化。参见 clone() 中关于 fastPathCheckNeeded 的注释。
        fastPathCheckNeeded = true;
        isFastPath = false;
        fastPathData = null;

        if (serialVersionOnStream < 4) {
            setRoundingMode(RoundingMode.HALF_EVEN);
        } else {
            setRoundingMode(getRoundingMode());
        }

        // 我们只需要检查最大计数，因为 NumberFormat.readObject 已经确保最大计数大于最小计数。
        if (super.getMaximumIntegerDigits() > DOUBLE_INTEGER_DIGITS ||
            super.getMaximumFractionDigits() > DOUBLE_FRACTION_DIGITS) {
            throw new InvalidObjectException("数字计数超出范围");
        }
        if (serialVersionOnStream < 3) {
            setMaximumIntegerDigits(super.getMaximumIntegerDigits());
            setMinimumIntegerDigits(super.getMinimumIntegerDigits());
            setMaximumFractionDigits(super.getMaximumFractionDigits());
            setMinimumFractionDigits(super.getMinimumFractionDigits());
        }
        if (serialVersionOnStream < 1) {
            // 没有指数字段
            useExponentialNotation = false;
        }
        serialVersionOnStream = currentSerialVersion;
    }

    //----------------------------------------------------------------------
    // 实例变量
    //----------------------------------------------------------------------


                private transient DigitList digitList = new DigitList();

    /**
     * 用于格式化正数时的前缀符号，例如 "+"。
     *
     * @serial
     * @see #getPositivePrefix
     */
    private String  positivePrefix = "";

    /**
     * 用于格式化正数时的后缀符号。
     * 通常为空字符串。
     *
     * @serial
     * @see #getPositiveSuffix
     */
    private String  positiveSuffix = "";

    /**
     * 用于格式化负数时的前缀符号，例如 "-"。
     *
     * @serial
     * @see #getNegativePrefix
     */
    private String  negativePrefix = "-";

    /**
     * 用于格式化负数时的后缀符号。
     * 通常为空字符串。
     *
     * @serial
     * @see #getNegativeSuffix
     */
    private String  negativeSuffix = "";

    /**
     * 非负数的前缀模式。此变量对应于 <code>positivePrefix</code>。
     *
     * <p>此模式由 <code>expandAffix()</code> 方法扩展为 <code>positivePrefix</code>，以更新后者以反映 <code>symbols</code> 的变化。
     * 如果此变量为 <code>null</code>，则 <code>positivePrefix</code> 被视为一个不会因 <code>symbols</code> 变化而变化的字面值。
     * 对于从流中恢复的版本早于流版本 2 的 <code>DecimalFormat</code> 对象，此变量始终为 <code>null</code>。
     *
     * @serial
     * @since 1.3
     */
    private String posPrefixPattern;

    /**
     * 非负数的后缀模式。此变量对应于 <code>positiveSuffix</code>。此变量类似于 <code>posPrefixPattern</code>；请参阅该变量以获取更多文档。
     *
     * @serial
     * @since 1.3
     */
    private String posSuffixPattern;

    /**
     * 负数的前缀模式。此变量对应于 <code>negativePrefix</code>。此变量类似于 <code>posPrefixPattern</code>；请参阅该变量以获取更多文档。
     *
     * @serial
     * @since 1.3
     */
    private String negPrefixPattern;

    /**
     * 负数的后缀模式。此变量对应于 <code>negativeSuffix</code>。此变量类似于 <code>posPrefixPattern</code>；请参阅该变量以获取更多文档。
     *
     * @serial
     * @since 1.3
     */
    private String negSuffixPattern;

    /**
     * 用于百分比、千分比等的乘数。
     *
     * @serial
     * @see #getMultiplier
     */
    private int     multiplier = 1;

    /**
     * 数字整数部分中每组数字之间的分组分隔符的数量。如果 <code>NumberFormat.groupingUsed</code> 为 true，则必须大于 0。
     *
     * @serial
     * @see #getGroupingSize
     * @see java.text.NumberFormat#isGroupingUsed
     */
    private byte    groupingSize = 3;  // 不变，如果使用千位分隔符则 > 0

    /**
     * 如果为 true，则在格式化数字时，即使数字的小数部分为零，也会强制显示小数分隔符。
     *
     * @serial
     * @see #isDecimalSeparatorAlwaysShown
     */
    private boolean decimalSeparatorAlwaysShown = false;

    /**
     * 如果为 true，则解析时尽可能返回 BigDecimal。
     *
     * @serial
     * @see #isParseBigDecimal
     * @since 1.5
     */
    private boolean parseBigDecimal = false;

    /**
     * 如果此对象表示货币格式，则为 true。这决定了是否使用货币小数分隔符而不是普通的小数分隔符。
     */
    private transient boolean isCurrencyFormat = false;

    /**
     * 此格式使用的 <code>DecimalFormatSymbols</code> 对象。它包含用于格式化数字的符号，例如分组分隔符、小数分隔符等。
     *
     * @serial
     * @see #setDecimalFormatSymbols
     * @see java.text.DecimalFormatSymbols
     */
    private DecimalFormatSymbols symbols = null; // LIU new DecimalFormatSymbols();

    /**
     * 如果为 true，则在格式化数字时强制使用指数（即科学）表示法。
     *
     * @serial
     * @since 1.2
     */
    private boolean useExponentialNotation;  // 在 Java 2 平台 v.1.2 中新增持久化

    /**
     * 描述正数前缀字符串的 FieldPositions。这是延迟创建的。需要时使用 <code>getPositivePrefixFieldPositions</code>。
     */
    private transient FieldPosition[] positivePrefixFieldPositions;

    /**
     * 描述正数后缀字符串的 FieldPositions。这是延迟创建的。需要时使用 <code>getPositiveSuffixFieldPositions</code>。
     */
    private transient FieldPosition[] positiveSuffixFieldPositions;

    /**
     * 描述负数前缀字符串的 FieldPositions。这是延迟创建的。需要时使用 <code>getNegativePrefixFieldPositions</code>。
     */
    private transient FieldPosition[] negativePrefixFieldPositions;

    /**
     * 描述负数后缀字符串的 FieldPositions。这是延迟创建的。需要时使用 <code>getNegativeSuffixFieldPositions</code>。
     */
    private transient FieldPosition[] negativeSuffixFieldPositions;

    /**
     * 当数字以指数表示法格式化时，用于显示指数的最小数字数量。如果 <code>useExponentialNotation</code> 不为 true，则忽略此字段。
     *
     * @serial
     * @since 1.2
     */
    private byte    minExponentDigits;       // 在 Java 2 平台 v.1.2 中新增持久化

    /**
     * <code>BigInteger</code> 或 <code>BigDecimal</code> 数字整数部分允许的最大数字数量。
     * <code>maximumIntegerDigits</code> 必须大于或等于 <code>minimumIntegerDigits</code>。
     *
     * @serial
     * @see #getMaximumIntegerDigits
     * @since 1.5
     */
    private int    maximumIntegerDigits = super.getMaximumIntegerDigits();


                /**
     * 允许在 <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的整数部分中的最小数字位数。
     * <code>minimumIntegerDigits</code> 必须小于或等于 <code>maximumIntegerDigits</code>。
     *
     * @serial
     * @see #getMinimumIntegerDigits
     * @since 1.5
     */
    private int    minimumIntegerDigits = super.getMinimumIntegerDigits();

    /**
     * 允许在 <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的小数部分中的最大数字位数。
     * <code>maximumFractionDigits</code> 必须大于或等于 <code>minimumFractionDigits</code>。
     *
     * @serial
     * @see #getMaximumFractionDigits
     * @since 1.5
     */
    private int    maximumFractionDigits = super.getMaximumFractionDigits();

    /**
     * 允许在 <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的小数部分中的最小数字位数。
     * <code>minimumFractionDigits</code> 必须小于或等于 <code>maximumFractionDigits</code>。
     *
     * @serial
     * @see #getMinimumFractionDigits
     * @since 1.5
     */
    private int    minimumFractionDigits = super.getMinimumFractionDigits();

    /**
     * 在此 DecimalFormat 中使用的 {@link java.math.RoundingMode}。
     *
     * @serial
     * @since 1.6
     */
    private RoundingMode roundingMode = RoundingMode.HALF_EVEN;

    // ------ 用于双精度算法的快速路径的 DecimalFormat 字段 ------

    /**
     * 用于存储快速路径算法中使用的数据的辅助内部工具类。几乎所有与快速路径相关的字段都封装在此类中。
     *
     * 任何 {@code DecimalFormat} 实例都有一个 {@code fastPathData} 引用字段，除非实例的属性使得该实例处于“快速路径”状态，并且在该状态下至少进行了一次格式化调用，否则该字段为 null。
     *
     * 几乎所有字段都仅与“快速路径”状态相关，并且在实例属性更改之前不会更改。
     *
     * {@code firstUsedIndex} 和 {@code lastFreeIndex} 是在调用 {@code fastDoubleFormat} 时使用和修改的唯一两个字段。
     *
     */
    private static class FastPathData {
        // --- 在快速路径中使用的临时字段，由多个方法共享。

        /** 格式化结果末尾的第一个未使用索引。 */
        int lastFreeIndex;

        /** 格式化结果开头的第一个使用索引。 */
        int firstUsedIndex;

        // --- 与快速路径状态相关的状态字段。仅因属性更改而变化。仅由 checkAndSetFastPathStatus() 设置。

        /** 本地零和默认零表示之间的差异。 */
        int  zeroDelta;

        /** 本地分组分隔符字符。 */
        char groupingChar;

        /** 格式化结果的最后一个整数位的固定索引位置。 */
        int integralLastIndex;

        /** 格式化结果的第一个小数位的固定索引位置。 */
        int fractionalFirstIndex;

        /** 取决于小数|货币状态的小数常量。 */
        double fractionalScaleFactor;
        int fractionalMaxIntBound;

        /** 将包含格式化结果的字符数组缓冲区。 */
        char[] fastPathContainer;

        /** 为了效率，以字符数组形式记录的后缀。 */
        char[] charsPositivePrefix;
        char[] charsNegativePrefix;
        char[] charsPositiveSuffix;
        char[] charsNegativeSuffix;
        boolean positiveAffixesRequired = true;
        boolean negativeAffixesRequired = true;
    }

    /** 实例的快速路径状态。逻辑状态。 */
    private transient boolean isFastPath = false;

    /** 标记下一次格式化调用时需要检查并重新初始化快速路径状态。 */
    private transient boolean fastPathCheckNeeded = true;

    /** DecimalFormat 对其 FastPathData 的引用。 */
    private transient FastPathData fastPathData;

    //----------------------------------------------------------------------

    static final int currentSerialVersion = 4;

    /**
     * 说明写入的版本的内部序列化版本。
     * 可能的值包括：
     * <ul>
     * <li><b>0</b>（默认）：Java 2 平台 v1.2 之前的版本
     * <li><b>1</b>：1.2 版本，包括两个新字段 <code>useExponentialNotation</code> 和 <code>minExponentDigits</code>。
     * <li><b>2</b>：1.3 及更高版本，增加了四个新字段： <code>posPrefixPattern</code>，<code>posSuffixPattern</code>，<code>negPrefixPattern</code> 和 <code>negSuffixPattern</code>。
     * <li><b>3</b>：1.5 及更高版本，增加了五个新字段： <code>maximumIntegerDigits</code>，<code>minimumIntegerDigits</code>，<code>maximumFractionDigits</code>，<code>minimumFractionDigits</code> 和 <code>parseBigDecimal</code>。
     * <li><b>4</b>：1.6 及更高版本，增加了一个新字段： <code>roundingMode</code>。
     * </ul>
     * @since 1.2
     * @serial
     */
    private int serialVersionOnStream = currentSerialVersion;

    //----------------------------------------------------------------------
    // 常量
    //----------------------------------------------------------------------

    // ------ 双精度快速路径常量 ------

    /** 适用于快速路径算法的最大有效整数值。 */
    private static final double MAX_INT_AS_DOUBLE = (double) Integer.MAX_VALUE;

    /**
     * 在快速路径方法中用于收集数字的数字数组。
     * 使用 3 个常量字符数组确保非常快速地收集数字。
     */
    private static class DigitArrays {
        static final char[] DigitOnes1000 = new char[1000];
        static final char[] DigitTens1000 = new char[1000];
        static final char[] DigitHundreds1000 = new char[1000];


                    // 初始化按需持有者类模式用于数字数组
        static {
            int tenIndex = 0;
            int hundredIndex = 0;
            char digitOne = '0';
            char digitTen = '0';
            char digitHundred = '0';
            for (int i = 0;  i < 1000; i++ ) {

                DigitOnes1000[i] = digitOne;
                if (digitOne == '9')
                    digitOne = '0';
                else
                    digitOne++;

                DigitTens1000[i] = digitTen;
                if (i == (tenIndex + 9)) {
                    tenIndex += 10;
                    if (digitTen == '9')
                        digitTen = '0';
                    else
                        digitTen++;
                }

                DigitHundreds1000[i] = digitHundred;
                if (i == (hundredIndex + 99)) {
                    digitHundred++;
                    hundredIndex += 100;
                }
            }
        }
    }
    // ------ 双精度常量的快速路径结束 ------

    // 用于程序化（非本地化）模式的字符常量。
    private static final char       PATTERN_ZERO_DIGIT         = '0';
    private static final char       PATTERN_GROUPING_SEPARATOR = ',';
    private static final char       PATTERN_DECIMAL_SEPARATOR  = '.';
    private static final char       PATTERN_PER_MILLE          = '\u2030';
    private static final char       PATTERN_PERCENT            = '%';
    private static final char       PATTERN_DIGIT              = '#';
    private static final char       PATTERN_SEPARATOR          = ';';
    private static final String     PATTERN_EXPONENT           = "E";
    private static final char       PATTERN_MINUS              = '-';

    /**
     * CURRENCY_SIGN 是货币的标准 Unicode 符号。它在模式中使用，并被替换为货币符号，
     * 或者如果它被加倍，则被替换为国际货币符号。如果在模式中看到 CURRENCY_SIGN，
     * 则小数分隔符将被替换为货币小数分隔符。
     *
     * CURRENCY_SIGN 不进行本地化。
     */
    private static final char       CURRENCY_SIGN = '\u00A4';

    private static final char       QUOTE = '\'';

    private static FieldPosition[] EmptyFieldPositionArray = new FieldPosition[0];

    // Java double 类型的整数和小数位数的上限
    static final int DOUBLE_INTEGER_DIGITS  = 309;
    static final int DOUBLE_FRACTION_DIGITS = 340;

    // BigDecimal 和 BigInteger 类型的整数和小数位数的上限
    static final int MAXIMUM_INTEGER_DIGITS  = Integer.MAX_VALUE;
    static final int MAXIMUM_FRACTION_DIGITS = Integer.MAX_VALUE;

    // 声明 JDK 1.1 序列化兼容性。
    static final long serialVersionUID = 864413376551465018L;
}
