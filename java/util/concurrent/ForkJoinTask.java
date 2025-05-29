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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.Constructor;

/**
 * 在 {@link ForkJoinPool} 中运行的任务的抽象基类。
 * 一个 {@code ForkJoinTask} 是一种线程实体，比普通线程轻量得多。大量的任务和子任务可以由
 * ForkJoinPool 中的少量实际线程托管，但会有一些使用限制。
 *
 * <p>一个“主” {@code ForkJoinTask} 在显式提交给 {@link ForkJoinPool} 时开始执行，或者如果尚未参与
 * ForkJoin 计算，则通过 {@link ForkJoinPool#commonPool()} 中的 {@link #fork}、{@link #invoke} 或相关方法开始。
 * 一旦启动，它通常会启动其他子任务。如类名所示，许多使用 {@code ForkJoinTask} 的程序仅使用方法 {@link #fork} 和
 * {@link #join}，或其衍生方法如 {@link #invokeAll(ForkJoinTask...) invokeAll}。然而，此类还提供了许多其他方法，这些方法在
 * 高级用法中可能发挥作用，以及扩展机制，允许支持新的 fork/join 处理形式。
 *
 * <p>{@code ForkJoinTask} 是 {@link Future} 的轻量级形式。
 * {@code ForkJoinTask} 的效率源于一组限制（这些限制只能部分静态强制执行），反映了它们主要用于计算纯函数或操作完全隔离对象的任务。
 * 主要的协调机制是 {@link #fork}，用于安排异步执行，以及 {@link #join}，在任务的结果计算完成之前不会继续执行。
 * 理想情况下，计算应避免使用 {@code synchronized} 方法或块，并应尽量减少除加入其他任务或使用 Phasers 之类的同步器之外的其他阻塞同步，
 * 这些同步器被宣传为与 fork/join 调度合作。可细分的任务还应避免执行阻塞 I/O，并且理想情况下应访问与其他正在运行的任务完全独立的变量。
 * 这些指南通过不允许抛出如 {@code IOExceptions} 之类的检查异常来松散地强制执行。然而，计算仍可能遇到未检查的异常，这些异常会被重新抛给尝试加入它们的调用者。
 * 这些异常可能还包括由于内部资源耗尽（如无法分配内部任务队列）而导致的 {@link RejectedExecutionException}。
 * 重新抛出的异常与常规异常的行为相同，但在可能的情况下，它们包含（例如使用 {@code ex.printStackTrace()} 显示的）发起计算的线程以及实际遇到异常的线程的堆栈跟踪；至少包含后者。
 *
 * <p>可以定义和使用可能阻塞的 ForkJoinTasks，但这样做需要三个额外的考虑：(1) 其他任务的完成不应依赖于阻塞外部同步或 I/O 的任务。
 * 从不加入（例如，那些继承自 {@link CountedCompleter} 的）事件风格的异步任务通常属于此类。(2) 为了最小化资源影响，任务应尽可能小；理想情况下，只执行（可能的）阻塞操作。(3) 除非使用
 * {@link ForkJoinPool.ManagedBlocker} API，或者可能阻塞的任务数量已知少于池的 {@link ForkJoinPool#getParallelism} 级别，否则池无法保证有足够的线程来确保进度或良好的性能。
 *
 * <p>等待任务完成并提取结果的主要方法是 {@link #join}，但有几个变体：{@link Future#get} 方法支持可中断和/或定时的等待完成，并使用 {@code Future}
 * 约定报告结果。方法 {@link #invoke} 在语义上等同于 {@code fork(); join()}，但总是尝试在当前线程中开始执行。这些方法的“<em>安静</em>”形式不提取结果或报告异常。
 * 当一组任务正在执行，且需要延迟处理结果或异常直到所有任务完成时，这些方法可能很有用。方法 {@code invokeAll}（有多个版本）执行最常见的并行调用形式：分叉一组任务并加入它们所有。
 *
 * <p>在最典型的用法中，一个 fork-join 对就像并行递归函数的调用（fork）和返回（join）。与其他形式的递归调用一样，返回（joins）应从最内层开始。例如，
 * {@code a.fork(); b.fork(); b.join(); a.join();} 可能比先加入 {@code a} 再加入 {@code b} 高效得多。
 *
 * <p>可以通过多个层次的细节查询任务的执行状态：{@link #isDone} 为真表示任务以任何方式完成（包括任务在执行前被取消的情况）；
 * {@link #isCompletedNormally} 为真表示任务在没有取消或遇到异常的情况下完成；{@link #isCancelled} 为真表示任务被取消（在这种情况下，
 * {@link #getException} 返回一个 {@link java.util.concurrent.CancellationException}）；而 {@link #isCompletedAbnormally} 为真表示任务被取消或遇到异常，
 * 在这种情况下，{@link #getException} 将返回遇到的异常或 {@link java.util.concurrent.CancellationException}。
 *
 * <p>ForkJoinTask 类通常不直接继承。相反，您继承一个支持特定风格的 fork/join 处理的抽象类，通常是 {@link RecursiveAction} 用于大多数不返回结果的计算，
 * {@link RecursiveTask} 用于返回结果的计算，以及 {@link CountedCompleter} 用于完成的动作触发其他动作的任务。通常，具体的 ForkJoinTask 子类声明其参数字段，
 * 在构造函数中建立，并定义一个使用此基类提供的控制方法的 {@code compute} 方法。
 *
 * <p>方法 {@link #join} 及其变体仅在完成依赖关系为无环时适用；即，并行计算可以描述为有向无环图（DAG）。否则，执行可能会遇到任务循环等待对方的形式的死锁。
 * 然而，此框架支持其他方法和技术（例如使用 {@link Phaser}、{@link #helpQuiesce} 和 {@link #complete}），这些方法和技术可能有助于构建不是静态结构为 DAG 的问题的自定义子类。
 * 为了支持此类用法，可以使用 {@link #setForkJoinTaskTag} 或 {@link #compareAndSetForkJoinTaskTag} 以 {@code short} 值原子地<em>标记</em>一个 ForkJoinTask，
 * 并使用 {@link #getForkJoinTaskTag} 检查。ForkJoinTask 实现不使用这些 {@code protected} 方法或标签，但它们可能在构建专业子类时有用。例如，可以使用提供的方法
 * 避免重新访问已处理的节点/任务。
 * （标记方法的名称冗长部分是为了鼓励定义反映其使用模式的方法。）
 *
 * <p>大多数基础支持方法都是 {@code final}，以防止覆盖与底层轻量级任务调度框架紧密相关的实现。创建新的基本风格的 fork/join 处理的开发人员应至少实现
 * {@code protected} 方法 {@link #exec}、{@link #setRawResult} 和 {@link #getRawResult}，同时引入一个可以在其子类中实现的抽象计算方法，
 * 可能依赖于此基类提供的其他 {@code protected} 方法。
 *
 * <p>ForkJoinTasks 应执行相对少量的计算。大型任务应拆分为较小的子任务，通常通过递归分解。作为一个非常粗略的经验法则，一个任务应执行超过 100 且少于 10000 个基本计算步骤，
 * 并应避免无限循环。如果任务太大，那么并行性无法提高吞吐量。如果太小，那么内存和内部任务维护开销可能会压倒处理。
 *
 * <p>此类提供了 {@code adapt} 方法用于 {@link Runnable} 和 {@link Callable}，当混合执行 {@code ForkJoinTasks} 与其他类型的任务时可能有用。
 * 当所有任务都是这种形式时，考虑使用以 <em>asyncMode</em> 构建的池。
 *
 * <p>ForkJoinTasks 是 {@code Serializable}，这使得它们可以在扩展中使用，例如远程执行框架。在执行前或后，而不是在执行期间，序列化任务是有意义的。
 * 序列化在执行本身中不被依赖。
 *
 * @since 1.7
 * @author Doug Lea
 */
