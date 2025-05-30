
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <P>用于执行静态 SQL 语句并返回其产生的结果的对象。
 * <P>
 * 默认情况下，每个 <code>Statement</code> 对象同时只能打开一个 <code>ResultSet</code> 对象。因此，如果一个
 * <code>ResultSet</code> 对象的读取与另一个 <code>ResultSet</code> 对象的读取交错进行，
 * 每个 <code>ResultSet</code> 对象必须由不同的 <code>Statement</code> 对象生成。所有
 * <code>Statement</code> 接口中的执行方法都会隐式地关闭当前的
 * <code>ResultSet</code> 对象（如果存在）。
 *
 * @see Connection#createStatement
 * @see ResultSet
 */
public interface Statement extends Wrapper, AutoCloseable {

    /**
     * 执行给定的 SQL 语句，该语句返回一个 <code>ResultSet</code> 对象。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 要发送到数据库的 SQL 语句，通常是一个静态的 SQL <code>SELECT</code> 语句
     * @return 包含给定查询产生的数据的 <code>ResultSet</code> 对象；永远不会为 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用，给定的
     *            SQL 语句产生除单个 <code>ResultSet</code> 对象以外的任何内容，或者在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并且至少尝试取消当前正在运行的 {@code Statement} 时
     */
    ResultSet executeQuery(String sql) throws SQLException;

    /**
     * 执行给定的 SQL 语句，该语句可能是 <code>INSERT</code>、<code>UPDATE</code> 或 <code>DELETE</code> 语句，
     * 或者是一个不返回任何内容的 SQL 语句，例如 SQL DDL 语句。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 一个 SQL 数据操作语言（DML）语句，如 <code>INSERT</code>、<code>UPDATE</code> 或
     * <code>DELETE</code>；或者是一个不返回任何内容的 SQL 语句，如 DDL 语句。
     *
     * @return (1) 对于 SQL 数据操作语言（DML）语句，返回受影响的行数
     *         或 (2) 对于不返回任何内容的 SQL 语句，返回 0
     *
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用，给定的
     * SQL 语句产生一个 <code>ResultSet</code> 对象，或者在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并且至少尝试取消当前正在运行的 {@code Statement} 时
     */
    int executeUpdate(String sql) throws SQLException;

    /**
     * 立即释放此 <code>Statement</code> 对象的数据库和 JDBC 资源，而不是等待自动关闭。
     * 通常情况下，尽早释放资源是一个好习惯，以避免占用数据库资源。
     * <P>
     * 在已经关闭的 <code>Statement</code> 对象上调用 <code>close</code> 方法没有效果。
     * <P>
     * <B>注意：</B>当 <code>Statement</code> 对象关闭时，如果存在当前的 <code>ResultSet</code> 对象，也会被关闭。
     *
     * @exception SQLException 如果发生数据库访问错误
     */
    void close() throws SQLException;

    //----------------------------------------------------------------------

    /**
     * 检索此 <code>Statement</code> 对象生成的 <code>ResultSet</code> 对象中字符和二进制列值的最大字节数。
     * 该限制仅适用于 <code>BINARY</code>、<code>VARBINARY</code>、<code>LONGVARBINARY</code>、<code>CHAR</code>、<code>VARCHAR</code>、
     * <code>NCHAR</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code> 和 <code>LONGVARCHAR</code> 列。如果超过限制，多余的
     * 数据将被静默丢弃。
     *
     * @return 存储字符和二进制值的列的当前列大小限制；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     * @see #setMaxFieldSize
     */
    int getMaxFieldSize() throws SQLException;

    /**
     * 设置此 <code>Statement</code> 对象生成的 <code>ResultSet</code> 对象中字符和二进制列值的最大字节数。
     *
     * 该限制仅适用于 <code>BINARY</code>、<code>VARBINARY</code>、<code>LONGVARBINARY</code>、<code>CHAR</code>、<code>VARCHAR</code>、
     * <code>NCHAR</code>、<code>NVARCHAR</code>、<code>LONGNVARCHAR</code> 和 <code>LONGVARCHAR</code> 字段。如果超过限制，多余的
     * 数据将被静默丢弃。为了最大限度的可移植性，使用大于 256 的值。
     *
     * @param max 新的列大小限制（以字节为单位）；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用
     *            或者不满足条件 {@code max >= 0}
     * @see #getMaxFieldSize
     */
    void setMaxFieldSize(int max) throws SQLException;

    /**
     * 检索此 <code>Statement</code> 对象生成的 <code>ResultSet</code> 对象可以包含的最大行数。如果超过此限制，
     * 多余的行将被静默丢弃。
     *
     * @return 此 <code>Statement</code> 对象生成的 <code>ResultSet</code> 对象的当前最大行数；
     *         零表示没有限制
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     * @see #setMaxRows
     */
    int getMaxRows() throws SQLException;

    /**
     * 设置此 <code>Statement</code> 对象生成的任何 <code>ResultSet</code> 对象可以包含的最大行数。
     * 如果超过此限制，多余的行将被静默丢弃。
     *
     * @param max 新的最大行数；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用
     *            或者不满足条件 {@code max >= 0}
     * @see #getMaxRows
     */
    void setMaxRows(int max) throws SQLException;

