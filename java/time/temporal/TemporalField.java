
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
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 日期时间字段，例如年中的月份或分钟中的小时。
 * <p>
 * 日期和时间是通过将时间线划分为对人类有意义的部分来表示的。此接口的实现代表这些字段。
 * <p>
 * 最常用的单位定义在 {@link ChronoField} 中。
 * 更多的字段在 {@link IsoFields}、{@link WeekFields} 和 {@link JulianFields} 中提供。
 * 字段也可以通过实现此接口由应用程序代码编写。
 * <p>
 * 字段的工作原理是双重调度。客户端代码调用类似 {@code LocalDateTime} 的日期时间方法，这些方法会检查字段是否为 {@code ChronoField}。
 * 如果是，则日期时间必须处理它。否则，方法调用将重新调度到此接口中的相应方法。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可以实例化的实现都必须是最终的、不可变的和线程安全的。
 * 实现应尽可能实现 {@code Serializable}。
 * 枚举是有效的实现选择。
 *
 * @since 1.8
 */
public interface TemporalField {

    /**
     * 获取请求区域设置的字段显示名称。
     * <p>
     * 如果没有该区域设置的显示名称，则必须返回一个合适的默认值。
     * <p>
     * 默认实现必须检查区域设置不为 null，并返回 {@code toString()}。
     *
     * @param locale  要使用的区域设置，不为 null
     * @return 区域设置的显示名称或合适的默认值，不为 null
     */
    default String getDisplayName(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        return toString();
    }

    /**
     * 获取字段的测量单位。
     * <p>
     * 字段的单位是在范围内变化的周期。例如，在字段 'MonthOfYear' 中，单位是 'Months'。
     * 参见 {@link #getRangeUnit()}。
     *
     * @return 定义字段基本单位的单位，不为 null
     */
    TemporalUnit getBaseUnit();

    /**
     * 获取字段的范围。
     * <p>
     * 字段的范围是字段变化的周期。例如，在字段 'MonthOfYear' 中，范围是 'Years'。
     * 参见 {@link #getBaseUnit()}。
     * <p>
     * 范围从不为 null。例如，'Year' 字段是 'YearOfForever' 的简写。因此，它的单位是 'Years'，范围是 'Forever'。
     *
     * @return 定义字段范围的单位，不为 null
     */
    TemporalUnit getRangeUnit();

    /**
     * 获取字段的有效值范围。
     * <p>
     * 所有字段都可以表示为 {@code long} 整数。此方法返回一个描述该值有效范围的对象。
     * 此方法通常仅适用于 ISO-8601 日历系统。
     * <p>
     * 注意，结果仅描述最小和最大有效值，不应过度解读。例如，可能存在范围内的无效值。
     *
     * @return 字段的有效值范围，不为 null
     */
    ValueRange range();

    //-----------------------------------------------------------------------
    /**
     * 检查此字段是否表示日期的组成部分。
     * <p>
     * 如果字段可以从 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 导出，则该字段是基于日期的。
     * 注意，{@code isDateBased()} 和 {@code isTimeBased()} 都返回 false 是有效的，例如表示一周中的分钟数的字段。
     *
     * @return 如果此字段是日期的组成部分，则返回 true
     */
    boolean isDateBased();

    /**
     * 检查此字段是否表示时间的组成部分。
     * <p>
     * 如果字段可以从 {@link ChronoField#NANO_OF_DAY NANO_OF_DAY} 导出，则该字段是基于时间的。
     * 注意，{@code isDateBased()} 和 {@code isTimeBased()} 都返回 false 是有效的，例如表示一周中的分钟数的字段。
     *
     * @return 如果此字段是时间的组成部分，则返回 true
     */
    boolean isTimeBased();

