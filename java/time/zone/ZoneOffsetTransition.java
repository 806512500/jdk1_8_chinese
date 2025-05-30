
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 两个时区偏移之间的转换，由本地时间线的不连续性引起。
 * <p>
 * 两个时区偏移之间的转换通常是由于夏令时的切换引起的。
 * 不连续性通常在春季是一个空隙，在秋季是一个重叠。
 * {@code ZoneOffsetTransition} 模型了两个时区偏移之间的转换。
 * <p>
 * 空隙发生在某些本地日期时间根本不存在的情况下。
 * 例如，当偏移从 {@code +03:00} 变为 {@code +04:00} 时。
 * 这可能被描述为“今晚1点时钟将向前跳一小时”。
 * <p>
 * 重叠发生在某些本地日期时间存在两次的情况下。
 * 例如，当偏移从 {@code +04:00} 变为 {@code +03:00} 时。
 * 这可能被描述为“今晚2点时钟将向后退一小时”。
 *
 * @implSpec
 * 该类是不可变的且线程安全。
 *
 * @since 1.8
 */
public final class ZoneOffsetTransition
        implements Comparable<ZoneOffsetTransition>, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -6946044323557704546L;
    /**
     * 转换时的本地转换日期时间。
     */
    private final LocalDateTime transition;
    /**
     * 转换前的偏移。
     */
    private final ZoneOffset offsetBefore;
    /**
     * 转换后的偏移。
     */
    private final ZoneOffset offsetAfter;

    //-----------------------------------------------------------------------
    /**
     * 获取定义两个偏移之间转换的实例。
     * <p>
     * 应用程序通常应从 {@link ZoneRules} 获取实例。
     * 该工厂仅用于创建 {@link ZoneRules}。
     *
     * @param transition  转换时的日期时间，从未实际发生，以转换前的偏移表示，不为空
     * @param offsetBefore  转换前的偏移，不为空
     * @param offsetAfter  转换后的偏移，不为空
     * @return 转换，不为空
     * @throws IllegalArgumentException 如果 {@code offsetBefore} 和 {@code offsetAfter}
     *         相等，或 {@code transition.getNano()} 返回非零值
     */
    public static ZoneOffsetTransition of(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        Objects.requireNonNull(transition, "transition");
        Objects.requireNonNull(offsetBefore, "offsetBefore");
        Objects.requireNonNull(offsetAfter, "offsetAfter");
        if (offsetBefore.equals(offsetAfter)) {
            throw new IllegalArgumentException("偏移必须不相等");
        }
        if (transition.getNano() != 0) {
            throw new IllegalArgumentException("纳秒必须为零");
        }
        return new ZoneOffsetTransition(transition, offsetBefore, offsetAfter);
    }

    /**
     * 创建定义两个偏移之间转换的实例。
     *
     * @param transition  转换前的偏移的日期时间，不为空
     * @param offsetBefore  转换前的偏移，不为空
     * @param offsetAfter  转换后的偏移，不为空
     */
    ZoneOffsetTransition(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.transition = transition;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    /**
     * 从纪元秒和偏移创建实例。
     *
     * @param epochSecond  转换的纪元秒
     * @param offsetBefore  转换前的偏移，不为空
     * @param offsetAfter  转换后的偏移，不为空
     */
    ZoneOffsetTransition(long epochSecond, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.transition = LocalDateTime.ofEpochSecond(epochSecond, 0, offsetBefore);
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
     * <a href="../../../serialized-form.html#java.time.zone.Ser">专用的序列化形式</a>
     * 写入对象。
     * @serialData
     * 参考
     * <a href="../../../serialized-form.html#java.time.zone.ZoneRules">ZoneRules.writeReplace</a>
     * 的序列化形式，了解纪元秒和偏移的编码。
     * <pre style="font-size:1.0em">{@code
     *
     *   out.writeByte(2);                // 标识 ZoneOffsetTransition
     *   out.writeEpochSec(toEpochSecond);
     *   out.writeOffset(offsetBefore);
     *   out.writeOffset(offsetAfter);
     * }
     * </pre>
     * @return 替换对象，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZOT, this);
    }

    /**
     * 将状态写入流。
     *
     * @param out  输出流，不为空
     * @throws IOException 如果发生错误
     */
    void writeExternal(DataOutput out) throws IOException {
        Ser.writeEpochSec(toEpochSecond(), out);
        Ser.writeOffset(offsetBefore, out);
        Ser.writeOffset(offsetAfter, out);
    }

    /**
     * 从流中读取状态。
     *
     * @param in  输入流，不为空
     * @return 创建的对象，不为空
     * @throws IOException 如果发生错误
     */
    static ZoneOffsetTransition readExternal(DataInput in) throws IOException {
        long epochSecond = Ser.readEpochSec(in);
        ZoneOffset before = Ser.readOffset(in);
        ZoneOffset after = Ser.readOffset(in);
        if (before.equals(after)) {
            throw new IllegalArgumentException("偏移必须不相等");
        }
        return new ZoneOffsetTransition(epochSecond, before, after);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取转换的瞬间。
     * <p>
     * 这是不连续性的瞬间，定义为“之后”偏移开始适用的第一个瞬间。
     * <p>
     * 方法 {@link #getInstant()}、{@link #getDateTimeBefore()} 和 {@link #getDateTimeAfter()}
     * 都表示同一个瞬间。
     *
     * @return 转换的瞬间，不为空
     */
    public Instant getInstant() {
        return transition.toInstant(offsetBefore);
    }

    /**
     * 获取转换的瞬间作为纪元秒。
     *
     * @return 转换的纪元秒
     */
    public long toEpochSecond() {
        return transition.toEpochSecond(offsetBefore);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取本地转换日期时间，以“之前”偏移表示。
     * <p>
     * 这是不连续性开始的日期时间，以“之前”偏移表示。
     * 在这个瞬间，“之后”偏移实际上被使用，因此这个日期时间和“之前”偏移的组合永远不会发生。
     * <p>
     * “之前”日期时间和偏移的组合表示与“之后”日期时间和偏移相同的瞬间。
     *
     * @return 以“之前”偏移表示的转换日期时间，不为空
     */
    public LocalDateTime getDateTimeBefore() {
        return transition;
    }

    /**
     * 获取本地转换日期时间，以“之后”偏移表示。
     * <p>
     * 这是不连续性后的第一个日期时间，新的偏移开始适用。
     * <p>
     * “之前”日期时间和偏移的组合表示与“之后”日期时间和偏移相同的瞬间。
     *
     * @return 以“之后”偏移表示的转换日期时间，不为空
     */
    public LocalDateTime getDateTimeAfter() {
        return transition.plusSeconds(getDurationSeconds());
    }

    /**
     * 获取转换前的偏移。
     * <p>
     * 这是在转换瞬间之前使用的偏移。
     *
     * @return 转换前的偏移，不为空
     */
    public ZoneOffset getOffsetBefore() {
        return offsetBefore;
    }

    /**
     * 获取转换后的偏移。
     * <p>
     * 这是在转换瞬间及之后使用的偏移。
     *
     * @return 转换后的偏移，不为空
     */
    public ZoneOffset getOffsetAfter() {
        return offsetAfter;
    }

    /**
     * 获取转换的持续时间。
     * <p>
     * 在大多数情况下，转换的持续时间是一小时，但这并不总是如此。
     * 对于空隙，持续时间是正的；对于重叠，持续时间是负的。
     * 时区基于秒，因此持续时间的纳秒部分为零。
     *
     * @return 转换的持续时间，空隙为正，重叠为负
     */
    public Duration getDuration() {
        return Duration.ofSeconds(getDurationSeconds());
    }

    /**
     * 获取转换的持续时间（以秒为单位）。
     *
     * @return 持续时间（以秒为单位）
     */
    private int getDurationSeconds() {
        return getOffsetAfter().getTotalSeconds() - getOffsetBefore().getTotalSeconds();
    }

    /**
     * 判断此转换是否代表本地时间线的空隙。
     * <p>
     * 空隙发生在某些本地日期时间根本不存在的情况下。
     * 例如，当偏移从 {@code +01:00} 变为 {@code +02:00} 时。
     * 这可能被描述为“今晚1点时钟将向前跳一小时”。
     *
     * @return 如果此转换是空隙，则返回 true；如果是重叠，则返回 false
     */
    public boolean isGap() {
        return getOffsetAfter().getTotalSeconds() > getOffsetBefore().getTotalSeconds();
    }

    /**
     * 判断此转换是否代表本地时间线的重叠。
     * <p>
     * 重叠发生在某些本地日期时间存在两次的情况下。
     * 例如，当偏移从 {@code +02:00} 变为 {@code +01:00} 时。
     * 这可能被描述为“今晚2点时钟将向后退一小时”。
     *
     * @return 如果此转换是重叠，则返回 true；如果是空隙，则返回 false
     */
    public boolean isOverlap() {
        return getOffsetAfter().getTotalSeconds() < getOffsetBefore().getTotalSeconds();
    }

    /**
     * 检查指定的偏移在此转换期间是否有效。
     * <p>
     * 这会检查给定的偏移在转换期间的某个时刻是否有效。
     * 空隙总是返回 false。
     * 重叠会返回 true，如果偏移是“之前”或“之后”的偏移。
     *
     * @param offset  要检查的偏移，null 返回 false
     * @return 如果偏移在此转换期间有效，则返回 true
     */
    public boolean isValidOffset(ZoneOffset offset) {
        return isGap() ? false : (getOffsetBefore().equals(offset) || getOffsetAfter().equals(offset));
    }

    /**
     * 获取在此转换期间有效的偏移。
     * <p>
     * 空隙将返回一个空列表，而重叠将返回两个偏移。
     *
     * @return 有效的偏移列表
     */
    List<ZoneOffset> getValidOffsets() {
        if (isGap()) {
            return Collections.emptyList();
        }
        return Arrays.asList(getOffsetBefore(), getOffsetAfter());
    }

    //-----------------------------------------------------------------------
    /**
     * 基于转换的瞬间比较此转换与另一个转换。
     * <p>
     * 这比较每个转换的瞬间。
     * 偏移被忽略，使得此顺序与 equals 不一致。
     *
     * @param transition  要比较的转换，不为空
     * @return 比较值，小于0表示小于，大于0表示大于
     */
    @Override
    public int compareTo(ZoneOffsetTransition transition) {
        return this.getInstant().compareTo(transition.getInstant());
    }


                //-----------------------------------------------------------------------
    /**
     * 检查此对象是否等于另一个对象。
     * <p>
     * 比较对象的整个状态。
     *
     * @param other  要比较的另一个对象，null 返回 false
     * @return 如果相等则返回 true
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ZoneOffsetTransition) {
            ZoneOffsetTransition d = (ZoneOffsetTransition) other;
            return transition.equals(d.transition) &&
                offsetBefore.equals(d.offsetBefore) && offsetAfter.equals(d.offsetAfter);
        }
        return false;
    }

    /**
     * 返回一个合适的哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return transition.hashCode() ^ offsetBefore.hashCode() ^ Integer.rotateLeft(offsetAfter.hashCode(), 16);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回描述此对象的字符串。
     *
     * @return 用于调试的字符串，不为 null
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Transition[")
            .append(isGap() ? "Gap" : "Overlap")
            .append(" at ")
            .append(transition)
            .append(offsetBefore)
            .append(" to ")
            .append(offsetAfter)
            .append(']');
        return buf.toString();
    }

}