    /**
     * 设置是否启用转义处理。
     * 如果启用了转义处理（默认值），驱动程序将在将 SQL 语句发送到数据库之前进行转义替换。
     *<p>
     * 可以使用 {@code Connection} 和 {@code DataSource} 属性
     * {@code escapeProcessing} 来更改默认的转义处理行为。值为 true（默认值）表示启用转义处理
     * 对所有 {@code Statement} 对象。值为 false 表示禁用所有 {@code Statement} 对象的转义处理。
     * {@code setEscapeProcessing} 方法可用于指定单个 {@code Statement} 对象的转义处理行为。
     * <p>
     * 注意：由于预编译语句通常在调用此方法之前已经解析，因此禁用 <code>PreparedStatements</code> 对象的转义处理
     * 将不会产生任何效果。
     *
     * @param enable <code>true</code> 启用转义处理；<code>false</code> 禁用转义处理
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     */
    void setEscapeProcessing(boolean enable) throws SQLException;

    /**
     * 检索驱动程序等待 <code>Statement</code> 对象执行的秒数。
     * 如果超过限制，将抛出 <code>SQLException</code>。
     *
     * @return 当前查询超时限制（以秒为单位）；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     * @see #setQueryTimeout
     */
    int getQueryTimeout() throws SQLException;

    /**
     * 设置驱动程序等待 <code>Statement</code> 对象执行的秒数。
     * 默认情况下，运行的语句没有时间限制。如果超过限制，将抛出 <code>SQLTimeoutException</code>。
     * JDBC 驱动程序必须将此限制应用于 <code>execute</code>、<code>executeQuery</code> 和 <code>executeUpdate</code> 方法。
     * <p>
     * <strong>注意：</strong>JDBC 驱动程序实现也可能将此限制应用于 {@code ResultSet} 方法
     * （请参阅驱动程序供应商文档以获取详细信息）。
     * <p>
     * <strong>注意：</strong>在 {@code Statement} 批处理的情况下，超时是应用于通过 {@code addBatch} 方法添加的单个 SQL 命令还是应用于通过 {@code executeBatch} 方法调用的整个 SQL 命令批处理，这是由实现定义的
     * （请参阅驱动程序供应商文档以获取详细信息）。
     *
     * @param seconds 新的查询超时限制（以秒为单位）；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用
     *            或者不满足条件 {@code seconds >= 0}
     * @see #getQueryTimeout
     */
    void setQueryTimeout(int seconds) throws SQLException;

    /**
     * 如果 DBMS 和驱动程序支持中止 SQL 语句，则取消此 <code>Statement</code> 对象。
     * 一个线程可以使用此方法取消另一个线程正在执行的语句。
     *
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     */
    void cancel() throws SQLException;

    /**
     * 检索此 <code>Statement</code> 对象上报的第一个警告。
     * 后续的 <code>Statement</code> 对象警告将链接到此 <code>SQLWarning</code> 对象。
     *
     * <p>每次（重新）执行语句时，警告链将自动清除。此方法不能在已关闭的
     * <code>Statement</code> 对象上调用；这样做将导致抛出 <code>SQLException</code>。
     *
     * <P><B>注意：</B>如果正在处理 <code>ResultSet</code> 对象，与该 <code>ResultSet</code> 对象读取相关的任何警告
     * 将链接到该 <code>ResultSet</code> 对象，而不是生成它的 <code>Statement</code> 对象。
     *
     * @return 第一个 <code>SQLWarning</code> 对象或 <code>null</code>
     *         如果没有警告
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     */
    SQLWarning getWarnings() throws SQLException;

    /**
     * 清除此 <code>Statement</code> 对象上报的所有警告。调用此方法后，
     * <code>getWarnings</code> 方法将返回 <code>null</code>，直到为该
     * <code>Statement</code> 对象报告新的警告。
     *
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     */
    void clearWarnings() throws SQLException;

    /**
     * 将 SQL 游标名称设置为给定的 <code>String</code>，该名称将用于后续的 <code>Statement</code> 对象
     * <code>execute</code> 方法。然后可以使用该名称在 SQL 定位更新或删除语句中标识
     * 由该语句生成的 <code>ResultSet</code> 对象中的当前行。如果数据库不支持定位更新/删除，
     * 此方法是一个空操作。为了确保游标具有支持更新的适当隔离级别，游标的 <code>SELECT</code> 语句
     * 应该具有 <code>SELECT FOR UPDATE</code> 的形式。如果 <code>FOR UPDATE</code> 不在场，定位更新可能会失败。
     *
     * <P><B>注意：</B>按定义，定位更新和删除的执行必须由与生成用于定位的 <code>ResultSet</code> 对象不同的
     * <code>Statement</code> 对象完成。此外，游标名称在连接内必须是唯一的。
     *
     * @param name 新的游标名称，必须在连接内唯一
     * @exception SQLException 如果发生数据库访问错误或此方法在已关闭的 <code>Statement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     */
    void setCursorName(String name) throws SQLException;

    //----------------------- Multiple Results --------------------------


                /**
     * 执行给定的 SQL 语句，该语句可能返回多个结果。
     * 在某些（不常见）情况下，单个 SQL 语句可能返回
     * 多个结果集和/或更新计数。通常你可以忽略这一点，除非你
     * (1) 执行一个已知可能返回多个结果的存储过程，或 (2) 动态执行一个
     * 未知的 SQL 字符串。
     * <P>
     * <code>execute</code> 方法执行一个 SQL 语句并指示
     * 第一个结果的形式。然后你必须使用方法
     * <code>getResultSet</code> 或 <code>getUpdateCount</code>
     * 来检索结果，并使用 <code>getMoreResults</code> 来
     * 移动到任何后续结果。
     * <p>
     * <strong>注意：</strong>此方法不能在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 任何 SQL 语句
     * @return 如果第一个结果是 <code>ResultSet</code>
     *         对象，则返回 <code>true</code>；如果是更新计数或没有结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用，
     * 或者在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并且至少尝试取消
     * 当前运行的 {@code Statement} 时
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     */
    boolean execute(String sql) throws SQLException;

