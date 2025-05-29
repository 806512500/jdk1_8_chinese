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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下条款发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.LockSupport;

/**
 * 一种基于能力的锁，具有三种模式来控制读/写访问。StampedLock 的状态由版本和模式组成。
 * 锁获取方法返回一个表示并控制相对于锁状态的访问的戳；这些方法的“try”版本可能会返回特殊值零，表示未能获取访问权限。
 * 锁释放和转换方法需要戳作为参数，并且如果它们与锁的状态不匹配，则会失败。三种模式是：
 *
 * <ul>
 *
 *  <li><b>写入。</b> 方法 {@link #writeLock} 可能会阻塞以等待独占访问，返回一个可以用于方法 {@link #unlockWrite} 释放锁的戳。
 *   还提供了未定时和定时版本的 {@code tryWriteLock}。当锁以写模式持有时，不能获取读锁，并且所有乐观读验证都会失败。 </li>
 *
 *  <li><b>读取。</b> 方法 {@link #readLock} 可能会阻塞以等待非独占访问，返回一个可以用于方法 {@link #unlockRead} 释放锁的戳。
 *   还提供了未定时和定时版本的 {@code tryReadLock}。 </li>
 *
 *  <li><b>乐观读取。</b> 方法 {@link #tryOptimisticRead} 仅在锁当前未以写模式持有时返回非零戳。
 *   方法 {@link #validate} 如果在获取给定戳后锁未被以写模式获取，则返回 true。这种模式可以被视为一种极其弱的读锁版本，
 *   可以随时被写入者破坏。乐观模式用于短的只读代码段时，通常可以减少争用并提高吞吐量。然而，其使用本质上是脆弱的。
 *   乐观读取部分应仅读取字段并将它们存储在本地变量中，以供验证后使用。在乐观模式下读取的字段可能非常不一致，
 *   因此使用仅适用于你足够熟悉数据表示以检查一致性和/或重复调用方法 {@code validate()} 的情况。例如，通常需要这样的步骤，
 *   当首先读取一个对象或数组引用，然后访问其字段、元素或方法时。 </li>
 *
 * </ul>
 *
 * <p>此类还支持在三种模式之间有条件地提供转换的方法。例如，方法 {@link #tryConvertToWriteLock} 尝试“升级”模式，
 * 如果 (1) 已处于写入模式 (2) 处于读取模式且没有其他读取者 (3) 处于乐观模式且锁可用，则返回有效的写戳。
 * 这些方法的形式旨在帮助减少基于重试的设计中出现的一些代码膨胀。
 *
 * <p>StampedLocks 旨在用作开发线程安全组件的内部工具。它们的使用依赖于对它们保护的数据、对象和方法的内部属性的了解。
 * 它们不是可重入的，因此锁定体不应调用可能尝试重新获取锁的其他未知方法（尽管你可以将戳传递给可以使用或转换它的其他方法）。
 * 读锁模式的使用依赖于相关代码部分是无副作用的。未验证的乐观读取部分不能调用不能容忍潜在不一致的方法。
 * 戳使用有限的表示形式，并且不是加密安全的（即，有效的戳可能是可猜测的）。在连续运行一年后（不少于一年），戳值可能会回收。
 * 保持超过此期间而未使用或验证的戳可能无法正确验证。StampedLocks 是可序列化的，但始终反序列化为初始未锁定状态，
 * 因此它们对于远程锁定没有用处。
 *
 * <p>StampedLock 的调度策略不一致地优先考虑读者或写者。所有“try”方法都是尽力而为，并不一定符合任何调度或公平策略。
 * 从任何“try”方法获取或转换锁返回零并不携带有关锁状态的任何信息；后续调用可能会成功。
 *
 * <p>由于它支持跨多种锁模式的协调使用，此类不直接实现 {@link Lock} 或 {@link ReadWriteLock} 接口。
 * 但是，可以在仅需要相关功能集的应用程序中将 StampedLock 视为 {@link #asReadLock()}、{@link #asWriteLock()} 或 {@link #asReadWriteLock()}。
 *
 * <p><b>示例用法。</b> 以下示例说明了一些使用惯例，这些惯例在一个维护简单二维点的类中。示例代码说明了一些 try/catch 约定，
 * 即使它们在这里严格来说不是必需的，因为它们的主体中不会发生异常。<br>
 *
 *  <pre>{@code
 * class Point {
 *   private double x, y;
 *   private final StampedLock sl = new StampedLock();
 *
 *   void move(double deltaX, double deltaY) { // 一个独占锁定的方法
 *     long stamp = sl.writeLock();
 *     try {
 *       x += deltaX;
 *       y += deltaY;
 *     } finally {
 *       sl.unlockWrite(stamp);
 *     }
 *   }
 *
 *   double distanceFromOrigin() { // 一个只读方法
 *     long stamp = sl.tryOptimisticRead();
 *     double currentX = x, currentY = y;
 *     if (!sl.validate(stamp)) {
 *        stamp = sl.readLock();
 *        try {
 *          currentX = x;
 *          currentY = y;
 *        } finally {
 *           sl.unlockRead(stamp);
 *        }
 *     }
 *     return Math.sqrt(currentX * currentX + currentY * currentY);
 *   }
 *
 *   void moveIfAtOrigin(double newX, double newY) { // 升级
 *     // 也可以从乐观模式开始，而不是读模式
 *     long stamp = sl.readLock();
 *     try {
 *       while (x == 0.0 && y == 0.0) {
 *         long ws = sl.tryConvertToWriteLock(stamp);
 *         if (ws != 0L) {
 *           stamp = ws;
 *           x = newX;
 *           y = newY;
 *           break;
 *         }
 *         else {
 *           sl.unlockRead(stamp);
 *           stamp = sl.writeLock();
 *         }
 *       }
 *     } finally {
 *       sl.unlock(stamp);
 *     }
 *   }
 * }}</pre>
 *
 * @since 1.8
 * @author Doug Lea
 */
