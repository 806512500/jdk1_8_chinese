/*
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
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

import javax.management.ObjectName;

/**
 * 平台管理对象是一个 {@linkplain javax.management.MXBean JMX MXBean}，
 * 用于监控和管理 Java 平台中的组件。
 * 每个平台管理对象在 {@linkplain ManagementFactory#getPlatformMBeanServer
 * 平台 MBeanServer} 访问中都有一个唯一的
 * <a href="ManagementFactory.html#MXBean">对象名称</a>。
 * 所有平台 MXBeans 都将实现此接口。
 *
 * <p>
 * 注意：
 * 平台 MXBean 接口（即 {@code PlatformManagedObject} 的所有子接口）
 * 仅由 Java 平台实现。这些接口中可能会在未来 Java SE 版本中添加新方法。
 * 此外，此 {@code PlatformManagedObject} 接口仅用于平台的管理接口扩展，而不用于应用程序。
 *
 * @see ManagementFactory
 * @since 1.7
 */
public interface PlatformManagedObject {
    /**
     * 返回一个 {@link ObjectName ObjectName} 实例，表示
     * 此平台管理对象的对象名称。
     *
     * @return 一个 {@link ObjectName ObjectName} 实例，表示
     * 此平台管理对象的对象名称。
     */
    public ObjectName getObjectName();
}