    //-----------------------------------------------------------------------
    /**
     * 检查此字段是否受时间对象支持。
     * <p>
     * 这确定时间访问器是否支持此字段。如果此方法返回 false，则无法查询此字段。
     * <p>
     * 有两中等效的方法可以使用此方法。第一种是直接调用此方法。第二种是使用 {@link TemporalAccessor#isSupported(TemporalField)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisField.isSupportedBy(temporal);
     *   temporal = temporal.isSupported(thisField);
     * </pre>
     * 推荐使用第二种方法，{@code isSupported(TemporalField)}，因为它在代码中更容易阅读。
     * <p>
     * 实现应使用 {@link ChronoField} 中的字段来确定是否支持。
     *
     * @param temporal  要查询的时间对象，不为 null
     * @return 如果可以查询此字段，则返回 true，否则返回 false
     */
    boolean isSupportedBy(TemporalAccessor temporal);

    /**
     * 使用时间对象来细化此字段的有效值范围。
     * <p>
     * 此方法使用时间对象来查找字段的有效值范围。这类似于 {@link #range()}，但此方法使用时间来细化结果。
     * 例如，如果字段是 {@code DAY_OF_MONTH}，则 {@code range} 方法不准确，因为有四种可能的月份长度，28、29、30 和 31 天。
     * 使用此方法与日期可以确保范围准确，返回这四种选项之一。
     * <p>
     * 有两中等效的方法可以使用此方法。第一种是直接调用此方法。第二种是使用 {@link TemporalAccessor#range(TemporalField)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisField.rangeRefinedBy(temporal);
     *   temporal = temporal.range(thisField);
     * </pre>
     * 推荐使用第二种方法，{@code range(TemporalField)}，因为它在代码中更容易阅读。
     * <p>
     * 实现应使用 {@link ChronoField} 中的字段来执行任何查询或计算。如果字段不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     *
     * @param temporal  用于细化结果的时间对象，不为 null
     * @return 此字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果时间对象不支持此字段
     */
    ValueRange rangeRefinedBy(TemporalAccessor temporal);

    /**
     * 从指定的时间对象中获取此字段的值。
     * <p>
     * 此方法查询时间对象以获取此字段的值。
     * <p>
     * 有两中等效的方法可以使用此方法。第一种是直接调用此方法。第二种是使用 {@link TemporalAccessor#getLong(TemporalField)}
     * （或 {@link TemporalAccessor#get(TemporalField)}）：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisField.getFrom(temporal);
     *   temporal = temporal.getLong(thisField);
     * </pre>
     * 推荐使用第二种方法，{@code getLong(TemporalField)}，因为它在代码中更容易阅读。
     * <p>
     * 实现应使用 {@link ChronoField} 中的字段来执行任何查询或计算。如果字段不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     *
     * @param temporal  要查询的时间对象，不为 null
     * @return 此字段的值，不为 null
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果时间对象不支持此字段
     * @throws ArithmeticException 如果发生数值溢出
     */
    long getFrom(TemporalAccessor temporal);

    /**
     * 返回一个基于指定时间对象的副本，其中此字段的值已更改。
     * <p>
     * 此方法返回一个基于指定时间对象的新时间对象，其中此字段的值已更改。例如，在 {@code LocalDate} 上，可以使用此方法来设置年、月或日。
     * 返回的对象具有与指定对象相同的可观察类型。
     * <p>
     * 在某些情况下，更改字段是不完全定义的。例如，如果目标对象是表示 1 月 31 日的日期，则将月份更改为 2 月将不明确。
     * 在这种情况下，实现负责解决结果。通常，它会选择前一个有效的日期，例如在此示例中选择 2 月的最后一天。
     * <p>
     * 有两中等效的方法可以使用此方法。第一种是直接调用此方法。第二种是使用 {@link Temporal#with(TemporalField, long)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisField.adjustInto(temporal);
     *   temporal = temporal.with(thisField);
     * </pre>
     * 推荐使用第二种方法，{@code with(TemporalField)}，因为它在代码中更容易阅读。
     * <p>
     * 实现应使用 {@link ChronoField} 中的字段来执行任何查询或计算。如果字段不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 实现不得修改指定的时间对象。相反，必须返回原始对象的调整副本。这为不可变和可变实现提供了等效且安全的行为。
     *
     * @param <R>  时间对象的类型
     * @param temporal 要调整的时间对象，不为 null
     * @param newValue 字段的新值
     * @return 调整后的时间对象，不为 null
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果时间对象不支持此字段
     * @throws ArithmeticException 如果发生数值溢出
     */
    <R extends Temporal> R adjustInto(R temporal, long newValue);