public class StampedLock implements java.io.Serializable {
    /*
     * 算法说明：
     *
     * 该设计采用了序列锁（如在 linux 内核中使用；参见 Lameter 的
     * http://www.lameter.com/gelato2005.pdf
     * 以及其它地方；参见
     * Boehm 的 http://www.hpl.hp.com/techreports/2012/HPL-2012-68.html）
     * 和有序读写锁（参见 Shirako 等人的
     * http://dl.acm.org/citation.cfm?id=2312015）
     *
     * 概念上，锁的主要状态包括一个序列号，当写锁时为奇数，否则为偶数。
     * 但是，当读锁时，此序列号会被读计数偏移。读计数在验证“乐观”seqlock-读取者风格的戳时被忽略。
     * 由于我们必须使用有限数量的位（目前为 7 位）来表示读取者，因此当读取者的数量超过计数字段时，会使用一个补充的读取者溢出字。
     * 我们通过将最大读取者计数值（RBITS）视为一个自旋锁来保护溢出更新。
     *
     * 等待者使用 AbstractQueuedSynchronizer 中使用的 CLH 锁的修改形式（参见其内部文档以获取更详细的说明），
     * 其中每个节点被标记（字段 mode）为读取者或写入者。一组等待的读取者在共同节点（字段 cowait）下分组，
     * 因此在大多数 CLH 机制中作为单个节点行动。由于队列结构，等待节点实际上不需要携带序列号；
     * 我们知道每个节点都大于其前驱。这简化了主要的 FIFO 调度策略，该策略结合了 Phase-Fair 锁的元素（参见 Brandenburg & Anderson，
     * 特别是 http://www.cs.unc.edu/~bbb/diss/）。特别是，我们使用 phase-fair 反插队规则：如果一个到达的读取者在读锁被持有时到达，
     * 但队列中有一个写入者，那么这个到达的读取者将被排队。（这条规则负责方法 acquireRead 的一些复杂性，但没有它，锁将变得非常不公平。）
     * 方法 release 本身不会（有时也不能）唤醒 cowaiters。这是由主线程完成的，但由 acquireRead 和 acquireWrite 方法中的其他线程帮助完成，
     * 这些线程没有更好的事情可做。
     *
     * 这些规则适用于实际排队的线程。所有 tryLock 形式都会不顾偏好规则尝试获取锁，因此可能会“插队”。在获取方法中使用随机自旋来减少（越来越昂贵的）
     * 上下文切换，同时避免许多线程之间的持续内存抖动。我们限制自旋到队列头部。线程自旋等待最多 SPINS 次（每次迭代以 50% 的概率减少自旋计数），
     * 然后阻塞。如果在唤醒后未能获取锁，并且仍然是（或成为）第一个等待线程（这表明其他线程插队并获取了锁），则增加自旋（最多 MAX_HEAD_SPINS），
     * 以减少持续输给插队线程的可能性。
     *
     * 几乎所有这些机制都在 acquireWrite 和 acquireRead 方法中实现，这些方法通常由于动作和重试依赖于一致的本地缓存读取而显得冗长。
     *
     * 如 Boehm 的论文（上文提到）所述，序列验证（主要是方法 validate()）需要比对“状态”的普通易失性读取更严格的排序规则。
     * 为了强制在验证之前和验证本身的读取排序，在这些情况下未被强制，我们使用 Unsafe.loadFence。
     *
     * 内存布局将锁状态和队列指针保持在一起（通常在同一缓存行上）。这通常适用于主要读取的负载。在大多数其他情况下，
     * 适应性自旋 CLH 锁的自然趋势减少内存争用，减少了进一步分散争用位置的动力，但可能会在未来进行改进。
     */


                private static final long serialVersionUID = -6001602636862214147L;