    /**
     * 以 <code>ResultSet</code> 对象的形式检索当前结果。
     * 该方法应仅对每个结果调用一次。
     *
     * @return 当前结果作为一个 <code>ResultSet</code> 对象，或
     * 如果结果是更新计数或没有更多结果，则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @see #execute
     */
    ResultSet getResultSet() throws SQLException;

    /**
     * 以更新计数的形式检索当前结果；
     * 如果结果是 <code>ResultSet</code> 对象或没有更多结果，则返回 -1。
     * 该方法应仅对每个结果调用一次。
     *
     * @return 当前结果作为一个更新计数；如果当前结果是
     * <code>ResultSet</code> 对象或没有更多结果，则返回 -1
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @see #execute
     */
    int getUpdateCount() throws SQLException;

    /**
     * 移动到此 <code>Statement</code> 对象的下一个结果，如果它是 <code>ResultSet</code> 对象，则返回
     * <code>true</code>，并隐式关闭任何当前通过 <code>getResultSet</code> 方法获得的 <code>ResultSet</code>
     * 对象。
     *
     * <P>当以下条件为真时，没有更多结果：
     * <PRE>{@code
     *     // stmt 是一个 Statement 对象
     *     ((stmt.getMoreResults() == false) && (stmt.getUpdateCount() == -1))
     * }</PRE>
     *
     * @return 如果下一个结果是 <code>ResultSet</code>
     *         对象，则返回 <code>true</code>；如果是更新计数或没有更多结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @see #execute
     */
    boolean getMoreResults() throws SQLException;


    //--------------------------JDBC 2.0-----------------------------


    /**
     * 给驱动程序一个提示，指示在使用此 <code>Statement</code> 对象创建的 <code>ResultSet</code>
     * 对象中处理行的方向。默认值是 <code>ResultSet.FETCH_FORWARD</code>。
     * <P>
     * 注意，此方法设置此 <code>Statement</code> 对象生成的结果集的默认获取方向。
     * 每个结果集都有自己的方法来获取和设置其自身的获取方向。
     *
     * @param direction 处理行的初始方向
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * 或给定的方向不是 <code>ResultSet.FETCH_FORWARD</code>、
     * <code>ResultSet.FETCH_REVERSE</code> 或 <code>ResultSet.FETCH_UNKNOWN</code> 之一
     * @since 1.2
     * @see #getFetchDirection
     */
    void setFetchDirection(int direction) throws SQLException;

    /**
     * 检索从该 <code>Statement</code> 对象生成的结果集的默认获取行方向。
     * 如果此 <code>Statement</code> 对象尚未通过调用 <code>setFetchDirection</code> 方法
     * 设置获取方向，则返回值是实现特定的。
     *
     * @return 从该 <code>Statement</code> 对象生成的结果集的默认获取方向
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @since 1.2
     * @see #setFetchDirection
     */
    int getFetchDirection() throws SQLException;

    /**
     * 给 JDBC 驱动程序一个提示，指示当需要从数据库中获取更多行时，应该从数据库中获取的行数
     * 用于此 <code>Statement</code> 生成的 <code>ResultSet</code> 对象。
     * 如果指定的值为零，则忽略此提示。
     * 默认值为零。
     *
     * @param rows 要获取的行数
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用或
     * 条件 {@code rows >= 0} 不满足。
     * @since 1.2
     * @see #getFetchSize
     */
    void setFetchSize(int rows) throws SQLException;

    /**
     * 检索从该 <code>Statement</code> 对象生成的结果集的默认获取大小。
     * 如果此 <code>Statement</code> 对象尚未通过调用 <code>setFetchSize</code> 方法
     * 设置获取大小，则返回值是实现特定的。
     *
     * @return 从该 <code>Statement</code> 对象生成的结果集的默认获取大小
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @since 1.2
     * @see #setFetchSize
     */
    int getFetchSize() throws SQLException;

    /**
     * 检索从该 <code>Statement</code> 对象生成的结果集的并发性。
     *
     * @return <code>ResultSet.CONCUR_READ_ONLY</code> 或
     * <code>ResultSet.CONCUR_UPDATABLE</code>
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @since 1.2
     */
    int getResultSetConcurrency() throws SQLException;

    /**
     * 检索从该 <code>Statement</code> 对象生成的结果集的类型。
     *
     * @return <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     * <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> 之一
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @since 1.2
     */
    int getResultSetType()  throws SQLException;

    /**
     * 将给定的 SQL 命令添加到此 <code>Statement</code> 对象的当前命令列表中。
     * 可以通过调用 <code>executeBatch</code> 方法将此列表中的命令作为批处理执行。
     * <P>
     * <strong>注意：</strong>此方法不能在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 通常这是一个 SQL <code>INSERT</code> 或
     * <code>UPDATE</code> 语句
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用，驱动程序不支持批处理更新，或
     * 方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @see #executeBatch
     * @see DatabaseMetaData#supportsBatchUpdates
     * @since 1.2
     */
    void addBatch( String sql ) throws SQLException;

    /**
     * 清空此 <code>Statement</code> 对象的当前 SQL 命令列表。
     * <P>
     * @exception SQLException 如果发生数据库访问错误，
     *  this 方法在已关闭的 <code>Statement</code> 上调用或驱动程序不支持批处理更新
     * @see #addBatch
     * @see DatabaseMetaData#supportsBatchUpdates
     * @since 1.2
     */
    void clearBatch() throws SQLException;

