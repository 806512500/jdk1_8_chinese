/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Java虚拟机内存系统的管理接口。
 *
 * <p> Java虚拟机有一个该接口的实现类的单个实例。此接口的实现类是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getMemoryMXBean} 方法或
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法获取。
 *
 * <p>用于在MBeanServer中唯一标识内存系统MXBean的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#MEMORY_MXBEAN_NAME
 *           <tt>java.lang:type=Memory</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * <h3>内存</h3>
 * Java虚拟机的内存系统管理以下几种内存：
 *
 * <h3>1. 堆</h3>
 * Java虚拟机有一个 <i>堆</i>，这是运行时数据区，从中分配所有类实例和数组的内存。堆在Java虚拟机启动时创建。
 * 对象的堆内存由自动内存管理系统（称为 <i>垃圾收集器</i>）回收。
 *
 * <p>堆的大小可以是固定的，也可以扩展和收缩。堆内存不需要是连续的。
 *
 * <h3>2. 非堆内存</h3>
 * Java虚拟机管理除堆之外的内存（称为 <i>非堆内存</i>）。
 *
 * <p> Java虚拟机有一个 <i>方法区</i>，由所有线程共享。方法区属于非堆内存。它存储每个类的结构，
 * 如运行时常量池、字段和方法数据，以及方法和构造器的代码。方法区在Java虚拟机启动时创建。
 *
 * <p>方法区在逻辑上是堆的一部分，但Java虚拟机实现可以选择不对其进行垃圾收集或压缩。与堆类似，方法区的大小可以是固定的，也可以扩展和收缩。方法区的内存不需要是连续的。
 *
 * <p>除了方法区外，Java虚拟机实现可能需要用于内部处理或优化的内存，这些内存也属于非堆内存。
 * 例如，JIT编译器需要内存来存储从Java虚拟机代码翻译成的本地机器代码，以提高性能。
 *
 * <h3>内存池和内存管理器</h3>
 * {@link MemoryPoolMXBean 内存池} 和
 * {@link MemoryManagerMXBean 内存管理器} 是监视和管理Java虚拟机内存系统的抽象实体。
 *
 * <p>内存池表示Java虚拟机管理的内存区域。Java虚拟机至少有一个内存池，并且在执行过程中可以创建或删除内存池。
 * 内存池可以属于堆或非堆内存。
 *
 * <p>内存管理器负责管理一个或多个内存池。垃圾收集器是一种内存管理器，负责回收不可达对象占用的内存。Java虚拟机可能有一个或多个内存管理器。在执行过程中，它可以
 * 添加或删除内存管理器。一个内存池可以由多个内存管理器管理。
 *
 * <h3>内存使用监控</h3>
 *
 * 内存使用是内存系统非常重要的监控属性。例如，内存使用可以指示：
 * <ul>
 *   <li>应用程序的内存使用情况，</li>
 *   <li>自动内存管理系统上的工作负载，</li>
 *   <li>潜在的内存泄漏。</li>
 * </ul>
 *
 * <p>
 * 内存使用可以通过三种方式监控：
 * <ul>
 *   <li>轮询</li>
 *   <li>使用阈值通知</li>
 *   <li>收集使用阈值通知</li>
 * </ul>
 *
 * 详细信息请参见 {@link MemoryPoolMXBean} 接口。
 *
 * <p>内存使用监控机制旨在用于负载平衡或工作负载分布。例如，当应用程序的内存使用超过
 * 某个阈值时，它将停止接收任何新的工作负载。它不旨在让应用程序检测和恢复低内存条件。
 *
 * <h3>通知</h3>
 *
 * <p>此 <tt>MemoryMXBean</tt> 是一个
 * {@link javax.management.NotificationEmitter NotificationEmitter}
 * 如果任何一个内存池支持 <a href="MemoryPoolMXBean.html#UsageThreshold">使用阈值</a>
 * 或 <a href="MemoryPoolMXBean.html#CollectionThreshold">收集使用阈值</a>，则可以发出两种类型的内存
 * {@link javax.management.Notification 通知}，这可以通过调用
 * {@link MemoryPoolMXBean#isUsageThresholdSupported} 和
 * {@link MemoryPoolMXBean#isCollectionUsageThresholdSupported} 方法确定。
 * <ul>
 *   <li>{@link MemoryNotificationInfo#MEMORY_THRESHOLD_EXCEEDED
 *       使用阈值超过通知} - 用于通知内存池的内存使用增加并达到或超过其
 *       <a href="MemoryPoolMXBean.html#UsageThreshold"> 使用阈值</a> 值。
 *       </li>
 *   <li>{@link MemoryNotificationInfo#MEMORY_COLLECTION_THRESHOLD_EXCEEDED
 *       收集使用阈值超过通知} - 用于通知在Java虚拟机尝试回收该内存池中的未使用对象后，
 *       内存池的内存使用大于或等于其
 *       <a href="MemoryPoolMXBean.html#CollectionThreshold">
 *       收集使用阈值</a>。</li>
 * </ul>
 *
 * <p>
 * 发出的通知是一个 {@link javax.management.Notification}
 * 实例，其 {@link javax.management.Notification#setUserData
 * 用户数据} 设置为一个 {@link CompositeData CompositeData}
 * 表示一个 {@link MemoryNotificationInfo} 对象，包含构建通知时内存池的信息。
 * <tt>CompositeData</tt> 包含的属性如 {@link MemoryNotificationInfo#from
 * MemoryNotificationInfo} 中所述。
 *
 * <hr>
 * <h3>NotificationEmitter</h3>
 * 通过 {@link ManagementFactory#getMemoryMXBean} 返回的
 * <tt>MemoryMXBean</tt> 对象实现了
 * {@link javax.management.NotificationEmitter NotificationEmitter}
 * 接口，允许在 <tt>MemoryMXBean</tt> 中注册一个监听器作为通知监听器。
 *
 * 下面是一个示例代码，注册一个 <tt>MyListener</tt> 来处理
 * <tt>MemoryMXBean</tt> 发出的通知。
 *
 * <blockquote><pre>
 * class MyListener implements javax.management.NotificationListener {
 *     public void handleNotification(Notification notif, Object handback) {
 *         // 处理通知
 *         ....
 *     }
 * }
 *
 * MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
 * NotificationEmitter emitter = (NotificationEmitter) mbean;
 * MyListener listener = new MyListener();
 * emitter.addNotificationListener(listener, null, null);
 * </pre></blockquote>
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
public interface MemoryMXBean extends PlatformManagedObject {
    /**
     * 返回待终结对象的近似数量。
     *
     * @return 待终结对象的近似数量。
     */
    public int getObjectPendingFinalizationCount();

    /**
     * 返回用于对象分配的堆的当前内存使用情况。堆由一个或多个内存池组成。返回的内存使用情况中的
     * <tt>used</tt> 和 <tt>committed</tt> 大小是所有堆内存池的这些值的总和，而返回的内存使用情况中的
     * <tt>init</tt> 和 <tt>max</tt> 大小表示堆内存的设置，可能不是所有堆内存池的这些值的总和。
     * <p>
     * 返回的内存使用情况中的已用内存量是被活动对象和尚未收集的垃圾对象（如果有）占用的内存量。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * <tt>MemoryUsage</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link MemoryUsage#from MemoryUsage} 中所述。
     *
     * @return 一个表示堆内存使用情况的 {@link MemoryUsage} 对象。
     */
    public MemoryUsage getHeapMemoryUsage();

    /**
     * 返回Java虚拟机使用的非堆内存的当前内存使用情况。非堆内存由一个或多个内存池组成。返回的内存使用情况中的
     * <tt>used</tt> 和 <tt>committed</tt> 大小是所有非堆内存池的这些值的总和，而返回的内存使用情况中的
     * <tt>init</tt> 和 <tt>max</tt> 大小表示非堆内存的设置，可能不是所有非堆内存池的这些值的总和。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * <tt>MemoryUsage</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link MemoryUsage#from MemoryUsage} 中所述。
     *
     * @return 一个表示非堆内存使用情况的 {@link MemoryUsage} 对象。
     */
    public MemoryUsage getNonHeapMemoryUsage();

    /**
     * 测试内存系统的详细输出是否已启用。
     *
     * @return 如果内存系统的详细输出已启用，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
     */
    public boolean isVerbose();

    /**
     * 启用或禁用内存系统的详细输出。详细输出信息和详细信息输出的输出流是实现依赖的。通常，Java虚拟机实现
     * 在垃圾收集时释放内存时会打印一条消息。
     *
     * <p>
     * 每次调用此方法都会全局启用或禁用详细输出。
     *
     * @param value <tt>true</tt> 以启用详细输出；<tt>false</tt> 以禁用。
     *
     * @exception  java.lang.SecurityException 如果存在安全管理器且调用者没有
     *             ManagementPermission("control") 权限。
     */
    public void setVerbose(boolean value);

    /**
     * 运行垃圾收集器。
     * 调用 <code>gc()</code> 实际上等效于调用：
     * <blockquote><pre>
     * System.gc()
     * </pre></blockquote>
     *
     * @see     java.lang.System#gc()
     */
    public void gc();

}
