
/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个 {@link ForkJoinTask}，当触发且没有剩余的待处理操作时执行完成操作。
 * CountedCompleters 通常在子任务停滞和阻塞的情况下比其他形式的 ForkJoinTasks 更加健壮，但编程时不如其他形式直观。CountedCompleter 的使用类似于其他基于完成的组件（如 {@link java.nio.channels.CompletionHandler}），不同之处在于可能需要多个 <em>待处理</em> 完成来触发完成操作 {@link #onCompletion(CountedCompleter)}，而不仅仅是其中一个。
 * 除非另有初始化，否则 {@linkplain #getPendingCount 待处理计数} 从零开始，但可以使用方法 {@link #setPendingCount}、{@link #addToPendingCount} 和 {@link #compareAndSetPendingCount} 进行（原子地）更改。调用 {@link #tryComplete} 时，如果待处理操作计数不为零，则递减该计数；否则，执行完成操作，如果此 completer 本身有 completer，则继续处理其 completer。与相关同步组件（如 {@link java.util.concurrent.Phaser Phaser} 和 {@link java.util.concurrent.Semaphore Semaphore}）一样，这些方法仅影响内部计数；它们不会建立任何进一步的内部账簿。特别是，不会维护待处理任务的身份。如下面所示，您可以创建子类，在需要时记录一些或所有待处理任务或其结果。如下面所示，还提供了支持完成遍历自定义的方法。然而，由于 CountedCompleters 仅提供基本的同步机制，因此可能需要创建进一步的抽象子类，以维护适合一组相关用法的链接、字段和附加支持方法。
 *
 * <p>一个具体的 CountedCompleter 类必须定义方法 {@link #compute}，该方法在大多数情况下（如下所示），应在返回之前调用 {@code tryComplete()} 一次。该类还可以选择性地覆盖方法 {@link #onCompletion(CountedCompleter)} 以在正常完成时执行操作，以及方法 {@link #onExceptionalCompletion(Throwable, CountedCompleter)} 以在发生任何异常时执行操作。
 *
 * <p>通常情况下，CountedCompleters 不承载结果，因此它们通常被声明为 {@code CountedCompleter<Void>}，并且总是返回 {@code null} 作为结果值。在其他情况下，您应该覆盖方法 {@link #getRawResult} 以从 {@code join(), invoke()} 和相关方法中提供结果。通常，此方法应返回 CountedCompleter 对象在完成时持有的结果的字段（或多个字段的函数）。方法 {@link #setRawResult} 默认在 CountedCompleters 中不起作用。可以覆盖此方法以维护其他对象或字段来保存结果数据，但这很少适用。
 *
 * <p>没有 completer（即，{@link #getCompleter} 返回 {@code null}）的 CountedCompleter 可以用作具有此附加功能的常规 ForkJoinTask。然而，任何有 completer 的 completer 仅作为其他计算的内部助手，因此其任务状态（如在 {@link ForkJoinTask#isDone} 等方法中报告的）是任意的；此状态仅在显式调用 {@link #complete}、{@link ForkJoinTask#cancel}、{@link ForkJoinTask#completeExceptionally(Throwable)} 或在方法 {@code compute} 中异常完成时发生变化。在任何异常完成时，异常可能会传递给任务的 completer（及其 completer，依此类推），如果存在 completer 且尚未完成。类似地，取消内部 CountedCompleter 仅对该 completer 有局部影响，因此通常不是很有用。
 *
 * <p><b>示例用法。</b>
 *
 * <p><b>并行递归分解。</b> CountedCompleters 可以以类似于 {@link RecursiveAction} 常用的方式排列成树，尽管设置它们的构造通常会有所不同。在这里，每个任务的 completer 是计算树中的父任务。即使将工作分解为单个任务（叶任务），基于树的技术通常也优于直接分叉叶任务，因为它们减少了线程间通信并提高了负载平衡。在递归情况下，完成两个子任务中较晚完成的那个会触发其父任务的完成（因为没有结果组合，所以默认的 no-op 实现方法 {@code onCompletion} 未被覆盖）。静态实用方法设置基础任务并调用它（这里，隐式使用 {@link ForkJoinPool#commonPool()}）。
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
 * 通过观察，在递归情况下，任务在分叉其右子任务后没有其他事情要做，因此可以直接调用其左子任务再返回。（这类似于尾递归消除。）此外，因为任务在执行其左子任务后返回（而不是继续调用 {@code tryComplete}），所以待处理计数设置为一：
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
 * 作为进一步的改进，注意到左子任务甚至不需要存在。不必创建新的左子任务，可以使用原始任务迭代，并为每个分叉添加一个待处理计数。此外，因为此树中的任务没有实现 {@link #onCompletion(CountedCompleter)} 方法，所以可以将 {@code tryComplete()} 替换为 {@link #propagateCompletion}。
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
 * <p><b>搜索。</b> CountedCompleters 的树可以搜索数据结构的不同部分中的值或属性，并在找到一个时在 {@link java.util.concurrent.atomic.AtomicReference AtomicReference} 中报告结果。其他任务可以轮询结果以避免不必要的工作。（您还可以 {@linkplain #cancel 取消} 其他任务，但通常更简单和高效的是让它们注意到结果已设置并跳过进一步的处理。）再次以数组为例，使用完全分区（实际上，叶任务几乎总是处理多个元素）：
 *
 * <pre> {@code
 * class Searcher<E> extends CountedCompleter<E> {
 *   final E[] array; final AtomicReference<E> result; final int lo, hi;
 *   Searcher(CountedCompleter<?> p, E[] array, AtomicReference<E> result, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.result = result; this.lo = lo; this.hi = hi;
 *   }
 *   public E getRawResult() { return result.get(); }
 *   public void compute() { // 类似于 ForEach 版本 3
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
 *   boolean matches(E e) { ... } // 如果找到则返回 true
 *
 *   public static <E> E search(E[] array) {
 *       return new Searcher<E>(null, array, new AtomicReference<E>(), 0, array.length).invoke();
 *   }
 * }}</pre>
 *
 * 在此示例中，以及在任务除了比较和设置公共结果外没有其他效果的其他示例中，尾部无条件调用 {@code tryComplete} 可以变为有条件（{@code if (result.get() == null) tryComplete();}），因为一旦根任务完成，就不需要进一步的账簿来管理完成。
 *
 * <p><b>记录子任务。</b> 需要访问多个子任务结果的 CountedCompleter 任务通常需要在方法 {@link #onCompletion(CountedCompleter)} 中访问这些结果。如下面的类所示（该类执行简化的 map-reduce，其中映射和归约都是类型 {@code E}），在分治设计中，一种方法是让每个子任务记录其兄弟任务，以便在方法 {@code onCompletion} 中访问。此技术适用于归约中组合左结果和右结果的顺序无关紧要的情况；有序归约需要显式的左/右标识。上述示例中的其他简化变体也可能适用。
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
 * 在这里，方法 {@code onCompletion} 采取了许多完成设计中常见的形式。此回调风格的方法在以下两种情况下之一被触发，每次任务的待处理计数为零或变为零：(1) 由任务本身触发，如果其待处理计数在调用 {@code tryComplete} 时为零，或 (2) 由其子任务在完成时递减待处理计数至零时触发。{@code caller} 参数区分这些情况。通常，当调用者是 {@code this} 时，不需要任何操作。否则，调用者参数（通常通过类型转换）可以提供要组合的值（和/或链接到其他值）。假设正确使用了待处理计数，方法 {@code onCompletion} 中的操作在任务及其子任务完成时发生（一次）。在此方法中访问此任务或已完成任务的字段时不需要额外的同步以确保线程安全性。
 *
 * <p><b>完成遍历。</b> 如果使用 {@code onCompletion} 处理完成不适用或不方便，可以使用方法 {@link #firstComplete} 和 {@link #nextComplete} 创建自定义遍历。例如，定义一个 MapReducer，仅在第三版 ForEach 示例的形式中分叉右子任务，完成必须沿未耗尽的子任务链接合作归约，可以如下实现：
 *
 * <pre> {@code
 * class MapReducer<E> extends CountedCompleter<E> { // version 2
 *   final E[] array; final MyMapper<E> mapper;
 *   final MyReducer<E> reducer; final int lo, hi;
 *   MapReducer<E> forks, next; // 记录子任务分叉的列表
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


                /** This task's completer, or null if none */
    final CountedCompleter<?> completer;
    /** The number of pending tasks until completion */
    volatile int pending;

    /**
     * 创建一个具有给定 completer 和初始 pending 计数的新 CountedCompleter。
     *
     * @param completer 该任务的 completer，如果没有则为 {@code null}
     * @param initialPendingCount 初始 pending 计数
     */
    protected CountedCompleter(CountedCompleter<?> completer,
                               int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    /**
     * 创建一个具有给定 completer 和初始 pending 计数为零的新 CountedCompleter。
     *
     * @param completer 该任务的 completer，如果没有则为 {@code null}
     */
    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
    }

    /**
     * 创建一个没有 completer 且初始 pending 计数为零的新 CountedCompleter。
     */
    protected CountedCompleter() {
        this.completer = null;
    }

    /**
     * 由该任务执行的主要计算。
     */
    public abstract void compute();

    /**
     * 当调用方法 {@link #tryComplete} 且 pending 计数为零时，或当调用无条件方法 {@link #complete} 时执行的操作。
     * 默认情况下，此方法不执行任何操作。可以通过检查给定的 caller 参数的身份来区分情况。
     * 如果不等于 {@code this}，则通常是一个可能包含结果（和/或其他结果的链接）的子任务，可以组合这些结果。
     *
     * @param caller 调用此方法的任务（可能是此任务本身）
     */
    public void onCompletion(CountedCompleter<?> caller) {
    }

    /**
     * 当调用方法 {@link #completeExceptionally(Throwable)} 或方法 {@link #compute} 抛出异常，
     * 且此任务尚未正常完成时执行的操作。进入此方法时，此任务 {@link ForkJoinTask#isCompletedAbnormally}。
     * 此方法的返回值控制进一步的传播：如果返回 {@code true} 且此任务有一个未完成的 completer，
     * 则该 completer 也会异常完成，异常与该 completer 相同。默认实现此方法不执行任何操作，仅返回 {@code true}。
     *
     * @param ex 异常
     * @param caller 调用此方法的任务（可能是此任务本身）
     * @return 如果此异常应传播到此任务的 completer（如果存在），则返回 {@code true}
     */
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        return true;
    }

    /**
     * 返回在该任务构造函数中建立的 completer，如果没有则返回 {@code null}。
     *
     * @return completer
     */
    public final CountedCompleter<?> getCompleter() {
        return completer;
    }

    /**
     * 返回当前的 pending 计数。
     *
     * @return 当前的 pending 计数
     */
    public final int getPendingCount() {
        return pending;
    }

    /**
     * 将 pending 计数设置为给定值。
     *
     * @param count 计数
     */
    public final void setPendingCount(int count) {
        pending = count;
    }

    /**
     * 原子地将给定值添加到 pending 计数。
     *
     * @param delta 要添加的值
     */
    public final void addToPendingCount(int delta) {
        U.getAndAddInt(this, PENDING, delta);
    }

    /**
     * 如果当前 pending 计数等于给定的预期值，则原子地将 pending 计数设置为给定的计数。
     *
     * @param expected 预期值
     * @param count 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count);
    }

    /**
     * 如果 pending 计数不为零，则原子地递减它。
     *
     * @return 进入此方法时的初始（未递减）pending 计数
     */
    public final int decrementPendingCountUnlessZero() {
        int c;
        do {} while ((c = pending) != 0 &&
                     !U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }

    /**
     * 返回当前计算的根；即，如果此任务没有 completer，则返回此任务，否则返回其 completer 的根。
     *
     * @return 当前计算的根
     */
    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this, p;
        while ((p = a.completer) != null)
            a = p;
        return a;
    }

    /**
     * 如果 pending 计数不为零，则递减计数；否则调用 {@link #onCompletion(CountedCompleter)}
     * 并尝试完成此任务的 completer（如果存在），否则标记此任务为完成。
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
     * 与 {@link #tryComplete} 等效，但不在完成路径上调用 {@link #onCompletion(CountedCompleter)}：
     * 如果 pending 计数不为零，则递减计数；否则尝试完成此任务的 completer（如果存在），否则标记此任务为完成。
     * 在某些情况下，如果不需要为计算中的每个 completer 调用 {@code onCompletion}，则此方法可能有用。
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
     * 无论 pending 计数如何，调用 {@link #onCompletion(CountedCompleter)}，标记此任务为完成，
     * 并进一步触发此任务的 completer（如果存在）的 {@link #tryComplete}。给定的 rawResult 用作
     * {@link #setRawResult} 的参数，在调用 {@link #onCompletion(CountedCompleter)} 或标记此任务为完成之前；
     * 其值仅对重写 {@code setRawResult} 的类有意义。此方法不修改 pending 计数。
     *
     * <p>当强制完成多个子任务中的任何一个（而不是全部）的结果时，此方法可能有用。
     * 但是，在常见的（并且推荐的）情况下，如果 {@code setRawResult} 未被重写，则可以更简单地使用 {@code quietlyCompleteRoot();}。
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
     * 如果此任务的 pending 计数为零，则返回此任务；否则递减其 pending 计数并返回 {@code null}。
     * 此方法设计用于与 {@link #nextComplete} 一起在完成遍历循环中使用。
     *
     * @return 如果 pending 计数为零，则返回此任务，否则返回 {@code null}
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
     * 或者，如果 completer 的 pending 计数不为零，则递减该 pending 计数并返回 {@code null}。
     * 否则，返回 completer。此方法可以作为完成遍历循环的一部分，用于同质任务层次结构：
     *
     * <pre> {@code
     * for (CountedCompleter<?> c = firstComplete();
     *      c != null;
     *      c = c.nextComplete()) {
     *   // ... process c ...
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
     * 等效于 {@code getRoot().quietlyComplete()}。
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
     * 如果此任务未完成，则尝试处理最多给定数量的其他未处理任务，这些任务在此任务的完成路径上，如果已知存在这些任务。
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
     * 返回计算的结果。默认情况下返回 {@code null}，这适用于 {@code Void} 操作，但在其他情况下应重写，
     * 几乎总是返回一个字段或在完成时持有结果的字段的函数。
     *
     * @return 计算的结果
     */
    public T getRawResult() { return null; }

    /**
     * 结果承载的 CountedCompleters 可以选择使用此方法来帮助维护结果数据。默认情况下不执行任何操作。
     * 不推荐重写。但是，如果此方法被重写以更新现有对象或字段，则通常必须定义为线程安全的。
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
