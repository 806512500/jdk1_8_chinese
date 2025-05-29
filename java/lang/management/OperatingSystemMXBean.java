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
 * 操作系统管理接口，用于管理Java虚拟机运行的操作系统。
 *
 * <p>Java虚拟机有一个此类实现的单个实例。此接口的实现是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getOperatingSystemMXBean} 方法或
 * 从 {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法中获取。
 *
 * <p>用于在MBeanServer中唯一标识操作系统的MXBean的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#OPERATING_SYSTEM_MXBEAN_NAME
 *      <tt>java.lang:type=OperatingSystem</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * <p>此接口定义了几个方便的方法，用于访问Java
 * 虚拟机运行的操作系统的系统属性。
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
public interface OperatingSystemMXBean extends PlatformManagedObject {
    /**
     * 返回操作系统名称。
     * 此方法等同于 <tt>System.getProperty("os.name")</tt>。
     *
     * @return 操作系统名称。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getName();

    /**
     * 返回操作系统架构。
     * 此方法等同于 <tt>System.getProperty("os.arch")</tt>。
     *
     * @return 操作系统架构。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getArch();

    /**
     * 返回操作系统版本。
     * 此方法等同于 <tt>System.getProperty("os.version")</tt>。
     *
     * @return 操作系统版本。
     *
     * @throws  java.lang.SecurityException
     *     如果存在安全经理，并且其
     *     <code>checkPropertiesAccess</code> 方法不允许访问
     *     此系统属性。
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see java.lang.System#getProperty
     */
    public String getVersion();

    /**
     * 返回Java虚拟机可用的处理器数量。
     * 此方法等同于 {@link Runtime#availableProcessors()} 方法。
     * <p> 此值在虚拟机的特定调用期间可能会发生变化。
     *
     * @return 虚拟机可用的处理器数量；永远不会小于一。
     */
    public int getAvailableProcessors();

    /**
     * 返回过去一分钟的系统负载平均值。
     * 系统负载平均值是可运行实体排队到
     * {@linkplain #getAvailableProcessors 可用处理器} 的数量和
     * 在可用处理器上运行的可运行实体的数量在一段时间内的平均值。
     * 负载平均值的计算方式是操作系统特定的，但通常是一个阻尼的时间依赖平均值。
     * <p>
     * 如果负载平均值不可用，则返回负值。
     * <p>
     * 此方法旨在提供系统负载的提示，并且可以频繁查询。
     * 在某些平台上，实现此方法可能代价高昂，因此负载平均值可能不可用。
     *
     * @return 系统负载平均值；如果不可用，则返回负值。
     *
     * @since 1.6
     */
    public double getSystemLoadAverage();
}
