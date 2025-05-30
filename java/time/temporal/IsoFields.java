
/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.time.temporal;

import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

/**
 * ISO-8601 日历系统特定的字段和单位，包括季度和基于周的年。
 * <p>
 * 本类定义了 ISO 日历系统特定的字段和单位。
 *
 * <h3>季度</h3>
 * ISO-8601 标准基于标准的 12 个月的年份。
 * 通常将其分为四个季度，通常缩写为 Q1、Q2、Q3 和 Q4。
 * <p>
 * 1 月、2 月和 3 月属于 Q1。
 * 4 月、5 月和 6 月属于 Q2。
 * 7 月、8 月和 9 月属于 Q3。
 * 10 月、11 月和 12 月属于 Q4。
 * <p>
 * 完整的日期使用三个字段表示：
 * <ul>
 * <li>{@link #DAY_OF_QUARTER DAY_OF_QUARTER} - 季度内的天数，从 1 到 90、91 或 92
 * <li>{@link #QUARTER_OF_YEAR QUARTER_OF_YEAR} - 基于周的年内的周数
 * <li>{@link ChronoField#YEAR YEAR} - 标准的 ISO 年份
 * </ul>
 *
 * <h3>基于周的年</h3>
 * ISO-8601 标准最初是作为数据交换格式设计的，定义了日期和时间的字符串格式。然而，它还定义了一种基于周的年概念的日期表示方法。
 * <p>
 * 日期使用三个字段表示：
 * <ul>
 * <li>{@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK} - 标准字段，定义从周一（1）到周日（7）的星期几
 * <li>{@link #WEEK_OF_WEEK_BASED_YEAR} - 基于周的年内的周数
 * <li>{@link #WEEK_BASED_YEAR WEEK_BASED_YEAR} - 基于周的年份
 * </ul>
 * 基于周的年份本身是相对于标准的 ISO 历年定义的。
 * 它与标准年份的不同之处在于，它总是从周一开始。
 * <p>
 * 基于周的年的第一周是标准 ISO 年中第一个至少有 4 天在新的一年中的周一那一周。
 * <ul>
 * <li>如果 1 月 1 日是周一，则第 1 周从 1 月 1 日开始
 * <li>如果 1 月 1 日是周二，则第 1 周从上一年 12 月 31 日开始
 * <li>如果 1 月 1 日是周三，则第 1 周从上一年 12 月 30 日开始
 * <li>如果 1 月 1 日是周四，则第 1 周从上一年 12 月 29 日开始
 * <li>如果 1 月 1 日是周五，则第 1 周从 1 月 4 日开始
 * <li>如果 1 月 1 日是周六，则第 1 周从 1 月 3 日开始
 * <li>如果 1 月 1 日是周日，则第 1 周从 1 月 2 日开始
 * </ul>
 * 大多数基于周的年有 52 周，但偶尔会有 53 周。
 * <p>
 * 例如：
 *
 * <table cellpadding="0" cellspacing="3" border="0" style="text-align: left; width: 50%;">
 * <caption>基于周的年份示例</caption>
 * <tr><th>日期</th><th>星期几</th><th>字段值</th></tr>
 * <tr><th>2008-12-28</th><td>周日</td><td>基于周的年 2008 的第 52 周</td></tr>
 * <tr><th>2008-12-29</th><td>周一</td><td>基于周的年 2009 的第 1 周</td></tr>
 * <tr><th>2008-12-31</th><td>周三</td><td>基于周的年 2009 的第 1 周</td></tr>
 * <tr><th>2009-01-01</th><td>周四</td><td>基于周的年 2009 的第 1 周</td></tr>
 * <tr><th>2009-01-04</th><td>周日</td><td>基于周的年 2009 的第 1 周</td></tr>
 * <tr><th>2009-01-05</th><td>周一</td><td>基于周的年 2009 的第 2 周</td></tr>
 * </table>
 *
 * @implSpec
 * <p>
 * 本类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class IsoFields {

    /**
     * 表示季度内天数的字段。
     * <p>
     * 该字段允许查询和设置季度内天数。
     * 季度内天数在标准年份的 Q1 从 1 到 90，在闰年的 Q1 从 1 到 91，在 Q2 从 1 到 91，在 Q3 和 Q4 从 1 到 92。
     * <p>
     * 只有在提供了年份、月份和一年中的天数时，才能计算季度内天数。
     * <p>
     * 设置此字段时，允许部分宽松，取值范围为 1 到 92。如果季度天数少于 92 天，则第 92 天（可能还有第 91 天）将属于下一个季度。
     * <p>
     * 在解析的解析阶段，可以从年份、季度和季度内天数创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有三个字段都会验证其有效值范围。季度内天数字段将根据年份和季度验证为 1 到 90、91 或 92。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有三个字段都会验证其有效值范围。季度内天数字段将验证为 1 到 92，忽略实际的年份和季度范围。如果季度内天数超出实际范围一天，则结果日期将晚一天。如果季度内天数超出实际范围两天，则结果日期将晚两天。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，只有年份会验证其有效值范围。结果日期的计算等同于以下三阶段方法。首先，在请求的年份的 1 月 1 日创建一个日期。然后取季度，减去 1，将季度数加到日期上。最后，取季度内天数，减去 1，将天数加到日期上。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalField DAY_OF_QUARTER = Field.DAY_OF_QUARTER;
    /**
     * 表示季度的字段。
     * <p>
     * 该字段允许查询和设置季度。
     * 季度的值从 1 到 4。
     * <p>
     * 只有在提供了月份时，才能计算季度。
     * <p>
     * 在解析的解析阶段，可以从年份、季度和季度内天数创建日期。
     * 详情见 {@link #DAY_OF_QUARTER}。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalField QUARTER_OF_YEAR = Field.QUARTER_OF_YEAR;
    /**
     * 表示基于周的年内周数的字段。
     * <p>
     * 该字段允许查询和设置基于周的年内周数。
     * 基于周的年内周数的值从 1 到 52，或 53（如果基于周的年有 53 周）。
     * <p>
     * 在解析的解析阶段，可以从基于周的年、基于周的年内周数和星期几创建日期。
     * <p>
     * 在 {@linkplain ResolverStyle#STRICT 严格模式} 下，所有三个字段都会验证其有效值范围。基于周的年内周数字段将根据基于周的年验证为 1 到 52 或 53。
     * <p>
     * 在 {@linkplain ResolverStyle#SMART 智能模式} 下，所有三个字段都会验证其有效值范围。基于周的年内周数字段将验证为 1 到 53，忽略基于周的年。如果基于周的年内周数为 53，但基于周的年只有 52 周，则结果日期将在下一个基于周的年的第 1 周。
     * <p>
     * 在 {@linkplain ResolverStyle#LENIENT 宽松模式} 下，只有基于周的年会验证其有效值范围。如果星期几超出 1 到 7 的范围，则结果日期将通过适当周数的调整，使星期几回到 1 到 7 的范围。如果基于周的年内周数超出 1 到 52 的范围，则任何多余的周数将加到或减去结果日期。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalField WEEK_OF_WEEK_BASED_YEAR = Field.WEEK_OF_WEEK_BASED_YEAR;
    /**
     * 表示基于周的年的字段。
     * <p>
     * 该字段允许查询和设置基于周的年。
     * <p>
     * 该字段的范围与 {@link LocalDate#MAX} 和 {@link LocalDate#MIN} 匹配。
     * <p>
     * 在解析的解析阶段，可以从基于周的年、基于周的年内周数和星期几创建日期。
     * 详情见 {@link #WEEK_OF_WEEK_BASED_YEAR}。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalField WEEK_BASED_YEAR = Field.WEEK_BASED_YEAR;
    /**
     * 用于加减基于周的年的单位。
     * <p>
     * 该单位允许将基于周的年数加到或从日期中减去。
     * 该单位等于 52 或 53 周。
     * 基于周的年的估计持续时间与标准 ISO 年的 {@code 365.2425 天} 相同。
     * <p>
     * 加法规则是将基于周的年数加到现有的基于周的年字段值上。如果结果的基于周的年只有 52 周，则日期将在下一个基于周的年的第 1 周。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalUnit WEEK_BASED_YEARS = Unit.WEEK_BASED_YEARS;
    /**
     * 表示季度年的单位。
     * 对于 ISO 日历系统，它等于 3 个月。
     * 季度年的估计持续时间是一年 {@code 365.2425 天} 的四分之一。
     * <p>
     * 该单位是一个不可变且线程安全的单例。
     */
    public static final TemporalUnit QUARTER_YEARS = Unit.QUARTER_YEARS;

    /**
     * 受限构造函数。
     */
    private IsoFields() {
        throw new AssertionError("不可实例化");
    }

    //-----------------------------------------------------------------------
    /**
     * 字段的实现。
     */
    private static enum Field implements TemporalField {
        DAY_OF_QUARTER {
            @Override
            public TemporalUnit getBaseUnit() {
                return DAYS;
            }
            @Override
            public TemporalUnit getRangeUnit() {
                return QUARTER_YEARS;
            }
            @Override
            public ValueRange range() {
                return ValueRange.of(1, 90, 92);
            }
            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(DAY_OF_YEAR) && temporal.isSupported(MONTH_OF_YEAR) &&
                        temporal.isSupported(YEAR) && isIso(temporal);
            }
            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: DayOfQuarter");
                }
                long qoy = temporal.getLong(QUARTER_OF_YEAR);
                if (qoy == 1) {
                    long year = temporal.getLong(YEAR);
                    return (IsoChronology.INSTANCE.isLeapYear(year) ? ValueRange.of(1, 91) : ValueRange.of(1, 90));
                } else if (qoy == 2) {
                    return ValueRange.of(1, 91);
                } else if (qoy == 3 || qoy == 4) {
                    return ValueRange.of(1, 92);
                } // else value not from 1 to 4, so drop through
                return range();
            }
            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: DayOfQuarter");
                }
                int doy = temporal.get(DAY_OF_YEAR);
                int moy = temporal.get(MONTH_OF_YEAR);
                long year = temporal.getLong(YEAR);
                return doy - QUARTER_DAYS[((moy - 1) / 3) + (IsoChronology.INSTANCE.isLeapYear(year) ? 4 : 0)];
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                // calls getFrom() to check if supported
                long curValue = getFrom(temporal);
                range().checkValidValue(newValue, this);  // leniently check from 1 to 92 TODO: check
                return (R) temporal.with(DAY_OF_YEAR, temporal.getLong(DAY_OF_YEAR) + (newValue - curValue));
            }
            @Override
            public ChronoLocalDate resolve(
                    Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                Long yearLong = fieldValues.get(YEAR);
                Long qoyLong = fieldValues.get(QUARTER_OF_YEAR);
                if (yearLong == null || qoyLong == null) {
                    return null;
                }
                int y = YEAR.checkValidIntValue(yearLong);  // always validate
                long doq = fieldValues.get(DAY_OF_QUARTER);
                ensureIso(partialTemporal);
                LocalDate date;
                if (resolverStyle == ResolverStyle.LENIENT) {
                    date = LocalDate.of(y, 1, 1).plusMonths(Math.multiplyExact(Math.subtractExact(qoyLong, 1), 3));
                    doq = Math.subtractExact(doq, 1);
                } else {
                    int qoy = QUARTER_OF_YEAR.range().checkValidIntValue(qoyLong, QUARTER_OF_YEAR);  // validated
                    date = LocalDate.of(y, ((qoy - 1) * 3) + 1, 1);
                    if (doq < 1 || doq > 90) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            rangeRefinedBy(date).checkValidValue(doq, this);  // only allow exact range
                        } else {  // SMART
                            range().checkValidValue(doq, this);  // allow 1-92 rolling into next quarter
                        }
                    }
                    doq--;
                }
                fieldValues.remove(this);
                fieldValues.remove(YEAR);
                fieldValues.remove(QUARTER_OF_YEAR);
                return date.plusDays(doq);
            }
            @Override
            public String toString() {
                return "DayOfQuarter";
            }
        },
        QUARTER_OF_YEAR {
            @Override
            public TemporalUnit getBaseUnit() {
                return QUARTER_YEARS;
            }
            @Override
            public TemporalUnit getRangeUnit() {
                return YEARS;
            }
            @Override
            public ValueRange range() {
                return ValueRange.of(1, 4);
            }
            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(MONTH_OF_YEAR) && isIso(temporal);
            }
            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: QuarterOfYear");
                }
                long moy = temporal.getLong(MONTH_OF_YEAR);
                return ((moy + 2) / 3);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                // calls getFrom() to check if supported
                long curValue = getFrom(temporal);
                range().checkValidValue(newValue, this);  // strictly check from 1 to 4
                return (R) temporal.with(MONTH_OF_YEAR, temporal.getLong(MONTH_OF_YEAR) + (newValue - curValue) * 3);
            }
            @Override
            public String toString() {
                return "QuarterOfYear";
            }
        },
        WEEK_OF_WEEK_BASED_YEAR {
            @Override
            public String getDisplayName(Locale locale) {
                Objects.requireNonNull(locale, "locale");
                LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                                            .getLocaleResources(locale);
                ResourceBundle rb = lr.getJavaTimeFormatData();
                return rb.containsKey("field.week") ? rb.getString("field.week") : toString();
            }


                        @Override
            public TemporalUnit getBaseUnit() {
                return WEEKS;
            }
            @Override
            public TemporalUnit getRangeUnit() {
                return WEEK_BASED_YEARS;
            }
            @Override
            public ValueRange range() {
                return ValueRange.of(1, 52, 53);
            }
            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(EPOCH_DAY) && isIso(temporal);
            }
            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: WeekOfWeekBasedYear");
                }
                return getWeekRange(LocalDate.from(temporal));
            }
            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: WeekOfWeekBasedYear");
                }
                return getWeek(LocalDate.from(temporal));
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                // 调用 getFrom() 检查是否支持
                range().checkValidValue(newValue, this);  // 宽容范围
                return (R) temporal.plus(Math.subtractExact(newValue, getFrom(temporal)), WEEKS);
            }
            @Override
            public ChronoLocalDate resolve(
                    Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                Long wbyLong = fieldValues.get(WEEK_BASED_YEAR);
                Long dowLong = fieldValues.get(DAY_OF_WEEK);
                if (wbyLong == null || dowLong == null) {
                    return null;
                }
                int wby = WEEK_BASED_YEAR.range().checkValidIntValue(wbyLong, WEEK_BASED_YEAR);  // 始终验证
                long wowby = fieldValues.get(WEEK_OF_WEEK_BASED_YEAR);
                ensureIso(partialTemporal);
                LocalDate date = LocalDate.of(wby, 1, 4);
                if (resolverStyle == ResolverStyle.LENIENT) {
                    long dow = dowLong;  // 未验证
                    if (dow > 7) {
                        date = date.plusWeeks((dow - 1) / 7);
                        dow = ((dow - 1) % 7) + 1;
                    } else if (dow < 1) {
                        date = date.plusWeeks(Math.subtractExact(dow,  7) / 7);
                        dow = ((dow + 6) % 7) + 1;
                    }
                    date = date.plusWeeks(Math.subtractExact(wowby, 1)).with(DAY_OF_WEEK, dow);
                } else {
                    int dow = DAY_OF_WEEK.checkValidIntValue(dowLong);  // 验证
                    if (wowby < 1 || wowby > 52) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            getWeekRange(date).checkValidValue(wowby, this);  // 仅允许精确范围
                        } else {  // SMART
                            range().checkValidValue(wowby, this);  // 允许 1-53 滚动到下一年
                        }
                    }
                    date = date.plusWeeks(wowby - 1).with(DAY_OF_WEEK, dow);
                }
                fieldValues.remove(this);
                fieldValues.remove(WEEK_BASED_YEAR);
                fieldValues.remove(DAY_OF_WEEK);
                return date;
            }
            @Override
            public String toString() {
                return "WeekOfWeekBasedYear";
            }
        },
        WEEK_BASED_YEAR {
            @Override
            public TemporalUnit getBaseUnit() {
                return WEEK_BASED_YEARS;
            }
            @Override
            public TemporalUnit getRangeUnit() {
                return FOREVER;
            }
            @Override
            public ValueRange range() {
                return YEAR.range();
            }
            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(EPOCH_DAY) && isIso(temporal);
            }
            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: WeekBasedYear");
                }
                return getWeekBasedYear(LocalDate.from(temporal));
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                if (isSupportedBy(temporal) == false) {
                    throw new UnsupportedTemporalTypeException("不支持的字段: WeekBasedYear");
                }
                int newWby = range().checkValidIntValue(newValue, WEEK_BASED_YEAR);  // 严格检查
                LocalDate date = LocalDate.from(temporal);
                int dow = date.get(DAY_OF_WEEK);
                int week = getWeek(date);
                if (week == 53 && getWeekRange(newWby) == 52) {
                    week = 52;
                }
                LocalDate resolved = LocalDate.of(newWby, 1, 4);  // 4th 是保证在第一周
                int days = (dow - resolved.get(DAY_OF_WEEK)) + ((week - 1) * 7);
                resolved = resolved.plusDays(days);
                return (R) temporal.with(resolved);
            }
            @Override
            public String toString() {
                return "WeekBasedYear";
            }
        };

        @Override
        public boolean isDateBased() {
            return true;
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            return range();
        }

        //-------------------------------------------------------------------------
        private static final int[] QUARTER_DAYS = {0, 90, 181, 273, 0, 91, 182, 274};

        private static boolean isIso(TemporalAccessor temporal) {
            return Chronology.from(temporal).equals(IsoChronology.INSTANCE);
        }

        private static void ensureIso(TemporalAccessor temporal) {
            if (isIso(temporal) == false) {
                throw new DateTimeException("解析需要 IsoChronology");
            }
        }

        private static ValueRange getWeekRange(LocalDate date) {
            int wby = getWeekBasedYear(date);
            return ValueRange.of(1, getWeekRange(wby));
        }

        private static int getWeekRange(int wby) {
            LocalDate date = LocalDate.of(wby, 1, 1);
            // 如果标准年从星期四开始，或在闰年从星期三开始，则为 53 周
            if (date.getDayOfWeek() == THURSDAY || (date.getDayOfWeek() == WEDNESDAY && date.isLeapYear())) {
                return 53;
            }
            return 52;
        }

        private static int getWeek(LocalDate date) {
            int dow0 = date.getDayOfWeek().ordinal();
            int doy0 = date.getDayOfYear() - 1;
            int doyThu0 = doy0 + (3 - dow0);  // 调整到星期四（星期四的索引为 3）
            int alignedWeek = doyThu0 / 7;
            int firstThuDoy0 = doyThu0 - (alignedWeek * 7);
            int firstMonDoy0 = firstThuDoy0 - 3;
            if (firstMonDoy0 < -3) {
                firstMonDoy0 += 7;
            }
            if (doy0 < firstMonDoy0) {
                return (int) getWeekRange(date.withDayOfYear(180).minusYears(1)).getMaximum();
            }
            int week = ((doy0 - firstMonDoy0) / 7) + 1;
            if (week == 53) {
                if ((firstMonDoy0 == -3 || (firstMonDoy0 == -2 && date.isLeapYear())) == false) {
                    week = 1;
                }
            }
            return week;
        }

        private static int getWeekBasedYear(LocalDate date) {
            int year = date.getYear();
            int doy = date.getDayOfYear();
            if (doy <= 3) {
                int dow = date.getDayOfWeek().ordinal();
                if (doy - dow < -2) {
                    year--;
                }
            } else if (doy >= 363) {
                int dow = date.getDayOfWeek().ordinal();
                doy = doy - 363 - (date.isLeapYear() ? 1 : 0);
                if (doy - dow >= 0) {
                    year++;
                }
            }
            return year;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 单位的实现。
     */
    private static enum Unit implements TemporalUnit {

        /**
         * 表示周基年概念的单位。
         */
        WEEK_BASED_YEARS("WeekBasedYears", Duration.ofSeconds(31556952L)),
        /**
         * 表示季度年概念的单位。
         */
        QUARTER_YEARS("QuarterYears", Duration.ofSeconds(31556952L / 4));

        private final String name;
        private final Duration duration;

        private Unit(String name, Duration estimatedDuration) {
            this.name = name;
            this.duration = estimatedDuration;
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

        @Override
        public boolean isDurationEstimated() {
            return true;
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
        public boolean isSupportedBy(Temporal temporal) {
            return temporal.isSupported(EPOCH_DAY);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            switch (this) {
                case WEEK_BASED_YEARS:
                    return (R) temporal.with(WEEK_BASED_YEAR,
                            Math.addExact(temporal.get(WEEK_BASED_YEAR), amount));
                case QUARTER_YEARS:
                    return (R) temporal.plus(amount / 4, YEARS)
                            .plus((amount % 4) * 3, MONTHS);
                default:
                    throw new IllegalStateException("不可达");
            }
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            if (temporal1Inclusive.getClass() != temporal2Exclusive.getClass()) {
                return temporal1Inclusive.until(temporal2Exclusive, this);
            }
            switch(this) {
                case WEEK_BASED_YEARS:
                    return Math.subtractExact(temporal2Exclusive.getLong(WEEK_BASED_YEAR),
                            temporal1Inclusive.getLong(WEEK_BASED_YEAR));
                case QUARTER_YEARS:
                    return temporal1Inclusive.until(temporal2Exclusive, MONTHS) / 3;
                default:
                    throw new IllegalStateException("不可达");
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
