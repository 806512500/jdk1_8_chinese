
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个可重用的同步屏障，功能类似于 {@link java.util.concurrent.CyclicBarrier CyclicBarrier} 和
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}，但支持更灵活的使用。
 *
 * <p><b>注册。</b> 与其它屏障不同，同步到一个 phaser 的 <em>注册</em> 任务数量可以随时间变化。任务可以在任何时候注册（使用
 * 方法 {@link #register}、{@link #bulkRegister} 或者构造函数中设置的初始任务数量），并且在任何到达时选择注销（使用
 * {@link #arriveAndDeregister}）。与大多数基本同步构造一样，注册和注销只影响内部计数；它们不建立任何进一步的内部记录，因此任务不能查询它们是否已注册。
 * （但是，你可以通过继承此类来引入这样的记录。）
 *
 * <p><b>同步。</b> 类似于 {@code CyclicBarrier}，一个 {@code Phaser} 可以被反复等待。方法 {@link
 * #arriveAndAwaitAdvance} 的效果类似于 {@link java.util.concurrent.CyclicBarrier#await CyclicBarrier.await}。
 * 每个 phaser 代有一个关联的阶段号。阶段号从零开始，当所有任务到达 phaser 时，阶段号会前进，超过 {@code
 * Integer.MAX_VALUE} 后会重置为零。阶段号的使用使独立控制到达 phaser 时和等待其他任务时的动作成为可能，通过两种类型的方法，这些方法可以由任何已注册的任务调用：
 *
 * <ul>
 *
 *   <li> <b>到达。</b> 方法 {@link #arrive} 和
 *       {@link #arriveAndDeregister} 记录到达。这些方法不会阻塞，但返回一个关联的 <em>到达阶段号</em>；即，到达所应用的 phaser 的阶段号。
 *       当给定阶段的最后一个任务到达时，会执行一个可选的动作并且阶段前进。这些动作由触发阶段前进的任务执行，并通过覆盖方法 {@link #onAdvance(int, int)} 来安排，
 *       该方法还控制终止。覆盖此方法类似于但比为 {@code CyclicBarrier} 提供屏障动作更灵活。
 *
 *   <li> <b>等待。</b> 方法 {@link #awaitAdvance} 需要一个参数，表示到达阶段号，并在 phaser 进入（或已经处于）不同阶段时返回。
 *       与使用 {@code CyclicBarrier} 的类似构造不同，方法 {@code awaitAdvance} 即使等待线程被中断也会继续等待。还提供了可中断和超时版本，
 *       但任务在等待时遇到的异常不会改变 phaser 的状态。如果需要，你可以在这些异常的处理程序中执行任何相关的恢复操作，通常是在调用 {@code forceTermination} 之后。
 *       当任务在 {@link ForkJoinPool} 中执行时，也可以使用 phaser，这将确保在其他任务因等待阶段前进而阻塞时有足够的并行性来执行任务。
 *
 * </ul>
 *
 * <p><b>终止。</b> phaser 可能进入一个 <em>终止</em> 状态，可以通过方法 {@link #isTerminated} 检查。终止后，所有同步方法会立即返回而不会等待阶段前进，如负返回值所示。
 * 同样，终止后尝试注册也没有效果。终止由 {@code onAdvance} 方法的调用触发，当该方法返回 {@code true} 时。默认实现当注销导致注册任务数量变为零时返回 {@code
 * true}。如下面的示例所示，当 phaser 控制具有固定迭代次数的动作时，通常方便覆盖此方法以在当前阶段号达到阈值时终止。方法 {@link #forceTermination} 也可用于立即释放等待线程并允许它们终止。
 *
 * <p><b>分层。</b> phaser 可以 <em>分层</em>（即，构建为树结构）以减少争用。具有大量任务的 phaser，如果直接同步会导致高争用成本，可以设置为共享一个公共父 phaser 的子 phaser 组。
 * 这可能会大大增加吞吐量，尽管它增加了每次操作的开销。
 *
 * <p>在分层 phaser 的树中，子 phaser 与父 phaser 的注册和注销是自动管理的。每当子 phaser 的注册任务数量变为非零（在 {@link #Phaser(Phaser,int)}
 * 构造函数、{@link #register} 或 {@link #bulkRegister} 中建立），子 phaser 会注册到其父 phaser。每当注册任务数量因调用 {@link #arriveAndDeregister} 而变为零时，
 * 子 phaser 会从其父 phaser 注销。
 *
 * <p><b>监控。</b> 虽然同步方法只能由已注册的任务调用，但 phaser 的当前状态可以由任何调用者监控。在任何给定时刻，总共有 {@link
 * #getRegisteredParties} 个任务，其中 {@link #getArrivedParties} 个任务已到达当前阶段（{@link #getPhase}）。当剩余的（{@link
 * #getUnarrivedParties}）任务到达时，阶段会前进。这些方法返回的值可能反映瞬时状态，因此通常不适用于同步控制。方法 {@link #toString} 以方便非正式监控的形式返回这些状态查询的快照。
 *
 * <p><b>示例用法：</b>
 *
 * <p>{@code Phaser} 可以用于代替 {@code CountDownLatch} 来控制一个服务可变数量任务的一次性动作。典型的用法是在设置此操作的方法中首先注册，然后启动动作，最后注销，如下所示：
 *
 *  <pre> {@code
 * void runTasks(List<Runnable> tasks) {
 *   final Phaser phaser = new Phaser(1); // "1" 表示注册自身
 *   // 创建并启动线程
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         phaser.arriveAndAwaitAdvance(); // 等待所有创建
 *         task.run();
 *       }
 *     }.start();
 *   }
 *
 *   // 允许线程启动并注销自身
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 * <p>一种使一组线程重复执行给定次数迭代的方法是覆盖 {@code onAdvance}：
 *
 *  <pre> {@code
 * void startTasks(List<Runnable> tasks, final int iterations) {
 *   final Phaser phaser = new Phaser() {
 *     protected boolean onAdvance(int phase, int registeredParties) {
 *       return phase >= iterations || registeredParties == 0;
 *     }
 *   };
 *   phaser.register();
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         do {
 *           task.run();
 *           phaser.arriveAndAwaitAdvance();
 *         } while (!phaser.isTerminated());
 *       }
 *     }.start();
 *   }
 *   phaser.arriveAndDeregister(); // 注销自身，不等待
 * }}</pre>
 *
 * 如果主任务稍后必须等待终止，它可以重新注册并执行类似的循环：
 *  <pre> {@code
 *   // ...
 *   phaser.register();
 *   while (!phaser.isTerminated())
 *     phaser.arriveAndAwaitAdvance();}</pre>
 *
 * <p>相关的构造可以在确保阶段号永远不会绕过 {@code Integer.MAX_VALUE} 的上下文中等待特定的阶段号。例如：
 *
 *  <pre> {@code
 * void awaitPhase(Phaser phaser, int phase) {
 *   int p = phaser.register(); // 假设调用者尚未注册
 *   while (p < phase) {
 *     if (phaser.isTerminated())
 *       // ... 处理意外终止
 *     else
 *       p = phaser.arriveAndAwaitAdvance();
 *   }
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 *
 * <p>要使用 phaser 树创建一组 {@code n} 个任务，可以使用以下形式的代码，假设 Task 类有一个接受 {@code Phaser} 的构造函数，该构造函数在构造时注册。调用 {@code build(new Task[n], 0, n,
 * new Phaser())} 后，这些任务可以启动，例如通过提交到池中：
 *
 *  <pre> {@code
 * void build(Task[] tasks, int lo, int hi, Phaser ph) {
 *   if (hi - lo > TASKS_PER_PHASER) {
 *     for (int i = lo; i < hi; i += TASKS_PER_PHASER) {
 *       int j = Math.min(i + TASKS_PER_PHASER, hi);
 *       build(tasks, i, j, new Phaser(ph));
 *     }
 *   } else {
 *     for (int i = lo; i < hi; ++i)
 *       tasks[i] = new Task(ph);
 *       // 假设 new Task(ph) 执行 ph.register()
 *   }
 * }}</pre>
 *
 * {@code TASKS_PER_PHASER} 的最佳值主要取决于预期的同步速率。对于每个阶段任务体非常小（因此速率高）的情况，值低至四可能合适，或者对于非常大的任务体，值可达数百。
 *
 * <p><b>实现说明</b>：此实现限制最大任务数量为 65535。尝试注册额外任务会导致 {@code IllegalStateException}。但是，你可以并且应该创建分层 phaser 以容纳任意大的参与者集。
 *
 * @since 1.7
 * @author Doug Lea
 */
public class Phaser {
    /*
     * 该类实现了 X10 "clocks" 的扩展。感谢 Vijay Saraswat 提出这个想法，感谢 Vivek Sarkar 提出增强功能以扩展功能。
     */

    /**
     * 主要状态表示，包含四个位字段：
     *
     * unarrived  -- 尚未到达屏障的任务数量（位 0-15）
     * parties    -- 要等待的任务数量（位 16-31）
     * phase      -- 障碍的代数（位 32-62）
     * terminated -- 如果屏障已终止，则设置（位 63 / 符号）
     *
     * 除了没有注册任务的 phaser 通过非法状态（unarrived 为 1，parties 为 0，表示 EMPTY）来区分。
     *
     * 为了高效地保持原子性，这些值被打包到一个（原子）long 中。良好的性能依赖于保持状态解码和编码的简单性，并缩短竞态窗口。
     *
     * 所有状态更新都通过 CAS 完成，除了子 phaser（即，具有非空父 phaser）的初始注册。在这种（相对罕见的）情况下，我们使用内置同步在首次注册时锁定。
     *
     * 子 phaser 的阶段允许滞后于其祖先的阶段，直到实际访问时为止——参见方法 reconcileState。
     */
    private volatile long state;

    private static final int  MAX_PARTIES     = 0xffff;
    private static final int  MAX_PHASE       = Integer.MAX_VALUE;
    private static final int  PARTIES_SHIFT   = 16;
    private static final int  PHASE_SHIFT     = 32;
    private static final int  UNARRIVED_MASK  = 0xffff;      // 用于掩码 int
    private static final long PARTIES_MASK    = 0xffff0000L; // 用于掩码 long
    private static final long COUNTS_MASK     = 0xffffffffL;
    private static final long TERMINATION_BIT = 1L << 63;

    // 一些特殊值
    private static final int  ONE_ARRIVAL     = 1;
    private static final int  ONE_PARTY       = 1 << PARTIES_SHIFT;
    private static final int  ONE_DEREGISTER  = ONE_ARRIVAL | ONE_PARTY;
    private static final int  EMPTY           = 1;

    // 以下解包方法通常手动内联

    private static int unarrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
    }

    private static int partiesOf(long s) {
        return (int)s >>> PARTIES_SHIFT;
    }

    private static int phaseOf(long s) {
        return (int)(s >>> PHASE_SHIFT);
    }

    private static int arrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 :
            (counts >>> PARTIES_SHIFT) - (counts & UNARRIVED_MASK);
    }

    /**
     * 该 phaser 的父 phaser，如果没有则为 null
     */
    private final Phaser parent;

    /**
     * phaser 树的根。如果不是树的一部分，则等于 this。
     */
    private final Phaser root;

    /**
     * 等待线程的 Treiber 栈头。为了在释放一些线程时添加其他线程时消除争用，我们使用两个栈，交替在偶数和奇数阶段使用。
     * 子 phaser 与根共享队列以加快释放速度。
     */
    private final AtomicReference<QNode> evenQ;
    private final AtomicReference<QNode> oddQ;

    private AtomicReference<QNode> queueFor(int phase) {
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }

    /**
     * 返回到达时的边界异常消息字符串。
     */
    private String badArrive(long s) {
        return "尝试未注册任务的到达 " +
            stateToString(s);
    }

    /**
     * 返回注册时的边界异常消息字符串。
     */
    private String badRegister(long s) {
        return "尝试注册超过 " +
            MAX_PARTIES + " 个任务 " + stateToString(s);
    }


                /**
     * 主要实现方法 arrive 和 arriveAndDeregister。
     * 手动优化以加速并最小化常见情况下仅减少 unarrived 字段的竞争窗口。
     *
     * @param adjust 从状态中减去的值；
     *               ONE_ARRIVAL 用于 arrive，
     *               ONE_DEREGISTER 用于 arriveAndDeregister
     */
    private int doArrive(int adjust) {
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                return phase;
            int counts = (int)s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s-=adjust)) {
                if (unarrived == 1) {
                    long n = s & PARTIES_MASK;  // 下一个状态的基值
                    int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                    if (root == this) {
                        if (onAdvance(phase, nextUnarrived))
                            n |= TERMINATION_BIT;
                        else if (nextUnarrived == 0)
                            n |= EMPTY;
                        else
                            n |= nextUnarrived;
                        int nextPhase = (phase + 1) & MAX_PHASE;
                        n |= (long)nextPhase << PHASE_SHIFT;
                        UNSAFE.compareAndSwapLong(this, stateOffset, s, n);
                        releaseWaiters(phase);
                    }
                    else if (nextUnarrived == 0) { // 传播注销
                        phase = parent.doArrive(ONE_DEREGISTER);
                        UNSAFE.compareAndSwapLong(this, stateOffset,
                                                  s, s | EMPTY);
                    }
                    else
                        phase = parent.doArrive(ONE_ARRIVAL);
                }
                return phase;
            }
        }
    }

    /**
     * 实现 register 和 bulkRegister
     *
     * @param registrations 要添加到 parties 和 unarrived 字段的值。必须大于零。
     */
    private int doRegister(int registrations) {
        // 状态调整
        long adjust = ((long)registrations << PARTIES_SHIFT) | registrations;
        final Phaser parent = this.parent;
        int phase;
        for (;;) {
            long s = (parent == null) ? state : reconcileState();
            int counts = (int)s;
            int parties = counts >>> PARTIES_SHIFT;
            int unarrived = counts & UNARRIVED_MASK;
            if (registrations > MAX_PARTIES - parties)
                throw new IllegalStateException(badRegister(s));
            phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                break;
            if (counts != EMPTY) {                  // 非首次注册
                if (parent == null || reconcileState() == s) {
                    if (unarrived == 0)             // 等待前进
                        root.internalAwaitAdvance(phase, null);
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset,
                                                       s, s + adjust))
                        break;
                }
            }
            else if (parent == null) {              // 根的首次注册
                long next = ((long)phase << PHASE_SHIFT) | adjust;
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))
                    break;
            }
            else {
                synchronized (this) {               // 子的首次注册
                    if (state == s) {               // 在锁下重新检查
                        phase = parent.doRegister(1);
                        if (phase < 0)
                            break;
                        // 当父注册成功时完成注册，即使与终止竞争，
                        // 因为这些是同一“事务”的一部分。
                        while (!UNSAFE.compareAndSwapLong
                               (this, stateOffset, s,
                                ((long)phase << PHASE_SHIFT) | adjust)) {
                            s = state;
                            phase = (int)(root.state >>> PHASE_SHIFT);
                            // assert (int)s == EMPTY;
                        }
                        break;
                    }
                }
            }
        }
        return phase;
    }

    /**
     * 如果必要，解析从根传播的滞后阶段。
     * 通常情况下，当根已前进但子 phaser 尚未这样做时，它们必须通过将 unarrived 设置为 parties（或如果 parties 为零，则重置为未注册的 EMPTY 状态）来完成自己的前进。
     *
     * @return 解析后的状态
     */
    private long reconcileState() {
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, p;
            // 使用当前 parties 将阶段 CAS 到根阶段，触发 unarrived
            while ((phase = (int)(root.state >>> PHASE_SHIFT)) !=
                   (int)(s >>> PHASE_SHIFT) &&
                   !UNSAFE.compareAndSwapLong
                   (this, stateOffset, s,
                    s = (((long)phase << PHASE_SHIFT) |
                         ((phase < 0) ? (s & COUNTS_MASK) :
                          (((p = (int)s >>> PARTIES_SHIFT) == 0) ? EMPTY :
                           ((s & PARTIES_MASK) | p))))))
                s = state;
        }
        return s;
    }

    /**
     * 创建一个没有初始注册方、没有父 phaser 且初始阶段编号为 0 的新 phaser。任何使用此 phaser 的线程都需要首先注册。
     */
    public Phaser() {
        this(null, 0);
    }

    /**
     * 创建一个具有给定数量的已注册未到达方、没有父 phaser 且初始阶段编号为 0 的新 phaser。
     *
     * @param parties 前进到下一阶段所需的方数
     * @throws IllegalArgumentException 如果 parties 小于零或大于支持的最大方数
     */
    public Phaser(int parties) {
        this(null, parties);
    }

    /**
     * 等同于 {@link #Phaser(Phaser, int) Phaser(parent, 0)}。
     *
     * @param parent 父 phaser
     */
    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    /**
     * 创建一个具有给定父 phaser 和已注册未到达方数量的新 phaser。当给定的父 phaser 非空且给定的方数大于零时，此子 phaser 会注册到其父 phaser。
     *
     * @param parent 父 phaser
     * @param parties 前进到下一阶段所需的方数
     * @throws IllegalArgumentException 如果 parties 小于零或大于支持的最大方数
     */
    public Phaser(Phaser parent, int parties) {
        if (parties >>> PARTIES_SHIFT != 0)
            throw new IllegalArgumentException("非法的方数");
        int phase = 0;
        this.parent = parent;
        if (parent != null) {
            final Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0)
                phase = parent.doRegister(1);
        }
        else {
            this.root = this;
            this.evenQ = new AtomicReference<QNode>();
            this.oddQ = new AtomicReference<QNode>();
        }
        this.state = (parties == 0) ? (long)EMPTY :
            ((long)phase << PHASE_SHIFT) |
            ((long)parties << PARTIES_SHIFT) |
            ((long)parties);
    }

    /**
     * 向此 phaser 添加一个新未到达方。如果正在进行 {@link #onAdvance} 调用，此方法可能会等待其完成后再返回。如果此 phaser 有父 phaser，并且此 phaser 之前没有注册方，此子 phaser 也会注册到其父 phaser。如果此 phaser 已终止，注册尝试无效，并返回负值。
     *
     * @return 此注册适用的到达阶段编号。如果此值为负，则此 phaser 已终止，注册无效。
     * @throws IllegalStateException 如果尝试注册的方数超过支持的最大方数
     */
    public int register() {
        return doRegister(1);
    }

    /**
     * 向此 phaser 添加给定数量的新未到达方。如果正在进行 {@link #onAdvance} 调用，此方法可能会等待其完成后再返回。如果此 phaser 有父 phaser，且给定的方数大于零，并且此 phaser 之前没有注册方，此子 phaser 也会注册到其父 phaser。如果此 phaser 已终止，注册尝试无效，并返回负值。
     *
     * @param parties 前进到下一阶段所需的额外方数
     * @return 此注册适用的到达阶段编号。如果此值为负，则此 phaser 已终止，注册无效。
     * @throws IllegalStateException 如果尝试注册的方数超过支持的最大方数
     * @throws IllegalArgumentException 如果 {@code parties < 0}
     */
    public int bulkRegister(int parties) {
        if (parties < 0)
            throw new IllegalArgumentException();
        if (parties == 0)
            return getPhase();
        return doRegister(parties);
    }

    /**
     * 到达此 phaser，不等待其他方到达。
     *
     * <p>未注册的方调用此方法是使用错误。然而，此错误可能仅在后续对 phaser 的某些操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段编号，或终止时的负值
     * @throws IllegalStateException 如果未终止且未到达的方数将变为负值
     */
    public int arrive() {
        return doArrive(ONE_ARRIVAL);
    }

    /**
     * 到达此 phaser 并从其注销，不等待其他方到达。注销会减少未来阶段中所需的方数。如果此 phaser 有父 phaser，且注销导致此 phaser 没有方，此 phaser 也会从其父 phaser 注销。
     *
     * <p>未注册的方调用此方法是使用错误。然而，此错误可能仅在后续对 phaser 的某些操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段编号，或终止时的负值
     * @throws IllegalStateException 如果未终止且注册或未到达的方数将变为负值
     */
    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    /**
     * 到达此 phaser 并等待其他方。效果等同于 {@code awaitAdvance(arrive())}。如果需要带有中断或超时的等待，可以使用 {@code
     * awaitAdvance} 方法的其他形式之一进行类似的构造。如果需要在到达时注销，使用 {@code awaitAdvance(arriveAndDeregister())}。
     *
     * <p>未注册的方调用此方法是使用错误。然而，此错误可能仅在后续对 phaser 的某些操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段编号，或终止时的（负）{@linkplain #getPhase() 当前阶段}
     * @throws IllegalStateException 如果未终止且未到达的方数将变为负值
     */
    public int arriveAndAwaitAdvance() {
        // doArrive+awaitAdvance 的特化版本，消除了一些读取/路径
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                return phase;
            int counts = (int)s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s,
                                          s -= ONE_ARRIVAL)) {
                if (unarrived > 1)
                    return root.internalAwaitAdvance(phase, null);
                if (root != this)
                    return parent.arriveAndAwaitAdvance();
                long n = s & PARTIES_MASK;  // 下一个状态的基值
                int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                if (onAdvance(phase, nextUnarrived))
                    n |= TERMINATION_BIT;
                else if (nextUnarrived == 0)
                    n |= EMPTY;
                else
                    n |= nextUnarrived;
                int nextPhase = (phase + 1) & MAX_PHASE;
                n |= (long)nextPhase << PHASE_SHIFT;
                if (!UNSAFE.compareAndSwapLong(this, stateOffset, s, n))
                    return (int)(state >>> PHASE_SHIFT); // 终止
                releaseWaiters(phase);
                return nextPhase;
            }
        }
    }

    /**
     * 等待此 phaser 的阶段从给定的阶段值前进，如果当前阶段不等于给定的阶段值或此 phaser 已终止，则立即返回。
     *
     * @param phase 一个到达阶段编号，或终止时的负值；此参数通常是 {@code arrive} 或 {@code arriveAndDeregister} 的返回值。
     * @return 下一个到达阶段编号，或参数为负时返回参数，或终止时的（负）{@linkplain #getPhase() 当前阶段}
     */
    public int awaitAdvance(int phase) {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase)
            return root.internalAwaitAdvance(phase, null);
        return p;
    }

    /**
     * 等待此 phaser 的阶段从给定的阶段值前进，如果等待时被中断则抛出 {@code InterruptedException}，或如果当前阶段不等于给定的阶段值或此 phaser 已终止，则立即返回。
     *
     * @param phase 一个到达阶段编号，或终止时的负值；此参数通常是 {@code arrive} 或 {@code arriveAndDeregister} 的返回值。
     * @return 下一个到达阶段编号，或参数为负时返回参数，或终止时的（负）{@linkplain #getPhase() 当前阶段}
     * @throws InterruptedException 如果等待时线程被中断
     */
    public int awaitAdvanceInterruptibly(int phase)
        throws InterruptedException {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, 0L);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
        }
        return p;
    }


