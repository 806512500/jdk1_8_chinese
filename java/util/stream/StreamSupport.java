
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
package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * 用于创建和操作流的低级实用方法。
 *
 * <p>此类主要用于为数据结构提供流视图的库编写者；大多数旨在供最终用户使用的静态流方法位于各种 {@code Stream} 类中。
 *
 * @since 1.8
 */
public final class StreamSupport {

    // 抑制默认构造函数，确保不可实例化。
    private StreamSupport() {}

    /**
     * 从 {@code Spliterator} 创建新的顺序或并行 {@code Stream}。
     *
     * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
     *
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用 {@link #stream(java.util.function.Supplier, int, boolean)}
     * 以减少与源的潜在干扰范围。有关详细信息，请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a>。
     *
     * @param <T> 流元素的类型
     * @param spliterator 描述流元素的 {@code Spliterator}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，则返回的流是顺序流。
     * @return 新的顺序或并行 {@code Stream}
     */
    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            parallel);
    }

    /**
     * 从 {@code Supplier} of {@code Spliterator} 创建新的顺序或并行 {@code Stream}。
     *
     * <p>在流管道的终端操作开始后，最多调用一次供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或者为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #stream(java.util.Spliterator, boolean)}
     * 可能更高效。
     * <p>在这种形式中使用 {@code Supplier} 提供了一层间接性，减少了与源的潜在干扰范围。由于供应商仅在终端操作开始后调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。有关详细信息，请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a>。
     *
     * @param <T> 流元素的类型
     * @param supplier 一个 {@code Spliterator} 的 {@code Supplier}
     * @param characteristics 提供的 {@code Spliterator} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，则返回的流是顺序流。
     * @return 新的顺序或并行 {@code Stream}
     * @see #stream(java.util.Spliterator, boolean)
     */
    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier,
                                       int characteristics,
                                       boolean parallel) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head<>(supplier,
                                            StreamOpFlag.fromCharacteristics(characteristics),
                                            parallel);
    }

    /**
     * 从 {@code Spliterator.OfInt} 创建新的顺序或并行 {@code IntStream}。
     *
     * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
     *
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用 {@link #intStream(java.util.function.Supplier, int, boolean)}
     * 以减少与源的潜在干扰范围。有关详细信息，请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a>。
     *
     * @param spliterator 描述流元素的 {@code Spliterator.OfInt}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，则返回的流是顺序流。
     * @return 新的顺序或并行 {@code IntStream}
     */
    public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head<>(spliterator,
                                      StreamOpFlag.fromCharacteristics(spliterator),
                                      parallel);
    }

    /**
     * 从 {@code Supplier} of {@code Spliterator.OfInt} 创建新的顺序或并行 {@code IntStream}。
     *
     * <p>在流管道的终端操作开始后，最多调用一次供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或者为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #intStream(java.util.Spliterator.OfInt, boolean)}
     * 可能更高效。
     * <p>在这种形式中使用 {@code Supplier} 提供了一层间接性，减少了与源的潜在干扰范围。由于供应商仅在终端操作开始后调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。有关详细信息，请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a>。
     *
     * @param supplier 一个 {@code Spliterator.OfInt} 的 {@code Supplier}
     * @param characteristics 提供的 {@code Spliterator.OfInt} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，则返回的流是顺序流。
     * @return 新的顺序或并行 {@code IntStream}
     * @see #intStream(java.util.Spliterator.OfInt, boolean)
     */
    public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier,
                                      int characteristics,
                                      boolean parallel) {
        return new IntPipeline.Head<>(supplier,
                                      StreamOpFlag.fromCharacteristics(characteristics),
                                      parallel);
    }


/**
 * 从 {@code Spliterator.OfLong} 创建一个新的顺序或并行的 {@code LongStream}。
 *
 * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
 *
 * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者
 * 是 <a href="../Spliterator.html#binding">延迟绑定</a> 的。否则，应使用
 * {@link #longStream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。详情请参阅
 * <a href="package-summary.html#NonInterference">非干扰性</a>。
 *
 * @param spliterator 描述流元素的 {@code Spliterator.OfLong}
 * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
 * @return 一个新的顺序或并行的 {@code LongStream}
 */
public static LongStream longStream(Spliterator.OfLong spliterator,
                                    boolean parallel) {
    return new LongPipeline.Head<>(spliterator,
                                   StreamOpFlag.fromCharacteristics(spliterator),
                                   parallel);
}

/**
 * 从 {@code Supplier} of {@code Spliterator.OfLong} 创建一个新的顺序或并行的 {@code LongStream}。
 *
 * <p>在流管道的终端操作开始后，最多只会调用一次 supplier 的 {@link Supplier#get()} 方法。
 *
 * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者
 * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用
 * {@link #longStream(java.util.Spliterator.OfLong, boolean)} 通常更高效。
 * <p>在此形式中使用 {@code Supplier} 提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。详情请参阅
 * <a href="package-summary.html#NonInterference">非干扰性</a>。
 *
 * @param supplier 一个 {@code Spliterator.OfLong} 的 {@code Supplier}
 * @param characteristics 供应的 {@code Spliterator.OfLong} 的 spliterator 特性。特性必须等于
 *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
 * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
 * @return 一个新的顺序或并行的 {@code LongStream}
 * @see #longStream(java.util.Spliterator.OfLong, boolean)
 */
public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier,
                                    int characteristics,
                                    boolean parallel) {
    return new LongPipeline.Head<>(supplier,
                                   StreamOpFlag.fromCharacteristics(characteristics),
                                   parallel);
}

/**
 * 从 {@code Spliterator.OfDouble} 创建一个新的顺序或并行的 {@code DoubleStream}。
 *
 * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
 *
 * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者
 * 是 <a href="../Spliterator.html#binding">延迟绑定</a> 的。否则，应使用
 * {@link #doubleStream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。详情请参阅
 * <a href="package-summary.html#NonInterference">非干扰性</a>。
 *
 * @param spliterator 描述流元素的 {@code Spliterator.OfDouble}
 * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
 * @return 一个新的顺序或并行的 {@code DoubleStream}
 */
public static DoubleStream doubleStream(Spliterator.OfDouble spliterator,
                                        boolean parallel) {
    return new DoublePipeline.Head<>(spliterator,
                                     StreamOpFlag.fromCharacteristics(spliterator),
                                     parallel);
}

/**
 * 从 {@code Supplier} of {@code Spliterator.OfDouble} 创建一个新的顺序或并行的 {@code DoubleStream}。
 *
 * <p>在流管道的终端操作开始后，最多只会调用一次 supplier 的 {@link Supplier#get()} 方法。
 *
 * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或者
 * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用
 * {@link #doubleStream(java.util.Spliterator.OfDouble, boolean)} 通常更高效。
 * <p>在此形式中使用 {@code Supplier} 提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。详情请参阅
 * <a href="package-summary.html#NonInterference">非干扰性</a>。
 *
 * @param supplier 一个 {@code Spliterator.OfDouble} 的 {@code Supplier}
 * @param characteristics 供应的 {@code Spliterator.OfDouble} 的 spliterator 特性。特性必须等于
 *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
 * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
 * @return 一个新的顺序或并行的 {@code DoubleStream}
 * @see #doubleStream(java.util.Spliterator.OfDouble, boolean)
 */
public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier,
                                        int characteristics,
                                        boolean parallel) {
    return new DoublePipeline.Head<>(supplier,
                                     StreamOpFlag.fromCharacteristics(characteristics),
                                     parallel);
}
