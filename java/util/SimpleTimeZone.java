
/*
 * Copyright (c) 1996, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * <code>SimpleTimeZone</code> 是 <code>TimeZone</code> 的一个具体子类，表示用于格里高利历的时间区。
 * 该类持有一个相对于格林尼治标准时间（GMT）的偏移量，称为 <em>原始偏移量</em>，以及夏令时开始和结束的规则。
 * 由于它只持有每个规则的单个值，因此无法处理 GMT 偏移量和夏令时计划的历史变化，但可以通过 {@link
 * #setStartYear setStartYear} 方法指定夏令时计划生效的年份。
 * <p>
 * 要构造一个具有夏令时计划的 <code>SimpleTimeZone</code>，该计划可以通过一组规则来描述，
 * <em>开始规则</em> 和 <em>结束规则</em>。夏令时开始或结束的日期可以通过 <em>月份</em>、
 * <em>月份中的日期</em> 和 <em>星期几</em> 的组合来指定。月份值由 Calendar {@link Calendar#MONTH MONTH} 字段值表示，
 * 例如 {@link Calendar#MARCH}。星期几值由 Calendar {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 字段值表示，
 * 例如 {@link Calendar#SUNDAY SUNDAY}。值组合的含义如下。
 *
 * <ul>
 * <li><b>月份中的确切日期</b><br>
 * 要指定月份中的确切日期，将 <em>月份</em> 和 <em>月份中的日期</em> 设置为确切值，将 <em>星期几</em> 设置为零。例如，
 * 要指定 3 月 1 日，将 <em>月份</em> 设置为 {@link Calendar#MARCH MARCH}，<em>月份中的日期</em> 设置为 1，
 * <em>星期几</em> 设置为 0。</li>
 *
 * <li><b>月份中的某一天或之后的星期几</b><br>
 * 要指定月份中的某一天或之后的星期几，将 <em>月份</em> 设置为确切的月份值，<em>月份中的日期</em> 设置为规则应用的日期或之后的日期，
 * <em>星期几</em> 设置为负的 {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 字段值。例如，要指定 4 月的第二个星期日，
 * 将 <em>月份</em> 设置为 {@link Calendar#APRIL APRIL}，<em>月份中的日期</em> 设置为 8，<em>星期几</em> 设置为 <code>-</code>{@link
 * Calendar#SUNDAY SUNDAY}。</li>
 *
 * <li><b>月份中的某一天或之前的星期几</b><br>
 * 要指定月份中的某一天或之前的星期几，将 <em>月份中的日期</em> 和 <em>星期几</em> 设置为负值。例如，要指定 3 月 21 日或之前的最后一个星期三，
 * 将 <em>月份</em> 设置为 {@link Calendar#MARCH MARCH}，<em>月份中的日期</em> 设置为 -21，<em>星期几</em> 设置为 <code>-</code>{@link Calendar#WEDNESDAY WEDNESDAY}。</li>
 *
 * <li><b>月份的最后一个星期几</b><br>
 * 要指定月份的最后一个星期几，将 <em>星期几</em> 设置为 {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} 值，<em>月份中的日期</em> 设置为 -1。例如，
 * 要指定 10 月的最后一个星期日，将 <em>月份</em> 设置为 {@link Calendar#OCTOBER OCTOBER}，<em>星期几</em> 设置为 {@link
 * Calendar#SUNDAY SUNDAY}，<em>月份中的日期</em> 设置为 -1。</li>
 *
 * </ul>
 * 夏令时开始或结束的时间通过一天中的毫秒值来指定。有三种 <em>模式</em> 来指定时间：{@link #WALL_TIME}、{@link
 * #STANDARD_TIME} 和 {@link #UTC_TIME}。例如，如果夏令时在标准时间 2:00 am 结束，可以在 {@link #WALL_TIME} 模式中通过 7200000
 * 毫秒来指定。在这种情况下，<em>结束规则</em> 的标准时间与夏令时相同。
 * <p>
 * 以下是构造时间区对象的参数示例。
 * <pre><code>
 *      // 基础 GMT 偏移量: -8:00
 *      // 夏令时开始:      在标准时间 2:00 am
 *      //                  4 月的第一个星期日
 *      // 夏令时结束:      在夏令时 2:00 am
 *      //                  10 月的最后一个星期日
 *      // 保存:            1 小时
 *      SimpleTimeZone(-28800000,
 *                     "America/Los_Angeles",
 *                     Calendar.APRIL, 1, -Calendar.SUNDAY,
 *                     7200000,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     7200000,
 *                     3600000)
 *
 *      // 基础 GMT 偏移量: +1:00
 *      // 夏令时开始:      在 UTC 时间 1:00 am
 *      //                  3 月的最后一个星期日
 *      // 夏令时结束:      在 UTC 时间 1:00 am
 *      //                  10 月的最后一个星期日
 *      // 保存:            1 小时
 *      SimpleTimeZone(3600000,
 *                     "Europe/Paris",
 *                     Calendar.MARCH, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     3600000)
 * </code></pre>
 * 这些参数规则也适用于设置规则的方法，例如 <code>setStartRule</code>。
 *
 * @since 1.1
 * @see      Calendar
 * @see      GregorianCalendar
 * @see      TimeZone
 * @author   David Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 */