    /**
     * 将命令批处理提交给数据库执行，如果所有命令都成功执行，则返回一个更新计数数组。
     * 返回的 <code>int</code> 元素数组按命令在批处理中的顺序排列，对应于批处理中命令的顺序。
     * 由 <code>executeBatch</code> 方法返回的数组中的元素可能是以下之一：
     * <OL>
     * <LI>大于或等于零的数字 -- 表示命令成功处理且是一个更新计数，给出命令执行时影响的数据库中的行数
     * <LI><code>SUCCESS_NO_INFO</code> 值 -- 表示命令成功处理但影响的行数未知
     * <P>
     * 如果批处理更新中的一个命令执行失败，此方法将抛出 <code>BatchUpdateException</code>，并且
     * JDBC 驱动程序可能会或可能不会继续处理批处理中的剩余命令。然而，驱动程序的行为必须与特定的 DBMS 一致，
     * 要么总是继续处理命令，要么从不继续处理命令。如果驱动程序在命令失败后继续处理，
     * 由 <code>BatchUpdateException.getUpdateCounts</code> 方法返回的数组将包含与批处理中的命令数量相同的元素，
     * 且至少有一个元素将是以下值：
     *
     * <LI><code>EXECUTE_FAILED</code> 值 -- 表示命令未能成功执行，仅当驱动程序在命令失败后继续处理命令时发生
     * </OL>
     * <P>
     * 可能的实现和返回值已在 Java 2 SDK, Standard Edition, version 1.3 中进行了修改，
     * 以适应在抛出 <code>BatchUpdateException</code> 对象后继续处理批处理更新中的命令的选项。
     *
     * @return 一个更新计数数组，包含批处理中每个命令的一个元素。数组的元素按命令添加到批处理中的顺序排列。
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用或驱动程序不支持批处理语句。抛出 {@link BatchUpdateException}
     * （<code>SQLException</code> 的子类）如果发送到数据库的命令未能正确执行或试图返回结果集。
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并且至少尝试取消
     * 当前运行的 {@code Statement} 时
     *
     * @see #addBatch
     * @see DatabaseMetaData#supportsBatchUpdates
     * @since 1.2
     */
    int[] executeBatch() throws SQLException;

    /**
     * 检索生成此 <code>Statement</code> 对象的 <code>Connection</code> 对象。
     * @return 生成此语句的连接
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法在已关闭的 <code>Statement</code> 上调用
     * @since 1.2
     */
    Connection getConnection()  throws SQLException;

  //--------------------------JDBC 3.0-----------------------------

    /**
     * 表示在调用 <code>getMoreResults</code> 时应关闭当前的 <code>ResultSet</code> 对象的常量。
     *
     * @since 1.4
     */
    int CLOSE_CURRENT_RESULT = 1;

    /**
     * 表示在调用 <code>getMoreResults</code> 时不应关闭当前的 <code>ResultSet</code> 对象的常量。
     *
     * @since 1.4
     */
    int KEEP_CURRENT_RESULT = 2;

    /**
     * 表示在调用 <code>getMoreResults</code> 时应关闭所有先前保持打开的 <code>ResultSet</code> 对象的常量。
     *
     * @since 1.4
     */
    int CLOSE_ALL_RESULTS = 3;

    /**
     * 表示批处理语句成功执行，但没有可用的受影响行数计数的常量。
     *
     * @since 1.4
     */
    int SUCCESS_NO_INFO = -2;

    /**
     * 表示在执行批处理语句时发生错误的常量。
     *
     * @since 1.4
     */
    int EXECUTE_FAILED = -3;

    /**
     * 表示应使生成的键可用于检索的常量。
     *
     * @since 1.4
     */
    int RETURN_GENERATED_KEYS = 1;

    /**
     * 表示不应使生成的键可用于检索的常量。
     *
     * @since 1.4
     */
    int NO_GENERATED_KEYS = 2;

    /**
     * 移动到此 <code>Statement</code> 对象的下一个结果，根据给定标志处理任何当前的
     * <code>ResultSet</code> 对象，并返回
     * <code>true</code> 如果下一个结果是 <code>ResultSet</code> 对象。
     *
     * <P>当以下条件为真时，没有更多结果：
     * <PRE>{@code
     *     // stmt 是一个 Statement 对象
     *     ((stmt.getMoreResults(current) == false) && (stmt.getUpdateCount() == -1))
     * }</PRE>
     *
     * @param current 以下 <code>Statement</code>
     *        常量之一，指示应如何处理通过 <code>getResultSet</code> 方法获得的当前
     *        <code>ResultSet</code> 对象：
     *        <code>Statement.CLOSE_CURRENT_RESULT</code>、
     *        <code>Statement.KEEP_CURRENT_RESULT</code> 或
     *        <code>Statement.CLOSE_ALL_RESULTS</code>
     * @return 如果下一个结果是 <code>ResultSet</code>
     *         对象，则返回 <code>true</code>；如果是更新计数或没有更多结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法在已关闭的 <code>Statement</code> 上调用或参数
     *         供应的不是以下之一：
     *        <code>Statement.CLOSE_CURRENT_RESULT</code>、
     *        <code>Statement.KEEP_CURRENT_RESULT</code> 或
     *        <code>Statement.CLOSE_ALL_RESULTS</code>
     *@exception SQLFeatureNotSupportedException 如果
     * <code>DatabaseMetaData.supportsMultipleOpenResults</code> 返回
     * <code>false</code> 且提供的参数为
     *        <code>Statement.KEEP_CURRENT_RESULT</code> 或
     *        <code>Statement.CLOSE_ALL_RESULTS</code>
     * @since 1.4
     * @see #execute
     */
    boolean getMoreResults(int current) throws SQLException;


