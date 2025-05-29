
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.management;

import javax.management.openmbean.CompositeData;
import sun.management.ManagementFactoryHelper;
import sun.management.ThreadInfoCompositeData;
import static java.lang.Thread.State.*;

/**
 * 线程信息。 <tt>ThreadInfo</tt> 包含关于线程的信息，包括：
 * <h3>一般线程信息</h3>
 * <ul>
 *   <li>线程 ID。</li>
 *   <li>线程的名称。</li>
 * </ul>
 *
 * <h3>执行信息</h3>
 * <ul>
 *   <li>线程状态。</li>
 *   <li>线程因以下原因被阻塞的对象：
 *       <ul>
 *       <li>等待进入同步块/方法，或</li>
 *       <li>在 {@link Object#wait Object.wait} 方法中等待通知，或</li>
 *       <li>由于 {@link java.util.concurrent.locks.LockSupport#park
 *           LockSupport.park} 调用而暂停。</li>
 *       </ul>
 *   </li>
 *   <li>线程被阻塞的对象的所有者线程 ID。</li>
 *   <li>线程的堆栈跟踪。</li>
 *   <li>线程锁定的对象监视器列表。</li>
 *   <li>线程锁定的 <a href="LockInfo.html#OwnableSynchronizer">
 *       可拥有的同步器</a> 列表。</li>
 * </ul>
 *
 * <h4><a name="SyncStats">同步统计信息</a></h4>
 * <ul>
 *   <li>线程因同步或等待通知而被阻塞的次数。</li>
 *   <li>自 {@link ThreadMXBean#setThreadContentionMonitoringEnabled
 *       线程竞争监控} 启用以来，线程因同步或等待通知而被阻塞的累积时间。
 *       某些 Java 虚拟机实现可能不支持此功能。可以使用
 *       {@link ThreadMXBean#isThreadContentionMonitoringSupported()}
 *       方法来确定 Java 虚拟机是否支持此功能。</li>
 * </ul>
 *
 * <p>此类线程信息类设计用于系统监控，而不是用于同步控制。
 *
 * <h4>MXBean 映射</h4>
 * <tt>ThreadInfo</tt> 被映射为一个 {@link CompositeData CompositeData}
 * 属性，如 {@link #from from} 方法中指定的。
 *
 * @see ThreadMXBean#getThreadInfo
 * @see ThreadMXBean#dumpAllThreads
 *
 * @author  Mandy Chung
 * @since   1.5
 */

public class ThreadInfo {
    private String       threadName;
    private long         threadId;
    private long         blockedTime;
    private long         blockedCount;
    private long         waitedTime;
    private long         waitedCount;
    private LockInfo     lock;
    private String       lockName;
    private long         lockOwnerId;
    private String       lockOwnerName;
    private boolean      inNative;
    private boolean      suspended;
    private Thread.State threadState;
    private StackTraceElement[] stackTrace;
    private MonitorInfo[]       lockedMonitors;
    private LockInfo[]          lockedSynchronizers;

    private static MonitorInfo[] EMPTY_MONITORS = new MonitorInfo[0];
    private static LockInfo[] EMPTY_SYNCS = new LockInfo[0];

    /**
     * 由 JVM 创建的 ThreadInfo 构造函数
     *
     * @param t             线程
     * @param state         线程状态
     * @param lockObj       线程被阻塞的对象
     * @param lockOwner     持有锁的线程
     * @param blockedCount  进入锁时被阻塞的次数
     * @param blockedTime   进入锁时被阻塞的近似时间
     * @param waitedCount   在锁上等待的次数
     * @param waitedTime    在锁上等待的近似时间
     * @param stackTrace    线程堆栈跟踪
     */
    private ThreadInfo(Thread t, int state, Object lockObj, Thread lockOwner,
                       long blockedCount, long blockedTime,
                       long waitedCount, long waitedTime,
                       StackTraceElement[] stackTrace) {
        initialize(t, state, lockObj, lockOwner,
                   blockedCount, blockedTime,
                   waitedCount, waitedTime, stackTrace,
                   EMPTY_MONITORS, EMPTY_SYNCS);
    }

