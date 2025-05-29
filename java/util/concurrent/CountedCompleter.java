
/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

/**
 * 一个 {@link ForkJoinTask}，当触发且没有剩余的待处理动作时执行完成动作。
 * 与其它形式的 ForkJoinTasks 相比，CountedCompleters 在子任务停滞和阻塞的情况下通常更健壮，但编程时不太直观。CountedCompleter 的使用类似于其他基于完成的组件（如 {@link java.nio.channels.CompletionHandler}），
 * 但不同之处在于可能需要多个 <em>待处理</em> 完成来触发完成动作 {@link #onCompletion(CountedCompleter)}，而不仅仅是其中一个。
 * 除非另有初始化，否则 {@linkplain #getPendingCount 待处理计数} 从零开始，但可以使用方法 {@link #setPendingCount}、{@link #addToPendingCount} 和
 * {@link #compareAndSetPendingCount}（原子地）更改。调用 {@link #tryComplete} 时，如果待处理动作计数不为零，则递减；否则，执行完成动作，如果此 completer 本身有 completer，
 * 则继续处理其 completer。与相关的同步组件（如 {@link java.util.concurrent.Phaser Phaser} 和
 * {@link java.util.concurrent.Semaphore Semaphore}）一样，这些方法仅影响内部计数；它们不会建立任何进一步的内部记录。特别是，不会维护待处理任务的身份。如下面所示，您可以创建子类来记录一些或所有待处理任务或其结果，当需要时。
 * 如下面所示，还提供了支持完成遍历定制的实用方法。然而，由于 CountedCompleters 仅提供基本的同步机制，创建进一步的抽象子类可能很有用，这些子类维护适合一组相关用法的链接、字段和附加支持方法。
 *
 * <p>一个具体的 CountedCompleter 类必须定义方法 {@link #compute}，在大多数情况下（如下面所示），应在返回前调用
 * {@code tryComplete()} 一次。该类还可以选择性地覆盖方法 {@link #onCompletion(CountedCompleter)} 以在正常完成时执行动作，以及方法
 * {@link #onExceptionalCompletion(Throwable, CountedCompleter)} 以在任何异常时执行动作。
 *
 * <p>通常情况下，CountedCompleters 不承载结果，因此通常声明为 {@code CountedCompleter<Void>}，并且总是返回 {@code null} 作为结果值。在其他情况下，
 * 您应该覆盖方法 {@link #getRawResult} 以从 {@code join(), invoke()} 和相关方法提供结果。通常，此方法应返回 CountedCompleter 对象在完成时持有的结果的字段（或一个或多个字段的函数）。
 * 方法 {@link #setRawResult} 默认在 CountedCompleters 中不起作用。虽然可以覆盖此方法以维护其他对象或字段来保存结果数据，但这很少适用。
 *
 * <p>一个没有 completer（即，{@link #getCompleter} 返回 {@code null}）的 CountedCompleter 可以用作具有此附加功能的常规 ForkJoinTask。
 * 然而，任何有另一个 completer 的 completer 仅作为其他计算的内部辅助，因此其任务状态（如在 {@link ForkJoinTask#isDone} 等方法中报告的）是任意的；此状态仅在显式调用
 * {@link #complete}、{@link ForkJoinTask#cancel}、{@link ForkJoinTask#completeExceptionally(Throwable)} 或方法 {@code compute} 异常完成时更改。在任何异常完成时，
 * 如果存在 completer 且尚未完成，异常可能会传递给任务的 completer（及其 completer，依此类推）。类似地，取消内部 CountedCompleter 仅对那个 completer 有局部影响，因此通常不太有用。
 *
 * <p><b>示例用法。</b>
 *
 * <p><b>并行递归分解。</b> CountedCompleters 可以以类似于 {@link RecursiveAction} 经常使用的树形结构排列，尽管设置它们的方式通常有所不同。在这里，每个任务的 completer 是计算树中的父任务。
 * 即使它们涉及更多的记录，CountedCompleters 在将可能耗时的操作（无法进一步细分）应用于数组或集合的每个元素时可能是更好的选择；特别是当某些元素完成所需的时间显著不同于其他元素时，无论是由于内在差异（例如 I/O）
 * 还是辅助影响（如垃圾收集）。因为 CountedCompleters 提供自己的延续，其他线程不需要阻塞等待执行它们。
 *
 * <p>例如，这里是一个使用二分递归分解将工作分解为单个部分（叶任务）的类的初始版本。即使工作被分解为单个调用，基于树的技术通常也优于直接分叉叶任务，因为它们减少了线程间通信并提高了负载平衡。
 * 在递归情况下，每对子任务中第二个完成的子任务会触发其父任务的完成（因为没有执行结果组合，所以方法 {@code onCompletion} 的默认空操作实现没有被覆盖）。静态实用方法设置基础任务并调用它
 * （这里，隐式使用 {@link ForkJoinPool#commonPool()}）。
 *
 * <pre> {@code
 * class MyOperation<E> { void apply(E e) { ... }  }
 *
 * class ForEach<E> extends CountedCompleter<Void> {
 *
 *   public static <E> void forEach(E[] array, MyOperation<E> op) {
 *     new ForEach<E>(null, array, op, 0, array.length).invoke();
 *   }
 *
 *   final E[] array; final MyOperation<E> op; final int lo, hi;
 *   ForEach(CountedCompleter<?> p, E[] array, MyOperation<E> op, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.op = op; this.lo = lo; this.hi = hi;
 *   }
 *
 *   public void compute() { // version 1
 *     if (hi - lo >= 2) {
 *       int mid = (lo + hi) >>> 1;
 *       setPendingCount(2); // 必须在分叉前设置待处理计数
 *       new ForEach(this, array, op, mid, hi).fork(); // 右子任务
 *       new ForEach(this, array, op, lo, mid).fork(); // 左子任务
 *     }
 *     else if (hi > lo)
 *       op.apply(array[lo]);
 *     tryComplete();
 *   }
 * }}</pre>
 *
 * 通过观察，在递归情况下，任务在分叉其右任务后没有其他要做的工作，因此可以直接在其返回前调用其左任务。这也是尾递归消除的类比。此外，因为任务在其执行左任务后返回（而不是继续调用
 * {@code tryComplete}），待处理计数设置为一：
 *
 * <pre> {@code
 * class ForEach<E> ...
 *   public void compute() { // version 2
 *     if (hi - lo >= 2) {
 *       int mid = (lo + hi) >>> 1;
 *       setPendingCount(1); // 只有一个待处理
 *       new ForEach(this, array, op, mid, hi).fork(); // 右子任务
 *       new ForEach(this, array, op, lo, mid).compute(); // 直接调用
 *     }
 *     else {
 *       if (hi > lo)
 *         op.apply(array[lo]);
 *       tryComplete();
 *     }
 *   }
 * }</pre>
 *
 * 作为进一步的改进，注意到左任务甚至不需要存在。而不是创建一个新的，我们可以使用原始任务迭代，并为每个分叉添加一个待处理计数。此外，因为此树中的任务没有实现
 * {@link #onCompletion(CountedCompleter)} 方法，可以使用 {@link #propagateCompletion} 替换 {@code tryComplete()}。
 *
 * <pre> {@code
 * class ForEach<E> ...
 *   public void compute() { // version 3
 *     int l = lo,  h = hi;
 *     while (h - l >= 2) {
 *       int mid = (l + h) >>> 1;
 *       addToPendingCount(1);
 *       new ForEach(this, array, op, mid, h).fork(); // 右子任务
 *       h = mid;
 *     }
 *     if (h > l)
 *       op.apply(array[l]);
 *     propagateCompletion();
 *   }
 * }</pre>
 *
 * 对此类的进一步改进可能包括预先计算待处理计数，以便在构造函数中建立，为叶步骤专门化类，每次迭代细分四个而不是两个，以及使用自适应阈值而不是总是细分到单个元素。
 *
 * <p><b>搜索。</b> 一个 CountedCompleters 树可以在数据结构的不同部分搜索一个值或属性，并在找到一个时在 {@link
 * java.util.concurrent.atomic.AtomicReference AtomicReference} 中报告结果。其他任务可以轮询结果以避免不必要的工作。（您还可以 {@linkplain #cancel
 * 取消} 其他任务，但通常更简单和高效的是让它们注意到结果已设置，如果已设置则跳过进一步的处理。）再次以数组为例，使用完全分区（在实践中，叶任务几乎总是处理多个元素）：
 *
 * <pre> {@code
 * class Searcher<E> extends CountedCompleter<E> {
 *   final E[] array; final AtomicReference<E> result; final int lo, hi;
 *   Searcher(CountedCompleter<?> p, E[] array, AtomicReference<E> result, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.result = result; this.lo = lo; this.hi = hi;
 *   }
 *   public E getRawResult() { return result.get(); }
 *   public void compute() { // 类似于 ForEach version 3
 *     int l = lo,  h = hi;
 *     while (result.get() == null && h >= l) {
 *       if (h - l >= 2) {
 *         int mid = (l + h) >>> 1;
 *         addToPendingCount(1);
 *         new Searcher(this, array, result, mid, h).fork();
 *         h = mid;
 *       }
 *       else {
 *         E x = array[l];
 *         if (matches(x) && result.compareAndSet(null, x))
 *           quietlyCompleteRoot(); // 根任务现在可连接
 *         break;
 *       }
 *     }
 *     tryComplete(); // 通常无论是否找到都完成
 *   }
 *   boolean matches(E e) { ... } // 如果找到返回 true
 *
 *   public static <E> E search(E[] array) {
 *       return new Searcher<E>(null, array, new AtomicReference<E>(), 0, array.length).invoke();
 *   }
 * }}</pre>
 *
 * 在这个例子中，以及在任务除了比较和设置公共结果外没有其他效果的其他例子中，尾部无条件调用 {@code tryComplete} 可以变为条件调用（{@code if (result.get() == null) tryComplete();}）
 * 因为一旦根任务完成，管理完成所需的进一步记录就不再需要。
 *
 * <p><b>记录子任务。</b> 需要访问多个子任务结果的 CountedCompleter 任务通常需要在方法 {@link #onCompletion(CountedCompleter)} 中访问这些结果。如下面的类所示
 * （执行简化的 map-reduce，其中映射和归约都是类型 {@code E}），在分而治之设计中，一种方法是让每个子任务记录其兄弟任务，以便在方法 {@code onCompletion} 中访问。
 * 这种技术适用于组合左结果和右结果顺序无关的归约；有序归约需要显式的左/右标识。上述示例中的其他简化变体也可能适用。
 *
 * <pre> {@code
 * class MyMapper<E> { E apply(E v) {  ...  } }
 * class MyReducer<E> { E apply(E x, E y) {  ...  } }
 * class MapReducer<E> extends CountedCompleter<E> {
 *   final E[] array; final MyMapper<E> mapper;
 *   final MyReducer<E> reducer; final int lo, hi;
 *   MapReducer<E> sibling;
 *   E result;
 *   MapReducer(CountedCompleter<?> p, E[] array, MyMapper<E> mapper,
 *              MyReducer<E> reducer, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.mapper = mapper;
 *     this.reducer = reducer; this.lo = lo; this.hi = hi;
 *   }
 *   public void compute() {
 *     if (hi - lo >= 2) {
 *       int mid = (lo + hi) >>> 1;
 *       MapReducer<E> left = new MapReducer(this, array, mapper, reducer, lo, mid);
 *       MapReducer<E> right = new MapReducer(this, array, mapper, reducer, mid, hi);
 *       left.sibling = right;
 *       right.sibling = left;
 *       setPendingCount(1); // 只有右子任务待处理
 *       right.fork();
 *       left.compute();     // 直接执行左子任务
 *     }
 *     else {
 *       if (hi > lo)
 *           result = mapper.apply(array[lo]);
 *       tryComplete();
 *     }
 *   }
 *   public void onCompletion(CountedCompleter<?> caller) {
 *     if (caller != this) {
 *       MapReducer<E> child = (MapReducer<E>)caller;
 *       MapReducer<E> sib = child.sibling;
 *       if (sib == null || sib.result == null)
 *         result = child.result;
 *       else
 *         result = reducer.apply(child.result, sib.result);
 *     }
 *   }
 *   public E getRawResult() { return result; }
 *
 *   public static <E> E mapReduce(E[] array, MyMapper<E> mapper, MyReducer<E> reducer) {
 *     return new MapReducer<E>(null, array, mapper, reducer,
 *                              0, array.length).invoke();
 *   }
 * }}</pre>
 *
 * 这里，方法 {@code onCompletion} 采取了许多完成设计中常见的形式。这种回调风格的方法在以下两种情况下之一，每次任务的待处理计数为零或变为零时触发一次：(1) 由任务本身触发，如果其待处理计数在调用
 * {@code tryComplete} 时为零，或 (2) 由其子任务之一在完成时触发并递减待处理计数至零。{@code caller} 参数区分这些情况。通常，当调用者是 {@code this} 时，不需要任何操作。
 * 否则，调用者参数（通常通过类型转换）可以提供要组合的值（和/或链接到其他值）。假设正确使用了待处理计数，方法 {@code onCompletion} 中的操作在任务及其子任务完成时发生（一次）。
 * 在此方法中访问此任务或其他已完成任务的字段时不需要额外的同步以确保线程安全。
 *
 * <p><b>完成遍历。</b> 如果使用 {@code onCompletion} 处理完成不适用或不方便，您可以使用方法 {@link #firstComplete} 和 {@link #nextComplete} 创建自定义遍历。
 * 例如，定义一个 MapReducer，仅在第三版 ForEach 示例的形式中拆分右子任务，完成必须沿未耗尽的子任务链接合作归约，可以如下实现：
 *
 * <pre> {@code
 * class MapReducer<E> extends CountedCompleter<E> { // version 2
 *   final E[] array; final MyMapper<E> mapper;
 *   final MyReducer<E> reducer; final int lo, hi;
 *   MapReducer<E> forks, next; // 记录子任务拆分的列表
 *   E result;
 *   MapReducer(CountedCompleter<?> p, E[] array, MyMapper<E> mapper,
 *              MyReducer<E> reducer, int lo, int hi, MapReducer<E> next) {
 *     super(p);
 *     this.array = array; this.mapper = mapper;
 *     this.reducer = reducer; this.lo = lo; this.hi = hi;
 *     this.next = next;
 *   }
 *   public void compute() {
 *     int l = lo,  h = hi;
 *     while (h - l >= 2) {
 *       int mid = (l + h) >>> 1;
 *       addToPendingCount(1);
 *       (forks = new MapReducer(this, array, mapper, reducer, mid, h, forks)).fork();
 *       h = mid;
 *     }
 *     if (h > l)
 *       result = mapper.apply(array[l]);
 *     // 通过沿子任务链接归约并推进来处理完成
 *     for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
 *       for (MapReducer t = (MapReducer)c, s = t.forks;  s != null; s = t.forks = s.next)
 *         t.result = reducer.apply(t.result, s.result);
 *     }
 *   }
 *   public E getRawResult() { return result; }
 *
 *   public static <E> E mapReduce(E[] array, MyMapper<E> mapper, MyReducer<E> reducer) {
 *     return new MapReducer<E>(null, array, mapper, reducer,
 *                              0, array.length, null).invoke();
 *   }
 * }}</pre>
 *
 * <p><b>触发器。</b> 一些 CountedCompleters 本身从未分叉，而是作为其他设计中的管道部分；包括那些在完成一个或多个异步任务时触发另一个异步任务的设计。例如：
 *
 * <pre> {@code
 * class HeaderBuilder extends CountedCompleter<...> { ... }
 * class BodyBuilder extends CountedCompleter<...> { ... }
 * class PacketSender extends CountedCompleter<...> {
 *   PacketSender(...) { super(null, 1); ... } // 在第二次完成时触发
 *   public void compute() { } // 从未调用
 *   public void onCompletion(CountedCompleter<?> caller) { sendPacket(); }
 * }
 * // 示例用法：
 * PacketSender p = new PacketSender();
 * new HeaderBuilder(p, ...).fork();
 * new BodyBuilder(p, ...).fork();
 * }</pre>
 *
 * @since 1.8
 * @author Doug Lea
 */