public class SimpleTimeZone extends TimeZone {
    /**
     * 构造一个具有给定基本时区偏移量和时区ID的SimpleTimeZone，没有夏令时安排。
     *
     * @param rawOffset  以毫秒为单位的基本时区偏移量。
     * @param ID         分配给此实例的时区名称。
     */
    public SimpleTimeZone(int rawOffset, String ID)
    {
        this.rawOffset = rawOffset;
        setID (ID);
        dstSavings = millisPerHour; // 以防用户稍后设置规则
    }

    /**
     * 构造一个具有给定基本时区偏移量、时区ID和夏令时开始和结束规则的SimpleTimeZone。
     * <code>startTime</code>和<code>endTime</code>都被指定为表示墙钟时间。假设夏令时的节省时间为3600000毫秒（即1小时）。此构造函数等效于：
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
     * @param rawOffset       从GMT开始的基本时区偏移量。
     * @param ID              分配给此对象的时区ID。
     * @param startMonth      夏令时开始的月份。月份是{@link Calendar#MONTH MONTH}字段值（0开始，例如0表示1月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       以本地墙钟时间（毫秒内的一天）表示的夏令时开始时间，在这种情况下是本地标准时间。
     * @param endMonth        夏令时结束的月份。月份是{@link Calendar#MONTH MONTH}字段值（0开始，例如9表示10月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         以本地墙钟时间（毫秒内的一天）表示的夏令时结束时间，在这种情况下是本地夏令时。
     * @exception IllegalArgumentException 如果开始或结束规则的月份、日期、星期几或时间参数超出范围
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
     * 构造一个具有给定基本时区偏移量、时区ID和夏令时开始和结束规则的SimpleTimeZone。
     * 假设<code>startTime</code>和<code>endTime</code>都表示为墙钟时间。此构造函数等效于：
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
     * @param rawOffset       从GMT开始的基本时区偏移量。
     * @param ID              分配给此对象的时区ID。
     * @param startMonth      夏令时开始的月份。月份是{@link Calendar#MONTH MONTH}字段值（0开始，例如0表示1月）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       以本地墙钟时间表示的夏令时开始时间，在这种情况下是本地标准时间。
     * @param endMonth        夏令时结束的月份。月份是{@link Calendar#MONTH MONTH}字段值（0开始，例如9表示10月）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         以本地墙钟时间表示的夏令时结束时间，在这种情况下是本地夏令时。
     * @param dstSavings      夏令时节省的时间量（以毫秒为单位）。
     * @exception IllegalArgumentException 如果开始或结束规则的月份、日期、星期几或时间参数超出范围
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
}

                /**
     * 使用给定的基本时区偏移量、时区ID和开始和结束夏令时的规则构造一个SimpleTimeZone。
     * 此构造函数接受开始和结束规则参数的完整集，包括<code>startTime</code>和
     * <code>endTime</code>的模式。模式指定为{@link #WALL_TIME 墙时间}或
     * {@link #STANDARD_TIME 标准时间}或{@link #UTC_TIME UTC时间}。
     *
     * @param rawOffset       从GMT开始的基本时区偏移量。
     * @param ID              分配给此对象的时区ID。
     * @param startMonth      夏令时开始的月份。月份是
     *                        {@link Calendar#MONTH MONTH}字段
     *                        值（0开始。例如，1月为0）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       以<code>startTimeMode</code>指定的模式开始的夏令时时间。
     * @param startTimeMode   由startTime指定的开始时间的模式。
     * @param endMonth        夏令时结束的月份。月份是
     *                        {@link Calendar#MONTH MONTH}字段
     *                        值（0开始。例如，10月为9）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endDayOfWeek    夏令时结束的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param endTime         以<code>endTimeMode</code>指定的模式结束的夏令时时间。
     * @param endTimeMode     由endTime指定的结束时间的模式。
     * @param dstSavings      夏令时期间节省的时间量，以毫秒为单位。
     *
     * @exception IllegalArgumentException 如果开始或结束规则的月份、日期、星期几、时间模式或
     * 时间参数超出范围，或者时间模式值无效。
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

        // this.useDaylight 由decodeRules设置
        decodeRules();
        if (dstSavings <= 0) {
            throw new IllegalArgumentException("非法的夏令时值: " + dstSavings);
        }
    }

    /**
     * 设置夏令时开始的年份。
     *
     * @param year  夏令时开始的年份。
     */
    public void setStartYear(int year)
    {
        startYear = year;
        invalidateCache();
    }

