
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.spi.NumberFormatProvider;
import java.util.Currency;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.spi.LocaleServiceProvider;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

/**
 * <code>NumberFormat</code> 是所有数字格式的抽象基类。此类提供了格式化和解析数字的接口。 <code>NumberFormat</code> 还提供了确定哪些区域设置具有数字格式以及它们的名称的方法。
 *
 * <p>
 * <code>NumberFormat</code> 帮助你为任何区域设置格式化和解析数字。你的代码可以完全独立于小数点、千位分隔符或甚至特定的十进制数字的区域设置约定，甚至数字格式是否为十进制。
 *
 * <p>
 * 要为当前区域设置格式化数字，可以使用其中一个工厂类方法：
 * <blockquote>
 * <pre>{@code
 * myString = NumberFormat.getInstance().format(myNumber);
 * }</pre>
 * </blockquote>
 * 如果你要格式化多个数字，多次获取格式并使用它会更高效，这样系统就不必多次获取有关本地语言和国家/地区约定的信息。
 * <blockquote>
 * <pre>{@code
 * NumberFormat nf = NumberFormat.getInstance();
 * for (int i = 0; i < myNumber.length; ++i) {
 *     output.println(nf.format(myNumber[i]) + "; ");
 * }
 * }</pre>
 * </blockquote>
 * 要为不同的区域设置格式化数字，可以在调用 <code>getInstance</code> 时指定它。
 * <blockquote>
 * <pre>{@code
 * NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
 * }</pre>
 * </blockquote>
 * 你还可以使用 <code>NumberFormat</code> 来解析数字：
 * <blockquote>
 * <pre>{@code
 * myNumber = nf.parse(myString);
 * }</pre>
 * </blockquote>
 * 使用 <code>getInstance</code> 或 <code>getNumberInstance</code> 获取正常的数字格式。使用 <code>getIntegerInstance</code> 获取整数数字格式。使用 <code>getCurrencyInstance</code> 获取货币数字格式。使用 <code>getPercentInstance</code> 获取显示百分比的格式。使用此格式，像 0.53 这样的分数将显示为 53%。
 *
 * <p>
 * 你还可以使用 <code>setMinimumFractionDigits</code> 等方法来控制数字的显示。如果你希望对格式或解析有更多控制，或者希望给用户提供更多控制，可以尝试将从工厂方法获取的 <code>NumberFormat</code> 转换为 <code>DecimalFormat</code>。这适用于大多数区域设置；只需记住将其放在 <code>try</code> 块中，以防遇到不寻常的区域设置。
 *
 * <p>
 * <code>NumberFormat</code> 和 <code>DecimalFormat</code> 的设计使得某些控制仅影响格式化，而其他控制仅影响解析。以下是这些控制方法的详细描述：
 * <p>
 * setParseIntegerOnly：仅影响解析，例如：
 * 如果为 true，"3456.78" → 3456（并将解析位置设置在索引 6 之后）
 * 如果为 false，"3456.78" → 3456.78（并将解析位置设置在索引 8 之后）
 * 这与格式化无关。如果你希望在小数点后可能没有数字时不显示小数点，可以使用 setDecimalSeparatorAlwaysShown。
 * <p>
 * setDecimalSeparatorAlwaysShown：仅影响格式化，并且仅在小数点后可能没有数字时生效，例如模式为 "#,##0.##" 时：
 * 如果为 true，3456.00 → "3,456."
 * 如果为 false，3456.00 → "3456"
 * 这与解析无关。如果你希望解析在小数点处停止，可以使用 setParseIntegerOnly。
 *
 * <p>
 * 你还可以使用带有 <code>ParsePosition</code> 和 <code>FieldPosition</code> 的 <code>parse</code> 和 <code>format</code> 方法的多种形式来：
 * <ul>
 * <li> 逐步解析字符串的各个部分
 * <li> 对齐小数点和其他区域
 * </ul>
 * 例如，你可以以两种方式对齐数字：
 * <ol>
 * <li> 如果你使用的是等宽字体并使用空格对齐，
 *      可以在格式化调用中传递 <code>FieldPosition</code>，其中 <code>field</code> = <code>INTEGER_FIELD</code>。输出时，
 *      <code>getEndIndex</code> 将被设置为整数部分最后一个字符与小数点之间的偏移量。在字符串前面添加
 *      (desiredSpaceCount - getEndIndex) 个空格。
 *
 * <li> 如果你使用的是比例字体，
 *      而不是用空格填充，可以测量从开始到 <code>getEndIndex</code> 的字符串宽度（以像素为单位）。
 *      然后将笔移动
 *      (desiredPixelWidth - widthToAlignmentPoint) 个像素，然后再绘制文本。
 *      这也适用于没有小数点但可能在末尾有其他字符的情况，例如负数中的括号："(12)" 代表 -12。
 * </ol>
 *
 * <h3><a name="synchronization">同步</a></h3>
 *
 * <p>
 * 数字格式通常不是同步的。建议为每个线程创建单独的格式实例。如果多个线程同时访问格式，必须在外部进行同步。
 *
 * @see          DecimalFormat
 * @see          ChoiceFormat
 * @author       Mark Davis
 * @author       Helena Shih
 */
