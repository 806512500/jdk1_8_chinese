
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
 * 一个数据表，表示数据库结果集，通常通过执行查询数据库的语句生成。
 *
 * <P><code>ResultSet</code> 对象维护一个指向其当前数据行的游标。最初，游标位于第一行之前。
 * <code>next</code> 方法将游标移动到下一行，因为它在 <code>ResultSet</code> 对象中没有更多行时返回 <code>false</code>，
 * 因此可以在 <code>while</code> 循环中使用它来遍历结果集。
 * <P>
 * 默认的 <code>ResultSet</code> 对象是不可更新的，游标只能向前移动。因此，只能从第一行到最后一行遍历一次。
 * 但是，可以生成可滚动和/或可更新的 <code>ResultSet</code> 对象。以下代码片段中，<code>con</code>
 * 是一个有效的 <code>Connection</code> 对象，说明了如何生成一个可滚动且不受他人更新影响且可更新的结果集。请参阅 <code>ResultSet</code> 字段以获取其他选项。
 * <PRE>
 *
 *       Statement stmt = con.createStatement(
 *                                      ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                                      ResultSet.CONCUR_UPDATABLE);
 *       ResultSet rs = stmt.executeQuery("SELECT a, b FROM TABLE2");
 *       // rs 将是可滚动的，不会显示他人所做的更改，
 *       // 并且是可更新的
 *
 * </PRE>
 * <code>ResultSet</code> 接口提供了
 * <i>获取器</i> 方法（如 <code>getBoolean</code>、<code>getLong</code> 等）用于从当前行检索列值。
 * 可以使用列的索引号或列名来检索值。通常，使用列索引会更高效。列从 1 开始编号。
 * 为了最大限度地提高可移植性，结果集中的每一行的列应按从左到右的顺序读取，并且每个列只读取一次。
 *
 * <P>对于获取器方法，JDBC 驱动程序会尝试将底层数据转换为获取器方法中指定的 Java 类型，并返回一个合适的 Java 值。
 * JDBC 规范中有一个表格显示了可以由 <code>ResultSet</code> 获取器方法使用的 SQL 类型到 Java 类型的允许映射。
 *
 * <P>用作获取器方法输入的列名不区分大小写。当使用列名调用获取器方法且多个列具有相同的名称时，
 * 将返回第一个匹配列的值。列名选项
 * 旨在用于在生成结果集的 SQL 查询中使用列名的情况。
 * 对于在查询中未显式命名的列，最好使用列号。如果使用列名，程序员应确保它们唯一地引用
 * 打算的列，这可以通过 SQL <i>AS</i> 子句来保证。
 * <P>
 * 在 JDBC 2.0 API（Java&trade; 2 SDK，
 * 标准版，版本 1.2）中为该接口添加了一组更新器方法。关于获取器方法参数的注释也适用于更新器方法的参数。
 * <P>
 * 更新器方法可以以两种方式使用：
 * <ol>
 * <LI>更新当前行中的列值。在可滚动的
 *     <code>ResultSet</code> 对象中，游标可以向前和向后移动，移动到绝对位置，或相对于当前行的位置。
 *     以下代码片段更新 <code>ResultSet</code> 对象 <code>rs</code> 中第五行的 <code>NAME</code> 列，
 *     然后使用 <code>updateRow</code> 方法从 <code>rs</code> 所派生的数据源表中更新该行。
 * <PRE>
 *
 *       rs.absolute(5); // 将游标移动到 rs 的第五行
 *       rs.updateString("NAME", "AINSWORTH"); // 更新第五行的 <code>NAME</code> 列为 <code>AINSWORTH</code>
 *       rs.updateRow(); // 更新数据源中的行
 *
 * </PRE>
 * <LI>将列值插入到插入行中。可更新的
 *     <code>ResultSet</code> 对象有一个特殊的行与其关联，用作构建要插入的行的暂存区。
 *     以下代码片段将游标移动到插入行，构建一个三列的行，并使用 <code>insertRow</code> 方法将其插入到 <code>rs</code> 和数据源表中。
 * <PRE>
 *
 *       rs.moveToInsertRow(); // 将游标移动到插入行
 *       rs.updateString(1, "AINSWORTH"); // 更新插入行的第一列为 <code>AINSWORTH</code>
 *       rs.updateInt(2,35); // 更新第二列为 <code>35</code>
 *       rs.updateBoolean(3, true); // 更新第三列为 <code>true</code>
 *       rs.insertRow();
 *       rs.moveToCurrentRow();
 *
 * </PRE>
 * </ol>
 * <P><code>ResultSet</code> 对象在生成它的 <code>Statement</code> 对象关闭、重新执行或用于从多个结果序列中检索下一个结果时自动关闭。
 *
 * <P><code>ResultSet</code> 对象的列数、类型和属性由 <code>ResultSet.getMetaData</code> 方法返回的 <code>ResultSetMetaData</code> 对象提供。
 *
 * @see Statement#executeQuery
 * @see Statement#getResultSet
 * @see ResultSetMetaData
 */

public interface ResultSet extends Wrapper, AutoCloseable {

    /**
     * 将游标从当前位置向前移动一行。
     * <code>ResultSet</code> 游标最初位于第一行之前；首次调用 <code>next</code> 方法将第一行设为当前行；
 * 第二次调用将第二行设为当前行，依此类推。
     * <p>
     * 当 <code>next</code> 方法的调用返回 <code>false</code> 时，游标位于最后一行之后。任何
     * 需要当前行的 <code>ResultSet</code> 方法的调用都将导致抛出 <code>SQLException</code>。
 * 如果结果集类型为 <code>TYPE_FORWARD_ONLY</code>，则由供应商指定其 JDBC 驱动程序实现是在后续调用 <code>next</code> 时返回 <code>false</code> 还是抛出 <code>SQLException</code>。
     *
     * <P>如果当前行有一个打开的输入流，调用 <code>next</code> 方法将隐式关闭该流。<code>ResultSet</code> 对象的
 * 警告链在读取新行时将被清除。
     *
     * @return 如果新的当前行有效，则返回 <code>true</code>；如果没有更多行，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    boolean next() throws SQLException;

    /**
     * 释放此 <code>ResultSet</code> 对象的数据库和
     * JDBC 资源，而不是等待它自动关闭。
     *
     * <P><code>ResultSet</code> 对象的关闭<strong>不会</strong>关闭由 <code>ResultSet</code> 创建的 <code>Blob</code>、
     * <code>Clob</code> 或 <code>NClob</code> 对象。<code>Blob</code>、
     * <code>Clob</code> 或 <code>NClob</code> 对象在其创建的事务期间至少保持有效，除非调用了它们的 <code>free</code> 方法。
     * <p>
     * 当 <code>ResultSet</code> 关闭时，通过调用 <code>getMetaData</code>
     * 方法创建的任何 <code>ResultSetMetaData</code> 实例仍然可以访问。
     *
     * <P><B>注意：</B><code>ResultSet</code> 对象
     * 由生成它的 <code>Statement</code> 对象在以下情况下自动关闭：
     * 该 <code>Statement</code> 对象被关闭、重新执行或用于从多个结果序列中检索下一个结果。
     * <p>
     * 对已经关闭的 <code>ResultSet</code> 对象调用 <code>close</code> 方法是一个空操作。
     *
     *
     * @exception SQLException 如果发生数据库访问错误
     */
    void close() throws SQLException;

