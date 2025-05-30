/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * 一个可以由 Timer 调度执行一次或多次的任务。
 *
 * @author  Josh Bloch
 * @see     Timer
 * @since   1.3
 */

public abstract class TimerTask implements Runnable {
    /**
     * 用于控制 TimerTask 内部访问的对象。
     */
    final Object lock = new Object();

    /**
     * 此任务的状态，从以下常量中选择。
     */
    int state = VIRGIN;

    /**
     * 此任务尚未被调度。
     */
    static final int VIRGIN = 0;

    /**
     * 此任务已被调度执行。如果它是一个非重复任务，则尚未执行。
     */
    static final int SCHEDULED   = 1;

    /**
     * 此非重复任务已执行（或正在执行）且未被取消。
     */
    static final int EXECUTED    = 2;

    /**
     * 此任务已被取消（通过调用 TimerTask.cancel）。
     */
    static final int CANCELLED   = 3;

    /**
     * 此任务的下一次执行时间，格式为 System.currentTimeMillis 返回的格式，假设此任务已被调度执行。
     * 对于重复任务，此字段在每次任务执行前更新。
     */
    long nextExecutionTime;

    /**
     * 重复任务的周期（毫秒）。正值表示固定速率执行。负值表示固定延迟执行。0 表示非重复任务。
     */
    long period = 0;

    /**
     * 创建一个新的定时任务。
     */
    protected TimerTask() {
    }

    /**
     * 此定时任务要执行的操作。
     */
    public abstract void run();

    /**
     * 取消此定时任务。如果任务已被调度执行一次且尚未运行，或尚未被调度，它将永远不会运行。
     * 如果任务已被调度重复执行，它将永远不会再次运行。（如果任务在调用此方法时正在运行，
     * 任务将运行到完成，但将永远不会再次运行。）
     *
     * <p>注意，从重复定时任务的 <tt>run</tt> 方法内部调用此方法绝对可以保证定时任务不会再次运行。
     *
     * <p>此方法可以多次调用；第二次及后续调用没有效果。
     *
     * @return 如果此任务已被调度执行一次且尚未运行，或此任务已被调度重复执行，则返回 true。
     *         如果任务已被调度执行一次且已运行，或任务从未被调度，或任务已被取消，则返回 false。
     *         （粗略地说，如果此方法阻止一个或多个已调度的执行，则返回 <tt>true</tt>。）
     */
    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    /**
     * 返回此任务最近一次实际执行的调度执行时间。（如果在任务执行过程中调用此方法，
     * 返回值是正在进行的任务执行的调度执行时间。）
     *
     * <p>此方法通常在任务的 run 方法内部调用，以确定当前任务执行是否足够及时以执行计划的活动：
     * <pre>{@code
     *   public void run() {
     *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
     *           MAX_TARDINESS)
     *               return;  // 太晚了；跳过此次执行。
     *       // 执行任务
     *   }
     * }</pre>
     * 此方法通常不与固定延迟执行的重复任务一起使用，因为它们的调度执行时间允许随时间漂移，因此不太重要。
     *
     * @return 此任务最近一次执行的调度时间，格式为 Date.getTime() 返回的格式。
     *         如果任务尚未开始其第一次执行，则返回值未定义。
     * @see Date#getTime()
     */
    public long scheduledExecutionTime() {
        synchronized(lock) {
            return (period < 0 ? nextExecutionTime + period
                               : nextExecutionTime - period);
        }
    }
}
