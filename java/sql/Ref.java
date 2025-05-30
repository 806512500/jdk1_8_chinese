/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * Java 编程语言中 SQL <code>REF</code> 值的映射，它是数据库中 SQL 结构化类型值的引用。
 * <P>
 * SQL <code>REF</code> 值存储在包含引用 SQL 结构化类型实例的表中，每个 <code>REF</code> 值都是该表中一个实例的唯一标识符。
 * SQL <code>REF</code> 值可以用作引用的 SQL 结构化类型的替代，无论是作为表中的列值还是结构化类型中的属性值。
 * <P>
 * 由于 SQL <code>REF</code> 值是指向 SQL 结构化类型的逻辑指针，因此 <code>Ref</code> 对象默认也是逻辑指针。
 * 因此，将 SQL <code>REF</code> 值检索为 <code>Ref</code> 对象不会在客户端上物化结构化类型的属性。
 * <P>
 * 可以使用 <code>PreparedStatement.setRef</code> 方法将 <code>Ref</code> 对象存储在数据库中。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则必须完全实现 <code>Ref</code> 接口上的所有方法。
 *
 * @see Struct
 * @since 1.2
 */
public interface Ref {

    /**
     * 检索此 <code>Ref</code> 对象引用的 SQL 结构化类型的完全限定 SQL 名称。
     *
     * @return 引用的 SQL 结构化类型的完全限定 SQL 名称
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    String getBaseTypeName() throws SQLException;

    /**
     * 检索引用的对象并使用给定的类型映射将其映射到 Java 类型。
     *
     * @param map 包含要使用的映射的 <code>java.util.Map</code> 对象（引用的 SQL 结构化类型的完全限定名称和 SQL 结构化类型将映射到的 <code>SQLData</code> 实现的类对象）
     * @return 一个 Java <code>Object</code>，它是此 <code>Ref</code> 对象引用的 SQL 结构化类型的自定义映射
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     * @see #setObject
     */
    Object getObject(java.util.Map<String,Class<?>> map) throws SQLException;


    /**
     * 检索此 <code>Ref</code> 对象引用的 SQL 结构化类型实例。如果连接的类型映射中有条目，则该实例将自定义映射到类型映射中指示的 Java 类。否则，结构化类型实例将映射到 <code>Struct</code> 对象。
     *
     * @return 一个 Java <code>Object</code>，它是此 <code>Ref</code> 对象引用的 SQL 结构化类型的映射
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     * @see #setObject
     */
    Object getObject() throws SQLException;

    /**
     * 将此 <code>Ref</code> 对象引用的结构化类型值设置为给定的 <code>Object</code> 实例。驱动程序在将其发送到数据库时会将其转换为 SQL 结构化类型。
     *
     * @param value 一个 <code>Object</code>，表示此 <code>Ref</code> 对象将引用的 SQL 结构化类型实例
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     * @see #getObject()
     * @see #getObject(Map)
     * @see PreparedStatement#setObject(int, Object)
     * @see CallableStatement#setObject(String, Object)
     */
    void setObject(Object value) throws SQLException;

}
