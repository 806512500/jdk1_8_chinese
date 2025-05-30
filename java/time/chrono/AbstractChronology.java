
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
package java.time.chrono;

import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.TemporalAdjusters.nextOrSame;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sun.util.logging.PlatformLogger;

/**
 * 日历系统的抽象实现，用于组织和识别日期。
 * <p>
 * 主日期和时间 API 基于 ISO 日历系统。
 * 日历系统在幕后操作，以表示日历系统的通用概念。
 * <p>
 * 有关更多详细信息，请参见 {@link Chronology}。
 *
 * @implSpec
 * 该类与 {@code Chronology} 接口分开，以便静态方法不会被继承。虽然可以直接实现 {@code Chronology}，
 * 但强烈建议扩展此抽象类。
 * <p>
 * 必须仔细实现此类以确保其他类正确运行。所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * 子类应尽可能实现 Serializable。
 *
 * @since 1.8
 */
public abstract class AbstractChronology implements Chronology {

    /**
     * ChronoLocalDate 排序常量。
     */
    static final Comparator<ChronoLocalDate> DATE_ORDER =
        (Comparator<ChronoLocalDate> & Serializable) (date1, date2) -> {
            return Long.compare(date1.toEpochDay(), date2.toEpochDay());
        };
    /**
     * ChronoLocalDateTime 排序常量。
     */
    static final Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> DATE_TIME_ORDER =
        (Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> & Serializable) (dateTime1, dateTime2) -> {
            int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
            if (cmp == 0) {
                cmp = Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
            }
            return cmp;
        };
    /**
     * ChronoZonedDateTime 排序常量。
     */
    static final Comparator<ChronoZonedDateTime<?>> INSTANT_ORDER =
            (Comparator<ChronoZonedDateTime<?>> & Serializable) (dateTime1, dateTime2) -> {
                int cmp = Long.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond());
                if (cmp == 0) {
                    cmp = Long.compare(dateTime1.toLocalTime().getNano(), dateTime2.toLocalTime().getNano());
                }
                return cmp;
            };

    /**
     * 按 ID 可用的日历映射。
     */
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_ID = new ConcurrentHashMap<>();
    /**
     * 按日历类型可用的日历映射。
     */
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_TYPE = new ConcurrentHashMap<>();

    /**
     * 通过其 ID 和类型注册一个 Chronology，以便通过 {@link #of(String)} 查找。
     * 必须在完全构造后注册日历系统。
     * 特别是在 Chronology 的构造函数中不能注册。
     *
     * @param chrono 要注册的日历系统；不为 null
     * @return 已注册的日历系统（如果有），可能为 null
     */
    static Chronology registerChrono(Chronology chrono) {
        return registerChrono(chrono, chrono.getId());
    }

    /**
     * 通过 ID 和类型注册一个 Chronology，以便通过 {@link #of(String)} 查找。
     * 必须在完全构造后注册日历系统。
     * 特别是在 Chronology 的构造函数中不能注册。
     *
     * @param chrono 要注册的日历系统；不为 null
     * @param id 要注册的日历系统 ID；不为 null
     * @return 已注册的日历系统（如果有），可能为 null
     */
    static Chronology registerChrono(Chronology chrono, String id) {
        Chronology prev = CHRONOS_BY_ID.putIfAbsent(id, chrono);
        if (prev == null) {
            String type = chrono.getCalendarType();
            if (type != null) {
                CHRONOS_BY_TYPE.putIfAbsent(type, chrono);
            }
        }
        return prev;
    }

    /**
     * 从 ID 和类型到 Chronology 的映射初始化。
     * 使用 ServiceLoader 查找并注册 bootclass 加载器中找到的 {@link java.time.chrono.AbstractChronology} 实现。
     * 内置的日历系统显式注册。
     * 通过线程的上下文类加载器配置的日历仅限于该线程，将被忽略。
     * <p>
     * 初始化仅使用 IsoChronology 的注册作为测试和最终步骤完成一次。
     * 多个线程可以并发执行初始化。
     * 只有每个 Chronology 的第一次注册由 ConcurrentHashMap 保留。
     * @return 如果缓存已初始化，则返回 true
     */
    private static boolean initCache() {
        if (CHRONOS_BY_ID.get("ISO") == null) {
            // 初始化未完成

            // 注册内置日历系统
            registerChrono(HijrahChronology.INSTANCE);
            registerChrono(JapaneseChronology.INSTANCE);
            registerChrono(MinguoChronology.INSTANCE);
            registerChrono(ThaiBuddhistChronology.INSTANCE);

            // 注册 ServiceLoader 中的日历系统
            @SuppressWarnings("rawtypes")
            ServiceLoader<AbstractChronology> loader =  ServiceLoader.load(AbstractChronology.class, null);
            for (AbstractChronology chrono : loader) {
                String id = chrono.getId();
                if (id.equals("ISO") || registerChrono(chrono) != null) {
                    // 记录尝试替换现有日历系统的日志
                    PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
                    logger.warning("Ignoring duplicate Chronology, from ServiceLoader configuration "  + id);
                }
            }

            // 最后，注册 IsoChronology 以标记初始化完成
            registerChrono(IsoChronology.INSTANCE);
            return true;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /**
     * 从区域设置获取 {@code Chronology} 的实例。
     * <p>
     * 请参见 {@link Chronology#ofLocale(Locale)}。
     *
     * @param locale 用于获取日历系统的区域设置，不为 null
     * @return 与区域设置关联的日历系统，不为 null
     * @throws java.time.DateTimeException 如果找不到区域设置指定的日历系统
     */
    static Chronology ofLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        String type = locale.getUnicodeLocaleType("ca");
        if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
            return IsoChronology.INSTANCE;
        }
        // 未预定义；按类型查找
        do {
            Chronology chrono = CHRONOS_BY_TYPE.get(type);
            if (chrono != null) {
                return chrono;
            }
            // 如果未找到，则执行初始化（一次）并重复查找
        } while (initCache());

        // 使用线程上下文类加载器的 ServiceLoader 查找日历系统
        // 应用程序提供的日历系统不得缓存
        @SuppressWarnings("rawtypes")
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            if (type.equals(chrono.getCalendarType())) {
                return chrono;
            }
        }
        throw new DateTimeException("Unknown calendar system: " + type);
    }

    //-----------------------------------------------------------------------
    /**
     * 从日历系统 ID 或类型获取 {@code Chronology} 的实例。
     * <p>
     * 请参见 {@link Chronology#of(String)}。
     *
     * @param id 日历系统 ID 或类型，不为 null
     * @return 请求标识符的日历系统，不为 null
     * @throws java.time.DateTimeException 如果找不到日历系统
     */
    static Chronology of(String id) {
        Objects.requireNonNull(id, "id");
        do {
            Chronology chrono = of0(id);
            if (chrono != null) {
                return chrono;
            }
            // 如果未找到，则执行初始化（一次）并重复查找
        } while (initCache());

        // 使用线程上下文类加载器的 ServiceLoader 查找日历系统
        // 应用程序提供的日历系统不得缓存
        @SuppressWarnings("rawtypes")
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            if (id.equals(chrono.getId()) || id.equals(chrono.getCalendarType())) {
                return chrono;
            }
        }
        throw new DateTimeException("Unknown chronology: " + id);
    }

    /**
     * 从日历系统 ID 或类型获取 {@code Chronology} 的实例。
     *
     * @param id 日历系统 ID 或类型，不为 null
     * @return 请求标识符的日历系统，或未找到时返回 null
     */
    private static Chronology of0(String id) {
        Chronology chrono = CHRONOS_BY_ID.get(id);
        if (chrono == null) {
            chrono = CHRONOS_BY_TYPE.get(id);
        }
        return chrono;
    }

    /**
     * 返回可用的日历系统。
     * <p>
     * 每个返回的 {@code Chronology} 都可用于系统。
     * 日历系统集包括系统日历系统和通过 ServiceLoader 配置提供的应用程序日历系统。
     *
     * @return 可用日历系统 ID 的独立、可修改集，不为 null
     */
    static Set<Chronology> getAvailableChronologies() {
        initCache();       // 强制初始化
        HashSet<Chronology> chronos = new HashSet<>(CHRONOS_BY_ID.values());

        /// 添加 ServiceLoader 配置中的日历系统
        @SuppressWarnings("rawtypes")
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            chronos.add(chrono);
        }
        return chronos;
    }

    //-----------------------------------------------------------------------
    /**
     * 创建实例。
     */
    protected AbstractChronology() {
    }

    //-----------------------------------------------------------------------
    /**
     * 在解析过程中将解析的 {@code ChronoField} 值解析为日期。
     * <p>
     * 大多数 {@code TemporalField} 实现使用字段上的解析方法进行解析。相比之下，{@code ChronoField} 类
     * 定义了仅相对于日历系统才有意义的字段。因此，{@code ChronoField} 日期字段在此处特定日历系统的上下文中解析。
     * <p>
     * 由该方法解析 {@code ChronoField} 实例，该方法可以在子类中重写。
     * <ul>
     * <li>{@code EPOCH_DAY} - 如果存在，则转换为日期，并且所有其他日期字段都与日期进行交叉检查。
     * <li>{@code PROLEPTIC_MONTH} - 如果存在，则拆分为 {@code YEAR} 和 {@code MONTH_OF_YEAR}。如果模式为严格或智能，
     *  则验证该字段。
     * <li>{@code YEAR_OF_ERA} 和 {@code ERA} - 如果两者都存在，则组合形成一个 {@code YEAR}。在宽松模式下，不验证 {@code YEAR_OF_ERA} 范围，
     *  在智能和严格模式下验证。在所有三种模式下验证 {@code ERA} 范围。如果仅存在 {@code YEAR_OF_ERA}，且模式为智能或宽松，
     *  则假定最后一个可用的纪元。在严格模式下，不假定任何纪元，并且 {@code YEAR_OF_ERA} 保持不变。如果仅存在 {@code ERA}，则保持不变。
     * <li>{@code YEAR}、{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} -
     *  如果都存在，则组合形成一个日期。在所有三种模式下验证 {@code YEAR}。如果模式为智能或严格，则验证月份和日期。
     *  如果模式为宽松，则以相当于在请求年的第一个月的第一天创建日期，然后添加月份差，然后添加日期差的方式组合日期。
     *  如果模式为智能，且日期大于年月的最大值，则将日期调整为该年月的最后一天。如果模式为严格，则必须形成一个有效的日期。
     * <li>{@code YEAR} 和 {@code DAY_OF_YEAR} -
     *  如果两者都存在，则组合形成一个日期。在所有三种模式下验证 {@code YEAR}。如果模式为宽松，则以相当于在请求年的第一天创建日期，
     *  然后添加日期差的方式组合日期。如果模式为智能或严格，则必须形成一个有效的日期。
     * <li>{@code YEAR}、{@code MONTH_OF_YEAR}、{@code ALIGNED_WEEK_OF_MONTH} 和 {@code ALIGNED_DAY_OF_WEEK_IN_MONTH} -
     *  如果都存在，则组合形成一个日期。在所有三种模式下验证 {@code YEAR}。如果模式为宽松，则以相当于在请求年的第一个月的第一天创建日期，
     *  然后添加月份差，然后添加周数差，然后添加日期差的方式组合日期。如果模式为智能或严格，则验证所有四个字段的外部范围。
     *  日期以相当于在请求年的第一个月的第一天创建日期，然后添加周数和日期差以达到其值的方式组合。如果模式为严格，则日期还验证调整日期和周数
     *  是否未改变月份。
     * <li>{@code YEAR}、{@code MONTH_OF_YEAR}、{@code ALIGNED_WEEK_OF_MONTH} 和 {@code DAY_OF_WEEK} -
     *  如果都存在，则组合形成一个日期。方法与上述 {@code ALIGNED_DAY_OF_WEEK_IN_MONTH} 中描述的年、月和周相同。
     *  日期在处理年、月和周后，将调整为下一个或相同的匹配日期。
     * <li>{@code YEAR}、{@code ALIGNED_WEEK_OF_YEAR} 和 {@code ALIGNED_DAY_OF_WEEK_IN_YEAR} -
     *  如果都存在，则组合形成一个日期。在所有三种模式下验证 {@code YEAR}。如果模式为宽松，则以相当于在请求年的第一天创建日期，
     *  然后添加周数差，然后添加日期差的方式组合日期。如果模式为智能或严格，则验证所有三个字段的外部范围。
     *  日期以相当于在请求年的第一天创建日期，然后添加周数和日期差以达到其值的方式组合。如果模式为严格，则日期还验证调整日期和周数
     *  是否未改变年份。
     * <li>{@code YEAR}、{@code ALIGNED_WEEK_OF_YEAR} 和 {@code DAY_OF_WEEK} -
     *  如果都存在，则组合形成一个日期。方法与上述 {@code ALIGNED_DAY_OF_WEEK_IN_YEAR} 中描述的年和周相同。
     *  日期在处理年和周后，将调整为下一个或相同的匹配日期。
     * </ul>
     * <p>
     * 默认实现适用于大多数日历系统。如果找到 {@link java.time.temporal.ChronoField#YEAR_OF_ERA} 而没有 {@link java.time.temporal.ChronoField#ERA}，
     * 则使用 {@link #eras()} 中的最后一个纪元。实现假设一周有 7 天，月份的第一天的值为 1，年份的第一天的值为 1，并且月份和年份的第一天总是存在。
     *
     * @param fieldValues 字段到值的映射，可以更新，不为 null
     * @param resolverStyle 请求的解析类型，不为 null
     * @return 解析的日期，如果信息不足以创建日期则返回 null
     * @throws java.time.DateTimeException 如果日期无法解析，通常是因为输入数据冲突
     */
    @Override
    public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        // 在创建纪元之前检查 epoch-day
        if (fieldValues.containsKey(EPOCH_DAY)) {
            return dateEpochDay(fieldValues.remove(EPOCH_DAY));
        }


                    // 修正公历月份
        resolveProlepticMonth(fieldValues, resolverStyle);

        // 如有必要，创建纪元以解析年份
        ChronoLocalDate resolved = resolveYearOfEra(fieldValues, resolverStyle);
        if (resolved != null) {
            return resolved;
        }

        // 构建日期
        if (fieldValues.containsKey(YEAR)) {
            if (fieldValues.containsKey(MONTH_OF_YEAR)) {
                if (fieldValues.containsKey(DAY_OF_MONTH)) {
                    return resolveYMD(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(ALIGNED_WEEK_OF_MONTH)) {
                    if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
                        return resolveYMAA(fieldValues, resolverStyle);
                    }
                    if (fieldValues.containsKey(DAY_OF_WEEK)) {
                        return resolveYMAD(fieldValues, resolverStyle);
                    }
                }
            }
            if (fieldValues.containsKey(DAY_OF_YEAR)) {
                return resolveYD(fieldValues, resolverStyle);
            }
            if (fieldValues.containsKey(ALIGNED_WEEK_OF_YEAR)) {
                if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
                    return resolveYAA(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(DAY_OF_WEEK)) {
                    return resolveYAD(fieldValues, resolverStyle);
                }
            }
        }
        return null;
    }

    void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = fieldValues.remove(PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                PROLEPTIC_MONTH.checkValidValue(pMonth);
            }
            // 第一天可能是设置公历月份最安全的方法
            // 不能添加到零年，因为不是所有历法都有零年
            ChronoLocalDate chronoDate = dateNow()
                    .with(DAY_OF_MONTH, 1).with(PROLEPTIC_MONTH, pMonth);
            addFieldValue(fieldValues, MONTH_OF_YEAR, chronoDate.get(MONTH_OF_YEAR));
            addFieldValue(fieldValues, YEAR, chronoDate.get(YEAR));
        }
    }

    ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long yoeLong = fieldValues.remove(YEAR_OF_ERA);
        if (yoeLong != null) {
            Long eraLong = fieldValues.remove(ERA);
            int yoe;
            if (resolverStyle != ResolverStyle.LENIENT) {
                yoe = range(YEAR_OF_ERA).checkValidIntValue(yoeLong, YEAR_OF_ERA);
            } else {
                yoe = Math.toIntExact(yoeLong);
            }
            if (eraLong != null) {
                Era eraObj = eraOf(range(ERA).checkValidIntValue(eraLong, ERA));
                addFieldValue(fieldValues, YEAR, prolepticYear(eraObj, yoe));
            } else {
                if (fieldValues.containsKey(YEAR)) {
                    int year = range(YEAR).checkValidIntValue(fieldValues.get(YEAR), YEAR);
                    ChronoLocalDate chronoDate = dateYearDay(year, 1);
                    addFieldValue(fieldValues, YEAR, prolepticYear(chronoDate.getEra(), yoe));
                } else if (resolverStyle == ResolverStyle.STRICT) {
                    // 如果是严格模式，则不创建纪元
                    // 恢复之前移除的字段，没有交叉检查问题
                    fieldValues.put(YEAR_OF_ERA, yoeLong);
                } else {
                    List<Era> eras = eras();
                    if (eras.isEmpty()) {
                        addFieldValue(fieldValues, YEAR, yoe);
                    } else {
                        Era eraObj = eras.get(eras.size() - 1);
                        addFieldValue(fieldValues, YEAR, prolepticYear(eraObj, yoe));
                    }
                }
            }
        } else if (fieldValues.containsKey(ERA)) {
            range(ERA).checkValidValue(fieldValues.get(ERA), ERA);  // 始终验证
        }
        return null;
    }

    ChronoLocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
            long days = Math.subtractExact(fieldValues.remove(DAY_OF_MONTH), 1);
            return date(y, 1, 1).plus(months, MONTHS).plus(days, DAYS);
        }
        int moy = range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR), MONTH_OF_YEAR);
        ValueRange domRange = range(DAY_OF_MONTH);
        int dom = domRange.checkValidIntValue(fieldValues.remove(DAY_OF_MONTH), DAY_OF_MONTH);
        if (resolverStyle == ResolverStyle.SMART) {  // 之前的有效日期
            try {
                return date(y, moy, dom);
            } catch (DateTimeException ex) {
                return date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
            }
        }
        return date(y, moy, dom);
    }

    ChronoLocalDate resolveYD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long days = Math.subtractExact(fieldValues.remove(DAY_OF_YEAR), 1);
            return dateYearDay(y, 1).plus(days, DAYS);
        }
        int doy = range(DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(DAY_OF_YEAR), DAY_OF_YEAR);
        return dateYearDay(y, doy);  // 智能模式与严格模式相同
    }

    ChronoLocalDate resolveYMAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
            long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1);
            long days = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH), 1);
            return date(y, 1, 1).plus(months, MONTHS).plus(weeks, WEEKS).plus(days, DAYS);
        }
        int moy = range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR), MONTH_OF_YEAR);
        int aw = range(ALIGNED_WEEK_OF_MONTH).checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), ALIGNED_WEEK_OF_MONTH);
        int ad = range(ALIGNED_DAY_OF_WEEK_IN_MONTH).checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH), ALIGNED_DAY_OF_WEEK_IN_MONTH);
        ChronoLocalDate date = date(y, moy, 1).plus((aw - 1) * 7 + (ad - 1), DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.get(MONTH_OF_YEAR) != moy) {
            throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的月份");
        }
        return date;
    }

    ChronoLocalDate resolveYMAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
            long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1);
            long dow = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1);
            return resolveAligned(date(y, 1, 1), months, weeks, dow);
        }
        int moy = range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR), MONTH_OF_YEAR);
        int aw = range(ALIGNED_WEEK_OF_MONTH).checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), ALIGNED_WEEK_OF_MONTH);
        int dow = range(DAY_OF_WEEK).checkValidIntValue(fieldValues.remove(DAY_OF_WEEK), DAY_OF_WEEK);
        ChronoLocalDate date = date(y, moy, 1).plus((aw - 1) * 7, DAYS).with(nextOrSame(DayOfWeek.of(dow)));
        if (resolverStyle == ResolverStyle.STRICT && date.get(MONTH_OF_YEAR) != moy) {
            throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的月份");
        }
        return date;
    }

    ChronoLocalDate resolveYAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1);
            long days = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR), 1);
            return dateYearDay(y, 1).plus(weeks, WEEKS).plus(days, DAYS);
        }
        int aw = range(ALIGNED_WEEK_OF_YEAR).checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), ALIGNED_WEEK_OF_YEAR);
        int ad = range(ALIGNED_DAY_OF_WEEK_IN_YEAR).checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR), ALIGNED_DAY_OF_WEEK_IN_YEAR);
        ChronoLocalDate date = dateYearDay(y, 1).plus((aw - 1) * 7 + (ad - 1), DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.get(YEAR) != y) {
            throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的年份");
        }
        return date;
    }

    ChronoLocalDate resolveYAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(YEAR).checkValidIntValue(fieldValues.remove(YEAR), YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1);
            long dow = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1);
            return resolveAligned(dateYearDay(y, 1), 0, weeks, dow);
        }
        int aw = range(ALIGNED_WEEK_OF_YEAR).checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), ALIGNED_WEEK_OF_YEAR);
        int dow = range(DAY_OF_WEEK).checkValidIntValue(fieldValues.remove(DAY_OF_WEEK), DAY_OF_WEEK);
        ChronoLocalDate date = dateYearDay(y, 1).plus((aw - 1) * 7, DAYS).with(nextOrSame(DayOfWeek.of(dow)));
        if (resolverStyle == ResolverStyle.STRICT && date.get(YEAR) != y) {
            throw new DateTimeException("严格模式拒绝解析的日期，因为它在不同的年份");
        }
        return date;
    }

    ChronoLocalDate resolveAligned(ChronoLocalDate base, long months, long weeks, long dow) {
        ChronoLocalDate date = base.plus(months, MONTHS).plus(weeks, WEEKS);
        if (dow > 7) {
            date = date.plus((dow - 1) / 7, WEEKS);
            dow = ((dow - 1) % 7) + 1;
        } else if (dow < 1) {
            date = date.plus(Math.subtractExact(dow,  7) / 7, WEEKS);
            dow = ((dow + 6) % 7) + 1;
        }
        return date.with(nextOrSame(DayOfWeek.of((int) dow)));
    }

    /**
     * 向映射中添加字段值对，检查冲突。
     * <p>
     * 如果字段尚未存在，则将字段值对添加到映射中。
     * 如果字段已存在且值与指定值相同，则不采取任何操作。
     * 如果字段已存在且值与指定值不同，则抛出异常。
     *
     * @param field  要添加的字段，不为空
     * @param value  要添加的值，不为空
     * @throws java.time.DateTimeException 如果字段已存在且值不同
     */
    void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
        Long old = fieldValues.get(field);  // 首先检查以获得更好的错误消息
        if (old != null && old.longValue() != value) {
            throw new DateTimeException("发现冲突: " + field + " " + old + " 与 " + field + " " + value + " 不同");
        }
        fieldValues.put(field, value);
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此历法与其他历法。
     * <p>
     * 比较顺序首先按历法 ID 字符串，然后按子类的任何附加信息。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义的。
     *
     * @implSpec
     * 此实现比较历法 ID。
     * 子类必须比较它们存储的任何附加状态。
     *
     * @param other  要比较的其他历法，不为空
     * @return 比较值，负数表示较小，正数表示较大
     */
    @Override
    public int compareTo(Chronology other) {
        return getId().compareTo(other.getId());
    }

    /**
     * 检查此历法是否等于另一个历法。
     * <p>
     * 比较基于对象的整个状态。
     *
     * @implSpec
     * 此实现检查类型并调用
     * {@link #compareTo(java.time.chrono.Chronology)}。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此历法等于其他历法，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof AbstractChronology) {
            return compareTo((AbstractChronology) obj) == 0;
        }
        return false;
    }

    /**
     * 此历法的哈希码。
     * <p>
     * 哈希码应基于对象的整个状态。
     *
     * @implSpec
     * 此实现基于历法 ID 和类。
     * 子类应添加它们存储的任何附加状态。
     *
     * @return 适当的哈希码
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 以 {@code String} 形式输出此历法，使用历法 ID。
     *
     * @return 此历法的字符串表示，不为空
     */
    @Override
    public String toString() {
        return getId();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a>
     * 写入历法。
     * <pre>
     *  out.writeByte(1);  // 标识此为历法
     *  out.writeUTF(getId());
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    Object writeReplace() {
        return new Ser(Ser.CHRONO_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws java.io.InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws ObjectStreamException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(getId());
    }

    static Chronology readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return Chronology.of(id);
    }

}
