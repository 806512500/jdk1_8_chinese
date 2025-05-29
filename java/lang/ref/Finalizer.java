
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

import java.security.PrivilegedAction;
import java.security.AccessController;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import sun.misc.VM;

final class Finalizer extends FinalReference<Object> { /* 包私有；必须与 Reference 类在同一包中 */

    private static ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private static Finalizer unfinalized = null;
    private static final Object lock = new Object();

    private Finalizer
        next = null,
        prev = null;

    private boolean hasBeenFinalized() {
        return (next == this);
    }

    private void add() {
        synchronized (lock) {
            if (unfinalized != null) {
                this.next = unfinalized;
                unfinalized.prev = this;
            }
            unfinalized = this;
        }
    }

    private void remove() {
        synchronized (lock) {
            if (unfinalized == this) {
                if (this.next != null) {
                    unfinalized = this.next;
                } else {
                    unfinalized = this.prev;
                }
            }
            if (this.next != null) {
                this.next.prev = this.prev;
            }
            if (this.prev != null) {
                this.prev.next = this.next;
            }
            this.next = this;   /* 表示此对象已被终结 */
            this.prev = this;
        }
    }

    private Finalizer(Object finalizee) {
        super(finalizee, queue);
        add();
    }

    static ReferenceQueue<Object> getQueue() {
        return queue;
    }

    /* 由 VM 调用 */
    static void register(Object finalizee) {
        new Finalizer(finalizee);
    }

    private void runFinalizer(JavaLangAccess jla) {
        synchronized (this) {
            if (hasBeenFinalized()) return;
            remove();
        }
        try {
            Object finalizee = this.get();
            if (finalizee != null && !(finalizee instanceof java.lang.Enum)) {
                jla.invokeFinalize(finalizee);

                /* 清除包含此变量的堆栈槽，以减少保守垃圾收集器误保留的机会 */
                finalizee = null;
            }
        } catch (Throwable x) { }
        super.clear();
    }

    /* 在系统线程组中为给定的 Runnable 创建一个特权的二级终结器线程，并等待其完成。

       该方法由 runFinalization 和 runFinalizersOnExit 两个方法使用。
       前者方法调用所有待处理的终结器，而后者方法在启用退出时终结化的情况下调用所有未调用的终结器。

       这两个方法可以通过将它们的工作卸载到常规终结器线程并等待该线程完成来实现。
       然而，创建一个新线程的优势在于，它使这些方法的调用者与停滞或死锁的终结器线程隔离。
     */
    private static void forkSecondaryFinalizer(final Runnable proc) {
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup tg = Thread.currentThread().getThreadGroup();
                    for (ThreadGroup tgn = tg;
                         tgn != null;
                         tg = tgn, tgn = tg.getParent());
                    Thread sft = new Thread(tg, proc, "Secondary finalizer");
                    sft.start();
                    try {
                        sft.join();
                    } catch (InterruptedException x) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }});
    }

    /* 由 Runtime.runFinalization() 调用 */
    static void runFinalization() {
        if (!VM.isBooted()) {
            return;
        }

        forkSecondaryFinalizer(new Runnable() {
            private volatile boolean running;
            public void run() {
                // 防止递归调用 run()
                if (running)
                    return;
                final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
                running = true;
                for (;;) {
                    Finalizer f = (Finalizer)queue.poll();
                    if (f == null) break;
                    f.runFinalizer(jla);
                }
            }
        });
    }

    /* 由 java.lang.Shutdown 调用 */
    static void runAllFinalizers() {
        if (!VM.isBooted()) {
            return;
        }

        forkSecondaryFinalizer(new Runnable() {
            private volatile boolean running;
            public void run() {
                // 防止递归调用 run()
                if (running)
                    return;
                final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
                running = true;
                for (;;) {
                    Finalizer f;
                    synchronized (lock) {
                        f = unfinalized;
                        if (f == null) break;
                        unfinalized = f.next;
                    }
                    f.runFinalizer(jla);
                }}});
    }

    private static class FinalizerThread extends Thread {
        private volatile boolean running;
        FinalizerThread(ThreadGroup g) {
            super(g, "Finalizer");
        }
        public void run() {
            // 防止递归调用 run()
            if (running)
                return;

            // 终结器线程在调用 System.initializeSystemClass 之前启动。等待 JavaLangAccess 可用
            while (!VM.isBooted()) {
                // 等待 VM 完成初始化
                try {
                    VM.awaitBooted();
                } catch (InterruptedException x) {
                    // 忽略并继续
                }
            }
            final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
            running = true;
            for (;;) {
                try {
                    Finalizer f = (Finalizer)queue.remove();
                    f.runFinalizer(jla);
                } catch (InterruptedException x) {
                    // 忽略并继续
                }
            }
        }
    }


                static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        Thread finalizer = new FinalizerThread(tg);
        finalizer.setPriority(Thread.MAX_PRIORITY - 2);
        finalizer.setDaemon(true);
        finalizer.start();
    }

}
