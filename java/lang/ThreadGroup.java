
/*
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintStream;
import java.util.Arrays;
import sun.misc.VM;

/**
 * 线程组表示一组线程。此外，线程组还可以包括其他线程组。线程组形成一个树结构，其中每个线程组除了初始线程组外都有一个父线程组。
 * <p>
 * 线程只能访问其自身线程组的信息，而不能访问其线程组的父线程组或其他线程组的信息。
 *
 * @author  未注明
 * @since   JDK1.0
 */
/* 该代码的锁定策略是尽可能只锁定树的一级，但必要时从子线程组到父线程组进行锁定。
 * 这样做的优点是限制了需要持有的锁的数量，特别是避免了获取根线程组（或全局锁）的锁，
 * 这在多处理器系统上有许多线程组时会成为争用的来源。
 * 这种策略通常会导致获取线程组状态的快照，并在此快照上进行操作，而不是在处理子线程组时锁定线程组。
 */
public
class ThreadGroup implements Thread.UncaughtExceptionHandler {
    private final ThreadGroup parent;
    String name;
    int maxPriority;
    boolean destroyed;
    boolean daemon;
    boolean vmAllowSuspension;

    int nUnstartedThreads = 0;
    int nthreads;
    Thread threads[];

    int ngroups;
    ThreadGroup groups[];

    /**
     * 创建一个不在任何线程组中的空线程组。此方法用于创建系统线程组。
     */
    private ThreadGroup() {     // 从C代码调用
        this.name = "system";
        this.maxPriority = Thread.MAX_PRIORITY;
        this.parent = null;
    }

    /**
     * 构造一个新的线程组。此新组的父线程组是当前运行线程的线程组。
     * <p>
     * 调用父线程组的 <code>checkAccess</code> 方法，不带参数；这可能会导致安全异常。
     *
     * @param   name   新线程组的名称。
     * @exception  SecurityException  如果当前线程不能在指定的线程组中创建线程。
     * @see     java.lang.ThreadGroup#checkAccess()
     * @since   JDK1.0
     */
    public ThreadGroup(String name) {
        this(Thread.currentThread().getThreadGroup(), name);
    }

    /**
     * 创建一个新的线程组。此新组的父线程组是指定的线程组。
     * <p>
     * 调用父线程组的 <code>checkAccess</code> 方法，不带参数；这可能会导致安全异常。
     *
     * @param     parent   父线程组。
     * @param     name     新线程组的名称。
     * @exception  NullPointerException  如果线程组参数为 <code>null</code>。
     * @exception  SecurityException  如果当前线程不能在指定的线程组中创建线程。
     * @see     java.lang.SecurityException
     * @see     java.lang.ThreadGroup#checkAccess()
     * @since   JDK1.0
     */
    public ThreadGroup(ThreadGroup parent, String name) {
        this(checkParentAccess(parent), parent, name);
    }

    private ThreadGroup(Void unused, ThreadGroup parent, String name) {
        this.name = name;
        this.maxPriority = parent.maxPriority;
        this.daemon = parent.daemon;
        this.vmAllowSuspension = parent.vmAllowSuspension;
        this.parent = parent;
        parent.add(this);
    }

    /*
     * @throws  NullPointerException  如果父参数为 {@code null}
     * @throws  SecurityException     如果当前线程不能在指定的线程组中创建线程。
     */
    private static Void checkParentAccess(ThreadGroup parent) {
        parent.checkAccess();
        return null;
    }

    /**
     * 返回此线程组的名称。
     *
     * @return  此线程组的名称。
     * @since   JDK1.0
     */
    public final String getName() {
        return name;
    }

    /**
     * 返回此线程组的父线程组。
     * <p>
     * 首先，如果父线程组不为 <code>null</code>，则调用父线程组的 <code>checkAccess</code> 方法，不带参数；这可能会导致安全异常。
     *
     * @return  此线程组的父线程组。顶级线程组是唯一一个其父线程组为 <code>null</code> 的线程组。
     * @exception  SecurityException  如果当前线程不能修改此线程组。
     * @see        java.lang.ThreadGroup#checkAccess()
     * @see        java.lang.SecurityException
     * @see        java.lang.RuntimePermission
     * @since   JDK1.0
     */
    public final ThreadGroup getParent() {
        if (parent != null)
            parent.checkAccess();
        return parent;
    }