/**
 * 等待此屏障的阶段从给定的阶段值或给定的超时时间过去，如果在等待过程中被中断，则抛出 {@code
 * InterruptedException}，或者如果当前阶段不等于给定的阶段值或此屏障已终止，则立即返回。
 *
 * @param phase 一个到达的阶段编号，或负值表示已终止；此参数通常是 {@code arrive} 或 {@code arriveAndDeregister} 方法返回的值。
 * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
 * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
 * @return 下一个到达的阶段编号，或如果参数为负值则返回参数，或如果已终止则返回（负的）{@linkplain #getPhase() 当前阶段}
 * @throws InterruptedException 如果线程在等待过程中被中断
 * @throws TimeoutException 如果在等待过程中超时
 */
public int awaitAdvanceInterruptibly(int phase,
                                     long timeout, TimeUnit unit)
    throws InterruptedException, TimeoutException {
    long nanos = unit.toNanos(timeout);
    final Phaser root = this.root;
    long s = (root == this) ? state : reconcileState();
    int p = (int)(s >>> PHASE_SHIFT);
    if (phase < 0)
        return phase;
    if (p == phase) {
        QNode node = new QNode(this, phase, true, true, nanos);
        p = root.internalAwaitAdvance(phase, node);
        if (node.wasInterrupted)
            throw new InterruptedException();
        else if (p == phase)
            throw new TimeoutException();
    }
    return p;
}

