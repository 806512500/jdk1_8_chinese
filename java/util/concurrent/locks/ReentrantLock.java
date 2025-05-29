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
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.Collection;

/**
 * 一个可重入的互斥 {@link Lock}，具有与使用 {@code synchronized} 方法和语句访问的隐式监视器锁相同的基本行为和语义，但具有扩展的功能。
 *
 * <p>一个 {@code ReentrantLock} 被最后成功锁定但尚未解锁的线程<em>拥有</em>。调用 {@code lock} 的线程将在锁未被其他线程拥有时返回，成功获取锁。如果当前线程已经拥有锁，该方法将立即返回。这可以通过方法 {@link #isHeldByCurrentThread} 和 {@link
 * #getHoldCount} 检查。
 *
 * <p>此类的构造函数接受一个可选的<em>公平性</em>参数。当设置为 {@code true} 时，在争用情况下，锁倾向于授予等待时间最长的线程。否则，此锁不保证任何特定的访问顺序。使用许多线程访问公平锁的程序可能显示较低的整体吞吐量（即，较慢；通常慢得多）比使用默认设置的程序，但锁的获取时间方差较小，并保证不会出现饥饿。然而，需要注意的是，锁的公平性并不保证线程调度的公平性。因此，使用公平锁的多个线程之一可能在其他活跃线程没有进展且当前未持有锁的情况下多次获得它。
 * 还需注意，未计时的 {@link #tryLock()} 方法不遵守公平性设置。如果锁可用，即使其他线程正在等待，它也会成功。
 *
 * <p>建议的做法是<em>始终</em>立即将对 {@code lock} 的调用与一个 {@code try} 块结合使用，最典型的用法是在一个 before/after 构造中，例如：
 *
 *  <pre> {@code
 * class X {
 *   private final ReentrantLock lock = new ReentrantLock();
 *   // ...
 *
 *   public void m() {
 *     lock.lock();  // 阻塞直到条件满足
 *     try {
 *       // ... 方法体
 *     } finally {
 *       lock.unlock()
 *     }
 *   }
 * }}</pre>
 *
 * <p>除了实现 {@link Lock} 接口外，此类还定义了许多用于检查锁状态的 {@code public} 和 {@code protected} 方法。其中一些方法仅对监视和诊断有用。
 *
 * <p>此类的序列化行为与内置锁相同：反序列化的锁处于未锁定状态，无论其在序列化时的状态如何。
 *
 * <p>此锁支持同一个线程最多 2147483647 次递归锁定。尝试超过此限制将导致锁定方法抛出 {@link Error}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    /** 提供所有实现机制的同步器 */
    private final Sync sync;

    /**
     * 此锁的同步控制基础。在下面子类化为公平和非公平版本。使用 AQS 状态表示锁的持有次数。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * 执行 {@link Lock#lock}。子类化的主要原因是允许非公平版本的快速路径。
         */
        abstract void lock();

        /**
         * 执行非公平的 tryLock。tryAcquire 在子类中实现，但两者都需要非公平的 try 用于 trylock 方法。
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // 溢出
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // 虽然我们通常需要在读取所有者之前读取状态，
            // 但我们不需要这样做来检查当前线程是否是所有者
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // 从外部类转发的方法

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * 从流中重建实例（即，反序列化它）。
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // 重置为未锁定状态
        }
    }

}


/**
 * 非公平锁的同步对象
 */
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * 执行锁操作。尝试立即抢占，如果失败则回退到正常的获取操作。
     */
    final void lock() {
        if (compareAndSetState(0, 1))
            setExclusiveOwnerThread(Thread.currentThread());
        else
            acquire(1);
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}

/**
 * 公平锁的同步对象
 */
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }

    /**
     * 公平版本的 tryAcquire。除非是递归调用或没有等待者或当前线程是第一个等待者，否则不授予访问权限。
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("最大锁计数超出");
            setState(nextc);
            return true;
        }
        return false;
    }
}

/**
 * 创建一个 {@code ReentrantLock} 实例。
 * 这相当于使用 {@code ReentrantLock(false)}。
 */
