
/*
 * Copyright (c) 2005, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.LocalGregorianCalendar;
import sun.util.calendar.ZoneInfo;

/**
 * {@code JapaneseImperialCalendar} 实现了一个日本历法系统，该系统支持从明治时代开始的皇纪年号。以下是该历法系统支持的年号。
 * <pre>{@code
 * ERA 值   年号名称    自（格里高利历）
 * ------------------------------------------------------
 *     0       N/A         N/A
 *     1       明治       1868-01-01T00:00:00 本地时间
 *     2       大正       1912-07-30T00:00:00 本地时间
 *     3       昭和       1926-12-25T00:00:00 本地时间
 *     4       平成       1989-01-08T00:00:00 本地时间
 *     5       令和       2019-05-01T00:00:00 本地时间
 * ------------------------------------------------------
 * }</pre>
 *
 * <p><code>ERA</code> 值 0 指定明治之前的年份，使用格里高利历年值。与 {@link
 * GregorianCalendar} 不同，不支持从儒略历到格里高利历的转换，因为这对明治之前的日本历法系统没有意义。为了表示格里高利历元年之前的年份，使用 0 和负值。日本皇室敕令和政府法令没有规定如何处理年号转换的时间差异。此历法实现假设所有转换均为本地时间。
 *
 * @author Masayoshi Okutsu
 * @since 1.6
 */
class JapaneseImperialCalendar extends Calendar {
    /*
     * 实现说明
     *
     * 此实现使用
     * sun.util.calendar.LocalGregorianCalendar 来执行大多数历法计算。LocalGregorianCalendar 是可配置的，并在启动时读取 <JRE_HOME>/lib/calendars.properties。
     */

    /**
     * 指定明治之前的 ERA 常量。
     */
    public static final int BEFORE_MEIJI = 0;

    /**
     * 指定明治 ERA 的常量。
     */
    public static final int MEIJI = 1;

    /**
     * 指定大正 ERA 的常量。
     */
    public static final int TAISHO = 2;

    /**
     * 指定昭和 ERA 的常量。
     */
    public static final int SHOWA = 3;

    /**
     * 指定平成 ERA 的常量。
     */
    public static final int HEISEI = 4;

    /**
     * 指定令和 ERA 的常量。
     */
    private static final int REIWA = 5;

    private static final int EPOCH_OFFSET   = 719163; // 1970年1月1日（格里高利历）的固定日期

    // 有用的毫秒常量。虽然 ONE_DAY 和 ONE_WEEK 可以放入 int 中，但为了防止算术溢出（bug 4173516），它们必须是 long。
    private static final int  ONE_SECOND = 1000;
    private static final int  ONE_MINUTE = 60 * ONE_SECOND;
    private static final int  ONE_HOUR   = 60 * ONE_MINUTE;
    private static final long ONE_DAY    = 24 * ONE_HOUR;
    private static final long ONE_WEEK   = 7 * ONE_DAY;

    // 引用 sun.util.calendar.LocalGregorianCalendar 实例（单例）。
    private static final LocalGregorianCalendar jcal
        = (LocalGregorianCalendar) CalendarSystem.forName("japanese");

    // 格里高利历实例。这是必需的，因为年号转换日期以格里高利历日期给出。
    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();

    // 代表“明治之前”的 Era 实例。
    private static final Era BEFORE_MEIJI_ERA = new Era("BeforeMeiji", "BM", Long.MIN_VALUE, false);

    // 皇纪年号。sun.util.calendar.LocalGregorianCalendar 没有代表明治之前的 Era，这在 Calendar 中不方便。因此，era[0] 是 BEFORE_MEIJI_ERA 的引用。
    private static final Era[] eras;

    // 每个年号的起始固定日期。
    private static final long[] sinceFixedDates;

    // 当前年号
    private static final int currentEra;

    /*
     * <pre>
     *                                 最小值       最小最大值     最大值     最大最大值
     * 字段名称             最小值       最小最小值     最大最小值     最大最大值
     * ----------             -------   -------     -------     -------
     * ERA                          0         0           1           1
     * YEAR                -292275055         1           ?           ?
     * MONTH                        0         0          11          11
     * WEEK_OF_YEAR                 1         1          52*         53
     * WEEK_OF_MONTH                0         0           4*          6
     * DAY_OF_MONTH                 1         1          28*         31
     * DAY_OF_YEAR                  1         1         365*        366
     * DAY_OF_WEEK                  1         1           7           7
     * DAY_OF_WEEK_IN_MONTH        -1        -1           4*          6
     * AM_PM                        0         0           1           1
     * HOUR                         0         0          11          11
     * HOUR_OF_DAY                  0         0          23          23
     * MINUTE                       0         0          59          59
     * SECOND                       0         0          59          59
     * MILLISECOND                  0         0         999         999
     * ZONE_OFFSET             -13:00    -13:00       14:00       14:00
     * DST_OFFSET                0:00      0:00        0:20        2:00
     * </pre>
     * *: 取决于年号
     */
    static final int MIN_VALUES[] = {
        0,              // ERA
        -292275055,     // YEAR
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
        -13 * ONE_HOUR,   // ZONE_OFFSET (UNIX 兼容性)
        0               // DST_OFFSET
    };
    static final int LEAST_MAX_VALUES[] = {
        0,              // ERA (稍后初始化)
        0,              // YEAR (稍后初始化)
        JANUARY,        // MONTH (昭和 64 结束于1月)
        0,              // WEEK_OF_YEAR (昭和 1 只有6天，可能是0周)
        4,              // WEEK_OF_MONTH
        28,             // DAY_OF_MONTH
        0,              // DAY_OF_YEAR (稍后初始化)
        SATURDAY,       // DAY_OF_WEEK
        4,              // DAY_OF_WEEK_IN
        PM,             // AM_PM
        11,             // HOUR
        23,             // HOUR_OF_DAY
        59,             // MINUTE
        59,             // SECOND
        999,            // MILLISECOND
        14 * ONE_HOUR,    // ZONE_OFFSET
        20 * ONE_MINUTE   // DST_OFFSET (历史最小最大值)
    };
    static final int MAX_VALUES[] = {
        0,              // ERA
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
        14 * ONE_HOUR,    // ZONE_OFFSET
        2 * ONE_HOUR      // DST_OFFSET (双夏令时)
    };

    // 声明与 JDK 1.6 的序列化兼容性
    private static final long serialVersionUID = -3364572813905467929L;

    static {
        Era[] es = jcal.getEras();
        int length = es.length + 1;
        eras = new Era[length];
        sinceFixedDates = new long[length];

        // eras[BEFORE_MEIJI] 和 sinceFixedDate[BEFORE_MEIJI] 与 Gregorian 相同。
        int index = BEFORE_MEIJI;
        int current = index;
        sinceFixedDates[index] = gcal.getFixedDate(BEFORE_MEIJI_ERA.getSinceDate());
        eras[index++] = BEFORE_MEIJI_ERA;
        for (Era e : es) {
            if (e.getSince(TimeZone.NO_TIMEZONE) < System.currentTimeMillis()) {
                current = index;
            }
            CalendarDate d = e.getSinceDate();
            sinceFixedDates[index] = gcal.getFixedDate(d);
            eras[index++] = e;
        }
        currentEra = current;

        LEAST_MAX_VALUES[ERA] = MAX_VALUES[ERA] = eras.length - 1;

        // 计算最小最大年份和最小最大日数。以下代码假设在格里高利历年中最多有一个年号转换。
        int year = Integer.MAX_VALUE;
        int dayOfYear = Integer.MAX_VALUE;
        CalendarDate date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        for (int i = 1; i < eras.length; i++) {
            long fd = sinceFixedDates[i];
            CalendarDate transitionDate = eras[i].getSinceDate();
            date.setDate(transitionDate.getYear(), BaseCalendar.JANUARY, 1);
            long fdd = gcal.getFixedDate(date);
            if (fd != fdd) {
                dayOfYear = Math.min((int)(fd - fdd) + 1, dayOfYear);
            }
            date.setDate(transitionDate.getYear(), BaseCalendar.DECEMBER, 31);
            fdd = gcal.getFixedDate(date);
            if (fd != fdd) {
                dayOfYear = Math.min((int)(fdd - fd) + 1, dayOfYear);
            }
            LocalGregorianCalendar.Date lgd = getCalendarDate(fd - 1);
            int y = lgd.getYear();
            // 除非第一年从1月1日开始，否则实际最大值可能少一年。例如，如果它是昭和 63 年 1 月 8 日，63 是实际最大值，因为昭和 64 年 1 月 8 日不存在。
            if (!(lgd.getMonth() == BaseCalendar.JANUARY && lgd.getDayOfMonth() == 1)) {
                y--;
            }
            year = Math.min(y, year);
        }
        LEAST_MAX_VALUES[YEAR] = year; // 最大年份可能小于此值。
        LEAST_MAX_VALUES[DAY_OF_YEAR] = dayOfYear;
    }

    /**
     * jdate 始终有一个 sun.util.calendar.LocalGregorianCalendar.Date 实例，以避免每次计算时创建它的开销。
     */
    private transient LocalGregorianCalendar.Date jdate;

