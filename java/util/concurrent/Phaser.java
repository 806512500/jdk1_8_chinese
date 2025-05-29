
/*
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

/*
 *
 *
 *
 *
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
 * 一个可重用的同步屏障，功能类似于
 * {@link java.util.concurrent.CyclicBarrier CyclicBarrier} 和
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}
 * 但支持更灵活的使用。
 *
 * <p><b>注册。</b> 与其他屏障不同，注册到一个 Phaser 的任务数量
 * 可以随时间变化。任务可以在任何时候注册（使用方法 {@link #register}、
 * {@link #bulkRegister} 或构造函数中设置的初始任务数量），并在到达时
 * 选择注销（使用 {@link #arriveAndDeregister}）。与大多数基本同步构造类似，
 * 注册和注销仅影响内部计数；它们不会建立任何进一步的内部记录，因此任务不能查询它们是否已注册。
 * （但是，您可以通过继承此类来引入这样的记录。）
 *
 * <p><b>同步。</b> 与 {@code CyclicBarrier} 类似，一个 {@code
 * Phaser} 可以被多次等待。方法 {@link
 * #arriveAndAwaitAdvance} 的效果类似于 {@link
 * java.util.concurrent.CyclicBarrier#await CyclicBarrier.await}。每个
 * Phaser 的一代都有一个相关的阶段号。阶段号从零开始，当所有任务到达 Phaser 时，
 * 阶段号会前进，当达到 {@code Integer.MAX_VALUE} 时，阶段号会回绕到零。阶段号的使用
 * 使任务在到达 Phaser 时和等待其他任务时可以独立控制动作，通过两种类型的方法，
 * 这些方法可以由任何已注册的任务调用：
 *
 * <ul>
 *
 *   <li> <b>到达。</b> 方法 {@link #arrive} 和
 *       {@link #arriveAndDeregister} 记录到达。这些方法不会阻塞，但返回一个相关的
 *       <em>到达阶段号</em>；即，到达适用的 Phaser 的阶段号。当给定阶段的最后一个任务
 *       到达时，会执行一个可选的动作并且阶段前进。这些动作由触发阶段前进的任务执行，
 *       通过重写方法 {@link #onAdvance(int, int)} 来安排，该方法还控制终止。重写此方法
 *       类似于但比为 {@code CyclicBarrier} 提供一个屏障动作更灵活。
 *
 *   <li> <b>等待。</b> 方法 {@link #awaitAdvance} 需要一个参数表示到达阶段号，
 *       并在 Phaser 进入（或已经处于）不同的阶段时返回。与使用 {@code CyclicBarrier}
 *       的类似构造不同，方法 {@code awaitAdvance} 即使等待线程被中断也会继续等待。
 *       也提供了可中断和超时版本，但在任务等待时遇到的异常不会改变 Phaser 的状态。
 *       如果需要，您可以在这些异常的处理程序中执行任何相关的恢复操作，通常在调用
 *       {@code forceTermination} 之后。Phasers 也可以由在 {@link ForkJoinPool}
 *       中执行的任务使用，这将确保在其他任务因等待阶段前进而阻塞时有足够的并行性来执行任务。
 *
 * </ul>
 *
 * <p><b>终止。</b> 一个 Phaser 可能进入一个 <em>终止</em> 状态，可以使用方法
 * {@link #isTerminated} 检查。终止后，所有同步方法会立即返回而不等待阶段前进，如
 * 负返回值所示。同样，终止后尝试注册也不会产生效果。终止由调用 {@code onAdvance}
 * 返回 {@code true} 触发。默认实现当注销导致注册任务数量变为零时返回 {@code true}。
 * 如下所示，当 Phaser 控制具有固定迭代次数的动作时，通常方便重写此方法以在当前阶段号
 * 达到阈值时终止。方法 {@link #forceTermination} 也用于立即释放等待线程并允许它们终止。
 *
 * <p><b>分层。</b> Phaser 可以 <em>分层</em>（即，构造成树形结构）以减少争用。
 * 具有大量任务的 Phaser 可能会经历严重的同步争用成本，可以设置为子 Phaser 组共享
 * 一个公共父 Phaser。这可能会大大增加吞吐量，尽管它会增加每个操作的开销。
 *
 * <p>在分层 Phaser 树中，子 Phaser 与父 Phaser 的注册和注销由系统自动管理。
 * 每当子 Phaser 的注册任务数量变为非零（在 {@link #Phaser(Phaser,int)}
 * 构造函数、{@link #register} 或 {@link #bulkRegister} 中建立）时，子 Phaser
 * 会注册到其父 Phaser。每当注册任务数量因调用 {@link #arriveAndDeregister}
 * 而变为零时，子 Phaser 会从其父 Phaser 注销。
 *
 * <p><b>监控。</b> 虽然同步方法只能由已注册的任务调用，但任何调用者都可以监控 Phaser 的当前状态。
 * 在任何给定时刻，总共有 {@link #getRegisteredParties} 个任务，其中 {@link
 * #getArrivedParties} 个任务已到达当前阶段（{@link #getPhase}）。当剩余的
 * （{@link #getUnarrivedParties}）任务到达时，阶段前进。这些方法返回的值可能反映瞬态状态，
 * 因此通常不适用于同步控制。方法 {@link #toString} 返回这些状态查询的快照，便于非正式监控。
 *
 * <p><b>示例用法：</b>
 *
 * <p>一个 {@code Phaser} 可以用于代替 {@code CountDownLatch} 来控制服务可变数量任务的一次性动作。
 * 典型的用法是在设置此方法的代码中首先注册，然后启动动作，然后注销，如下所示：
 *
 *  <pre> {@code
 * void runTasks(List<Runnable> tasks) {
 *   final Phaser phaser = new Phaser(1); // "1" to register self
 *   // create and start threads
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         phaser.arriveAndAwaitAdvance(); // await all creation
 *         task.run();
 *       }
 *     }.start();
 *   }
 *
 *   // allow threads to start and deregister self
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 * <p>一种使一组线程重复执行给定次数迭代动作的方法是重写 {@code onAdvance}：
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
 *   phaser.arriveAndDeregister(); // deregister self, don't wait
 * }}</pre>
 *
 * 如果主任务稍后必须等待终止，它可以重新注册并执行类似的循环：
 *  <pre> {@code
 *   // ...
 *   phaser.register();
 *   while (!phaser.isTerminated())
 *     phaser.arriveAndAwaitAdvance();}</pre>
 *
 * <p>相关的构造可以用于在确定阶段号不会回绕 {@code Integer.MAX_VALUE} 的上下文中等待特定的阶段号。
 * 例如：
 *
 *  <pre> {@code
 * void awaitPhase(Phaser phaser, int phase) {
 *   int p = phaser.register(); // assumes caller not already registered
 *   while (p < phase) {
 *     if (phaser.isTerminated())
 *       // ... deal with unexpected termination
 *     else
 *       p = phaser.arriveAndAwaitAdvance();
 *   }
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 *
 * <p>要使用 Phaser 树创建一组 {@code n} 任务，您可以使用以下形式的代码，假设 Task 类
 * 有一个接受 {@code Phaser} 的构造函数，该构造函数在构造时注册。调用
 * {@code build(new Task[n], 0, n, new Phaser())} 后，这些任务可以启动，例如通过提交到池中：
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
 *       // assumes new Task(ph) performs ph.register()
 *   }
 * }}</pre>
 *
 * {@code TASKS_PER_PHASER} 的最佳值主要取决于预期的同步速率。对于每个阶段任务体非常小（因此速率高）的情况，
 * 值低至四可能合适，而对于任务体非常大的情况，值可以高达数百。
 *
 * <p><b>实现说明</b>：此实现限制最大任务数量为 65535。尝试注册更多任务会导致
 * {@code IllegalStateException}。但是，您可以并且应该创建分层的 Phaser 以容纳任意大的参与者集。
 *
 * @since 1.7
 * @author Doug Lea
 */
