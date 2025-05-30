
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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sun.util.calendar.CalendarSystem;
import sun.util.calendar.LocalGregorianCalendar;

/**
 * 日本皇历系统。
 * <p>
 * 该编年系统定义了日本皇历系统的规则。
 * 该日历系统主要在日本使用。
 * 日本皇历系统与ISO日历系统相同，除了基于时代的年份编号。
 * <p>
 * 日本从明治6年开始引入公历。
 * 只支持明治及以后的时代；
 * 明治6年1月1日之前的日子不受支持。
 * <p>
 * 支持的 {@code ChronoField} 实例包括：
 * <ul>
 * <li>{@code DAY_OF_WEEK}
 * <li>{@code DAY_OF_MONTH}
 * <li>{@code DAY_OF_YEAR}
 * <li>{@code EPOCH_DAY}
 * <li>{@code MONTH_OF_YEAR}
 * <li>{@code PROLEPTIC_MONTH}
 * <li>{@code YEAR_OF_ERA}
 * <li>{@code YEAR}
 * <li>{@code ERA}
 * </ul>
 *
 * @implSpec
 * 该类是不可变的，并且是线程安全的。
 *
 * @since 1.8
 */
public final class JapaneseChronology extends AbstractChronology implements Serializable {

    static final LocalGregorianCalendar JCAL =
        (LocalGregorianCalendar) CalendarSystem.forName("japanese");

    // 用于创建 JapaneseImpericalCalendar 的区域设置。
    static final Locale LOCALE = Locale.forLanguageTag("ja-JP-u-ca-japanese");

    /**
     * 日本编年系统的单例实例。
     */
    public static final JapaneseChronology INSTANCE = new JapaneseChronology();

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 459996390165777884L;

    //-----------------------------------------------------------------------
    /**
     * 获取编年系统的ID - 'Japanese'。
     * <p>
     * ID 唯一标识了 {@code Chronology}。
     * 可以使用 {@link Chronology#of(String)} 通过 ID 查找 {@code Chronology}。
     *
     * @return 编年系统ID - 'Japanese'
     * @see #getCalendarType()
     */
    @Override
    public String getId() {
        return "Japanese";
    }

    /**
     * 获取底层日历系统的日历类型 - 'japanese'。
     * <p>
     * 日历类型是由
     * <em>Unicode Locale Data Markup Language (LDML)</em> 规范定义的标识符。
     * 可以使用 {@link Chronology#of(String)} 通过日历类型查找 {@code Chronology}。
     * 也可以作为区域设置的一部分，通过
     * {@link Locale#getUnicodeLocaleType(String)} 使用键 'ca' 访问。
     *
     * @return 日历系统类型 - 'japanese'
     * @see #getId()
     */
    @Override
    public String getCalendarType() {
        return "japanese";
    }