    /** 处理器数量，用于控制自旋 */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /** 在入队获取前的最大重试次数 */
    private static final int SPINS = (NCPU > 1) ? 1 << 6 : 0;

    /** 在获取时头部阻塞前的最大重试次数 */
    private static final int HEAD_SPINS = (NCPU > 1) ? 1 << 10 : 0;

    /** 在重新阻塞前的最大重试次数 */
    private static final int MAX_HEAD_SPINS = (NCPU > 1) ? 1 << 16 : 0;

    /** 在等待溢出自旋锁时的yield周期 */
    private static final int OVERFLOW_YIELD_RATE = 7; // 必须是2的幂减1

    /** 在溢出前用于读取计数的位数 */
    private static final int LG_READERS = 7;

    // 锁状态和戳操作的值
    private static final long RUNIT = 1L;
    private static final long WBIT  = 1L << LG_READERS;
    private static final long RBITS = WBIT - 1L;
    private static final long RFULL = RBITS - 1L;
    private static final long ABITS = RBITS | WBIT;
    private static final long SBITS = ~RBITS; // 注意与ABITS重叠

    // 锁状态的初始值；避免失败值零
    private static final long ORIGIN = WBIT << 1;

    // 从取消的获取方法返回的特殊值，以便调用者可以抛出IE
    private static final long INTERRUPTED = 1L;

    // 节点状态的值；顺序很重要
    private static final int WAITING   = -1;
    private static final int CANCELLED =  1;

    // 节点模式（int而不是boolean，以允许算术运算）
    private static final int RMODE = 0;
    private static final int WMODE = 1;

    /** 等待节点 */
    static final class WNode {
        volatile WNode prev;
        volatile WNode next;
        volatile WNode cowait;    // 链接的读取者列表
        volatile Thread thread;   // 可能被挂起时非空
        volatile int status;      // 0, WAITING, 或 CANCELLED
        final int mode;           // RMODE 或 WMODE
        WNode(int m, WNode p) { mode = m; prev = p; }
    }

    /** CLH队列的头部 */
    private transient volatile WNode whead;
    /** CLH队列的尾部（最后一个） */
    private transient volatile WNode wtail;

    // 视图
    transient ReadLockView readLockView;
    transient WriteLockView writeLockView;
    transient ReadWriteLockView readWriteLockView;

    /** 锁序列/状态 */
    private transient volatile long state;
    /** 当状态读取计数饱和时的额外读取计数 */
    private transient int readerOverflow;

    /**
     * 创建一个新的锁，初始状态为未锁定。
     */
    public StampedLock() {
        state = ORIGIN;
    }

