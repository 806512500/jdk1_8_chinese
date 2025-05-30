
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
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.NANOS;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Comparator;
import java.util.Objects;

/**
 * 一个带有 UTC/Greenwich 偏移量的 ISO-8601 日历系统中的日期时间，例如 {@code 2007-12-03T10:15:30+01:00}。
 * <p>
 * {@code OffsetDateTime} 是一个不可变的日期时间表示，带有偏移量。
 * 该类存储所有日期和时间字段，精确到纳秒，以及 UTC/Greenwich 的偏移量。例如，值
 * "2007 年 10 月 2 日 13:45.30.123456789 +02:00" 可以存储在 {@code OffsetDateTime} 中。
 * <p>
 * {@code OffsetDateTime}、{@link java.time.ZonedDateTime} 和 {@link java.time.Instant} 都存储了时间线上的一个瞬间，精确到纳秒。
 * {@code Instant} 是最简单的，仅表示瞬间。
 * {@code OffsetDateTime} 在瞬间上增加了 UTC/Greenwich 的偏移量，从而可以获取本地日期时间。
 * {@code ZonedDateTime} 增加了完整的时区规则。
 * <p>
 * 在更简单的应用程序中，建议使用 {@code ZonedDateTime} 或 {@code Instant} 来建模数据。
 * 本类可用于更详细地建模日期时间概念，或与数据库或网络协议通信。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）对 {@code OffsetDateTime} 实例进行操作可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的，线程安全的。
 *
 * @since 1.8
 */
