
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
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
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
import java.text.spi.DateFormatProvider;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.spi.LocaleServiceProvider;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

/**
 * {@code DateFormat} 是一个抽象类，用于定义语言无关的日期/时间格式化子类。
 * 日期/时间格式化子类，如 {@link SimpleDateFormat}，允许进行格式化（即，日期 &rarr; 文本）、解析（文本 &rarr; 日期）和规范化。
 * 日期表示为 <code>Date</code> 对象或自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
 *
 * <p>{@code DateFormat} 提供了许多类方法，用于根据默认或给定的区域设置和多种格式样式获取默认的日期/时间格式器。
 * 格式样式包括 {@link #FULL}、{@link #LONG}、{@link #MEDIUM} 和 {@link #SHORT}。更多关于使用这些样式的详细信息和示例请参见方法描述。
 *
 * <p>{@code DateFormat} 可帮助您以任何区域设置格式化和解析日期。
 * 您的代码可以完全独立于月份、星期几或日历格式（阴历与阳历）的区域设置惯例。
 *
 * <p>要为当前区域设置格式化日期，可以使用以下静态工厂方法之一：
 * <blockquote>
 * <pre>{@code
 * myString = DateFormat.getDateInstance().format(myDate);
 * }</pre>
 * </blockquote>
 * <p>如果您要格式化多个日期，多次使用格式器会更高效，这样系统不必多次获取有关本地语言和国家/地区惯例的信息。
 * <blockquote>
 * <pre>{@code
 * DateFormat df = DateFormat.getDateInstance();
 * for (int i = 0; i < myDate.length; ++i) {
 *     output.println(df.format(myDate[i]) + "; ");
 * }
 * }</pre>
 * </blockquote>
 * <p>要为不同的区域设置格式化日期，请在调用 {@link #getDateInstance(int, Locale) getDateInstance()} 时指定它。
 * <blockquote>
 * <pre>{@code
 * DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
 * }</pre>
 * </blockquote>
 * <p>您也可以使用 {@code DateFormat} 进行解析。
 * <blockquote>
 * <pre>{@code
 * myDate = df.parse(myString);
 * }</pre>
 * </blockquote>
 * <p>使用 {@code getDateInstance} 获取该国家/地区的正常日期格式。
 * 还有其他静态工厂方法可用。
 * 使用 {@code getTimeInstance} 获取该国家/地区的时间格式。
 * 使用 {@code getDateTimeInstance} 获取日期和时间格式。您可以传递不同的选项到这些工厂方法以控制结果的长度；
 * 从 {@link #SHORT} 到 {@link #MEDIUM} 到 {@link #LONG} 到 {@link #FULL}。具体结果取决于区域设置，但通常：
 * <ul><li>{@link #SHORT} 完全为数字，如 {@code 12.13.52} 或 {@code 3:30pm}
 * <li>{@link #MEDIUM} 更长，如 {@code Jan 12, 1952}
 * <li>{@link #LONG} 更长，如 {@code January 12, 1952} 或 {@code 3:30:32pm}
 * <li>{@link #FULL} 几乎完全指定，如
 * {@code Tuesday, April 12, 1952 AD or 3:30:42pm PST}。
 * </ul>
 *
 * <p>您也可以设置格式的时间区，如果需要的话。
 * 如果您希望对格式或解析进行更多控制（或给予用户更多控制），
 * 可以尝试将从工厂方法获取的 {@code DateFormat} 转换为 {@link SimpleDateFormat}。这适用于大多数国家/地区；
 * 只是记得将其放在 {@code try} 块中，以防遇到不寻常的情况。
 *
 * <p>您还可以使用带有 {@link ParsePosition} 和 {@link FieldPosition} 的解析和格式化方法的变体来
 * <ul><li>逐步解析字符串的各个部分。
 * <li>对任何特定字段进行对齐，或找出它在屏幕上的位置。
 * </ul>
 *
 * <h3><a name="synchronization">同步</a></h3>
 *
 * <p>
 * 日期格式化器不是同步的。
 * 建议为每个线程创建单独的格式实例。
 * 如果多个线程同时访问格式化器，必须在外部进行同步。
 *
 * @see          Format
 * @see          NumberFormat
 * @see          SimpleDateFormat
 * @see          java.util.Calendar
 * @see          java.util.GregorianCalendar
 * @see          java.util.TimeZone
 * @author       Mark Davis, Chen-Lieh Huang, Alan Liu
 */