    /**
     * 由 JVM 创建的 ThreadInfo 构造函数
     * 用于 {@link ThreadMXBean#getThreadInfo(long[],boolean,boolean)}
     * 和 {@link ThreadMXBean#dumpAllThreads}
     *
     * @param t             线程
     * @param state         线程状态
     * @param lockObj       线程被阻塞的对象
     * @param lockOwner     持有锁的线程
     * @param blockedCount  进入锁时被阻塞的次数
     * @param blockedTime   进入锁时被阻塞的近似时间
     * @param waitedCount   在锁上等待的次数
     * @param waitedTime    在锁上等待的近似时间
     * @param stackTrace    线程堆栈跟踪
     * @param monitors      锁定的监视器列表
     * @param stackDepths   堆栈深度列表
     * @param synchronizers 锁定的同步器列表
     */
    private ThreadInfo(Thread t, int state, Object lockObj, Thread lockOwner,
                       long blockedCount, long blockedTime,
                       long waitedCount, long waitedTime,
                       StackTraceElement[] stackTrace,
                       Object[] monitors,
                       int[] stackDepths,
                       Object[] synchronizers) {
        int numMonitors = (monitors == null ? 0 : monitors.length);
        MonitorInfo[] lockedMonitors;
        if (numMonitors == 0) {
            lockedMonitors = EMPTY_MONITORS;
        } else {
            lockedMonitors = new MonitorInfo[numMonitors];
            for (int i = 0; i < numMonitors; i++) {
                Object lock = monitors[i];
                String className = lock.getClass().getName();
                int identityHashCode = System.identityHashCode(lock);
                int depth = stackDepths[i];
                StackTraceElement ste = (depth >= 0 ? stackTrace[depth]
                                                    : null);
                lockedMonitors[i] = new MonitorInfo(className,
                                                    identityHashCode,
                                                    depth,
                                                    ste);
            }
        }


                    int numSyncs = (synchronizers == null ? 0 : synchronizers.length);
        LockInfo[] lockedSynchronizers;
        if (numSyncs == 0) {
            lockedSynchronizers = EMPTY_SYNCS;
        } else {
            lockedSynchronizers = new LockInfo[numSyncs];
            for (int i = 0; i < numSyncs; i++) {
                Object lock = synchronizers[i];
                String className = lock.getClass().getName();
                int identityHashCode = System.identityHashCode(lock);
                lockedSynchronizers[i] = new LockInfo(className,
                                                      identityHashCode);
            }
        }

        initialize(t, state, lockObj, lockOwner,
                   blockedCount, blockedTime,
                   waitedCount, waitedTime, stackTrace,
                   lockedMonitors, lockedSynchronizers);
    }

