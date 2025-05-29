/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
 * 一个 long 类型流的摘要统计信息：
 * <pre> {@code
 * LongSummaryStatistics stats = longStream.collect(LongSummaryStatistics::new,
 *                                                  LongSummaryStatistics::accept,
 *                                                  LongSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code LongSummaryStatistics} 可以用作
 * {@linkplain java.util.stream.Stream#collect(Collector) 收集}操作的目标
 * 用于一个 {@linkplain java.util.stream.Stream 流}。例如：
 *
 * <pre> {@code
 * LongSummaryStatistics stats = people.stream()
 *                                     .collect(Collectors.summarizingLong(Person::getAge));
 *}</pre>
 *
 * 这可以在一次遍历中计算出人的数量，以及他们的年龄的最小值、最大值、总和和平均值。
 *
 * @implNote 该实现不是线程安全的。但是，可以在并行流上安全地使用
 * {@link java.util.stream.Collectors#summarizingLong(java.util.function.ToLongFunction)
 * Collectors.toLongStatistics()}，因为并行实现的
 * {@link java.util.stream.Stream#collect Stream.collect()}
 * 提供了必要的分区、隔离和结果合并，以实现安全高效的并行执行。
 *
 * <p>此实现不会检查总和的溢出。
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
     * 记录一个新的 {@code int} 值到摘要信息中。
     *
     * @param value 输入值
     */
    @Override
    public void accept(int value) {
        accept((long) value);
    }

    /**
     * 记录一个新的 {@code long} 值到摘要信息中。
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
     * 返回一个非空的字符串表示形式，适用于调试。确切的表示格式未指定，可能在实现和版本之间有所不同。
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