
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
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;
import static java.time.temporal.ChronoUnit.CENTURIES;
import static java.time.temporal.ChronoUnit.DECADES;
import static java.time.temporal.ChronoUnit.ERAS;
import static java.time.temporal.ChronoUnit.MILLENNIA;
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
 * ISO-8601 日历系统中的年份，例如 {@code 2007}。
 * <p>
 * {@code Year} 是一个不可变的日期时间对象，表示一个年份。
 * 可以从年份中派生出任何字段。
 * <p>
 * <b>请注意，ISO 日历系统中的年份仅在现代年份中与格里高利-儒略系统中的年份对齐。俄罗斯部分地区直到 1920 年才采用现代格里高利/ISO 规则。
 * 因此，历史年份必须谨慎对待。</b>
 * <p>
 * 该类不存储或表示月份、日期、时间或时区。
 * 例如，值 "2007" 可以存储在 {@code Year} 中。
 * <p>
 * 该类表示的年份遵循 ISO-8601 标准，并使用前推纪年系统。公元 1 年之前是公元 0 年，然后是公元前 1 年。
 * <p>
 * ISO-8601 日历系统是当今世界上大多数地区使用的现代民用日历系统。它等同于前推格里高利日历系统，其中今天对闰年的规则适用于所有时间。
 * 对于今天编写的大多数应用程序，ISO-8601 规则完全适用。然而，任何使用历史日期并要求它们准确的应用程序都会发现 ISO-8601 方法不适用。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code Year} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应该使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class Year
        implements Temporal, TemporalAdjuster, Comparable<Year>, Serializable {

    /**
     * 支持的最小年份，'-999,999,999'。
     */
    public static final int MIN_VALUE = -999_999_999;
    /**
     * 支持的最大年份，'+999,999,999'。
     */
    public static final int MAX_VALUE = 999_999_999;

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -23038383694477807L;
    /**
     * 解析器。
     */
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .toFormatter();

    /**
     * 被表示的年份。
     */
    private final int year;

    //-----------------------------------------------------------------------
    /**
     * 从系统时钟在默认时区中获取当前年份。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前年份。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前年份，不为空
     */
    public static Year now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从系统时钟在指定时区中获取当前年份。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前年份。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将无法使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前年份，不为空
     */
    public static Year now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前年份。
     * <p>
     * 这将查询指定的时钟以获取当前年份。
     * 使用此方法可以使用替代时钟进行测试。
     * 可以通过 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前年份，不为空
     */
    public static Year now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);  // 调用一次
        return Year.of(now.getYear());
    }

    //-----------------------------------------------------------------------
    /**
     * 获取 {@code Year} 的实例。
     * <p>
     * 该方法接受前推 ISO 日历系统中的年份值。
     * <p>
     * 公元 2 年表示为 2。<br>
     * 公元 1 年表示为 1。<br>
     * 公元前 1 年表示为 0。<br>
     * 公元前 2 年表示为 -1。<br>
     *
     * @param isoYear  要表示的 ISO 前推年份，从 {@code MIN_VALUE} 到 {@code MAX_VALUE}
     * @return 年份，不为空
     * @throws DateTimeException 如果字段无效
     */
    public static Year of(int isoYear) {
        YEAR.checkValidValue(isoYear);
        return new Year(isoYear);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code Year} 的实例。
     * <p>
     * 该方法基于指定的时间对象获取年份。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code Year} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#YEAR 年份} 字段。
     * 仅当时间对象具有 ISO 历法或可以转换为 {@code LocalDate} 时，才允许提取。
     * <p>
     * 该方法匹配 {@link TemporalQuery} 函数接口的签名，允许通过方法引用使用它，{@code Year::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 年份，不为空
     * @throws DateTimeException 如果无法转换为 {@code Year}
     */
    public static Year from(TemporalAccessor temporal) {
        if (temporal instanceof Year) {
            return (Year) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 Year: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007}）获取 {@code Year} 的实例。
     * <p>
     * 字符串必须表示一个有效的年份。
     * 范围在 0000 到 9999 之外的年份必须以加号或减号为前缀。
     *
     * @param text  要解析的文本，如 "2007"，不为空
     * @return 解析的年份，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static Year parse(CharSequence text) {
        return parse(text, PARSER);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code Year} 的实例。
     * <p>
     * 文本使用格式化器解析，返回一个年份。
     *
     * @param text  要解析的文本，不为空
     * @param formatter  要使用的格式化器，不为空
     * @return 解析的年份，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static Year parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, Year::from);
    }

    //-------------------------------------------------------------------------
    /**
     * 根据 ISO 前推历法系统规则检查年份是否为闰年。
     * <p>
     * 该方法在整个时间线上应用当前的闰年规则。
     * 一般来说，能被 4 整除的年份是闰年。但是，能被 100 整除的年份不是闰年，除非能被 400 整除。
     * <p>
     * 例如，1904 年是闰年，因为它能被 4 整除。
     * 1900 年不是闰年，因为它能被 100 整除，但 2000 年是闰年，因为它能被 400 整除。
     * <p>
     * 计算是前推的——将相同的规则应用于遥远的未来和遥远的过去。
     * 这在历史上是不准确的，但符合 ISO-8601 标准。
     *
     * @param year  要检查的年份
     * @return 如果年份是闰年则为 true，否则为 false
     */
    public static boolean isLeap(long year) {
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param year  要表示的年份
     */
    private Year(int year) {
        this.year = year;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取年份值。
     * <p>
     * 该方法返回的年份是前推的，如 {@code get(YEAR)}。
     *
     * @return 年份，从 {@code MIN_VALUE} 到 {@code MAX_VALUE}
     */
    public int getValue() {
        return year;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 该方法检查此年份是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long) with}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code YEAR_OF_ERA}
     * <li>{@code YEAR}
     * <li>{@code ERA}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此年份上受支持则为 true，否则为 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == YEAR || field == YEAR_OF_ERA || field == ERA;
        }
        return field != null && field.isSupportedBy(this);
    }


                /**
     * 检查指定的单位是否受支持。
     * <p>
     * 此方法检查指定的单位是否可以添加到或从这一年中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则此查询在此实现。
     * 支持的单位有：
     * <ul>
     * <li>{@code YEARS}
     * <li>{@code DECADES}
     * <li>{@code CENTURIES}
     * <li>{@code MILLENNIA}
     * <li>{@code ERAS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递获得的。
     * 单位是否受支持由该单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit == YEARS || unit == DECADES || unit == CENTURIES || unit == MILLENNIA || unit == ERAS;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此年份用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回
     * 适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == YEAR_OF_ERA) {
            return (year <= 0 ? ValueRange.of(1, MAX_VALUE + 1) : ValueRange.of(1, MAX_VALUE));
        }
        return Temporal.super.range(field);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 此查询此年份中指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此年份的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。值是否可以获取，
     * 以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // override for Javadoc
    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 此查询此年份中指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此年份的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递获得的。值是否可以获取，
     * 以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case YEAR_OF_ERA: return (year < 1 ? 1 - year : year);
                case YEAR: return year;
                case ERA: return (year < 1 ? 0 : 1);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 根据 ISO 历法系统规则检查年份是否为闰年。
     * <p>
     * 此方法在整个时间线上应用当前的闰年规则。
     * 一般来说，能被 4 整除的年份是闰年。但是，能被 100 整除的年份不是闰年，
     * 除非能被 400 整除。
     * <p>
     * 例如，1904 是闰年，因为它能被 4 整除。
     * 1900 不是闰年，因为它能被 100 整除，但 2000 是闰年，因为它能被 400 整除。
     * <p>
     * 计算是预设的 - 适用相同的规则到远未来和远过去。
     * 这在历史上是不准确的，但符合 ISO-8601 标准。
     *
     * @return 如果年份是闰年，则返回 true，否则返回 false
     */
    public boolean isLeap() {
        return Year.isLeap(year);
    }

    /**
     * 检查月日是否对这一年有效。
     * <p>
     * 此方法检查这一年和输入的月日是否构成有效的日期。
     *
     * @param monthDay  要验证的月日，null 返回 false
     * @return 如果月日对这一年有效，则返回 true
     */
    public boolean isValidMonthDay(MonthDay monthDay) {
        return monthDay != null && monthDay.isValidYear(year);
    }

    /**
     * 获取这一年中的天数。
     *
     * @return 这一年中的天数，365 或 366
     */
    public int length() {
        return isLeap() ? 366 : 365;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回调整后的这一年副本。
     * <p>
     * 此方法返回一个基于此年份的 {@code Year}，年份已调整。
     * 调整通过指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行哪些调整。
     * <p>
     * 此方法的结果是通过调用
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法在指定的调整器上
     * 并将 {@code this} 作为参数传递获得的。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不为 null
     * @return 基于 {@code this} 并进行了调整的 {@code Year}，不为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year with(TemporalAdjuster adjuster) {
        return (Year) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此年份的副本，指定字段设置为新值。
     * <p>
     * 此方法返回一个基于此年份的 {@code Year}，指定字段的值已更改。
     * 如果由于字段不受支持或其他原因无法设置值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此实现。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code YEAR_OF_ERA} -
     *  返回一个指定纪年年的 {@code Year}
     *  纪元将保持不变。
     * <li>{@code YEAR} -
     *  返回一个指定年的 {@code Year}。
     *  这将完全替换日期，等同于 {@link #of(int)}。
     * <li>{@code ERA} -
     *  返回一个指定纪元的 {@code Year}。
     *  纪年年将保持不变。
     * </ul>
     * <p>
     * 在所有情况下，如果新值超出字段的有效值范围，
     * 则抛出 {@code DateTimeException}。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.adjustInto(Temporal, long)}
     * 并将 {@code this} 作为参数传递获得的。在这种情况下，字段决定
     * 是否以及如何调整实例。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不为 null
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 并设置了指定字段的 {@code Year}，不为 null
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case YEAR_OF_ERA: return Year.of((int) (year < 1 ? 1 - newValue : newValue));
                case YEAR: return Year.of((int) newValue);
                case ERA: return (getLong(ERA) == newValue ? this : Year.of(1 - year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此年份的副本，指定了要添加的量。
     * <p>
     * 此方法返回一个基于此年份的 {@code Year}，指定了要添加的量。
     * 量通常是 {@link Period}，但可以是任何实现
     * {@link TemporalAmount} 接口的其他类型。
     * <p>
     * 计算是委托给量对象，通过调用
     * {@link TemporalAmount#addTo(Temporal)}。量实现可以自由地
     * 以任何方式实现添加，但通常会回调到 {@link #plus(long, TemporalUnit)}。
     * 请查阅量实现的文档以确定是否可以成功添加。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加的量，不为 null
     * @return 基于此年份并进行了添加的 {@code Year}，不为 null
     * @throws DateTimeException 如果无法进行添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year plus(TemporalAmount amountToAdd) {
        return (Year) amountToAdd.addTo(this);
    }

    /**
     * 返回一个基于此年份的副本，指定了要添加的量。
     * <p>
     * 此方法返回一个基于此年份的 {@code Year}，指定了要添加的量。
     * 如果由于单位不受支持或其他原因无法添加量，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则添加在此实现。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code YEARS} -
     *  返回一个指定了要添加的年数的 {@code Year}。
     *  这等同于 {@link #plusYears(long)}。
     * <li>{@code DECADES} -
     *  返回一个指定了要添加的十年数的 {@code Year}。
     *  这等同于调用 {@link #plusYears(long)} 并将量乘以 10。
     * <li>{@code CENTURIES} -
     *  返回一个指定了要添加的世纪数的 {@code Year}。
     *  这等同于调用 {@link #plusYears(long)} 并将量乘以 100。
     * <li>{@code MILLENNIA} -
     *  返回一个指定了要添加的千年数的 {@code Year}。
     *  这等同于调用 {@link #plusYears(long)} 并将量乘以 1,000。
     * <li>{@code ERAS} -
     *  返回一个指定了要添加的纪元数的 {@code Year}。
     *  只支持两个纪元，因此量必须是一、零或负一。
     *  如果量非零，则年份将更改，使得纪年年不变。
     * </ul>
     * <p>
     * 所有其他 {@code ChronoUnit} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.addTo(Temporal, long)}
     * 并将 {@code this} 作为参数传递获得的。在这种情况下，单位决定
     * 是否以及如何进行添加。
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加到结果中的单位量，可以为负
     * @param unit  要添加的单位量，不为 null
     * @return 基于此年份并添加了指定量的 {@code Year}，不为 null
     * @throws DateTimeException 如果无法进行添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
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
     * 返回一个副本，其中指定了要添加的年数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param yearsToAdd  要添加的年数，可以是负数
     * @return 基于本年的 {@code Year}，添加了年数，不为空
     * @throws DateTimeException 如果结果超出支持范围
     */
    public Year plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return of(YEAR.checkValidIntValue(year + yearsToAdd));  // 溢出安全
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的数量。
     * <p>
     * 这返回一个基于本年的 {@code Year}，减去了指定的数量。
     * 数量通常是 {@link Period}，但可以是实现 {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给数量对象，通过调用 {@link TemporalAmount#subtractFrom(Temporal)}。数量实现可以自由地以任何方式实现减法，但通常会回调到 {@link #minus(long, TemporalUnit)}。请参阅数量实现的文档，以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于本年的 {@code Year}，减去了指定的数量，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year minus(TemporalAmount amountToSubtract) {
        return (Year) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，其中指定了要减去的数量。
     * <p>
     * 这返回一个基于本年的 {@code Year}，减去了以单位表示的数量。如果无法减去数量，因为单位不受支持或有其他原因，将抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。请参阅该方法以了解添加（以及因此减法）的工作原理。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param amountToSubtract  要从结果中减去的单位数量，可以是负数
     * @param unit  要减去的单位，不为空
     * @return 基于本年的 {@code Year}，减去了指定的数量，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Year minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    /**
     * 返回一个副本，其中指定了要减去的年数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param yearsToSubtract  要减去的年数，可以是负数
     * @return 基于本年的 {@code Year}，减去了年数，不为空
     * @throws DateTimeException 如果结果超出支持范围
     */
    public Year minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询查询本年。
     * <p>
     * 这使用指定的查询策略对象查询本年。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询的文档，以了解此方法的结果将是什么。
     * <p>
     * 本方法的结果是通过调用指定查询上的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        } else if (query == TemporalQueries.precision()) {
            return (R) YEARS;
        }
        return Temporal.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有本年。
     * <p>
     * 这返回一个与输入具有相同可观察类型的临时对象，但年份已更改为与本年相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 并传递 {@link ChronoField#YEAR} 作为字段。
     * 如果指定的时间对象不使用 ISO 日历系统，则会抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，反转调用模式会更清晰，建议使用 {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisYear.adjustInto(temporal);
     *   temporal = temporal.with(thisYear);
     * </pre>
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
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
        return temporal.with(YEAR, year);
    }

    /**
     * 计算到另一个年份的时间量，以指定的单位表示。
     * <p>
     * 这计算两个 {@code Year} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的年份。
     * 如果终点在起点之前，结果将为负数。
     * 传递给此方法的 {@code Temporal} 将使用 {@link #from(TemporalAccessor)} 转换为 {@code Year}。
     * 例如，可以计算两个年份之间的十年数，使用 {@code startYear.until(endYear, DECADES)}。
     * <p>
     * 计算返回一个整数，表示两个年份之间完整的单位数。
     * 例如，2012 年和 2031 年之间的十年数将仅为一个十年，因为还差一年才满两个十年。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, YEARS);
     *   amount = YEARS.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更易读。
     * <p>
     * 计算是在此方法中为 {@link ChronoUnit} 实现的。
     * 支持的单位有 {@code YEARS}、{@code DECADES}、{@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}。
     * 其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用 {@code TemporalUnit.between(Temporal, Temporal)} 并传递 {@code this} 作为第一个参数和转换后的输入时间作为第二个参数获得的。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param endExclusive  终点，不为空，将转换为 {@code Year}
     * @param unit  要测量的时间量的单位，不为空
     * @return 本年和终点年份之间的时间量
     * @throws DateTimeException 如果无法计算时间量，或无法将终点时间转换为 {@code Year}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Year end = Year.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            long yearsUntil = ((long) end.year) - year;  // 不会发生溢出
            switch ((ChronoUnit) unit) {
                case YEARS: return yearsUntil;
                case DECADES: return yearsUntil / 10;
                case CENTURIES: return yearsUntil / 100;
                case MILLENNIA: return yearsUntil / 1000;
                case ERAS: return end.getLong(ERA) - getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化器格式化本年。
     * <p>
     * 本年将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的年份字符串，不为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将本年与一年中的某一天结合，创建一个 {@code LocalDate}。
     * <p>
     * 这返回一个由本年和指定的一年中的某一天组成的 {@code LocalDate}。
     * <p>
     * 366 是闰年的有效值。
     *
     * @param dayOfYear  要使用的年中的某一天，从 1 到 365-366
     * @return 由本年和指定的一年中的某一天组成的本地日期，不为空
     * @throws DateTimeException 如果年中的某一天为零或更小，366 或更大，或等于 366 且本年不是闰年
     */
    public LocalDate atDay(int dayOfYear) {
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    /**
     * 将本年与月份结合，创建一个 {@code YearMonth}。
     * <p>
     * 这返回一个由本年和指定月份组成的 {@code YearMonth}。
     * 所有可能的年份和月份组合都是有效的。
     * <p>
     * 此方法可以作为链的一部分生成日期：
     * <pre>
     *  LocalDate date = year.atMonth(month).atDay(day);
     * </pre>
     *
     * @param month  要使用的月份，不为空
     * @return 由本年和指定月份组成的年月，不为空
     */
    public YearMonth atMonth(Month month) {
        return YearMonth.of(year, month);
    }

    /**
     * 将本年与月份结合，创建一个 {@code YearMonth}。
     * <p>
     * 这返回一个由本年和指定月份组成的 {@code YearMonth}。
     * 所有可能的年份和月份组合都是有效的。
     * <p>
     * 此方法可以作为链的一部分生成日期：
     * <pre>
     *  LocalDate date = year.atMonth(month).atDay(day);
     * </pre>
     *
     * @param month  要使用的月份，从 1（一月）到 12（十二月）
     * @return 由本年和指定月份组成的年月，不为空
     * @throws DateTimeException 如果月份无效
     */
    public YearMonth atMonth(int month) {
        return YearMonth.of(year, month);
    }

    /**
     * 将本年与月份和日期结合，创建一个 {@code LocalDate}。
     * <p>
     * 这返回一个由本年和指定的月份和日期组成的 {@code LocalDate}。
     * <p>
     * 如果本年不是闰年，2 月 29 日将调整为 2 月 28 日。
     *
     * @param monthDay  要使用的月份和日期，不为空
     * @return 由本年和指定的月份和日期组成的本地日期，不为空
     */
    public LocalDate atMonthDay(MonthDay monthDay) {
        return monthDay.atYear(year);
    }

    //-----------------------------------------------------------------------
    /**
     * 比较本年与另一个年份。
     * <p>
     * 比较基于年份的值。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义的。
     *
     * @param other  要比较的其他年份，不为空
     * @return 比较值，负数表示小于，正数表示大于
     */
    @Override
    public int compareTo(Year other) {
        return year - other.year;
    }

    /**
     * 检查本年是否在指定的年份之后。
     *
     * @param other  要比较的其他年份，不为空
     * @return 如果本年在指定的年份之后，返回 true
     */
    public boolean isAfter(Year other) {
        return year > other.year;
    }

    /**
     * 检查本年是否在指定的年份之前。
     *
     * @param other  要比较的其他年份，不为空
     * @return 如果本年在指定的年份之前，返回 true
     */
    public boolean isBefore(Year other) {
        return year < other.year;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查本年是否等于另一个年份。
     * <p>
     * 比较基于年份的时间线位置。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果本年等于其他年份，返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Year) {
            return year == ((Year) obj).year;
        }
        return false;
    }

    /**
     * 本年的哈希码。
     *
     * @return 适合的哈希码
     */
    @Override
    public int hashCode() {
        return year;
    }

    //-----------------------------------------------------------------------
    /**
     * 输出本年为 {@code String}。
     *
     * @return 本年的字符串表示，不为空
     */
    @Override
    public String toString() {
        return Integer.toString(year);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(11);  // 标识一个 Year
     *  out.writeInt(year);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.YEAR_TYPE, this);
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
    }

    static Year readExternal(DataInput in) throws IOException {
        return Year.of(in.readInt());
    }

}