    /**
     * 初始化 ThreadInfo 对象
     *
     * @param t             线程
     * @param state         线程状态
     * @param lockObj       线程被阻塞的对象
     * @param lockOwner     持有锁的线程
     * @param blockedCount  尝试进入锁时被阻塞的次数
     * @param blockedTime   尝试进入锁时被阻塞的近似时间
     * @param waitedCount   在锁上等待的次数
     * @param waitedTime    在锁上等待的近似时间
     * @param stackTrace    线程堆栈跟踪
     * @param lockedMonitors 被锁定的监视器列表
     * @param lockedSynchronizers 被锁定的同步器列表
     */
    private void initialize(Thread t, int state, Object lockObj, Thread lockOwner,
                            long blockedCount, long blockedTime,
                            long waitedCount, long waitedTime,
                            StackTraceElement[] stackTrace,
                            MonitorInfo[] lockedMonitors,
                            LockInfo[] lockedSynchronizers) {
        this.threadId = t.getId();
        this.threadName = t.getName();
        this.threadState = ManagementFactoryHelper.toThreadState(state);
        this.suspended = ManagementFactoryHelper.isThreadSuspended(state);
        this.inNative = ManagementFactoryHelper.isThreadRunningNative(state);
        this.blockedCount = blockedCount;
        this.blockedTime = blockedTime;
        this.waitedCount = waitedCount;
        this.waitedTime = waitedTime;

        if (lockObj == null) {
            this.lock = null;
            this.lockName = null;
        } else {
            this.lock = new LockInfo(lockObj);
            this.lockName =
                lock.getClassName() + '@' +
                    Integer.toHexString(lock.getIdentityHashCode());
        }
        if (lockOwner == null) {
            this.lockOwnerId = -1;
            this.lockOwnerName = null;
        } else {
            this.lockOwnerId = lockOwner.getId();
            this.lockOwnerName = lockOwner.getName();
        }
        if (stackTrace == null) {
            this.stackTrace = NO_STACK_TRACE;
        } else {
            this.stackTrace = stackTrace;
        }
        this.lockedMonitors = lockedMonitors;
        this.lockedSynchronizers = lockedSynchronizers;
    }