/**
 * 强制此屏障进入终止状态。注册的方数量不受影响。如果此屏障是分层屏障集的成员，则集中的所有屏障都将终止。如果此屏障已终止，则此方法无效。此方法可用于在一个或多个任务遇到意外异常后协调恢复。
 */
public void forceTermination() {
    // 只需更改根状态
    final Phaser root = this.root;
    long s;
    while ((s = root.state) >= 0) {
        if (UNSAFE.compareAndSwapLong(root, stateOffset,
                                      s, s | TERMINATION_BIT)) {
            // 信号所有线程
            releaseWaiters(0); // 偶数队列上的等待者
            releaseWaiters(1); // 奇数队列上的等待者
            return;
        }
    }
}

/**
 * 返回当前阶段编号。最大阶段编号为 {@code Integer.MAX_VALUE}，之后从零重新开始。终止时，阶段编号为负值，此时可以通过 {@code getPhase() + Integer.MIN_VALUE} 获取终止前的阶段。
 *
 * @return 阶段编号，或如果已终止则为负值
 */
public final int getPhase() {
    return (int)(root.state >>> PHASE_SHIFT);
}

/**
 * 返回在此屏障注册的方数量。
 *
 * @return 方的数量
 */
public int getRegisteredParties() {
    return partiesOf(state);
}

