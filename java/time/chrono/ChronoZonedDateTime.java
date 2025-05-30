
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
package java.time.chrono;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.NANOS;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.Comparator;
import java.util.Objects;

/**
 * 一个任意历法中的带时区的日期时间，旨在用于高级全球化用例。
 * <p>
 * <b>大多数应用程序应声明方法签名、字段和变量为 {@link ZonedDateTime}，而不是此接口。</b>
 * <p>
 * {@code ChronoZonedDateTime} 是带时区的偏移日期时间的抽象表示，
 * 其中 {@code Chronology}（或日历系统）是可插拔的。
 * 日期时间是通过 {@link TemporalField} 表达的字段定义的，其中大多数常见实现定义在 {@link ChronoField} 中。
 * 历法定义了日历系统如何运作以及标准字段的含义。
 *
 * <h3>何时使用此接口</h3>
 * API 的设计鼓励使用 {@code ZonedDateTime} 而不是此接口，即使应用程序需要处理多个日历系统也是如此。
 * 这一设计的理由在 {@link ChronoLocalDate} 中有详细说明。
 * <p>
 * 在使用此接口之前，请确保已经阅读并理解了 {@code ChronoLocalDate} 中的讨论。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @param <D> 此日期时间的日期的具体类型
 * @since 1.8
 */
