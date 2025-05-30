
/*
 * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
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

import static java.time.chrono.JapaneseDate.MEIJI_6_ISODATE;
import static java.time.temporal.ChronoField.ERA;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import sun.util.calendar.CalendarDate;

/**
 * 日本皇历系统中的一个时代。
 * <p>
 * 日本政府定义每个时代的官方名称和开始日期。时代是连续的，它们的日期范围不重叠，
 * 因此一个时代的结束日期总是下一个时代的开始日期的前一天。
 * <p>
 * Java SE 平台支持日本政府定义的所有时代，从明治时代开始。每个时代在平台上由一个整数值和一个名称标识。
 * {@link #of(int)} 和 {@link #valueOf(String)} 方法可用于获取每个时代的 JapaneseEra 的单例实例。
 * {@link #values()} 方法返回所有支持时代的单例实例。
 * <p>
 * 为了方便，此类声明了许多公共静态最终字段，这些字段引用了 values() 方法返回的单例实例。
 *
 * @apiNote
 * 本类中声明的字段可能会随着时间的推移而演变，与 {@link #values()} 方法的结果保持一致。
 * 然而，字段与单例实例之间不一定有一对一的对应关系。
 *
 * @apiNote
 * 日本政府可能会宣布一个新时代并定义其开始日期，但不定义其官方名称。在这种情况下，
 * 代表新时代的单例实例可能返回一个在官方名称定义之前不稳定的名称。开发人员在依赖于
 * 不对应于公共静态最终字段的任何单例实例返回的名称时应谨慎。
 *
 * @implSpec
 * 本类是不可变的和线程安全的。
 *
 * @since 1.8
 */