public abstract class NumberFormat extends Format  {

    /**
     * 用于构造 FieldPosition 对象的字段常量。表示应返回格式化数字的整数部分的位置。
     * @see java.text.FieldPosition
     */
    public static final int INTEGER_FIELD = 0;

    /**
     * 用于构造 FieldPosition 对象的字段常量。表示应返回格式化数字的小数部分的位置。
     * @see java.text.FieldPosition
     */
    public static final int FRACTION_FIELD = 1;

    /**
     * 唯一的构造函数。（通常由子类构造函数调用，通常是隐式的。）
     */
    protected NumberFormat() {
    }

    /**
     * 格式化数字并将结果文本追加到给定的字符串缓冲区。
     * 数字可以是 {@link java.lang.Number} 的任何子类。
     * <p>
     * 此实现使用 {@link java.lang.Number#longValue()} 提取数字的值，对于所有可以转换为 <code>long</code> 而不会丢失信息的整数类型值，包括 <code>BigInteger</code> 值的
     * {@link java.math.BigInteger#bitLength() 位长度} 小于 64 的值，以及其他所有类型使用 {@link java.lang.Number#doubleValue()}。然后调用
     * {@link #format(long,java.lang.StringBuffer,java.text.FieldPosition)}
     * 或 {@link #format(double,java.lang.StringBuffer,java.text.FieldPosition)}。
     * 这可能会导致 <code>BigInteger</code> 和 <code>BigDecimal</code> 值的大小信息和精度丢失。
     * @param number     要格式化的数字
     * @param toAppendTo 要追加格式化文本的 <code>StringBuffer</code>
     * @param pos        输入：对齐字段（如果需要）。输出：对齐字段的偏移量。
     * @return           传递的 <code>toAppendTo</code> 值
     * @exception        IllegalArgumentException 如果 <code>number</code> 为 null 或不是 <code>Number</code> 的实例。
     * @exception        NullPointerException 如果 <code>toAppendTo</code> 或 <code>pos</code> 为 null
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see              java.text.FieldPosition
     */
    @Override
    public StringBuffer format(Object number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        if (number instanceof Long || number instanceof Integer ||
            number instanceof Short || number instanceof Byte ||
            number instanceof AtomicInteger || number instanceof AtomicLong ||
            (number instanceof BigInteger &&
             ((BigInteger)number).bitLength() < 64)) {
            return format(((Number)number).longValue(), toAppendTo, pos);
        } else if (number instanceof Number) {
            return format(((Number)number).doubleValue(), toAppendTo, pos);
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Number");
        }
    }

    /**
     * 从字符串中解析文本以生成 <code>Number</code>。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为最后一个使用字符之后的索引（解析不一定使用到字符串的末尾），并返回解析的数字。更新后的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会改变，<code>pos</code> 的错误索引将设置为发生错误的字符索引，返回 null。
     * <p>
     * 有关数字解析的更多信息，请参阅 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 应解析的部分 <code>String</code>
     * @param pos 包含索引和错误索引信息的 <code>ParsePosition</code> 对象，如上所述。
     * @return 从字符串解析的 <code>Number</code>。发生错误时，返回 null。
     * @exception NullPointerException 如果 <code>pos</code> 为 null。
     */
    @Override
    public final Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

   /**
     * 格式化的特化。
     *
     * @param number 要格式化的双精度浮点数
     * @return 格式化的字符串
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public final String format(double number) {
        // 如果快速路径有效，则使用快速路径
        String result = fastFormat(number);
        if (result != null)
            return result;

        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }

    /*
     * fastFormat() 应仅在具体子类中实现。
     * 默认实现始终返回 null。
     */
    String fastFormat(double number) { return null; }