public interface ChronoZonedDateTime<D extends ChronoLocalDate>
        extends Temporal, Comparable<ChronoZonedDateTime<?>> {

    /**
     * 获取一个比较器，该比较器按时间线顺序比较 {@code ChronoZonedDateTime}，忽略历法。
     * <p>
     * 该比较器与 {@link #compareTo} 中的比较不同，因为它仅比较底层的瞬间，而不比较历法。
     * 这允许基于瞬间时间线的位置比较不同日历系统的日期。
     * 底层的比较等同于比较纪元秒和纳秒。
     *
     * @return 一个比较器，按时间线顺序比较，忽略历法
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    static Comparator<ChronoZonedDateTime<?>> timeLineOrder() {
        return AbstractChronology.INSTANT_ORDER;
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象中获取 {@code ChronoZonedDateTime} 的实例。
     * <p>
     * 此方法基于指定的时间对象创建一个带时区的日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，此工厂将其转换为 {@code ChronoZonedDateTime} 的实例。
     * <p>
     * 转换从时间对象中提取并组合历法、日期、时间和时区。
     * 该行为等同于使用 {@link Chronology#zonedDateTime(TemporalAccessor)} 与提取的历法。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许通过方法引用使用它，例如 {@code ChronoZonedDateTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 日期时间，不为空
     * @throws DateTimeException 如果无法转换为 {@code ChronoZonedDateTime}
     * @see Chronology#zonedDateTime(TemporalAccessor)
     */
    static ChronoZonedDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoZonedDateTime) {
            return (ChronoZonedDateTime<?>) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.zonedDateTime(temporal);
    }

    //-----------------------------------------------------------------------
    @Override
    default ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == INSTANT_SECONDS || field == OFFSET_SECONDS) {
                return field.range();
            }
            return toLocalDateTime().range(field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    default int get(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS:
                    throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
                case OFFSET_SECONDS:
                    return getOffset().getTotalSeconds();
            }
            return toLocalDateTime().get(field);
        }
        return Temporal.super.get(field);
    }

    @Override
    default long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS: return toEpochSecond();
                case OFFSET_SECONDS: return getOffset().getTotalSeconds();
            }
            return toLocalDateTime().getLong(field);
        }
        return field.getFrom(this);
    }

    /**
     * 获取此日期时间的本地日期部分。
     * <p>
     * 这将返回一个具有与该日期时间相同的年、月和日的本地日期。
     *
     * @return 此日期时间的日期部分，不为空
     */
    default D toLocalDate() {
        return toLocalDateTime().toLocalDate();
    }

    /**
     * 获取此日期时间的本地时间部分。
     * <p>
     * 这将返回一个具有与该日期时间相同的小时、分钟、秒和纳秒的本地时间。
     *
     * @return 此日期时间的时间部分，不为空
     */
    default LocalTime toLocalTime() {
        return toLocalDateTime().toLocalTime();
    }

    /**
     * 获取此日期时间的本地日期时间部分。
     * <p>
     * 这将返回一个具有与该日期时间相同的年、月和日的本地日期。
     *
     * @return 此日期时间的本地日期时间部分，不为空
     */
    ChronoLocalDateTime<D> toLocalDateTime();

    /**
     * 获取此日期时间的历法。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法定义。
     *
     * @return 历法，不为空
     */
    default Chronology getChronology() {
        return toLocalDate().getChronology();
    }

    /**
     * 获取时区偏移，例如 '+01:00'。
     * <p>
     * 这是从 UTC/Greenwich 到本地日期时间的偏移。
     *
     * @return 时区偏移，不为空
     */
    ZoneOffset getOffset();

    /**
     * 获取时区 ID，例如 'Europe/Paris'。
     * <p>
     * 这返回用于确定时区规则的存储时区 ID。
     *
     * @return 时区 ID，不为空
     */
    ZoneId getZone();

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，将时区偏移更改为本地时间线重叠时的两个有效偏移中的较早者。
     * <p>
     * 仅当本地时间线重叠时，此方法才有任何效果，例如在秋季夏令时转换时。
     * 在这种情况下，本地日期时间有两个有效的偏移。调用此方法将返回一个带有较早偏移的带时区的日期时间。
     * <p>
     * 如果在不是重叠时调用此方法，则返回 {@code this}。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此日期时间的带有较早偏移的 {@code ChronoZonedDateTime}，不为空
     * @throws DateTimeException 如果找不到时区的规则
     * @throws DateTimeException 如果没有规则适用于此日期时间
     */
    ChronoZonedDateTime<D> withEarlierOffsetAtOverlap();

    /**
     * 返回一个副本，将时区偏移更改为本地时间线重叠时的两个有效偏移中的较晚者。
     * <p>
     * 仅当本地时间线重叠时，此方法才有任何效果，例如在秋季夏令时转换时。
     * 在这种情况下，本地日期时间有两个有效的偏移。调用此方法将返回一个带有较晚偏移的带时区的日期时间。
     * <p>
     * 如果在不是重叠时调用此方法，则返回 {@code this}。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此日期时间的带有较晚偏移的 {@code ChronoZonedDateTime}，不为空
     * @throws DateTimeException 如果找不到时区的规则
     * @throws DateTimeException 如果没有规则适用于此日期时间
     */
    ChronoZonedDateTime<D> withLaterOffsetAtOverlap();

    /**
     * 返回一个副本，将时区更改为不同的时区，尽可能保留本地日期时间。
     * <p>
     * 此方法更改时区并保留本地日期时间。
     * 仅当新的时区中本地日期时间无效时，本地日期时间才会更改。
     * <p>
     * 要更改时区并调整本地日期时间，请使用 {@link #withZoneSameInstant(ZoneId)}。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param zone  要更改的时区，不为空
     * @return 基于此日期时间的带有请求时区的 {@code ChronoZonedDateTime}，不为空
     */
    ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone);

    /**
     * 返回一个副本，将时区更改为不同的时区，保留瞬间。
     * <p>
     * 此方法更改时区并保留瞬间。
     * 这通常会导致本地日期时间的更改。
     * <p>
     * 此方法基于保留相同的瞬间，因此本地时间线中的间隙和重叠对结果没有影响。
     * <p>
     * 要更改偏移同时保持本地时间，请使用 {@link #withZoneSameLocal(ZoneId)}。
     *
     * @param zone  要更改的时区，不为空
     * @return 基于此日期时间的带有请求时区的 {@code ChronoZonedDateTime}，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone);

    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 此方法检查指定的字段是否可以在该日期时间上查询。
     * 如果为 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 受支持的字段集由历法定义，通常包括所有 {@code ChronoField} 字段。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段可以查询则为 true，否则为 false
     */
    @Override
    boolean isSupported(TemporalField field);


                /**
     * 检查指定的单位是否受支持。
     * <p>
     * 检查指定的单位是否可以添加到或从这个日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 支持的单位集由历法定义，通常包括所有 {@code ChronoUnit} 单位，但 {@code FOREVER} 除外。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递来获得的。
     * 单位是否受支持由该单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override
    default boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit != FOREVER;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    // 重写以获得协变返回类型
    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoZonedDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), Temporal.super.with(adjuster));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    ChronoZonedDateTime<D> with(TemporalField field, long newValue);

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoZonedDateTime<D> plus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), Temporal.super.plus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit);

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoZonedDateTime<D> minus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), Temporal.super.minus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoZonedDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略对象查询此日期时间。
     * <p>
     * 使用指定的查询策略对象查询此日期时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解
     * 此方法的结果将是什么。
     * <p>
     * 该方法的结果是通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不允许为 null
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) {
            return (R) getZone();
        } else if (query == TemporalQueries.offset()) {
            return (R) getOffset();
        } else if (query == TemporalQueries.localTime()) {
            return (R) toLocalTime();
        } else if (query == TemporalQueries.chronology()) {
            return (R) getChronology();
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // 作为优化内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    /**
     * 使用指定的格式化器格式化此日期时间。
     * <p>
     * 此日期时间将传递给格式化器以生成字符串。
     * <p>
     * 默认实现必须如下所示：
     * <pre>
     *  return formatter.format(this);
     * </pre>
     *
     * @param formatter  要使用的格式化器，不允许为 null
     * @return 格式化的日期时间字符串，不允许为 null
     * @throws DateTimeException 如果打印时发生错误
     */
    default String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间转换为 {@code Instant}。
     * <p>
     * 返回一个表示与该日期时间相同时间点的 {@code Instant}。计算结合了
     * {@linkplain #toLocalDateTime() 本地日期时间} 和
     * {@linkplain #getOffset() 偏移量}。
     *
     * @return 表示相同时间点的 {@code Instant}，不允许为 null
     */
    default Instant toInstant() {
        return Instant.ofEpochSecond(toEpochSecond(), toLocalTime().getNano());
    }

    /**
     * 将此日期时间转换为从 1970-01-01T00:00:00Z 开始的秒数。
     * <p>
     * 使用 {@linkplain #toLocalDateTime() 本地日期时间} 和
     * {@linkplain #getOffset() 偏移量} 计算秒数，这是从 1970-01-01T00:00:00Z 开始的经过的秒数。
     * 在时间线上的瞬时点在 1970-01-01T00:00:00Z 之后的为正数，之前的为负数。
     *
     * @return 从 1970-01-01T00:00:00Z 开始的秒数
     */
    default long toEpochSecond() {
        long epochDay = toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + toLocalTime().toSecondOfDay();
        secs -= getOffset().getTotalSeconds();
        return secs;
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此日期时间与另一个日期时间，包括历法。
     * <p>
     * 比较基于瞬时，然后是本地日期时间，然后是时区 ID，然后是历法。
     * 它是“与 equals 一致”的，如 {@link Comparable} 所定义。
     * <p>
     * 如果所有被比较的日期时间对象都在同一历法中，则不需要额外的历法阶段。
     * <p>
     * 此默认实现执行上述定义的比较。
     *
     * @param other  要比较的其他日期时间，不允许为 null
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     */
    @Override
    default int compareTo(ChronoZonedDateTime<?> other) {
        int cmp = Long.compare(toEpochSecond(), other.toEpochSecond());
        if (cmp == 0) {
            cmp = toLocalTime().getNano() - other.toLocalTime().getNano();
            if (cmp == 0) {
                cmp = toLocalDateTime().compareTo(other.toLocalDateTime());
                if (cmp == 0) {
                    cmp = getZone().getId().compareTo(other.getZone().getId());
                    if (cmp == 0) {
                        cmp = getChronology().compareTo(other.getChronology());
                    }
                }
            }
        }
        return cmp;
    }

    /**
     * 检查此日期时间的瞬时是否在指定的日期时间之前。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它只比较日期时间的瞬时。
     * 这相当于使用 {@code dateTime1.toInstant().isBefore(dateTime2.toInstant());}。
     * <p>
     * 此默认实现基于纪元秒和纳秒进行比较。
     *
     * @param other  要比较的其他日期时间，不允许为 null
     * @return 如果此时间点在指定的日期时间之前，则返回 true
     */
    default boolean isBefore(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec < otherEpochSec ||
            (thisEpochSec == otherEpochSec && toLocalTime().getNano() < other.toLocalTime().getNano());
    }

    /**
     * 检查此日期时间的瞬时是否在指定的日期时间之后。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它只比较日期时间的瞬时。
     * 这相当于使用 {@code dateTime1.toInstant().isAfter(dateTime2.toInstant());}。
     * <p>
     * 此默认实现基于纪元秒和纳秒进行比较。
     *
     * @param other  要比较的其他日期时间，不允许为 null
     * @return 如果此时间点在指定的日期时间之后，则返回 true
     */
    default boolean isAfter(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec > otherEpochSec ||
            (thisEpochSec == otherEpochSec && toLocalTime().getNano() > other.toLocalTime().getNano());
    }

    /**
     * 检查此日期时间的瞬时是否等于指定的日期时间的瞬时。
     * <p>
     * 此方法与 {@link #compareTo} 和 {@link #equals} 中的比较不同，因为它只比较日期时间的瞬时。
     * 这相当于使用 {@code dateTime1.toInstant().equals(dateTime2.toInstant());}。
     * <p>
     * 此默认实现基于纪元秒和纳秒进行比较。
     *
     * @param other  要比较的其他日期时间，不允许为 null
     * @return 如果瞬时等于指定的日期时间的瞬时，则返回 true
     */
    default boolean isEqual(ChronoZonedDateTime<?> other) {
        return toEpochSecond() == other.toEpochSecond() &&
                toLocalTime().getNano() == other.toLocalTime().getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期时间是否等于另一个日期时间。
     * <p>
     * 比较基于带偏移的日期时间和时区。
     * 要比较时间线上的相同瞬时，使用 {@link #compareTo}。
     * 只比较 {@code ChronoZonedDateTime} 类型的对象，其他类型返回 false。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期时间等于其他日期时间，则返回 true
     */
    @Override
    boolean equals(Object obj);

    /**
     * 此日期时间的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间输出为 {@code String}。
     * <p>
     * 输出将包括完整的带时区的日期时间。
     *
     * @return 此日期时间的字符串表示，不允许为 null
     */
    @Override
    String toString();

}
