/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

/**
 * 由 {@link ForkJoinPool} 管理的线程，执行 {@link ForkJoinTask}。
 * 该类仅为了添加功能而可被子类化——没有可重写的方法涉及调度或执行。但是，您可以重写围绕主任务处理循环的初始化和终止方法。
 * 如果您创建了这样的子类，您还需要提供一个自定义的 {@link ForkJoinPool.ForkJoinWorkerThreadFactory} 以在 {@code ForkJoinPool} 中使用。
 *
 * @since 1.7
 * @author Doug Lea
 */
public class ForkJoinWorkerThread extends Thread {
    /*
     * ForkJoinWorkerThreads 由 ForkJoinPools 管理并执行 ForkJoinTasks。有关解释，请参阅类 ForkJoinPool 的内部文档。
     *
     * 该类仅维护与池和 WorkQueue 的链接。pool 字段在构造时立即设置，但 workQueue 字段在调用 registerWorker 完成之前未设置。这会导致可见性竞争，通过要求 workQueue 字段仅由拥有线程访问来容忍。
     *
     * 支持（非公共）子类 InnocuousForkJoinWorkerThread 需要我们在该类及其子类中打破相当多的封装（通过 Unsafe）以访问和设置线程字段。
     */

    final ForkJoinPool pool;                // 该线程工作的池
    final ForkJoinPool.WorkQueue workQueue; // 工作窃取机制

    /** 支持无权限的 AccessControlContext */
    private static final AccessControlContext INNOCUOUS_ACC =
        new AccessControlContext(
            new ProtectionDomain[] { new ProtectionDomain(null, null) });

    /**
     * 在给定池中创建一个 ForkJoinWorkerThread。
     *
     * @param pool 该线程工作的池
     * @throws NullPointerException 如果池为 null
     */
    protected ForkJoinWorkerThread(ForkJoinPool pool) {
        // 使用占位符，直到在 registerWorker 中设置有用的名称
        super("aForkJoinWorkerThread");
        this.pool = pool;
        this.workQueue = pool.registerWorker(this);
    }

    /**
     * 用于默认池的版本。这是单独的构造函数，以避免影响受保护的构造函数。
     */
    ForkJoinWorkerThread(ForkJoinPool pool, boolean innocuous) {
        super("aForkJoinWorkerThread");
        if (innocuous) {
            U.putOrderedObject(this, INHERITEDACCESSCONTROLCONTEXT, INNOCUOUS_ACC);
        }
        this.pool = pool;
        this.workQueue = pool.registerWorker(this);
    }

    /**
     * 用于 InnocuousForkJoinWorkerThread 的版本
     */
    ForkJoinWorkerThread(ForkJoinPool pool, ThreadGroup threadGroup,
                         AccessControlContext acc) {
        super(threadGroup, null, "aForkJoinWorkerThread");
        U.putOrderedObject(this, INHERITEDACCESSCONTROLCONTEXT, acc);
        eraseThreadLocals(); // 注册前清除
        this.pool = pool;
        this.workQueue = pool.registerWorker(this);
    }

    /**
     * 返回托管此线程的池。
     *
     * @return 池
     */
    public ForkJoinPool getPool() {
        return pool;
    }

    /**
     * 返回此线程在其池中的唯一索引号。返回值范围从零到池中可能存在的最大线程数（减一），并且在该线程的生命周期内不会改变。此方法对于跟踪每个工作线程的状态或收集结果而不是每个任务的应用程序可能很有用。
     *
     * @return 索引号
     */
    public int getPoolIndex() {
        return workQueue.getPoolIndex();
    }

    /**
     * 在构造后但在处理任何任务之前初始化内部状态。如果重写此方法，必须在方法的开头调用 {@code super.onStart()}。初始化需要小心：大多数字段必须具有合法的默认值，以确保在该线程开始处理任务之前，从其他线程尝试访问时能正确工作。
     */
    protected void onStart() {
    }

