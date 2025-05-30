
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

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import java.util.Comparator;
import java.util.Objects;

/**
 * 一个任意历法中的日期，不包含时间或时区，旨在用于高级全球化用例。
 * <p>
 * <b>大多数应用程序应声明方法签名、字段和变量为 {@link LocalDate}，而不是此接口。</b>
 * <p>
 * {@code ChronoLocalDate} 是日期的抽象表示，其中 {@code Chronology} 历法或日历系统是可插拔的。
 * 日期以 {@link TemporalField} 表示的字段定义，其中大多数常见实现定义在 {@link ChronoField} 中。
 * 历法定义了日历系统的操作方式和标准字段的含义。
 *
 * <h3>何时使用此接口</h3>
 * API 的设计鼓励使用 {@code LocalDate} 而不是此接口，即使应用程序需要处理多个日历系统。
 * <p>
 * 这个概念一开始可能会显得令人惊讶，因为全球化应用程序的自然方法似乎是抽象化日历系统。
 * 然而，如下面所述，抽象化日历系统通常是错误的方法，会导致逻辑错误和难以发现的错误。
 * 因此，选择使用此接口而不是 {@code LocalDate} 应该被视为应用程序范围内的架构决策。
 *
 * <h3>使用此接口需要考虑的架构问题</h3>
 * 这些是在应用程序中使用此接口之前必须考虑的一些问题。
 * <p>
 * 1) 使用此接口的应用程序，而不是仅使用 {@code LocalDate}，面临更高的错误概率。
 * 这是因为使用的日历系统在开发时是未知的。导致错误的一个主要原因是开发人员将 ISO 日历系统的日常知识
 * 应用于旨在处理任意日历系统的代码。下面的部分概述了这些假设如何导致问题。
 * 降低这种增加的错误风险的主要机制是严格的代码审查过程。这也应被视为代码维护期间的额外成本。
 * <p>
 * 2) 此接口不强制实现的不可变性。
 * 尽管实现说明指出所有实现必须是不可变的，但代码或类型系统中没有任何内容强制这一点。
 * 因此，任何声明接受 {@code ChronoLocalDate} 的方法都可能被传递一个编写不当或恶意编写的可变实现。
 * <p>
 * 3) 使用此接口的应用程序必须考虑纪元的影响。
 * {@code LocalDate} 通过确保 {@code getYear()} 返回回溯年份来保护用户免受纪元概念的影响。
 * 这一决定确保开发人员可以将 {@code LocalDate} 实例视为由三个字段组成 - 年份、月份和日期。
 * 相比之下，此接口的用户必须将日期视为由四个字段组成 - 纪元、纪元年份、月份和日期。
 * 额外的纪元字段经常被遗忘，但在任意日历系统中却是至关重要的。
 * 例如，在日本日历系统中，纪元代表天皇的统治。每当一个统治结束而另一个开始时，纪元年份就会重置为一。
 * <p>
 * 4) 两个系统之间传递日期的唯一国际标准是 ISO-8601 标准，该标准要求使用 ISO 日历系统。
 * 在整个应用程序中使用此接口将不可避免地导致需要在网络或组件边界之间传递日期，需要特定于应用程序的协议或格式。
 * <p>
 * 5) 长期持久化，如数据库，几乎总是只接受 ISO-8601 日历系统中的日期（或相关的儒略-格里高利）。
 * 在其他日历系统中传递日期会增加与持久化交互的复杂性。
 * <p>
 * 6) 在大多数情况下，将 {@code ChronoLocalDate} 在整个应用程序中传递是不必要的，如下面最后一部分所述。
 *
 * <h3>多日历系统代码中的错误假设</h3>
 * 如上所述，尝试使用和操作任意日历系统中的日期时有许多问题需要考虑。以下是一些关键问题。
 * <p>
 * 查询月份日期并假设值永远不会超过 31 是无效的。某些日历系统在某些月份中有超过 31 天。
 * <p>
 * 将 12 个月添加到日期并假设已添加一年是无效的。某些日历系统有不同的月份数，例如科普特或埃塞俄比亚日历系统中的 13 个月。
 * <p>
 * 将一个月添加到日期并假设月份值会增加一或滚动到下一年是无效的。某些日历系统中一年的月份数是可变的，例如希伯来日历。
 * <p>
 * 将一个月添加到日期，然后再添加一个月，并假设月份日期会保持接近其原始值是无效的。某些日历系统中最长月份和最短月份的长度差异很大。
 * 例如，科普特或埃塞俄比亚日历系统有 12 个月，每个月 30 天，还有 1 个月 5 天。
 * <p>
 * 将七天添加到日期并假设已添加一周是无效的。某些日历系统中一周的天数不是七天，例如法国革命日历。
 * <p>
 * 假设因为 {@code date1} 的年份大于 {@code date2} 的年份，所以 {@code date1} 在 {@code date2} 之后是无效的。
 * 这在所有日历系统中都是无效的，尤其是日本日历系统，其中纪元年份在每位新天皇即位时重新开始。
 * <p>
 * 假设月份为一且日期为一是一年的开始是无效的。并非所有日历系统都在月份值为一时开始一年。
 * <p>
 * 一般来说，当开发时未知日历系统时，操作日期甚至查询日期都容易出错。这就是为什么使用此接口的代码需要额外的代码审查。
 * 这也是为什么通常避免使用此接口类型的架构决策是正确的。
 *
 * <h3>使用 LocalDate 代替</h3>
 * 使用此接口的替代方案如下。
 * <ul>
 * <li>将所有涉及日期的方法签名声明为 {@code LocalDate}。
 * <li>将历法（日历系统）存储在用户配置文件中或从用户区域设置中查找。
 * <li>在打印和解析期间将 ISO {@code LocalDate} 转换为用户首选的日历系统。
 * </ul>
 * 这种方法将全球化日历系统的问题视为本地化问题，并将其限制在 UI 层。这种方法与 Java 平台中的其他本地化问题保持一致。
 * <p>
 * 如上所述，对日期进行计算，其中日历系统的规则是可插拔的，需要技能且不推荐。
 * 幸运的是，对任意日历系统中的日期进行计算的需求极为罕见。例如，图书馆书籍租赁方案的业务规则几乎不可能允许租赁期为一个月，
 * 其中月份的含义取决于用户的首选日历系统。
 * <p>
 * 在任意日历系统中对日期进行计算的一个关键用例是生成月度日历以供显示和用户交互。同样，这是一个 UI 问题，
 * 在 UI 层的少数方法中使用此接口可能是合理的。
 * <p>
 * 在系统的任何其他部分，如果必须在 ISO 以外的日历系统中操作日期，用例通常会指定要使用的日历系统。
 * 例如，应用程序可能需要计算下一个伊斯兰或希伯来节日，这可能需要操作日期。
 * 这种用例可以如下处理：
 * <ul>
 * <li>从传递给方法的 ISO {@code LocalDate} 开始
 * <li>将日期转换为备用日历系统，对于此用例，该日历系统是已知的而不是任意的
 * <li>执行计算
 * <li>转换回 {@code LocalDate}
 * </ul>
 * 编写低级框架或库的开发人员也应避免使用此接口。相反，应使用两个通用访问接口之一。
 * 如果需要只读访问，请使用 {@link TemporalAccessor}，如果需要读写访问，请使用 {@link Temporal}。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可以实例化的实现都必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现序列化。
 * <p>
 * 可能会向系统添加额外的日历系统。
 * 有关更多详细信息，请参阅 {@link Chronology}。
 *
 * @since 1.8
 */
