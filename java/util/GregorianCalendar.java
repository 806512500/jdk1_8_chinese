
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.JulianCalendar;
import sun.util.calendar.ZoneInfo;

/**
 * <code>GregorianCalendar</code> 是 <code>Calendar</code> 的一个具体子类，提供了世界上大多数地区使用的标准日历系统。
 *
 * <p> <code>GregorianCalendar</code> 是一个混合日历，支持 Julian 和 Gregorian 日历系统，并且支持一个单一的不连续点，该点默认对应于 Gregorian 日历的引入日期
 * （在某些国家是 1582 年 10 月 15 日，其他国家则晚一些）。调用者可以通过调用 {@link
 * #setGregorianChange(Date) setGregorianChange()} 来更改转换日期。
 *
 * <p>
 * 在最早采用 Gregorian 日历的国家中，1582 年 10 月 4 日（Julian）之后紧接着是 1582 年 10 月 15 日（Gregorian）。这个日历模型正确地反映了这一点。在 Gregorian 转换之前，<code>GregorianCalendar</code>
 * 实现了 Julian 日历。Gregorian 和 Julian 日历之间的唯一区别是闰年规则。Julian 日历规定每四年一个闰年，而 Gregorian 日历则省略了不能被 400 整除的世纪年。
 *
 * <p>
 * <code>GregorianCalendar</code> 实现了 <em>外推</em> 的 Gregorian 和 Julian 日历。也就是说，日期是通过将当前规则无限期地向前和向后外推来计算的。因此，
 * <code>GregorianCalendar</code> 可以用于所有年份，生成有意义且一致的结果。然而，使用 <code>GregorianCalendar</code> 获得的日期只有从公元 4 年 3 月 1 日起才是历史准确的，
 * 在此之前，闰年规则应用不规律，而在公元前 45 年之前，Julian 日历甚至不存在。
 *
 * <p>
 * 在 Gregorian 日历引入之前，新年是从 3 月 25 日开始的。为了避免混淆，这个日历始终使用 1 月 1 日。如果需要，可以对转换前且在 1 月 1 日到 3 月 24 日之间的日期进行手动调整。
 *
 * <h3><a name="week_and_year">周数和周年的</a></h3>
 *
 * <p>计算出的 {@link Calendar#WEEK_OF_YEAR
 * WEEK_OF_YEAR} 字段值范围从 1 到 53。一个日历年中的第一周是从 {@link
 * Calendar#getFirstDayOfWeek() getFirstDayOfWeek()} 开始的最早七天期，该期至少包含 {@link Calendar#getMinimalDaysInFirstWeek()
 * getMinimalDaysInFirstWeek()} 天。因此，它取决于 {@code getMinimalDaysInFirstWeek()}、{@code
 * getFirstDayOfWeek()} 和 1 月 1 日的星期几。第一周和下一年第一周（不包括）之间的周数从 2 到 52 或 53 依次编号
 * （除非涉及 Julian-Gregorian 转换的年份）。
 *
 * <p>{@code getFirstDayOfWeek()} 和 {@code
 * getMinimalDaysInFirstWeek()} 的值在构造 {@code
 * GregorianCalendar} 时使用区域设置依赖的资源进行初始化。<a name="iso8601_compatible_setting">当 {@code
 * getFirstDayOfWeek()} 是 {@code MONDAY} 且 {@code
 * getMinimalDaysInFirstWeek()} 是 4 时，周的确定与 ISO 8601 标准兼容</a>，这些值在偏好该标准的区域设置中使用。这些值可以通过调用 {@link Calendar#setFirstDayOfWeek(int) setFirstDayOfWeek()} 和
 * {@link Calendar#setMinimalDaysInFirstWeek(int)
 * setMinimalDaysInFirstWeek()} 显式设置。
 *
 * <p><a name="week_year"><em>周年</em></a> 与 {@code WEEK_OF_YEAR} 周期同步。第一周和最后一周（包括）之间的所有周都有相同的 <em>周年</em> 值。
 * 因此，周年第一周和最后一周的日期可能有不同的日历年值。
 *
 * <p>例如，1998 年 1 月 1 日是星期四。如果 {@code
 * getFirstDayOfWeek()} 是 {@code MONDAY} 且 {@code
 * getMinimalDaysInFirstWeek()} 是 4（ISO 8601 标准兼容设置），那么 1998 年的第一周从 1997 年 12 月 29 日开始，到 1998 年 1 月 4 日结束。1997 年最后三天的周年是 1998 年。
 * 如果 {@code getFirstDayOfWeek()} 是 {@code SUNDAY}，那么 1998 年的第一周从 1998 年 1 月 4 日开始，到 1998 年 1 月 10 日结束；1998 年的前三天则属于 1997 年的第 53 周，它们的周年是 1997 年。
 *
 * <h4>月周数</h4>
 *
 * <p>计算出的 <code>WEEK_OF_MONTH</code> 字段值范围从 0
 * 到 6。一个月的第一周（即 <code>WEEK_OF_MONTH =
 * 1</code> 的天数）是该月最早的一组至少
 * <code>getMinimalDaysInFirstWeek()</code> 天的连续天数，结束于 <code>getFirstDayOfWeek()</code> 之前的那一天。与一年的第一周不同，一个月的第一周可能短于 7 天，
 * 不一定从 <code>getFirstDayOfWeek()</code> 开始，也不会包括上个月的日期。一个月中第一周之前的日期的
 * <code>WEEK_OF_MONTH</code> 为 0。
 *
 * <p>例如，如果 <code>getFirstDayOfWeek()</code> 是 <code>SUNDAY</code>
 * 且 <code>getMinimalDaysInFirstWeek()</code> 是 4，那么 1998 年 1 月的第一周是从 1 月 4 日星期日到 1 月 10 日星期六。这些天的
 * <code>WEEK_OF_MONTH</code> 为 1。1 月 1 日星期四到 1 月 3 日星期六的
 * <code>WEEK_OF_MONTH</code> 为 0。如果将 <code>getMinimalDaysInFirstWeek()</code> 改为 3，那么 1 月 1 日到 1 月 3 日的
 * <code>WEEK_OF_MONTH</code> 为 1。
 *
 * <h4>默认字段值</h4>
 *
 * <p><code>clear</code> 方法将日历字段设置为未定义。<code>GregorianCalendar</code> 使用以下默认值，如果某个日历字段未定义，则使用这些值。
 *
 * <table cellpadding="0" cellspacing="3" border="0"
 *        summary="GregorianCalendar 默认字段值"
 *        style="text-align: left; width: 66%;">
 *   <tbody>
 *     <tr>
 *       <th style="vertical-align: top; background-color: rgb(204, 204, 255);
 *           text-align: center;">字段<br>
 *       </th>
 *       <th style="vertical-align: top; background-color: rgb(204, 204, 255);
 *           text-align: center;">默认值<br>
 *       </th>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle;">
 *              <code>ERA<br></code>
 *       </td>
 *       <td style="vertical-align: middle;">
 *              <code>AD<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle; background-color: rgb(238, 238, 255);">
 *              <code>YEAR<br></code>
 *       </td>
 *       <td style="vertical-align: middle; background-color: rgb(238, 238, 255);">
 *              <code>1970<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle;">
 *              <code>MONTH<br></code>
 *       </td>
 *       <td style="vertical-align: middle;">
 *              <code>JANUARY<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *              <code>DAY_OF_MONTH<br></code>
 *       </td>
 *       <td style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *              <code>1<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle;">
 *              <code>DAY_OF_WEEK<br></code>
 *       </td>
 *       <td style="vertical-align: middle;">
 *              <code>一周的第一天<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *              <code>WEEK_OF_MONTH<br></code>
 *       </td>
 *       <td style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *              <code>0<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top;">
 *              <code>DAY_OF_WEEK_IN_MONTH<br></code>
 *       </td>
 *       <td style="vertical-align: top;">
 *              <code>1<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle; background-color: rgb(238, 238, 255);">
 *              <code>AM_PM<br></code>
 *       </td>
 *       <td style="vertical-align: middle; background-color: rgb(238, 238, 255);">
 *              <code>AM<br></code>
 *       </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: middle;">
 *              <code>HOUR, HOUR_OF_DAY, MINUTE, SECOND, MILLISECOND<br></code>
 *       </td>
 *       <td style="vertical-align: middle;">
 *              <code>0<br></code>
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * <br>未在上述列表中的字段不适用默认值。
 *
 * <p>
 * <strong>示例：</strong>
 * <blockquote>
 * <pre>
 * // 获取 GMT-08:00（太平洋标准时间）支持的 ID
 * String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
 * // 如果没有返回 ID，说明有问题。退出。
 * if (ids.length == 0)
 *     System.exit(0);
 *
 *  // 开始输出
 * System.out.println("当前时间");
 *
 * // 创建一个太平洋标准时间时区
 * SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
 *
 * // 设置夏令时规则
 * pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 * pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 *
 * // 创建一个带有太平洋夏令时区的 GregorianCalendar，并设置当前日期和时间
 * Calendar calendar = new GregorianCalendar(pdt);
 * Date trialTime = new Date();
 * calendar.setTime(trialTime);
 *
 * // 输出一系列有趣的值
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *                    + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
 * System.out.println("DST_OFFSET: "
 *                    + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));
 *
 * System.out.println("当前时间，将小时重置为 3");
 * calendar.clear(Calendar.HOUR_OF_DAY); // 以便不覆盖
 * calendar.set(Calendar.HOUR, 3);
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *        + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000))); // 以小时为单位
 * System.out.println("DST_OFFSET: "
 *        + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000))); // 以小时为单位
 * </pre>
 * </blockquote>
 *
 * @see          TimeZone
 * @author David Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 * @since JDK1.1
 */
public class GregorianCalendar extends Calendar {
    /*
     * 实现说明
     *
     * 时代是从某个定义的起始点开始的天数或毫秒数。这里使用了 java.util.Date 的时代；即，从 1970 年 1 月 1 日（Gregorian），UTC 午夜开始的毫秒数。其他使用的时代包括
     * 1 年 1 月 1 日（Gregorian），这是 Gregorian 日历的第一天，以及 0 年 12 月 30 日（Gregorian），这是 Julian 日历的第一天。
     *
     * 我们实现了外推的 Julian 和 Gregorian 日历。这意味着我们实现了现代定义的日历，即使历史使用有所不同。例如，如果将 Gregorian 转换设置为 new Date(Long.MIN_VALUE)，我们有一个纯 Gregorian 日历，
     * 它将 1582 年发明 Gregorian 日历之前的日期标记为该日历存在时的日期。
     *
     * 同样，对于 Julian 日历，我们假设一致的 4 年闰年规则，即使历史上的闰年模式不规则，从公元前 45 年到公元前 9 年每 3 年一次，从公元 8 年开始每 4 年一次，中间没有闰年。因此，日期计算和
     * isLeapYear() 等函数不旨在历史准确。
     */


//////////////////
// Class Variables
//////////////////

    /**
     * <code>ERA</code>字段的值，表示
     * 公元前时期（即基督之前），也称为BCE。
     * 从<code>BC</code>到<code>AD</code>的年份序列是
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     *
     * @see #ERA
     */
    public static final int BC = 0;

    /**
     * {@link #ERA}字段的值，表示
     * 公元前时期，与{@link #BC}的值相同。
     *
     * @see #CE
     */
    static final int BCE = 0;

    /**
     * <code>ERA</code>字段的值，表示
     * 公元时期（公元），也称为CE。
     * 从<code>BC</code>到<code>AD</code>的年份序列是
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     *
     * @see #ERA
     */
    public static final int AD = 1;

    /**
     * {@link #ERA}字段的值，表示
     * 公元时期，与{@link #AD}的值相同。
     *
     * @see #BCE
     */
    static final int CE = 1;

    private static final int EPOCH_OFFSET   = 719163; // 1970年1月1日（格里高利历）的固定日期
    private static final int EPOCH_YEAR     = 1970;

    static final int MONTH_LENGTH[]
        = {31,28,31,30,31,30,31,31,30,31,30,31}; // 从0开始
    static final int LEAP_MONTH_LENGTH[]
        = {31,29,31,30,31,30,31,31,30,31,30,31}; // 从0开始

    // 有用的毫秒常量。虽然ONE_DAY和ONE_WEEK可以放入int中，但为了防止算术溢出
    // （bug 4173516），它们必须是long类型。
    private static final int  ONE_SECOND = 1000;
    private static final int  ONE_MINUTE = 60*ONE_SECOND;
    private static final int  ONE_HOUR   = 60*ONE_MINUTE;
    private static final long ONE_DAY    = 24*ONE_HOUR;
    private static final long ONE_WEEK   = 7*ONE_DAY;

    /*
     * <pre>
     *                            最大值       最小值
     * 字段名称        最小值       最小值     最大值     最大值
     * ----------        -------   -------     -------     -------
     * ERA                     0         0           1           1
     * YEAR                    1         1   292269054   292278994
     * MONTH                   0         0          11          11
     * WEEK_OF_YEAR            1         1          52*         53
     * WEEK_OF_MONTH           0         0           4*          6
     * DAY_OF_MONTH            1         1          28*         31
     * DAY_OF_YEAR             1         1         365*        366
     * DAY_OF_WEEK             1         1           7           7
     * DAY_OF_WEEK_IN_MONTH   -1        -1           4*          6
     * AM_PM                   0         0           1           1
     * HOUR                    0         0          11          11
     * HOUR_OF_DAY             0         0          23          23
     * MINUTE                  0         0          59          59
     * SECOND                  0         0          59          59
     * MILLISECOND             0         0         999         999
     * ZONE_OFFSET        -13:00    -13:00       14:00       14:00
     * DST_OFFSET           0:00      0:00        0:20        2:00
     * </pre>
     * *: 取决于格里高利历变更日期
     */
    static final int MIN_VALUES[] = {
        BCE,            // ERA
        1,              // YEAR
        JANUARY,        // MONTH
        1,              // WEEK_OF_YEAR
        0,              // WEEK_OF_MONTH
        1,              // DAY_OF_MONTH
        1,              // DAY_OF_YEAR
        SUNDAY,         // DAY_OF_WEEK
        1,              // DAY_OF_WEEK_IN_MONTH
        AM,             // AM_PM
        0,              // HOUR
        0,              // HOUR_OF_DAY
        0,              // MINUTE
        0,              // SECOND
        0,              // MILLISECOND
        -13*ONE_HOUR,   // ZONE_OFFSET (UNIX兼容性)
        0               // DST_OFFSET
    };
    static final int LEAST_MAX_VALUES[] = {
        CE,             // ERA
        292269054,      // YEAR
        DECEMBER,       // MONTH
        52,             // WEEK_OF_YEAR
        4,              // WEEK_OF_MONTH
        28,             // DAY_OF_MONTH
        365,            // DAY_OF_YEAR
        SATURDAY,       // DAY_OF_WEEK
        4,              // DAY_OF_WEEK_IN
        PM,             // AM_PM
        11,             // HOUR
        23,             // HOUR_OF_DAY
        59,             // MINUTE
        59,             // SECOND
        999,            // MILLISECOND
        14*ONE_HOUR,    // ZONE_OFFSET
        20*ONE_MINUTE   // DST_OFFSET (历史最小最大值)
    };
    static final int MAX_VALUES[] = {
        CE,             // ERA
        292278994,      // YEAR
        DECEMBER,       // MONTH
        53,             // WEEK_OF_YEAR
        6,              // WEEK_OF_MONTH
        31,             // DAY_OF_MONTH
        366,            // DAY_OF_YEAR
        SATURDAY,       // DAY_OF_WEEK
        6,              // DAY_OF_WEEK_IN
        PM,             // AM_PM
        11,             // HOUR
        23,             // HOUR_OF_DAY
        59,             // MINUTE
        59,             // SECOND
        999,            // MILLISECOND
        14*ONE_HOUR,    // ZONE_OFFSET
        2*ONE_HOUR      // DST_OFFSET (夏令时)
    };