    /**
     * 检索由于执行此 <code>Statement</code> 对象而生成的任何自动生成的键。如果此 <code>Statement</code> 对象未生成任何键，
     * 则返回一个空的 <code>ResultSet</code> 对象。
     *
     *<p><B>注意：</B>如果未指定表示自动生成键的列，
     * JDBC 驱动程序实现将确定最能表示自动生成键的列。
     *
     * @return 一个包含由执行此 <code>Statement</code> 对象生成的自动生成键的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误或
     * 此方法在已关闭的 <code>Statement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    ResultSet getGeneratedKeys() throws SQLException;

    /**
     * 执行给定的 SQL 语句，并向驱动程序发出信号，指示是否应使此 <code>Statement</code> 对象生成的自动生成键可用于检索。
     * 如果 SQL 语句不是 <code>INSERT</code> 语句，或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略该标志。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，如 <code>INSERT</code>、<code>UPDATE</code> 或
     * <code>DELETE</code>；或是一个不返回任何内容的 SQL 语句，如 DDL 语句。
     *
     * @param autoGeneratedKeys 一个标志，指示是否应使自动生成的键可用于检索；
     *         以下常量之一：
     *         <code>Statement.RETURN_GENERATED_KEYS</code>
     *         <code>Statement.NO_GENERATED_KEYS</code>
     * @return 要么 (1) SQL 数据操作语言 (DML) 语句的行数
     *         要么 (2) 不返回任何内容的 SQL 语句的 0
     *
     * @exception SQLException 如果发生数据库访问错误，
     *  此方法在已关闭的 <code>Statement</code> 上调用，给定的 SQL 语句返回一个 <code>ResultSet</code> 对象，
     *            给定的常量不是允许的常量之一，方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持带有 Statement.RETURN_GENERATED_KEYS 常量的此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @since 1.4
     */
    int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException;

    /**
     * 执行给定的 SQL 语句，并向驱动程序发出信号，指示给定数组中指示的自动生成键应可用于检索。
     * 此数组包含目标表中包含应可用于检索的自动生成键的列的索引。如果 SQL 语句不是 <code>INSERT</code> 语句，
     * 或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略该数组。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，如 <code>INSERT</code>、<code>UPDATE</code> 或
     * <code>DELETE</code>；或是一个不返回任何内容的 SQL 语句，如 DDL 语句。
     *
     * @param columnIndexes 一个包含应从插入行返回的列索引的数组
     * @return 要么 (1) SQL 数据操作语言 (DML) 语句的行数
     *         要么 (2) 不返回任何内容的 SQL 语句的 0
     *
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在已关闭的 <code>Statement</code> 上调用，SQL 语句返回一个 <code>ResultSet</code> 对象，
     *            提供给此方法的第二个参数不是有效的列索引的 <code>int</code> 数组，方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @since 1.4
     */
    int executeUpdate(String sql, int columnIndexes[]) throws SQLException;

    /**
     * 执行给定的 SQL 语句，并向驱动程序发出信号，指示给定数组中指示的自动生成键应可用于检索。
     * 此数组包含目标表中包含应可用于检索的自动生成键的列的名称。如果 SQL 语句不是 <code>INSERT</code> 语句，
     * 或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略该数组。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，如 <code>INSERT</code>、<code>UPDATE</code> 或
     * <code>DELETE</code>；或是一个不返回任何内容的 SQL 语句，如 DDL 语句。
     * @param columnNames 一个包含应从插入行返回的列名称的数组
     * @return 要么 <code>INSERT</code>、<code>UPDATE</code> 或 <code>DELETE</code> 语句的行数，
     *         要么不返回任何内容的 SQL 语句的 0
     * @exception SQLException 如果发生数据库访问错误，
     *  此方法在已关闭的 <code>Statement</code> 上调用，SQL 语句返回一个 <code>ResultSet</code> 对象，
     *            提供给此方法的第二个参数不是有效的列名称的 <code>String</code> 数组，方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @since 1.4
     */
    int executeUpdate(String sql, String columnNames[]) throws SQLException;

    /**
     * 执行给定的 SQL 语句，该语句可能返回多个结果，并向驱动程序发出信号，指示任何自动生成的键应可用于检索。
     * 如果 SQL 语句不是 <code>INSERT</code> 语句，或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略此信号。
     * <P>
     * 在某些（不常见）情况下，单个 SQL 语句可能返回多个结果集和/或更新计数。通常可以忽略这一点，除非 (1) 您正在执行一个已知可能返回多个结果的存储过程，
     * 或 (2) 您正在动态执行一个未知的 SQL 字符串。
     * <P>
     * <code>execute</code> 方法执行一个 SQL 语句并指示第一个结果的形式。然后必须使用 <code>getResultSet</code> 或 <code>getUpdateCount</code>
     * 方法检索结果，并使用 <code>getMoreResults</code> 方法移动到任何后续结果。
     *<p>
     *<strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 任何 SQL 语句
     * @param autoGeneratedKeys 一个常量，指示是否应使自动生成的键可用于使用 <code>getGeneratedKeys</code> 方法检索；
     *         以下常量之一：
     *         <code>Statement.RETURN_GENERATED_KEYS</code> 或
     *         <code>Statement.NO_GENERATED_KEYS</code>
     * @return 如果第一个结果是 <code>ResultSet</code> 对象，则返回 <code>true</code>；
     *         如果是更新计数或没有结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在已关闭的 <code>Statement</code> 上调用，提供给此方法的第二个参数不是
     *         <code>Statement.RETURN_GENERATED_KEYS</code> 或 <code>Statement.NO_GENERATED_KEYS</code>，
     * 方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持带有 Statement.RETURN_GENERATED_KEYS 常量的此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     *
     * @since 1.4
     */
    boolean execute(String sql, int autoGeneratedKeys) throws SQLException;

