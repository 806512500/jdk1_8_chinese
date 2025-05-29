
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个<a href="package-summary.html#Reduction">可变归约操作</a>，它将输入元素累积到一个可变的结果容器中，可选地在处理完所有输入元素后将累积的结果转换为最终表示。归约操作可以顺序执行或并行执行。
 *
 * <p>可变归约操作的示例包括：
 * 将元素累积到一个 {@code Collection} 中；使用 {@code StringBuilder} 连接字符串；计算关于元素的摘要信息，如总和、最小值、最大值或平均值；计算“枢轴表”摘要，如“按销售者划分的最大价值交易”等。类 {@link Collectors}
 * 提供了许多常见可变归约的实现。
 *
 * <p>一个 {@code Collector} 由四个协同工作的函数指定，这些函数将条目累积到一个可变的结果容器中，并可选地对结果执行最终转换。它们是：<ul>
 *     <li>创建新的结果容器（{@link #supplier()}）</li>
 *     <li>将新的数据元素合并到结果容器中（{@link #accumulator()}）</li>
 *     <li>将两个结果容器合并为一个（{@link #combiner()}）</li>
 *     <li>对容器执行可选的最终转换（{@link #finisher()}）</li>
 * </ul>
 *
 * <p>收集器还具有诸如 {@link Characteristics#CONCURRENT} 等特性，这些特性可以作为提示，用于归约实现以提高性能。
 *
 * <p>使用收集器的归约的顺序实现将使用供应器函数创建一个结果容器，并为每个输入元素调用一次累积器函数。并行实现将对输入进行分区，为每个分区创建一个结果容器，将每个分区的内容累积到该分区的子结果中，然后使用组合器函数将子结果合并为一个组合结果。
 *
 * <p>为了确保顺序和并行执行产生等效的结果，收集器函数必须满足一个<em>恒等性</em>和一个<a href="package-summary.html#Associativity">结合性</a>约束。
 *
 * <p>恒等性约束指出，对于任何部分累积的结果，将其与一个空的结果容器组合必须产生等效的结果。也就是说，对于通过任何一系列累积器和组合器调用产生的部分累积结果 {@code a}，{@code a} 必须等同于 {@code combiner.apply(a, supplier.get())}。
 *
 * <p>结合性约束指出，将计算拆分必须产生等效的结果。也就是说，对于任何输入元素 {@code t1} 和 {@code t2}，下面计算中的结果 {@code r1} 和 {@code r2} 必须等效：
 * <pre>{@code
 *     A a1 = supplier.get();
 *     accumulator.accept(a1, t1);
 *     accumulator.accept(a1, t2);
 *     R r1 = finisher.apply(a1);  // 未拆分的结果
 *
 *     A a2 = supplier.get();
 *     accumulator.accept(a2, t1);
 *     A a3 = supplier.get();
 *     accumulator.accept(a3, t2);
 *     R r2 = finisher.apply(combiner.apply(a2, a3));  // 拆分后的结果
 * } </pre>
 *
 * <p>对于没有 {@code UNORDERED} 特性的收集器，两个累积结果 {@code a1} 和 {@code a2} 如果满足 {@code finisher.apply(a1).equals(finisher.apply(a2))}，则认为是等效的。对于无序收集器，等效性放宽到允许与顺序差异相关的不等性。（例如，一个将元素累积到 {@code List} 中的无序收集器会认为两个列表等效，只要它们包含相同的元素，忽略顺序。）
 *
 * <p>基于 {@code Collector} 实现归约的库，如 {@link Stream#collect(Collector)}，必须遵守以下约束：
 * <ul>
 *     <li>传递给累积器函数的第一个参数，传递给组合器函数的两个参数，以及传递给完成器函数的参数必须是先前调用结果供应器、累积器或组合器函数的结果。</li>
 *     <li>实现不应对结果供应器、累积器或组合器函数的任何结果做任何处理，除了将它们再次传递给累积器、组合器或完成器函数，或将它们返回给归约操作的调用者。</li>
 *     <li>如果一个结果传递给组合器或完成器函数，并且该函数没有返回相同的对象，则该对象不再使用。</li>
 *     <li>一旦一个结果传递给组合器或完成器函数，它将不再传递给累积器函数。</li>
 *     <li>对于非并发收集器，从结果供应器、累积器或组合器函数返回的任何结果必须是串行线程隔离的。这使得可以在并行收集时无需 {@code Collector} 实现任何额外的同步。归约实现必须管理输入的正确分区，分区的独立处理，以及在累积完成后的组合。</li>
 *     <li>对于并发收集器，实现可以（但不是必须）并行实现归约。并发归约是指累积器函数从多个线程并发调用，使用同一个可并发修改的结果容器，而不是在累积期间将结果隔离。只有当收集器具有 {@link Characteristics#UNORDERED} 特性或源数据是无序的时，才应应用并发归约。</li>
 * </ul>
 *
 * <p>除了 {@link Collectors} 中的预定义实现外，还可以使用静态工厂方法 {@link #of(Supplier, BiConsumer, BinaryOperator, Characteristics...)} 构建收集器。例如，您可以创建一个将小部件累积到 {@code TreeSet} 中的收集器：
 *
 * <pre>{@code
 *     Collector<Widget, ?, TreeSet<Widget>> intoSet =
 *         Collector.of(TreeSet::new, TreeSet::add,
 *                      (left, right) -> { left.addAll(right); return left; });
 * }</pre>
 *
 * （这种行为也由预定义的收集器 {@link Collectors#toCollection(Supplier)} 实现。）
 *
 * @apiNote
 * 使用 {@code Collector} 执行归约操作应产生等效于以下的结果：
 * <pre>{@code
 *     R container = collector.supplier().get();
 *     for (T t : data)
 *         collector.accumulator().accept(container, t);
 *     return collector.finisher().apply(container);
 * }</pre>
 *
 * <p>但是，库可以自由地对输入进行分区，对分区执行归约，然后使用组合器函数将部分结果组合起来以实现并行归约。（根据具体的归约操作，这可能会表现得更好或更差，具体取决于累积器和组合器函数的相对成本。）
 *
 * <p>收集器设计为<em>可组合的</em>；许多 {@link Collectors} 中的方法都是接受一个收集器并生成一个新收集器的函数。例如，给定以下计算员工薪资总和的收集器：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Integer> summingSalaries
 *         = Collectors.summingInt(Employee::getSalary));
 * }</pre>
 *
 * 如果我们想创建一个按部门汇总薪资总和的收集器，可以重用“薪资总和”逻辑，使用 {@link Collectors#groupingBy(Function, Collector)}：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Map<Department, Integer>> summingSalariesByDept
 *         = Collectors.groupingBy(Employee::getDepartment, summingSalaries);
 * }</pre>
 *
 * @see Stream#collect(Collector)
 * @see Collectors
 *
 * @param <T> 归约操作的输入元素类型
 * @param <A> 归约操作的可变累积类型（通常作为实现细节隐藏）
 * @param <R> 归约操作的结果类型
 * @since 1.8
 */
public interface Collector<T, A, R> {
    /**
     * 创建并返回一个新的可变结果容器的函数。
     *
     * @return 返回一个新的、可变的结果容器的函数
     */
    Supplier<A> supplier();


                /**
     * 一个将值折叠到可变结果容器中的函数。
     *
     * @return 一个将值折叠到可变结果容器中的函数
     */
    BiConsumer<A, T> accumulator();

    /**
     * 一个接受两个部分结果并合并它们的函数。合并函数可以将一个参数的状态折叠到另一个参数中并返回，或者返回一个新的结果容器。
     *
     * @return 一个将两个部分结果合并为一个组合结果的函数
     */
    BinaryOperator<A> combiner();

    /**
     * 从中间累积类型 {@code A} 到最终结果类型 {@code R} 执行最终转换。
     *
     * <p>如果设置了特性 {@code IDENTITY_TRANSFORM}，则可以假定此函数是一个从 {@code A} 到 {@code R} 的身份转换。
     *
     * @return 一个将中间结果转换为最终结果的函数
     */
    Function<A, R> finisher();

    /**
     * 返回一个表示此收集器特性的 {@code Collector.Characteristics} 集合。此集合应该是不可变的。
     *
     * @return 一个不可变的收集器特性集合
     */
    Set<Characteristics> characteristics();

    /**
     * 返回由给定的 {@code supplier}、{@code accumulator} 和 {@code combiner} 函数描述的新 {@code Collector}。生成的
     * {@code Collector} 具有 {@code Collector.Characteristics.IDENTITY_FINISH} 特性。
     *
     * @param supplier 新收集器的供应商函数
     * @param accumulator 新收集器的累积器函数
     * @param combiner 新收集器的组合器函数
     * @param characteristics 新收集器的收集器特性
     * @param <T> 新收集器的输入元素类型
     * @param <R> 新收集器的中间累积结果类型和最终结果类型
     * @throws NullPointerException 如果任何参数为 null
     * @return 新的 {@code Collector}
     */
    public static<T, R> Collector<T, R, R> of(Supplier<R> supplier,
                                              BiConsumer<R, T> accumulator,
                                              BinaryOperator<R> combiner,
                                              Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = (characteristics.length == 0)
                                  ? Collectors.CH_ID
                                  : Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH,
                                                                           characteristics));
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, cs);
    }

    /**
     * 返回由给定的 {@code supplier}、{@code accumulator}、{@code combiner} 和 {@code finisher} 函数描述的新 {@code Collector}。
     *
     * @param supplier 新收集器的供应商函数
     * @param accumulator 新收集器的累积器函数
     * @param combiner 新收集器的组合器函数
     * @param finisher 新收集器的完成器函数
     * @param characteristics 新收集器的收集器特性
     * @param <T> 新收集器的输入元素类型
     * @param <A> 新收集器的中间累积类型
     * @param <R> 新收集器的最终结果类型
     * @throws NullPointerException 如果任何参数为 null
     * @return 新的 {@code Collector}
     */
    public static<T, A, R> Collector<T, A, R> of(Supplier<A> supplier,
                                                 BiConsumer<A, T> accumulator,
                                                 BinaryOperator<A> combiner,
                                                 Function<A, R> finisher,
                                                 Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(finisher);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = Collectors.CH_NOID;
        if (characteristics.length > 0) {
            cs = EnumSet.noneOf(Characteristics.class);
            Collections.addAll(cs, characteristics);
            cs = Collections.unmodifiableSet(cs);
        }
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, finisher, cs);
    }

    /**
     * 表示 {@code Collector} 特性的枚举，可用于优化减少实现。
     */
    enum Characteristics {
        /**
         * 表示此收集器是 <em>并发的</em>，意味着结果容器可以支持累积器函数从多个线程并发调用同一个结果容器。
         *
         * <p>如果一个 {@code CONCURRENT} 收集器不是 {@code UNORDERED}，则只有在应用于无序数据源时才应并发评估。
         */
        CONCURRENT,

        /**
         * 表示收集操作不承诺保留输入元素的遇到顺序。（如果结果容器没有固有的顺序，例如一个 {@link Set}，则可能是这种情况。）
         */
        UNORDERED,

        /**
         * 表示完成器函数是身份函数，可以省略。如果设置此特性，则必须确保从 A 到 R 的未检查转换将成功。
         */
        IDENTITY_FINISH
    }
}
