
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
 * Copyright (c) 2008-2013, Stephen Colebourne & Michael Nascimento Santos
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

import static java.time.temporal.ChronoField.AMPM_OF_DAY;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_DAY;
import static java.time.temporal.ChronoField.HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MICRO_OF_DAY;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;
import static java.time.temporal.ChronoField.MILLI_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * 解析数据的存储。
 * <p>
 * 该类在解析过程中用于收集数据。解析过程的一部分涉及处理可选块，因此会创建多个数据副本以支持必要的回溯。
 * <p>
 * 一旦解析完成，该类可以用作结果的 {@code TemporalAccessor}。在大多数情况下，只有在解析字段后才会暴露该类。
 *
 * @implSpec
 * 该类是一个可变的上下文，旨在由单个线程使用。在标准解析中使用该类是线程安全的，因为每次解析都会自动创建该类的新实例，且解析是单线程的。
 *
 * @since 1.8
 */
final class Parsed implements TemporalAccessor {
    // 一些字段使用包范围从 DateTimeParseContext 访问

    /**
     * 解析的字段。
     */
    final Map<TemporalField, Long> fieldValues = new HashMap<>();
    /**
     * 解析的时区。
     */
    ZoneId zone;
    /**
     * 解析的日历系统。
     */
    Chronology chrono;
    /**
     * 是否解析了闰秒。
     */
    boolean leapSecond;
    /**
     * 要使用的解析样式。
     */
    private ResolverStyle resolverStyle;
    /**
     * 解析的日期。
     */
    private ChronoLocalDate date;
    /**
     * 解析的时间。
     */
    private LocalTime time;
    /**
     * 仅解析时间时的额外周期。
     */
    Period excessDays = Period.ZERO;

    /**
     * 创建一个实例。
     */
    Parsed() {
    }

