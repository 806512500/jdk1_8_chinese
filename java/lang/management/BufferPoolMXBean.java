/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其关联公司。保留所有权利。
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
 * 一个缓冲池的管理接口，例如一个池中的
 * {@link java.nio.ByteBuffer#allocateDirect 直接} 或 {@link
 * java.nio.MappedByteBuffer 映射} 缓冲区。
 *
 * <p> 实现此接口的类是一个
 * {@link javax.management.MXBean}。Java
 * 虚拟机有一个或多个此接口的实现。可以使用 {@link
 * java.lang.management.ManagementFactory#getPlatformMXBeans getPlatformMXBeans}
 * 方法获取表示缓冲池管理接口的 {@code BufferPoolMXBean} 对象列表，如下所示：
 * <pre>
 *     List&lt;BufferPoolMXBean&gt; pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
 * </pre>
 *
 * <p> 管理接口还注册到了平台 {@link
 * javax.management.MBeanServer MBeanServer}。在 {@code MBeanServer} 中唯一标识
 * 管理接口的 {@link
 * javax.management.ObjectName ObjectName} 的形式为：
 * <pre>
 *     java.nio:type=BufferPool,name=<i>池名称</i>
 * </pre>
 * 其中 <em>池名称</em> 是缓冲池的 {@link #getName 名称}。
 *
 * @since   1.7
 */
public interface BufferPoolMXBean extends PlatformManagedObject {

    /**
     * 返回表示此缓冲池的名称。
     *
     * @return  此缓冲池的名称。
     */
    String getName();

    /**
     * 返回池中缓冲区数量的估计值。
     *
     * @return  此池中缓冲区数量的估计值
     */
    long getCount();

    /**
     * 返回池中缓冲区总容量的估计值。
     * 缓冲区的容量是它包含的元素数量，此方法返回的值是池中缓冲区总容量的估计值（以字节为单位）。
     *
     * @return  此池中缓冲区总容量的估计值（以字节为单位）
     */
    long getTotalCapacity();

    /**
     * 返回 Java 虚拟机为此缓冲池使用的内存的估计值。
     * 此方法返回的值可能与池中缓冲区总 {@link #getTotalCapacity 容量} 的估计值不同。
     * 这种差异可以通过对齐、内存分配器和其他实现特定的原因来解释。
     *
     * @return  Java 虚拟机为此缓冲池使用的内存的估计值（以字节为单位），如果无法获得内存使用量的估计值，则返回 {@code -1L}
     */
    long getMemoryUsed();
}
