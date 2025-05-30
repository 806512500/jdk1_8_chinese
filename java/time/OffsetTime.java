
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

import static java.time.LocalTime.NANOS_PER_HOUR;
import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoUnit.NANOS;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
import java.time.zone.ZoneRules;
import java.util.Objects;

/**
 * 带有 UTC/Greenwich 偏移量的 ISO-8601 日历系统中的时间，例如 {@code 10:15:30+01:00}。
 * <p>
 * {@code OffsetTime} 是一个不可变的日期时间对象，表示时间，通常
 * 视为小时-分钟-秒-偏移量。
 * 此类存储所有时间字段，精度达到纳秒，
 * 以及一个时区偏移量。
 * 例如，值 "13:45.30.123456789+02:00" 可以存储
 * 在一个 {@code OffsetTime} 中。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；使用身份敏感操作（包括引用相等性
 * ({@code ==})，身份哈希码，或同步）对 {@code OffsetTime} 实例的操作
 * 可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 此类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class OffsetTime
        implements Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {

    /**
     * 支持的最小 {@code OffsetTime}，'00:00:00+18:00'。
     * 这是在最大偏移量（更大的偏移量在时间线上更早）的日期开始时的午夜时间。
     * 这结合了 {@link LocalTime#MIN} 和 {@link ZoneOffset#MAX}。
     * 应用程序可以使用此值作为“远过去”的日期。
     */
    public static final OffsetTime MIN = LocalTime.MIN.atOffset(ZoneOffset.MAX);
    /**
     * 支持的最大 {@code OffsetTime}，'23:59:59.999999999-18:00'。
     * 这是在最小偏移量（更大的负偏移量在时间线上更晚）的日期结束时的午夜前的时间。
     * 这结合了 {@link LocalTime#MAX} 和 {@link ZoneOffset#MIN}。
     * 应用程序可以使用此值作为“远未来”的日期。
     */
    public static final OffsetTime MAX = LocalTime.MAX.atOffset(ZoneOffset.MIN);

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 7264499704384272492L;

    /**
     * 本地日期时间。
     */
    private final LocalTime time;
    /**
     * 从 UTC/Greenwich 的偏移量。
     */
    private final ZoneOffset offset;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前时间。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前时间。
     * 时钟中的时区将用于计算偏移量。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试
     * 因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前时间，不为空
     */
    public static OffsetTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前时间。
     * 指定时区可以避免依赖默认时区。
     * 指定时区将用于计算偏移量。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试
     * 因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前时间，不为空
     */
    public static OffsetTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前时间。
     * <p>
     * 这将查询指定的时钟以获取当前时间。
     * 时钟中的时区将用于计算偏移量。
     * <p>
     * 使用此方法可以使用替代时钟进行测试。
     * 可以使用 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前时间，不为空
     */
    public static OffsetTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // 调用一次
        return ofInstant(now, clock.getZone().getRules().getOffset(now));
    }

    //-----------------------------------------------------------------------
    /**
     * 从本地时间和偏移量获取 {@code OffsetTime} 的实例。
     *
     * @param time  本地时间，不为空
     * @param offset  时区偏移量，不为空
     * @return 偏移时间，不为空
     */
    public static OffsetTime of(LocalTime time, ZoneOffset offset) {
        return new OffsetTime(time, offset);
    }

    /**
     * 从小时、分钟、秒和纳秒获取 {@code OffsetTime} 的实例。
     * <p>
     * 这将创建一个具有四个指定字段的偏移时间。
     * <p>
     * 此方法主要用于编写测试用例。
     * 非测试代码通常会使用其他方法来创建偏移时间。
     * {@code LocalTime} 有两个额外的便利变体的
     * 等效工厂方法，接受较少的参数。
     * 为了减少 API 的足迹，这里没有提供这些方法。
     *
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @param offset  时区偏移量，不为空
     * @return 偏移时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        return new OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从 {@code Instant} 和时区 ID 获取 {@code OffsetTime} 的实例。
     * <p>
     * 这将创建一个与指定的即时相同的偏移时间。
     * 从 UTC/Greenwich 找到偏移量很简单，因为每个即时只有一个有效的
     * 偏移量。
     * <p>
     * 在转换过程中，即时的日期部分将被丢弃。
     * 这意味着转换永远不会因为即时超出有效日期范围而失败。
     *
     * @param instant  要从中创建时间的即时，不为空
     * @param zone  时区，可以是偏移量，不为空
     * @return 偏移时间，不为空
     */
    public static OffsetTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        long localSecond = instant.getEpochSecond() + offset.getTotalSeconds();  // 溢出将在稍后捕获
        int secsOfDay = (int) Math.floorMod(localSecond, SECONDS_PER_DAY);
        LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + instant.getNano());
        return new OffsetTime(time, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code OffsetTime} 的实例。
     * <p>
     * 这将根据指定的时间对象获取偏移时间。
     * {@code TemporalAccessor} 表示任意一组日期和时间信息，
     * 该工厂将其转换为 {@code OffsetTime} 的实例。
     * <p>
     * 转换将从时间对象中提取并组合 {@code ZoneOffset} 和
     * {@code LocalTime}。
     * 实现允许执行优化，例如访问
     * 与相关对象等效的字段。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名
     * 允许它通过方法引用作为查询使用，{@code OffsetTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 偏移时间，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code OffsetTime}
     */
    public static OffsetTime from(TemporalAccessor temporal) {
        if (temporal instanceof OffsetTime) {
            return (OffsetTime) temporal;
        }
        try {
            LocalTime time = LocalTime.from(temporal);
            ZoneOffset offset = ZoneOffset.from(temporal);
            return new OffsetTime(time, offset);
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 OffsetTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 10:15:30+01:00}）获取 {@code OffsetTime} 的实例。
     * <p>
     * 字符串必须表示一个有效的时间，并使用
     * {@link java.time.format.DateTimeFormatter#ISO_OFFSET_TIME} 进行解析。
     *
     * @param text  要解析的文本，如 "10:15:30+01:00"，不为空
     * @return 解析的本地时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * 使用特定格式器从文本字符串获取 {@code OffsetTime} 的实例。
     * <p>
     * 文本使用格式器进行解析，返回时间。
     *
     * @param text  要解析的文本，不为空
     * @param formatter  要使用的格式器，不为空
     * @return 解析的偏移时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, OffsetTime::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param time  本地时间，不为空
     * @param offset  时区偏移量，不为空
     */
    private OffsetTime(LocalTime time, ZoneOffset offset) {
        this.time = Objects.requireNonNull(time, "time");
        this.offset = Objects.requireNonNull(offset, "offset");
    }

    /**
     * 基于此时间返回一个新的时间，尽可能返回 {@code this}。
     *
     * @param time  要创建的时间，不为空
     * @param offset  要创建的时区偏移量，不为空
     */
    private OffsetTime with(LocalTime time, ZoneOffset offset) {
        if (this.time == time && this.offset.equals(offset)) {
            return this;
        }
        return new OffsetTime(time, offset);
    }


                //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 此方法检查此时间是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
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
     * <li>{@code OFFSET_SECONDS}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此时间上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased() || field == OFFSET_SECONDS;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否受支持。
     * <p>
     * 此方法检查指定的单位是否可以添加到或从这个带偏移的时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此处实现。
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
     * 是通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递获得的。
     * 单位是否受支持由单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override  // override for Javadoc
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
     * 此时间用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因而无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不允许为 null
     * @return 字段的有效值范围，不允许为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                return field.range();
            }
            return time.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 此方法查询此时间指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因而无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此时间的有效值，
     * 但 {@code NANO_OF_DAY} 和 {@code MICRO_OF_DAY} 由于值太大无法放入 {@code int} 中，
     * 会抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不允许为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // override for Javadoc
    public int get(TemporalField field) {
        return Temporal.super.get(field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 此方法查询此时间指定字段的值。
     * 如果由于字段不受支持或其他原因而无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此时间的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不允许为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                return offset.getTotalSeconds();
            }
            return time.getLong(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时区偏移，例如 '+01:00'。
     * <p>
     * 这是本地时间与 UTC/Greenwich 的偏移。
     *
     * @return 时区偏移，不允许为 null
     */
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，指定偏移确保结果具有相同的时间。
     * <p>
     * 此方法返回一个具有相同 {@code LocalTime} 和指定 {@code ZoneOffset} 的对象。
     * 不需要进行任何计算。
     * 例如，如果此时间表示 {@code 10:30+02:00}，而指定的偏移是
     * {@code +03:00}，则此方法将返回 {@code 10:30+03:00}。
     * <p>
     * 要考虑偏移之间的差异并调整时间字段，使用 {@link #withOffsetSameInstant}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param offset  要更改的时区偏移，不允许为 null
     * @return 基于此时间并具有请求偏移的 {@code OffsetTime}，不允许为 null
     */
    public OffsetTime withOffsetSameLocal(ZoneOffset offset) {
        return offset != null && offset.equals(this.offset) ? this : new OffsetTime(time, offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，指定偏移确保结果在同一隐含日的同一时刻。
     * <p>
     * 此方法返回一个具有指定 {@code ZoneOffset} 和调整后的 {@code LocalTime} 的对象。
     * 这将导致旧对象和新对象在隐含日的同一时刻表示相同的时间。
     * 这对于查找不同偏移下的本地时间很有用。
     * 例如，如果此时间表示 {@code 10:30+02:00}，而指定的偏移是
     * {@code +03:00}，则此方法将返回 {@code 11:30+03:00}。
     * <p>
     * 要更改偏移而不调整本地时间，使用 {@link #withOffsetSameLocal}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param offset  要更改的时区偏移，不允许为 null
     * @return 基于此时间并具有请求偏移的 {@code OffsetTime}，不允许为 null
     */
    public OffsetTime withOffsetSameInstant(ZoneOffset offset) {
        if (offset.equals(this.offset)) {
            return this;
        }
        int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
        LocalTime adjusted = time.plusSeconds(difference);
        return new OffsetTime(adjusted, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalTime} 部分。
     * <p>
     * 此方法返回一个具有与本日期时间相同小时、分钟、秒和纳秒的 {@code LocalTime}。
     *
     * @return 此日期时间的时间部分，不允许为 null
     */
    public LocalTime toLocalTime() {
        return time;
    }

    //-----------------------------------------------------------------------
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
     * 返回一个调整后的时间副本。
     * <p>
     * 此方法返回一个基于此时间的 {@code OffsetTime}，时间已调整。
     * 调整使用指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 简单的调整器可能只是设置某个字段，例如小时字段。
     * 更复杂的调整器可能将时间设置为一天的最后一小时。
     * <p>
     * 类 {@link LocalTime} 和 {@link ZoneOffset} 实现了 {@code TemporalAdjuster}，
     * 因此此方法可以用于更改时间或偏移：
     * <pre>
     *  result = offsetTime.with(time);
     *  result = offsetTime.with(offset);
     * </pre>
     * <p>
     * 此方法的结果是通过调用
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数获得的。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不允许为 null
     * @return 基于 {@code this} 并进行了调整的 {@code OffsetTime}，不允许为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalTime) {
            return with((LocalTime) adjuster, offset);
        } else if (adjuster instanceof ZoneOffset) {
            return with(time, (ZoneOffset) adjuster);
        } else if (adjuster instanceof OffsetTime) {
            return (OffsetTime) adjuster;
        }
        return (OffsetTime) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此时间的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此时间的 {@code OffsetTime}，指定字段的值已更改。
     * 可以使用此方法更改任何支持的字段，例如小时、分钟或秒。
     * 如果由于字段不受支持或其他原因而无法设置值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * <p>
     * {@code OFFSET_SECONDS} 字段将返回具有指定偏移的时间。
     * 本地时间保持不变。如果新的偏移值超出有效范围，
     * 则抛出 {@code DateTimeException}。
     * <p>
     * 其他 {@link #isSupported(TemporalField) 支持的字段} 将按照
     * {@link LocalTime#with(TemporalField, long)} LocalTime} 的相应方法进行处理。
     * 在这种情况下，偏移不参与计算，将保持不变。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.adjustInto(Temporal, long)}
     * 并将 {@code this} 作为参数传递获得的。在这种情况下，字段决定
     * 是否以及如何调整时间。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不允许为 null
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code OffsetTime}，不允许为 null
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                ChronoField f = (ChronoField) field;
                return with(time, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
            }
            return with(time.with(field, newValue), offset);
        }
        return field.adjustInto(this, newValue);
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，其中小时被更改。
     * <p>
     * 偏移不会影响计算，并且结果中将保持不变。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param hour  结果中要设置的小时，从 0 到 23
     * @return 基于此时间并具有请求小时的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果小时值无效
     */
    public OffsetTime withHour(int hour) {
        return with(time.withHour(hour), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，其中分钟被更改。
     * <p>
     * 偏移不会影响计算，并且结果中将保持不变。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param minute  结果中要设置的分钟，从 0 到 59
     * @return 基于此时间并具有请求分钟的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果分钟值无效
     */
    public OffsetTime withMinute(int minute) {
        return with(time.withMinute(minute), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，其中秒被更改。
     * <p>
     * 偏移不会影响计算，并且结果中将保持不变。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param second  结果中要设置的秒，从 0 到 59
     * @return 基于此时间并具有请求秒的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果秒值无效
     */
    public OffsetTime withSecond(int second) {
        return with(time.withSecond(second), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，其中纳秒被更改。
     * <p>
     * 偏移不会影响计算，并且结果中将保持不变。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param nanoOfSecond  结果中要设置的纳秒，从 0 到 999,999,999
     * @return 基于此时间并具有请求纳秒的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果纳秒值无效
     */
    public OffsetTime withNano(int nanoOfSecond) {
        return with(time.withNano(nanoOfSecond), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，其中时间被截断。
     * <p>
     * 截断返回一个原始时间的副本，其中指定单位以下的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒和纳秒字段设置为零。
     * <p>
     * 单位必须具有一个可以整除标准天长度的 {@linkplain TemporalUnit#getDuration() 时长}。
     * 这包括 {@link ChronoUnit} 中提供的所有时间单位和 {@link ChronoUnit#DAYS 天}。其他单位将抛出异常。
     * <p>
     * 偏移不会影响计算，并且结果中将保持不变。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此时间并已截断时间的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public OffsetTime truncatedTo(TemporalUnit unit) {
        return with(time.truncatedTo(unit), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此时间并添加了指定数量的副本。
     * <p>
     * 这返回一个基于此时间并添加了指定数量的 {@code OffsetTime}。
     * 数量通常是 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是通过调用 {@link TemporalAmount#addTo(Temporal)} 委托给数量对象的。
     * 数量实现可以自由地以任何方式实现添加，但通常会回调 {@link #plus(long, TemporalUnit)}。
     * 请参阅数量实现的文档以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param amountToAdd  要添加的数量，不为空
     * @return 基于此时间并已添加数量的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime plus(TemporalAmount amountToAdd) {
        return (OffsetTime) amountToAdd.addTo(this);
    }

    /**
     * 返回一个基于此时间并添加了指定数量的副本。
     * <p>
     * 这返回一个基于此时间并添加了以单位表示的数量的 {@code OffsetTime}。
     * 如果由于单位不受支持或其他原因无法添加数量，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则添加由 {@link LocalTime#plus(long, TemporalUnit)} 实现。
     * 偏移不会参与计算，并且结果中将保持不变。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 在这种情况下，单位决定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param amountToAdd  要添加到结果中的单位数量，可以为负数
     * @param unit  要添加的数量的单位，不为空
     * @return 基于此时间并已添加指定数量的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果无法添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with(time.plus(amountToAdd, unit), offset);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetTime} 并添加了指定小时数的副本。
     * <p>
     * 这将指定的小时数添加到此时间，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param hours  要添加的小时数，可以为负数
     * @return 基于此时间并已添加小时数的 {@code OffsetTime}，不为空
     */
    public OffsetTime plusHours(long hours) {
        return with(time.plusHours(hours), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并添加了指定分钟数的副本。
     * <p>
     * 这将指定的分钟数添加到此时间，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param minutes  要添加的分钟数，可以为负数
     * @return 基于此时间并已添加分钟数的 {@code OffsetTime}，不为空
     */
    public OffsetTime plusMinutes(long minutes) {
        return with(time.plusMinutes(minutes), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并添加了指定秒数的副本。
     * <p>
     * 这将指定的秒数添加到此时间，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param seconds  要添加的秒数，可以为负数
     * @return 基于此时间并已添加秒数的 {@code OffsetTime}，不为空
     */
    public OffsetTime plusSeconds(long seconds) {
        return with(time.plusSeconds(seconds), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并添加了指定纳秒数的副本。
     * <p>
     * 这将指定的纳秒数添加到此时间，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param nanos  要添加的纳秒数，可以为负数
     * @return 基于此时间并已添加纳秒数的 {@code OffsetTime}，不为空
     */
    public OffsetTime plusNanos(long nanos) {
        return with(time.plusNanos(nanos), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此时间并减去了指定数量的副本。
     * <p>
     * 这返回一个基于此时间并减去了指定数量的 {@code OffsetTime}。
     * 数量通常是 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是通过调用 {@link TemporalAmount#subtractFrom(Temporal)} 委托给数量对象的。
     * 数量实现可以自由地以任何方式实现减法，但通常会回调 {@link #minus(long, TemporalUnit)}。
     * 请参阅数量实现的文档以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于此时间并已减去数量的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime minus(TemporalAmount amountToSubtract) {
        return (OffsetTime) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个基于此时间并减去了指定数量的副本。
     * <p>
     * 这返回一个基于此时间并减去了以单位表示的数量的 {@code OffsetTime}。
     * 如果由于单位不受支持或其他原因无法减去数量，则会抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。
     * 请参阅该方法以了解添加（因此减法）的工作原理。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param amountToSubtract  要从结果中减去的单位数量，可以为负数
     * @param unit  要减去的数量的单位，不为空
     * @return 基于此时间并已减去指定数量的 {@code OffsetTime}，不为空
     * @throws DateTimeException 如果无法减去
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code OffsetTime} 并减去了指定小时数的副本。
     * <p>
     * 这将指定的小时数从此时间中减去，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param hours  要减去的小时数，可以为负数
     * @return 基于此时间并已减去小时数的 {@code OffsetTime}，不为空
     */
    public OffsetTime minusHours(long hours) {
        return with(time.minusHours(hours), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并减去了指定分钟数的副本。
     * <p>
     * 这将指定的分钟数从此时间中减去，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param minutes  要减去的分钟数，可以为负数
     * @return 基于此时间并已减去分钟数的 {@code OffsetTime}，不为空
     */
    public OffsetTime minusMinutes(long minutes) {
        return with(time.minusMinutes(minutes), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并减去了指定秒数的副本。
     * <p>
     * 这将指定的秒数从此时间中减去，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param seconds  要减去的秒数，可以为负数
     * @return 基于此时间并已减去秒数的 {@code OffsetTime}，不为空
     */
    public OffsetTime minusSeconds(long seconds) {
        return with(time.minusSeconds(seconds), offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 并减去了指定纳秒数的副本。
     * <p>
     * 这将指定的纳秒数从此时间中减去，返回一个新时间。
     * 计算会跨过午夜。
     * <p>
     * 本实例是不可变的，此方法调用不会对其产生影响。
     *
     * @param nanos  要减去的纳秒数，可以为负数
     * @return 基于此时间并已减去纳秒数的 {@code OffsetTime}，不为空
     */
    public OffsetTime minusNanos(long nanos) {
        return with(time.minusNanos(nanos), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定查询查询此时间。
     * <p>
     * 这使用指定的查询策略对象查询此时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。
     * 请阅读查询的文档以了解此方法的结果是什么。
     * <p>
     * 此方法的结果是通过调用指定查询的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return (R) offset;
        } else if (query == TemporalQueries.zoneId() | query == TemporalQueries.chronology() || query == TemporalQueries.localDate()) {
            return null;
        } else if (query == TemporalQueries.localTime()) {
            return (R) time;
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this);
    }


                /**
     * 将指定的时间对象调整为与此对象具有相同的偏移量和时间。
     * <p>
     * 此方法返回一个与输入对象具有相同可观察类型的临时对象，但偏移量和时间已更改为与此对象相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 两次，分别传递 {@link ChronoField#NANO_OF_DAY} 和
     * {@link ChronoField#OFFSET_SECONDS} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式会更清晰，建议使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisOffsetTime.adjustInto(temporal);
     *   temporal = temporal.with(thisOffsetTime);
     * </pre>
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal
                .with(NANO_OF_DAY, time.toNanoOfDay())
                .with(OFFSET_SECONDS, offset.getTotalSeconds());
    }

    /**
     * 计算到另一个时间的时间量，以指定的单位表示。
     * <p>
     * 此方法计算两个 {@code OffsetTime} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的时间。
     * 如果终点在起点之前，结果将为负。
     * 例如，可以使用 {@code startTime.until(endTime, HOURS)} 计算两个时间之间的小时数。
     * <p>
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为
     * {@code OffsetTime}。如果两个时间的偏移量不同，则指定的结束时间将被规范化为具有与此时间相同的偏移量。
     * <p>
     * 计算返回一个整数，表示两个时间之间的完整单位数。
     * 例如，11:30Z 和 13:29Z 之间的时间量（以小时为单位）将仅为一小时，因为还差一分钟才到两小时。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MINUTES);
     *   amount = MINUTES.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更易读。
     * <p>
     * 此方法的实现针对 {@link ChronoUnit} 进行了实现。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS} 和 {@code HALF_DAYS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并将 {@code this} 作为第一个参数和转换后的输入临时对象作为第二个参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param endExclusive  结束时间，不为空，将被转换为 {@code OffsetTime}
     * @param unit  衡量时间量的单位，不为空
     * @return 从本时间到结束时间的时间量
     * @throws DateTimeException 如果无法计算时间量，或结束时间无法转换为 {@code OffsetTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        OffsetTime end = OffsetTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            long nanosUntil = end.toEpochNano() - toEpochNano();  // 不会发生溢出
            switch ((ChronoUnit) unit) {
                case NANOS: return nanosUntil;
                case MICROS: return nanosUntil / 1000;
                case MILLIS: return nanosUntil / 1000_000;
                case SECONDS: return nanosUntil / NANOS_PER_SECOND;
                case MINUTES: return nanosUntil / NANOS_PER_MINUTE;
                case HOURS: return nanosUntil / NANOS_PER_HOUR;
                case HALF_DAYS: return nanosUntil / (12 * NANOS_PER_HOUR);
            }
            throw new UnsupportedTemporalTypeException("不支持的单位: " + unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化器格式化此时间。
     * <p>
     * 此时间将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的时间字符串，不为空
     * @throws DateTimeException 如果打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间与日期组合以创建 {@code OffsetDateTime}。
     * <p>
     * 此方法返回一个由此时间和指定日期组成的 {@code OffsetDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param date  要组合的日期，不为空
     * @return 由本时间和指定日期组成的偏移日期时间，不为空
     */
    public OffsetDateTime atDate(LocalDate date) {
        return OffsetDateTime.of(date, time, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间转换为基于 1970-01-01Z 的纪元纳秒。
     *
     * @return 纪元纳秒值
     */
    private long toEpochNano() {
        long nod = time.toNanoOfDay();
        long offsetNanos = offset.getTotalSeconds() * NANOS_PER_SECOND;
        return nod - offsetNanos;
    }

    //-----------------------------------------------------------------------
    /**
     * 将此 {@code OffsetTime} 与另一个时间进行比较。
     * <p>
     * 比较首先基于 UTC 等效时刻，然后基于本地时间。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义。
     * <p>
     * 例如，以下是比较器的顺序：
     * <ol>
     * <li>{@code 10:30+01:00}</li>
     * <li>{@code 11:00+01:00}</li>
     * <li>{@code 12:00+02:00}</li>
     * <li>{@code 11:30+01:00}</li>
     * <li>{@code 12:00+01:00}</li>
     * <li>{@code 12:30+01:00}</li>
     * </ol>
     * 值 #2 和 #3 表示时间线上的同一时刻。
     * 当两个值表示同一时刻时，将比较本地时间以区分它们。此步骤是为了使排序
     * 与 {@code equals()} 一致。
     * <p>
     * 要比较两个 {@code TemporalAccessor} 实例的底层本地时间，可以使用 {@link ChronoField#NANO_OF_DAY} 作为比较器。
     *
     * @param other  要比较的其他时间，不为空
     * @return 比较值，负数表示小于，正数表示大于
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    @Override
    public int compareTo(OffsetTime other) {
        if (offset.equals(other.offset)) {
            return time.compareTo(other.time);
        }
        int compare = Long.compare(toEpochNano(), other.toEpochNano());
        if (compare == 0) {
            compare = time.compareTo(other.time);
        }
        return compare;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此 {@code OffsetTime} 的时刻是否在指定时间的时刻之后，将两个时间应用到同一日期。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较时间的时刻。这相当于使用同一日期将两个
     * 时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不为空
     * @return 如果此时间在指定时间的时刻之后，则返回 true
     */
    public boolean isAfter(OffsetTime other) {
        return toEpochNano() > other.toEpochNano();
    }

    /**
     * 检查此 {@code OffsetTime} 的时刻是否在指定时间的时刻之前，将两个时间应用到同一日期。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较时间的时刻。这相当于使用同一日期将两个
     * 时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不为空
     * @return 如果此时间在指定时间的时刻之前，则返回 true
     */
    public boolean isBefore(OffsetTime other) {
        return toEpochNano() < other.toEpochNano();
    }

    /**
     * 检查此 {@code OffsetTime} 的时刻是否等于指定时间的时刻，将两个时间应用到同一日期。
     * <p>
     * 此方法与 {@link #compareTo} 和 {@link #equals} 中的比较不同，因为它仅比较时间的时刻。这相当于使用同一日期将两个
     * 时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不为空
     * @return 如果此时间等于指定时间的时刻，则返回 true
     */
    public boolean isEqual(OffsetTime other) {
        return toEpochNano() == other.toEpochNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时间是否等于另一个时间。
     * <p>
     * 比较基于本地时间和偏移量。
     * 要比较时间线上的同一时刻，使用 {@link #isEqual(OffsetTime)}。
     * <p>
     * 仅比较 {@code OffsetTime} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的底层本地时间，可以使用 {@link ChronoField#NANO_OF_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此时间等于其他时间，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OffsetTime) {
            OffsetTime other = (OffsetTime) obj;
            return time.equals(other.time) && offset.equals(other.offset);
        }
        return false;
    }

    /**
     * 为此时间生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return time.hashCode() ^ offset.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间输出为 {@code String}，例如 {@code 10:15:30+01:00}。
     * <p>
     * 输出将是以下 ISO-8601 格式之一：
     * <ul>
     * <li>{@code HH:mmXXXXX}</li>
     * <li>{@code HH:mm:ssXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSSSSXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSSSSSSSXXXXX}</li>
     * </ul>
     * 使用的格式将是输出完整时间值的最短格式，省略的部分隐式为零。
     *
     * @return 此时间的字符串表示，不为空
     */
    @Override
    public String toString() {
        return time.toString() + offset.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(9);  // 标识一个 OffsetTime
     *  // <a href="../../serialized-form.html#java.time.LocalTime">时间</a>，不包括一个字节的头
     *  // <a href="../../serialized-form.html#java.time.ZoneOffset">偏移量</a>，不包括一个字节的头
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.OFFSET_TIME_TYPE, this);
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
        time.writeExternal(out);
        offset.writeExternal(out);
    }

    static OffsetTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocalTime time = LocalTime.readExternal(in);
        ZoneOffset offset = ZoneOffset.readExternal(in);
        return OffsetTime.of(time, offset);
    }

}
