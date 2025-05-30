
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
package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * 创建和操作流的低级实用方法。
 *
 * <p>此类主要用于为数据结构提供流视图的库编写者；大多数静态流方法都是为最终用户设计的，位于各种 {@code Stream} 类中。
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
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用
     * {@link #stream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param <T> 流元素的类型
     * @param spliterator 描述流元素的 {@code Spliterator}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
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
     * <p>仅在流管道的终端操作开始后，才会调用供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #stream(java.util.Spliterator, boolean)}
     * 可能更高效。
     * <p>使用 {@code Supplier} 的这种形式提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param <T> 流元素的类型
     * @param supplier 一个 {@code Supplier} of a {@code Spliterator}
     * @param characteristics 供应商提供的 {@code Spliterator} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
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
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用
     * {@link #intStream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param spliterator 描述流元素的 {@code Spliterator.OfInt}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
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
     * <p>仅在流管道的终端操作开始后，才会调用供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #intStream(java.util.Spliterator.OfInt, boolean)}
     * 可能更高效。
     * <p>使用 {@code Supplier} 的这种形式提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param supplier 一个 {@code Supplier} of a {@code Spliterator.OfInt}
     * @param characteristics 供应商提供的 {@code Spliterator.OfInt} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
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
     * 从 {@code Spliterator.OfLong} 创建新的顺序或并行 {@code LongStream}。
     *
     * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
     *
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用
     * {@link #longStream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param spliterator 描述流元素的 {@code Spliterator.OfLong}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
     * @return 新的顺序或并行 {@code LongStream}
     */
    public static LongStream longStream(Spliterator.OfLong spliterator,
                                        boolean parallel) {
        return new LongPipeline.Head<>(spliterator,
                                       StreamOpFlag.fromCharacteristics(spliterator),
                                       parallel);
    }

    /**
     * 从 {@code Supplier} of {@code Spliterator.OfLong} 创建新的顺序或并行 {@code LongStream}。
     *
     * <p>仅在流管道的终端操作开始后，才会调用供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #longStream(java.util.Spliterator.OfLong, boolean)}
     * 可能更高效。
     * <p>使用 {@code Supplier} 的这种形式提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param supplier 一个 {@code Supplier} of a {@code Spliterator.OfLong}
     * @param characteristics 供应商提供的 {@code Spliterator.OfLong} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
     * @return 新的顺序或并行 {@code LongStream}
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
     * 从 {@code Spliterator.OfDouble} 创建新的顺序或并行 {@code DoubleStream}。
     *
     * <p>仅在流管道的终端操作开始后，才会遍历、拆分或查询 spliterator 的估计大小。
     *
     * <p>强烈建议 spliterator 报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a>。否则，应使用
     * {@link #doubleStream(java.util.function.Supplier, int, boolean)} 来减少对源的潜在干扰范围。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param spliterator 描述流元素的 {@code Spliterator.OfDouble}
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
     * @return 新的顺序或并行 {@code DoubleStream}
     */
    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator,
                                            boolean parallel) {
        return new DoublePipeline.Head<>(spliterator,
                                         StreamOpFlag.fromCharacteristics(spliterator),
                                         parallel);
    }

    /**
     * 从 {@code Supplier} of {@code Spliterator.OfDouble} 创建新的顺序或并行 {@code DoubleStream}。
     *
     * <p>仅在流管道的终端操作开始后，才会调用供应商的 {@link Supplier#get()} 方法。
     *
     * <p>对于报告 {@code IMMUTABLE} 或 {@code CONCURRENT} 特性的 spliterator，或为
     * <a href="../Spliterator.html#binding">延迟绑定</a> 的 spliterator，使用 {@link #doubleStream(java.util.Spliterator.OfDouble, boolean)}
     * 可能更高效。
     * <p>使用 {@code Supplier} 的这种形式提供了一层间接性，减少了对源的潜在干扰范围。由于供应商仅在终端操作开始后才被调用，因此在终端操作开始前对源的任何修改都会反映在流结果中。请参阅
     * <a href="package-summary.html#NonInterference">非干扰</a> 了解详细信息。
     *
     * @param supplier 一个 {@code Supplier} of a {@code Spliterator.OfDouble}
     * @param characteristics 供应商提供的 {@code Spliterator.OfDouble} 的 Spliterator 特性。特性必须等于
     *        {@code supplier.get().characteristics()}，否则在终端操作开始时可能会发生未定义的行为。
     * @param parallel 如果为 {@code true}，则返回的流是并行流；如果为 {@code false}，返回的流是顺序流。
     * @return 新的顺序或并行 {@code DoubleStream}
     * @see #doubleStream(java.util.Spliterator.OfDouble, boolean)
     */
    public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier,
                                            int characteristics,
                                            boolean parallel) {
        return new DoublePipeline.Head<>(supplier,
                                         StreamOpFlag.fromCharacteristics(characteristics),
                                         parallel);
    }
}
