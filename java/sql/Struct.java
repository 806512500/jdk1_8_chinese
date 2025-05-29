/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <p>Java 编程语言中 SQL 结构化类型的标准化映射。一个 <code>Struct</code> 对象包含其表示的 SQL 结构化类型的每个属性的值。
 * 默认情况下，只要应用程序有对该 <code>Struct</code> 实例的引用，该实例就是有效的。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则 <code>Struct</code> 接口上的所有方法都必须完全实现。
 * @since 1.2
 */

public interface Struct {

  /**
   * 检索此 <code>Struct</code> 对象表示的 SQL 结构化类型的 SQL 类型名称。
   *
   * @return 此 <code>Struct</code> 对象作为通用表示的 SQL 结构化类型的完全限定类型名称
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  String getSQLTypeName() throws SQLException;

  /**
   * 生成此 <code>Struct</code> 对象表示的 SQL 结构化类型的属性的有序值。
   * 在处理各个属性时，此方法使用与连接关联的类型映射进行类型映射的自定义。
   * 如果连接的类型映射中没有与属性表示的结构化类型匹配的条目，
   * 驱动程序将使用标准映射。
   * <p>
   * 从概念上讲，此方法对结构化类型的每个属性调用 <code>getObject</code> 方法，并返回包含结果的 Java 数组。
   *
   * @return 包含有序属性值的数组
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object[] getAttributes() throws SQLException;

  /**
   * 生成此 <code>Struct</code> 对象表示的 SQL 结构化类型的属性的有序值。
   * 在处理各个属性时，此方法使用给定的类型映射进行类型映射的自定义。
   * 如果给定的类型映射中没有与属性表示的结构化类型匹配的条目，
   * 驱动程序将使用标准映射。此方法从不使用与连接关联的类型映射。
   * <p>
   * 从概念上讲，此方法对结构化类型的每个属性调用 <code>getObject</code> 方法，并返回包含结果的 Java 数组。
   *
   * @param map SQL 类型名称到 Java 类的映射
   * @return 包含有序属性值的数组
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object[] getAttributes(java.util.Map<String,Class<?>> map)
      throws SQLException;
}