    //-----------------------------------------------------------------------
    /**
     * 从时代、年份、月份和日期字段中获取日本日历系统中的本地日期。
     * <p>
     * 日本的月份和日期与 ISO 日历系统中的相同。它们在时代变化时不会重置。
     * 例如：
     * <pre>
     *  6th Jan Showa 64 = ISO 1989-01-06
     *  7th Jan Showa 64 = ISO 1989-01-07
     *  8th Jan Heisei 1 = ISO 1989-01-08
     *  9th Jan Heisei 1 = ISO 1989-01-09
     * </pre>
     *
     * @param era  日本时代，不为空
     * @param yearOfEra  年份
     * @param month  月份
     * @param dayOfMonth  日期
     * @return 日本本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code JapaneseEra}
     */
    @Override
    public JapaneseDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        if (era instanceof JapaneseEra == false) {
            throw new ClassCastException("Era must be JapaneseEra");
        }
        return JapaneseDate.of((JapaneseEra) era, yearOfEra, month, dayOfMonth);
    }

    /**
     * 从历法年、月份和日期字段中获取日本日历系统中的本地日期。
     * <p>
     * 日本的历法年、月份和日期与 ISO 日历系统中的相同。它们在时代变化时不会重置。
     *
     * @param prolepticYear  历法年
     * @param month  月份
     * @param dayOfMonth  日期
     * @return 日本本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public JapaneseDate date(int prolepticYear, int month, int dayOfMonth) {
        return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    /**
     * 从时代、年份和年中的日期字段中获取日本日历系统中的本地日期。
     * <p>
     * 该工厂中的年中的日期相对于该年份的开始表达。
     * 仅在那些由于时代变化而将年份重置为一的年份中，年中的日期的正常含义才会改变。
     * 例如：
     * <pre>
     *  6th Jan Showa 64 = 年中的第6天
     *  7th Jan Showa 64 = 年中的第7天
     *  8th Jan Heisei 1 = 年中的第1天
     *  9th Jan Heisei 1 = 年中的第2天
     * </pre>
     *
     * @param era  日本时代，不为空
     * @param yearOfEra  年份
     * @param dayOfYear  年中的日期
     * @return 日本本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     * @throws ClassCastException 如果 {@code era} 不是 {@code JapaneseEra}
     */
    @Override
    public JapaneseDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return JapaneseDate.ofYearDay((JapaneseEra) era, yearOfEra, dayOfYear);
    }

    /**
     * 从历法年和年中的日期字段中获取日本日历系统中的本地日期。
     * <p>
     * 该工厂中的年中的日期相对于历法年的开始表达。
     * 日本的历法年和年中的日期与 ISO 日历系统中的相同。它们在时代变化时不会重置。
     *
     * @param prolepticYear  历法年
     * @param dayOfYear  年中的日期
     * @return 日本本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override
    public JapaneseDate dateYearDay(int prolepticYear, int dayOfYear) {
        return new JapaneseDate(LocalDate.ofYearDay(prolepticYear, dayOfYear));
    }

    /**
     * 从纪元日获取日本日历系统中的本地日期。
     *
     * @param epochDay  纪元日
     * @return 日本本地日期，不为空
     * @throws DateTimeException 如果无法创建日期
     */
    @Override  // 重写以返回协变类型
    public JapaneseDate dateEpochDay(long epochDay) {
        return new JapaneseDate(LocalDate.ofEpochDay(epochDay));
    }

    @Override
    public JapaneseDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    @Override
    public JapaneseDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    @Override
    public JapaneseDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    @Override
    public JapaneseDate date(TemporalAccessor temporal) {
        if (temporal instanceof JapaneseDate) {
            return (JapaneseDate) temporal;
        }
        return new JapaneseDate(LocalDate.from(temporal));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<JapaneseDate> localDateTime(TemporalAccessor temporal) {
        return (ChronoLocalDateTime<JapaneseDate>)super.localDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(TemporalAccessor temporal) {
        return (ChronoZonedDateTime<JapaneseDate>)super.zonedDateTime(temporal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(Instant instant, ZoneId zone) {
        return (ChronoZonedDateTime<JapaneseDate>)super.zonedDateTime(instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的年份是否为闰年。
     * <p>
     * 日本日历的闰年与 ISO 闰年完全一致。
     * 该方法不会验证传递的年份，只有在支持的范围内才有明确定义的结果。
     *
     * @param prolepticYear  要检查的历法年，未验证范围
     * @return 如果年份是闰年则返回 true
     */
    @Override
    public boolean isLeapYear(long prolepticYear) {
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof JapaneseEra == false) {
            throw new ClassCastException("Era must be JapaneseEra");
        }

        JapaneseEra jera = (JapaneseEra) era;
        int gregorianYear = jera.getPrivateEra().getSinceDate().getYear() + yearOfEra - 1;
        if (yearOfEra == 1) {
            return gregorianYear;
        }
        if (gregorianYear >= Year.MIN_VALUE && gregorianYear <= Year.MAX_VALUE) {
            LocalGregorianCalendar.Date jdate = JCAL.newCalendarDate(null);
            jdate.setEra(jera.getPrivateEra()).setDate(yearOfEra, 1, 1);
            if (JapaneseChronology.JCAL.validate(jdate)) {
                return gregorianYear;
            }
        }
        throw new DateTimeException("Invalid yearOfEra value");
    }

    /**
     * 从给定的数值返回日历系统时代对象。
     *
     * 请参阅每个时代的描述，了解以下数值：
     * {@link JapaneseEra#HEISEI}, {@link JapaneseEra#SHOWA}, {@link JapaneseEra#TAISHO},
     * {@link JapaneseEra#MEIJI}，仅支持明治及以后的时代。
     *
     * @param eraValue  时代值
     * @return 给定数值时代的日本 {@code Era}
     * @throws DateTimeException 如果 {@code eraValue} 无效
     */
    @Override
    public JapaneseEra eraOf(int eraValue) {
        return JapaneseEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return Arrays.<Era>asList(JapaneseEra.values());
    }

    JapaneseEra getCurrentEra() {
        // 假设最后一个 JapaneseEra 是当前的。
        JapaneseEra[] eras = JapaneseEra.values();
        return eras[eras.length - 1];
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueRange range(ChronoField field) {
        switch (field) {
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
            case ALIGNED_WEEK_OF_MONTH:
            case ALIGNED_WEEK_OF_YEAR:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            case YEAR_OF_ERA: {
                Calendar jcal = Calendar.getInstance(LOCALE);
                int startYear = getCurrentEra().getPrivateEra().getSinceDate().getYear();
                return ValueRange.of(1, jcal.getGreatestMinimum(Calendar.YEAR),
                        jcal.getLeastMaximum(Calendar.YEAR) + 1, // +1 由于不同的定义
                        Year.MAX_VALUE - startYear);
            }
            case DAY_OF_YEAR: {
                Calendar jcal = Calendar.getInstance(LOCALE);
                int fieldIndex = Calendar.DAY_OF_YEAR;
                return ValueRange.of(jcal.getMinimum(fieldIndex), jcal.getGreatestMinimum(fieldIndex),
                        jcal.getLeastMaximum(fieldIndex), jcal.getMaximum(fieldIndex));
            }
            case YEAR:
                return ValueRange.of(JapaneseDate.MEIJI_6_ISODATE.getYear(), Year.MAX_VALUE);
            case ERA:
                return ValueRange.of(JapaneseEra.MEIJI.getValue(), getCurrentEra().getValue());
            default:
                return field.range();
        }
    }


                //-----------------------------------------------------------------------
    @Override  // 重写以返回类型
    public JapaneseDate resolveDate(Map <TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (JapaneseDate) super.resolveDate(fieldValues, resolverStyle);
    }

    @Override  // 重写以实现特殊的日本行为
    ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        // 验证纪元和纪元年
        Long eraLong = fieldValues.get(ERA);
        JapaneseEra era = null;
        if (eraLong != null) {
            era = eraOf(range(ERA).checkValidIntValue(eraLong, ERA));  // 始终验证
        }
        Long yoeLong = fieldValues.get(YEAR_OF_ERA);
        int yoe = 0;
        if (yoeLong != null) {
            yoe = range(YEAR_OF_ERA).checkValidIntValue(yoeLong, YEAR_OF_ERA);  // 始终验证
        }
        // 如果只有纪元年且没有年份，则除非严格模式，否则创建纪元
        if (era == null && yoeLong != null && fieldValues.containsKey(YEAR) == false && resolverStyle != ResolverStyle.STRICT) {
            era = JapaneseEra.values()[JapaneseEra.values().length - 1];
        }
        // 如果两者都存在，则尝试创建日期
        if (yoeLong != null && era != null) {
            if (fieldValues.containsKey(MONTH_OF_YEAR)) {
                if (fieldValues.containsKey(DAY_OF_MONTH)) {
                    return resolveYMD(era, yoe, fieldValues, resolverStyle);
                }
            }
            if (fieldValues.containsKey(DAY_OF_YEAR)) {
                return resolveYD(era, yoe, fieldValues, resolverStyle);
            }
        }
        return null;
    }

    private int prolepticYearLenient(JapaneseEra era, int yearOfEra) {
        return era.getPrivateEra().getSinceDate().getYear() + yearOfEra - 1;
    }

     private ChronoLocalDate resolveYMD(JapaneseEra era, int yoe, Map<TemporalField,Long> fieldValues, ResolverStyle resolverStyle) {
         fieldValues.remove(ERA);
         fieldValues.remove(YEAR_OF_ERA);
         if (resolverStyle == ResolverStyle.LENIENT) {
             int y = prolepticYearLenient(era, yoe);
             long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
             long days = Math.subtractExact(fieldValues.remove(DAY_OF_MONTH), 1);
             return date(y, 1, 1).plus(months, MONTHS).plus(days, DAYS);
         }
         int moy = range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR), MONTH_OF_YEAR);
         int dom = range(DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(DAY_OF_MONTH), DAY_OF_MONTH);
         if (resolverStyle == ResolverStyle.SMART) {  // 前一个有效
             if (yoe < 1) {
                 throw new DateTimeException("无效的纪元年: " + yoe);
             }
             int y = prolepticYearLenient(era, yoe);
             JapaneseDate result;
             try {
                 result = date(y, moy, dom);
             } catch (DateTimeException ex) {
                 result = date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
             }
             // 处理纪元被更改的情况
             // 只允许新日期在与纪元更改相同的1月至12月内
             // 通过确保原始纪元年或结果纪元年为1来确定
             if (result.getEra() != era && result.get(YEAR_OF_ERA) > 1 && yoe > 1) {
                 throw new DateTimeException("纪元 " + era + " 的无效纪元年: " + yoe);
             }
             return result;
         }
         return date(era, yoe, moy, dom);
     }

    private ChronoLocalDate resolveYD(JapaneseEra era, int yoe, Map <TemporalField,Long> fieldValues, ResolverStyle resolverStyle) {
        fieldValues.remove(ERA);
        fieldValues.remove(YEAR_OF_ERA);
        if (resolverStyle == ResolverStyle.LENIENT) {
            int y = prolepticYearLenient(era, yoe);
            long days = Math.subtractExact(fieldValues.remove(DAY_OF_YEAR), 1);
            return dateYearDay(y, 1).plus(days, DAYS);
        }
        int doy = range(DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(DAY_OF_YEAR), DAY_OF_YEAR);
        return dateYearDay(era, yoe, doy);  // smart 与 strict 相同
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a>
     * 写入历法。
     * @serialData
     * <pre>
     *  out.writeByte(1);     // 识别历法
     *  out.writeUTF(getId());
     * </pre>
     *
     * @return {@code Ser} 的实例，不为 null
     */
    @Override
    Object writeReplace() {
        return super.writeReplace();
    }

    /**
     * 防止恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }
}
