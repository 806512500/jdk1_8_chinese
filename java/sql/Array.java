
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 在 Java 编程语言中映射 SQL 类型 <code>ARRAY</code>。
 * 默认情况下，<code>Array</code> 值是一个事务持续时间的引用，指向 SQL <code>ARRAY</code> 值。
 * 默认情况下，<code>Array</code> 对象使用内部的 SQL LOCATOR(array) 实现，这意味着 <code>Array</code> 对象包含一个逻辑指针，指向 SQL <code>ARRAY</code> 值的数据，而不是包含 <code>ARRAY</code> 值的数据。
 * <p>
 * <code>Array</code> 接口提供了方法，用于将 SQL <code>ARRAY</code> 值的数据以数组或 <code>ResultSet</code> 对象的形式带入客户端。
 * 如果 SQL <code>ARRAY</code> 的元素是 UDT，它们可以进行自定义映射。要创建自定义映射，程序员必须做两件事：
 * <ul>
 * <li>为要自定义映射的 UDT 创建一个实现 {@link SQLData} 接口的类。
 * <li>在类型映射中进行条目，包含：
 *   <ul>
 *   <li>UDT 的完全限定 SQL 类型名称
 *   <li>实现 <code>SQLData</code> 的 <code>Class</code> 对象
 *   </ul>
 * </ul>
 * <p>
 * 当为基本类型提供类型映射条目的类型映射被传递给方法 <code>getArray</code> 和 <code>getResultSet</code> 时，将使用该映射来映射 <code>ARRAY</code> 值的元素。
 * 如果没有提供类型映射，通常情况下是这样，将使用连接的类型映射。
 * 如果连接的类型映射或传递给方法的类型映射没有基本类型的条目，则根据标准映射映射元素。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则 <code>Array</code> 接口上的所有方法都必须完全实现。
 *
 * @since 1.2
 */

public interface Array {

