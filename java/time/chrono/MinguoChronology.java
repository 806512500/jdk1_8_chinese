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
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 民国历法系统。
 * <p>
 * 该历法定义了民国历法系统的规则。
 * 这个历法系统主要在中华民国（通常称为台湾）使用。
 * 日期对齐方式为 {@code 0001-01-01 (民国)} 对应 {@code 1912-01-01 (ISO)}。
 * <p>
 * 字段定义如下：
 * <ul>
 * <li>纪元 - 有两个纪元，当前的“民国”（ERA_ROC）和之前的纪元（ERA_BEFORE_ROC）。
 * <li>纪元年 - 当前纪元的纪元年从纪元开始逐年递增。
 *  对于之前的纪元，年份随着时光倒流而递增。
 *  当前纪元的值等于 ISO 前推年减去 1911。
 * <li>前推年 - 当前纪元的前推年与纪元年相同。
 *  对于之前的纪元，年份为零，然后为负值。
 *  值等于 ISO 前推年减去 1911。
 * <li>年份 - 民国年份与 ISO 完全一致。
 * <li>月份 - 民国月份与 ISO 完全一致。
 * <li>日 - 民国日与 ISO 完全一致。
 * <li>年份日 - 民国年份日与 ISO 完全一致。
 * <li>闰年 - 民国闰年模式与 ISO 完全一致，使得两个历法系统永远不会错开。
 * </ul>
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @since 1.8
 */
public final class MinguoChronology extends AbstractChronology implements Serializable {

    /**
     * 民国历法的单例实例。
     */
    public static final MinguoChronology INSTANCE = new MinguoChronology();

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 1039765215346859963L;
    /**
     * ISO 和民国之间的年份差。
     */
    static final int YEARS_DIFFERENCE = 1911;

    /**
     * 受限构造函数。
     */
    private MinguoChronology() {
    }

    //-----------------------------------------------------------------------
    /**
     * 获取历法的 ID - 'Minguo'。
     * <p>
     * 该 ID 唯一标识 {@code Chronology}。
     * 可以使用 {@link Chronology#of(String)} 查找 {@code Chronology}。
     *
     * @return 历法 ID - 'Minguo'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "Minguo";
    }

    /**
     * 获取底层历法系统的日历类型 - 'roc'。
     * <p>
     * 日历类型是由
     * <em>Unicode Locale Data Markup Language (LDML)</em> 规范定义的标识符。
     * 可以使用 {@link Chronology#of(String)} 查找 {@code Chronology}。
     * 也可以作为区域设置的一部分，通过
     * {@link Locale#getUnicodeLocaleType(String)} 使用键 'ca' 访问。
     *
     * @return 日历系统类型 - 'roc'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "roc";
    }

    //-----------------------------------------------------------------------
    /**
     * 从纪元、纪元年、月份和月份日字段获取民国历法系统中的本地日期。
     *
     * @param era  民国纪元，不为空
     * @param yearOfEra  纪元年
     * @param month  月份
     * @param dayOfMonth  月份日
     * @return 民国本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code MinguoEra}
     */
    @Override
    public MinguoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * 从前推年、月份和月份日字段获取民国历法系统中的本地日期。
     *
     * @param prolepticYear  前推年
     * @param month  月份
     * @param dayOfMonth  月份日
     * @return 民国本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public MinguoDate date(int prolepticYear, int month, int dayOfMonth) {
        return new MinguoDate(LocalDate.of(prolepticYear + YEARS_DIFFERENCE, month, dayOfMonth));
    }

    /**
     * 从纪元、纪元年和年份日字段获取民国历法系统中的本地日期。
     *
     * @param era  民国纪元，不为空
     * @param yearOfEra  纪元年
     * @param dayOfYear  年份日
     * @return 民国本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code MinguoEra}
     */
    @Override
    public MinguoDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * 从前推年和年份日字段获取民国历法系统中的本地日期。
     *
     * @param prolepticYear  前推年
     * @param dayOfYear  年份日
     * @return 民国本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public MinguoDate dateYearDay(int prolepticYear, int dayOfYear) {
        return new MinguoDate(LocalDate.ofYearDay(prolepticYear + YEARS_DIFFERENCE, dayOfYear));
    }

    /**
     * 从纪元日获取民国历法系统中的本地日期。
     *
     * @param epochDay  纪元日
     * @return 民国本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以返回协变类型
    public MinguoDate dateEpochDay(long epochDay) {
        return new MinguoDate(LocalDate.ofEpochDay(epochDay));
    }

    @Override
    public MinguoDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    @Override
    public MinguoDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    @Override
    public MinguoDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    @Override
    public MinguoDate date(TemporalAccessor temporal) {
        if (temporal instanceof MinguoDate) {
            return (MinguoDate) temporal;
        }
        return new MinguoDate(LocalDate.from(temporal));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<MinguoDate> localDateTime(TemporalAccessor temporal) {
        return (ChronoLocalDateTime<MinguoDate>)super.localDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<MinguoDate> zonedDateTime(TemporalAccessor temporal) {
        return (ChronoZonedDateTime<MinguoDate>)super.zonedDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<MinguoDate> zonedDateTime(Instant instant, ZoneId zone) {
        return (ChronoZonedDateTime<MinguoDate>)super.zonedDateTime(instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的年份是否为闰年。
     * <p>
     * 民国闰年与 ISO 闰年完全一致。
     * 该方法不验证传递的年份，且仅在支持的年份范围内有明确定义的结果。
     *
     * @param prolepticYear  要检查的前推年，不验证范围
     * @return 如果年份是闰年则返回 true
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear + YEARS_DIFFERENCE);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof MinguoEra == false) {
            throw new ClassCastException("Era must be MinguoEra");
        }
        return (era == MinguoEra.ROC ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public MinguoEra eraOf(int eraValue) {
        return MinguoEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(MinguoEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        switch (field) {
            case PROLEPTIC_MONTH: {
                ValueRange range = PROLEPTIC_MONTH.range();
                return ValueRange.of(range.getMinimum() - YEARS_DIFFERENCE * 12L, range.getMaximum() - YEARS_DIFFERENCE * 12L);
            }
            case YEAR_OF_ERA: {
                ValueRange range = YEAR.range();
                return ValueRange.of(1, range.getMaximum() - YEARS_DIFFERENCE, -range.getMinimum() + 1 + YEARS_DIFFERENCE);
            }
            case YEAR: {
                ValueRange range = YEAR.range();
                return ValueRange.of(range.getMinimum() - YEARS_DIFFERENCE, range.getMaximum() - YEARS_DIFFERENCE);
            }
        }
        return field.range();
    }

    //-----------------------------------------------------------------------
    @Override  // 覆盖以返回类型
    public MinguoDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (MinguoDate) super.resolveDate(fieldValues, resolverStyle);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a> 写入历法。
     * @serialData
     * <pre>
     *  out.writeByte(1);     // 标识历法
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
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
