/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Java虚拟机的编译系统的管理接口。
 *
 * <p>Java虚拟机有一个此类实现的单个实例。此接口的实现是一个<a href="ManagementFactory.html#MXBean">MXBean</a>，
 * 可以通过调用{@link ManagementFactory#getCompilationMXBean}方法或
 * {@link ManagementFactory#getPlatformMBeanServer 平台<tt>MBeanServer</tt>}方法获得。
 *
 * <p>用于在MBeanServer中唯一标识编译系统MXBean的<tt>ObjectName</tt>为：
 * <blockquote>
 *  {@link ManagementFactory#COMPILATION_MXBEAN_NAME
 *         <tt>java.lang:type=Compilation</tt>}
 * </blockquote>
 *
 * 可以通过调用{@link PlatformManagedObject#getObjectName}方法获得。
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
public interface CompilationMXBean extends PlatformManagedObject {
    /**
     * 返回即时（JIT）编译器的名称。
     *
     * @return JIT编译器的名称。
     */
    public java.lang.String    getName();

    /**
     * 测试Java虚拟机是否支持编译时间的监控。
     *
     * @return 如果支持编译时间监控则返回<tt>true</tt>；否则返回<tt>false</tt>。
     */
    public boolean isCompilationTimeMonitoringSupported();

    /**
     * 返回编译所花费的大致累积时间（以毫秒为单位）。
     * 如果使用多个线程进行编译，此值是每个线程编译所花费的大致时间的总和。
     *
     * <p>此方法由平台可选支持。
     * Java虚拟机实现可能不支持编译时间监控。可以使用{@link #isCompilationTimeMonitoringSupported}
     * 方法来确定Java虚拟机是否支持此操作。
     *
     * <p>此值不表示Java虚拟机的性能水平，也不用于不同虚拟机实现的性能比较。
     * 实现可能对编译时间有不同的定义和不同的测量方法。
     *
     * @return 编译时间（以毫秒为单位）
     * @throws java.lang.UnsupportedOperationException 如果Java
     * 虚拟机不支持此操作。
     *
     */
    public long                getTotalCompilationTime();
}
