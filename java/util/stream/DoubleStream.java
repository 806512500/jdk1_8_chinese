
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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

/**
 * 一个支持顺序和并行聚合操作的原始 double 值元素序列。这是 {@link Stream} 的 double 原始类型特化。
 *
 * <p>以下示例说明了使用 {@link Stream} 和 {@link DoubleStream} 进行聚合操作，计算红色小部件的重量总和：
 *
 * <pre>{@code
 *     double sum = widgets.stream()
 *                         .filter(w -> w.getColor() == RED)
 *                         .mapToDouble(w -> w.getWeight())
 *                         .sum();
 * }</pre>
 *
 * 有关流、流操作、流管道和并行的其他规范，请参阅 {@link Stream} 的类文档和 <a href="package-summary.html">java.util.stream</a> 的包文档。
 *
 * @since 1.8
 * @see Stream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface DoubleStream extends BaseStream<Double, DoubleStream> {

    /**
     * 返回一个包含此流中与给定谓词匹配的元素的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param predicate 应用于每个元素以确定其是否应包含的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *                  <a href="package-summary.html#Statelessness">无状态</a> 谓词
     * @return 新的流
     */
    DoubleStream filter(DoublePredicate predicate);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *               <a href="package-summary.html#Statelessness">无状态</a> 函数
     * @return 新的流
     */
    DoubleStream map(DoubleUnaryOperator mapper);

    /**
     * 返回一个对象值的 {@code Stream}，包含将给定函数应用于此流中元素的结果。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param <U> 新流的元素类型
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *               <a href="package-summary.html#Statelessness">无状态</a> 函数
     * @return 新的流
     */
    <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code IntStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *               <a href="package-summary.html#Statelessness">无状态</a> 函数
     * @return 新的流
     */
    IntStream mapToInt(DoubleToIntFunction mapper);

    /**
     * 返回一个包含将给定函数应用于此流中元素的结果的 {@code LongStream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *               <a href="package-summary.html#Statelessness">无状态</a> 函数
     * @return 新的流
     */
    LongStream mapToLong(DoubleToLongFunction mapper);

    /**
     * 返回一个包含将给定映射函数应用于此流中每个元素后生成的映射流内容的流。每个映射流在将其内容放入此流后都会被 {@link java.util.stream.BaseStream#close() 关闭}。
     * （如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 应用于每个元素的 <a href="package-summary.html#NonInterference">非干扰</a>、
     *               <a href="package-summary.html#Statelessness">无状态</a> 函数，生成一个包含新值的 {@code DoubleStream}
     * @return 新的流
     * @see Stream#flatMap(Function)
     */
    DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper);

    /**
     * 返回一个包含此流中不同元素的流。元素根据 {@link java.lang.Double#compare(double, double)} 进行相等性比较。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @return 结果流
     */
    DoubleStream distinct();

                /**
     * 返回一个包含此流中元素的流，元素按排序顺序排列。元素根据
     * {@link java.lang.Double#compare(double, double)} 进行相等性比较。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态
     * 中间操作</a>。
     *
     * @return 结果流
     */
    DoubleStream sorted();

    /**
     * 返回一个包含此流中元素的流，同时在从结果流中消耗每个元素时执行提供的操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * <p>对于并行流管道，操作可能在任何时间、任何线程中执行，当元素由上游操作提供时。如果操作修改了共享状态，
     * 它负责提供所需的同步。
     *
     * @apiNote 此方法主要用于支持调试，您希望在管道中的某个点看到元素的流动：
     * <pre>{@code
     *     DoubleStream.of(1, 2, 3, 4)
     *         .filter(e -> e > 2)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(e -> e * e)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .sum();
     * }</pre>
     *
     * @param action 在从流中消耗元素时执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     * @return 新的流
     */
    DoubleStream peek(DoubleConsumer action);

    /**
     * 返回一个包含此流中元素的流，但长度不超过 {@code maxSize}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路
     * 有状态中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code limit()} 通常是一个廉价的操作，但在有序的并行管道中，特别是对于较大的 {@code maxSize} 值，
     * 它可能非常昂贵，因为 {@code limit(n)} 必须返回不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的前 <em>n</em> 个元素。
     * 使用无序的流源（如 {@link #generate(DoubleSupplier)}) 或通过 {@link #unordered()} 移除排序约束，可能会在并行管道中显著加速 {@code limit()}，
     * 如果您的情况允许的话。如果需要与遇到顺序保持一致，并且在并行管道中使用 {@code limit()} 时性能不佳或内存利用率不高，
     * 使用 {@link #sequential()} 切换到顺序执行可能会提高性能。
     *
     * @param maxSize 流应限制的元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code maxSize} 为负数
     */
    DoubleStream limit(long maxSize);

    /**
     * 返回一个包含此流中剩余元素的流，跳过流中的前 {@code n} 个元素。
     * 如果此流包含少于 {@code n} 个元素，则返回一个空流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态
     * 中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code skip()} 通常是一个廉价的操作，但在有序的并行管道中，特别是对于较大的 {@code n} 值，
     * 它可能非常昂贵，因为 {@code skip(n)} 必须跳过不仅仅是任意的 <em>n</em> 个元素，而是按遇到顺序的前 <em>n</em> 个元素。
     * 使用无序的流源（如 {@link #generate(DoubleSupplier)}) 或通过 {@link #unordered()} 移除排序约束，可能会在并行管道中显著加速 {@code skip()}，
     * 如果您的情况允许的话。如果需要与遇到顺序保持一致，并且在并行管道中使用 {@code skip()} 时性能不佳或内存利用率不高，
     * 使用 {@link #sequential()} 切换到顺序执行可能会提高性能。
     *
     * @param n 要跳过的前导元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code n} 为负数
     */
    DoubleStream skip(long n);

    /**
     * 对此流中的每个元素执行一个操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * <p>对于并行流管道，此操作不保证尊重流的遇到顺序，因为这样做会牺牲并行的好处。对于任何给定的元素，
     * 操作可能在任何时间、任何线程中执行。如果操作访问了共享状态，它负责提供所需的同步。
     *
     * @param action 在元素上执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     */
    void forEach(DoubleConsumer action);

    /**
     * 对此流中的每个元素执行一个操作，保证对于具有定义的遇到顺序的流，每个元素都按遇到顺序处理。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @param action 在元素上执行的<a href="package-summary.html#NonInterference">
     *               不干扰</a>操作
     * @see #forEach(DoubleConsumer)
     */
    void forEachOrdered(DoubleConsumer action);

    /**
     * 返回一个包含此流中元素的数组。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 包含此流中元素的数组
     */
    double[] toArray();

                /**
     * 对此流的元素执行<a href="package-summary.html#Reduction">归约</a>操作，使用提供的身份值和
     * <a href="package-summary.html#Associativity">结合性</a>累加函数，并返回归约值。这等同于：
     * <pre>{@code
     *     double result = identity;
     *     for (double element : this stream)
     *         result = accumulator.applyAsDouble(result, element)
     *     return result;
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>{@code identity}值必须是累加函数的身份值。这意味着对于所有{@code x}，
     * {@code accumulator.apply(identity, x)}等于{@code x}。累加函数必须是
     * <a href="package-summary.html#Associativity">结合性</a>函数。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 求和、最小值、最大值和平均值都是归约的特殊情况。求和一个数字流可以表示为：
     *
     * <pre>{@code
     *     double sum = numbers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * 或者更简洁地：
     *
     * <pre>{@code
     *     double sum = numbers.reduce(0, Double::sum);
     * }</pre>
     *
     * <p>虽然这可能看起来比在循环中简单地修改运行总计更迂回地执行聚合，
     * 但归约操作可以更优雅地并行化，无需额外的同步，并大大减少了数据竞争的风险。
     *
     * @param identity 累加函数的身份值
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
    double reduce(double identity, DoubleBinaryOperator op);

    /**
     * 对此流的元素执行<a href="package-summary.html#Reduction">归约</a>操作，使用
     * <a href="package-summary.html#Associativity">结合性</a>累加函数，并返回描述归约值（如果有）的{@code OptionalDouble}。这等同于：
     * <pre>{@code
     *     boolean foundAny = false;
     *     double result = null;
     *     for (double element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.applyAsDouble(result, element);
     *     }
     *     return foundAny ? OptionalDouble.of(result) : OptionalDouble.empty();
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
     * @see #reduce(double, DoubleBinaryOperator)
     */
    OptionalDouble reduce(DoubleBinaryOperator op);

    /**
     * 对此流的元素执行<a href="package-summary.html#MutableReduction">可变归约</a>操作。可变归约是一种结果容器为可变结果容器的操作，
     * 例如一个{@code ArrayList}，元素是通过更新结果的状态而不是替换结果来合并的。这产生的结果等同于：
     * <pre>{@code
     *     R result = supplier.get();
     *     for (double element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>像{@link #reduce(double, DoubleBinaryOperator)}一样，{@code collect}操作可以在不需额外同步的情况下并行化。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param <R> 结果的类型
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
                  ObjDoubleConsumer<R> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * 返回此流中元素的和。
     *
     * 求和是<a href="package-summary.html#Reduction">归约</a>的一个特殊情况。如果
     * 浮点求和是精确的，此方法将等同于：
     *
     * <pre>{@code
     *     return reduce(0, Double::sum);
     * }</pre>
     *
     * 但是，由于浮点求和不是精确的，上述代码不一定等同于此方法计算的求和结果。
     *
     * <p>如果任何流元素是NaN或求和过程中的任何点是NaN，则求和结果将为NaN。
     *
     * 浮点求和的值既取决于输入值，也取决于加法操作的顺序。此方法的加法操作顺序有意未定义，以允许实现灵活性，提高计算结果的速度和准确性。
     *
     * 特别地，此方法可以使用补偿求和或其他技术来减少与简单求和{@code double}值相比的误差界限。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 按绝对值递增排序的元素往往会产生更准确的结果。
     *
     * @return 此流中元素的和
     */
    double sum();


                /**
     * 返回一个描述此流中最小元素的 {@code OptionalDouble}，如果此流为空，则返回一个空的 OptionalDouble。
     * 如果任何流元素为 NaN，则最小元素将为 {@code Double.NaN}。与数值比较运算符不同，此方法认为负零严格小于正零。
     * 这是一个 <a href="package-summary.html#Reduction">约简</a> 的特殊情况，等价于：
     * <pre>{@code
     *     return reduce(Double::min);
     * }</pre>
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 包含此流中最小元素的 {@code OptionalDouble}，如果流为空，则返回一个空的可选值
     */
    OptionalDouble min();

    /**
     * 返回一个描述此流中最大元素的 {@code OptionalDouble}，如果此流为空，则返回一个空的 OptionalDouble。
     * 如果任何流元素为 NaN，则最大元素将为 {@code Double.NaN}。与数值比较运算符不同，此方法认为负零严格小于正零。
     * 这是一个 <a href="package-summary.html#Reduction">约简</a> 的特殊情况，等价于：
     * <pre>{@code
     *     return reduce(Double::max);
     * }</pre>
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 包含此流中最大元素的 {@code OptionalDouble}，如果流为空，则返回一个空的可选值
     */
    OptionalDouble max();

    /**
     * 返回此流中的元素数量。这是一个 <a href="package-summary.html#Reduction">约简</a> 的特殊情况，等价于：
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 此流中的元素数量
     */
    long count();

    /**
     * 返回一个描述此流中元素算术平均值的 {@code OptionalDouble}，如果此流为空，则返回一个空的可选值。
     *
     * 如果任何记录的值为 NaN 或者在任何点上总和为 NaN，则平均值将为 NaN。
     *
     * <p>返回的平均值可能因值记录顺序的不同而有所变化。
     *
     * 该方法可能使用补偿求和或其他技术来减少用于计算平均值的 {@link #sum 数值总和} 的误差范围。
     *
     *  <p>平均值是一个 <a href="package-summary.html#Reduction">约简</a> 的特殊情况。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 按绝对值递增排序的元素往往能产生更准确的结果。
     *
     * @return 包含此流中元素平均值的 {@code OptionalDouble}，如果流为空，则返回一个空的可选值
     */
    OptionalDouble average();

    /**
     * 返回一个描述此流中元素各种汇总数据的 {@code DoubleSummaryStatistics}。这是一个 <a href="package-summary.html#Reduction">约简</a> 的特殊情况。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 描述此流中元素各种汇总数据的 {@code DoubleSummaryStatistics}
     */
    DoubleSummaryStatistics summaryStatistics();

    /**
     * 返回此流中是否有任何元素匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回 {@code false} 并且不评估谓词。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @apiNote
     * 该方法评估谓词在流元素上的 <em>存在量化</em> (存在 x 使得 P(x))。
     *
     * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果此流中的任何元素匹配提供的谓词，则返回 {@code true}，否则返回 {@code false}
     */
    boolean anyMatch(DoublePredicate predicate);

    /**
     * 返回此流中的所有元素是否都匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回 {@code true} 并且不评估谓词。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @apiNote
     * 该方法评估谓词在流元素上的 <em>全称量化</em> (对所有 x，P(x))。如果流为空，则量化被认为是 <em>空满足</em> 并且总是 {@code true}（无论 P(x) 如何）。
     *
     * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果此流中的所有元素都匹配提供的谓词或者流为空，则返回 {@code true}，否则返回 {@code false}
     */
    boolean allMatch(DoublePredicate predicate);

    /**
     * 返回此流中是否有任何元素不匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回 {@code true} 并且不评估谓词。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @apiNote
     * 该方法评估否定谓词在流元素上的 <em>全称量化</em> (对所有 x，~P(x))。如果流为空，则量化被认为是空满足并且总是 {@code true}，无论 P(x) 如何。
     *
     * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果此流中的任何元素都不匹配提供的谓词或者流为空，则返回 {@code true}，否则返回 {@code false}
     */
    boolean noneMatch(DoublePredicate predicate);

                /**
     * 返回一个描述此流第一个元素的 {@link OptionalDouble}，如果流为空，则返回一个空的 {@code OptionalDouble}。如果
     * 流没有遇到顺序，则可以返回任何元素。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @return 一个描述此流第一个元素的 {@code OptionalDouble}，如果流为空，则返回一个空的 {@code OptionalDouble}
     */
    OptionalDouble findFirst();

    /**
     * 返回一个描述流中某个元素的 {@link OptionalDouble}，如果流为空，则返回一个空的 {@code OptionalDouble}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * <p>此操作的行为明确是非确定性的；它可以自由选择流中的任何元素。这是为了在并行操作中实现最大性能；代价是多次调用
     * 可能不会返回相同的结果。如果需要稳定的结果，请使用 {@link #findFirst()}。
     *
     * @return 一个描述流中某个元素的 {@code OptionalDouble}，如果流为空，则返回一个空的 {@code OptionalDouble}
     * @see #findFirst()
     */
    OptionalDouble findAny();

    /**
     * 返回一个由此流的元素组成，转换为 {@code Double} 类型的 {@code Stream}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @return 一个由此流的元素组成，每个元素都转换为 {@code Double} 类型的 {@code Stream}
     */
    Stream<Double> boxed();

    @Override
    DoubleStream sequential();

    @Override
    DoubleStream parallel();

    @Override
    PrimitiveIterator.OfDouble iterator();

    @Override
    Spliterator.OfDouble spliterator();


    // 静态工厂方法

    /**
     * 返回一个用于构建 {@code DoubleStream} 的构建器。
     *
     * @return 一个流构建器
     */
    public static Builder builder() {
        return new Streams.DoubleStreamBuilderImpl();
    }

    /**
     * 返回一个空的顺序 {@code DoubleStream}。
     *
     * @return 一个空的顺序流
     */
    public static DoubleStream empty() {
        return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator(), false);
    }

    /**
     * 返回一个包含单个元素的顺序 {@code DoubleStream}。
     *
     * @param t 单个元素
     * @return 一个单元素顺序流
     */
    public static DoubleStream of(double t) {
        return StreamSupport.doubleStream(new Streams.DoubleStreamBuilderImpl(t), false);
    }

    /**
     * 返回一个顺序有序流，其元素是指定的值。
     *
     * @param values 新流的元素
     * @return 新的流
     */
    public static DoubleStream of(double... values) {
        return Arrays.stream(values);
    }

    /**
     * 返回一个通过迭代应用函数 {@code f} 到初始元素 {@code seed} 生成的无限顺序有序 {@code DoubleStream}，
     * 生成的流包含 {@code seed}，{@code f(seed)}，{@code f(f(seed))} 等。
     *
     * <p>在 {@code DoubleStream} 中的第一个元素（位置 {@code 0}）将是提供的 {@code seed}。对于 {@code n > 0}，
     * 位置 {@code n} 的元素将是应用函数 {@code f} 到位置 {@code n - 1} 的元素的结果。
     *
     * @param seed 初始元素
     * @param f 一个用于将前一个元素转换为新元素的函数
     * @return 一个新的顺序 {@code DoubleStream}
     */
    public static DoubleStream iterate(final double seed, final DoubleUnaryOperator f) {
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfDouble iterator = new PrimitiveIterator.OfDouble() {
            double t = seed;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public double nextDouble() {
                double v = t;
                t = f.applyAsDouble(t);
                return v;
            }
        };
        return StreamSupport.doubleStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    /**
     * 返回一个无限顺序无序流，其中每个元素由提供的 {@code DoubleSupplier} 生成。这适用于生成常量流、随机元素流等。
     *
     * @param s 用于生成元素的 {@code DoubleSupplier}
     * @return 一个新的无限顺序无序 {@code DoubleStream}
     */
    public static DoubleStream generate(DoubleSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.doubleStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfDouble(Long.MAX_VALUE, s), false);
    }

    /**
     * 创建一个延迟连接的流，其元素是第一个流的所有元素，后跟第二个流的所有元素。如果两个输入流都是有序的，则结果流也是有序的；
     * 如果任一输入流是并行的，则结果流也是并行的。当结果流关闭时，两个输入流的关闭处理程序都会被调用。
     *
     * @implNote
     * 通过重复连接构造流时要谨慎。访问深度连接的流的元素可能导致深度调用链，甚至导致 {@code StackOverflowException}。
     *
     * @param a 第一个流
     * @param b 第二个流
     * @return 两个输入流的连接
     */
    public static DoubleStream concat(DoubleStream a, DoubleStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);


                    Spliterator.OfDouble split = new Streams.ConcatSpliterator.OfDouble(
                a.spliterator(), b.spliterator());
        DoubleStream stream = StreamSupport.doubleStream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    /**
     * 一个可变的 {@code DoubleStream} 构建器。
     *
     * <p>流构建器具有生命周期，从构建阶段开始，在此期间可以添加元素，然后转换为已构建阶段，
     * 在此之后不能再添加元素。当调用 {@link #build()} 方法时，已构建阶段开始，该方法创建一个
     * 有序流，其元素是添加到流构建器中的元素，按添加的顺序排列。
     *
     * @see DoubleStream#builder()
     * @since 1.8
     */
    public interface Builder extends DoubleConsumer {

        /**
         * 向正在构建的流中添加一个元素。
         *
         * @throws IllegalStateException 如果构建器已经转换为已构建状态
         */
        @Override
        void accept(double t);

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
         * @throws IllegalStateException 如果构建器已经转换为已构建状态
         */
        default Builder add(double t) {
            accept(t);
            return this;
        }

        /**
         * 构建流，将此构建器转换为已构建状态。
         * 如果构建器进入已构建状态后仍有进一步的操作尝试，则抛出 {@code IllegalStateException}。
         *
         * @return 构建的流
         * @throws IllegalStateException 如果构建器已经转换为已构建状态
         */
        DoubleStream build();
    }
}