public final class OffsetDateTime
        implements Temporal, TemporalAdjuster, Comparable<OffsetDateTime>, Serializable {

    /**
     * 支持的最小 {@code OffsetDateTime}，'-999999999-01-01T00:00:00+18:00'。
     * 这是在最大偏移量（更大的偏移量在时间线上更早）的最小日期的午夜本地日期时间。
     * 这结合了 {@link LocalDateTime#MIN} 和 {@link ZoneOffset#MAX}。
     * 应用程序可以将其用作“远过去”的日期时间。
     */
    public static final OffsetDateTime MIN = LocalDateTime.MIN.atOffset(ZoneOffset.MAX);
    /**
     * 支持的最大 {@code OffsetDateTime}，'+999999999-12-31T23:59:59.999999999-18:00'。
     * 这是在最小偏移量（更大的负偏移量在时间线上更晚）的最大日期的午夜前的本地日期时间。
     * 这结合了 {@link LocalDateTime#MAX} 和 {@link ZoneOffset#MIN}。
     * 应用程序可以将其用作“远未来”的日期时间。
     */
    public static final OffsetDateTime MAX = LocalDateTime.MAX.atOffset(ZoneOffset.MIN);

    /**
     * 获取一个仅基于瞬间比较两个 {@code OffsetDateTime} 实例的比较器。
     * <p>
     * 该方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层的瞬间。
     *
     * @return 一个按时间线顺序比较的比较器
     *
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    public static Comparator<OffsetDateTime> timeLineOrder() {
        return OffsetDateTime::compareInstant;
    }

    /**
     * 比较此 {@code OffsetDateTime} 与另一个日期时间。
     * 比较基于瞬间。
     *
     * @param datetime1  要比较的第一个日期时间，不为空
     * @param datetime2  要比较的另一个日期时间，不为空
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     */
    private static int compareInstant(OffsetDateTime datetime1, OffsetDateTime datetime2) {
        if (datetime1.getOffset().equals(datetime2.getOffset())) {
            return datetime1.toLocalDateTime().compareTo(datetime2.toLocalDateTime());
        }
        int cmp = Long.compare(datetime1.toEpochSecond(), datetime2.toEpochSecond());
        if (cmp == 0) {
            cmp = datetime1.toLocalTime().getNano() - datetime2.toLocalTime().getNano();
        }
        return cmp;
    }

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 2287754244819255394L;

    /**
     * 本地日期时间。
     */
    private final LocalDateTime dateTime;
    /**
     * 从 UTC/Greenwich 的偏移量。
     */
    private final ZoneOffset offset;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前日期时间。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期时间。
     * 时钟中的时区将计算偏移量。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟的当前日期时间，不为空
     */
    public static OffsetDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前日期时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期时间。
     * 指定时区可以避免依赖默认时区。
     * 指定时区将计算偏移量。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前日期时间，不为空
     */
    public static OffsetDateTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定时钟获取当前日期时间。
     * <p>
     * 这将查询指定的时钟以获取当前日期时间。
     * 时钟中的时区将计算偏移量。
     * <p>
     * 使用此方法可以使用替代时钟进行测试。
     * 可以使用 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期时间，不为空
     */
    public static OffsetDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // 调用一次
        return ofInstant(now, clock.getZone().getRules().getOffset(now));
    }

    //-----------------------------------------------------------------------
    /**
     * 从日期、时间和偏移量获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 这将创建一个带有指定本地日期、时间和偏移量的偏移日期时间。
     *
     * @param date  本地日期，不为空
     * @param time  本地时间，不为空
     * @param offset  时区偏移量，不为空
     * @return 偏移日期时间，不为空
     */
    public static OffsetDateTime of(LocalDate date, LocalTime time, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(date, time);
        return new OffsetDateTime(dt, offset);
    }

    /**
     * 从日期时间和偏移量获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 这将创建一个带有指定本地日期时间和偏移量的偏移日期时间。
     *
     * @param dateTime  本地日期时间，不为空
     * @param offset  时区偏移量，不为空
     * @return 偏移日期时间，不为空
     */
    public static OffsetDateTime of(LocalDateTime dateTime, ZoneOffset offset) {
        return new OffsetDateTime(dateTime, offset);
    }

    /**
     * 从年、月、日、小时、分钟、秒、纳秒和偏移量获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 这将创建一个带有七个指定字段的偏移日期时间。
     * <p>
     * 此方法主要用于编写测试用例。
     * 非测试代码通常会使用其他方法来创建偏移时间。
     * {@code LocalDateTime} 有五个额外的方便变体的等效工厂方法，参数较少。
     * 为了减少 API 的足迹，这里没有提供这些方法。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，从 1（1 月）到 12（12 月）
     * @param dayOfMonth  要表示的月份中的日期，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @param offset  时区偏移量，不为空
     * @return 偏移日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或月份中的日期无效
     */
    public static OffsetDateTime of(
            int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        return new OffsetDateTime(dt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从 {@code Instant} 和时区 ID 获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 这将创建一个与指定瞬间相同的偏移日期时间。
     * 从 UTC/Greenwich 找到偏移量很简单，因为每个瞬间只有一个有效的偏移量。
     *
     * @param instant  要创建日期时间的瞬间，不为空
     * @param zone  时区，可以是偏移量，不为空
     * @return 偏移日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public static OffsetDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset);
        return new OffsetDateTime(ldt, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 这将根据指定的时间对象获取偏移日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code OffsetDateTime} 的实例。
     * <p>
     * 转换将首先从时间对象中获取 {@code ZoneOffset}。
     * 然后尝试获取 {@code LocalDateTime}，必要时回退到 {@code Instant}。
     * 结果将是 {@code ZoneOffset} 与 {@code LocalDateTime} 或 {@code Instant} 的组合。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 功能接口的签名，允许其通过方法引用作为查询使用，{@code OffsetDateTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 偏移日期时间，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code OffsetDateTime}
     */
    public static OffsetDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof OffsetDateTime) {
            return (OffsetDateTime) temporal;
        }
        try {
            ZoneOffset offset = ZoneOffset.from(temporal);
            LocalDate date = temporal.query(TemporalQueries.localDate());
            LocalTime time = temporal.query(TemporalQueries.localTime());
            if (date != null && time != null) {
                return OffsetDateTime.of(date, time, offset);
            } else {
                Instant instant = Instant.from(temporal);
                return OffsetDateTime.ofInstant(instant, offset);
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 OffsetDateTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }


                //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12-03T10:15:30+01:00}）获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 字符串必须表示一个有效的日期时间，并使用 {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME} 解析。
     *
     * @param text 要解析的文本，例如 "2007-12-03T10:15:30+01:00"，不能为空
     * @return 解析的带偏移的日期时间，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetDateTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code OffsetDateTime} 的实例。
     * <p>
     * 文本使用格式化器解析，返回一个日期时间。
     *
     * @param text 要解析的文本，不能为空
     * @param formatter 要使用的格式化器，不能为空
     * @return 解析的带偏移的日期时间，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, OffsetDateTime::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param dateTime 本地日期时间，不能为空
     * @param offset 时区偏移，不能为空
     */
    private OffsetDateTime(LocalDateTime dateTime, ZoneOffset offset) {
        this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
        this.offset = Objects.requireNonNull(offset, "offset");
    }

    /**
     * 基于此日期时间返回一个新的日期时间，尽可能返回 {@code this}。
     *
     * @param dateTime 要创建的日期时间，不能为空
     * @param offset 要创建的时区偏移，不能为空
     */
    private OffsetDateTime with(LocalDateTime dateTime, ZoneOffset offset) {
        if (this.dateTime == dateTime && this.offset.equals(offset)) {
            return this;
        }
        return new OffsetDateTime(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否支持。
     * <p>
     * 这个方法检查此日期时间是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long) with} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code NANO_OF_SECOND}
     * <li>{@code NANO_OF_DAY}
     * <li>{@code MICRO_OF_SECOND}
     * <li>{@code MICRO_OF_DAY}
     * <li>{@code MILLI_OF_SECOND}
     * <li>{@code MILLI_OF_DAY}
     * <li>{@code SECOND_OF_MINUTE}
     * <li>{@code SECOND_OF_DAY}
     * <li>{@code MINUTE_OF_HOUR}
     * <li>{@code MINUTE_OF_DAY}
     * <li>{@code HOUR_OF_AMPM}
     * <li>{@code CLOCK_HOUR_OF_AMPM}
     * <li>{@code HOUR_OF_DAY}
     * <li>{@code CLOCK_HOUR_OF_DAY}
     * <li>{@code AMPM_OF_DAY}
     * <li>{@code DAY_OF_WEEK}
     * <li>{@code ALIGNED_DAY_OF_WEEK_IN_MONTH}
     * <li>{@code ALIGNED_DAY_OF_WEEK_IN_YEAR}
     * <li>{@code DAY_OF_MONTH}
     * <li>{@code DAY_OF_YEAR}
     * <li>{@code EPOCH_DAY}
     * <li>{@code ALIGNED_WEEK_OF_MONTH}
     * <li>{@code ALIGNED_WEEK_OF_YEAR}
     * <li>{@code MONTH_OF_YEAR}
     * <li>{@code PROLEPTIC_MONTH}
     * <li>{@code YEAR_OF_ERA}
     * <li>{@code YEAR}
     * <li>{@code ERA}
     * <li>{@code INSTANT_SECONDS}
     * <li>{@code OFFSET_SECONDS}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否支持由字段确定。
     *
     * @param field 要检查的字段，null 返回 false
     * @return 如果此日期时间支持该字段则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        return field instanceof ChronoField || (field != null && field.isSupportedBy(this));
    }

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 这个方法检查指定的单位是否可以添加到或从这个日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位有：
     * <ul>
     * <li>{@code NANOS}
     * <li>{@code MICROS}
     * <li>{@code MILLIS}
     * <li>{@code SECONDS}
     * <li>{@code MINUTES}
     * <li>{@code HOURS}
     * <li>{@code HALF_DAYS}
     * <li>{@code DAYS}
     * <li>{@code WEEKS}
     * <li>{@code MONTHS}
     * <li>{@code YEARS}
     * <li>{@code DECADES}
     * <li>{@code CENTURIES}
     * <li>{@code MILLENNIA}
     * <li>{@code ERAS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 单位是否支持由单位确定。
     *
     * @param unit 要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去则返回 true，否则返回 false
     */
    @Override  // 重写以提供 Javadoc
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit != FOREVER;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此日期时间用于增强返回范围的准确性。
     * 如果由于字段不支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 范围是否可以获取由字段确定。
     *
     * @param field 要查询范围的字段，不能为空
     * @return 字段的有效值范围，不能为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == INSTANT_SECONDS || field == OFFSET_SECONDS) {
                return field.range();
            }
            return dateTime.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 这个方法查询此日期时间以获取指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值，
     * 但 {@code NANO_OF_DAY}、{@code MICRO_OF_DAY}、{@code EPOCH_DAY}、
     * {@code PROLEPTIC_MONTH} 和 {@code INSTANT_SECONDS} 的值太大，无法放入 {@code int}，将抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 是否可以获取值以及值的含义由字段确定。
     *
     * @param field 要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS:
                    throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
                case OFFSET_SECONDS:
                    return getOffset().getTotalSeconds();
            }
            return dateTime.get(field);
        }
        return Temporal.super.get(field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 这个方法查询此日期时间以获取指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 是否可以获取值以及值的含义由字段确定。
     *
     * @param field 要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS: return toEpochSecond();
                case OFFSET_SECONDS: return getOffset().getTotalSeconds();
            }
            return dateTime.getLong(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时区偏移，例如 '+01:00'。
     * <p>
     * 这是本地日期时间与 UTC/Greenwich 的偏移。
     *
     * @return 时区偏移，不能为空
     */
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，并指定偏移，确保结果具有相同的本地日期时间。
     * <p>
     * 此方法返回一个具有相同 {@code LocalDateTime} 和指定 {@code ZoneOffset} 的对象。
     * 不需要进行任何计算。
     * 例如，如果此时间表示 {@code 2007-12-03T10:30+02:00}，指定的偏移是 {@code +03:00}，
     * 那么此方法将返回 {@code 2007-12-03T10:30+03:00}。
     * <p>
     * 要考虑偏移之间的差异并调整时间字段，使用 {@link #withOffsetSameInstant}。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param offset 要更改的时区偏移，不能为空
     * @return 基于此日期时间并具有请求偏移的 {@code OffsetDateTime}，不能为空
     */
    public OffsetDateTime withOffsetSameLocal(ZoneOffset offset) {
        return with(dateTime, offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，并指定偏移，确保结果在同一时刻。
     * <p>
     * 此方法返回一个具有指定 {@code ZoneOffset} 和调整了两个偏移之间差异的 {@code LocalDateTime} 的对象。
     * 这将导致旧对象和新对象表示同一时刻。
     * 这对于查找不同偏移的本地时间很有用。
     * 例如，如果此时间表示 {@code 2007-12-03T10:30+02:00}，指定的偏移是 {@code +03:00}，
     * 那么此方法将返回 {@code 2007-12-03T11:30+03:00}。
     * <p>
     * 要更改偏移而不调整本地时间，使用 {@link #withOffsetSameLocal}。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param offset 要更改的时区偏移，不能为空
     * @return 基于此日期时间并具有请求偏移的 {@code OffsetDateTime}，不能为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime withOffsetSameInstant(ZoneOffset offset) {
        if (offset.equals(this.offset)) {
            return this;
        }
        int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
        LocalDateTime adjusted = dateTime.plusSeconds(difference);
        return new OffsetDateTime(adjusted, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalDateTime} 部分。
     * <p>
     * 这返回一个具有与此日期时间相同年、月、日和时间的 {@code LocalDateTime}。
     *
     * @return 此日期时间的本地日期时间部分，不能为空
     */
    public LocalDateTime toLocalDateTime() {
        return dateTime;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalDate} 部分。
     * <p>
     * 这返回一个具有与此日期时间相同年、月和日的 {@code LocalDate}。
     *
     * @return 此日期时间的日期部分，不能为空
     */
    public LocalDate toLocalDate() {
        return dateTime.toLocalDate();
    }


                /**
     * 获取年份字段。
     * <p>
     * 此方法返回年份的原始 {@code int} 值。
     * <p>
     * 通过此方法返回的年份是按 {@code get(YEAR)} 的规定计算的。
     * 要获取纪年的年份，请使用 {@code get(YEAR_OF_ERA)}。
     *
     * @return 年份，从 MIN_YEAR 到 MAX_YEAR
     */
    public int getYear() {
        return dateTime.getYear();
    }

    /**
     * 获取 1 到 12 的月份字段。
     * <p>
     * 此方法返回 1 到 12 的月份 {@code int} 值。
     * 在调用 {@link #getMonth()} 时，使用枚举 {@link Month} 通常会使应用程序代码更清晰。
     *
     * @return 月份，从 1 到 12
     * @see #getMonth()
     */
    public int getMonthValue() {
        return dateTime.getMonthValue();
    }

    /**
     * 使用 {@code Month} 枚举获取月份字段。
     * <p>
     * 此方法返回月份的枚举 {@link Month}。
     * 这样可以避免对 {@code int} 值的意义产生混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue() int 值}。
     *
     * @return 月份，非空
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return dateTime.getMonth();
    }

    /**
     * 获取月份中的日期字段。
     * <p>
     * 此方法返回月份中的日期的原始 {@code int} 值。
     *
     * @return 月份中的日期，从 1 到 31
     */
    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    /**
     * 获取年份中的日期字段。
     * <p>
     * 此方法返回年份中的日期的原始 {@code int} 值。
     *
     * @return 年份中的日期，从 1 到 365，或在闰年为 366
     */
    public int getDayOfYear() {
        return dateTime.getDayOfYear();
    }

    /**
     * 获取星期几字段，这是一个枚举 {@code DayOfWeek}。
     * <p>
     * 此方法返回星期几的枚举 {@link DayOfWeek}。
     * 这样可以避免对 {@code int} 值的意义产生混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link DayOfWeek#getValue() int 值}。
     * <p>
     * 可以从 {@code DayOfWeek} 获取更多详细信息，包括值的文本名称。
     *
     * @return 星期几，非空
     */
    public DayOfWeek getDayOfWeek() {
        return dateTime.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalTime} 部分。
     * <p>
     * 此方法返回一个具有与该日期时间相同小时、分钟、秒和纳秒的 {@code LocalTime}。
     *
     * @return 此日期时间的时间部分，非空
     */
    public LocalTime toLocalTime() {
        return dateTime.toLocalTime();
    }

    /**
     * 获取小时字段。
     *
     * @return 小时，从 0 到 23
     */
    public int getHour() {
        return dateTime.getHour();
    }

    /**
     * 获取分钟字段。
     *
     * @return 分钟，从 0 到 59
     */
    public int getMinute() {
        return dateTime.getMinute();
    }

    /**
     * 获取秒字段。
     *
     * @return 秒，从 0 到 59
     */
    public int getSecond() {
        return dateTime.getSecond();
    }

    /**
     * 获取纳秒字段。
     *
     * @return 纳秒，从 0 到 999,999,999
     */
    public int getNano() {
        return dateTime.getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 返回此日期时间的调整副本。
     * <p>
     * 此方法返回一个基于此日期时间的 {@code OffsetDateTime}，日期时间已调整。
     * 调整通过指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行哪些调整。
     * <p>
     * 简单的调整器可能只是设置某个字段，例如年份字段。
     * 更复杂的调整器可能将日期设置为月份的最后一天。
     * 一些常见的调整器在
     * {@link java.time.temporal.TemporalAdjusters TemporalAdjusters} 中提供。
     * 这些包括“月份的最后一天”和“下一个星期三”。
     * 关键日期时间类也实现了 {@code TemporalAdjuster} 接口，例如 {@link Month} 和 {@link java.time.MonthDay MonthDay}。
     * 调整器负责处理特殊情况，如月份长度不同和闰年。
     * <p>
     * 例如，以下代码返回 7 月的最后一天：
     * <pre>
     *  import static java.time.Month.*;
     *  import static java.time.temporal.TemporalAdjusters.*;
     *
     *  result = offsetDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     * <p>
     * 类 {@link LocalDate}、{@link LocalTime} 和 {@link ZoneOffset} 实现了
     * {@code TemporalAdjuster}，因此可以使用此方法更改日期、时间或时区：
     * <pre>
     *  result = offsetDateTime.with(date);
     *  result = offsetDateTime.with(time);
     *  result = offsetDateTime.with(offset);
     * </pre>
     * <p>
     * 通过调用指定调整器上的
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，非空
     * @return 基于 {@code this} 并进行了调整的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalDate || adjuster instanceof LocalTime || adjuster instanceof LocalDateTime) {
            return with(dateTime.with(adjuster), offset);
        } else if (adjuster instanceof Instant) {
            return ofInstant((Instant) adjuster, offset);
        } else if (adjuster instanceof ZoneOffset) {
            return with(dateTime, (ZoneOffset) adjuster);
        } else if (adjuster instanceof OffsetDateTime) {
            return (OffsetDateTime) adjuster;
        }
        return (OffsetDateTime) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此日期时间的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此日期时间的 {@code OffsetDateTime}，指定字段的值已更改。
     * 可以使用此方法更改任何支持的字段，如年份、月份或月份中的日期。
     * 如果由于字段不受支持或其他原因无法设置值，则会抛出异常。
     * <p>
     * 在某些情况下，更改指定字段可能会导致结果日期时间无效，例如将 1 月 31 日更改为 2 月会使月份中的日期无效。
     * 在这种情况下，字段负责解决日期。通常会选择前一个有效的日期，例如在此示例中选择 2 月的最后一天。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * <p>
     * {@code INSTANT_SECONDS} 字段将返回具有指定即时值的日期时间。
     * 时区偏移和纳秒不变。
     * 如果新的即时值超出有效范围，则会抛出 {@code DateTimeException}。
     * <p>
     * {@code OFFSET_SECONDS} 字段将返回具有指定时区偏移的日期时间。
     * 本地日期时间不变。如果新的时区偏移值超出有效范围，则会抛出 {@code DateTimeException}。
     * <p>
     * 其他 {@link #isSupported(TemporalField) 支持的字段} 的行为与
     * {@link LocalDateTime#with(TemporalField, long) LocalDateTime} 上的匹配方法相同。
     * 在这种情况下，时区偏移不参与计算，将保持不变。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 并传递 {@code this} 作为参数来获取此方法的结果。
     * 在这种情况下，字段确定是否以及如何调整即时值。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，非空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case INSTANT_SECONDS: return ofInstant(Instant.ofEpochSecond(newValue, getNano()), offset);
                case OFFSET_SECONDS: {
                    return with(dateTime, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
                }
            }
            return with(dateTime.with(field, newValue), offset);
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，年份已更改。
     * <p>
     * 时间和时区偏移不影响计算，结果中将保持不变。
     * 如果月份中的日期对年份无效，将更改为该月份的最后一天。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param year  要在结果中设置的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 基于此日期时间并设置了请求年份的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果年份值无效
     */
    public OffsetDateTime withYear(int year) {
        return with(dateTime.withYear(year), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，月份已更改。
     * <p>
     * 时间和时区偏移不影响计算，结果中将保持不变。
     * 如果月份中的日期对年份无效，将更改为该月份的最后一天。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param month  要在结果中设置的月份，从 1（1 月）到 12（12 月）
     * @return 基于此日期时间并设置了请求月份的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果月份值无效
     */
    public OffsetDateTime withMonth(int month) {
        return with(dateTime.withMonth(month), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，月份中的日期已更改。
     * <p>
     * 如果结果 {@code OffsetDateTime} 无效，将抛出异常。
     * 时间和时区偏移不影响计算，结果中将保持不变。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param dayOfMonth  要在结果中设置的月份中的日期，从 1 到 28-31
     * @return 基于此日期时间并设置了请求日期的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果月份中的日期值无效，
     *  或月份中的日期对月份-年份无效
     */
    public OffsetDateTime withDayOfMonth(int dayOfMonth) {
        return with(dateTime.withDayOfMonth(dayOfMonth), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，年份中的日期已更改。
     * <p>
     * 时间和时区偏移不影响计算，结果中将保持不变。
     * 如果结果 {@code OffsetDateTime} 无效，将抛出异常。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param dayOfYear  要在结果中设置的年份中的日期，从 1 到 365-366
     * @return 基于此日期并设置了请求日期的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果年份中的日期值无效，
     *  或年份中的日期对年份无效
     */
    public OffsetDateTime withDayOfYear(int dayOfYear) {
        return with(dateTime.withDayOfYear(dayOfYear), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，小时已更改。
     * <p>
     * 日期和时区偏移不影响计算，结果中将保持不变。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param hour  要在结果中设置的小时，从 0 到 23
     * @return 基于此日期时间并设置了请求小时的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果小时值无效
     */
    public OffsetDateTime withHour(int hour) {
        return with(dateTime.withHour(hour), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，分钟已更改。
     * <p>
     * 日期和时区偏移不影响计算，结果中将保持不变。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param minute  要在结果中设置的分钟，从 0 到 59
     * @return 基于此日期时间并设置了请求分钟的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果分钟值无效
     */
    public OffsetDateTime withMinute(int minute) {
        return with(dateTime.withMinute(minute), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，秒已更改。
     * <p>
     * 日期和时区偏移不影响计算，结果中将保持不变。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param second  要在结果中设置的秒，从 0 到 59
     * @return 基于此日期时间并设置了请求秒的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果秒值无效
     */
    public OffsetDateTime withSecond(int second) {
        return with(dateTime.withSecond(second), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetDateTime} 的副本，纳秒已更改。
     * <p>
     * 日期和时区偏移不影响计算，结果中将保持不变。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param nanoOfSecond  要在结果中设置的纳秒，从 0 到 999,999,999
     * @return 基于此日期时间并设置了请求纳秒的 {@code OffsetDateTime}，非空
     * @throws DateTimeException 如果纳秒值无效
     */
    public OffsetDateTime withNano(int nanoOfSecond) {
        return with(dateTime.withNano(nanoOfSecond), offset);
    }


                //-----------------------------------------------------------------------
    /**
     * 返回此 {@code OffsetDateTime} 的副本，时间被截断。
     * <p>
     * 截断返回原始日期时间的副本，其中指定单位以下的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒和纳秒字段设置为零。
     * <p>
     * 单位必须具有可以整除标准天长度的 {@linkplain TemporalUnit#getDuration() 时长}。
     * 这包括 {@link ChronoUnit} 中提供的所有时间单位和 {@link ChronoUnit#DAYS 天}。其他单位将抛出异常。
     * <p>
     * 时区偏移不影响计算，结果中的偏移将相同。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，时间被截断，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public OffsetDateTime truncatedTo(TemporalUnit unit) {
        return with(dateTime.truncatedTo(unit), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回此日期时间的副本，指定数量已添加。
     * <p>
     * 这返回一个基于此日期时间的 {@code OffsetDateTime}，指定数量已添加。
     * 数量通常是 {@link Period} 或 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给数量对象，通过调用 {@link TemporalAmount#addTo(Temporal)}。数量实现可以自由地以任何方式实现添加，但通常会回调到 {@link #plus(long, TemporalUnit)}。请参阅数量实现的文档，以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加的数量，不为空
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加指定数量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime plus(TemporalAmount amountToAdd) {
        return (OffsetDateTime) amountToAdd.addTo(this);
    }

    /**
     * 返回此日期时间的副本，指定数量已添加。
     * <p>
     * 这返回一个基于此日期时间的 {@code OffsetDateTime}，以单位表示的数量已添加。如果由于单位不受支持或其他原因无法添加数量，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则添加由 {@link LocalDateTime#plus(long, TemporalUnit)} 实现。
     * 时区偏移不参与计算，结果中的偏移将保持不变。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则此方法的结果是通过调用 {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递获得的。在这种情况下，单位确定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加到结果的单位数量，可以为负数
     * @param unit  要添加的单位，不为空
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加指定数量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with(dateTime.plus(amountToAdd, unit), offset);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的年数已添加。
     * <p>
     * 此方法通过三个步骤将指定数量添加到年份字段：
     * <ol>
     * <li>将输入年份添加到年份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的日期调整为最后一个有效日期</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）加上一年将导致无效日期 2009-02-29（平年）。为了避免返回无效结果，选择该月的最后一个有效日期，即 2009-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param years  要添加的年数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusYears(long years) {
        return with(dateTime.plusYears(years), offset);
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的月数已添加。
     * <p>
     * 此方法通过三个步骤将指定数量添加到月份字段：
     * <ol>
     * <li>将输入月份数添加到年中的月份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的日期调整为最后一个有效日期</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 加上一个月将导致无效日期 2007-04-31。为了避免返回无效结果，选择该月的最后一个有效日期，即 2007-04-30。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要添加的月数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加月数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusMonths(long months) {
        return with(dateTime.plusMonths(months), offset);
    }

    /**
     * 返回此 OffsetDateTime 的副本，指定的周数已添加。
     * <p>
     * 此方法通过将指定的周数添加到天数字段来实现，必要时会递增月份和年份字段以确保结果有效。
     * 结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2008-12-31 加上一周将导致 2009-01-07。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param weeks  要添加的周数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加周数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusWeeks(long weeks) {
        return with(dateTime.plusWeeks(weeks), offset);
    }

    /**
     * 返回此 OffsetDateTime 的副本，指定的天数已添加。
     * <p>
     * 此方法通过将指定的天数添加到天数字段来实现，必要时会递增月份和年份字段以确保结果有效。
     * 结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2008-12-31 加上一天将导致 2009-01-01。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要添加的天数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加天数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusDays(long days) {
        return with(dateTime.plusDays(days), offset);
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的小时数已添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hours  要添加的小时数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加小时数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusHours(long hours) {
        return with(dateTime.plusHours(hours), offset);
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的分钟数已添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minutes  要添加的分钟数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加分钟数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusMinutes(long minutes) {
        return with(dateTime.plusMinutes(minutes), offset);
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的秒数已添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param seconds  要添加的秒数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加秒数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime plusSeconds(long seconds) {
        return with(dateTime.plusSeconds(seconds), offset);
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的纳秒数已添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanos  要添加的纳秒数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已添加纳秒数，不为空
     * @throws DateTimeException 如果无法添加此单位
     */
    public OffsetDateTime plusNanos(long nanos) {
        return with(dateTime.plusNanos(nanos), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回此日期时间的副本，指定的数量已减去。
     * <p>
     * 这返回一个基于此日期时间的 {@code OffsetDateTime}，指定的数量已减去。
     * 数量通常是 {@link Period} 或 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给数量对象，通过调用 {@link TemporalAmount#subtractFrom(Temporal)}。数量实现可以自由地以任何方式实现减法，但通常会回调到 {@link #minus(long, TemporalUnit)}。请参阅数量实现的文档，以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已减去指定数量，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime minus(TemporalAmount amountToSubtract) {
        return (OffsetDateTime) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回此日期时间的副本，指定的数量已减去。
     * <p>
     * 这返回一个基于此日期时间的 {@code OffsetDateTime}，以单位表示的数量已减去。如果由于单位不受支持或其他原因无法减去数量，则会抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。请参阅该方法的完整描述，了解如何进行加法，从而了解减法的工作方式。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要从结果中减去的单位数量，可以为负数
     * @param unit  要减去的单位，不为空
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已减去指定数量，不为空
     * @throws DateTimeException 如果无法减去
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的年数已减去。
     * <p>
     * 此方法通过三个步骤将指定数量从年份字段中减去：
     * <ol>
     * <li>从年份字段中减去输入年份</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的日期调整为最后一个有效日期</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）减去一年将导致无效日期 2009-02-29（平年）。为了避免返回无效结果，选择该月的最后一个有效日期，即 2009-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param years  要减去的年数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已减去年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusYears(long years) {
        return (years == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-years));
    }

    /**
     * 返回此 {@code OffsetDateTime} 的副本，指定的月数已减去。
     * <p>
     * 此方法通过三个步骤将指定数量从月份字段中减去：
     * <ol>
     * <li>从年中的月份字段中减去输入月份数</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的日期调整为最后一个有效日期</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 减去一个月将导致无效日期 2007-04-31。为了避免返回无效结果，选择该月的最后一个有效日期，即 2007-04-30。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要减去的月数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime} 副本，已减去月数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusMonths(long months) {
        return (months == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-months));
    }


                /**
     * 返回一个副本，该副本减去指定的周数。
     * <p>
     * 此方法从天字段中减去指定的周数，必要时递减月和年字段以确保结果仍然有效。
     * 仅当超过最大/最小年份时，结果才无效。
     * <p>
     * 例如，2008-12-31 减去一周将得到 2009-01-07。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param weeks  要减去的周数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去周数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusWeeks(long weeks) {
        return (weeks == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeks));
    }

    /**
     * 返回一个副本，该副本减去指定的天数。
     * <p>
     * 此方法从天字段中减去指定的天数，必要时递减月和年字段以确保结果仍然有效。
     * 仅当超过最大/最小年份时，结果才无效。
     * <p>
     * 例如，2008-12-31 减去一天将得到 2009-01-01。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param days  要减去的天数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去天数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusDays(long days) {
        return (days == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-days));
    }

    /**
     * 返回一个副本，该副本减去指定的小时数。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param hours  要减去的小时数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去小时数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusHours(long hours) {
        return (hours == Long.MIN_VALUE ? plusHours(Long.MAX_VALUE).plusHours(1) : plusHours(-hours));
    }

    /**
     * 返回一个副本，该副本减去指定的分钟数。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param minutes  要减去的分钟数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去分钟数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusMinutes(long minutes) {
        return (minutes == Long.MIN_VALUE ? plusMinutes(Long.MAX_VALUE).plusMinutes(1) : plusMinutes(-minutes));
    }

    /**
     * 返回一个副本，该副本减去指定的秒数。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param seconds  要减去的秒数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去秒数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusSeconds(long seconds) {
        return (seconds == Long.MIN_VALUE ? plusSeconds(Long.MAX_VALUE).plusSeconds(1) : plusSeconds(-seconds));
    }

    /**
     * 返回一个副本，该副本减去指定的纳秒数。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param nanos  要减去的纳秒数，可以为负数
     * @return 基于此日期时间的 {@code OffsetDateTime}，减去纳秒数后，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public OffsetDateTime minusNanos(long nanos) {
        return (nanos == Long.MIN_VALUE ? plusNanos(Long.MAX_VALUE).plusNanos(1) : plusNanos(-nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询来查询此日期时间。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询的文档以了解
     * 此方法的结果将是什么。
     * <p>
     * 本方法的结果是通过调用指定查询上的
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可以为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return (R) getOffset();
        } else if (query == TemporalQueries.zoneId()) {
            return null;
        } else if (query == TemporalQueries.localDate()) {
            return (R) toLocalDate();
        } else if (query == TemporalQueries.localTime()) {
            return (R) toLocalTime();
        } else if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象，使其具有与本对象相同的偏移量、日期和时间。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的临时对象，其偏移量、日期和时间已更改为与本对象相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)}
     * 三次，分别传递 {@link ChronoField#EPOCH_DAY}、
     * {@link ChronoField#NANO_OF_DAY} 和 {@link ChronoField#OFFSET_SECONDS} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式更为清晰，建议使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisOffsetDateTime.adjustInto(temporal);
     *   temporal = temporal.with(thisOffsetDateTime);
     * </pre>
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        // OffsetDateTime 被视为三个独立的字段，而不是一个瞬间
        // 这会产生最一致的结果集
        // 偏移量在日期和时间之后设置，因为它通常是结果的一个小调整，而 ZonedDateTime 经常忽略偏移量
        return temporal
                .with(EPOCH_DAY, toLocalDate().toEpochDay())
                .with(NANO_OF_DAY, toLocalTime().toNanoOfDay())
                .with(OFFSET_SECONDS, getOffset().getTotalSeconds());
    }

    /**
     * 计算到另一个日期时间的时间量，以指定的单位表示。
     * <p>
     * 此方法计算两个 {@code OffsetDateTime} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的日期时间。
     * 如果终点在起点之前，结果将为负数。
     * 例如，可以使用 {@code startDateTime.until(endDateTime, DAYS)} 计算两个日期时间之间的天数。
     * <p>
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为
     * {@code OffsetDateTime}。如果两个日期时间的偏移量不同，指定的终点日期时间将被规范化为具有与本日期时间相同的偏移量。
     * <p>
     * 计算返回一个整数，表示两个日期时间之间的完整单位数。
     * 例如，2012-06-15T00:00Z 和 2012-08-14T23:59Z 之间的月数将仅为一个月，因为还差一分钟才到两个月。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * 选择应基于哪一种使代码更具可读性。
     * <p>
     * 本方法的实现是针对 {@link ChronoUnit} 的。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS} 和 {@code HALF_DAYS}、{@code DAYS}、
     * {@code WEEKS}、{@code MONTHS}、{@code YEARS}、{@code DECADES}、
     * {@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并将 {@code this} 作为第一个参数和转换后的输入临时对象作为第二个参数传递来获得此方法的结果。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param endExclusive  终点日期，不为空，将转换为 {@code OffsetDateTime}
     * @param unit  要测量的时间量的单位，不为空
     * @return 本日期时间与终点日期时间之间的时间量
     * @throws DateTimeException 如果无法计算时间量，或终点临时对象无法转换为 {@code OffsetDateTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        OffsetDateTime end = OffsetDateTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            end = end.withOffsetSameInstant(offset);
            return dateTime.until(end.dateTime, unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化器格式化此日期时间。
     * <p>
     * 此日期时间将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的日期时间字符串，不为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间与时区结合，创建一个 {@code ZonedDateTime}，确保结果具有相同的瞬间。
     * <p>
     * 此方法返回一个由本日期时间和指定时区组成的 {@code ZonedDateTime}。
     * 此转换将忽略可见的本地日期时间，而使用底层的瞬间。这避免了本地时间线上的任何间隙或重叠问题。
     * 结果可能在小时、分钟甚至天等字段上具有不同的值。
     * <p>
     * 若要尝试保留字段值，请使用 {@link #atZoneSimilarLocal(ZoneId)}。
     * 若要使用偏移量作为时区 ID，请使用 {@link #toZonedDateTime()}。
     *
     * @param zone  要使用的时区，不为空
     * @return 由本日期时间组成的 {@code ZonedDateTime}，不为空
     */
    public ZonedDateTime atZoneSameInstant(ZoneId zone) {
        return ZonedDateTime.ofInstant(dateTime, offset, zone);
    }

    /**
     * 将此日期时间与时区结合，创建一个 {@code ZonedDateTime}，尝试保持相同的本地日期和时间。
     * <p>
     * 此方法返回一个由本日期时间和指定时区组成的 {@code ZonedDateTime}。
     * 只要可能，结果将具有与本对象相同的本地日期时间。
     * <p>
     * 时区规则（如夏令时）意味着本地时间线上并非每个时间都存在。如果根据规则本地日期时间在间隙或重叠中，
     * 则将使用解析器来确定结果的本地时间和偏移量。
     * 此方法使用 {@link ZonedDateTime#ofLocal(LocalDateTime, ZoneId, ZoneOffset)}
     * 尽可能保留本实例的偏移量。
     * <p>
     * 有关间隙和重叠的更细粒度控制，有两种方法。
     * 如果您希望在重叠时使用较晚的偏移量，请在调用此方法后立即调用
     * {@link ZonedDateTime#withLaterOffsetAtOverlap()}。
     * <p>
     * 要在不考虑本地时间线的情况下创建具有相同瞬间的带时区的日期时间，请使用 {@link #atZoneSameInstant(ZoneId)}。
     * 若要使用偏移量作为时区 ID，请使用 {@link #toZonedDateTime()}。
     *
     * @param zone  要使用的时区，不为空
     * @return 由本日期和最早有效时间组成的带时区的日期时间，不为空
     */
    public ZonedDateTime atZoneSimilarLocal(ZoneId zone) {
        return ZonedDateTime.ofLocal(dateTime, zone, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间转换为 {@code OffsetTime}。
     * <p>
     * 此方法返回一个具有相同本地时间和偏移量的偏移时间。
     *
     * @return 代表时间和偏移量的 {@code OffsetTime}，不为空
     */
    public OffsetTime toOffsetTime() {
        return OffsetTime.of(dateTime.toLocalTime(), offset);
    }

    /**
     * 将此日期时间转换为 {@code ZonedDateTime}，使用偏移量作为时区 ID。
     * <p>
     * 此方法使用偏移量作为时区 ID 创建最简单的 {@code ZonedDateTime}。
     * <p>
     * 要控制使用的时区，请参见 {@link #atZoneSameInstant(ZoneId)} 和
     * {@link #atZoneSimilarLocal(ZoneId)}。
     *
     * @return 代表相同本地日期时间和偏移量的带时区的日期时间，不为空
     */
    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.of(dateTime, offset);
    }


                /**
     * 将此日期时间转换为 {@code Instant}。
     * <p>
     * 这将返回一个 {@code Instant}，表示与该日期时间相同的时间线上的点。
     *
     * @return 一个表示相同时间点的 {@code Instant}，不为空
     */
    public Instant toInstant() {
        return dateTime.toInstant(offset);
    }

    /**
     * 将此日期时间转换为从 1970-01-01T00:00:00Z 开始的秒数。
     * <p>
     * 这允许将此日期时间转换为 {@link ChronoField#INSTANT_SECONDS} 字段的值。这主要用于低级别的转换，而不是一般应用程序使用。
     *
     * @return 从 1970-01-01T00:00:00Z 开始的秒数
     */
    public long toEpochSecond() {
        return dateTime.toEpochSecond(offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此日期时间与另一个日期时间。
     * <p>
     * 比较基于时间点，然后是本地日期时间。它与 {@link Comparable} 中定义的“与 equals 一致”。
     * <p>
     * 例如，以下是比较顺序：
     * <ol>
     * <li>{@code 2008-12-03T10:30+01:00}</li>
     * <li>{@code 2008-12-03T11:00+01:00}</li>
     * <li>{@code 2008-12-03T12:00+02:00}</li>
     * <li>{@code 2008-12-03T11:30+01:00}</li>
     * <li>{@code 2008-12-03T12:00+01:00}</li>
     * <li>{@code 2008-12-03T12:30+01:00}</li>
     * </ol>
     * 值 #2 和 #3 表示时间线上的同一时间点。当两个值表示同一时间点时，比较本地日期时间以区分它们。这一步是为了使排序与 {@code equals()} 一致。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 比较值，小于 0 表示较小，大于 0 表示较大
     */
    @Override
    public int compareTo(OffsetDateTime other) {
        int cmp = compareInstant(this, other);
        if (cmp == 0) {
            cmp = toLocalDateTime().compareTo(other.toLocalDateTime());
        }
        return cmp;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期时间的时间点是否在指定日期时间之后。
     * <p>
     * 此方法与 {@link #compareTo} 和 {@link #equals} 中的比较不同，因为它仅比较日期时间的时间点。这相当于使用
     * {@code dateTime1.toInstant().isAfter(dateTime2.toInstant());}。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果此时间点在指定日期时间之后，返回 true
     */
    public boolean isAfter(OffsetDateTime other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec > otherEpochSec ||
            (thisEpochSec == otherEpochSec && toLocalTime().getNano() > other.toLocalTime().getNano());
    }

    /**
     * 检查此日期时间的时间点是否在指定日期时间之前。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较日期时间的时间点。这相当于使用
     * {@code dateTime1.toInstant().isBefore(dateTime2.toInstant());}。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果此时间点在指定日期时间之前，返回 true
     */
    public boolean isBefore(OffsetDateTime other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec < otherEpochSec ||
            (thisEpochSec == otherEpochSec && toLocalTime().getNano() < other.toLocalTime().getNano());
    }

    /**
     * 检查此日期时间的时间点是否等于指定日期时间的时间点。
     * <p>
     * 此方法与 {@link #compareTo} 和 {@link #equals} 中的比较不同，因为它仅比较日期时间的时间点。这相当于使用
     * {@code dateTime1.toInstant().equals(dateTime2.toInstant());}。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果时间点等于指定日期时间的时间点，返回 true
     */
    public boolean isEqual(OffsetDateTime other) {
        return toEpochSecond() == other.toEpochSecond() &&
                toLocalTime().getNano() == other.toLocalTime().getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期时间是否等于另一个日期时间。
     * <p>
     * 比较基于本地日期时间和偏移量。要比较时间线上的同一时间点，使用 {@link #isEqual}。
     * 只有 {@code OffsetDateTime} 类型的对象进行比较，其他类型返回 false。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期时间等于其他日期时间，返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OffsetDateTime) {
            OffsetDateTime other = (OffsetDateTime) obj;
            return dateTime.equals(other.dateTime) && offset.equals(other.offset);
        }
        return false;
    }

    /**
     * 此日期时间的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return dateTime.hashCode() ^ offset.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间输出为 {@code String}，例如 {@code 2007-12-03T10:15:30+01:00}。
     * <p>
     * 输出将是以下 ISO-8601 格式之一：
     * <ul>
     * <li>{@code uuuu-MM-dd'T'HH:mmXXXXX}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ssXXXXX}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSSXXXXX}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXXXX}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXXX}</li>
     * </ul>
     * 使用的格式将是输出完整时间值的最短格式，省略的部分隐式为零。
     *
     * @return 此日期时间的字符串表示，不为空
     */
    @Override
    public String toString() {
        return dateTime.toString() + offset.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(10);  // 标识一个 OffsetDateTime
     *  // 排除一个字节头的 <a href="../../serialized-form.html#java.time.LocalDateTime">日期时间</a>
     *  // 排除一个字节头的 <a href="../../serialized-form.html#java.time.ZoneOffset">偏移量</a>
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.OFFSET_DATE_TIME_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    void writeExternal(ObjectOutput out) throws IOException {
        dateTime.writeExternal(out);
        offset.writeExternal(out);
    }

    static OffsetDateTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocalDateTime dateTime = LocalDateTime.readExternal(in);
        ZoneOffset offset = ZoneOffset.readExternal(in);
        return OffsetDateTime.of(dateTime, offset);
    }

}