public class Phaser {
    /*
     * 该类实现了 X10 "clocks" 的扩展。感谢 Vijay Saraswat 提出这个想法，
     * 以及 Vivek Sarkar 对功能扩展的改进。
     */


    /**
     * 主状态表示，包含四个位字段：
     *
     * unarrived  -- 尚未到达屏障的参与方数量（位 0-15）
     * parties    -- 需要等待的参与方数量（位 16-31）
     * phase      -- 障碍的代（位 32-62）
     * terminated -- 如果屏障终止则设置（位 63 / 符号位）
     *
     * 除了没有注册参与方的障碍器，通过非法状态（即没有参与方但有一个未到达的参与方，如下文中的 EMPTY 编码）来区分。
     *
     * 为了高效地保持原子性，这些值被打包到一个单一的（原子）长整型中。良好的性能依赖于保持状态解码和编码的简单性，以及保持竞争窗口的短暂性。
     *
     * 所有状态更新都是通过 CAS 完成的，除了子障碍器（即具有非空父障碍器的障碍器）的初始注册。在这种（相对罕见的）情况下，我们在首次注册时使用内置同步锁定其父障碍器。
     *
     * 子障碍器的代可以落后于其祖先的代，直到实际访问时为止——参见方法 reconcileState。
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
     * 此障碍器的父障碍器，如果没有则为 null
     */
    private final Phaser parent;

