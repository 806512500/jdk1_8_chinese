/*
 * 版权所有 (c) 2003, 2008, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * Java 虚拟机的类加载系统的管理接口。
 *
 * <p>Java 虚拟机有一个此类实现的单个实例。此接口的实现是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getClassLoadingMXBean} 方法或从
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 获取。
 *
 * <p>用于在 <tt>MBeanServer</tt> 中唯一标识类加载系统 MXBean 的 <tt>ObjectName</tt> 是：
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
 *      JMX 规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问 MXBeans 的方法</a>
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public interface ClassLoadingMXBean extends PlatformManagedObject {

    /**
     * 返回自 Java 虚拟机启动执行以来已加载的类的总数。
     *
     * @return 已加载的类的总数。
     *
     */
    public long getTotalLoadedClassCount();

    /**
     * 返回当前在 Java 虚拟机中加载的类的数量。
     *
     * @return 当前加载的类的数量。
     */
    public int getLoadedClassCount();

    /**
     * 返回自 Java 虚拟机启动执行以来已卸载的类的总数。
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
     * 通常，Java 虚拟机实现每次加载类文件时都会打印一条消息。
     *
     * <p>此方法可以由多个线程并发调用。每次调用此方法都会全局启用或禁用详细输出。
     *
     * @param value <tt>true</tt> 以启用详细输出；
     *              <tt>false</tt> 以禁用。
     *
     * @exception  java.lang.SecurityException 如果存在安全管理器
     *             并且调用者没有
     *             ManagementPermission("control")。
     */
    public void setVerbose(boolean value);

}