    /**
     * 设置夏令时开始规则。例如，如果夏令时在4月的第一个星期日的当地时间2点开始，可以通过调用以下方法设置开始规则：
     * <pre><code>setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2*60*60*1000);</code></pre>
     *
     * @param startMonth      夏令时开始的月份。月份是
     *                        {@link Calendar#MONTH MONTH}字段
     *                        值（0开始。例如，1月为0）。
     * @param startDay        夏令时开始的月份中的日期。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startDayOfWeek  夏令时开始的星期几。
     *                        请参阅类描述中的此参数的特殊情况。
     * @param startTime       以当地时间（在这种情况下为本地标准时间）表示的夏令时开始时间。
     * @exception IllegalArgumentException 如果<code>startMonth</code>、<code>startDay</code>、
     * <code>startDayOfWeek</code>或<code>startTime</code>参数超出范围。
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
     * 将夏令时开始规则设置为月份内的固定日期。
     * 此方法等同于：
     * <pre><code>setStartRule(startMonth, startDay, 0, startTime)</code></pre>
     *
     * @param startMonth      夏令时开始的月份。月份是
     *                        {@link Calendar#MONTH MONTH}字段
     *                        值（0开始。例如，1月为0）。
     * @param startDay        夏令时开始的月份中的日期。
     * @param startTime       以当地时间（在这种情况下为本地标准时间）表示的夏令时开始时间。
     *                        请参阅类描述中的此参数的特殊情况。
     * @exception IllegalArgumentException 如果<code>startMonth</code>、
     * <code>startDayOfMonth</code>或<code>startTime</code>参数超出范围。
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startTime) {
        setStartRule(startMonth, startDay, 0, startTime);
    }


                /**
     * 设置夏令时开始规则为月份内的某一天之前或之后的某一周几，例如，8号或之后的第一个周一。
     *
     * @param startMonth      夏令时开始的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如1月为0）。
     * @param startDay        夏令时开始的月份中的日期。
     * @param startDayOfWeek  夏令时开始的周几。
     * @param startTime       夏令时开始的本地标准时间，以本地墙时间表示。
     * @param after           如果为true，此规则选择<code>dayOfMonth</code>或之后的第一个<code>dayOfWeek</code>。
     *                        如果为false，此规则选择<code>dayOfMonth</code>或之前的最后一个<code>dayOfWeek</code>。
     * @exception IllegalArgumentException 如果<code>startMonth</code>、<code>startDay</code>、
     * <code>startDayOfWeek</code>或<code>startTime</code>参数超出范围
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startDayOfWeek,
                             int startTime, boolean after)
    {
        // TODO: this method doesn't check the initial values of dayOfMonth or dayOfWeek.
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
     *                        值（从0开始，例如10月为9）。
     * @param endDay          夏令时结束的月份中的日期。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param endDayOfWeek    夏令时结束的周几。
     *                        有关此参数的特殊情况，请参阅类描述。
     * @param endTime         夏令时结束的本地墙时间（以毫秒为单位），此时为本地夏令时。
     * @exception IllegalArgumentException 如果<code>endMonth</code>、<code>endDay</code>、
     * <code>endDayOfWeek</code>或<code>endTime</code>参数超出范围
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
     * 设置夏令时结束规则为月份内的固定日期。此方法等同于：
     * <pre><code>setEndRule(endMonth, endDay, 0, endTime)</code></pre>
     *
     * @param endMonth        夏令时结束的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如10月为9）。
     * @param endDay          夏令时结束的月份中的日期。
     * @param endTime         夏令时结束的本地墙时间（以毫秒为单位），此时为本地夏令时。
     * @exception IllegalArgumentException 如果<code>endMonth</code>、<code>endDay</code>、
     * 或<code>endTime</code>参数超出范围
     * @since 1.2
     */
    public void setEndRule(int endMonth, int endDay, int endTime)
    {
        setEndRule(endMonth, endDay, 0, endTime);
    }