    /**
     * 临时 int[2] 用于获取时区偏移。zoneOffsets[0] 获取 GMT 偏移值，zoneOffsets[1] 获取夏令时值。
     */
    private transient int[] zoneOffsets;

    /**
     * 临时存储，用于在非宽松模式下保存原始 fields[] 值。
     */
    private transient int[] originalFields;

    /**
     * 构造一个基于给定时区和给定区域设置的当前时间的 <code>JapaneseImperialCalendar</code>。
     *
     * @param zone 给定的时区。
     * @param aLocale 给定的区域设置。
     */
    JapaneseImperialCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        jdate = jcal.newCalendarDate(zone);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * 构造一个“空”的 {@code JapaneseImperialCalendar}。
     *
     * @param zone    给定的时区
     * @param aLocale 给定的区域设置
     * @param flag    请求空实例的标志
     */
    JapaneseImperialCalendar(TimeZone zone, Locale aLocale, boolean flag) {
        super(zone, aLocale);
        jdate = jcal.newCalendarDate(zone);
    }

    /**
     * 返回 {@code "japanese"} 作为此 {@code JapaneseImperialCalendar} 的历法类型。
     *
     * @return {@code "japanese"}
     */
    @Override
    public String getCalendarType() {
        return "japanese";
    }

    /**
     * 将此 <code>JapaneseImperialCalendar</code> 与指定的 <code>Object</code> 进行比较。如果且仅当参数是一个 <code>JapaneseImperialCalendar</code> 对象，并且表示相同的时间值（从 <a href="Calendar.html#Epoch">纪元</a> 开始的毫秒偏移）以及相同的 <code>Calendar</code> 参数时，结果为 <code>true</code>。
     *
     * @param obj 要比较的对象。
     * @return 如果此对象等于 <code>obj</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see Calendar#compareTo(Calendar)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof JapaneseImperialCalendar &&
            super.equals(obj);
    }

    /**
     * 生成此 <code>JapaneseImperialCalendar</code> 对象的哈希码。
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ jdate.hashCode();
    }

    /**
     * 根据历法的规则，将指定的（带符号的）时间量添加到给定的历法字段。
     *
     * <p><em>添加规则 1</em>. 调用后 <code>field</code> 的值减去调用前 <code>field</code> 的值等于 <code>amount</code>，模以 <code>field</code> 中发生的任何溢出。当字段值超出其范围时，会发生溢出，结果是较大的字段被递增或递减，字段值被调整回其范围内。</p>
     *
     * <p><em>添加规则 2</em>. 如果较小的字段预期不变，但由于 <code>field</code> 改变后其最小值或最大值发生变化，使其无法等于其先前值，则其值将调整为尽可能接近其预期值。较小的字段表示较小的时间单位。例如，<code>HOUR</code> 是比 <code>DAY_OF_MONTH</code> 更小的字段。对于不预期不变的较小字段，不会进行调整。历法系统确定哪些字段预期不变。</p>
     *
     * @param field 历法字段。
     * @param amount 要添加到字段的日期或时间量。
     * @exception IllegalArgumentException 如果 <code>field</code> 是 <code>ZONE_OFFSET</code>、<code>DST_OFFSET</code> 或未知，或者在非宽松模式下任何历法字段的值超出范围。
     */
    @Override
    public void add(int field, int amount) {
        // 如果 amount == 0，即使给定字段超出范围也不执行任何操作。这是 JCK 测试的。
        if (amount == 0) {
            return;   // 不执行任何操作！
        }


                    if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        // 同步时间和日历字段。
        complete();

        if (field == YEAR) {
            LocalGregorianCalendar.Date d = (LocalGregorianCalendar.Date) jdate.clone();
            d.addYear(amount);
            pinDayOfMonth(d);
            set(ERA, getEraIndex(d));
            set(YEAR, d.getYear());
            set(MONTH, d.getMonth() - 1);
            set(DAY_OF_MONTH, d.getDayOfMonth());
        } else if (field == MONTH) {
            LocalGregorianCalendar.Date d = (LocalGregorianCalendar.Date) jdate.clone();
            d.addMonth(amount);
            pinDayOfMonth(d);
            set(ERA, getEraIndex(d));
            set(YEAR, d.getYear());
            set(MONTH, d.getMonth() - 1);
            set(DAY_OF_MONTH, d.getDayOfMonth());
        } else if (field == ERA) {
            int era = internalGet(ERA) + amount;
            if (era < 0) {
                era = 0;
            } else if (era > eras.length - 1) {
                era = eras.length - 1;
            }
            set(ERA, era);
        } else {
            long delta = amount;
            long timeOfDay = 0;
            switch (field) {
            // 处理时间字段。将给定的量转换为毫秒并调用 setTimeInMillis。
            case HOUR:
            case HOUR_OF_DAY:
                delta *= 60 * 60 * 1000;        // 小时转换为毫秒
                break;

            case MINUTE:
                delta *= 60 * 1000;             // 分钟转换为毫秒
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

            case DAY_OF_MONTH: // 日期的同义词
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

            // 其余字段（周、日或 AM_PM 字段）需要时区偏移（包括 GMT 和夏令时）变化调整。

            // 将当前时间转换为固定的日期和一天中的时间。
            long fd = cachedFixedDate;
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
            // 如果时区偏移已改变，则调整差异。
            if (zoneOffset != 0) {
                setTimeInMillis(time + zoneOffset);
                long fd2 = cachedFixedDate;
                // 如果调整已改变日期，则取前一个日期。
                if (fd2 != fd) {
                    setTimeInMillis(time - zoneOffset);
                }
            }
        }
    }

    @Override
    public void roll(int field, boolean up) {
        roll(field, up ? +1 : -1);
    }

    /**
     * 向指定的日历字段添加一个带符号的量，而不改变更大的字段。
     * 负的滚动量表示从字段中减去而不改变更大的字段。如果指定的量为 0，此方法不执行任何操作。
     *
     * <p>此方法在添加量之前调用 {@link #complete()} 以使所有日历字段规范化。如果在非宽松模式下有
     * 任何日历字段具有超出范围的值，则抛出 <code>IllegalArgumentException</code>。
     *
     * @param field 日历字段。
     * @param amount 要添加到 <code>field</code> 的带符号的量。
     * @exception IllegalArgumentException 如果 <code>field</code> 是
     * <code>ZONE_OFFSET</code>、<code>DST_OFFSET</code> 或未知，或者在非宽松模式下任何日历字段具有超出范围的值。
     * @see #roll(int,boolean)
     * @see #add(int,int)
     * @see #set(int,int)
     */
    @Override
    public void roll(int field, int amount) {
        // 如果 amount == 0，即使给定的字段超出范围也不执行任何操作。这是由 JCK 测试的。
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
        case ERA:
        case AM_PM:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
            // 这些字段处理简单，因为它们有固定的最小值和最大值。其他字段较为复杂，因为它们必须滚动的范围
            // 取决于日期、时区和纪元转换。
            break;

        case HOUR:
        case HOUR_OF_DAY:
            {
                int unit = max + 1; // 12 或 24 小时
                int h = internalGet(field);
                int nh = (h + amount) % unit;
                if (nh < 0) {
                    nh += unit;
                }
                time += ONE_HOUR * (nh - h);

                // 日期可能已改变，这可能会发生在夏令时转换将其带到下一天的情况，尽管这种情况非常罕见。但我们必须确保不改变更大的字段。
                CalendarDate d = jcal.getCalendarDate(time, getZone());
                if (internalGet(DAY_OF_MONTH) != d.getDayOfMonth()) {
                    d.setEra(jdate.getEra());
                    d.setDate(internalGet(YEAR),
                              internalGet(MONTH) + 1,
                              internalGet(DAY_OF_MONTH));
                    if (field == HOUR) {
                        assert (internalGet(AM_PM) == PM);
                        d.addHours(+12); // 恢复 PM
                    }
                    time = jcal.getTime(d);
                }
                int hourOfDay = d.getHours();
                internalSet(field, hourOfDay % unit);
                if (field == HOUR) {
                    internalSet(HOUR_OF_DAY, hourOfDay);
                } else {
                    internalSet(AM_PM, hourOfDay / 12);
                    internalSet(HOUR, hourOfDay % 12);
                }

                // 时区偏移和/或夏令时可能已改变。
                int zoneOffset = d.getZoneOffset();
                int saving = d.getDaylightSaving();
                internalSet(ZONE_OFFSET, zoneOffset - saving);
                internalSet(DST_OFFSET, saving);
                return;
            }

        case YEAR:
            min = getActualMinimum(field);
            max = getActualMaximum(field);
            break;

        case MONTH:
            // 滚动月份涉及将最终值固定在 [0, 11] 范围内，并在必要时调整 DAY_OF_MONTH。只有在更新 MONTH 字段后，DAY_OF_MONTH 非法时才进行调整。
            // 例如，<jan31>.roll(MONTH, 1) -> <feb28> 或 <feb29>。
            {
                if (!isTransitionYear(jdate.getNormalizedYear())) {
                    int year = jdate.getYear();
                    if (year == getMaximum(YEAR)) {
                        CalendarDate jd = jcal.getCalendarDate(time, getZone());
                        CalendarDate d = jcal.getCalendarDate(Long.MAX_VALUE, getZone());
                        max = d.getMonth() - 1;
                        int n = getRolledValue(internalGet(field), amount, min, max);
                        if (n == max) {
                            // 为了避免溢出，使用等效的年份。
                            jd.addYear(-400);
                            jd.setMonth(n + 1);
                            if (jd.getDayOfMonth() > d.getDayOfMonth()) {
                                jd.setDayOfMonth(d.getDayOfMonth());
                                jcal.normalize(jd);
                            }
                            if (jd.getDayOfMonth() == d.getDayOfMonth()
                                && jd.getTimeOfDay() > d.getTimeOfDay()) {
                                jd.setMonth(n + 1);
                                jd.setDayOfMonth(d.getDayOfMonth() - 1);
                                jcal.normalize(jd);
                                // 月份可能因规范化而改变。
                                n = jd.getMonth() - 1;
                            }
                            set(DAY_OF_MONTH, jd.getDayOfMonth());
                        }
                        set(MONTH, n);
                    } else if (year == getMinimum(YEAR)) {
                        CalendarDate jd = jcal.getCalendarDate(time, getZone());
                        CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        min = d.getMonth() - 1;
                        int n = getRolledValue(internalGet(field), amount, min, max);
                        if (n == min) {
                            // 为了避免下溢，使用等效的年份。
                            jd.addYear(+400);
                            jd.setMonth(n + 1);
                            if (jd.getDayOfMonth() < d.getDayOfMonth()) {
                                jd.setDayOfMonth(d.getDayOfMonth());
                                jcal.normalize(jd);
                            }
                            if (jd.getDayOfMonth() == d.getDayOfMonth()
                                && jd.getTimeOfDay() < d.getTimeOfDay()) {
                                jd.setMonth(n + 1);
                                jd.setDayOfMonth(d.getDayOfMonth() + 1);
                                jcal.normalize(jd);
                                // 月份可能因规范化而改变。
                                n = jd.getMonth() - 1;
                            }
                            set(DAY_OF_MONTH, jd.getDayOfMonth());
                        }
                        set(MONTH, n);
                    } else {
                        int mon = (internalGet(MONTH) + amount) % 12;
                        if (mon < 0) {
                            mon += 12;
                        }
                        set(MONTH, mon);

                        // 保持月份中的日期在范围内。我们不希望溢出到下一个月；例如，我们不希望 jan31 + 1 mo -> feb31 -> mar3。
                        int monthLen = monthLength(mon);
                        if (internalGet(DAY_OF_MONTH) > monthLen) {
                            set(DAY_OF_MONTH, monthLen);
                        }
                    }
                } else {
                    int eraIndex = getEraIndex(jdate);
                    CalendarDate transition = null;
                    if (jdate.getYear() == 1) {
                        transition = eras[eraIndex].getSinceDate();
                        min = transition.getMonth() - 1;
                    } else {
                        if (eraIndex < eras.length - 1) {
                            transition = eras[eraIndex + 1].getSinceDate();
                            if (transition.getYear() == jdate.getNormalizedYear()) {
                                max = transition.getMonth() - 1;
                                if (transition.getDayOfMonth() == 1) {
                                    max--;
                                }
                            }
                        }
                    }

                    if (min == max) {
                        // 该年只有一个月份。无需进一步处理。（例如，昭和元年（年 1）和最后一年只有一个月份。）
                        return;
                    }
                    int n = getRolledValue(internalGet(field), amount, min, max);
                    set(MONTH, n);
                    if (n == min) {
                        if (!(transition.getMonth() == BaseCalendar.JANUARY
                              && transition.getDayOfMonth() == 1)) {
                            if (jdate.getDayOfMonth() < transition.getDayOfMonth()) {
                                set(DAY_OF_MONTH, transition.getDayOfMonth());
                            }
                        }
                    } else if (n == max && (transition.getMonth() - 1 == n)) {
                        int dom = transition.getDayOfMonth();
                        if (jdate.getDayOfMonth() >= dom) {
                            set(DAY_OF_MONTH, dom - 1);
                        }
                    }
                }
                return;
            }

        case WEEK_OF_YEAR:
            {
                int y = jdate.getNormalizedYear();
                max = getActualMaximum(WEEK_OF_YEAR);
                set(DAY_OF_WEEK, internalGet(DAY_OF_WEEK)); // 更新 stamp[field]
                int woy = internalGet(WEEK_OF_YEAR);
                int value = woy + amount;
                if (!isTransitionYear(jdate.getNormalizedYear())) {
                    int year = jdate.getYear();
                    if (year == getMaximum(YEAR)) {
                        max = getActualMaximum(WEEK_OF_YEAR);
                    } else if (year == getMinimum(YEAR)) {
                        min = getActualMinimum(WEEK_OF_YEAR);
                        max = getActualMaximum(WEEK_OF_YEAR);
                        if (value > min && value < max) {
                            set(WEEK_OF_YEAR, value);
                            return;
                        }

                    }
                    // 如果新值在 min 和 max 之间（不包括 min 和 max），则可以使用该值。
                    if (value > min && value < max) {
                        set(WEEK_OF_YEAR, value);
                        return;
                    }
                    long fd = cachedFixedDate;
                    // 确保最小周具有当前的 DAY_OF_WEEK
                    long day1 = fd - (7 * (woy - min));
                    if (year != getMinimum(YEAR)) {
                        if (gcal.getYearFromFixedDate(day1) != y) {
                            min++;
                        }
                    } else {
                        CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        if (day1 < jcal.getFixedDate(d)) {
                            min++;
                        }
                    }


                                // 确保最大周数的情况
                    fd += 7 * (max - internalGet(WEEK_OF_YEAR));
                    if (gcal.getYearFromFixedDate(fd) != y) {
                        max--;
                    }
                    break;
                }

                // 在这里处理转换。
                long fd = cachedFixedDate;
                long day1 = fd - (7 * (woy - min));
                // 确保最小周数包含当前的 DAY_OF_WEEK
                LocalGregorianCalendar.Date d = getCalendarDate(day1);
                if (!(d.getEra() == jdate.getEra() && d.getYear() == jdate.getYear())) {
                    min++;
                }

                // 确保最大周数的情况
                fd += 7 * (max - woy);
                jcal.getCalendarDateFromFixedDate(d, fd);
                if (!(d.getEra() == jdate.getEra() && d.getYear() == jdate.getYear())) {
                    max--;
                }
                // value: 需要转换的新 WEEK_OF_YEAR 值
                // 转换为月和日。
                value = getRolledValue(woy, amount, min, max) - 1;
                d = getCalendarDate(day1 + value * 7);
                set(MONTH, d.getMonth() - 1);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case WEEK_OF_MONTH:
            {
                boolean isTransitionYear = isTransitionYear(jdate.getNormalizedYear());
                // dow: 从一周的第一天起的相对星期几
                int dow = internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();
                if (dow < 0) {
                    dow += 7;
                }

                long fd = cachedFixedDate;
                long month1;     // 月的第一天（通常是1）的固定日期
                int monthLength; // 实际月长度
                if (isTransitionYear) {
                    month1 = getFixedDateMonth1(jdate, fd);
                    monthLength = actualMonthLength();
                } else {
                    month1 = fd - internalGet(DAY_OF_MONTH) + 1;
                    monthLength = jcal.getMonthLength(jdate);
                }

                // 月的第一周的第一天。
                long monthDay1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(month1 + 6,
                                                                                     getFirstDayOfWeek());
                // 如果周有足够的天数形成一个完整的周，
                // 则周从上个月开始。
                if ((int)(monthDay1st - month1) >= getMinimalDaysInFirstWeek()) {
                    monthDay1st -= 7;
                }
                max = getActualMaximum(field);

                // value: 新的 WEEK_OF_MONTH 值
                int value = getRolledValue(internalGet(field), amount, 1, max) - 1;

                // nfd: 滚动日期的固定日期
                long nfd = monthDay1st + value * 7 + dow;

                // 与 WEEK_OF_YEAR 不同，如果 nfd 超出月份范围，
                // 我们需要改变星期几。
                if (nfd < month1) {
                    nfd = month1;
                } else if (nfd >= (month1 + monthLength)) {
                    nfd = month1 + monthLength - 1;
                }
                set(DAY_OF_MONTH, getCalendarDate(nfd).getDayOfMonth());
                return;
            }

        case DAY_OF_MONTH:
            {
                if (!isTransitionYear(jdate.getNormalizedYear())) {
                    max = jcal.getMonthLength(jdate);
                    break;
                }

                // TODO: 需要更改规范以使其可用于 DAY_OF_MONTH 滚动...

                // 转换处理。我们不能在这里更改年份和纪元值，
                // 因为这违反了日历滚动规范！
                long month1 = getFixedDateMonth1(jdate, cachedFixedDate);

                // 可能不是一个常规月份。将日期和范围转换为相对值，
                // 执行滚动，然后将结果转换回滚动日期。
                int value = getRolledValue((int)(cachedFixedDate - month1), amount,
                                           0, actualMonthLength() - 1);
                LocalGregorianCalendar.Date d = getCalendarDate(month1 + value);
                assert getEraIndex(d) == internalGetEra()
                    && d.getYear() == internalGet(YEAR) && d.getMonth()-1 == internalGet(MONTH);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case DAY_OF_YEAR:
            {
                max = getActualMaximum(field);
                if (!isTransitionYear(jdate.getNormalizedYear())) {
                    break;
                }

                // 处理转换。我们不能在这里更改年份和纪元值，
                // 因为这违反了日历滚动规范。
                int value = getRolledValue(internalGet(DAY_OF_YEAR), amount, min, max);
                long jan0 = cachedFixedDate - internalGet(DAY_OF_YEAR);
                LocalGregorianCalendar.Date d = getCalendarDate(jan0 + value);
                assert getEraIndex(d) == internalGetEra() && d.getYear() == internalGet(YEAR);
                set(MONTH, d.getMonth() - 1);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }

        case DAY_OF_WEEK:
            {
                int normalizedYear = jdate.getNormalizedYear();
                if (!isTransitionYear(normalizedYear) && !isTransitionYear(normalizedYear - 1)) {
                    // 如果年份中的周数相同，我们可以
                    // 直接更改 DAY_OF_WEEK。
                    int weekOfYear = internalGet(WEEK_OF_YEAR);
                    if (weekOfYear > 1 && weekOfYear < 52) {
                        set(WEEK_OF_YEAR, internalGet(WEEK_OF_YEAR));
                        max = SATURDAY;
                        break;
                    }
                }

                // 我们需要在年份边界附近和转换年份中
                // 以不同的方式处理。注意，更改纪元和年份值
                // 违反了滚动规则：不更改较大的日历字段...
                amount %= 7;
                if (amount == 0) {
                    return;
                }
                long fd = cachedFixedDate;
                long dowFirst = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fd, getFirstDayOfWeek());
                fd += amount;
                if (fd < dowFirst) {
                    fd += 7;
                } else if (fd >= dowFirst + 7) {
                    fd -= 7;
                }
                LocalGregorianCalendar.Date d = getCalendarDate(fd);
                set(ERA, getEraIndex(d));
                set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth());
                return;
            }

        case DAY_OF_WEEK_IN_MONTH:
            {
                min = 1; // 归一化后，min 应该是 1。
                if (!isTransitionYear(jdate.getNormalizedYear())) {
                    int dom = internalGet(DAY_OF_MONTH);
                    int monthLength = jcal.getMonthLength(jdate);
                    int lastDays = monthLength % 7;
                    max = monthLength / 7;
                    int x = (dom - 1) % 7;
                    if (x < lastDays) {
                        max++;
                    }
                    set(DAY_OF_WEEK, internalGet(DAY_OF_WEEK));
                    break;
                }

                // 转换年份处理。
                long fd = cachedFixedDate;
                long month1 = getFixedDateMonth1(jdate, fd);
                int monthLength = actualMonthLength();
                int lastDays = monthLength % 7;
                max = monthLength / 7;
                int x = (int)(fd - month1) % 7;
                if (x < lastDays) {
                    max++;
                }
                int value = getRolledValue(internalGet(field), amount, min, max) - 1;
                fd = month1 + value * 7 + x;
                LocalGregorianCalendar.Date d = getCalendarDate(fd);
                set(DAY_OF_MONTH, d.getDayOfMonth());
                return;
            }
        }

        set(field, getRolledValue(internalGet(field), amount, min, max));
    }