public abstract class ForkJoinTask<V> implements Future<V>, Serializable {

                    /*
     * 请参阅 ForkJoinPool 类的内部文档以获取一般实现概述。ForkJoinTasks 主要负责在调用 ForkJoinWorkerThread 和 ForkJoinPool 的方法时维护其 "status" 字段。
     *
     * 该类的方法大致分为以下几层：
     * (1) 基本状态维护
     * (2) 执行和等待完成
     * (3) 另外报告结果的用户级方法。
     * 由于此文件中的导出方法按 javadocs 中的顺序排列，因此有时这些层次结构不太明显。
     */

    /*
     * 状态字段包含运行控制状态位，这些状态位被打包到一个 int 中，以最小化占用空间并确保原子性（通过 CAS）。状态最初为零，直到完成前一直保持非负值，完成时状态（与 DONE_MASK 进行按位与操作后）持有值 NORMAL、CANCELLED 或 EXCEPTIONAL。其他线程正在阻塞等待的任务会设置 SIGNAL 位。带有 SIGNAL 位的被窃取任务完成时，会通过 notifyAll 唤醒任何等待者。尽管在某些情况下不是最优选择，但我们使用基本的内置 wait/notify 来利用 JVM 中的“监视器膨胀”，否则我们需要模拟这些功能以避免增加每个任务的额外簿记开销。我们希望这些监视器是“胖”的，即不使用偏向或薄锁技术，因此使用了一些避免这些技术的编码习惯，主要是通过安排每个同步块执行 wait、notifyAll 或两者。
     *
     * 这些控制位仅占用状态字段的上半部分（16 位）中的某些位。较低的位用于用户定义的标签。
     */

    /** 此任务的运行状态 */
    volatile int status; // 由池和工作线程直接访问
    static final int DONE_MASK   = 0xf0000000;  // 掩码出非完成位
    static final int NORMAL      = 0xf0000000;  // 必须为负数
    static final int CANCELLED   = 0xc0000000;  // 必须小于 NORMAL
    static final int EXCEPTIONAL = 0x80000000;  // 必须小于 CANCELLED
    static final int SIGNAL      = 0x00010000;  // 必须 >= 1 << 16
    static final int SMASK       = 0x0000ffff;  // 用于标签的短位

    /**
     * 标记完成并唤醒等待加入此任务的线程。
     *
     * @param completion 为 NORMAL、CANCELLED 或 EXCEPTIONAL 之一
     * @return 退出时的完成状态
     */
    private int setCompletion(int completion) {
        for (int s;;) {
            if ((s = status) < 0)
                return s;
            if (U.compareAndSwapInt(this, STATUS, s, s | completion)) {
                if ((s >>> 16) != 0)
                    synchronized (this) { notifyAll(); }
                return completion;
            }
        }
    }

    /**
     * 被窃取任务的主要执行方法。除非已完成，否则调用 exec 并在完成时记录状态，但不会在其他情况下等待完成。
     *
     * @return 从该方法退出时的状态
     */
    final int doExec() {
        int s; boolean completed;
        if ((s = status) >= 0) {
            try {
                completed = exec();
            } catch (Throwable rex) {
                return setExceptionalCompletion(rex);
            }
            if (completed)
                s = setCompletion(NORMAL);
        }
        return s;
    }

    /**
     * 如果未完成，则设置 SIGNAL 状态并执行 Object.wait(timeout)。
     * 退出时，此任务可能已完成或未完成。忽略中断。
     *
     * @param timeout 使用 Object.wait 的约定。
     */
    final void internalWait(long timeout) {
        int s;
        if ((s = status) >= 0 && // 强制完成者发出通知
            U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
            synchronized (this) {
                if (status >= 0)
                    try { wait(timeout); } catch (InterruptedException ie) { }
                else
                    notifyAll();
            }
        }
    }

