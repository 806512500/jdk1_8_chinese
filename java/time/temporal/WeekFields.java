
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.temporal;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

/**
 * 本地化的星期几、月份中的周数和年份中的周数字段的定义。
 * <p>
 * 标准周有七天，但不同文化对周的某些方面有不同的定义。此类表示周的定义，用于提供 {@link TemporalField} 实例。
 * <p>
 * WeekFields 提供五个字段：
 * {@link #dayOfWeek()}，{@link #weekOfMonth()}，{@link #weekOfYear()}，
 * {@link #weekOfWeekBasedYear()} 和 {@link #weekBasedYear()}
 * 用于从任何 {@linkplain Temporal 临时对象} 访问值。
 * <p>
 * 星期几、月份中的周数和年份中的周数的计算基于
 * {@linkplain ChronoField#YEAR 历法年}，
 * {@linkplain ChronoField#MONTH_OF_YEAR 月份}，
 * {@linkplain ChronoField#DAY_OF_MONTH 月份中的日期} 和
 * {@linkplain ChronoField#DAY_OF_WEEK ISO 星期几}，这些基于
 * {@linkplain ChronoField#EPOCH_DAY 时代日期} 和历法系统。
 * 这些值可能与 {@linkplain ChronoField#YEAR_OF_ERA 历法年} 不对齐，具体取决于历法系统。
 * <p>周的定义包括：
 * <ul>
 * <li>一周的第一天。
 * 例如，ISO-8601 标准认为周一是一周的第一天。
 * <li>第一周的最少天数。
 * 例如，ISO-8601 标准规定第一周至少需要 4 天。
 * </ul>
 * 这两个值一起允许将年或月划分为周。
 *
 * <h3>月份中的周数</h3>
 * 使用一个字段：月份中的周数。
 * 计算确保周不会跨越月份边界。
 * 月份被划分为每个周期从定义的第一天开始的周期。
 * 最早的周期如果少于最少天数则称为第 0 周，如果至少有最少天数则称为第 1 周。
 *
 * <table cellpadding="0" cellspacing="3" border="0" style="text-align: left; width: 50%;">
 * <caption>WeekFields 的示例</caption>
 * <tr><th>日期</th><td>星期几</td>
 *  <td>第一天: 周一<br>最少天数: 4</td><td>第一天: 周一<br>最少天数: 5</td></tr>
 * <tr><th>2008-12-31</th><td>周三</td>
 *  <td>12 月 2008 年的第 5 周</td><td>12 月 2008 年的第 5 周</td></tr>
 * <tr><th>2009-01-01</th><td>周四</td>
 *  <td>1 月 2009 年的第 1 周</td><td>1 月 2009 年的第 0 周</td></tr>
 * <tr><th>2009-01-04</th><td>周日</td>
 *  <td>1 月 2009 年的第 1 周</td><td>1 月 2009 年的第 0 周</td></tr>
 * <tr><th>2009-01-05</th><td>周一</td>
 *  <td>1 月 2009 年的第 2 周</td><td>1 月 2009 年的第 1 周</td></tr>
 * </table>
 *
 * <h3>年份中的周数</h3>
 * 使用一个字段：年份中的周数。
 * 计算确保周不会跨越年份边界。
 * 年份被划分为每个周期从定义的第一天开始的周期。
 * 最早的周期如果少于最少天数则称为第 0 周，如果至少有最少天数则称为第 1 周。
 *
 * <h3>基于周的年份</h3>
 * 基于周的年份使用两个字段，一个用于
 * {@link #weekOfWeekBasedYear() 基于周的年份中的周数}，一个用于
 * {@link #weekBasedYear() 基于周的年份}。在基于周的年份中，每个周只属于一个年份。基于周的年份的第一周是从第一周的第一天开始并且至少有最少天数的周。
 * 一个年份的第一周和最后一周可能包含上一个日历年或下一个日历年的日期。
 *
 * <table cellpadding="0" cellspacing="3" border="0" style="text-align: left; width: 50%;">
 * <caption>基于周的年份的 WeekFields 示例</caption>
 * <tr><th>日期</th><td>星期几</td>
 *  <td>第一天: 周一<br>最少天数: 4</td><td>第一天: 周一<br>最少天数: 5</td></tr>
 * <tr><th>2008-12-31</th><td>周三</td>
 *  <td>2009 年的第 1 周</td><td>2008 年的第 53 周</td></tr>
 * <tr><th>2009-01-01</th><td>周四</td>
 *  <td>2009 年的第 1 周</td><td>2008 年的第 53 周</td></tr>
 * <tr><th>2009-01-04</th><td>周日</td>
 *  <td>2009 年的第 1 周</td><td>2008 年的第 53 周</td></tr>
 * <tr><th>2009-01-05</th><td>周一</td>
 *  <td>2009 年的第 2 周</td><td>2009 年的第 1 周</td></tr>
 * </table>
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class WeekFields implements Serializable {
    // 实现说明
    // 查询月份中的周数或年份中的周数应返回绑定在月份/年份内的周数
    // 但是，设置周数应该是宽松的（使用加减周数）
    // 允许月份中的周数范围 [0 到 6]
    // 允许年份中的周数范围 [0 到 54]
    // 这是因为调用者不应该期望知道有效性的详细信息

    /**
     * 由第一天和最少天数组成的规则缓存。
     * 首先初始化以供 ISO 等定义使用。
     */
    private static final ConcurrentMap<String, WeekFields> CACHE = new ConcurrentHashMap<>(4, 0.75f, 2);

    /**
     * ISO-8601 定义，一周从周一开始，第一周至少有 4 天。
     * <p>
     * ISO-8601 标准定义了一个基于周的历法系统。
     * 它使用基于周的年份和基于周的年份中的周数概念来划分时间，而不是标准的年/月/日。
     * <p>
     * 注意，第一周可能从上一个日历年开始。
     * 注意，日历年的前几天可能属于上一个日历年对应的基于周的年份。
     */
    public static final WeekFields ISO = new WeekFields(DayOfWeek.MONDAY, 4);

    /**
     * 一种常见的周定义，一周从周日开始，第一周至少有 1 天。
     * <p>
     * 定义为从周日开始，每月至少有 1 天。
     * 这种周定义在美国和其他欧洲国家使用。
     */
    public static final WeekFields SUNDAY_START = WeekFields.of(DayOfWeek.SUNDAY, 1);

    /**
     * 用于加减基于周的年份的单位。
     * <p>
     * 这允许将基于周的年份数加到或从日期中减去。
     * 该单位等于 52 或 53 周。
     * 基于周的年份的估计持续时间与标准 ISO 年份的持续时间相同，为 {@code 365.2425 天}。
     * <p>
     * 加法规则将基于周的年份数加到现有的基于周的年份值上，保留基于周的年份中的周数和星期几，除非目标年份的周数太大。
     * 在这种情况下，周数将设置为该年份的最后一周，且星期几相同。
     * <p>
     * 该单位是不可变且线程安全的单例。
     */
    public static final TemporalUnit WEEK_BASED_YEARS = IsoFields.WEEK_BASED_YEARS;

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -1177360819670808121L;

    /**
     * 一周的第一天。
     */
    private final DayOfWeek firstDayOfWeek;
    /**
     * 第一周的最少天数。
     */
    private final int minimalDays;
    /**
     * 用于访问计算的 DayOfWeek 的字段。
     */
    private final transient TemporalField dayOfWeek = ComputedDayOfField.ofDayOfWeekField(this);
    /**
     * 用于访问计算的 WeekOfMonth 的字段。
     */
    private final transient TemporalField weekOfMonth = ComputedDayOfField.ofWeekOfMonthField(this);
    /**
     * 用于访问计算的 WeekOfYear 的字段。
     */
    private final transient TemporalField weekOfYear = ComputedDayOfField.ofWeekOfYearField(this);
    /**
     * 代表基于周的年份中的周数的字段。
     * <p>
     * 该字段允许查询和设置基于周的年份中的周数。
     * <p>
     * 该单位是不可变且线程安全的单例。
     */
    private final transient TemporalField weekOfWeekBasedYear = ComputedDayOfField.ofWeekOfWeekBasedYearField(this);
    /**
     * 代表基于周的年份的字段。
     * <p>
     * 该字段允许查询和设置基于周的年份。
     * <p>
     * 该单位是不可变且线程安全的单例。
     */
    private final transient TemporalField weekBasedYear = ComputedDayOfField.ofWeekBasedYearField(this);

    //-----------------------------------------------------------------------
    /**
     * 获取适用于特定地区的 {@code WeekFields} 实例。
     * <p>
     * 这将从本地化数据提供者中查找适当的值。
     *
     * @param locale  使用的地区，不为空
     * @return 周定义，不为空
     */
    public static WeekFields of(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        locale = new Locale(locale.getLanguage(), locale.getCountry());  // 消除变体

        int calDow = CalendarDataUtility.retrieveFirstDayOfWeek(locale);
        DayOfWeek dow = DayOfWeek.SUNDAY.plus(calDow - 1);
        int minDays = CalendarDataUtility.retrieveMinimalDaysInFirstWeek(locale);
        return WeekFields.of(dow, minDays);
    }

    /**
     * 从一周的第一天和最少天数获取 {@code WeekFields} 实例。
     * <p>
     * 一周的第一天定义了 ISO {@code DayOfWeek} 中的一周的第一天。
     * 第一周的最少天数定义了从一周的第一天开始，必须有多少天在一个月或一年中，周才被计为第一周。值为 1 将把一个月或一年的第一天视为第一周的一部分，而值为 7 则要求整个七天都在新的月份或年份中。
     * <p>
     * WeekFields 实例是单例；对于每个独特的 {@code firstDayOfWeek} 和 {@code minimalDaysInFirstWeek} 组合，返回相同的实例。
     *
     * @param firstDayOfWeek  一周的第一天，不为空
     * @param minimalDaysInFirstWeek  第一周的最少天数，从 1 到 7
     * @return 周定义，不为空
     * @throws IllegalArgumentException 如果最少天数值小于 1 或大于 7
     */
    public static WeekFields of(DayOfWeek firstDayOfWeek, int minimalDaysInFirstWeek) {
        String key = firstDayOfWeek.toString() + minimalDaysInFirstWeek;
        WeekFields rules = CACHE.get(key);
        if (rules == null) {
            rules = new WeekFields(firstDayOfWeek, minimalDaysInFirstWeek);
            CACHE.putIfAbsent(key, rules);
            rules = CACHE.get(key);
        }
        return rules;
    }

    //-----------------------------------------------------------------------
    /**
     * 创建定义的实例。
     *
     * @param firstDayOfWeek  一周的第一天，不为空
     * @param minimalDaysInFirstWeek  第一周的最少天数，从 1 到 7
     * @throws IllegalArgumentException 如果最少天数值无效
     */
    private WeekFields(DayOfWeek firstDayOfWeek, int minimalDaysInFirstWeek) {
        Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek");
        if (minimalDaysInFirstWeek < 1 || minimalDaysInFirstWeek > 7) {
            throw new IllegalArgumentException("最少天数值无效");
        }
        this.firstDayOfWeek = firstDayOfWeek;
        this.minimalDays = minimalDaysInFirstWeek;
    }


    //-----------------------------------------------------------------------
    /**
     * 从流中恢复 WeekFields 的状态。
     * 检查值是否有效。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 如果序列化的对象有无效的
     *     firstDayOfWeek 或 minimalDays 值。
     * @throws ClassNotFoundException 如果类无法解析
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException, InvalidObjectException
    {
        s.defaultReadObject();
        if (firstDayOfWeek == null) {
            throw new InvalidObjectException("firstDayOfWeek 为 null");
        }

        if (minimalDays < 1 || minimalDays > 7) {
            throw new InvalidObjectException("最小天数无效");
        }
    }

    /**
     * 返回与 {@code firstDayOfWeek} 和 {@code minimalDays} 关联的
     * 单例 WeekFields。
     * @return 与 firstDayOfWeek 和 minimalDays 关联的单例 WeekFields。
     * @throws InvalidObjectException 如果序列化的对象有无效的
     *     firstDayOfWeek 或 minimalDays 值。
     */
    private Object readResolve() throws InvalidObjectException {
        try {
            return WeekFields.of(firstDayOfWeek, minimalDays);
        } catch (IllegalArgumentException iae) {
            throw new InvalidObjectException("无效的序列化 WeekFields: " + iae.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取一周的第一天。
     * <p>
     * 一周的第一天因文化而异。
     * 例如，美国使用星期日，而法国和 ISO-8601 标准使用星期一。
     * 此方法返回使用标准 {@code DayOfWeek} 枚举的第一天。
     *
     * @return 一周的第一天，不为 null
     */
    public DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * 获取一个月或一年第一周的最小天数。
     * <p>
     * 定义一个月或一年第一周的天数因文化而异。
     * 例如，ISO-8601 要求至少有 4 天（超过一周的一半）才能算作第一周。
     *
     * @return 一个月或一年第一周的最小天数，从 1 到 7
     */
    public int getMinimalDaysInFirstWeek() {
        return minimalDays;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个字段，用于根据此 {@code WeekFields} 访问一周中的某一天。
     * <p>
     * 这类似于 {@link ChronoField#DAY_OF_WEEK}，但使用基于此 {@code WeekFields} 的
     * 一周中的某一天的值。
     * 一周中的某一天从 1 到 7 编号，其中
     * {@link #getFirstDayOfWeek() 一周的第一天} 被分配值 1。
     * <p>
     * 例如，如果一周的第一天是星期日，那么星期日的值为 1，其他天从星期一的 2 到星期六的 7。
     * <p>
     * 在解析的解析阶段，本地化的一周中的某一天将被转换为标准的 {@code ChronoField} 一周中的某一天。
     * 一周中的某一天必须在 1 到 7 的有效范围内。
     * 本类中的其他字段使用标准化的一周中的某一天构建日期。
     *
     * @return 提供访问本地化编号的一周中的某一天的字段，不为 null
     */
    public TemporalField dayOfWeek() {
        return dayOfWeek;
    }

    /**
     * 返回一个字段，用于根据此 {@code WeekFields} 访问一个月中的某一周。
     * <p>
     * 这表示一个月中某一周的概念，其中一周从固定的一周中的某一天开始，例如星期一。
     * 此字段通常与 {@link WeekFields#dayOfWeek()} 一起使用。
     * <p>
     * 第一周 (1) 是从 {@link WeekFields#getFirstDayOfWeek} 开始的一周，
     * 其中至少有 {@link WeekFields#getMinimalDaysInFirstWeek()} 天在该月。
     * 因此，第一周可能在该月开始前最多 {@code minDays} 天开始。
     * 如果第一周在该月开始后开始，则该月开始前的时期为第零周 (0)。
     * <p>
     * 例如：<br>
     * - 如果该月的第一天是星期一，第一周从 1 日开始，没有第零周<br>
     * - 如果该月的第二天是星期一，第一周从 2 日开始，1 日在第零周<br>
     * - 如果该月的第四天是星期一，第一周从 4 日开始，1 日到 3 日在第零周<br>
     * - 如果该月的第五天是星期一，第二周从 5 日开始，1 日到 4 日在第一周<br>
     * <p>
     * 此字段可以与任何日历系统一起使用。
     * <p>
     * 在解析的解析阶段，可以从年份、月中的某一周、年中的某个月和一周中的某一天创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有四个字段都
     * 会验证其有效值范围。会验证周中的某一周字段以确保结果月份是请求的月份。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有四个字段都
     * 会验证其有效值范围。周中的某一周字段从 0 到 6 验证，这意味着结果日期可能在指定的月份之外。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，年份和一周中的某一天
     * 会验证其有效值范围。结果日期的计算等同于以下四阶段方法。
     * 首先，在请求年份的 1 月第一周的第一天创建日期。
     * 然后取年中的某个月，减去一，将该月数加到日期上。
     * 然后取周中的某一周，减去一，将该周数加到日期上。
     * 最后，调整到本地化一周中的正确一天。
     *
     * @return 提供访问月中的某一周的字段，不为 null
     */
    public TemporalField weekOfMonth() {
        return weekOfMonth;
    }

    /**
     * 返回一个字段，用于根据此 {@code WeekFields} 访问一年中的某一周。
     * <p>
     * 这表示一年中某一周的概念，其中一周从固定的一周中的某一天开始，例如星期一。
     * 此字段通常与 {@link WeekFields#dayOfWeek()} 一起使用。
     * <p>
     * 第一周 (1) 是从 {@link WeekFields#getFirstDayOfWeek} 开始的一周，
     * 其中至少有 {@link WeekFields#getMinimalDaysInFirstWeek()} 天在该年。
     * 因此，第一周可能在该年开始前最多 {@code minDays} 天开始。
     * 如果第一周在该年开始后开始，则该年开始前的时期为第零周 (0)。
     * <p>
     * 例如：<br>
     * - 如果该年的第一天是星期一，第一周从 1 日开始，没有第零周<br>
     * - 如果该年的第二天是星期一，第一周从 2 日开始，1 日在第零周<br>
     * - 如果该年的第四天是星期一，第一周从 4 日开始，1 日到 3 日在第零周<br>
     * - 如果该年的第五天是星期一，第二周从 5 日开始，1 日到 4 日在第一周<br>
     * <p>
     * 此字段可以与任何日历系统一起使用。
     * <p>
     * 在解析的解析阶段，可以从年份、年中的某一周和一周中的某一天创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有三个字段都
     * 会验证其有效值范围。会验证年中的某一周字段以确保结果年份是请求的年份。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有三个字段都
     * 会验证其有效值范围。年中的某一周字段从 0 到 54 验证，这意味着结果日期可能在指定的年份之外。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，年份和一周中的某一天
     * 会验证其有效值范围。结果日期的计算等同于以下三阶段方法。
     * 首先，在请求年份的第一周的第一天创建日期。
     * 然后取年中的某一周，减去一，将该周数加到日期上。
     * 最后，调整到本地化一周中的正确一天。
     *
     * @return 提供访问年中的某一周的字段，不为 null
     */
    public TemporalField weekOfYear() {
        return weekOfYear;
    }

    /**
     * 返回一个字段，用于根据此 {@code WeekFields} 访问基于周的年中的某一周。
     * <p>
     * 这表示一年中某一周的概念，其中一周从固定的一周中的某一天开始，例如星期一，且每周属于一个年份。
     * 此字段通常与 {@link WeekFields#dayOfWeek()} 和
     * {@link WeekFields#weekBasedYear()} 一起使用。
     * <p>
     * 第一周 (1) 是从 {@link WeekFields#getFirstDayOfWeek} 开始的一周，
     * 其中至少有 {@link WeekFields#getMinimalDaysInFirstWeek()} 天在该年。
     * 如果第一周在该年开始后开始，则该年开始前的时期
     * 在上一年的最后一周。
     * <p>
     * 例如：<br>
     * - 如果该年的第一天是星期一，第一周从 1 日开始<br>
     * - 如果该年的第二天是星期一，第一周从 2 日开始，
     *   1 日在上一年的最后一周<br>
     * - 如果该年的第四天是星期一，第一周从 4 日开始，
     *   1 日到 3 日在上一年的最后一周<br>
     * - 如果该年的第五天是星期一，第二周从 5 日开始，
     *   1 日到 4 日在第一周<br>
     * <p>
     * 此字段可以与任何日历系统一起使用。
     * <p>
     * 在解析的解析阶段，可以从基于周的年份、年中的某一周和一周中的某一天创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有三个字段都
     * 会验证其有效值范围。会验证年中的某一周字段以确保结果基于周的年份是
     * 请求的基于周的年份。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有三个字段都
     * 会验证其有效值范围。基于周的年中的某一周字段从 1 到 53 验证，这意味着结果日期可能在
     * 指定的基于周的年份之后。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，年份和一周中的某一天
     * 会验证其有效值范围。结果日期的计算等同于以下三阶段方法。
     * 首先，在请求的基于周的年份的第一周的第一天创建日期。
     * 然后取基于周的年中的某一周，减去一，将该周数加到日期上。
     * 最后，调整到本地化一周中的正确一天。
     *
     * @return 提供访问基于周的年中的某一周的字段，不为 null
     */
    public TemporalField weekOfWeekBasedYear() {
        return weekOfWeekBasedYear;
    }

    /**
     * 返回一个字段，用于根据此 {@code WeekFields} 访问基于周的年份。
     * <p>
     * 这表示一周从固定的一周中的某一天开始，例如星期一，且每周属于一个年份的年份。
     * 此字段通常与 {@link WeekFields#dayOfWeek()} 和
     * {@link WeekFields#weekOfWeekBasedYear()} 一起使用。
     * <p>
     * 第一周 (1) 是从 {@link WeekFields#getFirstDayOfWeek} 开始的一周，
     * 其中至少有 {@link WeekFields#getMinimalDaysInFirstWeek()} 天在该年。
     * 因此，第一周可能在该年开始前开始。
     * 如果第一周在该年开始后开始，则该年开始前的时期
     * 在上一年的最后一周。
     * <p>
     * 此字段可以与任何日历系统一起使用。
     * <p>
     * 在解析的解析阶段，可以从基于周的年份、年中的某一周和一周中的某一天创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有三个字段都
     * 会验证其有效值范围。会验证年中的某一周字段以确保结果基于周的年份是
     * 请求的基于周的年份。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有三个字段都
     * 会验证其有效值范围。基于周的年中的某一周字段从 1 到 53 验证，这意味着结果日期可能在
     * 指定的基于周的年份之后。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，年份和一周中的某一天
     * 会验证其有效值范围。结果日期的计算等同于以下三阶段方法。
     * 首先，在请求的基于周的年份的第一周的第一天创建日期。
     * 然后取基于周的年中的某一周，减去一，将该周数加到日期上。
     * 最后，调整到本地化一周中的正确一天。
     *
     * @return 提供访问基于周的年份的字段，不为 null
     */
    public TemporalField weekBasedYear() {
        return weekBasedYear;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此 {@code WeekFields} 是否等于指定的对象。
     * <p>
     * 比较基于规则的整个状态，即
     * 一周的第一天和最小天数。
     *
     * @param object 要比较的其他规则，null 返回 false
     * @return 如果此规则等于指定的规则，则返回 true
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof WeekFields) {
            return hashCode() == object.hashCode();
        }
        return false;
    }

    /**
     * 此 {@code WeekFields} 的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return firstDayOfWeek.ordinal() * 7 + minimalDays;
    }

    //-----------------------------------------------------------------------
    /**
     * 此 {@code WeekFields} 实例的字符串表示形式。
     *
     * @return 字符串表示形式，不为 null
     */
    @Override
    public String toString() {
        return "WeekFields[" + firstDayOfWeek + ',' + minimalDays + ']';
    }

    //-----------------------------------------------------------------------
    /**
     * 计算 DayOfWeek、WeekOfMonth 和 WeekOfYear 的字段类型，
     * 基于 WeekFields。
     * 每个不同的 WeekFields 组合（一周的开始和最小天数）都需要一个单独的 Field 实例。
     * 提供了创建 DayOfWeek、WeekOfMonth 和 WeekOfYear 字段的构造函数。
     */
    static class ComputedDayOfField implements TemporalField {


        /**
         * 返回一个字段以访问基于 WeekFields 的星期几。
         * <p>
         * 第一周的第一天的 WeekDefintion 与 ISO DAY_OF_WEEK 字段一起使用来计算周边界。
         */
        static ComputedDayOfField ofDayOfWeekField(WeekFields weekDef) {
            return new ComputedDayOfField("DayOfWeek", weekDef, DAYS, WEEKS, DAY_OF_WEEK_RANGE);
        }

        /**
         * 返回一个字段以访问基于 WeekFields 的月中的周。
         * @see WeekFields#weekOfMonth()
         */
        static ComputedDayOfField ofWeekOfMonthField(WeekFields weekDef) {
            return new ComputedDayOfField("WeekOfMonth", weekDef, WEEKS, MONTHS, WEEK_OF_MONTH_RANGE);
        }

        /**
         * 返回一个字段以访问基于 WeekFields 的年中的周。
         * @see WeekFields#weekOfYear()
         */
        static ComputedDayOfField ofWeekOfYearField(WeekFields weekDef) {
            return new ComputedDayOfField("WeekOfYear", weekDef, WEEKS, YEARS, WEEK_OF_YEAR_RANGE);
        }

        /**
         * 返回一个字段以访问基于 WeekFields 的基于周的年中的周。
         * @see WeekFields#weekOfWeekBasedYear()
         */
        static ComputedDayOfField ofWeekOfWeekBasedYearField(WeekFields weekDef) {
            return new ComputedDayOfField("WeekOfWeekBasedYear", weekDef, WEEKS, IsoFields.WEEK_BASED_YEARS, WEEK_OF_WEEK_BASED_YEAR_RANGE);
        }

        /**
         * 返回一个字段以访问基于 WeekFields 的基于周的年。
         * @see WeekFields#weekBasedYear()
         */
        static ComputedDayOfField ofWeekBasedYearField(WeekFields weekDef) {
            return new ComputedDayOfField("WeekBasedYear", weekDef, IsoFields.WEEK_BASED_YEARS, FOREVER, ChronoField.YEAR.range());
        }

        /**
         * 返回一个新的基于周的年日期，包括年份、周中的周和星期几。
         * @param chrono 新日期的历法
         * @param yowby 基于周的年中的年份
         * @param wowby 基于周的年中的周
         * @param dow 星期几
         * @return 请求的年份、周中的周和星期几的 ChronoLocalDate
         */
        private ChronoLocalDate ofWeekBasedYear(Chronology chrono,
                int yowby, int wowby, int dow) {
            ChronoLocalDate date = chrono.date(yowby, 1, 1);
            int ldow = localizedDayOfWeek(date);
            int offset = startOfWeekOffset(1, ldow);

            // 将周中的周限制在同一年内
            int yearLen = date.lengthOfYear();
            int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
            wowby = Math.min(wowby, newYearWeek - 1);

            int days = -offset + (dow - 1) + (wowby - 1) * 7;
            return date.plus(days, DAYS);
        }

        private final String name;
        private final WeekFields weekDef;
        private final TemporalUnit baseUnit;
        private final TemporalUnit rangeUnit;
        private final ValueRange range;

        private ComputedDayOfField(String name, WeekFields weekDef, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
            this.name = name;
            this.weekDef = weekDef;
            this.baseUnit = baseUnit;
            this.rangeUnit = rangeUnit;
            this.range = range;
        }

        private static final ValueRange DAY_OF_WEEK_RANGE = ValueRange.of(1, 7);
        private static final ValueRange WEEK_OF_MONTH_RANGE = ValueRange.of(0, 1, 4, 6);
        private static final ValueRange WEEK_OF_YEAR_RANGE = ValueRange.of(0, 1, 52, 54);
        private static final ValueRange WEEK_OF_WEEK_BASED_YEAR_RANGE = ValueRange.of(1, 52, 53);

        @Override
        public long getFrom(TemporalAccessor temporal) {
            if (rangeUnit == WEEKS) {  // 星期几
                return localizedDayOfWeek(temporal);
            } else if (rangeUnit == MONTHS) {  // 月中的周
                return localizedWeekOfMonth(temporal);
            } else if (rangeUnit == YEARS) {  // 年中的周
                return localizedWeekOfYear(temporal);
            } else if (rangeUnit == WEEK_BASED_YEARS) {
                return localizedWeekOfWeekBasedYear(temporal);
            } else if (rangeUnit == FOREVER) {
                return localizedWeekBasedYear(temporal);
            } else {
                throw new IllegalStateException("unreachable, rangeUnit: " + rangeUnit + ", this: " + this);
            }
        }

        private int localizedDayOfWeek(TemporalAccessor temporal) {
            int sow = weekDef.getFirstDayOfWeek().getValue();
            int isoDow = temporal.get(DAY_OF_WEEK);
            return Math.floorMod(isoDow - sow, 7) + 1;
        }

        private int localizedDayOfWeek(int isoDow) {
            int sow = weekDef.getFirstDayOfWeek().getValue();
            return Math.floorMod(isoDow - sow, 7) + 1;
        }

        private long localizedWeekOfMonth(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int dom = temporal.get(DAY_OF_MONTH);
            int offset = startOfWeekOffset(dom, dow);
            return computeWeek(offset, dom);
        }

        private long localizedWeekOfYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            return computeWeek(offset, doy);
        }

        /**
         * 返回 temporal 的基于周的年份。
         * 年份可能是上一年、当前年或下一年。
         * @param temporal 任何历法的日期，不为空
         * @return 日期的基于周的年份
         */
        private int localizedWeekBasedYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int year = temporal.get(YEAR);
            int doy = temporal.get(DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                // 日期在上一年的最后一周；返回上一年
                return year - 1;
            } else {
                // 如果接近年底，使用更高精度的逻辑
                // 检查年中的日期是否属于下一年的部分周
                ValueRange dayRange = temporal.range(DAY_OF_YEAR);
                int yearLen = (int)dayRange.getMaximum();
                int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
                if (week >= newYearWeek) {
                    return year + 1;
                }
            }
            return year;
        }

        /**
         * 返回 temporal 的基于周的年中的周。
         * 周可能是上一年、当前年或下一年的一部分，具体取决于周的开始和最小天数。
         * @param temporal 任何历法的日期
         * @return 年中的周
         * @see #localizedWeekBasedYear(java.time.temporal.TemporalAccessor)
         */
        private int localizedWeekOfWeekBasedYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                // 日期在上一年的最后一周
                // 从上一年的最后一天重新计算
                ChronoLocalDate date = Chronology.from(temporal).date(temporal);
                date = date.minus(doy, DAYS);   // 回到上一年
                return localizedWeekOfWeekBasedYear(date);
            } else if (week > 50) {
                // 如果接近年底，使用更高精度的逻辑
                // 检查年中的日期是否属于下一年的部分周
                ValueRange dayRange = temporal.range(DAY_OF_YEAR);
                int yearLen = (int)dayRange.getMaximum();
                int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
                if (week >= newYearWeek) {
                    // 与下一年的周重叠；减少到下一年的周
                    week = week - newYearWeek + 1;
                }
            }
            return week;
        }

        /**
         * 返回一个偏移量，以将周的开始与月中的天或年中的天对齐。
         *
         * @param day 1 到无穷大的天
         * @param dow 该天的星期几；1 到 7
         * @return 一个偏移量，以将天与第一个“完整”周的开始对齐
         */
        private int startOfWeekOffset(int day, int dow) {
            // 第一周中与星期几对应的天的偏移量（零起始）
            int weekStart = Math.floorMod(day - dow, 7);
            int offset = -weekStart;
            if (weekStart + 1 > weekDef.getMinimalDaysInFirstWeek()) {
                // 上一周在当前月中有足够的天数构成一个“周”
                offset = 7 - weekStart;
            }
            return offset;
        }

        /**
         * 从参考天和参考星期几计算周号。
         *
         * @param offset 从 {@link #startOfWeekOffset} 获取的偏移量，以将日期与周的开始对齐
         * @param day 要计算周号的天
         * @return 周号，其中 0 用于部分周，1 用于第一个完整周
         */
        private int computeWeek(int offset, int day) {
            return ((7 + offset + (day - 1)) / 7);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            // 检查新值并获取字段的旧值
            int newVal = range.checkValidIntValue(newValue, this);  // 宽容检查范围
            int currentVal = temporal.get(this);
            if (newVal == currentVal) {
                return temporal;
            }

            if (rangeUnit == FOREVER) {     // 替换基于周的年的年份
                // 使用相同的历法创建一个新的日期对象，
                // 所需的年份和相同的周和星期几。
                int idow = temporal.get(weekDef.dayOfWeek);
                int wowby = temporal.get(weekDef.weekOfWeekBasedYear);
                return (R) ofWeekBasedYear(Chronology.from(temporal), (int)newValue, wowby, idow);
            } else {
                // 计算差异并使用字段的基本单位添加该差异
                return (R) temporal.plus(newVal - currentVal, baseUnit);
            }
        }

        @Override
        public ChronoLocalDate resolve(
                Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
            final long value = fieldValues.get(this);
            final int newValue = Math.toIntExact(value);  // 较宽的限制使溢出检查更轻松
            // 首先将本地星期几转换为 ISO 星期几
            // 先做这个处理，以处理 ISO 和本地星期几都被解析且可能不匹配的情况
            // 星期几总是严格的，因为两个不同的星期几值会使宽容模式复杂化
            if (rangeUnit == WEEKS) {  // 星期几
                final int checkedValue = range.checkValidIntValue(value, this);  // 不宽容，因为太复杂
                final int startDow = weekDef.getFirstDayOfWeek().getValue();
                long isoDow = Math.floorMod((startDow - 1) + (checkedValue - 1), 7) + 1;
                fieldValues.remove(this);
                fieldValues.put(DAY_OF_WEEK, isoDow);
                return null;
            }

            // 只有在存在 ISO 星期几的情况下才能构建日期
            if (fieldValues.containsKey(DAY_OF_WEEK) == false) {
                return null;
            }
            int isoDow = DAY_OF_WEEK.checkValidIntValue(fieldValues.get(DAY_OF_WEEK));
            int dow = localizedDayOfWeek(isoDow);

            // 构建日期
            Chronology chrono = Chronology.from(partialTemporal);
            if (fieldValues.containsKey(YEAR)) {
                int year = YEAR.checkValidIntValue(fieldValues.get(YEAR));  // 验证
                if (rangeUnit == MONTHS && fieldValues.containsKey(MONTH_OF_YEAR)) {  // 月中的周
                    long month = fieldValues.get(MONTH_OF_YEAR);  // 尚未验证
                    return resolveWoM(fieldValues, chrono, year, month, newValue, dow, resolverStyle);
                }
                if (rangeUnit == YEARS) {  // 年中的周
                    return resolveWoY(fieldValues, chrono, year, newValue, dow, resolverStyle);
                }
            } else if ((rangeUnit == WEEK_BASED_YEARS || rangeUnit == FOREVER) &&
                    fieldValues.containsKey(weekDef.weekBasedYear) &&
                    fieldValues.containsKey(weekDef.weekOfWeekBasedYear)) { // 基于周的年中的周和基于周的年的年份
                return resolveWBY(fieldValues, chrono, dow, resolverStyle);
            }
            return null;
        }

        private ChronoLocalDate resolveWoM(
                Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long month, long wom, int localDow, ResolverStyle resolverStyle) {
            ChronoLocalDate date;
            if (resolverStyle == ResolverStyle.LENIENT) {
                date = chrono.date(year, 1, 1).plus(Math.subtractExact(month, 1), MONTHS);
                long weeks = Math.subtractExact(wom, localizedWeekOfMonth(date));
                int days = localDow - localizedDayOfWeek(date);  // 安全，不会溢出
                date = date.plus(Math.addExact(Math.multiplyExact(weeks, 7), days), DAYS);
            } else {
                int monthValid = MONTH_OF_YEAR.checkValidIntValue(month);  // 验证
                date = chrono.date(year, monthValid, 1);
                int womInt = range.checkValidIntValue(wom, this);  // 验证
                int weeks = (int) (womInt - localizedWeekOfMonth(date));  // 安全，不会溢出
                int days = localDow - localizedDayOfWeek(date);  // 安全，不会溢出
                date = date.plus(weeks * 7 + days, DAYS);
                if (resolverStyle == ResolverStyle.STRICT && date.getLong(MONTH_OF_YEAR) != month) {
                    throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
                }
            }
            fieldValues.remove(this);
            fieldValues.remove(YEAR);
            fieldValues.remove(MONTH_OF_YEAR);
            fieldValues.remove(DAY_OF_WEEK);
            return date;
        }


                    private ChronoLocalDate resolveWoY(
                Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long woy, int localDow, ResolverStyle resolverStyle) {
            ChronoLocalDate date = chrono.date(year, 1, 1);
            if (resolverStyle == ResolverStyle.LENIENT) {
                long weeks = Math.subtractExact(woy, localizedWeekOfYear(date));
                int days = localDow - localizedDayOfWeek(date);  // 安全，不会溢出
                date = date.plus(Math.addExact(Math.multiplyExact(weeks, 7), days), DAYS);
            } else {
                int womInt = range.checkValidIntValue(woy, this);  // 验证
                int weeks = (int) (womInt - localizedWeekOfYear(date));  // 安全，不会溢出
                int days = localDow - localizedDayOfWeek(date);  // 安全，不会溢出
                date = date.plus(weeks * 7 + days, DAYS);
                if (resolverStyle == ResolverStyle.STRICT && date.getLong(YEAR) != year) {
                    throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的年份");
                }
            }
            fieldValues.remove(this);
            fieldValues.remove(YEAR);
            fieldValues.remove(DAY_OF_WEEK);
            return date;
        }

        private ChronoLocalDate resolveWBY(
                Map<TemporalField, Long> fieldValues, Chronology chrono, int localDow, ResolverStyle resolverStyle) {
            int yowby = weekDef.weekBasedYear.range().checkValidIntValue(
                    fieldValues.get(weekDef.weekBasedYear), weekDef.weekBasedYear);
            ChronoLocalDate date;
            if (resolverStyle == ResolverStyle.LENIENT) {
                date = ofWeekBasedYear(chrono, yowby, 1, localDow);
                long wowby = fieldValues.get(weekDef.weekOfWeekBasedYear);
                long weeks = Math.subtractExact(wowby, 1);
                date = date.plus(weeks, WEEKS);
            } else {
                int wowby = weekDef.weekOfWeekBasedYear.range().checkValidIntValue(
                        fieldValues.get(weekDef.weekOfWeekBasedYear), weekDef.weekOfWeekBasedYear);  // 验证
                date = ofWeekBasedYear(chrono, yowby, wowby, localDow);
                if (resolverStyle == ResolverStyle.STRICT && localizedWeekBasedYear(date) != yowby) {
                    throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的周基年");
                }
            }
            fieldValues.remove(this);
            fieldValues.remove(weekDef.weekBasedYear);
            fieldValues.remove(weekDef.weekOfWeekBasedYear);
            fieldValues.remove(DAY_OF_WEEK);
            return date;
        }

        //-----------------------------------------------------------------------
        @Override
        public String getDisplayName(Locale locale) {
            Objects.requireNonNull(locale, "locale");
            if (rangeUnit == YEARS) {  // 仅对周数有值
                LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                        .getLocaleResources(locale);
                ResourceBundle rb = lr.getJavaTimeFormatData();
                return rb.containsKey("field.week") ? rb.getString("field.week") : name;
            }
            return name;
        }

        @Override
        public TemporalUnit getBaseUnit() {
            return baseUnit;
        }

        @Override
        public TemporalUnit getRangeUnit() {
            return rangeUnit;
        }

        @Override
        public boolean isDateBased() {
            return true;
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public ValueRange range() {
            return range;
        }

        //-----------------------------------------------------------------------
        @Override
        public boolean isSupportedBy(TemporalAccessor temporal) {
            if (temporal.isSupported(DAY_OF_WEEK)) {
                if (rangeUnit == WEEKS) {  // 周中的某一天
                    return true;
                } else if (rangeUnit == MONTHS) {  // 月中的某周
                    return temporal.isSupported(DAY_OF_MONTH);
                } else if (rangeUnit == YEARS) {  // 年中的某周
                    return temporal.isSupported(DAY_OF_YEAR);
                } else if (rangeUnit == WEEK_BASED_YEARS) {
                    return temporal.isSupported(DAY_OF_YEAR);
                } else if (rangeUnit == FOREVER) {
                    return temporal.isSupported(YEAR);
                }
            }
            return false;
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (rangeUnit == ChronoUnit.WEEKS) {  // 周中的某一天
                return range;
            } else if (rangeUnit == MONTHS) {  // 月中的某周
                return rangeByWeek(temporal, DAY_OF_MONTH);
            } else if (rangeUnit == YEARS) {  // 年中的某周
                return rangeByWeek(temporal, DAY_OF_YEAR);
            } else if (rangeUnit == WEEK_BASED_YEARS) {
                return rangeWeekOfWeekBasedYear(temporal);
            } else if (rangeUnit == FOREVER) {
                return YEAR.range();
            } else {
                throw new IllegalStateException("无法到达，rangeUnit: " + rangeUnit + ", this: " + this);
            }
        }

        /**
         * 将字段范围映射到周范围
         * @param temporal 临时对象
         * @param field 要获取范围的字段
         * @return 调整为周的 ValueRange。
         */
        private ValueRange rangeByWeek(TemporalAccessor temporal, TemporalField field) {
            int dow = localizedDayOfWeek(temporal);
            int offset = startOfWeekOffset(temporal.get(field), dow);
            ValueRange fieldRange = temporal.range(field);
            return ValueRange.of(computeWeek(offset, (int) fieldRange.getMinimum()),
                    computeWeek(offset, (int) fieldRange.getMaximum()));
        }

        /**
         * 将字段范围映射到周年的周范围。
         * @param temporal 临时对象
         * @return 调整为周的 ValueRange。
         */
        private ValueRange rangeWeekOfWeekBasedYear(TemporalAccessor temporal) {
            if (!temporal.isSupported(DAY_OF_YEAR)) {
                return WEEK_OF_YEAR_RANGE;
            }
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                // 该天在前一年的最后一周
                // 从前一年的最后一天重新计算
                ChronoLocalDate date = Chronology.from(temporal).date(temporal);
                date = date.minus(doy + 7, DAYS);   // 回到前一年
                return rangeWeekOfWeekBasedYear(date);
            }
            // 检查一年中的某天是否在与下一年相关的部分周内
            ValueRange dayRange = temporal.range(DAY_OF_YEAR);
            int yearLen = (int)dayRange.getMaximum();
            int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());

            if (week >= newYearWeek) {
                // 与下一年的周重叠；从下一年的某周重新计算
                ChronoLocalDate date = Chronology.from(temporal).date(temporal);
                date = date.plus(yearLen - doy + 1 + 7, ChronoUnit.DAYS);
                return rangeWeekOfWeekBasedYear(date);
            }
            return ValueRange.of(1, newYearWeek-1);
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            return name + "[" + weekDef.toString() + "]";
        }
    }
}
