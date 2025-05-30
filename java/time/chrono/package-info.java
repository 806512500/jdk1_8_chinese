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
 * 通用 API，用于除默认 ISO 之外的日历系统。
 * </p>
 * <p>
 * 主要 API 是基于 ISO-8601 中定义的日历系统。
 * 但是，还有其他日历系统，此包提供了对它们的基本支持。
 * 替代日历在 {@link java.time.chrono} 包中提供。
 * </p>
 * <p>
 * 日历系统由 {@link java.time.chrono.Chronology} 接口定义，
 * 而日历系统中的日期由 {@link java.time.chrono.ChronoLocalDate} 接口定义。
 * </p>
 * <p>
 * 建议应用程序尽可能使用主要 API，包括从持久数据存储（如数据库）读取和写入的代码，
 * 以及通过网络发送日期和时间的代码。
 * “chrono”类则用于用户界面级别处理本地化的输入/输出。
 * 有关详细讨论，请参阅 {@link java.time.chrono.ChronoLocalDate ChronoLocalDate}。
 * </p>
 * <p>
 * 在应用程序中使用非 ISO 日历系统会引入显著的额外复杂性。
 * 在使用“chrono”接口之前，请确保已阅读 {@code ChronoLocalDate} 中的警告和建议。
 * </p>
 * <p>
 * 支持的日历系统包括：
 * </p>
 * <ul>
 * <li>{@link java.time.chrono.HijrahChronology 伊斯兰历}</li>
 * <li>{@link java.time.chrono.JapaneseChronology 日本历}</li>
 * <li>{@link java.time.chrono.MinguoChronology 民国历}</li>
 * <li>{@link java.time.chrono.ThaiBuddhistChronology 泰国佛教历}</li>
 * </ul>
 *
 * <h3>示例</h3>
 * <p>
 * 此示例列出所有可用日历的今天日期。
 * </p>
 * <pre>
 *   // 枚举可用日历列表并打印每个日历的今天日期。
 *       Set&lt;Chronology&gt; chronos = Chronology.getAvailableChronologies();
 *       for (Chronology chrono : chronos) {
 *           ChronoLocalDate date = chrono.dateNow();
 *           System.out.printf("   %20s: %s%n", chrono.getId(), date.toString());
 *       }
 * </pre>
 *
 * <p>
 * 此示例在命名的非 ISO 日历系统中创建并使用日期。
 * </p>
 * <pre>
 *   // 打印泰国佛教日期
 *       ChronoLocalDate now1 = Chronology.of("ThaiBuddhist").dateNow();
 *       int day = now1.get(ChronoField.DAY_OF_MONTH);
 *       int dow = now1.get(ChronoField.DAY_OF_WEEK);
 *       int month = now1.get(ChronoField.MONTH_OF_YEAR);
 *       int year = now1.get(ChronoField.YEAR);
 *       System.out.printf("  Today is %s %s %d-%s-%d%n", now1.getChronology().getId(),
 *                 dow, day, month, year);
 *   // 打印泰国佛教历的今天日期和一年的最后一天。
 *       ChronoLocalDate first = now1
 *                 .with(ChronoField.DAY_OF_MONTH, 1)
 *                 .with(ChronoField.MONTH_OF_YEAR, 1);
 *       ChronoLocalDate last = first
 *                 .plus(1, ChronoUnit.YEARS)
 *                 .minus(1, ChronoUnit.DAYS);
 *       System.out.printf("  %s: 1st of year: %s; end of year: %s%n", last.getChronology().getId(),
 *                 first, last);
 *  </pre>
 *
 * <p>
 * 此示例在特定的泰国佛教日历系统中创建并使用日期。
 * </p>
 * <pre>
 *   // 打印泰国佛教日期
 *       ThaiBuddhistDate now1 = ThaiBuddhistDate.now();
 *       int day = now1.get(ChronoField.DAY_OF_MONTH);
 *       int dow = now1.get(ChronoField.DAY_OF_WEEK);
 *       int month = now1.get(ChronoField.MONTH_OF_YEAR);
 *       int year = now1.get(ChronoField.YEAR);
 *       System.out.printf("  Today is %s %s %d-%s-%d%n", now1.getChronology().getId(),
 *                 dow, day, month, year);
 *
 *   // 打印泰国佛教历的今天日期和一年的最后一天。
 *       ThaiBuddhistDate first = now1
 *                 .with(ChronoField.DAY_OF_MONTH, 1)
 *                 .with(ChronoField.MONTH_OF_YEAR, 1);
 *       ThaiBuddhistDate last = first
 *                 .plus(1, ChronoUnit.YEARS)
 *                 .minus(1, ChronoUnit.DAYS);
 *       System.out.printf("  %s: 1st of year: %s; end of year: %s%n", last.getChronology().getId(),
 *                 first, last);
 *  </pre>
 *
 * <h3>包规范</h3>
 * <p>
 * 除非另有说明，否则将 null 参数传递给此包中任何类或接口的构造函数或方法将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
 * Javadoc "@param" 定义用于总结 null 行为。
 * 每个方法中未显式记录 "@throws {@link java.lang.NullPointerException}"。
 * </p>
 * <p>
 * 所有计算都应检查数值溢出并抛出 {@link java.lang.ArithmeticException} 或 {@link java.time.DateTimeException}。
 * </p>
 * @since JDK1.8
 */
package java.time.chrono;
