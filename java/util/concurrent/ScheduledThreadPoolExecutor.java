
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
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * 一个 {@link ThreadPoolExecutor}，可以额外安排在给定延迟后运行的命令，或定期执行。
 * 当需要多个工作线程时，或需要 {@link ThreadPoolExecutor}（此类扩展的类）提供的额外灵活性或功能时，
 * 该类优于 {@link java.util.Timer}。
 *
 * <p>延迟任务在启用后不会提前执行，但不提供何时开始执行的实时保证。对于设置为完全相同执行时间的任务，
 * 按提交的先进先出（FIFO）顺序启用。
 *
 * <p>当提交的任务在运行前被取消时，将抑制执行。默认情况下，这样的已取消任务不会自动从工作队列中移除，
 * 直到其延迟时间到期。虽然这允许进一步检查和监控，但可能导致已取消任务的无界保留。要避免这种情况，
 * 将 {@link #setRemoveOnCancelPolicy} 设置为 {@code true}，这将导致任务在取消时立即从工作队列中移除。
 *
 * <p>通过 {@code scheduleAtFixedRate} 或 {@code scheduleWithFixedDelay} 安排的任务的连续执行不会重叠。
 * 虽然不同的执行可能由不同的线程完成，但先前执行的效果 <a
 * href="package-summary.html#MemoryVisibility"><i>先于</i></a> 后续执行的效果。
 *
 * <p>虽然此类继承自 {@link ThreadPoolExecutor}，但继承的一些调整方法对它没有用处。特别是，由于它作为一个
 * 固定大小的池使用 {@code corePoolSize} 线程和一个无界队列，对 {@code maximumPoolSize} 的调整没有实际效果。
 * 此外，将 {@code corePoolSize} 设置为零或使用 {@code allowCoreThreadTimeOut} 几乎总是不好的主意，因为这可能
 * 导致池在任务变得可运行时没有线程来处理任务。
 *
 * <p><b>扩展说明：</b>此类重写了 {@link ThreadPoolExecutor#execute(Runnable) execute} 和
 * {@link AbstractExecutorService#submit(Runnable) submit} 方法以生成内部 {@link ScheduledFuture} 对象来控制每个任务的延迟和调度。
 * 为了保持功能，子类中的这些方法的任何进一步重写都必须调用超类版本，这实际上禁用了任务的进一步自定义。然而，此类提供了
 * 替代的受保护的扩展方法 {@code decorateTask}（每个 {@code Runnable} 和 {@code Callable} 有一个版本），
 * 可用于自定义通过 {@code execute}、{@code submit}、{@code schedule}、{@code scheduleAtFixedRate} 和
 * {@code scheduleWithFixedDelay} 方法进入的命令使用的具体任务类型。默认情况下，{@code ScheduledThreadPoolExecutor}
 * 使用一个扩展 {@link FutureTask} 的任务类型。但是，可以使用以下形式的子类来修改或替换此任务类型：
 *
 *  <pre> {@code
 * public class CustomScheduledExecutor extends ScheduledThreadPoolExecutor {
 *
 *   static class CustomTask<V> implements RunnableScheduledFuture<V> { ... }
 *
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Runnable r, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(r, task);
 *   }
 *
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Callable<V> c, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(c, task);
 *   }
 *   // ... 添加构造函数等
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ScheduledThreadPoolExecutor
        extends ThreadPoolExecutor
        implements ScheduledExecutorService {

    /*
     * 该类通过以下方式专门化 ThreadPoolExecutor 实现：
     *
     * 1. 使用自定义任务类型 ScheduledFutureTask 用于任务，即使那些不需要调度的任务（即，
     *    使用 ExecutorService execute 而不是 ScheduledExecutorService 方法提交的任务）也被视为具有零延迟的延迟任务。
     *
     * 2. 使用自定义队列（DelayedWorkQueue），它是无界 DelayQueue 的变体。缺乏容量限制以及
     *    corePoolSize 和 maximumPoolSize 实际上相同，简化了一些执行机制（参见 delayedExecute），
     *    与 ThreadPoolExecutor 相比。
     *
     * 3. 支持可选的关机后运行参数，这导致关机方法的重写以移除和取消不应在关机后运行的任务，以及
     *    任务（重新）提交与关机重叠时的不同重新检查逻辑。
     *
     * 4. 任务装饰方法以允许拦截和仪器化，因为子类不能以其他方式重写 submit 方法以获得这种效果。这些方法
     *    对池控制逻辑没有影响。
     */

    /**
     * 如果在关机后应取消/抑制周期性任务，则为 false。
     */
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;

    /**
     * 如果在关机后应取消非周期性任务，则为 false。
     */
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

    /**
     * 如果 ScheduledFutureTask.cancel 应从队列中移除，则为 true。
     */
    private volatile boolean removeOnCancel = false;

    /**
     * 用于打破调度关系的序列号，进而保证 FIFO 顺序。
     */
    private static final AtomicLong sequencer = new AtomicLong();

    /**
     * 返回当前纳秒时间。
     */
    final long now() {
        return System.nanoTime();
    }

    private class ScheduledFutureTask<V>
            extends FutureTask<V> implements RunnableScheduledFuture<V> {

        /** 用于打破 FIFO 关系的序列号 */
        private final long sequenceNumber;

        /** 任务启用执行的时间，以纳秒为单位 */
        private long time;

        /**
         * 重复任务的周期，以纳秒为单位。正值表示固定速率执行。负值表示固定延迟执行。0 表示非重复任务。
         */
        private final long period;

        /** 由 reExecutePeriodic 重新入队的实际任务 */
        RunnableScheduledFuture<V> outerTask = this;

        /**
         * 延迟队列中的索引，以支持更快的取消。
         */
        int heapIndex;

        /**
         * 使用给定的纳秒时间触发时间创建一次性动作。
         */
        ScheduledFutureTask(Runnable r, V result, long ns) {
            super(r, result);
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * 使用给定的纳秒时间和周期创建周期性动作。
         */
        ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            super(r, result);
            this.time = ns;
            this.period = period;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * 使用给定的纳秒时间触发时间创建一次性动作。
         */
        ScheduledFutureTask(Callable<V> callable, long ns) {
            super(callable);
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(time - now(), NANOSECONDS);
        }

        public int compareTo(Delayed other) {
            if (other == this) // 如果是同一个对象，则比较为零
                return 0;
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
                long diff = time - x.time;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
            return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
        }

        /**
         * 如果这是一个周期性（而不是一次性）动作，则返回 {@code true}。
         *
         * @return 如果是周期性的，则返回 {@code true}
         */
        public boolean isPeriodic() {
            return period != 0;
        }

        /**
         * 为周期性任务设置下次运行时间。
         */
        private void setNextRunTime() {
            long p = period;
            if (p > 0)
                time += p;
            else
                time = triggerTime(-p);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled && removeOnCancel && heapIndex >= 0)
                remove(this);
            return cancelled;
        }

        /**
         * 覆盖 FutureTask 版本，以便在周期性任务时重置/重新入队。
         */
        public void run() {
            boolean periodic = isPeriodic();
            if (!canRunInCurrentRunState(periodic))
                cancel(false);
            else if (!periodic)
                ScheduledFutureTask.super.run();
            else if (ScheduledFutureTask.super.runAndReset()) {
                setNextRunTime();
                reExecutePeriodic(outerTask);
            }
        }
    }

    /**
     * 如果当前运行状态和关机后运行参数允许运行任务，则返回 true。
     *
     * @param periodic 如果此任务是周期性的，则为 true，否则为 false
     */
    boolean canRunInCurrentRunState(boolean periodic) {
        return isRunningOrShutdown(periodic ?
                                   continueExistingPeriodicTasksAfterShutdown :
                                   executeExistingDelayedTasksAfterShutdown);
    }

    /**
     * 延迟或周期性任务的主要执行方法。如果池已关闭，则拒绝任务。否则将任务添加到队列中，
     * 必要时启动线程以运行任务。（我们不能预先启动线程来运行任务，因为任务（可能）还不应该运行。）
     * 如果在任务添加过程中池已关闭，则根据状态和关机后运行参数取消并移除任务。
     *
     * @param task 任务
     */
    private void delayedExecute(RunnableScheduledFuture<?> task) {
        if (isShutdown())
            reject(task);
        else {
            super.getQueue().add(task);
            if (isShutdown() &&
                !canRunInCurrentRunState(task.isPeriodic()) &&
                remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }

    /**
     * 除非当前运行状态不允许，否则重新入队周期性任务。与 delayedExecute 类似，但丢弃任务而不是拒绝。
     *
     * @param task 任务
     */
    void reExecutePeriodic(RunnableScheduledFuture<?> task) {
        if (canRunInCurrentRunState(true)) {
            super.getQueue().add(task);
            if (!canRunInCurrentRunState(true) && remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }

    /**
     * 取消并清除由于关机策略不应运行的所有任务队列。在 super.shutdown 内调用。
     */
    @Override void onShutdown() {
        BlockingQueue<Runnable> q = super.getQueue();
        boolean keepDelayed =
            getExecuteExistingDelayedTasksAfterShutdownPolicy();
        boolean keepPeriodic =
            getContinueExistingPeriodicTasksAfterShutdownPolicy();
        if (!keepDelayed && !keepPeriodic) {
            for (Object e : q.toArray())
                if (e instanceof RunnableScheduledFuture<?>)
                    ((RunnableScheduledFuture<?>) e).cancel(false);
            q.clear();
        }
        else {
            // 遍历快照以避免迭代器异常
            for (Object e : q.toArray()) {
                if (e instanceof RunnableScheduledFuture) {
                    RunnableScheduledFuture<?> t =
                        (RunnableScheduledFuture<?>)e;
                    if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) ||
                        t.isCancelled()) { // 如果已取消，也移除
                        if (q.remove(t))
                            t.cancel(false);
                    }
                }
            }
        }
        tryTerminate();
    }

    /**
     * 修改或替换用于执行可运行任务的任务。此方法可用于覆盖用于管理内部任务的具体类。
     * 默认实现仅返回给定的任务。
     *
     * @param runnable 提交的 Runnable
     * @param task 创建的用于执行可运行任务的任务
     * @param <V> 任务结果的类型
     * @return 可以执行可运行任务的任务
     * @since 1.6
     */
    protected <V> RunnableScheduledFuture<V> decorateTask(
        Runnable runnable, RunnableScheduledFuture<V> task) {
        return task;
    }


                /**
     * 修改或替换用于执行可调用任务的任务。
     * 此方法可用于覆盖用于管理内部任务的具体类。
     * 默认实现只是返回给定的任务。
     *
     * @param callable 提交的 Callable
     * @param task 用于执行可调用任务的任务
     * @param <V> 任务结果的类型
     * @return 可以执行可调用任务的任务
     * @since 1.6
     */
    protected <V> RunnableScheduledFuture<V> decorateTask(
        Callable<V> callable, RunnableScheduledFuture<V> task) {
        return task;
    }

    /**
     * 创建一个新的 {@code ScheduledThreadPoolExecutor}，具有给定的核心池大小。
     *
     * @param corePoolSize 保持在池中的线程数，即使它们是空闲的，除非设置了 {@code allowCoreThreadTimeOut}
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     */
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
    }

    /**
     * 创建一个新的 {@code ScheduledThreadPoolExecutor}，具有给定的初始参数。
     *
     * @param corePoolSize 保持在池中的线程数，即使它们是空闲的，除非设置了 {@code allowCoreThreadTimeOut}
     * @param threadFactory 当执行器创建新线程时使用的工厂
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     * @throws NullPointerException 如果 {@code threadFactory} 为 null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       ThreadFactory threadFactory) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue(), threadFactory);
    }

    /**
     * 创建一个新的 ScheduledThreadPoolExecutor，具有给定的初始参数。
     *
     * @param corePoolSize 保持在池中的线程数，即使它们是空闲的，除非设置了 {@code allowCoreThreadTimeOut}
     * @param handler 当执行被阻塞时使用的处理程序，因为线程限制和队列容量已达到
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     * @throws NullPointerException 如果 {@code handler} 为 null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue(), handler);
    }

    /**
     * 创建一个新的 ScheduledThreadPoolExecutor，具有给定的初始参数。
     *
     * @param corePoolSize 保持在池中的线程数，即使它们是空闲的，除非设置了 {@code allowCoreThreadTimeOut}
     * @param threadFactory 当执行器创建新线程时使用的工厂
     * @param handler 当执行被阻塞时使用的处理程序，因为线程限制和队列容量已达到
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     * @throws NullPointerException 如果 {@code threadFactory} 或 {@code handler} 为 null
     */
    public ScheduledThreadPoolExecutor(int corePoolSize,
                                       ThreadFactory threadFactory,
                                       RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue(), threadFactory, handler);
    }

    /**
     * 返回延迟操作的触发时间。
     */
    private long triggerTime(long delay, TimeUnit unit) {
        return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
    }

    /**
     * 返回延迟操作的触发时间。
     */
    long triggerTime(long delay) {
        return now() +
            ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    /**
     * 约束队列中所有延迟值在 Long.MAX_VALUE 之内，以避免在 compareTo 中溢出。
     * 这可能发生在某个任务有资格出队但尚未出队，而其他任务以 Long.MAX_VALUE 的延迟添加时。
     */
    private long overflowFree(long delay) {
        Delayed head = (Delayed) super.getQueue().peek();
        if (head != null) {
            long headDelay = head.getDelay(NANOSECONDS);
            if (headDelay < 0 && (delay - headDelay < 0))
                delay = Long.MAX_VALUE + headDelay;
        }
        return delay;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<?> t = decorateTask(command,
            new ScheduledFutureTask<Void>(command, null,
                                          triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit) {
        if (callable == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<V> t = decorateTask(callable,
            new ScheduledFutureTask<V>(callable,
                                       triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft =
            new ScheduledFutureTask<Void>(command,
                                          null,
                                          triggerTime(initialDelay, unit),
                                          unit.toNanos(period));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (delay <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft =
            new ScheduledFutureTask<Void>(command,
                                          null,
                                          triggerTime(initialDelay, unit),
                                          unit.toNanos(-delay));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }

    /**
     * 以零延迟执行 {@code command}。
     * 这相当于 {@link #schedule(Runnable,long,TimeUnit) schedule(command, 0, anyUnit)}。
     * 注意，对队列和 {@code shutdownNow} 返回的列表的检查将访问零延迟的 {@link ScheduledFuture}，而不是 {@code command} 本身。
     *
     * <p>使用 {@code ScheduledFuture} 对象的一个后果是，{@link ThreadPoolExecutor#afterExecute afterExecute} 总是
     * 被调用时第二个 {@code Throwable} 参数为 null，即使 {@code command} 突然终止。相反，此类任务抛出的 {@code Throwable}
     * 可以通过 {@link Future#get} 获得。
     *
     * @throws RejectedExecutionException 根据 {@code RejectedExecutionHandler} 的判断，如果任务
     *         不能被接受执行，因为执行器已关闭
     * @throws NullPointerException {@inheritDoc}
     */
    public void execute(Runnable command) {
        schedule(command, 0, NANOSECONDS);
    }

    // 覆盖 AbstractExecutorService 方法

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public Future<?> submit(Runnable task) {
        return schedule(task, 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(Executors.callable(task, result), 0, NANOSECONDS);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> Future<T> submit(Callable<T> task) {
        return schedule(task, 0, NANOSECONDS);
    }

    /**
     * 设置在执行器已 {@code shutdown} 时是否继续执行现有周期任务的策略。
     * 在这种情况下，这些任务只有在 {@code shutdownNow} 或在已关闭时将策略设置为
     * {@code false} 时才会终止。
     * 此值默认为 {@code false}。
     *
     * @param value 如果 {@code true}，则继续执行，否则不继续
     * @see #getContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown())
            onShutdown();
    }

    /**
     * 获取在执行器已 {@code shutdown} 时是否继续执行现有周期任务的策略。
     * 在这种情况下，这些任务只有在 {@code shutdownNow} 或在已关闭时将策略设置为
     * {@code false} 时才会终止。
     * 此值默认为 {@code false}。
     *
     * @return 如果将继续执行，则返回 {@code true}
     * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return continueExistingPeriodicTasksAfterShutdown;
    }

    /**
     * 设置在执行器已 {@code shutdown} 时是否执行现有延迟任务的策略。
     * 在这种情况下，这些任务只有在 {@code shutdownNow} 或在已关闭时将策略设置为
     * {@code false} 时才会终止。
     * 此值默认为 {@code true}。
     *
     * @param value 如果 {@code true}，则执行，否则不执行
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        executeExistingDelayedTasksAfterShutdown = value;
        if (!value && isShutdown())
            onShutdown();
    }

    /**
     * 获取在执行器已 {@code shutdown} 时是否执行现有延迟任务的策略。
     * 在这种情况下，这些任务只有在 {@code shutdownNow} 或在已关闭时将策略设置为
     * {@code false} 时才会终止。
     * 此值默认为 {@code true}。
     *
     * @return 如果将继续执行，则返回 {@code true}
     * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return executeExistingDelayedTasksAfterShutdown;
    }

    /**
     * 设置取消的任务是否应立即从工作队列中移除的策略。此值默认为 {@code false}。
     *
     * @param value 如果 {@code true}，则取消时立即移除，否则不移除
     * @see #getRemoveOnCancelPolicy
     * @since 1.7
     */
    public void setRemoveOnCancelPolicy(boolean value) {
        removeOnCancel = value;
    }

    /**
     * 获取取消的任务是否应立即从工作队列中移除的策略。此值默认为 {@code false}。
     *
     * @return 如果取消的任务立即从队列中移除，则返回 {@code true}
     * @see #setRemoveOnCancelPolicy
     * @since 1.7
     */
    public boolean getRemoveOnCancelPolicy() {
        return removeOnCancel;
    }

    /**
     * 开始有序关闭，在此过程中，先前提交的任务将被执行，但不会接受新的任务。
     * 如果已经关闭，则此调用没有额外的效果。
     *
     * <p>此方法不会等待先前提交的任务完成执行。使用 {@link #awaitTermination awaitTermination}
     * 来完成该操作。
     *
     * <p>如果已将 {@code ExecuteExistingDelayedTasksAfterShutdownPolicy} 设置为 {@code false}，
     * 则取消延迟尚未到期的现有延迟任务。并且除非已将 {@code
     * ContinueExistingPeriodicTasksAfterShutdownPolicy} 设置为 {@code true}，
     * 否则现有周期任务的未来执行将被取消。
     *
     * @throws SecurityException {@inheritDoc}
     */
    public void shutdown() {
        super.shutdown();
    }

    /**
     * 尝试停止所有正在积极执行的任务，停止等待任务的处理，并返回一个包含从未开始执行的任务的列表。
     *
     * <p>此方法不会等待正在积极执行的任务终止。使用 {@link #awaitTermination awaitTermination}
     * 来完成该操作。
     *
     * <p>除了尽力尝试停止正在积极执行的任务外，没有其他保证。此实现通过 {@link Thread#interrupt} 取消任务，
     * 因此任何不响应中断的任务可能永远不会终止。
     *
     * @return 从未开始执行的任务列表。
     *         列表中的每个元素都是一个 {@link ScheduledFuture}，
     *         包括使用 {@code execute} 提交的任务，这些任务在调度目的中用作零延迟的 {@code ScheduledFuture} 的基础。
     * @throws SecurityException {@inheritDoc}
     */
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }


                /**
                 * 返回此执行器使用的任务队列。队列中的每个元素都是一个 {@link ScheduledFuture}，包括使用 {@code execute} 提交的任务，这些任务在调度目的中用作零延迟 {@code ScheduledFuture} 的基础。遍历此队列不保证按任务将要执行的顺序进行。
                 *
                 * @return 任务队列
                 */
                public BlockingQueue<Runnable> getQueue() {
                    return super.getQueue();
                }

                /**
                 * 专门的延迟队列。为了与 TPE 声明匹配，此类必须声明为 BlockingQueue<Runnable>，即使它只能持有 RunnableScheduledFutures。
                 */
                static class DelayedWorkQueue extends AbstractQueue<Runnable>
                    implements BlockingQueue<Runnable> {

                    /*
                     * DelayedWorkQueue 基于类似于 DelayQueue 和 PriorityQueue 中的数据结构，但每个 ScheduledFutureTask 还记录了其在堆数组中的索引。这消除了在取消时查找任务的需要，大大加快了移除速度（从 O(n) 降低到 O(log n)），并减少了因等待元素上升到顶部而产生的垃圾保留。但由于队列中也可能包含不是 ScheduledFutureTasks 的 RunnableScheduledFutures，我们不能保证这些索引总是可用的，因此在这种情况下我们回退到线性搜索。（我们预计大多数任务不会被装饰，而且更快的情况会更常见。）
                     *
                     * 所有堆操作必须记录索引变化——主要在 siftUp 和 siftDown 中。移除时，任务的 heapIndex 被设置为 -1。注意，ScheduledFutureTasks 在队列中最多出现一次（这不一定是其他类型的任务或工作队列的情况），因此可以通过 heapIndex 唯一标识。
                     */

                    private static final int INITIAL_CAPACITY = 16;
                    private RunnableScheduledFuture<?>[] queue =
                        new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
                    private final ReentrantLock lock = new ReentrantLock();
                    private int size = 0;

                    /**
                     * 被指定等待队列头部任务的线程。这种变体的 Leader-Follower 模式（http://www.cs.wustl.edu/~schmidt/POSA/POSA2/）用于最小化不必要的定时等待。当一个线程成为领导者时，它只等待下一个延迟到期，但其他线程无限期等待。领导者线程必须在从 take() 或 poll(...) 返回之前信号其他线程，除非在此期间有其他线程成为领导者。每当队列头部被替换为具有更早到期时间的任务时，领导者字段通过被重置为 null 而失效，并且一些等待的线程（但不一定是当前领导者）被信号。因此，等待的线程必须准备好在等待时获取和失去领导权。
                     */
                    private Thread leader = null;

                    /**
                     * 当队列头部出现更新的任务或需要新的线程成为领导者时发出信号的条件。
                     */
                    private final Condition available = lock.newCondition();

                    /**
                     * 如果 f 是 ScheduledFutureTask，则设置其 heapIndex。
                     */
                    private void setIndex(RunnableScheduledFuture<?> f, int idx) {
                        if (f instanceof ScheduledFutureTask)
                            ((ScheduledFutureTask)f).heapIndex = idx;
                    }

                    /**
                     * 将底部添加的元素向上筛选到其堆有序位置。仅在持有锁时调用。
                     */
                    private void siftUp(int k, RunnableScheduledFuture<?> key) {
                        while (k > 0) {
                            int parent = (k - 1) >>> 1;
                            RunnableScheduledFuture<?> e = queue[parent];
                            if (key.compareTo(e) >= 0)
                                break;
                            queue[k] = e;
                            setIndex(e, k);
                            k = parent;
                        }
                        queue[k] = key;
                        setIndex(key, k);
                    }

                    /**
                     * 将顶部添加的元素向下筛选到其堆有序位置。仅在持有锁时调用。
                     */
                    private void siftDown(int k, RunnableScheduledFuture<?> key) {
                        int half = size >>> 1;
                        while (k < half) {
                            int child = (k << 1) + 1;
                            RunnableScheduledFuture<?> c = queue[child];
                            int right = child + 1;
                            if (right < size && c.compareTo(queue[right]) > 0)
                                c = queue[child = right];
                            if (key.compareTo(c) <= 0)
                                break;
                            queue[k] = c;
                            setIndex(c, k);
                            k = child;
                        }
                        queue[k] = key;
                        setIndex(key, k);
                    }

                    /**
                     * 调整堆数组的大小。仅在持有锁时调用。
                     */
                    private void grow() {
                        int oldCapacity = queue.length;
                        int newCapacity = oldCapacity + (oldCapacity >> 1); // 增长 50%
                        if (newCapacity < 0) // 溢出
                            newCapacity = Integer.MAX_VALUE;
                        queue = Arrays.copyOf(queue, newCapacity);
                    }

                    /**
                     * 查找给定对象的索引，如果不存在则返回 -1。
                     */
                    private int indexOf(Object x) {
                        if (x != null) {
                            if (x instanceof ScheduledFutureTask) {
                                int i = ((ScheduledFutureTask) x).heapIndex;
                                // 检查；x 可能是来自其他池的 ScheduledFutureTask。
                                if (i >= 0 && i < size && queue[i] == x)
                                    return i;
                            } else {
                                for (int i = 0; i < size; i++)
                                    if (x.equals(queue[i]))
                                        return i;
                            }
                        }
                        return -1;
                    }

                    public boolean contains(Object x) {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            return indexOf(x) != -1;
                        } finally {
                            lock.unlock();
                        }
                    }

                    public boolean remove(Object x) {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            int i = indexOf(x);
                            if (i < 0)
                                return false;

                            setIndex(queue[i], -1);
                            int s = --size;
                            RunnableScheduledFuture<?> replacement = queue[s];
                            queue[s] = null;
                            if (s != i) {
                                siftDown(i, replacement);
                                if (queue[i] == replacement)
                                    siftUp(i, replacement);
                            }
                            return true;
                        } finally {
                            lock.unlock();
                        }
                    }

                    public int size() {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            return size;
                        } finally {
                            lock.unlock();
                        }
                    }

                    public boolean isEmpty() {
                        return size() == 0;
                    }

                    public int remainingCapacity() {
                        return Integer.MAX_VALUE;
                    }

                    public RunnableScheduledFuture<?> peek() {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            return queue[0];
                        } finally {
                            lock.unlock();
                        }
                    }

                    public boolean offer(Runnable x) {
                        if (x == null)
                            throw new NullPointerException();
                        RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>)x;
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            int i = size;
                            if (i >= queue.length)
                                grow();
                            size = i + 1;
                            if (i == 0) {
                                queue[0] = e;
                                setIndex(e, 0);
                            } else {
                                siftUp(i, e);
                            }
                            if (queue[0] == e) {
                                leader = null;
                                available.signal();
                            }
                        } finally {
                            lock.unlock();
                        }
                        return true;
                    }

                    public void put(Runnable e) {
                        offer(e);
                    }

                    public boolean add(Runnable e) {
                        return offer(e);
                    }

                    public boolean offer(Runnable e, long timeout, TimeUnit unit) {
                        return offer(e);
                    }

                    /**
                     * 为 poll 和 take 执行通用簿记：用最后一个元素替换第一个元素并将其向下筛选。仅在持有锁时调用。
                     * @param f 要移除并返回的任务
                     */
                    private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
                        int s = --size;
                        RunnableScheduledFuture<?> x = queue[s];
                        queue[s] = null;
                        if (s != 0)
                            siftDown(0, x);
                        setIndex(f, -1);
                        return f;
                    }

                    public RunnableScheduledFuture<?> poll() {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            RunnableScheduledFuture<?> first = queue[0];
                            if (first == null || first.getDelay(NANOSECONDS) > 0)
                                return null;
                            else
                                return finishPoll(first);
                        } finally {
                            lock.unlock();
                        }
                    }

                    public RunnableScheduledFuture<?> take() throws InterruptedException {
                        final ReentrantLock lock = this.lock;
                        lock.lockInterruptibly();
                        try {
                            for (;;) {
                                RunnableScheduledFuture<?> first = queue[0];
                                if (first == null)
                                    available.await();
                                else {
                                    long delay = first.getDelay(NANOSECONDS);
                                    if (delay <= 0)
                                        return finishPoll(first);
                                    first = null; // 等待时不要保留引用
                                    if (leader != null)
                                        available.await();
                                    else {
                                        Thread thisThread = Thread.currentThread();
                                        leader = thisThread;
                                        try {
                                            available.awaitNanos(delay);
                                        } finally {
                                            if (leader == thisThread)
                                                leader = null;
                                        }
                                    }
                                }
                            }
                        } finally {
                            if (leader == null && queue[0] != null)
                                available.signal();
                            lock.unlock();
                        }
                    }

                    public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit)
                        throws InterruptedException {
                        long nanos = unit.toNanos(timeout);
                        final ReentrantLock lock = this.lock;
                        lock.lockInterruptibly();
                        try {
                            for (;;) {
                                RunnableScheduledFuture<?> first = queue[0];
                                if (first == null) {
                                    if (nanos <= 0)
                                        return null;
                                    else
                                        nanos = available.awaitNanos(nanos);
                                } else {
                                    long delay = first.getDelay(NANOSECONDS);
                                    if (delay <= 0)
                                        return finishPoll(first);
                                    if (nanos <= 0)
                                        return null;
                                    first = null; // 等待时不要保留引用
                                    if (nanos < delay || leader != null)
                                        nanos = available.awaitNanos(nanos);
                                    else {
                                        Thread thisThread = Thread.currentThread();
                                        leader = thisThread;
                                        try {
                                            long timeLeft = available.awaitNanos(delay);
                                            nanos -= delay - timeLeft;
                                        } finally {
                                            if (leader == thisThread)
                                                leader = null;
                                        }
                                    }
                                }
                            }
                        } finally {
                            if (leader == null && queue[0] != null)
                                available.signal();
                            lock.unlock();
                        }
                    }

                    public void clear() {
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            for (int i = 0; i < size; i++) {
                                RunnableScheduledFuture<?> t = queue[i];
                                if (t != null) {
                                    queue[i] = null;
                                    setIndex(t, -1);
                                }
                            }
                            size = 0;
                        } finally {
                            lock.unlock();
                        }
                    }

                    /**
                     * 仅在任务已过期时返回第一个元素。
                     * 仅由 drainTo 使用。仅在持有锁时调用。
                     */
                    private RunnableScheduledFuture<?> peekExpired() {
                        // assert lock.isHeldByCurrentThread();
                        RunnableScheduledFuture<?> first = queue[0];
                        return (first == null || first.getDelay(NANOSECONDS) > 0) ?
                            null : first;
                    }

                    public int drainTo(Collection<? super Runnable> c) {
                        if (c == null)
                            throw new NullPointerException();
                        if (c == this)
                            throw new IllegalArgumentException();
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            RunnableScheduledFuture<?> first;
                            int n = 0;
                            while ((first = peekExpired()) != null) {
                                c.add(first);   // 以这种顺序，以防 add() 抛出异常。
                                finishPoll(first);
                                ++n;
                            }
                            return n;
                        } finally {
                            lock.unlock();
                        }
                    }

                    public int drainTo(Collection<? super Runnable> c, int maxElements) {
                        if (c == null)
                            throw new NullPointerException();
                        if (c == this)
                            throw new IllegalArgumentException();
                        if (maxElements <= 0)
                            return 0;
                        final ReentrantLock lock = this.lock;
                        lock.lock();
                        try {
                            RunnableScheduledFuture<?> first;
                            int n = 0;
                            while (n < maxElements && (first = peekExpired()) != null) {
                                c.add(first);   // 以这种顺序，以防 add() 抛出异常。
                                finishPoll(first);
                                ++n;
                            }
                            return n;
                        } finally {
                            lock.unlock();
                        }
                    }


                    public Object[] toArray() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return Arrays.copyOf(queue, size, Object[].class);
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (a.length < size)
                    return (T[]) Arrays.copyOf(queue, size, a.getClass());
                System.arraycopy(queue, 0, a, 0, size);
                if (a.length > size)
                    a[size] = null;
                return a;
            } finally {
                lock.unlock();
            }
        }

        public Iterator<Runnable> iterator() {
            return new Itr(Arrays.copyOf(queue, size));
        }

        /**
         * 快照迭代器，基于底层 q 数组的副本工作。
         */
        private class Itr implements Iterator<Runnable> {
            final RunnableScheduledFuture<?>[] array;
            int cursor = 0;     // 下一个要返回的元素的索引
            int lastRet = -1;   // 上一个返回的元素的索引，如果没有则为 -1

            Itr(RunnableScheduledFuture<?>[] array) {
                this.array = array;
            }

            public boolean hasNext() {
                return cursor < array.length;
            }

            public Runnable next() {
                if (cursor >= array.length)
                    throw new NoSuchElementException();
                lastRet = cursor;
                return array[cursor++];
            }

            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                DelayedWorkQueue.this.remove(array[lastRet]);
                lastRet = -1;
            }
        }
    }
}
