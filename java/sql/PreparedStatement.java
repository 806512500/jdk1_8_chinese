
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.math.BigDecimal;
import java.util.Calendar;
import java.io.Reader;
import java.io.InputStream;

/**
 * 一个表示预编译SQL语句的对象。
 * <P>SQL语句被预编译并存储在
 * <code>PreparedStatement</code> 对象中。然后可以使用此对象
 * 高效地多次执行此语句。
 *
 * <P><B>注意：</B>用于设置IN参数值的setter方法（<code>setShort</code>，<code>setString</code>，
 * 等等）必须指定与输入参数定义的SQL类型兼容的类型。例如，如果IN参数的SQL类型为
 * <code>INTEGER</code>，则应使用<code>setInt</code>方法。
 *
 * <p>如果需要任意参数类型转换，应使用带有目标SQL类型的<code>setObject</code>方法。
 * <P>
 * 在以下设置参数的示例中，<code>con</code>表示
 * 一个活动的连接：
 * <PRE>
 *   PreparedStatement pstmt = con.prepareStatement("UPDATE EMPLOYEES
 *                                     SET SALARY = ? WHERE ID = ?");
 *   pstmt.setBigDecimal(1, 153833.00)
 *   pstmt.setInt(2, 110592)
 * </PRE>
 *
 * @see Connection#prepareStatement
 * @see ResultSet
 */

public interface PreparedStatement extends Statement {

    /**
     * 执行此<code>PreparedStatement</code>对象中的SQL查询
     * 并返回由查询生成的<code>ResultSet</code>对象。
     *
     * @return 一个包含查询生成的数据的<code>ResultSet</code>对象；从不为<code>null</code>
     * @exception SQLException 如果发生数据库访问错误；
     * 此方法在已关闭的<code>PreparedStatement</code>上调用或SQL
     * 语句不返回<code>ResultSet</code>对象
     * @throws SQLTimeoutException 当驱动程序确定由{@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消
     * 当前正在运行的{@code Statement}
     */
    ResultSet executeQuery() throws SQLException;

    /**
     * 执行此<code>PreparedStatement</code>对象中的SQL语句，
     * 该语句必须是SQL数据操作语言（DML）语句，如<code>INSERT</code>，<code>UPDATE</code>或
     * <code>DELETE</code>；或返回空的SQL语句，
     * 如DDL语句。
     *
     * @return 要么（1）SQL数据操作语言（DML）语句的行数
     *         要么（2）返回空的SQL语句的0
     * @exception SQLException 如果发生数据库访问错误；
     * 此方法在已关闭的<code>PreparedStatement</code>上调用
     * 或SQL语句返回<code>ResultSet</code>对象
     * @throws SQLTimeoutException 当驱动程序确定由{@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消
     * 当前正在运行的{@code Statement}
     */
    int executeUpdate() throws SQLException;

    /**
     * 将指定的参数设置为SQL <code>NULL</code>。
     *
     * <P><B>注意：</B>必须指定参数的SQL类型。
     *
     * @param parameterIndex 第一个参数是1，第二个是2，...
     * @param sqlType 在<code>java.sql.Types</code>中定义的SQL类型代码
     * @exception SQLException 如果parameterIndex不对应于SQL语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的<code>PreparedStatement</code>上调用
     * @exception SQLFeatureNotSupportedException 如果<code>sqlType</code>是
     * <code>ARRAY</code>，<code>BLOB</code>，<code>CLOB</code>，
     * <code>DATALINK</code>，<code>JAVA_OBJECT</code>，<code>NCHAR</code>，
     * <code>NCLOB</code>，<code>NVARCHAR</code>，<code>LONGNVARCHAR</code>，
     * <code>REF</code>，<code>ROWID</code>，<code>SQLXML</code>
     * 或<code>STRUCT</code>数据类型，并且JDBC驱动程序不支持
     * 此数据类型
     */
    void setNull(int parameterIndex, int sqlType) throws SQLException;

    /**
     * 将指定的参数设置为给定的Java <code>boolean</code>值。
     * 驱动程序在将其发送到数据库时将其转换为SQL <code>BIT</code>或<code>BOOLEAN</code>值。
     *
     * @param parameterIndex 第一个参数是1，第二个是2，...
     * @param x 参数值
     * @exception SQLException 如果parameterIndex不对应于SQL语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的<code>PreparedStatement</code>上调用
     */
    void setBoolean(int parameterIndex, boolean x) throws SQLException;

