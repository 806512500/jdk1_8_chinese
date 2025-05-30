
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
package java.time.temporal;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;

/**
 * {@code TemporalQuery} 的通用实现。
 * <p>
 * 本类提供了 {@link TemporalQuery} 的通用实现。
 * 这些实现定义在这里是因为它们必须是常量，而 lambda 表达式的定义不能保证这一点。
 * 通过在这里一次性赋值，它们成为“正常”的 Java 常量。
 * <p>
 * 查询是提取时间对象信息的关键工具。
 * 它们存在是为了外部化查询过程，允许不同的方法，就像策略设计模式一样。
 * 例如，一个查询可能检查日期是否是闰年的 2 月 29 日前一天，或者计算到你下一个生日的天数。
 * <p>
 * {@link TemporalField} 接口提供了另一种查询时间对象的机制。
 * 该接口的限制是只能返回 {@code long}。
 * 相比之下，查询可以返回任何类型。
 * <p>
 * 使用 {@code TemporalQuery} 有两种等效的方法。
 * 第一种是直接调用此接口上的方法。
 * 第二种是使用 {@link TemporalAccessor#query(TemporalQuery)}：
 * <pre>
 *   // 这两行是等效的，但推荐使用第二种方法
 *   temporal = thisQuery.queryFrom(temporal);
 *   temporal = temporal.query(thisQuery);
 * </pre>
 * 推荐使用第二种方法，{@code query(TemporalQuery)}，因为代码中阅读起来更清晰。
 * <p>
 * 最常见的实现是方法引用，例如 {@code LocalDate::from} 和 {@code ZoneId::from}。
 * 还提供了其他常见的查询，用于返回：
 * <ul>
 * <li> 一个时间体系，
 * <li> 一个本地日期，
 * <li> 一个本地时间，
 * <li> 一个时区偏移，
 * <li> 一个精度，
 * <li> 一个时区，或
 * <li> 一个时区 ID。
 * </ul>
 *
 * @since 1.8
 */
public final class TemporalQueries {
    // 注意每个方法都必须提供一个常量，而不是计算值，因为它们将使用 == 进行检查
    // 还必须确保每个常量都是不同的（由于 == 检查）
    // 因此，对这段代码的修改必须谨慎进行

    /**
     * 私有构造函数，因为这是一个工具类。
     */
    private TemporalQueries() {
    }

    //-----------------------------------------------------------------------
    // 特殊常量用于从 TemporalAccessor 提取无法通过其他方式派生的信息
    // 在这里添加了 Javadoc，以便让它们看起来比实际更正常

    /**
     * 严格的 {@code ZoneId} 查询。
     * <p>
     * 此查询从 {@code TemporalAccessor} 查询时区。
     * 只有当日期时间概念上包含 {@code ZoneId} 时，才会返回时区。
     * 如果日期时间概念上仅包含 {@code ZoneOffset}，则不会返回时区。
     * 因此，{@link java.time.ZonedDateTime} 将返回 {@code getZone()} 的结果，
     * 而 {@link java.time.OffsetDateTime} 将返回 null。
     * <p>
     * 在大多数情况下，应用程序应使用 {@link #zone()}，因为此查询过于严格。
     * <p>
     * 实现 {@code TemporalAccessor} 的 JDK 类的结果如下：<br>
     * {@code LocalDate} 返回 null<br>
     * {@code LocalTime} 返回 null<br>
     * {@code LocalDateTime} 返回 null<br>
     * {@code ZonedDateTime} 返回关联的时区<br>
     * {@code OffsetTime} 返回 null<br>
     * {@code OffsetDateTime} 返回 null<br>
     * {@code ChronoLocalDate} 返回 null<br>
     * {@code ChronoLocalDateTime} 返回 null<br>
     * {@code ChronoZonedDateTime} 返回关联的时区<br>
     * {@code Era} 返回 null<br>
     * {@code DayOfWeek} 返回 null<br>
     * {@code Month} 返回 null<br>
     * {@code Year} 返回 null<br>
     * {@code YearMonth} 返回 null<br>
     * {@code MonthDay} 返回 null<br>
     * {@code ZoneOffset} 返回 null<br>
     * {@code Instant} 返回 null<br>
     *
     * @return 可以获取时间对象时区 ID 的查询，不为空
     */
    public static TemporalQuery<ZoneId> zoneId() {
        return TemporalQueries.ZONE_ID;
    }

