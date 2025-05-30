
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
import java.util.Calendar;
import java.util.Objects;

import sun.util.calendar.CalendarDate;
import sun.util.calendar.LocalGregorianCalendar;

/**
 * 日本天皇历系统中的日期。
 * <p>
 * 此日期使用 {@linkplain JapaneseChronology 日本天皇历}。
 * 这个历法系统主要在日本使用。
 * <p>
 * 日本天皇历系统与ISO历法系统相同，除了基于时代的年份编号。假设的年份被定义为
 * 等于ISO假设的年份。
 * <p>
 * 日本在明治6年引入了格里高利历。
 * 只支持明治及以后的时代；
 * 不支持明治6年1月1日之前的日期。
 * <p>
 * 例如，日本年份“平成24”对应ISO年份“2012”。<br>
 * 调用 {@code japaneseDate.get(YEAR_OF_ERA)} 将返回24。<br>
 * 调用 {@code japaneseDate.get(YEAR)} 将返回2012。<br>
 * 调用 {@code japaneseDate.get(ERA)} 将返回2，对应于
 * {@code JapaneseChronology.ERA_HEISEI}。<br>
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code JapaneseDate} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})，身份哈希码，或同步）可能会产生不可预测的结果，应避免。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @since 1.8
 */
public final class JapaneseDate
        extends ChronoLocalDateImpl<JapaneseDate>
        implements ChronoLocalDate, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -305327627230580483L;

    /**
     * 底层的ISO本地日期。
     */
    private final transient LocalDate isoDate;
    /**
     * 此日期的日本时代。
     */
    private transient JapaneseEra era;
    /**
     * 此日期的日本天皇历年的年份。
     */
    private transient int yearOfEra;

    /**
     * 日本天皇历支持的最早日期是明治6年1月1日。
     */
    static final LocalDate MEIJI_6_ISODATE = LocalDate.of(1873, 1, 1);

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前的 {@code JapaneseDate}。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前日期，不为空
     */
    public static JapaneseDate now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前的 {@code JapaneseDate}。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区ID，不为空
     * @return 使用系统时钟的当前日期，不为空
     */
    public static JapaneseDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前的 {@code JapaneseDate}。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法可以使用替代时钟进行测试。
     * 可以通过 {@linkplain Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期，不为空
     * @throws DateTimeException 如果无法获取当前日期
     */
    public static JapaneseDate now(Clock clock) {
        return new JapaneseDate(LocalDate.now(clock));
    }

    /**
     * 从时代、年份、月份和日期字段获取表示日本历法系统中的日期的 {@code JapaneseDate}。
     * <p>
     * 这将返回一个具有指定字段的 {@code JapaneseDate}。
     * 日期必须对年份和月份有效，否则将抛出异常。
     * <p>
     * 日本的月份和日期与ISO历法系统中的相同。它们在时代变化时不会重置。
     * 例如：
     * <pre>
     *  明治64年1月6日 = ISO 1989-01-06
     *  明治64年1月7日 = ISO 1989-01-07
     *  平成1年1月8日 = ISO 1989-01-08
     *  平成1年1月9日 = ISO 1989-01-09
     * </pre>
     *
     * @param era  日本时代，不为空
     * @param yearOfEra  日本年份
     * @param month  日本月份，从1到12
     * @param dayOfMonth  日本日期，从1到31
     * @return 日本历法系统中的日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者日期对月份-年份无效，
     *  或者日期不是日本时代
     */
    public static JapaneseDate of(JapaneseEra era, int yearOfEra, int month, int dayOfMonth) {
        Objects.requireNonNull(era, "era");
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate(null);
        jdate.setEra(era.getPrivateEra()).setDate(yearOfEra, month, dayOfMonth);
        if (!JapaneseChronology.JCAL.validate(jdate)) {
            throw new DateTimeException("年份、月份和日期对时代无效");
        }
        LocalDate date = LocalDate.of(jdate.getNormalizedYear(), month, dayOfMonth);
        return new JapaneseDate(era, yearOfEra, date);
    }

    /**
     * 从假设年份、月份和日期字段获取表示日本历法系统中的日期的 {@code JapaneseDate}。
     * <p>
     * 这将返回一个具有指定字段的 {@code JapaneseDate}。
     * 日期必须对年份和月份有效，否则将抛出异常。
     * <p>
     * 日本假设年份、月份和日期与ISO历法系统中的相同。它们在时代变化时不会重置。
     *
     * @param prolepticYear  日本假设年份
     * @param month  日本月份，从1到12
     * @param dayOfMonth  日本日期，从1到31
     * @return 日本历法系统中的日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者日期对月份-年份无效
     */
    public static JapaneseDate of(int prolepticYear, int month, int dayOfMonth) {
        return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    /**
     * 从时代、年份和年中的日期字段获取表示日本历法系统中的日期的 {@code JapaneseDate}。
     * <p>
     * 这将返回一个具有指定字段的 {@code JapaneseDate}。
     * 日期必须对年份有效，否则将抛出异常。
     * <p>
     * 此工厂中的年中的日期相对于年份的开始表达。
     * 仅在年份因时代变化而重置为1的年份中，年中的日期的正常含义才会改变。
     * 例如：
     * <pre>
     *  明治64年1月6日 = 年中的第6天
     *  明治64年1月7日 = 年中的第7天
     *  平成1年1月8日 = 年中的第1天
     *  平成1年1月9日 = 年中的第2天
     * </pre>
     *
     * @param era  日本时代，不为空
     * @param yearOfEra  日本年份
     * @param dayOfYear  日历年的第几天，从1到366
     * @return 日本历法系统中的日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者年中的日期对年份无效
     */
    static JapaneseDate ofYearDay(JapaneseEra era, int yearOfEra, int dayOfYear) {
        Objects.requireNonNull(era, "era");
        CalendarDate firstDay = era.getPrivateEra().getSinceDate();
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate(null);
        jdate.setEra(era.getPrivateEra());
        if (yearOfEra == 1) {
            jdate.setDate(yearOfEra, firstDay.getMonth(), firstDay.getDayOfMonth() + dayOfYear - 1);
        } else {
            jdate.setDate(yearOfEra, 1, dayOfYear);
        }
        JapaneseChronology.JCAL.normalize(jdate);
        if (era.getPrivateEra() != jdate.getEra() || yearOfEra != jdate.getYear()) {
            throw new DateTimeException("参数无效");
        }
        LocalDate localdate = LocalDate.of(jdate.getNormalizedYear(),
                                      jdate.getMonth(), jdate.getDayOfMonth());
        return new JapaneseDate(era, yearOfEra, localdate);
    }

    /**
     * 从时间对象获取 {@code JapaneseDate}。
     * <p>
     * 这将基于指定的时间获取日本历法系统中的日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将这些信息转换为 {@code JapaneseDate} 的实例。
     * <p>
     * 转换通常使用 {@link ChronoField#EPOCH_DAY EPOCH_DAY}
     * 字段，该字段在历法系统中是标准化的。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用使用它作为查询，{@code JapaneseDate::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 日本历法系统中的日期，不为空
     * @throws DateTimeException 如果无法转换为 {@code JapaneseDate}
     */
    public static JapaneseDate from(TemporalAccessor temporal) {
        return JapaneseChronology.INSTANCE.date(temporal);
    }

    //-----------------------------------------------------------------------
    /**
     * 从ISO日期创建实例。
     *
     * @param isoDate  标准本地日期，验证后不为空
     */
    JapaneseDate(LocalDate isoDate) {
        if (isoDate.isBefore(MEIJI_6_ISODATE)) {
            throw new DateTimeException("不支持明治6年之前的 JapaneseDate");
        }
        LocalGregorianCalendar.Date jdate = toPrivateJapaneseDate(isoDate);
        this.era = JapaneseEra.toJapaneseEra(jdate.getEra());
        this.yearOfEra = jdate.getYear();
        this.isoDate = isoDate;
    }

    /**
     * 构造 {@code JapaneseDate}。此构造函数不会验证给定的参数，
     * 且 {@code era} 和 {@code year} 必须与 {@code isoDate} 一致。
     *
     * @param era  时代，验证后不为空
     * @param year  年份，验证后
     * @param isoDate  标准本地日期，验证后不为空
     */
    JapaneseDate(JapaneseEra era, int year, LocalDate isoDate) {
        if (isoDate.isBefore(MEIJI_6_ISODATE)) {
            throw new DateTimeException("不支持明治6年之前的 JapaneseDate");
        }
        this.era = era;
        this.yearOfEra = year;
        this.isoDate = isoDate;
    }


                //-----------------------------------------------------------------------
    /**
     * 获取此日期的历法，即日本历法系统。
     * <p>
     * {@code Chronology} 表示使用的历法系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法定义。
     *
     * @return 日本历法，不为空
     */
    @Override
    public JapaneseChronology getChronology() {
        return JapaneseChronology.INSTANCE;
    }

    /**
     * 获取此日期适用的时代。
     * <p>
     * 日本历法系统有多个由 {@link JapaneseEra} 定义的时代。
     *
     * @return 适用于此日期的时代，不为空
     */
    @Override
    public JapaneseEra getEra() {
        return era;
    }

    /**
     * 返回此日期表示的月份长度。
     * <p>
     * 这返回月份的天数。
     * 月份长度与 ISO 历法系统相匹配。
     *
     * @return 月份的天数
     */
    @Override
    public int lengthOfMonth() {
        return isoDate.lengthOfMonth();
    }

    @Override
    public int lengthOfYear() {
        Calendar jcal = Calendar.getInstance(JapaneseChronology.LOCALE);
        jcal.set(Calendar.ERA, era.getValue() + JapaneseEra.ERA_OFFSET);
        jcal.set(yearOfEra, isoDate.getMonthValue() - 1, isoDate.getDayOfMonth());
        return  jcal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这检查此日期是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code DAY_OF_WEEK}
     * <li>{@code DAY_OF_MONTH}
     * <li>{@code DAY_OF_YEAR}
     * <li>{@code EPOCH_DAY}
     * <li>{@code MONTH_OF_YEAR}
     * <li>{@code PROLEPTIC_MONTH}
     * <li>{@code YEAR_OF_ERA}
     * <li>{@code YEAR}
     * <li>{@code ERA}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此日期上受支持则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field == ALIGNED_DAY_OF_WEEK_IN_MONTH || field == ALIGNED_DAY_OF_WEEK_IN_YEAR ||
                field == ALIGNED_WEEK_OF_MONTH || field == ALIGNED_WEEK_OF_YEAR) {
            return false;
        }
        return ChronoLocalDate.super.isSupported(field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (isSupported(field)) {
                ChronoField f = (ChronoField) field;
                switch (f) {
                    case DAY_OF_MONTH: return ValueRange.of(1, lengthOfMonth());
                    case DAY_OF_YEAR: return ValueRange.of(1, lengthOfYear());
                    case YEAR_OF_ERA: {
                        Calendar jcal = Calendar.getInstance(JapaneseChronology.LOCALE);
                        jcal.set(Calendar.ERA, era.getValue() + JapaneseEra.ERA_OFFSET);
                        jcal.set(yearOfEra, isoDate.getMonthValue() - 1, isoDate.getDayOfMonth());
                        return ValueRange.of(1, jcal.getActualMaximum(Calendar.YEAR));
                    }
                }
                return getChronology().range(f);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            // 与 ISO 相同：
            // DAY_OF_WEEK, DAY_OF_MONTH, EPOCH_DAY, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR
            //
            // 日历特定字段
            // DAY_OF_YEAR, YEAR_OF_ERA, ERA
            switch ((ChronoField) field) {
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                case ALIGNED_WEEK_OF_MONTH:
                case ALIGNED_WEEK_OF_YEAR:
                    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
                case YEAR_OF_ERA:
                    return yearOfEra;
                case ERA:
                    return era.getValue();
                case DAY_OF_YEAR:
                    Calendar jcal = Calendar.getInstance(JapaneseChronology.LOCALE);
                    jcal.set(Calendar.ERA, era.getValue() + JapaneseEra.ERA_OFFSET);
                    jcal.set(yearOfEra, isoDate.getMonthValue() - 1, isoDate.getDayOfMonth());
                    return jcal.get(Calendar.DAY_OF_YEAR);
            }
            return isoDate.getLong(field);
        }
        return field.getFrom(this);
    }

    /**
     * 从给定的 {@code isoDate} 转换为 {@code LocalGregorianCalendar.Date}。
     *
     * @param isoDate  本地日期，不为空
     * @return 一个 {@code LocalGregorianCalendar.Date}，不为空
     */
    private static LocalGregorianCalendar.Date toPrivateJapaneseDate(LocalDate isoDate) {
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate(null);
        sun.util.calendar.Era sunEra = JapaneseEra.privateEraFrom(isoDate);
        int year = isoDate.getYear();
        if (sunEra != null) {
            year -= sunEra.getSinceDate().getYear() - 1;
        }
        jdate.setEra(sunEra).setYear(year).setMonth(isoDate.getMonthValue()).setDayOfMonth(isoDate.getDayOfMonth());
        JapaneseChronology.JCAL.normalize(jdate);
        return jdate;
    }

    //-----------------------------------------------------------------------
    @Override
    public JapaneseDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (getLong(f) == newValue) {  // getLong() 验证受支持的字段
                return this;
            }
            switch (f) {
                case YEAR_OF_ERA:
                case YEAR:
                case ERA: {
                    int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
                    switch (f) {
                        case YEAR_OF_ERA:
                            return this.withYear(nvalue);
                        case YEAR:
                            return with(isoDate.withYear(nvalue));
                        case ERA: {
                            return this.withYear(JapaneseEra.of(nvalue), yearOfEra);
                        }
                    }
                }
            }
            // YEAR, PROLEPTIC_MONTH 和其他字段与 ISO 相同
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
    public  JapaneseDate with(TemporalAdjuster adjuster) {
        return super.with(adjuster);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public JapaneseDate plus(TemporalAmount amount) {
        return super.plus(amount);
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    public JapaneseDate minus(TemporalAmount amount) {
        return super.minus(amount);
    }
    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中年份已更改。
     * <p>
     * 此方法更改日期的年份。
     * 如果月份-日期对年份无效，则选择前一个有效的日期。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param era  要在结果中设置的时代，不为空
     * @param yearOfEra  要在返回的日期中设置的年份
     * @return 基于此日期并具有请求年份的 {@code JapaneseDate}，从不为空
     * @throws DateTimeException 如果 {@code year} 无效
     */
    private JapaneseDate withYear(JapaneseEra era, int yearOfEra) {
        int year = JapaneseChronology.INSTANCE.prolepticYear(era, yearOfEra);
        return with(isoDate.withYear(year));
    }

    /**
     * 返回一个副本，其中年份已更改。
     * <p>
     * 此方法更改日期的年份。
     * 如果月份-日期对年份无效，则选择前一个有效的日期。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param year  要在返回的日期中设置的年份
     * @return 基于此日期并具有请求年份的 {@code JapaneseDate}，从不为空
     * @throws DateTimeException 如果 {@code year} 无效
     */
    private JapaneseDate withYear(int year) {
        return withYear(getEra(), year);
    }

    //-----------------------------------------------------------------------
    @Override
    JapaneseDate plusYears(long years) {
        return with(isoDate.plusYears(years));
    }

    @Override
    JapaneseDate plusMonths(long months) {
        return with(isoDate.plusMonths(months));
    }

    @Override
    JapaneseDate plusWeeks(long weeksToAdd) {
        return with(isoDate.plusWeeks(weeksToAdd));
    }

    @Override
    JapaneseDate plusDays(long days) {
        return with(isoDate.plusDays(days));
    }

    @Override
    public JapaneseDate plus(long amountToAdd, TemporalUnit unit) {
        return super.plus(amountToAdd, unit);
    }

    @Override
    public JapaneseDate minus(long amountToAdd, TemporalUnit unit) {
        return super.minus(amountToAdd, unit);
    }

    @Override
    JapaneseDate minusYears(long yearsToSubtract) {
        return super.minusYears(yearsToSubtract);
    }

    @Override
    JapaneseDate minusMonths(long monthsToSubtract) {
        return super.minusMonths(monthsToSubtract);
    }

    @Override
    JapaneseDate minusWeeks(long weeksToSubtract) {
        return super.minusWeeks(weeksToSubtract);
    }

    @Override
    JapaneseDate minusDays(long daysToSubtract) {
        return super.minusDays(daysToSubtract);
    }

    private JapaneseDate with(LocalDate newDate) {
        return (newDate.equals(isoDate) ? this : new JapaneseDate(newDate));
    }

    @Override        // 用于 Javadoc 和协变返回类型
    @SuppressWarnings("unchecked")
    public final ChronoLocalDateTime<JapaneseDate> atTime(LocalTime localTime) {
        return (ChronoLocalDateTime<JapaneseDate>)super.atTime(localTime);
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDate) {
        Period period = isoDate.until(endDate);
        return getChronology().period(period.getYears(), period.getMonths(), period.getDays());
    }

    @Override  // 重写以提高性能
    public long toEpochDay() {
        return isoDate.toEpochDay();
    }

    //-------------------------------------------------------------------------
    /**
     * 比较此日期与另一个日期，包括历法。
     * <p>
     * 比较此 {@code JapaneseDate} 与另一个，确保日期相同。
     * <p>
     * 只比较 {@code JapaneseDate} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同历法中的日期，使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期等于其他日期则返回 true
     */
    @Override  // 重写以提高性能
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof JapaneseDate) {
            JapaneseDate otherDate = (JapaneseDate) obj;
            return this.isoDate.equals(otherDate.isoDate);
        }
        return false;
    }

    /**
     * 此日期的哈希码。
     *
     * @return 基于历法和日期的合适哈希码
     */
    @Override  // 重写以提高性能
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
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(4);                 // 标识一个 JapaneseDate
     *  out.writeInt(get(YEAR));
     *  out.writeByte(get(MONTH_OF_YEAR));
     *  out.writeByte(get(DAY_OF_MONTH));
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.JAPANESE_DATE_TYPE, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        // JapaneseChronology 隐含在 JAPANESE_DATE_TYPE 中
        out.writeInt(get(YEAR));
        out.writeByte(get(MONTH_OF_YEAR));
        out.writeByte(get(DAY_OF_MONTH));
    }

    static JapaneseDate readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        int month = in.readByte();
        int dayOfMonth = in.readByte();
        return JapaneseChronology.INSTANCE.date(year, month, dayOfMonth);
    }

}