/**
 * 返回已到达当前阶段的注册方数量。如果此屏障已终止，则返回的值无意义且任意。
 *
 * @return 已到达的方数量
 */
public int getArrivedParties() {
    return arrivedOf(reconcileState());
}

/**
 * 返回尚未到达当前阶段的注册方数量。如果此屏障已终止，则返回的值无意义且任意。
 *
 * @return 未到达的方数量
 */
public int getUnarrivedParties() {
    return unarrivedOf(reconcileState());
}

/**
 * 返回此屏障的父屏障，如果不存在则返回 {@code null}。
 *
 * @return 此屏障的父屏障，如果不存在则返回 {@code null}
 */
public Phaser getParent() {
    return parent;
}

/**
 * 返回此屏障的根祖先，如果它没有父屏障，则根祖先就是它自己。
 *
 * @return 此屏障的根祖先
 */
public Phaser getRoot() {
    return root;
}

/**
 * 如果此屏障已终止，则返回 {@code true}。
 *
 * @return 如果此屏障已终止，则返回 {@code true}
 */
public boolean isTerminated() {
    return root.state < 0L;
}

/**
 * 可重写的方法，在即将推进阶段时执行操作，并控制终止。此方法在推进此屏障的方到达时被调用（当所有其他等待的方处于休眠状态时）。如果此方法返回 {@code
 * true}，此屏障将在推进时进入最终终止状态，后续调用 {@link #isTerminated} 将返回 true。任何（未检查的）异常或错误由调用此方法的方抛出，此时不会发生推进。
 *
 * <p>此方法的参数提供了当前转换的屏障状态。在此方法内调用到达、注册和等待方法的效果是未指定的，不应依赖。
 *
 * <p>如果此屏障是分层屏障集的成员，则 {@code onAdvance} 仅在每个推进时为根屏障调用。
 *
 * <p>为了支持最常见的用例，默认实现此方法在注册的方数量变为零时返回 {@code true}，这是由于方调用 {@code arriveAndDeregister}。可以通过重写此方法始终返回 {@code false} 来禁用此行为，从而允许未来的注册继续：
 *
 * <pre> {@code
 * Phaser phaser = new Phaser() {
 *   protected boolean onAdvance(int phase, int parties) { return false; }
 * }}</pre>
 *
 * @param phase 进入此方法时的当前阶段编号，在此屏障推进之前
 * @param registeredParties 当前注册的方数量
 * @return 如果此屏障应终止，则返回 {@code true}
 */
