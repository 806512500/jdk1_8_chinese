
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

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoZonedDateTime;
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
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

/**
 * ISO-8601 日历系统中的带时区的日期时间，例如 {@code 2007-12-03T10:15:30+01:00 Europe/Paris}。
 * <p>
 * {@code ZonedDateTime} 是一个不可变的带时区的日期时间表示。
 * 该类存储所有日期和时间字段，精度达到纳秒，并且存储一个时区，使用时区偏移来处理模糊的本地日期时间。
 * 例如，“2007 年 10 月 2 日 13:45.30.123456789 +02:00 在欧洲/巴黎时区”可以存储在 {@code ZonedDateTime} 中。
 * <p>
 * 该类处理从 {@code LocalDateTime} 的本地时间线到 {@code Instant} 的即时时间线的转换。
 * 两条时间线之间的差异是 UTC/Greenwich 的偏移量，由 {@code ZoneOffset} 表示。
 * <p>
 * 在两条时间线之间进行转换涉及使用从 {@code ZoneId} 访问的 {@link ZoneRules 规则} 计算偏移量。
 * 获取即时的偏移量很简单，因为每个即时只有一个有效的偏移量。相比之下，获取本地日期时间的偏移量并不简单。有三种情况：
 * <ul>
 * <li>正常，有一个有效的偏移量。在一年中的大部分时间里，正常情况适用，其中本地日期时间有一个单一的有效偏移量。</li>
 * <li>间隔，没有有效的偏移量。这是当钟表向前跳转时，通常由于春季夏令时从“冬季”变为“夏季”。
 * 在间隔中，有些本地日期时间值没有有效的偏移量。</li>
 * <li>重叠，有两个有效的偏移量。这是当钟表向后设置时，通常由于秋季夏令时从“夏季”变为“冬季”。
 * 在重叠中，有些本地日期时间值有两个有效的偏移量。</li>
 * </ul>
 * <p>
 * 任何直接或间接从本地日期时间转换到即时的方法都可能很复杂。
 * <p>
 * 对于间隔，一般策略是如果本地日期时间落在间隔中间，则结果的带时区的日期时间的本地日期时间将向前移动间隔的长度，
 * 结果是一个在较晚偏移量中的日期时间，通常是“夏季”时间。
 * <p>
 * 对于重叠，一般策略是如果本地日期时间落在重叠中间，则保留前一个偏移量。如果没有前一个偏移量，或者前一个偏移量无效，
 * 则使用较早的偏移量，通常是“夏季”时间。两个附加方法，
 * {@link #withEarlierOffsetAtOverlap()} 和 {@link #withLaterOffsetAtOverlap()}，
 * 帮助管理重叠的情况。
 * <p>
 * 从设计角度来看，这个类应主要视为 {@code LocalDateTime} 和 {@code ZoneId} 的组合。
 * {@code ZoneOffset} 是一个重要的但次要的信息，用于确保该类表示一个即时，特别是在夏令时重叠期间。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code ZonedDateTime} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 一个 {@code ZonedDateTime} 持有相当于三个独立对象的状态，
 * 一个 {@code LocalDateTime}，一个 {@code ZoneId} 和解析后的 {@code ZoneOffset}。
 * 偏移量和本地日期时间用于在必要时定义一个即时。时区 ID 用于获取控制偏移量何时变化的规则。
 * 偏移量不能自由设置，因为时区控制哪些偏移量是有效的。
 * <p>
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class ZonedDateTime
        implements Temporal, ChronoZonedDateTime<LocalDate>, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -6260982410461394882L;

    /**
     * 本地日期时间。
     */
    private final LocalDateTime dateTime;
    /**
     * 从 UTC/Greenwich 的偏移量。
     */
    private final ZoneOffset offset;
    /**
     * 时区。
     */
    private final ZoneId zone;

    //-----------------------------------------------------------------------
    /**
     * 从系统时钟在默认时区中获取当前日期时间。
     * <p>
     * 这将查询默认时区中的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期时间。
     * 时区和偏移量将根据时钟中的时区设置。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟的当前日期时间，不为空
     */
    public static ZonedDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从系统时钟在指定时区中获取当前日期时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期时间。
     * 指定时区避免依赖默认时区。偏移量将根据指定的时区计算。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前日期时间，不为空
     */
    public static ZonedDateTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前日期时间。
     * <p>
     * 这将查询指定的时钟以获取当前日期时间。
     * 时区和偏移量将根据时钟中的时区设置。
     * <p>
     * 使用此方法允许使用替代时钟进行测试。
     * 可以使用 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期时间，不为空
     */
    public static ZonedDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // 调用一次
        return ofInstant(now, clock.getZone());
    }

    //-----------------------------------------------------------------------
    /**
     * 从本地日期和时间获取 {@code ZonedDateTime} 的实例。
     * <p>
     * 这将创建一个尽可能匹配输入本地日期和时间的带时区的日期时间。
     * 时区规则，如夏令时，意味着并非每个本地日期时间都对指定的时区有效，因此本地日期时间可能会被调整。
     * <p>
     * 本地日期时间和时间首先组合成一个本地日期时间。
     * 然后将本地日期时间解析为时间线上的单个即时。
     * 这是通过根据时区 ID 的 {@link ZoneRules 规则} 查找本地日期时间的有效偏移量来实现的。
     * <p>
     * 在大多数情况下，每个本地日期时间只有一个有效的偏移量。
     * 在重叠的情况下，当钟表向后设置时，有两个有效的偏移量。
     * 该方法使用较早的偏移量，通常对应于“夏季”。
     * <p>
     * 在间隔的情况下，当钟表向前跳转时，没有有效的偏移量。
     * 相反，本地日期时间将根据间隔的长度向后调整。
     * 对于典型的夏令时变化，本地日期时间将向后移动一个小时，进入通常对应于“夏季”的偏移量。
     *
     * @param date  本地日期，不为空
     * @param time  本地时间，不为空
     * @param zone  时区，不为空
     * @return 带偏移量的日期时间，不为空
     */
    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return of(LocalDateTime.of(date, time), zone);
    }

    /**
     * 从本地日期时间获取 {@code ZonedDateTime} 的实例。
     * <p>
     * 这将创建一个尽可能匹配输入本地日期时间的带时区的日期时间。
     * 时区规则，如夏令时，意味着并非每个本地日期时间都对指定的时区有效，因此本地日期时间可能会被调整。
     * <p>
     * 本地日期时间将解析为时间线上的单个即时。
     * 这是通过根据时区 ID 的 {@link ZoneRules 规则} 查找本地日期时间的有效偏移量来实现的。
     * <p>
     * 在大多数情况下，每个本地日期时间只有一个有效的偏移量。
     * 在重叠的情况下，当钟表向后设置时，有两个有效的偏移量。
     * 该方法使用较早的偏移量，通常对应于“夏季”。
     * <p>
     * 在间隔的情况下，当钟表向前跳转时，没有有效的偏移量。
     * 相反，本地日期时间将根据间隔的长度向后调整。
     * 对于典型的夏令时变化，本地日期时间将向后移动一个小时，进入通常对应于“夏季”的偏移量。
     *
     * @param localDateTime  本地日期时间，不为空
     * @param zone  时区，不为空
     * @return 带时区的日期时间，不为空
     */
    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ofLocal(localDateTime, zone, null);
    }

    /**
     * 从年、月、日、小时、分钟、秒、纳秒和时区获取 {@code ZonedDateTime} 的实例。
     * <p>
     * 这将创建一个尽可能匹配七个指定字段的本地日期时间的带时区的日期时间。
     * 时区规则，如夏令时，意味着并非每个本地日期时间都对指定的时区有效，因此本地日期时间可能会被调整。
     * <p>
     * 本地日期时间将解析为时间线上的单个即时。
     * 这是通过根据时区 ID 的 {@link ZoneRules 规则} 查找本地日期时间的有效偏移量来实现的。
     * <p>
     * 在大多数情况下，每个本地日期时间只有一个有效的偏移量。
     * 在重叠的情况下，当钟表向后设置时，有两个有效的偏移量。
     * 该方法使用较早的偏移量，通常对应于“夏季”。
     * <p>
     * 在间隔的情况下，当钟表向前跳转时，没有有效的偏移量。
     * 相反，本地日期时间将根据间隔的长度向后调整。
     * 对于典型的夏令时变化，本地日期时间将向后移动一个小时，进入通常对应于“夏季”的偏移量。
     * <p>
     * 该方法主要用于编写测试用例。
     * 非测试代码通常会使用其他方法来创建带偏移量的时间。
     * {@code LocalDateTime} 有五个额外的方便变体的等效工厂方法，参数较少。
     * 它们未在此处提供，以减少 API 的占用空间。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，从 1（1 月）到 12（12 月）
     * @param dayOfMonth  要表示的月份中的日期，从 1 到 31
     * @param hour  要表示的小时，从 0 到 23
     * @param minute  要表示的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @param zone  时区，不为空
     * @return 带偏移量的日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或月份中的日期无效
     */
    public static ZonedDateTime of(
            int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        return ofLocal(dt, zone, null);
    }


                /**
     * 从本地日期时间获取 {@code ZonedDateTime} 实例，尽可能使用首选偏移量。
     * <p>
     * 本地日期时间被解析为时间线上的单个时间点。
     * 这是通过找到本地日期时间的有效 UTC/Greenwich 偏移量来实现的，该偏移量由时区 ID 的 {@link ZoneRules 规则} 定义。
     * <p>
     * 在大多数情况下，对于本地日期时间只有一个有效的偏移量。
     * 在重叠的情况下，即时钟倒退时，有两个有效的偏移量。
     * 如果首选偏移量是有效的偏移量之一，则使用它。
     * 否则使用较早的有效偏移量，通常对应于“夏季”。
     * <p>
     * 在时钟跳过的情况下，即时钟向前跳，没有有效的偏移量。
     * 相反，本地日期时间会根据时差调整为更晚的时间。
     * 对于典型的1小时夏令时变化，本地日期时间将被调整为1小时后，通常对应于“夏季”偏移量。
     *
     * @param localDateTime  本地日期时间，不为空
     * @param zone  时区，不为空
     * @param preferredOffset  时区偏移量，如果没有偏好则为 null
     * @return 时区日期时间，不为空
     */
    public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(zone, "zone");
        if (zone instanceof ZoneOffset) {
            return new ZonedDateTime(localDateTime, (ZoneOffset) zone, zone);
        }
        ZoneRules rules = zone.getRules();
        List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);
        ZoneOffset offset;
        if (validOffsets.size() == 1) {
            offset = validOffsets.get(0);
        } else if (validOffsets.size() == 0) {
            ZoneOffsetTransition trans = rules.getTransition(localDateTime);
            localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
            offset = trans.getOffsetAfter();
        } else {
            if (preferredOffset != null && validOffsets.contains(preferredOffset)) {
                offset = preferredOffset;
            } else {
                offset = Objects.requireNonNull(validOffsets.get(0), "offset");  // 保护不受不良 ZoneRules 影响
            }
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 从 {@code Instant} 获取 {@code ZonedDateTime} 实例。
     * <p>
     * 这将创建一个与指定的瞬间具有相同瞬间的时区日期时间。
     * 调用 {@link #toInstant()} 将返回一个与这里使用的瞬间相等的瞬间。
     * <p>
     * 将瞬间转换为时区日期时间很简单，因为每个瞬间只有一个有效的偏移量。
     *
     * @param instant  用于创建日期时间的瞬间，不为空
     * @param zone  时区，不为空
     * @return 时区日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return create(instant.getEpochSecond(), instant.getNano(), zone);
    }

    /**
     * 从组合的本地日期时间和偏移量获取 {@code ZonedDateTime} 实例。
     * <p>
     * 通过 {@link LocalDateTime#toInstant(ZoneOffset) 组合} {@code LocalDateTime} 和 {@code ZoneOffset} 创建一个时区日期时间。
     * 这种组合唯一地指定了一个瞬间，没有歧义。
     * <p>
     * 将瞬间转换为时区日期时间很简单，因为每个瞬间只有一个有效的偏移量。如果有效的偏移量与指定的偏移量不同，
     * 则时区日期时间的日期时间和偏移量将与指定的不同。
     * <p>
     * 如果要使用的 {@code ZoneId} 是一个 {@code ZoneOffset}，此方法等同于 {@link #of(LocalDateTime, ZoneId)}。
     *
     * @param localDateTime  本地日期时间，不为空
     * @param offset  时区偏移量，不为空
     * @param zone  时区，不为空
     * @return 时区日期时间，不为空
     */
    public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        if (zone.getRules().isValidOffset(localDateTime, offset)) {
            return new ZonedDateTime(localDateTime, offset, zone);
        }
        return create(localDateTime.toEpochSecond(offset), localDateTime.getNano(), zone);
    }

    /**
     * 使用从 1970-01-01T00:00:00Z 开始的秒数获取 {@code ZonedDateTime} 实例。
     *
     * @param epochSecond  从 1970-01-01T00:00:00Z 开始的秒数
     * @param nanoOfSecond  秒内的纳秒数，从 0 到 999,999,999
     * @param zone  时区，不为空
     * @return 时区日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
        ZoneRules rules = zone.getRules();
        Instant instant = Instant.ofEpochSecond(epochSecond, nanoOfSecond);  // TODO: rules should be queryable by epochSeconds
        ZoneOffset offset = rules.getOffset(instant);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset);
        return new ZonedDateTime(ldt, offset, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 严格验证本地日期时间、偏移量和时区 ID 的组合，获取 {@code ZonedDateTime} 实例。
     * <p>
     * 这将创建一个时区日期时间，确保偏移量根据指定时区的规则对本地日期时间有效。
     * 如果偏移量无效，将抛出异常。
     *
     * @param localDateTime  本地日期时间，不为空
     * @param offset  时区偏移量，不为空
     * @param zone  时区，不为空
     * @return 时区日期时间，不为空
     */
    public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        if (rules.isValidOffset(localDateTime, offset) == false) {
            ZoneOffsetTransition trans = rules.getTransition(localDateTime);
            if (trans != null && trans.isGap()) {
                // 错误消息简单地说是夏令时
                // 尽管还有其他类型的时差
                throw new DateTimeException("LocalDateTime '" + localDateTime +
                        "' 在时区 '" + zone +
                        "' 中不存在，因为本地时间线上有一个时差，通常由夏令时引起");
            }
            throw new DateTimeException("ZoneOffset '" + offset + "' 对 LocalDateTime '" +
                    localDateTime + "' 在时区 '" + zone + "' 中无效");
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    /**
     * 宽松地获取 {@code ZonedDateTime} 实例，适用于高级用例，允许任何组合的本地日期时间、偏移量和时区 ID。
     * <p>
     * 这将创建一个时区日期时间，除了不允许为空外，没有任何其他检查。
     * 这意味着，结果的时区日期时间可能具有与时区 ID 冲突的偏移量。
     * <p>
     * 此方法适用于高级用例。
     * 例如，考虑这样一个情况：创建一个具有有效字段的时区日期时间，然后将其存储在数据库或基于序列化的存储中。在某个时间点之后，
     * 定义时区的政府改变了规则，使得最初存储的本地日期时间现在不存在。此方法可以用于创建一个“无效”状态的对象，尽管规则发生了变化。
     *
     * @param localDateTime  本地日期时间，不为空
     * @param offset  时区偏移量，不为空
     * @param zone  时区，不为空
     * @return 时区日期时间，不为空
     */
    private static ZonedDateTime ofLenient(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        if (zone instanceof ZoneOffset && offset.equals(zone) == false) {
            throw new IllegalArgumentException("ZoneId 必须匹配 ZoneOffset");
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code ZonedDateTime} 实例。
     * <p>
     * 根据指定的时间对象获取一个时区日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，此工厂将其转换为 {@code ZonedDateTime} 的实例。
     * <p>
     * 转换将首先从时间对象中获取一个 {@code ZoneId}，必要时回退到 {@code ZoneOffset}。然后尝试获取一个 {@code Instant}，
     * 必要时回退到 {@code LocalDateTime}。结果将是 {@code ZoneId} 或 {@code ZoneOffset} 与 {@code Instant} 或 {@code LocalDateTime} 的组合。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名，允许通过方法引用将其用作查询，例如 {@code ZonedDateTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 时区日期时间，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code ZonedDateTime}
     */
    public static ZonedDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof ZonedDateTime) {
            return (ZonedDateTime) temporal;
        }
        try {
            ZoneId zone = ZoneId.from(temporal);
            if (temporal.isSupported(INSTANT_SECONDS)) {
                long epochSecond = temporal.getLong(INSTANT_SECONDS);
                int nanoOfSecond = temporal.get(NANO_OF_SECOND);
                return create(epochSecond, nanoOfSecond, zone);
            } else {
                LocalDate date = LocalDate.from(temporal);
                LocalTime time = LocalTime.from(temporal);
                return of(date, time, zone);
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 ZonedDateTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12-03T10:15:30+01:00[Europe/Paris]}）获取 {@code ZonedDateTime} 实例。
     * <p>
     * 字符串必须表示一个有效的日期时间，并使用 {@link java.time.format.DateTimeFormatter#ISO_ZONED_DATE_TIME} 解析。
     *
     * @param text  要解析的文本，如 "2007-12-03T10:15:30+01:00[Europe/Paris]"，不为空
     * @return 解析的时区日期时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static ZonedDateTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code ZonedDateTime} 实例。
     * <p>
     * 使用格式化器解析文本，返回一个日期时间。
     *
     * @param text  要解析的文本，不为空
     * @param formatter  要使用的格式化器，不为空
     * @return 解析的时区日期时间，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, ZonedDateTime::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param dateTime  日期时间，验证为不为空
     * @param offset  时区偏移量，验证为不为空
     * @param zone  时区，验证为不为空
     */
    private ZonedDateTime(LocalDateTime dateTime, ZoneOffset offset, ZoneId zone) {
        this.dateTime = dateTime;
        this.offset = offset;
        this.zone = zone;
    }

    /**
     * 使用此时区 ID 解析新的本地日期时间，尽可能保留偏移量。
     *
     * @param newDateTime  新的本地日期时间，不为空
     * @return 时区日期时间，不为空
     */
    private ZonedDateTime resolveLocal(LocalDateTime newDateTime) {
        return ofLocal(newDateTime, zone, offset);
    }

    /**
     * 使用偏移量识别瞬间来解析新的本地日期时间。
     *
     * @param newDateTime  新的本地日期时间，不为空
     * @return 时区日期时间，不为空
     */
    private ZonedDateTime resolveInstant(LocalDateTime newDateTime) {
        return ofInstant(newDateTime, offset, zone);
    }

    /**
     * 为 with 方法解析偏移量到此时区日期时间。
     * <p>
     * 通常忽略偏移量，除非可以在夏令时重叠时切换偏移量。
     *
     * @param offset  偏移量，不为空
     * @return 时区日期时间，不为空
     */
    private ZonedDateTime resolveOffset(ZoneOffset offset) {
        if (offset.equals(this.offset) == false && zone.getRules().isValidOffset(dateTime, offset)) {
            return new ZonedDateTime(dateTime, offset, zone);
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这检查此日期时间是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
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
     * 字段是否受支持由字段确定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果此日期时间支持该字段则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        return field instanceof ChronoField || (field != null && field.isSupportedBy(this));
    }


                /**
     * 检查指定的单位是否受支持。
     * <p>
     * 此方法检查指定的单位是否可以添加到或从这个日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则此查询在此处实现。
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
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递来获得的。
     * 单位是否受支持由该单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 单位是否可以添加/减去，不能则返回 false
     */
    @Override  // 重写以提供 Javadoc
    public boolean isSupported(TemporalUnit unit) {
        return ChronoZonedDateTime.super.isSupported(unit);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此日期时间用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得的。
     * 是否可以获取范围由字段决定。
     *
     * @param field  要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
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
     * 此方法查询此日期时间中指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值，
     * 但 {@code NANO_OF_DAY}、{@code MICRO_OF_DAY}、{@code EPOCH_DAY}、
     * {@code PROLEPTIC_MONTH} 和 {@code INSTANT_SECONDS} 太大，无法放入 {@code int}，
     * 因此会抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得的。
     * 是否可以获取值，以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc 和性能
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
        return ChronoZonedDateTime.super.get(field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 此方法查询此日期时间中指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得的。
     * 是否可以获取值，以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
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
     * @return 时区偏移，不为 null
     */
    @Override
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * 返回一个副本，将时区偏移更改为本地时间线重叠时的较早偏移。
     * <p>
     * 当本地时间线重叠时，例如在秋季夏令时转换时，此方法才有作用。
     * 在这种情况下，本地日期时间有两个有效的偏移。调用此方法将返回
     * 一个带有较早偏移的带时区的日期时间。
     * <p>
     * 如果调用此方法时不是重叠时间，则返回 {@code this}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此日期时间并带有较早偏移的 {@code ZonedDateTime}，不为 null
     */
    @Override
    public ZonedDateTime withEarlierOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(dateTime);
        if (trans != null && trans.isOverlap()) {
            ZoneOffset earlierOffset = trans.getOffsetBefore();
            if (earlierOffset.equals(offset) == false) {
                return new ZonedDateTime(dateTime, earlierOffset, zone);
            }
        }
        return this;
    }

    /**
     * 返回一个副本，将时区偏移更改为本地时间线重叠时的较晚偏移。
     * <p>
     * 当本地时间线重叠时，例如在秋季夏令时转换时，此方法才有作用。
     * 在这种情况下，本地日期时间有两个有效的偏移。调用此方法将返回
     * 一个带有较晚偏移的带时区的日期时间。
     * <p>
     * 如果调用此方法时不是重叠时间，则返回 {@code this}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此日期时间并带有较晚偏移的 {@code ZonedDateTime}，不为 null
     */
    @Override
    public ZonedDateTime withLaterOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(toLocalDateTime());
        if (trans != null) {
            ZoneOffset laterOffset = trans.getOffsetAfter();
            if (laterOffset.equals(offset) == false) {
                return new ZonedDateTime(dateTime, laterOffset, zone);
            }
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时区，例如 'Europe/Paris'。
     * <p>
     * 此方法返回时区 ID。这标识了时区 {@link ZoneRules 规则}，
     * 决定了与 UTC/Greenwich 的偏移何时以及如何变化。
     * <p>
     * 时区 ID 可能与 {@linkplain #getOffset() 偏移} 相同。
     * 如果这是真的，那么任何未来的计算，如加法或减法，
     * 都不会因时区规则而产生复杂的边缘情况。
     * 请参阅 {@link #withFixedOffsetZone()}。
     *
     * @return 时区，不为 null
     */
    @Override
    public ZoneId getZone() {
        return zone;
    }

    /**
     * 返回一个副本，使用不同的时区，尽可能保留本地日期时间。
     * <p>
     * 此方法更改时区并保留本地日期时间。
     * 仅当本地日期时间对新时区无效时，才会更改本地日期时间，
     * 确定方法与 {@link #ofLocal(LocalDateTime, ZoneId, ZoneOffset)} 相同。
     * <p>
     * 要更改时区并调整本地日期时间，使用 {@link #withZoneSameInstant(ZoneId)}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param zone  要更改的时区，不为 null
     * @return 基于此日期时间并带有请求时区的 {@code ZonedDateTime}，不为 null
     */
    @Override
    public ZonedDateTime withZoneSameLocal(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return this.zone.equals(zone) ? this : ofLocal(dateTime, zone, offset);
    }

    /**
     * 返回一个副本，使用不同的时区，保留瞬间。
     * <p>
     * 此方法更改时区并保留瞬间。
     * 这通常会导致本地日期时间的变化。
     * <p>
     * 此方法基于保留相同的瞬间，因此本地时间线上的间隙和重叠
     * 对结果没有影响。
     * <p>
     * 要更改偏移同时保持本地时间，使用 {@link #withZoneSameLocal(ZoneId)}。
     *
     * @param zone  要更改的时区，不为 null
     * @return 基于此日期时间并带有请求时区的 {@code ZonedDateTime}，不为 null
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    @Override
    public ZonedDateTime withZoneSameInstant(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return this.zone.equals(zone) ? this :
            create(dateTime.toEpochSecond(offset), dateTime.getNano(), zone);
    }

    /**
     * 返回一个副本，将时区 ID 设置为偏移。
     * <p>
     * 此方法返回一个时区 ID 与 {@link #getOffset()} 相同的带时区的日期时间。
     * 结果的本地日期时间、偏移和瞬间将与此日期时间相同。
     * <p>
     * 将日期时间设置为固定单个偏移意味着任何未来的
     * 计算，如加法或减法，都不会因时区规则而产生复杂的边缘情况。
     * 当通过网络发送带时区的日期时间时，这可能也很有用，
     * 因为大多数协议，如 ISO-8601，只处理偏移，而不处理基于区域的时区 ID。
     * <p>
     * 这等同于 {@code ZonedDateTime.of(zdt.toLocalDateTime(), zdt.getOffset())}。
     *
     * @return 时区 ID 设置为偏移的 {@code ZonedDateTime}，不为 null
     */
    public ZonedDateTime withFixedOffsetZone() {
        return this.zone.equals(offset) ? this : new ZonedDateTime(dateTime, offset, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalDateTime} 部分。
     * <p>
     * 此方法返回一个与此日期时间具有相同年、月、日和时间的 {@code LocalDateTime}。
     *
     * @return 此日期时间的本地日期时间部分，不为 null
     */
    @Override  // 重写以指定返回类型
    public LocalDateTime toLocalDateTime() {
        return dateTime;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalDate} 部分。
     * <p>
     * 此方法返回一个与此日期时间具有相同年、月和日的 {@code LocalDate}。
     *
     * @return 此日期时间的日期部分，不为 null
     */
    @Override  // 重写以指定返回类型
    public LocalDate toLocalDate() {
        return dateTime.toLocalDate();
    }

    /**
     * 获取年份字段。
     * <p>
     * 此方法返回年份的原始 {@code int} 值。
     * <p>
     * 通过 {@code get(YEAR)} 返回的年份是历法年。
     * 要获取纪年，使用 {@code get(YEAR_OF_ERA)}。
     *
     * @return 年份，从 MIN_YEAR 到 MAX_YEAR
     */
    public int getYear() {
        return dateTime.getYear();
    }

    /**
     * 获取 1 到 12 的月份字段。
     * <p>
     * 此方法返回 1 到 12 的月份值。
     * 应用代码通常更清晰，如果调用 {@link #getMonth()} 使用枚举 {@link Month}。
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
     * 这样可以避免对 {@code int} 值的含义产生混淆。
     * 如果需要访问原始的 {@code int} 值，则枚举
     * 提供了 {@link Month#getValue() int 值}。
     *
     * @return 月份，不为空
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return dateTime.getMonth();
    }

    /**
     * 获取月份中的日期字段。
     * <p>
     * 此方法返回月份中的原始 {@code int} 值。
     *
     * @return 月份中的日期，从 1 到 31
     */
    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    /**
     * 获取年份中的日期字段。
     * <p>
     * 此方法返回年份中的原始 {@code int} 值。
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
     * 这样可以避免对 {@code int} 值的含义产生混淆。
     * 如果需要访问原始的 {@code int} 值，则枚举
     * 提供了 {@link DayOfWeek#getValue() int 值}。
     * <p>
     * 可以从 {@code DayOfWeek} 获取更多详细信息。
     * 这包括值的文本名称。
     *
     * @return 星期几，不为空
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
     * @return 此日期时间的时间部分，不为空
     */
    @Override  // 重写用于 Javadoc 和性能
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
     * 此方法返回一个基于此日期时间的 {@code ZonedDateTime}，日期时间已调整。
     * 调整是使用指定的调整器策略对象进行的。
     * 请阅读调整器的文档以了解将进行哪些调整。
     * <p>
     * 一个简单的调整器可能只是设置一个字段，例如年份字段。
     * 一个更复杂的调整器可能将日期设置为该月的最后一天。
     * 一些常见的调整器在
     * {@link java.time.temporal.TemporalAdjusters TemporalAdjusters} 中提供。
     * 这些包括“该月的最后一天”和“下一个星期三”。
     * 关键日期时间类也实现了 {@code TemporalAdjuster} 接口，
     * 例如 {@link Month} 和 {@link java.time.MonthDay MonthDay}。
     * 调整器负责处理特殊情况，例如月份长度不同和闰年。
     * <p>
     * 例如，此代码返回 7 月的最后一天：
     * <pre>
     *  import static java.time.Month.*;
     *  import static java.time.temporal.TemporalAdjusters.*;
     *
     *  result = zonedDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     * <p>
     * {@link LocalDate} 和 {@link LocalTime} 实现了 {@code TemporalAdjuster}，
     * 因此可以使用此方法更改日期、时间或偏移量：
     * <pre>
     *  result = zonedDateTime.with(date);
     *  result = zonedDateTime.with(time);
     * </pre>
     * <p>
     * {@link ZoneOffset} 也实现了 {@code TemporalAdjuster}，但将其用作参数通常没有效果。
     * {@code ZonedDateTime} 的偏移量主要由时区控制。因此，更改偏移量通常没有意义，
     * 因为对于本地日期时间和时区，只有一个有效的偏移量。
     * 如果带有时区的日期时间处于夏令时重叠中，则偏移量用于在两个有效偏移量之间切换。
     * 在所有其他情况下，偏移量将被忽略。
     * <p>
     * 通过调用指定调整器的
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不为空
     * @return 基于 {@code this} 并进行了调整的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalDate) {
            return resolveLocal(LocalDateTime.of((LocalDate) adjuster, dateTime.toLocalTime()));
        } else if (adjuster instanceof LocalTime) {
            return resolveLocal(LocalDateTime.of(dateTime.toLocalDate(), (LocalTime) adjuster));
        } else if (adjuster instanceof LocalDateTime) {
            return resolveLocal((LocalDateTime) adjuster);
        } else if (adjuster instanceof OffsetDateTime) {
            OffsetDateTime odt = (OffsetDateTime) adjuster;
            return ofLocal(odt.toLocalDateTime(), zone, odt.getOffset());
        } else if (adjuster instanceof Instant) {
            Instant instant = (Instant) adjuster;
            return create(instant.getEpochSecond(), instant.getNano(), zone);
        } else if (adjuster instanceof ZoneOffset) {
            return resolveOffset((ZoneOffset) adjuster);
        }
        return (ZonedDateTime) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此日期时间的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此日期时间的 {@code ZonedDateTime}，指定字段的值已更改。
     * 可以使用此方法更改任何支持的字段，例如年份、月份或月份中的日期。
     * 如果由于字段不受支持或其他原因而无法设置值，则会抛出异常。
     * <p>
     * 在某些情况下，更改指定字段可能会导致结果日期时间变得无效，
     * 例如将 1 月 31 日的月份更改为 2 月会使月份中的日期无效。
     * 在这种情况下，字段负责解析日期。通常会选择
     * 前一个有效的日期，例如在此示例中选择 2 月的最后一天。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * <p>
     * {@code INSTANT_SECONDS} 字段将返回具有指定即时值的日期时间。
     * 时区和纳秒不变。
     * 结果将具有从新即时值和原始时区派生的偏移量。
     * 如果新的即时值超出有效范围，则会抛出 {@code DateTimeException}。
     * <p>
     * {@code OFFSET_SECONDS} 字段通常会被忽略。
     * {@code ZonedDateTime} 的偏移量主要由时区控制。因此，更改偏移量通常没有意义，
     * 因为对于本地日期时间和时区，只有一个有效的偏移量。
     * 如果带有时区的日期时间处于夏令时重叠中，则偏移量用于在两个有效偏移量之间切换。
     * 在所有其他情况下，偏移量将被忽略。
     * 如果新的偏移值超出有效范围，则会抛出 {@code DateTimeException}。
     * <p>
     * 其他 {@link #isSupported(TemporalField) 支持的字段} 将按照
     * {@link LocalDateTime#with(TemporalField, long) LocalDateTime} 的相应方法进行处理。
     * 时区不参与计算，将保持不变。
     * 当转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能，将保留偏移量，否则将使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 并传递 {@code this} 作为参数来获取此方法的结果。
     * 在这种情况下，字段确定是否以及如何调整即时值。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不为空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case INSTANT_SECONDS:
                    return create(newValue, getNano(), zone);
                case OFFSET_SECONDS:
                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue));
                    return resolveOffset(offset);
            }
            return resolveLocal(dateTime.with(field, newValue));
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，年份已更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#withYear(int) 更改本地日期时间的年份}。
     * 然后将其转换回 {@code ZonedDateTime}，使用时区 ID 获取偏移量。
     * <p>
     * 当转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能，将保留偏移量，否则将使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param year  要在结果中设置的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 基于此日期时间并具有请求年份的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果年份值无效
     */
    public ZonedDateTime withYear(int year) {
        return resolveLocal(dateTime.withYear(year));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，月份已更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#withMonth(int) 更改本地日期时间的月份}。
     * 然后将其转换回 {@code ZonedDateTime}，使用时区 ID 获取偏移量。
     * <p>
     * 当转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能，将保留偏移量，否则将使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param month  要在结果中设置的月份，从 1（1 月）到 12（12 月）
     * @return 基于此日期时间并具有请求月份的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果月份值无效
     */
    public ZonedDateTime withMonth(int month) {
        return resolveLocal(dateTime.withMonth(month));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，月份中的日期已更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#withDayOfMonth(int) 更改本地日期时间的月份中的日期}。
     * 然后将其转换回 {@code ZonedDateTime}，使用时区 ID 获取偏移量。
     * <p>
     * 当转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能，将保留偏移量，否则将使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param dayOfMonth  要在结果中设置的月份中的日期，从 1 到 28-31
     * @return 基于此日期时间并具有请求日期的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果月份中的日期值无效，
     *  或月份中的日期对于该年份和月份无效
     */
    public ZonedDateTime withDayOfMonth(int dayOfMonth) {
        return resolveLocal(dateTime.withDayOfMonth(dayOfMonth));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，年份中的日期已更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#withDayOfYear(int) 更改本地日期时间的年份中的日期}。
     * 然后将其转换回 {@code ZonedDateTime}，使用时区 ID 获取偏移量。
     * <p>
     * 当转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能，将保留偏移量，否则将使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param dayOfYear  要在结果中设置的年份中的日期，从 1 到 365-366
     * @return 基于此日期并具有请求日期的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果年份中的日期值无效，
     *  或年份中的日期对于该年份无效
     */
    public ZonedDateTime withDayOfYear(int dayOfYear) {
        return resolveLocal(dateTime.withDayOfYear(dayOfYear));
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中小时被更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@linkplain LocalDateTime#withHour(int) 更改本地日期时间的时间}。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hour  结果中要设置的小时数，从 0 到 23
     * @return 基于此日期时间的 {@code ZonedDateTime}，具有请求的小时数，不为空
     * @throws DateTimeException 如果小时值无效
     */
    public ZonedDateTime withHour(int hour) {
        return resolveLocal(dateTime.withHour(hour));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中分钟被更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@linkplain LocalDateTime#withMinute(int) 更改本地日期时间的时间}。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param minute  结果中要设置的分钟数，从 0 到 59
     * @return 基于此日期时间的 {@code ZonedDateTime}，具有请求的分钟数，不为空
     * @throws DateTimeException 如果分钟值无效
     */
    public ZonedDateTime withMinute(int minute) {
        return resolveLocal(dateTime.withMinute(minute));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中秒被更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@linkplain LocalDateTime#withSecond(int) 更改本地日期时间的时间}。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param second  结果中要设置的秒数，从 0 到 59
     * @return 基于此日期时间的 {@code ZonedDateTime}，具有请求的秒数，不为空
     * @throws DateTimeException 如果秒值无效
     */
    public ZonedDateTime withSecond(int second) {
        return resolveLocal(dateTime.withSecond(second));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中纳秒被更改。
     * <p>
     * 此操作在本地时间线上进行，
     * {@linkplain LocalDateTime#withNano(int) 更改本地日期时间的时间}。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param nanoOfSecond  结果中要设置的纳秒数，从 0 到 999,999,999
     * @return 基于此日期时间的 {@code ZonedDateTime}，具有请求的纳秒数，不为空
     * @throws DateTimeException 如果纳秒值无效
     */
    public ZonedDateTime withNano(int nanoOfSecond) {
        return resolveLocal(dateTime.withNano(nanoOfSecond));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中时间被截断。
     * <p>
     * 截断返回一个原始日期时间的副本，其中比指定单位小的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒和纳秒字段设置为零。
     * <p>
     * 单位必须具有一个 {@linkplain TemporalUnit#getDuration() 持续时间}，
     * 该持续时间可以无余数地除进标准天的长度。
     * 这包括 {@link ChronoUnit} 中提供的所有时间单位和 {@link ChronoUnit#DAYS 天}。
     * 其他单位将抛出异常。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#truncatedTo(TemporalUnit) 截断} 底层的本地日期时间。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此日期时间的 {@code ZonedDateTime}，时间被截断，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public ZonedDateTime truncatedTo(TemporalUnit unit) {
        return resolveLocal(dateTime.truncatedTo(unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此日期时间的副本，其中指定了要添加的量。
     * <p>
     * 此方法返回一个 {@code ZonedDateTime}，基于此日期时间，指定了要添加的量。
     * 量通常是 {@link Period} 或 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给量对象，通过调用
     * {@link TemporalAmount#addTo(Temporal)}。量实现可以自由地以任何方式实现添加，
     * 但通常会回调 {@link #plus(long, TemporalUnit)}。查阅量实现的文档以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加的量，不为空
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period) amountToAdd;
            return resolveLocal(dateTime.plus(periodToAdd));
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (ZonedDateTime) amountToAdd.addTo(this);
    }

    /**
     * 返回一个基于此日期时间的副本，其中指定了要添加的量。
     * <p>
     * 此方法返回一个 {@code ZonedDateTime}，基于此日期时间，以单位形式添加了量。
     * 如果由于单位不受支持或其他原因无法添加量，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则添加在此处实现。
     * 时区不是计算的一部分，结果中将保持不变。
     * 日期和时间单位的计算方式不同。
     * <p>
     * 日期单位在本地时间线上操作。
     * 首先将周期添加到本地日期时间，然后使用时区ID转换回带时区的日期时间。
     * 转换使用 {@link #ofLocal(LocalDateTime, ZoneId, ZoneOffset)}，使用添加前的偏移量。
     * <p>
     * 时间单位在即时时间线上操作。
     * 首先将周期添加到本地日期时间，然后使用时区ID转换回带时区的日期时间。
     * 转换使用 {@link #ofInstant(LocalDateTime, ZoneOffset, ZoneId)}，使用添加前的偏移量。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.addTo(Temporal, long)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。在这种情况下，单位确定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加到结果中的单位量，可以为负数
     * @param unit  要添加的单位量，不为空
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了指定的量，不为空
     * @throws DateTimeException 如果无法添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            if (unit.isDateBased()) {
                return resolveLocal(dateTime.plus(amountToAdd, unit));
            } else {
                return resolveInstant(dateTime.plus(amountToAdd, unit));
            }
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中指定了要添加的年数。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#plusYears(long) 添加年份} 到本地日期时间。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param years  要添加的年数，可以为负数
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusYears(long years) {
        return resolveLocal(dateTime.plusYears(years));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中指定了要添加的月数。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#plusMonths(long) 添加月数} 到本地日期时间。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要添加的月数，可以为负数
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了月数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusMonths(long months) {
        return resolveLocal(dateTime.plusMonths(months));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中指定了要添加的周数。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#plusWeeks(long) 添加周数} 到本地日期时间。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param weeks  要添加的周数，可以为负数
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了周数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusWeeks(long weeks) {
        return resolveLocal(dateTime.plusWeeks(weeks));
    }

    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中指定了要添加的天数。
     * <p>
     * 此操作在本地时间线上进行，
     * {@link LocalDateTime#plusDays(long) 添加天数} 到本地日期时间。
     * 然后使用时区ID获取偏移量，将此时间转换回 {@code ZonedDateTime}。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，
     * 则如果可能将保留偏移量，否则使用较早的偏移量。
     * 如果处于间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要添加的天数，可以为负数
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了天数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusDays(long days) {
        return resolveLocal(dateTime.plusDays(days));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code ZonedDateTime} 的副本，其中指定了要添加的小时数。
     * <p>
     * 此操作在即时时间线上进行，因此添加一个小时总是会晚一个小时。
     * 这可能导致本地日期时间的变化量不是一小时。
     * 请注意，这与天、月和年使用的方法不同，因此添加一天与添加24小时不同。
     * <p>
     * 例如，考虑一个时区，春季DST转换意味着本地时间01:00到01:59出现两次，从偏移量+02:00变为+01:00。
     * <ul>
     * <li>在00:30+02:00上添加一个小时将导致01:30+02:00
     * <li>在01:30+02:00上添加一个小时将导致01:30+01:00
     * <li>在01:30+01:00上添加一个小时将导致02:30+01:00
     * <li>在00:30+02:00上添加三个小时将导致02:30+01:00
     * </ul>
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param hours  要添加的小时数，可以为负数
     * @return 基于此日期时间的 {@code ZonedDateTime}，添加了小时数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusHours(long hours) {
        return resolveInstant(dateTime.plusHours(hours));
    }


                /**
     * 返回一个副本，其中指定了要添加的分钟数。
     * <p>
     * 该操作在即时时间线上进行，因此添加一分钟将始终是之后的一分钟。
     * 这可能会导致本地日期时间变化的量不同于一分钟。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param minutes  要添加的分钟数，可以是负数
     * @return 基于此日期时间并添加了分钟数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusMinutes(long minutes) {
        return resolveInstant(dateTime.plusMinutes(minutes));
    }

    /**
     * 返回一个副本，其中指定了要添加的秒数。
     * <p>
     * 该操作在即时时间线上进行，因此添加一秒将始终是之后的一秒。
     * 这可能会导致本地日期时间变化的量不同于一秒。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param seconds  要添加的秒数，可以是负数
     * @return 基于此日期时间并添加了秒数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusSeconds(long seconds) {
        return resolveInstant(dateTime.plusSeconds(seconds));
    }

    /**
     * 返回一个副本，其中指定了要添加的纳秒数。
     * <p>
     * 该操作在即时时间线上进行，因此添加一纳秒将始终是之后的一纳秒。
     * 这可能会导致本地日期时间变化的量不同于一纳秒。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param nanos  要添加的纳秒数，可以是负数
     * @return 基于此日期时间并添加了纳秒数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime plusNanos(long nanos) {
        return resolveInstant(dateTime.plusNanos(nanos));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的量。
     * <p>
     * 该方法返回一个基于此日期时间的 {@code ZonedDateTime}，其中指定了要减去的量。
     * 该量通常是 {@link Period} 或 {@link Duration}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给量对象，通过调用 {@link TemporalAmount#subtractFrom(Temporal)}。量实现可以自由地以任何方式实现减法，但通常会回调 {@link #minus(long, TemporalUnit)}。请参阅量实现的文档，以确定是否可以成功减去。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要减去的量，不为空
     * @return 基于此日期时间并进行了减法操作的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period) amountToSubtract;
            return resolveLocal(dateTime.minus(periodToSubtract));
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (ZonedDateTime) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，其中指定了要减去的量。
     * <p>
     * 该方法返回一个基于此日期时间的 {@code ZonedDateTime}，其中指定了要减去的量。如果无法减去该量，因为单位不受支持或有其他原因，将抛出异常。
     * <p>
     * 日期单位和时间单位的计算方式不同。
     * <p>
     * 日期单位在本地时间线上操作。
     * 首先从本地日期时间中减去该周期，然后使用时区 ID 转换回带时区的日期时间。
     * 转换使用 {@link #ofLocal(LocalDateTime, ZoneId, ZoneOffset)}，并使用减法前的偏移量。
     * <p>
     * 时间单位在即时时间线上操作。
     * 首先从本地日期时间中减去该周期，然后使用时区 ID 转换回带时区的日期时间。
     * 转换使用 {@link #ofInstant(LocalDateTime, ZoneOffset, ZoneId)}，并使用减法前的偏移量。
     * <p>
     * 该方法等同于 {@link #plus(long, TemporalUnit)}，但量为负数。请参阅该方法以了解加法（以及减法）的详细工作原理。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要从结果中减去的单位量，可以是负数
     * @param unit  要减去的单位量，不为空
     * @return 基于此日期时间并减去了指定量的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的年数。
     * <p>
     * 该操作在本地时间线上进行，通过 {@link LocalDateTime#minusYears(long)} 从本地日期时间中减去年数。
     * 然后使用时区 ID 将其转换回 {@code ZonedDateTime}，以获取偏移量。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，则如果可能将保留偏移量，否则将使用较早的偏移量。如果在间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param years  要减去的年数，可以是负数
     * @return 基于此日期时间并减去了年数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusYears(long years) {
        return (years == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-years));
    }

    /**
     * 返回一个副本，其中指定了要减去的月数。
     * <p>
     * 该操作在本地时间线上进行，通过 {@link LocalDateTime#minusMonths(long)} 从本地日期时间中减去月数。
     * 然后使用时区 ID 将其转换回 {@code ZonedDateTime}，以获取偏移量。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，则如果可能将保留偏移量，否则将使用较早的偏移量。如果在间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param months  要减去的月数，可以是负数
     * @return 基于此日期时间并减去了月数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusMonths(long months) {
        return (months == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-months));
    }

    /**
     * 返回一个副本，其中指定了要减去的周数。
     * <p>
     * 该操作在本地时间线上进行，通过 {@link LocalDateTime#minusWeeks(long)} 从本地日期时间中减去周数。
     * 然后使用时区 ID 将其转换回 {@code ZonedDateTime}，以获取偏移量。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，则如果可能将保留偏移量，否则将使用较早的偏移量。如果在间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param weeks  要减去的周数，可以是负数
     * @return 基于此日期时间并减去了周数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusWeeks(long weeks) {
        return (weeks == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeks));
    }

    /**
     * 返回一个副本，其中指定了要减去的天数。
     * <p>
     * 该操作在本地时间线上进行，通过 {@link LocalDateTime#minusDays(long)} 从本地日期时间中减去天数。
     * 然后使用时区 ID 将其转换回 {@code ZonedDateTime}，以获取偏移量。
     * <p>
     * 在转换回 {@code ZonedDateTime} 时，如果本地日期时间处于重叠中，则如果可能将保留偏移量，否则将使用较早的偏移量。如果在间隔中，本地日期时间将向前调整间隔的长度。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param days  要减去的天数，可以是负数
     * @return 基于此日期时间并减去了天数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusDays(long days) {
        return (days == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-days));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的小时数。
     * <p>
     * 该操作在即时时间线上进行，因此减去一小时将始终是之前的一小时。
     * 这可能会导致本地日期时间变化的量不同于一小时。
     * 请注意，这与用于天、月和年的方法不同，因此减去一天并不等同于加上24小时。
     * <p>
     * 例如，考虑一个时区，其中春季夏令时转换意味着本地时间 01:00 到 01:59 重复两次，偏移量从 +02:00 变为 +01:00。
     * <ul>
     * <li>从 02:30+01:00 减去一小时将得到 01:30+02:00
     * <li>从 01:30+01:00 减去一小时将得到 01:30+02:00
     * <li>从 01:30+02:00 减去一小时将得到 00:30+01:00
     * <li>从 02:30+01:00 减去三小时将得到 00:30+02:00
     * </ul>
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param hours  要减去的小时数，可以是负数
     * @return 基于此日期时间并减去了小时数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusHours(long hours) {
        return (hours == Long.MIN_VALUE ? plusHours(Long.MAX_VALUE).plusHours(1) : plusHours(-hours));
    }

    /**
     * 返回一个副本，其中指定了要减去的分钟数。
     * <p>
     * 该操作在即时时间线上进行，因此减去一分钟将始终是之前的一分钟。
     * 这可能会导致本地日期时间变化的量不同于一分钟。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param minutes  要减去的分钟数，可以是负数
     * @return 基于此日期时间并减去了分钟数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusMinutes(long minutes) {
        return (minutes == Long.MIN_VALUE ? plusMinutes(Long.MAX_VALUE).plusMinutes(1) : plusMinutes(-minutes));
    }

    /**
     * 返回一个副本，其中指定了要减去的秒数。
     * <p>
     * 该操作在即时时间线上进行，因此减去一秒将始终是之前的一秒。
     * 这可能会导致本地日期时间变化的量不同于一秒。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param seconds  要减去的秒数，可以是负数
     * @return 基于此日期时间并减去了秒数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusSeconds(long seconds) {
        return (seconds == Long.MIN_VALUE ? plusSeconds(Long.MAX_VALUE).plusSeconds(1) : plusSeconds(-seconds));
    }

    /**
     * 返回一个副本，其中指定了要减去的纳秒数。
     * <p>
     * 该操作在即时时间线上进行，因此减去一纳秒将始终是之前的一纳秒。
     * 这可能会导致本地日期时间变化的量不同于一纳秒。
     * 请注意，这与用于天、月和年的方法不同。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param nanos  要减去的纳秒数，可以是负数
     * @return 基于此日期时间并减去了纳秒数的 {@code ZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public ZonedDateTime minusNanos(long nanos) {
        return (nanos == Long.MIN_VALUE ? plusNanos(Long.MAX_VALUE).plusNanos(1) : plusNanos(-nanos));
    }


                //-----------------------------------------------------------------------
    /**
     * 使用指定的查询来查询此日期时间。
     * <p>
     * 此方法使用指定的查询策略对象来查询此日期时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询的文档以了解
     * 此方法的结果将是什么。
     * <p>
     * 该方法的结果是通过调用指定查询上的
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不能为空
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override  // 重写以供 Javadoc 使用
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R) toLocalDate();
        }
        return ChronoZonedDateTime.super.query(query);
    }

    /**
     * 计算到另一个日期时间之间的指定单位的时间量。
     * <p>
     * 此方法计算两个 {@code ZonedDateTime} 对象之间的指定 {@code TemporalUnit} 的时间量。
     * 起点和终点分别是 {@code this} 和指定的日期时间。
     * 如果终点在起点之前，结果将为负数。
     * 例如，可以使用 {@code startDateTime.until(endDateTime, DAYS)} 计算两个日期时间之间的天数。
     * <p>
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为
     * {@code ZonedDateTime}。如果两个带时区的日期时间的时区不同，指定的终点日期时间将被规范化为与
     * 此日期时间具有相同的时区。
     * <p>
     * 计算返回一个整数，表示两个日期时间之间的完整单位数。
     * 例如，2012-06-15T00:00Z 和 2012-08-14T23:59Z 之间的月数将仅为一个月，因为还差一分钟才满两个月。
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
     * 该计算在 {@link ChronoUnit} 中实现。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS}、{@code HALF_DAYS}、{@code DAYS}、
     * {@code WEEKS}、{@code MONTHS}、{@code YEARS}、{@code DECADES}、
     * {@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 日期单位和时间单位的计算方式不同。
     * <p>
     * 日期单位在本地时间线上操作，使用本地日期时间。
     * 例如，从第1天中午到次日中午的天数将始终计为恰好一天，无论是否有夏令时变化。
     * <p>
     * 时间单位在即时时间线上操作。
     * 计算实际上将两个带时区的日期时间转换为即时，然后计算两个即时之间的期间。
     * 例如，从第1天中午到次日中午的小时数可能是23、24或25小时（或其他数量），具体取决于是否有夏令时变化。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.between(Temporal, Temporal)} 并传递 {@code this} 作为第一个参数和转换后的输入
     * 时间作为第二个参数获得的。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param endExclusive  终点日期，不包括，将转换为 {@code ZonedDateTime}，不能为空
     * @param unit  要测量的时间量的单位，不能为空
     * @return 从该日期时间到终点日期时间的时间量
     * @throws DateTimeException 如果无法计算时间量，或终点时间无法转换为 {@code ZonedDateTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        ZonedDateTime end = ZonedDateTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            end = end.withZoneSameInstant(zone);
            if (unit.isDateBased()) {
                return dateTime.until(end.dateTime, unit);
            } else {
                return toOffsetDateTime().until(end.toOffsetDateTime(), unit);
            }
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化器格式化此日期时间。
     * <p>
     * 此日期时间将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不能为空
     * @return 格式化的日期时间字符串，不能为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    @Override  // 重写以供 Javadoc 和性能使用
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间转换为 {@code OffsetDateTime}。
     * <p>
     * 此方法使用本地日期时间和偏移量创建一个偏移日期时间。时区 ID 被忽略。
     *
     * @return 一个表示相同本地日期时间和偏移量的偏移日期时间，不能为空
     */
    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.of(dateTime, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期时间是否等于另一个日期时间。
     * <p>
     * 比较基于偏移日期时间和时区。仅比较 {@code ZonedDateTime} 类型的对象，其他类型返回 false。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期时间等于其他日期时间，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ZonedDateTime) {
            ZonedDateTime other = (ZonedDateTime) obj;
            return dateTime.equals(other.dateTime) &&
                offset.equals(other.offset) &&
                zone.equals(other.zone);
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
        return dateTime.hashCode() ^ offset.hashCode() ^ Integer.rotateLeft(zone.hashCode(), 3);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间输出为 {@code String}，例如
     * {@code 2007-12-03T10:15:30+01:00[Europe/Paris]}。
     * <p>
     * 格式由 {@code LocalDateTime} 后跟 {@code ZoneOffset} 组成。
     * 如果 {@code ZoneId} 与偏移量不同，则输出 ID。如果偏移量和 ID 相同，输出与 ISO-8601 兼容。
     *
     * @return 此日期时间的字符串表示，不能为空
     */
    @Override  // 重写以供 Javadoc 使用
    public String toString() {
        String str = dateTime.toString() + offset.toString();
        if (offset != zone) {
            str += '[' + zone.toString() + ']';
        }
        return str;
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(6);  // 标识一个 ZonedDateTime
     *  // 排除一个字节头的 <a href="../../serialized-form.html#java.time.LocalDateTime">dateTime</a>
     *  // 排除一个字节头的 <a href="../../serialized-form.html#java.time.ZoneOffset">offset</a>
     *  // 排除一个字节头的 <a href="../../serialized-form.html#java.time.ZoneId">zone ID</a>
     * </pre>
     *
     * @return {@code Ser} 的实例，不能为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZONE_DATE_TIME_TYPE, this);
    }

    /**
     * 防止恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化委托进行反序列化");
    }

    void writeExternal(DataOutput out) throws IOException {
        dateTime.writeExternal(out);
        offset.writeExternal(out);
        zone.write(out);
    }

    static ZonedDateTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocalDateTime dateTime = LocalDateTime.readExternal(in);
        ZoneOffset offset = ZoneOffset.readExternal(in);
        ZoneId zone = (ZoneId) Ser.read(in);
        return ZonedDateTime.ofLenient(dateTime, offset, zone);
    }

}