    /**
     * 将指定的参数设置为给定的Java <code>byte</code>值。
     * 驱动程序在将其发送到数据库时将其转换为SQL <code>TINYINT</code>值。
     *
     * @param parameterIndex 第一个参数是1，第二个是2，...
     * @param x 参数值
     * @exception SQLException 如果parameterIndex不对应于SQL语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的<code>PreparedStatement</code>上调用
     */
    void setByte(int parameterIndex, byte x) throws SQLException;

    /**
     * 将指定的参数设置为给定的Java <code>short</code>值。
     * 驱动程序在将其发送到数据库时将其转换为SQL <code>SMALLINT</code>值。
     *
     * @param parameterIndex 第一个参数是1，第二个是2，...
     * @param x 参数值
     * @exception SQLException 如果parameterIndex不对应于SQL语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的<code>PreparedStatement</code>上调用
     */
    void setShort(int parameterIndex, short x) throws SQLException;

                /**
     * 将指定参数设置为给定的 Java <code>int</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>INTEGER</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setInt(int parameterIndex, int x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>long</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>BIGINT</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setLong(int parameterIndex, long x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>float</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>REAL</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setFloat(int parameterIndex, float x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>double</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>DOUBLE</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setDouble(int parameterIndex, double x) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.math.BigDecimal</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>NUMERIC</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>String</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>VARCHAR</code> 或 <code>LONGVARCHAR</code> 值
     * （取决于参数的大小与驱动程序对 <code>VARCHAR</code> 值的限制）
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setString(int parameterIndex, String x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java 字节数组。驱动程序在将其发送到数据库时将其转换为 SQL <code>VARBINARY</code> 或 <code>LONGVARBINARY</code>
     * （取决于参数的大小与驱动程序对 <code>VARBINARY</code> 值的限制）
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setBytes(int parameterIndex, byte x[]) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Date</code> 值，使用运行应用程序的虚拟机的默认时区。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>DATE</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setDate(int parameterIndex, java.sql.Date x)
            throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Time</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>TIME</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setTime(int parameterIndex, java.sql.Time x)
            throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Timestamp</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>TIMESTAMP</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setTimestamp(int parameterIndex, java.sql.Timestamp x)
            throws SQLException;

                /**
     * 将指定参数设置为给定的输入流，该输入流将具有指定的字节数。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 ASCII 值时，通过 <code>java.io.InputStream</code>
     * 发送可能更为实际。数据将根据需要从流中读取，直到文件结束。JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含 ASCII 参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setAsciiStream(int parameterIndex, java.io.InputStream x, int length)
            throws SQLException;

    /**
     * 将指定参数设置为给定的输入流，该输入流将具有指定的字节数。
     *
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 Unicode 值时，通过 <code>java.io.InputStream</code> 对象
     * 发送可能更为实际。数据将根据需要从流中读取，直到文件结束。JDBC 驱动程序将执行从 Unicode 到数据库字符格式的任何必要转换。
     *
     * Unicode 流的字节格式必须是 Java UTF-8，如 Java 虚拟机规范中定义的。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含 Unicode 参数值的 <code>java.io.InputStream</code> 对象
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @deprecated 使用 {@code setCharacterStream}
     */
    @Deprecated
    void setUnicodeStream(int parameterIndex, java.io.InputStream x,
                          int length) throws SQLException;

    /**
     * 将指定参数设置为给定的输入流，该输入流将具有指定的字节数。
     * 当向 <code>LONGVARBINARY</code> 参数输入非常大的二进制值时，通过 <code>java.io.InputStream</code> 对象
     * 发送可能更为实际。数据将根据需要从流中读取，直到文件结束。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含二进制参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void setBinaryStream(int parameterIndex, java.io.InputStream x,
                         int length) throws SQLException;

    /**
     * 立即清除当前的参数值。
     * <P>通常情况下，参数值在重复使用语句时保持有效。设置参数值会自动清除其先前的值。然而，在某些情况下，立即释放当前参数值使用的资源是有用的；
     * 这可以通过调用 <code>clearParameters</code> 方法来完成。
     *
     * @exception SQLException 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     */
    void clearParameters() throws SQLException;

    //----------------------------------------------------------------------
    // 高级功能：