    // 声明与JDK 1.1的序列化兼容性
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final long serialVersionUID = -8125100834729963327L;

    // 引用sun.util.calendar.Gregorian实例（单例）。
    private static final Gregorian gcal =
                                CalendarSystem.getGregorianCalendar();

    // 引用JulianCalendar实例（单例），根据需要设置。参见
    // getJulianCalendarSystem()。
    private static JulianCalendar jcal;

    // JulianCalendar时期。参见getJulianCalendarSystem()。
    private static Era[] jeras;

    // gregorianCutover的默认值。
    static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;

/////////////////////
// Instance Variables
/////////////////////

    /**
     * 使用格里高利历规则的时间点，以自标准纪元以来的毫秒数表示。默认值为1582年10月15日
     * （格里高利历）00:00:00 UTC或-12219292800000L。对于此值，1582年10月4日
     * （儒略历）后跟1582年10月15日（格里高利历）。这对应于儒略日数2299161。
     * @serial
     */
    private long gregorianCutover = DEFAULT_GREGORIAN_CUTOVER;

    /**
     * gregorianCutover的固定日期。
     */
    private transient long gregorianCutoverDate =
        (((DEFAULT_GREGORIAN_CUTOVER + 1)/ONE_DAY) - 1) + EPOCH_OFFSET; // == 577736

    /**
     * gregorianCutover在格里高利历中的标准化年份，0表示公元前1年，-1表示公元前2年，等等。
     */
    private transient int gregorianCutoverYear = 1582;

    /**
     * gregorianCutover在儒略历中的标准化年份，0表示公元前1年，-1表示公元前2年，等等。
     */
    private transient int gregorianCutoverYearJulian = 1582;

    /**
     * gdate始终具有一个sun.util.calendar.Gregorian.Date实例，以避免创建它的开销。假设大多数
     * 应用程序只需要格里高利历计算。
     */
    private transient BaseCalendar.Date gdate;

    /**
     * 引用gdate或JulianCalendar.Date实例。调用complete()后，此值保证已设置。
     */
    private transient BaseCalendar.Date cdate;

    /**
     * 用于计算cdate中的日期的CalendarSystem。调用complete()后，此值保证已设置并
     * 与cdate值一致。
     */
    private transient BaseCalendar calsys;

    /**
     * 用于获取时区偏移的临时int[2]。zoneOffsets[0]获取GMT偏移值，zoneOffsets[1]获取DST节省值。
     */
    private transient int[] zoneOffsets;

    /**
     * 用于在非宽松模式下保存原始fields[]值的临时存储。
     */
    private transient int[] originalFields;

///////////////
// Constructors
///////////////

    /**
     * 使用当前时间、默认时区和默认
     * {@link Locale.Category#FORMAT FORMAT}区域设置构造默认的<code>GregorianCalendar</code>。
     */
    public GregorianCalendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        setZoneShared(true);
    }

    /**
     * 使用给定时区的当前时间和默认
     * {@link Locale.Category#FORMAT FORMAT}区域设置构造<code>GregorianCalendar</code>。
     *
     * @param zone 给定的时区。
     */
    public GregorianCalendar(TimeZone zone) {
        this(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 使用当前时间、默认时区和给定的区域设置构造<code>GregorianCalendar</code>。
     *
     * @param aLocale 给定的区域设置。
     */
    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefaultRef(), aLocale);
        setZoneShared(true);
    }

    /**
     * 使用当前时间、给定时区和给定的区域设置构造<code>GregorianCalendar</code>。
     *
     * @param zone 给定的时区。
     * @param aLocale 给定的区域设置。
     */
    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        gdate = (BaseCalendar.Date) gcal.newCalendarDate(zone);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * 使用给定日期在默认时区和默认区域设置中设置构造<code>GregorianCalendar</code>。
     *
     * @param year 用于设置日历中<code>YEAR</code>日历字段的值。
     * @param month 用于设置日历中<code>MONTH</code>日历字段的值。
     * 月份值从0开始。例如，0表示1月。
     * @param dayOfMonth 用于设置日历中<code>DAY_OF_MONTH</code>日历字段的值。
     */
    public GregorianCalendar(int year, int month, int dayOfMonth) {
        this(year, month, dayOfMonth, 0, 0, 0, 0);
    }

    /**
     * 使用给定日期和时间在默认时区和默认区域设置中设置构造<code>GregorianCalendar</code>。
     *
     * @param year 用于设置日历中<code>YEAR</code>日历字段的值。
     * @param month 用于设置日历中<code>MONTH</code>日历字段的值。
     * 月份值从0开始。例如，0表示1月。
     * @param dayOfMonth 用于设置日历中<code>DAY_OF_MONTH</code>日历字段的值。
     * @param hourOfDay 用于设置日历中<code>HOUR_OF_DAY</code>日历字段的值。
     * @param minute 用于设置日历中<code>MINUTE</code>日历字段的值。
     */
    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay,
                             int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
    }

    /**
     * 使用给定日期和时间在默认时区和默认区域设置中设置构造<code>GregorianCalendar</code>。
     *
     * @param year 用于设置日历中<code>YEAR</code>日历字段的值。
     * @param month 用于设置日历中<code>MONTH</code>日历字段的值。
     * 月份值从0开始。例如，0表示1月。
     * @param dayOfMonth 用于设置日历中<code>DAY_OF_MONTH</code>日历字段的值。
     * @param hourOfDay 用于设置日历中<code>HOUR_OF_DAY</code>日历字段的值。
     * @param minute 用于设置日历中<code>MINUTE</code>日历字段的值。
     * @param second 用于设置日历中<code>SECOND</code>日历字段的值。
     */
    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay,
                             int minute, int second) {
        this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
    }

    /**
     * 使用给定日期和时间在默认时区和默认区域设置中设置构造<code>GregorianCalendar</code>。
     *
     * @param year 用于设置日历中<code>YEAR</code>日历字段的值。
     * @param month 用于设置日历中<code>MONTH</code>日历字段的值。
     * 月份值从0开始。例如，0表示1月。
     * @param dayOfMonth 用于设置日历中<code>DAY_OF_MONTH</code>日历字段的值。
     * @param hourOfDay 用于设置日历中<code>HOUR_OF_DAY</code>日历字段的值。
     * @param minute 用于设置日历中<code>MINUTE</code>日历字段的值。
     * @param second 用于设置日历中<code>SECOND</code>日历字段的值。
     * @param millis 用于设置日历中<code>MILLISECOND</code>日历字段的值。
     */
    GregorianCalendar(int year, int month, int dayOfMonth,
                      int hourOfDay, int minute, int second, int millis) {
        super();
        gdate = (BaseCalendar.Date) gcal.newCalendarDate(getZone());
        this.set(YEAR, year);
        this.set(MONTH, month);
        this.set(DAY_OF_MONTH, dayOfMonth);

        // 在设置HOUR_OF_DAY之前设置AM_PM和HOUR，以在设置为
        // 非宽松模式时不会抛出异常（6178071）。
        if (hourOfDay >= 12 && hourOfDay <= 23) {
            // 如果hourOfDay是有效的PM小时，则设置正确的PM值
            // 以便在设置为非宽松模式时不会抛出异常。
            this.internalSet(AM_PM, PM);
            this.internalSet(HOUR, hourOfDay - 12);
        } else {
            // AM_PM的默认值为AM。
            // 对于宽松性，这里不关心任何超出范围的值。
            this.internalSet(HOUR, hourOfDay);
        }
        // AM_PM和HOUR的戳记值必须为COMPUTED。 (6440854)
        setFieldsComputed(HOUR_MASK|AM_PM_MASK);

        this.set(HOUR_OF_DAY, hourOfDay);
        this.set(MINUTE, minute);
        this.set(SECOND, second);
        // 当此构造函数公开时，应更改为set()。
        this.internalSet(MILLISECOND, millis);
    }

    /**
     * 构造一个空的GregorianCalendar。
     *
     * @param zone    给定的时区
     * @param aLocale 给定的区域设置
     * @param flag    请求空实例的标志
     */
    GregorianCalendar(TimeZone zone, Locale locale, boolean flag) {
        super(zone, locale);
        gdate = (BaseCalendar.Date) gcal.newCalendarDate(getZone());
    }