    /**
     * 设置夏令时结束规则为月份内的某一天之前或之后的某一周几，例如，8号或之后的第一个周一。
     *
     * @param endMonth        夏令时结束的月份。月份是一个 {@link Calendar#MONTH MONTH} 字段
     *                        值（从0开始，例如10月为9）。
     * @param endDay          夏令时结束的月份中的日期。
     * @param endDayOfWeek    夏令时结束的周几。
     * @param endTime         夏令时结束的本地墙时间（以毫秒为单位），此时为本地夏令时。
     * @param after           如果为true，此规则选择<code>endDay</code>或之后的第一个<code>endDayOfWeek</code>。
     *                        如果为false，此规则选择<code>endDay</code>或之前的最后一个<code>endDayOfWeek</code>。
     * @exception IllegalArgumentException 如果<code>endMonth</code>、<code>endDay</code>、
     * <code>endDayOfWeek</code>或<code>endTime</code>参数超出范围
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
     * 返回给定时间此时区与UTC的偏移量。如果给定时间处于夏令时，偏移量会调整为包含夏令时的偏移。
     *
     * @param date 给定时间
     * @return 需要添加到UTC以获取本地时间的毫秒数。
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
     * 返回指定日期和时间的本地时间与UTC之间的差值（以毫秒为单位），考虑了原始偏移量和夏令时的影响。此方法假设起始和结束月份是不同的。
     * 它还使用默认的 {@link GregorianCalendar} 对象作为其底层日历，例如用于确定闰年。不要将此方法的结果与默认
     * <code>GregorianCalendar</code> 以外的日历一起使用。
     *
     * <p><em>注意：通常，客户端应使用
     * <code>Calendar.get(ZONE_OFFSET) + Calendar.get(DST_OFFSET)</code>
     * 而不是调用此方法。</em>
     *
     * @param era       给定日期的纪元。
     * @param year      给定日期的年份。
     * @param month     给定日期的月份。月份从0开始，例如，0表示1月。
     * @param day       给定日期的月份中的天数。
     * @param dayOfWeek 给定日期的周中的天数。
     * @param millis    <em>标准</em>本地时间中的毫秒数。
     * @return          需要添加到UTC以获取本地时间的毫秒数。
     * @exception       IllegalArgumentException 如果 <code>era</code>、
     *                  <code>month</code>、<code>day</code>、<code>dayOfWeek</code>、
     *                  或 <code>millis</code> 参数超出范围
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
                         int millis)
    {
        if (era != GregorianCalendar.AD && era != GregorianCalendar.BC) {
            throw new IllegalArgumentException("非法纪元 " + era);
        }

        int y = year;
        if (era == GregorianCalendar.BC) {
            // 调整 y 以适应 GregorianCalendar 风格的年份编号。
            y = 1 - y;
        }

        // 如果年份不能用64位长整数表示为毫秒，将其转换为等效年份。这是为了通过一些实际上无用的 JCK 测试用例，
        // 因为指定的年份不能被 Java 时间系统支持。
        if (y >= 292278994) {
            y = 2800 + y % 2800;
        } else if (y <= -292269054) {
            // y %= 28 也会产生等效年份，但使用正年份编号会更方便使用 UNIX cal 命令。
            y = (int) CalendarUtils.mod((long) y, 28);
        }

        // 将年份转换为其1基月份值
        int m = month + 1;

        // 首先，将时间计算为格里高利历日期。
        BaseCalendar cal = gcal;
        BaseCalendar.Date cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cdate.setDate(y, m, day);
        long time = cal.getTime(cdate); // 标准化 cdate
        time += millis - rawOffset; // UTC 时间

        // 如果时间值表示的时间早于默认的格里高利历转换时间，使用朱利安历系统重新计算时间。
        // 对于朱利安历系统，标准化的年份编号为 ..., -2 (BCE 2), -1 (BCE 1), 1, 2 ...，这与
        // GregorianCalendar 风格的年份编号 (..., -1, 0 (BCE 1), 1, 2, ...) 不同。
        if (time < GregorianCalendar.DEFAULT_GREGORIAN_CUTOVER) {
            cal = (BaseCalendar) CalendarSystem.forName("julian");
            cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
            cdate.setNormalizedDate(y, m, day);
            time = cal.getTime(cdate) + millis - rawOffset;
        }

        if ((cdate.getNormalizedYear() != y)
            || (cdate.getMonth() != m)
            || (cdate.getDayOfMonth() != day)
            // 验证应该是 cdate.getDayOfWeek() == dayOfWeek。但是，为了兼容性，我们不检查 dayOfWeek。
            || (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY)
            || (millis < 0 || millis >= (24*60*60*1000))) {
            throw new IllegalArgumentException();
        }

        if (!useDaylight || year < startYear || era != GregorianCalendar.CE) {
            return rawOffset;
        }

        return getOffset(cal, cdate, y, time);
    }

    private int getOffset(BaseCalendar cal, BaseCalendar.Date cdate, int year, long time) {
        synchronized (this) {
            if (cacheStart != 0) {
                if (time >= cacheStart && time < cacheEnd) {
                    return rawOffset + dstSavings;
                }
                if (year == cacheYear) {
                    return rawOffset;
                }
            }
        }

        long start = getStart(cal, cdate, year);
        long end = getEnd(cal, cdate, year);
        int offset = rawOffset;
        if (start <= end) {
            if (time >= start && time < end) {
                offset += dstSavings;
            }
            synchronized (this) {
                cacheYear = year;
                cacheStart = start;
                cacheEnd = end;
            }
        } else {
            if (time < end) {
                // TODO: 支持格里高利历转换。前一年可能在另一个日历系统中。
                start = getStart(cal, cdate, year - 1);
                if (time >= start) {
                    offset += dstSavings;
                }
            } else if (time >= start) {
                // TODO: 支持格里高利历转换。下一年可能在另一个日历系统中。
                end = getEnd(cal, cdate, year + 1);
                if (time < end) {
                    offset += dstSavings;
                }
            }
            if (start <= end) {
                synchronized (this) {
                    // 起始和结束转换跨越多个年份。
                    cacheYear = (long) startYear - 1;
                    cacheStart = start;
                    cacheEnd = end;
                }
            }
        }
        return offset;
    }


                private long getStart(BaseCalendar cal, BaseCalendar.Date cdate, int year) {
        int time = startTime;
        if (startTimeMode != UTC_TIME) {
            time -= rawOffset;
        }
        return getTransition(cal, cdate, startMode, year, startMonth, startDay,
                             startDayOfWeek, time);
    }

    private long getEnd(BaseCalendar cal, BaseCalendar.Date cdate, int year) {
        int time = endTime;
        if (endTimeMode != UTC_TIME) {
            time -= rawOffset;
        }
        if (endTimeMode == WALL_TIME) {
            time -= dstSavings;
        }
        return getTransition(cal, cdate, endMode, year, endMonth, endDay,
                                        endDayOfWeek, time);
    }

    private long getTransition(BaseCalendar cal, BaseCalendar.Date cdate,
                               int mode, int year, int month, int dayOfMonth,
                               int dayOfWeek, int timeOfDay) {
        cdate.setNormalizedYear(year);
        cdate.setMonth(month + 1);
        switch (mode) {
        case DOM_MODE:
            cdate.setDayOfMonth(dayOfMonth);
            break;

        case DOW_IN_MONTH_MODE:
            cdate.setDayOfMonth(1);
            if (dayOfMonth < 0) {
                cdate.setDayOfMonth(cal.getMonthLength(cdate));
            }
            cdate = (BaseCalendar.Date) cal.getNthDayOfWeek(dayOfMonth, dayOfWeek, cdate);
            break;

        case DOW_GE_DOM_MODE:
            cdate.setDayOfMonth(dayOfMonth);
            cdate = (BaseCalendar.Date) cal.getNthDayOfWeek(1, dayOfWeek, cdate);
            break;

        case DOW_LE_DOM_MODE:
            cdate.setDayOfMonth(dayOfMonth);
            cdate = (BaseCalendar.Date) cal.getNthDayOfWeek(-1, dayOfWeek, cdate);
            break;
        }
        return cal.getTime(cdate) + timeOfDay;
    }

    /**
     * 获取此时区的 GMT 偏移量。
     * @return 以毫秒为单位的 GMT 偏移量
     * @see #setRawOffset
     */
    public int getRawOffset()
    {
        // 如果我们有历史时区数据，将考虑给定的日期。
        return rawOffset;
    }

