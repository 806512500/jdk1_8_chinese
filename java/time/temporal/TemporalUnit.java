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
import java.time.Duration;
import java.time.LocalTime;
import java.time.Period;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;

/**
 * 日期时间单位，例如天或小时。
 * <p>
 * 时间测量基于单位，如年、月、日、小时、分钟和秒。
 * 该接口的实现代表这些单位。
 * <p>
 * 该接口的实例代表单位本身，而不是单位的数量。
 * 有关以常见单位表示数量的类，请参见 {@link Period}。
 * <p>
 * 最常用的单位定义在 {@link ChronoUnit} 中。
 * 进一步的单位在 {@link IsoFields} 中提供。
 * 也可以通过实现此接口来编写应用程序代码中的单位。
 * <p>
 * 单位的工作原理是双分派。客户端代码调用类似 {@code LocalDateTime} 的日期时间方法，这些方法会检查单位是否为 {@code ChronoUnit}。
 * 如果是，则日期时间必须处理它。
 * 否则，方法调用将重新分派到此接口中的相应方法。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * 建议尽可能使用枚举。
 *
 * @since 1.8
 */
public interface TemporalUnit {

    /**
     * 获取此单位的持续时间，这可能是估计值。
     * <p>
     * 所有单位从此方法返回的持续时间都以标准纳秒为单位。
     * 持续时间将为正且非零。
     * 例如，一小时的持续时间为 {@code 60 * 60 * 1,000,000,000ns}。
     * <p>
     * 一些单位可能返回准确的持续时间，而另一些则返回估计值。
     * 例如，由于夏令时变化的可能性，天的持续时间为估计值。
     * 要确定持续时间是否为估计值，请使用 {@link #isDurationEstimated()}。
     *
     * @return 此单位的持续时间，这可能是估计值，不为 null
     */
    Duration getDuration();

    /**
     * 检查单位的持续时间是否为估计值。
     * <p>
     * 所有单位都有持续时间，但持续时间并不总是准确的。
     * 例如，由于夏令时变化的可能性，天的持续时间为估计值。
     * 如果持续时间是估计值，则此方法返回 true，否则返回 false。
     * 请注意，准确/估计忽略了闰秒。
     *
     * @return 如果持续时间是估计值，则返回 true，否则返回 false
     */
    boolean isDurationEstimated();

    //-----------------------------------------------------------------------
    /**
     * 检查此单位是否表示日期的组成部分。
     * <p>
     * 如果可以从日期中推断出意义，则该日期是时间性的。
     * 它必须具有 {@linkplain #getDuration() 持续时间}，该持续时间是标准天长度的整数倍。
     * 请注意，{@code isDateBased()} 和 {@code isTimeBased()} 都可以返回 false，例如表示 36 小时的单位。
     *
     * @return 如果此单位是日期的组成部分，则返回 true
     */
    boolean isDateBased();

    /**
     * 检查此单位是否表示时间的组成部分。
     * <p>
     * 如果可以从时间中推断出意义，则该单位是时间性的。
     * 它必须具有 {@linkplain #getDuration() 持续时间}，该持续时间可以整除标准天的长度。
     * 请注意，{@code isDateBased()} 和 {@code isTimeBased()} 都可以返回 false，例如表示 36 小时的单位。
     *
     * @return 如果此单位是时间的组成部分，则返回 true
     */
    boolean isTimeBased();

    //-----------------------------------------------------------------------
    /**
     * 检查指定的时间对象是否支持此单位。
     * <p>
     * 这检查实现的日期时间是否可以添加/减去此单位。
     * 这可以用来避免抛出异常。
     * <p>
     * 此默认实现使用 {@link Temporal#plus(long, TemporalUnit)} 衍生值。
     *
     * @param temporal  要检查的时间对象，不为 null
     * @return 如果单位受支持，则返回 true
     */
    default boolean isSupportedBy(Temporal temporal) {
        if (temporal instanceof LocalTime) {
            return isTimeBased();
        }
        if (temporal instanceof ChronoLocalDate) {
            return isDateBased();
        }
        if (temporal instanceof ChronoLocalDateTime || temporal instanceof ChronoZonedDateTime) {
            return true;
        }
        try {
            temporal.plus(1, this);
            return true;
        } catch (UnsupportedTemporalTypeException ex) {
            return false;
        } catch (RuntimeException ex) {
            try {
                temporal.plus(-1, this);
                return true;
            } catch (RuntimeException ex2) {
                return false;
            }
        }
    }

