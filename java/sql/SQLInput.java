
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
 * 一个包含表示 SQL 结构类型或 SQL 区分类型实例的值流的输入流。
 * 此接口仅用于自定义映射，由驱动程序在后台使用，程序员从不直接调用
 * <code>SQLInput</code> 方法。读取方法
 * (<code>readLong</code>, <code>readBytes</code> 等)
 * 为 <code>SQLData</code> 接口的实现提供了一种从 <code>SQLInput</code> 对象中读取值的方法。
 * 如 <code>SQLData</code> 中所述，对读取方法的调用必须按照类型在 SQL 定义中出现的顺序进行。
 * <code>wasNull</code> 方法用于确定最后读取的值是否为 SQL <code>NULL</code>。
 * <P>当使用实现 <code>SQLData</code> 接口的对象调用 <code>getObject</code> 方法时，
 * JDBC 驱动程序调用 <code>SQLData.getSQLType</code> 方法以确定正在自定义映射的用户定义类型 (UDT) 的 SQL 类型。
 * 驱动程序创建一个 <code>SQLInput</code> 实例，并用 UDT 的属性填充它。
 * 然后驱动程序将输入流传递给 <code>SQLData.readSQL</code> 方法，该方法在其实现中调用
 * <code>SQLInput</code> 读取方法以从输入流中读取属性。
 * @since 1.2
 */

public interface SQLInput {


    //================================================================
    // 从 SQL 数据流中读取属性的方法。
    // 这些方法对应于 java.sql.ResultSet 的列访问方法。
    //================================================================

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>String</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    String readString() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>boolean</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean readBoolean() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>byte</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    byte readByte() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>short</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    short readShort() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>int</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    int readInt() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>long</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    long readLong() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>float</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    float readFloat() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>double</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    double readDouble() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的 <code>java.math.BigDecimal</code> 对象返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.math.BigDecimal readBigDecimal() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Java 编程语言中的字节数组返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    byte[] readBytes() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 <code>java.sql.Date</code> 对象返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Date readDate() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 <code>java.sql.Time</code> 对象返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Time readTime() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 <code>java.sql.Timestamp</code> 对象返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Timestamp readTimestamp() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 Unicode 字符流返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.Reader readCharacterStream() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为 ASCII 字符流返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.InputStream readAsciiStream() throws SQLException;

