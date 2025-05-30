
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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

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
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * ISO-8601日历系统中的月-日，例如 {@code --12-03}。
 * <p>
 * {@code MonthDay} 是一个不可变的日期-时间对象，表示月和日的组合。任何可以从月和日派生的字段，如季度，都可以获取。
 * <p>
 * 该类不存储或表示年份、时间或时区。例如，值 "12月3日" 可以存储在 {@code MonthDay} 中。
 * <p>
 * 由于 {@code MonthDay} 不包含年份，因此2月29日被认为是有效的。
 * <p>
 * 该类实现了 {@link TemporalAccessor} 而不是 {@link Temporal}。这是因为没有外部信息就无法确定2月29日是否有效，从而无法实现加减操作。
 * 与此相关的是，{@code MonthDay} 仅提供查询和设置 {@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 字段的访问。
 * <p>
 * ISO-8601日历系统是当今世界上大多数地区使用的现代民用日历系统。它等同于公历系统，其中今天的闰年规则适用于所有时间。
 * 对于今天编写的大多数应用程序，ISO-8601规则是完全合适的。然而，任何使用历史日期并要求其准确性的应用程序将发现ISO-8601方法不适合。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code MonthDay} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class MonthDay
        implements TemporalAccessor, TemporalAdjuster, Comparable<MonthDay>, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -939150713474957432L;
    /**
     * 月份，不能为空。
     */
    private final int month;
    /**
     * 日期。
     */
    private final int day;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前的月-日。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前的月-日。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区获取的当前月-日，不能为空
     */
    public static MonthDay now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前的月-日。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前的月-日。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不能为空
     * @return 使用系统时钟获取的当前月-日，不能为空
     */
    public static MonthDay now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前的月-日。
     * <p>
     * 这将查询指定的时钟以获取当前的月-日。
     * 使用此方法允许在测试中使用替代时钟。
     * 可以通过 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不能为空
     * @return 当前的月-日，不能为空
     */
    public static MonthDay now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);  // 调用一次
        return MonthDay.of(now.getMonth(), now.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * 获取 {@code MonthDay} 的实例。
     * <p>
     * 日期必须在闰年中对月份有效。
     * 因此，对于2月，29日是有效的。
     * <p>
     * 例如，传递4月（4）和31日将抛出异常，因为任何年份都没有4月31日。相比之下，传递2月29日是允许的，因为该月-日有时是有效的。
     *
     * @param month  要表示的月份，不能为空
     * @param dayOfMonth  要表示的日期，从1到31
     * @return 月-日，不能为空
     * @throws DateTimeException 如果任何字段的值超出范围，或者日期对月份无效
     */
    public static MonthDay of(Month month, int dayOfMonth) {
        Objects.requireNonNull(month, "month");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (dayOfMonth > month.maxLength()) {
            throw new DateTimeException("非法值，日期 " + dayOfMonth +
                    " 对于月份 " + month.name() 是无效的");
        }
        return new MonthDay(month.getValue(), dayOfMonth);
    }

    /**
     * 获取 {@code MonthDay} 的实例。
     * <p>
     * 日期必须在闰年中对月份有效。
     * 因此，对于2月（2），29日是有效的。
     * <p>
     * 例如，传递4月（4）和31日将抛出异常，因为任何年份都没有4月31日。相比之下，传递2月29日是允许的，因为该月-日有时是有效的。
     *
     * @param month  要表示的月份，从1（1月）到12（12月）
     * @param dayOfMonth  要表示的日期，从1到31
     * @return 月-日，不能为空
     * @throws DateTimeException 如果任何字段的值超出范围，或者日期对月份无效
     */
    public static MonthDay of(int month, int dayOfMonth) {
        return of(Month.of(month), dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code MonthDay} 的实例。
     * <p>
     * 这是基于指定的时间对象获取月-日。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code MonthDay} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH DAY_OF_MONTH} 字段。
     * 仅当时间对象具有ISO历法，或可以转换为 {@code LocalDate} 时，才允许提取。
     * <p>
     * 该方法匹配函数接口 {@link TemporalQuery} 的签名，允许通过方法引用使用它，例如 {@code MonthDay::from}。
     *
     * @param temporal  要转换的时间对象，不能为空
     * @return 月-日，不能为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code MonthDay}
     */
    public static MonthDay from(TemporalAccessor temporal) {
        if (temporal instanceof MonthDay) {
            return (MonthDay) temporal;
        }
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(MONTH_OF_YEAR), temporal.get(DAY_OF_MONTH));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 MonthDay: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code --12-03}）获取 {@code MonthDay} 的实例。
     * <p>
     * 字符串必须表示一个有效的月-日。
     * 格式为 {@code --MM-dd}。
     *
     * @param text  要解析的文本，如 "--12-03"，不能为空
     * @return 解析的月-日，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static MonthDay parse(CharSequence text) {
        return parse(text, PARSER);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code MonthDay} 的实例。
     * <p>
     * 文本使用格式化器解析，返回一个月-日。
     *
     * @param text  要解析的文本，不能为空
     * @param formatter  要使用的格式化器，不能为空
     * @return 解析的月-日，不能为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, MonthDay::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数，已验证。
     *
     * @param month  要表示的月份，已验证从1到12
     * @param dayOfMonth  要表示的日期，已验证从1到29-31
     */
    private MonthDay(int month, int dayOfMonth) {
        this.month = month;
        this.day = dayOfMonth;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查是否支持指定的字段。
     * <p>
     * 这检查此月-日是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code MONTH_OF_YEAR}
     * <li>{@code YEAR}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并传递 {@code this} 作为参数获得的。
     * 字段是否支持由字段确定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果此月-日支持该字段则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == MONTH_OF_YEAR || field == DAY_OF_MONTH;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此月-日用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并传递 {@code this} 作为参数获得的。
     * 范围是否可以获取由字段确定。
     *
     * @param field  要查询范围的字段，不能为空
     * @return 字段的有效值范围，不能为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return field.range();
        } else if (field == DAY_OF_MONTH) {
            return ValueRange.of(1, getMonth().minLength(), getMonth().maxLength());
        }
        return TemporalAccessor.super.range(field);
    }


                /**
     * 从这个月-日中获取指定字段的值，作为 {@code int}。
     * <p>
     * 此查询此月-日以获取指定字段的值。
     * 返回的值将始终在该字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此月-日返回有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。是否可以获取值以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值的范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 为 Javadoc 覆盖
    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    /**
     * 从这个月-日中获取指定字段的值，作为 {@code long}。
     * <p>
     * 此查询此月-日以获取指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此月-日返回有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。是否可以获取值以及值的含义由字段决定。
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
                // alignedDOW 和 alignedWOM 不支持，因为它们不能在 with() 中设置
                case DAY_OF_MONTH: return day;
                case MONTH_OF_YEAR: return month;
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取 1 到 12 的月-年字段。
     * <p>
     * 此方法以 {@code int} 形式返回月份，从 1 到 12。
     * 为了使应用程序代码更清晰，建议使用枚举 {@link Month} 通过调用 {@link #getMonth()}。
     *
     * @return 1 到 12 的月-年
     * @see #getMonth()
     */
    public int getMonthValue() {
        return month;
    }

    /**
     * 使用 {@code Month} 枚举获取月-年字段。
     * <p>
     * 此方法返回月-年的枚举 {@link Month}。
     * 这样可以避免对 {@code int} 值的含义产生混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue() int 值}。
     *
     * @return 月-年，不为 null
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return Month.of(month);
    }

    /**
     * 获取日-月字段。
     * <p>
     * 此方法以原始 {@code int} 形式返回日-月。
     *
     * @return 1 到 31 的日-月
     */
    public int getDayOfMonth() {
        return day;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查该年份是否对此月-日有效。
     * <p>
     * 此方法检查此月和日与输入年份是否形成有效的日期。这只能在 2 月 29 日返回 false。
     *
     * @param year  要验证的年份
     * @return 如果该年份对此月-日有效，则返回 true
     * @see Year#isValidMonthDay(MonthDay)
     */
    public boolean isValidYear(int year) {
        return (day == 29 && month == 2 && Year.isLeap(year) == false) == false;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中月-年已更改。
     * <p>
     * 此方法返回一个具有指定月份的月-日。
     * 如果指定月份的日-月无效，日-月将调整为该月份的最后一个有效日-月。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param month  要在返回的月-日中设置的月-年，从 1（1 月）到 12（12 月）
     * @return 基于此月-日并具有请求月份的 {@code MonthDay}，不为 null
     * @throws DateTimeException 如果月-年值无效
     */
    public MonthDay withMonth(int month) {
        return with(Month.of(month));
    }

    /**
     * 返回一个副本，其中月-年已更改。
     * <p>
     * 此方法返回一个具有指定月份的月-日。
     * 如果指定月份的日-月无效，日-月将调整为该月份的最后一个有效日-月。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param month  要在返回的月-日中设置的月-年，不为 null
     * @return 基于此月-日并具有请求月份的 {@code MonthDay}，不为 null
     */
    public MonthDay with(Month month) {
        Objects.requireNonNull(month, "month");
        if (month.getValue() == this.month) {
            return this;
        }
        int day = Math.min(this.day, month.maxLength());
        return new MonthDay(month.getValue(), day);
    }

    /**
     * 返回一个副本，其中日-月已更改。
     * <p>
     * 此方法返回一个具有指定日-月的月-日。
     * 如果日-月对月份无效，将抛出异常。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param dayOfMonth  要在返回的月-日中设置的日-月，从 1 到 31
     * @return 基于此月-日并具有请求日-月的 {@code MonthDay}，不为 null
     * @throws DateTimeException 如果日-月值无效，或日-月对月份无效
     */
    public MonthDay withDayOfMonth(int dayOfMonth) {
        if (dayOfMonth == this.day) {
            return this;
        }
        return of(month, dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询查询此月-日。
     * <p>
     * 此方法使用指定的查询策略对象查询此月-日。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解此方法的结果。
     * <p>
     * 通过调用 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为 null
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此月-日。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的时态对象，其月和日-月已更改为与此相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)} 两次，传递 {@link ChronoField#MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH} 作为字段。
     * 如果指定的时间对象不使用 ISO 日历系统，则抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisMonthDay.adjustInto(temporal);
     *   temporal = temporal.with(thisMonthDay);
     * </pre>
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为 null
     * @return 调整后的对象，不为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE) == false) {
            throw new DateTimeException("仅支持 ISO 日期时间的调整");
        }
        temporal = temporal.with(MONTH_OF_YEAR, month);
        return temporal.with(DAY_OF_MONTH, Math.min(temporal.range(DAY_OF_MONTH).getMaximum(), day));
    }

    /**
     * 使用指定的格式器格式化此月-日。
     * <p>
     * 此月-日将传递给格式器以生成字符串。
     *
     * @param formatter  要使用的格式器，不为 null
     * @return 格式化的月-日字符串，不为 null
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此月-日与年份组合以创建 {@code LocalDate}。
     * <p>
     * 此方法返回一个由此月-日和指定年份组成的 {@code LocalDate}。
     * <p>
     * 如果年份不是闰年，2 月 29 日将调整为 2 月 28 日。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param year  要使用的年份，从 MIN_YEAR 到 MAX_YEAR
     * @return 由此月-日和指定年份组成的本地日期，不为 null
     * @throws DateTimeException 如果年份超出有效年份范围
     */
    public LocalDate atYear(int year) {
        return LocalDate.of(year, month, isValidYear(year) ? day : 28);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此月-日与另一个月-日进行比较。
     * <p>
     * 比较首先基于月份的值，然后基于日-月的值。
     * 它与 {@link Comparable} 定义的“与 equals 一致”。
     *
     * @param other  要比较的另一个月-日，不为 null
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     */
    @Override
    public int compareTo(MonthDay other) {
        int cmp = (month - other.month);
        if (cmp == 0) {
            cmp = (day - other.day);
        }
        return cmp;
    }

    /**
     * 检查此月-日是否在指定的月-日之后。
     *
     * @param other  要比较的另一个月-日，不为 null
     * @return 如果此月-日在指定的月-日之后，则返回 true
     */
    public boolean isAfter(MonthDay other) {
        return compareTo(other) > 0;
    }

    /**
     * 检查此月-日是否在指定的月-日之前。
     *
     * @param other  要比较的另一个月-日，不为 null
     * @return 如果此月-日在指定的月-日之前，则返回 true
     */
    public boolean isBefore(MonthDay other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此月-日是否等于另一个月-日。
     * <p>
     * 比较基于月-日在一年中的时间线位置。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此月-日等于另一个月-日，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MonthDay) {
            MonthDay other = (MonthDay) obj;
            return month == other.month && day == other.day;
        }
        return false;
    }

    /**
     * 为此月-日生成哈希码。
     *
     * @return 适当的哈希码
     */
    @Override
    public int hashCode() {
        return (month << 6) + day;
    }

    //-----------------------------------------------------------------------
    /**
     * 以 {@code String} 形式输出此月-日，例如 {@code --12-03}。
     * <p>
     * 输出格式为 {@code --MM-dd}：
     *
     * @return 此月-日的字符串表示形式，不为 null
     */
    @Override
    public String toString() {
        return new StringBuilder(10).append("--")
            .append(month < 10 ? "0" : "").append(month)
            .append(day < 10 ? "-0" : "-").append(day)
            .toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(13);  // 标识 MonthDay
     *  out.writeByte(month);
     *  out.writeByte(day);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.MONTH_DAY_TYPE, this);
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
        out.writeByte(month);
        out.writeByte(day);
    }

    static MonthDay readExternal(DataInput in) throws IOException {
        byte month = in.readByte();
        byte day = in.readByte();
        return MonthDay.of(month, day);
    }

}
