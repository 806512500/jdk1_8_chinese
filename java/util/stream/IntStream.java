
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

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

/**
 * 一个支持顺序和并行聚合操作的原始 int 值元素序列。这是 {@link Stream} 的 int 原始类型特化。
 *
 * <p>以下示例说明了使用 {@link Stream} 和 {@link IntStream} 进行聚合操作，计算红色小部件的总重量：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * 有关流、流操作、流管道和并行的其他规范，请参阅 {@link Stream} 类文档和 <a href="package-summary.html">java.util.stream</a> 包文档。
 *
 * @since 1.8
 * @see Stream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface IntStream extends BaseStream<Integer, IntStream> {

    /**
     * 返回一个包含此流中匹配给定谓词的元素的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，用于确定每个元素是否应包含在内
     * @return 新的流
     */
    IntStream filter(IntPredicate predicate);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，用于应用于每个元素
     * @return 新的流
     */
    IntStream map(IntUnaryOperator mapper);

    /**
     * 返回一个对象值的 {@code Stream}，包含将给定函数应用于此流中元素的结果。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param <U> 新流的元素类型
     * @param mapper 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，用于应用于每个元素
     * @return 新的流
     */
    <U> Stream<U> mapToObj(IntFunction<? extends U> mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code LongStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，用于应用于每个元素
     * @return 新的流
     */
    LongStream mapToLong(IntToLongFunction mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code DoubleStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，用于应用于每个元素
     * @return 新的流
     */
    DoubleStream mapToDouble(IntToDoubleFunction mapper);

    /**
     * 返回一个包含将提供的映射函数应用于此流中每个元素后生成的流的内容的流。每个映射流在将其内容放入此流后都会被 {@link java.util.stream.BaseStream#close() 关闭}。（如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，用于应用于每个元素，生成一个新的 {@code IntStream} 值
     * @return 新的流
     * @see Stream#flatMap(Function)
     */
    IntStream flatMap(IntFunction<? extends IntStream> mapper);

    /**
     * 返回一个包含此流中不同元素的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @return 新的流
     */
    IntStream distinct();

    /**
     * 返回一个包含此流中按排序顺序排列的元素的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @return 新的流
     */
    IntStream sorted();

    /**
     * 返回一个包含此流中元素的流，并在从结果流中消耗元素时对每个元素执行提供的操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * <p>对于并行流管道，操作可能在元素由上游操作提供时的任何时间和任何线程中调用。如果操作修改了共享状态，则需要提供所需的同步。
     *
     * @apiNote 此方法主要用于调试，您希望看到元素在管道中的某个点流过：
     * <pre>{@code
     *     IntStream.of(1, 2, 3, 4)
     *         .filter(e -> e > 2)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(e -> e * e)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .sum();
     * }</pre>
     *
     * @param action 一个 <a href="package-summary.html#NonInterference">
     *               非干扰的</a> 操作，用于在从流中消耗元素时执行
     * @return 新的流
     */
    IntStream peek(IntConsumer action);

    /**
     * 返回一个包含此流中元素的流，但长度不超过 {@code maxSize}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路有状态的中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code limit()} 通常是一个廉价的操作，但在有序并行管道中，特别是对于较大的 {@code maxSize} 值，它可能非常昂贵，因为 {@code limit(n)} 被约束为返回不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的 <em>前 n</em> 个元素。使用无序的流源（如 {@link #generate(IntSupplier)}) 或移除排序约束（使用 {@link #unordered()}）可能在并行管道中显著提高 {@code limit()} 的速度，前提是您的情况允许。如果需要与遇到顺序保持一致，并且在并行管道中使用 {@code limit()} 时性能或内存利用率不佳，切换到顺序执行（使用 {@link #sequential()}）可能会提高性能。
     *
     * @param maxSize 流应限制的最大长度
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code maxSize} 为负
     */
    IntStream limit(long maxSize);

    /**
     * 返回一个包含此流中剩余元素的流，跳过流中的前 {@code n} 个元素。
     * 如果此流包含的元素少于 {@code n} 个，则返回一个空流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code skip()} 通常是一个廉价的操作，但在有序并行管道中，特别是对于较大的 {@code n} 值，它可能非常昂贵，因为 {@code skip(n)} 被约束为跳过不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的 <em>前 n</em> 个元素。使用无序的流源（如 {@link #generate(IntSupplier)}) 或移除排序约束（使用 {@link #unordered()}）可能在并行管道中显著提高 {@code skip()} 的速度，前提是您的情况允许。如果需要与遇到顺序保持一致，并且在并行管道中使用 {@code skip()} 时性能或内存利用率不佳，切换到顺序执行（使用 {@link #sequential()}）可能会提高性能。
     *
     * @param n 要跳过的前 n 个元素的数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code n} 为负
     */
    IntStream skip(long n);

    /**
     * 对此流中的每个元素执行一个操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * <p>对于并行流管道，此操作不保证尊重流的遇到顺序，因为这样做会牺牲并行的好处。对于任何给定的元素，操作可能在库选择的任何时间和任何线程中执行。如果操作访问共享状态，则需要提供所需的同步。
     *
     * @param action 一个 <a href="package-summary.html#NonInterference">
     *               非干扰的</a> 操作，用于在元素上执行
     */
    void forEach(IntConsumer action);

    /**
     * 对此流中的每个元素执行一个操作，保证对于具有定义的遇到顺序的流，每个元素都按遇到顺序处理。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param action 一个 <a href="package-summary.html#NonInterference">
     *               非干扰的</a> 操作，用于在元素上执行
     * @see #forEach(IntConsumer)
     */
    void forEachOrdered(IntConsumer action);

    /**
     * 返回一个包含此流中元素的数组。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 一个包含此流中元素的数组
     */
    int[] toArray();

    /**
     * 对此流中的元素执行 <a href="package-summary.html#Reduction">归约</a>，使用提供的标识值和一个 <a href="package-summary.html#Associativity">结合的</a>
     * 累加函数，并返回归约值。这等同于：
     * <pre>{@code
     *     int result = identity;
     *     for (int element : this stream)
     *         result = accumulator.applyAsInt(result, element)
     *     return result;
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>标识值必须是累加函数的标识值。这意味着对于所有 {@code x}，
     * {@code accumulator.apply(identity, x)} 等于 {@code x}。累加函数必须是 <a href="package-summary.html#Associativity">结合的</a>。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 求和、最小值、最大值和平均值都是归约的特殊情况。求和一个整数流可以表示为：
     *
     * <pre>{@code
     *     int sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * 或更简洁地：
     *
     * <pre>{@code
     *     int sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>虽然这可能看起来比在循环中简单地累加一个运行总计更迂回，但归约操作并行化更优雅，不需要额外的同步，并且大大减少了数据竞争的风险。
     *
     * @param identity 累加函数的标识值
     * @param op 一个 <a href="package-summary.html#Associativity">结合的</a>、
     *           <a href="package-summary.html#NonInterference">非干扰的</a>、
     *           <a href="package-summary.html#Statelessness">无状态的</a>
     *           函数，用于组合两个值
     * @return 归约结果
     * @see #sum()
     * @see #min()
     * @see #max()
     * @see #average()
     */
    int reduce(int identity, IntBinaryOperator op);


                /**
     * 对此流的元素执行<a href="package-summary.html#Reduction">归约</a>操作，使用
     * <a href="package-summary.html#Associativity">结合性</a>累加函数，并返回一个描述归约值的
     * {@code OptionalInt}，如果有的话。这相当于：
     * <pre>{@code
     *     boolean foundAny = false;
     *     int result = null;
     *     for (int element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.applyAsInt(result, element);
     *     }
     *     return foundAny ? OptionalInt.of(result) : OptionalInt.empty();
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>{@code accumulator}函数必须是一个
     * <a href="package-summary.html#Associativity">结合性</a>函数。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @param op 一个<a href="package-summary.html#Associativity">结合性</a>、
     *           <a href="package-summary.html#NonInterference">非干扰性</a>、
     *           <a href="package-summary.html#Statelessness">无状态性</a>
     *           的用于组合两个值的函数
     * @return 归约的结果
     * @see #reduce(int, IntBinaryOperator)
     */
    OptionalInt reduce(IntBinaryOperator op);

    /**
     * 对此流的元素执行一个<a href="package-summary.html#MutableReduction">可变归约</a>操作。
     * 可变归约是一种归约值是可变结果容器（如{@code ArrayList}）的操作，元素通过更新结果的状态而不是替换结果来合并。
     * 这产生的结果等同于：
     * <pre>{@code
     *     R result = supplier.get();
     *     for (int element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>像{@link #reduce(int, IntBinaryOperator)}一样，{@code collect}操作可以并行化而不需要额外的同步。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @param <R> 结果的类型
     * @param supplier 一个创建新结果容器的函数。对于并行执行，此函数可能被调用多次且每次必须返回一个新值。
     * @param accumulator 一个<a href="package-summary.html#Associativity">结合性</a>、
     *                    <a href="package-summary.html#NonInterference">非干扰性</a>、
     *                    <a href="package-summary.html#Statelessness">无状态性</a>
     *                    的用于将额外元素合并到结果中的函数
     * @param combiner 一个<a href="package-summary.html#Associativity">结合性</a>、
     *                    <a href="package-summary.html#NonInterference">非干扰性</a>、
     *                    <a href="package-summary.html#Statelessness">无状态性</a>
     *                    的用于组合两个值的函数，必须与累加器函数兼容
     * @return 归约的结果
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     */
    <R> R collect(Supplier<R> supplier,
                  ObjIntConsumer<R> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * 返回此流中元素的和。这是一个<a href="package-summary.html#Reduction">归约</a>的特殊情况
     * 并等同于：
     * <pre>{@code
     *     return reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 此流中元素的和
     */
    int sum();

    /**
     * 返回一个描述此流中最小元素的{@code OptionalInt}，如果此流为空，则返回一个空的可选值。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况
     * 并等同于：
     * <pre>{@code
     *     return reduce(Integer::min);
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 一个包含此流中最小元素的{@code OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}
     */
    OptionalInt min();

    /**
     * 返回一个描述此流中最大元素的{@code OptionalInt}，如果此流为空，则返回一个空的可选值。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况
     * 并等同于：
     * <pre>{@code
     *     return reduce(Integer::max);
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 一个包含此流中最大元素的{@code OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}
     */
    OptionalInt max();

    /**
     * 返回此流中元素的数量。这是一个<a href="package-summary.html#Reduction">归约</a>的特殊情况
     * 并等同于：
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 此流中元素的数量
     */
    long count();

    /**
     * 返回一个描述此流中元素算术平均值的{@code OptionalDouble}，如果此流为空，则返回一个空的可选值。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 一个包含此流中元素平均值的{@code OptionalDouble}，如果流为空，则返回一个空的可选值
     */
    OptionalDouble average();

    /**
     * 返回一个描述此流中元素各种汇总数据的{@code IntSummaryStatistics}。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终结操作</a>。
     *
     * @return 一个描述此流中元素各种汇总数据的{@code IntSummaryStatistics}
     */
    IntSummaryStatistics summaryStatistics();

    /**
     * 返回此流中是否有元素匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会评估谓词。
     * 如果流为空，则返回{@code false}且不评估谓词。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终结操作</a>。
     *
     * @apiNote
     * 此方法评估谓词在流元素上的<em>存在量化</em>（存在x P(x)）。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">非干扰性</a>、
     *                  <a href="package-summary.html#Statelessness">无状态性</a>
     *                  的应用于此流元素的谓词
     * @return 如果此流中有元素匹配提供的谓词，则返回{@code true}，否则返回{@code false}
     */
    boolean anyMatch(IntPredicate predicate);

    /**
     * 返回此流中所有元素是否匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会评估谓词。
     * 如果流为空，则返回{@code true}且不评估谓词。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终结操作</a>。
     *
     * @apiNote
     * 此方法评估谓词在流元素上的<em>全称量化</em>（对于所有x P(x)）。如果流为空，则量化被认为是<em>空真</em>
     * 且总是{@code true}（无论P(x)如何）。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">非干扰性</a>、
     *                  <a href="package-summary.html#Statelessness">无状态性</a>
     *                  的应用于此流元素的谓词
     * @return 如果此流中所有元素匹配提供的谓词或流为空，则返回{@code true}，否则返回{@code false}
     */
    boolean allMatch(IntPredicate predicate);

    /**
     * 返回此流中是否有元素不匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会评估谓词。
     * 如果流为空，则返回{@code true}且不评估谓词。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终结操作</a>。
     *
     * @apiNote
     * 此方法评估否定谓词在流元素上的<em>全称量化</em>（对于所有x ~P(x)）。如果流为空，则量化被认为是空真
     * 且总是{@code true}，无论P(x)如何。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">非干扰性</a>、
     *                  <a href="package-summary.html#Statelessness">无状态性</a>
     *                  的应用于此流元素的谓词
     * @return 如果此流中没有元素匹配提供的谓词或流为空，则返回{@code true}，否则返回{@code false}
     */
    boolean noneMatch(IntPredicate predicate);

    /**
     * 返回一个描述此流中第一个元素的{@link OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}。
     * 如果流没有遍历顺序，则可以返回任何元素。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终结操作</a>。
     *
     * @return 一个描述此流中第一个元素的{@code OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}
     */
    OptionalInt findFirst();

    /**
     * 返回一个描述此流中某个元素的{@link OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终结操作</a>。
     *
     * <p>此操作的行为显式地是非确定性的；它可以自由选择流中的任何元素。这是为了在并行操作中实现最大性能；
     * 代价是多次调用同一源可能不会返回相同的结果。（如果需要稳定的结果，请使用{@link #findFirst()}。）
     *
     * @return 一个描述此流中某个元素的{@code OptionalInt}，如果流为空，则返回一个空的{@code OptionalInt}
     * @see #findFirst()
     */
    OptionalInt findAny();

    /**
     * 返回一个由此流中的元素转换为{@code long}的{@code LongStream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 一个由此流中的元素转换为{@code long}的{@code LongStream}
     */
    LongStream asLongStream();

    /**
     * 返回一个由此流中的元素转换为{@code double}的{@code DoubleStream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 一个由此流中的元素转换为{@code double}的{@code DoubleStream}
     */
    DoubleStream asDoubleStream();

    /**
     * 返回一个由此流中的元素装箱为{@code Integer}的{@code Stream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 一个由此流中的元素装箱为{@code Integer}的{@code Stream}
     */
    Stream<Integer> boxed();

    @Override
    IntStream sequential();

    @Override
    IntStream parallel();

    @Override
    PrimitiveIterator.OfInt iterator();

    @Override
    Spliterator.OfInt spliterator();

    // 静态工厂

    /**
     * 返回一个用于{@code IntStream}的构建器。
     *
     * @return 一个流构建器
     */
    public static Builder builder() {
        return new Streams.IntStreamBuilderImpl();
    }

    /**
     * 返回一个空的顺序{@code IntStream}。
     *
     * @return 一个空的顺序流
     */
    public static IntStream empty() {
        return StreamSupport.intStream(Spliterators.emptyIntSpliterator(), false);
    }

    /**
     * 返回一个包含单个元素的顺序{@code IntStream}。
     *
     * @param t 单个元素
     * @return 一个单例顺序流
     */
    public static IntStream of(int t) {
        return StreamSupport.intStream(new Streams.IntStreamBuilderImpl(t), false);
    }

    /**
     * 返回一个顺序有序的流，其元素是指定的值。
     *
     * @param values 新流的元素
     * @return 新流
     */
    public static IntStream of(int... values) {
        return Arrays.stream(values);
    }

    /**
     * 返回一个由函数{@code f}迭代应用于初始元素{@code seed}产生的无限顺序有序的{@code IntStream}，
     * 产生一个包含{@code seed}、{@code f(seed)}、{@code f(f(seed))}等的流。
     *
     * <p>流中的第一个元素（位置{@code 0}）将是提供的{@code seed}。对于{@code n > 0}，位置
     * {@code n}的元素将是将函数{@code f}应用于位置{@code n - 1}的元素的结果。
     *
     * @param seed 初始元素
     * @param f 一个应用于前一个元素以生成新元素的函数
     * @return 一个新的顺序{@code IntStream}
     */
    public static IntStream iterate(final int seed, final IntUnaryOperator f) {
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfInt iterator = new PrimitiveIterator.OfInt() {
            int t = seed;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public int nextInt() {
                int v = t;
                t = f.applyAsInt(t);
                return v;
            }
        };
        return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }


                /**
     * 返回一个无限顺序无序流，其中每个元素由提供的 {@code IntSupplier} 生成。这适用于生成常量流、随机元素流等。
     *
     * @param s 生成元素的 {@code IntSupplier}
     * @return 一个新的无限顺序无序 {@code IntStream}
     */
    public static IntStream generate(IntSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.intStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfInt(Long.MAX_VALUE, s), false);
    }

    /**
     * 返回一个从 {@code startInclusive}（包含）到 {@code endExclusive}（不包含）的顺序有序 {@code IntStream}，步长为 {@code 1}。
     *
     * @apiNote
     * <p>可以使用以下 {@code for} 循环顺序生成相同的递增值序列：
     * <pre>{@code
     *     for (int i = startInclusive; i < endExclusive ; i++) { ... }
     * }</pre>
     *
     * @param startInclusive 初始值（包含）
     * @param endExclusive 上界（不包含）
     * @return 一个顺序 {@code IntStream}，包含指定范围的 {@code int} 元素
     */
    public static IntStream range(int startInclusive, int endExclusive) {
        if (startInclusive >= endExclusive) {
            return empty();
        } else {
            return StreamSupport.intStream(
                    new Streams.RangeIntSpliterator(startInclusive, endExclusive, false), false);
        }
    }

    /**
     * 返回一个从 {@code startInclusive}（包含）到 {@code endInclusive}（包含）的顺序有序 {@code IntStream}，步长为 {@code 1}。
     *
     * @apiNote
     * <p>可以使用以下 {@code for} 循环顺序生成相同的递增值序列：
     * <pre>{@code
     *     for (int i = startInclusive; i <= endInclusive ; i++) { ... }
     * }</pre>
     *
     * @param startInclusive 初始值（包含）
     * @param endInclusive 上界（包含）
     * @return 一个顺序 {@code IntStream}，包含指定范围的 {@code int} 元素
     */
    public static IntStream rangeClosed(int startInclusive, int endInclusive) {
        if (startInclusive > endInclusive) {
            return empty();
        } else {
            return StreamSupport.intStream(
                    new Streams.RangeIntSpliterator(startInclusive, endInclusive, true), false);
        }
    }

    /**
     * 创建一个惰性连接的流，其元素是第一个流的所有元素，后跟第二个流的所有元素。如果两个输入流都是有序的，则结果流也是有序的；如果任一输入流是并行的，则结果流也是并行的。当结果流被关闭时，两个输入流的关闭处理程序都会被调用。
     *
     * @implNote
     * 通过重复连接构建流时要谨慎。访问深层连接流的元素可能导致深层调用链，甚至导致 {@code StackOverflowException}。
     *
     * @param a 第一个流
     * @param b 第二个流
     * @return 两个输入流的连接
     */
    public static IntStream concat(IntStream a, IntStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        Spliterator.OfInt split = new Streams.ConcatSpliterator.OfInt(
                a.spliterator(), b.spliterator());
        IntStream stream = StreamSupport.intStream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    /**
     * 一个可变的 {@code IntStream} 构建器。
     *
     * <p>流构建器有一个生命周期，开始于构建阶段，在此期间可以添加元素，然后转换为已构建状态，之后不能再添加元素。当调用 {@link #build()} 方法时，构建阶段开始，创建一个顺序流，其元素是添加到流构建器中的元素，顺序与添加的顺序相同。
     *
     * @see IntStream#builder()
     * @since 1.8
     */
    public interface Builder extends IntConsumer {

        /**
         * 向正在构建的流中添加一个元素。
         *
         * @throws IllegalStateException 如果构建器已转换为已构建状态
         */
        @Override
        void accept(int t);

        /**
         * 向正在构建的流中添加一个元素。
         *
         * @implSpec
         * 默认实现的行为如下：
         * <pre>{@code
         *     accept(t)
         *     return this;
         * }</pre>
         *
         * @param t 要添加的元素
         * @return {@code this} 构建器
         * @throws IllegalStateException 如果构建器已转换为已构建状态
         */
        default Builder add(int t) {
            accept(t);
            return this;
        }

        /**
         * 构建流，将此构建器转换为已构建状态。如果构建器已进入已构建状态后仍有进一步的操作尝试，则抛出 {@code IllegalStateException}。
         *
         * @return 已构建的流
         * @throws IllegalStateException 如果构建器已转换为已构建状态
         */
        IntStream build();
    }
}