public interface ChronoLocalDate
        extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate> {

    /**
     * 获取一个比较器，该比较器按时间线顺序忽略历法比较 {@code ChronoLocalDate}。
     * <p>
     * 该比较器与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期而不比较历法。
     * 这允许基于日期在本地时间线上的位置比较不同日历系统中的日期。
     * 底层比较等同于比较纪元日。
     *
     * @return 一个按时间线顺序忽略历法的比较器
     * @see #isAfter
     * @see #isBefore
     * @see #isEqual
     */
    static Comparator<ChronoLocalDate> timeLineOrder() {
        return AbstractChronology.DATE_ORDER;
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象中获取 {@code ChronoLocalDate} 的实例。
     * <p>
     * 此方法基于指定的时间对象获取一个本地日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ChronoLocalDate} 的实例。
     * <p>
     * 转换从时间对象中提取并组合历法和日期。行为等同于使用
     * {@link Chronology#date(TemporalAccessor)} 与提取的历法。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名，允许通过方法引用使用它，例如 {@code ChronoLocalDate::from}。
     *
     * @param temporal  要转换的时间对象，不为 null
     * @return 日期，不为 null
     * @throws DateTimeException 如果无法转换为 {@code ChronoLocalDate}
     * @see Chronology#date(TemporalAccessor)
     */
    static ChronoLocalDate from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDate) {
            return (ChronoLocalDate) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoLocalDate from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.date(temporal);
    }


                //-----------------------------------------------------------------------
    /**
     * 获取此日期的历法系统。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * 时代和其他字段在 {@link ChronoField} 中由历法系统定义。
     *
     * @return 历法系统，不为空
     */
    Chronology getChronology();

    /**
     * 获取由历法系统定义的时代。
     * <p>
     * 时代在概念上是时间线的最大划分。
     * 大多数日历系统有一个单一的纪元将时间线分为两个时代。
     * 然而，有些日历系统有多个时代，例如每个领导者的统治时期。
     * 其确切含义由 {@code Chronology} 确定。
     * <p>
     * 所有正确实现的 {@code Era} 类都是单例，因此
     * 编写 {@code date.getEra() == SomeChrono.ERA_NAME)} 是有效的代码。
     * <p>
     * 此默认实现使用 {@link Chronology#eraOf(int)}。
     *
     * @return 适用于此日期的历法系统特定时代常量，不为空
     */
    default Era getEra() {
        return getChronology().eraOf(get(ERA));
    }

    /**
     * 检查此日期的年份是否为闰年，由日历系统定义。
     * <p>
     * 闰年是一个比正常年份更长的年份。
     * 其确切含义由历法系统确定，但必须满足闰年比非闰年的年份更长的约束。
     * <p>
     * 此默认实现使用 {@link Chronology#isLeapYear(long)}。
     *
     * @return 如果此日期在闰年则返回 true，否则返回 false
     */
    default boolean isLeapYear() {
        return getChronology().isLeapYear(getLong(YEAR));
    }

    /**
     * 返回此日期表示的月份的长度，由日历系统定义。
     * <p>
     * 此方法返回月份的天数。
     *
     * @return 月份的天数
     */
    int lengthOfMonth();

    /**
     * 返回此日期表示的年份的长度，由日历系统定义。
     * <p>
     * 此方法返回年份的天数。
     * <p>
     * 默认实现使用 {@link #isLeapYear()} 并返回 365 或 366。
     *
     * @return 年份的天数
     */
    default int lengthOfYear() {
        return (isLeapYear() ? 366 : 365);
    }

    /**
     * 检查指定的字段是否支持。
     * <p>
     * 此方法检查指定的字段是否可以查询此日期。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 支持的字段集由历法系统定义，通常包括所有 {@code ChronoField} 日期字段。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段可以查询则返回 true，否则返回 false
     */
    @Override
    default boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isDateBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 此方法检查指定的单位是否可以添加到或从这个日期中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 支持的单位集由历法系统定义，通常包括所有 {@code ChronoUnit} 日期单位，但不包括 {@code FOREVER}。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递获得的。
     * 单位是否支持由单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去则返回 true，否则返回 false
     */
    @Override
    default boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isDateBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    // 重写以实现协变返回类型
    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate with(TemporalAdjuster adjuster) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), Temporal.super.with(adjuster));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws UnsupportedTemporalTypeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return ChronoLocalDateImpl.ensureValid(getChronology(), field.adjustInto(this, newValue));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate plus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), Temporal.super.plus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return ChronoLocalDateImpl.ensureValid(getChronology(), unit.addTo(this, amountToAdd));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate minus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), Temporal.super.minus(amount));
    }

    /**
     * {@inheritDoc}
     * @throws DateTimeException {@inheritDoc}
     * @throws UnsupportedTemporalTypeException {@inheritDoc}
     * @throws ArithmeticException {@inheritDoc}
     */
    @Override
    default ChronoLocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略对象查询此日期。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解
     * 此方法的结果是什么。
     * <p>
     * 此方法的结果是通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并在指定查询中传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        } else if (query == TemporalQueries.localTime()) {
            return null;
        } else if (query == TemporalQueries.chronology()) {
            return (R) getChronology();
        } else if (query == TemporalQueries.precision()) {
            return (R) DAYS;
        }
        // 作为优化内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象，使其具有与该对象相同的日期。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的临时对象，但日期已更改为与该对象相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)}
     * 并传递 {@link ChronoField#EPOCH_DAY} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式会更清晰，建议使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisLocalDate.adjustInto(temporal);
     *   temporal = temporal.with(thisLocalDate);
     * </pre>
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    default Temporal adjustInto(Temporal temporal) {
        return temporal.with(EPOCH_DAY, toEpochDay());
    }

    /**
     * 计算到另一个日期的时间间隔，以指定的单位表示。
     * <p>
     * 此方法计算两个 {@code ChronoLocalDate} 对象之间的时间间隔，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的日期。
     * 如果终点在起点之前，结果将为负数。
     * 传递给此方法的 {@code Temporal} 将使用 {@link Chronology#date(TemporalAccessor)} 转换为
     * {@code ChronoLocalDate}。计算返回一个整数，表示两个日期之间的完整单位数。
     * 例如，可以使用 {@code startDate.until(endDate, DAYS)} 计算两个日期之间的天数。
     * <p>
     * 有两种等效的方法可以使用此方法。
     * 第一种是调用此方法。第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * 选择应基于哪种方法使代码更易读。
     * <p>
     * 此方法的实现对于 {@link ChronoUnit} 是在本方法中完成的。
     * 单位 {@code DAYS}、{@code WEEKS}、{@code MONTHS}、{@code YEARS}、
     * {@code DECADES}、{@code CENTURIES}、{@code MILLENNIA} 和 {@code ERAS}
     * 应由所有实现支持。其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.between(Temporal, Temporal)} 并将 {@code this} 作为第一个参数和转换后的输入临时对象作为
     * 第二个参数传递获得的。
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param endExclusive  终点，不为空，将转换为同一历法系统的 {@code ChronoLocalDate}
     * @param unit  要测量的单位，不为空
     * @return 从该日期到终点的时间间隔
     * @throws DateTimeException 如果无法计算时间间隔，或无法将终点临时对象转换为 {@code ChronoLocalDate}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以实现 Javadoc
    long until(Temporal endExclusive, TemporalUnit unit);

    /**
     * 计算此日期与另一个日期之间的期间，作为 {@code ChronoPeriod}。
     * <p>
     * 此方法计算两个日期之间的期间。所有提供的历法系统都使用年、月和日计算期间，
     * 但 {@code ChronoPeriod} API 允许使用其他单位表示期间。
     * <p>
     * 起点和终点分别是 {@code this} 和指定的日期。
     * 如果终点在起点之前，结果将为负数。年、月和日的负号将相同。
     * <p>
     * 计算是使用此日期的历法系统进行的。如果需要，输入日期将被转换以匹配。
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param endDateExclusive  终点，不为空，可以是任何历法系统的日期
     * @return 从该日期到终点的期间，不为空
     * @throws DateTimeException 如果无法计算期间
     * @throws ArithmeticException 如果发生数值溢出
     */
    ChronoPeriod until(ChronoLocalDate endDateExclusive);

    /**
     * 使用指定的格式化器格式化此日期。
     * <p>
     * 此日期将传递给格式化器以生成字符串。
     * <p>
     * 默认实现必须如下所示：
     * <pre>
     *  return formatter.format(this);
     * </pre>
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的日期字符串，不为空
     * @throws DateTimeException 如果打印时发生错误
     */
    default String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期与时间组合以创建 {@code ChronoLocalDateTime}。
     * <p>
     * 此方法返回一个由此日期和指定时间组成的 {@code ChronoLocalDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param localTime  要使用的本地时间，不为空
     * @return 由此日期和指定时间组成的本地日期时间，不为空
     */
    @SuppressWarnings("unchecked")
    default ChronoLocalDateTime<?> atTime(LocalTime localTime) {
        return ChronoLocalDateTimeImpl.of(this, localTime);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此日期转换为 Epoch Day。
     * <p>
     * {@link ChronoField#EPOCH_DAY Epoch Day 计数} 是一个简单的递增天数计数，其中第 0 天是 1970-01-01（ISO）。
     * 此定义对所有历法系统都是相同的，可以实现转换。
     * <p>
     * 默认实现查询 {@code EPOCH_DAY} 字段。
     *
     * @return 等效于此日期的 Epoch Day
     */
    default long toEpochDay() {
        return getLong(EPOCH_DAY);
    }


                //-----------------------------------------------------------------------
    /**
     * 比较此日期与另一个日期，包括编年史。
     * <p>
     * 比较首先基于底层时间线日期，然后基于编年史。
     * 它与 {@link Comparable} 中定义的“与 equals 一致”。
     * <p>
     * 例如，以下是比较器顺序：
     * <ol>
     * <li>{@code 2012-12-03 (ISO)}</li>
     * <li>{@code 2012-12-04 (ISO)}</li>
     * <li>{@code 2555-12-04 (ThaiBuddhist)}</li>
     * <li>{@code 2012-12-05 (ISO)}</li>
     * </ol>
     * 值 #2 和 #3 表示时间线上的同一日期。
     * 当两个值表示同一日期时，比较编年史 ID 以区分它们。
     * 这一步是使排序“与 equals 一致”所必需的。
     * <p>
     * 如果所有被比较的日期对象都在同一编年史中，则不需要额外的编年史阶段，仅使用本地日期。
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同编年史中的日期，可以使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     * <p>
     * 此默认实现执行上述定义的比较。
     *
     * @param other  要比较的其他日期，不为 null
     * @return 比较值，负数表示小于，正数表示大于
     */
    @Override
    default int compareTo(ChronoLocalDate other) {
        int cmp = Long.compare(toEpochDay(), other.toEpochDay());
        if (cmp == 0) {
            cmp = getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    /**
     * 检查此日期是否在指定日期之后，忽略编年史。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期，而不是编年史。
     * 这允许不同日历系统中的日期基于时间线位置进行比较。
     * 这相当于使用 {@code date1.toEpochDay() > date2.toEpochDay()}。
     * <p>
     * 此默认实现基于 epoch-day 进行比较。
     *
     * @param other  要比较的其他日期，不为 null
     * @return 如果此日期在指定日期之后，返回 true
     */
    default boolean isAfter(ChronoLocalDate other) {
        return this.toEpochDay() > other.toEpochDay();
    }

    /**
     * 检查此日期是否在指定日期之前，忽略编年史。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期，而不是编年史。
     * 这允许不同日历系统中的日期基于时间线位置进行比较。
     * 这相当于使用 {@code date1.toEpochDay() < date2.toEpochDay()}。
     * <p>
     * 此默认实现基于 epoch-day 进行比较。
     *
     * @param other  要比较的其他日期，不为 null
     * @return 如果此日期在指定日期之前，返回 true
     */
    default boolean isBefore(ChronoLocalDate other) {
        return this.toEpochDay() < other.toEpochDay();
    }

    /**
     * 检查此日期是否等于指定日期，忽略编年史。
     * <p>
     * 此方法与 {@link #compareTo} 中的比较不同，因为它仅比较底层日期，而不是编年史。
     * 这允许不同日历系统中的日期基于时间线位置进行比较。
     * 这相当于使用 {@code date1.toEpochDay() == date2.toEpochDay()}。
     * <p>
     * 此默认实现基于 epoch-day 进行比较。
     *
     * @param other  要比较的其他日期，不为 null
     * @return 如果底层日期等于指定日期，返回 true
     */
    default boolean isEqual(ChronoLocalDate other) {
        return this.toEpochDay() == other.toEpochDay();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此日期是否等于另一个日期，包括编年史。
     * <p>
     * 比较此日期与另一个日期，确保日期和编年史相同。
     * <p>
     * 要比较两个 {@code TemporalAccessor} 实例的日期，包括两个不同编年史中的日期，可以使用 {@link ChronoField#EPOCH_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此日期等于其他日期，返回 true
     */
    @Override
    boolean equals(Object obj);

    /**
     * 为此日期生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * 将此日期输出为 {@code String}。
     * <p>
     * 输出将包括完整的本地日期。
     *
     * @return 格式化的日期，不为 null
     */
    @Override
    String toString();

}