    /**
     * 障碍器树的根。如果不是树的一部分，则等于 this。
     */
    private final Phaser root;

    /**
     * 等待线程的 Treiber 栈的头。为了在释放一些线程时添加其他线程时消除竞争，我们使用两个栈，交替使用偶数和奇数代。
     * 子障碍器与根共享队列以加快释放速度。
     */
    private final AtomicReference<QNode> evenQ;
    private final AtomicReference<QNode> oddQ;

    private AtomicReference<QNode> queueFor(int phase) {
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }

    /**
     * 返回到达时边界异常的消息字符串。
     */
    private String badArrive(long s) {
        return "尝试未注册的参与方到达 " +
            stateToString(s);
    }

    /**
     * 返回注册时边界异常的消息字符串。
     */
    private String badRegister(long s) {
        return "尝试注册超过 " +
            MAX_PARTIES + " 个参与方 " + stateToString(s);
    }

    /**
     * 方法 arrive 和 arriveAndDeregister 的主要实现。手动调整以加速并最小化常见情况下仅减少未到达字段的竞争窗口。
     *
     * @param adjust 从状态中减去的值；
     *               ONE_ARRIVAL 对于 arrive，
     *               ONE_DEREGISTER 对于 arriveAndDeregister
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
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s -= adjust)) {
                if (unarrived == 1) {
                    long n = s & PARTIES_MASK;  // 下一个状态的基
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
     * 方法 register 和 bulkRegister 的实现
     *
     * @param registrations 要添加到参与方和未到达字段的数量。必须大于零。
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
            if (counts != EMPTY) {                  // 不是第一次注册
                if (parent == null || reconcileState() == s) {
                    if (unarrived == 0)             // 等待前进
                        root.internalAwaitAdvance(phase, null);
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset,
                                                       s, s + adjust))
                        break;
                }
            }
            else if (parent == null) {              // 第一次根注册
                long next = ((long)phase << PHASE_SHIFT) | adjust;
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))
                    break;
            }
            else {
                synchronized (this) {               // 第一次子注册
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
     * 如果必要，从根解决滞后阶段传播。
     * 通常在根已经前进但子阶段器尚未前进时进行协调，在这种情况下，它们必须通过将未到达的设置为参与者（或如果参与者数为零，则重置为未注册的空状态）来完成自己的前进。
     *
     * @return 协调后的状态
     */
    private long reconcileState() {
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, p;
            // 使用当前参与者数通过CAS将阶段设置为根阶段，触发未到达
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
     * 创建一个没有初始注册参与者的、没有父级的、初始阶段号为0的阶段器。任何使用此阶段器的线程都需要先注册。
     */
    public Phaser() {
        this(null, 0);
    }

    /**
     * 创建一个具有给定数量的未到达注册参与者、没有父级、初始阶段号为0的阶段器。
     *
     * @param parties 进入下一阶段所需的参与者数量
     * @throws IllegalArgumentException 如果参与者数量小于零或大于支持的最大参与者数量
     */
    public Phaser(int parties) {
        this(null, parties);
    }

    /**
     * 等同于 {@link #Phaser(Phaser, int) Phaser(parent, 0)}。
     *
     * @param parent 父级阶段器
     */
    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    /**
     * 创建一个具有给定父级和未到达注册参与者数量的阶段器。当给定的父级不为null且给定的参与者数量大于零时，此子阶段器将注册到其父级。
     *
     * @param parent 父级阶段器
     * @param parties 进入下一阶段所需的参与者数量
     * @throws IllegalArgumentException 如果参与者数量小于零或大于支持的最大参与者数量
     */
    public Phaser(Phaser parent, int parties) {
        if (parties >>> PARTIES_SHIFT != 0)
            throw new IllegalArgumentException("不合法的参与者数量");
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
     * 向此阶段器添加一个新的未到达参与者。如果正在进行 {@link #onAdvance} 调用，此方法可能会等待其完成后再返回。如果此阶段器有父级，并且此阶段器之前没有注册的参与者，此子阶段器也将注册到其父级。如果此阶段器已终止，注册尝试将无效，并返回负值。
     *
     * @return 此注册适用的到达阶段号。如果此值为负，则此阶段器已终止，注册无效。
     * @throws IllegalStateException 如果尝试注册的参与者数量超过支持的最大数量
     */
    public int register() {
        return doRegister(1);
    }

    /**
     * 向此阶段器添加给定数量的新未到达参与者。如果正在进行 {@link #onAdvance} 调用，此方法可能会等待其完成后再返回。如果此阶段器有父级，并且给定的参与者数量大于零，且此阶段器之前没有注册的参与者，此子阶段器也将注册到其父级。如果此阶段器已终止，注册尝试将无效，并返回负值。
     *
     * @param parties 进入下一阶段所需的额外参与者数量
     * @return 此注册适用的到达阶段号。如果此值为负，则此阶段器已终止，注册无效。
     * @throws IllegalStateException 如果尝试注册的参与者数量超过支持的最大数量
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
     * 到达此阶段器，不等待其他参与者到达。
     *
     * <p>未注册的参与者调用此方法是使用错误。然而，此错误可能仅在后续对此阶段器的操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段号，或如果已终止则为负值
     * @throws IllegalStateException 如果未终止且未到达的参与者数量将变为负值
     */
    public int arrive() {
        return doArrive(ONE_ARRIVAL);
    }

    /**
     * 到达此阶段器并从其注销，不等待其他参与者到达。注销会减少未来阶段中所需的参与者数量。如果此阶段器有父级，并且注销导致此阶段器的参与者数量为零，此阶段器也将从其父级注销。
     *
     * <p>未注册的参与者调用此方法是使用错误。然而，此错误可能仅在后续对此阶段器的操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段号，或如果已终止则为负值
     * @throws IllegalStateException 如果未终止且注册或未到达的参与者数量将变为负值
     */
    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }


    /**
     * 到达此 Phaser 并等待其他方。效果等同于 {@code awaitAdvance(arrive())}。如果需要带中断或超时等待，
     * 可以使用 {@code awaitAdvance} 方法的其他形式构造类似的方法。如果到达时需要注销，使用 {@code awaitAdvance(arriveAndDeregister())}。
     *
     * <p>未注册的方调用此方法是使用错误。然而，此错误可能仅在后续对此 Phaser 的某些操作中导致 {@code
     * IllegalStateException}，如果有的话。
     *
     * @return 到达的阶段号，或如果已终止则返回（负的）{@linkplain #getPhase() 当前阶段}
     * @throws IllegalStateException 如果未终止且未到达方的数量变为负数
     */
    public int arriveAndAwaitAdvance() {
        // 优化的 doArrive+awaitAdvance，消除了一些读取/路径
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
                    return (int)(state >>> PHASE_SHIFT); // 已终止
                releaseWaiters(phase);
                return nextPhase;
            }
        }
    }