    /**
     * 执行给定的 SQL 语句，该语句可能返回多个结果，并向驱动程序发出信号，指示给定数组中指示的自动生成键应可用于检索。
     * 此数组包含目标表中包含应可用于检索的自动生成键的列的索引。如果 SQL 语句不是 <code>INSERT</code> 语句，
     * 或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略该数组。
     * <P>
     * 在某些（不常见）情况下，单个 SQL 语句可能返回多个结果集和/或更新计数。通常可以忽略这一点，除非 (1) 您正在执行一个已知可能返回多个结果的存储过程，
     * 或 (2) 您正在动态执行一个未知的 SQL 字符串。
     * <P>
     * <code>execute</code> 方法执行一个 SQL 语句并指示第一个结果的形式。然后必须使用 <code>getResultSet</code> 或 <code>getUpdateCount</code>
     * 方法检索结果，并使用 <code>getMoreResults</code> 方法移动到任何后续结果。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 任何 SQL 语句
     * @param columnIndexes 一个包含应通过调用 <code>getGeneratedKeys</code> 方法可用于检索的插入行中的列索引的数组
     * @return 如果第一个结果是 <code>ResultSet</code> 对象，则返回 <code>true</code>；
     *         如果是更新计数或没有结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在已关闭的 <code>Statement</code> 上调用，传递给此方法的 <code>int</code> 数组中的元素不是有效的列索引，
     * 方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     *
     * @since 1.4
     */
    boolean execute(String sql, int columnIndexes[]) throws SQLException;

    /**
     * 执行给定的 SQL 语句，该语句可能返回多个结果，并向驱动程序发出信号，指示给定数组中指示的自动生成键应可用于检索。
     * 此数组包含目标表中包含应可用于检索的自动生成键的列的名称。如果 SQL 语句不是 <code>INSERT</code> 语句，
     * 或是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），
     * 驱动程序将忽略该数组。
     * <P>
     * 在某些（不常见）情况下，单个 SQL 语句可能返回多个结果集和/或更新计数。通常可以忽略这一点，除非 (1) 您正在执行一个已知可能返回多个结果的存储过程，
     * 或 (2) 您正在动态执行一个未知的 SQL 字符串。
     * <P>
     * <code>execute</code> 方法执行一个 SQL 语句并指示第一个结果的形式。然后必须使用 <code>getResultSet</code> 或 <code>getUpdateCount</code>
     * 方法检索结果，并使用 <code>getMoreResults</code> 方法移动到任何后续结果。
     *<p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     * @param sql 任何 SQL 语句
     * @param columnNames 一个包含应通过调用 <code>getGeneratedKeys</code> 方法可用于检索的插入行中的列名称的数组
     * @return 如果下一个结果是 <code>ResultSet</code> 对象，则返回 <code>true</code>；
     *         如果是更新计数或没有更多结果，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在已关闭的 <code>Statement</code> 上调用，传递给此方法的 <code>String</code> 数组中的元素不是有效的列名称，
     * 方法在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已超过，并且至少尝试取消当前运行的 {@code Statement} 时
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * @see #getGeneratedKeys
     *
     * @since 1.4
     */
    boolean execute(String sql, String columnNames[]) throws SQLException;


               /**
     * 获取由这个 <code>Statement</code> 对象生成的 <code>ResultSet</code> 对象的结果集保持性。
     *
     * @return 要么是 <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>，要么是
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法被调用在已关闭的 <code>Statement</code> 上
     *
     * @since 1.4
     */
    int getResultSetHoldability() throws SQLException;

    /**
     * 检索此 <code>Statement</code> 对象是否已关闭。如果已调用 close 方法或自动关闭，则 <code>Statement</code> 被关闭。
     * @return 如果此 <code>Statement</code> 对象已关闭，则返回 true；如果仍然打开，则返回 false
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.6
     */
    boolean isClosed() throws SQLException;

        /**
         * 请求将 <code>Statement</code> 对象放入池中或不放入池中。指定的值是对语句池实现的提示，指示应用程序希望语句是否被池化。是否使用此提示取决于语句池管理器。
         * <p>
         * 语句的可池化值适用于驱动程序实现的内部语句缓存和应用程序服务器及其他应用程序实现的外部语句缓存。
         * <p>
         * 默认情况下，创建时 <code>Statement</code> 不可池化，而 <code>PreparedStatement</code> 和 <code>CallableStatement</code>
         * 可池化。
         * <p>
         * @param poolable              请求语句池化为 true，请求语句不池化为 false
         * <p>
         * @throws SQLException 如果此方法被调用在已关闭的
         * <code>Statement</code> 上
         * <p>
         * @since 1.6
         */
        void setPoolable(boolean poolable)
                throws SQLException;

        /**
         * 返回一个值，指示 <code>Statement</code>
         * 是否可池化。
         * <p>
         * @return              如果 <code>Statement</code>
         * 可池化，则返回 <code>true</code>；否则返回 <code>false</code>
         * <p>
         * @throws SQLException 如果此方法被调用在已关闭的
         * <code>Statement</code> 上
         * <p>
         * @since 1.6
         * <p>
         * @see java.sql.Statement#setPoolable(boolean) setPoolable(boolean)
         */
        boolean isPoolable()
                throws SQLException;

    //--------------------------JDBC 4.1 -----------------------------