   /**
     * 格式化的特化。
     *
     * @param number 要格式化的长整数
     * @return 格式化的字符串
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public final String format(long number) {
        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }

   /**
     * 格式化的特化。
     *
     * @param number     要格式化的双精度浮点数
     * @param toAppendTo 要追加格式化文本的 StringBuffer
     * @param pos        字段位置
     * @return 格式化的 StringBuffer
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public abstract StringBuffer format(double number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);

   /**
     * 格式化的特化。
     *
     * @param number     要格式化的长整数
     * @param toAppendTo 要追加格式化文本的 StringBuffer
     * @param pos        字段位置
     * @return 格式化的 StringBuffer
     * @exception        ArithmeticException 如果需要舍入且舍入模式设置为 RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public abstract StringBuffer format(long number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);

   /**
     * 如果可能（例如，在 [Long.MIN_VALUE, Long.MAX_VALUE] 范围内且没有小数），返回 Long，否则返回 Double。
     * 如果设置了 IntegerOnly，将在小数点（或等效字符；例如，对于有理数 "1 2/3"，将在 1 之后停止）处停止。
     * 不抛出异常；如果无法解析对象，索引将保持不变！
     *
     * @param source 要解析的字符串
     * @param parsePosition 解析位置
     * @return 解析的值
     * @see java.text.NumberFormat#isParseIntegerOnly
     * @see java.text.Format#parseObject
     */
    public abstract Number parse(String source, ParsePosition parsePosition);


