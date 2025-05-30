
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 泰国佛教历法系统。
 * <p>
 * 该历法定义了泰国佛教历法系统的规则。
 * 该历法主要在泰国使用。
 * 日期对齐方式为 {@code 2484-01-01 (佛教)} 对应 {@code 1941-01-01 (ISO)}。
 * <p>
 * 字段定义如下：
 * <ul>
 * <li>纪元 - 有两个纪元，当前的“佛教”（ERA_BE）和之前的纪元（ERA_BEFORE_BE）。
 * <li>纪元年 - 当前纪元的纪元年从纪元开始逐年递增。
 *  对于之前的纪元，年份随时间倒退而递增。
 *  当前纪元的值等于 ISO 前推年加上 543。
 * <li>前推年 - 当前纪元的前推年与纪元年相同。
 *  对于之前的纪元，年份有零或负值。
 *  值等于 ISO 前推年加上 543。
 * <li>年中的月 - 泰国佛教历法的年中的月与 ISO 完全一致。
 * <li>月中的日 - 泰国佛教历法的月中的日与 ISO 完全一致。
 * <li>年中的日 - 泰国佛教历法的年中的日与 ISO 完全一致。
 * <li>闰年 - 泰国佛教历法的闰年模式与 ISO 完全一致，使得两个历法从不脱节。
 * </ul>
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @since 1.8
 */
public final class ThaiBuddhistChronology extends AbstractChronology implements Serializable {