    /**
     * 指定当所有依赖的结果集关闭时，此 {@code Statement} 将被关闭。如果执行 {@code Statement}
     * 没有生成任何结果集，此方法没有效果。
     * <p>
     * <strong>注意：</strong>多次调用 {@code closeOnCompletion} 不会切换此 {@code Statement} 的效果。但是，调用 {@code closeOnCompletion}
     * 会影响后续语句的执行，以及当前有打开的、依赖的结果集的语句。
     *
     * @throws SQLException 如果此方法被调用在已关闭的
     * {@code Statement} 上
     * @since 1.7
     */
    public void closeOnCompletion() throws SQLException;

    /**
     * 返回一个值，指示当所有依赖的结果集关闭时，此 {@code Statement} 是否将被关闭。
     * @return 如果所有依赖的结果集关闭时 {@code Statement} 将被关闭，则返回 {@code true}；否则返回 {@code false}
     * @throws SQLException 如果此方法被调用在已关闭的
     * {@code Statement} 上
     * @since 1.7
     */
    public boolean isCloseOnCompletion() throws SQLException;


    //--------------------------JDBC 4.2 -----------------------------

    /**
     * 以更新计数的形式获取当前结果；如果结果是 <code>ResultSet</code> 对象或没有更多结果，则返回 -1
     * 该方法应仅对每个结果调用一次。
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     *<p>
     * 默认实现将抛出 {@code UnsupportedOperationException}
     *
     * @return 以更新计数形式的当前结果；如果当前结果是 <code>ResultSet</code> 对象或没有更多结果，则返回 -1
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法被调用在已关闭的 <code>Statement</code> 上
     * @see #execute
     * @since 1.8
     */
    default long getLargeUpdateCount() throws SQLException {
        throw new UnsupportedOperationException("getLargeUpdateCount not implemented");
    }

    /**
     * 设置由这个 <code>Statement</code> 对象生成的任何
     * <code>ResultSet</code> 对象可以包含的最大行数。
     * 如果超过此限制，多余的
     * 行将被静默丢弃。
     * <p>
     * 当行数限制可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     *<p>
     * 默认实现将抛出 {@code UnsupportedOperationException}
     *
     * @param max 新的最大行数限制；零表示没有限制
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法被调用在已关闭的 <code>Statement</code> 上
     *            或条件 {@code max >= 0} 不满足
     * @see #getMaxRows
     * @since 1.8
     */
    default void setLargeMaxRows(long max) throws SQLException {
        throw new UnsupportedOperationException("setLargeMaxRows not implemented");
    }

    /**
     * 获取由这个
     * <code>Statement</code> 对象生成的
     * <code>ResultSet</code> 对象可以包含的最大行数。如果超过此限制，
     * 多余的行将被静默丢弃。
     * <p>
     * 当返回的行数限制可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     *<p>
     * 默认实现将返回 {@code 0}
     *
     * @return 由这个 <code>Statement</code> 对象生成的 <code>ResultSet</code>
     *         对象的当前最大行数；
     *         零表示没有限制
     * @exception SQLException 如果发生数据库访问错误或
     * this 方法被调用在已关闭的 <code>Statement</code> 上
     * @see #setMaxRows
     * @since 1.8
     */
    default long getLargeMaxRows() throws SQLException {
        return 0;
    }

    /**
     * 将一批命令提交给数据库执行，并且如果所有命令都成功执行，返回一个更新计数数组。
     * 返回数组中的 <code>long</code> 元素按命令在批处理中的顺序排列，该顺序是根据它们被添加到批处理中的顺序确定的。
     * 由方法 {@code executeLargeBatch} 返回的数组中的元素可以是以下之一：
     * <OL>
     * <LI>大于或等于零的数字 -- 表示命令成功处理，并且是一个更新计数，给出了命令执行时影响的数据库中的行数
     * <LI>值为 <code>SUCCESS_NO_INFO</code> -- 表示命令成功处理，但受影响的行数未知
     * <P>
     * 如果批处理更新中的一个命令执行失败，此方法将抛出 <code>BatchUpdateException</code>，JDBC
     * 驱动程序可能会或可能不会继续处理批处理中的剩余命令。但是，驱动程序的行为必须与特定的 DBMS 一致，要么总是继续处理命令，要么从不继续处理命令。如果驱动程序在命令失败后继续处理，
     * 方法 <code>BatchUpdateException.getLargeUpdateCounts</code>
     * 返回的数组将包含与批处理中的命令数量相同数量的元素，且至少有一个元素将是以下之一：
     *
     * <LI>值为 <code>EXECUTE_FAILED</code> -- 表示命令未能成功执行，仅当驱动程序在命令失败后继续处理命令时才会发生
     * </OL>
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     *<p>
     * 默认实现将抛出 {@code UnsupportedOperationException}
     *
     * @return 一个更新计数数组，包含批处理中每个命令的一个元素。数组中的元素按命令被添加到批处理中的顺序排列。
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法被调用在已关闭的 <code>Statement</code> 上或驱动程序不支持批处理语句。抛出 {@link BatchUpdateException}
     * （<code>SQLException</code> 的子类）如果批处理中的一个命令未能正确执行或尝试返回结果集。
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement}
     *
     * @see #addBatch
     * @see DatabaseMetaData#supportsBatchUpdates
     * @since 1.8
     */
    default long[] executeLargeBatch() throws SQLException {
        throw new UnsupportedOperationException("executeLargeBatch not implemented");
    }

