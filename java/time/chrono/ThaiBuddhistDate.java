
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

import static java.time.chrono.ThaiBuddhistChronology.YEARS_DIFFERENCE;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * 泰国佛教日历系统中的日期。
 * <p>
 * 此日期使用 {@linkplain ThaiBuddhistChronology 泰国佛教日历}。
 * 该日历系统主要在泰国使用。
 * 日期对齐，使得 {@code 2484-01-01 (佛教)} 是 {@code 1941-01-01 (ISO)}。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）对 {@code ThaiBuddhistDate} 实例进行操作可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class ThaiBuddhistDate
        extends ChronoLocalDateImpl<ThaiBuddhistDate>
        implements ChronoLocalDate, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -8722293800195731463L;

    /**
     * 底层日期。
     */
    private final transient LocalDate isoDate;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前 {@code ThaiBuddhistDate}。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前日期，不为空
     */
    public static ThaiBuddhistDate now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前 {@code ThaiBuddhistDate}。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前日期，不为空
     */
    public static ThaiBuddhistDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前 {@code ThaiBuddhistDate}。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法可以使用替代时钟进行测试。
     * 可以通过 {@linkplain Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期，不为空
     * @throws DateTimeException 如果无法获取当前日期
     */
    public static ThaiBuddhistDate now(Clock clock) {
        return new ThaiBuddhistDate(LocalDate.now(clock));
    }

    /**
     * 从公历年、月份和日期字段获取表示泰国佛教日历系统的 {@code ThaiBuddhistDate}。
     * <p>
     * 这将返回具有指定字段的 {@code ThaiBuddhistDate}。
     * 日期必须在年份和月份范围内有效，否则将抛出异常。
     *
     * @param prolepticYear  泰国佛教公历年
     * @param month  泰国佛教月份，从 1 到 12
     * @param dayOfMonth  泰国佛教日期，从 1 到 31
     * @return 泰国佛教日历系统的日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，或日期在月份年份中无效
     */
    public static ThaiBuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
        return new ThaiBuddhistDate(LocalDate.of(prolepticYear - YEARS_DIFFERENCE, month, dayOfMonth));
    }

    /**
     * 从时间对象获取 {@code ThaiBuddhistDate}。
     * <p>
     * 这将基于指定的时间对象获取泰国佛教日历系统的日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ThaiBuddhistDate} 实例。
     * <p>
     * 转换通常使用 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 字段，该字段在日历系统中是标准化的。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名，允许其通过方法引用用作查询，{@code ThaiBuddhistDate::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 泰国佛教日历系统的日期，不为空
     * @throws DateTimeException 如果无法转换为 {@code ThaiBuddhistDate}
     */
    public static ThaiBuddhistDate from(TemporalAccessor temporal) {
        return ThaiBuddhistChronology.INSTANCE.date(temporal);
    }

    //-----------------------------------------------------------------------
    /**
     * 从 ISO 日期创建实例。
     *
     * @param isoDate  标准本地日期，已验证不为空
     */
    ThaiBuddhistDate(LocalDate isoDate) {
        Objects.requireNonNull(isoDate, "isoDate");
        this.isoDate = isoDate;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期的历法，即泰国佛教日历系统。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法定义。
     *
     * @return 泰国佛教历法，不为空
     */
    @Override
    public ThaiBuddhistChronology getChronology() {
        return ThaiBuddhistChronology.INSTANCE;
    }

    /**
     * 获取此日期适用的时代。
     * <p>
     * 泰国佛教日历系统有两个时代，'BE' 和 'BEFORE_BE'，由 {@link ThaiBuddhistEra} 定义。
     *
     * @return 适用于此日期的时代，不为空
     */
    @Override
    public ThaiBuddhistEra getEra() {
        return (getProlepticYear() >= 1 ? ThaiBuddhistEra.BE : ThaiBuddhistEra.BEFORE_BE);
    }

    /**
     * 返回此日期表示的月份长度。
     * <p>
     * 这将返回月份的天数。
     * 月份长度与 ISO 日历系统匹配。
     *
     * @return 月份的天数
     */
    @Override
    public int lengthOfMonth() {
        return isoDate.lengthOfMonth();
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (isSupported(field)) {
                ChronoField f = (ChronoField) field;
                switch (f) {
                    case DAY_OF_MONTH:
                    case DAY_OF_YEAR:
                    case ALIGNED_WEEK_OF_MONTH:
                        return isoDate.range(field);
                    case YEAR_OF_ERA: {
                        ValueRange range = YEAR.range();
                        long max = (getProlepticYear() <= 0 ? -(range.getMinimum() + YEARS_DIFFERENCE) + 1 : range.getMaximum() + YEARS_DIFFERENCE);
                        return ValueRange.of(1, max);
                    }
                }
                return getChronology().range(f);
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case PROLEPTIC_MONTH:
                    return getProlepticMonth();
                case YEAR_OF_ERA: {
                    int prolepticYear = getProlepticYear();
                    return (prolepticYear >= 1 ? prolepticYear : 1 - prolepticYear);
                }
                case YEAR:
                    return getProlepticYear();
                case ERA:
                    return (getProlepticYear() >= 1 ? 1 : 0);
            }
            return isoDate.getLong(field);
        }
        return field.getFrom(this);
    }

    private long getProlepticMonth() {
        return getProlepticYear() * 12L + isoDate.getMonthValue() - 1;
    }

    private int getProlepticYear() {
        return isoDate.getYear() + YEARS_DIFFERENCE;
    }

    //-----------------------------------------------------------------------
    @Override
    public ThaiBuddhistDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (getLong(f) == newValue) {
                return this;
            }
            switch (f) {
                case PROLEPTIC_MONTH:
                    getChronology().range(f).checkValidValue(newValue, f);
                    return plusMonths(newValue - getProlepticMonth());
                case YEAR_OF_ERA:
                case YEAR:
                case ERA: {
                    int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
                    switch (f) {
                        case YEAR_OF_ERA:
                            return with(isoDate.withYear((getProlepticYear() >= 1 ? nvalue : 1 - nvalue)  - YEARS_DIFFERENCE));
                        case YEAR:
                            return with(isoDate.withYear(nvalue - YEARS_DIFFERENCE));
                        case ERA:
                            return with(isoDate.withYear((1 - getProlepticYear()) - YEARS_DIFFERENCE));
                    }
                }
            }
            return with(isoDate.with(field, newValue));
        }
        return super.with(field, newValue);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public  ThaiBuddhistDate with(TemporalAdjuster adjuster) {
        return super.with(adjuster);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public ThaiBuddhistDate plus(TemporalAmount amount) {
        return super.plus(amount);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public ThaiBuddhistDate minus(TemporalAmount amount) {
        return super.minus(amount);
    }

    //-----------------------------------------------------------------------
    @Override
    ThaiBuddhistDate plusYears(long years) {
        return with(isoDate.plusYears(years));
    }

    @Override
    ThaiBuddhistDate plusMonths(long months) {
        return with(isoDate.plusMonths(months));
    }

    @Override
    ThaiBuddhistDate plusWeeks(long weeksToAdd) {
        return super.plusWeeks(weeksToAdd);
    }

    @Override
    ThaiBuddhistDate plusDays(long days) {
        return with(isoDate.plusDays(days));
    }

    @Override
    public ThaiBuddhistDate plus(long amountToAdd, TemporalUnit unit) {
        return super.plus(amountToAdd, unit);
    }


                @Override
    public ThaiBuddhistDate minus(long amountToAdd, TemporalUnit unit) {
        return super.minus(amountToAdd, unit);
    }

    @Override
    ThaiBuddhistDate minusYears(long yearsToSubtract) {
        return super.minusYears(yearsToSubtract);
    }

    @Override
    ThaiBuddhistDate minusMonths(long monthsToSubtract) {
        return super.minusMonths(monthsToSubtract);
    }

    @Override
    ThaiBuddhistDate minusWeeks(long weeksToSubtract) {
        return super.minusWeeks(weeksToSubtract);
    }

    @Override
    ThaiBuddhistDate minusDays(long daysToSubtract) {
        return super.minusDays(daysToSubtract);
    }

    private ThaiBuddhistDate with(LocalDate newDate) {
        return (newDate.equals(isoDate) ? this : new ThaiBuddhistDate(newDate));
    }

    @Override        // 用于 Javadoc 和协变返回类型
    @SuppressWarnings("unchecked")
    public final ChronoLocalDateTime<ThaiBuddhistDate> atTime(LocalTime localTime) {
        return (ChronoLocalDateTime<ThaiBuddhistDate>) super.atTime(localTime);
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDate) {
        Period period = isoDate.until(endDate);
        return getChronology().period(period.getYears(), period.getMonths(), period.getDays());
    }

    @Override  // 为了性能重写
    public long toEpochDay() {
        return isoDate.toEpochDay();
    }

    //-------------------------------------------------------------------------
    /**
     * 比较此日期与另一个日期，包括编年。
     * <p>
     * 比较此 {@code ThaiBuddhistDate} 与另一个，确保日期相同。
     * <p>
     * 只比较 {@code ThaiBuddhistDate} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同编年中的日期，使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期等于其他日期，则返回 true
     */
    @Override  // 为了性能重写
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ThaiBuddhistDate) {
            ThaiBuddhistDate otherDate = (ThaiBuddhistDate) obj;
            return this.isoDate.equals(otherDate.isoDate);
        }
        return false;
    }

    /**
     * 此日期的哈希码。
     *
     * @return 基于编年和日期的合适哈希码
     */
    @Override  // 为了性能重写
    public int hashCode() {
        return getChronology().getId().hashCode() ^ isoDate.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(10);                // 标识 ThaiBuddhistDate
     *  out.writeInt(get(YEAR));
     *  out.writeByte(get(MONTH_OF_YEAR));
     *  out.writeByte(get(DAY_OF_MONTH));
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.THAIBUDDHIST_DATE_TYPE, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        // ThaiBuddhistChronology 在 THAIBUDDHIST_DATE_TYPE 中是隐式的
        out.writeInt(this.get(YEAR));
        out.writeByte(this.get(MONTH_OF_YEAR));
        out.writeByte(this.get(DAY_OF_MONTH));
    }

    static ThaiBuddhistDate readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        int month = in.readByte();
        int dayOfMonth = in.readByte();
        return ThaiBuddhistChronology.INSTANCE.date(year, month, dayOfMonth);
    }

}