    /**
     * 阻塞非工作线程直到完成。
     * @return 完成时的状态
     */
    private int externalAwaitDone() {
        int s = ((this instanceof CountedCompleter) ? // 尝试帮助
                 ForkJoinPool.common.externalHelpComplete(
                     (CountedCompleter<?>)this, 0) :
                 ForkJoinPool.common.tryExternalUnpush(this) ? doExec() : 0);
        if (s >= 0 && (s = status) >= 0) {
            boolean interrupted = false;
            do {
                if (U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                    synchronized (this) {
                        if (status >= 0) {
                            try {
                                wait(0L);
                            } catch (InterruptedException ie) {
                                interrupted = true;
                            }
                        }
                        else
                            notifyAll();
                    }
                }
            } while ((s = status) >= 0);
            if (interrupted)
                Thread.currentThread().interrupt();
        }
        return s;
    }

    /**
     * 阻塞非工作线程直到完成或中断。
     */
    private int externalInterruptibleAwaitDone() throws InterruptedException {
        int s;
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((s = status) >= 0 &&
            (s = ((this instanceof CountedCompleter) ?
                  ForkJoinPool.common.externalHelpComplete(
                      (CountedCompleter<?>)this, 0) :
                  ForkJoinPool.common.tryExternalUnpush(this) ? doExec() :
                  0)) >= 0) {
            while ((s = status) >= 0) {
                if (U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                    synchronized (this) {
                        if (status >= 0)
                            wait(0L);
                        else
                            notifyAll();
                    }
                }
            }
        }
        return s;
    }


                    /**
     * 实现 join, get, quietlyJoin 的方法。直接处理已完成、外部等待和 unfork+exec 的情况。
     * 其他情况则交给 ForkJoinPool.awaitJoin 处理。
     *
     * @return 完成时的状态
     */
    private int doJoin() {
        int s; Thread t; ForkJoinWorkerThread wt; ForkJoinPool.WorkQueue w;
        return (s = status) < 0 ? s :
            ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            (w = (wt = (ForkJoinWorkerThread)t).workQueue).
            tryUnpush(this) && (s = doExec()) < 0 ? s :
            wt.pool.awaitJoin(w, this, 0L) :
            externalAwaitDone();
    }

    /**
     * 实现 invoke, quietlyInvoke 的方法。
     *
     * @return 完成时的状态
     */
    private int doInvoke() {
        int s; Thread t; ForkJoinWorkerThread wt;
        return (s = doExec()) < 0 ? s :
            ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            (wt = (ForkJoinWorkerThread)t).pool.
            awaitJoin(wt.workQueue, this, 0L) :
            externalAwaitDone();
    }

    // 异常表支持

    /**
     * 任务抛出的异常表，用于调用者报告异常。
     * 由于异常很少发生，我们不直接将它们与任务对象一起保存，而是使用弱引用表。
     * 注意：取消异常不会出现在表中，而是作为状态值记录。
     *
     * 注意：这些静态变量在下面的静态块中初始化。
     */
    private static final ExceptionNode[] exceptionTable;
    private static final ReentrantLock exceptionTableLock;
    private static final ReferenceQueue<Object> exceptionTableRefQueue;

    /**
     * 异常表的固定容量。
     */
    private static final int EXCEPTION_MAP_CAPACITY = 32;

    /**
     * 异常表的键值节点。链式哈希表使用身份比较、完全锁定和弱引用键。
     * 表的容量是固定的，因为它只在足够长的时间内维护任务异常，以便 joiner 访问它们，因此不应变得非常大。
     * 然而，由于我们不知道最后一个 joiner 何时完成，因此必须使用弱引用并清除它们。我们在每次操作时都这样做（因此完全锁定）。
     * 此外，任何 ForkJoinPool 中的某些线程将在其池变为静止时调用 helpExpungeStaleExceptions。
     */
    static final class ExceptionNode extends WeakReference<ForkJoinTask<?>> {
        final Throwable ex;
        ExceptionNode next;
        final long thrower;  // 使用 ID 而不是引用以避免弱循环
        final int hashCode;  // 在弱引用消失之前存储任务的哈希码
        ExceptionNode(ForkJoinTask<?> task, Throwable ex, ExceptionNode next) {
            super(task, exceptionTableRefQueue);
            this.ex = ex;
            this.next = next;
            this.thrower = Thread.currentThread().getId();
            this.hashCode = System.identityHashCode(task);
        }
    }

    /**
     * 记录异常并设置状态。
     *
     * @return 退出时的状态
     */
    final int recordExceptionalCompletion(Throwable ex) {
        int s;
        if ((s = status) >= 0) {
            int h = System.identityHashCode(this);
            final ReentrantLock lock = exceptionTableLock;
            lock.lock();
            try {
                expungeStaleExceptions();
                ExceptionNode[] t = exceptionTable;
                int i = h & (t.length - 1);
                for (ExceptionNode e = t[i]; ; e = e.next) {
                    if (e == null) {
                        t[i] = new ExceptionNode(this, ex, t[i]);
                        break;
                    }
                    if (e.get() == this) // 已存在
                        break;
                }
            } finally {
                lock.unlock();
            }
            s = setCompletion(EXCEPTIONAL);
        }
        return s;
    }

    /**
     * 记录异常并可能传播。
     *
     * @return 退出时的状态
     */
    private int setExceptionalCompletion(Throwable ex) {
        int s = recordExceptionalCompletion(ex);
        if ((s & DONE_MASK) == EXCEPTIONAL)
            internalPropagateException(ex);
        return s;
    }

    /**
     * 为具有完成者的任务提供异常传播支持的钩子。
     */
    void internalPropagateException(Throwable ex) {
    }

