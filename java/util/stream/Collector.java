
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个<a href="package-summary.html#Reduction">可变归约操作</a>，它将输入元素累积到一个可变的结果容器中，可选地在处理完所有输入元素后将累积的结果转换为最终表示。归约操作可以顺序或并行执行。
 *
 * <p>可变归约操作的示例包括：将元素累积到一个 {@code Collection} 中；使用 {@code StringBuilder} 连接字符串；计算元素的摘要信息，如总和、最小值、最大值或平均值；计算“转置表”摘要，如“按卖方计算的最大交易额”等。类 {@link Collectors} 提供了许多常见的可变归约实现。
 *
 * <p>{@code Collector} 由四个协同工作的函数指定，这些函数将条目累积到一个可变的结果容器中，并可选地对容器执行最终转换。它们是：<ul>
 *     <li>创建新的结果容器（{@link #supplier()}）</li>
 *     <li>将新数据元素合并到结果容器中（{@link #accumulator()}）</li>
 *     <li>将两个结果容器合并为一个（{@link #combiner()}）</li>
 *     <li>对容器执行可选的最终转换（{@link #finisher()}）</li>
 * </ul>
 *
 * <p>收集器还有一组特性，如 {@link Characteristics#CONCURRENT}，这些特性可以作为提示，用于归约实现以提高性能。
 *
 * <p>使用收集器进行归约的顺序实现将使用供应函数创建一个结果容器，并为每个输入元素调用累积函数一次。并行实现将对输入进行分区，为每个分区创建一个结果容器，将每个分区的内容累积到该分区的子结果中，然后使用组合函数将子结果合并为一个组合结果。
 *
 * <p>为了确保顺序和并行执行产生等效的结果，收集器函数必须满足一个 <em>恒等性</em> 和一个 <a href="package-summary.html#Associativity">结合性</a> 约束。
 *
 * <p>恒等性约束要求，对于任何部分累积的结果，将其与空结果容器组合必须产生等效的结果。也就是说，对于通过任何一系列累积和组合调用产生的部分累积结果 {@code a}，{@code a} 必须等同于 {@code combiner.apply(a, supplier.get())}。
 *
 * <p>结合性约束要求，拆分计算必须产生等效的结果。也就是说，对于任何输入元素 {@code t1} 和 {@code t2}，下面计算中的结果 {@code r1} 和 {@code r2} 必须等效：
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
 *     R r2 = finisher.apply(combiner.apply(a2, a3));  // 拆分的结果
 * } </pre>
 *
 * <p>对于没有 {@code UNORDERED} 特性的收集器，两个累积结果 {@code a1} 和 {@code a2} 是等效的，如果 {@code finisher.apply(a1).equals(finisher.apply(a2))}。对于无序的收集器，等效性放宽到允许与顺序差异相关的不等性。（例如，一个无序的收集器将元素累积到一个 {@code List} 中，会认为两个列表等效，只要它们包含相同的元素，忽略顺序。）
 *
 * <p>基于 {@code Collector} 实现归约的库，如 {@link Stream#collect(Collector)}，必须遵守以下约束：
 * <ul>
 *     <li>传递给累积函数的第一个参数，传递给组合函数的两个参数，以及传递给最终转换函数的参数，必须是先前调用结果供应函数、累积函数或组合函数的结果。</li>
 *     <li>实现不应做任何与结果供应函数、累积函数或组合函数的结果无关的事情，除了将它们再次传递给累积函数、组合函数或最终转换函数，或将它们返回给归约操作的调用者。</li>
 *     <li>如果结果传递给组合或最终转换函数，并且从该函数返回的不是同一个对象，则该对象不再使用。</li>
 *     <li>一旦结果传递给组合或最终转换函数，它不再传递给累积函数。</li>
 *     <li>对于非并发收集器，从结果供应函数、累积函数或组合函数返回的任何结果必须是串行线程隔离的。这使得可以在并行收集时无需 {@code Collector} 实现任何额外的同步。归约实现必须管理输入的正确分区，分区的独立处理，以及在累积完成后进行组合。</li>
 *     <li>对于并发收集器，实现可以（但不是必须）并行实现归约。并发归约是指累积函数从多个线程并发调用，使用同一个可并发修改的结果容器，而不是在累积期间将结果隔离。只有当收集器具有 {@link Characteristics#UNORDERED} 特性或源数据无序时，才能应用并发归约。</li>
 * </ul>
 *
 * <p>除了 {@link Collectors} 中的预定义实现外，静态工厂方法 {@link #of(Supplier, BiConsumer, BinaryOperator, Characteristics...)} 可用于构建收集器。例如，您可以创建一个将小部件累积到 {@code TreeSet} 中的收集器：
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
 * 使用 {@code Collector} 进行归约操作应产生等效于以下结果：
 * <pre>{@code
 *     R container = collector.supplier().get();
 *     for (T t : data)
 *         collector.accumulator().accept(container, t);
 *     return collector.finisher().apply(container);
 * }</pre>
 *
 * <p>但是，库可以自由地对输入进行分区，对分区执行归约，然后使用组合函数将部分结果合并，以实现并行归约。（根据具体的归约操作，这可能会表现得更好或更差，具体取决于累积函数和组合函数的相对成本。）
 *
 * <p>收集器设计为 <em>可组合的</em>；许多 {@link Collectors} 中的方法都是函数，它们接受一个收集器并生成一个新的收集器。例如，给定以下计算员工薪资总和的收集器：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Integer> summingSalaries
 *         = Collectors.summingInt(Employee::getSalary))
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
     * 创建并返回新的可变结果容器的函数。
     *
     * @return 返回新的、可变结果容器的函数
     */
    Supplier<A> supplier();

    /**
     * 将值折叠到可变结果容器中的函数。
     *
     * @return 将值折叠到可变结果容器中的函数
     */
    BiConsumer<A, T> accumulator();

    /**
     * 接受两个部分结果并将其合并的函数。组合函数可以将一个参数的状态折叠到另一个参数中并返回，也可以返回一个新的结果容器。
     *
     * @return 将两个部分结果合并为组合结果的函数
     */
    BinaryOperator<A> combiner();

    /**
     * 从中间累积类型 {@code A} 执行最终转换到最终结果类型 {@code R}。
     *
     * <p>如果特性 {@code IDENTITY_TRANSFORM} 被设置，此函数可以假定为一个带有从 {@code A} 到 {@code R} 的未检查类型转换的身份转换。
     *
     * @return 将中间结果转换为最终结果的函数
     */
    Function<A, R> finisher();

    /**
     * 返回一个包含 {@code Collector.Characteristics} 的 {@code Set}，表示此收集器的特性。此集应该是不可变的。
     *
     * @return 不可变的收集器特性集
     */
    Set<Characteristics> characteristics();

    /**
     * 返回由给定的 {@code supplier}、{@code accumulator} 和 {@code combiner} 函数描述的新 {@code Collector}。生成的 {@code Collector} 具有 {@code Collector.Characteristics.IDENTITY_FINISH} 特性。
     *
     * @param supplier 新收集器的供应函数
     * @param accumulator 新收集器的累积函数
     * @param combiner 新收集器的组合函数
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
     * @param supplier 新收集器的供应函数
     * @param accumulator 新收集器的累积函数
     * @param combiner 新收集器的组合函数
     * @param finisher 新收集器的最终转换函数
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
     * 表示 {@code Collector} 特性的特性，可以用于优化归约实现。
     */
    enum Characteristics {
        /**
         * 表示此收集器是 <em>并发的</em>，意味着结果容器可以支持累积函数从多个线程并发调用同一个结果容器。
         *
         * <p>如果一个 {@code CONCURRENT} 收集器不是 {@code UNORDERED} 的，则只有在应用于无序数据源时才应并发评估。
         */
        CONCURRENT,


        /**
         * 表示集合操作不承诺保留输入元素的遇到顺序。 （如果结果容器没有固有的顺序，例如 {@link Set}，则可能是这种情况。）
         */
        UNORDERED,

        /**
         * 表示终结函数是恒等函数，可以省略。 如果设置，则必须确保从 A 到 R 的未检查类型转换将成功。
         */
        IDENTITY_FINISH
    }
}