public abstract class DateFormat extends Format {

    /**
     * 用于计算日期时间字段和时间点的 {@link Calendar} 实例。此字段用于格式化和解析。
     *
     * <p>子类应将此字段初始化为与该 {@link Locale} 相关的 {@link Calendar}。
     * @serial
     */
    protected Calendar calendar;

    /**
     * 用于格式化日期和时间中数字的数字格式器。子类应将此字段初始化为与该 {@link Locale} 相关的数字格式。
     * @serial
     */
    protected NumberFormat numberFormat;

    /**
     * 用于 ERA 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int ERA_FIELD = 0;
    /**
     * 用于 YEAR 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int YEAR_FIELD = 1;
    /**
     * 用于 MONTH 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int MONTH_FIELD = 2;
    /**
     * 用于 DATE 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int DATE_FIELD = 3;
    /**
     * 用于基于 1 的 HOUR_OF_DAY 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     * HOUR_OF_DAY1_FIELD 用于基于 1 的 24 小时时钟。
     * 例如，23:59 + 01:00 结果为 24:59。
     */
    public final static int HOUR_OF_DAY1_FIELD = 4;
    /**
     * 用于基于 0 的 HOUR_OF_DAY 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     * HOUR_OF_DAY0_FIELD 用于基于 0 的 24 小时时钟。
     * 例如，23:59 + 01:00 结果为 00:59。
     */
    public final static int HOUR_OF_DAY0_FIELD = 5;
    /**
     * 用于 MINUTE 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int MINUTE_FIELD = 6;
    /**
     * 用于 SECOND 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int SECOND_FIELD = 7;
    /**
     * 用于 MILLISECOND 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int MILLISECOND_FIELD = 8;
    /**
     * 用于 DAY_OF_WEEK 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int DAY_OF_WEEK_FIELD = 9;
    /**
     * 用于 DAY_OF_YEAR 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int DAY_OF_YEAR_FIELD = 10;
    /**
     * 用于 DAY_OF_WEEK_IN_MONTH 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
    /**
     * 用于 WEEK_OF_YEAR 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int WEEK_OF_YEAR_FIELD = 12;
    /**
     * 用于 WEEK_OF_MONTH 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int WEEK_OF_MONTH_FIELD = 13;
    /**
     * 用于 AM_PM 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int AM_PM_FIELD = 14;
    /**
     * 用于基于 1 的 HOUR 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     * HOUR1_FIELD 用于基于 1 的 12 小时时钟。
     * 例如，11:30 PM + 1 小时结果为 12:30 AM。
     */
    public final static int HOUR1_FIELD = 15;
    /**
     * 用于基于 0 的 HOUR 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     * HOUR0_FIELD 用于基于 0 的 12 小时时钟。
     * 例如，11:30 PM + 1 小时结果为 00:30 AM。
     */
    public final static int HOUR0_FIELD = 16;
    /**
     * 用于 TIMEZONE 字段对齐的有用常量。
     * 用于日期/时间格式化中的 FieldPosition。
     */
    public final static int TIMEZONE_FIELD = 17;

    // 声明与 1.1 FCS 的序列化兼容性
    private static final long serialVersionUID = 7218322306649953788L;