    /**
     * 读取流中的下一个属性并将其作为未解释的字节流返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.InputStream readBinaryStream() throws SQLException;

    //================================================================
    // 从流中读取 SQL 用户定义类型项的方法。
    //================================================================

    /**
     * 读取流头部的数据并将其作为 Java 编程语言中的 <code>Object</code> 返回。实际返回的对象类型由默认类型映射决定，
     * 以及此流类型映射中的任何自定义。
     *
     * <P>类型映射由 JDBC 驱动程序在将流传递给应用程序之前注册到流中。
     *
     * <P>当流头部的数据为 SQL <code>NULL</code> 时，此方法返回 <code>null</code>。如果数据为 SQL 结构化或区分类型，
     * 它会确定流头部数据的 SQL 类型。如果流的类型映射中有该 SQL 类型的条目，驱动程序会构造适当类的对象并调用该对象的
     * <code>SQLData.readSQL</code> 方法，该方法从流中读取更多数据，使用该方法描述的协议。
     *
     * @return 流头部的数据作为 Java 编程语言中的 <code>Object</code>；如果数据为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Object readObject() throws SQLException;

    /**
     * 从流中读取 SQL <code>REF</code> 值并将其作为 Java 编程语言中的 <code>Ref</code> 对象返回。
     *
     * @return 一个表示 SQL <code>REF</code> 值的 <code>Ref</code> 对象；如果读取的值为
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Ref readRef() throws SQLException;

    /**
     * 从流中读取 SQL <code>BLOB</code> 值并将其作为 Java 编程语言中的 <code>Blob</code> 对象返回。
     *
     * @return 一个表示 SQL <code>BLOB</code> 值数据的 <code>Blob</code> 对象；如果读取的值为
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Blob readBlob() throws SQLException;

    /**
     * 从流中读取 SQL <code>CLOB</code> 值并将其作为 Java 编程语言中的 <code>Clob</code> 对象返回。
     *
     * @return 一个表示 SQL <code>CLOB</code> 值数据的 <code>Clob</code> 对象；如果读取的值为
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Clob readClob() throws SQLException;

    /**
     * 从流中读取 SQL <code>ARRAY</code> 值并将其作为 Java 编程语言中的 <code>Array</code> 对象返回。
     *
     * @return 一个表示 SQL <code>ARRAY</code> 值数据的 <code>Array</code> 对象；如果读取的值为
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Array readArray() throws SQLException;

    /**
     * 检索最后读取的值是否为 SQL <code>NULL</code>。
     *
     * @return 如果最近读取的 SQL 值为 SQL <code>NULL</code>，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     *
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean wasNull() throws SQLException;

    //---------------------------- JDBC 3.0 -------------------------

    /**
     * 从流中读取 SQL <code>DATALINK</code> 值并将其作为 Java 编程语言中的 <code>java.net.URL</code> 对象返回。
     *
     * @return 一个 <code>java.net.URL</code> 对象。
     * @exception SQLException 如果发生数据库访问错误，或 URL 格式不正确
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    java.net.URL readURL() throws SQLException;


                 //---------------------------- JDBC 4.0 -------------------------

    /**
     * 从流中读取一个 SQL <code>NCLOB</code> 值，并将其作为 Java 编程语言中的
     * <code>NClob</code> 对象返回。
     *
     * @return 一个 <code>NClob</code> 对象，表示流头部的 SQL <code>NCLOB</code> 值；
     * 如果读取的值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    NClob readNClob() throws SQLException;

    /**
     * 从流中读取下一个属性，并将其作为 Java 编程语言中的 <code>String</code> 返回。
     * 用于访问 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    String readNString() throws SQLException;

    /**
     * 从流中读取一个 SQL <code>XML</code> 值，并将其作为 Java 编程语言中的
     * <code>SQLXML</code> 对象返回。
     *
     * @return 一个 <code>SQLXML</code> 对象，表示流头部的 SQL <code>XML</code> 值；
     * 如果读取的值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    SQLXML readSQLXML() throws SQLException;

    /**
     * 从流中读取一个 SQL <code>ROWID</code> 值，并将其作为 Java 编程语言中的
     * <code>RowId</code> 对象返回。
     *
     * @return 一个 <code>RowId</code> 对象，表示流头部的 SQL <code>ROWID</code> 值；
     * 如果读取的值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId readRowId() throws SQLException;

    //--------------------------JDBC 4.2 -----------------------------

    /**
     * 从流中读取下一个属性，并将其作为 Java 编程语言中的 {@code Object} 返回。
     * 实际返回的对象类型由指定的 Java 数据类型和此流的类型映射中的任何自定义决定。
     *
     * <P>类型映射由 JDBC 驱动程序在将流传递给应用程序之前注册到流中。
     *
     * <P>当流头部的属性是 SQL {@code NULL} 时，此方法返回 {@code null}。如果属性是 SQL
     * 结构化或区分类型，它会确定流头部的属性的 SQL 类型。如果流的类型映射中有该 SQL 类型的条目，
     * 驱动程序将构造适当类的对象，并调用该对象的 {@code SQLData.readSQL} 方法，该方法从
     * 流中读取额外的数据，使用该方法描述的协议。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param <T> 由此类对象建模的类的类型
     * @param type 表示要将属性转换为的 Java 数据类型的类。
     * @return 流头部的属性作为 Java 编程语言中的 {@code Object}；
     * 如果属性是 SQL {@code NULL}，则返回 {@code null}
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.8
     */
    default <T> T readObject(Class<T> type) throws SQLException {
       throw new SQLFeatureNotSupportedException();
    }
}
