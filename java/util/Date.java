
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.text.DateFormat;
import java.time.LocalDate;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.lang.ref.SoftReference;
import java.time.Instant;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.ZoneInfo;

/**
 * 类 <code>Date</code> 表示一个特定的瞬间，具有毫秒精度。
 * <p>
 * 在 JDK 1.1 之前，类 <code>Date</code> 还有两个额外的功能。它可以解释日期为年、月、日、小时、分钟和秒值。
 * 它还允许格式化和解析日期字符串。不幸的是，这些功能的 API 不利于国际化。从 JDK 1.1 开始，
 * 应该使用 <code>Calendar</code> 类将日期和时间字段进行转换，使用 <code>DateFormat</code> 类进行日期字符串的格式化和解析。
 * <code>Date</code> 类中的相应方法已被弃用。
 * <p>
 * 尽管 <code>Date</code> 类旨在反映协调世界时（UTC），但根据 Java 虚拟机的主机环境，它可能无法完全准确。
 * 几乎所有现代操作系统都认为 1 天 = 24 × 60 × 60 = 86400 秒。然而，在 UTC 中，大约每一年或两年会有一个额外的秒，称为“闰秒”。
 * 闰秒总是作为一天的最后一秒添加，并且总是在 12 月 31 日或 6 月 30 日。例如，1995 年的最后一分钟因为添加了闰秒而有 61 秒。
 * 大多数计算机时钟的精度不足以反映闰秒的区别。
 * <p>
 * 一些计算机标准是根据格林尼治标准时间（GMT）定义的，这等同于世界时（UT）。GMT 是“民用”名称；
 * UT 是“科学”名称。UTC 和 UT 之间的区别在于 UTC 基于原子钟，而 UT 基于天文观测，从实际应用的角度来看，这种区别微乎其微。
 * 由于地球的自转不是均匀的（它会变慢和加速），UT 并不总是均匀流动。为了使 UTC 保持在 UT1（一种应用了某些修正的 UT 版本）的 0.9 秒以内，
 * 会根据需要引入闰秒。还有其他的时间和日期系统；例如，基于卫星的全球定位系统（GPS）使用的时间尺度与 UTC 同步，但不调整闰秒。
 * 有关进一步信息的有趣来源是美国海军天文台，特别是时间局：
 * <blockquote><pre>
 *     <a href=http://tycho.usno.navy.mil>http://tycho.usno.navy.mil</a>
 * </pre></blockquote>
 * <p>
 * 以及他们对“时间系统”的定义：
 * <blockquote><pre>
 *     <a href=http://tycho.usno.navy.mil/systime.html>http://tycho.usno.navy.mil/systime.html</a>
 * </pre></blockquote>
 * <p>
 * 在类 <code>Date</code> 的所有方法中，接受或返回年、月、日、小时、分钟和秒值时，使用以下表示：
 * <ul>
 * <li>年 <i>y</i> 由整数 <i>y</i>&nbsp;<code>-&nbsp;1900</code> 表示。
 * <li>月由 0 到 11 的整数表示；0 是 1 月，1 是 2 月，依此类推；因此 11 是 12 月。
 * <li>日（月中的日期）由 1 到 31 的整数表示，通常如此。
 * <li>小时由 0 到 23 的整数表示。因此，午夜到凌晨 1 点是小时 0，中午到下午 1 点是小时 12。
 * <li>分钟由 0 到 59 的整数表示，通常如此。
 * <li>秒由 0 到 61 的整数表示；60 和 61 仅在闰秒时出现，即使如此，也只有在正确跟踪闰秒的 Java 实现中才会出现。
 *     由于目前闰秒的引入方式，同一分钟内出现两个闰秒的可能性极低，但此规范遵循 ISO C 的日期和时间约定。
 * </ul>
 * <p>
 * 在所有用于这些目的的方法中，提供的参数不必在上述范围内；例如，日期可以指定为 1 月 32 日，解释为 2 月 1 日。
 *
 * @author  James Gosling
 * @author  Arthur van Hoff
 * @author  Alan Liu
 * @see     java.text.DateFormat
 * @see     java.util.Calendar
 * @see     java.util.TimeZone
 * @since   JDK1.0
 */