public final class JapaneseEra
        implements Era, Serializable {

    // 从时代值到 0 基数索引的偏移值。
    // 即，getValue() + ERA_OFFSET == 0 基数索引
    static final int ERA_OFFSET = 2;

    static final sun.util.calendar.Era[] ERA_CONFIG;

    /**
     * '明治' 时代（1868-01-01 - 1912-07-29）的单例实例，其值为 -1。
     */
    public static final JapaneseEra MEIJI = new JapaneseEra(-1, LocalDate.of(1868, 1, 1));
    /**
     * '大正' 时代（1912-07-30 - 1926-12-24）的单例实例，其值为 0。
     */
    public static final JapaneseEra TAISHO = new JapaneseEra(0, LocalDate.of(1912, 7, 30));
    /**
     * '昭和' 时代（1926-12-25 - 1989-01-07）的单例实例，其值为 1。
     */
    public static final JapaneseEra SHOWA = new JapaneseEra(1, LocalDate.of(1926, 12, 25));
    /**
     * '平成' 时代（1989-01-08 - 2019-04-30）的单例实例，其值为 2。
     */
    public static final JapaneseEra HEISEI = new JapaneseEra(2, LocalDate.of(1989, 1, 8));
    /**
     * '令和' 时代（2019-05-01 - ）的单例实例，其值为 3。
     */
    private static final JapaneseEra REIWA = new JapaneseEra(3, LocalDate.of(2019, 5, 1));

    // 预定义的 JapaneseEra 常量的数量。
    // 可能会有一个由属性定义的补充时代。
    private static final int N_ERA_CONSTANTS = REIWA.getValue() + ERA_OFFSET;

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 1466499369062886794L;

    // 单例 JapaneseEra 实例的数组
    private static final JapaneseEra[] KNOWN_ERAS;

    static {
        ERA_CONFIG = JapaneseChronology.JCAL.getEras();

        KNOWN_ERAS = new JapaneseEra[ERA_CONFIG.length];
        KNOWN_ERAS[0] = MEIJI;
        KNOWN_ERAS[1] = TAISHO;
        KNOWN_ERAS[2] = SHOWA;
        KNOWN_ERAS[3] = HEISEI;
        KNOWN_ERAS[4] = REIWA;
        for (int i = N_ERA_CONSTANTS; i < ERA_CONFIG.length; i++) {
            CalendarDate date = ERA_CONFIG[i].getSinceDate();
            LocalDate isoDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
            KNOWN_ERAS[i] = new JapaneseEra(i - ERA_OFFSET + 1, isoDate);
        }
    };

    /**
     * 时代值。
     * @serial
     */
    private final transient int eraValue;

    // 时代的开始日期
    private final transient LocalDate since;

    /**
     * 创建一个实例。
     *
     * @param eraValue  验证后的时代值
     * @param since  验证后的表示时代开始日期的日期，不能为空
     */
    private JapaneseEra(int eraValue, LocalDate since) {
        this.eraValue = eraValue;
        this.since = since;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回与该 {@code JapaneseEra} 对应的 Sun 私有 Era 实例。
     *
     * @return 该 {@code JapaneseEra} 的 Sun 私有 Era 实例。
     */
    sun.util.calendar.Era getPrivateEra() {
        return ERA_CONFIG[ordinal(eraValue)];
    }

    //-----------------------------------------------------------------------
    /**
     * 从一个 {@code int} 值中获取 {@code JapaneseEra} 的实例。
     * <ul>
     * <li>值 {@code 1} 与 '昭和' 时代相关联，因为它包含 1970-01-01（ISO 日历系统）。</li>
     * <li>值 {@code -1} 和 {@code 0} 分别与两个较早的时代，明治和大正相关联。</li>
     * <li>大于 {@code 1} 的值与后来的时代相关联，从平成（{@code 2}）开始。</li>
     * </ul>
     * <p>
     * 从 {@link values()} 方法返回的每个 {@code JapaneseEra} 实例都有一个 int 值（可通过 {@link Era#getValue()} 获得），
     * 该值被此方法接受。
     *
     * @param japaneseEra  要表示的时代
     * @return {@code JapaneseEra} 单例，不为空
     * @throws DateTimeException 如果值无效
     */
    public static JapaneseEra of(int japaneseEra) {
        if (japaneseEra < MEIJI.eraValue || japaneseEra + ERA_OFFSET > KNOWN_ERAS.length) {
            throw new DateTimeException("Invalid era: " + japaneseEra);
        }
        return KNOWN_ERAS[ordinal(japaneseEra)];
    }

    /**
     * 返回具有指定名称的 {@code JapaneseEra}。
     * <p>
     * 字符串必须与时代的名称完全匹配。
     * （不允许多余的空白字符。）
     *
     * @param japaneseEra  日本时代的名称；非空
     * @return {@code JapaneseEra} 单例，从不为空
     * @throws IllegalArgumentException 如果没有具有指定名称的 JapaneseEra
     */
    public static JapaneseEra valueOf(String japaneseEra) {
        Objects.requireNonNull(japaneseEra, "japaneseEra");
        for (JapaneseEra era : KNOWN_ERAS) {
            if (era.getName().equals(japaneseEra)) {
                return era;
            }
        }
        throw new IllegalArgumentException("japaneseEra is invalid");
    }

    /**
     * 返回一个 JapaneseEras 的数组。
     * <p>
     * 可以使用此方法遍历 JapaneseEras，如下所示：
     * <pre>
     * for (JapaneseEra c : JapaneseEra.values())
     *     System.out.println(c);
     * </pre>
     *
     * @return 一个 JapaneseEras 的数组
     */
    public static JapaneseEra[] values() {
        return Arrays.copyOf(KNOWN_ERAS, KNOWN_ERAS.length);
    }

    /**
     * {@inheritDoc}
     *
     * @param style {@inheritDoc}
     * @param locale {@inheritDoc}
     */
    @Override
    public String getDisplayName(TextStyle style, Locale locale) {
        // 如果这个 JapaneseEra 是一个补充的，从时代定义中获取名称。
        if (getValue() > N_ERA_CONSTANTS - ERA_OFFSET) {
            Objects.requireNonNull(locale, "locale");
            return style.asNormal() == TextStyle.NARROW ? getAbbreviation() : getName();
        }

        return new DateTimeFormatterBuilder()
            .appendText(ERA, style)
            .toFormatter(locale)
            .withChronology(JapaneseChronology.INSTANCE)
            .format(this == MEIJI ? MEIJI_6_ISODATE : since);
    }

    //-----------------------------------------------------------------------
    /**
     * 从日期中获取 {@code JapaneseEra} 的实例。
     *
     * @param date  日期，不能为空
     * @return Era 单例，从不为空
     */
    static JapaneseEra from(LocalDate date) {
        if (date.isBefore(MEIJI_6_ISODATE)) {
            throw new DateTimeException("JapaneseDate before Meiji 6 are not supported");
        }
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            JapaneseEra era = KNOWN_ERAS[i];
            if (date.compareTo(era.since) >= 0) {
                return era;
            }
        }
        return null;
    }

    static JapaneseEra toJapaneseEra(sun.util.calendar.Era privateEra) {
        for (int i = ERA_CONFIG.length - 1; i >= 0; i--) {
            if (ERA_CONFIG[i].equals(privateEra)) {
                return KNOWN_ERAS[i];
            }
        }
        return null;
    }

    static sun.util.calendar.Era privateEraFrom(LocalDate isoDate) {
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            JapaneseEra era = KNOWN_ERAS[i];
            if (isoDate.compareTo(era.since) >= 0) {
                return ERA_CONFIG[i];
            }
        }
        return null;
    }

    /**
     * 从 Era 值返回数组中的索引。
     * eraValue 是一个有效的 Era 编号，-1..2。
     *
     * @param eraValue  要转换为索引的 Era 值
     * @return 当前 Era 的索引
     */
    private static int ordinal(int eraValue) {
        return eraValue + ERA_OFFSET - 1;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值的范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此时代用于提高返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * {@code ERA} 字段返回范围。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 是否可以获取范围由字段决定。
     * <p>
     * 由于日本日历系统的性质，有效日本时代的范围可能会随着时间的推移而变化。
     *
     * @param field  要查询范围的字段，不能为空
     * @return 字段的有效值范围，不能为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    @Override  // override as super would return range from 0 to 1
    public ValueRange range(TemporalField field) {
        if (field == ERA) {
            return JapaneseChronology.INSTANCE.range(ERA);
        }
        return Era.super.range(field);
    }


                //-----------------------------------------------------------------------
    String getAbbreviation() {
        return ERA_CONFIG[ordinal(getValue())].getAbbreviation();
    }

    String getName() {
        return ERA_CONFIG[ordinal(getValue())].getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    //-----------------------------------------------------------------------
    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化委托进行反序列化");
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.chrono.Ser">专用序列化形式</a>
     * 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(5);        // 标识一个 JapaneseEra
     *  out.writeInt(getValue());
     * </pre>
     *
     * @return {@code Ser} 的实例，不为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.JAPANESE_ERA_TYPE, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeByte(this.getValue());
    }

    static JapaneseEra readExternal(DataInput in) throws IOException {
        byte eraValue = in.readByte();
        return JapaneseEra.of(eraValue);
    }

}
