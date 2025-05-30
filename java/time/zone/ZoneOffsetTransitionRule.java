
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
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.zone;

import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.util.Objects;

/**
 * 一个规则，表达如何创建转换。
 * <p>
 * 该类允许表达未来转换的规则。
 * 一个规则可以以多种形式表示：
 * <ul>
 * <li>3月16日
 * <li>3月16日或之后的星期日
 * <li>3月16日或之前的星期日
 * <li>2月最后一个星期日
 * </ul>
 * 这些不同类型的规则可以被表达和查询。
 *
 * @implSpec
 * 该类是不可变的和线程安全的。
 *
 * @since 1.8
 */
public final class ZoneOffsetTransitionRule implements Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 6889046316657758795L;

    /**
     * 转换周第一天的月日的月份。
     * 实际日期将由dowChange字段调整。
     */
    private final Month month;
    /**
     * 转换周第一天的月日的日期。
     * 如果为正数，表示转换可以发生的那一周的开始。
     * 如果为负数，表示转换可以发生的那一周的结束。
     * 该值是从月份末尾计算的天数，例如
     * {@code -1} 是月份的最后一天，{@code -2} 是倒数第二天，依此类推。
     */
    private final byte dom;
    /**
     * 转换的星期几，如果不需要更改月日，则为null。
     */
    private final DayOfWeek dow;
    /**
     * 转换前的偏移时间。
     */
    private final LocalTime time;
    /**
     * 转换时间是否为午夜。
     */
    private final boolean timeEndOfDay;
    /**
     * 本地时间应如何解释的定义。
     */
    private final TimeDefinition timeDefinition;
    /**
     * 转换时的标准偏移。
     */
    private final ZoneOffset standardOffset;
    /**
     * 转换前的偏移。
     */
    private final ZoneOffset offsetBefore;
    /**
     * 转换后的偏移。
     */
    private final ZoneOffset offsetAfter;

    /**
     * 获取定义每年规则以在两个偏移之间创建转换的实例。
     * <p>
     * 应用程序通常应从 {@link ZoneRules} 获取实例。
     * 该工厂仅用于创建 {@link ZoneRules}。
     *
     * @param month  转换周第一天的月日的月份，不为null
     * @param dayOfMonthIndicator  转换周第一天的月日的日期，如果该周是该日期或更晚，则为正数，如果该周是该日期或更早，则为负数，从月份末尾计算，范围从 -28 到 31，不包括 0
     * @param dayOfWeek  需要的星期几，如果不需要更改月日，则为null
     * @param time  转换前的偏移时间，不为null
     * @param timeEndOfDay  时间是否为午夜
     * @param timeDefnition  如何解释转换
     * @param standardOffset  转换时的标准偏移，不为null
     * @param offsetBefore  转换前的偏移，不为null
     * @param offsetAfter  转换后的偏移，不为null
     * @return 规则，不为null
     * @throws IllegalArgumentException 如果月日指示器无效
     * @throws IllegalArgumentException 如果结束日标志为true且时间不是午夜
     */
    public static ZoneOffsetTransitionRule of(
            Month month,
            int dayOfMonthIndicator,
            DayOfWeek dayOfWeek,
            LocalTime time,
            boolean timeEndOfDay,
            TimeDefinition timeDefnition,
            ZoneOffset standardOffset,
            ZoneOffset offsetBefore,
            ZoneOffset offsetAfter) {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(timeDefnition, "timeDefnition");
        Objects.requireNonNull(standardOffset, "standardOffset");
        Objects.requireNonNull(offsetBefore, "offsetBefore");
        Objects.requireNonNull(offsetAfter, "offsetAfter");
        if (dayOfMonthIndicator < -28 || dayOfMonthIndicator > 31 || dayOfMonthIndicator == 0) {
            throw new IllegalArgumentException("月日指示器必须在 -28 到 31 之间，不包括 0");
        }
        if (timeEndOfDay && time.equals(LocalTime.MIDNIGHT) == false) {
            throw new IllegalArgumentException("如果结束日标志为true，时间必须为午夜");
        }
        return new ZoneOffsetTransitionRule(month, dayOfMonthIndicator, dayOfWeek, time, timeEndOfDay, timeDefnition, standardOffset, offsetBefore, offsetAfter);
    }

    /**
     * 创建定义每年规则以在两个偏移之间创建转换的实例。
     *
     * @param month  转换周第一天的月日的月份，不为null
     * @param dayOfMonthIndicator  转换周第一天的月日的日期，如果该周是该日期或更晚，则为正数，如果该周是该日期或更早，则为负数，从月份末尾计算，范围从 -28 到 31，不包括 0
     * @param dayOfWeek  需要的星期几，如果不需要更改月日，则为null
     * @param time  转换前的偏移时间，不为null
     * @param timeEndOfDay  时间是否为午夜
     * @param timeDefnition  如何解释转换
     * @param standardOffset  转换时的标准偏移，不为null
     * @param offsetBefore  转换前的偏移，不为null
     * @param offsetAfter  转换后的偏移，不为null
     * @throws IllegalArgumentException 如果月日指示器无效
     * @throws IllegalArgumentException 如果结束日标志为true且时间不是午夜
     */
    ZoneOffsetTransitionRule(
            Month month,
            int dayOfMonthIndicator,
            DayOfWeek dayOfWeek,
            LocalTime time,
            boolean timeEndOfDay,
            TimeDefinition timeDefnition,
            ZoneOffset standardOffset,
            ZoneOffset offsetBefore,
            ZoneOffset offsetAfter) {
        this.month = month;
        this.dom = (byte) dayOfMonthIndicator;
        this.dow = dayOfWeek;
        this.time = time;
        this.timeEndOfDay = timeEndOfDay;
        this.timeDefinition = timeDefnition;
        this.standardOffset = standardOffset;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    //-----------------------------------------------------------------------
    /**
     * 防止恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    /**
     * 使用
     * <a href="../../../serialized-form.html#java.time.zone.Ser">专用序列化形式</a>
     * 写入对象。
     * @serialData
     * 参考
     * <a href="../../../serialized-form.html#java.time.zone.ZoneRules">ZoneRules.writeReplace</a>
     * 的序列化形式，了解纪元秒和偏移的编码。
     * <pre style="font-size:1.0em">{@code
     *
     *      out.writeByte(3);                // 标识 ZoneOffsetTransition
     *      final int timeSecs = (timeEndOfDay ? 86400 : time.toSecondOfDay());
     *      final int stdOffset = standardOffset.getTotalSeconds();
     *      final int beforeDiff = offsetBefore.getTotalSeconds() - stdOffset;
     *      final int afterDiff = offsetAfter.getTotalSeconds() - stdOffset;
     *      final int timeByte = (timeSecs % 3600 == 0 ? (timeEndOfDay ? 24 : time.getHour()) : 31);
     *      final int stdOffsetByte = (stdOffset % 900 == 0 ? stdOffset / 900 + 128 : 255);
     *      final int beforeByte = (beforeDiff == 0 || beforeDiff == 1800 || beforeDiff == 3600 ? beforeDiff / 1800 : 3);
     *      final int afterByte = (afterDiff == 0 || afterDiff == 1800 || afterDiff == 3600 ? afterDiff / 1800 : 3);
     *      final int dowByte = (dow == null ? 0 : dow.getValue());
     *      int b = (month.getValue() << 28) +          // 4 位
     *              ((dom + 32) << 22) +                // 6 位
     *              (dowByte << 19) +                   // 3 位
     *              (timeByte << 14) +                  // 5 位
     *              (timeDefinition.ordinal() << 12) +  // 2 位
     *              (stdOffsetByte << 4) +              // 8 位
     *              (beforeByte << 2) +                 // 2 位
     *              afterByte;                          // 2 位
     *      out.writeInt(b);
     *      if (timeByte == 31) {
     *          out.writeInt(timeSecs);
     *      }
     *      if (stdOffsetByte == 255) {
     *          out.writeInt(stdOffset);
     *      }
     *      if (beforeByte == 3) {
     *          out.writeInt(offsetBefore.getTotalSeconds());
     *      }
     *      if (afterByte == 3) {
     *          out.writeInt(offsetAfter.getTotalSeconds());
     *      }
     * }
     * </pre>
     *
     * @return 替代对象，不为null
     */
    private Object writeReplace() {
        return new Ser(Ser.ZOTRULE, this);
    }

    /**
     * 将状态写入流。
     *
     * @param out 输出流，不为null
     * @throws IOException 如果发生错误
     */
    void writeExternal(DataOutput out) throws IOException {
        final int timeSecs = (timeEndOfDay ? 86400 : time.toSecondOfDay());
        final int stdOffset = standardOffset.getTotalSeconds();
        final int beforeDiff = offsetBefore.getTotalSeconds() - stdOffset;
        final int afterDiff = offsetAfter.getTotalSeconds() - stdOffset;
        final int timeByte = (timeSecs % 3600 == 0 ? (timeEndOfDay ? 24 : time.getHour()) : 31);
        final int stdOffsetByte = (stdOffset % 900 == 0 ? stdOffset / 900 + 128 : 255);
        final int beforeByte = (beforeDiff == 0 || beforeDiff == 1800 || beforeDiff == 3600 ? beforeDiff / 1800 : 3);
        final int afterByte = (afterDiff == 0 || afterDiff == 1800 || afterDiff == 3600 ? afterDiff / 1800 : 3);
        final int dowByte = (dow == null ? 0 : dow.getValue());
        int b = (month.getValue() << 28) +          // 4 位
                ((dom + 32) << 22) +                // 6 位
                (dowByte << 19) +                   // 3 位
                (timeByte << 14) +                  // 5 位
                (timeDefinition.ordinal() << 12) +  // 2 位
                (stdOffsetByte << 4) +              // 8 位
                (beforeByte << 2) +                 // 2 位
                afterByte;                          // 2 位
        out.writeInt(b);
        if (timeByte == 31) {
            out.writeInt(timeSecs);
        }
        if (stdOffsetByte == 255) {
            out.writeInt(stdOffset);
        }
        if (beforeByte == 3) {
            out.writeInt(offsetBefore.getTotalSeconds());
        }
        if (afterByte == 3) {
            out.writeInt(offsetAfter.getTotalSeconds());
        }
    }

    /**
     * 从流中读取状态。
     *
     * @param in 输入流，不为null
     * @return 创建的对象，不为null
     * @throws IOException 如果发生错误
     */
    static ZoneOffsetTransitionRule readExternal(DataInput in) throws IOException {
        int data = in.readInt();
        Month month = Month.of(data >>> 28);
        int dom = ((data & (63 << 22)) >>> 22) - 32;
        int dowByte = (data & (7 << 19)) >>> 19;
        DayOfWeek dow = dowByte == 0 ? null : DayOfWeek.of(dowByte);
        int timeByte = (data & (31 << 14)) >>> 14;
        TimeDefinition defn = TimeDefinition.values()[(data & (3 << 12)) >>> 12];
        int stdByte = (data & (255 << 4)) >>> 4;
        int beforeByte = (data & (3 << 2)) >>> 2;
        int afterByte = (data & 3);
        LocalTime time = (timeByte == 31 ? LocalTime.ofSecondOfDay(in.readInt()) : LocalTime.of(timeByte % 24, 0));
        ZoneOffset std = (stdByte == 255 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds((stdByte - 128) * 900));
        ZoneOffset before = (beforeByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + beforeByte * 1800));
        ZoneOffset after = (afterByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + afterByte * 1800));
        return ZoneOffsetTransitionRule.of(month, dom, dow, time, timeByte == 24, defn, std, before, after);
    }


                //-----------------------------------------------------------------------
    /**
     * 获取转换的月份。
     * <p>
     * 如果规则定义了确切的日期，则月份是该日期的月份。
     * <p>
     * 如果规则定义了转换可能发生的一周，则月份是转换的最早或最晚可能日期的月份。
     *
     * @return 转换的月份，不为空
     */
    public Month getMonth() {
        return month;
    }

    /**
     * 获取转换的月份中的日期指示符。
     * <p>
     * 如果规则定义了确切的日期，则日期是该日期的月份。
     * <p>
     * 如果规则定义了转换可能发生的一周，则日期定义了转换周的开始或结束。
     * <p>
     * 如果值为正数，则表示正常的月份中的日期，是转换可能发生的最早日期。
     * 日期可能是2月29日，应被视为非闰年的3月1日。
     * <p>
     * 如果值为负数，则表示从月末倒数的天数，其中 {@code -1} 表示月末。
     * 在这种情况下，标识的日期是转换可能发生的最晚日期。
     *
     * @return 月份中的日期指示符，从 -28 到 31，不包括 0
     */
    public int getDayOfMonthIndicator() {
        return dom;
    }

    /**
     * 获取转换的星期几。
     * <p>
     * 如果规则定义了确切的日期，则返回 null。
     * <p>
     * 如果规则定义了转换可能发生的一周，则此方法返回月份中的日期将调整到的星期几。
     * 如果日期为正数，则调整为较晚的日期。
     * 如果日期为负数，则调整为较早的日期。
     *
     * @return 转换发生的星期几，如果规则定义了确切的日期则返回 null
     */
    public DayOfWeek getDayOfWeek() {
        return dow;
    }

    /**
     * 获取转换的本地时间，必须与 {@link #isMidnightEndOfDay()} 一起检查。
     * <p>
     * 时间使用时间定义转换为瞬时时间。
     *
     * @return 转换的本地时间，不为空
     */
    public LocalTime getLocalTime() {
        return time;
    }

    /**
     * 转换的本地时间是否为午夜结束时间。
     * <p>
     * 转换可能表示为24:00发生。
     *
     * @return 本地时间午夜是否在一天的开始或结束
     */
    public boolean isMidnightEndOfDay() {
        return timeEndOfDay;
    }

    /**
     * 获取时间定义，指定如何将时间转换为瞬时时间。
     * <p>
     * 本地时间可以使用标准偏移、墙偏移或 UTC 转换为瞬时时间。
     *
     * @return 时间定义，不为空
     */
    public TimeDefinition getTimeDefinition() {
        return timeDefinition;
    }

    /**
     * 获取转换时的标准偏移。
     *
     * @return 标准偏移，不为空
     */
    public ZoneOffset getStandardOffset() {
        return standardOffset;
    }

    /**
     * 获取转换前的偏移。
     *
     * @return 转换前的偏移，不为空
     */
    public ZoneOffset getOffsetBefore() {
        return offsetBefore;
    }

    /**
     * 获取转换后的偏移。
     *
     * @return 转换后的偏移，不为空
     */
    public ZoneOffset getOffsetAfter() {
        return offsetAfter;
    }

    //-----------------------------------------------------------------------
    /**
     * 为指定年份创建转换实例。
     * <p>
     * 计算使用 ISO-8601 日历系统。
     *
     * @param year  要创建转换的年份，不为空
     * @return 转换实例，不为空
     */
    public ZoneOffsetTransition createTransition(int year) {
        LocalDate date;
        if (dom < 0) {
            date = LocalDate.of(year, month, month.length(IsoChronology.INSTANCE.isLeapYear(year)) + 1 + dom);
            if (dow != null) {
                date = date.with(previousOrSame(dow));
            }
        } else {
            date = LocalDate.of(year, month, dom);
            if (dow != null) {
                date = date.with(nextOrSame(dow));
            }
        }
        if (timeEndOfDay) {
            date = date.plusDays(1);
        }
        LocalDateTime localDT = LocalDateTime.of(date, time);
        LocalDateTime transition = timeDefinition.createDateTime(localDT, standardOffset, offsetBefore);
        return new ZoneOffsetTransition(transition, offsetBefore, offsetAfter);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此对象是否等于另一个对象。
     * <p>
     * 比较对象的整个状态。
     *
     * @param otherRule  要比较的另一个对象，null 返回 false
     * @return 如果相等则返回 true
     */
    @Override
    public boolean equals(Object otherRule) {
        if (otherRule == this) {
            return true;
        }
        if (otherRule instanceof ZoneOffsetTransitionRule) {
            ZoneOffsetTransitionRule other = (ZoneOffsetTransitionRule) otherRule;
            return month == other.month && dom == other.dom && dow == other.dow &&
                timeDefinition == other.timeDefinition &&
                time.equals(other.time) &&
                timeEndOfDay == other.timeEndOfDay &&
                standardOffset.equals(other.standardOffset) &&
                offsetBefore.equals(other.offsetBefore) &&
                offsetAfter.equals(other.offsetAfter);
        }
        return false;
    }

    /**
     * 返回合适的哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        int hash = ((time.toSecondOfDay() + (timeEndOfDay ? 1 : 0)) << 15) +
                (month.ordinal() << 11) + ((dom + 32) << 5) +
                ((dow == null ? 7 : dow.ordinal()) << 2) + (timeDefinition.ordinal());
        return hash ^ standardOffset.hashCode() ^
                offsetBefore.hashCode() ^ offsetAfter.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 返回描述此对象的字符串。
     *
     * @return 用于调试的字符串，不为空
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TransitionRule[")
            .append(offsetBefore.compareTo(offsetAfter) > 0 ? "Gap " : "Overlap ")
            .append(offsetBefore).append(" to ").append(offsetAfter).append(", ");
        if (dow != null) {
            if (dom == -1) {
                buf.append(dow.name()).append(" on or before last day of ").append(month.name());
            } else if (dom < 0) {
                buf.append(dow.name()).append(" on or before last day minus ").append(-dom - 1).append(" of ").append(month.name());
            } else {
                buf.append(dow.name()).append(" on or after ").append(month.name()).append(' ').append(dom);
            }
        } else {
            buf.append(month.name()).append(' ').append(dom);
        }
        buf.append(" at ").append(timeEndOfDay ? "24:00" : time.toString())
            .append(" ").append(timeDefinition)
            .append(", standard offset ").append(standardOffset)
            .append(']');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 定义将本地时间转换为实际转换日期时间的方式。
     * <p>
     * 时区规则以三种方式之一表达：
     * <ul>
     * <li>相对于 UTC</li>
     * <li>相对于标准偏移</li>
     * <li>相对于墙偏移（你在墙上时钟上看到的时间）</li>
     * </ul>
     */
    public static enum TimeDefinition {
        /** 本地日期时间以 UTC 偏移表示。 */
        UTC,
        /** 本地日期时间以墙偏移表示。 */
        WALL,
        /** 本地日期时间以标准偏移表示。 */
        STANDARD;

        /**
         * 将指定的本地日期时间转换为实际在墙上时钟上看到的本地日期时间。
         * <p>
         * 此方法使用此枚举的类型进行转换。
         * 输出相对于转换的“之前”偏移定义。
         * <p>
         * UTC 类型使用 UTC 偏移。
         * STANDARD 类型使用标准偏移。
         * WALL 类型返回输入的日期时间。
         * 结果旨在与墙偏移一起使用。
         *
         * @param dateTime  本地日期时间，不为空
         * @param standardOffset  标准偏移，不为空
         * @param wallOffset  墙偏移，不为空
         * @return 相对于墙/之前偏移的日期时间，不为空
         */
        public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset) {
            switch (this) {
                case UTC: {
                    int difference = wallOffset.getTotalSeconds() - ZoneOffset.UTC.getTotalSeconds();
                    return dateTime.plusSeconds(difference);
                }
                case STANDARD: {
                    int difference = wallOffset.getTotalSeconds() - standardOffset.getTotalSeconds();
                    return dateTime.plusSeconds(difference);
                }
                default:  // WALL
                    return dateTime;
            }
        }
    }

}