public class Date
    implements java.io.Serializable, Cloneable, Comparable<Date>
{
    private static final BaseCalendar gcal =
                                CalendarSystem.getGregorianCalendar();
    private static BaseCalendar jcal;

    private transient long fastTime;

    /*
     * 如果 cdate 为 null，则 fastTime 表示毫秒时间。
     * 如果 cdate.isNormalized() 为 true，则 fastTime 和 cdate 同步。否则，忽略 fastTime，cdate 表示时间。
     */
    private transient BaseCalendar.Date cdate;

    // 在使用该值之前初始化。参见 parse()。
    private static int defaultCenturyStart;

    /* 使用修改后的 java.util.Date 的 serialVersionUID
     * 以实现与 JDK1.1 的互操作性。Date 被修改为只写入和读取 UTC 时间。
     */
    private static final long serialVersionUID = 7523967970034938905L;

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示分配时的时间，精确到毫秒。
     *
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date() {
        this(System.currentTimeMillis());
    }

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示自标准基准时间（称为“纪元”）以来的指定毫秒数，
     * 即 1970 年 1 月 1 日 00:00:00 GMT。
     *
     * @param   date   自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date(long date) {
        fastTime = date;
    }

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示由 <code>year</code>、<code>month</code> 和
     * <code>date</code> 参数指定的日期的午夜（本地时间）。
     *
     * @param   year    年份减去 1900。
     * @param   month   0 到 11 之间的月。
     * @param   date    1 到 31 之间的月中的日期。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>Calendar.set(year + 1900, month, date)</code>
     * 或 <code>GregorianCalendar(year + 1900, month, date)</code>。
     */
    @Deprecated
    public Date(int year, int month, int date) {
        this(year, month, date, 0, 0, 0);
    }

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示由 <code>year</code>、<code>month</code>、<code>date</code>、
     * <code>hrs</code> 和 <code>min</code> 参数指定的分钟的开始时间（本地时区）。
     *
     * @param   year    年份减去 1900。
     * @param   month   0 到 11 之间的月。
     * @param   date    1 到 31 之间的月中的日期。
     * @param   hrs     0 到 23 之间的小时。
     * @param   min     0 到 59 之间的分钟。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>Calendar.set(year + 1900, month, date, hrs, min)</code>
     * 或 <code>GregorianCalendar(year + 1900, month, date, hrs, min)</code>。
     */
    @Deprecated
    public Date(int year, int month, int date, int hrs, int min) {
        this(year, month, date, hrs, min, 0);
    }

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示由 <code>year</code>、<code>month</code>、<code>date</code>、
     * <code>hrs</code>、<code>min</code> 和 <code>sec</code> 参数指定的秒的开始时间（本地时区）。
     *
     * @param   year    年份减去 1900。
     * @param   month   0 到 11 之间的月。
     * @param   date    1 到 31 之间的月中的日期。
     * @param   hrs     0 到 23 之间的小时。
     * @param   min     0 到 59 之间的分钟。
     * @param   sec     0 到 59 之间的秒。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>Calendar.set(year + 1900, month, date, hrs, min, sec)</code>
     * 或 <code>GregorianCalendar(year + 1900, month, date, hrs, min, sec)</code>。
     */
    @Deprecated
    public Date(int year, int month, int date, int hrs, int min, int sec) {
        int y = year + 1900;
        // 月从 0 开始。因此，为了支持 Long.MAX_VALUE，我们需要规范化月。
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        BaseCalendar cal = getCalendarSystem(y);
        cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.getDefaultRef());
        cdate.setNormalizedDate(y, month + 1, date).setTimeOfDay(hrs, min, sec, 0);
        getTimeImpl();
        cdate = null;
    }

    /**
     * 分配一个 <code>Date</code> 对象并初始化它，使其表示由字符串 <code>s</code> 指定的日期和时间，
     * 该字符串由 {@link Date#parse} 方法解释。
     *
     * @param   s   日期的字符串表示。
     * @see     java.text.DateFormat
     * @see     java.util.Date#parse(java.lang.String)
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>DateFormat.parse(String s)</code>。
     */
    @Deprecated
    public Date(String s) {
        this(parse(s));
    }

    /**
     * 返回此对象的副本。
     */
    public Object clone() {
        Date d = null;
        try {
            d = (Date)super.clone();
            if (cdate != null) {
                d.cdate = (BaseCalendar.Date) cdate.clone();
            }
        } catch (CloneNotSupportedException e) {} // 不会发生
        return d;
    }

    /**
     * 根据参数确定日期和时间。参数被解释为年、月、月中的日期、小时、分钟和秒，与六个参数的 <tt>Date</tt> 构造函数完全相同，
     * 但参数相对于 UTC 而不是本地时区进行解释。返回的时间表示为从纪元（1970 年 1 月 1 日 00:00:00 GMT）开始的距离，以毫秒为单位。
     *
     * @param   year    年份减去 1900。
     * @param   month   0 到 11 之间的月。
     * @param   date    1 到 31 之间的月中的日期。
     * @param   hrs     0 到 23 之间的小时。
     * @param   min     0 到 59 之间的分钟。
     * @param   sec     0 到 59 之间的秒。
     * @return  从 1970 年 1 月 1 日 00:00:00 GMT 开始，以毫秒为单位的距离，表示由参数指定的日期和时间。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>Calendar.set(year + 1900, month, date, hrs, min, sec)</code>
     * 或 <code>GregorianCalendar(year + 1900, month, date, hrs, min, sec)</code>，使用 UTC <code>TimeZone</code>，
     * 然后调用 <code>Calendar.getTime().getTime()</code>。
     */
    @Deprecated
    public static long UTC(int year, int month, int date,
                           int hrs, int min, int sec) {
        int y = year + 1900;
        // 月从 0 开始。因此，为了支持 Long.MAX_VALUE，我们需要规范化月。
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        int m = month + 1;
        BaseCalendar cal = getCalendarSystem(y);
        BaseCalendar.Date udate = (BaseCalendar.Date) cal.newCalendarDate(null);
        udate.setNormalizedDate(y, m, date).setTimeOfDay(hrs, min, sec, 0);

        // 使用 Date 实例进行规范化。其 fastTime 是规范化后的 UTC 值。
        Date d = new Date(0);
        d.normalize(udate);
        return d.fastTime;
    }

    /**
     * 尝试将字符串 <tt>s</tt> 解释为日期和时间的表示。如果尝试成功，返回的时间表示为从纪元（1970 年 1 月 1 日 00:00:00 GMT）开始的距离，以毫秒为单位。
     * 如果尝试失败，则抛出 <tt>IllegalArgumentException</tt>。
     * <p>
     * 它接受许多语法；特别是，它识别 IETF 标准日期语法：“Sat, 12 Aug 1995 13:30:00 GMT”。它还理解美国大陆的时间区缩写，但为了通用使用，
     * 应该使用时间区偏移：“Sat, 12 Aug 1995 13:30:00 GMT+0430”（格林尼治经线以西 4 小时 30 分钟）。如果没有指定时间区，则假设为本地时间区。
     * GMT 和 UTC 被视为等效。
     * <p>
     * 字符串 <tt>s</tt> 从左到右处理，查找感兴趣的数据。字符串 <tt>s</tt> 中在 ASCII 括号字符 <tt>(</tt> 和 <tt>)</tt> 内的任何内容都被忽略。
     * 括号可以嵌套。否则，字符串 <tt>s</tt> 中允许的唯一字符是这些 ASCII 字符：
     * <blockquote><pre>
     * abcdefghijklmnopqrstuvwxyz
     * ABCDEFGHIJKLMNOPQRSTUVWXYZ
     * 0123456789,+-:/
     * </pre></blockquote>
     * 以及空白字符。<p>
     * 连续的十进制数字被视为十进制数：<ul>
     * <li>如果数字前面有 <tt>+</tt> 或 <tt>-</tt> 且已经识别了年份，则该数字是时间区偏移。如果数字小于 24，则认为是按小时计算的偏移。
     *     否则，认为是按分钟计算的偏移，以 24 小时格式表示，不带标点。前面的 <tt>-</tt> 表示向西偏移。时间区偏移总是相对于 UTC（格林尼治）。
     *     因此，例如，字符串中的 <tt>-5</tt> 表示“格林尼治以西 5 小时”，<tt>+0430</tt> 表示“格林尼治以东 4 小时 30 分钟”。
     *     允许字符串冗余指定 <tt>GMT</tt>、<tt>UT</tt> 或 <tt>UTC</tt>，例如 <tt>GMT-5</tt> 或 <tt>utc+0430</tt>。
     * <li>如果数字满足以下条件之一，则被视为年份：
     * <ul>
     *     <li>数字等于或大于 70 且后面跟着空格、逗号、斜杠或字符串结束
     *     <li>数字小于 70，且已经识别了月和月中的日期</li>
     * </ul>
     *     如果识别的年份小于 100，则解释为相对于初始化 Date 类时日期在 80 年前和 19 年后的世纪的缩写年份。
     *     调整年份后，减去 1900。例如，如果当前年份是 1999，则 19 到 99 的年份假设为 1919 到 1999，0 到 18 的年份假设为 2000 到 2018。
     *     注意，这与 {@link java.text.SimpleDateFormat} 中对小于 100 的年份的解释略有不同。
     * <li>如果数字后面跟着冒号，则被视为小时，除非已经识别了小时，否则被视为分钟。
     * <li>如果数字后面跟着斜杠，则被视为月（减去 1 以生成 0 到 11 的范围），除非已经识别了月，否则被视为月中的日期。
     * <li>如果数字后面跟着空格、逗号、连字符或字符串结束，则如果已经识别了小时但未识别分钟，被视为分钟；
     *     否则，如果已经识别了分钟但未识别秒，被视为秒；否则，被视为月中的日期。</ul><p>
     * 连续的字母序列被视为单词并按以下方式处理：<ul>
     * <li>与 <tt>AM</tt> 匹配的单词（忽略大小写）被忽略（但如果未识别小时或小时小于 <tt>1</tt> 或大于 <tt>12</tt>，则解析失败）。
     * <li>与 <tt>PM</tt> 匹配的单词（忽略大小写）将 12 加到小时上（但如果未识别小时或小时小于 <tt>1</tt> 或大于 <tt>12</tt>，则解析失败）。
     * <li>与 <tt>SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY</tt> 或 <tt>SATURDAY</tt> 的任何前缀匹配的单词（忽略大小写）被忽略。
     *     例如，<tt>sat, Friday, TUE</tt> 和 <tt>Thurs</tt> 被忽略。
     * <li>与 <tt>JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER</tt> 或 <tt>DECEMBER</tt> 的任何前缀匹配的单词（忽略大小写），
     *     按照此处给出的顺序，被视为指定月并转换为数字（0 到 11）。例如，<tt>aug, Sept, april</tt> 和 <tt>NOV</tt> 被识别为月。<tt>Ma</tt> 也被识别为 <tt>MARCH</tt>，而不是 <tt>MAY</tt>。
     * <li>与 <tt>GMT, UT</tt> 或 <tt>UTC</tt> 匹配的单词（忽略大小写）被视为引用 UTC。
     * <li>与 <tt>EST, CST, MST</tt> 或 <tt>PST</tt> 匹配的单词（忽略大小写）被视为引用格林尼治以西五、六、七或八小时的北美时区。
     *     与 <tt>EDT, CDT, MDT</tt> 或 <tt>PDT</tt> 匹配的单词（忽略大小写）被视为引用同一时区，但处于夏令时。</ul><p>
     * 一旦整个字符串 s 被扫描，它将以两种方式之一转换为时间结果。如果识别了时区或时区偏移，则年、月、月中的日期、小时、分钟和秒在 UTC 中解释，然后应用时区偏移。
     * 否则，年、月、月中的日期、小时、分钟和秒在本地时区中解释。
     *
     * @param   s   要解析为日期的字符串。
     * @return  从 1970 年 1 月 1 日 00:00:00 GMT 开始，以毫秒为单位的距离，表示字符串参数表示的时间。
     * @see     java.text.DateFormat
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>DateFormat.parse(String s)</code>。
     */
    @Deprecated
    public static long parse(String s) {
        int year = Integer.MIN_VALUE;
        int mon = -1;
        int mday = -1;
        int hour = -1;
        int min = -1;
        int sec = -1;
        int millis = -1;
        int c = -1;
        int i = 0;
        int n = -1;
        int wst = -1;
        int tzoffset = -1;
        int prevc = 0;
    syntax:
        {
            if (s == null)
                break syntax;
            int limit = s.length();
            while (i < limit) {
                c = s.charAt(i);
                i++;
                if (c <= ' ' || c == ',')
                    continue;
                if (c == '(') { // 跳过注释
                    int depth = 1;
                    while (i < limit) {
                        c = s.charAt(i);
                        i++;
                        if (c == '(') depth++;
                        else if (c == ')')
                            if (--depth <= 0)
                                break;
                    }
                    continue;
                }
                if ('0' <= c && c <= '9') {
                    n = c - '0';
                    while (i < limit && '0' <= (c = s.charAt(i)) && c <= '9') {
                        n = n * 10 + c - '0';
                        i++;
                    }
                    if (prevc == '+' || prevc == '-' && year != Integer.MIN_VALUE) {
                        // 时区偏移
                        if (n < 24)
                            n = n * 60; // 例如 "GMT-3"
                        else
                            n = n % 100 + n / 100 * 60; // 例如 "GMT-0430"
                        if (prevc == '+')   // 加号表示格林尼治以东
                            n = -n;
                        if (tzoffset != 0 && tzoffset != -1)
                            break syntax;
                        tzoffset = n;
                    } else if (n >= 70)
                        if (year != Integer.MIN_VALUE)
                            break syntax;
                        else if (c <= ' ' || c == ',' || c == '/' || i >= limit)
                            // year = n < 1900 ? n : n - 1900;
                            year = n;
                        else
                            break syntax;
                    else if (c == ':')
                        if (hour < 0)
                            hour = (byte) n;
                        else if (min < 0)
                            min = (byte) n;
                        else
                            break syntax;
                    else if (c == '/')
                        if (mon < 0)
                            mon = (byte) (n - 1);
                        else if (mday < 0)
                            mday = (byte) n;
                        else
                            break syntax;
                    else if (i < limit && c != ',' && c > ' ' && c != '-')
                        break syntax;
                    else if (hour >= 0 && min < 0)
                        min = (byte) n;
                    else if (min >= 0 && sec < 0)
                        sec = (byte) n;
                    else if (mday < 0)
                        mday = (byte) n;
                    // 处理小于 70 的两位数年份（70-99 在上面处理）。
                    else if (year == Integer.MIN_VALUE && mon >= 0 && mday >= 0)
                        year = n;
                    else
                        break syntax;
                    prevc = 0;
                } else if (c == '/' || c == ':' || c == '+' || c == '-')
                    prevc = c;
                else {
                    int st = i - 1;
                    while (i < limit) {
                        c = s.charAt(i);
                        if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
                            break;
                        i++;
                    }
                    if (i <= st + 1)
                        break syntax;
                    int k;
                    for (k = wtb.length; --k >= 0;)
                        if (wtb[k].regionMatches(true, 0, s, st, i - st)) {
                            int action = ttb[k];
                            if (action != 0) {
                                if (action == 1) {  // pm
                                    if (hour > 12 || hour < 1)
                                        break syntax;
                                    else if (hour < 12)
                                        hour += 12;
                                } else if (action == 14) {  // am
                                    if (hour > 12 || hour < 1)
                                        break syntax;
                                    else if (hour == 12)
                                        hour = 0;
                                } else if (action <= 13) {  // 月
                                    if (mon < 0)
                                        mon = (byte) (action - 2);
                                    else
                                        break syntax;
                                } else {
                                    tzoffset = action - 10000;
                                }
                            }
                            break;
                        }
                    if (k < 0)
                        break syntax;
                    prevc = 0;
                }
            }
            if (year == Integer.MIN_VALUE || mon < 0 || mday < 0)
                break syntax;
            // 解析 2 位数年份，使其处于正确的默认世纪。
            if (year < 100) {
                synchronized (Date.class) {
                    if (defaultCenturyStart == 0) {
                        defaultCenturyStart = gcal.getCalendarDate().getYear() - 80;
                    }
                }
                year += (defaultCenturyStart / 100) * 100;
                if (year < defaultCenturyStart) year += 100;
            }
            if (sec < 0)
                sec = 0;
            if (min < 0)
                min = 0;
            if (hour < 0)
                hour = 0;
            BaseCalendar cal = getCalendarSystem(year);
            if (tzoffset == -1)  { // 未指定时区，必须使用本地时区
                BaseCalendar.Date ldate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.getDefaultRef());
                ldate.setDate(year, mon + 1, mday);
                ldate.setTimeOfDay(hour, min, sec, 0);
                return cal.getTime(ldate);
            }
            BaseCalendar.Date udate = (BaseCalendar.Date) cal.newCalendarDate(null); // 无时区
            udate.setDate(year, mon + 1, mday);
            udate.setTimeOfDay(hour, min, sec, 0);
            return cal.getTime(udate) + tzoffset * (60 * 1000);
        }
        // 语法错误
        throw new IllegalArgumentException();
    }
    private final static String wtb[] = {
        "am", "pm",
        "monday", "tuesday", "wednesday", "thursday", "friday",
        "saturday", "sunday",
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december",
        "gmt", "ut", "utc", "est", "edt", "cst", "cdt",
        "mst", "mdt", "pst", "pdt"
    };
    private final static int ttb[] = {
        14, 1, 0, 0, 0, 0, 0, 0, 0,
        2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        10000 + 0, 10000 + 0, 10000 + 0,    // GMT/UT/UTC
        10000 + 5 * 60, 10000 + 4 * 60,     // EST/EDT
        10000 + 6 * 60, 10000 + 5 * 60,     // CST/CDT
        10000 + 7 * 60, 10000 + 6 * 60,     // MST/MDT
        10000 + 8 * 60, 10000 + 7 * 60      // PST/PDT
    };


                /**
     * 返回一个值，该值是从此 <code>Date</code> 对象表示的时间点（以本地时区解释）开始或包含的年份减去 1900 的结果。
     *
     * @return  由该日期表示的年份，减去 1900。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.YEAR) - 1900</code>。
     */
    @Deprecated
    public int getYear() {
        return normalize().getYear() - 1900;
    }

    /**
     * 将此 <tt>Date</tt> 对象的年份设置为指定值加上 1900。此 <code>Date</code> 对象被修改，以便它表示一个时间点，该时间点在指定的年份内，月份、日期、小时、分钟和秒与之前相同（以本地时区解释）。当然，如果日期是 2 月 29 日，例如，年份设置为非闰年，则新日期将被视为 3 月 1 日。
     *
     * @param   year    年份值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.YEAR, year + 1900)</code>。
     */
    @Deprecated
    public void setYear(int year) {
        getCalendarDate().setNormalizedYear(year + 1900);
    }

    /**
     * 返回一个表示此 <tt>Date</tt> 对象表示的时间点（以本地时区解释）开始或包含的月份的数字。返回的值介于 <code>0</code> 和 <code>11</code> 之间，其中 <code>0</code> 表示 1 月。
     *
     * @return  由该日期表示的月份。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.MONTH)</code>。
     */
    @Deprecated
    public int getMonth() {
        return normalize().getMonth() - 1; // 调整 1 基数到 0 基数
    }

    /**
     * 将此日期的月份设置为指定值。此 <tt>Date</tt> 对象被修改，以便它表示一个时间点，该时间点在指定的月份内，年份、日期、小时、分钟和秒与之前相同（以本地时区解释）。例如，如果日期是 10 月 31 日，月份设置为 6 月，则新日期将被视为 7 月 1 日，因为 6 月只有 30 天。
     *
     * @param   month   介于 0-11 之间的月份值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.MONTH, int month)</code>。
     */
    @Deprecated
    public void setMonth(int month) {
        int y = 0;
        if (month >= 12) {
            y = month / 12;
            month %= 12;
        } else if (month < 0) {
            y = CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        BaseCalendar.Date d = getCalendarDate();
        if (y != 0) {
            d.setNormalizedYear(d.getNormalizedYear() + y);
        }
        d.setMonth(month + 1); // 调整 0 基数到 1 基数的月份编号
    }

    /**
     * 返回此 <tt>Date</tt> 对象表示的月份中的日期。返回的值介于 <code>1</code> 和 <code>31</code> 之间，表示包含或开始于该 <tt>Date</tt> 对象表示的时间点的月份中的日期（以本地时区解释）。
     *
     * @return  由该日期表示的月份中的日期。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.DAY_OF_MONTH)</code>。
     * @deprecated
     */
    @Deprecated
    public int getDate() {
        return normalize().getDayOfMonth();
    }

    /**
     * 将此 <tt>Date</tt> 对象的月份中的日期设置为指定值。此 <tt>Date</tt> 对象被修改，以便它表示一个时间点，该时间点在指定的月份中的日期内，年份、月份、小时、分钟和秒与之前相同（以本地时区解释）。例如，如果日期是 4 月 30 日，日期设置为 31，则它将被视为 5 月 1 日，因为 4 月只有 30 天。
     *
     * @param   date   介于 1-31 之间的月份中的日期值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.DAY_OF_MONTH, int date)</code>。
     */
    @Deprecated
    public void setDate(int date) {
        getCalendarDate().setDayOfMonth(date);
    }

    /**
     * 返回此日期表示的星期几。返回的值（<tt>0</tt> = 星期日，<tt>1</tt> = 星期一，<tt>2</tt> = 星期二，<tt>3</tt> = 星期三，<tt>4</tt> = 星期四，<tt>5</tt> = 星期五，<tt>6</tt> = 星期六）表示包含或开始于该 <tt>Date</tt> 对象表示的时间点的星期几（以本地时区解释）。
     *
     * @return  由该日期表示的星期几。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.DAY_OF_WEEK)</code>。
     */
    @Deprecated
    public int getDay() {
        return normalize().getDayOfWeek() - BaseCalendar.SUNDAY;
    }

    /**
     * 返回此 <tt>Date</tt> 对象表示的小时。返回的值是一个数字（<tt>0</tt> 到 <tt>23</tt>），表示包含或开始于该 <tt>Date</tt> 对象表示的时间点的小时（以本地时区解释）。
     *
     * @return  由该日期表示的小时。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.HOUR_OF_DAY)</code>。
     */
    @Deprecated
    public int getHours() {
        return normalize().getHours();
    }

    /**
     * 将此 <tt>Date</tt> 对象的小时设置为指定值。此 <tt>Date</tt> 对象被修改，以便它表示一个时间点，该时间点在指定的小时内，年份、月份、日期、分钟和秒与之前相同（以本地时区解释）。
     *
     * @param   hours   小时值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.HOUR_OF_DAY, int hours)</code>。
     */
    @Deprecated
    public void setHours(int hours) {
        getCalendarDate().setHours(hours);
    }

    /**
     * 返回此日期表示的分钟数，以本地时区解释。返回的值介于 <code>0</code> 和 <code>59</code> 之间。
     *
     * @return  由该日期表示的分钟数。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.MINUTE)</code>。
     */
    @Deprecated
    public int getMinutes() {
        return normalize().getMinutes();
    }

    /**
     * 将此 <tt>Date</tt> 对象的分钟设置为指定值。此 <tt>Date</tt> 对象被修改，以便它表示一个时间点，该时间点在指定的分钟内，年份、月份、日期、小时和秒与之前相同（以本地时区解释）。
     *
     * @param   minutes   分钟值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.MINUTE, int minutes)</code>。
     */
    @Deprecated
    public void setMinutes(int minutes) {
        getCalendarDate().setMinutes(minutes);
    }

    /**
     * 返回此日期表示的分钟中的秒数。返回的值介于 <code>0</code> 和 <code>61</code> 之间。值 <code>60</code> 和 <code>61</code> 仅在那些考虑闰秒的 Java 虚拟机上出现。
     *
     * @return  由该日期表示的分钟中的秒数。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.get(Calendar.SECOND)</code>。
     */
    @Deprecated
    public int getSeconds() {
        return normalize().getSeconds();
    }

    /**
     * 将此 <tt>Date</tt> 的秒数设置为指定值。此 <tt>Date</tt> 对象被修改，以便它表示一个时间点，该时间点在指定的分钟中的秒内，年份、月份、日期、小时和分钟与之前相同（以本地时区解释）。
     *
     * @param   seconds   秒数值。
     * @see     java.util.Calendar
     * @deprecated 从 JDK 版本 1.1 开始，
     * 替换为 <code>Calendar.set(Calendar.SECOND, int seconds)</code>。
     */
    @Deprecated
    public void setSeconds(int seconds) {
        getCalendarDate().setSeconds(seconds);
    }

    /**
     * 返回自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数，由该 <tt>Date</tt> 对象表示。
     *
     * @return  自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数，由该日期表示。
     */
    public long getTime() {
        return getTimeImpl();
    }

    private final long getTimeImpl() {
        if (cdate != null && !cdate.isNormalized()) {
            normalize();
        }
        return fastTime;
    }

    /**
     * 将此 <code>Date</code> 对象设置为表示自 1970 年 1 月 1 日 00:00:00 GMT 以来的 <code>time</code> 毫秒后的时间点。
     *
     * @param   time   毫秒数。
     */
    public void setTime(long time) {
        fastTime = time;
        cdate = null;
    }

    /**
     * 测试此日期是否在指定日期之前。
     *
     * @param   when   一个日期。
     * @return  如果且仅当此 <tt>Date</tt> 对象表示的时间点严格早于 <tt>when</tt> 表示的时间点，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @exception NullPointerException 如果 <code>when</code> 为 null。
     */
    public boolean before(Date when) {
        return getMillisOf(this) < getMillisOf(when);
    }

    /**
     * 测试此日期是否在指定日期之后。
     *
     * @param   when   一个日期。
     * @return  如果且仅当此 <tt>Date</tt> 对象表示的时间点严格晚于 <tt>when</tt> 表示的时间点，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @exception NullPointerException 如果 <code>when</code> 为 null。
     */
    public boolean after(Date when) {
        return getMillisOf(this) > getMillisOf(when);
    }

    /**
     * 比较两个日期是否相等。
     * 结果为 <code>true</code> 当且仅当参数不是 <code>null</code> 并且是一个 <code>Date</code> 对象，表示与该对象相同的时间点，精确到毫秒。
     * <p>
     * 因此，两个 <code>Date</code> 对象相等当且仅当 <code>getTime</code> 方法返回的 <code>long</code> 值相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see     java.util.Date#getTime()
     */
    public boolean equals(Object obj) {
        return obj instanceof Date && getTime() == ((Date) obj).getTime();
    }

    /**
     * 返回此 <code>Date</code> 对象的毫秒值，而不影响其内部状态。
     */
    static final long getMillisOf(Date date) {
        if (date.cdate == null || date.cdate.isNormalized()) {
            return date.fastTime;
        }
        BaseCalendar.Date d = (BaseCalendar.Date) date.cdate.clone();
        return gcal.getTime(d);
    }

    /**
     * 比较两个日期的顺序。
     *
     * @param   anotherDate   要比较的 <code>Date</code>。
     * @return  如果此日期等于 <code>anotherDate</code>，则返回 <code>0</code>；如果此日期在 <code>anotherDate</code> 之前，则返回小于 <code>0</code> 的值；如果此日期在 <code>anotherDate</code> 之后，则返回大于 <code>0</code> 的值。
     * @since   1.2
     * @exception NullPointerException 如果 <code>anotherDate</code> 为 null。
     */
    public int compareTo(Date anotherDate) {
        long thisTime = getMillisOf(this);
        long anotherTime = getMillisOf(anotherDate);
        return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
    }

    /**
     * 返回此对象的哈希码值。结果是 <tt>long</tt> 值的两个部分的异或。也就是说，哈希码是以下表达式的值：
     * <blockquote><pre>{@code
     * (int)(this.getTime()^(this.getTime() >>> 32))
     * }</pre></blockquote>
     *
     * @return  此对象的哈希码值。
     */
    public int hashCode() {
        long ht = this.getTime();
        return (int) ht ^ (int) (ht >> 32);
    }

    /**
     * 将此 <code>Date</code> 对象转换为以下形式的 <code>String</code>：
     * <blockquote><pre>
     * dow mon dd hh:mm:ss zzz yyyy</pre></blockquote>
     * 其中：<ul>
     * <li><tt>dow</tt> 是星期几（<tt>Sun, Mon, Tue, Wed, Thu, Fri, Sat</tt>）。
     * <li><tt>mon</tt> 是月份（<tt>Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec</tt>）。
     * <li><tt>dd</tt> 是月份中的日期（<tt>01</tt> 到 <tt>31</tt>），以两位十进制数字表示。
     * <li><tt>hh</tt> 是一天中的小时（<tt>00</tt> 到 <tt>23</tt>），以两位十进制数字表示。
     * <li><tt>mm</tt> 是小时中的分钟（<tt>00</tt> 到 <tt>59</tt>），以两位十进制数字表示。
     * <li><tt>ss</tt> 是分钟中的秒（<tt>00</tt> 到 <tt>61</tt>），以两位十进制数字表示。
     * <li><tt>zzz</tt> 是时区（可能反映夏令时）。标准时区缩写包括 <tt>parse</tt> 方法识别的那些。如果时区信息不可用，则 <tt>zzz</tt> 为空——即没有字符。
     * <li><tt>yyyy</tt> 是年份，以四位十进制数字表示。
     * </ul>
     *
     * @return  该日期的字符串表示。
     * @see     java.util.Date#toLocaleString()
     * @see     java.util.Date#toGMTString()
     */
    public String toString() {
        // "EEE MMM dd HH:mm:ss zzz yyyy";
        BaseCalendar.Date date = normalize();
        StringBuilder sb = new StringBuilder(28);
        int index = date.getDayOfWeek();
        if (index == BaseCalendar.SUNDAY) {
            index = 8;
        }
        convertToAbbr(sb, wtb[index]).append(' ');                        // EEE
        convertToAbbr(sb, wtb[date.getMonth() - 1 + 2 + 7]).append(' ');  // MMM
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 2).append(' '); // dd


                    CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');   // HH
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':'); // mm
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2).append(' '); // ss
        TimeZone zi = date.getZone();
        if (zi != null) {
            sb.append(zi.getDisplayName(date.isDaylightTime(), TimeZone.SHORT, Locale.US)); // zzz
        } else {
            sb.append("GMT");
        }
        sb.append(' ').append(date.getYear());  // yyyy
        return sb.toString();
    }

    /**
     * 将给定的名称转换为其3字母缩写（例如，
     * "monday" -> "Mon"）并将其存储在给定的
     * <code>StringBuilder</code> 中。
     */
    private static final StringBuilder convertToAbbr(StringBuilder sb, String name) {
        sb.append(Character.toUpperCase(name.charAt(0)));
        sb.append(name.charAt(1)).append(name.charAt(2));
        return sb;
    }

    /**
     * 创建此 <tt>Date</tt> 对象的字符串表示形式，形式取决于实现。目的是使形式对 Java 应用程序的用户来说熟悉，无论它可能在何处运行。意图类似于 ISO C 的 <code>strftime()</code> 函数支持的 "<code>%c</code>" 格式。
     *
     * @return 使用本地约定的此日期的字符串表示形式。
     * @see     java.text.DateFormat
     * @see     java.util.Date#toString()
     * @see     java.util.Date#toGMTString()
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>DateFormat.format(Date date)</code>。
     */
    @Deprecated
    public String toLocaleString() {
        DateFormat formatter = DateFormat.getDateTimeInstance();
        return formatter.format(this);
    }

    /**
     * 创建此 <tt>Date</tt> 对象的字符串表示形式，形式为：
     * <blockquote><pre>
     * d mon yyyy hh:mm:ss GMT</pre></blockquote>
     * 其中：<ul>
     * <li><i>d</i> 是月份中的日期（<tt>1</tt> 到 <tt>31</tt>），
     *     用一到两位十进制数字表示。
     * <li><i>mon</i> 是月份（<tt>Jan, Feb, Mar, Apr, May, Jun, Jul,
     *     Aug, Sep, Oct, Nov, Dec</tt>）。
     * <li><i>yyyy</i> 是年份，用四位十进制数字表示。
     * <li><i>hh</i> 是一天中的小时（<tt>00</tt> 到 <tt>23</tt>），
     *     用两位十进制数字表示。
     * <li><i>mm</i> 是小时内的分钟（<tt>00</tt> 到 <tt>59</tt>），
     *     用两位十进制数字表示。
     * <li><i>ss</i> 是分钟内的秒（<tt>00</tt> 到 <tt>61</tt>），
     *     用两位十进制数字表示。
     * <li><i>GMT</i> 是精确的 ASCII 字母 "<tt>GMT</tt>"，表示格林尼治标准时间。
     * </ul><p>
     * 结果不依赖于本地时区。
     *
     * @return 使用 Internet GMT 约定的此日期的字符串表示形式。
     * @see     java.text.DateFormat
     * @see     java.util.Date#toString()
     * @see     java.util.Date#toLocaleString()
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>DateFormat.format(Date date)</code>，使用 GMT <code>TimeZone</code>。
     */
    @Deprecated
    public String toGMTString() {
        // d MMM yyyy HH:mm:ss 'GMT'
        long t = getTime();
        BaseCalendar cal = getCalendarSystem(t);
        BaseCalendar.Date date =
            (BaseCalendar.Date) cal.getCalendarDate(getTime(), (TimeZone)null);
        StringBuilder sb = new StringBuilder(32);
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 1).append(' '); // d
        convertToAbbr(sb, wtb[date.getMonth() - 1 + 2 + 7]).append(' ');  // MMM
        sb.append(date.getYear()).append(' ');                            // yyyy
        CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');      // HH
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':');    // mm
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2);                // ss
        sb.append(" GMT");                                                // ' GMT'
        return sb.toString();
    }

    /**
     * 返回适用于此 <code>Date</code> 对象所表示的时间的本地时区相对于 UTC 的偏移量，以分钟为单位。
     * <p>
     * 例如，在马萨诸塞州，格林尼治以西五个时区：
     * <blockquote><pre>
     * new Date(96, 1, 14).getTimezoneOffset() 返回 300</pre></blockquote>
     * 因为在 1996 年 2 月 14 日，使用标准时间（东部标准时间），比 UTC 晚五小时；但：
     * <blockquote><pre>
     * new Date(96, 5, 1).getTimezoneOffset() 返回 240</pre></blockquote>
     * 因为在 1996 年 6 月 1 日，使用夏令时（东部夏令时），比 UTC 晚四小时。<p>
     * 此方法产生的结果与以下计算相同：
     * <blockquote><pre>
     * (this.getTime() - UTC(this.getYear(),
     *                       this.getMonth(),
     *                       this.getDate(),
     *                       this.getHours(),
     *                       this.getMinutes(),
     *                       this.getSeconds())) / (60 * 1000)
     * </pre></blockquote>
     *
     * @return 当前时区的时间偏移量，以分钟为单位。
     * @see     java.util.Calendar#ZONE_OFFSET
     * @see     java.util.Calendar#DST_OFFSET
     * @see     java.util.TimeZone#getDefault
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>-(Calendar.get(Calendar.ZONE_OFFSET) +
     * Calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)</code>。
     */
    @Deprecated
    public int getTimezoneOffset() {
        int zoneOffset;
        if (cdate == null) {
            TimeZone tz = TimeZone.getDefaultRef();
            if (tz instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo)tz).getOffsets(fastTime, null);
            } else {
                zoneOffset = tz.getOffset(fastTime);
            }
        } else {
            normalize();
            zoneOffset = cdate.getZoneOffset();
        }
        return -zoneOffset/60000;  // 转换为分钟
    }

    private final BaseCalendar.Date getCalendarDate() {
        if (cdate == null) {
            BaseCalendar cal = getCalendarSystem(fastTime);
            cdate = (BaseCalendar.Date) cal.getCalendarDate(fastTime,
                                                            TimeZone.getDefaultRef());
        }
        return cdate;
    }

    private final BaseCalendar.Date normalize() {
        if (cdate == null) {
            BaseCalendar cal = getCalendarSystem(fastTime);
            cdate = (BaseCalendar.Date) cal.getCalendarDate(fastTime,
                                                            TimeZone.getDefaultRef());
            return cdate;
        }

        // 使用 cdate 中的时区首先对 cdate 进行规范化。这是为了兼容行为。
        if (!cdate.isNormalized()) {
            cdate = normalize(cdate);
        }

        // 如果默认时区已更改，则使用新的时区重新计算字段。
        TimeZone tz = TimeZone.getDefaultRef();
        if (tz != cdate.getZone()) {
            cdate.setZone(tz);
            CalendarSystem cal = getCalendarSystem(cdate);
            cal.getCalendarDate(fastTime, cdate);
        }
        return cdate;
    }

    // fastTime 和返回的数据在返回时同步。
    private final BaseCalendar.Date normalize(BaseCalendar.Date date) {
        int y = date.getNormalizedYear();
        int m = date.getMonth();
        int d = date.getDayOfMonth();
        int hh = date.getHours();
        int mm = date.getMinutes();
        int ss = date.getSeconds();
        int ms = date.getMillis();
        TimeZone tz = date.getZone();

        // 如果指定的年份不能用毫秒值处理，使用 GregorianCalendar 进行完全兼容的处理。这是某些 JCK 测试的要求。限制基于最大年份值 - 可以用 d、hh、mm、ss 和 ms 的最大值表示的年份。此外，让 GregorianCalendar 处理默认的转换年份，这样我们就不需要在这里担心转换。
        if (y == 1582 || y > 280000000 || y < -280000000) {
            if (tz == null) {
                tz = TimeZone.getTimeZone("GMT");
            }
            GregorianCalendar gc = new GregorianCalendar(tz);
            gc.clear();
            gc.set(GregorianCalendar.MILLISECOND, ms);
            gc.set(y, m-1, d, hh, mm, ss);
            fastTime = gc.getTimeInMillis();
            BaseCalendar cal = getCalendarSystem(fastTime);
            date = (BaseCalendar.Date) cal.getCalendarDate(fastTime, tz);
            return date;
        }

        BaseCalendar cal = getCalendarSystem(y);
        if (cal != getCalendarSystem(date)) {
            date = (BaseCalendar.Date) cal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
        }
        // 执行 GregorianCalendar 风格的规范化。
        fastTime = cal.getTime(date);

        // 如果规范化后的日期需要其他日历系统，我们需要使用另一个日历系统重新计算。
        BaseCalendar ncal = getCalendarSystem(fastTime);
        if (ncal != cal) {
            date = (BaseCalendar.Date) ncal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
            fastTime = ncal.getTime(date);
        }
        return date;
    }

    /**
     * 返回给定日期使用的格里高利或儒略日历系统。从 1582 年 10 月 15 日开始使用格里高利日历。
     *
     * @param year 规范化的日历年（不是 -1900）
     * @return 用于指定日期的日历系统
     */
    private static final BaseCalendar getCalendarSystem(int year) {
        if (year >= 1582) {
            return gcal;
        }
        return getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(long utc) {
        // 快速检查 `utc` 给定的时间戳是否为纪元或更晚。如果它在 1970 年之前，我们将转换为本地时间进行比较。
        if (utc >= 0
            || utc >= GregorianCalendar.DEFAULT_GREGORIAN_CUTOVER
                        - TimeZone.getDefaultRef().getOffset(utc)) {
            return gcal;
        }
        return getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(BaseCalendar.Date cdate) {
        if (jcal == null) {
            return gcal;
        }
        if (cdate.getEra() != null) {
            return jcal;
        }
        return gcal;
    }

    synchronized private static final BaseCalendar getJulianCalendar() {
        if (jcal == null) {
            jcal = (BaseCalendar) CalendarSystem.forName("julian");
        }
        return jcal;
    }

    /**
     * 将此对象的状态保存到流中（即，序列化它）。
     *
     * @serialData 由 <code>getTime()</code> 返回的值（long）。这表示从 1970 年 1 月 1 日 00:00:00 GMT 开始的毫秒偏移量。
     */
    private void writeObject(ObjectOutputStream s)
         throws IOException
    {
        s.writeLong(getTimeImpl());
    }

    /**
     * 从流中恢复此对象（即，反序列化它）。
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        fastTime = s.readLong();
    }

    /**
     * 从 {@code Instant} 对象获取 {@code Date} 实例。
     * <p>
     * {@code Instant} 使用纳秒精度，而 {@code Date} 使用毫秒精度。转换将截断任何多余的精度信息，就像纳秒量被一千万整除一样。
     * <p>
     * {@code Instant} 可以存储比 {@code Date} 更远的未来和更远的过去的时间点。在这种情况下，此方法将抛出异常。
     *
     * @param instant 要转换的瞬间
     * @return 一个表示与提供的瞬间相同时间点的 {@code Date}
     * @exception NullPointerException 如果 {@code instant} 为 null。
     * @exception IllegalArgumentException 如果瞬间太大而无法表示为 {@code Date}
     * @since 1.8
     */
    public static Date from(Instant instant) {
        try {
            return new Date(instant.toEpochMilli());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 将此 {@code Date} 对象转换为 {@code Instant}。
     * <p>
     * 转换创建一个表示与此 {@code Date} 相同时间点的 {@code Instant}。
     *
     * @return 一个表示与此 {@code Date} 对象相同时间点的瞬间
     * @since 1.8
     */
    public Instant toInstant() {
        return Instant.ofEpochMilli(getTime());
    }
}