   /**
    * 使用给定的对象设置指定参数的值。
    *
    * 此方法类似于 {@link #setObject(int parameterIndex,
    * Object x, int targetSqlType, int scaleOrLength)}，不同之处在于它假定比例为零。
    *
    * @param parameterIndex 第一个参数为 1，第二个为 2，...
    * @param x 包含输入参数值的对象
    * @param targetSqlType 要发送到数据库的 SQL 类型（如 java.sql.Types 中定义）
    * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
    * 在已关闭的 PreparedStatement 上调用此方法
    * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持指定的 targetSqlType
    * @see Types
    */
    void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException;

    /**
     * <p>使用给定的对象设置指定参数的值。
     *
     * <p>JDBC 规范指定了从 Java <code>Object</code> 类型到 SQL 类型的标准映射。给定的参数
     * 将被转换为相应的 SQL 类型，然后再发送到数据库。
     *
     * <p>注意，此方法可用于传递特定于数据库的抽象数据类型，方法是使用特定于驱动程序的 Java 类型。
     *
     * 如果对象是实现 <code>SQLData</code> 接口的类的实例，JDBC 驱动程序应调用 <code>SQLData.writeSQL</code>
     * 方法将其写入 SQL 数据流。如果对象是实现 <code>Ref</code>、<code>Blob</code>、<code>Clob</code>、
     * <code>NClob</code>、<code>Struct</code>、<code>java.net.URL</code>、<code>RowId</code>、<code>SQLXML</code>
     * 或 <code>Array</code> 接口的类的实例，驱动程序应将其作为相应 SQL 类型的值传递给数据库。
     * <P>
     *<b>注意：</b>并非所有数据库都允许发送未指定类型的 Null 到后端。为了最大限度的可移植性，应使用 <code>setNull</code>
     * 或 <code>setObject(int parameterIndex, Object x, int sqlType)</code> 方法，而不是 <code>setObject(int parameterIndex, Object x)</code>。
     *<p>
     * <b>注意：</b>如果存在歧义，例如对象是实现上述多个接口的类的实例，此方法将抛出异常。
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含输入参数值的对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误；
     *  在已关闭的 <code>PreparedStatement</code> 上调用此方法或给定对象的类型不明确
     */
    void setObject(int parameterIndex, Object x) throws SQLException;

                /**
     * 执行此 <code>PreparedStatement</code> 对象中的 SQL 语句，
     * 该语句可以是任何类型的 SQL 语句。
     * 一些预编译语句返回多个结果；<code>execute</code>
     * 方法处理这些复杂的语句，以及由 <code>executeQuery</code>
     * 和 <code>executeUpdate</code> 方法处理的较简单的语句形式。
     * <P>
     * <code>execute</code> 方法返回一个 <code>boolean</code> 值，
     * 以指示第一个结果的形式。必须调用 <code>getResultSet</code> 或 <code>getUpdateCount</code>
     * 方法来检索结果；必须调用 <code>getMoreResults</code> 方法
     * 来移动到任何后续结果。
     *
     * @return 如果第一个结果是 <code>ResultSet</code> 对象，则返回 <code>true</code>；
     *         如果第一个结果是更新计数或没有结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * 或向此方法提供参数
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并且至少尝试取消
     * 当前正在运行的 {@code Statement}
     * @see Statement#execute
     * @see Statement#getResultSet
     * @see Statement#getUpdateCount
     * @see Statement#getMoreResults

     */
    boolean execute() throws SQLException;

    //--------------------------JDBC 2.0-----------------------------

    /**
     * 将一组参数添加到此 <code>PreparedStatement</code>
     * 对象的命令批处理中。
     *
     * @exception SQLException 如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @see Statement#addBatch
     * @since 1.2
     */
    void addBatch() throws SQLException;

    /**
     * 将指定的参数设置为给定的 <code>Reader</code>
     * 对象，该对象具有给定数量的字符。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 UNICODE 值时，
     * 通过 <code>java.io.Reader</code> 对象发送它可能更为实用。
     * 数据将从流中按需读取，直到文件结束。JDBC 驱动程序将
     * 执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准
     * Java 流对象或实现标准接口的您自己的子类。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param reader 包含 Unicode 数据的 <code>java.io.Reader</code> 对象
     * @param length 流中的字符数
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @since 1.2
     */
    void setCharacterStream(int parameterIndex,
                          java.io.Reader reader,
                          int length) throws SQLException;

