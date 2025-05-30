
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
 * (C) Copyright Taligent, Inc. 1996-1998 - All Rights Reserved
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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.BuddhistCalendar;
import sun.util.calendar.ZoneInfo;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.spi.CalendarProvider;

/**
 * <code>Calendar</code> 类是一个抽象类，提供了将特定时间点转换为一组 {@link
 * #fields 日历字段}（如 <code>YEAR</code>、<code>MONTH</code>、<code>DAY_OF_MONTH</code>、<code>HOUR</code> 等）的方法，以及操作这些日历字段的方法，例如获取下一周的日期。一个时间点可以通过一个从 <a name="Epoch"><em>纪元</em></a>（1970年1月1日 00:00:00.000 GMT（格里高利历））开始的毫秒值来表示。
 *
 * <p>该类还提供了实现包外具体日历系统的附加字段和方法。这些字段和方法被定义为 <code>protected</code>。
 *
 * <p>
 * 与其他地区敏感类一样，<code>Calendar</code> 提供了一个类方法 <code>getInstance</code>，用于获取这种类型的通用对象。<code>Calendar</code> 的 <code>getInstance</code> 方法返回一个 <code>Calendar</code> 对象，其日历字段已使用当前日期和时间初始化：
 * <blockquote>
 * <pre>
 *     Calendar rightNow = Calendar.getInstance();
 * </pre>
 * </blockquote>
 *
 * <p><code>Calendar</code> 对象可以生成实现特定语言和日历风格（例如，日本-格里高利历、日本-传统历）的日期-时间格式所需的所有日历字段值。<code>Calendar</code> 定义了某些日历字段的值范围及其含义。例如，日历系统的第一月的值为 <code>MONTH == JANUARY</code>。其他值由具体子类定义，如 <code>ERA</code>。具体字段文档和子类文档中提供了详细信息。
 *
 * <h3>获取和设置日历字段值</h3>
 *
 * <p>可以通过调用 <code>set</code> 方法来设置日历字段值。在 <code>Calendar</code> 需要计算其时间值（从纪元开始的毫秒数）或日历字段值时，将解释设置在 <code>Calendar</code> 中的所有字段值。调用 <code>get</code>、<code>getTimeInMillis</code>、<code>getTime</code>、<code>add</code> 和 <code>roll</code> 时会涉及此类计算。
 *
 * <h4>宽容模式</h4>
 *
 * <p><code>Calendar</code> 有两种解释日历字段的模式：<em>宽容模式</em> 和 <em>非宽容模式</em>。当 <code>Calendar</code> 处于宽容模式时，它可以接受比它生成的范围更宽的日历字段值。当 <code>Calendar</code> 重新计算日历字段值以供 <code>get()</code> 返回时，所有日历字段都会被规范化。例如，一个宽容的 <code>GregorianCalendar</code> 会将 <code>MONTH == JANUARY</code>、<code>DAY_OF_MONTH == 32</code> 解释为2月1日。
 *
 * <p>当 <code>Calendar</code> 处于非宽容模式时，如果其日历字段有任何不一致，它将抛出异常。例如，<code>GregorianCalendar</code> 始终生成 <code>DAY_OF_MONTH</code> 值在1到该月天数之间。如果设置了任何超出范围的字段值，非宽容的 <code>GregorianCalendar</code> 在计算其时间或日历字段值时将抛出异常。
 *
 * <h4><a name="first_week">第一周</a></h4>
 *
 * <code>Calendar</code> 使用两个参数定义特定于地区的七天周：一周的第一天和第一周的最小天数（从1到7）。这些数字在构造 <code>Calendar</code> 时从地区资源数据中获取。也可以通过设置其值的方法显式指定。
 *
 * <p>在设置或获取 <code>WEEK_OF_MONTH</code> 或 <code>WEEK_OF_YEAR</code> 字段时，<code>Calendar</code> 必须确定月份或年份的第一周作为参考点。月份或年份的第一周定义为最早开始于 <code>getFirstDayOfWeek()</code> 并包含至少 <code>getMinimalDaysInFirstWeek()</code> 天的七天期。周编号 ..., -1, 0 在第一周之前；周编号 2, 3,... 在第一周之后。请注意，<code>get()</code> 返回的规范化编号可能不同。例如，某些 <code>Calendar</code> 子类可能将一年中第1周之前的周指定为前一年的第 <code><i>n</i></code> 周。
 *
 * <h4>日历字段解析</h4>
 *
 * 在从日历字段计算日期和时间时，可能缺少计算所需的信息（如只有年和月而没有日），或者可能存在不一致的信息（如1996年7月15日（格里高利历）是星期二，而不是星期一）。<code>Calendar</code> 将按照以下方式解析日历字段值以确定日期和时间。
 *
 * <p><a name="resolution">如果日历字段值之间存在冲突，<code>Calendar</code> 会优先考虑最近设置的字段。</a> 以下是日历字段的默认组合。最常用的组合由最近设置的单个字段确定。
 *
 * <p><a name="date_resolution">对于日期字段</a>：
 * <blockquote>
 * <pre>
 * YEAR + MONTH + DAY_OF_MONTH
 * YEAR + MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
 * YEAR + MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
 * YEAR + DAY_OF_YEAR
 * YEAR + DAY_OF_WEEK + WEEK_OF_YEAR
 * </pre></blockquote>
 *
 * <a name="time_resolution">对于时间字段</a>：
 * <blockquote>
 * <pre>
 * HOUR_OF_DAY
 * AM_PM + HOUR
 * </pre></blockquote>
 *
 * <p>如果在选定的字段组合中，某些日历字段的值未设置，<code>Calendar</code> 将使用它们的默认值。每个字段的默认值可能因具体的日历系统而异。例如，在 <code>GregorianCalendar</code> 中，字段的默认值与纪元开始时相同：即 <code>YEAR = 1970</code>，<code>MONTH = JANUARY</code>，<code>DAY_OF_MONTH = 1</code> 等。
 *
 * <p>
 * <strong>注意：</strong> 某些特定时间的解释可能存在某些可能的模糊性，这些模糊性按以下方式解决：
 * <ol>
 *     <li> 23:59 是一天的最后一分钟，00:00 是下一天的第一分钟。因此，1999年12月31日 23:59 < 2000年1月1日 00:00 < 2000年1月1日 00:01。
 *
 *     <li> 尽管历史上不精确，午夜也属于 "am"，中午属于 "pm"，因此在同一天，
 *          12:00 am（午夜）< 12:01 am，12:00 pm（中午）< 12:01 pm
 * </ol>
 *
 * <p>
 * 日期或时间格式字符串不是日历定义的一部分，因为这些必须在运行时可由用户修改或覆盖。使用 {@link DateFormat}
 * 来格式化日期。
 *
 * <h4>字段操作</h4>
 *
 * 可以使用三种方法更改日历字段：<code>set()</code>、<code>add()</code> 和 <code>roll()</code>。
 *
 * <p><strong><code>set(f, value)</code></strong> 将日历字段 <code>f</code> 更改为 <code>value</code>。此外，它设置一个内部成员变量，以指示日历字段 <code>f</code> 已更改。尽管日历字段 <code>f</code> 立即更改，但日历的时间值（毫秒）不会在下一次调用 <code>get()</code>、<code>getTime()</code>、<code>getTimeInMillis()</code>、<code>add()</code> 或 <code>roll()</code> 之前重新计算。因此，多次调用 <code>set()</code> 不会触发多次不必要的计算。由于使用 <code>set()</code> 更改了日历字段，其他日历字段也可能更改，具体取决于日历字段、日历字段值和日历系统。此外，<code>get(f)</code> 在重新计算日历字段后不一定返回 <code>set</code> 方法设置的 <code>value</code>。具体细节由具体的日历类确定。</p>
 *
 * <p><em>示例</em>：考虑一个最初设置为1999年8月31日的 <code>GregorianCalendar</code>。调用 <code>set(Calendar.MONTH, Calendar.SEPTEMBER)</code> 将日期设置为1999年9月31日。这是一个临时的内部表示，如果调用 <code>getTime()</code>，则解析为1999年10月1日。然而，在调用 <code>getTime()</code> 之前调用 <code>set(Calendar.DAY_OF_MONTH, 30)</code> 将日期设置为1999年9月30日，因为 <code>set()</code> 本身不会触发重新计算。</p>
 *
 * <p><strong><code>add(f, delta)</code></strong> 将 <code>delta</code> 添加到字段 <code>f</code>。这相当于调用 <code>set(f, get(f) + delta)</code>，但有两个调整：</p>
 *
 * <blockquote>
 *   <p><strong>添加规则1</strong>. 调用后字段 <code>f</code> 的值减去调用前字段 <code>f</code> 的值等于 <code>delta</code>，模以字段 <code>f</code> 中发生的任何溢出。当字段值超出其范围时，会发生溢出，结果是更大的字段被递增或递减，字段值被调整回其范围内。</p>
 *
 *   <p><strong>添加规则2</strong>. 如果期望较小的字段不变，但由于字段 <code>f</code> 更改后其最小值或最大值的变化或其他约束（如时区偏移变化）而无法等于其先前值，则其值将调整为尽可能接近其期望值。较小的字段表示较小的时间单位。<code>HOUR</code> 是比 <code>DAY_OF_MONTH</code> 更小的字段。不调整不期望不变的较小字段。日历系统确定哪些字段期望不变。</p>
 * </blockquote>
 *
 * <p>此外，与 <code>set()</code> 不同，<code>add()</code> 强制立即重新计算日历的毫秒数和所有字段。</p>
 *
 * <p><em>示例</em>：考虑一个最初设置为1999年8月31日的 <code>GregorianCalendar</code>。调用 <code>add(Calendar.MONTH, 13)</code> 将日历设置为2000年9月30日。<strong>添加规则1</strong> 将 <code>MONTH</code> 字段设置为9月，因为将13个月加到8月会得到下一年的9月。由于 <code>DAY_OF_MONTH</code> 在 <code>GregorianCalendar</code> 中的9月不能为31，<strong>添加规则2</strong> 将 <code>DAY_OF_MONTH</code> 设置为30，这是最接近的可能值。尽管 <code>DAY_OF_WEEK</code> 是一个较小的字段，但规则2不会调整它，因为在 <code>GregorianCalendar</code> 中，当月更改时，<code>DAY_OF_WEEK</code> 预期会更改。</p>
 *
 * <p><strong><code>roll(f, delta)</code></strong> 将 <code>delta</code> 添加到字段 <code>f</code> 而不更改更大的字段。这相当于调用 <code>add(f, delta)</code>，但有以下调整：</p>
 *
 * <blockquote>
 *   <p><strong>滚动规则</strong>. 更大的字段在调用后保持不变。更大的字段表示更大的时间单位。<code>DAY_OF_MONTH</code> 是比 <code>HOUR</code> 更大的字段。</p>
 * </blockquote>
 *
 * <p><em>示例</em>：参见 {@link java.util.GregorianCalendar#roll(int, int)}。
 *
 * <p><strong>使用模型</strong>. 为了激发 <code>add()</code> 和 <code>roll()</code> 的行为，考虑一个具有月份、日期和年份增量和减量按钮的用户界面组件，以及一个底层的 <code>GregorianCalendar</code>。如果界面显示1999年1月31日，用户按下月份增量按钮，它应该显示什么？如果底层实现使用 <code>set()</code>，它可能会显示1999年3月3日。一个更好的结果应该是1999年2月28日。此外，如果用户再次按下月份增量按钮，它应该显示1999年3月31日，而不是1999年3月28日。通过保存原始日期并根据是否应影响更大的字段使用 <code>add()</code> 或 <code>roll()</code>，用户界面可以按照大多数用户的直观预期运行。</p>
 *
 * @see          java.lang.System#currentTimeMillis()
 * @see          Date
 * @see          GregorianCalendar
 * @see          TimeZone
 * @see          java.text.DateFormat
 * @author Mark Davis, David Goldsmith, Chen-Lieh Huang, Alan Liu
 * @since JDK1.1
 */
