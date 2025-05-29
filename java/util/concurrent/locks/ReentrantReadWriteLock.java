
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
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如 http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.Collection;

/**
 * 实现了 {@link ReadWriteLock}，支持与 {@link ReentrantLock} 类似的语义。
 * <p>此类具有以下属性：
 *
 * <ul>
 * <li><b>获取顺序</b>
 *
 * <p>此类不对读取或写入锁的访问施加读取或写入偏好顺序。但是，它支持可选的<em>公平性</em>策略。
 *
 * <dl>
 * <dt><b><i>非公平模式（默认）</i></b>
 * <dd>当以非公平模式（默认）构建时，读取和写入锁的进入顺序是未指定的，受重入约束的影响。一个持续争用的非公平锁可能会无限期地推迟一个或多个读取或写入线程，但通常比公平锁具有更高的吞吐量。
 *
 * <dt><b><i>公平模式</i></b>
 * <dd>当以公平模式构建时，线程以近似到达顺序的策略争夺进入。当当前持有的锁被释放时，等待时间最长的单个写入线程将被分配写入锁，或者如果有一组读取线程等待时间比所有等待的写入线程更长，那么该组将被分配读取锁。
 *
 * <p>尝试获取公平读取锁（非重入）的线程如果写入锁被持有，或者有等待的写入线程，将被阻塞。该线程直到最老的当前等待的写入线程获取并释放了写入锁后才会获取读取锁。当然，如果等待的写入线程放弃了等待，留下一个或多个读取线程作为队列中最长等待者且写入锁空闲，那么这些读取线程将被分配读取锁。
 *
 * <p>尝试获取公平写入锁（非重入）的线程如果读取锁和写入锁都空闲（这意味着没有等待的线程）才会被阻塞。（注意，非阻塞的 {@link ReadLock#tryLock()} 和 {@link WriteLock#tryLock()} 方法不会遵守此公平设置，如果可能，将立即获取锁，无论等待的线程如何。）
 * <p>
 * </dl>
 *
 * <li><b>重入性</b>
 *
 * <p>此锁允许读取者和写入者以 {@link ReentrantLock} 的风格重新获取读取或写入锁。在持有写入锁的线程释放所有写入锁之前，不允许非重入的读取者。
 *
 * <p>此外，写入者可以获取读取锁，但反之则不行。在写入锁被持有的调用或回调方法中执行读取锁下的读取时，重入可以非常有用。如果读取者尝试获取写入锁，它将永远不会成功。
 *
 * <li><b>锁降级</b>
 * <p>重入性还允许从写入锁降级到读取锁，通过获取写入锁，然后获取读取锁，最后释放写入锁。然而，从读取锁升级到写入锁是<b>不可能的</b>。
 *
 * <li><b>锁获取中断</b>
 * <p>读取锁和写入锁都支持在锁获取期间的中断。
 *
 * <li><b>{@link Condition} 支持</b>
 * <p>写入锁提供了一个 {@link Condition} 实现，该实现与 {@link ReentrantLock#newCondition} 为 {@link ReentrantLock} 提供的 {@link Condition} 实现的行为相同。当然，此 {@link Condition} 只能与写入锁一起使用。
 *
 * <p>读取锁不支持 {@link Condition}，并且 {@code readLock().newCondition()} 会抛出 {@code UnsupportedOperationException}。
 *
 * <li><b>监控</b>
 * <p>此类支持确定锁是否被持有或争用的方法。这些方法旨在用于监控系统状态，而不是用于同步控制。
 * </ul>
 *
 * <p>此类的序列化行为与内置锁相同：反序列化的锁处于未锁定状态，无论其在序列化时的状态如何。
 *
 * <p><b>示例用法</b>。以下是一个代码示例，展示了如何在更新缓存后执行锁降级（处理多个锁的非嵌套方式时，异常处理特别棘手）：
 *
 * <pre> {@code
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *
 *   void processCachedData() {
 *     rwl.readLock().lock();
 *     if (!cacheValid) {
 *       // 必须在获取写入锁之前释放读取锁
 *       rwl.readLock().unlock();
 *       rwl.writeLock().lock();
 *       try {
 *         // 重新检查状态，因为另一个线程可能在我们之前获取了写入锁并改变了状态。
 *         if (!cacheValid) {
 *           data = ...
 *           cacheValid = true;
 *         }
 *         // 通过在释放写入锁之前获取读取锁来降级
 *         rwl.readLock().lock();
 *       } finally {
 *         rwl.writeLock().unlock(); // 解锁写入，但仍持有读取
 *       }
 *     }
 *
 *     try {
 *       use(data);
 *     } finally {
 *       rwl.readLock().unlock();
 *     }
 *   }
 * }}</pre>
 *
 * ReentrantReadWriteLock 可用于提高某些类型的集合在某些使用情况下的并发性。这通常仅在预期集合较大、读取线程多于写入线程且操作的开销超过同步开销时才有价值。例如，以下是一个使用 TreeMap 的类，该类预期较大且并发访问。
 *
 *  <pre> {@code
 * class RWDictionary {
 *   private final Map<String, Data> m = new TreeMap<String, Data>();
 *   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *   private final Lock r = rwl.readLock();
 *   private final Lock w = rwl.writeLock();
 *
 *   public Data get(String key) {
 *     r.lock();
 *     try { return m.get(key); }
 *     finally { r.unlock(); }
 *   }
 *   public String[] allKeys() {
 *     r.lock();
 *     try { return m.keySet().toArray(); }
 *     finally { r.unlock(); }
 *   }
 *   public Data put(String key, Data value) {
 *     w.lock();
 *     try { return m.put(key, value); }
 *     finally { w.unlock(); }
 *   }
 *   public void clear() {
 *     w.lock();
 *     try { m.clear(); }
 *     finally { w.unlock(); }
 *   }
 * }}</pre>
 *
 * <h3>实现说明</h3>
 *
 * <p>此锁支持最多 65535 个递归写入锁和 65535 个读取锁。尝试超过这些限制将导致锁定方法抛出 {@link Error}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantReadWriteLock
        implements ReadWriteLock, java.io.Serializable {
    private static final long serialVersionUID = -6992448646407690164L;
    /** 提供读取锁的内部类 */
    private final ReentrantReadWriteLock.ReadLock readerLock;
    /** 提供写入锁的内部类 */
    private final ReentrantReadWriteLock.WriteLock writerLock;
    /** 执行所有同步机制 */
    final Sync sync;

                /**
     * 创建一个具有默认（非公平）排序属性的新 {@code ReentrantReadWriteLock}。
     */
    public ReentrantReadWriteLock() {
        this(false);
    }

    /**
     * 创建一个具有给定公平策略的新 {@code ReentrantReadWriteLock}。
     *
     * @param fair 如果此锁应使用公平排序策略，则为 {@code true}
     */
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
    public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }

    /**
     * ReentrantReadWriteLock 的同步实现。
     * 分为公平和非公平版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 6317671515068378041L;

        /*
         * 读取与写入计数提取常量和函数。
         * 锁状态逻辑上分为两个无符号短整型：
         * 较低的一个表示独占（写入）锁持有计数，
         * 较高的一个表示共享（读取）持有计数。
         */

        static final int SHARED_SHIFT   = 16;
        static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
        static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

        /** 返回计数中表示的共享持有次数 */
        static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
        /** 返回计数中表示的独占持有次数 */
        static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }

        /**
         * 每线程读取持有计数的计数器。
         * 作为 ThreadLocal 维护；缓存在 cachedHoldCounter 中
         */
        static final class HoldCounter {
            int count = 0;
            // 使用 id 而不是引用，以避免垃圾保留
            final long tid = getThreadId(Thread.currentThread());
        }

        /**
         * ThreadLocal 子类。为了反序列化机制的需要，显式定义。
         */
        static final class ThreadLocalHoldCounter
            extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        /**
         * 当前线程持有的可重入读取锁的数量。
         * 仅在构造函数和 readObject 中初始化。
         * 每当线程的读取持有计数降至 0 时，都会移除。
         */
        private transient ThreadLocalHoldCounter readHolds;

        /**
         * 成功获取读取锁的最后一个线程的持有计数。
         * 这节省了在常见情况下下一个释放线程是最后一个获取线程时的 ThreadLocal 查找。
         * 这不是易失性的，因为它只是一个启发式方法，线程缓存它是很好的。
         *
         * <p>可以比它缓存的读取持有计数的线程更长寿，但通过不保留对线程的引用来避免垃圾保留。
         *
         * <p>通过良性数据竞争访问；依赖于内存模型的最终字段和无中生有的保证。
         */
        private transient HoldCounter cachedHoldCounter;

        /**
         * firstReader 是第一个获取读取锁的线程。
         * firstReaderHoldCount 是 firstReader 的持有计数。
         *
         * <p>更准确地说，firstReader 是最后一个将共享计数从 0 改为 1 的唯一线程，并且自此之后没有释放读取锁；如果没有这样的线程，则为 null。
         *
         * <p>除非线程在没有释放其读取锁的情况下终止，否则不会导致垃圾保留，因为 tryReleaseShared 会将其设置为 null。
         *
         * <p>通过良性数据竞争访问；依赖于内存模型的无中生有引用保证。
         *
         * <p>这允许对无竞争的读取锁的读取持有进行非常廉价的跟踪。
         */
        private transient Thread firstReader = null;
        private transient int firstReaderHoldCount;

        Sync() {
            readHolds = new ThreadLocalHoldCounter();
            setState(getState()); // 确保 readHolds 的可见性
        }

        /*
         * 获取和释放使用相同的代码来处理公平和非公平锁，但在队列非空时是否/如何允许插队方面有所不同。
         */

        /**
         * 如果当前线程在尝试获取读取锁时，且在其他方面有资格这样做，但由于超越其他等待线程的策略而应阻塞，则返回 true。
         */
        abstract boolean readerShouldBlock();

        /**
         * 如果当前线程在尝试获取写入锁时，且在其他方面有资格这样做，但由于超越其他等待线程的策略而应阻塞，则返回 true。
         */
        abstract boolean writerShouldBlock();

        /*
         * 请注意，tryRelease 和 tryAcquire 可以由 Conditions 调用。
         * 因此，它们的参数可能包含在条件等待期间全部释放并在 tryAcquire 中重新建立的读取和写入持有。
         */

        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setExclusiveOwnerThread(null);
            setState(nextc);
            return free;
        }

        protected final boolean tryAcquire(int acquires) {
            /*
             * 步骤：
             * 1. 如果读取计数非零或写入计数非零
             *    且所有者是不同的线程，则失败。
             * 2. 如果计数会饱和，则失败。（这只能在计数已经非零时发生。）
             * 3. 否则，如果线程是重入获取或
             *    队列策略允许它，则有资格获取锁。如果可以，更新状态
             *    并设置所有者。
             */
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                // （注意：如果 c != 0 且 w == 0，则共享计数 != 0）
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // 重入获取
                setState(c + acquires);
                return true;
            }
            if (writerShouldBlock() ||
                !compareAndSetState(c, c + acquires))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }


                    protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread();
            if (firstReader == current) {
                // 断言 firstReaderHoldCount > 0;
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0)
                        throw unmatchedUnlockException();
                }
                --rh.count;
            }
            for (;;) {
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    // 释放读锁对读者没有影响，
                    // 但如果读锁和写锁现在都空闲，可能允许等待的写者继续。
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException(
                "尝试解锁读锁，但当前线程未锁定该锁");
        }

        protected final int tryAcquireShared(int unused) {
            /*
             * 流程：
             * 1. 如果写锁被其他线程持有，失败。
             * 2. 否则，此线程在状态方面有资格获得锁，因此询问是否应因队列策略而阻塞。
             *    如果不阻塞，尝试通过CAS状态和更新计数来授予锁。
             *    注意，此步骤不检查重入获取，这被推迟到完整版本，
             *    以避免在更典型的非重入情况下检查持有计数。
             * 3. 如果步骤2失败，无论是因为线程显然没有资格，还是CAS失败，还是计数饱和，
             *    则链接到具有完整重试循环的版本。
             */
            Thread current = Thread.currentThread();
            int c = getState();
            if (exclusiveCount(c) != 0 &&
                getExclusiveOwnerThread() != current)
                return -1;
            int r = sharedCount(c);
            if (!readerShouldBlock() &&
                r < MAX_COUNT &&
                compareAndSetState(c, c + SHARED_UNIT)) {
                if (r == 0) {
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    firstReaderHoldCount++;
                } else {
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * 读取的完整获取版本，处理在 tryAcquireShared 中未处理的 CAS 失败和重入读取。
         */
        final int fullTryAcquireShared(Thread current) {
            /*
             * 此代码部分冗余于 tryAcquireShared 中的代码，但通过不使 tryAcquireShared
             * 复杂化与重试和懒惰读取持有计数之间的交互而整体上更简单。
             */
            HoldCounter rh = null;
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // 否则我们持有独占锁；在这里阻塞会导致死锁。
                } else if (readerShouldBlock()) {
                    // 确保我们不是重入地获取读锁
                    if (firstReader == current) {
                        // 断言 firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            return -1;
                    }
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("最大锁计数超出");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // 为释放缓存
                    }
                    return 1;
                }
            }
        }

        /**
         * 执行写锁的 tryLock，允许在两种模式下抢占。
         * 除了没有调用 writerShouldBlock 之外，效果与 tryAcquire 完全相同。
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w == MAX_COUNT)
                    throw new Error("最大锁计数超出");
            }
            if (!compareAndSetState(c, c + 1))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }


                    /**
         * 执行读取的 tryLock，允许在两种模式下强行插入。
         * 除了没有调用 readerShouldBlock 之外，其效果与 tryAcquireShared 完全相同。
         */
        final boolean tryReadLock() {
            Thread current = Thread.currentThread();
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0 &&
                    getExclusiveOwnerThread() != current)
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("最大锁计数超出");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // 虽然我们通常需要在读取所有者之前读取状态，
            // 但我们不需要这样做来检查当前线程是否是所有者
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        // 传递到外部类的方法

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            // 必须在读取所有者之前读取状态以确保内存一致性
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;

            Thread current = Thread.currentThread();
            if (firstReader == current)
                return firstReaderHoldCount;

            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid == getThreadId(current))
                return rh.count;

            int count = readHolds.get().count;
            if (count == 0) readHolds.remove();
            return count;
        }

        /**
         * 从流中重新构建实例（即反序列化）。
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // 重置为未锁定状态
        }

        final int getCount() { return getState(); }
    }

    /**
     * 非公平版本的 Sync
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;
        final boolean writerShouldBlock() {
            return false; // 写入者可以总是强行插入
        }
        final boolean readerShouldBlock() {
            /* 作为一种避免无限写入者饥饿的启发式方法，
             * 如果队列中暂时看起来是头部的线程（如果存在）是一个等待的写入者，则阻塞。
             * 这只是一个概率效应，因为如果等待的写入者在其他已启用的读取者之后，
             * 而这些读取者尚未从队列中排出，那么新的读取者将不会阻塞。
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    /**
     * 公平版本的 Sync
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }
        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    /**
     * 由方法 {@link ReentrantReadWriteLock#readLock} 返回的锁。
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        /**
         * 供子类使用的构造函数
         *
         * @param lock 外部锁对象
         * @throws NullPointerException 如果锁为 null
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * 获取读锁。
         *
         * <p>如果写锁未被其他线程持有，则立即获取读锁。
         *
         * <p>如果写锁被其他线程持有，则当前线程将被禁用以进行线程调度
         * 并处于休眠状态，直到获取读锁。
         */
        public void lock() {
            sync.acquireShared(1);
        }

        /**
         * 如果当前线程未被 {@linkplain Thread#interrupt 中断}，则获取读锁。
         *
         * <p>如果写锁未被其他线程持有，则立即获取读锁。
         *
         * <p>如果写锁被其他线程持有，则当前线程将被禁用以进行线程调度
         * 并处于休眠状态，直到发生以下两种情况之一：
         *
         * <ul>
         *
         * <li>当前线程获取了读锁；或
         *
         * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程。
         *
         * </ul>
         *
         * <p>如果当前线程：
         *
         * <ul>
         *
         * <li>在进入此方法时已设置中断状态；或
         *
         * <li>在获取读锁时被 {@linkplain Thread#interrupt 中断}，
         *
         * </ul>
         *
         * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
         *
         * <p>在此实现中，由于此方法是一个显式的中断点，因此优先响应
         * 中断而不是正常或重入获取锁。
         *
         * @throws InterruptedException 如果当前线程被中断
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }


                    /**
         * 仅在调用时写锁未被其他线程持有时获取读锁。
         *
         * <p>如果写锁未被其他线程持有，则立即获取读锁并返回 {@code true}。即使此锁已设置为使用公平排序策略，调用 {@code tryLock()}
         * <em>将</em> 立即获取可用的读锁，无论是否有其他线程当前正在等待读锁。这种“插队”行为在某些情况下是有用的，尽管它破坏了公平性。如果您希望遵循此锁的公平设置，
         * 则使用 {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }，这几乎是等效的（它还检测中断）。
         *
         * <p>如果写锁被其他线程持有，则此方法将立即返回 {@code false}。
         *
         * @return 如果读锁被获取，则返回 {@code true}
         */
        public boolean tryLock() {
            return sync.tryReadLock();
        }

        /**
         * 如果写锁未被其他线程持有，并且当前线程未被 {@linkplain Thread#interrupt
         * 中断}，则在给定的等待时间内获取读锁。
         *
         * <p>如果写锁未被其他线程持有，则立即获取读锁并返回 {@code true}。如果此锁已设置为使用公平排序策略，则不会获取可用的锁
         * <em>不会</em> 如果有其他线程正在等待锁。这与 {@link #tryLock()} 方法不同。如果您希望在公平锁上允许插队的定时 {@code tryLock}，则可以组合定时和非定时形式：
         *
         *  <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         *
         * <p>如果写锁被其他线程持有，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到以下三种情况之一发生：
         *
         * <ul>
         *
         * <li>当前线程获取了读锁；或
         *
         * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程；或
         *
         * <li>指定的等待时间已过。
         *
         * </ul>
         *
         * <p>如果读锁被获取，则返回 {@code true}。
         *
         * <p>如果当前线程：
         *
         * <ul>
         *
         * <li>在进入此方法时已设置中断状态；或
         *
         * <li>在获取读锁时被 {@linkplain Thread#interrupt 中断}，
         *
         * </ul> 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
         *
         * <p>如果指定的等待时间已过，则返回 {@code false}。如果时间小于或等于零，方法将不会等待。
         *
         * <p>在此实现中，由于此方法是一个显式的中断点，因此优先响应中断，而不是正常或重新获取锁，以及报告等待时间的流逝。
         *
         * @param timeout 等待读锁的时间
         * @param unit 超时参数的时间单位
         * @return 如果读锁被获取，则返回 {@code true}
         * @throws InterruptedException 如果当前线程被中断
         * @throws NullPointerException 如果时间单位为 null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        /**
         * 尝试释放此锁。
         *
         * <p>如果读取器数量现在为零，则锁将可用于写锁尝试。
         */
        public void unlock() {
            sync.releaseShared(1);
        }

        /**
         * 抛出 {@code UnsupportedOperationException}，因为 {@code ReadLocks} 不支持条件。
         *
         * @throws UnsupportedOperationException 始终抛出
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * 返回一个标识此锁及其锁状态的字符串。状态（在括号中）包括字符串 {@code "Read locks ="}
         * 后跟持有的读锁数量。
         *
         * @return 一个标识此锁及其锁状态的字符串
         */
        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                "[Read locks = " + r + "]";
        }
    }

    /**
     * 由方法 {@link ReentrantReadWriteLock#writeLock} 返回的锁。
     */
    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        /**
         * 供子类使用的构造函数
         *
         * @param lock 外部锁对象
         * @throws NullPointerException 如果锁为 null
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * 获取写锁。
         *
         * <p>如果读锁和写锁均未被其他线程持有，则立即获取写锁，并将写锁持有计数设置为
         * 一。
         *
         * <p>如果当前线程已经持有写锁，则写锁持有计数递增一，并立即返回。
         *
         * <p>如果锁被其他线程持有，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到获取写锁，此时写锁持有计数设置为一。
         */
        public void lock() {
            sync.acquire(1);
        }


                    /**
         * 获取写锁，除非当前线程被 {@linkplain Thread#interrupt 中断}。
         *
         * <p>如果读锁或写锁都没有被其他线程持有，则获取写锁
         * 并立即返回，将写锁持有计数设置为
         * 一。
         *
         * <p>如果当前线程已经持有此锁，则
         * 持有计数增加一，方法立即返回。
         *
         * <p>如果锁被其他线程持有，则当前
         * 线程将被禁用以进行线程调度
         * 并休眠，直到发生以下两种情况之一：
         *
         * <ul>
         *
         * <li>当前线程获取了写锁；或
         *
         * <li>其他线程 {@linkplain Thread#interrupt 中断}
         * 当前线程。
         *
         * </ul>
         *
         * <p>如果当前线程获取了写锁，则
         * 锁持有计数设置为一。
         *
         * <p>如果当前线程：
         *
         * <ul>
         *
         * <li>在进入此方法时其中断状态已设置；
         * 或
         *
         * <li>在获取写锁时被
         * {@linkplain Thread#interrupt 中断}，
         *
         * </ul>
         *
         * 则抛出 {@link InterruptedException} 并清除当前
         * 线程的中断状态。
         *
         * <p>在此实现中，由于此方法是一个显式的
         * 中断点，因此优先响应
         * 中断，而不是正常或重入获取锁。
         *
         * @throws InterruptedException 如果当前线程被中断
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        /**
         * 仅在调用时锁未被其他线程持有时获取写锁。
         *
         * <p>如果读锁或写锁都没有被其他线程持有，
         * 则获取写锁并立即返回 {@code true}，
         * 将写锁持有计数设置为一。即使此锁已设置为使用公平排序策略，
         * 调用 {@code tryLock()} <em>将</em> 立即获取
         * 可用的锁，无论是否有其他线程正在等待写锁。这种“插队”行为在某些情况下是有用的，
         * 即使它破坏了公平性。如果您希望遵守此锁的公平设置，则使用 {@link
         * #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
         * 几乎等效（它还检测中断）。
         *
         * <p>如果当前线程已经持有此锁，则
         * 持有计数增加一，方法返回
         * {@code true}。
         *
         * <p>如果锁被其他线程持有，则此方法
         * 将立即返回 {@code false}。
         *
         * @return 如果锁是自由的并被当前线程获取，或者当前线程已经持有
         * 写锁，则返回 {@code true}；否则返回 {@code false}。
         */
        public boolean tryLock( ) {
            return sync.tryWriteLock();
        }

        /**
         * 如果写锁未被其他线程持有，并且当前线程未被 {@linkplain Thread#interrupt 中断}，
         * 则在给定的等待时间内获取写锁。
         *
         * <p>如果读锁或写锁都没有被其他线程持有，
         * 则获取写锁并立即返回 {@code true}，
         * 将写锁持有计数设置为一。如果此锁已设置为使用公平排序策略，则不会
         * 获取可用的锁，如果其他线程正在等待写锁。这与 {@link
         * #tryLock()} 方法不同。如果您希望在公平锁上允许定时的 {@code tryLock}
         * 插队，则将定时和非定时形式组合在一起：
         *
         *  <pre> {@code
         * if (lock.tryLock() ||
         *     lock.tryLock(timeout, unit)) {
         *   ...
         * }}</pre>
         *
         * <p>如果当前线程已经持有此锁，则
         * 持有计数增加一，方法返回
         * {@code true}。
         *
         * <p>如果锁被其他线程持有，则当前
         * 线程将被禁用以进行线程调度
         * 并休眠，直到发生以下三种情况之一：
         *
         * <ul>
         *
         * <li>当前线程获取了写锁；或
         *
         * <li>其他线程 {@linkplain Thread#interrupt 中断}
         * 当前线程；或
         *
         * <li>指定的等待时间已过
         *
         * </ul>
         *
         * <p>如果获取了写锁，则返回值为 {@code true}，并将写锁持有计数设置为一。
         *
         * <p>如果当前线程：
         *
         * <ul>
         *
         * <li>在进入此方法时其中断状态已设置；
         * 或
         *
         * <li>在获取写锁时被
         * {@linkplain Thread#interrupt 中断}，
         *
         * </ul>
         *
         * 则抛出 {@link InterruptedException} 并清除当前
         * 线程的中断状态。
         *
         * <p>如果指定的等待时间已过，则返回值为
         * {@code false}。如果时间小于或
         * 等于零，方法将不会等待。
         *
         * <p>在此实现中，由于此方法是一个显式的
         * 中断点，因此优先响应
         * 中断，而不是正常或重入获取锁，以及报告等待时间的流逝。
         *
         * @param timeout 等待写锁的时间
         * @param unit 超时参数的时间单位
         *
         * @return 如果锁是自由的并被当前线程获取，或者当前线程已经持有
         * 写锁，则返回 {@code true}；如果等待时间已过而锁未被获取，则返回 {@code false}。
         *
         * @throws InterruptedException 如果当前线程被中断
         * @throws NullPointerException 如果时间单位为 null
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }


                    /**
         * 尝试释放此锁。
         *
         * <p>如果当前线程是此锁的持有者，则持有计数将递减。如果持有计数现在为零，则锁被释放。如果当前线程不是此锁的持有者，则抛出 {@link
         * IllegalMonitorStateException}。
         *
         * @throws IllegalMonitorStateException 如果当前线程未持有此锁
         */
        public void unlock() {
            sync.release(1);
        }

        /**
         * 返回一个 {@link Condition} 实例，用于与这个 {@link Lock} 实例一起使用。
         * <p>返回的 {@link Condition} 实例支持与内置监视器锁一起使用的 {@link
         * Object} 监视器方法（{@link Object#wait() wait}，{@link Object#notify notify} 和 {@link
         * Object#notifyAll notifyAll}）相同的用法。
         *
         * <ul>
         *
         * <li>如果在调用任何 {@link
         * Condition} 方法时未持有此写锁，则抛出 {@link
         * IllegalMonitorStateException}。（读锁独立于写锁，因此不会被检查或影响。但是，在当前线程还持有读锁的情况下调用条件等待方法基本上总是错误的，因为其他可以解除阻塞的线程将无法获取写锁。）
         *
         * <li>当调用条件 {@linkplain Condition#await() 等待} 方法时，写锁将被释放，并且在它们返回之前，写锁将被重新获取，锁的持有计数将恢复到调用方法时的状态。
         *
         * <li>如果等待中的线程被 {@linkplain Thread#interrupt 中断}，则等待将终止，抛出 {@link
         * InterruptedException}，并且线程的中断状态将被清除。
         *
         * <li>等待线程按 FIFO 顺序被通知。
         *
         * <li>从等待方法返回的线程重新获取锁的顺序与最初获取锁的线程相同，默认情况下未指定，但对于 <em>公平</em> 锁，优先考虑等待时间最长的线程。
         *
         * </ul>
         *
         * @return Condition 对象
         */
        public Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * 返回一个标识此锁的字符串，以及其锁状态。状态，用括号表示，包括字符串
         * {@code "Unlocked"} 或字符串 {@code "Locked by"}
         * 后跟 {@linkplain Thread#getName 拥有线程的名称}。
         *
         * @return 一个标识此锁及其锁状态的字符串
         */
        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                                       "[Unlocked]" :
                                       "[Locked by thread " + o.getName() + "]");
        }

        /**
         * 查询当前线程是否持有此写锁。效果与 {@link
         * ReentrantReadWriteLock#isWriteLockedByCurrentThread} 相同。
         *
         * @return 如果当前线程持有此锁，则返回 {@code true}，否则返回 {@code false}
         * @since 1.6
         */
        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        /**
         * 查询当前线程对这个写锁的持有次数。线程对锁的持有次数等于每次锁定操作与解锁操作不匹配的次数。效果与
         * {@link ReentrantReadWriteLock#getWriteHoldCount} 相同。
         *
         * @return 当前线程对这个锁的持有次数，如果当前线程未持有此锁，则返回零
         * @since 1.6
         */
        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

    // 仪器和状态

    /**
     * 如果此锁设置了公平性，则返回 {@code true}。
     *
     * @return 如果此锁设置了公平性，则返回 {@code true}
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 返回当前拥有写锁的线程，如果没有拥有则返回
     * {@code null}。当此方法由非拥有者线程调用时，返回值反映了当前锁状态的最佳努力近似值。例如，即使有线程尝试获取锁但尚未成功，所有者也可能暂时为 {@code null}。
     * 此方法旨在促进构建提供更广泛的锁监控设施的子类。
     *
     * @return 所有者，或如果未拥有则返回 {@code null}
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * 查询对此锁持有的读锁数量。此方法旨在用于监视系统状态，而不是用于同步控制。
     * @return 持有的读锁数量
     */
    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    /**
     * 查询是否有任何线程持有写锁。此方法旨在用于监视系统状态，而不是用于同步控制。
     *
     * @return 如果任何线程持有写锁，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    /**
     * 查询当前线程是否持有写锁。
     *
     * @return 如果当前线程持有写锁，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }


                /**
     * 查询当前线程对这个锁的重入写持有的次数。写线程每次执行锁操作而没有相应的解锁操作时，就持有一个锁。
     *
     * @return 当前线程对写锁的持有次数，如果当前线程没有持有写锁，则返回零
     */
    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    /**
     * 查询当前线程对这个锁的重入读持有的次数。读线程每次执行锁操作而没有相应的解锁操作时，就持有一个锁。
     *
     * @return 当前线程对读锁的持有次数，如果当前线程没有持有读锁，则返回零
     * @since 1.6
     */
    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    /**
     * 返回可能正在等待获取写锁的线程集合。由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。返回集合中的元素没有特定的顺序。此方法旨在促进子类提供更广泛的锁监控功能。
     *
     * @return 线程集合
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * 返回可能正在等待获取读锁的线程集合。由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。返回集合中的元素没有特定的顺序。此方法旨在促进子类提供更广泛的锁监控功能。
     *
     * @return 线程集合
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * 查询是否有任何线程正在等待获取读或写锁。请注意，由于取消可能随时发生，因此返回 {@code true} 并不保证任何其他线程将最终获取锁。此方法主要用于系统状态的监控。
     *
     * @return 如果可能有其他线程正在等待获取锁，则返回 {@code true}
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 查询给定线程是否正在等待获取读或写锁。请注意，由于取消可能随时发生，因此返回 {@code true} 并不保证该线程将最终获取锁。此方法主要用于系统状态的监控。
     *
     * @param thread 线程
     * @return 如果给定线程正在排队等待此锁，则返回 {@code true}
     * @throws NullPointerException 如果线程为 null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * 返回估计的正在等待获取读或写锁的线程数。该值只是一个估计，因为线程数可能在方法遍历内部数据结构时动态变化。此方法旨在用于系统状态的监控，而不是同步控制。
     *
     * @return 等待此锁的线程数的估计值
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * 返回可能正在等待获取读或写锁的线程集合。由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。返回集合中的元素没有特定的顺序。此方法旨在促进子类提供更广泛的监控功能。
     *
     * @return 线程集合
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 查询是否有任何线程正在等待与写锁关联的给定条件。请注意，由于超时和中断可能随时发生，因此返回 {@code true} 并不保证未来的 {@code signal} 将唤醒任何线程。此方法主要用于系统状态的监控。
     *
     * @param condition 条件
     * @return 如果有等待的线程，则返回 {@code true}
     * @throws IllegalMonitorStateException 如果此锁未被持有
     * @throws IllegalArgumentException 如果给定条件未与此锁关联
     * @throws NullPointerException 如果条件为 null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * 返回与写锁关联的给定条件正在等待的线程数的估计值。请注意，由于超时和中断可能随时发生，因此估计值仅作为实际等待者数量的上限。此方法旨在用于系统状态的监控，而不是同步控制。
     *
     * @param condition 条件
     * @return 等待线程数的估计值
     * @throws IllegalMonitorStateException 如果此锁未被持有
     * @throws IllegalArgumentException 如果给定条件未与此锁关联
     * @throws NullPointerException 如果条件为 null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

                /**
     * 返回一个包含可能正在等待与写锁关联的给定条件的线程的集合。
     * 由于实际的线程集可能在构建此结果时动态变化，因此返回的集合只是一个尽力而为的估计。
     * 返回的集合中的元素没有特定的顺序。此方法旨在促进提供更广泛的条件监控设施的子类的构建。
     *
     * @param condition 条件
     * @return 线程集合
     * @throws IllegalMonitorStateException 如果此锁未被持有
     * @throws IllegalArgumentException 如果给定条件未与此锁关联
     * @throws NullPointerException 如果条件为 null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * 返回一个标识此锁及其锁状态的字符串。
     * 状态（在方括号中）包括字符串 {@code "Write locks ="} 后跟重新进入持有的写锁数量，
     * 以及字符串 {@code "Read locks ="} 后跟持有的读锁数量。
     *
     * @return 一个标识此锁及其锁状态的字符串
     */
    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
            "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    /**
     * 返回给定线程的线程 ID。我们必须直接访问此值，而不是通过方法 Thread.getId()，
     * 因为 getId() 不是 final 的，并且已知在某些情况下被重写，导致不保留唯一映射。
     */
    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            TID_OFFSET = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("tid"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
