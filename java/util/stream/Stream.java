
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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

/**
 * 支持顺序和并行聚合操作的元素序列。以下示例说明了使用
 * {@link Stream} 和 {@link IntStream} 进行聚合操作：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * 在这个示例中，{@code widgets} 是一个 {@code Collection<Widget>}。我们通过 {@link Collection#stream Collection.stream()}
 * 创建一个 {@code Widget} 对象的流，过滤它以生成仅包含红色小部件的流，然后将其转换为表示每个红色小部件重量的 {@code int} 值流。
 * 然后对这个流进行求和以生成总重量。
 *
 * <p>除了 {@code Stream}（对象引用流）之外，还有原始类型的特化版本，如 {@link IntStream}、{@link LongStream} 和
 * {@link DoubleStream}，它们都被称为“流”，并符合此处描述的特性和限制。
 *
 * <p>为了执行计算，流
 * <a href="package-summary.html#StreamOps">操作</a> 被组合成一个
 * <em>流管道</em>。流管道由一个源（可能是数组、集合、生成函数、I/O 通道等）、零个或多个
 * <em>中间操作</em>（将流转换为另一个流，如 {@link Stream#filter(Predicate)}）和一个
 * <em>终端操作</em>（生成结果或副作用，如 {@link Stream#count()} 或 {@link Stream#forEach(Consumer)}）组成。
 * 流是惰性的；只有在启动终端操作时才会对源数据进行计算，源元素也只在需要时被消耗。
 *
 * <p>虽然集合和流在表面上有一些相似之处，但它们的目标不同。集合主要关注于高效管理和访问其元素。
 * 相比之下，流不提供直接访问或操作其元素的手段，而是关注于声明性地描述其源以及将在该源上进行的聚合计算操作。
 * 但是，如果提供的流操作不能提供所需的功能，可以使用 {@link #iterator()} 和 {@link #spliterator()} 操作进行受控遍历。
 *
 * <p>流管道，如上面的“widgets”示例，可以被视为对流源的
 * <em>查询</em>。除非源被显式设计为支持并发修改（如 {@link ConcurrentHashMap}），否则在查询过程中修改流源可能会导致不可预测或错误的行为。
 *
 * <p>大多数流操作接受描述用户指定行为的参数，如传递给 {@code mapToInt} 的 lambda 表达式 {@code w -> w.getWeight()}。
 * 为了保持正确的行为，这些 <em>行为参数</em>：
 * <ul>
 * <li>必须是 <a href="package-summary.html#NonInterference">非干扰的</a>
 * （它们不会修改流源）；</li>
 * <li>在大多数情况下必须是 <a href="package-summary.html#Statelessness">无状态的</a>
 * （其结果不应依赖于在执行流管道期间可能会改变的任何状态）。</li>
 * </ul>
 *
 * <p>这些参数始终是
 * <a href="../function/package-summary.html">函数接口</a> 的实例，如 {@link java.util.function.Function}，
 * 通常是 lambda 表达式或方法引用。除非另有说明，这些参数必须是
 * <em>非空的</em>。
 *
 * <p>流应仅操作一次（调用中间或终端流操作）。这排除了例如“分叉”流，其中同一源馈送两个或多个管道，或多次遍历同一流。
 * 如果流检测到其被重用，流实现可能会抛出 {@link IllegalStateException}。然而，由于某些流操作可能返回其接收者而不是新的流对象，
 * 因此可能无法在所有情况下检测到重用。
 *
 * <p>流具有 {@link #close()} 方法并实现 {@link AutoCloseable}，但几乎所有的流实例在使用后实际上不需要关闭。
 * 通常，只有源是 I/O 通道的流（如 {@link Files#lines(Path, Charset)} 返回的流）才需要关闭。大多数流由集合、数组或生成函数支持，
 * 不需要特殊资源管理。（如果流确实需要关闭，可以在 {@code try}-with-resources 语句中声明为资源。）
 *
 * <p>流管道可以按
 * <a href="package-summary.html#Parallelism">顺序或并行</a> 执行。这是流的一个属性。
 * 流是使用初始选择的顺序或并行执行创建的。（例如，{@link Collection#stream() Collection.stream()} 创建一个顺序流，
 * 而 {@link Collection#parallelStream() Collection.parallelStream()} 创建一个并行流。）可以通过
 * {@link #sequential()} 或 {@link #parallel()} 方法修改此执行模式，可以通过
 * {@link #isParallel()} 方法查询此执行模式。
 *
 * @param <T> 流元素的类型
 * @since 1.8
 * @see IntStream
 * @see LongStream
 * @see DoubleStream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface Stream<T> extends BaseStream<T, Stream<T>> {


                /**
     * 返回一个由满足给定谓词的此流元素组成的流。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，用于确定每个元素是否应包含
     * @return 新的流
     */
    Stream<T> filter(Predicate<? super T> predicate);

    /**
     * 返回一个由将给定函数应用于此流元素的结果组成的流。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param <R> 新流的元素类型
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素
     * @return 新的流
     */
    <R> Stream<R> map(Function<? super T, ? extends R> mapper);

    /**
     * 返回一个由将给定函数应用于此流元素的结果组成的 {@code IntStream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">
     *     中间操作</a>。
     *
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素
     * @return 新的流
     */
    IntStream mapToInt(ToIntFunction<? super T> mapper);

    /**
     * 返回一个由将给定函数应用于此流元素的结果组成的 {@code LongStream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素
     * @return 新的流
     */
    LongStream mapToLong(ToLongFunction<? super T> mapper);

    /**
     * 返回一个由将给定函数应用于此流元素的结果组成的 {@code DoubleStream}。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素
     * @return 新的流
     */
    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

    /**
     * 返回一个由将提供的映射函数应用于此流的每个元素，然后用每个元素的内容替换该元素的结果组成的流。每个映射流在将其内容放入此流后都会被
     * {@link java.util.stream.BaseStream#close() 关闭}。（如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @apiNote
     * {@code flatMap()} 操作的效果是将流的元素进行一对一的转换，然后将转换后的内容展平为新的流。
     *
     * <p><b>示例。</b>
     *
     * <p>如果 {@code orders} 是一个购买订单的流，每个购买订单包含一个行项目的集合，则以下代码将生成一个包含所有订单中所有行项目的流：
     * <pre>{@code
     *     orders.flatMap(order -> order.getLineItems().stream())...
     * }</pre>
     *
     * <p>如果 {@code path} 是文件的路径，则以下代码将生成一个包含该文件中的 {@code words} 的流：
     * <pre>{@code
     *     Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8);
     *     Stream<String> words = lines.flatMap(line -> Stream.of(line.split(" +")));
     * }</pre>
     * 传递给 {@code flatMap} 的 {@code mapper} 函数使用简单的正则表达式将行分割成单词数组，然后从该数组创建一个单词流。
     *
     * @param <R> 新流的元素类型
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素，生成新的值流
     * @return 新的流
     */
    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    /**
     * 返回一个由将提供的映射函数应用于此流的每个元素，然后用每个元素的内容替换该元素的结果组成的 {@code IntStream}。每个映射流在将其内容放入此流后都会被
     * {@link java.util.stream.BaseStream#close() 关闭}。（如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素，生成新的值流
     * @return 新的流
     * @see #flatMap(Function)
     */
    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    /**
     * 返回一个由将提供的映射函数应用于此流的每个元素，然后用每个元素的内容替换该元素的结果组成的 {@code LongStream}。每个映射流在将其内容放入此流后都会被
     * {@link java.util.stream.BaseStream#close() 关闭}。（如果映射流为 {@code null}，则使用空流代替。）
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个<a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素，生成新的值流
     * @return 新的流
     * @see #flatMap(Function)
     */
    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

                /**
     * 返回一个 {@code DoubleStream}，其中包含将提供的映射函数应用于此流的每个元素后生成的流的内容。
     * 每个映射流在将其内容放入此流后都会被 {@link java.util.stream.BaseStream#close() 关闭}。（如果映射流为
     * {@code null}，则使用空流代替。）
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * @param mapper 一个 <a href="package-summary.html#NonInterference">不干扰的</a>，
     *               <a href="package-summary.html#Statelessness">无状态的</a>
     *               函数，应用于每个元素，生成新的值流
     * @return 新的流
     * @see #flatMap(Function)
     */
    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    /**
     * 返回一个由此流中的不同元素（根据 {@link Object#equals(Object)}）组成的流。
     *
     * <p>对于有序流，选择不同元素的过程是稳定的（对于重复的元素，保留最先出现的元素）。对于无序流，不提供稳定性保证。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @apiNote
     * 在并行管道中为 {@code distinct()} 保持稳定性相对昂贵（需要该操作作为一个完整的屏障，
     * 伴随着大量的缓冲开销），并且稳定性通常不是必需的。使用无序的流源（如 {@link #generate(Supplier)})
     * 或使用 {@link #unordered()} 移除排序约束，可能会使并行管道中的 {@code distinct()} 执行得更高效，
     * 如果您的情况允许的话。如果需要与遇到的顺序保持一致，并且您在并行管道中使用 {@code distinct()} 时遇到性能或内存利用率低的问题，
     * 切换到顺序执行 {@link #sequential()} 可能会提高性能。
     *
     * @return 新的流
     */
    Stream<T> distinct();

    /**
     * 返回一个由此流中的元素组成的流，按照自然顺序排序。如果此流的元素不是 {@code Comparable}，
     * 当终端操作执行时，可能会抛出 {@code java.lang.ClassCastException}。
     *
     * <p>对于有序流，排序是稳定的。对于无序流，不提供稳定性保证。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @return 新的流
     */
    Stream<T> sorted();

    /**
     * 返回一个由此流中的元素组成的流，按照提供的 {@code Comparator} 排序。
     *
     * <p>对于有序流，排序是稳定的。对于无序流，不提供稳定性保证。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态的中间操作</a>。
     *
     * @param comparator 一个 <a href="package-summary.html#NonInterference">不干扰的</a>，
     *                   <a href="package-summary.html#Statelessness">无状态的</a>
     *                   {@code Comparator}，用于比较流中的元素
     * @return 新的流
     */
    Stream<T> sorted(Comparator<? super T> comparator);

    /**
     * 返回一个由此流中的元素组成的流，在从结果流中消耗元素时，还会对每个元素执行提供的操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间操作</a>。
     *
     * <p>对于并行流管道，操作可能在元素被上游操作提供时的任何时间和任何线程中被调用。如果操作修改了共享状态，
     * 它有责任提供所需的同步。
     *
     * @apiNote 此方法主要用于支持调试，您希望在管道中的某个点看到元素的流动：
     * <pre>{@code
     *     Stream.of("one", "two", "three", "four")
     *         .filter(e -> e.length() > 3)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(String::toUpperCase)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * @param action 一个 <a href="package-summary.html#NonInterference">
     *                 不干扰的</a> 操作，将在从流中消耗元素时执行
     * @return 新的流
     */
    Stream<T> peek(Consumer<? super T> action);

    /**
     * 返回一个由此流中的元素组成的流，但长度不超过 {@code maxSize}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路有状态的中间操作</a>。
     *
     * @apiNote
     * 虽然在顺序流管道中 {@code limit()} 通常是一个廉价的操作，但在有序的并行管道中，尤其是对于较大的 {@code maxSize} 值，
     * 它可能非常昂贵，因为 {@code limit(n)} 必须返回不仅仅是任意的 <em>n</em> 个元素，而是遇到顺序中的前 <em>n</em> 个元素。
     * 使用无序的流源（如 {@link #generate(Supplier)}) 或使用 {@link #unordered()} 移除排序约束，可能会使并行管道中的 {@code limit()}
     * 执行得更高效，如果您的情况允许的话。如果需要与遇到的顺序保持一致，并且您在并行管道中使用 {@code limit()} 时遇到性能或内存利用率低的问题，
     * 切换到顺序执行 {@link #sequential()} 可能会提高性能。
     *
     * @param maxSize 流应限制的最大长度
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code maxSize} 为负数
     */
    Stream<T> limit(long maxSize);

                /**
     * 返回一个流，该流由丢弃了此流前 {@code n} 个元素后的剩余元素组成。
     * 如果此流包含少于 {@code n} 个元素，则返回一个空流。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">有状态
     * 的中间操作</a>。
     *
     * @apiNote
     * 虽然 {@code skip()} 通常在顺序流管道中是一个廉价的操作，但在有序的并行管道中可能会非常昂贵，
     * 尤其是对于较大的 {@code n} 值，因为 {@code skip(n)} 必须跳过不仅仅是任意的 <em>n</em> 个元素，
     * 而是按遇到顺序的 <em>前 n</em> 个元素。使用无序的流源（如 {@link #generate(Supplier)}) 或
     * 通过 {@link #unordered()} 移除排序约束，可能会在并行管道中显著加速 {@code skip()} 的执行，
     * 如果您的情况允许的话。如果需要与遇到顺序保持一致，并且您在并行管道中使用 {@code skip()} 时遇到性能低下或内存使用问题，
     * 切换到使用 {@link #sequential()} 的顺序执行可能会提高性能。
     *
     * @param n 要跳过的前导元素数量
     * @return 新的流
     * @throws IllegalArgumentException 如果 {@code n} 为负数
     */
    Stream<T> skip(long n);

    /**
     * 对此流的每个元素执行一个操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * <p>此操作的行为明确是非确定性的。
     * 对于并行流管道，此操作不保证尊重流的遇到顺序，因为这样做会牺牲并行的好处。对于任何给定的元素，
     * 操作可能在库选择的任何时间并在任何线程中执行。如果操作访问共享状态，则负责提供所需的同步。
     *
     * @param action 要对元素执行的 <a href="package-summary.html#NonInterference">
     *               非干扰</a> 操作
     */
    void forEach(Consumer<? super T> action);

    /**
     * 按照流的遇到顺序（如果流有定义的遇到顺序）对流的每个元素执行一个操作。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * <p>此操作按遇到顺序（如果存在）逐个处理元素。对一个元素执行操作 <a href="../concurrent/package-summary.html#MemoryVisibility"><i>先于</i></a>
     * 对后续元素执行操作，但对于任何给定的元素，操作可能在库选择的任何线程中执行。
     *
     * @param action 要对元素执行的 <a href="package-summary.html#NonInterference">
     *               非干扰</a> 操作
     * @see #forEach(Consumer)
     */
    void forEachOrdered(Consumer<? super T> action);

    /**
     * 返回一个包含此流元素的数组。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 包含此流元素的数组
     */
    Object[] toArray();

    /**
     * 返回一个包含此流元素的数组，使用提供的 {@code generator} 函数来分配返回的数组，
     * 以及可能需要用于分区执行或调整大小的任何其他数组。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @apiNote
     * 生成器函数接受一个整数，即所需数组的大小，并生成一个所需大小的数组。这可以简洁地用数组构造器引用表示：
     * <pre>{@code
     *     Person[] men = people.stream()
     *                          .filter(p -> p.getGender() == MALE)
     *                          .toArray(Person[]::new);
     * }</pre>
     *
     * @param <A> 结果数组的元素类型
     * @param generator 一个生成所需类型和提供长度的新数组的函数
     * @return 包含此流元素的数组
     * @throws ArrayStoreException 如果从数组生成器返回的数组的运行时类型不是此流中每个元素的运行时类型的超类型
     */
    <A> A[] toArray(IntFunction<A[]> generator);

    /**
     * 对此流的元素执行 <a href="package-summary.html#Reduction">归约</a>，
     * 使用提供的身份值和一个 <a href="package-summary.html#Associativity">关联的</a>
     * 累加函数，并返回归约值。这等同于：
     * <pre>{@code
     *     T result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>身份值必须是累加函数的身份值。这意味着对于所有 {@code t}，
     * {@code accumulator.apply(identity, t)} 等于 {@code t}。累加函数必须是
     * <a href="package-summary.html#Associativity">关联的</a> 函数。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @apiNote 求和、最小值、最大值、平均值和字符串连接都是归约的特殊情况。求和一个数字流可以表示为：
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * 或：
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>虽然这可能看起来比在循环中简单地修改一个运行总计更绕，但归约操作可以更优雅地并行化，
     * 而不需要额外的同步，并且大大减少了数据竞争的风险。
     *
     * @param identity 累加函数的身份值
     * @param accumulator 一个 <a href="package-summary.html#Associativity">关联的</a>、
     *                    <a href="package-summary.html#NonInterference">非干扰的</a>、
     *                    <a href="package-summary.html#Statelessness">无状态的</a>
     *                    用于组合两个值的函数
     * @return 归约的结果
     */
    T reduce(T identity, BinaryOperator<T> accumulator);

                /**
     * 对此流的元素执行 <a href="package-summary.html#Reduction">归约</a> 操作，使用
     * <a href="package-summary.html#Associativity">结合性</a> 累加函数，并返回一个描述归约值的 {@code Optional}，
     * 如果有的话。这等同于：
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>{@code accumulator} 函数必须是
     * <a href="package-summary.html#Associativity">结合性</a> 函数。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param accumulator 一个 <a href="package-summary.html#Associativity">结合性</a> 的，
     *                    <a href="package-summary.html#NonInterference">非干扰性</a> 的，
     *                    <a href="package-summary.html#Statelessness">无状态</a> 的
     *                    用于组合两个值的函数
     * @return 描述归约结果的 {@link Optional}
     * @throws NullPointerException 如果归约的结果为 null
     * @see #reduce(Object, BinaryOperator)
     * @see #min(Comparator)
     * @see #max(Comparator)
     */
    Optional<T> reduce(BinaryOperator<T> accumulator);

    /**
     * 对此流的元素执行 <a href="package-summary.html#Reduction">归约</a> 操作，使用提供的标识、累加和
     * 组合函数。这等同于：
     * <pre>{@code
     *     U result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * 但不受限于顺序执行。
     *
     * <p>{@code identity} 值必须是组合函数的标识值。这意味着对于所有 {@code u}，{@code combiner(identity, u)}
     * 等于 {@code u}。此外，组合函数必须与累加函数兼容；对于所有
     * {@code u} 和 {@code t}，以下必须成立：
     * <pre>{@code
     *     combiner.apply(u, accumulator.apply(identity, t)) == accumulator.apply(u, t)
     * }</pre>
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote 许多使用这种形式的归约可以更简单地通过显式的 {@code map} 和 {@code reduce} 操作组合来表示。
     * {@code accumulator} 函数充当融合的映射器和累加器，有时比单独的映射和归约更有效，例如，当已知先前的归约值允许你避免一些计算时。
     *
     * @param <U> 结果的类型
     * @param identity 组合函数的标识值
     * @param accumulator 一个 <a href="package-summary.html#Associativity">结合性</a> 的，
     *                    <a href="package-summary.html#NonInterference">非干扰性</a> 的，
     *                    <a href="package-summary.html#Statelessness">无状态</a> 的
     *                    用于将额外元素纳入结果的函数
     * @param combiner 一个 <a href="package-summary.html#Associativity">结合性</a> 的，
     *                    <a href="package-summary.html#NonInterference">非干扰性</a> 的，
     *                    <a href="package-summary.html#Statelessness">无状态</a> 的
     *                    用于组合两个值的函数，必须与累加函数兼容
     * @return 归约的结果
     * @see #reduce(BinaryOperator)
     * @see #reduce(Object, BinaryOperator)
     */
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    /**
     * 对此流的元素执行 <a href="package-summary.html#MutableReduction">可变归约</a> 操作。可变归约是一种归约，
     * 其中归约值是一个可变的结果容器，如 {@code ArrayList}，元素是通过更新结果的状态而不是替换结果来合并的。这产生的结果等同于：
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>像 {@link #reduce(Object, BinaryOperator)} 一样，{@code collect} 操作可以在不需额外同步的情况下并行化。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @apiNote JDK 中有许多现有的类，其签名非常适合用作 {@code collect()} 的方法引用参数。
     * 例如，以下将字符串累积到一个 {@code ArrayList} 中：
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     *
     * <p>以下将字符串流连接成一个单一的字符串：
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     *
     * @param <R> 结果的类型
     * @param supplier 一个创建新结果容器的函数。对于并行执行，此函数可能被调用多次，并且每次必须返回一个新值。
     * @param accumulator 一个 <a href="package-summary.html#Associativity">结合性</a> 的，
     *                    <a href="package-summary.html#NonInterference">非干扰性</a> 的，
     *                    <a href="package-summary.html#Statelessness">无状态</a> 的
     *                    用于将额外元素纳入结果的函数
     * @param combiner 一个 <a href="package-summary.html#Associativity">结合性</a> 的，
     *                    <a href="package-summary.html#NonInterference">非干扰性</a> 的，
     *                    <a href="package-summary.html#Statelessness">无状态</a> 的
     *                    用于组合两个值的函数，必须与累加函数兼容
     * @return 归约的结果
     */
    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);


    /**
     * 对此流的元素执行一个<a href="package-summary.html#MutableReduction">可变
     * 归约</a>操作，使用一个{@code Collector}。一个{@code Collector}
     * 封装了用作
     * {@link #collect(Supplier, BiConsumer, BiConsumer)}参数的函数，允许重用
     * 收集策略和组合收集操作，如多级分组或分区。
     *
     * <p>如果流是并行的，并且{@code Collector}
     * 是{@link Collector.Characteristics#CONCURRENT 并发}的，并且
     * 流是无序的，或者收集器是
     * {@link Collector.Characteristics#UNORDERED 无序}的，
     * 那么将执行并发归约（有关并发归约的详细信息，请参见{@link Collector}）。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * <p>当并行执行时，可能会实例化、填充和合并多个中间结果，以保持可变数据结构的隔离。因此，即使使用非线程安全的数据结构（如{@code ArrayList}）并行执行，
     * 也不需要额外的同步来进行并行归约。
     *
     * @apiNote
     * 以下代码将字符串累积到一个ArrayList中：
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     *
     * <p>以下代码将按城市分类{@code Person}对象：
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
     * }</pre>
     *
     * <p>以下代码将按州和城市分类{@code Person}对象，级联两个{@code Collector}：
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupingBy(Person::getState,
     *                                                      Collectors.groupingBy(Person::getCity)));
     * }</pre>
     *
     * @param <R> 结果的类型
     * @param <A> {@code Collector}的中间累积类型
     * @param collector 描述归约的{@code Collector}
     * @return 归约的结果
     * @see #collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    <R, A> R collect(Collector<? super T, A, R> collector);

    /**
     * 根据提供的{@code Comparator}返回此流的最小元素。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @param comparator 一个<a href="package-summary.html#NonInterference">非干扰的</a>，
     *                   <a href="package-summary.html#Statelessness">无状态的</a>
     *                   {@code Comparator}，用于比较此流的元素
     * @return 一个描述此流最小元素的{@code Optional}，
     * 或者如果流为空，则返回一个空的{@code Optional}
     * @throws NullPointerException 如果最小元素为null
     */
    Optional<T> min(Comparator<? super T> comparator);

    /**
     * 根据提供的{@code Comparator}返回此流的最大元素。这是一个
     * <a href="package-summary.html#Reduction">归约</a>的特殊情况。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @param comparator 一个<a href="package-summary.html#NonInterference">非干扰的</a>，
     *                   <a href="package-summary.html#Statelessness">无状态的</a>
     *                   {@code Comparator}，用于比较此流的元素
     * @return 一个描述此流最大元素的{@code Optional}，
     * 或者如果流为空，则返回一个空的{@code Optional}
     * @throws NullPointerException 如果最大元素为null
     */
    Optional<T> max(Comparator<? super T> comparator);

    /**
     * 返回此流中的元素数量。这是一个<a href="package-summary.html#Reduction">归约</a>的特殊情况，
     * 等价于：
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">终端操作</a>。
     *
     * @return 此流中的元素数量
     */
    long count();

    /**
     * 返回此流中是否有元素匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回
     * {@code false}，并且不评估谓词。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @apiNote
     * 此方法评估谓词在流元素上的<em>存在量化</em>（存在x P(x)）。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果此流中有元素匹配提供的谓词，则返回{@code true}，否则返回{@code false}
     */
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * 返回此流中的所有元素是否都匹配提供的谓词。如果确定结果不需要评估所有元素，则可能不会对所有元素评估谓词。如果流为空，则返回
     * {@code true}，并且不评估谓词。
     *
     * <p>这是一个<a href="package-summary.html#StreamOps">短路终端操作</a>。
     *
     * @apiNote
     * 此方法评估谓词在流元素上的<em>全称量化</em>（对所有x P(x)）。如果流为空，则量化被认为是<em>空满足的</em>，并且总是{@code true}（无论P(x)如何）。
     *
     * @param predicate 一个<a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果此流中的所有元素都匹配提供的谓词，或者流为空，则返回{@code true}，否则返回{@code false}
     */
    boolean allMatch(Predicate<? super T> predicate);


                /**
     * 返回此流中是否没有元素匹配所提供的谓词。
     * 如果不需要确定结果，则可能不会对所有元素进行谓词评估。  如果流为空，则返回 {@code true}
     * 并且不会评估谓词。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路
     * 终端操作</a>。
     *
     * @apiNote
     * 此方法评估流中元素的 <em>全称量化</em> 的否定谓词（对于所有 x ~P(x)）。  如果
     * 流为空，则量化被认为是空满足的，并且总是 {@code true}，无论 P(x) 如何。
     *
     * @param predicate 一个 <a href="package-summary.html#NonInterference">非干扰的</a>，
     *                  <a href="package-summary.html#Statelessness">无状态的</a>
     *                  谓词，应用于此流的元素
     * @return 如果流中没有元素匹配所提供的谓词或流为空，则返回 {@code true}，否则返回 {@code false}
     */
    boolean noneMatch(Predicate<? super T> predicate);

    /**
     * 返回一个描述此流第一个元素的 {@link Optional}，如果流为空，则返回一个空的 {@code Optional}。  如果
     * 流没有遇到顺序，则可以返回任何元素。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路
     * 终端操作</a>。
     *
     * @return 一个描述此流第一个元素的 {@code Optional}，如果流为空，则返回一个空的 {@code Optional}
     * @throws NullPointerException 如果选择的元素为 null
     */
    Optional<T> findFirst();

    /**
     * 返回一个描述流中某个元素的 {@link Optional}，如果流为空，则返回一个空的 {@code Optional}。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">短路
     * 终端操作</a>。
     *
     * <p>此操作的行为明确是非确定性的；它可以自由选择流中的任何元素。  这是为了在并行操作中实现最大性能；代价是
     * 多次调用同一源可能不会返回相同的结果。  （如果需要稳定的结果，请使用 {@link #findFirst()}。）
     *
     * @return 一个描述流中某个元素的 {@code Optional}，如果流为空，则返回一个空的 {@code Optional}
     * @throws NullPointerException 如果选择的元素为 null
     * @see #findFirst()
     */
    Optional<T> findAny();

    // 静态工厂

    /**
     * 返回一个用于 {@code Stream} 的构建器。
     *
     * @param <T> 元素类型
     * @return 一个流构建器
     */
    public static<T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    /**
     * 返回一个空的顺序 {@code Stream}。
     *
     * @param <T> 流元素的类型
     * @return 一个空的顺序流
     */
    public static<T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator(), false);
    }

    /**
     * 返回一个包含单个元素的顺序 {@code Stream}。
     *
     * @param t 单个元素
     * @param <T> 流元素的类型
     * @return 一个单元素顺序流
     */
    public static<T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * 返回一个顺序有序流，其元素是指定的值。
     *
     * @param <T> 流元素的类型
     * @param values 新流的元素
     * @return 新的流
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // 从数组创建流是安全的
    public static<T> Stream<T> of(T... values) {
        return Arrays.stream(values);
    }

    /**
     * 返回一个通过迭代应用函数 {@code f} 到初始元素 {@code seed} 生成的无限顺序有序 {@code Stream}，
     * 生成的 {@code Stream} 包含 {@code seed}，{@code f(seed)}，
     * {@code f(f(seed))} 等。
     *
     * <p>流中的第一个元素（位置 {@code 0}）将是提供的 {@code seed}。  对于 {@code n > 0}，位置
     * {@code n} 处的元素将是将函数 {@code f} 应用于位置 {@code n - 1} 处的元素的结果。
     *
     * @param <T> 流元素的类型
     * @param seed 初始元素
     * @param f 一个函数，应用于前一个元素以生成新元素
     * @return 一个新的顺序 {@code Stream}
     */
    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        final Iterator<T> iterator = new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) Streams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return t = (t == Streams.NONE) ? seed : f.apply(t);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    /**
     * 返回一个无限顺序无序流，其中每个元素由提供的 {@code Supplier} 生成。  这适用于生成常量流、随机元素流等。
     *
     * @param <T> 流元素的类型
     * @param s 生成元素的 {@code Supplier}
     * @return 一个新的无限顺序无序 {@code Stream}
     */
    public static<T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }


                /**
     * 创建一个惰性连接的流，其元素是第一个流的所有元素，后跟第二个流的所有元素。如果两个输入流都是有序的，则结果流也是有序的；
     * 如果任一输入流是并行的，则结果流也是并行的。当结果流关闭时，将调用两个输入流的关闭处理程序。
     *
     * @implNote
     * 通过重复连接来构建流时需谨慎。访问深度连接的流中的元素可能导致深层调用链，甚至导致 {@code StackOverflowException}。
     *
     * @param <T> 流元素的类型
     * @param a 第一个流
     * @param b 第二个流
     * @return 两个输入流的连接
     */
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    /**
     * 一个可变的 {@code Stream} 构建器。这允许通过单独生成元素并将其添加到构建器中来创建 {@code Stream}（避免了使用
     * {@code ArrayList} 作为临时缓冲区时带来的复制开销。）
     *
     * <p>流构建器有一个生命周期，从构建阶段开始，在此期间可以添加元素，然后转换为已构建阶段，此后不能再添加元素。
     * 当调用 {@link #build()} 方法时，已构建阶段开始，该方法创建一个有序的 {@code Stream}，其元素是添加到流构建器中的元素，
     * 顺序与添加时相同。
     *
     * @param <T> 流元素的类型
     * @see Stream#builder()
     * @since 1.8
     */
    public interface Builder<T> extends Consumer<T> {

        /**
         * 向正在构建的流中添加一个元素。
         *
         * @throws IllegalStateException 如果构建器已经转换为已构建状态
         */
        @Override
        void accept(T t);

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
        default Builder<T> add(T t) {
            accept(t);
            return this;
        }

        /**
         * 构建流，将此构建器转换为已构建状态。如果构建器进入已构建状态后还有进一步的操作尝试，则抛出 {@code IllegalStateException}。
         *
         * @return 构建的流
         * @throws IllegalStateException 如果构建器已经转换为已构建状态
         */
        Stream<T> build();

    }
}
