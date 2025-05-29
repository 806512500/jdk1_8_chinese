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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * 一个版本的 {@link AbstractQueuedSynchronizer}，其中同步状态被维护为一个 {@code long}。
 * 该类具有与 {@code AbstractQueuedSynchronizer} 完全相同的结构、属性和方法，
 * 例外是所有与状态相关的参数和结果都被定义为 {@code long} 而不是 {@code int}。
 * 当创建需要 64 位状态的同步器（如多级锁和屏障）时，此类可能非常有用。
 *
 * <p>有关使用说明和示例，请参阅 {@link AbstractQueuedSynchronizer}。
 *
 * @since 1.6
 * @author Doug Lea
 */
public abstract class AbstractQueuedLongSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414692L;

    /*
      为了保持源代码同步，此源文件的其余部分
      完全克隆自 AbstractQueuedSynchronizer，替换类名并将与同步状态相关的 int 类型更改为 long 类型。请保持这种方式。
    */

    /**
     * 创建一个新的 {@code AbstractQueuedLongSynchronizer} 实例
     * 初始同步状态为零。
     */
    protected AbstractQueuedLongSynchronizer() { }

    /**
     * 等待队列节点类。
     *
     * <p>等待队列是“CLH”（Craig, Landin, 和 Hagersten）锁队列的变体。CLH 锁通常用于自旋锁。
     * 我们改为使用它们作为阻塞同步器，但使用相同的基本策略，即在节点的前驱中保存一些关于线程的控制信息。
     * 每个节点中的“状态”字段跟踪线程是否应该阻塞。当其前驱释放时，节点会被信号通知。
     * 队列中的每个节点还充当特定通知样式的监视器，持有单个等待线程。状态字段并不控制线程是否被授予锁等；
     * 如果线程在队列中排在第一位，它可以尝试获取锁。但排在第一位并不保证成功；它只是赋予了竞争的权利。
     * 因此，当前释放的竞争线程可能需要重新等待。
     *
     * <p>要将节点入队到 CLH 锁中，您需要原子地将其作为新的尾节点插入。要出队，您只需设置头字段。
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
     *
     * <p>将节点插入 CLH 队列只需要对“尾”进行单个原子操作，因此从未入队到已入队有一个简单的原子分界点。
     * 同样，出队仅涉及更新“头”。然而，节点确定其后继者需要更多的工作，部分原因是处理由于超时和中断可能引起的取消。
     *
     * <p>“前驱”链接（在原始 CLH 锁中未使用）主要用于处理取消。如果一个节点被取消，其后继者通常会被重新链接到一个未被取消的前驱。
     * 对于自旋锁中类似机制的解释，请参阅 Scott 和 Scherer 的论文，网址为
     * http://www.cs.rochester.edu/u/scott/synchronization/
     *
     * <p>我们还使用“后继”链接来实现阻塞机制。每个节点的线程 ID 保存在它自己的节点中，因此前驱通过遍历后继链接来确定唤醒哪个线程。
     * 确定后继者必须避免与新入队节点设置其前驱的“后继”字段时的竞态条件。这是通过在必要时从原子更新的“尾”向后检查来解决的，
     * 当一个节点的后继者似乎为 null 时。（或者，换一种说法，后继链接是一种优化，因此我们通常不需要向后扫描。）
     *
     * <p>取消引入了一些保守的基本算法。由于我们必须轮询其他节点的取消，我们可能会错过一个被取消的节点是在我们前面还是后面。
     * 这是通过在取消时总是唤醒后继者来处理的，允许它们稳定在一个新的前驱上，除非我们可以确定一个未被取消的前驱来承担这个责任。
     *
     * <p>CLH 队列需要一个虚拟头节点来启动。但我们在构造时不创建它们，因为如果从未出现竞争，这将是浪费的。
     * 相反，节点是在第一次竞争时构造的，并设置头和尾指针。
     *
     * <p>等待条件的线程使用相同的节点，但使用额外的链接。条件只需要在简单的（非并发）链接队列中链接节点，
     * 因为它们仅在独占持有时访问。在等待时，节点被插入到条件队列中。在信号时，节点被转移到主队列。
     * 状态字段的特殊值用于标记节点所在的队列。
     *
     * <p>感谢 Dave Dice、Mark Moir、Victor Luchangco、Bill Scherer 和 Michael Scott，
     * 以及 JSR-166 专家组成员提供的有益的想法、讨论和批评，这些对本类的设计非常有帮助。
     */
    static final class Node {
        /** 标记表示节点正在以共享模式等待 */
        static final Node SHARED = new Node();
        /** 标记表示节点正在以独占模式等待 */
        static final Node EXCLUSIVE = null;


                    /** 表示线程已取消的 waitStatus 值 */
        static final int CANCELLED =  1;
        /** 表示后继线程需要唤醒的 waitStatus 值 */
        static final int SIGNAL    = -1;
        /** 表示线程正在等待条件的 waitStatus 值 */
        static final int CONDITION = -2;
        /**
         * 表示下一个 acquireShared 应该无条件传播的 waitStatus 值
         */
        static final int PROPAGATE = -3;

        /**
         * 状态字段，仅取以下值：
         *   SIGNAL:     该节点的后继节点（或即将）被阻塞（通过 park），因此当前节点在释放或取消时必须唤醒其后继节点。
         *               为了避免竞争，获取方法必须首先指示它们需要一个信号，
         *               然后重试原子获取，然后在失败时阻塞。
         *   CANCELLED:  由于超时或中断，此节点已被取消。
         *               节点永远不会离开此状态。特别是，
         *               拥有已取消节点的线程永远不会再次阻塞。
         *   CONDITION:  该节点当前在条件队列上。
         *               在传输之前，它不会用作同步队列节点，
         *               在传输时，状态将被设置为 0。（在此处使用此值与字段的其他用途无关，
         *               但简化了机制。）
         *   PROPAGATE:  一个 releaseShared 应该传播到其他节点。
         *               这是在 doReleaseShared 中为头节点设置的，以确保传播继续，
         *               即使其他操作已经介入。
         *   0:          以上都不是
         *
         * 值按数字排列以简化使用。
         * 非负值表示节点不需要信号。因此，大多数代码不需要检查特定值，只需检查符号。
         *
         * 该字段对于普通同步节点初始化为 0，对于条件节点初始化为 CONDITION。它使用 CAS 修改
         * （或在可能的情况下，无条件的易失性写入）。
         */
        volatile int waitStatus;

        /**
         * 链接到当前节点/线程依赖于检查 waitStatus 的前驱节点。
         * 在入队时分配，并在出队时（为了 GC）清空。
         * 此外，在前驱节点取消时，我们会绕过找到一个未取消的前驱节点，该节点总是存在
         * 因为头节点永远不会被取消：节点只有在成功获取后才会成为头节点。
         * 取消的线程永远不会成功获取，线程只会取消自己，而不是任何其他节点。
         */
        volatile Node prev;

        /**
         * 链接到当前节点/线程在释放时唤醒的后继节点。
         * 在入队时分配，当绕过已取消的前驱节点时调整，并在出队时（为了 GC）清空。
         * 入队操作不会在连接后立即将前驱节点的 next 字段分配给当前节点，因此看到 null 的 next 字段
         * 并不一定意味着节点在队列的末尾。然而，如果 next 字段看起来是 null，我们可以从尾部扫描 prev
         * 以进行双重检查。已取消节点的 next 字段设置为指向节点本身而不是 null，以使 isOnSyncQueue 更容易。
         */
        volatile Node next;

        /**
         * 入队此节点的线程。在构造时初始化并在使用后清空。
         */
        volatile Thread thread;

        /**
         * 链接到下一个等待条件的节点，或特殊值 SHARED。
         * 因为条件队列仅在独占模式下访问，所以我们只需要一个简单的链表队列来保持节点
         * 在它们等待条件时。然后将它们传输到队列以重新获取。并且因为条件只能是独占的，
         * 我们通过使用特殊值来表示共享模式来节省一个字段。
         */
        Node nextWaiter;

        /**
         * 如果节点以共享模式等待，则返回 true。
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回前驱节点，如果为 null 则抛出 NullPointerException。
         * 在前驱节点不能为 null 时使用。可以省略 null 检查，但为了帮助 VM 而保留。
         *
         * @return 此节点的前驱节点
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // 用于建立初始头节点或 SHARED 标记
        }

        Node(Thread thread, Node mode) {     // 用于 addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // 用于 Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * 等待队列的头节点，惰性初始化。除了初始化外，仅通过 setHead 方法修改。
     * 注意：如果头节点存在，其 waitStatus 保证不会是 CANCELLED。
     */
    private transient volatile Node head;

    /**
     * 等待队列的尾节点，惰性初始化。仅通过 enq 方法添加新的等待节点时修改。
     */
    private transient volatile Node tail;

    /**
     * 同步状态。
     */
    private volatile long state;

                /**
     * 返回同步状态的当前值。
     * 此操作具有 {@code volatile} 读取的内存语义。
     * @return 当前状态值
     */
    protected final long getState() {
        return state;
    }

    /**
     * 设置同步状态的值。
     * 此操作具有 {@code volatile} 写入的内存语义。
     * @param newState 新的状态值
     */
    protected final void setState(long newState) {
        state = newState;
    }

    /**
     * 如果当前状态值等于预期值，则原子地将同步状态设置为给定的更新值。
     * 此操作具有 {@code volatile} 读取和写入的内存语义。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。如果实际值不等于预期值，则返回 {@code false}。
     */
    protected final boolean compareAndSetState(long expect, long update) {
        // 请参阅下面的内联设置以支持此操作
        return unsafe.compareAndSwapLong(this, stateOffset, expect, update);
    }

    // 队列工具

    /**
     * 在使用定时等待之前，自旋比使用定时等待更快的纳秒数。粗略估计即可
     * 改善非常短的超时响应。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * 将节点插入队列，必要时初始化。参见上图。
     * @param node 要插入的节点
     * @return 节点的前驱
     */
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // 必须初始化
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 为当前线程和给定模式创建并入队节点。
     *
     * @param mode Node.EXCLUSIVE 用于独占，Node.SHARED 用于共享
     * @return 新节点
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // 尝试快速路径入队；失败时回退到完整入队
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    /**
     * 将队列的头设置为节点，从而出队。仅由获取方法调用。
     * 还清空未使用的字段以利于垃圾回收并抑制不必要的信号和遍历。
     *
     * @param node 节点
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * 唤醒节点的后继，如果存在的话。
     *
     * @param node 节点
     */
    private void unparkSuccessor(Node node) {
        /*
         * 如果状态为负（即，可能需要信号）尝试提前清除以准备发送信号。
         * 如果此操作失败或状态被等待线程更改，也没有关系。
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * 要唤醒的线程保存在后继中，通常只是下一个节点。
         * 但如果已取消或显然为空，则从尾部向后遍历以找到实际的
         * 未取消的后继。
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }

    /**
     * 共享模式下的释放操作——唤醒后继并确保传播。（注意：对于独占模式，释放仅需
     * 在头节点需要信号时调用其 unparkSuccessor。）
     */
    private void doReleaseShared() {
        /*
         * 确保释放传播，即使有其他正在进行的获取/释放操作也是如此。
         * 这通常通过尝试唤醒头节点的后继来完成，如果需要信号的话。
         * 但如果不需要，状态将被设置为 PROPAGATE 以确保释放时继续传播。
         * 此外，我们必须循环，以防在此过程中添加了新节点。
         * 另外，与使用 unparkSuccessor 的其他情况不同，我们需要知道 CAS 重置状态是否失败，
         * 如果失败，则重新检查。
         */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // 循环重新检查情况
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // 循环处理失败的 CAS
            }
            if (h == head)                   // 如果头节点改变则循环
                break;
        }
    }

    /**
     * 将队列的头设置为节点，并检查后继是否可能在共享模式下等待，
     * 如果是，则根据 propagate > 0 或设置了 PROPAGATE 状态进行传播。
     *
     * @param node 节点
     * @param propagate tryAcquireShared 的返回值
     */
    private void setHeadAndPropagate(Node node, long propagate) {
        Node h = head; // 记录旧头节点以供下面检查
        setHead(node);
        /*
         * 如果满足以下条件之一，则尝试唤醒下一个排队的节点：
         *   调用者指示传播，
         *     或者之前的操作（在 setHead 之前或之后）记录了传播
         *     （注意：这里使用 waitStatus 的符号检查，因为
         *      PROPAGATE 状态可能会转换为 SIGNAL。）
         * 并且
         *   下一个节点在共享模式下等待，
         *     或者我们不知道，因为它看起来是 null
         *
         * 这两个检查中的保守性可能导致不必要的唤醒，但只有在有多个
         * 竞争获取/释放时才会发生，因此大多数情况下现在或不久后需要信号。
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }


                // 各种版本的acquire的工具方法

    /**
     * 取消正在进行的获取尝试。
     *
     * @param node 节点
     */
    private void cancelAcquire(Node node) {
        // 如果节点不存在，则忽略
        if (node == null)
            return;

        node.thread = null;

        // 跳过已取消的前驱节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext 是要解链的明显节点。如果CAS失败，说明我们输掉了与另一个取消或信号的竞争，因此不需要进一步操作。
        Node predNext = pred.next;

        // 这里可以使用无条件写入而不是CAS。
        // 在这个原子步骤之后，其他节点可以跳过我们。
        // 在此之前，我们不受其他线程的干扰。
        node.waitStatus = Node.CANCELLED;

        // 如果我们是尾节点，移除我们自己。
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // 如果后继节点需要信号，尝试设置前驱节点的下一个链接
            // 以便它会得到一个信号。否则唤醒它以传播。
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // 帮助GC
        }
    }

    /**
     * 检查并更新获取失败的节点的状态。
     * 如果线程应该阻塞，则返回true。这是所有获取循环中的主要信号控制。
     * 要求 pred == node.prev。
     *
     * @param pred 节点的前驱，持有状态
     * @param node 节点
     * @return {@code true} 如果线程应该阻塞
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * 该节点已经设置了状态，请求释放时向其发送信号，
             * 因此它可以安全地阻塞。
             */
            return true;
        if (ws > 0) {
            /*
             * 前驱被取消。跳过前驱并指示重试。
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus 必须是 0 或 PROPAGATE。指示我们需要一个信号，但不立即阻塞。
             * 调用者需要重试以确保在阻塞前无法获取。
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 方便方法，中断当前线程。
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * 方便方法，阻塞并检查是否被中断
     *
     * @return {@code true} 如果被中断
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /*
     * 各种版本的acquire，根据独占/共享和控制模式的不同而有所不同。
     * 每个版本大部分相同，但令人烦恼地不同。由于异常机制（包括确保在tryAcquire抛出异常时取消）和其他控制的交互，
     * 只能进行少量的重构，否则会严重影响性能。
     */

    /**
     * 以独占且不可中断模式为已经在队列中的线程获取。
     * 也用于条件等待方法和获取。
     *
     * @param node 节点
     * @param arg 获取参数
     * @return {@code true} 如果等待时被中断
     */
    final boolean acquireQueued(final Node node, long arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // 帮助GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以独占且可中断模式获取。
     * @param arg 获取参数
     */
    private void doAcquireInterruptibly(long arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // 帮助GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以独占且带超时模式获取。
     *
     * @param arg 获取参数
     * @param nanosTimeout 最大等待时间
     * @return {@code true} 如果获取成功
     */
    private boolean doAcquireNanos(long arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // 帮助GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

                /**
     * 以共享模式不可中断地获取。
     * @param arg 获取参数
     */
    private void doAcquireShared(long arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    long r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // 帮助垃圾回收
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以共享模式可中断地获取。
     * @param arg 获取参数
     */
    private void doAcquireSharedInterruptibly(long arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    long r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // 帮助垃圾回收
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以共享模式定时获取。
     *
     * @param arg 获取参数
     * @param nanosTimeout 最大等待时间
     * @return 如果获取成功则返回 {@code true}
     */
    private boolean doAcquireSharedNanos(long arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    long r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // 帮助垃圾回收
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    // 主要导出的方法

    /**
     * 尝试以独占模式获取。此方法应查询对象的状态是否允许以独占模式获取，如果允许，则获取。
     *
     * <p>此方法始终由执行获取的线程调用。如果此方法报告失败，获取方法可能会将线程排队（如果尚未排队），直到被其他线程释放后收到信号。这可以用于实现方法 {@link Lock#tryLock()}。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 获取参数。此值始终是传递给获取方法的值，或者是在条件等待时保存的值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果成功则返回 {@code true}。成功后，此对象已被获取。
     * @throws IllegalMonitorStateException 如果获取会使此同步器处于非法状态。此异常必须以一致的方式抛出，以确保同步正确工作。
     * @throws UnsupportedOperationException 如果不支持独占模式
     */
    protected boolean tryAcquire(long arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试设置状态以反映独占模式下的释放。
     *
     * <p>此方法始终由执行释放的线程调用。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 释放参数。此值始终是传递给释放方法的值，或者是在条件等待时的当前状态值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果此对象现在处于完全释放状态，则返回 {@code true}，因此任何等待的线程都可以尝试获取；否则返回 {@code false}。
     * @throws IllegalMonitorStateException 如果释放会使此同步器处于非法状态。此异常必须以一致的方式抛出，以确保同步正确工作。
     * @throws UnsupportedOperationException 如果不支持独占模式
     */
    protected boolean tryRelease(long arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试以共享模式获取。此方法应查询对象的状态是否允许以共享模式获取，如果允许，则获取。
     *
     * <p>此方法始终由执行获取的线程调用。如果此方法报告失败，获取方法可能会将线程排队（如果尚未排队），直到被其他线程释放后收到信号。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 获取参数。此值始终是传递给获取方法的值，或者是在条件等待时保存的值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果失败则返回负值；如果共享模式下的获取成功但后续的共享模式获取不能成功，则返回零；如果共享模式下的获取成功且后续的共享模式获取也可能成功，则返回正值，此时后续等待的线程必须检查可用性。（支持三种不同的返回值使此方法可以在仅有时独占获取的上下文中使用。）成功后，此对象已被获取。
     * @throws IllegalMonitorStateException 如果获取会使此同步器处于非法状态。此异常必须以一致的方式抛出，以确保同步正确工作。
     * @throws UnsupportedOperationException 如果不支持共享模式
     */
    protected long tryAcquireShared(long arg) {
        throw new UnsupportedOperationException();
    }


    /**
     * 尝试将状态设置为反映共享模式下的释放。
     *
     * <p>此方法总是由执行释放操作的线程调用。
     *
     * <p>默认实现抛出
     * {@link UnsupportedOperationException}。
     *
     * @param arg 释放参数。这个值总是传递给释放方法，或者是在条件等待进入时的当前状态值。该值未被解释，可以表示任何你想要的内容。
     * @return 如果此共享模式的释放可能允许等待的获取（共享或独占）成功，则返回 {@code true}；否则返回 {@code false}
     * @throws IllegalMonitorStateException 如果释放会使此同步器处于非法状态。为了同步正确工作，必须以一致的方式抛出此异常。
     * @throws UnsupportedOperationException 如果不支持共享模式
     */
    protected boolean tryReleaseShared(long arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果当前（调用）线程独占地持有同步，则返回 {@code true}。此方法在每次调用非等待的 {@link ConditionObject} 方法时被调用。
     * （等待方法反而调用 {@link #release}。）
     *
     * <p>默认实现抛出 {@link
     * UnsupportedOperationException}。此方法仅在 {@link ConditionObject} 方法内部调用，因此如果未使用条件，则无需定义。
     *
     * @return 如果同步独占地持有，则返回 {@code true}；
     *         否则返回 {@code false}
     * @throws UnsupportedOperationException 如果不支持条件
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * 以独占模式获取，忽略中断。通过至少调用一次 {@link #tryAcquire} 实现，
     * 成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用 {@link
     * #tryAcquire} 直到成功。此方法可以用于实现方法 {@link Lock#lock}。
     *
     * @param arg 获取参数。此值传递给
     *        {@link #tryAcquire} 但除此之外未被解释，可以表示任何你想要的内容。
     */
    public final void acquire(long arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * 以独占模式获取，如果被中断则中止。
     * 通过首先检查中断状态，然后至少调用一次 {@link #tryAcquire} 实现，成功时返回。
     * 否则，线程将被排队，可能反复阻塞和解除阻塞，调用 {@link #tryAcquire}
     * 直到成功或线程被中断。此方法可以用于实现方法 {@link Lock#lockInterruptibly}。
     *
     * @param arg 获取参数。此值传递给
     *        {@link #tryAcquire} 但除此之外未被解释，可以表示任何你想要的内容。
     * @throws InterruptedException 如果当前线程被中断
     */
    public final void acquireInterruptibly(long arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * 以独占模式尝试获取，如果被中断则中止，如果给定的超时时间过期则失败。
     * 通过首先检查中断状态，然后至少调用一次 {@link
     * #tryAcquire} 实现，成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用
     * {@link #tryAcquire} 直到成功或线程被中断或超时时间过期。此方法可以用于实现方法 {@link Lock#tryLock(long, TimeUnit)}。
     *
     * @param arg 获取参数。此值传递给
     *        {@link #tryAcquire} 但除此之外未被解释，可以表示任何你想要的内容。
     * @param nanosTimeout 最多等待的纳秒数
     * @return 如果获取成功，则返回 {@code true}；如果超时，则返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     */
    public final boolean tryAcquireNanos(long arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * 以独占模式释放。如果 {@link #tryRelease} 返回 true，则通过解除一个或多个线程的阻塞来实现。
     * 此方法可以用于实现方法 {@link Lock#unlock}。
     *
     * @param arg 释放参数。此值传递给
     *        {@link #tryRelease} 但除此之外未被解释，可以表示任何你想要的内容。
     * @return 从 {@link #tryRelease} 返回的值
     */
    public final boolean release(long arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**
     * 以共享模式获取，忽略中断。通过首先至少调用一次 {@link #tryAcquireShared} 实现，
     * 成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用 {@link
     * #tryAcquireShared} 直到成功。
     *
     * @param arg 获取参数。此值传递给
     *        {@link #tryAcquireShared} 但除此之外未被解释，
     *        可以表示任何你想要的内容。
     */
    public final void acquireShared(long arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

                /**
     * 以共享模式获取，如果被中断则中止。实现方式是首先检查中断状态，然后至少调用一次
     * {@link #tryAcquireShared}，成功时返回。否则，线程将被排队，可能会反复阻塞和解除阻塞，
     * 调用 {@link #tryAcquireShared} 直到成功或线程被中断。
     * @param arg 获取参数。此值传递给 {@link #tryAcquireShared} 但除此之外未被解释，可以表示任何内容。
     * @throws InterruptedException 如果当前线程被中断
     */
    public final void acquireSharedInterruptibly(long arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * 尝试以共享模式获取，如果被中断则中止，如果给定的超时时间过去则失败。实现方式是首先检查中断状态，然后至少调用一次 {@link
     * #tryAcquireShared}，成功时返回。否则，线程将被排队，可能会反复阻塞和解除阻塞，
     * 调用 {@link #tryAcquireShared} 直到成功或线程被中断或超时时间过去。
     *
     * @param arg 获取参数。此值传递给 {@link #tryAcquireShared} 但除此之外未被解释，可以表示任何内容。
     * @param nanosTimeout 最大等待的纳秒数
     * @return 如果获取成功返回 {@code true}；如果超时返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     */
    public final boolean tryAcquireSharedNanos(long arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * 以共享模式释放。如果 {@link #tryReleaseShared} 返回 true，则解除一个或多个线程的阻塞。
     *
     * @param arg 释放参数。此值传递给 {@link #tryReleaseShared} 但除此之外未被解释，可以表示任何内容。
     * @return 从 {@link #tryReleaseShared} 返回的值
     */
    public final boolean releaseShared(long arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // 队列检查方法

    /**
     * 查询是否有线程正在等待获取。请注意，由于中断和超时引起的取消可能随时发生，
     * 因此 {@code true} 的返回值不能保证任何其他线程将最终获取。
     *
     * <p>在此实现中，此操作在常数时间内返回。
     *
     * @return 如果可能有其他线程正在等待获取，则返回 {@code true}
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 查询是否有线程曾经争夺过此同步器的获取；即是否有获取方法曾经阻塞。
     *
     * <p>在此实现中，此操作在常数时间内返回。
     *
     * @return 如果曾经有过争夺，则返回 {@code true}
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 返回队列中的第一个（等待时间最长的）线程，如果没有线程当前正在排队，则返回
     * {@code null}。
     *
     * <p>在此实现中，此操作通常在常数时间内返回，但在其他线程同时修改队列时可能会迭代。
     *
     * @return 队列中的第一个（等待时间最长的）线程，如果没有线程当前正在排队，则返回
     *         {@code null}
     */
    public final Thread getFirstQueuedThread() {
        // 只处理快速路径，否则委托
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * 当快速路径失败时调用的 getFirstQueuedThread 版本
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * 第一个节点通常是 head.next。尝试获取其线程字段，确保一致的读取：如果线程
         * 字段被置为 null 或 s.prev 不再是 head，则其他线程(s) 在我们的某些读取之间并发执行了 setHead。
         * 我们尝试两次，如果失败则转而进行遍历。
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head 的 next 字段可能尚未设置，或者可能在 setHead 之后被取消设置。因此，我们必须检查
         * 尾部是否实际上是第一个节点。如果不是，我们继续安全地从尾部遍历到头部以找到第一个，
         * 确保终止。
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * 如果给定的线程当前正在排队，则返回 true。
     *
     * <p>此实现遍历队列以确定给定线程的存在。
     *
     * @param thread 线程
     * @return 如果给定的线程在队列上，则返回 {@code true}
     * @throws NullPointerException 如果线程为 null
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }


                /**
     * 如果存在一个明显处于队列首位的线程，返回 {@code true}，并且该线程处于独占模式。如果此方法返回
     * {@code true}，并且当前线程正在尝试以共享模式获取（即，此方法是从 {@link
     * #tryAcquireShared} 调用的），则可以保证当前线程不是队列中的第一个线程。仅在 ReentrantReadWriteLock 中作为启发式方法使用。
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * 查询是否有任何线程等待获取的时间比当前线程更长。
     *
     * <p>此方法的调用等效于（但可能更高效）：
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>由于中断和超时可能导致取消，因此 {@code true} 的返回值并不保证其他线程会在当前线程之前获取。
     * 同样，如果此方法返回 {@code false}，则可能有另一个线程在队列为空时赢得入队竞争。
     *
     * <p>此方法设计用于公平同步器，以避免 <a href="AbstractQueuedSynchronizer.html#barging">插队</a>。
     * 这样的同步器的 {@link #tryAcquire} 方法应该在返回 {@code true} 时返回 {@code false}，
     * 其 {@link #tryAcquireShared} 方法应该在返回负值时返回 {@code true}（除非这是重新进入获取）。
     * 例如，一个公平的、可重入的、独占模式同步器的 {@code tryAcquire} 方法可能如下所示：
     *
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // 重新进入获取；增加持有计数
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false;
     *   } else {
     *     // 尝试正常获取
     *   }
     * }}</pre>
     *
     * @return 如果有队列中的线程先于当前线程，则返回 {@code true}；如果当前线程位于队列头部或队列为空，则返回 {@code false}
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // 此方法的正确性取决于 head 在 tail 之前初始化，并且如果当前线程是队列中的第一个线程，则 head.next 必须准确。
        Node t = tail; // 以相反的初始化顺序读取字段
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }


    // 仪器和监控方法

    /**
     * 返回一个估计的正在等待获取的线程数量。此值只是一个估计值，因为线程数量可能会在该方法遍历内部数据结构时动态变化。
     * 此方法设计用于监控系统状态，而不是同步控制。
     *
     * @return 等待获取的线程数量的估计值
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * 返回一个包含可能正在等待获取的线程的集合。由于实际的线程集可能会在构建此结果时动态变化，因此返回的集合只是一个最佳估计。
     * 返回的集合中的元素没有特定的顺序。此方法设计用于构建提供更广泛的监控设施的子类。
     *
     * @return 线程的集合
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * 返回一个包含可能正在等待以独占模式获取的线程的集合。这具有与 {@link #getQueuedThreads} 相同的属性，但只返回那些由于独占获取而等待的线程。
     *
     * @return 线程的集合
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * 返回一个包含可能正在等待以共享模式获取的线程的集合。这具有与 {@link #getQueuedThreads} 相同的属性，但只返回那些由于共享获取而等待的线程。
     *
     * @return 线程的集合
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * 返回一个标识此同步器及其状态的字符串。状态（用括号括起来）包括字符串 {@code "State ="}
     * 后跟 {@link #getState} 的当前值，以及根据队列是否为空的 {@code "nonempty"} 或 {@code "empty"}。
     *
     * @return 一个标识此同步器及其状态的字符串
     */
    public String toString() {
        long s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }

    // 内部支持条件的方法

    /**
     * 如果一个节点（最初被放置在条件队列上的节点）现在正在等待重新获取同步队列，则返回 true。
     * @param node 节点
     * @return 如果正在重新获取，则返回 true
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // 如果有后继节点，它必须在队列上
            return true;
        /*
         * node.prev 可以是非空的，但尚未在队列上，因为将其放置在队列上的 CAS 可能会失败。因此我们必须
         * 从尾部遍历以确保它实际上已经到达。在调用此方法时，它总是接近尾部，除非 CAS 失败（这不太可能），它将
         * 在那里，所以我们几乎从不遍历太多。
         */
        return findNodeFromTail(node);
    }

    /**
     * 通过从尾部向后搜索来确定节点是否在同步队列上。仅在 isOnSyncQueue 需要时调用。
     * @return 如果存在，则返回 true
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * 将节点从条件队列转移到同步队列。如果成功则返回 true。
     * @param node 节点
     * @return 如果成功转移（否则节点在信号之前被取消）则返回 true
     */
    final boolean transferForSignal(Node node) {
        /*
         * 如果不能更改 waitStatus，节点已被取消。
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * 拼接到队列上，并尝试设置前驱节点的 waitStatus 以表示线程（可能）正在等待。如果取消或
         * 设置 waitStatus 失败，则唤醒以重新同步（在这种情况下，waitStatus 可以暂时且无害地错误）。
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    /**
     * 在取消等待后，如果必要，将节点转移到同步队列。如果线程在节点被信号之前被取消，则返回 true。
     *
     * @param node 节点
     * @return 如果在节点被信号之前被取消，则返回 true
     */
    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        /*
         * 如果我们输给了一个信号（），那么我们必须等到它完成其 enq（）。在不完整的转移期间取消
         * 是罕见且短暂的，所以只需自旋。
         */
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**
     * 使用当前状态值调用 release；返回保存的状态。如果失败，则取消节点并抛出异常。
     * @param node 本次等待的条件节点
     * @return 之前的同步状态
     */
    final long fullyRelease(Node node) {
        boolean failed = true;
        try {
            long savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    // 条件的监控方法

    /**
     * 查询给定的 ConditionObject 是否使用此同步器作为其锁。
     *
     * @param condition 条件
     * @return 如果拥有，则返回 {@code true}
     * @throws NullPointerException 如果条件为 null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * 查询是否有线程正在等待与此同步器关联的给定条件。注意，由于超时和中断可能随时发生，因此
     * 返回 {@code true} 并不保证未来的 {@code signal} 会唤醒任何线程。此方法主要用于
     * 监控系统状态。
     *
     * @param condition 条件
     * @return 如果有等待的线程，则返回 {@code true}
     * @throws IllegalMonitorStateException 如果没有持有独占同步
     * @throws IllegalArgumentException 如果给定的条件不与此同步器关联
     * @throws NullPointerException 如果条件为 null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * 返回与此同步器关联的给定条件的等待线程数的估计值。注意，由于超时和中断可能随时发生，因此
     * 估计值仅作为实际等待线程数的上限。此方法主要用于监控系统状态，而不是同步控制。
     *
     * @param condition 条件
     * @return 等待线程的估计数
     * @throws IllegalMonitorStateException 如果没有持有独占同步
     * @throws IllegalArgumentException 如果给定的条件不与此同步器关联
     * @throws NullPointerException 如果条件为 null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }


                /**
     * 返回一个集合，包含可能正在等待与此同步器关联的给定条件的线程。由于在构建此结果时实际的线程集可能会动态变化，因此返回的集合只是一个尽力而为的估计。返回的集合中的元素没有特定的顺序。
     *
     * @param condition 条件
     * @return 线程集合
     * @throws IllegalMonitorStateException 如果没有持有独占同步
     * @throws IllegalArgumentException 如果给定的条件与此同步器不关联
     * @throws NullPointerException 如果条件为 null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * 为 {@link AbstractQueuedLongSynchronizer} 提供的条件实现，作为 {@link Lock} 实现的基础。
     *
     * <p>此类的方法文档描述了机制，而不是从 Lock 和 Condition 用户的角度描述的行为规范。此类的导出版本通常需要附带描述条件语义的文档，这些语义依赖于关联的 {@code AbstractQueuedLongSynchronizer}。
     *
     * <p>此类是可序列化的，但所有字段都是瞬态的，因此反序列化的条件没有等待者。
     *
     * @since 1.6
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        /** 条件队列的第一个节点。 */
        private transient Node firstWaiter;
        /** 条件队列的最后一个节点。 */
        private transient Node lastWaiter;

        /**
         * 创建一个新的 {@code ConditionObject} 实例。
         */
        public ConditionObject() { }

        // 内部方法

        /**
         * 将新的等待者添加到等待队列。
         * @return 新的等待节点
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // 如果 lastWaiter 被取消，则清除。
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * 移除并转移节点，直到遇到非取消的节点或 null。部分从 signal 中分离出来，以鼓励编译器内联没有等待者的情况。
         * @param first (非空) 条件队列上的第一个节点
         */
        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        /**
         * 移除并转移所有节点。
         * @param first (非空) 条件队列上的第一个节点
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * 从条件队列中取消链接已取消的等待者节点。仅在持有锁时调用。当在条件等待期间发生取消，或在插入新等待者时看到 lastWaiter 被取消时调用。此方法需要避免在没有信号的情况下保留垃圾。因此，即使它可能需要完全遍历，也仅在没有信号的情况下发生超时或取消时才起作用。它遍历所有节点，而不是在特定目标处停止，以在取消风暴期间无需多次重新遍历即可取消链接所有指向垃圾节点的指针。
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        // 公共方法

        /**
         * 如果存在，将等待时间最长的线程从该条件的等待队列移动到拥有锁的等待队列。
         *
         * @throws IllegalMonitorStateException 如果 {@link #isHeldExclusively}
         *         返回 {@code false}
         */
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        /**
         * 将所有线程从该条件的等待队列移动到拥有锁的等待队列。
         *
         * @throws IllegalMonitorStateException 如果 {@link #isHeldExclusively}
         *         返回 {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }


                    /**
         * 实现不可中断的条件等待。
         * <ol>
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用 {@link #release} 并将保存的状态作为参数，如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒。
         * <li> 通过调用 {@link #acquire} 的专门版本并以保存的状态作为参数来重新获取锁。
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            long savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /*
         * 对于可中断的等待，我们需要跟踪是否在被条件阻塞时抛出
         * InterruptedException，或者在等待重新获取锁时被中断。
         */

        /** 模式表示在等待退出时重新中断 */
        private static final int REINTERRUPT =  1;
        /** 模式表示在等待退出时抛出 InterruptedException */
        private static final int THROW_IE    = -1;

        /**
         * 检查中断，如果在被信号唤醒前被中断则返回 THROW_IE，如果在被信号唤醒后被中断则返回 REINTERRUPT，如果没有被中断则返回 0。
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * 根据模式抛出 InterruptedException，重新中断当前线程，或者什么都不做。
         */
        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * 实现可中断的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用 {@link #release} 并将保存的状态作为参数，如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒或被中断。
         * <li> 通过调用 {@link #acquire} 的专门版本并以保存的状态作为参数来重新获取锁。
         * <li> 如果在第 4 步被阻塞时被中断，抛出 InterruptedException。
         * </ol>
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            long savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // 清理已取消的等待者
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * 实现带超时的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用 {@link #release} 并将保存的状态作为参数，如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒、被中断或超时。
         * <li> 通过调用 {@link #acquire} 的专门版本并以保存的状态作为参数来重新获取锁。
         * <li> 如果在第 4 步被阻塞时被中断，抛出 InterruptedException。
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            long savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * 实现绝对时间带超时的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用 {@link #release} 并将保存的状态作为参数，如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒、被中断或超时。
         * <li> 通过调用 {@link #acquire} 的专门版本并以保存的状态作为参数来重新获取锁。
         * <li> 如果在第 4 步被阻塞时被中断，抛出 InterruptedException。
         * <li> 如果在第 4 步被阻塞时超时，返回 false，否则返回 true。
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            long savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }


                    /**
         * 实现定时条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用 {@link #release} 并将保存的状态作为参数传递，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号、中断或超时。
         * <li> 通过调用 {@link #acquire} 的专用版本并使用保存的状态作为参数重新获取锁。
         * <li> 如果在第 4 步被阻塞时被中断，抛出 InterruptedException。
         * <li> 如果在第 4 步被阻塞时超时，返回 false，否则返回 true。
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            long savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  支持工具

        /**
         * 如果此条件是由给定的同步对象创建的，则返回 true。
         *
         * @return 如果拥有，则返回 {@code true}
         */
        final boolean isOwnedBy(AbstractQueuedLongSynchronizer sync) {
            return sync == AbstractQueuedLongSynchronizer.this;
        }

        /**
         * 查询是否有线程正在等待此条件。
         * 实现 {@link AbstractQueuedLongSynchronizer#hasWaiters(ConditionObject)}。
         *
         * @return 如果有等待的线程，则返回 {@code true}
         * @throws IllegalMonitorStateException 如果 {@link #isHeldExclusively}
         *         返回 {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * 返回一个估计值，表示有多少线程正在等待此条件。
         * 实现 {@link AbstractQueuedLongSynchronizer#getWaitQueueLength(ConditionObject)}。
         *
         * @return 等待线程的估计数量
         * @throws IllegalMonitorStateException 如果 {@link #isHeldExclusively}
         *         返回 {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * 返回一个集合，包含可能正在等待此条件的线程。
         * 实现 {@link AbstractQueuedLongSynchronizer#getWaitingThreads(ConditionObject)}。
         *
         * @return 线程的集合
         * @throws IllegalMonitorStateException 如果 {@link #isHeldExclusively}
         *         返回 {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * 设置以支持 compareAndSet。我们需要在此处本地实现这一点：为了允许未来的增强，我们不能显式地继承 AtomicLong，这本来是高效且有用的。因此，作为较小的恶，我们使用 hotspot 内在 API 本地实现。同时，我们也为其他 CAS 字段这样做（这些字段可以使用原子字段更新器实现）。
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedLongSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedLongSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedLongSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

                    } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS头字段。仅由enq使用。
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS尾字段。仅由enq使用。
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS节点的waitStatus字段。
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS节点的next字段。
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
