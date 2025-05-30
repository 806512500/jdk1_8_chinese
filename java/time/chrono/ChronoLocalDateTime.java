
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

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.NANOS;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import java.time.zone.ZoneRules;
import java.util.Comparator;
import java.util.Objects;

/**
 * 一个任意历法系统中的日期时间，不带时区，适用于高级全球化用例。
 * <p>
 * <b>大多数应用程序应声明方法签名、字段和变量为 {@link LocalDateTime}，而不是此接口。</b>
 * <p>
 * {@code ChronoLocalDateTime} 是本地日期时间的抽象表示，其中 {@code Chronology}（历法系统）是可插拔的。
 * 日期时间通过 {@link TemporalField} 表达的字段定义，其中大多数常见实现定义在 {@link ChronoField} 中。
 * 历法系统定义了日历系统的操作方式和标准字段的含义。
 *
 * <h3>何时使用此接口</h3>
 * API 的设计鼓励使用 {@code LocalDateTime} 而不是此接口，即使应用程序需要处理多个日历系统也是如此。
 * 这一设计的理由在 {@link ChronoLocalDate} 中有详细说明。
 * <p>
 * 在使用此接口之前，确保已阅读并理解 {@code ChronoLocalDate} 中的讨论。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可实例化的实现必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @param <D> 此日期时间的日期的具体类型
 * @since 1.8
 */
