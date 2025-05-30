
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
package java.time.temporal;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.ERAS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.HALF_DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MICROS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

/**
 * 一组标准字段。
 * <p>
 * 这组字段提供基于字段的访问，用于操作日期、时间或日期时间。
 * 标准字段集可以通过实现 {@link TemporalField} 进行扩展。
 * <p>
 * 这些字段旨在适用于多种日历系统。
 * 例如，大多数非 ISO 日历系统定义日期为年、月和日，只是规则略有不同。
 * 每个字段的文档解释了其操作方式。
 *
 * @implSpec
 * 这是一个最终的、不可变的和线程安全的枚举。
 *
 * @since 1.8
 */
public enum ChronoField implements TemporalField {

    /**
     * 纳秒。
     * <p>
     * 这是秒内的纳秒数，从 0 到 999,999,999。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示纳秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_MINUTE}、
     * {@link #SECOND_OF_DAY} 或 {@link #INSTANT_SECONDS} 的值，则填充未知精度的零。
     * <p>
     * 当此字段用于设置值时，应设置尽可能多的精度，使用整数除法去除多余的精度。
     * 例如，如果 {@code TemporalAccessor} 存储时间到毫秒精度，则纳秒必须除以 1,000,000，然后替换毫秒。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该字段与 {@code MILLI_OF_SECOND} 和 {@code MICRO_OF_SECOND} 一起解析。
     */
    NANO_OF_SECOND("NanoOfSecond", NANOS, SECONDS, ValueRange.of(0, 999_999_999)),
    /**
     * 纳秒。
     * <p>
     * 这是天内的纳秒数，从 0 到 (24 * 60 * 60 * 1,000,000,000) - 1。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示纳秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_DAY} 的值，则填充未知精度的零。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该值被拆分为 {@code NANO_OF_SECOND}、{@code SECOND_OF_MINUTE}、
     * {@code MINUTE_OF_HOUR} 和 {@code HOUR_OF_DAY} 字段。
     */
    NANO_OF_DAY("NanoOfDay", NANOS, DAYS, ValueRange.of(0, 86400L * 1000_000_000L - 1)),
    /**
     * 微秒。
     * <p>
     * 这是秒内的微秒数，从 0 到 999,999。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示微秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_MINUTE}、
     * {@link #SECOND_OF_DAY} 或 {@link #INSTANT_SECONDS} 的值，则填充未知精度的零。
     * <p>
     * 当此字段用于设置值时，其行为应与设置 {@link #NANO_OF_SECOND} 时相同，但值乘以 1,000。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该字段与 {@code MILLI_OF_SECOND} 一起解析以生成 {@code NANO_OF_SECOND}。
     */
    MICRO_OF_SECOND("MicroOfSecond", MICROS, SECONDS, ValueRange.of(0, 999_999)),
    /**
     * 微秒。
     * <p>
     * 这是天内的微秒数，从 0 到 (24 * 60 * 60 * 1,000,000) - 1。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示微秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_DAY} 的值，则填充未知精度的零。
     * <p>
     * 当此字段用于设置值时，其行为应与设置 {@link #NANO_OF_DAY} 时相同，但值乘以 1,000。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该值被拆分为 {@code MICRO_OF_SECOND}、{@code SECOND_OF_MINUTE}、
     * {@code MINUTE_OF_HOUR} 和 {@code HOUR_OF_DAY} 字段。
     */
    MICRO_OF_DAY("MicroOfDay", MICROS, DAYS, ValueRange.of(0, 86400L * 1000_000L - 1)),
    /**
     * 毫秒。
     * <p>
     * 这是秒内的毫秒数，从 0 到 999。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示毫秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_MINUTE}、
     * {@link #SECOND_OF_DAY} 或 {@link #INSTANT_SECONDS} 的值，则填充未知精度的零。
     * <p>
     * 当此字段用于设置值时，其行为应与设置 {@link #NANO_OF_SECOND} 时相同，但值乘以 1,000,000。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该字段与 {@code MICRO_OF_SECOND} 一起解析以生成 {@code NANO_OF_SECOND}。
     */
    MILLI_OF_SECOND("MilliOfSecond", MILLIS, SECONDS, ValueRange.of(0, 999)),
    /**
     * 毫秒。
     * <p>
     * 这是天内的毫秒数，从 0 到 (24 * 60 * 60 * 1,000) - 1。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 此字段用于表示毫秒，处理秒的任何部分。
     * {@code TemporalAccessor} 的实现应提供此字段的值，如果它们可以返回 {@link #SECOND_OF_DAY} 的值，则填充未知精度的零。
     * <p>
     * 当此字段用于设置值时，其行为应与设置 {@link #NANO_OF_DAY} 时相同，但值乘以 1,000,000。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该值被拆分为 {@code MILLI_OF_SECOND}、{@code SECOND_OF_MINUTE}、
     * {@code MINUTE_OF_HOUR} 和 {@code HOUR_OF_DAY} 字段。
     */
    MILLI_OF_DAY("MilliOfDay", MILLIS, DAYS, ValueRange.of(0, 86400L * 1000L - 1)),
    /**
     * 分钟。
     * <p>
     * 这是分钟内的秒数，从 0 到 59。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     */
    SECOND_OF_MINUTE("SecondOfMinute", SECONDS, MINUTES, ValueRange.of(0, 59), "second"),
    /**
     * 秒。
     * <p>
     * 这是天内的秒数，从 0 到 (24 * 60 * 60) - 1。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该值被拆分为 {@code SECOND_OF_MINUTE}、{@code MINUTE_OF_HOUR}
     * 和 {@code HOUR_OF_DAY} 字段。
     */
    SECOND_OF_DAY("SecondOfDay", SECONDS, DAYS, ValueRange.of(0, 86400L - 1)),
    /**
     * 小时。
     * <p>
     * 这是小时内的分钟数，从 0 到 59。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     */
    MINUTE_OF_HOUR("MinuteOfHour", MINUTES, HOURS, ValueRange.of(0, 59), "minute"),
    /**
     * 分钟。
     * <p>
     * 这是天内的分钟数，从 0 到 (24 * 60) - 1。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该值被拆分为 {@code MINUTE_OF_HOUR} 和 {@code HOUR_OF_DAY} 字段。
     */
    MINUTE_OF_DAY("MinuteOfDay", MINUTES, DAYS, ValueRange.of(0, (24 * 60) - 1)),
    /**
     * 小时。
     * <p>
     * 这是 AM/PM 内的小时数，从 0 到 11。
     * 这是在标准 12 小时数字钟上观察到的小时。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下从 0 到 11 进行验证。
     * 在宽松模式下，该值不进行验证。它与 {@code AMPM_OF_DAY} 一起使用，通过将
     * {@code AMPM_OF_DAY} 值乘以 12 来形成 {@code HOUR_OF_DAY}。
     */
    HOUR_OF_AMPM("HourOfAmPm", HOURS, HALF_DAYS, ValueRange.of(0, 11)),
    /**
     * 小时。
     * <p>
     * 这是 AM/PM 内的小时数，从 1 到 12。
     * 这是在标准 12 小时模拟墙钟上观察到的小时。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式下从 1 到 12 进行验证，在智能模式下从 0 到 12 进行验证。
     * 在宽松模式下，该值不进行验证。该字段转换为具有相同值的 {@code HOUR_OF_AMPM}，
     * 除非值为 12，此时转换为 0。
     */
    CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm", HOURS, HALF_DAYS, ValueRange.of(1, 12)),
    /**
     * 小时。
     * <p>
     * 这是天内的小时数，从 0 到 23。
     * 这是在标准 24 小时数字钟上观察到的小时。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下进行验证，但在宽松模式下不进行验证。
     * 该字段与 {@code MINUTE_OF_HOUR}、{@code SECOND_OF_MINUTE} 和
     * {@code NANO_OF_SECOND} 一起使用以生成 {@code LocalTime}。
     * 在宽松模式下，任何多余的天数将添加到解析的日期，或
     * 通过 {@link java.time.format.DateTimeFormatter#parsedExcessDays()} 获取。
     */
    HOUR_OF_DAY("HourOfDay", HOURS, DAYS, ValueRange.of(0, 23), "hour"),
    /**
     * 小时。
     * <p>
     * 这是天内的小时数，从 1 到 24。
     * 这是在 24 小时模拟墙钟上观察到的小时。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式下从 1 到 24 进行验证，在智能模式下从 0 到 24 进行验证。
     * 在宽松模式下，该值不进行验证。该字段转换为具有相同值的 {@code HOUR_OF_DAY}，
     * 除非值为 24，此时转换为 0。
     */
    CLOCK_HOUR_OF_DAY("ClockHourOfDay", HOURS, DAYS, ValueRange.of(1, 24)),
    /**
     * AM/PM。
     * <p>
     * 这是天内的 AM/PM 数，从 0 (AM) 到 1 (PM)。
     * 此字段对所有日历系统具有相同的含义。
     * <p>
     * 当解析此字段时，其行为等同于以下内容：
     * 该值在严格模式和智能模式下从 0 到 1 进行验证。
     * 在宽松模式下，该值不进行验证。它与 {@code HOUR_OF_AMPM} 一起使用，通过将
     * {@code AMPM_OF_DAY} 值乘以 12 来形成 {@code HOUR_OF_DAY}。
     */
    AMPM_OF_DAY("AmPmOfDay", HALF_DAYS, DAYS, ValueRange.of(0, 1), "dayperiod"),
    /**
     * 星期几，例如星期二。
     * <p>
     * 这代表标准的星期几概念。
     * 在默认的 ISO 日历系统中，这从星期一 (1) 到星期日 (7)。
     * {@link DayOfWeek} 类可以用于解释结果。
     * <p>
     * 大多数非 ISO 日历系统也定义了一个与 ISO 对齐的七天星期。
     * 这些日历系统也必须使用相同的编号系统，从星期一 (1) 到星期日 (7)，这允许使用 {@code DayOfWeek}。
     * <p>
     * 没有标准七天星期的日历系统应实现此字段，如果它们有类似的概念，即命名或编号的天数在一个类似于星期的周期内。
     * 建议编号从 1 开始。
     */
    DAY_OF_WEEK("DayOfWeek", DAYS, WEEKS, ValueRange.of(1, 7), "weekday"),
    /**
     * 月内对齐的星期几。
     * <p>
     * 这代表周期为一周的天数计数，其中周与月的开始对齐。
     * 此字段通常与 {@link #ALIGNED_WEEK_OF_MONTH} 一起使用。
     * <p>
     * 例如，在一个七天星期的日历系统中，第一个对齐的月内周从月的第 1 天开始，第二个对齐的周从月的第 8 天开始，依此类推。
     * 在这些对齐的周内，天数从 1 到 7 编号并作为此字段的值返回。
     * 因此，月的第 1 到 7 天将有对齐的星期几值从 1 到 7。
     * 月的第 8 到 14 天将重复此过程，对齐的星期几值从 1 到 7。
     * <p>
     * 没有七天星期的日历系统通常应以相同的方式实现此字段，但使用不同的周长。
     */
    ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth", DAYS, WEEKS, ValueRange.of(1, 7)),
    /**
     * 年内对齐的星期几。
     * <p>
     * 这代表周期为一周的天数计数，其中周与年的开始对齐。
     * 此字段通常与 {@link #ALIGNED_WEEK_OF_YEAR} 一起使用。
     * <p>
     * 例如，在一个七天星期的日历系统中，第一个对齐的年内周从年的第 1 天开始，第二个对齐的周从年的第 8 天开始，依此类推。
     * 在这些对齐的周内，天数从 1 到 7 编号并作为此字段的值返回。
     * 因此，年的第 1 到 7 天将有对齐的星期几值从 1 到 7。
     * 年的第 8 到 14 天将重复此过程，对齐的星期几值从 1 到 7。
     * <p>
     * 没有七天星期的日历系统通常应以相同的方式实现此字段，但使用不同的周长。
     */
    ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear", DAYS, WEEKS, ValueRange.of(1, 7)),
    /**
     * 月内天数。
     * <p>
     * 这代表月内的天数概念。
     * 在默认的 ISO 日历系统中，这从 1 到 31，大多数月份都是如此。
     * 4 月、6 月、9 月和 11 月有 1 到 30 天，而 2 月有 1 到 28 天，闰年有 1 到 29 天。
     * <p>
     * 非 ISO 日历系统应使用该日历系统用户最认可的月内天数值实现此字段。
     * 通常，这是从 1 到月长的天数计数。
     */
    DAY_OF_MONTH("DayOfMonth", DAYS, MONTHS, ValueRange.of(1, 28, 31), "day"),
    /**
     * 年内天数。
     * <p>
     * 这代表年内的天数概念。
     * 在默认的 ISO 日历系统中，这从 1 到 365，标准年份，1 到 366，闰年。
     * <p>
     * 非 ISO 日历系统应使用该日历系统用户最认可的年内天数值实现此字段。
     * 通常，这是从 1 到年长的天数计数。
     * <p>
     * 注意，非 ISO 日历系统的年编号系统可能在与月编号自然重置不同的时间点发生变化。
     * 例如，日本日历系统中，一个时代的变更，将年号重置为 1，可以在任何日期发生。
     * 时代和年号的重置也会导致年内天数重置为 1，但不会重置月内天数或月内天数。
     */
    DAY_OF_YEAR("DayOfYear", DAYS, YEARS, ValueRange.of(1, 365, 366)),
    /**
     * 基于 1970-01-01（ISO）的纪元天数。
     * <p>
     * 这是从 1970-01-01（ISO）开始的连续天数计数，其中 1970-01-01（ISO）为零。
     * 注意，这使用的是 <i>本地</i> 时间线，忽略偏移和时区。
     * <p>
     * 此字段在所有日历系统中严格定义为具有相同的含义。
     * 这是确保日历之间互操作性的必要条件。
     */
    EPOCH_DAY("EpochDay", DAYS, FOREVER, ValueRange.of((long) (Year.MIN_VALUE * 365.25), (long) (Year.MAX_VALUE * 365.25))),
    /**
     * 月内对齐的周数。
     * <p>
     * 这代表周期为一个月的周数计数，其中周与月的开始对齐。
     * 此字段通常与 {@link #ALIGNED_DAY_OF_WEEK_IN_MONTH} 一起使用。
     * <p>
     * 例如，在一个七天星期的日历系统中，第一个对齐的月内周从月的第 1 天开始，第二个对齐的周从月的第 8 天开始，依此类推。
     * 因此，月的第 1 到 7 天在对齐的周 1，而月的第 8 到 14 天在对齐的周 2，依此类推。
     * <p>
     * 没有七天星期的日历系统通常应以相同的方式实现此字段，但使用不同的周长。
     */
    ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth", WEEKS, MONTHS, ValueRange.of(1, 4, 5)),
    /**
     * 年内对齐的周数。
     * <p>
     * 这代表周期为一年的周数计数，其中周与年的开始对齐。
     * 此字段通常与 {@link #ALIGNED_DAY_OF_WEEK_IN_YEAR} 一起使用。
     * <p>
     * 例如，在一个七天星期的日历系统中，第一个对齐的年内周从年的第 1 天开始，第二个对齐的周从年的第 8 天开始，依此类推。
     * 因此，年的第 1 到 7 天在对齐的周 1，而年的第 8 到 14 天在对齐的周 2，依此类推。
     * <p>
     * 没有七天星期的日历系统通常应以相同的方式实现此字段，但使用不同的周长。
     */
    ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear", WEEKS, YEARS, ValueRange.of(1, 53)),
    /**
     * 年内月份，例如 3 月。
     * <p>
     * 这代表年内的月份概念。
     * 在默认的 ISO 日历系统中，这从 1 月 (1) 到 12 月 (12)。
     * <p>
     * 非 ISO 日历系统应使用该日历系统用户最认可的年内月份值实现此字段。
     * 通常，这是从 1 开始的月份计数。
     */
    MONTH_OF_YEAR("MonthOfYear", MONTHS, YEARS, ValueRange.of(1, 12), "month"),
    /**
     * 基于从 0 年开始的连续月份计数。
     * <p>
     * 这是从 0 年开始的连续月份计数，其中 0 年的第一个月的值为 0。
     * 后来的月份值逐渐增大。
     * 更早的月份值逐渐减小。
     * 月份序列中没有间隙或中断。
     * 注意，这使用的是 <i>本地</i> 时间线，忽略偏移和时区。
     * <p>
     * 在默认的 ISO 日历系统中，2012 年 6 月的值为
     * {@code (2012 * 12 + 6 - 1)}。此字段主要用于内部使用。
     * <p>
     * 非 ISO 日历系统必须按照上述定义实现此字段。
     * 它只是一个从 0 年开始的简单零基月份计数。
     * 所有具有完整 0 年定义的日历系统都有一个 0 年。
     * 如果日历系统有一个排除 0 年的最小年，则必须推断出一个 0 年，以便此方法被定义。
     */
    PROLEPTIC_MONTH("ProlepticMonth", MONTHS, FOREVER, ValueRange.of(Year.MIN_VALUE * 12L, Year.MAX_VALUE * 12L + 11)),
    /**
     * 时代内的年份。
     * <p>
     * 这代表时代内的年份概念。
     * 此字段通常与 {@link #ERA} 一起使用。
     * <p>
     * 日期的标准心理模型基于三个概念 - 年、月和日。
     * 这些映射到 {@code YEAR}、{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 字段。
     * 注意，这里没有提到时代。
     * 日期的完整模型需要四个概念 - 时代、年、月和日。这些映射到
     * {@code ERA}、{@code YEAR_OF_ERA}、{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 字段。
     * 使用此字段还是 {@code YEAR} 取决于使用哪种心理模型。
     * 有关此主题的更多讨论，请参见 {@link ChronoLocalDate}。
     * <p>
     * 在默认的 ISO 日历系统中，定义了两个时代，'BCE' 和 'CE'。
     * 当前使用的是 'CE' 时代，年份从 1 到最大值。
     * 'BCE' 时代是前一个时代，年份倒数。
     * <p>
     * 例如，每次减去一年的结果如下：<br>
     * - 公元前 2 年 = 'CE' 年 2<br>
     * - 公元前 1 年 = 'CE' 年 1<br>
     * - 公元 0 年 = 'BCE' 年 1<br>
     * - 公元 -1 年 = 'BCE' 年 2<br>
     * <p>
     * 注意，ISO-8601 标准实际上没有定义时代。
     * 还要注意，ISO 时代与众所周知的 AD/BC 时代不一致，因为从儒略历到格里高利历的变更。
     * <p>
     * 非 ISO 日历系统应使用该日历系统用户最认可的年份值实现此字段。
     * 由于大多数日历系统只有两个时代，年份编号方式通常与 ISO 日历系统相同。
     * 年份值通常应始终为正，但这不是强制要求。
     */
    YEAR_OF_ERA("YearOfEra", YEARS, FOREVER, ValueRange.of(1, Year.MAX_VALUE, Year.MAX_VALUE + 1)),
    /**
     * 顺序年份，例如 2012。
     * <p>
     * 这代表年份的概念，顺序计数并使用负数。
     * 顺序年份不按时代解释。
     * 有关顺序年份到年份的映射示例，请参见 {@link #YEAR_OF_ERA}。
     * <p>
     * 日期的标准心理模型基于三个概念 - 年、月和日。
     * 这些映射到 {@code YEAR}、{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 字段。
     * 注意，这里没有提到时代。
     * 日期的完整模型需要四个概念 - 时代、年、月和日。这些映射到
     * {@code ERA}、{@code YEAR_OF_ERA}、{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 字段。
     * 使用此字段还是 {@code YEAR_OF_ERA} 取决于使用哪种心理模型。
     * 有关此主题的更多讨论，请参见 {@link ChronoLocalDate}。
     * <p>
     * 非 ISO 日历系统应按以下方式实现此字段。
     * 如果日历系统只有两个时代，一个固定日期之前和之后，那么
     * 顺序年份值必须与后来时代的年份值相同，而更早时代的年份值逐渐为负。
     * 如果日历系统有超过两个时代，那么顺序年份值可以定义为任何适当的值，尽管将其定义为与 ISO 相同可能是最佳选择。
     */
    YEAR("Year", YEARS, FOREVER, ValueRange.of(Year.MIN_VALUE, Year.MAX_VALUE), "year"),
    /**
     * 时代。
     * <p>
     * 这代表时代概念，这是时间线的最大划分。
     * 此字段通常与 {@link #YEAR_OF_ERA} 一起使用。
     * <p>
     * 在默认的 ISO 日历系统中，定义了两个时代，'BCE' 和 'CE'。
     * 当前使用的是 'CE' 时代，年份从 1 到最大值。
     * 'BCE' 时代是前一个时代，年份倒数。
     * 有关详细示例，请参见 {@link #YEAR_OF_ERA}。
     * <p>
     * 非 ISO 日历系统应实现此字段以定义时代。
     * 1970-01-01（ISO）时活跃的时代的值必须分配为 1。
     * 更早的时代必须具有逐渐减小的值。
     * 更晚的时代必须具有逐渐增大的值。
     */
    ERA("Era", ERAS, FOREVER, ValueRange.of(0, 1), "era"),
    /**
     * 瞬时纪元秒。
     * <p>
     * 这代表从 1970-01-01T00:00Z（ISO）开始的连续秒数计数，其中 1970-01-01T00:00Z（ISO）为零。
     * 此字段可以与 {@link #NANO_OF_SECOND} 一起使用以表示秒的小数部分。
     * <p>
     * {@link Instant} 表示时间线上的一个瞬时点。
     * 单独使用时，瞬时点没有足够的信息来获取本地日期时间。
     * 只有与偏移或时区配对时，才能计算出本地日期或时间。
     * <p>
     * 此字段在所有日历系统中严格定义为具有相同的含义。
     * 这是确保日历之间互操作性的必要条件。
     */
    INSTANT_SECONDS("InstantSeconds", SECONDS, FOREVER, ValueRange.of(Long.MIN_VALUE, Long.MAX_VALUE)),
    /**
     * 与 UTC/Greenwich 的偏移量。
     * <p>
     * 这代表本地时间与 UTC/Greenwich 之间的偏移量，以秒为单位。
     * <p>
     * {@link ZoneOffset} 表示本地时间与 UTC/Greenwich 之间的差异，通常是一个固定数量的小时和分钟。
     * 它等同于偏移量的总秒数。
     * 例如，冬季巴黎的偏移量为 {@code +01:00}，即 3600 秒。
     * <p>
     * 此字段在所有日历系统中严格定义为具有相同的含义。
     * 这是确保日历之间互操作性的必要条件。
     */
    OFFSET_SECONDS("OffsetSeconds", SECONDS, FOREVER, ValueRange.of(-18 * 3600, 18 * 3600));


