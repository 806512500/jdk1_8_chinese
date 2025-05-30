/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import java.security.PrivilegedAction;
import java.security.AccessController;

import sun.awt.PeerEvent;

import sun.util.logging.PlatformLogger;

/**
 * 该工具类用于在允许 {@code EventDispatchThread} 分发事件的同时挂起线程的执行。
 * 类的 API 方法是线程安全的。
 *
 * @author Anton Tarasov, Artem Ananiev
 *
 * @since 1.7
 */
class WaitDispatchSupport implements SecondaryLoop {

    private final static PlatformLogger log =
        PlatformLogger.getLogger("java.awt.event.WaitDispatchSupport");

    private EventDispatchThread dispatchThread;
    private EventFilter filter;

    private volatile Conditional extCondition;
    private volatile Conditional condition;

    private long interval;
    // 使用共享的守护计时器来服务于所有的 WaitDispatchSupport 实例
    private static Timer timer;
    // 当此 WDS 到期时，我们取消计时器任务，但保留共享计时器运行
    private TimerTask timerTask;

    private AtomicBoolean keepBlockingEDT = new AtomicBoolean(false);
    private AtomicBoolean keepBlockingCT = new AtomicBoolean(false);
    private AtomicBoolean afterExit = new AtomicBoolean(false);

    private static synchronized void initializeTimer() {
        if (timer == null) {
            timer = new Timer("AWT-WaitDispatchSupport-Timer", true);
        }
    }

    /**
     * 创建一个 {@code WaitDispatchSupport} 实例来
     * 服务给定的事件分发线程。
     *
     * @param dispatchThread 一个事件分发线程，该线程在等待期间不应停止分发事件
     *
     * @since 1.7
     */
    public WaitDispatchSupport(EventDispatchThread dispatchThread) {
        this(dispatchThread, null);
    }

