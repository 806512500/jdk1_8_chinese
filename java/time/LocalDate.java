
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

import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
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
import java.util.Objects;

/**
 * ISO-8601日历系统中的一个没有时区的日期，例如 {@code 2007-12-03}。
 * <p>
 * {@code LocalDate} 是一个不可变的日期时间对象，表示一个日期，通常被视为年-月-日。其他日期字段，如年份中的第几天、周中的第几天和年中的第几周，也可以访问。
 * 例如，值 "2007年10月2日" 可以存储在 {@code LocalDate} 中。
 * <p>
 * 该类不存储或表示时间或时区。相反，它是日期的描述，如生日。它不能在没有额外信息（如偏移量或时区）的情况下表示时间线上的一个瞬间。
 * <p>
 * ISO-8601日历系统是今天世界上大多数地方使用的现代民用日历系统。它等同于回溯性格里高利历系统，即今天的所有闰年规则都适用于所有时间。
 * 对于今天编写的大多数应用程序，ISO-8601规则是完全合适的。然而，任何使用历史日期并要求它们准确的应用程序都会发现ISO-8601方法不合适。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code LocalDate} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会导致不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class LocalDate
        implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {

    /**
     * 支持的最小 {@code LocalDate}，'-999999999-01-01'。
     * 应用程序可以将其用作“远过去”的日期。
     */
    public static final LocalDate MIN = LocalDate.of(Year.MIN_VALUE, 1, 1);
    /**
     * 支持的最大 {@code LocalDate}，'+999999999-12-31'。
     * 应用程序可以将其用作“远未来”的日期。
     */
    public static final LocalDate MAX = LocalDate.of(Year.MAX_VALUE, 12, 31);

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 2942565459149668126L;
    /**
     * 400年周期中的天数。
     */
    private static final int DAYS_PER_CYCLE = 146097;
    /**
     * 从公元0年到1970年的天数。
     * 从公元0年到2000年有五个400年周期。
     * 从1970年到2000年有7个闰年。
     */
    static final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

    /**
     * 年份。
     */
    private final int year;
    /**
     * 月份。
     */
    private final short month;
    /**
     * 月份中的日期。
     */
    private final short day;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前日期。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前日期，不为空
     */
    public static LocalDate now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前日期。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区ID，不为空
     * @return 使用系统时钟的当前日期，不为空
     */
    public static LocalDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前日期。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法允许在测试中使用替代时钟。
     * 可以通过 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前日期，不为空
     */
    public static LocalDate now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        // 内联以避免创建对象和 Instant 检查
        final Instant now = clock.instant();  // 调用一次
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        long epochSec = now.getEpochSecond() + offset.getTotalSeconds();  // 溢出在后面捕获
        long epochDay = Math.floorDiv(epochSec, SECONDS_PER_DAY);
        return LocalDate.ofEpochDay(epochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * 从年、月和日获取 {@code LocalDate} 的实例。
     * <p>
     * 这返回一个具有指定年、月和日的 {@code LocalDate}。
     * 日期必须对年和月有效，否则将抛出异常。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，不为空
     * @param dayOfMonth  要表示的月份中的日期，从 1 到 31
     * @return 本地日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者月份中的日期对年-月无效
     */
    public static LocalDate of(int year, Month month, int dayOfMonth) {
        YEAR.checkValidValue(year);
        Objects.requireNonNull(month, "month");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year, month.getValue(), dayOfMonth);
    }

    /**
     * 从年、月和日获取 {@code LocalDate} 的实例。
     * <p>
     * 这返回一个具有指定年、月和日的 {@code LocalDate}。
     * 日期必须对年和月有效，否则将抛出异常。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，从 1（1月）到 12（12月）
     * @param dayOfMonth  要表示的月份中的日期，从 1 到 31
     * @return 本地日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者月份中的日期对年-月无效
     */
    public static LocalDate of(int year, int month, int dayOfMonth) {
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(month);
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return create(year, month, dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 从年和年中的日期获取 {@code LocalDate} 的实例。
     * <p>
     * 这返回一个具有指定年和年中的日期的 {@code LocalDate}。
     * 年中的日期必须对年有效，否则将抛出异常。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param dayOfYear  要表示的年中的日期，从 1 到 366
     * @return 本地日期，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者年中的日期对年无效
     */
    public static LocalDate ofYearDay(int year, int dayOfYear) {
        YEAR.checkValidValue(year);
        DAY_OF_YEAR.checkValidValue(dayOfYear);
        boolean leap = IsoChronology.INSTANCE.isLeapYear(year);
        if (dayOfYear == 366 && leap == false) {
            throw new DateTimeException("无效的日期 '年中的第366天'，因为 '" + year + "' 不是闰年");
        }
        Month moy = Month.of((dayOfYear - 1) / 31 + 1);
        int monthEnd = moy.firstDayOfYear(leap) + moy.length(leap) - 1;
        if (dayOfYear > monthEnd) {
            moy = moy.plus(1);
        }
        int dom = dayOfYear - moy.firstDayOfYear(leap) + 1;
        return new LocalDate(year, moy.getValue(), dom);
    }

    //-----------------------------------------------------------------------
    /**
     * 从纪元日计数获取 {@code LocalDate} 的实例。
     * <p>
     * 这返回一个具有指定纪元日的 {@code LocalDate}。
     * {@link ChronoField#EPOCH_DAY EPOCH_DAY} 是一个简单的递增计数
     * 的天数，其中第0天是1970-01-01。负数表示更早的日期。
     *
     * @param epochDay  要转换的纪元日，基于1970-01-01
     * @return 本地日期，不为空
     * @throws DateTimeException 如果纪元日超出支持的日期范围
     */
    public static LocalDate ofEpochDay(long epochDay) {
        long zeroDay = epochDay + DAYS_0000_TO_1970;
        // 找到基于3月的年份
        zeroDay -= 60;  // 调整到0000-03-01，使闰日位于四年周期的末尾
        long adjust = 0;
        if (zeroDay < 0) {
            // 调整负年份为正数以进行计算
            long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
            adjust = adjustCycles * 400;
            zeroDay += -adjustCycles * DAYS_PER_CYCLE;
        }
        long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        if (doyEst < 0) {
            // 修正估计值
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }
        yearEst += adjust;  // 重置任何负年份
        int marchDoy0 = (int) doyEst;

        // 将基于3月的值转换回基于1月的值
        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        int month = (marchMonth0 + 2) % 12 + 1;
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;

        // 现在确定年份是正确的，检查年份
        int year = YEAR.checkValidIntValue(yearEst);
        return new LocalDate(year, month, dom);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code LocalDate} 的实例。
     * <p>
     * 这根据指定的时间对象获取一个本地日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code LocalDate} 的实例。
     * <p>
     * 转换使用 {@link TemporalQueries#localDate()} 查询，该查询依赖于提取
     * {@link ChronoField#EPOCH_DAY EPOCH_DAY} 字段。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 功能接口的签名，允许通过方法引用使用它，{@code LocalDate::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 本地日期，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code LocalDate}
     */
    public static LocalDate from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalDate date = temporal.query(TemporalQueries.localDate());
        if (date == null) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 LocalDate: " +
                    temporal + " 类型为 " + temporal.getClass().getName());
        }
        return date;
    }


                //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12-03}）中获取 {@code LocalDate} 的实例。
     * <p>
     * 字符串必须表示一个有效的日期，并使用
     * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE} 进行解析。
     *
     * @param text 要解析的文本，如 "2007-12-03"，不能为空
     * @return 解析后的本地日期，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalDate parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 使用特定的格式化器从文本字符串中获取 {@code LocalDate} 的实例。
     * <p>
     * 文本使用格式化器进行解析，返回一个日期。
     *
     * @param text 要解析的文本，不能为空
     * @param formatter 要使用的格式化器，不能为空
     * @return 解析后的本地日期，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalDate::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 从年、月和日字段创建本地日期。
     *
     * @param year 要表示的年份，从 MIN_YEAR 到 MAX_YEAR 进行验证
     * @param month 要表示的月份，从 1 到 12 进行验证
     * @param dayOfMonth 要表示的月份中的日期，从 1 到 31 进行验证
     * @return 本地日期，不能为空
     * @throws DateTimeException 如果月份中的日期无效
     */
    private static LocalDate create(int year, int month, int dayOfMonth) {
        if (dayOfMonth > 28) {
            int dom = 31;
            switch (month) {
                case 2:
                    dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    dom = 30;
                    break;
            }
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw new DateTimeException("无效日期 '2月29日'，因为 '" + year + "' 不是闰年");
                } else {
                    throw new DateTimeException("无效日期 '" + Month.of(month).name() + " " + dayOfMonth + "'");
                }
            }
        }
        return new LocalDate(year, month, dayOfMonth);
    }

    /**
     * 解析日期，解决月份末尾的日期。
     *
     * @param year 要表示的年份，从 MIN_YEAR 到 MAX_YEAR 进行验证
     * @param month 要表示的月份，从 1 到 12 进行验证
     * @param day 要表示的月份中的日期，从 1 到 31 进行验证
     * @return 解析后的日期，不能为空
     */
    private static LocalDate resolvePreviousValid(int year, int month, int day) {
        switch (month) {
            case 2:
                day = Math.min(day, IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                day = Math.min(day, 30);
                break;
        }
        return new LocalDate(year, month, day);
    }

    /**
     * 构造函数，已验证。
     *
     * @param year 要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month 要表示的月份，不能为空
     * @param dayOfMonth 要表示的月份中的日期，对于年份和月份有效，从 1 到 31
     */
    private LocalDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = (short) month;
        this.day = (short) dayOfMonth;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这会检查此日期是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
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
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获得的。
     * 字段是否受支持由字段决定。
     *
     * @param field 要检查的字段，null 返回 false
     * @return 如果此日期支持该字段，则返回 true，否则返回 false
     */
    @Override  // 重写以提供 Javadoc
    public boolean isSupported(TemporalField field) {
        return ChronoLocalDate.super.isSupported(field);
    }

    /**
     * 检查指定的单位是否受支持。
     * <p>
     * 这会检查指定的单位是否可以添加到或从这个日期中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位有：
     * <ul>
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
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递来获得的。
     * 单位是否受支持由单位决定。
     *
     * @param unit 要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override  // 重写以提供 Javadoc
    public boolean isSupported(TemporalUnit unit) {
        return ChronoLocalDate.super.isSupported(unit);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此日期用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field 要查询范围的字段，不能为空
     * @return 字段的有效值范围，不能为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (f.isDateBased()) {
                switch (f) {
                    case DAY_OF_MONTH: return ValueRange.of(1, lengthOfMonth());
                    case DAY_OF_YEAR: return ValueRange.of(1, lengthOfYear());
                    case ALIGNED_WEEK_OF_MONTH: return ValueRange.of(1, getMonth() == Month.FEBRUARY && isLeapYear() == false ? 4 : 5);
                    case YEAR_OF_ERA:
                        return (getYear() <= 0 ? ValueRange.of(1, Year.MAX_VALUE + 1) : ValueRange.of(1, Year.MAX_VALUE));
                }
                return field.range();
            }
            throw new UnsupportedTemporalTypeException("不受支持的字段: " + field);
        }
        return field.rangeRefinedBy(this);
    }

    /**
     * 以 {@code int} 形式获取此日期中指定字段的值。
     * <p>
     * 这会查询此日期中指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此日期返回有效的值，
     * 但 {@code EPOCH_DAY} 和 {@code PROLEPTIC_MONTH} 太大而无法放入 {@code int} 中，将抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递来获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field 要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出该字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc 和性能
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return ChronoLocalDate.super.get(field);
    }

    /**
     * 以 {@code long} 形式获取此日期中指定字段的值。
     * <p>
     * 这会查询此日期中指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此日期返回有效的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递来获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field 要获取的字段，不能为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == EPOCH_DAY) {
                return toEpochDay();
            }
            if (field == PROLEPTIC_MONTH) {
                return getProlepticMonth();
            }
            return get0(field);
        }
        return field.getFrom(this);
    }

    private int get0(TemporalField field) {
        switch ((ChronoField) field) {
            case DAY_OF_WEEK: return getDayOfWeek().getValue();
            case ALIGNED_DAY_OF_WEEK_IN_MONTH: return ((day - 1) % 7) + 1;
            case ALIGNED_DAY_OF_WEEK_IN_YEAR: return ((getDayOfYear() - 1) % 7) + 1;
            case DAY_OF_MONTH: return day;
            case DAY_OF_YEAR: return getDayOfYear();
            case EPOCH_DAY: throw new UnsupportedTemporalTypeException("无效字段 'EpochDay'，请使用 getLong() 方法");
            case ALIGNED_WEEK_OF_MONTH: return ((day - 1) / 7) + 1;
            case ALIGNED_WEEK_OF_YEAR: return ((getDayOfYear() - 1) / 7) + 1;
            case MONTH_OF_YEAR: return month;
            case PROLEPTIC_MONTH: throw new UnsupportedTemporalTypeException("无效字段 'ProlepticMonth'，请使用 getLong() 方法");
            case YEAR_OF_ERA: return (year >= 1 ? year : 1 - year);
            case YEAR: return year;
            case ERA: return (year >= 1 ? 1 : 0);
        }
        throw new UnsupportedTemporalTypeException("不受支持的字段: " + field);
    }

    private long getProlepticMonth() {
        return (year * 12L + month - 1);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期的历法系统，即 ISO 日历系统。
     * <p>
     * {@code Chronology} 表示使用的历法系统。
     * ISO-8601 历法系统是当今世界上大多数地区使用的现代民用历法系统。
     * 它等同于公历系统，其中今天的闰年规则适用于所有时间。
     *
     * @return ISO 历法系统，不能为空
     */
    @Override
    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    /**
     * 获取此日期适用的纪元。
     * <p>
     * 官方 ISO-8601 标准不定义纪元，但 {@code IsoChronology} 定义了。
     * 它定义了两个纪元，'CE' 从公元 1 年开始，'BCE' 从公元 0 年向后。
     * 由于朱利安-格里高利历法转换前的日期与历史不符，
     * 'BCE' 和 'CE' 之间的转换也不与常用的纪元（通常称为 'BC' 和 'AD'）对齐。
     * <p>
     * 本类的用户通常应忽略此方法，因为它主要存在以满足
     * {@link ChronoLocalDate} 合约，其中需要支持日本历法系统。
     * <p>
     * 返回的纪元将是一个单例，可以使用 {@code ==} 运算符与
     * {@link IsoChronology} 中的常量进行比较。
     *
     * @return 适用于此日期的 {@code IsoChronology} 纪元常量，不能为空
     */
    @Override // 重写以提供 Javadoc
    public Era getEra() {
        return ChronoLocalDate.super.getEra();
    }


                /**
     * 获取年份字段。
     * <p>
     * 此方法返回年份的原始 {@code int} 值。
     * <p>
     * 通过此方法返回的年份是按 {@code get(YEAR)} 计算的。要获取纪年年份，请使用 {@code get(YEAR_OF_ERA)}。
     *
     * @return 年份，从 MIN_YEAR 到 MAX_YEAR
     */
    public int getYear() {
        return year;
    }

    /**
     * 获取 1 到 12 的月份字段。
     * <p>
     * 此方法返回 1 到 12 的 {@code int} 月份值。
     * 应用代码通常更清晰，如果调用 {@link #getMonth()} 使用枚举 {@link Month}。
     *
     * @return 1 到 12 的月份
     * @see #getMonth()
     */
    public int getMonthValue() {
        return month;
    }

    /**
     * 使用 {@code Month} 枚举获取月份字段。
     * <p>
     * 此方法返回月份的枚举 {@link Month}。
     * 这样可以避免对 {@code int} 值的混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue() int 值}。
     *
     * @return 月份，不为空
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return Month.of(month);
    }

    /**
     * 获取月份中的日期字段。
     * <p>
     * 此方法返回月份中的原始 {@code int} 值。
     *
     * @return 1 到 31 的月份中的日期
     */
    public int getDayOfMonth() {
        return day;
    }

    /**
     * 获取年份中的日期字段。
     * <p>
     * 此方法返回年份中的原始 {@code int} 值。
     *
     * @return 1 到 365，或闰年中的 366
     */
    public int getDayOfYear() {
        return getMonth().firstDayOfYear(isLeapYear()) + day - 1;
    }

    /**
     * 获取星期几字段，这是一个枚举 {@code DayOfWeek}。
     * <p>
     * 此方法返回星期几的枚举 {@link DayOfWeek}。
     * 这样可以避免对 {@code int} 值的混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link DayOfWeek#getValue() int 值}。
     * <p>
     * 可以从 {@code DayOfWeek} 获取更多详细信息，包括值的文本名称。
     *
     * @return 星期几，不为空
     */
    public DayOfWeek getDayOfWeek() {
        int dow0 = (int)Math.floorMod(toEpochDay() + 3, 7);
        return DayOfWeek.of(dow0 + 1);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查年份是否为闰年，根据 ISO 回归历法系统规则。
     * <p>
     * 此方法在整个时间线上应用当前的闰年规则。
     * 一般来说，如果年份能被四整除，则为闰年。但是，能被 100 整除的年份不是闰年，除非能被 400 整除。
     * <p>
     * 例如，1904 是闰年，因为它能被 4 整除。
     * 1900 不是闰年，因为它能被 100 整除，但 2000 是闰年，因为它能被 400 整除。
     * <p>
     * 计算是回归的——将相同的规则应用于遥远的未来和过去。
     * 这在历史上是不准确的，但符合 ISO-8601 标准。
     *
     * @return 如果年份是闰年，则返回 true，否则返回 false
     */
    @Override // 重写以提供 Javadoc 和性能
    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear(year);
    }

    /**
     * 返回此日期表示的月份长度。
     * <p>
     * 此方法返回月份的天数。
     * 例如，一月份的日期将返回 31。
     *
     * @return 月份的天数
     */
    @Override
    public int lengthOfMonth() {
        switch (month) {
            case 2:
                return (isLeapYear() ? 29 : 28);
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * 返回此日期表示的年份长度。
     * <p>
     * 此方法返回年份的天数，365 或 366。
     *
     * @return 如果年份是闰年，则返回 366，否则返回 365
     */
    @Override // 重写以提供 Javadoc 和性能
    public int lengthOfYear() {
        return (isLeapYear() ? 366 : 365);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回调整后的此日期的副本。
     * <p>
     * 此方法返回一个基于此日期的 {@code LocalDate}，日期已调整。
     * 调整使用指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 简单的调整器可能只是设置一个字段，例如年份字段。
     * 更复杂的调整器可能将日期设置为月份的最后一天。
     * <p>
     * 一些常见的调整器在
     * {@link java.time.temporal.TemporalAdjusters TemporalAdjusters} 中提供。
     * 这些包括“月份的最后一天”和“下一个星期三”。
     * 关键日期时间类也实现了 {@code TemporalAdjuster} 接口，例如 {@link Month} 和 {@link java.time.MonthDay MonthDay}。
     * 调整器负责处理特殊情况，例如月份长度的变化和闰年。
     * <p>
     * 例如，此代码返回 7 月最后一天的日期：
     * <pre>
     *  import static java.time.Month.*;
     *  import static java.time.temporal.TemporalAdjusters.*;
     *
     *  result = localDate.with(JULY).with(lastDayOfMonth());
     * </pre>
     * <p>
     * 通过调用指定调整器上的 {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不为空
     * @return 基于 {@code this} 并进行了调整的 {@code LocalDate}，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalDate) {
            return (LocalDate) adjuster;
        }
        return (LocalDate) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此日期的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此日期的 {@code LocalDate}，指定字段的值已更改。
     * 可以使用此方法更改任何支持的字段，例如年份、月份或月份中的日期。
     * 如果由于字段不支持或其他原因无法设置值，则会抛出异常。
     * <p>
     * 在某些情况下，更改指定字段可能导致结果日期无效，例如将 1 月 31 日的月份更改为 2 月会使月份中的日期无效。
     * 在这种情况下，字段负责解决日期。通常，它会选择前一个有效的日期，例如在此示例中选择 2 月的最后一天。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现调整。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code DAY_OF_WEEK} -
     *  返回一个具有指定星期几的 {@code LocalDate}。
     *  日期在周一到周日的范围内向前或向后调整最多 6 天。
     * <li>{@code ALIGNED_DAY_OF_WEEK_IN_MONTH} -
     *  返回一个具有指定对齐星期几的 {@code LocalDate}。
     *  日期调整为指定的月份对齐星期几。
     *  对齐周以给定月份的第一天开始。
     *  这可能导致日期移动到下个月最多 6 天。
     * <li>{@code ALIGNED_DAY_OF_WEEK_IN_YEAR} -
     *  返回一个具有指定对齐星期几的 {@code LocalDate}。
     *  日期调整为指定的年份对齐星期几。
     *  对齐周以给定年份的第一天开始。
     *  这可能导致日期移动到下一年最多 6 天。
     * <li>{@code DAY_OF_MONTH} -
     *  返回一个具有指定月份中的日期的 {@code LocalDate}。
     *  月份和年份将保持不变。如果月份中的日期对年份和月份无效，则抛出 {@code DateTimeException}。
     * <li>{@code DAY_OF_YEAR} -
     *  返回一个具有指定年份中的日期的 {@code LocalDate}。
     *  年份将保持不变。如果年份中的日期无效，则抛出 {@code DateTimeException}。
     * <li>{@code EPOCH_DAY} -
     *  返回一个具有指定纪元日的 {@code LocalDate}。
     *  这将完全替换日期，等同于 {@link #ofEpochDay(long)}。
     * <li>{@code ALIGNED_WEEK_OF_MONTH} -
     *  返回一个具有指定对齐周数的 {@code LocalDate}。
     *  对齐周以给定月份的第一天开始。
     *  此调整将日期以整周为单位移动以匹配指定的周。
     *  结果将具有与本日期相同的星期几。
     *  这可能导致日期移动到下个月。
     * <li>{@code ALIGNED_WEEK_OF_YEAR} -
     *  返回一个具有指定对齐周数的 {@code LocalDate}。
     *  对齐周以给定年份的第一天开始。
     *  此调整将日期以整周为单位移动以匹配指定的周。
     *  结果将具有与本日期相同的星期几。
     *  这可能导致日期移动到下一年。
     * <li>{@code MONTH_OF_YEAR} -
     *  返回一个具有指定月份的 {@code LocalDate}。
     *  年份将保持不变。月份中的日期也将保持不变，除非对新的月份和年份无效。在这种情况下，月份中的日期将调整为新的月份和年份的最大有效值。
     * <li>{@code PROLEPTIC_MONTH} -
     *  返回一个具有指定回归月的 {@code LocalDate}。
     *  月份中的日期将保持不变，除非对新的月份和年份无效。在这种情况下，月份中的日期将调整为新的月份和年份的最大有效值。
     * <li>{@code YEAR_OF_ERA} -
     *  返回一个具有指定纪年年的 {@code LocalDate}。
     *  纪年和月份将保持不变。月份中的日期也将保持不变，除非对新的月份和年份无效。在这种情况下，月份中的日期将调整为新的月份和年份的最大有效值。
     * <li>{@code YEAR} -
     *  返回一个具有指定年份的 {@code LocalDate}。
     *  月份将保持不变。月份中的日期也将保持不变，除非对新的月份和年份无效。在这种情况下，月份中的日期将调整为新的月份和年份的最大有效值。
     * <li>{@code ERA} -
     *  返回一个具有指定纪年的 {@code LocalDate}。
     *  纪年年和月份将保持不变。月份中的日期也将保持不变，除非对新的月份和年份无效。在这种情况下，月份中的日期将调整为新的月份和年份的最大有效值。
     * </ul>
     * <p>
     * 在所有情况下，如果新值超出字段的有效范围，则会抛出 {@code DateTimeException}。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.adjustInto(Temporal, long)}
     * 并传递 {@code this} 作为参数来获取此方法的结果。在这种情况下，字段确定是否以及如何调整日期。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不为空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code LocalDate}，不为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case DAY_OF_WEEK: return plusDays(newValue - getDayOfWeek().getValue());
                case ALIGNED_DAY_OF_WEEK_IN_MONTH: return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ALIGNED_DAY_OF_WEEK_IN_YEAR: return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case DAY_OF_MONTH: return withDayOfMonth((int) newValue);
                case DAY_OF_YEAR: return withDayOfYear((int) newValue);
                case EPOCH_DAY: return LocalDate.ofEpochDay(newValue);
                case ALIGNED_WEEK_OF_MONTH: return plusWeeks(newValue - getLong(ALIGNED_WEEK_OF_MONTH));
                case ALIGNED_WEEK_OF_YEAR: return plusWeeks(newValue - getLong(ALIGNED_WEEK_OF_YEAR));
                case MONTH_OF_YEAR: return withMonth((int) newValue);
                case PROLEPTIC_MONTH: return plusMonths(newValue - getProlepticMonth());
                case YEAR_OF_ERA: return withYear((int) (year >= 1 ? newValue : 1 - newValue));
                case YEAR: return withYear((int) newValue);
                case ERA: return (getLong(ERA) == newValue ? this : withYear(1 - year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code LocalDate} 的副本，年份已更改。
     * <p>
     * 如果月份中的日期对年份无效，它将更改为该月份的最后一天。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param year  要在结果中设置的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 基于此日期并设置了请求年份的 {@code LocalDate}，不为空
     * @throws DateTimeException 如果年份值无效
     */
    public LocalDate withYear(int year) {
        if (this.year == year) {
            return this;
        }
        YEAR.checkValidValue(year);
        return resolvePreviousValid(year, month, day);
    }


                /**
     * 返回一个副本，其中此 {@code LocalDate} 的月份已更改。
     * <p>
     * 如果该月的天数对于年份无效，它将更改为该月的最后一天。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param month  结果中要设置的月份，从 1（一月）到 12（十二月）
     * @return 基于此日期的 {@code LocalDate}，具有请求的月份，不为空
     * @throws DateTimeException 如果月份值无效
     */
    public LocalDate withMonth(int month) {
        if (this.month == month) {
            return this;
        }
        MONTH_OF_YEAR.checkValidValue(month);
        return resolvePreviousValid(year, month, day);
    }

    /**
     * 返回一个副本，其中此 {@code LocalDate} 的月中的天数已更改。
     * <p>
     * 如果结果日期无效，则会抛出异常。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param dayOfMonth  结果中要设置的月中的天数，从 1 到 28-31
     * @return 基于此日期的 {@code LocalDate}，具有请求的天数，不为空
     * @throws DateTimeException 如果月中的天数值无效，
     *  或者月中的天数对于月年无效
     */
    public LocalDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return of(year, month, dayOfMonth);
    }

    /**
     * 返回一个副本，其中此 {@code LocalDate} 的年中的天数已更改。
     * <p>
     * 如果结果日期无效，则会抛出异常。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param dayOfYear  结果中要设置的年中的天数，从 1 到 365-366
     * @return 基于此日期的 {@code LocalDate}，具有请求的天数，不为空
     * @throws DateTimeException 如果年中的天数值无效，
     *  或者年中的天数对于年份无效
     */
    public LocalDate withDayOfYear(int dayOfYear) {
        if (this.getDayOfYear() == dayOfYear) {
            return this;
        }
        return ofYearDay(year, dayOfYear);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要添加的数量。
     * <p>
     * 这返回一个基于此日期的 {@code LocalDate}，指定了要添加的数量。
     * 数量通常是 {@link Period}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是通过调用 {@link TemporalAmount#addTo(Temporal)} 委托给数量对象的。数量实现可以自由地以任何方式实现加法，但通常会回调到 {@link #plus(long, TemporalUnit)}。请参阅数量实现的文档，以确定是否可以成功添加。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加的数量，不为空
     * @return 基于此日期的 {@code LocalDate}，已进行加法，不为空
     * @throws DateTimeException 如果无法进行加法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period) amountToAdd;
            return plusMonths(periodToAdd.toTotalMonths()).plusDays(periodToAdd.getDays());
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (LocalDate) amountToAdd.addTo(this);
    }

    /**
     * 返回一个副本，其中指定了要添加的数量。
     * <p>
     * 这返回一个基于此日期的 {@code LocalDate}，以单位形式添加的数量。如果无法添加数量，因为单位不受支持或由于其他原因，将抛出异常。
     * <p>
     * 在某些情况下，添加数量可能会导致结果日期变得无效。例如，向 1 月 31 日添加一个月将导致 2 月 31 日。在这种情况下，单位负责解决日期。通常，它会选择前一个有效的日期，例如，在此示例中，2 月的最后一天。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则在此处实现加法。支持的字段表现如下：
     * <ul>
     * <li>{@code DAYS} -
     *  返回一个添加了指定天数的 {@code LocalDate}。
     *  这相当于 {@link #plusDays(long)}。
     * <li>{@code WEEKS} -
     *  返回一个添加了指定周数的 {@code LocalDate}。
     *  这相当于 {@link #plusWeeks(long)}，使用 7 天为一周。
     * <li>{@code MONTHS} -
     *  返回一个添加了指定月数的 {@code LocalDate}。
     *  这相当于 {@link #plusMonths(long)}。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * <li>{@code YEARS} -
     *  返回一个添加了指定年数的 {@code LocalDate}。
     *  这相当于 {@link #plusYears(long)}。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * <li>{@code DECADES} -
     *  返回一个添加了指定十年数的 {@code LocalDate}。
     *  这相当于调用 {@link #plusYears(long)}，数量乘以 10。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * <li>{@code CENTURIES} -
     *  返回一个添加了指定世纪数的 {@code LocalDate}。
     *  这相当于调用 {@link #plusYears(long)}，数量乘以 100。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * <li>{@code MILLENNIA} -
     *  返回一个添加了指定千年的 {@code LocalDate}。
     *  这相当于调用 {@link #plusYears(long)}，数量乘以 1,000。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * <li>{@code ERAS} -
     *  返回一个添加了指定纪元数的 {@code LocalDate}。
     *  只支持两个纪元，因此数量必须为 1、0 或 -1。
     *  如果数量非零，则年份将更改，使纪元中的年份保持不变。
     *  月中的天数将保持不变，除非它对于新的月份和年份无效。在这种情况下，月中的天数将调整为新的月份和年份的最大有效值。
     * </ul>
     * <p>
     * 所有其他 {@code ChronoUnit} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递来获取此方法的结果。在这种情况下，单位确定是否以及如何执行加法。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToAdd  要添加到结果中的单位数量，可以为负数
     * @param unit  要添加的数量的单位，不为空
     * @return 基于此日期的 {@code LocalDate}，已添加指定数量，不为空
     * @throws DateTimeException 如果无法进行加法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case DAYS: return plusDays(amountToAdd);
                case WEEKS: return plusWeeks(amountToAdd);
                case MONTHS: return plusMonths(amountToAdd);
                case YEARS: return plusYears(amountToAdd);
                case DECADES: return plusYears(Math.multiplyExact(amountToAdd, 10));
                case CENTURIES: return plusYears(Math.multiplyExact(amountToAdd, 100));
                case MILLENNIA: return plusYears(Math.multiplyExact(amountToAdd, 1000));
                case ERAS: return with(ERA, Math.addExact(getLong(ERA), amountToAdd));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要添加的年数。
     * <p>
     * 此方法通过以下三个步骤将指定数量添加到年份字段：
     * <ol>
     * <li>将输入年份添加到年份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的天数调整为最后一天</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）加上一年将导致无效日期 2009-02-29（平年）。为了避免返回无效结果，选择该月的最后一天，即 2009-02-28。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param yearsToAdd  要添加的年数，可以为负数
     * @return 基于此日期的 {@code LocalDate}，已添加年数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        int newYear = YEAR.checkValidIntValue(year + yearsToAdd);  // 安全溢出
        return resolvePreviousValid(newYear, month, day);
    }

    /**
     * 返回一个副本，其中指定了要添加的月数。
     * <p>
     * 此方法通过以下三个步骤将指定数量添加到月份字段：
     * <ol>
     * <li>将输入月份添加到月份字段</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月中的天数调整为最后一天</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 加上一个月将导致无效日期 2007-04-31。为了避免返回无效结果，选择该月的最后一天，即 2007-04-30。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param monthsToAdd  要添加的月数，可以为负数
     * @return 基于此日期的 {@code LocalDate}，已添加月数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + monthsToAdd;  // 安全溢出
        int newYear = YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12));
        int newMonth = (int)Math.floorMod(calcMonths, 12) + 1;
        return resolvePreviousValid(newYear, newMonth, day);
    }

    /**
     * 返回一个副本，其中指定了要添加的周数。
     * <p>
     * 此方法以周为单位将指定数量添加到天数字段，必要时递增月份和年份字段，以确保结果保持有效。结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2008-12-31 加上一周将导致 2009-01-07。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param weeksToAdd  要添加的周数，可以为负数
     * @return 基于此日期的 {@code LocalDate}，已添加周数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    /**
     * 返回一个副本，其中指定了要添加的天数。
     * <p>
     * 此方法将指定数量添加到天数字段，必要时递增月份和年份字段，以确保结果保持有效。结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2008-12-31 加上一天将导致 2009-01-01。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param daysToAdd  要添加的天数，可以为负数
     * @return 基于此日期的 {@code LocalDate}，已添加天数，不为空
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        long mjDay = Math.addExact(toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(mjDay);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的数量。
     * <p>
     * 这返回一个基于此日期的 {@code LocalDate}，指定了要减去的数量。
     * 数量通常是 {@link Period}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是通过调用 {@link TemporalAmount#subtractFrom(Temporal)} 委托给数量对象的。数量实现可以自由地以任何方式实现减法，但通常会回调到 {@link #minus(long, TemporalUnit)}。请参阅数量实现的文档，以确定是否可以成功减去。
     * <p>
     * 此实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于此日期的 {@code LocalDate}，已进行减法，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period) amountToSubtract;
            return minusMonths(periodToSubtract.toTotalMonths()).minusDays(periodToSubtract.getDays());
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (LocalDate) amountToSubtract.subtractFrom(this);
    }


                /**
     * 返回一个基于此日期并减去指定数量单位的副本。
     * <p>
     * 此方法返回一个基于此日期的 {@code LocalDate}，减去了指定单位的数量。如果由于单位不受支持或其他原因无法减去该数量，
     * 将抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。
     * 请参阅该方法以了解添加（以及因此减去）的工作原理。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  从结果中减去的单位数量，可以为负数
     * @param unit  要减去的单位，不为 null
     * @return 一个基于此日期并减去指定数量的 {@code LocalDate}，不为 null
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public LocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code LocalDate} 并减去指定年数的副本。
     * <p>
     * 此方法从年份字段中减去指定数量，分三个步骤进行：
     * <ol>
     * <li>从年份字段中减去输入的年数</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月份中的天数调整为最后一个有效天数</li>
     * </ol>
     * <p>
     * 例如，2008-02-29（闰年）减去一年将导致无效日期 2007-02-29（平年）。为了避免返回无效结果，
     * 选择该月的最后一天 2007-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param yearsToSubtract  要减去的年数，可以为负数
     * @return 一个基于此日期并减去指定年数的 {@code LocalDate}，不为 null
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract));
    }

    /**
     * 返回一个基于此 {@code LocalDate} 并减去指定月数的副本。
     * <p>
     * 此方法从月份字段中减去指定数量，分三个步骤进行：
     * <ol>
     * <li>从月份字段中减去输入的月数</li>
     * <li>检查结果日期是否无效</li>
     * <li>如有必要，将月份中的天数调整为最后一个有效天数</li>
     * </ol>
     * <p>
     * 例如，2007-03-31 减去一个月将导致无效日期 2007-02-31。为了避免返回无效结果，
     * 选择该月的最后一天 2007-02-28。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param monthsToSubtract  要减去的月数，可以为负数
     * @return 一个基于此日期并减去指定月数的 {@code LocalDate}，不为 null
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate minusMonths(long monthsToSubtract) {
        return (monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract));
    }

    /**
     * 返回一个基于此 {@code LocalDate} 并减去指定周数的副本。
     * <p>
     * 此方法从天数字段中减去指定数量的周数，必要时递减月份和年份字段以确保结果有效。
     * 结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2009-01-07 减去一周将导致 2008-12-31。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param weeksToSubtract  要减去的周数，可以为负数
     * @return 一个基于此日期并减去指定周数的 {@code LocalDate}，不为 null
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate minusWeeks(long weeksToSubtract) {
        return (weeksToSubtract == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeksToSubtract));
    }

    /**
     * 返回一个基于此 {@code LocalDate} 并减去指定天数的副本。
     * <p>
     * 此方法从天数字段中减去指定数量，必要时递减月份和年份字段以确保结果有效。
     * 结果仅在超过最大/最小年份时无效。
     * <p>
     * 例如，2009-01-01 减去一天将导致 2008-12-31。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param daysToSubtract  要减去的天数，可以为负数
     * @return 一个基于此日期并减去指定天数的 {@code LocalDate}，不为 null
     * @throws DateTimeException 如果结果超出支持的日期范围
     */
    public LocalDate minusDays(long daysToSubtract) {
        return (daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略查询此日期。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询的文档以了解此方法的结果。
     * <p>
     * 通过调用指定查询的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为 null
     * @return 查询结果，可能为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R) this;
        }
        return ChronoLocalDate.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有与此对象相同的日期。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的临时对象，但日期已更改为与此对象相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 并传递 {@link ChronoField#EPOCH_DAY} 作为字段。
     * <p>
     * 在大多数情况下，建议反转调用模式，使用 {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisLocalDate.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalDate);
     * </pre>
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param temporal  要调整的目标对象，不为 null
     * @return 调整后的对象，不为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc
    public Temporal adjustInto(Temporal temporal) {
        return ChronoLocalDate.super.adjustInto(temporal);
    }

    /**
     * 计算与此日期之间的指定单位的时间量。
     * <p>
     * 此方法计算两个 {@code LocalDate} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的日期。
     * 如果终点在起点之前，结果将为负数。
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为 {@code LocalDate}。
     * 例如，可以使用 {@code startDate.until(endDate, DAYS)} 计算两个日期之间的天数。
     * <p>
     * 计算返回一个整数，表示两个日期之间的完整单位数量。
     * 例如，2012-06-15 和 2012-08-14 之间的月数将仅为一个月，因为还差一天才满两个月。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更具可读性。
     * <p>
     * 该计算在 {@link ChronoUnit} 的实现中完成。
     * 支持的单位有 {@code DAYS}、{@code WEEKS}、{@code MONTHS}、{@code YEARS}、
     * {@code DECADES}、{@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。
     * 其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.between(Temporal, Temporal)} 并传递 {@code this} 作为第一个参数和转换后的输入临时对象作为第二个参数获得的。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param endExclusive  终点日期，不为 null，将转换为 {@code LocalDate}
     * @param unit  测量时间量的单位，不为 null
     * @return 从该日期到终点日期的时间量
     * @throws DateTimeException 如果无法计算时间量，或无法将终点临时对象转换为 {@code LocalDate}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDate end = LocalDate.from(endExclusive);
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
        return unit.between(this, end);
    }

    long daysUntil(LocalDate end) {
        return end.toEpochDay() - toEpochDay();  // 不会发生溢出
    }

    private long monthsUntil(LocalDate end) {
        long packed1 = getProlepticMonth() * 32L + getDayOfMonth();  // 不会发生溢出
        long packed2 = end.getProlepticMonth() * 32L + end.getDayOfMonth();  // 不会发生溢出
        return (packed2 - packed1) / 32;
    }

    /**
     * 计算此日期与另一日期之间的期间，作为 {@code Period}。
     * <p>
     * 此方法计算两个日期之间的年、月和天数。
     * 起点和终点分别是 {@code this} 和指定的日期。
     * 如果终点在起点之前，结果将为负数。
     * 年、月和天的负号将相同。
     * <p>
     * 计算使用 ISO 日历系统。
     * 如有必要，输入日期将转换为 ISO。
     * <p>
     * 起始日期包括在内，但终点日期不包括在内。
     * 期间通过移除完整的月份，然后计算剩余的天数，调整以确保两者具有相同的符号。
     * 年数和月数基于 12 个月的年份进行规范化。
     * 如果终点的月份中的天数大于或等于起点的月份中的天数，则认为该月份是完整的。
     * 例如，从 {@code 2010-01-15} 到 {@code 2011-03-18} 是 "1 年，2 个月和 3 天"。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link Period#between(LocalDate, LocalDate)}：
     * <pre>
     *   // 这两行是等效的
     *   period = start.until(end);
     *   period = Period.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更具可读性。
     *
     * @param endDateExclusive  终点日期，不为 null，可以是任何历法
     * @return 从该日期到终点日期的期间，不为 null
     */
    @Override
    public Period until(ChronoLocalDate endDateExclusive) {
        LocalDate end = LocalDate.from(endDateExclusive);
        long totalMonths = end.getProlepticMonth() - this.getProlepticMonth();  // 安全
        int days = end.day - this.day;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            LocalDate calcDate = this.plusMonths(totalMonths);
            days = (int) (end.toEpochDay() - calcDate.toEpochDay());  // 安全
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12;  // 安全
        int months = (int) (totalMonths % 12);  // 安全
        return Period.of(Math.toIntExact(years), months, days);
    }

    /**
     * 使用指定的格式化器格式化此日期。
     * <p>
     * 此日期将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为 null
     * @return 格式化的日期字符串，不为 null
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    @Override  // 重写以提供 Javadoc 和性能
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期与时间组合以创建一个 {@code LocalDateTime}。
     * <p>
     * 此方法返回一个基于此日期并指定时间的 {@code LocalDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param time  要组合的时间，不为 null
     * @return 一个基于此日期和指定时间的本地日期时间，不为 null
     */
    @Override
    public LocalDateTime atTime(LocalTime time) {
        return LocalDateTime.of(this, time);
    }


                /**
     * 将此日期与时间组合以创建一个 {@code LocalDateTime}。
     * <p>
     * 这将返回一个从该日期指定的小时和分钟组成的 {@code LocalDateTime}。
     * 秒和纳秒字段将被设置为零。
     * 单个时间字段必须在其有效范围内。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param hour  要使用的小时，从 0 到 23
     * @param minute  要使用的分钟，从 0 到 59
     * @return 由该日期和指定时间组成的地方日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public LocalDateTime atTime(int hour, int minute) {
        return atTime(LocalTime.of(hour, minute));
    }

    /**
     * 将此日期与时间组合以创建一个 {@code LocalDateTime}。
     * <p>
     * 这将返回一个从该日期指定的小时、分钟和秒组成的 {@code LocalDateTime}。
     * 纳秒字段将被设置为零。
     * 单个时间字段必须在其有效范围内。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param hour  要使用的小时，从 0 到 23
     * @param minute  要使用的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @return 由该日期和指定时间组成的地方日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public LocalDateTime atTime(int hour, int minute, int second) {
        return atTime(LocalTime.of(hour, minute, second));
    }

    /**
     * 将此日期与时间组合以创建一个 {@code LocalDateTime}。
     * <p>
     * 这将返回一个从该日期指定的小时、分钟、秒和纳秒组成的 {@code LocalDateTime}。
     * 单个时间字段必须在其有效范围内。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param hour  要使用的小时，从 0 到 23
     * @param minute  要使用的分钟，从 0 到 59
     * @param second  要表示的秒，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒，从 0 到 999,999,999
     * @return 由该日期和指定时间组成的地方日期时间，不为空
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond) {
        return atTime(LocalTime.of(hour, minute, second, nanoOfSecond));
    }

    /**
     * 将此日期与带偏移的时间组合以创建一个 {@code OffsetDateTime}。
     * <p>
     * 这将返回一个从该日期指定的时间组成的 {@code OffsetDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param time  要组合的时间，不为空
     * @return 由该日期和指定时间组成的带偏移的日期时间，不为空
     */
    public OffsetDateTime atTime(OffsetTime time) {
        return OffsetDateTime.of(LocalDateTime.of(this, time.toLocalTime()), time.getOffset());
    }

    /**
     * 将此日期与午夜时间组合以创建一个 {@code LocalDateTime}，表示该日期的开始时间。
     * <p>
     * 这将返回一个从该日期午夜时间 00:00 组成的 {@code LocalDateTime}，表示该日期的开始时间。
     *
     * @return 该日期开始时间的午夜地方日期时间，不为空
     */
    public LocalDateTime atStartOfDay() {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT);
    }

    /**
     * 根据时区规则返回该日期在该时区最早有效时间的带时区的日期时间。
     * <p>
     * 时区规则，如夏令时，意味着不是每个地方日期时间在指定的时区都是有效的，因此该地方日期时间可能不是午夜。
     * <p>
     * 在大多数情况下，每个地方日期时间只有一个有效的偏移量。
     * 在重叠的情况下，有两个有效的偏移量，使用较早的一个，对应于该日期午夜的第一次出现。
     * 在间隔的情况下，带时区的日期时间将表示间隔之后的瞬间。
     * <p>
     * 如果时区 ID 是 {@link ZoneOffset}，则结果总是午夜时间。
     * <p>
     * 要在给定时区中转换为特定时间，请调用 {@link #atTime(LocalTime)}，然后调用 {@link LocalDateTime#atZone(ZoneId)}。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 由该日期和该时区最早有效时间组成的带时区的日期时间，不为空
     */
    public ZonedDateTime atStartOfDay(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        // 需要处理从 11:30 到 00:30 的间隔情况
        // 标准 ZDT 工厂将导致 01:00 而不是 00:30
        LocalDateTime ldt = atTime(LocalTime.MIDNIGHT);
        if (zone instanceof ZoneOffset == false) {
            ZoneRules rules = zone.getRules();
            ZoneOffsetTransition trans = rules.getTransition(ldt);
            if (trans != null && trans.isGap()) {
                ldt = trans.getDateTimeAfter();
            }
        }
        return ZonedDateTime.of(ldt, zone);
    }

    //-----------------------------------------------------------------------
    @Override
    public long toEpochDay() {
        long y = year;
        long m = month;
        long total = 0;
        total += 365 * y;
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        } else {
            total -= y / -4 - y / -100 + y / -400;
        }
        total += ((367 * m - 362) / 12);
        total += day - 1;
        if (m > 2) {
            total--;
            if (isLeapYear() == false) {
                total--;
            }
        }
        return total - DAYS_0000_TO_1970;
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此日期与另一个日期。
     * <p>
     * 比较主要基于日期，从最早到最晚。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义。
     * <p>
     * 如果所有被比较的日期都是 {@code LocalDate} 的实例，则比较将完全基于日期。
     * 如果一些被比较的日期在不同的历法系统中，则还会考虑历法系统，参见 {@link java.time.chrono.ChronoLocalDate#compareTo}。
     *
     * @param other  要比较的其他日期，不为空
     * @return 比较值，负数表示小于，正数表示大于
     */
    @Override  // 重写以提供 Javadoc 和性能
    public int compareTo(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other);
        }
        return ChronoLocalDate.super.compareTo(other);
    }

    int compareTo0(LocalDate otherDate) {
        int cmp = (year - otherDate.year);
        if (cmp == 0) {
            cmp = (month - otherDate.month);
            if (cmp == 0) {
                cmp = (day - otherDate.day);
            }
        }
        return cmp;
    }

    /**
     * 检查此日期是否在指定日期之后。
     * <p>
     * 这会检查此日期是否在其他日期之后。
     * <pre>
     *   LocalDate a = LocalDate.of(2012, 6, 30);
     *   LocalDate b = LocalDate.of(2012, 7, 1);
     *   a.isAfter(b) == false
     *   a.isAfter(a) == false
     *   b.isAfter(a) == true
     * </pre>
     * <p>
     * 此方法仅考虑两个日期在本地时间线上的位置。
     * 它不考虑历法系统或日历系统。
     * 这与 {@link #compareTo(ChronoLocalDate)} 中的比较不同，但与 {@link ChronoLocalDate#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期，不为空
     * @return 如果此日期在指定日期之后，返回 true
     */
    @Override  // 重写以提供 Javadoc 和性能
    public boolean isAfter(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other) > 0;
        }
        return ChronoLocalDate.super.isAfter(other);
    }

    /**
     * 检查此日期是否在指定日期之前。
     * <p>
     * 这会检查此日期是否在其他日期之前。
     * <pre>
     *   LocalDate a = LocalDate.of(2012, 6, 30);
     *   LocalDate b = LocalDate.of(2012, 7, 1);
     *   a.isBefore(b) == true
     *   a.isBefore(a) == false
     *   b.isBefore(a) == false
     * </pre>
     * <p>
     * 此方法仅考虑两个日期在本地时间线上的位置。
     * 它不考虑历法系统或日历系统。
     * 这与 {@link #compareTo(ChronoLocalDate)} 中的比较不同，但与 {@link ChronoLocalDate#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期，不为空
     * @return 如果此日期在指定日期之前，返回 true
     */
    @Override  // 重写以提供 Javadoc 和性能
    public boolean isBefore(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other) < 0;
        }
        return ChronoLocalDate.super.isBefore(other);
    }

    /**
     * 检查此日期是否等于指定日期。
     * <p>
     * 这会检查此日期是否在本地时间线上与另一个日期表示相同的点。
     * <pre>
     *   LocalDate a = LocalDate.of(2012, 6, 30);
     *   LocalDate b = LocalDate.of(2012, 7, 1);
     *   a.isEqual(b) == false
     *   a.isEqual(a) == true
     *   b.isEqual(a) == false
     * </pre>
     * <p>
     * 此方法仅考虑两个日期在本地时间线上的位置。
     * 它不考虑历法系统或日历系统。
     * 这与 {@link #compareTo(ChronoLocalDate)} 中的比较不同，但与 {@link ChronoLocalDate#timeLineOrder()} 的方法相同。
     *
     * @param other  要比较的其他日期，不为空
     * @return 如果此日期等于指定日期，返回 true
     */
    @Override  // 重写以提供 Javadoc 和性能
    public boolean isEqual(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other) == 0;
        }
        return ChronoLocalDate.super.isEqual(other);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期是否等于另一个日期。
     * <p>
     * 比较此 {@code LocalDate} 与另一个，确保日期相同。
     * <p>
     * 仅比较 {@code LocalDate} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同历法系统的日期，使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期等于其他日期，返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDate) {
            return compareTo0((LocalDate) obj) == 0;
        }
        return false;
    }

    /**
     * 返回此日期的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        int yearValue = year;
        int monthValue = month;
        int dayValue = day;
        return (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (monthValue << 6) + (dayValue));
    }

    //-----------------------------------------------------------------------
    /**
     * 以 {@code String} 形式输出此日期，例如 {@code 2007-12-03}。
     * <p>
     * 输出将采用 ISO-8601 格式 {@code uuuu-MM-dd}。
     *
     * @return 此日期的字符串表示，不为空
     */
    @Override
    public String toString() {
        int yearValue = year;
        int monthValue = month;
        int dayValue = day;
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(10);
        if (absYear < 1000) {
            if (yearValue < 0) {
                buf.append(yearValue - 10000).deleteCharAt(1);
            } else {
                buf.append(yearValue + 10000).deleteCharAt(0);
            }
        } else {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        }
        return buf.append(monthValue < 10 ? "-0" : "-")
            .append(monthValue)
            .append(dayValue < 10 ? "-0" : "-")
            .append(dayValue)
            .toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(3);  // 标识一个 LocalDate
     *  out.writeInt(year);
     *  out.writeByte(month);
     *  out.writeByte(day);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.LOCAL_DATE_TYPE, this);
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

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(year);
        out.writeByte(month);
        out.writeByte(day);
    }

    static LocalDate readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        int month = in.readByte();
        int dayOfMonth = in.readByte();
        return LocalDate.of(year, month, dayOfMonth);
    }

}