    /**
     * 覆盖 Format。
     * 将时间对象格式化为时间字符串。时间对象的例子包括以毫秒表示的时间值和 Date 对象。
     * @param obj 必须是 Number 或 Date。
     * @param toAppendTo 返回的时间字符串的字符串缓冲区。
     * @return 作为 toAppendTo 传递的字符串缓冲区，附加了格式化的文本。
     * @param fieldPosition 跟踪返回字符串中字段的位置。
     * 输入：对齐字段，如果需要。输出：对齐字段的偏移量。例如，给定时间文本 "1996.07.10 AD at 15:08:56 PDT"，
     * 如果给定的 fieldPosition 是 DateFormat.YEAR_FIELD，fieldPosition 的开始索引和结束索引将分别设置为 0 和 4。
     * 注意，如果同一个时间字段在模式中出现多次，fieldPosition 将设置为该时间字段的第一次出现。例如，使用模式
     * "h a z (zzzz)" 和对齐字段 DateFormat.TIMEZONE_FIELD 将 Date 格式化为时间字符串 "1 PM PDT (Pacific Daylight Time)"，
     * fieldPosition 的开始索引和结束索引将分别设置为 5 和 8，对应于时间区模式字符 'z' 的第一次出现。
     * @see java.text.Format
     */
    public final StringBuffer format(Object obj, StringBuffer toAppendTo,
                                     FieldPosition fieldPosition)
    {
        if (obj instanceof Date)
            return format( (Date)obj, toAppendTo, fieldPosition );
        else if (obj instanceof Number)
            return format( new Date(((Number)obj).longValue()),
                          toAppendTo, fieldPosition );
        else
            throw new IllegalArgumentException("Cannot format given Object as a Date");
    }

    /**
     * 将 Date 格式化为日期/时间字符串。
     * @param date 要格式化为日期/时间字符串的 Date。
     * @param toAppendTo 返回的日期/时间字符串的字符串缓冲区。
     * @param fieldPosition 跟踪返回字符串中字段的位置。
     * 输入：对齐字段，如果需要。输出：对齐字段的偏移量。例如，给定时间文本 "1996.07.10 AD at 15:08:56 PDT"，
     * 如果给定的 fieldPosition 是 DateFormat.YEAR_FIELD，fieldPosition 的开始索引和结束索引将分别设置为 0 和 4。
     * 注意，如果同一个时间字段在模式中出现多次，fieldPosition 将设置为该时间字段的第一次出现。例如，使用模式
     * "h a z (zzzz)" 和对齐字段 DateFormat.TIMEZONE_FIELD 将 Date 格式化为时间字符串 "1 PM PDT (Pacific Daylight Time)"，
     * fieldPosition 的开始索引和结束索引将分别设置为 5 和 8，对应于时间区模式字符 'z' 的第一次出现。
     * @return 作为 toAppendTo 传递的字符串缓冲区，附加了格式化的文本。
     */
    public abstract StringBuffer format(Date date, StringBuffer toAppendTo,
                                        FieldPosition fieldPosition);

