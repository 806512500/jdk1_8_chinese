
/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个可以用来获取 <code>ResultSet</code> 对象中列的类型和属性信息的对象。
 * 以下代码片段创建了 <code>ResultSet</code> 对象 rs，创建了 <code>ResultSetMetaData</code> 对象 rsmd，并使用 rsmd
 * 查找 rs 有多少列以及 rs 中的第一列是否可以在 <code>WHERE</code> 子句中使用。
 * <PRE>
 *
 *     ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 *     ResultSetMetaData rsmd = rs.getMetaData();
 *     int numberOfColumns = rsmd.getColumnCount();
 *     boolean b = rsmd.isSearchable(1);
 *
 * </PRE>
 */

public interface ResultSetMetaData extends Wrapper {

    /**
     * 返回此 <code>ResultSet</code> 对象中的列数。
     *
     * @return 列数
     * @exception SQLException 如果发生数据库访问错误
     */
    int getColumnCount() throws SQLException;

    /**
     * 指示指定的列是否自动编号。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 如果是则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isAutoIncrement(int column) throws SQLException;

    /**
     * 指示列的大小写是否重要。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 如果是则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isCaseSensitive(int column) throws SQLException;

    /**
     * 指示指定的列是否可以在 where 子句中使用。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 如果是则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isSearchable(int column) throws SQLException;

    /**
     * 指示指定的列是否为现金值。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 如果是则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isCurrency(int column) throws SQLException;

    /**
     * 指示指定列中的值是否可以为 null。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 给定列的可空性状态；为 <code>columnNoNulls</code>、
     *          <code>columnNullable</code> 或 <code>columnNullableUnknown</code> 之一
     * @exception SQLException 如果发生数据库访问错误
     */
    int isNullable(int column) throws SQLException;

    /**
     * 表示列不允许 <code>NULL</code> 值的常量。
     */
    int columnNoNulls = 0;

    /**
     * 表示列允许 <code>NULL</code> 值的常量。
     */
    int columnNullable = 1;

    /**
     * 表示列的值的可空性未知的常量。
     */
    int columnNullableUnknown = 2;

    /**
     * 指示指定列中的值是否为带符号数。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 如果是则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isSigned(int column) throws SQLException;

    /**
     * 指示指定列的正常最大宽度（以字符为单位）。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 指定列的宽度允许的最大字符数
     * @exception SQLException 如果发生数据库访问错误
     */
    int getColumnDisplaySize(int column) throws SQLException;

    /**
     * 获取指定列的建议标题，用于打印和显示。建议的标题通常由 SQL <code>AS</code> 子句指定。
     * 如果未指定 SQL <code>AS</code>，则从 <code>getColumnLabel</code> 返回的值将与 <code>getColumnName</code> 方法返回的值相同。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 建议的列标题
     * @exception SQLException 如果发生数据库访问错误
     */
    String getColumnLabel(int column) throws SQLException;

    /**
     * 获取指定列的名称。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 列名
     * @exception SQLException 如果发生数据库访问错误
     */
    String getColumnName(int column) throws SQLException;

    /**
     * 获取指定列的表的模式。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 模式名或不适用时为 ""
     * @exception SQLException 如果发生数据库访问错误
     */
    String getSchemaName(int column) throws SQLException;

    /**
     * 获取指定列的指定列大小。
     * 对于数值数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示形式的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，
     * 这是字节长度。对于列大小不适用的数据类型，返回 0。
     *
     * @param column 第一列是 1，第二列是 2，...
     * @return 精度
     * @exception SQLException 如果发生数据库访问错误
     */
    int getPrecision(int column) throws SQLException;

                /**
     * 获取指定列的小数点右侧的数字位数。
     * 对于不适用小数位的数据类型，返回0。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 小数位数
     * @exception SQLException 如果发生数据库访问错误
     */
    int getScale(int column) throws SQLException;

    /**
     * 获取指定列的表名。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 表名或如果不适用则返回""
     * @exception SQLException 如果发生数据库访问错误
     */
    String getTableName(int column) throws SQLException;

    /**
     * 获取指定列的表的目录名。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 指定列所在的表的目录名，如果不适用则返回""
     * @exception SQLException 如果发生数据库访问错误
     */
    String getCatalogName(int column) throws SQLException;

    /**
     * 检索指定列的SQL类型。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 来自java.sql.Types的SQL类型
     * @exception SQLException 如果发生数据库访问错误
     * @see Types
     */
    int getColumnType(int column) throws SQLException;

    /**
     * 检索指定列的数据库特定类型名称。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 数据库使用的类型名称。如果列类型是用户定义的类型，则返回完全限定的类型名称。
     * @exception SQLException 如果发生数据库访问错误
     */
    String getColumnTypeName(int column) throws SQLException;

    /**
     * 指示指定列是否肯定不可写。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 如果是则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isReadOnly(int column) throws SQLException;

    /**
     * 指示是否可以成功写入指定列。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 如果是则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isWritable(int column) throws SQLException;

    /**
     * 指示写入指定列是否肯定成功。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 如果是则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isDefinitelyWritable(int column) throws SQLException;

    //--------------------------JDBC 2.0-----------------------------------

    /**
     * <p>返回如果调用<code>ResultSet.getObject</code>方法来检索列值时生成的Java类的完全限定名称。
     * <code>ResultSet.getObject</code>可能返回此方法返回的类的子类。
     *
     * @param column 第一列是1，第二列是2，...
     * @return 用于<code>ResultSet.getObject</code>方法检索指定列值的Java编程语言中的类的完全限定名称。
     *         这是用于自定义映射的类名。
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    String getColumnClassName(int column) throws SQLException;
}
