
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

import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;
import static java.time.temporal.ChronoUnit.CENTURIES;
import static java.time.temporal.ChronoUnit.DECADES;
import static java.time.temporal.ChronoUnit.ERAS;
import static java.time.temporal.ChronoUnit.MILLENNIA;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
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
import java.util.Objects;

/**
 * ISO-8601 日历系统中的年月，例如 {@code 2007-12}。
 * <p>
 * {@code YearMonth} 是一个不可变的日期时间对象，表示年和月的组合。可以从年和月中派生出的所有字段，例如年中的季度，都可以获取。
 * <p>
 * 该类不存储或表示日期、时间或时区。例如，值 "2007 年 10 月" 可以存储在 {@code YearMonth} 中。
 * <p>
 * ISO-8601 日历系统是当今世界上大多数地区使用的现代民用日历系统。它等同于公历系统，其中今天的闰年规则适用于所有时间。对于今天编写的大多数应用程序，ISO-8601 规则完全适用。然而，任何使用历史日期并要求其准确性的应用程序将发现 ISO-8601 方法不适合。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code YearMonth} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class YearMonth
        implements Temporal, TemporalAdjuster, Comparable<YearMonth>, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 4183400860270640070L;
    /**
     * 解析器。
     */
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendLiteral('-')
        .appendValue(MONTH_OF_YEAR, 2)
        .toFormatter();

    /**
     * 年份。
     */
    private final int year;
    /**
     * 月份，不能为空。
     */
    private final int month;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前年月。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前年月。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前年月，不能为空
     */
    public static YearMonth now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前年月。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前年月。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不能为空
     * @return 使用系统时钟的当前年月，不能为空
     */
    public static YearMonth now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前年月。
     * <p>
     * 这将查询指定的时钟以获取当前年月。
     * 使用此方法可以使用替代时钟进行测试。
     * 替代时钟可以通过 {@link Clock 依赖注入} 引入。
     *
     * @param clock  要使用的时钟，不能为空
     * @return 当前年月，不能为空
     */
    public static YearMonth now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);  // 调用一次
        return YearMonth.of(now.getYear(), now.getMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * 从年份和月份获取 {@code YearMonth} 的实例。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，不能为空
     * @return 年月，不能为空
     * @throws DateTimeException 如果年份值无效
     */
    public static YearMonth of(int year, Month month) {
        Objects.requireNonNull(month, "month");
        return of(year, month.getValue());
    }

    /**
     * 从年份和月份获取 {@code YearMonth} 的实例。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR
     * @param month  要表示的月份，从 1（1月）到 12（12月）
     * @return 年月，不能为空
     * @throws DateTimeException 如果任一字段值无效
     */
    public static YearMonth of(int year, int month) {
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(month);
        return new YearMonth(year, month);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code YearMonth} 的实例。
     * <p>
     * 这将根据指定的时间对象获取年月。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code YearMonth} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#YEAR YEAR} 和
     * {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 字段。
     * 仅当时间对象具有 ISO
     * 历法，或可以转换为 {@code LocalDate} 时，提取才是允许的。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 功能接口的签名
     * 允许其通过方法引用作为查询使用，{@code YearMonth::from}。
     *
     * @param temporal  要转换的时间对象，不能为空
     * @return 年月，不能为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code YearMonth}
     */
    public static YearMonth from(TemporalAccessor temporal) {
        if (temporal instanceof YearMonth) {
            return (YearMonth) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(YEAR), temporal.get(MONTH_OF_YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 YearMonth: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12}）获取 {@code YearMonth} 的实例。
     * <p>
     * 字符串必须表示一个有效的年月。
     * 格式必须为 {@code uuuu-MM}。
     * 范围在 0000 到 9999 之外的年份必须以加号或减号为前缀。
     *
     * @param text  要解析的文本，如 "2007-12"，不能为空
     * @return 解析的年月，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static YearMonth parse(CharSequence text) {
        return parse(text, PARSER);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code YearMonth} 的实例。
     * <p>
     * 文本使用格式化器解析，返回一个年月。
     *
     * @param text  要解析的文本，不能为空
     * @param formatter  要使用的格式化器，不能为空
     * @return 解析的年月，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static YearMonth parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, YearMonth::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param year  要表示的年份，从 MIN_YEAR 到 MAX_YEAR 验证
     * @param month  要表示的月份，从 1（1月）到 12（12月）验证
     */
    private YearMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }

    /**
     * 返回一个带有新年份和月份的副本，检查是否确实需要创建新对象。
     *
     * @param newYear  要表示的年份，从 MIN_YEAR 到 MAX_YEAR 验证
     * @param newMonth  要表示的月份，验证不能为空
     * @return 年月，不能为空
     */
    private YearMonth with(int newYear, int newMonth) {
        if (year == newYear && month == newMonth) {
            return this;
        }
        return new YearMonth(newYear, newMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这检查此年月是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code MONTH_OF_YEAR}
     * <li>{@code PROLEPTIC_MONTH}
     * <li>{@code YEAR_OF_ERA}
     * <li>{@code YEAR}
     * <li>{@code ERA}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 字段是否受支持由字段确定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此年月上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == YEAR || field == MONTH_OF_YEAR ||
                    field == PROLEPTIC_MONTH || field == YEAR_OF_ERA || field == ERA;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否受支持。
     * <p>
     * 这检查指定的单位是否可以添加到或从该年月中减去。
     * 如果为 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位有：
     * <ul>
     * <li>{@code MONTHS}
     * <li>{@code YEARS}
     * <li>{@code DECADES}
     * <li>{@code CENTURIES}
     * <li>{@code MILLENNIA}
     * <li>{@code ERAS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递来获得。
     * 单位是否受支持由单位确定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit == MONTHS || unit == YEARS || unit == DECADES || unit == CENTURIES || unit == MILLENNIA || unit == ERAS;
        }
        return unit != null && unit.isSupportedBy(this);
    }


                //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此年月用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得。
     * 是否可以获取范围由字段决定。
     *
     * @param field  要查询范围的字段，不得为 null
     * @return 字段的有效值范围，不得为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == YEAR_OF_ERA) {
            return (getYear() <= 0 ? ValueRange.of(1, Year.MAX_VALUE + 1) : ValueRange.of(1, Year.MAX_VALUE));
        }
        return Temporal.super.range(field);
    }

    /**
     * 从这个年月获取指定字段的值，作为 {@code int}。
     * <p>
     * 此方法查询此年月指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此年月的有效值，但 {@code PROLEPTIC_MONTH} 太大，无法放入 {@code int} 中，会抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得。是否可以获取值，以及值的含义，由字段决定。
     *
     * @param field  要获取的字段，不得为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值，或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持，或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // override for Javadoc
    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    /**
     * 从这个年月获取指定字段的值，作为 {@code long}。
     * <p>
     * 此方法查询此年月指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此年月的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得。是否可以获取值，以及值的含义，由字段决定。
     *
     * @param field  要获取的字段，不得为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case MONTH_OF_YEAR: return month;
                case PROLEPTIC_MONTH: return getProlepticMonth();
                case YEAR_OF_ERA: return (year < 1 ? 1 - year : year);
                case YEAR: return year;
                case ERA: return (year < 1 ? 0 : 1);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    private long getProlepticMonth() {
        return (year * 12L + month - 1);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取年份字段。
     * <p>
     * 此方法返回年份的原始 {@code int} 值。
     * <p>
     * 通过 {@code get(YEAR)} 返回的年份是历法年。
     *
     * @return 年份，从 MIN_YEAR 到 MAX_YEAR
     */
    public int getYear() {
        return year;
    }

    /**
     * 获取月份字段，从 1 到 12。
     * <p>
     * 此方法返回月份的 {@code int} 值，从 1 到 12。
     * 通常情况下，调用 {@link #getMonth()} 使用枚举 {@link Month} 会使代码更清晰。
     *
     * @return 月份，从 1 到 12
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
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue()} 方法。
     *
     * @return 月份，不得为 null
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return Month.of(month);
    }

    //-----------------------------------------------------------------------
    /**
     * 根据 ISO 历法系统规则检查年份是否为闰年。
     * <p>
     * 此方法在整个时间线上应用当前的闰年规则。
     * 一般来说，能被 4 整除的年份是闰年。但是，能被 100 整除的年份不是闰年，除非能被 400 整除。
     * <p>
     * 例如，1904 年是闰年，因为它能被 4 整除。
     * 1900 年不是闰年，因为它能被 100 整除，但 2000 年是闰年，因为它能被 400 整除。
     * <p>
     * 计算是历法的——将相同的规则应用于遥远的未来和遥远的过去。
     * 这在历史上是不准确的，但符合 ISO-8601 标准。
     *
     * @return 如果年份是闰年返回 true，否则返回 false
     */
    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear(year);
    }

    /**
     * 检查给定的月份中的日期是否有效。
     * <p>
     * 此方法检查此年月和输入的日期是否构成一个有效的日期。
     *
     * @param dayOfMonth  要验证的月份中的日期，从 1 到 31，无效值返回 false
     * @return 如果日期在该年月有效返回 true
     */
    public boolean isValidDay(int dayOfMonth) {
        return dayOfMonth >= 1 && dayOfMonth <= lengthOfMonth();
    }

    /**
     * 返回该月的长度，考虑年份。
     * <p>
     * 此方法返回该月的天数。
     * 例如，1 月会返回 31。
     *
     * @return 该月的天数，从 28 到 31
     */
    public int lengthOfMonth() {
        return getMonth().length(isLeapYear());
    }

    /**
     * 返回该年的长度。
     * <p>
     * 此方法返回该年的天数，365 或 366。
     *
     * @return 如果是闰年返回 366，否则返回 365
     */
    public int lengthOfYear() {
        return (isLeapYear() ? 366 : 365);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回调整后的年月副本。
     * <p>
     * 此方法返回一个基于此年月的 {@code YearMonth}，年月已根据指定的调整器策略对象进行了调整。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 简单的调整器可能只是设置一个字段，如年份字段。
     * 更复杂的调整器可能将年月设置为哈雷彗星下次经过地球的月份。
     * <p>
     * 此方法的结果是通过调用指定调整器的
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数获得的。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不得为 null
     * @return 基于 {@code this} 并进行了调整的 {@code YearMonth}，不得为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth with(TemporalAdjuster adjuster) {
        return (YearMonth) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此年月的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此年月的 {@code YearMonth}，指定字段的值已更改。
     * 可以使用此方法更改任何支持的字段，如年份或月份。
     * 如果由于字段不受支持或其他原因无法设置值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code MONTH_OF_YEAR} -
     *  返回一个具有指定月份的 {@code YearMonth}。
     *  年份将保持不变。
     * <li>{@code PROLEPTIC_MONTH} -
     *  返回一个具有指定历法月份的 {@code YearMonth}。
     *  这将完全替换此对象的年份和月份。
     * <li>{@code YEAR_OF_ERA} -
     *  返回一个具有指定纪年年份的 {@code YearMonth}。
     *  月份和纪年将保持不变。
     * <li>{@code YEAR} -
     *  返回一个具有指定年份的 {@code YearMonth}。
     *  月份将保持不变。
     * <li>{@code ERA} -
     *  返回一个具有指定纪年的 {@code YearMonth}。
     *  月份和纪年年份将保持不变。
     * </ul>
     * <p>
     * 在所有情况下，如果新值超出字段的有效值范围，则会抛出 {@code DateTimeException}。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 并将 {@code this} 作为参数传递获得。在这种情况下，字段决定是否以及如何调整实例。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不得为 null
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code YearMonth}，不得为 null
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case MONTH_OF_YEAR: return withMonth((int) newValue);
                case PROLEPTIC_MONTH: return plusMonths(newValue - getProlepticMonth());
                case YEAR_OF_ERA: return withYear((int) (year < 1 ? 1 - newValue : newValue));
                case YEAR: return withYear((int) newValue);
                case ERA: return (getLong(ERA) == newValue ? this : withYear(1 - year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此 {@code YearMonth} 的副本，年份已更改。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param year  要在返回的年月中设置的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 基于此年月并设置了请求年份的 {@code YearMonth}，不得为 null
     * @throws DateTimeException 如果年份值无效
     */
    public YearMonth withYear(int year) {
        YEAR.checkValidValue(year);
        return with(year, month);
    }

    /**
     * 返回一个基于此 {@code YearMonth} 的副本，月份已更改。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param month  要在返回的年月中设置的月份，从 1（1 月）到 12（12 月）
     * @return 基于此年月并设置了请求月份的 {@code YearMonth}，不得为 null
     * @throws DateTimeException 如果月份值无效
     */
    public YearMonth withMonth(int month) {
        MONTH_OF_YEAR.checkValidValue(month);
        return with(year, month);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此年月的副本，指定了添加的量。
     * <p>
     * 此方法返回一个基于此年月的 {@code YearMonth}，指定了添加的量。
     * 该量通常是 {@link Period}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是通过调用 {@link TemporalAmount#addTo(Temporal)} 委托给量对象的。
     * 量实现可以自由地以任何方式实现添加，但通常会回调 {@link #plus(long, TemporalUnit)}。
     * 请参阅量实现的文档以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加的量，不得为 null
     * @return 基于此年月并进行了添加的 {@code YearMonth}，不得为 null
     * @throws DateTimeException 如果无法进行添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth plus(TemporalAmount amountToAdd) {
        return (YearMonth) amountToAdd.addTo(this);
    }


                /**
     * 返回一个副本，其中指定了要添加的量。
     * <p>
     * 此方法返回一个基于此对象的 {@code YearMonth}，并添加了指定单位的量。如果由于单位不受支持或其他原因无法添加该量，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则在此处实现添加。支持的字段行为如下：
     * <ul>
     * <li>{@code MONTHS} -
     *  返回一个添加了指定月份数的 {@code YearMonth}。
     *  这等同于 {@link #plusMonths(long)}。
     * <li>{@code YEARS} -
     *  返回一个添加了指定年份数的 {@code YearMonth}。
     *  这等同于 {@link #plusYears(long)}。
     * <li>{@code DECADES} -
     *  返回一个添加了指定十年数的 {@code YearMonth}。
     *  这等同于调用 {@link #plusYears(long)}，并将数量乘以 10。
     * <li>{@code CENTURIES} -
     *  返回一个添加了指定世纪数的 {@code YearMonth}。
     *  这等同于调用 {@link #plusYears(long)}，并将数量乘以 100。
     * <li>{@code MILLENNIA} -
     *  返回一个添加了指定千年数的 {@code YearMonth}。
     *  这等同于调用 {@link #plusYears(long)}，并将数量乘以 1,000。
     * <li>{@code ERAS} -
     *  返回一个添加了指定纪元数的 {@code YearMonth}。
     *  仅支持两个纪元，因此数量必须是一、零或负一。
     *  如果数量非零，则年份将更改，以使纪元中的年份保持不变。
     * </ul>
     * <p>
     * 所有其他 {@code ChronoUnit} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.addTo(Temporal, long)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。在这种情况下，单位确定是否以及如何执行添加。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加到结果中的单位数量，可以为负数
     * @param unit  要添加的单位，不为空
     * @return 基于此年月并添加了指定数量的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果无法进行添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
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

    /**
     * 返回一个副本，其中指定了要添加的年份数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param yearsToAdd  要添加的年份数，可以为负数
     * @return 基于此年月并添加了年份数的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public YearMonth plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        int newYear = YEAR.checkValidIntValue(year + yearsToAdd);  // 安全溢出
        return with(newYear, month);
    }

    /**
     * 返回一个副本，其中指定了要添加的月份数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param monthsToAdd  要添加的月份数，可以为负数
     * @return 基于此年月并添加了月份数的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public YearMonth plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + monthsToAdd;  // 安全溢出
        int newYear = YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12));
        int newMonth = (int)Math.floorMod(calcMonths, 12) + 1;
        return with(newYear, newMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的量。
     * <p>
     * 此方法返回一个基于此对象的 {@code YearMonth}，并减去了指定的量。该量通常是 {@link Period}，但可以是实现
     * {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给量对象，通过调用 {@link TemporalAmount#subtractFrom(Temporal)}。量实现可以自由地以任何方式实现减法，
     * 但通常会回调 {@link #minus(long, TemporalUnit)}。请参阅量实现的文档，以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要减去的量，不为空
     * @return 基于此年月并进行了减法的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth minus(TemporalAmount amountToSubtract) {
        return (YearMonth) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，其中指定了要减去的量。
     * <p>
     * 此方法返回一个基于此对象的 {@code YearMonth}，并减去了指定单位的量。如果由于单位不受支持或其他原因无法减去该量，
     * 则会抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。请参阅该方法以了解添加（从而减法）的工作方式。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要从结果中减去的单位数量，可以为负数
     * @param unit  要减去的单位，不为空
     * @return 基于此年月并减去了指定数量的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public YearMonth minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    /**
     * 返回一个副本，其中指定了要减去的年份数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param yearsToSubtract  要减去的年份数，可以为负数
     * @return 基于此年月并减去了年份数的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public YearMonth minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract));
    }

    /**
     * 返回一个副本，其中指定了要减去的月份数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param monthsToSubtract  要减去的月份数，可以为负数
     * @return 基于此年月并减去了月份数的 {@code YearMonth}，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public YearMonth minusMonths(long monthsToSubtract) {
        return (monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略查询此年月。
     * <p>
     * 此方法使用指定的查询策略对象查询此年月。{@code TemporalQuery} 对象定义了获取结果的逻辑。
     * 请阅读查询的文档，以了解此方法的结果是什么。
     * <p>
     * 本方法的结果是通过调用 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        } else if (query == TemporalQueries.precision()) {
            return (R) MONTHS;
        }
        return Temporal.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此年月。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的临时对象，年月更改为与此相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 并传递 {@link ChronoField#PROLEPTIC_MONTH} 作为字段。
     * 如果指定的时间对象不使用 ISO 日历系统，则会抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，反转调用模式更清晰，建议使用 {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisYearMonth.adjustInto(temporal);
     *   temporal = temporal.with(thisYearMonth);
     * </pre>
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE) == false) {
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        return temporal.with(PROLEPTIC_MONTH, getProlepticMonth());
    }

    /**
     * 计算到另一个年月的时间量，以指定单位表示。
     * <p>
     * 此方法计算两个 {@code YearMonth} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的年月。
     * 如果终点在起点之前，结果将为负数。
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为 {@code YearMonth}。
     * 例如，可以使用 {@code startYearMonth.until(endYearMonth, YEARS)} 计算两个年月之间的年数。
     * <p>
     * 计算返回一个整数，表示两个年月之间的完整单位数。
     * 例如，2012-06 和 2032-05 之间的十年数将仅为一个十年，因为还差一个月才满两个十年。
     * <p>
     * 有两种等效的方法可以使用此方法。第一种是调用此方法。第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更易读。
     * <p>
     * 计算是在此方法中为 {@link ChronoUnit} 实现的。支持的单位有 {@code MONTHS}、{@code YEARS}、{@code DECADES}、
     * {@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并将 {@code this} 作为第一个参数和转换后的输入时间作为第二个参数传递来获取此方法的结果。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param endExclusive  终点，不为空，将转换为 {@code YearMonth}
     * @param unit  要测量的时间单位，不为空
     * @return 从本年月到终点年月的时间量
     * @throws DateTimeException 如果无法计算时间量，或无法将终点时间转换为 {@code YearMonth}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        YearMonth end = YearMonth.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            long monthsUntil = end.getProlepticMonth() - getProlepticMonth();  // 不会溢出
            switch ((ChronoUnit) unit) {
                case MONTHS: return monthsUntil;
                case YEARS: return monthsUntil / 12;
                case DECADES: return monthsUntil / 120;
                case CENTURIES: return monthsUntil / 1200;
                case MILLENNIA: return monthsUntil / 12000;
                case ERAS: return end.getLong(ERA) - getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化器格式化此年月。
     * <p>
     * 此年月将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的年月字符串，不为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }


                //-----------------------------------------------------------------------
    /**
     * 将此年月与月中的某一天结合，创建一个 {@code LocalDate}。
     * <p>
     * 这将返回一个由该年月和指定的月中的某一天组成的 {@code LocalDate}。
     * <p>
     * 月中的某一天的值必须对该年月有效。
     * <p>
     * 该方法可以作为链的一部分来生成日期：
     * <pre>
     *  LocalDate date = year.atMonth(month).atDay(day);
     * </pre>
     *
     * @param dayOfMonth  要使用的月中的某一天，从 1 到 31
     * @return 由该年月和指定的月中的某一天组成的日期，不为空
     * @throws DateTimeException 如果该天对年月无效
     * @see #isValidDay(int)
     */
    public LocalDate atDay(int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * 返回该月的最后一天的 {@code LocalDate}。
     * <p>
     * 这将返回一个基于该年月的 {@code LocalDate}。
     * 月中的某一天被设置为该月的最后一天，考虑到闰年。
     * <p>
     * 该方法可以作为链的一部分来生成日期：
     * <pre>
     *  LocalDate date = year.atMonth(month).atEndOfMonth();
     * </pre>
     *
     * @return 该年月的最后一天，不为空
     */
    public LocalDate atEndOfMonth() {
        return LocalDate.of(year, month, lengthOfMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此年月与另一个年月。
     * <p>
     * 比较首先基于年份的值，然后基于月份的值。
     * 它与 {@link Comparable} 定义的“与 equals 一致”。
     *
     * @param other  要比较的另一个年月，不为空
     * @return 比较值，小于 0 表示较小，大于 0 表示较大
     */
    @Override
    public int compareTo(YearMonth other) {
        int cmp = (year - other.year);
        if (cmp == 0) {
            cmp = (month - other.month);
        }
        return cmp;
    }

    /**
     * 检查此年月是否在指定的年月之后。
     *
     * @param other  要比较的另一个年月，不为空
     * @return 如果此年月在指定的年月之后，返回 true
     */
    public boolean isAfter(YearMonth other) {
        return compareTo(other) > 0;
    }

    /**
     * 检查此年月是否在指定的年月之前。
     *
     * @param other  要比较的另一个年月，不为空
     * @return 如果此年月在指定的年月之前，返回 true
     */
    public boolean isBefore(YearMonth other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此年月是否等于另一个年月。
     * <p>
     * 比较基于年月的时间线位置。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此年月等于另一个年月，返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof YearMonth) {
            YearMonth other = (YearMonth) obj;
            return year == other.year && month == other.month;
        }
        return false;
    }

    /**
     * 为此年月生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return year ^ (month << 27);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此年月输出为一个 {@code String}，例如 {@code 2007-12}。
     * <p>
     * 输出格式为 {@code uuuu-MM}：
     *
     * @return 此年月的字符串表示，不为空
     */
    @Override
    public String toString() {
        int absYear = Math.abs(year);
        StringBuilder buf = new StringBuilder(9);
        if (absYear < 1000) {
            if (year < 0) {
                buf.append(year - 10000).deleteCharAt(1);
            } else {
                buf.append(year + 10000).deleteCharAt(0);
            }
        } else {
            buf.append(year);
        }
        return buf.append(month < 10 ? "-0" : "-")
            .append(month)
            .toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(12);  // 标识一个 YearMonth
     *  out.writeInt(year);
     *  out.writeByte(month);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.YEAR_MONTH_TYPE, this);
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

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(year);
        out.writeByte(month);
    }

    static YearMonth readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        byte month = in.readByte();
        return YearMonth.of(year, month);
    }

}