public abstract class CountedCompleter<T> extends ForkJoinTask<T> {
    private static final long serialVersionUID = 5232453752276485070L;


    /** 该任务的完成者，如果没有则为 null */
    final CountedCompleter<?> completer;
    /** 完成前的待处理任务数 */
    volatile int pending;

    /**
     * 使用给定的完成者和初始待处理计数创建一个新的 CountedCompleter。
     *
     * @param completer 该任务的完成者，如果没有则为 {@code null}
     * @param initialPendingCount 初始待处理计数
     */
    protected CountedCompleter(CountedCompleter<?> completer,
                               int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    /**
     * 使用给定的完成者和初始待处理计数为零创建一个新的 CountedCompleter。
     *
     * @param completer 该任务的完成者，如果没有则为 {@code null}
     */
    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
    }

    /**
     * 创建一个新的 CountedCompleter，没有完成者且初始待处理计数为零。
     */
    protected CountedCompleter() {
        this.completer = null;
    }

    /**
     * 该任务执行的主要计算。
     */
    public abstract void compute();

    /**
     * 当调用方法 {@link #tryComplete} 且待处理计数为零，或者无条件调用方法 {@link #complete} 时执行的动作。
     * 默认情况下，此方法不执行任何操作。可以通过检查给定的调用者参数的身份来区分不同的情况。
     * 如果不等于 {@code this}，则通常是可能包含结果（和/或其他结果的链接）以进行组合的子任务。
     *
     * @param caller 调用此方法的任务（可能是此任务本身）
     */
    public void onCompletion(CountedCompleter<?> caller) {
    }