    /**
     * 返回此线程组的最大优先级。属于此组的线程不能具有高于最大优先级的优先级。
     *
     * @return  此线程组中的线程可以具有的最大优先级。
     * @see     #setMaxPriority
     * @since   JDK1.0
     */
    public final int getMaxPriority() {
        return maxPriority;
    }

    /**
     * 测试此线程组是否为守护线程组。守护线程组在其最后一个线程停止或其最后一个子线程组被销毁时自动销毁。
     *
     * @return  <code>true</code> 如果此线程组是守护线程组；否则返回 <code>false</code>。
     * @since   JDK1.0
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * 测试此线程组是否已被销毁。
     *
     * @return  如果此对象已被销毁，则返回 true
     * @since   JDK1.1
     */
    public synchronized boolean isDestroyed() {
        return destroyed;
    }

    /**
     * 更改此线程组的守护状态。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带参数；这可能会导致安全异常。
     * <p>
     * 守护线程组在其最后一个线程停止或其最后一个子线程组被销毁时自动销毁。
     *
     * @param      daemon   如果为 <code>true</code>，将此线程组标记为守护线程组；否则，将此线程组标记为普通线程组。
     * @exception  SecurityException  如果当前线程不能修改此线程组。
     * @see        java.lang.SecurityException
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     */
    public final void setDaemon(boolean daemon) {
        checkAccess();
        this.daemon = daemon;
    }

