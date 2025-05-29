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

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * 提供了一个实现阻塞锁及相关同步器（信号量、事件等）的框架，这些同步器依赖于先进先出（FIFO）等待队列。本类设计为大多数依赖于单个原子 {@code int} 值表示状态的同步器的有用基础。子类必须定义改变此状态的受保护方法，并定义该状态在获取或释放此对象方面的含义。给定这些方法，本类中的其他方法执行所有排队和阻塞机制。子类可以维护其他状态字段，但只有使用 {@link #getState}、{@link #setState} 和 {@link #compareAndSetState} 方法操作的原子更新的 {@code int} 值在同步方面被跟踪。
 *
 * <p>子类应定义为非公共内部帮助类，用于实现其封闭类的同步属性。类
 * {@code AbstractQueuedSynchronizer} 未实现任何同步接口。相反，它定义了可以由具体锁和相关同步器根据需要调用的方法，如 {@link #acquireInterruptibly}，以实现它们的公共方法。
 *
 * <p>本类支持默认的 <em>独占</em> 模式和/或 <em>共享</em> 模式。当以独占模式获取时，其他线程的尝试获取不能成功。多个线程以共享模式获取可能（但不一定）成功。此类不“理解”这些差异，除了在机械意义上，当共享模式获取成功时，下一个等待的线程（如果存在）也必须确定它是否可以获取。不同模式下等待的线程共享同一个 FIFO 队列。通常，实现子类只支持这些模式之一，但例如在 {@link ReadWriteLock} 中，两种模式都可以发挥作用。只支持独占模式或共享模式的子类不需要定义未使用模式的方法。
 *
 * <p>本类定义了一个嵌套的 {@link ConditionObject} 类，可以用作由支持独占模式的子类实现的 {@link Condition} 接口，其中方法 {@link #isHeldExclusively} 报告当前线程是否独占持有同步，方法 {@link #release} 调用时带有当前的 {@link #getState} 值，完全释放此对象，而 {@link #acquire} 给定此保存的状态值，最终恢复此对象到其先前的获取状态。没有其他 {@code AbstractQueuedSynchronizer} 方法创建这样的条件，因此如果不能满足此约束，则不要使用它。{@link ConditionObject} 的行为当然取决于其同步器实现的语义。
 *
 * <p>本类提供了内部队列的检查、工具和监控方法，以及条件对象的类似方法。这些方法可以根据需要导出到使用 {@code AbstractQueuedSynchronizer} 进行同步机制的类中。
 *
 * <p>本类的序列化仅存储维护状态的底层原子整数，因此反序列化的对象具有空的线程队列。通常需要序列化的子类将定义一个 {@code readObject} 方法，该方法在反序列化时将此恢复到已知的初始状态。
 *
 * <h3>使用</h3>
 *
 * <p>要使用本类作为同步器的基础，重新定义以下适用的方法，通过检查和/或修改使用 {@link #getState}、{@link #setState} 和/或 {@link #compareAndSetState} 的同步状态：
 *
 * <ul>
 * <li> {@link #tryAcquire}
 * <li> {@link #tryRelease}
 * <li> {@link #tryAcquireShared}
 * <li> {@link #tryReleaseShared}
 * <li> {@link #isHeldExclusively}
 * </ul>
 *
 * 每个这些方法默认都抛出 {@link
 * UnsupportedOperationException}。这些方法的实现必须是内部线程安全的，并且通常应该是简短的，不阻塞。定义这些方法是使用本类的<em>唯一</em>支持方式。所有其他方法都声明为 {@code final}，因为它们不能独立变化。
 *
 * <p>您可能会发现从 {@link
 * AbstractOwnableSynchronizer} 继承的方法对于跟踪独占同步器的所有者线程很有用。鼓励您使用它们——这使得监控和诊断工具能够帮助用户确定哪些线程持有锁。
 *
 * <p>尽管本类基于内部 FIFO 队列，但它并不自动强制执行 FIFO 获取策略。独占同步的核心形式如下：
 *
 * <pre>
 * 获取：
 *     while (!tryAcquire(arg)) {
 *        <em>如果线程尚未排队，则将其加入队列</em>;
 *        <em>可能阻塞当前线程</em>;
 *     }
 *
 * 释放：
 *     if (tryRelease(arg))
 *        <em>解除第一个排队线程的阻塞</em>;
 * </pre>
 *
 * （共享模式类似，但可能涉及级联信号。）
 *
 * <p id="barging">由于获取检查在排队之前调用，新获取的线程可能会<em>插队</em>，排在其他被阻塞和排队的线程之前。但是，如果您希望，可以定义 {@code tryAcquire} 和/或 {@code tryAcquireShared} 以通过调用一个或多个检查方法来禁用插队，从而提供<em>公平</em>的 FIFO 获取顺序。特别是，大多数公平同步器可以定义 {@code tryAcquire} 以在 {@link #hasQueuedPredecessors}（一个专门为公平同步器设计的方法）返回 {@code true} 时返回 {@code false}。其他变化也是可能的。
 *
 * <p>吞吐量和可扩展性通常最高的是默认插队（也称为<em>贪婪</em>、<em>放弃</em>和<em>避免车队</em>）策略。虽然不能保证公平或无饥饿，但较早排队的线程被允许在较晚排队的线程之前重新竞争，每次重新竞争都有与新来者无偏见的机会成功。此外，虽然获取不会以通常意义上的“自旋”进行，但在阻塞之前，它们可能会执行多次 {@code tryAcquire} 调用，这些调用与其他计算交织在一起。当独占同步仅短暂持有时，这提供了自旋的大部分好处，而没有大多数缺点。如果需要，您可以通过在调用获取方法之前添加“快速路径”检查，可能预先检查 {@link #hasContended} 和/或 {@link #hasQueuedThreads}，仅在同步器可能不被竞争时才这样做。
 *
 * <p>本类通过将其使用范围专门化为可以依赖于 {@code int} 状态、获取和释放参数以及内部 FIFO 等待队列的同步器，部分地提供了高效和可扩展的基础。当这不足以满足需求时，您可以使用 {@link java.util.concurrent.atomic atomic} 类、您自己的自定义 {@link java.util.Queue} 类和 {@link LockSupport} 阻塞支持从较低级别构建同步器。
 *
 * <h3>使用示例</h3>
 *
 * <p>以下是一个非可重入互斥锁类，使用值零表示解锁状态，使用一表示锁定状态。虽然非可重入锁严格来说不需要记录当前所有者线程，但此类无论如何都会这样做，以便更容易监控。它还支持条件并暴露一个工具方法：
 *
 *  <pre> {@code
 * class Mutex implements Lock, java.io.Serializable {
 *
 *   // 我们的内部帮助类
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     // 报告是否处于锁定状态
 *     protected boolean isHeldExclusively() {
 *       return getState() == 1;
 *     }
 *
 *     // 如果状态为零，则获取锁
 *     public boolean tryAcquire(int acquires) {
 *       assert acquires == 1; // 否则未使用
 *       if (compareAndSetState(0, 1)) {
 *         setExclusiveOwnerThread(Thread.currentThread());
 *         return true;
 *       }
 *       return false;
 *     }
 *
 *     // 通过将状态设置为零来释放锁
 *     protected boolean tryRelease(int releases) {
 *       assert releases == 1; // 否则未使用
 *       if (getState() == 0) throw new IllegalMonitorStateException();
 *       setExclusiveOwnerThread(null);
 *       setState(0);
 *       return true;
 *     }
 *
 *     // 提供一个条件
 *     Condition newCondition() { return new ConditionObject(); }
 *
 *     // 反序列化正确
 *     private void readObject(ObjectInputStream s)
 *         throws IOException, ClassNotFoundException {
 *       s.defaultReadObject();
 *       setState(0); // 重置为解锁状态
 *     }
 *   }
 *
 *   // 同步对象完成所有繁重的工作。我们只是转发给它。
 *   private final Sync sync = new Sync();
 *
 *   public void lock()                { sync.acquire(1); }
 *   public boolean tryLock()          { return sync.tryAcquire(1); }
 *   public void unlock()              { sync.release(1); }
 *   public Condition newCondition()   { return sync.newCondition(); }
 *   public boolean isLocked()         { return sync.isHeldExclusively(); }
 *   public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
 *   public void lockInterruptibly() throws InterruptedException {
 *     sync.acquireInterruptibly(1);
 *   }
 *   public boolean tryLock(long timeout, TimeUnit unit)
 *       throws InterruptedException {
 *     return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 *   }
 * }}</pre>
 *
 * <p>以下是一个类似于 {@link java.util.concurrent.CountDownLatch CountDownLatch} 的闩锁类，但只需要一个 {@code signal} 即可触发。因为闩锁是非独占的，所以它使用 {@code 共享} 获取和释放方法。
 *
 *  <pre> {@code
 * class BooleanLatch {
 *
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     boolean isSignalled() { return getState() != 0; }
 *
 *     protected int tryAcquireShared(int ignore) {
 *       return isSignalled() ? 1 : -1;
 *     }
 *
 *     protected boolean tryReleaseShared(int ignore) {
 *       setState(1);
 *       return true;
 *     }
 *   }
 *
 *   private final Sync sync = new Sync();
 *   public boolean isSignalled() { return sync.isSignalled(); }
 *   public void signal()         { sync.releaseShared(1); }
 *   public void await() throws InterruptedException {
 *     sync.acquireSharedInterruptibly(1);
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {


                private static final long serialVersionUID = 7373984972572414691L;

    /**
     * 创建一个新的 {@code AbstractQueuedSynchronizer} 实例
     * 初始同步状态为零。
     */
    protected AbstractQueuedSynchronizer() { }

    /**
     * 等待队列节点类。
     *
     * <p>等待队列是“CLH”（Craig, Landin, 和 Hagersten）锁队列的一种变体。CLH 锁通常用于自旋锁。
     * 我们将其用于阻塞同步器，但使用相同的基本策略，即在节点的前驱中保存一些线程的控制信息。
     * 每个节点中的“状态”字段跟踪线程是否应阻塞。当其前驱释放时，节点会被唤醒。
     * 队列中的每个节点还充当特定通知风格的监视器，持有单个等待线程。状态字段并不控制线程是否被授予锁等。
     * 如果线程在队列中排在第一位，它可以尝试获取锁。但排在第一位并不保证成功；它只是赋予了竞争的权利。
     * 因此，当前释放的竞争者线程可能需要重新等待。
     *
     * <p>要将节点插入 CLH 锁队列，您需要原子地将其作为新的尾节点拼接。要出队，只需设置头字段。
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
     *
     * <p>将节点插入 CLH 队列只需要对“尾”进行一次原子操作，因此从未排队到已排队有一个简单的原子分界点。
     * 同样，出队仅涉及更新“头”。然而，节点确定其后继者需要更多的工作，部分原因是处理由于超时和中断导致的可能取消。
     *
     * <p>“前驱”链接（在原始 CLH 锁中未使用）主要用于处理取消。如果节点被取消，其后继者通常会被重新链接到一个未取消的前驱。
     * 对于自旋锁中类似机制的解释，请参阅 Scott 和 Scherer 的论文：
     * http://www.cs.rochester.edu/u/scott/synchronization/
     *
     * <p>我们还使用“后继”链接来实现阻塞机制。每个节点的线程 ID 保存在其自己的节点中，因此前驱通过遍历后继链接来确定唤醒哪个线程。
     * 确定后继者必须避免与新排队的节点竞争以设置其前驱的“后继”字段。这是通过在节点的后继者似乎为 null 时，从原子更新的“尾”向后检查来解决的。
     * （或者换句话说，后继链接是一种优化，因此我们通常不需要向后扫描。）
     *
     * <p>取消引入了一些保守性。由于我们必须轮询其他节点的取消，我们可能会错过取消节点是在我们前面还是后面。
     * 这是通过在取消时总是唤醒后继者来处理的，允许它们稳定在一个新的前驱上，除非我们可以确定一个未取消的前驱来承担这个责任。
     *
     * <p>CLH 队列需要一个虚拟头节点来启动。但我们在构造时不会创建它们，因为如果从未出现竞争，这将是浪费。
     * 相反，节点是在第一次竞争时构造的，并设置头和尾指针。
     *
     * <p>等待条件的线程使用相同的节点，但使用额外的链接。条件队列只需要简单的（非并发）链接队列，因为它们仅在独占持有时访问。
     * 在等待时，节点被插入到条件队列中。在信号时，节点被转移到主队列。状态字段的特殊值用于标记节点在哪个队列上。
     *
     * <p>感谢 Dave Dice、Mark Moir、Victor Luchangco、Bill Scherer 和 Michael Scott，以及 JSR-166 专家小组的成员，
     * 他们为这个类的设计提供了有益的想法、讨论和批评。
     */
    static final class Node {
        /** 标记表示节点正在以共享模式等待 */
        static final Node SHARED = new Node();
        /** 标记表示节点正在以独占模式等待 */
        static final Node EXCLUSIVE = null;

        /** waitStatus 值表示线程已取消 */
        static final int CANCELLED =  1;
        /** waitStatus 值表示后继线程需要唤醒 */
        static final int SIGNAL    = -1;
        /** waitStatus 值表示线程正在等待条件 */
        static final int CONDITION = -2;
        /**
         * waitStatus 值表示下一个 acquireShared 应该无条件传播
         */
        static final int PROPAGATE = -3;

        /**
         * 状态字段，仅取以下值：
         *   SIGNAL:     该节点的后继（或即将）被阻塞（通过 park），因此当前节点在释放或取消时必须唤醒其后继。
         *               为了避免竞争，获取方法必须首先指示它们需要信号，
         *               然后重试原子获取，然后，
         *               在失败时，阻塞。
         *   CANCELLED:  该节点由于超时或中断而取消。
         *               节点永远不会离开此状态。特别是，
         *               具有取消节点的线程永远不会再次阻塞。
         *   CONDITION:  该节点当前在条件队列上。
         *               它不会作为同步队列节点使用，直到被转移，此时状态将被设置为 0。
         *               （在此处使用此值与字段的其他用途无关，但简化了机制。）
         *   PROPAGATE:  一个 releaseShared 应该传播到其他节点。这仅在头节点上设置，
         *               在 doReleaseShared 中设置，以确保即使其他操作已经介入，传播也会继续。
         *   0:          以上都不是
         *
         * 这些值按数字排列，以简化使用。非负值表示节点不需要信号。
         * 因此，大多数代码不需要检查特定值，只需检查符号。
         *
         * 该字段对于普通同步节点初始化为 0，对于条件节点初始化为 CONDITION。
         * 它使用 CAS（或在可能的情况下，无条件的易失性写入）进行修改。
         */
        volatile int waitStatus;


                    /**
         * 当前节点/线程依赖的前驱节点，用于检查 waitStatus。在入队时分配，并且仅在出队时（为了GC）置空。另外，在前驱节点被取消时，我们在查找未取消的节点时会走捷径，这样的节点总是存在的，因为头节点永远不会被取消：一个节点只有在成功获取后才会成为头节点。一个被取消的线程永远不会成功获取，线程只会取消自己，而不是任何其他节点。
         */
        volatile Node prev;

        /**
         * 当前节点/线程在释放时唤醒的后继节点。在入队时分配，当绕过已取消的前驱节点时调整，并且在出队时（为了GC）置空。入队操作不会在连接后立即将前驱节点的 next 字段分配，因此看到 null 的 next 字段并不一定意味着该节点位于队列末尾。然而，如果 next 字段看起来是 null，我们可以从尾部扫描 prev 来双重检查。已取消节点的 next 字段设置为指向节点本身而不是 null，以使 isOnSyncQueue 的操作更简单。
         */
        volatile Node next;

        /**
         * 入队此节点的线程。在构造时初始化并在使用后置空。
         */
        volatile Thread thread;

        /**
         * 等待条件的下一个节点，或特殊值 SHARED。因为条件队列仅在持有独占模式时访问，我们只需要一个简单的链式队列来保存节点，当它们等待条件时。然后它们被转移到队列以重新获取。并且因为条件只能是独占的，我们通过使用特殊值来表示共享模式来节省一个字段。
         */
        Node nextWaiter;

        /**
         * 如果节点处于共享模式，则返回 true。
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回前驱节点，如果为 null 则抛出 NullPointerException。当前驱节点不能为 null 时使用。null 检查可以省略，但为了帮助 VM 而保留。
         *
         * @return 该节点的前驱
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

        Node(Thread thread, Node mode) {     // 由 addWaiter 使用
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // 由 Condition 使用
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * 等待队列的头节点，惰性初始化。除了初始化外，仅通过 setHead 方法修改。注意：如果头节点存在，其 waitStatus 保证不是 CANCELLED。
     */
    private transient volatile Node head;

    /**
     * 等待队列的尾节点，惰性初始化。仅通过 enq 方法添加新的等待节点时修改。
     */
    private transient volatile Node tail;

    /**
     * 同步状态。
     */
    private volatile int state;

    /**
     * 返回同步状态的当前值。
     * 此操作具有 {@code volatile} 读取的内存语义。
     * @return 当前状态值
     */
    protected final int getState() {
        return state;
    }

    /**
     * 设置同步状态的值。
     * 此操作具有 {@code volatile} 写入的内存语义。
     * @param newState 新的状态值
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * 如果当前状态值等于预期值，则原子地将同步状态设置为给定的更新值。
     * 此操作具有 {@code volatile} 读取和写入的内存语义。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 false 表示实际值不等于预期值。
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // 请参阅下面的内联设置以支持此操作
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // 队列实用程序

    /**
     * 以纳秒为单位，旋转比使用计时停放更快的时间。粗略估计就足够了，以提高非常短的超时的响应性。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * 将节点插入队列，必要时初始化。参见上面的图片。
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
        // 尝试 enq 的快速路径；失败时回退到完整的 enq
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
     * 将队列的头部设置为节点，从而出队。仅由获取方法调用。同时为了垃圾回收和抑制不必要的信号和遍历，清空未使用的字段。
     *
     * @param node 节点
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * 如果存在后继节点，则唤醒节点的后继。
     *
     * @param node 节点
     */
    private void unparkSuccessor(Node node) {
        /*
         * 如果状态为负（即，可能需要信号）尝试提前清除以准备发送信号。如果此操作失败或等待线程更改状态，这没关系。
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * 要唤醒的线程保存在后继中，通常只是下一个节点。但如果已取消或显然为空，则从尾部向后遍历以找到实际的非取消后继。
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
     * 共享模式下的释放操作——信号后继并确保传播。（注意：对于独占模式，释放仅相当于如果头部需要信号，则调用unparkSuccessor。）
     */
    private void doReleaseShared() {
        /*
         * 确保释放传播，即使有其他正在进行的获取/释放操作。这通常通过尝试唤醒头部的后继来完成，如果它需要信号。但如果不需要，状态将设置为PROPAGATE以确保释放时继续传播。
         * 此外，我们必须循环以防止在执行此操作时添加新节点。与unparkSuccessor的其他使用不同，我们需要知道CAS重置状态是否失败，如果失败则重新检查。
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
                    continue;                // CAS失败时循环
            }
            if (h == head)                   // 如果头部改变则循环
                break;
        }
    }

    /**
     * 设置队列的头部，并检查后继是否可能在共享模式下等待，如果是，则在propagate > 0或设置了PROPAGATE状态时传播。
     *
     * @param node 节点
     * @param propagate tryAcquireShared的返回值
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // 记录旧头部以供下面检查
        setHead(node);
        /*
         * 如果满足以下条件之一，则尝试信号下一个排队的节点：
         *   调用者指示传播，
         *     或之前的操作（在setHead之前或之后）记录了传播
         *     （注意：此操作使用waitStatus的符号检查，因为PROPAGATE状态可能会转换为SIGNAL。）
         * 并且
         *   下一个节点在共享模式下等待，
         *     或我们不知道，因为它看起来是null
         *
         * 这两个检查的保守性可能导致不必要的唤醒，但仅在有多个竞争获取/释放时，因此大多数情况下现在或不久后需要信号。
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    // 各种版本的获取操作的工具方法

    /**
     * 取消正在进行的获取尝试。
     *
     * @param node 节点
     */
    private void cancelAcquire(Node node) {
        // 如果节点不存在则忽略
        if (node == null)
            return;

        node.thread = null;

        // 跳过已取消的前驱
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext 是要解链的明显节点。如果失败，说明我们输给了另一个取消或信号，因此不需要进一步操作。
        Node predNext = pred.next;

        // 这里可以使用无条件写入而不是CAS。
        // 在这个原子步骤之后，其他节点可以跳过我们。
        // 在此之前，我们不受其他线程的干扰。
        node.waitStatus = Node.CANCELLED;

        // 如果我们是尾部，移除自己。
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // 如果后继需要信号，尝试设置前驱的next-link
            // 以便它会收到一个。否则唤醒它以传播。
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

            node.next = node; // 帮助垃圾回收
        }
    }

    /**
     * 检查并更新获取失败的节点的状态。如果线程应该阻塞，则返回true。这是所有获取循环中的主要信号控制。需要pred == node.prev。
     *
     * @param pred 持有状态的节点的前驱
     * @param node 节点
     * @return 如果线程应该阻塞，则返回{@code true}
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * 该节点已经设置了状态，请求释放时向它发送信号，因此它可以安全地阻塞。
             */
            return true;
        if (ws > 0) {
            /*
             * 前驱已取消。跳过前驱并指示重试。
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus 必须是 0 或 PROPAGATE。指示我们需要一个信号，但不要立即阻塞。调用者需要重试以确保在阻塞前不能获取。
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }


                /**
     * 便捷方法，用于中断当前线程。
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * 便捷方法，用于暂停并检查是否被中断
     *
     * @return 如果被中断则返回 {@code true}
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /*
     * 各种形式的获取方法，根据独占/共享和控制模式的不同而变化。每个方法大体相同，但略有不同。
     * 由于异常机制（包括确保在 tryAcquire 抛出异常时取消）和其他控制的交互作用，可以进行的重构很少，
     * 至少不会对性能造成太大影响。
     */

    /**
     * 以独占且不可中断模式为已在队列中的线程获取资源。也用于条件等待方法和获取操作。
     *
     * @param node 节点
     * @param arg 获取参数
     * @return 如果等待期间被中断则返回 {@code true}
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // 帮助垃圾回收
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
     * 以独占且可中断模式获取资源。
     * @param arg 获取参数
     */
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // 帮助垃圾回收
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
     * 以独占且带超时模式获取资源。
     *
     * @param arg 获取参数
     * @param nanosTimeout 最大等待时间
     * @return 如果成功获取则返回 {@code true}
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
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
                    p.next = null; // 帮助垃圾回收
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
     * 以共享且不可中断模式获取资源。
     * @param arg 获取参数
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
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
     * 以共享且可中断模式获取资源。
     * @param arg 获取参数
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
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
     * 以共享且带超时模式获取资源。
     *
     * @param arg 获取参数
     * @param nanosTimeout 最大等待时间
     * @return 如果成功获取则返回 {@code true}
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
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
                    int r = tryAcquireShared(arg);
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
     * 尝试以独占模式获取。此方法应查询对象的状态是否允许以独占模式获取，如果允许则获取。
     *
     * <p>此方法总是由执行获取的线程调用。如果此方法报告失败，获取方法可能会将线程排队（如果它尚未排队），直到被其他线程释放时才被唤醒。这可以用于实现 {@link Lock#tryLock()} 方法。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 获取参数。此值始终是传递给获取方法的值，或是在条件等待时保存的值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果成功则返回 {@code true}。成功后，此对象已被获取。
     * @throws IllegalMonitorStateException 如果获取会导致此同步器处于非法状态。为了同步正确工作，此异常必须以一致的方式抛出。
     * @throws UnsupportedOperationException 如果不支持独占模式
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试将状态设置为反映以独占模式释放。
     *
     * <p>此方法总是由执行释放的线程调用。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 释放参数。此值始终是传递给释放方法的值，或是在条件等待时的当前状态值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果此对象现在处于完全释放状态，允许任何等待的线程尝试获取，则返回 {@code true}；否则返回 {@code false}。
     * @throws IllegalMonitorStateException 如果释放会导致此同步器处于非法状态。为了同步正确工作，此异常必须以一致的方式抛出。
     * @throws UnsupportedOperationException 如果不支持独占模式
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试以共享模式获取。此方法应查询对象的状态是否允许以共享模式获取，如果允许则获取。
     *
     * <p>此方法总是由执行获取的线程调用。如果此方法报告失败，获取方法可能会将线程排队（如果它尚未排队），直到被其他线程释放时才被唤醒。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 获取参数。此值始终是传递给获取方法的值，或是在条件等待时保存的值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果失败则返回负值；如果共享模式获取成功但后续的共享模式获取不能成功，则返回零；如果共享模式获取成功且后续的共享模式获取也可能成功，则返回正值，此时后续等待的线程必须检查可用性。（支持三种不同的返回值使此方法可用于有时仅部分独占的上下文中。）成功后，此对象已被获取。
     * @throws IllegalMonitorStateException 如果获取会导致此同步器处于非法状态。为了同步正确工作，此异常必须以一致的方式抛出。
     * @throws UnsupportedOperationException 如果不支持共享模式
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试将状态设置为反映以共享模式释放。
     *
     * <p>此方法总是由执行释放的线程调用。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。
     *
     * @param arg 释放参数。此值始终是传递给释放方法的值，或是在条件等待时的当前状态值。此值未被解释，可以表示任何你想要的内容。
     * @return 如果此共享模式的释放可能允许等待的获取（共享或独占）成功，则返回 {@code true}；否则返回 {@code false}。
     * @throws IllegalMonitorStateException 如果释放会导致此同步器处于非法状态。为了同步正确工作，此异常必须以一致的方式抛出。
     * @throws UnsupportedOperationException 如果不支持共享模式
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果当前（调用）线程独占持有同步，则返回 {@code true}。此方法在每次调用非等待的 {@link ConditionObject} 方法时被调用。（等待方法则调用 {@link #release}。）
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}。此方法仅在 {@link ConditionObject} 方法内部调用，因此如果未使用条件，则无需定义。
     *
     * @return 如果同步独占持有则返回 {@code true}；否则返回 {@code false}。
     * @throws UnsupportedOperationException 如果不支持条件
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }


    /**
     * 以独占模式获取，忽略中断。通过至少调用一次 {@link #tryAcquire} 实现，
     * 成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用 {@link
     * #tryAcquire} 直到成功。此方法可用于实现方法 {@link Lock#lock}。
     *
     * @param arg 获取参数。此值传递给 {@link #tryAcquire}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * 以独占模式获取，如果被中断则中止。通过首先检查中断状态，然后至少调用一次 {@link #tryAcquire} 实现，
     * 成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用 {@link #tryAcquire}
     * 直到成功或线程被中断。此方法可用于实现方法 {@link Lock#lockInterruptibly}。
     *
     * @param arg 获取参数。此值传递给 {@link #tryAcquire}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     * @throws InterruptedException 如果当前线程被中断
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * 尝试以独占模式获取，如果被中断则中止，如果给定超时时间已过则失败。通过首先检查中断状态，然后至少调用一次 {@link
     * #tryAcquire} 实现，成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，调用
     * {@link #tryAcquire} 直到成功或线程被中断或超时时间已过。此方法可用于实现方法 {@link Lock#tryLock(long, TimeUnit)}。
     *
     * @param arg 获取参数。此值传递给 {@link #tryAcquire}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     * @param nanosTimeout 最大等待纳秒数
     * @return 如果获取成功返回 {@code true}；如果超时返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * 以独占模式释放。如果 {@link #tryRelease} 返回 true，则通过解除一个或多个线程的阻塞实现。
     * 此方法可用于实现方法 {@link Lock#unlock}。
     *
     * @param arg 释放参数。此值传递给 {@link #tryRelease}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     * @return 从 {@link #tryRelease} 返回的值
     */
    public final boolean release(int arg) {
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
     * @param arg 获取参数。此值传递给 {@link #tryAcquireShared}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * 以共享模式获取，如果被中断则中止。通过首先检查中断状态，然后至少调用一次
     * {@link #tryAcquireShared} 实现，成功时返回。否则，线程将被排队，可能反复阻塞和解除阻塞，
     * 调用 {@link #tryAcquireShared} 直到成功或线程被中断。
     * @param arg 获取参数。
     * 此值传递给 {@link #tryAcquireShared}，但除此之外未被解释，可以表示任何
     * 你想要的内容。
     * @throws InterruptedException 如果当前线程被中断
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * 尝试以共享模式获取，如果被中断则中止，如果给定超时时间已过则失败。通过首先检查中断状态，然后至少调用一次 {@link
     * #tryAcquireShared} 实现，成功时返回。否则，
     * 线程将被排队，可能反复阻塞和解除阻塞，
     * 调用 {@link #tryAcquireShared} 直到成功或线程被中断或超时时间已过。
     *
     * @param arg 获取参数。此值传递给 {@link #tryAcquireShared}，但除此之外未被解释，
     *        可以表示任何你想要的内容。
     * @param nanosTimeout 最大等待纳秒数
     * @return 如果获取成功返回 {@code true}；如果超时返回 {@code false}
     * @throws InterruptedException 如果当前线程被中断
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

                /**
     * 以共享模式释放。如果 {@link #tryReleaseShared} 返回 true，则通过解除一个或多个线程的阻塞来实现。
     *
     * @param arg 释放参数。此值传递给 {@link #tryReleaseShared}，但除此之外未被解释，可以表示任何你想要的内容。
     * @return 从 {@link #tryReleaseShared} 返回的值
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // 队列检查方法

    /**
     * 查询是否有线程正在等待获取。请注意，由于中断和超时导致的取消可能随时发生，因此 {@code true} 的返回值并不保证任何其他线程将最终获取。
     *
     * <p>在此实现中，此操作在常数时间内返回。
     *
     * @return 如果可能有其他线程正在等待获取，则返回 {@code true}
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 查询是否有线程曾经争夺过此同步器的获取；也就是说，如果获取方法曾经阻塞过。
     *
     * <p>在此实现中，此操作在常数时间内返回。
     *
     * @return 如果曾经有过争夺，则返回 {@code true}
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 返回队列中的第一个（等待时间最长的）线程，如果没有线程当前在队列中，则返回 {@code null}。
     *
     * <p>在此实现中，此操作通常在常数时间内返回，但如果其他线程正在并发修改队列，则可能需要遍历。
     *
     * @return 队列中的第一个（等待时间最长的）线程，如果没有线程当前在队列中，则返回 {@code null}
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
         * 第一个节点通常是 head.next。尝试获取其线程字段，确保一致的读取：如果线程字段被置为 null 或 s.prev 不再是 head，则
         * 一些其他线程在我们的读取之间并发地执行了 setHead。我们尝试两次，如果失败则进行遍历。
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head 的 next 字段可能尚未设置，或者在 setHead 之后被取消设置。因此，我们必须检查 tail 是否实际上是第一个节点。
         * 如果不是，我们继续从 tail 向 head 安全地遍历以找到第一个，确保终止。
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
     * 如果给定的线程当前在队列中，则返回 true。
     *
     * <p>此实现遍历队列以确定给定线程的存在。
     *
     * @param thread 线程
     * @return 如果给定的线程在队列中，则返回 {@code true}
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
     * 如果存在，返回第一个排队的线程是否以独占模式等待。如果此方法返回 {@code true}，并且当前线程正在尝试以共享模式获取（即，此方法是从 {@link
     * #tryAcquireShared} 调用的），则可以保证当前线程不是第一个排队的线程。仅在 ReentrantReadWriteLock 中用作启发式方法。
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * 查询是否有线程等待的时间比当前线程更长。
     *
     * <p>此方法的调用等效于（但可能更高效）：
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>请注意，由于中断和超时导致的取消可能随时发生，因此 {@code true} 的返回值并不保证其他线程将在当前线程之前获取。同样，如果此方法返回 {@code false}，
     * 由于队列为空，其他线程可能在方法调用后赢得入队竞争。
     *
     * <p>此方法旨在由公平同步器使用，以避免 <a href="AbstractQueuedSynchronizer#barging">插队</a>。这样的同步器的 {@link #tryAcquire} 方法应该在
     * 此方法返回 {@code true} 时返回 {@code false}（除非这是重入获取），其 {@link #tryAcquireShared} 方法应该在返回负值时返回（除非这是重入获取）。
     * 例如，一个公平、重入、独占模式同步器的 {@code tryAcquire} 方法可能如下所示：
     *
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // 重入获取；增加持有计数
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false;
     *   } else {
     *     // 尝试正常获取
     *   }
     * }}</pre>
     *
     * @return 如果有排在当前线程前面的排队线程，则返回 {@code true}，如果当前线程在队列的头部或队列为空，则返回 {@code false}
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // 该方法的正确性取决于 head 在 tail 之前初始化，以及如果当前线程是队列中的第一个，head.next 必须准确。
        Node t = tail; // 以相反的初始化顺序读取字段
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    // 仪器和监控方法

    /**
     * 返回估计的等待获取锁的线程数量。该值只是一个估计值，因为在该方法遍历内部数据结构时，线程数量可能会动态变化。此方法旨在用于监控系统状态，而不是用于同步控制。
     *
     * @return 估计的等待获取锁的线程数量
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
     * 返回一个包含可能正在等待获取锁的线程的集合。由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。返回集合中的元素没有特定的顺序。此方法旨在促进构建提供更广泛的监控设施的子类。
     *
     * @return 线程集合
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
     * 返回一个包含可能正在以独占模式等待获取锁的线程的集合。这与 {@link #getQueuedThreads} 具有相同的属性，只是它只返回那些因独占获取而等待的线程。
     *
     * @return 线程集合
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
     * 返回一个包含可能正在以共享模式等待获取锁的线程的集合。这与 {@link #getQueuedThreads} 具有相同的属性，只是它只返回那些因共享获取而等待的线程。
     *
     * @return 线程集合
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
     * 返回一个标识此同步器及其状态的字符串。状态（在方括号中）包括字符串 {@code "State ="} 后跟 {@link #getState} 的当前值，以及根据队列是否为空而定的 {@code "nonempty"} 或 {@code "empty"}。
     *
     * @return 一个标识此同步器及其状态的字符串
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // 条件支持的内部方法

    /**
     * 如果节点（总是最初放置在条件队列上的节点）现在正在同步队列上重新获取锁，则返回 true。
     * @param node 节点
     * @return 如果正在重新获取，则返回 true
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // 如果有后继节点，它必须在队列上
            return true;
        /*
         * node.prev 可以是非空的，但尚未在队列上，因为将它放置在队列上的 CAS 操作可能失败。因此，我们必须从尾部遍历以确保它实际上已经到达。在调用此方法时，它通常会非常接近尾部，除非 CAS 失败（这不太可能），它将在那里，所以我们几乎从不遍历很多。
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
     * 将节点从条件队列转移到同步队列。如果成功，则返回 true。
     * @param node 节点
     * @return 如果成功转移（否则节点在信号之前被取消），则返回 true
     */
    final boolean transferForSignal(Node node) {
        /*
         * 如果不能更改 waitStatus，节点已被取消。
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * 拼接到队列上，并尝试将前驱节点的 waitStatus 设置为表示线程（可能）正在等待。如果被取消或尝试设置 waitStatus 失败，则唤醒以重新同步（在这种情况下，waitStatus 可能暂时且无害地错误）。
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    /**
     * 在取消等待后，如果必要，将节点转移到同步队列。如果线程在被信号之前被取消，则返回 true。
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
         * 如果我们输给了一个 signal()，那么我们必须等到它完成其 enq()。在不完整的转移期间取消是罕见且短暂的，所以只需自旋。
         */
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }


                /**
     * 调用带有当前状态值的 release 方法；返回保存的状态。
     * 如果失败，则取消节点并抛出异常。
     * @param node 用于此等待的条件节点
     * @return 之前的同步状态
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
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
     * @return 如果拥有则返回 {@code true}
     * @throws NullPointerException 如果条件为 null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * 查询是否有线程正在等待与此同步器关联的给定条件。请注意，由于超时和中断可能随时发生，因此 {@code true} 的返回值并不能保证未来的 {@code signal} 会唤醒任何线程。此方法主要用于系统状态的监控。
     *
     * @param condition 条件
     * @return 如果有等待的线程则返回 {@code true}
     * @throws IllegalMonitorStateException 如果未持有独占同步
     * @throws IllegalArgumentException 如果给定的条件不与此同步器关联
     * @throws NullPointerException 如果条件为 null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * 返回一个估计值，表示正在等待与此同步器关联的给定条件的线程数。请注意，由于超时和中断可能随时发生，因此估计值仅作为实际等待线程数的上限。此方法主要用于系统状态的监控，而不是同步控制。
     *
     * @param condition 条件
     * @return 等待线程的估计数
     * @throws IllegalMonitorStateException 如果未持有独占同步
     * @throws IllegalArgumentException 如果给定的条件不与此同步器关联
     * @throws NullPointerException 如果条件为 null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * 返回一个集合，包含可能正在等待与此同步器关联的给定条件的线程。由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。返回集合中的元素没有特定的顺序。
     *
     * @param condition 条件
     * @return 线程集合
     * @throws IllegalMonitorStateException 如果未持有独占同步
     * @throws IllegalArgumentException 如果给定的条件不与此同步器关联
     * @throws NullPointerException 如果条件为 null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * 作为 {@link AbstractQueuedSynchronizer} 基础的 {@link Lock} 实现的条件实现。
     *
     * <p>此类的方法文档描述了机制，而不是从 Lock 和 Condition 用户的角度描述行为规范。导出此类的版本通常需要附带描述条件语义的文档，这些语义依赖于关联的 {@code AbstractQueuedSynchronizer}。
     *
     * <p>此类是可序列化的，但所有字段都是瞬态的，因此反序列化的条件没有等待者。
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
         * 向等待队列中添加一个新的等待者。
         * @return 新的等待节点
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // 如果 lastWaiter 被取消，则清理。
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
         * 从条件队列中取消已取消的等待节点的链接。
         * 仅在持有锁时调用。当在条件等待期间发生取消，或在插入新等待者时发现 lastWaiter 已被取消时调用此方法。
         * 该方法用于避免在没有信号的情况下保留垃圾。因此，即使它可能需要完全遍历，也仅在没有信号的情况下发生超时或取消时才起作用。
         * 它遍历所有节点而不是在特定目标处停止，以在取消风暴期间无需多次重新遍历即可取消对垃圾节点的所有链接。
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
         * 将最长等待的线程（如果存在）从该条件的等待队列移动到拥有锁的等待队列。
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
         * <li> 使用保存的状态调用 {@link #release}，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒。
         * <li> 通过调用 {@link #acquire} 的专用版本并使用保存的状态作为参数重新获取锁。
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
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
         * 对于可中断的等待，我们需要跟踪是在阻塞在条件上时中断还是在阻塞等待重新获取锁时中断。
         */

        /** 退出等待时重新中断的模式 */
        private static final int REINTERRUPT =  1;
        /** 退出等待时抛出 InterruptedException 的模式 */
        private static final int THROW_IE    = -1;

        /**
         * 检查是否中断，如果在被信号唤醒前中断则返回 THROW_IE，如果在被信号唤醒后中断则返回 REINTERRUPT，如果没有中断则返回 0。
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * 根据模式抛出 InterruptedException，重新中断当前线程，或什么都不做。
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
         * <li> 如果当前线程被中断，则抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 使用保存的状态调用 {@link #release}，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号唤醒或被中断。
         * <li> 通过调用 {@link #acquire} 的专用版本并使用保存的状态作为参数重新获取锁。
         * <li> 如果在第 4 步阻塞时被中断，则抛出 InterruptedException。
         * </ol>
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // 如果已取消，则清理
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }


                    /**
         * 实现带时间限制的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用带有保存状态作为参数的 {@link #release}，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号、中断或超时。
         * <li> 通过调用带有保存状态作为参数的 {@link #acquire} 的专门版本重新获取锁。
         * <li> 如果在第 4 步被阻塞时被中断，抛出 InterruptedException。
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
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
         * 实现绝对带时间限制的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用带有保存状态作为参数的 {@link #release}，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号、中断或超时。
         * <li> 通过调用带有保存状态作为参数的 {@link #acquire} 的专门版本重新获取锁。
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
            int savedState = fullyRelease(node);
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
         * 实现带时间限制的条件等待。
         * <ol>
         * <li> 如果当前线程被中断，抛出 InterruptedException。
         * <li> 保存由 {@link #getState} 返回的锁状态。
         * <li> 调用带有保存状态作为参数的 {@link #release}，
         *      如果失败则抛出 IllegalMonitorStateException。
         * <li> 阻塞直到被信号、中断或超时。
         * <li> 通过调用带有保存状态作为参数的 {@link #acquire} 的专门版本重新获取锁。
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
            int savedState = fullyRelease(node);
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
         * @return 如果拥有则为 {@code true}
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }


                    /**
         * 查询是否有任何线程正在等待此条件。
         * 实现了 {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}。
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
         * 返回一个估计的正在等待此条件的线程数量。
         * 实现了 {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}。
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
         * 返回一个包含可能正在等待此条件的线程的集合。
         * 实现了 {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}。
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
     * 设置以支持 compareAndSet。我们需要在此处原生实现这一点：为了允许未来的增强，我们不能显式地继承 AtomicInteger，尽管这样做会更高效且有用。因此，作为较小的恶，我们使用 hotspot 内在 API 原生实现。同时，我们也对其他 CAS 字段进行同样的处理（否则可以使用原子字段更新器）。
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
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS head 字段。仅由 enq 使用。
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail 字段。仅由 enq 使用。
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS 节点的 waitStatus 字段。
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS 节点的 next 字段。
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
