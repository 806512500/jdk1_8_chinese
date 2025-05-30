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

import java.time.Duration;

/**
 * 一组标准的日期周期单位。
 * <p>
 * 这组单位提供基于单位的访问，以操作日期、时间或日期时间。
 * 标准单位集可以通过实现 {@link TemporalUnit} 来扩展。
 * <p>
 * 这些单位旨在适用于多种日历系统。
 * 例如，大多数非 ISO 日历系统定义了年、月和日的单位，只是规则略有不同。
 * 每个单位的文档解释了其操作方式。
 *
 * @implSpec
 * 这是一个最终的、不可变的和线程安全的枚举。
 *
 * @since 1.8
 */
public enum ChronoUnit implements TemporalUnit {

    /**
     * 表示纳秒概念的单位，是最小的时间单位。
     * 对于 ISO 日历系统，它等于秒单位的 1,000,000,000 分之一。
     */
    NANOS("Nanos", Duration.ofNanos(1)),
    /**
     * 表示微秒概念的单位。
     * 对于 ISO 日历系统，它等于秒单位的 1,000,000 分之一。
     */
    MICROS("Micros", Duration.ofNanos(1000)),
    /**
     * 表示毫秒概念的单位。
     * 对于 ISO 日历系统，它等于秒单位的 1000 分之一。
     */
    MILLIS("Millis", Duration.ofNanos(1000_000)),
    /**
     * 表示秒概念的单位。
     * 对于 ISO 日历系统，它等于 SI 单位系统中的秒，除了在闰秒附近。
     */
    SECONDS("Seconds", Duration.ofSeconds(1)),
    /**
     * 表示分钟概念的单位。
     * 对于 ISO 日历系统，它等于 60 秒。
     */
    MINUTES("Minutes", Duration.ofSeconds(60)),
    /**
     * 表示小时概念的单位。
     * 对于 ISO 日历系统，它等于 60 分钟。
     */
    HOURS("Hours", Duration.ofSeconds(3600)),
    /**
     * 表示半天概念的单位，用于 AM/PM。
     * 对于 ISO 日历系统，它等于 12 小时。
     */
    HALF_DAYS("HalfDays", Duration.ofSeconds(43200)),
    /**
     * 表示天概念的单位。
     * 对于 ISO 日历系统，它是从午夜到午夜的标准天。
     * 一天的估计持续时间是 {@code 24 小时}。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于地球上的日出和日落之间的时间。不要求天从午夜开始——在转换日历系统时，日期应在中午等效。
     */
    DAYS("Days", Duration.ofSeconds(86400)),
    /**
     * 表示周概念的单位。
     * 对于 ISO 日历系统，它等于 7 天。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天。
     */
    WEEKS("Weeks", Duration.ofSeconds(7 * 86400L)),
    /**
     * 表示月概念的单位。
     * 对于 ISO 日历系统，月的长度因年份中的月而异。
     * 月的估计持续时间是一年 {@code 365.2425 天} 的十二分之一。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天。
     */
    MONTHS("Months", Duration.ofSeconds(31556952L / 12)),
    /**
     * 表示年概念的单位。
     * 对于 ISO 日历系统，它等于 12 个月。
     * 年的估计持续时间是 {@code 365.2425 天}。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天或大致等于地球绕太阳公转定义的年。
     */
    YEARS("Years", Duration.ofSeconds(31556952L)),
    /**
     * 表示十年概念的单位。
     * 对于 ISO 日历系统，它等于 10 年。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天，通常是整数年。
     */
    DECADES("Decades", Duration.ofSeconds(31556952L * 10L)),
    /**
     * 表示世纪概念的单位。
     * 对于 ISO 日历系统，它等于 100 年。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天，通常是整数年。
     */
    CENTURIES("Centuries", Duration.ofSeconds(31556952L * 100L)),
    /**
     * 表示千年概念的单位。
     * 对于 ISO 日历系统，它等于 1000 年。
     * <p>
     * 当与其他日历系统一起使用时，它必须对应于整数天，通常是整数年。
     */
    MILLENNIA("Millennia", Duration.ofSeconds(31556952L * 1000L)),
    /**
     * 表示纪元概念的单位。
     * ISO 日历系统没有纪元，因此无法向日期或日期时间添加纪元。
     * 纪元的估计持续时间被人为定义为 {@code 1,000,000,000 年}。
     * <p>
     * 当与其他日历系统一起使用时，对单位没有限制。
     */
    ERAS("Eras", Duration.ofSeconds(31556952L * 1000_000_000L)),
    /**
     * 表示永恒概念的人为单位。
     * 这主要用于与 {@link TemporalField} 一起表示无界字段，如年或纪元。
     * 纪元的估计持续时间被人为定义为 {@code Duration} 支持的最大持续时间。
     */
    FOREVER("Forever", Duration.ofSeconds(Long.MAX_VALUE, 999_999_999));

    private final String name;
    private final Duration duration;

    private ChronoUnit(String name, Duration estimatedDuration) {
        this.name = name;
        this.duration = estimatedDuration;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此单位在 ISO 日历系统中的估计持续时间。
     * <p>
     * 本类中的所有单位都有估计的持续时间。
     * 天因夏令时而变化，而月的长度各不相同。
     *
     * @return 此单位的估计持续时间，不为空
     */
    @Override
    public Duration getDuration() {
        return duration;
    }

    /**
     * 检查单位的持续时间是否为估计值。
     * <p>
     * 本类中的所有时间单位都被认为是准确的，而所有日期单位都被认为是估计的。
     * <p>
     * 此定义忽略了闰秒，但考虑了天因夏令时而变化，月的长度各不相同。
     *
     * @return 如果持续时间是估计的，则返回 true，否则返回 false
     */
    @Override
    public boolean isDurationEstimated() {
        return this.compareTo(DAYS) >= 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此单位是否为日期单位。
     * <p>
     * 从天到纪元（包括）的所有单位都是基于日期的。
     * 基于时间的单位和 {@code FOREVER} 返回 false。
     *
     * @return 如果是日期单位，则返回 true，否则返回 false
     */
    @Override
    public boolean isDateBased() {
        return this.compareTo(DAYS) >= 0 && this != FOREVER;
    }

    /**
     * 检查此单位是否为时间单位。
     * <p>
     * 从纳秒到半天（包括）的所有单位都是基于时间的。
     * 基于日期的单位和 {@code FOREVER} 返回 false。
     *
     * @return 如果是时间单位，则返回 true，否则返回 false
     */
    @Override
    public boolean isTimeBased() {
        return this.compareTo(DAYS) < 0;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupportedBy(Temporal temporal) {
        return temporal.isSupported(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        return (R) temporal.plus(amount, this);
    }

    //-----------------------------------------------------------------------
    @Override
    public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        return temporal1Inclusive.until(temporal2Exclusive, this);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return name;
    }

}
