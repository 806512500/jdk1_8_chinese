
/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
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
 * 从格林尼治/UTC的时间偏移量，例如 {@code +02:00}。
 * <p>
 * 时间偏移量是从格林尼治/UTC的时间区的差异量。这通常是一个固定的小时和分钟数。
 * <p>
 * 世界不同地区有不同的时间偏移量。
 * 时间偏移量如何随地点和一年中的时间变化的规则在 {@link ZoneId} 类中被捕获。
 * <p>
 * 例如，巴黎在冬季比格林尼治/UTC快一小时，夏季快两小时。
 * 巴黎的 {@code ZoneId} 实例将引用两个 {@code ZoneOffset} 实例 - 一个冬季的 {@code +01:00} 实例，
 * 和一个夏季的 {@code +02:00} 实例。
 * <p>
 * 2008年，世界各地的时间偏移量从 -12:00 到 +14:00 不等。
 * 为了防止该范围的扩展引起问题，同时仍提供验证，偏移量的范围被限制在 -18:00 到 18:00 之间（包括两端）。
 * <p>
 * 本类设计用于 ISO 日历系统。
 * 小时、分钟和秒的字段假设符合 ISO 标准定义。如果其他日历系统的时间字段定义与 ISO 日历系统匹配，也可以使用本类。
 * <p>
 * {@code ZoneOffset} 的实例必须使用 {@link #equals} 进行比较。
 * 实现可以选择缓存某些常见的偏移量，但应用程序不应依赖此类缓存。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code ZoneOffset} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})，身份哈希码，或同步）可能产生不可预测的结果，应避免。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 本类是不可变和线程安全的。
 *
 * @since 1.8
 */
