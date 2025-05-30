
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
 * 用于将用户定义类型的属性写回数据库的输出流。此接口仅用于自定义映射，由驱动程序使用，其方法从未由程序员直接调用。
 * <p>当一个实现了 <code>SQLData</code> 接口的类的对象作为 SQL 语句的参数传递时，JDBC 驱动程序调用 <code>SQLData.getSQLType</code> 方法
 * 以确定传递给数据库的 SQL 数据类型。驱动程序然后创建一个 <code>SQLOutput</code> 实例，并将其传递给 <code>SQLData.writeSQL</code> 方法。
 * <code>writeSQL</code> 方法反过来调用适当的 <code>SQLOutput</code> 写入方法（如 <code>writeBoolean</code>、<code>writeCharacterStream</code> 等）
 * 将 <code>SQLData</code> 对象的数据写入 <code>SQLOutput</code> 输出流，作为 SQL 用户定义类型的表示。
 * @since 1.2
 */

public interface SQLOutput {

  //================================================================
  // 将属性写入 SQL 数据流的方法。这些方法对应于 java.sql.ResultSet 的列访问方法。
  //================================================================

  /**
   * 将下一个属性作为 Java 编程语言中的 <code>String</code> 写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeString(String x) throws SQLException;

  /**
   * 将下一个属性作为 Java 布尔值写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeBoolean(boolean x) throws SQLException;

  /**
   * 将下一个属性作为 Java 字节写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeByte(byte x) throws SQLException;

  /**
   * 将下一个属性作为 Java 短整型写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeShort(short x) throws SQLException;

  /**
   * 将下一个属性作为 Java 整型写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeInt(int x) throws SQLException;

  /**
   * 将下一个属性作为 Java 长整型写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeLong(long x) throws SQLException;

  /**
   * 将下一个属性作为 Java 浮点型写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeFloat(float x) throws SQLException;

  /**
   * 将下一个属性作为 Java 双精度浮点型写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeDouble(double x) throws SQLException;

  /**
   * 将下一个属性作为 java.math.BigDecimal 对象写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeBigDecimal(java.math.BigDecimal x) throws SQLException;

  /**
   * 将下一个属性作为字节数组写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeBytes(byte[] x) throws SQLException;

  /**
   * 将下一个属性作为 java.sql.Date 对象写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeDate(java.sql.Date x) throws SQLException;

  /**
   * 将下一个属性作为 java.sql.Time 对象写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeTime(java.sql.Time x) throws SQLException;

  /**
   * 将下一个属性作为 java.sql.Timestamp 对象写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeTimestamp(java.sql.Timestamp x) throws SQLException;

  /**
   * 将下一个属性作为 Unicode 字符流写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeCharacterStream(java.io.Reader x) throws SQLException;

  /**
   * 将下一个属性作为 ASCII 字符流写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeAsciiStream(java.io.InputStream x) throws SQLException;

  /**
   * 将下一个属性作为未解释的字节流写入流。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeBinaryStream(java.io.InputStream x) throws SQLException;

  //================================================================
  // 将 SQL 用户定义类型项写入流的方法。这些方法将对象作为 SQL 结构类型、区分类型、构造类型和定位类型值传递给数据库。
  // 它们分解 Java 对象并使用上述方法将叶数据项写入流。
  //================================================================

  /**
   * 将给定的 <code>SQLData</code> 对象包含的数据写入流。
   * 当 <code>SQLData</code> 对象为 <code>null</code> 时，此方法将 SQL <code>NULL</code> 写入流。
   * 否则，它调用给定对象的 <code>SQLData.writeSQL</code> 方法，该方法将对象的属性写入流。
   * <code>SQLData.writeSQL</code> 方法的实现调用适当的 <code>SQLOutput</code> 写入方法，按顺序写入对象的每个属性。
   * 属性必须从 <code>SQLInput</code> 输入流中读取，并按 SQL 用户定义类型定义中列出的顺序写入 <code>SQLOutput</code> 输出流。
   *
   * @param x 代表 SQL 结构类型或区分类型数据的对象
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeObject(SQLData x) throws SQLException;

  /**
   * 将 SQL <code>REF</code> 值写入流。
   *
   * @param x 代表 SQL <code>REF</code> 值数据的 <code>Ref</code> 对象
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeRef(Ref x) throws SQLException;

  /**
   * 将 SQL <code>BLOB</code> 值写入流。
   *
   * @param x 代表 SQL <code>BLOB</code> 值数据的 <code>Blob</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeBlob(Blob x) throws SQLException;

  /**
   * 将 SQL <code>CLOB</code> 值写入流。
   *
   * @param x 代表 SQL <code>CLOB</code> 值数据的 <code>Clob</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeClob(Clob x) throws SQLException;

  /**
   * 将 SQL 结构类型值写入流。
   *
   * @param x 代表 SQL 结构类型数据的 <code>Struct</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeStruct(Struct x) throws SQLException;

  /**
   * 将 SQL <code>ARRAY</code> 值写入流。
   *
   * @param x 代表 SQL <code>ARRAY</code> 类型数据的 <code>Array</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  void writeArray(Array x) throws SQLException;

     //--------------------------- JDBC 3.0 ------------------------

     /**
      * 将 SQL <code>DATALINK</code> 值写入流。
      *
      * @param x 代表 SQL DATALINK 类型数据的 <code>java.net.URL</code> 对象
      *
      * @exception SQLException 如果发生数据库访问错误
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
      * @since 1.4
      */
     void writeURL(java.net.URL x) throws SQLException;

