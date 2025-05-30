
/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.EPOCH_DAY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import sun.util.logging.PlatformLogger;

/**
 * 希贾拉历（Hijrah）是一个支持伊斯兰历的阴历。
 * <p>
 * 希贾拉历遵循希贾拉历系统的规则。希贾拉历有几种变体，基于新月确定的时间和观察地点的不同。
 * 在一些变体中，每个月的长度是根据月球和地球的天文数据计算得出的，而在其他变体中，每个月的长度是由授权的新月观察确定的。
 * 对于算法计算的历法，可以预测未来。对于基于观察的历法，只有过去的观察数据可用。
 * <p>
 * 每个月的长度为29天或30天。普通年有354天；闰年有355天。
 *
 * <p>
 * CLDR和LDML标识变体：
 * <table cellpadding="2" summary="希贾拉历的变体">
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">历法ID</th>
 * <th class="colFirst" align="left">历法类型</th>
 * <th class="colFirst" align="left">区域设置扩展，参见 {@link java.util.Locale}</th>
 * <th class="colLast" align="left">描述</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="altColor">
 * <td>Hijrah-umalqura</td>
 * <td>islamic-umalqura</td>
 * <td>ca-islamic-umalqura</td>
 * <td>伊斯兰 - 沙特阿拉伯乌玛尔库拉历</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>可以通过 {@link Chronology#getAvailableChronologies()} 获得其他变体。
 *
 * <p>示例</p>
 * <p>
 * 从区域设置中选择历法使用 {@link Chronology#ofLocale} 基于支持的BCP 47扩展机制请求特定的历法（"ca"）。例如，
 * </p>
 * <pre>
 *      Locale locale = Locale.forLanguageTag("en-US-u-ca-islamic-umalqura");
 *      Chronology chrono = Chronology.ofLocale(locale);
 * </pre>
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @implNote
 * 每个希贾拉变体单独配置。每个变体由一个定义 {@code ID}、历法类型、历法的开始、与
 * ISO历法的对齐以及一定年份范围内每个月的长度的属性资源定义。
 * 变体在 {@code calendars.properties} 文件中标识。新属性以 {@code "calendars.hijrah."} 开头：
 * <table cellpadding="2" border="0" summary="希贾拉历变体的配置">
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">属性名称</th>
 * <th class="colFirst" align="left">属性值</th>
 * <th class="colLast" align="left">描述 </th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="altColor">
 * <td>calendars.hijrah.{ID}</td>
 * <td>定义 {@code {ID}} 变体的属性资源</td>
 * <td>属性资源位于 {@code calendars.properties} 文件中</td>
 * </tr>
 * <tr class="rowColor">
 * <td>calendars.hijrah.{ID}.type</td>
 * <td>历法类型</td>
 * <td>LDML定义历法类型名称</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * 希贾拉属性资源是一组描述历法的属性。语法由 {@code java.util.Properties#load(Reader)} 定义。
 * <table cellpadding="2" summary="希贾拉历的配置">
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">属性名称</th>
 * <th class="colFirst" align="left">属性值</th>
 * <th class="colLast" align="left">描述</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="altColor">
 * <td>id</td>
 * <td>历法ID，例如 "Hijrah-umalqura"</td>
 * <td>常用历法的ID</td>
 * </tr>
 * <tr class="rowColor">
 * <td>type</td>
 * <td>历法类型，例如 "islamic-umalqura"</td>
 * <td>LDML定义历法类型</td>
 * </tr>
 * <tr class="altColor">
 * <td>version</td>
 * <td>版本，例如: "1.8.0_1"</td>
 * <td>希贾拉变体数据的版本</td>
 * </tr>
 * <tr class="rowColor">
 * <td>iso-start</td>
 * <td>ISO开始日期，格式为 {@code yyyy-MM-dd}，例如: "1900-04-30"</td>
 * <td>最小希贾拉年的第一天的ISO日期。</td>
 * </tr>
 * <tr class="altColor">
 * <td>yyyy - 4位数字的年份，例如 "1434"</td>
 * <td>12个月长度的序列，例如: "29 30 29 30 29 30 30 30 29 30 29 29"</td>
 * <td>12个月的长度，以空格分隔。必须为每个年份提供一个数字年份属性，且不能有空缺。
 * 月份长度必须在29-32之间（包括29和32）。
 * </td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @since 1.8
 */
public final class HijrahChronology extends AbstractChronology implements Serializable {