    /**
     * 将指定的参数设置为给定的
     *  <code>REF(&lt;structured-type&gt;)</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>REF</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 一个 SQL <code>REF</code> 值
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void setRef (int parameterIndex, Ref x) throws SQLException;

    /**
     * 将指定的参数设置为给定的 <code>java.sql.Blob</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>BLOB</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 映射 SQL <code>BLOB</code> 值的 <code>Blob</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void setBlob (int parameterIndex, Blob x) throws SQLException;

    /**
     * 将指定的参数设置为给定的 <code>java.sql.Clob</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>CLOB</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 映射 SQL <code>CLOB</code> 值的 <code>Clob</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void setClob (int parameterIndex, Clob x) throws SQLException;

    /**
     * 将指定的参数设置为给定的 <code>java.sql.Array</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>ARRAY</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 映射 SQL <code>ARRAY</code> 值的 <code>Array</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void setArray (int parameterIndex, Array x) throws SQLException;

                /**
     * 获取一个包含有关 <code>ResultSet</code> 对象列的信息的 <code>ResultSetMetaData</code> 对象，
     * 该 <code>ResultSet</code> 对象将在执行此 <code>PreparedStatement</code> 对象时返回。
     * <P>
     * 由于 <code>PreparedStatement</code> 对象是预编译的，因此可以在不执行它的情况下知道它将返回的 <code>ResultSet</code> 对象的信息。
     * 因此，可以在 <code>PreparedStatement</code> 对象上调用 <code>getMetaData</code> 方法，而不是等待执行它，
     * 然后在返回的 <code>ResultSet</code> 对象上调用 <code>ResultSet.getMetaData</code> 方法。
     * <P>
     * <B>注意：</B> 对于某些驱动程序，使用此方法可能代价高昂，因为底层 DBMS 缺乏支持。
     *
     * @return <code>ResultSet</code> 对象列的描述或如果驱动程序无法返回
     *         <code>ResultSetMetaData</code> 对象，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误或
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    ResultSetMetaData getMetaData() throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Date</code> 值，使用给定的 <code>Calendar</code> 对象。
     * 驱动程序使用 <code>Calendar</code> 对象构造一个 SQL <code>DATE</code> 值，然后将其发送到数据库。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区来计算日期。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @param cal 驱动程序将用于构造日期的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；
     * 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @since 1.2
     */
    void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
            throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Time</code> 值，使用给定的 <code>Calendar</code> 对象。
     * 驱动程序使用 <code>Calendar</code> 对象构造一个 SQL <code>TIME</code> 值，然后将其发送到数据库。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区来计算时间。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @param cal 驱动程序将用于构造时间的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；
     * 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @since 1.2
     */
    void setTime(int parameterIndex, java.sql.Time x, Calendar cal)
            throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Timestamp</code> 值，使用给定的 <code>Calendar</code> 对象。
     * 驱动程序使用 <code>Calendar</code> 对象构造一个 SQL <code>TIMESTAMP</code> 值，然后将其发送到数据库。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区来计算时间戳。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @param cal 驱动程序将用于构造时间戳的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；
     * 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @since 1.2
     */
    void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal)
            throws SQLException;

    /**
     * 将指定参数设置为 SQL <code>NULL</code>。
     * 此方法的版本应用于用户定义的类型和 REF 类型参数。用户定义类型的示例包括：STRUCT、DISTINCT、JAVA_OBJECT 和命名数组类型。
     *
     * <P><B>注意：</B> 为了可移植性，应用程序在指定 NULL 用户定义或 REF 参数时必须给出 SQL 类型代码和完全限定的 SQL 类型名称。
     * 对于用户定义的类型，名称是参数本身的类型名称。对于 REF 参数，名称是引用类型的类型名称。如果 JDBC 驱动程序不需要类型代码或类型名称信息，它可以忽略这些信息。
     *
     * 尽管此方法旨在用于用户定义和 Ref 参数，但可以用于设置任何 JDBC 类型的 null 参数。
     * 如果参数不是用户定义类型或 REF 类型，给定的 typeName 将被忽略。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param sqlType <code>java.sql.Types</code> 中的值
     * @param typeName SQL 用户定义类型的完全限定名称；如果参数不是用户定义类型或 REF，则忽略
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；
     * 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是
     * <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、<code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、
     * <code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、<code>REF</code>、<code>ROWID</code>、<code>SQLXML</code>
     * 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持此数据类型，或 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
  void setNull (int parameterIndex, int sqlType, String typeName)
    throws SQLException;

                //------------------------- JDBC 3.0 -----------------------------------

    /**
     * 将指定参数设置为给定的 <code>java.net.URL</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>DATALINK</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 要设置的 <code>java.net.URL</code> 对象
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setURL(int parameterIndex, java.net.URL x) throws SQLException;

    /**
     * 检索此 <code>PreparedStatement</code> 对象的参数的数量、类型和属性。
     *
     * @return 一个包含此 <code>PreparedStatement</code> 对象每个参数标记的
     * 数量、类型和属性信息的 <code>ParameterMetaData</code> 对象
     * @exception SQLException 如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @see ParameterMetaData
     * @since 1.4
     */
    ParameterMetaData getParameterMetaData() throws SQLException;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * 将指定参数设置为给定的 <code>java.sql.RowId</code> 对象。驱动程序在将其发送到数据库时
     * 将其转换为 SQL <code>ROWID</code> 值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param x 参数值
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
    void setRowId(int parameterIndex, RowId x) throws SQLException;


    /**
     * 将指定参数设置为给定的 <code>String</code> 对象。驱动程序在将其发送到数据库时
     * 将其转换为 SQL <code>NCHAR</code> 或 <code>NVARCHAR</code> 或 <code>LONGNVARCHAR</code>
     * 值（取决于参数大小相对于驱动程序对 <code>NVARCHAR</code> 值的限制）。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param value 参数值
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；
     * 如果发生数据库访问错误；或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNString(int parameterIndex, String value) throws SQLException;

    /**
     * 将指定参数设置为 <code>Reader</code> 对象。该 <code>Reader</code> 读取数据直到文件结束。
     * 驱动程序执行从 Java 字符格式到数据库中的国家字符集的必要转换。
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param value 参数值
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；
     * 如果发生数据库访问错误；或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException;

    /**
     * 将指定参数设置为 <code>java.sql.NClob</code> 对象。驱动程序在将其发送到数据库时
     * 将其转换为 SQL <code>NCLOB</code> 值。
     * @param parameterIndex 第一个参数是 1，第二个是 2，...
     * @param value 参数值
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；
     * 如果发生数据库访问错误；或在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNClob(int parameterIndex, NClob value) throws SQLException;

    /**
     * 将指定参数设置为 <code>Reader</code> 对象。读取器必须包含由长度指定的字符数，否则在执行
     * <code>PreparedStatement</code> 时将生成 <code>SQLException</code>。
     * 此方法与 <code>setCharacterStream (int, Reader, int)</code> 方法不同，因为它通知驱动程序
     * 参数值应作为 <code>CLOB</code> 发送到服务器。当使用 <code>setCharacterStream</code> 方法时，
     * 驱动程序可能需要做额外的工作来确定参数数据应作为 <code>LONGVARCHAR</code> 还是 <code>CLOB</code>
     * 发送到服务器。
     * @param parameterIndex 第一个参数的索引是 1，第二个是 2，...
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误；在此方法在已关闭的 <code>PreparedStatement</code> 上调用或
     * 指定的长度小于零时
     *
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setClob(int parameterIndex, Reader reader, long length)
       throws SQLException;


/**
 * 将指定参数设置为 <code>InputStream</code> 对象。输入流必须包含指定数量的字符，否则当 <code>PreparedStatement</code> 执行时将生成 <code>SQLException</code>。
 * 该方法与 <code>setBinaryStream (int, InputStream, int)</code> 方法不同，因为它通知驱动程序参数值应作为 <code>BLOB</code> 发送到服务器。
 * 当使用 <code>setBinaryStream</code> 方法时，驱动程序可能需要额外的工作来确定参数数据应作为 <code>LONGVARBINARY</code> 还是 <code>BLOB</code> 发送到服务器。
 * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
 * @param inputStream 包含要设置参数值的数据的对象。
 * @param length 参数数据的字节数。
 * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误；
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用；如果指定的长度小于零或输入流中的字节数与指定长度不匹配。
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 *
 * @since 1.6
 */