    /**
     * 排他地获取锁，必要时阻塞，直到可用。
     *
     * @return 可用于解锁或转换模式的戳
     */
    public long writeLock() {
        long s, next;  // 仅在完全解锁的情况下绕过acquireWrite
        return ((((s = state) & ABITS) == 0L &&
                 U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
                next : acquireWrite(false, 0L));
    }

    /**
     * 如果锁立即可用，则排他地获取锁。
     *
     * @return 可用于解锁或转换模式的戳，
     * 或者如果锁不可用则为零
     */
    public long tryWriteLock() {
        long s, next;
        return ((((s = state) & ABITS) == 0L &&
                 U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
                next : 0L);
    }

    /**
     * 如果锁在给定时间内可用，并且当前线程未被中断，则排他地获取锁。
     * 超时和中断下的行为与方法 {@link Lock#tryLock(long,TimeUnit)} 指定的行为匹配。
     *
     * @param time 等待锁的最大时间
     * @param unit {@code time} 参数的时间单位
     * @return 可用于解锁或转换模式的戳，
     * 或者如果锁不可用则为零
     * @throws InterruptedException 如果当前线程在获取锁之前被中断
     */
    public long tryWriteLock(long time, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long next, deadline;
            if ((next = tryWriteLock()) != 0L)
                return next;
            if (nanos <= 0L)
                return 0L;
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            if ((next = acquireWrite(true, deadline)) != INTERRUPTED)
                return next;
        }
        throw new InterruptedException();
    }

    /**
     * 排他地获取锁，必要时阻塞，直到可用或当前线程被中断。
     * 中断下的行为与方法 {@link Lock#lockInterruptibly()} 指定的行为匹配。
     *
     * @return 可用于解锁或转换模式的戳
     * @throws InterruptedException 如果当前线程在获取锁之前被中断
     */
    public long writeLockInterruptibly() throws InterruptedException {
        long next;
        if (!Thread.interrupted() &&
            (next = acquireWrite(true, 0L)) != INTERRUPTED)
            return next;
        throw new InterruptedException();
    }

    /**
     * 非排他地获取锁，必要时阻塞，直到可用。
     *
     * @return 可用于解锁或转换模式的戳
     */
    public long readLock() {
        long s = state, next;  // 绕过acquireRead在常见的无竞争情况下
        return ((whead == wtail && (s & ABITS) < RFULL &&
                 U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ?
                next : acquireRead(false, 0L));
    }

    /**
     * 如果锁立即可用，则非排他地获取锁。
     *
     * @return 可用于解锁或转换模式的戳，
     * 或者如果锁不可用则为零
     */
    public long tryReadLock() {
        for (;;) {
            long s, m, next;
            if ((m = (s = state) & ABITS) == WBIT)
                return 0L;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                    return next;
            }
            else if ((next = tryIncReaderOverflow(s)) != 0L)
                return next;
        }
    }

                /**
     * 如果锁在给定时间内可用且当前线程未被中断，则非独占地获取锁。
     * 在超时和中断情况下的行为与方法 {@link Lock#tryLock(long,TimeUnit)} 的指定行为相匹配。
     *
     * @param time 等待锁的最大时间
     * @param unit {@code time} 参数的时间单位
     * @return 可用于解锁或转换模式的戳，如果锁不可用则返回零
     * @throws InterruptedException 如果当前线程在获取锁之前被中断
     */
    public long tryReadLock(long time, TimeUnit unit)
        throws InterruptedException {
        long s, m, next, deadline;
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            if ((m = (s = state) & ABITS) != WBIT) {
                if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            if (nanos <= 0L)
                return 0L;
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            if ((next = acquireRead(true, deadline)) != INTERRUPTED)
                return next;
        }
        throw new InterruptedException();
    }

    /**
     * 非独占地获取锁，必要时阻塞，直到锁可用或当前线程被中断。
     * 在中断情况下的行为与方法 {@link Lock#lockInterruptibly()} 的指定行为相匹配。
     *
     * @return 可用于解锁或转换模式的戳
     * @throws InterruptedException 如果当前线程在获取锁之前被中断
     */
    public long readLockInterruptibly() throws InterruptedException {
        long next;
        if (!Thread.interrupted() &&
            (next = acquireRead(true, 0L)) != INTERRUPTED)
            return next;
        throw new InterruptedException();
    }

    /**
     * 返回一个稍后可以验证的戳，如果独占地锁定则返回零。
     *
     * @return 一个戳，如果独占地锁定则返回零
     */
    public long tryOptimisticRead() {
        long s;
        return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
    }

    /**
     * 如果自给定戳发行以来锁未被独占地获取，则返回 true。如果戳为零，则始终返回 false。
     * 如果戳表示当前持有的锁，则始终返回 true。使用未从 {@link #tryOptimisticRead} 或此锁的锁定方法获得的值调用此方法没有定义的效果或结果。
     *
     * @param stamp 一个戳
     * @return 如果自给定戳发行以来锁未被独占地获取，则返回 {@code true}；否则返回 false
     */
    public boolean validate(long stamp) {
        U.loadFence();
        return (stamp & SBITS) == (state & SBITS);
    }

    /**
     * 如果锁状态与给定的戳匹配，则释放独占锁。
     *
     * @param stamp 由写锁操作返回的戳
     * @throws IllegalMonitorStateException 如果戳不匹配此锁的当前状态
     */
    public void unlockWrite(long stamp) {
        WNode h;
        if (state != stamp || (stamp & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        state = (stamp += WBIT) == 0L ? ORIGIN : stamp;
        if ((h = whead) != null && h.status != 0)
            release(h);
    }

    /**
     * 如果锁状态与给定的戳匹配，则释放非独占锁。
     *
     * @param stamp 由读锁操作返回的戳
     * @throws IllegalMonitorStateException 如果戳不匹配此锁的当前状态
     */
    public void unlockRead(long stamp) {
        long s, m; WNode h;
        for (;;) {
            if (((s = state) & SBITS) != (stamp & SBITS) ||
                (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
                throw new IllegalMonitorStateException();
            if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    break;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    /**
     * 如果锁状态与给定的戳匹配，则释放锁的相应模式。
     *
     * @param stamp 由锁定操作返回的戳
     * @throws IllegalMonitorStateException 如果戳不匹配此锁的当前状态
     */
    public void unlock(long stamp) {
        long a = stamp & ABITS, m, s; WNode h;
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            if ((m = s & ABITS) == 0L)
                break;
            else if (m == WBIT) {
                if (a != m)
                    break;
                state = (s += WBIT) == 0L ? ORIGIN : s;
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return;
            }
            else if (a == 0L || a >= WBIT)
                break;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                return;
        }
        throw new IllegalMonitorStateException();
    }

    /**
     * 如果锁状态与给定的戳匹配，则执行以下操作之一。如果戳表示持有写锁，则返回它。
     * 或者，如果是一个读锁，如果写锁可用，则释放读锁并返回一个写戳。
     * 或者，如果是乐观读，仅在立即可用时返回一个写戳。此方法在所有其他情况下返回零。
     *
     * @param stamp 一个戳
     * @return 一个有效的写戳，或在失败时返回零
     */
    public long tryConvertToWriteLock(long stamp) {
        long a = stamp & ABITS, m, s, next;
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                if (U.compareAndSwapLong(this, STATE, s, next = s + WBIT))
                    return next;
            }
            else if (m == WBIT) {
                if (a != m)
                    break;
                return stamp;
            }
            else if (m == RUNIT && a != 0L) {
                if (U.compareAndSwapLong(this, STATE, s,
                                         next = s - RUNIT + WBIT))
                    return next;
            }
            else
                break;
        }
        return 0L;
    }

                /**
     * 如果锁状态与给定的戳记匹配，则执行以下操作之一。如果戳记表示持有写锁，则释放它并获取读锁。
     * 或者，如果是一个读锁，则返回它。或者，如果是乐观读取，获取读锁并仅在立即可用时返回读戳记。
     * 如果其他所有情况，此方法返回零。
     *
     * @param stamp 一个戳记
     * @return 有效的读戳记，或在失败时返回零
     */
    public long tryConvertToReadLock(long stamp) {
        long a = stamp & ABITS, m, s, next; WNode h;
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                else if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            else if (m == WBIT) {
                if (a != m)
                    break;
                state = next = s + (WBIT + RUNIT);
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return next;
            }
            else if (a != 0L && a < WBIT)
                return stamp;
            else
                break;
        }
        return 0L;
    }

    /**
     * 如果锁状态与给定的戳记匹配，则，如果戳记表示持有锁，释放它并返回一个观察戳记。
     * 或者，如果是乐观读取，在验证后返回它。此方法在所有其他情况下返回零，因此可用作“tryUnlock”的一种形式。
     *
     * @param stamp 一个戳记
     * @return 有效的乐观读戳记，或在失败时返回零
     */
    public long tryConvertToOptimisticRead(long stamp) {
        long a = stamp & ABITS, m, s, next; WNode h;
        U.loadFence();
        for (;;) {
            if (((s = state) & SBITS) != (stamp & SBITS))
                break;
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                return s;
            }
            else if (m == WBIT) {
                if (a != m)
                    break;
                state = next = (s += WBIT) == 0L ? ORIGIN : s;
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return next;
            }
            else if (a == 0L || a >= WBIT)
                break;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return next & SBITS;
                }
            }
            else if ((next = tryDecReaderOverflow(s)) != 0L)
                return next & SBITS;
        }
        return 0L;
    }

