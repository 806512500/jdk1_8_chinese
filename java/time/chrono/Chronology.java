
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

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 一个日历系统，用于组织和识别日期。
 * <p>
 * 主要的日期和时间 API 基于 ISO 日历系统。
 * 日历系统在幕后操作，表示日历系统的通用概念。
 * 例如，日本、民国、泰历等。
 * <p>
 * 大多数其他日历系统也基于年、月、日的共享概念，
 * 这些概念与地球绕太阳和月球绕地球的周期有关。
 * 这些共享概念由 {@link ChronoField} 定义，并可供任何 {@code Chronology} 实现使用：
 * <pre>
 *   LocalDate isoDate = ...
 *   ThaiBuddhistDate thaiDate = ...
 *   int isoYear = isoDate.get(ChronoField.YEAR);
 *   int thaiYear = thaiDate.get(ChronoField.YEAR);
 * </pre>
 * 如上所示，尽管日期对象属于不同的日历系统，由不同的
 * {@code Chronology} 实例表示，但都可以使用 {@code ChronoField} 上的相同常量进行查询。
 * 有关此影响的详细讨论，请参见 {@link ChronoLocalDate}。
 * 一般来说，建议使用已知的基于 ISO 的 {@code LocalDate}，而不是
 * {@code ChronoLocalDate}。
 * <p>
 * 虽然 {@code Chronology} 对象通常使用 {@code ChronoField} 并基于
 * 时代、纪年、月份和日期的模型，但这不是必需的。
 * {@code Chronology} 实例可以表示完全不同的日历系统，例如玛雅日历。
 * <p>
 * 从实际角度来看，{@code Chronology} 实例还充当工厂。
 * {@link #of(String)} 方法允许通过标识符查找实例，
 * 而 {@link #ofLocale(Locale)} 方法允许通过区域设置查找。
 * <p>
 * {@code Chronology} 实例提供了一组方法来创建 {@code ChronoLocalDate} 实例。
 * 日期类用于操作特定日期。
 * <ul>
 * <li> {@link #dateNow() dateNow()}
 * <li> {@link #dateNow(Clock) dateNow(clock)}
 * <li> {@link #dateNow(ZoneId) dateNow(zone)}
 * <li> {@link #date(int, int, int) date(yearProleptic, month, day)}
 * <li> {@link #date(Era, int, int, int) date(era, yearOfEra, month, day)}
 * <li> {@link #dateYearDay(int, int) dateYearDay(yearProleptic, dayOfYear)}
 * <li> {@link #dateYearDay(Era, int, int) dateYearDay(era, yearOfEra, dayOfYear)}
 * <li> {@link #date(TemporalAccessor) date(TemporalAccessor)}
 * </ul>
 *
 * <h3 id="addcalendars">添加新的日历</h3>
 * 可以通过应用程序扩展可用的日历系统。
 * 添加新的日历系统需要编写 {@code Chronology}、{@code ChronoLocalDate} 和 {@code Era} 的实现。
 * 大多数特定于日历系统的逻辑将位于
 * {@code ChronoLocalDate} 实现中。
 * {@code Chronology} 实现充当工厂。
 * <p>
 * 为了允许发现额外的日历系统，使用了 {@link java.util.ServiceLoader ServiceLoader}。
 * 必须在 {@code META-INF/services} 目录中添加一个文件，文件名为
 * 'java.time.chrono.Chronology'，列出实现类。
 * 有关服务加载的更多详细信息，请参见 ServiceLoader。
 * 对于通过 id 或 calendarType 查找，系统提供的日历系统优先于应用程序提供的日历系统。
 * <p>
 * 每个日历系统必须定义一个在系统内唯一的日历系统 ID。
 * 如果日历系统由 CLDR 规范定义，则日历类型是 CLDR 类型和（如果适用）CLDR 变体的组合，
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确操作。
 * 所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @since 1.8
 */
public interface Chronology extends Comparable<Chronology> {

    /**
     * 从时间对象获取 {@code Chronology} 实例。
     * <p>
     * 此方法根据指定的时间对象获取日历系统。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code Chronology} 实例。
     * <p>
     * 转换将使用 {@link TemporalQueries#chronology()} 获取日历系统。
     * 如果指定的时间对象没有日历系统，则返回 {@link IsoChronology}。
     * <p>
     * 此方法的签名与函数接口 {@link TemporalQuery} 匹配，
     * 允许通过方法引用使用它，例如 {@code Chronology::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 日历系统，不为空
     * @throws DateTimeException 如果无法转换为 {@code Chronology}
     */
    static Chronology from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        Chronology obj = temporal.query(TemporalQueries.chronology());
        return (obj != null ? obj : IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 从区域设置获取 {@code Chronology} 实例。
     * <p>
     * 此方法根据指定的区域设置返回一个 {@code Chronology}，
     * 通常返回 {@code IsoChronology}。其他日历系统
     * 仅在区域设置中显式选择时返回。
     * <p>
     * {@link Locale} 类提供访问一系列有助于本地化应用程序的信息，
     * 包括语言和区域，例如 "en-GB" 表示在英国使用的英语。
     * <p>
     * {@code Locale} 类还支持一种扩展机制，可以用于识别日历系统。
     * 该机制是一种键值对形式，其中日历系统的键为 "ca"。
     * 例如，区域设置 "en-JP-u-ca-japanese" 表示在日本使用的英语和日本日历系统。
     * <p>
     * 此方法通过与传递 "ca" 到 {@link Locale#getUnicodeLocaleType(String)} 相当的方式
     * 查找所需的日历系统。
     * 如果 "ca" 键不存在，则返回 {@code IsoChronology}。
     * <p>
     * 请注意，此方法的行为与较旧的
     * {@link java.util.Calendar#getInstance(Locale)} 方法不同。
     * 如果该方法接收到 "th_TH" 区域设置，它将返回 {@code BuddhistCalendar}。
     * 相比之下，此方法将返回 {@code IsoChronology}。
     * 将区域设置 "th-TH-u-ca-buddhist" 传递给任一方法将
     * 导致泰国佛教日历系统，因此是泰国日历系统本地化的推荐方法。
     * <p>
     * 对于日本日历系统，情况类似但更简单。
     * 区域设置 "jp_JP_JP" 以前用于访问日历系统。
     * 然而，与泰国区域设置不同，"ja_JP_JP" 由
     * {@code Locale} 自动转换为现代和推荐的形式 "ja-JP-u-ca-japanese"。
     * 因此，此方法与
     * {@code Calendar#getInstance(Locale)} 之间没有行为差异。
     *
     * @param locale  用于获取日历系统的区域设置，不为空
     * @return 与区域设置关联的日历系统，不为空
     * @throws DateTimeException 如果区域设置指定的日历系统找不到
     */
    static Chronology ofLocale(Locale locale) {
        return AbstractChronology.ofLocale(locale);
    }

    //-----------------------------------------------------------------------
    /**
     * 从日历系统 ID 或日历系统类型获取 {@code Chronology} 实例。
     * <p>
     * 此方法根据 ID 或类型返回一个日历系统。
     * {@link #getId() 日历系统 ID} 唯一标识日历系统。
     * {@link #getCalendarType() 日历系统类型} 由 CLDR 规范定义。
     * <p>
     * 日历系统可以是系统日历系统或通过 ServiceLoader 配置提供的应用程序日历系统。
     * <p>
     * 由于某些日历可以自定义，因此 ID 或类型通常指默认自定义。
     * 例如，公历可以有多个从儒略历转换的日期，但查找仅提供默认转换日期。
     *
     * @param id  日历系统 ID 或日历系统类型，不为空
     * @return 请求标识符的日历系统，不为空
     * @throws DateTimeException 如果找不到日历系统
     */
    static Chronology of(String id) {
        return AbstractChronology.of(id);
    }

    /**
     * 返回可用的日历系统。
     * <p>
     * 每个返回的 {@code Chronology} 都可在系统中使用。
     * 日历系统集包括系统日历系统和
     * 通过 ServiceLoader 配置提供的任何应用程序日历系统。
     *
     * @return 可用日历系统 ID 的独立、可修改集，不为空
     */
    static Set<Chronology> getAvailableChronologies() {
        return AbstractChronology.getAvailableChronologies();
    }

    //-----------------------------------------------------------------------
    /**
     * 获取日历系统的 ID。
     * <p>
     * ID 唯一标识 {@code Chronology}。
     * 可以使用 {@link #of(String)} 通过 ID 查找 {@code Chronology}。
     *
     * @return 日历系统 ID，不为空
     * @see #getCalendarType()
     */
    String getId();

    /**
     * 获取日历系统的类型。
     * <p>
     * 日历类型是由 CLDR 和
     * <em>Unicode Locale Data Markup Language (LDML)</em> 规范定义的标识符，
     * 用于唯一标识日历系统。
     * {@code getCalendarType} 是 CLDR 日历类型和变体（如果适用）的组合，
     * 以 "-" 分隔。
     * 可以使用 {@link #of(String)} 通过日历类型查找 {@code Chronology}。
     *
     * @return 日历系统类型，如果日历未由 CLDR/LDML 定义，则为 null
     * @see #getId()
     */
    String getCalendarType();

    //-----------------------------------------------------------------------
    /**
     * 从纪年、纪年年份、月份和日期字段获取此日历系统中的本地日期。
     *
     * @implSpec
     * 默认实现将纪年和纪年年份组合成一个历元年，然后调用 {@link #date(int, int, int)}。
     *
     * @param era  适用于日历系统的纪年，不为空
     * @param yearOfEra  日历系统的纪年年份
     * @param month  日历系统的月份
     * @param dayOfMonth  日历系统的日期
     * @return 此日历系统中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是适用于日历系统的正确类型
     */
    default ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * 从历元年、月份和日期字段获取此日历系统中的本地日期。
     *
     * @param prolepticYear  日历系统的历元年
     * @param month  日历系统的月份
     * @param dayOfMonth  日历系统的日期
     * @return 此日历系统中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    ChronoLocalDate date(int prolepticYear, int month, int dayOfMonth);


                /**
     * 在此历法中从纪元、纪元年和年中的天数字段获取本地日期。
     *
     * @implSpec
     * 默认实现将纪元和纪元年组合成一个延续年，然后调用 {@link #dateYearDay(int, int)}。
     *
     * @param era  适用于该历法的纪元，不为空
     * @param yearOfEra  该历法的纪元年
     * @param dayOfYear  该历法的年中的天数
     * @return 该历法中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是适用于该历法的正确类型
     */
    default ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * 在此历法中从延续年和年中的天数字段获取本地日期。
     *
     * @param prolepticYear  该历法的延续年
     * @param dayOfYear  该历法的年中的天数
     * @return 该历法中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    ChronoLocalDate dateYearDay(int prolepticYear, int dayOfYear);

    /**
     * 从纪元日获取此历法中的本地日期。
     * <p>
     * {@link ChronoField#EPOCH_DAY EPOCH_DAY} 的定义对所有历法系统都是相同的，因此可以用于转换。
     *
     * @param epochDay  纪元日
     * @return 该历法中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    ChronoLocalDate dateEpochDay(long epochDay);

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取此历法中的当前本地日期。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前日期。
     * <p>
     * 使用此方法将阻止在测试中使用备用时钟，因为时钟是硬编码的。
     *
     * @implSpec
     * 默认实现调用 {@link #dateNow(Clock)}。
     *
     * @return 使用系统时钟和默认时区的当前本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    default ChronoLocalDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取此历法中的当前本地日期。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前日期。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将阻止在测试中使用备用时钟，因为时钟是硬编码的。
     *
     * @implSpec
     * 默认实现调用 {@link #dateNow(Clock)}。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    default ChronoLocalDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    /**
     * 从指定时钟获取此历法中的当前本地日期。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法允许在测试中使用备用时钟。
     * 可以通过 {@link Clock 依赖注入} 引入备用时钟。
     *
     * @implSpec
     * 默认实现调用 {@link #date(TemporalAccessor)}。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    default ChronoLocalDate dateNow(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return date(LocalDate.now(clock));
    }

    //-----------------------------------------------------------------------
    /**
     * 从另一个时间对象获取此历法中的本地日期。
     * <p>
     * 这是基于指定的时间对象获取此历法中的日期。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ChronoLocalDate} 的实例。
     * <p>
     * 转换通常使用 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 字段，该字段在历法系统中是标准化的。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许其通过方法引用作为查询使用，例如 {@code aChronology::date}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 该历法中的本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @see ChronoLocalDate#from(TemporalAccessor)
     */
    ChronoLocalDate date(TemporalAccessor temporal);

    /**
     * 从另一个时间对象获取此历法中的本地日期时间。
     * <p>
     * 这是基于指定的时间对象获取此历法中的日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ChronoLocalDateTime} 的实例。
     * <p>
     * 转换提取并组合时间对象中的 {@code ChronoLocalDate} 和 {@code LocalTime}。
     * 实现允许执行优化，例如访问与相关对象等效的字段。
     * 结果使用此历法。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许其通过方法引用作为查询使用，例如 {@code aChronology::localDateTime}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 该历法中的本地日期时间，不为空
     * @throws DateTimeException 如果无法创建日期时间
     * @see ChronoLocalDateTime#from(TemporalAccessor)
     */
    default ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal) {
        try {
            return date(temporal).atTime(LocalTime.from(temporal));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 ChronoLocalDateTime: " + temporal.getClass(), ex);
        }
    }

    /**
     * 从另一个时间对象获取此历法中的 {@code ChronoZonedDateTime}。
     * <p>
     * 这是基于指定的时间对象获取此历法中的带时区的日期时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code ChronoZonedDateTime} 的实例。
     * <p>
     * 转换将首先从时间对象中获取 {@code ZoneId}，必要时回退到 {@code ZoneOffset}。然后尝试获取 {@code Instant}，必要时回退到 {@code ChronoLocalDateTime}。
     * 结果将是 {@code ZoneId} 或 {@code ZoneOffset} 与 {@code Instant} 或 {@code ChronoLocalDateTime} 的组合。
     * 实现允许执行优化，例如访问与相关对象等效的字段。
     * 结果使用此历法。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许其通过方法引用作为查询使用，例如 {@code aChronology::zonedDateTime}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 该历法中的带时区的日期时间，不为空
     * @throws DateTimeException 如果无法创建日期时间
     * @see ChronoZonedDateTime#from(TemporalAccessor)
     */
    default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal) {
        try {
            ZoneId zone = ZoneId.from(temporal);
            try {
                Instant instant = Instant.from(temporal);
                return zonedDateTime(instant, zone);

            } catch (DateTimeException ex1) {
                ChronoLocalDateTimeImpl<?> cldt = ChronoLocalDateTimeImpl.ensureValid(this, localDateTime(temporal));
                return ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 ChronoZonedDateTime: " + temporal.getClass(), ex);
        }
    }

    /**
     * 从 {@code Instant} 获取此历法中的 {@code ChronoZonedDateTime}。
     * <p>
     * 这是获取与指定的即时相同的带时区的日期时间。
     *
     * @param instant  要创建日期时间的即时，不为空
     * @param zone  时区，不为空
     * @return 带时区的日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    default ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的年份是否为闰年。
     * <p>
     * 闰年是比正常年份更长的年份。
     * 确切的含义由历法根据以下约束条件确定。
     * <ul>
     * <li>闰年必须意味着年长于非闰年。
     * <li>不支持年份概念的历法必须返回 false。
     * </ul>
     *
     * @param prolepticYear  要检查的延续年，未验证范围
     * @return 如果年份是闰年则为 true
     */
    boolean isLeapYear(long prolepticYear);

    /**
     * 计算给定纪元和纪元年的延续年。
     * <p>
     * 这是将纪元和纪元年组合成单个延续年字段。
     * <p>
     * 如果历法积极使用纪元，例如 {@code JapaneseChronology}，则纪元年将针对纪元进行验证。
     * 对于其他历法，验证是可选的。
     *
     * @param era  适用于该历法的纪元，不为空
     * @param yearOfEra  该历法的纪元年
     * @return 延续年
     * @throws DateTimeException 如果无法转换为延续年，例如纪元年对纪元无效
     * @throws ClassCastException 如果 {@code era} 不是适用于该历法的正确类型
     */
    int prolepticYear(Era era, int yearOfEra);

    /**
     * 从数值创建历法纪元对象。
     * <p>
     * 纪元是时间线上最大的划分。
     * 大多数历法系统有一个纪元，将时间线分为两个纪元。
     * 然而，有些历法系统有多个纪元，例如每个领导者的统治时期。
     * 确切的含义由历法根据以下约束条件确定。
     * <p>
     * 1970-01-01 使用的纪元必须具有值 1。
     * 后来的纪元必须具有顺序更高的值。
     * 更早的纪元必须具有顺序更低的值。
     * 每个历法必须引用一个枚举或类似的单例以提供纪元值。
     * <p>
     * 此方法返回适用于指定纪元值的单例纪元。
     *
     * @param eraValue  纪元值
     * @return 历法系统纪元，不为空
     * @throws DateTimeException 如果无法创建纪元
     */
    Era eraOf(int eraValue);

    /**
     * 获取此历法的纪元列表。
     * <p>
     * 大多数历法系统有一个纪元，年份在此纪元内有意义。
     * 如果历法系统不支持纪元概念，则必须返回一个空列表。
     *
     * @return 该历法的纪元列表，可以是不可变的，不为空
     */
    List<Era> eras();

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 所有字段都可以表示为 {@code long} 整数。
     * 此方法返回描述该值有效范围的对象。
     * <p>
     * 请注意，结果仅描述最小和最大有效值，不要过度解读它们。例如，范围内的某些值可能对字段无效。
     * <p>
     * 无论历法是否支持该字段，此方法都会返回结果。
     *
     * @param field  要获取范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     */
    ValueRange range(ChronoField field);

    //-----------------------------------------------------------------------
    /**
     * 获取此历法的文本表示。
     * <p>
     * 这返回用于标识历法的文本名称，适合呈现给用户。
     * 参数控制返回文本的样式和语言环境。
     *
     * @implSpec
     * 默认实现的行为类似于使用格式化器格式化历法文本名称。
     *
     * @param style  所需的文本样式，不为空
     * @param locale  要使用的语言环境，不为空
     * @return 历法的文本值，不为空
     */
    default String getDisplayName(TextStyle style, Locale locale) {
        TemporalAccessor temporal = new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }
            @Override
            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return (R) Chronology.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
        return new DateTimeFormatterBuilder().appendChronologyText(style).toFormatter(locale).format(temporal);
    }

    //-----------------------------------------------------------------------
    /**
     * 在解析期间将解析的 {@code ChronoField} 值解析为日期。
     * <p>
     * 大多数 {@code TemporalField} 实现使用字段上的解析方法进行解析。相比之下，{@code ChronoField} 类定义的字段仅在相对于历法时才有意义。
     * 因此，{@code ChronoField} 日期字段在此特定历法的上下文中解析。
     * <p>
     * 默认实现，解释典型的解析行为，提供在 {@link AbstractChronology} 中。
     *
     * @param fieldValues  字段到值的映射，可以更新，不为空
     * @param resolverStyle  请求的解析类型，不为空
     * @return 解析的日期，如果信息不足无法创建日期则为 null
     * @throws DateTimeException 如果日期无法解析，通常是因为输入数据有冲突
     */
    ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);


                //-----------------------------------------------------------------------
    /**
     * 根据年、月和日获取此纪元的周期。
     * <p>
     * 这将返回一个与该纪元绑定的周期，使用指定的年、月和日。所有提供的纪元都使用基于年、月和日的周期，
     * 但是 {@code ChronoPeriod} API 允许使用其他单位表示周期。
     *
     * @implSpec
     * 默认实现返回一个适用于大多数日历系统的实现类。它仅基于这三个单位。规范化、加法和减法
     * 从 {@link #range(ChronoField)} 导出一年中的月份数。如果一年中的月份数是固定的，
     * 那么加法、减法和规范化计算方法略有不同。
     * <p>
     * 如果实现一个非常规的日历系统，该系统不是基于年、月和日，或者希望直接控制，
     * 则必须直接实现 {@code ChronoPeriod} 接口。
     * <p>
     * 返回的周期是不可变且线程安全的。
     *
     * @param years  年数，可以为负数
     * @param months  月数，可以为负数
     * @param days  天数，可以为负数
     * @return 以该纪元表示的周期，不为 null
     */
    default ChronoPeriod period(int years, int months, int days) {
        return new ChronoPeriodImpl(this, years, months, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此纪元与另一个纪元进行比较。
     * <p>
     * 比较顺序首先按纪元 ID 字符串，然后按子类特定的任何其他信息。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义的。
     *
     * @param other  要比较的其他纪元，不为 null
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     */
    @Override
    int compareTo(Chronology other);

    /**
     * 检查此纪元是否等于另一个纪元。
     * <p>
     * 比较基于对象的整个状态。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此纪元等于其他纪元，则返回 true
     */
    @Override
    boolean equals(Object obj);

    /**
     * 此纪元的哈希码。
     * <p>
     * 哈希码应基于对象的整个状态。
     *
     * @return 一个合适的哈希码
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * 将此纪元输出为 {@code String}。
     * <p>
     * 格式应包括对象的整个状态。
     *
     * @return 此纪元的字符串表示，不为 null
     */
    @Override
    String toString();

}
