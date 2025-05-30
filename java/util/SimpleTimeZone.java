
/*
 * Copyright (c) 1996, 2017, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.Gregorian;

/**
 * <code>SimpleTimeZone</code> 是 <code>TimeZone</code> 的一个具体子类，
 * 用于表示与格里高利历相关的时区。
 * 该类包含一个与 GMT 的偏移量，称为 <em>原始偏移量</em>，以及夏令时的开始和结束规则。
 * 由于它只包含每个规则的单个值，因此无法处理 GMT 偏移量和夏令时安排的历史变化，
 * 但可以通过 {@link #setStartYear setStartYear} 方法指定夏令时安排开始生效的年份。
 * <p>
 * 要构造一个具有夏令时安排的 <code>SimpleTimeZone</code>，可以使用一组规则，
 * <em>开始规则</em> 和 <em>结束规则</em>。夏令时开始或结束的日期由 <em>月份</em>、
 * <em>月份中的日期</em> 和 <em>星期几</em> 的组合来指定。月份值由 Calendar {@link Calendar#MONTH MONTH} 字段值表示，
 * 例如 {@link Calendar#MARCH}。星期几值由 Calendar {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 字段值表示，
 * 例如 {@link Calendar#SUNDAY SUNDAY}。值组合的含义如下。
 *
 * <ul>
 * <li><b>确切的月份日期</b><br>
 * 要指定确切的月份日期，将 <em>月份</em> 和 <em>月份中的日期</em> 设置为确切值，将 <em>星期几</em> 设置为零。例如，
 * 要指定 3 月 1 日，将 <em>月份</em> 设置为 {@link Calendar#MARCH MARCH}，<em>月份中的日期</em> 设置为 1，
 * <em>星期几</em> 设置为 0。</li>
 *
 * <li><b>月份中的某一天或之后的星期几</b><br>
 * 要指定月份中的某一天或之后的星期几，将 <em>月份</em> 设置为确切的月份值，<em>月份中的日期</em> 设置为规则应用的日期，
 * <em>星期几</em> 设置为负的 {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 字段值。例如，要指定 4 月的第二个星期日，
 * 将 <em>月份</em> 设置为 {@link Calendar#APRIL APRIL}，<em>月份中的日期</em> 设置为 8，<em>星期几</em> 设置为 <code>-</code>{@link
 * Calendar#SUNDAY SUNDAY}。</li>
 *
 * <li><b>月份中的某一天或之前的星期几</b><br>
 * 要指定月份中的某一天或之前的星期几，将 <em>月份中的日期</em> 和 <em>星期几</em> 设置为负值。例如，要指定 3 月 21 日或之前的最后一个星期三，
 * 将 <em>月份</em> 设置为 {@link Calendar#MARCH MARCH}，<em>月份中的日期</em> 设置为 -21，<em>星期几</em> 设置为 <code>-</code>{@link Calendar#WEDNESDAY WEDNESDAY}。</li>
 *
 * <li><b>月份的最后一个星期几</b><br>
 * 要指定月份的最后一个星期几，将 <em>星期几</em> 设置为 {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 值，将 <em>月份中的日期</em> 设置为 -1。例如，
 * 要指定 10 月的最后一个星期日，将 <em>月份</em> 设置为 {@link Calendar#OCTOBER OCTOBER}，<em>星期几</em> 设置为 {@link
 * Calendar#SUNDAY SUNDAY}，<em>月份中的日期</em> 设置为 -1。</li>
 *
 * </ul>
 * 夏令时开始或结束的时间由一天中的毫秒值指定。有三种 <em>模式</em> 可以指定时间：{@link #WALL_TIME}、{@link
 * #STANDARD_TIME} 和 {@link #UTC_TIME}。例如，如果夏令时在标准时间 2:00 am 结束，可以在 {@link #WALL_TIME} 模式下指定 7200000
 * 毫秒。在这种情况下，<em>结束规则</em> 的墙钟时间与夏令时相同。
 * <p>
 * 以下是构造时区对象的参数示例。
 * <pre><code>
 *      // 基础 GMT 偏移量：-8:00
 *      // 夏令时开始：      在标准时间 2:00 am
 *      //                  在 4 月的第一个星期日
 *      // 夏令时结束：      在夏令时 2:00 am
 *      //                  在 10 月的最后一个星期日
 *      // 节约：            1 小时
 *      SimpleTimeZone(-28800000,
 *                     "America/Los_Angeles",
 *                     Calendar.APRIL, 1, -Calendar.SUNDAY,
 *                     7200000,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     7200000,
 *                     3600000)
 *
 *      // 基础 GMT 偏移量：+1:00
 *      // 夏令时开始：      在 UTC 时间 1:00 am
 *      //                  在 3 月的最后一个星期日
 *      // 夏令时结束：      在 UTC 时间 1:00 am
 *      //                  在 10 月的最后一个星期日
 *      // 节约：            1 小时
 *      SimpleTimeZone(3600000,
 *                     "Europe/Paris",
 *                     Calendar.MARCH, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     3600000)
 * </code></pre>
 * 这些参数规则也适用于设置规则的方法，如 <code>setStartRule</code>。
 *
 * @since 1.1
 * @see      Calendar
 * @see      GregorianCalendar
 * @see      TimeZone
 * @author   David Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 */

public class SimpleTimeZone extends TimeZone {
    /**
     * 构造一个没有夏令时安排的 SimpleTimeZone，给定基础 GMT 偏移量和时区 ID。
     *
     * @param rawOffset  与 GMT 的基础时区偏移量（以毫秒为单位）。
     * @param ID         分配给此实例的时区名称。
     */
    public SimpleTimeZone(int rawOffset, String ID)
    {
        this.rawOffset = rawOffset;
        setID (ID);
        dstSavings = millisPerHour; // 以防用户稍后设置规则
    }