    /**
     * 设置基本时区偏移量到 GMT。
     * 这是添加到 UTC 以获取本地时间的偏移量。
     * @see #getRawOffset
     */
    public void setRawOffset(int offsetMillis)
    {
        this.rawOffset = offsetMillis;
    }

    /**
     * 设置在夏令时期间时钟提前的时间量（以毫秒为单位）。
     * @param millisSavedDuringDST 当夏令时规则生效时，相对于标准时间时钟提前的毫秒数。通常为一小时（3600000）。
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
     * 返回在夏令时期间时钟提前的时间量（以毫秒为单位）。
     *
     * @return 当夏令时规则生效时，相对于标准时间时钟提前的毫秒数，如果此时区不使用夏令时，则返回 0（零）。
     *
     * @see #setDSTSavings
     * @since 1.2
     */
    public int getDSTSavings() {
        return useDaylight ? dstSavings : 0;
    }

    /**
     * 查询此时间区是否使用夏令时。
     * @return 如果此时间区使用夏令时，则返回 true；否则返回 false。
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
     * 查询给定日期是否在夏令时期间。
     * @return 如果给定日期在夏令时期间，则返回 true；否则返回 false。
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
     * @return 如果给定的 <code>obj</code> 与此 <code>SimpleTimeZone</code> 对象相同，则返回 true；否则返回 false。
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
     * 如果此区域具有与另一个区域相同的规则和偏移量，则返回 <code>true</code>。
     * @param other 要比较的 TimeZone 对象
     * @return 如果给定的区域是 SimpleTimeZone 并且具有与此区域相同的规则和偏移量，则返回 <code>true</code>
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
     * 返回此时区的字符串表示形式。
     * @return 此时区的字符串表示形式。
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
     * <code>Calendar.DECEMBER</code> 之间（包括这两个值）。此值不能等于
     * <code>endMonth</code>。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startMonth;

    /**
     * 此字段有两种可能的解释：
     * <dl>
     * <dt><code>startMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> 表示夏令时开始的 <code>startMonth</code> 月份中的日期，从 1 到 28、30 或 31，具体取决于
     * <code>startMonth</code>。
     * </dd>
     * <dt><code>startMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> 表示夏令时开始的 <code>startMonth</code> 月份中的 <code>startDayOfWeek</code>。例如，值为 +1 且
     * <code>startDayOfWeek</code> 为 <code>Calendar.SUNDAY</code> 表示 <code>startMonth</code> 的第一个星期日。同样，+2 表示第二个星期日，-1 表示最后一个星期日。值为 0 是非法的。
     * </dd>
     * </dl>
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startDay;

    /**
     * 夏令时开始的星期几。此值必须在 <code>Calendar.SUNDAY</code> 和
     * <code>Calendar.SATURDAY</code> 之间（包括这两个值）。
     * <p>如果 <code>useDaylight</code> 为 false 或
     * <code>startMode == DAY_OF_MONTH</code>，则忽略此值。
     * @serial
     */
    private int startDayOfWeek;

    /**
     * 夏令时开始的时间，以午夜后的毫秒数表示。此值表示为标准时间、夏令时或 UTC 时间，具体取决于 <code>startTimeMode</code> 的设置。
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
     * <code>Calendar.UNDECIMBER</code> 之间。此值不能等于
     * <code>startMonth</code>。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int endMonth;

    /**
     * 此字段有两种可能的解释：
     * <dl>
     * <dt><code>endMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> 表示夏令时结束的 <code>endMonth</code> 月份中的日期，从 1 到 28、30 或 31，具体取决于
     * <code>endMonth</code>。
     * </dd>
     * <dt><code>endMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> 表示夏令时结束的 <code>endMonth</code> 月份中的 <code>endDayOfWeek</code>。例如，值为 +1 且
     * <code>endDayOfWeek</code> 为 <code>Calendar.SUNDAY</code> 表示 <code>endMonth</code> 的第一个星期日。同样，+2 表示第二个星期日，-1 表示最后一个星期日。值为 0 是非法的。
     * </dd>
     * </dl>
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int endDay;

    /**
     * 夏令时结束的星期几。此值必须在 <code>Calendar.SUNDAY</code> 和
     * <code>Calendar.SATURDAY</code> 之间（包括这两个值）。
     * <p>如果 <code>useDaylight</code> 为 false 或
     * <code>endMode == DAY_OF_MONTH</code>，则忽略此值。
     * @serial
     */
    private int endDayOfWeek;

