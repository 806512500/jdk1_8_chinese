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
package java.time.temporal;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.FOREVER;

import java.time.DateTimeException;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Map;

/**
 * 一组日期字段，提供对儒略日的访问。
 * <p>
 * 儒略日是一种在科学界常用的标准表示日期和时间的方法。
 * 它表示为一个十进制数，表示从午夜开始的完整天数。
 * 本类表示从午夜开始计数完整天数的儒略日变体。
 * <p>
 * 这些字段是相对于 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 实现的。
 * 如果 {@code EPOCH_DAY} 可用，这些字段是支持的，可以查询和设置。
 * 这些字段适用于所有历法。
 *
 * @implSpec
 * 这是一个不可变且线程安全的类。
 *
 * @since 1.8
 */
public final class JulianFields {

    /**
     * 从儒略日到 EPOCH DAY 的偏移量。
     */
    private static final long JULIAN_DAY_OFFSET = 2440588L;

    /**
     * 儒略日字段。
     * <p>
     * 这是一个基于整数的儒略日数版本。
     * 儒略日是一个广为人知的系统，表示从第 0 天开始的完整天数，
     * 第 0 天被定义为公元前 4713 年 1 月 1 日（儒略历），即格里高利历的 -4713-11-24。
     * 该字段的名称为 'JulianDay'，基本单位为 'DAYS'。
     * 该字段始终引用本地日期时间，忽略偏移量或时区。
     * <p>
     * 对于日期时间，'JULIAN_DAY.getFrom()' 假设从午夜到下一个午夜前的值相同。
     * 当 'JULIAN_DAY.adjustInto()' 应用于日期时间时，一天中的时间部分保持不变。
     * 'JULIAN_DAY.adjustInto()' 和 'JULIAN_DAY.getFrom()' 仅适用于可以转换为 {@code ChronoField#EPOCH_DAY} 的 {@code Temporal} 对象。
     * 对于其他类型的对象，将抛出 {@link UnsupportedTemporalTypeException}。
     * <p>
     * 在解析的解析阶段，可以从儒略日字段创建日期。
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 和 {@linkplain ResolverStyle#SMART 智能模式} 下，
     * 儒略日值将验证是否在有效值范围内。
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，不进行验证。
     *
     * <h3>天文和科学注释</h3>
     * 标准的天文定义使用分数来表示一天中的时间，
     * 因此 3.25 表示时间 18:00，因为一天从中午开始。
     * 本实现使用整数，且一天从午夜开始。
     * 儒略日数的整数值是在问题日期的中午的天文儒略日值。
     * 这相当于天文儒略日，四舍五入到整数 {@code JDN = floor(JD + 0.5)}。
     *
     * <pre>
     *  | ISO 日期          |  儒略日数 | 天文儒略日 |
     *  | 1970-01-01T00:00  |         2,440,588  |         2,440,587.5     |
     *  | 1970-01-01T06:00  |         2,440,588  |         2,440,587.75    |
     *  | 1970-01-01T12:00  |         2,440,588  |         2,440,588.0     |
     *  | 1970-01-01T18:00  |         2,440,588  |         2,440,588.25    |
     *  | 1970-01-02T00:00  |         2,440,589  |         2,440,588.5     |
     *  | 1970-01-02T06:00  |         2,440,589  |         2,440,588.75    |
     *  | 1970-01-02T12:00  |         2,440,589  |         2,440,589.0     |
     * </pre>
     * <p>
     * 儒略日有时被认为暗示了世界时或 UTC，但本实现始终使用本地日期的儒略日数，
     * 无论偏移量或时区如何。
     */
    public static final TemporalField JULIAN_DAY = Field.JULIAN_DAY;

