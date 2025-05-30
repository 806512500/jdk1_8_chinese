
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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * 一个以标准年-月-日历系统表示的日期。
 * <p>
 * 此类用于处理非ISO日历系统的日期。
 * 例如，日本、民国、泰历等。
 * <p>
 * {@code ChronoLocalDate} 基于年、月、日的通用概念。
 * 日历系统，由 {@link java.time.chrono.Chronology} 表示，表达了字段之间的关系，
 * 该类允许对生成的日期进行操作。
 * <p>
 * 请注意，并非所有日历系统都适合使用此类。
 * 例如，玛雅历使用与年、月、日无关的系统。
 * <p>
 * API设计鼓励在应用程序的大部分代码中使用 {@code LocalDate}。
 * 这包括从持久数据存储（如数据库）读取和写入日期的代码，
 * 以及通过网络发送日期和时间的代码。{@code ChronoLocalDate} 实例则在用户界面级别用于处理本地化的输入/输出。
 *
 * <P>示例： </p>
 * <pre>
 *        System.out.printf("Example()%n");
 *        // 枚举可用的日历列表并打印每个日历的今天日期
 *        Set&lt;Chronology&gt; chronos = Chronology.getAvailableChronologies();
 *        for (Chronology chrono : chronos) {
 *            ChronoLocalDate date = chrono.dateNow();
 *            System.out.printf("   %20s: %s%n", chrono.getID(), date.toString());
 *        }
 *
 *        // 打印希吉拉历的日期和日历
 *        ChronoLocalDate date = Chronology.of("Hijrah").dateNow();
 *        int day = date.get(ChronoField.DAY_OF_MONTH);
 *        int dow = date.get(ChronoField.DAY_OF_WEEK);
 *        int month = date.get(ChronoField.MONTH_OF_YEAR);
 *        int year = date.get(ChronoField.YEAR);
 *        System.out.printf("  Today is %s %s %d-%s-%d%n", date.getChronology().getID(),
 *                dow, day, month, year);

 *        // 打印今天的日期和一年的最后一天
 *        ChronoLocalDate now1 = Chronology.of("Hijrah").dateNow();
 *        ChronoLocalDate first = now1.with(ChronoField.DAY_OF_MONTH, 1)
 *                .with(ChronoField.MONTH_OF_YEAR, 1);
 *        ChronoLocalDate last = first.plus(1, ChronoUnit.YEARS)
 *                .minus(1, ChronoUnit.DAYS);
 *        System.out.printf("  Today is %s: start: %s; end: %s%n", last.getChronology().getID(),
 *                first, last);
 * </pre>
 *
 * <h3>添加日历</h3>
 * <p> 可以通过定义 {@link ChronoLocalDate} 的子类来表示日期实例，
 * 并实现 {@code Chronology} 作为 {@code ChronoLocalDate} 子类的工厂来扩展日历集。
 * </p>
 * <p> 为了允许发现额外的日历类型，{@code Chronology} 的实现必须注册为实现 {@code Chronology} 接口的服务，
 * 在 {@code META-INF/Services} 文件中，按照 {@link java.util.ServiceLoader} 的规范。
 * 子类必须根据 {@code Chronology} 类的描述进行操作，并提供其
 * {@link java.time.chrono.Chronology#getId() 日历ID} 和 {@link Chronology#getCalendarType() 日历类型}。 </p>
 *
 * @implSpec
 * 必须谨慎实现此抽象类以确保其他类正确运行。
 * 所有可以实例化的实现都必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @param <D> 此日期时间的 ChronoLocalDate
 * @since 1.8
 */
abstract class ChronoLocalDateImpl<D extends ChronoLocalDate>
        implements ChronoLocalDate, Temporal, TemporalAdjuster, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 6282433883239719096L;

    /**
     * 将 {@code Temporal} 转换为 {@code ChronoLocalDate} 并确保其具有指定的日历系统。
     *
     * @param chrono  要检查的日历系统，不为空
     * @param temporal  要转换的日期时间，不为空
     * @return 检查并转换为 {@code ChronoLocalDate} 的日期时间，不为空
     * @throws ClassCastException 如果日期时间不能转换为 ChronoLocalDate
     *  或日历系统不等于此 Chronology
     */
    static <D extends ChronoLocalDate> D ensureValid(Chronology chrono, Temporal temporal) {
        @SuppressWarnings("unchecked")
        D other = (D) temporal;
        if (chrono.equals(other.getChronology()) == false) {
            throw new ClassCastException("Chronology mismatch, expected: " + chrono.getId() + ", actual: " + other.getChronology().getId());
        }
        return other;
    }

    //-----------------------------------------------------------------------
    /**
     * 创建一个实例。
     */
    ChronoLocalDateImpl() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public D with(TemporalAdjuster adjuster) {
        return (D) ChronoLocalDate.super.with(adjuster);
    }

    @Override
    @SuppressWarnings("unchecked")
    public D with(TemporalField field, long value) {
        return (D) ChronoLocalDate.super.with(field, value);
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public D plus(TemporalAmount amount) {
        return (D) ChronoLocalDate.super.plus(amount);
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public D plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case DAYS: return plusDays(amountToAdd);
                case WEEKS: return plusDays(Math.multiplyExact(amountToAdd, 7));
                case MONTHS: return plusMonths(amountToAdd);
                case YEARS: return plusYears(amountToAdd);
                case DECADES: return plusYears(Math.multiplyExact(amountToAdd, 10));
                case CENTURIES: return plusYears(Math.multiplyExact(amountToAdd, 100));
                case MILLENNIA: return plusYears(Math.multiplyExact(amountToAdd, 1000));
                case ERAS: return with(ERA, Math.addExact(getLong(ERA), amountToAdd));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return (D) ChronoLocalDate.super.plus(amountToAdd, unit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public D minus(TemporalAmount amount) {
        return (D) ChronoLocalDate.super.minus(amount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public D minus(long amountToSubtract, TemporalUnit unit) {
        return (D) ChronoLocalDate.super.minus(amountToSubtract, unit);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此日期的副本，并添加指定数量的年份。
     * <p>
     * 这将指定的年份周期添加到日期。
     * 在某些情况下，添加年份可能会导致结果日期无效。
     * 如果发生这种情况，其他字段（通常是月中的日期）将被调整以确保结果有效。
     * 通常会选择该月的最后一天。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param yearsToAdd  要添加的年份，可以为负
     * @return 基于此日期并添加了年份的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    abstract D plusYears(long yearsToAdd);

    /**
     * 返回一个基于此日期的副本，并添加指定数量的月份。
     * <p>
     * 这将指定的月份周期添加到日期。
     * 在某些情况下，添加月份可能会导致结果日期无效。
     * 如果发生这种情况，其他字段（通常是月中的日期）将被调整以确保结果有效。
     * 通常会选择该月的最后一天。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param monthsToAdd  要添加的月份，可以为负
     * @return 基于此日期并添加了月份的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    abstract D plusMonths(long monthsToAdd);

    /**
     * 返回一个基于此日期的副本，并添加指定数量的周。
     * <p>
     * 这将指定的周数周期添加到日期。
     * 在某些情况下，添加周数可能会导致结果日期无效。
     * 如果发生这种情况，其他字段将被调整以确保结果有效。
     * <p>
     * 默认实现使用 {@link #plusDays(long)}，每周7天。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param weeksToAdd  要添加的周数，可以为负
     * @return 基于此日期并添加了周数的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    D plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    /**
     * 返回一个基于此日期的副本，并添加指定数量的天数。
     * <p>
     * 这将指定的天数周期添加到日期。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param daysToAdd  要添加的天数，可以为负
     * @return 基于此日期并添加了天数的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    abstract D plusDays(long daysToAdd);

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此日期的副本，并减去指定数量的年份。
     * <p>
     * 这将指定的年份周期从日期中减去。
     * 在某些情况下，减去年份可能会导致结果日期无效。
     * 如果发生这种情况，其他字段（通常是月中的日期）将被调整以确保结果有效。
     * 通常会选择该月的最后一天。
     * <p>
     * 默认实现使用 {@link #plusYears(long)}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param yearsToSubtract  要减去的年份，可以为负
     * @return 基于此日期并减去了年份的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    @SuppressWarnings("unchecked")
    D minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl<D>)plusYears(Long.MAX_VALUE)).plusYears(1) : plusYears(-yearsToSubtract));
    }

    /**
     * 返回一个基于此日期的副本，并减去指定数量的月份。
     * <p>
     * 这将指定的月份周期从日期中减去。
     * 在某些情况下，减去月份可能会导致结果日期无效。
     * 如果发生这种情况，其他字段（通常是月中的日期）将被调整以确保结果有效。
     * 通常会选择该月的最后一天。
     * <p>
     * 默认实现使用 {@link #plusMonths(long)}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param monthsToSubtract  要减去的月份，可以为负
     * @return 基于此日期并减去了月份的日期，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    @SuppressWarnings("unchecked")
    D minusMonths(long monthsToSubtract) {
        return (monthsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl<D>)plusMonths(Long.MAX_VALUE)).plusMonths(1) : plusMonths(-monthsToSubtract));
    }


                /**
     * 返回一个副本，从该日期中减去指定的周数。
     * <p>
     * 这会从日期中减去指定的周数。
     * 在某些情况下，减去周数可能会导致结果日期无效。
     * 如果发生这种情况，其他字段将被调整以确保结果有效。
     * <p>
     * 默认实现使用 {@link #plusWeeks(long)}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param weeksToSubtract  要减去的周数，可以是负数
     * @return 基于此日期的副本，减去了指定的周数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    @SuppressWarnings("unchecked")
    D minusWeeks(long weeksToSubtract) {
        return (weeksToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl<D>)plusWeeks(Long.MAX_VALUE)).plusWeeks(1) : plusWeeks(-weeksToSubtract));
    }

    /**
     * 返回一个副本，从该日期中减去指定的天数。
     * <p>
     * 这会从日期中减去指定的天数。
     * <p>
     * 默认实现使用 {@link #plusDays(long)}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param daysToSubtract  要减去的天数，可以是负数
     * @return 基于此日期的副本，减去了指定的天数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    @SuppressWarnings("unchecked")
    D minusDays(long daysToSubtract) {
        return (daysToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl<D>)plusDays(Long.MAX_VALUE)).plusDays(1) : plusDays(-daysToSubtract));
    }

    //-----------------------------------------------------------------------
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull(endExclusive, "endExclusive");
        ChronoLocalDate end = getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case DAYS: return daysUntil(end);
                case WEEKS: return daysUntil(end) / 7;
                case MONTHS: return monthsUntil(end);
                case YEARS: return monthsUntil(end) / 12;
                case DECADES: return monthsUntil(end) / 120;
                case CENTURIES: return monthsUntil(end) / 1200;
                case MILLENNIA: return monthsUntil(end) / 12000;
                case ERAS: return end.getLong(ERA) - getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        Objects.requireNonNull(unit, "unit");
        return unit.between(this, end);
    }

    private long daysUntil(ChronoLocalDate end) {
        return end.toEpochDay() - toEpochDay();  // no overflow
    }

    private long monthsUntil(ChronoLocalDate end) {
        ValueRange range = getChronology().range(MONTH_OF_YEAR);
        if (range.getMaximum() != 12) {
            throw new IllegalStateException("ChronoLocalDateImpl only supports Chronologies with 12 months per year");
        }
        long packed1 = getLong(PROLEPTIC_MONTH) * 32L + get(DAY_OF_MONTH);  // no overflow
        long packed2 = end.getLong(PROLEPTIC_MONTH) * 32L + end.get(DAY_OF_MONTH);  // no overflow
        return (packed2 - packed1) / 32;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChronoLocalDate) {
            return compareTo((ChronoLocalDate) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        long epDay = toEpochDay();
        return getChronology().hashCode() ^ ((int) (epDay ^ (epDay >>> 32)));
    }

    @Override
    public String toString() {
        // getLong() 减少在 toString() 中发生异常的机会
        long yoe = getLong(YEAR_OF_ERA);
        long moy = getLong(MONTH_OF_YEAR);
        long dom = getLong(DAY_OF_MONTH);
        StringBuilder buf = new StringBuilder(30);
        buf.append(getChronology().toString())
                .append(" ")
                .append(getEra())
                .append(" ")
                .append(yoe)
                .append(moy < 10 ? "-0" : "-").append(moy)
                .append(dom < 10 ? "-0" : "-").append(dom);
        return buf.toString();
    }

}
