
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import javax.management.openmbean.CompositeData;

/**
 * Java 虚拟机内存系统的管理接口。
 *
 * <p> Java 虚拟机有一个此类实现的单个实例。此接口的实现是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getMemoryMXBean} 方法或
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法获得。
 *
 * <p>用于唯一标识 MBeanServer 内存系统 MXBean 的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#MEMORY_MXBEAN_NAME
 *           <tt>java.lang:type=Memory</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获得。
 *
 * <h3> 内存 </h3>
 * Java 虚拟机的内存系统管理以下类型的内存：
 *
 * <h3> 1. 堆 </h3>
 * Java 虚拟机有一个 <i>堆</i>，这是运行时数据区域，从中为所有类实例和数组分配内存。它在 Java 虚拟机启动时创建。
 * 堆中的对象内存由自动内存管理系统（称为 <i>垃圾收集器</i>）回收。
 *
 * <p>堆的大小可以是固定的，也可以扩展和缩小。
 * 堆内存不需要连续。
 *
 * <h3> 2. 非堆内存 </h3>
 * Java 虚拟机管理除堆以外的内存（称为 <i>非堆内存</i>）。
 *
 * <p> Java 虚拟机有一个 <i>方法区</i>，由所有线程共享。
 * 方法区属于非堆内存。它存储每个类的结构，如运行时常量池、字段和方法数据，以及方法和构造函数的代码。它在 Java 虚拟机启动时创建。
 *
 * <p>方法区在逻辑上是堆的一部分，但 Java 虚拟机实现可以选择不对其进行垃圾收集或压缩。与堆类似，方法区的大小可以是固定的，也可以扩展和缩小。方法区的内存不需要连续。
 *
 * <p>除了方法区，Java 虚拟机实现可能需要用于内部处理或优化的内存，这些内存也属于非堆内存。
 * 例如，JIT 编译器需要内存来存储从 Java 虚拟机代码翻译成的本地机器代码，以提高性能。
 *
 * <h3>内存池和内存管理器</h3>
 * {@link MemoryPoolMXBean 内存池} 和
 * {@link MemoryManagerMXBean 内存管理器} 是监控和管理 Java 虚拟机内存系统的抽象实体。
 *
 * <p>内存池表示 Java 虚拟机管理的内存区域。Java 虚拟机至少有一个内存池，并且在执行过程中可以创建或删除内存池。
 * 内存池可以属于堆或非堆内存。
 *
 * <p>内存管理器负责管理一个或多个内存池。垃圾收集器是一种内存管理器，负责回收不可达对象占用的内存。Java 虚拟机可能有一个或多个内存管理器。它可以在执行过程中添加或删除内存管理器。一个内存池可以由多个内存管理器管理。
 *
 * <h3>内存使用监控</h3>
 *
 * 内存使用是内存系统非常重要的监控属性。例如，内存使用可以指示：
 * <ul>
 *   <li>应用程序的内存使用情况，</li>
 *   <li>自动内存管理系统的工作负载，</li>
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
 * 详细信息在 {@link MemoryPoolMXBean} 接口中指定。
 *
 * <p>内存使用监控机制旨在用于负载平衡或工作负载分配。例如，当应用程序的内存使用超过某个阈值时，它将停止接收任何新的工作负载。它不用于应用程序检测和恢复低内存条件。
 *
 * <h3>通知</h3>
 *
 * <p>此 <tt>MemoryMXBean</tt> 是一个
 * {@link javax.management.NotificationEmitter NotificationEmitter}
 * 如果任何一个内存池支持 <a href="MemoryPoolMXBean.html#UsageThreshold">使用阈值</a>
 * 或 <a href="MemoryPoolMXBean.html#CollectionThreshold">收集使用阈值</a>，则会发出两种类型的内存 {@link javax.management.Notification
 * 通知}，这可以通过调用
 * {@link MemoryPoolMXBean#isUsageThresholdSupported} 和
 * {@link MemoryPoolMXBean#isCollectionUsageThresholdSupported} 方法确定。
 * <ul>
 *   <li>{@link MemoryNotificationInfo#MEMORY_THRESHOLD_EXCEEDED
 *       使用阈值超过通知} - 用于通知内存池的内存使用增加并达到或超过其
 *       <a href="MemoryPoolMXBean.html#UsageThreshold"> 使用阈值</a> 值。
 *       </li>
 *   <li>{@link MemoryNotificationInfo#MEMORY_COLLECTION_THRESHOLD_EXCEEDED
 *       收集使用阈值超过通知} - 用于通知内存池的内存使用在 Java 虚拟机
 *       在该内存池中回收未使用对象后大于或等于其
 *       <a href="MemoryPoolMXBean.html#CollectionThreshold">
 *       收集使用阈值</a>。</li>
 * </ul>
 *
 * <p>
 * 发出的通知是一个 {@link javax.management.Notification}
 * 实例，其 {@link javax.management.Notification#setUserData
 * 用户数据} 被设置为一个 {@link CompositeData CompositeData}
 * 表示一个 {@link MemoryNotificationInfo} 对象，其中包含构建通知时内存池的信息。该 <tt>CompositeData</tt> 包含
 * 如 {@link MemoryNotificationInfo#from
 * MemoryNotificationInfo} 中所述的属性。
 *
 * <hr>
 * <h3>NotificationEmitter</h3>
 * 通过 {@link ManagementFactory#getMemoryMXBean} 返回的
 * <tt>MemoryMXBean</tt> 对象实现了
 * {@link javax.management.NotificationEmitter NotificationEmitter}
 * 接口，允许在 <tt>MemoryMXBean</tt> 内注册一个监听器作为通知监听器。
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
 *      JMX 规范。</a>
 * @see <a href="package-summary.html#examples">
 *      访问 MXBeans 的方式</a>
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
     * 返回用于对象分配的堆的当前内存使用情况。堆由一个或多个内存池组成。
     * 返回的内存使用情况中的<tt>已用</tt>和<tt>已提交</tt>大小是所有堆内存池的这些值的总和，
     * 而返回的内存使用情况中的<tt>初始</tt>和<tt>最大</tt>大小表示堆内存的设置，这可能不是所有堆
     * 内存池的这些值的总和。
     * <p>
     * 返回的内存使用情况中的已用内存量是被活动对象和尚未被收集的垃圾对象（如果有）占用的内存量。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * <tt>MemoryUsage</tt>的映射类型是
     * <tt>CompositeData</tt>，其属性如
     * {@link MemoryUsage#from MemoryUsage}中所指定。
     *
     * @return 一个表示堆内存使用情况的{@link MemoryUsage}对象。
     */
    public MemoryUsage getHeapMemoryUsage();

    /**
     * 返回Java虚拟机使用的非堆内存的当前内存使用情况。
     * 非堆内存由一个或多个内存池组成。
     * 返回的内存使用情况中的<tt>已用</tt>和<tt>已提交</tt>大小是所有非堆内存池的这些值的总和，
     * 而返回的内存使用情况中的<tt>初始</tt>和<tt>最大</tt>大小表示非堆
     * 内存的设置，这可能不是所有非堆
     * 内存池的这些值的总和。
     *
     * <p>
     * <b>MBeanServer访问</b>：<br>
     * <tt>MemoryUsage</tt>的映射类型是
     * <tt>CompositeData</tt>，其属性如
     * {@link MemoryUsage#from MemoryUsage}中所指定。
     *
     * @return 一个表示非堆内存使用情况的{@link MemoryUsage}对象。
     */
    public MemoryUsage getNonHeapMemoryUsage();

    /**
     * 测试内存系统的详细输出是否已启用。
     *
     * @return 如果内存系统的详细输出已启用，则返回<tt>true</tt>；否则返回<tt>false</tt>。
     */
    public boolean isVerbose();

    /**
     * 启用或禁用内存系统的详细输出。详细输出信息和详细信息输出的流是实现依赖的。
     * 通常，Java虚拟机实现会在每次进行垃圾收集时释放内存时打印一条消息。
     *
     * <p>
     * 该方法的每次调用都会全局启用或禁用详细输出。
     *
     * @param value <tt>true</tt>启用详细输出；
     *              <tt>false</tt>禁用。
     *
     * @exception  java.lang.SecurityException 如果存在安全管理器
     *             并且调用者没有
     *             ManagementPermission("control")。
     */
    public void setVerbose(boolean value);

    /**
     * 运行垃圾收集器。
     * 调用<code>gc()</code>实际上等效于调用：
     * <blockquote><pre>
     * System.gc()
     * </pre></blockquote>
     *
     * @see     java.lang.System#gc()
     */
    public void gc();

}