/////////////////
// Public methods
/////////////////

    /**
     * 设置<code>GregorianCalendar</code>变更日期。这是从儒略日期切换到格里高利日期的时间点。默认值为1582年10月15日
     * （格里高利历）。在此之前，日期将处于儒略历。
     * <p>
     * 要获得一个纯儒略历，将变更日期设置为
     * <code>Date(Long.MAX_VALUE)</code>。要获得一个纯格里高利历，
     * 将变更日期设置为<code>Date(Long.MIN_VALUE)</code>。
     *
     * @param date 给定的格里高利变更日期。
     */
    public void setGregorianChange(Date date) {
        long cutoverTime = date.getTime();
        if (cutoverTime == gregorianCutover) {
            return;
        }
        // 在更改变更日期之前，确保获取此日历的时间。
        complete();
        setGregorianChange(cutoverTime);
    }


                private void setGregorianChange(long cutoverTime) {
        gregorianCutover = cutoverTime;
        gregorianCutoverDate = CalendarUtils.floorDivide(cutoverTime, ONE_DAY)
                                + EPOCH_OFFSET;

        // 提供“纯”儒略历。
        // 严格来说，最后的一毫秒应该是格里高利历日期。然而，API 文档规定，将转换日期设置为 Long.MAX_VALUE 将使此日历成为纯儒略历。（参见 4167995）
        if (cutoverTime == Long.MAX_VALUE) {
            gregorianCutoverDate++;
        }

        BaseCalendar.Date d = getGregorianCutoverDate();

        // 设置转换年份（在格里高利年编号中）
        gregorianCutoverYear = d.getYear();

        BaseCalendar julianCal = getJulianCalendarSystem();
        d = (BaseCalendar.Date) julianCal.newCalendarDate(TimeZone.NO_TIMEZONE);
        julianCal.getCalendarDateFromFixedDate(d, gregorianCutoverDate - 1);
        gregorianCutoverYearJulian = d.getNormalizedYear();

        if (time < gregorianCutover) {
            // 在新的转换日期下，字段值不再有效。
            setUnnormalized();
        }
    }

    /**
     * 获取格里高利历更改日期。这是从儒略日期转换为格里高利日期的点。默认值是 1582 年 10 月 15 日（格里高利历）。在此之前，日期将使用儒略历。
     *
     * @return 此 <code>GregorianCalendar</code> 对象的格里高利转换日期。
     */
    public final Date getGregorianChange() {
        return new Date(gregorianCutover);
    }

    /**
     * 确定给定年份是否为闰年。如果给定年份是闰年，则返回 <code>true</code>。要指定公元前年份，必须给出 <code>1 - 年份</code>。例如，公元前 4 年表示为 -3。
     *
     * @param year 给定的年份。
     * @return 如果给定年份是闰年，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isLeapYear(int year) {
        if ((year & 3) != 0) {
            return false;
        }

        if (year > gregorianCutoverYear) {
            return (year%100 != 0) || (year%400 == 0); // 格里高利历
        }
        if (year < gregorianCutoverYearJulian) {
            return true; // 儒略历
        }
        boolean gregorian;
        // 如果给定年份是格里高利转换年份，我们需要确定该年份的二月应使用哪种日历系统。
        if (gregorianCutoverYear == gregorianCutoverYearJulian) {
            BaseCalendar.Date d = getCalendarDate(gregorianCutoverDate); // 格里高利历
            gregorian = d.getMonth() < BaseCalendar.MARCH;
        } else {
            gregorian = year == gregorianCutoverYear;
        }
        return gregorian ? (year%100 != 0) || (year%400 == 0) : true;
    }

    /**
     * 返回 {@code "gregory"} 作为日历类型。
     *
     * @return {@code "gregory"}
     * @since 1.8
     */
    @Override
    public String getCalendarType() {
        return "gregory";
    }

    /**
     * 比较此 <code>GregorianCalendar</code> 与指定的 <code>Object</code>。结果为 <code>true</code> 当且仅当参数是一个 <code>GregorianCalendar</code> 对象，表示与该对象相同的时间值（从 <a href="Calendar.html#Epoch">纪元</a> 开始的毫秒偏移量）且具有相同的 <code>Calendar</code> 参数和格里高利转换日期。
     *
     * @param obj 要比较的对象。
     * @return 如果此对象等于 <code>obj</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see Calendar#compareTo(Calendar)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof GregorianCalendar &&
            super.equals(obj) &&
            gregorianCutover == ((GregorianCalendar)obj).gregorianCutover;
    }

    /**
     * 生成此 <code>GregorianCalendar</code> 对象的哈希码。
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ (int)gregorianCutoverDate;
    }

    /**
     * 根据日历的规则，向给定的日历字段添加指定的（带符号的）时间量。
     *
     * <p><em>添加规则 1</em>。调用后 <code>field</code> 的值减去调用前 <code>field</code> 的值等于 <code>amount</code>，模以 <code>field</code> 发生的任何溢出。当字段值超出其范围时，会发生溢出，结果是较大的字段被递增或递减，字段值被调整回其范围内。</p>
     *
     * <p><em>添加规则 2</em>。如果期望较小的字段保持不变，但由于 <code>field</code> 发生变化后其最小值或最大值发生变化，使得其无法等于之前的值，则其值将调整为尽可能接近其预期值。较小的字段表示较小的时间单位。 <code>HOUR</code> 是比 <code>DAY_OF_MONTH</code> 更小的字段。不调整那些不期望保持不变的较小字段。日历系统确定哪些字段应保持不变。</p>
     *
     * @param field 日历字段。
     * @param amount 要添加到字段的日期或时间量。
     * @exception IllegalArgumentException 如果 <code>field</code> 是 <code>ZONE_OFFSET</code>、<code>DST_OFFSET</code> 或未知，或者在非宽松模式下任何日历字段的值超出范围。
     */
    @Override
    public void add(int field, int amount) {
        // 如果 amount == 0，即使给定字段超出范围，也不执行任何操作。这是 JCK 测试的。
        if (amount == 0) {
            return;   // 不做任何操作！
        }

        if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        // 同步时间和日历字段。
        complete();

        if (field == YEAR) {
            int year = internalGet(YEAR);
            if (internalGetEra() == CE) {
                year += amount;
                if (year > 0) {
                    set(YEAR, year);
                } else { // year <= 0
                    set(YEAR, 1 - year);
                    // 如果 year == 0，你得到 1 公元前。
                    set(ERA, BCE);
                }
            }
            else { // era == BCE
                year -= amount;
                if (year > 0) {
                    set(YEAR, year);
                } else { // year <= 0
                    set(YEAR, 1 - year);
                    // 如果 year == 0，你得到 1 公元
                    set(ERA, CE);
                }
            }
            pinDayOfMonth();
        } else if (field == MONTH) {
            int month = internalGet(MONTH) + amount;
            int year = internalGet(YEAR);
            int y_amount;

            if (month >= 0) {
                y_amount = month/12;
            } else {
                y_amount = (month+1)/12 - 1;
            }
            if (y_amount != 0) {
                if (internalGetEra() == CE) {
                    year += y_amount;
                    if (year > 0) {
                        set(YEAR, year);
                    } else { // year <= 0
                        set(YEAR, 1 - year);
                        // 如果 year == 0，你得到 1 公元前
                        set(ERA, BCE);
                    }
                }
                else { // era == BCE
                    year -= y_amount;
                    if (year > 0) {
                        set(YEAR, year);
                    } else { // year <= 0
                        set(YEAR, 1 - year);
                        // 如果 year == 0，你得到 1 公元
                        set(ERA, CE);
                    }
                }
            }

            if (month >= 0) {
                set(MONTH,  month % 12);
            } else {
                // month < 0
                month %= 12;
                if (month < 0) {
                    month += 12;
                }
                set(MONTH, JANUARY + month);
            }
            pinDayOfMonth();
        } else if (field == ERA) {
            int era = internalGet(ERA) + amount;
            if (era < 0) {
                era = 0;
            }
            if (era > 1) {
                era = 1;
            }
            set(ERA, era);
        } else {
            long delta = amount;
            long timeOfDay = 0;
            switch (field) {
            // 在这里处理时间字段。将给定的量转换为毫秒并调用 setTimeInMillis。
            case HOUR:
            case HOUR_OF_DAY:
                delta *= 60 * 60 * 1000;        // 小时转换为分钟
                break;

            case MINUTE:
                delta *= 60 * 1000;             // 分钟转换为秒
                break;

            case SECOND:
                delta *= 1000;                  // 秒转换为毫秒
                break;

            case MILLISECOND:
                break;

            // 处理涉及时区偏移变化调整的周、日和 AM_PM 字段。将给定的量转换为天数。
            case WEEK_OF_YEAR:
            case WEEK_OF_MONTH:
            case DAY_OF_WEEK_IN_MONTH:
                delta *= 7;
                break;

            case DAY_OF_MONTH: // DATE 的同义词
            case DAY_OF_YEAR:
            case DAY_OF_WEEK:
                break;

            case AM_PM:
                // 将量转换为天数（delta）和 +12 或 -12 小时（timeOfDay）。
                delta = amount / 2;
                timeOfDay = 12 * (amount % 2);
                break;
            }

            // 时间字段不需要时区偏移变化调整。
            if (field >= HOUR) {
                setTimeInMillis(time + delta);
                return;
            }

            // 其余字段（周、日或 AM_PM 字段）需要时区偏移（包括 GMT 和 DST）变化调整。

            // 将当前时间转换为固定的日期和一天中的时间。
            long fd = getCurrentFixedDate();
            timeOfDay += internalGet(HOUR_OF_DAY);
            timeOfDay *= 60;
            timeOfDay += internalGet(MINUTE);
            timeOfDay *= 60;
            timeOfDay += internalGet(SECOND);
            timeOfDay *= 1000;
            timeOfDay += internalGet(MILLISECOND);
            if (timeOfDay >= ONE_DAY) {
                fd++;
                timeOfDay -= ONE_DAY;
            } else if (timeOfDay < 0) {
                fd--;
                timeOfDay += ONE_DAY;
            }

            fd += delta; // fd 是计算后的预期固定日期
            int zoneOffset = internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET);
            setTimeInMillis((fd - EPOCH_OFFSET) * ONE_DAY + timeOfDay - zoneOffset);
            zoneOffset -= internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET);
            // 如果时区偏移发生变化，则调整差异。
            if (zoneOffset != 0) {
                setTimeInMillis(time + zoneOffset);
                long fd2 = getCurrentFixedDate();
                // 如果调整改变了日期，则取前一个日期。
                if (fd2 != fd) {
                    setTimeInMillis(time - zoneOffset);
                }
            }
        }
    }

    /**
     * 在给定的时间字段上添加或减去（上下）一个时间单位，而不改变较大的字段。
     * <p>
     * <em>示例</em>：考虑一个最初设置为 1999 年 12 月 31 日的 <code>GregorianCalendar</code>。调用 {@link #roll(int,boolean) roll(Calendar.MONTH, true)}
     * 将日历设置为 1999 年 1 月 31 日。<code>YEAR</code> 字段保持不变，因为它比 <code>MONTH</code> 更大。</p>
     *
     * @param up 指示指定的日历字段的值是向上滚动还是向下滚动。使用 <code>true</code> 表示向上滚动，<code>false</code> 表示向下滚动。
     * @exception IllegalArgumentException 如果 <code>field</code> 是 <code>ZONE_OFFSET</code>、<code>DST_OFFSET</code> 或未知，或者在非宽松模式下任何日历字段的值超出范围。
     * @see #add(int,int)
     * @see #set(int,int)
     */
    @Override
    public void roll(int field, boolean up) {
        roll(field, up ? +1 : -1);
    }

    /**
     * 向指定的日历字段添加带符号的量，而不改变较大的字段。负的滚动量表示从字段中减去，而不改变较大的字段。如果指定的量为 0，此方法不执行任何操作。
     *
     * <p>此方法在添加量之前调用 {@link #complete()} 以使所有日历字段规范化。如果在非宽松模式下有任何日历字段的值超出范围，则抛出 <code>IllegalArgumentException</code>。
     *
     * <p>
     * <em>示例</em>：考虑一个最初设置为 1999 年 8 月 31 日的 <code>GregorianCalendar</code>。调用 <code>roll(Calendar.MONTH,
     * 8)</code> 将日历设置为 1999 年 4 月 30 日。使用 <code>GregorianCalendar</code>，<code>DAY_OF_MONTH</code> 字段在 4 月不能为 31。<code>DAY_OF_MONTH</code> 被设置为最接近的可能值，即 30。<code>YEAR</code> 字段保持 1999 年不变，因为它比 <code>MONTH</code> 更大。
     * <p>
     * <em>示例</em>：考虑一个最初设置为 1999 年 6 月 6 日星期日的 <code>GregorianCalendar</code>。调用
     * <code>roll(Calendar.WEEK_OF_MONTH, -1)</code> 将日历设置为 1999 年 6 月 1 日星期二，而调用
     * <code>add(Calendar.WEEK_OF_MONTH, -1)</code> 将日历设置为 1999 年 5 月 30 日星期日。这是因为滚动规则施加了额外的约束：当 <code>WEEK_OF_MONTH</code> 被滚动时，<code>MONTH</code> 不能改变。结合添加规则 1，结果日期必须在 1999 年 6 月 1 日星期二和 1999 年 6 月 5 日星期六之间。根据添加规则 2，<code>DAY_OF_WEEK</code>（在更改 <code>WEEK_OF_MONTH</code> 时保持不变）被设置为星期二，这是最接近星期日的可能值（其中星期日是一周的第一天）。</p>
     *
     * @param field 日历字段。
     * @param amount 要添加到 <code>field</code> 的带符号量。
     * @exception IllegalArgumentException 如果 <code>field</code> 是 <code>ZONE_OFFSET</code>、<code>DST_OFFSET</code> 或未知，或者在非宽松模式下任何日历字段的值超出范围。
     * @see #roll(int,boolean)
     * @see #add(int,int)
     * @see #set(int,int)
     * @since 1.2
     */
    @Override
    public void roll(int field, int amount) {
        // 如果 amount == 0，即使给定字段超出范围，也不执行任何操作。这是 JCK 测试的。
        if (amount == 0) {
            return;
        }


                    if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        // 同步时间和日历字段。
        complete();

        int min = getMinimum(field);
        int max = getMaximum(field);

        switch (field) {
        case AM_PM:
        case ERA:
        case YEAR:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
            // 这些字段处理简单，因为它们有固定的最小值和最大值。DAY_OF_MONTH 字段几乎同样简单。其他字段较为复杂，因为它们的滚动范围取决于日期。
            break;

        case HOUR:
        case HOUR_OF_DAY:
            {
                int rolledValue = getRolledValue(internalGet(field), amount, min, max);
                int hourOfDay = rolledValue;
                if (field == HOUR && internalGet(AM_PM) == PM) {
                    hourOfDay += 12;
                }

                // 创建当前日期/时间值以执行基于标准时间的滚动。
                CalendarDate d = calsys.getCalendarDate(time, getZone());
                d.setHours(hourOfDay);
                time = calsys.getTime(d);

                // 如果保持在同一标准时间，尝试下一个或上一个小时。
                if (internalGet(HOUR_OF_DAY) == d.getHours()) {
                    hourOfDay = getRolledValue(rolledValue, amount > 0 ? +1 : -1, min, max);
                    if (field == HOUR && internalGet(AM_PM) == PM) {
                        hourOfDay += 12;
                    }
                    d.setHours(hourOfDay);
                    time = calsys.getTime(d);
                }
                // 获取可能因夏令时转换而改变的新小时值。
                hourOfDay = d.getHours();
                // 更新与小时相关的字段
                internalSet(HOUR_OF_DAY, hourOfDay);
                internalSet(AM_PM, hourOfDay / 12);
                internalSet(HOUR, hourOfDay % 12);

                // 时区偏移和/或夏令时可能已改变。
                int zoneOffset = d.getZoneOffset();
                int saving = d.getDaylightSaving();
                internalSet(ZONE_OFFSET, zoneOffset - saving);
                internalSet(DST_OFFSET, saving);
                return;
            }

        case MONTH:
            // 滚动月份涉及将最终值固定在 [0, 11] 范围内，并在必要时调整 DAY_OF_MONTH。只有在更新 MONTH 字段后，DAY_OF_MONTH 是非法的情况下，我们才会调整 DAY_OF_MONTH。例如，<jan31>.roll(MONTH, 1) -> <feb28> 或 <feb29>。
            {
                if (!isCutoverYear(cdate.getNormalizedYear())) {
                    int mon = (internalGet(MONTH) + amount) % 12;
                    if (mon < 0) {
                        mon += 12;
                    }
                    set(MONTH, mon);

                    // 保持月份内的天数在范围内。我们不希望溢出到下个月；例如，我们不希望 jan31 + 1 mo -> feb31 -> mar3。
                    int monthLen = monthLength(mon);
                    if (internalGet(DAY_OF_MONTH) > monthLen) {
                        set(DAY_OF_MONTH, monthLen);
                    }
                } else {
                    // 我们需要处理由于转换而导致的年份和月份长度不同。
                    int yearLength = getActualMaximum(MONTH) + 1;
                    int mon = (internalGet(MONTH) + amount) % yearLength;
                    if (mon < 0) {
                        mon += yearLength;
                    }
                    set(MONTH, mon);
                    int monthLen = getActualMaximum(DAY_OF_MONTH);
                    if (internalGet(DAY_OF_MONTH) > monthLen) {
                        set(DAY_OF_MONTH, monthLen);
                    }
                }
                return;
            }

        case WEEK_OF_YEAR:
            {
                int y = cdate.getNormalizedYear();
                max = getActualMaximum(WEEK_OF_YEAR);
                set(DAY_OF_WEEK, internalGet(DAY_OF_WEEK));
                int woy = internalGet(WEEK_OF_YEAR);
                int value = woy + amount;
                if (!isCutoverYear(y)) {
                    int weekYear = getWeekYear();
                    if (weekYear == y) {
                        // 如果新值在 min 和 max 之间（不包括），则可以使用该值。
                        if (value > min && value < max) {
                            set(WEEK_OF_YEAR, value);
                            return;
                        }
                        long fd = getCurrentFixedDate();
                        // 确保最小周包含当前日历年的 DAY_OF_WEEK
                        long day1 = fd - (7 * (woy - min));
                        if (calsys.getYearFromFixedDate(day1) != y) {
                            min++;
                        }

                        // 确保最大周也一样
                        fd += 7 * (max - internalGet(WEEK_OF_YEAR));
                        if (calsys.getYearFromFixedDate(fd) != y) {
                            max--;
                        }
                    } else {
                        // 当 WEEK_OF_YEAR 和 YEAR 不同步时，调整 woy 和 amount 以保持在日历年内。
                        if (weekYear > y) {
                            if (amount < 0) {
                                amount++;
                            }
                            woy = max;
                        } else {
                            if (amount > 0) {
                                amount -= woy - max;
                            }
                            woy = min;
                        }
                    }
                    set(field, getRolledValue(woy, amount, min, max));
                    return;
                }

                // 处理转换。
                long fd = getCurrentFixedDate();
                BaseCalendar cal;
                if (gregorianCutoverYear == gregorianCutoverYearJulian) {
                    cal = getCutoverCalendarSystem();
                } else if (y == gregorianCutoverYear) {
                    cal = gcal;
                } else {
                    cal = getJulianCalendarSystem();
                }
                long day1 = fd - (7 * (woy - min));
                // 确保最小周包含当前日历年的 DAY_OF_WEEK
                if (cal.getYearFromFixedDate(day1) != y) {
                    min++;
                }

                // 确保最大周也一样
                fd += 7 * (max - woy);
                cal = (fd >= gregorianCutoverDate) ? gcal : getJulianCalendarSystem();
                if (cal.getYearFromFixedDate(fd) != y) {
                    max--;
                }
                // value: 必须转换为月份和月份中的天数的新 WEEK_OF_YEAR。
                value = getRolledValue(woy, amount, min, max) - 1;
                BaseCalendar.Date d = getCalendarDate(day1 + value * 7);
                set(MONTH, d.getMonth() - 1);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case WEEK_OF_MONTH:
            {
                boolean isCutoverYear = isCutoverYear(cdate.getNormalizedYear());
                // dow: 从一周的第一天起的相对星期几
                int dow = internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();
                if (dow < 0) {
                    dow += 7;
                }

                long fd = getCurrentFixedDate();
                long month1;     // 月份第一天（通常是 1）的固定日期
                int monthLength; // 实际月份长度
                if (isCutoverYear) {
                    month1 = getFixedDateMonth1(cdate, fd);
                    monthLength = actualMonthLength();
                } else {
                    month1 = fd - internalGet(DAY_OF_MONTH) + 1;
                    monthLength = calsys.getMonthLength(cdate);
                }

                // 月份的第一天的星期几。
                long monthDay1st = BaseCalendar.getDayOfWeekDateOnOrBefore(month1 + 6,
                                                                           getFirstDayOfWeek());
                // 如果一周有足够的天数形成一周，则该周从上个月开始。
                if ((int)(monthDay1st - month1) >= getMinimalDaysInFirstWeek()) {
                    monthDay1st -= 7;
                }
                max = getActualMaximum(field);

                // value: 新的 WEEK_OF_MONTH 值
                int value = getRolledValue(internalGet(field), amount, 1, max) - 1;

                // nfd: 滚动日期的固定日期
                long nfd = monthDay1st + value * 7 + dow;

                // 与 WEEK_OF_YEAR 不同，如果 nfd 超出月份范围，我们需要更改星期几。
                if (nfd < month1) {
                    nfd = month1;
                } else if (nfd >= (month1 + monthLength)) {
                    nfd = month1 + monthLength - 1;
                }
                int dayOfMonth;
                if (isCutoverYear) {
                    // 如果在转换年，将 nfd 转换为其日历日期并使用 dayOfMonth。
                    BaseCalendar.Date d = getCalendarDate(nfd);
                    dayOfMonth = d.getDayOfMonth();
                } else {
                    dayOfMonth = (int)(nfd - month1) + 1;
                }
                set(DAY_OF_MONTH, dayOfMonth);
                return;
            }

        case DAY_OF_MONTH:
            {
                if (!isCutoverYear(cdate.getNormalizedYear())) {
                    max = calsys.getMonthLength(cdate);
                    break;
                }

                // 转换年处理
                long fd = getCurrentFixedDate();
                long month1 = getFixedDateMonth1(cdate, fd);
                // 可能不是常规月份。将日期和范围转换为相对值，执行滚动，然后将结果转换回滚动日期。
                int value = getRolledValue((int)(fd - month1), amount, 0, actualMonthLength() - 1);
                BaseCalendar.Date d = getCalendarDate(month1 + value);
                assert d.getMonth()-1 == internalGet(MONTH);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case DAY_OF_YEAR:
            {
                max = getActualMaximum(field);
                if (!isCutoverYear(cdate.getNormalizedYear())) {
                    break;
                }

                // 处理转换。
                long fd = getCurrentFixedDate();
                long jan1 = fd - internalGet(DAY_OF_YEAR) + 1;
                int value = getRolledValue((int)(fd - jan1) + 1, amount, min, max);
                BaseCalendar.Date d = getCalendarDate(jan1 + value - 1);
                set(MONTH, d.getMonth() - 1);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case DAY_OF_WEEK:
            {
                if (!isCutoverYear(cdate.getNormalizedYear())) {
                    // 如果年份中的周在同一年内，我们可以直接更改 DAY_OF_WEEK。
                    int weekOfYear = internalGet(WEEK_OF_YEAR);
                    if (weekOfYear > 1 && weekOfYear < 52) {
                        set(WEEK_OF_YEAR, weekOfYear); // 更新 stamp[WEEK_OF_YEAR]
                        max = SATURDAY;
                        break;
                    }
                }

                // 在年份边界和转换年需要以不同方式处理。注意，更改纪元和年份值违反滚动规则：不更改较大的日历字段...
                amount %= 7;
                if (amount == 0) {
                    return;
                }
                long fd = getCurrentFixedDate();
                long dowFirst = BaseCalendar.getDayOfWeekDateOnOrBefore(fd, getFirstDayOfWeek());
                fd += amount;
                if (fd < dowFirst) {
                    fd += 7;
                } else if (fd >= dowFirst + 7) {
                    fd -= 7;
                }
                BaseCalendar.Date d = getCalendarDate(fd);
                set(ERA, (d.getNormalizedYear() <= 0 ? BCE : CE));
                set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth());
                return;
            }

        case DAY_OF_WEEK_IN_MONTH:
            {
                min = 1; // 归一化后，min 应为 1。
                if (!isCutoverYear(cdate.getNormalizedYear())) {
                    int dom = internalGet(DAY_OF_MONTH);
                    int monthLength = calsys.getMonthLength(cdate);
                    int lastDays = monthLength % 7;
                    max = monthLength / 7;
                    int x = (dom - 1) % 7;
                    if (x < lastDays) {
                        max++;
                    }
                    set(DAY_OF_WEEK, internalGet(DAY_OF_WEEK));
                    break;
                }

                // 转换年处理
                long fd = getCurrentFixedDate();
                long month1 = getFixedDateMonth1(cdate, fd);
                int monthLength = actualMonthLength();
                int lastDays = monthLength % 7;
                max = monthLength / 7;
                int x = (int)(fd - month1) % 7;
                if (x < lastDays) {
                    max++;
                }
                int value = getRolledValue(internalGet(field), amount, min, max) - 1;
                fd = month1 + value * 7 + x;
                BaseCalendar cal = (fd >= gregorianCutoverDate) ? gcal : getJulianCalendarSystem();
                BaseCalendar.Date d = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                cal.getCalendarDateFromFixedDate(d, fd);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }
        }

        set(field, getRolledValue(internalGet(field), amount, min, max));
    }

    /**
     * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最小值。最小值定义为对于任何可能的时间值，由 {@link
     * Calendar#get(int) get} 方法返回的最小值，同时考虑当前的
     * {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}、
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}、
     * {@link #getGregorianChange() getGregorianChange} 和
     * {@link Calendar#getTimeZone() getTimeZone} 方法的值。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最小值。
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    @Override
    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }


                /**
                 * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最大值。最大值定义为
                 * {@link Calendar#get(int) get} 方法对于任何可能的时间值返回的最大值，
                 * 考虑到当前的 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
                 * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
                 * {@link #getGregorianChange() getGregorianChange} 和
                 * {@link Calendar#getTimeZone() getTimeZone} 方法的值。
                 *
                 * @param field 日历字段。
                 * @return 给定日历字段的最大值。
                 * @see #getMinimum(int)
                 * @see #getGreatestMinimum(int)
                 * @see #getLeastMaximum(int)
                 * @see #getActualMinimum(int)
                 * @see #getActualMaximum(int)
                 */
                @Override
                public int getMaximum(int field) {
                    switch (field) {
                    case MONTH:
                    case DAY_OF_MONTH:
                    case DAY_OF_YEAR:
                    case WEEK_OF_YEAR:
                    case WEEK_OF_MONTH:
                    case DAY_OF_WEEK_IN_MONTH:
                    case YEAR:
                        {
                            // 在格里高利历 200-3-1 或之后，朱利安历和格里高利历日期相同或格里高利历日期更大（即，300-3-1 之后存在“差距”）。
                            if (gregorianCutoverYear > 200) {
                                break;
                            }
                            // 可能存在“重叠”日期。
                            GregorianCalendar gc = (GregorianCalendar) clone();
                            gc.setLenient(true);
                            gc.setTimeInMillis(gregorianCutover);
                            int v1 = gc.getActualMaximum(field);
                            gc.setTimeInMillis(gregorianCutover-1);
                            int v2 = gc.getActualMaximum(field);
                            return Math.max(MAX_VALUES[field], Math.max(v1, v2));
                        }
                    }
                    return MAX_VALUES[field];
                }

                /**
                 * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最高最小值。最高最小值定义为
                 * {@link #getActualMinimum(int)} 对于任何可能的时间值返回的最大值，
                 * 考虑到当前的 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
                 * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
                 * {@link #getGregorianChange() getGregorianChange} 和
                 * {@link Calendar#getTimeZone() getTimeZone} 方法的值。
                 *
                 * @param field 日历字段。
                 * @return 给定日历字段的最高最小值。
                 * @see #getMinimum(int)
                 * @see #getMaximum(int)
                 * @see #getLeastMaximum(int)
                 * @see #getActualMinimum(int)
                 * @see #getActualMaximum(int)
                 */
                @Override
                public int getGreatestMinimum(int field) {
                    if (field == DAY_OF_MONTH) {
                        BaseCalendar.Date d = getGregorianCutoverDate();
                        long mon1 = getFixedDateMonth1(d, gregorianCutoverDate);
                        d = getCalendarDate(mon1);
                        return Math.max(MIN_VALUES[field], d.getDayOfMonth());
                    }
                    return MIN_VALUES[field];
                }

                /**
                 * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最低最大值。最低最大值定义为
                 * {@link #getActualMaximum(int)} 对于任何可能的时间值返回的最小值，
                 * 考虑到当前的 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
                 * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
                 * {@link #getGregorianChange() getGregorianChange} 和
                 * {@link Calendar#getTimeZone() getTimeZone} 方法的值。
                 *
                 * @param field 日历字段
                 * @return 给定日历字段的最低最大值。
                 * @see #getMinimum(int)
                 * @see #getMaximum(int)
                 * @see #getGreatestMinimum(int)
                 * @see #getActualMinimum(int)
                 * @see #getActualMaximum(int)
                 */
                @Override
                public int getLeastMaximum(int field) {
                    switch (field) {
                    case MONTH:
                    case DAY_OF_MONTH:
                    case DAY_OF_YEAR:
                    case WEEK_OF_YEAR:
                    case WEEK_OF_MONTH:
                    case DAY_OF_WEEK_IN_MONTH:
                    case YEAR:
                        {
                            GregorianCalendar gc = (GregorianCalendar) clone();
                            gc.setLenient(true);
                            gc.setTimeInMillis(gregorianCutover);
                            int v1 = gc.getActualMaximum(field);
                            gc.setTimeInMillis(gregorianCutover-1);
                            int v2 = gc.getActualMaximum(field);
                            return Math.min(LEAST_MAX_VALUES[field], Math.min(v1, v2));
                        }
                    }
                    return LEAST_MAX_VALUES[field];
                }

                /**
                 * 返回此日历字段在给定时间值和当前
                 * {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
                 * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
                 * {@link #getGregorianChange() getGregorianChange} 和
                 * {@link Calendar#getTimeZone() getTimeZone} 方法值的条件下可能具有的最小值。
                 *
                 * <p>例如，如果格里高利历变更日期是 1970 年 1 月 10 日，而此 <code>GregorianCalendar</code> 的日期是 1970 年 1 月 20 日，
                 * 那么 <code>DAY_OF_MONTH</code> 字段的实际最小值是 10，因为 1970 年 1 月 10 日的前一天是 1996 年 12 月 27 日（朱利安历）。
                 * 因此，1969 年 12 月 28 日到 1970 年 1 月 9 日不存在。
                 *
                 * @param field 日历字段
                 * @return 此 <code>GregorianCalendar</code> 的给定时间值的字段最小值
                 * @see #getMinimum(int)
                 * @see #getMaximum(int)
                 * @see #getGreatestMinimum(int)
                 * @see #getLeastMaximum(int)
                 * @see #getActualMaximum(int)
                 * @since 1.2
                 */
                @Override
                public int getActualMinimum(int field) {
                    if (field == DAY_OF_MONTH) {
                        GregorianCalendar gc = getNormalizedCalendar();
                        int year = gc.cdate.getNormalizedYear();
                        if (year == gregorianCutoverYear || year == gregorianCutoverYearJulian) {
                            long month1 = getFixedDateMonth1(gc.cdate, gc.calsys.getFixedDate(gc.cdate));
                            BaseCalendar.Date d = getCalendarDate(month1);
                            return d.getDayOfMonth();
                        }
                    }
                    return getMinimum(field);
                }

                /**
                 * 返回此日历字段在给定时间值和当前
                 * {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
                 * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
                 * {@link #getGregorianChange() getGregorianChange} 和
                 * {@link Calendar#getTimeZone() getTimeZone} 方法值的条件下可能具有的最大值。
                 * 例如，如果此实例的日期是 2004 年 2 月 1 日，<code>DAY_OF_MONTH</code> 字段的实际最大值是 29，因为 2004 年是闰年；
                 * 如果此实例的日期是 2005 年 2 月 1 日，那么它的值是 28。
                 *
                 * <p>此方法根据 {@link Calendar#YEAR YEAR}（日历年）值计算 {@link Calendar#WEEK_OF_YEAR WEEK_OF_YEAR} 的最大值，
                 * 而不是 <a href="#week_year">周年</a>。调用 {@link #getWeeksInWeekYear()} 获取此 {@code GregorianCalendar} 的周年的 {@code WEEK_OF_YEAR} 的最大值。
                 *
                 * @param field 日历字段
                 * @return 此 <code>GregorianCalendar</code> 的给定时间值的字段最大值
                 * @see #getMinimum(int)
                 * @see #getMaximum(int)
                 * @see #getGreatestMinimum(int)
                 * @see #getLeastMaximum(int)
                 * @see #getActualMinimum(int)
                 * @since 1.2
                 */
                @Override
                public int getActualMaximum(int field) {
                    final int fieldsForFixedMax = ERA_MASK|DAY_OF_WEEK_MASK|HOUR_MASK|AM_PM_MASK|
                        HOUR_OF_DAY_MASK|MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK|
                        ZONE_OFFSET_MASK|DST_OFFSET_MASK;
                    if ((fieldsForFixedMax & (1<<field)) != 0) {
                        return getMaximum(field);
                    }

                    GregorianCalendar gc = getNormalizedCalendar();
                    BaseCalendar.Date date = gc.cdate;
                    BaseCalendar cal = gc.calsys;
                    int normalizedYear = date.getNormalizedYear();

                    int value = -1;
                    switch (field) {
                    case MONTH:
                        {
                            if (!gc.isCutoverYear(normalizedYear)) {
                                value = DECEMBER;
                                break;
                            }

                            // 下一年的 1 月 1 日可能存在或不存在。
                            long nextJan1;
                            do {
                                nextJan1 = gcal.getFixedDate(++normalizedYear, BaseCalendar.JANUARY, 1, null);
                            } while (nextJan1 < gregorianCutoverDate);
                            BaseCalendar.Date d = (BaseCalendar.Date) date.clone();
                            cal.getCalendarDateFromFixedDate(d, nextJan1 - 1);
                            value = d.getMonth() - 1;
                        }
                        break;

                    case DAY_OF_MONTH:
                        {
                            value = cal.getMonthLength(date);
                            if (!gc.isCutoverYear(normalizedYear) || date.getDayOfMonth() == value) {
                                break;
                            }

                            // 处理变更年。
                            long fd = gc.getCurrentFixedDate();
                            if (fd >= gregorianCutoverDate) {
                                break;
                            }
                            int monthLength = gc.actualMonthLength();
                            long monthEnd = gc.getFixedDateMonth1(gc.cdate, fd) + monthLength - 1;
                            // 将固定日期转换为其日历日期。
                            BaseCalendar.Date d = gc.getCalendarDate(monthEnd);
                            value = d.getDayOfMonth();
                        }
                        break;

                    case DAY_OF_YEAR:
                        {
                            if (!gc.isCutoverYear(normalizedYear)) {
                                value = cal.getYearLength(date);
                                break;
                            }

                            // 处理变更年。
                            long jan1;
                            if (gregorianCutoverYear == gregorianCutoverYearJulian) {
                                BaseCalendar cocal = gc.getCutoverCalendarSystem();
                                jan1 = cocal.getFixedDate(normalizedYear, 1, 1, null);
                            } else if (normalizedYear == gregorianCutoverYearJulian) {
                                jan1 = cal.getFixedDate(normalizedYear, 1, 1, null);
                            } else {
                                jan1 = gregorianCutoverDate;
                            }
                            // 下一年的 1 月 1 日可能存在或不存在。
                            long nextJan1 = gcal.getFixedDate(++normalizedYear, 1, 1, null);
                            if (nextJan1 < gregorianCutoverDate) {
                                nextJan1 = gregorianCutoverDate;
                            }
                            assert jan1 <= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(),
                                                            date.getDayOfMonth(), date);
                            assert nextJan1 >= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(),
                                                            date.getDayOfMonth(), date);
                            value = (int)(nextJan1 - jan1);
                        }
                        break;

                    case WEEK_OF_YEAR:
                        {
                            if (!gc.isCutoverYear(normalizedYear)) {
                                // 获取该年 1 月 1 日的星期几
                                CalendarDate d = cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                                d.setDate(date.getYear(), BaseCalendar.JANUARY, 1);
                                int dayOfWeek = cal.getDayOfWeek(d);
                                // 用 firstDayOfWeek 值规范化星期几
                                dayOfWeek -= getFirstDayOfWeek();
                                if (dayOfWeek < 0) {
                                    dayOfWeek += 7;
                                }
                                value = 52;
                                int magic = dayOfWeek + getMinimalDaysInFirstWeek() - 1;
                                if ((magic == 6) ||
                                    (date.isLeapYear() && (magic == 5 || magic == 12))) {
                                    value++;
                                }
                                break;
                            }

                            if (gc == this) {
                                gc = (GregorianCalendar) gc.clone();
                            }
                            int maxDayOfYear = getActualMaximum(DAY_OF_YEAR);
                            gc.set(DAY_OF_YEAR, maxDayOfYear);
                            value = gc.get(WEEK_OF_YEAR);
                            if (internalGet(YEAR) != gc.getWeekYear()) {
                                gc.set(DAY_OF_YEAR, maxDayOfYear - 7);
                                value = gc.get(WEEK_OF_YEAR);
                            }
                        }
                        break;

                    case WEEK_OF_MONTH:
                        {
                            if (!gc.isCutoverYear(normalizedYear)) {
                                CalendarDate d = cal.newCalendarDate(null);
                                d.setDate(date.getYear(), date.getMonth(), 1);
                                int dayOfWeek = cal.getDayOfWeek(d);
                                int monthLength = cal.getMonthLength(d);
                                dayOfWeek -= getFirstDayOfWeek();
                                if (dayOfWeek < 0) {
                                    dayOfWeek += 7;
                                }
                                int nDaysFirstWeek = 7 - dayOfWeek; // 第一周的天数
                                value = 3;
                                if (nDaysFirstWeek >= getMinimalDaysInFirstWeek()) {
                                    value++;
                                }
                                monthLength -= nDaysFirstWeek + 7 * 3;
                                if (monthLength > 0) {
                                    value++;
                                    if (monthLength > 7) {
                                        value++;
                                    }
                                }
                                break;
                            }

                            // 变更年处理
                            if (gc == this) {
                                gc = (GregorianCalendar) gc.clone();
                            }
                            int y = gc.internalGet(YEAR);
                            int m = gc.internalGet(MONTH);
                            do {
                                value = gc.get(WEEK_OF_MONTH);
                                gc.add(WEEK_OF_MONTH, +1);
                            } while (gc.get(YEAR) == y && gc.get(MONTH) == m);
                        }
                        break;

                    case DAY_OF_WEEK_IN_MONTH:
                        {
                            // 可能在格里高利历变更月份
                            int ndays, dow1;
                            int dow = date.getDayOfWeek();
                            if (!gc.isCutoverYear(normalizedYear)) {
                                BaseCalendar.Date d = (BaseCalendar.Date) date.clone();
                                ndays = cal.getMonthLength(d);
                                d.setDayOfMonth(1);
                                cal.normalize(d);
                                dow1 = d.getDayOfWeek();
                            } else {
                                // 让克隆的 GregorianCalendar 处理变更情况。
                                if (gc == this) {
                                    gc = (GregorianCalendar) clone();
                                }
                                ndays = gc.actualMonthLength();
                                gc.set(DAY_OF_MONTH, gc.getActualMinimum(DAY_OF_MONTH));
                                dow1 = gc.get(DAY_OF_WEEK);
                            }
                            int x = dow - dow1;
                            if (x < 0) {
                                x += 7;
                            }
                            ndays -= x;
                            value = (ndays + 6) / 7;
                        }
                        break;


                    case YEAR:
            /* The year computation is no different, in principle, from the
             * others, however, the range of possible maxima is large.  In
             * addition, the way we know we've exceeded the range is different.
             * For these reasons, we use the special case code below to handle
             * this field.
             *
             * The actual maxima for YEAR depend on the type of calendar:
             *
             *     Gregorian = May 17, 292275056 BCE - Aug 17, 292278994 CE
             *     Julian    = Dec  2, 292269055 BCE - Jan  3, 292272993 CE
             *     Hybrid    = Dec  2, 292269055 BCE - Aug 17, 292278994 CE
             *
             * We know we've exceeded the maximum when either the month, date,
             * time, or era changes in response to setting the year.  We don't
             * check for month, date, and time here because the year and era are
             * sufficient to detect an invalid year setting.  NOTE: If code is
             * added to check the month and date in the future for some reason,
             * Feb 29 must be allowed to shift to Mar 1 when setting the year.
             */
            {
                if (gc == this) {
                    gc = (GregorianCalendar) clone();
                }

                // 计算从该日历年的开始到当前时间的毫秒偏移量，并在最大年份的限制内调整最大年份值。
                long current = gc.getYearOffsetInMillis();

                if (gc.internalGetEra() == CE) {
                    gc.setTimeInMillis(Long.MAX_VALUE);
                    value = gc.get(YEAR);
                    long maxEnd = gc.getYearOffsetInMillis();
                    if (current > maxEnd) {
                        value--;
                    }
                } else {
                    CalendarSystem mincal = gc.getTimeInMillis() >= gregorianCutover ?
                        gcal : getJulianCalendarSystem();
                    CalendarDate d = mincal.getCalendarDate(Long.MIN_VALUE, getZone());
                    long maxEnd = (cal.getDayOfYear(d) - 1) * 24 + d.getHours();
                    maxEnd *= 60;
                    maxEnd += d.getMinutes();
                    maxEnd *= 60;
                    maxEnd += d.getSeconds();
                    maxEnd *= 1000;
                    maxEnd += d.getMillis();
                    value = d.getYear();
                    if (value <= 0) {
                        assert mincal == gcal;
                        value = 1 - value;
                    }
                    if (current < maxEnd) {
                        value--;
                    }
                }
            }
            break;

        default:
            throw new ArrayIndexOutOfBoundsException(field);
        }
        return value;
    }

    /**
     * 返回从该年年初开始的毫秒偏移量。此 Calendar 对象必须已规范化。
     */
    private long getYearOffsetInMillis() {
        long t = (internalGet(DAY_OF_YEAR) - 1) * 24;
        t += internalGet(HOUR_OF_DAY);
        t *= 60;
        t += internalGet(MINUTE);
        t *= 60;
        t += internalGet(SECOND);
        t *= 1000;
        return t + internalGet(MILLISECOND) -
            (internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET));
    }

    @Override
    public Object clone()
    {
        GregorianCalendar other = (GregorianCalendar) super.clone();

        other.gdate = (BaseCalendar.Date) gdate.clone();
        if (cdate != null) {
            if (cdate != gdate) {
                other.cdate = (BaseCalendar.Date) cdate.clone();
            } else {
                other.cdate = other.gdate;
            }
        }
        other.originalFields = null;
        other.zoneOffsets = null;
        return other;
    }

    @Override
    public TimeZone getTimeZone() {
        TimeZone zone = super.getTimeZone();
        // 为了在 CalendarDates 中共享时区
        gdate.setZone(zone);
        if (cdate != null && cdate != gdate) {
            cdate.setZone(zone);
        }
        return zone;
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        // 为了在 CalendarDates 中共享时区
        gdate.setZone(zone);
        if (cdate != null && cdate != gdate) {
            cdate.setZone(zone);
        }
    }

    /**
     * 返回 {@code true} 表示此 {@code GregorianCalendar} 支持周日期。
     *
     * @return {@code true} (总是)
     * @see #getWeekYear()
     * @see #setWeekDate(int,int,int)
     * @see #getWeeksInWeekYear()
     * @since 1.7
     */
    @Override
    public final boolean isWeekDateSupported() {
        return true;
    }

    /**
     * 返回此 {@code GregorianCalendar} 表示的 <a href="#week_year">周年</a>。年份 1 到最大周年的周数的日期具有相同的周年值，该值可能比 {@link Calendar#YEAR YEAR}（日历年）值早一年或晚一年。
     *
     * <p>此方法在计算周年之前调用 {@link Calendar#complete()}。
     *
     * @return 此 {@code GregorianCalendar} 表示的周年。如果 {@link Calendar#ERA ERA} 值为 {@link #BC}，则年份表示为 0 或负数：BC 1 是 0，BC 2
     *         是 -1，BC 3 是 -2，依此类推。
     * @throws IllegalArgumentException
     *         如果在非宽松模式下任何日历字段无效。
     * @see #isWeekDateSupported()
     * @see #getWeeksInWeekYear()
     * @see Calendar#getFirstDayOfWeek()
     * @see Calendar#getMinimalDaysInFirstWeek()
     * @since 1.7
     */
    @Override
    public int getWeekYear() {
        int year = get(YEAR); // 隐式调用 complete()
        if (internalGetEra() == BCE) {
            year = 1 - year;
        }

        // 快速路径，处理不受儒略-格里高利转换影响的格里高利历年
        if (year > gregorianCutoverYear + 1) {
            int weekOfYear = internalGet(WEEK_OF_YEAR);
            if (internalGet(MONTH) == JANUARY) {
                if (weekOfYear >= 52) {
                    --year;
                }
            } else {
                if (weekOfYear == 1) {
                    ++year;
                }
            }
            return year;
        }

        // 通用（慢）路径
        int dayOfYear = internalGet(DAY_OF_YEAR);
        int maxDayOfYear = getActualMaximum(DAY_OF_YEAR);
        int minimalDays = getMinimalDaysInFirstWeek();

        // 在克隆此 GregorianCalendar 之前快速检查年份调整的可能性
        if (dayOfYear > minimalDays && dayOfYear < (maxDayOfYear - 6)) {
            return year;
        }

        // 创建一个用于计算的克隆
        GregorianCalendar cal = (GregorianCalendar) clone();
        cal.setLenient(true);
        // 使用 GMT 以避免中间日期计算影响时间字段
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // 转到该年的第一天，通常是 1 月 1 日
        cal.set(DAY_OF_YEAR, 1);
        cal.complete();

        // 获取该年的第一个星期的第一天
        int delta = getFirstDayOfWeek() - cal.get(DAY_OF_WEEK);
        if (delta != 0) {
            if (delta < 0) {
                delta += 7;
            }
            cal.add(DAY_OF_YEAR, delta);
        }
        int minDayOfYear = cal.get(DAY_OF_YEAR);
        if (dayOfYear < minDayOfYear) {
            if (minDayOfYear <= minimalDays) {
                --year;
            }
        } else {
            cal.set(YEAR, year + 1);
            cal.set(DAY_OF_YEAR, 1);
            cal.complete();
            int del = getFirstDayOfWeek() - cal.get(DAY_OF_WEEK);
            if (del != 0) {
                if (del < 0) {
                    del += 7;
                }
                cal.add(DAY_OF_YEAR, del);
            }
            minDayOfYear = cal.get(DAY_OF_YEAR) - 1;
            if (minDayOfYear == 0) {
                minDayOfYear = 7;
            }
            if (minDayOfYear >= minimalDays) {
                int days = maxDayOfYear - dayOfYear + 1;
                if (days <= (7 - minDayOfYear)) {
                    ++year;
                }
            }
        }
        return year;
    }

    /**
     * 将此 {@code GregorianCalendar} 设置为由日期规范符 - <a href="#week_year">{@code weekYear}</a>，
     * {@code weekOfYear}，和 {@code dayOfWeek} 给出的日期。{@code weekOfYear}
     * 跟随 <a href="#week_and_year">{@code WEEK_OF_YEAR}
     * 编号</a>。  {@code dayOfWeek} 值必须是 {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 值之一：{@link
     * Calendar#SUNDAY SUNDAY} 到 {@link Calendar#SATURDAY SATURDAY}。
     *
     * <p>请注意，数字星期表示法与 ISO 8601 标准不同，当 {@code
     * getFirstDayOfWeek()} 为 {@code MONDAY} 且 {@code
     * getMinimalDaysInFirstWeek()} 为 4 时，{@code weekOfYear}
     * 编号与标准兼容。
     *
     * <p>与 {@code set} 方法不同，所有日历字段和时间值在返回时都会计算。
     *
     * <p>如果 {@code weekOfYear} 超出 {@code weekYear} 中的有效周编号范围，在宽松模式下调整 {@code weekYear}
     * 和 {@code weekOfYear} 值，或在非宽松模式下抛出 {@code IllegalArgumentException}。
     *
     * @param weekYear    周年
     * @param weekOfYear  基于 {@code weekYear} 的周编号
     * @param dayOfWeek   星期值：{@link #DAY_OF_WEEK DAY_OF_WEEK} 字段的常量之一：
     *                    {@link Calendar#SUNDAY SUNDAY}，...，
     *                    {@link Calendar#SATURDAY SATURDAY}。
     * @exception IllegalArgumentException
     *            如果给定的任何日期规范符无效，
     *            或在非宽松模式下任何日历字段与给定的日期规范符不一致
     * @see GregorianCalendar#isWeekDateSupported()
     * @see Calendar#getFirstDayOfWeek()
     * @see Calendar#getMinimalDaysInFirstWeek()
     * @since 1.7
     */
    @Override
    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        if (dayOfWeek < SUNDAY || dayOfWeek > SATURDAY) {
            throw new IllegalArgumentException("invalid dayOfWeek: " + dayOfWeek);
        }

        // 为了避免日期计算改变时间字段，使用具有 GMT 时区的克隆。
        GregorianCalendar gc = (GregorianCalendar) clone();
        gc.setLenient(true);
        int era = gc.get(ERA);
        gc.clear();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(ERA, era);
        gc.set(YEAR, weekYear);
        gc.set(WEEK_OF_YEAR, 1);
        gc.set(DAY_OF_WEEK, getFirstDayOfWeek());
        int days = dayOfWeek - getFirstDayOfWeek();
        if (days < 0) {
            days += 7;
        }
        days += 7 * (weekOfYear - 1);
        if (days != 0) {
            gc.add(DAY_OF_YEAR, days);
        } else {
            gc.complete();
        }

        if (!isLenient() &&
            (gc.getWeekYear() != weekYear
             || gc.internalGet(WEEK_OF_YEAR) != weekOfYear
             || gc.internalGet(DAY_OF_WEEK) != dayOfWeek)) {
            throw new IllegalArgumentException();
        }

        set(ERA, gc.internalGet(ERA));
        set(YEAR, gc.internalGet(YEAR));
        set(MONTH, gc.internalGet(MONTH));
        set(DAY_OF_MONTH, gc.internalGet(DAY_OF_MONTH));

        // 为了避免在非宽松模式下抛出 IllegalArgumentException，内部设置 WEEK_OF_YEAR
        internalSet(WEEK_OF_YEAR, weekOfYear);
        complete();
    }

    /**
     * 返回此 {@code GregorianCalendar} 表示的 <a href="#week_year">周年的周数</a>。
     *
     * <p>例如，如果此 {@code GregorianCalendar} 的日期是 2008 年 12 月 31 日，并且启用了 <a href="#iso8601_compatible_setting">ISO 8601 兼容设置</a>，此方法将返回 53，表示 2008 年 12 月 29 日到 2010 年 1 月 3 日的周期，而 {@link
     * #getActualMaximum(int) getActualMaximum(WEEK_OF_YEAR)} 将返回 52，表示 2007 年 12 月 31 日到 2008 年 12 月 28 日的周期。
     *
     * @return 周年的周数。
     * @see Calendar#WEEK_OF_YEAR
     * @see #getWeekYear()
     * @see #getActualMaximum(int)
     * @since 1.7
     */
    @Override
    public int getWeeksInWeekYear() {
        GregorianCalendar gc = getNormalizedCalendar();
        int weekYear = gc.getWeekYear();
        if (weekYear == gc.internalGet(YEAR)) {
            return gc.getActualMaximum(WEEK_OF_YEAR);
        }

        // 使用第 2 周来计算 WEEK_OF_YEAR 的最大值
        if (gc == this) {
            gc = (GregorianCalendar) gc.clone();
        }
        gc.setWeekDate(weekYear, 2, internalGet(DAY_OF_WEEK));
        return gc.getActualMaximum(WEEK_OF_YEAR);
    }