    /**
     * 用于查询 {@code Chronology} 的查询。
     * <p>
     * 此查询从 {@code TemporalAccessor} 查询时间体系。
     * 如果目标 {@code TemporalAccessor} 表示日期或日期的一部分，
     * 则应返回该日期表示的时间体系。
     * 由于此定义，仅表示时间的对象，如 {@code LocalTime}，将返回 null。
     * <p>
     * 实现 {@code TemporalAccessor} 的 JDK 类的结果如下：<br>
     * {@code LocalDate} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code LocalTime} 返回 null（不表示日期）<br>
     * {@code LocalDateTime} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code ZonedDateTime} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code OffsetTime} 返回 null（不表示日期）<br>
     * {@code OffsetDateTime} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code ChronoLocalDate} 返回关联的时间体系<br>
     * {@code ChronoLocalDateTime} 返回关联的时间体系<br>
     * {@code ChronoZonedDateTime} 返回关联的时间体系<br>
     * {@code Era} 返回关联的时间体系<br>
     * {@code DayOfWeek} 返回 null（跨时间体系共享）<br>
     * {@code Month} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code Year} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code YearMonth} 返回 {@code IsoChronology.INSTANCE}<br>
     * {@code MonthDay} 返回 null {@code IsoChronology.INSTANCE}<br>
     * {@code ZoneOffset} 返回 null（不表示日期）<br>
     * {@code Instant} 返回 null（不表示日期）<br>
     * <p>
     * 可以通过方法引用 {@code Chronology::from} 将 {@link java.time.chrono.Chronology#from(TemporalAccessor)} 用作 {@code TemporalQuery}。
     * 该方法与此查询等效，只是如果无法获取时间体系，则会抛出异常。
     *
     * @return 可以获取时间对象时间体系的查询，不为空
     */
    public static TemporalQuery<Chronology> chronology() {
        return TemporalQueries.CHRONO;
    }

    /**
     * 用于查询最小支持单位的查询。
     * <p>
     * 此查询从 {@code TemporalAccessor} 查询时间精度。
     * 如果目标 {@code TemporalAccessor} 表示一致或完整的日期时间、日期或时间，
     * 则必须返回实际支持的最小精度。
     * 注意，如 {@code NANO_OF_DAY} 和 {@code NANO_OF_SECOND} 等字段定义为始终返回，忽略精度，
     * 因此这是查找实际最小支持单位的唯一方法。
     * 例如，如果 {@code GregorianCalendar} 实现了 {@code TemporalAccessor}，则会返回 {@code MILLIS} 的精度。
     * <p>
     * 实现 {@code TemporalAccessor} 的 JDK 类的结果如下：<br>
     * {@code LocalDate} 返回 {@code DAYS}<br>
     * {@code LocalTime} 返回 {@code NANOS}<br>
     * {@code LocalDateTime} 返回 {@code NANOS}<br>
     * {@code ZonedDateTime} 返回 {@code NANOS}<br>
     * {@code OffsetTime} 返回 {@code NANOS}<br>
     * {@code OffsetDateTime} 返回 {@code NANOS}<br>
     * {@code ChronoLocalDate} 返回 {@code DAYS}<br>
     * {@code ChronoLocalDateTime} 返回 {@code NANOS}<br>
     * {@code ChronoZonedDateTime} 返回 {@code NANOS}<br>
     * {@code Era} 返回 {@code ERAS}<br>
     * {@code DayOfWeek} 返回 {@code DAYS}<br>
     * {@code Month} 返回 {@code MONTHS}<br>
     * {@code Year} 返回 {@code YEARS}<br>
     * {@code YearMonth} 返回 {@code MONTHS}<br>
     * {@code MonthDay} 返回 null（不表示完整的日期或时间）<br>
     * {@code ZoneOffset} 返回 null（不表示日期或时间）<br>
     * {@code Instant} 返回 {@code NANOS}<br>
     *
     * @return 可以获取时间对象精度的查询，不为空
     */
    public static TemporalQuery<TemporalUnit> precision() {
        return TemporalQueries.PRECISION;
    }

    //-----------------------------------------------------------------------
    // 非特殊常量是派生信息的标准查询
    /**
     * 宽松的 {@code ZoneId} 查询，回退到 {@code ZoneOffset}。
     * <p>
     * 此查询从 {@code TemporalAccessor} 查询时区。
     * 它首先尝试使用 {@link #zoneId()} 获取时区。
     * 如果未找到时区，则尝试获取 {@link #offset()}。
     * 因此，{@link java.time.ZonedDateTime} 将返回 {@code getZone()} 的结果，
     * 而 {@link java.time.OffsetDateTime} 将返回 {@code getOffset()} 的结果。
     * <p>
     * 在大多数情况下，应用程序应使用此查询而不是 {@code #zoneId()}。
     * <p>
     * 可以通过方法引用 {@code ZoneId::from} 将 {@link ZoneId#from(TemporalAccessor)} 用作 {@code TemporalQuery}。
     * 该方法与此查询等效，只是如果无法获取时区，则会抛出异常。
     *
     * @return 可以获取时间对象时区 ID 或偏移的查询，不为空
     */
    public static TemporalQuery<ZoneId> zone() {
        return TemporalQueries.ZONE;
    }

