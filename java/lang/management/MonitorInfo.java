/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.management;

import javax.management.openmbean.CompositeData;
import sun.management.MonitorInfoCompositeData;

/**
 * 关于对象监视器锁的信息。当进入该对象的同步块或方法时，对象监视器被锁定。
 *
 * <h3>MXBean 映射</h3>
 * <tt>MonitorInfo</tt> 被映射为一个 {@link CompositeData CompositeData}
 * 属性如 {@link #from from} 方法中指定的那样。
 *
 * @author  Mandy Chung
 * @since   1.6
 */
public class MonitorInfo extends LockInfo {

    private int    stackDepth;
    private StackTraceElement stackFrame;

    /**
     * 构造一个 <tt>MonitorInfo</tt> 对象。
     *
     * @param className 锁对象类的完全限定名。
     * @param identityHashCode 锁对象的 {@link System#identityHashCode
     *                         身份哈希码}。
     * @param stackDepth 对象监视器被锁定时在堆栈跟踪中的深度。
     * @param stackFrame 锁定对象监视器的堆栈帧。
     * @throws IllegalArgumentException 如果
     *    <tt>stackDepth</tt> &ge; 0 但 <tt>stackFrame</tt> 为 <tt>null</tt>，
     *    或 <tt>stackDepth</tt> &lt; 0 但 <tt>stackFrame</tt> 不为
     *       <tt>null</tt>。
     */
    public MonitorInfo(String className,
                       int identityHashCode,
                       int stackDepth,
                       StackTraceElement stackFrame) {
        super(className, identityHashCode);
        if (stackDepth >= 0 && stackFrame == null) {
            throw new IllegalArgumentException("参数 stackDepth 是 " +
                stackDepth + " 但 stackFrame 为 null");
        }
        if (stackDepth < 0 && stackFrame != null) {
            throw new IllegalArgumentException("参数 stackDepth 是 " +
                stackDepth + " 但 stackFrame 不为 null");
        }
        this.stackDepth = stackDepth;
        this.stackFrame = stackFrame;
    }

    /**
     * 返回对象监视器被锁定时在堆栈跟踪中的深度。深度是 {@link ThreadInfo#getStackTrace} 方法返回的
     * <tt>StackTraceElement</tt> 数组的索引。
     *
     * @return 对象监视器被锁定时在堆栈跟踪中的深度，或如果不可用则返回负数。
     */
    public int getLockedStackDepth() {
        return stackDepth;
    }

    /**
     * 返回锁定对象监视器的堆栈帧。
     *
     * @return 锁定对象监视器的 <tt>StackTraceElement</tt>，或如果不可用则返回 <tt>null</tt>。
     */
    public StackTraceElement getLockedStackFrame() {
        return stackFrame;
    }

    /**
     * 返回由给定 <tt>CompositeData</tt> 表示的 <tt>MonitorInfo</tt> 对象。
     * 给定的 <tt>CompositeData</tt> 必须包含以下属性以及
     * <a href="LockInfo.html#MappedType">
     * 映射类型</a> 中指定的 {@link LockInfo} 类的属性：
     * <blockquote>
     * <table border summary="给定 CompositeData 包含的属性及其类型">
     * <tr>
     *   <th align=left>属性名</th>
     *   <th align=left>类型</th>
     * </tr>
     * <tr>
     *   <td>lockedStackFrame</td>
     *   <td><tt>CompositeData，如 {@link ThreadInfo#from
     *       ThreadInfo.from} 方法中定义的 <a href="ThreadInfo.html#StackTrace">stackTrace</a>
     *       属性中指定的那样。
     *       </tt></td>
     * </tr>
     * <tr>
     *   <td>lockedStackDepth</td>
     *   <td><tt>java.lang.Integer</tt></td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * @param cd 表示 <tt>MonitorInfo</tt> 的 <tt>CompositeData</tt>
     *
     * @throws IllegalArgumentException 如果 <tt>cd</tt> 不表示具有上述属性的 <tt>MonitorInfo</tt>。
     *
     * @return 由 <tt>cd</tt> 表示的 <tt>MonitorInfo</tt> 对象，如果 <tt>cd</tt> 为 <tt>null</tt> 则返回 <tt>null</tt>。
     */
    public static MonitorInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof MonitorInfoCompositeData) {
            return ((MonitorInfoCompositeData) cd).getMonitorInfo();
        } else {
            MonitorInfoCompositeData.validateCompositeData(cd);
            String className = MonitorInfoCompositeData.getClassName(cd);
            int identityHashCode = MonitorInfoCompositeData.getIdentityHashCode(cd);
            int stackDepth = MonitorInfoCompositeData.getLockedStackDepth(cd);
            StackTraceElement stackFrame = MonitorInfoCompositeData.getLockedStackFrame(cd);
            return new MonitorInfo(className,
                                   identityHashCode,
                                   stackDepth,
                                   stackFrame);
        }
    }

}