    /**
     * 夏令时结束的时间，以午夜后的毫秒数表示。此值表示为标准时间、夏令时或 UTC 时间，具体取决于 <code>endTimeMode</code> 的设置。
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
     * 值。如果此值小于 1，则夏令时在所有 <code>AD</code> 年份都会被观察到。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     */
    private int startYear;

    /**
     * 此时区与 GMT 之间的偏移量（以毫秒为单位）。负偏移量表示在格林尼治以西。为了获得本地 <em>标准</em> 时间，
     * 需要将偏移量加到 GMT 时间上。为了获得本地时间，可能还需要加上 <code>dstSavings</code>。
     * @serial
     */
    private int rawOffset;

                /**
     * 一个布尔值，当且仅当此时区使用夏令时为 true。如果此值为 false，则忽略其他几个字段。
     * @serial
     */
    private boolean useDaylight=false; // 表示此时区是否使用夏令时

    private static final int millisPerHour = 60*60*1000;
    private static final int millisPerDay  = 24*millisPerHour;

    /**
     * 此字段在 JDK 1.1 中被序列化，所以我们必须保持这种方式以维持序列化兼容性。然而，每次创建新的时区时，没有必要重新创建数组。
     * @serial 包含值 {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31} 的字节数组。从 Java 2 平台 v1.2 开始忽略此字段，但为了与 JDK 1.1 兼容，必须将其流输出。
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
     * 此字段的设置影响 <code>startDay</code> 字段的解释。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
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
     * 此字段的设置影响 <code>endDay</code> 字段的解释。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     * @since 1.1.4
     */
    private int endMode;

    /**
     * 一个正值，表示夏令时期间节省的时间量（以毫秒为单位）。
     * 通常为一小时（3600000）；有时为30分钟（1800000）。
     * <p>如果 <code>useDaylight</code> 为 false，则忽略此值。
     * @serial
     * @since 1.1.4
     */
    private int dstSavings;

    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();

    /**
     * 表示夏令时单个周期的缓存值。当缓存值有效时，cacheStart 是夏令时的开始时间（包含），cacheEnd 是结束时间（不包含）。
     *
     * 如果 cacheStart 和 cacheEnd 在同一年，则 cacheYear 有一个年份值。如果 cacheStart 和 cacheEnd 在不同年，则 cacheYear 设置为 startYear - 1。如果缓存值无效，则 cacheStart 为 0。cacheYear 是 long 类型，以支持 Integer.MIN_VALUE - 1（JCK 要求）。
     */
    private transient long cacheYear;
    private transient long cacheStart;
    private transient long cacheEnd;

    /**
     * 指定 startMode 和 endMode 值的常量。
     */
    private static final int DOM_MODE          = 1; // 确切的月份日期，"Mar 1"
    private static final int DOW_IN_MONTH_MODE = 2; // 月份中的星期几，"lastSun"
    private static final int DOW_GE_DOM_MODE   = 3; // 月份后的星期几，"Sun>=15"
    private static final int DOW_LE_DOM_MODE   = 4; // 月份前的星期几，"Sun<=21"

    /**
     * 以标准时间指定的开始或结束时间模式的常量。标准时间是开始规则的标准时间，结束规则的夏令时。
     * @since 1.4
     */
    public static final int WALL_TIME = 0; // 0 用于向后兼容

    /**
     * 以标准时间指定的开始或结束时间模式的常量。
     * @since 1.4
     */
    public static final int STANDARD_TIME = 1;

    /**
     * 以 UTC 指定的开始或结束时间模式的常量。例如，欧盟规则以 UTC 时间指定。
     * @since 1.4
     */
    public static final int UTC_TIME = 2;

    // 声明与 1.1 兼容
    static final long serialVersionUID = -403250971215465050L;

    // 内部序列化版本，说明写入的版本
    // - 0（默认）适用于 JDK 1.1.3 及更早版本
    // - 1 适用于 JDK 1.1.4 及更高版本，包括 3 个新字段
    // - 2 适用于 JDK 1.3 及更高版本，包括 2 个新字段
    static final int currentSerialVersion = 2;