    /**
     * 等待此 Phaser 的阶段从给定的阶段值推进，如果当前阶段不等于给定的阶段值或此 Phaser 已终止，则立即返回。
     *
     * @param phase 到达的阶段号，或如果已终止则为负值；此参数通常是由先前调用 {@code arrive} 或 {@code arriveAndDeregister} 返回的值。
     * @return 下一个到达的阶段号，或如果参数为负则返回参数，或如果已终止则返回（负的）{@linkplain #getPhase() 当前阶段}
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
     * 等待此 Phaser 的阶段从给定的阶段值推进，如果等待时被中断则抛出 {@code InterruptedException}，
     * 或如果当前阶段不等于给定的阶段值或此 Phaser 已终止，则立即返回。
     *
     * @param phase 到达的阶段号，或如果已终止则为负值；此参数通常是由先前调用 {@code arrive} 或 {@code arriveAndDeregister} 返回的值。
     * @return 下一个到达的阶段号，或如果参数为负则返回参数，或如果已终止则返回（负的）{@linkplain #getPhase() 当前阶段}
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
     * 等待此 Phaser 的阶段从给定的阶段值推进或给定的超时时间到期，如果等待时被中断则抛出 {@code
     * InterruptedException}，或如果当前阶段不等于给定的阶段值或此 Phaser 已终止，则立即返回。
     *
     * @param phase 到达的阶段号，或如果已终止则为负值；此参数通常是由先前调用 {@code arrive} 或 {@code arriveAndDeregister} 返回的值。
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 下一个到达的阶段号，或如果参数为负则返回参数，或如果已终止则返回（负的）{@linkplain #getPhase() 当前阶段}
     * @throws InterruptedException 如果等待时线程被中断
     * @throws TimeoutException 如果等待时超时
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
     * 强制此 Phaser 进入终止状态。注册的参与方数量不受影响。如果此 Phaser 是分层 Phaser 集合的成员，则该集合中的所有 Phaser 都将终止。如果此 Phaser 已经终止，则此方法无效。此方法可用于在一项或多项任务遇到意外异常后协调恢复。
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
     * 返回当前的阶段号。最大阶段号为 {@code Integer.MAX_VALUE}，之后从零重新开始。终止时，阶段号为负数，此时可以通过 {@code getPhase() + Integer.MIN_VALUE} 获取终止前的阶段号。
     *
     * @return 阶段号，或如果已终止则为负数
     */
    public final int getPhase() {
        return (int)(root.state >>> PHASE_SHIFT);
    }

