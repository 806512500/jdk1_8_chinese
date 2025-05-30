
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

import static java.time.LocalTime.HOURS_PER_DAY;
import static java.time.LocalTime.MICROS_PER_DAY;
import static java.time.LocalTime.MILLIS_PER_DAY;
import static java.time.LocalTime.MINUTES_PER_DAY;
import static java.time.LocalTime.NANOS_PER_DAY;
import static java.time.LocalTime.NANOS_PER_HOUR;
import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoLocalDateTime;
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
import java.util.Objects;

/**
 * ISO-8601日历系统中的无时区日期时间，例如 {@code 2007-12-03T10:15:30}。
 * <p>
 * {@code LocalDateTime} 是一个不可变的日期时间对象，表示日期时间，通常被视为年-月-日-时-分-秒。其他日期和时间字段，
 * 如年中的天数、周中的天数和年中的周数，也可以访问。时间表示为纳秒精度。
 * 例如，值 "2007年10月2日 13:45.30.123456789" 可以存储在 {@code LocalDateTime} 中。
 * <p>
 * 该类不存储或表示时区。相反，它是日期的描述，如生日，结合本地时间，如墙上的时钟所见。
 * 它不能在没有额外信息（如偏移或时区）的情况下表示时间线上的瞬间。
 * <p>
 * ISO-8601日历系统是当今世界上大多数地区使用的现代民用日历系统。它等同于公历系统，
 * 在该系统中，今天的闰年规则适用于所有时间。对于今天编写的大多数应用程序，ISO-8601规则是完全适用的。
 * 然而，任何使用历史日期并要求其准确性的应用程序将发现 ISO-8601 方法不适合。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code LocalDateTime} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class LocalDateTime
        implements Temporal, TemporalAdjuster, ChronoLocalDateTime<LocalDate>, Serializable {

    /**
     * 支持的最小 {@code LocalDateTime}，'-999999999-01-01T00:00:00'。
     * 这是最小日期开始时的午夜本地日期时间。
     * 这结合了 {@link LocalDate#MIN} 和 {@link LocalTime#MIN}。
     * 应用程序可以将其用作“远过去”的日期时间。
     */
    public static final LocalDateTime MIN = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN);
    /**
     * 支持的最大 {@code LocalDateTime}，'+999999999-12-31T23:59:59.999999999'。
     * 这是最大日期结束时的午夜前的本地日期时间。
     * 这结合了 {@link LocalDate#MAX} 和 {@link LocalTime#MAX}。
     * 应用程序可以将其用作“远未来”的日期时间。
     */
    public static final LocalDateTime MAX = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX);

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 6207766400415563566L;

    /**
     * 日期部分。
     */
    private final LocalDate date;
    /**
     * 时间部分。
     */
    private final LocalTime time;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前日期时间。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期时间。
     * <p>
     * 使用此方法将防止使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前日期时间，不为空
     */
    public static LocalDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前日期时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期时间。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将防止使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前日期时间，不为空
     */
    public static LocalDateTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定时钟获取当前日期时间。
     * <p>
     * 这将查询指定的时钟以获取当前日期时间。
     * 使用此方法允许使用替代时钟进行测试。替代时钟可以通过 {@link Clock 依赖注入} 引入。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期时间，不为空
     */
    public static LocalDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // 调用一次
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        return ofEpochSecond(now.getEpochSecond(), now.getNano(), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从年、月、日、小时和分钟获取 {@code LocalDateTime} 实例，将秒和纳秒设置为零。
     * <p>
     * 这返回一个具有指定年、月、日、小时和分钟的 {@code LocalDateTime}。
     * 该日必须是年和月的有效日期，否则将抛出异常。秒和纳秒字段将被设置为零。
     *
     * @param year  要表示的年，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月，不为空
     * @param dayOfMonth  要表示的日，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或该月的日期无效
     */
    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute);
        return new LocalDateTime(date, time);
    }

    /**
     * 从年、月、日、小时、分钟和秒获取 {@code LocalDateTime} 实例，将纳秒设置为零。
     * <p>
     * 这返回一个具有指定年、月、日、小时、分钟和秒的 {@code LocalDateTime}。
     * 该日必须是年和月的有效日期，否则将抛出异常。纳秒字段将被设置为零。
     *
     * @param year  要表示的年，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月，不为空
     * @param dayOfMonth  要表示的日，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或该月的日期无效
     */
    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second);
        return new LocalDateTime(date, time);
    }

    /**
     * 从年、月、日、小时、分钟、秒和纳秒获取 {@code LocalDateTime} 实例。
     * <p>
     * 这返回一个具有指定年、月、日、小时、分钟、秒和纳秒的 {@code LocalDateTime}。
     * 该日必须是年和月的有效日期，否则将抛出异常。
     *
     * @param year  要表示的年，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月，不为空
     * @param dayOfMonth  要表示的日，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或该月的日期无效
     */
    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    //-----------------------------------------------------------------------
    /**
     * 从年、月、日、小时和分钟获取 {@code LocalDateTime} 实例，将秒和纳秒设置为零。
     * <p>
     * 这返回一个具有指定年、月、日、小时和分钟的 {@code LocalDateTime}。
     * 该日必须是年和月的有效日期，否则将抛出异常。秒和纳秒字段将被设置为零。
     *
     * @param year  要表示的年，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月，从 1（1月）到 12（12月）
     * @param dayOfMonth  要表示的日，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或该月的日期无效
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute);
        return new LocalDateTime(date, time);
    }

    /**
     * 从年、月、日、小时、分钟和秒获取 {@code LocalDateTime} 实例，将纳秒设置为零。
     * <p>
     * 这返回一个具有指定年、月、日、小时、分钟和秒的 {@code LocalDateTime}。
     * 该日必须是年和月的有效日期，否则将抛出异常。纳秒字段将被设置为零。
     *
     * @param year  要表示的年，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月，从 1（1月）到 12（12月）
     * @param dayOfMonth  要表示的日，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或该月的日期无效
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second);
        return new LocalDateTime(date, time);
    }


                /**
     * 从年、月、日、小时、分钟、秒和纳秒获取 {@code LocalDateTime} 的实例。
     * <p>
     * 这将返回一个指定年、月、日、小时、分钟、秒和纳秒的 {@code LocalDateTime}。
     * 该日必须是该年和月的有效日期，否则将抛出异常。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，从 1（1月）到 12（12月）
     * @param dayOfMonth  要表示的月份中的日期，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者该月-年中的日期无效
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    /**
     * 从日期和时间获取 {@code LocalDateTime} 的实例。
     *
     * @param date  本地日期，不为空
     * @param time  本地时间，不为空
     * @return 本地日期时间，不为空
     */
    public static LocalDateTime of(LocalDate date, LocalTime time) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(time, "time");
        return new LocalDateTime(date, time);
    }

    //-------------------------------------------------------------------------
    /**
     * 从 {@code Instant} 和时区 ID 获取 {@code LocalDateTime} 的实例。
     * <p>
     * 这将根据指定的即时创建本地日期时间。
     * 首先，使用时区 ID 和即时获取 UTC/格林尼治的偏移量，
     * 这很简单，因为每个即时只有一个有效的偏移量。
     * 然后，使用即时和偏移量计算本地日期时间。
     *
     * @param instant  用于创建日期时间的即时，不为空
     * @param zone  时区，可以是偏移量，不为空
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public static LocalDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        return ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset);
    }

    /**
     * 使用从 1970-01-01T00:00:00Z 开始的秒数获取 {@code LocalDateTime} 的实例。
     * <p>
     * 这允许将 {@link ChronoField#INSTANT_SECONDS} 基准时秒字段
     * 转换为本地日期时间。这主要用于低级转换，而不是一般应用程序使用。
     *
     * @param epochSecond  从 1970-01-01T00:00:00Z 开始的秒数
     * @param nanoOfSecond  秒内的纳秒，从 0 到 999,999,999
     * @param offset  时区偏移量，不为空
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围，
     *  或者纳秒无效
     */
    public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        long localSecond = epochSecond + offset.getTotalSeconds();  // 溢出在后面捕获
        long localEpochDay = Math.floorDiv(localSecond, SECONDS_PER_DAY);
        int secsOfDay = (int)Math.floorMod(localSecond, SECONDS_PER_DAY);
        LocalDate date = LocalDate.ofEpochDay(localEpochDay);
        LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code LocalDateTime} 的实例。
     * <p>
     * 这将根据指定的时间对象获取本地日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将这些信息转换为 {@code LocalDateTime} 的实例。
     * <p>
     * 转换从时间对象中提取并组合 {@code LocalDate} 和 {@code LocalTime}。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用使用它，例如 {@code LocalDateTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 本地日期时间，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code LocalDateTime}
     */
    public static LocalDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof LocalDateTime) {
            return (LocalDateTime) temporal;
        } else if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).toLocalDateTime();
        } else if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toLocalDateTime();
        }
        try {
            LocalDate date = LocalDate.from(temporal);
            LocalTime time = LocalTime.from(temporal);
            return new LocalDateTime(date, time);
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 LocalDateTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12-03T10:15:30}）获取 {@code LocalDateTime} 的实例。
     * <p>
     * 字符串必须表示一个有效的日期时间，并使用
     * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME} 解析。
     *
     * @param text  要解析的文本，如 "2007-12-03T10:15:30"，不为空
     * @return 解析的本地日期时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalDateTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * 使用特定格式器从文本字符串获取 {@code LocalDateTime} 的实例。
     * <p>
     * 文本使用格式器解析，返回日期时间。
     *
     * @param text  要解析的文本，不为空
     * @param formatter  要使用的格式器，不为空
     * @return 解析的本地日期时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalDateTime::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param date  日期时间的日期部分，验证不为空
     * @param time  日期时间的时间部分，验证不为空
     */
    private LocalDateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * 返回具有新日期和时间的此日期时间的副本，检查是否需要创建新对象。
     *
     * @param newDate  新日期时间的日期部分，不为空
     * @param newTime  新日期时间的时间部分，不为空
     * @return 日期时间，不为空
     */
    private LocalDateTime with(LocalDate newDate, LocalTime newTime) {
        if (date == newDate && time == newTime) {
            return this;
        }
        return new LocalDateTime(newDate, newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这检查此日期时间是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long) with}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
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
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否受支持由字段确定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此日期时间上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return f.isDateBased() || f.isTimeBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否受支持。
     * <p>
     * 这检查指定的单位是否可以添加到或从该日期时间中减去。
     * 如果为 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则在此处实现查询。
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
     * 如果单位不是 {@code ChronoUnit}，则通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 单位是否受支持由单位确定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override  // 重写以提供 Javadoc
    public boolean isSupported(TemporalUnit unit) {
        return ChronoLocalDateTime.super.isSupported(unit);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此日期时间用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 范围是否可以获取由字段确定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeBased() ? time.range(field) : date.range(field));
        }
        return field.rangeRefinedBy(this);
    }

    /**
     * 从此日期时间获取指定字段的值作为 {@code int}。
     * <p>
     * 这查询此日期时间以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此日期时间返回有效值，
     * 但 {@code NANO_OF_DAY}、{@code MICRO_OF_DAY}、{@code EPOCH_DAY} 和 {@code PROLEPTIC_MONTH}
     * 太大，无法放入 {@code int} 中，将抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 是否可以获取值以及值的含义由字段确定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或
     *         字段的值超出有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或
     *         值的范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeBased() ? time.get(field) : date.get(field));
        }
        return ChronoLocalDateTime.super.get(field);
    }


                /**
     * 从这个日期时间中获取指定字段的值，作为 {@code long}。
     * <p>
     * 此方法查询此日期时间中指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则此查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此日期时间返回有效的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 是否可以获取值以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return (f.isTimeBased() ? time.getLong(field) : date.getLong(field));
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalDate} 部分。
     * <p>
     * 此方法返回一个与该日期时间具有相同年、月和日的 {@code LocalDate}。
     *
     * @return 此日期时间的日期部分，不为空
     */
    @Override
    public LocalDate toLocalDate() {
        return date;
    }

    /**
     * 获取年份字段。
     * <p>
     * 此方法返回年份的原始 {@code int} 值。
     * <p>
     * 该方法返回的年份是按 {@code get(YEAR)} 的方式计算的。要获取年份在纪元中的值，请使用 {@code get(YEAR_OF_ERA)}。
     *
     * @return 年份，从 MIN_YEAR 到 MAX_YEAR
     */
    public int getYear() {
        return date.getYear();
    }

    /**
     * 获取从 1 到 12 的月份字段。
     * <p>
     * 此方法返回月份的 {@code int} 值，范围从 1 到 12。
     * 为了使代码更清晰，建议使用枚举 {@link Month} 调用 {@link #getMonth()}。
     *
     * @return 月份，从 1 到 12
     * @see #getMonth()
     */
    public int getMonthValue() {
        return date.getMonthValue();
    }

    /**
     * 使用 {@code Month} 枚举获取月份字段。
     * <p>
     * 此方法返回月份的枚举 {@link Month}。
     * 这样可以避免对 {@code int} 值的混淆。如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue()} 方法。
     *
     * @return 月份，不为空
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return date.getMonth();
    }

    /**
     * 获取月份中的日期字段。
     * <p>
     * 此方法返回月份中的日期的原始 {@code int} 值。
     *
     * @return 月份中的日期，从 1 到 31
     */
    public int getDayOfMonth() {
        return date.getDayOfMonth();
    }

    /**
     * 获取年份中的日期字段。
     * <p>
     * 此方法返回年份中的日期的原始 {@code int} 值。
     *
     * @return 年份中的日期，从 1 到 365，或在闰年为 366
     */
    public int getDayOfYear() {
        return date.getDayOfYear();
    }

    /**
     * 获取星期几字段，返回枚举 {@code DayOfWeek}。
     * <p>
     * 此方法返回星期几的枚举 {@link DayOfWeek}。
     * 这样可以避免对 {@code int} 值的混淆。如果需要访问原始的 {@code int} 值，枚举提供了 {@link DayOfWeek#getValue()} 方法。
     * <p>
     * 可以从 {@code DayOfWeek} 获取更多详细信息，包括值的文本名称。
     *
     * @return 星期几，不为空
     */
    public DayOfWeek getDayOfWeek() {
        return date.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalTime} 部分。
     * <p>
     * 此方法返回一个与该日期时间具有相同小时、分钟、秒和纳秒的 {@code LocalTime}。
     *
     * @return 此日期时间的时间部分，不为空
     */
    @Override
    public LocalTime toLocalTime() {
        return time;
    }

    /**
     * 获取小时字段。
     *
     * @return 小时，从 0 到 23
     */
    public int getHour() {
        return time.getHour();
    }

    /**
     * 获取分钟字段。
     *
     * @return 分钟，从 0 到 59
     */
    public int getMinute() {
        return time.getMinute();
    }

    /**
     * 获取秒字段。
     *
     * @return 秒，从 0 到 59
     */
    public int getSecond() {
        return time.getSecond();
    }

    /**
     * 获取纳秒字段。
     *
     * @return 纳秒，从 0 到 999,999,999
     */
    public int getNano() {
        return time.getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 返回调整后的此日期时间的副本。
     * <p>
     * 此方法返回一个基于此日期时间的 {@code LocalDateTime}，日期时间已根据指定的调整器策略对象进行了调整。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 一个简单的调整器可能只是设置某个字段，如年份字段。
     * 一个更复杂的调整器可能将日期设置为该月的最后一天。
     * <p>
     * 一些常见的调整器在
     * {@link java.time.temporal.TemporalAdjusters TemporalAdjusters} 中提供。
     * 这些包括“该月的最后一天”和“下一个星期三”。
     * 一些关键的日期时间类也实现了 {@code TemporalAdjuster} 接口，如 {@link Month} 和 {@link java.time.MonthDay MonthDay}。
     * 调整器负责处理特殊情况，如月份长度不同和闰年。
     * <p>
     * 例如，以下代码返回一个在 7 月最后一天的日期：
     * <pre>
     *  import static java.time.Month.*;
     *  import static java.time.temporal.TemporalAdjusters.*;
     *
     *  result = localDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     * <p>
     * {@link LocalDate} 和 {@link LocalTime} 类实现了 {@code TemporalAdjuster} 接口，
     * 因此可以使用此方法更改日期、时间或偏移量：
     * <pre>
     *  result = localDateTime.with(date);
     *  result = localDateTime.with(time);
     * </pre>
     * <p>
     * 该方法的结果是通过调用指定调整器的
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数获得的。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不为空
     * @return 基于 {@code this} 并进行了调整的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalDate) {
            return with((LocalDate) adjuster, time);
        } else if (adjuster instanceof LocalTime) {
            return with(date, (LocalTime) adjuster);
        } else if (adjuster instanceof LocalDateTime) {
            return (LocalDateTime) adjuster;
        }
        return (LocalDateTime) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此日期时间的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此日期时间的 {@code LocalDateTime}，指定字段的值已更改。
     * 可以用于更改任何支持的字段，如年份、月份或月份中的日期。
     * 如果由于字段不支持或其他原因无法设置值，则会抛出异常。
     * <p>
     * 在某些情况下，更改指定字段可能会导致结果日期时间无效，例如将 1 月 31 日的月份更改为 2 月会使月份中的日期无效。
     * 在这种情况下，字段负责解决日期。通常会选择前一个有效的日期，例如在此示例中选择该月的最后一天。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将按照
     * {@link LocalDate#with(TemporalField, long) LocalDate} 或
     * {@link LocalTime#with(TemporalField, long) LocalTime} 中的相应方法行为。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 并传递 {@code this} 作为参数获得的。
     * 在这种情况下，字段决定是否以及如何调整实例。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不为空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (f.isTimeBased()) {
                return with(date, time.with(field, newValue));
            } else {
                return with(date.with(field, newValue), time);
            }
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，年份已更改。
     * <p>
     * 时间不影响计算，结果中的时间将相同。
     * 如果月份中的日期对年份无效，它将更改为该月的最后一天。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param year  要在结果中设置的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 基于此日期时间并设置了请求年份的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果年份值无效
     */
    public LocalDateTime withYear(int year) {
        return with(date.withYear(year), time);
    }

    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，月份已更改。
     * <p>
     * 时间不影响计算，结果中的时间将相同。
     * 如果月份中的日期对年份无效，它将更改为该月的最后一天。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param month  要在结果中设置的月份，从 1（1 月）到 12（12 月）
     * @return 基于此日期时间并设置了请求月份的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果月份值无效
     */
    public LocalDateTime withMonth(int month) {
        return with(date.withMonth(month), time);
    }

    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，月份中的日期已更改。
     * <p>
     * 如果结果日期时间无效，将抛出异常。
     * 时间不影响计算，结果中的时间将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param dayOfMonth  要在结果中设置的月份中的日期，从 1 到 28-31
     * @return 基于此日期时间并设置了请求日期的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果月份中的日期值无效，
     *  或月份中的日期对年份无效
     */
    public LocalDateTime withDayOfMonth(int dayOfMonth) {
        return with(date.withDayOfMonth(dayOfMonth), time);
    }

    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，年份中的日期已更改。
     * <p>
     * 如果结果日期时间无效，将抛出异常。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param dayOfYear  要在结果中设置的年份中的日期，从 1 到 365-366
     * @return 基于此日期并设置了请求日期的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果年份中的日期值无效，
     *  或年份中的日期对年份无效
     */
    public LocalDateTime withDayOfYear(int dayOfYear) {
        return with(date.withDayOfYear(dayOfYear), time);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，小时已更改。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param hour  要在结果中设置的小时，从 0 到 23
     * @return 基于此日期时间并设置了请求小时的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果小时值无效
     */
    public LocalDateTime withHour(int hour) {
        LocalTime newTime = time.withHour(hour);
        return with(date, newTime);
    }

    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，分钟已更改。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param minute  要在结果中设置的分钟，从 0 到 59
     * @return 基于此日期时间并设置了请求分钟的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果分钟值无效
     */
    public LocalDateTime withMinute(int minute) {
        LocalTime newTime = time.withMinute(minute);
        return with(date, newTime);
    }

    /**
     * 返回一个基于此 {@code LocalDateTime} 的副本，秒已更改。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param second  要在结果中设置的秒，从 0 到 59
     * @return 基于此日期时间并设置了请求秒的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果秒值无效
     */
    public LocalDateTime withSecond(int second) {
        LocalTime newTime = time.withSecond(second);
        return with(date, newTime);
    }


                /**
     * 返回一个副本，其中此 {@code LocalDateTime} 的纳秒值已更改。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanoOfSecond  要在结果中设置的纳秒值，从 0 到 999,999,999
     * @return 一个基于此日期时间的 {@code LocalDateTime}，具有请求的纳秒值，不为空
     * @throws DateTimeException 如果纳秒值无效
     */
    public LocalDateTime withNano(int nanoOfSecond) {
        LocalTime newTime = time.withNano(nanoOfSecond);
        return with(date, newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code LocalDateTime} 的时间已截断。
     * <p>
     * 截断返回一个原始日期时间的副本，其中小于指定单位的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒和纳秒字段设置为零。
     * <p>
     * 单位必须具有一个 {@linkplain TemporalUnit#getDuration() 持续时间}，
     * 该持续时间可以无余数地整除标准天的长度。
     * 这包括 {@link ChronoUnit} 中提供的所有时间单位和 {@link ChronoUnit#DAYS 天}。
     * 其他单位会抛出异常。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param unit  要截断到的单位，不为空
     * @return 一个基于此日期时间的 {@code LocalDateTime}，时间已截断，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public LocalDateTime truncatedTo(TemporalUnit unit) {
        return with(date, time.truncatedTo(unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了添加的量。
     * <p>
     * 这返回一个 {@code LocalDateTime}，基于此实例，指定了添加的量。
     * 量通常是 {@link Period} 或 {@link Duration}，但可以是
     * 实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给量对象，通过调用
     * {@link TemporalAmount#addTo(Temporal)}。量实现可以自由地
     * 以任何方式实现添加，但通常会回调到 {@link #plus(long, TemporalUnit)}。
     * 请参阅量实现的文档，以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加的量，不为空
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了指定的量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period) amountToAdd;
            return with(date.plus(periodToAdd), time);
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (LocalDateTime) amountToAdd.addTo(this);
    }

    /**
     * 返回一个副本，其中指定了添加的量。
     * <p>
     * 这返回一个 {@code LocalDateTime}，基于此实例，以单位形式添加了量。
     * 如果无法添加量，因为单位不受支持或有其他原因，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则在此处实现添加。
     * 日期单位的添加方式与 {@link LocalDate#plus(long, TemporalUnit)} 相同。
     * 时间单位的添加方式与 {@link LocalTime#plus(long, TemporalUnit)} 相同，任何溢出的天数
     * 相当于使用 {@link #plusDays(long)}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用
     * {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 在这种情况下，单位确定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加到结果中的单位量，可以为负
     * @param unit  要添加的单位，不为空
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了指定的量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plusDays(amountToAdd / MICROS_PER_DAY).plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
                case MILLIS: return plusDays(amountToAdd / MILLIS_PER_DAY).plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000_000);
                case SECONDS: return plusSeconds(amountToAdd);
                case MINUTES: return plusMinutes(amountToAdd);
                case HOURS: return plusHours(amountToAdd);
                case HALF_DAYS: return plusDays(amountToAdd / 256).plusHours((amountToAdd % 256) * 12);  // no overflow (256 is multiple of 2)
            }
            return with(date.plus(amountToAdd, unit), time);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了添加的年数。
     * <p>
     * 此方法通过三个步骤将指定的量添加到年份字段：
     * <ol>
     * <li>将输入的年份添加到年份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将日调整到最后一个有效日</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）加上一年将导致无效日期 2009-02-29（平年）。
     * 为了避免返回无效结果，选择该月的最后一个有效日，即 2009-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param years  要添加的年数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusYears(long years) {
        LocalDate newDate = date.plusYears(years);
        return with(newDate, time);
    }

    /**
     * 返回一个副本，其中指定了添加的月数。
     * <p>
     * 此方法通过三个步骤将指定的量添加到月份字段：
     * <ol>
     * <li>将输入的月数添加到月份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将日调整到最后一个有效日</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 加上一个月将导致无效日期 2007-04-31。
     * 为了避免返回无效结果，选择该月的最后一个有效日，即 2007-04-30。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要添加的月数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了月数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusMonths(long months) {
        LocalDate newDate = date.plusMonths(months);
        return with(newDate, time);
    }

    /**
     * 返回一个副本，其中指定了添加的周数。
     * <p>
     * 此方法将指定的周数添加到天数字段，必要时递增月份和年份字段以确保结果有效。
     * 结果仅在最大/最小年份超出时无效。
     * <p>
     * 例如，2008-12-31 加上一周将导致 2009-01-07。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param weeks  要添加的周数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了周数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusWeeks(long weeks) {
        LocalDate newDate = date.plusWeeks(weeks);
        return with(newDate, time);
    }

    /**
     * 返回一个副本，其中指定了添加的天数。
     * <p>
     * 此方法将指定的量添加到天数字段，必要时递增月份和年份字段以确保结果有效。
     * 结果仅在最大/最小年份超出时无效。
     * <p>
     * 例如，2008-12-31 加上一天将导致 2009-01-01。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要添加的天数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了天数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusDays(long days) {
        LocalDate newDate = date.plusDays(days);
        return with(newDate, time);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了添加的小时数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hours  要添加的小时数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了小时数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, 1);
    }

    /**
     * 返回一个副本，其中指定了添加的分钟数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minutes  要添加的分钟数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了分钟数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, 1);
    }

    /**
     * 返回一个副本，其中指定了添加的秒数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param seconds  要添加的秒数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了秒数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, 1);
    }

    /**
     * 返回一个副本，其中指定了添加的纳秒数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanos  要添加的纳秒数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，添加了纳秒数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime plusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, 1);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了减去的量。
     * <p>
     * 这返回一个 {@code LocalDateTime}，基于此实例，指定了减去的量。
     * 量通常是 {@link Period} 或 {@link Duration}，但可以是
     * 实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给量对象，通过调用
     * {@link TemporalAmount#subtractFrom(Temporal)}。量实现可以自由地
     * 以任何方式实现减法，但通常会回调到 {@link #minus(long, TemporalUnit)}。
     * 请参阅量实现的文档，以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要减去的量，不为空
     * @return 一个基于此日期时间的 {@code LocalDateTime}，减去了指定的量，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period) amountToSubtract;
            return with(date.minus(periodToSubtract), time);
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (LocalDateTime) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，其中指定了减去的量。
     * <p>
     * 这返回一个 {@code LocalDateTime}，基于此实例，以单位形式减去了量。
     * 如果无法减去量，因为单位不受支持或有其他原因，将抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但量为负。
     * 有关添加（以及因此减法）的详细说明，请参阅该方法。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要从结果中减去的单位量，可以为负
     * @param unit  要减去的单位，不为空
     * @return 一个基于此日期时间的 {@code LocalDateTime}，减去了指定的量，不为空
     * @throws DateTimeException 如果无法减去
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了减去的年数。
     * <p>
     * 此方法通过三个步骤从年份字段中减去指定的量：
     * <ol>
     * <li>从年份字段中减去输入的年数</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将日调整到最后一个有效日</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）减去一年将导致无效日期 2009-02-29（平年）。
     * 为了避免返回无效结果，选择该月的最后一个有效日，即 2009-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param years  要减去的年数，可以为负
     * @return 一个基于此日期时间的 {@code LocalDateTime}，减去了年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusYears(long years) {
        return (years == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-years));
    }


                /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的月份数。
     * <p>
     * 此方法从月份字段中减去指定的数量，分为三个步骤：
     * <ol>
     * <li>从月份字段中减去输入的月份数</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将日期调整为该月的最后一天</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 减去一个月将导致无效日期 2007-04-31。为了避免返回无效结果，选择该月的最后一天 2007-04-30。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要减去的月份数，可以是负数
     * @return 基于此日期时间并减去月份数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusMonths(long months) {
        return (months == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-months));
    }

    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的周数。
     * <p>
     * 此方法从天数字段中减去指定的周数，必要时递减月份和年份字段以确保结果有效。
     * 结果仅在超出最大/最小年份时无效。
     * <p>
     * 例如，2009-01-07 减去一周将导致 2008-12-31。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param weeks  要减去的周数，可以是负数
     * @return 基于此日期时间并减去周数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusWeeks(long weeks) {
        return (weeks == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeks));
    }

    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的天数。
     * <p>
     * 此方法从天数字段中减去指定的数量，必要时递减月份和年份字段以确保结果有效。
     * 结果仅在超出最大/最小年份时无效。
     * <p>
     * 例如，2009-01-01 减去一天将导致 2008-12-31。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要减去的天数，可以是负数
     * @return 基于此日期时间并减去天数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusDays(long days) {
        return (days == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-days));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的小时数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hours  要减去的小时数，可以是负数
     * @return 基于此日期时间并减去小时数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, -1);
   }

    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的分钟数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minutes  要减去的分钟数，可以是负数
     * @return 基于此日期时间并减去分钟数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, -1);
    }

    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的秒数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param seconds  要减去的秒数，可以是负数
     * @return 基于此日期时间并减去秒数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, -1);
    }

    /**
     * 返回一个副本，该副本从这个 {@code LocalDateTime} 中减去指定数量的纳秒数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanos  要减去的纳秒数，可以是负数
     * @return 基于此日期时间并减去纳秒数的 {@code LocalDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDateTime minusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, -1);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本基于指定的周期进行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param newDate  用于计算的基础日期，不为空
     * @param hours  要添加的小时数，可以是负数
     * @param minutes 要添加的分钟数，可以是负数
     * @param seconds 要添加的秒数，可以是负数
     * @param nanos 要添加的纳秒数，可以是负数
     * @param sign  确定添加或减去的符号
     * @return 组合结果，不为空
     */
    private LocalDateTime plusWithOverflow(LocalDate newDate, long hours, long minutes, long seconds, long nanos, int sign) {
        // 9223372036854775808 long, 2147483648 int
        if ((hours | minutes | seconds | nanos) == 0) {
            return with(newDate, time);
        }
        long totDays = nanos / NANOS_PER_DAY +             //   max/24*60*60*1B
                seconds / SECONDS_PER_DAY +                //   max/24*60*60
                minutes / MINUTES_PER_DAY +                //   max/24*60
                hours / HOURS_PER_DAY;                     //   max/24
        totDays *= sign;                                   // total max*0.4237...
        long totNanos = nanos % NANOS_PER_DAY +                    //   max  86400000000000
                (seconds % SECONDS_PER_DAY) * NANOS_PER_SECOND +   //   max  86400000000000
                (minutes % MINUTES_PER_DAY) * NANOS_PER_MINUTE +   //   max  86400000000000
                (hours % HOURS_PER_DAY) * NANOS_PER_HOUR;          //   max  86400000000000
        long curNoD = time.toNanoOfDay();                       //   max  86400000000000
        totNanos = totNanos * sign + curNoD;                    // total 432000000000000
        totDays += Math.floorDiv(totNanos, NANOS_PER_DAY);
        long newNoD = Math.floorMod(totNanos, NANOS_PER_DAY);
        LocalTime newTime = (newNoD == curNoD ? time : LocalTime.ofNanoOfDay(newNoD));
        return with(newDate.plusDays(totDays), newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略查询此日期时间。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询文档以了解此方法的结果。
     * <p>
     * 通过调用指定查询上的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override  // 重写以提供 Javadoc
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R) date;
        }
        return ChronoLocalDateTime.super.query(query);
    }

    /**
     * 调整指定的时间对象，使其具有与本对象相同的日期和时间。
     * <p>
     * 此方法返回一个与输入类型相同的时间对象，但日期和时间已更改为与本对象相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)} 两次，传递 {@link ChronoField#EPOCH_DAY} 和
     * {@link ChronoField#NANO_OF_DAY} 作为字段。
     * <p>
     * 在大多数情况下，建议反转调用模式，使用 {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisLocalDateTime.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalDateTime);
     * </pre>
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc
    public Temporal adjustInto(Temporal temporal) {
        return ChronoLocalDateTime.super.adjustInto(temporal);
    }

    /**
     * 计算到另一个日期时间的时间量，以指定的单位表示。
     * <p>
     * 此方法计算两个 {@code LocalDateTime} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的日期时间。
     * 如果终点在起点之前，结果将为负数。
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为 {@code LocalDateTime}。
     * 例如，可以使用 {@code startDateTime.until(endDateTime, DAYS)} 计算两个日期时间之间的天数。
     * <p>
     * 计算返回一个整数，表示两个日期时间之间的完整单位数。
     * 例如，2012-06-15T00:00 和 2012-08-14T23:59 之间的月份数仅为一个月，因为还差一分钟才到两个月。
     * <p>
     * 有两 种等效的方法使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更易读。
     * <p>
     * 该计算在 {@link ChronoUnit} 的实现中完成。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS} 和 {@code HALF_DAYS}、{@code DAYS}、
     * {@code WEEKS}、{@code MONTHS}、{@code YEARS}、{@code DECADES}、
     * {@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并传递 {@code this} 作为第一个参数和转换后的输入时间作为第二个参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param endExclusive  结束日期，不为空，转换为 {@code LocalDateTime}
     * @param unit  要测量的时间量的单位，不为空
     * @return 从本日期时间到结束日期时间的时间量
     * @throws DateTimeException 如果无法计算时间量，或结束时间无法转换为 {@code LocalDateTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDateTime end = LocalDateTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            if (unit.isTimeBased()) {
                long amount = date.daysUntil(end.date);
                if (amount == 0) {
                    return time.until(end.time, unit);
                }
                long timePart = end.time.toNanoOfDay() - time.toNanoOfDay();
                if (amount > 0) {
                    amount--;  // 安全
                    timePart += NANOS_PER_DAY;  // 安全
                } else {
                    amount++;  // 安全
                    timePart -= NANOS_PER_DAY;  // 安全
                }
                switch ((ChronoUnit) unit) {
                    case NANOS:
                        amount = Math.multiplyExact(amount, NANOS_PER_DAY);
                        break;
                    case MICROS:
                        amount = Math.multiplyExact(amount, MICROS_PER_DAY);
                        timePart = timePart / 1000;
                        break;
                    case MILLIS:
                        amount = Math.multiplyExact(amount, MILLIS_PER_DAY);
                        timePart = timePart / 1_000_000;
                        break;
                    case SECONDS:
                        amount = Math.multiplyExact(amount, SECONDS_PER_DAY);
                        timePart = timePart / NANOS_PER_SECOND;
                        break;
                    case MINUTES:
                        amount = Math.multiplyExact(amount, MINUTES_PER_DAY);
                        timePart = timePart / NANOS_PER_MINUTE;
                        break;
                    case HOURS:
                        amount = Math.multiplyExact(amount, HOURS_PER_DAY);
                        timePart = timePart / NANOS_PER_HOUR;
                        break;
                    case HALF_DAYS:
                        amount = Math.multiplyExact(amount, 2);
                        timePart = timePart / (NANOS_PER_HOUR * 12);
                        break;
                }
                return Math.addExact(amount, timePart);
            }
            LocalDate endDate = end.date;
            if (endDate.isAfter(date) && end.time.isBefore(time)) {
                endDate = endDate.minusDays(1);
            } else if (endDate.isBefore(date) && end.time.isAfter(time)) {
                endDate = endDate.plusDays(1);
            }
            return date.until(endDate, unit);
        }
        return unit.between(this, end);
    }


                /**
     * 使用指定的格式化器格式化此日期时间。
     * <p>
     * 该日期时间将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不能为空
     * @return 格式化的日期时间字符串，不能为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    @Override  // 重写以用于 Javadoc 和性能
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间与偏移量组合以创建一个 {@code OffsetDateTime}。
     * <p>
     * 这将返回一个由该日期时间在指定偏移量下形成的 {@code OffsetDateTime}。
     * 所有可能的日期时间和偏移量组合都是有效的。
     *
     * @param offset  要组合的偏移量，不能为空
     * @return 由该日期时间和指定偏移量形成的偏移日期时间，不能为空
     */
    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.of(this, offset);
    }

    /**
     * 将此日期时间与时区组合以创建一个 {@code ZonedDateTime}。
     * <p>
     * 这将返回一个由该日期时间在指定时区下形成的 {@code ZonedDateTime}。
     * 结果将尽可能匹配该日期时间。
     * 时区规则（如夏令时）意味着并非所有本地日期时间都适用于指定的时区，因此本地日期时间可能会进行调整。
     * <p>
     * 本地日期时间将解析为时间线上的单个瞬间。
     * 这是通过找到 UTC/Greenwich 为本地日期时间定义的有效偏移量来实现的。
     * <p>
     * 在大多数情况下，每个本地日期时间只有一个有效的偏移量。
     * 在重叠的情况下，即时钟被拨回，有两个有效的偏移量。
     * 此方法使用较早的偏移量，通常对应于“夏季”。
     * <p>
     * 在间隙的情况下，即时钟向前跳动，没有有效的偏移量。
     * 相反，本地日期时间将被调整为比原来晚的时长。
     * 对于典型的夏令时变化，本地日期时间将被调整为晚一小时，通常对应于“夏季”。
     * <p>
     * 要在重叠时使用较晚的偏移量，请在该方法的结果上调用
     * {@link ZonedDateTime#withLaterOffsetAtOverlap()}。
     * 要在有间隙或重叠时抛出异常，请使用
     * {@link ZonedDateTime#ofStrict(LocalDateTime, ZoneOffset, ZoneId)}。
     *
     * @param zone  要使用的时区，不能为空
     * @return 由该日期时间形成的带时区的日期时间，不能为空
     */
    @Override
    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.of(this, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此日期时间与另一个日期时间。
     * <p>
     * 比较主要基于日期时间，从最早到最晚。
     * 它是“与 equals 一致”的，如 {@link Comparable} 所定义。
     * <p>
     * 如果所有被比较的日期时间都是 {@code LocalDateTime} 的实例，
     * 则比较将完全基于日期时间。
     * 如果一些被比较的日期时间属于不同的历法系统，则还会考虑历法系统，参见 {@link ChronoLocalDateTime#compareTo}。
     *
     * @param other  要比较的其他日期时间，不能为空
     * @return 比较值，负数表示小于，正数表示大于
     */
    @Override  // 重写以用于 Javadoc 和性能
    public int compareTo(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return compareTo0((LocalDateTime) other);
        }
        return ChronoLocalDateTime.super.compareTo(other);
    }

    private int compareTo0(LocalDateTime other) {
        int cmp = date.compareTo0(other.toLocalDate());
        if (cmp == 0) {
            cmp = time.compareTo(other.toLocalTime());
        }
        return cmp;
    }

    /**
     * 检查此日期时间是否在指定的日期时间之后。
     * <p>
     * 这会检查此日期时间是否表示本地时间线上在其他日期时间之后的点。
     * <pre>
     *   LocalDate a = LocalDateTime.of(2012, 6, 30, 12, 00);
     *   LocalDate b = LocalDateTime.of(2012, 7, 1, 12, 00);
     *   a.isAfter(b) == false
     *   a.isAfter(a) == false
     *   b.isAfter(a) == true
     * </pre>
     * <p>
     * 此方法仅考虑两个日期时间在本地时间线上的位置。
     * 它不考虑历法系统。
     * 这与 {@link #compareTo(ChronoLocalDateTime)} 中的比较不同，
     * 但与 {@link ChronoLocalDateTime#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期时间，不能为空
     * @return 如果此日期时间在指定的日期时间之后，则返回 true
     */
    @Override  // 重写以用于 Javadoc 和性能
    public boolean isAfter(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return compareTo0((LocalDateTime) other) > 0;
        }
        return ChronoLocalDateTime.super.isAfter(other);
    }

    /**
     * 检查此日期时间是否在指定的日期时间之前。
     * <p>
     * 这会检查此日期时间是否表示本地时间线上在其他日期时间之前的点。
     * <pre>
     *   LocalDate a = LocalDateTime.of(2012, 6, 30, 12, 00);
     *   LocalDate b = LocalDateTime.of(2012, 7, 1, 12, 00);
     *   a.isBefore(b) == true
     *   a.isBefore(a) == false
     *   b.isBefore(a) == false
     * </pre>
     * <p>
     * 此方法仅考虑两个日期时间在本地时间线上的位置。
     * 它不考虑历法系统。
     * 这与 {@link #compareTo(ChronoLocalDateTime)} 中的比较不同，
     * 但与 {@link ChronoLocalDateTime#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期时间，不能为空
     * @return 如果此日期时间在指定的日期时间之前，则返回 true
     */
    @Override  // 重写以用于 Javadoc 和性能
    public boolean isBefore(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return compareTo0((LocalDateTime) other) < 0;
        }
        return ChronoLocalDateTime.super.isBefore(other);
    }

    /**
     * 检查此日期时间是否等于指定的日期时间。
     * <p>
     * 这会检查此日期时间是否表示本地时间线上与另一个日期时间相同的点。
     * <pre>
     *   LocalDate a = LocalDateTime.of(2012, 6, 30, 12, 00);
     *   LocalDate b = LocalDateTime.of(2012, 7, 1, 12, 00);
     *   a.isEqual(b) == false
     *   a.isEqual(a) == true
     *   b.isEqual(a) == false
     * </pre>
     * <p>
     * 此方法仅考虑两个日期时间在本地时间线上的位置。
     * 它不考虑历法系统。
     * 这与 {@link #compareTo(ChronoLocalDateTime)} 中的比较不同，
     * 但与 {@link ChronoLocalDateTime#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期时间，不能为空
     * @return 如果此日期时间等于指定的日期时间，则返回 true
     */
    @Override  // 重写以用于 Javadoc 和性能
    public boolean isEqual(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return compareTo0((LocalDateTime) other) == 0;
        }
        return ChronoLocalDateTime.super.isEqual(other);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期时间是否等于另一个日期时间。
     * <p>
     * 比较此 {@code LocalDateTime} 与另一个，确保日期时间相同。
     * 只有 {@code LocalDateTime} 类型的对象才会进行比较，其他类型返回 false。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期时间等于其他日期时间，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDateTime) {
            LocalDateTime other = (LocalDateTime) obj;
            return date.equals(other.date) && time.equals(other.time);
        }
        return false;
    }

    /**
     * 为此日期时间生成哈希码。
     *
     * @return 适合的哈希码
     */
    @Override
    public int hashCode() {
        return date.hashCode() ^ time.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间输出为 {@code String}，例如 {@code 2007-12-03T10:15:30}。
     * <p>
     * 输出将是以下 ISO-8601 格式之一：
     * <ul>
     * <li>{@code uuuu-MM-dd'T'HH:mm}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSS}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSSSSS}</li>
     * <li>{@code uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS}</li>
     * </ul>
     * 使用的格式将是能输出时间完整值的最短格式，省略的部分默认为零。
     *
     * @return 此日期时间的字符串表示，不能为空
     */
    @Override
    public String toString() {
        return date.toString() + 'T' + time.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(5);  // 标识一个 LocalDateTime
     *  // <a href="../../serialized-form.html#java.time.LocalDate">日期</a>，不包括一个字节的头部
     *  // <a href="../../serialized-form.html#java.time.LocalTime">时间</a>，不包括一个字节的头部
     * </pre>
     *
     * @return {@code Ser} 的实例，不能为空
     */
    private Object writeReplace() {
        return new Ser(Ser.LOCAL_DATE_TIME_TYPE, this);
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

    void writeExternal(DataOutput out) throws IOException {
        date.writeExternal(out);
        time.writeExternal(out);
    }

    static LocalDateTime readExternal(DataInput in) throws IOException {
        LocalDate date = LocalDate.readExternal(in);
        LocalTime time = LocalTime.readExternal(in);
        return LocalDateTime.of(date, time);
    }

}
