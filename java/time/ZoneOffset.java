
/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

import static java.time.LocalTime.MINUTES_PER_HOUR;
import static java.time.LocalTime.SECONDS_PER_HOUR;
import static java.time.LocalTime.SECONDS_PER_MINUTE;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 从格林尼治/UTC的时间区偏移量，例如 {@code +02:00}。
 * <p>
 * 时间区偏移量是从格林尼治/UTC的时间区差异的量。这通常是一个固定数量的小时和分钟。
 * <p>
 * 世界不同地区有不同的时间区偏移量。
 * 《时间区规则》类捕获了这些偏移量如何随地点和时间变化的规则。
 * <p>
 * 例如，巴黎在冬季比格林尼治/UTC提前一小时，在夏季提前两小时。
 * 巴黎的 {@code ZoneId} 实例将引用两个 {@code ZoneOffset} 实例——一个用于冬季的 {@code +01:00} 实例，
 * 一个用于夏季的 {@code +02:00} 实例。
 * <p>
 * 2008年，世界各地的时间区偏移量范围从 -12:00 到 +14:00。
 * 为了防止该范围的扩展，同时仍提供验证，偏移量的范围被限制在 -18:00 到 18:00 之间（包括两端）。
 * <p>
 * 本类设计用于 ISO 日历系统。
 * 小时、分钟和秒的字段假设符合 ISO 标准定义。本类可以与其他日历系统一起使用，
 * 只要时间字段的定义与 ISO 日历系统的定义匹配。
 * <p>
 * {@code ZoneOffset} 的实例必须使用 {@link #equals} 进行比较。
 * 实现可以选择缓存某些常见的偏移量，但应用程序不应依赖这种缓存。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code ZoneOffset} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 本类是不可变的，线程安全的。
 *
 * @since 1.8
 */
