/*
 * 版权所有 (c) 1997, 2017, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.ref;

import sun.misc.Cleaner;
import sun.misc.JavaLangRefAccess;
import sun.misc.SharedSecrets;

/**
 * 引用对象的抽象基类。此类定义了所有引用对象共有的操作。由于引用对象的实现与垃圾收集器紧密合作，因此此类不能直接被子类化。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public abstract class Reference<T> {

    /* 一个 Reference 实例处于四种可能的内部状态之一：
     *
     *     Active: 受垃圾收集器特殊处理。在收集器检测到引用对象的可达性变为适当状态后的一段时间内，它会将实例的状态更改为 Pending 或 Inactive，具体取决于实例创建时是否注册了队列。在前一种情况下，它还会将实例添加到待处理引用列表中。新创建的实例是 Active。
     *
     *     Pending: 待处理引用列表的元素，等待由引用处理线程入队。未注册的实例永远不会处于此状态。
     *
     *     Enqueued: 与实例创建时注册的队列的元素。当实例从其 ReferenceQueue 中移除时，它将变为 Inactive。未注册的实例永远不会处于此状态。
     *
     *     Inactive: 没有更多要做的。一旦实例变为 Inactive，其状态将永远不会改变。
     *
     * 状态编码在 queue 和 next 字段中，如下所示：
     *
     *     Active: queue = 实例注册的 ReferenceQueue，或 ReferenceQueue.NULL（如果未注册队列）；next = null。
     *
     *     Pending: queue = 实例注册的 ReferenceQueue；next = this
     *
     *     Enqueued: queue = ReferenceQueue.ENQUEUED；next = 队列中的下一个实例（或如果在列表末尾则为 this）。
     *
     *     Inactive: queue = ReferenceQueue.NULL；next = this。
     *
     * 使用此方案，收集器只需检查 next 字段即可确定引用实例是否需要特殊处理：如果 next 字段为 null，则实例是 Active；如果它为非 null，则收集器应正常处理该实例。
     *
     * 为了确保并发收集器可以发现活动的引用对象而不干扰可能对这些对象应用 enqueue() 方法的应用程序线程，收集器应通过 discovered 字段链接发现的对象。discovered 字段也用于链接待处理列表中的引用对象。
     */

    private T referent;         /* 由 GC 特别处理 */

    volatile ReferenceQueue<? super T> queue;

    /* 当 Active:   NULL
     *     pending:   this
     *    Enqueued:   队列中的下一个引用（或如果在列表末尾则为 this）
     *    Inactive:   this
     */
    @SuppressWarnings("rawtypes")
    volatile Reference next;

    /* 当 Active:   由 GC 维护的已发现引用列表中的下一个元素（或如果在列表末尾则为 this）
     *     pending:   待处理列表中的下一个元素（或如果在列表末尾则为 null）
     *   否则:   NULL
     */
    transient private Reference<T> discovered;  /* 由 VM 使用 */


    /* 用于与垃圾收集器同步的对象。收集器必须在每个收集周期开始时获取此锁。因此，任何持有此锁的代码都应尽快完成，不分配新对象，并避免调用用户代码。
     */
    static private class Lock { }
    private static Lock lock = new Lock();


    /* 等待入队的引用列表。收集器将引用添加到此列表，而引用处理线程则从中移除它们。此列表由上述锁对象保护。列表使用 discovered 字段链接其元素。
     */
    private static Reference<Object> pending = null;

    /* 高优先级线程，用于入队待处理的引用
     */
    private static class ReferenceHandler extends Thread {

        private static void ensureClassInitialized(Class<?> clazz) {
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw (Error) new NoClassDefFoundError(e.getMessage()).initCause(e);
            }
        }

        static {
            // 预加载并初始化 InterruptedException 和 Cleaner 类
            // 以避免在运行循环中因内存不足而加载/初始化它们时出现问题。
            ensureClassInitialized(InterruptedException.class);
            ensureClassInitialized(Cleaner.class);
        }

        ReferenceHandler(ThreadGroup g, String name) {
            super(g, name);
        }

        public void run() {
            while (true) {
                tryHandlePending(true);
            }
        }
    }

    /**
     * 尝试处理待处理的 {@link Reference}（如果有）。
     * 如果有 {@link Reference} 待处理，则返回 {@code true} 作为提示，表示可能还有其他待处理的 {@link Reference}；
     * 如果当前没有待处理的 {@link Reference}，则返回 {@code false}，程序可以执行其他有用的工作而不是循环。
     *
     * @param waitForNotify 如果 {@code true} 且没有待处理的 {@link Reference}，则等待 VM 通知或被中断；
     *                      如果 {@code false}，则在没有待处理的 {@link Reference} 时立即返回。
     * @return 如果有 {@link Reference} 待处理并已处理，或者我们等待通知并收到通知或线程在被通知前被中断，则返回 {@code true}；
     *         否则返回 {@code false}。
     */
    static boolean tryHandlePending(boolean waitForNotify) {
        Reference<Object> r;
        Cleaner c;
        try {
            synchronized (lock) {
                if (pending != null) {
                    r = pending;
                    // 'instanceof' 有时可能会抛出 OutOfMemoryError
                    // 因此在从 'pending' 链中取消链接 'r' 之前执行此操作...
                    c = r instanceof Cleaner ? (Cleaner) r : null;
                    // 从 'pending' 链中取消链接 'r'
                    pending = r.discovered;
                    r.discovered = null;
                } else {
                    // 等待锁可能会导致 OutOfMemoryError
                    // 因为它可能会尝试分配异常对象。
                    if (waitForNotify) {
                        lock.wait();
                    }
                    // 如果等待则重试
                    return waitForNotify;
                }
            }
        } catch (OutOfMemoryError x) {
            // 让其他线程获得 CPU 时间，希望它们释放一些活动引用
            // 并且 GC 会回收一些空间。
            // 还可以防止在 'r instanceof Cleaner' 上方持续抛出 OOME 的情况下 CPU 密集型自旋...
            Thread.yield();
            // 重试
            return true;
        } catch (InterruptedException x) {
            // 重试
            return true;
        }


                    // 快速路径用于清理
        if (c != null) {
            c.clean();
            return true;
        }

        ReferenceQueue<? super Object> q = r.queue;
        if (q != ReferenceQueue.NULL) q.enqueue(r);
        return true;
    }

    static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        Thread handler = new ReferenceHandler(tg, "Reference Handler");
        /* 如果有一个仅系统使用的优先级大于
         * MAX_PRIORITY，它将在这里使用
         */
        handler.setPriority(Thread.MAX_PRIORITY);
        handler.setDaemon(true);
        handler.start();

        // 在 SharedSecrets 中提供访问
        SharedSecrets.setJavaLangRefAccess(new JavaLangRefAccess() {
            @Override
            public boolean tryHandlePendingReference() {
                return tryHandlePending(false);
            }
        });
    }

    /* -- 引用访问器和设置器 -- */

    /**
     * 返回此引用对象的引用对象。如果此引用对象已被清除，无论是由程序还是垃圾收集器清除，
     * 则此方法返回 <code>null</code>。
     *
     * @return   该引用对象引用的对象，或者
     *           如果此引用对象已被清除，则返回 <code>null</code>
     */
    public T get() {
        return this.referent;
    }

    /**
     * 清除此引用对象。调用此方法不会导致此对象被入队。
     *
     * <p> 此方法仅由 Java 代码调用；当垃圾收集器清除引用时，它会直接清除，不会调用此方法。
     */
    public void clear() {
        this.referent = null;
    }


    /* -- 队列操作 -- */

    /**
     * 告诉此引用对象是否已被入队，无论是由程序还是垃圾收集器入队。如果此引用对象在创建时未注册到队列，
     * 则此方法将始终返回 <code>false</code>。
     *
     * @return   如果且仅当此引用对象已被入队时，返回 <code>true</code>
     */
    public boolean isEnqueued() {
        return (this.queue == ReferenceQueue.ENQUEUED);
    }

    /**
     * 将此引用对象添加到其注册的队列中，如果有。
     *
     * <p> 此方法仅由 Java 代码调用；当垃圾收集器入队引用时，它会直接入队，不会调用此方法。
     *
     * @return   如果此引用对象成功入队，则返回 <code>true</code>；如果它已被入队或在创建时未注册到队列，则返回 <code>false</code>
     */
    public boolean enqueue() {
        return this.queue.enqueue(this);
    }


    /* -- 构造函数 -- */

    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        this.referent = referent;
        this.queue = (queue == null) ? ReferenceQueue.NULL : queue;
    }

}
