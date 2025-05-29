
/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.spi;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/**
 * 一个抽象类，用于提供 {@code Calendar} 字段值的本地化字符串表示（显示名称）的服务提供者。
 *
 * <p><a name="calendartypes"><b>日历类型</b></a>
 *
 * <p>日历类型用于指定 {@link
 * #getDisplayName(String, int, int, int, Locale) getDisplayName} 和 {@link
 * #getDisplayNames(String, int, int, Locale) getDisplayNames} 方法提供日历字段值名称的日历系统。详情请参见 {@link Calendar#getCalendarType()}。
 *
 * <p><b>日历字段</b>
 *
 * <p>日历字段使用 {@link
 * Calendar} 中定义的常量指定。以下是每个日历系统需要支持的日历通用字段及其值。
 *
 * <table style="border-bottom:1px solid" border="1" cellpadding="3" cellspacing="0" summary="字段值">
 *   <tr>
 *     <th>字段</th>
 *     <th>值</th>
 *     <th>描述</th>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link Calendar#MONTH}</td>
 *     <td valign="top">{@link Calendar#JANUARY} 到 {@link Calendar#UNDECIMBER}</td>
 *     <td>月份编号从 0 开始（例如，0 - 一月，...，11 - 十二月）。某些日历系统有 13 个月。如果支持的地区需要，月份名称需要在格式化和独立形式中都支持。如果两种形式没有区别，应返回相同的名称。</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link Calendar#DAY_OF_WEEK}</td>
 *     <td valign="top">{@link Calendar#SUNDAY} 到 {@link Calendar#SATURDAY}</td>
 *     <td>星期几的编号从 1 开始，从星期日开始（即，1 - 星期日，...，7 - 星期六）。</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link Calendar#AM_PM}</td>
 *     <td valign="top">{@link Calendar#AM} 到 {@link Calendar#PM}</td>
 *     <td>0 - 上午，1 - 下午</td>
 *   </tr>
 * </table>
 *
 * <p style="margin-top:20px">以下是需要支持的日历特定字段及其值。
 *
 * <table style="border-bottom:1px solid" border="1" cellpadding="3" cellspacing="0" summary="日历类型和字段值">
 *   <tr>
 *     <th>日历类型</th>
 *     <th>字段</th>
 *     <th>值</th>
 *     <th>描述</th>
 *   </tr>
 *   <tr>
 *     <td rowspan="2" valign="top">{@code "gregory"}</td>
 *     <td rowspan="2" valign="top">{@link Calendar#ERA}</td>
 *     <td>0</td>
 *     <td>{@link java.util.GregorianCalendar#BC} (BCE)</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>{@link java.util.GregorianCalendar#AD} (CE)</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="2" valign="top">{@code "buddhist"}</td>
 *     <td rowspan="2" valign="top">{@link Calendar#ERA}</td>
 *     <td>0</td>
 *     <td>BC (BCE)</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>B.E. (佛教纪元)</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="6" valign="top">{@code "japanese"}</td>
 *     <td rowspan="5" valign="top">{@link Calendar#ERA}</td>
 *     <td>0</td>
 *     <td>明治前 (Seireki)</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>明治 (Meiji)</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>大正 (Taisho)</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>昭和 (Showa)</td>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>平成 (Heisei)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link Calendar#YEAR}</td>
 *     <td>1</td>
 *     <td>每个时代的第一个年份。当指定长格式 ({@link Calendar#LONG_FORMAT} 或 {@link Calendar#LONG_STANDALONE}) 时应返回。有关更多信息，请参见 {@code SimpleDateFormat} 中的 <a href="../../text/SimpleDateFormat.html#year">年份表示</a>。</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="2" valign="top">{@code "roc"}</td>
 *     <td rowspan="2" valign="top">{@link Calendar#ERA}</td>
 *     <td>0</td>
 *     <td>民国前</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>民国 (R.O.C.)</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="2" valign="top">{@code "islamic"}</td>
 *     <td rowspan="2" valign="top">{@link Calendar#ERA}</td>
 *     <td>0</td>
 *     <td>AH 前</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>希吉拉年 (AH)</td>
 *   </tr>
 * </table>
 *
 * <p>“gregory”日历的字段值名称必须与 {@link java.text.spi.DateFormatSymbolsProvider} 提供的日期时间符号一致。
 *
 * <p>时区名称由 {@link TimeZoneNameProvider} 支持。
 *
 * @author Masayoshi Okutsu
 * @since 1.8
 * @see CalendarDataProvider
 * @see Locale#getUnicodeLocaleType(String)
 */
public abstract class CalendarNameProvider extends LocaleServiceProvider {
    /**
     * 唯一的构造函数。（通常由子类构造函数隐式调用。）
     */
    protected CalendarNameProvider() {
    }