    /**
     * 流中的序列化数据版本。可能的值：
     * <dl>
     * <dt><b>0</b> 或未出现在流中</dt>
     * <dd>
     * JDK 1.1.3 或更早版本。
     * </dd>
     * <dt><b>1</b></dt>
     * <dd>
     * JDK 1.1.4 或更高版本。包括三个新字段：<code>startMode</code>，<code>endMode</code> 和 <code>dstSavings</code>。
     * </dd>
     * <dt><b>2</b></dt>
     * <dd>
     * JDK 1.3 或更高版本。包括两个新字段：<code>startTimeMode</code> 和 <code>endTimeMode</code>。
     * </dd>
     * </dl>
     * 当流输出此类时，将写入最新格式和最高的允许 <code>serialVersionOnStream</code>。
     * @serial
     * @since 1.1.4
     */
    private int serialVersionOnStream = currentSerialVersion;

                // 最大规则数。
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
    //       Sun>=8   第八个或之后的第一个星期日
    //       Sun<=25  25号或之前的最后一个星期日
    // 这进一步复杂化了，因为我们需要与1.1 FCS保持向后兼容。最后，我们需要最小化API的更改。为了满足这些要求，我们支持三种表示系统，并在它们之间进行转换。
    //
    // 内部表示
    // 这是在构造或流输入完成后SimpleTimeZone对象采用的格式。规则直接表示，使用未编码的格式。下面仅讨论开始规则；结束规则类似。
    //   startMode      采用枚举值 DAY_OF_MONTH，
    //                  DOW_IN_MONTH, DOW_AFTER_DOM, 或 DOW_BEFORE_DOM。
    //   startDay       月份的日期，或对于DOW_IN_MONTH模式，一个表示哪个DOW的值，例如+1表示第一个，+2表示第二个，-1表示最后一个等。
    //   startDayOfWeek 一周中的日期。DAY_OF_MONTH模式下忽略。
    //
    // 编码表示
    // 这是构造函数和setStartRule()、setEndRule()接受的格式。它使用各种正、负和零值的组合来编码不同的规则。这种表示允许我们在不更改API的情况下指定所有不同的规则类型。
    //   MODE              startMonth    startDay    startDayOfWeek
    //   DOW_IN_MONTH_MODE >=0           !=0         >0
    //   DOM_MODE          >=0           >0          ==0
    //   DOW_GE_DOM_MODE   >=0           >0          <0
    //   DOW_LE_DOM_MODE   >=0           <0          <0
    //   (无夏令时)        不关心         ==0         不关心
    //
    // 流表示
    // 我们必须与1.1 FCS保持二进制兼容。1.1代码仅处理DOW_IN_MONTH_MODE和非夏令时模式，后者由useDaylight标志指示。当我们流输出一个对象时，我们将其转换为近似的DOW_IN_MONTH_MODE表示，以便1.1代码可以解析和使用。之后，我们将完整的表示单独写入，以便当代代码可以识别和解析。完整的表示以“打包”格式写入，包括版本号、长度和字节数组。此类的未来版本可能指定不同的版本。如果他们希望包含额外的数据，他们应该将它们存储在下面的打包表示之后。
    //----------------------------------------------------------------------

    /**
     * 给定startDay和startDayOfMonth中的编码规则集，解码它们并适当设置startMode。同样处理endDay和endDayOfMonth。进入时，一周中的日期变量可能是零或负数，以指示特殊模式。月份中的日期变量也可能为负数。退出时，模式变量将被设置，一周中的日期和月份中的日期变量将为正数。此方法还识别startDay或endDay为零表示没有夏令时。
     */
    private void decodeRules()
    {
        decodeStartRule();
        decodeEndRule();
    }

