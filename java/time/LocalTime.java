
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

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MICRO_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoUnit.NANOS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
import java.util.Objects;

/**
 * ISO-8601 日历系统中的一个不带时区的时间，例如 {@code 10:15:30}。
 * <p>
 * {@code LocalTime} 是一个不可变的日期时间对象，表示时间，通常视为小时-分钟-秒。
 * 时间表示到纳秒精度。
 * 例如，值 "13:45.30.123456789" 可以存储在 {@code LocalTime} 中。
 * <p>
 * 该类不存储或表示日期或时区。
 * 相反，它是对墙钟上看到的本地时间的描述。
 * 它不能在没有额外信息（如偏移量或时区）的情况下表示时间线上的一个瞬间。
 * <p>
 * ISO-8601 日历系统是当今世界上大多数地区使用的现代民用日历系统。
 * 此 API 假设所有日历系统使用相同的表示形式，即此类，用于一天中的时间。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code LocalTime} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})，身份哈希码，或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class LocalTime
        implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable {

    /**
     * 支持的最小 {@code LocalTime}，'00:00'。
     * 这是当天开始时的午夜时间。
     */
    public static final LocalTime MIN;
    /**
     * 支持的最大 {@code LocalTime}，'23:59:59.999999999'。
     * 这是当天结束时午夜前的时间。
     */
    public static final LocalTime MAX;
    /**
     * 当天开始时的午夜时间，'00:00'。
     */
    public static final LocalTime MIDNIGHT;
    /**
     * 当天中午的时间，'12:00'。
     */
    public static final LocalTime NOON;
    /**
     * 每小时的本地时间常量。
     */
    private static final LocalTime[] HOURS = new LocalTime[24];
    static {
        for (int i = 0; i < HOURS.length; i++) {
            HOURS[i] = new LocalTime(i, 0, 0, 0);
        }
        MIDNIGHT = HOURS[0];
        NOON = HOURS[12];
        MIN = HOURS[0];
        MAX = new LocalTime(23, 59, 59, 999_999_999);
    }

    /**
     * 每天的小时数。
     */
    static final int HOURS_PER_DAY = 24;
    /**
     * 每小时的分钟数。
     */
    static final int MINUTES_PER_HOUR = 60;
    /**
     * 每天的分钟数。
     */
    static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    /**
     * 每分钟的秒数。
     */
    static final int SECONDS_PER_MINUTE = 60;
    /**
     * 每小时的秒数。
     */
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * 每天的秒数。
     */
    static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    /**
     * 每天的毫秒数。
     */
    static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L;
    /**
     * 每天的微秒数。
     */
    static final long MICROS_PER_DAY = SECONDS_PER_DAY * 1000_000L;
    /**
     * 每秒的纳秒数。
     */
    static final long NANOS_PER_SECOND = 1000_000_000L;
    /**
     * 每分钟的纳秒数。
     */
    static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;
    /**
     * 每小时的纳秒数。
     */
    static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * 每天的纳秒数。
     */
    static final long NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY;

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 6414437269572265201L;

    /**
     * 小时。
     */
    private final byte hour;
    /**
     * 分钟。
     */
    private final byte minute;
    /**
     * 秒。
     */
    private final byte second;
    /**
     * 纳秒。
     */
    private final int nano;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前时间。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前时间。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前时间，不为空
     */
    public static LocalTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前时间。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前时间，不为空
     */
    public static LocalTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前时间。
     * <p>
     * 这将查询指定的时钟以获取当前时间。
     * 使用此方法允许使用替代时钟进行测试。
     * 可以通过 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前时间，不为空
     */
    public static LocalTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        // 内联 OffsetTime 工厂以避免创建对象和 InstantProvider 检查
        final Instant now = clock.instant();  // 调用一次
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        long localSecond = now.getEpochSecond() + offset.getTotalSeconds();  // 溢出在后面捕获
        int secsOfDay = (int) Math.floorMod(localSecond, SECONDS_PER_DAY);
        return ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + now.getNano());
    }

    //-----------------------------------------------------------------------
    /**
     * 从小时和分钟获取 {@code LocalTime} 的实例。
     * <p>
     * 这将返回一个具有指定小时和分钟的 {@code LocalTime}。
     * 秒和纳秒字段将设置为零。
     *
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @return 本地时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public static LocalTime of(int hour, int minute) {
        HOUR_OF_DAY.checkValidValue(hour);
        if (minute == 0) {
            return HOURS[hour];  // 为了性能
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return new LocalTime(hour, minute, 0, 0);
    }

    /**
     * 从小时、分钟和秒获取 {@code LocalTime} 的实例。
     * <p>
     * 这将返回一个具有指定小时、分钟和秒的 {@code LocalTime}。
     * 纳秒字段将设置为零。
     *
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @return 本地时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public static LocalTime of(int hour, int minute, int second) {
        HOUR_OF_DAY.checkValidValue(hour);
        if ((minute | second) == 0) {
            return HOURS[hour];  // 为了性能
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        return new LocalTime(hour, minute, second, 0);
    }

    /**
     * 从小时、分钟、秒和纳秒获取 {@code LocalTime} 的实例。
     * <p>
     * 这将返回一个具有指定小时、分钟、秒和纳秒的 {@code LocalTime}。
     *
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @return 本地时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        HOUR_OF_DAY.checkValidValue(hour);
        MINUTE_OF_HOUR.checkValidValue(minute);
        SECOND_OF_MINUTE.checkValidValue(second);
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour, minute, second, nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * 从一天中的秒数获取 {@code LocalTime} 的实例。
     * <p>
     * 这将返回一个具有指定一天中的秒数的 {@code LocalTime}。
     * 纳秒字段将设置为零。
     *
     * @param secondOfDay  一天中的秒数，从 {@code 0} 到 {@code 24 * 60 * 60 - 1}
     * @return 本地时间，不为空
     * @throws DateTimeException 如果一天中的秒数无效
     */
    public static LocalTime ofSecondOfDay(long secondOfDay) {
        SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
        secondOfDay -= hours * SECONDS_PER_HOUR;
        int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
        secondOfDay -= minutes * SECONDS_PER_MINUTE;
        return create(hours, minutes, (int) secondOfDay, 0);
    }

    /**
     * 从一天中的纳秒数获取 {@code LocalTime} 的实例。
     * <p>
     * 这将返回一个具有指定一天中的纳秒数的 {@code LocalTime}。
     *
     * @param nanoOfDay  一天中的纳秒数，从 {@code 0} 到 {@code 24 * 60 * 60 * 1,000,000,000 - 1}
     * @return 本地时间，不为空
     * @throws DateTimeException 如果一天中的纳秒数无效
     */
    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
        nanoOfDay -= hours * NANOS_PER_HOUR;
        int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
        nanoOfDay -= minutes * NANOS_PER_MINUTE;
        int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
        nanoOfDay -= seconds * NANOS_PER_SECOND;
        return create(hours, minutes, seconds, (int) nanoOfDay);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code LocalTime} 的实例。
     * <p>
     * 这将基于指定的时间对象获取本地时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code LocalTime} 的实例。
     * <p>
     * 转换使用 {@link TemporalQueries#localTime()} 查询，该查询依赖于提取
     * {@link ChronoField#NANO_OF_DAY NANO_OF_DAY} 字段。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 函数接口的签名，
     * 允许通过方法引用使用它，例如 {@code LocalTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 本地时间，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code LocalTime}
     */
    public static LocalTime from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalTime time = temporal.query(TemporalQueries.localTime());
        if (time == null) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 LocalTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName());
        }
        return time;
    }


                //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 10:15}）获取 {@code LocalTime} 的实例。
     * <p>
     * 字符串必须表示一个有效的时间，并使用
     * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_TIME} 进行解析。
     *
     * @param text  要解析的文本，如 "10:15:30"，不能为空
     * @return 解析后的地方时间，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code LocalTime} 的实例。
     * <p>
     * 文本使用格式化器进行解析，返回一个时间。
     *
     * @param text  要解析的文本，不能为空
     * @param formatter  要使用的格式化器，不能为空
     * @return 解析后的地方时间，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalTime::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 从小时、分钟、秒和纳秒字段创建一个地方时间。
     * <p>
     * 该工厂方法可能返回一个缓存的值，但应用程序不应依赖于此。
     *
     * @param hour  要表示的小时，验证范围为 0 到 23
     * @param minute  要表示的分钟，验证范围为 0 到 59
     * @param second  要表示的秒，验证范围为 0 到 59
     * @param nanoOfSecond  要表示的纳秒，验证范围为 0 到 999,999,999
     * @return 地方时间，不能为空
     */
    private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {
        if ((minute | second | nanoOfSecond) == 0) {
            return HOURS[hour];
        }
        return new LocalTime(hour, minute, second, nanoOfSecond);
    }

    /**
     * 构造函数，已预先验证。
     *
     * @param hour  要表示的小时，验证范围为 0 到 23
     * @param minute  要表示的分钟，验证范围为 0 到 59
     * @param second  要表示的秒，验证范围为 0 到 59
     * @param nanoOfSecond  要表示的纳秒，验证范围为 0 到 999,999,999
     */
    private LocalTime(int hour, int minute, int second, int nanoOfSecond) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
        this.nano = nanoOfSecond;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否支持。
     * <p>
     * 这个方法检查这个时间是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段包括：
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
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 字段是否支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此时间上支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 这个方法检查指定的单位是否可以添加到或从这个时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位包括：
     * <ul>
     * <li>{@code NANOS}
     * <li>{@code MICROS}
     * <li>{@code MILLIS}
     * <li>{@code SECONDS}
     * <li>{@code MINUTES}
     * <li>{@code HOURS}
     * <li>{@code HALF_DAYS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递来获得。
     * 单位是否支持由单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override  // 为 Javadoc 覆盖
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 这个时间用于增强返回范围的准确性。
     * 如果由于字段不支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不能为空
     * @return 字段的有效值范围，不能为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     */
    @Override  // 为 Javadoc 覆盖
    public ValueRange range(TemporalField field) {
        return Temporal.super.range(field);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 这个方法查询这个时间指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于这个时间返回有效的值，
     * 但 {@code NANO_OF_DAY} 和 {@code MICRO_OF_DAY} 由于值太大无法放入 {@code int} 而抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或
     *         字段的值超出有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持或
     *         值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 为 Javadoc 和性能覆盖
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return Temporal.super.get(field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 这个方法查询这个时间指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于这个时间返回有效的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == NANO_OF_DAY) {
                return toNanoOfDay();
            }
            if (field == MICRO_OF_DAY) {
                return toNanoOfDay() / 1000;
            }
            return get0(field);
        }
        return field.getFrom(this);
    }

    private int get0(TemporalField field) {
        switch ((ChronoField) field) {
            case NANO_OF_SECOND: return nano;
            case NANO_OF_DAY: throw new UnsupportedTemporalTypeException("Invalid field 'NanoOfDay' for get() method, use getLong() instead");
            case MICRO_OF_SECOND: return nano / 1000;
            case MICRO_OF_DAY: throw new UnsupportedTemporalTypeException("Invalid field 'MicroOfDay' for get() method, use getLong() instead");
            case MILLI_OF_SECOND: return nano / 1000_000;
            case MILLI_OF_DAY: return (int) (toNanoOfDay() / 1000_000);
            case SECOND_OF_MINUTE: return second;
            case SECOND_OF_DAY: return toSecondOfDay();
            case MINUTE_OF_HOUR: return minute;
            case MINUTE_OF_DAY: return hour * 60 + minute;
            case HOUR_OF_AMPM: return hour % 12;
            case CLOCK_HOUR_OF_AMPM: int ham = hour % 12; return (ham % 12 == 0 ? 12 : ham);
            case HOUR_OF_DAY: return hour;
            case CLOCK_HOUR_OF_DAY: return (hour == 0 ? 24 : hour);
            case AMPM_OF_DAY: return hour / 12;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取小时字段。
     *
     * @return 小时，范围为 0 到 23
     */
    public int getHour() {
        return hour;
    }

    /**
     * 获取分钟字段。
     *
     * @return 分钟，范围为 0 到 59
     */
    public int getMinute() {
        return minute;
    }

    /**
     * 获取秒字段。
     *
     * @return 秒，范围为 0 到 59
     */
    public int getSecond() {
        return second;
    }

    /**
     * 获取纳秒字段。
     *
     * @return 纳秒，范围为 0 到 999,999,999
     */
    public int getNano() {
        return nano;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回调整后的时间副本。
     * <p>
     * 这个方法返回一个基于此时间的 {@code LocalTime}，时间已调整。
     * 调整使用指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 简单的调整器可能只是设置一个字段，如小时字段。
     * 更复杂的调整器可能将时间设置为一天的最后一小时。
     * <p>
     * 该方法的结果通过调用
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数获得。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不能为空
     * @return 基于 {@code this} 并进行了调整的 {@code LocalTime}，不能为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalTime) {
            return (LocalTime) adjuster;
        }
        return (LocalTime) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此时间的副本，指定字段设置为新值。
     * <p>
     * 这个方法返回一个基于此时间的 {@code LocalTime}，指定字段的值已更改。
     * 如果由于字段不支持或其他原因无法设置值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此实现。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code NANO_OF_SECOND} -
     *  返回一个具有指定纳秒的 {@code LocalTime}。
     *  小时、分钟和秒将保持不变。
     * <li>{@code NANO_OF_DAY} -
     *  返回一个具有指定纳秒的 {@code LocalTime}。
     *  这将完全替换时间，等同于 {@link #ofNanoOfDay(long)}。
     * <li>{@code MICRO_OF_SECOND} -
     *  返回一个具有指定微秒乘以 1,000 的纳秒的 {@code LocalTime}。
     *  小时、分钟和秒将保持不变。
     * <li>{@code MICRO_OF_DAY} -
     *  返回一个具有指定微秒的 {@code LocalTime}。
     *  这将完全替换时间，等同于使用 {@link #ofNanoOfDay(long)}
     *  并将微秒乘以 1,000。
     * <li>{@code MILLI_OF_SECOND} -
     *  返回一个具有指定毫秒乘以 1,000,000 的纳秒的 {@code LocalTime}。
     *  小时、分钟和秒将保持不变。
     * <li>{@code MILLI_OF_DAY} -
     *  返回一个具有指定毫秒的 {@code LocalTime}。
     *  这将完全替换时间，等同于使用 {@link #ofNanoOfDay(long)}
     *  并将毫秒乘以 1,000,000。
     * <li>{@code SECOND_OF_MINUTE} -
     *  返回一个具有指定秒的 {@code LocalTime}。
     *  小时、分钟和纳秒将保持不变。
     * <li>{@code SECOND_OF_DAY} -
     *  返回一个具有指定秒的 {@code LocalTime}。
     *  纳秒将保持不变。
     * <li>{@code MINUTE_OF_HOUR} -
     *  返回一个具有指定分钟的 {@code LocalTime}。
     *  小时、秒和纳秒将保持不变。
     * <li>{@code MINUTE_OF_DAY} -
     *  返回一个具有指定分钟的 {@code LocalTime}。
     *  秒和纳秒将保持不变。
     * <li>{@code HOUR_OF_AMPM} -
     *  返回一个具有指定小时的 {@code LocalTime}。
     *  上午/下午、分钟、秒和纳秒将保持不变。
     * <li>{@code CLOCK_HOUR_OF_AMPM} -
     *  返回一个具有指定时钟小时的 {@code LocalTime}。
     *  上午/下午、分钟、秒和纳秒将保持不变。
     * <li>{@code HOUR_OF_DAY} -
     *  返回一个具有指定小时的 {@code LocalTime}。
     *  分钟、秒和纳秒将保持不变。
     * <li>{@code CLOCK_HOUR_OF_DAY} -
     *  返回一个具有指定时钟小时的 {@code LocalTime}。
     *  分钟、秒和纳秒将保持不变。
     * <li>{@code AMPM_OF_DAY} -
     *  返回一个具有指定上午/下午的 {@code LocalTime}。
     *  小时、分钟、秒和纳秒将保持不变。
     * </ul>
     * <p>
     * 在所有情况下，如果新值超出字段的有效值范围，
     * 则抛出 {@code DateTimeException}。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.adjustInto(Temporal, long)}
     * 并将 {@code this} 作为参数传递来获得。
     * 在这种情况下，字段决定是否以及如何调整实例。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不能为空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code LocalTime}，不能为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case NANO_OF_SECOND: return withNano((int) newValue);
                case NANO_OF_DAY: return LocalTime.ofNanoOfDay(newValue);
                case MICRO_OF_SECOND: return withNano((int) newValue * 1000);
                case MICRO_OF_DAY: return LocalTime.ofNanoOfDay(newValue * 1000);
                case MILLI_OF_SECOND: return withNano((int) newValue * 1000_000);
                case MILLI_OF_DAY: return LocalTime.ofNanoOfDay(newValue * 1000_000);
                case SECOND_OF_MINUTE: return withSecond((int) newValue);
                case SECOND_OF_DAY: return plusSeconds(newValue - toSecondOfDay());
                case MINUTE_OF_HOUR: return withMinute((int) newValue);
                case MINUTE_OF_DAY: return plusMinutes(newValue - (hour * 60 + minute));
                case HOUR_OF_AMPM: return plusHours(newValue - (hour % 12));
                case CLOCK_HOUR_OF_AMPM: return plusHours((newValue == 12 ? 0 : newValue) - (hour % 12));
                case HOUR_OF_DAY: return withHour((int) newValue);
                case CLOCK_HOUR_OF_DAY: return withHour((int) (newValue == 24 ? 0 : newValue));
                case AMPM_OF_DAY: return plusHours((newValue - (hour / 12)) * 12);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.adjustInto(this, newValue);
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code LocalTime} 的小时数已更改。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hour  要在结果中设置的小时数，从 0 到 23
     * @return 基于此时间并具有请求小时数的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果小时值无效
     */
    public LocalTime withHour(int hour) {
        if (this.hour == hour) {
            return this;
        }
        HOUR_OF_DAY.checkValidValue(hour);
        return create(hour, minute, second, nano);
    }

    /**
     * 返回一个副本，其中此 {@code LocalTime} 的分钟数已更改。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minute  要在结果中设置的分钟数，从 0 到 59
     * @return 基于此时间并具有请求分钟数的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果分钟值无效
     */
    public LocalTime withMinute(int minute) {
        if (this.minute == minute) {
            return this;
        }
        MINUTE_OF_HOUR.checkValidValue(minute);
        return create(hour, minute, second, nano);
    }

    /**
     * 返回一个副本，其中此 {@code LocalTime} 的秒数已更改。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param second  要在结果中设置的秒数，从 0 到 59
     * @return 基于此时间并具有请求秒数的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果秒值无效
     */
    public LocalTime withSecond(int second) {
        if (this.second == second) {
            return this;
        }
        SECOND_OF_MINUTE.checkValidValue(second);
        return create(hour, minute, second, nano);
    }

    /**
     * 返回一个副本，其中此 {@code LocalTime} 的纳秒数已更改。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanoOfSecond  要在结果中设置的纳秒数，从 0 到 999,999,999
     * @return 基于此时间并具有请求纳秒数的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果纳秒值无效
     */
    public LocalTime withNano(int nanoOfSecond) {
        if (this.nano == nanoOfSecond) {
            return this;
        }
        NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return create(hour, minute, second, nanoOfSecond);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code LocalTime} 的时间已截断。
     * <p>
     * 截断返回一个副本，其中指定单位以下的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒数和纳秒数字段设置为零。
     * <p>
     * 单位必须具有一个 {@linkplain TemporalUnit#getDuration() 持续时间}，该持续时间可以整除标准天的长度。
     * 这包括所有 {@link ChronoUnit} 中提供的时间单位和 {@link ChronoUnit#DAYS 天}。其他单位会抛出异常。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此时间并已截断时间的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public LocalTime truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() > SECONDS_PER_DAY) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if ((NANOS_PER_DAY % dur) != 0) {
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod = toNanoOfDay();
        return ofNanoOfDay((nod / dur) * dur);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了添加的量。
     * <p>
     * 这返回一个基于此时间的 {@code LocalTime}，指定了添加的量。
     * 该量通常是 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给量对象，通过调用 {@link TemporalAmount#addTo(Temporal)}。量实现可以自由地以任何方式实现添加，但通常会回调 {@link #plus(long, TemporalUnit)}。请参阅量实现的文档，以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加的量，不为空
     * @return 基于此时间并已进行添加的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果无法进行添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime plus(TemporalAmount amountToAdd) {
        return (LocalTime) amountToAdd.addTo(this);
    }

    /**
     * 返回一个副本，其中指定了添加的量。
     * <p>
     * 这返回一个基于此时间的 {@code LocalTime}，指定了以单位形式添加的量。如果无法添加该量，因为单位不受支持或其他原因，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则添加在此处实现。支持的字段行为如下：
     * <ul>
     * <li>{@code NANOS} -
     *  返回一个具有指定纳秒数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusNanos(long)}。
     * <li>{@code MICROS} -
     *  返回一个具有指定微秒数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusNanos(long)}，但量乘以 1,000。
     * <li>{@code MILLIS} -
     *  返回一个具有指定毫秒数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusNanos(long)}，但量乘以 1,000,000。
     * <li>{@code SECONDS} -
     *  返回一个具有指定秒数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusSeconds(long)}。
     * <li>{@code MINUTES} -
     *  返回一个具有指定分钟数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusMinutes(long)}。
     * <li>{@code HOURS} -
     *  返回一个具有指定小时数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusHours(long)}。
     * <li>{@code HALF_DAYS} -
     *  返回一个具有指定半天数添加的 {@code LocalTime}。
     *  这相当于 {@link #plusHours(long)}，但量乘以 12。
     * </ul>
     * <p>
     * 所有其他 {@code ChronoUnit} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则此方法的结果是通过调用 {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递获得的。在这种情况下，单位决定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加到结果中的单位量，可以为负
     * @param unit  要添加的单位量，不为空
     * @return 基于此时间并已添加指定量的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果无法进行添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
                case MILLIS: return plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000_000);
                case SECONDS: return plusSeconds(amountToAdd);
                case MINUTES: return plusMinutes(amountToAdd);
                case HOURS: return plusHours(amountToAdd);
                case HALF_DAYS: return plusHours((amountToAdd % 2) * 12);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了添加的小时数。
     * <p>
     * 这将指定的小时数添加到此时间，返回一个新时间。计算会绕过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hoursToAdd  要添加的小时数，可以为负
     * @return 基于此时间并已添加小时数的 {@code LocalTime}，不为空
     */
    public LocalTime plusHours(long hoursToAdd) {
        if (hoursToAdd == 0) {
            return this;
        }
        int newHour = ((int) (hoursToAdd % HOURS_PER_DAY) + hour + HOURS_PER_DAY) % HOURS_PER_DAY;
        return create(newHour, minute, second, nano);
    }

    /**
     * 返回一个副本，其中指定了添加的分钟数。
     * <p>
     * 这将指定的分钟数添加到此时间，返回一个新时间。计算会绕过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minutesToAdd  要添加的分钟数，可以为负
     * @return 基于此时间并已添加分钟数的 {@code LocalTime}，不为空
     */
    public LocalTime plusMinutes(long minutesToAdd) {
        if (minutesToAdd == 0) {
            return this;
        }
        int mofd = hour * MINUTES_PER_HOUR + minute;
        int newMofd = ((int) (minutesToAdd % MINUTES_PER_DAY) + mofd + MINUTES_PER_DAY) % MINUTES_PER_DAY;
        if (mofd == newMofd) {
            return this;
        }
        int newHour = newMofd / MINUTES_PER_HOUR;
        int newMinute = newMofd % MINUTES_PER_HOUR;
        return create(newHour, newMinute, second, nano);
    }

    /**
     * 返回一个副本，其中指定了添加的秒数。
     * <p>
     * 这将指定的秒数添加到此时间，返回一个新时间。计算会绕过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param secondstoAdd  要添加的秒数，可以为负
     * @return 基于此时间并已添加秒数的 {@code LocalTime}，不为空
     */
    public LocalTime plusSeconds(long secondstoAdd) {
        if (secondstoAdd == 0) {
            return this;
        }
        int sofd = hour * SECONDS_PER_HOUR +
                    minute * SECONDS_PER_MINUTE + second;
        int newSofd = ((int) (secondstoAdd % SECONDS_PER_DAY) + sofd + SECONDS_PER_DAY) % SECONDS_PER_DAY;
        if (sofd == newSofd) {
            return this;
        }
        int newHour = newSofd / SECONDS_PER_HOUR;
        int newMinute = (newSofd / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
        int newSecond = newSofd % SECONDS_PER_MINUTE;
        return create(newHour, newMinute, newSecond, nano);
    }

    /**
     * 返回一个副本，其中指定了添加的纳秒数。
     * <p>
     * 这将指定的纳秒数添加到此时间，返回一个新时间。计算会绕过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanosToAdd  要添加的纳秒数，可以为负
     * @return 基于此时间并已添加纳秒数的 {@code LocalTime}，不为空
     */
    public LocalTime plusNanos(long nanosToAdd) {
        if (nanosToAdd == 0) {
            return this;
        }
        long nofd = toNanoOfDay();
        long newNofd = ((nanosToAdd % NANOS_PER_DAY) + nofd + NANOS_PER_DAY) % NANOS_PER_DAY;
        if (nofd == newNofd) {
            return this;
        }
        int newHour = (int) (newNofd / NANOS_PER_HOUR);
        int newMinute = (int) ((newNofd / NANOS_PER_MINUTE) % MINUTES_PER_HOUR);
        int newSecond = (int) ((newNofd / NANOS_PER_SECOND) % SECONDS_PER_MINUTE);
        int newNano = (int) (newNofd % NANOS_PER_SECOND);
        return create(newHour, newMinute, newSecond, newNano);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了减去的量。
     * <p>
     * 这返回一个基于此时间的 {@code LocalTime}，指定了减去的量。
     * 该量通常是 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给量对象，通过调用 {@link TemporalAmount#subtractFrom(Temporal)}。量实现可以自由地以任何方式实现减法，但通常会回调 {@link #minus(long, TemporalUnit)}。请参阅量实现的文档，以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要减去的量，不为空
     * @return 基于此时间并已进行减法的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime minus(TemporalAmount amountToSubtract) {
        return (LocalTime) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，其中指定了减去的量。
     * <p>
     * 这返回一个基于此时间的 {@code LocalTime}，指定了以单位形式减去的量。如果无法减去该量，因为单位不受支持或其他原因，将抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但量为负。请参阅该方法的完整描述，了解添加（因此减法）的工作方式。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要从结果中减去的单位量，可以为负
     * @param unit  要减去的单位量，不为空
     * @return 基于此时间并已减去指定量的 {@code LocalTime}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code LocalTime} 的副本，减去指定的小时数。
     * <p>
     * 从这个时间减去指定的小时数，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hoursToSubtract  要减去的小时数，可以是负数
     * @return 基于此时间并减去小时数的 {@code LocalTime}，不为空
     */
    public LocalTime minusHours(long hoursToSubtract) {
        return plusHours(-(hoursToSubtract % HOURS_PER_DAY));
    }

    /**
     * 返回一个基于此 {@code LocalTime} 的副本，减去指定的分钟数。
     * <p>
     * 从这个时间减去指定的分钟数，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minutesToSubtract  要减去的分钟数，可以是负数
     * @return 基于此时间并减去分钟数的 {@code LocalTime}，不为空
     */
    public LocalTime minusMinutes(long minutesToSubtract) {
        return plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY));
    }

    /**
     * 返回一个基于此 {@code LocalTime} 的副本，减去指定的秒数。
     * <p>
     * 从这个时间减去指定的秒数，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param secondsToSubtract  要减去的秒数，可以是负数
     * @return 基于此时间并减去秒数的 {@code LocalTime}，不为空
     */
    public LocalTime minusSeconds(long secondsToSubtract) {
        return plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY));
    }

    /**
     * 返回一个基于此 {@code LocalTime} 的副本，减去指定的纳秒数。
     * <p>
     * 从这个时间减去指定的纳秒数，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanosToSubtract  要减去的纳秒数，可以是负数
     * @return 基于此时间并减去纳秒数的 {@code LocalTime}，不为空
     */
    public LocalTime minusNanos(long nanosToSubtract) {
        return plusNanos(-(nanosToSubtract % NANOS_PER_DAY));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询来查询此时间。
     * <p>
     * 使用指定的查询策略对象来查询此时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解
     * 此方法的结果将是什么。
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
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() ||
                query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        } else if (query == TemporalQueries.localTime()) {
            return (R) this;
        } else if (query == TemporalQueries.localDate()) {
            return null;
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // 作为优化，内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象，使其具有与该对象相同的时间。
     * <p>
     * 返回一个与输入具有相同可观察类型的临时对象，但时间已更改为与该对象相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)}
     * 传递 {@link ChronoField#NANO_OF_DAY} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式会更清晰，建议使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisLocalTime.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalTime);
     * </pre>
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(NANO_OF_DAY, toNanoOfDay());
    }

    /**
     * 计算到另一个时间的时间量，以指定的单位表示。
     * <p>
     * 以单个 {@code TemporalUnit} 表示两个 {@code LocalTime} 之间的时间量。
     * 起点和终点分别是 {@code this} 和指定的时间。
     * 如果终点在起点之前，结果将为负。
     * 传递给此方法的 {@code Temporal} 使用 {@link #from(TemporalAccessor)} 转换为
     * {@code LocalTime}。例如，可以计算两个时间之间的小时数
     * 使用 {@code startTime.until(endTime, HOURS)}。
     * <p>
     * 计算返回一个整数，表示两个时间之间完整的单位数。
     * 例如，11:30 和 13:29 之间的小时数将仅为一小时，因为还差一分钟才到两小时。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MINUTES);
     *   amount = MINUTES.between(start, end);
     * </pre>
     * 应根据哪种方法使代码更易读来选择。
     * <p>
     * 该计算在 {@link ChronoUnit} 的实现中完成。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS} 和 {@code HALF_DAYS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并传递 {@code this} 作为第一个参数和转换后的输入临时对象作为第二个参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param endExclusive  结束时间，不为空，转换为 {@code LocalTime}
     * @param unit  要测量的时间量的单位，不为空
     * @return 从这个时间到结束时间的时间量
     * @throws DateTimeException 如果无法计算时间量，或结束时间无法转换为 {@code LocalTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalTime end = LocalTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            long nanosUntil = end.toNanoOfDay() - toNanoOfDay();  // 不会溢出
            switch ((ChronoUnit) unit) {
                case NANOS: return nanosUntil;
                case MICROS: return nanosUntil / 1000;
                case MILLIS: return nanosUntil / 1000_000;
                case SECONDS: return nanosUntil / NANOS_PER_SECOND;
                case MINUTES: return nanosUntil / NANOS_PER_MINUTE;
                case HOURS: return nanosUntil / NANOS_PER_HOUR;
                case HALF_DAYS: return nanosUntil / (12 * NANOS_PER_HOUR);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式器格式化此时间。
     * <p>
     * 此时间将传递给格式器以生成字符串。
     *
     * @param formatter  要使用的格式器，不为空
     * @return 格式化的时间字符串，不为空
     * @throws DateTimeException 如果打印时发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间与日期组合以创建一个 {@code LocalDateTime}。
     * <p>
     * 返回一个由此时间在指定日期组成的 {@code LocalDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param date  要组合的日期，不为空
     * @return 由此时间和指定日期组成的本地日期时间，不为空
     */
    public LocalDateTime atDate(LocalDate date) {
        return LocalDateTime.of(date, this);
    }

    /**
     * 将此时间与偏移量组合以创建一个 {@code OffsetTime}。
     * <p>
     * 返回一个由此时间在指定偏移量组成的 {@code OffsetTime}。
     * 所有可能的时间和偏移量组合都是有效的。
     *
     * @param offset  要组合的偏移量，不为空
     * @return 由此时间和指定偏移量组成的偏移时间，不为空
     */
    public OffsetTime atOffset(ZoneOffset offset) {
        return OffsetTime.of(this, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 提取时间作为一天中的秒数，
     * 从 {@code 0} 到 {@code 24 * 60 * 60 - 1}。
     *
     * @return 与此时间等效的一天中的秒数
     */
    public int toSecondOfDay() {
        int total = hour * SECONDS_PER_HOUR;
        total += minute * SECONDS_PER_MINUTE;
        total += second;
        return total;
    }

    /**
     * 提取时间作为一天中的纳秒数，
     * 从 {@code 0} 到 {@code 24 * 60 * 60 * 1,000,000,000 - 1}。
     *
     * @return 与此时间等效的一天中的纳秒数
     */
    public long toNanoOfDay() {
        long total = hour * NANOS_PER_HOUR;
        total += minute * NANOS_PER_MINUTE;
        total += second * NANOS_PER_SECOND;
        total += nano;
        return total;
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间与另一个时间进行比较。
     * <p>
     * 比较基于一天内本地时间的时间线位置。
     * 它与 {@link Comparable} 定义的“与等于一致”。
     *
     * @param other  要比较的其他时间，不为空
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    @Override
    public int compareTo(LocalTime other) {
        int cmp = Integer.compare(hour, other.hour);
        if (cmp == 0) {
            cmp = Integer.compare(minute, other.minute);
            if (cmp == 0) {
                cmp = Integer.compare(second, other.second);
                if (cmp == 0) {
                    cmp = Integer.compare(nano, other.nano);
                }
            }
        }
        return cmp;
    }

    /**
     * 检查此时间是否在指定时间之后。
     * <p>
     * 比较基于一天内时间的时间线位置。
     *
     * @param other  要比较的其他时间，不为空
     * @return 如果此时间在指定时间之后，返回 true
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    public boolean isAfter(LocalTime other) {
        return compareTo(other) > 0;
    }

    /**
     * 检查此时间是否在指定时间之前。
     * <p>
     * 比较基于一天内时间的时间线位置。
     *
     * @param other  要比较的其他时间，不为空
     * @return 如果此时间在指定时间之前，返回 true
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    public boolean isBefore(LocalTime other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时间是否等于另一个时间。
     * <p>
     * 比较基于一天内时间的时间线位置。
     * <p>
     * 只比较 {@code LocalTime} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，使用
     * {@link ChronoField#NANO_OF_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此时间等于其他时间，返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalTime) {
            LocalTime other = (LocalTime) obj;
            return hour == other.hour && minute == other.minute &&
                    second == other.second && nano == other.nano;
        }
        return false;
    }

    /**
     * 此时间的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        long nod = toNanoOfDay();
        return (int) (nod ^ (nod >>> 32));
    }

    //-----------------------------------------------------------------------
    /**
     * 以 {@code String} 形式输出此时间，例如 {@code 10:15}。
     * <p>
     * 输出将是以下 ISO-8601 格式之一：
     * <ul>
     * <li>{@code HH:mm}</li>
     * <li>{@code HH:mm:ss}</li>
     * <li>{@code HH:mm:ss.SSS}</li>
     * <li>{@code HH:mm:ss.SSSSSS}</li>
     * <li>{@code HH:mm:ss.SSSSSSSSS}</li>
     * </ul>
     * 使用的格式将是输出完整时间值的最短格式，省略的部分隐式为零。
     *
     * @return 此时间的字符串表示，不为空
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(18);
        int hourValue = hour;
        int minuteValue = minute;
        int secondValue = second;
        int nanoValue = nano;
        buf.append(hourValue < 10 ? "0" : "").append(hourValue)
            .append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
            if (nanoValue > 0) {
                buf.append('.');
                if (nanoValue % 1000_000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000_000) + 1000).substring(1));
                } else if (nanoValue % 1000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000) + 1000_000).substring(1));
                } else {
                    buf.append(Integer.toString((nanoValue) + 1000_000_000).substring(1));
                }
            }
        }
        return buf.toString();
    }


                //-----------------------------------------------------------------------
    /**
     * 使用<a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a>写入对象。
     * @serialData
     * 一个二进制补码值表示剩余值不在流中，应设置为零。
     * <pre>
     *  out.writeByte(4);  // 标识一个 LocalTime
     *  if (nano == 0) {
     *    if (second == 0) {
     *      if (minute == 0) {
     *        out.writeByte(~hour);
     *      } else {
     *        out.writeByte(hour);
     *        out.writeByte(~minute);
     *      }
     *    } else {
     *      out.writeByte(hour);
     *      out.writeByte(minute);
     *      out.writeByte(~second);
     *    }
     *  } else {
     *    out.writeByte(hour);
     *    out.writeByte(minute);
     *    out.writeByte(second);
     *    out.writeInt(nano);
     *  }
     * </pre>
     *
     * @return {@code Ser} 的实例，不为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.LOCAL_TIME_TYPE, this);
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
        if (nano == 0) {
            if (second == 0) {
                if (minute == 0) {
                    out.writeByte(~hour);
                } else {
                    out.writeByte(hour);
                    out.writeByte(~minute);
                }
            } else {
                out.writeByte(hour);
                out.writeByte(minute);
                out.writeByte(~second);
            }
        } else {
            out.writeByte(hour);
            out.writeByte(minute);
            out.writeByte(second);
            out.writeInt(nano);
        }
    }

    static LocalTime readExternal(DataInput in) throws IOException {
        int hour = in.readByte();
        int minute = 0;
        int second = 0;
        int nano = 0;
        if (hour < 0) {
            hour = ~hour;
        } else {
            minute = in.readByte();
            if (minute < 0) {
                minute = ~minute;
            } else {
                second = in.readByte();
                if (second < 0) {
                    second = ~second;
                } else {
                    nano = in.readInt();
                }
            }
        }
        return LocalTime.of(hour, minute, second, nano);
    }

}