    /**
     * 当调用方法 {@link #completeExceptionally(Throwable)} 或方法 {@link #compute} 抛出异常，
     * 且此任务尚未正常完成时执行的动作。进入此方法时，此任务 {@link ForkJoinTask#isCompletedAbnormally}。
     * 此方法的返回值控制进一步的传播：如果返回 {@code true} 且此任务有未完成的完成者，则该完成者也会异常完成，异常与该完成者相同。
     * 默认实现此方法不执行任何操作，仅返回 {@code true}。
     *
     * @param ex 异常
     * @param caller 调用此方法的任务（可能是此任务本身）
     * @return 如果此异常应传播到此任务的完成者（如果存在），则返回 {@code true}
     */
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        return true;
    }

    /**
     * 返回在该任务构造函数中设置的完成者，如果没有则返回 {@code null}。
     *
     * @return 完成者
     */
    public final CountedCompleter<?> getCompleter() {
        return completer;
    }

    /**
     * 返回当前的待处理计数。
     *
     * @return 当前的待处理计数
     */
    public final int getPendingCount() {
        return pending;
    }

    /**
     * 将待处理计数设置为给定值。
     *
     * @param count 计数
     */
    public final void setPendingCount(int count) {
        pending = count;
    }

    /**
     * 原子地将给定值加到待处理计数上。
     *
     * @param delta 要加的值
     */
    public final void addToPendingCount(int delta) {
        U.getAndAddInt(this, PENDING, delta);
    }

    /**
     * 如果当前值为给定的期望值，则原子地将待处理计数设置为给定的值。
     *
     * @param expected 期望值
     * @param count 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count);
    }

    /**
     * 如果待处理计数不为零，则原子地递减它。
     *
     * @return 进入此方法时的初始（未递减）待处理计数
     */
    public final int decrementPendingCountUnlessZero() {
        int c;
        do {} while ((c = pending) != 0 &&
                     !U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }

    /**
     * 返回当前计算的根任务；即，如果没有完成者则为该任务，否则为完成者的根任务。
     *
     * @return 当前计算的根任务
     */
    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this, p;
        while ((p = a.completer) != null)
            a = p;
        return a;
    }

    /**
     * 如果待处理计数不为零，则递减计数；否则调用 {@link #onCompletion(CountedCompleter)}，
     * 然后尝试完成此任务的完成者（如果存在），否则标记此任务为完成。
     */
    public final void tryComplete() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            if ((c = a.pending) == 0) {
                a.onCompletion(s);
                if ((a = (s = a).completer) == null) {
                    s.quietlyComplete();
                    return;
                }
            }
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))
                return;
        }
    }

    /**
     * 等同于 {@link #tryComplete}，但在完成路径上不调用 {@link #onCompletion(CountedCompleter)}：
     * 如果待处理计数不为零，则递减计数；否则，尝试完成此任务的完成者（如果存在），否则标记此任务为完成。
     * 在计算中不需要或不应为每个完成者调用 {@code onCompletion} 的情况下，此方法可能有用。
     */
    public final void propagateCompletion() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            if ((c = a.pending) == 0) {
                if ((a = (s = a).completer) == null) {
                    s.quietlyComplete();
                    return;
                }
            }
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))
                return;
        }
    }


    /**
     * 无论待处理计数如何，调用 {@link #onCompletion(CountedCompleter)}，将此任务标记为已完成，并进一步触发此任务的 completer（如果存在）上的 {@link #tryComplete}。
     * 给定的 rawResult 用作 {@link #setRawResult} 的参数，在调用 {@link #onCompletion(CountedCompleter)} 或将此任务标记为完成之前使用；其值仅对重写 {@code setRawResult} 的类有意义。
     * 此方法不会修改待处理计数。
     *
     * <p>当强制完成多个子任务结果中的任何一个（而不是全部）时，此方法可能很有用。
     * 但是，在常见的（并且推荐的）情况下，如果 {@code setRawResult} 未被重写，可以更简单地使用 {@code quietlyCompleteRoot();} 来达到同样的效果。
     *
     * @param rawResult 原始结果
     */
    public void complete(T rawResult) {
        CountedCompleter<?> p;
        setRawResult(rawResult);
        onCompletion(this);
        quietlyComplete();
        if ((p = completer) != null)
            p.tryComplete();
    }

    /**
     * 如果此任务的待处理计数为零，则返回此任务；否则递减其待处理计数并返回 {@code null}。
     * 此方法设计用于与 {@link #nextComplete} 一起在完成遍历循环中使用。
     *
     * @return 如果待处理计数为零，则返回此任务，否则返回 {@code null}
     */
    public final CountedCompleter<?> firstComplete() {
        for (int c;;) {
            if ((c = pending) == 0)
                return this;
            else if (U.compareAndSwapInt(this, PENDING, c, c - 1))
                return null;
        }
    }

    /**
     * 如果此任务没有 completer，则调用 {@link ForkJoinTask#quietlyComplete} 并返回 {@code null}。
     * 或者，如果 completer 的待处理计数不为零，则递减该待处理计数并返回 {@code null}。
     * 否则，返回 completer。此方法可以作为完成遍历循环的一部分用于同质任务层次结构：
     *
     * <pre> {@code
     * for (CountedCompleter<?> c = firstComplete();
     *      c != null;
     *      c = c.nextComplete()) {
     *   // ... 处理 c ...
     * }}</pre>
     *
     * @return completer，如果没有则返回 {@code null}
     */
    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> p;
        if ((p = completer) != null)
            return p.firstComplete();
        else {
            quietlyComplete();
            return null;
        }
    }

    /**
     * 等同于 {@code getRoot().quietlyComplete()}。
     */
    public final void quietlyCompleteRoot() {
        for (CountedCompleter<?> a = this, p;;) {
            if ((p = a.completer) == null) {
                a.quietlyComplete();
                return;
            }
            a = p;
        }
    }

    /**
     * 如果此任务尚未完成，则尝试处理最多给定数量的其他未处理任务，这些任务位于此任务的完成路径上（如果已知存在）。
     *
     * @param maxTasks 要处理的最大任务数。如果小于或等于零，则不处理任何任务。
     */
    public final void helpComplete(int maxTasks) {
        Thread t; ForkJoinWorkerThread wt;
        if (maxTasks > 0 && status >= 0) {
            if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
                (wt = (ForkJoinWorkerThread)t).pool.
                    helpComplete(wt.workQueue, this, maxTasks);
            else
                ForkJoinPool.common.externalHelpComplete(this, maxTasks);
        }
    }

    /**
     * 支持 ForkJoinTask 异常传播。
     */
    void internalPropagateException(Throwable ex) {
        CountedCompleter<?> a = this, s = a;
        while (a.onExceptionalCompletion(ex, s) &&
               (a = (s = a).completer) != null && a.status >= 0 &&
               a.recordExceptionalCompletion(ex) == EXCEPTIONAL)
            ;
    }

    /**
     * 实现 CountedCompleters 的执行约定。
     */
    protected final boolean exec() {
        compute();
        return false;
    }

    /**
     * 返回计算结果。默认返回 {@code null}，这适用于 {@code Void} 操作，但在其他情况下应重写，几乎总是返回一个字段或函数，该字段在完成时持有结果。
     *
     * @return 计算结果
     */
    public T getRawResult() { return null; }

    /**
     * 结果承载的 CountedCompleters 可以选择使用此方法来帮助维护结果数据。默认情况下，不执行任何操作。
     * 不建议重写。但是，如果此方法被重写以更新现有对象或字段，则通常必须定义为线程安全的。
     */
    protected void setRawResult(T t) { }

    // Unsafe 机制
    private static final sun.misc.Unsafe U;
    private static final long PENDING;
    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            PENDING = U.objectFieldOffset
                (CountedCompleter.class.getDeclaredField("pending"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