    /**
     * 解码开始规则并验证参数。参数预计以编码形式存在，通过否定或置零某些值来表示各种规则模式。表示格式为：
     * <p>
     * <pre>
     *            DOW_IN_MONTH  DOM    DOW>=DOM  DOW<=DOM  无夏令时
     *            ------------  -----  --------  --------  ----------
     * month       0..11        相同    相同      相同     不关心
     * day        -5..5         1..31   1..31    -1..-31   0
     * dayOfWeek   1..7         0      -1..-7    -1..-7    不关心
     * time        0..ONEDAY    相同    相同      相同     不关心
     * </pre>
     * 月份的范围不包括UNDECIMBER，因为此类实际上是特定于GregorianCalendar的，不使用该月份。时间的范围包括ONEDAY（而不是结束于ONEDAY-1），因为结束规则是一个排他性限制点。也就是说，处于夏令时的时间范围包括那些>=开始时间和<结束时间的。因此，应该可以指定一个ONEDAY的结束，以包括整个一天。虽然这等同于次日的时间0，但并不总是可以指定这一点，例如在12月31日。虽然可以说开始范围仍然应该是0..ONEDAY-1，但为了保持一致，我们保持开始和结束范围相同。
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
                           "非法开始一周中的日期 " + startDayOfWeek);
                }
            }
            if (startMode == DOW_IN_MONTH_MODE) {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException(
                            "非法开始一周中的日期在月份 " + startDay);
                }
            } else if (startDay < 1 || startDay > staticMonthLength[startMonth]) {
                throw new IllegalArgumentException(
                        "非法开始日期 " + startDay);
            }
        }
    }


                /**
     * 解码结束规则并验证参数。此方法与 decodeStartRule() 完全类似。
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
                           "非法结束周几 " + endDayOfWeek);
                }
            }
            if (endMode == DOW_IN_MONTH_MODE) {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException(
                            "非法结束周几在月 " + endDay);
                }
            } else if (endDay < 1 || endDay > staticMonthLength[endMonth]) {
                throw new IllegalArgumentException(
                        "非法结束日 " + endDay);
            }
        }
    }

    /**
     * 使规则与 1.1 FCS 代码兼容。由于 1.1 FCS 代码仅理解周几在月的规则，我们必须修改其他模式的规则以使其在 1.1 FCS 术语中近似等效。此方法在流输出此类对象时使用。调用后，规则将被修改，可能会丢失信息。即使从语义上讲 startMode 和 endMode 应该设置为 DOW_IN_MONTH_MODE，但它们不会被更改，因为规则修改仅打算暂时使用。
     */
    private void makeRulesCompatible()
    {
        switch (startMode) {
        case DOM_MODE:
            startDay = 1 + (startDay / 7);
            startDayOfWeek = Calendar.SUNDAY;
            break;

        case DOW_GE_DOM_MODE:
            // 月中的 1 号等同于周几在月的规则
            // 也就是说，Sun>=1 == 第一个周日。
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
            // 月中的 1 号等同于周几在月的规则
            // 也就是说，Sun>=1 == 第一个周日。
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
         * 将开始和结束时间调整为标准时间。除非它推进到下一天或前一天，否则这工作得很好。如果发生这种情况，我们将尝试粗略地调整日期规则。日期规则已经被强制为 DOW_IN_MONTH 模式，因此我们更改周几以向前或向后移动一天。在大多数情况下，一旦我们调整日期规则，首先对原始规则进行更精细的调整将被浪费。
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
     * 将开始和结束规则打包成一个字节数组。仅打包 makeRulesCompatible 不保留的数据。
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
     * 给定由 packRules 生成的字节数组，将其解释为开始和结束规则。
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
     * 将开始时间和结束时间打包成一个字节数组。这是从序列化版本2开始必需的。
     */
    private int[] packTimes() {
        int[] times = new int[2];
        times[0] = startTime;
        times[1] = endTime;
        return times;
    }

    /**
     * 从字节数组中解包开始时间和结束时间。这是从序列化版本2开始必需的。
     */
    private void unpackTimes(int[] times) {
        startTime = times[0];
        endTime = times[1];
    }

    /**
     * 将此对象的状态保存到流中（即，序列化它）。
     *
     * @serialData 我们写出两种格式，一种是与JDK 1.1兼容的格式，使用<code>DOW_IN_MONTH_MODE</code>规则，在必需部分中，另一种是完整的规则，以打包格式在可选部分中。可选部分将被JDK 1.1代码在流输入时忽略。
     * <p> 可选部分的内容：发出一个字节数组的长度（int）；在本发行版中为4。发出给定长度的字节数组。字节数组的内容是字段<code>startDay</code>、<code>startDayOfWeek</code>、<code>endDay</code>和<code>endDayOfWeek</code>的真实值。必需部分中的这些字段的值是适合<code>DOW_IN_MONTH_MODE</code>规则模式的近似值，这是JDK 1.1唯一识别的模式。
     */
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        // 构造一个二进制规则
        byte[] rules = packRules();
        int[] times = packTimes();

        // 转换为1.1 FCS规则。这一步可能会导致我们丢失信息。
        makeRulesCompatible();

        // 写出1.1 FCS规则
        stream.defaultWriteObject();

        // 在流的可选数据区域中写出二进制规则。
        stream.writeInt(rules.length);
        stream.write(rules);
        stream.writeObject(times);

        // 恢复原始规则。这恢复了由makeRulesCompatible丢失的信息。
        unpackRules(rules);
        unpackTimes(times);
    }

    /**
     * 从流中重新构造此对象（即，反序列化它）。
     *
     * 我们处理JDK 1.1
     * 二进制格式和带有打包字节数组的完整格式。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();

        if (serialVersionOnStream < 1) {
            // 修复1.1 SimpleTimeZone代码中的一个错误——即，
            // startDayOfWeek和endDayOfWeek通常未初始化。我们不能做太多，所以假设为星期日，这实际上在大多数情况下都能工作。
            if (startDayOfWeek == 0) {
                startDayOfWeek = Calendar.SUNDAY;
            }
            if (endDayOfWeek == 0) {
                endDayOfWeek = Calendar.SUNDAY;
            }

            // 变量dstSavings、startMode和endMode是1.1之后的，因此如果从1.1流中读取，它们将不存在。修复它们。
            startMode = endMode = DOW_IN_MONTH_MODE;
            dstSavings = millisPerHour;
        } else {
            // 对于1.1.4，除了3个新的实例变量外，我们还在可选区域中
            // 存储实际规则（这些规则尚未与1.1兼容）。在这里读取并解析它们。
            int length = stream.readInt();
            if (length <= MAX_RULE_NUM) {
                byte[] rules = new byte[length];
                stream.readFully(rules);
                unpackRules(rules);
            } else {
                throw new InvalidObjectException("Too many rules: " + length);
            }
        }

        if (serialVersionOnStream >= 2) {
            int[] times = (int[]) stream.readObject();
            unpackTimes(times);
        }

        serialVersionOnStream = currentSerialVersion;
    }
}