     //--------------------------- JDBC 4.0 ------------------------

  /**
   * 将下一个属性作为 Java 编程语言中的 <code>String</code> 写入流。驱动程序在将其发送到流时将其转换为 SQL <code>NCHAR</code> 或
   * <code>NVARCHAR</code> 或 <code>LONGNVARCHAR</code> 值（取决于参数相对于驱动程序对 <code>NVARCHAR</code> 值的限制的大小）。
   *
   * @param x 要传递给数据库的值
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.6
   */
  void writeNString(String x) throws SQLException;

  /**
   * 将 SQL <code>NCLOB</code> 值写入流。
   *
   * @param x 代表 SQL <code>NCLOB</code> 值数据的 <code>NClob</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.6
   */
  void writeNClob(NClob x) throws SQLException;

  /**
   * 将 SQL <code>ROWID</code> 值写入流。
   *
   * @param x 代表 SQL <code>ROWID</code> 值数据的 <code>RowId</code> 对象
   *
   * @exception SQLException 如果发生数据库访问错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.6
   */
  void writeRowId(RowId x) throws SQLException;


  /**
   * 将 SQL <code>XML</code> 值写入流中。
   *
   * @param x 一个表示 SQL <code>XML</code> 值数据的 <code>SQLXML</code> 对象
   *
   * @throws SQLException 如果发生数据库访问错误，<code>java.xml.transform.Result</code>、
   * <code>Writer</code> 或 <code>OutputStream</code> 尚未为 <code>SQLXML</code> 对象关闭，或
   * 处理 XML 值时发生错误。异常的 <code>getCause</code> 方法
   * 可能提供更详细的异常，例如，如果流中不包含有效的 XML。
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.6
   */
  void writeSQLXML(SQLXML x) throws SQLException;

  //--------------------------JDBC 4.2 -----------------------------

  /**
   * 将给定对象中包含的数据写入流中。对象将在发送到流之前转换为指定的目标 SQL 类型。
   *<p>
   * 当 {@code object} 为 {@code null} 时，此
   * 方法将 SQL {@code NULL} 写入流中。
   * <p>
   * 如果对象具有自定义映射（是实现 {@code SQLData} 接口的类的实例），
   * JDBC 驱动程序应调用方法 {@code SQLData.writeSQL} 将其写入 SQL 数据流。
   * 另一方面，如果对象是实现
   * {@code Ref}、{@code Blob}、{@code Clob}、{@code NClob}、
   * {@code Struct}、{@code java.net.URL} 或
   * {@code Array} 接口的类的实例，驱动程序应将其作为相应 SQL 类型的值传递给数据库。
   *<P>
   * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
   *
   * @param x 包含输入参数值的对象
   * @param targetSqlType 要发送到数据库的 SQL 类型。
   * @exception SQLException 如果发生数据库访问错误，或
   *            如果由 x 指定的 Java 对象是 InputStream 或 Reader 对象，并且 scale 参数的值小于零
   * @exception SQLFeatureNotSupportedException 如果
   * JDBC 驱动程序不支持此数据类型
   * @see JDBCType
   * @see SQLType
   * @since 1.8
   */
  default void writeObject(Object x, SQLType targetSqlType) throws SQLException {
        throw new SQLFeatureNotSupportedException();
  }

}
