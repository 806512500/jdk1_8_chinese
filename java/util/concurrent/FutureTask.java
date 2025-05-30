
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
import java.util.concurrent.locks.LockSupport;

/**
 * 一个可取消的异步计算。此类提供了一个 {@link Future} 的基本实现，具有启动和取消计算、查询计算是否完成以及检索计算结果的方法。只有在计算完成时才能检索结果；如果计算尚未完成，{@code get} 方法将阻塞。一旦计算完成，计算就不能重新启动或取消（除非使用 {@link #runAndReset} 调用计算）。
 *
 * <p>{@code FutureTask} 可以用于包装 {@link Callable} 或 {@link Runnable} 对象。因为 {@code FutureTask} 实现了 {@code Runnable}，所以可以将其提交给 {@link Executor} 以执行。
 *
 * <p>除了作为独立类使用外，此类还提供了可能在创建自定义任务类时有用的 {@code protected} 功能。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此 FutureTask 的 {@code get} 方法返回的结果类型
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
     * 修订说明：此版本与依赖于 AbstractQueuedSynchronizer 的早期版本不同，主要是为了避免在取消竞争期间保留中断状态时给用户带来意外。当前设计中的同步控制依赖于通过 CAS 更新的 "state" 字段来跟踪完成情况，以及一个简单的 Treiber 栈来保存等待线程。
     *
     * 风格说明：与往常一样，我们绕过了使用 AtomicXFieldUpdaters 的开销，而是直接使用 Unsafe 内在函数。
     */

    /**
     * 此任务的运行状态，初始为 NEW。运行状态仅在方法 set、setException 和 cancel 中转换为终止状态。在完成期间，状态可能会暂时变为 COMPLETING（当结果正在设置时）或 INTERRUPTING（仅在满足 cancel(true) 时中断运行器）。从中间状态到最终状态的转换使用更便宜的有序/懒惰写入，因为值是唯一的，不能进一步修改。
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
    private Object outcome; // 非 volatile，受 state 读写保护
    /** 运行 callable 的线程；在 run() 期间通过 CAS 设置 */
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
     * 创建一个 {@code FutureTask}，在运行时将执行给定的 {@code Callable}。
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
     * 创建一个 {@code FutureTask}，在运行时将执行给定的 {@code Runnable}，并安排在成功完成时 {@code get} 返回给定的结果。
     *
     * @param runnable 可运行任务
     * @param result 在成功完成时返回的结果。如果不需要特定结果，可以考虑使用以下形式：
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
     * 当此任务转换到状态 {@code isDone}（无论是正常还是通过取消）时调用的受保护方法。默认实现不执行任何操作。子类可以重写此方法以调用完成回调或执行簿记。注意，可以在此方法的实现中查询状态以确定此任务是否已被取消。
     */
    protected void done() { }

    /**
     * 将此未来的结设置为给定值，除非此未来已被设置或已被取消。
     *
     * <p>此方法由 {@link #run} 方法在计算成功完成时内部调用。
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
     * 使此未来报告一个 {@link ExecutionException}，其原因是由给定的异常，除非此未来已被设置或已被取消。
     *
     * <p>此方法由 {@link #run} 方法在计算失败时内部调用。
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
            // 在置空 runner 后必须重新读取状态，以防止中断泄露
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * 执行计算而不设置其结果，然后将此未来重置为初始状态，如果计算遇到异常或被取消，则失败。此方法设计用于本质上多次执行的任务。
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
            // 在置空 runner 后必须重新读取状态，以防止中断泄露
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * 确保从可能的 cancel(true) 收到的中断仅在任务处于 run 或 runAndReset 时传递。
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // 可能会出现中断者在有机会中断我们之前停滞的情况。让我们耐心地自旋等待。
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // 等待待处理的中断

        // assert state == INTERRUPTED;

        // 我们希望清除从 cancel(true) 收到的任何中断。然而，允许使用中断作为任务与其调用者之间独立通信的机制，没有方法可以仅清除取消中断。
        //
        // Thread.interrupted();
    }

    /**
     * 用于在 Treiber 栈中记录等待线程的简单链表节点。有关更详细解释，请参见其他类如 Phaser 和 SynchronousQueue。
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * 移除并通知所有等待线程，调用 done()，并置空 callable。
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

        callable = null;        // 以减少内存占用
    }

    /**
     * 等待完成或在中断或超时时中止。
     *
     * @param timed 如果使用定时等待，则为 true
     * @param nanos 如果定时，则等待时间
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
            else if (s == COMPLETING) // 不能超时
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
     * 尝试解除一个超时或中断的等待节点的链接，以避免垃圾积累。内部节点只需简单地解开，无需CAS，因为即使它们无论如何都会被释放者遍历，这也是无害的。为了避免从已经移除的节点解开的影响，如果出现竞争情况，列表将重新遍历。当节点很多时，这会很慢，但我们不期望列表长到足以使更高开销的方案更有优势。
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // 重新开始于 removeWaiter 竞争
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