protected boolean onAdvance(int phase, int registeredParties) {
    return registeredParties == 0;
}

/**
 * 返回一个标识此屏障及其状态的字符串。状态（用方括号表示）包括字符串 {@code
 * "phase = "} 后跟阶段编号，{@code "parties = "} 后跟注册的方数量，以及 {@code
 * "arrived = "} 后跟已到达的方数量。
 *
 * @return 一个标识此屏障及其状态的字符串
 */
public String toString() {
    return stateToString(reconcileState());
}

/**
 * 实现 toString 和基于字符串的错误消息
 */
private String stateToString(long s) {
    return super.toString() +
        "[phase = " + phaseOf(s) +
        " parties = " + partiesOf(s) +
        " arrived = " + arrivedOf(s) + "]";
}

// 等待机制

/**
 * 从队列中移除并信号阶段的线程。
 */
private void releaseWaiters(int phase) {
    QNode q;   // 队列的第一个元素
    Thread t;  // 它的线程
    AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
    while ((q = head.get()) != null &&
           q.phase != (int)(root.state >>> PHASE_SHIFT)) {
        if (head.compareAndSet(q, q.next) &&
            (t = q.thread) != null) {
            q.thread = null;
            LockSupport.unpark(t);
        }
    }
}

/**
 * 释放等待者的变体，另外尝试移除由于超时或中断而不再等待推进的任何节点。目前，仅当节点位于队列头部时才移除节点，这足以减少大多数用例中的内存占用。
 *
 * @return 退出时的当前阶段
 */