void setBlob(int parameterIndex, InputStream inputStream, long length)
    throws SQLException;
/**
 * 将指定参数设置为 <code>Reader</code> 对象。读取器必须包含指定数量的字符，否则当 <code>PreparedStatement</code> 执行时将生成 <code>SQLException</code>。
 * 该方法与 <code>setCharacterStream (int, Reader, int)</code> 方法不同，因为它通知驱动程序参数值应作为 <code>NCLOB</code> 发送到服务器。
 * 当使用 <code>setCharacterStream</code> 方法时，驱动程序可能需要额外的工作来确定参数数据应作为 <code>LONGNVARCHAR</code> 还是 <code>NCLOB</code> 发送到服务器。
 * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
 * @param reader 包含要设置参数值的数据的对象。
 * @param length 参数数据的字符数。
 * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果指定的长度小于零；
 * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 *
 * @since 1.6
 */
void setNClob(int parameterIndex, Reader reader, long length)
  throws SQLException;

/**
 * <p>将指定参数设置为给定的 <code>java.sql.SQLXML</code> 对象。
 * 驱动程序在将其发送到数据库时会将其转换为 SQL <code>XML</code> 值。
 * <p>
 *
 * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
 * @param xmlObject 映射 SQL <code>XML</code> 值的 <code>SQLXML</code> 对象
 * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误；
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * 或 <code>java.xml.transform.Result</code>、<code>Writer</code> 或 <code>OutputStream</code> 尚未关闭
 * 对于 <code>SQLXML</code> 对象
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 *
 * @since 1.6
 */
