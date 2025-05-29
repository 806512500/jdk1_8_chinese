
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

/**
 * 类用于支持对元素流进行函数式操作，例如集合上的 map-reduce 转换。例如：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(b -> b.getColor() == RED)
 *                      .mapToInt(b -> b.getWeight())
 *                      .sum();
 * }</pre>
 *
 * <p>在这里，我们使用 {@code widgets}，一个 {@code Collection<Widget>}，
 * 作为流的源，然后对流执行过滤-映射-归约操作，以获得红色小部件的总重量。总和是一个
 * <a href="package-summary.html#Reduction">归约</a> 操作的例子。
 *
 * <p>本包中引入的关键抽象是 <em>流</em>。类 {@link java.util.stream.Stream}，
 * {@link java.util.stream.IntStream}，{@link java.util.stream.LongStream}，和
 * {@link java.util.stream.DoubleStream} 是对象和原始 {@code int}，{@code long} 和
 * {@code double} 类型的流。流与集合在几个方面不同：
 *
 * <ul>
 *     <li>无存储。流不是一个存储元素的数据结构；相反，它从数据结构、数组、生成函数或 I/O 通道等源中传输元素，通过计算操作管道。</li>
 *     <li>函数式。对流的操作产生一个结果，但不修改其源。例如，对从集合中获得的 {@code Stream} 进行过滤会产生一个没有过滤元素的新 {@code Stream}，而不是从源集合中删除元素。</li>
 *     <li>懒惰。许多流操作，如过滤、映射或去重，可以懒惰地实现，提供优化的机会。例如，“找到第一个包含三个连续元音的字符串”不需要检查所有输入字符串。
 *     流操作分为中间操作（产生 {@code Stream} 的操作）和终端操作（产生值或副作用的操作）。中间操作总是懒惰的。</li>
 *     <li>可能无界。虽然集合有有限的大小，但流不必如此。短路操作如 {@code limit(n)} 或
 *     {@code findFirst()} 可以使对无限流的计算在有限时间内完成。</li>
 *     <li>可消费。流中的元素在其生命周期中只能被访问一次。像 {@link java.util.Iterator} 一样，需要生成新的流来重新访问源的相同元素。
 *     </li>
 * </ul>
 *
 * 流可以通过多种方式获得。一些示例如下：
 * <ul>
 *     <li>通过 {@link java.util.Collection} 的 {@code stream()} 和
 *     {@code parallelStream()} 方法；</li>
 *     <li>通过 {@link java.util.Arrays#stream(Object[])} 从数组中；</li>
 *     <li>通过流类上的静态工厂方法，如
 *     {@link java.util.stream.Stream#of(Object[])},
 *     {@link java.util.stream.IntStream#range(int, int)}
 *     或 {@link java.util.stream.Stream#iterate(Object, UnaryOperator)}；</li>
 *     <li>通过 {@link java.io.BufferedReader#lines()} 获取文件的行；</li>
 *     <li>通过 {@link java.nio.file.Files} 中的方法获取文件路径的流；</li>
 *     <li>通过 {@link java.util.Random#ints()} 获取随机数的流；</li>
 *     <li>JDK 中的许多其他流生成方法，包括
 *     {@link java.util.BitSet#stream()},
 *     {@link java.util.regex.Pattern#splitAsStream(java.lang.CharSequence)},
 *     和 {@link java.util.jar.JarFile#stream()}。</li>
 * </ul>
 *
 * <p>第三方库可以使用 <a href="package-summary.html#StreamSources">这些技术</a> 提供额外的流源。
 *
 * <h2><a name="StreamOps">流操作和管道</a></h2>
 *
 * <p>流操作分为 <em>中间</em> 操作和 <em>终端</em> 操作，并组合形成 <em>流
 * 管道</em>。流管道由源（如 {@code Collection}、数组、生成函数或 I/O 通道）开始；
 * 紧接着是零个或多个中间操作，如
 * {@code Stream.filter} 或 {@code Stream.map}；最后是一个终端操作，如
 * {@code Stream.forEach} 或 {@code Stream.reduce}。
 *
 * <p>中间操作返回一个新的流。它们总是
 * <em>懒惰</em> 的；执行中间操作如
 * {@code filter()} 实际上不会执行任何过滤，而是创建一个新的流，当遍历时包含初始流中符合给定谓词的元素。遍历
 * 管道源的操作不会在执行终端操作之前开始。
 *
 * <p>终端操作，如 {@code Stream.forEach} 或
 * {@code IntStream.sum}，可能会遍历流以产生结果或副作用。执行终端操作后，流管道被视为已消费，不能再使用；如果需要再次遍历
 * 相同的数据源，必须返回数据源以获取新的流。几乎所有情况下，终端操作都是 <em>急切</em> 的，
 * 在返回之前完成对数据源的遍历和管道的处理。只有终端操作 {@code iterator()} 和
 * {@code spliterator()} 不是；这些操作作为“逃生舱”提供，以支持任意的客户端控制的管道遍历，当现有操作不足以完成任务时。
 *
 * <p>懒惰地处理流可以显著提高效率；在上述过滤-映射-求和示例中，过滤、映射和求和可以融合成一次对数据的遍历，中间状态最少。懒惰处理还可以避免在不需要时检查所有数据；对于“找到第一个长度超过 1000 个字符的字符串”这样的操作，只需要检查足够多的字符串，找到具有所需特征的字符串即可，而不需要检查所有字符串。（当输入流是无限而非仅仅是大时，这种行为变得更加重要。）
 *
 * <p>中间操作进一步分为 <em>无状态</em>
 * 和 <em>有状态</em> 操作。无状态操作，如 {@code filter} 和 {@code map}，在处理新元素时不会保留之前看到的元素的状态——每个元素可以独立于对其他元素的操作进行处理。有状态操作，如
 * {@code distinct} 和 {@code sorted}，在处理新元素时可能需要结合之前看到的元素的状态。
 *
 * <p>有状态操作可能需要处理整个输入才能产生结果。例如，对流进行排序时，必须看到流中的所有元素才能产生结果。因此，在并行计算中，包含有状态中间操作的某些管道可能需要多次遍历数据或需要缓冲大量数据。只包含无状态中间操作的管道可以在单次遍历中处理，无论是顺序还是并行，数据缓冲最少。
 *
 * <p>此外，一些操作被认为是 <em>短路</em> 操作。中间操作是短路操作，如果在面对无限输入时，它可能产生有限的流作为结果。终端操作是短路操作，如果在面对无限输入时，它可能在有限时间内终止。管道中包含短路操作是处理无限流在有限时间内正常终止的必要但不充分条件。
 *
 * <h3>并行性</h3>
 *
 * <p>使用显式的 {@code for-} 循环处理元素本质上是串行的。流通过将计算重构为聚合操作管道，而不是对每个单独元素的命令式操作，从而促进并行执行。所有流操作都可以顺序或并行执行。JDK 中的流实现默认创建顺序流，除非显式请求并行。例如，{@code Collection} 有方法
 * {@link java.util.Collection#stream} 和 {@link java.util.Collection#parallelStream}，
 * 分别生成顺序和并行流；其他流生成方法如 {@link java.util.stream.IntStream#range(int, int)}
 * 生成顺序流，但可以通过调用其 {@link java.util.stream.BaseStream#parallel()} 方法高效地并行化。要并行执行上述“小部件重量总和”查询，我们将
 * 这样做：
 *
 * <pre>{@code
 *     int sumOfWeights = widgets.}<code><b>parallelStream()</b></code>{@code
 *                               .filter(b -> b.getColor() == RED)
 *                               .mapToInt(b -> b.getWeight())
 *                               .sum();
 * }</pre>
 *
 * <p>这个示例的顺序和并行版本之间唯一的区别是初始流的创建，使用 "{@code parallelStream()}"
 * 而不是 "{@code stream()}”。当终端操作被调用时，根据调用终端操作的流的方向，流管道将顺序或并行执行。是否流将顺序或并行执行可以通过 {@code isParallel()} 方法确定，流的方向可以通过
 * {@link java.util.stream.BaseStream#sequential()} 和
 * {@link java.util.stream.BaseStream#parallel()} 操作修改。当终端操作被调用时，根据调用终端操作的流的模式，流管道将顺序或并行执行。
 *
 * <p>除了明确标识为非确定性的操作，如 {@code findAny()}，流是顺序执行还是并行执行不应改变计算结果。
 *
 * <p>大多数流操作接受描述用户指定行为的参数，这些参数通常是 lambda 表达式。为了保持正确的行为，这些 <em>行为参数</em> 必须是 <em>非干扰的</em>，并且在大多数情况下必须是 <em>无状态的</em>。这些参数总是
 * <a href="../function/package-summary.html">函数接口</a> 的实例，如 {@link java.util.function.Function}，并且通常是 lambda 表达式或方法引用。
 *
 * <h3><a name="NonInterference">非干扰</a></h3>
 *
 * 流使您能够对各种数据源（包括非线程安全的集合如
 * {@code ArrayList}）执行可能并行的聚合操作。这只有在防止
 * <em>干扰</em> 数据源时才可能。除了逃逸舱操作 {@code iterator()} 和
 * {@code spliterator()}，执行从调用终端操作开始，到终端操作完成结束。对于大多数数据源，防止干扰意味着确保在执行流管道期间
 * <em>不修改数据源</em>。唯一的例外是并发集合，这些集合专门设计用于处理并发修改。并发流源是那些其 {@code Spliterator} 报告
 * {@code CONCURRENT} 特性的流源。
 *
 * <p>因此，如果流源可能不是并发的，流管道中的行为参数不应修改流的数据源。
 * 如果行为参数修改或导致修改非并发数据源的流，就会
 * <em>干扰</em> 数据源。非干扰的需求适用于所有管道，而不仅仅是并行管道。除非流源是并发的，否则在执行流管道期间修改流的数据源可能会导致异常、错误结果或不符合规范的行为。
 *
 * 对于行为良好的流源，可以在终端操作开始之前修改源，并且这些修改将反映在覆盖的元素中。例如，考虑以下代码：
 *
 * <pre>{@code
 *     List<String> l = new ArrayList(Arrays.asList("one", "two"));
 *     Stream<String> sl = l.stream();
 *     l.add("three");
 *     String s = sl.collect(joining(" "));
 * }</pre>
 *
 * 首先创建一个包含两个字符串的列表：“one” 和 “two”。然后从该列表创建一个流。接下来通过添加第三个字符串：“three” 修改列表。最后收集并连接流的元素。由于在终端 {@code collect}
 * 操作开始之前修改了列表，结果将是一个字符串“one two three”。JDK 集合返回的所有流，以及大多数其他 JDK 类生成的流，在这方面都是行为良好的；对于其他库生成的流，请参阅
 * <a href="package-summary.html#StreamSources">低级流构造</a> 以了解构建行为良好流的要求。
 *
 * <h3><a name="Statelessness">无状态行为</a></h3>
 *
 * 如果流操作的行为参数是 <em>有状态的</em>，流管道的结果可能是非确定性的或不正确的。有状态的 lambda（或实现适当函数接口的其他对象）是其结果依赖于在执行流管道期间可能发生变化的状态。例如，以下 {@code map()} 中的参数
 * 是一个有状态的 lambda：
 *
 * <pre>{@code
 *     Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());
 *     stream.parallel().map(e -> { if (seen.add(e)) return 0; else return e; })...
 * }</pre>
 *
 * 如果在并行执行映射操作，由于线程调度的不同，对于相同的输入，结果可能会因运行而异，而使用无状态的 lambda 表达式，结果将始终相同。
 *
 * <p>此外，尝试从行为参数中访问可变状态会使您在安全性和性能之间面临一个糟糕的选择；如果您不对该状态的访问进行同步，您将面临数据竞争，因此您的代码是错误的；但如果您对状态的访问进行同步，您可能会因为同步而削弱您希望从并行化中获得的好处。最好的方法是完全避免在流操作中使用有状态的行为参数；通常有办法重构流管道以避免状态性。
 *
 * <h3>副作用</h3>
 *
 * 在流操作的行为参数中使用副作用通常是不鼓励的，因为它们通常会导致无意中违反无状态要求，以及其他线程安全风险。
 *
 * <p>如果行为参数确实有副作用，除非明确说明，否则不会保证这些副作用对其他线程的
 * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>可见性</i></a>
 * 以及在同一流管道中对“相同”元素的不同操作的执行线程。此外，这些效果的顺序可能会令人惊讶。即使管道被约束产生一个
 * <em>结果</em> 与流源的遍历顺序一致（例如，{@code IntStream.range(0,5).parallel().map(x -> x*2).toArray()}
 * 必须产生 {@code [0, 2, 4, 6, 8]}），也不会保证映射函数应用于单个元素的顺序，或给定元素的行为参数在哪个线程中执行。
 *
 * <p>许多可能倾向于使用副作用的计算可以更安全、更高效地表达为无副作用的形式，例如使用
 * <a href="package-summary.html#Reduction">归约</a> 而不是可变累加器。然而，使用 {@code println()} 进行调试的副作用通常是无害的。少数几个流操作，如
 * {@code forEach()} 和 {@code peek()}，只能通过副作用操作；这些操作应谨慎使用。
 *
 * <p>作为如何将不适当使用副作用的流管道转换为不使用副作用的流管道的示例，以下代码搜索一个字符串流，找到与给定正则表达式匹配的字符串，并将匹配项放入列表中。
 *
 * <pre>{@code
 *     ArrayList<String> results = new ArrayList<>();
 *     stream.filter(s -> pattern.matcher(s).matches())
 *           .forEach(s -> results.add(s));  // 不必要的副作用！
 * }</pre>
 *
 * 这段代码不必要地使用了副作用。如果并行执行，由于 {@code ArrayList} 的非线程安全性，会导致错误结果，而添加必要的同步会导致争用，削弱并行化的好处。此外，这里使用副作用是完全不必要的；可以将 {@code forEach()} 替换为更安全、更高效且更易于并行化的归约操作：
 *
 * <pre>{@code
 *     List<String> results =
 *         stream.filter(s -> pattern.matcher(s).matches())
 *               .collect(Collectors.toList());  // 无副作用！
 * }</pre>
 *
 * <h3><a name="Ordering">顺序</a></h3>
 *
 * <p>流可能有或没有定义的 <em>遍历顺序</em>。流是否有遍历顺序取决于源和中间操作。某些流源（如 {@code List} 或数组）本质上是有序的，而其他源（如 {@code HashSet}）则不是。某些中间操作，如 {@code sorted()}，可能会对无序流施加遍历顺序，而其他操作可能会使有序流无序，如 {@link java.util.stream.BaseStream#unordered()}。此外，某些终端操作可能会忽略遍历顺序，如
 * {@code forEach()}。
 *
 * <p>如果流是有序的，大多数操作都受遍历顺序的约束；如果源是一个包含 {@code [1, 2, 3]} 的 {@code List}，那么执行 {@code map(x -> x*2)} 的结果必须是 {@code [2, 4, 6]}。然而，如果源没有定义的遍历顺序，那么 {@code [2, 4, 6]} 的任何排列都是有效的结果。
 *
 * <p>对于顺序流，存在或不存在遍历顺序不会影响性能，只影响确定性。如果流是有序的，对相同源重复执行相同的流管道将产生相同的结果；如果流是无序的，重复执行可能会产生不同的结果。
 *
 * <p>对于并行流，放松顺序约束有时可以提高执行效率。某些聚合操作，如过滤重复项（{@code distinct()}）或分组归约（{@code Collectors.groupingBy()}），如果元素顺序不重要，可以更高效地实现。同样，与遍历顺序紧密相关的操作，如 {@code limit()}，可能需要缓冲以确保正确的顺序，从而削弱并行化的好处。在流具有遍历顺序，但用户不特别
 * <em>关心</em> 该遍历顺序的情况下，使用 {@link java.util.stream.BaseStream#unordered() unordered()} 显式取消顺序可能提高某些有状态或终端操作的并行性能。然而，大多数流管道，如上述“小部件重量总和”示例，即使在顺序约束下仍然可以高效并行化。
 *
 * <h2><a name="Reduction">归约操作</a></h2>
 *
 * <em>归约</em> 操作（也称为 <em>折叠</em>）接受一系列输入元素，并通过重复应用组合操作将它们组合成一个单一的汇总结果，如求一组数字的和或最大值，或将元素累积到列表中。流类有多种形式的通用归约操作，称为
 * {@link java.util.stream.Stream#reduce(java.util.function.BinaryOperator) reduce()}
 * 和 {@link java.util.stream.Stream#collect(java.util.stream.Collector) collect()}，以及多种专用归约形式，如
 * {@link java.util.stream.IntStream#sum() sum()}，{@link java.util.stream.IntStream#max() max()}，或 {@link java.util.stream.IntStream#count() count()}。
 *
 * <p>当然，这样的操作可以很容易地通过简单的顺序循环实现，如下所示：
 * <pre>{@code
 *    int sum = 0;
 *    for (int x : numbers) {
 *       sum += x;
 *    }
 * }</pre>
 * 然而，与上述的可变累加相比，归约操作有很好的理由。归约不仅“更抽象”——它操作整个流而不是单个元素——而且如果用于处理元素的函数是
 * <a href="package-summary.html#Associativity">结合的</a> 和
 * <a href="package-summary.html#NonInterfering">无状态的</a>，归约操作本质上是可并行化的。例如，给定一个数字流，我们想找到它们的和，可以写：
 * <pre>{@code
 *    int sum = numbers.stream().reduce(0, (x,y) -> x+y);
 * }</pre>
 * 或：
 * <pre>{@code
 *    int sum = numbers.stream().reduce(0, Integer::sum);
 * }</pre>
 *
 * <p>这些归约操作可以安全地并行执行，几乎不需要修改：
 * <pre>{@code
 *    int sum = numbers.parallelStream().reduce(0, Integer::sum);
 * }</pre>
 *
 * <p>归约可以并行化得很好，因为实现可以在子集上并行操作，然后将中间结果合并以获得最终正确的答案。（即使语言有一个“并行 for-each”构造，可变累加方法仍然需要开发人员提供
 * 线程安全的更新到共享累加变量 {@code sum}，并且所需的同步可能会消除并行化带来的任何性能提升。）使用 {@code reduce()} 代替可以完全消除并行化归约操作的负担，库可以提供一个高效的并行实现，而不需要额外的同步。
 *
 * <p>前面的“小部件”示例展示了归约如何与其他操作结合，用批量操作替换 for 循环。如果 {@code widgets} 是一个包含 {@code Widget} 对象的集合，这些对象有一个 {@code getWeight} 方法，我们可以找到最重的小部件：
 * <pre>{@code
 *     OptionalInt heaviest = widgets.parallelStream()
 *                                   .mapToInt(Widget::getWeight)
 *                                   .max();
 * }</pre>
 *
 * <p>在其更通用的形式中，类型为 {@code <T>} 的元素上的 {@code reduce} 操作，产生类型为 {@code <U>} 的结果，需要三个参数：
 * <pre>{@code
 * <U> U reduce(U identity,
 *              BiFunction<U, ? super T, U> accumulator,
 *              BinaryOperator<U> combiner);
 * }</pre>
 * 这里，<em>标识元素</em> 既是归约的初始种子值，也是没有输入元素时的默认结果。<em>累加器</em> 函数接受一个部分结果和下一个元素，产生一个新的部分结果。<em>组合器</em> 函数将两个部分结果组合成一个新的部分结果。（在并行归约中，输入被分区，为每个分区计算一个部分累加结果，然后将部分结果组合以产生最终结果。）
 *
 * <p>更正式地说，<em>标识元素</em> 必须是 <em>组合器</em> 函数的 <em>标识</em>。这意味着对于所有 {@code u}，
 * {@code combiner.apply(identity, u)} 等于 {@code u}。此外，<em>组合器</em> 函数必须是
 * <a href="package-summary.html#Associativity">结合的</a> 并且必须与 <em>累加器</em> 函数兼容：对于所有 {@code u}
 * 和 {@code t}，{@code combiner.apply(u, accumulator.apply(identity, t))} 必须
 * 与 {@code accumulator.apply(u, t)} 相等。
 *
 * <p>三参数形式是两参数形式的泛化，将映射步骤合并到累加步骤中。我们可以将简单的求和示例重新写成更通用的形式：
 * <pre>{@code
 *     int sumOfWeights = widgets.stream()
 *                               .reduce(0,
 *                                       (sum, b) -> sum + b.getWeight())
 *                                       Integer::sum);
 * }</pre>
 * 虽然这种形式是可行的，但显式的映射-归约形式更具可读性，因此通常应优先使用。泛化形式适用于可以显著优化映射和归约的场景。
 *
 * <h3><a name="MutableReduction">可变归约</a></h3>
 *
 * <em>可变归约操作</em> 将输入元素累积到一个可变的结果容器中，如 {@code Collection} 或 {@code StringBuilder}，在处理元素时。
 *
 * <p>如果我们想将一个字符串流连接成一个长字符串，我们 <em>可以</em> 使用普通的归约实现：
 * <pre>{@code
 *     String concatenated = strings.reduce("", String::concat)
 * }</pre>
 *
 * <p>我们将会得到所需的结果，甚至可以并行执行。然而，性能可能不会令人满意！这样的实现会进行大量的字符串复制，运行时间将是 <em>O(n^2)</em> 的字符数。一个更高效的实现方法是使用一个可变容器，如 {@link java.lang.StringBuilder}，来累积结果。我们可以使用与普通归约相同的并行化技术来实现可变归约。
 *
 * <p>可变归约操作称为
 * {@link java.util.stream.Stream#collect(Collector) collect()}，因为它将所需的结果收集到一个结果容器中，如 {@code Collection}。
 * 一个 {@code collect} 操作需要三个函数：一个供应商函数来构造结果容器的新实例，一个累加器函数将输入元素累积到结果容器中，一个组合函数将一个结果容器的内容合并到另一个结果容器中。这种形式与普通归约的通用形式非常相似：
 * <pre>{@code
 * <R> R collect(Supplier<R> supplier,
 *               BiConsumer<R, ? super T> accumulator,
 *               BiConsumer<R, R> combiner);
 * }</pre>
 * <p>与 {@code reduce()} 一样，以这种方式抽象表达 {@code collect} 的一个好处是它可以直接并行化：我们可以并行累积部分结果，然后将它们合并，只要累加器和组合函数满足适当的条件。例如，要将流中元素的字符串表示形式收集到一个 {@code ArrayList} 中，我们可以编写显式的顺序 for-each 形式：
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
 * 这里，我们的供应商是 {@link java.util.ArrayList#ArrayList() ArrayList 构造函数}，累加器将字符串化的元素添加到 {@code ArrayList} 中，组合器使用 {@link java.util.ArrayList#addAll addAll} 将字符串从一个容器复制到另一个容器。
 *
 * <p>{@code collect} 的供应商、累加器和组合器三个方面是紧密耦合的。我们可以使用 {@link java.util.stream.Collector} 抽象来捕获所有三个方面。上述将字符串收集到 {@code List} 中的示例可以使用标准的 {@code Collector} 重写为：
 * <pre>{@code
 *     List<String> strings = stream.map(Object::toString)
 *                                  .collect(Collectors.toList());
 * }</pre>
 *
 * <p>将可变归约封装到一个 {@link java.util.stream.Collector} 中还有另一个优势：可组合性。类 {@link java.util.stream.Collectors} 包含许多预定义的收集器工厂，包括将一个收集器转换为另一个的组合器。例如，假设我们有一个计算员工工资总和的收集器，如下所示：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Integer> summingSalaries
 *         = Collectors.summingInt(Employee::getSalary);
 * }</pre>
 *
 * （第二个类型参数的 {@code ?} 表示我们不关心此收集器使用的中间表示。）
 * 如果我们想创建一个按部门汇总工资总和的收集器，我们可以使用
 * {@link java.util.stream.Collectors#groupingBy(java.util.function.Function, java.util.stream.Collector) groupingBy} 重用 {@code summingSalaries}：
 *
 * <pre>{@code
 *     Map<Department, Integer> salariesByDept
 *         = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment,
 *                                                            summingSalaries));
 * }</pre>
 *
 * <p>与常规归约操作一样，只有满足适当的条件，{@code collect()} 操作才能并行化。对于任何部分累积的结果，将其与空结果容器组合必须产生等效的结果。也就是说，对于任何部分累积的结果
 * {@code p}，它是通过一系列累加器和组合器调用得到的，{@code p} 必须等效于
 * {@code combiner.apply(p, supplier.get())}。
 *
 * <p>无论计算如何拆分，都必须产生等效的结果。对于任何输入元素 {@code t1} 和 {@code t2}，以下计算中的结果
 * {@code r1} 和 {@code r2} 必须等效：
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
 * <p>这里，等效性通常根据 {@link java.lang.Object#equals(Object)} 判断。但在某些情况下，等效性可以放宽以考虑顺序差异。
 *
 * <h3><a name="ConcurrentReduction">归约、并发和顺序</a></h3>
 *
 * 对于某些复杂的归约操作，例如产生 {@code Map} 的 {@code collect()}，如：
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .collect(Collectors.groupingBy(Transaction::getBuyer));
 * }</pre>
 * 实际上并行执行该操作可能是反productive的。这是因为合并步骤（通过键合并两个 {@code Map}）对于某些 {@code Map} 实现来说可能非常昂贵。
 *
 * <p>然而，假设用于此归约的结果容器是一个可并发修改的集合——例如一个
 * {@link java.util.concurrent.ConcurrentHashMap}。在这种情况下，可以并行调用累加器，将结果直接并发地存入同一个共享结果容器中，从而消除将不同结果容器合并的需要。这可能提高并行执行的性能。我们称这种归约为
 * <em>并发</em> 归约。
 *
 * <p>支持并发归约的 {@link java.util.stream.Collector} 标记有
 * {@link java.util.stream.Collector.Characteristics#CONCURRENT} 特性。然而，可并发修改的集合也有缺点。如果多个线程并发地将结果存入同一个共享容器中，结果存入的顺序是不确定的。因此，只有在流处理过程中顺序不重要时，才能进行并发归约。{@link java.util.stream.Stream#collect(Collector)}
 * 实现只有在以下情况下才会执行并发归约：
 * <ul>
 * <li>流是并行的；</li>
 * <li>收集器具有
 * {@link java.util.stream.Collector.Characteristics#CONCURRENT} 特性，且；</li>
 * <li>流是无序的，或者收集器具有
 * {@link java.util.stream.Collector.Characteristics#UNORDERED} 特性。
 * </ul>
 * 您可以使用 {@link java.util.stream.BaseStream#unordered()} 方法确保流是无序的。例如：
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .unordered()
 *               .collect(groupingByConcurrent(Transaction::getBuyer));
 * }</pre>
 * （其中 {@link java.util.stream.Collectors#groupingByConcurrent} 是 {@code groupingBy} 的并发等效形式）。
 *
 * <p>如果需要确保给定键的元素按源中出现的顺序出现，那么我们不能使用并发归约，因为顺序是并发插入的牺牲品。在这种情况下，我们只能实现顺序归约或基于合并的并行归约。
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
 * 到目前为止，所有流示例都使用了类似
 * {@link java.util.Collection#stream()} 或 {@link java.util.Arrays#stream(Object[])}
 * 的方法来获取流。这些提供流的方法是如何实现的？
 *
 * <p>类 {@link java.util.stream.StreamSupport} 有多个低级方法用于创建流，所有这些方法都使用某种形式的
 * {@link java.util.Spliterator}。分隔器是并行版本的
 * {@link java.util.Iterator}；它描述了（可能是无限的）元素集合，支持顺序推进、批量遍历和将输入的一部分拆分为另一个分隔器，以便并行处理。在最低级别，所有流都由分隔器驱动。
 *
 * <p>实现分隔器有许多选择，几乎都是在实现的简单性和使用该分隔器的流的运行时性能之间的权衡。创建分隔器的最简单但性能最差的方法是使用
 * {@link java.util.Spliterators#spliteratorUnknownSize(java.util.Iterator, int)}
 * 从迭代器创建分隔器。虽然这样的分隔器可以工作，但它可能会提供较差的并行性能，因为我们丢失了大小信息（底层数据集有多大），并且受到简单的拆分算法的限制。
 *
 * <p>高质量的分隔器将提供平衡且已知大小的拆分、准确的大小信息，以及分隔器或数据的许多其他
 * {@link java.util.Spliterator#characteristics() 特性}，这些特性可以用于优化执行。
 *
 * <p>可变数据源的分隔器面临额外的挑战；
 * 绑定到数据的时间，因为数据可能在分隔器创建和流管道执行之间发生变化。理想情况下，流的分隔器会报告一个特性，表示


 * {@code IMMUTABLE} 或 {@code CONCURRENT}；如果不是，则应为
 * <a href="../Spliterator.html#binding"><em>延迟绑定</em></a>。如果源不能直接提供推荐的 spliterator，
 * 它可以使用 {@code Supplier} 间接提供 spliterator，并通过接受 {@code Supplier} 的版本
 * {@link java.util.stream.StreamSupport#stream(Supplier, int, boolean) stream()} 构建流。
 * 仅在流管道的终端操作开始后，才从供应商处获取 spliterator。
 *
 * <p>这些要求显著减少了流源的修改与流管道执行之间潜在干扰的范围。基于具有所需特性的 spliterator 的流，
 * 或使用基于 Supplier 的工厂形式的流，在终端操作开始之前对数据源的修改是免疫的（前提是流操作的行为参数满足
 * 非干扰和无状态的所需标准）。有关更多详细信息，请参阅
 * <a href="package-summary.html#NonInterference">非干扰</a>。
 *
 * @since 1.8
 */
package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