    /**
     * 希贾拉历的ID。
     */
    private final transient String typeId;
    /**
     * 希贾拉历的历法类型。
     */
    private final transient String calendarType;
    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 3127340209035924785L;
    /**
     * 沙特阿拉伯伊斯兰乌玛尔库拉历的单例实例。
     * 其他希贾拉历变体可能从 {@link Chronology#getAvailableChronologies} 获得。
     */
    public static final HijrahChronology INSTANCE;
    /**
     * 标记以指示配置数据初始化是否完成。
     * @see #checkCalendarInit()
     */
    private transient volatile boolean initComplete;
    /**
     * 以希贾拉历纪元月为索引的纪元日数组。
     * 由 {@link #loadCalendarData} 计算。
     */
    private transient int[] hijrahEpochMonthStartDays;
    /**
     * 该希贾拉历的最小纪元日。
     * 由 {@link #loadCalendarData} 计算。
     */
    private transient int minEpochDay;
    /**
     * 有历法数据的最大小纪元日。
     * 由 {@link #loadCalendarData} 计算。
     */
    private transient int maxEpochDay;
    /**
     * 最小纪元月。
     * 由 {@link #loadCalendarData} 计算。
     */
    private transient int hijrahStartEpochMonth;
    /**
     * 月份的最短长度。
     * 由 {@link #createEpochMonths} 计算。
     */
    private transient int minMonthLength;
    /**
     * 月份的最短长度。
     * 由 {@link #createEpochMonths} 计算。
     */
    private transient int maxMonthLength;
    /**
     * 年份的最短天数。
     * 由 {@link #createEpochMonths} 计算。
     */
    private transient int minYearLength;
    /**
     * 年份的最短天数。
     * 由 {@link #createEpochMonths} 计算。
     */
    private transient int maxYearLength;
    /**
     * 引用存储在
     * ${java.home}/lib/calendars.properties
     * 中的属性。
     */
    private final transient static Properties calendarProperties;

    /**
     * 希贾拉历变体的属性名称前缀。
     */
    private static final String PROP_PREFIX = "calendar.hijrah.";
    /**
     * 包含变体历法类型的属性名称后缀。
     */
    private static final String PROP_TYPE_SUFFIX = ".type";

    /**
     * 在 lib/calendars.properties 文件中找到的预定义历法的静态初始化。
     */
    static {
        try {
            calendarProperties = sun.util.calendar.BaseCalendar.getCalendarProperties();
        } catch (IOException ioe) {
            throw new InternalError("无法初始化 lib/calendars.properties", ioe);
        }

        try {
            INSTANCE = new HijrahChronology("Hijrah-umalqura");
            // 通过别名注册
            AbstractChronology.registerChrono(INSTANCE, "Hijrah");
            AbstractChronology.registerChrono(INSTANCE, "islamic");
        } catch (DateTimeException ex) {
            // 缺少希贾拉历将导致初始化此类失败。
            PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
            logger.severe("无法初始化希贾拉历: Hijrah-umalqura", ex);
            throw new RuntimeException("无法初始化 Hijrah-umalqura 历法", ex.getCause());
        }
        registerVariants();
    }

    /**
     * 为列出的每个希贾拉变体创建希贾拉历并注册。
     * 初始化期间的异常将被记录但忽略。
     */
    private static void registerVariants() {
        for (String name : calendarProperties.stringPropertyNames()) {
            if (name.startsWith(PROP_PREFIX)) {
                String id = name.substring(PROP_PREFIX.length());
                if (id.indexOf('.') >= 0) {
                    continue;   // 无名称或不是历法的简单名称
                }
                if (id.equals(INSTANCE.getId())) {
                    continue;           // 不重复默认值
                }
                try {
                    // 创建并注册变体
                    HijrahChronology chrono = new HijrahChronology(id);
                    AbstractChronology.registerChrono(chrono);
                } catch (DateTimeException ex) {
                    // 记录错误并继续
                    PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
                    logger.severe("无法初始化希贾拉历: " + id, ex);
                }
            }
        }
    }

