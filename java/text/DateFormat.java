
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * {@code DateFormat} is an abstract class for date/time formatting subclasses which
 * formats and parses dates or time in a language-independent manner.
 * The date/time formatting subclass, such as {@link SimpleDateFormat}, allows for
 * formatting (i.e., date &rarr; text), parsing (text &rarr; date), and
 * normalization.  The date is represented as a <code>Date</code> object or
 * as the milliseconds since January 1, 1970, 00:00:00 GMT.
 *
 * <p>{@code DateFormat} provides many class methods for obtaining default date/time
 * formatters based on the default or a given locale and a number of formatting
 * styles. The formatting styles include {@link #FULL}, {@link #LONG}, {@link #MEDIUM}, and {@link #SHORT}. More
 * detail and examples of using these styles are provided in the method
 * descriptions.
 *
 * <p>{@code DateFormat} helps you to format and parse dates for any locale.
 * Your code can be completely independent of the locale conventions for
 * months, days of the week, or even the calendar format: lunar vs. solar.
 *
 * <p>To format a date for the current Locale, use one of the
 * static factory methods:
 * <blockquote>
 * <pre>{@code
 * myString = DateFormat.getDateInstance().format(myDate);
 * }</pre>
 * </blockquote>
 * <p>If you are formatting multiple dates, it is
 * more efficient to get the format and use it multiple times so that
 * the system doesn't have to fetch the information about the local
 * language and country conventions multiple times.
 * <blockquote>
 * <pre>{@code
 * DateFormat df = DateFormat.getDateInstance();
 * for (int i = 0; i < myDate.length; ++i) {
 *     output.println(df.format(myDate[i]) + "; ");
 * }
 * }</pre>
 * </blockquote>
 * <p>To format a date for a different Locale, specify it in the
 * call to {@link #getDateInstance(int, Locale) getDateInstance()}.
 * <blockquote>
 * <pre>{@code
 * DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
 * }</pre>
 * </blockquote>
 * <p>You can use a DateFormat to parse also.
 * <blockquote>
 * <pre>{@code
 * myDate = df.parse(myString);
 * }</pre>
 * </blockquote>
 * <p>Use {@code getDateInstance} to get the normal date format for that country.
 * There are other static factory methods available.
 * Use {@code getTimeInstance} to get the time format for that country.
 * Use {@code getDateTimeInstance} to get a date and time format. You can pass in
 * different options to these factory methods to control the length of the
 * result; from {@link #SHORT} to {@link #MEDIUM} to {@link #LONG} to {@link #FULL}. The exact result depends
 * on the locale, but generally:
 * <ul><li>{@link #SHORT} is completely numeric, such as {@code 12.13.52} or {@code 3:30pm}
 * <li>{@link #MEDIUM} is longer, such as {@code Jan 12, 1952}
 * <li>{@link #LONG} is longer, such as {@code January 12, 1952} or {@code 3:30:32pm}
 * <li>{@link #FULL} is pretty completely specified, such as
 * {@code Tuesday, April 12, 1952 AD or 3:30:42pm PST}.
 * </ul>
 *
 * <p>You can also set the time zone on the format if you wish.
 * If you want even more control over the format or parsing,
 * (or want to give your users more control),
 * you can try casting the {@code DateFormat} you get from the factory methods
 * to a {@link SimpleDateFormat}. This will work for the majority
 * of countries; just remember to put it in a {@code try} block in case you
 * encounter an unusual one.
 *
 * <p>You can also use forms of the parse and format methods with
 * {@link ParsePosition} and {@link FieldPosition} to
 * allow you to
 * <ul><li>progressively parse through pieces of a string.
 * <li>align any particular field, or find out where it is for selection
 * on the screen.
 * </ul>
 *
 * <h3><a name="synchronization">Synchronization</a></h3>
 *
 * <p>
 * Date formats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
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
     * The {@link Calendar} instance used for calculating the date-time fields
     * and the instant of time. This field is used for both formatting and
     * parsing.
     *
     * <p>Subclasses should initialize this field to a {@link Calendar}
     * appropriate for the {@link Locale} associated with this
     * <code>DateFormat</code>.
     * @serial
     */
    protected Calendar calendar;

    /**
     * The number formatter that <code>DateFormat</code> uses to format numbers
     * in dates and times.  Subclasses should initialize this to a number format
     * appropriate for the locale associated with this <code>DateFormat</code>.
     * @serial
     */
    protected NumberFormat numberFormat;

                /**
     * 用于日期/时间格式化的ERA字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int ERA_FIELD = 0;
    /**
     * 用于日期/时间格式化的YEAR字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int YEAR_FIELD = 1;
    /**
     * 用于日期/时间格式化的MONTH字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int MONTH_FIELD = 2;
    /**
     * 用于日期/时间格式化的DATE字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int DATE_FIELD = 3;
    /**
     * 用于日期/时间格式化的基于1的HOUR_OF_DAY字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     * HOUR_OF_DAY1_FIELD 用于基于1的24小时时钟。
     * 例如，23:59 + 01:00 结果为 24:59。
     */
    public final static int HOUR_OF_DAY1_FIELD = 4;
    /**
     * 用于日期/时间格式化的基于0的HOUR_OF_DAY字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     * HOUR_OF_DAY0_FIELD 用于基于0的24小时时钟。
     * 例如，23:59 + 01:00 结果为 00:59。
     */
    public final static int HOUR_OF_DAY0_FIELD = 5;
    /**
     * 用于日期/时间格式化的MINUTE字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int MINUTE_FIELD = 6;
    /**
     * 用于日期/时间格式化的SECOND字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int SECOND_FIELD = 7;
    /**
     * 用于日期/时间格式化的MILLISECOND字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int MILLISECOND_FIELD = 8;
    /**
     * 用于日期/时间格式化的DAY_OF_WEEK字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int DAY_OF_WEEK_FIELD = 9;
    /**
     * 用于日期/时间格式化的DAY_OF_YEAR字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int DAY_OF_YEAR_FIELD = 10;
    /**
     * 用于日期/时间格式化的DAY_OF_WEEK_IN_MONTH字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
    /**
     * 用于日期/时间格式化的WEEK_OF_YEAR字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int WEEK_OF_YEAR_FIELD = 12;
    /**
     * 用于日期/时间格式化的WEEK_OF_MONTH字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int WEEK_OF_MONTH_FIELD = 13;
    /**
     * 用于日期/时间格式化的AM_PM字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int AM_PM_FIELD = 14;
    /**
     * 用于日期/时间格式化的基于1的HOUR字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     * HOUR1_FIELD 用于基于1的12小时时钟。
     * 例如，11:30 PM + 1小时 结果为 12:30 AM。
     */
    public final static int HOUR1_FIELD = 15;
    /**
     * 用于日期/时间格式化的基于0的HOUR字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     * HOUR0_FIELD 用于基于0的12小时时钟。
     * 例如，11:30 PM + 1小时 结果为 00:30 AM。
     */
    public final static int HOUR0_FIELD = 16;
    /**
     * 用于日期/时间格式化的TIMEZONE字段对齐的有用常量。
     * 在日期/时间格式化的FieldPosition中使用。
     */
    public final static int TIMEZONE_FIELD = 17;

    // 声明与1.1 FCS的序列化兼容性
    private static final long serialVersionUID = 7218322306649953788L;

    /**
     * 覆盖Format。
     * 将时间对象格式化为时间字符串。时间对象的例子包括以毫秒表示的时间值和Date对象。
     * @param obj 必须是Number或Date。
     * @param toAppendTo 用于返回时间字符串的字符串缓冲区。
     * @return 作为toAppendTo传入的字符串缓冲区，附加了格式化的文本。
     * @param fieldPosition 跟踪返回字符串中字段的位置。
     * 输入：如果需要，对齐字段。输出：对齐字段的偏移量。例如，给定时间文本 "1996.07.10 AD at 15:08:56 PDT"，
     * 如果给定的fieldPosition是DateFormat.YEAR_FIELD，fieldPosition的开始索引和结束索引将分别设置为0和4。
     * 注意，如果同一个时间字段在模式中出现多次，fieldPosition将设置为该时间字段的第一次出现。例如，使用模式
     * "h a z (zzzz)" 和对齐字段 DateFormat.TIMEZONE_FIELD 格式化一个Date到时间字符串 "1 PM PDT (Pacific Daylight Time)"，
     * fieldPosition的开始索引和结束索引将分别设置为5和8，对应于时区模式字符 'z' 的第一次出现。
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
     * 将Date格式化为日期/时间字符串。
     * @param date 要格式化为日期/时间字符串的Date。
     * @param toAppendTo 用于返回日期/时间字符串的字符串缓冲区。
     * @param fieldPosition 跟踪返回字符串中字段的位置。
     * 输入：如果需要，对齐字段。输出：对齐字段的偏移量。例如，给定时间文本 "1996.07.10 AD at 15:08:56 PDT"，
     * 如果给定的fieldPosition是DateFormat.YEAR_FIELD，fieldPosition的开始索引和结束索引将分别设置为0和4。
     * 注意，如果同一个时间字段在模式中出现多次，fieldPosition将设置为该时间字段的第一次出现。例如，使用模式
     * "h a z (zzzz)" 和对齐字段 DateFormat.TIMEZONE_FIELD 格式化一个Date到时间字符串 "1 PM PDT (Pacific Daylight Time)"，
     * fieldPosition的开始索引和结束索引将分别设置为5和8，对应于时区模式字符 'z' 的第一次出现。
     * @return 作为toAppendTo传入的字符串缓冲区，附加了格式化的文本。
     */
    public abstract StringBuffer format(Date date, StringBuffer toAppendTo,
                                        FieldPosition fieldPosition);


                /**
     * 将日期格式化为日期/时间字符串。
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
     * @return 从字符串解析出的 <code>Date</code>。
     * @exception ParseException 如果指定字符串的开头无法解析。
     */
    public Date parse(String source) throws ParseException
    {
        ParsePosition pos = new ParsePosition(0);
        Date result = parse(source, pos);
        if (pos.index == 0)
            throw new ParseException("无法解析日期: \"" + source + "\"" ,
                pos.errorIndex);
        return result;
    }

    /**
     * 根据给定的解析位置解析日期/时间字符串。例如，时间文本 {@code "07/10/96 4:5 PM, PDT"} 将被解析为一个与 {@code Date(837039900000L)} 等效的 {@code Date}。
     *
     * <p>默认情况下，解析是宽松的：如果输入不是此对象格式方法使用的格式，但仍然可以解析为日期，则解析成功。客户端可以通过调用 {@link #setLenient(boolean) setLenient(false)} 来要求严格遵守格式。
     *
     * <p>此解析操作使用 {@link #calendar} 生成一个 {@code Date}。因此，根据子类实现，{@code calendar} 的日期时间字段和 {@code TimeZone} 值可能已被覆盖。任何通过调用 {@link #setTimeZone(java.util.TimeZone) setTimeZone} 之前设置的 {@code TimeZone} 值可能需要恢复以进行进一步操作。
     *
     * @param source 要解析的日期/时间字符串。
     *
     * @param pos 输入时，开始解析的位置；输出时，解析终止的位置，或如果解析失败，则为开始位置。
     *
     * @return 一个 {@code Date}，如果输入无法解析，则返回 {@code null}。
     */
    public abstract Date parse(String source, ParsePosition pos);

    /**
     * 从字符串中解析文本以生成 <code>Date</code>。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为最后一个使用字符之后的索引（解析不一定使用到字符串的末尾），并返回解析的日期。更新的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会改变，<code>pos</code> 的错误索引将设置为发生错误的字符索引，并返回 null。
     * <p>
     * 有关日期解析的更多信息，请参见 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个 <code>String</code>，其部分应被解析。
     * @param pos 一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return 从字符串解析出的 <code>Date</code>。发生错误时，返回 null。
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
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的默认格式样式的日期格式器。
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
     * 获取默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境的给定格式样式的日期格式器。
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
     * 获取给定语言环境的给定格式样式的日期格式器。
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
 * 获取默认格式化样式的默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的日期格式化器。
 * <p>这相当于调用
 * {@link #getDateInstance(int, Locale) getDateInstance(DEFAULT,
 *     Locale.getDefault(Locale.Category.FORMAT))}。
 * @see java.util.Locale#getDefault(java.util.Locale.Category)
 * @see java.util.Locale.Category#FORMAT
 * @return 一个日期格式化器。
 */
public final static DateFormat getDateInstance()
{
    return get(0, DEFAULT, 2, Locale.getDefault(Locale.Category.FORMAT));
}

/**
 * 获取给定格式化样式的默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的日期格式化器。
 * <p>这相当于调用
 * {@link #getDateInstance(int, Locale) getDateInstance(style,
 *     Locale.getDefault(Locale.Category.FORMAT))}。
 * @see java.util.Locale#getDefault(java.util.Locale.Category)
 * @see java.util.Locale.Category#FORMAT
 * @param style 给定的格式化样式。例如，在美国本地，SHORT 为 "M/d/yy"。
 * @return 一个日期格式化器。
 */
public final static DateFormat getDateInstance(int style)
{
    return get(0, style, 2, Locale.getDefault(Locale.Category.FORMAT));
}

/**
 * 获取给定本地的给定格式化样式的日期格式化器。
 * @param style 给定的格式化样式。例如，在美国本地，SHORT 为 "M/d/yy"。
 * @param aLocale 给定的本地。
 * @return 一个日期格式化器。
 */
public final static DateFormat getDateInstance(int style,
                                                 Locale aLocale)
{
    return get(0, style, 2, aLocale);
}

/**
 * 获取默认格式化样式的默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的日期/时间格式化器。
 * <p>这相当于调用
 * {@link #getDateTimeInstance(int, int, Locale) getDateTimeInstance(DEFAULT,
 *     DEFAULT, Locale.getDefault(Locale.Category.FORMAT))}。
 * @see java.util.Locale#getDefault(java.util.Locale.Category)
 * @see java.util.Locale.Category#FORMAT
 * @return 一个日期/时间格式化器。
 */
public final static DateFormat getDateTimeInstance()
{
    return get(DEFAULT, DEFAULT, 3, Locale.getDefault(Locale.Category.FORMAT));
}

/**
 * 获取给定日期和时间格式化样式的默认 {@link java.util.Locale.Category#FORMAT FORMAT} 本地的日期/时间格式化器。
 * <p>这相当于调用
 * {@link #getDateTimeInstance(int, int, Locale) getDateTimeInstance(dateStyle,
 *     timeStyle, Locale.getDefault(Locale.Category.FORMAT))}。
 * @see java.util.Locale#getDefault(java.util.Locale.Category)
 * @see java.util.Locale.Category#FORMAT
 * @param dateStyle 给定的日期格式化样式。例如，在美国本地，SHORT 为 "M/d/yy"。
 * @param timeStyle 给定的时间格式化样式。例如，在美国本地，SHORT 为 "h:mm a"。
 * @return 一个日期/时间格式化器。
 */
public final static DateFormat getDateTimeInstance(int dateStyle,
                                                       int timeStyle)
{
    return get(timeStyle, dateStyle, 3, Locale.getDefault(Locale.Category.FORMAT));
}

/**
 * 获取给定格式化样式的给定本地的日期/时间格式化器。
 * @param dateStyle 给定的日期格式化样式。
 * @param timeStyle 给定的时间格式化样式。
 * @param aLocale 给定的本地。
 * @return 一个日期/时间格式化器。
 */
public final static DateFormat
        getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale)
{
    return get(timeStyle, dateStyle, 3, aLocale);
}

/**
 * 获取使用 SHORT 样式格式化日期和时间的默认日期/时间格式化器。
 *
 * @return 一个日期/时间格式化器
 */
public final static DateFormat getInstance() {
    return getDateTimeInstance(SHORT, SHORT);
}

/**
 * 返回一个数组，其中包含此类的 <code>get*Instance</code> 方法可以返回本地化实例的所有本地。
 * 返回的数组表示 Java 运行时和已安装的
 * {@link java.text.spi.DateFormatProvider DateFormatProvider} 实现支持的本地的并集。
 * 它必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
 *
 * @return 可以获取本地化 <code>DateFormat</code> 实例的本地数组。
 */
public static Locale[] getAvailableLocales()
{
    LocaleServiceProviderPool pool =
        LocaleServiceProviderPool.getPool(DateFormatProvider.class);
    return pool.getAvailableLocales();
}

/**
 * 设置此日期格式化器使用的日历。最初，使用指定或默认本地的默认日历。
 *
 * <p>任何之前设置的 {@link java.util.TimeZone TimeZone} 和 {@linkplain
 * #isLenient() 宽容性} 值将被 {@code newCalendar} 的值覆盖。
 *
 * @param newCalendar 要由日期格式化器使用的新的 {@code Calendar}。
 */
public void setCalendar(Calendar newCalendar)
{
    this.calendar = newCalendar;
}

/**
 * 获取与此日期/时间格式化器关联的日历。
 *
 * @return 与此日期/时间格式化器关联的日历。
 */
public Calendar getCalendar()
{
    return calendar;
}

/**
 * 允许设置数字格式化器。
 * @param newNumberFormat 给定的新 NumberFormat。
 */
public void setNumberFormat(NumberFormat newNumberFormat)
{
    this.numberFormat = newNumberFormat;
}

/**
 * 获取此日期/时间格式化器用于格式化和解析时间的数字格式化器。
 * @return 此日期/时间格式化器使用的数字格式化器。
 */
public NumberFormat getNumberFormat()
{
    return numberFormat;
}


    /**
     * 为这个 {@code DateFormat} 对象的日历设置时区。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().setTimeZone(zone)
     * }</pre></blockquote>
     *
     * <p>通过此方法设置的 {@code TimeZone} 可能会被
     * {@link #setCalendar(java.util.Calendar) setCalendar} 调用覆盖。
     *
     * <p>通过此方法设置的 {@code TimeZone} 可能会被
     * 解析方法调用的结果覆盖。
     *
     * @param zone 给定的新时区。
     */
    public void setTimeZone(TimeZone zone)
    {
        calendar.setTimeZone(zone);
    }

    /**
     * 获取时区。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().getTimeZone()
     * }</pre></blockquote>
     *
     * @return 与 DateFormat 的日历关联的时区。
     */
    public TimeZone getTimeZone()
    {
        return calendar.getTimeZone();
    }

    /**
     * 指定日期/时间解析是否应宽松。使用宽松解析时，解析器可能会使用启发式方法来解释与该对象的格式不完全匹配的输入。使用严格解析时，输入必须与该对象的格式匹配。
     *
     * <p>此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().setLenient(lenient)
     * }</pre></blockquote>
     *
     * <p>此宽松值可能被 {@link
     * #setCalendar(java.util.Calendar) setCalendar()} 调用覆盖。
     *
     * @param lenient 当 {@code true} 时，解析是宽松的
     * @see java.util.Calendar#setLenient(boolean)
     */
    public void setLenient(boolean lenient)
    {
        calendar.setLenient(lenient);
    }

    /**
     * 告知日期/时间解析是否应宽松。
     * 此方法等效于以下调用。
     * <blockquote><pre>{@code
     * getCalendar().isLenient()
     * }</pre></blockquote>
     *
     * @return 如果 {@link #calendar} 是宽松的，则返回 {@code true}；
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
     * 使用给定的时区和/或日期样式在给定的区域设置中创建一个 DateFormat。
     * @param timeStyle 一个从 0 到 3 的值，表示时间格式，
     * 忽略如果标志是 2
     * @param dateStyle 一个从 0 到 3 的值，表示时间格式，
     * 忽略如果标志是 1
     * @param flags 1 表示时间格式，2 表示日期格式，
     * 或 3 表示日期/时间格式
     * @param loc 用于格式化的区域设置
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

        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DateFormatProvider.class, loc);
        DateFormat dateFormat = get(adapter, timeStyle, dateStyle, loc);
        if (dateFormat == null) {
            dateFormat = get(LocaleProviderAdapter.forJRE(), timeStyle, dateStyle, loc);
        }
        return dateFormat;
    }

    private static DateFormat get(LocaleProviderAdapter adapter, int timeStyle, int dateStyle, Locale loc) {
        DateFormatProvider provider = adapter.getDateFormatProvider();
        DateFormat dateFormat;
        if (timeStyle == -1) {
            dateFormat = provider.getDateInstance(dateStyle, loc);
        } else {
            if (dateStyle == -1) {
                dateFormat = provider.getTimeInstance(timeStyle, loc);
            } else {
                dateFormat = provider.getDateTimeInstance(dateStyle, timeStyle, loc);
            }
        }
        return dateFormat;
    }

    /**
     * 创建一个新的日期格式。
     */
    protected DateFormat() {}

    /**
     * 定义在从 <code>DateFormat.formatToCharacterIterator</code> 返回的
     * <code>AttributedCharacterIterator</code> 中用作属性键的常量，
     * 以及在 <code>FieldPosition</code> 中用作字段标识符的常量。
     * <p>
     * 该类还提供了两种方法来映射
     * 其常量与相应的 Calendar 常量。
     *
     * @since 1.4
     * @see java.util.Calendar
     */
    public static class Field extends Format.Field {

        // 声明与 1.4 FCS 兼容的序列化
        private static final long serialVersionUID = 7441350119349544720L;


                    // 本类中所有实例的表，用于 readResolve
        private static final Map<String, Field> instanceMap = new HashMap<>(18);
        // 从 Calendar 常量（如 Calendar.ERA）到 Field
        // 常量（如 Field.ERA）的映射。
        private static final Field[] calendarToFieldMapping =
                                             new Field[Calendar.FIELD_COUNT];

        /** Calendar 字段。 */
        private int calendarField;

        /**
         * 返回与 <code>Calendar</code> 常量 <code>calendarField</code> 对应的
         * <code>Field</code> 常量。如果 <code>Calendar</code>
         * 常量和 <code>Field</code> 之间没有直接映射，则返回 null。
         *
         * @throws IllegalArgumentException 如果 <code>calendarField</code> 不是
         *         <code>Calendar</code> 字段常量的值。
         * @param calendarField Calendar 字段常量
         * @return 表示 calendarField 的 Field 实例。
         * @see java.util.Calendar
         */
        public static Field ofCalendarField(int calendarField) {
            if (calendarField < 0 || calendarField >=
                        calendarToFieldMapping.length) {
                throw new IllegalArgumentException("Unknown Calendar constant "
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
         *        但对于不对应于合法 <code>Calendar</code> 值的情况，应使用 <code>-1</code>
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
         * 返回与此属性关联的 <code>Calendar</code> 字段。例如，如果这表示
         * <code>Calendar</code> 的小时字段，这将返回
         * <code>Calendar.HOUR</code>。如果没有对应的
         * <code>Calendar</code> 常量，这将返回 -1。
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
                throw new InvalidObjectException("子类未正确实现 readResolve");
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
         * 标识纪元字段的常量。
         */
        public final static Field ERA = new Field("era", Calendar.ERA);

        /**
         * 标识年份字段的常量。
         */
        public final static Field YEAR = new Field("year", Calendar.YEAR);

        /**
         * 标识月份字段的常量。
         */
        public final static Field MONTH = new Field("month", Calendar.MONTH);

        /**
         * 标识月份中的日期字段的常量。
         */
        public final static Field DAY_OF_MONTH = new
                            Field("day of month", Calendar.DAY_OF_MONTH);

        /**
         * 标识一天中的小时字段的常量，其中合法值为 1 到 24。
         */
        public final static Field HOUR_OF_DAY1 = new Field("hour of day 1",-1);

        /**
         * 标识一天中的小时字段的常量，其中合法值为 0 到 23。
         */
        public final static Field HOUR_OF_DAY0 = new
               Field("hour of day", Calendar.HOUR_OF_DAY);

        /**
         * 标识分钟字段的常量。
         */
        public final static Field MINUTE =new Field("minute", Calendar.MINUTE);

        /**
         * 标识秒字段的常量。
         */
        public final static Field SECOND =new Field("second", Calendar.SECOND);

        /**
         * 标识毫秒字段的常量。
         */
        public final static Field MILLISECOND = new
                Field("millisecond", Calendar.MILLISECOND);

        /**
         * 标识星期几字段的常量。
         */
        public final static Field DAY_OF_WEEK = new
                Field("day of week", Calendar.DAY_OF_WEEK);

        /**
         * 标识一年中的日期字段的常量。
         */
        public final static Field DAY_OF_YEAR = new
                Field("day of year", Calendar.DAY_OF_YEAR);

        /**
         * 标识月份中的星期几字段的常量。
         */
        public final static Field DAY_OF_WEEK_IN_MONTH =
                     new Field("day of week in month",
                                            Calendar.DAY_OF_WEEK_IN_MONTH);

        /**
         * 标识一年中的周数字段的常量。
         */
        public final static Field WEEK_OF_YEAR = new
              Field("week of year", Calendar.WEEK_OF_YEAR);

                    /**
         * 用于标识月份中周的字段的常量。
         */
        public final static Field WEEK_OF_MONTH = new
            Field("week of month", Calendar.WEEK_OF_MONTH);

        /**
         * 用于标识一天中的时间指示符
         * （例如 "a.m." 或 "p.m."）字段的常量。
         */
        public final static Field AM_PM = new
                            Field("am pm", Calendar.AM_PM);

        /**
         * 用于标识小时字段的常量，其中合法值为
         * 1 到 12。
         */
        public final static Field HOUR1 = new Field("hour 1", -1);

        /**
         * 用于标识小时字段的常量，其中合法值为
         * 0 到 11。
         */
        public final static Field HOUR0 = new
                            Field("hour", Calendar.HOUR);

        /**
         * 用于标识时区字段的常量。
         */
        public final static Field TIME_ZONE = new Field("time zone", -1);
    }
}
