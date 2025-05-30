/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 用于管理缓冲池的接口，例如直接或映射的缓冲区池。
 *
 * <p> 实现此接口的类是一个
 * {@link javax.management.MXBean}。Java
 * 虚拟机有一个或多个此类接口的实现。可以使用 {@link
 * java.lang.management.ManagementFactory#getPlatformMXBeans getPlatformMXBeans}
 * 方法获取表示缓冲池管理接口的 {@code BufferPoolMXBean} 对象列表，如下所示：
 * <pre>
 *     List&lt;BufferPoolMXBean&gt; pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
 * </pre>
 *
 * <p> 管理接口还注册到平台 {@link
 * javax.management.MBeanServer MBeanServer}。在 {@code MBeanServer} 中唯一标识
 * 管理接口的 {@link
 * javax.management.ObjectName ObjectName} 的形式为：
 * <pre>
 *     java.nio:type=BufferPool,name=<i>pool name</i>
 * </pre>
 * 其中 <em>pool name</em> 是缓冲池的 {@link #getName 名称}。
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
     * 返回此池中缓冲区总容量的估计值。
     * 缓冲区的容量是它包含的元素数量，此方法返回的值是池中缓冲区总容量的估计值（以字节为单位）。
     *
     * @return  此池中缓冲区总容量的估计值（以字节为单位）
     */
    long getTotalCapacity();

    /**
     * 返回 Java 虚拟机为此缓冲池使用的内存的估计值。
     * 此方法返回的值可能与池中缓冲区总 {@link #getTotalCapacity 容量} 的估计值不同。这种差异可能是由于对齐、内存分配器和其他实现特定的原因。
     *
     * @return  Java 虚拟机为此缓冲池使用的内存估计值（以字节为单位），如果无法获得内存使用量的估计值，则返回 {@code -1L}
     */
    long getMemoryUsed();
}