    @Override
    public String getDisplayName(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, SHORT, NARROW_FORMAT, locale,
                                    ERA_MASK|YEAR_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }

        int fieldValue = get(field);

        // "GanNen" 仅在 LONG 样式中支持。
        if (field == YEAR
            && (getBaseStyle(style) != LONG || fieldValue != 1 || get(ERA) == 0)) {
            return null;
        }

        String name = CalendarDataUtility.retrieveFieldValueName(getCalendarType(), field,
                                                                 fieldValue, style, locale);
        // 如果 ERA 值为 null 或空，则
        // 尝试从 Era 实例中获取其名称或缩写。
        if ((name == null || name.isEmpty()) &&
                field == ERA &&
                fieldValue < eras.length) {
            Era era = eras[fieldValue];
            name = (style == SHORT) ? era.getAbbreviation() : era.getName();
        }
        return name;
    }

    @Override
    public Map<String,Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!checkDisplayNameParams(field, style, ALL_STYLES, NARROW_FORMAT, locale,
                                    ERA_MASK|YEAR_MASK|MONTH_MASK|DAY_OF_WEEK_MASK|AM_PM_MASK)) {
            return null;
        }
        Map<String, Integer> names;
        names = CalendarDataUtility.retrieveFieldValueNames(getCalendarType(), field, style, locale);
        // 如果 strings[] 的元素少于 eras[]，则从 eras[] 获取更多名称。
        if (names != null) {
            if (field == ERA) {
                int size = names.size();
                if (style == ALL_STYLES) {
                    Set<Integer> values = new HashSet<>();
                    // 计算唯一的纪元值
                    for (String key : names.keySet()) {
                        values.add(names.get(key));
                    }
                    size = values.size();
                }
                if (size < eras.length) {
                    int baseStyle = getBaseStyle(style);
                    for (int i = size; i < eras.length; i++) {
                        Era era = eras[i];
                        if (baseStyle == ALL_STYLES || baseStyle == SHORT
                                || baseStyle == NARROW_FORMAT) {
                            names.put(era.getAbbreviation(), i);
                        }
                        if (baseStyle == ALL_STYLES || baseStyle == LONG) {
                            names.put(era.getName(), i);
                        }
                    }
                }
            }
        }
        return names;
    }

    /**
     * 返回此 <code>Calendar</code> 实例给定日历字段的最小值。最小值
     * 定义为对于任何可能的时间值，由 {@link
     * Calendar#get(int) get} 方法返回的最小值，
     * 考虑到 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的当前值。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最小值。
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    /**
     * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最大值。最大值
     * 定义为对于任何可能的时间值，由 {@link
     * Calendar#get(int) get} 方法返回的最大值，
     * 考虑到 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的当前值。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最大值。
     * @see #getMinimum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    public int getMaximum(int field) {
        switch (field) {
        case YEAR:
            {
                // 该值应取决于此日历的时区。
                LocalGregorianCalendar.Date d = jcal.getCalendarDate(Long.MAX_VALUE,
                                                                     getZone());
                return Math.max(LEAST_MAX_VALUES[YEAR], d.getYear());
            }
        }
        return MAX_VALUES[field];
    }

    /**
     * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最高最小值。
     * 最高最小值定义为对于任何可能的时间值，由
     * {@link #getActualMinimum(int)} 返回的最大值，
     * 考虑到 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的当前值。
     *
     * @param field 日历字段。
     * @return 给定日历字段的最高最小值。
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    public int getGreatestMinimum(int field) {
        return field == YEAR ? 1 : MIN_VALUES[field];
    }

    /**
     * 返回此 <code>GregorianCalendar</code> 实例给定日历字段的最低最大值。
     * 最低最大值定义为对于任何可能的时间值，由
     * {@link #getActualMaximum(int)} 返回的最小值，
     * 考虑到 {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的当前值。
     *
     * @param field 日历字段
     * @return 给定日历字段的最低最大值。
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getActualMinimum(int)
     * @see #getActualMaximum(int)
     */
    public int getLeastMaximum(int field) {
        switch (field) {
        case YEAR:
            {
                return Math.min(LEAST_MAX_VALUES[YEAR], getMaximum(YEAR));
            }
        }
        return LEAST_MAX_VALUES[field];
    }


                /**
     * 返回此日历字段可能具有的最小值，
     * 考虑给定的时间值和当前的
     * {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的值。
     *
     * @param field 日历字段
     * @return 给定字段的最小值，基于此 <code>JapaneseImperialCalendar</code> 的时间值
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMaximum(int)
     */
    public int getActualMinimum(int field) {
        if (!isFieldSet(YEAR_MASK|MONTH_MASK|WEEK_OF_YEAR_MASK, field)) {
            return getMinimum(field);
        }

        int value = 0;
        JapaneseImperialCalendar jc = getNormalizedCalendar();
        // 获取包含时区和时间的本地日期，
        // 这些在 jc.jdate 中缺失。
        LocalGregorianCalendar.Date jd = jcal.getCalendarDate(jc.getTimeInMillis(),
                                                              getZone());
        int eraIndex = getEraIndex(jd);
        switch (field) {
        case YEAR:
            {
                if (eraIndex > BEFORE_MEIJI) {
                    value = 1;
                    long since = eras[eraIndex].getSince(getZone());
                    CalendarDate d = jcal.getCalendarDate(since, getZone());
                    // 使用相同的年份来处理闰年。
                    // 即，jd 和 d 必须在闰年或平年上达成一致。
                    jd.setYear(d.getYear());
                    jcal.normalize(jd);
                    assert jd.isLeapYear() == d.isLeapYear();
                    if (getYearOffsetInMillis(jd) < getYearOffsetInMillis(d)) {
                        value++;
                    }
                } else {
                    value = getMinimum(field);
                    CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                    // 如果可能，使用等效的年份 d.getYear()。否则，忽略闰年和平年的差异。
                    int y = d.getYear();
                    if (y > 400) {
                        y -= 400;
                    }
                    jd.setYear(y);
                    jcal.normalize(jd);
                    if (getYearOffsetInMillis(jd) < getYearOffsetInMillis(d)) {
                        value++;
                    }
                }
            }
            break;

        case MONTH:
            {
                // 在明治之前和明治时期，1月是第一个月。
                if (eraIndex > MEIJI && jd.getYear() == 1) {
                    long since = eras[eraIndex].getSince(getZone());
                    CalendarDate d = jcal.getCalendarDate(since, getZone());
                    value = d.getMonth() - 1;
                    if (jd.getDayOfMonth() < d.getDayOfMonth()) {
                        value++;
                    }
                }
            }
            break;

        case WEEK_OF_YEAR:
            {
                value = 1;
                CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                // 向前移动400年以避免下溢。
                d.addYear(+400);
                jcal.normalize(d);
                jd.setEra(d.getEra());
                jd.setYear(d.getYear());
                jcal.normalize(jd);

                long jan1 = jcal.getFixedDate(d);
                long fd = jcal.getFixedDate(jd);
                int woy = getWeekNumber(jan1, fd);
                long day1 = fd - (7 * (woy - 1));
                if ((day1 < jan1) ||
                    (day1 == jan1 &&
                     jd.getTimeOfDay() < d.getTimeOfDay())) {
                    value++;
                }
            }
            break;
        }
        return value;
    }

    /**
     * 返回此日历字段可能具有的最大值，
     * 考虑给定的时间值和当前的
     * {@link Calendar#getFirstDayOfWeek() getFirstDayOfWeek}，
     * {@link Calendar#getMinimalDaysInFirstWeek() getMinimalDaysInFirstWeek}，
     * 和 {@link Calendar#getTimeZone() getTimeZone} 方法的值。
     * 例如，如果此实例的日期是平成16年2月1日，
     * <code>DAY_OF_MONTH</code> 字段的实际最大值是29，因为平成16年是闰年，
     * 如果此实例的日期是平成17年2月1日，则为28。
     *
     * @param field 日历字段
     * @return 给定字段的最大值，基于此 <code>JapaneseImperialCalendar</code> 的时间值
     * @see #getMinimum(int)
     * @see #getMaximum(int)
     * @see #getGreatestMinimum(int)
     * @see #getLeastMaximum(int)
     * @see #getActualMinimum(int)
     */
    public int getActualMaximum(int field) {
        final int fieldsForFixedMax = ERA_MASK|DAY_OF_WEEK_MASK|HOUR_MASK|AM_PM_MASK|
            HOUR_OF_DAY_MASK|MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK|
            ZONE_OFFSET_MASK|DST_OFFSET_MASK;
        if ((fieldsForFixedMax & (1<<field)) != 0) {
            return getMaximum(field);
        }

        JapaneseImperialCalendar jc = getNormalizedCalendar();
        LocalGregorianCalendar.Date date = jc.jdate;
        int normalizedYear = date.getNormalizedYear();

        int value = -1;
        switch (field) {
        case MONTH:
            {
                value = DECEMBER;
                if (isTransitionYear(date.getNormalizedYear())) {
                    // TODO: 一年中可能有多个过渡。
                    int eraIndex = getEraIndex(date);
                    if (date.getYear() != 1) {
                        eraIndex++;
                        assert eraIndex < eras.length;
                    }
                    long transition = sinceFixedDates[eraIndex];
                    long fd = jc.cachedFixedDate;
                    if (fd < transition) {
                        LocalGregorianCalendar.Date ldate
                            = (LocalGregorianCalendar.Date) date.clone();
                        jcal.getCalendarDateFromFixedDate(ldate, transition - 1);
                        value = ldate.getMonth() - 1;
                    }
                } else {
                    LocalGregorianCalendar.Date d = jcal.getCalendarDate(Long.MAX_VALUE,
                                                                         getZone());
                    if (date.getEra() == d.getEra() && date.getYear() == d.getYear()) {
                        value = d.getMonth() - 1;
                    }
                }
            }
            break;

        case DAY_OF_MONTH:
            value = jcal.getMonthLength(date);
            break;

        case DAY_OF_YEAR:
            {
                if (isTransitionYear(date.getNormalizedYear())) {
                    // 处理过渡年。
                    // TODO: 一年中可能有多个过渡。
                    int eraIndex = getEraIndex(date);
                    if (date.getYear() != 1) {
                        eraIndex++;
                        assert eraIndex < eras.length;
                    }
                    long transition = sinceFixedDates[eraIndex];
                    long fd = jc.cachedFixedDate;
                    CalendarDate d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getNormalizedYear(), BaseCalendar.JANUARY, 1);
                    if (fd < transition) {
                        value = (int)(transition - gcal.getFixedDate(d));
                    } else {
                        d.addYear(+1);
                        value = (int)(gcal.getFixedDate(d) - transition);
                    }
                } else {
                    LocalGregorianCalendar.Date d = jcal.getCalendarDate(Long.MAX_VALUE,
                                                                         getZone());
                    if (date.getEra() == d.getEra() && date.getYear() == d.getYear()) {
                        long fd = jcal.getFixedDate(d);
                        long jan1 = getFixedDateJan1(d, fd);
                        value = (int)(fd - jan1) + 1;
                    } else if (date.getYear() == getMinimum(YEAR)) {
                        CalendarDate d1 = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        long fd1 = jcal.getFixedDate(d1);
                        d1.addYear(1);
                        d1.setMonth(BaseCalendar.JANUARY).setDayOfMonth(1);
                        jcal.normalize(d1);
                        long fd2 = jcal.getFixedDate(d1);
                        value = (int)(fd2 - fd1);
                    } else {
                        value = jcal.getYearLength(date);
                    }
                }
            }
            break;

        case WEEK_OF_YEAR:
            {
                if (!isTransitionYear(date.getNormalizedYear())) {
                    LocalGregorianCalendar.Date jd = jcal.getCalendarDate(Long.MAX_VALUE,
                                                                          getZone());
                    if (date.getEra() == jd.getEra() && date.getYear() == jd.getYear()) {
                        long fd = jcal.getFixedDate(jd);
                        long jan1 = getFixedDateJan1(jd, fd);
                        value = getWeekNumber(jan1, fd);
                    } else if (date.getEra() == null && date.getYear() == getMinimum(YEAR)) {
                        CalendarDate d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                        // 向前移动400年以避免下溢。
                        d.addYear(+400);
                        jcal.normalize(d);
                        jd.setEra(d.getEra());
                        jd.setDate(d.getYear() + 1, BaseCalendar.JANUARY, 1);
                        jcal.normalize(jd);
                        long jan1 = jcal.getFixedDate(d);
                        long nextJan1 = jcal.getFixedDate(jd);
                        long nextJan1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6,
                                                                                            getFirstDayOfWeek());
                        int ndays = (int)(nextJan1st - nextJan1);
                        if (ndays >= getMinimalDaysInFirstWeek()) {
                            nextJan1st -= 7;
                        }
                        value = getWeekNumber(jan1, nextJan1st);
                    } else {
                        // 获取该年1月1日的星期几
                        CalendarDate d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                        d.setDate(date.getNormalizedYear(), BaseCalendar.JANUARY, 1);
                        int dayOfWeek = gcal.getDayOfWeek(d);
                        // 使用 firstDayOfWeek 值规范化星期几
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
                    }
                    break;
                }

                if (jc == this) {
                    jc = (JapaneseImperialCalendar) jc.clone();
                }
                int max = getActualMaximum(DAY_OF_YEAR);
                jc.set(DAY_OF_YEAR, max);
                value = jc.get(WEEK_OF_YEAR);
                if (value == 1 && max > 7) {
                    jc.add(WEEK_OF_YEAR, -1);
                    value = jc.get(WEEK_OF_YEAR);
                }
            }
            break;

        case WEEK_OF_MONTH:
            {
                LocalGregorianCalendar.Date jd = jcal.getCalendarDate(Long.MAX_VALUE,
                                                                      getZone());
                if (!(date.getEra() == jd.getEra() && date.getYear() == jd.getYear())) {
                    CalendarDate d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getNormalizedYear(), date.getMonth(), 1);
                    int dayOfWeek = gcal.getDayOfWeek(d);
                    int monthLength = actualMonthLength();
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
                } else {
                    long fd = jcal.getFixedDate(jd);
                    long month1 = fd - jd.getDayOfMonth() + 1;
                    value = getWeekNumber(month1, fd);
                }
            }
            break;

        case DAY_OF_WEEK_IN_MONTH:
            {
                int ndays, dow1;
                int dow = date.getDayOfWeek();
                BaseCalendar.Date d = (BaseCalendar.Date) date.clone();
                ndays = jcal.getMonthLength(d);
                d.setDayOfMonth(1);
                jcal.normalize(d);
                dow1 = d.getDayOfWeek();
                int x = dow - dow1;
                if (x < 0) {
                    x += 7;
                }
                ndays -= x;
                value = (ndays + 6) / 7;
            }
            break;

        case YEAR:
            {
                CalendarDate jd = jcal.getCalendarDate(jc.getTimeInMillis(), getZone());
                CalendarDate d;
                int eraIndex = getEraIndex(date);
                if (eraIndex == eras.length - 1) {
                    d = jcal.getCalendarDate(Long.MAX_VALUE, getZone());
                    value = d.getYear();
                    // 使用等效的年份来避免 getYearOffsetInMillis 调用中的溢出。
                    if (value > 400) {
                        jd.setYear(value - 400);
                    }
                } else {
                    d = jcal.getCalendarDate(eras[eraIndex + 1].getSince(getZone()) - 1,
                                             getZone());
                    value = d.getYear();
                    // 使用与 d.getYear() 相同的年份以保持闰年和平年的统一。
                    jd.setYear(value);
                }
                jcal.normalize(jd);
                if (getYearOffsetInMillis(jd) > getYearOffsetInMillis(d)) {
                    value--;
                }
            }
            break;


                    default:
            throw new ArrayIndexOutOfBoundsException(field);
        }
        return value;
    }

    /**
     * 返回从年份开始的毫秒偏移量。在 Long.MIN_VALUE 的年份中，这是一个超出限制的伪值。
     * 调用此方法之前，给定的 CalendarDate 对象必须已规范化。
     */
    private long getYearOffsetInMillis(CalendarDate date) {
        long t = (jcal.getDayOfYear(date) - 1) * ONE_DAY;
        return t + date.getTimeOfDay() - date.getZoneOffset();
    }

    public Object clone() {
        JapaneseImperialCalendar other = (JapaneseImperialCalendar) super.clone();

        other.jdate = (LocalGregorianCalendar.Date) jdate.clone();
        other.originalFields = null;
        other.zoneOffsets = null;
        return other;
    }

    public TimeZone getTimeZone() {
        TimeZone zone = super.getTimeZone();
        // 与 CalendarDate 共享时区
        jdate.setZone(zone);
        return zone;
    }

    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        // 与 CalendarDate 共享时区
        jdate.setZone(zone);
    }

    /**
     * 与 jdate 对应的固定日期。如果值为 Long.MIN_VALUE，则固定日期值未知。
     */
    transient private long cachedFixedDate = Long.MIN_VALUE;

    /**
     * 将时间值（从 <a href="Calendar.html#Epoch">Epoch</a> 开始的毫秒偏移量）转换为日历字段值。
     * 不会首先重新计算时间；要重新计算时间，然后计算字段，请调用 <code>complete</code> 方法。
     *
     * @see Calendar#complete
     */
    protected void computeFields() {
        int mask = 0;
        if (isPartiallyNormalized()) {
            // 确定需要计算哪些日历字段。
            mask = getSetStateFields();
            int fieldMask = ~mask & ALL_FIELDS;
            if (fieldMask != 0 || cachedFixedDate == Long.MIN_VALUE) {
                mask |= computeFields(fieldMask,
                                      mask & (ZONE_OFFSET_MASK|DST_OFFSET_MASK));
                assert mask == ALL_FIELDS;
            }
        } else {
            // 指定所有字段
            mask = ALL_FIELDS;
            computeFields(mask, 0);
        }
        // 计算所有字段后，将字段状态设置为 `COMPUTED'。
        setFieldsComputed(mask);
    }

    /**
     * 此 computeFields 实现了从 UTC（从 Epoch 开始的毫秒偏移量）到日历字段值的转换。
     * fieldMask 指定要将哪些字段的设置状态更改为 COMPUTED，尽管所有字段都设置为正确值。
     * 这是为了修复 4685354。
     *
     * @param fieldMask 位掩码，用于指定要更改设置状态的字段。
     * @param tzMask 位掩码，用于指定用于时间计算的时间区偏移字段。
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

        // 通过分别计算时间和 zoneOffset，我们可以使用比以前实现更宽的时间+zoneOffset 范围。
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

        // 查看是否可以使用 jdate 以避免日期计算。
        if (fixedDate != cachedFixedDate || fixedDate < 0) {
            jcal.getCalendarDateFromFixedDate(jdate, fixedDate);
            cachedFixedDate = fixedDate;
        }
        int era = getEraIndex(jdate);
        int year = jdate.getYear();

        // 始终设置 ERA 和 YEAR 值。
        internalSet(ERA, era);
        internalSet(YEAR, year);
        int mask = fieldMask | (ERA_MASK|YEAR_MASK);

        int month =  jdate.getMonth() - 1; // 0-based
        int dayOfMonth = jdate.getDayOfMonth();

        // 设置基本日期字段。
        if ((fieldMask & (MONTH_MASK|DAY_OF_MONTH_MASK|DAY_OF_WEEK_MASK))
            != 0) {
            internalSet(MONTH, month);
            internalSet(DAY_OF_MONTH, dayOfMonth);
            internalSet(DAY_OF_WEEK, jdate.getDayOfWeek());
            mask |= MONTH_MASK|DAY_OF_MONTH_MASK|DAY_OF_WEEK_MASK;
        }

        if ((fieldMask & (HOUR_OF_DAY_MASK|AM_PM_MASK|HOUR_MASK
                          |MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK)) != 0) {
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
            mask |= (HOUR_OF_DAY_MASK|AM_PM_MASK|HOUR_MASK
                     |MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK);
        }

        if ((fieldMask & (ZONE_OFFSET_MASK|DST_OFFSET_MASK)) != 0) {
            internalSet(ZONE_OFFSET, zoneOffsets[0]);
            internalSet(DST_OFFSET, zoneOffsets[1]);
            mask |= (ZONE_OFFSET_MASK|DST_OFFSET_MASK);
        }

        if ((fieldMask & (DAY_OF_YEAR_MASK|WEEK_OF_YEAR_MASK
                          |WEEK_OF_MONTH_MASK|DAY_OF_WEEK_IN_MONTH_MASK)) != 0) {
            int normalizedYear = jdate.getNormalizedYear();
            // 如果是纪元转换年，需要处理不规则的年份边界。
            boolean transitionYear = isTransitionYear(jdate.getNormalizedYear());
            int dayOfYear;
            long fixedDateJan1;
            if (transitionYear) {
                fixedDateJan1 = getFixedDateJan1(jdate, fixedDate);
                dayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
            } else if (normalizedYear == MIN_VALUES[YEAR]) {
                CalendarDate dx = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
                fixedDateJan1 = jcal.getFixedDate(dx);
                dayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
            } else {
                dayOfYear = (int) jcal.getDayOfYear(jdate);
                fixedDateJan1 = fixedDate - dayOfYear + 1;
            }
            long fixedDateMonth1 = transitionYear ?
                getFixedDateMonth1(jdate, fixedDate) : fixedDate - dayOfMonth + 1;

            internalSet(DAY_OF_YEAR, dayOfYear);
            internalSet(DAY_OF_WEEK_IN_MONTH, (dayOfMonth - 1) / 7 + 1);

            int weekOfYear = getWeekNumber(fixedDateJan1, fixedDate);

            // 规范要求以 ISO8601 样式计算 WEEK_OF_YEAR。这会带来一些问题。
            if (weekOfYear == 0) {
                // 如果日期属于上一年的最后一周，则使用“上一年”12 月 31 日的周数。
                // 再次，如果上一年是过渡年，需要特别处理。
                // 通常，一年的第一天的前一天是 12 月 31 日，但在日本天皇纪年系统中并不总是如此。
                long fixedDec31 = fixedDateJan1 - 1;
                long prevJan1;
                LocalGregorianCalendar.Date d = getCalendarDate(fixedDec31);
                if (!(transitionYear || isTransitionYear(d.getNormalizedYear()))) {
                    prevJan1 = fixedDateJan1 - 365;
                    if (d.isLeapYear()) {
                        --prevJan1;
                    }
                } else if (transitionYear) {
                    if (jdate.getYear() == 1) {
                        // 从明治时代以来，一年中没有多次过渡的情况。
                        // 历史上有这种情况。未来可能会再次出现这种情况。
                        if (era > REIWA) {
                            CalendarDate pd = eras[era - 1].getSinceDate();
                            if (normalizedYear == pd.getYear()) {
                                d.setMonth(pd.getMonth()).setDayOfMonth(pd.getDayOfMonth());
                            }
                        } else {
                            d.setMonth(LocalGregorianCalendar.JANUARY).setDayOfMonth(1);
                        }
                        jcal.normalize(d);
                        prevJan1 = jcal.getFixedDate(d);
                    } else {
                        prevJan1 = fixedDateJan1 - 365;
                        if (d.isLeapYear()) {
                            --prevJan1;
                        }
                    }
                } else {
                    CalendarDate cd = eras[getEraIndex(jdate)].getSinceDate();
                    d.setMonth(cd.getMonth()).setDayOfMonth(cd.getDayOfMonth());
                    jcal.normalize(d);
                    prevJan1 = jcal.getFixedDate(d);
                }
                weekOfYear = getWeekNumber(prevJan1, fixedDec31);
            } else {
                if (!transitionYear) {
                    // 普通年份
                    if (weekOfYear >= 52) {
                        long nextJan1 = fixedDateJan1 + 365;
                        if (jdate.isLeapYear()) {
                            nextJan1++;
                        }
                        long nextJan1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6,
                                                                                            getFirstDayOfWeek());
                        int ndays = (int)(nextJan1st - nextJan1);
                        if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
                            // 日期包含在第一周内。
                            weekOfYear = 1;
                        }
                    }
                } else {
                    LocalGregorianCalendar.Date d = (LocalGregorianCalendar.Date) jdate.clone();
                    long nextJan1;
                    if (jdate.getYear() == 1) {
                        d.addYear(+1);
                        d.setMonth(LocalGregorianCalendar.JANUARY).setDayOfMonth(1);
                        nextJan1 = jcal.getFixedDate(d);
                    } else {
                        int nextEraIndex = getEraIndex(d) + 1;
                        CalendarDate cd = eras[nextEraIndex].getSinceDate();
                        d.setEra(eras[nextEraIndex]);
                        d.setDate(1, cd.getMonth(), cd.getDayOfMonth());
                        jcal.normalize(d);
                        nextJan1 = jcal.getFixedDate(d);
                    }
                    long nextJan1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6,
                                                                                        getFirstDayOfWeek());
                    int ndays = (int)(nextJan1st - nextJan1);
                    if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
                        // 日期包含在第一周内。
                        weekOfYear = 1;
                    }
                }
            }
            internalSet(WEEK_OF_YEAR, weekOfYear);
            internalSet(WEEK_OF_MONTH, getWeekNumber(fixedDateMonth1, fixedDate));
            mask |= (DAY_OF_YEAR_MASK|WEEK_OF_YEAR_MASK|WEEK_OF_MONTH_MASK|DAY_OF_WEEK_IN_MONTH_MASK);
        }
        return mask;
    }

    /**
     * 返回固定日期 fixedDay1 和 fixedDate 之间周期的周数。
     * 应用 getFirstDayOfWeek-getMinimalDaysInFirstWeek 规则来计算周数。
     *
     * @param fixedDay1 周期第一天的固定日期
     * @param fixedDate 周期最后一天的固定日期
     * @return 给定周期的周数
     */
    private int getWeekNumber(long fixedDay1, long fixedDate) {
        // 由于 Julian 和 Gregorian 在此计算中是相同的概念，因此可以始终使用 `jcal'。
        long fixedDay1st = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDay1 + 6,
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
     * 将日历字段值转换为时间值（从 <a href="Calendar.html#Epoch">Epoch</a> 开始的毫秒偏移量）。
     *
     * @exception IllegalArgumentException 如果任何日历字段无效。
     */
    protected void computeTime() {
        // 在非宽松模式下，对外部设置的日历字段进行简要检查。
        // 通过此检查，将字段值存储在 originalFields[] 中，以查看它们是否在稍后被规范化。
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
        // 使用选定的字段计算时间。
        int fieldMask = selectFields();

        int year;
        int era;

        if (isSet(ERA)) {
            era = internalGet(ERA);
            year = isSet(YEAR) ? internalGet(YEAR) : 1;
        } else {
            if (isSet(YEAR)) {
                era = currentEra;
                year = internalGet(YEAR);
            } else {
                // 相当于 1970 年（格里高利历）
                era = SHOWA;
                year = 45;
            }
        }

        // 计算一天中的时间。我们依赖于未设置字段为 0 的约定。
        long timeOfDay = 0;
        if (isFieldSet(fieldMask, HOUR_OF_DAY)) {
            timeOfDay += (long) internalGet(HOUR_OF_DAY);
        } else {
            timeOfDay += internalGet(HOUR);
            // AM_PM 的默认值为 0，表示上午。
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
        long fixedDate = timeOfDay / ONE_DAY;
        timeOfDay %= ONE_DAY;
        while (timeOfDay < 0) {
            timeOfDay += ONE_DAY;
            --fixedDate;
        }

        // 计算自 1 年 1 月 1 日（格里高利历）以来的固定日期。
        fixedDate += getFixedDate(era, year, fieldMask);

        // millis 表示本地时钟时间（以毫秒为单位）。
        long millis = (fixedDate - EPOCH_OFFSET) * ONE_DAY + timeOfDay;

        // 计算时区偏移量和夏令时偏移量。这里存在两个潜在的模棱两可之处。
        // 1. 进入夏令时的转换。这里，指定时间为 2:00 am - 2:59 am
        //    可能处于标准时间或夏令时，但 2:00 am 是一个无效的表示（表示从 1:59:59 am 标准时间跳到 3:00:00 am 夏令时）。
        //    我们假设标准时间。
        // 2. 退出夏令时的转换。这里，指定时间为 1:00 am - 1:59 am
        //    可能处于标准时间或夏令时。两者都是有效的表示（表示从 1:59:59 夏令时跳到 1:00:00 标准时间）。
        //    再次假设标准时间。
        // 我们使用 TimeZone 对象，除非用户显式设置了 ZONE_OFFSET
        // 或 DST_OFFSET 字段；在这种情况下，我们使用这些字段。
        TimeZone zone = getZone();
        if (zoneOffsets == null) {
            zoneOffsets = new int[2];
        }
        int tzMask = fieldMask & (ZONE_OFFSET_MASK | DST_OFFSET_MASK);
        if (tzMask != (ZONE_OFFSET_MASK | DST_OFFSET_MASK)) {
            if (zone instanceof ZoneInfo) {
                ((ZoneInfo) zone).getOffsetsByWall(millis, zoneOffsets);
            } else {
                zone.getOffsets(millis - zone.getRawOffset(), zoneOffsets);
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

        // 设置此日历的毫秒时间
        time = millis;

        int mask = computeFields(fieldMask | getSetStateFields(), tzMask);

        if (!isLenient()) {
            for (int field = 0; field < FIELD_COUNT; field++) {
                if (!isExternallySet(field)) {
                    continue;
                }
                if (originalFields[field] != internalGet(field)) {
                    int wrongValue = internalGet(field);
                    // 恢复原始字段值
                    System.arraycopy(originalFields, 0, fields, 0, fields.length);
                    throw new IllegalArgumentException(getFieldName(field) + "=" + wrongValue
                                                       + ", expected " + originalFields[field]);
                }
            }
        }
        setFieldsNormalized(mask);
    }

    /**
     * 使用给定的年份和指定的日历字段计算格里高利历或儒略历的固定日期。
     *
     * @param era 时代索引
     * @param year 规范化的年份编号，0 表示公元前 1 年，-1 表示公元前 2 年，等等。
     * @param fieldMask 用于日期计算的日历字段
     * @return 固定日期
     * @see Calendar#selectFields
     */
    private long getFixedDate(int era, int year, int fieldMask) {
        int month = JANUARY;
        int firstDayOfMonth = 1;
        if (isFieldSet(fieldMask, MONTH)) {
            // 无需检查 MONTH 是否已设置（无需调用 isSet(MONTH)）
            // 因为其未设置值恰好是 JANUARY（0）。
            month = internalGet(MONTH);

            // 如果月份超出范围，将其调整到范围内。
            if (month > DECEMBER) {
                year += month / 12;
                month %= 12;
            } else if (month < JANUARY) {
                int[] rem = new int[1];
                year += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
        } else {
            if (year == 1 && era != 0) {
                CalendarDate d = eras[era].getSinceDate();
                month = d.getMonth() - 1;
                firstDayOfMonth = d.getDayOfMonth();
            }
        }

        // 如果年份是最小值，调整基础日期。
        if (year == MIN_VALUES[YEAR]) {
            CalendarDate dx = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
            int m = dx.getMonth() - 1;
            if (month < m) {
                month = m;
            }
            if (month == m) {
                firstDayOfMonth = dx.getDayOfMonth();
            }
        }

        LocalGregorianCalendar.Date date = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        date.setEra(era > 0 ? eras[era] : null);
        date.setDate(year, month + 1, firstDayOfMonth);
        jcal.normalize(date);

        // 获取自 1 年 1 月 1 日（格里高利历）以来的固定日期。我们处于 'year' 年的 'month' 月的第一天。
        long fixedDate = jcal.getFixedDate(date);

        if (isFieldSet(fieldMask, MONTH)) {
            // 基于月份的计算
            if (isFieldSet(fieldMask, DAY_OF_MONTH)) {
                // 我们处于月份的“第一天”（可能不是 1）。如果设置了 DAY_OF_MONTH，
                // 只需添加偏移量。如果 isSet 调用返回 false，那意味着
                // DAY_OF_MONTH 仅因为选定的组合而被选中。我们不需要添加任何内容，
                // 因为默认值是“第一天”。
                if (isSet(DAY_OF_MONTH)) {
                    // 为了避免 DAY_OF_MONTH - firstDayOfMonth 溢出，先添加 DAY_OF_MONTH，再减去 firstDayOfMonth。
                    fixedDate += internalGet(DAY_OF_MONTH);
                    fixedDate -= firstDayOfMonth;
                }
            } else {
                if (isFieldSet(fieldMask, WEEK_OF_MONTH)) {
                    long firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6,
                                                                                            getFirstDayOfWeek());
                    // 如果第一周有足够的天数，则移动到前一周。
                    if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
                        firstDayOfWeek -= 7;
                    }
                    if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                        firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6,
                                                                                           internalGet(DAY_OF_WEEK));
                    }
                    // 在宽松模式下，我们将前一个月的天数视为指定的
                    // WEEK_OF_MONTH 的一部分。参见 4633646。
                    fixedDate = firstDayOfWeek + 7 * (internalGet(WEEK_OF_MONTH) - 1);
                } else {
                    int dayOfWeek;
                    if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                        dayOfWeek = internalGet(DAY_OF_WEEK);
                    } else {
                        dayOfWeek = getFirstDayOfWeek();
                    }
                    // 我们基于星期几在月中的位置。唯一的
                    // 复杂之处在于星期几在月中的位置为负。
                    int dowim;
                    if (isFieldSet(fieldMask, DAY_OF_WEEK_IN_MONTH)) {
                        dowim = internalGet(DAY_OF_WEEK_IN_MONTH);
                    } else {
                        dowim = 1;
                    }
                    if (dowim >= 0) {
                        fixedDate = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate + (7 * dowim) - 1,
                                                                                      dayOfWeek);
                    } else {
                        // 转到指定周边界的下一周的第一天。
                        int lastDate = monthLength(month, year) + (7 * (dowim + 1));
                        // 然后，获取最后一天或之前的星期几日期。
                        fixedDate = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate + lastDate - 1,
                                                                                      dayOfWeek);
                    }
                }
            }
        } else {
            // 我们处于一年的第一天。
            if (isFieldSet(fieldMask, DAY_OF_YEAR)) {
                if (isTransitionYear(date.getNormalizedYear())) {
                    fixedDate = getFixedDateJan1(date, fixedDate);
                }
                // 添加偏移量，然后减去 1。（确保避免溢出。）
                fixedDate += internalGet(DAY_OF_YEAR);
                fixedDate--;
            } else {
                long firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6,
                                                                                        getFirstDayOfWeek());
                // 如果第一周有足够的天数，则移动到前一周。
                if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
                    firstDayOfWeek -= 7;
                }
                if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
                    int dayOfWeek = internalGet(DAY_OF_WEEK);
                    if (dayOfWeek != getFirstDayOfWeek()) {
                        firstDayOfWeek = LocalGregorianCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6,
                                                                                           dayOfWeek);
                    }
                }
                fixedDate = firstDayOfWeek + 7 * ((long) internalGet(WEEK_OF_YEAR) - 1);
            }
        }
        return fixedDate;
    }

    /**
     * 返回指定日期之前一年的第一天（通常是 1 月 1 日）的固定日期。
     *
     * @param date 计算第一年第一天的日期。该日期必须在转换年。
     * @param fixedDate 日期的固定日期表示形式
     */
    private long getFixedDateJan1(LocalGregorianCalendar.Date date, long fixedDate) {
        Era era = date.getEra();
        if (date.getEra() != null && date.getYear() == 1) {
            for (int eraIndex = getEraIndex(date); eraIndex > 0; eraIndex--) {
                CalendarDate d = eras[eraIndex].getSinceDate();
                long fd = gcal.getFixedDate(d);
                // 一年中可能有多个时代转换。
                if (fd > fixedDate) {
                    continue;
                }
                return fd;
            }
        }
        CalendarDate d = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        d.setDate(date.getNormalizedYear(), Gregorian.JANUARY, 1);
        return gcal.getFixedDate(d);
    }

    /**
     * 返回指定日期之前一个月的第一天（通常是该月的 1 日）的固定日期。
     *
     * @param date 计算一个月第一天的日期。该日期必须在时代转换年。
     * @param fixedDate 日期的固定日期表示形式
     */
    private long getFixedDateMonth1(LocalGregorianCalendar.Date date,
                                          long fixedDate) {
        int eraIndex = getTransitionEraIndex(date);
        if (eraIndex != -1) {
            long transition = sinceFixedDates[eraIndex];
            // 如果给定日期在转换日期或之后，则返回转换日期。
            if (transition <= fixedDate) {
                return transition;
            }
        }

        // 否则，我们可以使用该月的 1 日。
        return fixedDate - date.getDayOfMonth() + 1;
    }

    /**
     * 从指定的固定日期生成一个 LocalGregorianCalendar.Date。
     *
     * @param fd 固定日期
     */
    private static LocalGregorianCalendar.Date getCalendarDate(long fd) {
        LocalGregorianCalendar.Date d = jcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        jcal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    /**
     * 返回指定月份在指定格里高利年中的长度。年份编号必须是规范化的。
     *
     * @see GregorianCalendar#isLeapYear(int)
     */
    private int monthLength(int month, int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear) ?
            GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
    }

    /**
     * 返回指定月份在 internalGet(YEAR) 提供的年份中的长度。
     *
     * @see GregorianCalendar#isLeapYear(int)
     */
    private int monthLength(int month) {
        assert jdate.isNormalized();
        return jdate.isLeapYear() ?
            GregorianCalendar.LEAP_MONTH_LENGTH[month] : GregorianCalendar.MONTH_LENGTH[month];
    }

    private int actualMonthLength() {
        int length = jcal.getMonthLength(jdate);
        int eraIndex = getTransitionEraIndex(jdate);
        if (eraIndex != -1) {
            long transitionFixedDate = sinceFixedDates[eraIndex];
            CalendarDate d = eras[eraIndex].getSinceDate();
            if (transitionFixedDate <= cachedFixedDate) {
                length -= d.getDayOfMonth() - 1;
            } else {
                length = d.getDayOfMonth() - 1;
            }
        }
        return length;
    }


                /**
     * 如果给定的日期在转换月份中，则返回新的纪元索引。例如，如果给定的日期是平成1年（1989年）1月20日，则返回平成的纪元索引。同样，如果给定的日期是昭和64年（1989年）1月3日，则返回平成的纪元索引。如果给定的日期不在任何转换月份中，则返回-1。
     */
    private static int getTransitionEraIndex(LocalGregorianCalendar.Date date) {
        int eraIndex = getEraIndex(date);
        CalendarDate transitionDate = eras[eraIndex].getSinceDate();
        if (transitionDate.getYear() == date.getNormalizedYear() &&
            transitionDate.getMonth() == date.getMonth()) {
            return eraIndex;
        }
        if (eraIndex < eras.length - 1) {
            transitionDate = eras[++eraIndex].getSinceDate();
            if (transitionDate.getYear() == date.getNormalizedYear() &&
                transitionDate.getMonth() == date.getMonth()) {
                return eraIndex;
            }
        }
        return -1;
    }

    private boolean isTransitionYear(int normalizedYear) {
        for (int i = eras.length - 1; i > 0; i--) {
            int transitionYear = eras[i].getSinceDate().getYear();
            if (normalizedYear == transitionYear) {
                return true;
            }
            if (normalizedYear > transitionYear) {
                break;
            }
        }
        return false;
    }

    private static int getEraIndex(LocalGregorianCalendar.Date date) {
        Era era = date.getEra();
        for (int i = eras.length - 1; i > 0; i--) {
            if (eras[i] == era) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 如果此对象已规范化（所有字段和时间同步），则返回此对象。否则，在宽松模式下调用complete()方法后返回克隆对象。
     */
    private JapaneseImperialCalendar getNormalizedCalendar() {
        JapaneseImperialCalendar jc;
        if (isFullyNormalized()) {
            jc = this;
        } else {
            // 创建克隆并规范化日历字段
            jc = (JapaneseImperialCalendar) this.clone();
            jc.setLenient(true);
            jc.complete();
        }
        return jc;
    }

    /**
     * 在进行如add(MONTH)、add(YEAR)等调整后，我们不希望月份跳跃。例如，我们不希望1月31日加1个月跳到3月3日，而是希望跳到2月28日。可能会遇到此问题的调整调用此方法以保持正确的月份。
     */
    private void pinDayOfMonth(LocalGregorianCalendar.Date date) {
        int year = date.getYear();
        int dom = date.getDayOfMonth();
        if (year != getMinimum(YEAR)) {
            date.setDayOfMonth(1);
            jcal.normalize(date);
            int monthLength = jcal.getMonthLength(date);
            if (dom > monthLength) {
                date.setDayOfMonth(monthLength);
            } else {
                date.setDayOfMonth(dom);
            }
            jcal.normalize(date);
        } else {
            LocalGregorianCalendar.Date d = jcal.getCalendarDate(Long.MIN_VALUE, getZone());
            LocalGregorianCalendar.Date realDate = jcal.getCalendarDate(time, getZone());
            long tod = realDate.getTimeOfDay();
            // 使用等效年份。
            realDate.addYear(+400);
            realDate.setMonth(date.getMonth());
            realDate.setDayOfMonth(1);
            jcal.normalize(realDate);
            int monthLength = jcal.getMonthLength(realDate);
            if (dom > monthLength) {
                realDate.setDayOfMonth(monthLength);
            } else {
                if (dom < d.getDayOfMonth()) {
                    realDate.setDayOfMonth(d.getDayOfMonth());
                } else {
                    realDate.setDayOfMonth(dom);
                }
            }
            if (realDate.getDayOfMonth() == d.getDayOfMonth() && tod < d.getTimeOfDay()) {
                realDate.setDayOfMonth(Math.min(dom + 1, monthLength));
            }
            // 恢复年份。
            date.setDate(year, realDate.getMonth(), realDate.getDayOfMonth());
            // 不在此处规范化日期，以防止下溢。
        }
    }

    /**
     * 返回'roll'操作后的新值。
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
     * 返回ERA。我们需要一个特殊的方法，因为默认ERA是当前纪元，但ERA为零（未设置）意味着明治之前。
     */
    private int internalGetEra() {
        return isSet(ERA) ? internalGet(ERA) : currentEra;
    }

    /**
     * 更新内部状态。
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (jdate == null) {
            jdate = jcal.newCalendarDate(getZone());
            cachedFixedDate = Long.MIN_VALUE;
        }
    }
}
