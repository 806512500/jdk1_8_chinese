
/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.math.BigDecimal;
import java.util.Calendar;
import java.io.Reader;
import java.io.InputStream;

/**
 * 用于执行 SQL 存储过程的接口。JDBC API 提供了一种存储过程 SQL 转义语法，允许以标准方式调用所有 RDBMS 的存储过程。这种转义语法有两种形式，一种包含结果参数，另一种不包含。如果使用结果参数，必须将其注册为 OUT 参数。其他参数可以用于输入、输出或两者。参数按数字顺序引用，第一个参数为 1。
 * <PRE>
 *   {?= call &lt;procedure-name&gt;[(&lt;arg1&gt;,&lt;arg2&gt;, ...)]}
 *   {call &lt;procedure-name&gt;[(&lt;arg1&gt;,&lt;arg2&gt;, ...)]}
 * </PRE>
 * <P>
 * IN 参数值使用从 {@link PreparedStatement} 继承的 <code>set</code> 方法设置。所有 OUT 参数的类型必须在执行存储过程之前注册；它们的值通过这里提供的 <code>get</code> 方法在执行后检索。
 * <P>
 * <code>CallableStatement</code> 可以返回一个 {@link ResultSet} 对象或多个 <code>ResultSet</code> 对象。多个 <code>ResultSet</code> 对象使用从 {@link Statement} 继承的操作处理。
 * <P>
 * 为了获得最大的可移植性，应在获取输出参数的值之前处理调用的 <code>ResultSet</code> 对象和更新计数。
 *
 *
 * @see Connection#prepareCall
 * @see ResultSet
 */

public interface CallableStatement extends PreparedStatement {

    /**
     * 注册位于序号位置 <code>parameterIndex</code> 的 OUT 参数为 JDBC 类型 <code>sqlType</code>。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型 <code>sqlType</code> 确定了在 <code>get</code> 方法中读取该参数值时必须使用的 Java 类型。
     * <p>
     * 如果预期返回给此输出参数的 JDBC 类型特定于该数据库，<code>sqlType</code> 应为 <code>java.sql.Types.OTHER</code>。方法 {@link #getObject} 用于检索值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param sqlType 由 <code>java.sql.Types</code> 定义的 JDBC 类型代码。如果参数是 JDBC 类型 <code>NUMERIC</code> 或 <code>DECIMAL</code>，应使用接受 scale 值的 <code>registerOutParameter</code> 版本。
     *
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是 <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、<code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、<code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、<code>REF</code>、<code>ROWID</code>、<code>SQLXML</code> 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持这些数据类型
     * @see Types
     */
    void registerOutParameter(int parameterIndex, int sqlType)
        throws SQLException;

    /**
     * 注册位于序号位置 <code>parameterIndex</code> 的参数为 JDBC 类型 <code>sqlType</code>。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型 <code>sqlType</code> 确定了在 <code>get</code> 方法中读取该参数值时必须使用的 Java 类型。
     * <p>
     * 当参数是 JDBC 类型 <code>NUMERIC</code> 或 <code>DECIMAL</code> 时，应使用此版本的 <code>registerOutParameter</code>。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param sqlType 由 <code>java.sql.Types</code> 定义的 SQL 类型代码。
     * @param scale 所需的小数点右边的位数。必须大于或等于零。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是 <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、<code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、<code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、<code>REF</code>、<code>ROWID</code>、<code>SQLXML</code> 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持这些数据类型
     * @see Types
     */
    void registerOutParameter(int parameterIndex, int sqlType, int scale)
        throws SQLException;

    /**
     * 检索最后一个读取的 OUT 参数是否具有 SQL <code>NULL</code> 值。注意，此方法仅应在调用 getter 方法之后调用；否则，没有值可用于确定其是否为 <code>null</code>。
     *
     * @return 如果最后一个读取的参数是 SQL <code>NULL</code>，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或在已关闭的 <code>CallableStatement</code> 上调用此方法
     */
    boolean wasNull() throws SQLException;

    /**
     * 以 Java 编程语言中的 <code>String</code> 形式检索指定的 JDBC <code>CHAR</code>、<code>VARCHAR</code> 或 <code>LONGVARCHAR</code> 参数的值。
     * <p>
     * 对于固定长度类型 JDBC <code>CHAR</code>，返回的 <code>String</code> 对象具有与数据库中的 SQL <code>CHAR</code> 值完全相同的值，包括数据库添加的任何填充。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setString
     */
    String getString(int parameterIndex) throws SQLException;