    /**
     * 报告最后读取的列是否具有 SQL <code>NULL</code> 值。
     * 注意，您必须首先调用其中一个 getter 方法
     * 读取列的值，然后调用
     * <code>wasNull</code> 方法来检查读取的值是否为
     * SQL <code>NULL</code>。
     *
     * @return 如果最后读取的列值为 SQL
     *         <code>NULL</code>，则返回 <code>true</code>，否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    boolean wasNull() throws SQLException;

    // 通过列索引访问结果的方法

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>String</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    String getString(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>boolean</code>。
     *
     * <P>如果指定的列的数据类型为 CHAR 或 VARCHAR
     * 并且包含 "0"，或者数据类型为 BIT、TINYINT、SMALLINT、INTEGER 或 BIGINT
     * 并且包含 0，则返回 <code>false</code>。如果指定的列的数据类型为 CHAR 或 VARCHAR
     * 并且包含 "1"，或者数据类型为 BIT、TINYINT、SMALLINT、INTEGER 或 BIGINT
     * 并且包含 1，则返回 <code>true</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>false</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    boolean getBoolean(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>byte</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    byte getByte(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>short</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    short getShort(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>int</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    int getInt(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>long</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     */
    long getLong(int columnIndex) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>float</code> 类型。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    float getFloat(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>double</code> 类型。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>0</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    double getDouble(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.BigDecimal</code> 类型。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param scale 小数点右边的位数
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @deprecated 使用 {@code getBigDecimal(int columnIndex)}
     *             或 {@code getBigDecimal(String columnLabel)}
     */
    @Deprecated
    BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>byte</code> 数组。
     * 数组中的字节表示由驱动程序返回的原始值。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    byte[] getBytes(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Date</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    java.sql.Date getDate(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Time</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    java.sql.Time getTime(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Timestamp</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 ASCII 字符的流。可以从流中分块读取值。此方法特别适合
     * 获取大的 <code>LONGVARCHAR</code> 值。JDBC 驱动程序将
     * 从数据库格式转换为 ASCII。
     *
     * <P><B>注意：</B> 必须在获取任何其他列的值之前读取返回流中的所有数据。下一次
     * 调用 getter 方法会隐式关闭流。此外，流在调用方法
     * <code>InputStream.available</code> 时可能返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 一个 Java 输入流，提供数据库列值
     * 作为一字节 ASCII 字符的流；
     * 如果值是 SQL <code>NULL</code>，则返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     */
    java.io.InputStream getAsciiStream(int columnIndex) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为两个字节的3字符流。第一个字节是高位字节；第二个字节是低位字节。
     *
     * 该值可以从流中分块读取。此方法特别适合
     * 获取大的 <code>LONGVARCHAR</code> 值。JDBC驱动程序将执行从数据库
     * 格式到Unicode的任何必要转换。
     *
     * <P><B>注意：</B> 在获取任何其他列的值之前，必须读取返回流中的所有数据。下一次
     * 调用获取器方法会隐式关闭流。此外，当调用方法
     * <code>InputStream.available</code> 时，流可能会返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @return 一个Java输入流，提供数据库列值
     *         作为两个字节的Unicode字符流；
     *         如果值是SQL <code>NULL</code>，则返回值是
     *         <code>null</code>
     *
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @deprecated 使用 <code>getCharacterStream</code> 代替
     *              <code>getUnicodeStream</code>
     */
    @Deprecated
    java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为未解释的字节流。该值可以从流中分块读取。此方法特别适合
     * 获取大的 <code>LONGVARBINARY</code> 值。
     *
     * <P><B>注意：</B> 在获取任何其他列的值之前，必须读取返回流中的所有数据。下一次
     * 调用获取器方法会隐式关闭流。此外，当调用方法
     * <code>InputStream.available</code> 时，流可能会返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @return 一个Java输入流，提供数据库列值
     *         作为未解释的字节流；
     *         如果值是SQL <code>NULL</code>，则返回值是
     *         <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    java.io.InputStream getBinaryStream(int columnIndex)
        throws SQLException;


    // 通过列标签访问结果的方法

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>String</code>。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回
     * 值为 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    String getString(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>boolean</code>。
     *
     * <P>如果指定列的数据类型为 CHAR 或 VARCHAR 且包含 "0"，或数据类型为 BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT
     * 且包含 0，则返回 <code>false</code>。如果指定列的数据类型为 CHAR 或 VARCHAR
     * 且包含 "1"，或数据类型为 BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT
     * 且包含 1，则返回 <code>true</code>。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回
     * 值为 <code>false</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    boolean getBoolean(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>byte</code>。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回
     * 值为 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    byte getByte(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>short</code>。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回
     * 值为 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    short getShort(String columnLabel) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>int</code> 类型。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    int getInt(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>long</code> 类型。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    long getLong(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>float</code> 类型。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    float getFloat(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>double</code> 类型。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>0</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    double getDouble(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.math.BigDecimal</code> 类型。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param scale 小数点右边的位数
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @deprecated 使用 {@code getBigDecimal(int columnIndex)}
     *             或 {@code getBigDecimal(String columnLabel)}
     */
    @Deprecated
    BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>byte</code> 数组。
     * 数组中的字节表示驱动程序返回的原始值。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    byte[] getBytes(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Date</code> 对象。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    java.sql.Date getDate(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Time</code> 对象。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；
     * 如果值是 SQL <code>NULL</code>，
     * 则返回的值是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在关闭的结果集上调用此方法
     */
    java.sql.Time getTime(String columnLabel) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 Java 编程语言中的 <code>java.sql.Timestamp</code> 对象。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在此方法上调用时结果集已关闭
     */
    java.sql.Timestamp getTimestamp(String columnLabel) throws SQLException;

    /**
     * 以 ASCII 字符流的形式获取此 <code>ResultSet</code> 对象当前行中指定列的值。
     * 值可以从流中分块读取。此方法特别适合于检索大的 <code>LONGVARCHAR</code> 值。
     * JDBC 驱动程序将执行从数据库格式到 ASCII 的任何必要转换。
     *
     * <P><B>注意：</B>在获取任何其他列的值之前，必须先读取返回流中的所有数据。
     * 下一次调用 getter 方法会隐式关闭流。此外，当调用 <code>available</code> 方法时，
     * 流可能返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 Java 输入流，提供数据库列值作为一字节 ASCII 字符的流。
     * 如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在此方法上调用时结果集已关闭
     */
    java.io.InputStream getAsciiStream(String columnLabel) throws SQLException;

    /**
     * 以两字节 Unicode 字符流的形式获取此 <code>ResultSet</code> 对象当前行中指定列的值。
     * 第一个字节是高位字节；第二个字节是低位字节。
     *
     * 值可以从流中分块读取。此方法特别适合于检索大的 <code>LONGVARCHAR</code> 值。
     * JDBC 技术启用的驱动程序将执行从数据库格式到 Unicode 的任何必要转换。
     *
     * <P><B>注意：</B>在获取任何其他列的值之前，必须先读取返回流中的所有数据。
     * 下一次调用 getter 方法会隐式关闭流。此外，当调用 <code>InputStream.available</code> 方法时，
     * 流可能返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 Java 输入流，提供数据库列值作为两字节 Unicode 字符的流。
     * 如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在此方法上调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @deprecated 使用 <code>getCharacterStream</code> 代替
     */
    @Deprecated
    java.io.InputStream getUnicodeStream(String columnLabel) throws SQLException;

    /**
     * 以未解释的 <code>byte</code> 流的形式获取此 <code>ResultSet</code> 对象当前行中指定列的值。
     * 值可以从流中分块读取。此方法特别适合于检索大的 <code>LONGVARBINARY</code> 值。
     *
     * <P><B>注意：</B>在获取任何其他列的值之前，必须先读取返回流中的所有数据。
     * 下一次调用 getter 方法会隐式关闭流。此外，当调用 <code>available</code> 方法时，
     * 流可能返回 <code>0</code>，无论是否有数据可用。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 Java 输入流，提供数据库列值作为未解释的字节流；
     * 如果值是 SQL <code>NULL</code>，则结果是 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在此方法上调用时结果集已关闭
     */
    java.io.InputStream getBinaryStream(String columnLabel)
        throws SQLException;


    // 高级功能：

    /**
     * 获取对这个 <code>ResultSet</code> 对象的调用报告的第一个警告。
     * 此方法返回的 <code>SQLWarning</code> 对象将链接此 <code>ResultSet</code> 对象上的后续警告。
     *
     * <P>每次读取新行时，警告链将自动清除。不得对此方法调用已关闭的 <code>ResultSet</code> 对象；
     * 这样做将导致抛出 <code>SQLException</code>。
     * <P>
     * <B>注意：</B>此警告链仅涵盖由 <code>ResultSet</code> 方法引起的警告。
     * 由 <code>Statement</code> 方法（如读取 OUT 参数）引起的任何警告将链接到 <code>Statement</code> 对象。
     *
     * @return 报告的第一个 <code>SQLWarning</code> 对象或
     *         如果没有警告则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误或在此方法上调用时结果集已关闭
     */
    SQLWarning getWarnings() throws SQLException;

                /**
     * 清除此 <code>ResultSet</code> 对象上报告的所有警告。
     * 调用此方法后，<code>getWarnings</code> 方法
     * 将返回 <code>null</code>，直到为此 <code>ResultSet</code> 对象报告新的警告。
     *
     * @exception SQLException 如果发生数据库访问错误或在此方法上调用已关闭的结果集
     */
    void clearWarnings() throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象使用的 SQL 游标名称。
     *
     * <P>在 SQL 中，通过命名的游标检索结果表。可以使用引用游标名称的定位更新/删除语句
     * 更新或删除结果集的当前行。为了确保游标具有支持更新的适当隔离级别，
     * 游标的 <code>SELECT</code> 语句应为 <code>SELECT FOR UPDATE</code> 形式。如果省略
     * <code>FOR UPDATE</code>，定位更新可能会失败。
     *
     * <P>JDBC API 通过提供 <code>ResultSet</code> 对象使用的 SQL 游标名称来支持此 SQL 功能。
     * <code>ResultSet</code> 对象的当前行也是此 SQL 游标的当前行。
     *
     * @return 此 <code>ResultSet</code> 对象游标的 SQL 名称
     * @exception SQLException 如果发生数据库访问错误或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     */
    String getCursorName() throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象的列的数量、类型和属性。
     *
     * @return 此 <code>ResultSet</code> 对象的列的描述
     * @exception SQLException 如果发生数据库访问错误或在此方法上调用已关闭的结果集
     */
    ResultSetMetaData getMetaData() throws SQLException;

    /**
     * <p>获取此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>Object</code>。
     *
     * <p>此方法将返回给定列的值作为 Java 对象。Java 对象的类型将是与列的 SQL 类型相对应的默认
     * Java 对象类型，遵循 JDBC 规范中指定的内置类型映射。如果值是 SQL <code>NULL</code>，
     * 驱动程序将返回 Java <code>null</code>。
     *
     * <p>此方法也可用于读取特定于数据库的抽象数据类型。
     *
     * 在 JDBC 2.0 API 中，方法 <code>getObject</code> 的行为已扩展以实例化
     * SQL 用户定义类型的数据。
     * <p>
     * 如果 <code>Connection.getTypeMap</code> 不抛出
     * <code>SQLFeatureNotSupportedException</code>，
     * 那么当列包含结构化或区分值时，此方法的行为就像调用：
     * <code>getObject(columnIndex, this.getStatement().getConnection().getTypeMap())</code> 一样。
     *
     * 如果 <code>Connection.getTypeMap</code> 抛出
     * <code>SQLFeatureNotSupportedException</code>，
     * 则不支持结构化值，区分值将映射到由 DISTINCT 类型的基础 SQL 类型确定的默认 Java 类。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 包含列值的 <code>java.lang.Object</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在此方法上调用已关闭的结果集
     */
    Object getObject(int columnIndex) throws SQLException;

    /**
     * <p>获取此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>Object</code>。
     *
     * <p>此方法将返回给定列的值作为 Java 对象。Java 对象的类型将是与列的 SQL 类型相对应的默认
     * Java 对象类型，遵循 JDBC 规范中指定的内置类型映射。如果值是 SQL <code>NULL</code>，
     * 驱动程序将返回 Java <code>null</code>。
     * <P>
     * 此方法也可用于读取特定于数据库的抽象数据类型。
     * <P>
     * 在 JDBC 2.0 API 中，方法 <code>getObject</code> 的行为已扩展以实例化
     * SQL 用户定义类型的数据。当列包含结构化或区分值时，此方法的行为就像调用：
     * <code>getObject(columnIndex, this.getStatement().getConnection().getTypeMap())</code> 一样。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 包含列值的 <code>java.lang.Object</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在此方法上调用已关闭的结果集
     */
    Object getObject(String columnLabel) throws SQLException;

    //----------------------------------------------------------------

    /**
     * 将给定的 <code>ResultSet</code> 列标签映射到其
     * <code>ResultSet</code> 列索引。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 给定列名称的列索引
     * @exception SQLException 如果 <code>ResultSet</code> 对象
     * 不包含名为 <code>columnLabel</code> 的列，发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     */
    int findColumn(String columnLabel) throws SQLException;

    //--------------------------JDBC 2.0-----------------------------------

    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * 获取当前行中指定列的值，作为
     * <code>java.io.Reader</code> 对象。
     * @return 包含列值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * <code>null</code>。
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    java.io.Reader getCharacterStream(int columnIndex) throws SQLException;

    /**
     * 获取当前行中指定列的值，作为
     * <code>java.io.Reader</code> 对象。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列标签。如果未指定 SQL AS 子句，则标签是列名
     * @return 包含列值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * <code>null</code>。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    java.io.Reader getCharacterStream(String columnLabel) throws SQLException;

    /**
     * 获取当前行中指定列的值，作为
     * <code>java.math.BigDecimal</code> 对象，具有完整精度。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值（完整精度）；
     * 如果值是 SQL <code>NULL</code>，则返回
     * <code>null</code>。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    BigDecimal getBigDecimal(int columnIndex) throws SQLException;

    /**
     * 获取当前行中指定列的值，作为
     * <code>java.math.BigDecimal</code> 对象，具有完整精度。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列标签。如果未指定 SQL AS 子句，则标签是列名
     * @return 列值（完整精度）；
     * 如果值是 SQL <code>NULL</code>，则返回
     * <code>null</code>。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @since 1.2
     *
     */
    BigDecimal getBigDecimal(String columnLabel) throws SQLException;

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * 获取光标是否在
     * 此 <code>ResultSet</code> 对象的第一行之前。
     * <p>
     * <strong>注意：</strong>对于结果集类型为 <code>TYPE_FORWARD_ONLY</code> 的 <code>ResultSet</code>，支持 <code>isBeforeFirst</code> 方法是可选的
     *
     * @return 如果光标在第一行之前，则为 <code>true</code>；
     * 如果光标在其他任何位置或结果集不包含任何行，则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean isBeforeFirst() throws SQLException;

    /**
     * 获取光标是否在
     * 此 <code>ResultSet</code> 对象的最后一行之后。
     * <p>
     * <strong>注意：</strong>对于结果集类型为 <code>TYPE_FORWARD_ONLY</code> 的 <code>ResultSet</code>，支持 <code>isAfterLast</code> 方法是可选的
     *
     * @return 如果光标在最后一行之后，则为 <code>true</code>；
     * 如果光标在其他任何位置或结果集不包含任何行，则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean isAfterLast() throws SQLException;

    /**
     * 获取光标是否在
     * 此 <code>ResultSet</code> 对象的第一行上。
     * <p>
     * <strong>注意：</strong>对于结果集类型为 <code>TYPE_FORWARD_ONLY</code> 的 <code>ResultSet</code>，支持 <code>isFirst</code> 方法是可选的
     *
     * @return 如果光标在第一行上，则为 <code>true</code>；
     * 否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean isFirst() throws SQLException;

    /**
     * 获取光标是否在
     * 此 <code>ResultSet</code> 对象的最后一行上。
     *  <strong>注意：</strong>调用 <code>isLast</code> 方法可能会很昂贵，
     * 因为 JDBC 驱动程序
     * 可能需要提前获取一行以确定
     * 当前行是否是结果集的最后一行。
     * <p>
     * <strong>注意：</strong>对于结果集类型为 <code>TYPE_FORWARD_ONLY</code> 的 <code>ResultSet</code>，支持 <code>isLast</code> 方法是可选的
     * @return 如果光标在最后一行上，则为 <code>true</code>；
     * 否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean isLast() throws SQLException;

                /**
     * 将光标移动到此 <code>ResultSet</code> 对象的开头，即第一行之前。如果结果集中没有行，此方法将不起作用。
     *
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void beforeFirst() throws SQLException;

    /**
     * 将光标移动到此 <code>ResultSet</code> 对象的末尾，即最后一行之后。如果结果集中没有行，此方法将不起作用。
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void afterLast() throws SQLException;

    /**
     * 将光标移动到此 <code>ResultSet</code> 对象的第一行。
     *
     * @return 如果光标位于有效行上，则返回 <code>true</code>；如果结果集中没有行，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean first() throws SQLException;

    /**
     * 将光标移动到此 <code>ResultSet</code> 对象的最后一行。
     *
     * @return 如果光标位于有效行上，则返回 <code>true</code>；如果结果集中没有行，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean last() throws SQLException;

    /**
     * 检索当前行号。第一行是1，第二行是2，依此类推。
     * <p>
     * <strong>注意：</strong>对于结果集类型为 <code>TYPE_FORWARD_ONLY</code> 的 <code>ResultSet</code>，支持 <code>getRow</code> 方法是可选的。
     *
     * @return 当前行号；如果没有当前行，则返回 <code>0</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    int getRow() throws SQLException;

    /**
     * 将光标移动到此 <code>ResultSet</code> 对象中的给定行号。
     *
     * <p>如果行号为正数，光标将移动到相对于结果集开头的给定行号。第一行是1，第二行是2，依此类推。
     *
     * <p>如果给定的行号为负数，光标将移动到相对于结果集末尾的绝对行位置。例如，调用方法 <code>absolute(-1)</code> 将光标定位在最后一行；调用方法 <code>absolute(-2)</code> 将光标移动到倒数第二行，依此类推。
     *
     * <p>如果指定的行号为零，光标将移动到第一行之前。
     *
     * <p>尝试将光标定位在结果集的第一行/最后一行之外，将使光标位于第一行之前或最后一行之后。
     *
     * <p><B>注意：</B>调用 <code>absolute(1)</code> 与调用 <code>first()</code> 相同。调用 <code>absolute(-1)</code> 与调用 <code>last()</code> 相同。
     *
     * @param row 光标应移动到的行号。零值表示光标将被定位在第一行之前；正数表示从结果集开头计数的行号；负数表示从结果集末尾计数的行号
     * @return 如果光标移动到此 <code>ResultSet</code> 对象中的位置，则返回 <code>true</code>；如果光标在第一行之前或最后一行之后，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean absolute( int row ) throws SQLException;

    /**
     * 将光标相对移动一定数量的行，可以是正数或负数。尝试移动到结果集的第一行/最后一行之外，将使光标位于第一行之前/最后一行之后。调用 <code>relative(0)</code> 是有效的，但不会改变光标位置。
     *
     * <p>注意：调用方法 <code>relative(1)</code> 与调用方法 <code>next()</code> 相同，调用方法 <code>relative(-1)</code> 与调用方法 <code>previous()</code> 相同。
     *
     * @param rows 一个 <code>int</code>，指定从当前行移动的行数；正数向前移动光标；负数向后移动光标
     * @return 如果光标位于行上，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean relative( int rows ) throws SQLException;

                /**
     * 将光标移动到此
     * <code>ResultSet</code> 对象的前一行。
     *<p>
     * 当调用 <code>previous</code> 方法返回 <code>false</code> 时，
     * 光标将位于第一行之前。任何需要当前行的
     * <code>ResultSet</code> 方法调用都将导致抛出
     * <code>SQLException</code>。
     *<p>
     * 如果当前行有一个打开的输入流，调用 <code>previous</code> 方法将隐式关闭它。
     * <code>ResultSet</code> 对象的警告更改在读取新行时被清除。
     *<p>
     *
     * @return 如果光标现在定位在一个有效的行上，则返回 <code>true</code>;
     * 如果光标定位在第一行之前，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误；
     * 此方法在已关闭的结果集上调用，或结果集类型为 <code>TYPE_FORWARD_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    boolean previous() throws SQLException;

    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------

    /**
     * 表示结果集中行将按正向处理的常量；从第一行到最后一行。
     * 此常量用作 <code>setFetchDirection</code> 方法的提示，
     * 驱动程序可以选择忽略。
     * @since 1.2
     */
    int FETCH_FORWARD = 1000;

    /**
     * 表示结果集中行将按反向处理的常量；从最后一行到第一行。
     * 此常量用作 <code>setFetchDirection</code> 方法的提示，
     * 驱动程序可以选择忽略。
     * @since 1.2
     */
    int FETCH_REVERSE = 1001;

    /**
     * 表示结果集中行的处理顺序未知的常量。
     * 此常量用作 <code>setFetchDirection</code> 方法的提示，
     * 驱动程序可以选择忽略。
     */
    int FETCH_UNKNOWN = 1002;

    /**
     * 为处理此
     * <code>ResultSet</code> 对象中的行提供方向提示。
     * 初始值由生成此 <code>ResultSet</code> 对象的
     * <code>Statement</code> 对象确定。
     * 可以随时更改提取方向。
     *
     * @param direction 一个 <code>int</code>，指定建议的提取方向；
     *        可以是 <code>ResultSet.FETCH_FORWARD</code>、
     *        <code>ResultSet.FETCH_REVERSE</code> 或
     *        <code>ResultSet.FETCH_UNKNOWN</code>
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用，或
     * 结果集类型为 <code>TYPE_FORWARD_ONLY</code> 且提取方向不是 <code>FETCH_FORWARD</code>
     * @since 1.2
     * @see Statement#setFetchDirection
     * @see #getFetchDirection
     */
    void setFetchDirection(int direction) throws SQLException;

    /**
     * 检索此
     * <code>ResultSet</code> 对象的提取方向。
     *
     * @return 此 <code>ResultSet</code> 对象的当前提取方向
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @since 1.2
     * @see #setFetchDirection
     */
    int getFetchDirection() throws SQLException;

    /**
     * 为 JDBC 驱动程序提供一个提示，指示当此
     * <code>ResultSet</code> 对象需要更多行时应从数据库中提取的行数。
     * 如果指定的提取大小为零，JDBC 驱动程序将忽略该值，并自由地做出最佳猜测。
     * 默认值由创建结果集的
     * <code>Statement</code> 对象设置。可以随时更改提取大小。
     *
     * @param rows 要提取的行数
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用，或
     * 条件 {@code rows >= 0} 不满足
     * @since 1.2
     * @see #getFetchSize
     */
    void setFetchSize(int rows) throws SQLException;

    /**
     * 检索此
     * <code>ResultSet</code> 对象的提取大小。
     *
     * @return 此 <code>ResultSet</code> 对象的当前提取大小
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @since 1.2
     * @see #setFetchSize
     */
    int getFetchSize() throws SQLException;

    /**
     * 表示 <code>ResultSet</code> 对象类型，其光标只能向前移动的常量。
     * @since 1.2
     */
    int TYPE_FORWARD_ONLY = 1003;

    /**
     * 表示 <code>ResultSet</code> 对象类型，该对象可滚动但通常对底层数据的更改不敏感。
     * @since 1.2
     */
    int TYPE_SCROLL_INSENSITIVE = 1004;

    /**
     * 表示 <code>ResultSet</code> 对象类型，该对象可滚动且通常对底层数据的更改敏感。
     * @since 1.2
     */
    int TYPE_SCROLL_SENSITIVE = 1005;

    /**
     * 检索此 <code>ResultSet</code> 对象的类型。
     * 类型由创建结果集的 <code>Statement</code> 对象确定。
     *
     * @return <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    int getType() throws SQLException;

                /**
     * 表示 <code>ResultSet</code> 对象不可更新的并发模式的常量。
     * @since 1.2
     */
    int CONCUR_READ_ONLY = 1007;

    /**
     * 表示 <code>ResultSet</code> 对象可更新的并发模式的常量。
     * @since 1.2
     */
    int CONCUR_UPDATABLE = 1008;

    /**
     * 检索此 <code>ResultSet</code> 对象的并发模式。
     * 使用的并发模式由创建结果集的 <code>Statement</code> 对象确定。
     *
     * @return 并发类型，可以是
     *         <code>ResultSet.CONCUR_READ_ONLY</code>
     *         或 <code>ResultSet.CONCUR_UPDATABLE</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @since 1.2
     */
    int getConcurrency() throws SQLException;

    //---------------------------------------------------------------------
    // 更新
    //---------------------------------------------------------------------

    /**
     * 检索当前行是否已被更新。返回的值取决于结果集是否能够检测到更新。
     * <p>
     * <strong>注意：</strong> 对于并发模式为 <code>CONCUR_READ_ONLY</code> 的结果集，支持 <code>rowUpdated</code> 方法是可选的
     * @return 如果检测到当前行已被所有者或其他人可见地更新，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see DatabaseMetaData#updatesAreDetected
     * @since 1.2
     */
    boolean rowUpdated() throws SQLException;

    /**
     * 检索当前行是否已插入。返回的值取决于此
     * <code>ResultSet</code> 对象是否能够检测到可见插入。
     * <p>
     * <strong>注意：</strong> 对于并发模式为 <code>CONCUR_READ_ONLY</code> 的结果集，支持 <code>rowInserted</code> 方法是可选的
     * @return 如果检测到当前行已被插入，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @see DatabaseMetaData#insertsAreDetected
     * @since 1.2
     */
    boolean rowInserted() throws SQLException;

    /**
     * 检索行是否已被删除。已删除的行可能在结果集中留下可见的“空洞”。此方法可用于
     * 检测结果集中的“空洞”。返回的值取决于此 <code>ResultSet</code> 对象是否能够检测到删除。
     * <p>
     * <strong>注意：</strong> 对于并发模式为 <code>CONCUR_READ_ONLY</code> 的结果集，支持 <code>rowDeleted</code> 方法是可选的
     * @return 如果检测到当前行已被所有者或其他人删除，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @see DatabaseMetaData#deletesAreDetected
     * @since 1.2
     */
    boolean rowDeleted() throws SQLException;

    /**
     * 使用 <code>null</code> 值更新指定的列。
     *
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code>
     * 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发模式为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateNull(int columnIndex) throws SQLException;

    /**
     * 使用 <code>boolean</code> 值更新指定的列。
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发模式为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBoolean(int columnIndex, boolean x) throws SQLException;

    /**
     * 使用 <code>byte</code> 值更新指定的列。
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发模式为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateByte(int columnIndex, byte x) throws SQLException;

                /**
     * 更新指定列的值为 <code>short</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateShort(int columnIndex, short x) throws SQLException;

    /**
     * 更新指定列的值为 <code>int</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateInt(int columnIndex, int x) throws SQLException;

    /**
     * 更新指定列的值为 <code>long</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateLong(int columnIndex, long x) throws SQLException;

    /**
     * 更新指定列的值为 <code>float</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateFloat(int columnIndex, float x) throws SQLException;

    /**
     * 更新指定列的值为 <code>double</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateDouble(int columnIndex, double x) throws SQLException;

    /**
     * 更新指定列的值为 <code>java.math.BigDecimal</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException;

    /**
     * 更新指定列的值为 <code>String</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 而是调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateString(int columnIndex, String x) throws SQLException;

                /**
     * 使用 <code>byte</code> 数组值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBytes(int columnIndex, byte x[]) throws SQLException;

    /**
     * 使用 <code>java.sql.Date</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateDate(int columnIndex, java.sql.Date x) throws SQLException;

    /**
     * 使用 <code>java.sql.Time</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateTime(int columnIndex, java.sql.Time x) throws SQLException;

    /**
     * 使用 <code>java.sql.Timestamp</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateTimestamp(int columnIndex, java.sql.Timestamp x)
      throws SQLException;

    /**
     * 使用指定字节数的 ascii 流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateAsciiStream(int columnIndex,
                           java.io.InputStream x,
                           int length) throws SQLException;

    /**
     * 使用指定字节数的二进制流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBinaryStream(int columnIndex,
                            java.io.InputStream x,
                            int length) throws SQLException;

    /**
     * 使用指定字节数的字符流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；
     * 相反，调用 <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateCharacterStream(int columnIndex,
                             java.io.Reader x,
                             int length) throws SQLException;

                /**
     * 更新指定列的值为 <code>Object</code> 类型。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *<p>
     * 如果第二个参数是 <code>InputStream</code>，则流必须包含由 scaleOrLength 指定的字节数。如果第二个参数是
     * <code>Reader</code>，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些条件不满足，驱动程序将在执行语句时生成
     * <code>SQLException</code>。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param scaleOrLength 对于 <code>java.math.BigDecimal</code> 类型的对象，
     *          这是小数点后的数字位数。对于 Java 对象类型 <code>InputStream</code> 和 <code>Reader</code>，
     *          这是流或读取器中数据的长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateObject(int columnIndex, Object x, int scaleOrLength)
      throws SQLException;

    /**
     * 更新指定列的值为 <code>Object</code> 类型。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateObject(int columnIndex, Object x) throws SQLException;

    /**
     * 更新指定列的值为 <code>null</code>。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateNull(String columnLabel) throws SQLException;

    /**
     * 更新指定列的值为 <code>boolean</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBoolean(String columnLabel, boolean x) throws SQLException;

    /**
     * 更新指定列的值为 <code>byte</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateByte(String columnLabel, byte x) throws SQLException;

    /**
     * 更新指定列的值为 <code>short</code> 类型。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateShort(String columnLabel, short x) throws SQLException;

                /**
     * 使用 <code>int</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateInt(String columnLabel, int x) throws SQLException;

    /**
     * 使用 <code>long</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateLong(String columnLabel, long x) throws SQLException;

    /**
     * 使用 <code>float</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateFloat(String columnLabel, float x) throws SQLException;

    /**
     * 使用 <code>double</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateDouble(String columnLabel, double x) throws SQLException;

    /**
     * 使用 <code>java.sql.BigDecimal</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException;

    /**
     * 使用 <code>String</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateString(String columnLabel, String x) throws SQLException;

    /**
     * 使用字节数组值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；相反，应调用 <code>updateRow</code>
     * 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateBytes(String columnLabel, byte x[]) throws SQLException;

                /**
     * 使用 <code>java.sql.Date</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateDate(String columnLabel, java.sql.Date x) throws SQLException;

    /**
     * 使用 <code>java.sql.Time</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateTime(String columnLabel, java.sql.Time x) throws SQLException;

    /**
     * 使用 <code>java.sql.Timestamp</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateTimestamp(String columnLabel, java.sql.Timestamp x)
      throws SQLException;

    /**
     * 使用指定字节数的 ASCII 流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateAsciiStream(String columnLabel,
                           java.io.InputStream x,
                           int length) throws SQLException;

    /**
     * 使用指定字节数的二进制流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateBinaryStream(String columnLabel,
                            java.io.InputStream x,
                            int length) throws SQLException;

    /**
     * 使用指定字节数的字符流值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param reader 包含新列值的 <code>java.io.Reader</code> 对象
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.2
     */
    void updateCharacterStream(String columnLabel,
                             java.io.Reader reader,
                             int length) throws SQLException;

                /**
     * 使用 <code>Object</code> 值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *<p>
     * 如果第二个参数是 <code>InputStream</code>，则流必须包含由 scaleOrLength 指定的字节数。如果第二个参数是
     * <code>Reader</code>，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些条件不满足，驱动程序将在执行语句时生成
     * <code>SQLException</code>。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @param scaleOrLength 对于 <code>java.math.BigDecimal</code> 类型的对象，
     *          这是小数点后的位数。对于 Java 对象类型 <code>InputStream</code> 和 <code>Reader</code>，
     *          这是流或读取器中的数据长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateObject(String columnLabel, Object x, int scaleOrLength)
      throws SQLException;

    /**
     * 使用 <code>Object</code> 值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateObject(String columnLabel, Object x) throws SQLException;

    /**
     * 将插入行的内容插入到此
     * <code>ResultSet</code> 对象和数据库中。调用此方法时，光标必须位于插入行。
     *
     * @exception SQLException 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>，
     * 在已关闭的结果集上调用此方法，
     * 当光标不在插入行时调用此方法，
     * 或者插入行中的非空列未被赋予非空值
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void insertRow() throws SQLException;

    /**
     * 使用此 <code>ResultSet</code> 对象当前行的新内容更新底层数据库。
     * 当光标位于插入行时，不能调用此方法。
     *
     * @exception SQLException 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>；
     * 在已关闭的结果集上调用此方法或
     * 当光标位于插入行时调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void updateRow() throws SQLException;

    /**
     * 从此 <code>ResultSet</code> 对象和底层数据库中删除当前行。当
     * 光标位于插入行时，不能调用此方法。
     *
     * @exception SQLException 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>；
     * 在已关闭的结果集上调用此方法
     * 或当光标位于插入行时调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    void deleteRow() throws SQLException;

    /**
     * 使用数据库中的最新值刷新当前行。当
     * 光标位于插入行时，不能调用此方法。
     *
     * <P> <code>refreshRow</code> 方法为应用程序提供了一种
     * 明确告诉 JDBC 驱动程序从数据库中重新获取行（或行）的方法。当 JDBC 驱动程序进行缓存或预取时，应用程序可能希望调用
     * <code>refreshRow</code> 以从数据库中获取最新值。如果获取大小大于一，JDBC 驱动程序实际上可能一次刷新多行。
     *
     * <P> 所有值都将根据事务隔离级别和光标敏感性进行刷新。如果在调用更新方法后，但在调用
     * <code>updateRow</code> 方法之前调用 <code>refreshRow</code>，则对行所做的更新将丢失。频繁调用
     * <code>refreshRow</code> 方法可能会降低性能。
     *
     * @exception SQLException 如果发生数据库访问错误；
     * 在已关闭的结果集上调用此方法；
     * 结果集类型为 <code>TYPE_FORWARD_ONLY</code> 或当
     * 光标位于插入行时调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果
     * 集类型和结果集并发性。
     * @since 1.2
     */
    void refreshRow() throws SQLException;

                /**
     * 取消对此 <code>ResultSet</code> 对象中当前行所做的更新。
     * 在调用更新方法后和调用 <code>updateRow</code> 方法之前，可以调用此方法来回滚对行的更新。
     * 如果没有进行更新或已调用 <code>updateRow</code>，此方法将不起作用。
     *
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code> 或在插入行上调用此方法
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    void cancelRowUpdates() throws SQLException;

    /**
     * 将光标移动到插入行。在光标位于插入行时，会记住当前的光标位置。
     *
     * 插入行是与可更新结果集关联的特殊行。它基本上是一个缓冲区，在此缓冲区中，可以通过调用更新方法构建新行，然后再将该行插入结果集中。
     *
     * 当光标位于插入行时，只能调用更新器、获取器和 <code>insertRow</code> 方法。
     * 每次调用此方法时，结果集中的所有列都必须赋予一个值，然后才能调用 <code>insertRow</code>。
     * 必须先调用更新器方法，然后才能对列值调用获取器方法。
     *
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用
     * 或结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    void moveToInsertRow() throws SQLException;

    /**
     * 将光标移动到记住的光标位置，通常是当前行。如果光标不在插入行上，此方法将不起作用。
     *
     * @exception SQLException 如果发生数据库访问错误；此方法在已关闭的结果集上调用
     * 或结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    void moveToCurrentRow() throws SQLException;

    /**
     * 检索生成此 <code>ResultSet</code> 对象的 <code>Statement</code> 对象。
     * 如果结果集是通过其他方式生成的，例如通过 <code>DatabaseMetaData</code> 方法，此方法可能返回 <code>null</code>。
     *
     * @return 生成此 <code>ResultSet</code> 对象的 <code>Statement</code> 对象，或如果结果集是通过其他方式生成的，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    Statement getStatement() throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Object</code>。
     * 如果值是 SQL <code>NULL</code>，驱动程序将返回 Java <code>null</code>。
     * 此方法使用给定的 <code>Map</code> 对象来映射正在检索的 SQL 结构化或区分类型。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param map 包含从 SQL 类型名称到 Java 编程语言类的映射的 <code>java.util.Map</code> 对象
     * @return 代表 SQL 值的 Java 编程语言中的 <code>Object</code>
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    Object getObject(int columnIndex, java.util.Map<String,Class<?>> map)
        throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Ref</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 代表 SQL <code>REF</code> 值的 <code>Ref</code> 对象
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    Ref getRef(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Blob</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 代表 SQL <code>BLOB</code> 值的 <code>Blob</code> 对象
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    Blob getBlob(int columnIndex) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Clob</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 代表 SQL <code>CLOB</code> 值的 <code>Clob</code> 对象
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.2
     */
    Clob getClob(int columnIndex) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Array</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 一个 <code>Array</code> 对象，表示指定列中的 SQL <code>ARRAY</code> 值
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Array getArray(int columnIndex) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Object</code>。
     * 如果值是 SQL <code>NULL</code>，则驱动程序返回 Java <code>null</code>。
     * 如果适当，此方法使用指定的 <code>Map</code> 对象进行自定义映射。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param map 包含从 SQL 类型名称到 Java 编程语言类的映射的 <code>java.util.Map</code> 对象
     * @return 一个 <code>Object</code>，表示指定列中的 SQL 值
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Object getObject(String columnLabel, java.util.Map<String,Class<?>> map)
      throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Ref</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 <code>Ref</code> 对象，表示指定列中的 SQL <code>REF</code> 值
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Ref getRef(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Blob</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 <code>Blob</code> 对象，表示指定列中的 SQL <code>BLOB</code> 值
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Blob getBlob(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Clob</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 <code>Clob</code> 对象，表示指定列中的 SQL <code>CLOB</code> 值
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Clob getClob(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>Array</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 一个 <code>Array</code> 对象，表示指定列中的 SQL <code>ARRAY</code> 值
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     */
    Array getArray(String columnLabel) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>java.sql.Date</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param cal 用于构造日期的 <code>java.util.Calendar</code> 对象
     * @return 一个 <code>java.sql.Date</code> 对象；如果值是 SQL <code>NULL</code>，
     * 则返回 Java 编程语言中的 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @since 1.2
     */
    java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.sql.Date</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param cal 用于构造日期的 <code>java.util.Calendar</code> 对象
     * @return 作为 <code>java.sql.Date</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     * @since 1.2
     */
    java.sql.Date getDate(String columnLabel, Calendar cal) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.sql.Time</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param cal 用于构造时间的 <code>java.util.Calendar</code> 对象
     * @return 作为 <code>java.sql.Time</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     * @since 1.2
     */
    java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.sql.Time</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param cal 用于构造时间的 <code>java.util.Calendar</code> 对象
     * @return 作为 <code>java.sql.Time</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     * @since 1.2
     */
    java.sql.Time getTime(String columnLabel, Calendar cal) throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.sql.Timestamp</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param cal 用于构造时间戳的 <code>java.util.Calendar</code> 对象
     * @return 作为 <code>java.sql.Timestamp</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     * @since 1.2
     */
    java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
      throws SQLException;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.sql.Timestamp</code> 对象。
     * 如果底层数据库不存储时区信息，此方法使用给定的日历构造适当的毫秒值。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param cal 用于构造日期的 <code>java.util.Calendar</code> 对象
     * @return 作为 <code>java.sql.Timestamp</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效或
     * 如果发生数据库访问错误或在已关闭的结果集上调用此方法
     * @since 1.2
     */
    java.sql.Timestamp getTimestamp(String columnLabel, Calendar cal)
      throws SQLException;

    //-------------------------- JDBC 3.0 ----------------------------------------

    /**
     * 表示具有此保持性的打开 <code>ResultSet</code> 对象在当前事务提交时将保持打开状态的常量。
     *
     * @since 1.4
     */
    int HOLD_CURSORS_OVER_COMMIT = 1;

    /**
     * 表示具有此保持性的打开 <code>ResultSet</code> 对象在当前事务提交时将关闭的常量。
     *
     * @since 1.4
     */
    int CLOSE_CURSORS_AT_COMMIT = 2;

    /**
     * 获取此 <code>ResultSet</code> 对象当前行指定列的值，作为 Java 编程语言中的 <code>java.net.URL</code> 对象。
     *
     * @param columnIndex 列的索引 1 是第一列，2 是第二列，...
     * @return 作为 <code>java.net.URL</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，则返回 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；此方法
     * 在已关闭的结果集上调用或 URL 格式错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    java.net.URL getURL(int columnIndex) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，作为 Java 编程语言中的 <code>java.net.URL</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 作为 <code>java.net.URL</code> 对象的列值；
     * 如果值是 SQL <code>NULL</code>，
     * 则返回 Java 编程语言中的 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；此方法
     * 在已关闭的结果集上调用或 URL 格式错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    java.net.URL getURL(String columnLabel) throws SQLException;

    /**
     * 使用 <code>java.sql.Ref</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateRef(int columnIndex, java.sql.Ref x) throws SQLException;

    /**
     * 使用 <code>java.sql.Ref</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateRef(String columnLabel, java.sql.Ref x) throws SQLException;

    /**
     * 使用 <code>java.sql.Blob</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException;

    /**
     * 使用 <code>java.sql.Blob</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateBlob(String columnLabel, java.sql.Blob x) throws SQLException;

    /**
     * 使用 <code>java.sql.Clob</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateClob(int columnIndex, java.sql.Clob x) throws SQLException;

    /**
     * 使用 <code>java.sql.Clob</code> 值更新指定列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.4
     */
    void updateClob(String columnLabel, java.sql.Clob x) throws SQLException;

                /**
     * 使用 <code>java.sql.Array</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void updateArray(int columnIndex, java.sql.Array x) throws SQLException;

    /**
     * 使用 <code>java.sql.Array</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在已关闭的结果集上调用此方法
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void updateArray(String columnLabel, java.sql.Array x) throws SQLException;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * 从当前行的此 <code>ResultSet</code> 对象中检索指定列的值作为 Java 编程语言中的 <code>java.sql.RowId</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @throws SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId getRowId(int columnIndex) throws SQLException;

    /**
     * 从当前行的此 <code>ResultSet</code> 对象中检索指定列的值作为 Java 编程语言中的 <code>java.sql.RowId</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值是 SQL <code>NULL</code>，则返回的值是 <code>null</code>
     * @throws SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    RowId getRowId(String columnLabel) throws SQLException;

    /**
     * 使用 <code>RowId</code> 值更新指定的列。更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用
     * <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateRowId(int columnIndex, RowId x) throws SQLException;

    /**
     * 使用 <code>RowId</code> 值更新指定的列。更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用
     * <code>updateRow</code> 或 <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法上调用已关闭的结果集
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateRowId(String columnLabel, RowId x) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象的保持性
     * @return 要么是 <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>，要么是 <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException 如果发生数据库访问错误
     * 或在此方法上调用已关闭的结果集
     * @since 1.6
     */
    int getHoldability() throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象是否已关闭。如果已调用 close 方法，或自动关闭，则 <code>ResultSet</code> 已关闭。
     *
     * @return 如果此 <code>ResultSet</code> 对象已关闭，则返回 true；如果仍然打开，则返回 false
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.6
     */
    boolean isClosed() throws SQLException;

                /**
     * 使用 <code>String</code> 值更新指定的列。
     * 该方法旨在用于更新 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param nString 要更新的列的值
     * @throws SQLException 如果 columnIndex 无效；
     * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或者如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNString(int columnIndex, String nString) throws SQLException;

    /**
     * 使用 <code>String</code> 值更新指定的列。
     * 该方法旨在用于更新 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param nString 要更新的列的值
     * @throws SQLException 如果 columnLabel 无效；
     * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法；
     * 结果集并发性为 <CODE>CONCUR_READ_ONLY</code>
     *  或者如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNString(String columnLabel, String nString) throws SQLException;

    /**
     * 使用 <code>java.sql.NClob</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param nClob 要更新的列的值
     * @throws SQLException 如果 columnIndex 无效；
     * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法；
     * 如果发生数据库访问错误或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNClob(int columnIndex, NClob nClob) throws SQLException;

    /**
     * 使用 <code>java.sql.NClob</code> 值更新指定的列。
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param nClob 要更新的列的值
     * @throws SQLException 如果 columnLabel 无效；
     * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法；
     *  如果发生数据库访问错误或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNClob(String columnLabel, NClob nClob) throws SQLException;

    /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>NClob</code> 对象。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 一个 <code>NClob</code> 对象，表示指定列中的 SQL
     *         <code>NCLOB</code> 值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法
     * 或者如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    NClob getNClob(int columnIndex) throws SQLException;

  /**
     * 检索此 <code>ResultSet</code> 对象当前行中指定列的值
     * 作为 Java 编程语言中的 <code>NClob</code> 对象。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @return 一个 <code>NClob</code> 对象，表示指定列中的 SQL <code>NCLOB</code>
     * 值
     * @exception SQLException 如果 columnLabel 无效；
   * 如果驱动程序不支持国家
     *         字符集；如果驱动程序可以检测到可能发生的数据转换
     *  错误；在已关闭的结果集上调用此方法
     * 或者如果发生数据库访问错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    NClob getNClob(String columnLabel) throws SQLException;

                /**
     * 获取当前行中指定列的值，作为 Java 编程语言中的
     * <code>java.sql.SQLXML</code> 对象。
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 映射 <code>SQL XML</code> 值的 <code>SQLXML</code> 对象
     * @throws SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    SQLXML getSQLXML(int columnIndex) throws SQLException;

    /**
     * 获取当前行中指定列的值，作为 Java 编程语言中的
     * <code>java.sql.SQLXML</code> 对象。
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 映射 <code>SQL XML</code> 值的 <code>SQLXML</code> 对象
     * @throws SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    SQLXML getSQLXML(String columnLabel) throws SQLException;
    /**
     * 使用 <code>java.sql.SQLXML</code> 值更新指定列。
     * 更新器
     * 方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是
     * 调用 <code>updateRow</code> 或 <code>insertRow</code> 方法
     * 以更新数据库。
     * <p>
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param xmlObject 要更新的列的值
     * @throws SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；此方法
     * 在已关闭的结果集上调用；
     * <code>java.xml.transform.Result</code>、
     * <code>Writer</code> 或 <code>OutputStream</code> 未关闭
     * 对于 <code>SQLXML</code> 对象；
     * 如果处理 XML 值时出错或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>。异常的 <code>getCause</code> 方法
     * 可能提供更详细的异常，例如，如果流不包含有效的 XML。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException;
    /**
     * 使用 <code>java.sql.SQLXML</code> 值更新指定列。
     * 更新器
     * 方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是
     * 调用 <code>updateRow</code> 或 <code>insertRow</code> 方法
     * 以更新数据库。
     * <p>
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param xmlObject 列值
     * @throws SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；此方法
     * 在已关闭的结果集上调用；
     * <code>java.xml.transform.Result</code>、
     * <code>Writer</code> 或 <code>OutputStream</code> 未关闭
     * 对于 <code>SQLXML</code> 对象；
     * 如果处理 XML 值时出错或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>。异常的 <code>getCause</code> 方法
     * 可能提供更详细的异常，例如，如果流不包含有效的 XML。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException;

    /**
     * 获取当前行中指定列的值，作为 Java 编程语言中的
     * <code>String</code>。
     * 旨在用于访问
     * <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回值为 <code>null</code>
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    String getNString(int columnIndex) throws SQLException;


    /**
     * 获取当前行中指定列的值，作为 Java 编程语言中的
     * <code>String</code>。
     * 旨在用于访问
     * <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @return 列值；如果值为 SQL <code>NULL</code>，则
     * 返回值为 <code>null</code>
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    String getNString(String columnLabel) throws SQLException;


    /**
     * 获取当前行中指定列的值，作为
     * <code>java.io.Reader</code> 对象。
     * 旨在用于访问
     * <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     *
     * @return 包含列值的 <code>java.io.Reader</code> 对象；如果值为 SQL <code>NULL</code>，则
     * 在 Java 编程语言中返回值为 <code>null</code>。
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    java.io.Reader getNCharacterStream(int columnIndex) throws SQLException;

                /**
     * 获取此 <code>ResultSet</code> 对象当前行中指定列的值，
     * 作为 <code>java.io.Reader</code> 对象。
     * 该方法适用于访问 <code>NCHAR</code>,<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列标签。 如果未指定 SQL AS 子句，则标签是列的名称
     * @return 包含列值的 <code>java.io.Reader</code> 对象；如果值是 SQL <code>NULL</code>，则返回
     * <code>null</code>（Java 编程语言中的）
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误
     * 或在此方法调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    java.io.Reader getNCharacterStream(String columnLabel) throws SQLException;

    /**
     * 使用字符流值更新指定列，该值将具有指定的字节数。 
     * 驱动程序执行从 Java 字符格式到数据库中的国家字符集的必要转换。
     * 该方法适用于更新 <code>NCHAR</code>,<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。 更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code> 或在此方法调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNCharacterStream(int columnIndex,
                             java.io.Reader x,
                             long length) throws SQLException;

    /**
     * 使用字符流值更新指定列，该值将具有指定的字节数。 
     * 驱动程序执行从 Java 字符格式到数据库中的国家字符集的必要转换。
     * 该方法适用于更新 <code>NCHAR</code>,<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。 更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列标签。 如果未指定 SQL AS 子句，则标签是列的名称
     * @param reader 包含新列值的 <code>java.io.Reader</code> 对象
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code> 或在此方法调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateNCharacterStream(String columnLabel,
                             java.io.Reader reader,
                             long length) throws SQLException;
    /**
     * 使用 ascii 流值更新指定列，该值将具有指定的字节数。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。 更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateAsciiStream(int columnIndex,
                           java.io.InputStream x,
                           long length) throws SQLException;

    /**
     * 使用二进制流值更新指定列，该值将具有指定的字节数。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。 更新方法不会
     * 更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或在此方法调用时结果集已关闭
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateBinaryStream(int columnIndex,
                            java.io.InputStream x,
                            long length) throws SQLException;

                /**
     * 使用指定字节数的字符流值更新指定的列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateCharacterStream(int columnIndex,
                             java.io.Reader x,
                             long length) throws SQLException;
    /**
     * 使用指定字节数的 ASCII 流值更新指定的列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateAsciiStream(String columnLabel,
                           java.io.InputStream x,
                           long length) throws SQLException;

    /**
     * 使用指定字节数的二进制流值更新指定的列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param x 新的列值
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateBinaryStream(String columnLabel,
                            java.io.InputStream x,
                            long length) throws SQLException;

    /**
     * 使用指定字节数的字符流值更新指定的列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param reader 包含新列值的 <code>java.io.Reader</code> 对象
     * @param length 流的长度
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateCharacterStream(String columnLabel,
                             java.io.Reader reader,
                             long length) throws SQLException;
    /**
     * 使用给定的输入流更新指定的列，该流具有指定的字节数。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param inputStream 包含要设置参数值的数据的对象。
     * @param length 参数数据的字节数。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException;

    /**
     * 使用给定的输入流更新指定的列，该流具有指定的字节数。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param inputStream 包含要设置参数值的数据的对象。
     * @param length 参数数据的字节数。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException;

                /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列，该对象的长度为给定的字符数。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 UNICODE 值时，通过 <code>java.io.Reader</code>
     * 对象发送可能更为实际。JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateClob(int columnIndex,  Reader reader, long length) throws SQLException;

    /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列，该对象的长度为给定的字符数。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 UNICODE 值时，通过 <code>java.io.Reader</code>
     * 对象发送可能更为实际。JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateClob(String columnLabel,  Reader reader, long length) throws SQLException;
   /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列，该对象的长度为给定的字符数。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 UNICODE 值时，通过 <code>java.io.Reader</code>
     * 对象发送可能更为实际。JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 columnIndex 无效；
     * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；
     * 此方法在已关闭的结果集上调用，如果发生数据库访问错误或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateNClob(int columnIndex,  Reader reader, long length) throws SQLException;

    /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列，该对象的长度为给定的字符数。
     * 当向 <code>LONGVARCHAR</code> 参数输入非常大的 UNICODE 值时，通过 <code>java.io.Reader</code>
     * 对象发送可能更为实际。JDBC 驱动程序将执行从 UNICODE 到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * @param columnLabel 用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签为列名
     * @param reader 包含要设置参数值的数据的对象。
     * @param length 参数数据中的字符数。
     * @throws SQLException 如果 columnLabel 无效；
     * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；
     * 此方法在已关闭的结果集上调用，如果发生数据库访问错误或
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateNClob(String columnLabel,  Reader reader, long length) throws SQLException;

    //---

    /**
     * 使用字符流值更新指定的列。
     * 数据将从流中按需读取，直到达到流的末尾。驱动程序将执行从 Java 字符格式到
     * 数据库国家字符集的必要转换。它旨在用于更新 <code>NCHAR</code>、<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * <p>
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>请查阅您的 JDBC 驱动程序文档，以确定使用带有长度参数的
     * <code>updateNCharacterStream</code> 版本是否更高效。
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code> 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void updateNCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException;

                /**
     * 使用字符流值更新指定的列。
     * 数据将从流中按需读取，直到流结束。驱动程序将执行从Java字符格式到数据库中的国家字符集的必要转换。
     * 该方法旨在用于更新 <code>NCHAR</code>,<code>NVARCHAR</code>
     * 和 <code>LONGNVARCHAR</code> 列。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的
     * <code>updateNCharacterStream</code> 版本可能更高效。
     *
     * @param columnLabel 使用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签为列的名称
     * @param reader 包含新列值的 <code>java.io.Reader</code> 对象
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code> 或此方法在已关闭的结果集上调用
      * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateNCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException;
    /**
     * 使用ASCII流值更新指定的列。
     * 数据将从流中按需读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的
     * <code>updateAsciiStream</code> 版本可能更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateAsciiStream(int columnIndex,
                           java.io.InputStream x) throws SQLException;

    /**
     * 使用二进制流值更新指定的列。
     * 数据将从流中按需读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的
     * <code>updateBinaryStream</code> 版本可能更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateBinaryStream(int columnIndex,
                            java.io.InputStream x) throws SQLException;

    /**
     * 使用字符流值更新指定的列。
     * 数据将从流中按需读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的
     * <code>updateCharacterStream</code> 版本可能更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param x 新的列值
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException;
    /**
     * 使用ASCII流值更新指定的列。
     * 数据将从流中按需读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的
     * <code>updateAsciiStream</code> 版本可能更高效。
     *
     * @param columnLabel 使用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签为列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateAsciiStream(String columnLabel,
                           java.io.InputStream x) throws SQLException;

                /**
     * 使用二进制流值更新指定的列。
     * 数据将从流中读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B> 请参阅您的JDBC驱动程序文档，以确定是否使用带长度参数的
     * <code>updateBinaryStream</code> 版本可能会更高效。
     *
     * @param columnLabel 用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签是列的名称
     * @param x 新的列值
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateBinaryStream(String columnLabel,
                            java.io.InputStream x) throws SQLException;

    /**
     * 使用字符流值更新指定的列。
     * 数据将从流中读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B> 请参阅您的JDBC驱动程序文档，以确定是否使用带长度参数的
     * <code>updateCharacterStream</code> 版本可能会更高效。
     *
     * @param columnLabel 用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签是列的名称
     * @param reader 包含新列值的 <code>java.io.Reader</code> 对象
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException;
    /**
     * 使用给定的输入流更新指定的列。数据将从流中读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B> 请参阅您的JDBC驱动程序文档，以确定是否使用带长度参数的
     * <code>updateBlob</code> 版本可能会更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param inputStream 包含要设置参数值的数据的对象。
     * @exception SQLException 如果 columnIndex 无效；如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateBlob(int columnIndex, InputStream inputStream) throws SQLException;

    /**
     * 使用给定的输入流更新指定的列。数据将从流中读取，直到流结束。
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     *   <P><B>注意：</B> 请参阅您的JDBC驱动程序文档，以确定是否使用带长度参数的
     * <code>updateBlob</code> 版本可能会更高效。
     *
     * @param columnLabel 用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签是列的名称
     * @param inputStream 包含要设置参数值的数据的对象。
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateBlob(String columnLabel, InputStream inputStream) throws SQLException;

    /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列。
     *  数据将从流中读取，直到流结束。JDBC驱动程序将执行从UNICODE到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     *   <P><B>注意：</B> 请参阅您的JDBC驱动程序文档，以确定是否使用带长度参数的
     * <code>updateClob</code> 版本可能会更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param reader 包含要设置参数值的数据的对象。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持
     * 此方法
     * @since 1.6
     */
    void updateClob(int columnIndex,  Reader reader) throws SQLException;

                /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列。
     * 数据将从流中按需读取，直到到达流的末尾。JDBC驱动程序将执行从UNICODE到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的 <code>updateClob</code> 版本可能更高效。
     *
     * @param columnLabel 用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签是列的名称
     * @param reader 包含要设置参数值的数据的对象。
     * @exception SQLException 如果 columnLabel 无效；如果发生数据库访问错误；
     * 结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateClob(String columnLabel,  Reader reader) throws SQLException;
   /**
     * 使用给定的 <code>Reader</code> 更新指定的列。
     *
     * 数据将从流中按需读取，直到到达流的末尾。JDBC驱动程序将执行从UNICODE到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的 <code>updateNClob</code> 版本可能更高效。
     *
     * @param columnIndex 第一列是1，第二列是2，...
     * @param reader 包含要设置参数值的数据的对象。
     * @throws SQLException 如果 columnIndex 无效；
    * 如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；此方法在已关闭的结果集上调用，
     * 如果发生数据库访问错误或结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateNClob(int columnIndex,  Reader reader) throws SQLException;

    /**
     * 使用给定的 <code>Reader</code> 对象更新指定的列。
     * 数据将从流中按需读取，直到到达流的末尾。JDBC驱动程序将执行从UNICODE到数据库字符格式的任何必要转换。
     *
     * <p>
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 <code>updateRow</code> 或
     * <code>insertRow</code> 方法来更新数据库。
     *
     * <P><B>注意：</B>查阅您的JDBC驱动程序文档，以确定是否使用带有长度参数的 <code>updateNClob</code> 版本可能更高效。
     *
     * @param columnLabel 用SQL AS子句指定的列的标签。如果未指定SQL AS子句，则标签是列的名称
     * @param reader 包含要设置参数值的数据的对象。
     * @throws SQLException 如果 columnLabel 无效；如果驱动程序不支持国家字符集；如果驱动程序可以检测到可能发生数据转换错误；此方法在已关闭的结果集上调用；
     * 如果发生数据库访问错误或结果集并发性为 <code>CONCUR_READ_ONLY</code>
     * @exception SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.6
     */
    void updateNClob(String columnLabel,  Reader reader) throws SQLException;

    //------------------------- JDBC 4.1 -----------------------------------


    /**
     *<p>检索此 <code>ResultSet</code> 对象当前行中指定列的值，并将从列的SQL类型转换为请求的Java数据类型，如果支持该转换。如果转换不支持或类型为null，则抛出
     * <code>SQLException</code>。
     *<p>
     * 至少，实现必须支持附录B，表B-3中定义的转换和适当用户定义的SQL类型到实现 {@code SQLData} 或 {@code Struct} 的Java类型的转换。
     * 可能支持额外的转换，这些转换由供应商定义。
     * @param <T> 此类对象建模的类的类型
     * @param columnIndex 第一列是1，第二列是2，...
     * @param type 表示要将指定列转换为的Java数据类型的类。
     * @return 包含列值的 {@code type} 实例
     * @throws SQLException 如果转换不支持，类型为null或发生其他错误。异常的 getCause() 方法可能提供更详细的异常，例如，如果发生转换错误
     * @throws SQLFeatureNotSupportedException 如果JDBC驱动程序不支持此方法
     * @since 1.7
     */
     public <T> T getObject(int columnIndex, Class<T> type) throws SQLException;

    /**
     *<p>获取此 <code>ResultSet</code> 对象当前行中指定列的值，并将从 SQL 类型转换为请求的 Java 数据类型（如果支持这种转换）。如果转换不支持或类型为 null，则抛出
     * <code>SQLException</code>。
     *<p>
     * 至少，实现必须支持附录 B，表 B-3 中定义的转换，以及将适当的用户定义的 SQL 类型转换为实现 {@code SQLData} 或 {@code Struct} 的 Java 类型。
     * 可能支持额外的转换，这些转换由供应商定义。
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param type 表示要将指定列转换为的 Java 数据类型的类
     * @param <T> 由此类对象建模的类型
     * @return 包含列值的 {@code type} 实例
     * @throws SQLException 如果转换不支持，类型为 null 或发生其他错误。异常的 getCause() 方法可能提供更详细的异常，例如，如果发生转换错误
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.7
     */
     public <T> T getObject(String columnLabel, Class<T> type) throws SQLException;

    //------------------------- JDBC 4.2 -----------------------------------

    /**
     * 使用 {@code Object} 值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 {@code updateRow} 或
     * {@code insertRow} 方法来更新数据库。
     *<p>
     * 如果第二个参数是 {@code InputStream}，则流必须包含由 scaleOrLength 指定的字节数。如果第二个参数是
     * {@code Reader}，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些条件不满足，驱动程序将在执行语句时生成
     * {@code SQLException}。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @param scaleOrLength 对于 {@code java.math.BigDecimal} 类型的对象，这是小数点后的位数。对于
     *          Java 对象类型 {@code InputStream} 和 {@code Reader}，这是流或读取器中数据的长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性是 {@code CONCUR_READ_ONLY}
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法；如果 JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
     default void updateObject(int columnIndex, Object x,
             SQLType targetSqlType, int scaleOrLength)  throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject not implemented");
    }

    /**
     * 使用 {@code Object} 值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 {@code updateRow} 或
     * {@code insertRow} 方法来更新数据库。
     *<p>
     * 如果第二个参数是 {@code InputStream}，则流必须包含由 scaleOrLength 指定的字节数。如果第二个参数是
     * {@code Reader}，则读取器必须包含由 scaleOrLength 指定的字符数。如果这些条件不满足，驱动程序将在执行语句时生成
     * {@code SQLException}。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @param scaleOrLength 对于 {@code java.math.BigDecimal} 类型的对象，这是小数点后的位数。对于
     *          Java 对象类型 {@code InputStream} 和 {@code Reader}，这是流或读取器中数据的长度。对于所有其他类型，
     *          此值将被忽略。
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性是 {@code CONCUR_READ_ONLY}
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法；如果 JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void updateObject(String columnLabel, Object x,
            SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject not implemented");
    }

    /**
     * 使用 {@code Object} 值更新指定的列。
     *
     * 更新方法用于更新当前行或插入行中的列值。更新方法不会更新底层数据库；而是调用 {@code updateRow} 或
     * {@code insertRow} 方法来更新数据库。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param columnIndex 第一列是 1，第二列是 2，...
     * @param x 新的列值
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @exception SQLException 如果 columnIndex 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性是 {@code CONCUR_READ_ONLY}
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法；如果 JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void updateObject(int columnIndex, Object x, SQLType targetSqlType)
            throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject not implemented");
    }

                /**
     * 使用 {@code Object} 值更新指定的列。
     *
     * 更新器方法用于更新当前行或插入行中的列值。更新器方法不会更新底层数据库；相反，要更新数据库，需要调用 {@code updateRow} 或
     * {@code insertRow} 方法。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param columnLabel 使用 SQL AS 子句指定的列的标签。如果未指定 SQL AS 子句，则标签是列的名称
     * @param x 新的列值
     * @param targetSqlType 要发送到数据库的 SQL 类型
     * @exception SQLException 如果 columnLabel 无效；
     * 如果发生数据库访问错误；
     * 结果集并发性为 {@code CONCUR_READ_ONLY}
     * 或此方法在已关闭的结果集上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法；如果 JDBC 驱动程序不支持指定的 targetSqlType
     * @see JDBCType
     * @see SQLType
     * @since 1.8
     */
    default void updateObject(String columnLabel, Object x,
            SQLType targetSqlType) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject not implemented");
    }
}