    /**
     * 构造一个 SimpleTimeZone，给定基础 GMT 偏移量、时区 ID 和夏令时的开始和结束规则。
     * <code>startTime</code> 和 <code>endTime</code> 均假定为墙钟时间表示。假设夏令时的节约时间为 3600000 毫秒（即 1 小时）。
     * 此构造函数等效于：
     * <pre><code>
     *     SimpleTimeZone(rawOffset,
     *                    ID,
     *                    startMonth,
     *                    startDay,
     *                    startDayOfWeek,
     *                    startTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    endMonth,
     *                    endDay,
     *                    endDayOfWeek,
     *                    endTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    3600000)
     * </code></pre>
     *
     * @param rawOffset       与 GMT 的基础时区偏移量。
     * @param ID              分配给此对象的时区 ID。
     * @param startMonth      夏令时开始的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，0 表示 1 月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       夏令时开始的本地墙钟时间（在一天中的毫秒数），在这种情况下是本地标准时间。
     * @param endMonth        夏令时结束的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，9 表示 10 月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         夏令时结束的本地墙钟时间（在一天中的毫秒数），在这种情况下是本地夏令时。
     * @exception IllegalArgumentException 如果开始或结束规则中的月份、日期、星期几或时间参数超出范围
     */
    public SimpleTimeZone(int rawOffset, String ID,
                          int startMonth, int startDay, int startDayOfWeek, int startTime,
                          int endMonth, int endDay, int endDayOfWeek, int endTime)
    {
        this(rawOffset, ID,
             startMonth, startDay, startDayOfWeek, startTime, WALL_TIME,
             endMonth, endDay, endDayOfWeek, endTime, WALL_TIME,
             millisPerHour);
    }

    /**
     * 构造一个 SimpleTimeZone，给定基础 GMT 偏移量、时区 ID 和夏令时的开始和结束规则。
     * <code>startTime</code> 和 <code>endTime</code> 均假定为墙钟时间表示。此构造函数等效于：
     * <pre><code>
     *     SimpleTimeZone(rawOffset,
     *                    ID,
     *                    startMonth,
     *                    startDay,
     *                    startDayOfWeek,
     *                    startTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    endMonth,
     *                    endDay,
     *                    endDayOfWeek,
     *                    endTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    dstSavings)
     * </code></pre>
     *
     * @param rawOffset       与 GMT 的基础时区偏移量。
     * @param ID              分配给此对象的时区 ID。
     * @param startMonth      夏令时开始的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，0 表示 1 月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       夏令时开始的本地墙钟时间，这种情况下是本地标准时间。
     * @param endMonth        夏令时结束的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，9 表示 10 月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         夏令时结束的本地墙钟时间，这种情况下是本地夏令时。
     * @param dstSavings      夏令时期间节约的时间（以毫秒为单位）。
     * @exception IllegalArgumentException 如果开始或结束规则中的月份、日期、星期几或时间参数超出范围
     * @since 1.2
     */
    public SimpleTimeZone(int rawOffset, String ID,
                          int startMonth, int startDay, int startDayOfWeek, int startTime,
                          int endMonth, int endDay, int endDayOfWeek, int endTime,
                          int dstSavings)
    {
        this(rawOffset, ID,
             startMonth, startDay, startDayOfWeek, startTime, WALL_TIME,
             endMonth, endDay, endDayOfWeek, endTime, WALL_TIME,
             dstSavings);
    }

    /**
     * 构造一个 SimpleTimeZone，给定基础 GMT 偏移量、时区 ID 和夏令时的开始和结束规则。
     * 此构造函数接受完整的开始和结束规则参数集，包括 <code>startTime</code> 和
     * <code>endTime</code> 的模式。模式指定 {@link #WALL_TIME 墙钟时间} 或 {@link #STANDARD_TIME 标准时间} 或 {@link #UTC_TIME UTC 时间}。
     *
     * @param rawOffset       与 GMT 的基础时区偏移量。
     * @param ID              分配给此对象的时区 ID。
     * @param startMonth      夏令时开始的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，0 表示 1 月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       夏令时开始的时间，由 <code>startTimeMode</code> 指定的时间模式表示。
     * @param startTimeMode   由 startTime 指定的开始时间的模式。
     * @param endMonth        夏令时结束的月份。月份是 {@link Calendar#MONTH MONTH} 字段值（0 基础。例如，9 表示 10 月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         夏令时结束的时间，由 <code>endTimeMode</code> 指定的时间模式表示。
     * @param endTimeMode     由 endTime 指定的结束时间的模式
     * @param dstSavings      夏令时期间节约的时间（以毫秒为单位）。
     *
     * @exception IllegalArgumentException 如果开始或结束规则中的月份、日期、星期几、时间模式或时间参数超出范围，或者时间模式值无效。
     *
     * @see #WALL_TIME
     * @see #STANDARD_TIME
     * @see #UTC_TIME
     *
     * @since 1.4
     */
    public SimpleTimeZone(int rawOffset, String ID,
                          int startMonth, int startDay, int startDayOfWeek,
                          int startTime, int startTimeMode,
                          int endMonth, int endDay, int endDayOfWeek,
                          int endTime, int endTimeMode,
                          int dstSavings) {


                    setID(ID);
        this.rawOffset      = rawOffset;
        this.startMonth     = startMonth;
        this.startDay       = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime      = startTime;
        this.startTimeMode  = startTimeMode;
        this.endMonth       = endMonth;
        this.endDay         = endDay;
        this.endDayOfWeek   = endDayOfWeek;
        this.endTime        = endTime;
        this.endTimeMode    = endTimeMode;
        this.dstSavings     = dstSavings;

        // this.useDaylight is set by decodeRules
        decodeRules();
        if (dstSavings <= 0) {
            throw new IllegalArgumentException("非法的夏令时值: " + dstSavings);
        }
    }

    /**
     * 设置夏令时开始年份。
     *
     * @param year  夏令时开始年份。
     */
    public void setStartYear(int year)
    {
        startYear = year;
        invalidateCache();
    }