    /**
     * 执行给定的 SQL 语句，该语句可能是 <code>INSERT</code>，
     * <code>UPDATE</code> 或 <code>DELETE</code> 语句或
     * 不返回任何内容的 SQL 语句，例如 SQL DDL 语句。
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     * <p>
     * <strong>注意：</strong>此方法不能在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     *<p>
     * 默认实现将抛出 {@code UnsupportedOperationException}
     *
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，
     * such as <code>INSERT</code>, <code>UPDATE</code> 或
     * <code>DELETE</code>；或不返回任何内容的 SQL 语句，例如 DDL 语句。
     *
     * @return 要么 (1) SQL 数据操作语言 (DML) 语句的行数
     *         或 (2) 不返回任何内容的 SQL 语句的 0
     *
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法被调用在已关闭的 <code>Statement</code> 上，给定的
     *            SQL 语句生成一个 <code>ResultSet</code> 对象，方法被调用在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement}
     * @since 1.8
     */
    default long executeLargeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException("executeLargeUpdate not implemented");
    }

    /**
     * 执行给定的 SQL 语句并向驱动程序发出信号，指示是否应使此 <code>Statement</code> 对象
     * 生成的自动生成的键可用于检索。驱动程序将忽略该标志，如果 SQL 语句
     * 不是 <code>INSERT</code> 语句，或能够返回
     * 自动生成的键的 SQL 语句（此类语句的列表是供应商特定的）。
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     * <p>
     * <strong>注意：</strong>此方法不能在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，
     * such as <code>INSERT</code>, <code>UPDATE</code> 或
     * <code>DELETE</code>；或不返回任何内容的 SQL 语句，例如 DDL 语句。
     *
     * @param autoGeneratedKeys 一个标志，指示是否应使自动生成的键可用于检索；
     *         以下常量之一：
     *         <code>Statement.RETURN_GENERATED_KEYS</code>
     *         <code>Statement.NO_GENERATED_KEYS</code>
     * @return 要么 (1) SQL 数据操作语言 (DML) 语句的行数
     *         或 (2) 不返回任何内容的 SQL 语句的 0
     *
     * @exception SQLException 如果发生数据库访问错误，
     *  this 方法被调用在已关闭的 <code>Statement</code> 上，给定的
     *            SQL 语句返回一个 <code>ResultSet</code> 对象，
     *            给定的常量不是允许的常量之一，方法被调用在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * this 方法与常量 Statement.RETURN_GENERATED_KEYS
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement}
     * @since 1.8
     */
    default long executeLargeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
    }

    /**
     * 执行给定的 SQL 语句并向驱动程序发出信号，指示应在给定数组中指示的自动生成的键可用于检索。此数组包含目标表中包含应使可用的自动生成的键的列的索引。驱动程序将忽略该数组，如果 SQL 语句
     * 不是 <code>INSERT</code> 语句，或能够返回
     * 自动生成的键的 SQL 语句（此类语句的列表是供应商特定的）。
     * <p>
     * 当返回的行数可能超过
     * {@link Integer#MAX_VALUE} 时，应使用此方法。
     * <p>
     * <strong>注意：</strong>此方法不能在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，
     * such as <code>INSERT</code>, <code>UPDATE</code> 或
     * <code>DELETE</code>；或不返回任何内容的 SQL 语句，例如 DDL 语句。
     *
     * @param columnIndexes 一个列索引数组，指示应从插入的行返回的列
     * @return 要么 (1) SQL 数据操作语言 (DML) 语句的行数
     *         或 (2) 不返回任何内容的 SQL 语句的 0
     *
     * @exception SQLException 如果发生数据库访问错误，
     * this 方法被调用在已关闭的 <code>Statement</code> 上，SQL
     * 语句返回一个 <code>ResultSet</code> 对象，提供给此方法的第二个参数
     * 不是有效的列索引的 <code>int</code> 数组，方法被调用在
     * <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上
     * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout}
     * 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement}
     * @since 1.8
     */
    default long executeLargeUpdate(String sql, int columnIndexes[]) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
    }


                /**
     * 执行给定的 SQL 语句并指示驱动程序在给定数组中指示的自动生成的键应可用于检索。此数组包含目标表中应可获取的自动生成键的列名。如果 SQL 语句不是 <code>INSERT</code> 语句，或者不是能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），驱动程序将忽略该数组。
     * <p>
     * 当返回的行数可能超过 {@link Integer#MAX_VALUE} 时，应使用此方法。
     * <p>
     * <strong>注意：</strong>此方法不能在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用。
     *<p>
     * 默认实现将抛出 {@code SQLFeatureNotSupportedException}
     *
     * @param sql 一个 SQL 数据操作语言 (DML) 语句，例如 <code>INSERT</code>、<code>UPDATE</code> 或 <code>DELETE</code>；或一个不返回任何内容的 SQL 语句，例如 DDL 语句。
     * @param columnNames 应从插入的行返回的列名数组
     * @return 对于 <code>INSERT</code>、<code>UPDATE</code> 或 <code>DELETE</code> 语句，返回行数；对于不返回任何内容的 SQL 语句，返回 0
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的 <code>Statement</code> 上调用，SQL 语句返回一个 <code>ResultSet</code> 对象，传递给此方法的第二个参数不是包含有效列名的 <code>String</code> 数组，或在 <code>PreparedStatement</code> 或 <code>CallableStatement</code> 上调用此方法
     * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @throws SQLTimeoutException 当驱动程序确定由 {@code setQueryTimeout} 方法指定的超时值已被超过，并至少尝试取消当前正在运行的 {@code Statement} 时
     * @since 1.8
     */
    default long executeLargeUpdate(String sql, String columnNames[])
            throws SQLException {
        throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
    }
}