    /**
     * 返回指定时间对象的副本，并添加指定的周期。
     * <p>
     * 添加的周期是此单位的倍数。例如，此方法可以用于通过调用表示“天”的实例上的此方法，向日期添加“3天”，传递日期和周期“3”。
     * 要添加的周期可以是负数，这相当于减法。
     * <p>
     * 使用此方法有两种等效方式。
     * 第一种是直接调用此方法。
     * 第二种是使用 {@link Temporal#plus(long, TemporalUnit)}：
     * <pre>
     *   // 这两行是等效的，但推荐使用第二种方法
     *   temporal = thisUnit.addTo(temporal);
     *   temporal = temporal.plus(thisUnit);
     * </pre>
     * 推荐使用第二种方法，{@code plus(TemporalUnit)}，因为它在代码中更易读。
     * <p>
     * 实现应使用 {@link ChronoUnit} 中可用的单位或 {@link ChronoField} 中可用的字段进行任何查询或计算。
     * 如果单位不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 实现不得修改指定的时间对象。
     * 相反，必须返回原始对象的调整副本。
     * 这为不可变和可变实现提供了等效的安全行为。
     *
     * @param <R>  时间对象的类型
     * @param temporal  要调整的时间对象，不为 null
     * @param amount  要添加的此单位的数量，可以是正数或负数
     * @return 调整后的时间对象，不为 null
     * @throws DateTimeException 如果无法添加数量
     * @throws UnsupportedTemporalTypeException 如果单位不受时间对象支持
     */
    <R extends Temporal> R addTo(R temporal, long amount);

    //-----------------------------------------------------------------------
    /**
     * 计算两个时间对象之间的时间量。
     * <p>
     * 这按此单位计算时间量。起始点和结束点作为时间对象提供，必须是兼容的类型。
     * 实现将把第二个类型转换为第一个类型的实例，然后再计算时间量。
     * 如果结束时间在开始时间之前，则结果将为负。
     * 例如，可以使用 {@code HOURS.between(startTime, endTime)} 计算两个时间对象之间的时间量（以小时为单位）。
     * <p>
     * 计算返回一个整数，表示两个时间对象之间完整单位的数量。
     * 例如，时间 11:30 和 13:29 之间的时间量（以小时为单位）将仅为一小时，因为还差一分钟才到两小时。
     * <p>
     * 使用此方法有两种等效方式。
     * 第一种是直接调用此方法。
     * 第二种是使用 {@link Temporal#until(Temporal, TemporalUnit)}：
     * <pre>
     *   // 这两行是等效的
     *   between = thisUnit.between(start, end);
     *   between = start.until(end, thisUnit);
     * </pre>
     * 选择应基于哪一种使代码更易读。
     * <p>
     * 例如，此方法允许计算两个日期之间的时间量（以天为单位）：
     * <pre>
     *  long daysBetween = DAYS.between(start, end);
     *  // 或者
     *  long daysBetween = start.until(end, DAYS);
     * </pre>
     * <p>
     * 实现应使用 {@link ChronoUnit} 中可用的单位或 {@link ChronoField} 中可用的字段进行任何查询或计算。
     * 如果单位不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * 实现不得修改指定的时间对象。
     *
     * @implSpec
     * 实现必须首先使用 {@code getClass()} 检查两个时间对象是否具有相同的类型。
     * 如果它们不相同，则结果必须通过调用 {@code temporal1Inclusive.until(temporal2Exclusive, this)} 获得。
     *
     * @param temporal1Inclusive  基础时间对象，不为 null
     * @param temporal2Exclusive  另一个时间对象，不为 null
     * @return 从 temporal1Inclusive 到 temporal2Exclusive 的时间量，以此单位表示；
     *         如果 temporal2Exclusive 晚于 temporal1Inclusive，则为正数，否则为负数
     * @throws DateTimeException 如果无法计算时间量，或者结束时间对象无法转换为与开始时间对象相同的类型
     * @throws UnsupportedTemporalTypeException 如果单位不受时间对象支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);

    //-----------------------------------------------------------------------
    /**
     * 获取单位的描述性名称。
     * <p>
     * 该名称应为复数形式，首字母大写，如 'Days' 或 'Minutes'。
     *
     * @return 此单位的名称，不为 null
     */
    @Override
    String toString();

}
