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
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.format;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Objects;

/**
 * 用于日期和时间打印的上下文对象。
 * <p>
 * 该类提供了一个包装格式中使用的项的单一包装。
 *
 * @implSpec
 * 该类是一个旨在单线程使用的可变上下文。在标准打印中使用该类是线程安全的，因为框架为每次格式化创建该类的新实例，且打印是单线程的。
 *
 * @since 1.8
 */
final class DateTimePrintContext {

    /**
     * 正在输出的临时对象。
     */
    private TemporalAccessor temporal;
    /**
     * 格式化器，不能为空。
     */
    private DateTimeFormatter formatter;
    /**
     * 当前格式化器是否为可选。
     */
    private int optional;

    /**
     * 创建上下文的新实例。
     *
     * @param temporal  正在输出的临时对象，不能为空
     * @param formatter  控制格式的格式化器，不能为空
     */
    DateTimePrintContext(TemporalAccessor temporal, DateTimeFormatter formatter) {
        super();
        this.temporal = adjust(temporal, formatter);
        this.formatter = formatter;
    }

    private static TemporalAccessor adjust(final TemporalAccessor temporal, DateTimeFormatter formatter) {
        // 首先处理正常情况（提前返回是一种优化）
        Chronology overrideChrono = formatter.getChronology();
        ZoneId overrideZone = formatter.getZone();
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }

        // 确保最小化更改（提前返回是一种优化）
        Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
        ZoneId temporalZone = temporal.query(TemporalQueries.zoneId());
        if (Objects.equals(overrideChrono, temporalChrono)) {
            overrideChrono = null;
        }
        if (Objects.equals(overrideZone, temporalZone)) {
            overrideZone = null;
        }
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }

        // 进行调整
        final Chronology effectiveChrono = (overrideChrono != null ? overrideChrono : temporalChrono);
        if (overrideZone != null) {
            // 如果有区域和瞬时时间，计算很简单，必要时默认使用日历
            if (temporal.isSupported(INSTANT_SECONDS)) {
                Chronology chrono = (effectiveChrono != null ? effectiveChrono : IsoChronology.INSTANCE);
                return chrono.zonedDateTime(Instant.from(temporal), overrideZone);
            }
            // 阻止更改 OffsetTime 的区域，以及类似的问题情况
            if (overrideZone.normalized() instanceof ZoneOffset && temporal.isSupported(OFFSET_SECONDS) &&
                    temporal.get(OFFSET_SECONDS) != overrideZone.getRules().getOffset(Instant.EPOCH).getTotalSeconds()) {
                throw new DateTimeException("无法应用覆盖区域 '" + overrideZone +
                        "'，因为正在格式化的临时对象具有不同的偏移量，但" +
                        "不表示一个瞬时时间: " + temporal);
            }
        }
        final ZoneId effectiveZone = (overrideZone != null ? overrideZone : temporalZone);
        final ChronoLocalDate effectiveDate;
        if (overrideChrono != null) {
            if (temporal.isSupported(EPOCH_DAY)) {
                effectiveDate = effectiveChrono.date(temporal);
            } else {
                // 检查除 epoch-day 之外的日期字段，忽略将 null 转换为 ISO 的情况
                if (!(overrideChrono == IsoChronology.INSTANCE && temporalChrono == null)) {
                    for (ChronoField f : ChronoField.values()) {
                        if (f.isDateBased() && temporal.isSupported(f)) {
                            throw new DateTimeException("无法应用覆盖日历 '" + overrideChrono +
                                    "'，因为正在格式化的临时对象包含日期字段，但" +
                                    "不表示一个完整的日期: " + temporal);
                        }
                    }
                }
                effectiveDate = null;
            }
        } else {
            effectiveDate = null;
        }

        // 组合可用数据
        // 这是一个非标准的临时对象，几乎是一个纯委托
        // 这更好地处理了底层临时实例类似映射的情况
        return new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.isSupported(field);
                }
                return temporal.isSupported(field);
            }
            @Override
            public ValueRange range(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.range(field);
                }
                return temporal.range(field);
            }
            @Override
            public long getLong(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.getLong(field);
                }
                return temporal.getLong(field);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return (R) effectiveChrono;
                }
                if (query == TemporalQueries.zoneId()) {
                    return (R) effectiveZone;
                }
                if (query == TemporalQueries.precision()) {
                    return temporal.query(query);
                }
                return query.queryFrom(this);
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * 获取正在输出的临时对象。
     *
     * @return 临时对象，不能为空
     */
    TemporalAccessor getTemporal() {
        return temporal;
    }

    /**
     * 获取区域设置。
     * <p>
     * 该区域设置用于控制格式输出中的本地化，除非本地化由 DecimalStyle 控制。
     *
     * @return 区域设置，不能为空
     */
    Locale getLocale() {
        return formatter.getLocale();
    }

    /**
     * 获取 DecimalStyle。
     * <p>
     * DecimalStyle 控制数字输出的本地化。
     *
     * @return DecimalStyle，不能为空
     */
    DecimalStyle getDecimalStyle() {
        return formatter.getDecimalStyle();
    }

    //-----------------------------------------------------------------------
    /**
     * 开始打印输入的可选部分。
     */
    void startOptional() {
        this.optional++;
    }

    /**
     * 结束打印输入的可选部分。
     */
    void endOptional() {
        this.optional--;
    }

    /**
     * 使用查询获取值。
     *
     * @param query  要使用的查询，不能为空
     * @return 结果，如果未找到且可选为真，则为 null
     * @throws DateTimeException 如果类型不可用且部分不可选
     */
    <R> R getValue(TemporalQuery<R> query) {
        R result = temporal.query(query);
        if (result == null && optional == 0) {
            throw new DateTimeException("无法提取值: " + temporal.getClass());
        }
        return result;
    }

    /**
     * 获取指定字段的值。
     * <p>
     * 这将返回指定字段的值。
     *
     * @param field  要查找的字段，不能为空
     * @return 值，如果未找到且可选为真，则为 null
     * @throws DateTimeException 如果字段不可用且部分不可选
     */
    Long getValue(TemporalField field) {
        try {
            return temporal.getLong(field);
        } catch (DateTimeException ex) {
            if (optional > 0) {
                return null;
            }
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 返回上下文的字符串版本，用于调试。
     *
     * @return 上下文的字符串表示，不能为空
     */
    @Override
    public String toString() {
        return temporal.toString();
    }

}
