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
import java.util.stream.Collector;

/**
 * 用于收集统计信息（如计数、最小值、最大值、总和和平均值）的状态对象。
 *
 * <p>此类设计用于与（但不强制要求）
 * {@linkplain java.util.stream 流}一起使用。例如，您可以使用以下代码计算整数流的统计信息：
 * <pre> {@code
 * IntSummaryStatistics stats = intStream.collect(IntSummaryStatistics::new,
 *                                                IntSummaryStatistics::accept,
 *                                                IntSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code IntSummaryStatistics} 可以用作
 * {@linkplain java.util.stream.Stream#collect(Collector) 聚合}
 * 的目标，例如：
 *
 * <pre> {@code
 * IntSummaryStatistics stats = people.stream()
 *                                    .collect(Collectors.summarizingInt(Person::getDependents));
 *}</pre>
 *
 * 这可以在一次遍历中计算出人员的数量，以及他们依赖人数的最小值、最大值、总和和平均值。
 *
 * @implNote 本实现不是线程安全的。但是，可以在并行流上安全地使用
 * {@link java.util.stream.Collectors#summarizingInt(java.util.function.ToIntFunction)
 * Collectors.toIntStatistics()}，因为并行实现的 {@link java.util.stream.Stream#collect Stream.collect()}
 * 提供了必要的分区、隔离和合并结果，以确保安全和高效的并行执行。
 *
 * <p>本实现不检查总和的溢出。
 * @since 1.8
 */
public class IntSummaryStatistics implements IntConsumer {
    private long count;
    private long sum;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    /**
     * 构造一个空实例，计数为零，总和为零，
     * {@code Integer.MAX_VALUE} 为最小值，{@code Integer.MIN_VALUE} 为最大值，平均值为零。
     */
    public IntSummaryStatistics() { }

    /**
     * 记录一个新的值到统计信息中
     *
     * @param value 输入值
     */
    @Override
    public void accept(int value) {
        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * 将另一个 {@code IntSummaryStatistics} 的状态合并到当前实例中。
     *
     * @param other 另一个 {@code IntSummaryStatistics}
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    public void combine(IntSummaryStatistics other) {
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
     * 返回记录的值的总和，如果没有记录任何值，则返回零。
     *
     * @return 值的总和，如果没有记录任何值则返回零
     */
    public final long getSum() {
        return sum;
    }

    /**
     * 返回记录的最小值，如果没有记录任何值，则返回 {@code Integer.MAX_VALUE}。
     *
     * @return 最小值，如果没有记录任何值则返回 {@code Integer.MAX_VALUE}
     */
    public final int getMin() {
        return min;
    }

    /**
     * 返回记录的最大值，如果没有记录任何值，则返回 {@code Integer.MIN_VALUE}。
     *
     * @return 最大值，如果没有记录任何值则返回 {@code Integer.MIN_VALUE}
     */
    public final int getMax() {
        return max;
    }

    /**
     * 返回记录的值的算术平均值，如果没有记录任何值，则返回零。
     *
     * @return 值的算术平均值，如果没有记录任何值则返回零
     */
    public final double getAverage() {
        return getCount() > 0 ? (double) getSum() / getCount() : 0.0d;
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * 返回一个非空的字符串表示，适用于调试。确切的表示格式未指定，可能会在不同实现和版本之间有所不同。
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