/////////////////////////////
// Time => Fields computation
/////////////////////////////

    /**
     * 与 gdate 对应的固定日期。如果值为 Long.MIN_VALUE，则固定日期值未知。目前，儒略日历日期未缓存。
     */
    transient private long cachedFixedDate = Long.MIN_VALUE;

    /**
     * 将时间值（从 <a
     * href="Calendar.html#Epoch">纪元</a> 开始的毫秒偏移量）转换为日历字段值。
     * 不会先重新计算时间；要重新计算时间，然后计算字段，调用 <code>complete</code> 方法。
     *
     * @see Calendar#complete
     */
    @Override
    protected void computeFields() {
        int mask;
        if (isPartiallyNormalized()) {
            // 确定需要计算哪些日历字段。
            mask = getSetStateFields();
            int fieldMask = ~mask & ALL_FIELDS;
            // 如果 calsys == null，我们必须调用 computeTime 以设置 calsys 和 cdate。（6263644）
            if (fieldMask != 0 || calsys == null) {
                mask |= computeFields(fieldMask,
                                      mask & (ZONE_OFFSET_MASK|DST_OFFSET_MASK));
                assert mask == ALL_FIELDS;
            }
        } else {
            mask = ALL_FIELDS;
            computeFields(mask, 0);
        }
        // 计算所有字段后，将字段状态设置为 `COMPUTED'。
        setFieldsComputed(mask);
    }

    /**
     * 此 computeFields 实现了从 UTC
     * （从纪元开始的毫秒偏移量）到日历
     * 字段值的转换。fieldMask 指定要将哪些字段的设置状态更改为 COMPUTED，尽管所有字段都设置为
     * 正确的值。这是为了修复 4685354。
     *
     * @param fieldMask 位掩码，指定要更改设置状态的字段。
     * @param tzMask 位掩码，指定用于时间计算的时间区偏移字段
     * @return 一个新的字段掩码，指示实际已设置的字段值。
     */
    private int computeFields(int fieldMask, int tzMask) {
        int zoneOffset = 0;
        TimeZone tz = getZone();
        if (zoneOffsets == null) {
            zoneOffsets = new int[2];
        }
        if (tzMask != (ZONE_OFFSET_MASK|DST_OFFSET_MASK)) {
            if (tz instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo)tz).getOffsets(time, zoneOffsets);
            } else {
                zoneOffset = tz.getOffset(time);
                zoneOffsets[0] = tz.getRawOffset();
                zoneOffsets[1] = zoneOffset - zoneOffsets[0];
            }
        }
        if (tzMask != 0) {
            if (isFieldSet(tzMask, ZONE_OFFSET)) {
                zoneOffsets[0] = internalGet(ZONE_OFFSET);
            }
            if (isFieldSet(tzMask, DST_OFFSET)) {
                zoneOffsets[1] = internalGet(DST_OFFSET);
            }
            zoneOffset = zoneOffsets[0] + zoneOffsets[1];
        }


                    // 通过分别计算时间和时区偏移，我们可以处理比以前实现更广泛的时间+时区偏移范围。
        // the wider range of time+zoneOffset than the previous
        // implementation.
        long fixedDate = zoneOffset / ONE_DAY;
        int timeOfDay = zoneOffset % (int)ONE_DAY;
        fixedDate += time / ONE_DAY;
        timeOfDay += (int) (time % ONE_DAY);
        if (timeOfDay >= ONE_DAY) {
            timeOfDay -= ONE_DAY;
            ++fixedDate;
        } else {
            while (timeOfDay < 0) {
                timeOfDay += ONE_DAY;
                --fixedDate;
            }
        }
        fixedDate += EPOCH_OFFSET;

        int era = CE;
        int year;
        if (fixedDate >= gregorianCutoverDate) {
            // 处理公历日期。
            assert cachedFixedDate == Long.MIN_VALUE || gdate.isNormalized()
                        : "cache control: not normalized";
            assert cachedFixedDate == Long.MIN_VALUE ||
                   gcal.getFixedDate(gdate.getNormalizedYear(),
                                          gdate.getMonth(),
                                          gdate.getDayOfMonth(), gdate)
                                == cachedFixedDate
                        : "cache control: inconsistency" +
                          ", cachedFixedDate=" + cachedFixedDate +
                          ", computed=" +
                          gcal.getFixedDate(gdate.getNormalizedYear(),
                                                 gdate.getMonth(),
                                                 gdate.getDayOfMonth(),
                                                 gdate) +
                          ", date=" + gdate;

            // 查看是否可以使用 gdate 来避免日期计算。
            if (fixedDate != cachedFixedDate) {
                gcal.getCalendarDateFromFixedDate(gdate, fixedDate);
                cachedFixedDate = fixedDate;
            }

            year = gdate.getYear();
            if (year <= 0) {
                year = 1 - year;
                era = BCE;
            }
            calsys = gcal;
            cdate = gdate;
            assert cdate.getDayOfWeek() > 0 : "dow=" + cdate.getDayOfWeek() + ", date=" + cdate;
        } else {
            // 处理儒略历日期。
            calsys = getJulianCalendarSystem();
            cdate = (BaseCalendar.Date) jcal.newCalendarDate(getZone());
            jcal.getCalendarDateFromFixedDate(cdate, fixedDate);
            Era e = cdate.getEra();
            if (e == jeras[0]) {
                era = BCE;
            }
            year = cdate.getYear();
        }

        // 始终设置 ERA 和 YEAR 值。
        internalSet(ERA, era);
        internalSet(YEAR, year);
        int mask = fieldMask | (ERA_MASK | YEAR_MASK);

        int month = cdate.getMonth() - 1; // 从0开始
        int dayOfMonth = cdate.getDayOfMonth();

        // 设置基本日期字段。
        if ((fieldMask & (MONTH_MASK | DAY_OF_MONTH_MASK | DAY_OF_WEEK_MASK))
            != 0) {
            internalSet(MONTH, month);
            internalSet(DAY_OF_MONTH, dayOfMonth);
            internalSet(DAY_OF_WEEK, cdate.getDayOfWeek());
            mask |= MONTH_MASK | DAY_OF_MONTH_MASK | DAY_OF_WEEK_MASK;
        }

        if ((fieldMask & (HOUR_OF_DAY_MASK | AM_PM_MASK | HOUR_MASK
                          | MINUTE_MASK | SECOND_MASK | MILLISECOND_MASK)) != 0) {
            if (timeOfDay != 0) {
                int hours = timeOfDay / ONE_HOUR;
                internalSet(HOUR_OF_DAY, hours);
                internalSet(AM_PM, hours / 12); // 假设 AM == 0
                internalSet(HOUR, hours % 12);
                int r = timeOfDay % ONE_HOUR;
                internalSet(MINUTE, r / ONE_MINUTE);
                r %= ONE_MINUTE;
                internalSet(SECOND, r / ONE_SECOND);
                internalSet(MILLISECOND, r % ONE_SECOND);
            } else {
                internalSet(HOUR_OF_DAY, 0);
                internalSet(AM_PM, AM);
                internalSet(HOUR, 0);
                internalSet(MINUTE, 0);
                internalSet(SECOND, 0);
                internalSet(MILLISECOND, 0);
            }
            mask |= (HOUR_OF_DAY_MASK | AM_PM_MASK | HOUR_MASK
                     | MINUTE_MASK | SECOND_MASK | MILLISECOND_MASK);
        }

        if ((fieldMask & (ZONE_OFFSET_MASK | DST_OFFSET_MASK)) != 0) {
            internalSet(ZONE_OFFSET, zoneOffsets[0]);
            internalSet(DST_OFFSET, zoneOffsets[1]);
            mask |= (ZONE_OFFSET_MASK | DST_OFFSET_MASK);
        }

        if ((fieldMask & (DAY_OF_YEAR_MASK | WEEK_OF_YEAR_MASK | WEEK_OF_MONTH_MASK | DAY_OF_WEEK_IN_MONTH_MASK)) != 0) {
            int normalizedYear = cdate.getNormalizedYear();
            long fixedDateJan1 = calsys.getFixedDate(normalizedYear, 1, 1, cdate);
            int dayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
            long fixedDateMonth1 = fixedDate - dayOfMonth + 1;
            int cutoverGap = 0;
            int cutoverYear = (calsys == gcal) ? gregorianCutoverYear : gregorianCutoverYearJulian;
            int relativeDayOfMonth = dayOfMonth - 1;

            // 如果我们在转换年份，需要一些特殊处理。
            if (normalizedYear == cutoverYear) {
                // 需要处理“缺失”的天数。
                if (gregorianCutoverYearJulian <= gregorianCutoverYear) {
                    // 我们需要确定我们所处的位置。转换差距甚至可能超过一年。 (一年差异在约48667年。)
                    fixedDateJan1 = getFixedDateJan1(cdate, fixedDate);
                    if (fixedDate >= gregorianCutoverDate) {
                        fixedDateMonth1 = getFixedDateMonth1(cdate, fixedDate);
                    }
                }
                int realDayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
                cutoverGap = dayOfYear - realDayOfYear;
                dayOfYear = realDayOfYear;
                relativeDayOfMonth = (int)(fixedDate - fixedDateMonth1);
            }
            internalSet(DAY_OF_YEAR, dayOfYear);
            internalSet(DAY_OF_WEEK_IN_MONTH, relativeDayOfMonth / 7 + 1);

            int weekOfYear = getWeekNumber(fixedDateJan1, fixedDate);

            // 规范要求按照 ISO8601 风格计算 WEEK_OF_YEAR。这会带来一些问题。
            if (weekOfYear == 0) {
                // 如果日期属于上一年的最后一周，使用上一年“12/31”的周数。如果上一年是公历转换年，需要特别处理。通常，1月1日的前一天是12月31日，但在 GregorianCalendar 中并不总是如此。
                long fixedDec31 = fixedDateJan1 - 1;
                long prevJan1 = fixedDateJan1 - 365;
                if (normalizedYear > (cutoverYear + 1)) {
                    if (CalendarUtils.isGregorianLeapYear(normalizedYear - 1)) {
                        --prevJan1;
                    }
                } else if (normalizedYear <= gregorianCutoverYearJulian) {
                    if (CalendarUtils.isJulianLeapYear(normalizedYear - 1)) {
                        --prevJan1;
                    }
                } else {
                    BaseCalendar calForJan1 = calsys;
                    //int prevYear = normalizedYear - 1;
                    int prevYear = getCalendarDate(fixedDec31).getNormalizedYear();
                    if (prevYear == gregorianCutoverYear) {
                        calForJan1 = getCutoverCalendarSystem();
                        if (calForJan1 == jcal) {
                            prevJan1 = calForJan1.getFixedDate(prevYear,
                                                               BaseCalendar.JANUARY,
                                                               1,
                                                               null);
                        } else {
                            prevJan1 = gregorianCutoverDate;
                            calForJan1 = gcal;
                        }
                    } else if (prevYear <= gregorianCutoverYearJulian) {
                        calForJan1 = getJulianCalendarSystem();
                        prevJan1 = calForJan1.getFixedDate(prevYear,
                                                           BaseCalendar.JANUARY,
                                                           1,
                                                           null);
                    }
                }
                weekOfYear = getWeekNumber(prevJan1, fixedDec31);
            } else {
                if (normalizedYear > gregorianCutoverYear ||
                    normalizedYear < (gregorianCutoverYearJulian - 1)) {
                    // 普通年份
                    if (weekOfYear >= 52) {
                        long nextJan1 = fixedDateJan1 + 365;
                        if (cdate.isLeapYear()) {
                            nextJan1++;
                        }
                        long nextJan1st = BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6,
                                                                                  getFirstDayOfWeek());
                        int ndays = (int)(nextJan1st - nextJan1);
                        if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
                            // 第一周包含该日期。
                            weekOfYear = 1;
                        }
                    }
                } else {
                    BaseCalendar calForJan1 = calsys;
                    int nextYear = normalizedYear + 1;
                    if (nextYear == (gregorianCutoverYearJulian + 1) &&
                        nextYear < gregorianCutoverYear) {
                        // 如果差距超过一年。
                        nextYear = gregorianCutoverYear;
                    }
                    if (nextYear == gregorianCutoverYear) {
                        calForJan1 = getCutoverCalendarSystem();
                    }

                    long nextJan1;
                    if (nextYear > gregorianCutoverYear
                        || gregorianCutoverYearJulian == gregorianCutoverYear
                        || nextYear == gregorianCutoverYearJulian) {
                        nextJan1 = calForJan1.getFixedDate(nextYear,
                                                           BaseCalendar.JANUARY,
                                                           1,
                                                           null);
                    } else {
                        nextJan1 = gregorianCutoverDate;
                        calForJan1 = gcal;
                    }

                    long nextJan1st = BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6,
                                                                              getFirstDayOfWeek());
                    int ndays = (int)(nextJan1st - nextJan1);
                    if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
                        // 第一周包含该日期。
                        weekOfYear = 1;
                    }
                }
            }
            internalSet(WEEK_OF_YEAR, weekOfYear);
            internalSet(WEEK_OF_MONTH, getWeekNumber(fixedDateMonth1, fixedDate));
            mask |= (DAY_OF_YEAR_MASK | WEEK_OF_YEAR_MASK | WEEK_OF_MONTH_MASK | DAY_OF_WEEK_IN_MONTH_MASK);
        }
        return mask;
    }

    /**
     * 返回固定日期1和固定日期之间周数。使用 getFirstDayOfWeek-getMinimalDaysInFirstWeek 规则计算周数。
     *
     * @param fixedDay1 周期第一天的固定日期
     * @param fixedDate 周期最后一天的固定日期
     * @return 给定周期的周数
     */
    private int getWeekNumber(long fixedDay1, long fixedDate) {
        // 我们可以始终使用 `gcal'，因为在此计算中，儒略历和公历是相同的。
        long fixedDay1st = Gregorian.getDayOfWeekDateOnOrBefore(fixedDay1 + 6,
                                                                getFirstDayOfWeek());
        int ndays = (int)(fixedDay1st - fixedDay1);
        assert ndays <= 7;
        if (ndays >= getMinimalDaysInFirstWeek()) {
            fixedDay1st -= 7;
        }
        int normalizedDayOfPeriod = (int)(fixedDate - fixedDay1st);
        if (normalizedDayOfPeriod >= 0) {
            return normalizedDayOfPeriod / 7 + 1;
        }
        return CalendarUtils.floorDivide(normalizedDayOfPeriod, 7) + 1;
    }

    /**
     * 将日历字段值转换为时间值（从 <a href="Calendar.html#Epoch">纪元</a> 开始的毫秒偏移量）。
     *
     * @exception IllegalArgumentException 如果任何日历字段无效。
     */
    @Override
    protected void computeTime() {
        // 在非宽松模式下，对外部设置的日历字段进行简要检查。通过此检查，字段值将存储在 originalFields[] 中，以查看它们是否在稍后被规范化。
        if (!isLenient()) {
            if (originalFields == null) {
                originalFields = new int[FIELD_COUNT];
            }
            for (int field = 0; field < FIELD_COUNT; field++) {
                int value = internalGet(field);
                if (isExternallySet(field)) {
                    // 快速验证任何超出范围的值
                    if (value < getMinimum(field) || value > getMaximum(field)) {
                        throw new IllegalArgumentException(getFieldName(field));
                    }
                }
                originalFields[field] = value;
            }
        }

        // 让超类确定用于计算时间的日历字段。
        int fieldMask = selectFields();

        // 年份默认为纪元开始。我们不检查 fieldMask 中的 YEAR，因为 YEAR 是确定日期的必填字段。
        int year = isSet(YEAR) ? internalGet(YEAR) : EPOCH_YEAR;

        int era = internalGetEra();
        if (era == BCE) {
            year = 1 - year;
        } else if (era != CE) {
            // 即使在宽松模式下，我们也禁止除 CE 和 BCE 以外的 ERA 值。
            // (宽松模式下可以应用与 add()/roll() 相同的规范化规则。但此检查保持不变以确保 1.5 版本的兼容性。)
            throw new IllegalArgumentException("Invalid era");
        }

        // 如果年份为 0 或负数，我们稍后需要设置 ERA 值。
        if (year <= 0 && !isSet(ERA)) {
            fieldMask |= ERA_MASK;
            setFieldsComputed(ERA_MASK);
        }

        // 计算一天中的时间。我们依赖于未设置字段为 0 的约定。
        long timeOfDay = 0;
        if (isFieldSet(fieldMask, HOUR_OF_DAY)) {
            timeOfDay += (long) internalGet(HOUR_OF_DAY);
        } else {
            timeOfDay += internalGet(HOUR);
            // AM_PM 的默认值为 0，表示 AM。
            if (isFieldSet(fieldMask, AM_PM)) {
                timeOfDay += 12 * internalGet(AM_PM);
            }
        }
        timeOfDay *= 60;
        timeOfDay += internalGet(MINUTE);
        timeOfDay *= 60;
        timeOfDay += internalGet(SECOND);
        timeOfDay *= 1000;
        timeOfDay += internalGet(MILLISECOND);


                    // 将一天中的时间转换为天数和从午夜开始的毫秒偏移量。
        // millisecond offset from midnight.
        long fixedDate = timeOfDay / ONE_DAY;
        timeOfDay %= ONE_DAY;
        while (timeOfDay < 0) {
            timeOfDay += ONE_DAY;
            --fixedDate;
        }

        // 计算自公元1年1月1日（格里高利历）以来的固定日期。
        calculateFixedDate: {
            long gfd, jfd;
            if (year > gregorianCutoverYear && year > gregorianCutoverYearJulian) {
                gfd = fixedDate + getFixedDate(gcal, year, fieldMask);
                if (gfd >= gregorianCutoverDate) {
                    fixedDate = gfd;
                    break calculateFixedDate;
                }
                jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
            } else if (year < gregorianCutoverYear && year < gregorianCutoverYearJulian) {
                jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
                if (jfd < gregorianCutoverDate) {
                    fixedDate = jfd;
                    break calculateFixedDate;
                }
                gfd = jfd;
            } else {
                jfd = fixedDate + getFixedDate(getJulianCalendarSystem(), year, fieldMask);
                gfd = fixedDate + getFixedDate(gcal, year, fieldMask);
            }

            // 现在我们必须确定这是哪个历法日期。

            // 如果日期是从 Julian 历法年的开始计算的，则使用 jfd；
            if (isFieldSet(fieldMask, DAY_OF_YEAR) || isFieldSet(fieldMask, WEEK_OF_YEAR)) {
                if (gregorianCutoverYear == gregorianCutoverYearJulian) {
                    fixedDate = jfd;
                    break calculateFixedDate;
                } else if (year == gregorianCutoverYear) {
                    fixedDate = gfd;
                    break calculateFixedDate;
                }
            }

            if (gfd >= gregorianCutoverDate) {
                if (jfd >= gregorianCutoverDate) {
                    fixedDate = gfd;
                } else {
                    // 日期处于“重叠”期。无法区分。使用之前的日期计算。
                    if (calsys == gcal || calsys == null) {
                        fixedDate = gfd;
                    } else {
                        fixedDate = jfd;
                    }
                }
            } else {
                if (jfd < gregorianCutoverDate) {
                    fixedDate = jfd;
                } else {
                    // 日期处于“缺失”期。
                    if (!isLenient()) {
                        throw new IllegalArgumentException("指定的日期不存在");
                    }
                    // 为了兼容性，使用 Julian 日期，这将生成一个格里高利日期。
                    fixedDate = jfd;
                }
            }
        }

        // millis 表示以毫秒为单位的本地时钟时间。
        long millis = (fixedDate - EPOCH_OFFSET) * ONE_DAY + timeOfDay;

        // 计算时区偏移量和夏令时偏移量。这里存在两个潜在的模棱两可之处。
        // 我们假设切换时间为 2:00 am（本地时间）进行讨论。
        // 1. 进入夏令时的过渡。这里，指定时间为 2:00 am - 2:59 am
        //    可能是标准时间或夏令时。但是，2:00 am 是一个无效的表示（表示从 1:59:59 am 标准时间跳到 3:00:00 am 夏令时）。
        //    我们假设标准时间。
        // 2. 退出夏令时的过渡。这里，指定时间为 1:00 am - 1:59 am
        //    可能是标准时间或夏令时。两者都是有效的表示（表示从 1:59:59 夏令时跳到 1:00:00 标准时间）。
        //    再次，我们假设标准时间。
        // 我们使用 TimeZone 对象，除非用户显式设置了 ZONE_OFFSET
        // 或 DST_OFFSET 字段；然后我们使用这些字段。
        TimeZone zone = getZone();
        if (zoneOffsets == null) {
            zoneOffsets = new int[2];
        }
        int tzMask = fieldMask & (ZONE_OFFSET_MASK|DST_OFFSET_MASK);
        if (tzMask != (ZONE_OFFSET_MASK|DST_OFFSET_MASK)) {
            if (zone instanceof ZoneInfo) {
                ((ZoneInfo)zone).getOffsetsByWall(millis, zoneOffsets);
            } else {
                int gmtOffset = isFieldSet(fieldMask, ZONE_OFFSET) ?
                                    internalGet(ZONE_OFFSET) : zone.getRawOffset();
                zone.getOffsets(millis - gmtOffset, zoneOffsets);
            }
        }
        if (tzMask != 0) {
            if (isFieldSet(tzMask, ZONE_OFFSET)) {
                zoneOffsets[0] = internalGet(ZONE_OFFSET);
            }
            if (isFieldSet(tzMask, DST_OFFSET)) {
                zoneOffsets[1] = internalGet(DST_OFFSET);
            }
        }

        // 调整时区偏移量以获取 UTC 时间。
        millis -= zoneOffsets[0] + zoneOffsets[1];

        // 设置此日历的时间（以毫秒为单位）
        time = millis;

        int mask = computeFields(fieldMask | getSetStateFields(), tzMask);

        if (!isLenient()) {
            for (int field = 0; field < FIELD_COUNT; field++) {
                if (!isExternallySet(field)) {
                    continue;
                }
                if (originalFields[field] != internalGet(field)) {
                    String s = originalFields[field] + " -> " + internalGet(field);
                    // 恢复原始字段值
                    System.arraycopy(originalFields, 0, fields, 0, fields.length);
                    throw new IllegalArgumentException(getFieldName(field) + ": " + s);
                }
            }
        }
        setFieldsNormalized(mask);
    }

    /**
     * 使用给定的年份和指定的日历字段计算格里高利历或儒略历的固定日期。
     *
     * @param cal 用于日期计算的日历系统
     * @param year 规范化的年份编号，0 表示公元前1年，-1 表示公元前2年，等等。
     * @param fieldMask 用于日期计算的日历字段
     * @return 固定日期
     * @see Calendar#selectFields
     */
    private long getFixedDate(BaseCalendar cal, int year, int fieldMask) {
        int month = JANUARY;
        if (isFieldSet(fieldMask, MONTH)) {
            // 无需检查 MONTH 是否已设置（无需调用 isSet(MONTH)）
            // 因为其未设置值恰好是 JANUARY（0）。
            month = internalGet(MONTH);

            // 如果月份超出范围，将其调整到范围内
            if (month > DECEMBER) {
                year += month / 12;
                month %= 12;
            } else if (month < JANUARY) {
                int[] rem = new int[1];
                year += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
        }

        // 获取自公元1年1月1日（格里高利历）以来的固定日期。我们处于 'year' 年 'month' 月的第一天。
        long fixedDate = cal.getFixedDate(year, month + 1, 1,
                                          cal == gcal ? gdate : null);
        if (isFieldSet(fieldMask, MONTH)) {
            // 基于月份的计算
            if (isFieldSet(fieldMask, DAY_OF_MONTH)) {
                // 我们处于月份的第一天。如果设置了 DAY_OF_MONTH，则只需添加偏移量。如果 isSet 调用返回 false，
                // 则表示 DAY_OF_MONTH 仅因为选定的组合而被选中。我们不需要添加任何内容，因为默认值是 1。
                if (isSet(DAY_OF_MONTH)) {
                    // 为了避免 DAY_OF_MONTH-1 的下溢，先添加 DAY_OF_MONTH，然后减去 1。
                    fixedDate += internalGet(DAY_OF_MONTH);
                    fixedDate--;
                }
            } else {
                if (isFieldSet(fieldMask, WEEK_OF_MONTH)) {
                    long firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6,
                                                                                  getFirstDayOfWeek());
                    // 如果第一周有足够的天数，则移动到前一周。
                    if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
                        firstDayOfWeek -= 7;
                    }
                    if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                        firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6,
                                                                                 internalGet(DAY_OF_WEEK));
                    }
                    // 在宽松模式下，我们将前几个月的天数视为指定的 WEEK_OF_MONTH 的一部分。参见 4633646。
                    fixedDate = firstDayOfWeek + 7 * (internalGet(WEEK_OF_MONTH) - 1);
                } else {
                    int dayOfWeek;
                    if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                        dayOfWeek = internalGet(DAY_OF_WEEK);
                    } else {
                        dayOfWeek = getFirstDayOfWeek();
                    }
                    // 我们基于星期几在月份中的位置进行计算。唯一的复杂之处在于如果星期几在月份中的位置是负数。
                    int dowim;
                    if (isFieldSet(fieldMask, DAY_OF_WEEK_IN_MONTH)) {
                        dowim = internalGet(DAY_OF_WEEK_IN_MONTH);
                    } else {
                        dowim = 1;
                    }
                    if (dowim >= 0) {
                        fixedDate = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + (7 * dowim) - 1,
                                                                            dayOfWeek);
                    } else {
                        // 转到指定周边界的下一周的第一天。
                        int lastDate = monthLength(month, year) + (7 * (dowim + 1));
                        // 然后，获取最后一天之前的星期几日期。
                        fixedDate = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + lastDate - 1,
                                                                            dayOfWeek);
                    }
                }
            }
        } else {
            if (year == gregorianCutoverYear && cal == gcal
                && fixedDate < gregorianCutoverDate
                && gregorianCutoverYear != gregorianCutoverYearJulian) {
                // 该年的1月1日不存在。使用 gregorianCutoverDate 作为该年的第一天。
                fixedDate = gregorianCutoverDate;
            }
            // 我们处于该年的第一天。
            if (isFieldSet(fieldMask, DAY_OF_YEAR)) {
                // 添加偏移量，然后减去 1。（确保避免下溢。）
                fixedDate += internalGet(DAY_OF_YEAR);
                fixedDate--;
            } else {
                long firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6,
                                                                              getFirstDayOfWeek());
                // 如果第一周有足够的天数，则移动到前一周。
                if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
                    firstDayOfWeek -= 7;
                }
                if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                    int dayOfWeek = internalGet(DAY_OF_WEEK);
                    if (dayOfWeek != getFirstDayOfWeek()) {
                        firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6,
                                                                                 dayOfWeek);
                    }
                }
                fixedDate = firstDayOfWeek + 7 * ((long)internalGet(WEEK_OF_YEAR) - 1);
            }
        }

        return fixedDate;
    }

    /**
     * 如果此对象已规范化（所有字段和时间同步），则返回此对象。否则，在宽松模式下调用 complete() 后返回克隆对象。
     */
    private GregorianCalendar getNormalizedCalendar() {
        GregorianCalendar gc;
        if (isFullyNormalized()) {
            gc = this;
        } else {
            // 创建克隆并规范化日历字段
            gc = (GregorianCalendar) this.clone();
            gc.setLenient(true);
            gc.complete();
        }
        return gc;
    }

    /**
     * 返回 Julian 日历系统实例（单例）。'jcal' 和 'jeras' 在返回时设置。
     */
    private static synchronized BaseCalendar getJulianCalendarSystem() {
        if (jcal == null) {
            jcal = (JulianCalendar) CalendarSystem.forName("julian");
            jeras = jcal.getEras();
        }
        return jcal;
    }

    /**
     * 返回切分日期之前的日期的日历系统。如果切分日期是1月1日，则返回格里高利历。否则，返回儒略历。
     */
    private BaseCalendar getCutoverCalendarSystem() {
        if (gregorianCutoverYearJulian < gregorianCutoverYear) {
            return gcal;
        }
        return getJulianCalendarSystem();
    }

    /**
     * 确定指定的年份（规范化）是否为格里高利历切分年。此对象必须已规范化。
     */
    private boolean isCutoverYear(int normalizedYear) {
        int cutoverYear = (calsys == gcal) ? gregorianCutoverYear : gregorianCutoverYearJulian;
        return normalizedYear == cutoverYear;
    }

    /**
     * 返回指定日期之前一年的第一天（通常是1月1日）的固定日期。
     *
     * @param date 计算该年第一天的日期。该日期必须在切分年（格里高利历或儒略历）中。
     * @param fixedDate 日期的固定日期表示形式
     */
    private long getFixedDateJan1(BaseCalendar.Date date, long fixedDate) {
        assert date.getNormalizedYear() == gregorianCutoverYear ||
            date.getNormalizedYear() == gregorianCutoverYearJulian;
        if (gregorianCutoverYear != gregorianCutoverYearJulian) {
            if (fixedDate >= gregorianCutoverDate) {
                // 切分日期之前的日期在同一年（格里高利历）中不存在。因此，该年没有1月1日。使用切分日期作为该年的第一天。
                return gregorianCutoverDate;
            }
        }
        // 规范化年份的1月1日应该存在。
        BaseCalendar juliancal = getJulianCalendarSystem();
        return juliancal.getFixedDate(date.getNormalizedYear(), BaseCalendar.JANUARY, 1, null);
    }


                /**
     * 返回指定日期之前月份的第一天的固定日期（通常是该月的1号）。
     *
     * @param date 计算月份第一天的日期。该日期必须在转换年（格里高利或儒略）中。
     * @param fixedDate 日期的固定日期表示。
     */
    private long getFixedDateMonth1(BaseCalendar.Date date, long fixedDate) {
        assert date.getNormalizedYear() == gregorianCutoverYear ||
            date.getNormalizedYear() == gregorianCutoverYearJulian;
        BaseCalendar.Date gCutover = getGregorianCutoverDate();
        if (gCutover.getMonth() == BaseCalendar.JANUARY
            && gCutover.getDayOfMonth() == 1) {
            // 转换发生在1月1日。
            return fixedDate - date.getDayOfMonth() + 1;
        }

        long fixedDateMonth1;
        // 转换发生在该年中的某个时间。
        if (date.getMonth() == gCutover.getMonth()) {
            // 转换发生在该月。
            BaseCalendar.Date jLastDate = getLastJulianDate();
            if (gregorianCutoverYear == gregorianCutoverYearJulian
                && gCutover.getMonth() == jLastDate.getMonth()) {
                // “间隔”发生在同一月。
                fixedDateMonth1 = jcal.getFixedDate(date.getNormalizedYear(),
                                                    date.getMonth(),
                                                    1,
                                                    null);
            } else {
                // 使用转换日期作为该月的第一天。
                fixedDateMonth1 = gregorianCutoverDate;
            }
        } else {
            // 转换发生在该月之前。
            fixedDateMonth1 = fixedDate - date.getDayOfMonth() + 1;
        }

        return fixedDateMonth1;
    }

    /**
     * 从指定的固定日期生成一个CalendarDate。
     *
     * @param fd 固定日期
     */
    private BaseCalendar.Date getCalendarDate(long fd) {
        BaseCalendar cal = (fd >= gregorianCutoverDate) ? gcal : getJulianCalendarSystem();
        BaseCalendar.Date d = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    /**
     * 返回格里高利转换日期作为BaseCalendar.Date。该日期是格里高利日期。
     */
    private BaseCalendar.Date getGregorianCutoverDate() {
        return getCalendarDate(gregorianCutoverDate);
    }

    /**
     * 返回格里高利转换日期前一天作为BaseCalendar.Date。该日期是儒略日期。
     */
    private BaseCalendar.Date getLastJulianDate() {
        return getCalendarDate(gregorianCutoverDate - 1);
    }

    /**
     * 返回指定年份中指定月份的长度。年份编号必须是标准化的。
     *
     * @see #isLeapYear(int)
     */
    private int monthLength(int month, int year) {
        return isLeapYear(year) ? LEAP_MONTH_LENGTH[month] : MONTH_LENGTH[month];
    }

    /**
     * 返回由internalGet(YEAR)提供的年份中指定月份的长度。
     *
     * @see #isLeapYear(int)
     */
    private int monthLength(int month) {
        int year = internalGet(YEAR);
        if (internalGetEra() == BCE) {
            year = 1 - year;
        }
        return monthLength(month, year);
    }

    private int actualMonthLength() {
        int year = cdate.getNormalizedYear();
        if (year != gregorianCutoverYear && year != gregorianCutoverYearJulian) {
            return calsys.getMonthLength(cdate);
        }
        BaseCalendar.Date date = (BaseCalendar.Date) cdate.clone();
        long fd = calsys.getFixedDate(date);
        long month1 = getFixedDateMonth1(date, fd);
        long next1 = month1 + calsys.getMonthLength(date);
        if (next1 < gregorianCutoverDate) {
            return (int)(next1 - month1);
        }
        if (cdate != gdate) {
            date = (BaseCalendar.Date) gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        }
        gcal.getCalendarDateFromFixedDate(date, next1);
        next1 = getFixedDateMonth1(date, next1);
        return (int)(next1 - month1);
    }

    /**
     * 返回指定年份的长度（以天为单位）。年份必须是标准化的。
     */
    private int yearLength(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    /**
     * 返回由internalGet(YEAR)提供的年份的长度（以天为单位）。
     */
    private int yearLength() {
        int year = internalGet(YEAR);
        if (internalGetEra() == BCE) {
            year = 1 - year;
        }
        return yearLength(year);
    }

    /**
     * 在进行如add(MONTH)，add(YEAR)等调整后，我们不希望月份跳变。例如，我们不希望1月31日加1个月后跳到3月3日，而是希望跳到2月28日。可能会遇到此问题的调整调用此方法以保留正确的月份。
     */
    private void pinDayOfMonth() {
        int year = internalGet(YEAR);
        int monthLen;
        if (year > gregorianCutoverYear || year < gregorianCutoverYearJulian) {
            monthLen = monthLength(internalGet(MONTH));
        } else {
            GregorianCalendar gc = getNormalizedCalendar();
            monthLen = gc.getActualMaximum(DAY_OF_MONTH);
        }
        int dom = internalGet(DAY_OF_MONTH);
        if (dom > monthLen) {
            set(DAY_OF_MONTH, monthLen);
        }
    }

    /**
     * 返回此对象的固定日期值。时间值和日历字段必须同步。
     */
    private long getCurrentFixedDate() {
        return (calsys == gcal) ? cachedFixedDate : calsys.getFixedDate(cdate);
    }

    /**
     * 返回“roll”操作后的新值。
     */
    private static int getRolledValue(int value, int amount, int min, int max) {
        assert value >= min && value <= max;
        int range = max - min + 1;
        amount %= range;
        int n = value + amount;
        if (n > max) {
            n -= range;
        } else if (n < min) {
            n += range;
        }
        assert n >= min && n <= max;
        return n;
    }

    /**
     * 返回ERA。我们需要一个特殊的方法，因为默认ERA是CE，但未设置的ERA是BCE。
     */
    private int internalGetEra() {
        return isSet(ERA) ? internalGet(ERA) : CE;
    }

    /**
     * 更新内部状态。
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (gdate == null) {
            gdate = (BaseCalendar.Date) gcal.newCalendarDate(getZone());
            cachedFixedDate = Long.MIN_VALUE;
        }
        setGregorianChange(gregorianCutover);
    }

    /**
     * 将此对象转换为表示与该GregorianCalendar相同时间点的ZonedDateTime。
     * <p>
     * 由于此对象支持儒略-格里高利转换日期，而ZonedDateTime不支持，因此可能返回的年、月、日值会有所不同。结果将表示ISO日历系统中的正确日期，这也将是修改后的儒略日的相同值。
     *
     * @return 一个表示与该格里高利日历相同时间点的带时区日期时间
     * @since 1.8
     */
    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getTimeInMillis()),
                                       getTimeZone().toZoneId());
    }

    /**
     * 从ZonedDateTime对象获取默认区域设置的GregorianCalendar实例。
     * <p>
     * 由于ZonedDateTime不支持儒略-格里高利转换日期并使用ISO日历系统，返回的GregorianCalendar是一个纯格里高利日历，并使用ISO 8601标准定义周，其中{@code MONDAY}是{@link Calendar#getFirstDayOfWeek() 第一周的第一天}，{@code 4}是{@link Calendar#getMinimalDaysInFirstWeek() 第一周的最小天数}。
     * <p>
     * ZonedDateTime可以存储比GregorianCalendar更远的未来和更远的过去的时间点。在这种情况下，此方法将抛出一个{@code IllegalArgumentException}异常。
     *
     * @param zdt 要转换的带时区日期时间对象
     * @return 一个表示与提供的带时区日期时间相同时间点的格里高利日历
     * @exception NullPointerException 如果{@code zdt}为null
     * @exception IllegalArgumentException 如果带时区日期时间太大，无法表示为GregorianCalendar
     * @since 1.8
     */
    public static GregorianCalendar from(ZonedDateTime zdt) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zdt.getZone()));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.setFirstDayOfWeek(MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        try {
            cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(), 1000),
                                              zdt.get(ChronoField.MILLI_OF_SECOND)));
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
        return cal;
    }
}
