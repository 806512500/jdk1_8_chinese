/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2012, 2013 Stephen Colebourne & Michael Nascimento Santos
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
import java.time.Period;
import java.util.List;

/**
 * 定义时间量的框架级接口，例如 "6 小时"、"8 天" 或 "2 年 3 个月"。
 * <p>
 * 这是时间量的基本接口类型。时间量与日期或时间点不同，因为它不绑定到时间线上的任何特定点。
 * <p>
 * 时间量可以被视为 {@code Map}，其中键是 {@link TemporalUnit}，值是 {@code long}，通过 {@link #getUnits()} 和 {@link #get(TemporalUnit)} 暴露。
 * 一个简单的例子可能只有一个单位-值对，如 "6 小时"。一个更复杂的例子可能有多个单位-值对，如 "7 年、3 个月和 5 天"。
 * <p>
 * 有两个常见的实现。{@link Period} 是基于日期的实现，存储年、月和日。{@link Duration} 是基于时间的实现，存储秒和纳秒，但提供了一些使用其他基于持续时间的单位（如分钟、小时和固定 24 小时天）的访问方法。
 * <p>
 * 该接口是一个框架级接口，不建议在应用程序代码中广泛使用。相反，应用程序应创建并传递具体类型的实例，如 {@code Period} 和 {@code Duration}。
 *
 * @implSpec
 * 该接口不对实现的可变性施加任何限制，但强烈建议使用不可变性。
 *
 * @since 1.8
 */
public interface TemporalAmount {

    /**
     * 返回请求单位的值。
     * 从 {@link #getUnits()} 返回的单位唯一定义了 {@code TemporalAmount} 的值。必须为 {@code getUnits} 列表中的每个单位返回一个值。
     *
     * @implSpec
     * 实现可以声明支持 {@link #getUnits()} 列表中未列出的单位。通常，实现会定义额外的单位，作为开发人员的便利转换。
     *
     * @param unit 要返回值的 {@code TemporalUnit}
     * @return 单位的 long 值
     * @throws DateTimeException 如果无法获取单位的值
     * @throws UnsupportedTemporalTypeException 如果 {@code unit} 不受支持
     */
    long get(TemporalUnit unit);

    /**
     * 返回唯一定义此 TemporalAmount 值的单位列表。
     * {@code TemporalUnits} 列表由实现类定义。调用 {@code getUnits} 时，列表是单位的快照，且不可变。单位按持续时间从长到短的顺序排列。
     *
     * @implSpec
     * 单位列表完整且唯一地表示对象的状态，没有遗漏、重叠或重复。单位按持续时间从长到短的顺序排列。
     *
     * @return {@code TemporalUnits} 的列表；不为空
     */
    List<TemporalUnit> getUnits();

    /**
     * 将时间量添加到指定的时间对象。
     * <p>
     * 使用实现类封装的逻辑将时间量添加到指定的时间对象。
     * <p>
     * 使用此方法有两种等效的方式。第一种是直接调用此方法。第二种是使用 {@link Temporal#plus(TemporalAmount)}：
     * <pre>
     *   // 这两行是等效的，但推荐使用第二种方法
     *   dateTime = amount.addTo(dateTime);
     *   dateTime = dateTime.plus(adder);
     * </pre>
     * 推荐使用第二种方法，{@code plus(TemporalAmount)}，因为它在代码中更清晰易读。
     *
     * @implSpec
     * 实现必须将输入对象进行添加。实现定义添加逻辑，并负责记录该逻辑。它可以使用 {@code Temporal} 上的任何方法来查询时间对象并执行添加。返回的对象必须与输入对象具有相同的可观察类型。
     * <p>
     * 输入对象不得被修改。相反，必须返回原始对象的调整副本。这为不可变和可变时间对象提供了等效且安全的行为。
     * <p>
     * 输入时间对象可能属于非 ISO 日历系统。实现可以选择记录与其他日历系统的兼容性，或通过 {@link TemporalQueries#chronology() 查询日历系统} 拒绝非 ISO 时间对象。
     * <p>
     * 此方法可能被多个线程并行调用。调用时必须是线程安全的。
     *
     * @param temporal 要添加时间量的时间对象，不为空
     * @return 具有添加结果的相同可观察类型对象，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    Temporal addTo(Temporal temporal);

    /**
     * 从指定的时间对象中减去此时间量。
     * <p>
     * 使用实现类封装的逻辑从指定的时间对象中减去时间量。
     * <p>
     * 使用此方法有两种等效的方式。第一种是直接调用此方法。第二种是使用 {@link Temporal#minus(TemporalAmount)}：
     * <pre>
     *   // 这两行是等效的，但推荐使用第二种方法
     *   dateTime = amount.subtractFrom(dateTime);
     *   dateTime = dateTime.minus(amount);
     * </pre>
     * 推荐使用第二种方法，{@code minus(TemporalAmount)}，因为它在代码中更清晰易读。
     *
     * @implSpec
     * 实现必须将输入对象进行减法。实现定义减法逻辑，并负责记录该逻辑。它可以使用 {@code Temporal} 上的任何方法来查询时间对象并执行减法。返回的对象必须与输入对象具有相同的可观察类型。
     * <p>
     * 输入对象不得被修改。相反，必须返回原始对象的调整副本。这为不可变和可变时间对象提供了等效且安全的行为。
     * <p>
     * 输入时间对象可能属于非 ISO 日历系统。实现可以选择记录与其他日历系统的兼容性，或通过 {@link TemporalQueries#chronology() 查询日历系统} 拒绝非 ISO 时间对象。
     * <p>
     * 此方法可能被多个线程并行调用。调用时必须是线程安全的。
     *
     * @param temporal 要减去时间量的时间对象，不为空
     * @return 具有减法结果的相同可观察类型对象，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    Temporal subtractFrom(Temporal temporal);
}
