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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * 流的基础接口，支持顺序和并行聚合操作。以下示例说明了使用流类型 {@link Stream}
 * 和 {@link IntStream} 计算红色小部件的重量总和：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * 有关流、流操作、流管道和并行性的详细说明，请参阅 {@link Stream} 类文档和
 * <a href="package-summary.html">java.util.stream</a> 包文档。
 *
 * @param <T> 流元素的类型
 * @param <S> 实现 {@code BaseStream} 的流类型
 * @since 1.8
 * @see Stream
 * @see IntStream
 * @see LongStream
 * @see DoubleStream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface BaseStream<T, S extends BaseStream<T, S>>
        extends AutoCloseable {
    /**
     * 返回此流的元素迭代器。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 此流的元素迭代器
     */
    Iterator<T> iterator();

    /**
     * 返回此流的元素分割器。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 此流的元素分割器
     */
    Spliterator<T> spliterator();

    /**
     * 返回此流，如果执行终端操作，是否将并行执行。在调用终端流操作方法后调用此方法可能会产生不可预测的结果。
     *
     * @return 如果此流在执行时将并行执行，则返回 {@code true}
     */
    boolean isParallel();

    /**
     * 返回一个等效的顺序流。可能返回自身，因为流已经是顺序的，或者底层流状态已被修改为顺序的。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 顺序流
     */
    S sequential();

    /**
     * 返回一个等效的并行流。可能返回自身，因为流已经是并行的，或者底层流状态已被修改为并行的。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 并行流
     */
    S parallel();

    /**
     * 返回一个等效的无序流。可能返回自身，因为流已经是无序的，或者底层流状态已被修改为无序的。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 无序流
     */
    S unordered();

    /**
     * 返回一个带有额外关闭处理器的等效流。关闭处理器在调用流的 {@link #close()} 方法时运行，并按添加的顺序执行。
     * 即使前面的关闭处理器抛出异常，所有关闭处理器都会运行。如果任何关闭处理器抛出异常，第一个抛出的异常将传递给
     * {@code close()} 方法的调用者，剩余的异常将作为抑制异常添加到该异常中（除非剩余的异常与第一个异常相同，因为异常不能抑制自身）。可能返回自身。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @param closeHandler 当流关闭时执行的任务
     * @return 一个流，如果流关闭时会运行处理器
     */
    S onClose(Runnable closeHandler);

    /**
     * 关闭此流，导致此流管道的所有关闭处理器被调用。
     *
     * @see AutoCloseable#close()
     */
    @Override
    void close();
}
