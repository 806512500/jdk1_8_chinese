
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
 * Copyright (c) 2012-2013, Stephen Colebourne & Michael Nascimento Santos
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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * 常见且有用的 TemporalAdjusters。
 * <p>
 * 调整器是修改时间对象的关键工具。
 * 它们存在是为了外部化调整过程，允许不同的方法，就像策略设计模式一样。
 * 例如，一个调整器可能设置日期以避免周末，或者一个设置日期为当月最后一天的调整器。
 * <p>
 * 使用 {@code TemporalAdjuster} 有两种等效的方法。
 * 第一种是直接调用接口上的方法。
 * 第二种是使用 {@link Temporal#with(TemporalAdjuster)}：
 * <pre>
 *   // 这两行是等效的，但推荐第二种方法
 *   temporal = thisAdjuster.adjustInto(temporal);
 *   temporal = temporal.with(thisAdjuster);
 * </pre>
 * 推荐使用第二种方法，{@code with(TemporalAdjuster)}，因为它在代码中更清晰易读。
 * <p>
 * 本类包含一组标准的调整器，作为静态方法提供。
 * 这些包括：
 * <ul>
 * <li>查找当月的第一天或最后一天
 * <li>查找下个月的第一天
 * <li>查找当年的第一天或最后一天
 * <li>查找下一年的第一天
 * <li>查找当月的第一个或最后一个星期几，例如“六月的第一个星期三”
 * <li>查找下一个或上一个星期几，例如“下一个星期四”
 * </ul>
 *
 * @implSpec
 * 静态方法提供的所有实现都是不可变的。
 *
 * @see TemporalAdjuster
 * @since 1.8
 */
public final class TemporalAdjusters {

    /**
     * 私有构造函数，因为这是一个工具类。
     */
    private TemporalAdjusters() {
    }

    //-----------------------------------------------------------------------
    /**
     * 获取一个包装日期调整器的 {@code TemporalAdjuster}。
     * <p>
     * {@code TemporalAdjuster} 基于低级别的 {@code Temporal} 接口。
     * 此方法允许从 {@code LocalDate} 到 {@code LocalDate} 的调整被包装以匹配基于时间的接口。
     * 这是为了方便用户编写的调整器更简单。
     * <p>
     * 一般来说，用户编写的调整器应该是静态常量：
     * <pre>{@code
     *  static TemporalAdjuster TWO_DAYS_LATER =
     *       TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(2));
     * }</pre>
     *
     * @param dateBasedAdjuster 日期基础调整器，不为空
     * @return 包装日期调整器的时间调整器，不为空
     */
    public static TemporalAdjuster ofDateAdjuster(UnaryOperator<LocalDate> dateBasedAdjuster) {
        Objects.requireNonNull(dateBasedAdjuster, "dateBasedAdjuster");
        return (temporal) -> {
            LocalDate input = LocalDate.from(temporal);
            LocalDate output = dateBasedAdjuster.apply(input);
            return temporal.with(output);
        };
    }

    //-----------------------------------------------------------------------
    /**
     * 返回“当月第一天”调整器，返回一个新日期，设置为当前月份的第一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2011-01-01。<br>
     * 输入 2011-02-15 将返回 2011-02-01。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  temporal.with(DAY_OF_MONTH, 1);
     * </pre>
     *
     * @return 当月第一天调整器，不为空
     */
    public static TemporalAdjuster firstDayOfMonth() {
        return (temporal) -> temporal.with(DAY_OF_MONTH, 1);
    }

    /**
     * 返回“当月最后一天”调整器，返回一个新日期，设置为当前月份的最后一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2011-01-31。<br>
     * 输入 2011-02-15 将返回 2011-02-28。<br>
     * 输入 2012-02-15 将返回 2012-02-29（闰年）。<br>
     * 输入 2011-04-15 将返回 2011-04-30。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  long lastDay = temporal.range(DAY_OF_MONTH).getMaximum();
     *  temporal.with(DAY_OF_MONTH, lastDay);
     * </pre>
     *
     * @return 当月最后一天调整器，不为空
     */
    public static TemporalAdjuster lastDayOfMonth() {
        return (temporal) -> temporal.with(DAY_OF_MONTH, temporal.range(DAY_OF_MONTH).getMaximum());
    }

    /**
     * 返回“下个月第一天”调整器，返回一个新日期，设置为下个月的第一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2011-02-01。<br>
     * 输入 2011-02-15 将返回 2011-03-01。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  temporal.with(DAY_OF_MONTH, 1).plus(1, MONTHS);
     * </pre>
     *
     * @return 下个月第一天调整器，不为空
     */
    public static TemporalAdjuster firstDayOfNextMonth() {
        return (temporal) -> temporal.with(DAY_OF_MONTH, 1).plus(1, MONTHS);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回“当年第一天”调整器，返回一个新日期，设置为当前年份的第一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2011-01-01。<br>
     * 输入 2011-02-15 将返回 2011-01-01。<br>
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  temporal.with(DAY_OF_YEAR, 1);
     * </pre>
     *
     * @return 当年第一天调整器，不为空
     */
    public static TemporalAdjuster firstDayOfYear() {
        return (temporal) -> temporal.with(DAY_OF_YEAR, 1);
    }

    /**
     * 返回“当年最后一天”调整器，返回一个新日期，设置为当前年份的最后一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2011-12-31。<br>
     * 输入 2011-02-15 将返回 2011-12-31。<br>
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  long lastDay = temporal.range(DAY_OF_YEAR).getMaximum();
     *  temporal.with(DAY_OF_YEAR, lastDay);
     * </pre>
     *
     * @return 当年最后一天调整器，不为空
     */
    public static TemporalAdjuster lastDayOfYear() {
        return (temporal) -> temporal.with(DAY_OF_YEAR, temporal.range(DAY_OF_YEAR).getMaximum());
    }

    /**
     * 返回“下一年第一天”调整器，返回一个新日期，设置为下一年的第一天。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-01-15 将返回 2012-01-01。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它等效于：
     * <pre>
     *  temporal.with(DAY_OF_YEAR, 1).plus(1, YEARS);
     * </pre>
     *
     * @return 下一年第一天调整器，不为空
     */
    public static TemporalAdjuster firstDayOfNextYear() {
        return (temporal) -> temporal.with(DAY_OF_YEAR, 1).plus(1, YEARS);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回第一个在月中的调整器，返回一个新日期，该日期在同一个月中，与第一个匹配的星期几相同。
     * 这用于表达式如“三月的第一个星期二”。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-12-15 为（星期一）将返回 2011-12-05。<br>
     * 输入 2011-12-15 为（星期五）将返回 2011-12-02。<br>
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 和 {@code DAY_OF_MONTH} 字段
     * 以及 {@code DAYS} 单位，并假设一周有七天。
     *
     * @param dayOfWeek 星期几，不为空
     * @return 第一个在月中的调整器，不为空
     */
    public static TemporalAdjuster firstInMonth(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.dayOfWeekInMonth(1, dayOfWeek);
    }

    /**
     * 返回最后一个在月中的调整器，返回一个新日期，该日期在同一个月中，与最后一个匹配的星期几相同。
     * 这用于表达式如“三月的最后一个星期二”。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-12-15 为（星期一）将返回 2011-12-26。<br>
     * 输入 2011-12-15 为（星期五）将返回 2011-12-30。<br>
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 和 {@code DAY_OF_MONTH} 字段
     * 以及 {@code DAYS} 单位，并假设一周有七天。
     *
     * @param dayOfWeek 星期几，不为空
     * @return 第一个在月中的调整器，不为空
     */
    public static TemporalAdjuster lastInMonth(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.dayOfWeekInMonth(-1, dayOfWeek);
    }

    /**
     * 返回星期几在月中的调整器，返回一个新日期，该日期在同一个月中，与指定的星期几和顺序相同。
     * 这用于表达式如“三月的第二个星期二”。
     * <p>
     * ISO 日历系统的行为如下：<br>
     * 输入 2011-12-15 为（1,星期二）将返回 2011-12-06。<br>
     * 输入 2011-12-15 为（2,星期二）将返回 2011-12-13。<br>
     * 输入 2011-12-15 为（3,星期二）将返回 2011-12-20。<br>
     * 输入 2011-12-15 为（4,星期二）将返回 2011-12-27。<br>
     * 输入 2011-12-15 为（5,星期二）将返回 2012-01-03。<br>
     * 输入 2011-12-15 为（-1,星期二）将返回 2011-12-27（当月最后一个）。<br>
     * 输入 2011-12-15 为（-4,星期二）将返回 2011-12-06（当月最后一个前3周）。<br>
     * 输入 2011-12-15 为（-5,星期二）将返回 2011-11-29（当月最后一个前4周）。<br>
     * 输入 2011-12-15 为（0,星期二）将返回 2011-11-29（上个月最后一个）。<br>
     * <p>
     * 对于正数或零顺序，算法等效于找到当月内第一个匹配的星期几，然后加上几周。
     * 对于负数顺序，算法等效于找到当月内最后一个匹配的星期几，然后减去几周。
     * 周数的顺序没有验证，并根据此算法宽松解释。此定义意味着顺序为零时，找到上个月最后一个匹配的星期几。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 和 {@code DAY_OF_MONTH} 字段
     * 以及 {@code DAYS} 单位，并假设一周有七天。
     *
     * @param ordinal 月中的周数，无界但通常从 -5 到 5
     * @param dayOfWeek 星期几，不为空
     * @return 星期几在月中的调整器，不为空
     */
    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        int dowValue = dayOfWeek.getValue();
        if (ordinal >= 0) {
            return (temporal) -> {
                Temporal temp = temporal.with(DAY_OF_MONTH, 1);
                int curDow = temp.get(DAY_OF_WEEK);
                int dowDiff = (dowValue - curDow + 7) % 7;
                dowDiff += (ordinal - 1L) * 7L;  // 安全防止溢出
                return temp.plus(dowDiff, DAYS);
            };
        } else {
            return (temporal) -> {
                Temporal temp = temporal.with(DAY_OF_MONTH, temporal.range(DAY_OF_MONTH).getMaximum());
                int curDow = temp.get(DAY_OF_WEEK);
                int daysDiff = dowValue - curDow;
                daysDiff = (daysDiff == 0 ? 0 : (daysDiff > 0 ? daysDiff - 7 : daysDiff));
                daysDiff -= (-ordinal - 1L) * 7L;  // 安全防止溢出
                return temp.plus(daysDiff, DAYS);
            };
        }
    }


                //-----------------------------------------------------------------------
    /**
     * 返回下一个星期几调整器，该调整器将日期调整到指定星期几之后的第一个出现日期。
     * <p>
     * ISO日历系统的行为如下：<br>
     * 输入 2011-01-15（星期六）对于参数（MONDAY）将返回 2011-01-17（两天后）。<br>
     * 输入 2011-01-15（星期六）对于参数（WEDNESDAY）将返回 2011-01-19（四天后）。<br>
     * 输入 2011-01-15（星期六）对于参数（SATURDAY）将返回 2011-01-22（七天后）。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 字段和 {@code DAYS} 单位，
     * 并假设一周有七天。
     *
     * @param dayOfWeek  要移动到的星期几，不为空
     * @return 下一个星期几调整器，不为空
     */
    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return (temporal) -> {
            int calDow = temporal.get(DAY_OF_WEEK);
            int daysDiff = calDow - dowValue;
            return temporal.plus(daysDiff >= 0 ? 7 - daysDiff : -daysDiff, DAYS);
        };
    }

    /**
     * 返回下一个或相同星期几调整器，该调整器将日期调整到指定星期几之后的第一个出现日期，
     * 除非已经是该星期几，则返回相同的对象。
     * <p>
     * ISO日历系统的行为如下：<br>
     * 输入 2011-01-15（星期六）对于参数（MONDAY）将返回 2011-01-17（两天后）。<br>
     * 输入 2011-01-15（星期六）对于参数（WEDNESDAY）将返回 2011-01-19（四天后）。<br>
     * 输入 2011-01-15（星期六）对于参数（SATURDAY）将返回 2011-01-15（与输入相同）。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 字段和 {@code DAYS} 单位，
     * 并假设一周有七天。
     *
     * @param dayOfWeek  要检查或移动到的星期几，不为空
     * @return 下一个或相同星期几调整器，不为空
     */
    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return (temporal) -> {
            int calDow = temporal.get(DAY_OF_WEEK);
            if (calDow == dowValue) {
                return temporal;
            }
            int daysDiff = calDow - dowValue;
            return temporal.plus(daysDiff >= 0 ? 7 - daysDiff : -daysDiff, DAYS);
        };
    }

    /**
     * 返回上一个星期几调整器，该调整器将日期调整到指定星期几之前的第一个出现日期。
     * <p>
     * ISO日历系统的行为如下：<br>
     * 输入 2011-01-15（星期六）对于参数（MONDAY）将返回 2011-01-10（五天前）。<br>
     * 输入 2011-01-15（星期六）对于参数（WEDNESDAY）将返回 2011-01-12（三天前）。<br>
     * 输入 2011-01-15（星期六）对于参数（SATURDAY）将返回 2011-01-08（七天前）。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 字段和 {@code DAYS} 单位，
     * 并假设一周有七天。
     *
     * @param dayOfWeek  要移动到的星期几，不为空
     * @return 上一个星期几调整器，不为空
     */
    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return (temporal) -> {
            int calDow = temporal.get(DAY_OF_WEEK);
            int daysDiff = dowValue - calDow;
            return temporal.minus(daysDiff >= 0 ? 7 - daysDiff : -daysDiff, DAYS);
        };
    }

    /**
     * 返回上一个或相同星期几调整器，该调整器将日期调整到指定星期几之前的第一个出现日期，
     * 除非已经是该星期几，则返回相同的对象。
     * <p>
     * ISO日历系统的行为如下：<br>
     * 输入 2011-01-15（星期六）对于参数（MONDAY）将返回 2011-01-10（五天前）。<br>
     * 输入 2011-01-15（星期六）对于参数（WEDNESDAY）将返回 2011-01-12（三天前）。<br>
     * 输入 2011-01-15（星期六）对于参数（SATURDAY）将返回 2011-01-15（与输入相同）。
     * <p>
     * 该行为适用于大多数日历系统。
     * 它使用 {@code DAY_OF_WEEK} 字段和 {@code DAYS} 单位，
     * 并假设一周有七天。
     *
     * @param dayOfWeek  要检查或移动到的星期几，不为空
     * @return 上一个或相同星期几调整器，不为空
     */
    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return (temporal) -> {
            int calDow = temporal.get(DAY_OF_WEEK);
            if (calDow == dowValue) {
                return temporal;
            }
            int daysDiff = dowValue - calDow;
            return temporal.minus(daysDiff >= 0 ? 7 - daysDiff : -daysDiff, DAYS);
        };
    }

}
