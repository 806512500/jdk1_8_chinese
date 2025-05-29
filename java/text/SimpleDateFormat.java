
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
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
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
import static java.text.DateFormatSymbols.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.ZoneInfoFile;
import sun.util.locale.provider.LocaleProviderAdapter;

/**
 * <code>SimpleDateFormat</code> 是一个用于以区域敏感方式格式化和解析日期的具体类。它允许进行格式化（日期 → 文本）、解析（文本 → 日期）和规范化。
 *
 * <p>
 * <code>SimpleDateFormat</code> 允许您从选择任何用户定义的日期时间格式模式开始。但是，建议您使用 <code>DateFormat</code> 中的 <code>getTimeInstance</code>、<code>getDateInstance</code> 或 <code>getDateTimeInstance</code> 方法创建日期时间格式器。这些类方法可以返回一个使用默认格式模式初始化的日期/时间格式器。您可以根据需要使用 <code>applyPattern</code> 方法修改格式模式。有关使用这些方法的更多信息，请参见
 * {@link DateFormat}。
 *
 * <h3>日期和时间模式</h3>
 * <p>
 * 日期和时间格式由 <em>日期和时间模式</em> 字符串指定。
 * 在日期和时间模式字符串中，从 <code>'A'</code> 到 <code>'Z'</code> 和从 <code>'a'</code> 到 <code>'z'</code> 的未加引号的字母被解释为表示日期或时间字符串的组件的模式字母。文本可以使用单引号 (<code>'</code>) 来避免解释。 <code>"''"</code> 表示单引号。所有其他字符都不会被解释；它们在格式化时直接复制到输出字符串中，或在解析时与输入字符串进行匹配。
 * <p>
 * 定义了以下模式字母（从 <code>'A'</code> 到 <code>'Z'</code> 和从 <code>'a'</code> 到 <code>'z'</code> 的所有其他字符都是保留的）：
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows pattern letters, date/time component, presentation, and examples.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *         <th align=left>字母
 *         <th align=left>日期或时间组件
 *         <th align=left>表示形式
 *         <th align=left>示例
 *     <tr>
 *         <td><code>G</code>
 *         <td>纪元标识符
 *         <td><a href="#text">文本</a>
 *         <td><code>AD</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>y</code>
 *         <td>年份
 *         <td><a href="#year">年份</a>
 *         <td><code>1996</code>; <code>96</code>
 *     <tr>
 *         <td><code>Y</code>
 *         <td>周年的年份
 *         <td><a href="#year">年份</a>
 *         <td><code>2009</code>; <code>09</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>M</code>
 *         <td>年中的月份（上下文敏感）
 *         <td><a href="#month">月份</a>
 *         <td><code>July</code>; <code>Jul</code>; <code>07</code>
 *     <tr>
 *         <td><code>L</code>
 *         <td>年中的月份（独立形式）
 *         <td><a href="#month">月份</a>
 *         <td><code>July</code>; <code>Jul</code>; <code>07</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>w</code>
 *         <td>年中的周数
 *         <td><a href="#number">数字</a>
 *         <td><code>27</code>
 *     <tr>
 *         <td><code>W</code>
 *         <td>月中的周数
 *         <td><a href="#number">数字</a>
 *         <td><code>2</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>D</code>
 *         <td>年中的天数
 *         <td><a href="#number">数字</a>
 *         <td><code>189</code>
 *     <tr>
 *         <td><code>d</code>
 *         <td>月中的天数
 *         <td><a href="#number">数字</a>
 *         <td><code>10</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>F</code>
 *         <td>月中的星期几
 *         <td><a href="#number">数字</a>
 *         <td><code>2</code>
 *     <tr>
 *         <td><code>E</code>
 *         <td>星期几的名称
 *         <td><a href="#text">文本</a>
 *         <td><code>Tuesday</code>; <code>Tue</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>u</code>
 *         <td>星期几的数字（1 = 星期一，...，7 = 星期日）
 *         <td><a href="#number">数字</a>
 *         <td><code>1</code>
 *     <tr>
 *         <td><code>a</code>
 *         <td>上午/下午标记
 *         <td><a href="#text">文本</a>
 *         <td><code>PM</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>H</code>
 *         <td>一天中的小时（0-23）
 *         <td><a href="#number">数字</a>
 *         <td><code>0</code>
 *     <tr>
 *         <td><code>k</code>
 *         <td>一天中的小时（1-24）
 *         <td><a href="#number">数字</a>
 *         <td><code>24</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>K</code>
 *         <td>上午/下午中的小时（0-11）
 *         <td><a href="#number">数字</a>
 *         <td><code>0</code>
 *     <tr>
 *         <td><code>h</code>
 *         <td>上午/下午中的小时（1-12）
 *         <td><a href="#number">数字</a>
 *         <td><code>12</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>m</code>
 *         <td>小时中的分钟
 *         <td><a href="#number">数字</a>
 *         <td><code>30</code>
 *     <tr>
 *         <td><code>s</code>
 *         <td>分钟中的秒
 *         <td><a href="#number">数字</a>
 *         <td><code>55</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>S</code>
 *         <td>毫秒
 *         <td><a href="#number">数字</a>
 *         <td><code>978</code>
 *     <tr>
 *         <td><code>z</code>
 *         <td>时区
 *         <td><a href="#timezone">一般时区</a>
 *         <td><code>Pacific Standard Time</code>; <code>PST</code>; <code>GMT-08:00</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>Z</code>
 *         <td>时区
 *         <td><a href="#rfc822timezone">RFC 822 时区</a>
 *         <td><code>-0800</code>
 *     <tr>
 *         <td><code>X</code>
 *         <td>时区
 *         <td><a href="#iso8601timezone">ISO 8601 时区</a>
 *         <td><code>-08</code>; <code>-0800</code>;  <code>-08:00</code>
 * </table>
 * </blockquote>
 * 模式字母通常会重复，因为它们的数量决定了确切的表示形式：
 * <ul>
 * <li><strong><a name="text">文本：</a></strong>
 *     对于格式化，如果模式字母的数量为 4 个或更多，则使用完整形式；否则，如果可用，则使用短形式或缩写形式。
 *     对于解析，无论模式字母的数量如何，两种形式都可接受。<br><br></li>
 * <li><strong><a name="number">数字：</a></strong>
 *     对于格式化，模式字母的数量是最小的数字数量，较短的数字会用零填充到这个数量。
 *     对于解析，除非需要将两个相邻的字段分开，否则模式字母的数量会被忽略。<br><br></li>
 * <li><strong><a name="year">年份：</a></strong>
 *     如果格式器的 {@link #getCalendar() Calendar} 是格里高利历，则应用以下规则。<br>
 *     <ul>
 *     <li>对于格式化，如果模式字母的数量为 2，则年份被截断为 2 位数字；否则，它被解释为一个 <a href="#number">数字</a>。
 *     <li>对于解析，如果模式字母的数量大于 2，则年份按字面解释，无论数字的位数如何。因此，使用模式 "MM/dd/yyyy"，"01/11/12" 解析为 12 年 1 月 11 日。
 *     <li>对于使用缩写年份模式 ("y" 或 "yy") 的解析，<code>SimpleDateFormat</code> 必须将缩写年份解释为相对于某个世纪的年份。它通过将日期调整到 <code>SimpleDateFormat</code> 实例创建时间前 80 年和后 20 年之间来实现这一点。例如，使用模式 "MM/dd/yy" 和一个在 1997 年 1 月 1 日创建的 <code>SimpleDateFormat</code> 实例，字符串 "01/11/12" 将被解释为 2012 年 1 月 11 日，而字符串 "05/04/64" 将被解释为 1964 年 5 月 4 日。
 *         在解析期间，只有由 {@link Character#isDigit(char)} 定义的恰好两位数字的字符串才会被解析为默认世纪。任何其他数字字符串，如一位数字、三位或更多位数字，或不是全部数字的两位字符串（例如 "-1"），都会被字面解释。因此，"01/02/3" 或 "01/02/003" 使用相同的模式解析为 3 年 1 月 2 日。同样，"01/02/-3" 解析为 4 年 1 月 2 日。
 *     </ul>
 *     否则，应用特定于日历系统的格式。<br>
 *     <br>
 *     如果指定了周年的年份 {@code 'Y'}，并且 {@linkplain #getCalendar() 日历} 不支持任何 <a href="../util/GregorianCalendar.html#week_year">周年</a>，则使用日历年份 ({@code 'y'}) 代替。可以通过调用 {@link DateFormat#getCalendar() getCalendar()}.{@link java.util.Calendar#isWeekDateSupported() isWeekDateSupported()} 来测试对周年年的支持。<br><br></li>
 * <li><strong><a name="month">月份：</a></strong>
 *     如果模式字母的数量为 3 个或更多，则月份被解释为 <a href="#text">文本</a>；否则，它被解释为一个 <a href="#number">数字</a>。<br>
 *     <ul>
 *     <li>字母 <em>M</em> 生成上下文敏感的月份名称，例如名称的嵌入形式。如果使用构造函数 {@link #SimpleDateFormat(String, DateFormatSymbols)} 或方法 {@link #setDateFormatSymbols(DateFormatSymbols)} 明确设置了 {@code DateFormatSymbols}，则使用 {@code DateFormatSymbols} 中给出的月份名称。</li>
 *     <li>字母 <em>L</em> 生成月份名称的独立形式。</li>
 *     </ul>
 *     <br></li>
 * <li><strong><a name="timezone">一般时区：</a></strong>
 *     如果时区有名称，则它们被解释为 <a href="#text">文本</a>。对于表示 GMT 偏移值的时区，使用以下语法：
 *     <pre>
 *     <a name="GMTOffsetTimeZone"><i>GMTOffsetTimeZone:</i></a>
 *             <code>GMT</code> <i>Sign</i> <i>Hours</i> <code>:</code> <i>Minutes</i>
 *     <i>Sign:</i> 之一
 *             <code>+ -</code>
 *     <i>Hours:</i>
 *             <i>Digit</i>
 *             <i>Digit</i> <i>Digit</i>
 *     <i>Minutes:</i>
 *             <i>Digit</i> <i>Digit</i>
 *     <i>Digit:</i> 之一
 *             <code>0 1 2 3 4 5 6 7 8 9</code></pre>
 *     <i>Hours</i> 必须在 0 和 23 之间，<i>Minutes</i> 必须在 00 和 59 之间。格式与区域无关，数字必须取自 Unicode 标准的基本拉丁文块。
 *     <p>对于解析，<a href="#rfc822timezone">RFC 822 时区</a> 也被接受。<br><br></li>
 * <li><strong><a name="rfc822timezone">RFC 822 时区：</a></strong>
 *     对于格式化，使用 RFC 822 4 位时区格式：
 *
 *     <pre>
 *     <i>RFC822TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
 *     <i>TwoDigitHours:</i>
 *             <i>Digit Digit</i></pre>
 *     <i>TwoDigitHours</i> 必须在 00 和 23 之间。其他定义与 <a href="#timezone">一般时区</a> 相同。
 *
 *     <p>对于解析，<a href="#timezone">一般时区</a> 也被接受。
 * <li><strong><a name="iso8601timezone">ISO 8601 时区：</a></strong>
 *     模式字母的数量决定了格式化和解析的格式如下：
 *     <pre>
 *     <i>ISO8601TimeZone:</i>
 *             <i>OneLetterISO8601TimeZone</i>
 *             <i>TwoLetterISO8601TimeZone</i>
 *             <i>ThreeLetterISO8601TimeZone</i>
 *     <i>OneLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i>
 *             {@code Z}
 *     <i>TwoLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
 *             {@code Z}
 *     <i>ThreeLetterISO8601TimeZone:</i>
 *             <i>Sign</i> <i>TwoDigitHours</i> {@code :} <i>Minutes</i>
 *             {@code Z}</pre>
 *     其他定义与 <a href="#timezone">一般时区</a> 或 <a href="#rfc822timezone">RFC 822 时区</a> 相同。
 *
 *     <p>对于格式化，如果 GMT 偏移值为 0，则生成 {@code "Z"}。如果模式字母的数量为 1，则忽略小时的小数部分。例如，如果模式为 {@code "X"} 且时区为 {@code "GMT+05:30"}，则生成 {@code "+05"}。
 *
 *     <p>对于解析，{@code "Z"} 被解析为 UTC 时区标识符。
 *     <a href="#timezone">一般时区</a> <em>不</em> 被接受。
 *
 *     <p>如果模式字母的数量为 4 个或更多，则在构造 <code>SimpleDateFormat</code> 或应用模式时会抛出 {@link IllegalArgumentException}。
 * </ul>
 * <code>SimpleDateFormat</code> 还支持 <em>本地化的日期和时间模式</em> 字符串。在这些字符串中，上述模式字母可以被其他特定于区域的模式字母替换。
 * <code>SimpleDateFormat</code> 不处理除模式字母外的文本的本地化；这由类的客户端负责。
 *
 * <h4>示例</h4>
 *
 * 以下示例显示了在 US 区域中如何解释日期和时间模式。给定的日期和时间是 2001-07-04 12:08:56，位于美国太平洋时间时区。
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Examples of date and time patterns interpreted in the U.S. locale">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *         <th align=left>日期和时间模式
 *         <th align=left>结果
 *     <tr>
 *         <td><code>"yyyy.MM.dd G 'at' HH:mm:ss z"</code>
 *         <td><code>2001.07.04 AD at 12:08:56 PDT</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>"EEE, MMM d, ''yy"</code>
 *         <td><code>Wed, Jul 4, '01</code>
 *     <tr>
 *         <td><code>"h:mm a"</code>
 *         <td><code>12:08 PM</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>"hh 'o''clock' a, zzzz"</code>
 *         <td><code>12 o'clock PM, Pacific Daylight Time</code>
 *     <tr>
 *         <td><code>"K:mm a, z"</code>
 *         <td><code>0:08 PM, PDT</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>"yyyyy.MMMMM.dd GGG hh:mm aaa"</code>
 *         <td><code>02001.July.04 AD 12:08 PM</code>
 *     <tr>
 *         <td><code>"EEE, d MMM yyyy HH:mm:ss Z"</code>
 *         <td><code>Wed, 4 Jul 2001 12:08:56 -0700</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>"yyMMddHHmmssZ"</code>
 *         <td><code>010704120856-0700</code>
 *     <tr>
 *         <td><code>"yyyy-MM-dd'T'HH:mm:ss.SSSZ"</code>
 *         <td><code>2001-07-04T12:08:56.235-0700</code>
 *     <tr style="background-color: rgb(238, 238, 255);">
 *         <td><code>"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"</code>
 *         <td><code>2001-07-04T12:08:56.235-07:00</code>
 *     <tr>
 *         <td><code>"YYYY-'W'ww-u"</code>
 *         <td><code>2001-W27-3</code>
 * </table>
 * </blockquote>
 *
 * <h4><a name="synchronization">同步</a></h4>
 *
 * <p>
 * 日期格式器不是同步的。
 * 建议为每个线程创建单独的格式器实例。
 * 如果多个线程同时访问格式器，必须在外部进行同步。
 *
 * @see          <a href="https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html">Java 教程</a>
 * @see          java.util.Calendar
 * @see          java.util.TimeZone
 * @see          DateFormat
 * @see          DateFormatSymbols
 * @author       Mark Davis, Chen-Lieh Huang, Alan Liu
 */
