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

/**
 * <p>
 * 使用字段和单位以及日期时间调整器访问日期和时间。
 * </p>
 * <p>
 * 该包扩展了基础包，以提供更强大用例的额外功能。支持包括：
 * </p>
 * <ul>
 * <li>日期时间单位，如年、月、日和小时</li>
 * <li>日期时间字段，如年份、月份、星期几或小时</li>
 * <li>日期时间调整函数</li>
 * <li>不同的周定义</li>
 * </ul>
 *
 * <h3>字段和单位</h3>
 * <p>
 * 日期和时间以字段和单位表示。
 * 单位用于测量时间量，如年、日或分钟。
 * 所有单位都实现了 {@link java.time.temporal.TemporalUnit}。
 * 常见的单位定义在 {@link java.time.temporal.ChronoUnit} 中，如 {@code DAYS}。
 * 单位接口设计允许应用程序定义的单位。
 * </p>
 * <p>
 * 字段用于表示较大日期时间的一部分，如年份、月份、星期几或分钟内的秒数。
 * 所有字段都实现了 {@link java.time.temporal.TemporalField}。
 * 常见的字段定义在 {@link java.time.temporal.ChronoField} 中，如 {@code HOUR_OF_DAY}。
 * 还有由 {@link java.time.temporal.JulianFields}、{@link java.time.temporal.WeekFields}
 * 和 {@link java.time.temporal.IsoFields} 定义的其他字段。
 * 字段接口设计允许应用程序定义的字段。
 * </p>
 * <p>
 * 该包提供了工具，允许以最适合框架的方式访问日期和时间的单位和字段。
 * {@link java.time.temporal.Temporal} 提供了支持字段的日期时间类型的抽象。
 * 其方法支持获取字段的值、创建具有修改字段值的新日期时间，以及查询其他信息，通常用于提取偏移量或时区。
 * </p>
 * <p>
 * 字段在应用程序代码中的一个用途是检索没有便捷方法的字段。
 * 例如，获取月份中的天数足够常见，以至于 {@code LocalDate} 上有一个名为 {@code getDayOfMonth()} 的方法。
 * 但对于更不常见的字段，必须使用字段。
 * 例如，{@code date.get(ChronoField.ALIGNED_WEEK_OF_MONTH)}。
 * 字段还提供了访问有效值范围的方法。
 * </p>
 *
 * <h3>调整和查询</h3>
 * <p>
 * 日期时间问题空间的一个关键部分是将日期调整为新的、相关值，
 * 如“月的最后一天”或“下一个星期三”。
 * 这些被建模为调整基础日期时间的函数。
 * 函数实现了 {@link java.time.temporal.TemporalAdjuster} 并操作 {@code Temporal}。
 * 常见的函数在 {@link java.time.temporal.TemporalAdjusters} 中提供。
 * 例如，要查找给定日期之后的星期几的第一个出现，使用
 * {@link java.time.temporal.TemporalAdjusters#next(DayOfWeek)}，如
 * {@code date.with(next(MONDAY))}。
 * 应用程序也可以通过实现 {@link java.time.temporal.TemporalAdjuster} 定义调整器。
 * </p>
 * <p>
 * {@link java.time.temporal.TemporalAmount} 接口建模相对时间量。
 * </p>
 * <p>
 * 除了调整日期时间，还提供了一个接口，通过
 * {@link java.time.temporal.TemporalQuery} 启用查询。
 * 最常见的查询接口实现是方法引用。
 * 主要类上的 {@code from(TemporalAccessor)} 方法都可以使用，如
 * {@code LocalDate::from} 或 {@code Month::from}。
 * 进一步的实现作为静态方法在 {@link java.time.temporal.TemporalQueries} 中提供。
 * 应用程序也可以通过实现 {@link java.time.temporal.TemporalQuery} 定义查询。
 * </p>
 *
 * <h3>周</h3>
 * <p>
 * 不同地区对周有不同的定义。
 * 例如，在欧洲，周通常从星期一开始，而在美国，周从星期日开始。
 * {@link java.time.temporal.WeekFields} 类建模了这种区别。
 * </p>
 * <p>
 * ISO 日历系统定义了基于周的年份划分。
 * 这定义了一个基于完整的周一到周一的周的年份。
 * 这在 {@link java.time.temporal.IsoFields} 中建模。
 * </p>
 *
 * <h3>包规范</h3>
 * <p>
 * 除非另有说明，否则将 null 参数传递给此包中任何类或接口的构造函数或方法
 * 将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
 * Javadoc "@param" 定义用于总结 null 行为。
 * "@throws {@link java.lang.NullPointerException}" 不在每个方法中显式记录。
 * </p>
 * <p>
 * 所有计算都应检查数值溢出并抛出 {@link java.lang.ArithmeticException}
 * 或 {@link java.time.DateTimeException}。
 * </p>
 * @since JDK1.8
 */
package java.time.temporal;