    /**
     * 执行与此工作线程终止相关的清理。如果重写此方法，必须在重写方法的末尾调用 {@code super.onTermination}。
     *
     * @param exception 导致此线程因无法恢复的错误而中止的异常，或如果正常完成则为 {@code null}
     */
    protected void onTermination(Throwable exception) {
    }

    /**
     * 此方法必须是公共的，但不应显式调用。它执行主运行循环以执行 {@link ForkJoinTask}。
     */
    public void run() {
        if (workQueue.array == null) { // 仅运行一次
            Throwable exception = null;
            try {
                onStart();
                pool.runWorker(workQueue);
            } catch (Throwable ex) {
                exception = ex;
            } finally {
                try {
                    onTermination(exception);
                } catch (Throwable ex) {
                    if (exception == null)
                        exception = ex;
                } finally {
                    pool.deregisterWorker(this, exception);
                }
            }
        }
    }

    /**
     * 通过将 Thread 映射设置为 null 来清除 ThreadLocals。
     */
    final void eraseThreadLocals() {
        U.putObject(this, THREADLOCALS, null);
        U.putObject(this, INHERITABLETHREADLOCALS, null);
    }

    /**
     * 用于 InnocuousForkJoinWorkerThread 的非公共钩子方法
     */
    void afterTopLevelExec() {
    }

    // 设置以允许在构造函数中设置线程字段
    private static final sun.misc.Unsafe U;
    private static final long THREADLOCALS;
    private static final long INHERITABLETHREADLOCALS;
    private static final long INHERITEDACCESSCONTROLCONTEXT;
    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            THREADLOCALS = U.objectFieldOffset
                (tk.getDeclaredField("threadLocals"));
            INHERITABLETHREADLOCALS = U.objectFieldOffset
                (tk.getDeclaredField("inheritableThreadLocals"));
            INHERITEDACCESSCONTROLCONTEXT = U.objectFieldOffset
                (tk.getDeclaredField("inheritedAccessControlContext"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * 没有权限、不属于任何用户定义的 ThreadGroup，并在运行每个顶级任务后清除所有 ThreadLocals 的工作线程。
     */
    static final class InnocuousForkJoinWorkerThread extends ForkJoinWorkerThread {
        /** 所有 InnocuousForkJoinWorkerThreads 的 ThreadGroup */
        private static final ThreadGroup innocuousThreadGroup =
            createThreadGroup();

        InnocuousForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool, innocuousThreadGroup, INNOCUOUS_ACC);
        }

        @Override // 以清除 ThreadLocals
        void afterTopLevelExec() {
            eraseThreadLocals();
        }

        @Override // 始终报告系统加载器
        public ClassLoader getContextClassLoader() {
            return ClassLoader.getSystemClassLoader();
        }

        @Override // 静默失败
        public void setUncaughtExceptionHandler(UncaughtExceptionHandler x) { }

        @Override // 偏执地
        public void setContextClassLoader(ClassLoader cl) {
            throw new SecurityException("setContextClassLoader");
        }

        /**
         * 返回一个以系统 ThreadGroup（顶级、无父级的组）为父级的新组。使用 Unsafe 遍历 Thread.group 和 ThreadGroup.parent 字段。
         */
        private static ThreadGroup createThreadGroup() {
            try {
                sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
                Class<?> tk = Thread.class;
                Class<?> gk = ThreadGroup.class;
                long tg = u.objectFieldOffset(tk.getDeclaredField("group"));
                long gp = u.objectFieldOffset(gk.getDeclaredField("parent"));
                ThreadGroup group = (ThreadGroup)
                    u.getObject(Thread.currentThread(), tg);
                while (group != null) {
                    ThreadGroup parent = (ThreadGroup)u.getObject(group, gp);
                    if (parent == null)
                        return new ThreadGroup(group,
                                               "InnocuousForkJoinWorkerThreadGroup");
                    group = parent;
                }
            } catch (Exception e) {
                throw new Error(e);
            }
            // 如果为空，则作为不可能发生的保护措施
            throw new Error("Cannot create ThreadGroup");
        }
    }

}