public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {


                // Data flow in Calendar
    // ---------------------

    // 当前时间由 Calendar 以两种方式表示：自纪元（1970年1月1日 0:00 UTC）以来的 UTC 毫秒数，以及本地字段，如 MONTH、HOUR、AM_PM 等。可以计算毫秒数和字段之间的转换。这种转换所需的数据由 Calendar 拥有的 TimeZone 对象封装。TimeZone 对象提供的数据也可能被用户直接设置的 ZONE_OFFSET 和/或 DST_OFFSET 字段覆盖。该类跟踪调用者最近设置的信息，并根据需要计算任何其他信息。

    // 如果用户使用 set() 设置字段，数据流如下。这由 Calendar 子类的 computeTime() 方法实现。在此过程中，某些字段可能被忽略。用于解决应关注哪些字段的歧义算法在类文档中描述。

    //   本地字段（YEAR, MONTH, DATE, HOUR, MINUTE, 等）
    //           |
    //           | 使用 Calendar 特定算法
    //           V
    //   本地标准毫秒数
    //           |
    //           | 使用 TimeZone 或用户设置的 ZONE_OFFSET / DST_OFFSET
    //           V
    //   UTC 毫秒数（在 time 数据成员中）

    // 如果用户使用 setTime() 或 setTimeInMillis() 设置 UTC 毫秒数，数据流如下。这由 Calendar 子类的 computeFields() 方法实现。

    //   UTC 毫秒数（在 time 数据成员中）
    //           |
    //           | 使用 TimeZone getOffset()
    //           V
    //   本地标准毫秒数
    //           |
    //           | 使用 Calendar 特定算法
    //           V
    //   本地字段（YEAR, MONTH, DATE, HOUR, MINUTE, 等）

    // 通常，当必要时，会从字段，通过本地和 UTC 毫秒数，再回到字段进行往返。这由 complete() 方法实现。将部分字段解析为 UTC 毫秒值，允许从该值生成所有剩余字段。如果 Calendar 是宽松的，字段在重新生成时也会被归一化到标准范围。

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示纪元，例如 Julian 日历中的 AD 或 BC。这是一个特定于日历的值；请参阅子类文档。
     *
     * @see GregorianCalendar#AD
     * @see GregorianCalendar#BC
     */
    public final static int ERA = 0;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示年份。这是一个特定于日历的值；请参阅子类文档。
     */
    public final static int YEAR = 1;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示月份。这是一个特定于日历的值。Gregorian 和 Julian 日历中的第一个月是 <code>JANUARY</code>，值为 0；最后一个月取决于一年中的月份数。
     *
     * @see #JANUARY
     * @see #FEBRUARY
     * @see #MARCH
     * @see #APRIL
     * @see #MAY
     * @see #JUNE
     * @see #JULY
     * @see #AUGUST
     * @see #SEPTEMBER
     * @see #OCTOBER
     * @see #NOVEMBER
     * @see #DECEMBER
     * @see #UNDECIMBER
     */
    public final static int MONTH = 2;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示当前年份内的周数。根据 <code>getFirstDayOfWeek()</code> 和 <code>getMinimalDaysInFirstWeek()</code> 定义的年份第一周的值为 1。子类定义年份第一周之前的天数的 <code>WEEK_OF_YEAR</code> 值。
     *
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public final static int WEEK_OF_YEAR = 3;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示当前月份内的周数。根据 <code>getFirstDayOfWeek()</code> 和 <code>getMinimalDaysInFirstWeek()</code> 定义的月份第一周的值为 1。子类定义月份第一周之前的天数的 <code>WEEK_OF_MONTH</code> 值。
     *
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public final static int WEEK_OF_MONTH = 4;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示月份中的日期。这是 <code>DAY_OF_MONTH</code> 的同义词。月份中的第一天值为 1。
     *
     * @see #DAY_OF_MONTH
     */
    public final static int DATE = 5;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示月份中的日期。这是 <code>DATE</code> 的同义词。月份中的第一天值为 1。
     *
     * @see #DATE
     */
    public final static int DAY_OF_MONTH = 5;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示当前年份中的天数。年份中的第一天值为 1。
     */
    public final static int DAY_OF_YEAR = 6;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示星期几。此字段的值为 <code>SUNDAY</code>、<code>MONDAY</code>、<code>TUESDAY</code>、<code>WEDNESDAY</code>、<code>THURSDAY</code>、<code>FRIDAY</code> 和 <code>SATURDAY</code>。
     *
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public final static int DAY_OF_WEEK = 7;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示当前月份内星期几的序号。与 <code>DAY_OF_WEEK</code> 字段一起，这唯一地指定了月份中的某一天。与 <code>WEEK_OF_MONTH</code> 和 <code>WEEK_OF_YEAR</code> 不同，此字段的值 <em>不</em> 依赖于 <code>getFirstDayOfWeek()</code> 或 <code>getMinimalDaysInFirstWeek()</code>。月份中的第 1 到 7 天总是对应于 <code>DAY_OF_WEEK_IN_MONTH 1</code>；第 8 到 14 天对应于 <code>DAY_OF_WEEK_IN_MONTH 2</code>，依此类推。<code>DAY_OF_WEEK_IN_MONTH 0</code> 表示 <code>DAY_OF_WEEK_IN_MONTH 1</code> 之前的那一周。负值从月份末尾倒数，因此月份的最后一个星期日表示为 <code>DAY_OF_WEEK = SUNDAY, DAY_OF_WEEK_IN_MONTH = -1</code>。因为负值倒数，它们通常在月份中的对齐方式与正值不同。例如，如果一个月有 31 天，<code>DAY_OF_WEEK_IN_MONTH -1</code> 将与 <code>DAY_OF_WEEK_IN_MONTH 5</code> 和 <code>4</code> 的末尾重叠。
     *
     * @see #DAY_OF_WEEK
     * @see #WEEK_OF_MONTH
     */
    public final static int DAY_OF_WEEK_IN_MONTH = 8;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示 <code>HOUR</code> 是上午还是下午。例如，在 10:04:15.250 PM 时，<code>AM_PM</code> 是 <code>PM</code>。
     *
     * @see #AM
     * @see #PM
     * @see #HOUR
     */
    public final static int AM_PM = 9;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示上午或下午的小时数。<code>HOUR</code> 用于 12 小时制（0 - 11）。中午和午夜表示为 0，而不是 12。例如，在 10:04:15.250 PM 时，<code>HOUR</code> 是 10。
     *
     * @see #AM_PM
     * @see #HOUR_OF_DAY
     */
    public final static int HOUR = 10;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示一天中的小时数。<code>HOUR_OF_DAY</code> 用于 24 小时制。例如，在 10:04:15.250 PM 时，<code>HOUR_OF_DAY</code> 是 22。
     *
     * @see #HOUR
     */
    public final static int HOUR_OF_DAY = 11;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示分钟数。
     * 例如，在 10:04:15.250 PM 时，<code>MINUTE</code> 是 4。
     */
    public final static int MINUTE = 12;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示秒数。
     * 例如，在 10:04:15.250 PM 时，<code>SECOND</code> 是 15。
     */
    public final static int SECOND = 13;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示毫秒数。
     * 例如，在 10:04:15.250 PM 时，<code>MILLISECOND</code> 是 250。
     */
    public final static int MILLISECOND = 14;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示与 GMT 的原始偏移量（以毫秒为单位）。
     * <p>
     * 如果 <code>TimeZone</code> 实现子类支持历史 GMT 偏移量变化，此字段将反映此 <code>Calendar</code> 的时区的正确 GMT 偏移量值。
     */
    public final static int ZONE_OFFSET = 15;

    /**
     * 用于 <code>get</code> 和 <code>set</code> 的字段编号，表示夏令时偏移量（以毫秒为单位）。
     * <p>
     * 如果 <code>TimeZone</code> 实现子类支持历史夏令时计划变化，此字段将反映此 <code>Calendar</code> 的时区的正确夏令时偏移量值。
     */
    public final static int DST_OFFSET = 16;

    /**
     * 由 <code>get</code> 和 <code>set</code> 认识的字段数量。
     * 字段编号范围从 <code>0..FIELD_COUNT-1</code>。
     */
    public final static int FIELD_COUNT = 17;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期日。
     */
    public final static int SUNDAY = 1;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期一。
     */
    public final static int MONDAY = 2;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期二。
     */
    public final static int TUESDAY = 3;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期三。
     */
    public final static int WEDNESDAY = 4;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期四。
     */
    public final static int THURSDAY = 5;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期五。
     */
    public final static int FRIDAY = 6;

    /**
     * {@link #DAY_OF_WEEK} 字段的值，表示星期六。
     */
    public final static int SATURDAY = 7;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第一个月。
     */
    public final static int JANUARY = 0;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第二个月。
     */
    public final static int FEBRUARY = 1;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第三个月。
     */
    public final static int MARCH = 2;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第四个月。
     */
    public final static int APRIL = 3;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第五个月。
     */
    public final static int MAY = 4;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第六个月。
     */
    public final static int JUNE = 5;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第七个月。
     */
    public final static int JULY = 6;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第八个月。
     */
    public final static int AUGUST = 7;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第九个月。
     */
    public final static int SEPTEMBER = 8;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第十个月。
     */
    public final static int OCTOBER = 9;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第十一个月。
     */
    public final static int NOVEMBER = 10;

    /**
     * {@link #MONTH} 字段的值，表示 Gregorian 和 Julian 日历中的第十二个月。
     */
    public final static int DECEMBER = 11;

    /**
     * {@link #MONTH} 字段的值，表示第十三个月。虽然 <code>GregorianCalendar</code> 不使用此值，但农历使用。
     */
    public final static int UNDECIMBER = 12;

    /**
     * {@link #AM_PM} 字段的值，表示从午夜到刚好午夜前的时段。
     */
    public final static int AM = 0;

    /**
     * {@link #AM_PM} 字段的值，表示从中午到刚好午夜前的时段。
     */
    public final static int PM = 1;

    /**
     * 用于 {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示所有样式的名称，如 "January" 和 "Jan"。
     *
     * @see #SHORT_FORMAT
     * @see #LONG_FORMAT
     * @see #SHORT_STANDALONE
     * @see #LONG_STANDALONE
     * @see #SHORT
     * @see #LONG
     * @since 1.6
     */
    public static final int ALL_STYLES = 0;

    static final int STANDALONE_MASK = 0x8000;

    /**
     * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和 {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，等同于 {@link #SHORT_FORMAT}。
     *
     * @see #SHORT_STANDALONE
     * @see #LONG
     * @since 1.6
     */
    public static final int SHORT = 1;

    /**
     * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和 {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，等同于 {@link #LONG_FORMAT}。
     *
     * @see #LONG_STANDALONE
     * @see #SHORT
     * @since 1.6
     */
    public static final int LONG = 2;

    /**
     * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和 {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示用于格式化的窄名称。窄名称通常是单个字符的字符串，如 "M" 表示星期一。
     *
     * @see #NARROW_STANDALONE
     * @see #SHORT_FORMAT
     * @see #LONG_FORMAT
     * @since 1.8
     */
    public static final int NARROW_FORMAT = 4;


/**
 * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和
 * {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示独立使用的窄名称。
 * 窄名称通常是单个字符的字符串，例如 "M" 表示星期一。
 *
 * @see #NARROW_FORMAT
 * @see #SHORT_STANDALONE
 * @see #LONG_STANDALONE
 * @since 1.8
 */
public static final int NARROW_STANDALONE = NARROW_FORMAT | STANDALONE_MASK;

/**
 * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和
 * {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示用于格式化的短名称。
 *
 * @see #SHORT_STANDALONE
 * @see #LONG_FORMAT
 * @see #LONG_STANDALONE
 * @since 1.8
 */
public static final int SHORT_FORMAT = 1;

/**
 * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和
 * {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示用于格式化的长名称。
 *
 * @see #LONG_STANDALONE
 * @see #SHORT_FORMAT
 * @see #SHORT_STANDALONE
 * @since 1.8
 */
public static final int LONG_FORMAT = 2;

/**
 * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和
 * {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示独立使用的短名称，
 * 例如日历表头中的月份缩写。
 *
 * @see #SHORT_FORMAT
 * @see #LONG_FORMAT
 * @see #LONG_STANDALONE
 * @since 1.8
 */
public static final int SHORT_STANDALONE = SHORT | STANDALONE_MASK;

/**
 * 用于 {@link #getDisplayName(int, int, Locale) getDisplayName} 和
 * {@link #getDisplayNames(int, int, Locale) getDisplayNames} 的样式说明符，表示独立使用的长名称，
 * 例如日历表头中的月份名称。
 *
 * @see #LONG_FORMAT
 * @see #SHORT_FORMAT
 * @see #SHORT_STANDALONE
 * @since 1.8
 */
public static final int LONG_STANDALONE = LONG | STANDALONE_MASK;

// 内部注释：
// Calendar 包含两种时间表示：当前 "time" 以毫秒为单位，和一组表示当前时间的 "fields"。
// 这两种表示通常是一致的，但可能会出现不同步的情况。
// 1. 初始时，没有字段被设置，时间无效。
// 2. 如果设置了时间，所有字段将被计算并同步。
// 3. 如果设置了一个单独的字段，时间无效。
// 当对象需要返回结果给用户或用于计算时，时间和字段的重新计算会发生。

/**
 * 本日历当前设置时间的日历字段值。
 * 这是一个包含 <code>FIELD_COUNT</code> 个整数的数组，索引值从 <code>ERA</code> 到 <code>DST_OFFSET</code>。
 * @serial
 */
@SuppressWarnings("ProtectedField")
protected int           fields[];

/**
 * 一个布尔数组，指示日历中指定的日历字段是否已设置。
 * 新对象没有任何字段被设置。在第一次调用生成字段的方法后，所有字段将保持已设置状态。
 * 这是一个包含 <code>FIELD_COUNT</code> 个布尔值的数组，索引值从 <code>ERA</code> 到 <code>DST_OFFSET</code>。
 * @serial
 */
@SuppressWarnings("ProtectedField")
protected boolean       isSet[];

/**
 * 指定每个字段被设置的时间戳。有两个特殊值，UNSET 和 COMPUTED。从
 * MINIMUM_USER_SET 到 Integer.MAX_VALUE 的值是合法的用户设置值。
 */
transient private int   stamp[];

/**
 * 本日历当前设置的时间，以自 1970 年 1 月 1 日 0:00:00 GMT 以来的毫秒数表示。
 * @see #isTimeSet
 * @serial
 */
@SuppressWarnings("ProtectedField")
protected long          time;

/**
 * 如果 <code>time</code> 的值有效，则为 true。
 * 更改 <code>field[]</code> 中的项会使时间无效。
 * @see #time
 * @serial
 */
@SuppressWarnings("ProtectedField")
protected boolean       isTimeSet;

/**
 * 如果 <code>fields[]</code> 与当前设置的时间同步，则为 true。
 * 如果为 false，则下次尝试获取字段值时将强制从 <code>time</code> 的当前值重新计算所有字段。
 * @serial
 */
@SuppressWarnings("ProtectedField")
protected boolean       areFieldsSet;

/**
 * 如果所有字段都已设置，则为 true。
 * @serial
 */
transient boolean       areAllFieldsSet;

/**
 * <code>True</code> 如果此日历在从 <code>fields[]</code> 计算 <code>time</code> 时允许超出范围的字段值。
 * @see #setLenient
 * @see #isLenient
 * @serial
 */
private boolean         lenient = true;

/**
 * 本日历使用的 <code>TimeZone</code>。日历使用时区数据在本地时间和 GMT 之间进行转换。
 * @serial
 */
private TimeZone        zone;

/**
 * <code>True</code> 如果时区引用了一个共享的 TimeZone 对象。
 */
transient private boolean sharedZone = false;

/**
 * 一周的第一天，可能的值为 <code>SUNDAY</code>、<code>MONDAY</code> 等。这是一个依赖于区域设置的值。
 * @serial
 */
private int             firstDayOfWeek;

/**
 * 一个月或一年中第一周所需的天数，可能的值从 1 到 7。这是一个依赖于区域设置的值。
 * @serial
 */
private int             minimalDaysInFirstWeek;

/**
 * 用于缓存区域设置的 firstDayOfWeek 和 minimalDaysInFirstWeek。
 */
private static final ConcurrentMap<Locale, int[]> cachedLocaleData
    = new ConcurrentHashMap<>(3);

// stamp[] 的特殊值
/**
 * 对应的 fields[] 没有值。
 */
private static final int        UNSET = 0;

/**
 * 对应的 fields[] 的值已内部计算。
 */
private static final int        COMPUTED = 1;

/**
 * 对应的 fields[] 的值已外部设置。大于 1 的时间戳值表示对应的 fields[] 值被设置的时间。
 */
private static final int        MINIMUM_USER_STAMP = 2;

/**
 * 表示所有字段的掩码值。
 */
static final int ALL_FIELDS = (1 << FIELD_COUNT) - 1;

/**
 * <code>stamp[]</code> 的下一个可用值，这是一个内部数组。
 * 实际上不应该将此值写入流中，将来可能会从流中移除。在此期间，应使用 <code>MINIMUM_USER_STAMP</code>。
 * @serial
 */
private int             nextStamp = MINIMUM_USER_STAMP;

// 内部序列化版本，表示写入的版本
// - 0（默认）表示 JDK 1.1.5 及之前的版本
// - 1 表示从 JDK 1.1.6 开始的版本，写入正确的 'time' 值以及其他字段的兼容值。这是一个过渡格式。
// - 2（尚未实现）未来的版本，其中 fields[]、areFieldsSet 和 isTimeSet 变为瞬态，isSet[] 被移除。
// 在 JDK 1.1.6 中，我们写入与版本 2 兼容的格式。
static final int        currentSerialVersion = 1;

/**
 * 流中的序列化数据版本。可能的值：
 * <dl>
 * <dt><b>0</b> 或未出现在流中</dt>
 * <dd>
 * JDK 1.1.5 或更早版本。
 * </dd>
 * <dt><b>1</b></dt>
 * <dd>
 * JDK 1.1.6 或更高版本。写入正确的 'time' 值以及其他字段的兼容值。这是一个过渡格式。
 * </dd>
 * </dl>
 * 当流输出此类时，将写入最新格式和最高的允许 <code>serialVersionOnStream</code>。
 * @serial
 * @since JDK1.1.6
 */
private int             serialVersionOnStream = currentSerialVersion;

// 声明与 JDK 1.1 的序列化兼容性
static final long       serialVersionUID = -1807547505821590642L;

// 日历字段的掩码值
@SuppressWarnings("PointlessBitwiseExpression")
final static int ERA_MASK           = (1 << ERA);
final static int YEAR_MASK          = (1 << YEAR);
final static int MONTH_MASK         = (1 << MONTH);
final static int WEEK_OF_YEAR_MASK  = (1 << WEEK_OF_YEAR);
final static int WEEK_OF_MONTH_MASK = (1 << WEEK_OF_MONTH);
final static int DAY_OF_MONTH_MASK  = (1 << DAY_OF_MONTH);
final static int DATE_MASK          = DAY_OF_MONTH_MASK;
final static int DAY_OF_YEAR_MASK   = (1 << DAY_OF_YEAR);
final static int DAY_OF_WEEK_MASK   = (1 << DAY_OF_WEEK);
final static int DAY_OF_WEEK_IN_MONTH_MASK  = (1 << DAY_OF_WEEK_IN_MONTH);
final static int AM_PM_MASK         = (1 << AM_PM);
final static int HOUR_MASK          = (1 << HOUR);
final static int HOUR_OF_DAY_MASK   = (1 << HOUR_OF_DAY);
final static int MINUTE_MASK        = (1 << MINUTE);
final static int SECOND_MASK        = (1 << SECOND);
final static int MILLISECOND_MASK   = (1 << MILLISECOND);
final static int ZONE_OFFSET_MASK   = (1 << ZONE_OFFSET);
final static int DST_OFFSET_MASK    = (1 << DST_OFFSET);

/**
 * {@code Calendar.Builder} 用于从各种日期时间参数创建 {@code Calendar}。
 *
 * <p>有两种方法可以将 {@code Calendar} 设置为日期时间值。一种是将瞬时参数设置为从 <a
 * href="Calendar.html#Epoch">纪元</a> 开始的毫秒偏移量。另一种是将单独的字段参数（如 {@link Calendar#YEAR YEAR}）设置为其所需的值。这两种方法不能混用。尝试同时设置瞬时值和单独的字段将导致抛出 {@link IllegalStateException}。但是，允许覆盖瞬时或字段参数的先前值。
 *
 * <p>如果提供的字段参数不足以确定日期和/或时间，构建 {@code Calendar} 时将使用特定于日历的默认值。例如，如果未为格里高利历提供 {@link Calendar#YEAR YEAR} 值，将使用 1970。如果字段参数之间存在任何冲突，将应用 <a
 * href="Calendar.html#resolution">解析规则</a>。因此，字段设置的顺序很重要。
 *
 * <p>除了日期时间参数外，还可以设置
 * {@linkplain #setLocale(Locale) 区域设置}、
 * {@linkplain #setTimeZone(TimeZone) 时区}、
 * {@linkplain #setWeekDefinition(int, int) 周定义} 和
 * {@linkplain #setLenient(boolean) 宽容模式} 参数。
 *
 * <p><b>示例</b>
 * <p>以下是一些示例用法。示例代码假设 {@code Calendar} 常量已静态导入。
 *
 * <p>以下代码生成一个日期为 2012-12-31（格里高利历）的 {@code Calendar}，因为根据 <a
 * href="GregorianCalendar.html#iso8601_compatible_setting">ISO 8601 兼容周参数</a>，星期一是周的第一天。
 * <pre>
 *   Calendar cal = new Calendar.Builder().setCalendarType("iso8601")
 *                        .setWeekDate(2013, 1, MONDAY).build();</pre>
 * <p>以下代码生成一个日期为 1989-01-08（格里高利历）的日本 {@code Calendar}，假设默认 {@link Calendar#ERA ERA} 是从那天开始的 <em>平成</em>。
 * <pre>
 *   Calendar cal = new Calendar.Builder().setCalendarType("japanese")
 *                        .setFields(YEAR, 1, DAY_OF_YEAR, 1).build();</pre>
 *
 * @since 1.8
 * @see Calendar#getInstance(TimeZone, Locale)
 * @see Calendar#fields
 */
public static class Builder {
    private static final int NFIELDS = FIELD_COUNT + 1; // +1 for WEEK_YEAR
    private static final int WEEK_YEAR = FIELD_COUNT;

    private long instant;
    // Calendar.stamp[]（下半部分）和 Calendar.fields[]（上半部分）的组合
    private int[] fields;
    // 从 MINIMUM_USER_STAMP 开始的伪时间戳。
    // （COMPUTED 用于表示已设置瞬时值。）
    private int nextStamp;
    // maxFieldIndex 保持已设置字段的最大索引。
    // （WEEK_YEAR 从不包含在内。）
    private int maxFieldIndex;
    private String type;
    private TimeZone zone;
    private boolean lenient = true;
    private Locale locale;
    private int firstDayOfWeek, minimalDaysInFirstWeek;

    /**
     * 构造一个 {@code Calendar.Builder}。
     */
    public Builder() {
    }

    /**
     * 将瞬时参数设置为从 <a href="Calendar.html#Epoch">纪元</a> 开始的给定 {@code instant} 值的毫秒偏移量。
     *
     * @param instant 从纪元开始的毫秒偏移量
     * @return 此 {@code Calendar.Builder}
     * @throws IllegalStateException 如果已设置任何字段参数
     * @see Calendar#setTime(Date)
     * @see Calendar#setTimeInMillis(long)
     * @see Calendar#time
     */
    public Builder setInstant(long instant) {
        if (fields != null) {
            throw new IllegalStateException();
        }
        this.instant = instant;
        nextStamp = COMPUTED;
        return this;
    }

    /**
     * 将瞬时参数设置为由 {@link Date} 给定的 {@code instant} 值。此方法等同于调用
     * {@link #setInstant(long) setInstant(instant.getTime())}。
     *
     * @param instant 表示从纪元开始的毫秒偏移量的 {@code Date}
     * @return 此 {@code Calendar.Builder}
     * @throws NullPointerException  如果 {@code instant} 为 {@code null}
     * @throws IllegalStateException 如果已设置任何字段参数
     * @see Calendar#setTime(Date)
     * @see Calendar#setTimeInMillis(long)
     * @see Calendar#time
     */
    public Builder setInstant(Date instant) {
        return setInstant(instant.getTime()); // NPE if instant == null
    }

    /**
     * 将 {@code field} 参数设置为给定的 {@code value}。
     * {@code field} 是 {@link Calendar#fields} 的索引，例如 {@link Calendar#DAY_OF_MONTH DAY_OF_MONTH}。
     * 本方法中不执行字段值验证。在构建 {@code Calendar} 时，超出范围的值将在宽容模式下被规范化或在非宽容模式下被检测为无效值。
     *
     * @param field {@code Calendar} 字段的索引
     * @param value 字段值
     * @return 此 {@code Calendar.Builder}
     * @throws IllegalArgumentException 如果 {@code field} 无效
     * @throws IllegalStateException 如果已设置瞬时值，或字段已被设置太多次
     *                      （大约 {@link Integer#MAX_VALUE} 次）。
     * @see Calendar#set(int, int)
     */
    public Builder set(int field, int value) {
        // 注意：WEEK_YEAR 不能通过此方法设置。
        if (field < 0 || field >= FIELD_COUNT) {
            throw new IllegalArgumentException("field is invalid");
        }
        if (isInstantSet()) {
            throw new IllegalStateException("instant has been set");
        }
        allocateFields();
        internalSet(field, value);
        return this;
    }


                    /**
         * 将字段参数设置为其由 {@code fieldValuePairs} 给出的值，这些值是字段及其值的对。
         * 例如，
         * <pre>
         *   setFeilds(Calendar.YEAR, 2013,
         *             Calendar.MONTH, Calendar.DECEMBER,
         *             Calendar.DAY_OF_MONTH, 23);</pre>
         * 等效于以下 {@link #set(int, int) set} 调用序列：
         * <pre>
         *   set(Calendar.YEAR, 2013)
         *   .set(Calendar.MONTH, Calendar.DECEMBER)
         *   .set(Calendar.DAY_OF_MONTH, 23);</pre>
         *
         * @param fieldValuePairs 字段-值对
         * @return 此 {@code Calendar.Builder}
         * @throws NullPointerException 如果 {@code fieldValuePairs} 为 {@code null}
         * @throws IllegalArgumentException 如果任何字段无效，
         *             或者 {@code fieldValuePairs.length} 是奇数。
         * @throws IllegalStateException 如果实例值已设置，
         *             或者字段已设置太多次（大约 {@link Integer#MAX_VALUE} 次）。
         */
        public Builder setFields(int... fieldValuePairs) {
            int len = fieldValuePairs.length;
            if ((len % 2) != 0) {
                throw new IllegalArgumentException();
            }
            if (isInstantSet()) {
                throw new IllegalStateException("实例值已设置");
            }
            if ((nextStamp + len / 2) < 0) {
                throw new IllegalStateException("时间戳计数器溢出");
            }
            allocateFields();
            for (int i = 0; i < len; ) {
                int field = fieldValuePairs[i++];
                // 注意：WEEK_YEAR 不能通过此方法设置。
                if (field < 0 || field >= FIELD_COUNT) {
                    throw new IllegalArgumentException("字段无效");
                }
                internalSet(field, fieldValuePairs[i++]);
            }
            return this;
        }

        /**
         * 将日期字段参数设置为由 {@code year}、{@code month} 和 {@code dayOfMonth} 给出的值。
         * 此方法等效于以下调用：
         * <pre>
         *   setFields(Calendar.YEAR, year,
         *             Calendar.MONTH, month,
         *             Calendar.DAY_OF_MONTH, dayOfMonth);</pre>
         *
         * @param year       {@link Calendar#YEAR YEAR} 值
         * @param month      {@link Calendar#MONTH MONTH} 值
         *                   （月份编号是 <em>0 基数</em>）。
         * @param dayOfMonth {@link Calendar#DAY_OF_MONTH DAY_OF_MONTH} 值
         * @return 此 {@code Calendar.Builder}
         */
        public Builder setDate(int year, int month, int dayOfMonth) {
            return setFields(YEAR, year, MONTH, month, DAY_OF_MONTH, dayOfMonth);
        }

        /**
         * 将一天中的时间字段参数设置为由 {@code hourOfDay}、{@code minute} 和 {@code second} 给出的值。
         * 此方法等效于以下调用：
         * <pre>
         *   setTimeOfDay(hourOfDay, minute, second, 0);</pre>
         *
         * @param hourOfDay {@link Calendar#HOUR_OF_DAY HOUR_OF_DAY} 值
         *                  （24 小时制）
         * @param minute    {@link Calendar#MINUTE MINUTE} 值
         * @param second    {@link Calendar#SECOND SECOND} 值
         * @return 此 {@code Calendar.Builder}
         */
        public Builder setTimeOfDay(int hourOfDay, int minute, int second) {
            return setTimeOfDay(hourOfDay, minute, second, 0);
        }

        /**
         * 将一天中的时间字段参数设置为由 {@code hourOfDay}、{@code minute}、{@code second} 和
         * {@code millis} 给出的值。此方法等效于以下调用：
         * <pre>
         *   setFields(Calendar.HOUR_OF_DAY, hourOfDay,
         *             Calendar.MINUTE, minute,
         *             Calendar.SECOND, second,
         *             Calendar.MILLISECOND, millis);</pre>
         *
         * @param hourOfDay {@link Calendar#HOUR_OF_DAY HOUR_OF_DAY} 值
         *                  （24 小时制）
         * @param minute    {@link Calendar#MINUTE MINUTE} 值
         * @param second    {@link Calendar#SECOND SECOND} 值
         * @param millis    {@link Calendar#MILLISECOND MILLISECOND} 值
         * @return 此 {@code Calendar.Builder}
         */
        public Builder setTimeOfDay(int hourOfDay, int minute, int second, int millis) {
            return setFields(HOUR_OF_DAY, hourOfDay, MINUTE, minute,
                             SECOND, second, MILLISECOND, millis);
        }

        /**
         * 将基于周的日期参数设置为给定的日期说明符 - 周年、周数和周几。
         *
         * <p>如果指定的日历不支持周日期，则 {@link #build() build} 方法将抛出 {@link IllegalArgumentException}。
         *
         * @param weekYear   周年
         * @param weekOfYear 基于 {@code weekYear} 的周数
         * @param dayOfWeek  周几值：{@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 字段的常量之一：
         *     {@link Calendar#SUNDAY SUNDAY}，...，{@link Calendar#SATURDAY SATURDAY}。
         * @return 此 {@code Calendar.Builder}
         * @see Calendar#setWeekDate(int, int, int)
         * @see Calendar#isWeekDateSupported()
         */
        public Builder setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
            allocateFields();
            internalSet(WEEK_YEAR, weekYear);
            internalSet(WEEK_OF_YEAR, weekOfYear);
            internalSet(DAY_OF_WEEK, dayOfWeek);
            return this;
        }

        /**
         * 将时区参数设置为给定的 {@code zone}。如果未给此 {@code Caledar.Builder} 提供时区参数，
         * 则在 {@link #build() build} 方法中将使用 {@linkplain TimeZone#getDefault() 默认
         * <code>TimeZone</code>}。
         *
         * @param zone {@link TimeZone}
         * @return 此 {@code Calendar.Builder}
         * @throws NullPointerException 如果 {@code zone} 为 {@code null}
         * @see Calendar#setTimeZone(TimeZone)
         */
        public Builder setTimeZone(TimeZone zone) {
            if (zone == null) {
                throw new NullPointerException();
            }
            this.zone = zone;
            return this;
        }

        /**
         * 将宽松模式参数设置为由 {@code lenient} 给出的值。如果未给此 {@code Calendar.Builder} 提供宽松参数，
         * 则在 {@link #build() build} 方法中将使用宽松模式。
         *
         * @param lenient {@code true} 表示宽松模式；
         *                {@code false} 表示非宽松模式
         * @return 此 {@code Calendar.Builder}
         * @see Calendar#setLenient(boolean)
         */
        public Builder setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        /**
         * 将日历类型参数设置为给定的 {@code type}。通过此方法给出的日历类型优先于通过
         * {@linkplain #setLocale(Locale) 语言环境} 显式或隐式给出的任何日历类型。
         *
         * <p>除了由 {@link Calendar#getAvailableCalendarTypes() Calendar.getAvailableCalendarTypes}
         * 方法返回的可用日历类型外，还可以使用 {@code "gregorian"} 和 {@code "iso8601"} 作为
         * {@code "gregory"} 的别名。
         *
         * @param type 日历类型
         * @return 此 {@code Calendar.Builder}
         * @throws NullPointerException 如果 {@code type} 为 {@code null}
         * @throws IllegalArgumentException 如果 {@code type} 未知
         * @throws IllegalStateException 如果已设置其他日历类型
         * @see Calendar#getCalendarType()
         * @see Calendar#getAvailableCalendarTypes()
         */
        public Builder setCalendarType(String type) {
            if (type.equals("gregorian")) { // 如果 type == null，则抛出 NPE
                type = "gregory";
            }
            if (!Calendar.getAvailableCalendarTypes().contains(type)
                    && !type.equals("iso8601")) {
                throw new IllegalArgumentException("未知日历类型: " + type);
            }
            if (this.type == null) {
                this.type = type;
            } else {
                if (!this.type.equals(type)) {
                    throw new IllegalStateException("日历类型覆盖");
                }
            }
            return this;
        }

        /**
         * 将语言环境参数设置为给定的 {@code locale}。如果未给此 {@code Calendar.Builder} 提供语言环境，
         * 则使用 {@linkplain
         * Locale#getDefault(Locale.Category) 默认 <code>Locale</code>}
         * 为 {@link Locale.Category#FORMAT}。
         *
         * <p>如果未通过调用 {@link #setCalendarType(String) setCalendarType} 方法显式给出日历类型，
         * 则使用 {@code Locale} 值来确定要构建的 {@code Calendar} 类型。
         *
         * <p>如果未通过调用 {@link #setWeekDefinition(int,int) setWeekDefinition} 方法显式给出周定义参数，
         * 则使用 {@code Locale} 的默认值。
         *
         * @param locale {@link Locale}
         * @throws NullPointerException 如果 {@code locale} 为 {@code null}
         * @return 此 {@code Calendar.Builder}
         * @see Calendar#getInstance(Locale)
         */
        public Builder setLocale(Locale locale) {
            if (locale == null) {
                throw new NullPointerException();
            }
            this.locale = locale;
            return this;
        }

        /**
         * 将周定义参数设置为由 {@code firstDayOfWeek} 和 {@code minimalDaysInFirstWeek} 给出的值，
         * 用于确定 <a href="Calendar.html#First_Week">一年中的第一周</a>。通过此方法给出的参数优先于
         * 由 {@linkplain #setLocale(Locale) 语言环境} 给出的默认值。
         *
         * @param firstDayOfWeek 一周的第一天；为
         *                       {@link Calendar#SUNDAY} 到 {@link Calendar#SATURDAY} 之一
         * @param minimalDaysInFirstWeek 第一周中的最小天数（1..7）
         * @return 此 {@code Calendar.Builder}
         * @throws IllegalArgumentException 如果 {@code firstDayOfWeek} 或
         *                                  {@code minimalDaysInFirstWeek} 无效
         * @see Calendar#getFirstDayOfWeek()
         * @see Calendar#getMinimalDaysInFirstWeek()
         */
        public Builder setWeekDefinition(int firstDayOfWeek, int minimalDaysInFirstWeek) {
            if (!isValidWeekParameter(firstDayOfWeek)
                    || !isValidWeekParameter(minimalDaysInFirstWeek)) {
                throw new IllegalArgumentException();
            }
            this.firstDayOfWeek = firstDayOfWeek;
            this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
            return this;
        }

        /**
         * 返回由 setter 方法设置的参数构建的 {@code Calendar}。通过 {@link #setCalendarType(String)
         * setCalendarType} 方法或 {@linkplain #setLocale(Locale) 语言环境} 给出的日历类型用于确定要创建的
         * {@code Calendar} 类型。如果未显式给出日历类型，则创建语言环境的默认日历。
         *
         * <p>如果日历类型为 {@code "iso8601"}，则 {@link GregorianCalendar} 的
         * {@linkplain GregorianCalendar#setGregorianChange(Date) 格里高利历更改日期}
         * 设置为 {@code Date(Long.MIN_VALUE)} 以成为 <em>延续的</em> 格里高利历。其周定义参数也设置为
         * <a href="GregorianCalendar.html#iso8601_compatible_setting">与 ISO 8601 标准兼容</a>。
         * 请注意，使用 {@code "iso8601"} 创建的 {@code GregorianCalendar} 的
         * {@link GregorianCalendar#getCalendarType() getCalendarType} 方法返回 {@code "gregory"}。
         *
         * <p>如果未显式给出语言环境和时区参数，则使用默认值。
         *
         * <p>任何超出范围的字段值在宽松模式下被规范化，或在非宽松模式下被检测为无效值。
         *
         * @return 使用此 {@code Calendar.Builder} 的参数构建的 {@code Calendar}
         * @throws IllegalArgumentException 如果日历类型未知，或者在非宽松模式下给出任何无效字段值，或者
         *             为不支持周日期的日历类型给出了周日期。
         * @see Calendar#getInstance(TimeZone, Locale)
         * @see Locale#getDefault(Locale.Category)
         * @see TimeZone#getDefault()
         */
        public Calendar build() {
            if (locale == null) {
                locale = Locale.getDefault();
            }
            if (zone == null) {
                zone = TimeZone.getDefault();
            }
            Calendar cal;
            if (type == null) {
                type = locale.getUnicodeLocaleType("ca");
            }
            if (type == null) {
                if (locale.getCountry() == "TH"
                    && locale.getLanguage() == "th") {
                    type = "buddhist";
                } else {
                    type = "gregory";
                }
            }
            switch (type) {
            case "gregory":
                cal = new GregorianCalendar(zone, locale, true);
                break;
            case "iso8601":
                GregorianCalendar gcal = new GregorianCalendar(zone, locale, true);
                // 使 gcal 成为延续的格里高利历
                gcal.setGregorianChange(new Date(Long.MIN_VALUE));
                // 使周定义与 ISO 8601 兼容
                setWeekDefinition(MONDAY, 4);
                cal = gcal;
                break;
            case "buddhist":
                cal = new BuddhistCalendar(zone, locale);
                cal.clear();
                break;
            case "japanese":
                cal = new JapaneseImperialCalendar(zone, locale, true);
                break;
            default:
                throw new IllegalArgumentException("未知日历类型: " + type);
            }
            cal.setLenient(lenient);
            if (firstDayOfWeek != 0) {
                cal.setFirstDayOfWeek(firstDayOfWeek);
                cal.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
            }
            if (isInstantSet()) {
                cal.setTimeInMillis(instant);
                cal.complete();
                return cal;
            }


                        if (fields != null) {
                boolean weekDate = isSet(WEEK_YEAR)
                                       && fields[WEEK_YEAR] > fields[YEAR];
                if (weekDate && !cal.isWeekDateSupported()) {
                    throw new IllegalArgumentException("周日期不受支持：" + type);
                }

                // 从最小时间戳设置到最大时间戳，以便
                // 在日历中解析字段。
                for (int stamp = MINIMUM_USER_STAMP; stamp < nextStamp; stamp++) {
                    for (int index = 0; index <= maxFieldIndex; index++) {
                        if (fields[index] == stamp) {
                            cal.set(index, fields[NFIELDS + index]);
                            break;
                        }
                    }
                }

                if (weekDate) {
                    int weekOfYear = isSet(WEEK_OF_YEAR) ? fields[NFIELDS + WEEK_OF_YEAR] : 1;
                    int dayOfWeek = isSet(DAY_OF_WEEK)
                                    ? fields[NFIELDS + DAY_OF_WEEK] : cal.getFirstDayOfWeek();
                    cal.setWeekDate(fields[NFIELDS + WEEK_YEAR], weekOfYear, dayOfWeek);
                }
                cal.complete();
            }

            return cal;
        }

        private void allocateFields() {
            if (fields == null) {
                fields = new int[NFIELDS * 2];
                nextStamp = MINIMUM_USER_STAMP;
                maxFieldIndex = -1;
            }
        }

        private void internalSet(int field, int value) {
            fields[field] = nextStamp++;
            if (nextStamp < 0) {
                throw new IllegalStateException("时间戳计数器溢出");
            }
            fields[NFIELDS + field] = value;
            if (field > maxFieldIndex && field < WEEK_YEAR) {
                maxFieldIndex = field;
            }
        }

        private boolean isInstantSet() {
            return nextStamp == COMPUTED;
        }

        private boolean isSet(int index) {
            return fields != null && fields[index] > UNSET;
        }

        private boolean isValidWeekParameter(int value) {
            return value > 0 && value <= 7;
        }
    }

    /**
     * 使用默认时区和默认 {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境构造一个日历。
     * @see     TimeZone#getDefault
     */
    protected Calendar()
    {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        sharedZone = true;
    }

    /**
     * 使用指定的时区和语言环境构造一个日历。
     *
     * @param zone 要使用的时区
     * @param aLocale 周数据的语言环境
     */
    protected Calendar(TimeZone zone, Locale aLocale)
    {
        fields = new int[FIELD_COUNT];
        isSet = new boolean[FIELD_COUNT];
        stamp = new int[FIELD_COUNT];

        this.zone = zone;
        setWeekCountData(aLocale);
    }

    /**
     * 使用默认时区和语言环境获取一个日历。返回的日历基于当前时间
     * 和默认的 {@link Locale.Category#FORMAT FORMAT} 语言环境。
     *
     * @return 一个日历。
     */
    public static Calendar getInstance()
    {
        return createCalendar(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 使用指定的时区和默认语言环境获取一个日历。返回的日历基于当前时间
     * 和给定的时区与默认的 {@link Locale.Category#FORMAT FORMAT} 语言环境。
     *
     * @param zone 要使用的时区
     * @return 一个日历。
     */
    public static Calendar getInstance(TimeZone zone)
    {
        return createCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 使用默认时区和指定的语言环境获取一个日历。返回的日历基于当前时间
     * 和默认的时区与给定的语言环境。
     *
     * @param aLocale 周数据的语言环境
     * @return 一个日历。
     */
    public static Calendar getInstance(Locale aLocale)
    {
        return createCalendar(TimeZone.getDefault(), aLocale);
    }

    /**
     * 使用指定的时区和语言环境获取一个日历。返回的日历基于当前时间
     * 和给定的时区与语言环境。
     *
     * @param zone 要使用的时区
     * @param aLocale 周数据的语言环境
     * @return 一个日历。
     */
    public static Calendar getInstance(TimeZone zone,
                                       Locale aLocale)
    {
        return createCalendar(zone, aLocale);
    }

    private static Calendar createCalendar(TimeZone zone,
                                           Locale aLocale)
    {
        CalendarProvider provider =
            LocaleProviderAdapter.getAdapter(CalendarProvider.class, aLocale)
                                 .getCalendarProvider();
        if (provider != null) {
            try {
                return provider.getInstance(zone, aLocale);
            } catch (IllegalArgumentException iae) {
                // 回退到默认实例化
            }
        }

        Calendar cal = null;

        if (aLocale.hasExtensions()) {
            String caltype = aLocale.getUnicodeLocaleType("ca");
            if (caltype != null) {
                switch (caltype) {
                case "buddhist":
                cal = new BuddhistCalendar(zone, aLocale);
                    break;
                case "japanese":
                    cal = new JapaneseImperialCalendar(zone, aLocale);
                    break;
                case "gregory":
                    cal = new GregorianCalendar(zone, aLocale);
                    break;
                }
            }
        }
        if (cal == null) {
            // 如果没有显式指定已知的日历类型，
            // 则以传统方式创建一个日历：
            // 为 th_TH 语言环境创建一个 BuddhistCalendar，
            // 为 ja_JP_JP 语言环境创建一个 JapaneseImperialCalendar，
            // 或为其他任何语言环境创建一个 GregorianCalendar。
            // 注意：语言、国家和变体字符串已进行内部化。
            if (aLocale.getLanguage() == "th" && aLocale.getCountry() == "TH") {
                cal = new BuddhistCalendar(zone, aLocale);
            } else if (aLocale.getVariant() == "JP" && aLocale.getLanguage() == "ja"
                       && aLocale.getCountry() == "JP") {
                cal = new JapaneseImperialCalendar(zone, aLocale);
            } else {
                cal = new GregorianCalendar(zone, aLocale);
            }
        }
        return cal;
    }

    /**
     * 返回可以从此类的 <code>getInstance</code> 方法返回本地化实例的所有语言环境的数组。
     * 返回的数组必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的语言环境实例。
     *
     * @return 可以获取本地化 <code>Calendar</code> 实例的语言环境数组。
     */
    public static synchronized Locale[] getAvailableLocales()
    {
        return DateFormat.getAvailableLocales();
    }

    /**
     * 将当前日历字段值 {@link #fields fields[]} 转换为毫秒时间值
     * {@link #time}。
     *
     * @see #complete()
     * @see #computeFields()
     */
    protected abstract void computeTime();

    /**
     * 将当前毫秒时间值 {@link #time} 转换为日历字段值 {@link #fields fields[]}。
     * 这允许您同步日历字段值与为日历设置的新时间。时间不会先重新计算；
     * 要重新计算时间，然后计算字段，调用 {@link #complete()} 方法。
     *
     * @see #computeTime()
     */
    protected abstract void computeFields();

    /**
     * 返回一个表示此 <code>Calendar</code> 时间值（从 <a
     * href="#Epoch">纪元</a> 开始的毫秒偏移量）的 <code>Date</code> 对象。
     *
     * @return 一个表示时间值的 <code>Date</code>。
     * @see #setTime(Date)
     * @see #getTimeInMillis()
     */
    public final Date getTime() {
        return new Date(getTimeInMillis());
    }

    /**
     * 使用给定的 <code>Date</code> 设置此日历的时间。
     * <p>
     * 注意：使用 <code>Date(Long.MAX_VALUE)</code> 或 <code>Date(Long.MIN_VALUE)</code>
     * 调用 <code>setTime()</code> 可能会导致从 <code>get()</code> 获取的字段值不正确。
     *
     * @param date 给定的日期。
     * @see #getTime()
     * @see #setTimeInMillis(long)
     */
    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    /**
     * 返回此日历的时间值（以毫秒为单位）。
     *
     * @return 从纪元开始的当前时间（以 UTC 毫秒为单位）。
     * @see #getTime()
     * @see #setTimeInMillis(long)
     */
    public long getTimeInMillis() {
        if (!isTimeSet) {
            updateTime();
        }
        return time;
    }

    /**
     * 从此给定的 long 值设置此日历的当前时间。
     *
     * @param millis 从纪元开始的当前时间（以 UTC 毫秒为单位）。
     * @see #setTime(Date)
     * @see #getTimeInMillis()
     */
    public void setTimeInMillis(long millis) {
        // 如果不需要重新计算日历字段值，则不执行任何操作。
        if (time == millis && isTimeSet && areFieldsSet && areAllFieldsSet
            && (zone instanceof ZoneInfo) && !((ZoneInfo)zone).isDirty()) {
            return;
        }
        time = millis;
        isTimeSet = true;
        areFieldsSet = false;
        computeFields();
        areAllFieldsSet = areFieldsSet = true;
    }

    /**
     * 返回给定日历字段的值。在宽松模式下，所有日历字段都被标准化。在非宽松模式下，所有
     * 日历字段都被验证，如果任何日历字段的值超出范围，此方法将抛出异常。标准化和验证由
     * {@link #complete()} 方法处理，该过程依赖于日历系统。
     *
     * @param field 给定的日历字段。
     * @return 给定日历字段的值。
     * @throws ArrayIndexOutOfBoundsException 如果指定的字段超出范围
     *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>)。
     * @see #set(int,int)
     * @see #complete()
     */
    public int get(int field)
    {
        complete();
        return internalGet(field);
    }

    /**
     * 返回给定日历字段的值。此方法不涉及字段值的标准化或验证。
     *
     * @param field 给定的日历字段。
     * @return 给定日历字段的值。
     * @see #get(int)
     */
    protected final int internalGet(int field)
    {
        return fields[field];
    }

    /**
     * 设置给定日历字段的值。此方法不会影响此
     * <code>Calendar</code> 实例中字段的设置状态。
     *
     * @throws IndexOutOfBoundsException 如果指定的字段超出范围
     *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>)。
     * @see #areFieldsSet
     * @see #isTimeSet
     * @see #areAllFieldsSet
     * @see #set(int,int)
     */
    final void internalSet(int field, int value)
    {
        fields[field] = value;
    }

    /**
     * 设置给定日历字段的值。此方法无论宽松模式如何都不会解释该值。
     *
     * @param field 给定的日历字段。
     * @param value 要设置给给定日历字段的值。
     * @throws ArrayIndexOutOfBoundsException 如果指定的字段超出范围
     *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>)。
     * @see #set(int,int,int)
     * @see #set(int,int,int,int,int)
     * @see #set(int,int,int,int,int,int)
     * @see #get(int)
     */
    public void set(int field, int value)
    {
        // 如果字段部分标准化，则在更改任何字段之前计算所有字段。
        if (areFieldsSet && !areAllFieldsSet) {
            computeFields();
        }
        internalSet(field, value);
        isTimeSet = false;
        areFieldsSet = false;
        isSet[field] = true;
        stamp[field] = nextStamp++;
        if (nextStamp == Integer.MAX_VALUE) {
            adjustStamp();
        }
    }

    /**
     * 设置日历字段 <code>YEAR</code>、<code>MONTH</code> 和 <code>DAY_OF_MONTH</code> 的值。
     * 保留其他字段的先前值。如果不希望保留，请先调用 {@link #clear()}。
     *
     * @param year 用于设置 <code>YEAR</code> 日历字段的值。
     * @param month 用于设置 <code>MONTH</code> 日历字段的值。
     * 月份值从 0 开始。例如，0 表示一月。
     * @param date 用于设置 <code>DAY_OF_MONTH</code> 日历字段的值。
     * @see #set(int,int)
     * @see #set(int,int,int,int,int)
     * @see #set(int,int,int,int,int,int)
     */
    public final void set(int year, int month, int date)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
    }

    /**
     * 设置日历字段 <code>YEAR</code>、<code>MONTH</code>、<code>DAY_OF_MONTH</code>、
     * <code>HOUR_OF_DAY</code> 和 <code>MINUTE</code> 的值。
     * 保留其他字段的先前值。如果不希望保留，请先调用 {@link #clear()}。
     *
     * @param year 用于设置 <code>YEAR</code> 日历字段的值。
     * @param month 用于设置 <code>MONTH</code> 日历字段的值。
     * 月份值从 0 开始。例如，0 表示一月。
     * @param date 用于设置 <code>DAY_OF_MONTH</code> 日历字段的值。
     * @param hourOfDay 用于设置 <code>HOUR_OF_DAY</code> 日历字段的值。
     * @param minute 用于设置 <code>MINUTE</code> 日历字段的值。
     * @see #set(int,int)
     * @see #set(int,int,int)
     * @see #set(int,int,int,int,int,int)
     */
    public final void set(int year, int month, int date, int hourOfDay, int minute)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
    }

    /**
     * 设置日历字段 <code>YEAR</code>、<code>MONTH</code>、<code>DAY_OF_MONTH</code>、
     * <code>HOUR_OF_DAY</code>、<code>MINUTE</code> 和 <code>SECOND</code> 的值。
     * 保留其他字段的先前值。如果不希望保留，请先调用 {@link #clear()}。
     *
     * @param year 用于设置 <code>YEAR</code> 日历字段的值。
     * @param month 用于设置 <code>MONTH</code> 日历字段的值。
     * 月份值从 0 开始。例如，0 表示一月。
     * @param date 用于设置 <code>DAY_OF_MONTH</code> 日历字段的值。
     * @param hourOfDay 用于设置 <code>HOUR_OF_DAY</code> 日历字段的值。
     * @param minute 用于设置 <code>MINUTE</code> 日历字段的值。
     * @param second 用于设置 <code>SECOND</code> 日历字段的值。
     * @see #set(int,int)
     * @see #set(int,int,int)
     * @see #set(int,int,int,int,int)
     */
    public final void set(int year, int month, int date, int hourOfDay, int minute,
                          int second)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
    }


                /**
     * 将此 <code>Calendar</code> 的所有日历字段值和时间值（从 <a href="#Epoch">纪元</a> 开始的毫秒偏移量）设置为未定义。
     * 这意味着 {@link #isSet(int) isSet()} 将为所有日历字段返回 <code>false</code>，并且日期和时间计算将像这些字段从未被设置一样处理。
     * <code>Calendar</code> 实现类可以使用其特定的默认字段值进行日期/时间计算。例如，<code>GregorianCalendar</code> 如果 <code>YEAR</code> 字段值未定义，则使用 1970。
     *
     * @see #clear(int)
     */
    public final void clear()
    {
        for (int i = 0; i < fields.length; ) {
            stamp[i] = fields[i] = 0; // UNSET == 0
            isSet[i++] = false;
        }
        areAllFieldsSet = areFieldsSet = false;
        isTimeSet = false;
    }

    /**
     * 设置给定的日历字段值和此 <code>Calendar</code> 的时间值（从 <a href="#Epoch">纪元</a> 开始的毫秒偏移量）为未定义。
     * 这意味着 {@link #isSet(int) isSet(field)} 将返回 <code>false</code>，并且日期和时间计算将像该字段从未被设置一样处理。
     * <code>Calendar</code> 实现类可以使用该字段的特定默认值进行日期和时间计算。
     *
     * <p>{@link #HOUR_OF_DAY}、{@link #HOUR} 和 {@link #AM_PM} 字段独立处理，并应用 <a href="#time_resolution">一天中时间的解析规则</a>。
     * 清除这些字段中的一个不会重置此 <code>Calendar</code> 的小时值。使用 {@link #set(int,int) set(Calendar.HOUR_OF_DAY, 0)} 重置小时值。
     *
     * @param field 要清除的日历字段。
     * @see #clear()
     */
    public final void clear(int field)
    {
        fields[field] = 0;
        stamp[field] = UNSET;
        isSet[field] = false;

        areAllFieldsSet = areFieldsSet = false;
        isTimeSet = false;
    }

    /**
     * 确定给定的日历字段是否已设置值，包括由 <code>get</code> 方法调用触发的内部字段计算设置的值。
     *
     * @param field 要测试的日历字段
     * @return <code>true</code> 如果给定的日历字段已设置值；否则返回 <code>false</code>。
     */
    public final boolean isSet(int field)
    {
        return stamp[field] != UNSET;
    }

    /**
     * 返回给定 <code>style</code> 和 <code>locale</code> 下日历 <code>field</code> 值的字符串表示形式。
     * 如果没有适用的字符串表示形式，则返回 <code>null</code>。如果给定的日历 <code>field</code> 适用字符串表示形式，此方法将调用
     * {@link Calendar#get(int) get(field)} 获取日历 <code>field</code> 值。
     *
     * <p>例如，如果此 <code>Calendar</code> 是 <code>GregorianCalendar</code> 且日期为 2005-01-01，则 {@link #MONTH} 字段的字符串表示形式
     * 在英语区域设置中为 "January"（长格式）或 "Jan"（短格式）。然而，对于 {@link #DAY_OF_MONTH} 字段没有字符串表示形式，此方法将返回 <code>null</code>。
     *
     * <p>默认实现支持在给定 <code>locale</code> 下 {@link DateFormatSymbols} 有名称的日历字段。
     *
     * @param field
     *        返回其字符串表示形式的日历字段
     * @param style
     *        应用于字符串表示形式的样式；可以是 {@link #SHORT_FORMAT} ({@link #SHORT})、{@link #SHORT_STANDALONE}、
     *        {@link #LONG_FORMAT} ({@link #LONG})、{@link #LONG_STANDALONE}、{@link #NARROW_FORMAT} 或 {@link #NARROW_STANDALONE}。
     * @param locale
     *        字符串表示形式的区域设置（任何由 {@code locale} 指定的日历类型将被忽略）
     * @return 给定 {@code field} 在给定 {@code style} 下的字符串表示形式，或如果无适用的字符串表示形式则返回 {@code null}。
     * @exception IllegalArgumentException
     *        如果 {@code field} 或 {@code style} 无效，或者此 {@code Calendar} 是非宽容的且任何日历字段有无效值
     * @exception NullPointerException
     *        如果 {@code locale} 为 null
     * @since 1.6
     */
    public String getDisplayName(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, SHORT, NARROW_FORMAT, locale,
                            ERA_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }

        String calendarType = getCalendarType();
        int fieldValue = get(field);
        // 独立样式和窄格式样式仅通过 CalendarDataProviders 支持。
        if (isStandaloneStyle(style) || isNarrowFormatStyle(style)) {
            String val = CalendarDataUtility.retrieveFieldValueName(calendarType,
                                                                    field, fieldValue,
                                                                    style, locale);
            // 在此处执行回退以遵循 CLDR 规则
            if (val == null) {
                if (isNarrowFormatStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType,
                                                                     field, fieldValue,
                                                                     toStandaloneStyle(style),
                                                                     locale);
                } else if (isStandaloneStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType,
                                                                     field, fieldValue,
                                                                     getBaseStyle(style),
                                                                     locale);
                }
            }
            return val;
        }

        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = getFieldStrings(field, style, symbols);
        if (strings != null) {
            if (fieldValue < strings.length) {
                return strings[fieldValue];
            }
        }
        return null;
    }

    /**
     * 返回包含给定 <code>style</code> 和 <code>locale</code> 下日历 <code>field</code> 所有名称及其对应字段值的 {@code Map}。
     * 例如，如果此 {@code Calendar} 是 {@link GregorianCalendar}，则返回的映射将包含 "Jan" 对应 {@link #JANUARY}、"Feb" 对应 {@link #FEBRUARY} 等，在英语区域设置中为 {@linkplain #SHORT 短} 样式。
     *
     * <p>由于使用单个字符，窄名称可能不唯一，例如 "S" 表示星期日和星期六。在这种情况下，窄名称不会包含在返回的 {@code Map} 中。
     *
     * <p>其他日历字段的值可能会影响显示名称集的确定。例如，如果此 {@code Calendar} 是阴阳历系统且 {@link #YEAR} 字段给出的年份有一个闰月，
     * 则此方法将返回包含闰月名称的月份名称，月份名称将映射到该年份特定的值。
     *
     * <p>默认实现支持包含在 {@link DateFormatSymbols} 中的显示名称。例如，如果 {@code field} 是 {@link #MONTH} 且 {@code style} 是 {@link #ALL_STYLES}，
     * 则此方法将返回一个包含 {@link DateFormatSymbols#getShortMonths()} 和 {@link DateFormatSymbols#getMonths()} 返回的所有字符串的 {@code Map}。
     *
     * @param field
     *        返回其显示名称的日历字段
     * @param style
     *        应用于字符串表示形式的样式；可以是 {@link #SHORT_FORMAT} ({@link #SHORT})、{@link #SHORT_STANDALONE}、
     *        {@link #LONG_FORMAT} ({@link #LONG})、{@link #LONG_STANDALONE}、{@link #NARROW_FORMAT} 或 {@link #NARROW_STANDALONE}。
     * @param locale
     *        显示名称的区域设置
     * @return 包含给定 {@code style} 和 {@code locale} 下所有显示名称及其字段值的 {@code Map}，或如果未定义 {@code field} 的显示名称则返回 {@code null}。
     * @exception IllegalArgumentException
     *        如果 {@code field} 或 {@code style} 无效，或者此 {@code Calendar} 是非宽容的且任何日历字段有无效值
     * @exception NullPointerException
     *        如果 {@code locale} 为 null
     * @since 1.6
     */
    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, ALL_STYLES, NARROW_FORMAT, locale,
                                    ERA_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }

        String calendarType = getCalendarType();
        if (style == ALL_STYLES || isStandaloneStyle(style) || isNarrowFormatStyle(style)) {
            Map<String, Integer> map;
            map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, style, locale);

            // 在此处执行回退以遵循 CLDR 规则
            if (map == null) {
                if (isNarrowFormatStyle(style)) {
                    map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field,
                                                                      toStandaloneStyle(style), locale);
                } else if (style != ALL_STYLES) {
                    map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field,
                                                                      getBaseStyle(style), locale);
                }
            }
            return map;
        }

        // SHORT 或 LONG
        return getDisplayNamesImpl(field, style, locale);
    }

    private Map<String,Integer> getDisplayNamesImpl(int field, int style, Locale locale) {
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = getFieldStrings(field, style, symbols);
        if (strings != null) {
            Map<String,Integer> names = new HashMap<>();
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].length() == 0) {
                    continue;
                }
                names.put(strings[i], i);
            }
            return names;
        }
        return null;
    }

    boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle,
                                   Locale locale, int fieldMask) {
        int baseStyle = getBaseStyle(style); // 忽略独立样式掩码
        if (field < 0 || field >= fields.length ||
            baseStyle < minStyle || baseStyle > maxStyle) {
            throw new IllegalArgumentException();
        }
        if (locale == null) {
            throw new NullPointerException();
        }
        return isFieldSet(fieldMask, field);
    }

    private String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
        int baseStyle = getBaseStyle(style); // 忽略独立样式掩码

        // DateFormatSymbols 不支持任何窄名称。
        if (baseStyle == NARROW_FORMAT) {
            return null;
        }

        String[] strings = null;
        switch (field) {
        case ERA:
            strings = symbols.getEras();
            break;

        case MONTH:
            strings = (baseStyle == LONG) ? symbols.getMonths() : symbols.getShortMonths();
            break;

        case DAY_OF_WEEK:
            strings = (baseStyle == LONG) ? symbols.getWeekdays() : symbols.getShortWeekdays();
            break;

        case AM_PM:
            strings = symbols.getAmPmStrings();
            break;
        }
        return strings;
    }

    /**
     * 填充日历字段中未设置的字段。首先，如果时间值（从 <a href="#Epoch">纪元</a> 开始的毫秒偏移量）尚未从日历字段值计算出来，则调用 {@link #computeTime()} 方法。
     * 然后，调用 {@link #computeFields()} 方法计算所有日历字段值。
     */
    protected void complete()
    {
        if (!isTimeSet) {
            updateTime();
        }
        if (!areFieldsSet || !areAllFieldsSet) {
            computeFields(); // 填充未设置的字段
            areAllFieldsSet = areFieldsSet = true;
        }
    }

    /**
     * 返回指定的日历字段是否已通过调用其中一个设置方法而不是通过内部时间计算设置值。
     *
     * @return <code>true</code> 如果字段已通过外部设置，<code>false</code> 否则。
     * @exception IndexOutOfBoundsException 如果指定的 <code>field</code> 超出范围
     *               (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>)。
     * @see #selectFields()
     * @see #setFieldsComputed(int)
     */
    final boolean isExternallySet(int field) {
        return stamp[field] >= MINIMUM_USER_STAMP;
    }

    /**
     * 返回一个字段掩码（位掩码），指示所有已设置状态的日历字段。
     *
     * @return 表示已设置状态字段的位掩码
     */
    final int getSetStateFields() {
        int mask = 0;
        for (int i = 0; i < fields.length; i++) {
            if (stamp[i] != UNSET) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    /**
     * 将指定的日历字段的状态设置为 <em>已计算</em>。此状态表示指定的日历字段已通过内部时间计算设置有效值，而不是通过调用其中一个设置方法设置的。
     *
     * @param fieldMask 要标记为已计算的字段。
     * @exception IndexOutOfBoundsException 如果指定的 <code>field</code> 超出范围
     *               (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>)。
     * @see #isExternallySet(int)
     * @see #selectFields()
     */
    final void setFieldsComputed(int fieldMask) {
        if (fieldMask == ALL_FIELDS) {
            for (int i = 0; i < fields.length; i++) {
                stamp[i] = COMPUTED;
                isSet[i] = true;
            }
            areFieldsSet = areAllFieldsSet = true;
        } else {
            for (int i = 0; i < fields.length; i++) {
                if ((fieldMask & 1) == 1) {
                    stamp[i] = COMPUTED;
                    isSet[i] = true;
                } else {
                    if (areAllFieldsSet && !isSet[i]) {
                        areAllFieldsSet = false;
                    }
                }
                fieldMask >>>= 1;
            }
        }
    }


                /**
     * 将日历字段中未由 <code>fieldMask</code> 指定的字段状态设置为 <em>未设置</em>。如果 <code>fieldMask</code>
     * 指定了所有日历字段，则此 <code>Calendar</code> 的状态变为所有日历字段与时间值（从纪元开始的毫秒偏移量）同步。
     *
     * @param fieldMask 表示哪些日历字段与时间值同步的字段掩码。
     * @exception IndexOutOfBoundsException 如果指定的 <code>field</code> 超出范围
     *               (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
     * @see #isExternallySet(int)
     * @see #selectFields()
     */
    final void setFieldsNormalized(int fieldMask) {
        if (fieldMask != ALL_FIELDS) {
            for (int i = 0; i < fields.length; i++) {
                if ((fieldMask & 1) == 0) {
                    stamp[i] = fields[i] = 0; // UNSET == 0
                    isSet[i] = false;
                }
                fieldMask >>= 1;
            }
        }

        // 一些或所有字段与毫秒同步，但时间戳值尚未标准化。
        areFieldsSet = true;
        areAllFieldsSet = false;
    }

    /**
     * 返回日历字段是否部分与时间值同步或完全同步但时间戳值尚未标准化。
     */
    final boolean isPartiallyNormalized() {
        return areFieldsSet && !areAllFieldsSet;
    }

    /**
     * 返回日历字段是否完全与时间值同步。
     */
    final boolean isFullyNormalized() {
        return areFieldsSet && areAllFieldsSet;
    }

    /**
     * 标记此日历为不同步。
     */
    final void setUnnormalized() {
        areFieldsSet = areAllFieldsSet = false;
    }

    /**
     * 返回指定的 <code>field</code> 是否在 <code>fieldMask</code> 中设置。
     */
    static boolean isFieldSet(int fieldMask, int field) {
        return (fieldMask & (1 << field)) != 0;
    }

    /**
     * 返回一个表示哪些日历字段值用于计算时间值的字段掩码。日历字段作为位掩码返回，每个位对应一个字段，即，
     * 字段 <code>field</code> 的掩码值为 <code>(1 &lt;&lt; field)</code>。例如，0x26 表示
     * <code>YEAR</code>、<code>MONTH</code> 和 <code>DAY_OF_MONTH</code> 字段（即，0x26 等于
     * <code>(1&lt;&lt;YEAR)|(1&lt;&lt;MONTH)|(1&lt;&lt;DAY_OF_MONTH))</code>。
     *
     * <p>此方法支持类描述中所述的日历字段解析。如果给定字段的位掩码已设置且该字段尚未设置（即，<code>isSet(field)</code> 为
     * <code>false</code>），则必须使用该字段的默认值，这意味着该字段已被选中，因为所选组合涉及该字段。
     *
     * @return 选定字段的位掩码
     * @see #isExternallySet(int)
     */
    final int selectFields() {
        // 此实现取自 GregorianCalendar 类。

        // 无论其 SET 状态如何，YEAR 字段必须始终使用，因为 YEAR 是确定日期的必要字段
        // 并且默认值（EPOCH_YEAR）可能在标准化过程中发生变化。
        int fieldMask = YEAR_MASK;

        if (stamp[ERA] != UNSET) {
            fieldMask |= ERA_MASK;
        }
        // 查找指定年份内某天的最近一组字段。这些可以是以下组合之一：
        //   MONTH + DAY_OF_MONTH
        //   MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
        //   MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
        //   DAY_OF_YEAR
        //   WEEK_OF_YEAR + DAY_OF_WEEK
        // 我们查找每个组中的最近字段以确定组的年龄。对于涉及周相关字段（如 WEEK_OF_MONTH、DAY_OF_WEEK_IN_MONTH 或 WEEK_OF_YEAR）的组，
        // 必须同时设置周相关字段和 DAY_OF_WEEK，整个组才被视为已设置。（参见 bug 4153860 - liu 7/24/98。）
        int dowStamp = stamp[DAY_OF_WEEK];
        int monthStamp = stamp[MONTH];
        int domStamp = stamp[DAY_OF_MONTH];
        int womStamp = aggregateStamp(stamp[WEEK_OF_MONTH], dowStamp);
        int dowimStamp = aggregateStamp(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
        int doyStamp = stamp[DAY_OF_YEAR];
        int woyStamp = aggregateStamp(stamp[WEEK_OF_YEAR], dowStamp);

        int bestStamp = domStamp;
        if (womStamp > bestStamp) {
            bestStamp = womStamp;
        }
        if (dowimStamp > bestStamp) {
            bestStamp = dowimStamp;
        }
        if (doyStamp > bestStamp) {
            bestStamp = doyStamp;
        }
        if (woyStamp > bestStamp) {
            bestStamp = woyStamp;
        }

        /* 不存在完整的组合。查找 WEEK_OF_MONTH、DAY_OF_WEEK_IN_MONTH 或 WEEK_OF_YEAR 单独的情况。
         * 将单独的 DAY_OF_WEEK 视为 DAY_OF_WEEK_IN_MONTH。
         */
        if (bestStamp == UNSET) {
            womStamp = stamp[WEEK_OF_MONTH];
            dowimStamp = Math.max(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
            woyStamp = stamp[WEEK_OF_YEAR];
            bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);

            /* 将单独的 MONTH 或没有字段的情况视为 DAY_OF_MONTH。如果未设置任何字段，这可能导致 bestStamp = domStamp = UNSET，
             * 表示 DAY_OF_MONTH。
             */
            if (bestStamp == UNSET) {
                bestStamp = domStamp = monthStamp;
            }
        }

        if (bestStamp == domStamp ||
           (bestStamp == womStamp && stamp[WEEK_OF_MONTH] >= stamp[WEEK_OF_YEAR]) ||
           (bestStamp == dowimStamp && stamp[DAY_OF_WEEK_IN_MONTH] >= stamp[WEEK_OF_YEAR])) {
            fieldMask |= MONTH_MASK;
            if (bestStamp == domStamp) {
                fieldMask |= DAY_OF_MONTH_MASK;
            } else {
                assert (bestStamp == womStamp || bestStamp == dowimStamp);
                if (dowStamp != UNSET) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                if (womStamp == dowimStamp) {
                    // 当它们相等时，为了兼容性，优先选择 WEEK_OF_MONTH。
                    if (stamp[WEEK_OF_MONTH] >= stamp[DAY_OF_WEEK_IN_MONTH]) {
                        fieldMask |= WEEK_OF_MONTH_MASK;
                    } else {
                        fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                    }
                } else {
                    if (bestStamp == womStamp) {
                        fieldMask |= WEEK_OF_MONTH_MASK;
                    } else {
                        assert (bestStamp == dowimStamp);
                        if (stamp[DAY_OF_WEEK_IN_MONTH] != UNSET) {
                            fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
                        }
                    }
                }
            }
        } else {
            assert (bestStamp == doyStamp || bestStamp == woyStamp ||
                    bestStamp == UNSET);
            if (bestStamp == doyStamp) {
                fieldMask |= DAY_OF_YEAR_MASK;
            } else {
                assert (bestStamp == woyStamp);
                if (dowStamp != UNSET) {
                    fieldMask |= DAY_OF_WEEK_MASK;
                }
                fieldMask |= WEEK_OF_YEAR_MASK;
            }
        }

        // 查找指定一天时间的最合适的字段组。这里只有两种可能性；HOUR_OF_DAY 或 AM_PM 和 HOUR。
        int hourOfDayStamp = stamp[HOUR_OF_DAY];
        int hourStamp = aggregateStamp(stamp[HOUR], stamp[AM_PM]);
        bestStamp = (hourStamp > hourOfDayStamp) ? hourStamp : hourOfDayStamp;

        // 如果 bestStamp 仍为 UNSET，则取 HOUR 或 AM_PM。（参见 4846659）
        if (bestStamp == UNSET) {
            bestStamp = Math.max(stamp[HOUR], stamp[AM_PM]);
        }

        // 小时
        if (bestStamp != UNSET) {
            if (bestStamp == hourOfDayStamp) {
                fieldMask |= HOUR_OF_DAY_MASK;
            } else {
                fieldMask |= HOUR_MASK;
                if (stamp[AM_PM] != UNSET) {
                    fieldMask |= AM_PM_MASK;
                }
            }
        }
        if (stamp[MINUTE] != UNSET) {
            fieldMask |= MINUTE_MASK;
        }
        if (stamp[SECOND] != UNSET) {
            fieldMask |= SECOND_MASK;
        }
        if (stamp[MILLISECOND] != UNSET) {
            fieldMask |= MILLISECOND_MASK;
        }
        if (stamp[ZONE_OFFSET] >= MINIMUM_USER_STAMP) {
                fieldMask |= ZONE_OFFSET_MASK;
        }
        if (stamp[DST_OFFSET] >= MINIMUM_USER_STAMP) {
            fieldMask |= DST_OFFSET_MASK;
        }

        return fieldMask;
    }

    int getBaseStyle(int style) {
        return style & ~STANDALONE_MASK;
    }

    private int toStandaloneStyle(int style) {
        return style | STANDALONE_MASK;
    }

    private boolean isStandaloneStyle(int style) {
        return (style & STANDALONE_MASK) != 0;
    }

    private boolean isNarrowStyle(int style) {
        return style == NARROW_FORMAT || style == NARROW_STANDALONE;
    }

    private boolean isNarrowFormatStyle(int style) {
        return style == NARROW_FORMAT;
    }

    /**
     * 返回两个字段的伪时间戳，给定它们各自的伪时间戳。如果任一字段未设置，则聚合结果为未设置。否则，
     * 聚合结果为两个时间戳中较晚的一个。
     */
    private static int aggregateStamp(int stamp_a, int stamp_b) {
        if (stamp_a == UNSET || stamp_b == UNSET) {
            return UNSET;
        }
        return (stamp_a > stamp_b) ? stamp_a : stamp_b;
    }

    /**
     * 返回一个不可修改的 {@code Set}，包含运行时环境中 {@code Calendar} 支持的所有日历类型。可用的日历类型可以用于
     * <a href="Locale.html#def_locale_extension">Unicode 语言环境扩展</a>。返回的 {@code Set} 至少包含 {@code "gregory"}。
     * 日历类型不包括别名，如将 {@code "gregorian"} 作为 {@code "gregory"} 的别名。
     *
     * @return 包含所有可用日历类型的不可修改的 {@code Set}
     * @since 1.8
     * @see #getCalendarType()
     * @see Calendar.Builder#setCalendarType(String)
     * @see Locale#getUnicodeLocaleType(String)
     */
    public static Set<String> getAvailableCalendarTypes() {
        return AvailableCalendarTypes.SET;
    }

    private static class AvailableCalendarTypes {
        private static final Set<String> SET;
        static {
            Set<String> set = new HashSet<>(3);
            set.add("gregory");
            set.add("buddhist");
            set.add("japanese");
            SET = Collections.unmodifiableSet(set);
        }
        private AvailableCalendarTypes() {
        }
    }

    /**
     * 返回此 {@code Calendar} 的日历类型。日历类型由 <em>Unicode 语言环境数据标记语言 (LDML)</em>
     * 规范定义。
     *
     * <p>此方法的默认实现返回此 {@code Calendar} 实例的类名。任何实现 LDML 定义的日历系统的子类应覆盖此方法以返回适当的日历类型。
     *
     * @return LDML 定义的日历类型或此 {@code Calendar} 实例的类名
     * @since 1.8
     * @see <a href="Locale.html#def_extensions">语言环境扩展</a>
     * @see Locale.Builder#setLocale(Locale)
     * @see Locale.Builder#setUnicodeLocaleKeyword(String, String)
     */
    public String getCalendarType() {
        return this.getClass().getName();
    }

    /**
     * 比较此 <code>Calendar</code> 与指定的 <code>Object</code>。结果为 <code>true</code> 当且仅当
     * 参数是一个与同一日历系统表示相同时间值（从 <a href="#Epoch">纪元</a> 开始的毫秒偏移量）且具有相同
     * <code>Calendar</code> 参数的 <code>Calendar</code> 对象。
     *
     * <p><code>Calendar</code> 参数是 <code>isLenient</code>、<code>getFirstDayOfWeek</code>、
     * <code>getMinimalDaysInFirstWeek</code> 和 <code>getTimeZone</code> 方法表示的值。如果两个 <code>Calendar</code>
     * 之间的这些参数有任何不同，此方法返回 <code>false</code>。
     *
     * <p>使用 {@link #compareTo(Calendar) compareTo} 方法仅比较时间值。
     *
     * @param obj 要比较的对象。
     * @return 如果此对象等于 <code>obj</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        try {
            Calendar that = (Calendar)obj;
            return compareTo(getMillisOf(that)) == 0 &&
                lenient == that.lenient &&
                firstDayOfWeek == that.firstDayOfWeek &&
                minimalDaysInFirstWeek == that.minimalDaysInFirstWeek &&
                (zone instanceof ZoneInfo ?
                    zone.equals(that.zone) :
                    zone.equals(that.getTimeZone()));
        } catch (Exception e) {
            // 注意：GregorianCalendar.computeTime 在 ERA 值无效时抛出
            // IllegalArgumentException，即使在宽松模式下也是如此。
        }
        return false;
    }

    /**
     * 返回此日历的哈希码。
     *
     * @return 此对象的哈希码值。
     * @since 1.2
     */
    @Override
    public int hashCode() {
        // 'otheritems' 表示以前版本的哈希码。
        int otheritems = (lenient ? 1 : 0)
            | (firstDayOfWeek << 1)
            | (minimalDaysInFirstWeek << 4)
            | (zone.hashCode() << 7);
        long t = getMillisOf(this);
        return (int) t ^ (int)(t >> 32) ^ otheritems;
    }

    /**
     * 返回此 <code>Calendar</code> 表示的时间是否在指定 <code>Object</code> 表示的时间之前。此方法等价于：
     * <pre>{@code
     *         compareTo(when) < 0
     * }</pre>
     * 当且仅当 <code>when</code> 是一个 <code>Calendar</code> 实例时。否则，此方法返回 <code>false</code>。
     *
     * @param when 要比较的对象
     * @return 如果此 <code>Calendar</code> 的时间在 <code>when</code> 表示的时间之前，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @see     #compareTo(Calendar)
     */
    public boolean before(Object when) {
        return when instanceof Calendar
            && compareTo((Calendar)when) < 0;
    }


                /**
     * 返回此 <code>Calendar</code> 是否表示一个时间
     * 在指定的 <code>Object</code> 表示的时间之后。此方法等同于：
     * <pre>{@code
     *         compareTo(when) > 0
     * }</pre>
     * 当且仅当 <code>when</code> 是一个 <code>Calendar</code>
     * 实例时。否则，该方法返回 <code>false</code>。
     *
     * @param when 要比较的 <code>Object</code>
     * @return <code>true</code> 如果此 <code>Calendar</code> 的时间
     * 在 <code>when</code> 表示的时间之后；否则返回 <code>false</code>。
     * @see     #compareTo(Calendar)
     */
    public boolean after(Object when) {
        return when instanceof Calendar
            && compareTo((Calendar)when) > 0;
    }

    /**
     * 比较两个 <code>Calendar</code> 对象表示的时间值（从 <a
     * href="#Epoch">纪元</a> 开始的毫秒偏移量）。
     *
     * @param anotherCalendar 要比较的 <code>Calendar</code>。
     * @return 值 <code>0</code> 表示参数表示的时间
     * 与此 <code>Calendar</code> 表示的时间相等；值
     * 小于 <code>0</code> 表示此 <code>Calendar</code> 的时间
     * 在参数表示的时间之前；值大于
     * <code>0</code> 表示此 <code>Calendar</code> 的时间
     * 在参数表示的时间之后。
     * @exception NullPointerException 如果指定的 <code>Calendar</code> 是
     *            <code>null</code>。
     * @exception IllegalArgumentException 如果指定的 <code>Calendar</code> 对象的时间值
     * 由于任何无效的日历值而无法获取。
     * @since   1.5
     */
    @Override
    public int compareTo(Calendar anotherCalendar) {
        return compareTo(getMillisOf(anotherCalendar));
    }

    /**
     * 根据日历的规则，向给定的日历字段添加或减去指定的时间量。例如，要从当前时间减去 5 天，可以通过调用：
     * <p><code>add(Calendar.DAY_OF_MONTH, -5)</code> 来实现。
     *
     * @param field 日历字段。
     * @param amount 要添加到字段的日期或时间量。
     * @see #roll(int,int)
     * @see #set(int,int)
     */
    abstract public void add(int field, int amount);

    /**
     * 在给定的时间字段上添加或减去（上下）一个时间单位，而不改变较大的字段。例如，要将当前日期向上滚动一天，可以通过调用：
     * <p>roll(Calendar.DATE, true) 来实现。
     * 当在年或 Calendar.YEAR 字段上滚动时，年份值将在 1 和调用
     * <code>getMaximum(Calendar.YEAR)</code> 返回的值之间滚动。
     * 当在月或 Calendar.MONTH 字段上滚动时，其他字段（如日期）可能会冲突并需要更改。例如，
     * 在 01/31/96 上滚动月份将导致 02/29/96。
     * 当在一天中的小时或 Calendar.HOUR_OF_DAY 字段上滚动时，小时值将在 0 到 23 之间滚动，这是以零为基准的。
     *
     * @param field 时间字段。
     * @param up 指示指定的时间字段的值是向上滚动还是向下滚动。使用 true 表示向上滚动，否则为 false。
     * @see Calendar#add(int,int)
     * @see Calendar#set(int,int)
     */
    abstract public void roll(int field, boolean up);

    /**
     * 向指定的日历字段添加指定的（带符号的）量，而不改变较大的字段。负量表示向下滚动。
     *
     * <p>注意：此 <code>Calendar</code> 上的默认实现只是反复调用
     * 版本的 {@link #roll(int,boolean) roll()}，该版本每次滚动一个单位。这可能不会总是做正确的事情。例如，如果 <code>DAY_OF_MONTH</code> 字段是 31，
     * 通过二月滚动将使其设置为 28。 <code>GregorianCalendar</code> 版本的此函数解决了此问题。其他子类
     * 也应该提供此函数的覆盖版本，以确保正确的行为。
     *
     * @param field 日历字段。
     * @param amount 要添加到日历 <code>field</code> 的带符号量。
     * @since 1.2
     * @see #roll(int,boolean)
     * @see #add(int,int)
     * @see #set(int,int)
     */
    public void roll(int field, int amount)
    {
        while (amount > 0) {
            roll(field, true);
            amount--;
        }
        while (amount < 0) {
            roll(field, false);
            amount++;
        }
    }

    /**
     * 使用给定的时区值设置时区。
     *
     * @param value 给定的时区。
     */
    public void setTimeZone(TimeZone value)
    {
        zone = value;
        sharedZone = false;
        /* 使用新时区重新计算时间的字段。这在 isTimeSet 为 false（在调用 set() 之后）时也有效。在这种情况下，
         * 将使用新时区从字段计算时间，然后从该时间重新计算字段。考虑以下调用序列：cal.setTimeZone(EST); cal.set(HOUR, 1); cal.setTimeZone(PST)。
         * cal 是设置为 1 点 EST 还是 1 点 PST？答案是 PST。更一般地说，调用 setTimeZone() 会影响
         * 在其之前和之后的 set() 调用，直到下一次调用 complete()。
         */
        areAllFieldsSet = areFieldsSet = false;
    }

    /**
     * 获取时区。
     *
     * @return 与此日历关联的时区对象。
     */
    public TimeZone getTimeZone()
    {
        // 如果时区对象被其他日历实例共享，则创建一个克隆。
        if (sharedZone) {
            zone = (TimeZone) zone.clone();
            sharedZone = false;
        }
        return zone;
    }

    /**
     * 返回时区（不进行克隆）。
     */
    TimeZone getZone() {
        return zone;
    }

    /**
     * 设置 sharedZone 标志为 <code>shared</code>。
     */
    void setZoneShared(boolean shared) {
        sharedZone = shared;
    }

    /**
     * 指定日期/时间解释是否应宽松。在宽松解释模式下，像 "1996 年 2 月 942 日" 这样的日期将被视为
     * 1996 年 2 月 1 日之后的第 941 天。在严格（非宽松）解释模式下，这样的日期将导致抛出异常。默认为宽松模式。
     *
     * @param lenient <code>true</code> 表示开启宽松模式；<code>false</code> 表示关闭宽松模式。
     * @see #isLenient()
     * @see java.text.DateFormat#setLenient
     */
    public void setLenient(boolean lenient)
    {
        this.lenient = lenient;
    }

    /**
     * 告诉日期/时间解释是否应宽松。
     *
     * @return <code>true</code> 表示此日历的解释模式为宽松；
     * <code>false</code> 表示非宽松。
     * @see #setLenient(boolean)
     */
    public boolean isLenient()
    {
        return lenient;
    }

    /**
     * 设置一周的第一天；例如，在美国为 <code>SUNDAY</code>，在法国为 <code>MONDAY</code>。
     *
     * @param value 给定的一周的第一天。
     * @see #getFirstDayOfWeek()
     * @see #getMinimalDaysInFirstWeek()
     */
    public void setFirstDayOfWeek(int value)
    {
        if (firstDayOfWeek == value) {
            return;
        }
        firstDayOfWeek = value;
        invalidateWeekFields();
    }

    /**
     * 获取一周的第一天；例如，在美国为 <code>SUNDAY</code>，在法国为 <code>MONDAY</code>。
     *
     * @return 一周的第一天。
     * @see #setFirstDayOfWeek(int)
     * @see #getMinimalDaysInFirstWeek()
     */
    public int getFirstDayOfWeek()
    {
        return firstDayOfWeek;
    }

    /**
     * 设置一年中第一周所需的最小天数；
     * 例如，如果第一周定义为包含一年中第一个月的第一天，调用此方法时使用值 1。如果
     * 必须是完整的一周，使用值 7。
     *
     * @param value 给定的一年中第一周所需的最小天数。
     * @see #getMinimalDaysInFirstWeek()
     */
    public void setMinimalDaysInFirstWeek(int value)
    {
        if (minimalDaysInFirstWeek == value) {
            return;
        }
        minimalDaysInFirstWeek = value;
        invalidateWeekFields();
    }

    /**
     * 获取一年中第一周所需的最小天数；
     * 例如，如果第一周定义为包含一年中第一个月的第一天，此方法返回 1。如果
     * 所需的最小天数必须是完整的一周，此方法返回 7。
     *
     * @return 一年中第一周所需的最小天数。
     * @see #setMinimalDaysInFirstWeek(int)
     */
    public int getMinimalDaysInFirstWeek()
    {
        return minimalDaysInFirstWeek;
    }

    /**
     * 返回此 {@code Calendar} 是否支持周日期。
     *
     * <p>此方法的默认实现返回 {@code false}。
     *
     * @return {@code true} 如果此 {@code Calendar} 支持周日期；
     *         {@code false} 否则。
     * @see #getWeekYear()
     * @see #setWeekDate(int,int,int)
     * @see #getWeeksInWeekYear()
     * @since 1.7
     */
    public boolean isWeekDateSupported() {
        return false;
    }

    /**
     * 返回此 {@code Calendar} 表示的周年代。周年代与周周期同步。一周的第一天
     * 是周年代的第一天。
     *
     * <p>此方法的默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @return 此 {@code Calendar} 的周年代
     * @exception UnsupportedOperationException
     *            如果此 {@code Calendar} 不支持任何周年代编号。
     * @see #isWeekDateSupported()
     * @see #getFirstDayOfWeek()
     * @see #getMinimalDaysInFirstWeek()
     * @since 1.7
     */
    public int getWeekYear() {
        throw new UnsupportedOperationException();
    }

    /**
     * 使用给定的日期规范符 - 周年代、一年中的周数和一周中的天数 - 设置此 {@code Calendar} 的日期。
     *
     * <p>与 {@code set} 方法不同，所有日历字段和 {@code time} 值在返回时都会被计算。
     *
     * <p>如果 {@code weekOfYear} 超出 {@code weekYear} 中的有效周数范围，
     * 在宽松模式下，{@code weekYear} 和 {@code weekOfYear} 值将被调整，或在非宽松模式下抛出 {@code
     * IllegalArgumentException}。
     *
     * <p>此方法的默认实现抛出 {@code UnsupportedOperationException}。
     *
     * @param weekYear   周年代
     * @param weekOfYear 基于 {@code weekYear} 的周数
     * @param dayOfWeek  一周中的天数值：为 {@link #DAY_OF_WEEK} 字段的常量之一：{@link
     *                   #SUNDAY}，...，{@link #SATURDAY}。
     * @exception IllegalArgumentException
     *            如果给定的任何日期规范符无效
     *            或在非宽松模式下，任何日历字段与给定的日期规范符不一致
     * @exception UnsupportedOperationException
     *            如果此 {@code Calendar} 不支持任何周年代编号。
     * @see #isWeekDateSupported()
     * @see #getFirstDayOfWeek()
     * @see #getMinimalDaysInFirstWeek()
     * @since 1.7
     */
    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回此 {@code Calendar} 表示的周年代中的周数。
     *
     * <p>此方法的默认实现抛出 {@code UnsupportedOperationException}。
     *
     * @return 周年代中的周数。
     * @exception UnsupportedOperationException
     *            如果此 {@code Calendar} 不支持任何周年代编号。
     * @see #WEEK_OF_YEAR
     * @see #isWeekDateSupported()
     * @see #getWeekYear()
     * @see #getActualMaximum(int)
     * @since 1.7
     */
    public int getWeeksInWeekYear() {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回此 <code>Calendar</code> 实例给定日历字段的最小值。最小值定义为
     * 通过 {@link #get(int) get} 方法返回的任何可能时间值中的最小值。最小值取决于
     * 实例的特定日历系统参数。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最小值。
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    abstract public int getMinimum(int field);

    /**
     * 返回此 <code>Calendar</code> 实例给定日历字段的最大值。最大值定义为
     * 通过 {@link #get(int) get} 方法返回的任何可能时间值中的最大值。最大值取决于
     * 实例的特定日历系统参数。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最大值。
     * @see #getMinimum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    abstract public int getMaximum(int field);

    /**
     * 返回此 <code>Calendar</code> 实例给定日历字段的最高最小值。最高最小值定义为
     * 通过 {@link #getActualMinimum(int)} 返回的任何可能时间值中的最大值。最高最小值取决于
     * 实例的特定日历系统参数。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最高最小值。
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    abstract public int getGreatestMinimum(int field);

    /**
     * 返回此 <code>Calendar</code> 实例给定日历字段的最低最大值。最低最大值定义为
     * 通过 {@link #getActualMaximum(int)} 返回的任何可能时间值中的最小值。最低最大值取决于
     * 实例的特定日历系统参数。例如，对于
     * 格里高利历系统的 <code>Calendar</code>，对于 <code>DAY_OF_MONTH</code> 字段，返回 28，
     * 因为这是此日历中最短月份的最后一天，即普通年份的二月 28 日。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最低最大值。
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    abstract public int getLeastMaximum(int field);


    /**
     * 返回指定日历字段在当前 <code>Calendar</code> 时间值下可能具有的最小值。
     *
     * <p>此方法的默认实现使用迭代算法来确定日历字段的实际最小值。子类应尽可能覆盖此方法以实现更高效的算法 - 在许多情况下，它们可以简单地返回 <code>getMinimum()</code>。
     *
     * @param field 日历字段
     * @return 当前 <code>Calendar</code> 时间值下指定日历字段的最小值
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMaximum(int)
     * @since 1.2
     */
    public int getActualMinimum(int field) {
        int fieldValue = getGreatestMinimum(field);
        int endValue = getMinimum(field);

        // 如果已知最小值总是相同的，直接返回它
        if (fieldValue == endValue) {
            return fieldValue;
        }

        // 克隆日历以避免影响实际的日历，并设置它接受任何字段值
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);

        // 从 getLeastMaximum() 到 getMaximum() 逐个尝试每个值，直到找到一个规范化为另一个值的值。最后一个规范化为其自身的值是当前日期的实际最小值
        int result = fieldValue;

        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            } else {
                result = fieldValue;
                fieldValue--;
            }
        } while (fieldValue >= endValue);

        return result;
    }

    /**
     * 返回指定日历字段在当前 <code>Calendar</code> 时间值下可能具有的最大值。例如，在希伯来日历系统中，某些年份的 <code>MONTH</code> 字段的最大值为 12，而其他年份为 13。
     *
     * <p>此方法的默认实现使用迭代算法来确定日历字段的实际最大值。子类应尽可能覆盖此方法以实现更高效的算法。
     *
     * @param field 日历字段
     * @return 当前 <code>Calendar</code> 时间值下指定日历字段的最大值
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @since 1.2
     */
    public int getActualMaximum(int field) {
        int fieldValue = getLeastMaximum(field);
        int endValue = getMaximum(field);

        // 如果已知最大值总是相同的，直接返回它
        if (fieldValue == endValue) {
            return fieldValue;
        }

        // 克隆日历以避免影响实际的日历，并设置它接受任何字段值
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);

        // 如果计算周数，将星期几设置为星期日。我们知道一个月或一年的最后一周将包含一周的第一天
        if (field == WEEK_OF_YEAR || field == WEEK_OF_MONTH) {
            work.set(DAY_OF_WEEK, firstDayOfWeek);
        }

        // 从 getLeastMaximum() 到 getMaximum() 逐个尝试每个值，直到找到一个规范化为另一个值的值。最后一个规范化为其自身的值是当前日期的实际最大值
        int result = fieldValue;

        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) {
                break;
            } else {
                result = fieldValue;
                fieldValue++;
            }
        } while (fieldValue <= endValue);

        return result;
    }

    /**
     * 创建并返回此对象的副本。
     *
     * @return 此对象的副本。
     */
    @Override
    public Object clone()
    {
        try {
            Calendar other = (Calendar) super.clone();

            other.fields = new int[FIELD_COUNT];
            other.isSet = new boolean[FIELD_COUNT];
            other.stamp = new int[FIELD_COUNT];
            for (int i = 0; i < FIELD_COUNT; i++) {
                other.fields[i] = fields[i];
                other.stamp[i] = stamp[i];
                other.isSet[i] = isSet[i];
            }
            other.zone = (TimeZone) zone.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable 接口
            throw new InternalError(e);
        }
    }

    private static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH",
        "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR",
        "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET"
    };

    /**
     * 返回指定日历字段的名称。
     *
     * @param field 日历字段
     * @return 日历字段名称
     * @exception IndexOutOfBoundsException 如果 <code>field</code> 为负数，等于或大于 <code>FIELD_COUNT</code>。
     */
    static String getFieldName(int field) {
        return FIELD_NAME[field];
    }

    /**
     * 返回此日历的字符串表示形式。此方法仅用于调试目的，返回字符串的格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return 此日历的字符串表示形式。
     */
    @Override
    public String toString() {
        // 注意：BuddhistCalendar.toString() 解释此方法生成的字符串，将格里高利历年份替换为佛历年的值。它依赖于 "...,YEAR=<year>,..." 或 "...,YEAR=?,..."。
        StringBuilder buffer = new StringBuilder(800);
        buffer.append(getClass().getName()).append('[');
        appendValue(buffer, "time", isTimeSet, time);
        buffer.append(",areFieldsSet=").append(areFieldsSet);
        buffer.append(",areAllFieldsSet=").append(areAllFieldsSet);
        buffer.append(",lenient=").append(lenient);
        buffer.append(",zone=").append(zone);
        appendValue(buffer, ",firstDayOfWeek", true, (long) firstDayOfWeek);
        appendValue(buffer, ",minimalDaysInFirstWeek", true, (long) minimalDaysInFirstWeek);
        for (int i = 0; i < FIELD_COUNT; ++i) {
            buffer.append(',');
            appendValue(buffer, FIELD_NAME[i], isSet(i), (long) fields[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    // =======================privates===============================

    private static void appendValue(StringBuilder sb, String item, boolean valid, long value) {
        sb.append(item).append('=');
        if (valid) {
            sb.append(value);
        } else {
            sb.append('?');
        }
    }

    /**
     * firstDayOfWeek 和 minimalDaysInFirstWeek 都是与区域设置相关的。它们用于确定特定日期在给定区域设置下的周数。这些值在构造 Calendar 时必须设置。
     * @param desiredLocale 给定的区域设置。
     */
    private void setWeekCountData(Locale desiredLocale)
    {
        /* 尝试从缓存中获取区域设置数据 */
        int[] data = cachedLocaleData.get(desiredLocale);
        if (data == null) {  /* 缓存未命中 */
            data = new int[2];
            data[0] = CalendarDataUtility.retrieveFirstDayOfWeek(desiredLocale);
            data[1] = CalendarDataUtility.retrieveMinimalDaysInFirstWeek(desiredLocale);
            cachedLocaleData.putIfAbsent(desiredLocale, data);
        }
        firstDayOfWeek = data[0];
        minimalDaysInFirstWeek = data[1];
    }

    /**
     * 重新计算时间并更新 isTimeSet 和 areFieldsSet 状态字段。调用者应检查 isTimeSet，只有在 isTimeSet 为 false 时才调用此方法。
     */
    private void updateTime() {
        computeTime();
        // 从 1.5 版本开始，areFieldsSet 和 areAllFieldsSet 的值不再在此处控制。
        isTimeSet = true;
    }

    private int compareTo(long t) {
        long thisTime = getMillisOf(this);
        return (thisTime > t) ? 1 : (thisTime == t) ? 0 : -1;
    }

    private static long getMillisOf(Calendar calendar) {
        if (calendar.isTimeSet) {
            return calendar.time;
        }
        Calendar cal = (Calendar) calendar.clone();
        cal.setLenient(true);
        return cal.getTimeInMillis();
    }

    /**
     * 在 nextStamp 溢出前调整 stamp[] 值。返回时，nextStamp 被设置为下一个戳值。
     */
    private void adjustStamp() {
        int max = MINIMUM_USER_STAMP;
        int newStamp = MINIMUM_USER_STAMP;

        for (;;) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < stamp.length; i++) {
                int v = stamp[i];
                if (v >= newStamp && min > v) {
                    min = v;
                }
                if (max < v) {
                    max = v;
                }
            }
            if (max != min && min == Integer.MAX_VALUE) {
                break;
            }
            for (int i = 0; i < stamp.length; i++) {
                if (stamp[i] == min) {
                    stamp[i] = newStamp;
                }
            }
            newStamp++;
            if (min == max) {
                break;
            }
        }
        nextStamp = newStamp;
    }

    /**
     * 如果 WEEK_OF_MONTH 和 WEEK_OF_YEAR 字段已内部计算，则使用新参数值设置这些字段的新值。
     */
    private void invalidateWeekFields()
    {
        if (stamp[WEEK_OF_MONTH] != COMPUTED &&
            stamp[WEEK_OF_YEAR] != COMPUTED) {
            return;
        }

        // 在更改 firstDayOfWeek 和/或 minimalDaysInFirstWeek 后，必须检查这些字段的新值。如果字段值已更改，则设置新值。 (4822110)
        Calendar cal = (Calendar) clone();
        cal.setLenient(true);
        cal.clear(WEEK_OF_MONTH);
        cal.clear(WEEK_OF_YEAR);

        if (stamp[WEEK_OF_MONTH] == COMPUTED) {
            int weekOfMonth = cal.get(WEEK_OF_MONTH);
            if (fields[WEEK_OF_MONTH] != weekOfMonth) {
                fields[WEEK_OF_MONTH] = weekOfMonth;
            }
        }

        if (stamp[WEEK_OF_YEAR] == COMPUTED) {
            int weekOfYear = cal.get(WEEK_OF_YEAR);
            if (fields[WEEK_OF_YEAR] != weekOfYear) {
                fields[WEEK_OF_YEAR] = weekOfYear;
            }
        }
    }

    /**
     * 将此对象的状态保存到流中（即序列化它）。
     *
     * 理想情况下，<code>Calendar</code> 只应写入其状态数据和当前时间，而不写入任何字段数据，如 <code>fields[]</code>、<code>isTimeSet</code>、<code>areFieldsSet</code> 和 <code>isSet[]</code>。<code>nextStamp</code> 也不应成为持久状态的一部分。不幸的是，在 JDK 1.1 发布前，这并没有实现。为了与 JDK 1.1 兼容，我们始终必须写入字段值和状态标志。<code>nextStamp</code> 可以从序列化流中移除；这可能会在未来不久实现。
     */
    private synchronized void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        // 尝试正确计算时间，以备将来（流版本 2）使用，届时不再写入 fields[] 或 isSet[]。
        if (!isTimeSet) {
            try {
                updateTime();
            }
            catch (IllegalArgumentException e) {}
        }

        // 如果此 Calendar 有一个 ZoneInfo，保存它并设置一个 SimpleTimeZone 等效项（作为单个 DST 调度）以实现向后兼容。
        TimeZone savedZone = null;
        if (zone instanceof ZoneInfo) {
            SimpleTimeZone stz = ((ZoneInfo)zone).getLastRuleInstance();
            if (stz == null) {
                stz = new SimpleTimeZone(zone.getRawOffset(), zone.getID());
            }
            savedZone = zone;
            zone = stz;
        }

        // 写入 1.1 FCS 对象。
        stream.defaultWriteObject();

        // 写入 ZoneInfo 对象
        // 4802409: 即使为 null 也写入，作为临时解决方法
        // 4844924 在 corba-iiop 中的真正修复
        stream.writeObject(savedZone);
        if (savedZone != null) {
            zone = savedZone;
        }
    }

    private static class CalendarAccessControlContext {
        private static final AccessControlContext INSTANCE;
        static {
            RuntimePermission perm = new RuntimePermission("accessClassInPackage.sun.util.calendar");
            PermissionCollection perms = perm.newPermissionCollection();
            perms.add(perm);
            INSTANCE = new AccessControlContext(new ProtectionDomain[] {
                                                    new ProtectionDomain(null, perms)
                                                });
        }
        private CalendarAccessControlContext() {
        }
    }

    /**
     * 从流中重新构造此对象（即反序列化它）。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        final ObjectInputStream input = stream;
        input.defaultReadObject();

        stamp = new int[FIELD_COUNT];

        // 从版本 2 开始（尚未实现），我们期望 fields[]、isSet[]、isTimeSet 和 areFieldsSet 不再被序列化。我们期望 'time' 是正确的。
        if (serialVersionOnStream >= 2)
        {
            isTimeSet = true;
            if (fields == null) {
                fields = new int[FIELD_COUNT];
            }
            if (isSet == null) {
                isSet = new boolean[FIELD_COUNT];
            }
        }
        else if (serialVersionOnStream >= 0)
        {
            for (int i=0; i<FIELD_COUNT; ++i) {
                stamp[i] = isSet[i] ? COMPUTED : UNSET;
            }
        }

        serialVersionOnStream = currentSerialVersion;

        // 如果有 ZoneInfo 对象，使用它作为 zone。
        ZoneInfo zi = null;
        try {
            zi = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ZoneInfo>() {
                        @Override
                        public ZoneInfo run() throws Exception {
                            return (ZoneInfo) input.readObject();
                        }
                    },
                    CalendarAccessControlContext.INSTANCE);
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (!(e instanceof OptionalDataException)) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) e;
                }
                throw new RuntimeException(e);
            }
        }
        if (zi != null) {
            zone = zi;
        }
    }


                    // 如果反序列化的对象包含 SimpleTimeZone，尝试
        // 用 ZoneInfo 等效对象（自 1.4 起）替换它，以尽可能
        // 与基于 SimpleTimeZone 的实现保持兼容。
        if (zone instanceof SimpleTimeZone) {
            String id = zone.getID();
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz != null && tz.hasSameRules(zone) && tz.getID().equals(id)) {
                zone = tz;
            }
        }
    }

    /**
     * 将此对象转换为 {@link Instant}。
     * <p>
     * 转换创建一个表示与该 {@code Calendar} 相同时间点的 {@code Instant}。
     *
     * @return 表示相同时间点的即时对象
     * @since 1.8
     */
    public final Instant toInstant() {
        return Instant.ofEpochMilli(getTimeInMillis());
    }
}