    /**
     * 取消任务，忽略 cancel 抛出的任何异常。用于工作线程和池的关闭。
     * 根据规范，cancel 不应抛出任何异常，但如果确实抛出，我们在关闭期间没有解决方法，因此对此情况进行保护。
     */
    static final void cancelIgnoringExceptions(ForkJoinTask<?> t) {
        if (t != null && t.status >= 0) {
            try {
                t.cancel(false);
            } catch (Throwable ignore) {
            }
        }
    }

    /**
     * 移除异常节点并清除状态。
     */
    private void clearExceptionalCompletion() {
        int h = System.identityHashCode(this);
        final ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            ExceptionNode[] t = exceptionTable;
            int i = h & (t.length - 1);
            ExceptionNode e = t[i];
            ExceptionNode pred = null;
            while (e != null) {
                ExceptionNode next = e.next;
                if (e.get() == this) {
                    if (pred == null)
                        t[i] = next;
                    else
                        pred.next = next;
                    break;
                }
                pred = e;
                e = next;
            }
            expungeStaleExceptions();
            status = 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果可用，返回给定任务的可重抛异常。为了提供准确的堆栈跟踪，如果异常不是由当前线程抛出的，
     * 我们尝试创建一个与抛出的异常相同类型的异常，但以记录的异常作为其原因。如果没有这样的构造函数，
     * 我们尝试使用无参数构造函数，然后使用 initCause 达到相同的效果。如果这些都不适用，或者由于其他异常而失败，
     * 我们返回记录的异常，尽管它可能包含误导性的堆栈跟踪。
     *
     * @return 异常，如果没有则返回 null
     */
    private Throwable getThrowableException() {
        if ((status & DONE_MASK) != EXCEPTIONAL)
            return null;
        int h = System.identityHashCode(this);
        ExceptionNode e;
        final ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            expungeStaleExceptions();
            ExceptionNode[] t = exceptionTable;
            e = t[h & (t.length - 1)];
            while (e != null && e.get() != this)
                e = e.next;
        } finally {
            lock.unlock();
        }
        Throwable ex;
        if (e == null || (ex = e.ex) == null)
            return null;
        if (e.thrower != Thread.currentThread().getId()) {
            Class<? extends Throwable> ec = ex.getClass();
            try {
                Constructor<?> noArgCtor = null;
                Constructor<?>[] cs = ec.getConstructors();// 仅公共构造函数
                for (int i = 0; i < cs.length; ++i) {
                    Constructor<?> c = cs[i];
                    Class<?>[] ps = c.getParameterTypes();
                    if (ps.length == 0)
                        noArgCtor = c;
                    else if (ps.length == 1 && ps[0] == Throwable.class) {
                        Throwable wx = (Throwable)c.newInstance(ex);
                        return (wx == null) ? ex : wx;
                    }
                }
                if (noArgCtor != null) {
                    Throwable wx = (Throwable)(noArgCtor.newInstance());
                    if (wx != null) {
                        wx.initCause(ex);
                        return wx;
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return ex;
    }


                    /**
     * 检查过时的引用并移除它们。仅在持有锁时调用。
     */
    private static void expungeStaleExceptions() {
        for (Object x; (x = exceptionTableRefQueue.poll()) != null;) {
            if (x instanceof ExceptionNode) {
                int hashCode = ((ExceptionNode)x).hashCode;
                ExceptionNode[] t = exceptionTable;
                int i = hashCode & (t.length - 1);
                ExceptionNode e = t[i];
                ExceptionNode pred = null;
                while (e != null) {
                    ExceptionNode next = e.next;
                    if (e == x) {
                        if (pred == null)
                            t[i] = next;
                        else
                            pred.next = next;
                        break;
                    }
                    pred = e;
                    e = next;
                }
            }
        }
    }

    /**
     * 如果锁可用，检查过时的引用并移除它们。
     * 在池变为空闲时由 ForkJoinPool 调用。
     */
    static final void helpExpungeStaleExceptions() {
        final ReentrantLock lock = exceptionTableLock;
        if (lock.tryLock()) {
            try {
                expungeStaleExceptions();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 一种“偷偷抛出”异常的方法，用于传递异常。
     */
    static void rethrow(Throwable ex) {
        if (ex != null)
            ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
    }

    /**
     * “偷偷抛出”异常的实现部分，依赖于泛型的限制来规避编译器对重新抛出未检查异常的警告。
     */
    @SuppressWarnings("unchecked") static <T extends Throwable>
        void uncheckedThrow(Throwable t) throws T {
        throw (T)t; // 依赖于空泛型转换
    }

    /**
     * 抛出与给定状态相关联的异常（如果有）。
     */
    private void reportException(int s) {
        if (s == CANCELLED)
            throw new CancellationException();
        if (s == EXCEPTIONAL)
            rethrow(getThrowableException());
    }

    // 公有方法

    /**
     * 安排异步执行当前任务所在的池中的此任务，如果适用，否则使用 {@link
     * ForkJoinPool#commonPool()}。虽然不一定强制执行，但多次分叉一个任务（除非它已完成并重新初始化）是使用错误。对任务状态或其操作的数据的后续修改不一定对任何其他线程可见，除非在调用 {@link #join} 或相关方法，或调用 {@link #isDone} 返回 {@code
     * true} 之前进行。
     *
     * @return {@code this}，以简化使用
     */
    public final ForkJoinTask<V> fork() {
        Thread t;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            ((ForkJoinWorkerThread)t).workQueue.push(this);
        else
            ForkJoinPool.common.externalPush(this);
        return this;
    }

    /**
     * 当任务 {@link #isDone 完成} 时返回计算结果。此方法与 {@link #get()} 不同之处在于，异常完成会导致抛出 {@code RuntimeException} 或 {@code Error}，而不是 {@code ExecutionException}，并且调用线程的中断不会导致该方法通过抛出 {@code
     * InterruptedException} 而突然返回。
     *
     * @return 计算结果
     */
    public final V join() {
        int s;
        if ((s = doJoin() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }

    /**
     * 开始执行此任务，必要时等待其完成，并返回其结果，或者如果底层计算抛出异常，则抛出（未检查的）{@code RuntimeException} 或 {@code Error}。
     *
     * @return 计算结果
     */
    public final V invoke() {
        int s;
        if ((s = doInvoke() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }

    /**
     * 分叉给定的任务，当每个任务 {@code isDone} 或遇到（未检查的）异常时返回，此时异常将被重新抛出。如果多个任务遇到异常，此方法将抛出这些异常中的任何一个。如果任何任务遇到异常，其他任务可能会被取消。但是，在异常返回时，单个任务的执行状态不保证。可以使用 {@link
     * #getException()} 和相关方法检查每个任务的状态，以确定它们是否已被取消、正常完成或异常完成，或未处理。
     *
     * @param t1 第一个任务
     * @param t2 第二个任务
     * @throws NullPointerException 如果任何任务为 null
     */
    public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
        int s1, s2;
        t2.fork();
        if ((s1 = t1.doInvoke() & DONE_MASK) != NORMAL)
            t1.reportException(s1);
        if ((s2 = t2.doJoin() & DONE_MASK) != NORMAL)
            t2.reportException(s2);
    }

    /**
     * 分叉给定的任务，当每个任务 {@code isDone} 或遇到（未检查的）异常时返回，此时异常将被重新抛出。如果多个任务遇到异常，此方法将抛出这些异常中的任何一个。如果任何任务遇到异常，其他任务可能会被取消。但是，在异常返回时，单个任务的执行状态不保证。可以使用 {@link
     * #getException()} 和相关方法检查每个任务的状态，以确定它们是否已被取消、正常完成或异常完成，或未处理。
     *
     * @param tasks 任务列表
     * @throws NullPointerException 如果任何任务为 null
     */
    public static void invokeAll(ForkJoinTask<?>... tasks) {
        Throwable ex = null;
        int last = tasks.length - 1;
        for (int i = last; i >= 0; --i) {
            ForkJoinTask<?> t = tasks[i];
            if (t == null) {
                if (ex == null)
                    ex = new NullPointerException();
            }
            else if (i != 0)
                t.fork();
            else if (t.doInvoke() < NORMAL && ex == null)
                ex = t.getException();
        }
        for (int i = 1; i <= last; ++i) {
            ForkJoinTask<?> t = tasks[i];
            if (t != null) {
                if (ex != null)
                    t.cancel(false);
                else if (t.doJoin() < NORMAL)
                    ex = t.getException();
            }
        }
        if (ex != null)
            rethrow(ex);
    }


                    /**
     * 分叉指定集合中的所有任务，当每个任务满足 {@code isDone} 或遇到（未检查的）异常时返回。
     * 如果遇到异常，该异常将被重新抛出。如果多个任务遇到异常，那么此方法将抛出这些异常中的任意一个。
     * 如果任何任务遇到异常，其他任务可能会被取消。但是，在异常返回时，单个任务的执行状态无法保证。
     * 可以使用 {@link #getException()} 和相关方法来检查每个任务是否已被取消、正常完成、异常完成或未处理。
     *
     * @param tasks 任务集合
     * @param <T> 任务返回值的类型
     * @return 任务参数，以简化使用
     * @throws NullPointerException 如果任务或任何元素为 null
     */
    public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
        if (!(tasks instanceof RandomAccess) || !(tasks instanceof List<?>)) {
            invokeAll(tasks.toArray(new ForkJoinTask<?>[tasks.size()]));
            return tasks;
        }
        @SuppressWarnings("unchecked")
        List<? extends ForkJoinTask<?>> ts =
            (List<? extends ForkJoinTask<?>>) tasks;
        Throwable ex = null;
        int last = ts.size() - 1;
        for (int i = last; i >= 0; --i) {
            ForkJoinTask<?> t = ts.get(i);
            if (t == null) {
                if (ex == null)
                    ex = new NullPointerException();
            }
            else if (i != 0)
                t.fork();
            else if (t.doInvoke() < NORMAL && ex == null)
                ex = t.getException();
        }
        for (int i = 1; i <= last; ++i) {
            ForkJoinTask<?> t = ts.get(i);
            if (t != null) {
                if (ex != null)
                    t.cancel(false);
                else if (t.doJoin() < NORMAL)
                    ex = t.getException();
            }
        }
        if (ex != null)
            rethrow(ex);
        return tasks;
    }

    /**
     * 尝试取消此任务的执行。如果任务已经完成或由于其他原因无法取消，此尝试将失败。
     * 如果成功，并且在调用 {@code cancel} 时此任务尚未开始执行，则此任务的执行将被抑制。
     * 在此方法成功返回后，除非有后续调用 {@link #reinitialize}，后续调用 {@link #isCancelled}、
     * {@link #isDone} 和 {@code cancel} 将返回 {@code true}，调用 {@link #join} 和相关方法将导致
     * {@code CancellationException}。
     *
     * <p>此方法可以在子类中重写，但必须确保这些属性仍然有效。特别是，{@code cancel} 方法本身不得抛出异常。
     *
     * <p>此方法设计为由 <em>其他</em> 任务调用。要终止当前任务，可以在其计算方法中返回或抛出未检查的异常，
     * 或调用 {@link #completeExceptionally(Throwable)}。
     *
     * @param mayInterruptIfRunning 在默认实现中，此值无效，因为中断不用于控制取消。
     *
     * @return 如果此任务现在已被取消，则返回 {@code true}
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        return (setCompletion(CANCELLED) & DONE_MASK) == CANCELLED;
    }

    public final boolean isDone() {
        return status < 0;
    }

    public final boolean isCancelled() {
        return (status & DONE_MASK) == CANCELLED;
    }

    /**
     * 如果此任务抛出异常或被取消，则返回 {@code true}。
     *
     * @return 如果此任务抛出异常或被取消，则返回 {@code true}
     */
    public final boolean isCompletedAbnormally() {
        return status < NORMAL;
    }

    /**
     * 如果此任务在未抛出异常且未被取消的情况下完成，则返回 {@code true}。
     *
     * @return 如果此任务在未抛出异常且未被取消的情况下完成，则返回 {@code true}
     */
    public final boolean isCompletedNormally() {
        return (status & DONE_MASK) == NORMAL;
    }

    /**
     * 返回基础计算抛出的异常，如果已取消，则返回 {@code CancellationException}，如果没有异常或方法尚未完成，则返回 {@code null}。
     *
     * @return 异常，如果没有则返回 {@code null}
     */
    public final Throwable getException() {
        int s = status & DONE_MASK;
        return ((s >= NORMAL)    ? null :
                (s == CANCELLED) ? new CancellationException() :
                getThrowableException());
    }

    /**
     * 异常完成此任务，如果尚未中止或取消，则在 {@code join} 和相关操作时抛出给定的异常。
     * 此方法可用于在异步任务中引发异常，或强制完成不会以其他方式完成的任务。在其他情况下使用此方法是不推荐的。
     * 此方法可被重写，但重写版本必须调用 {@code super} 实现以保持保证。
     *
     * @param ex 要抛出的异常。如果此异常不是 {@code RuntimeException} 或 {@code Error}，
     * 则实际抛出的异常将是一个带有原因 {@code ex} 的 {@code RuntimeException}。
     */
    public void completeExceptionally(Throwable ex) {
        setExceptionalCompletion((ex instanceof RuntimeException) ||
                                 (ex instanceof Error) ? ex :
                                 new RuntimeException(ex));
    }

    /**
     * 完成此任务，如果尚未中止或取消，则在后续调用 {@code join} 和相关操作时返回给定的值。
     * 此方法可用于为异步任务提供结果，或为不会以其他方式正常完成的任务提供替代处理。在其他情况下使用此方法是不推荐的。
     * 此方法可被重写，但重写版本必须调用 {@code super} 实现以保持保证。
     *
     * @param value 此任务的结果值
     */
    public void complete(V value) {
        try {
            setRawResult(value);
        } catch (Throwable rex) {
            setExceptionalCompletion(rex);
            return;
        }
        setCompletion(NORMAL);
    }

                    /**
     * 以不设置值的方式正常完成此任务。由 {@link #setRawResult} 最近设置的值（默认为 {@code
     * null}）将在后续的 {@code join} 和相关操作中作为结果返回。
     *
     * @since 1.8
     */
    public final void quietlyComplete() {
        setCompletion(NORMAL);
    }

    /**
     * 如果必要，等待计算完成，然后检索其结果。
     *
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程不是 ForkJoinPool 的成员，并且在等待时被中断
     */
    public final V get() throws InterruptedException, ExecutionException {
        int s = (Thread.currentThread() instanceof ForkJoinWorkerThread) ?
            doJoin() : externalInterruptibleAwaitDone();
        Throwable ex;
        if ((s &= DONE_MASK) == CANCELLED)
            throw new CancellationException();
        if (s == EXCEPTIONAL && (ex = getThrowableException()) != null)
            throw new ExecutionException(ex);
        return getRawResult();
    }

    /**
     * 如果必要，最多等待给定的时间让计算完成，然后如果可用则检索其结果。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程不是 ForkJoinPool 的成员，并且在等待时被中断
     * @throws TimeoutException 如果等待超时
     */
    public final V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        int s;
        long nanos = unit.toNanos(timeout);
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((s = status) >= 0 && nanos > 0L) {
            long d = System.nanoTime() + nanos;
            long deadline = (d == 0L) ? 1L : d; // 避免 0
            Thread t = Thread.currentThread();
            if (t instanceof ForkJoinWorkerThread) {
                ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
                s = wt.pool.awaitJoin(wt.workQueue, this, deadline);
            }
            else if ((s = ((this instanceof CountedCompleter) ?
                           ForkJoinPool.common.externalHelpComplete(
                               (CountedCompleter<?>)this, 0) :
                           ForkJoinPool.common.tryExternalUnpush(this) ?
                           doExec() : 0)) >= 0) {
                long ns, ms; // 以纳秒为单位测量，但以毫秒为单位等待
                while ((s = status) >= 0 &&
                       (ns = deadline - System.nanoTime()) > 0L) {
                    if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) > 0L &&
                        U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                        synchronized (this) {
                            if (status >= 0)
                                wait(ms); // 可以抛出 InterruptedException
                            else
                                notifyAll();
                        }
                    }
                }
            }
        }
        if (s >= 0)
            s = status;
        if ((s &= DONE_MASK) != NORMAL) {
            Throwable ex;
            if (s == CANCELLED)
                throw new CancellationException();
            if (s != EXCEPTIONAL)
                throw new TimeoutException();
            if ((ex = getThrowableException()) != null)
                throw new ExecutionException(ex);
        }
        return getRawResult();
    }

    /**
     * 连接此任务，但不返回其结果或抛出其异常。当处理任务集合时，此方法可能有用，特别是当某些任务已被取消或以其他方式已知已中止时。
     */
    public final void quietlyJoin() {
        doJoin();
    }

    /**
     * 开始执行此任务并在必要时等待其完成，但不返回其结果或抛出其异常。
     */
    public final void quietlyInvoke() {
        doInvoke();
    }

    /**
     * 可能执行任务，直到承载当前任务的池 {@link ForkJoinPool#isQuiescent 处于静止状态}。此方法在设计中可能有用，其中许多任务被分叉，但没有显式连接，而是执行它们直到全部处理完毕。
     */
    public static void helpQuiesce() {
        Thread t;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            wt.pool.helpQuiescePool(wt.workQueue);
        }
        else
            ForkJoinPool.quiesceCommonPool();
    }

    /**
     * 重置此任务的内部簿记状态，允许后续的 {@code fork}。此方法允许重复使用此任务，但仅在以下情况下：此任务从未被分叉，或者已被分叉，然后完成，并且此任务的所有未完成的连接也已完成。其他使用条件下的效果不保证。
     * 此方法完成后，{@code isDone()} 报告 {@code false}，且 {@code getException()} 报告 {@code
     * null}。但是，{@code getRawResult} 返回的值不受影响。要清除此值，可以调用 {@code
     * setRawResult(null)}。
     */
    public void reinitialize() {
        if ((status & DONE_MASK) == EXCEPTIONAL)
            clearExceptionalCompletion();
        else
            status = 0;
    }

                    /**
     * 返回当前任务执行所在的线程池，如果此任务是在任何 ForkJoinPool 之外执行的，则返回 null。
     *
     * @see #inForkJoinPool
     * @return 线程池，或如果不存在则返回 {@code null}
     */
    public static ForkJoinPool getPool() {
        Thread t = Thread.currentThread();
        return (t instanceof ForkJoinWorkerThread) ?
            ((ForkJoinWorkerThread) t).pool : null;
    }

    /**
     * 如果当前线程是作为 ForkJoinPool 计算执行的 {@link
     * ForkJoinWorkerThread}，则返回 {@code true}。
     *
     * @return 如果当前线程是作为 ForkJoinPool 计算执行的 {@link
     * ForkJoinWorkerThread}，则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean inForkJoinPool() {
        return Thread.currentThread() instanceof ForkJoinWorkerThread;
    }

    /**
     * 尝试取消此任务的执行调度。如果此任务是当前线程最近分叉的任务，并且尚未在其他线程中开始执行，此方法通常（但不保证）会成功。当安排可以被处理但未被窃取的任务的本地处理时，此方法可能有用。
     *
     * @return 如果成功取消分叉，则返回 {@code true}
     */
    public boolean tryUnfork() {
        Thread t;
        return (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
                ((ForkJoinWorkerThread)t).workQueue.tryUnpush(this) :
                ForkJoinPool.common.tryExternalUnpush(this));
    }

    /**
     * 返回当前工作线程已分叉但尚未执行的任务数量的估计值。此值可能有助于做出是否分叉其他任务的启发式决策。
     *
     * @return 任务数量
     */
    public static int getQueuedTaskCount() {
        Thread t; ForkJoinPool.WorkQueue q;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            q = ((ForkJoinWorkerThread)t).workQueue;
        else
            q = ForkJoinPool.commonSubmitterQueue();
        return (q == null) ? 0 : q.queueSize();
    }

    /**
     * 返回当前工作线程持有的本地队列任务数量与可能窃取这些任务的其他工作线程数量之间的差值，如果此线程不在 ForkJoinPool 中运行，则返回零。此值可能有助于做出是否分叉其他任务的启发式决策。在 ForkJoinTasks 的许多使用中，每个工作线程应保持一个小的常数剩余任务（例如，3），如果超过这个阈值，则应本地处理计算。
     *
     * @return 任务的剩余数量，可能是负数
     */
    public static int getSurplusQueuedTaskCount() {
        return ForkJoinPool.getSurplusQueuedTaskCount();
    }

    // 扩展方法

    /**
     * 返回 {@link #join} 将返回的结果，即使此任务异常完成，或者如果此任务尚未完成，则返回 {@code null}。此方法旨在辅助调试以及支持扩展。在其他上下文中使用此方法是不鼓励的。
     *
     * @return 结果，或如果未完成则返回 {@code null}
     */
    public abstract V getRawResult();

    /**
     * 强制返回给定的值作为结果。此方法旨在支持扩展，通常不应调用。
     *
     * @param value 值
     */
    protected abstract void setRawResult(V value);

    /**
     * 立即执行此任务的基本操作，并返回如果此方法返回时，此任务是否肯定已正常完成。此方法可能返回 false，以表示此任务不一定完成（或未知是否完成），例如在需要显式调用完成方法的异步操作中。此方法也可能抛出（未检查的）异常以表示异常退出。此方法旨在支持扩展，通常不应调用。
     *
     * @return 如果已知此任务已正常完成，则返回 {@code true}
     */
    protected abstract boolean exec();

    /**
     * 返回但不取消调度或执行当前线程已排队但尚未执行的任务，如果立即可用。没有保证此任务实际上会被轮询或执行。相反，即使任务存在但无法在没有与其他线程竞争的情况下访问，此方法也可能返回 null。此方法主要设计用于支持扩展，通常不会很有用。
     *
     * @return 下一个任务，或如果没有可用的任务则返回 {@code null}
     */
    protected static ForkJoinTask<?> peekNextLocalTask() {
        Thread t; ForkJoinPool.WorkQueue q;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            q = ((ForkJoinWorkerThread)t).workQueue;
        else
            q = ForkJoinPool.commonSubmitterQueue();
        return (q == null) ? null : q.peek();
    }

    /**
     * 如果当前线程在 ForkJoinPool 中运行，取消调度并返回当前线程已排队但尚未执行的下一个任务，如果不执行。此方法主要设计用于支持扩展，通常不会很有用。
     *
     * @return 下一个任务，或如果没有可用的任务则返回 {@code null}
     */
    protected static ForkJoinTask<?> pollNextLocalTask() {
        Thread t;
        return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            ((ForkJoinWorkerThread)t).workQueue.nextLocalTask() :
            null;
    }

    /**
     * 如果当前线程在 ForkJoinPool 中运行，取消调度并返回当前线程已排队但尚未执行的下一个任务，如果可用，或如果不可用，则返回其他线程分叉的任务，如果可用。可用性可能是短暂的，因此 {@code null} 结果并不一定意味着此任务运行所在的池处于静止状态。此方法主要设计用于支持扩展，通常不会很有用。
     *
     * @return 任务，或如果没有可用的任务则返回 {@code null}
     */
    protected static ForkJoinTask<?> pollTask() {
        Thread t; ForkJoinWorkerThread wt;
        return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            (wt = (ForkJoinWorkerThread)t).pool.nextTaskFor(wt.workQueue) :
            null;
    }

                    // 标签操作

    /**
     * 返回此任务的标签。
     *
     * @return 此任务的标签
     * @since 1.8
     */
    public final short getForkJoinTaskTag() {
        return (short)status;
    }

    /**
     * 原子地设置此任务的标签值。
     *
     * @param tag 标签值
     * @return 标签的前一个值
     * @since 1.8
     */
    public final short setForkJoinTaskTag(short tag) {
        for (int s;;) {
            if (U.compareAndSwapInt(this, STATUS, s = status,
                                    (s & ~SMASK) | (tag & SMASK)))
                return (short)s;
        }
    }

    /**
     * 原子地有条件地设置此任务的标签值。
     * 除了其他应用之外，标签可以用作任务在图上操作时的访问标记，
     * 例如在方法中检查：{@code
     * if (task.compareAndSetForkJoinTaskTag((short)0, (short)1))}
     * 在处理之前，否则退出，因为节点已经被访问过。
     *
     * @param e 预期的标签值
     * @param tag 新的标签值
     * @return 如果成功则返回 {@code true}；即当前值等于 e 且现在是 tag。
     * @since 1.8
     */
    public final boolean compareAndSetForkJoinTaskTag(short e, short tag) {
        for (int s;;) {
            if ((short)(s = status) != e)
                return false;
            if (U.compareAndSwapInt(this, STATUS, s,
                                    (s & ~SMASK) | (tag & SMASK)))
                return true;
        }
    }

    /**
     * Runnable 的适配器。这实现了 RunnableFuture
     * 以符合 AbstractExecutorService 在 ForkJoinPool 中使用时的约束。
     */
    static final class AdaptedRunnable<T> extends ForkJoinTask<T>
        implements RunnableFuture<T> {
        final Runnable runnable;
        T result;
        AdaptedRunnable(Runnable runnable, T result) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
            this.result = result; // 即使在完成之前设置也是可以的
        }
        public final T getRawResult() { return result; }
        public final void setRawResult(T v) { result = v; }
        public final boolean exec() { runnable.run(); return true; }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    /**
     * 没有结果的 Runnable 的适配器
     */
    static final class AdaptedRunnableAction extends ForkJoinTask<Void>
        implements RunnableFuture<Void> {
        final Runnable runnable;
        AdaptedRunnableAction(Runnable runnable) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
        }
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) { }
        public final boolean exec() { runnable.run(); return true; }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    /**
     * 失败时强制工作线程异常的 Runnable 的适配器
     */
    static final class RunnableExecuteAction extends ForkJoinTask<Void> {
        final Runnable runnable;
        RunnableExecuteAction(Runnable runnable) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
        }
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) { }
        public final boolean exec() { runnable.run(); return true; }
        void internalPropagateException(Throwable ex) {
            rethrow(ex); // 重新抛出 exec() 之外的捕获。
        }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    /**
     * Callable 的适配器
     */
    static final class AdaptedCallable<T> extends ForkJoinTask<T>
        implements RunnableFuture<T> {
        final Callable<? extends T> callable;
        T result;
        AdaptedCallable(Callable<? extends T> callable) {
            if (callable == null) throw new NullPointerException();
            this.callable = callable;
        }
        public final T getRawResult() { return result; }
        public final void setRawResult(T v) { result = v; }
        public final boolean exec() {
            try {
                result = callable.call();
                return true;
            } catch (Error err) {
                throw err;
            } catch (RuntimeException rex) {
                throw rex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 2838392045355241008L;
    }

    /**
     * 返回一个新的 {@code ForkJoinTask}，该任务执行给定的 {@code Runnable} 的 {@code run}
     * 方法作为其操作，并在 {@link #join} 时返回 null 结果。
     *
     * @param runnable 可运行的操作
     * @return 任务
     */
    public static ForkJoinTask<?> adapt(Runnable runnable) {
        return new AdaptedRunnableAction(runnable);
    }

    /**
     * 返回一个新的 {@code ForkJoinTask}，该任务执行给定的 {@code Runnable} 的 {@code run}
     * 方法作为其操作，并在 {@link #join} 时返回给定的结果。
     *
     * @param runnable 可运行的操作
     * @param result 完成时的结果
     * @param <T> 结果的类型
     * @return 任务
     */
    public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
        return new AdaptedRunnable<T>(runnable, result);
    }

    /**
     * 返回一个新的 {@code ForkJoinTask}，该任务执行给定的 {@code Callable} 的 {@code call}
     * 方法作为其操作，并在 {@link #join} 时返回其结果，将遇到的任何检查异常
     * 转换为 {@code RuntimeException}。
     *
     * @param callable 可调用的操作
     * @param <T> 可调用结果的类型
     * @return 任务
     */
    public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
        return new AdaptedCallable<T>(callable);
    }

                    // 序列化支持

    private static final long serialVersionUID = -7721805057305804111L;

    /**
     * 将此任务保存到流中（即，序列化它）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 当前运行状态和执行期间抛出的异常，如果没有则为 {@code null}
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        s.writeObject(getException());
    }

    /**
     * 从流中重新构建此任务（即，反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果找不到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object ex = s.readObject();
        if (ex != null)
            setExceptionalCompletion((Throwable)ex);
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe U;
    private static final long STATUS;

    static {
        exceptionTableLock = new ReentrantLock();
        exceptionTableRefQueue = new ReferenceQueue<Object>();
        exceptionTable = new ExceptionNode[EXCEPTION_MAP_CAPACITY];
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ForkJoinTask.class;
            STATUS = U.objectFieldOffset
                (k.getDeclaredField("status"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