public final class ZoneOffset
        extends ZoneId
        implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {

    /** 按秒数缓存的时间偏移量。 */
    private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap<>(16, 0.75f, 4);
    /** 按ID缓存的时间偏移量。 */
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
     * UTC 的时间偏移量，ID 为 'Z'。
     */
    public static final ZoneOffset UTC = ZoneOffset.ofTotalSeconds(0);
    /**
     * 支持的最小偏移量常量。
     */
    public static final ZoneOffset MIN = ZoneOffset.ofTotalSeconds(-MAX_SECONDS);
    /**
     * 支持的最大偏移量常量。
     */
    public static final ZoneOffset MAX = ZoneOffset.ofTotalSeconds(MAX_SECONDS);

    /**
     * 总偏移量（以秒为单位）。
     */
    private final int totalSeconds;
    /**
     * 时间偏移量的字符串形式。
     */
    private final transient String id;

    //-----------------------------------------------------------------------
    /**
     * 使用ID获取 {@code ZoneOffset} 的实例。
     * <p>
     * 该方法解析 {@code ZoneOffset} 的字符串ID以返回一个实例。解析接受 {@link #getId()} 生成的所有格式，
     * 以及一些额外的格式：
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
     * 返回的偏移量ID将被规范化为 {@link #getId()} 描述的格式之一。
     * <p>
     * 支持的最大范围是从 +18:00 到 -18:00（包括两端）。
     *
     * @param offsetId  偏移量ID，不为空
     * @return 时间偏移量，不为空
     * @throws DateTimeException 如果偏移量ID无效
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
                throw new DateTimeException("无效的 ZoneOffset ID，格式不正确: " + offsetId);
        }
        char first = offsetId.charAt(0);
        if (first != '+' && first != '-') {
            throw new DateTimeException("无效的 ZoneOffset ID，未找到预期的加号或减号: " + offsetId);
        }
        if (first == '-') {
            return ofHoursMinutesSeconds(-hours, -minutes, -seconds);
        } else {
            return ofHoursMinutesSeconds(hours, minutes, seconds);
        }
    }

    /**
     * 解析一个两位数的零前缀数字。
     *
     * @param offsetId  偏移 ID，不为空
     * @param pos  要解析的位置，有效
     * @param precededByColon  该数字是否应以冒号前缀
     * @return 解析的数字，从 0 到 99
     */
    private static int parseNumber(CharSequence offsetId, int pos, boolean precededByColon) {
        if (precededByColon && offsetId.charAt(pos - 1) != ':') {
            throw new DateTimeException("无效的 ZoneOffset ID，未找到预期的冒号: " + offsetId);
        }
        char ch1 = offsetId.charAt(pos);
        char ch2 = offsetId.charAt(pos + 1);
        if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
            throw new DateTimeException("无效的 ZoneOffset ID，找到非数字字符: " + offsetId);
        }
        return (ch1 - 48) * 10 + (ch2 - 48);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用小时偏移量获取 {@code ZoneOffset} 的实例。
     *
     * @param hours  时区偏移量，从 -18 到 +18
     * @return 时区偏移量，不为空
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
     * @param hours  时区偏移量，从 -18 到 +18
     * @param minutes  时区偏移量，从 0 到 ±59，符号与小时匹配
     * @return 时区偏移量，不为空
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
     * @param hours  时区偏移量，从 -18 到 +18
     * @param minutes  时区偏移量，从 0 到 ±59，符号与小时和秒匹配
     * @param seconds  时区偏移量，从 0 到 ±59，符号与小时和分钟匹配
     * @return 时区偏移量，不为空
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
     * 根据指定的时间对象获取偏移量。
     * {@code TemporalAccessor} 表示任意一组日期和时间信息，此工厂将其转换为 {@code ZoneOffset} 的实例。
     * <p>
     * {@code TemporalAccessor} 表示某种形式的日期和时间信息。
     * 此工厂将任意时间对象转换为 {@code ZoneOffset} 的实例。
     * <p>
     * 转换使用 {@link TemporalQueries#offset()} 查询，该查询依赖于提取 {@link ChronoField#OFFSET_SECONDS OFFSET_SECONDS} 字段。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许其通过方法引用作为查询使用，例如 {@code ZoneOffset::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 时区偏移量，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code ZoneOffset}
     */
    public static ZoneOffset from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        ZoneOffset offset = temporal.query(TemporalQueries.offset());
        if (offset == null) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 ZoneOffset: " +
                    temporal + " 类型为 " + temporal.getClass().getName());
        }
        return offset;
    }

                //-----------------------------------------------------------------------
    /**
     * 验证偏移字段。
     *
     * @param hours  时区偏移量，以小时为单位，范围为 -18 到 +18
     * @param minutes  时区偏移量，以分钟为单位，范围为 0 到 ±59
     * @param seconds  时区偏移量，以秒为单位，范围为 0 到 ±59
     * @throws DateTimeException 如果偏移量不在要求的范围内
     */
    private static void validate(int hours, int minutes, int seconds) {
        if (hours < -18 || hours > 18) {
            throw new DateTimeException("时区偏移小时数不在有效范围内: 值 " + hours +
                    " 不在 -18 到 18 的范围内");
        }
        if (hours > 0) {
            if (minutes < 0 || seconds < 0) {
                throw new DateTimeException("时区偏移分钟数和秒数必须为正数，因为小时数为正数");
            }
        } else if (hours < 0) {
            if (minutes > 0 || seconds > 0) {
                throw new DateTimeException("时区偏移分钟数和秒数必须为负数，因为小时数为负数");
            }
        } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
            throw new DateTimeException("时区偏移分钟数和秒数必须具有相同的符号");
        }
        if (minutes < -59 || minutes > 59) {
            throw new DateTimeException("时区偏移分钟数不在有效范围内: 值 " +
                    minutes + " 不在 -59 到 59 的范围内");
        }
        if (seconds < -59 || seconds > 59) {
            throw new DateTimeException("时区偏移秒数不在有效范围内: 值 " +
                    seconds + " 不在 -59 到 59 的范围内");
        }
        if (Math.abs(hours) == 18 && (minutes | seconds) != 0) {
            throw new DateTimeException("时区偏移不在有效范围内: -18:00 到 +18:00");
        }
    }

    /**
     * 计算总偏移量（以秒为单位）。
     *
     * @param hours  时区偏移量，以小时为单位，范围为 -18 到 +18
     * @param minutes  时区偏移量，以分钟为单位，范围为 0 到 ±59，符号与小时数和秒数一致
     * @param seconds  时区偏移量，以秒为单位，范围为 0 到 ±59，符号与小时数和分钟数一致
     * @return 总偏移量（以秒为单位）
     */
    private static int totalSeconds(int hours, int minutes, int seconds) {
        return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定总偏移量（以秒为单位）的 {@code ZoneOffset} 实例。
     * <p>
     * 偏移量必须在 {@code -18:00} 到 {@code +18:00} 范围内，即 -64800 到 +64800。
     *
     * @param totalSeconds  总时区偏移量（以秒为单位），范围为 -64800 到 +64800
     * @return ZoneOffset 实例，不为空
     * @throws DateTimeException 如果偏移量不在要求的范围内
     */
    public static ZoneOffset ofTotalSeconds(int totalSeconds) {
        if (totalSeconds < -MAX_SECONDS || totalSeconds > MAX_SECONDS) {
            throw new DateTimeException("时区偏移不在有效范围内: -18:00 到 +18:00");
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
     * @param totalSeconds  总时区偏移量（以秒为单位），范围为 -64800 到 +64800
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
     * 它返回小时、分钟和秒字段的总和，作为可以添加到时间的单个偏移量。
     *
     * @return 总时区偏移量（以秒为单位）
     */
    public int getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * 获取标准化的时区偏移 ID。
     * <p>
     * ID 是标准 ISO-8601 格式字符串的轻微变体。
     * 有三种格式：
     * <ul>
     * <li>{@code Z} - 用于 UTC（ISO-8601）
     * <li>{@code +hh:mm} 或 {@code -hh:mm} - 如果秒数为零（ISO-8601）
     * <li>{@code +hh:mm:ss} 或 {@code -hh:mm:ss} - 如果秒数非零（非 ISO-8601）
     * </ul>
     *
     * @return 时区偏移 ID，不为空
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
     * 检查此偏移量是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code OFFSET_SECONDS} 字段返回 true。
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。
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
     * 此偏移量用于提高返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 受支持的字段} 将返回
     * 适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。
     * 是否可以获取范围由字段决定。
     *
     * @param field  要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override  // 重写以提供 Javadoc
    public ValueRange range(TemporalField field) {
        return TemporalAccessor.super.range(field);
    }

    /**
     * 从这个偏移量中以 {@code int} 形式获取指定字段的值。
     * <p>
     * 此查询此偏移量以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code OFFSET_SECONDS} 字段返回偏移量的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。是否可以获取值，
     * 以及值代表什么，由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或
     *         字段的值超出有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或
     *         值的范围超出 {@code int}
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
     * 从这个偏移量中以 {@code long} 形式获取指定字段的值。
     * <p>
     * 此查询此偏移量以获取指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code OFFSET_SECONDS} 字段返回偏移量的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。是否可以获取值，
     * 以及值代表什么，由字段决定。
     *
     * @param field  要获取的字段，不为 null
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
     * 使用指定的查询策略对象查询此偏移量。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解
     * 此方法的结果将是什么。
     * <p>
     * 通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并
     * 将 {@code this} 作为参数传递来获取此方法的结果。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为 null
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
     * 将指定的时间对象调整为与该对象具有相同的偏移量。
     * <p>
     * 这将返回一个与输入具有相同可观察类型的对象，但偏移量已更改为与该对象相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)}
     * 并将 {@link ChronoField#OFFSET_SECONDS} 作为字段传递。
     * <p>
     * 在大多数情况下，通过使用
     * {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisOffset.adjustInto(temporal);
     *   temporal = temporal.with(thisOffset);
     * </pre>
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不允许为 null
     * @return 调整后的对象，不允许为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(OFFSET_SECONDS, totalSeconds);
    }

    //-----------------------------------------------------------------------
    /**
     * 按降序比较此偏移量与其他偏移量。
     * <p>
     * 偏移量按照一天中全球相同时间发生的顺序进行比较。因此，偏移量 {@code +10:00} 在
     * 偏移量 {@code +09:00} 之前，依此类推，直到 {@code -18:00}。
     * <p>
     * 比较是“与 equals 一致的”，如 {@link Comparable} 所定义。
     *
     * @param other  要比较的另一个日期，不允许为 null
     * @return 比较值，小于 0 表示较小，大于 0 表示较大
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
     * @return 如果此偏移量等于另一个偏移量，则返回 true
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
     * 为此偏移量生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return totalSeconds;
    }

    //-----------------------------------------------------------------------
    /**
     * 输出此偏移量的 {@code String} 表示形式，使用规范化 ID。
     *
     * @return 此偏移量的字符串表示形式，不允许为 null
     */
    @Override
    public String toString() {
        return id;
    }

    // -----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(8);                  // 标识一个 ZoneOffset
     *  int offsetByte = totalSeconds % 900 == 0 ? totalSeconds / 900 : 127;
     *  out.writeByte(offsetByte);
     *  if (offsetByte == 127) {
     *      out.writeInt(totalSeconds);
     *  }
     * </pre>
     *
     * @return {@code Ser} 的实例，不允许为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.ZONE_OFFSET_TYPE, this);
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
