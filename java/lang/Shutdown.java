
/*
 * 版权所有 (c) 1999, 2020, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;


/**
 * 包私有工具类，包含管理虚拟机关闭序列的数据结构和逻辑。
 *
 * @author   Mark Reinhold
 * @since    1.3
 */

class Shutdown {

    /* 关闭状态 */
    private static final int RUNNING = 0;
    private static final int HOOKS = 1;
    private static final int FINALIZERS = 2;
    private static int state = RUNNING;

    /* 在退出时是否运行所有终结器？ */
    private static boolean runFinalizersOnExit = false;

    // 系统关闭挂钩注册到预定义的槽位。
    // 关闭挂钩列表如下：
    // (0) 控制台恢复挂钩
    // (1) 应用程序挂钩
    // (2) 删除退出挂钩
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final Runnable[] hooks = new Runnable[MAX_SYSTEM_HOOKS];

    // 当前运行的关闭挂钩在挂钩数组中的索引
    private static int currentRunningHook = 0;

    /* 前面的静态字段由该锁保护 */
    private static class Lock { };
    private static Object lock = new Lock();

    /* 本地 halt 方法的锁对象 */
    private static Object haltLock = new Lock();

    /* 由 Runtime.runFinalizersOnExit 调用 */
    static void setRunFinalizersOnExit(boolean run) {
        synchronized (lock) {
            runFinalizersOnExit = run;
        }
    }


    /**
     * 添加新的关闭挂钩。检查关闭状态和挂钩本身，但不执行任何安全检查。
     *
     * registerShutdownInProgress 参数应为 false，除非注册 DeleteOnExitHook，因为应用程序关闭挂钩可能
     * 在关闭过程中添加第一个文件到删除退出列表。
     *
     * @params slot  关闭挂钩数组中的槽位，其元素将在关闭时按顺序调用
     * @params registerShutdownInProgress 如果关闭正在进行中，是否允许注册挂钩
     * @params hook  要注册的挂钩
     *
     * @throw IllegalStateException
     *        如果 registerShutdownInProgress 为 false 且关闭正在进行中；或
     *        如果 registerShutdownInProgress 为 true 且关闭过程已超过给定槽位
     */
    static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        synchronized (lock) {
            if (hooks[slot] != null)
                throw new InternalError("Shutdown hook at slot " + slot + " already registered");

            if (!registerShutdownInProgress) {
                if (state > RUNNING)
                    throw new IllegalStateException("Shutdown in progress");
            } else {
                if (state > HOOKS || (state == HOOKS && slot <= currentRunningHook))
                    throw new IllegalStateException("Shutdown in progress");
            }

            hooks[slot] = hook;
        }
    }

    /* 运行所有已注册的关闭挂钩
     */
    private static void runHooks() {
        for (int i=0; i < MAX_SYSTEM_HOOKS; i++) {
            try {
                Runnable hook;
                synchronized (lock) {
                    // 获取锁以确保在关闭过程中注册的挂钩在此处可见。
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook != null) hook.run();
            } catch(Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath td = (ThreadDeath)t;
                    throw td;
                }
            }
        }
    }

    /* 通知 VM 是时候停止了。 */
    static native void beforeHalt();

    /* halt 方法在 halt 锁上同步
     * 以避免删除关闭文件列表的损坏。
     * 它调用真正的本地 halt 方法。
     */
    static void halt(int status) {
        synchronized (haltLock) {
            halt0(status);
        }
    }

    static native void halt0(int status);

    /* 用于调用 java.lang.ref.Finalizer.runAllFinalizers 的通道 */
    private static native void runAllFinalizers();


    /* 实际的关闭序列定义在此。
     *
     * 如果没有 runFinalizersOnExit，这将非常简单——我们只需运行挂钩然后停止。相反，我们需要跟踪
     * 我们是在运行挂钩还是终结器。在后一种情况下，终结器可以调用 exit(1) 以立即终止，而在前一种情况下
     * 任何进一步的 exit(n) 调用（对于任何 n）都会停滞。请注意，如果启用了退出时的终结器，它们仅在
     * 通过 exit(0) 初始化关闭时运行；它们永远不会在 exit(n) 为 n != 0 或响应 SIGINT、SIGTERM 等时运行。
     */
    private static void sequence() {
        synchronized (lock) {
            /* 防止守护线程在 DestroyJavaVM 初始化关闭序列后调用 exit
             */
            if (state != HOOKS) return;
        }
        runHooks();
        boolean rfoe;
        synchronized (lock) {
            state = FINALIZERS;
            rfoe = runFinalizersOnExit;
        }
        if (rfoe) runAllFinalizers();
    }


    /* 由 Runtime.exit 调用，它执行所有安全检查。
     * 也由系统提供的终止事件的处理程序调用，
     * 应该传递非零状态码。
     */
    static void exit(int status) {
        boolean runMoreFinalizers = false;
        synchronized (lock) {
            if (status != 0) runFinalizersOnExit = false;
            switch (state) {
            case RUNNING:       /* 初始化关闭 */
                state = HOOKS;
                break;
            case HOOKS:         /* 停滞并停止 */
                break;
            case FINALIZERS:
                if (status != 0) {
                    /* 在非零状态时立即停止 */
                    halt(status);
                } else {
                    /* 与旧行为兼容：
                     * 运行更多终结器然后停止
                     */
                    runMoreFinalizers = runFinalizersOnExit;
                }
                break;
            }
        }
        if (runMoreFinalizers) {
            runAllFinalizers();
            halt(status);
        }
        synchronized (Shutdown.class) {
            /* 在类对象上同步，导致任何尝试初始化关闭的其他线程
             * 无限期停滞
             */
            beforeHalt();
            sequence();
            halt(status);
        }
    }

    /* 由 JNI DestroyJavaVM 过程在最后一个非守护线程结束时调用。与 exit 方法不同，
     * 此方法实际上不会停止 VM。
     */
    static void shutdown() {
        synchronized (lock) {
            switch (state) {
            case RUNNING:       /* 启动关闭过程 */
                state = HOOKS;
                break;
            case HOOKS:         /* 暂停然后返回 */
            case FINALIZERS:
                break;
            }
        }
        synchronized (Shutdown.class) {
            sequence();
        }
    }

}