    /**
     * 设置线程组的最大优先级。线程组中已经具有较高优先级的线程不受影响。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带参数；这可能会导致安全异常。
     * <p>
     * 如果 <code>pri</code> 参数小于 {@link Thread#MIN_PRIORITY} 或大于 {@link Thread#MAX_PRIORITY}，则线程组的最大优先级保持不变。
     * <p>
     * 否则，此 ThreadGroup 对象的优先级设置为指定的 <code>pri</code> 和此线程组的父线程组允许的最大优先级中的较小值。（如果此线程组是系统线程组，没有父线程组，则其最大优先级直接设置为 <code>pri</code>。）然后，此方法以 <code>pri</code> 为参数递归调用此线程组中的每个子线程组。
     *
     * @param      pri   线程组的新优先级。
     * @exception  SecurityException  如果当前线程不能修改此线程组。
     * @see        #getMaxPriority
     * @see        java.lang.SecurityException
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     */
    public final void setMaxPriority(int pri) {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            if (pri < Thread.MIN_PRIORITY || pri > Thread.MAX_PRIORITY) {
                return;
            }
            maxPriority = (parent != null) ? Math.min(pri, parent.maxPriority) : pri;
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            groupsSnapshot[i].setMaxPriority(pri);
        }
    }

    /**
     * 测试此线程组是否为指定线程组或其祖先线程组之一。
     *
     * @param   g   一个线程组。
     * @return  <code>true</code> 如果此线程组是指定线程组或其祖先线程组之一；否则返回 <code>false</code>。
     * @since   JDK1.0
     */
    public final boolean parentOf(ThreadGroup g) {
        for (; g != null ; g = g.parent) {
            if (g == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * 确定当前运行的线程是否有权限修改此线程组。
     * <p>
     * 如果存在安全管理器，其 <code>checkAccess</code> 方法将被调用，参数为此线程组。这可能会导致抛出 <code>SecurityException</code>。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程组。
     * @see        java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
     * @since      JDK1.0
     */
    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }

    /**
     * 返回此线程组及其子组中的活动线程数的估计值。递归地遍历此线程组的所有子组。
     *
     * <p> 返回的值只是一个估计值，因为线程数可能会在方法遍历内部数据结构时动态变化，并可能受到某些系统线程的影响。此方法主要用于调试和监控目的。
     *
     * @return  此线程组及其所有子组中的活动线程数的估计值。
     *
     * @since   JDK1.0
     */
    public int activeCount() {
        int result;
        // 拍摄子组数据的快照，以便在子线程组计算时不会持有此锁
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            if (destroyed) {
                return 0;
            }
            result = nthreads;
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            result += groupsSnapshot[i].activeCount();
        }
        return result;
    }

    /**
     * 将此线程组及其子组中的所有活动线程复制到指定的数组中。
     *
     * <p> 调用此方法的行为与调用
     *
     * <blockquote>
     * {@linkplain #enumerate(Thread[], boolean) enumerate}{@code (list, true)}
     * </blockquote>
     *
     * 完全相同。
     *
     * @param  list
     *         用于存放线程列表的数组
     *
     * @return  放入数组中的线程数
     *
     * @throws  SecurityException
     *          如果 {@linkplain #checkAccess checkAccess} 确定当前线程不能访问此线程组
     *
     * @since   JDK1.0
     */
    public int enumerate(Thread list[]) {
        checkAccess();
        return enumerate(list, 0, true);
    }

    /**
     * 将此线程组中的所有活动线程复制到指定的数组中。如果 {@code recurse} 为 {@code true}，
     * 则此方法递归地枚举此线程组的所有子组，并将这些子组中的所有活动线程的引用也包括在内。如果数组太短而无法容纳所有线程，则多余的线程将被静默忽略。
     *
     * <p> 应用程序可以使用 {@linkplain #activeCount activeCount} 方法来估计数组应该有多大，但是
     * <i>如果数组太短而无法容纳所有线程，多余的线程将被静默忽略。</i>  如果需要获取此线程组中的每个活动线程，调用者应验证返回的 int 值是否严格小于 {@code list} 的长度。
     *
     * <p> 由于此方法中的固有竞争条件，建议仅将其用于调试和监控目的。
     *
     * @param  list
     *         用于存放线程列表的数组
     *
     * @param  recurse
     *         如果为 {@code true}，则递归地枚举此线程组的所有子组
     *
     * @return  放入数组中的线程数
     *
     * @throws  SecurityException
     *          如果 {@linkplain #checkAccess checkAccess} 确定当前线程不能访问此线程组
     *
     * @since   JDK1.0
     */
    public int enumerate(Thread list[], boolean recurse) {
        checkAccess();
        return enumerate(list, 0, recurse);
    }


                private int enumerate(Thread list[], int n, boolean recurse) {
        int ngroupsSnapshot = 0;
        ThreadGroup[] groupsSnapshot = null;
        synchronized (this) {
            if (destroyed) {
                return 0;
            }
            int nt = nthreads;
            if (nt > list.length - n) {
                nt = list.length - n;
            }
            for (int i = 0; i < nt; i++) {
                if (threads[i].isAlive()) {
                    list[n++] = threads[i];
                }
            }
            if (recurse) {
                ngroupsSnapshot = ngroups;
                if (groups != null) {
                    groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
                } else {
                    groupsSnapshot = null;
                }
            }
        }
        if (recurse) {
            for (int i = 0 ; i < ngroupsSnapshot ; i++) {
                n = groupsSnapshot[i].enumerate(list, n, true);
            }
        }
        return n;
    }

    /**
     * 返回此线程组及其子组中活动组的数量估计值。递归遍历此线程组的所有子组。
     *
     * <p> 返回的值只是一个估计值，因为线程组的数量可能会在该方法遍历内部数据结构时动态变化。此方法主要用于调试和监控目的。
     *
     * @return  以此线程组为祖先的活动线程组的数量
     *
     * @since   JDK1.0
     */
    public int activeGroupCount() {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            if (destroyed) {
                return 0;
            }
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        int n = ngroupsSnapshot;
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            n += groupsSnapshot[i].activeGroupCount();
        }
        return n;
    }

    /**
     * 将此线程组及其子组中所有活动子组的引用复制到指定数组中。
     *
     * <p> 调用此方法的行为与调用
     *
     * <blockquote>
     * {@linkplain #enumerate(ThreadGroup[], boolean) enumerate}{@code (list, true)}
     * </blockquote>
     *
     * 完全相同。
     *
     * @param  list
     *         用于存放线程组列表的数组
     *
     * @return  放入数组中的线程组数量
     *
     * @throws  SecurityException
     *          如果 {@linkplain #checkAccess checkAccess} 确定当前线程无法访问此线程组
     *
     * @since   JDK1.0
     */
    public int enumerate(ThreadGroup list[]) {
        checkAccess();
        return enumerate(list, 0, true);
    }

    /**
     * 将此线程组中所有活动子组的引用复制到指定数组中。如果 {@code recurse} 为
     * {@code true}，此方法递归枚举此线程组的所有子组，并将所有活动线程组的引用也包括在内。
     *
     * <p> 应用程序可以使用
     * {@linkplain #activeGroupCount activeGroupCount} 方法来
     * 估计数组应该有多大，但如果数组太短而无法容纳所有线程组，则多余的线程组将被静默忽略。如果必须获取此线程组中的每个活动子组，调用者应验证返回的 int 值是否严格小于
     * {@code list} 的长度。
     *
     * <p> 由于此方法中存在固有的竞争条件，建议仅将其用于调试和监控目的。
     *
     * @param  list
     *         用于存放线程组列表的数组
     *
     * @param  recurse
     *         如果为 {@code true}，递归枚举所有子组
     *
     * @return  放入数组中的线程组数量
     *
     * @throws  SecurityException
     *          如果 {@linkplain #checkAccess checkAccess} 确定当前线程无法访问此线程组
     *
     * @since   JDK1.0
     */
    public int enumerate(ThreadGroup list[], boolean recurse) {
        checkAccess();
        return enumerate(list, 0, recurse);
    }

    private int enumerate(ThreadGroup list[], int n, boolean recurse) {
        int ngroupsSnapshot = 0;
        ThreadGroup[] groupsSnapshot = null;
        synchronized (this) {
            if (destroyed) {
                return 0;
            }
            int ng = ngroups;
            if (ng > list.length - n) {
                ng = list.length - n;
            }
            if (ng > 0) {
                System.arraycopy(groups, 0, list, n, ng);
                n += ng;
            }
            if (recurse) {
                ngroupsSnapshot = ngroups;
                if (groups != null) {
                    groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
                } else {
                    groupsSnapshot = null;
                }
            }
        }
        if (recurse) {
            for (int i = 0 ; i < ngroupsSnapshot ; i++) {
                n = groupsSnapshot[i].enumerate(list, n, true);
            }
        }
        return n;
    }

    /**
     * 停止此线程组中的所有线程。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带任何参数；这可能会导致安全异常。
     * <p>
     * 然后，此方法调用此线程组及其所有子组中的所有线程的 <code>stop</code> 方法。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程组或线程组中的任何线程。
     * @see        java.lang.SecurityException
     * @see        java.lang.Thread#stop()
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     * @deprecated    此方法本质上是不安全的。请参阅
     *     {@link Thread#stop} 了解详细信息。
     */
    @Deprecated
    public final void stop() {
        if (stopOrSuspend(false))
            Thread.currentThread().stop();
    }

    /**
     * 中断此线程组中的所有线程。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带任何参数；这可能会导致安全异常。
     * <p>
     * 然后，此方法调用此线程组及其所有子组中的所有线程的 <code>interrupt</code> 方法。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程组或线程组中的任何线程。
     * @see        java.lang.Thread#interrupt()
     * @see        java.lang.SecurityException
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      1.2
     */
    public final void interrupt() {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            for (int i = 0 ; i < nthreads ; i++) {
                threads[i].interrupt();
            }
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            groupsSnapshot[i].interrupt();
        }
    }

    /**
     * 暂停此线程组中的所有线程。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带任何参数；这可能会导致安全异常。
     * <p>
     * 然后，此方法调用此线程组及其所有子组中的所有线程的 <code>suspend</code> 方法。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程组或线程组中的任何线程。
     * @see        java.lang.Thread#suspend()
     * @see        java.lang.SecurityException
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     * @deprecated    此方法本质上容易导致死锁。请参阅
     *     {@link Thread#suspend} 了解详细信息。
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public final void suspend() {
        if (stopOrSuspend(true))
            Thread.currentThread().suspend();
    }

    /**
     * 辅助方法：递归停止或暂停（由布尔参数决定）此线程组及其子组中的所有线程，但当前线程除外。如果（且仅当）当前线程在此线程组或其子组中，此方法返回 true。
     */
    @SuppressWarnings("deprecation")
    private boolean stopOrSuspend(boolean suspend) {
        boolean suicide = false;
        Thread us = Thread.currentThread();
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot = null;
        synchronized (this) {
            checkAccess();
            for (int i = 0 ; i < nthreads ; i++) {
                if (threads[i]==us)
                    suicide = true;
                else if (suspend)
                    threads[i].suspend();
                else
                    threads[i].stop();
            }

            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++)
            suicide = groupsSnapshot[i].stopOrSuspend(suspend) || suicide;

        return suicide;
    }

    /**
     * 恢复此线程组中的所有线程。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带任何参数；这可能会导致安全异常。
     * <p>
     * 然后，此方法调用此线程组及其所有子组中的所有线程的 <code>resume</code> 方法。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程组或线程组中的任何线程。
     * @see        java.lang.SecurityException
     * @see        java.lang.Thread#resume()
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     * @deprecated    此方法仅与
     *      <tt>Thread.suspend</tt> 和 <tt>ThreadGroup.suspend</tt> 一起使用，
     *       这两个方法已被弃用，因为它们本质上容易导致死锁。请参阅 {@link Thread#suspend} 了解详细信息。
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public final void resume() {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            for (int i = 0 ; i < nthreads ; i++) {
                threads[i].resume();
            }
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            groupsSnapshot[i].resume();
        }
    }

    /**
     * 销毁此线程组及其所有子组。此线程组必须为空，表示所有曾在此线程组中的线程均已停止。
     * <p>
     * 首先，调用此线程组的 <code>checkAccess</code> 方法，不带任何参数；这可能会导致安全异常。
     *
     * @exception  IllegalThreadStateException  如果线程组不为空或线程组已被销毁。
     * @exception  SecurityException  如果当前线程无法修改此线程组。
     * @see        java.lang.ThreadGroup#checkAccess()
     * @since      JDK1.0
     */
    public final void destroy() {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            checkAccess();
            if (destroyed || (nthreads > 0)) {
                throw new IllegalThreadStateException();
            }
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
            if (parent != null) {
                destroyed = true;
                ngroups = 0;
                groups = null;
                nthreads = 0;
                threads = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i += 1) {
            groupsSnapshot[i].destroy();
        }
        if (parent != null) {
            parent.remove(this);
        }
    }

    /**
     * 将指定的线程组添加到此组。
     * @param g 要添加的指定线程组
     * @exception IllegalThreadStateException 如果线程组已被销毁。
     */
    private final void add(ThreadGroup g){
        synchronized (this) {
            if (destroyed) {
                throw new IllegalThreadStateException();
            }
            if (groups == null) {
                groups = new ThreadGroup[4];
            } else if (ngroups == groups.length) {
                groups = Arrays.copyOf(groups, ngroups * 2);
            }
            groups[ngroups] = g;

            // 这是最后做的，因此如果线程被杀死，这无关紧要
            ngroups++;
        }
    }

    /**
     * 从此组中移除指定的线程组。
     * @param g 要移除的线程组
     * @return 如果此线程组已被销毁，则返回 true。
     */
    private void remove(ThreadGroup g) {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            for (int i = 0 ; i < ngroups ; i++) {
                if (groups[i] == g) {
                    ngroups -= 1;
                    System.arraycopy(groups, i + 1, groups, i, ngroups - i);
                    // 删除对已死线程组的悬空引用，以便垃圾收集器可以收集它
                    groups[ngroups] = null;
                    break;
                }
            }
            if (nthreads == 0) {
                notifyAll();
            }
            if (daemon && (nthreads == 0) &&
                (nUnstartedThreads == 0) && (ngroups == 0))
            {
                destroy();
            }
        }
    }


    /**
     * 增加线程组中未启动线程的计数。未启动的线程不会添加到线程组中，以便如果它们从未启动，可以被收集，但必须计数，以便守护线程组中包含未启动的线程时不会被销毁。
     */
    void addUnstarted() {
        synchronized(this) {
            if (destroyed) {
                throw new IllegalThreadStateException();
            }
            nUnstartedThreads++;
        }
    }


                /**
     * 将指定的线程添加到此线程组。
     *
     * <p> 注意：此方法由库代码和虚拟机调用。虚拟机会调用此方法将某些系统线程添加到系统线程组。
     *
     * @param  t
     *         要添加的线程
     *
     * @throws  IllegalThreadStateException
     *          如果线程组已被销毁
     */
    void add(Thread t) {
        synchronized (this) {
            if (destroyed) {
                throw new IllegalThreadStateException();
            }
            if (threads == null) {
                threads = new Thread[4];
            } else if (nthreads == threads.length) {
                threads = Arrays.copyOf(threads, nthreads * 2);
            }
            threads[nthreads] = t;

            // 这是最后做的，以防线程被终止
            nthreads++;

            // 线程现在是组的完全成员，即使它可能尚未启动。这将防止组被销毁，因此未启动的线程计数减少。
            nUnstartedThreads--;
        }
    }

    /**
     * 通知组线程 {@code t} 启动失败。
     *
     * <p> 线程组的状态将回滚，就像从未尝试启动该线程一样。线程再次被视为线程组的未启动成员，允许后续尝试启动该线程。
     *
     * @param  t
     *         调用了其 start 方法的线程
     */
    void threadStartFailed(Thread t) {
        synchronized(this) {
            remove(t);
            nUnstartedThreads++;
        }
    }

    /**
     * 通知组线程 {@code t} 已终止。
     *
     * <p> 如果满足以下所有条件，则销毁组：这是一个守护线程组；组中没有更多的活动或未启动的线程；此线程组中没有子组。
     *
     * @param  t
     *         已终止的线程
     */
    void threadTerminated(Thread t) {
        synchronized (this) {
            remove(t);

            if (nthreads == 0) {
                notifyAll();
            }
            if (daemon && (nthreads == 0) &&
                (nUnstartedThreads == 0) && (ngroups == 0))
            {
                destroy();
            }
        }
    }

    /**
     * 从组中移除指定的线程。在已销毁的线程组上调用此方法无效。
     *
     * @param  t
     *         要移除的线程
     */
    private void remove(Thread t) {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            for (int i = 0 ; i < nthreads ; i++) {
                if (threads[i] == t) {
                    System.arraycopy(threads, i + 1, threads, i, --nthreads - i);
                    // 消除对已死线程的悬空引用，以便垃圾回收器可以回收它。
                    threads[nthreads] = null;
                    break;
                }
            }
        }
    }

    /**
     * 将有关此线程组的信息打印到标准输出。此方法仅用于调试。
     *
     * @since   JDK1.0
     */
    public void list() {
        list(System.out, 0);
    }
    void list(PrintStream out, int indent) {
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized (this) {
            for (int j = 0 ; j < indent ; j++) {
                out.print(" ");
            }
            out.println(this);
            indent += 4;
            for (int i = 0 ; i < nthreads ; i++) {
                for (int j = 0 ; j < indent ; j++) {
                    out.print(" ");
                }
                out.println(threads[i]);
            }
            ngroupsSnapshot = ngroups;
            if (groups != null) {
                groupsSnapshot = Arrays.copyOf(groups, ngroupsSnapshot);
            } else {
                groupsSnapshot = null;
            }
        }
        for (int i = 0 ; i < ngroupsSnapshot ; i++) {
            groupsSnapshot[i].list(out, indent);
        }
    }

    /**
     * 当此线程组中的线程因未捕获的异常而停止，并且该线程没有安装特定的 {@link Thread.UncaughtExceptionHandler}
     * 时，由 Java 虚拟机调用。
     * <p>
     * <code>ThreadGroup</code> 的 <code>uncaughtException</code> 方法执行以下操作：
     * <ul>
     * <li>如果此线程组有父线程组，则调用该父线程组的 <code>uncaughtException</code> 方法，参数相同。
     * <li>否则，检查是否安装了 {@linkplain Thread#getDefaultUncaughtExceptionHandler 默认未捕获异常处理器}，
     *     如果安装了，则调用其 <code>uncaughtException</code> 方法，参数相同。
     * <li>否则，检查 <code>Throwable</code> 参数是否是 {@link ThreadDeath} 的实例。如果是，则不执行任何特殊操作。
     *     否则，使用 <code>Throwable</code> 的 {@link Throwable#printStackTrace printStackTrace} 方法，
     *     将包含线程名称（使用线程的 {@link Thread#getName getName} 方法返回）和堆栈回溯的消息打印到
     *     {@linkplain System#err 标准错误流}。
     * </ul>
     * <p>
     * 应用程序可以在 <code>ThreadGroup</code> 的子类中重写此方法以提供未捕获异常的替代处理。
     *
     * @param   t   即将退出的线程。
     * @param   e   未捕获的异常。
     * @since   JDK1.0
     */
    public void uncaughtException(Thread t, Throwable e) {
        if (parent != null) {
            parent.uncaughtException(t, e);
        } else {
            Thread.UncaughtExceptionHandler ueh =
                Thread.getDefaultUncaughtExceptionHandler();
            if (ueh != null) {
                ueh.uncaughtException(t, e);
            } else if (!(e instanceof ThreadDeath)) {
                System.err.print("Exception in thread \""
                                 + t.getName() + "\" ");
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * 由 VM 用于控制低内存隐式挂起。
     *
     * @param b 允许或不允许挂起的布尔值
     * @return 成功时返回 true
     * @since   JDK1.1
     * @deprecated 此调用的定义依赖于已弃用的 {@link #suspend}。此外，此调用的行为从未指定。
     */
    @Deprecated
    public boolean allowThreadSuspension(boolean b) {
        this.vmAllowSuspension = b;
        if (!b) {
            VM.unsuspendSomeThreads();
        }
        return true;
    }

    /**
     * 返回此线程组的字符串表示形式。
     *
     * @return  此线程组的字符串表示形式。
     * @since   JDK1.0
     */
    public String toString() {
        return getClass().getName() + "[name=" + getName() + ",maxpri=" + maxPriority + "]";
    }
}
