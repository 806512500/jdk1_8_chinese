/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Java虚拟机的类加载系统的管理接口。
 *
 * <p>Java虚拟机有一个此类实现的单个实例。此接口的实现是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getClassLoadingMXBean} 方法或从
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 获取。
 *
 * <p>用于在 <tt>MBeanServer</tt> 中唯一标识类加载系统MXBean的 <tt>ObjectName</tt> 是：
 * <blockquote>
 * {@link ManagementFactory#CLASS_LOADING_MXBEAN_NAME
 *        <tt>java.lang:type=ClassLoading</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * @see ManagementFactory#getPlatformMXBeans(Class)
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问MXBeans的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface ClassLoadingMXBean extends PlatformManagedObject {

    /**
     * 返回自Java虚拟机启动以来已加载的类的总数。
     *
     * @return 已加载的类的总数。
     *
     */
    public long getTotalLoadedClassCount();

    /**
     * 返回当前在Java虚拟机中加载的类的数量。
     *
     * @return 当前加载的类的数量。
     */
    public int getLoadedClassCount();

    /**
     * 返回自Java虚拟机启动以来已卸载的类的总数。
     *
     * @return 已卸载的类的总数。
     */
    public long getUnloadedClassCount();

    /**
     * 测试类加载系统的详细输出是否已启用。
     *
     * @return 如果类加载系统的详细输出已启用，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
     */
    public boolean isVerbose();

    /**
     * 启用或禁用类加载系统的详细输出。详细输出信息和详细信息输出的流是实现依赖的。
     * 通常，Java虚拟机实现每次加载类文件时都会打印一条消息。
     *
     * <p>此方法可以由多个线程并发调用。每次调用此方法都会全局启用或禁用详细输出。
     *
     * @param value <tt>true</tt> 启用详细输出；<tt>false</tt> 禁用。
     *
     * @exception  java.lang.SecurityException 如果存在安全经理且调用者没有
     *             ManagementPermission("control") 权限。
     */
    public void setVerbose(boolean value);

}