    /**
     * 用于查询 {@code ZoneOffset} 的查询，如果未找到则返回 null。
     * <p>
     * 此查询返回一个可以用于查询时间对象偏移的 {@code TemporalQuery}。
     * 如果时间对象无法提供偏移，则查询将返回 null。
     * <p>
     * 查询实现检查 {@link ChronoField#OFFSET_SECONDS OFFSET_SECONDS} 字段并使用它创建一个 {@code ZoneOffset}。
     * <p>
     * 可以通过方法引用 {@code ZoneOffset::from} 将 {@link java.time.ZoneOffset#from(TemporalAccessor)} 用作 {@code TemporalQuery}。
     * 如果时间对象包含偏移，则此查询和 {@code ZoneOffset::from} 将返回相同的结果。
     * 如果时间对象不包含偏移，则方法引用将抛出异常，而此查询将返回 null。
     *
     * @return 可以获取时间对象偏移的查询，不为空
     */
    public static TemporalQuery<ZoneOffset> offset() {
        return TemporalQueries.OFFSET;
    }

    /**
     * 用于查询 {@code LocalDate} 的查询，如果未找到则返回 null。
     * <p>
     * 此查询返回一个可以用于查询时间对象本地日期的 {@code TemporalQuery}。
     * 如果时间对象无法提供本地日期，则查询将返回 null。
     * <p>
     * 查询实现检查 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 字段并使用它创建一个 {@code LocalDate}。
     * <p>
     * 可以通过方法引用 {@code LocalDate::from} 将 {@link ZoneOffset#from(TemporalAccessor)} 用作 {@code TemporalQuery}。
     * 如果时间对象包含日期，则此查询和 {@code LocalDate::from} 将返回相同的结果。
     * 如果时间对象不包含日期，则方法引用将抛出异常，而此查询将返回 null。
     *
     * @return 可以获取时间对象日期的查询，不为空
     */
    public static TemporalQuery<LocalDate> localDate() {
        return TemporalQueries.LOCAL_DATE;
    }

    /**
     * 用于查询 {@code LocalTime} 的查询，如果未找到则返回 null。
     * <p>
     * 此查询返回一个可以用于查询时间对象本地时间的 {@code TemporalQuery}。
     * 如果时间对象无法提供本地时间，则查询将返回 null。
     * <p>
     * 查询实现检查 {@link ChronoField#NANO_OF_DAY NANO_OF_DAY} 字段并使用它创建一个 {@code LocalTime}。
     * <p>
     * 可以通过方法引用 {@code LocalTime::from} 将 {@link ZoneOffset#from(TemporalAccessor)} 用作 {@code TemporalQuery}。
     * 如果时间对象包含时间，则此查询和 {@code LocalTime::from} 将返回相同的结果。
     * 如果时间对象不包含时间，则方法引用将抛出异常，而此查询将返回 null。
     *
     * @return 可以获取时间对象时间的查询，不为空
     */
    public static TemporalQuery<LocalTime> localTime() {
        return TemporalQueries.LOCAL_TIME;
    }


                //-----------------------------------------------------------------------
    /**
     * 一个严格查询 {@code ZoneId}。
     */
    static final TemporalQuery<ZoneId> ZONE_ID = (temporal) ->
        temporal.query(TemporalQueries.ZONE_ID);

    /**
     * 一个查询 {@code Chronology}。
     */
    static final TemporalQuery<Chronology> CHRONO = (temporal) ->
        temporal.query(TemporalQueries.CHRONO);

    /**
     * 一个查询最小支持单位。
     */
    static final TemporalQuery<TemporalUnit> PRECISION = (temporal) ->
        temporal.query(TemporalQueries.PRECISION);

    //-----------------------------------------------------------------------
    /**
     * 一个查询 {@code ZoneOffset}，如果未找到则返回 null。
     */
    static final TemporalQuery<ZoneOffset> OFFSET = (temporal) -> {
        if (temporal.isSupported(OFFSET_SECONDS)) {
            return ZoneOffset.ofTotalSeconds(temporal.get(OFFSET_SECONDS));
        }
        return null;
    };

    /**
     * 一个宽松查询 {@code ZoneId}，如果未找到则回退到 {@code ZoneOffset}。
     */
    static final TemporalQuery<ZoneId> ZONE = (temporal) -> {
        ZoneId zone = temporal.query(ZONE_ID);
        return (zone != null ? zone : temporal.query(OFFSET));
    };

    /**
     * 一个查询 {@code LocalDate}，如果未找到则返回 null。
     */
    static final TemporalQuery<LocalDate> LOCAL_DATE = (temporal) -> {
        if (temporal.isSupported(EPOCH_DAY)) {
            return LocalDate.ofEpochDay(temporal.getLong(EPOCH_DAY));
        }
        return null;
    };

    /**
     * 一个查询 {@code LocalTime}，如果未找到则返回 null。
     */
    static final TemporalQuery<LocalTime> LOCAL_TIME = (temporal) -> {
        if (temporal.isSupported(NANO_OF_DAY)) {
            return LocalTime.ofNanoOfDay(temporal.getLong(NANO_OF_DAY));
        }
        return null;
    };

}
