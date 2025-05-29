/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.lang.management;

/**
 * 内存管理器的管理接口。
 * 一个内存管理器管理 Java 虚拟机的一个或多个内存池。
 *
 * <p> Java 虚拟机有一个或多个内存管理器。
 * 实现此接口的实例是一个 <a href="ManagementFactory.html#MXBean">MXBean</a>，
 * 可以通过调用 {@link ManagementFactory#getMemoryManagerMXBeans} 方法或
 * 从 {@link ManagementFactory#getPlatformMBeanServer 平台 <tt>MBeanServer</tt>} 方法中获取。
 *
 * <p>用于在 MBeanServer 内唯一标识内存管理器 MXBean 的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *   {@link ManagementFactory#MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE
 *    <tt>java.lang:type=MemoryManager</tt>}<tt>,name=</tt><i>管理器的名称</i>
 * </blockquote>
 *
 * 可以通过调用 {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * @see ManagementFactory#getPlatformMXBeans(Class)
 * @see MemoryMXBean
 *
 * @see <a href="../../../javax/management/package-summary.html">
 *      JMX 规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问 MXBeans 的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface MemoryManagerMXBean extends PlatformManagedObject {
    /**
     * 返回表示此内存管理器的名称。
     *
     * @return 此内存管理器的名称。
     */
    public String getName();

    /**
     * 测试此内存管理器在 Java 虚拟机中是否有效。 一旦 Java 虚拟机从内存系统中移除此内存管理器，它就变得无效。
     *
     * @return <tt>true</tt> 如果内存管理器在 Java 虚拟机中有效；
     *         <tt>false</tt> 否则。
     */
    public boolean isValid();

    /**
     * 返回此内存管理器管理的内存池的名称。
     *
     * @return 一个 <tt>String</tt> 对象数组，每个对象是此内存管理器管理的内存池的名称。
     */
    public String[] getMemoryPoolNames();
}