public final class ZoneOffset
        extends ZoneId
        implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {

    /** 按偏移秒数缓存的时间区偏移量。 */
    private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap<>(16, 0.75f, 4);
    /** 按 ID 缓存的时间区偏移量。 */
    private static final ConcurrentMap<String, ZoneOffset> ID_CACHE = new ConcurrentHashMap<>(16, 0.75f, 4);

    /**
     * 绝对最大秒数。
     */
    private static final int MAX_SECONDS = 18 * SECONDS_PER_HOUR;
    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 2357656521762053153L;

    /**
     * UTC 的时间区偏移量，ID 为 'Z'。
     */
    public static final ZoneOffset UTC = ZoneOffset.ofTotalSeconds(0);
    /**
     * 支持的最大偏移量常量。
     */
    public static final ZoneOffset MIN = ZoneOffset.ofTotalSeconds(-MAX_SECONDS);
    /**
     * 支持的最大偏移量常量。
     */
    public static final ZoneOffset MAX = ZoneOffset.ofTotalSeconds(MAX_SECONDS);

    /**
     * 总偏移秒数。
     */
    private final int totalSeconds;
    /**
     * 时间区偏移量的字符串形式。
     */
    private final transient String id;

    //-----------------------------------------------------------------------
    /**
     * 使用 ID 获取 {@code ZoneOffset} 的实例。
     * <p>
     * 该方法解析 {@code ZoneOffset} 的字符串 ID 以返回一个实例。解析接受所有由
     * {@link #getId()} 生成的格式，以及一些额外的格式：
     * <ul>
     * <li>{@code Z} - 表示 UTC
     * <li>{@code +h}
     * <li>{@code +hh}
     * <li>{@code +hh:mm}
     * <li>{@code -hh:mm}
     * <li>{@code +hhmm}
     * <li>{@code -hhmm}
     * <li>{@code +hh:mm:ss}
     * <li>{@code -hh:mm:ss}
     * <li>{@code +hhmmss}
     * <li>{@code -hhmmss}
     * </ul>
     * 注意 &plusmn; 表示加号或减号。
     * <p>
     * 返回偏移量的 ID 将被规范化为 {@link #getId()} 描述的格式之一。
     * <p>
     * 支持的最大范围是从 +18:00 到 -18:00（包括两端）。
     *
     * @param offsetId  偏移量 ID，不为空
     * @return 时间区偏移量，不为空
     * @throws DateTimeException 如果偏移量 ID 无效
     */
    @SuppressWarnings("fallthrough")
    public static ZoneOffset of(String offsetId) {
        Objects.requireNonNull(offsetId, "offsetId");
        // "Z" 总是在缓存中
        ZoneOffset offset = ID_CACHE.get(offsetId);
        if (offset != null) {
            return offset;
        }

        // 解析 - +h, +hh, +hhmm, +hh:mm, +hhmmss, +hh:mm:ss
        final int hours, minutes, seconds;
        switch (offsetId.length()) {
            case 2:
                offsetId = offsetId.charAt(0) + "0" + offsetId.charAt(1);  // fallthru
            case 3:
                hours = parseNumber(offsetId, 1, false);
                minutes = 0;
                seconds = 0;
                break;
            case 5:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = 0;
                break;
            case 6:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = 0;
                break;
            case 7:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = parseNumber(offsetId, 5, false);
                break;
            case 9:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = parseNumber(offsetId, 7, true);
                break;
            default:
                throw new DateTimeException("Invalid ID for ZoneOffset, invalid format: " + offsetId);
        }
        char first = offsetId.charAt(0);
        if (first != '+' && first != '-') {
            throw new DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: " + offsetId);
        }
        if (first == '-') {
            return ofHoursMinutesSeconds(-hours, -minutes, -seconds);
        } else {
            return ofHoursMinutesSeconds(hours, minutes, seconds);
        }
    }

    /**
     * 解析一个两位零前缀的数字。
     *
     * @param offsetId  偏移量 ID，不为空
     * @param pos  要解析的位置，有效
     * @param precededByColon  该数字是否应以冒号为前缀
     * @return 解析的数字，从 0 到 99
     */
    private static int parseNumber(CharSequence offsetId, int pos, boolean precededByColon) {
        if (precededByColon && offsetId.charAt(pos - 1) != ':') {
            throw new DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: " + offsetId);
        }
        char ch1 = offsetId.charAt(pos);
        char ch2 = offsetId.charAt(pos + 1);
        if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
            throw new DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: " + offsetId);
        }
        return (ch1 - 48) * 10 + (ch2 - 48);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用小时偏移量获取 {@code ZoneOffset} 的实例。
     *
     * @param hours  时间区偏移量的小时数，从 -18 到 +18
     * @return 时间区偏移量，不为空
     * @throws DateTimeException 如果偏移量不在所需范围内
     */
    public static ZoneOffset ofHours(int hours) {
        return ofHoursMinutesSeconds(hours, 0, 0);
    }

    /**
     * 使用小时和分钟偏移量获取 {@code ZoneOffset} 的实例。
     * <p>
     * 小时和分钟组件的符号必须匹配。
     * 因此，如果小时为负数，分钟也必须为负数或零。
     * 如果小时为零，分钟可以为正数、负数或零。
     *
     * @param hours  时间区偏移量的小时数，从 -18 到 +18
     * @param minutes  时间区偏移量的分钟数，从 0 到 &plusmn;59，符号与小时匹配
     * @return 时间区偏移量，不为空
     * @throws DateTimeException 如果偏移量不在所需范围内
     */
    public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
        return ofHoursMinutesSeconds(hours, minutes, 0);
    }

    /**
     * 使用小时、分钟和秒偏移量获取 {@code ZoneOffset} 的实例。
     * <p>
     * 小时、分钟和秒组件的符号必须匹配。
     * 因此，如果小时为负数，分钟和秒也必须为负数或零。
     *
     * @param hours  时间区偏移量的小时数，从 -18 到 +18
     * @param minutes  时间区偏移量的分钟数，从 0 到 &plusmn;59，符号与小时和秒匹配
     * @param seconds  时间区偏移量的秒数，从 0 到 &plusmn;59，符号与小时和分钟匹配
     * @return 时间区偏移量，不为空
     * @throws DateTimeException 如果偏移量不在所需范围内
     */
    public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
        validate(hours, minutes, seconds);
        int totalSeconds = totalSeconds(hours, minutes, seconds);
        return ofTotalSeconds(totalSeconds);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code ZoneOffset} 的实例。
     * <p>
     * 该方法基于指定的时间对象获取偏移量。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ZoneOffset} 的实例。
     * <p>
     * {@code TemporalAccessor} 表示某种形式的日期和时间信息。
     * 该工厂将任意的时间对象转换为 {@code ZoneOffset} 的实例。
     * <p>
     * 转换使用 {@link TemporalQueries#offset()} 查询，该查询依赖于提取
     * {@link ChronoField#OFFSET_SECONDS OFFSET_SECONDS} 字段。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 功能接口的签名，允许通过方法引用使用它，例如 {@code ZoneOffset::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 时间区偏移量，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code ZoneOffset}
     */
    public static ZoneOffset from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        ZoneOffset offset = temporal.query(TemporalQueries.offset());
        if (offset == null) {
            throw new DateTimeException("Unable to obtain ZoneOffset from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName());
        }
        return offset;
    }

    //-----------------------------------------------------------------------
    /**
     * 验证偏移量字段。
     *
     * @param hours  时间区偏移量的小时数，从 -18 到 +18
     * @param minutes  时间区偏移量的分钟数，从 0 到 &plusmn;59
     * @param seconds  时间区偏移量的秒数，从 0 到 &plusmn;59
     * @throws DateTimeException 如果偏移量不在所需范围内
     */
    private static void validate(int hours, int minutes, int seconds) {
        if (hours < -18 || hours > 18) {
            throw new DateTimeException("Zone offset hours not in valid range: value " + hours +
                    " is not in the range -18 to 18");
        }
        if (hours > 0) {
            if (minutes < 0 || seconds < 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be positive because hours is positive");
            }
        } else if (hours < 0) {
            if (minutes > 0 || seconds > 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be negative because hours is negative");
            }
        } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
            throw new DateTimeException("Zone offset minutes and seconds must have the same sign");
        }
        if (minutes < -59 || minutes > 59) {
            throw new DateTimeException("Zone offset minutes not in valid range: value " +
                    minutes + " is not in the range -59 to 59");
        }
        if (seconds < -59 || seconds > 59) {
            throw new DateTimeException("Zone offset seconds not in valid range: value " +
                    seconds + " is not in the range -59 to 59");
        }
        if (Math.abs(hours) == 18 && (minutes | seconds) != 0) {
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
    }


                /**
     * 计算总偏移量（以秒为单位）。
     *
     * @param hours  时区偏移量（以小时为单位），范围从 -18 到 +18
     * @param minutes  时区偏移量（以分钟为单位），范围从 0 到 ±59，符号与小时和秒相同
     * @param seconds  时区偏移量（以秒为单位），范围从 0 到 ±59，符号与小时和分钟相同
     * @return 总偏移量（以秒为单位）
     */
    private static int totalSeconds(int hours, int minutes, int seconds) {
        return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取一个指定总偏移量（以秒为单位）的 {@code ZoneOffset} 实例。
     * <p>
     * 偏移量必须在 {@code -18:00} 到 {@code +18:00} 范围内，对应于 -64800 到 +64800。
     *
     * @param totalSeconds  总时区偏移量（以秒为单位），范围从 -64800 到 +64800
     * @return ZoneOffset，不为空
     * @throws DateTimeException 如果偏移量不在所需范围内
     */
    public static ZoneOffset ofTotalSeconds(int totalSeconds) {
        if (totalSeconds < -MAX_SECONDS || totalSeconds > MAX_SECONDS) {
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
        if (totalSeconds % (15 * SECONDS_PER_MINUTE) == 0) {
            Integer totalSecs = totalSeconds;
            ZoneOffset result = SECONDS_CACHE.get(totalSecs);
            if (result == null) {
                result = new ZoneOffset(totalSeconds);
                SECONDS_CACHE.putIfAbsent(totalSecs, result);
                result = SECONDS_CACHE.get(totalSecs);
                ID_CACHE.putIfAbsent(result.getId(), result);
            }
            return result;
        } else {
            return new ZoneOffset(totalSeconds);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param totalSeconds  总时区偏移量（以秒为单位），范围从 -64800 到 +64800
     */
    private ZoneOffset(int totalSeconds) {
        super();
        this.totalSeconds = totalSeconds;
        id = buildId(totalSeconds);
    }

    private static String buildId(int totalSeconds) {
        if (totalSeconds == 0) {
            return "Z";
        } else {
            int absTotalSeconds = Math.abs(totalSeconds);
            StringBuilder buf = new StringBuilder();
            int absHours = absTotalSeconds / SECONDS_PER_HOUR;
            int absMinutes = (absTotalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
            buf.append(totalSeconds < 0 ? "-" : "+")
                .append(absHours < 10 ? "0" : "").append(absHours)
                .append(absMinutes < 10 ? ":0" : ":").append(absMinutes);
            int absSeconds = absTotalSeconds % SECONDS_PER_MINUTE;
            if (absSeconds != 0) {
                buf.append(absSeconds < 10 ? ":0" : ":").append(absSeconds);
            }
            return buf.toString();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取总时区偏移量（以秒为单位）。
     * <p>
     * 这是访问偏移量的主要方式。
     * 它返回小时、分钟和秒字段的总和，可以将其添加到时间中。
     *
     * @return 总时区偏移量（以秒为单位）
     */
    public int getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * 获取标准化的时区偏移量 ID。
     * <p>
     * 该 ID 是标准 ISO-8601 格式字符串的微小变体。
     * 有三种格式：
     * <ul>
     * <li>{@code Z} - 表示 UTC（ISO-8601）
     * <li>{@code +hh:mm} 或 {@code -hh:mm} - 如果秒为零（ISO-8601）
     * <li>{@code +hh:mm:ss} 或 {@code -hh:mm:ss} - 如果秒不为零（非 ISO-8601）
     * </ul>
     *
     * @return 时区偏移量 ID，不为空
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 获取关联的时区规则。
     * <p>
     * 规则在查询时将始终返回此偏移量。
     * 实现类是不可变的、线程安全的和可序列化的。
     *
     * @return 规则，不为空
     */
    @Override
    public ZoneRules getRules() {
        return ZoneRules.of(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这会检查此偏移量是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@code OFFSET_SECONDS} 字段返回 true。
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此偏移量上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == OFFSET_SECONDS;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此偏移量用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 受支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override  // 重写以提供 Javadoc
    public ValueRange range(TemporalField field) {
        return TemporalAccessor.super.range(field);
    }

    /**
     * 从此偏移量中获取指定字段的值（以 {@code int} 表示）。
     * <p>
     * 这会查询此偏移量以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@code OFFSET_SECONDS} 字段返回偏移量的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc 和性能
    public int get(TemporalField field) {
        if (field == OFFSET_SECONDS) {
            return totalSeconds;
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return range(field).checkValidIntValue(getLong(field), field);
    }

    /**
     * 从此偏移量中获取指定字段的值（以 {@code long} 表示）。
     * <p>
     * 这会查询此偏移量以获取指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@code OFFSET_SECONDS} 字段返回偏移量的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field == OFFSET_SECONDS) {
            return totalSeconds;
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略对象查询此偏移量。
     * <p>
     * 这会使用指定的查询策略对象查询此偏移量。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。
     * 请阅读查询的文档以了解此方法的结果。
     * <p>
     * 此方法的结果是通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
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
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return (R) this;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有与此对象相同的偏移量。
     * <p>
     * 这将返回与输入具有相同可观察类型的时态对象，但偏移量已更改为与此对象相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)}
     * 并传递 {@link ChronoField#OFFSET_SECONDS} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式更清晰，建议使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisOffset.adjustInto(temporal);
     *   temporal = temporal.with(thisOffset);
     * </pre>
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(OFFSET_SECONDS, totalSeconds);
    }

    //-----------------------------------------------------------------------
    /**
     * 按降序比较此偏移量与另一个偏移量。
     * <p>
     * 偏移量按其在一天中世界各地出现的顺序进行比较。
     * 因此，偏移量 {@code +10:00} 位于偏移量 {@code +09:00} 之前，依此类推，直到 {@code -18:00}。
     * <p>
     * 比较是“与 equals 一致的”，如 {@link Comparable} 所定义。
     *
     * @param other  要比较的其他偏移量，不为空
     * @return 比较值，负数表示小于，正数表示大于
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    @Override
    public int compareTo(ZoneOffset other) {
        // abs(totalSeconds) <= MAX_SECONDS，因此这里不会发生溢出
        return other.totalSeconds - totalSeconds;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此偏移量是否等于另一个偏移量。
     * <p>
     * 比较基于偏移量的秒数。
     * 这等同于按 ID 进行比较。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此偏移量等于其他偏移量，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof ZoneOffset) {
            return totalSeconds == ((ZoneOffset) obj).totalSeconds;
        }
        return false;
    }

    /**
     * 此偏移量的哈希码。
     *
     * @return 适当的哈希码
     */
    @Override
    public int hashCode() {
        return totalSeconds;
    }

    //-----------------------------------------------------------------------
    /**
     * 以标准化的 ID 输出此偏移量的字符串表示形式。
     *
     * @return 此偏移量的字符串表示形式，不为空
     */
    @Override
    public String toString() {
        return id;
    }

    // -----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(8);                  // 标识 ZoneOffset
     *  int offsetByte = totalSeconds % 900 == 0 ? totalSeconds / 900 : 127;
     *  out.writeByte(offsetByte);
     *  if (offsetByte == 127) {
     *      out.writeInt(totalSeconds);
     *  }
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZONE_OFFSET_TYPE, this);
    }


                /**
     * 防止恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    @Override
    void write(DataOutput out) throws IOException {
        out.writeByte(Ser.ZONE_OFFSET_TYPE);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        final int offsetSecs = totalSeconds;
        int offsetByte = offsetSecs % 900 == 0 ? offsetSecs / 900 : 127;  // 压缩到 -72 到 +72
        out.writeByte(offsetByte);
        if (offsetByte == 127) {
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readExternal(DataInput in) throws IOException {
        int offsetByte = in.readByte();
        return (offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * 900));
    }

}