public class SimpleDateFormat extends DateFormat {


                // 官方序列版本ID，以一种隐晦的方式说明了我们兼容的版本
    // 哪个版本我们是兼容的
    static final long serialVersionUID = 4774881970558875024L;

    // 内部序列版本，说明了写入的版本
    // - 0（默认）适用于JDK 1.1.3及之前的版本
    // - 1 适用于从JDK 1.1.4开始的版本，其中包含了一个新字段
    static final int currentSerialVersion = 1;

    /**
     * 流中序列化数据的版本。可能的值：
     * <ul>
     * <li><b>0</b> 或未在流中出现：JDK 1.1.3。此版本在流中没有 <code>defaultCenturyStart</code>。
     * <li><b>1</b> JDK 1.1.4 或更高版本。此版本添加了 <code>defaultCenturyStart</code>。
     * </ul>
     * 当流输出此类时，会写入最新的格式和最高的允许 <code>serialVersionOnStream</code>。
     * @serial
     * @since JDK1.1.4
     */
    private int serialVersionOnStream = currentSerialVersion;

    /**
     * 此格式器的模式字符串。这始终是一个非本地化的模式。不允许为 null。详情见类文档。
     * @serial
     */
    private String pattern;

    /**
     * 保存的 numberFormat 和模式。
     * @see SimpleDateFormat#checkNegativeNumberExpression
     */
    transient private NumberFormat originalNumberFormat;
    transient private String originalNumberPattern;

    /**
     * 用于格式化和解析的减号。
     */
    transient private char minusSign = '-';

    /**
     * 当负号跟随数字时为真。
     * （默认情况下在阿拉伯语中为真。）
     */
    transient private boolean hasFollowingMinusSign = false;

    /**
     * 如果需要使用独立形式，则为真。
     */
    transient private boolean forceStandaloneForm = false;

    /**
     * 编译后的模式。
     */
    transient private char[] compiledPattern;

    /**
     * 编译后模式的标签。
     */
    private final static int TAG_QUOTE_ASCII_CHAR       = 100;
    private final static int TAG_QUOTE_CHARS            = 101;

    /**
     * 依赖于区域设置的数字零。
     * @see #zeroPaddingNumber
     * @see java.text.DecimalFormatSymbols#getZeroDigit
     */
    transient private char zeroDigit;

    /**
     * 此格式器使用的周名称、月名称等符号。不允许为 null。
     * @serial
     * @see java.text.DateFormatSymbols
     */
    private DateFormatSymbols formatData;

    /**
     * 我们将带有两位数年的日期映射到从 <code>defaultCenturyStart</code> 开始的世纪，这可以是任何日期。不允许为 null。
     * @serial
     * @since JDK1.1.4
     */
    private Date defaultCenturyStart;

    transient private int defaultCenturyStartYear;

    private static final int MILLIS_PER_MINUTE = 60 * 1000;

    // 对于没有名称的时区，使用字符串 GMT+minutes 和 GMT-minutes。例如，在法国时区为 GMT+60。
    private static final String GMT = "GMT";

    /**
     * 使用区域设置键缓存 NumberFormat 实例。
     */
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData
        = new ConcurrentHashMap<>(3);

    /**
     * 用于实例化此 <code>SimpleDateFormat</code> 的区域设置。如果此对象是由较旧的 <code>SimpleDateFormat</code> 创建并反序列化，则该值可能为 null。
     *
     * @serial
     * @since 1.6
     */
    private Locale locale;

    /**
     * 指示此 <code>SimpleDateFormat</code> 是否应使用 DateFormatSymbols。如果为真，则格式化和解析方法使用 DateFormatSymbols 的值。如果为假，则格式化和解析方法调用 Calendar.getDisplayName 或 Calendar.getDisplayNames。
     */
    transient boolean useDateFormatSymbols;

