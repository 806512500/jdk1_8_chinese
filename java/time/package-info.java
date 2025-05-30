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

/**
 * <p>
 * 日期、时间、瞬间和持续时间的主要 API。
 * </p>
 * <p>
 * 本包中定义的类表示主要的日期时间概念，包括瞬间、持续时间、日期、时间、时区和周期。
 * 它们基于 ISO 日历系统，这是按照公历规则的 <i>事实上的</i> 世界日历。
 * 所有类都是不可变的且线程安全的。
 * </p>
 * <p>
 * 每个日期时间实例由方便地通过 API 提供的字段组成。对于字段的低级访问，请参阅 {@code java.time.temporal} 包。
 * 每个类都支持打印和解析各种日期和时间。有关自定义选项，请参阅 {@code java.time.format} 包。
 * </p>
 * <p>
 * {@code java.time.chrono} 包包含日历中立的 API
 * {@link java.time.chrono.ChronoLocalDate ChronoLocalDate}，
 * {@link java.time.chrono.ChronoLocalDateTime ChronoLocalDateTime}，
 * {@link java.time.chrono.ChronoZonedDateTime ChronoZonedDateTime} 和
 * {@link java.time.chrono.Era Era}。
 * 这些 API 旨在供需要使用本地化日历的应用程序使用。
 * 建议应用程序在系统边界（如数据库或网络）之间使用本包中的 ISO-8601 日期和时间类。
 * 日历中立的 API 应保留用于与用户的交互。
 * </p>
 *
 * <h3>日期和时间</h3>
 * <p>
 * {@link java.time.Instant} 实质上是一个数字时间戳。
 * 可以从 {@link java.time.Clock} 获取当前的 Instant。
 * 这对于记录和持久化时间点非常有用，过去通常与存储 {@link java.lang.System#currentTimeMillis()} 的结果相关联。
 * </p>
 * <p>
 * {@link java.time.LocalDate} 存储没有时间的日期。
 * 这存储一个日期，如 '2010-12-03'，可以用于存储生日。
 * </p>
 * <p>
 * {@link java.time.LocalTime} 存储没有日期的时间。
 * 这存储一个时间，如 '11:30'，可以用于存储开门或关门时间。
 * </p>
 * <p>
 * {@link java.time.LocalDateTime} 存储日期和时间。
 * 这存储一个日期时间，如 '2010-12-03T11:30'。
 * </p>
 * <p>
 * {@link java.time.ZonedDateTime} 存储带有时区的日期和时间。
 * 如果您希望在计算日期和时间时考虑 {@link java.time.ZoneId}，如 'Europe/Paris'，这将非常有用。
 * 在可能的情况下，建议使用没有时区的更简单的类。
 * 广泛使用时区往往会增加应用程序的复杂性。
 * </p>
 *
 * <h3>持续时间和周期</h3>
 * <p>
 * 除了日期和时间，API 还允许存储时间和周期。
 * {@link java.time.Duration} 是时间线上以纳秒为单位的简单时间度量。
 * {@link java.time.Period} 以人类有意义的单位（如年或天）表示时间量。
 * </p>
 *
 * <h3>其他值类型</h3>
 * <p>
 * {@link java.time.Month} 存储单独的月份。
 * 这存储一个单独的月份，如 'DECEMBER'。
 * </p>
 * <p>
 * {@link java.time.DayOfWeek} 存储单独的星期几。
 * 这存储一个单独的星期几，如 'TUESDAY'。
 * </p>
 * <p>
 * {@link java.time.Year} 存储单独的年份。
 * 这存储一个单独的年份，如 '2010'。
 * </p>
 * <p>
 * {@link java.time.YearMonth} 存储没有日或时间的年和月。
 * 这存储一个年和月，如 '2010-12'，可以用于存储信用卡过期日期。
 * </p>
 * <p>
 * {@link java.time.MonthDay} 存储没有年或时间的月和日。
 * 这存储一个月和日，如 '--12-03'，可以用于存储不存储年份的年度事件，如生日。
 * </p>
 * <p>
 * {@link java.time.OffsetTime} 存储没有日期的 UTC 偏移时间。
 * 这存储一个时间，如 '11:30+01:00'。{@link java.time.ZoneOffset ZoneOffset} 的形式为 '+01:00'。
 * </p>
 * <p>
 * {@link java.time.OffsetDateTime} 存储带有 UTC 偏移的日期和时间。
 * 这存储一个日期时间，如 '2010-12-03T11:30+01:00'。这有时出现在 XML 消息和其他形式的持久化中，但包含的信息比完整的时区少。
 * </p>
 *
 * <h3>包规范</h3>
 * <p>
 * 除非另有说明，否则将 null 传递给本包中任何类或接口的构造函数或方法将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
 * Javadoc 中的 "@param" 定义用于总结 null 行为。"@throws {@link java.lang.NullPointerException}" 不在每个方法中显式记录。
 * </p>
 * <p>
 * 所有计算都应检查数值溢出并抛出 {@link java.lang.ArithmeticException} 或 {@link java.time.DateTimeException}。
 * </p>
 *
 * <h3>设计说明（非规范性）</h3>
 * <p>
 * API 设计为尽早拒绝 null 并明确这种行为。
 * 一个关键的例外是任何接受对象并返回布尔值的方法，用于检查或验证，通常对 null 返回 false。
 * </p>
 * <p>
 * API 设计为在主高级 API 中尽可能类型安全。
 * 因此，对于日期、时间、日期时间以及带有偏移和时区的变体，都有单独的类。
 * 这看起来像是很多类，但大多数应用程序可以只使用五种日期/时间类型。
 * <ul>
 * <li>{@link java.time.Instant} - 时间戳</li>
 * <li>{@link java.time.LocalDate} - 没有时间或任何偏移或时区引用的日期</li>
 * <li>{@link java.time.LocalTime} - 没有日期或任何偏移或时区引用的时间</li>
 * <li>{@link java.time.LocalDateTime} - 结合日期和时间，但仍然没有偏移或时区</li>
 * <li>{@link java.time.ZonedDateTime} - 带有时区和从 UTC/Greenwich 解析的偏移的“完整”日期时间</li>
 * </ul>
 * <p>
 * {@code Instant} 是与 {@code java.util.Date} 最接近的等效类。
 * {@code ZonedDateTime} 是与 {@code java.util.GregorianCalendar} 最接近的等效类。
 * </p>
 * <p>
 * 在可能的情况下，应用程序应使用 {@code LocalDate}、{@code LocalTime} 和 {@code LocalDateTime} 更好地建模领域。
 * 例如，生日应存储在 {@code LocalDate} 中。
 * 请记住，任何使用 {@linkplain java.time.ZoneId 时区}（如 'Europe/Paris'）都会增加计算的复杂性。
 * 许多应用程序可以仅使用 {@code LocalDate}、{@code LocalTime} 和 {@code Instant}，在用户界面（UI）层添加时区。
 * </p>
 * <p>
 * 偏移日期时间类型 {@code OffsetTime} 和 {@code OffsetDateTime} 主要用于网络协议和数据库访问。
 * 例如，大多数数据库不能自动存储像 'Europe/Paris' 这样的时区，但可以存储像 '+02:00' 这样的偏移。
 * </p>
 * <p>
 * 还提供了表示日期最重要子部分的类，包括 {@code Month}、{@code DayOfWeek}、{@code Year}、{@code YearMonth} 和 {@code MonthDay}。
 * 这些类可以用于建模更复杂的日期时间概念。
 * 例如，{@code YearMonth} 适用于表示信用卡过期日期。
 * </p>
 * <p>
 * 虽然有许多类表示日期的不同方面，但处理时间不同方面的类相对较少。
 * 如果完全遵循类型安全，将会有小时-分钟、小时-分钟-秒和小时-分钟-秒-纳秒的类。
 * 虽然逻辑上纯粹，但这不是一个实际的选择，因为由于日期和时间的组合，类的数量几乎会增加三倍。
 * 因此，{@code LocalTime} 用于所有时间精度，使用零来表示较低的精度。
 * </p>
 * <p>
 * 完全遵循类型安全的最终结论可能还会为日期时间中的每个字段提供单独的类，例如 HourOfDay 和 DayOfMonth 的类。
 * 这种方法在 Java 语言中过于复杂，缺乏可用性。
 * 周期也存在类似的问题。
 * 有理由为每个周期单位提供单独的类，例如 Years 和 Minutes 的类型。
 * 然而，这会产生很多类和类型转换的问题。
 * 因此，提供的日期时间类型是纯度和实用性的折衷。
 * </p>
 * <p>
 * API 在方法数量方面具有相对较大的表面面积。
 * 通过使用一致的方法前缀来管理这一点。
 * <ul>
 * <li>{@code of} - 静态工厂方法</li>
 * <li>{@code parse} - 专注于解析的静态工厂方法</li>
 * <li>{@code get} - 获取某物的值</li>
 * <li>{@code is} - 检查某物是否为真</li>
 * <li>{@code with} - 不可变的 setter 等效方法</li>
 * <li>{@code plus} - 向对象添加数量</li>
 * <li>{@code minus} - 从对象中减去数量</li>
 * <li>{@code to} - 将此对象转换为另一种类型</li>
 * <li>{@code at} - 将此对象与另一个对象组合，例如 {@code date.atTime(time)}</li>
 * </ul>
 * <p>
 * 多个日历系统是设计挑战中的一个棘手问题。
 * 第一原则是大多数用户需要标准的 ISO 日历系统。
 * 因此，主要类仅使用 ISO。第二原则是大多数需要非 ISO 日历系统的用户需要它进行用户交互，因此这是一个 UI 本地化问题。
 * 因此，应在数据模型和持久存储中将日期和时间对象存储为 ISO 对象，仅在显示时转换为本地日历。
 * 日历系统应单独存储在用户偏好设置中。
 * </p>
 * <p>
 * 然而，有些用户认为他们需要在应用程序中存储和使用任意日历系统中的日期。
 * 这由 {@link java.time.chrono.ChronoLocalDate} 支持，但在使用它之前必须仔细阅读该接口的 Javadoc 中的所有相关警告。
 * 总的来说，需要在多个日历系统之间进行通用互操作的应用程序通常需要以与仅使用 ISO 日历的应用程序非常不同的方式编写，
 * 因此大多数应用程序应仅使用 ISO 并避免使用 {@code ChronoLocalDate}。
 * </p>
 * <p>
 * API 还设计为用户可扩展的，因为计算时间的方法有很多。
 * 通过 {@link java.time.temporal.TemporalAccessor TemporalAccessor} 和
 * {@link java.time.temporal.Temporal Temporal} 访问的 {@linkplain java.time.temporal.TemporalField 字段} 和
 * {@linkplain java.time.temporal.TemporalUnit 单位} API 为应用程序提供了相当大的灵活性。
 * 此外，{@link java.time.temporal.TemporalQuery TemporalQuery} 和
 * {@link java.time.temporal.TemporalAdjuster TemporalAdjuster} 接口提供了日常功能，使代码接近业务需求：
 * </p>
 * <pre>
 *   LocalDate customerBirthday = customer.loadBirthdayFromDatabase();
 *   LocalDate today = LocalDate.now();
 *   if (customerBirthday.equals(today)) {
 *     LocalDate specialOfferExpiryDate = today.plusWeeks(2).with(next(FRIDAY));
 *     customer.sendBirthdaySpecialOffer(specialOfferExpiryDate);
 *   }
 *
 * </pre>
 *
 * @since JDK1.8
 */
package java.time;
