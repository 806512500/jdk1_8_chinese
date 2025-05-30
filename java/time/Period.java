
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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO-8601日历系统中的基于日期的时间量，例如“2年，3个月和4天”。
 * <p>
 * 该类以年、月和日为单位建模时间的数量或金额。
 * 有关基于时间的等效类，请参见 {@link Duration}。
 * <p>
 * 在添加到 {@link ZonedDateTime} 时，持续时间和周期对夏令时的处理方式不同。
 * {@code Duration} 将添加确切的秒数，因此一天的持续时间始终恰好为24小时。
 * 相比之下，{@code Period} 将添加一个概念上的天，尝试保持本地时间。
 * <p>
 * 例如，考虑将一个周期的一天和一个持续时间的一天添加到夏令时跳跃前一天的18:00。
 * {@code Period} 将添加概念上的天，并在次日18:00生成一个 {@code ZonedDateTime}。
 * 相比之下，{@code Duration} 将添加确切的24小时，导致次日19:00生成一个 {@code ZonedDateTime}（假设夏令时跳跃为1小时）。
 * <p>
 * 周期支持的单位是 {@link ChronoUnit#YEARS YEARS}、
 * {@link ChronoUnit#MONTHS MONTHS} 和 {@link ChronoUnit#DAYS DAYS}。
 * 所有三个字段始终存在，但可能设置为零。
 * <p>
 * ISO-8601日历系统是当今世界上大多数地区使用的现代民用日历系统。
 * 它等同于公历系统，其中今天的闰年规则适用于所有时间。
 * <p>
 * 周期被建模为有方向的时间量，意味着周期的各个部分可能是负数。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code Period} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class Period
        implements ChronoPeriod, Serializable {

    /**
     * 表示零周期的常量。
     */
    public static final Period ZERO = new Period(0, 0, 0);
    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -3587258372562876L;
    /**
     * 解析模式。
     */
    private static final Pattern PATTERN =
            Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?", Pattern.CASE_INSENSITIVE);

    /**
     * 支持的单位集合。
     */
    private static final List<TemporalUnit> SUPPORTED_UNITS =
            Collections.unmodifiableList(Arrays.<TemporalUnit>asList(YEARS, MONTHS, DAYS));

    /**
     * 年数。
     */
    private final int years;
    /**
     * 月数。
     */
    private final int months;
    /**
     * 天数。
     */
    private final int days;

    //-----------------------------------------------------------------------
    /**
     * 获取表示年数的 {@code Period}。
     * <p>
     * 结果周期将具有指定的年数。
     * 月和日单位将为零。
     *
     * @param years  年数，可以是正数或负数
     * @return 年数的周期，不为空
     */
    public static Period ofYears(int years) {
        return create(years, 0, 0);
    }

    /**
     * 获取表示月数的 {@code Period}。
     * <p>
     * 结果周期将具有指定的月数。
     * 年和日单位将为零。
     *
     * @param months  月数，可以是正数或负数
     * @return 月数的周期，不为空
     */
    public static Period ofMonths(int months) {
        return create(0, months, 0);
    }

    /**
     * 获取表示周数的 {@code Period}。
     * <p>
     * 结果周期将是基于天的，天数等于周数乘以7。
     * 年和月单位将为零。
     *
     * @param weeks  周数，可以是正数或负数
     * @return 周数转换为天数的周期，不为空
     */
    public static Period ofWeeks(int weeks) {
        return create(0, 0, Math.multiplyExact(weeks, 7));
    }

    /**
     * 获取表示天数的 {@code Period}。
     * <p>
     * 结果周期将具有指定的天数。
     * 年和月单位将为零。
     *
     * @param days  天数，可以是正数或负数
     * @return 天数的周期，不为空
     */
    public static Period ofDays(int days) {
        return create(0, 0, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取表示年数、月数和天数的 {@code Period}。
     * <p>
     * 这将基于年、月和天创建一个实例。
     *
     * @param years  年数，可以是负数
     * @param months  月数，可以是负数
     * @param days  天数，可以是负数
     * @return 年数、月数和天数的周期，不为空
     */
    public static Period of(int years, int months, int days) {
        return create(years, months, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间量中获取 {@code Period} 的实例。
     * <p>
     * 这将基于指定的时间量获取一个周期。
     * {@code TemporalAmount} 表示一个时间量，可以是基于日期的或基于时间的，该工厂将其提取为 {@code Period}。
     * <p>
     * 转换将遍历时间量的单位集，并使用
     * {@link ChronoUnit#YEARS YEARS}、{@link ChronoUnit#MONTHS MONTHS}
     * 和 {@link ChronoUnit#DAYS DAYS} 单位创建一个周期。
     * 如果发现其他单位，则会抛出异常。
     * <p>
     * 如果时间量是 {@code ChronoPeriod}，则必须使用 ISO 日历系统。
     *
     * @param amount  要转换的时间量，不为空
     * @return 等效的周期，不为空
     * @throws DateTimeException 如果无法转换为 {@code Period}
     * @throws ArithmeticException 如果年数、月数或天数超出 int 范围
     */
    public static Period from(TemporalAmount amount) {
        if (amount instanceof Period) {
            return (Period) amount;
        }
        if (amount instanceof ChronoPeriod) {
            if (IsoChronology.INSTANCE.equals(((ChronoPeriod) amount).getChronology()) == false) {
                throw new DateTimeException("Period requires ISO chronology: " + amount);
            }
        }
        Objects.requireNonNull(amount, "amount");
        int years = 0;
        int months = 0;
        int days = 0;
        for (TemporalUnit unit : amount.getUnits()) {
            long unitAmount = amount.get(unit);
            if (unit == ChronoUnit.YEARS) {
                years = Math.toIntExact(unitAmount);
            } else if (unit == ChronoUnit.MONTHS) {
                months = Math.toIntExact(unitAmount);
            } else if (unit == ChronoUnit.DAYS) {
                days = Math.toIntExact(unitAmount);
            } else {
                throw new DateTimeException("Unit must be Years, Months or Days, but was " + unit);
            }
        }
        return create(years, months, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code PnYnMnD}）中获取 {@code Period}。
     * <p>
     * 这将解析由 {@code toString()} 生成的字符串，该字符串基于 ISO-8601 周期格式 {@code PnYnMnD} 和 {@code PnW}。
     * <p>
     * 字符串以可选的符号开始，用 ASCII 负号或正号表示。如果为负，则整个周期取反。
     * 接下来是大写或小写的 ASCII 字母 "P"。
     * 然后有四个部分，每个部分由一个数字和一个后缀组成。
     * 至少必须有一个部分。
     * 各部分的后缀是 ASCII 字母 "Y"、"M"、"W" 和 "D"，分别表示年、月、周和日，接受大写或小写。
     * 后缀必须按顺序出现。
     * 每个部分的数字部分必须由 ASCII 数字组成。
     * 数字可以由 ASCII 负号或正号前缀。
     * 数字必须解析为 {@code int}。
     * <p>
     * 前导正负号以及其他单位的负值不是 ISO-8601 标准的一部分。此外，ISO-8601 不允许混合使用 {@code PnYnMnD} 和 {@code PnW} 格式。
     * 任何基于周的输入将乘以7并视为天数。
     * <p>
     * 例如，以下都是有效的输入：
     * <pre>
     *   "P2Y"             -- Period.ofYears(2)
     *   "P3M"             -- Period.ofMonths(3)
     *   "P4W"             -- Period.ofWeeks(4)
     *   "P5D"             -- Period.ofDays(5)
     *   "P1Y2M3D"         -- Period.of(1, 2, 3)
     *   "P1Y2M3W4D"       -- Period.of(1, 2, 25)
     *   "P-1Y2M"          -- Period.of(-1, 2, 0)
     *   "-P1Y2M"          -- Period.of(-1, -2, 0)
     * </pre>
     *
     * @param text  要解析的文本，不为空
     * @return 解析的周期，不为空
     * @throws DateTimeParseException 如果文本无法解析为周期
     */
    public static Period parse(CharSequence text) {
        Objects.requireNonNull(text, "text");
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            int negate = ("-".equals(matcher.group(1)) ? -1 : 1);
            String yearMatch = matcher.group(2);
            String monthMatch = matcher.group(3);
            String weekMatch = matcher.group(4);
            String dayMatch = matcher.group(5);
            if (yearMatch != null || monthMatch != null || dayMatch != null || weekMatch != null) {
                try {
                    int years = parseNumber(text, yearMatch, negate);
                    int months = parseNumber(text, monthMatch, negate);
                    int weeks = parseNumber(text, weekMatch, negate);
                    int days = parseNumber(text, dayMatch, negate);
                    days = Math.addExact(days, Math.multiplyExact(weeks, 7));
                    return create(years, months, days);
                } catch (NumberFormatException ex) {
                    throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0, ex);
                }
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0);
    }

    private static int parseNumber(CharSequence text, String str, int negate) {
        if (str == null) {
            return 0;
        }
        int val = Integer.parseInt(str);
        try {
            return Math.multiplyExact(val, negate);
        } catch (ArithmeticException ex) {
            throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0, ex);
        }
    }


                //-----------------------------------------------------------------------
    /**
     * 获取由两个日期之间的年数、月数和天数组成的 {@code Period}。
     * <p>
     * 开始日期包含在内，但结束日期不包含在内。
     * 通过移除完整的月份，然后计算剩余的天数，调整以确保两者具有相同的符号来计算周期。
     * 基于12个月的年份，将月份数量拆分为年份和月份。
     * 如果结束日大于或等于开始日，则视为一个月。
     * 例如，从 {@code 2010-01-15} 到 {@code 2011-03-18} 是一年、两个月和三天。
     * <p>
     * 如果结束日期在开始日期之前，此方法的结果可以是负周期。
     * 年、月和天的负号将相同。
     *
     * @param startDateInclusive  开始日期，包含在内，不为空
     * @param endDateExclusive  结束日期，不包含在内，不为空
     * @return 该日期和结束日期之间的周期，不为空
     * @see ChronoLocalDate#until(ChronoLocalDate)
     */
    public static Period between(LocalDate startDateInclusive, LocalDate endDateExclusive) {
        return startDateInclusive.until(endDateExclusive);
    }

    //-----------------------------------------------------------------------
    /**
     * 创建一个实例。
     *
     * @param years  年数
     * @param months  月数
     * @param days  天数
     */
    private static Period create(int years, int months, int days) {
        if ((years | months | days) == 0) {
            return ZERO;
        }
        return new Period(years, months, days);
    }

    /**
     * 构造函数。
     *
     * @param years  年数
     * @param months  月数
     * @param days  天数
     */
    private Period(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取请求单位的值。
     * <p>
     * 这将返回三个支持单位中的每个单位的值，
     * {@link ChronoUnit#YEARS YEARS}，{@link ChronoUnit#MONTHS MONTHS} 和
     * {@link ChronoUnit#DAYS DAYS}。
     * 所有其他单位都会抛出异常。
     *
     * @param unit  要返回值的 {@code TemporalUnit}
     * @return 单位的 long 值
     * @throws DateTimeException 如果单位不受支持
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return getYears();
        } else if (unit == ChronoUnit.MONTHS) {
            return getMonths();
        } else if (unit == ChronoUnit.DAYS) {
            return getDays();
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    /**
     * 获取此周期支持的单位集。
     * <p>
     * 支持的单位是 {@link ChronoUnit#YEARS YEARS}，
     * {@link ChronoUnit#MONTHS MONTHS} 和 {@link ChronoUnit#DAYS DAYS}。
     * 它们按年、月、日的顺序返回。
     * <p>
     * 该集合可以与 {@link #get(TemporalUnit)} 一起使用
     * 以访问周期的整个状态。
     *
     * @return 包含年、月和日单位的列表，不为空
     */
    @Override
    public List<TemporalUnit> getUnits() {
        return SUPPORTED_UNITS;
    }

    /**
     * 获取此周期的历法系统，即 ISO 日历系统。
     * <p>
     * {@code Chronology} 表示使用的日历系统。
     * ISO-8601 日历系统是当今世界上大多数地方使用的现代民用日历系统。
     * 它等同于公历系统，其中今天对闰年的规则适用于所有时间。
     *
     * @return ISO 历法系统，不为空
     */
    @Override
    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此周期的所有三个单位是否为零。
     * <p>
     * 零周期的年、月和天单位的值为零。
     *
     * @return 如果此周期为零长度，则返回 true
     */
    public boolean isZero() {
        return (this == ZERO);
    }

    /**
     * 检查此周期的三个单位中是否有任何一个是负数。
     * <p>
     * 这会检查年、月或天单位是否小于零。
     *
     * @return 如果此周期的任何单位为负数，则返回 true
     */
    public boolean isNegative() {
        return years < 0 || months < 0 || days < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此周期的年数。
     * <p>
     * 这将返回年单位。
     * <p>
     * 月单位不会自动与年单位规范化。
     * 这意味着 "15 个月" 与 "1 年和 3 个月" 不同。
     *
     * @return 此周期的年数，可能为负数
     */
    public int getYears() {
        return years;
    }

    /**
     * 获取此周期的月数。
     * <p>
     * 这将返回月单位。
     * <p>
     * 月单位不会自动与年单位规范化。
     * 这意味着 "15 个月" 与 "1 年和 3 个月" 不同。
     *
     * @return 此周期的月数，可能为负数
     */
    public int getMonths() {
        return months;
    }

    /**
     * 获取此周期的天数。
     * <p>
     * 这将返回天单位。
     *
     * @return 此周期的天数，可能为负数
     */
    public int getDays() {
        return days;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个具有指定年数的此周期的副本。
     * <p>
     * 这将在此周期的副本中设置年单位的数量。
     * 月和天单位不受影响。
     * <p>
     * 月单位不会自动与年单位规范化。
     * 这意味着 "15 个月" 与 "1 年和 3 个月" 不同。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param years  要表示的年数，可能为负数
     * @return 基于此周期并具有请求年数的 {@code Period}，不为空
     */
    public Period withYears(int years) {
        if (years == this.years) {
            return this;
        }
        return create(years, months, days);
    }

    /**
     * 返回一个具有指定月数的此周期的副本。
     * <p>
     * 这将在此周期的副本中设置月单位的数量。
     * 年和天单位不受影响。
     * <p>
     * 月单位不会自动与年单位规范化。
     * 这意味着 "15 个月" 与 "1 年和 3 个月" 不同。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param months  要表示的月数，可能为负数
     * @return 基于此周期并具有请求月数的 {@code Period}，不为空
     */
    public Period withMonths(int months) {
        if (months == this.months) {
            return this;
        }
        return create(years, months, days);
    }

    /**
     * 返回一个具有指定天数的此周期的副本。
     * <p>
     * 这将在此周期的副本中设置天单位的数量。
     * 年和月单位不受影响。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param days  要表示的天数，可能为负数
     * @return 基于此周期并具有请求天数的 {@code Period}，不为空
     */
    public Period withDays(int days) {
        if (days == this.days) {
            return this;
        }
        return create(years, months, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个具有指定周期相加的此周期的副本。
     * <p>
     * 这将分别对年、月和天进行操作。
     * 不进行规范化。
     * <p>
     * 例如，"1 年，6 个月和 3 天" 加上 "2 年，2 个月和 2 天"
     * 返回 "3 年，8 个月和 5 天"。
     * <p>
     * 指定的数量通常是一个 {@code Period} 的实例。
     * 其他类型将使用 {@link Period#from(TemporalAmount)} 进行解释。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param amountToAdd  要添加的数量，不为空
     * @return 基于此周期并具有请求周期相加的 {@code Period}，不为空
     * @throws DateTimeException 如果指定的数量具有非 ISO 历法系统或
     *  包含无效单位
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period plus(TemporalAmount amountToAdd) {
        Period isoAmount = Period.from(amountToAdd);
        return create(
                Math.addExact(years, isoAmount.years),
                Math.addExact(months, isoAmount.months),
                Math.addExact(days, isoAmount.days));
    }

    /**
     * 返回一个具有指定年数相加的此周期的副本。
     * <p>
     * 这将在此周期的副本中将数量添加到年单位。
     * 月和天单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 加上 2 年返回 "3 年，6 个月和 3 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param yearsToAdd  要添加的年数，正数或负数
     * @return 基于此周期并具有指定年数相加的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return create(Math.toIntExact(Math.addExact(years, yearsToAdd)), months, days);
    }

    /**
     * 返回一个具有指定月数相加的此周期的副本。
     * <p>
     * 这将在此周期的副本中将数量添加到月单位。
     * 年和天单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 加上 2 个月返回 "1 年，8 个月和 3 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param monthsToAdd  要添加的月数，正数或负数
     * @return 基于此周期并具有指定月数相加的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        return create(years, Math.toIntExact(Math.addExact(months, monthsToAdd)), days);
    }

    /**
     * 返回一个具有指定天数相加的此周期的副本。
     * <p>
     * 这将在此周期的副本中将数量添加到天单位。
     * 年和月单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 加上 2 天返回 "1 年，6 个月和 5 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param daysToAdd  要添加的天数，正数或负数
     * @return 基于此周期并具有指定天数相加的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        return create(years, months, Math.toIntExact(Math.addExact(days, daysToAdd)));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个具有指定周期相减的此周期的副本。
     * <p>
     * 这将分别对年、月和天进行操作。
     * 不进行规范化。
     * <p>
     * 例如，"1 年，6 个月和 3 天" 减去 "2 年，2 个月和 2 天"
     * 返回 "-1 年，4 个月和 1 天"。
     * <p>
     * 指定的数量通常是一个 {@code Period} 的实例。
     * 其他类型将使用 {@link Period#from(TemporalAmount)} 进行解释。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param amountToSubtract  要减去的数量，不为空
     * @return 基于此周期并具有请求周期相减的 {@code Period}，不为空
     * @throws DateTimeException 如果指定的数量具有非 ISO 历法系统或
     *  包含无效单位
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period minus(TemporalAmount amountToSubtract) {
        Period isoAmount = Period.from(amountToSubtract);
        return create(
                Math.subtractExact(years, isoAmount.years),
                Math.subtractExact(months, isoAmount.months),
                Math.subtractExact(days, isoAmount.days));
    }

    /**
     * 返回一个具有指定年数相减的此周期的副本。
     * <p>
     * 这将在此周期的副本中从年单位中减去数量。
     * 月和天单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 减去 2 年返回 "-1 年，6 个月和 3 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param yearsToSubtract  要减去的年数，正数或负数
     * @return 基于此周期并具有指定年数相减的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract));
    }

    /**
     * 返回一个具有指定月数相减的此周期的副本。
     * <p>
     * 这将在此周期的副本中从月单位中减去数量。
     * 年和天单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 减去 2 个月返回 "1 年，4 个月和 3 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param monthsToSubtract  要减去的月数，正数或负数
     * @return 基于此周期并具有指定月数相减的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period minusMonths(long monthsToSubtract) {
        return (monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract));
    }

    /**
     * 返回一个具有指定天数相减的此周期的副本。
     * <p>
     * 这将在此周期的副本中从天单位中减去数量。
     * 年和月单位不受影响。
     * 例如，"1 年，6 个月和 3 天" 减去 2 天返回 "1 年，6 个月和 1 天"。
     * <p>
     * 该实例是不可变的，不会受此方法调用的影响。
     *
     * @param daysToSubtract  要减去的天数，正数或负数
     * @return 基于此周期并具有指定天数相减的 {@code Period}，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period minusDays(long daysToSubtract) {
        return (daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract));
    }


                //-----------------------------------------------------------------------
    /**
     * 返回一个新的实例，该实例中的每个时间段元素都乘以指定的标量。
     * <p>
     * 这将返回一个时间段，其中的年、月和日单位分别乘以指定的标量。
     * 例如，一个“2年，-3个月和4天”的时间段乘以3将返回“6年，-9个月和12天”。
     * 不进行规范化。
     *
     * @param scalar  要乘以的标量，不为空
     * @return 基于此时间段的 {@code Period}，其金额乘以标量，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period multipliedBy(int scalar) {
        if (this == ZERO || scalar == 1) {
            return this;
        }
        return create(
                Math.multiplyExact(years, scalar),
                Math.multiplyExact(months, scalar),
                Math.multiplyExact(days, scalar));
    }

    /**
     * 返回一个新的实例，该实例中的每个时间段金额取反。
     * <p>
     * 这将返回一个时间段，其中的年、月和日单位分别取反。
     * 例如，一个“2年，-3个月和4天”的时间段将被取反为“-2年，3个月和-4天”。
     * 不进行规范化。
     *
     * @return 基于此时间段的 {@code Period}，其金额取反，不为空
     * @throws ArithmeticException 如果发生数值溢出，这仅在某个单位的值为 {@code Long.MIN_VALUE} 时发生
     */
    public Period negated() {
        return multipliedBy(-1);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中的年和月已规范化。
     * <p>
     * 这将规范化年和月单位，保持日单位不变。
     * 月单位将调整为绝对值小于11，年单位将相应调整。例如，一个“1年和15个月”的时间段将规范化为“2年和3个月”。
     * <p>
     * 规范化后，年和月单位的符号将相同。
     * 例如，一个“1年和-25个月”的时间段将规范化为“-1年和-1个月”。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @return 基于此时间段的 {@code Period}，其中多余的月已规范化为年，不为空
     * @throws ArithmeticException 如果发生数值溢出
     */
    public Period normalized() {
        long totalMonths = toTotalMonths();
        long splitYears = totalMonths / 12;
        int splitMonths = (int) (totalMonths % 12);  // 不会发生溢出
        if (splitYears == years && splitMonths == months) {
            return this;
        }
        return create(Math.toIntExact(splitYears), splitMonths, days);
    }

    /**
     * 获取此时间段的总月数。
     * <p>
     * 这将返回时间段中的总月数，通过将年数乘以12并加上月数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @return 时间段中的总月数，可能为负数
     */
    public long toTotalMonths() {
        return years * 12L + months;  // 不会发生溢出
    }

    //-------------------------------------------------------------------------
    /**
     * 将此时间段添加到指定的时间对象。
     * <p>
     * 这将返回一个与输入相同可观察类型的临时对象，其中添加了此时间段。
     * 如果时间对象有历法，它必须是ISO历法。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#plus(TemporalAmount)} 反转调用模式会更清晰。
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   dateTime = thisPeriod.addTo(dateTime);
     *   dateTime = dateTime.plus(thisPeriod);
     * </pre>
     * <p>
     * 计算过程如下。
     * 首先，检查时间对象的历法，确保它是ISO历法或null。
     * 其次，如果月数为零，且年数非零，则添加年数；否则，如果年数和月数的组合非零，则添加年数和月数。
     * 最后，添加任何天数。
     * <p>
     * 这种方法确保部分时间段可以添加到部分日期。
     * 例如，一个包含年和/或月的时间段可以添加到 {@code YearMonth}，但包含天的时间段则不能。
     * 该方法还确保在必要时将年和月一起添加，以确保在月底的正确行为。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的时间对象，不为空
     * @return 已进行调整的同一类型对象，不为空
     * @throws DateTimeException 如果无法添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal addTo(Temporal temporal) {
        validateChrono(temporal);
        if (months == 0) {
            if (years != 0) {
                temporal = temporal.plus(years, YEARS);
            }
        } else {
            long totalMonths = toTotalMonths();
            if (totalMonths != 0) {
                temporal = temporal.plus(totalMonths, MONTHS);
            }
        }
        if (days != 0) {
            temporal = temporal.plus(days, DAYS);
        }
        return temporal;
    }

    /**
     * 从指定的时间对象中减去此时间段。
     * <p>
     * 这将返回一个与输入相同可观察类型的临时对象，其中减去了此时间段。
     * 如果时间对象有历法，它必须是ISO历法。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#minus(TemporalAmount)} 反转调用模式会更清晰。
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   dateTime = thisPeriod.subtractFrom(dateTime);
     *   dateTime = dateTime.minus(thisPeriod);
     * </pre>
     * <p>
     * 计算过程如下。
     * 首先，检查时间对象的历法，确保它是ISO历法或null。
     * 其次，如果月数为零，且年数非零，则减去年数；否则，如果年数和月数的组合非零，则减去年数和月数。
     * 最后，减去任何天数。
     * <p>
     * 这种方法确保部分时间段可以从部分日期中减去。
     * 例如，一个包含年和/或月的时间段可以从 {@code YearMonth} 中减去，但包含天的时间段则不能。
     * 该方法还确保在必要时将年和月一起减去，以确保在月底的正确行为。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的时间对象，不为空
     * @return 已进行调整的同一类型对象，不为空
     * @throws DateTimeException 如果无法减去
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal subtractFrom(Temporal temporal) {
        validateChrono(temporal);
        if (months == 0) {
            if (years != 0) {
                temporal = temporal.minus(years, YEARS);
            }
        } else {
            long totalMonths = toTotalMonths();
            if (totalMonths != 0) {
                temporal = temporal.minus(totalMonths, MONTHS);
            }
        }
        if (days != 0) {
            temporal = temporal.minus(days, DAYS);
        }
        return temporal;
    }

    /**
     * 验证时间对象的历法是否正确。
     */
    private void validateChrono(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
        if (temporalChrono != null && IsoChronology.INSTANCE.equals(temporalChrono) == false) {
            throw new DateTimeException("Chronology mismatch, expected: ISO, actual: " + temporalChrono.getId());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时间段是否等于另一个时间段。
     * <p>
     * 比较基于类型 {@code Period} 和每个三个金额。
     * 要相等，年、月和日单位必须分别相等。
     * 请注意，这意味着一个“15个月”的时间段不等于一个“1年和3个月”的时间段。
     *
     * @param obj  要检查的对象，为空返回 false
     * @return 如果此时间段等于另一个时间段，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Period) {
            Period other = (Period) obj;
            return years == other.years &&
                    months == other.months &&
                    days == other.days;
        }
        return false;
    }

    /**
     * 为该时间段生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return years + Integer.rotateLeft(months, 8) + Integer.rotateLeft(days, 16);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间段输出为 {@code String}，例如 {@code P6Y3M1D}。
     * <p>
     * 输出将采用 ISO-8601 时间段格式。
     * 零时间段将表示为零天，'P0D'。
     *
     * @return 此时间段的字符串表示，不为空
     */
    @Override
    public String toString() {
        if (this == ZERO) {
            return "P0D";
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append('P');
            if (years != 0) {
                buf.append(years).append('Y');
            }
            if (months != 0) {
                buf.append(months).append('M');
            }
            if (days != 0) {
                buf.append(days).append('D');
            }
            return buf.toString();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(14);  // 标识一个 Period
     *  out.writeInt(years);
     *  out.writeInt(months);
     *  out.writeInt(days);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.PERIOD_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws java.io.InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(years);
        out.writeInt(months);
        out.writeInt(days);
    }

    static Period readExternal(DataInput in) throws IOException {
        int years = in.readInt();
        int months = in.readInt();
        int days = in.readInt();
        return Period.of(years, months, days);
    }

}
