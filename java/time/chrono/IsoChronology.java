
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

import java.io.InvalidObjectException;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * ISO日历系统。
 * <p>
 * 该编年系统定义了ISO日历系统的规则。
 * 该日历系统基于ISO-8601标准，这是事实上的世界日历。
 * <p>
 * 字段定义如下：
 * <ul>
 * <li>纪元 - 有两个纪元，'当前纪元'（CE）和'当前纪元之前'（BCE）。
 * <li>纪元年 - 纪元年与当前CE纪元的预设年相同。
 *  在ISO纪元之前的BCE纪元，年份随着时光倒流从1向上增加。
 * <li>预设年 - 预设年与当前纪元的纪元年相同。
 *  对于前一个纪元，年份有零，然后是负值。
 * <li>年中的月 - ISO年中有12个月，编号从1到12。
 * <li>月中的日 - ISO月中的日数在28到31天之间，编号从1到31。
 *  4月、6月、9月和11月有30天，1月、3月、5月、7月、8月、10月和12月有31天。
 *  2月有28天，闰年有29天。
 * <li>年中的日 - 标准ISO年有365天，闰年有366天。
 *  日子编号从1到365或1到366。
 * <li>闰年 - 闰年每4年发生一次，但年份能被100整除且不能被400整除的年份除外。
 * </ul>
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @since 1.8
 */
public final class IsoChronology extends AbstractChronology implements Serializable {

    /**
     * ISO编年系统的单例实例。
     */
    public static final IsoChronology INSTANCE = new IsoChronology();

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -1440403870442975015L;

    /**
     * 受限构造函数。
     */
    private IsoChronology() {
    }

    //-----------------------------------------------------------------------
    /**
     * 获取编年系统的ID - 'ISO'。
     * <p>
     * ID唯一标识了{@code Chronology}。
     * 可以使用{@link Chronology#of(String)}通过ID查找{@code Chronology}。
     *
     * @return 编年系统ID - 'ISO'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "ISO";
    }

    /**
     * 获取底层日历系统的日历类型 - 'iso8601'。
     * <p>
     * 日历类型是由
     * <em>Unicode Locale Data Markup Language (LDML)</em>规范定义的标识符。
     * 可以使用{@link Chronology#of(String)}通过日历类型查找{@code Chronology}。
     * 也可以作为区域设置的一部分，通过
     * {@link Locale#getUnicodeLocaleType(String)}使用键'ca'访问。
     *
     * @return 日历系统类型 - 'iso8601'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "iso8601";
    }

    //-----------------------------------------------------------------------
    /**
     * 从纪元、纪元年、年中的月和月中的日字段获取ISO本地日期。
     *
     * @param era  ISO纪元，不为空
     * @param yearOfEra  ISO纪元年
     * @param month  ISO年中的月
     * @param dayOfMonth  ISO月中的日
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果{@code era}的类型不是{@code IsoEra}
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * 从预设年、年中的月和月中的日字段获取ISO本地日期。
     * <p>
     * 这等同于{@link LocalDate#of(int, int, int)}。
     *
     * @param prolepticYear  ISO预设年
     * @param month  ISO年中的月
     * @param dayOfMonth  ISO月中的日
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate date(int prolepticYear, int month, int dayOfMonth) {
        return LocalDate.of(prolepticYear, month, dayOfMonth);
    }

    /**
     * 从纪元、纪元年和年中的日字段获取ISO本地日期。
     *
     * @param era  ISO纪元，不为空
     * @param yearOfEra  ISO纪元年
     * @param dayOfYear  ISO年中的日
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * 从预设年和年中的日字段获取ISO本地日期。
     * <p>
     * 这等同于{@link LocalDate#ofYearDay(int, int)}。
     *
     * @param prolepticYear  ISO预设年
     * @param dayOfYear  ISO年中的日
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateYearDay(int prolepticYear, int dayOfYear) {
        return LocalDate.ofYearDay(prolepticYear, dayOfYear);
    }

    /**
     * 从纪元日获取ISO本地日期。
     * <p>
     * 这等同于{@link LocalDate#ofEpochDay(long)}。
     *
     * @param epochDay  纪元日
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateEpochDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    //-----------------------------------------------------------------------
    /**
     * 从另一个日期时间对象获取ISO本地日期。
     * <p>
     * 这等同于{@link LocalDate#from(TemporalAccessor)}。
     *
     * @param temporal  要转换的日期时间对象，不为空
     * @return ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate date(TemporalAccessor temporal) {
        return LocalDate.from(temporal);
    }

    /**
     * 从另一个日期时间对象获取ISO本地日期时间。
     * <p>
     * 这等同于{@link LocalDateTime#from(TemporalAccessor)}。
     *
     * @param temporal  要转换的日期时间对象，不为空
     * @return ISO本地日期时间，不为空
     * @throws DateTimeException 如果无法创建日期时间
     */
    @Override  // 覆盖以协变返回类型
    public LocalDateTime localDateTime(TemporalAccessor temporal) {
        return LocalDateTime.from(temporal);
    }

