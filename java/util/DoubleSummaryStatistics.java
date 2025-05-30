
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

import java.util.function.DoubleConsumer;
import java.util.stream.Collector;

/**
 * 用于收集统计信息（如计数、最小值、最大值、总和和平均值）的状态对象。
 *
 * <p>此类设计用于与（但不强制要求）
 * {@linkplain java.util.stream 流}一起使用。例如，您可以使用以下代码计算双精度浮点数流的统计信息：
 * <pre> {@code
 * DoubleSummaryStatistics stats = doubleStream.collect(DoubleSummaryStatistics::new,
 *                                                      DoubleSummaryStatistics::accept,
 *                                                      DoubleSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code DoubleSummaryStatistics} 可以用作
 * {@linkplain java.util.stream.Stream#collect(Collector) 聚合}
 * 的目标，用于 {@linkplain java.util.stream.Stream 流}。例如：
 *
 * <pre> {@code
 * DoubleSummaryStatistics stats = people.stream()
 *     .collect(Collectors.summarizingDouble(Person::getWeight));
 *}</pre>
 *
 * 这将一次性计算人员的数量，以及他们的体重的最小值、最大值、总和和平均值。
 *
 * @implNote 本实现不是线程安全的。但是，可以在并行流上安全地使用
 * {@link java.util.stream.Collectors#summarizingDouble(java.util.function.ToDoubleFunction)
 * Collectors.toDoubleStatistics()}，因为并行实现的 {@link java.util.stream.Stream#collect Stream.collect()}
 * 提供了必要的分区、隔离和结果合并，以实现安全且高效的并行执行。
 * @since 1.8
 */
public class DoubleSummaryStatistics implements DoubleConsumer {
    private long count;
    private double sum;
    private double sumCompensation; // 总和的低阶位
    private double simpleSum; // 用于计算非有限输入的正确总和
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * 构造一个空实例，计数为零，总和为零，
     * {@code Double.POSITIVE_INFINITY} 为最小值，{@code Double.NEGATIVE_INFINITY}
     * 为最大值，平均值为零。
     */
    public DoubleSummaryStatistics() { }

    /**
     * 记录另一个值到统计信息中。
     *
     * @param value 输入值
     */
    @Override
    public void accept(double value) {
        ++count;
        simpleSum += value;
        sumWithCompensation(value);
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * 将另一个 {@code DoubleSummaryStatistics} 的状态合并到此对象中。
     *
     * @param other 另一个 {@code DoubleSummaryStatistics}
     * @throws NullPointerException 如果 {@code other} 为 null
     */
    public void combine(DoubleSummaryStatistics other) {
        count += other.count;
        simpleSum += other.simpleSum;
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
    }

    /**
     * 使用 Kahan 求和 / 补偿求和方法合并一个新的双精度浮点值。
     */
    private void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp; // 舍入误差的小狼
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
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
     * 如果任何记录的值是 NaN 或总和在任何点是 NaN
     * 则总和将为 NaN。
     *
     * <p> 浮点数总和的值既取决于输入值，也取决于加法操作的顺序。此方法的加法操作顺序有意未定义，以允许实现灵活性，提高计算结果的速度和准确性。
     *
     * 特别是，此方法可以使用补偿求和或其他技术来减少与简单双精度值求和相比的误差界限。
     *
     * @apiNote 按绝对值递增顺序排序的值往往会产生更准确的结果。
     *
     * @return 值的总和，如果没有则返回零
     */
    public final double getSum() {
        // 将两个术语相加以获得最终总和，以获得更好的误差界限
        double tmp =  sum + sumCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
            // 如果补偿总和由于累积一个或多个相同符号的无穷大值而错误地为 NaN，
            // 返回存储在 simpleSum 中的正确符号的无穷大。
            return simpleSum;
        else
            return tmp;
    }


                /**
     * 返回记录的最小值，如果任何记录的值为 NaN，则返回 {@code Double.NaN}，如果没有记录任何值，则返回 {@code Double.POSITIVE_INFINITY}。与数值比较运算符不同，此方法认为负零严格小于正零。
     *
     * @return 记录的最小值，如果任何记录的值为 NaN，则返回 {@code Double.NaN}，如果没有记录任何值，则返回 {@code Double.POSITIVE_INFINITY}
     */
    public final double getMin() {
        return min;
    }

    /**
     * 返回记录的最大值，如果任何记录的值为 NaN，则返回 {@code Double.NaN}，如果没有记录任何值，则返回 {@code Double.NEGATIVE_INFINITY}。与数值比较运算符不同，此方法认为负零严格小于正零。
     *
     * @return 记录的最大值，如果任何记录的值为 NaN，则返回 {@code Double.NaN}，如果没有记录任何值，则返回 {@code Double.NEGATIVE_INFINITY}
     */
    public final double getMax() {
        return max;
    }

    /**
     * 返回记录值的算术平均值，如果没有记录任何值，则返回零。
     *
     * 如果任何记录的值为 NaN 或者总和在任何点为 NaN，则平均值将为 NaN。
     *
     * <p>返回的平均值可能因值的记录顺序而异。
     *
     * 此方法可以使用补偿求和或其他技术来减少用于计算平均值的 {@link #getSum 数值总和} 的误差。
     *
     * @apiNote 按绝对值递增顺序排序的值倾向于产生更准确的结果。
     *
     * @return 值的算术平均值，如果没有记录任何值，则返回零
     */
    public final double getAverage() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    /**
     * {@inheritDoc}
     *
     * 返回一个非空的字符串表示，适用于调试。确切的表示格式未指定，可能在不同实现和版本之间有所不同。
     */
    @Override
    public String toString() {
        return String.format(
            "%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
            this.getClass().getSimpleName(),
            getCount(),
            getSum(),
            getMin(),
            getAverage(),
            getMax());
    }
}
