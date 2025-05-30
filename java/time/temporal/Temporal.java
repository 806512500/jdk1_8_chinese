
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
package java.time.temporal;

import java.time.DateTimeException;

/**
 * 定义对时间对象（如日期、时间、偏移量或这些的组合）进行读写访问的框架级接口。
 * <p>
 * 这是那些可以使用加法和减法进行操作的日期、时间和偏移量对象的基础接口类型。
 * 它由那些可以提供和操作信息作为 {@linkplain TemporalField 字段} 或 {@linkplain TemporalQuery 查询} 的类实现。
 * 请参见 {@link TemporalAccessor} 以获取此接口的只读版本。
 * <p>
 * 大多数日期和时间信息可以表示为数字。
 * 这些信息使用 {@code TemporalField} 建模，数字使用 {@code long} 存储以处理大值。年、月和日是简单示例，但它们还包括瞬时和偏移量。
 * 请参见 {@link ChronoField} 以获取标准字段集。
 * <p>
 * 两件日期/时间信息无法用数字表示，即 {@linkplain java.time.chrono.Chronology 日历系统} 和 {@linkplain java.time.ZoneId 时区}。
 * 这些可以通过 {@link #query(TemporalQuery) 查询} 使用 {@link TemporalQuery} 定义的静态方法访问。
 * <p>
 * 此接口是一个框架级接口，不应广泛用于应用程序代码中。相反，应用程序应创建并传递具体类型的实例，如 {@code LocalDate}。
 * 这样做的原因有很多，部分原因是此接口的实现可能使用非 ISO 日历系统。
 * 请参见 {@link java.time.chrono.ChronoLocalDate} 以获取更详细的讨论。
 *
 * <h3>何时实现</h3>
 * <p>
 * 一个类如果满足以下三个标准，则应实现此接口：
 * <ul>
 * <li>它提供对日期/时间/偏移量信息的访问，如同 {@code TemporalAccessor}
 * <li>字段集从最大到最小是连续的
 * <li>字段集是完整的，不需要其他字段来定义所表示字段的有效范围
 * </ul>
 * <p>
 * 四个示例可以澄清这一点：
 * <ul>
 * <li>{@code LocalDate} 实现此接口，因为它表示一组从天到永远的连续字段，并且不需要外部信息来确定每个日期的有效性。因此，它可以正确实现加法/减法。
 * <li>{@code LocalTime} 实现此接口，因为它表示一组从纳秒到天的连续字段，并且不需要外部信息来确定有效性。它可以通过环绕天来正确实现加法/减法。
 * <li>{@code MonthDay}，即年中的月和月中的日的组合，不实现此接口。虽然组合是连续的，从天到年中的月，但该组合没有足够的信息来定义月中的日的有效范围。因此，它无法正确实现加法/减法。
 * <li>星期几和月中的日的组合（如“星期五13日”）不应实现此接口。它不表示一组连续的字段，因为天到周与天到月重叠。
 * </ul>
 *
 * @implSpec
 * 此接口对实现的可变性没有限制，但强烈建议不可变。
 * 所有实现必须是 {@link Comparable}。
 *
 * @since 1.8
 */