private int abortWait(int phase) {
    AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
    for (;;) {
        Thread t;
        QNode q = head.get();
        int p = (int)(root.state >>> PHASE_SHIFT);
        if (q == null || ((t = q.thread) != null && q.phase == p))
            return p;
        if (head.compareAndSet(q, q.next) && t != null) {
            q.thread = null;
            LockSupport.unpark(t);
        }
    }
}

/** CPU 的数量，用于自旋控制 */
private static final int NCPU = Runtime.getRuntime().availableProcessors();

/**
 * 在等待推进时，每次到达前自旋的次数。在多处理器上，一次性完全阻塞和唤醒大量线程通常是一个非常慢的过程，因此我们使用可充电的自旋来避免这种情况，当在内部等待推进时，如果线程注意到在阻塞前有其他到达，并且看起来有足够的 CPU 可用，它将自旋更多次。该值权衡了良好的公民行为与大的不必要的减速。
 */
static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;

/**
 * 可能会阻塞并等待阶段推进，除非被中止。仅在根屏障上调用。
 *
 * @param phase 当前阶段
 * @param node 如果非空，则是用于跟踪中断和超时的等待节点；如果为空，表示不可中断的等待
 * @return 当前阶段
 */
private int internalAwaitAdvance(int phase, QNode node) {
    // assert root == this;
    releaseWaiters(phase-1);          // 确保旧队列干净
    boolean queued = false;           // 真表示节点已入队
    int lastUnarrived = 0;            // 用于在变化时增加自旋
    int spins = SPINS_PER_ARRIVAL;
    long s;
    int p;
    while ((p = (int)((s = state) >>> PHASE_SHIFT)) == phase) {
        if (node == null) {           // 在不可中断模式下自旋
            int unarrived = (int)s & UNARRIVED_MASK;
            if (unarrived != lastUnarrived &&
                (lastUnarrived = unarrived) < NCPU)
                spins += SPINS_PER_ARRIVAL;
            boolean interrupted = Thread.interrupted();
            if (interrupted || --spins < 0) { // 需要节点记录中断
                node = new QNode(this, phase, false, false, 0L);
                node.wasInterrupted = interrupted;
            }
        }
        else if (node.isReleasable()) // 完成或中止
            break;
        else if (!queued) {           // 推入队列
            AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
            QNode q = node.next = head.get();
            if ((q == null || q.phase == phase) &&
                (int)(state >>> PHASE_SHIFT) == phase) // 避免过时的入队
                queued = head.compareAndSet(q, node);
        }
        else {
            try {
                ForkJoinPool.managedBlock(node);
            } catch (InterruptedException ie) {
                node.wasInterrupted = true;
            }
        }
    }

    if (node != null) {
        if (node.thread != null)
            node.thread = null;       // 避免需要唤醒
        if (node.wasInterrupted && !node.interruptible)
            Thread.currentThread().interrupt();
        if (p == phase && (p = (int)(state >>> PHASE_SHIFT)) == phase)
            return abortWait(phase); // 可能在中止时清理
    }
    releaseWaiters(phase);
    return p;
}