    /**
     * 使用默认模式和默认的 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的日期格式符号构造 <code>SimpleDateFormat</code>。
     * <b>注意：</b>此构造函数可能不支持所有区域设置。为了全面覆盖，请使用 {@link DateFormat} 类中的工厂方法。
     */
    public SimpleDateFormat() {
        this("", Locale.getDefault(Locale.Category.FORMAT));
        applyPatternImpl(LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(locale)
                         .getDateTimePattern(SHORT, SHORT, calendar));
    }

    /**
     * 使用给定的模式和默认的 {@link java.util.Locale.Category#FORMAT FORMAT} 区域设置的日期格式符号构造 <code>SimpleDateFormat</code>。
     * <b>注意：</b>此构造函数可能不支持所有区域设置。为了全面覆盖，请使用 {@link DateFormat} 类中的工厂方法。
     * <p>这相当于调用
     * {@link #SimpleDateFormat(String, Locale)
     *     SimpleDateFormat(pattern, Locale.getDefault(Locale.Category.FORMAT))}。
     *
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @param pattern 描述日期和时间格式的模式
     * @exception NullPointerException 如果给定的模式为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    public SimpleDateFormat(String pattern)
    {
        this(pattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 使用给定的模式和给定区域设置的默认日期格式符号构造 <code>SimpleDateFormat</code>。
     * <b>注意：</b>此构造函数可能不支持所有区域设置。为了全面覆盖，请使用 {@link DateFormat} 类中的工厂方法。
     *
     * @param pattern 描述日期和时间格式的模式
     * @param locale 应使用的区域设置
     * @exception NullPointerException 如果给定的模式或区域设置为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    public SimpleDateFormat(String pattern, Locale locale)
    {
        if (pattern == null || locale == null) {
            throw new NullPointerException();
        }


                    initializeCalendar(locale);
        this.pattern = pattern;
        this.formatData = DateFormatSymbols.getInstanceRef(locale);
        this.locale = locale;
        initialize(locale);
    }

    /**
     * 使用给定的模式和日期格式符号构造一个 <code>SimpleDateFormat</code>。
     *
     * @param pattern 描述日期和时间格式的模式
     * @param formatSymbols 用于格式化的日期格式符号
     * @exception NullPointerException 如果给定的模式或 formatSymbols 为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols)
    {
        if (pattern == null || formatSymbols == null) {
            throw new NullPointerException();
        }

        this.pattern = pattern;
        this.formatData = (DateFormatSymbols) formatSymbols.clone();
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        initializeCalendar(this.locale);
        initialize(this.locale);
        useDateFormatSymbols = true;
    }

    /* 初始化 compiledPattern 和 numberFormat 字段 */
    private void initialize(Locale loc) {
        // 验证并编译给定的模式。
        compiledPattern = compile(pattern);

        /* 首先尝试缓存 */
        numberFormat = cachedNumberFormatData.get(loc);
        if (numberFormat == null) { /* 缓存未命中 */
            numberFormat = NumberFormat.getIntegerInstance(loc);
            numberFormat.setGroupingUsed(false);

            /* 更新缓存 */
            cachedNumberFormatData.putIfAbsent(loc, numberFormat);
        }
        numberFormat = (NumberFormat) numberFormat.clone();

        initializeDefaultCentury();
    }

    private void initializeCalendar(Locale loc) {
        if (calendar == null) {
            assert loc != null;
            // 格式对象必须使用此区域的符号构造。
            // 但是，日历应使用当前默认的时区。
            // 如果此时区未包含在区域字符串中，则时区将使用通用的 GMT+/-H:MM 术语格式化。
            calendar = Calendar.getInstance(TimeZone.getDefault(), loc);
        }
    }

    /**
     * 返回给定模式的编译形式。编译模式的语法为：
     * <blockquote>
     * CompiledPattern:
     *     EntryList
     * EntryList:
     *     Entry
     *     EntryList Entry
     * Entry:
     *     TagField
     *     TagField data
     * TagField:
     *     Tag Length
     *     TaggedData
     * Tag:
     *     pattern_char_index
     *     TAG_QUOTE_CHARS
     * Length:
     *     short_length
     *     long_length
     * TaggedData:
     *     TAG_QUOTE_ASCII_CHAR ascii_char
     *
     * </blockquote>
     *
     * 其中 `short_length' 是一个介于 0 和 254 之间的 8 位无符号整数。`long_length' 是一个 8 位整数 255 和一个 32 位有符号整数值的序列，该值被拆分为两个 char 的上部和下部 16 位字段。`pattern_char_index' 是一个介于 0 和 18 之间的 8 位整数。`ascii_char' 是一个 7 位 ASCII 字符值。`data' 取决于其 Tag 值。
     * <p>
     * 如果 Length 是 short_length，Tag 和 short_length 将被打包在一个 char 中，如下所示。
     * <blockquote>
     *     char[0] = (Tag << 8) | short_length;
     * </blockquote>
     *
     * 如果 Length 是 long_length，Tag 和 255 将被打包在第一个 char 中，32 位整数如下所示。
     * <blockquote>
     *     char[0] = (Tag << 8) | 255;
     *     char[1] = (char) (long_length >>> 16);
     *     char[2] = (char) (long_length & 0xffff);
     * </blockquote>
     * <p>
     * 如果 Tag 是 pattern_char_index，其 Length 是模式字符的数量。例如，如果给定的模式是 "yyyy"，Tag 是 1，Length 是 4，后面没有数据。
     * <p>
     * 如果 Tag 是 TAG_QUOTE_CHARS，其 Length 是跟随 TagField 的 char 数量。例如，如果给定的模式是 "'o''clock'"，Length 是 7，后面是一个 char 序列 <code>o&nbs;'&nbs;c&nbs;l&nbs;o&nbs;c&nbs;k</code>。
     * <p>
     * TAG_QUOTE_ASCII_CHAR 是一个特殊标签，其 Length 位置是一个 ASCII 字符。例如，如果给定的模式是 "'o'"，TaggedData 条目是 <code>((TAG_QUOTE_ASCII_CHAR&nbs;<<&nbs;8)&nbs;|&nbs;'o')</code>。
     *
     * @exception NullPointerException 如果给定的模式为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    private char[] compile(String pattern) {
        int length = pattern.length();
        boolean inQuote = false;
        StringBuilder compiledCode = new StringBuilder(length * 2);
        StringBuilder tmpBuffer = null;
        int count = 0, tagcount = 0;
        int lastTag = -1, prevTag = -1;

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);

            if (c == '\'') {
                // '' 被视为单引号，无论是否在引用部分。
                if ((i + 1) < length) {
                    c = pattern.charAt(i + 1);
                    if (c == '\'') {
                        i++;
                        if (count != 0) {
                            encode(lastTag, count, compiledCode);
                            tagcount++;
                            prevTag = lastTag;
                            lastTag = -1;
                            count = 0;
                        }
                        if (inQuote) {
                            tmpBuffer.append(c);
                        } else {
                            compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                        }
                        continue;
                    }
                }
                if (!inQuote) {
                    if (count != 0) {
                        encode(lastTag, count, compiledCode);
                        tagcount++;
                        prevTag = lastTag;
                        lastTag = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(0);
                    }
                    inQuote = true;
                } else {
                    int len = tmpBuffer.length();
                    if (len == 1) {
                        char ch = tmpBuffer.charAt(0);
                        if (ch < 128) {
                            compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | ch));
                        } else {
                            compiledCode.append((char)(TAG_QUOTE_CHARS << 8 | 1));
                            compiledCode.append(ch);
                        }
                    } else {
                        encode(TAG_QUOTE_CHARS, len, compiledCode);
                        compiledCode.append(tmpBuffer);
                    }
                    inQuote = false;
                }
                continue;
            }
            if (inQuote) {
                tmpBuffer.append(c);
                continue;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                if (count != 0) {
                    encode(lastTag, count, compiledCode);
                    tagcount++;
                    prevTag = lastTag;
                    lastTag = -1;
                    count = 0;
                }
                if (c < 128) {
                    // 在大多数情况下，c 会是一个分隔符，如 ':'。
                    compiledCode.append((char)(TAG_QUOTE_ASCII_CHAR << 8 | c));
                } else {
                    // 将任何连续的非 ASCII 字母字符放在一个单独的 TAG_QUOTE_CHARS 中。
                    int j;
                    for (j = i + 1; j < length; j++) {
                        char d = pattern.charAt(j);
                        if (d == '\'' || (d >= 'a' && d <= 'z' || d >= 'A' && d <= 'Z')) {
                            break;
                        }
                    }
                    compiledCode.append((char)(TAG_QUOTE_CHARS << 8 | (j - i)));
                    for (; i < j; i++) {
                        compiledCode.append(pattern.charAt(i));
                    }
                    i--;
                }
                continue;
            }


                        int tag;
            if ((tag = DateFormatSymbols.patternChars.indexOf(c)) == -1) {
                throw new IllegalArgumentException("非法的模式字符 " +
                                                   "'" + c + "'");
            }
            if (lastTag == -1 || lastTag == tag) {
                lastTag = tag;
                count++;
                continue;
            }
            encode(lastTag, count, compiledCode);
            tagcount++;
            prevTag = lastTag;
            lastTag = tag;
            count = 1;
        }

        if (inQuote) {
            throw new IllegalArgumentException("未终止的引号");
        }

        if (count != 0) {
            encode(lastTag, count, compiledCode);
            tagcount++;
            prevTag = lastTag;
        }

        forceStandaloneForm = (tagcount == 1 && prevTag == PATTERN_MONTH);

        // 将编译的模式复制到字符数组
        int len = compiledCode.length();
        char[] r = new char[len];
        compiledCode.getChars(0, len, r, 0);
        return r;
    }

    /**
     * 对给定的标签和长度进行编码，并将编码后的字符放入缓冲区。
     */
    private static void encode(int tag, int length, StringBuilder buffer) {
        if (tag == PATTERN_ISO_ZONE && length >= 4) {
            throw new IllegalArgumentException("无效的 ISO 8601 格式: 长度=" + length);
        }
        if (length < 255) {
            buffer.append((char)(tag << 8 | length));
        } else {
            buffer.append((char)((tag << 8) | 0xff));
            buffer.append((char)(length >>> 16));
            buffer.append((char)(length & 0xffff));
        }
    }

