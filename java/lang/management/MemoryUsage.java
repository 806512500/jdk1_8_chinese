
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

import javax.management.openmbean.CompositeData;
import sun.management.MemoryUsageCompositeData;

/**
 * 一个 <tt>MemoryUsage</tt> 对象表示内存使用情况的快照。
 * <tt>MemoryUsage</tt> 类的实例通常由用于获取 Java 虚拟机中各个内存池或整个堆或非堆内存的内存使用情况信息的方法构造。
 *
 * <p> 一个 <tt>MemoryUsage</tt> 对象包含四个值：
 * <table summary="描述 MemoryUsage 对象内容">
 * <tr>
 * <td valign=top> <tt>init</tt> </td>
 * <td valign=top> 表示 Java 虚拟机在启动时从操作系统请求用于内存管理的初始内存量（以字节为单位）。
 *      Java 虚拟机可能会随着时间从操作系统请求更多内存，并且也可能释放内存给系统。
 *      <tt>init</tt> 的值可能是未定义的。
 * </td>
 * </tr>
 * <tr>
 * <td valign=top> <tt>used</tt> </td>
 * <td valign=top> 表示当前使用的内存量（以字节为单位）。
 * </td>
 * </tr>
 * <tr>
 * <td valign=top> <tt>committed</tt> </td>
 * <td valign=top> 表示 Java 虚拟机可以使用的已保证的内存量（以字节为单位）。
 *      已提交的内存量可能会随着时间变化（增加或减少）。Java 虚拟机可能会释放内存给系统，<tt>committed</tt> 可能小于 <tt>init</tt>。
 *      <tt>committed</tt> 始终大于或等于 <tt>used</tt>。
 * </td>
 * </tr>
 * <tr>
 * <td valign=top> <tt>max</tt> </td>
 * <td valign=top> 表示可用于内存管理的最大内存量（以字节为单位）。其值可能是未定义的。
 *      如果定义了最大内存量，它可能会随着时间变化。使用的内存和已提交的内存量始终小于或等于 <tt>max</tt>（如果定义了 <tt>max</tt>）。
 *      如果尝试增加使用的内存使得 <tt>used &gt; committed</tt>，即使 <tt>used &lt;= max</tt> 仍然为真（例如，当系统虚拟内存不足时），内存分配也可能失败。
 * </td>
 * </tr>
 * </table>
 *
 * 下面是一个显示内存池示例的图片：
 *
 * <pre>
 *        +----------------------------------------------+
 *        +////////////////           |                  +
 *        +////////////////           |                  +
 *        +----------------------------------------------+
 *
 *        |--------|
 *           init
 *        |---------------|
 *               used
 *        |---------------------------|
 *                  committed
 *        |----------------------------------------------|
 *                            max
 * </pre>
 *
 * <h3>MXBean 映射</h3>
 * <tt>MemoryUsage</tt> 映射到一个 {@link CompositeData CompositeData}，其属性如 {@link #from from} 方法中指定的那样。
 *
 * @author   Mandy Chung
 * @since   1.5
 */
public class MemoryUsage {
    private final long init;
    private final long used;
    private final long committed;
    private final long max;

    /**
     * 构造一个 <tt>MemoryUsage</tt> 对象。
     *
     * @param init      Java 虚拟机分配的初始内存量（以字节为单位）；
     *                  或者如果未定义则为 <tt>-1</tt>。
     * @param used      已使用的内存量（以字节为单位）。
     * @param committed 已提交的内存量（以字节为单位）。
     * @param max       可以使用的最大内存量（以字节为单位）；
     *                  或者如果未定义则为 <tt>-1</tt>。
     *
     * @throws IllegalArgumentException 如果
     * <ul>
     * <li> <tt>init</tt> 或 <tt>max</tt> 的值为负但不为 <tt>-1</tt>；或</li>
     * <li> <tt>used</tt> 或 <tt>committed</tt> 的值为负；或</li>
     * <li> <tt>used</tt> 大于 <tt>committed</tt> 的值；或</li>
     * <li> <tt>committed</tt> 大于 <tt>max</tt> 的值（如果定义了 <tt>max</tt>）。</li>
     * </ul>
     */
    public MemoryUsage(long init,
                       long used,
                       long committed,
                       long max) {
        if (init < -1) {
            throw new IllegalArgumentException( "init parameter = " +
                init + " is negative but not -1.");
        }
        if (max < -1) {
            throw new IllegalArgumentException( "max parameter = " +
                max + " is negative but not -1.");
        }
        if (used < 0) {
            throw new IllegalArgumentException( "used parameter = " +
                used + " is negative.");
        }
        if (committed < 0) {
            throw new IllegalArgumentException( "committed parameter = " +
                committed + " is negative.");
        }
        if (used > committed) {
            throw new IllegalArgumentException( "used = " + used +
                " should be <= committed = " + committed);
        }
        if (max >= 0 && committed > max) {
            throw new IllegalArgumentException( "committed = " + committed +
                " should be < max = " + max);
        }

        this.init = init;
        this.used = used;
        this.committed = committed;
        this.max = max;
    }