    /**
     * 设置夏令时开始规则。例如，如果夏令时在4月的第一个周日2点开始，可以通过调用以下方法设置开始规则：
     * <pre><code>setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2*60*60*1000);</code></pre>
     *
     * @param startMonth      夏令时开始的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，0表示1月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param startTime       夏令时开始的本地标准时间。
     * @exception IllegalArgumentException 如果 <code>startMonth</code>、<code>startDay</code>、
     * <code>startDayOfWeek</code> 或 <code>startTime</code> 参数超出范围
     */
    public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime)
    {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime = startTime;
        startTimeMode = WALL_TIME;
        decodeStartRule();
        invalidateCache();
    }

    /**
     * 将夏令时开始规则设置为月份内的固定日期。此方法等同于：
     * <pre><code>setStartRule(startMonth, startDay, 0, startTime)</code></pre>
     *
     * @param startMonth      夏令时开始的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，0表示1月）。
     * @param startDay        夏令时开始的月份中的日期。
     * @param startTime       夏令时开始的本地标准时间。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @exception IllegalArgumentException 如果 <code>startMonth</code>、
     * <code>startDayOfMonth</code> 或 <code>startTime</code> 参数超出范围
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startTime) {
        setStartRule(startMonth, startDay, 0, startTime);
    }

    /**
     * 将夏令时开始规则设置为月份内的特定日期前后的某一天，例如，8号或之后的第一个周一。
     *
     * @param startMonth      夏令时开始的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，0表示1月）。
     * @param startDay        夏令时开始的月份中的日期。
     * @param startDayOfWeek  夏令时开始的星期几。
     * @param startTime       夏令时开始的本地标准时间。
     * @param after           如果为 true，此规则选择 <code>dayOfMonth</code> 或之后的第一个 <code>dayOfWeek</code>。
     *                        如果为 false，此规则选择 <code>dayOfMonth</code> 或之前的最后一个 <code>dayOfWeek</code>。
     * @exception IllegalArgumentException 如果 <code>startMonth</code>、<code>startDay</code>、
     * <code>startDayOfWeek</code> 或 <code>startTime</code> 参数超出范围
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startDayOfWeek,
                             int startTime, boolean after)
    {
        // TODO: 此方法不检查 dayOfMonth 或 dayOfWeek 的初始值。
        if (after) {
            setStartRule(startMonth, startDay, -startDayOfWeek, startTime);
        } else {
            setStartRule(startMonth, -startDay, -startDayOfWeek, startTime);
        }
    }

    /**
     * 设置夏令时结束规则。例如，如果夏令时在10月的最后一个周日2点结束，可以通过调用以下方法设置结束规则：
     * <code>setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2*60*60*1000);</code>
     *
     * @param endMonth        夏令时结束的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，9表示10月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param endTime         夏令时结束的本地标准时间，
     *                        （以毫秒为单位）此时为本地夏令时。
     * @exception IllegalArgumentException 如果 <code>endMonth</code>、<code>endDay</code>、
     * <code>endDayOfWeek</code> 或 <code>endTime</code> 参数超出范围
     */
    public void setEndRule(int endMonth, int endDay, int endDayOfWeek,
                           int endTime)
    {
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
        this.endTimeMode = WALL_TIME;
        decodeEndRule();
        invalidateCache();
    }

    /**
     * 将夏令时结束规则设置为月份内的固定日期。此方法等同于：
     * <pre><code>setEndRule(endMonth, endDay, 0, endTime)</code></pre>
     *
     * @param endMonth        夏令时结束的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，9表示10月）。
     * @param endDay          夏令时结束的月份中的日期。
     * @param endTime         夏令时结束的本地标准时间，
     *                        （以毫秒为单位）此时为本地夏令时。
     * @exception IllegalArgumentException 如果 <code>endMonth</code>、<code>endDay</code>、
     * 或 <code>endTime</code> 参数超出范围
     * @since 1.2
     */
    public void setEndRule(int endMonth, int endDay, int endTime)
    {
        setEndRule(endMonth, endDay, 0, endTime);
    }

    /**
     * 将夏令时结束规则设置为月份内的特定日期前后的某一天，例如，8号或之后的第一个周一。
     *
     * @param endMonth        夏令时结束的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如，9表示10月）。
     * @param endDay          夏令时结束的月份中的日期。
     * @param endDayOfWeek    夏令时结束的星期几。
     * @param endTime         夏令时结束的本地标准时间，
     *                        （以毫秒为单位）此时为本地夏令时。
     * @param after           如果为 true，此规则选择 <code>endDay</code> 或之后的第一个 <code>endDayOfWeek</code>。
     *                        如果为 false，此规则选择 <code>endDay</code> 或之前的最后一个 <code>endDayOfWeek</code>。
     * @exception IllegalArgumentException 如果 <code>endMonth</code>、<code>endDay</code>、
     * <code>endDayOfWeek</code> 或 <code>endTime</code> 参数超出范围
     * @since 1.2
     */
    public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime, boolean after)
    {
        if (after) {
            setEndRule(endMonth, endDay, -endDayOfWeek, endTime);
        } else {
            setEndRule(endMonth, -endDay, -endDayOfWeek, endTime);
        }
    }

    /**
     * 返回给定时间此时区与 UTC 的偏移量。如果给定时间处于夏令时，偏移量会调整夏令时的偏移量。
     *
     * @param date 给定时间，用于查找时区偏移量
     * @return 需要添加到 UTC 以获取本地时间的时间量（以毫秒为单位）。
     * @since 1.4
     */
    public int getOffset(long date) {
        return getOffsets(date, null);
    }

    /**
     * @see TimeZone#getOffsets
     */
    int getOffsets(long date, int[] offsets) {
        int offset = rawOffset;

      computeOffset:
        if (useDaylight) {
            synchronized (this) {
                if (cacheStart != 0) {
                    if (date >= cacheStart && date < cacheEnd) {
                        offset += dstSavings;
                        break computeOffset;
                    }
                }
            }
            BaseCalendar cal = date >= GregorianCalendar.DEFAULT_GREGORIAN_CUTOVER ?
                gcal : (BaseCalendar) CalendarSystem.forName("julian");
            BaseCalendar.Date cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
            // 获取本地时间的年份
            cal.getCalendarDate(date + rawOffset, cdate);
            int year = cdate.getNormalizedYear();
            if (year >= startYear) {
                // 为转换计算清除时间元素
                cdate.setTimeOfDay(0, 0, 0, 0);
                offset = getOffset(cal, cdate, year, date);
            }
        }

        if (offsets != null) {
            offsets[0] = rawOffset;
            offsets[1] = offset - rawOffset;
        }
        return offset;
    }

   /**
     * 返回本地时间与 UTC 之间的差值（以毫秒为单位），考虑了原始偏移量和夏令时的影响，适用于指定的日期和时间。
     * 此方法假设开始和结束月份不同。它还使用默认的 {@link GregorianCalendar} 对象作为底层日历，
     * 例如，用于确定闰年。不要将此方法的结果用于非默认的 <code>GregorianCalendar</code>。
     *
     * <p><em>注意：通常，客户端应使用
     * <code>Calendar.get(ZONE_OFFSET) + Calendar.get(DST_OFFSET)</code>
     * 而不是调用此方法。</em>
     *
     * @param era       给定日期的纪元。
     * @param year      给定日期的年份。
     * @param month     给定日期的月份。月份从0开始，例如，0表示1月。
     * @param day       给定日期的月份中的日期。
     * @param dayOfWeek 给定日期的星期几。
     * @param millis    <em>标准</em>本地时间中的毫秒数。
     * @return          需要添加到 UTC 以获取本地时间的毫秒数。
     * @exception       IllegalArgumentException 如果 <code>era</code>、
     *                  <code>month</code>、<code>day</code>、<code>dayOfWeek</code>、
     *                  或 <code>millis</code> 参数超出范围
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
                         int millis)
    {
        if (era != GregorianCalendar.AD && era != GregorianCalendar.BC) {
            throw new IllegalArgumentException("非法的纪元 " + era);
        }

        int y = year;
        if (era == GregorianCalendar.BC) {
            // 调整 y 以适应 GregorianCalendar 风格的年份编号。
            y = 1 - y;
        }

        // 如果年份不能用64位长整数表示，将其转换为等效年份。这是为了通过一些 JCK 测试用例，
        // 尽管指定的年份实际上无法被 Java 时间系统支持。
        if (y >= 292278994) {
            y = 2800 + y % 2800;
        } else if (y <= -292269054) {
            // y %= 28 也会产生等效年份，但使用正年份编号更方便使用 UNIX cal 命令。
            y = (int) CalendarUtils.mod((long) y, 28);
        }

        // 将年份转换为其1基月份值
        int m = month + 1;

        // 首先，将时间计算为格里高利历日期。
        BaseCalendar cal = gcal;
        BaseCalendar.Date cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cdate.setDate(y, m, day);
        long time = cal.getTime(cdate); // 规范化 cdate
        time += millis - rawOffset; // UTC 时间

        // 如果时间值表示的时间早于默认的格里高利历转换时间，使用儒略历系统重新计算时间。
        // 对于儒略历系统，规范化年份编号为 ..., -2 (BCE 2), -1 (BCE 1), 1, 2 ...，
        // 与 GregorianCalendar 风格的年份编号 (..., -1, 0 (BCE 1), 1, 2, ...) 不同。
        if (time < GregorianCalendar.DEFAULT_GREGORIAN_CUTOVER) {
            cal = (BaseCalendar) CalendarSystem.forName("julian");
            cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
            cdate.setNormalizedDate(y, m, day);
            time = cal.getTime(cdate) + millis - rawOffset;
        }

        if ((cdate.getNormalizedYear() != y)
            || (cdate.getMonth() != m)
            || (cdate.getDayOfMonth() != day)
            // 验证应该是 cdate.getDayOfWeek() == dayOfWeek。然而，为了兼容性，我们不检查 dayOfWeek。
            || (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY)
            || (millis < 0 || millis >= (24*60*60*1000))) {
            throw new IllegalArgumentException();
        }


    /**
     * 获取此时区的GMT偏移量。
     * @return GMT偏移值，以毫秒为单位
     * @see #setRawOffset
     */
    public int getRawOffset()
    {
        // 如果我们有历史时区数据，给定的日期将被考虑在内。
        return rawOffset;
    }

    /**
     * 设置基础时区偏移量到GMT。
     * 这是从UTC添加的偏移量以获取本地时间。
     * @see #getRawOffset
     */
    public void setRawOffset(int offsetMillis)
    {
        this.rawOffset = offsetMillis;
    }

    /**
     * 设置在夏令时期间时钟向前调整的时间量。
     * @param millisSavedDuringDST 与标准时间相比，夏令时规则生效时时间向前调整的毫秒数。通常为一小时（3600000）。
     * @see #getDSTSavings
     * @since 1.2
     */
    public void setDSTSavings(int millisSavedDuringDST) {
        if (millisSavedDuringDST <= 0) {
            throw new IllegalArgumentException("非法夏令时值: "
                                               + millisSavedDuringDST);
        }
        dstSavings = millisSavedDuringDST;
    }

    /**
     * 返回在夏令时期间时钟向前调整的时间量。
     *
     * @return 与标准时间相比，夏令时规则生效时时间向前调整的毫秒数，或0（如果此时区不使用夏令时）。
     *
     * @see #setDSTSavings
     * @since 1.2
     */
    public int getDSTSavings() {
        return useDaylight ? dstSavings : 0;
    }

    /**
     * 查询此时间区是否使用夏令时。
     * @return 如果此时间区使用夏令时，则返回true；否则返回false。
     */
    public boolean useDaylightTime()
    {
        return useDaylight;
    }

    /**
     * 如果此 {@code SimpleTimeZone} 观察夏令时，则返回 {@code true}。此方法等同于 {@link
     * #useDaylightTime()}。
     *
     * @return 如果此 {@code SimpleTimeZone} 观察夏令时，则返回 {@code true}；否则返回 {@code false}。
     * @since 1.7
     */
    @Override
    public boolean observesDaylightTime() {
        return useDaylightTime();
    }

    /**
     * 查询给定日期是否处于夏令时。
     * @return 如果给定日期处于夏令时，则返回true；否则返回false。
     */
    public boolean inDaylightTime(Date date)
    {
        return (getOffset(date.getTime()) != rawOffset);
    }

    /**
     * 返回此 <code>SimpleTimeZone</code> 实例的克隆。
     * @return 此实例的克隆。
     */
    public Object clone()
    {
        return super.clone();
    }

    /**
     * 生成 SimpleDateFormat 对象的哈希码。
     * @return 此对象的哈希码
     */
    public synchronized int hashCode()
    {
        return startMonth ^ startDay ^ startDayOfWeek ^ startTime ^
            endMonth ^ endDay ^ endDayOfWeek ^ endTime ^ rawOffset;
    }

    /**
     * 比较两个 <code>SimpleTimeZone</code> 对象的相等性。
     *
     * @param obj  要比较的 <code>SimpleTimeZone</code> 对象。
     * @return     如果给定的 <code>obj</code> 与此 <code>SimpleTimeZone</code> 对象相同，则返回true；否则返回false。
     */
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleTimeZone)) {
            return false;
        }

        SimpleTimeZone that = (SimpleTimeZone) obj;

        return getID().equals(that.getID()) &&
            hasSameRules(that);
    }

    /**
     * 如果此时区具有与另一个时区相同的规则和偏移量，则返回 <code>true</code>。
     * @param other 要比较的 TimeZone 对象
     * @return 如果给定的时区是 SimpleTimeZone 并且具有与此时区相同的规则和偏移量，则返回 <code>true</code>
     * @since 1.2
     */
    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone that = (SimpleTimeZone) other;
        return rawOffset == that.rawOffset &&
            useDaylight == that.useDaylight &&
            (!useDaylight
             // 仅在使用夏令时时检查规则
             || (dstSavings == that.dstSavings &&
                 startMode == that.startMode &&
                 startMonth == that.startMonth &&
                 startDay == that.startDay &&
                 startDayOfWeek == that.startDayOfWeek &&
                 startTime == that.startTime &&
                 startTimeMode == that.startTimeMode &&
                 endMode == that.endMode &&
                 endMonth == that.endMonth &&
                 endDay == that.endDay &&
                 endDayOfWeek == that.endDayOfWeek &&
                 endTime == that.endTime &&
                 endTimeMode == that.endTimeMode &&
                 startYear == that.startYear));
    }

    /**
     * 返回此时间区的字符串表示形式。
     * @return 此时间区的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() +
            "[id=" + getID() +
            ",offset=" + rawOffset +
            ",dstSavings=" + dstSavings +
            ",useDaylight=" + useDaylight +
            ",startYear=" + startYear +
            ",startMode=" + startMode +
            ",startMonth=" + startMonth +
            ",startDay=" + startDay +
            ",startDayOfWeek=" + startDayOfWeek +
            ",startTime=" + startTime +
            ",startTimeMode=" + startTimeMode +
            ",endMode=" + endMode +
            ",endMonth=" + endMonth +
            ",endDay=" + endDay +
            ",endDayOfWeek=" + endDayOfWeek +
            ",endTime=" + endTime +
            ",endTimeMode=" + endTimeMode + ']';
    }

    // =======================privates===============================

    /**
     * 夏令时开始的月份。此值必须在 <code>Calendar.JANUARY</code> 和
     * <code>Calendar.DECEMBER</code> 之间（包括）。此值不能等于 <code>endMonth</code>。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startMonth;

    /**
     * 此字段有两种可能的解释：
     * <dl>
     * <dt><code>startMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> 表示 <code>startMonth</code> 月的哪一天开始夏令时，从1到28、30或31，具体取决于 <code>startMonth</code>。
     * </dd>
     * <dt><code>startMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> 表示 <code>startMonth</code> 月的第几个 <code>startDayOfWeek</code> 开始夏令时。例如，值为 +1 和 <code>startDayOfWeek</code> 为 <code>Calendar.SUNDAY</code> 表示 <code>startMonth</code> 月的第一个星期日。同样，+2 表示第二个星期日，-1 表示最后一个星期日。值为 0 是非法的。
     * </dd>
     * </dl>
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startDay;

    /**
     * 夏令时开始的星期几。此值必须在 <code>Calendar.SUNDAY</code> 和
     * <code>Calendar.SATURDAY</code> 之间（包括）。
     * <p>如果 <code>useDaylight</code> 为 false 或 <code>startMode == DAY_OF_MONTH</code>，则忽略此值。
     * @serial
     */
    private int startDayOfWeek;

    /**
     * 夏令时开始的时间，以午夜后的毫秒数表示。此值根据 <code>startTimeMode</code> 的设置表示为标准时间、夏令时或UTC时间。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startTime;

    /**
     * <code>startTime</code> 的格式，可以是 WALL_TIME、STANDARD_TIME 或 UTC_TIME。
     * @serial
     * @since 1.3
     */
    private int startTimeMode;

    /**
     * 夏令时结束的月份。此值必须在 <code>Calendar.JANUARY</code> 和
     * <code>Calendar.UNDECIMBER</code> 之间。此值不能等于 <code>startMonth</code>。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int endMonth;

    /**
     * 此字段有两种可能的解释：
     * <dl>
     * <dt><code>endMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> 表示 <code>endMonth</code> 月的哪一天结束夏令时，从1到28、30或31，具体取决于 <code>endMonth</code>。
     * </dd>
     * <dt><code>endMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> 表示 <code>endMonth</code> 月的第几个 <code>endDayOfWeek</code> 结束夏令时。例如，值为 +1 和 <code>endDayOfWeek</code> 为 <code>Calendar.SUNDAY</code> 表示 <code>endMonth</code> 月的第一个星期日。同样，+2 表示第二个星期日，-1 表示最后一个星期日。值为 0 是非法的。
     * </dd>
     * </dl>
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int endDay;

    /**
     * 夏令时结束的星期几。此值必须在 <code>Calendar.SUNDAY</code> 和
     * <code>Calendar.SATURDAY</code> 之间（包括）。
     * <p>如果 <code>useDaylight</code> 为 false 或 <code>endMode == DAY_OF_MONTH</code>，则忽略此值。
     * @serial
     */
    private int endDayOfWeek;

    /**
     * 夏令时结束的时间，以午夜后的毫秒数表示。此值根据 <code>endTimeMode</code> 的设置表示为标准时间、夏令时或UTC时间。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int endTime;

    /**
     * <code>endTime</code> 的格式，可以是 <code>WALL_TIME</code>、
     * <code>STANDARD_TIME</code> 或 <code>UTC_TIME</code>。
     * @serial
     * @since 1.3
     */
    private int endTimeMode;

    /**
     * 夏令时首次被观察到的年份。这是一个 {@link GregorianCalendar#AD AD}
     * 值。如果此值小于1，则夏令时在所有 <code>AD</code> 年份都被观察到。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startYear;

    /**
     * 此时区与GMT之间的偏移量，以毫秒为单位。负偏移量表示在格林尼治以西。为了获得本地 <em>标准</em> 时间，将偏移量加到GMT时间上。为了获得本地夏令时，可能还需要加上 <code>dstSavings</code>。
     * @serial
     */
    private int rawOffset;

    /**
     * 一个布尔值，如果此区域使用夏令时，则为 true。如果此值为 false，则忽略其他几个字段。
     * @serial
     */
    private boolean useDaylight=false; // 表示此时间区是否使用夏令时


                private static final int millisPerHour = 60*60*1000;
    private static final int millisPerDay  = 24*millisPerHour;

    /**
     * 该字段在JDK 1.1中进行了序列化，因此我们必须保持这种方式以维持序列化兼容性。然而，每次创建新的时区时，没有必要重新创建数组。
     * @serial 包含值{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}的字节数组。自Java 2平台v1.2起被忽略，但为了与JDK 1.1的兼容性，必须进行流处理。
     */
    private final byte monthLength[] = staticMonthLength;
    private final static byte staticMonthLength[] = {31,28,31,30,31,30,31,31,30,31,30,31};
    private final static byte staticLeapMonthLength[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    /**
     * 指定开始规则模式的变量。取以下值：
     * <dl>
     * <dt><code>DOM_MODE</code></dt>
     * <dd>
     * 确切的星期几；例如，3月1日。
     * </dd>
     * <dt><code>DOW_IN_MONTH_MODE</code></dt>
     * <dd>
     * 月份中的星期几；例如，3月的最后一个星期日。
     * </dd>
     * <dt><code>DOW_GE_DOM_MODE</code></dt>
     * <dd>
     * 月份后的星期几；例如，3月15日或之后的星期日。
     * </dd>
     * <dt><code>DOW_LE_DOM_MODE</code></dt>
     * <dd>
     * 月份前的星期几；例如，3月15日或之前的星期日。
     * </dd>
     * </dl>
     * 该字段的设置影响<code>startDay</code>字段的解释。
     * <p>如果<code>useDaylight</code>为false，则此值被忽略。
     * @serial
     * @since 1.1.4
     */
    private int startMode;

    /**
     * 指定结束规则模式的变量。取以下值：
     * <dl>
     * <dt><code>DOM_MODE</code></dt>
     * <dd>
     * 确切的星期几；例如，3月1日。
     * </dd>
     * <dt><code>DOW_IN_MONTH_MODE</code></dt>
     * <dd>
     * 月份中的星期几；例如，3月的最后一个星期日。
     * </dd>
     * <dt><code>DOW_GE_DOM_MODE</code></dt>
     * <dd>
     * 月份后的星期几；例如，3月15日或之后的星期日。
     * </dd>
     * <dt><code>DOW_LE_DOM_MODE</code></dt>
     * <dd>
     * 月份前的星期几；例如，3月15日或之前的星期日。
     * </dd>
     * </dl>
     * 该字段的设置影响<code>endDay</code>字段的解释。
     * <p>如果<code>useDaylight</code>为false，则此值被忽略。
     * @serial
     * @since 1.1.4
     */
    private int endMode;

    /**
     * 表示夏令时节省的时间量的正值（以毫秒为单位）。
     * 通常为一小时（3600000）；有时为30分钟（1800000）。
     * <p>如果<code>useDaylight</code>为false，则此值被忽略。
     * @serial
     * @since 1.1.4
     */
    private int dstSavings;

    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();

    /**
     * 缓存表示单个夏令时周期的值。当缓存值有效时，cacheStart是夏令时的开始时间（包含），cacheEnd是结束时间（不包含）。
     *
     * 如果cacheStart和cacheEnd在同一年内，cacheYear将具有年份值。如果cacheStart和cacheEnd不在同一年内，cacheYear将设置为startYear - 1。如果缓存值无效，cacheStart为0。cacheYear是long类型，以支持Integer.MIN_VALUE - 1（JCK要求）。
     */
    private transient long cacheYear;
    private transient long cacheStart;
    private transient long cacheEnd;

    /**
     * 指定startMode和endMode值的常量。
     */
    private static final int DOM_MODE          = 1; // 确切的月份日期，"Mar 1"
    private static final int DOW_IN_MONTH_MODE = 2; // 月份中的星期几，"lastSun"
    private static final int DOW_GE_DOM_MODE   = 3; // 月份后的星期几，"Sun>=15"
    private static final int DOW_LE_DOM_MODE   = 4; // 月份前的星期几，"Sun<=21"

    /**
     * 以标准时间指定的开始或结束时间模式的常量。标准时间是开始规则的标准时间，结束规则的夏令时。
     * @since 1.4
     */
    public static final int WALL_TIME = 0; // 0用于向后兼容

    /**
     * 以标准时间指定的开始或结束时间模式的常量。
     * @since 1.4
     */
    public static final int STANDARD_TIME = 1;

    /**
     * 以UTC指定的开始或结束时间模式的常量。例如，欧盟规则以UTC时间指定。
     * @since 1.4
     */
    public static final int UTC_TIME = 2;

    // 声明与1.1的兼容性
    static final long serialVersionUID = -403250971215465050L;

    // 内部序列化版本，表示写入的版本
    // - 0（默认）表示JDK 1.1.3及之前的版本
    // - 1表示JDK 1.1.4及之后的版本，包括3个新字段
    // - 2表示JDK 1.3及之后的版本，包括2个新字段
    static final int currentSerialVersion = 2;

    /**
     * 流中的序列化数据版本。可能的值：
     * <dl>
     * <dt><b>0</b>或不在流中</dt>
     * <dd>
     * JDK 1.1.3或更早。
     * </dd>
     * <dt><b>1</b></dt>
     * <dd>
     * JDK 1.1.4或之后。包括三个新字段：<code>startMode</code>，<code>endMode</code>，和<code>dstSavings</code>。
     * </dd>
     * <dt><b>2</b></dt>
     * <dd>
     * JDK 1.3或之后。包括两个新字段：<code>startTimeMode</code>和<code>endTimeMode</code>。
     * </dd>
     * </dl>
     * 当流输出此类时，将写入最新的格式和最高的允许<code>serialVersionOnStream</code>。
     * @serial
     * @since 1.1.4
     */
    private int serialVersionOnStream = currentSerialVersion;

    // 规则的最大数量。
    private static final int MAX_RULE_NUM = 6;

    synchronized private void invalidateCache() {
        cacheYear = startYear - 1;
        cacheStart = cacheEnd = 0;
    }

    //----------------------------------------------------------------------
    // 规则表示
    //
    // 我们表示以下几种规则：
    //       5        月份的第五天
    //       lastSun  月份的最后一个星期日
    //       lastMon  月份的最后一个星期一
    //       Sun>=8   第一个星期日在8日或之后
    //       Sun<=25  最后一个星期日在25日或之前
    // 这进一步复杂化了我们需要与1.1 FCS保持向后兼容的事实。最后，我们需要最小化API更改。为了满足这些要求，我们支持三种表示系统，并在它们之间进行转换。
    //
    // 内部表示
    // 这是SimpleTimeZone对象在构造或流输入完成后采用的格式。规则直接表示，使用未编码的格式。下面仅讨论开始规则；结束规则类似。
    //   startMode      取枚举值DAY_OF_MONTH，DOW_IN_MONTH，DOW_AFTER_DOM，或DOW_BEFORE_DOM。
    //   startDay       月份的日期，或对于DOW_IN_MONTH模式，表示哪个DOW的值，如+1表示第一个，+2表示第二个，-1表示最后一个等。
    //   startDayOfWeek 星期几。DAY_OF_MONTH模式下忽略。
    //
    // 编码表示
    // 这是由构造函数和setStartRule()、setEndRule()接受的格式。它使用正数、负数和零的各种组合来编码不同的规则。这种表示允许我们在不改变API的情况下指定所有不同的规则。
    //   MODE              startMonth    startDay    startDayOfWeek
    //   DOW_IN_MONTH_MODE >=0           !=0         >0
    //   DOM_MODE          >=0           >0          ==0
    //   DOW_GE_DOM_MODE   >=0           >0          <0
    //   DOW_LE_DOM_MODE   >=0           <0          <0
    //   （无夏令时）       不关心        ==0         不关心
    //
    // 流表示
    // 我们必须与1.1 FCS保持二进制兼容。1.1代码只能处理DOW_IN_MONTH_MODE和非夏令时模式，后者由useDaylight标志指示。当我们流输出对象时，我们将转换为近似的DOW_IN_MONTH_MODE表示，以便1.1代码可以解析和使用。之后，我们将单独写入完整的表示，以便当代代码可以识别和解析。完整的表示以“打包”格式写入，包括版本号、长度和字节数组。未来版本的此类可以指定不同的版本。如果它们希望包含额外的数据，应该将它们存储在下面的打包表示之后。
    //----------------------------------------------------------------------

    /**
     * 给定startDay和startDayOfMonth中的编码规则，解码它们并适当设置startMode。对endDay和endDayOfMonth执行相同操作。进入时，星期几变量可能是零或负数，以指示特殊模式。月份日期变量也可能是负数。退出时，模式变量将被设置，星期几和月份日期变量将为正数。此方法还识别startDay或endDay为零表示没有夏令时。
     */
    private void decodeRules()
    {
        decodeStartRule();
        decodeEndRule();
    }

    /**
     * 解码开始规则并验证参数。参数预计为编码形式，通过否定或置零某些值来表示各种规则模式。表示格式为：
     * <p>
     * <pre>
     *            DOW_IN_MONTH  DOM    DOW>=DOM  DOW<=DOM  无夏令时
     *            ------------  -----  --------  --------  ----------
     * month       0..11        相同    相同      相同     不关心
     * day        -5..5         1..31   1..31    -1..-31   0
     * dayOfWeek   1..7         0      -1..-7    -1..-7    不关心
     * time        0..ONEDAY    相同    相同      相同     不关心
     * </pre>
     * 月份的范围不包括UNDECIMBER，因为此类实际上是特定于GregorianCalendar的，后者不使用该月份。时间的范围包括ONEDAY（而不是以ONEDAY-1结束），因为结束规则是一个排他性的极限点。也就是说，处于夏令时的时间范围包括那些>=开始时间和<结束时间的。因此，应该可以指定一个ONEDAY的结束，以包括整个一天。虽然这等同于次日的时间0，但在12月31日等情况下，这并不总是可能的。尽管可以说开始范围应该是0..ONEDAY-1，但为了保持一致性，我们保持开始和结束范围相同。
     */
    private void decodeStartRule() {
        useDaylight = (startDay != 0) && (endDay != 0);
        if (startDay != 0) {
            if (startMonth < Calendar.JANUARY || startMonth > Calendar.DECEMBER) {
                throw new IllegalArgumentException(
                        "非法开始月份 " + startMonth);
            }
            if (startTime < 0 || startTime > millisPerDay) {
                throw new IllegalArgumentException(
                        "非法开始时间 " + startTime);
            }
            if (startDayOfWeek == 0) {
                startMode = DOM_MODE;
            } else {
                if (startDayOfWeek > 0) {
                    startMode = DOW_IN_MONTH_MODE;
                } else {
                    startDayOfWeek = -startDayOfWeek;
                    if (startDay > 0) {
                        startMode = DOW_GE_DOM_MODE;
                    } else {
                        startDay = -startDay;
                        startMode = DOW_LE_DOM_MODE;
                    }
                }
                if (startDayOfWeek > Calendar.SATURDAY) {
                    throw new IllegalArgumentException(
                           "非法开始星期几 " + startDayOfWeek);
                }
            }
            if (startMode == DOW_IN_MONTH_MODE) {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException(
                            "非法开始月份中的星期几 " + startDay);
                }
            } else if (startDay < 1 || startDay > staticMonthLength[startMonth]) {
                throw new IllegalArgumentException(
                        "非法开始日期 " + startDay);
            }
        }
    }

    /**
     * 解码结束规则并验证参数。此方法与decodeStartRule()完全类似。
     * @see decodeStartRule
     */
    private void decodeEndRule() {
        useDaylight = (startDay != 0) && (endDay != 0);
        if (endDay != 0) {
            if (endMonth < Calendar.JANUARY || endMonth > Calendar.DECEMBER) {
                throw new IllegalArgumentException(
                        "非法结束月份 " + endMonth);
            }
            if (endTime < 0 || endTime > millisPerDay) {
                throw new IllegalArgumentException(
                        "非法结束时间 " + endTime);
            }
            if (endDayOfWeek == 0) {
                endMode = DOM_MODE;
            } else {
                if (endDayOfWeek > 0) {
                    endMode = DOW_IN_MONTH_MODE;
                } else {
                    endDayOfWeek = -endDayOfWeek;
                    if (endDay > 0) {
                        endMode = DOW_GE_DOM_MODE;
                    } else {
                        endDay = -endDay;
                        endMode = DOW_LE_DOM_MODE;
                    }
                }
                if (endDayOfWeek > Calendar.SATURDAY) {
                    throw new IllegalArgumentException(
                           "非法结束星期几 " + endDayOfWeek);
                }
            }
            if (endMode == DOW_IN_MONTH_MODE) {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException(
                            "非法结束月份中的星期几 " + endDay);
                }
            } else if (endDay < 1 || endDay > staticMonthLength[endMonth]) {
                throw new IllegalArgumentException(
                        "非法结束日期 " + endDay);
            }
        }
    }

    /**
     * 使规则与1.1 FCS代码兼容。由于1.1 FCS代码仅理解月份中的星期几规则，我们必须将其他模式的规则修改为其在1.1 FCS中的近似等效。此方法在流输出此类对象时使用。调用后，规则将被修改，可能会丢失信息。startMode和endMode不会被更改，即使从语义上它们应该被设置为DOW_IN_MONTH_MODE，因为规则修改仅旨在临时使用。
     */
    private void makeRulesCompatible()
    {
        switch (startMode) {
        case DOM_MODE:
            startDay = 1 + (startDay / 7);
            startDayOfWeek = Calendar.SUNDAY;
            break;


                    case DOW_GE_DOM_MODE:
            // 一个月的第一天等同于 DOW_IN_MONTH_MODE
            // 也就是说，Sun>=1 == firstSun。
            if (startDay != 1) {
                startDay = 1 + (startDay / 7);
            }
            break;

        case DOW_LE_DOM_MODE:
            if (startDay >= 30) {
                startDay = -1;
            } else {
                startDay = 1 + (startDay / 7);
            }
            break;
        }

        switch (endMode) {
        case DOM_MODE:
            endDay = 1 + (endDay / 7);
            endDayOfWeek = Calendar.SUNDAY;
            break;

        case DOW_GE_DOM_MODE:
            // 一个月的第一天等同于 DOW_IN_MONTH_MODE
            // 也就是说，Sun>=1 == firstSun。
            if (endDay != 1) {
                endDay = 1 + (endDay / 7);
            }
            break;

        case DOW_LE_DOM_MODE:
            if (endDay >= 30) {
                endDay = -1;
            } else {
                endDay = 1 + (endDay / 7);
            }
            break;
        }

        /*
         * 调整开始和结束时间到标准时间。这在大多数情况下都能完美工作，
         * 除非它推到了下一天或前一天。如果发生这种情况，我们将尝试粗略地调整日期规则。
         * 日期规则已经被强制转换为 DOW_IN_MONTH 模式，因此我们更改星期几以向前或向后移动一天。
         * 虽然可以在调整日期规则之前对原始规则进行更精细的调整，但在大多数情况下，一旦我们调整了日期规则，这种额外的努力就会浪费掉。
         */
        switch (startTimeMode) {
        case UTC_TIME:
            startTime += rawOffset;
            break;
        }
        while (startTime < 0) {
            startTime += millisPerDay;
            startDayOfWeek = 1 + ((startDayOfWeek+5) % 7); // 向后一天
        }
        while (startTime >= millisPerDay) {
            startTime -= millisPerDay;
            startDayOfWeek = 1 + (startDayOfWeek % 7); // 向前一天
        }

        switch (endTimeMode) {
        case UTC_TIME:
            endTime += rawOffset + dstSavings;
            break;
        case STANDARD_TIME:
            endTime += dstSavings;
        }
        while (endTime < 0) {
            endTime += millisPerDay;
            endDayOfWeek = 1 + ((endDayOfWeek+5) % 7); // 向后一天
        }
        while (endTime >= millisPerDay) {
            endTime -= millisPerDay;
            endDayOfWeek = 1 + (endDayOfWeek % 7); // 向前一天
        }
    }

    /**
     * 将开始和结束规则打包到一个字节数组中。仅打包 makeRulesCompatible 未保留的数据。
     */
    private byte[] packRules()
    {
        byte[] rules = new byte[MAX_RULE_NUM];
        rules[0] = (byte)startDay;
        rules[1] = (byte)startDayOfWeek;
        rules[2] = (byte)endDay;
        rules[3] = (byte)endDayOfWeek;

        // 从序列化版本 2 开始，包含时间模式
        rules[4] = (byte)startTimeMode;
        rules[5] = (byte)endTimeMode;

        return rules;
    }

    /**
     * 给定一个由 packRules 生成的字节数组，将其解释为开始和结束规则。
     */
    private void unpackRules(byte[] rules)
    {
        startDay       = rules[0];
        startDayOfWeek = rules[1];
        endDay         = rules[2];
        endDayOfWeek   = rules[3];

        // 从序列化版本 2 开始，包含时间模式
        if (rules.length >= MAX_RULE_NUM) {
            startTimeMode = rules[4];
            endTimeMode   = rules[5];
        }
    }

    /**
     * 将开始和结束时间打包到一个字节数组中。这是从序列化版本 2 开始必需的。
     */
    private int[] packTimes() {
        int[] times = new int[2];
        times[0] = startTime;
        times[1] = endTime;
        return times;
    }

    /**
     * 从一个字节数组中解包开始和结束时间。这是从序列化版本 2 开始必需的。
     */
    private void unpackTimes(int[] times) {
        startTime = times[0];
        endTime = times[1];
    }

    /**
     * 将此对象的状态保存到流中（即序列化）。
     *
     * @serialData 我们写入两种格式，一种是与 JDK 1.1 兼容的格式，使用 <code>DOW_IN_MONTH_MODE</code> 规则，
     * 在必需部分中，另一种是完整的规则，以打包格式在可选部分中。JDK 1.1 代码在流输入时将忽略可选部分。
     * <p> 可选部分的内容：发出一个字节数组的长度（int）；在本发行版中为 4。发出给定长度的字节数组。
     * 字节数组的内容是字段 <code>startDay</code>、<code>startDayOfWeek</code>、<code>endDay</code>
     * 和 <code>endDayOfWeek</code> 的真实值。必需部分中的这些字段的值是适合 <code>DOW_IN_MONTH_MODE</code>
     * 规则的近似值，这是 JDK 1.1 唯一识别的模式。
     */
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        // 构建一个二进制规则
        byte[] rules = packRules();
        int[] times = packTimes();

        // 转换为 1.1 FCS 规则。这一步可能会导致我们丢失信息。
        makeRulesCompatible();

        // 写入 1.1 FCS 规则
        stream.defaultWriteObject();

        // 在流的可选数据区域中写入二进制规则。
        stream.writeInt(rules.length);
        stream.write(rules);
        stream.writeObject(times);

        // 恢复原始规则。这将恢复 makeRulesCompatible 丢失的信息。
        unpackRules(rules);
        unpackTimes(times);
    }

    /**
     * 从流中重建此对象（即反序列化）。
     *
     * 我们处理 JDK 1.1 二进制格式和带有打包字节数组的完整格式。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();

        if (serialVersionOnStream < 1) {
            // 修复 1.1 SimpleTimeZone 代码中的一个错误——即，
            // startDayOfWeek 和 endDayOfWeek 通常未初始化。我们不能做太多，所以假设是 SUNDAY，这在大多数情况下实际上可以工作。
            if (startDayOfWeek == 0) {
                startDayOfWeek = Calendar.SUNDAY;
            }
            if (endDayOfWeek == 0) {
                endDayOfWeek = Calendar.SUNDAY;
            }

            // 变量 dstSavings、startMode 和 endMode 是 1.1 之后的，因此如果从 1.1 流中读取，它们将不存在。修复它们。
            startMode = endMode = DOW_IN_MONTH_MODE;
            dstSavings = millisPerHour;
        } else {
            // 对于 1.1.4，除了 3 个新的实例变量外，我们还在可选区域中存储了实际规则（这些规则未与 1.1 兼容）。
            // 在这里读取并解析它们。
            int length = stream.readInt();
            if (length <= MAX_RULE_NUM) {
                byte[] rules = new byte[length];
                stream.readFully(rules);
                unpackRules(rules);
            } else {
                throw new InvalidObjectException("规则太多: " + length);
            }
        }

        if (serialVersionOnStream >= 2) {
            int[] times = (int[]) stream.readObject();
            unpackTimes(times);
        }

        serialVersionOnStream = currentSerialVersion;
    }
}
