
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

/**
 * 用于支持对元素流进行函数式操作的类，例如集合上的 map-reduce 转换。例如：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(b -> b.getColor() == RED)
 *                      .mapToInt(b -> b.getWeight())
 *                      .sum();
 * }</pre>
 *
 * <p>这里我们使用 {@code widgets}，一个 {@code Collection<Widget>}，
 * 作为流的源，然后对流执行过滤-映射-归约操作，以获得红色小部件的重量总和。（求和是一个
 * <a href="package-summary.html#Reduction">归约</a> 操作的例子。）
 *
 * <p>本包中引入的关键抽象是 <em>流</em>。类 {@link java.util.stream.Stream}，
 * {@link java.util.stream.IntStream}，{@link java.util.stream.LongStream}，
 * 和 {@link java.util.stream.DoubleStream} 是对象和基本类型 {@code int}，{@code long}
 * 和 {@code double} 的流。流与集合在几个方面不同：
 *
 * <ul>
 *     <li>无存储。流不是一个存储元素的数据结构；相反，它从数据结构、数组、生成函数或 I/O 通道等源
 *     通过计算操作管道传递元素。</li>
 *     <li>具有函数性。对流的操作产生一个结果，但不会修改其源。例如，对从集合获得的流进行过滤
 *     会产生一个新的不包含被过滤元素的流，而不是从源集合中删除元素。</li>
 *     <li>寻求懒惰性。许多流操作，如过滤、映射或去重，可以懒惰地实现，从而暴露出优化的机会。
 *     例如，“找到第一个包含三个连续元音的字符串”不需要检查所有输入字符串。流操作分为
 *     中间（产生流的）操作和终端（产生值或副作用的）操作。中间操作总是懒惰的。</li>
 *     <li>可能无界。虽然集合有有限的大小，但流不必如此。短路操作如 {@code limit(n)} 或
 *     {@code findFirst()} 可以使对无限流的计算在有限时间内完成。</li>
 *     <li>可消费。流中的元素在其生命周期中只能被访问一次。像 {@link java.util.Iterator} 一样，
 *     必须生成新的流才能重新访问源的相同元素。</li>
 * </ul>
 *
 * 可以通过多种方式获得流。一些示例如下：
 * <ul>
 *     <li>通过 {@link java.util.Collection} 的 {@code stream()} 和
 *     {@code parallelStream()} 方法；</li>
 *     <li>通过 {@link java.util.Arrays#stream(Object[])} 从数组；</li>
 *     <li>通过流类的静态工厂方法，如 {@link java.util.stream.Stream#of(Object[])}，
 *     {@link java.util.stream.IntStream#range(int, int)} 或
 *     {@link java.util.stream.Stream#iterate(Object, UnaryOperator)}；</li>
 *     <li>通过 {@link java.io.BufferedReader#lines()} 从文件的行；</li>
 *     <li>通过 {@link java.nio.file.Files} 中的方法从文件路径流；</li>
 *     <li>通过 {@link java.util.Random#ints()} 从随机数流；</li>
 *     <li>JDK 中的许多其他流承载方法，包括 {@link java.util.BitSet#stream()}，
 *     {@link java.util.regex.Pattern#splitAsStream(java.lang.CharSequence)}，
 *     和 {@link java.util.jar.JarFile#stream()}。</li>
 * </ul>
 *
 * <p>第三方库可以使用 <a href="package-summary.html#StreamSources">这些技术</a>
 * 提供额外的流源。
 *
 * <h2><a name="StreamOps">流操作和管道</a></h2>
 *
 * <p>流操作分为 <em>中间</em> 和 <em>终端</em> 操作，并组合形成 <em>流
 * 管道</em>。流管道由源（如 {@code Collection}、数组、生成函数或 I/O 通道）；
 * 零个或多个中间操作，如 {@code Stream.filter} 或 {@code Stream.map}；和一个终端操作，如
 * {@code Stream.forEach} 或 {@code Stream.reduce} 组成。
 *
 * <p>中间操作返回一个新的流。它们总是 <em>懒惰的</em>；执行中间操作如
 * {@code filter()} 实际上不会执行任何过滤，而是创建一个新的流，当遍历时，该流包含初始流中
 * 符合给定谓词的元素。遍历管道源不会在终端操作执行之前开始。
 *
 * <p>终端操作，如 {@code Stream.forEach} 或 {@code IntStream.sum}，可能遍历流以产生结果或副作用。
 * 终端操作执行后，流管道被视为已消费，不能再使用；如果需要重新遍历相同的数据源，
 * 必须返回数据源以获取新的流。几乎所有终端操作都是 <em>急切的</em>，在遍历数据源和处理管道之前完成。
 * 只有终端操作 {@code iterator()} 和 {@code spliterator()} 不是；这些操作作为“逃生舱”提供，
 * 以启用任意客户端控制的管道遍历，以防现有操作不足以完成任务。
 *
 * <p>懒惰地处理流可以显著提高效率；在上述过滤-映射-求和示例中，过滤、映射和求和可以融合成一次遍历数据，
 * 且中间状态最少。懒惰性也允许在必要时避免检查所有数据；对于“找到第一个长度超过 1000 个字符的字符串”操作，
 * 只需要检查足够多的字符串，找到一个具有所需特征的字符串即可，而不需要检查所有可用的字符串。（当输入流是无限的时，
 * 这种行为变得更加重要。）
 *
 * <p>中间操作进一步分为 <em>无状态</em> 和 <em>有状态</em> 操作。无状态操作，如 {@code filter}
 * 和 {@code map}，在处理新元素时不会保留之前看到的元素的状态——每个元素可以独立处理。有状态操作，如
 * {@code distinct} 和 {@code sorted}，在处理新元素时可能需要结合之前看到的元素的状态。
 *
 * <p>有状态操作可能需要处理完所有输入后才能产生结果。例如，不能在看到流中所有元素之前对流进行排序。
 * 因此，在并行计算中，包含有状态中间操作的某些管道可能需要多次遍历数据或需要缓冲大量数据。
 * 只包含无状态中间操作的管道可以在单次遍历中处理，无论是顺序还是并行，且数据缓冲最少。
 *
 * <p>此外，某些操作被视为 <em>短路操作</em>。中间操作是短路操作，如果在面对无限输入时，它可能产生有限的流作为结果。
 * 终端操作是短路操作，如果在面对无限输入时，它可能在有限时间内终止。在管道中包含短路操作是处理无限流在有限时间内正常终止的必要条件，
 * 但不是充分条件。
 *
 * <h3>并行性</h3>
 *
 * <p>使用显式 {@code for-} 循环处理元素本质上是串行的。流通过将计算重构为聚合操作管道，而不是对每个单独元素的命令式操作，
 * 促进了并行执行。所有流操作都可以顺序或并行执行。JDK 中的流实现默认创建串行流，除非显式请求并行性。
 * 例如，{@code Collection} 有方法 {@link java.util.Collection#stream} 和 {@link java.util.Collection#parallelStream}，
 * 分别生成顺序和并行流；其他流承载方法如 {@link java.util.stream.IntStream#range(int, int)}
 * 生成顺序流，但这些流可以通过调用其 {@link java.util.stream.BaseStream#parallel()} 方法高效地并行化。
 * 要并行执行上述“小部件重量总和”查询，我们将这样做：
 *
 * <pre>{@code
 *     int sumOfWeights = widgets.}<code><b>parallelStream()</b></code>{@code
 *                               .filter(b -> b.getColor() == RED)
 *                               .mapToInt(b -> b.getWeight())
 *                               .sum();
 * }</pre>
 *
 * <p>这个例子中，串行和并行版本的唯一区别是初始流的创建，使用 “{@code parallelStream()}” 而不是 “{@code stream()}”。
 * 当终端操作被调用时，流管道将根据调用它的流的方向顺序或并行执行。是否以顺序或并行方式执行流可以通过
 * {@code isParallel()} 方法确定，流的方向可以通过
 * {@link java.util.stream.BaseStream#sequential()} 和
 * {@link java.util.stream.BaseStream#parallel()} 操作修改。当终端操作被调用时，流管道将根据调用它的流的模式顺序或并行执行。
 *
 * <p>除非操作被明确标识为非确定性的，如 {@code findAny()}，否则流是顺序执行还是并行执行不应改变计算结果。
 *
 * <p>大多数流操作接受描述用户指定行为的参数，这些参数通常是 lambda 表达式。为了保持正确的行为，
 * 这些 <em>行为参数</em> 必须是 <em>非干扰的</em>，并且在大多数情况下必须是 <em>无状态的</em>。
 * 这样的参数始终是 <a href="../function/package-summary.html">函数接口</a> 的实例，如
 * {@link java.util.function.Function}，并且通常是 lambda 表达式或方法引用。
 *
 * <h3><a name="NonInterference">非干扰</a></h3>
 *
 * 流允许你对各种数据源（包括非线程安全的集合，如 {@code ArrayList}）执行可能并行的聚合操作。这只有在可以防止
 * <em>干扰</em> 数据源的情况下才可能。除了逃逸舱操作 {@code iterator()} 和 {@code spliterator()} 外，
 * 执行从终端操作开始，到终端操作完成结束。对于大多数数据源，防止干扰意味着确保在执行流管道期间
 * <em>不修改数据源</em>。唯一例外是并发集合，它们专门设计用于处理并发修改。
 * 并发流源是那些其 {@code Spliterator} 报告 {@code CONCURRENT} 特性的流源。
 *
 * <p>相应地，如果流源可能不是并发的，流管道中的行为参数不应修改流的数据源。
 * 如果行为参数 <em>干扰</em> 非并发数据源，即修改或导致修改流的数据源。无论管道是否并行，
 * 干扰的需求都适用。除非流源是并发的，否则在执行流管道期间修改流的数据源可能会导致异常、错误结果或不符合规范的行为。
 *
 * 对于行为良好的流源，可以在终端操作开始前修改源，这些修改将反映在覆盖的元素中。例如，考虑以下代码：
 *
 * <pre>{@code
 *     List<String> l = new ArrayList(Arrays.asList("one", "two"));
 *     Stream<String> sl = l.stream();
 *     l.add("three");
 *     String s = sl.collect(joining(" "));
 * }</pre>
 *
 * 首先创建一个包含两个字符串的列表：“one” 和 “two”。然后从该列表创建一个流。接下来，通过添加第三个字符串：“three” 修改列表。
 * 最后，将流中的元素收集并连接在一起。由于在终端 {@code collect} 操作开始前修改了列表，结果将是一个字符串 “one two three”。
 * JDK 集合返回的所有流，以及大多数其他 JDK 类，都是以这种方式行为良好的；对于由其他库生成的流，请参阅
 * <a href="package-summary.html#StreamSources">低级流构造</a> 以了解构建行为良好流的要求。
 *
 * <h3><a name="Statelessness">无状态行为</a></h3>
 *
 * 如果流操作的行为参数是 <em>有状态的</em>，流管道的结果可能是非确定性的或不正确的。有状态的 lambda
 * （或其他实现适当函数接口的对象）是其结果依赖于在执行流管道期间可能发生变化的状态。例如，以下代码中的
 * {@code map()} 的参数是一个有状态的 lambda：
 *
 * <pre>{@code
 *     Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());
 *     stream.parallel().map(e -> { if (seen.add(e)) return 0; else return e; })...
 * }</pre>
 *
 * 如果在并行执行映射操作，由于线程调度的不同，对于相同的输入，结果可能会在每次运行时不同，而使用无状态的 lambda 表达式时，结果总是相同的。
 *
 * <p>此外，尝试访问行为参数中的可变状态会使你在安全性和性能方面面临两难选择；如果你不对该状态的访问进行同步，你将面临数据竞争，因此你的代码是错误的；
 * 但如果你对状态的访问进行同步，你可能会因为争用而削弱你试图从并行性中受益。最好的方法是完全避免在流操作中使用有状态的行为参数；
 * 通常有方法可以重构流管道以避免状态性。
 *
 * <h3>副作用</h3>
 *
 * 流操作的行为参数中的副作用，通常不鼓励，因为它们可能导致无意中违反无状态性要求，以及其他线程安全风险。
 *
 * <p>如果行为参数确实有副作用，除非明确说明，否则对这些副作用的
 * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>可见性</i></a>
 * 对其他线程没有保证，也没有保证同一元素在相同流管道中的不同操作将在同一线程中执行。此外，这些效果的顺序可能会令人惊讶。
 * 即使管道被约束产生一个与流源的遇到顺序一致的 <em>结果</em>（例如，
 * {@code IntStream.range(0,5).parallel().map(x -> x*2).toArray()} 必须产生 {@code [0, 2, 4, 6, 8]}），
 * 也没有保证映射函数对单个元素的执行顺序，或对给定元素的行为参数在哪个线程中执行。
 *
 * <p>许多情况下，你可能会被诱惑使用副作用的计算，可以更安全、更高效地表达为无副作用的计算，例如使用
 * <a href="package-summary.html#Reduction">归约</a> 而不是可变累加器。然而，使用 {@code println()} 进行调试的副作用通常是无害的。
 * 一小部分流操作，如 {@code forEach()} 和 {@code peek()}，只能通过副作用操作；这些操作应谨慎使用。
 *
 * <p>作为如何将不适当使用副作用的流管道转换为不使用副作用的流管道的示例，以下代码搜索字符串流中与给定正则表达式匹配的字符串，并将匹配项放入列表中。
 *
 * <pre>{@code
 *     ArrayList<String> results = new ArrayList<>();
 *     stream.filter(s -> pattern.matcher(s).matches())
 *           .forEach(s -> results.add(s));  // 不必要的副作用！
 * }</pre>
 *
 * 这段代码不必要地使用了副作用。如果并行执行，由于 {@code ArrayList} 的非线程安全性，会导致错误结果，而添加必要的同步会导致争用，削弱并行性的优势。
 * 此外，这里使用副作用是完全不必要的；可以将 {@code forEach()} 替换为更安全、更高效且更有利于并行化的归约操作：
 *
 * <pre>{@code
 *     List<String> results =
 *         stream.filter(s -> pattern.matcher(s).matches())
 *               .collect(Collectors.toList());  // 无副作用！
 * }</pre>
 *
 * <h3><a name="Ordering">顺序</a></h3>
 *
 * <p>流可能有或没有定义的 <em>遇到顺序</em>。流是否有遇到顺序取决于源和中间操作。某些流源（如 {@code List} 或数组）
 * 本质上是有序的，而其他（如 {@code HashSet}）则不是。某些中间操作，如 {@code sorted()}，可能对无序流强加遇到顺序，
 * 而其他操作，如 {@link java.util.stream.BaseStream#unordered()}，可能将有序流变为无序。此外，某些终端操作可能忽略遇到顺序，
 * 如 {@code forEach()}。
 *
 * <p>如果流是有序的，大多数操作受遇到顺序的约束；如果源是一个包含 {@code [1, 2, 3]} 的 {@code List}，
 * 则执行 {@code map(x -> x*2)} 的结果必须是 {@code [2, 4, 6]}。然而，如果源没有定义的遇到顺序，
 * 则 {@code [2, 4, 6]} 的任何排列都是有效的结果。
 *
 * <p>对于顺序流，存在或不存在遇到顺序不会影响性能，只影响确定性。如果流是有序的，对相同源重复执行相同的流管道将产生相同的结果；
 * 如果流是无序的，重复执行可能会产生不同的结果。
 *
 * <p>对于并行流，放松顺序约束有时可以提高执行效率。某些聚合操作，如过滤重复项（{@code distinct()}）或分组归约
 * （{@code Collectors.groupingBy()}）如果元素的顺序不重要，可以更高效地实现。类似地，与遇到顺序紧密相关的操作，如
 * {@code limit()}，可能需要缓冲以确保正确的顺序，从而削弱并行性的优势。在流有遇到顺序，但用户不特别
 * <em>关心</em> 该遇到顺序的情况下，使用 {@link java.util.stream.BaseStream#unordered() unordered()} 显式地去顺序化
 * 可能会提高某些有状态或终端操作的并行性能。然而，大多数流管道，如上述“小部件重量总和”示例，即使在顺序约束下仍然可以高效并行化。
 *
 * <h2><a name="Reduction">归约操作</a></h2>
 *
 * <em>归约</em> 操作（也称为 <em>折叠</em>）接受一系列输入元素，并通过重复应用组合操作将其组合成一个单一的摘要结果，例如求一组数字的和或最大值，
 * 或将元素累积到列表中。流类有多种形式的通用归约操作，称为
 * {@link java.util.stream.Stream#reduce(java.util.function.BinaryOperator) reduce()}
 * 和 {@link java.util.stream.Stream#collect(java.util.stream.Collector) collect()}，
 * 以及多种专用归约形式，如 {@link java.util.stream.IntStream#sum() sum()}、
 * {@link java.util.stream.IntStream#max() max()} 或 {@link java.util.stream.IntStream#count() count()}。
 *
 * <p>当然，这样的操作可以很容易地用简单的顺序循环实现，例如：
 * <pre>{@code
 *    int sum = 0;
 *    for (int x : numbers) {
 *       sum += x;
 *    }
 * }</pre>
 * 然而，归约操作不仅“更抽象”——它操作整个流而不是单个元素——而且如果用于处理元素的函数是
 * <a href="package-summary.html#Associativity">结合的</a> 和
 * <a href="package-summary.html#NonInterfering">无状态的</a>，归约操作本质上是可以并行化的。
 * 例如，给定一个数字流，我们想求其和，可以这样写：
 * <pre>{@code
 *    int sum = numbers.stream().reduce(0, (x,y) -> x+y);
 * }</pre>
 * 或者：
 * <pre>{@code
 *    int sum = numbers.stream().reduce(0, Integer::sum);
 * }</pre>
 *
 * <p>这些归约操作可以安全地并行执行，几乎不需要修改：
 * <pre>{@code
 *    int sum = numbers.parallelStream().reduce(0, Integer::sum);
 * }</pre>
 *
 * <p>归约操作可以并行化，因为实现可以在子集上并行操作，然后将中间结果合并以获得最终正确的答案。
 * （即使语言有一个“并行 for-each”构造，变体累加方法仍然需要开发人员提供对共享累加变量 {@code sum} 的线程安全更新，
 * 而所需的同步可能会消除并行性的任何性能优势。）使用 {@code reduce()} 代替消除了并行化归约操作的所有负担，
 * 库可以提供高效的并行实现，而无需额外的同步。
 *
 * <p>前面的“小部件”示例展示了归约如何与其他操作结合，用批量操作替换 for 循环。如果 {@code widgets}
 * 是一个包含 {@code Widget} 对象的集合，这些对象有一个 {@code getWeight} 方法，我们可以找到最重的小部件：
 * <pre>{@code
 *     OptionalInt heaviest = widgets.parallelStream()
 *                                   .mapToInt(Widget::getWeight)
 *                                   .max();
 * }</pre>
 *
 * <p>在更通用的形式中，类型为 {@code <T>} 的元素上的 {@code reduce} 操作，产生类型为 {@code <U>} 的结果，
 * 需要三个参数：
 * <pre>{@code
 * <U> U reduce(U identity,
 *              BiFunction<U, ? super T, U> accumulator,
 *              BinaryOperator<U> combiner);
 * }</pre>
 * 这里，<em>初始值</em> 元素既是归约的初始种子值，也是没有输入元素时的默认结果。<em>累加器</em> 函数接受一个部分结果和下一个元素，
 * 并生成一个新的部分结果。<em>组合器</em> 函数将两个部分结果组合成一个新的部分结果。（在并行归约中，输入被分区，
 * 对每个分区计算部分累加结果，然后将部分结果组合以产生最终结果。）
 *
 * <p>更正式地说，<em>初始值</em> 必须是 <em>组合器</em> 函数的 <em>单位元</em>。这意味着对于所有 {@code u}，
 * {@code combiner.apply(identity, u)} 等于 {@code u}。此外，<em>组合器</em> 函数必须是
 * <a href="package-summary.html#Associativity">结合的</a>，并且必须与 <em>累加器</em> 函数兼容：对于所有 {@code u}
 * 和 {@code t}，{@code combiner.apply(u, accumulator.apply(identity, t))} 必须
 * 与 {@code accumulator.apply(u, t)} 相等。
 *
 * <p>三参数形式是二参数形式的泛化，将映射步骤纳入累加步骤。我们可以将简单的求和示例重新表述为使用更通用的形式：
 * <pre>{@code
 *     int sumOfWeights = widgets.stream()
 *                               .reduce(0,
 *                                       (sum, b) -> sum + b.getWeight())
 *                                       Integer::sum);
 * }</pre>
 * 虽然可以使用显式的映射-归约形式，但通常更可读，因此通常应优先使用。泛化形式适用于可以显著优化映射和归约的组合的情况。
 *
 * <h3><a name="MutableReduction">可变归约</a></h3>
 *
 * <em>可变归约操作</em> 将输入元素累积到一个可变的结果容器中，如 {@code Collection} 或 {@code StringBuilder}，
 * 在处理元素时。
 *
 * <p>如果我们想将一个字符串流连接成一个单一的长字符串，我们 <em>可以</em> 使用普通的归约实现：
 * <pre>{@code
 *     String concatenated = strings.reduce("", String::concat)
 * }</pre>
 *
 * <p>我们将得到所需的结果，甚至可以并行工作。然而，性能可能不会令人满意！这样的实现将进行大量的字符串复制，运行时间将是
 * <em>O(n^2)</em> 的字符数。一种更高效的实现方法是使用可变的结果容器，如 {@link java.lang.StringBuilder}，
 * 我们可以使用与普通归约相同的并行化技术。
 *
 * <p>可变归约操作称为
 * {@link java.util.stream.Stream#collect(Collector) collect()}，因为它将所需的结果收集到一个结果容器中，
 * 如 {@code Collection}。一个 {@code collect} 操作需要三个函数：一个供应商函数用于构造结果容器的新实例，
 * 一个累加器函数用于将输入元素纳入结果容器，一个组合函数用于将一个结果容器的内容合并到另一个中。这种形式与普通归约的通用形式非常相似：
 * <pre>{@code
 * <R> R collect(Supplier<R> supplier,
 *               BiConsumer<R, ? super T> accumulator,
 *               BiConsumer<R, R> combiner);
 * }</pre>
 * <p>与 {@code reduce()} 一样，将 {@code collect} 表达为这种抽象形式的一个好处是它可以直接并行化：我们可以并行地累积部分结果，然后将它们合并，
 * 只要累加和组合函数满足适当的条件。例如，要将流中元素的字符串表示收集到一个 {@code ArrayList} 中，我们可以编写显式的顺序 for-each 形式：
 * <pre>{@code
 *     ArrayList<String> strings = new ArrayList<>();
 *     for (T element : stream) {
 *         strings.add(element.toString());
 *     }
 * }</pre>
 * 或者我们可以使用可并行化的 collect 形式：
 * <pre>{@code
 *     ArrayList<String> strings = stream.collect(() -> new ArrayList<>(),
 *                                                (c, e) -> c.add(e.toString()),
 *                                                (c1, c2) -> c1.addAll(c2));
 * }</pre>
 * 或者，将映射操作从累加器函数中提取出来，我们可以更简洁地表达为：
 * <pre>{@code
 *     List<String> strings = stream.map(Object::toString)
 *                                  .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
 * }</pre>
 * 这里，我们的供应商是 {@link java.util.ArrayList#ArrayList() ArrayList 构造函数}，累加器将字符串化的元素添加到 {@code ArrayList} 中，
 * 组合器使用 {@link java.util.ArrayList#addAll addAll} 将字符串从一个容器复制到另一个容器。
 *
 * <p>{@code collect} 的供应商、累加器和组合器三个方面是紧密耦合的。我们可以使用 {@link java.util.stream.Collector} 抽象来捕获所有三个方面。
 * 上述将字符串收集到 {@code List} 中的示例可以使用标准的 {@code Collector} 重写为：
 * <pre>{@code
 *     List<String> strings = stream.map(Object::toString)
 *                                  .collect(Collectors.toList());
 * }</pre>
 *
 * <p>将可变归约封装到一个 {@code Collector} 中还有一个优势：可组合性。类 {@link java.util.stream.Collectors} 包含许多预定义的收集器工厂，
 * 包括将一个收集器转换为另一个的组合器。例如，假设我们有一个收集器，用于计算员工流的工资总和，如下所示：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Integer> summingSalaries
 *         = Collectors.summingInt(Employee::getSalary);
 * }</pre>
 *
 * （第二个类型参数的 {@code ?} 表示我们不关心此收集器使用的中间表示。）
 * 如果我们想创建一个收集器来按部门汇总工资总和，我们可以使用
 * {@link java.util.stream.Collectors#groupingBy(java.util.function.Function, java.util.stream.Collector) groupingBy} 重用 {@code summingSalaries}：
 *
 * <pre>{@code
 *     Map<Department, Integer> salariesByDept
 *         = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment,
 *                                                            summingSalaries));
 * }</pre>
 *
 * <p>与常规归约操作一样，只有在满足适当条件的情况下，{@code collect()} 操作才能并行化。对于任何部分累积结果，
 * 将其与空结果容器组合必须产生等效的结果。也就是说，对于任何部分累积结果 {@code p}，该结果是通过任何系列的累加器和组合器调用获得的，
 * {@code p} 必须与 {@code combiner.apply(p, supplier.get())} 等效。
 *
 * <p>此外，无论计算如何拆分，都必须产生等效的结果。对于任何输入元素 {@code t1} 和 {@code t2}，以下计算中的
 * 结果 {@code r1} 和 {@code r2} 必须等效：
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
 * }</pre>
 *
 * <p>这里，等效通常是指根据 {@link java.lang.Object#equals(Object)}。但在某些情况下，等效可以放宽以考虑顺序的差异。
 *
 * <h3><a name="ConcurrentReduction">归约、并发和顺序</a></h3>
 *
 * 对于某些复杂的归约操作，例如产生 {@code Map} 的 {@code collect()}，如：
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .collect(Collectors.groupingBy(Transaction::getBuyer));
 * }</pre>
 * 实际上并行执行该操作可能是反productive的。这是因为合并步骤（通过键合并一个 {@code Map} 到另一个）对于某些 {@code Map} 实现来说可能非常昂贵。
 *
 * <p>然而，假设在该归约中使用的结果容器是一个并发可修改的集合——如 {@link java.util.concurrent.ConcurrentHashMap}。
 * 在这种情况下，可以并行地将结果累积到同一个共享结果容器中，从而消除了合并不同结果容器的需要。这可能对并行执行性能提供提升。我们称这种归约为
 * <em>并发</em> 归约。
 *
 * <p>支持并发归约的 {@link java.util.stream.Collector} 标记为具有
 * {@link java.util.stream.Collector.Characteristics#CONCURRENT} 特性。然而，并发集合也有其缺点。如果多个线程同时向共享容器中累积结果，
 * 结果的顺序是不确定的。因此，只有在流的顺序不重要时，才能进行并发归约。{@link java.util.stream.Stream#collect(Collector)}
 * 实现只有在以下条件满足时才会执行并发归约：
 * <ul>
 * <li>流是并行的；</li>
 * <li>收集器具有
 * {@link java.util.stream.Collector.Characteristics#CONCURRENT} 特性，且；</li>
 * <li>流是无序的，或者收集器具有
 * {@link java.util.stream.Collector.Characteristics#UNORDERED} 特性。</li>
 * </ul>
 * 您可以通过使用 {@link java.util.stream.BaseStream#unordered()} 方法确保流是无序的。例如：
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .unordered()
 *               .collect(groupingByConcurrent(Transaction::getBuyer));
 * }</pre>
 * （其中 {@link java.util.stream.Collectors#groupingByConcurrent} 是 {@code groupingBy} 的并发等效形式）。
 *
 * <p>注意，如果需要确保给定键的元素按其在源中出现的顺序出现，那么我们不能使用并发归约，因为顺序是并发插入的牺牲品。
 * 在这种情况下，我们只能实现顺序归约或基于合并的并行归约。
 *
 * <h3><a name="Associativity">结合性</a></h3>
 *
 * 操作符或函数 {@code op} 是 <em>结合的</em>，如果以下条件成立：
 * <pre>{@code
 *     (a op b) op c == a op (b op c)
 * }</pre>
 * 结合性对并行评估的重要性可以从扩展到四个项的情况中看出：
 * <pre>{@code
 *     a op b op c op d == (a op b) op (c op d)
 * }</pre>
 * 因此，我们可以并行地评估 {@code (a op b)} 和 {@code (c op d)}，然后对结果调用 {@code op}。
 *
 * <p>结合操作的例子包括数值加法、最小值、最大值和字符串连接。
 *
 * <h2><a name="StreamSources">低级流构造</a></h2>
 *
 * 到目前为止，所有流示例都使用了类似 {@link java.util.Collection#stream()} 或 {@link java.util.Arrays#stream(Object[])}
 * 的方法来获取流。这些承载流的方法是如何实现的？
 *
 * <p>类 {@link java.util.stream.StreamSupport} 有多个用于创建流的低级方法，所有这些方法都使用某种形式的
 * {@link java.util.Spliterator}。分隔器是并行的 {@link java.util.Iterator} 类比；它描述了（可能是无限的）元素集合，
 * 支持顺序推进、批量遍历和将输入的一部分拆分为另一个分隔器，该分隔器可以并行处理。在最低级别，所有流都是由分隔器驱动的。
 *
 * <p>实现分隔器有许多实现选择，几乎都是简单实现和运行时性能之间的权衡。创建分隔器的最简单但性能最差的方法是
 * 从迭代器创建分隔器，使用 {@link java.util.Spliterators#spliteratorUnknownSize(java.util.Iterator, int)}。
 * 尽管这样的分隔器可以工作，但它可能会导致较差的并行性能，因为我们丢失了大小信息（底层数据集有多大），并且受到简单的拆分算法的限制。
 *
 * <p>高质量的分隔器将提供平衡和已知大小的拆分、准确的大小信息，以及分隔器或数据的许多其他
 * {@link java.util.Spliterator#characteristics() 特性}，这些特性可以由实现用于优化执行。
 *
 * <p>可变数据源的分隔器面临额外的挑战；绑定到数据的时间；因为数据可能在分隔器创建和流管道执行之间发生变化。
 * 理想情况下，流的分隔器会报告一个特性，表示
```java


/*
    Copyright (c) 1996, 1999, ...
 */
 * {@code IMMUTABLE} 或 {@code CONCURRENT}；如果不是，则应为
 * <a href="../Spliterator.html#binding"><em>延迟绑定</em></a>。如果源无法直接提供推荐的 spliterator，
 * 它可以使用 {@code Supplier} 间接提供 spliterator，并通过接受 {@code Supplier} 的版本
 * {@link java.util.stream.StreamSupport#stream(Supplier, int, boolean) stream()} 构造流。
 * 仅在流管道的终端操作开始后，才从供应商处获取 spliterator。
 *
 * <p>这些要求显著减少了流源的修改与流管道执行之间潜在干扰的范围。基于具有所需特征的 spliterator 的流，
 * 或使用基于 Supplier 的工厂形式的流，在终端操作开始之前对数据源的修改是免疫的（前提是流操作的行为参数满足
 * 非干扰和无状态性的要求）。有关更多详细信息，请参阅
 * <a href="package-summary.html#NonInterference">非干扰</a>。
 *
 * @since 1.8
 */
package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
