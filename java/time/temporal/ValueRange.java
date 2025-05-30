
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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;

/**
 * 日期时间字段的有效值范围。
 * <p>
 * 所有 {@link TemporalField} 实例都有一个有效值范围。
 * 例如，ISO 月中的日期从 1 到 28 到 31 不等。
 * 此类捕获该有效范围。
 * <p>
 * 需要注意此类的局限性。
 * 只提供最小值和最大值。
 * 在范围内部可能存在无效值。
 * 例如，一个奇怪的字段可能有 1, 2, 4, 6, 7 为有效值，因此
 * 有效范围为 '1 - 7'，尽管 3 和 5 是无效值。
 * <p>
 * 此类的实例不特定于某个字段。
 *
 * @implSpec
 * 此类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class ValueRange implements Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -7317881728594519368L;

    /**
     * 最小的最小值。
     */
    private final long minSmallest;
    /**
     * 最大的最小值。
     */
    private final long minLargest;
    /**
     * 最小的最大值。
     */
    private final long maxSmallest;
    /**
     * 最大的最大值。
     */
    private final long maxLargest;

    /**
     * 获取固定值范围。
     * <p>
     * 此工厂方法获取最小值和最大值固定的范围。
     * 例如，ISO 年中的月份始终从 1 到 12。
     *
     * @param min  最小值
     * @param max  最大值
     * @return min, max 的 ValueRange，不为 null
     * @throws IllegalArgumentException 如果最小值大于最大值
     */
    public static ValueRange of(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }
        return new ValueRange(min, min, max, max);
    }

    /**
     * 获取可变值范围。
     * <p>
     * 此工厂方法获取最小值固定而最大值可能变化的范围。
     * 例如，ISO 月中的日期始终从 1 开始，但结束于 28 到 31 之间。
     *
     * @param min  最小值
     * @param maxSmallest  最小的最大值
     * @param maxLargest  最大的最大值
     * @return min, 最小的最大值, 最大的最大值 的 ValueRange，不为 null
     * @throws IllegalArgumentException 如果
     *     最小值大于最小的最大值，
     *  或最小的最大值大于最大的最大值
     */
    public static ValueRange of(long min, long maxSmallest, long maxLargest) {
        return of(min, min, maxSmallest, maxLargest);
    }

    /**
     * 获取完全可变值范围。
     * <p>
     * 此工厂方法获取最小值和最大值都可能变化的范围。
     *
     * @param minSmallest  最小的最小值
     * @param minLargest  最大的最小值
     * @param maxSmallest  最小的最大值
     * @param maxLargest  最大的最大值
     * @return 最小的最小值, 最大的最小值, 最小的最大值, 最大的最大值 的 ValueRange，不为 null
     * @throws IllegalArgumentException 如果
     *     最小的最小值大于最小的最大值，
     *  或最小的最大值大于最大的最大值
     *  或最大的最小值大于最大的最大值
     */
    public static ValueRange of(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        if (minSmallest > minLargest) {
            throw new IllegalArgumentException("最小的最小值必须小于最大的最小值");
        }
        if (maxSmallest > maxLargest) {
            throw new IllegalArgumentException("最小的最大值必须小于最大的最大值");
        }
        if (minLargest > maxLargest) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }
        return new ValueRange(minSmallest, minLargest, maxSmallest, maxLargest);
    }

    /**
     * 限制性构造函数。
     *
     * @param minSmallest  最小的最小值
     * @param minLargest  最大的最小值
     * @param maxSmallest  最小的最大值
     * @param maxLargest  最大的最大值
     */
    private ValueRange(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        this.minSmallest = minSmallest;
        this.minLargest = minLargest;
        this.maxSmallest = maxSmallest;
        this.maxLargest = maxLargest;
    }

    //-----------------------------------------------------------------------
    /**
     * 判断值范围是否固定且完全已知。
     * <p>
     * 例如，ISO 月中的日期从 1 到 28 到 31。
     * 由于最大值存在不确定性，因此范围不是固定的。
     * 但是，对于一月，范围始终为 1 到 31，因此是固定的。
     *
     * @return 如果值集是固定的，则返回 true
     */
    public boolean isFixed() {
        return minSmallest == minLargest && maxSmallest == maxLargest;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取字段可以取的最小值。
     * <p>
     * 例如，ISO 月中的日期始终从 1 开始。
     * 因此，最小值为 1。
     *
     * @return 该字段的最小值
     */
    public long getMinimum() {
        return minSmallest;
    }

    /**
     * 获取字段可以取的最大可能最小值。
     * <p>
     * 例如，ISO 月中的日期始终从 1 开始。
     * 因此，最大可能最小值为 1。
     *
     * @return 该字段的最大可能最小值
     */
    public long getLargestMinimum() {
        return minLargest;
    }

    /**
     * 获取字段可以取的最小可能最大值。
     * <p>
     * 例如，ISO 月中的日期从 28 到 31 天不等。
     * 因此，最小可能最大值为 28。
     *
     * @return 该字段的最小可能最大值
     */
    public long getSmallestMaximum() {
        return maxSmallest;
    }

    /**
     * 获取字段可以取的最大值。
     * <p>
     * 例如，ISO 月中的日期从 28 到 31 天不等。
     * 因此，最大值为 31。
     *
     * @return 该字段的最大值
     */
    public long getMaximum() {
        return maxLargest;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查范围内的所有值是否都适合 {@code int}。
     * <p>
     * 此方法检查所有有效值是否在 {@code int} 的范围内。
     * <p>
     * 例如，ISO 年中的月份值从 1 到 12，适合 {@code int}。
     * 相比之下，ISO 日中的纳秒值从 1 到 86,400,000,000,000，不适合 {@code int}。
     * <p>
     * 此实现使用 {@link #getMinimum()} 和 {@link #getMaximum()}。
     *
     * @return 如果有效值始终适合 {@code int}，则返回 true
     */
    public boolean isIntValue() {
        return getMinimum() >= Integer.MIN_VALUE && getMaximum() <= Integer.MAX_VALUE;
    }

    /**
     * 检查值是否在有效范围内。
     * <p>
     * 此方法检查值是否在存储的值范围内。
     *
     * @param value  要检查的值
     * @return 如果值有效，则返回 true
     */
    public boolean isValidValue(long value) {
        return (value >= getMinimum() && value <= getMaximum());
    }

    /**
     * 检查值是否在有效范围内且所有值都适合 {@code int}。
     * <p>
     * 此方法结合了 {@link #isIntValue()} 和 {@link #isValidValue(long)}。
     *
     * @param value  要检查的值
     * @return 如果值有效且适合 {@code int}，则返回 true
     */
    public boolean isValidIntValue(long value) {
        return isIntValue() && isValidValue(value);
    }

    /**
     * 检查指定的值是否有效。
     * <p>
     * 此方法验证值是否在有效值范围内。
     * 字段仅用于改进错误消息。
     *
     * @param value  要检查的值
     * @param field  要检查的字段，可以为 null
     * @return 传递的值
     * @see #isValidValue(long)
     */
    public long checkValidValue(long value, TemporalField field) {
        if (isValidValue(value) == false) {
            throw new DateTimeException(genInvalidFieldMessage(field, value));
        }
        return value;
    }

    /**
     * 检查指定的值是否有效且适合 {@code int}。
     * <p>
     * 此方法验证值是否在有效值范围内且所有有效值都在 {@code int} 的范围内。
     * 字段仅用于改进错误消息。
     *
     * @param value  要检查的值
     * @param field  要检查的字段，可以为 null
     * @return 传递的值
     * @see #isValidIntValue(long)
     */
    public int checkValidIntValue(long value, TemporalField field) {
        if (isValidIntValue(value) == false) {
            throw new DateTimeException(genInvalidFieldMessage(field, value));
        }
        return (int) value;
    }

    private String genInvalidFieldMessage(TemporalField field, long value) {
        if (field != null) {
            return "无效值 " + field + "（有效值 " + this + "）: " + value;
        } else {
            return "无效值（有效值 " + this + "）: " + value;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从流中恢复 ValueRange 的状态。
     * 检查值是否有效。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 如果
     *     最小的最小值大于最小的最大值，
     *  或最小的最大值大于最大的最大值
     *  或最大的最小值大于最大的最大值
     * @throws ClassNotFoundException 如果无法解析类
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException, InvalidObjectException
    {
        s.defaultReadObject();
        if (minSmallest > minLargest) {
            throw new InvalidObjectException("最小的最小值必须小于最大的最小值");
        }
        if (maxSmallest > maxLargest) {
            throw new InvalidObjectException("最小的最大值必须小于最大的最大值");
        }
        if (minLargest > maxLargest) {
            throw new InvalidObjectException("最小值必须小于最大值");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此范围是否等于另一个范围。
     * <p>
     * 比较基于四个值：最小值、最大的最小值、最小的最大值和最大值。
     * 只比较 {@code ValueRange} 类型的对象，其他类型返回 false。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此范围等于另一个范围，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ValueRange) {
            ValueRange other = (ValueRange) obj;
            return minSmallest == other.minSmallest && minLargest == other.minLargest &&
                   maxSmallest == other.maxSmallest && maxLargest == other.maxLargest;
        }
        return false;
    }

    /**
     * 此范围的哈希码。
     *
     * @return 适合的哈希码
     */
    @Override
    public int hashCode() {
        long hash = minSmallest + (minLargest << 16) + (minLargest >> 48) +
                (maxSmallest << 32) + (maxSmallest >> 32) + (maxLargest << 48) +
                (maxLargest >> 16);
        return (int) (hash ^ (hash >>> 32));
    }


                //-----------------------------------------------------------------------
    /**
     * 输出此范围作为 {@code String}。
     * <p>
     * 格式为 '{min}/{largestMin} - {smallestMax}/{max}'，
     * 其中，如果 largestMin 或 smallestMax 与 min 或 max 相同，则可以省略这些部分及其关联的斜杠。
     *
     * @return 此范围的字符串表示形式，不为空
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(minSmallest);
        if (minSmallest != minLargest) {
            buf.append('/').append(minLargest);
        }
        buf.append(" - ").append(maxSmallest);
        if (maxSmallest != maxLargest) {
            buf.append('/').append(maxLargest);
        }
        return buf.toString();
    }

}