    /*
     * 从 {@link CompositeData CompositeData} 构造一个 <tt>ThreadInfo</tt> 对象。
     */
    private ThreadInfo(CompositeData cd) {
        ThreadInfoCompositeData ticd = ThreadInfoCompositeData.getInstance(cd);

        threadId = ticd.threadId();
        threadName = ticd.threadName();
        blockedTime = ticd.blockedTime();
        blockedCount = ticd.blockedCount();
        waitedTime = ticd.waitedTime();
        waitedCount = ticd.waitedCount();
        lockName = ticd.lockName();
        lockOwnerId = ticd.lockOwnerId();
        lockOwnerName = ticd.lockOwnerName();
        threadState = ticd.threadState();
        suspended = ticd.suspended();
        inNative = ticd.inNative();
        stackTrace = ticd.stackTrace();

        // 6.0 属性
        if (ticd.isCurrentVersion()) {
            lock = ticd.lockInfo();
            lockedMonitors = ticd.lockedMonitors();
            lockedSynchronizers = ticd.lockedSynchronizers();
        } else {
            // lockInfo 是 1.6 版本的 ThreadInfo 中新增的属性
            // 如果 cd 是 5.0 版本，从 lockName 值构造 LockInfo 对象。
            if (lockName != null) {
                String result[] = lockName.split("@");
                if (result.length == 2) {
                    int identityHashCode = Integer.parseInt(result[1], 16);
                    lock = new LockInfo(result[0], identityHashCode);
                } else {
                    assert result.length == 2;
                    lock = null;
                }
            } else {
                lock = null;
            }
            lockedMonitors = EMPTY_MONITORS;
            lockedSynchronizers = EMPTY_SYNCS;
        }
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程的 ID。
     *
     * @return 关联线程的 ID。
     */
    public long getThreadId() {
        return threadId;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程的名称。
     *
     * @return 关联线程的名称。
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程的状态。
     *
     * @return 关联线程的 <tt>Thread.State</tt>。
     */
    public Thread.State getThreadState() {
         return threadState;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程自线程竞争监控启用以来尝试进入或重新进入监视器的近似累积经过时间（以毫秒为单位）。
     * 即，自线程竞争监控上次启用以来，线程处于 {@link java.lang.Thread.State#BLOCKED BLOCKED} 状态的总累积时间。
     * 如果线程竞争监控已禁用，此方法返回 <tt>-1</tt>。
     *
     * <p>Java 虚拟机可能会使用高分辨率计时器来测量时间。此统计信息在重新启用线程竞争监控时重置。
     *
     * @return 线程进入 <tt>BLOCKED</tt> 状态的近似累积经过时间（以毫秒为单位）；
     * <tt>-1</tt> 表示线程竞争监控已禁用。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java 虚拟机不支持此操作。
     *
     * @see ThreadMXBean#isThreadContentionMonitoringSupported
     * @see ThreadMXBean#setThreadContentionMonitoringEnabled
     */
    public long getBlockedTime() {
        return blockedTime;
    }


                /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 阻塞以进入或重新进入监视器的总次数。
     * 即线程处于
     * {@link java.lang.Thread.State#BLOCKED BLOCKED} 状态的次数。
     *
     * @return 线程进入 <tt>BLOCKED</tt> 状态的总次数。
     */
    public long getBlockedCount() {
        return blockedCount;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 自线程竞争监控启用以来等待通知的近似累积经过时间（以毫秒为单位）。
     * 即自线程竞争监控启用以来，线程处于
     * {@link java.lang.Thread.State#WAITING WAITING}
     * 或 {@link java.lang.Thread.State#TIMED_WAITING TIMED_WAITING} 状态的总累积时间。
     * 如果线程竞争监控已禁用，此方法返回 <tt>-1</tt>。
     *
     * <p>Java 虚拟机可能会使用高分辨率计时器来测量时间。当
     * 重新启用线程竞争监控时，此统计信息将重置。
     *
     * @return 线程处于 <tt>WAITING</tt> 或
     * <tt>TIMED_WAITING</tt> 状态的近似累积经过时间（以毫秒为单位）；
     * 如果线程竞争监控已禁用，则返回 <tt>-1</tt>。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持此操作。
     *
     * @see ThreadMXBean#isThreadContentionMonitoringSupported
     * @see ThreadMXBean#setThreadContentionMonitoringEnabled
     */
    public long getWaitedTime() {
        return waitedTime;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 等待通知的总次数。
     * 即线程处于
     * {@link java.lang.Thread.State#WAITING WAITING}
     * 或 {@link java.lang.Thread.State#TIMED_WAITING TIMED_WAITING} 状态的次数。
     *
     * @return 线程处于 <tt>WAITING</tt> 或 <tt>TIMED_WAITING</tt> 状态的总次数。
     */
    public long getWaitedCount() {
        return waitedCount;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 阻塞等待的对象的 <tt>LockInfo</tt>。
     * 线程可能因以下原因之一而阻塞等待：
     * <ul>
     * <li>获取对象监视器以进入或重新进入同步块/方法。
     *     <br>线程处于 {@link java.lang.Thread.State#BLOCKED BLOCKED}
     *     状态，等待进入 <tt>synchronized</tt> 语句或方法。
     *     <p></li>
     * <li>由其他线程通知对象监视器。
     *     <br>线程因调用 {@link Object#wait Object.wait} 方法而处于
     *     {@link java.lang.Thread.State#WAITING WAITING}
     *     或 {@link java.lang.Thread.State#TIMED_WAITING TIMED_WAITING} 状态。
     *     <p></li>
     * <li>负责线程停放的同步对象。
     *     <br>线程因调用
     *     {@link java.util.concurrent.locks.LockSupport#park(Object)
     *     LockSupport.park} 方法而处于
     *     {@link java.lang.Thread.State#WAITING WAITING}
     *     或 {@link java.lang.Thread.State#TIMED_WAITING TIMED_WAITING} 状态。同步对象
     *     是从
     *     {@link java.util.concurrent.locks.LockSupport#getBlocker
     *     LockSupport.getBlocker} 方法返回的对象。通常它是一个
     *     <a href="LockInfo.html#OwnableSynchronizer"> 可拥有的同步器</a>
     *     或一个 {@link java.util.concurrent.locks.Condition Condition}。</li>
     * </ul>
     *
     * <p>如果线程不在上述任何条件下，此方法返回 <tt>null</tt>。
     *
     * @return 线程阻塞等待的对象的 <tt>LockInfo</tt>，如果线程没有阻塞等待任何对象，则返回 <tt>null</tt>。
     * @since 1.6
     */
    public LockInfo getLockInfo() {
        return lock;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 阻塞等待的对象的 {@link LockInfo#toString 字符串表示}。
     * 此方法等同于调用：
     * <blockquote>
     * <pre>
     * getLockInfo().toString()
     * </pre></blockquote>
     *
     * <p>如果此线程没有阻塞等待任何对象或对象未被任何线程拥有，此方法将返回 <tt>null</tt>。
     *
     * @return 线程阻塞等待的对象的字符串表示，如果线程没有阻塞等待任何对象或对象未被任何线程拥有，则返回 <tt>null</tt>。
     *
     * @see #getLockInfo
     */
    public String getLockName() {
        return lockName;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 阻塞等待的对象的所有者线程的 ID。
     * 如果此线程没有阻塞等待任何对象或对象未被任何线程拥有，此方法将返回 <tt>-1</tt>。
     *
     * @return 此线程阻塞等待的对象的所有者线程的线程 ID；
     * 如果此线程没有阻塞或对象未被任何线程拥有，则返回 <tt>-1</tt>。
     *
     * @see #getLockInfo
     */
    public long getLockOwnerId() {
        return lockOwnerId;
    }

    /**
     * 返回与此 <tt>ThreadInfo</tt> 关联的线程
     * 阻塞等待的对象的所有者线程的名称。
     * 如果此线程没有阻塞等待任何对象或对象未被任何线程拥有，此方法将返回 <tt>null</tt>。
     *
     * @return 此线程阻塞等待的对象的所有者线程的名称；
     * 如果此线程没有阻塞或对象未被任何线程拥有，则返回 <tt>null</tt>。
     *
     * @see #getLockInfo
     */
    public String getLockOwnerName() {
        return lockOwnerName;
    }


/**
 * 返回与此 <tt>ThreadInfo</tt> 关联的线程的堆栈跟踪。
 * 如果没有为此线程信息请求堆栈跟踪，此方法将返回一个零长度的数组。
 * 如果返回的数组长度非零，则数组的第一个元素代表堆栈的顶部，即序列中最新的方法调用。
 * 数组的最后一个元素代表堆栈的底部，即序列中最不新的方法调用。
 *
 * <p>在某些情况下，某些 Java 虚拟机可能会从堆栈跟踪中省略一个或多个堆栈帧。
 * 在极端情况下，如果虚拟机没有与此 <tt>ThreadInfo</tt> 关联的线程的堆栈跟踪信息，
 * 则允许此方法返回一个零长度的数组。
 *
 * @return 线程的 <tt>StackTraceElement</tt> 对象数组。
 */
public StackTraceElement[] getStackTrace() {
    return stackTrace;
}

/**
 * 测试与此 <tt>ThreadInfo</tt> 关联的线程是否已挂起。
 * 如果调用了 {@link Thread#suspend}，此方法将返回 <tt>true</tt>。
 *
 * @return 如果线程已挂起，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
 */
public boolean isSuspended() {
    return suspended;
}

/**
 * 测试与此 <tt>ThreadInfo</tt> 关联的线程是否通过 Java 本地接口 (JNI) 执行本地代码。
 * JNI 本地代码不包括虚拟机支持代码或虚拟机生成的编译本地代码。
 *
 * @return 如果线程正在执行本地代码，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
 */
public boolean isInNative() {
    return inNative;
}

/**
 * 返回此线程信息的字符串表示形式。
 * 该字符串的格式取决于实现。
 * 返回的字符串通常包括 {@linkplain #getThreadName 线程名称}、
 * {@linkplain #getThreadId 线程 ID}、其 {@linkplain #getThreadState 状态}，
 * 以及如果有，则包括 {@linkplain #getStackTrace 堆栈跟踪}。
 *
 * @return 此线程信息的字符串表示形式。
 */
public String toString() {
    StringBuilder sb = new StringBuilder("\"" + getThreadName() + "\"" +
                                         " Id=" + getThreadId() + " " +
                                         getThreadState());
    if (getLockName() != null) {
        sb.append(" on " + getLockName());
    }
    if (getLockOwnerName() != null) {
        sb.append(" owned by \"" + getLockOwnerName() +
                  "\" Id=" + getLockOwnerId());
    }
    if (isSuspended()) {
        sb.append(" (suspended)");
    }
    if (isInNative()) {
        sb.append(" (in native)");
    }
    sb.append('\n');
    int i = 0;
    for (; i < stackTrace.length && i < MAX_FRAMES; i++) {
        StackTraceElement ste = stackTrace[i];
        sb.append("\tat " + ste.toString());
        sb.append('\n');
        if (i == 0 && getLockInfo() != null) {
            Thread.State ts = getThreadState();
            switch (ts) {
                case BLOCKED:
                    sb.append("\t-  blocked on " + getLockInfo());
                    sb.append('\n');
                    break;
                case WAITING:
                    sb.append("\t-  waiting on " + getLockInfo());
                    sb.append('\n');
                    break;
                case TIMED_WAITING:
                    sb.append("\t-  waiting on " + getLockInfo());
                    sb.append('\n');
                    break;
                default:
            }
        }

        for (MonitorInfo mi : lockedMonitors) {
            if (mi.getLockedStackDepth() == i) {
                sb.append("\t-  locked " + mi);
                sb.append('\n');
            }
        }
    }
    if (i < stackTrace.length) {
        sb.append("\t...");
        sb.append('\n');
    }

    LockInfo[] locks = getLockedSynchronizers();
    if (locks.length > 0) {
        sb.append("\n\tNumber of locked synchronizers = " + locks.length);
        sb.append('\n');
        for (LockInfo li : locks) {
            sb.append("\t- " + li);
            sb.append('\n');
        }
    }
    sb.append('\n');
    return sb.toString();
}
private static final int MAX_FRAMES = 8;

/**
 * 返回由给定 <tt>CompositeData</tt> 表示的 <tt>ThreadInfo</tt> 对象。
 * 给定的 <tt>CompositeData</tt> 必须包含以下属性，除非另有说明：
 * <blockquote>
 * <table border summary="给定 CompositeData 包含的属性及其类型">
 * <tr>
 *   <th align=left>属性名称</th>
 *   <th align=left>类型</th>
 * </tr>
 * <tr>
 *   <td>threadId</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>threadName</td>
 *   <td><tt>java.lang.String</tt></td>
 * </tr>
 * <tr>
 *   <td>threadState</td>
 *   <td><tt>java.lang.String</tt></td>
 * </tr>
 * <tr>
 *   <td>suspended</td>
 *   <td><tt>java.lang.Boolean</tt></td>
 * </tr>
 * <tr>
 *   <td>inNative</td>
 *   <td><tt>java.lang.Boolean</tt></td>
 * </tr>
 * <tr>
 *   <td>blockedCount</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>blockedTime</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>waitedCount</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>waitedTime</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>lockInfo</td>
 *   <td><tt>javax.management.openmbean.CompositeData</tt>
 *       - 由 {@link LockInfo#from} 方法指定的 {@link LockInfo} 的映射类型。
 *       <p>
 *       如果 <tt>cd</tt> 不包含此属性，则 <tt>LockInfo</tt> 对象将从 <tt>lockName</tt> 属性的值构造。 </td>
 * </tr>
 * <tr>
 *   <td>lockName</td>
 *   <td><tt>java.lang.String</tt></td>
 * </tr>
 * <tr>
 *   <td>lockOwnerId</td>
 *   <td><tt>java.lang.Long</tt></td>
 * </tr>
 * <tr>
 *   <td>lockOwnerName</td>
 *   <td><tt>java.lang.String</tt></td>
 * </tr>
 * <tr>
 *   <td><a name="StackTrace">stackTrace</a></td>
 *   <td><tt>javax.management.openmbean.CompositeData[]</tt>
 *       <p>
 *       每个元素是一个表示 StackTraceElement 的 <tt>CompositeData</tt>，包含以下属性：
 *       <blockquote>
 *       <table cellspacing=1 cellpadding=0 summary="给定 CompositeData 包含的属性及其类型">
 *       <tr>
 *         <th align=left>属性名称</th>
 *         <th align=left>类型</th>
 *       </tr>
 *       <tr>
 *         <td>className</td>
 *         <td><tt>java.lang.String</tt></td>
 *       </tr>
 *       <tr>
 *         <td>methodName</td>
 *         <td><tt>java.lang.String</tt></td>
 *       </tr>
 *       <tr>
 *         <td>fileName</td>
 *         <td><tt>java.lang.String</tt></td>
 *       </tr>
 *       <tr>
 *         <td>lineNumber</td>
 *         <td><tt>java.lang.Integer</tt></td>
 *       </tr>
 *       <tr>
 *         <td>nativeMethod</td>
 *         <td><tt>java.lang.Boolean</tt></td>
 *       </tr>
 *       </table>
 *       </blockquote>
 *   </td>
 * </tr>
 * <tr>
 *   <td>lockedMonitors</td>
 *   <td><tt>javax.management.openmbean.CompositeData[]</tt>
 *       - 元素类型为 {@link MonitorInfo#from Monitor.from} 方法指定的 {@link MonitorInfo} 的映射类型。
 *       <p>
 *       如果 <tt>cd</tt> 不包含此属性，则此属性将设置为空数组。 </td>
 * </tr>
 * <tr>
 *   <td>lockedSynchronizers</td>
 *   <td><tt>javax.management.openmbean.CompositeData[]</tt>
 *       - 元素类型为 {@link LockInfo#from} 方法指定的 {@link LockInfo} 的映射类型。
 *       <p>
 *       如果 <tt>cd</tt> 不包含此属性，则此属性将设置为空数组。 </td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @param cd 表示 <tt>ThreadInfo</tt> 的 <tt>CompositeData</tt>
 *
 * @throws IllegalArgumentException 如果 <tt>cd</tt> 不表示具有上述属性的 <tt>ThreadInfo</tt>。
 *
 * @return 如果 <tt>cd</tt> 不为 <tt>null</tt>，则返回由 <tt>cd</tt> 表示的 <tt>ThreadInfo</tt> 对象；
 *         否则返回 <tt>null</tt>。
 */
public static ThreadInfo from(CompositeData cd) {
    if (cd == null) {
        return null;
    }


                    if (cd instanceof ThreadInfoCompositeData) {
            return ((ThreadInfoCompositeData) cd).getThreadInfo();
        } else {
            return new ThreadInfo(cd);
        }
    }

    /**
     * 返回一个 {@link MonitorInfo} 对象数组，每个对象表示与此 <tt>ThreadInfo</tt>
     * 关联的线程当前锁定的对象监视器。
     * 如果没有为此线程信息请求锁定的监视器或线程没有锁定任何监视器，
     * 此方法将返回一个零长度的数组。
     *
     * @return 一个 <tt>MonitorInfo</tt> 对象数组，表示线程锁定的对象监视器。
     *
     * @since 1.6
     */
    public MonitorInfo[] getLockedMonitors() {
        return lockedMonitors;
    }

    /**
     * 返回一个 {@link LockInfo} 对象数组，每个对象表示一个 <a href="LockInfo.html#OwnableSynchronizer">可拥有
     * 同步器</a>，当前由与此 <tt>ThreadInfo</tt> 关联的线程锁定。如果未为此线程信息请求锁定的同步器或线程
     * 没有锁定任何同步器，此方法将返回一个零长度的数组。
     *
     * @return 一个 <tt>LockInfo</tt> 对象数组，表示线程锁定的可拥有同步器。
     *
     * @since 1.6
     */
    public LockInfo[] getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    private static final StackTraceElement[] NO_STACK_TRACE =
        new StackTraceElement[0];
}