                /**
     * 获取指定的 JDBC <code>BIT</code>
     * 或 <code>BOOLEAN</code> 参数的值，作为 Java 编程语言中的
     * <code>boolean</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，
     *         结果是 <code>false</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setBoolean
     */
    boolean getBoolean(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>TINYINT</code> 参数的值，
     * 作为 Java 编程语言中的 <code>byte</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setByte
     */
    byte getByte(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>SMALLINT</code> 参数的值，
     * 作为 Java 编程语言中的 <code>short</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setShort
     */
    short getShort(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>INTEGER</code> 参数的值，
     * 作为 Java 编程语言中的 <code>int</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setInt
     */
    int getInt(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>BIGINT</code> 参数的值，
     * 作为 Java 编程语言中的 <code>long</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setLong
     */
    long getLong(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>FLOAT</code> 参数的值，
     * 作为 Java 编程语言中的 <code>float</code> 类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     *         是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setFloat
     */
    float getFloat(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>DOUBLE</code> 参数的值，作为 Java 编程语言中的 <code>double</code> 类型。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     *         是 <code>0</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setDouble
     */
    double getDouble(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>NUMERIC</code> 参数的值，作为
     * <code>java.math.BigDecimal</code> 对象，具有 <i>scale</i> 位小数。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        和以此类推
     * @param scale 小数点右边的位数
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     *         是 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @deprecated 使用 <code>getBigDecimal(int parameterIndex)</code>
     *             或 <code>getBigDecimal(String parameterName)</code>
     * @see #setBigDecimal
     */
    @Deprecated
    BigDecimal getBigDecimal(int parameterIndex, int scale)
        throws SQLException;

    /**
     * 获取指定的 JDBC <code>BINARY</code> 或
     * <code>VARBINARY</code> 参数的值，作为 Java 编程语言中的 <code>byte</code> 数组。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        和以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     *         是 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setBytes
     */
    byte[] getBytes(int parameterIndex) throws SQLException;

                /**
     * 获取指定的 JDBC <code>DATE</code> 参数的值，作为 <code>java.sql.Date</code> 对象。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setDate
     */
    java.sql.Date getDate(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>TIME</code> 参数的值，作为 <code>java.sql.Time</code> 对象。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setTime
     */
    java.sql.Time getTime(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>TIMESTAMP</code> 参数的值，作为 <code>java.sql.Timestamp</code> 对象。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        以此类推
     * @return 参数值。如果值是 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setTimestamp
     */
    java.sql.Timestamp getTimestamp(int parameterIndex)
        throws SQLException;

    //----------------------------------------------------------------------
    // 高级功能:


    /**
     * 以 Java 编程语言中的 <code>Object</code> 获取指定参数的值。如果值是 SQL <code>NULL</code>，
     * 驱动程序返回 Java <code>null</code>。
     * <p>
     * 此方法返回一个 Java 对象，其类型对应于使用方法 <code>registerOutParameter</code> 为此参数注册的 JDBC
     * 类型。通过将目标 JDBC 类型注册为 <code>java.sql.Types.OTHER</code>，此方法可用于读取数据库特定的抽象数据类型。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     *        以此类推
     * @return 包含 OUT 参数值的 <code>java.lang.Object</code>
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see Types
     * @see #setObject
     */
    Object getObject(int parameterIndex) throws SQLException;


    //--------------------------JDBC 2.0-----------------------------

    /**
     * 获取指定的 JDBC <code>NUMERIC</code> 参数的值，作为 <code>java.math.BigDecimal</code> 对象，其小数点后的位数与值本身包含的位数相同。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 完整精度的参数值。如果值是 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setBigDecimal
     * @since 1.2
     */
    BigDecimal getBigDecimal(int parameterIndex) throws SQLException;

    /**
     * 返回一个表示 OUT 参数 <code>parameterIndex</code> 值的对象，并使用 <code>map</code> 进行参数值的自定义映射。
     * <p>
     * 此方法返回一个 Java 对象，其类型对应于使用方法 <code>registerOutParameter</code> 为此参数注册的 JDBC
     * 类型。通过将目标 JDBC 类型注册为 <code>java.sql.Types.OTHER</code>，此方法可用于读取数据库特定的抽象数据类型。
     * @param parameterIndex 第一个参数是 1，第二个是 2，以此类推
     * @param map 从 SQL 类型名称到 Java 类的映射
     * @return 包含 OUT 参数值的 <code>java.lang.Object</code>
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setObject
     * @since 1.2
     */
    Object getObject(int parameterIndex, java.util.Map<String,Class<?>> map)
        throws SQLException;

    /**
     * 获取指定的 JDBC <code>REF(&lt;structured-type&gt;)</code> 参数的值，作为 Java 编程语言中的 {@link java.sql.Ref} 对象。
     * @param parameterIndex 第一个参数是 1，第二个是 2，
     * 和以此类推
     * @return 参数值作为 Java 编程语言中的 <code>Ref</code> 对象。如果值为 SQL <code>NULL</code>，则返回值为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Ref getRef (int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>BLOB</code> 参数的值，作为 Java 编程语言中的 {@link java.sql.Blob} 对象。
     * @param parameterIndex 第一个参数是 1，第二个是 2，以此类推
     * @return 参数值作为 Java 编程语言中的 <code>Blob</code> 对象。如果值为 SQL <code>NULL</code>，则返回值为 <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Blob getBlob (int parameterIndex) throws SQLException;

                /**
     * 获取指定的 JDBC <code>CLOB</code> 参数的值，作为 Java 编程语言中的
     * <code>java.sql.Clob</code> 对象。
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @return 作为 Java 编程语言中的 <code>Clob</code> 对象的参数值。如果值为 SQL <code>NULL</code>，则返回 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Clob getClob (int parameterIndex) throws SQLException;

    /**
     *
     * 获取指定的 JDBC <code>ARRAY</code> 参数的值，作为 Java 编程语言中的
     * {@link java.sql.Array} 对象。
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @return 作为 Java 编程语言中的 <code>Array</code> 对象的参数值。如果值为 SQL <code>NULL</code>，则返回 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Array getArray (int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>DATE</code> 参数的值，作为
     * <code>java.sql.Date</code> 对象，使用
     * 给定的 <code>Calendar</code> 对象构造日期。
     * 使用 <code>Calendar</code> 对象，驱动程序
     * 可以根据自定义时区和区域设置计算日期。
     * 如果未指定 <code>Calendar</code> 对象，驱动程序将使用
     * 默认时区和区域设置。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param cal 驱动程序将用于构造日期的 <code>Calendar</code> 对象
     * @return 参数值。如果值为 SQL <code>NULL</code>，结果为 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setDate
     * @since 1.2
     */
    java.sql.Date getDate(int parameterIndex, Calendar cal)
        throws SQLException;

    /**
     * 获取指定的 JDBC <code>TIME</code> 参数的值，作为
     * <code>java.sql.Time</code> 对象，使用
     * 给定的 <code>Calendar</code> 对象构造时间。
     * 使用 <code>Calendar</code> 对象，驱动程序
     * 可以根据自定义时区和区域设置计算时间。
     * 如果未指定 <code>Calendar</code> 对象，驱动程序将使用
     * 默认时区和区域设置。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param cal 驱动程序将用于构造时间的 <code>Calendar</code> 对象
     * @return 参数值；如果值为 SQL <code>NULL</code>，结果为 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setTime
     * @since 1.2
     */
    java.sql.Time getTime(int parameterIndex, Calendar cal)
        throws SQLException;

    /**
     * 获取指定的 JDBC <code>TIMESTAMP</code> 参数的值，作为
     * <code>java.sql.Timestamp</code> 对象，使用
     * 给定的 <code>Calendar</code> 对象构造
     * <code>Timestamp</code> 对象。
     * 使用 <code>Calendar</code> 对象，驱动程序
     * 可以根据自定义时区和区域设置计算时间戳。
     * 如果未指定 <code>Calendar</code> 对象，驱动程序将使用
     * 默认时区和区域设置。
     *
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param cal 驱动程序将用于构造时间戳的 <code>Calendar</code> 对象
     * @return 参数值。如果值为 SQL <code>NULL</code>，结果为 <code>null</code>。
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @see #setTimestamp
     * @since 1.2
     */
    java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal)
        throws SQLException;


    /**
     * 注册指定的输出参数。
     * 此版本的
     * <code>registerOutParameter</code> 方法
     * 应用于用户定义的或 <code>REF</code> 输出参数。用户定义的类型示例包括：<code>STRUCT</code>，<code>DISTINCT</code>，
     * <code>JAVA_OBJECT</code> 和命名数组类型。
     *<p>
     * 所有输出参数都必须在执行存储过程之前注册。
     * <p> 对于用户定义的参数，应提供参数的完全限定的 SQL
     * 类型名称，而 <code>REF</code> 参数需要提供引用类型的完全限定类型名称。不需要类型代码和类型名称信息的 JDBC 驱动程序可以忽略这些信息。 为了可移植性，
     * 应用程序应始终为用户定义的和 <code>REF</code> 参数提供这些值。
     *
     * 尽管此方法旨在用于用户定义的和 <code>REF</code> 参数，
     * 但可以用于注册任何 JDBC 类型的参数。
     * 如果参数没有用户定义的或 <code>REF</code> 类型，<i>typeName</i> 参数将被忽略。
     *
     * <P><B>注意：</B> 读取输出参数的值时，必须使用与
     * 参数注册的 SQL 类型对应的 Java 类型的 getter 方法。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @param sqlType {@link java.sql.Types} 中的一个值
     * @param typeName SQL 结构化类型的完全限定名称
     * @exception SQLException 如果参数索引无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是
     * <code>ARRAY</code>，<code>BLOB</code>，<code>CLOB</code>，
     * <code>DATALINK</code>，<code>JAVA_OBJECT</code>，<code>NCHAR</code>，
     * <code>NCLOB</code>，<code>NVARCHAR</code>，<code>LONGNVARCHAR</code>，
     * <code>REF</code>，<code>ROWID</code>，<code>SQLXML</code>
     * 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持此数据类型
     * @see Types
     * @since 1.2
     */
    void registerOutParameter (int parameterIndex, int sqlType, String typeName)
        throws SQLException;

              //--------------------------JDBC 3.0-----------------------------

    /**
     * 注册名为
     * <code>parameterName</code> 的 OUT 参数为 JDBC 类型
     * <code>sqlType</code>。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型决定了在 <code>get</code> 方法中读取该参数值时必须使用的 Java 类型。
     * <p>
     * 如果预期返回给此输出参数的 JDBC 类型特定于该数据库，则 <code>sqlType</code>
     * 应为 <code>java.sql.Types.OTHER</code>。方法
     * {@link #getObject} 用于检索值。
     * @param parameterName 参数的名称
     * @param sqlType 由 <code>java.sql.Types</code> 定义的 JDBC 类型代码。
     * 如果参数的 JDBC 类型为 <code>NUMERIC</code>
     * 或 <code>DECIMAL</code>，应使用接受比例值的
     * <code>registerOutParameter</code> 版本。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是
     * <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、
     * <code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、
     * <code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、
     * <code>REF</code>、<code>ROWID</code>、<code>SQLXML</code>
     * 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持此数据类型，或者 JDBC 驱动程序不支持此方法
     * @since 1.4
     * @see Types
     */
    void registerOutParameter(String parameterName, int sqlType)
        throws SQLException;

    /**
     * 注册名为
     * <code>parameterName</code> 的参数为 JDBC 类型
     * <code>sqlType</code>。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型决定了在 <code>get</code> 方法中读取该参数值时必须使用的 Java 类型。
     * <p>
     * 当参数的 JDBC 类型为 <code>NUMERIC</code>
     * 或 <code>DECIMAL</code> 时，应使用此版本的 <code>registerOutParameter</code>。
     *
     * @param parameterName 参数的名称
     * @param sqlType 由 <code>java.sql.Types</code> 定义的 SQL 类型代码。
     * @param scale 小数点右侧所需的位数。必须大于或等于零。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是
     * <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、
     * <code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、
     * <code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、
     * <code>REF</code>、<code>ROWID</code>、<code>SQLXML</code>
     * 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持此数据类型，或者 JDBC 驱动程序不支持此方法
     * @since 1.4
     * @see Types
     */
    void registerOutParameter(String parameterName, int sqlType, int scale)
        throws SQLException;

    /**
     * 注册指定的输出参数。此版本的 <code>registerOutParameter</code> 方法
     * 应用于用户命名的或 REF 输出参数。用户命名的类型示例包括：STRUCT、DISTINCT、JAVA_OBJECT 和
     * 命名数组类型。
     *<p>
     * 所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 对于用户命名的参数，应给出参数的完全限定的 SQL 类型名称，而 REF 参数则需要给出引用类型的完全限定类型名称。不需要类型代码和类型名称信息的 JDBC 驱动程序可能会忽略这些信息。然而，为了可移植性，应用程序应始终为用户命名的和 REF 参数提供这些值。
     *
     * 尽管此方法旨在用于用户命名的和 REF 参数，
     * 但可以用于注册任何 JDBC 类型的参数。如果参数没有用户命名的或 REF 类型，则忽略 typeName 参数。
     *
     * <P><B>注意：</B> 读取输出参数的值时，必须使用与参数注册的 SQL 类型相对应的 Java 类型的 <code>getXXX</code> 方法。
     *
     * @param parameterName 参数的名称
     * @param sqlType 来自 {@link java.sql.Types} 的值
     * @param typeName SQL 结构化类型的完全限定名称
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 <code>sqlType</code> 是
     * <code>ARRAY</code>、<code>BLOB</code>、<code>CLOB</code>、
     * <code>DATALINK</code>、<code>JAVA_OBJECT</code>、<code>NCHAR</code>、
     * <code>NCLOB</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code>、
     * <code>REF</code>、<code>ROWID</code>、<code>SQLXML</code>
     * 或 <code>STRUCT</code> 数据类型，且 JDBC 驱动程序不支持此数据类型，或者 JDBC 驱动程序不支持此方法
     * @see Types
     * @since 1.4
     */
    void registerOutParameter (String parameterName, int sqlType, String typeName)
        throws SQLException;

    /**
     * 以 <code>java.net.URL</code> 对象的形式检索指定的 JDBC <code>DATALINK</code> 参数的值。
     *
     * @param parameterIndex 第一个参数为 1，第二个参数为 2，依此类推
     * @return 一个 <code>java.net.URL</code> 对象，表示用作指定参数的
     *         JDBC <code>DATALINK</code> 值
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误，此方法在已关闭的 <code>CallableStatement</code> 上调用，
     *            或返回的 URL 在 Java 平台上不是有效 URL
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setURL
     * @since 1.4
     */
    java.net.URL getURL(int parameterIndex) throws SQLException;

                /**
     * 将指定参数设置为给定的 <code>java.net.URL</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>DATALINK</code> 值。
     *
     * @param parameterName 参数的名称
     * @param val 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误；
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     *            或 URL 格式不正确
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getURL
     * @since 1.4
     */
    void setURL(String parameterName, java.net.URL val) throws SQLException;

    /**
     * 将指定参数设置为 SQL <code>NULL</code>。
     *
     * <P><B>注意:</B> 必须指定参数的 SQL 类型。
     *
     * @param parameterName 参数的名称
     * @param sqlType 在 <code>java.sql.Types</code> 中定义的 SQL 类型代码
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setNull(String parameterName, int sqlType) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>boolean</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>BIT</code> 或 <code>BOOLEAN</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @see #getBoolean
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setBoolean(String parameterName, boolean x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>byte</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>TINYINT</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getByte
     * @since 1.4
     */
    void setByte(String parameterName, byte x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>short</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>SMALLINT</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getShort
     * @since 1.4
     */
    void setShort(String parameterName, short x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>int</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>INTEGER</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getInt
     * @since 1.4
     */
    void setInt(String parameterName, int x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>long</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>BIGINT</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getLong
     * @since 1.4
     */
    void setLong(String parameterName, long x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>float</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>FLOAT</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getFloat
     * @since 1.4
     */
    void setFloat(String parameterName, float x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>double</code> 值。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>DOUBLE</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getDouble
     * @since 1.4
     */
    void setDouble(String parameterName, double x) throws SQLException;

                /**
     * 将指定参数设置为给定的 <code>java.math.BigDecimal</code> 值。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>NUMERIC</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getBigDecimal
     * @since 1.4
     */
    void setBigDecimal(String parameterName, BigDecimal x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java <code>String</code> 值。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>VARCHAR</code> 或 <code>LONGVARCHAR</code> 值
     * （取决于参数的大小与驱动程序对 <code>VARCHAR</code> 值的限制）。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getString
     * @since 1.4
     */
    void setString(String parameterName, String x) throws SQLException;

    /**
     * 将指定参数设置为给定的 Java 字节数组。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>VARBINARY</code> 或 <code>LONGVARBINARY</code>
     * （取决于参数的大小与驱动程序对 <code>VARBINARY</code> 值的限制）。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getBytes
     * @since 1.4
     */
    void setBytes(String parameterName, byte x[]) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Date</code> 值，使用运行应用程序的虚拟机的默认时区。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>DATE</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getDate
     * @since 1.4
     */
    void setDate(String parameterName, java.sql.Date x)
        throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Time</code> 值。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>TIME</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getTime
     * @since 1.4
     */
    void setTime(String parameterName, java.sql.Time x)
        throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Timestamp</code> 值。
     * 驱动程序在将数据发送到数据库时将其转换为 SQL <code>TIMESTAMP</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getTimestamp
     * @since 1.4
     */
    void setTimestamp(String parameterName, java.sql.Timestamp x)
        throws SQLException;

    /**
     * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
     * 当将非常大的 ASCII 值输入到 <code>LONGVARCHAR</code> 参数时，通过 <code>java.io.InputStream</code>
     * 发送可能更为实际。数据将从流中按需读取，直到达到文件末尾。JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的自定义子类。
     *
     * @param parameterName 参数的名称
     * @param x 包含 ASCII 参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setAsciiStream(String parameterName, java.io.InputStream x, int length)
        throws SQLException;

    /**
     * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
     * 当将非常大的二进制值输入到 <code>LONGVARBINARY</code> 参数时，通过 <code>java.io.InputStream</code> 对象发送可能更为实际。
     * 数据将从流中按需读取，直到达到文件末尾。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的自定义子类。
     *
     * @param parameterName 参数的名称
     * @param x 包含二进制参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setBinaryStream(String parameterName, java.io.InputStream x,
                         int length) throws SQLException;

                /**
     * 设置指定参数的值为给定的对象。
     *
     * <p>给定的 Java 对象将在发送到数据库之前转换为给定的目标 SQL 类型。
     *
     * 如果对象具有自定义映射（即实现了 <code>SQLData</code> 接口的类），
     * JDBC 驱动程序应调用 <code>SQLData.writeSQL</code> 方法将其写入 SQL 数据流。
     * 另一方面，如果对象是实现了 <code>Ref</code>、<code>Blob</code>、<code>Clob</code>、<code>NClob</code>、
     * <code>Struct</code>、<code>java.net.URL</code> 或 <code>Array</code> 接口的类，
     * 驱动程序应将其作为相应 SQL 类型的值传递给数据库。
     * <P>
     * 注意，此方法可用于传递特定于数据库的抽象数据类型。
     *
     * @param parameterName 参数的名称
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型（如 java.sql.Types 中定义）。scale 参数可以进一步限定此类型。
     * @param scale 对于 java.sql.Types.DECIMAL 或 java.sql.Types.NUMERIC 类型，
     *          这是小数点后的位数。对于所有其他类型，此值将被忽略。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的目标 SQL 类型
     * @see Types
     * @see #getObject
     * @since 1.4
     */
    void setObject(String parameterName, Object x, int targetSqlType, int scale)
        throws SQLException;

    /**
     * 设置指定参数的值为给定的对象。
     *
     * 此方法类似于 {@link #setObject(String parameterName,
     * Object x, int targetSqlType, int scaleOrLength)}，
     * 但假设 scale 为零。
     *
     * @param parameterName 参数的名称
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型（如 java.sql.Types 中定义）
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的目标 SQL 类型
     * @see #getObject
     * @since 1.4
     */
    void setObject(String parameterName, Object x, int targetSqlType)
        throws SQLException;

    /**
     * 设置指定参数的值为给定的对象。
     *
     * <p>JDBC 规范指定了从 Java <code>Object</code> 类型到 SQL 类型的标准映射。给定的参数
     * 将在发送到数据库之前转换为相应的 SQL 类型。
     * <p>注意，此方法可用于通过使用特定于驱动程序的 Java 类型传递特定于数据库的抽象数据类型。
     *
     * 如果对象是实现了 <code>SQLData</code> 接口的类，
     * JDBC 驱动程序应调用 <code>SQLData.writeSQL</code> 方法将其写入 SQL 数据流。
     * 另一方面，如果对象是实现了 <code>Ref</code>、<code>Blob</code>、<code>Clob</code>、<code>NClob</code>、
     * <code>Struct</code>、<code>java.net.URL</code> 或 <code>Array</code> 接口的类，
     * 驱动程序应将其作为相应 SQL 类型的值传递给数据库。
     * <P>
     * 如果存在歧义，例如，对象是实现了上述多个接口的类，此方法将抛出异常。
     *<p>
     *<b>注意：</b>并非所有数据库都允许发送非类型的 Null 到后端。为了最大程度的可移植性，应使用 <code>setNull</code> 或
     * <code>setObject(String parameterName, Object x, int sqlType)</code> 方法
     * 而不是 <code>setObject(String parameterName, Object x)</code>。
     *<p>
     * @param parameterName 参数的名称
     * @param x 包含输入参数值的对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误，
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用或给定的 <code>Object</code> 参数存在歧义
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getObject
     * @since 1.4
     */
    void setObject(String parameterName, Object x) throws SQLException;


    /**
     * 将指定参数设置为给定的 <code>Reader</code> 对象，该对象具有给定的字符数。
     * 当将非常大的 UNICODE 值输入到 <code>LONGVARCHAR</code> 参数时，通过
     * <code>java.io.Reader</code> 对象发送可能更为实用。数据将从流中按需读取，直到文件结束。
     * JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象或实现标准接口的您自己的子类。
     *
     * @param parameterName 参数的名称
     * @param reader 包含用作指定参数的 UNICODE 数据的 <code>java.io.Reader</code> 对象
     * @param length 流中的字符数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setCharacterStream(String parameterName,
                            java.io.Reader reader,
                            int length) throws SQLException;

                /**
     * 将指定参数设置为给定的 <code>java.sql.Date</code> 值，
     * 使用给定的 <code>Calendar</code> 对象。驱动程序使用
     * <code>Calendar</code> 对象来构造一个 SQL <code>DATE</code> 值，
     * 然后将该值发送到数据库。使用
     * <code>Calendar</code> 对象，驱动程序可以计算出考虑自定义时区的日期。如果没有
     * 指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @param cal 驱动程序将用于构造日期的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getDate
     * @since 1.4
     */
    void setDate(String parameterName, java.sql.Date x, Calendar cal)
        throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Time</code> 值，
     * 使用给定的 <code>Calendar</code> 对象。驱动程序使用
     * <code>Calendar</code> 对象来构造一个 SQL <code>TIME</code> 值，
     * 然后将该值发送到数据库。使用
     * <code>Calendar</code> 对象，驱动程序可以计算出考虑自定义时区的时间。如果没有
     * 指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @param cal 驱动程序将用于构造时间的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getTime
     * @since 1.4
     */
    void setTime(String parameterName, java.sql.Time x, Calendar cal)
        throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Timestamp</code> 值，
     * 使用给定的 <code>Calendar</code> 对象。驱动程序使用
     * <code>Calendar</code> 对象来构造一个 SQL <code>TIMESTAMP</code> 值，
     * 然后将该值发送到数据库。使用
     * <code>Calendar</code> 对象，驱动程序可以计算出考虑自定义时区的时间戳。如果没有
     * 指定 <code>Calendar</code> 对象，驱动程序将使用默认时区，即运行应用程序的虚拟机的时区。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @param cal 驱动程序将用于构造时间戳的 <code>Calendar</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getTimestamp
     * @since 1.4
     */
    void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal)
        throws SQLException;

    /**
     * 将指定参数设置为 SQL <code>NULL</code>。
     * 此方法的版本 <code>setNull</code> 应用于用户定义的类型和 REF 类型参数。示例
     * 包括：STRUCT, DISTINCT, JAVA_OBJECT 和命名数组类型。
     *
     * <P><B>注意：</B>为了确保可移植性，应用程序在指定 NULL 用户定义或 REF 参数时必须给出
     * SQL 类型代码和完全限定的 SQL 类型名称。对于用户定义的类型，
     * 名称是参数本身的类型名称。对于 REF 参数，名称是引用类型的类型名称。
     * <p>
     * 尽管此方法旨在用于用户定义和 REF 参数，
     * 但可以用于设置任何 JDBC 类型的 NULL 参数。如果参数不是用户定义类型或
     * SQL <code>REF</code> 值，给定的 typeName 将被忽略。
     *
     *
     * @param parameterName 参数的名称
     * @param sqlType <code>java.sql.Types</code> 中的一个值
     * @param typeName 完全限定的 SQL 用户定义类型名称；
     *        如果参数不是用户定义类型或 SQL <code>REF</code> 值，则忽略
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void setNull (String parameterName, int sqlType, String typeName)
        throws SQLException;

    /**
     * 以 Java 编程语言中的 <code>String</code> 形式检索 JDBC <code>CHAR</code>、<code>VARCHAR</code>，
     * 或 <code>LONGVARCHAR</code> 参数的值。
     * <p>
     * 对于固定长度的 JDBC <code>CHAR</code> 类型，
     * 返回的 <code>String</code> 对象
     * 与数据库中的 SQL
     * <code>CHAR</code> 值完全相同，包括数据库添加的任何填充。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果为 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setString
     * @since 1.4
     */
    String getString(String parameterName) throws SQLException;

                /**
     * 获取 JDBC <code>BIT</code> 或 <code>BOOLEAN</code>
     * 参数的值，作为 Java 编程语言中的 <code>boolean</code>。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>false</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setBoolean
     * @since 1.4
     */
    boolean getBoolean(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>TINYINT</code> 参数的值，作为 Java 编程语言中的 <code>byte</code>。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setByte
     * @since 1.4
     */
    byte getByte(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>SMALLINT</code> 参数的值，作为 Java 编程语言中的 <code>short</code>。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setShort
     * @since 1.4
     */
    short getShort(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>INTEGER</code> 参数的值，作为 Java 编程语言中的 <code>int</code>。
     *
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，
     *         结果是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setInt
     * @since 1.4
     */
    int getInt(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>BIGINT</code> 参数的值，作为 Java 编程语言中的 <code>long</code>。
     *
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，
     *         结果是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setLong
     * @since 1.4
     */
    long getLong(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>FLOAT</code> 参数的值，作为 Java 编程语言中的 <code>float</code>。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，
     *         结果是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setFloat
     * @since 1.4
     */
    float getFloat(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>DOUBLE</code> 参数的值，作为 Java 编程语言中的 <code>double</code>。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，
     *         结果是 <code>0</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setDouble
     * @since 1.4
     */
    double getDouble(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>BINARY</code> 或 <code>VARBINARY</code>
     * 参数的值，作为 Java 编程语言中的 <code>byte</code> 数组。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果是
     *  <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setBytes
     * @since 1.4
     */
    byte[] getBytes(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>DATE</code> 参数的值，作为
     * <code>java.sql.Date</code> 对象。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果
     * 是 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setDate
     * @since 1.4
     */
    java.sql.Date getDate(String parameterName) throws SQLException;

                /**
     * 获取 JDBC <code>TIME</code> 参数的值，作为 <code>java.sql.Time</code> 对象。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果是 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setTime
     * @since 1.4
     */
    java.sql.Time getTime(String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>TIMESTAMP</code> 参数的值，作为 <code>java.sql.Timestamp</code> 对象。
     * @param parameterName 参数的名称
     * @return 参数值。如果值是 SQL <code>NULL</code>，结果是 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setTimestamp
     * @since 1.4
     */
    java.sql.Timestamp getTimestamp(String parameterName) throws SQLException;

    /**
     * 以 Java 编程语言中的 <code>Object</code> 形式获取参数的值。如果值是 SQL <code>NULL</code>，
     * 驱动程序返回 Java <code>null</code>。
     * <p>
     * 此方法返回一个 Java 对象，其类型对应于使用 <code>registerOutParameter</code> 方法为此参数注册的 JDBC 类型。
     * 通过将目标 JDBC 类型注册为 <code>java.sql.Types.OTHER</code>，此方法可用于读取数据库特定的抽象数据类型。
     * @param parameterName 参数的名称
     * @return 包含 OUT 参数值的 <code>java.lang.Object</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see Types
     * @see #setObject
     * @since 1.4
     */
    Object getObject(String parameterName) throws SQLException;

    /**
     * 以 <code>java.math.BigDecimal</code> 对象的形式获取 JDBC <code>NUMERIC</code> 参数的值，该对象包含值中的所有小数位。
     * @param parameterName 参数的名称
     * @return 完整精度的参数值。如果值是 SQL <code>NULL</code>，结果是 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setBigDecimal
     * @since 1.4
     */
    BigDecimal getBigDecimal(String parameterName) throws SQLException;

    /**
     * 返回一个表示 OUT 参数 <code>parameterName</code> 值的对象，并使用 <code>map</code> 进行参数值的自定义映射。
     * <p>
     * 此方法返回一个 Java 对象，其类型对应于使用 <code>registerOutParameter</code> 方法为此参数注册的 JDBC 类型。
     * 通过将目标 JDBC 类型注册为 <code>java.sql.Types.OTHER</code>，此方法可用于读取数据库特定的抽象数据类型。
     * @param parameterName 参数的名称
     * @param map 从 SQL 类型名称到 Java 类的映射
     * @return 包含 OUT 参数值的 <code>java.lang.Object</code>
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setObject
     * @since 1.4
     */
    Object getObject(String parameterName, java.util.Map<String,Class<?>> map)
      throws SQLException;

    /**
     * 以 Java 编程语言中的 {@link java.sql.Ref} 对象形式获取 JDBC <code>REF(&lt;structured-type&gt;)</code> 参数的值。
     *
     * @param parameterName 参数的名称
     * @return 以 Java 编程语言中的 <code>Ref</code> 对象形式的参数值。如果值是 SQL <code>NULL</code>，
     *         返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    Ref getRef (String parameterName) throws SQLException;

    /**
     * 以 Java 编程语言中的 {@link java.sql.Blob} 对象形式获取 JDBC <code>BLOB</code> 参数的值。
     *
     * @param parameterName 参数的名称
     * @return 以 Java 编程语言中的 <code>Blob</code> 对象形式的参数值。如果值是 SQL <code>NULL</code>，
     *         返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    Blob getBlob (String parameterName) throws SQLException;

                /**
     * 获取 JDBC <code>CLOB</code> 参数的值，作为 Java 编程语言中的
     * <code>java.sql.Clob</code> 对象。
     * @param parameterName 参数的名称
     * @return 作为 Java 编程语言中的 <code>Clob</code> 对象的参数值。如果值为 SQL <code>NULL</code>，
     *         则返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    Clob getClob (String parameterName) throws SQLException;

    /**
     * 获取 JDBC <code>ARRAY</code> 参数的值，作为 Java 编程语言中的
     * {@link java.sql.Array} 对象。
     *
     * @param parameterName 参数的名称
     * @return 作为 Java 编程语言中的 <code>Array</code> 对象的参数值。如果值为 SQL <code>NULL</code>，
     *         则返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    Array getArray (String parameterName) throws SQLException;

    /**
     * 使用给定的 <code>Calendar</code> 对象构造日期，获取 JDBC <code>DATE</code> 参数的值，
     * 作为 <code>java.sql.Date</code> 对象。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区和区域设置来计算日期。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区和区域设置。
     *
     * @param parameterName 参数的名称
     * @param cal 驱动程序将用于构造日期的 <code>Calendar</code> 对象
     * @return 参数值。如果值为 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setDate
     * @since 1.4
     */
    java.sql.Date getDate(String parameterName, Calendar cal)
        throws SQLException;

    /**
     * 使用给定的 <code>Calendar</code> 对象构造时间，获取 JDBC <code>TIME</code> 参数的值，
     * 作为 <code>java.sql.Time</code> 对象。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区和区域设置来计算时间。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区和区域设置。
     *
     * @param parameterName 参数的名称
     * @param cal 驱动程序将用于构造时间的 <code>Calendar</code> 对象
     * @return 参数值；如果值为 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setTime
     * @since 1.4
     */
    java.sql.Time getTime(String parameterName, Calendar cal)
        throws SQLException;

    /**
     * 使用给定的 <code>Calendar</code> 对象构造 <code>Timestamp</code> 对象，获取 JDBC <code>TIMESTAMP</code> 参数的值，
     * 作为 <code>java.sql.Timestamp</code> 对象。
     * 使用 <code>Calendar</code> 对象，驱动程序可以考虑自定义时区和区域设置来计算时间戳。
     * 如果没有指定 <code>Calendar</code> 对象，驱动程序将使用默认时区和区域设置。
     *
     *
     * @param parameterName 参数的名称
     * @param cal 驱动程序将用于构造时间戳的 <code>Calendar</code> 对象
     * @return 参数值。如果值为 SQL <code>NULL</code>，则结果为 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setTimestamp
     * @since 1.4
     */
    java.sql.Timestamp getTimestamp(String parameterName, Calendar cal)
        throws SQLException;

    /**
     * 获取 JDBC <code>DATALINK</code> 参数的值，作为 Java 编程语言中的
     * <code>java.net.URL</code> 对象。
     *
     * @param parameterName 参数的名称
     * @return 作为 Java 编程语言中的 <code>java.net.URL</code> 对象的参数值。如果值为 SQL <code>NULL</code>，
     *         则返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误，
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法，或 URL 有问题
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #setURL
     * @since 1.4
     */
    java.net.URL getURL(String parameterName) throws SQLException;

    //------------------------- JDBC 4.0 -----------------------------------

                /**
     * 获取指定的 JDBC <code>ROWID</code> 参数的值，作为 <code>java.sql.RowId</code> 对象。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2,...
     * @return 一个表示 JDBC <code>ROWID</code> 值的 <code>RowId</code> 对象，用作指定参数。如果参数包含
     * SQL <code>NULL</code>，则返回 <code>null</code> 值。
     * @throws SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId getRowId(int parameterIndex) throws SQLException;

    /**
     * 获取指定的 JDBC <code>ROWID</code> 参数的值，作为 <code>java.sql.RowId</code> 对象。
     *
     * @param parameterName 参数的名称
     * @return 一个表示 JDBC <code>ROWID</code> 值的 <code>RowId</code> 对象，用作指定参数。如果参数包含
     * SQL <code>NULL</code>，则返回 <code>null</code> 值。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId getRowId(String parameterName) throws SQLException;

     /**
     * 将指定参数设置为给定的 <code>java.sql.RowId</code> 对象。驱动程序在发送到数据库时将其转换为 SQL <code>ROWID</code>。
     *
     * @param parameterName 参数的名称
     * @param x 参数值
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setRowId(String parameterName, RowId x) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>String</code> 对象。驱动程序将其转换为 SQL <code>NCHAR</code> 或
     * <code>NVARCHAR</code> 或 <code>LONGNVARCHAR</code>
     * @param parameterName 要设置的参数的名称
     * @param value 参数值
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setNString(String parameterName, String value)
            throws SQLException;

    /**
     * 将指定参数设置为 <code>Reader</code> 对象。该 <code>Reader</code> 读取数据直到文件结束。驱动程序执行必要的转换，将 Java 字符格式转换为数据库中的国家字符集。
     * @param parameterName 要设置的参数的名称
     * @param value 参数值
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setNCharacterStream(String parameterName, Reader value, long length)
            throws SQLException;

     /**
     * 将指定参数设置为 <code>java.sql.NClob</code> 对象。该对象实现了 <code>java.sql.NClob</code> 接口。此 <code>NClob</code>
     * 对象映射到 SQL <code>NCLOB</code>。
     * @param parameterName 要设置的参数的名称
     * @param value 参数值
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNClob(String parameterName, NClob value) throws SQLException;

    /**
     * 将指定参数设置为 <code>Reader</code> 对象。该 <code>reader</code> 必须包含由长度指定的字符数，否则在执行 <code>CallableStatement</code> 时将生成 <code>SQLException</code>。
     * 该方法与 <code>setCharacterStream (int, Reader, int)</code> 方法不同，因为它通知驱动程序参数值应作为 <code>CLOB</code> 发送到服务器。当使用 <code>setCharacterStream</code> 方法时，驱动程序可能需要做额外的工作来确定参数数据应作为 <code>LONGVARCHAR</code> 还是 <code>CLOB</code> 发送到服务器。
     * @param parameterName 要设置的参数的名称
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果指定的长度小于零；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setClob(String parameterName, Reader reader, long length)
       throws SQLException;

                /**
     * 将指定参数设置为 <code>InputStream</code> 对象。<code>inputstream</code> 必须包含由长度指定的字符数，
     * 否则当执行 <code>CallableStatement</code> 时将生成 <code>SQLException</code>。
     * 该方法与 <code>setBinaryStream (int, InputStream, int)</code> 方法不同，因为它通知驱动程序参数值应
     * 作为 <code>BLOB</code> 发送到服务器。当使用 <code>setBinaryStream</code> 方法时，
     * 驱动程序可能需要做额外的工作来确定参数数据应作为 <code>LONGVARBINARY</code> 还是 <code>BLOB</code> 发送到服务器。
     *
     * @param parameterName 要设置的参数的名称
     * the second is 2, ...
     *
     * @param inputStream 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字节数。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果指定的长度
     * 小于零；如果输入流中的字节数与指定长度不匹配；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setBlob(String parameterName, InputStream inputStream, long length)
        throws SQLException;
    /**
     * 将指定参数设置为 <code>Reader</code> 对象。<code>reader</code> 必须包含由长度指定的字符数，
     * 否则当执行 <code>CallableStatement</code> 时将生成 <code>SQLException</code>。
     * 该方法与 <code>setCharacterStream (int, Reader, int)</code> 方法不同，因为它通知驱动程序参数值应
     * 作为 <code>NCLOB</code> 发送到服务器。当使用 <code>setCharacterStream</code> 方法时，
     * 驱动程序可能需要做额外的工作来确定参数数据应作为 <code>LONGNVARCHAR</code> 还是 <code>NCLOB</code> 发送到服务器。
     *
     * @param parameterName 要设置的参数的名称
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果指定的长度小于零；
     * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNClob(String parameterName, Reader reader, long length)
       throws SQLException;

    /**
     * 以 Java 编程语言中的 <code>java.sql.NClob</code> 对象形式检索指定 JDBC <code>NCLOB</code> 参数的值。
     *
     * @param parameterIndex 第一个参数是 1，第二个是 2，依此类推
     * @return 以 Java 编程语言中的 <code>NClob</code> 对象形式的参数值。如果值为 SQL <code>NULL</code>，则返回
     * <code>null</code>。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    NClob getNClob (int parameterIndex) throws SQLException;


    /**
     * 以 Java 编程语言中的 <code>java.sql.NClob</code> 对象形式检索 JDBC <code>NCLOB</code> 参数的值。
     * @param parameterName 参数的名称
     * @return 以 Java 编程语言中的 <code>NClob</code> 对象形式的参数值。如果值为 SQL <code>NULL</code>，
     * 则返回 <code>null</code>。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    NClob getNClob (String parameterName) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.SQLXML</code> 对象。驱动程序在将其发送到数据库时将其转换为
     * <code>SQL XML</code> 值。
     *
     * @param parameterName 参数的名称
     * @param xmlObject 映射 <code>SQL XML</code> 值的 <code>SQLXML</code> 对象
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误；
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法或
     * <code>java.xml.transform.Result</code>、<code>Writer</code> 或 <code>OutputStream</code> 尚未关闭
     * 对于 <code>SQLXML</code> 对象
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
    void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException;

    /**
     * 以 Java 编程语言中的 <code>java.sql.SQLXML</code> 对象形式检索指定 <code>SQL XML</code> 参数的值。
     * @param parameterIndex 第一个参数的索引是 1，第二个是 2，依此类推
     * @return 映射 <code>SQL XML</code> 值的 <code>SQLXML</code> 对象
     * @throws SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    SQLXML getSQLXML(int parameterIndex) throws SQLException;

                /**
     * 获取指定的 <code>SQL XML</code> 参数的值，作为 Java 编程语言中的
     * <code>java.sql.SQLXML</code> 对象。
     * @param parameterName 参数的名称
     * @return 一个映射 <code>SQL XML</code> 值的 <code>SQLXML</code> 对象
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    SQLXML getSQLXML(String parameterName) throws SQLException;

    /**
     * 获取指定的 <code>NCHAR</code>、
     * <code>NVARCHAR</code>
     * 或 <code>LONGNVARCHAR</code> 参数的值，
     * 作为 Java 编程语言中的 <code>String</code>。
     *  <p>
     * 对于固定长度的 JDBC <code>NCHAR</code> 类型，
     * 返回的 <code>String</code> 对象
     * 与数据库中的 SQL
     * <code>NCHAR</code> 值完全相同，包括数据库添加的任何填充。
     *
     * @param parameterIndex 第一个参数的索引为 1，第二个为 2，...
     * @return 一个映射 <code>NCHAR</code>、<code>NVARCHAR</code> 或 <code>LONGNVARCHAR</code> 值的
     * <code>String</code> 对象
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     * @see #setNString
     */
    String getNString(int parameterIndex) throws SQLException;


    /**
     * 获取指定的 <code>NCHAR</code>、
     * <code>NVARCHAR</code>
     * 或 <code>LONGNVARCHAR</code> 参数的值，
     * 作为 Java 编程语言中的 <code>String</code>。
     * <p>
     * 对于固定长度的 JDBC <code>NCHAR</code> 类型，
     * 返回的 <code>String</code> 对象
     * 与数据库中的 SQL
     * <code>NCHAR</code> 值完全相同，包括数据库添加的任何填充。
     *
     * @param parameterName 参数的名称
     * @return 一个映射 <code>NCHAR</code>、<code>NVARCHAR</code> 或 <code>LONGNVARCHAR</code> 值的
     * <code>String</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     * @see #setNString
     */
    String getNString(String parameterName) throws SQLException;

    /**
     * 获取指定参数的值，作为 Java 编程语言中的
     * <code>java.io.Reader</code> 对象。
     * 它旨在用于访问 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 参数。
     *
     * @return 一个包含参数值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * Java 编程语言中的 <code>null</code>。
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    java.io.Reader getNCharacterStream(int parameterIndex) throws SQLException;

    /**
     * 获取指定参数的值，作为 Java 编程语言中的
     * <code>java.io.Reader</code> 对象。
     * 它旨在用于访问 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 参数。
     *
     * @param parameterName 参数的名称
     * @return 一个包含参数值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * Java 编程语言中的 <code>null</code>
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    java.io.Reader getNCharacterStream(String parameterName) throws SQLException;

    /**
     * 获取指定参数的值，作为 Java 编程语言中的
     * <code>java.io.Reader</code> 对象。
     *
     * @return 一个包含参数值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * Java 编程语言中的 <code>null</code>。
     * @param parameterIndex 第一个参数为 1，第二个为 2，...
     * @exception SQLException 如果 parameterIndex 无效；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @since 1.6
     */
    java.io.Reader getCharacterStream(int parameterIndex) throws SQLException;

    /**
     * 获取指定参数的值，作为 Java 编程语言中的
     * <code>java.io.Reader</code> 对象。
     *
     * @param parameterName 参数的名称
     * @return 一个包含参数值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * Java 编程语言中的 <code>null</code>
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    java.io.Reader getCharacterStream(String parameterName) throws SQLException;

                /**
     * 将指定参数设置为给定的 <code>java.sql.Blob</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>BLOB</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 映射 SQL <code>BLOB</code> 值的 <code>Blob</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setBlob (String parameterName, Blob x) throws SQLException;

    /**
     * 将指定参数设置为给定的 <code>java.sql.Clob</code> 对象。
     * 驱动程序在将其发送到数据库时将其转换为 SQL <code>CLOB</code> 值。
     *
     * @param parameterName 参数的名称
     * @param x 映射 SQL <code>CLOB</code> 值的 <code>Clob</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setClob (String parameterName, Clob x) throws SQLException;
    /**
     * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
     * 当非常大的 ASCII 值输入到 <code>LONGVARCHAR</code> 参数时，通过
     * <code>java.io.InputStream</code> 发送可能更为实际。数据将从流中按需读取，直到文件结束。
     * JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterName 参数的名称
     * @param x 包含 ASCII 参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setAsciiStream(String parameterName, java.io.InputStream x, long length)
        throws SQLException;

    /**
     * 将指定参数设置为给定的输入流，该流将具有指定的字节数。
     * 当非常大的二进制值输入到 <code>LONGVARBINARY</code> 参数时，通过
     * <code>java.io.InputStream</code> 对象发送可能更为实际。数据将从流中按需读取，直到文件结束。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterName 参数的名称
     * @param x 包含二进制参数值的 Java 输入流
     * @param length 流中的字节数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setBinaryStream(String parameterName, java.io.InputStream x,
                         long length) throws SQLException;
        /**
     * 将指定参数设置为给定的 <code>Reader</code> 对象，该对象具有给定的字符数。
     * 当非常大的 UNICODE 值输入到 <code>LONGVARCHAR</code> 参数时，通过
     * <code>java.io.Reader</code> 对象发送可能更为实际。数据将从流中按需读取，直到文件结束。
     * JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     *
     * @param parameterName 参数的名称
     * @param reader 包含用于指定参数的 UNICODE 数据的 <code>java.io.Reader</code> 对象
     * @param length 流中的字符数
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setCharacterStream(String parameterName,
                            java.io.Reader reader,
                            long length) throws SQLException;
     //--
    /**
     * 将指定参数设置为给定的输入流。
     * 当非常大的 ASCII 值输入到 <code>LONGVARCHAR</code> 参数时，通过
     * <code>java.io.InputStream</code> 发送可能更为实际。数据将从流中按需读取，直到文件结束。
     * JDBC 驱动程序将执行从 ASCII 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     * <P><B>注意：</B>查阅您的 JDBC 驱动程序文档，以确定是否使用带有长度参数的
     * <code>setAsciiStream</code> 版本可能更有效。
     *
     * @param parameterName 参数的名称
     * @param x 包含 ASCII 参数值的 Java 输入流
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
    */
    void setAsciiStream(String parameterName, java.io.InputStream x)
            throws SQLException;
    /**
     * 将指定参数设置为给定的输入流。
     * 当非常大的二进制值输入到 <code>LONGVARBINARY</code> 参数时，通过
     * <code>java.io.InputStream</code> 对象发送可能更为实际。数据将从流中按需读取，直到文件结束。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     * <P><B>注意：</B>查阅您的 JDBC 驱动程序文档，以确定是否使用带有长度参数的
     * <code>setBinaryStream</code> 版本可能更有效。
     *
     * @param parameterName 参数的名称
     * @param x 包含二进制参数值的 Java 输入流
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setBinaryStream(String parameterName, java.io.InputStream x)
    throws SQLException;
    /**
     * 将指定参数设置为给定的 <code>Reader</code> 对象。
     * 当非常大的 UNICODE 值输入到 <code>LONGVARCHAR</code> 参数时，通过
     * <code>java.io.Reader</code> 对象发送可能更为实际。数据将从流中按需读取，直到文件结束。
     * JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <P><B>注意：</B>此流对象可以是标准的 Java 流对象，也可以是实现标准接口的您自己的子类。
     * <P><B>注意：</B>查阅您的 JDBC 驱动程序文档，以确定是否使用带有长度参数的
     * <code>setCharacterStream</code> 版本可能更有效。
     *
     * @param parameterName 参数的名称
     * @param reader 包含 Unicode 数据的 <code>java.io.Reader</code> 对象
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void setCharacterStream(String parameterName,
                          java.io.Reader reader) throws SQLException;
  /**
     * 将指定参数设置为 <code>Reader</code> 对象。该 <code>Reader</code> 读取数据直到文件结束。
     * 驱动程序执行从 Java 字符格式到数据库国家字符集的必要转换。
     */


                 * <P><B>注意:</B> 该流对象可以是标准的
     * Java 流对象，也可以是实现标准接口的自定义子类。
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
     * <code>setNCharacterStream</code> 版本是否更高效。
     *
     * @param parameterName 参数的名称
     * @param value 参数值
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；如果发生数据库访问错误；或者在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setNCharacterStream(String parameterName, Reader value) throws SQLException;

    /**
     * 将指定的参数设置为 <code>Reader</code> 对象。
     * 该方法与 <code>setCharacterStream (int, Reader)</code> 方法不同，
     * 因为它通知驱动程序参数值应作为 <code>CLOB</code> 发送到服务器。当使用 <code>setCharacterStream</code> 方法时，
     * 驱动程序可能需要额外的工作来确定参数数据应作为 <code>LONGVARCHAR</code> 还是 <code>CLOB</code> 发送到服务器。
     *
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
     * <code>setClob</code> 版本是否更高效。
     *
     * @param parameterName 参数的名称
     * @param reader 包含要设置为参数值的数据的对象。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或在已关闭的 <code>CallableStatement</code> 上调用此方法
     *
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
     void setClob(String parameterName, Reader reader)
       throws SQLException;

    /**
     * 将指定的参数设置为 <code>InputStream</code> 对象。
     * 该方法与 <code>setBinaryStream (int, InputStream)</code> 方法不同，
     * 因为它通知驱动程序参数值应作为 <code>BLOB</code> 发送到服务器。当使用 <code>setBinaryStream</code> 方法时，
     * 驱动程序可能需要额外的工作来确定参数数据应作为 <code>LONGVARBINARY</code> 还是 <code>BLOB</code> 发送到服务器。
     *
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
     * <code>setBlob</code> 版本是否更高效。
     *
     * @param parameterName 参数的名称
     * @param inputStream 包含要设置为参数值的数据的对象。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setBlob(String parameterName, InputStream inputStream)
        throws SQLException;
    /**
     * 将指定的参数设置为 <code>Reader</code> 对象。
     * 该方法与 <code>setCharacterStream (int, Reader)</code> 方法不同，
     * 因为它通知驱动程序参数值应作为 <code>NCLOB</code> 发送到服务器。当使用 <code>setCharacterStream</code> 方法时，
     * 驱动程序可能需要额外的工作来确定参数数据应作为 <code>LONGNVARCHAR</code> 还是 <code>NCLOB</code> 发送到服务器。
     * <P><B>注意:</B> 请参阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
     * <code>setNClob</code> 版本是否更高效。
     *
     * @param parameterName 参数的名称
     * @param reader 包含要设置为参数值的数据的对象。
     * @throws SQLException 如果 parameterName 不对应于命名参数；如果驱动程序不支持国家字符集；
     * 如果驱动程序可以检测到可能发生数据转换
     *  错误；如果发生数据库访问错误或
     * 在已关闭的 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.6
     */
     void setNClob(String parameterName, Reader reader)
       throws SQLException;

    //------------------------- JDBC 4.1 -----------------------------------

    /**
     *<p>返回一个表示 OUT 参数 {@code parameterIndex} 值的对象，
     * 并将参数的 SQL 类型转换为请求的 Java 数据类型，如果支持该转换。如果转换不支持或类型为 null，
     * 则抛出 <code>SQLException</code>。
     *<p>
     * 至少，实现必须支持附录 B 表 B-3 中定义的转换，以及将适当的用户定义的 SQL 类型转换为实现 {@code SQLData} 或 {@code Struct} 的 Java 类型。
     * 可能支持其他转换，并由供应商定义。
     *
     * @param parameterIndex 第一个参数为 1，第二个参数为 2，依此类推
     * @param type 表示要将指定参数转换为的 Java 数据类型的类。
     * @param <T> 由此类对象建模的类型的类型
     * @return 包含 OUT 参数值的 {@code type} 实例
     * @throws SQLException 如果转换不支持，类型为 null 或发生其他错误。异常的 getCause() 方法可能提供更详细的异常，例如，如果发生转换错误
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.7
     */
     public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException;

    /**
     *<p>返回表示 OUT 参数 {@code parameterName} 值的对象，并将
     * SQL 类型的参数转换为请求的 Java 数据类型，如果支持转换的话。如果转换不
     * 支持或指定的类型为 null，则抛出 <code>SQLException</code>。
     *<p>
     * 至少，实现必须支持附录 B，表 B-3 中定义的转换，以及将适当的用户定义 SQL
     * 类型转换为实现 {@code SQLData} 或 {@code Struct} 的 Java 类型。
     * 可能支持额外的转换，这些转换由供应商定义。
     *
     * @param parameterName 参数的名称
     * @param type 表示要将指定参数转换为的 Java 数据类型的类
     * @param <T> 由此类对象建模的类的类型
     * @return 包含 OUT 参数值的 {@code type} 实例
     * @throws SQLException 如果转换不支持，类型为 null 或
     *         发生其他错误。异常的 getCause() 方法可能提供更详细的异常，例如，
     * 如果发生转换错误
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.7
     */
     public <T> T getObject(String parameterName, Class<T> type) throws SQLException;

     //------------------------- JDBC 4.2 -----------------------------------

     /**
     * <p>使用给定对象设置指定参数的值。
     *
     * 如果第二个参数是 {@code InputStream}，则流必须包含由 scaleOrLength
     * 指定的字节数。如果第二个参数是 {@code Reader}，则读取器必须包含由
     * scaleOrLength 指定的字符数。如果不满足这些条件，驱动程序将在执行
     * 预编译语句时生成一个 {@code SQLException}。
     *
     * <p>给定的 Java 对象将在发送到数据库之前转换为给定的 targetSqlType。
     *
     * 如果对象具有自定义映射（实现 {@code SQLData} 接口的类），
     * JDBC 驱动程序应调用方法 {@code SQLData.writeSQL} 将其写入 SQL 数据流。
     * 另一方面，如果对象是实现
     * {@code Ref}、{@code Blob}、{@code Clob}、{@code NClob}、
     *  {@code Struct}、{@code java.net.URL} 或
     * {@code Array} 的类，驱动程序应将其作为相应 SQL 类型的值传递给数据库。
     *
     * <p>请注意，此方法可用于传递特定于数据库的抽象数据类型。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterName 参数的名称
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型。scale 参数可以进一步限定此类型。
     * @param scaleOrLength 对于 {@code java.sql.JDBCType.DECIMAL}
     *          或 {@code java.sql.JDBCType.NUMERIC 类型}，
     *          这是小数点后的位数。对于 Java 对象类型 {@code InputStream} 和 {@code Reader}，
     *          这是流或读取器中的数据长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误
     * 或此方法在已关闭的 {@code CallableStatement} 上调用，或者
     *            如果 x 指定的 Java 对象是 InputStream
     *            或 Reader 对象且 scale 参数的值小于零
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     *
     * @since 1.8
     */
     default void setObject(String parameterName, Object x, SQLType targetSqlType,
             int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("setObject not implemented");
    }
    /**
     * 使用给定对象设置指定参数的值。
     *
     * 此方法类似于 {@link #setObject(String parameterName,
     * Object x, SQLType targetSqlType, int scaleOrLength)}，
     * 但假设比例为零。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterName 参数的名称
     * @param x 包含输入参数值的对象
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误
     * 或此方法在已关闭的 {@code CallableStatement} 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
     default void setObject(String parameterName, Object x, SQLType targetSqlType)
        throws SQLException {
        throw new SQLFeatureNotSupportedException("setObject not implemented");
    }

    /**
     * 将 OUT 参数注册到 JDBC 类型 {@code sqlType} 的序号位置
     * {@code parameterIndex}。在执行存储过程之前，必须注册所有 OUT 参数。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型决定了在 {@code get} 方法中必须使用的 Java 类型
     * 以读取该参数的值。
     * <p>
     * 如果预期返回到此输出参数的 JDBC 类型是特定于此数据库的，{@code sqlType}
     * 可能是 {@code JDBCType.OTHER} 或由 JDBC 驱动程序支持的 {@code SQLType}。方法
     * {@link #getObject} 用于检索值。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterIndex 第一个参数为 1，第二个参数为 2，
     *        以此类推
     * @param sqlType 由 {@code SQLType} 定义的用于注册 OUT 参数的 JDBC 类型代码。
     *        如果参数的 JDBC 类型为 {@code JDBCType.NUMERIC}
     *        或 {@code JDBCType.DECIMAL}，应使用接受比例值的
     *        {@code registerOutParameter} 版本。
     *
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 此方法在已关闭的 {@code CallableStatement} 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void registerOutParameter(int parameterIndex, SQLType sqlType)
        throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }

                /**
     * 注册参数在序号位置
     * {@code parameterIndex} 为 JDBC 类型
     * {@code sqlType}。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型 {@code sqlType} 决定了必须在
     * {@code get} 方法中使用的 Java 类型以读取该参数的值。
     * <p>
     * 当参数的 JDBC 类型为 {@code JDBCType.NUMERIC}
     * 或 {@code JDBCType.DECIMAL} 时，应使用此版本的 {@code  registerOutParameter}。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2，
     * 以此类推
     * @param sqlType 由 {@code SQLType} 定义的用于注册 OUT 参数的 JDBC 类型代码。
     * @param scale 希望的小数点右侧的位数。必须大于或等于零。
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 {@code CallableStatement} 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void registerOutParameter(int parameterIndex, SQLType sqlType,
            int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }
    /**
     * 注册指定的输出参数。
     * 此方法的版本
     * {@code  registerOutParameter}
     * 应用于用户定义或 {@code REF} 输出参数。
     * 示例
     * 用户定义的类型包括：{@code STRUCT}, {@code DISTINCT},
     * {@code JAVA_OBJECT}，以及命名的数组类型。
     *<p>
     * 所有 OUT 参数必须在执行存储过程之前注册。
     * <p> 对于用户定义的参数，还应给出参数的完全限定的 SQL
     * 类型名称，而 {@code REF}
     * 参数需要给出引用类型的完全限定类型名称。不需要类型代码和类型名称信息的 JDBC 驱动程序可能会忽略这些信息。 为了可移植性，
     * 应用程序应始终为用户定义和 {@code REF} 参数提供这些值。
     *
     * 尽管此方法旨在用于用户定义和 {@code REF} 参数，
     * 但可以用于注册任何 JDBC 类型的参数。
     * 如果参数不是用户定义或 {@code REF} 类型，则
     * <i>typeName</i> 参数将被忽略。
     *
     * <P><B>注意：</B> 当读取输出参数的值时，您
     * 必须使用与参数注册的 SQL 类型对应的 Java 类型的 getter 方法。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterIndex 第一个参数为 1，第二个为 2,...
     * @param sqlType 由 {@code SQLType} 定义的用于注册 OUT 参数的 JDBC 类型代码。
     * @param typeName SQL 结构化类型的完全限定名称
     * @exception SQLException 如果 parameterIndex 无效；
     * 如果发生数据库访问错误或
     * 在已关闭的 {@code CallableStatement} 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void registerOutParameter (int parameterIndex, SQLType sqlType,
            String typeName) throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }

    /**
     * 注册名为
     * <code>parameterName</code> 的 OUT 参数为 JDBC 类型
     * {@code sqlType}。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型 {@code sqlType} 决定了必须在
     * {@code get} 方法中使用的 Java 类型以读取该参数的值。
     * <p>
     * 如果预期返回到此输出参数的 JDBC 类型特定于此特定数据库，{@code sqlType}
     * 应为 {@code JDBCType.OTHER} 或由 JDBC 驱动程序支持的 {@code SQLType}。方法
     * {@link #getObject} 用于检索值。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterName 参数的名称
     * @param sqlType 由 {@code SQLType} 定义的用于注册 OUT 参数的 JDBC 类型代码。
     * 如果参数的 JDBC 类型为 {@code JDBCType.NUMERIC}
     * 或 {@code JDBCType.DECIMAL}，应使用接受 scale 值的
     * {@code  registerOutParameter} 版本。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 {@code CallableStatement} 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * 或 JDBC 驱动程序不支持
     * 此方法
     * @since 1.8
     * @see JDBCType
     * @see SQLType
     */
    default void registerOutParameter(String parameterName, SQLType sqlType)
        throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }

    /**
     * 注册名为
     * <code>parameterName</code> 的参数为 JDBC 类型
     * {@code sqlType}。所有 OUT 参数必须在执行存储过程之前注册。
     * <p>
     * 为 OUT 参数指定的 JDBC 类型 {@code sqlType} 决定了必须在
     * {@code get} 方法中使用的 Java 类型以读取该参数的值。
     * <p>
     * 当参数的 JDBC 类型为 {@code JDBCType.NUMERIC}
     * 或 {@code JDBCType.DECIMAL} 时，应使用此版本的 {@code  registerOutParameter}。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterName 参数的名称
     * @param sqlType 由 {@code SQLType} 定义的用于注册 OUT 参数的 JDBC 类型代码。
     * @param scale 希望的小数点右侧的位数。必须大于或等于零。
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 在已关闭的 {@code CallableStatement} 上调用此方法
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * 或 JDBC 驱动程序不支持
     * 此方法
     * @since 1.8
     * @see JDBCType
     * @see SQLType
     */
    default void registerOutParameter(String parameterName, SQLType sqlType,
            int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }

                /**
     * 注册指定的输出参数。此版本的 {@code  registerOutParameter}
     * 方法应用于用户命名或 REF 输出参数。用户命名类型包括：STRUCT, DISTINCT, JAVA_OBJECT 和
     * 命名数组类型。
     *<p>
     * 所有 OUT 参数必须在执行存储过程之前注册。
     * </p>
     * 对于用户命名的参数，应提供参数的完全限定的 SQL 类型名称，而对于 REF 参数，则需要提供引用类型的完全限定类型名称。如果 JDBC 驱动程序不需要类型代码和类型名称信息，它可以忽略这些信息。然而，为了可移植性，应用程序应始终为用户命名和 REF 参数提供这些值。
     *
     * 尽管此方法旨在用于用户命名和 REF 参数，但它也可以用于注册任何 JDBC 类型的参数。如果参数没有用户命名或 REF 类型，则忽略 typeName 参数。
     *
     * <P><B>注意：</B>在读取输出参数的值时，必须使用其 Java 类型 XXX 与参数注册的 SQL 类型相对应的 {@code getXXX} 方法。
     *<P>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param parameterName 参数的名称
     * @param sqlType 由 {@code SQLType} 定义的 JDBC 类型代码，用于注册 OUT 参数。
     * @param typeName SQL 结构化类型的完全限定名称
     * @exception SQLException 如果 parameterName 不对应于命名参数；如果发生数据库访问错误或
     * 此方法在已关闭的 {@code CallableStatement} 上调用
     * @exception SQLFeatureNotSupportedException 如果
     * JDBC 驱动程序不支持指定的 sqlType
     * 或 JDBC 驱动程序不支持此方法
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void registerOutParameter (String parameterName, SQLType sqlType,
            String typeName) throws SQLException {
        throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
    }
}
