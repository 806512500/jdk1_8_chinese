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
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * 一个 {@link ExecutorService}，使用一个或多个线程池中的线程执行每个提交的任务，通常使用 {@link Executors} 工厂方法配置。
 *
 * <p>线程池解决了两个不同的问题：通常在执行大量异步任务时提供改进的性能，因为减少了每个任务的调用开销，
 * 并且它们提供了一种限制和管理执行任务集合时消耗的资源（包括线程）的方法。
 * 每个 {@code ThreadPoolExecutor} 还维护一些基本统计信息，例如已完成任务的数量。
 *
 * <p>为了在广泛的上下文中发挥作用，此类提供了许多可调参数和扩展挂钩。
 * 但是，鼓励程序员使用更方便的 {@link Executors} 工厂方法 {@link
 * Executors#newCachedThreadPool}（无界线程池，具有自动线程回收功能），{@link Executors#newFixedThreadPool}
 * （固定大小的线程池）和 {@link
 * Executors#newSingleThreadExecutor}（单个后台线程），这些方法预配置了最常见的使用场景。
 * 否则，在手动配置和调整此类时，请使用以下指南：
 *
 * <dl>
 *
 * <dt>核心和最大池大小</dt>
 *
 * <dd>{@code ThreadPoolExecutor} 会根据
 * corePoolSize（见 {@link #getCorePoolSize}）和
 * maximumPoolSize（见 {@link #getMaximumPoolSize}）设置的边界自动调整池大小（见 {@link #getPoolSize}）。
 *
 * 当在方法 {@link #execute(Runnable)} 中提交新任务时，如果运行的线程少于 corePoolSize，将创建一个新线程来处理请求，即使其他工作线程处于空闲状态。
 * 如果运行的线程多于 corePoolSize 但少于 maximumPoolSize，只有当队列已满时才会创建新线程。
 * 通过将 corePoolSize 和 maximumPoolSize 设置为相同的值，可以创建固定大小的线程池。
 * 通过将 maximumPoolSize 设置为一个实际上无界的值，例如 {@code
 * Integer.MAX_VALUE}，可以允许池容纳任意数量的并发任务。
 * 通常，核心和最大池大小仅在构造时设置，但也可以使用 {@link #setCorePoolSize} 和 {@link
 * #setMaximumPoolSize} 动态更改。</dd>
 *
 * <dt>按需创建</dt>
 *
 * <dd>默认情况下，即使是核心线程也只有在新任务到达时才会被创建和启动，但可以使用方法 {@link #prestartCoreThread} 或 {@link
 * #prestartAllCoreThreads} 动态覆盖此行为。如果使用非空队列构造池，您可能希望预启动线程。</dd>
 *
 * <dt>创建新线程</dt>
 *
 * <dd>使用 {@link ThreadFactory} 创建新线程。如果不另行指定，将使用 {@link Executors#defaultThreadFactory}，
 * 该工厂创建的所有线程都在同一个 {@link
 * ThreadGroup} 中，并具有相同的 {@code NORM_PRIORITY} 优先级和非守护线程状态。
 * 通过提供不同的 ThreadFactory，可以更改线程的名称、线程组、优先级、守护线程状态等。
 * 如果 ThreadFactory 在被要求创建线程时返回 null，执行器将继续运行，但可能无法执行任何任务。
 * 线程应具有 "modifyThread" {@code RuntimePermission}。如果工作线程或其他使用池的线程不具备此权限，
 * 服务可能会降级：配置更改可能无法及时生效，关闭的池可能保持在可以终止但未完成终止的状态。</dd>
 *
 * <dt>保持活动时间</dt>
 *
 * <dd>如果池当前的线程数多于 corePoolSize，如果空闲时间超过 keepAliveTime（见 {@link #getKeepAliveTime(TimeUnit)}），
 * 多余的线程将被终止。这提供了一种在池不活跃使用时减少资源消耗的手段。
 * 如果池后来变得更加活跃，将创建新线程。此参数也可以使用方法 {@link #setKeepAliveTime(long,
 * TimeUnit)} 动态更改。使用值 {@code Long.MAX_VALUE} {@link
 * TimeUnit#NANOSECONDS} 可以有效地禁用在关闭前的空闲线程终止。
 * 默认情况下，保持活动策略仅适用于多于 corePoolSize 的线程。但可以使用方法 {@link #allowCoreThreadTimeOut(boolean)}
 * 将此超时策略应用于核心线程，前提是 keepAliveTime 值非零。</dd>
 *
 * <dt>队列</dt>
 *
 * <dd>任何 {@link BlockingQueue} 都可以用于传输和保存提交的任务。使用此队列与池大小的调整有关：
 *
 * <ul>
 *
 * <li> 如果运行的线程少于 corePoolSize，执行器总是优先添加新线程
 * 而不是排队。</li>
 *
 * <li> 如果运行的线程数等于或超过 corePoolSize，执行器总是优先排队请求
 * 而不是添加新线程。</li>
 *
 * <li> 如果请求无法排队，只有在不会超过 maximumPoolSize 的情况下才会创建新线程，否则任务将被拒绝。</li>
 *
 * </ul>
 *
 * 有三种通用的队列策略：
 * <ol>
 *
 * <li> <em>直接传递。</em> 一个工作队列的好默认选择是 {@link SynchronousQueue}，它将任务传递给线程而不会以其他方式保存它们。
 * 在此情况下，尝试排队任务将失败，除非有线程立即可用以运行它，因此将创建一个新线程。
 * 此策略避免了处理可能具有内部依赖关系的请求集时的锁定。
 * 直接传递通常需要无界的 maximumPoolSizes 以避免拒绝新提交的任务。
 * 这反过来又允许当命令继续以比处理速度更快的平均速度到达时，线程增长不受限制的可能性。  </li>
 *
 * <li><em>无界队列。</em> 使用无界队列（例如没有预定义容量的 {@link LinkedBlockingQueue}）将导致所有 corePoolSize 线程忙碌时新任务在队列中等待。
 * 因此，创建的线程数将不会超过 corePoolSize。（因此 maximumPoolSize 的值没有影响。）
 * 当每个任务完全独立于其他任务时，此队列方式可能是合适的，因此任务不能影响其他任务的执行；例如，在网页服务器中。
 * 虽然这种队列方式在平滑瞬时请求爆发方面可能有用，但它也允许当命令继续以比处理速度更快的平均速度到达时，工作队列增长不受限制的可能性。  </li>
 *
 * <li><em>有界队列。</em> 有界队列（例如 {@link ArrayBlockingQueue}）与有限的 maximumPoolSizes 一起使用时有助于防止资源耗尽，但可能更难以调整和控制。
 * 队列大小和最大池大小可以相互权衡：使用大队列和小池可以最小化 CPU 使用率、操作系统资源和上下文切换开销，但可能导致吞吐量人为降低。
 * 如果任务经常阻塞（例如，如果它们是 I/O 绑定的），系统可能能够调度比您允许的更多的线程。
 * 使用小队列通常需要更大的池大小，这可以保持 CPU 忙碌，但可能会遇到不可接受的调度开销，这也降低了吞吐量。  </li>
 *
 * </ol>
 *
 * </dd>
 *
 * <dt>被拒绝的任务</dt>
 *
 * <dd>在方法 {@link #execute(Runnable)} 中提交的新任务将在执行器已关闭时被<em>拒绝</em>，
 * 以及当执行器使用有限的最大线程数和工作队列容量，并且已饱和时。在任一情况下，
 * {@code execute} 方法将调用其 {@link RejectedExecutionHandler} 的 {@link
 * RejectedExecutionHandler#rejectedExecution(Runnable, ThreadPoolExecutor)} 方法。提供了四种预定义的处理策略：
 *
 * <ol>
 *
 * <li> 在默认的 {@link ThreadPoolExecutor.AbortPolicy} 中，处理程序在拒绝时抛出运行时 {@link RejectedExecutionException}。 </li>
 *
 * <li> 在 {@link ThreadPoolExecutor.CallerRunsPolicy} 中，调用 {@code execute} 的线程本身运行任务。
 * 这提供了一种简单的反馈控制机制，可以减慢新任务提交的速率。 </li>
 *
 * <li> 在 {@link ThreadPoolExecutor.DiscardPolicy} 中，无法执行的任务将被简单地丢弃。  </li>
 *
 * <li> 在 {@link ThreadPoolExecutor.DiscardOldestPolicy} 中，如果执行器未关闭，工作队列头部的任务将被丢弃，
 * 然后重新尝试执行（这可能会再次失败，导致此操作重复。） </li>
 *
 * </ol>
 *
 * 可以定义和使用其他类型的 {@link
 * RejectedExecutionHandler} 类。这样做需要一些特别注意，特别是当策略设计为仅在特定容量或队列策略下工作时。 </dd>
 *
 * <dt>钩子方法</dt>
 *
 * <dd>此类提供了可覆盖的 {@code protected} {@link #beforeExecute(Thread, Runnable)} 和
 * {@link #afterExecute(Runnable, Throwable)} 方法，这些方法在每个任务执行前后被调用。
 * 这些方法可以用于操纵执行环境；例如，重新初始化 ThreadLocals，收集统计信息，或添加日志条目。
 * 另外，方法 {@link #terminated} 可以被覆盖以在执行器完全终止后执行任何特殊处理。
 *
 * <p>如果钩子或回调方法抛出异常，内部工作线程可能会失败并突然终止。</dd>
 *
 * <dt>队列维护</dt>
 *
 * <dd>方法 {@link #getQueue()} 允许访问工作队列以进行监视和调试。强烈不建议将此方法用于任何其他目的。
 * 提供了两种方法，{@link #remove(Runnable)} 和 {@link #purge}，以帮助在大量排队任务被取消时进行存储回收。</dd>
 *
 * <dt>最终化</dt>
 *
 * <dd>程序中不再引用且没有剩余线程的池将被 {@code shutdown} 自动。如果希望确保即使用户忘记调用 {@link #shutdown} 也能回收未引用的池，
 * 则必须安排使未使用的线程最终死亡，通过设置适当的保持活动时间，使用零核心线程的下限和/或设置 {@link #allowCoreThreadTimeOut(boolean)}。 </dd>
 *
 * </dl>
 *
 * <p><b>扩展示例</b>。此类的大多数扩展都覆盖了一个或多个受保护的钩子方法。例如，以下子类添加了一个简单的暂停/恢复功能：
 *
 *  <pre> {@code
 * class PausableThreadPoolExecutor extends ThreadPoolExecutor {
 *   private boolean isPaused;
 *   private ReentrantLock pauseLock = new ReentrantLock();
 *   private Condition unpaused = pauseLock.newCondition();
 *
 *   public PausableThreadPoolExecutor(...) { super(...); }
 *
 *   protected void beforeExecute(Thread t, Runnable r) {
 *     super.beforeExecute(t, r);
 *     pauseLock.lock();
 *     try {
 *       while (isPaused) unpaused.await();
 *     } catch (InterruptedException ie) {
 *       t.interrupt();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void pause() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = true;
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void resume() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = false;
 *       unpaused.signalAll();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ThreadPoolExecutor extends AbstractExecutorService {
    /**
     * 主池控制状态，ctl，是一个原子整数，打包了两个概念字段
     *   workerCount，表示有效的线程数
     *   runState，    表示是否正在运行、关闭等
     *
     * 为了将它们打包到一个整数中，我们将 workerCount 限制为 (2^29)-1（约 5 亿）线程，而不是 (2^31)-1（20 亿）。
     * 如果将来这成为一个问题，可以将变量更改为 AtomicLong，并调整下面的移位/掩码常量。但在需要出现之前，使用 int 会使代码更快更简单。
     *
     * workerCount 是被允许启动且未被允许停止的工作者数量。该值可能暂时与实际的活动线程数不同，
     * 例如当 ThreadFactory 在被要求创建线程时失败，以及当退出线程在终止前仍在进行清理工作时。
     * 用户可见的池大小报告为工作者集的当前大小。
     *
     * runState 提供主要的生命周期控制，取值为：
     *
     *   RUNNING:  接受新任务并处理排队的任务
     *   SHUTDOWN: 不接受新任务，但处理排队的任务
     *   STOP:     不接受新任务，不处理排队的任务，
     *             并中断正在进行的任务
     *   TIDYING:  所有任务已终止，workerCount 为零，
     *             转换到 TIDYING 状态的线程将运行 terminated() 钩子方法
     *   TERMINATED: terminated() 已完成
     *
     * 这些值的数值顺序很重要，以允许有序比较。runState 随时间单调递增，但不必经过每个状态。转换为：
     *
     * RUNNING -> SHUTDOWN
     *    在调用 shutdown() 时，可能在 finalize() 中隐式调用
     * (RUNNING 或 SHUTDOWN) -> STOP
     *    在调用 shutdownNow() 时
     * SHUTDOWN -> TIDYING
     *    当队列和池都为空时
     * STOP -> TIDYING
     *    当池为空时
     * TIDYING -> TERMINATED
     *    当 terminated() 钩子方法完成后
     *
     * 在状态达到 TERMINATED 时，等待在 awaitTermination() 中的线程将返回。
     *
     * 检测从 SHUTDOWN 到 TIDYING 的转换不如您希望的那样直接，因为队列可能在 SHUTDOWN 状态期间从非空变为为空，反之亦然，
     * 但我们只能在看到队列为空后，看到 workerCount 为 0 时终止（这有时需要重新检查——见下文）。
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

                    // runState 存储在高阶位
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // 打包和解包 ctl
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    /*
     * 位字段访问器，不需要解包 ctl。
     * 这些依赖于位布局和 workerCount 从不为负。
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * 尝试对 ctl 的 workerCount 字段进行 CAS 增量操作。
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * 尝试对 ctl 的 workerCount 字段进行 CAS 减量操作。
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * 减少 ctl 的 workerCount 字段。仅在线程突然终止时调用（参见 processWorkerExit）。其他减少操作在 getTask 中执行。
     */
    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    /**
     * 用于保存任务并传递给工作线程的队列。我们不要求 workQueue.poll() 返回 null 必定意味着 workQueue.isEmpty()，因此仅依赖 isEmpty 来判断队列是否为空（例如，在决定从 SHUTDOWN 转换到 TIDYING 时）。这适应了特殊用途的队列，如 DelayQueues，poll() 被允许在延迟到期后返回 null。
     */
    private final BlockingQueue<Runnable> workQueue;

    /**
     * 访问工作线程集及相关簿记时持有的锁。虽然我们可以使用某种并发集，但事实证明使用锁通常是更好的选择。原因之一是这会序列化 interruptIdleWorkers，从而避免不必要的中断风暴，特别是在关闭期间。否则，退出的线程会并发地中断那些尚未中断的线程。它还简化了与 largestPoolSize 等相关的某些统计簿记。在 shutdown 和 shutdownNow 时我们也持有 mainLock，以确保在单独检查中断权限和实际中断时工作线程集是稳定的。
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * 池中所有工作线程的集合。仅在持有 mainLock 时访问。
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * 支持 awaitTermination 的等待条件
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * 跟踪达到的最大池大小。仅在持有 mainLock 时访问。
     */
    private int largestPoolSize;

    /**
     * 完成的任务计数器。仅在工作线程终止时更新。仅在持有 mainLock 时访问。
     */
    private long completedTaskCount;

    /*
     * 所有用户控制参数都被声明为易失性的，以便基于最新的值执行操作，但无需锁定，因为没有内部不变量依赖于它们相对于其他操作的同步变化。
     */

    /**
     * 新线程的工厂。所有线程都使用此工厂创建（通过方法 addWorker）。所有调用者都必须准备好 addWorker 失败，这可能反映了系统或用户的策略限制线程数量。即使不被视为错误，创建线程失败可能导致新任务被拒绝或现有任务卡在队列中。
     *
     * 我们进一步确保即使在尝试创建线程时抛出错误（如 OutOfMemoryError）时也能保持池不变量。由于在 Thread.start 中需要分配本地堆栈，这种错误相当常见，用户将希望执行干净的池关闭以清理。很可能有足够的内存供清理代码完成而不会再次遇到 OutOfMemoryError。
     */
    private volatile ThreadFactory threadFactory;

    /**
     * 在执行饱和或关闭时调用的处理器。
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * 空闲线程等待工作的超时时间（纳秒）。当线程数多于 corePoolSize 或 allowCoreThreadTimeOut 为 true 时，线程使用此超时时间。否则，它们会无限期等待新任务。
     */
    private volatile long keepAliveTime;

    /**
     * 如果为 false（默认值），核心线程即使空闲也会保持存活。如果为 true，核心线程使用 keepAliveTime 等待工作。
     */
    private volatile boolean allowCoreThreadTimeOut;

    /**
     * 核心池大小是保持存活（不允许超时等）的最小工作线程数，除非设置了 allowCoreThreadTimeOut，此时最小值为零。
     */
    private volatile int corePoolSize;

    /**
     * 最大池大小。实际的最大值在内部由 CAPACITY 限制。
     */
    private volatile int maximumPoolSize;

    /**
     * 默认的拒绝执行处理器
     */
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();

                    /**
     * 调用 shutdown 和 shutdownNow 所需的权限。
     * 我们还要求（参见 checkShutdownAccess）调用者实际上有权限中断工作线程集中的线程
     * （由 Thread.interrupt 控制，这依赖于 ThreadGroup.checkAccess，而后者又依赖于
     * SecurityManager.checkAccess）。只有在这些检查通过的情况下才会尝试关闭。
     *
     * 所有实际调用 Thread.interrupt 的地方（参见
     * interruptIdleWorkers 和 interruptWorkers）都会忽略
     * SecurityExceptions，这意味着尝试中断会默默地失败。在关闭的情况下，除非 SecurityManager 的策略不一致，
     * 有时允许访问线程，有时不允许，否则它们不应该失败。在这些情况下，未能实际中断线程可能会禁用或延迟完全终止。
     * 其他使用 interruptIdleWorkers 的情况是建议性的，实际中断失败只会延迟对配置更改的响应，因此不会特别处理。
     */
    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");

    /* 执行终结器时使用的上下文，或为 null。 */
    private final AccessControlContext acc;

    /**
     * Worker 类主要维护运行任务的线程的中断控制状态，以及其他一些次要的簿记。
     * 该类利用扩展 AbstractQueuedSynchronizer 来简化围绕每个任务执行的锁的获取和释放。
     * 这可以防止旨在唤醒等待任务的工作者线程的中断反而中断正在运行的任务。我们实现了一个简单的非可重入互斥锁，
     * 而不是使用 ReentrantLock，因为我们不希望工作者任务在调用如 setCorePoolSize 等池控制方法时能够重新获取锁。
     * 此外，为了抑制线程实际开始运行任务之前的中断，我们将锁状态初始化为负值，并在启动时清除（在 runWorker 中）。
     */
    private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * 此类将永远不会被序列化，但我们提供了一个 serialVersionUID 以抑制 javac 警告。
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** 此工作者正在运行的线程。如果工厂失败，则为 null。 */
        final Thread thread;
        /** 要运行的初始任务。可能为 null。 */
        Runnable firstTask;
        /** 每线程任务计数器 */
        volatile long completedTasks;

        /**
         * 使用给定的初始任务和从 ThreadFactory 获取的线程创建。
         * @param firstTask 初始任务（如果没有则为 null）
         */
        Worker(Runnable firstTask) {
            setState(-1); // 抑制 runWorker 之前的中断
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** 将主运行循环委托给外部的 runWorker  */
        public void run() {
            runWorker(this);
        }

        // 锁方法
        //
        // 值 0 表示未锁定状态。
        // 值 1 表示已锁定状态。

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /*
     * 设置控制状态的方法
     */

    /**
     * 将 runState 转换为给定的目标状态，或者如果已经至少是给定的目标状态，则保持不变。
     *
     * @param targetState 所需的状态，可以是 SHUTDOWN 或 STOP
     *        （但不能是 TIDYING 或 TERMINATED —— 对于这些状态使用 tryTerminate）
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
     * 如果满足以下条件之一，则转换为 TERMINATED 状态：(SHUTDOWN 且池和队列为空) 或 (STOP 且池为空)。
     * 如果其他情况下有资格终止但 workerCount 不为零，则中断一个空闲工作者以确保关闭信号传播。
     * 必须在任何可能使终止成为可能的操作之后调用此方法 —— 减少工作者数量或在关闭期间从队列中移除任务。
     * 该方法不是私有的，以允许 ScheduledThreadPoolExecutor 访问。
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            if (workerCountOf(c) != 0) { // 有资格终止
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // 否则在 CAS 失败时重试
        }
    }

                    /*
     * 用于控制工作线程中断的方法。
     */

    /**
     * 如果存在安全管理器，确保调用者具有关闭线程的一般权限（参见shutdownPerm）。
     * 如果此检查通过，还会确保调用者有权中断每个工作线程。即使第一个检查通过，
     * 也可能不为真，如果安全管理器对某些线程有特殊处理。
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 中断所有线程，即使它们是活跃的。忽略SecurityExceptions（在这种情况下，某些线程可能保持未被中断）。
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断可能正在等待任务的线程（如未被锁定所示），以便它们可以检查终止或配置更改。忽略
     * SecurityExceptions（在这种情况下，某些线程可能保持未被中断）。
     *
     * @param onlyOne 如果为true，最多中断一个工作线程。这仅在终止已启用但仍有其他工作线程时从tryTerminate调用。
     * 在这种情况下，最多中断一个等待的工作线程以传播关闭信号，以防所有线程当前都在等待。
     * 中断任意线程确保自关闭开始以来新到达的工作线程最终也会退出。
     * 为了保证最终的终止，总是只中断一个空闲的工作线程就足够了，但shutdown()中断所有空闲的工作线程
     * 以便冗余的工作线程能够迅速退出，而不是等待一个落后的任务完成。
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断空闲工作线程的通用形式，以避免必须记住布尔参数的含义。
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * 大多数还导出到ScheduledThreadPoolExecutor的杂项工具方法
     */

    /**
     * 调用给定命令的拒绝执行处理程序。包保护供ScheduledThreadPoolExecutor使用。
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 在调用shutdown时执行运行状态转换后的任何进一步清理。在这里是一个空操作，但由
     * ScheduledThreadPoolExecutor用于取消延迟任务。
     */
    void onShutdown() {
    }

    /**
     * ScheduledThreadPoolExecutor需要的状态检查，以在关闭期间启用运行任务。
     *
     * @param shutdownOK 如果在SHUTDOWN状态下应该返回true，则为true
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * 将任务队列排空到一个新列表中，通常使用drainTo。但如果队列是DelayQueue或任何其他类型的
     * 队列，poll或drainTo可能无法移除某些元素，它将逐个删除这些元素。
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * 创建、运行和清理工作线程的方法
     */

    /**
     * 检查是否可以根据当前池状态和给定的边界（核心或最大）添加新工作线程。如果可以，
     * 工作线程计数将相应调整，并且，如果可能，将创建并启动一个新工作线程，运行firstTask作为其
     * 第一个任务。如果池已停止或有资格关闭，此方法返回false。如果线程工厂在请求时无法创建线程，
     * 它也返回false。如果线程创建失败，无论是由于线程工厂返回null，还是由于异常（通常是Thread.start()中的
     * OutOfMemoryError），我们都会干净地回滚。
     *
     * @param firstTask 新线程应首先运行的任务（或为null）。当线程池中的线程少于corePoolSize时（在这种情况下，我们总是启动一个），
     * 或当队列已满时（在这种情况下，我们必须绕过队列），工作线程会使用一个初始的第一个任务（在方法execute()中）创建。
     * 初始空闲线程通常通过prestartCoreThread或替换其他死亡的工作线程创建。
     *
     * @param core 如果为true，使用corePoolSize作为边界，否则使用maximumPoolSize。（这里使用布尔指示器而不是值，以确保在检查其他池状态后读取最新的值）。
     * @return 如果成功则为true
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);


                            // 仅在必要时检查队列是否为空。
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // 重新读取 ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // 否则由于 workerCount 的变化导致 CAS 失败；重试内部循环
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // 持有锁时重新检查。
                    // 如果 ThreadFactory 失败或在获取锁之前已关闭，则退出。
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // 预检查 t 是否可启动
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    /**
     * 回滚工作线程的创建。
     * - 如果存在，则从 workers 中移除工作线程
     * - 减少工作线程计数
     * - 重新检查终止情况，以防此工作线程的存在阻碍了终止
     */
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                workers.remove(w);
            decrementWorkerCount();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 执行即将死亡的工作线程的清理和记账。仅从工作线程调用。
     * 除非 completedAbruptly 为 true，否则假定 workerCount 已经调整以反映退出。
     * 此方法从工作线程集中移除线程，并且可能终止线程池或在以下情况下替换工作线程：
     * 1. 由于用户任务异常退出
     * 2. 运行中的工作线程少于 corePoolSize
     * 3. 队列非空但没有工作线程
     *
     * @param w 工作线程
     * @param completedAbruptly 如果工作线程因用户异常而死亡
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // 如果突然死亡，则 workerCount 未调整
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // 无需替换
            }
            addWorker(null, false);
        }
    }

    /**
     * 根据当前配置设置执行阻塞或定时等待任务，或在以下情况下返回 null：
     * 1. 工作线程数超过 maximumPoolSize（由于调用了 setMaximumPoolSize）。
     * 2. 线程池已停止。
     * 3. 线程池已关闭且队列为空。
     * 4. 该工作线程在等待任务时超时，并且超时的工作线程受终止影响（即，
     *    {@code allowCoreThreadTimeOut || workerCount > corePoolSize}）
     *    在定时等待前后，且如果队列非空，此工作线程不是池中的最后一个线程。
     *
     * @return 任务，或如果工作线程必须退出，则返回 null，此时 workerCount 减少
     */
    private Runnable getTask() {
        boolean timedOut = false; // 上一次 poll() 是否超时？

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 仅在必要时检查队列是否为空。
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // 工作线程是否受裁剪？
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

                    /**
     * 主工作线程运行循环。重复从队列中获取任务并执行，同时处理多个问题：
     *
     * 1. 我们可能从一个初始任务开始，在这种情况下，我们不需要获取第一个任务。否则，只要线程池正在运行，我们就从getTask获取任务。如果它返回null，则由于线程池状态或配置参数的改变，工作线程退出。其他退出情况是由于外部代码抛出异常，这种情况下completedAbruptly为真，通常会导致processWorkerExit替换这个线程。
     *
     * 2. 在运行任何任务之前，获取锁以防止任务执行期间其他线程池中断，然后我们确保除非线程池正在停止，否则此线程没有设置中断。
     *
     * 3. 每个任务运行前都会调用beforeExecute，这可能会抛出异常，这种情况下我们使线程死亡（通过completedAbruptly为真中断循环）而不处理任务。
     *
     * 4. 假设beforeExecute正常完成，我们运行任务，收集任何抛出的异常以发送给afterExecute。我们分别处理RuntimeException、Error（这两者规范保证我们能够捕获）和任意的Throwables。因为我们在Runnable.run中不能重新抛出Throwables，我们在输出时（到线程的UncaughtExceptionHandler）将它们包装在Errors中。任何抛出的异常也会保守地导致线程死亡。
     *
     * 5. 任务.run完成后，我们调用afterExecute，这可能会抛出异常，这也会导致线程死亡。根据JLS第14.20节，这个异常将是有效的，即使任务.run抛出异常也是如此。
     *
     * 异常机制的总体效果是，afterExecute和线程的UncaughtExceptionHandler尽可能准确地提供了用户代码遇到的任何问题的信息。
     *
     * @param w 工作者线程
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // 允许中断
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // 如果线程池正在停止，确保线程被中断；
                // 如果不是，确保线程没有被中断。这需要在第二种情况下重新检查，以处理关闭时的中断竞争
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    // 公共构造函数和方法

    /**
     * 使用给定的初始参数和默认线程工厂及拒绝执行处理器创建一个新的 {@code ThreadPoolExecutor}。
     * 使用 {@link Executors} 工厂方法可能更方便。
     *
     * @param corePoolSize 即使它们是空闲的，也要保持在池中的线程数，除非设置了 {@code allowCoreThreadTimeOut}
     * @param maximumPoolSize 允许在池中的最大线程数
     * @param keepAliveTime 当线程数大于核心数时，这是多余空闲线程等待新任务前的最大等待时间。
     * @param unit {@code keepAliveTime} 参数的时间单位
     * @param workQueue 用于在执行前保存任务的队列。此队列将仅保存由 {@code execute} 方法提交的 {@code Runnable} 任务。
     * @throws IllegalArgumentException 如果以下条件之一成立：<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException 如果 {@code workQueue} 为 null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * 使用给定的初始参数和默认拒绝执行处理器创建一个新的 {@code ThreadPoolExecutor}。
     *
     * @param corePoolSize 即使它们是空闲的，也要保持在池中的线程数，除非设置了 {@code allowCoreThreadTimeOut}
     * @param maximumPoolSize 允许在池中的最大线程数
     * @param keepAliveTime 当线程数大于核心数时，这是多余空闲线程等待新任务前的最大等待时间。
     * @param unit {@code keepAliveTime} 参数的时间单位
     * @param workQueue 用于在执行前保存任务的队列。此队列将仅保存由 {@code execute} 方法提交的 {@code Runnable} 任务。
     * @param threadFactory 当执行器创建新线程时使用的工厂
     * @throws IllegalArgumentException 如果以下条件之一成立：<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException 如果 {@code workQueue}
     *         或 {@code threadFactory} 为 null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }

                    /**
     * 创建一个具有给定初始参数和默认线程工厂的 {@code ThreadPoolExecutor}。
     *
     * @param corePoolSize 即使它们处于空闲状态，也要保持在池中的线程数，除非设置了 {@code allowCoreThreadTimeOut}
     * @param maximumPoolSize 池中允许的最大线程数
     * @param keepAliveTime 当线程数大于核心线程数时，这是多余的空闲线程在终止前等待新任务的最大时间。
     * @param unit {@code keepAliveTime} 参数的时间单位
     * @param workQueue 用于在执行前保存任务的队列。此队列将仅保存由 {@code execute} 方法提交的 {@code Runnable} 任务。
     * @param handler 当线程边界和队列容量达到上限导致执行被阻塞时使用的处理器
     * @throws IllegalArgumentException 如果以下任一条件成立：<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException 如果 {@code workQueue}
     *         或 {@code handler} 为 null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

    /**
     * 创建一个具有给定初始参数的 {@code ThreadPoolExecutor}。
     *
     * @param corePoolSize 即使它们处于空闲状态，也要保持在池中的线程数，除非设置了 {@code allowCoreThreadTimeOut}
     * @param maximumPoolSize 池中允许的最大线程数
     * @param keepAliveTime 当线程数大于核心线程数时，这是多余的空闲线程在终止前等待新任务的最大时间。
     * @param unit {@code keepAliveTime} 参数的时间单位
     * @param workQueue 用于在执行前保存任务的队列。此队列将仅保存由 {@code execute} 方法提交的 {@code Runnable} 任务。
     * @param threadFactory 当执行器创建新线程时使用的工厂
     * @param handler 当线程边界和队列容量达到上限导致执行被阻塞时使用的处理器
     * @throws IllegalArgumentException 如果以下任一条件成立：<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException 如果 {@code workQueue}
     *         或 {@code threadFactory} 或 {@code handler} 为 null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 在未来的某个时间执行给定的任务。该任务可能在一个新线程或现有的线程池线程中执行。
     *
     * 如果任务无法提交执行，无论是因为此执行器已关闭还是因为其容量已达到上限，
     * 任务将由当前的 {@code RejectedExecutionHandler} 处理。
     *
     * @param command 要执行的任务
     * @throws RejectedExecutionException 根据 {@code RejectedExecutionHandler} 的判断，如果任务
     *         无法接受执行
     * @throws NullPointerException 如果 {@code command} 为 null
     */
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * 操作分为3步：
         *
         * 1. 如果运行中的线程数少于 corePoolSize，尝试启动一个新线程，将给定的任务作为其第一个任务。
         *    调用 addWorker 原子地检查 runState 和 workerCount，因此可以防止在不应该添加线程时的误报，通过返回 false。
         *
         * 2. 如果任务可以成功排队，我们仍然需要双重检查是否应该添加线程
         *    （因为自上次检查以来现有线程已死亡）或自进入此方法以来池已关闭。因此我们
         *    重新检查状态，如果已停止则回滚排队，或者如果没有线程则启动一个新线程。
         *
         * 3. 如果我们无法排队任务，那么我们尝试添加一个新线程。如果失败，我们知道我们已关闭或饱和
         *    并因此拒绝任务。
         */
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }

                    /**
     * 启动一个有序的关闭过程，在此过程中之前提交的任务将继续执行，但不再接受新的任务。
     * 如果已经处于关闭状态，调用此方法将不会产生额外的效果。
     *
     * <p>此方法不会等待之前提交的任务完成执行。 若要等待任务完成，请使用 {@link #awaitTermination awaitTermination}。
     *
     * @throws SecurityException {@inheritDoc}
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    /**
     * 尝试停止所有正在执行的任务，停止处理等待的任务，并返回一个包含等待执行的任务列表。
     * 这些任务在调用此方法后将从任务队列中移除。
     *
     * <p>此方法不会等待正在执行的任务终止。 若要等待任务完成，请使用 {@link #awaitTermination awaitTermination}。
     *
     * <p>除了尽力尝试停止正在执行的任务外，没有其他保证。 本实现通过 {@link Thread#interrupt} 取消任务，
     * 因此任何不响应中断的任务可能永远不会终止。
     *
     * @throws SecurityException {@inheritDoc}
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    /**
     * 如果此执行器在调用 {@link #shutdown} 或 {@link #shutdownNow} 后正在终止但尚未完全终止，则返回 true。
     * 此方法对于调试可能有用。 在关闭后一段时间返回 true 可能表明提交的任务忽略了或抑制了中断，
     * 导致此执行器无法正确终止。
     *
     * @return 如果正在终止但尚未终止，则返回 {@code true}
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 当此执行器不再被引用且没有线程时，调用 {@code shutdown}。
     */
    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    /**
     * 设置用于创建新线程的线程工厂。
     *
     * @param threadFactory 新的线程工厂
     * @throws NullPointerException 如果 threadFactory 为 null
     * @see #getThreadFactory
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null)
            throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    /**
     * 返回用于创建新线程的线程工厂。
     *
     * @return 当前线程工厂
     * @see #setThreadFactory(ThreadFactory)
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * 设置新的不可执行任务处理器。
     *
     * @param handler 新的处理器
     * @throws NullPointerException 如果 handler 为 null
     * @see #getRejectedExecutionHandler
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        this.handler = handler;
    }

    /**
     * 返回当前的不可执行任务处理器。
     *
     * @return 当前处理器
     * @see #setRejectedExecutionHandler(RejectedExecutionHandler)
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * 设置核心线程数。 这将覆盖构造函数中设置的任何值。 如果新值小于当前值，多余现有的线程将在下一次空闲时终止。
     * 如果较大，将根据需要启动新线程以执行队列中的任务。
     *
     * @param corePoolSize 新的核心大小
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     * @see #getCorePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // 我们实际上并不知道需要多少新线程。 作为一种启发式方法，预先启动足够多的新工作线程（最多到新的核心大小），
            // 以处理队列中的当前任务数量，但如果在此过程中队列变为空，则停止。
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }


                    /**
     * 返回线程池的核心线程数。
     *
     * @return 线程池的核心线程数
     * @see #setCorePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * 启动一个核心线程，使其空闲等待任务。这覆盖了只有在执行新任务时才启动核心线程的默认策略。如果所有核心线程已经启动，此方法将返回 {@code false}。
     *
     * @return 如果启动了一个线程，则返回 {@code true}
     */
    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
            addWorker(null, true);
    }

    /**
     * 与 prestartCoreThread 相同，但确保即使 corePoolSize 为 0 时，至少启动一个线程。
     */
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    /**
     * 启动所有核心线程，使其空闲等待任务。这覆盖了只有在执行新任务时才启动核心线程的默认策略。
     *
     * @return 启动的线程数
     */
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }

    /**
     * 如果此线程池允许核心线程在没有任务到达的情况下超时并终止，当新任务到达时需要时会被替换，则返回 true。当为 true 时，应用于非核心线程的相同超时策略也适用于核心线程。当为 false（默认值）时，核心线程永远不会因缺乏传入任务而终止。
     *
     * @return 如果允许核心线程超时，则返回 {@code true}，否则返回 {@code false}
     *
     * @since 1.6
     */
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    /**
     * 设置核心线程是否可以在没有任务到达的情况下超时并终止，当新任务到达时需要时会被替换的策略。当为 false 时，核心线程永远不会因缺乏传入任务而终止。当为 true 时，应用于非核心线程的相同超时策略也适用于核心线程。为了避免持续的线程替换，当设置为 {@code true} 时，超时时间必须大于零。此方法通常应在池被积极使用之前调用。
     *
     * @param value 如果应超时，则为 {@code true}，否则为 {@code false}
     * @throws IllegalArgumentException 如果 value 为 {@code true} 且当前超时时间不大于零
     *
     * @since 1.6
     */
    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("核心线程必须具有非零的超时时间");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value)
                interruptIdleWorkers();
        }
    }

    /**
     * 设置允许的最大线程数。这覆盖了构造函数中设置的任何值。如果新值小于当前值，当空闲时，多余的现有线程将被终止。
     *
     * @param maximumPoolSize 新的最大值
     * @throws IllegalArgumentException 如果新最大值小于或等于零，或小于 {@linkplain #getCorePoolSize 核心池大小}
     * @see #getMaximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize)
            interruptIdleWorkers();
    }

    /**
     * 返回允许的最大线程数。
     *
     * @return 允许的最大线程数
     * @see #setMaximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * 设置线程在被终止前可能保持空闲的时间限制。如果当前池中的线程数超过核心线程数，这些线程在没有处理任务的情况下等待此时间后将被终止。这覆盖了构造函数中设置的任何值。
     *
     * @param time 等待的时间。时间值为零将导致多余线程在执行任务后立即终止。
     * @param unit {@code time} 参数的时间单位
     * @throws IllegalArgumentException 如果 {@code time} 小于零或 {@code time} 为零且 {@code allowsCoreThreadTimeOut}
     * @see #getKeepAliveTime(TimeUnit)
     */
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0)
            throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut())
            throw new IllegalArgumentException("核心线程必须具有非零的超时时间");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
            interruptIdleWorkers();
    }

    /**
     * 返回线程的空闲时间，即超过核心池大小的线程在被终止前可能保持空闲的时间。
     *
     * @param unit 结果的时间单位
     * @return 时间限制
     * @see #setKeepAliveTime(long, TimeUnit)
     */
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* 用户级队列工具 */

    /**
     * 返回此执行器使用的任务队列。访问任务队列主要用于调试和监控。此队列可能正在使用中。检索任务队列不会阻止队列中的任务执行。
     *
     * @return 任务队列
     */
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }


                    /**
     * 如果此任务存在于执行器的内部队列中，则将其移除，从而防止尚未开始的任务被执行。
     *
     * <p>此方法可用作取消方案的一部分。它可能无法移除已转换为其他形式并放置在内部队列中的任务。例如，使用 {@code submit} 方法输入的任务可能会转换为维护 {@code Future} 状态的形式。
     * 然而，在这种情况下，可以使用 {@link #purge} 方法来移除已取消的 Futures。
     *
     * @param task 要移除的任务
     * @return 如果任务被移除，则返回 {@code true}
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // 如果处于 SHUTDOWN 状态且队列为空，则尝试终止
        return removed;
    }

    /**
     * 尝试从工作队列中移除所有已取消的 {@link Future} 任务。此方法可用作存储回收操作，对功能没有其他影响。已取消的任务永远不会执行，但可能在工作队列中累积，直到工作线程能够主动移除它们。调用此方法则尝试立即移除它们。
     * 然而，此方法在其他线程干扰的情况下可能无法移除任务。
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // 如果在遍历过程中遇到干扰，则采用慢速路径。
            // 为遍历制作副本，并调用移除已取消的条目。
            // 慢速路径更可能是 O(N*N)。
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // 如果处于 SHUTDOWN 状态且队列为空，则尝试终止
    }

    /* 统计信息 */

    /**
     * 返回线程池中当前线程的数量。
     *
     * @return 线程的数量
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 去除罕见且令人惊讶的可能性：isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回当前正在执行任务的线程的近似数量。
     *
     * @return 线程的数量
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回线程池中曾经同时存在的最大线程数。
     *
     * @return 线程的数量
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回曾经被安排执行的任务的近似总数。由于任务和线程的状态可能在计算过程中动态变化，返回的值只是一个近似值。
     *
     * @return 任务的数量
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回已完成执行的任务的近似总数。由于任务和线程的状态可能在计算过程中动态变化，返回的值只是一个近似值，但不会在连续调用中减少。
     *
     * @return 任务的数量
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回一个标识此线程池及其状态的字符串，包括运行状态和估计的工作者线程和任务数量。
     *
     * @return 一个标识此线程池及其状态的字符串
     */
    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked())
                    ++nactive;
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "运行中" :
                     (runStateAtLeast(c, TERMINATED) ? "已终止" :
                      "正在关闭"));
        return super.toString() +
            "[" + rs +
            ", 线程池大小 = " + nworkers +
            ", 活动线程 = " + nactive +
            ", 队列中的任务 = " + workQueue.size() +
            ", 已完成的任务 = " + ncompleted +
            "]";
    }


                    /* 扩展钩子 */

    /**
     * 在给定线程中执行给定的Runnable之前调用的方法。此方法由将要执行任务{@code r}的线程{@code t}调用，可用于重新初始化ThreadLocals，或执行日志记录。
     *
     * <p>此实现不执行任何操作，但可以在子类中自定义。注意：为了正确嵌套多个重写，子类通常应在该方法的末尾调用{@code super.beforeExecute}。
     *
     * @param t 将要运行任务{@code r}的线程
     * @param r 将要执行的任务
     */
    protected void beforeExecute(Thread t, Runnable r) { }

    /**
     * 在给定的Runnable执行完成后调用的方法。此方法由执行任务的线程调用。如果非空，Throwable是导致执行突然终止的未捕获的{@code RuntimeException}或{@code Error}。
     *
     * <p>此实现不执行任何操作，但可以在子类中自定义。注意：为了正确嵌套多个重写，子类通常应在该方法的开始处调用{@code super.afterExecute}。
     *
     * <p><b>注意：</b>当动作被封装在任务（如{@link FutureTask}）中，无论是显式还是通过如{@code submit}等方法，这些任务对象会捕获并维护计算异常，因此它们不会导致突然终止，内部异常<em>不会</em>传递给此方法。如果您希望在此方法中捕获这两种类型的失败，您可以进一步探测此类情况，如下例子类所示，该子类打印直接原因或任务被中止时的底层异常：
     *
     *  <pre> {@code
     * class ExtendedExecutor extends ThreadPoolExecutor {
     *   // ...
     *   protected void afterExecute(Runnable r, Throwable t) {
     *     super.afterExecute(r, t);
     *     if (t == null && r instanceof Future<?>) {
     *       try {
     *         Object result = ((Future<?>) r).get();
     *       } catch (CancellationException ce) {
     *           t = ce;
     *       } catch (ExecutionException ee) {
     *           t = ee.getCause();
     *       } catch (InterruptedException ie) {
     *           Thread.currentThread().interrupt(); // 忽略/重置
     *       }
     *     }
     *     if (t != null)
     *       System.out.println(t);
     *   }
     * }}</pre>
     *
     * @param r 已完成的runnable
     * @param t 导致终止的异常，或如果执行正常完成则为null
     */
    protected void afterExecute(Runnable r, Throwable t) { }

    /**
     * 当执行器终止时调用的方法。默认实现不执行任何操作。注意：为了正确嵌套多个重写，子类通常应在该方法内调用{@code super.terminated}。
     */
    protected void terminated() { }

    /* 预定义的RejectedExecutionHandlers */

    /**
     * 一个处理被拒绝任务的处理器，它在调用{@code execute}方法的线程中直接运行被拒绝的任务，除非执行器已被关闭，此时任务将被丢弃。
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * 创建一个{@code CallerRunsPolicy}。
         */
        public CallerRunsPolicy() { }

        /**
         * 在调用者的线程中执行任务r，除非执行器已被关闭，此时任务将被丢弃。
         *
         * @param r 请求执行的runnable任务
         * @param e 尝试执行此任务的执行器
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * 一个处理被拒绝任务的处理器，它抛出一个{@code RejectedExecutionException}。
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        /**
         * 创建一个{@code AbortPolicy}。
         */
        public AbortPolicy() { }

        /**
         * 始终抛出RejectedExecutionException。
         *
         * @param r 请求执行的runnable任务
         * @param e 尝试执行此任务的执行器
         * @throws RejectedExecutionException 始终
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    /**
     * 一个处理被拒绝任务的处理器，它默默地丢弃被拒绝的任务。
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * 创建一个{@code DiscardPolicy}。
         */
        public DiscardPolicy() { }

        /**
         * 不执行任何操作，其效果是丢弃任务r。
         *
         * @param r 请求执行的runnable任务
         * @param e 尝试执行此任务的执行器
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    /**
     * 一个处理被拒绝任务的处理器，它丢弃最旧的未处理请求，然后重试{@code execute}，除非执行器已被关闭，此时任务将被丢弃。
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * 为给定执行器创建一个{@code DiscardOldestPolicy}。
         */
        public DiscardOldestPolicy() { }

        /**
         * 获取并忽略执行器将要执行的下一个任务（如果立即可用），然后重试任务r的执行，除非执行器已被关闭，此时任务r将被丢弃。
         *
         * @param r 请求执行的runnable任务
         * @param e 尝试执行此任务的执行器
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
}
