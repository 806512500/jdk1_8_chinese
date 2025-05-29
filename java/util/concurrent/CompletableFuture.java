
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并如解释的那样发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个可以显式完成（设置其值和状态）的 {@link Future}，并且可以用作 {@link CompletionStage}，
 * 支持在其完成时触发的依赖函数和操作。
 *
 * <p>当两个或多个线程尝试
 * {@link #complete complete}、
 * {@link #completeExceptionally completeExceptionally} 或
 * {@link #cancel cancel}
 * 一个 CompletableFuture 时，只有其中一个成功。
 *
 * <p>除了这些和相关的方法直接操作状态和结果外，CompletableFuture 实现了接口 {@link CompletionStage}，具有以下策略： <ul>
 *
 * <li>为 <em>非异步</em> 方法提供的依赖完成操作可以由完成当前 CompletableFuture 的线程执行，也可以由任何其他调用完成方法的调用者执行。</li>
 *
 * <li>所有没有显式 Executor 参数的 <em>异步</em> 方法都使用 {@link ForkJoinPool#commonPool()}
 * （除非它不支持至少两个并行级别，在这种情况下，为每个任务创建一个新线程）执行。为了简化监控、调试和跟踪，所有生成的异步任务都是标记接口 {@link
 * AsynchronousCompletionTask} 的实例。</li>
 *
 * <li>所有 CompletionStage 方法都独立于其他公共方法实现，因此一个方法的行为不会受到子类中其他方法重写的影响。</li> </ul>
 *
 * <p>CompletableFuture 还实现了 {@link Future}，具有以下策略： <ul>
 *
 * <li>由于（与 {@link FutureTask} 不同）此类无法直接控制导致其完成的计算，
 * 取消被视为另一种形式的异常完成。方法 {@link #cancel cancel} 的效果与
 * {@code completeExceptionally(new CancellationException())} 相同。方法
 * {@link #isCompletedExceptionally} 可用于确定 CompletableFuture 是否以任何异常方式完成。</li>
 *
 * <li>在使用 CompletionException 异常完成的情况下，方法 {@link #get()} 和 {@link #get(long, TimeUnit)} 抛出一个
 * {@link ExecutionException}，其原因与相应的 CompletionException 中持有的相同。为了简化大多数上下文中的使用，此类还定义了方法 {@link #join()} 和
 * {@link #getNow}，在这些情况下直接抛出 CompletionException。</li> </ul>
 *
 * @author Doug Lea
 * @since 1.8
 */
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {

    /*
     * 概述：
     *
     * 一个 CompletableFuture 可能有依赖的完成动作，这些动作收集在一个链接栈中。它通过 CAS 更新结果字段来原子地完成，
     * 然后弹出并运行这些动作。这适用于正常与异常结果、同步与异步动作、二元触发器以及各种形式的完成。
     *
     * 字段 result（通过 CAS 设置）的非空性表示完成。使用 AltResult 来包装 null 作为结果，以及持有异常。
     * 使用单个字段使得完成的检测和触发变得简单。编码和解码虽然直接，但增加了捕获和关联异常的复杂性。
     * 一些简化依赖于（静态）NIL（用于包装 null 结果）是唯一具有 null 异常字段的 AltResult，因此我们通常不需要显式比较。
     * 即使某些泛型转换未检查（参见 SuppressWarnings 注解），它们的放置位置是适当的，即使进行了检查。
     *
     * 依赖动作由 Completion 对象表示，这些对象链接为 Treiber 栈，头部由字段 "stack" 指向。每个动作类型都有一个 Completion 类，
     * 分为单输入（UniCompletion）、双输入（BiCompletion）、投影（BiCompletions 使用两个输入中的一个（而非两个））、共享（CoCompletion，用于两个源中的第二个），
     * 零输入源动作，以及解除等待线程阻塞的 Signallers。类 Completion 继承自 ForkJoinTask 以支持异步执行
     * （通过利用其“标签”方法来维护声明，不增加空间开销）。它还声明为 Runnable 以允许与任意执行器一起使用。
     *
     * 每种 CompletionStage 的支持依赖于一个单独的类，以及两个 CompletableFuture 方法：
     *
     * * 一个与函数对应的 Completion 类，名称为 X，前缀为 "Uni"、"Bi" 或 "Or"。每个类包含源、动作和依赖的字段。它们非常相似，仅在底层功能形式上有所不同。
     *   我们这样做是为了用户在常见用法中不会遇到多层适配器。我们还包括不对应于用户方法的 "Relay" 类/方法；它们将结果从一个阶段复制到另一个阶段。
     *
     * * 布尔型 CompletableFuture 方法 x(...)（例如 uniApply）接受所有需要的参数来检查动作是否可触发，然后要么运行动作，要么通过执行其 Completion 参数来安排其异步执行。
     *   该方法返回 true 如果已知完成。
     *
     * * Completion 方法 tryFire(int mode) 调用与其关联的 x 方法，并传递其持有的参数，成功时清理。模式参数允许 tryFire 被调用两次（SYNC，然后 ASYNC）；
     *   第一次用于在安排执行时筛选和捕获异常，第二次当从任务中调用时。少数类不用于异步，因此形式略有不同。claim() 回调如果已被其他线程声明，则抑制函数调用。
     *
     * * CompletableFuture 方法 xStage(...) 由 CompletableFuture 的公共阶段方法 x 调用。它筛选用户参数并调用和/或创建阶段对象。如果不是异步且 x 已完成，
     *   则立即运行动作。否则创建一个 Completion c，推入 x 的栈（除非已完成），并通过 c.tryFire 启动或触发。这还涵盖了 x 在推入时可能完成的竞争条件。
     *   具有两个输入的类（例如 BiApply）在推入动作时处理两个输入的竞争条件。第二个完成是一个指向第一个的 CoCompletion，共享以确保最多一个执行动作。
     *   多参数方法 allOf 和 anyOf 通过成对形成完成树来实现这一点。
     *
     * 注意方法的泛型类型参数根据 "this" 是源、依赖还是完成而变化。
     *
     * 方法 postComplete 在完成时调用，除非目标保证不可见（即，尚未返回或链接）。多个线程可以调用 postComplete，该方法原子地弹出每个依赖动作，
     * 并尝试通过方法 tryFire 触发它，模式为 NESTED。触发可以递归传播，因此 NESTED 模式返回其完成的依赖（如果存在）以供其调用者进一步处理（参见方法 postFire）。
     *
     * 阻塞方法 get() 和 join() 依赖于唤醒等待线程的 Signaller Completions。其机制类似于 FutureTask、Phaser 和 SynchronousQueue 中使用的 Treiber 栈等待节点。
     * 有关算法细节，请参阅它们的内部文档。
     *
     * 没有预防措施，CompletableFutures 会因 Completion 链的积累而容易产生垃圾。因此，我们尽可能快地将字段置为 null（特别是方法 Completion.detach）。
     * 无论如何需要的筛选检查无害地忽略了可能在与线程将字段置为 null 的竞争中获得的 null 参数。我们还尝试从可能永远不会弹出的栈中解除已触发的 Completions（参见方法 postFire）。
     * Completion 字段不需要声明为 final 或 volatile，因为它们只有在安全发布后才对其他线程可见。
     */


                volatile Object result;       // 结果或封装的 AltResult
    volatile Completion stack;    // 依赖操作的 Treiber 栈的栈顶

    final boolean internalComplete(Object r) { // 从 null CAS 到 r
        return UNSAFE.compareAndSwapObject(this, RESULT, null, r);
    }

    final boolean casStack(Completion cmp, Completion val) {
        return UNSAFE.compareAndSwapObject(this, STACK, cmp, val);
    }

    /** 如果成功将 c 推入栈中，则返回 true。 */
    final boolean tryPushStack(Completion c) {
        Completion h = stack;
        lazySetNext(c, h);
        return UNSAFE.compareAndSwapObject(this, STACK, h, c);
    }

    /** 无条件地将 c 推入栈中，必要时重试。 */
    final void pushStack(Completion c) {
        do {} while (!tryPushStack(c));
    }

    /* ------------- 结果的编码和解码 -------------- */

    static final class AltResult { // 参见上文
        final Throwable ex;        // 仅在 NIL 时为 null
        AltResult(Throwable x) { this.ex = x; }
    }

    /** null 值的编码。 */
    static final AltResult NIL = new AltResult(null);

    /** 以 null 值完成，除非已经完成。 */
    final boolean completeNull() {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           NIL);
    }

    /** 返回给定非异常值的编码。 */
    final Object encodeValue(T t) {
        return (t == null) ? NIL : t;
    }

    /** 以非异常结果完成，除非已经完成。 */
    final boolean completeValue(T t) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           (t == null) ? NIL : t);
    }

    /**
     * 返回给定（非 null）异常的编码，如果它已经是一个 CompletionException，则直接返回。
     */
    static AltResult encodeThrowable(Throwable x) {
        return new AltResult((x instanceof CompletionException) ? x :
                             new CompletionException(x));
    }

    /** 以异常结果完成，除非已经完成。 */
    final boolean completeThrowable(Throwable x) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           encodeThrowable(x));
    }

    /**
     * 返回给定（非 null）异常的编码，如果它已经是一个 CompletionException，则直接返回。可能返回给定的 Object r（必须是源未来的结果），如果它是等效的，即这是一个现有 CompletionException 的简单传递。
     */
    static Object encodeThrowable(Throwable x, Object r) {
        if (!(x instanceof CompletionException))
            x = new CompletionException(x);
        else if (r instanceof AltResult && x == ((AltResult)r).ex)
            return r;
        return new AltResult(x);
    }

    /**
     * 以给定（非 null）异常结果完成，除非它已经是一个 CompletionException，除非已经完成。可能以给定的 Object r（必须是源未来的结 果）完成，如果它是等效的，即这是现有 CompletionException 的简单传播。
     */
    final boolean completeThrowable(Throwable x, Object r) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           encodeThrowable(x, r));
    }

    /**
     * 返回给定参数的编码：如果异常不为 null，则编码为 AltResult。否则使用给定的值，如果为 null，则封装为 NIL。
     */
    Object encodeOutcome(T t, Throwable x) {
        return (x == null) ? (t == null) ? NIL : t : encodeThrowable(x);
    }

    /**
     * 返回复制结果的编码；如果异常，则重新封装为 CompletionException，否则返回参数。
     */
    static Object encodeRelay(Object r) {
        Throwable x;
        return (((r instanceof AltResult) &&
                 (x = ((AltResult)r).ex) != null &&
                 !(x instanceof CompletionException)) ?
                new AltResult(new CompletionException(x)) : r);
    }

    /**
     * 以 r 或 r 的副本完成，除非已经完成。如果异常，r 首先被强制转换为 CompletionException。
     */
    final boolean completeRelay(Object r) {
        return UNSAFE.compareAndSwapObject(this, RESULT, null,
                                           encodeRelay(r));
    }

    /**
     * 使用 Future.get 的约定报告结果。
     */
    private static <T> T reportGet(Object r)
        throws InterruptedException, ExecutionException {
        if (r == null) // 按照下面的约定，null 表示中断
            throw new InterruptedException();
        if (r instanceof AltResult) {
            Throwable x, cause;
            if ((x = ((AltResult)r).ex) == null)
                return null;
            if (x instanceof CancellationException)
                throw (CancellationException)x;
            if ((x instanceof CompletionException) &&
                (cause = x.getCause()) != null)
                x = cause;
            throw new ExecutionException(x);
        }
        @SuppressWarnings("unchecked") T t = (T) r;
        return t;
    }

    /**
     * 解码结果以返回结果或抛出未检查的异常。
     */
    private static <T> T reportJoin(Object r) {
        if (r instanceof AltResult) {
            Throwable x;
            if ((x = ((AltResult)r).ex) == null)
                return null;
            if (x instanceof CancellationException)
                throw (CancellationException)x;
            if (x instanceof CompletionException)
                throw (CompletionException)x;
            throw new CompletionException(x);
        }
        @SuppressWarnings("unchecked") T t = (T) r;
        return t;
    }

    /* ------------- 异步任务预备 -------------- */

    /**
     * 一个标记接口，用于标识由 {@code async} 方法生成的异步任务。这可能对监控、调试和跟踪异步活动有用。
     *
     * @since 1.8
     */
    public static interface AsynchronousCompletionTask {
    }


                private static final boolean useCommonPool =
        (ForkJoinPool.getCommonPoolParallelism() > 1);

    /**
     * 默认执行器 —— 如果 ForkJoinPool.commonPool() 不能支持并行，则使用 ThreadPerTaskExecutor。
     */
    private static final Executor asyncPool = useCommonPool ?
        ForkJoinPool.commonPool() : new ThreadPerTaskExecutor();

    /** 如果 ForkJoinPool.commonPool() 不能支持并行，则作为回退方案。 */
    static final class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) { new Thread(r).start(); }
    }

    /**
     * 检查用户提供的执行器参数是否为空，并在并行性禁用时将 commonPool 的使用转换为 asyncPool。
     */
    static Executor screenExecutor(Executor e) {
        if (!useCommonPool && e == ForkJoinPool.commonPool())
            return asyncPool;
        if (e == null) throw new NullPointerException();
        return e;
    }

    // Completion.tryFire 的模式。符号性很重要。
    static final int SYNC   =  0;
    static final int ASYNC  =  1;
    static final int NESTED = -1;

    /**
     * 在等待获取时自旋前。
     * 在单处理器上不需要自旋。
     *
     * 调用 Runtime.availableProcessors 是昂贵的，此处缓存该值。
     * 不幸的是，这依赖于首次初始化时可用的 CPU 数量。这会影响多处理器系统在启动时仅报告一个 CPU 可用，初始化 SPINS 为 0，然后使更多 CPU 在线的情况。这会导致性能损失，因为自旋次数会比其他情况下少。
     */
    private static final int SPINS = (Runtime.getRuntime().availableProcessors() > 1 ?
                                      1 << 8 : 0);

    /* ------------- 基础 Completion 类和操作 -------------- */

    @SuppressWarnings("serial")
    abstract static class Completion extends ForkJoinTask<Void>
        implements Runnable, AsynchronousCompletionTask {
        volatile Completion next;      // Treiber 栈链接

        /**
         * 如果触发，则执行完成操作，返回一个可能需要传播的依赖项，如果存在的话。
         *
         * @param mode SYNC, ASYNC, 或 NESTED
         */
        abstract CompletableFuture<?> tryFire(int mode);

        /** 如果可能仍然可触发，则返回 true。用于 cleanStack。 */
        abstract boolean isLive();

        public final void run()                { tryFire(ASYNC); }
        public final boolean exec()            { tryFire(ASYNC); return true; }
        public final Void getRawResult()       { return null; }
        public final void setRawResult(Void v) {}
    }

    static void lazySetNext(Completion c, Completion next) {
        UNSAFE.putOrderedObject(c, NEXT, next);
    }

    /**
     * 弹出并尝试触发所有可到达的依赖项。仅在已知完成时调用。
     */
    final void postComplete() {
        /*
         * 在每一步中，变量 f 持有当前要弹出和运行的依赖项。
         * 它仅沿一条路径扩展，将其他路径推入以避免无界递归。
         */
        CompletableFuture<?> f = this; Completion h;
        while ((h = f.stack) != null ||
               (f != this && (h = (f = this).stack) != null)) {
            CompletableFuture<?> d; Completion t;
            if (f.casStack(h, t = h.next)) {
                if (t != null) {
                    if (f != this) {
                        pushStack(h);
                        continue;
                    }
                    h.next = null;    // 分离
                }
                f = (d = h.tryFire(NESTED)) == null ? this : d;
            }
        }
    }

    /** 遍历栈并取消链接已死的 Completions。*/
    final void cleanStack() {
        for (Completion p = null, q = stack; q != null;) {
            Completion s = q.next;
            if (q.isLive()) {
                p = q;
                q = s;
            }
            else if (p == null) {
                casStack(q, s);
                q = stack;
            }
            else {
                p.next = s;
                if (p.isLive())
                    q = s;
                else {
                    p = null;  // 重新开始
                    q = stack;
                }
            }
        }
    }

    /* ------------- 单输入 Completions -------------- */

    /** 一个具有源、依赖项和执行器的 Completion。 */
    @SuppressWarnings("serial")
    abstract static class UniCompletion<T,V> extends Completion {
        Executor executor;                 // 要使用的执行器（如果为 null，则表示没有）
        CompletableFuture<V> dep;          // 要完成的依赖项
        CompletableFuture<T> src;          // 操作的源

        UniCompletion(Executor executor, CompletableFuture<V> dep,
                      CompletableFuture<T> src) {
            this.executor = executor; this.dep = dep; this.src = src;
        }

        /**
         * 如果可以运行操作，则返回 true。仅在已知可触发时调用。使用 FJ 标记位确保只有一个线程声明所有权。如果为异步，则启动任务 —— 后续调用 tryFire 将运行操作。
         */
        final boolean claim() {
            Executor e = executor;
            if (compareAndSetForkJoinTaskTag((short)0, (short)1)) {
                if (e == null)
                    return true;
                executor = null; // 禁用
                e.execute(this);
            }
            return false;
        }

        final boolean isLive() { return dep != null; }
    }

    /** 如果未完成，则推送给定的 Completion（如果存在）。 */
    final void push(UniCompletion<?,?> c) {
        if (c != null) {
            while (result == null && !tryPushStack(c))
                lazySetNext(c, null); // 在失败时清除
        }
    }

    /**
     * 成功触发 UniCompletion tryFire 后的后处理。尝试清理源 a 的栈，然后根据模式运行 postComplete 或返回 this 给调用者。
     */
    final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
        if (a != null && a.stack != null) {
            if (mode < 0 || a.result == null)
                a.cleanStack();
            else
                a.postComplete();
        }
        if (result != null && stack != null) {
            if (mode < 0)
                return this;
            else
                postComplete();
        }
        return null;
    }


                @SuppressWarnings("serial")
    static final class UniApply<T,V> extends UniCompletion<T,V> {
        Function<? super T,? extends V> fn;
        UniApply(Executor executor, CompletableFuture<V> dep,
                 CompletableFuture<T> src,
                 Function<? super T,? extends V> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniApply(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniApply(CompletableFuture<S> a,
                               Function<? super S,? extends T> f,
                               UniApply<S,T> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") S s = (S) r;
                completeValue(f.apply(s));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniApplyStage(
        Executor e, Function<? super T,? extends V> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<V> d =  new CompletableFuture<V>();
        if (e != null || !d.uniApply(this, f, null)) {
            UniApply<T,V> c = new UniApply<T,V>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniAccept<T> extends UniCompletion<T,Void> {
        Consumer<? super T> fn;
        UniAccept(Executor executor, CompletableFuture<Void> dep,
                  CompletableFuture<T> src, Consumer<? super T> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniAccept(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniAccept(CompletableFuture<S> a,
                                Consumer<? super S> f, UniAccept<S> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") S s = (S) r;
                f.accept(s);
                completeNull();
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<Void> uniAcceptStage(Executor e,
                                                   Consumer<? super T> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.uniAccept(this, f, null)) {
            UniAccept<T> c = new UniAccept<T>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniRun<T> extends UniCompletion<T,Void> {
        Runnable fn;
        UniRun(Executor executor, CompletableFuture<Void> dep,
               CompletableFuture<T> src, Runnable fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniRun(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniRun(CompletableFuture<?> a, Runnable f, UniRun<?> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            else
                try {
                    if (c != null && !c.claim())
                        return false;
                    f.run();
                    completeNull();
                } catch (Throwable ex) {
                    completeThrowable(ex);
                }
        }
        return true;
    }

    private CompletableFuture<Void> uniRunStage(Executor e, Runnable f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.uniRun(this, f, null)) {
            UniRun<T> c = new UniRun<T>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniWhenComplete<T> extends UniCompletion<T,T> {
        BiConsumer<? super T, ? super Throwable> fn;
        UniWhenComplete(Executor executor, CompletableFuture<T> dep,
                        CompletableFuture<T> src,
                        BiConsumer<? super T, ? super Throwable> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniWhenComplete(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }


                final boolean uniWhenComplete(CompletableFuture<T> a,
                                  BiConsumer<? super T,? super Throwable> f,
                                  UniWhenComplete<T> c) {
        Object r; T t; Throwable x = null;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    x = ((AltResult)r).ex;
                    t = null;
                } else {
                    @SuppressWarnings("unchecked") T tr = (T) r;
                    t = tr;
                }
                f.accept(t, x);
                if (x == null) {
                    internalComplete(r);
                    return true;
                }
            } catch (Throwable ex) {
                if (x == null)
                    x = ex;
            }
            completeThrowable(x, r);
        }
        return true;
    }

    private CompletableFuture<T> uniWhenCompleteStage(
        Executor e, BiConsumer<? super T, ? super Throwable> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<T> d = new CompletableFuture<T>();
        if (e != null || !d.uniWhenComplete(this, f, null)) {
            UniWhenComplete<T> c = new UniWhenComplete<T>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniHandle<T,V> extends UniCompletion<T,V> {
        BiFunction<? super T, Throwable, ? extends V> fn;
        UniHandle(Executor executor, CompletableFuture<V> dep,
                  CompletableFuture<T> src,
                  BiFunction<? super T, Throwable, ? extends V> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniHandle(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniHandle(CompletableFuture<S> a,
                                BiFunction<? super S, Throwable, ? extends T> f,
                                UniHandle<S,T> c) {
        Object r; S s; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    x = ((AltResult)r).ex;
                    s = null;
                } else {
                    x = null;
                    @SuppressWarnings("unchecked") S ss = (S) r;
                    s = ss;
                }
                completeValue(f.apply(s, x));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniHandleStage(
        Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        if (e != null || !d.uniHandle(this, f, null)) {
            UniHandle<T,V> c = new UniHandle<T,V>(e, d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniExceptionally<T> extends UniCompletion<T,T> {
        Function<? super Throwable, ? extends T> fn;
        UniExceptionally(CompletableFuture<T> dep, CompletableFuture<T> src,
                         Function<? super Throwable, ? extends T> fn) {
            super(null, dep, src); this.fn = fn;
        }
        final CompletableFuture<T> tryFire(int mode) { // never ASYNC
            // assert mode != ASYNC;
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null || !d.uniExceptionally(a = src, fn, this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniExceptionally(CompletableFuture<T> a,
                                   Function<? super Throwable, ? extends T> f,
                                   UniExceptionally<T> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        if (result == null) {
            try {
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null) {
                    if (c != null && !c.claim())
                        return false;
                    completeValue(f.apply(x));
                } else
                    internalComplete(r);
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<T> uniExceptionallyStage(
        Function<Throwable, ? extends T> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<T> d = new CompletableFuture<T>();
        if (!d.uniExceptionally(this, f, null)) {
            UniExceptionally<T> c = new UniExceptionally<T>(d, this, f);
            push(c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class UniRelay<T> extends UniCompletion<T,T> { // for Compose
        UniRelay(CompletableFuture<T> dep, CompletableFuture<T> src) {
            super(null, dep, src);
        }
        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d; CompletableFuture<T> a;
            if ((d = dep) == null || !d.uniRelay(a = src))
                return null;
            src = null; dep = null;
            return d.postFire(a, mode);
        }
    }

    final boolean uniRelay(CompletableFuture<T> a) {
        Object r;
        if (a == null || (r = a.result) == null)
            return false;
        if (result == null) // no need to claim
            completeRelay(r);
        return true;
    }


                @SuppressWarnings("serial")
    static final class UniCompose<T,V> extends UniCompletion<T,V> {
        Function<? super T, ? extends CompletionStage<V>> fn;
        UniCompose(Executor executor, CompletableFuture<V> dep,
                   CompletableFuture<T> src,
                   Function<? super T, ? extends CompletionStage<V>> fn) {
            super(executor, dep, src); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d; CompletableFuture<T> a;
            if ((d = dep) == null ||
                !d.uniCompose(a = src, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; fn = null;
            return d.postFire(a, mode);
        }
    }

    final <S> boolean uniCompose(
        CompletableFuture<S> a,
        Function<? super S, ? extends CompletionStage<T>> f,
        UniCompose<S,T> c) {
        Object r; Throwable x;
        if (a == null || (r = a.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            try {
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") S s = (S) r;
                CompletableFuture<T> g = f.apply(s).toCompletableFuture();
                if (g.result == null || !uniRelay(g)) {
                    UniRelay<T> copy = new UniRelay<T>(this, g);
                    g.push(copy);
                    copy.tryFire(SYNC);
                    if (result == null)
                        return false;
                }
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <V> CompletableFuture<V> uniComposeStage(
        Executor e, Function<? super T, ? extends CompletionStage<V>> f) {
        if (f == null) throw new NullPointerException();
        Object r; Throwable x;
        if (e == null && (r = result) != null) {
            // 尝试直接返回函数结果
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    return new CompletableFuture<V>(encodeThrowable(x, r));
                }
                r = null;
            }
            try {
                @SuppressWarnings("unchecked") T t = (T) r;
                CompletableFuture<V> g = f.apply(t).toCompletableFuture();
                Object s = g.result;
                if (s != null)
                    return new CompletableFuture<V>(encodeRelay(s));
                CompletableFuture<V> d = new CompletableFuture<V>();
                UniRelay<V> copy = new UniRelay<V>(d, g);
                g.push(copy);
                copy.tryFire(SYNC);
                return d;
            } catch (Throwable ex) {
                return new CompletableFuture<V>(encodeThrowable(ex));
            }
        }
        CompletableFuture<V> d = new CompletableFuture<V>();
        UniCompose<T,V> c = new UniCompose<T,V>(e, d, this, f);
        push(c);
        c.tryFire(SYNC);
        return d;
    }

    /* ------------- 两个输入的完成 -------------- */

    /** 一个具有两个源的动作的完成 */
    @SuppressWarnings("serial")
    abstract static class BiCompletion<T,U,V> extends UniCompletion<T,V> {
        CompletableFuture<U> snd; // 动作的第二个源
        BiCompletion(Executor executor, CompletableFuture<V> dep,
                     CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(executor, dep, src); this.snd = snd;
        }
    }

    /** 委托给 BiCompletion 的完成 */
    @SuppressWarnings("serial")
    static final class CoCompletion extends Completion {
        BiCompletion<?,?,?> base;
        CoCompletion(BiCompletion<?,?,?> base) { this.base = base; }
        final CompletableFuture<?> tryFire(int mode) {
            BiCompletion<?,?,?> c; CompletableFuture<?> d;
            if ((c = base) == null || (d = c.tryFire(mode)) == null)
                return null;
            base = null; // 分离
            return d;
        }
        final boolean isLive() {
            BiCompletion<?,?,?> c;
            return (c = base) != null && c.dep != null;
        }
    }

    /** 如果两者都未完成，则将完成推送到 this 和 b。 */
    final void bipush(CompletableFuture<?> b, BiCompletion<?,?,?> c) {
        if (c != null) {
            Object r;
            while ((r = result) == null && !tryPushStack(c))
                lazySetNext(c, null); // 失败时清除
            if (b != null && b != this && b.result == null) {
                Completion q = (r != null) ? c : new CoCompletion(c);
                while (b.result == null && !b.tryPushStack(q))
                    lazySetNext(q, null); // 失败时清除
            }
        }
    }

    /** 成功的 BiCompletion tryFire 之后的后处理。 */
    final CompletableFuture<T> postFire(CompletableFuture<?> a,
                                        CompletableFuture<?> b, int mode) {
        if (b != null && b.stack != null) { // 清理第二个源
            if (mode < 0 || b.result == null)
                b.cleanStack();
            else
                b.postComplete();
        }
        return postFire(a, mode);
    }

    @SuppressWarnings("serial")
    static final class BiApply<T,U,V> extends BiCompletion<T,U,V> {
        BiFunction<? super T,? super U,? extends V> fn;
        BiApply(Executor executor, CompletableFuture<V> dep,
                CompletableFuture<T> src, CompletableFuture<U> snd,
                BiFunction<? super T,? super U,? extends V> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biApply(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }


                final <R,S> boolean biApply(CompletableFuture<R> a,
                                CompletableFuture<S> b,
                                BiFunction<? super R,? super S,? extends T> f,
                                BiApply<R,S,T> c) {
        Object r, s; Throwable x;
        // 检查输入参数是否为空
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            // 检查 a 的结果是否为 AltResult 类型
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            // 检查 b 的结果是否为 AltResult 类型
            if (s instanceof AltResult) {
                if ((x = ((AltResult)s).ex) != null) {
                    completeThrowable(x, s);
                    break tryComplete;
                }
                s = null;
            }
            try {
                // 如果 c 不为空且无法声明，则返回 false
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") R rr = (R) r;
                @SuppressWarnings("unchecked") S ss = (S) s;
                // 应用函数 f 并完成值
                completeValue(f.apply(rr, ss));
            } catch (Throwable ex) {
                // 捕获异常并完成异常
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U,V> CompletableFuture<V> biApplyStage(
        Executor e, CompletionStage<U> o,
        BiFunction<? super T,? super U,? extends V> f) {
        CompletableFuture<U> b;
        // 检查函数 f 和 o 是否为空
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        // 如果执行器不为空或 d 无法应用，则创建 BiApply 并尝试执行
        if (e != null || !d.biApply(this, b, f, null)) {
            BiApply<T,U,V> c = new BiApply<T,U,V>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiAccept<T,U> extends BiCompletion<T,U,Void> {
        BiConsumer<? super T,? super U> fn;
        // 构造函数
        BiAccept(Executor executor, CompletableFuture<Void> dep,
                 CompletableFuture<T> src, CompletableFuture<U> snd,
                 BiConsumer<? super T,? super U> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        // 尝试执行
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biAccept(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S> boolean biAccept(CompletableFuture<R> a,
                                 CompletableFuture<S> b,
                                 BiConsumer<? super R,? super S> f,
                                 BiAccept<R,S> c) {
        Object r, s; Throwable x;
        // 检查输入参数是否为空
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        tryComplete: if (result == null) {
            // 检查 a 的结果是否为 AltResult 类型
            if (r instanceof AltResult) {
                if ((x = ((AltResult)r).ex) != null) {
                    completeThrowable(x, r);
                    break tryComplete;
                }
                r = null;
            }
            // 检查 b 的结果是否为 AltResult 类型
            if (s instanceof AltResult) {
                if ((x = ((AltResult)s).ex) != null) {
                    completeThrowable(x, s);
                    break tryComplete;
                }
                s = null;
            }
            try {
                // 如果 c 不为空且无法声明，则返回 false
                if (c != null && !c.claim())
                    return false;
                @SuppressWarnings("unchecked") R rr = (R) r;
                @SuppressWarnings("unchecked") S ss = (S) s;
                // 应用消费者 f
                f.accept(rr, ss);
                // 完成空值
                completeNull();
            } catch (Throwable ex) {
                // 捕获异常并完成异常
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U> CompletableFuture<Void> biAcceptStage(
        Executor e, CompletionStage<U> o,
        BiConsumer<? super T,? super U> f) {
        CompletableFuture<U> b;
        // 检查函数 f 和 o 是否为空
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        // 如果执行器不为空或 d 无法接受，则创建 BiAccept 并尝试执行
        if (e != null || !d.biAccept(this, b, f, null)) {
            BiAccept<T,U> c = new BiAccept<T,U>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiRun<T,U> extends BiCompletion<T,U,Void> {
        Runnable fn;
        // 构造函数
        BiRun(Executor executor, CompletableFuture<Void> dep,
              CompletableFuture<T> src,
              CompletableFuture<U> snd,
              Runnable fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        // 尝试执行
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.biRun(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean biRun(CompletableFuture<?> a, CompletableFuture<?> b,
                        Runnable f, BiRun<?,?> c) {
        Object r, s; Throwable x;
        // 检查输入参数是否为空
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null || f == null)
            return false;
        if (result == null) {
            // 检查 a 的结果是否为 AltResult 类型
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            // 检查 b 的结果是否为 AltResult 类型
            else if (s instanceof AltResult && (x = ((AltResult)s).ex) != null)
                completeThrowable(x, s);
            else
                try {
                    // 如果 c 不为空且无法声明，则返回 false
                    if (c != null && !c.claim())
                        return false;
                    // 运行任务
                    f.run();
                    // 完成空值
                    completeNull();
                } catch (Throwable ex) {
                    // 捕获异常并完成异常
                    completeThrowable(ex);
                }
        }
        return true;
    }


                private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o,
                                               Runnable f) {
        CompletableFuture<?> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.biRun(this, b, f, null)) {
            BiRun<T,?> c = new BiRun<>(e, d, this, b, f);
            bipush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class BiRelay<T,U> extends BiCompletion<T,U,Void> { // for And
        BiRelay(CompletableFuture<Void> dep,
                CompletableFuture<T> src,
                CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null || !d.biRelay(a = src, b = snd))
                return null;
            src = null; snd = null; dep = null;
            return d.postFire(a, b, mode);
        }
    }

    boolean biRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        Object r, s; Throwable x;
        if (a == null || (r = a.result) == null ||
            b == null || (s = b.result) == null)
            return false;
        if (result == null) {
            if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                completeThrowable(x, r);
            else if (s instanceof AltResult && (x = ((AltResult)s).ex) != null)
                completeThrowable(x, s);
            else
                completeNull();
        }
        return true;
    }

    /** Recursively constructs a tree of completions. */
    static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs,
                                           int lo, int hi) {
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (lo > hi) // empty
            d.result = NIL;
        else {
            CompletableFuture<?> a, b;
            int mid = (lo + hi) >>> 1;
            if ((a = (lo == mid ? cfs[lo] :
                      andTree(cfs, lo, mid))) == null ||
                (b = (lo == hi ? a : (hi == mid+1) ? cfs[hi] :
                      andTree(cfs, mid+1, hi)))  == null)
                throw new NullPointerException();
            if (!d.biRelay(a, b)) {
                BiRelay<?,?> c = new BiRelay<>(d, a, b);
                a.bipush(b, c);
                c.tryFire(SYNC);
            }
        }
        return d;
    }

    /* ------------- Projected (Ored) BiCompletions -------------- */

    /** Pushes completion to this and b unless either done. */
    final void orpush(CompletableFuture<?> b, BiCompletion<?,?,?> c) {
        if (c != null) {
            while ((b == null || b.result == null) && result == null) {
                if (tryPushStack(c)) {
                    if (b != null && b != this && b.result == null) {
                        Completion q = new CoCompletion(c);
                        while (result == null && b.result == null &&
                               !b.tryPushStack(q))
                            lazySetNext(q, null); // clear on failure
                    }
                    break;
                }
                lazySetNext(c, null); // clear on failure
            }
        }
    }

    @SuppressWarnings("serial")
    static final class OrApply<T,U extends T,V> extends BiCompletion<T,U,V> {
        Function<? super T,? extends V> fn;
        OrApply(Executor executor, CompletableFuture<V> dep,
                CompletableFuture<T> src,
                CompletableFuture<U> snd,
                Function<? super T,? extends V> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orApply(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final <R,S extends R> boolean orApply(CompletableFuture<R> a,
                                          CompletableFuture<S> b,
                                          Function<? super R, ? extends T> f,
                                          OrApply<R,S,T> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        tryComplete: if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    if ((x = ((AltResult)r).ex) != null) {
                        completeThrowable(x, r);
                        break tryComplete;
                    }
                    r = null;
                }
                @SuppressWarnings("unchecked") R rr = (R) r;
                completeValue(f.apply(rr));
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U extends T,V> CompletableFuture<V> orApplyStage(
        Executor e, CompletionStage<U> o,
        Function<? super T, ? extends V> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<V> d = new CompletableFuture<V>();
        if (e != null || !d.orApply(this, b, f, null)) {
            OrApply<T,U,V> c = new OrApply<T,U,V>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrAccept<T,U extends T> extends BiCompletion<T,U,Void> {
        Consumer<? super T> fn;
        OrAccept(Executor executor, CompletableFuture<Void> dep,
                 CompletableFuture<T> src,
                 CompletableFuture<U> snd,
                 Consumer<? super T> fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orAccept(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }


                final <R,S extends R> boolean orAccept(CompletableFuture<R> a,
                                           CompletableFuture<S> b,
                                           Consumer<? super R> f,
                                           OrAccept<R,S> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        tryComplete: if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult) {
                    if ((x = ((AltResult)r).ex) != null) {
                        completeThrowable(x, r);
                        break tryComplete;
                    }
                    r = null;
                }
                @SuppressWarnings("unchecked") R rr = (R) r;
                f.accept(rr);
                completeNull();
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(
        Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        CompletableFuture<U> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.orAccept(this, b, f, null)) {
            OrAccept<T,U> c = new OrAccept<T,U>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrRun<T,U> extends BiCompletion<T,U,Void> {
        Runnable fn;
        OrRun(Executor executor, CompletableFuture<Void> dep,
              CompletableFuture<T> src,
              CompletableFuture<U> snd,
              Runnable fn) {
            super(executor, dep, src, snd); this.fn = fn;
        }
        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null ||
                !d.orRun(a = src, b = snd, fn, mode > 0 ? null : this))
                return null;
            dep = null; src = null; snd = null; fn = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean orRun(CompletableFuture<?> a, CompletableFuture<?> b,
                        Runnable f, OrRun<?,?> c) {
        Object r; Throwable x;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null) || f == null)
            return false;
        if (result == null) {
            try {
                if (c != null && !c.claim())
                    return false;
                if (r instanceof AltResult && (x = ((AltResult)r).ex) != null)
                    completeThrowable(x, r);
                else {
                    f.run();
                    completeNull();
                }
            } catch (Throwable ex) {
                completeThrowable(ex);
            }
        }
        return true;
    }

    private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o,
                                               Runnable f) {
        CompletableFuture<?> b;
        if (f == null || (b = o.toCompletableFuture()) == null)
            throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        if (e != null || !d.orRun(this, b, f, null)) {
            OrRun<T,?> c = new OrRun<>(e, d, this, b, f);
            orpush(b, c);
            c.tryFire(SYNC);
        }
        return d;
    }

    @SuppressWarnings("serial")
    static final class OrRelay<T,U> extends BiCompletion<T,U,Object> { // for Or
        OrRelay(CompletableFuture<Object> dep, CompletableFuture<T> src,
                CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }
        final CompletableFuture<Object> tryFire(int mode) {
            CompletableFuture<Object> d;
            CompletableFuture<T> a;
            CompletableFuture<U> b;
            if ((d = dep) == null || !d.orRelay(a = src, b = snd))
                return null;
            src = null; snd = null; dep = null;
            return d.postFire(a, b, mode);
        }
    }

    final boolean orRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        Object r;
        if (a == null || b == null ||
            ((r = a.result) == null && (r = b.result) == null))
            return false;
        if (result == null)
            completeRelay(r);
        return true;
    }

    /** 递归构建完成树。 */
    static CompletableFuture<Object> orTree(CompletableFuture<?>[] cfs,
                                            int lo, int hi) {
        CompletableFuture<Object> d = new CompletableFuture<Object>();
        if (lo <= hi) {
            CompletableFuture<?> a, b;
            int mid = (lo + hi) >>> 1;
            if ((a = (lo == mid ? cfs[lo] :
                      orTree(cfs, lo, mid))) == null ||
                (b = (lo == hi ? a : (hi == mid+1) ? cfs[hi] :
                      orTree(cfs, mid+1, hi)))  == null)
                throw new NullPointerException();
            if (!d.orRelay(a, b)) {
                OrRelay<?,?> c = new OrRelay<>(d, a, b);
                a.orpush(b, c);
                c.tryFire(SYNC);
            }
        }
        return d;
    }

    /* ------------- 无输入的异步形式 -------------- */

    @SuppressWarnings("serial")
    static final class AsyncSupply<T> extends ForkJoinTask<Void>
            implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<T> dep; Supplier<T> fn;
        AsyncSupply(CompletableFuture<T> dep, Supplier<T> fn) {
            this.dep = dep; this.fn = fn;
        }

        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) {}
        public final boolean exec() { run(); return true; }

        public void run() {
            CompletableFuture<T> d; Supplier<T> f;
            if ((d = dep) != null && (f = fn) != null) {
                dep = null; fn = null;
                if (d.result == null) {
                    try {
                        d.completeValue(f.get());
                    } catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
                d.postComplete();
            }
        }
    }


                static <U> CompletableFuture<U> asyncSupplyStage(Executor e,
                                                     Supplier<U> f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<U> d = new CompletableFuture<U>();
        e.execute(new AsyncSupply<U>(d, f));
        return d;
    }

    @SuppressWarnings("serial")
    static final class AsyncRun extends ForkJoinTask<Void>
            implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<Void> dep; Runnable fn;
        AsyncRun(CompletableFuture<Void> dep, Runnable fn) {
            this.dep = dep; this.fn = fn;
        }

        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) {}
        public final boolean exec() { run(); return true; }

        public void run() {
            CompletableFuture<Void> d; Runnable f;
            if ((d = dep) != null && (f = fn) != null) {
                dep = null; fn = null;
                if (d.result == null) {
                    try {
                        f.run();
                        d.completeNull();
                    } catch (Throwable ex) {
                        d.completeThrowable(ex);
                    }
                }
                d.postComplete();
            }
        }
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f == null) throw new NullPointerException();
        CompletableFuture<Void> d = new CompletableFuture<Void>();
        e.execute(new AsyncRun(d, f));
        return d;
    }

    /* ------------- Signallers -------------- */

    /**
     * 用于记录和释放等待线程的完成。此类实现了 ManagedBlocker 以避免在 ForkJoinPools 中阻塞操作堆积时发生饥饿。
     */
    @SuppressWarnings("serial")
    static final class Signaller extends Completion
        implements ForkJoinPool.ManagedBlocker {
        long nanos;                    // 如果是定时的，则为等待时间
        final long deadline;           // 如果是定时的，则为非零值
        volatile int interruptControl; // > 0: 可中断, < 0: 已中断
        volatile Thread thread;

        Signaller(boolean interruptible, long nanos, long deadline) {
            this.thread = Thread.currentThread();
            this.interruptControl = interruptible ? 1 : 0;
            this.nanos = nanos;
            this.deadline = deadline;
        }
        final CompletableFuture<?> tryFire(int ignore) {
            Thread w; // 无需原子性地声明
            if ((w = thread) != null) {
                thread = null;
                LockSupport.unpark(w);
            }
            return null;
        }
        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (Thread.interrupted()) {
                int i = interruptControl;
                interruptControl = -1;
                if (i > 0)
                    return true;
            }
            if (deadline != 0L &&
                (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                thread = null;
                return true;
            }
            return false;
        }
        public boolean block() {
            if (isReleasable())
                return true;
            else if (deadline == 0L)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
        final boolean isLive() { return thread != null; }
    }

    /**
     * 等待后返回原始结果，如果可中断且被中断，则返回 null。
     */
    private Object waitingGet(boolean interruptible) {
        Signaller q = null;
        boolean queued = false;
        int spins = -1;
        Object r;
        while ((r = result) == null) {
            if (spins < 0)
                spins = SPINS;
            else if (spins > 0) {
                if (ThreadLocalRandom.nextSecondarySeed() >= 0)
                    --spins;
            }
            else if (q == null)
                q = new Signaller(interruptible, 0L, 0L);
            else if (!queued)
                queued = tryPushStack(q);
            else if (interruptible && q.interruptControl < 0) {
                q.thread = null;
                cleanStack();
                return null;
            }
            else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ie) {
                    q.interruptControl = -1;
                }
            }
        }
        if (q != null) {
            q.thread = null;
            if (q.interruptControl < 0) {
                if (interruptible)
                    r = null; // 报告中断
                else
                    Thread.currentThread().interrupt();
            }
        }
        postComplete();
        return r;
    }

    /**
     * 等待后返回原始结果，如果被中断，则返回 null，如果超时，则抛出 TimeoutException。
     */
    private Object timedGet(long nanos) throws TimeoutException {
        if (Thread.interrupted())
            return null;
        if (nanos <= 0L)
            throw new TimeoutException();
        long d = System.nanoTime() + nanos;
        Signaller q = new Signaller(true, nanos, d == 0L ? 1L : d); // 避免 0
        boolean queued = false;
        Object r;
        // 故意不在此处自旋（如 waitingGet 所做），因为上面的 nanoTime() 调用类似于自旋。
        while ((r = result) == null) {
            if (!queued)
                queued = tryPushStack(q);
            else if (q.interruptControl < 0 || q.nanos <= 0L) {
                q.thread = null;
                cleanStack();
                if (q.interruptControl < 0)
                    return null;
                throw new TimeoutException();
            }
            else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ie) {
                    q.interruptControl = -1;
                }
            }
        }
        if (q.interruptControl < 0)
            r = null;
        q.thread = null;
        postComplete();
        return r;
    }

                /* ------------- 公共方法 -------------- */

    /**
     * 创建一个新的未完成的 CompletableFuture。
     */
    public CompletableFuture() {
    }

    /**
     * 使用给定的编码结果创建一个新的已完成的 CompletableFuture。
     */
    private CompletableFuture(Object r) {
        this.result = r;
    }

    /**
     * 返回一个新的 CompletableFuture，该 CompletableFuture 通过在 {@link ForkJoinPool#commonPool()} 中运行的任务异步完成，
     * 该任务通过调用给定的 Supplier 获取值。
     *
     * @param supplier 返回用于完成返回的 CompletableFuture 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return asyncSupplyStage(asyncPool, supplier);
    }

    /**
     * 返回一个新的 CompletableFuture，该 CompletableFuture 通过在给定执行器中运行的任务异步完成，
     * 该任务通过调用给定的 Supplier 获取值。
     *
     * @param supplier 返回用于完成返回的 CompletableFuture 的值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 函数的返回类型
     * @return 新的 CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,
                                                       Executor executor) {
        return asyncSupplyStage(screenExecutor(executor), supplier);
    }

    /**
     * 返回一个新的 CompletableFuture，该 CompletableFuture 通过在 {@link ForkJoinPool#commonPool()} 中运行的任务异步完成，
     * 该任务在运行给定的操作后完成。
     *
     * @param runnable 在完成返回的 CompletableFuture 之前运行的操作
     * @return 新的 CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return asyncRunStage(asyncPool, runnable);
    }

    /**
     * 返回一个新的 CompletableFuture，该 CompletableFuture 通过在给定执行器中运行的任务异步完成，
     * 该任务在运行给定的操作后完成。
     *
     * @param runnable 在完成返回的 CompletableFuture 之前运行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable,
                                                   Executor executor) {
        return asyncRunStage(screenExecutor(executor), runnable);
    }

    /**
     * 返回一个已经使用给定值完成的新的 CompletableFuture。
     *
     * @param value 值
     * @param <U> 值的类型
     * @return 完成的 CompletableFuture
     */
    public static <U> CompletableFuture<U> completedFuture(U value) {
        return new CompletableFuture<U>((value == null) ? NIL : value);
    }

    /**
     * 如果以任何方式完成（正常、异常或通过取消），则返回 {@code true}。
     *
     * @return 如果完成，则返回 {@code true}
     */
    public boolean isDone() {
        return result != null;
    }

    /**
     * 如果需要，等待此 Future 完成，然后返回其结果。
     *
     * @return 结果值
     * @throws CancellationException 如果此 Future 被取消
     * @throws ExecutionException 如果此 Future 异常完成
     * @throws InterruptedException 如果等待时当前线程被中断
     */
    public T get() throws InterruptedException, ExecutionException {
        Object r;
        return reportGet((r = result) == null ? waitingGet(true) : r);
    }

    /**
     * 如果需要，最多等待给定的时间让此 Future 完成，如果可用则返回其结果。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 结果值
     * @throws CancellationException 如果此 Future 被取消
     * @throws ExecutionException 如果此 Future 异常完成
     * @throws InterruptedException 如果等待时当前线程被中断
     * @throws TimeoutException 如果等待超时
     */
    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        Object r;
        long nanos = unit.toNanos(timeout);
        return reportGet((r = result) == null ? timedGet(nanos) : r);
    }

    /**
     * 当完成时返回结果值，如果异常完成则抛出（未检查的）异常。为了更好地符合常用的函数形式，
     * 如果此 CompletableFuture 的完成涉及的计算抛出了异常，此方法将抛出一个（未检查的）{@link CompletionException}，
     * 其底层异常作为其原因。
     *
     * @return 结果值
     * @throws CancellationException 如果计算被取消
     * @throws CompletionException 如果此 Future 异常完成或完成计算抛出了异常
     */
    public T join() {
        Object r;
        return reportJoin((r = result) == null ? waitingGet(false) : r);
    }

    /**
     * 如果完成，则返回结果值（或抛出遇到的任何异常），否则返回给定的 valueIfAbsent。
     *
     * @param valueIfAbsent 如果未完成，则返回的值
     * @return 如果完成，则返回结果值，否则返回给定的 valueIfAbsent
     * @throws CancellationException 如果计算被取消
     * @throws CompletionException 如果此 Future 异常完成或完成计算抛出了异常
     */
    public T getNow(T valueIfAbsent) {
        Object r;
        return ((r = result) == null) ? valueIfAbsent : reportJoin(r);
    }

    /**
     * 如果尚未完成，则将 {@link #get()} 和相关方法返回的值设置为给定值。
     *
     * @param value 结果值
     * @return 如果此调用导致此 CompletableFuture 转换到完成状态，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean complete(T value) {
        boolean triggered = completeValue(value);
        postComplete();
        return triggered;
    }


    /**
     * 如果尚未完成，则导致调用 {@link #get()}
     * 和相关方法抛出给定的异常。
     *
     * @param ex 异常
     * @return 如果此调用导致此 CompletableFuture
     * 转换为完成状态，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) throw new NullPointerException();
        boolean triggered = internalComplete(new AltResult(ex));
        postComplete();
        return triggered;
    }

    public <U> CompletableFuture<U> thenApply(
        Function<? super T,? extends U> fn) {
        return uniApplyStage(null, fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(
        Function<? super T,? extends U> fn) {
        return uniApplyStage(asyncPool, fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(
        Function<? super T,? extends U> fn, Executor executor) {
        return uniApplyStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return uniAcceptStage(null, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return uniAcceptStage(asyncPool, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action,
                                                   Executor executor) {
        return uniAcceptStage(screenExecutor(executor), action);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return uniRunStage(null, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return uniRunStage(asyncPool, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action,
                                                Executor executor) {
        return uniRunStage(screenExecutor(executor), action);
    }

    public <U,V> CompletableFuture<V> thenCombine(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn) {
        return biApplyStage(null, other, fn);
    }

    public <U,V> CompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn) {
        return biApplyStage(asyncPool, other, fn);
    }

    public <U,V> CompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T,? super U,? extends V> fn, Executor executor) {
        return biApplyStage(screenExecutor(executor), other, fn);
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(null, other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(asyncPool, other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action, Executor executor) {
        return biAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other,
                                                Runnable action) {
        return biRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other,
                                                     Runnable action) {
        return biRunStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other,
                                                     Runnable action,
                                                     Executor executor) {
        return biRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> applyToEither(
        CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(null, other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(asyncPool, other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other, Function<? super T, U> fn,
        Executor executor) {
        return orApplyStage(screenExecutor(executor), other, fn);
    }

    public CompletableFuture<Void> acceptEither(
        CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(null, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other, Consumer<? super T> action,
        Executor executor) {
        return orAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other,
                                                  Runnable action) {
        return orRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other,
                                                       Runnable action) {
        return orRunStage(asyncPool, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other,
                                                       Runnable action,
                                                       Executor executor) {
        return orRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> thenCompose(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(null, fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(asyncPool, fn);
    }


                public <U> CompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn,
        Executor executor) {
        return uniComposeStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<T> whenComplete(
        BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(null, action);
    }

    public CompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(asyncPool, action);
    }

    public CompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return uniWhenCompleteStage(screenExecutor(executor), action);
    }

    public <U> CompletableFuture<U> handle(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(null, fn);
    }

    public <U> CompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(asyncPool, fn);
    }

    public <U> CompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return uniHandleStage(screenExecutor(executor), fn);
    }

    /**
     * 返回此 CompletableFuture。
     *
     * @return 此 CompletableFuture
     */
    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    // 不在接口 CompletionStage 中

    /**
     * 返回一个新的 CompletableFuture，当此 CompletableFuture 完成时，它也会完成，如果此 CompletableFuture 异常完成，则使用触发此 CompletableFuture 完成的异常的给定函数的结果完成；否则，如果此 CompletableFuture 正常完成，则返回的 CompletableFuture 也以相同的值正常完成。
     * 注意：此功能的更灵活版本可使用 {@code whenComplete} 和 {@code handle} 方法获得。
     *
     * @param fn 如果此 CompletableFuture 异常完成，则用于计算返回的 CompletableFuture 值的函数
     * @return 新的 CompletableFuture
     */
    public CompletableFuture<T> exceptionally(
        Function<Throwable, ? extends T> fn) {
        return uniExceptionallyStage(fn);
    }

    /* ------------- 任意数量的构造方法 -------------- */

    /**
     * 返回一个新的 CompletableFuture，当所有给定的 CompletableFutures 完成时，它也会完成。如果任何给定的 CompletableFutures 异常完成，则返回的 CompletableFuture 也会异常完成，带有 CompletionException，该异常将此异常作为其原因。否则，给定的 CompletableFutures 的结果（如果有）不会反映在返回的 CompletableFuture 中，但可以通过单独检查它们来获得。如果没有提供任何 CompletableFutures，则返回一个以值 {@code null} 完成的 CompletableFuture。
     *
     * <p>此方法的应用之一是在继续程序之前等待一组独立的 CompletableFutures 完成，例如：{@code CompletableFuture.allOf(c1, c2, c3).join();}。
     *
     * @param cfs 给定的 CompletableFutures
     * @return 一个新的 CompletableFuture，当所有给定的 CompletableFutures 完成时，它也会完成
     * @throws NullPointerException 如果数组或其任何元素为 {@code null}
     */
    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        return andTree(cfs, 0, cfs.length - 1);
    }

    /**
     * 返回一个新的 CompletableFuture，当任何给定的 CompletableFutures 完成时，它也会完成，具有相同的结果。否则，如果它异常完成，返回的 CompletableFuture 也会异常完成，带有 CompletionException，该异常将此异常作为其原因。如果没有提供任何 CompletableFutures，则返回一个未完成的 CompletableFuture。
     *
     * @param cfs 给定的 CompletableFutures
     * @return 一个新的 CompletableFuture，当任何给定的 CompletableFutures 完成时，它也会完成，具有相同的结果或异常
     * @throws NullPointerException 如果数组或其任何元素为 {@code null}
     */
    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        return orTree(cfs, 0, cfs.length - 1);
    }

    /* ------------- 控制和状态方法 -------------- */

    /**
     * 如果尚未完成，则使用 {@link CancellationException} 完成此 CompletableFuture。未完成的依赖 CompletableFutures 也将异常完成，带有由这个 {@code CancellationException} 引起的 {@link CompletionException}。
     *
     * @param mayInterruptIfRunning 在此实现中，此值没有效果，因为中断不用于控制处理。
     *
     * @return 如果此任务现在已取消，则返回 {@code true}
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = (result == null) &&
            internalComplete(new AltResult(new CancellationException()));
        postComplete();
        return cancelled || isCancelled();
    }

    /**
     * 如果此 CompletableFuture 在正常完成前被取消，则返回 {@code true}。
     *
     * @return 如果此 CompletableFuture 在正常完成前被取消，则返回 {@code true}
     */
    public boolean isCancelled() {
        Object r;
        return ((r = result) instanceof AltResult) &&
            (((AltResult)r).ex instanceof CancellationException);
    }

    /**
     * 如果此 CompletableFuture 以任何方式异常完成，则返回 {@code true}。可能的原因包括取消、显式调用 {@code completeExceptionally} 以及 CompletionStage 动作的突然终止。
     *
     * @return 如果此 CompletableFuture 异常完成，则返回 {@code true}
     */
    public boolean isCompletedExceptionally() {
        Object r;
        return ((r = result) instanceof AltResult) && r != NIL;
    }

                /**
     * 强制设置或重置方法 {@link #get()} 和相关方法随后返回的值，无论是否已完成。此方法仅设计用于错误恢复操作，
     * 即使在这种情况下，也可能导致正在进行的依赖完成使用已建立的而非覆盖的结果。
     *
     * @param value 完成值
     */
    public void obtrudeValue(T value) {
        result = (value == null) ? NIL : value;
        postComplete();
    }

    /**
     * 强制导致方法 {@link #get()} 和相关方法的后续调用抛出给定的异常，无论是否已完成。此方法仅设计用于错误恢复操作，
     * 即使在这种情况下，也可能导致正在进行的依赖完成使用已建立的而非覆盖的结果。
     *
     * @param ex 异常
     * @throws NullPointerException 如果异常为 null
     */
    public void obtrudeException(Throwable ex) {
        if (ex == null) throw new NullPointerException();
        result = new AltResult(ex);
        postComplete();
    }

    /**
     * 返回估计的 CompletableFutures 数量，这些 CompletableFutures 的完成依赖于此 CompletableFuture 的完成。
     * 此方法设计用于监控系统状态，而不是同步控制。
     *
     * @return 依赖于此 CompletableFuture 完成的 CompletableFutures 数量
     */
    public int getNumberOfDependents() {
        int count = 0;
        for (Completion p = stack; p != null; p = p.next)
            ++count;
        return count;
    }

    /**
     * 返回一个标识此 CompletableFuture 以及其完成状态的字符串。状态，用括号表示，包含字符串 {@code "Completed Normally"}
     * 或字符串 {@code "Completed Exceptionally"}，或字符串 {@code "Not completed"}，后跟依赖于其完成的 CompletableFutures 数量（如果有）。
     *
     * @return 一个标识此 CompletableFuture 及其状态的字符串
     */
    public String toString() {
        Object r = result;
        int count;
        return super.toString() +
            ((r == null) ?
             (((count = getNumberOfDependents()) == 0) ?
              "[Not completed]" :
              "[Not completed, " + count + " dependents]") :
             (((r instanceof AltResult) && ((AltResult)r).ex != null) ?
              "[Completed exceptionally]" :
              "[Completed normally]"));
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long RESULT;
    private static final long STACK;
    private static final long NEXT;
    static {
        try {
            final sun.misc.Unsafe u;
            UNSAFE = u = sun.misc.Unsafe.getUnsafe();
            Class<?> k = CompletableFuture.class;
            RESULT = u.objectFieldOffset(k.getDeclaredField("result"));
            STACK = u.objectFieldOffset(k.getDeclaredField("stack"));
            NEXT = u.objectFieldOffset
                (Completion.class.getDeclaredField("next"));
        } catch (Exception x) {
            throw new Error(x);
        }
    }
}