    /**
     * 返回在此 Phaser 上注册的参与方数量。
     *
     * @return 参与方数量
     */
    public int getRegisteredParties() {
        return partiesOf(state);
    }

    /**
     * 返回已到达此 Phaser 当前阶段的注册参与方数量。如果此 Phaser 已终止，则返回的值没有意义且任意。
     *
     * @return 已到达的参与方数量
     */
    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }

    /**
     * 返回尚未到达此 Phaser 当前阶段的注册参与方数量。如果此 Phaser 已终止，则返回的值没有意义且任意。
     *
     * @return 未到达的参与方数量
     */
    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }

    /**
     * 返回此 Phaser 的父级，如果没有则返回 {@code null}。
     *
     * @return 此 Phaser 的父级，如果没有则返回 {@code null}
     */
    public Phaser getParent() {
        return parent;
    }

    /**
     * 返回此 Phaser 的根祖先，如果没有父级则为自身。
     *
     * @return 此 Phaser 的根祖先
     */
    public Phaser getRoot() {
        return root;
    }

    /**
     * 如果此 Phaser 已终止，则返回 {@code true}。
     *
     * @return 如果此 Phaser 已终止，则返回 {@code true}
     */
    public boolean isTerminated() {
        return root.state < 0L;
    }

    /**
     * 可重写的方法，在即将推进阶段时执行操作并控制终止。当推进此 Phaser 的参与方到达时（其他所有等待的参与方处于休眠状态），将调用此方法。如果此方法返回 {@code true}，则此 Phaser 将在推进时设置为最终终止状态，后续调用 {@link #isTerminated} 将返回 true。此方法抛出的任何（未检查的）异常或错误将传播到尝试推进此 Phaser 的参与方，此时不会发生推进。
     *
     * <p>此方法的参数提供了当前转换的 Phaser 状态。在此方法内调用到达、注册和等待方法的效果是未指定的，不应依赖。
     *
     * <p>如果此 Phaser 是分层 Phaser 集合的成员，则每次推进时仅为其根 Phaser 调用 {@code onAdvance}。
     *
     * <p>为了支持最常见的用例，默认实现此方法在参与方调用 {@code arriveAndDeregister} 后注册的参与方数量变为零时返回 {@code true}。可以通过重写此方法始终返回 {@code false} 来禁用此行为，从而允许未来的注册继续：
     *
     * <pre> {@code
     * Phaser phaser = new Phaser() {
     *   protected boolean onAdvance(int phase, int parties) { return false; }
     * }}</pre>
     *
     * @param phase 进入此方法时的当前阶段号，在此 Phaser 推进之前
     * @param registeredParties 当前注册的参与方数量
     * @return 如果此 Phaser 应终止，则返回 {@code true}
     */
    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    /**
     * 返回一个标识此 Phaser 及其状态的字符串。状态（用方括号括起来）包括字符串 {@code "phase = "} 后跟阶段号，{@code "parties = "} 后跟注册的参与方数量，以及 {@code "arrived = "} 后跟已到达的参与方数量。
     *
     * @return 一个标识此 Phaser 及其状态的字符串
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
     * 从队列中移除并通知线程，用于阶段。
     */
    private void releaseWaiters(int phase) {
        QNode q;   // 队列的第一个元素
        Thread t;  // 其线程
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
     * 释放等待者的一种变体，同时尝试移除由于超时或中断而不再等待前进的节点。目前，只有当节点位于队列头部时才会被移除，这足以在大多数使用情况下减少内存占用。
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

    /** CPU的数量，用于自旋控制 */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * 在等待前进时，每次到达前自旋的次数。在多处理器上，同时阻塞和唤醒大量线程通常是一个非常慢的过程，因此我们使用可充电的自旋来避免这种情况，当线程定期到达时：当内部等待前进的线程在阻塞前注意到另一个到达，并且似乎有足够的可用CPU时，它会再自旋 SPINS_PER_ARRIVAL 次然后阻塞。该值在良好的公民行为和不必要的大减速之间进行了权衡。
     */
    static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;

    /**
     * 可能会阻塞并等待阶段前进，除非被中止。仅在根阶段器上调用。
     *
     * @param phase 当前阶段
     * @param node 如果非空，则是用于跟踪中断和超时的等待节点；如果为空，则表示不可中断的等待
     * @return 当前阶段
     */
    private int internalAwaitAdvance(int phase, QNode node) {
        // assert root == this;
        releaseWaiters(phase-1);          // 确保旧队列已清理
        boolean queued = false;           // 当节点已入队时为 true
        int lastUnarrived = 0;            // 用于在变化时增加自旋次数
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
                if (interrupted || --spins < 0) { // 需要节点来记录中断
                    node = new QNode(this, phase, false, false, 0L);
                    node.wasInterrupted = interrupted;
                }
            }
            else if (node.isReleasable()) // 已完成或已中止
                break;
            else if (!queued) {           // 推入队列
                AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
                QNode q = node.next = head.get();
                if ((q == null || q.phase == phase) &&
                    (int)(state >>> PHASE_SHIFT) == phase) // 避免陈旧入队
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
     * 用于 Treiber 栈表示等待队列的等待节点
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
}
