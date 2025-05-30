/*
 * Copyright (c) 1999, 2022, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 包私有实用类，包含管理虚拟机关闭序列的数据结构和逻辑。
 *
 * @author   Mark Reinhold
 * @since    1.3
 *
 * @see java.io.Console
 * @see ApplicationShutdownHooks
 * @see java.io.DeleteOnExitHook
 */

class Shutdown {

    // 系统关闭钩子注册在预定义的槽位。
    // 关闭钩子列表如下：
    // (0) 控制台恢复钩子
    // (1) ApplicationShutdownHooks 调用所有已注册的应用程序关闭钩子并等待它们完成
    // (2) DeleteOnExit 钩子
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final Runnable[] hooks = new Runnable[MAX_SYSTEM_HOOKS];

    // 当前正在运行的关闭钩子在 hooks 数组中的索引
    private static int currentRunningHook = -1;

    // 跟踪是否已经（开始）关闭
    private static boolean isShutdown;

    /* 前面的静态字段受此锁保护 */
    private static class Lock { };
    private static Object lock = new Lock();

    /* 本地 halt 方法的锁对象 */
    private static Object haltLock = new Lock();

    /**
     * 添加一个新的系统关闭钩子。检查关闭状态和钩子本身，但不执行任何安全检查。
     *
     * registerShutdownInProgress 参数应为 false，除非注册 DeleteOnExitHook，因为第一个文件
     * 可能由应用程序关闭钩子添加到删除列表中。
     *
     * @param slot  关闭钩子数组中的槽位，其元素将在关闭时按顺序调用
     * @param registerShutdownInProgress 如果关闭正在进行中，允许注册钩子
     * @param hook  要注册的钩子
     *
     * @throws IllegalStateException
     *         如果 registerShutdownInProgress 为 false 且关闭正在进行中；或
     *         如果 registerShutdownInProgress 为 true 且关闭过程已超过给定槽位
     */
    static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        if (slot < 0 || slot >= MAX_SYSTEM_HOOKS) {
            throw new IllegalArgumentException("无效的槽位: " + slot);
        }
        synchronized (lock) {
            if (hooks[slot] != null)
                throw new InternalError("槽位 " + slot + " 的关闭钩子已注册");

            if (!registerShutdownInProgress) {
                if (currentRunningHook >= 0)
                    throw new IllegalStateException("关闭正在进行中");
            } else {
                if (isShutdown || slot <= currentRunningHook)
                    throw new IllegalStateException("关闭正在进行中");
            }

            hooks[slot] = hook;
        }
    }

    /* 运行所有系统关闭钩子。
     *
     * 系统关闭钩子在同步于 Shutdown.class 的线程中运行。其他线程调用 Runtime::exit, Runtime::halt
     * 或 JNI DestroyJavaVM 将无限期阻塞。
     *
     * ApplicationShutdownHooks 注册为一个单独的钩子，启动所有应用程序关闭钩子并等待它们完成。
     */
    private static void runHooks() {
        synchronized (lock) {
            /* 防止守护线程在 DestroyJavaVM 初始化关闭序列后调用 exit */
            if (isShutdown) return;
        }

        for (int i=0; i < MAX_SYSTEM_HOOKS; i++) {
            try {
                Runnable hook;
                synchronized (lock) {
                    // 获取锁以确保在关闭期间注册的钩子在此处可见。
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook != null) hook.run();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath td = (ThreadDeath)t;
                    throw td;
                }
            }
        }

        // 设置关闭状态
        // 同步是为了可见性；只有一个线程可以到达这里。
        synchronized (lock) {
            isShutdown = true;
        }
    }

    /* 通知 VM 是时候停止了。 */
    static native void beforeHalt();

    /* halt 方法在 halt 锁上同步
     * 以避免删除列表中的文件在关闭时被破坏。
     * 它调用真正的本地 halt 方法。
     */
    static void halt(int status) {
        synchronized (haltLock) {
            halt0(status);
        }
    }

    static native void halt0(int status);

    /* 由 Runtime.exit 调用，执行所有安全检查。
     * 也由系统提供的终止事件的处理程序调用，
     * 应该传递非零状态码。
     */
    static void exit(int status) {
        synchronized (lock) {
            if (status != 0 && isShutdown) {
                /* 非零状态立即停止 */
                halt(status);
            }
        }
        synchronized (Shutdown.class) {
            /* 同步于类对象，导致任何尝试初始化关闭的其他线程
             * 无限期阻塞
             */
            beforeHalt();
            runHooks();
            halt(status);
        }
    }


    /* 由 JNI DestroyJavaVM 过程在最后一个非守护线程完成时调用。
     * 与 exit 方法不同，此方法不会实际停止 VM。
     */
    static void shutdown() {
        synchronized (Shutdown.class) {
            runHooks();
        }
    }

}