void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException;

/**
 * <p>使用给定的对象设置指定参数的值。
 *
 * 如果第二个参数是 <code>InputStream</code>，则流必须包含由 scaleOrLength 指定的字节数。如果第二个参数是
 * <code>Reader</code>，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些条件不满足，驱动程序将在执行预编译语句时生成 <code>SQLException</code>。
 *
 * <p>给定的 Java 对象将在发送到数据库之前转换为给定的目标 SQL 类型。
 *
 * 如果对象具有自定义映射（实现 <code>SQLData</code> 接口的类），
 * JDBC 驱动程序应调用 <code>SQLData.writeSQL</code> 方法将其写入 SQL 数据流。
 * 另一方面，如果对象是实现 <code>Ref</code>、<code>Blob</code>、<code>Clob</code>、<code>NClob</code>、
 * <code>Struct</code>、<code>java.net.URL</code> 或 <code>Array</code> 接口的类，驱动程序应将其作为相应 SQL 类型的值传递给数据库。
 *
 * <p>请注意，此方法可用于传递特定于数据库的抽象数据类型。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param x 包含输入参数值的对象
 * @param targetSqlType 要发送到数据库的 SQL 类型（在 java.sql.Types 中定义）。scale 参数可以进一步限定此类型。
 * @param scaleOrLength 对于 <code>java.sql.Types.DECIMAL</code>
 *          或 <code>java.sql.Types.NUMERIC types</code>，
 *          这是小数点后的位数。对于 Java 对象类型 <code>InputStream</code> 和 <code>Reader</code>，
 *          这是流或读取器中的数据长度。对于所有其他类型，
 *          此值将被忽略。
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误；
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用或
 *            如果 x 指定的 Java 对象是 InputStream
 *            或 Reader 对象且 scale 参数的值小于零
 * @exception SQLFeatureNotSupportedException 如果
 * JDBC 驱动程序不支持指定的目标 SQL 类型
 * @see Types
 *
 */
void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
        throws SQLException;
/**
 * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
 * 当非常大的 ASCII 值输入到 <code>LONGVARCHAR</code>
 * 参数时，通过 <code>java.io.InputStream</code> 发送可能更实际。数据将从流中按需读取，直到文件结束。
 * JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param x 包含 ASCII 参数值的 Java 输入流
 * @param length 流中的字节数
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @since 1.6
*/
void setAsciiStream(int parameterIndex, java.io.InputStream x, long length)
        throws SQLException;
/**
 * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
 * 当非常大的二进制值输入到 <code>LONGVARBINARY</code>
 * 参数时，通过 <code>java.io.InputStream</code> 对象发送可能更实际。数据将从流中按需读取，直到文件结束。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param x 包含二进制参数值的 Java 输入流
 * @param length 流中的字节数
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @since 1.6
 */
void setBinaryStream(int parameterIndex, java.io.InputStream x,
                     long length) throws SQLException;
/**
 * 将指定参数设置为给定的 <code>Reader</code>
 * 对象，该对象具有指定的字符数。
 * 当非常大的 UNICODE 值输入到 <code>LONGVARCHAR</code>
 * 参数时，通过 <code>java.io.Reader</code> 对象发送可能更实际。数据将从流中按需读取，直到文件结束。
 * JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param reader 包含 Unicode 数据的 <code>java.io.Reader</code> 对象
 * @param length 流中的字符数
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @since 1.6
 */
void setCharacterStream(int parameterIndex,
                        java.io.Reader reader,
                        long length) throws SQLException;
