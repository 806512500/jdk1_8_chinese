/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.management;
import javax.management.openmbean.CompositeData;
import sun.management.MemoryNotifInfoCompositeData;

/**
 * 内存通知的信息。
 *
 * <p>
 * 当 Java 虚拟机检测到内存池的内存使用量超过阈值时，{@link MemoryMXBean} 会发出内存通知。
 * 发出的通知将包含检测到条件的内存通知信息：
 * <ul>
 *   <li>内存池的名称。</li>
 *   <li>构建通知时内存池的内存使用情况。</li>
 *   <li>构建通知时内存使用量跨越阈值的次数。
 *       对于使用阈值通知，此计数将是
 *       {@link MemoryPoolMXBean#getUsageThresholdCount 使用阈值计数}。对于收集阈值通知，
 *       此计数将是
 *       {@link MemoryPoolMXBean#getCollectionUsageThresholdCount
 *       收集使用阈值计数}。
 *       </li>
 * </ul>
 *
 * <p>
 * 一个表示 <tt>MemoryNotificationInfo</tt> 对象的 {@link CompositeData CompositeData}
 * 存储在 {@link javax.management.Notification#setUserData 用户数据}
 * 中的一个 {@link javax.management.Notification 通知} 中。
 * 提供了 {@link #from from} 方法将 <tt>CompositeData</tt> 转换为 <tt>MemoryNotificationInfo</tt>
 * 对象。例如：
 *
 * <blockquote><pre>
 *      Notification notif;
 *
 *      // 接收由 MemoryMXBean 发出的通知并设置为 notif
 *      ...
 *
 *      String notifType = notif.getType();
 *      if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED) ||
 *          notifType.equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
 *          // 检索内存通知信息
 *          CompositeData cd = (CompositeData) notif.getUserData();
 *          MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
 *          ....
 *      }
 * </pre></blockquote>
 *
 * <p>
 * <tt>MemoryMXBean</tt> 发出的通知类型有：
 * <ul>
 *   <li>一个 {@link #MEMORY_THRESHOLD_EXCEEDED
 *       使用阈值超过通知}。
 *       <br>当内存池的内存使用量增加并达到或超过其
 *       <a href="MemoryPoolMXBean.html#UsageThreshold"> 使用阈值</a> 时，将发出此通知。
 *       在内存使用量返回到低于使用阈值之前，后续跨越使用阈值值不会导致进一步的通知。
 *       <p></li>
 *   <li>一个 {@link #MEMORY_COLLECTION_THRESHOLD_EXCEEDED
 *       收集使用阈值超过通知}。
 *       <br>当 Java 虚拟机在内存池中回收未使用的对象后，内存池的内存使用量大于或等于其
 *       <a href="MemoryPoolMXBean.html#CollectionThreshold">
 *       收集使用阈值</a> 时，将发出此通知。</li>
 * </ul>
 *
 * @author  Mandy Chung
 * @since   1.5
 *
 */
public class MemoryNotificationInfo {
    private final String poolName;
    private final MemoryUsage usage;
    private final long count;

    /**
     * 通知类型，表示
     * 内存池的内存使用量已
     * 达到或超过其
     * <a href="MemoryPoolMXBean.html#UsageThreshold"> 使用阈值</a>。
     * 此通知由 {@link MemoryMXBean} 发出。
     * 在内存使用量返回到低于使用阈值之前，后续跨越使用阈值值不会导致进一步的通知。
     * 该通知类型的值为
     * <tt>java.management.memory.threshold.exceeded</tt>。
     */
    public static final String MEMORY_THRESHOLD_EXCEEDED =
        "java.management.memory.threshold.exceeded";

    /**
     * 通知类型，表示
     * 内存池的内存使用量在 Java 虚拟机回收该内存池中的未使用对象后大于或等于其
     * <a href="MemoryPoolMXBean.html#CollectionThreshold">
     * 收集使用阈值</a>。
     * 此通知由 {@link MemoryMXBean} 发出。
     * 该通知类型的值为
     * <tt>java.management.memory.collection.threshold.exceeded</tt>。
     */
    public static final String MEMORY_COLLECTION_THRESHOLD_EXCEEDED =
        "java.management.memory.collection.threshold.exceeded";

    /**
     * 构造一个 <tt>MemoryNotificationInfo</tt> 对象。
     *
     * @param poolName 触发此通知的内存池的名称。
     * @param usage 内存池的内存使用情况。
     * @param count 阈值跨越次数。
     */
    public MemoryNotificationInfo(String poolName,
                                  MemoryUsage usage,
                                  long count) {
        if (poolName == null) {
            throw new NullPointerException("Null poolName");
        }
        if (usage == null) {
            throw new NullPointerException("Null usage");
        }

        this.poolName = poolName;
        this.usage = usage;
        this.count = count;
    }

    MemoryNotificationInfo(CompositeData cd) {
        MemoryNotifInfoCompositeData.validateCompositeData(cd);

        this.poolName = MemoryNotifInfoCompositeData.getPoolName(cd);
        this.usage = MemoryNotifInfoCompositeData.getUsage(cd);
        this.count = MemoryNotifInfoCompositeData.getCount(cd);
    }

    /**
     * 返回触发此通知的内存池的名称。
     * 内存池的内存使用量已跨越阈值。
     *
     * @return 触发此通知的内存池的名称。
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * 返回构建此通知时内存池的内存使用情况。
     *
     * @return 构建此通知时内存池的内存使用情况。
     */
    public MemoryUsage getUsage() {
        return usage;
    }

    /**
     * 返回构建通知时内存使用量跨越阈值的次数。
     * 对于使用阈值通知，此计数将是
     * {@link MemoryPoolMXBean#getUsageThresholdCount 阈值
     * 计数}。对于收集阈值通知，
     * 此计数将是
     * {@link MemoryPoolMXBean#getCollectionUsageThresholdCount
     * 收集使用阈值计数}。
     *
     * @return 构建通知时内存使用量跨越阈值的次数。
     */
    public long getCount() {
        return count;
    }

    /**
     * 返回由给定的 <tt>CompositeData</tt> 表示的 <tt>MemoryNotificationInfo</tt> 对象。
     * 给定的 <tt>CompositeData</tt> 必须包含以下属性：
     * <blockquote>
     * <table border summary="给定的 CompositeData 包含的属性和类型">
     * <tr>
     *   <th align=left>属性名称</th>
     *   <th align=left>类型</th>
     * </tr>
     * <tr>
     *   <td>poolName</td>
     *   <td><tt>java.lang.String</tt></td>
     * </tr>
     * <tr>
     *   <td>usage</td>
     *   <td><tt>javax.management.openmbean.CompositeData</tt></td>
     * </tr>
     * <tr>
     *   <td>count</td>
     *   <td><tt>java.lang.Long</tt></td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * @param cd 表示 <tt>MemoryNotificationInfo</tt> 的 <tt>CompositeData</tt>
     *
     * @throws IllegalArgumentException 如果 <tt>cd</tt> 不表示 <tt>MemoryNotificationInfo</tt> 对象。
     *
     * @return 如果 <tt>cd</tt> 不为 <tt>null</tt>，则返回由 <tt>cd</tt> 表示的 <tt>MemoryNotificationInfo</tt> 对象；
     *         否则返回 <tt>null</tt>。
     */
    public static MemoryNotificationInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof MemoryNotifInfoCompositeData) {
            return ((MemoryNotifInfoCompositeData) cd).getMemoryNotifInfo();
        } else {
            return new MemoryNotificationInfo(cd);
        }
    }
}