                /**
     * 从给定字符串的开头解析文本以生成一个数字。
     * 该方法可能不会使用给定字符串的全部文本。
     * <p>
     * 有关数字解析的更多信息，请参阅 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个应从其开头解析的 <code>String</code>。
     * @return 从字符串解析出的 <code>Number</code>。
     * @exception ParseException 如果指定字符串的开头无法解析。
     */
    public Number parse(String source) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = parse(source, parsePosition);
        if (parsePosition.index == 0) {
            throw new ParseException("无法解析的数字: \"" + source + "\"",
                                     parsePosition.errorIndex);
        }
        return result;
    }

    /**
     * 如果此格式将数字解析为仅整数，则返回 true。
     * 例如，在英语区域设置中，当 ParseIntegerOnly 为 true 时，字符串 "1234." 将被解析为整数值 1234，解析将在 "." 字符处停止。当然，解析操作接受的确切格式取决于 NumberFormat 的子类，并且是区域设置相关的。
     *
     * @return 如果数字应仅解析为整数，则返回 {@code true}；否则返回 {@code false}
     */
    public boolean isParseIntegerOnly() {
        return parseIntegerOnly;
    }

    /**
     * 设置数字是否应仅解析为整数。
     *
     * @param value 如果数字应仅解析为整数，则返回 {@code true}；否则返回 {@code false}
     * @see #isParseIntegerOnly
     */
    public void setParseIntegerOnly(boolean value) {
        parseIntegerOnly = value;
    }

    //============== 区域设置相关 =====================

    /**
     * 返回当前默认 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的通用数字格式。
     * 这等同于调用 {@link #getNumberInstance() getNumberInstance()}。
     *
     * @return 用于通用数字格式化的 {@code NumberFormat} 实例
     */
    public final static NumberFormat getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), NUMBERSTYLE);
    }

    /**
     * 返回指定区域设置的通用数字格式。
     * 这等同于调用 {@link #getNumberInstance(java.util.Locale) getNumberInstance(inLocale)}。
     *
     * @param inLocale 所需的区域设置
     * @return 用于通用数字格式化的 {@code NumberFormat} 实例
     */
    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    /**
     * 返回当前默认 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的通用数字格式。
     * <p>这等同于调用
     * {@link #getNumberInstance(Locale)
     *     getNumberInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @return 用于通用数字格式化的 {@code NumberFormat} 实例
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     */
    public final static NumberFormat getNumberInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), NUMBERSTYLE);
    }

    /**
     * 返回指定区域设置的通用数字格式。
     *
     * @param inLocale 所需的区域设置
     * @return 用于通用数字格式化的 {@code NumberFormat} 实例
     */
    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    /**
     * 返回当前默认 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的整数数字格式。返回的数字格式配置为使用半偶舍入（参见 {@link
     * java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}）对浮点数进行格式化，以四舍五入到最接近的整数，并且仅解析输入字符串的整数部分（参见 {@link
     * #isParseIntegerOnly isParseIntegerOnly}）。
     * <p>这等同于调用
     * {@link #getIntegerInstance(Locale)
     *     getIntegerInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @see #getRoundingMode()
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 用于整数值的数字格式
     * @since 1.4
     */
    public final static NumberFormat getIntegerInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), INTEGERSTYLE);
    }

    /**
     * 返回指定区域设置的整数数字格式。返回的数字格式配置为使用半偶舍入（参见 {@link
     * java.math.RoundingMode#HALF_EVEN RoundingMode.HALF_EVEN}）对浮点数进行格式化，以四舍五入到最接近的整数，并且仅解析输入字符串的整数部分（参见 {@link
     * #isParseIntegerOnly isParseIntegerOnly}）。
     *
     * @param inLocale 所需的区域设置
     * @see #getRoundingMode()
     * @return 用于整数值的数字格式
     * @since 1.4
     */
    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(inLocale, INTEGERSTYLE);
    }

    /**
     * 返回当前默认 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的货币格式。
     * <p>这等同于调用
     * {@link #getCurrencyInstance(Locale)
     *     getCurrencyInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @return 用于货币格式化的 {@code NumberFormat} 实例
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     */
    public final static NumberFormat getCurrencyInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), CURRENCYSTYLE);
    }

    /**
     * 返回指定区域设置的货币格式。
     *
     * @param inLocale 所需的区域设置
     * @return 用于货币格式化的 {@code NumberFormat} 实例
     */
    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(inLocale, CURRENCYSTYLE);
    }

    /**
     * 返回当前默认 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的百分比格式。
     * <p>这等同于调用
     * {@link #getPercentInstance(Locale)
     *     getPercentInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @return 用于百分比格式化的 {@code NumberFormat} 实例
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     */
    public final static NumberFormat getPercentInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), PERCENTSTYLE);
    }

    /**
     * 返回指定区域设置的百分比格式。
     *
     * @param inLocale 所需的区域设置
     * @return 用于百分比格式化的 {@code NumberFormat} 实例
     */
    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(inLocale, PERCENTSTYLE);
    }

    /**
     * 返回当前默认区域设置的科学计数格式。
     */
    /*public*/ final static NumberFormat getScientificInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), SCIENTIFICSTYLE);
    }

    /**
     * 返回指定区域设置的科学计数格式。
     *
     * @param inLocale 所需的区域设置
     */
    /*public*/ static NumberFormat getScientificInstance(Locale inLocale) {
        return getInstance(inLocale, SCIENTIFICSTYLE);
    }

    /**
     * 返回此类的 <code>get*Instance</code> 方法可以返回本地化实例的所有区域设置。
     * 返回的数组表示 Java 运行时支持的区域设置和已安装的
     * {@link java.text.spi.NumberFormatProvider NumberFormatProvider} 实现支持的区域设置的并集。
     * 它必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 可用本地化 <code>NumberFormat</code> 实例的区域设置数组。
     */
    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(NumberFormatProvider.class);
        return pool.getAvailableLocales();
    }

    /**
     * 覆盖 hashCode。
     */
    @Override
    public int hashCode() {
        return maximumIntegerDigits * 37 + maxFractionDigits;
        // 足够的字段以获得合理的分布
    }

    /**
     * 覆盖 equals。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberFormat other = (NumberFormat) obj;
        return (maximumIntegerDigits == other.maximumIntegerDigits
            && minimumIntegerDigits == other.minimumIntegerDigits
            && maximumFractionDigits == other.maximumFractionDigits
            && minimumFractionDigits == other.minimumFractionDigits
            && groupingUsed == other.groupingUsed
            && parseIntegerOnly == other.parseIntegerOnly);
    }

    /**
     * 覆盖 Cloneable。
     */
    @Override
    public Object clone() {
        NumberFormat other = (NumberFormat) super.clone();
        return other;
    }

    /**
     * 如果此格式中使用了分组，则返回 true。例如，在英语区域设置中，当分组开启时，数字 1234567 可能会被格式化为 "1,234,567"。分组分隔符以及每个分组的大小取决于区域设置，并由 NumberFormat 的子类确定。
     *
     * @return 如果使用分组，则返回 {@code true}；否则返回 {@code false}
     * @see #setGroupingUsed
     */
    public boolean isGroupingUsed() {
        return groupingUsed;
    }

    /**
     * 设置此格式中是否使用分组。
     *
     * @param newValue 如果使用分组，则返回 {@code true}；否则返回 {@code false}
     * @see #isGroupingUsed
     */
    public void setGroupingUsed(boolean newValue) {
        groupingUsed = newValue;
    }

    /**
     * 返回数字的整数部分允许的最大位数。
     *
     * @return 最大位数
     * @see #setMaximumIntegerDigits
     */
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }

    /**
     * 设置数字的整数部分允许的最大位数。maximumIntegerDigits 必须 &ge; minimumIntegerDigits。如果新的 maximumIntegerDigits 值小于当前的 minimumIntegerDigits 值，则 minimumIntegerDigits 也将被设置为新的值。
     *
     * @param newValue 要显示的最大整数位数；如果小于零，则使用零。具体子类可能会根据要格式化的数字类型强制执行此值的上限。
     * @see #getMaximumIntegerDigits
     */
    public void setMaximumIntegerDigits(int newValue) {
        maximumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            minimumIntegerDigits = maximumIntegerDigits;
        }
    }

    /**
     * 返回数字的整数部分允许的最小位数。
     *
     * @return 最小位数
     * @see #setMinimumIntegerDigits
     */
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    /**
     * 设置数字的整数部分允许的最小位数。minimumIntegerDigits 必须 &le; maximumIntegerDigits。如果新的 minimumIntegerDigits 值超过当前的 maximumIntegerDigits 值，则 maximumIntegerDigits 也将被设置为新的值。
     *
     * @param newValue 要显示的最小整数位数；如果小于零，则使用零。具体子类可能会根据要格式化的数字类型强制执行此值的上限。
     * @see #getMinimumIntegerDigits
     */
    public void setMinimumIntegerDigits(int newValue) {
        minimumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            maximumIntegerDigits = minimumIntegerDigits;
        }
    }

    /**
     * 返回数字的小数部分允许的最大位数。
     *
     * @return 最大位数。
     * @see #setMaximumFractionDigits
     */
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * 设置数字的小数部分允许的最大位数。maximumFractionDigits 必须 &ge; minimumFractionDigits。如果新的 maximumFractionDigits 值小于当前的 minimumFractionDigits 值，则 minimumFractionDigits 也将被设置为新的值。
     *
     * @param newValue 要显示的最大小数位数；如果小于零，则使用零。具体子类可能会根据要格式化的数字类型强制执行此值的上限。
     * @see #getMaximumFractionDigits
     */
    public void setMaximumFractionDigits(int newValue) {
        maximumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits) {
            minimumFractionDigits = maximumFractionDigits;
        }
    }

    /**
     * 返回数字的小数部分允许的最小位数。
     *
     * @return 最小位数
     * @see #setMinimumFractionDigits
     */
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * 设置数字的小数部分允许的最小位数。minimumFractionDigits 必须 &le; maximumFractionDigits。如果新的 minimumFractionDigits 值超过当前的 maximumFractionDigits 值，则 maximumIntegerDigits 也将被设置为新的值。
     *
     * @param newValue 要显示的最小小数位数；如果小于零，则使用零。具体子类可能会根据要格式化的数字类型强制执行此值的上限。
     * @see #getMinimumFractionDigits
     */
    public void setMinimumFractionDigits(int newValue) {
        minimumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits) {
            maximumFractionDigits = minimumFractionDigits;
        }
    }


                /**
     * 获取此数字格式在格式化货币值时使用的货币。初始值以地区依赖的方式派生。
     * 返回的值可能为 null，如果无法确定有效的货币且未使用
     * {@link #setCurrency(java.util.Currency) setCurrency} 设置货币。
     * <p>
     * 默认实现抛出
     * <code>UnsupportedOperationException</code>。
     *
     * @return 此数字格式使用的货币，或 <code>null</code>
     * @exception UnsupportedOperationException 如果数字格式类不实现货币格式化
     * @since 1.4
     */
    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }

    /**
     * 设置此数字格式在格式化货币值时使用的货币。这不会更新数字格式使用的最小或最大
     * 小数位数。
     * <p>
     * 默认实现抛出
     * <code>UnsupportedOperationException</code>。
     *
     * @param currency 此数字格式将使用的新货币
     * @exception UnsupportedOperationException 如果数字格式类不实现货币格式化
     * @exception NullPointerException 如果 <code>currency</code> 为 null
     * @since 1.4
     */
    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取此 NumberFormat 使用的 {@link java.math.RoundingMode}。
     * NumberFormat 中此方法的默认实现总是抛出 {@link java.lang.UnsupportedOperationException}。
     * 处理不同舍入模式的子类应覆盖此方法。
     *
     * @exception UnsupportedOperationException 默认实现总是抛出此异常
     * @return 用于此 NumberFormat 的 <code>RoundingMode</code>。
     * @see #setRoundingMode(RoundingMode)
     * @since 1.6
     */
    public RoundingMode getRoundingMode() {
        throw new UnsupportedOperationException();
    }

    /**
     * 设置此 NumberFormat 使用的 {@link java.math.RoundingMode}。
     * NumberFormat 中此方法的默认实现总是抛出 {@link java.lang.UnsupportedOperationException}。
     * 处理不同舍入模式的子类应覆盖此方法。
     *
     * @exception UnsupportedOperationException 默认实现总是抛出此异常
     * @exception NullPointerException 如果 <code>roundingMode</code> 为 null
     * @param roundingMode 要使用的 <code>RoundingMode</code>
     * @see #getRoundingMode()
     * @since 1.6
     */
    public void setRoundingMode(RoundingMode roundingMode) {
        throw new UnsupportedOperationException();
    }

    // =======================privates===============================

    private static NumberFormat getInstance(Locale desiredLocale,
                                           int choice) {
        LocaleProviderAdapter adapter;
        adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class,
                                                   desiredLocale);
        NumberFormat numberFormat = getInstance(adapter, desiredLocale, choice);
        if (numberFormat == null) {
            numberFormat = getInstance(LocaleProviderAdapter.forJRE(),
                                       desiredLocale, choice);
        }
        return numberFormat;
    }

    private static NumberFormat getInstance(LocaleProviderAdapter adapter,
                                            Locale locale, int choice) {
        NumberFormatProvider provider = adapter.getNumberFormatProvider();
        NumberFormat numberFormat = null;
        switch (choice) {
        case NUMBERSTYLE:
            numberFormat = provider.getNumberInstance(locale);
            break;
        case PERCENTSTYLE:
            numberFormat = provider.getPercentInstance(locale);
            break;
        case CURRENCYSTYLE:
            numberFormat = provider.getCurrencyInstance(locale);
            break;
        case INTEGERSTYLE:
            numberFormat = provider.getIntegerInstance(locale);
            break;
        }
        return numberFormat;
    }

    /**
     * 首先，读取默认的可序列化数据。
     *
     * 然后，如果 <code>serialVersionOnStream</code> 小于 1，表示该流由 JDK 1.1 写入，
     * 将 <code>int</code> 字段（如 <code>maximumIntegerDigits</code>）设置为等于
     * <code>byte</code> 字段（如 <code>maxIntegerDigits</code>），因为这些 <code>int</code>
     * 字段在 JDK 1.1 中不存在。最后，将 <code>serialVersionOnStream</code> 设置回最大允许值，
     * 以便如果再次将此对象流化，可以正确地进行默认序列化。
     *
     * <p>如果 <code>minimumIntegerDigits</code> 大于
     * <code>maximumIntegerDigits</code> 或 <code>minimumFractionDigits</code>
     * 大于 <code>maximumFractionDigits</code>，则流数据无效，此方法将抛出
     * <code>InvalidObjectException</code>。此外，如果这些值中的任何一个为负数，此方法将抛出
     * <code>InvalidObjectException</code>。
     *
     * @since 1.2
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        if (serialVersionOnStream < 1) {
            // 没有额外的 int 字段，重新分配以使用它们。
            maximumIntegerDigits = maxIntegerDigits;
            minimumIntegerDigits = minIntegerDigits;
            maximumFractionDigits = maxFractionDigits;
            minimumFractionDigits = minFractionDigits;
        }
        if (minimumIntegerDigits > maximumIntegerDigits ||
            minimumFractionDigits > maximumFractionDigits ||
            minimumIntegerDigits < 0 || minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        serialVersionOnStream = currentSerialVersion;
    }

    /**
     * 写出默认的可序列化数据，首先将 <code>byte</code> 字段（如 <code>maxIntegerDigits</code>）
     * 设置为等于 <code>int</code> 字段（如 <code>maximumIntegerDigits</code>）（或设置为
     * <code>Byte.MAX_VALUE</code>，取较小者），以与 JDK 1.1 版本的流格式兼容。
     *
     * @since 1.2
     */
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        maxIntegerDigits = (maximumIntegerDigits > Byte.MAX_VALUE) ?
                           Byte.MAX_VALUE : (byte)maximumIntegerDigits;
        minIntegerDigits = (minimumIntegerDigits > Byte.MAX_VALUE) ?
                           Byte.MAX_VALUE : (byte)minimumIntegerDigits;
        maxFractionDigits = (maximumFractionDigits > Byte.MAX_VALUE) ?
                            Byte.MAX_VALUE : (byte)maximumFractionDigits;
        minFractionDigits = (minimumFractionDigits > Byte.MAX_VALUE) ?
                            Byte.MAX_VALUE : (byte)minimumFractionDigits;
        stream.defaultWriteObject();
    }

    // 工厂方法用于指定格式样式的常量。
    private static final int NUMBERSTYLE = 0;
    private static final int CURRENCYSTYLE = 1;
    private static final int PERCENTSTYLE = 2;
    private static final int SCIENTIFICSTYLE = 3;
    private static final int INTEGERSTYLE = 4;

    /**
     * 如果在格式化和解析数字时使用分组（即千位）分隔符，则为 true。
     *
     * @serial
     * @see #isGroupingUsed
     */
    private boolean groupingUsed = true;

    /**
     * 允许在数字整数部分中的最大位数。 <code>maxIntegerDigits</code> 必须大于或等于
     * <code>minIntegerDigits</code>。
     * <p>
     * <strong>注意：</strong>此字段仅用于与 JDK 1.1 的序列化兼容性。在 Java 平台 2 v1.2 及更高版本中，
     * 使用新的 <code>int</code> 字段 <code>maximumIntegerDigits</code>。在写入流时，
     * <code>maxIntegerDigits</code> 被设置为 <code>maximumIntegerDigits</code> 或
     * <code>Byte.MAX_VALUE</code>，取较小者。在从流中读取时，仅当 <code>serialVersionOnStream</code>
     * 小于 1 时才使用此字段。
     *
     * @serial
     * @see #getMaximumIntegerDigits
     */
    private byte    maxIntegerDigits = 40;

    /**
     * 允许在数字整数部分中的最小位数。 <code>minimumIntegerDigits</code> 必须小于或等于
     * <code>maximumIntegerDigits</code>。
     * <p>
     * <strong>注意：</strong>此字段仅用于与 JDK 1.1 的序列化兼容性。在 Java 平台 2 v1.2 及更高版本中，
     * 使用新的 <code>int</code> 字段 <code>minimumIntegerDigits</code>。在写入流时，
     * <code>minIntegerDigits</code> 被设置为 <code>minimumIntegerDigits</code> 或
     * <code>Byte.MAX_VALUE</code>，取较小者。在从流中读取时，仅当 <code>serialVersionOnStream</code>
     * 小于 1 时才使用此字段。
     *
     * @serial
     * @see #getMinimumIntegerDigits
     */
    private byte    minIntegerDigits = 1;

    /**
     * 允许在数字小数部分中的最大位数。 <code>maximumFractionDigits</code> 必须大于或等于
     * <code>minimumFractionDigits</code>。
     * <p>
     * <strong>注意：</strong>此字段仅用于与 JDK 1.1 的序列化兼容性。在 Java 平台 2 v1.2 及更高版本中，
     * 使用新的 <code>int</code> 字段 <code>maximumFractionDigits</code>。在写入流时，
     * <code>maxFractionDigits</code> 被设置为 <code>maximumFractionDigits</code> 或
     * <code>Byte.MAX_VALUE</code>，取较小者。在从流中读取时，仅当 <code>serialVersionOnStream</code>
     * 小于 1 时才使用此字段。
     *
     * @serial
     * @see #getMaximumFractionDigits
     */
    private byte    maxFractionDigits = 3;    // 不变，>= minFractionDigits

    /**
     * 允许在数字小数部分中的最小位数。 <code>minimumFractionDigits</code> 必须小于或等于
     * <code>maximumFractionDigits</code>。
     * <p>
     * <strong>注意：</strong>此字段仅用于与 JDK 1.1 的序列化兼容性。在 Java 平台 2 v1.2 及更高版本中，
     * 使用新的 <code>int</code> 字段 <code>minimumFractionDigits</code>。在写入流时，
     * <code>minFractionDigits</code> 被设置为 <code>minimumFractionDigits</code> 或
     * <code>Byte.MAX_VALUE</code>，取较小者。在从流中读取时，仅当 <code>serialVersionOnStream</code>
     * 小于 1 时才使用此字段。
     *
     * @serial
     * @see #getMinimumFractionDigits
     */
    private byte    minFractionDigits = 0;

    /**
     * 如果此格式将仅解析整数，则为 true。
     *
     * @serial
     * @see #isParseIntegerOnly
     */
    private boolean parseIntegerOnly = false;

    // 1.2 版本的新字段。byte 类型对于整数位数来说太小了。

    /**
     * 允许在数字整数部分中的最大位数。 <code>maximumIntegerDigits</code> 必须大于或等于
     * <code>minimumIntegerDigits</code>。
     *
     * @serial
     * @since 1.2
     * @see #getMaximumIntegerDigits
     */
    private int    maximumIntegerDigits = 40;

    /**
     * 允许在数字整数部分中的最小位数。 <code>minimumIntegerDigits</code> 必须小于或等于
     * <code>maximumIntegerDigits</code>。
     *
     * @serial
     * @since 1.2
     * @see #getMinimumIntegerDigits
     */
    private int    minimumIntegerDigits = 1;

    /**
     * 允许在数字小数部分中的最大位数。 <code>maximumFractionDigits</code> 必须大于或等于
     * <code>minimumFractionDigits</code>。
     *
     * @serial
     * @since 1.2
     * @see #getMaximumFractionDigits
     */
    private int    maximumFractionDigits = 3;    // 不变，>= minFractionDigits

    /**
     * 允许在数字小数部分中的最小位数。 <code>minimumFractionDigits</code> 必须小于或等于
     * <code>maximumFractionDigits</code>。
     *
     * @serial
     * @since 1.2
     * @see #getMinimumFractionDigits
     */
    private int    minimumFractionDigits = 0;

    static final int currentSerialVersion = 1;

    /**
     * 描述流中 <code>NumberFormat</code> 的版本。可能的值为：
     * <ul>
     * <li><b>0</b>（或未初始化）：JDK 1.1 版本的流格式。在此版本中，
     *     <code>int</code> 字段（如 <code>maximumIntegerDigits</code>）不存在，
     *     而使用 <code>byte</code> 字段（如 <code>maxIntegerDigits</code>）。
     *
     * <li><b>1</b>：1.2 版本的流格式。忽略 <code>byte</code> 字段（如 <code>maxIntegerDigits</code>）的值，
     *     而使用 <code>int</code> 字段（如 <code>maximumIntegerDigits</code>）。
     * </ul>
     * 在流化 <code>NumberFormat</code> 时，总是写入最新格式（对应于最高的允许 <code>serialVersionOnStream</code>）。
     *
     * @serial
     * @since 1.2
     */
    private int serialVersionOnStream = currentSerialVersion;

    // 移除了 "implements Cloneable" 子句。需要更新序列化 ID 以保持向后兼容性。
    static final long serialVersionUID = -2308460125733713944L;


    //
    // 用于 AttributedCharacterIterator 属性的类
    //
    /**
     * 定义在 <code>NumberFormat.formatToCharacterIterator</code> 返回的
     * <code>AttributedCharacterIterator</code> 中使用的属性键，以及
     * <code>FieldPosition</code> 中的字段标识符。
     *
     * @since 1.4
     */
    public static class Field extends Format.Field {

        // 声明与 1.4 FCS 兼容
        private static final long serialVersionUID = 7494728892700160890L;

        // 本类中所有实例的表，用于 readResolve
        private static final Map<String, Field> instanceMap = new HashMap<>(11);

        /**
         * 使用指定的名称创建 Field 实例。
         *
         * @param name 属性的名称
         */
        protected Field(String name) {
            super(name);
            if (this.getClass() == NumberFormat.Field.class) {
                instanceMap.put(name, this);
            }
        }

        /**
         * 将反序列化的实例解析为预定义的常量。
         *
         * @throws InvalidObjectException 如果常量无法解析
         * @return 解析后的 NumberFormat.Field 常量
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != NumberFormat.Field.class) {
                throw new InvalidObjectException("子类未正确实现 readResolve");
            }


                        Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("未知的属性名称");
            }
        }

        /**
         * 标识整数字段的常量。
         */
        public static final Field INTEGER = new Field("integer");

        /**
         * 标识分数字段的常量。
         */
        public static final Field FRACTION = new Field("fraction");

        /**
         * 标识指数字段的常量。
         */
        public static final Field EXPONENT = new Field("exponent");

        /**
         * 标识小数点分隔符字段的常量。
         */
        public static final Field DECIMAL_SEPARATOR =
                            new Field("decimal separator");

        /**
         * 标识符号字段的常量。
         */
        public static final Field SIGN = new Field("sign");

        /**
         * 标识千位分隔符字段的常量。
         */
        public static final Field GROUPING_SEPARATOR =
                            new Field("grouping separator");

        /**
         * 标识指数符号字段的常量。
         */
        public static final Field EXPONENT_SYMBOL = new
                            Field("exponent symbol");

        /**
         * 标识百分号字段的常量。
         */
        public static final Field PERCENT = new Field("percent");

        /**
         * 标识千分号字段的常量。
         */
        public static final Field PERMILLE = new Field("per mille");

        /**
         * 标识货币字段的常量。
         */
        public static final Field CURRENCY = new Field("currency");

        /**
         * 标识指数符号字段的常量。
         */
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
    }
}