public interface ChronoLocalDateTime<D extends ChronoLocalDate>
        extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>> {

    /**
     * 获取一个比较器，该比较器按时间线顺序比较 {@code ChronoLocalDateTime}，忽略历法系统。
     * <p>
     * 该比较器与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期时间，而不比较历法系统。
     * 这允许不同日历系统中的日期基于日期时间在本地时间线上的位置进行比较。
     * 底层比较等同于比较纪元日和纳秒数。
     *
     * @return 比较时忽略历法系统的时间线顺序比较器
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    static Comparator<ChronoLocalDateTime<?>> timeLineOrder() {
        return AbstractChronology.DATE_TIME_ORDER;
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象中获取 {@code ChronoLocalDateTime} 的实例。
     * <p>
     * 此方法基于指定的时间对象获取本地日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，此工厂将其转换为 {@code ChronoLocalDateTime} 的实例。
     * <p>
     * 转换从时间对象中提取并组合历法系统和日期时间。
     * 行为等同于使用 {@link Chronology#localDateTime(TemporalAccessor)} 与提取的历法系统。
     * 实现允许对等效于相关对象的字段进行优化访问。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名，允许通过方法引用使用它，例如 {@code ChronoLocalDateTime::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 日期时间，不为空
     * @throws DateTimeException 如果无法转换为 {@code ChronoLocalDateTime}
     * @see Chronology#localDateTime(TemporalAccessor)
     */
    static ChronoLocalDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDateTime) {
            return (ChronoLocalDateTime<?>) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.localDateTime(temporal);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的历法系统。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法系统定义。
     *
     * @return 历法系统，不为空
     */
    default Chronology getChronology() {
        return toLocalDate().getChronology();
    }

    /**
     * 获取此日期时间的本地日期部分。
     * <p>
     * 这返回一个具有与此日期时间相同年、月和日的本地日期。
     *
     * @return 此日期时间的日期部分，不为空
     */
    D toLocalDate() ;

    /**
     * 获取此日期时间的本地时间部分。
     * <p>
     * 这返回一个具有与此日期时间相同小时、分钟、秒和纳秒的本地时间。
     *
     * @return 此日期时间的时间部分，不为空
     */
    LocalTime toLocalTime();

    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 此方法检查指定的字段是否可以在此日期时间上查询。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)} 方法将抛出异常。
     * <p>
     * 支持的字段集由历法系统定义，通常包括所有 {@code ChronoField} 日期和时间字段。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获得。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，为空返回 false
     * @return 如果字段可以查询则返回 true，否则返回 false
     */
    @Override
    boolean isSupported(TemporalField field);

    /**
     * 检查指定的单位是否受支持。
     * <p>
     * 此方法检查指定的单位是否可以添加到或从此日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 支持的单位集由历法系统定义，通常包括所有 {@code ChronoUnit} 单位，但不包括 {@code FOREVER}。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递来获得。
     * 单位是否受支持由单位决定。
     *
     * @param unit  要检查的单位，为空返回 false
     * @return 如果单位可以添加/减去则返回 true，否则返回 false
     */
    @Override
    default boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit != FOREVER;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    // 重写以实现协变返回类型
    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), Temporal.super.with(adjuster));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    ChronoLocalDateTime<D> with(TemporalField field, long newValue);

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDateTime<D> plus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), Temporal.super.plus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    ChronoLocalDateTime<D> plus(long amountToAdd, TemporalUnit unit);

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDateTime<D> minus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), Temporal.super.minus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略对象查询此日期时间。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期时间。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解此方法的结果。
     * <p>
     * 此方法的结果通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获得。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能为空（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        } else if (query == TemporalQueries.localTime()) {
            return (R) toLocalTime();
        } else if (query == TemporalQueries.chronology()) {
            return (R) getChronology();
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // 作为优化，内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象，使其具有与此对象相同的日期和时间。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的临时对象，但日期和时间已更改为与此对象相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 两次，
     * 传递 {@link ChronoField#EPOCH_DAY} 和 {@link ChronoField#NANO_OF_DAY} 作为字段。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#with(TemporalAdjuster)} 反转调用模式更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisLocalDateTime.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalDateTime);
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
    default Temporal adjustInto(Temporal temporal) {
        return temporal
                .with(EPOCH_DAY, toLocalDate().toEpochDay())
                .with(NANO_OF_DAY, toLocalTime().toNanoOfDay());
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
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的日期时间字符串，不为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    default String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间与时区结合以创建 {@code ChronoZonedDateTime}。
     * <p>
     * 这将返回一个由此日期时间在指定时区形成的 {@code ChronoZonedDateTime}。结果将尽可能匹配此日期时间。
     * 时区规则，如夏令时，意味着并非每个本地日期时间都适用于指定的时区，因此本地日期时间可能会被调整。
     * <p>
     * 本地日期时间解析为时间线上的单个瞬间。
     * 这是通过找到本地日期时间的有效偏移量（UTC/格林尼治）来实现的，该偏移量由时区ID的 {@link ZoneRules 规则} 定义。
     * <p>
     * 在大多数情况下，本地日期时间只有一个有效偏移量。
     * 在重叠的情况下，即时钟回拨时，有两个有效偏移量。
     * 此方法使用较早的偏移量，通常对应于“夏季”。
     * <p>
     * 在间隙的情况下，即时钟向前跳动时，没有有效的偏移量。
     * 相反，本地日期时间将根据间隙的长度调整为更晚的时间。
     * 对于典型的夏令时变化，本地日期时间将向后移动一个小时，进入通常对应于“夏季”的偏移量。
     * <p>
     * 要在重叠期间获取较晚的偏移量，请调用
     * {@link ChronoZonedDateTime#withLaterOffsetAtOverlap()} 该方法的结果。
     *
     * @param zone  要使用的时区，不为空
     * @return 由此日期时间形成的时间区日期时间，不为空
     */
    ChronoZonedDateTime<D> atZone(ZoneId zone);

    //-----------------------------------------------------------------------
    /**
     * 将此日期时间转换为 {@code Instant}。
     * <p>
     * 这将此本地日期时间和指定的偏移量结合以形成 {@code Instant}。
     * <p>
     * 默认实现从日期的纪元日和时间的秒数计算。
     *
     * @param offset  用于转换的偏移量，不为空
     * @return 代表同一瞬间的 {@code Instant}，不为空
     */
    default Instant toInstant(ZoneOffset offset) {
        return Instant.ofEpochSecond(toEpochSecond(offset), toLocalTime().getNano());
    }

    /**
     * 将此日期时间转换为自 1970-01-01T00:00:00Z 以来的秒数。
     * <p>
     * 这将此本地日期时间和指定的偏移量结合以计算纪元秒值，即自 1970-01-01T00:00:00Z 以来的秒数。
     * 在纪元之后的时间点为正数，之前的时间点为负数。
     * <p>
     * 默认实现从日期的纪元日和时间的秒数计算。
     *
     * @param offset  用于转换的偏移量，不为空
     * @return 自 1970-01-01T00:00:00Z 以来的秒数
     */
    default long toEpochSecond(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        long epochDay = toLocalDate().toEpochDay();
        long secs = epochDay * 86400 + toLocalTime().toSecondOfDay();
        secs -= offset.getTotalSeconds();
        return secs;
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此日期时间与另一个日期时间，包括编年。
     * <p>
     * 比较首先基于底层时间线日期时间，然后基于编年。
     * 它与 {@link Comparable} 中定义的“与equals一致”。
     * <p>
     * 例如，以下是比较器顺序：
     * <ol>
     * <li>{@code 2012-12-03T12:00 (ISO)}</li>
     * <li>{@code 2012-12-04T12:00 (ISO)}</li>
     * <li>{@code 2555-12-04T12:00 (ThaiBuddhist)}</li>
     * <li>{@code 2012-12-05T12:00 (ISO)}</li>
     * </ol>
     * #2 和 #3 代表时间线上的同一日期时间。
     * 当两个值代表同一日期时间时，比较编年ID以区分它们。
     * 这一步是为了使排序“与equals一致”。
     * <p>
     * 如果所有被比较的日期时间都在同一编年中，则不需要额外的编年阶段，仅使用本地日期时间。
     * <p>
     * 默认实现执行上述比较。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 比较值，小于0表示小于，大于0表示大于
     */
    @Override
    default int compareTo(ChronoLocalDateTime<?> other) {
        int cmp = toLocalDate().compareTo(other.toLocalDate());
        if (cmp == 0) {
            cmp = toLocalTime().compareTo(other.toLocalTime());
            if (cmp == 0) {
                cmp = getChronology().compareTo(other.getChronology());
            }
        }
        return cmp;
    }

    /**
     * 检查此日期时间是否在指定的日期时间之后，忽略编年。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期时间，而不比较编年。
     * 这允许不同日历系统中的日期基于时间线位置进行比较。
     * <p>
     * 默认实现基于纪元日和纳秒数进行比较。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果此日期时间在指定的日期时间之后，返回true
     */
    default boolean isAfter(ChronoLocalDateTime<?> other) {
        long thisEpDay = this.toLocalDate().toEpochDay();
        long otherEpDay = other.toLocalDate().toEpochDay();
        return thisEpDay > otherEpDay ||
            (thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() > other.toLocalTime().toNanoOfDay());
    }

    /**
     * 检查此日期时间是否在指定的日期时间之前，忽略编年。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期时间，而不比较编年。
     * 这允许不同日历系统中的日期基于时间线位置进行比较。
     * <p>
     * 默认实现基于纪元日和纳秒数进行比较。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果此日期时间在指定的日期时间之前，返回true
     */
    default boolean isBefore(ChronoLocalDateTime<?> other) {
        long thisEpDay = this.toLocalDate().toEpochDay();
        long otherEpDay = other.toLocalDate().toEpochDay();
        return thisEpDay < otherEpDay ||
            (thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() < other.toLocalTime().toNanoOfDay());
    }

    /**
     * 检查此日期时间是否等于指定的日期时间，忽略编年。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期和时间，而不比较编年。
     * 这允许不同日历系统中的日期时间基于时间线位置进行比较。
     * <p>
     * 默认实现基于纪元日和纳秒数进行比较。
     *
     * @param other  要比较的其他日期时间，不为空
     * @return 如果底层日期时间等于指定的日期时间，返回true
     */
    default boolean isEqual(ChronoLocalDateTime<?> other) {
        // 首先进行时间检查，这比计算纪元日更便宜。
        return this.toLocalTime().toNanoOfDay() == other.toLocalTime().toNanoOfDay() &&
               this.toLocalDate().toEpochDay() == other.toLocalDate().toEpochDay();
    }

    /**
     * 检查此日期时间是否等于另一个日期时间，包括编年。
     * <p>
     * 比较此日期时间与另一个日期时间，确保日期时间和编年相同。
     *
     * @param obj  要检查的对象，为空返回false
     * @return 如果此日期时间等于其他日期时间，返回true
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
     * 输出将包括完整的本地日期时间。
     *
     * @return 此日期时间的字符串表示，不为空
     */
    @Override
    String toString();

}
