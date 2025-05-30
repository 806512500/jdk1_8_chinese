
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

import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.MONTHS;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;

/**
 * 月份，如 '七月'。
 * <p>
 * {@code Month} 是一个枚举，表示一年中的 12 个月份 -
 * 一月、二月、三月、四月、五月、六月、七月、八月、九月、十月、
 * 十一月和十二月。
 * <p>
 * 除了文本枚举名称外，每个月份还有一个 {@code int} 值。
 * {@code int} 值遵循正常用法和 ISO-8601 标准，
 * 从 1（一月）到 12（十二月）。建议应用程序使用枚举而不是 {@code int} 值以确保代码清晰。
 * <p>
 * <b>不要使用 {@code ordinal()} 来获取 {@code Month} 的数字表示。
 * 使用 {@code getValue()} 代替。</b>
 * <p>
 * 此枚举表示一个在许多日历系统中都存在的常见概念。
 * 任何定义了与 ISO-8601 日历系统完全等效的月份概念的日历系统都可以使用此枚举。
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum Month implements TemporalAccessor, TemporalAdjuster {

    /**
     * 一月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 1}。
     */
    JANUARY,
    /**
     * 二月的单例实例，有 28 天，闰年有 29 天。
     * 这个实例的数字值为 {@code 2}。
     */
    FEBRUARY,
    /**
     * 三月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 3}。
     */
    MARCH,
    /**
     * 四月的单例实例，有 30 天。
     * 这个实例的数字值为 {@code 4}。
     */
    APRIL,
    /**
     * 五月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 5}。
     */
    MAY,
    /**
     * 六月的单例实例，有 30 天。
     * 这个实例的数字值为 {@code 6}。
     */
    JUNE,
    /**
     * 七月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 7}。
     */
    JULY,
    /**
     * 八月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 8}。
     */
    AUGUST,
    /**
     * 九月的单例实例，有 30 天。
     * 这个实例的数字值为 {@code 9}。
     */
    SEPTEMBER,
    /**
     * 十月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 10}。
     */
    OCTOBER,
    /**
     * 十一月的单例实例，有 30 天。
     * 这个实例的数字值为 {@code 11}。
     */
    NOVEMBER,
    /**
     * 十二月的单例实例，有 31 天。
     * 这个实例的数字值为 {@code 12}。
     */
    DECEMBER;
    /**
     * 所有常量的私有缓存。
     */
    private static final Month[] ENUMS = Month.values();

    //-----------------------------------------------------------------------
    /**
     * 从 {@code int} 值获取 {@code Month} 的实例。
     * <p>
     * {@code Month} 是一个枚举，表示一年中的 12 个月份。
     * 此工厂方法允许从 {@code int} 值获取枚举。
     * {@code int} 值遵循 ISO-8601 标准，从 1（一月）到 12（十二月）。
     *
     * @param month  要表示的月份，从 1（一月）到 12（十二月）
     * @return 月份，不为空
     * @throws DateTimeException 如果月份无效
     */
    public static Month of(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid value for MonthOfYear: " + month);
        }
        return ENUMS[month - 1];
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code Month} 的实例。
     * <p>
     * 此方法根据指定的时间对象获取月份。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 此工厂方法将其转换为 {@code Month} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 字段。
     * 仅当时间对象具有 ISO 日历系统，或可以转换为 {@code LocalDate} 时，才允许提取。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用使用 {@code Month::from} 作为查询。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 月份，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code Month}
     */
    public static Month from(TemporalAccessor temporal) {
        if (temporal instanceof Month) {
            return (Month) temporal;
        }
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(MONTH_OF_YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Month from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取月份的 {@code int} 值。
     * <p>
     * 值的编号遵循 ISO-8601 标准，
     * 从 1（一月）到 12（十二月）。
     *
     * @return 月份，从 1（一月）到 12（十二月）
     */
    public int getValue() {
        return ordinal() + 1;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取文本表示，如 'Jan' 或 'December'。
     * <p>
     * 此方法返回用于标识月份的文本名称，
     * 适合呈现给用户。参数控制返回文本的样式和语言环境。
     * <p>
     * 如果没有找到文本映射，则返回 {@link #getValue() 数字值}。
     *
     * @param style  所需的文本长度，不为空
     * @param locale  使用的语言环境，不为空
     * @return 月份的文本值，不为空
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(MONTH_OF_YEAR, style).toFormatter(locale).format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 此方法检查此月份是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则
     * 此方法返回 true。所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，为空返回 false
     * @return 如果字段在此月份上受支持则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == MONTH_OF_YEAR;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此月份用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则
     * 将返回月份的范围，从 1 到 12。所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return field.range();
        }
        return TemporalAccessor.super.range(field);
    }

    /**
     * 从此月份获取指定字段的值作为 {@code int}。
     * <p>
     * 此方法查询此月份以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则
     * 将返回月份的值，从 1 到 12。所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值，在有效值范围内
     * @throws DateTimeException 如果无法获取字段的值或
     *         字段的值超出有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或
     *         值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public int get(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return getValue();
        }
        return TemporalAccessor.super.get(field);
    }

    /**
     * 从此月份获取指定字段的值作为 {@code long}。
     * <p>
     * 此方法查询此月份以获取指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则
     * 将返回月份的值，从 1 到 12。所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return getValue();
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }


                //-----------------------------------------------------------------------
    /**
     * 返回从这个月份开始指定数量季度后的月份。
     * <p>
     * 计算会从一年的末尾从十二月滚动到一月。
     * 指定的周期可以是负数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要添加的月份数，可以是正数或负数
     * @return 结果月份，不为空
     */
    public Month plus(long months) {
        int amount = (int) (months % 12);
        return ENUMS[(ordinal() + (amount + 12)) % 12];
    }

    /**
     * 返回从这个月份开始指定数量月份数之前的月份。
     * <p>
     * 计算会从一年的开始从一月滚动到十二月。
     * 指定的周期可以是负数。
     * <p>
     * 本实例是不可变的，此方法调用不会影响它。
     *
     * @param months  要减去的月份数，可以是正数或负数
     * @return 结果月份，不为空
     */
    public Month minus(long months) {
        return plus(-(months % 12));
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此月份的天数。
     * <p>
     * 这会根据是否为闰年返回相应的天数。
     * <p>
     * 二月在标准年份有28天，在闰年有29天。
     * 四月、六月、九月和十一月有30天。
     * 其他所有月份都有31天。
     *
     * @param leapYear  如果需要闰年的长度则为true
     * @return 此月份的天数，从28到31
     */
    public int length(boolean leapYear) {
        switch (this) {
            case FEBRUARY:
                return (leapYear ? 29 : 28);
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * 获取此月份的最小天数。
     * <p>
     * 二月的最小长度为28天。
     * 四月、六月、九月和十一月有30天。
     * 其他所有月份都有31天。
     *
     * @return 此月份的最小天数，从28到31
     */
    public int minLength() {
        switch (this) {
            case FEBRUARY:
                return 28;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * 获取此月份的最大天数。
     * <p>
     * 二月的最大长度为29天。
     * 四月、六月、九月和十一月有30天。
     * 其他所有月份都有31天。
     *
     * @return 此月份的最大天数，从29到31
     */
    public int maxLength() {
        switch (this) {
            case FEBRUARY:
                return 29;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取对应于此月份第一天的年份中的天数。
     * <p>
     * 这会返回此月份开始的年份中的天数，使用闰年标志来确定二月的长度。
     *
     * @param leapYear  如果需要闰年的长度则为true
     * @return 对应于此月份第一天的年份中的天数，从1到336
     */
    public int firstDayOfYear(boolean leapYear) {
        int leap = leapYear ? 1 : 0;
        switch (this) {
            case JANUARY:
                return 1;
            case FEBRUARY:
                return 32;
            case MARCH:
                return 60 + leap;
            case APRIL:
                return 91 + leap;
            case MAY:
                return 121 + leap;
            case JUNE:
                return 152 + leap;
            case JULY:
                return 182 + leap;
            case AUGUST:
                return 213 + leap;
            case SEPTEMBER:
                return 244 + leap;
            case OCTOBER:
                return 274 + leap;
            case NOVEMBER:
                return 305 + leap;
            case DECEMBER:
            default:
                return 335 + leap;
        }
    }

    /**
     * 获取对应于此季度第一个月份的月份。
     * <p>
     * 一年可以分为四个季度。
     * 此方法返回基础月份所在季度的第一个月份。
     * 一月、二月和三月返回一月。
     * 四月、五月和六月返回四月。
     * 七月、八月和九月返回七月。
     * 十月、十一月和十二月返回十月。
     *
     * @return 对应于此月份所在季度的第一个月份，不为空
     */
    public Month firstMonthOfQuarter() {
        return ENUMS[(ordinal() / 3) * 3];
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询对象查询此月份。
     * <p>
     * 此方法使用指定的查询策略对象查询此月份。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解此方法的结果。
     * <p>
     * 该方法的结果是通过调用指定查询对象上的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能为null（由查询定义）
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
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此月份。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的时态对象，但月份已更改为与本月份相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)} 方法并传递 {@link ChronoField#MONTH_OF_YEAR} 作为字段。
     * 如果指定的时间对象不使用ISO日历系统，则会抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，反转调用模式更清晰，建议使用 {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisMonth.adjustInto(temporal);
     *   temporal = temporal.with(thisMonth);
     * </pre>
     * <p>
     * 例如，给定一个五月的日期，以下输出：
     * <pre>
     *   dateInMay.with(JANUARY);    // 四个月前
     *   dateInMay.with(APRIL);      // 一个月前
     *   dateInMay.with(MAY);        // 同一天
     *   dateInMay.with(JUNE);       // 一个月后
     *   dateInMay.with(DECEMBER);   // 七个月后
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
        return temporal.with(MONTH_OF_YEAR, getValue());
    }

}