    /**
     * 创建一个 {@code WaitDispatchSupport} 实例来
     * 服务给定的事件分发线程。
     *
     * @param dispatchThread 一个事件分发线程，该线程在等待期间不应停止分发事件
     * @param extCond 用于确定循环是否应终止的条件对象
     *
     * @since 1.7
     */
    public WaitDispatchSupport(EventDispatchThread dispatchThread,
                               Conditional extCond)
    {
        if (dispatchThread == null) {
            throw new IllegalArgumentException("The dispatchThread can not be null");
        }

        this.dispatchThread = dispatchThread;
        this.extCondition = extCond;
        this.condition = new Conditional() {
            @Override
            public boolean evaluate() {
                if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                    log.finest("evaluate(): blockingEDT=" + keepBlockingEDT.get() +
                               ", blockingCT=" + keepBlockingCT.get());
                }
                boolean extEvaluate =
                    (extCondition != null) ? extCondition.evaluate() : true;
                if (!keepBlockingEDT.get() || !extEvaluate) {
                    if (timerTask != null) {
                        timerTask.cancel();
                        timerTask = null;
                    }
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * 创建一个 {@code WaitDispatchSupport} 实例来
     * 服务给定的事件分发线程。
     * <p>
     * {@link EventFilter} 在等待期间设置在 {@code dispatchThread} 上。
     * 过滤器在等待过程完成后被移除。
     * <p>
     *
     *
     * @param dispatchThread 一个事件分发线程，该线程在等待期间不应停止分发事件
     * @param filter 要设置的 {@code EventFilter}
     * @param interval 等待的时间间隔。注意，当等待过程发生在 EDT 上时，
     *        不能保证在给定时间内停止
     *
     * @since 1.7
     */
    public WaitDispatchSupport(EventDispatchThread dispatchThread,
                               Conditional extCondition,
                               EventFilter filter, long interval)
    {
        this(dispatchThread, extCondition);
        this.filter = filter;
        if (interval < 0) {
            throw new IllegalArgumentException("The interval value must be >= 0");
        }
        this.interval = interval;
        if (interval != 0) {
            initializeTimer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enter() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("enter(): blockingEDT=" + keepBlockingEDT.get() +
                     ", blockingCT=" + keepBlockingCT.get());
        }

        if (!keepBlockingEDT.compareAndSet(false, true)) {
            log.fine("The secondary loop is already running, aborting");
            return false;
        }
        try {
            if (afterExit.get()) {
                log.fine("Exit was called already, aborting");
                return false;
            }

            final Runnable run = new Runnable() {
                public void run() {
                    log.fine("Starting a new event pump");
                    if (filter == null) {
                        dispatchThread.pumpEvents(condition);
                    } else {
                        dispatchThread.pumpEventsForFilter(condition, filter);
                    }
                }
            };

            // 我们有两种阻塞机制：如果我们在分发线程上，启动一个新的事件泵；
            // 如果我们在任何其他线程上，调用 treelock 上的 wait()

            Thread currentThread = Thread.currentThread();
            if (currentThread == dispatchThread) {
                if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                    log.finest("On dispatch thread: " + dispatchThread);
                }
                if (interval != 0) {
                    if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                        log.finest("scheduling the timer for " + interval + " ms");
                    }
                    timer.schedule(timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (keepBlockingEDT.compareAndSet(true, false)) {
                                wakeupEDT();
                            }
                        }
                    }, interval);
                }
                // 处置当前 AppContext 上正在分发的 SequencedEvent，以防止挂起 - 详见 4531693
                SequencedEvent currentSE = KeyboardFocusManager.
                        getCurrentKeyboardFocusManager().getCurrentSequencedEvent();
                if (currentSE != null) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("Dispose current SequencedEvent: " + currentSE);
                    }
                    currentSE.dispose();
                }
                // 如果在启动新的事件泵之前调用了 exit() 方法，它将向 EDT 发送唤醒事件。
                // 该事件将在新的事件泵启动后处理。因此，enter() 方法不会挂起。
                //
                // 事件泵应该是特权的。参见 6300270。
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        run.run();
                        return null;
                    }
                });
            } else {
                if (log.isLoggable(PlatformLogger.Level.FINEST)) {
                    log.finest("On non-dispatch thread: " + currentThread);
                }
                keepBlockingCT.set(true);
                synchronized (getTreeLock()) {
                    if (afterExit.get()) return false;
                    if (filter != null) {
                        dispatchThread.addEventFilter(filter);
                    }
                    try {
                        EventQueue eq = dispatchThread.getEventQueue();
                        eq.postEvent(new PeerEvent(this, run, PeerEvent.PRIORITY_EVENT));
                        if (interval > 0) {
                            long currTime = System.currentTimeMillis();
                            while (keepBlockingCT.get() &&
                                    ((extCondition != null) ? extCondition.evaluate() : true) &&
                                    (currTime + interval > System.currentTimeMillis()))
                            {
                                getTreeLock().wait(interval);
                            }
                        } else {
                            while (keepBlockingCT.get() &&
                                    ((extCondition != null) ? extCondition.evaluate() : true))
                            {
                                getTreeLock().wait();
                            }
                        }
                        if (log.isLoggable(PlatformLogger.Level.FINE)) {
                            log.fine("waitDone " + keepBlockingEDT.get() + " " + keepBlockingCT.get());
                        }
                    } catch (InterruptedException e) {
                        if (log.isLoggable(PlatformLogger.Level.FINE)) {
                            log.fine("Exception caught while waiting: " + e);
                        }
                    } finally {
                        if (filter != null) {
                            dispatchThread.removeEventFilter(filter);
                        }
                    }
                }
            }
            return true;
        }
        finally {
            keepBlockingEDT.set(false);
            keepBlockingCT.set(false);
            afterExit.set(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean exit() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("exit(): blockingEDT=" + keepBlockingEDT.get() +
                     ", blockingCT=" + keepBlockingCT.get());
        }
        afterExit.set(true);
        if (keepBlockingEDT.getAndSet(false)) {
            wakeupEDT();
            return true;
        }
        return false;
    }

    private final static Object getTreeLock() {
        return Component.LOCK;
    }

    private final Runnable wakingRunnable = new Runnable() {
        public void run() {
            log.fine("Wake up EDT");
            synchronized (getTreeLock()) {
                keepBlockingCT.set(false);
                getTreeLock().notifyAll();
            }
            log.fine("Wake up EDT done");
        }
    };

    private void wakeupEDT() {
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            log.finest("wakeupEDT(): EDT == " + dispatchThread);
        }
        EventQueue eq = dispatchThread.getEventQueue();
        eq.postEvent(new PeerEvent(this, wakingRunnable, PeerEvent.PRIORITY_EVENT));
    }
}
