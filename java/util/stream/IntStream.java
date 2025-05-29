
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * <p>以下示例说明了使用 {@link Stream} 和 {@link IntStream} 的聚合操作，计算红色小部件的重量总和：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * 有关流、流操作、流管道和并行的其他说明，请参阅 {@link Stream} 的类文档和 <a href="package-summary.html">java.util.stream</a> 的包文档。
 *
 * @since 1.8
 * @see Stream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface IntStream extends BaseStream<Integer, IntStream> {

    /**
     * 返回一个包含此流中满足给定谓词的元素的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param predicate 应用于每个元素以确定是否应包含该元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *                  <a href="package-summary.html#Statelessness">无状态的</a> 谓词
     * @return 新的流
     */
    IntStream filter(IntPredicate predicate);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a> 函数
     * @return 新的流
     */
    IntStream map(IntUnaryOperator mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的对象值 {@code Stream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param <U> 新流的元素类型
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a> 函数
     * @return 新的流
     */
    <U> Stream<U> mapToObj(IntFunction<? extends U> mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code LongStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a> 函数
     * @return 新的流
     */
    LongStream mapToLong(IntToLongFunction mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code DoubleStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a> 函数
     * @return 新的流
     */
    DoubleStream mapToDouble(IntToDoubleFunction mapper);

    /**
     * 返回一个包含将给定映射函数应用于此流中每个元素后生成的映射流内容的流。每个映射流在将其内容放入此流后都会被 {@link java.util.stream.BaseStream#close() 关闭}。
     * （如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰的</a>、
     *               <a href="package-summary.html#Statelessness">无状态的</a> 函数，该函数生成一个包含新值的 {@code IntStream}
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
     * 返回一个包含此流中元素按排序顺序排列的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的
     * 中间操作</a>。
     *
     * @return 新的流
     */
    IntStream sorted();

    /**
     * 返回一个包含此流中元素的流，并在从结果流中消耗元素时对每个元素执行提供的操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * <p>对于并行流管道，当上游操作使元素可用时，操作可能在任何时间、任何线程中被调用。如果操作修改了共享状态，
     * 则需要提供所需的同步。
     *
     * @apiNote 此方法主要用于支持调试，您希望看到元素在管道中经过某个点时的情况：
     * <pre>{@code
     *     IntStream.of(1, 2, 3, 4)
     *         .filter(e -> e > 2)
     *         .peek(e -> System.out.println("过滤后的值: " + e))
     *         .map(e -> e * e)
     *         .peek(e -> System.out.println("映射后的值: " + e))
     *         .sum();
     * }</pre>
     *
     * @param action 在从流中消耗元素时对元素执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     * @return 新的流
     */
    IntStream peek(IntConsumer action);

    /**
     * 返回一个包含此流中元素的流，但长度不超过 {@code maxSize}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路
     * 有状态的中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code limit()} 通常是一个廉价的操作，但在有序的并行管道中，特别是对于较大的 {@code maxSize} 值，
     * 它可能非常昂贵，因为 {@code limit(n)} 必须返回不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的前 <em>n</em> 个元素。
     * 使用无序的流源（如 {@link #generate(IntSupplier)}) 或通过 {@link #unordered()} 移除排序约束，可能会在并行管道中显著加速 {@code limit()}，
     * 如果您的情况允许的话。如果需要与遇到顺序一致，并且您在并行管道中使用 {@code limit()} 时遇到性能或内存使用问题，
     * 使用 {@link #sequential()} 切换到顺序执行可能会提高性能。
     *
     * @param maxSize 流应限制的元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code maxSize} 为负数
     */
    IntStream limit(long maxSize);

    /**
     * 返回一个包含此流中剩余元素的流，在丢弃流的前 {@code n} 个元素之后。
     * 如果此流包含的元素少于 {@code n} 个，则返回一个空流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的
     * 中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code skip()} 通常是一个廉价的操作，但在有序的并行管道中，特别是对于较大的 {@code n} 值，
     * 它可能非常昂贵，因为 {@code skip(n)} 必须跳过不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的前 <em>n</em> 个元素。
     * 使用无序的流源（如 {@link #generate(IntSupplier)}) 或通过 {@link #unordered()} 移除排序约束，可能会在并行管道中显著加速 {@code skip()}，
     * 如果您的情况允许的话。如果需要与遇到顺序一致，并且您在并行管道中使用 {@code skip()} 时遇到性能或内存使用问题，
     * 使用 {@link #sequential()} 切换到顺序执行可能会提高性能。
     *
     * @param n 要跳过的前导元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code n} 为负数
     */
    IntStream skip(long n);

    /**
     * 对此流中的每个元素执行一个操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * <p>对于并行流管道，此操作不保证尊重流的遇到顺序，因为这样做会牺牲并行性的优势。对于任何给定的元素，
     * 操作可能在库选择的任何时间、任何线程中执行。如果操作访问共享状态，则需要提供所需的同步。
     *
     * @param action 在元素上执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     */
    void forEach(IntConsumer action);

    /**
     * 对此流中的每个元素执行一个操作，保证对于具有定义的遇到顺序的流，每个元素都按遇到顺序处理。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @param action 在元素上执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     * @see #forEach(IntConsumer)
     */
    void forEachOrdered(IntConsumer action);

    /**
     * 返回一个包含此流中元素的数组。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 包含此流中元素的数组
     */
    int[] toArray();

                /**
     * 对此流的元素执行<a href="package-summary.html#Reduction">归约</a>操作，使用提供的初始值和
     * <a href="package-summary.html#Associativity">结合性</a>累加函数，并返回归约值。这等价于：
     * <pre>{@code
     *     int result = identity;
     *     for (int element : this stream)
     *         result = accumulator.applyAsInt(result, element)
     *     return result;
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>初始值{@code identity}必须是累加函数的身份值。这意味着对于所有{@code x}，
     * {@code accumulator.apply(identity, x)}等于{@code x}。累加函数必须是
     * <a href="package-summary.html#Associativity">结合性</a>函数。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 求和、最小值、最大值和平均值都是归约的特殊情况。求和一个数字流可以表示为：
     *
     * <pre>{@code
     *     int sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * 或者更简洁地：
     *
     * <pre>{@code
     *     int sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>虽然这可能看起来比在循环中简单地改变运行总计更绕，但归约操作可以更优雅地并行化，而不需要额外的
     * 同步，并且大大减少了数据竞争的风险。
     *
     * @param identity 累加函数的初始值
     * @param op 一个<a href="package-summary.html#Associativity">结合性</a>、
     *           <a href="package-summary.html#NonInterference">非干扰性</a>、
     *           <a href="package-summary.html#Statelessness">无状态</a>
     *           用于组合两个值的函数
     * @return 归约的结果
     * @see #sum()
     * @see #min()
     * @see #max()
     * @see #average()
     */
    int reduce(int identity, IntBinaryOperator op);

    /**
     * 对此流的元素执行<a href="package-summary.html#Reduction">归约</a>操作，使用
     * <a href="package-summary.html#Associativity">结合性</a>累加函数，并返回一个描述归约值的
     * {@code OptionalInt}，如果有的话。这等价于：
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
     * <p>累加函数必须是<a href="package-summary.html#Associativity">结合性</a>函数。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param op 一个<a href="package-summary.html#Associativity">结合性</a>、
     *           <a href="package-summary.html#NonInterference">非干扰性</a>、
     *           <a href="package-summary.html#Statelessness">无状态</a>
     *           用于组合两个值的函数
     * @return 归约的结果
     * @see #reduce(int, IntBinaryOperator)
     */
    OptionalInt reduce(IntBinaryOperator op);

    /**
     * 对此流的元素执行<a href="package-summary.html#MutableReduction">可变归约</a>操作。可变归约是一种
     * 归约值是可变结果容器（如{@code ArrayList}），并且通过更新结果的状态而不是替换结果来合并元素的操作。
     * 这产生的结果等价于：
     * <pre>{@code
     *     R result = supplier.get();
     *     for (int element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>像{@link #reduce(int, IntBinaryOperator)}一样，{@code collect}操作可以在不需额外同步的情况下并行化。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param <R> 结果类型
     * @param supplier 一个创建新结果容器的函数。对于并行执行，此函数可能被调用多次，每次必须返回一个新值。
     * @param accumulator 一个<a href="package-summary.html#Associativity">结合性</a>、
     *                    <a href="package-summary.html#NonInterference">非干扰性</a>、
     *                    <a href="package-summary.html#Statelessness">无状态</a>
     *                    用于将额外元素合并到结果中的函数
     * @param combiner 一个<a href="package-summary.html#Associativity">结合性</a>、
     *                    <a href="package-summary.html#NonInterference">非干扰性</a>、
     *                    <a href="package-summary.html#Statelessness">无状态</a>
     *                    用于组合两个值的函数，必须与累加函数兼容
     * @return 归约的结果
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     */
    <R> R collect(Supplier<R> supplier,
                  ObjIntConsumer<R> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * 返回此流中元素的和。这是一个<a href="package-summary.html#Reduction">归约</a>的特殊情况
     * 并等价于：
     * <pre>{@code
     *     return reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 此流中元素的和
     */
    int sum();


/**
 * 返回描述此流最小元素的 {@code OptionalInt}，如果此流为空，则返回空的可选值。这是一个
 * <a href="package-summary.html#Reduction">归约</a> 的特殊情况，等价于：
 * <pre>{@code
 *     return reduce(Integer::min);
 * }</pre>
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">终结操作</a>。
 *
 * @return 包含此流最小元素的 {@code OptionalInt}，如果流为空，则返回空的 {@code OptionalInt}
 */
OptionalInt min();

/**
 * 返回描述此流最大元素的 {@code OptionalInt}，如果此流为空，则返回空的可选值。这是一个
 * <a href="package-summary.html#Reduction">归约</a> 的特殊情况，等价于：
 * <pre>{@code
 *     return reduce(Integer::max);
 * }</pre>
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">终结操作</a>。
 *
 * @return 包含此流最大元素的 {@code OptionalInt}，如果流为空，则返回空的 {@code OptionalInt}
 */
OptionalInt max();

/**
 * 返回此流中元素的数量。这是一个 <a href="package-summary.html#Reduction">归约</a> 的特殊情况，
 * 等价于：
 * <pre>{@code
 *     return mapToLong(e -> 1L).sum();
 * }</pre>
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">终结操作</a>。
 *
 * @return 此流中元素的数量
 */
long count();

/**
 * 返回描述此流元素算术平均值的 {@code OptionalDouble}，如果此流为空，则返回空的可选值。这是一个
 * <a href="package-summary.html#Reduction">归约</a> 的特殊情况。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">终结操作</a>。
 *
 * @return 包含此流元素平均值的 {@code OptionalDouble}，如果流为空，则返回空的可选值
 */
OptionalDouble average();

/**
 * 返回描述此流元素各种汇总数据的 {@code IntSummaryStatistics}。这是一个
 * <a href="package-summary.html#Reduction">归约</a> 的特殊情况。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">终结操作</a>。
 *
 * @return 描述此流元素各种汇总数据的 {@code IntSummaryStatistics}
 */
IntSummaryStatistics summaryStatistics();

/**
 * 返回此流中是否有元素匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回
 * {@code false} 且不评估谓词。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">短路终结操作</a>。
 *
 * @apiNote
 * 此方法评估谓词在流元素上的 <em>存在量化</em>（存在 x 使得 P(x)）。
 *
 * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
 *                  <a href="package-summary.html#Statelessness">无状态的</a>
 *                  谓词，应用于此流的元素
 * @return 如果流中存在匹配提供的谓词的元素，则返回 {@code true}，否则返回 {@code false}
 */
boolean anyMatch(IntPredicate predicate);

/**
 * 返回此流中所有元素是否匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回
 * {@code true} 且不评估谓词。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">短路终结操作</a>。
 *
 * @apiNote
 * 此方法评估谓词在流元素上的 <em>全称量化</em>（对所有 x 有 P(x)）。如果流为空，则量化被认为是 <em>空满足</em>，
 * 总是返回 {@code true}（无论 P(x) 如何）。
 *
 * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
 *                  <a href="package-summary.html#Statelessness">无状态的</a>
 *                  谓词，应用于此流的元素
 * @return 如果流中所有元素都匹配提供的谓词或流为空，则返回 {@code true}，否则返回 {@code false}
 */
boolean allMatch(IntPredicate predicate);

/**
 * 返回此流中是否有元素不匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回
 * {@code true} 且不评估谓词。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">短路终结操作</a>。
 *
 * @apiNote
 * 此方法评估否定谓词在流元素上的 <em>全称量化</em>（对所有 x 有 ~P(x)）。如果流为空，则量化被认为是空满足，
 * 总是返回 {@code true}，无论 P(x) 如何。
 *
 * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
 *                  <a href="package-summary.html#Statelessness">无状态的</a>
 *                  谓词，应用于此流的元素
 * @return 如果流中没有元素匹配提供的谓词或流为空，则返回 {@code true}，否则返回 {@code false}
 */
boolean noneMatch(IntPredicate predicate);

/**
 * 返回描述此流第一个元素的 {@link OptionalInt}，如果流为空，则返回空的 {@code OptionalInt}。如果流没有遍历顺序，
 * 则可能返回任何元素。
 *
 * <p>这是一个 <a href="package-summary.html#StreamOps">短路终结操作</a>。
 *
 * @return 描述此流第一个元素的 {@code OptionalInt}，如果流为空，则返回空的 {@code OptionalInt}
 */
OptionalInt findFirst();

                /**
     * 返回一个描述流中某个元素的 {@link OptionalInt}，如果流为空，则返回一个空的 {@code OptionalInt}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * <p>此操作的行为是显式非确定性的；它可以自由选择流中的任何元素。这是为了在并行操作中实现最大性能；代价是多次调用
     * 可能不会返回相同的结果。 （如果需要稳定的结果，请改用 {@link #findFirst()}。）
     *
     * @return 描述此流中某个元素的 {@code OptionalInt}，如果流为空，则返回一个空的 {@code OptionalInt}
     * @see #findFirst()
     */
    OptionalInt findAny();

    /**
     * 返回一个由此流的元素转换为 {@code long} 组成的 {@code LongStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 由此流的元素转换为 {@code long} 组成的 {@code LongStream}
     */
    LongStream asLongStream();

    /**
     * 返回一个由此流的元素转换为 {@code double} 组成的 {@code DoubleStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 由此流的元素转换为 {@code double} 组成的 {@code DoubleStream}
     */
    DoubleStream asDoubleStream();

    /**
     * 返回一个由此流的元素转换为 {@code Integer} 组成的 {@code Stream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 由此流的元素转换为 {@code Integer} 组成的 {@code Stream}
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
     * 返回一个用于 {@code IntStream} 的构建器。
     *
     * @return 一个流构建器
     */
    public static Builder builder() {
        return new Streams.IntStreamBuilderImpl();
    }

    /**
     * 返回一个空的顺序 {@code IntStream}。
     *
     * @return 一个空的顺序流
     */
    public static IntStream empty() {
        return StreamSupport.intStream(Spliterators.emptyIntSpliterator(), false);
    }

    /**
     * 返回一个包含单个元素的顺序 {@code IntStream}。
     *
     * @param t 单个元素
     * @return 一个单元素的顺序流
     */
    public static IntStream of(int t) {
        return StreamSupport.intStream(new Streams.IntStreamBuilderImpl(t), false);
    }

    /**
     * 返回一个顺序有序流，其元素是指定的值。
     *
     * @param values 新流的元素
     * @return 新的流
     */
    public static IntStream of(int... values) {
        return Arrays.stream(values);
    }

    /**
     * 返回一个通过迭代应用函数 {@code f} 到初始元素 {@code seed} 生成的无限顺序有序 {@code IntStream}，
     * 生成的流包含 {@code seed}，{@code f(seed)}，{@code f(f(seed))} 等。
     *
     * <p>流中的第一个元素（位置 {@code 0}）将是提供的 {@code seed}。对于 {@code n > 0}，位置
     * {@code n} 的元素将是应用函数 {@code f} 到位置 {@code n - 1} 的元素的结果。
     *
     * @param seed 初始元素
     * @param f 一个函数，用于将前一个元素转换为新元素
     * @return 一个新的顺序 {@code IntStream}
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
     * @param s 用于生成元素的 {@code IntSupplier}
     * @return 一个新的无限顺序无序 {@code IntStream}
     */
    public static IntStream generate(IntSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.intStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfInt(Long.MAX_VALUE, s), false);
    }

    /**
     * 返回一个从 {@code startInclusive}（包含）到 {@code endExclusive}（不包含）的顺序有序 {@code IntStream}，
     * 步长为递增的 {@code 1}。
     *
     * @apiNote
     * <p>可以使用以下 {@code for} 循环顺序生成等效的递增值序列：
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
     * 返回一个从 {@code startInclusive}（包含）到 {@code endInclusive}（包含）的递增步长为 {@code 1} 的顺序有序 {@code IntStream}。
     *
     * @apiNote
     * <p>可以使用以下 {@code for} 循环顺序生成等效的递增值序列：
     * <pre>{@code
     *     for (int i = startInclusive; i <= endInclusive ; i++) { ... }
     * }</pre>
     *
     * @param startInclusive 初始值（包含）
     * @param endInclusive 上界（包含）
     * @return 该范围内的 {@code int} 元素的顺序 {@code IntStream}
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
     * 创建一个懒连接的流，其元素是第一个流的所有元素，后跟第二个流的所有元素。如果两个输入流都是有序的，则结果流是有序的；如果任一输入流是并行的，则结果流是并行的。当结果流关闭时，两个输入流的关闭处理程序都会被调用。
     *
     * @implNote
     * 通过重复连接构建流时要谨慎。访问深度连接的流的元素可能导致深度调用链，甚至导致 {@code StackOverflowException}。
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
     * <p>流构建器有一个生命周期，从构建阶段开始，在此阶段可以添加元素，然后转换为已构建阶段，此后不能再添加元素。当调用 {@link #build()} 方法时，构建阶段开始，该方法创建一个有序流，其元素是添加到流构建器中的元素，顺序与添加时相同。
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
         * 默认实现的行为类似于：
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
         * 构建流，将此构建器转换为已构建状态。
         * 如果构建器已进入已构建状态后仍有进一步的操作尝试，则抛出 {@code IllegalStateException}。
         *
         * @return 已构建的流
         * @throws IllegalStateException 如果构建器已转换为已构建状态
         */
        IntStream build();
    }
}