public ReentrantLock() {
    sync = new NonfairSync();
}

/**
 * 使用给定的公平策略创建一个 {@code ReentrantLock} 实例。
 *
 * @param fair 如果此锁应使用公平排序策略，则为 {@code true}
 */
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}

/**
 * 获取锁。
 *
 * <p>如果锁未被其他线程持有，则立即获取锁并返回，将锁持有计数设置为一。
 *
 * <p>如果当前线程已经持有锁，则将持有计数递增一并立即返回。
 *
 * <p>如果锁被其他线程持有，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到获取到锁，此时锁持有计数将被设置为一。
 */
public void lock() {
    sync.lock();
}

/**
 * 除非当前线程被 {@linkplain Thread#interrupt 中断}，否则获取锁。
 *
 * <p>如果锁未被其他线程持有，则立即获取锁并返回，将锁持有计数设置为一。
 *
 * <p>如果当前线程已经持有此锁，则将持有计数递增一并立即返回。
 *
 * <p>如果锁被其他线程持有，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到以下两种情况之一发生：
 *
 * <ul>
 *
 * <li>当前线程获取到锁；或
 *
 * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程。
 *
 * </ul>
 *
 * <p>如果当前线程获取到锁，则锁持有计数将被设置为一。
 *
 * <p>如果当前线程：
 *
 * <ul>
 *
 * <li>在进入此方法时已设置中断状态；或
 *
 * <li>在获取锁时被 {@linkplain Thread#interrupt 中断}，
 *
 * </ul>
 *
 * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
 *
 * <p>在此实现中，由于此方法是一个显式的中断点，因此优先响应中断，而不是正常或递归获取锁。
 *
 * @throws InterruptedException 如果当前线程被中断
 */
public void lockInterruptibly() throws InterruptedException {
    sync.acquireInterruptibly(1);
}

/**
 * 仅在调用时锁未被其他线程持有时获取锁。
 *
 * <p>如果锁未被其他线程持有，则立即获取锁并返回 {@code true}，将锁持有计数设置为一。即使此锁已设置为使用公平排序策略，调用 {@code tryLock()} <em>将</em> 立即获取锁（如果可用），无论是否有其他线程正在等待锁。
 * 这种“抢占”行为在某些情况下可能是有用的，尽管它破坏了公平性。如果您希望遵守此锁的公平设置，则使用
 * {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }，这几乎是等效的（它还检测中断）。
 *
 * <p>如果当前线程已经持有此锁，则将持有计数递增一并返回 {@code true}。
 *
 * <p>如果锁被其他线程持有，则此方法将立即返回 {@code false}。
 *
 * @return 如果锁是自由的并被当前线程获取，或锁已经被当前线程持有，则返回 {@code true}；否则返回 {@code false}
 */
public boolean tryLock() {
    return sync.nonfairTryAcquire(1);
}

