
/*
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
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
 * <code>DecimalFormat</code> 是 <code>NumberFormat</code> 的一个具体子类，用于格式化十进制数字。它具有多种功能，旨在使解析和格式化任何语言环境中的数字成为可能，包括对西文、阿拉伯文和印度数字的支持。它还支持不同类型的数字，包括整数（123）、定点数（123.4）、科学记数法（1.23E4）、百分比（12%）和货币金额（$123）。所有这些都可以进行本地化。
 *
 * <p>要为特定语言环境（包括默认语言环境）获取 <code>NumberFormat</code>，请调用 <code>NumberFormat</code> 的工厂方法之一，例如 <code>getInstance()</code>。通常，不要直接调用 <code>DecimalFormat</code> 的构造函数，因为 <code>NumberFormat</code> 工厂方法可能会返回 <code>DecimalFormat</code> 以外的子类。如果需要自定义格式对象，可以这样做：
 *
 * <blockquote><pre>
 * NumberFormat f = NumberFormat.getInstance(loc);
 * if (f instanceof DecimalFormat) {
 *     ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(true);
 * }
 * </pre></blockquote>
 *
 * <p><code>DecimalFormat</code> 包含一个 <em>模式</em> 和一组 <em>符号</em>。模式可以直接使用 <code>applyPattern()</code> 设置，也可以通过 API 方法间接设置。符号存储在 <code>DecimalFormatSymbols</code> 对象中。使用 <code>NumberFormat</code> 工厂方法时，模式和符号从本地化的 <code>ResourceBundle</code> 中读取。
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
 *         除 &#92;uFFFE、&#92;uFFFF 和特殊字符外的任何 Unicode 字符
 * <i>Suffix:</i>
 *         除 &#92;uFFFE、&#92;uFFFF 和特殊字符外的任何 Unicode 字符
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
 * <p><code>DecimalFormat</code> 模式包含一个正模式和一个负模式，例如 <code>"#,##0.00;(#,##0.00)"</code>。每个子模式都有一个前缀、数字部分和后缀。负子模式是可选的；如果省略，则使用带本地化减号（在大多数语言环境中为 <code>'-'</code>）的正子模式作为负子模式。也就是说，<code>"0.00"</code> 等同于 <code>"0.00;-0.00"</code>。如果指定了显式的负子模式，它仅用于指定负前缀和后缀；数字位数、最小位数等特性与正模式相同。这意味着 <code>"#,##0.0#;(#)"</code> 产生的行为与 <code>"#,##0.0#;(#,##0.0#)"</code> 完全相同。
 *
 * <p>前缀、后缀以及用于无穷大、数字、千位分隔符、小数点等的符号可以设置为任意值，并在格式化时正确显示。但是，必须确保这些符号和字符串不冲突，否则解析将不可靠。例如，正负前缀或后缀必须是不同的，以便 <code>DecimalFormat.parse()</code> 能够区分正数和负数。（如果它们相同，则 <code>DecimalFormat</code> 会表现得好像没有指定负子模式一样。）另一个例子是小数点和千位分隔符应该是不同的字符，否则解析将不可能。
 *
 * <p>分组分隔符通常用于千位，但在某些国家/地区，它用于万位。分组间隔是分组字符之间的固定数字位数，例如 3 用于 100,000,000 或 4 用于 1,0000,0000。如果提供了一个包含多个分组字符的模式，则最后一个分组字符与整数部分末尾之间的间隔是使用的间隔。因此 <code>"#,##,###,####"</code> == <code>"######,####"</code> == <code>"##,####,####"</code>。
 *
 * <h4>特殊模式字符</h4>
 *
 * <p>模式中的许多字符是按字面意思使用的；在解析时进行匹配，在格式化时不变地输出。特殊字符代表其他字符、字符串或字符类。如果要将它们作为前缀或后缀中的字面值显示，通常必须进行引用，除非另有说明。
 *
 * <p>以下字符用于非本地化的模式。本地化模式使用此格式化程序的 <code>DecimalFormatSymbols</code> 对象中的相应字符，这些字符将失去其特殊状态。货币符号和引号是两个例外，它们不会本地化。
 *
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart showing symbol,
 *  location, localized, and meaning.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>符号
 *          <th align=left>位置
 *          <th align=left>本地化？
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
 *          <td>科学记数法中的尾数和指数之间的分隔符。<em>在前缀或后缀中不需要引用。</em>
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
 *          <td>乘以 1000 并显示为千分比
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>&#164;</code> (<code>&#92;u00A4</code>)
 *          <td>前缀或后缀
 *          <td>否
 *          <td>货币符号，由货币符号替换。如果加倍，则由国际货币符号替换。如果模式中存在，则使用货币小数点而不是小数点。
 *     <tr valign=top>
 *          <td><code>'</code>
 *          <td>前缀或后缀
 *          <td>否
 *          <td>用于在前缀或后缀中引用特殊字符，例如 <code>"'#'#"</code> 将 123 格式化为 <code>"#123"</code>。要创建单引号本身，可以使用两个连续的单引号： <code>"# o''clock"</code>。
 * </table>
 * </blockquote>
 *
 * <h4>科学记数法</h4>
 *
 * <p>科学记数法中的数字表示为尾数和 10 的幂的乘积，例如 1234 可以表示为 1.234 x 10^3。尾数通常在 1.0 &le; x {@literal <} 10.0 范围内，但不一定如此。
 * <code>DecimalFormat</code> 可以通过模式指示格式化和解析科学记数法；目前没有工厂方法可以创建科学记数法格式。在模式中，指数字符后面紧跟一个或多个数字字符表示科学记数法。例如： <code>"0.###E0"</code> 将数字 1234 格式化为 <code>"1.234E3"</code>。
 *
 * <ul>
 * <li>指数字符后的数字字符数给出了最小指数位数。没有最大值。负指数使用本地化减号格式化，<em>而不是</em>模式中的前缀和后缀。这允许使用模式，例如 <code>"0.###E0 m/s"</code>。
 *
 * <li>最小和最大整数位数一起解释：
 *
 * <ul>
 * <li>如果最大整数位数大于最小整数位数且大于 1，则强制指数为最大整数位数的倍数，并将最小整数位数解释为 1。最常见的用法是生成 <em>工程记数法</em>，其中指数为 3 的倍数，例如 <code>"##0.#####E0"</code>。使用此模式，数字 12345 格式化为 <code>"12.345E3"</code>，123456 格式化为 <code>"123.456E3"</code>。
 *
 * <li>否则，通过调整指数来实现最小整数位数。例如：0.00123 使用 <code>"00.###E0"</code> 格式化为 <code>"12.3E-4"</code>。
 * </ul>
 *
 * <li>尾数中的有效数字数是 <em>最小整数</em> 和 <em>最大小数</em> 位数的总和，不受最大整数位数的影响。例如，12345 使用 <code>"##0.##E0"</code> 格式化为 <code>"12.3E3"</code>。要显示所有数字，可以将有效数字数设置为零。有效数字数不影响解析。
 *
 * <li>指数模式中不得包含分组分隔符。
 * </ul>
 *
 * <h4>四舍五入</h4>
 *
 * <code>DecimalFormat</code> 提供了在 {@link java.math.RoundingMode} 中定义的四舍五入模式进行格式化。默认情况下，它使用 {@link java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}。
 *
 * <h4>数字</h4>
 *
 * 在格式化时，<code>DecimalFormat</code> 使用 <code>DecimalFormatSymbols</code> 对象中定义的本地化零数字开始的十个连续字符作为数字。在解析时，除了这些数字外，还识别所有 Unicode 十进制数字，如 {@link Character#digit Character.digit} 所定义。
 *
 * <h4>特殊值</h4>
 *
 * <p><code>NaN</code> 被格式化为一个字符串，通常包含单个字符 <code>&#92;uFFFD</code>。此字符串由 <code>DecimalFormatSymbols</code> 对象确定。这是唯一一个不使用前缀和后缀的值。
 *
 * <p>无穷大被格式化为一个字符串，通常包含单个字符 <code>&#92;u221E</code>，并应用正负前缀和后缀。无穷大字符串由 <code>DecimalFormatSymbols</code> 对象确定。
 *
 * <p>负零 (<code>"-0"</code>) 解析为
 * <ul>
 * <li><code>BigDecimal(0)</code> 如果 <code>isParseBigDecimal()</code> 为 true，
 * <li><code>Long(0)</code> 如果 <code>isParseBigDecimal()</code> 为 false 且 <code>isParseIntegerOnly()</code> 为 true，
 * <li><code>Double(-0.0)</code> 如果 <code>isParseBigDecimal()</code> 和 <code>isParseIntegerOnly()</code> 均为 false。
 * </ul>
 *
 * <h4><a name="synchronization">同步</a></h4>
 *
 * <p>
 * 十进制格式通常不是同步的。
 * 建议为每个线程创建单独的格式实例。
 * 如果多个线程同时访问格式，必须在外部进行同步。
 *
 * <h4>示例</h4>
 *
 * <blockquote><pre>{@code
 * <strong>// 打印每个语言环境的本地化数字、整数、货币和百分比格式</strong>
 * Locale[] locales = NumberFormat.getAvailableLocales();
 * double myNumber = -1234.56;
 * NumberFormat form;
 * for (int j = 0; j < 4; ++j) {
 *     System.out.println("FORMAT");
 *     for (int i = 0; i < locales.length; ++i) {
 *         if (locales[i].getCountry().length() == 0) {
 *            continue; // 跳过仅语言的语言环境
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
     * 使用默认模式和默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的符号创建一个 DecimalFormat。
     * 当国际化不是主要关注点时，这是一种方便的方式。
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

        // 总是在设置符号后应用模式
        this.symbols = DecimalFormatSymbols.getInstance(def);
        applyPattern(all[0], false);
    }


    /**
     * 使用给定的模式和默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的符号创建一个 DecimalFormat。
     * 当国际化不是主要关注点时，这是一种方便的方式。
     * <p>
     * 要为给定的本地获取标准格式，请使用 NumberFormat 上的工厂方法，如 getNumberInstance。这些工厂将
     * 返回给定本地最合适的 NumberFormat 子类。
     *
     * @param pattern 一个非本地化的模式字符串。
     * @exception NullPointerException 如果 <code>pattern</code> 为 null
     * @exception IllegalArgumentException 如果给定的模式无效。
     * @see java.text.NumberFormat#getInstance
     * @see java.text.NumberFormat#getNumberInstance
     * @see java.text.NumberFormat#getCurrencyInstance
     * @see java.text.NumberFormat#getPercentInstance
     */
    public DecimalFormat(String pattern) {
        // 总是在设置符号后应用模式
        this.symbols = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        applyPattern(pattern, false);
    }


    /**
     * 使用给定的模式和符号创建一个 DecimalFormat。
     * 当需要完全自定义格式的行为时，使用此构造函数。
     * <p>
     * 要为给定的本地获取标准格式，请使用 NumberFormat 上的工厂方法，如
     * getInstance 或 getCurrencyInstance。如果只需要对标准格式进行轻微调整，
     * 可以修改由 NumberFormat 工厂方法返回的格式。
     *
     * @param pattern 一个非本地化的模式字符串
     * @param symbols 要使用的符号集
     * @exception NullPointerException 如果任何给定的参数为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     * @see java.text.NumberFormat#getInstance
     * @see java.text.NumberFormat#getNumberInstance
     * @see java.text.NumberFormat#getCurrencyInstance
     * @see java.text.NumberFormat#getPercentInstance
     * @see java.text.DecimalFormatSymbols
     */
    public DecimalFormat (String pattern, DecimalFormatSymbols symbols) {
        // 总是在设置符号后应用模式
        this.symbols = (DecimalFormatSymbols)symbols.clone();
        applyPattern(pattern, false);
    }


    // 重写
    /**
     * 格式化一个数字并将结果文本追加到给定的字符串缓冲区。
     * 数字可以是 {@link java.lang.Number} 的任何子类。
     * <p>
     * 此实现使用允许的最大精度。
     * @param number 要格式化的数字
     * @param toAppendTo 要追加格式化文本的 <code>StringBuffer</code>
     * @param pos 输入：对齐字段，如果需要的话。
     *            输出：对齐字段的偏移量。
     * @return 传递的 <code>toAppendTo</code> 的值
     * @exception IllegalArgumentException 如果 <code>number</code> 为 null 或不是 <code>Number</code> 的实例。
     * @exception NullPointerException 如果 <code>toAppendTo</code> 或 <code>pos</code> 为 null
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
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
     * 格式化一个 double 以生成字符串。
     * @param number 要格式化的 double
     * @param result 文本要追加到的位置
     * @param fieldPosition 输入：对齐字段，如果需要的话。
     *                     输出：对齐字段的偏移量。
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @return 格式化的数字字符串
     * @see java.text.FieldPosition
     */
    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
        // 如果 fieldPosition 是 DontCareFieldPosition 实例，我们可以尝试使用快速路径代码。
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
     * 格式化一个 double 以生成字符串。
     * @param number 要格式化的 double
     * @param result 文本要追加到的位置
     * @param delegate 通知子字段的位置
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
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

        /* 检测 double 是否为负数很容易，但 -0.0 除外。这是一个具有零尾数（和指数）但负号位的 double。
         * 从语义上讲，它与正号位的零是不同的，这种区别对某些计算很重要。然而，检测它有点棘手，因为
         * (-0.0 == 0.0) 和 !(-0.0 < 0.0)。那么，你可能会问，它如何与 +0.0 表现不同？1/(-0.0) == -Infinity。
         * 正确检测 -0.0 是解决 4106658、4106667 和 4147706 问题的关键。Liu 7/6/98。
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

        // 此时我们保证是一个非负有限数。
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
     * 格式化一个 long 以生成字符串。
     * @param number 要格式化的 long
     * @param result 文本要追加到的位置
     * @param fieldPosition 输入：对齐字段，如果需要的话。
     *                     输出：对齐字段的偏移量。
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
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
     * 格式化一个 long 以生成字符串。
     * @param number 要格式化的 long
     * @param result 文本要追加到的位置
     * @param delegate 通知子字段的位置
     * @return 格式化的数字字符串
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
     */
    private StringBuffer format(long number, StringBuffer result,
                               FieldDelegate delegate) {
        boolean isNegative = (number < 0);
        if (isNegative) {
            number = -number;
        }

        // 通常，long 值总是表示实际的有限数，因此我们不必检查 +/- Infinity 或 NaN。但是，
        // 有一种情况需要小心：乘数可以将接近 MIN_VALUE 或 MAX_VALUE 的数推到合法范围之外。我们在乘法之前检查这一点，
        // 如果发生这种情况，我们使用 BigInteger 代替。
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
     * 格式化一个 BigDecimal 以生成字符串。
     * @param number 要格式化的 BigDecimal
     * @param result 文本要追加到的位置
     * @param fieldPosition 输入：对齐字段，如果需要的话。
     *                     输出：对齐字段的偏移量。
     * @return 格式化的数字字符串
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.FieldPosition
     */
    private StringBuffer format(BigDecimal number, StringBuffer result,
                                FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        return format(number, result, fieldPosition.getFieldDelegate());
    }

    /**
     * 格式化一个 BigDecimal 以生成字符串。
     * @param number 要格式化的 BigDecimal
     * @param result 文本要追加到的位置
     * @param delegate 通知子字段的位置
     * @exception ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
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
     * @param result    附加文本的位置
     * @param fieldPosition    输入：对齐字段，如果需要的话。
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
     * @param result    附加文本的位置
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
     * 格式化一个对象以生成一个 <code>AttributedCharacterIterator</code>。
     * 可以使用返回的 <code>AttributedCharacterIterator</code>
     * 构建结果字符串，以及确定结果字符串的信息。
     * <p>
     * 每个属性键的类型为 <code>NumberFormat.Field</code>，属性值与属性键相同。
     *
     * @exception NullPointerException 如果 obj 为 null。
     * @exception IllegalArgumentException 当 Format 无法格式化给定对象时。
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
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

    /* 快速路径格式化将在满足以下条件时用于 format(double ...) 方法（参见 checkAndSetFastPathStatus()）：
     * - 仅当实例属性满足预定义条件时。
     * - 要格式化的 double 的绝对值 <= Integer.MAX_VALUE。
     *
     * 基本方法是将 double 值的二进制到十进制转换分为两个阶段：
     * * double 的整数部分的转换。
     * * double 的小数部分的转换（限于两到三位）。
     *
     * double 的整数部分的隔离和转换是直接的。小数部分的转换更为微妙，依赖于 double 到所需十进制精度的某些舍入属性。使用 BigDecimal 的术语，当 double 值的量级小于 Integer.MAX_VALUE 且舍入到最近的偶数且目标格式有两到三位 *scale*（小数点后的位数）时，应用此快速路径算法。
     *
     * 在最近偶数的舍入策略下，返回的结果是目标格式（在这种情况下为十进制）中最接近输入值（在这种情况下为二进制）的确切数值的数字字符串。如果两个目标格式的数字距离相等，则返回最后一个数字为偶数的那个。为了计算这样一个正确舍入的值，需要咨询返回数字位置之外的一些数字信息。
     *
     * 通常，需要在返回的数字位置之外计算一个保护位、一个舍入位和一个粘性 *位*。如果丢弃的输入部分足够大，返回的数字字符串将递增。在最近偶数的舍入中，递增的阈值发生在数字之间的中点附近。粘性位记录新格式中确切输入值的任何剩余尾数；粘性位仅在接近中点的舍入情况下咨询。
     *
     * 给定数字和位值的计算，舍入就变成了一个查找表问题。对于十进制，偶数/奇数的情况如下：
     *
     * 最后一位   舍入位   粘性位
     * 6      5       0      => 6   // 恰好在中点，返回偶数位。
     * 6      5       1      => 7   // 稍微超过中点，向上舍入。
     * 7      5       0      => 8   // 恰好在中点，向上舍入到偶数。
     * 7      5       1      => 8   // 稍微超过中点，向上舍入。
     * 其他偶数和奇数的最后返回位有类似的情况。
     *
     * 然而，小于 0.5 的十进制负幂在二进制分数中 *不能* 精确表示。特别是，0.005（两位小数的舍入限制）和 0.0005（三位小数的舍入限制）不能表示。因此，对于接近这些情况的输入值，粘性位已知被设置，这将舍入逻辑简化为：
     *
     * 最后一位   舍入位   粘性位
     * 6      5       1      => 7   // 稍微超过中点，向上舍入。
     * 7      5       1      => 8   // 稍微超过中点，向上舍入。
     *
     * 换句话说，如果舍入位是 5，粘性位已知被设置。如果舍入位不是 5，粘性位不相关。因此，关于是否递增目标 *十进制* 值的一些逻辑可以基于对 *二进制* 输入数字的二进制计算的测试。
     */

    /**
     * 检查此实例使用快速路径的有效性。如果快速路径对这个实例有效，将快速路径状态设置为 true 并根据需要初始化快速路径实用字段。
     *
     * 该方法应很少调用，否则将破坏快速路径性能。这意味着避免频繁更改实例的属性，因为对于大多数属性，每次更改发生时，下次格式化调用时都需要调用此方法。
     *
     * 快速路径规则：
     *  类似于默认的 DecimalFormat 实例化情况。
     *  更精确地说：
     *  - HALF_EVEN 舍入模式，
     *  - isGroupingUsed() 为 true，
     *  - 分组大小为 3，
     *  - 倍数为 1，
     *  - 小数点不强制显示，
     *  - 不使用指数表示法，
     *  - 最小整数位数恰好为 1 且最大整数位数至少为 10
     *  - 对于小数位数，使用默认情况中的确切值：
     *     货币：最小 = 最大 = 2。
     *     十进制：最小 = 0。最大 = 3。
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

            // 快速路径算法对最小整数位数和最大整数位数进行了半硬编码。
            isFastPath = ((minimumIntegerDigits == 1) &&
                          (maximumIntegerDigits >= 10));

            // 快速路径算法对最小小数位数和最大小数位数进行了硬编码。
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
         * 在成功检查快速路径条件并设置快速路径数据后返回 true。返回值用于
         * fastFormat() 方法决定是否调用 resetFastPathData 方法重新初始化快速路径数据
         * 或者它是否已经在本方法中初始化。
         */
        return true;
    }

    private void resetFastPathData(boolean fastPathWasOn) {
        // 由于一些实例属性可能已更改但仍处于快速路径情况下，无论如何都需要重新初始化 fastPathData。
        if (isFastPath) {
            // 如果尚未实例化 fastPathData，则需要实例化。
            if (fastPathData == null) {
                fastPathData = new FastPathData();
            }

            // 设置格式化时使用的特定于区域的常量。
            // '0' 是我们默认的零表示。
            fastPathData.zeroDelta = symbols.getZeroDigit() - '0';
            fastPathData.groupingChar = symbols.getGroupingSeparator();

            // 设置与货币/十进制模式相关的分数常量。
            fastPathData.fractionalMaxIntBound = (isCurrencyFormat)
                    ? 99 : 999;
            fastPathData.fractionalScaleFactor = (isCurrencyFormat)
                    ? 100.0d : 1000.0d;

            // 记录是否需要添加前缀或后缀
            fastPathData.positiveAffixesRequired
                    = (positivePrefix.length() != 0)
                        || (positiveSuffix.length() != 0);
            fastPathData.negativeAffixesRequired
                    = (negativePrefix.length() != 0)
                        || (negativeSuffix.length() != 0);

            // 创建一个缓存的 char 容器，最大可能大小。
            int maxNbIntegralDigits = 10;
            int maxNbGroups = 3;
            int containerSize
                    = Math.max(positivePrefix.length(), negativePrefix.length())
                    + maxNbIntegralDigits + maxNbGroups + 1
                    + maximumFractionDigits
                    + Math.max(positiveSuffix.length(), negativeSuffix.length());

            fastPathData.fastPathContainer = new char[containerSize];

            // 设置前缀和后缀 char 数组常量。
            fastPathData.charsPositiveSuffix = positiveSuffix.toCharArray();
            fastPathData.charsNegativeSuffix = negativeSuffix.toCharArray();
            fastPathData.charsPositivePrefix = positivePrefix.toCharArray();
            fastPathData.charsNegativePrefix = negativePrefix.toCharArray();

            // 设置整数和小数位的固定索引位置。
            // 在缓存的结果容器中设置小数点。
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
            // 之前的状态是快速路径，现在不再是。
            // 重置缓存的数组常量。
            fastPathData.fastPathContainer = null;
            fastPathData.charsPositiveSuffix = null;
            fastPathData.charsNegativeSuffix = null;
            fastPathData.charsPositivePrefix = null;
            fastPathData.charsNegativePrefix = null;
        }
    }


                /**
     * 如果需要对 {@code scaledFractionalPartAsInt} 进行向上取整，则返回 true，
     * 否则返回 false。
     *
     * 这是一个实用方法，它对传递的分数值在缩放小数点处（货币情况为 2 位，小数情况为 3 位）进行正确的半偶取整决策，
     * 当缩放小数点后的近似分数部分恰好为 0.5d 时。这是通过对 {@code fractionalPart} 浮点值进行精确计算来完成的。
     *
     * 该方法仅由私有的 {@code fastDoubleFormat} 方法调用。
     *
     * 用于精确计算的算法包括：
     *
     * <b><i>FastTwoSum</i></b> 算法，由 T.J.Dekker 提出，描述在论文
     * "<i>A  Floating-Point   Technique  for  Extending  the  Available
     * Precision</i>" 由 Dekker 和 "<i>Adaptive  Precision Floating-Point
     * Arithmetic and Fast Robust Geometric Predicates</i>" 由 J.Shewchuk 中。
     *
     * 一种修改版的 <b><i>Sum2S</i></b> 级联求和，描述在
     * "<i>Accurate Sum and Dot Product</i>" 由 Takeshi Ogita 等人。正如 Ogita 在论文中所说，这是 Kahan-Babuska 求和算法的等效版本，因为我们按大小顺序求和。
     * 因此我们可以使用 <i>FastTwoSum</i> 算法而不是更昂贵的 Knuth 的 <i>TwoSum</i>。
     *
     * 我们这样做是为了避免使用更昂贵的精确 "<i>TwoProduct</i>" 算法，如 Shewchuk 的论文中所述。请参阅代码中的注释。
     *
     * @param  fractionalPart 用于取整决策的分数值。
     * @param scaledFractionalPartAsInt 缩放分数值的整数部分。
     *
     * @return 需要采取的半偶取整决策。
     */
    private boolean exactRoundUp(double fractionalPart,
                                 int scaledFractionalPartAsInt) {

        /* exactRoundUp() 方法仅由 fastDoubleFormat() 调用。
         * 传递参数应满足的前置条件是：
         * scaledFractionalPartAsInt ==
         *     (int) (fractionalPart * fastPathData.fractionalScaleFactor)。
         * 这由 fastDoubleFormat() 代码确保。
         */

        /* 我们首先计算 fastDoubleFormat() 在缩放分数部分上产生的舍入误差。我们通过对传递的 fractionalPart 进行精确计算来完成这一点。舍入决策将根据舍入误差做出。
         */

        /* ---- TwoProduct(fractionalPart, 缩放因子（即 1000.0d 或 100.0d）)。
         *
         * 以下是使用 Ogita 的 Sum2S 级联求和优化的传递分数部分与缩放因子的精确 "TwoProduct" 计算，该求和被适配为 Kahan-Babuska 等效版本，通过使用 FastTwoSum（更快）而不是 Knuth 的 TwoSum。
         *
         * 我们可以这样做是因为我们按从小到大的顺序求和，因此可以使用 FastTwoSum 而不会产生额外的误差。
         *
         * 精确的 "TwoProduct" 计算需要 17 次浮点运算。我们用 FastTwoSum 计算的级联求和来代替，每次涉及与 2 的幂的精确乘法。
         *
         * 这样做可以节省 4 次乘法和 1 次加法，相比使用传统的 "TwoProduct"。
         *
         * 缩放因子为 100（货币情况）或 1000（小数情况）。
         * - 当为 1000 时，我们用 (1024 - 16 - 8) = 1000 替换。
         * - 当为 100 时，我们用 (128 - 32 + 4) = 100 替换。
         * 每次与 2 的幂（1024, 128, 32, 16, 8, 4）的乘法都是精确的。
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
            // 乘以 2**n 是移位。没有舍入误差。没有误差。
            approxMax    = fractionalPart * 128.00d;
            approxMedium = - (fractionalPart * 32.00d);
            approxMin    = fractionalPart * 4.00d;
        } else {
            // 缩放因子为 1000 = 1024 - 16 - 8。
            // 乘以 2**n 是移位。没有舍入误差。没有误差。
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

        // 现在我们得到了缩放分数的舍入误差。
        double scaledFractionalRoundoff = roundoffTotal - bVirtual;

        // ---- TwoProduct(fractionalPart, 缩放因子（即 1000.0d 或 100.0d）) 结束。

        /* ---- 做出取整决策
         *
         * 我们根据舍入误差和半偶取整规则做出取整决策。
         *
         * 上面的 TwoProduct 给出了近似缩放分数的精确舍入误差，我们知道这个近似值恰好为 0.5d，因为调用者（fastDoubleFormat）已经测试过这一点。
         *
         * 决策首先来自计算出的精确舍入误差的符号。
         * - 由于是精确的舍入误差，它不能在缩放分数小于 0.5d 时为正，也不能在缩放分数大于 0.5d 时为负。这留下了以下三种情况。
         * - 为正，因此缩放分数 == 0.500....0fff ==> 向上取整。
         * - 为负，因此缩放分数 == 0.499....9fff ==> 不向上取整。
         * - 为零，因此缩放分数 == 0.5 ==> 应用半偶取整：
         *    仅当缩放分数的整数部分为奇数时才向上取整。
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

        // ---- 做出取整决策结束
    }

    /**
     * 从传递的 {@code number} 中收集整数位，同时根据需要设置分组字符。相应地更新 {@code firstUsedIndex}。
     *
     * 从 {@code backwardIndex} 位置（包含）开始向下循环。
     *
     * @param number 从中收集数字的 int 值。
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

        // 收集最后 3 位或更少的数字。
        digitsBuffer[index] = DigitArrays.DigitOnes1000[number];
        if (number > 9) {
            digitsBuffer[--index]  = DigitArrays.DigitTens1000[number];
            if (number > 99)
                digitsBuffer[--index]   = DigitArrays.DigitHundreds1000[number];
        }

        fastPathData.firstUsedIndex = index;
    }

    /**
     * 从传递的 {@code number} 中收集 2 位（货币）或 3 位（小数）的分数位，从 {@code startIndex} 位置（包含）开始。
     * 这里没有标点符号（没有分组字符）。相应地更新 {@code fastPathData.lastFreeIndex}。
     *
     *
     * @param number 从中收集数字的 int 值。
     * @param digitsBuffer 存储数字的 char 数组容器。
     * @param startIndex 从 digitsBuffer 中开始存储数字的位置。
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
            // 小数情况。百位始终收集
            digitsBuffer[index++] = DigitArrays.DigitHundreds1000[number];

            // 结尾的零不会被收集。
            if (digitOnes != '0') {
                digitsBuffer[index++] = digitTens;
                digitsBuffer[index++] = digitOnes;
            } else if (digitTens != '0')
                digitsBuffer[index++] = digitTens;

        } else
            // 这是小数模式且分数部分为零。
            // 必须从结果中移除小数点。
            index--;

        fastPathData.lastFreeIndex = index;
    }

    /**
     * 内部实用工具。
     * 将传递的 {@code prefix} 和 {@code suffix} 添加到 {@code container} 中。
     *
     * @param container 要添加前缀/后缀的 char 数组容器。
     * @param prefix 要作为前缀添加的字符序列。
     * @param suffix 要作为后缀添加的字符序列。
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
     * 将传递的 {@code prefix} 字符添加到给定结果 {@code container} 中。相应地更新 {@code fastPathData.firstUsedIndex}。
     *
     * @param prefix 要添加到结果的前缀字符。
     * @param len 要添加的字符数。
     * @param container 要添加前缀的 char 数组容器。
     */
    private void prependPrefix(char[] prefix,
                               int len,
                               char[] container) {

        fastPathData.firstUsedIndex -= len;
        int startIndex = fastPathData.firstUsedIndex;

        // 如果要添加的前缀仅 1 个字符长，直接赋值该字符。
        // 如果前缀长度小于或等于 4，我们使用一种专用算法，该算法比 System.arraycopy 更快。
        // 如果大于 4，我们使用 System.arraycopy。
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
     * 将传递的 {@code suffix} 字符添加到给定结果 {@code container} 中。相应地更新 {@code fastPathData.lastFreeIndex}。
     *
     * @param suffix 要添加到结果的后缀字符。
     * @param len 要添加的字符数。
     * @param container 要添加后缀的 char 数组容器。
     */
    private void appendSuffix(char[] suffix,
                              int len,
                              char[] container) {

        int startIndex = fastPathData.lastFreeIndex;

        // 如果要添加的后缀仅 1 个字符长，直接赋值该字符。
        // 如果后缀长度小于或等于 4，我们使用一种专用算法，该算法比 System.arraycopy 更快。
        // 如果大于 4，我们使用 System.arraycopy。
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
     * 必须在添加前缀和后缀之前调用，因为我们引用了 {@code fastPathData.firstUsedIndex} 和 {@code fastPathData.lastFreeIndex}，
     * 并且不支持前缀和后缀（为了速度）。
     *
     * 从 {@code fastPathData} 中最后一个使用索引开始向后循环。
     *
     * @param digitsBuffer 存储数字的 char 数组容器。
     */
    private void localizeDigits(char[] digitsBuffer) {

        // 我们将仅对数字进行本地化，使用分组大小，
        // 并考虑分数部分。

        // 首先考虑分数部分。
        int digitsCounter =
            fastPathData.lastFreeIndex - fastPathData.fractionalFirstIndex;

        // 没有分数位的情况。
        if (digitsCounter < 0)
            digitsCounter = groupingSize;


                    // 仅数字需要本地化。
        for (int cursor = fastPathData.lastFreeIndex - 1;
             cursor >= fastPathData.firstUsedIndex;
             cursor--) {
            if (digitsCounter != 0) {
                // 这是一个数字字符，我们需要本地化它。
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
     * 该算法构建格式化结果，并将其放入专用的
     * {@code fastPathData.fastPathContainer} 中。
     *
     * @param d 要格式化的双精度值。
     * @param negative 指明 {@code d} 是否为负数的标志。
     */
    private void fastDoubleFormat(double d,
                                  boolean negative) {

        char[] container = fastPathData.fastPathContainer;

        /*
         * 该算法的原理是：
         * - 将传递的双精度值分解为其整数部分和小数部分
         *    并将其转换为整数。
         * - 然后根据半偶舍入规则决定是否需要向上舍入，首先使用近似缩放的小数部分。
         * - 对于困难的情况（近似缩放的小数部分恰好为 0.5d），我们通过调用
         *    exactRoundUp 实用方法来细化舍入决策，该方法既计算近似的精确舍入
         *    又做出正确的舍入决策。
         * - 如果需要，我们对小数部分进行向上舍入，如果遇到“全九”情况，
         *    可能会将舍入传播到整数部分。
         * - 然后我们从结果的整数部分和小数部分收集数字，同时即时设置所需的分组字符。
         * - 然后根据需要本地化收集的数字，
         * - 最后，如果需要，添加前缀/后缀。
         */

        // d 的精确整数部分。
        int integralPartAsInt = (int) d;

        // d 的精确小数部分（因为我们减去了它的整数部分）。
        double exactFractionalPart = d - (double) integralPartAsInt;

        // d 的近似缩放小数部分（由于乘法）。
        double scaledFractional =
            exactFractionalPart * fastPathData.fractionalScaleFactor;

        // 上面缩放小数部分的精确整数部分。
        int fractionalPartAsInt = (int) scaledFractional;

        // 上面缩放小数部分的精确小数部分。
        scaledFractional = scaledFractional - (double) fractionalPartAsInt;

        // 只有当 scaledFractional 恰好为 0.5d 时，我们才需要进行精确计算并做出精细的舍入决策，
        // 因为上面的近似结果可能导致不正确的决策。
        // 否则，与 0.5d 进行比较（严格大于或小于）即可。
        boolean roundItUp = false;
        if (scaledFractional >= 0.5d) {
            if (scaledFractional == 0.5d)
                // 舍入需要精细的决策。
                roundItUp = exactRoundUp(exactFractionalPart, fractionalPartAsInt);
            else
                roundItUp = true;

            if (roundItUp) {
                // 对小数部分进行向上舍入（如果需要，也对整数部分进行向上舍入）。
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
     * 快速路径格式化的快捷方式，由 NumberFormat 调用，或由
     * format(double, ...) 公共方法调用。
     *
     * 如果实例可以应用快速路径，并且传递的双精度值不是 NaN 或
     * 无穷大，且在整数范围内，我们会在必要时将 {@code d} 更改为其正值，
     * 然后调用 {@code fastDoubleFormat}。
     *
     * 否则，根据约定返回 null，因为快速路径无法执行。
     *
     * @param d 要格式化的双精度值
     *
     * @return 作为字符串的 {@code d} 的格式化结果。
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
            // 不应为无穷大和 NaN 使用快速路径。
            return null;

        // 提取并记录双精度值的符号，可能将其更改为正值，
        // 然后调用 fastDoubleFormat()。
        boolean negative = false;
        if (d < 0.0d) {
            negative = true;
            d = -d;
        } else if (d == 0.0d) {
            negative = (Math.copySign(1.0d, d) == -1.0d);
            d = +0.0d;
        }

        if (d > MAX_INT_AS_DOUBLE)
            // 过滤掉超出预期快速路径范围的值
            return null;
        else {
            if (!isDataSet) {
                /*
                 * 如果通过 checkAndSetFastPathStatus() 没有设置快速路径数据
                 * 并且满足快速路径条件，则直接通过 resetFastPathData() 重置数据
                 */
                resetFastPathData(isFastPath);
            }
            fastDoubleFormat(d, negative);

        }


        // 从更新的 fastPathContainer 返回新的字符串。
        return new String(fastPathData.fastPathContainer,
                          fastPathData.firstUsedIndex,
                          fastPathData.lastFreeIndex - fastPathData.firstUsedIndex);

    }

    // ======== 双精度快速路径格式化逻辑结束 =========================

    /**
     * 完成有限数字的格式化。进入时，digitList 必须填充正确的数字。
     */
    private StringBuffer subformat(StringBuffer result, FieldDelegate delegate,
                                   boolean isNegative, boolean isInteger,
                                   int maxIntDigits, int minIntDigits,
                                   int maxFraDigits, int minFraDigits) {
        // 注意：这不再需要，因为 DigitList 会处理这个问题。
        //
        //  // 负指数表示小数点和第一个非零数字之间的前导零数量，对于
        //  // 值 < 0.1（例如，对于 0.00123，-fExponent == 2）。如果这
        //  // 大于最大小数位数，则打印表示的数字会发生下溢。
        //  // 我们在这里识别这种情况，并在 DigitList 表示为零的情况下设置它。
        //
        //  if (-digitList.decimalAt >= getMaximumFractionDigits())
        //  {
        //      digitList.count = 0;
        //  }

        char zero = symbols.getZeroDigit();
        int zeroDelta = zero - '0'; // '0' 是 DigitList 的零表示
        char grouping = symbols.getGroupingSeparator();
        char decimal = isCurrencyFormat ?
            symbols.getMonetaryDecimalSeparator() :
            symbols.getDecimalSeparator();

        /* 根据 bug 4147706，DecimalFormat 必须尊重格式化为零的数字的符号。
         * 这允许进行合理的计算并保留关系，如 signum(1/x) = signum(x)，其中 x 是 +Infinity 或
         * -Infinity。在修复此问题之前，我们总是将零值格式化为正数。Liu 7/6/98。
         */
        if (digitList.isZero()) {
            digitList.decimalAt = 0; // 标准化
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

            // 最小整数位数在指数表示中通过调整指数来处理。
            // 例如，0.01234 有 3 个最小整数位数是 "123.4E-4"。

            // 最大整数位数被解释为表示重复范围。这对于工程表示法很有用，
            // 其中指数被限制为 3 的倍数。例如，0.01234 有 3 个最大整数位数是 "12.34e-3"。
            // 如果最大整数位数 > 1 且大于最小整数位数，则忽略最小整数位数。
            int exponent = digitList.decimalAt;
            int repeat = maxIntDigits;
            int minimumIntegerDigits = minIntDigits;
            if (repeat > 1 && repeat > minIntDigits) {
                // 定义了重复范围；如下调整。
                // 如果 repeat == 3，我们有 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
                // -3,-4,-5=>-6 等。这考虑到了我们这里的指数比预期的少一个；
                // 它是 0.MMMMMx10^n 的格式。
                if (exponent >= 1) {
                    exponent = ((exponent - 1) / repeat) * repeat;
                } else {
                    // 整数除法向 0 舍入
                    exponent = ((exponent - repeat) / repeat) * repeat;
                }
                minimumIntegerDigits = 1;
            } else {
                // 没有定义重复范围；使用最小整数位数。
                exponent -= minimumIntegerDigits;
            }

            // 我们现在输出最小数量的数字，如果有更多数字，则输出更多，最多到最大数量的数字。
            // 我们在“整数”数字后放置小数点，这些数字是前 (decimalAt - exponent) 个数字。
            int minimumDigits = minIntDigits + minFraDigits;
            if (minimumDigits < 0) {    // 溢出？
                minimumDigits = Integer.MAX_VALUE;
            }

            // 如果数字为零，则整数位数需要特别处理，因为可能没有数字。
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

            // 指数使用模式指定的最小指数位数输出。指数位数没有最大限制，
            // 因为截断指数会导致不可接受的不准确。
            int fieldStart = result.length();

            result.append(symbols.getExponentSeparator());

            delegate.formatted(Field.EXPONENT_SYMBOL, Field.EXPONENT_SYMBOL,
                               fieldStart, result.length(), result);

            // 对于零值，我们强制指数为零。我们必须在这里这样做，而不是更早，
            // 因为该值用于上面确定整数位数。
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

            // 输出整数部分。这里的 'count' 是我们将显示的整数位数的总数，包括为了满足 getMinimumIntegerDigits 而需要的前导零，
            // 以及数字中实际存在的位数。
            int count = minIntDigits;
            int digitIndex = 0; // 指向 digitList.fDigits[] 的索引
            if (digitList.decimalAt > 0 && count < digitList.decimalAt) {
                count = digitList.decimalAt;
            }

            // 处理 getMaximumIntegerDigits() 小于实际整数位数的情况。如果确实如此，我们输出最不重要的 max integer digits。
            // 例如，值 1997 以 2 个最大整数位数打印就是 "97"。
            if (count > maxIntDigits) {
                count = maxIntDigits;
                digitIndex = digitList.decimalAt - count;
            }

            int sizeBeforeIntegerPart = result.length();
            for (int i=count-1; i>=0; --i) {
                if (i < digitList.decimalAt && digitIndex < digitList.count) {
                    // 输出一个实际的数字
                    result.append((char)(digitList.digits[digitIndex++] + zeroDelta));
                } else {
                    // 输出前导零
                    result.append(zero);
                }

                // 必要时输出分组分隔符。但不要在 i==0 时输出分组分隔符；这是整数部分的末尾。
                if (isGroupingUsed() && i>0 && (groupingSize != 0) &&
                    (i % groupingSize == 0)) {
                    int gStart = result.length();
                    result.append(grouping);
                    delegate.formatted(Field.GROUPING_SEPARATOR,
                                       Field.GROUPING_SEPARATOR, gStart,
                                       result.length(), result);
                }
            }

            // 确定是否有任何可打印的小数位。如果我们已经用完了数字，则没有。
            boolean fractionPresent = (minFraDigits > 0) ||
                (!isInteger && digitIndex < digitList.count);

            // 如果没有小数部分，并且我们没有打印任何整数位，则打印一个零。否则我们将不打印任何数字，
            // 并且将无法解析此字符串。
            if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
                result.append(zero);
            }

            delegate.formatted(INTEGER_FIELD, Field.INTEGER, Field.INTEGER,
                               iFieldStart, result.length(), result);

            // 如果我们总是输出小数分隔符，则输出小数分隔符。
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
                // 这里是我们退出循环的地方。我们如果已经输出了最大小数位数（在上面的 for 表达式中指定）则退出。
                // 当我们已经输出了最小位数并且：
                // 我们有一个整数，所以没有小数部分要显示，或者我们已经没有有效数字了。
                if (i >= minFraDigits &&
                    (isInteger || digitIndex >= digitList.count)) {
                    break;
                }

                // 输出前导小数零。这些零是在小数点之后但在任何有效数字之前。这些只在 abs(被格式化的数字) < 1.0 时输出。
                if (-1-i > (digitList.decimalAt-1)) {
                    result.append(zero);
                    continue;
                }

                // 如果我们还有精度，输出一个数字，否则输出一个零。我们不想输出噪声数字。
                if (!isInteger && digitIndex < digitList.count) {
                    result.append((char)(digitList.digits[digitIndex++] + zeroDelta));
                } else {
                    result.append(zero);
                }
            }

            // 记录字段信息供调用者使用。
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
     * <code>delegate</code> 会收到 <code>positions</code> 中的所有
     * <code>FieldPosition</code> 通知。
     * <p>
     * 如果 <code>positions</code> 中的某个 <code>FieldPosition</code>
     * 标识了一个 <code>SIGN</code> 属性，它将被映射到
     * <code>signAttribute</code>。这用于
     * 必要时将 <code>SIGN</code> 属性映射到 <code>EXPONENT</code>
     * 属性。
     * <p>
     * 这由 <code>subformat</code> 用于添加前缀/后缀。
     */
    private void append(StringBuffer result, String string,
                        FieldDelegate delegate,
                        FieldPosition[] positions,
                        Format.Field signAttribute) {
        int start = result.length();

        if (string.length() > 0) {
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
     * 如果解析成功，则 <code>pos</code> 的索引将更新为
     * 使用的最后一个字符之后的索引（解析不一定使用到字符串的末尾的所有字符），并且返回解析的数字。
     * 更新的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会更改，<code>pos</code> 的错误索引将设置为
     * 发生错误的字符的索引，并且返回 null。
     * <p>
     * 返回的子类取决于 {@link #isParseBigDecimal} 的值以及被解析的字符串。
     * <ul>
     *   <li>如果 <code>isParseBigDecimal()</code> 为 false（默认值），
     *       大多数整数值将以 <code>Long</code> 对象的形式返回，无论它们如何书写：<code>"17"</code> 和
     *       <code>"17.000"</code> 都解析为 <code>Long(17)</code>。
     *       不能放入 <code>Long</code> 的值将以 <code>Double</code> 形式返回。这包括具有小数部分的值，
     *       无限值，<code>NaN</code>，以及值 -0.0。
     *       <code>DecimalFormat</code> <em>不会</em> 根据源字符串中是否存在小数分隔符来决定返回 <code>Double</code> 还是 <code>Long</code>。
     *       这样做将阻止溢出 double 尾数的整数，如 <code>"-9,223,372,036,854,775,808.00"</code>，无法准确解析。
     *       <p>
     *       调用者可以使用 <code>Number</code> 方法
     *       <code>doubleValue</code>，<code>longValue</code> 等来获取所需类型。
     *   <li>如果 <code>isParseBigDecimal()</code> 为 true，值将以 <code>BigDecimal</code> 对象形式返回。
     *       这些值是通过 {@link java.math.BigDecimal#BigDecimal(String)}
     *       为相应字符串构造的，格式与区域无关。特殊值负无穷大、正无穷大和 NaN 将返回
     *       持有相应 <code>Double</code> 常量值的 <code>Double</code> 实例。
     * </ul>
     * <p>
     * <code>DecimalFormat</code> 解析所有表示十进制数字的 Unicode 字符，这些字符由 <code>Character.digit()</code> 定义。
     * 此外，<code>DecimalFormat</code> 还将 <code>DecimalFormatSymbols</code> 对象中定义的本地化零数字开始的
     * 十个连续字符识别为数字。
     *
     * @param text 要解析的字符串
     * @param pos  一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return     解析的值，如果解析失败则返回 <code>null</code>
     * @exception  NullPointerException 如果 <code>text</code> 或
     *             <code>pos</code> 为 null。
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
                catch (ArithmeticException e) {  // 非终止十进制扩展
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
                if (longResult < 0) {  // 得到 Long.MIN_VALUE
                    gotLongMinimum = true;
                }
            } else {
                doubleResult = digitList.getDouble();
            }

            // 除以乘数。我们在这里必须小心，不要进行不必要的 double 和 long 之间的转换。
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

            // 此时，如果我们通过乘数除以结果，结果可能适合 long。我们检查这种情况并在可能的情况下返回 long。
            // 我们必须在应用负号（如果适用）之后执行此操作，以处理 LONG_MIN 的情况；否则，如果我们用正数 -LONG_MIN 做这件事，
            // double 将 > 0，但 long 将 < 0。我们还必须在 -0.0 的情况下保留 double，这将与 long 0 转换为 double 后相等
            // （bug 4162852）。
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
     * 将给定的文本解析为数字。解析从 parsePosition 开始，直到遇到无法解析的字符。
     * @param text 要解析的字符串。
     * @param parsePosition 开始解析的位置。返回时，为第一个无法解析的字符的位置。
     * @param digits 设置为解析值的 DigitList。
     * @param isExponent 如果为 true，解析指数。这意味着没有无限值且只能是整数。
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
        if (!isExponent && text.regionMatches(position,symbols.getInfinity(),0,
                          symbols.getInfinity().length())) {
            position += symbols.getInfinity().length();
            status[STATUS_INFINITE] = true;
        } else {
            // 现在我们有一个可能包含分组符号和小数点的数字字符串。我们希望将这些处理成一个 DigitList。
            // 我们不想将一堆前导零放入 DigitList，所以我们跟踪小数点的位置，
            // 只将有效数字放入 DigitList，并根据需要调整指数。

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
            int exponent = 0; // 如果有指数，设置为指数值

            // 我们必须自己跟踪 digitCount，因为 digits.count 在达到最大允许数字时会固定。
            int digitCount = 0;

            backup = -1;
            for (; position < text.length(); ++position) {
                char ch = text.charAt(position);

                /* 我们识别所有数字范围，而不仅仅是拉丁数字范围 '0'..'9'。我们通过使用 Character.digit() 方法，
                 * 将有效的 Unicode 数字转换为 0..9 范围。
                 *
                 * 字符 'ch' 可能是数字。如果是，将其值从 0 到 9 放入 'digit'。首先尝试使用区域设置数字，
                 * 这可能是或可能不是标准 Unicode 数字范围。如果失败，尝试使用标准 Unicode 数字范围
                 * 通过调用 Character.digit()。如果这也失败，digit 将有一个不在 0..9 范围内的值。
                 */
                int digit = ch - zero;
                if (digit < 0 || digit > 9) {
                    digit = Character.digit(ch, 10);
                }

                if (digit == 0) {
                    // 取消备份设置（见分组处理程序下方）
                    backup = -1; // 在 continue 语句下方执行此操作!!!
                    sawDigit = true;

                    // 处理前导零
                    if (digits.count == 0) {
                        // 忽略数字整数部分的前导零。
                        if (!sawDecimal) {
                            continue;
                        }

                        // 如果我们已经看到小数点，但还没有看到有效数字，
                        // 则通过将 digits.decimalAt 减少到负值来计算前导零。
                        --digits.decimalAt;
                    } else {
                        ++digitCount;
                        digits.append((char)(digit + '0'));
                    }
                } else if (digit > 0 && digit <= 9) { // [sic] digit==0 已经处理
                    sawDigit = true;
                    ++digitCount;
                    digits.append((char)(digit + '0'));

                    // 取消备份设置（见分组处理程序下方）
                    backup = -1;
                } else if (!isExponent && ch == decimal) {
                    // 如果我们只解析整数，或者我们已经看到了小数点，则不解析这个小数点。
                    if (isParseIntegerOnly() || sawDecimal) {
                        break;
                    }
                    digits.decimalAt = digitCount; // 不是 digits.count!
                    sawDecimal = true;
                } else if (!isExponent && ch == grouping && isGroupingUsed()) {
                    if (sawDecimal) {
                        break;
                    }
                    // 如果我们使用分组字符，忽略它们，但要求它们后面跟着一个数字。否则我们备份并重新处理它们。
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
                    break; // 无论成功还是失败，我们退出此循环
                } else {
                    break;
                }
            }

            if (backup != -1) {
                position = backup;
            }

            // 如果没有小数点，我们有一个整数
            if (!sawDecimal) {
                digits.decimalAt = digitCount; // 不是 digits.count!
            }

            // 如果有任何指数，调整小数点位置
            digits.decimalAt += exponent;

            // 如果文本字符串中的任何部分都没有被识别。例如，解析 "x" 与模式 "#0.00"（返回索引和错误索引均为 0）
            // 解析 "$" 与模式 "$#0.00"。（返回索引 0 和错误索引 1）。
            if (!sawDigit && digitCount == 0) {
                parsePosition.index = oldStart;
                parsePosition.errorIndex = oldStart;
                return false;
            }
        }

        // 检查后缀
        if (!isExponent) {
            if (gotPositive) {
                gotPositive = text.regionMatches(position,positiveSuffix,0,
                                                 positiveSuffix.length());
            }
            if (gotNegative) {
                gotNegative = text.regionMatches(position,negativeSuffix,0,
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
            (gotPositive ? positiveSuffix.length() : negativeSuffix.length()); // 标记成功!
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
     * 返回一个副本的十进制格式符号，通常不被程序员或用户更改。
     * @return 所需的 DecimalFormatSymbols 的副本
     * @see java.text.DecimalFormatSymbols
     */
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            // 不允许多个引用
            return (DecimalFormatSymbols) symbols.clone();
        } catch (Exception foo) {
            return null; // 应该不会发生
        }
    }


    /**
     * 设置十进制格式符号，通常不被程序员或用户更改。
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
            // 应该不会发生
        }
    }

    /**
     * 获取正前缀。
     * <P>示例：+123, $123, sFr123
     *
     * @return 正前缀
     */
    public String getPositivePrefix () {
        return positivePrefix;
    }

    /**
     * 设置正前缀。
     * <P>示例：+123, $123, sFr123
     *
     * @param newValue 新的正前缀
     */
    public void setPositivePrefix (String newValue) {
        positivePrefix = newValue;
        posPrefixPattern = null;
        positivePrefixFieldPositions = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于正数前缀的字段位置。如果用户通过 <code>setPositivePrefix</code> 显式设置了正前缀，则不使用此方法。这是惰性创建的。
     *
     * @return 正前缀中的字段位置
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
     * 获取负前缀。
     * <P>示例：-123, ($123)（带有负后缀），sFr-123
     *
     * @return 负前缀
     */
    public String getNegativePrefix () {
        return negativePrefix;
    }

    /**
     * 设置负前缀。
     * <P>示例：-123, ($123)（带有负后缀），sFr-123
     *
     * @param newValue 新的负前缀
     */
    public void setNegativePrefix (String newValue) {
        negativePrefix = newValue;
        negPrefixPattern = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于负数前缀的字段位置。如果用户通过 <code>setNegativePrefix</code> 显式设置了负前缀，则不使用此方法。这是惰性创建的。
     *
     * @return 负前缀中的字段位置
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
     * 获取正后缀。
     * <P>示例：123%
     *
     * @return 正后缀
     */
    public String getPositiveSuffix () {
        return positiveSuffix;
    }

    /**
     * 设置正后缀。
     * <P>示例：123%
     *
     * @param newValue 新的正后缀
     */
    public void setPositiveSuffix (String newValue) {
        positiveSuffix = newValue;
        posSuffixPattern = null;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回用于正数后缀的字段位置。如果用户通过 <code>setPositiveSuffix</code> 显式设置了正后缀，则不使用此方法。这是惰性创建的。
     *
     * @return 正后缀中的字段位置
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
     * 获取负后缀。
     * <P>示例：-123%，($123)（带有正后缀）
     *
     * @return 负后缀
     */
    public String getNegativeSuffix () {
        return negativeSuffix;
    }

    /**
     * 设置负后缀。
     * <P>示例：123%
     *
     * @param newValue 新的负后缀
     */
    public void setNegativeSuffix (String newValue) {
        negativeSuffix = newValue;
        negSuffixPattern = null;
        fastPathCheckNeeded = true;
    }


                /**
     * 返回用于负数的后缀字段的 FieldPositions。如果用户已通过 <code>setNegativeSuffix</code> 明确设置了负数后缀，则不使用此方法。此方法是惰性创建的。
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
     * 对于百分比格式，将乘数设置为 100 并将后缀设置为 '%'（对于阿拉伯语，使用阿拉伯百分号）。
     * 对于千分比格式，将乘数设置为 1000 并将后缀设置为 '&#92;u2030'。
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
     * 返回分组大小。分组大小是数字整数部分中分组分隔符之间的数字位数。例如，在数字 "123,456.78" 中，分组大小为 3。
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
     * 设置分组大小。分组大小是数字整数部分中分组分隔符之间的数字位数。例如，在数字 "123,456.78" 中，分组大小为 3。
     * <br>
     * 传递的值将转换为字节，可能会丢失信息。
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
     * 允许获取整数的十进制分隔符的行为。（十进制分隔符在小数中总是出现。）
     * <P>示例：十进制 ON: 12345 &rarr; 12345.; OFF: 12345 &rarr; 12345
     *
     * @return 如果总是显示十进制分隔符，则返回 {@code true}；否则返回 {@code false}
     */
    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    /**
     * 允许设置整数的十进制分隔符的行为。（十进制分隔符在小数中总是出现。）
     * <P>示例：十进制 ON: 12345 &rarr; 12345.; OFF: 12345 &rarr; 12345
     *
     * @param newValue 如果总是显示十进制分隔符，则返回 {@code true}；否则返回 {@code false}
     */
    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        decimalSeparatorAlwaysShown = newValue;
        fastPathCheckNeeded = true;
    }

    /**
     * 返回 {@link #parse(java.lang.String, java.text.ParsePosition)} 方法是否返回 <code>BigDecimal</code>。默认值为 false。
     *
     * @return 如果解析方法返回 BigDecimal，则返回 {@code true}；否则返回 {@code false}
     * @see #setParseBigDecimal
     * @since 1.5
     */
    public boolean isParseBigDecimal() {
        return parseBigDecimal;
    }

    /**
     * 设置 {@link #parse(java.lang.String, java.text.ParsePosition)} 方法是否返回 <code>BigDecimal</code>。
     *
     * @param newValue 如果解析方法返回 BigDecimal，则返回 {@code true}；否则返回 {@code false}
     * @see #isParseBigDecimal
     * @since 1.5
     */
    public void setParseBigDecimal(boolean newValue) {
        parseBigDecimal = newValue;
    }

    /**
     * 标准重写；语义不变。
     */
    @Override
    public Object clone() {
        DecimalFormat other = (DecimalFormat) super.clone();
        other.symbols = (DecimalFormatSymbols) symbols.clone();
        other.digitList = (DigitList) digitList.clone();

        // 快速路径算法几乎是无状态的。唯一的状态逻辑是 isFastPath 标志。此外，fastPathCheckNeeded 是一个哨兵标志，
        // 当设置为 true 时，会强制重新计算所有快速路径字段。
        //
        // 因此，克隆时不需要克隆所有快速路径字段。我们只需要将 fastPathCheckNeeded 设置为 true，
        // 并将 fastPathData 初始化为 null，就像它是一个全新的实例一样。每次使用快速路径算法时，
        // 所有快速路径字段将仅重新计算一次。
        other.fastPathCheckNeeded = true;
        other.isFastPath = false;
        other.fastPathData = null;

        return other;
    }

    /**
     * 重写 equals
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false; // super 做类检查
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
     * 重写 hashCode
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 37 + positivePrefix.hashCode();
        // 只需足够的字段以获得合理的分布
    }

    /**
     * 合成一个表示此 Format 对象当前状态的模式字符串。
     *
     * @return 模式字符串
     * @see #applyPattern
     */
    public String toPattern() {
        return toPattern( false );
    }

    /**
     * 合成一个表示此 Format 对象当前状态的本地化模式字符串。
     *
     * @return 本地化模式字符串
     * @see #applyPattern
     */
    public String toLocalizedPattern() {
        return toPattern( true );
    }

    /**
     * 将前缀模式字符串扩展为扩展的前缀字符串。如果任何前缀模式字符串为 null，则不扩展。每当符号或前缀模式更改时，应调用此方法以保持扩展的前缀字符串的最新状态。
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
     * 将前缀模式扩展为前缀字符串。模式中的所有字符都是字面量，除非由 QUOTE 前缀。识别 QUOTE 后的以下字符：PATTERN_PERCENT, PATTERN_PER_MILLE,
     * PATTERN_MINUS, 和 CURRENCY_SIGN。如果 CURRENCY_SIGN 重复（QUOTE + CURRENCY_SIGN + CURRENCY_SIGN），则解释为 ISO 4217 货币代码。
     * QUOTE 后的任何其他字符表示其本身。QUOTE 必须后跟另一个字符；QUOTE 不能单独出现在模式的末尾。
     *
     * @param pattern 非空的、可能为空的模式
     * @param buffer 一个临时 StringBuffer；其内容将丢失
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
     * 将前缀模式扩展为描述模式如何扩展的 FieldPosition 数组。
     * 模式中的所有字符都是字面量，除非由 QUOTE 前缀。识别 QUOTE 后的以下字符：PATTERN_PERCENT, PATTERN_PER_MILLE,
     * PATTERN_MINUS, 和 CURRENCY_SIGN。如果 CURRENCY_SIGN 重复（QUOTE + CURRENCY_SIGN + CURRENCY_SIGN），则解释为 ISO 4217 货币代码。
     * QUOTE 后的任何其他字符表示其本身。QUOTE 必须后跟另一个字符；QUOTE 不能单独出现在模式的末尾。
     *
     * @param pattern 非空的、可能为空的模式
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
                    if (string.length() > 0) {
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
     * 将前缀模式追加到给定的 StringBuffer，根据需要引用特殊字符。使用内部前缀模式（如果存在），或在内部前缀模式为 null 时使用字面量前缀。
     * 追加的字符串在传递给 toPattern() 时将生成相同的前缀模式（或字面量前缀）。
     *
     * @param buffer 前缀字符串追加到此
     * @param affixPattern 一个模式，如 posPrefixPattern；可以为 null
     * @param expAffix 相应的扩展前缀，如 positivePrefix。如果 affixPattern 为 null，则忽略。如果 affixPattern 为 null，则将 expAffix 作为字面量前缀追加。
     * @param localized 如果追加的模式应包含本地化的模式字符，则为 true；否则，追加非本地化的模式字符
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
                    // 下面追加另一个 QUOTE
                } else if (c == CURRENCY_SIGN &&
                           i<affixPattern.length() &&
                           affixPattern.charAt(i) == CURRENCY_SIGN) {
                    ++i;
                    buffer.append(c);
                    // 下面追加另一个 CURRENCY_SIGN
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
     * 将给定的 affix 附加到 StringBuffer 中，如果存在特殊字符则使用引号。单引号本身在任何情况下都必须转义。
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
     * 生成模式的实际工作。 */
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
     * 将给定的模式应用到此 Format 对象。模式是各种格式属性的简写说明。
     * 这些属性也可以通过各种 setter 方法单独更改。
     * <p>
     * 该例程不设置整数位数的限制，因为这是典型的最终用户需求；
     * 如果您想设置实际值，请使用 setMaximumInteger。
     * 对于负数，使用第二个模式，用分号分隔
     * <P>示例 <code>"#,#00.0#"</code> &rarr; 1,234.56
     * <P>这意味着最少 2 位整数，1 位小数，最多 2 位小数。
     * <p>示例： <code>"#,#00.0#;(#,#00.0#)"</code> 用于负数的括号表示。
     * <p>在负数模式中，最小和最大计数被忽略；
     * 假定这些值在正数模式中设置。
     *
     * @param pattern 新的模式
     * @exception NullPointerException 如果 <code>pattern</code> 为 null
     * @exception IllegalArgumentException 如果给定的模式无效。
     */
    public void applyPattern(String pattern) {
        applyPattern(pattern, false);
    }

    /**
     * 将给定的模式应用到此 Format 对象。假设模式是本地化的表示。模式是各种格式属性的简写说明。
     * 这些属性也可以通过各种 setter 方法单独更改。
     * <p>
     * 该例程不设置整数位数的限制，因为这是典型的最终用户需求；
     * 如果您想设置实际值，请使用 setMaximumInteger。
     * 对于负数，使用第二个模式，用分号分隔
     * <P>示例 <code>"#,#00.0#"</code> &rarr; 1,234.56
     * <P>这意味着最少 2 位整数，1 位小数，最多 2 位小数。
     * <p>示例： <code>"#,#00.0#;(#,#00.0#)"</code> 用于负数的括号表示。
     * <p>在负数模式中，最小和最大计数被忽略；
     * 假定这些值在正数模式中设置。
     *
     * @param pattern 新的模式
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

        // 两个变量用于记录模式中第一阶段占用的子范围。
        // 在处理第二个模式（表示负数的模式）时，用于确保两个模式之间的第一阶段没有偏差。
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

            // 阶段范围从 0 到 2。阶段 0 是前缀。阶段 1 是模式中包含数字、小数分隔符、
            // 分组字符的部分。阶段 2 是后缀。在阶段 0 和 2 中，识别并转换百分号、千分号和货币符号。
            // 字符被严格地分阶段分离；例如，如果阶段 1 的字符出现在后缀中，它们必须被引用。
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
                        // 引号内的引号表示要么是闭合引号，要么是两个引号，即引号字面量。也就是说，我们有 'do' 或 'don''t' 中的第二个引号。
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
                            // 使用前视来确定货币符号是否重复。
                            boolean doubled = (pos + 1) < pattern.length() &&
                                pattern.charAt(pos + 1) == CURRENCY_SIGN;
                            if (doubled) { // 跳过重复的字符
                             ++pos;
                            }
                            isCurrencyFormat = true;
                            affix.append(doubled ? "'\u00A4\u00A4" : "'\u00A4");
                            continue;
                        } else if (ch == QUOTE) {
                            // 引号外的引号表示要么是开引号，要么是两个引号，即引号字面量。也就是说，我们有 'do' 或 o''clock 中的第一个引号。
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
                            // 在看到阶段 1 的数字字符之前不允许分隔符，并且在第二个模式（j == 0）中不允许分隔符。
                            if (phase == 0 || j == 0) {
                                throw new IllegalArgumentException("未引用的特殊字符 '" +
                                    ch + "' 在模式 \"" + pattern + '"');
                            }
                            start = pos + 1;
                            pos = pattern.length();
                            continue;
                        }

                        // 接下来处理直接附加的字符。
                        else if (ch == percent) {
                            if (multiplier != 1) {
                                throw new IllegalArgumentException("模式 \"" +
                                    pattern + "\" 中的百分号/千分号字符过多");
                            }
                            multiplier = 100;
                            affix.append("'%");
                            continue;
                        } else if (ch == perMill) {
                            if (multiplier != 1) {
                                throw new IllegalArgumentException("模式 \"" +
                                    pattern + "\" 中的百分号/千分号字符过多");
                            }
                            multiplier = 1000;
                            affix.append("'\u2030");
                            continue;
                        } else if (ch == minus) {
                            affix.append("'-");
                            continue;
                        }
                    }
                    // 注意，如果我们在引号内，或者这是一个未引用的非特殊字符，通常会落入这里。
                    affix.append(ch);
                    break;

                case 1:
                    // 两个子模式中的第一阶段必须相同。我们通过直接比较来强制执行这一点。在处理第一个子模式时，我们只是记录其长度。在处理第二个子模式时，我们比较字符。
                    if (j == 1) {
                        ++phaseOneLength;
                    } else {
                        if (--phaseOneLength == 0) {
                            phase = 2;
                            affix = suffix;
                        }
                        continue;
                    }


                                // 处理数字、小数点和分组字符。我们记录五条信息。我们期望数字
                    // 按照 ####0000.#### 的模式出现，并记录左侧数字、零（中心）数字和右侧
                    // 数字的数量。记录最后一个分组字符的位置（应该在前两个字符块中的某个位置），
                    // 以及小数点的位置（如果有）（应该在零数字中）。如果没有
                    // 小数点，则不应该有右侧数字。
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
                                pattern + "\" 中出现意外的 '0'");
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
                        pos = pos + exponent.length();
                        while (pos < pattern.length() &&
                               pattern.charAt(pos) == zeroDigit) {
                            ++minExponentDigits;
                            ++phaseOneLength;
                            ++pos;
                        }

                        if ((digitLeftCount + zeroDigitCount) < 1 ||
                            minExponentDigits < 1) {
                            throw new IllegalArgumentException("格式不正确的指数模式 \"" + pattern + "\"");
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

            // 处理没有 '0' 模式字符的模式。这些模式是合法的，但必须进行解释。 "##.###" -> "#0.###"。
            // ".###" -> ".0##"。
            /* 我们允许 "####" 形式的模式产生零 zeroDigitCount
             * （明白了吗？）；尽管这似乎可能会导致 format() 生成空字符串，但 format() 会检查
             * 这种情况并在这种情况下输出一个零数字。
             * 使 zeroDigitCount 为零可以得到最小整数位数为零，这允许正确的往返模式。也就是说，
             * 我们不希望 "#" 在调用 toPattern() 时变成 "#0"（即使从语义上讲它确实是这样的）。
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
                throw new IllegalArgumentException("格式不正确的模式 \"" +
                    pattern + '"');
            }

            if (j == 1) {
                posPrefixPattern = prefix.toString();
                posSuffixPattern = suffix.toString();
                negPrefixPattern = posPrefixPattern;   // 先假设这些
                negSuffixPattern = posSuffixPattern;
                int digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
                /* 有效的 decimalPos 是小数点的位置或如果没有小数点则应该是的位置。注意，如果 decimalPos<0，
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

        if (pattern.length() == 0) {
            posPrefixPattern = posSuffixPattern = "";
            setMinimumIntegerDigits(0);
            setMaximumIntegerDigits(MAXIMUM_INTEGER_DIGITS);
            setMinimumFractionDigits(0);
            setMaximumFractionDigits(MAXIMUM_FRACTION_DIGITS);
        }

        // 如果没有负数模式，或者负数模式与正数模式相同，则在正数模式前加上负号以形成负数模式。
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
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和
     * 309 中的较小值。负输入值将被替换为 0。
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
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和
     * 309 中的较小值。负输入值将被替换为 0。
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
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和
     * 340 中的较小值。负输入值将被替换为 0。
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
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用 <code>newValue</code> 和
     * 340 中的较小值。负输入值将被替换为 0。
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
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用返回值和
     * 309 中的较小值。
     * @see #setMaximumIntegerDigits
     */
    @Override
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }

    /**
     * 获取数字整数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用返回值和
     * 309 中的较小值。
     * @see #setMinimumIntegerDigits
     */
    @Override
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    /**
     * 获取数字小数部分允许的最大位数。
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用返回值和
     * 340 中的较小值。
     * @see #setMaximumFractionDigits
     */
    @Override
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * 获取数字小数部分允许的最小位数。
     * 对于格式化非 <code>BigInteger</code> 和
     * <code>BigDecimal</code> 对象的数字，使用返回值和
     * 340 中的较小值。
     * @see #setMinimumFractionDigits
     */
    @Override
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * 获取此数字格式在格式化货币值时使用的货币。
     * 通过调用此数字格式的符号上的
     * {@link DecimalFormatSymbols#getCurrency DecimalFormatSymbols.getCurrency}
     * 获取货币。
     *
     * @return 由此数字格式使用的货币，或 <code>null</code>
     * @since 1.4
     */
    @Override
    public Currency getCurrency() {
        return symbols.getCurrency();
    }

    /**
     * 设置此数字格式在格式化货币值时使用的货币。这不会更新数字格式使用的最小或最大
     * 小数位数。通过调用此数字格式的符号上的
     * {@link DecimalFormatSymbols#setCurrency DecimalFormatSymbols.setCurrency}
     * 设置货币。
     *
     * @param currency 由此数字格式使用的新的货币
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
     * @return 由此 DecimalFormat 使用的 <code>RoundingMode</code>。
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
     * 验证超类的数字计数字段是否正确反映了对非 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象格式化数字的限制。这些限制存储在超类中，以保持与旧版本的序列化兼容性，而 <code>BigInteger</code> 和 <code>BigDecimal</code> 对象的限制则保存在本类中。
     * 如果在超类中，最小或最大整数位数大于 <code>DOUBLE_INTEGER_DIGITS</code> 或最小或最大小数位数大于 <code>DOUBLE_FRACTION_DIGITS</code>，则流数据无效，此方法将抛出 <code>InvalidObjectException</code>。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 4，则将 <code>roundingMode</code> 初始化为 {@link java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}。此字段是版本 4 中新增的。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 3，则使用超类的相应 getter 方法的值调用最小和最大整数和小数位数的 setter 方法，以初始化本类中的字段。这些字段是版本 3 中新增的。
     * <li>
     * 如果 <code>serialVersionOnStream</code> 小于 1，表示流是由 JDK 1.1 编写的，则将 <code>useExponentialNotation</code> 初始化为 false，因为 JDK 1.1 中没有此字段。
     * <li>
     * 将 <code>serialVersionOnStream</code> 设置为允许的最大值，以便如果此对象再次被序列化，将正确执行默认序列化。
     * </ol>
     *
     * <p>版本 2 之前的流将没有前缀模式变量 <code>posPrefixPattern</code> 等。因此，它们将被初始化为 <code>null</code>，这意味着前缀字符串将被视为字面值。这正是我们想要的，因为这对应于版本 2 之前的行为。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        digitList = new DigitList();

        // 当实例被反序列化时，我们强制完全重新初始化快速路径。参见 clone() 中关于 fastPathCheckNeeded 的注释。
        fastPathCheckNeeded = true;
        isFastPath = false;
        fastPathData = null;

        if (serialVersionOnStream < 4) {
            setRoundingMode(RoundingMode.HALF_EVEN);
        } else {
            setRoundingMode(getRoundingMode());
        }

        // 我们只需要检查最大计数，因为 NumberFormat.readObject 已经确保最大值大于最小值。
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
     * 用于格式化正数的前缀符号，例如 "+"。
     *
     * @serial
     * @see #getPositivePrefix
     */
    private String  positivePrefix = "";

    /**
     * 用于格式化正数的后缀符号。这通常是一个空字符串。
     *
     * @serial
     * @see #getPositiveSuffix
     */
    private String  positiveSuffix = "";

    /**
     * 用于格式化负数的前缀符号，例如 "-"。
     *
     * @serial
     * @see #getNegativePrefix
     */
    private String  negativePrefix = "-";

    /**
     * 用于格式化负数的后缀符号。这通常是一个空字符串。
     *
     * @serial
     * @see #getNegativeSuffix
     */
    private String  negativeSuffix = "";

    /**
     * 非负数的前缀模式。此变量对应于 <code>positivePrefix</code>。
     *
     * <p>此模式由 <code>expandAffix()</code> 方法扩展为 <code>positivePrefix</code>，以反映 <code>symbols</code> 的变化。如果此变量为 <code>null</code>，则 <code>positivePrefix</code> 被视为一个不会因 <code>symbols</code> 变化而改变的字面值。对于从版本 2 之前的流恢复的 <code>DecimalFormat</code> 对象，此变量始终为 <code>null</code>。
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
     * 数字整数部分中每组数字之间的分隔符数量。如果 <code>NumberFormat.groupingUsed</code> 为 true，则必须大于 0。
     *
     * @serial
     * @see #getGroupingSize
     * @see java.text.NumberFormat#isGroupingUsed
     */
    private byte    groupingSize = 3;  // 不变，如果 useThousands 为 true，则大于 0

    /**
     * 如果为 true，则在格式化数字时，即使数字的小数部分为零，也会始终显示小数分隔符。
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
     * 如果为 true，则表示此对象表示货币格式。这决定了是否使用货币小数分隔符而不是普通的小数分隔符。
     */
    private transient boolean isCurrencyFormat = false;

    /**
     * 本格式使用的 <code>DecimalFormatSymbols</code> 对象。它包含用于格式化数字的符号，例如分组分隔符、小数分隔符等。
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
    private boolean useExponentialNotation;  // 在 Java 2 平台 v.1.2 中新增的持久性字段

    /**
     * 描述正数前缀字符串的 FieldPositions。这是惰性创建的。需要时使用 <code>getPositivePrefixFieldPositions</code>。
     */
    private transient FieldPosition[] positivePrefixFieldPositions;

    /**
     * 描述正数后缀字符串的 FieldPositions。这是惰性创建的。需要时使用 <code>getPositiveSuffixFieldPositions</code>。
     */
    private transient FieldPosition[] positiveSuffixFieldPositions;

    /**
     * 描述负数前缀字符串的 FieldPositions。这是惰性创建的。需要时使用 <code>getNegativePrefixFieldPositions</code>。
     */
    private transient FieldPosition[] negativePrefixFieldPositions;

    /**
     * 描述负数后缀字符串的 FieldPositions。这是惰性创建的。需要时使用 <code>getNegativeSuffixFieldPositions</code>。
     */
    private transient FieldPosition[] negativeSuffixFieldPositions;

    /**
     * 当数字以指数表示法格式化时，用于显示指数的最小位数。如果 <code>useExponentialNotation</code> 不为 true，则忽略此字段。
     *
     * @serial
     * @since 1.2
     */
    private byte    minExponentDigits;       // 在 Java 2 平台 v.1.2 中新增的持久性字段

    /**
     * <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的整数部分允许的最大位数。
     * <code>maximumIntegerDigits</code> 必须大于或等于 <code>minimumIntegerDigits</code>。
     *
     * @serial
     * @see #getMaximumIntegerDigits
     * @since 1.5
     */
    private int    maximumIntegerDigits = super.getMaximumIntegerDigits();

    /**
     * <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的整数部分允许的最小位数。
     * <code>minimumIntegerDigits</code> 必须小于或等于 <code>maximumIntegerDigits</code>。
     *
     * @serial
     * @see #getMinimumIntegerDigits
     * @since 1.5
     */
    private int    minimumIntegerDigits = super.getMinimumIntegerDigits();

    /**
     * <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的小数部分允许的最大位数。
     * <code>maximumFractionDigits</code> 必须大于或等于 <code>minimumFractionDigits</code>。
     *
     * @serial
     * @see #getMaximumFractionDigits
     * @since 1.5
     */
    private int    maximumFractionDigits = super.getMaximumFractionDigits();

    /**
     * <code>BigInteger</code> 或 <code>BigDecimal</code> 数字的小数部分允许的最小位数。
     * <code>minimumFractionDigits</code> 必须小于或等于 <code>maximumFractionDigits</code>。
     *
     * @serial
     * @see #getMinimumFractionDigits
     * @since 1.5
     */
    private int    minimumFractionDigits = super.getMinimumFractionDigits();

    /**
     * 本 <code>DecimalFormat</code> 中使用的 {@link java.math.RoundingMode}。
     *
     * @serial
     * @since 1.6
     */
    private RoundingMode roundingMode = RoundingMode.HALF_EVEN;

    // ------ 用于双精度算法快速路径的 DecimalFormat 字段 ------

    /**
     * 用于存储快速路径算法中使用的数据的辅助内部工具类。几乎所有与快速路径相关的字段都封装在此类中。
     *
     * 任何 {@code DecimalFormat} 实例都有一个 {@code fastPathData} 引用字段，除非实例的属性使其处于“快速路径”状态，并且在该状态下至少调用了一次格式化方法，否则该字段为 null。
     *
     * 几乎所有字段都仅与“快速路径”状态相关，直到实例的属性发生变化之前不会改变。
     *
     * {@code firstUsedIndex} 和 {@code lastFreeIndex} 是唯一在调用 {@code fastDoubleFormat} 时使用和修改的字段。
     *
     */
    private static class FastPathData {
        // --- 快速路径中使用的临时字段，由多个方法共享。

        /** 格式化结果末尾的第一个未使用索引。 */
        int lastFreeIndex;

        /** 格式化结果开头的第一个使用索引。 */
        int firstUsedIndex;

        // --- 与快速路径状态相关的状态字段。仅因属性变化而改变。仅由 checkAndSetFastPathStatus() 设置。

        /** 本地零与默认零表示之间的差异。 */
        int  zeroDelta;

        /** 本地分组分隔符字符。 */
        char groupingChar;

        /** 格式化结果中最后一个整数位的固定索引位置。 */
        int integralLastIndex;

        /** 格式化结果中第一个小数位的固定索引位置。 */
        int fractionalFirstIndex;

        /** 依赖于小数|货币状态的小数常量。 */
        double fractionalScaleFactor;
        int fractionalMaxIntBound;

        /** 将包含格式化结果的字符数组缓冲区。 */
        char[] fastPathContainer;

        /** 为提高效率而记录的前缀字符数组。 */
        char[] charsPositivePrefix;
        char[] charsNegativePrefix;
        char[] charsPositiveSuffix;
        char[] charsNegativeSuffix;
        boolean positiveAffixesRequired = true;
        boolean negativeAffixesRequired = true;
    }

    /** 实例的格式快速路径状态。逻辑状态。 */
    private transient boolean isFastPath = false;

    /** 标记，表示在下次格式化调用时需要检查和重新初始化快速路径状态。 */
    private transient boolean fastPathCheckNeeded = true;

    /** DecimalFormat 对其 FastPathData 的引用。 */
    private transient FastPathData fastPathData;

    //----------------------------------------------------------------------

    static final int currentSerialVersion = 4;

    /**
     * 内部序列化版本，表示写入的版本。可能的值包括：
     * <ul>
     * <li><b>0</b>（默认值）：Java 2 平台 v1.2 之前的版本
     * <li><b>1</b>：1.2 版本，包括两个新字段 <code>useExponentialNotation</code> 和 <code>minExponentDigits</code>。
     * <li><b>2</b>：1.3 及更高版本，增加了四个新字段： <code>posPrefixPattern</code>、<code>posSuffixPattern</code>、<code>negPrefixPattern</code> 和 <code>negSuffixPattern</code>。
     * <li><b>3</b>：1.5 及更高版本，增加了五个新字段： <code>maximumIntegerDigits</code>、<code>minimumIntegerDigits</code>、<code>maximumFractionDigits</code>、<code>minimumFractionDigits</code> 和 <code>parseBigDecimal</code>。
     * <li><b>4</b>：1.6 及更高版本，增加了一个新字段： <code>roundingMode</code>。
     * </ul>
     * @since 1.2
     * @serial
     */
    private int serialVersionOnStream = currentSerialVersion;


    //----------------------------------------------------------------------
    // 常量
    //----------------------------------------------------------------------

    // ------ 双精度常量的快速路径 ------

    /** 用于应用快速路径算法的最大有效整数值 */
    private static final double MAX_INT_AS_DOUBLE = (double) Integer.MAX_VALUE;

    /**
     * 在快速路径方法中用于收集数字的数字数组。
     * 使用3个常量字符数组确保数字的快速收集
     */
    private static class DigitArrays {
        static final char[] DigitOnes1000 = new char[1000];
        static final char[] DigitTens1000 = new char[1000];
        static final char[] DigitHundreds1000 = new char[1000];

        // 按需初始化持有者类的数组
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
     * CURRENCY_SIGN 是货币的标准Unicode符号。它在模式中使用，并被货币符号替换，
     * 或者如果它被加倍，则被国际货币符号替换。如果在模式中看到 CURRENCY_SIGN，
     * 则小数分隔符将被货币小数分隔符替换。
     *
     * CURRENCY_SIGN 不进行本地化。
     */
    private static final char       CURRENCY_SIGN = '\u00A4';

    private static final char       QUOTE = '\'';

    private static FieldPosition[] EmptyFieldPositionArray = new FieldPosition[0];

    // Java double 的整数和小数位数的上限
    static final int DOUBLE_INTEGER_DIGITS  = 309;
    static final int DOUBLE_FRACTION_DIGITS = 340;

    // BigDecimal 和 BigInteger 的整数和小数位数的上限
    static final int MAXIMUM_INTEGER_DIGITS  = Integer.MAX_VALUE;
    static final int MAXIMUM_FRACTION_DIGITS = Integer.MAX_VALUE;

    // 声明 JDK 1.1 序列化兼容性。
    static final long serialVersionUID = 864413376551465018L;
}
