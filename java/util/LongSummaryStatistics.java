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
package java.util;

import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Collector;

/**
 * 用于收集统计信息（如计数、最小值、最大值、总和和平均值）的状态对象。
 *
 * <p>此类设计用于与（但不强制要求）
 * {@linkplain java.util.stream 流}一起使用。例如，您可以计算
 * 一个 long 流的汇总统计信息：
 * <pre> {@code
 * LongSummaryStatistics stats = longStream.collect(LongSummaryStatistics::new,
 *                                                  LongSummaryStatistics::accept,
 *                                                  LongSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code LongSummaryStatistics} 可以用作
 * {@linkplain java.util.stream.Stream#collect(Collector) 收集}操作的
 * 目标，用于一个 {@linkplain java.util.stream.Stream 流}。例如：
 *
 * <pre> {@code
 * LongSummaryStatistics stats = people.stream()
 *                                     .collect(Collectors.summarizingLong(Person::getAge));
 *}</pre>
 *
 * 这在一个遍历中计算了人的数量，以及他们的年龄的最小值、最大值、总和和平均值。
 *
 * @implNote 此实现不是线程安全的。但是，可以在并行流上安全地使用
 * {@link java.util.stream.Collectors#summarizingLong(java.util.function.ToLongFunction)
 * Collectors.toLongStatistics()}，因为并行实现的
 * {@link java.util.stream.Stream#collect Stream.collect()}
 * 提供了必要的分区、隔离和合并结果，以确保安全和高效的并行执行。
 *
 * <p>此实现不检查总和的溢出。
 * @since 1.8
 */
public class LongSummaryStatistics implements LongConsumer, IntConsumer {
    private long count;
    private long sum;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;

    /**
     * 构造一个空实例，计数为零，总和为零，
     * {@code Long.MAX_VALUE} 为最小值，{@code Long.MIN_VALUE} 为最大值，平均值为零。
     */
    public LongSummaryStatistics() { }

    /**
     * 将一个新的 {@code int} 值记录到汇总信息中。
     *
     * @param value 输入值
     */
    @Override
    public void accept(int value) {
        accept((long) value);
    }

    /**
     * 将一个新的 {@code long} 值记录到汇总信息中。
     *
     * @param value 输入值
     */
    @Override
    public void accept(long value) {
        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * 将另一个 {@code LongSummaryStatistics} 的状态合并到此对象中。
     *
     * @param other 另一个 {@code LongSummaryStatistics}
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    public void combine(LongSummaryStatistics other) {
        count += other.count;
        sum += other.sum;
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
    }

    /**
     * 返回记录的值的数量。
     *
     * @return 值的数量
     */
    public final long getCount() {
        return count;
    }

    /**
     * 返回记录的值的总和，如果没有记录任何值则返回零。
     *
     * @return 值的总和，如果没有则返回零
     */
    public final long getSum() {
        return sum;
    }

    /**
     * 返回记录的最小值，如果没有记录任何值则返回 {@code Long.MAX_VALUE}。
     *
     * @return 最小值，如果没有则返回 {@code Long.MAX_VALUE}
     */
    public final long getMin() {
        return min;
    }

    /**
     * 返回记录的最大值，如果没有记录任何值则返回 {@code Long.MIN_VALUE}。
     *
     * @return 最大值，如果没有则返回 {@code Long.MIN_VALUE}
     */
    public final long getMax() {
        return max;
    }

    /**
     * 返回记录的值的算术平均值，如果没有记录任何值则返回零。
     *
     * @return 值的算术平均值，如果没有则返回零
     */
    public final double getAverage() {
        return getCount() > 0 ? (double) getSum() / getCount() : 0.0d;
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * 返回一个非空的字符串表示，适用于调试。确切的表示格式未指定，可能因实现和版本而异。
     */
    public String toString() {
        return String.format(
            "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
            this.getClass().getSimpleName(),
            getCount(),
            getSum(),
            getMin(),
            getAverage(),
            getMax());
    }
}