    /**
     * 如果持有写锁，则释放它，而无需戳记值。此方法可用于错误后的恢复。
     *
     * @return 如果锁被持有，则返回{@code true}，否则返回false
     */
    public boolean tryUnlockWrite() {
        long s; WNode h;
        if (((s = state) & WBIT) != 0L) {
            state = (s += WBIT) == 0L ? ORIGIN : s;
            if ((h = whead) != null && h.status != 0)
                release(h);
            return true;
        }
        return false;
    }

    /**
     * 如果持有读锁，则释放一次读锁的持有，而无需戳记值。此方法可用于错误后的恢复。
     *
     * @return 如果读锁被持有，则返回{@code true}，否则返回false
     */
    public boolean tryUnlockRead() {
        long s, m; WNode h;
        while ((m = (s = state) & ABITS) != 0L && m < WBIT) {
            if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return true;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                return true;
        }
        return false;
    }

    // 状态监控方法

    /**
     * 返回给定状态s的组合持有状态和溢出读计数。
     */
    private int getReadLockCount(long s) {
        long readers;
        if ((readers = s & RBITS) >= RFULL)
            readers = RFULL + readerOverflow;
        return (int) readers;
    }

    /**
     * 如果锁当前被独占持有，则返回{@code true}。
     *
     * @return 如果锁当前被独占持有，则返回{@code true}
     */
    public boolean isWriteLocked() {
        return (state & WBIT) != 0L;
    }

    /**
     * 如果锁当前被非独占持有，则返回{@code true}。
     *
     * @return 如果锁当前被非独占持有，则返回{@code true}
     */
    public boolean isReadLocked() {
        return (state & RBITS) != 0L;
    }

    /**
     * 查询此锁持有的读锁数量。此方法设计用于监控系统状态，而不是用于同步控制。
     * @return 持有的读锁数量
     */
    public int getReadLockCount() {
        return getReadLockCount(state);
    }

    /**
     * 返回一个标识此锁及其锁状态的字符串。状态，用括号括起来，包括字符串{@code "Unlocked"}或字符串{@code "Write-locked"}或字符串
     * {@code "Read-locks:"}后跟当前持有的读锁数量。
     *
     * @return 一个标识此锁及其锁状态的字符串
     */
    public String toString() {
        long s = state;
        return super.toString() +
            ((s & ABITS) == 0L ? "[Unlocked]" :
             (s & WBIT) != 0L ? "[Write-locked]" :
             "[Read-locks:" + getReadLockCount(s) + "]");
    }