    /**
     * 修改后的儒略日字段。
     * <p>
     * 这是一个基于整数的修改后的儒略日数版本。
     * 修改后的儒略日（MJD）是一个广为人知的系统，连续计数天数。
     * 它相对于天文儒略日定义为 {@code MJD = JD - 2400000.5}。
     * 每个修改后的儒略日从午夜到午夜。
     * 该字段始终引用本地日期时间，忽略偏移量或时区。
     * <p>
     * 对于日期时间，'MODIFIED_JULIAN_DAY.getFrom()' 假设从午夜到下一个午夜前的值相同。
     * 当 'MODIFIED_JULIAN_DAY.adjustInto()' 应用于日期时间时，一天中的时间部分保持不变。
     * 'MODIFIED_JULIAN_DAY.adjustInto()' 和 'MODIFIED_JULIAN_DAY.getFrom()' 仅适用于可以转换为 {@code ChronoField#EPOCH_DAY} 的 {@code Temporal} 对象。
     * 对于其他类型的对象，将抛出 {@link UnsupportedTemporalTypeException}。
     * <p>
     * 本实现是一个基于整数的 MJD，小数部分四舍五入到下限。
     * <p>
     * 在解析的解析阶段，可以从修改后的儒略日字段创建日期。
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 和 {@linkplain ResolverStyle#SMART 智能模式} 下，
     * 修改后的儒略日值将验证是否在有效值范围内。
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，不进行验证。
     *
     * <h3>天文和科学注释</h3>
     * <pre>
     *  | ISO 日期          | 修改后的儒略日 |      小数 MJD |
     *  | 1970-01-01T00:00  |             40,587  |       40,587.0   |
     *  | 1970-01-01T06:00  |             40,587  |       40,587.25  |
     *  | 1970-01-01T12:00  |             40,587  |       40,587.5   |
     *  | 1970-01-01T18:00  |             40,587  |       40,587.75  |
     *  | 1970-01-02T00:00  |             40,588  |       40,588.0   |
     *  | 1970-01-02T06:00  |             40,588  |       40,588.25  |
     *  | 1970-01-02T12:00  |             40,588  |       40,588.5   |
     * </pre>
     *
     * 修改后的儒略日有时被认为暗示了世界时或 UTC，但本实现始终使用本地日期的修改后的儒略日，
     * 无论偏移量或时区如何。
     */
    public static final TemporalField MODIFIED_JULIAN_DAY = Field.MODIFIED_JULIAN_DAY;

    /**
     * Rata Die 字段。
     * <p>
     * Rata Die 从 0001-01-01（ISO）午夜开始连续计数完整天数，第 1 天从午夜开始。
     * 该字段始终引用本地日期时间，忽略偏移量或时区。
     * <p>
     * 对于日期时间，'RATA_DIE.getFrom()' 假设从午夜到下一个午夜前的值相同。
     * 当 'RATA_DIE.adjustInto()' 应用于日期时间时，一天中的时间部分保持不变。
     * 'RATA_DIE.adjustInto()' 和 'RATA_DIE.getFrom()' 仅适用于可以转换为 {@code ChronoField#EPOCH_DAY} 的 {@code Temporal} 对象。
     * 对于其他类型的对象，将抛出 {@link UnsupportedTemporalTypeException}。
     * <p>
     * 在解析的解析阶段，可以从 Rata Die 字段创建日期。
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 和 {@linkplain ResolverStyle#SMART 智能模式} 下，
     * Rata Die 值将验证是否在有效值范围内。
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，不进行验证。
     */
    public static final TemporalField RATA_DIE = Field.RATA_DIE;

    /**
     * 受限的构造函数。
     */
    private JulianFields() {
        throw new AssertionError("不可实例化");
    }

    /**
     * JulianFields 的实现。每个实例都是单例。
     */
    private static enum Field implements TemporalField {
        JULIAN_DAY("JulianDay", DAYS, FOREVER, JULIAN_DAY_OFFSET),
        MODIFIED_JULIAN_DAY("ModifiedJulianDay", DAYS, FOREVER, 40587L),
        RATA_DIE("RataDie", DAYS, FOREVER, 719163L);

        private static final long serialVersionUID = -7501623920830201812L;

        private final transient String name;
        private final transient TemporalUnit baseUnit;
        private final transient TemporalUnit rangeUnit;
        private final transient ValueRange range;
        private final transient long offset;

        private Field(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, long offset) {
            this.name = name;
            this.baseUnit = baseUnit;
            this.rangeUnit = rangeUnit;
            this.range = ValueRange.of(-365243219162L + offset, 365241780471L + offset);
            this.offset = offset;
        }

        //-----------------------------------------------------------------------
        @Override
        public TemporalUnit getBaseUnit() {
            return baseUnit;
        }

        @Override
        public TemporalUnit getRangeUnit() {
            return rangeUnit;
        }

        @Override
        public boolean isDateBased() {
            return true;
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public ValueRange range() {
            return range;
        }

        //-----------------------------------------------------------------------
        @Override
        public boolean isSupportedBy(TemporalAccessor temporal) {
            return temporal.isSupported(EPOCH_DAY);
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (isSupportedBy(temporal) == false) {
                throw new DateTimeException("不支持的字段: " + this);
            }
            return range();
        }

        @Override
        public long getFrom(TemporalAccessor temporal) {
            return temporal.getLong(EPOCH_DAY) + offset;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            if (range().isValidValue(newValue) == false) {
                throw new DateTimeException("无效值: " + name + " " + newValue);
            }
            return (R) temporal.with(EPOCH_DAY, Math.subtractExact(newValue, offset));
        }

        //-----------------------------------------------------------------------
        @Override
        public ChronoLocalDate resolve(
                Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
            long value = fieldValues.remove(this);
            Chronology chrono = Chronology.from(partialTemporal);
            if (resolverStyle == ResolverStyle.LENIENT) {
                return chrono.dateEpochDay(Math.subtractExact(value, offset));
            }
            range().checkValidValue(value, this);
            return chrono.dateEpochDay(value - offset);
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            return name;
        }
    }
}
