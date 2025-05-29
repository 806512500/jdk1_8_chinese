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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 的说明发布到公共领域。
 */

package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;

/**
 * 可取消的异步计算。此类提供了 {@link Future} 的基本实现，具有启动和取消计算的方法，查询计算是否完成的方法，以及检索计算结果的方法。只有在计算完成后才能检索结果；如果计算尚未完成，{@code get}
 * 方法将阻塞。一旦计算完成，计算就不能被重新启动或取消（除非使用 {@link #runAndReset} 调用了计算）。
 *
 * <p>{@code FutureTask} 可以用来包装一个 {@link Callable} 或 {@link Runnable} 对象。因为 {@code FutureTask} 实现了
 * {@code Runnable}，所以可以将一个 {@code FutureTask} 提交给一个 {@link Executor} 以执行。
 *
 * <p>除了作为一个独立的类，此类还提供了可能在创建自定义任务类时有用的 {@code protected} 功能。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此 FutureTask 的 {@code get} 方法返回的结果类型
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
     * 修订说明：此版本与依赖于 AbstractQueuedSynchronizer 的早期版本不同，主要是为了避免在取消操作竞争期间保留中断状态给用户带来的意外。当前设计中的同步控制依赖于通过 CAS 更新的“状态”字段来跟踪完成情况，以及一个简单的 Treiber 栈来保存等待的线程。
     *
     * 风格说明：如常，我们绕过了使用 AtomicXFieldUpdaters 的开销，而是直接使用 Unsafe 内在函数。
     */

    /**
     * 该任务的运行状态，初始为 NEW。运行状态仅在 set、setException 和 cancel 方法中转换为终止状态。在完成过程中，状态可能会暂时变为 COMPLETING（当结果正在设置时）或
     * INTERRUPTING（仅在满足 cancel(true) 时中断运行者）。从这些中间状态到最终状态的转换使用更便宜的有序/懒惰写入，因为值是唯一的，且不能进一步修改。
     *
     * 可能的状态转换：
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    /** 底层的 callable；运行后置空 */
    private Callable<V> callable;
    /** 从 get() 返回的结果或抛出的异常 */
    private Object outcome; // 非 volatile，由状态读写保护
    /** 运行 callable 的线程；在 run() 期间通过 CAS 更新 */
    private volatile Thread runner;
    /** 等待线程的 Treiber 栈 */
    private volatile WaitNode waiters;

    /**
     * 返回已完成任务的结果或抛出异常。
     *
     * @param s 完成状态值
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }

    /**
     * 创建一个 {@code FutureTask}，该任务在运行时将执行给定的 {@code Callable}。
     *
     * @param  callable 可调用任务
     * @throws NullPointerException 如果 callable 为 null
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // 确保 callable 的可见性
    }

    /**
     * 创建一个 {@code FutureTask}，该任务在运行时将执行给定的 {@code Runnable}，并安排 {@code get} 在成功完成时返回给定的结果。
     *
     * @param runnable 可运行任务
     * @param result 在成功完成时返回的结果。如果不需要特定结果，可以考虑使用以下形式的构造：
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException 如果 runnable 为 null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // 确保 callable 的可见性
    }

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    public boolean isDone() {
        return state != NEW;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW &&
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                  mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        try {    // 以防中断调用时抛出异常
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // 最终状态
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * 受保护的方法，当此任务转换到完成状态（无论是正常完成还是被取消）时调用。默认实现不执行任何操作。子类可以重写此方法以调用完成回调或执行簿记。请注意，您可以在此方法的实现中查询状态以确定此任务是否已被取消。
     */
    protected void done() { }

    /**
     * 将此未来的值设置为给定值，除非此未来已经被设置或已被取消。
     *
     * <p>此方法在 {@link #run} 方法成功完成计算时内部调用。
     *
     * @param v 值
     */
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // 最终状态
            finishCompletion();
        }
    }

    /**
     * 使此未来报告一个带有给定异常作为原因的 {@link ExecutionException}，除非此未来已经被设置或已被取消。
     *
     * <p>此方法在 {@link #run} 方法计算失败时内部调用。
     *
     * @param t 失败的原因
     */
    protected void setException(Throwable t) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // 最终状态
            finishCompletion();
        }
    }

    public void run() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            // runner 必须在状态确定之前非空，以防止并发调用 run()
            runner = null;
            // 在将 runner 置为 null 后必须重新读取状态，以防止中断泄露
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * 执行计算而不设置其结果，然后将此未来重置为初始状态，如果计算遇到异常或被取消，则无法完成此操作。此方法设计用于本质上多次执行的任务。
     *
     * @return 如果成功运行并重置，则返回 {@code true}
     */
    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // 不设置结果
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner 必须在状态确定之前非空，以防止并发调用 run()
            runner = null;
            // 在将 runner 置为 null 后必须重新读取状态，以防止中断泄露
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * 确保来自可能的 cancel(true) 的任何中断仅在任务处于 run 或 runAndReset 时传递。
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // 我们的中断者可能在有机会中断我们之前停滞。让我们耐心地自旋等待。
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // 等待挂起的中断

        // assert state == INTERRUPTED;

        // 我们希望清除可能从 cancel(true) 收到的任何中断。但是，允许使用中断作为任务与调用者之间独立通信的机制，并且没有方法仅清除取消中断。
        //
        // Thread.interrupted();
    }

    /**
     * 简单的链表节点，用于在 Treiber 栈中记录等待线程。有关更详细的解释，请参见其他类，如 Phaser 和 SynchronousQueue。
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * 移除并通知所有等待线程，调用 done()，并使 callable 置为 null。
     */
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // 解链以帮助垃圾回收
                    q = next;
                }
                break;
            }
        }

        done();

        callable = null;    // 以减少内存占用
        
    }

    /**
     * 等待完成，或在中断或超时时中止。
     *
     * @param timed 如果使用定时等待，则为 true
     * @param nanos 如果定时，则等待的时间
     * @return 完成时的状态
     */
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            else if (s == COMPLETING) // 不能在此时超时
                Thread.yield();
            else if (q == null)
                q = new WaitNode();
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q);
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            else
                LockSupport.park(this);
        }
    }

    /**
     * 尝试解除超时或中断的等待节点的链接，以避免累积垃圾。内部节点只需简单地解开链接，而无需使用 CAS，因为即使它们仍然被释放者遍历也是无害的。
     * 为了避免从已移除的节点中解开链接的影响，如果出现明显的竞争情况，列表将重新遍历。当节点很多时，这会很慢，但我们不期望列表足够长到使更高开销的方案变得更有优势。
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // 在 removeWaiter 竞争时重新开始
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // 检查竞争
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