    /**
     * 解析此字段以提供更简单的替代或日期。
     * <p>
     * 此方法在解析阶段调用。它旨在允许应用程序定义的字段简化为更标准的字段，例如 {@code ChronoField} 中的字段，或简化为日期。
     * <p>
     * 应用程序通常不应直接调用此方法。
     *
     * @implSpec
     * 如果实现表示可以简化的字段，或可以与其他字段组合的字段，则必须实现此方法。
     * <p>
     * 指定的映射包含解析的当前状态。映射是可变的，必须进行修改以解析字段和相关字段。此方法仅在映射包含此字段时在解析期间调用，因此实现应假设此字段存在。
     * <p>
     * 解析字段将包括查看此字段的值，以及可能的其他字段，并更新映射以包含更简单的值，例如 {@code ChronoField}，或返回完整的 {@code ChronoLocalDate}。
     * 如果解析成功，代码必须从映射中移除所有已解析的字段，包括此字段。
     * <p>
     * 例如，{@code IsoFields} 类包含季度中的月份和季度中的天数字段。该类中此方法的实现将两个字段加上 {@link ChronoField#YEAR YEAR} 解析为完整的 {@code LocalDate}。
     * 解析方法将从映射中移除所有三个字段，然后返回 {@code LocalDate}。
     * <p>
     * 部分完成的时间对象用于允许查询时区和日历。通常，只需查询日历。
     * 查询除时区或日历以外的其他内容是未定义的，不得依赖。
     * 其他方法（如 {@code get}、{@code getLong}、{@code range} 和 {@code isSupported}）的行为是不可预测的，结果是未定义的。
     * <p>
     * 如果解析应该是可能的，但数据无效，应使用解析样式来确定适当的宽容度，这可能需要抛出 {@code DateTimeException} 或 {@code ArithmeticException}。
     * 如果无法解析，解析方法必须返回 null。
     * <p>
     * 当解析时间字段时，映射将被修改并返回 null。当解析日期字段时，通常从方法返回日期，并从映射中移除已解析的字段。但是，将日期字段解析为可以生成日期的其他 {@code ChronoField} 实例（如 {@code EPOCH_DAY}）也是可以接受的。
     * <p>
     * 并非所有 {@code TemporalAccessor} 实现都接受为返回值。调用此方法的实现必须接受 {@code ChronoLocalDate}、{@code ChronoLocalDateTime}、{@code ChronoZonedDateTime} 和 {@code LocalTime}。
     * <p>
     * 默认实现必须返回 null。
     *
     * @param fieldValues  字段到值的映射，可以更新，不为 null
     * @param partialTemporal  部分完成的时间对象，用于查询时区和日历；查询其他内容是未定义的，不推荐，不为 null
     * @param resolverStyle  请求的解析类型，不为 null
     * @return 解析的时间对象；仅更改映射或未解析时返回 null
     * @throws ArithmeticException 如果发生数值溢出
     * @throws DateTimeException 如果解析导致错误。这必须不在首先检查是否支持字段的情况下查询时间对象的字段时抛出
     */
    default TemporalAccessor resolve(
            Map<TemporalField, Long> fieldValues,
            TemporalAccessor partialTemporal,
            ResolverStyle resolverStyle) {
        return null;
    }


                /**
     * 获取字段的描述性名称。
     * <p>
     * 格式应为 'BaseOfRange'，例如 'MonthOfYear'，
     * 除非字段的范围为 {@code FOREVER}，此时仅提及基本单位，例如 'Year' 或 'Era'。
     *
     * @return 字段的名称，不为空
     */
    @Override
    String toString();


}