    // 视图

    /**
     * 返回此StampedLock的一个普通{@link Lock}视图，其中{@link Lock#lock}方法映射到{@link #readLock}，
     * 其他方法也类似。返回的Lock不支持{@link Condition}；方法{@link Lock#newCondition()}抛出{@code
     * UnsupportedOperationException}。
     *
     * @return 锁
     */
    public Lock asReadLock() {
        ReadLockView v;
        return ((v = readLockView) != null ? v :
                (readLockView = new ReadLockView()));
    }

                /**
     * 返回此 StampedLock 的一个普通 {@link Lock} 视图，其中
     * {@link Lock#lock} 方法映射到 {@link #writeLock}，
     * 其他方法也类似映射。返回的 Lock 不支持 {@link Condition}；
     * 方法 {@link Lock#newCondition()} 抛出 {@code
     * UnsupportedOperationException}。
     *
     * @return 锁
     */
    public Lock asWriteLock() {
        WriteLockView v;
        return ((v = writeLockView) != null ? v :
                (writeLockView = new WriteLockView()));
    }

    /**
     * 返回此 StampedLock 的一个 {@link ReadWriteLock} 视图，其中
     * {@link ReadWriteLock#readLock()} 方法映射到
     * {@link #asReadLock()}，而 {@link ReadWriteLock#writeLock()} 映射到
     * {@link #asWriteLock()}。
     *
     * @return 锁
     */
    public ReadWriteLock asReadWriteLock() {
        ReadWriteLockView v;
        return ((v = readWriteLockView) != null ? v :
                (readWriteLockView = new ReadWriteLockView()));
    }

    // 视图类