//-----
/**
 * 将指定参数设置为给定的输入流。
 * 当非常大的 ASCII 值输入到 <code>LONGVARCHAR</code>
 * 参数时，通过 <code>java.io.InputStream</code> 发送可能更实际。数据将从流中按需读取，直到文件结束。
 * JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 * <P><B>注意：</B>请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
 * <code>setAsciiStream</code> 版本是否更高效。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param x 包含 ASCII 参数值的 Java 输入流
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 * @since 1.6
*/
void setAsciiStream(int parameterIndex, java.io.InputStream x)
        throws SQLException;
/**
 * 将指定参数设置为给定的输入流。
 * 当非常大的二进制值输入到 <code>LONGVARBINARY</code>
 * 参数时，通过 <code>java.io.InputStream</code> 对象发送可能更实际。数据将从流中按需读取，直到文件结束。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 * <P><B>注意：</B>请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
 * <code>setBinaryStream</code> 版本是否更高效。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param x 包含二进制参数值的 Java 输入流
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 * @since 1.6
 */
void setBinaryStream(int parameterIndex, java.io.InputStream x)
throws SQLException;
/**
 * 将指定参数设置为给定的 <code>Reader</code>
 * 对象。
 * 当非常大的 UNICODE 值输入到 <code>LONGVARCHAR</code>
 * 参数时，通过 <code>java.io.Reader</code> 对象发送可能更实际。数据将从流中按需读取，直到文件结束。
 * JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
 *
 * <P><B>注意：</B>此流对象可以是标准的
 * Java 流对象或您自己的实现标准接口的子类。
 * <P><B>注意：</B>请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
 * <code>setCharacterStream</code> 版本是否更高效。
 *
 * @param parameterIndex 第一个参数为 1，第二个为 2，...
 * @param reader 包含 Unicode 数据的 <code>java.io.Reader</code> 对象
 * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；如果发生数据库访问错误或
 * 这个方法在已关闭的 <code>PreparedStatement</code> 上调用
 * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
 * @since 1.6
 */
void setCharacterStream(int parameterIndex,
                        java.io.Reader reader) throws SQLException;