/**
 * 如果锁未被其他线程持有且当前线程未被 {@linkplain Thread#interrupt 中断}，则在给定的等待时间内获取锁。
 *
 * <p>如果锁未被其他线程持有，则立即获取锁并返回 {@code true}，将锁持有计数设置为一。如果此锁已设置为使用公平排序策略，则不会获取可用的锁，如果其他线程正在等待锁。这与 {@link #tryLock()} 方法不同。如果您希望在公平锁上允许抢占的定时 {@code tryLock}，则将定时和非定时形式结合使用：
 *
 *  <pre> {@code
 * if (lock.tryLock() ||
 *     lock.tryLock(timeout, unit)) {
 *   ...
 * }}</pre>
 *
 * <p>如果当前线程已经持有此锁，则将持有计数递增一并返回 {@code true}。
 *
 * <p>如果锁被其他线程持有，则当前线程将被禁用以进行线程调度，并进入休眠状态，直到以下三种情况之一发生：
 *
 * <ul>
 *
 * <li>当前线程获取到锁；或
 *
 * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
 *
 * <li>指定的等待时间已过
 *
 * </ul>
 *
 * <p>如果获取到锁，则返回 {@code true}，并将锁持有计数设置为一。
 *
 * <p>如果当前线程：
 *
 * <ul>
 *
 * <li>在进入此方法时已设置中断状态；或
 *
 * <li>在获取锁时被 {@linkplain Thread#interrupt 中断}，
 *
 * </ul>
 * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
 *
 * <p>如果指定的等待时间已过，则返回 {@code false}。如果时间小于或等于零，则方法将不会等待。
 *
 * <p>在此实现中，由于此方法是一个显式的中断点，因此优先响应中断，而不是正常或递归获取锁，以及报告等待时间的流逝。
 *
 * @param timeout 等待锁的时间
 * @param unit 超时参数的时间单位
 * @return 如果锁是自由的并被当前线程获取，或锁已经被当前线程持有，则返回 {@code true}；如果等待时间已过而锁未被获取，则返回 {@code false}
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
     * <p>如果当前线程是此锁的持有者，则持有计数将递减。如果持有计数现在为零，则锁将被释放。如果当前线程不是此锁的持有者，则抛出 {@link IllegalMonitorStateException}。
     *
     * @throws IllegalMonitorStateException 如果当前线程没有持有此锁
     */
    public void unlock() {
        sync.release(1);
    }

    /**
     * 返回一个 {@link Condition} 实例，用于与此 {@link Lock} 实例一起使用。
     *
     * <p>返回的 {@link Condition} 实例支持与内置监视器锁一起使用的 {@link Object} 监视器方法（{@link
     * Object#wait() wait}，{@link Object#notify notify} 和 {@link
     * Object#notifyAll notifyAll}）相同的用法。
     *
     * <ul>
     *
     * <li>如果在调用任何 {@link Condition}
     * {@linkplain Condition#await() 等待} 或 {@linkplain
     * Condition#signal 信号} 方法时未持有此锁，则抛出 {@link
     * IllegalMonitorStateException}。
     *
     * <li>当调用条件 {@linkplain Condition#await() 等待}
     * 方法时，锁将被释放，并且在方法返回之前，锁将被重新获取，锁的持有计数将恢复到调用方法时的状态。
     *
     * <li>如果等待中的线程被 {@linkplain Thread#interrupt 中断}，
     * 则等待将终止，抛出 {@link
     * InterruptedException}，并且线程的中断状态将被清除。
     *
     * <li>等待线程按 FIFO 顺序被通知。
     *
     * <li>从等待方法返回的线程重新获取锁的顺序与线程最初获取锁的顺序相同，默认情况下未指定，但对于 <em>公平</em> 锁，优先考虑等待时间最长的线程。
     *
     * </ul>
     *
     * @return Condition 对象
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
     * 查询当前线程对此锁的持有次数。
     *
     * <p>线程对锁的每次锁定操作如果没有匹配的解锁操作，则线程对锁有一个持有。
     *
     * <p>持有计数信息通常仅用于测试和调试目的。例如，如果某段代码不应该在已经持有锁的情况下进入，我们可以断言这一点：
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *   public void m() {
     *     assert lock.getHoldCount() == 0;
     *     lock.lock();
     *     try {
     *       // ... 方法体
     *     } finally {
     *       lock.unlock();
     *     }
     *   }
     * }}</pre>
     *
     * @return 当前线程对此锁的持有次数，如果当前线程未持有此锁，则返回零
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * 查询当前线程是否持有此锁。
     *
     * <p>类似于内置监视器锁的 {@link Thread#holdsLock(Object)} 方法，此方法通常用于调试和测试。例如，一个方法应该仅在持有锁的情况下被调用，可以断言这一点：
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert lock.isHeldByCurrentThread();
     *       // ... 方法体
     *   }
     * }}</pre>
     *
     * <p>它也可以用于确保以非重入方式使用重入锁，例如：
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert !lock.isHeldByCurrentThread();
     *       lock.lock();
     *       try {
     *           // ... 方法体
     *       } finally {
     *           lock.unlock();
     *       }
     *   }
     * }}</pre>
     *
     * @return 如果当前线程持有此锁，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * 查询是否有任何线程持有此锁。此方法设计用于系统状态的监控，而不是用于同步控制。
     *
     * @return 如果有任何线程持有此锁，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
     * 如果此锁设置了公平性，则返回 {@code true}。
     *
     * @return 如果此锁设置了公平性，则返回 {@code true}
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 返回当前拥有此锁的线程，如果没有线程拥有此锁，则返回 {@code null}。当此方法由非拥有者线程调用时，返回值反映了当前锁状态的最佳近似值。例如，即使有线程尝试获取锁但尚未成功，所有者也可能暂时为 {@code null}。
     * 此方法设计用于辅助构建提供更广泛的锁监控功能的子类。
     *
     * @return 拥有者，如果没有线程拥有此锁，则返回 {@code null}
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * 查询是否有任何线程正在等待获取此锁。请注意，由于取消可能随时发生，因此 {@code true}
     * 返回值并不能保证任何其他线程将最终获取此锁。此方法主要用于系统状态的监控。
     *
     * @return 如果可能有其他线程正在等待获取锁，则返回 {@code true}
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }


                /**
     * 查询给定线程是否正在等待获取此锁。请注意，因为取消可能随时发生，所以
     * {@code true} 的返回值并不能保证此线程最终会获取此锁。此方法主要用于
     * 监控系统状态。
     *
     * @param thread 线程
     * @return 如果给定线程正在排队等待此锁，则返回 {@code true}
     * @throws NullPointerException 如果线程为 null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * 返回估计的等待获取此锁的线程数量。由于线程数量可能在方法遍历
     * 内部数据结构时动态变化，所以返回的值只是一个估计值。此方法主要用于
     * 监控系统状态，而不是用于同步控制。
     *
     * @return 等待此锁的线程的估计数量
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * 返回一个包含可能正在等待获取此锁的线程的集合。由于实际的线程集可能在
     * 构建此结果时动态变化，所以返回的集合只是一个尽力而为的估计。返回集合中的
     * 元素没有特定的顺序。此方法旨在帮助构建提供更广泛监控功能的子类。
     *
     * @return 线程的集合
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 查询是否有任何线程正在等待与此锁关联的给定条件。请注意，因为超时和
     * 中断可能随时发生，所以 {@code true} 的返回值并不能保证未来的 {@code signal}
     * 会唤醒任何线程。此方法主要用于监控系统状态。
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
     * 返回估计的等待与此锁关联的给定条件的线程数量。请注意，因为超时和
     * 中断可能随时发生，所以估计值仅作为实际等待者数量的上限。此方法主要用于
     * 监控系统状态，而不是用于同步控制。
     *
     * @param condition 条件
     * @return 等待线程的估计数量
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
     * 返回一个包含可能正在等待与此锁关联的给定条件的线程的集合。由于实际的线程集可能在
     * 构建此结果时动态变化，所以返回的集合只是一个尽力而为的估计。返回集合中的
     * 元素没有特定的顺序。此方法旨在帮助构建提供更广泛条件监控功能的子类。
     *
     * @param condition 条件
     * @return 线程的集合
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
     * 返回一个标识此锁及其锁状态的字符串。状态（在方括号中）包括字符串 {@code "Unlocked"}
     * 或字符串 {@code "Locked by"} 后跟
     * {@linkplain Thread#getName 拥有线程的名称}。
     *
     * @return 一个标识此锁及其锁状态的字符串
     */
    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
}
