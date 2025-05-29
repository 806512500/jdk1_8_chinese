/*
 * 版权所有 (c) 2003, 2008, Oracle 和/或其关联公司。保留所有权利。
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
 * Java 虚拟机垃圾收集的管理接口。垃圾收集是 Java 虚拟机用来查找和回收不可达对象以释放内存空间的过程。垃圾收集器是一种
 * {@link MemoryManagerMXBean 内存管理器}。
 *
 * <p> 一个 Java 虚拟机可能有一个或多个此接口实现类的实例。
 * 实现此接口的实例是一个 <a href="ManagementFactory.html#MXBean">MXBean</a>，
 * 可以通过调用 {@link ManagementFactory#getGarbageCollectorMXBeans} 方法或
 * 从 {@link ManagementFactory#getPlatformMBeanServer 平台 <tt>MBeanServer</tt>} 方法中获取。
 *
 * <p>用于唯一识别 MBeanServer 中垃圾收集器 MXBean 的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *   {@link ManagementFactory#GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
 *    <tt>java.lang:type=GarbageCollector</tt>}<tt>,name=</tt><i>收集器的名称</i>
 * </blockquote>
 *
 * 可以通过调用 {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * 一个平台通常包括特定于垃圾收集算法的额外平台依赖信息，用于监控。
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
public interface GarbageCollectorMXBean extends MemoryManagerMXBean {
    /**
     * 返回已发生的垃圾收集总数。
     * 如果此收集器的收集计数未定义，此方法返回 <tt>-1</tt>。
     *
     * @return 已发生的垃圾收集总数。
     */
    public long getCollectionCount();

    /**
     * 返回近似的累积收集耗时（以毫秒为单位）。如果此收集器的收集耗时未定义，此方法返回 <tt>-1</tt>。
     * <p>
     * Java 虚拟机实现可能使用高分辨率计时器来测量耗时。即使收集计数已增加，如果收集耗时非常短，此方法也可能返回相同的值。
     *
     * @return 近似的累积收集耗时（以毫秒为单位）。
     */
    public long getCollectionTime();
}
