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

import java.sql.Clob;

/**
 * Java&trade; 编程语言中 SQL <code>NCLOB</code> 类型的映射。
 * SQL <code>NCLOB</code> 是一种内置类型，用于在数据库表的行中以国家字符集存储字符大对象。
 * <P><code>NClob</code> 接口扩展了 <code>Clob</code> 接口，后者提供了获取 SQL <code>NCLOB</code> 值的长度、
 * 在客户端实现 <code>NCLOB</code> 值以及在 <code>NCLOB</code> 值中搜索子字符串或 <code>NCLOB</code> 对象的方法。
 * <code>NClob</code> 对象与 <code>Clob</code> 对象一样，在创建它的事务期间有效。
 * <code>ResultSet</code>、<code>CallableStatement</code> 和 <code>PreparedStatement</code> 接口中的方法，
 * 如 <code>getNClob</code> 和 <code>setNClob</code>，允许程序员访问 SQL <code>NCLOB</code> 值。
 * 此外，此接口还提供了更新 <code>NCLOB</code> 值的方法。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则必须完全实现 <code>NClob</code> 接口上的所有方法。
 *
 * @since 1.6
 */

public interface NClob extends Clob { }
