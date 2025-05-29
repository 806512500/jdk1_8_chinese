/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 日志设施的管理接口。
 *
 * <p>存在一个全局的 <tt>PlatformLoggingMXBean</tt> 实例。
 * 可以使用 {@link java.lang.management.ManagementFactory#getPlatformMXBean(Class)
 * ManagementFactory.getPlatformMXBean} 方法来获取
 * {@code PlatformLoggingMXBean} 对象，如下所示：
 * <pre>
 *     PlatformLoggingMXBean logging = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
 * </pre>
 * {@code PlatformLoggingMXBean} 对象也注册在平台
 * {@linkplain java.lang.management.ManagementFactory#getPlatformMBeanServer
 * MBeanServer} 中。
 * 用于在 MBeanServer 中唯一标识 {@code PlatformLoggingMXBean} 的
 * {@link javax.management.ObjectName ObjectName} 是：
 * <pre>
 *      {@link java.util.logging.LogManager#LOGGING_MXBEAN_NAME java.util.logging:type=Logging}
 * </pre>
 *
 * <p>在平台 <tt>MBeanServer</tt> 中使用此 {@code ObjectName} 注册的实例
 * 实现了 {@link java.util.logging.LoggingMXBean} 定义的所有属性。
 *
 * @since   1.7
 */
public interface PlatformLoggingMXBean extends PlatformManagedObject {

    /**
     * 返回当前注册的
     * {@linkplain java.util.logging.Logger logger} 名称列表。此方法
     * 调用 {@link java.util.logging.LogManager#getLoggerNames} 并
     * 返回一个包含 logger 名称的列表。
     *
     * @return 一个包含当前注册的 {@code Logger} 名称的 {@code String} 列表。
     */
    java.util.List<String> getLoggerNames();

    /**
     * 获取与指定 logger 关联的日志 {@linkplain java.util.logging.Logger#getLevel
     * 级别} 的名称。
     * 如果指定的 logger 不存在，则返回 {@code null}。
     * 此方法首先查找给定名称的 logger，然后通过调用：
     * <blockquote>
     *   {@link java.util.logging.Logger#getLevel
     *    Logger.getLevel()}.{@link java.util.logging.Level#getName getName()};
     * </blockquote>
     * 返回日志级别的名称。
     *
     * <p>
     * 如果指定 logger 的 {@code Level} 为 {@code null}，
     * 这意味着此 logger 的有效级别是从其父级继承的，
     * 将返回一个空字符串。
     *
     * @param loggerName 要检索的 {@code Logger} 的名称。
     *
     * @return 指定 logger 的日志级别名称；如果指定 logger 的日志级别为
     *         {@code null}，则返回一个空字符串。如果指定的 logger 不存在，
     *         则返回 {@code null}。
     *
     * @see java.util.logging.Logger#getLevel
     */
    String getLoggerLevel(String loggerName);

    /**
     * 将指定的 logger 设置为指定的新
     * {@linkplain java.util.logging.Logger#setLevel 级别}。
     * 如果 {@code levelName} 不为 {@code null}，则将指定 logger 的级别设置为
     * 与 {@code levelName} 匹配的解析后的 {@link java.util.logging.Level Level}。
     * 如果 {@code levelName} 为 {@code null}，则将指定 logger 的级别设置为 {@code null}，
     * 并且 logger 的有效级别将从其最近的具有特定（非 null）级别值的祖先继承。
     *
     * @param loggerName 要设置的 {@code Logger} 的名称。
     *                   必须非 null。
     * @param levelName 要设置在指定 logger 上的级别名称，
     *                 或者如果设置级别以从其最近的祖先继承，则为 {@code null}。
     *
     * @throws IllegalArgumentException 如果指定的 logger 不存在，或者 {@code levelName} 不是有效的级别名称。
     *
     * @throws SecurityException 如果存在安全经理，并且调用者没有 LoggingPermission("control")。
     *
     * @see java.util.logging.Logger#setLevel
     */
    void setLoggerLevel(String loggerName, String levelName);

    /**
     * 返回指定 logger 的
     * {@linkplain java.util.logging.Logger#getParent 父级} 的名称。
     * 如果指定的 logger 不存在，则返回 {@code null}。
     * 如果指定的 logger 是命名空间中的根 {@code Logger}，
     * 结果将是一个空字符串。
     *
     * @param loggerName 一个 {@code Logger} 的名称。
     *
     * @return 最近存在的父 logger 的名称；
     *         如果指定的 logger 是根 logger，则返回一个空字符串。
     *         如果指定的 logger 不存在，则返回 {@code null}。
     */
    String getParentLoggerName(String loggerName);
}
