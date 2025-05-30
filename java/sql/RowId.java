/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
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
 *
 * 在 Java 编程语言中表示 SQL ROWID 值。SQL ROWID 是一种内置类型，其值可以被视为其标识的数据库表中行的地址。该地址是逻辑的还是物理的，由其来源数据源决定。
 * <p>
 * <code>ResultSet</code>、<code>CallableStatement</code> 和 <code>PreparedStatement</code> 接口中的方法，如 <code>getRowId</code> 和 <code>setRowId</code>，
 * 允许程序员访问 SQL <code>ROWID</code> 值。<code>RowId</code> 接口提供了一种方法，用于将 <code>ROWID</code> 的值表示为字节数组或 <code>String</code>。
 * <p>
 * <code>DatabaseMetaData</code> 接口中的 <code>getRowIdLifetime</code> 方法可用于确定 <code>RowId</code> 对象是否在其创建的事务期间、其创建的会话期间或其标识的行未被删除的情况下有效。
 * 除了指定其在来源数据源外部的有效生命周期外，<code>getRowIdLifetime</code> 还指定了 <code>ROWID</code> 值在其来源数据源内的有效生命周期。在这方面，它与大对象不同，因为大对象在其来源数据源内的有效生命周期没有限制。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则 <code>RowId</code> 接口上的所有方法都必须完全实现。
 *
 * @see java.sql.DatabaseMetaData
 * @since 1.6
 */

public interface RowId {
    /**
     * 将此 <code>RowId</code> 与指定的对象进行比较。结果为 <code>true</code> 当且仅当参数不为 null 且是一个表示与该对象相同的 ROWID 的 RowId 对象。
     * <p>
     * 在比较两个 <code>RowId</code> 时，考虑它们的来源和有效生命周期非常重要。如果两个 <code>RowId</code> 都有效，并且都来自同一数据源的同一表，则如果它们相等，它们标识相同的行；如果一个或多个不再保证有效，或者它们来自不同的数据源或同一数据源的不同表，它们可能相等但仍然不标识相同的行。
     *
     * @param obj 要与之比较的 <code>Object</code>。
     * @return 如果 <code>RowId</code> 相等，则返回 true；否则返回 false
     * @since 1.6
     */
    boolean equals(Object obj);

    /**
     * 返回一个表示此 <code>java.sql.RowId</code> 对象指定的 SQL <code>ROWID</code> 值的字节数组。
     *
     * @return 一个字节数组，其长度由提供连接的驱动程序确定，表示此 java.sql.RowId 对象指定的 ROWID 值。
     */
     byte[] getBytes();

     /**
      * 返回一个表示此 <code>java.sql.RowId</code> 对象指定的 SQL ROWID 值的字符串。
      * <p>
      * 类似于 <code>java.sql.Date.toString()</code> 返回其 DATE 的内容为字符串 "2004-03-17" 而不是 SQL 中的 DATE 字面量（即字符串 DATE "2004-03-17"），toString()
      * 返回其 ROWID 的内容，格式由提供连接的驱动程序确定，可能不是 <code>ROWID</code> 字面量。
      *
      * @return 一个字符串，其格式由提供连接的驱动程序确定，表示此 <code>java.sql.RowId</code> 对象指定的 <code>ROWID</code> 值。
      */
     String toString();

     /**
      * 返回此 <code>RowId</code> 对象的哈希码值。
      *
      * @return <code>RowId</code> 的哈希码
      */
     int hashCode();

}