/**
 * 将指定参数设置为 <code>Reader</code> 对象。读取器读取数据直到文件结束。驱动程序执行从 Java 字符格式到
 * 数据库国家字符集的必要转换。
 */


                 * <P><B>注意:</B> 该流对象可以是标准的
     * Java 流对象，也可以是实现了标准接口的自定义子类。
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定是否
     * 使用带有长度参数的 <code>setNCharacterStream</code> 版本会更高效。
     *
     * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
     * @param value 参数值
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换
     * 错误；如果发生数据库访问错误；或者在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNCharacterStream(int parameterIndex, Reader value) throws SQLException;

    /**
     * 将指定的参数设置为 <code>Reader</code> 对象。
     * 该方法与 <code>setCharacterStream (int, Reader)</code> 方法不同，
     * 因为它通知驱动程序参数值应作为 <code>CLOB</code> 发送到服务器。当使用
     * <code>setCharacterStream</code> 方法时，驱动程序可能需要额外的工作来确定参数
     * 数据应作为 <code>LONGVARCHAR</code> 还是 <code>CLOB</code> 发送到服务器。
     *
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定是否
     * 使用带有长度参数的 <code>setClob</code> 版本会更高效。
     *
     * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
     * @param reader 包含要设置的参数值的数据的对象。
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误；在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * 或者 parameterIndex 不对应于 SQL 语句中的参数标记
     *
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setClob(int parameterIndex, Reader reader)
       throws SQLException;

    /**
     * 将指定的参数设置为 <code>InputStream</code> 对象。
     * 该方法与 <code>setBinaryStream (int, InputStream)</code>
     * 方法不同，因为它通知驱动程序参数值应作为 <code>BLOB</code> 发送到服务器。当使用
     * <code>setBinaryStream</code> 方法时，驱动程序可能需要额外的工作来确定参数
     * 数据应作为 <code>LONGVARBINARY</code> 还是 <code>BLOB</code> 发送到服务器。
     *
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定是否
     * 使用带有长度参数的 <code>setBlob</code> 版本会更高效。
     *
     * @param parameterIndex 第一个参数的索引为 1，
     * 第二个为 2，...
     * @param inputStream 包含要设置的参数值的数据的对象。
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误；
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法或者
     * parameterIndex 不对应于 SQL 语句中的参数标记，
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setBlob(int parameterIndex, InputStream inputStream)
        throws SQLException;
    /**
     * 将指定的参数设置为 <code>Reader</code> 对象。
     * 该方法与 <code>setCharacterStream (int, Reader)</code> 方法不同，
     * 因为它通知驱动程序参数值应作为 <code>NCLOB</code> 发送到服务器。当使用
     * <code>setCharacterStream</code> 方法时，驱动程序可能需要额外的工作来确定参数
     * 数据应作为 <code>LONGNVARCHAR</code> 还是 <code>NCLOB</code> 发送到服务器。
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定是否
     * 使用带有长度参数的 <code>setNClob</code> 版本会更高效。
     *
     * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
     * @param reader 包含要设置的参数值的数据的对象。
     * @throws SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；
     * 如果驱动程序不支持国家字符集；
     * 如果驱动程序可以检测到可能发生数据转换
     * 错误；如果发生数据库访问错误或者
     * 在已关闭的 <code>PreparedStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setNClob(int parameterIndex, Reader reader)
       throws SQLException;

    //------------------------- JDBC 4.2 -----------------------------------

    /**
     * <p>使用给定的对象设置指定参数的值。
     *
     * 如果第二个参数是 {@code InputStream}，则流必须包含由 scaleOrLength 指定的字节数。
     * 如果第二个参数是 {@code Reader}，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些
     * 条件不满足，驱动程序将在执行预编译语句时生成一个
     * {@code SQLException}。
     *
     * <p>给定的 Java 对象将在发送到数据库之前转换为指定的 targetSqlType。
     *
     * 如果对象具有自定义映射（实现 {@code SQLData} 接口的类），
     * JDBC 驱动程序应调用方法 {@code SQLData.writeSQL} 将其写入 SQL 数据流。
     * 另一方面，如果对象是实现
     * {@code Ref}、{@code Blob}、{@code Clob}、{@code NClob}、
     * {@code Struct}、{@code java.net.URL} 或 {@code Array} 接口的类，
     * 驱动程序应将其作为相应 SQL 类型的值传递给数据库。
     *
     * <p>请注意，此方法可用于传递特定于数据库的抽象数据类型。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型。scale 参数可能进一步限定此类型。
     * @param scaleOrLength 对于 {@code java.sql.JDBCType.DECIMAL}
     *          或 {@code java.sql.JDBCType.NUMERIC} 类型，
     *          这是小数点后的位数。对于
     *          Java 对象类型 {@code InputStream} 和 {@code Reader}，
     *          这是流或读取器中的数据长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数
     * 标记；如果发生数据库访问错误
     * 或者在已关闭的 {@code PreparedStatement} 上调用此方法
     * 或者指定的 Java 对象 x 是 InputStream 或 Reader 对象且 scale 参数的值小于零
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void setObject(int parameterIndex, Object x, SQLType targetSqlType,
             int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("setObject not implemented");
    }

                /**
     * 设置指定参数的值为给定的对象。
     *
     * 此方法类似于 {@link #setObject(int parameterIndex,
     * Object x, SQLType targetSqlType, int scaleOrLength)}，
     * 但假设其精度为零。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @exception SQLException 如果 parameterIndex 不对应于 SQL 语句中的参数标记；
     * 数据库访问错误发生或此方法在已关闭的 {@code PreparedStatement} 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void setObject(int parameterIndex, Object x, SQLType targetSqlType)
      throws SQLException {
        throw new SQLFeatureNotSupportedException("setObject not implemented");
    }

    /**
     * 执行此 <code>PreparedStatement</code> 对象中的 SQL 语句，
     * 该语句必须是 SQL 数据操作语言 (DML) 语句，如 <code>INSERT</code>、<code>UPDATE</code> 或
     * <code>DELETE</code>；或者是不返回任何内容的 SQL 语句，如 DDL 语句。
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     * <p>
     * 默认实现将抛出 {@code UnsupportedOperationException}
     *
     * @return (1) 对于 SQL 数据操作语言 (DML) 语句，返回行数；(2) 对于不返回任何内容的 SQL 语句，返回 0
     * @exception SQLException 如果发生数据库访问错误；
     * 此方法在已关闭的 <code>PreparedStatement</code> 上调用
     * 或 SQL 语句返回一个 <code>ResultSet</code> 对象
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement} 时
     * @since 1.8
     */
    default long executeLargeUpdate() throws SQLException {
        throw new UnsupportedOperationException("executeLargeUpdate not implemented");
    }
}
