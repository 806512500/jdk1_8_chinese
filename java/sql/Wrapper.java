/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * 用于提供在实例实际上是代理类时检索代理实例的委托实例的能力的 JDBC 类的接口。
 * <p>
 * 许多 JDBC 驱动程序实现采用包装器模式来提供超出传统 JDBC API 的数据源特定扩展。开发人员可能希望访问
 * 这些被包装的资源（代理类实例表示的实际资源）。此接口描述了一种标准机制，用于访问
 * 由其代理表示的这些包装资源，以允许直接访问资源委托。
 *
 * @since 1.6
 */

public interface Wrapper {

    /**
     * 返回实现给定接口的对象，以允许访问非标准方法或代理未公开的标准方法。
     *
     * 如果接收者实现了该接口，则结果是接收者或接收者的代理。如果接收者是包装器
     * 且包装的对象实现了该接口，则结果是包装的对象或包装对象的代理。否则返回
     * 递归调用包装对象的 <code>unwrap</code> 或该结果的代理。如果接收者不是
     * 包装器且不实现该接口，则抛出 <code>SQLException</code>。
     *
     * @param <T> 由此类对象建模的类的类型
     * @param iface 接口定义的类，结果必须实现该接口。
     * @return 实现接口的对象。可能是实际实现对象的代理。
     * @throws java.sql.SQLException 如果找不到实现接口的对象
     * @since 1.6
     */
        <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;

    /**
     * 如果此对象实现了接口参数或直接或间接包装了一个实现该接口的对象，则返回 true。否则返回 false。如果此对象实现了接口，则返回 true，
     * 否则如果此对象是包装器，则返回递归调用包装对象的 <code>isWrapperFor</code> 的结果。如果此对象不实现接口且不是包装器，则返回 false。
     * 应该将此方法实现为与 <code>unwrap</code> 相比的低成本操作，以便调用者可以使用此方法来避免可能失败的昂贵的 <code>unwrap</code> 调用。如果此方法
     * 返回 true，则使用相同参数调用 <code>unwrap</code> 应该成功。
     *
     * @param iface 定义接口的类。
     * @return 如果此对象实现接口或直接或间接包装了一个实现该接口的对象，则返回 true。
     * @throws java.sql.SQLException 如果在确定此对象是否是具有给定接口的对象的包装器时发生错误。
     * @since 1.6
     */
    boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException;

}
