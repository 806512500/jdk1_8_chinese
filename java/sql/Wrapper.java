/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * JDBC类的接口，提供在实例实际上是代理类时检索代理实例的能力。
 * <p>
 * 许多JDBC驱动程序实现使用包装器模式来提供超出传统JDBC API的数据源特定扩展。开发人员可能希望访问
 * 这些被包装的资源（代理类实例表示的实际资源）。此接口描述了一种标准机制，以访问
 * 通过其代理表示的这些包装资源，允许直接访问资源代理。
 *
 * @since 1.6
 */

public interface Wrapper {

    /**
     * 返回实现给定接口的对象，以允许访问非标准方法或代理未公开的标准方法。
     *
     * 如果接收者实现了该接口，则结果是接收者本身或接收者的代理。如果接收者是包装器
     * 并且包装的对象实现了该接口，则结果是包装的对象或该对象的代理。否则，返回递归调用
     * 包装对象的<code>unwrap</code>的结果或该结果的代理。如果接收者不是
     * 包装器且未实现该接口，则抛出<code>SQLException</code>。
     *
     * @param <T> 由此类对象建模的类的类型
     * @param iface 一个定义结果必须实现的接口的类。
     * @return 实现接口的对象。可能是实际实现对象的代理。
     * @throws java.sql.SQLException 如果未找到实现该接口的对象
     * @since 1.6
     */
        <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;

    /**
     * 如果此对象实现了接口参数或直接或间接包装了一个实现该接口的对象，则返回true。否则返回false。如果此对象实现了该接口，则返回true，
     * 否则，如果此对象是包装器，则返回递归调用包装对象的<code>isWrapperFor</code>的结果。如果此对象未实现该接口且不是包装器，则返回false。
     * 应该将此方法实现为与<code>unwrap</code>相比的低成本操作，以便调用者可以使用此方法来避免可能失败的昂贵的<code>unwrap</code>调用。如果此方法
     * 返回true，则使用相同参数调用<code>unwrap</code>应该成功。
     *
     * @param iface 定义接口的类。
     * @return 如果此对象实现了接口或直接或间接包装了一个实现该接口的对象，则返回true。
     * @throws java.sql.SQLException 如果在确定此对象是否为具有给定接口的对象的包装器时发生错误。
     * @since 1.6
     */
    boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException;

}
