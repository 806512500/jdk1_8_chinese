
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

import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
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

/**
 * 伊斯兰历系统中的日期。
 * <p>
 * 此日期使用 {@linkplain HijrahChronology 伊斯兰历} 的一个变体。
 * <p>
 * 伊斯兰历每年的总天数与格里高利历不同，每个月的长度基于月亮绕地球一周的周期
 * （即从一个新月到下一个新月的周期）。
 * 有关支持的变体的详细信息，请参阅 {@link HijrahChronology}。
 * <p>
 * 每个 HijrahDate 都绑定到一个特定的 HijrahChronology，
 * 从该日期计算出的每个 HijrahDate 都会传播相同的历法。
 * 要使用不同的伊斯兰历变体，可以使用其 HijrahChronology 创建新的 HijrahDate 实例。
 * 或者，可以使用 {@link #withVariant} 方法转换到新的 HijrahChronology。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code HijrahDate} 实例使用身份敏感操作（包括引用相等性
 * （{@code ==}），身份哈希码，或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class HijrahDate
        extends ChronoLocalDateImpl<HijrahDate>
        implements ChronoLocalDate, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -5207853542612002020L;
    /**
     * 该 HijrahDate 的历法。
     */
    private final transient HijrahChronology chrono;
    /**
     * 延续年。
     */
    private final transient int prolepticYear;
    /**
     * 年中的月份。
     */
    private final transient int monthOfYear;
    /**
     * 月中的日期。
     */
    private final transient int dayOfMonth;

    //-------------------------------------------------------------------------
    /**
     * 从伊斯兰延续年、年中的月份和月中的日期获取 {@code HijrahDate} 的实例。
     *
     * @param prolepticYear  要在伊斯兰历中表示的延续年
     * @param monthOfYear  要表示的年中的月份，从 1 到 12
     * @param dayOfMonth  要表示的月中的日期，从 1 到 30
     * @return 伊斯兰日期，从不为 null
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    static HijrahDate of(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
        return new HijrahDate(chrono, prolepticYear, monthOfYear, dayOfMonth);
    }

    /**
     * 返回指定历法和纪元日的 HijrahDate。
     * @param chrono 伊斯兰历法
     * @param epochDay 纪元日
     * @return 纪元日的 HijrahDate；非空
     */
    static HijrahDate ofEpochDay(HijrahChronology chrono, long epochDay) {
        return new HijrahDate(chrono, epochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取默认时区中的伊斯兰 Umm Al-Qura 历法的当前 {@code HijrahDate}。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期。
     * <p>
     * 使用此方法将无法在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前日期，从不为 null
     */
    public static HijrahDate now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 获取指定时区中的伊斯兰 Umm Al-Qura 历法的当前 {@code HijrahDate}。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前日期，从不为 null
     */
    public static HijrahDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取伊斯兰 Umm Al-Qura 历法的当前 {@code HijrahDate}。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法允许在测试中使用替代时钟。
     * 可以通过 {@linkplain Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期，从不为 null
     * @throws DateTimeException 如果无法获取当前日期
     */
    public static HijrahDate now(Clock clock) {
        return HijrahDate.ofEpochDay(HijrahChronology.INSTANCE, LocalDate.now(clock).toEpochDay());
    }

    /**
     * 从延续年、年中的月份和月中的日期字段获取伊斯兰 Umm Al-Qura 历法的 {@code HijrahDate}。
     * <p>
     * 这将返回一个具有指定字段的 {@code HijrahDate}。
     * 日期必须对年份和月份有效，否则将抛出异常。
     *
     * @param prolepticYear  伊斯兰延续年
     * @param month  伊斯兰年中的月份，从 1 到 12
     * @param dayOfMonth  伊斯兰月中的日期，从 1 到 30
     * @return 伊斯兰历法系统中的日期，从不为 null
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者月中的日期对年份-月份无效
     */
    public static HijrahDate of(int prolepticYear, int month, int dayOfMonth) {
        return HijrahChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
    }

    /**
     * 从时间对象获取伊斯兰 Umm Al-Qura 历法的 {@code HijrahDate}。
     * <p>
     * 这将基于指定的时间对象获取伊斯兰历法系统中的日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code HijrahDate} 的实例。
     * <p>
     * 转换通常使用 {@link ChronoField#EPOCH_DAY EPOCH_DAY}
     * 字段，该字段在日历系统中是标准化的。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用使用它，{@code HijrahDate::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 伊斯兰历法系统中的日期，从不为 null
     * @throws DateTimeException 如果无法转换为 {@code HijrahDate}
     */
    public static HijrahDate from(TemporalAccessor temporal) {
        return HijrahChronology.INSTANCE.date(temporal);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用延续年、年中的月份和月中的日期字段构造 {@code HijrahDate}。
     *
     * @param chrono  用于创建日期的历法
     * @param prolepticYear  延续年
     * @param monthOfYear  年中的月份
     * @param dayOfMonth  月中的日期
     */
    private HijrahDate(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
        // 计算格里高利日会检查有效范围
        chrono.getEpochDay(prolepticYear, monthOfYear, dayOfMonth);

        this.chrono = chrono;
        this.prolepticYear = prolepticYear;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * 使用纪元日构造实例。
     *
     * @param epochDay  纪元日
     */
    private HijrahDate(HijrahChronology chrono, long epochDay) {
        int[] dateInfo = chrono.getHijrahDateInfo((int)epochDay);

        this.chrono = chrono;
        this.prolepticYear = dateInfo[0];
        this.monthOfYear = dateInfo[1];
        this.dayOfMonth = dateInfo[2];
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期的历法，即伊斯兰历法系统。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法定义。
     *
     * @return 伊斯兰历法，从不为 null
     */
    @Override
    public HijrahChronology getChronology() {
        return chrono;
    }

    /**
     * 获取此日期适用的时代。
     * <p>
     * 伊斯兰历法系统有一个时代，'AH'，
     * 由 {@link HijrahEra} 定义。
     *
     * @return 适用于此日期的时代，从不为 null
     */
    @Override
    public HijrahEra getEra() {
        return HijrahEra.AH;
    }

    /**
     * 返回此日期表示的月份的长度。
     * <p>
     * 这将返回月份的天数。
     * 伊斯兰历法系统中的月份长度在 29 到 30 天之间变化。
     *
     * @return 月份的天数
     */
    @Override
    public int lengthOfMonth() {
        return chrono.getMonthLength(prolepticYear, monthOfYear);
    }

    /**
     * 返回此日期表示的年份的长度。
     * <p>
     * 这将返回年份的天数。
     * 伊斯兰历法系统中的年份通常比 ISO 日历系统中的年份短。
     *
     * @return 年份的天数
     */
    @Override
    public int lengthOfYear() {
        return chrono.getYearLength(prolepticYear);
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (isSupported(field)) {
                ChronoField f = (ChronoField) field;
                switch (f) {
                    case DAY_OF_MONTH: return ValueRange.of(1, lengthOfMonth());
                    case DAY_OF_YEAR: return ValueRange.of(1, lengthOfYear());
                    case ALIGNED_WEEK_OF_MONTH: return ValueRange.of(1, 5);  // TODO
                    // TODO 有效年份的有限范围是否会导致年份在中途开始/结束？这会影响范围
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
                case DAY_OF_WEEK: return getDayOfWeek();
                case ALIGNED_DAY_OF_WEEK_IN_MONTH: return ((getDayOfWeek() - 1) % 7) + 1;
                case ALIGNED_DAY_OF_WEEK_IN_YEAR: return ((getDayOfYear() - 1) % 7) + 1;
                case DAY_OF_MONTH: return this.dayOfMonth;
                case DAY_OF_YEAR: return this.getDayOfYear();
                case EPOCH_DAY: return toEpochDay();
                case ALIGNED_WEEK_OF_MONTH: return ((dayOfMonth - 1) / 7) + 1;
                case ALIGNED_WEEK_OF_YEAR: return ((getDayOfYear() - 1) / 7) + 1;
                case MONTH_OF_YEAR: return monthOfYear;
                case PROLEPTIC_MONTH: return getProlepticMonth();
                case YEAR_OF_ERA: return prolepticYear;
                case YEAR: return prolepticYear;
                case ERA: return getEraValue();
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.getFrom(this);
    }


                private long getProlepticMonth() {
        return prolepticYear * 12L + monthOfYear - 1;
    }

    @Override
    public HijrahDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            // 不使用 checkValidIntValue，以便 EPOCH_DAY 和 PROLEPTIC_MONTH 能够工作
            chrono.range(f).checkValidValue(newValue, f);    // TODO: 验证值
            int nvalue = (int) newValue;
            switch (f) {
                case DAY_OF_WEEK: return plusDays(newValue - getDayOfWeek());
                case ALIGNED_DAY_OF_WEEK_IN_MONTH: return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ALIGNED_DAY_OF_WEEK_IN_YEAR: return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case DAY_OF_MONTH: return resolvePreviousValid(prolepticYear, monthOfYear, nvalue);
                case DAY_OF_YEAR: return plusDays(Math.min(nvalue, lengthOfYear()) - getDayOfYear());
                case EPOCH_DAY: return new HijrahDate(chrono, newValue);
                case ALIGNED_WEEK_OF_MONTH: return plusDays((newValue - getLong(ALIGNED_WEEK_OF_MONTH)) * 7);
                case ALIGNED_WEEK_OF_YEAR: return plusDays((newValue - getLong(ALIGNED_WEEK_OF_YEAR)) * 7);
                case MONTH_OF_YEAR: return resolvePreviousValid(prolepticYear, nvalue, dayOfMonth);
                case PROLEPTIC_MONTH: return plusMonths(newValue - getProlepticMonth());
                case YEAR_OF_ERA: return resolvePreviousValid(prolepticYear >= 1 ? nvalue : 1 - nvalue, monthOfYear, dayOfMonth);
                case YEAR: return resolvePreviousValid(nvalue, monthOfYear, dayOfMonth);
                case ERA: return resolvePreviousValid(1 - prolepticYear, monthOfYear, dayOfMonth);
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return super.with(field, newValue);
    }

    private HijrahDate resolvePreviousValid(int prolepticYear, int month, int day) {
        int monthDays = chrono.getMonthLength(prolepticYear, month);
        if (day > monthDays) {
            day = monthDays;
        }
        return HijrahDate.of(chrono, prolepticYear, month, day);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException 如果无法进行调整。例如，如果调整器需要 ISO 历法
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public  HijrahDate with(TemporalAdjuster adjuster) {
        return super.with(adjuster);
    }

    /**
     * 返回具有请求的历法的 {@code HijrahDate}。
     * <p>
     * 年、月和日将根据新的请求的 HijrahChronology 进行检查。如果历法的月长度较短，
     * 则日将减少到该月的最后一天。
     *
     * @param chronology 新的 HijrahChonology，非空
     * @return 具有请求的 HijrahChronology 的 HijrahDate，非空
     */
    public HijrahDate withVariant(HijrahChronology chronology) {
        if (chrono == chronology) {
            return this;
        }
        // 类似于 resolvePreviousValid，日将保持在相同的月份内
        int monthDays = chronology.getDayOfYear(prolepticYear, monthOfYear);
        return HijrahDate.of(chronology, prolepticYear, monthOfYear,(dayOfMonth > monthDays) ? monthDays : dayOfMonth );
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public HijrahDate plus(TemporalAmount amount) {
        return super.plus(amount);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public HijrahDate minus(TemporalAmount amount) {
        return super.minus(amount);
    }

    @Override
    public long toEpochDay() {
        return chrono.getEpochDay(prolepticYear, monthOfYear, dayOfMonth);
    }

    /**
     * 获取年份中的天数字段。
     * <p>
     * 此方法返回原始的 {@code int} 值，表示年份中的天数。
     *
     * @return 年份中的天数
     */
    private int getDayOfYear() {
        return chrono.getDayOfYear(prolepticYear, monthOfYear) + dayOfMonth;
    }

    /**
     * 获取星期几的值。
     *
     * @return 星期几；从 epochday 计算得出
     */
    private int getDayOfWeek() {
        int dow0 = (int)Math.floorMod(toEpochDay() + 3, 7);
        return dow0 + 1;
    }

    /**
     * 获取此日期的纪元。
     *
     * @return 此日期的纪元；从 epochDay 计算得出
     */
    private int getEraValue() {
        return (prolepticYear > 1 ? 1 : 0);
    }

    //-----------------------------------------------------------------------
    /**
     * 根据希吉拉历法系统规则检查年份是否为闰年。
     *
     * @return 如果此日期在闰年中，则返回 true
     */
    @Override
    public boolean isLeapYear() {
        return chrono.isLeapYear(prolepticYear);
    }

    //-----------------------------------------------------------------------
    @Override
    HijrahDate plusYears(long years) {
        if (years == 0) {
            return this;
        }
        int newYear = Math.addExact(this.prolepticYear, (int)years);
        return resolvePreviousValid(newYear, monthOfYear, dayOfMonth);
    }

    @Override
    HijrahDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = prolepticYear * 12L + (monthOfYear - 1);
        long calcMonths = monthCount + monthsToAdd;  // 安全的溢出
        int newYear = chrono.checkValidYear(Math.floorDiv(calcMonths, 12L));
        int newMonth = (int)Math.floorMod(calcMonths, 12L) + 1;
        return resolvePreviousValid(newYear, newMonth, dayOfMonth);
    }

    @Override
    HijrahDate plusWeeks(long weeksToAdd) {
        return super.plusWeeks(weeksToAdd);
    }

    @Override
    HijrahDate plusDays(long days) {
        return new HijrahDate(chrono, toEpochDay() + days);
    }

    @Override
    public HijrahDate plus(long amountToAdd, TemporalUnit unit) {
        return super.plus(amountToAdd, unit);
    }

    @Override
    public HijrahDate minus(long amountToSubtract, TemporalUnit unit) {
        return super.minus(amountToSubtract, unit);
    }

    @Override
    HijrahDate minusYears(long yearsToSubtract) {
        return super.minusYears(yearsToSubtract);
    }

    @Override
    HijrahDate minusMonths(long monthsToSubtract) {
        return super.minusMonths(monthsToSubtract);
    }

    @Override
    HijrahDate minusWeeks(long weeksToSubtract) {
        return super.minusWeeks(weeksToSubtract);
    }

    @Override
    HijrahDate minusDays(long daysToSubtract) {
        return super.minusDays(daysToSubtract);
    }

    @Override        // 用于 javadoc 和协变返回类型
    @SuppressWarnings("unchecked")
    public final ChronoLocalDateTime<HijrahDate> atTime(LocalTime localTime) {
        return (ChronoLocalDateTime<HijrahDate>)super.atTime(localTime);
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDate) {
        // TODO: 未测试
        HijrahDate end = getChronology().date(endDate);
        long totalMonths = (end.prolepticYear - this.prolepticYear) * 12 + (end.monthOfYear - this.monthOfYear);  // 安全
        int days = end.dayOfMonth - this.dayOfMonth;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            HijrahDate calcDate = this.plusMonths(totalMonths);
            days = (int) (end.toEpochDay() - calcDate.toEpochDay());  // 安全
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12;  // 安全
        int months = (int) (totalMonths % 12);  // 安全
        return getChronology().period(Math.toIntExact(years), months, days);
    }

    //-------------------------------------------------------------------------
    /**
     * 比较此日期与另一个日期，包括历法。
     * <p>
     * 比较此 {@code HijrahDate} 与另一个日期，确保日期相同。
     * <p>
     * 只比较 {@code HijrahDate} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同历法的日期，
     * 请使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期与另一个日期相等且历法相同，则返回 true
     */
    @Override  // 重写以提高性能
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HijrahDate) {
            HijrahDate otherDate = (HijrahDate) obj;
            return prolepticYear == otherDate.prolepticYear
                && this.monthOfYear == otherDate.monthOfYear
                && this.dayOfMonth == otherDate.dayOfMonth
                && getChronology().equals(otherDate.getChronology());
        }
        return false;
    }

    /**
     * 此日期的哈希码。
     *
     * @return 仅基于历法和日期的合适的哈希码
     */
    @Override  // 重写以提高性能
    public int hashCode() {
        int yearValue = prolepticYear;
        int monthValue = monthOfYear;
        int dayValue = dayOfMonth;
        return getChronology().getId().hashCode() ^ (yearValue & 0xFFFFF800)
                ^ ((yearValue << 11) + (monthValue << 6) + (dayValue));
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
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用的序列化形式</a>
     * 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(6);                 // 标识一个 HijrahDate
     *  out.writeObject(chrono);          // HijrahChronology 变体
     *  out.writeInt(get(YEAR));
     *  out.writeByte(get(MONTH_OF_YEAR));
     *  out.writeByte(get(DAY_OF_MONTH));
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.HIJRAH_DATE_TYPE, this);
    }

    void writeExternal(ObjectOutput out) throws IOException {
        // HijrahChronology 在 Hijrah_DATE_TYPE 中是隐式的
        out.writeObject(getChronology());
        out.writeInt(get(YEAR));
        out.writeByte(get(MONTH_OF_YEAR));
        out.writeByte(get(DAY_OF_MONTH));
    }

    static HijrahDate readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        HijrahChronology chrono = (HijrahChronology) in.readObject();
        int year = in.readInt();
        int month = in.readByte();
        int dayOfMonth = in.readByte();
        return chrono.date(year, month, dayOfMonth);
    }

}
