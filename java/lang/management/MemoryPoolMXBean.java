
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
 * 管理接口用于内存池。内存池表示由Java虚拟机管理的内存资源，并由一个或多个{@link MemoryManagerMXBean 内存管理器}管理。
 *
 * <p> Java虚拟机有一个或多个此接口实现类的实例。实现此接口的实例是一个<a href="ManagementFactory.html#MXBean">MXBean</a>，
 * 可以通过调用{@link ManagementFactory#getMemoryPoolMXBeans}方法或从{@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>}方法中获取。
 *
 * <p>用于在<tt>MBeanServer</tt>中唯一标识内存池MXBean的<tt>ObjectName</tt>为：
 * <blockquote>
 *    {@link ManagementFactory#MEMORY_POOL_MXBEAN_DOMAIN_TYPE
 *    <tt>java.lang:type=MemoryPool</tt>}<tt>,name=</tt><i>内存池的名称</i>
 * </blockquote>
 *
 * 可以通过调用{@link PlatformManagedObject#getObjectName}方法获取。
 *
 * <h3>内存类型</h3>
 * <p>Java虚拟机有一个用于对象分配的堆，还维护用于方法区和Java虚拟机执行的非堆内存。Java虚拟机可以有一个或多个内存池。每个内存池表示以下类型之一的内存区域：
 * <ul>
 *   <li>{@link MemoryType#HEAP 堆}</li>
 *   <li>{@link MemoryType#NON_HEAP 非堆}</li>
 * </ul>
 *
 * <h3>内存使用监控</h3>
 *
 * 内存池具有以下属性：
 * <ul>
 *   <li><a href="#Usage">内存使用</a></li>
 *   <li><a href="#PeakUsage">峰值内存使用</a></li>
 *   <li><a href="#UsageThreshold">使用阈值</a></li>
 *   <li><a href="#CollectionThreshold">收集使用阈值</a>
 *       （仅由某些<em>垃圾收集</em>内存池支持）</li>
 * </ul>
 *
 * <h3><a name="Usage">1. 内存使用</a></h3>
 *
 * {@link #getUsage}方法提供内存池当前使用情况的估计值。对于垃圾收集的内存池，使用的内存量包括池中所有对象占用的内存，包括<em>可达</em>和<em>不可达</em>的对象。
 *
 * <p>通常，此方法是获取近似内存使用情况的轻量级操作。对于某些内存池，例如，当对象不是连续存储时，此方法可能是一个需要计算当前内存使用情况的昂贵操作。实现应记录这种情况。
 *
 * <h3><a name="PeakUsage">2. 峰值内存使用</a></h3>
 *
 * Java虚拟机自启动或峰值重置以来维护内存池的峰值内存使用情况。峰值内存使用情况由{@link #getPeakUsage}方法返回，并通过调用{@link #resetPeakUsage}方法重置。
 *
 * <h3><a name="UsageThreshold">3. 使用阈值</a></h3>
 *
 * 每个内存池都有一个可管理的属性称为<i>使用阈值</i>，其默认值由Java虚拟机提供。默认值是平台相关的。使用阈值可以通过{@link #setUsageThreshold setUsageThreshold}方法设置。
 * 如果阈值设置为正值，则启用此内存池的使用阈值跨越检查。如果使用阈值设置为零，则禁用此内存池的使用阈值跨越检查。可以使用{@link MemoryPoolMXBean#isUsageThresholdSupported}方法确定是否支持此功能。
 * <p>
 * Java虚拟机在最合适的时间对内存池进行使用阈值跨越检查，通常是在垃圾收集时间。每个内存池维护一个{@link #getUsageThresholdCount 使用阈值计数}，每当Java虚拟机检测到内存池使用情况跨越阈值时，该计数将递增。
 * <p>
 * 此可管理的使用阈值属性设计用于低开销地监控内存使用情况的增加趋势。使用阈值可能不适合某些内存池。例如，分代垃圾收集器，许多Java虚拟机实现中常用的一种垃圾收集算法，管理两个或更多代，按对象的年龄分隔对象。
 * 大多数对象被分配到<em>最年轻的一代</em>（例如，一个托儿所内存池）。托儿所内存池设计为被填满，收集托儿所内存池将释放其大部分内存空间，因为预计它包含的大多是短生命周期的对象，在垃圾收集时大多是不可达的。
 * 在这种情况下，托儿所内存池支持使用阈值不太合适。此外，如果一个内存池中的对象分配成本非常低（例如，仅是原子指针交换），Java虚拟机可能不会支持该内存池的使用阈值，因为与对象分配成本相比，比较使用情况与阈值的开销更高。
 *
 * <p>
 * 可以使用<a href="#Polling">轮询</a>或<a href="#ThresholdNotification">阈值通知</a>机制监控系统的内存使用情况。
 *
 * <ol type="a">
 *   <li><a name="Polling"><b>轮询</b></a>
 *       <p>
 *       应用程序可以通过调用所有内存池的{@link #getUsage}方法或支持使用阈值的内存池的{@link #isUsageThresholdExceeded}方法来连续监控其内存使用情况。下面是一个示例代码，其中有一个线程专用于任务分发和处理。每隔一段时间，它将根据内存使用情况确定是否应该接收和处理新任务。如果内存使用情况超过其使用阈值，它将重新分配所有未完成的任务到其他VM，并停止接收新任务，直到内存使用情况返回到其使用阈值以下。
 *
 *       <pre>
 *       // 假设此池支持使用阈值。
 *       // 将阈值设置为myThreshold，超过此阈值不应接受新任务。
 *       pool.setUsageThreshold(myThreshold);
 *       ....
 *
 *       boolean lowMemory = false;
 *       while (true) {
 *          if (pool.isUsageThresholdExceeded()) {
 *              // 潜在的低内存，因此将任务重新分配到其他VM
 *              lowMemory = true;
 *              redistributeTasks();
 *              // 停止接收新任务
 *              stopReceivingTasks();
 *          } else {
 *              if (lowMemory) {
 *                  // 恢复接收任务
 *                  lowMemory = false;
 *                  resumeReceivingTasks();
 *              }
 *              // 处理未完成的任务
 *              ...
 *          }
 *          // 睡眠一段时间
 *          try {
 *              Thread.sleep(sometime);
 *          } catch (InterruptedException e) {
 *              ...
 *          }
 *       }
 *       </pre>
 *
 * <hr>
 *       上面的示例没有区分内存使用情况暂时低于使用阈值的情况与内存使用情况在两次迭代之间保持高于阈值的情况。由{@link #getUsageThresholdCount}方法返回的使用阈值计数可以用于确定两次轮询之间内存使用情况是否已返回到阈值以下。
 *       <p>
 *       下面显示了另一个示例，如果内存池处于低内存状态，将采取某些行动，并忽略行动处理时间期间的内存使用情况变化。
 *
 *       <pre>
 *       // 假设此池支持使用阈值。
 *       // 将阈值设置为myThreshold，确定应用程序在低内存条件下是否采取某些行动。
 *       pool.setUsageThreshold(myThreshold);
 *
 *       int prevCrossingCount = 0;
 *       while (true) {
 *           // 忙循环以检测内存使用情况是否已超过阈值。
 *           while (!pool.isUsageThresholdExceeded() ||
 *                  pool.getUsageThresholdCount() == prevCrossingCount) {
 *               try {
 *                   Thread.sleep(sometime)
 *               } catch (InterruptException e) {
 *                   ....
 *               }
 *           }
 *
 *           // 执行某些处理，例如检查内存使用情况并发出警告
 *           ....
 *
 *           // 获取当前阈值计数。忙循环将忽略处理期间发生的任何阈值跨越。
 *           prevCrossingCount = pool.getUsageThresholdCount();
 *       }
 *       </pre><hr>
 *   </li>
 *   <li><a name="ThresholdNotification"><b>使用阈值通知</b></a>
 *       <p>
 *       使用阈值通知将由{@link MemoryMXBean}发出。当Java虚拟机检测到内存池的内存使用情况达到或超过使用阈值时，虚拟机将触发<tt>MemoryMXBean</tt>发出一个
 *       {@link MemoryNotificationInfo#MEMORY_THRESHOLD_EXCEEDED 使用阈值超过通知}。直到使用情况降至阈值以下并再次超过阈值之前，不会生成另一个使用阈值超过通知。
 *       <p>
 *       下面是一个示例代码，实现了与上面第一个示例相同的逻辑，但使用使用阈值通知机制检测低内存条件，而不是轮询。在此示例代码中，接收到通知后，通知监听器通知另一个线程执行实际操作，例如重新分配未完成的任务、停止接收任务或恢复接收任务。
 *       <tt>handleNotification</tt>方法应设计为执行最少的工作并立即返回，以避免延迟后续通知的传递。耗时的操作应由单独的线程执行。通知监听器可能由多个线程并发调用；因此，监听器执行的任务应适当同步。
 *
 *       <pre>
 *       class MyListener implements javax.management.NotificationListener {
 *            public void handleNotification(Notification notification, Object handback)  {
 *                String notifType = notification.getType();
 *                if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
 *                    // 潜在的低内存，通知另一个线程将未完成的任务重新分配到其他VM
 *                    // 并停止接收新任务。
 *                    lowMemory = true;
 *                    notifyAnotherThread(lowMemory);
 *                }
 *            }
 *       }
 *
 *       // 将MyListener注册到MemoryMXBean
 *       MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
 *       NotificationEmitter emitter = (NotificationEmitter) mbean;
 *       MyListener listener = new MyListener();
 *       emitter.addNotificationListener(listener, null, null);
 *
 *       // 假设此池支持使用阈值。
 *       // 将阈值设置为myThreshold，超过此阈值不应接受新任务。
 *       pool.setUsageThreshold(myThreshold);
 *
 *       // 启用了使用阈值检测，通知将由MyListener处理。继续其他处理。
 *       ....
 *
 *       </pre>
 * <hr>
 *       <p>
 *       没有保证<tt>MemoryMXBean</tt>何时会发出阈值通知，以及通知何时会被传递。当通知监听器被调用时，内存池的内存使用情况可能已多次跨越使用阈值。
 *       {@link MemoryNotificationInfo#getCount}方法返回在构建通知时内存使用情况跨越使用阈值的次数。可以将其与由{@link #getUsageThresholdCount}方法返回的当前使用阈值计数进行比较，以确定是否发生了这种情况。
 *   </li>
 * </ol>
 *
 * <h3><a name="CollectionThreshold">4. 收集使用阈值</a></h3>
 *
 * 收集使用阈值是一个可管理的属性，仅适用于某些垃圾收集的内存池。在Java虚拟机通过在垃圾收集时间回收内存池中的未使用对象来回收内存空间后，某些字节数量的内存池仍将被使用。收集使用阈值允许为此字节数量设置一个值，如果超过该阈值，将由{@link MemoryMXBean}发出一个
 * {@link MemoryNotificationInfo#MEMORY_THRESHOLD_EXCEEDED 收集使用阈值超过通知}。此外，{@link #getCollectionUsageThresholdCount 收集使用阈值计数}将递增。
 *
 * <p>
 * 可以使用{@link MemoryPoolMXBean#isCollectionUsageThresholdSupported}方法确定是否支持此功能。
 *
 * <p>
 * Java虚拟机在内存池基础上执行收集使用阈值检查。如果收集使用阈值设置为正值，则启用此检查。如果收集使用阈值设置为零，则在此内存池上禁用此检查。默认值为零。Java虚拟机在垃圾收集时间执行收集使用阈值检查。
 *
 * <p>
 * 一些垃圾收集的内存池可能选择不支持收集使用阈值。例如，内存池仅由连续并发垃圾收集器管理。某些线程可以在该内存池中分配对象，同时并发垃圾收集器同时回收未使用的对象。除非有一个明确定义的垃圾收集时间，这是检查内存使用情况的最佳时间，否则不应支持收集使用阈值。
 *
 * <p>
 * 收集使用阈值设计用于监控Java虚拟机在回收内存空间后内存使用情况。收集使用情况也可以通过上面描述的用于<a href="#UsageThreshold">使用阈值</a>的轮询和阈值通知机制以类似的方式进行监控。
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
public interface MemoryPoolMXBean extends PlatformManagedObject {
    /**
     * 返回表示此内存池的名称。
     *
     * @return 此内存池的名称。
     */
    public String getName();

                /**
     * 返回此内存池的类型。
     *
     * <p>
     * <b>MBeanServer 访问</b>:<br>
     * <tt>MemoryType</tt> 的映射类型是 <tt>String</tt>
     * 值是 <tt>MemoryType</tt> 的名称。
     *
     * @return 此内存池的类型。
     */
    public MemoryType getType();

    /**
     * 返回此内存池的内存使用量估计值。
     * 如果此内存池无效（即不再存在），则此方法返回 <tt>null</tt>。
     *
     * <p>
     * 此方法请求 Java 虚拟机尽力估计此内存池的当前内存使用量。
     * 对于某些内存池，此方法可能是一个需要一些计算来确定估计值的昂贵操作。
     * 实现应记录这种情况。
     *
     * <p>此方法设计用于监控系统内存使用情况和检测低内存状况。
     *
     * <p>
     * <b>MBeanServer 访问</b>:<br>
     * <tt>MemoryUsage</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link MemoryUsage#from MemoryUsage} 中指定。
     *
     * @return 一个 {@link MemoryUsage} 对象；如果此池无效，则返回 <tt>null</tt>。
     */
    public MemoryUsage getUsage();

    /**
     * 返回自 Java 虚拟机启动或峰值重置以来此内存池的峰值内存使用量。
     * 如果此内存池无效（即不再存在），则此方法返回 <tt>null</tt>。
     *
     * <p>
     * <b>MBeanServer 访问</b>:<br>
     * <tt>MemoryUsage</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link MemoryUsage#from MemoryUsage} 中指定。
     *
     * @return 一个表示峰值内存使用量的 {@link MemoryUsage} 对象；如果此池无效，则返回 <tt>null</tt>。
     *
     */
    public MemoryUsage getPeakUsage();

    /**
     * 将此内存池的峰值内存使用量统计重置为当前内存使用量。
     *
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("control") 权限。
     */
    public void resetPeakUsage();

    /**
     * 测试此内存池在 Java 虚拟机中是否有效。
     * 一旦 Java 虚拟机从内存系统中移除此内存池，内存池就会变得无效。
     *
     * @return <tt>true</tt> 如果内存池在运行中的 Java 虚拟机中有效；
     *         否则返回 <tt>false</tt>。
     */
    public boolean isValid();

    /**
     * 返回管理此内存池的内存管理器的名称。
     * 每个内存池至少由一个内存管理器管理。
     *
     * @return 一个 <tt>String</tt> 对象数组，每个都是管理此内存池的内存管理器的名称。
     */
    public String[] getMemoryManagerNames();

    /**
     * 返回此内存池的使用阈值（以字节为单位）。
     * 每个内存池都有一个平台依赖的默认阈值。
     * 可以通过 {@link #setUsageThreshold setUsageThreshold} 方法更改当前使用阈值。
     *
     * @return 此内存池的使用阈值（以字节为单位）。
     *
     * @throws UnsupportedOperationException 如果此内存池不支持使用阈值。
     *
     * @see #isUsageThresholdSupported
     */
    public long getUsageThreshold();

    /**
     * 如果此内存池支持使用阈值，则将其阈值设置为给定的 <tt>threshold</tt> 值。
     * 如果阈值设置为正值，则在此内存池中启用使用阈值跨越检查。
     * 如果设置为零，则禁用使用阈值跨越检查。
     *
     * @param threshold 新的阈值（以字节为单位）。必须是非负数。
     *
     * @throws IllegalArgumentException 如果 <tt>threshold</tt> 为负数
     *         或大于此内存池定义的最大内存量。
     *
     * @throws UnsupportedOperationException 如果此内存池
     *         不支持使用阈值。
     *
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("control") 权限。
     *
     * @see #isUsageThresholdSupported
     * @see <a href="#UsageThreshold">使用阈值</a>
     */
    public void setUsageThreshold(long threshold);

    /**
     * 测试此内存池的内存使用量是否达到或超过其使用阈值。
     *
     * @return <tt>true</tt> 如果此内存池的内存使用量达到或超过阈值；
     * <tt>false</tt> 否则。
     *
     * @throws UnsupportedOperationException 如果此内存池
     *         不支持使用阈值。
     */
    public boolean isUsageThresholdExceeded();

    /**
     * 返回内存使用量跨越使用阈值的次数。
     *
     * @return 内存使用量跨越其使用阈值的次数。
     *
     * @throws UnsupportedOperationException 如果此内存池
     * 不支持使用阈值。
     */
    public long getUsageThresholdCount();

    /**
     * 测试此内存池是否支持使用阈值。
     *
     * @return <tt>true</tt> 如果此内存池支持使用阈值；
     * <tt>false</tt> 否则。
     */
    public boolean isUsageThresholdSupported();

    /**
     * 返回此内存池的集合使用阈值（以字节为单位）。
     * 默认值为零。可以通过
     * {@link #setCollectionUsageThreshold setCollectionUsageThreshold} 方法更改集合使用阈值。
     *
     * @return 此内存池的集合使用阈值（以字节为单位）。
     *
     * @throws UnsupportedOperationException 如果此内存池
     *         不支持集合使用阈值。
     *
     * @see #isCollectionUsageThresholdSupported
     */
    public long getCollectionUsageThreshold();


/**
 * 将此内存池的集合使用阈值设置为给定的 <tt>threshold</tt> 值。
 * 当此阈值设置为正数时，Java虚拟机将在回收此内存池中未使用对象后，尽可能适当时检查内存使用情况。
 * <p>
 * 如果阈值设置为正数，则在此内存池中启用集合使用阈值跨越检查。
 * 如果设置为零，则禁用集合使用阈值跨越检查。
 *
 * @param threshold 以字节为单位的新集合使用阈值。必须是非负数。
 *
 * @throws IllegalArgumentException 如果 <tt>threshold</tt> 为负数
 *         或大于此内存池的最大内存量（如果已定义）。
 *
 * @throws UnsupportedOperationException 如果此内存池
 *         不支持集合使用阈值。
 *
 * @throws java.lang.SecurityException 如果存在安全管理器
 *         且调用者没有 ManagementPermission("control") 权限。
 *
 * @see #isCollectionUsageThresholdSupported
 * @see <a href="#CollectionThreshold">集合使用阈值</a>
 */
public void setCollectionUsageThreshold(long threshold);

/**
 * 测试此内存池在Java虚拟机最近一次尽力回收未使用对象后，
 * 内存使用情况是否达到或超过其集合使用阈值。
 * 本方法不会请求Java虚拟机执行任何垃圾收集，除了其正常的
 * 自动内存管理。
 *
 * @return 如果此内存池在最近一次集合中的内存使用情况
 * 达到或超过集合使用阈值，则返回 <tt>true</tt>；
 * 否则返回 <tt>false</tt>。
 *
 * @throws UnsupportedOperationException 如果此内存池
 *         不支持使用阈值。
 */
public boolean isCollectionUsageThresholdExceeded();

/**
 * 返回Java虚拟机检测到内存使用情况达到或
 * 超过集合使用阈值的次数。
 *
 * @return 内存使用情况达到或超过集合使用阈值的次数。
 *
 * @throws UnsupportedOperationException 如果此内存池
 *         不支持集合使用阈值。
 *
 * @see #isCollectionUsageThresholdSupported
 */
public long getCollectionUsageThresholdCount();

/**
 * 返回Java虚拟机最近一次尽力回收此内存池中未使用对象后的内存使用情况。
 * 本方法不会请求Java虚拟机执行任何垃圾收集，除了其正常的
 * 自动内存管理。
 * 如果Java虚拟机不支持此方法，则本方法返回 <tt>null</tt>。
 *
 * <p>
 * <b>MBeanServer 访问</b>：<br>
 * <tt>MemoryUsage</tt> 的映射类型是
 * <tt>CompositeData</tt>，属性如 {@link MemoryUsage#from MemoryUsage} 中所述。
 *
 * @return 一个 {@link MemoryUsage}，表示Java虚拟机最近一次尽力回收未使用对象后
 * 此内存池的内存使用情况；
 * 如果此方法不受支持，则返回 <tt>null</tt>。
 */
public MemoryUsage getCollectionUsage();

/**
 * 测试此内存池是否支持集合使用阈值。
 *
 * @return 如果此内存池支持集合使用阈值，则返回 <tt>true</tt>；否则返回 <tt>false</tt>。
 */
public boolean isCollectionUsageThresholdSupported();
}
