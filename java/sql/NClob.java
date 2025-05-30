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

import java.sql.Clob;

/**
 * 在 Java&trade; 编程语言中
 * 对 SQL <code>NCLOB</code> 类型的映射。
 * SQL <code>NCLOB</code> 是一个内置类型，
 * 用于存储使用国家字符集的字符大对象
 * 作为数据库表中一行的列值。
 * <P><code>NClob</code> 接口扩展了 <code>Clob</code> 接口，
 * 提供了获取 SQL <code>NCLOB</code> 值的长度、
 * 在客户端实例化 <code>NCLOB</code> 值以及
 * 在 <code>NCLOB</code> 值内搜索子字符串或 <code>NCLOB</code> 对象的方法。
 * <code>NClob</code> 对象，就像 <code>Clob</code> 对象一样，在创建它的事务期间有效。
 * <code>ResultSet</code>、<code>CallableStatement</code> 和 <code>PreparedStatement</code> 接口中的方法，
 * 如 <code>getNClob</code> 和 <code>setNClob</code> 允许程序员
 * 访问 SQL <code>NCLOB</code> 值。此外，此接口还提供了更新 <code>NCLOB</code> 值的方法。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则必须完全实现
 * <code>NClob</code> 接口上的所有方法。
 *
 * @since 1.6
 */

public interface NClob extends Clob { }