/**
 * Treiber 栈表示等待队列的等待节点
 */
static final class QNode implements ForkJoinPool.ManagedBlocker {
    final Phaser phaser;
    final int phase;
    final boolean interruptible;
    final boolean timed;
    boolean wasInterrupted;
    long nanos;
    final long deadline;
    volatile Thread thread; // 取消等待时置为 null
    QNode next;

    QNode(Phaser phaser, int phase, boolean interruptible,
          boolean timed, long nanos) {
        this.phaser = phaser;
        this.phase = phase;
        this.interruptible = interruptible;
        this.nanos = nanos;
        this.timed = timed;
        this.deadline = timed ? System.nanoTime() + nanos : 0L;
        thread = Thread.currentThread();
    }

    public boolean isReleasable() {
        if (thread == null)
            return true;
        if (phaser.getPhase() != phase) {
            thread = null;
            return true;
        }
        if (Thread.interrupted())
            wasInterrupted = true;
        if (wasInterrupted && interruptible) {
            thread = null;
            return true;
        }
        if (timed) {
            if (nanos > 0L) {
                nanos = deadline - System.nanoTime();
            }
            if (nanos <= 0L) {
                thread = null;
                return true;
            }
        }
        return false;
    }

    public boolean block() {
        if (isReleasable())
            return true;
        else if (!timed)
            LockSupport.park(this);
        else if (nanos > 0L)
            LockSupport.parkNanos(this, nanos);
        return isReleasable();
    }
}

// Unsafe 机制

private static final sun.misc.Unsafe UNSAFE;
private static final long stateOffset;
static {
    try {
        UNSAFE = sun.misc.Unsafe.getUnsafe();
        Class<?> k = Phaser.class;
        stateOffset = UNSAFE.objectFieldOffset
            (k.getDeclaredField("state"));
    } catch (Exception e) {
        throw new Error(e);
    }
}
