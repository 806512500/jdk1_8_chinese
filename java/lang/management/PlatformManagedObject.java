/*
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
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

import javax.management.ObjectName;

/**
 * 一个平台管理对象是用于监控和管理 Java 平台中组件的
 * {@linkplain javax.management.MXBean JMX MXBean}。
 * 每个平台管理对象在 {@linkplain ManagementFactory#getPlatformMBeanServer
 * 平台 MBeanServer} 访问中都有一个唯一的
 * <a href="ManagementFactory.html#MXBean">对象名称</a>。
 * 所有平台 MXBeans 都将实现此接口。
 *
 * <p>
 * 注意：
 * 平台 MXBean 接口（即 {@code PlatformManagedObject} 的所有子接口）
 * 仅由 Java 平台实现。未来 Java SE 版本可能会在这些接口中添加新方法。
 * 此外，此 {@code PlatformManagedObject} 接口仅用于平台管理接口的扩展，
 * 而不是用于应用程序。
 *
 * @see ManagementFactory
 * @since 1.7
 */
public interface PlatformManagedObject {
    /**
     * 返回一个表示此平台管理对象对象名称的
     * {@link ObjectName ObjectName} 实例。
     *
     * @return 一个表示此平台管理对象对象名称的
     * {@link ObjectName ObjectName} 实例。
     */
    public ObjectName getObjectName();
}
