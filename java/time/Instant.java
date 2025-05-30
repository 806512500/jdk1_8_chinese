
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
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time;

import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.LocalTime.SECONDS_PER_HOUR;
import static java.time.LocalTime.SECONDS_PER_MINUTE;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.NANOS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * 时间线上的一个瞬时点。
 * <p>
 * 该类建模了时间线上的单个瞬时点。
 * 这可能用于记录应用程序中的事件时间戳。
 * <p>
 * 瞬时点的范围需要存储一个大于 {@code long} 的数字。
 * 为了实现这一点，该类存储了一个表示纪元秒的 {@code long} 和一个表示纳秒的 {@code int}，
 * 纳秒的范围始终在 0 到 999,999,999 之间。
 * 纪元秒是从标准 Java 纪元 {@code 1970-01-01T00:00:00Z} 开始测量的，
 * 其中纪元后的瞬时点具有正值，纪元前的瞬时点具有负值。
 * 对于纪元秒和纳秒部分，较大的值在时间线上总是晚于较小的值。
 *
 * <h3>时间尺度</h3>
 * <p>
 * 太阳日的长度是人类测量时间的标准方式。
 * 传统上，它被细分为 24 小时，每小时 60 分钟，每分钟 60 秒，形成一个 86400 秒的日。
 * <p>
 * 现代时间测量基于原子钟，这些钟精确地定义了相对于铯原子跃迁的 SI 秒。
 * SI 秒的长度被定义为非常接近一天的 86400 分之一。
 * <p>
 * 不幸的是，地球自转的长度是变化的。
 * 此外，随着时间的推移，地球的平均自转周期变长。
 * 因此，2012 年的太阳日长度略长于 86400 个 SI 秒。
 * 任何给定日的实际长度和地球变慢的程度都是不可预测的，只能通过测量确定。
 * UT1 时间尺度捕捉到了准确的日长，但只能在日完成后的某个时间获得。
 * <p>
 * UTC 时间尺度是一种标准方法，用于将 UT1 中的所有额外秒分数打包成整秒，称为“闰秒”。
 * 闰秒的添加或移除取决于地球的自转变化。
 * 因此，UTC 允许在必要时将一天的长度设置为 86399 个 SI 秒或 86401 个 SI 秒，以保持与太阳的对齐。
 * <p>
 * 现代 UTC 时间尺度于 1972 年引入，引入了整闰秒的概念。
 * 1958 年至 1972 年间，UTC 的定义很复杂，包括次秒的微小跃迁和名义秒长度的改变。
 * 截至 2012 年，正在讨论改变 UTC 的定义，可能移除闰秒或引入其他改变。
 * <p>
 * 鉴于上述准确时间测量的复杂性，Java API 定义了其自己的时间尺度，即“Java 时间尺度”。
 * <p>
 * Java 时间尺度将每个日历日精确划分为 86400 个细分，称为秒。
 * 这些秒可能与 SI 秒不同。
 * 它与国际民用时间的实际定义非常接近，该定义会不时改变。
 * <p>
 * Java 时间尺度对时间线的不同部分有不同的定义，每个部分基于用作民用时间基础的国际共识时间尺度。
 * 每当国际共识时间尺度被修改或替换时，必须为 Java 时间尺度定义一个新部分。
 * 每个部分必须满足以下要求：
 * <ul>
 * <li>Java 时间尺度应与基础国际民用时间尺度非常接近；</li>
 * <li>Java 时间尺度应在每天中午与国际民用时间尺度完全匹配；</li>
 * <li>Java 时间尺度与国际民用时间尺度之间应有精确的定义关系。</li>
 * </ul>
 * 截至 2013 年，Java 时间尺度有两个部分。
 * <p>
 * 对于从 1972-11-03（确切边界详见下文）到进一步通知的这一部分，国际共识时间尺度是 UTC（带闰秒）。
 * 在这一部分中，Java 时间尺度与 <a href="http://www.cl.cam.ac.uk/~mgk25/time/utc-sls/">UTC-SLS</a> 相同。
 * 在没有闰秒的日子里，它与 UTC 完全相同。
 * 在有闰秒的日子里，闰秒在一天的最后 1000 秒内均匀分布，保持每天 86400 秒的外观。
 * <p>
 * 对于 1972-11-03 之前的部分，向后延伸任意远，国际共识时间尺度被定义为 UT1，应用于回溯性地，
 * 这相当于格林尼治的（平均）太阳时间。在这一部分中，Java 时间尺度与国际共识时间尺度相同。
 * 两部分之间的精确边界是 1972-11-03T00:00 和 1972-11-04T12:00 之间 UT1 = UTC 的瞬间。
 * <p>
 * 使用 JSR-310 API 实现 Java 时间尺度的实现不要求提供任何亚秒级准确的时钟，或单调或平滑地前进。
 * 因此，实现不要求实际执行 UTC-SLS 调整或以其他方式意识到闰秒。
 * 然而，JSR-310 要求实现必须记录定义表示当前瞬时的时钟时所使用的方法。
 * 有关可用时钟的详细信息，请参阅 {@link Clock}。
 * <p>
 * Java 时间尺度用于所有日期时间类。
 * 这包括 {@code Instant}、{@code LocalDate}、{@code LocalTime}、{@code OffsetDateTime}、
 * {@code ZonedDateTime} 和 {@code Duration}。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code Instant} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class Instant
        implements Temporal, TemporalAdjuster, Comparable<Instant>, Serializable {

    /**
     * 1970-01-01T00:00:00Z 纪元瞬时点的常量。
     */
    public static final Instant EPOCH = new Instant(0, 0);
    /**
     * 支持的最小纪元秒。
     */
    private static final long MIN_SECOND = -31557014167219200L;
    /**
     * 支持的最大纪元秒。
     */
    private static final long MAX_SECOND = 31556889864403199L;
    /**
     * 支持的最小 {@code Instant}，'-1000000000-01-01T00:00Z'。
     * 应用程序可以将其用作“远过去”的瞬时点。
     * <p>
     * 这比最小的 {@code LocalDateTime} 早一年。
     * 这提供了足够的值来处理影响瞬时点的 {@code ZoneOffset} 范围，以及本地日期时间。
     * 该值还被选择使得年份的值可以存储在 {@code int} 中。
     */
    public static final Instant MIN = Instant.ofEpochSecond(MIN_SECOND, 0);
    /**
     * 支持的最大 {@code Instant}，'1000000000-12-31T23:59:59.999999999Z'。
     * 应用程序可以将其用作“远未来”的瞬时点。
     * <p>
     * 这比最大的 {@code LocalDateTime} 晚一年。
     * 这提供了足够的值来处理影响瞬时点的 {@code ZoneOffset} 范围，以及本地日期时间。
     * 该值还被选择使得年份的值可以存储在 {@code int} 中。
     */
    public static final Instant MAX = Instant.ofEpochSecond(MAX_SECOND, 999_999_999);

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -665713676816604388L;

    /**
     * 从 1970-01-01T00:00:00Z 纪元开始的秒数。
     */
    private final long seconds;
    /**
     * 从秒字段开始的纳秒数。
     * 这始终是正数，且不超过 999,999,999。
     */
    private final int nanos;

    //-----------------------------------------------------------------------
    /**
     * 从系统时钟获取当前瞬时点。
     * <p>
     * 这将查询 {@link Clock#systemUTC() 系统 UTC 时钟} 以获取当前瞬时点。
     * <p>
     * 使用此方法将防止在测试中使用替代时间源，因为时钟实际上是硬编码的。
     *
     * @return 使用系统时钟的当前瞬时点，不为空
     */
    public static Instant now() {
        return Clock.systemUTC().instant();
    }

    /**
     * 从指定的时钟获取当前瞬时点。
     * <p>
     * 这将查询指定的时钟以获取当前时间。
     * <p>
     * 使用此方法允许在测试中使用替代时钟。
     * 可以使用 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前瞬时点，不为空
     */
    public static Instant now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return clock.instant();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用从 1970-01-01T00:00:00Z 纪元开始的秒数获取 {@code Instant} 的实例。
     * <p>
     * 纳秒字段设置为零。
     *
     * @param epochSecond  从 1970-01-01T00:00:00Z 开始的秒数
     * @return 一个瞬时点，不为空
     * @throws DateTimeException 如果瞬时点超出最大或最小瞬时点
     */
    public static Instant ofEpochSecond(long epochSecond) {
        return create(epochSecond, 0);
    }

    /**
     * 使用从 1970-01-01T00:00:00Z 纪元开始的秒数和纳秒部分获取 {@code Instant} 的实例。
     * <p>
     * 该方法允许传递任意数量的纳秒。
     * 工厂将调整秒数和纳秒值，以确保存储的纳秒在 0 到 999,999,999 之间。
     * 例如，以下将导致完全相同的瞬时点：
     * <pre>
     *  Instant.ofEpochSecond(3, 1);
     *  Instant.ofEpochSecond(4, -999_999_999);
     *  Instant.ofEpochSecond(2, 1000_000_001);
     * </pre>
     *
     * @param epochSecond  从 1970-01-01T00:00:00Z 开始的秒数
     * @param nanoAdjustment  秒数的纳秒调整值，正数或负数
     * @return 一个瞬时点，不为空
     * @throws DateTimeException 如果瞬时点超出最大或最小瞬时点
     * @throws ArithmeticException 如果发生数值溢出
     */
    public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
        long secs = Math.addExact(epochSecond, Math.floorDiv(nanoAdjustment, NANOS_PER_SECOND));
        int nos = (int)Math.floorMod(nanoAdjustment, NANOS_PER_SECOND);
        return create(secs, nos);
    }


                /**
     * 使用从 1970-01-01T00:00:00Z 时代开始的毫秒数获取 {@code Instant} 实例。
     * <p>
     * 从指定的毫秒数中提取秒数和纳秒数。
     *
     * @param epochMilli 从 1970-01-01T00:00:00Z 开始的毫秒数
     * @return 一个即时对象，不为空
     * @throws DateTimeException 如果即时对象超出最大或最小即时
     */
    public static Instant ofEpochMilli(long epochMilli) {
        long secs = Math.floorDiv(epochMilli, 1000);
        int mos = (int)Math.floorMod(epochMilli, 1000);
        return create(secs, mos * 1000_000);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code Instant} 实例。
     * <p>
     * 根据指定的时间对象获取即时对象。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，该工厂将其转换为 {@code Instant} 实例。
     * <p>
     * 转换提取 {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS}
     * 和 {@link ChronoField#NANO_OF_SECOND NANO_OF_SECOND} 字段。
     * <p>
     * 该方法匹配函数接口 {@link TemporalQuery} 的签名，允许通过方法引用使用它，例如 {@code Instant::from}。
     *
     * @param temporal 要转换的时间对象，不为空
     * @return 即时对象，不为空
     * @throws DateTimeException 如果无法转换为 {@code Instant}
     */
    public static Instant from(TemporalAccessor temporal) {
        if (temporal instanceof Instant) {
            return (Instant) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            long instantSecs = temporal.getLong(INSTANT_SECONDS);
            int nanoOfSecond = temporal.get(NANO_OF_SECOND);
            return Instant.ofEpochSecond(instantSecs, nanoOfSecond);
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 Instant: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 2007-12-03T10:15:30.00Z}）获取 {@code Instant} 实例。
     * <p>
     * 字符串必须表示一个有效的 UTC 即时对象，并使用
     * {@link DateTimeFormatter#ISO_INSTANT} 进行解析。
     *
     * @param text 要解析的文本，不为空
     * @return 解析后的即时对象，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static Instant parse(final CharSequence text) {
        return DateTimeFormatter.ISO_INSTANT.parse(text, Instant::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用秒数和纳秒数获取 {@code Instant} 实例。
     *
     * @param seconds 秒数
     * @param nanoOfSecond 纳秒数，从 0 到 999,999,999
     * @throws DateTimeException 如果即时对象超出最大或最小即时
     */
    private static Instant create(long seconds, int nanoOfSecond) {
        if ((seconds | nanoOfSecond) == 0) {
            return EPOCH;
        }
        if (seconds < MIN_SECOND || seconds > MAX_SECOND) {
            throw new DateTimeException("即时对象超出最大或最小即时");
        }
        return new Instant(seconds, nanoOfSecond);
    }

    /**
     * 使用从 1970-01-01T00:00:00Z 时代开始的秒数和纳秒数构造 {@code Instant} 实例。
     *
     * @param epochSecond 从 1970-01-01T00:00:00Z 开始的秒数
     * @param nanos 纳秒数，必须为正数
     */
    private Instant(long epochSecond, int nanos) {
        super();
        this.seconds = epochSecond;
        this.nanos = nanos;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否支持。
     * <p>
     * 检查此即时对象是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code NANO_OF_SECOND}
     * <li>{@code MICRO_OF_SECOND}
     * <li>{@code MILLI_OF_SECOND}
     * <li>{@code INSTANT_SECONDS}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否支持由字段决定。
     *
     * @param field 要检查的字段，null 返回 false
     * @return 如果字段在此即时对象上支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == INSTANT_SECONDS || field == NANO_OF_SECOND || field == MICRO_OF_SECOND || field == MILLI_OF_SECOND;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 检查指定的单位是否可以添加到或从这个日期时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位有：
     * <ul>
     * <li>{@code NANOS}
     * <li>{@code MICROS}
     * <li>{@code MILLIS}
     * <li>{@code SECONDS}
     * <li>{@code MINUTES}
     * <li>{@code HOURS}
     * <li>{@code HALF_DAYS}
     * <li>{@code DAYS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果是通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递获得的。
     * 单位是否支持由单位决定。
     *
     * @param unit 要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased() || unit == DAYS;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此即时对象用于增强返回范围的准确性。
     * 如果由于字段不支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 范围是否可以获取由字段决定。
     *
     * @param field 要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     */
    @Override  // override for Javadoc
    public ValueRange range(TemporalField field) {
        return Temporal.super.range(field);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 从此即时对象查询指定字段的值。
     * 返回的值将始终在字段的有效范围内。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值，
     * 但 {@code INSTANT_SECONDS} 太大，无法放入 {@code int}，将抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取，以及值的含义由字段决定。
     *
     * @param field 要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // override for Javadoc and performance
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case NANO_OF_SECOND: return nanos;
                case MICRO_OF_SECOND: return nanos / 1000;
                case MILLI_OF_SECOND: return nanos / 1000_000;
                case INSTANT_SECONDS: INSTANT_SECONDS.checkValidIntValue(seconds);
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return range(field).checkValidIntValue(field.getFrom(this), field);
    }

    /**
     * 以 {@code long} 形式获取指定字段的值。
     * <p>
     * 从此即时对象查询指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回基于此日期时间的有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 值是否可以获取，以及值的含义由字段决定。
     *
     * @param field 要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case NANO_OF_SECOND: return nanos;
                case MICRO_OF_SECOND: return nanos / 1000;
                case MILLI_OF_SECOND: return nanos / 1000_000;
                case INSTANT_SECONDS: return seconds;
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取从 Java 时代 1970-01-01T00:00:00Z 开始的秒数。
     * <p>
     * 从 1970-01-01T00:00:00Z 开始的秒数是一个简单的递增计数。
     * 秒数 0 是 1970-01-01T00:00:00Z。
     * 秒数的纳秒部分由 {@code getNanosOfSecond} 返回。
     *
     * @return 从 1970-01-01T00:00:00Z 开始的秒数
     */
    public long getEpochSecond() {
        return seconds;
    }

    /**
     * 获取从秒开始的纳秒数。
     * <p>
     * 纳秒数表示从 {@code getEpochSecond} 返回的秒数开始的总纳秒数。
     *
     * @return 秒数内的纳秒数，始终为正数，不超过 999,999,999
     */
    public int getNano() {
        return nanos;
    }

    //-------------------------------------------------------------------------
    /**
     * 返回调整后的即时对象副本。
     * <p>
     * 返回一个基于此即时对象的 {@code Instant}，即时对象已根据指定的调整器策略对象进行调整。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 该方法的结果是通过调用
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并在指定调整器上调用 {@code this} 作为参数获得的。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不为空
     * @return 基于 {@code this} 并已进行调整的 {@code Instant}，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant with(TemporalAdjuster adjuster) {
        return (Instant) adjuster.adjustInto(this);
    }

    /**
     * 返回一个基于此即时对象的副本，指定字段的值已更改。
     * <p>
     * 返回一个基于此即时对象的 {@code Instant}，指定字段的值已更改。
     * 如果由于字段不支持或其他原因无法设置值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此实现。
     * 支持的字段行为如下：
     * <ul>
     * <li>{@code NANO_OF_SECOND} -
     *  返回一个具有指定纳秒数的 {@code Instant}。秒数将保持不变。
     * <li>{@code MICRO_OF_SECOND} -
     *  返回一个具有指定微秒数乘以 1,000 的纳秒数的 {@code Instant}。秒数将保持不变。
     * <li>{@code MILLI_OF_SECOND} -
     *  返回一个具有指定毫秒数乘以 1,000,000 的纳秒数的 {@code Instant}。秒数将保持不变。
     * <li>{@code INSTANT_SECONDS} -
     *  返回一个具有指定秒数的 {@code Instant}。纳秒数将保持不变。
     * </ul>
     * <p>
     * 在所有情况下，如果新值超出字段的有效范围，则抛出 {@code DateTimeException}。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 并将 {@code this} 作为参数传递获得的。
     * 字段决定是否以及如何调整即时对象。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field 要在结果中设置的字段，不为空
     * @param newValue 结果中字段的新值
     * @return 基于 {@code this} 并已设置指定字段的 {@code Instant}，不为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case MILLI_OF_SECOND: {
                    int nval = (int) newValue * 1000_000;
                    return (nval != nanos ? create(seconds, nval) : this);
                }
                case MICRO_OF_SECOND: {
                    int nval = (int) newValue * 1000;
                    return (nval != nanos ? create(seconds, nval) : this);
                }
                case NANO_OF_SECOND: return (newValue != nanos ? create(seconds, (int) newValue) : this);
                case INSTANT_SECONDS: return (newValue != seconds ? create(newValue, nanos) : this);
            }
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.adjustInto(this, newValue);
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本已根据指定单位截断。
     * <p>
     * 截断该瞬间返回原始副本的副本，其中小于指定单位的字段被设置为零。
     * 字段的计算基于使用 UTC 偏移量，如 {@code toString} 中所见。
     * 例如，使用 {@link ChronoUnit#MINUTES MINUTES} 单位截断将
     * 四舍五入到最近的分钟，将秒和纳秒设置为零。
     * <p>
     * 该单位必须具有一个 {@linkplain TemporalUnit#getDuration() 持续时间}
     * 该持续时间可以被标准天的长度整除，没有余数。
     * 这包括 {@link ChronoUnit} 中提供的所有时间单位和
     * {@link ChronoUnit#DAYS DAYS}。其他单位将抛出异常。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此瞬间的 {@code Instant}，时间已截断，不为空
     * @throws DateTimeException 如果单位对截断无效
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public Instant truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() > LocalTime.SECONDS_PER_DAY) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if ((LocalTime.NANOS_PER_DAY % dur) != 0) {
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod = (seconds % LocalTime.SECONDS_PER_DAY) * LocalTime.NANOS_PER_SECOND + nanos;
        long result = (nod / dur) * dur;
        return plusNanos(result - nod);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本已添加指定的数量。
     * <p>
     * 这返回一个 {@code Instant}，基于此实例，已添加指定的数量。
     * 该数量通常是 {@link Duration}，但可以是实现
     * {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给数量对象，通过调用
     * {@link TemporalAmount#addTo(Temporal)}。数量实现可以自由地
     * 以任何方式实现加法，但通常会回调 {@link #plus(long, TemporalUnit)}。请参阅数量实现的文档
     * 以确定是否可以成功添加。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加的数量，不为空
     * @return 基于此瞬间的 {@code Instant}，已进行加法，不为空
     * @throws DateTimeException 如果无法进行加法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant plus(TemporalAmount amountToAdd) {
        return (Instant) amountToAdd.addTo(this);
    }

    /**
     * 返回一个副本，该副本已添加指定的数量。
     * <p>
     * 这返回一个 {@code Instant}，基于此实例，已添加以单位表示的数量。如果无法添加数量，因为
     * 单位不受支持或其他原因，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则加法在此处实现。
     * 支持的字段表现如下：
     * <ul>
     * <li>{@code NANOS} -
     *  返回一个 {@code Instant}，已添加指定的纳秒数。
     *  这相当于 {@link #plusNanos(long)}。
     * <li>{@code MICROS} -
     *  返回一个 {@code Instant}，已添加指定的微秒数。
     *  这相当于 {@link #plusNanos(long)}，数量乘以 1,000。
     * <li>{@code MILLIS} -
     *  返回一个 {@code Instant}，已添加指定的毫秒数。
     *  这相当于 {@link #plusNanos(long)}，数量乘以 1,000,000。
     * <li>{@code SECONDS} -
     *  返回一个 {@code Instant}，已添加指定的秒数。
     *  这相当于 {@link #plusSeconds(long)}。
     * <li>{@code MINUTES} -
     *  返回一个 {@code Instant}，已添加指定的分钟数。
     *  这相当于 {@link #plusSeconds(long)}，数量乘以 60。
     * <li>{@code HOURS} -
     *  返回一个 {@code Instant}，已添加指定的小时数。
     *  这相当于 {@link #plusSeconds(long)}，数量乘以 3,600。
     * <li>{@code HALF_DAYS} -
     *  返回一个 {@code Instant}，已添加指定的半天数。
     *  这相当于 {@link #plusSeconds(long)}，数量乘以 43,200（12 小时）。
     * <li>{@code DAYS} -
     *  返回一个 {@code Instant}，已添加指定的天数。
     *  这相当于 {@link #plusSeconds(long)}，数量乘以 86,400（24 小时）。
     * </ul>
     * <p>
     * 所有其他 {@code ChronoUnit} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.addTo(Temporal, long)}
     * 传递 {@code this} 作为参数获得的。在这种情况下，单位确定
     * 是否以及如何执行加法。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加到结果中的单位数量，可以为负
     * @param unit  要添加的数量的单位，不为空
     * @return 基于此瞬间的 {@code Instant}，已添加指定的数量，不为空
     * @throws DateTimeException 如果无法进行加法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case NANOS: return plusNanos(amountToAdd);
                case MICROS: return plus(amountToAdd / 1000_000, (amountToAdd % 1000_000) * 1000);
                case MILLIS: return plusMillis(amountToAdd);
                case SECONDS: return plusSeconds(amountToAdd);
                case MINUTES: return plusSeconds(Math.multiplyExact(amountToAdd, SECONDS_PER_MINUTE));
                case HOURS: return plusSeconds(Math.multiplyExact(amountToAdd, SECONDS_PER_HOUR));
                case HALF_DAYS: return plusSeconds(Math.multiplyExact(amountToAdd, SECONDS_PER_DAY / 2));
                case DAYS: return plusSeconds(Math.multiplyExact(amountToAdd, SECONDS_PER_DAY));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本已添加指定的秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param secondsToAdd  要添加的秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已添加指定的秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant plusSeconds(long secondsToAdd) {
        return plus(secondsToAdd, 0);
    }

    /**
     * 返回一个副本，该副本已添加指定的毫秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param millisToAdd  要添加的毫秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已添加指定的毫秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant plusMillis(long millisToAdd) {
        return plus(millisToAdd / 1000, (millisToAdd % 1000) * 1000_000);
    }

    /**
     * 返回一个副本，该副本已添加指定的纳秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param nanosToAdd  要添加的纳秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已添加指定的纳秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant plusNanos(long nanosToAdd) {
        return plus(0, nanosToAdd);
    }

    /**
     * 返回一个副本，该副本已添加指定的持续时间。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param secondsToAdd  要添加的秒数，可以为正或负
     * @param nanosToAdd  要添加的纳秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已添加指定的秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    private Instant plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0) {
            return this;
        }
        long epochSec = Math.addExact(seconds, secondsToAdd);
        epochSec = Math.addExact(epochSec, nanosToAdd / NANOS_PER_SECOND);
        nanosToAdd = nanosToAdd % NANOS_PER_SECOND;
        long nanoAdjustment = nanos + nanosToAdd;  // 安全的 int + NANOS_PER_SECOND
        return ofEpochSecond(epochSec, nanoAdjustment);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本已减去指定的数量。
     * <p>
     * 这返回一个 {@code Instant}，基于此实例，已减去指定的数量。
     * 该数量通常是 {@link Duration}，但可以是实现
     * {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算是委托给数量对象，通过调用
     * {@link TemporalAmount#subtractFrom(Temporal)}。数量实现可以自由地
     * 以任何方式实现减法，但通常会回调 {@link #minus(long, TemporalUnit)}。请参阅数量实现的文档
     * 以确定是否可以成功减去。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于此瞬间的 {@code Instant}，已进行减法，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant minus(TemporalAmount amountToSubtract) {
        return (Instant) amountToSubtract.subtractFrom(this);
    }

    /**
     * 返回一个副本，该副本已减去指定的数量。
     * <p>
     * 这返回一个 {@code Instant}，基于此实例，已减去以单位表示的数量。如果无法减去数量，
     * 因为单位不受支持或其他原因，将抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，数量为负。
     * 请参阅该方法以了解加法（因此减法）的工作原理。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要从结果中减去的单位数量，可以为负
     * @param unit  要减去的数量的单位，不为空
     * @return 基于此瞬间的 {@code Instant}，已减去指定的数量，不为空
     * @throws DateTimeException 如果无法进行减法
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Instant minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，该副本已减去指定的秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param secondsToSubtract  要减去的秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已减去指定的秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant minusSeconds(long secondsToSubtract) {
        if (secondsToSubtract == Long.MIN_VALUE) {
            return plusSeconds(Long.MAX_VALUE).plusSeconds(1);
        }
        return plusSeconds(-secondsToSubtract);
    }

    /**
     * 返回一个副本，该副本已减去指定的毫秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param millisToSubtract  要减去的毫秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已减去指定的毫秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant minusMillis(long millisToSubtract) {
        if (millisToSubtract == Long.MIN_VALUE) {
            return plusMillis(Long.MAX_VALUE).plusMillis(1);
        }
        return plusMillis(-millisToSubtract);
    }

    /**
     * 返回一个副本，该副本已减去指定的纳秒数。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param nanosToSubtract  要减去的纳秒数，可以为正或负
     * @return 基于此瞬间的 {@code Instant}，已减去指定的纳秒数，不为空
     * @throws DateTimeException 如果结果超出最大或最小瞬间
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Instant minusNanos(long nanosToSubtract) {
        if (nanosToSubtract == Long.MIN_VALUE) {
            return plusNanos(Long.MAX_VALUE).plusNanos(1);
        }
        return plusNanos(-nanosToSubtract);
    }


                //-------------------------------------------------------------------------
    /**
     * 使用指定的查询策略查询此时间点。
     * <p>
     * 此方法使用指定的查询策略对象查询此时间点。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询的文档以了解此方法的结果。
     * <p>
     * 该方法的结果是通过调用指定查询上的
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不允许为 null
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // 作为优化，内联 TemporalAccessor.super.query(query)
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() ||
                query == TemporalQueries.zone() || query == TemporalQueries.offset() ||
                query == TemporalQueries.localDate() || query == TemporalQueries.localTime()) {
            return null;
        }
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象以具有此时间点。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的时态对象，但时间点已更改为与此时间点相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)}
     * 两次，传递 {@link ChronoField#INSTANT_SECONDS} 和
     * {@link ChronoField#NANO_OF_SECOND} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式会更清晰，使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等价的，但推荐第二种方法
     *   temporal = thisInstant.adjustInto(temporal);
     *   temporal = temporal.with(thisInstant);
     * </pre>
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不允许为 null
     * @return 调整后的对象，不允许为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(INSTANT_SECONDS, seconds).with(NANO_OF_SECOND, nanos);
    }

    /**
     * 计算到另一个时间点的时间量，以指定的单位表示。
     * <p>
     * 此方法计算两个 {@code Instant} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的时间点。
     * 如果终点在起点之前，结果将为负数。
     * 计算返回一个整数，表示两个时间点之间完整的单位数。
     * 传递给此方法的 {@code Temporal} 使用 {@link #from(TemporalAccessor)} 转换为
     * {@code Instant}。例如，可以使用 {@code startInstant.until(endInstant, SECONDS)}
     * 计算两个日期之间的天数。
     * <p>
     * 使用此方法有两种等效的方式。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等价的
     *   amount = start.until(end, SECONDS);
     *   amount = SECONDS.between(start, end);
     * </pre>
     * 选择应基于哪种方式使代码更易读。
     * <p>
     * 该计算在 {@link ChronoUnit} 的实现中完成。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS}、{@code HALF_DAYS} 和 {@code DAYS}。
     * 其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则此方法的结果
     * 是通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并传递 {@code this} 作为第一个参数和转换后的输入时态作为第二个参数获得的。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param endExclusive  终点，不包括，转换为 {@code Instant}，不允许为 null
     * @param unit  要测量的时间量的单位，不允许为 null
     * @return 从这个时间点到终点的时间量
     * @throws DateTimeException 如果无法计算时间量，或者终点时态无法转换为 {@code Instant}
     * @throws UnsupportedTemporalTypeException 如果单位不支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Instant end = Instant.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case NANOS: return nanosUntil(end);
                case MICROS: return nanosUntil(end) / 1000;
                case MILLIS: return Math.subtractExact(end.toEpochMilli(), toEpochMilli());
                case SECONDS: return secondsUntil(end);
                case MINUTES: return secondsUntil(end) / SECONDS_PER_MINUTE;
                case HOURS: return secondsUntil(end) / SECONDS_PER_HOUR;
                case HALF_DAYS: return secondsUntil(end) / (12 * SECONDS_PER_HOUR);
                case DAYS: return secondsUntil(end) / (SECONDS_PER_DAY);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    private long nanosUntil(Instant end) {
        long secsDiff = Math.subtractExact(end.seconds, seconds);
        long totalNanos = Math.multiplyExact(secsDiff, NANOS_PER_SECOND);
        return Math.addExact(totalNanos, end.nanos - nanos);
    }

    private long secondsUntil(Instant end) {
        long secsDiff = Math.subtractExact(end.seconds, seconds);
        long nanosDiff = end.nanos - nanos;
        if (secsDiff > 0 && nanosDiff < 0) {
            secsDiff--;
        } else if (secsDiff < 0 && nanosDiff > 0) {
            secsDiff++;
        }
        return secsDiff;
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间点与偏移量结合以创建 {@code OffsetDateTime}。
     * <p>
     * 此方法返回一个由此时间点和指定的 UTC/Greenwich 偏移量组成的 {@code OffsetDateTime}。
     * 如果时间点太大而无法适应带偏移量的日期时间，则会抛出异常。
     * <p>
     * 此方法等同于
     * {@link OffsetDateTime#ofInstant(Instant, ZoneId) OffsetDateTime.ofInstant(this, offset)}。
     *
     * @param offset  要结合的偏移量，不允许为 null
     * @return 由此时间点和指定偏移量组成的带偏移量的日期时间，不允许为 null
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.ofInstant(this, offset);
    }

    /**
     * 将此时间点与时区结合以创建 {@code ZonedDateTime}。
     * <p>
     * 此方法返回一个由此时间点和指定的时区组成的 {@code ZonedDateTime}。
     * 如果时间点太大而无法适应带时区的日期时间，则会抛出异常。
     * <p>
     * 此方法等同于
     * {@link ZonedDateTime#ofInstant(Instant, ZoneId) ZonedDateTime.ofInstant(this, zone)}。
     *
     * @param zone  要结合的时区，不允许为 null
     * @return 由此时间点和指定时区组成的带时区的日期时间，不允许为 null
     * @throws DateTimeException 如果结果超出支持的范围
     */
    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.ofInstant(this, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间点转换为自 1970-01-01T00:00:00Z 以来的毫秒数。
     * <p>
     * 如果此时间点表示的时间点太远，超出了 {@code long} 毫秒的范围，则会抛出异常。
     * <p>
     * 如果此时间点的精度高于毫秒，则转换将丢弃多余的精度信息，就像纳秒数被整除一百万一样。
     *
     * @return 自 1970-01-01T00:00:00Z 以来的毫秒数
     * @throws ArithmeticException 如果发生数值溢出
     */
    public long toEpochMilli() {
        if (seconds < 0 && nanos > 0) {
            long millis = Math.multiplyExact(seconds+1, 1000);
            long adjustment = nanos / 1000_000 - 1000;
            return Math.addExact(millis, adjustment);
        } else {
            long millis = Math.multiplyExact(seconds, 1000);
            return Math.addExact(millis, nanos / 1000_000);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间点与指定的时间点进行比较。
     * <p>
     * 比较基于时间线上的时间点位置。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义的。
     *
     * @param otherInstant  要比较的其他时间点，不允许为 null
     * @return 比较值，小于 0 表示小于，大于 0 表示大于
     * @throws NullPointerException 如果 otherInstant 为 null
     */
    @Override
    public int compareTo(Instant otherInstant) {
        int cmp = Long.compare(seconds, otherInstant.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return nanos - otherInstant.nanos;
    }

    /**
     * 检查此时间点是否在指定的时间点之后。
     * <p>
     * 比较基于时间线上的时间点位置。
     *
     * @param otherInstant  要比较的其他时间点，不允许为 null
     * @return 如果此时间点在指定的时间点之后，则返回 true
     * @throws NullPointerException 如果 otherInstant 为 null
     */
    public boolean isAfter(Instant otherInstant) {
        return compareTo(otherInstant) > 0;
    }

    /**
     * 检查此时间点是否在指定的时间点之前。
     * <p>
     * 比较基于时间线上的时间点位置。
     *
     * @param otherInstant  要比较的其他时间点，不允许为 null
     * @return 如果此时间点在指定的时间点之前，则返回 true
     * @throws NullPointerException 如果 otherInstant 为 null
     */
    public boolean isBefore(Instant otherInstant) {
        return compareTo(otherInstant) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时间点是否等于指定的时间点。
     * <p>
     * 比较基于时间线上的时间点位置。
     *
     * @param otherInstant  其他时间点，null 返回 false
     * @return 如果其他时间点等于此时间点，则返回 true
     */
    @Override
    public boolean equals(Object otherInstant) {
        if (this == otherInstant) {
            return true;
        }
        if (otherInstant instanceof Instant) {
            Instant other = (Instant) otherInstant;
            return this.seconds == other.seconds &&
                   this.nanos == other.nanos;
        }
        return false;
    }

    /**
     * 返回此时间点的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return ((int) (seconds ^ (seconds >>> 32))) + 51 * nanos;
    }

    //-----------------------------------------------------------------------
    /**
     * 使用 ISO-8601 表示法返回此时间点的字符串表示。
     * <p>
     * 使用的格式与 {@link DateTimeFormatter#ISO_INSTANT} 相同。
     *
     * @return 此时间点的 ISO-8601 表示，不允许为 null
     */
    @Override
    public String toString() {
        return DateTimeFormatter.ISO_INSTANT.format(this);
    }

    // -----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(2);  // 标识一个 Instant
     *  out.writeLong(seconds);
     *  out.writeInt(nanos);
     * </pre>
     *
     * @return {@code Ser} 的实例，不允许为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.INSTANT_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeLong(seconds);
        out.writeInt(nanos);
    }

    static Instant readExternal(DataInput in) throws IOException {
        long seconds = in.readLong();
        int nanos = in.readInt();
        return Instant.ofEpochSecond(seconds, nanos);
    }

}