    /**
     * 佛教历法的单例实例。
     */
    public static final ThaiBuddhistChronology INSTANCE = new ThaiBuddhistChronology();

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 2775954514031616474L;
    /**
     * 存储添加到 ISO 年的偏移量。
     */
    static final int YEARS_DIFFERENCE = 543;
    /**
     * 纪元的窄名称。
     */
    private static final HashMap<String, String[]> ERA_NARROW_NAMES = new HashMap<>();
    /**
     * 纪元的短名称。
     */
    private static final HashMap<String, String[]> ERA_SHORT_NAMES = new HashMap<>();
    /**
     * 纪元的全名称。
     */
    private static final HashMap<String, String[]> ERA_FULL_NAMES = new HashMap<>();
    /**
     * 纪元名称的备用语言。
     */
    private static final String FALLBACK_LANGUAGE = "en";
    /**
     * 纪元名称的语言。
     */
    private static final String TARGET_LANGUAGE = "th";
    /**
     * 名称数据。
     */
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"BB", "BE"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"BB", "BE"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"B.B.", "B.E."});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e.\u0e28.",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Before Buddhist", "Budhhist Era"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e\u0e38\u0e17\u0e18\u0e28\u0e31\u0e01\u0e23\u0e32\u0e0a",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
    }

    /**
     * 受限构造函数。
     */
    private ThaiBuddhistChronology() {
    }

    //-----------------------------------------------------------------------
    /**
     * 获取历法的 ID - 'ThaiBuddhist'。
     * <p>
     * 该 ID 唯一标识 {@code Chronology}。
     * 可以使用 {@link Chronology#of(String)} 查找 {@code Chronology}。
     *
     * @return 历法 ID - 'ThaiBuddhist'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "ThaiBuddhist";
    }

    /**
     * 获取底层历法系统的日历类型 - 'buddhist'。
     * <p>
     * 日历类型是由
     * <em>Unicode Locale Data Markup Language (LDML)</em> 规范定义的标识符。
     * 可以使用 {@link Chronology#of(String)} 查找 {@code Chronology}。
     * 也可以作为区域设置的一部分，通过
     * {@link Locale#getUnicodeLocaleType(String)} 使用键 'ca' 访问。
     *
     * @return 日历系统类型 - 'buddhist'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "buddhist";
    }

    //-----------------------------------------------------------------------
    /**
     * 从纪元、纪元年、年中的月和月中的日字段获取泰国佛教历法的本地日期。
     *
     * @param era  泰国佛教纪元，不为空
     * @param yearOfEra  纪元年
     * @param month  年中的月
     * @param dayOfMonth  月中的日
     * @return 泰国佛教本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code ThaiBuddhistEra}
     */
    @Override
    public ThaiBuddhistDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * 从前推年、年中的月和月中的日字段获取泰国佛教历法的本地日期。
     *
     * @param prolepticYear  前推年
     * @param month  年中的月
     * @param dayOfMonth  月中的日
     * @return 泰国佛教本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public ThaiBuddhistDate date(int prolepticYear, int month, int dayOfMonth) {
        return new ThaiBuddhistDate(LocalDate.of(prolepticYear - YEARS_DIFFERENCE, month, dayOfMonth));
    }

    /**
     * 从纪元、纪元年和年中的日字段获取泰国佛教历法的本地日期。
     *
     * @param era  泰国佛教纪元，不为空
     * @param yearOfEra  纪元年
     * @param dayOfYear  年中的日
     * @return 泰国佛教本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code ThaiBuddhistEra}
     */
    @Override
    public ThaiBuddhistDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * 从前推年和年中的日字段获取泰国佛教历法的本地日期。
     *
     * @param prolepticYear  前推年
     * @param dayOfYear  年中的日
     * @return 泰国佛教本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public ThaiBuddhistDate dateYearDay(int prolepticYear, int dayOfYear) {
        return new ThaiBuddhistDate(LocalDate.ofYearDay(prolepticYear - YEARS_DIFFERENCE, dayOfYear));
    }

    /**
     * 从纪元日获取泰国佛教历法的本地日期。
     *
     * @param epochDay  纪元日
     * @return 泰国佛教本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 覆盖以协变返回类型
    public ThaiBuddhistDate dateEpochDay(long epochDay) {
        return new ThaiBuddhistDate(LocalDate.ofEpochDay(epochDay));
    }

    @Override
    public ThaiBuddhistDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    @Override
    public ThaiBuddhistDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    @Override
    public ThaiBuddhistDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    @Override
    public ThaiBuddhistDate date(TemporalAccessor temporal) {
        if (temporal instanceof ThaiBuddhistDate) {
            return (ThaiBuddhistDate) temporal;
        }
        return new ThaiBuddhistDate(LocalDate.from(temporal));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<ThaiBuddhistDate> localDateTime(TemporalAccessor temporal) {
        return (ChronoLocalDateTime<ThaiBuddhistDate>)super.localDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<ThaiBuddhistDate> zonedDateTime(TemporalAccessor temporal) {
        return (ChronoZonedDateTime<ThaiBuddhistDate>)super.zonedDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<ThaiBuddhistDate> zonedDateTime(Instant instant, ZoneId zone) {
        return (ChronoZonedDateTime<ThaiBuddhistDate>)super.zonedDateTime(instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的年份是否为闰年。
     * <p>
     * 泰国佛教闰年与 ISO 闰年完全一致。
     * 该方法不会验证传递的年份，并且仅在支持的范围内有明确定义的结果。
     *
     * @param prolepticYear  要检查的前推年，不验证范围
     * @return 如果年份是闰年则返回 true
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear - YEARS_DIFFERENCE);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof ThaiBuddhistEra == false) {
            throw new ClassCastException("Era must be BuddhistEra");
        }
        return (era == ThaiBuddhistEra.BE ? yearOfEra : 1 - yearOfEra);
    }

    @Override
    public ThaiBuddhistEra eraOf(int eraValue) {
        return ThaiBuddhistEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(ThaiBuddhistEra.values());
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        switch (field) {
            case PROLEPTIC_MONTH: {
                ValueRange range = PROLEPTIC_MONTH.range();
                return ValueRange.of(range.getMinimum() + YEARS_DIFFERENCE * 12L, range.getMaximum() + YEARS_DIFFERENCE * 12L);
            }
            case YEAR_OF_ERA: {
                ValueRange range = YEAR.range();
                return ValueRange.of(1, -(range.getMinimum() + YEARS_DIFFERENCE) + 1, range.getMaximum() + YEARS_DIFFERENCE);
            }
            case YEAR: {
                ValueRange range = YEAR.range();
                return ValueRange.of(range.getMinimum() + YEARS_DIFFERENCE, range.getMaximum() + YEARS_DIFFERENCE);
            }
        }
        return field.range();
    }

    //-----------------------------------------------------------------------
    @Override  // 覆盖以协变返回类型
    public ThaiBuddhistDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (ThaiBuddhistDate) super.resolveDate(fieldValues, resolverStyle);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a> 写入历法。
     * @serialData
     * <pre>
     *  out.writeByte(1);     // 标识一个历法
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