    /**
     * 创建一个副本。
     */
    Parsed copy() {
        // 仅复制解析阶段使用的字段
        Parsed cloned = new Parsed();
        cloned.fieldValues.putAll(this.fieldValues);
        cloned.zone = this.zone;
        cloned.chrono = this.chrono;
        cloned.leapSecond = this.leapSecond;
        return cloned;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(TemporalField field) {
        if (fieldValues.containsKey(field) ||
                (date != null && date.isSupported(field)) ||
                (time != null && time.isSupported(field))) {
            return true;
        }
        return field != null && (field instanceof ChronoField == false) && field.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        Objects.requireNonNull(field, "field");
        Long value = fieldValues.get(field);
        if (value != null) {
            return value;
        }
        if (date != null && date.isSupported(field)) {
            return date.getLong(field);
        }
        if (time != null && time.isSupported(field)) {
            return time.getLong(field);
        }
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
        }
        return field.getFrom(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId()) {
            return (R) zone;
        } else if (query == TemporalQueries.chronology()) {
            return (R) chrono;
        } else if (query == TemporalQueries.localDate()) {
            return (R) (date != null ? LocalDate.from(date) : null);
        } else if (query == TemporalQueries.localTime()) {
            return (R) time;
        } else if (query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return query.queryFrom(this);
        } else if (query == TemporalQueries.precision()) {
            return null;  // 不是完整的日期/时间
        }
        // 作为优化内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 解析此上下文中的字段。
     *
     * @param resolverStyle  解析样式，不为空
     * @param resolverFields  用于解析的字段，为空表示所有字段
     * @return this，用于方法链式调用
     * @throws DateTimeException 如果解析一个字段会导致另一个字段的值冲突
     */
    TemporalAccessor resolve(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
        if (resolverFields != null) {
            fieldValues.keySet().retainAll(resolverFields);
        }
        this.resolverStyle = resolverStyle;
        resolveFields();
        resolveTimeLenient();
        crossCheck();
        resolvePeriod();
        resolveFractional();
        resolveInstant();
        return this;
    }

    //-----------------------------------------------------------------------
    private void resolveFields() {
        // 解析 ChronoField
        resolveInstantFields();
        resolveDateFields();
        resolveTimeFields();

        // 如果有其他字段，处理它们
        // 任何宽松的日期解析应返回 epoch-day
        if (fieldValues.size() > 0) {
            int changedCount = 0;
            outer:
            while (changedCount < 50) {
                for (Map.Entry<TemporalField, Long> entry : fieldValues.entrySet()) {
                    TemporalField targetField = entry.getKey();
                    TemporalAccessor resolvedObject = targetField.resolve(fieldValues, this, resolverStyle);
                    if (resolvedObject != null) {
                        if (resolvedObject instanceof ChronoZonedDateTime) {
                            ChronoZonedDateTime<?> czdt = (ChronoZonedDateTime<?>) resolvedObject;
                            if (zone == null) {
                                zone = czdt.getZone();
                            } else if (zone.equals(czdt.getZone()) == false) {
                                throw new DateTimeException("ChronoZonedDateTime 必须使用有效的解析时区: " + zone);
                            }
                            resolvedObject = czdt.toLocalDateTime();
                        }
                        if (resolvedObject instanceof ChronoLocalDateTime) {
                            ChronoLocalDateTime<?> cldt = (ChronoLocalDateTime<?>) resolvedObject;
                            updateCheckConflict(cldt.toLocalTime(), Period.ZERO);
                            updateCheckConflict(cldt.toLocalDate());
                            changedCount++;
                            continue outer;  // 必须重新启动以避免并发修改
                        }
                        if (resolvedObject instanceof ChronoLocalDate) {
                            updateCheckConflict((ChronoLocalDate) resolvedObject);
                            changedCount++;
                            continue outer;  // 必须重新启动以避免并发修改
                        }
                        if (resolvedObject instanceof LocalTime) {
                            updateCheckConflict((LocalTime) resolvedObject, Period.ZERO);
                            changedCount++;
                            continue outer;  // 必须重新启动以避免并发修改
                        }
                        throw new DateTimeException("方法 resolve() 只能返回 ChronoZonedDateTime, " +
                                "ChronoLocalDateTime, ChronoLocalDate 或 LocalTime");
                    } else if (fieldValues.containsKey(targetField) == false) {
                        changedCount++;
                        continue outer;  // 必须重新启动以避免并发修改
                    }
                }
                break;
            }
            if (changedCount == 50) {  // 捕获无限循环
                throw new DateTimeException("解析的字段之一具有实现不正确的 resolve 方法");
            }
            // 如果有更改，则必须重新解析 ChronoField
            if (changedCount > 0) {
                resolveInstantFields();
                resolveDateFields();
                resolveTimeFields();
            }
        }
    }

    private void updateCheckConflict(TemporalField targetField, TemporalField changeField, Long changeValue) {
        Long old = fieldValues.put(changeField, changeValue);
        if (old != null && old.longValue() != changeValue.longValue()) {
            throw new DateTimeException("发现冲突: " + changeField + " " + old +
                    " 与 " + changeField + " " + changeValue +
                    " 在解析 " + targetField 时不同");
        }
    }

    //-----------------------------------------------------------------------
    private void resolveInstantFields() {
        // 如果有可用的时区，解析解析的即时秒数到日期和时间
        if (fieldValues.containsKey(INSTANT_SECONDS)) {
            if (zone != null) {
                resolveInstantFields0(zone);
            } else {
                Long offsetSecs = fieldValues.get(OFFSET_SECONDS);
                if (offsetSecs != null) {
                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSecs.intValue());
                    resolveInstantFields0(offset);
                }
            }
        }
    }

    private void resolveInstantFields0(ZoneId selectedZone) {
        Instant instant = Instant.ofEpochSecond(fieldValues.remove(INSTANT_SECONDS));
        ChronoZonedDateTime<?> zdt = chrono.zonedDateTime(instant, selectedZone);
        updateCheckConflict(zdt.toLocalDate());
        updateCheckConflict(INSTANT_SECONDS, SECOND_OF_DAY, (long) zdt.toLocalTime().toSecondOfDay());
    }

    //-----------------------------------------------------------------------
    private void resolveDateFields() {
        updateCheckConflict(chrono.resolveDate(fieldValues, resolverStyle));
    }

    private void updateCheckConflict(ChronoLocalDate cld) {
        if (date != null) {
            if (cld != null && date.equals(cld) == false) {
                throw new DateTimeException("发现冲突: 字段解析为两个不同的日期: " + date + " " + cld);
            }
        } else if (cld != null) {
            if (chrono.equals(cld.getChronology()) == false) {
                throw new DateTimeException("ChronoLocalDate 必须使用有效的解析日历系统: " + chrono);
            }
            date = cld;
        }
    }


                //-----------------------------------------------------------------------
    private void resolveTimeFields() {
        // 简化字段
        if (fieldValues.containsKey(CLOCK_HOUR_OF_DAY)) {
            // 宽容模式允许任何值，智能模式允许 0-24，严格模式允许 1-24
            long ch = fieldValues.remove(CLOCK_HOUR_OF_DAY);
            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0)) {
                CLOCK_HOUR_OF_DAY.checkValidValue(ch);
            }
            updateCheckConflict(CLOCK_HOUR_OF_DAY, HOUR_OF_DAY, ch == 24 ? 0 : ch);
        }
        if (fieldValues.containsKey(CLOCK_HOUR_OF_AMPM)) {
            // 宽容模式允许任何值，智能模式允许 0-12，严格模式允许 1-12
            long ch = fieldValues.remove(CLOCK_HOUR_OF_AMPM);
            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0)) {
                CLOCK_HOUR_OF_AMPM.checkValidValue(ch);
            }
            updateCheckConflict(CLOCK_HOUR_OF_AMPM, HOUR_OF_AMPM, ch == 12 ? 0 : ch);
        }
        if (fieldValues.containsKey(AMPM_OF_DAY) && fieldValues.containsKey(HOUR_OF_AMPM)) {
            long ap = fieldValues.remove(AMPM_OF_DAY);
            long hap = fieldValues.remove(HOUR_OF_AMPM);
            if (resolverStyle == ResolverStyle.LENIENT) {
                updateCheckConflict(AMPM_OF_DAY, HOUR_OF_DAY, Math.addExact(Math.multiplyExact(ap, 12), hap));
            } else {  // 严格或智能模式
                AMPM_OF_DAY.checkValidValue(ap);
                HOUR_OF_AMPM.checkValidValue(ap);
                updateCheckConflict(AMPM_OF_DAY, HOUR_OF_DAY, ap * 12 + hap);
            }
        }
        if (fieldValues.containsKey(NANO_OF_DAY)) {
            long nod = fieldValues.remove(NANO_OF_DAY);
            if (resolverStyle != ResolverStyle.LENIENT) {
                NANO_OF_DAY.checkValidValue(nod);
            }
            updateCheckConflict(NANO_OF_DAY, HOUR_OF_DAY, nod / 3600_000_000_000L);
            updateCheckConflict(NANO_OF_DAY, MINUTE_OF_HOUR, (nod / 60_000_000_000L) % 60);
            updateCheckConflict(NANO_OF_DAY, SECOND_OF_MINUTE, (nod / 1_000_000_000L) % 60);
            updateCheckConflict(NANO_OF_DAY, NANO_OF_SECOND, nod % 1_000_000_000L);
        }
        if (fieldValues.containsKey(MICRO_OF_DAY)) {
            long cod = fieldValues.remove(MICRO_OF_DAY);
            if (resolverStyle != ResolverStyle.LENIENT) {
                MICRO_OF_DAY.checkValidValue(cod);
            }
            updateCheckConflict(MICRO_OF_DAY, SECOND_OF_DAY, cod / 1_000_000L);
            updateCheckConflict(MICRO_OF_DAY, MICRO_OF_SECOND, cod % 1_000_000L);
        }
        if (fieldValues.containsKey(MILLI_OF_DAY)) {
            long lod = fieldValues.remove(MILLI_OF_DAY);
            if (resolverStyle != ResolverStyle.LENIENT) {
                MILLI_OF_DAY.checkValidValue(lod);
            }
            updateCheckConflict(MILLI_OF_DAY, SECOND_OF_DAY, lod / 1_000);
            updateCheckConflict(MILLI_OF_DAY, MILLI_OF_SECOND, lod % 1_000);
        }
        if (fieldValues.containsKey(SECOND_OF_DAY)) {
            long sod = fieldValues.remove(SECOND_OF_DAY);
            if (resolverStyle != ResolverStyle.LENIENT) {
                SECOND_OF_DAY.checkValidValue(sod);
            }
            updateCheckConflict(SECOND_OF_DAY, HOUR_OF_DAY, sod / 3600);
            updateCheckConflict(SECOND_OF_DAY, MINUTE_OF_HOUR, (sod / 60) % 60);
            updateCheckConflict(SECOND_OF_DAY, SECOND_OF_MINUTE, sod % 60);
        }
        if (fieldValues.containsKey(MINUTE_OF_DAY)) {
            long mod = fieldValues.remove(MINUTE_OF_DAY);
            if (resolverStyle != ResolverStyle.LENIENT) {
                MINUTE_OF_DAY.checkValidValue(mod);
            }
            updateCheckConflict(MINUTE_OF_DAY, HOUR_OF_DAY, mod / 60);
            updateCheckConflict(MINUTE_OF_DAY, MINUTE_OF_HOUR, mod % 60);
        }

        // 严格合并部分秒字段，将宽容扩展留到后面
        if (fieldValues.containsKey(NANO_OF_SECOND)) {
            long nos = fieldValues.get(NANO_OF_SECOND);
            if (resolverStyle != ResolverStyle.LENIENT) {
                NANO_OF_SECOND.checkValidValue(nos);
            }
            if (fieldValues.containsKey(MICRO_OF_SECOND)) {
                long cos = fieldValues.remove(MICRO_OF_SECOND);
                if (resolverStyle != ResolverStyle.LENIENT) {
                    MICRO_OF_SECOND.checkValidValue(cos);
                }
                nos = cos * 1000 + (nos % 1000);
                updateCheckConflict(MICRO_OF_SECOND, NANO_OF_SECOND, nos);
            }
            if (fieldValues.containsKey(MILLI_OF_SECOND)) {
                long los = fieldValues.remove(MILLI_OF_SECOND);
                if (resolverStyle != ResolverStyle.LENIENT) {
                    MILLI_OF_SECOND.checkValidValue(los);
                }
                updateCheckConflict(MILLI_OF_SECOND, NANO_OF_SECOND, los * 1_000_000L + (nos % 1_000_000L));
            }
        }

        // 如果所有四个字段都可用（优化），则转换为时间
        if (fieldValues.containsKey(HOUR_OF_DAY) && fieldValues.containsKey(MINUTE_OF_HOUR) &&
                fieldValues.containsKey(SECOND_OF_MINUTE) && fieldValues.containsKey(NANO_OF_SECOND)) {
            long hod = fieldValues.remove(HOUR_OF_DAY);
            long moh = fieldValues.remove(MINUTE_OF_HOUR);
            long som = fieldValues.remove(SECOND_OF_MINUTE);
            long nos = fieldValues.remove(NANO_OF_SECOND);
            resolveTime(hod, moh, som, nos);
        }
    }

    private void resolveTimeLenient() {
        // 宽容地从不完整的信息创建时间
        // 在所有其他操作之后完成，因为它从无中创建信息
        // 这会破坏 updateCheckConflict(field)

        if (time == null) {
            // NANO_OF_SECOND 已与 MILLI/MICRO 合并
            if (fieldValues.containsKey(MILLI_OF_SECOND)) {
                long los = fieldValues.remove(MILLI_OF_SECOND);
                if (fieldValues.containsKey(MICRO_OF_SECOND)) {
                    // 合并 milli-of-second 和 micro-of-second 以获得更好的错误消息
                    long cos = los * 1_000 + (fieldValues.get(MICRO_OF_SECOND) % 1_000);
                    updateCheckConflict(MILLI_OF_SECOND, MICRO_OF_SECOND, cos);
                    fieldValues.remove(MICRO_OF_SECOND);
                    fieldValues.put(NANO_OF_SECOND, cos * 1_000L);
                } else {
                    // 将 milli-of-second 转换为 nano-of-second
                    fieldValues.put(NANO_OF_SECOND, los * 1_000_000L);
                }
            } else if (fieldValues.containsKey(MICRO_OF_SECOND)) {
                // 将 micro-of-second 转换为 nano-of-second
                long cos = fieldValues.remove(MICRO_OF_SECOND);
                fieldValues.put(NANO_OF_SECOND, cos * 1_000L);
            }

            // 宽容地合并小时/分钟/秒/纳秒
            Long hod = fieldValues.get(HOUR_OF_DAY);
            if (hod != null) {
                Long moh = fieldValues.get(MINUTE_OF_HOUR);
                Long som = fieldValues.get(SECOND_OF_MINUTE);
                Long nos = fieldValues.get(NANO_OF_SECOND);

                // 检查无法默认的无效组合
                if ((moh == null && (som != null || nos != null)) ||
                        (moh != null && som == null && nos != null)) {
                    return;
                }

                // 必要时进行默认并构建时间
                long mohVal = (moh != null ? moh : 0);
                long somVal = (som != null ? som : 0);
                long nosVal = (nos != null ? nos : 0);
                resolveTime(hod, mohVal, somVal, nosVal);
                fieldValues.remove(HOUR_OF_DAY);
                fieldValues.remove(MINUTE_OF_HOUR);
                fieldValues.remove(SECOND_OF_MINUTE);
                fieldValues.remove(NANO_OF_SECOND);
            }
        }

        // 验证剩余部分
        if (resolverStyle != ResolverStyle.LENIENT && fieldValues.size() > 0) {
            for (Entry<TemporalField, Long> entry : fieldValues.entrySet()) {
                TemporalField field = entry.getKey();
                if (field instanceof ChronoField && field.isTimeBased()) {
                    ((ChronoField) field).checkValidValue(entry.getValue());
                }
            }
        }
    }

    private void resolveTime(long hod, long moh, long som, long nos) {
        if (resolverStyle == ResolverStyle.LENIENT) {
            long totalNanos = Math.multiplyExact(hod, 3600_000_000_000L);
            totalNanos = Math.addExact(totalNanos, Math.multiplyExact(moh, 60_000_000_000L));
            totalNanos = Math.addExact(totalNanos, Math.multiplyExact(som, 1_000_000_000L));
            totalNanos = Math.addExact(totalNanos, nos);
            int excessDays = (int) Math.floorDiv(totalNanos, 86400_000_000_000L);  // 安全的 int 转换
            long nod = Math.floorMod(totalNanos, 86400_000_000_000L);
            updateCheckConflict(LocalTime.ofNanoOfDay(nod), Period.ofDays(excessDays));
        } else {  // 严格或智能模式
            int mohVal = MINUTE_OF_HOUR.checkValidIntValue(moh);
            int nosVal = NANO_OF_SECOND.checkValidIntValue(nos);
            // 处理 24:00 一天结束
            if (resolverStyle == ResolverStyle.SMART && hod == 24 && mohVal == 0 && som == 0 && nosVal == 0) {
                updateCheckConflict(LocalTime.MIDNIGHT, Period.ofDays(1));
            } else {
                int hodVal = HOUR_OF_DAY.checkValidIntValue(hod);
                int somVal = SECOND_OF_MINUTE.checkValidIntValue(som);
                updateCheckConflict(LocalTime.of(hodVal, mohVal, somVal, nosVal), Period.ZERO);
            }
        }
    }

    private void resolvePeriod() {
        // 如果同时有日期和时间，则添加整数天
        if (date != null && time != null && excessDays.isZero() == false) {
            date = date.plus(excessDays);
            excessDays = Period.ZERO;
        }
    }

    private void resolveFractional() {
        // 确保分数秒可用，因为 ChronoField 需要
        // resolveTimeLenient() 将已将 MICRO_OF_SECOND/MILLI_OF_SECOND 合并到 NANO_OF_SECOND
        if (time == null &&
                (fieldValues.containsKey(INSTANT_SECONDS) ||
                    fieldValues.containsKey(SECOND_OF_DAY) ||
                    fieldValues.containsKey(SECOND_OF_MINUTE))) {
            if (fieldValues.containsKey(NANO_OF_SECOND)) {
                long nos = fieldValues.get(NANO_OF_SECOND);
                fieldValues.put(MICRO_OF_SECOND, nos / 1000);
                fieldValues.put(MILLI_OF_SECOND, nos / 1000000);
            } else {
                fieldValues.put(NANO_OF_SECOND, 0L);
                fieldValues.put(MICRO_OF_SECOND, 0L);
                fieldValues.put(MILLI_OF_SECOND, 0L);
            }
        }
    }

    private void resolveInstant() {
        // 如果有日期、时间和时区，则添加即时秒
        if (date != null && time != null) {
            if (zone != null) {
                long instant = date.atTime(time).atZone(zone).getLong(ChronoField.INSTANT_SECONDS);
                fieldValues.put(INSTANT_SECONDS, instant);
            } else {
                Long offsetSecs = fieldValues.get(OFFSET_SECONDS);
                if (offsetSecs != null) {
                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSecs.intValue());
                    long instant = date.atTime(time).atZone(offset).getLong(ChronoField.INSTANT_SECONDS);
                    fieldValues.put(INSTANT_SECONDS, instant);
                }
            }
        }
    }

    private void updateCheckConflict(LocalTime timeToSet, Period periodToSet) {
        if (time != null) {
            if (time.equals(timeToSet) == false) {
                throw new DateTimeException("发现冲突：字段解析为不同的时间: " + time + " " + timeToSet);
            }
            if (excessDays.isZero() == false && periodToSet.isZero() == false && excessDays.equals(periodToSet) == false) {
                throw new DateTimeException("发现冲突：字段解析为不同的多余周期: " + excessDays + " " + periodToSet);
            } else {
                excessDays = periodToSet;
            }
        } else {
            time = timeToSet;
            excessDays = periodToSet;
        }
    }

    //-----------------------------------------------------------------------
    private void crossCheck() {
        // 仅交叉检查日期、时间和日期时间
        // 尽量避免对象创建
        if (date != null) {
            crossCheck(date);
        }
        if (time != null) {
            crossCheck(time);
            if (date != null && fieldValues.size() > 0) {
                crossCheck(date.atTime(time));
            }
        }
    }

    private void crossCheck(TemporalAccessor target) {
        for (Iterator<Entry<TemporalField, Long>> it = fieldValues.entrySet().iterator(); it.hasNext(); ) {
            Entry<TemporalField, Long> entry = it.next();
            TemporalField field = entry.getKey();
            if (target.isSupported(field)) {
                long val1;
                try {
                    val1 = target.getLong(field);
                } catch (RuntimeException ex) {
                    continue;
                }
                long val2 = entry.getValue();
                if (val1 != val2) {
                    throw new DateTimeException("发现冲突：字段 " + field + " " + val1 +
                            " 与 " + field + " " + val2 + " 从 " + target + " 导出的不同");
                }
                it.remove();
            }
        }
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);
        buf.append(fieldValues).append(',').append(chrono);
        if (zone != null) {
            buf.append(',').append(zone);
        }
        if (date != null || time != null) {
            buf.append(" resolved to ");
            if (date != null) {
                buf.append(date);
                if (time != null) {
                    buf.append('T').append(time);
                }
            } else {
                buf.append(time);
            }
        }
        return buf.toString();
    }

}
