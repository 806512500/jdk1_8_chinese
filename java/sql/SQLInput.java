
/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 包含表示 SQL 结构类型或 SQL 区分类型实例的值流的输入流。
 * 该接口仅用于自定义映射，由驱动程序在后台使用，程序员永远不会直接调用
 * <code>SQLInput</code> 方法。读取方法
 * (<code>readLong</code>, <code>readBytes</code> 等)
 * 为 <code>SQLData</code> 接口的实现提供了一种从 <code>SQLInput</code> 对象中读取值的方法。
 * 如 <code>SQLData</code> 中所述，对读取方法的调用必须按照类型在 SQL 定义中出现的顺序进行。
 * <code>wasNull</code> 方法用于确定最后读取的值是否为 SQL <code>NULL</code>。
 * <P>当使用实现 <code>SQLData</code> 接口的类的对象调用 <code>getObject</code> 方法时，
 * JDBC 驱动程序调用 <code>SQLData.getSQLType</code> 方法以确定正在自定义映射的用户定义类型 (UDT) 的 SQL 类型。
 * 驱动程序创建一个 <code>SQLInput</code> 实例，并用 UDT 的属性填充它。
 * 然后驱动程序将输入流传递给 <code>SQLData.readSQL</code> 方法，该方法在其实现中调用
 * <code>SQLInput</code> 读取方法，以从输入流中读取属性。
 * @since 1.2
 */

public interface SQLInput {


    //================================================================
    // 从 SQL 数据流中读取属性的方法。
    // 这些方法对应于 java.sql.ResultSet 的列访问方法。
    //================================================================

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>String</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    String readString() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>boolean</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean readBoolean() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>byte</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    byte readByte() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>short</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    short readShort() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>int</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    int readInt() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>long</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    long readLong() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>float</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    float readFloat() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>double</code> 返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    double readDouble() throws SQLException;

    /**
     * 读取流中的下一个属性，并将其作为 Java 编程语言中的 <code>java.math.BigDecimal</code> 对象返回。
     *
     * @return 属性；如果值为 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.math.BigDecimal readBigDecimal() throws SQLException;

                /**
     * 从流中读取下一个属性并将其作为 Java 编程语言中的字节数组返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    byte[] readBytes() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为 <code>java.sql.Date</code> 对象返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Date readDate() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为 <code>java.sql.Time</code> 对象返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Time readTime() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为 <code>java.sql.Timestamp</code> 对象返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.sql.Timestamp readTimestamp() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为 Unicode 字符流返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.Reader readCharacterStream() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为 ASCII 字符流返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.InputStream readAsciiStream() throws SQLException;

    /**
     * 从流中读取下一个属性并将其作为未解释的字节流返回。
     *
     * @return 属性；如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    java.io.InputStream readBinaryStream() throws SQLException;

    //================================================================
    // 从流中读取 SQL 用户定义类型项的方法。
    //================================================================

    /**
     * 从流的头部读取数据项并将其作为 Java 编程语言中的 <code>Object</code> 返回。实际返回的对象类型由默认类型映射决定，
     * 以及此流类型映射中的任何自定义。
     *
     * <P>类型映射由 JDBC 驱动程序在将流传递给应用程序之前注册。
     *
     * <P>当流头部的数据项是 SQL <code>NULL</code> 时，该方法返回 <code>null</code>。如果数据项是 SQL 结构化或区别类型，
     * 它会确定流头部数据项的 SQL 类型。如果流的类型映射中有该 SQL 类型的条目，驱动程序将构造适当类的对象并调用该对象的
     * <code>SQLData.readSQL</code> 方法，该方法使用该方法描述的协议从流中读取更多数据。
     *
     * @return 流头部的数据项作为 Java 编程语言中的 <code>Object</code>；如果数据项是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Object readObject() throws SQLException;

    /**
     * 从流中读取 SQL <code>REF</code> 值并将其作为 Java 编程语言中的 <code>Ref</code> 对象返回。
     *
     * @return 代表流头部 SQL <code>REF</code> 值的 <code>Ref</code> 对象；如果读取的值是
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Ref readRef() throws SQLException;

    /**
     * 从流中读取 SQL <code>BLOB</code> 值并将其作为 Java 编程语言中的 <code>Blob</code> 对象返回。
     *
     * @return 代表流头部 SQL <code>BLOB</code> 值数据的 <code>Blob</code> 对象；如果读取的值是
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Blob readBlob() throws SQLException;

    /**
     * 从流中读取 SQL <code>CLOB</code> 值并将其作为 Java 编程语言中的 <code>Clob</code> 对象返回。
     *
     * @return 代表流头部 SQL <code>CLOB</code> 值数据的 <code>Clob</code> 对象；如果读取的值是
     * SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Clob readClob() throws SQLException;

                /**
     * 从流中读取一个 SQL <code>ARRAY</code> 值，并将其作为 Java 编程语言中的
     * <code>Array</code> 对象返回。
     *
     * @return 一个表示 SQL <code>ARRAY</code> 值的 <code>Array</code> 对象，位于流的头部；
     * 如果读取的值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Array readArray() throws SQLException;

    /**
     * 检索最后读取的值是否为 SQL <code>NULL</code>。
     *
     * @return 如果最近读取的 SQL 值是 SQL <code>NULL</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     *
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean wasNull() throws SQLException;

    //---------------------------- JDBC 3.0 -------------------------

    /**
     * 从流中读取一个 SQL <code>DATALINK</code> 值，并将其作为 Java 编程语言中的
     * <code>java.net.URL</code> 对象返回。
     *
     * @return 一个 <code>java.net.URL</code> 对象。
     * @exception SQLException 如果发生数据库访问错误，或 URL 格式错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    java.net.URL readURL() throws SQLException;

     //---------------------------- JDBC 4.0 -------------------------

    /**
     * 从流中读取一个 SQL <code>NCLOB</code> 值，并将其作为 Java 编程语言中的
     * <code>NClob</code> 对象返回。
     *
     * @return 一个表示 SQL <code>NCLOB</code> 值的 <code>NClob</code> 对象，位于流的头部；
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
     * @return 一个表示 SQL <code>XML</code> 值的 <code>SQLXML</code> 对象，位于流的头部；
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
     * @return 一个表示 SQL <code>ROWID</code> 值的 <code>RowId</code> 对象，位于流的头部；
     * 如果读取的值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId readRowId() throws SQLException;

    //--------------------------JDBC 4.2 -----------------------------

    /**
     * 从流中读取下一个属性，并将其作为 Java 编程语言中的
     * {@code Object} 返回。实际返回的对象类型由指定的
     * Java 数据类型和此流的类型映射中的任何自定义决定。
     *
     * <P>类型映射由 JDBC 驱动程序在将流传递给应用程序之前注册到流中。
     *
     * <P>当流头部的属性是 SQL {@code NULL} 时，该方法返回 {@code null}。如果属性是 SQL
     * 结构化或区分类型，它会确定流头部的属性的 SQL 类型。
     * 如果流的类型映射中有该 SQL 类型的条目，驱动程序将构造适当类的对象并调用该对象的
     * {@code SQLData.readSQL} 方法，该方法使用该方法描述的协议从流中读取更多数据。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param <T> 由此类对象建模的类的类型
     * @param type 表示要将属性转换为的 Java 数据类型的类。
     * @return 作为 Java 编程语言中的 {@code Object} 的流头部的属性；
     * 如果属性是 SQL {@code NULL}，则返回 {@code null}
     * @exception SQLException 如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.8
     */
    default <T> T readObject(Class<T> type) throws SQLException {
       throw new SQLFeatureNotSupportedException();
    }
}