public interface Temporal extends TemporalAccessor {

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 这会检查指定的单位是否可以添加到或从这个日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoUnit} 中定义的所有单位。
     * 如果单位支持，则必须返回 true，否则返回 false。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.isSupportedBy(Temporal)}
     * 并将 {@code this} 作为参数传递获得的。
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察状态。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    boolean isSupported(TemporalUnit unit);

    /**
     * 返回一个与该对象类型相同且已进行调整的对象。
     * <p>
     * 根据指定调整器的规则调整此日期时间。
     * 简单的调整器可能只是设置一个字段，如年字段。
     * 更复杂的调整器可能将日期设置为该月的最后一天。
     * {@link java.time.temporal.TemporalAdjusters TemporalAdjusters} 提供了一些常见的调整，如“该月的最后一天”和“下一个星期三”。
     * 调整器负责处理特殊情况，如月份长度不同和闰年。
     * <p>
     * 以下是一些示例代码，说明如何以及为何使用此方法：
     * <pre>
     *  date = date.with(Month.JULY);        // 大多数关键类实现了 TemporalAdjuster
     *  date = date.with(lastDayOfMonth());  // 从 Adjusters 静态导入
     *  date = date.with(next(WEDNESDAY));   // 从 Adjusters 和 DayOfWeek 静态导入
     * </pre>
     *
     * @implSpec
     * <p>
     * 实现不得更改此对象或指定的时间对象。
     * 相反，必须返回一个调整后的副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     * <p>
     * 默认实现必须等效于以下代码：
     * <pre>
     *  return adjuster.adjustInto(this);
     * </pre>
     *
     * @param adjuster  要使用的调整器，不为 null
     * @return 一个已进行指定调整且类型相同的对象，不为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    default Temporal with(TemporalAdjuster adjuster) {
        return adjuster.adjustInto(this);
    }

    /**
     * 返回一个与该对象类型相同且指定字段已更改的对象。
     * <p>
     * 基于此对象返回一个新对象，指定字段的值已更改。
     * 例如，在 {@code LocalDate} 上，这可以用于设置年、月或日。
     * 返回的对象将具有与该对象相同的可观察类型。
     * <p>
     * 在某些情况下，更改字段是不完全定义的。例如，如果目标对象是表示1月31日的日期，则将月更改为2月将不清楚。
     * 在这种情况下，字段负责解决结果。通常它会选择前一个有效日期，例如在此示例中选择2月的最后一天。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoField} 中定义的所有字段。
     * 如果字段支持，则必须进行调整。
     * 如果不支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 是通过调用 {@code TemporalField.adjustInto(Temporal, long)}
     * 并将 {@code this} 作为第一个参数传递获得的。
     * <p>
     * 实现不得更改此对象。
     * 相反，必须返回一个调整后的副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     *
     * @param field  要在结果中设置的字段，不为 null
     * @param newValue  结果中字段的新值
     * @return 一个已设置指定字段且类型相同的对象，不为 null
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    Temporal with(TemporalField field, long newValue);

    //-----------------------------------------------------------------------
    /**
     * 返回一个与该对象类型相同且已添加指定数量的对象。
     * <p>
     * 调整此时间对象，根据指定数量的规则进行添加。
     * 数量通常是 {@link java.time.Period}，但可以是实现
     * {@link TemporalAmount} 接口的任何其他类型，如 {@link java.time.Duration}。
     * <p>
     * 以下是一些示例代码，说明如何以及为何使用此方法：
     * <pre>
     *  date = date.plus(period);                // 添加一个 Period 实例
     *  date = date.plus(duration);              // 添加一个 Duration 实例
     *  date = date.plus(workingDays(6));        // 示例用户编写的 workingDays 方法
     * </pre>
     * <p>
     * 注意，调用 {@code plus} 后再调用 {@code minus} 不保证返回相同的时间。
     *
     * @implSpec
     * <p>
     * 实现不得更改此对象或指定的时间对象。
     * 相反，必须返回一个调整后的副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     * <p>
     * 默认实现必须等效于以下代码：
     * <pre>
     *  return amount.addTo(this);
     * </pre>
     *
     * @param amount  要添加的数量，不为 null
     * @return 一个已进行指定调整且类型相同的对象，不为 null
     * @throws DateTimeException 如果无法进行添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    default Temporal plus(TemporalAmount amount) {
        return amount.addTo(this);
    }

    /**
     * 返回一个与该对象类型相同且已添加指定周期的对象。
     * <p>
     * 基于此对象返回一个新对象，指定周期已添加。
     * 例如，在 {@code LocalDate} 上，这可以用于添加年、月或日。
     * 返回的对象将具有与该对象相同的可观察类型。
     * <p>
     * 在某些情况下，更改字段是不完全定义的。例如，如果目标对象是表示1月31日的日期，则添加一个月将不清楚。
     * 在这种情况下，字段负责解决结果。通常它会选择前一个有效日期，例如在此示例中选择2月的最后一天。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoUnit} 中定义的所有单位。
     * 如果单位支持，则必须进行添加。
     * 如果不支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.addTo(Temporal, long)}
     * 并将 {@code this} 作为第一个参数传递获得的。
     * <p>
     * 实现不得更改此对象。
     * 相反，必须返回一个调整后的副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     *
     * @param amountToAdd  要添加的指定单位的数量，可以为负数
     * @param unit  要添加的数量的单位，不为 null
     * @return 一个已添加指定周期且类型相同的对象，不为 null
     * @throws DateTimeException 如果无法添加单位
     * @throws UnsupportedTemporalTypeException 如果单位不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    Temporal plus(long amountToAdd, TemporalUnit unit);

    //-----------------------------------------------------------------------
    /**
     * 返回一个与该对象类型相同且已减去指定数量的对象。
     * <p>
     * 调整此时间对象，根据指定数量的规则进行减去。
     * 数量通常是 {@link java.time.Period}，但可以是实现
     * {@link TemporalAmount} 接口的任何其他类型，如 {@link java.time.Duration}。
     * <p>
     * 以下是一些示例代码，说明如何以及为何使用此方法：
     * <pre>
     *  date = date.minus(period);               // 减去一个 Period 实例
     *  date = date.minus(duration);             // 减去一个 Duration 实例
     *  date = date.minus(workingDays(6));       // 示例用户编写的 workingDays 方法
     * </pre>
     * <p>
     * 注意，调用 {@code plus} 后再调用 {@code minus} 不保证返回相同的时间。
     *
     * @implSpec
     * <p>
     * 实现不得更改此对象或指定的时间对象。
     * 相反，必须返回一个调整后的副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     * <p>
     * 默认实现必须等效于以下代码：
     * <pre>
     *  return amount.subtractFrom(this);
     * </pre>
     *
     * @param amount  要减去的数量，不为 null
     * @return 一个已进行指定调整且类型相同的对象，不为 null
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    default Temporal minus(TemporalAmount amount) {
        return amount.subtractFrom(this);
    }


                /**
     * 返回一个与该对象类型相同且指定了减去的时间段的对象。
     * <p>
     * 此方法返回一个基于此对象的新对象，指定了减去的时间段。
     * 例如，在 {@code LocalDate} 上，这可以用于减去一定数量的年、月或日。
     * 返回的对象将具有与此对象相同的可观察类型。
     * <p>
     * 在某些情况下，更改字段是不完全定义的。例如，如果目标对象是表示 3 月 31 日的日期，
     * 那么减去一个月将是不明确的。在这种情况下，字段负责解决结果。通常它会选择
     * 上一个有效的日期，例如在这个例子中会选择 2 月的最后一天。
     *
     * @implSpec
     * 实现必须以与默认方法行为等效的方式行为。
     * <p>
     * 实现不得更改此对象。相反，必须返回原始对象的调整副本。
     * 这为不可变和可变实现提供了等效且安全的行为。
     * <p>
     * 默认实现必须以等效于以下代码的方式行为：
     * <pre>
     *  return (amountToSubtract == Long.MIN_VALUE ?
     *      plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
     * </pre>
     *
     * @param amountToSubtract  要减去的指定单位的数量，可以为负
     * @param unit  要减去的单位，不为 null
     * @return 一个与该对象类型相同且指定了减去的时间段的对象，不为 null
     * @throws DateTimeException 如果无法减去该单位
     * @throws UnsupportedTemporalTypeException 如果该单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    default Temporal minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 计算以指定单位表示的到另一个时间对象的时间量。
     * <p>
     * 这计算两个时间对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的时间对象。
     * 如果终点类型不同，则会转换为与起点相同的类型。
     * 如果终点在起点之前，结果将为负。
     * 例如，可以使用 {@code startTime.until(endTime, HOURS)} 计算两个时间对象之间的时间量（以小时为单位）。
     * <p>
     * 计算结果是一个整数，表示两个时间对象之间完整的单位数。
     * 例如，时间 11:30 和 13:29 之间的时间量（以小时为单位）将仅为一小时，因为还差一分钟才到两小时。
     * <p>
     * 使用此方法有两种等效的方式。第一种是直接调用此方法。第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   temporal = start.until(end, unit);
     *   temporal = unit.between(start, end);
     * </pre>
     * 选择应基于哪一种使代码更易读。
     * <p>
     * 例如，此方法允许计算两个日期之间的天数：
     * <pre>
     *  long daysBetween = start.until(end, DAYS);
     *  // 或者
     *  long daysBetween = DAYS.between(start, end);
     * </pre>
     *
     * @implSpec
     * 实现必须首先检查输入的时间对象是否与实现具有相同的可观察类型。
     * 然后必须为所有 {@link ChronoUnit} 实例执行计算。
     * 对于不受支持的 {@code ChronoUnit} 实例，必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并将 {@code this} 作为第一个参数，将转换后的输入时间对象作为第二个参数来获得的。
     * <p>
     * 总之，实现必须以等效于以下伪代码的方式行为：
     * <pre>
     *  // 将终点时间对象转换为与此类相同的类型
     *  if (unit instanceof ChronoUnit) {
     *    // 如果单位受支持，则计算并返回结果
     *    // 否则对于不受支持的单位抛出 UnsupportedTemporalTypeException
     *  }
     *  return unit.between(this, convertedEndTemporal);
     * </pre>
     * <p>
     * 注意，只有当两个时间对象的类型通过 {@code getClass()} 评估为完全相同时，才能调用单位的 {@code between} 方法。
     * <p>
     * 实现必须确保在调用此只读方法时没有可观察状态被更改。
     *
     * @param endExclusive  终点时间对象，不为 null，转换为与此对象相同的类型
     * @param unit  要测量的时间量的单位，不为 null
     * @return 以该单位表示的此时间对象与指定时间对象之间的时间量；
     *  如果指定的时间对象晚于此对象，则为正；如果早于此对象，则为负
     * @throws DateTimeException 如果无法计算时间量，或者无法将终点时间对象转换为与此时间对象相同的类型
     * @throws UnsupportedTemporalTypeException 如果该单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    long until(Temporal endExclusive, TemporalUnit unit);

}