    /**
     * 从另一个日期时间对象获取ISO带时区的日期时间。
     * <p>
     * 这等同于{@link ZonedDateTime#from(TemporalAccessor)}。
     *
     * @param temporal  要转换的日期时间对象，不为空
     * @return ISO带时区的日期时间，不为空
     * @throws DateTimeException 如果无法创建日期时间
     */
    @Override  // 覆盖以协变返回类型
    public ZonedDateTime zonedDateTime(TemporalAccessor temporal) {
        return ZonedDateTime.from(temporal);
    }

    /**
     * 从{@code Instant}获取ISO带时区的日期时间。
     * <p>
     * 这等同于{@link ZonedDateTime#ofInstant(Instant, ZoneId)}。
     *
     * @param instant  要创建日期时间的瞬间，不为空
     * @param zone  时区，不为空
     * @return 带时区的日期时间，不为空
     * @throws DateTimeException 如果结果超出支持的范围
     */
    @Override
    public ZonedDateTime zonedDateTime(Instant instant, ZoneId zone) {
        return ZonedDateTime.ofInstant(instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前ISO本地日期。
     * <p>
     * 这将查询默认时区的{@link Clock#systemDefaultZone() 系统时钟}以获取当前日期。
     * <p>
     * 使用此方法将阻止使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前ISO本地日期。
     * <p>
     * 这将查询指定时区的{@link Clock#system(ZoneId) 系统时钟}以获取当前日期。
     * 指定时区避免了对默认时区的依赖。
     * <p>
     * 使用此方法将阻止使用替代时钟进行测试，因为时钟是硬编码的。
     *
     * @return 使用系统时钟的当前ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    /**
     * 从指定的时钟获取当前ISO本地日期。
     * <p>
     * 这将查询指定的时钟以获取当前日期 - 今天。
     * 使用此方法允许使用替代时钟进行测试。
     * 可以通过{@link Clock 依赖注入}引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前ISO本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public LocalDate dateNow(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return date(LocalDate.now(clock));
    }

    //-----------------------------------------------------------------------
    /**
     * 根据ISO预设日历系统的规则检查年份是否为闰年。
     * <p>
     * 此方法在整个时间线上应用当前的闰年规则。
     * 一般来说，能被4整除的年份是闰年。
     * 然而，能被100整除的年份不是闰年，但能被400整除的年份是闰年。
     * <p>
     * 例如，1904年是闰年，因为它能被4整除。
     * 1900年不是闰年，因为它能被100整除，但2000年是闰年，因为它能被400整除。
     * <p>
     * 该计算是预设的 - 将相同的规则应用于遥远的未来和遥远的过去。
     * 这在历史上是不准确的，但符合ISO-8601标准。
     *
     * @param prolepticYear  要检查的ISO预设年
     * @return 如果年份是闰年则为true，否则为false
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return ((prolepticYear & 3) == 0) && ((prolepticYear % 100) != 0 || (prolepticYear % 400) == 0);
    }


                @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof IsoEra == false) {
            throw new ClassCastException("Era must be IsoEra");
        }
        return (era == IsoEra.CE ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public IsoEra eraOf(int eraValue) {
        return IsoEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(IsoEra.values());
    }

    //-----------------------------------------------------------------------
    /**
     * 解析解析过程中解析的 {@code ChronoField} 值为日期。
     * <p>
     * 大多数 {@code TemporalField} 实现是使用字段上的解析方法解析的。相比之下，
     * {@code ChronoField} 类定义了仅相对于历法才有意义的字段。因此，{@code ChronoField}
     * 日期字段在此特定历法的上下文中解析。
     * <p>
     * {@code ChronoField} 实例在 ISO 历法系统中按以下方式解析。
     * <ul>
     * <li>{@code EPOCH_DAY} - 如果存在，这将转换为 {@code LocalDate}，然后所有其他日期字段
     *  都将与日期进行交叉检查。
     * <li>{@code PROLEPTIC_MONTH} - 如果存在，则将其拆分为 {@code YEAR} 和 {@code MONTH_OF_YEAR}。
     *  如果模式为严格或智能，则验证该字段。
     * <li>{@code YEAR_OF_ERA} 和 {@code ERA} - 如果两者都存在，则它们将组合形成一个 {@code YEAR}。
     *  在宽松模式下，不验证 {@code YEAR_OF_ERA} 范围，在智能和严格模式下则进行验证。在所有三种模式下，
     *  都验证 {@code ERA} 的范围。如果仅存在 {@code YEAR_OF_ERA}，且模式为智能或宽松，则假定当前纪元（CE/AD）。
     *  在严格模式下，不假定任何纪元，且 {@code YEAR_OF_ERA} 保持不变。如果仅存在 {@code ERA}，则保持不变。
     * <li>{@code YEAR}，{@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} -
     *  如果三个都存在，则它们将组合形成一个 {@code LocalDate}。在所有三种模式下，都验证 {@code YEAR}。
     *  如果模式为智能或严格，则验证月份和日期，日期从 1 到 31 验证。如果模式为宽松，则日期以相当于在请求年份的
     *  1 月 1 日创建日期，然后添加月份差，再添加天数差的方式组合。如果模式为智能，且日期大于该年月的最大日期，
     *  则调整日期为该年月的最后一天。如果模式为严格，则三个字段必须形成一个有效日期。
     * <li>{@code YEAR} 和 {@code DAY_OF_YEAR} -
     *  如果两者都存在，则它们将组合形成一个 {@code LocalDate}。在所有三种模式下，都验证 {@code YEAR}。
     *  如果模式为宽松，则日期以相当于在请求年份的 1 月 1 日创建日期，然后添加天数差的方式组合。如果模式为智能或严格，
     *  则两个字段必须形成一个有效日期。
     * <li>{@code YEAR}，{@code MONTH_OF_YEAR}，{@code ALIGNED_WEEK_OF_MONTH} 和
     *  {@code ALIGNED_DAY_OF_WEEK_IN_MONTH} -
     *  如果四个都存在，则它们将组合形成一个 {@code LocalDate}。在所有三种模式下，都验证 {@code YEAR}。
     *  如果模式为宽松，则日期以相当于在请求年份的 1 月 1 日创建日期，然后添加月份差，再添加周数差，最后添加天数差的方式组合。
     *  如果模式为智能或严格，则验证所有四个字段的外范围。日期以相当于在请求年份和月份的第一天创建日期，然后添加周数和天数
     *  以达到其值的方式组合。如果模式为严格，则日期还验证调整天数和周数是否改变了月份。
     * <li>{@code YEAR}，{@code MONTH_OF_YEAR}，{@code ALIGNED_WEEK_OF_MONTH} 和
     *  {@code DAY_OF_WEEK} - 如果四个都存在，则它们将组合形成一个 {@code LocalDate}。方法与上述
     *  {@code ALIGNED_DAY_OF_WEEK_IN_MONTH} 中描述的年份、月份和周数的方法相同。在处理完年份、月份和周数后，
     *  调整为下一个或相同的匹配星期几。
     * <li>{@code YEAR}，{@code ALIGNED_WEEK_OF_YEAR} 和 {@code ALIGNED_DAY_OF_WEEK_IN_YEAR} -
     *  如果三个都存在，则它们将组合形成一个 {@code LocalDate}。在所有三种模式下，都验证 {@code YEAR}。
     *  如果模式为宽松，则日期以相当于在请求年份的 1 月 1 日创建日期，然后添加周数差，再添加天数差的方式组合。
     *  如果模式为智能或严格，则验证所有三个字段的外范围。日期以相当于在请求年份的第一天创建日期，然后添加周数和天数
     *  以达到其值的方式组合。如果模式为严格，则日期还验证调整天数和周数是否改变了年份。
     * <li>{@code YEAR}，{@code ALIGNED_WEEK_OF_YEAR} 和 {@code DAY_OF_WEEK} -
     *  如果三个都存在，则它们将组合形成一个 {@code LocalDate}。方法与上述
     *  {@code ALIGNED_DAY_OF_WEEK_IN_YEAR} 中描述的年份和周数的方法相同。在处理完年份和周数后，
     *  调整为下一个或相同的匹配星期几。
     * </ul>
     *
     * @param fieldValues  字段到值的映射，可以更新，不为空
     * @param resolverStyle  请求的解析类型，不为空
     * @return 解析的日期，如果信息不足无法创建日期则返回 null
     * @throws DateTimeException 如果日期无法解析，通常是因为输入数据冲突
     */
    @Override  // 重写以提高性能
    public LocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (LocalDate) super.resolveDate(fieldValues, resolverStyle);
    }

    @Override  // 重写以实现更好的回溯算法
    void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = fieldValues.remove(PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                PROLEPTIC_MONTH.checkValidValue(pMonth);
            }
            addFieldValue(fieldValues, MONTH_OF_YEAR, Math.floorMod(pMonth, 12) + 1);
            addFieldValue(fieldValues, YEAR, Math.floorDiv(pMonth, 12));
        }
    }

    @Override  // 重写以增强行为
    LocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long yoeLong = fieldValues.remove(YEAR_OF_ERA);
        if (yoeLong != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                YEAR_OF_ERA.checkValidValue(yoeLong);
            }
            Long era = fieldValues.remove(ERA);
            if (era == null) {
                Long year = fieldValues.get(YEAR);
                if (resolverStyle == ResolverStyle.STRICT) {
                    // 如果严格，则不假设纪元，但与年份进行交叉检查
                    if (year != null) {
                        addFieldValue(fieldValues, YEAR, (year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
                    } else {
                        // 重新插入之前删除的字段，没有交叉检查问题
                        fieldValues.put(YEAR_OF_ERA, yoeLong);
                    }
                } else {
                    // 假设纪元
                    addFieldValue(fieldValues, YEAR, (year == null || year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
                }
            } else if (era.longValue() == 1L) {
                addFieldValue(fieldValues, YEAR, yoeLong);
            } else if (era.longValue() == 0L) {
                addFieldValue(fieldValues, YEAR, Math.subtractExact(1, yoeLong));
            } else {
                throw new DateTimeException("无效的纪元值: " + era);
            }
        } else if (fieldValues.containsKey(ERA)) {
            ERA.checkValidValue(fieldValues.get(ERA));  // 始终验证
        }
        return null;
    }

    @Override  // 重写以提高性能
    LocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
            long days = Math.subtractExact(fieldValues.remove(DAY_OF_MONTH), 1);
            return LocalDate.of(y, 1, 1).plusMonths(months).plusDays(days);
        }
        int moy = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR));
        int dom = DAY_OF_MONTH.checkValidIntValue(fieldValues.remove(DAY_OF_MONTH));
        if (resolverStyle == ResolverStyle.SMART) {  // 之前的有效值
            if (moy == 4 || moy == 6 || moy == 9 || moy == 11) {
                dom = Math.min(dom, 30);
            } else if (moy == 2) {
                dom = Math.min(dom, Month.FEBRUARY.length(Year.isLeap(y)));

            }
        }
        return LocalDate.of(y, moy, dom);
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        return field.range();
    }

    //-----------------------------------------------------------------------
    /**
     * 基于年、月和日获取此历法的周期。
     * <p>
     * 这返回一个基于指定年、月和日的 ISO 历法周期。有关详细信息，请参见 {@link Period}。
     *
     * @param years  年数，可以为负
     * @param months  月数，可以为负
     * @param days  日数，可以为负
     * @return 以本历法表示的周期，不为空
     * @return ISO 周期，不为空
     */
    @Override  // 重写以实现协变返回类型
    public Period period(int years, int months, int days) {
        return Period.of(years, months, days);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a>
     * 序列化此历法。
     * @serialData
     * <pre>
     *  out.writeByte(1);     // 识别一个历法
     *  out.writeUTF(getId());
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    @Override
    Object writeReplace() {
        return super.writeReplace();
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }
}