    /**
     * 返回给定 <code>style</code> 和 <code>locale</code> 下日历 <code>field value</code> 的字符串表示（显示名称）。如果没有适用的字符串表示，则返回 <code>null</code>。
     *
     * <p>{@code field} 是一个 {@code Calendar} 字段索引，例如 {@link
     * Calendar#MONTH}。时区字段 {@link Calendar#ZONE_OFFSET} 和
     * {@link Calendar#DST_OFFSET} <em>不</em> 被此方法支持。如果指定了任何时区字段，必须返回 {@code null}。
     *
     * <p>{@code value} 是 {@code field} 值的数字表示。例如，如果 {@code field} 是 {@link Calendar#DAY_OF_WEEK}，则有效的值是 {@link Calendar#SUNDAY} 到 {@link Calendar#SATURDAY}
     * （包括）。
     *
     * <p>{@code style} 给出字符串表示的样式。它是 {@link Calendar#SHORT_FORMAT} ({@link Calendar#SHORT SHORT})、
     * {@link Calendar#SHORT_STANDALONE}、{@link Calendar#LONG_FORMAT}
     * ({@link Calendar#LONG LONG})、{@link Calendar#LONG_STANDALONE}、
     * {@link Calendar#NARROW_FORMAT} 或 {@link Calendar#NARROW_STANDALONE} 之一。
     *
     * <p>例如，以下调用将返回 {@code "Sunday"}。
     * <pre>
     * getDisplayName("gregory", Calendar.DAY_OF_WEEK, Calendar.SUNDAY,
     *                Calendar.LONG_STANDALONE, Locale.ENGLISH);
     * </pre>
     *
     * @param calendarType
     *              日历类型。（给定的 {@code locale} 中的任何日历类型将被忽略。）
     * @param field
     *              {@code Calendar} 字段索引，例如 {@link Calendar#DAY_OF_WEEK}
     * @param value
     *              {@code Calendar field} 的值，例如 {@link Calendar#MONDAY}
     * @param style
     *              字符串表示的样式：{@link
     *              Calendar#SHORT_FORMAT} ({@link Calendar#SHORT SHORT})、
     *              {@link Calendar#SHORT_STANDALONE}、{@link
     *              Calendar#LONG_FORMAT} ({@link Calendar#LONG LONG})、
     *              {@link Calendar#LONG_STANDALONE}、
     *              {@link Calendar#NARROW_FORMAT} 或
     *              {@link Calendar#NARROW_STANDALONE} 之一。
     * @param locale
     *              所需的地区
     * @return {@code field value} 的字符串表示，如果字符串表示不适用或给定的日历类型未知，则返回 {@code
     *         null}
     * @throws IllegalArgumentException
     *         如果 {@code field} 或 {@code style} 无效
     * @throws NullPointerException 如果 {@code locale} 为 {@code null}
     * @see TimeZoneNameProvider
     * @see java.util.Calendar#get(int)
     * @see java.util.Calendar#getDisplayName(int, int, Locale)
     */
    public abstract String getDisplayName(String calendarType,
                                          int field, int value,
                                          int style, Locale locale);


                /**
     * 返回一个包含给定 {@code style} 和 {@code locale} 下 {@code Calendar} {@code field} 的所有字符串表示（显示名称）
     * 及其对应字段值的 {@code Map}。
     *
     * <p>{@code field} 是一个 {@code Calendar} 字段索引，例如 {@link
     * Calendar#MONTH}。时间区域字段，如 {@link Calendar#ZONE_OFFSET} 和
     * {@link Calendar#DST_OFFSET}，不被此方法支持。如果指定了任何时间区域字段，则必须返回 {@code null}。
     *
     * <p>{@code style} 给出字符串表示的样式。它必须是 {@link Calendar#ALL_STYLES}，{@link Calendar#SHORT_FORMAT} ({@link
     * Calendar#SHORT SHORT})，{@link Calendar#SHORT_STANDALONE}，{@link
     * Calendar#LONG_FORMAT} ({@link Calendar#LONG LONG})，{@link
     * Calendar#LONG_STANDALONE}，{@link Calendar#NARROW_FORMAT} 或
     * {@link Calendar#NARROW_STANDALONE} 之一。注意，由于使用单个字符（例如“S”同时表示星期日和星期六），窄名称可能
     * 不唯一，在这种情况下，不会包含任何窄名称。
     *
     * <p>例如，以下调用将返回一个包含 {@code "January"} 对应 {@link Calendar#JANUARY}，{@code "Jan"} 对应 {@link
     * Calendar#JANUARY}，{@code "February"} 对应 {@link Calendar#FEBRUARY}，
     * {@code "Feb"} 对应 {@link Calendar#FEBRUARY} 等的 {@code Map}。
     * <pre>
     * getDisplayNames("gregory", Calendar.MONTH, Calendar.ALL_STYLES, Locale.ENGLISH);
     * </pre>
     *
     * @param calendarType
     *              日历类型。（给定的 {@code locale} 中的任何日历类型将被忽略。）
     * @param field
     *              返回显示名称的日历字段
     * @param style
     *              应用于显示名称的样式；必须是
     *              {@link Calendar#ALL_STYLES}，{@link Calendar#SHORT_FORMAT}
     *              ({@link Calendar#SHORT SHORT})，{@link
     *              Calendar#SHORT_STANDALONE}，{@link Calendar#LONG_FORMAT}
     *              ({@link Calendar#LONG LONG})，{@link Calendar#LONG_STANDALONE}，
     *              {@link Calendar#NARROW_FORMAT}，
     *              或 {@link Calendar#NARROW_STANDALONE} 之一
     * @param locale
     *              所需的区域设置
     * @return 包含 {@code field} 在 {@code style} 和 {@code locale} 下的所有显示名称及其 {@code field} 值的 {@code Map}，
     *         如果没有为 {@code field} 定义显示名称，则返回 {@code null}
     * @throws NullPointerException
     *         如果 {@code locale} 为 {@code null}
     * @see Calendar#getDisplayNames(int, int, Locale)
     */
    public abstract Map<String, Integer> getDisplayNames(String calendarType,
                                                         int field, int style,
                                                         Locale locale);
}
