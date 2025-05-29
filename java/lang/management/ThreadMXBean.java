
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

import java.util.Map;

/**
 * Java 虚拟机线程系统的管理接口。
 *
 * <p> Java 虚拟机有一个此类实现的单个实例。此接口的实现是一个
 * <a href="ManagementFactory.html#MXBean">MXBean</a>，可以通过调用
 * {@link ManagementFactory#getThreadMXBean} 方法或
 * {@link ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 方法获取。
 *
 * <p>用于唯一标识 MBeanServer 中线程系统 MXBean 的 <tt>ObjectName</tt> 是：
 * <blockquote>
 *    {@link ManagementFactory#THREAD_MXBEAN_NAME
 *           <tt>java.lang:type=Threading</tt>}
 * </blockquote>
 *
 * 可以通过调用
 * {@link PlatformManagedObject#getObjectName} 方法获取。
 *
 * <h3>线程 ID</h3>
 * 线程 ID 是通过调用 {@link java.lang.Thread#getId} 方法为线程返回的一个正长整数值。
 * 线程 ID 在其生命周期内是唯一的。当线程终止时，此线程 ID 可能会被重用。
 *
 * <p> 本接口中的一些方法接受线程 ID 或线程 ID 数组作为输入参数，并返回每个线程的信息。
 *
 * <h3>线程 CPU 时间</h3>
 * Java 虚拟机实现可能支持测量当前线程、任何线程或无任何线程的 CPU 时间。
 *
 * <p>
 * 可以使用 {@link #isThreadCpuTimeSupported} 方法确定 Java 虚拟机是否支持测量任何线程的 CPU 时间。
 * 可以使用 {@link #isCurrentThreadCpuTimeSupported} 方法确定 Java 虚拟机是否支持测量当前线程的 CPU 时间。
 * 支持测量任何线程 CPU 时间的 Java 虚拟机实现也将支持当前线程的测量。
 *
 * <p> 本接口提供的 CPU 时间具有纳秒精度，但不一定具有纳秒精度。
 *
 * <p>
 * Java 虚拟机可能默认禁用 CPU 时间测量。
 * 可以使用 {@link #isThreadCpuTimeEnabled} 和 {@link #setThreadCpuTimeEnabled}
 * 方法测试是否启用了 CPU 时间测量以及分别启用/禁用此支持。
 * 在某些 Java 虚拟机实现中，启用线程 CPU 测量可能会很昂贵。
 *
 * <h3>线程争用监视</h3>
 * 一些 Java 虚拟机可能支持线程争用监视。当启用线程争用监视时，线程因同步或等待通知而阻塞的累积经过时间将被收集并返回在
 * <a href="ThreadInfo.html#SyncStats"><tt>ThreadInfo</tt></a> 对象中。
 * <p>
 * 可以使用 {@link #isThreadContentionMonitoringSupported} 方法确定 Java 虚拟机是否支持线程争用监视。
 * 默认情况下，线程争用监视是禁用的。可以使用 {@link #setThreadContentionMonitoringEnabled} 方法启用线程争用监视。
 *
 * <h3>同步信息和死锁检测</h3>
 * 一些 Java 虚拟机可能支持监视
 * {@linkplain #isObjectMonitorUsageSupported 对象监视器使用} 和
 * {@linkplain #isSynchronizerUsageSupported 可拥有同步器使用}。
 * 可以使用 {@link #getThreadInfo(long[], boolean, boolean)} 和
 * {@link #dumpAllThreads} 方法获取线程堆栈跟踪和同步信息，包括线程被阻塞以获取或等待的
 * {@linkplain LockInfo <i>锁</i>} 以及线程当前拥有的锁。
 * <p>
 * <tt>ThreadMXBean</tt> 接口提供了
 * {@link #findMonitorDeadlockedThreads} 和
 * {@link #findDeadlockedThreads} 方法来查找运行应用程序中的死锁。
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

public interface ThreadMXBean extends PlatformManagedObject {
    /**
     * 返回当前活动线程的数量，包括守护线程和非守护线程。
     *
     * @return 当前活动线程的数量。
     */
    public int getThreadCount();

    /**
     * 返回自 Java 虚拟机启动或峰值重置以来的最大活动线程数。
     *
     * @return 最大活动线程数。
     */
    public int getPeakThreadCount();

    /**
     * 返回自 Java 虚拟机启动以来创建并启动的线程总数。
     *
     * @return 启动的线程总数。
     */
    public long getTotalStartedThreadCount();

    /**
     * 返回当前活动守护线程的数量。
     *
     * @return 当前活动守护线程的数量。
     */
    public int getDaemonThreadCount();

    /**
     * 返回所有活动线程的 ID。
     * 返回数组中包含的一些线程可能在该方法返回时已终止。
     *
     * @return <tt>long</tt> 数组，每个元素是一个线程 ID。
     *
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("monitor") 权限。
     */
    public long[] getAllThreadIds();

    /**
     * 返回指定 <tt>id</tt> 的线程的线程信息，不包含堆栈跟踪。
     * 此方法等效于调用：
     * <blockquote>
     *   {@link #getThreadInfo(long, int) getThreadInfo(id, 0);}
     * </blockquote>
     *
     * <p>
     * 此方法返回一个 <tt>ThreadInfo</tt> 对象，表示指定 ID 的线程的信息。
     * 返回的 <tt>ThreadInfo</tt> 对象中的堆栈跟踪、锁定监视器和锁定同步器
     * 将为空。
     *
     * 如果指定 ID 的线程未活动或不存在，此方法将返回 <tt>null</tt>。线程在其启动后且未死亡前被视为活动。
     *
     * <p>
     * <b>MBeanServer 访问</b>：<br>
     * <tt>ThreadInfo</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如
     * {@link ThreadInfo#from ThreadInfo.from} 方法中指定。
     *
     * @param id 线程的 ID。必须为正数。
     *
     * @return 指定 ID 的线程的 {@link ThreadInfo} 对象，不包含堆栈跟踪、锁定监视器和同步器信息；
     * <tt>null</tt> 如果指定 ID 的线程未活动或不存在。
     *
     * @throws IllegalArgumentException 如果 {@code id <= 0}。
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("monitor") 权限。
     */
    public ThreadInfo getThreadInfo(long id);

                /**
     * 返回每个线程的线程信息
     * 其ID在输入数组 <tt>ids</tt> 中，不包含堆栈跟踪。
     * 此方法等同于调用：
     * <blockquote><pre>
     *   {@link #getThreadInfo(long[], int) getThreadInfo}(ids, 0);
     * </pre></blockquote>
     *
     * <p>
     * 此方法返回一个 <tt>ThreadInfo</tt> 对象数组。
     * 每个 <tt>ThreadInfo</tt> 对象中的堆栈跟踪、锁定的监视器和锁定的同步器
     * 将为空。
     *
     * 如果给定ID的线程未存活或不存在，
     * 返回数组中相应的元素将包含 <tt>null</tt>。 线程存活是指
     * 它已被启动且尚未死亡。
     *
     * <p>
     * <b>MBeanServer 访问</b>：<br>
     * <tt>ThreadInfo</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link ThreadInfo#from ThreadInfo.from} 方法中所述。
     *
     * @param ids 线程ID数组。
     * @return 一个包含 {@link ThreadInfo} 对象的数组，每个对象包含
     * 信息关于输入ID数组中相应元素的线程
     * 不包含堆栈跟踪、锁定的监视器和同步器信息。
     *
     * @throws IllegalArgumentException 如果输入数组 <tt>ids</tt> 中的任何元素 {@code <= 0}。
     * @throws java.lang.SecurityException 如果存在安全管理器
     *         且调用者没有 ManagementPermission("monitor")。
     */
    public ThreadInfo[] getThreadInfo(long[] ids);

    /**
     * 返回指定 <tt>id</tt> 的线程的线程信息，
     * 包含指定数量的堆栈跟踪元素。
     * <tt>maxDepth</tt> 参数表示从堆栈跟踪中检索的
     * {@link StackTraceElement} 的最大数量。
     * 如果 <tt>maxDepth == Integer.MAX_VALUE</tt>，则将转储线程的整个堆栈跟踪。
     * 如果 <tt>maxDepth == 0</tt>，则不会转储线程的堆栈跟踪。
     * 此方法不会获取线程的锁定监视器和锁定的同步器。
     * <p>
     * 当Java虚拟机没有关于线程的堆栈跟踪信息
     * 或 <tt>maxDepth == 0</tt> 时，
     * <tt>ThreadInfo</tt> 对象中的
     * 堆栈跟踪将是一个空的 <tt>StackTraceElement</tt> 数组。
     *
     * <p>
     * 如果给定ID的线程未存活或不存在，
     * 此方法将返回 <tt>null</tt>。 线程存活是指
     * 它已被启动且尚未死亡。
     *
     * <p>
     * <b>MBeanServer 访问</b>：<br>
     * <tt>ThreadInfo</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link ThreadInfo#from ThreadInfo.from} 方法中所述。
     *
     * @param id 线程的线程ID。必须为正数。
     * @param maxDepth 要转储的堆栈跟踪中的最大条目数。可以使用 <tt>Integer.MAX_VALUE</tt> 请求
     * 转储整个堆栈。
     *
     * @return 给定ID的线程的 {@link ThreadInfo}，
     * 不包含锁定的监视器和同步器信息。
     * 如果给定ID的线程未存活或不存在，则返回 <tt>null</tt>。
     *
     * @throws IllegalArgumentException 如果 {@code id <= 0}。
     * @throws IllegalArgumentException 如果 <tt>maxDepth 是负数</tt>。
     * @throws java.lang.SecurityException 如果存在安全管理器
     *         且调用者没有 ManagementPermission("monitor")。
     *
     */
    public ThreadInfo getThreadInfo(long id, int maxDepth);

    /**
     * 返回每个线程的线程信息
     * 其ID在输入数组 <tt>ids</tt> 中，
     * 包含指定数量的堆栈跟踪元素。
     * <tt>maxDepth</tt> 参数表示从堆栈跟踪中检索的
     * {@link StackTraceElement} 的最大数量。
     * 如果 <tt>maxDepth == Integer.MAX_VALUE</tt>，则将转储线程的整个堆栈跟踪。
     * 如果 <tt>maxDepth == 0</tt>，则不会转储线程的堆栈跟踪。
     * 此方法不会获取线程的锁定监视器和锁定的同步器。
     * <p>
     * 当Java虚拟机没有关于线程的堆栈跟踪信息
     * 或 <tt>maxDepth == 0</tt> 时，
     * <tt>ThreadInfo</tt> 对象中的
     * 堆栈跟踪将是一个空的 <tt>StackTraceElement</tt> 数组。
     * <p>
     * 此方法返回一个 <tt>ThreadInfo</tt> 对象数组，
     * 每个对象是 <tt>ids</tt> 数组中相同索引的线程的信息。
     * 如果给定ID的线程未存活或不存在，
     * 则在返回数组中相应的元素中设置 <tt>null</tt>。 线程存活是指
     * 它已被启动且尚未死亡。
     *
     * <p>
     * <b>MBeanServer 访问</b>：<br>
     * <tt>ThreadInfo</tt> 的映射类型是
     * <tt>CompositeData</tt>，属性如 {@link ThreadInfo#from ThreadInfo.from} 方法中所述。
     *
     * @param ids 线程ID数组
     * @param maxDepth 要转储的堆栈跟踪中的最大条目数。可以使用 <tt>Integer.MAX_VALUE</tt> 请求
     * 转储整个堆栈。
     *
     * @return 一个包含 {@link ThreadInfo} 对象的数组，每个对象包含
     * 信息关于输入ID数组中相应元素的线程，不包含锁定的监视器和
     * 同步器信息。
     *
     * @throws IllegalArgumentException 如果 <tt>maxDepth 是负数</tt>。
     * @throws IllegalArgumentException 如果输入数组 <tt>ids</tt> 中的任何元素 {@code <= 0}。
     * @throws java.lang.SecurityException 如果存在安全管理器
     *         且调用者没有 ManagementPermission("monitor")。
     *
     */
    public ThreadInfo[] getThreadInfo(long[] ids, int maxDepth);

                /**
     * 测试 Java 虚拟机是否支持线程竞争监视。
     *
     * @return
     *   <tt>true</tt>
     *     如果 Java 虚拟机支持线程竞争监视；
     *   <tt>false</tt> 否则。
     */
    public boolean isThreadContentionMonitoringSupported();

    /**
     * 测试是否启用了线程竞争监视。
     *
     * @return <tt>true</tt> 如果启用了线程竞争监视；
     *         <tt>false</tt> 否则。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java 虚拟机不支持线程竞争监视。
     *
     * @see #isThreadContentionMonitoringSupported
     */
    public boolean isThreadContentionMonitoringEnabled();

    /**
     * 启用或禁用线程竞争监视。
     * 默认情况下，线程竞争监视是禁用的。
     *
     * @param enable <tt>true</tt> 启用；
     *               <tt>false</tt> 禁用。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持线程竞争监视。
     *
     * @throws java.lang.SecurityException 如果存在安全管理器
     *         并且调用者没有
     *         ManagementPermission("control") 权限。
     *
     * @see #isThreadContentionMonitoringSupported
     */
    public void setThreadContentionMonitoringEnabled(boolean enable);

    /**
     * 返回当前线程的总 CPU 时间（以纳秒为单位）。
     * 返回的值具有纳秒精度，但不一定具有纳秒精度。
     * 如果实现区分用户模式时间和系统模式时间，则返回的 CPU 时间是
     * 当前线程在用户模式或系统模式下执行的时间。
     *
     * <p>
     * 这是一个方便的本地管理方法，等同于调用：
     * <blockquote><pre>
     *   {@link #getThreadCpuTime getThreadCpuTime}(Thread.currentThread().getId());
     * </pre></blockquote>
     *
     * @return 如果启用了 CPU 时间测量，则返回当前线程的总 CPU 时间；否则返回 <tt>-1</tt>。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持当前线程的 CPU 时间测量。
     *
     * @see #getCurrentThreadUserTime
     * @see #isCurrentThreadCpuTimeSupported
     * @see #isThreadCpuTimeEnabled
     * @see #setThreadCpuTimeEnabled
     */
    public long getCurrentThreadCpuTime();

    /**
     * 返回当前线程在用户模式下执行的 CPU 时间（以纳秒为单位）。
     * 返回的值具有纳秒精度，但不一定具有纳秒精度。
     *
     * <p>
     * 这是一个方便的本地管理方法，等同于调用：
     * <blockquote><pre>
     *   {@link #getThreadUserTime getThreadUserTime}(Thread.currentThread().getId());
     * </pre></blockquote>
     *
     * @return 如果启用了 CPU 时间测量，则返回当前线程的用户级 CPU 时间；否则返回 <tt>-1</tt>。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持当前线程的 CPU 时间测量。
     *
     * @see #getCurrentThreadCpuTime
     * @see #isCurrentThreadCpuTimeSupported
     * @see #isThreadCpuTimeEnabled
     * @see #setThreadCpuTimeEnabled
     */
    public long getCurrentThreadUserTime();

    /**
     * 返回指定 ID 的线程的总 CPU 时间（以纳秒为单位）。
     * 返回的值具有纳秒精度，但不一定具有纳秒精度。
     * 如果实现区分用户模式时间和系统模式时间，则返回的 CPU 时间是
     * 该线程在用户模式或系统模式下执行的时间。
     *
     * <p>
     * 如果指定 ID 的线程未存活或不存在，此方法返回 <tt>-1</tt>。如果禁用了 CPU 时间测量，此方法返回 <tt>-1</tt>。
     * 线程存活是指已启动且尚未终止。
     * <p>
     * 如果在启动线程后启用 CPU 时间测量，Java 虚拟机实现可以选择从启用该功能的任何时间点开始测量 CPU 时间。
     *
     * @param id 线程的 ID
     * @return 如果指定 ID 的线程存在、存活且启用了 CPU 时间测量，则返回指定 ID 的线程的总 CPU 时间；
     * <tt>-1</tt> 否则。
     *
     * @throws IllegalArgumentException 如果 {@code id <= 0}。
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持其他线程的 CPU 时间测量。
     *
     * @see #getThreadUserTime
     * @see #isThreadCpuTimeSupported
     * @see #isThreadCpuTimeEnabled
     * @see #setThreadCpuTimeEnabled
     */
    public long getThreadCpuTime(long id);

    /**
     * 返回指定 ID 的线程在用户模式下执行的 CPU 时间（以纳秒为单位）。
     * 返回的值具有纳秒精度，但不一定具有纳秒精度。
     *
     * <p>
     * 如果指定 ID 的线程未存活或不存在，此方法返回 <tt>-1</tt>。如果禁用了 CPU 时间测量，此方法返回 <tt>-1</tt>。
     * 线程存活是指已启动且尚未终止。
     * <p>
     * 如果在启动线程后启用 CPU 时间测量，Java 虚拟机实现可以选择从启用该功能的任何时间点开始测量 CPU 时间。
     *
     * @param id 线程的 ID
     * @return 如果指定 ID 的线程存在、存活且启用了 CPU 时间测量，则返回指定 ID 的线程的用户级 CPU 时间；
     * <tt>-1</tt> 否则。
     *
     * @throws IllegalArgumentException 如果 {@code id <= 0}。
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持其他线程的 CPU 时间测量。
     *
     * @see #getThreadCpuTime
     * @see #isThreadCpuTimeSupported
     * @see #isThreadCpuTimeEnabled
     * @see #setThreadCpuTimeEnabled
     */
    public long getThreadUserTime(long id);

                /**
     * 测试 Java 虚拟机实现是否支持任何线程的 CPU 时间测量。
     * 支持任何线程 CPU 时间测量的 Java 虚拟机实现也将支持当前线程的 CPU 时间测量。
     *
     * @return
     *   <tt>true</tt>
     *     如果 Java 虚拟机支持任何线程的 CPU 时间测量；
     *   <tt>false</tt> 否则。
     */
    public boolean isThreadCpuTimeSupported();

    /**
     * 测试 Java 虚拟机是否支持当前线程的 CPU 时间测量。
     * 如果 {@link #isThreadCpuTimeSupported} 返回 <tt>true</tt>，则此方法返回 <tt>true</tt>。
     *
     * @return
     *   <tt>true</tt>
     *     如果 Java 虚拟机支持当前线程的 CPU 时间测量；
     *   <tt>false</tt> 否则。
     */
    public boolean isCurrentThreadCpuTimeSupported();

    /**
     * 测试是否启用了线程 CPU 时间测量。
     *
     * @return <tt>true</tt> 如果启用了线程 CPU 时间测量；
     *         <tt>false</tt> 否则。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java 虚拟机不支持其他线程或当前线程的 CPU 时间测量。
     *
     * @see #isThreadCpuTimeSupported
     * @see #isCurrentThreadCpuTimeSupported
     */
    public boolean isThreadCpuTimeEnabled();

    /**
     * 启用或禁用线程 CPU 时间测量。默认值取决于平台。
     *
     * @param enable <tt>true</tt> 启用；
     *               <tt>false</tt> 禁用。
     *
     * @throws java.lang.UnsupportedOperationException 如果 Java
     * 虚拟机不支持任何线程或当前线程的 CPU 时间测量。
     *
     * @throws java.lang.SecurityException 如果存在安全经理
     *         并且调用者没有 ManagementPermission("control") 权限。
     *
     * @see #isThreadCpuTimeSupported
     * @see #isCurrentThreadCpuTimeSupported
     */
    public void setThreadCpuTimeEnabled(boolean enable);

    /**
     * 查找处于死锁状态等待获取对象监视器的线程循环。也就是说，被阻塞等待进入同步块或在
     * {@link Object#wait Object.wait} 调用后重新进入同步块的线程，
     * 其中每个线程拥有一个监视器，同时尝试获取另一个由其他线程持有的监视器。
     * <p>
     * 更正式地说，如果线程是“等待对象监视器拥有关系”中的循环的一部分，则该线程处于“监视器死锁”状态。在最简单的情况下，线程 A 被阻塞等待由线程 B 拥有的监视器，而线程 B 被阻塞等待由线程 A 拥有的监视器。
     * <p>
     * 该方法设计用于故障排除，而不是同步控制。它可能是一个昂贵的操作。
     * <p>
     * 该方法仅查找涉及对象监视器的死锁。要查找涉及对象监视器和
     * <a href="LockInfo.html#OwnableSynchronizer">可拥有同步器</a> 的死锁，
     * 应使用 {@link #findDeadlockedThreads findDeadlockedThreads} 方法。
     *
     * @return 一个包含处于监视器死锁状态的线程 ID 的数组，如果没有则返回 <tt>null</tt>。
     *
     * @throws java.lang.SecurityException 如果存在安全经理
     *         并且调用者没有 ManagementPermission("monitor") 权限。
     *
     * @see #findDeadlockedThreads
     */
    public long[] findMonitorDeadlockedThreads();

    /**
     * 重置峰值线程数为当前活动线程数。
     *
     * @throws java.lang.SecurityException 如果存在安全经理
     *         并且调用者没有 ManagementPermission("control") 权限。
     *
     * @see #getPeakThreadCount
     * @see #getThreadCount
     */
    public void resetPeakThreadCount();

    /**
     * 查找处于死锁状态等待获取对象监视器或
     * <a href="LockInfo.html#OwnableSynchronizer">可拥有同步器</a> 的线程循环。
     *
     * 如果每个线程拥有一个锁，同时尝试获取另一个由其他线程持有的锁，则这些线程在循环中等待这两种类型的锁时处于“死锁”状态。
     * <p>
     * 该方法设计用于故障排除，而不是同步控制。它可能是一个昂贵的操作。
     *
     * @return 一个包含处于死锁状态等待对象监视器或可拥有同步器的线程 ID 的数组，如果没有则返回 <tt>null</tt>。
     *
     * @throws java.lang.SecurityException 如果存在安全经理
     *         并且调用者没有 ManagementPermission("monitor") 权限。
     * @throws java.lang.UnsupportedOperationException 如果 Java 虚拟机不支持可拥有同步器使用情况的监视。
     *
     * @see #isSynchronizerUsageSupported
     * @see #findMonitorDeadlockedThreads
     * @since 1.6
     */
    public long[] findDeadlockedThreads();

    /**
     * 测试 Java 虚拟机是否支持对象监视器使用情况的监视。
     *
     * @return
     *   <tt>true</tt>
     *     如果 Java 虚拟机支持对象监视器使用情况的监视；
     *   <tt>false</tt> 否则。
     *
     * @see #dumpAllThreads
     * @since 1.6
     */
    public boolean isObjectMonitorUsageSupported();

    /**
     * 测试 Java 虚拟机是否支持
     * <a href="LockInfo.html#OwnableSynchronizer">
     * 可拥有同步器</a> 使用情况的监视。
     *
     * @return
     *   <tt>true</tt>
     *     如果 Java 虚拟机支持可拥有同步器使用情况的监视；
     *   <tt>false</tt> 否则。
     *
     * @see #dumpAllThreads
     * @since 1.6
     */
    public boolean isSynchronizerUsageSupported();

                /**
     * 返回输入数组 <tt>ids</tt> 中每个线程的线程信息，包括堆栈跟踪和同步信息。
     *
     * <p>
     * 此方法获取每个线程的快照信息，包括：
     * <ul>
     *    <li>完整的堆栈跟踪，</li>
     *    <li>如果 <tt>lockedMonitors</tt> 为 <tt>true</tt>，则包括线程当前锁定的对象监视器，</li>
     *    <li>如果 <tt>lockedSynchronizers</tt> 为 <tt>true</tt>，则包括线程当前锁定的<a href="LockInfo.html#OwnableSynchronizer">
     *        可拥有的同步器</a>。</li>
     * </ul>
     * <p>
     * 此方法返回一个 <tt>ThreadInfo</tt> 对象数组，每个对象包含与 <tt>ids</tt> 数组中相同索引的线程信息。
     * 如果给定 ID 的线程未存活或不存在，则返回数组中相应元素将设置为 <tt>null</tt>。线程存活是指它已启动且尚未死亡。
     * <p>
     * 如果线程未锁定任何对象监视器或 <tt>lockedMonitors</tt> 为 <tt>false</tt>，返回的 <tt>ThreadInfo</tt> 对象将具有一个空的 <tt>MonitorInfo</tt> 数组。同样，如果线程未锁定任何同步器或 <tt>lockedSynchronizers</tt> 为 <tt>false</tt>，返回的 <tt>ThreadInfo</tt> 对象将具有一个空的 <tt>LockInfo</tt> 数组。
     *
     * <p>
     * 当 <tt>lockedMonitors</tt> 和 <tt>lockedSynchronizers</tt> 参数均为 <tt>false</tt> 时，等同于调用：
     * <blockquote><pre>
     *     {@link #getThreadInfo(long[], int)  getThreadInfo(ids, Integer.MAX_VALUE)}
     * </pre></blockquote>
     *
     * <p>
     * 此方法设计用于故障排除，而不是同步控制。它可能是一个昂贵的操作。
     *
     * <p>
     * <b>MBeanServer 访问</b>：<br>
     * <tt>ThreadInfo</tt> 的映射类型是 <tt>CompositeData</tt>，属性如 {@link ThreadInfo#from ThreadInfo.from} 方法中所述。
     *
     * @param  ids 线程 ID 数组。
     * @param  lockedMonitors 如果为 <tt>true</tt>，则检索所有锁定的监视器。
     * @param  lockedSynchronizers 如果为 <tt>true</tt>，则检索所有锁定的可拥有的同步器。
     *
     * @return 一个 {@link ThreadInfo} 对象数组，每个对象包含输入 ID 数组中相应元素的线程信息。
     *
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("monitor") 权限。
     * @throws java.lang.UnsupportedOperationException
     *         <ul>
     *           <li>如果 <tt>lockedMonitors</tt> 为 <tt>true</tt> 但 Java 虚拟机不支持 {@linkplain #isObjectMonitorUsageSupported
     *               对象监视器使用情况} 的监控；或</li>
     *           <li>如果 <tt>lockedSynchronizers</tt> 为 <tt>true</tt> 但 Java 虚拟机不支持 {@linkplain #isSynchronizerUsageSupported
     *               可拥有的同步器使用情况} 的监控。</li>
     *         </ul>
     *
     * @see #isObjectMonitorUsageSupported
     * @see #isSynchronizerUsageSupported
     *
     * @since 1.6
     */
    public ThreadInfo[] getThreadInfo(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers);

    /**
     * 返回所有存活线程的线程信息，包括堆栈跟踪和同步信息。
     * 返回的数组中包含的一些线程可能在本方法返回时已终止。
     *
     * <p>
     * 此方法返回一个 {@link ThreadInfo} 对象数组，如 {@link #getThreadInfo(long[], boolean, boolean)}
     * 方法中所述。
     *
     * @param  lockedMonitors 如果为 <tt>true</tt>，则转储所有锁定的监视器。
     * @param  lockedSynchronizers 如果为 <tt>true</tt>，则转储所有锁定的可拥有的同步器。
     *
     * @return 包含所有存活线程的 {@link ThreadInfo} 数组。
     *
     * @throws java.lang.SecurityException 如果存在安全经理且调用者没有
     *         ManagementPermission("monitor") 权限。
     * @throws java.lang.UnsupportedOperationException
     *         <ul>
     *           <li>如果 <tt>lockedMonitors</tt> 为 <tt>true</tt> 但 Java 虚拟机不支持 {@linkplain #isObjectMonitorUsageSupported
     *               对象监视器使用情况} 的监控；或</li>
     *           <li>如果 <tt>lockedSynchronizers</tt> 为 <tt>true</tt> 但 Java 虚拟机不支持 {@linkplain #isSynchronizerUsageSupported
     *               可拥有的同步器使用情况} 的监控。</li>
     *         </ul>
     *
     * @see #isObjectMonitorUsageSupported
     * @see #isSynchronizerUsageSupported
     *
     * @since 1.6
     */
    public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers);
}
