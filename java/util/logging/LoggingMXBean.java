/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.util.logging;


/**
 * 日志设施的管理接口。建议使用 {@link java.lang.management.PlatformLoggingMXBean} 管理接口，
 * 该接口实现了此 {@code LoggingMXBean} 中定义的所有属性。可以使用
 * {@link java.lang.management.ManagementFactory#getPlatformMXBean(Class)
 * ManagementFactory.getPlatformMXBean} 方法获取表示日志管理接口的
 * {@code PlatformLoggingMXBean} 对象。
 *
 * <p>存在一个全局的 <tt>LoggingMXBean</tt> 实例。此实例是一个 {@link javax.management.MXBean MXBean}，
 * 可以通过调用 {@link LogManager#getLoggingMXBean} 方法或从
 * {@linkplain java.lang.management.ManagementFactory#getPlatformMBeanServer
 * 平台 <tt>MBeanServer</tt>} 获取。
 * <p>
 * 在 {@code MBeanServer} 中唯一标识日志管理接口的
 * {@link javax.management.ObjectName ObjectName} 是：
 * <pre>
 *    {@link LogManager#LOGGING_MXBEAN_NAME java.util.logging:type=Logging}
 * </pre>
 * <p>
 * 在平台 {@code MBeanServer} 中注册的实例也是一个 {@link java.lang.management.PlatformLoggingMXBean}。
 *
 * @author  Ron Mann
 * @author  Mandy Chung
 * @since   1.5
 *
 * @see java.lang.management.PlatformLoggingMXBean
 */
public interface LoggingMXBean {

    /**
     * 返回当前注册的日志记录器名称列表。此方法调用 {@link LogManager#getLoggerNames} 并返回日志记录器名称列表。
     *
     * @return 一个 <tt>String</tt> 列表，每个元素都是一个当前注册的 <tt>Logger</tt> 名称。
     */
    public java.util.List<String> getLoggerNames();

    /**
     * 获取指定日志记录器关联的日志级别名称。如果指定的日志记录器不存在，返回 <tt>null</tt>。
     * 此方法首先查找给定名称的日志记录器，然后通过调用：
     * <blockquote>
     *   {@link Logger#getLevel Logger.getLevel()}.{@link Level#getName getName()};
     * </blockquote>
     * 返回日志级别的名称。
     *
     * <p>
     * 如果指定日志记录器的 <tt>Level</tt> 为 <tt>null</tt>，这意味着该日志记录器的有效级别是从其父级继承的，
     * 将返回一个空字符串。
     *
     * @param loggerName 要检索的 <tt>Logger</tt> 名称。
     *
     * @return 指定日志记录器的日志级别名称；如果指定日志记录器的日志级别为 <tt>null</tt>，返回空字符串。
     *         如果指定的日志记录器不存在，返回 <tt>null</tt>。
     *
     * @see Logger#getLevel
     */
    public String getLoggerLevel(String loggerName);

    /**
     * 将指定的日志记录器设置为指定的新级别。如果 <tt>levelName</tt> 不为 <tt>null</tt>，则将指定日志记录器的级别设置为与 <tt>levelName</tt> 匹配的解析后的 <tt>Level</tt>。
     * 如果 <tt>levelName</tt> 为 <tt>null</tt>，则将指定日志记录器的级别设置为 <tt>null</tt>，并且该日志记录器的有效级别将从其最近的具有特定（非空）级别值的祖先继承。
     *
     * @param loggerName 要设置的 <tt>Logger</tt> 名称。必须非空。
     * @param levelName 要在指定日志记录器上设置的级别名称，或 <tt>null</tt> 表示从其最近的祖先继承级别。
     *
     * @throws IllegalArgumentException 如果指定的日志记录器不存在，或 <tt>levelName</tt> 不是有效的级别名称。
     *
     * @throws SecurityException 如果存在安全经理，并且调用者没有 LoggingPermission("control") 权限。
     *
     * @see Logger#setLevel
     */
    public void setLoggerLevel(String loggerName, String levelName);

    /**
     * 返回指定日志记录器的父级名称。如果指定的日志记录器不存在，返回 <tt>null</tt>。
     * 如果指定的日志记录器是命名空间中的根 <tt>Logger</tt>，结果将是一个空字符串。
     *
     * @param loggerName 一个 <tt>Logger</tt> 的名称。
     *
     * @return 最近存在的父日志记录器的名称；如果指定的日志记录器是根日志记录器，返回空字符串。
     *         如果指定的日志记录器不存在，返回 <tt>null</tt>。
     */
    public String getParentLoggerName(String loggerName);
}