    /**
     * 为命名的变体创建希贾拉历。
     * 从 {@code calendars.properties} 中检索资源和历法类型。
     * 属性名称为 {@code "calendar.hijrah." + id}
     * 和  {@code "calendar.hijrah." + id + ".type"}
     * @param id 历法的ID
     * @throws DateTimeException 如果属性文件中缺少历法类型。
     * @throws IllegalArgumentException 如果ID为空
     */
    private HijrahChronology(String id) throws DateTimeException {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("历法ID为空");
        }
        String propName = PROP_PREFIX + id + PROP_TYPE_SUFFIX;
        String calType = calendarProperties.getProperty(propName);
        if (calType == null || calType.isEmpty()) {
            throw new DateTimeException("属性文件中缺少或为空: " + propName);
        }
        this.typeId = id;
        this.calendarType = calType;
    }

    /**
     * 检查并确保历法数据已初始化。
     * 初始化检查在公共方法和包级方法之间的边界处执行。如果一个公共方法调用另一个公共方法，则调用者中不需要检查。
     * HijrahDate 的构造函数调用 {@link #getEpochDay} 或
     * {@link #getHijrahDateInfo}，因此从 HijrahDate 到
     * HijrahChronology 的每次包级方法调用都已检查。
     *
     * @throws DateTimeException 如果历法数据配置错误或加载数据时发生 IOException
     */
    private void checkCalendarInit() {
        // 保持简短以便内联提高性能
        if (initComplete == false) {
            loadCalendarData();
            initComplete = true;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取历法的ID。
     * <p>
     * ID 唯一标识 {@code Chronology}。可以使用 {@link Chronology#of(String)} 通过 ID 查找 {@code Chronology}。
     *
     * @return 历法ID，非空
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return typeId;
    }


                /**
     * 获取伊斯兰历的历法类型。
     * <p>
     * 历法类型是由
     * <em>Unicode Locale Data Markup Language (LDML)</em> 规范定义的标识符。
     * 它可以用于通过 {@link Chronology#of(String)} 查找 {@code Chronology}。
     *
     * @return 历法系统类型；如果历法有标准类型，则返回非空值，否则返回 null
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return calendarType;
    }

    //-----------------------------------------------------------------------
    /**
     * 从纪元、纪元年、年中的月和月中的日字段中获取伊斯兰历的本地日期。
     *
     * @param era  伊斯兰纪元，不为空
     * @param yearOfEra  纪元年
     * @param month  年中的月
     * @param dayOfMonth  月中的日
     * @return 伊斯兰本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code HijrahEra}
     */
    @Override
    public HijrahDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * 从历法年、年中的月和月中的日字段中获取伊斯兰历的本地日期。
     *
     * @param prolepticYear  历法年
     * @param month  年中的月
     * @param dayOfMonth  月中的日
     * @return 伊斯兰本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public HijrahDate date(int prolepticYear, int month, int dayOfMonth) {
        return HijrahDate.of(this, prolepticYear, month, dayOfMonth);
    }

    /**
     * 从纪元、纪元年和年中的日字段中获取伊斯兰历的本地日期。
     *
     * @param era  伊斯兰纪元，不为空
     * @param yearOfEra  纪元年
     * @param dayOfYear  年中的日
     * @return 伊斯兰本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code HijrahEra}
     */
    @Override
    public HijrahDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * 从历法年和年中的日字段中获取伊斯兰历的本地日期。
     *
     * @param prolepticYear  历法年
     * @param dayOfYear  年中的日
     * @return 伊斯兰本地日期，不为空
     * @throws DateTimeException 如果年份超出范围，或年中的日对年份无效
     */
    @Override
    public HijrahDate dateYearDay(int prolepticYear, int dayOfYear) {
        HijrahDate date = HijrahDate.of(this, prolepticYear, 1, 1);
        if (dayOfYear > date.lengthOfYear()) {
            throw new DateTimeException("无效的年中的日: " + dayOfYear);
        }
        return date.plusDays(dayOfYear - 1);
    }

    /**
     * 从纪元日获取伊斯兰历的本地日期。
     *
     * @param epochDay  纪元日
     * @return 伊斯兰本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以返回协变类型
    public HijrahDate dateEpochDay(long epochDay) {
        return HijrahDate.ofEpochDay(this, epochDay);
    }

    @Override
    public HijrahDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    @Override
    public HijrahDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    @Override
    public HijrahDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    @Override
    public HijrahDate date(TemporalAccessor temporal) {
        if (temporal instanceof HijrahDate) {
            return (HijrahDate) temporal;
        }
        return HijrahDate.ofEpochDay(this, temporal.getLong(EPOCH_DAY));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<HijrahDate> localDateTime(TemporalAccessor temporal) {
        return (ChronoLocalDateTime<HijrahDate>) super.localDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<HijrahDate> zonedDateTime(TemporalAccessor temporal) {
        return (ChronoZonedDateTime<HijrahDate>) super.zonedDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<HijrahDate> zonedDateTime(Instant instant, ZoneId zone) {
        return (ChronoZonedDateTime<HijrahDate>) super.zonedDateTime(instant, zone);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isLeapYear(long prolepticYear) {
        checkCalendarInit();
        if (prolepticYear < getMinimumYear() || prolepticYear > getMaximumYear()) {
            return false;
        }
        int len = getYearLength((int) prolepticYear);
        return (len > 354);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof HijrahEra == false) {
            throw new ClassCastException("纪元必须是 HijrahEra");
        }
        return yearOfEra;
    }

    @Override
    public HijrahEra eraOf(int eraValue) {
        switch (eraValue) {
            case 1:
                return HijrahEra.AH;
            default:
                throw new DateTimeException("无效的伊斯兰纪元");
        }
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(HijrahEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        checkCalendarInit();
        if (field instanceof ChronoField) {
            ChronoField f = field;
            switch (f) {
                case DAY_OF_MONTH:
                    return ValueRange.of(1, 1, getMinimumMonthLength(), getMaximumMonthLength());
                case DAY_OF_YEAR:
                    return ValueRange.of(1, getMaximumDayOfYear());
                case ALIGNED_WEEK_OF_MONTH:
                    return ValueRange.of(1, 5);
                case YEAR:
                case YEAR_OF_ERA:
                    return ValueRange.of(getMinimumYear(), getMaximumYear());
                case ERA:
                    return ValueRange.of(1, 1);
                default:
                    return field.range();
            }
        }
        return field.range();
    }

    //-----------------------------------------------------------------------
    @Override  // 覆盖以返回类型
    public HijrahDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (HijrahDate) super.resolveDate(fieldValues, resolverStyle);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查年份的有效性。
     *
     * @param prolepticYear 要检查的年份
     */
    int checkValidYear(long prolepticYear) {
        if (prolepticYear < getMinimumYear() || prolepticYear > getMaximumYear()) {
            throw new DateTimeException("无效的伊斯兰年份: " + prolepticYear);
        }
        return (int) prolepticYear;
    }

    void checkValidDayOfYear(int dayOfYear) {
        if (dayOfYear < 1 || dayOfYear > getMaximumDayOfYear()) {
            throw new DateTimeException("无效的伊斯兰年中的日: " + dayOfYear);
        }
    }

    void checkValidMonth(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("无效的伊斯兰月: " + month);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个包含从纪元日计算出的伊斯兰年、月和日的数组。
     *
     * @param epochDay  纪元日
     * @return int[0] = 年, int[1] = 月, int[2] = 日
     */
    int[] getHijrahDateInfo(int epochDay) {
        checkCalendarInit();    // 确保历法已初始化
        if (epochDay < minEpochDay || epochDay >= maxEpochDay) {
            throw new DateTimeException("伊斯兰日期超出范围");
        }

        int epochMonth = epochDayToEpochMonth(epochDay);
        int year = epochMonthToYear(epochMonth);
        int month = epochMonthToMonth(epochMonth);
        int day1 = epochMonthToEpochDay(epochMonth);
        int date = epochDay - day1; // epochDay - dayOfEpoch(year, month);

        int dateInfo[] = new int[3];
        dateInfo[0] = year;
        dateInfo[1] = month + 1; // 转换为1为基数。
        dateInfo[2] = date + 1; // 转换为1为基数。
        return dateInfo;
    }

    /**
     * 从伊斯兰年、月和日计算纪元日。
     *
     * @param prolepticYear 要表示的年份，0为起始
     * @param monthOfYear 要表示的年中的月，1为起始
     * @param dayOfMonth 要表示的月中的日，1为起始
     * @return 纪元日
     */
    long getEpochDay(int prolepticYear, int monthOfYear, int dayOfMonth) {
        checkCalendarInit();    // 确保历法已初始化
        checkValidMonth(monthOfYear);
        int epochMonth = yearToEpochMonth(prolepticYear) + (monthOfYear - 1);
        if (epochMonth < 0 || epochMonth >= hijrahEpochMonthStartDays.length) {
            throw new DateTimeException("无效的伊斯兰日期，年份: " +
                    prolepticYear +  ", 月: " + monthOfYear);
        }
        if (dayOfMonth < 1 || dayOfMonth > getMonthLength(prolepticYear, monthOfYear)) {
            throw new DateTimeException("无效的伊斯兰月中的日: " + dayOfMonth);
        }
        return epochMonthToEpochDay(epochMonth) + (dayOfMonth - 1);
    }

    /**
     * 返回年份和月份的年中的日。
     *
     * @param prolepticYear 一个历法年
     * @param month 月份，1为起始
     * @return 年中的日，1为起始
     */
    int getDayOfYear(int prolepticYear, int month) {
        return yearMonthToDayOfYear(prolepticYear, (month - 1));
    }

    /**
     * 返回年份和月份的月长度。
     *
     * @param prolepticYear 一个历法年
     * @param monthOfYear 月份，1为起始。
     * @return 月份的长度
     */
    int getMonthLength(int prolepticYear, int monthOfYear) {
        int epochMonth = yearToEpochMonth(prolepticYear) + (monthOfYear - 1);
        if (epochMonth < 0 || epochMonth >= hijrahEpochMonthStartDays.length) {
            throw new DateTimeException("无效的伊斯兰日期，年份: " +
                    prolepticYear +  ", 月: " + monthOfYear);
        }
        return epochMonthLength(epochMonth);
    }

    /**
     * 返回年长度。
     * 注意：第12个月必须存在于数据中。
     *
     * @param prolepticYear 一个历法年
     * @return 年长度（天数）
     */
    int getYearLength(int prolepticYear) {
        return yearMonthToDayOfYear(prolepticYear, 12);
    }

    /**
     * 返回支持的最小伊斯兰年份。
     *
     * @return 最小值
     */
    int getMinimumYear() {
        return epochMonthToYear(0);
    }

    /**
     * 返回支持的最大伊斯兰年份。
     *
     * @return 最大值
     */
    int getMaximumYear() {
        return epochMonthToYear(hijrahEpochMonthStartDays.length - 1) - 1;
    }

    /**
     * 返回最大月中的日。
     *
     * @return 最大月中的日
     */
    int getMaximumMonthLength() {
        return maxMonthLength;
    }

    /**
     * 返回最小最大月中的日。
     *
     * @return 最小最大月中的日
     */
    int getMinimumMonthLength() {
        return minMonthLength;
    }

    /**
     * 返回最大年中的日。
     *
     * @return 最大年中的日
     */
    int getMaximumDayOfYear() {
        return maxYearLength;
    }

    /**
     * 返回最小最大年中的日。
     *
     * @return 最小最大年中的日
     */
    int getSmallestMaximumDayOfYear() {
        return minYearLength;
    }

    /**
     * 通过在表中定位纪元日返回纪元月。纪元月是表中的索引。
     *
     * @param epochDay
     * @return 包含纪元日的月份起始元素的索引。
     */
    private int epochDayToEpochMonth(int epochDay) {
        // 二分查找
        int ndx = Arrays.binarySearch(hijrahEpochMonthStartDays, epochDay);
        if (ndx < 0) {
            ndx = -ndx - 2;
        }
        return ndx;
    }

    /**
     * 从纪元月计算伊斯兰年。
     *
     * @param epochMonth 纪元月
     * @return 伊斯兰年
     */
    private int epochMonthToYear(int epochMonth) {
        return (epochMonth + hijrahStartEpochMonth) / 12;
    }

    /**
     * 返回伊斯兰年的纪元月。
     *
     * @param year 伊斯兰年
     * @return 年份开始的纪元月。
     */
    private int yearToEpochMonth(int year) {
        return (year * 12) - hijrahStartEpochMonth;
    }

    /**
     * 从纪元月返回伊斯兰月。
     *
     * @param epochMonth 纪元月
     * @return 伊斯兰年的月份
     */
    private int epochMonthToMonth(int epochMonth) {
        return (epochMonth + hijrahStartEpochMonth) % 12;
    }

    /**
     * 返回纪元月开始的纪元日。
     *
     * @param epochMonth 纪元月
     * @return 纪元月开始的纪元日。
     */
    private int epochMonthToEpochDay(int epochMonth) {
        return hijrahEpochMonthStartDays[epochMonth];

    }

    /**
     * 返回请求的伊斯兰年和月份的年中的日。
     *
     * @param prolepticYear 伊斯兰年
     * @param month 伊斯兰月
     * @return 年中的日，从月份开始
     */
    private int yearMonthToDayOfYear(int prolepticYear, int month) {
        int epochMonthFirst = yearToEpochMonth(prolepticYear);
        return epochMonthToEpochDay(epochMonthFirst + month)
                - epochMonthToEpochDay(epochMonthFirst);
    }

    /**
     * 返回纪元月的长度。它从下一个月的开始减去请求月份的开始计算得出。
     *
     * @param epochMonth 纪元月；假设在范围内
     * @return 纪元月的长度（天数）
     */
    private int epochMonthLength(int epochMonth) {
        // 纪元月表中的最后一个条目不是月份的开始
        return hijrahEpochMonthStartDays[epochMonth + 1]
                - hijrahEpochMonthStartDays[epochMonth];
    }

    //-----------------------------------------------------------------------
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_VERSION = "version";
    private static final String KEY_ISO_START = "iso-start";

    /**
     * 从资源中返回配置属性。
     * <p>
     * 变体配置资源的默认位置是：
     * <pre>
     *   "$java.home/lib/" + 资源名称
     * </pre>
     *
     * @param resource 日历属性资源的名称
     * @return 从资源读取的属性。
     * @throws Exception 如果访问属性资源失败
     */
    private static Properties readConfigProperties(final String resource) throws Exception {
        try {
            return AccessController
                    .doPrivileged((java.security.PrivilegedExceptionAction<Properties>)
                        () -> {
                        String libDir = System.getProperty("java.home") + File.separator + "lib";
                        File file = new File(libDir, resource);
                        Properties props = new Properties();
                        try (InputStream is = new FileInputStream(file)) {
                            props.load(is);
                        }
                        return props;
                    });
        } catch (PrivilegedActionException pax) {
            throw pax.getException();
        }
    }


                /**
     * 加载并处理此日历类型的希吉拉日历属性文件。
     * 提取起始希吉拉日期和对应的 ISO 日期，并用于计算 epochDate 偏移。
     * 版本号被识别并忽略。
     * 其他所有内容都是包含每年 12 个月长度的数据。
     *
     * @throws DateTimeException 如果从资源初始化日历数据失败
     */
    private void loadCalendarData() {
        try {
            String resourceName = calendarProperties.getProperty(PROP_PREFIX + typeId);
            Objects.requireNonNull(resourceName, "Resource missing for calendar: " + PROP_PREFIX + typeId);
            Properties props = readConfigProperties(resourceName);

            Map<Integer, int[]> years = new HashMap<>();
            int minYear = Integer.MAX_VALUE;
            int maxYear = Integer.MIN_VALUE;
            String id = null;
            String type = null;
            String version = null;
            int isoStart = 0;
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String key = (String) entry.getKey();
                switch (key) {
                    case KEY_ID:
                        id = (String)entry.getValue();
                        break;
                    case KEY_TYPE:
                        type = (String)entry.getValue();
                        break;
                    case KEY_VERSION:
                        version = (String)entry.getValue();
                        break;
                    case KEY_ISO_START: {
                        int[] ymd = parseYMD((String) entry.getValue());
                        isoStart = (int) LocalDate.of(ymd[0], ymd[1], ymd[2]).toEpochDay();
                        break;
                    }
                    default:
                        try {
                            // 其他所有内容要么是年份，要么无效
                            int year = Integer.valueOf(key);
                            int[] months = parseMonths((String) entry.getValue());
                            years.put(year, months);
                            maxYear = Math.max(maxYear, year);
                            minYear = Math.min(minYear, year);
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("bad key: " + key);
                        }
                }
            }

            if (!getId().equals(id)) {
                throw new IllegalArgumentException("Configuration is for a different calendar: " + id);
            }
            if (!getCalendarType().equals(type)) {
                throw new IllegalArgumentException("Configuration is for a different calendar type: " + type);
            }
            if (version == null || version.isEmpty()) {
                throw new IllegalArgumentException("Configuration does not contain a version");
            }
            if (isoStart == 0) {
                throw new IllegalArgumentException("Configuration does not contain a ISO start date");
            }

            // 现在创建并验证由 epochMonth 索引的 epochDays 数组
            hijrahStartEpochMonth = minYear * 12;
            minEpochDay = isoStart;
            hijrahEpochMonthStartDays = createEpochMonths(minEpochDay, minYear, maxYear, years);
            maxEpochDay = hijrahEpochMonthStartDays[hijrahEpochMonthStartDays.length - 1];

            // 计算最小和最大年长度（以天为单位）。
            for (int year = minYear; year < maxYear; year++) {
                int length = getYearLength(year);
                minYearLength = Math.min(minYearLength, length);
                maxYearLength = Math.max(maxYearLength, length);
            }
        } catch (Exception ex) {
            // 记录错误并抛出 DateTimeException
            PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
            logger.severe("Unable to initialize Hijrah calendar proxy: " + typeId, ex);
            throw new DateTimeException("Unable to initialize HijrahCalendar: " + typeId, ex);
        }
    }

    /**
     * 将从 minYear 到 maxYear 的年份到月长度的映射转换为线性连续的 epochDays 数组。
     * 索引是根据年份和月份计算的 hijrahMonth，并由 minYear 偏移。每个条目的值是该月第一天对应的 epochDay。
     *
     * @param minYear 提供数据的最小年份
     * @param maxYear 提供数据的最大年份
     * @param years 从年份到 12 个月长度数组的映射
     * @return 从 min 到 max 的每个月的 epochDays 数组
     */
    private int[] createEpochMonths(int epochDay, int minYear, int maxYear, Map<Integer, int[]> years) {
        // 计算日期数组的大小
        int numMonths = (maxYear - minYear + 1) * 12 + 1;

        // 将运行的 epochDay 初始化为对应的 ISO Epoch 天
        int epochMonth = 0; // 数组 epochMonths 的索引
        int[] epochMonths = new int[numMonths];
        minMonthLength = Integer.MAX_VALUE;
        maxMonthLength = Integer.MIN_VALUE;

        // 只有完整的年份是有效的，数组中的任何零都是非法的
        for (int year = minYear; year <= maxYear; year++) {
            int[] months = years.get(year);// 必须没有空缺
            for (int month = 0; month < 12; month++) {
                int length = months[month];
                epochMonths[epochMonth++] = epochDay;

                if (length < 29 || length > 32) {
                    throw new IllegalArgumentException("Invalid month length in year: " + minYear);
                }
                epochDay += length;
                minMonthLength = Math.min(minMonthLength, length);
                maxMonthLength = Math.max(maxMonthLength, length);
            }
        }

        // 插入最终的 epochDay
        epochMonths[epochMonth++] = epochDay;

        if (epochMonth != epochMonths.length) {
            throw new IllegalStateException("Did not fill epochMonths exactly: ndx = " + epochMonth
                    + " should be " + epochMonths.length);
        }

        return epochMonths;
    }

    /**
     * 解析特定年份的属性值中的 12 个月长度。
     *
     * @param line 年份属性的值
     * @return 包含 12 个月长度的 int[12] 数组
     * @throws IllegalArgumentException 如果月份数量不是 12
     * @throws NumberFormatException 如果 12 个标记不是数字
     */
    private int[] parseMonths(String line) {
        int[] months = new int[12];
        String[] numbers = line.split("\\s");
        if (numbers.length != 12) {
            throw new IllegalArgumentException("wrong number of months on line: " + Arrays.toString(numbers) + "; count: " + numbers.length);
        }
        for (int i = 0; i < 12; i++) {
            try {
                months[i] = Integer.valueOf(numbers[i]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("bad key: " + numbers[i]);
            }
        }
        return months;
    }

    /**
     * 解析 yyyy-MM-dd 为 3 元素数组 [yyyy, mm, dd]。
     *
     * @param string 输入字符串
     * @return 包含年、月、日的 3 元素数组
     */
    private int[] parseYMD(String string) {
        // yyyy-MM-dd
        string = string.trim();
        try {
            if (string.charAt(4) != '-' || string.charAt(7) != '-') {
                throw new IllegalArgumentException("date must be yyyy-MM-dd");
            }
            int[] ymd = new int[3];
            ymd[0] = Integer.valueOf(string.substring(0, 4));
            ymd[1] = Integer.valueOf(string.substring(5, 7));
            ymd[2] = Integer.valueOf(string.substring(8, 10));
            return ymd;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("date must be yyyy-MM-dd", ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a>
     * 写入编年史。
     * @serialData
     * <pre>
     *  out.writeByte(1);     // 识别一个编年史
     *  out.writeUTF(getId());
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    @Override
    Object writeReplace() {
        return super.writeReplace();
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