  /**
   * 检索由这个 <code>Array</code> 对象指定的数组中的元素的 SQL 类型名称。
   * 如果元素是内置类型，它返回元素的数据库特定类型名称。
   * 如果元素是用户定义类型 (UDT)，此方法返回完全限定的 SQL 类型名称。
   *
   * @return 一个 <code>String</code>，表示内置基本类型的数据库特定名称；或表示基本类型是 UDT 的完全限定 SQL 类型名称
   * @exception SQLException 如果在尝试访问类型名称时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  String getBaseTypeName() throws SQLException;

  /**
   * 检索由这个 <code>Array</code> 对象指定的数组中的元素的 JDBC 类型。
   *
   * @return 一个来自类 {@link java.sql.Types} 的常量，表示由这个 <code>Array</code> 对象指定的数组中的元素的类型代码
   * @exception SQLException 如果在尝试访问基本类型时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  int getBaseType() throws SQLException;

  /**
   * 以 Java 编程语言中的数组形式检索由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的内容。
   * 此方法版本使用与连接关联的类型映射进行类型映射的自定义。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于将映射到基本数据类型的基类型具体化时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @return 一个 Java 编程语言中的数组，包含由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的有序元素
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray() throws SQLException;

  /**
   * 检索由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的内容。
   * 此方法使用指定的 <code>map</code> 进行类型映射的自定义，除非数组的基本类型与 <code>map</code> 中的用户定义类型不匹配，此时它使用标准映射。
   * 此版本的 <code>getArray</code> 方法使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于将映射到基本数据类型的基类型具体化时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @param map 包含 SQL 类型名称到 Java 编程语言类映射的 <code>java.util.Map</code> 对象
   * @return 一个 Java 编程语言中的数组，包含由这个对象指定的 SQL 数组的有序元素
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(java.util.Map<String,Class<?>> map) throws SQLException;

  /**
   * 检索由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的切片，从指定的 <code>index</code> 开始，包含最多 <code>count</code> 个连续的 SQL 数组元素。
   * 此方法使用与连接关联的类型映射进行类型映射的自定义。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于将映射到基本数据类型的基类型具体化时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @param index 要检索的第一个元素的数组索引；第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @return 一个包含最多 <code>count</code> 个连续元素的数组，从元素 <code>index</code> 开始
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(long index, int count) throws SQLException;

  /**
   * 检索由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的切片，从指定的 <code>index</code> 开始，包含最多 <code>count</code> 个连续的 SQL 数组元素。
   * <P>
   * 此方法使用指定的 <code>map</code> 进行类型映射的自定义，除非数组的基本类型与 <code>map</code> 中的用户定义类型不匹配，此时它使用标准映射。
   * 此版本的 <code>getArray</code> 方法使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于将映射到基本数据类型的基类型具体化时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @param index 要检索的第一个元素的数组索引；第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @param map 包含 SQL 类型名称和 Java 编程语言类映射的 <code>java.util.Map</code> 对象
   * @return 一个包含最多 <code>count</code> 个连续元素的数组，从元素 <code>index</code> 开始
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(long index, int count, java.util.Map<String,Class<?>> map)
    throws SQLException;

  /**
   * 检索一个包含由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的元素的结果集。
   * 如果适当，数组的元素将使用连接的类型映射进行映射；否则，使用标准映射。
   * <p>
   * 结果集包含每个数组元素的一行，每行有两列。第二列存储元素值；第一列存储该元素在数组中的索引（第一个数组元素的索引为 1）。
   * 行按索引的升序排列。
   *
   * @return 一个包含由这个 <code>Array</code> 对象指定的数组的每个元素的一行的 {@link ResultSet} 对象，行按索引的升序排列。
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet () throws SQLException;

  /**
   * 检索一个包含由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的元素的结果集。
   * 此方法使用指定的 <code>map</code> 进行类型映射的自定义，除非数组的基本类型与 <code>map</code> 中的用户定义类型不匹配，此时它使用标准映射。
   * 此版本的 <code>getResultSet</code> 方法使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * 结果集包含每个数组元素的一行，每行有两列。第二列存储元素值；第一列存储该元素在数组中的索引（第一个数组元素的索引为 1）。
   * 行按索引的升序排列。
   *
   * @param map 包含 SQL 用户定义类型到 Java 编程语言类映射的 <code>Map</code> 对象
   * @return 一个包含由这个 <code>Array</code> 对象指定的数组的每个元素的一行的 <code>ResultSet</code> 对象，行按索引的升序排列。
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet (java.util.Map<String,Class<?>> map) throws SQLException;

  /**
   * 检索一个包含从索引 <code>index</code> 开始的子数组元素的结果集，包含最多 <code>count</code> 个连续的元素。
   * 此方法使用连接的类型映射来映射数组的元素，如果映射包含基本类型的条目。否则，使用标准映射。
   * <P>
   * 结果集包含由这个对象指定的 SQL 数组的每个元素的一行，第一行包含索引 <code>index</code> 处的元素。结果集包含最多 <code>count</code> 行，按索引的升序排列。每行有两列：第二列存储元素值；第一列存储该元素在数组中的索引。
   *
   * @param index 要检索的第一个元素的数组索引；第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @return 一个包含最多 <code>count</code> 个连续元素的结果集，从索引 <code>index</code> 开始
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet(long index, int count) throws SQLException;

  /**
   * 检索一个包含从索引 <code>index</code> 开始的子数组元素的结果集，包含最多 <code>count</code> 个连续的元素。
   * 此方法使用指定的 <code>map</code> 进行类型映射的自定义，除非数组的基本类型与 <code>map</code> 中的用户定义类型不匹配，此时它使用标准映射。
   * 此版本的 <code>getResultSet</code> 方法使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <P>
   * 结果集包含由这个对象指定的 SQL 数组的每个元素的一行，第一行包含索引 <code>index</code> 处的元素。结果集包含最多 <code>count</code> 行，按索引的升序排列。每行有两列：第二列存储元素值；第一列存储该元素在数组中的索引。
   *
   * @param index 要检索的第一个元素的数组索引；第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @param map 包含 SQL 类型名称到 Java 编程语言类映射的 <code>Map</code> 对象
   * @return 一个包含最多 <code>count</code> 个连续元素的结果集，从索引 <code>index</code> 开始
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet (long index, int count,
                          java.util.Map<String,Class<?>> map)
    throws SQLException;
    /**
     * 释放 <code>Array</code> 对象并释放它持有的资源。调用 <code>free</code> 方法后，对象无效。
     *<p>
     * 调用 <code>free</code> 后，任何尝试调用除 <code>free</code> 以外的方法都将导致抛出 <code>SQLException</code>。
     * 如果多次调用 <code>free</code>，后续的 <code>free</code> 调用被视为无操作。
     *<p>
     *
     * @throws SQLException 如果在释放 <code>Array</code> 的资源时发生错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void free() throws SQLException;

}