    /**
     * 将 Date 格式化为日期/时间字符串。
     * @param date 要格式化为时间字符串的时间值。
     * @return 格式化的时间字符串。
     */
    public final String format(Date date)
    {
        return format(date, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }

    /**
     * 从给定字符串的开头解析文本以生成日期。
     * 该方法可能不会使用给定字符串的全部文本。
     * <p>
     * 有关日期解析的更多信息，请参见 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个 <code>String</code>，其开头应被解析。
     * @return 从字符串解析的 <code>Date</code>。
     * @exception ParseException 如果指定字符串的开头无法解析。
     */
    public Date parse(String source) throws ParseException
    {
        ParsePosition pos = new ParsePosition(0);
        Date result = parse(source, pos);
        if (pos.index == 0)
            throw new ParseException("Unparseable date: \"" + source + "\"" ,
                pos.errorIndex);
        return result;
    }


                /**
     * 根据给定的解析位置解析日期/时间字符串。 例如，时间文本 {@code "07/10/96 4:5 PM, PDT"} 将被解析成一个 {@code Date}
     * 等效于 {@code Date(837039900000L)}。
     *
     * <p> 默认情况下，解析是宽松的：如果输入不是此对象格式方法使用的格式，但仍然可以解析为日期，则解析成功。 客户端可以通过调用
     * {@link #setLenient(boolean) setLenient(false)} 来坚持严格遵守格式。
     *
     * <p>此解析操作使用 {@link #calendar} 生成一个 {@code Date}。因此，{@code calendar} 的日期时间字段和
     * {@code TimeZone} 值可能已被覆盖，具体取决于子类实现。任何通过调用
     * {@link #setTimeZone(java.util.TimeZone) setTimeZone} 之前设置的 {@code TimeZone} 值可能需要恢复以进行进一步操作。
     *
     * @param source  要解析的日期/时间字符串
     *
     * @param pos   输入时，开始解析的位置；输出时，解析终止的位置，或如果解析失败，则为起始位置。
     *
     * @return      一个 {@code Date}，如果输入无法解析则返回 {@code null}
     */
    public abstract Date parse(String source, ParsePosition pos);

    /**
     * 从字符串中解析文本以生成一个 <code>Date</code>。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为最后一个使用字符之后的索引（解析不一定使用到字符串的末尾），并返回解析的日期。
     * 更新后的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会改变，<code>pos</code> 的错误索引将设置为发生错误的字符索引，并返回 null。
     * <p>
     * 有关日期解析的更多信息，请参见 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个 <code>String</code>，其中一部分应该被解析。
     * @param pos 一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return 从字符串解析的 <code>Date</code>。如果发生错误，返回 null。
     * @exception NullPointerException 如果 <code>pos</code> 为 null。
     */
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    /**
     * 完整样式模式的常量。
     */
    public static final int FULL = 0;
    /**
     * 长样式模式的常量。
     */
    public static final int LONG = 1;
    /**
     * 中等样式模式的常量。
     */
    public static final int MEDIUM = 2;
    /**
     * 短样式模式的常量。
     */
    public static final int SHORT = 3;
    /**
     * 默认样式模式的常量。其值为 MEDIUM。
     */
    public static final int DEFAULT = MEDIUM;

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的默认格式样式的日期/时间格式器。
     * <p>这相当于调用
     * {@link #getTimeInstance(int, Locale) getTimeInstance(DEFAULT,
     *     Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 一个时间格式器。
     */
    public final static DateFormat getTimeInstance()
    {
        return get(DEFAULT, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的给定格式样式的日期/时间格式器。
     * <p>这相当于调用
     * {@link #getTimeInstance(int, Locale) getTimeInstance(style,
     *     Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @param style 给定的格式样式。例如，在美国语言环境中，SHORT 为 "h:mm a"。
     * @return 一个时间格式器。
     */
    public final static DateFormat getTimeInstance(int style)
    {
        return get(style, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取给定语言环境的给定格式样式的日期/时间格式器。
     * @param style 给定的格式样式。例如，在美国语言环境中，SHORT 为 "h:mm a"。
     * @param aLocale 给定的语言环境。
     * @return 一个时间格式器。
     */
    public final static DateFormat getTimeInstance(int style,
                                                 Locale aLocale)
    {
        return get(style, 0, 1, aLocale);
    }

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的默认格式样式的日期格式器。
     * <p>这相当于调用
     * {@link #getDateInstance(int, Locale) getDateInstance(DEFAULT,
     *     Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 一个日期格式器。
     */
    public final static DateFormat getDateInstance()
    {
        return get(0, DEFAULT, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的给定格式样式的日期格式器。
     * <p>这相当于调用
     * {@link #getDateInstance(int, Locale) getDateInstance(style,
     *     Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @param style 给定的格式样式。例如，在美国语言环境中，SHORT 为 "M/d/yy"。
     * @return 一个日期格式器。
     */
    public final static DateFormat getDateInstance(int style)
    {
        return get(0, style, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取给定语言环境的给定格式样式的日期格式器。
     * @param style 给定的格式样式。例如，在美国语言环境中，SHORT 为 "M/d/yy"。
     * @param aLocale 给定的语言环境。
     * @return 一个日期格式器。
     */
    public final static DateFormat getDateInstance(int style,
                                                 Locale aLocale)
    {
        return get(0, style, 2, aLocale);
    }

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的默认格式样式的日期/时间格式器。
     * <p>这相当于调用
     * {@link #getDateTimeInstance(int, int, Locale) getDateTimeInstance(DEFAULT,
     *     DEFAULT, Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 一个日期/时间格式器。
     */
    public final static DateFormat getDateTimeInstance()
    {
        return get(DEFAULT, DEFAULT, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的给定日期和时间格式样式的日期/时间格式器。
     * <p>这相当于调用
     * {@link #getDateTimeInstance(int, int, Locale) getDateTimeInstance(dateStyle,
     *     timeStyle, Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @param dateStyle 给定的日期格式样式。例如，在美国语言环境中，SHORT 为 "M/d/yy"。
     * @param timeStyle 给定的时间格式样式。例如，在美国语言环境中，SHORT 为 "h:mm a"。
     * @return 一个日期/时间格式器。
     */
    public final static DateFormat getDateTimeInstance(int dateStyle,
                                                       int timeStyle)
    {
        return get(timeStyle, dateStyle, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取给定语言环境的给定格式样式的日期/时间格式器。
     * @param dateStyle 给定的日期格式样式。
     * @param timeStyle 给定的时间格式样式。
     * @param aLocale 给定的语言环境。
     * @return 一个日期/时间格式器。
     */
    public final static DateFormat
        getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale)
    {
        return get(timeStyle, dateStyle, 3, aLocale);
    }

    /**
     * 获取一个默认的日期/时间格式器，该格式器使用 SHORT 样式表示日期和时间。
     *
     * @return 一个日期/时间格式器
     */
    public final static DateFormat getInstance() {
        return getDateTimeInstance(SHORT, SHORT);
    }

    /**
     * 返回一个数组，包含此类的 <code>get*Instance</code> 方法可以返回本地化实例的所有语言环境。
     * 返回的数组表示 Java 运行时支持的语言环境和已安装的
     * {@link java.text.spi.DateFormatProvider DateFormatProvider} 实现支持的语言环境的并集。
     * 它必须至少包含一个等于
     * {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 可以获取本地化 <code>DateFormat</code> 实例的语言环境数组。
     */
    public static Locale[] getAvailableLocales()
    {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(DateFormatProvider.class);
        return pool.getAvailableLocales();
    }

    /**
     * 设置此日期格式器使用的日历。最初，使用指定或默认语言环境的默认日历。
     *
     * <p>任何先前设置的 {@link java.util.TimeZone TimeZone} 和 {@linkplain
     * #isLenient() 宽容性} 值将被 {@code newCalendar} 的值覆盖。
     *
     * @param newCalendar 要由日期格式器使用的新的 {@code Calendar}。
     */
    public void setCalendar(Calendar newCalendar)
    {
        this.calendar = newCalendar;
    }

    /**
     * 获取与此日期/时间格式器关联的日历。
     *
     * @return 与此日期/时间格式器关联的日历。
     */
    public Calendar getCalendar()
    {
        return calendar;
    }

    /**
     * 设置数字格式器。
     * @param newNumberFormat 给定的新 NumberFormat。
     */
    public void setNumberFormat(NumberFormat newNumberFormat)
    {
        this.numberFormat = newNumberFormat;
    }

    /**
     * 获取此日期/时间格式器用于格式化和解析时间的数字格式器。
     * @return 此日期/时间格式器使用的数字格式器。
     */
    public NumberFormat getNumberFormat()
    {
        return numberFormat;
    }

    /**
     * 设置此 {@code DateFormat} 对象的日历的时间区。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().setTimeZone(zone)
     * }</pre></blockquote>
     *
     * <p>通过此方法设置的时间区将被 {@link #setCalendar(java.util.Calendar) setCalendar} 调用覆盖。
     *
     * <p>通过此方法设置的时间区可能因调用解析方法而被覆盖。
     *
     * @param zone 给定的新时间区。
     */
    public void setTimeZone(TimeZone zone)
    {
        calendar.setTimeZone(zone);
    }

    /**
     * 获取时间区。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().getTimeZone()
     * }</pre></blockquote>
     *
     * @return 与 DateFormat 的日历关联的时间区。
     */
    public TimeZone getTimeZone()
    {
        return calendar.getTimeZone();
    }

    /**
     * 指定日期/时间解析是否宽松。 使用宽松解析时，解析器可以使用启发式方法来解释与此对象格式不完全匹配的输入。
     * 使用严格解析时，输入必须与此对象的格式匹配。
     *
     * <p>此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().setLenient(lenient)
     * }</pre></blockquote>
     *
     * <p>此宽容值将被 {@link #setCalendar(java.util.Calendar) setCalendar()} 调用覆盖。
     *
     * @param lenient 当 {@code true} 时，解析是宽松的
     * @see java.util.Calendar#setLenient(boolean)
     */
    public void setLenient(boolean lenient)
    {
        calendar.setLenient(lenient);
    }

    /**
     * 告诉日期/时间解析是否宽松。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().isLenient()
     * }</pre></blockquote>
     *
     * @return 如果 {@link #calendar} 是宽容的，则返回 {@code true}；
     *         否则返回 {@code false}。
     * @see java.util.Calendar#isLenient()
     */
    public boolean isLenient()
    {
        return calendar.isLenient();
    }

    /**
     * 覆盖 hashCode
     */
    public int hashCode() {
        return numberFormat.hashCode();
        // 只有足够的字段以获得合理的分布
    }

    /**
     * 覆盖 equals
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormat other = (DateFormat) obj;
        return (// calendar.equivalentTo(other.calendar) // 此 API 尚不存在！
                calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek() &&
                calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek() &&
                calendar.isLenient() == other.calendar.isLenient() &&
                calendar.getTimeZone().equals(other.calendar.getTimeZone()) &&
                numberFormat.equals(other.numberFormat));
    }

    /**
     * 覆盖 Cloneable
     */
    public Object clone()
    {
        DateFormat other = (DateFormat) super.clone();
        other.calendar = (Calendar) calendar.clone();
        other.numberFormat = (NumberFormat) numberFormat.clone();
        return other;
    }

    /**
     * 使用给定的时间和/或日期样式在给定的语言环境中创建一个 DateFormat。
     * @param timeStyle 一个从 0 到 3 表示时间格式的值，如果标志为 2 则忽略
     * @param dateStyle 一个从 0 到 3 表示时间格式的值，如果标志为 1 则忽略
     * @param flags 1 表示时间格式，2 表示日期格式，3 表示日期/时间格式
     * @param loc 格式的语言环境
     */
    private static DateFormat get(int timeStyle, int dateStyle,
                                  int flags, Locale loc) {
        if ((flags & 1) != 0) {
            if (timeStyle < 0 || timeStyle > 3) {
                throw new IllegalArgumentException("非法时间样式 " + timeStyle);
            }
        } else {
            timeStyle = -1;
        }
        if ((flags & 2) != 0) {
            if (dateStyle < 0 || dateStyle > 3) {
                throw new IllegalArgumentException("非法日期样式 " + dateStyle);
            }
        } else {
            dateStyle = -1;
        }


    /**
     * 创建一个新的日期格式。
     */
    protected DateFormat() {}

    /**
     * 定义在从 <code>DateFormat.formatToCharacterIterator</code> 返回的
     * <code>AttributedCharacterIterator</code> 和 <code>FieldPosition</code> 中
     * 用作属性键和字段标识符的常量。
     * <p>
     * 该类还提供了两个方法，用于在这些常量和相应的 Calendar 常量之间进行映射。
     *
     * @since 1.4
     * @see java.util.Calendar
     */
    public static class Field extends Format.Field {

        // 声明与 1.4 FCS 兼容的序列化
        private static final long serialVersionUID = 7441350119349544720L;

        // 本类中所有实例的表，用于 readResolve
        private static final Map<String, Field> instanceMap = new HashMap<>(18);
        // 从 Calendar 常量（如 Calendar.ERA）到 Field 常量（如 Field.ERA）的映射
        private static final Field[] calendarToFieldMapping =
                                             new Field[Calendar.FIELD_COUNT];

        /** Calendar 字段。 */
        private int calendarField;

        /**
         * 返回与 <code>Calendar</code> 常量 <code>calendarField</code> 对应的
         * <code>Field</code> 常量。如果 <code>Calendar</code> 常量和 <code>Field</code>
         * 之间没有直接映射，则返回 null。
         *
         * @throws IllegalArgumentException 如果 <code>calendarField</code> 不是
         *         <code>Calendar</code> 字段常量的值。
         * @param calendarField Calendar 字段常量
         * @return 代表 calendarField 的 Field 实例。
         * @see java.util.Calendar
         */
        public static Field ofCalendarField(int calendarField) {
            if (calendarField < 0 || calendarField >=
                        calendarToFieldMapping.length) {
                throw new IllegalArgumentException("未知的 Calendar 常量 "
                                                   + calendarField);
            }
            return calendarToFieldMapping[calendarField];
        }

        /**
         * 创建一个 <code>Field</code>。
         *
         * @param name <code>Field</code> 的名称
         * @param calendarField 此 <code>Field</code> 对应的 <code>Calendar</code> 常量；
         *        可以使用任何值，即使是在合法 <code>Calendar</code> 值范围之外的值，
         *        但应使用 <code>-1</code> 表示不对应于合法 <code>Calendar</code> 值的值
         */
        protected Field(String name, int calendarField) {
            super(name);
            this.calendarField = calendarField;
            if (this.getClass() == DateFormat.Field.class) {
                instanceMap.put(name, this);
                if (calendarField >= 0) {
                    // assert(calendarField < Calendar.FIELD_COUNT);
                    calendarToFieldMapping[calendarField] = this;
                }
            }
        }

        /**
         * 返回与此属性关联的 <code>Calendar</code> 字段。例如，如果这表示 <code>Calendar</code>
         * 的小时字段，这将返回 <code>Calendar.HOUR</code>。如果没有对应的 <code>Calendar</code>
         * 常量，这将返回 -1。
         *
         * @return 此字段的 Calendar 常量
         * @see java.util.Calendar
         */
        public int getCalendarField() {
            return calendarField;
        }

        /**
         * 将反序列化的实例解析为预定义的常量。
         *
         * @throws InvalidObjectException 如果常量无法解析。
         * @return 解析后的 DateFormat.Field 常量
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != DateFormat.Field.class) {
                throw new InvalidObjectException("子类没有正确实现 readResolve");
            }

            Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("未知的属性名称");
            }
        }

        //
        // 常量
        //

        /**
         * 识别纪元字段的常量。
         */
        public final static Field ERA = new Field("era", Calendar.ERA);

        /**
         * 识别年份字段的常量。
         */
        public final static Field YEAR = new Field("year", Calendar.YEAR);

        /**
         * 识别月份字段的常量。
         */
        public final static Field MONTH = new Field("month", Calendar.MONTH);

        /**
         * 识别月份中的日期字段的常量。
         */
        public final static Field DAY_OF_MONTH = new
                            Field("day of month", Calendar.DAY_OF_MONTH);

        /**
         * 识别一天中的小时字段的常量，合法值为 1 到 24。
         */
        public final static Field HOUR_OF_DAY1 = new Field("hour of day 1",-1);

        /**
         * 识别一天中的小时字段的常量，合法值为 0 到 23。
         */
        public final static Field HOUR_OF_DAY0 = new
               Field("hour of day", Calendar.HOUR_OF_DAY);

        /**
         * 识别分钟字段的常量。
         */
        public final static Field MINUTE =new Field("minute", Calendar.MINUTE);

        /**
         * 识别秒字段的常量。
         */
        public final static Field SECOND =new Field("second", Calendar.SECOND);

        /**
         * 识别毫秒字段的常量。
         */
        public final static Field MILLISECOND = new
                Field("millisecond", Calendar.MILLISECOND);

        /**
         * 识别星期几字段的常量。
         */
        public final static Field DAY_OF_WEEK = new
                Field("day of week", Calendar.DAY_OF_WEEK);

        /**
         * 识别一年中的日期字段的常量。
         */
        public final static Field DAY_OF_YEAR = new
                Field("day of year", Calendar.DAY_OF_YEAR);

        /**
         * 识别月份中的星期几字段的常量。
         */
        public final static Field DAY_OF_WEEK_IN_MONTH =
                     new Field("day of week in month",
                                            Calendar.DAY_OF_WEEK_IN_MONTH);

        /**
         * 识别一年中的周数字段的常量。
         */
        public final static Field WEEK_OF_YEAR = new
              Field("week of year", Calendar.WEEK_OF_YEAR);

        /**
         * 识别一个月中的周数字段的常量。
         */
        public final static Field WEEK_OF_MONTH = new
            Field("week of month", Calendar.WEEK_OF_MONTH);

        /**
         * 识别一天中的时间指示符（例如 "a.m." 或 "p.m."）字段的常量。
         */
        public final static Field AM_PM = new
                            Field("am pm", Calendar.AM_PM);

        /**
         * 识别小时字段的常量，合法值为 1 到 12。
         */
        public final static Field HOUR1 = new Field("hour 1", -1);

        /**
         * 识别小时字段的常量，合法值为 0 到 11。
         */
        public final static Field HOUR0 = new
                            Field("hour", Calendar.HOUR);

        /**
         * 识别时区字段的常量。
         */
        public final static Field TIME_ZONE = new Field("time zone", -1);
    }
}
