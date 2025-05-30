/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2013, Stephen Colebourne & Michael Nascimento Santos
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

import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.util.Objects;

/**
 * 一个基于日期的时间量，例如“3年，4个月和5天”，在任意历法系统中，旨在用于高级全球化用例。
 * <p>
 * 此接口在历法系统中建模基于日期的时间量。
 * 尽管大多数历法系统使用年、月和日，但有些不使用。
 * 因此，此接口仅以由 {@code Chronology} 定义的一组支持的单位来操作。
 * 一组支持的单位对于给定的历法是固定的。
 * 支持的单位的量可以设置为零。
 * <p>
 * 该时间段被建模为有方向的时间量，意味着时间段的各个部分可能是负数。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有可以实例化的实现都必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @since 1.8
 */
public interface ChronoPeriod
        extends TemporalAmount {

    /**
     * 获取由两个日期之间的时间量组成的 {@code ChronoPeriod}。
     * <p>
     * 起始日期包含在内，但结束日期不包含在内。
     * 该时间段使用 {@link ChronoLocalDate#until(ChronoLocalDate)} 计算。
 * 因此，计算是特定于历法的。
     * <p>
     * 使用第一个日期的历法。
     * 第二个日期的历法被忽略，该日期在计算开始前被转换为目标历法系统。
     * <p>
     * 如果结束日期在起始日期之前，此方法的结果可以是一个负时间段。
     * 在大多数情况下，支持字段中的正/负符号将是相同的。
     *
     * @param startDateInclusive  起始日期，包含，指定计算的历法，不为空
     * @param endDateExclusive  结束日期，不包含，任何历法，不为空
     * @return 该日期和结束日期之间的时间段，不为空
     * @see ChronoLocalDate#until(ChronoLocalDate)
     */
    public static ChronoPeriod between(ChronoLocalDate startDateInclusive, ChronoLocalDate endDateExclusive) {
        Objects.requireNonNull(startDateInclusive, "startDateInclusive");
        Objects.requireNonNull(endDateExclusive, "endDateExclusive");
        return startDateInclusive.until(endDateExclusive);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取请求单位的值。
     * <p>
     * 支持的单位是特定于历法的。
     * 它们通常是 {@link ChronoUnit#YEARS YEARS}、
     * {@link ChronoUnit#MONTHS MONTHS} 和 {@link ChronoUnit#DAYS DAYS}。
     * 请求不支持的单位将抛出异常。
     *
     * @param unit 用于返回值的 {@code TemporalUnit}
     * @return 单位的 long 值
     * @throws DateTimeException 如果单位不支持
     * @throws UnsupportedTemporalTypeException 如果单位不支持
     */
    @Override
    long get(TemporalUnit unit);

    /**
     * 获取此时间段支持的单位集。
     * <p>
     * 支持的单位是特定于历法的。
     * 它们通常是 {@link ChronoUnit#YEARS YEARS}、
     * {@link ChronoUnit#MONTHS MONTHS} 和 {@link ChronoUnit#DAYS DAYS}。
     * 它们按从大到小的顺序返回。
     * <p>
     * 该集合可以与 {@link #get(TemporalUnit)} 结合使用
     * 以访问时间段的整个状态。
     *
     * @return 包含支持单位的列表，不为空
     */
    @Override
    List<TemporalUnit> getUnits();

    /**
     * 获取定义支持单位含义的历法。
     * <p>
     * 该时间段由历法定义。
     * 它控制支持的单位并限制加法/减法
     * 仅限于相同历法的 {@code ChronoLocalDate} 实例。
     *
     * @return 定义该时间段的历法，不为空
     */
    Chronology getChronology();

    //-----------------------------------------------------------------------
    /**
     * 检查此时间段的所有支持单位是否为零。
     *
     * @return 如果此时间段为零长度，则返回 true
     */
    default boolean isZero() {
        for (TemporalUnit unit : getUnits()) {
            if (get(unit) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查此时间段的任何支持单位是否为负数。
     *
     * @return 如果此时间段的任何单位为负数，则返回 true
     */
    default boolean isNegative() {
        for (TemporalUnit unit : getUnits()) {
            if (get(unit) < 0) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此时间段的副本，添加了指定的时间段。
     * <p>
     * 如果指定的量是 {@code ChronoPeriod}，则它必须具有
     * 与该时间段相同的历法。实现可以选择接受或拒绝其他
     * {@code TemporalAmount} 实现。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd 要添加的时间段，不为空
     * @return 基于此时间段并添加了请求时间段的 {@code ChronoPeriod}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    ChronoPeriod plus(TemporalAmount amountToAdd);

    /**
     * 返回一个基于此时间段的副本，减去了指定的时间段。
     * <p>
     * 如果指定的量是 {@code ChronoPeriod}，则它必须具有
     * 与该时间段相同的历法。实现可以选择接受或拒绝其他
     * {@code TemporalAmount} 实现。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract 要减去的时间段，不为空
     * @return 基于此时间段并减去了请求时间段的 {@code ChronoPeriod}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    ChronoPeriod minus(TemporalAmount amountToSubtract);

    //-----------------------------------------------------------------------
    /**
     * 返回一个新实例，其中此时间段中的每个量乘以指定的标量。
     * <p>
     * 这将返回一个时间段，其中每个支持的单位分别乘以。
     * 例如，一个“2年，-3个月和4天”的时间段乘以
     * 3 将返回“6年，-9个月和12天”。
     * 没有进行规范化。
     *
     * @param scalar 要乘以的标量，不为空
     * @return 基于此时间段并乘以标量的 {@code ChronoPeriod}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    ChronoPeriod multipliedBy(int scalar);

    /**
     * 返回一个新实例，其中此时间段中的每个量取反。
     * <p>
     * 这将返回一个时间段，其中每个支持的单位分别取反。
     * 例如，一个“2年，-3个月和4天”的时间段将被
     * 取反为“-2年，3个月和-4天”。
     * 没有进行规范化。
     *
     * @return 基于此时间段并取反的 {@code ChronoPeriod}，不为空
     * @throws ArithmeticException 如果发生数值溢出，这仅在
     * 一个单位的值为 {@code Long.MIN_VALUE} 时发生
     */
    default ChronoPeriod negated() {
        return multipliedBy(-1);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个基于此时间段的副本，其中每个单位的量已规范化。
     * <p>
     * 规范化的过程特定于每个历法系统。
     * 例如，在 ISO 历法系统中，年和月被规范化，但日不被规范化，因此
     * “15个月”将被规范化为“1年和3个月”。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此时间段并规范化了每个单位的量的 {@code ChronoPeriod}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    ChronoPeriod normalized();

    //-------------------------------------------------------------------------
    /**
     * 将此时间段添加到指定的时间对象。
     * <p>
     * 这将返回一个与输入具有相同可观察类型的临时对象
     * 并添加了此时间段。
     * <p>
     * 在大多数情况下，通过使用
     * {@link Temporal#plus(TemporalAmount)} 反转调用模式更为清晰。
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   dateTime = thisPeriod.addTo(dateTime);
     *   dateTime = dateTime.plus(thisPeriod);
     * </pre>
     * <p>
     * 指定的临时对象必须具有与该时间段相同的历法。
     * 这将返回一个临时对象，其中非零支持单位已添加。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal 要调整的临时对象，不为空
     * @return 已进行调整的相同类型的对象，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    Temporal addTo(Temporal temporal);

    /**
     * 从指定的时间对象中减去此时间段。
     * <p>
     * 这将返回一个与输入具有相同可观察类型的临时对象
     * 并减去了此时间段。
     * <p>
     * 在大多数情况下，通过使用
     * {@link Temporal#minus(TemporalAmount)} 反转调用模式更为清晰。
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   dateTime = thisPeriod.subtractFrom(dateTime);
     *   dateTime = dateTime.minus(thisPeriod);
     * </pre>
     * <p>
     * 指定的临时对象必须具有与该时间段相同的历法。
     * 这将返回一个临时对象，其中非零支持单位已减去。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal 要调整的临时对象，不为空
     * @return 已进行调整的相同类型的对象，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    Temporal subtractFrom(Temporal temporal);

    //-----------------------------------------------------------------------
    /**
     * 检查此时间段是否等于另一个时间段，包括历法。
     * <p>
     * 比较此时间段与另一个时间段，确保类型、每个量和
     * 历法相同。
     * 请注意，这意味着一个“15个月”的时间段不等于一个
     * “1年和3个月”的时间段。
     *
     * @param obj 要检查的对象，为空返回 false
     * @return 如果此时间段等于其他时间段，则返回 true
     */
    @Override
    boolean equals(Object obj);

    /**
     * 为该时间段生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * 将此时间段输出为 {@code String}。
     * <p>
     * 输出将包括时间段的量和历法。
     *
     * @return 该时间段的字符串表示，不为空
     */
    @Override
    String toString();

}