                private final String name;
    private final TemporalUnit baseUnit;
    private final TemporalUnit rangeUnit;
    private final ValueRange range;
    private final String displayNameKey;

    private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
        this.displayNameKey = null;
    }

    private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit,
            ValueRange range, String displayNameKey) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
        this.displayNameKey = displayNameKey;
    }

    @Override
    public String getDisplayName(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        if (displayNameKey == null) {
            return name;
        }

        LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                                    .getLocaleResources(locale);
        ResourceBundle rb = lr.getJavaTimeFormatData();
        String key = "field." + displayNameKey;
        return rb.containsKey(key) ? rb.getString(key) : name;
    }

    @Override
    public TemporalUnit getBaseUnit() {
        return baseUnit;
    }

    @Override
    public TemporalUnit getRangeUnit() {
        return rangeUnit;
    }

    /**
     * 获取该字段的有效值范围。
     * <p>
     * 所有字段都可以表示为一个 {@code long} 整数。
     * 此方法返回一个描述该值有效范围的对象。
     * <p>
     * 此方法返回该字段在 ISO-8601 日历系统中的范围。
     * 该范围可能不适用于其他日历系统。
     * 使用 {@link Chronology#range(ChronoField)} 访问不同日历系统的正确范围。
     * <p>
     * 注意，结果仅描述最小和最大有效值，
     * 不要对它们读取过多信息。例如，范围内的某些值可能对字段无效。
     *
     * @return 该字段的有效值范围，不为空
     */
    @Override
    public ValueRange range() {
        return range;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此字段是否表示日期的一部分。
     * <p>
     * 从星期几到纪元的字段是基于日期的。
     *
     * @return 如果它是日期的一部分，则返回 true
     */
    @Override
    public boolean isDateBased() {
        return ordinal() >= DAY_OF_WEEK.ordinal() && ordinal() <= ERA.ordinal();
    }

    /**
     * 检查此字段是否表示时间的一部分。
     * <p>
     * 从纳秒到一天中的上午或下午的字段是基于时间的。
     *
     * @return 如果它是时间的一部分，则返回 true
     */
    @Override
    public boolean isTimeBased() {
        return ordinal() < DAY_OF_WEEK.ordinal();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的值是否对此字段有效。
     * <p>
     * 这验证该值是否在 {@link #range()} 返回的有效值的外范围之内。
     * <p>
     * 此方法检查 ISO-8601 日历系统中字段的范围。
     * 该范围可能不适用于其他日历系统。
     * 使用 {@link Chronology#range(ChronoField)} 访问不同日历系统的正确范围。
     *
     * @param value  要检查的值
     * @return 传递的值
     */
    public long checkValidValue(long value) {
        return range().checkValidValue(value, this);
    }

    /**
     * 检查指定的值是否有效并适合 {@code int}。
     * <p>
     * 这验证该值是否在 {@link #range()} 返回的有效值的外范围之内。
     * 它还检查所有有效值是否在 {@code int} 的范围内。
     * <p>
     * 此方法检查 ISO-8601 日历系统中字段的范围。
     * 该范围可能不适用于其他日历系统。
     * 使用 {@link Chronology#range(ChronoField)} 访问不同日历系统的正确范围。
     *
     * @param value  要检查的值
     * @return 传递的值
     */
    public int checkValidIntValue(long value) {
        return range().checkValidIntValue(value, this);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupportedBy(TemporalAccessor temporal) {
        return temporal.isSupported(this);
    }

    @Override
    public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
        return temporal.range(this);
    }

    @Override
    public long getFrom(TemporalAccessor temporal) {
        return temporal.getLong(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R adjustInto(R temporal, long newValue) {
        return (R) temporal.with(this, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return name;
    }

}
