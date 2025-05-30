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
 * Java虚拟机的垃圾收集管理接口。垃圾收集是Java虚拟机用于查找和回收不可达对象以释放内存空间的过程。垃圾收集器是一种类型的
 * {@link MemoryManagerMXBean 内存管理器}。
 *
 * <p>Java虚拟机可能有一个或多个此接口的实现类实例。
 * 实现此接口的实例是一个<a href="ManagementFactory.html#MXBean">MXBean</a>，
 * 可以通过调用
 * {@link ManagementFactory#getGarbageCollectorMXBeans} 方法或
 * 从 {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法中获取。
 *
 * <p>用于在MBeanServer中唯一标识垃圾收集器MXBean的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *   {@link ManagementFactory#GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
 *    <tt>java.lang:type=GarbageCollector</tt>}<tt>,name=</tt><i>收集器的名称</i>
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * 一个平台通常包括特定于垃圾收集算法的额外平台依赖信息，用于监控。
 *
 * @see ManagementFactory#getPlatformMXBeans(Class)
 * @see MemoryMXBean
 *
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问MXBeans的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface GarbageCollectorMXBean extends MemoryManagerMXBean {
    /**
     * 返回已发生的收集次数。
     * 如果此收集器的收集次数未定义，此方法返回 <tt>-1</tt>。
     *
     * @return 已发生的收集次数。
     */
    public long getCollectionCount();

    /**
     * 返回近似的累积收集耗时（以毫秒为单位）。
     * 如果此收集器的收集耗时未定义，此方法返回 <tt>-1</tt>。
     * <p>
     * Java虚拟机实现可能使用高分辨率计时器来测量耗时。
     * 即使收集次数已增加，如果收集耗时非常短，此方法也可能返回相同的值。
     *
     * @return 近似的累积收集耗时（以毫秒为单位）。
     */
    public long getCollectionTime();


}