    /* 初始化我们用于消除歧义年份的字段。单独初始化
     * 以便可以从 readObject() 中调用。
     */
    private void initializeDefaultCentury() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add( Calendar.YEAR, -80 );
        parseAmbiguousDatesAsAfter(calendar.getTime());
    }

    /* 定义一个世纪窗口，用于消除使用两位年份的日期歧义。
     */
    private void parseAmbiguousDatesAsAfter(Date startDate) {
        defaultCenturyStart = startDate;
        calendar.setTime(startDate);
        defaultCenturyStartYear = calendar.get(Calendar.YEAR);
    }

    /**
     * 设置两位年份将被解释为的 100 年周期的开始日期。
     *
     * @param startDate 在解析过程中，两位年份将被放置在 <code>startDate</code> 到 <code>startDate + 100 年</code> 的范围内。
     * @see #get2DigitYearStart
     * @since 1.2
     */
    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(new Date(startDate.getTime()));
    }

    /**
     * 返回两位年份将被解释为的 100 年周期的开始日期。
     *
     * @return 解析两位年份的 100 年周期的开始日期。
     * @see #set2DigitYearStart
     * @since 1.2
     */
    public Date get2DigitYearStart() {
        return (Date) defaultCenturyStart.clone();
    }

    /**
     * 将给定的 <code>Date</code> 格式化为日期/时间字符串，并将结果追加到给定的 <code>StringBuffer</code> 中。
     *
     * @param date 要格式化为日期/时间字符串的日期时间值。
     * @param toAppendTo 新的日期时间文本将追加到此处。
     * @param pos 格式化位置。输入：对齐字段（如果需要）。输出：对齐字段的偏移量。
     * @return 格式化的日期时间字符串。
     * @exception NullPointerException 如果给定的 {@code date} 为 {@code null}。
     */
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                               FieldPosition pos)
    {
        pos.beginIndex = pos.endIndex = 0;
        return format(date, toAppendTo, pos.getFieldDelegate());
    }

    // 从 Format 创建 FieldDelegate 后调用
    private StringBuffer format(Date date, StringBuffer toAppendTo,
                                FieldDelegate delegate) {
        // 将输入日期转换为时间字段列表
        calendar.setTime(date);

        boolean useDateFormatSymbols = useDateFormatSymbols();

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }

            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                toAppendTo.append((char)count);
                break;

            case TAG_QUOTE_CHARS:
                toAppendTo.append(compiledPattern, i, count);
                i += count;
                break;

            default:
                subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
                break;
            }
        }
        return toAppendTo;
    }

    /**
     * 格式化一个对象，生成一个 <code>AttributedCharacterIterator</code>。
     * 可以使用返回的 <code>AttributedCharacterIterator</code> 构建结果字符串，以及确定结果字符串的信息。
     * <p>
     * AttributedCharacterIterator 的每个属性键都是 <code>DateFormat.Field</code> 类型，对应的属性值与属性键相同。
     *
     * @exception NullPointerException 如果 obj 为 null。
     * @exception IllegalArgumentException 如果格式化程序无法格式化给定对象，或者格式化程序的模式字符串无效。
     * @param obj 要格式化的对象
     * @return 描述格式化值的 AttributedCharacterIterator。
     * @since 1.4
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        CharacterIteratorFieldDelegate delegate = new
                         CharacterIteratorFieldDelegate();


                    if (obj instanceof Date) {
            format((Date)obj, sb, delegate);
        }
        else if (obj instanceof Number) {
            format(new Date(((Number)obj).longValue()), sb, delegate);
        }
        else if (obj == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator 必须传递非空对象");
        }
        else {
            throw new IllegalArgumentException(
                             "无法将给定对象格式化为日期");
        }
        return delegate.getIterator(sb.toString());
    }

    // 将模式字符字符串索引映射到 Calendar 字段编号
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = {
        Calendar.ERA,
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DATE,
        Calendar.HOUR_OF_DAY,
        Calendar.HOUR_OF_DAY,
        Calendar.MINUTE,
        Calendar.SECOND,
        Calendar.MILLISECOND,
        Calendar.DAY_OF_WEEK,
        Calendar.DAY_OF_YEAR,
        Calendar.DAY_OF_WEEK_IN_MONTH,
        Calendar.WEEK_OF_YEAR,
        Calendar.WEEK_OF_MONTH,
        Calendar.AM_PM,
        Calendar.HOUR,
        Calendar.HOUR,
        Calendar.ZONE_OFFSET,
        Calendar.ZONE_OFFSET,
        CalendarBuilder.WEEK_YEAR,         // 伪 Calendar 字段
        CalendarBuilder.ISO_DAY_OF_WEEK,   // 伪 Calendar 字段
        Calendar.ZONE_OFFSET,
        Calendar.MONTH
    };

    // 将模式字符字符串索引映射到 DateFormat 字段编号
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = {
        DateFormat.ERA_FIELD,
        DateFormat.YEAR_FIELD,
        DateFormat.MONTH_FIELD,
        DateFormat.DATE_FIELD,
        DateFormat.HOUR_OF_DAY1_FIELD,
        DateFormat.HOUR_OF_DAY0_FIELD,
        DateFormat.MINUTE_FIELD,
        DateFormat.SECOND_FIELD,
        DateFormat.MILLISECOND_FIELD,
        DateFormat.DAY_OF_WEEK_FIELD,
        DateFormat.DAY_OF_YEAR_FIELD,
        DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD,
        DateFormat.WEEK_OF_YEAR_FIELD,
        DateFormat.WEEK_OF_MONTH_FIELD,
        DateFormat.AM_PM_FIELD,
        DateFormat.HOUR1_FIELD,
        DateFormat.HOUR0_FIELD,
        DateFormat.TIMEZONE_FIELD,
        DateFormat.TIMEZONE_FIELD,
        DateFormat.YEAR_FIELD,
        DateFormat.DAY_OF_WEEK_FIELD,
        DateFormat.TIMEZONE_FIELD,
        DateFormat.MONTH_FIELD
    };

    // 从 DecimalFormatSymbols 索引映射到 Field 常量
    private static final Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = {
        Field.ERA,
        Field.YEAR,
        Field.MONTH,
        Field.DAY_OF_MONTH,
        Field.HOUR_OF_DAY1,
        Field.HOUR_OF_DAY0,
        Field.MINUTE,
        Field.SECOND,
        Field.MILLISECOND,
        Field.DAY_OF_WEEK,
        Field.DAY_OF_YEAR,
        Field.DAY_OF_WEEK_IN_MONTH,
        Field.WEEK_OF_YEAR,
        Field.WEEK_OF_MONTH,
        Field.AM_PM,
        Field.HOUR1,
        Field.HOUR0,
        Field.TIME_ZONE,
        Field.TIME_ZONE,
        Field.YEAR,
        Field.DAY_OF_WEEK,
        Field.TIME_ZONE,
        Field.MONTH
    };

    /**
     * 私有成员函数，执行实际的日期/时间格式化。
     */
    private void subFormat(int patternCharIndex, int count,
                           FieldDelegate delegate, StringBuffer buffer,
                           boolean useDateFormatSymbols)
    {
        int     maxIntCount = Integer.MAX_VALUE;
        String  current = null;
        int     beginOffset = buffer.length();

        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        int value;
        if (field == CalendarBuilder.WEEK_YEAR) {
            if (calendar.isWeekDateSupported()) {
                value = calendar.getWeekYear();
            } else {
                // 使用日历年 'y' 代替
                patternCharIndex = PATTERN_YEAR;
                field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
                value = calendar.get(field);
            }
        } else if (field == CalendarBuilder.ISO_DAY_OF_WEEK) {
            value = CalendarBuilder.toISODayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            value = calendar.get(field);
        }

        int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
        if (!useDateFormatSymbols && field < Calendar.ZONE_OFFSET
            && patternCharIndex != PATTERN_MONTH_STANDALONE) {
            current = calendar.getDisplayName(field, style, locale);
        }

        // 注意：zeroPaddingNumber() 假设 maxDigits 为 2 或 maxIntCount。如果对此进行任何更改，
        // 必须修复 zeroPaddingNumber()。

        switch (patternCharIndex) {
        case PATTERN_ERA: // 'G'
            if (useDateFormatSymbols) {
                String[] eras = formatData.getEras();
                if (value < eras.length) {
                    current = eras[value];
                }
            }
            if (current == null) {
                current = "";
            }
            break;

        case PATTERN_WEEK_YEAR: // 'Y'
        case PATTERN_YEAR:      // 'y'
            if (calendar instanceof GregorianCalendar) {
                if (count != 2) {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, 2, 2, buffer);
                } // 将 1996 裁剪为 96
            } else {
                if (current == null) {
                    zeroPaddingNumber(value, style == Calendar.LONG ? 1 : count,
                                      maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_MONTH:            // 'M' (上下文敏感)
            if (useDateFormatSymbols) {
                String[] months;
                if (count >= 4) {
                    months = formatData.getMonths();
                    current = months[value];
                } else if (count == 3) {
                    months = formatData.getShortMonths();
                    current = months[value];
                }
            } else {
                if (count < 3) {
                    current = null;
                } else if (forceStandaloneForm) {
                    current = calendar.getDisplayName(field, style | 0x8000, locale);
                    if (current == null) {
                        current = calendar.getDisplayName(field, style, locale);
                    }
                }
            }
            if (current == null) {
                zeroPaddingNumber(value+1, count, maxIntCount, buffer);
            }
            break;


                    case PATTERN_MONTH_STANDALONE: // 'L'
            assert current == null;
            if (locale == null) {
                String[] months;
                if (count >= 4) {
                    months = formatData.getMonths();
                    current = months[value];
                } else if (count == 3) {
                    months = formatData.getShortMonths();
                    current = months[value];
                }
            } else {
                if (count >= 3) {
                    current = calendar.getDisplayName(field, style | 0x8000, locale);
                }
            }
            if (current == null) {
                zeroPaddingNumber(value + 1, count, maxIntCount, buffer);
            }
            break;

        case PATTERN_HOUR_OF_DAY1: // 'k' 1-based.  eg, 23:59 + 1 hour =>> 24:59
            if (current == null) {
                if (value == 0) {
                    zeroPaddingNumber(calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1,
                                      count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_DAY_OF_WEEK: // 'E'
            if (useDateFormatSymbols) {
                String[] weekdays;
                if (count >= 4) {
                    weekdays = formatData.getWeekdays();
                    current = weekdays[value];
                } else { // count < 4, use abbreviated form if exists
                    weekdays = formatData.getShortWeekdays();
                    current = weekdays[value];
                }
            }
            break;

        case PATTERN_AM_PM:    // 'a'
            if (useDateFormatSymbols) {
                String[] ampm = formatData.getAmPmStrings();
                current = ampm[value];
            }
            break;

        case PATTERN_HOUR1:    // 'h' 1-based.  eg, 11PM + 1 hour =>> 12 AM
            if (current == null) {
                if (value == 0) {
                    zeroPaddingNumber(calendar.getLeastMaximum(Calendar.HOUR) + 1,
                                      count, maxIntCount, buffer);
                } else {
                    zeroPaddingNumber(value, count, maxIntCount, buffer);
                }
            }
            break;

        case PATTERN_ZONE_NAME: // 'z'
            if (current == null) {
                if (formatData.locale == null || formatData.isZoneStringsSet) {
                    int zoneIndex =
                        formatData.getZoneIndex(calendar.getTimeZone().getID());
                    if (zoneIndex == -1) {
                        value = calendar.get(Calendar.ZONE_OFFSET) +
                            calendar.get(Calendar.DST_OFFSET);
                        buffer.append(ZoneInfoFile.toCustomID(value));
                    } else {
                        int index = (calendar.get(Calendar.DST_OFFSET) == 0) ? 1 : 3;
                        if (count < 4) {
                            // Use the short name
                            index++;
                        }
                        String[][] zoneStrings = formatData.getZoneStringsWrapper();
                        buffer.append(zoneStrings[zoneIndex][index]);
                    }
                } else {
                    TimeZone tz = calendar.getTimeZone();
                    boolean daylight = (calendar.get(Calendar.DST_OFFSET) != 0);
                    int tzstyle = (count < 4 ? TimeZone.SHORT : TimeZone.LONG);
                    buffer.append(tz.getDisplayName(daylight, tzstyle, formatData.locale));
                }
            }
            break;

        case PATTERN_ZONE_VALUE: // 'Z' ("-/+hhmm" form)
            value = (calendar.get(Calendar.ZONE_OFFSET) +
                     calendar.get(Calendar.DST_OFFSET)) / 60000;

            int width = 4;
            if (value >= 0) {
                buffer.append('+');
            } else {
                width++;
            }

            int num = (value / 60) * 100 + (value % 60);
            CalendarUtils.sprintf0d(buffer, num, width);
            break;

        case PATTERN_ISO_ZONE:   // 'X'
            value = calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET);

            if (value == 0) {
                buffer.append('Z');
                break;
            }

            value /=  60000;
            if (value >= 0) {
                buffer.append('+');
            } else {
                buffer.append('-');
                value = -value;
            }

            CalendarUtils.sprintf0d(buffer, value / 60, 2);
            if (count == 1) {
                break;
            }

            if (count == 3) {
                buffer.append(':');
            }
            CalendarUtils.sprintf0d(buffer, value % 60, 2);
            break;

        default:
     // case PATTERN_DAY_OF_MONTH:         // 'd'
     // case PATTERN_HOUR_OF_DAY0:         // 'H' 0-based.  eg, 23:59 + 1 hour =>> 00:59
     // case PATTERN_MINUTE:               // 'm'
     // case PATTERN_SECOND:               // 's'
     // case PATTERN_MILLISECOND:          // 'S'
     // case PATTERN_DAY_OF_YEAR:          // 'D'
     // case PATTERN_DAY_OF_WEEK_IN_MONTH: // 'F'
     // case PATTERN_WEEK_OF_YEAR:         // 'w'
     // case PATTERN_WEEK_OF_MONTH:        // 'W'
     // case PATTERN_HOUR0:                // 'K' eg, 11PM + 1 hour =>> 0 AM
     // case PATTERN_ISO_DAY_OF_WEEK:      // 'u' pseudo field, Monday = 1, ..., Sunday = 7
            if (current == null) {
                zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
            break;
        } // switch (patternCharIndex)

        if (current != null) {
            buffer.append(current);
        }

        int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex];
        Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex];

        delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), buffer);
    }

    /**
     * Formats a number with the specified minimum and maximum number of digits.
     */
    private void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer)
    {
        // Optimization for 1, 2 and 4 digit numbers. This should
        // cover most cases of formatting date/time related items.
        // Note: This optimization code assumes that maxDigits is
        // either 2 or Integer.MAX_VALUE (maxIntCount in format()).
        try {
            if (zeroDigit == 0) {
                zeroDigit = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < 100 && minDigits >= 1 && minDigits <= 2) {
                    if (value < 10) {
                        if (minDigits == 2) {
                            buffer.append(zeroDigit);
                        }
                        buffer.append((char)(zeroDigit + value));
                    } else {
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                    }
                    return;
                } else if (value >= 1000 && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char)(zeroDigit + value / 1000));
                        value %= 1000;
                        buffer.append((char)(zeroDigit + value / 100));
                        value %= 100;
                        buffer.append((char)(zeroDigit + value / 10));
                        buffer.append((char)(zeroDigit + value % 10));
                        return;
                    }
                    if (minDigits == 2 && maxDigits == 2) {
                        zeroPaddingNumber(value % 100, 2, 2, buffer);
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }


                    numberFormat.setMinimumIntegerDigits(minDigits);
        numberFormat.setMaximumIntegerDigits(maxDigits);
        numberFormat.format((long)value, buffer, DontCareFieldPosition.INSTANCE);
    }


    /**
     * 从字符串中解析文本以生成 <code>Date</code>。
     * <p>
     * 该方法尝试从由 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为使用后的最后一个字符的索引（解析不一定使用到字符串末尾的所有字符），并返回解析的日期。更新后的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会改变，<code>pos</code> 的错误索引将设置为发生错误的字符的索引，并返回 null。
     *
     * <p>此解析操作使用 {@link DateFormat#calendar
     * calendar} 生成 {@code Date}。在解析之前，{@code
     * calendar} 的所有日期时间字段都被 {@linkplain Calendar#clear()
     * 清除}，并且使用 {@code calendar} 的默认值来填充任何缺失的日期时间信息。例如，如果解析操作没有给出年份值，则使用 {@link GregorianCalendar} 解析的 {@code Date} 的年份值为 1970。根据给定的模式和 {@code text} 中的时区值，{@code
     * TimeZone} 值可能会被覆盖。通过调用 {@link #setTimeZone(java.util.TimeZone) setTimeZone} 设置的任何 {@code
     * TimeZone} 值可能需要恢复以进行进一步的操作。
     *
     * @param text  一个 <code>String</code>，其部分应被解析。
     * @param pos   一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return 从字符串解析的 <code>Date</code>。发生错误时，返回 null。
     * @exception NullPointerException 如果 <code>text</code> 或 <code>pos</code> 为 null。
     */
    @Override
    public Date parse(String text, ParsePosition pos)
    {
        checkNegativeNumberExpression();

        int start = pos.index;
        int oldStart = start;
        int textLength = text.length();

        boolean[] ambiguousYear = {false};

        CalendarBuilder calb = new CalendarBuilder();

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }

            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                if (start >= textLength || text.charAt(start) != (char)count) {
                    pos.index = oldStart;
                    pos.errorIndex = start;
                    return null;
                }
                start++;
                break;

            case TAG_QUOTE_CHARS:
                while (count-- > 0) {
                    if (start >= textLength || text.charAt(start) != compiledPattern[i++]) {
                        pos.index = oldStart;
                        pos.errorIndex = start;
                        return null;
                    }
                    start++;
                }
                break;

            default:
                // 查看下一个模式以确定是否需要遵守模式字母的数量进行解析。
                // 当使用没有字段分隔符的模式解析连续的数字文本（例如 "20010704"）时，这是必需的，模式如 "yyyyMMdd"。
                boolean obeyCount = false;

                // 在阿拉伯语中，负数的减号放在数字之后。
                // 即使在其他语言环境中，也可以使用 DateFormat.setNumberFormat() 将减号放在数字之后。
                // 如果减号和字段分隔符都是 '-'，subParse() 需要确定给定文本中数字后的 '-' 是分隔符还是前一个数字的减号。我们根据 compiledPattern 中的信息给 subParse() 提供线索。
                boolean useFollowingMinusSignAsDelimiter = false;

                if (i < compiledPattern.length) {
                    int nextTag = compiledPattern[i] >>> 8;
                    if (!(nextTag == TAG_QUOTE_ASCII_CHAR ||
                          nextTag == TAG_QUOTE_CHARS)) {
                        obeyCount = true;
                    }

                    if (hasFollowingMinusSign &&
                        (nextTag == TAG_QUOTE_ASCII_CHAR ||
                         nextTag == TAG_QUOTE_CHARS)) {
                        int c;
                        if (nextTag == TAG_QUOTE_ASCII_CHAR) {
                            c = compiledPattern[i] & 0xff;
                        } else {
                            c = compiledPattern[i+1];
                        }

                        if (c == minusSign) {
                            useFollowingMinusSignAsDelimiter = true;
                        }
                    }
                }
                start = subParse(text, start, tag, count, obeyCount,
                                 ambiguousYear, pos,
                                 useFollowingMinusSignAsDelimiter, calb);
                if (start < 0) {
                    pos.index = oldStart;
                    return null;
                }
            }
        }

        // 此时 Calendar 的字段已被设置。当计算时间时，Calendar 会为缺失的字段填充默认值。

        pos.index = start;

        Date parsedDate;
        try {
            parsedDate = calb.establish(calendar).getTime();
            // 如果年份值是模糊的，
            // 则两位数年份 == 默认起始年份
            if (ambiguousYear[0]) {
                if (parsedDate.before(defaultCenturyStart)) {
                    parsedDate = calb.addYear(100).establish(calendar).getTime();
                }
            }
        }
        // 如果任何字段超出范围（例如，MONTH == 17），Calendar.getTime() 将抛出 IllegalArgumentException。
        catch (IllegalArgumentException e) {
            pos.errorIndex = start;
            pos.index = oldStart;
            return null;
        }


                    return parsedDate;
    }

    /**
     * 用于 subParse 的私有代码大小缩减函数。
     * @param text 正在解析的时间文本。
     * @param start 开始解析的位置。
     * @param field 正在解析的日期字段。
     * @param data 要解析的字符串数组。
     * @return 如果匹配成功，则返回新的起始位置；否则返回负数表示匹配失败。
     */
    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb)
    {
        int i = 0;
        int count = data.length;

        if (field == Calendar.DAY_OF_WEEK) {
            i = 1;
        }

        // data[] 数组中可能有多个字符串以相同的前缀开始（例如，捷克语中的 Cerven 和 Cervenec（六月和七月））。
        // 我们跟踪最长的匹配项，并返回该匹配项。请注意，这不幸地要求我们测试所有数组元素。
        int bestMatchLength = 0, bestMatch = -1;
        for (; i<count; ++i)
        {
            int length = data[i].length();
            // 如果还没有匹配项，则始终进行比较；否则仅对潜在的更好匹配项（较长的字符串）进行比较。
            if (length > bestMatchLength &&
                text.regionMatches(true, start, data[i], 0, length))
            {
                bestMatch = i;
                bestMatchLength = length;
            }
        }
        if (bestMatch >= 0)
        {
            calb.set(field, bestMatch);
            return start + bestMatchLength;
        }
        return -start;
    }

    /**
     * 执行与 matchString(String, int, int, String[]) 相同的操作。此方法接受 Map<String, Integer> 而不是 String[]。
     */
    private int matchString(String text, int start, int field,
                            Map<String,Integer> data, CalendarBuilder calb) {
        if (data != null) {
            // TODO: 当它在规范中时，使其成为默认值。
            if (data instanceof SortedMap) {
                for (String name : data.keySet()) {
                    if (text.regionMatches(true, start, name, 0, name.length())) {
                        calb.set(field, data.get(name));
                        return start + name.length();
                    }
                }
                return -start;
            }

            String bestMatch = null;

            for (String name : data.keySet()) {
                int length = name.length();
                if (bestMatch == null || length > bestMatch.length()) {
                    if (text.regionMatches(true, start, name, 0, length)) {
                        bestMatch = name;
                    }
                }
            }

            if (bestMatch != null) {
                calb.set(field, data.get(bestMatch));
                return start + bestMatch.length();
            }
        }
        return -start;
    }

    private int matchZoneString(String text, int start, String[] zoneNames) {
        for (int i = 1; i <= 4; ++i) {
            // 检查长和短时区 [1 & 2]，
            // 以及长和短夏令时 [3 & 4]。
            String zoneName = zoneNames[i];
            if (text.regionMatches(true, start,
                                   zoneName, 0, zoneName.length())) {
                return i;
            }
        }
        return -1;
    }

    private boolean matchDSTString(String text, int start, int zoneIndex, int standardIndex,
                                   String[][] zoneStrings) {
        int index = standardIndex + 2;
        String zoneName  = zoneStrings[zoneIndex][index];
        if (text.regionMatches(true, start,
                               zoneName, 0, zoneName.length())) {
            return true;
        }
        return false;
    }

    /**
     * 查找与 zoneStrings 匹配的时间区域 'text' 并设置到内部日历。
     */
    private int subParseZoneString(String text, int start, CalendarBuilder calb) {
        boolean useSameName = false; // 如果标准时间和夏令时使用相同的缩写，则为 true。
        TimeZone currentTimeZone = getTimeZone();

        // 在此阶段，通过检查 TimeZoneNames 字符串中的区域数据来查找命名的时间区域。
        // 能够解析短形式和长形式。
        int zoneIndex = formatData.getZoneIndex(currentTimeZone.getID());
        TimeZone tz = null;
        String[][] zoneStrings = formatData.getZoneStringsWrapper();
        String[] zoneNames = null;
        int nameIndex = 0;
        if (zoneIndex != -1) {
            zoneNames = zoneStrings[zoneIndex];
            if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                if (nameIndex <= 2) {
                    // 检查标准名称（缩写）和夏令时名称是否相同。
                    useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                }
                tz = TimeZone.getTimeZone(zoneNames[0]);
            }
        }
        if (tz == null) {
            zoneIndex = formatData.getZoneIndex(TimeZone.getDefault().getID());
            if (zoneIndex != -1) {
                zoneNames = zoneStrings[zoneIndex];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                }
            }
        }

        if (tz == null) {
            int len = zoneStrings.length;
            for (int i = 0; i < len; i++) {
                zoneNames = zoneStrings[i];
                if ((nameIndex = matchZoneString(text, start, zoneNames)) > 0) {
                    if (nameIndex <= 2) {
                        useSameName = zoneNames[nameIndex].equalsIgnoreCase(zoneNames[nameIndex + 2]);
                    }
                    tz = TimeZone.getTimeZone(zoneNames[0]);
                    break;
                }
            }
        }
        if (tz != null) { // 匹配到任何？
            if (!tz.equals(currentTimeZone)) {
                setTimeZone(tz);
            }
            // 如果匹配的时间区域在标准时间和夏令时使用相同的名称
            // （缩写），则让日历中的时间区域决定哪一个。
            //
            // 还有如果 tz.getDSTSaving() 返回 0 表示夏令时，使用 tz 确定本地时间。（6645292）
            int dstAmount = (nameIndex >= 3) ? tz.getDSTSavings() : 0;
            if (!(useSameName || (nameIndex >= 3 && dstAmount == 0))) {
                calb.clear(Calendar.ZONE_OFFSET).set(Calendar.DST_OFFSET, dstAmount);
            }
            return (start + zoneNames[nameIndex].length());
        }
        return -start;
    }


                /**
     * 解析时间区域偏移的数字形式，如 "hh:mm"，并将解析值设置到 calb 中。
     *
     * @param text  要解析的文本
     * @param start 开始解析的字符位置
     * @param sign  1: 正数; -1: 负数
     * @param count 0: 'Z' 或 "GMT+hh:mm" 解析; 1 - 3: 'X' 的数量
     * @param colon true - hh 和 mm 之间需要冒号; false - 不需要冒号
     * @param calb  用于存储解析值的 CalendarBuilder
     * @return 更新的解析位置，或其负值表示解析错误
     */
    private int subParseNumericZone(String text, int start, int sign, int count,
                                    boolean colon, CalendarBuilder calb) {
        int index = start;

      parse:
        try {
            char c = text.charAt(index++);
            // 解析 hh
            int hours;
            if (!isDigit(c)) {
                break parse;
            }
            hours = c - '0';
            c = text.charAt(index++);
            if (isDigit(c)) {
                hours = hours * 10 + (c - '0');
            } else {
                // 如果在 RFC 822 或 'X' (ISO) 中没有冒号，则需要两个数字。
                if (count > 0 || !colon) {
                    break parse;
                }
                --index;
            }
            if (hours > 23) {
                break parse;
            }
            int minutes = 0;
            if (count != 1) {
                // 继续解析 mm
                c = text.charAt(index++);
                if (colon) {
                    if (c != ':') {
                        break parse;
                    }
                    c = text.charAt(index++);
                }
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = c - '0';
                c = text.charAt(index++);
                if (!isDigit(c)) {
                    break parse;
                }
                minutes = minutes * 10 + (c - '0');
                if (minutes > 59) {
                    break parse;
                }
            }
            minutes += hours * 60;
            calb.set(Calendar.ZONE_OFFSET, minutes * MILLIS_PER_MINUTE * sign)
                .set(Calendar.DST_OFFSET, 0);
            return index;
        } catch (IndexOutOfBoundsException e) {
        }
        return  1 - index; // -(index - 1)
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 私有成员函数，将解析的日期字符串转换为 timeFields。如果失败，返回 -start (对于 ParsePosition)。
     * @param text 要解析的时间文本。
     * @param start 开始解析的位置。
     * @param patternCharIndex 模式字符的索引。
     * @param count 模式字符的数量。
     * @param obeyCount 如果为 true，则下一个字段直接紧邻此字段，
     * 并且我们应该使用 count 来知道何时停止解析。
     * @param ambiguousYear 返回参数；如果 ambiguousYear[0] 为 true，则解析了两位数的年份，可能需要调整。
     * @param origPos origPos.errorIndex 用于返回发生匹配失败时的错误索引。
     * @return 如果匹配成功，则返回新的起始位置；-1 表示匹配失败，否则返回其他值。如果发生匹配失败，
     * 则设置 origPos.errorIndex 为错误索引。
     */
    private int subParse(String text, int start, int patternCharIndex, int count,
                         boolean obeyCount, boolean[] ambiguousYear,
                         ParsePosition origPos,
                         boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
        Number number;
        int value = 0;
        ParsePosition pos = new ParsePosition(0);
        pos.index = start;
        if (patternCharIndex == PATTERN_WEEK_YEAR && !calendar.isWeekDateSupported()) {
            // 使用日历年 'y' 代替
            patternCharIndex = PATTERN_YEAR;
        }
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];

        // 如果这里有任何空格，跳过它们。如果到达字符串末尾，则失败。
        for (;;) {
            if (pos.index >= text.length()) {
                origPos.errorIndex = start;
                return -1;
            }
            char c = text.charAt(pos.index);
            if (c != ' ' && c != '\t') {
                break;
            }
            ++pos.index;
        }
        // 记录实际的起始索引
        int actualStart = pos.index;

      parsing:
        {
            // 我们在这里处理一些特殊情况，需要解析数字值。我们在下面处理更通用的情况。我们需要在这里处理一些，
            // 因为某些字段需要对解析值进行额外处理。
            if (patternCharIndex == PATTERN_HOUR_OF_DAY1 ||
                patternCharIndex == PATTERN_HOUR1 ||
                (patternCharIndex == PATTERN_MONTH && count <= 2) ||
                patternCharIndex == PATTERN_YEAR ||
                patternCharIndex == PATTERN_WEEK_YEAR) {
                // 将此与下面的 obeyCount 逻辑统一会很好，但这将很困难。
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number == null) {
                    if (patternCharIndex != PATTERN_YEAR || calendar instanceof GregorianCalendar) {
                        break parsing;
                    }
                } else {
                    value = number.intValue();

                    if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }
                }
            }


                        boolean useDateFormatSymbols = useDateFormatSymbols();

            int index;
            switch (patternCharIndex) {
            case PATTERN_ERA: // 'G'
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.ERA, formatData.getEras(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_WEEK_YEAR: // 'Y'
            case PATTERN_YEAR:      // 'y'
                if (!(calendar instanceof GregorianCalendar)) {
                    // 日历可能有年份的文本表示，例如 JapaneseImperialCalendar 中的 "\u5143"。
                    int style = (count >= 4) ? Calendar.LONG : Calendar.SHORT;
                    Map<String, Integer> map = calendar.getDisplayNames(field, style, locale);
                    if (map != null) {
                        if ((index = matchString(text, start, field, map, calb)) > 0) {
                            return index;
                        }
                    }
                    calb.set(field, value);
                    return pos.index;
                }

                // 如果 YEAR 模式字符有 3 个或更多，这表示年份值应按字面值处理，不进行任何两位年份调整（例如，从 "01" 到 2001）。否则
                // 我们会进行调整，将两位年份放在正确的世纪内，解析的字符串从 "00" 到 "99"。任何其他字符串都按字面值处理： "2250", "-1", "1", "002"。
                if (count <= 2 && (pos.index - actualStart) == 2
                    && Character.isDigit(text.charAt(actualStart))
                    && Character.isDigit(text.charAt(actualStart + 1))) {
                    // 假设例如默认世纪开始是 6/18/1903。
                    // 这意味着两位年份将被强制到 6/18/1903 到 6/17/2003 的范围内。因此，年份 00, 01 和 02
                    // 对应于 2000, 2001 和 2002。年份 04, 05 等对应于 1904, 1905 等。如果年份是 03，则
                    // 如果其他字段指定的日期在 6/18 之前，则为 2003，否则为 1903。因此，03 是一个模糊的年份。所有其他
                    // 两位年份都是明确的。
                    int ambiguousTwoDigitYear = defaultCenturyStartYear % 100;
                    ambiguousYear[0] = value == ambiguousTwoDigitYear;
                    value += (defaultCenturyStartYear/100)*100 +
                        (value < ambiguousTwoDigitYear ? 100 : 0);
                }
                calb.set(field, value);
                return pos.index;

            case PATTERN_MONTH: // 'M'
                if (count <= 2) // 即，M 或 MM。
                {
                    // 如果模式使用数字样式：M 或 MM，则不想解析月份字符串。
                    // [我们在上面计算了 'value'。]
                    calb.set(Calendar.MONTH, value - 1);
                    return pos.index;
                }

                if (useDateFormatSymbols) {
                    // count >= 3 // 即，MMM 或 MMMM
                    // 要能够解析短形式和长形式。
                    // 先尝试 count == 4：
                    int newStart;
                    if ((newStart = matchString(text, start, Calendar.MONTH,
                                                formatData.getMonths(), calb)) > 0) {
                        return newStart;
                    }
                    // count == 4 失败，现在尝试 count == 3
                    if ((index = matchString(text, start, Calendar.MONTH,
                                             formatData.getShortMonths(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String, Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_HOUR_OF_DAY1: // 'k' 1-based.  例如，23:59 + 1 小时 =>> 24:59
                if (!isLenient()) {
                    // 在非宽松模式下验证小时值
                    if (value < 1 || value > 24) {
                        break parsing;
                    }
                }
                // [我们在上面计算了 'value'。]
                if (value == calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1) {
                    value = 0;
                }
                calb.set(Calendar.HOUR_OF_DAY, value);
                return pos.index;

            case PATTERN_DAY_OF_WEEK:  // 'E'
                {
                    if (useDateFormatSymbols) {
                        // 要能够解析短形式和长形式。
                        // 先尝试 count == 4 (DDDD)：
                        int newStart;
                        if ((newStart=matchString(text, start, Calendar.DAY_OF_WEEK,
                                                  formatData.getWeekdays(), calb)) > 0) {
                            return newStart;
                        }
                        // DDDD 失败，现在尝试 DDD
                        if ((index = matchString(text, start, Calendar.DAY_OF_WEEK,
                                                 formatData.getShortWeekdays(), calb)) > 0) {
                            return index;
                        }
                    } else {
                        int[] styles = { Calendar.LONG, Calendar.SHORT };
                        for (int style : styles) {
                            Map<String,Integer> map = calendar.getDisplayNames(field, style, locale);
                            if ((index = matchString(text, start, field, map, calb)) > 0) {
                                return index;
                            }
                        }
                    }
                }
                break parsing;


                        case PATTERN_AM_PM:    // 'a'
                if (useDateFormatSymbols) {
                    if ((index = matchString(text, start, Calendar.AM_PM,
                                             formatData.getAmPmStrings(), calb)) > 0) {
                        return index;
                    }
                } else {
                    Map<String,Integer> map = getDisplayNamesMap(field, locale);
                    if ((index = matchString(text, start, field, map, calb)) > 0) {
                        return index;
                    }
                }
                break parsing;

            case PATTERN_HOUR1: // 'h' 1-based.  eg, 11PM + 1 hour =>> 12 AM
                if (!isLenient()) {
                    // Validate the hour value in non-lenient
                    if (value < 1 || value > 12) {
                        break parsing;
                    }
                }
                // [We computed 'value' above.]
                if (value == calendar.getLeastMaximum(Calendar.HOUR) + 1) {
                    value = 0;
                }
                calb.set(Calendar.HOUR, value);
                return pos.index;

            case PATTERN_ZONE_NAME:  // 'z'
            case PATTERN_ZONE_VALUE: // 'Z'
                {
                    int sign = 0;
                    try {
                        char c = text.charAt(pos.index);
                        if (c == '+') {
                            sign = 1;
                        } else if (c == '-') {
                            sign = -1;
                        }
                        if (sign == 0) {
                            // Try parsing a custom time zone "GMT+hh:mm" or "GMT".
                            if ((c == 'G' || c == 'g')
                                && (text.length() - start) >= GMT.length()
                                && text.regionMatches(true, start, GMT, 0, GMT.length())) {
                                pos.index = start + GMT.length();

                                if ((text.length() - pos.index) > 0) {
                                    c = text.charAt(pos.index);
                                    if (c == '+') {
                                        sign = 1;
                                    } else if (c == '-') {
                                        sign = -1;
                                    }
                                }

                                if (sign == 0) {    /* "GMT" without offset */
                                    calb.set(Calendar.ZONE_OFFSET, 0)
                                        .set(Calendar.DST_OFFSET, 0);
                                    return pos.index;
                                }

                                // Parse the rest as "hh:mm"
                                int i = subParseNumericZone(text, ++pos.index,
                                                            sign, 0, true, calb);
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            } else {
                                // Try parsing the text as a time zone
                                // name or abbreviation.
                                int i = subParseZoneString(text, pos.index, calb);
                                if (i > 0) {
                                    return i;
                                }
                                pos.index = -i;
                            }
                        } else {
                            // Parse the rest as "hhmm" (RFC 822)
                            int i = subParseNumericZone(text, ++pos.index,
                                                        sign, 0, false, calb);
                            if (i > 0) {
                                return i;
                            }
                            pos.index = -i;
                        }
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                break parsing;

            case PATTERN_ISO_ZONE:   // 'X'
                {
                    if ((text.length() - pos.index) <= 0) {
                        break parsing;
                    }

                    int sign;
                    char c = text.charAt(pos.index);
                    if (c == 'Z') {
                        calb.set(Calendar.ZONE_OFFSET, 0).set(Calendar.DST_OFFSET, 0);
                        return ++pos.index;
                    }

                    // parse text as "+/-hh[[:]mm]" based on count
                    if (c == '+') {
                        sign = 1;
                    } else if (c == '-') {
                        sign = -1;
                    } else {
                        ++pos.index;
                        break parsing;
                    }
                    int i = subParseNumericZone(text, ++pos.index, sign, count,
                                                count == 3, calb);
                    if (i > 0) {
                        return i;
                    }
                    pos.index = -i;
                }
                break parsing;

            default:
         // case PATTERN_DAY_OF_MONTH:         // 'd'
         // case PATTERN_HOUR_OF_DAY0:         // 'H' 0-based.  eg, 23:59 + 1 hour =>> 00:59
         // case PATTERN_MINUTE:               // 'm'
         // case PATTERN_SECOND:               // 's'
         // case PATTERN_MILLISECOND:          // 'S'
         // case PATTERN_DAY_OF_YEAR:          // 'D'
         // case PATTERN_DAY_OF_WEEK_IN_MONTH: // 'F'
         // case PATTERN_WEEK_OF_YEAR:         // 'w'
         // case PATTERN_WEEK_OF_MONTH:        // 'W'
         // case PATTERN_HOUR0:                // 'K' 0-based.  eg, 11PM + 1 hour =>> 0 AM
         // case PATTERN_ISO_DAY_OF_WEEK:      // 'u' (pseudo field);

                // Handle "generic" fields
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        break parsing;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number != null) {
                    value = number.intValue();


                                if (useFollowingMinusSignAsDelimiter && (value < 0) &&
                        (((pos.index < text.length()) &&
                         (text.charAt(pos.index) != minusSign)) ||
                         ((pos.index == text.length()) &&
                          (text.charAt(pos.index-1) == minusSign)))) {
                        value = -value;
                        pos.index--;
                    }

                    calb.set(field, value);
                    return pos.index;
                }
                break parsing;
            }
        }

        // 解析失败。
        origPos.errorIndex = pos.index;
        return -1;
    }

    /**
     * 如果显式设置了 DateFormatSymbols 或 locale 为 null，则返回 true。
     */
    private boolean useDateFormatSymbols() {
        return useDateFormatSymbols || locale == null;
    }

    /**
     * 翻译模式，将 from 字符串中的每个字符映射到 to 字符串中的相应字符。
     *
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    private String translatePattern(String pattern, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (inQuote) {
                if (c == '\'') {
                    inQuote = false;
                }
            }
            else {
                if (c == '\'') {
                    inQuote = true;
                } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    int ci = from.indexOf(c);
                    if (ci >= 0) {
                        // patternChars 由于序列化兼容性比 localPatternChars 更长。localPatternChars 不支持的模式字母会直接通过。
                        if (ci < to.length()) {
                            c = to.charAt(ci);
                        }
                    } else {
                        throw new IllegalArgumentException("非法模式 " +
                                                           "字符 '" +
                                                           c + "'");
                    }
                }
            }
            result.append(c);
        }
        if (inQuote) {
            throw new IllegalArgumentException("模式中的未完成引号");
        }
        return result.toString();
    }

    /**
     * 返回描述此日期格式的模式字符串。
     *
     * @return 描述此日期格式的模式字符串。
     */
    public String toPattern() {
        return pattern;
    }

    /**
     * 返回描述此日期格式的本地化模式字符串。
     *
     * @return 描述此日期格式的本地化模式字符串。
     */
    public String toLocalizedPattern() {
        return translatePattern(pattern,
                                DateFormatSymbols.patternChars,
                                formatData.getLocalPatternChars());
    }

    /**
     * 将给定的模式字符串应用到此日期格式。
     *
     * @param pattern 此日期格式的新日期和时间模式
     * @exception NullPointerException 如果给定的模式为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    public void applyPattern(String pattern)
    {
        applyPatternImpl(pattern);
    }

    private void applyPatternImpl(String pattern) {
        compiledPattern = compile(pattern);
        this.pattern = pattern;
    }

    /**
     * 将给定的本地化模式字符串应用到此日期格式。
     *
     * @param pattern 要映射到此格式的新日期和时间格式模式的字符串
     * @exception NullPointerException 如果给定的模式为 null
     * @exception IllegalArgumentException 如果给定的模式无效
     */
    public void applyLocalizedPattern(String pattern) {
         String p = translatePattern(pattern,
                                     formatData.getLocalPatternChars(),
                                     DateFormatSymbols.patternChars);
         compiledPattern = compile(p);
         this.pattern = p;
    }

    /**
     * 获取此日期格式的日期和时间格式符号的副本。
     *
     * @return 此日期格式的日期和时间格式符号
     * @see #setDateFormatSymbols
     */
    public DateFormatSymbols getDateFormatSymbols()
    {
        return (DateFormatSymbols)formatData.clone();
    }

    /**
     * 设置此日期格式的日期和时间格式符号。
     *
     * @param newFormatSymbols 新的日期和时间格式符号
     * @exception NullPointerException 如果给定的 newFormatSymbols 为 null
     * @see #getDateFormatSymbols
     */
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols)
    {
        this.formatData = (DateFormatSymbols)newFormatSymbols.clone();
        useDateFormatSymbols = true;
    }

    /**
     * 创建此 <code>SimpleDateFormat</code> 的副本。这也会克隆格式的日期格式符号。
     *
     * @return 此 <code>SimpleDateFormat</code> 的克隆
     */
    @Override
    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) formatData.clone();
        return other;
    }

    /**
     * 返回此 <code>SimpleDateFormat</code> 对象的哈希码值。
     *
     * @return 此 <code>SimpleDateFormat</code> 对象的哈希码值。
     */
    @Override
    public int hashCode()
    {
        return pattern.hashCode();
        // 足够的字段以获得合理的分布
    }

    /**
     * 将给定的对象与此 <code>SimpleDateFormat</code> 进行比较以确定是否相等。
     *
     * @return 如果给定的对象与此 <code>SimpleDateFormat</code> 相等，则返回 true
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!super.equals(obj)) {
            return false; // 超类进行类检查
        }
        SimpleDateFormat that = (SimpleDateFormat) obj;
        return (pattern.equals(that.pattern)
                && formatData.equals(that.formatData));
    }


                private static final int[] REST_OF_STYLES = {
        Calendar.SHORT_STANDALONE, Calendar.LONG_FORMAT, Calendar.LONG_STANDALONE,
    };
    private Map<String, Integer> getDisplayNamesMap(int field, Locale locale) {
        Map<String, Integer> map = calendar.getDisplayNames(field, Calendar.SHORT_FORMAT, locale);
        // 获取所有 SHORT 和 LONG 样式（避免 NARROW 样式）。
        for (int style : REST_OF_STYLES) {
            Map<String, Integer> m = calendar.getDisplayNames(field, style, locale);
            if (m != null) {
                map.putAll(m);
            }
        }
        return map;
    }

    /**
     * 从输入流中读取对象后，验证对象中的格式模式。
     * <p>
     * @exception InvalidObjectException 如果模式无效
     */
    private void readObject(ObjectInputStream stream)
                         throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        try {
            compiledPattern = compile(pattern);
        } catch (Exception e) {
            throw new InvalidObjectException("无效的模式");
        }

        if (serialVersionOnStream < 1) {
            // 没有 defaultCenturyStart 字段
            initializeDefaultCentury();
        }
        else {
            // 填充依赖的瞬态字段
            parseAmbiguousDatesAsAfter(defaultCenturyStart);
        }
        serialVersionOnStream = currentSerialVersion;

        // 如果反序列化的对象有一个 SimpleTimeZone，尝试
        // 用 ZoneInfo 的等效对象替换它，以尽可能与
        // SimpleTimeZone 基础实现兼容。
        TimeZone tz = getTimeZone();
        if (tz instanceof SimpleTimeZone) {
            String id = tz.getID();
            TimeZone zi = TimeZone.getTimeZone(id);
            if (zi != null && zi.hasSameRules(tz) && zi.getID().equals(id)) {
                setTimeZone(zi);
            }
        }
    }

    /**
     * 分析 DecimalFormat 的负数子模式并根据需要设置/更新值。
     */
    private void checkNegativeNumberExpression() {
        if ((numberFormat instanceof DecimalFormat) &&
            !numberFormat.equals(originalNumberFormat)) {
            String numberPattern = ((DecimalFormat)numberFormat).toPattern();
            if (!numberPattern.equals(originalNumberPattern)) {
                hasFollowingMinusSign = false;

                int separatorIndex = numberPattern.indexOf(';');
                // 如果负数子模式不为空，我们需要分析
                // 它以检查它是否有跟随的减号。
                if (separatorIndex > -1) {
                    int minusIndex = numberPattern.indexOf('-', separatorIndex);
                    if ((minusIndex > numberPattern.lastIndexOf('0')) &&
                        (minusIndex > numberPattern.lastIndexOf('#'))) {
                        hasFollowingMinusSign = true;
                        minusSign = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getMinusSign();
                    }
                }
                originalNumberPattern = numberPattern;
            }
            originalNumberFormat = numberFormat;
        }
    }

}