    final class ReadLockView implements Lock {
        public void lock() { readLock(); }
        public void lockInterruptibly() throws InterruptedException {
            readLockInterruptibly();
        }
        public boolean tryLock() { return tryReadLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryReadLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockRead(); }
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class WriteLockView implements Lock {
        public void lock() { writeLock(); }
        public void lockInterruptibly() throws InterruptedException {
            writeLockInterruptibly();
        }
        public boolean tryLock() { return tryWriteLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryWriteLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockWrite(); }
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class ReadWriteLockView implements ReadWriteLock {
        public Lock readLock() { return asReadLock(); }
        public Lock writeLock() { return asWriteLock(); }
    }

    // 为视图类提供的不带戳参数的解锁方法。
    // 需要这些方法是因为视图类的锁方法会丢弃戳。

    final void unstampedUnlockWrite() {
        WNode h; long s;
        if (((s = state) & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        state = (s += WBIT) == 0L ? ORIGIN : s;
        if ((h = whead) != null && h.status != 0)
            release(h);
    }

    final void unstampedUnlockRead() {
        for (;;) {
            long s, m; WNode h;
            if ((m = (s = state) & ABITS) == 0L || m >= WBIT)
                throw new IllegalMonitorStateException();
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    break;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        state = ORIGIN; // 重置为未锁定状态
    }

    // 内部方法

    /**
     * 尝试通过首先将状态访问位值设置为 RBITS，表示持有自旋锁，
     * 然后更新，最后释放，来增加 readerOverflow。
     *
     * @param s 读取溢出戳：(s & ABITS) >= RFULL
     * @return 成功时返回新戳，否则返回零
     */
    private long tryIncReaderOverflow(long s) {
        // assert (s & ABITS) >= RFULL;
        if ((s & ABITS) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                ++readerOverflow;
                state = s;
                return s;
            }
        }
        else if ((LockSupport.nextSecondarySeed() &
                  OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        return 0L;
    }

    /**
     * 尝试减少 readerOverflow。
     *
     * @param s 读取溢出戳：(s & ABITS) >= RFULL
     * @return 成功时返回新戳，否则返回零
     */
    private long tryDecReaderOverflow(long s) {
        // assert (s & ABITS) >= RFULL;
        if ((s & ABITS) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                int r; long next;
                if ((r = readerOverflow) > 0) {
                    readerOverflow = r - 1;
                    next = s;
                }
                else
                    next = s - RUNIT;
                 state = next;
                 return next;
            }
        }
        else if ((LockSupport.nextSecondarySeed() &
                  OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        return 0L;
    }

    /**
     * 唤醒 h（通常是 whead）的后继节点。这通常是 h.next，但如果 next
     * 指针滞后，则可能需要从 wtail 开始遍历。当一个或多个线程被取消时，
     * 这可能会失败唤醒获取线程，但取消方法本身提供了额外的保护措施以确保活跃性。
     */
    private void release(WNode h) {
        if (h != null) {
            WNode q; Thread w;
            U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
            if ((q = h.next) == null || q.status == CANCELLED) {
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            if (q != null && (w = q.thread) != null)
                U.unpark(w);
        }
    }

    /**
     * 请参见上述解释。
     *
     * @param interruptible 如果应检查中断并返回 INTERRUPTED，则为 true
     * @param deadline 如果非零，则为 System.nanoTime 值，超时后返回零
     * @return 下一个状态，或 INTERRUPTED
     */
    private long acquireWrite(boolean interruptible, long deadline) {
        WNode node = null, p;
        for (int spins = -1;;) { // 在入队时自旋
            long m, s, ns;
            if ((m = (s = state) & ABITS) == 0L) {
                if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                    return ns;
            }
            else if (spins < 0)
                spins = (m == WBIT && wtail == whead) ? SPINS : 0;
            else if (spins > 0) {
                if (LockSupport.nextSecondarySeed() >= 0)
                    --spins;
            }
            else if ((p = wtail) == null) { // 初始化队列
                WNode hd = new WNode(WMODE, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            else if (node == null)
                node = new WNode(WMODE, p);
            else if (node.prev != p)
                node.prev = p;
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node;
                break;
            }
        }


                    for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            if ((h = whead) == p) {
                if (spins < 0)
                    spins = HEAD_SPINS;
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                for (int k = spins;;) { // 在头部自旋
                    long s, ns;
                    if (((s = state) & ABITS) == 0L) {
                        if (U.compareAndSwapLong(this, STATE, s,
                                                 ns = s + WBIT)) {
                            whead = node;
                            node.prev = null;
                            return ns;
                        }
                    }
                    else if (LockSupport.nextSecondarySeed() >= 0 &&
                             --k <= 0)
                        break;
                }
            }
            else if (h != null) { // 帮助释放过时的等待者
                WNode c; Thread w;
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            if (whead == h) {
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // 过时
                }
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    long time; // park的0参数表示没有超时
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                        whead == h && node.prev == p)
                        U.park(false, time);  // 模拟 LockSupport.park
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    /**
     * 请参阅上方的解释。
     *
     * @param interruptible 如果应检查中断并返回 INTERRUPTED，则为 true
     * @param deadline 如果非零，则为 System.nanoTime 值，超时并返回零
     * @return 下一个状态，或 INTERRUPTED
     */
    private long acquireRead(boolean interruptible, long deadline) {
        WNode node = null, p;
        for (int spins = -1;;) {
            WNode h;
            if ((h = whead) == (p = wtail)) {
                for (long m, s, ns;;) {
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                        return ns;
                    else if (m >= WBIT) {
                        if (spins > 0) {
                            if (LockSupport.nextSecondarySeed() >= 0)
                                --spins;
                        }
                        else {
                            if (spins == 0) {
                                WNode nh = whead, np = wtail;
                                if ((nh == h && np == p) || (h = nh) != (p = np))
                                    break;
                            }
                            spins = SPINS;
                        }
                    }
                }
            }
            if (p == null) { // 初始化队列
                WNode hd = new WNode(WMODE, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            else if (node == null)
                node = new WNode(RMODE, p);
            else if (h == p || p.mode != RMODE) {
                if (node.prev != p)
                    node.prev = p;
                else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                    p.next = node;
                    break;
                }
            }
            else if (!U.compareAndSwapObject(p, WCOWAIT,
                                             node.cowait = p.cowait, node))
                node.cowait = null;
            else {
                for (;;) {
                    WNode pp, c; Thread w;
                    if ((h = whead) != null && (c = h.cowait) != null &&
                        U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null) // 帮助释放
                        U.unpark(w);
                    if (h == (pp = p.prev) || h == p || pp == null) {
                        long m, s, ns;
                        do {
                            if ((m = (s = state) & ABITS) < RFULL ?
                                U.compareAndSwapLong(this, STATE, s,
                                                     ns = s + RUNIT) :
                                (m < WBIT &&
                                 (ns = tryIncReaderOverflow(s)) != 0L))
                                return ns;
                        } while (m < WBIT);
                    }
                    if (whead == h && p.prev == pp) {
                        long time;
                        if (pp == null || h == p || p.status > 0) {
                            node = null; // 丢弃
                            break;
                        }
                        if (deadline == 0L)
                            time = 0L;
                        else if ((time = deadline - System.nanoTime()) <= 0L)
                            return cancelWaiter(node, p, false);
                        Thread wt = Thread.currentThread();
                        U.putObject(wt, PARKBLOCKER, this);
                        node.thread = wt;
                        if ((h != pp || (state & ABITS) == WBIT) &&
                            whead == h && p.prev == pp)
                            U.park(false, time);
                        node.thread = null;
                        U.putObject(wt, PARKBLOCKER, null);
                        if (interruptible && Thread.interrupted())
                            return cancelWaiter(node, p, true);
                    }
                }
            }
        }


                    for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            if ((h = whead) == p) {
                if (spins < 0)
                    spins = HEAD_SPINS;
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                for (int k = spins;;) { // 在头部自旋
                    long m, s, ns;
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                        WNode c; Thread w;
                        whead = node;
                        node.prev = null;
                        while ((c = node.cowait) != null) {
                            if (U.compareAndSwapObject(node, WCOWAIT,
                                                       c, c.cowait) &&
                                (w = c.thread) != null)
                                U.unpark(w);
                        }
                        return ns;
                    }
                    else if (m >= WBIT &&
                             LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break;
                }
            }
            else if (h != null) {
                WNode c; Thread w;
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            if (whead == h) {
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // 过时
                }
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    long time;
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    if (p.status < 0 &&
                        (p != h || (state & ABITS) == WBIT) &&
                        whead == h && node.prev == p)
                        U.park(false, time);
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    /**
     * 如果节点非空，强制取消状态并尽可能地将其从队列中解除链接，并唤醒任何共等待者（节点或组，视情况而定），并在任何情况下帮助释放当前第一个等待者，如果锁是空闲的。（使用空参数调用作为条件释放形式，目前不需要，但可能在未来的取消策略中需要）。这是AbstractQueuedSynchronizer中取消方法的一个变体（参见AQS内部文档的详细解释）。
     *
     * @param node 如果非空，则为等待者
     * @param group 节点或节点共等待的组
     * @param interrupted 如果已中断
     * @return 如果中断或Thread.interrupted，则返回INTERRUPTED，否则返回零
     */
    private long cancelWaiter(WNode node, WNode group, boolean interrupted) {
        if (node != null && group != null) {
            Thread w;
            node.status = CANCELLED;
            // 从组中解除取消的节点
            for (WNode p = group, q; (q = p.cowait) != null;) {
                if (q.status == CANCELLED) {
                    U.compareAndSwapObject(p, WCOWAIT, q, q.cowait);
                    p = group; // 重新开始
                }
                else
                    p = q;
            }
            if (group == node) {
                for (WNode r = group.cowait; r != null; r = r.cowait) {
                    if ((w = r.thread) != null)
                        U.unpark(w);       // 唤醒未取消的共等待者
                }
                for (WNode pred = node.prev; pred != null; ) { // 解除链接
                    WNode succ, pp;        // 查找有效的后继者
                    while ((succ = node.next) == null ||
                           succ.status == CANCELLED) {
                        WNode q = null;    // 慢速查找后继者
                        for (WNode t = wtail; t != null && t != node; t = t.prev)
                            if (t.status != CANCELLED)
                                q = t;     // 如果后继者已取消，则不链接
                        if (succ == q ||   // 确保准确的后继者
                            U.compareAndSwapObject(node, WNEXT,
                                                   succ, succ = q)) {
                            if (succ == null && node == wtail)
                                U.compareAndSwapObject(this, WTAIL, node, pred);
                            break;
                        }
                    }
                    if (pred.next == node) // 解除前驱链接
                        U.compareAndSwapObject(pred, WNEXT, node, succ);
                    if (succ != null && (w = succ.thread) != null) {
                        succ.thread = null;
                        U.unpark(w);       // 唤醒后继者以观察新的前驱
                    }
                    if (pred.status != CANCELLED || (pp = pred.prev) == null)
                        break;
                    node.prev = pp;        // 如果新的前驱错误或已取消，则重复
                    U.compareAndSwapObject(pp, WNEXT, pred, succ);
                    pred = pp;
                }
            }
        }
        WNode h; // 可能释放第一个等待者
        while ((h = whead) != null) {
            long s; WNode q; // 类似于release()，但检查资格
            if ((q = h.next) == null || q.status == CANCELLED) {
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            if (h == whead) {
                if (q != null && h.status == 0 &&
                    ((s = state) & ABITS) != WBIT && // 等待者有资格
                    (s == 0L || q.mode == RMODE))
                    release(h);
                break;
            }
        }
        return (interrupted || Thread.interrupted()) ? INTERRUPTED : 0L;
    }

                // 不安全机制
    private static final sun.misc.Unsafe U;
    private static final long STATE;
    private static final long WHEAD;
    private static final long WTAIL;
    private static final long WNEXT;
    private static final long WSTATUS;
    private static final long WCOWAIT;
    private static final long PARKBLOCKER;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = StampedLock.class;
            Class<?> wk = WNode.class;
            STATE = U.objectFieldOffset
                (k.getDeclaredField("state"));
            WHEAD = U.objectFieldOffset
                (k.getDeclaredField("whead"));
            WTAIL = U.objectFieldOffset
                (k.getDeclaredField("wtail"));
            WSTATUS = U.objectFieldOffset
                (wk.getDeclaredField("status"));
            WNEXT = U.objectFieldOffset
                (wk.getDeclaredField("next"));
            WCOWAIT = U.objectFieldOffset
                (wk.getDeclaredField("cowait"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