    /**
     * 从一个 {@link CompositeData CompositeData} 构造一个 <tt>MemoryUsage</tt> 对象。
     */
    private MemoryUsage(CompositeData cd) {
        // 验证输入的复合数据
        MemoryUsageCompositeData.validateCompositeData(cd);

        this.init = MemoryUsageCompositeData.getInit(cd);
        this.used = MemoryUsageCompositeData.getUsed(cd);
        this.committed = MemoryUsageCompositeData.getCommitted(cd);
        this.max = MemoryUsageCompositeData.getMax(cd);
    }

                /**
     * 返回 Java 虚拟机最初从操作系统请求用于内存管理的内存量（以字节为单位）。
     * 如果初始内存大小未定义，则此方法返回 <tt>-1</tt>。
     *
     * @return 初始内存大小（以字节为单位）；
     * <tt>-1</tt> 如果未定义。
     */
    public long getInit() {
        return init;
    }

    /**
     * 返回已使用的内存量（以字节为单位）。
     *
     * @return 已使用的内存量（以字节为单位）。
     *
     */
    public long getUsed() {
        return used;
    };

    /**
     * 返回 Java 虚拟机承诺使用的内存量（以字节为单位）。此数量的内存是 Java 虚拟机保证使用的。
     *
     * @return 承诺的内存量（以字节为单位）。
     *
     */
    public long getCommitted() {
        return committed;
    };

    /**
     * 返回用于内存管理的最大内存量（以字节为单位）。如果最大内存大小未定义，此方法返回 <tt>-1</tt>。
     *
     * <p> 如果此最大内存量大于已承诺的内存量，则不能保证此数量的内存可用于内存管理。
     * 即使已使用的内存量不超过此最大大小，Java 虚拟机也可能无法分配内存。
     *
     * @return 最大内存量（以字节为单位）；
     * <tt>-1</tt> 如果未定义。
     */
    public long getMax() {
        return max;
    };

    /**
     * 返回此内存使用情况的描述性表示。
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("init = " + init + "(" + (init >> 10) + "K) ");
        buf.append("used = " + used + "(" + (used >> 10) + "K) ");
        buf.append("committed = " + committed + "(" +
                   (committed >> 10) + "K) " );
        buf.append("max = " + max + "(" + (max >> 10) + "K)");
        return buf.toString();
    }

    /**
     * 返回由给定 <tt>CompositeData</tt> 表示的 <tt>MemoryUsage</tt> 对象。给定的 <tt>CompositeData</tt>
     * 必须包含以下属性：
     *
     * <blockquote>
     * <table border summary="给定 CompositeData 包含的属性及其类型">
     * <tr>
     *   <th align=left>属性名称</th>
     *   <th align=left>类型</th>
     * </tr>
     * <tr>
     *   <td>init</td>
     *   <td><tt>java.lang.Long</tt></td>
     * </tr>
     * <tr>
     *   <td>used</td>
     *   <td><tt>java.lang.Long</tt></td>
     * </tr>
     * <tr>
     *   <td>committed</td>
     *   <td><tt>java.lang.Long</tt></td>
     * </tr>
     * <tr>
     *   <td>max</td>
     *   <td><tt>java.lang.Long</tt></td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * @param cd 表示 <tt>MemoryUsage</tt> 的 <tt>CompositeData</tt>
     *
     * @throws IllegalArgumentException 如果 <tt>cd</tt> 不表示具有上述属性的 <tt>MemoryUsage</tt>。
     *
     * @return 由 <tt>cd</tt> 表示的 <tt>MemoryUsage</tt> 对象，如果 <tt>cd</tt> 不为 <tt>null</tt>；
     *         否则返回 <tt>null</tt>。
     */
    public static MemoryUsage from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof MemoryUsageCompositeData) {
            return ((MemoryUsageCompositeData) cd).getMemoryUsage();
        } else {
            return new MemoryUsage(cd);
        }

    }
}
