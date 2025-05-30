
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

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * <P>与特定数据库的连接（会话）。SQL 语句在连接的上下文中执行并返回结果。
 * <P>
 * 一个 <code>Connection</code> 对象的数据库能够提供描述其表、支持的 SQL 语法、存储过程、此连接的功能等信息。
 * 这些信息可以通过 <code>getMetaData</code> 方法获取。
 *
 * <P><B>注意：</B>当配置 <code>Connection</code> 时，JDBC 应用程序应使用适当的 <code>Connection</code> 方法，例如
 * <code>setAutoCommit</code> 或 <code>setTransactionIsolation</code>。
 * 应用程序不应直接调用 SQL 命令来更改连接的配置，当有可用的 JDBC 方法时。默认情况下，<code>Connection</code> 对象处于
 * 自动提交模式，这意味着它在执行每个语句后自动提交更改。如果禁用了自动提交模式，则必须显式调用 <code>commit</code> 方法
 * 以提交更改；否则，数据库更改将不会保存。
 * <P>
 * 使用 JDBC 2.1 核心 API 创建的新 <code>Connection</code> 对象最初关联一个空的类型映射。用户可以在该类型映射中输入
 * 自定义映射以映射 UDT。当使用 <code>ResultSet.getObject</code> 方法从数据源检索 UDT 时，<code>getObject</code> 方法
 * 将检查连接的类型映射，以查看是否有该 UDT 的条目。如果有，<code>getObject</code> 方法将 UDT 映射到指定的类。
 * 如果没有条目，UDT 将使用标准映射。
 * <p>
 * 用户可以创建一个新的类型映射，这是一个 <code>java.util.Map</code> 对象，在其中输入条目，并将其传递给可以执行自定义映射的
 * <code>java.sql</code> 方法。在这种情况下，该方法将使用给定的类型映射而不是连接关联的类型映射。
 * <p>
 * 例如，以下代码片段指定 SQL 类型 <code>ATHLETES</code> 将映射到 Java 编程语言中的类 <code>Athletes</code>。
 * 代码片段检索 <code>Connection</code> 对象 <code>con</code> 的类型映射，将条目插入其中，然后将带有新条目的类型映射设置为连接的类型映射。
 * <pre>
 *      java.util.Map map = con.getTypeMap();
 *      map.put("mySchemaName.ATHLETES", Class.forName("Athletes"));
 *      con.setTypeMap(map);
 * </pre>
 *
 * @see DriverManager#getConnection
 * @see Statement
 * @see ResultSet
 * @see DatabaseMetaData
 */
public interface Connection  extends Wrapper, AutoCloseable {

    /**
     * 创建一个用于向数据库发送 SQL 语句的 <code>Statement</code> 对象。
     * 通常使用 <code>Statement</code> 对象执行没有参数的 SQL 语句。如果相同的 SQL 语句执行多次，
     * 使用 <code>PreparedStatement</code> 对象可能更高效。
     * <P>
     * 使用返回的 <code>Statement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>，
     * 并且并发级别为 <code>CONCUR_READ_ONLY</code>。创建的结果集的保持性可以通过调用 {@link #getHoldability} 确定。
     *
     * @return 一个新的默认 <code>Statement</code> 对象
     * @exception SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     */
    Statement createStatement() throws SQLException;

    /**
     * 创建一个用于向数据库发送带参数的 SQL 语句的 <code>PreparedStatement</code> 对象。
     * <P>
     * 带或不带 IN 参数的 SQL 语句可以预编译并存储在 <code>PreparedStatement</code> 对象中。然后可以使用此对象高效地多次执行该语句。
     *
     * <P><B>注意：</B>此方法针对需要预编译的带参数的 SQL 语句进行了优化。如果驱动程序支持预编译，
     * <code>prepareStatement</code> 方法将把语句发送到数据库进行预编译。某些驱动程序可能不支持预编译。在这种情况下，
     * 语句可能不会在 <code>PreparedStatement</code> 对象执行之前发送到数据库。这不会直接影响用户；然而，它确实会影响
 * 哪些方法会抛出某些 <code>SQLException</code> 对象。
     * <P>
     * 使用返回的 <code>PreparedStatement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>，
     * 并且并发级别为 <code>CONCUR_READ_ONLY</code>。创建的结果集的保持性可以通过调用 {@link #getHoldability} 确定。
     *
     * @param sql 可能包含一个或多个 '?' IN 参数占位符的 SQL 语句
     * @return 一个包含预编译 SQL 语句的新默认 <code>PreparedStatement</code> 对象
     * @exception SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     */
    PreparedStatement prepareStatement(String sql)
        throws SQLException;

    /**
     * 创建一个用于调用数据库存储过程的 <code>CallableStatement</code> 对象。
     * <code>CallableStatement</code> 对象提供了设置其 IN 和 OUT 参数的方法，
     * 以及调用存储过程的方法。
     *
     * <P><B>注意：</B>此方法针对存储过程调用语句进行了优化。某些驱动程序可能在调用 <code>prepareCall</code> 方法时
     * 将调用语句发送到数据库；其他驱动程序可能在 <code>CallableStatement</code> 对象执行时才发送。这不会直接影响用户；
     * 然而，它确实会影响哪些方法会抛出某些 <code>SQLException</code> 对象。
     * <P>
     * 使用返回的 <code>CallableStatement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>，
     * 并且并发级别为 <code>CONCUR_READ_ONLY</code>。创建的结果集的保持性可以通过调用 {@link #getHoldability} 确定。
     *
     * @param sql 可能包含一个或多个 '?' 参数占位符的 SQL 语句。通常此语句使用 JDBC 调用转义语法指定。
     * @return 一个包含预编译 SQL 语句的新默认 <code>CallableStatement</code> 对象
     * @exception SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     */
    CallableStatement prepareCall(String sql) throws SQLException;

    /**
     * 将给定的 SQL 语句转换为系统的本机 SQL 语法。
     * 驱动程序可能在发送之前将 JDBC SQL 语法转换为其系统的本机 SQL 语法。此方法返回驱动程序将要发送的语句的本机形式。
     *
     * @param sql 可能包含一个或多个 '?' 参数占位符的 SQL 语句
     * @return 该语句的本机形式
     * @exception SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     */
    String nativeSQL(String sql) throws SQLException;

    /**
     * 将此连接的自动提交模式设置为给定状态。
     * 如果连接处于自动提交模式，则其所有 SQL 语句将作为单独的事务执行并提交。否则，其 SQL 语句将分组为
     * 由调用 <code>commit</code> 方法或 <code>rollback</code> 方法终止的事务。默认情况下，新连接处于自动提交模式。
     * <P>
     * 提交发生在语句完成时。语句完成的时间取决于 SQL 语句的类型：
     * <ul>
     * <li>对于 DML 语句，如 Insert、Update 或 Delete 以及 DDL 语句，语句在执行完毕后即完成。
     * <li>对于 Select 语句，语句在关联的结果集关闭时完成。
     * <li>对于 <code>CallableStatement</code> 对象或返回多个结果的语句，语句在所有关联的结果集关闭，所有更新计数和输出参数检索完毕后完成。
     *</ul>
     * <P>
     * <B>注意：</B>如果在此方法调用期间更改了自动提交模式，则事务将被提交。如果 <code>setAutoCommit</code> 被调用但自动提交模式未更改，
     * 则调用是一个空操作。
     *
     * @param autoCommit <code>true</code> 启用自动提交模式；<code>false</code> 禁用自动提交模式
     * @exception SQLException 如果发生数据库访问错误，<code>setAutoCommit(true)</code> 在参与分布式事务时被调用，
     * 或在此方法调用时连接已关闭
     * @see #getAutoCommit
     */
    void setAutoCommit(boolean autoCommit) throws SQLException;

    /**
     * 检索此 <code>Connection</code> 对象的当前自动提交模式。
     *
     * @return 此 <code>Connection</code> 对象的自动提交模式的当前状态
     * @exception SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     * @see #setAutoCommit
     */
    boolean getAutoCommit() throws SQLException;

    /**
     * 使自上次提交/回滚以来的所有更改永久化，并释放此 <code>Connection</code> 对象当前持有的所有数据库锁。
     * 此方法仅应在禁用了自动提交模式时使用。
     *
     * @exception SQLException 如果发生数据库访问错误，此方法在参与分布式事务时被调用，如果此方法在连接已关闭时被调用或此
     *            <code>Connection</code> 对象处于自动提交模式
     * @see #setAutoCommit
     */
    void commit() throws SQLException;

    /**
     * 撤销当前事务中的所有更改，并释放此 <code>Connection</code> 对象当前持有的所有数据库锁。
     * 此方法仅应在禁用了自动提交模式时使用。
     *
     * @exception SQLException 如果发生数据库访问错误，此方法在参与分布式事务时被调用，如果此方法在连接已关闭时被调用或此
     *            <code>Connection</code> 对象处于自动提交模式
     * @see #setAutoCommit
     */
    void rollback() throws SQLException;

    /**
     * 立即释放此 <code>Connection</code> 对象的数据库和 JDBC 资源，而不是等待它们自动释放。
     * <P>
     * 如果 <code>Connection</code> 对象已关闭，则调用 <code>close</code> 方法是一个空操作。
     * <P>
     * 强烈建议应用程序在调用 <code>close</code> 方法之前显式提交或回滚活动事务。如果在有活动事务时调用 <code>close</code> 方法，
     * 结果是实现定义的。
     * <P>
     *
     * @exception SQLException 如果发生数据库访问错误
     */
    void close() throws SQLException;

    /**
     * 检索此 <code>Connection</code> 对象是否已关闭。如果已调用 <code>close</code> 方法或发生某些致命错误，则连接已关闭。
     * 保证只有在调用 <code>Connection.close</code> 方法后调用此方法时才会返回 <code>true</code>。
     * <P>
     * 通常无法调用此方法来确定连接是否有效或无效。典型的客户端可以通过捕获在尝试执行操作时可能抛出的任何异常来确定连接是否无效。
     *
     * @return 如果此 <code>Connection</code> 对象已关闭，则返回 <code>true</code>；如果仍处于打开状态，则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isClosed() throws SQLException;

    //======================================================================
    // 高级功能：

    /**
     * 检索一个包含此 <code>Connection</code> 对象所代表的数据库的元数据的 <code>DatabaseMetaData</code> 对象。
     * 元数据包括关于数据库的表、支持的 SQL 语法、存储过程、此连接的功能等信息。
     *
     * @return 一个 <code>DatabaseMetaData</code> 对象，用于此 <code>Connection</code> 对象
     * @exception  SQLException 如果发生数据库访问错误或在此方法调用时连接已关闭
     */
    DatabaseMetaData getMetaData() throws SQLException;

    /**
     * 将此连接设置为只读模式，作为提示驱动程序启用数据库优化。
     *
     * <P><B>注意：</B>此方法不能在事务期间调用。
     *
     * @param readOnly <code>true</code> 启用只读模式；<code>false</code> 禁用只读模式
     * @exception SQLException 如果发生数据库访问错误，此方法在连接已关闭时被调用或此方法在事务期间被调用
     */
    void setReadOnly(boolean readOnly) throws SQLException;


                /**
     * 获取此 <code>Connection</code>
     * 对象是否处于只读模式。
     *
     * @return 如果此 <code>Connection</code> 对象
     *         是只读的，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     */
    boolean isReadOnly() throws SQLException;

    /**
     * 设置给定的目录名称，以选择
     * 此 <code>Connection</code> 对象的数据库中的一个子空间
     * 来工作。
     * <P>
     * 如果驱动程序不支持目录，它将
     * 静默忽略此请求。
     * <p>
     * 调用 {@code setCatalog} 对先前创建或准备的
     * {@code Statement} 对象没有影响。DBMS 是否在调用
     * {@code Connection} 方法 {@code prepareStatement} 或 {@code prepareCall} 时立即进行准备操作是实现定义的。
     * 为了最大可移植性，应在创建或准备 {@code Statement} 之前调用
     * {@code setCatalog}。
     *
     * @param catalog 要工作的目录（此
     *        <code>Connection</code> 对象的数据库中的子空间）的名称
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @see #getCatalog
     */
    void setCatalog(String catalog) throws SQLException;

    /**
     * 获取此 <code>Connection</code> 对象的当前目录名称。
     *
     * @return 当前目录名称或如果没有目录则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @see #setCatalog
     */
    String getCatalog() throws SQLException;

    /**
     * 表示不支持事务的常量。
     */
    int TRANSACTION_NONE             = 0;

    /**
     * 表示可能发生脏读、不可重复读和幻读的常量。
     * 此级别允许一个事务读取另一个事务尚未提交的行（“脏读”）。如果这些更改被回滚，
     * 第二个事务将检索到无效的行。
     */
    int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     * 表示禁止脏读；可能发生不可重复读和幻读的常量。
     * 此级别仅禁止事务读取未提交更改的行。
     */
    int TRANSACTION_READ_COMMITTED   = 2;

    /**
     * 表示禁止脏读和不可重复读；可能发生幻读的常量。
     * 此级别禁止事务读取未提交更改的行，并且还禁止以下情况：一个事务读取一行，
     * 第二个事务更改该行，第一个事务重新读取该行，第二次读取时获取不同的值
     * （“不可重复读”）。
     */
    int TRANSACTION_REPEATABLE_READ  = 4;

    /**
     * 表示禁止脏读、不可重复读和幻读的常量。
     * 此级别包括 <code>TRANSACTION_REPEATABLE_READ</code> 中的禁止项，并进一步禁止以下情况：
     * 一个事务读取满足 <code>WHERE</code> 条件的所有行，第二个事务插入满足该 <code>WHERE</code> 条件的行，
     * 第一个事务重新读取相同的条件，检索到额外的“幻”行。
     */
    int TRANSACTION_SERIALIZABLE     = 8;

    /**
     * 尝试将此 <code>Connection</code> 对象的事务隔离级别更改为给定的级别。
     * 接口中定义的常量是可能的事务隔离级别。
     * <P>
     * <B>注意：</B> 如果在事务期间调用此方法，结果是实现定义的。
     *
     * @param level 以下 <code>Connection</code> 常量之一：
     *        <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
     *        <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *        <code>Connection.TRANSACTION_REPEATABLE_READ</code> 或
     *        <code>Connection.TRANSACTION_SERIALIZABLE</code>。
     *        （注意 <code>Connection.TRANSACTION_NONE</code> 不能使用，
     *        因为它表示不支持事务。）
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用
     *            或给定的参数不是 <code>Connection</code> 常量
     * @see DatabaseMetaData#supportsTransactionIsolationLevel
     * @see #getTransactionIsolation
     */
    void setTransactionIsolation(int level) throws SQLException;

    /**
     * 获取此 <code>Connection</code> 对象的当前
     * 事务隔离级别。
     *
     * @return 当前事务隔离级别，将是以下常量之一：
     *        <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
     *        <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *        <code>Connection.TRANSACTION_REPEATABLE_READ</code>,
     *        <code>Connection.TRANSACTION_SERIALIZABLE</code> 或
     *        <code>Connection.TRANSACTION_NONE</code>。
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @see #setTransactionIsolation
     */
    int getTransactionIsolation() throws SQLException;

    /**
     * 获取此 <code>Connection</code> 对象报告的第一个警告。
     * 如果有多个警告，后续警告将链接到第一个警告，
     * 可以通过调用警告的 <code>SQLWarning.getNextWarning</code> 方法来检索。
     * <P>
     * 此方法不能在已关闭的连接上调用；这样做将导致
     * 抛出 <code>SQLException</code>。
     *
     * <P><B>注意：</B> 后续警告将链接到此
     * SQLWarning。
     *
     * @return 第一个 <code>SQLWarning</code> 对象或如果没有警告则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误或
     *            此方法在已关闭的连接上调用
     * @see SQLWarning
     */
    SQLWarning getWarnings() throws SQLException;

    /**
     * 清除此 <code>Connection</code> 对象报告的所有警告。
     * 调用此方法后，<code>getWarnings</code> 方法将返回 <code>null</code>，
     * 直到为该 <code>Connection</code> 对象报告新的警告。
     *
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     */
    void clearWarnings() throws SQLException;


    //--------------------------JDBC 2.0-----------------------------

    /**
     * 创建一个将生成具有给定类型和并发性的
     * <code>ResultSet</code> 对象的 <code>Statement</code> 对象。
     * 此方法与上面的 <code>createStatement</code> 方法相同，但允许覆盖默认的结果集
     * 类型和并发性。创建的结果集的保持性可以通过调用 {@link #getHoldability} 来确定。
     *
     * @param resultSetType 结果集类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 并发类型；可以是
     *        <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *        <code>ResultSet.CONCUR_UPDATABLE</code>
     * @return 一个新的 <code>Statement</code> 对象，将生成具有给定类型和
     *         并发性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用
     *         或给定的参数不是 <code>ResultSet</code> 常量表示的类型和并发性
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型和结果集并发性。
     * @since 1.2
     */
    Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException;

    /**
     *
     * 创建一个将生成具有给定类型和并发性的
     * <code>ResultSet</code> 对象的 <code>PreparedStatement</code> 对象。
     * 此方法与上面的 <code>prepareStatement</code> 方法相同，但允许覆盖默认的结果集
     * 类型和并发性。创建的结果集的保持性可以通过调用 {@link #getHoldability} 来确定。
     *
     * @param sql 一个 <code>String</code> 对象，是发送到数据库的 SQL 语句；可能包含一个或多个 '?' IN
     *            参数
     * @param resultSetType 结果集类型；可以是
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 并发类型；可以是
     *         <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @return 一个新的 <code>PreparedStatement</code> 对象，包含预编译的 SQL 语句，将生成具有给定类型和并发性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用
     *         或给定的参数不是 <code>ResultSet</code> 常量表示的类型和并发性
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型和结果集并发性。
     * @since 1.2
     */
    PreparedStatement prepareStatement(String sql, int resultSetType,
                                       int resultSetConcurrency)
        throws SQLException;

    /**
     * 创建一个将生成具有给定类型和并发性的
     * <code>ResultSet</code> 对象的 <code>CallableStatement</code> 对象。
     * 此方法与上面的 <code>prepareCall</code> 方法相同，但允许覆盖默认的结果集
     * 类型和并发性。创建的结果集的保持性可以通过调用 {@link #getHoldability} 来确定。
     *
     * @param sql 一个 <code>String</code> 对象，是发送到数据库的 SQL 语句；可能包含一个或多个 '?' 参数
     * @param resultSetType 结果集类型；可以是
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 并发类型；可以是
     *         <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @return 一个新的 <code>CallableStatement</code> 对象，包含预编译的 SQL 语句，将生成具有给定类型和并发性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用
     *         或给定的参数不是 <code>ResultSet</code> 常量表示的类型和并发性
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型和结果集并发性。
     * @since 1.2
     */
    CallableStatement prepareCall(String sql, int resultSetType,
                                  int resultSetConcurrency) throws SQLException;

    /**
     * 获取与此 <code>Connection</code> 对象关联的 <code>Map</code> 对象。
     * 除非应用程序已添加条目，否则返回的类型映射将是空的。
     * <p>
     * 必须在调用 <code>setTypeMap</code> 之后对从
     *  <code>getTypeMap</code> 返回的 <code>Map</code> 对象进行更改，因为 JDBC 驱动程序可能会创建 <code>Map</code> 对象的内部副本：
     *
     * <pre>
     *      Map&lt;String,Class&lt;?&gt;&gt; myMap = con.getTypeMap();
     *      myMap.put("mySchemaName.ATHLETES", Athletes.class);
     *      con.setTypeMap(myMap);
     * </pre>
     * @return 与此 <code>Connection</code> 对象关联的 <code>java.util.Map</code> 对象
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     * @see #setTypeMap
     */
    java.util.Map<String,Class<?>> getTypeMap() throws SQLException;

    /**
     * 将给定的 <code>TypeMap</code> 对象安装为此 <code>Connection</code> 对象的类型映射。
     * 类型映射将用于自定义映射 SQL 结构化类型和区分类型。
     *<p>
     * 必须在调用 <code>setMap</code> 之前设置 <code>TypeMap</code> 的值，因为 JDBC 驱动程序可能会创建 <code>TypeMap</code> 的内部副本：
     *
     * <pre>
     *      Map myMap&lt;String,Class&lt;?&gt;&gt; = new HashMap&lt;String,Class&lt;?&gt;&gt;();
     *      myMap.put("mySchemaName.ATHLETES", Athletes.class);
     *      con.setTypeMap(myMap);
     * </pre>
     * @param map 要安装的 <code>java.util.Map</code> 对象，作为此 <code>Connection</code>
     *        对象的默认类型映射的替代
     * @exception SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用或
     *        给定的参数不是 <code>java.util.Map</code> 对象
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.2
     * @see #getTypeMap
     */
    void setTypeMap(java.util.Map<String,Class<?>> map) throws SQLException;

    //--------------------------JDBC 3.0-----------------------------


    /**
     * 将使用此 <code>Connection</code> 对象创建的 <code>ResultSet</code> 对象的默认保持性更改为给定的
     * 保持性。可以通过调用
     * {@link DatabaseMetaData#getResultSetHoldability} 来确定 <code>ResultSet</code> 对象的默认保持性。
     *
     * @param holdability 一个 <code>ResultSet</code> 保持性常量；可以是
     *        <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *        <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用，或给定的参数
     *         不是 <code>ResultSet</code> 常量表示的保持性
     * @exception SQLFeatureNotSupportedException 如果给定的保持性不支持
     * @see #getHoldability
     * @see DatabaseMetaData#getResultSetHoldability
     * @see ResultSet
     * @since 1.4
     */
    void setHoldability(int holdability) throws SQLException;


                /**
     * 获取使用此 <code>Connection</code> 对象创建的 <code>ResultSet</code> 对象的当前保持性。
     *
     * @return 保持性，为以下之一：
     *        <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *        <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @see #setHoldability
     * @see DatabaseMetaData#getResultSetHoldability
     * @see ResultSet
     * @since 1.4
     */
    int getHoldability() throws SQLException;

    /**
     * 在当前事务中创建一个未命名的保存点，并返回表示该保存点的新 <code>Savepoint</code> 对象。
     *
     *<p> 如果在没有活动事务的情况下调用 setSavepoint，则将在新创建的保存点处开始一个事务。
     *
     * @return 新的 <code>Savepoint</code> 对象
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在分布式事务中调用，
     * 此方法在已关闭的连接上调用
     *            或此 <code>Connection</code> 对象当前处于自动提交模式
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see Savepoint
     * @since 1.4
     */
    Savepoint setSavepoint() throws SQLException;

    /**
     * 在当前事务中创建一个具有给定名称的保存点，并返回表示该保存点的新 <code>Savepoint</code> 对象。
     *
     * <p> 如果在没有活动事务的情况下调用 setSavepoint，则将在新创建的保存点处开始一个事务。
     *
     * @param name 包含保存点名称的 <code>String</code>
     * @return 新的 <code>Savepoint</code> 对象
     * @exception SQLException 如果发生数据库访问错误，
          * 此方法在分布式事务中调用，
     * 此方法在已关闭的连接上调用
     *            或此 <code>Connection</code> 对象当前处于自动提交模式
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see Savepoint
     * @since 1.4
     */
    Savepoint setSavepoint(String name) throws SQLException;

    /**
     * 撤销给定 <code>Savepoint</code> 对象设置后所做的所有更改。
     * <P>
     * 仅当自动提交已禁用时，才应使用此方法。
     *
     * @param savepoint 要回滚到的 <code>Savepoint</code> 对象
     * @exception SQLException 如果发生数据库访问错误，
     * 此方法在分布式事务中调用，
     * 此方法在已关闭的连接上调用，
     *            给定的 <code>Savepoint</code> 对象不再有效，
     *            或此 <code>Connection</code> 对象当前处于自动提交模式
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see Savepoint
     * @see #rollback
     * @since 1.4
     */
    void rollback(Savepoint savepoint) throws SQLException;

    /**
     * 从当前事务中移除指定的 <code>Savepoint</code> 对象及其后续的 <code>Savepoint</code> 对象。移除保存点后，任何对该保存点的引用都将导致抛出 <code>SQLException</code>。
     *
     * @param savepoint 要移除的 <code>Savepoint</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此
     *  方法在已关闭的连接上调用或
     *            给定的 <code>Savepoint</code> 对象不是当前事务中的有效保存点
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void releaseSavepoint(Savepoint savepoint) throws SQLException;

    /**
     * 创建一个 <code>Statement</code> 对象，该对象将生成具有给定类型、并发性和保持性的 <code>ResultSet</code> 对象。
     * 此方法与上述 <code>createStatement</code> 方法相同，但允许覆盖默认的结果集类型、并发性和保持性。
     *
     * @param resultSetType 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return 一个新的 <code>Statement</code> 对象，该对象将生成具有给定类型、并发性和保持性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此
     * 方法在已关闭的连接上调用
     *            或给定的参数不是表示类型、并发性和保持性的 <code>ResultSet</code> 常量
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型、结果集保持性和结果集并发性。
     * @see ResultSet
     * @since 1.4
     */
    Statement createStatement(int resultSetType, int resultSetConcurrency,
                              int resultSetHoldability) throws SQLException;

    /**
     * 创建一个 <code>PreparedStatement</code> 对象，该对象将生成具有给定类型、并发性和保持性的 <code>ResultSet</code> 对象。
     * <P>
     * 此方法与上述 <code>prepareStatement</code> 方法相同，但允许覆盖默认的结果集类型、并发性和保持性。
     *
     * @param sql 要发送到数据库的 SQL 语句；可能包含一个或多个 '?' IN 参数
     * @param resultSetType 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return 一个新的 <code>PreparedStatement</code> 对象，该对象包含预编译的 SQL 语句，并将生成具有给定类型、并发性和保持性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此
     * 方法在已关闭的连接上调用
     *            或给定的参数不是表示类型、并发性和保持性的 <code>ResultSet</code> 常量
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型、结果集保持性和结果集并发性。
     * @see ResultSet
     * @since 1.4
     */
    PreparedStatement prepareStatement(String sql, int resultSetType,
                                       int resultSetConcurrency, int resultSetHoldability)
        throws SQLException;

    /**
     * 创建一个 <code>CallableStatement</code> 对象，该对象将生成具有给定类型和并发性的 <code>ResultSet</code> 对象。
     * 此方法与上述 <code>prepareCall</code> 方法相同，但允许覆盖默认的结果集类型、结果集并发类型和保持性。
     *
     * @param sql 要发送到数据库的 SQL 语句；可能包含一个或多个 '?' 参数
     * @param resultSetType 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.CONCUR_READ_ONLY</code> 或
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability 以下 <code>ResultSet</code> 常量之一：
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return 一个新的 <code>CallableStatement</code> 对象，该对象包含预编译的 SQL 语句，并将生成具有给定类型、并发性和保持性的 <code>ResultSet</code> 对象
     * @exception SQLException 如果发生数据库访问错误，此
     * 方法在已关闭的连接上调用
     *            或给定的参数不是表示类型、并发性和保持性的 <code>ResultSet</code> 常量
      * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法或此方法不支持指定的结果集类型、结果集保持性和结果集并发性。
     * @see ResultSet
     * @since 1.4
     */
    CallableStatement prepareCall(String sql, int resultSetType,
                                  int resultSetConcurrency,
                                  int resultSetHoldability) throws SQLException;


    /**
     * 创建一个默认的 <code>PreparedStatement</code> 对象，该对象具有返回自动生成的键的能力。给定的常量告诉驱动程序是否应使自动生成的键可用于检索。如果 SQL 语句不是 <code>INSERT</code> 语句，或是一个能够返回自动生成的键的 SQL 语句（此类语句的列表是供应商特定的），则此参数将被忽略。
     * <P>
     * <B>注意：</B> 此方法针对受益于预编译的参数化 SQL 语句进行了优化。如果驱动程序支持预编译，则 <code>prepareStatement</code> 方法将把语句发送到数据库进行预编译。某些驱动程序可能不支持预编译。在这种情况下，语句可能不会在 <code>PreparedStatement</code> 对象执行之前发送到数据库。这不会直接影响用户，但会影响抛出某些 <code>SQLException</code> 的方法。
     * <P>
     * 使用返回的 <code>PreparedStatement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>，并发级别为 <code>CONCUR_READ_ONLY</code>。创建的结果集的保持性可以通过调用 {@link #getHoldability} 确定。
     *
     * @param sql 可能包含一个或多个 '?' IN 参数占位符的 SQL 语句
     * @param autoGeneratedKeys 一个标志，指示是否应返回自动生成的键；为以下之一：
     *        <code>Statement.RETURN_GENERATED_KEYS</code> 或
     *        <code>Statement.NO_GENERATED_KEYS</code>
     * @return 一个新的 <code>PreparedStatement</code> 对象，该对象包含预编译的 SQL 语句，并具有返回自动生成的键的能力
     * @exception SQLException 如果发生数据库访问错误，此
     *  方法在已关闭的连接上调用
     *         或给定的参数不是表示是否应返回自动生成的键的 <code>Statement</code> 常量
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法与 <code>Statement.RETURN_GENERATED_KEYS</code> 常量一起使用
     * @since 1.4
     */
    PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException;

    /**
     * 创建一个默认的 <code>PreparedStatement</code> 对象，该对象能够返回由给定数组指定的自动生成的键。此数组包含目标表中包含应返回的自动生成的键的列的索引。如果 SQL 语句不是 <code>INSERT</code> 语句，或是一个能够返回自动生成的键的 SQL 语句（此类语句的列表是供应商特定的），则驱动程序将忽略该数组。
     *<p>
     * 一个包含或不包含 IN 参数的 SQL 语句可以预编译并存储在一个 <code>PreparedStatement</code> 对象中。然后可以使用此对象高效地多次执行该语句。
     * <P>
     * <B>注意：</B> 此方法针对受益于预编译的参数化 SQL 语句进行了优化。如果驱动程序支持预编译，则 <code>prepareStatement</code> 方法将把语句发送到数据库进行预编译。某些驱动程序可能不支持预编译。在这种情况下，语句可能不会在 <code>PreparedStatement</code> 对象执行之前发送到数据库。这不会直接影响用户，但会影响抛出某些 <code>SQLException</code> 的方法。
     * <P>
     * 使用返回的 <code>PreparedStatement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>，并发级别为 <code>CONCUR_READ_ONLY</code>。创建的结果集的保持性可以通过调用 {@link #getHoldability} 确定。
     *
     * @param sql 可能包含一个或多个 '?' IN 参数占位符的 SQL 语句
     * @param columnIndexes 包含应从插入的行或行返回的列的索引的数组
     * @return 一个新的 <code>PreparedStatement</code> 对象，该对象包含预编译的语句，并能够返回由给定列索引数组指定的自动生成的键
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.4
     */
    PreparedStatement prepareStatement(String sql, int columnIndexes[])
        throws SQLException;


                /**
     * 创建一个默认的 <code>PreparedStatement</code> 对象，该对象能够返回由给定数组指定的自动生成的键。
     * 该数组包含目标表中包含应返回的自动生成键的列名。
     * 如果 SQL 语句不是 <code>INSERT</code> 语句，或者是一个能够返回自动生成键的 SQL 语句（此类语句的列表是供应商特定的），则驱动程序将忽略该数组。
     * <P>
     * 带有或不带 IN 参数的 SQL 语句可以被预编译并存储在一个 <code>PreparedStatement</code> 对象中。然后可以使用此对象高效地多次执行该语句。
     * <P>
     * <B>注意：</B>此方法针对从预编译中受益的参数化 SQL 语句进行了优化。如果驱动程序支持预编译，
     * <code>prepareStatement</code> 方法将把语句发送到数据库进行预编译。有些驱动程序可能不支持预编译。在这种情况下，语句可能
     * 不会发送到数据库，直到 <code>PreparedStatement</code> 对象被执行。这不会直接影响用户；但是，它确实影响哪些方法会抛出某些 SQLException。
     * <P>
     * 使用返回的 <code>PreparedStatement</code> 对象创建的结果集默认类型为 <code>TYPE_FORWARD_ONLY</code>
     * 并且并发级别为 <code>CONCUR_READ_ONLY</code>。可以通过调用 {@link #getHoldability} 来确定创建的结果集的保持性。
     *
     * @param sql 一个可能包含一个或多个 '?' IN 参数占位符的 SQL 语句
     * @param columnNames 一个列名数组，指示应从插入的行或行中返回的列
     * @return 一个新的 <code>PreparedStatement</code> 对象，包含预编译的语句，能够返回由给定列名数组指定的自动生成键
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     *
     * @since 1.4
     */
    PreparedStatement prepareStatement(String sql, String columnNames[])
        throws SQLException;

    /**
     * 构建一个实现 <code>Clob</code> 接口的对象。返回的对象最初不包含任何数据。可以使用 <code>Clob</code> 接口的 <code>setAsciiStream</code>、
     * <code>setCharacterStream</code> 和 <code>setString</code> 方法向 <code>Clob</code> 中添加数据。
     * @return 实现 <code>Clob</code> 接口的对象
     * @throws SQLException 如果无法构建实现 <code>Clob</code> 接口的对象，此方法在已关闭的连接上调用或发生数据库访问错误。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此数据类型
     *
     * @since 1.6
     */
    Clob createClob() throws SQLException;

    /**
     * 构建一个实现 <code>Blob</code> 接口的对象。返回的对象最初不包含任何数据。可以使用 <code>Blob</code> 接口的 <code>setBinaryStream</code> 和
     * <code>setBytes</code> 方法向 <code>Blob</code> 中添加数据。
     * @return 实现 <code>Blob</code> 接口的对象
     * @throws SQLException 如果无法构建实现 <code>Blob</code> 接口的对象，此方法在已关闭的连接上调用或发生数据库访问错误。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此数据类型
     *
     * @since 1.6
     */
    Blob createBlob() throws SQLException;

    /**
     * 构建一个实现 <code>NClob</code> 接口的对象。返回的对象最初不包含任何数据。可以使用 <code>NClob</code> 接口的 <code>setAsciiStream</code>、
     * <code>setCharacterStream</code> 和 <code>setString</code> 方法向 <code>NClob</code> 中添加数据。
     * @return 实现 <code>NClob</code> 接口的对象
     * @throws SQLException 如果无法构建实现 <code>NClob</code> 接口的对象，此方法在已关闭的连接上调用或发生数据库访问错误。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此数据类型
     *
     * @since 1.6
     */
    NClob createNClob() throws SQLException;

    /**
     * 构建一个实现 <code>SQLXML</code> 接口的对象。返回的对象最初不包含任何数据。可以使用 <code>SQLXML</code> 接口的 <code>createXmlStreamWriter</code> 对象和
     * <code>setString</code> 方法向 <code>SQLXML</code> 对象中添加数据。
     * @return 实现 <code>SQLXML</code> 接口的对象
     * @throws SQLException 如果无法构建实现 <code>SQLXML</code> 接口的对象，此方法在已关闭的连接上调用或发生数据库访问错误。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此数据类型
     * @since 1.6
     */
    SQLXML createSQLXML() throws SQLException;

        /**
         * 如果连接未关闭且仍然有效，则返回 true。
         * 驱动程序应当在调用此方法时提交查询或使用其他机制来验证连接仍然有效。
         * <p>
         * 驱动程序提交的用于验证连接的查询应当在当前事务的上下文中执行。
         *
         * @param timeout - 等待数据库操作完成的时间，以秒为单位。如果
         * 超时时间到期而操作尚未完成，此方法返回 false。值为
         * 0 表示不对数据库操作应用超时。
         * <p>
         * @return 如果连接有效，则返回 true，否则返回 false
         * @exception SQLException 如果为 <code>timeout</code> 提供的值小于 0
         * @since 1.6
         *
         * @see java.sql.DatabaseMetaData#getClientInfoProperties
         */
         boolean isValid(int timeout) throws SQLException;

        /**
         * 将指定名称的客户端信息属性的值设置为指定的值。
         * <p>
         * 应用程序可以使用 <code>DatabaseMetaData.getClientInfoProperties</code>
         * 方法来确定驱动程序支持的客户端信息属性及其每个属性的最大长度。
         * <p>
         * 驱动程序将指定的值存储在数据库中的合适位置。例如，在特殊寄存器、会话参数或系统表列中。为了提高效率，驱动程序可能会延迟设置数据库中的值，直到下一次执行或准备语句。除了将客户端信息存储在数据库中的合适位置外，这些方法不应以任何方式改变连接的行为。提供的值仅用于记账、诊断和调试目的。
         * <p>
         * 如果驱动程序不识别指定的客户端信息名称，驱动程序应生成警告。
         * <p>
         * 如果提供给此方法的值大于属性的最大长度，驱动程序可以截断值并生成警告，或者生成 <code>SQLClientInfoException</code>。如果驱动程序生成 <code>SQLClientInfoException</code>，则指定的值未设置在连接上。
         * <p>
         * 以下是标准的客户端信息属性。驱动程序不需要支持这些属性，但是如果驱动程序支持可以由标准属性描述的客户端信息属性，应使用标准属性名称。
         *
         * <ul>
         * <li>ApplicationName - 当前使用连接的应用程序的名称</li>
         * <li>ClientUser - 使用连接的应用程序正在为哪个用户执行工作。这可能与用于建立连接的用户名不同。</li>
         * <li>ClientHostname - 使用连接的应用程序正在运行的计算机的主机名。</li>
         * </ul>
         * <p>
         * @param name 要设置的客户端信息属性的名称
         * @param value 要设置的客户端信息属性的值。如果值为 null，则清除指定属性的当前值。
         * <p>
         * @throws SQLClientInfoException 如果数据库服务器在设置客户端信息值时返回错误，或此方法在已关闭的连接上调用
         * <p>
         * @since 1.6
         */
         void setClientInfo(String name, String value)
                throws SQLClientInfoException;

        /**
     * 设置连接的客户端信息属性的值。 <code>Properties</code> 对象包含要设置的客户端信息属性的名称和值。属性列表中包含的客户端信息属性集将替换连接上的当前客户端信息属性集。如果连接上当前设置的属性未出现在属性列表中，则清除该属性。指定空的属性列表将清除连接上的所有属性。有关更多信息，请参见 <code>setClientInfo (String, String)</code>。
     * <p>
     * 如果在设置任何客户端信息属性时发生错误，将抛出 <code>SQLClientInfoException</code>。 <code>SQLClientInfoException</code>
     * 包含未设置的客户端信息属性的信息。由于某些数据库不允许原子地设置多个客户端信息属性，因此客户端信息的状态未知。对于这些数据库，一个或多个属性可能在错误发生之前已被设置。
     * <p>
     *
     * @param properties 要设置的客户端信息属性列表
     * <p>
     * @see java.sql.Connection#setClientInfo(String, String) setClientInfo(String, String)
     * @since 1.6
     * <p>
     * @throws SQLClientInfoException 如果数据库服务器在设置客户端信息值时返回错误，或此方法在已关闭的连接上调用
     *
     */
         void setClientInfo(Properties properties)
                throws SQLClientInfoException;

        /**
         * 返回指定名称的客户端信息属性的值。如果指定的客户端信息属性未设置且没有默认值，此方法可能返回 null。如果指定的客户端信息属性名称不受驱动程序支持，此方法也将返回 null。
         * <p>
         * 应用程序可以使用 <code>DatabaseMetaData.getClientInfoProperties</code>
         * 方法来确定驱动程序支持的客户端信息属性。
         * <p>
         * @param name 要检索的客户端信息属性的名称
         * <p>
         * @return 指定的客户端信息属性的值
         * <p>
         * @throws SQLException 如果数据库服务器在从数据库中获取客户端信息值时返回错误，或此方法在已关闭的连接上调用
         * <p>
         * @since 1.6
         *
         * @see java.sql.DatabaseMetaData#getClientInfoProperties
         */
         String getClientInfo(String name)
                throws SQLException;

        /**
         * 返回一个包含驱动程序支持的每个客户端信息属性的名称和当前值的列表。客户端信息属性的值可能为 null，如果属性未设置且没有默认值。
         * <p>
         * @return 一个包含驱动程序支持的每个客户端信息属性的名称和当前值的 <code>Properties</code> 对象。
         * <p>
         * @throws SQLException 如果数据库服务器在从数据库中获取客户端信息值时返回错误，或此方法在已关闭的连接上调用
         * <p>
         * @since 1.6
         */
         Properties getClientInfo()
                throws SQLException;

/**
  * 创建 Array 对象的工厂方法。
  *<p>
  * <b>注意: </b>当 <code>createArrayOf</code> 用于创建映射到基本数据类型的数组对象时，实现定义该 <code>Array</code> 对象是该基本数据类型的数组还是 <code>Object</code> 的数组。
  * <p>
  * <b>注意: </b>JDBC 驱动程序负责将 <code>Object</code> 数组的元素映射到 <code>java.sql.Types</code> 中定义的给定 <code>Object</code> 类的默认 JDBC SQL 类型。默认映射在 JDBC 规范的附录 B 中指定。如果结果的 JDBC 类型不是适合给定 typeName 的类型，则实现定义是否抛出 <code>SQLException</code> 或驱动程序支持结果转换。
  *
  * @param typeName 元素映射到的 SQL 类型的名称。typeName 是数据库特定的名称，可能是内置类型、用户定义类型或此数据库支持的标准 SQL 类型。这是 <code>Array.getBaseTypeName</code> 返回的值
  * @param elements 用于填充返回对象的元素
  * @return 元素映射到指定 SQL 类型的 Array 对象
  * @throws SQLException 如果发生数据库错误，JDBC 类型不适合 typeName 且转换不支持，typeName 为 null 或此方法在已关闭的连接上调用
  * @throws SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此数据类型
  * @since 1.6
  */
 Array createArrayOf(String typeName, Object[] elements) throws
SQLException;


            /**
  * 创建 Struct 对象的工厂方法。
  *
  * @param typeName 该 <code>Struct</code> 对象映射到的 SQL 结构类型名称。typeName 是为此数据库定义的用户定义类型的名称。它是 <code>Struct.getSQLTypeName</code> 返回的值。

  * @param attributes 用于填充返回对象的属性
  *  @return 一个映射到给定 SQL 类型并用给定属性填充的 Struct 对象
  * @throws SQLException 如果发生数据库错误，typeName 为 null 或此方法在已关闭的连接上调用
  * @throws SQLFeatureNotSupportedException  如果 JDBC 驱动程序不支持此数据类型
  * @since 1.6
  */
 Struct createStruct(String typeName, Object[] attributes)
throws SQLException;

   //--------------------------JDBC 4.1 -----------------------------

   /**
    * 设置要访问的给定模式名称。
    * <P>
    * 如果驱动程序不支持模式，它将
    * 忽略此请求。
    * <p>
    * 调用 {@code setSchema} 对之前创建或准备的
    * {@code Statement} 对象没有影响。是否在调用 {@code Connection}
    * 方法 {@code prepareStatement} 或 {@code prepareCall} 时立即执行 DBMS
    * 准备操作取决于具体实现。为了最大限度地提高可移植性，应在创建或准备
    * {@code Statement} 之前调用 {@code setSchema}。
    *
    * @param schema 要在其中工作的模式名称
    * @exception SQLException 如果发生数据库访问错误
    * 或此方法在已关闭的连接上调用
    * @see #getSchema
    * @since 1.7
    */
    void setSchema(String schema) throws SQLException;

    /**
     * 检索此 <code>Connection</code> 对象的当前模式名称。
     *
     * @return 当前模式名称或 <code>null</code>（如果不存在）
     * @exception SQLException 如果发生数据库访问错误
     * 或此方法在已关闭的连接上调用
     * @see #setSchema
     * @since 1.7
     */
    String getSchema() throws SQLException;

    /**
     * 终止一个打开的连接。调用 <code>abort</code> 会导致：
     * <ul>
     * <li>连接被标记为已关闭
     * <li>关闭与数据库的任何物理连接
     * <li>释放连接使用的资源
     * <li>确保任何当前访问连接的线程要么完成，要么抛出 <code>SQLException</code>。
     * </ul>
     * <p>
     * 调用 <code>abort</code> 标记连接为已关闭并释放任何
     * 资源。在已关闭的连接上调用 <code>abort</code> 是一个空操作。
     * <p>
     * 释放连接持有的资源可能需要一段时间。当
     * <code>abort</code> 方法返回时，连接将被标记为已关闭，传递给 <code>abort</code>
     * 的 <code>Executor</code> 可能仍在执行释放资源的任务。
     * <p>
     * 此方法检查是否存在 <code>SQLPermission</code>
     * 对象，然后才允许方法继续。如果存在
     * <code>SecurityManager</code> 并且其
     * <code>checkPermission</code> 方法拒绝调用 <code>abort</code>，
     * 此方法将抛出 <code>java.lang.SecurityException</code>。
     * @param executor 用于 <code>abort</code> 的 <code>Executor</code> 实现。
     * @throws java.sql.SQLException 如果发生数据库访问错误或
     * {@code executor} 为 {@code null}，
     * @throws java.lang.SecurityException 如果存在安全经理并且其
     *    <code>checkPermission</code> 方法拒绝调用 <code>abort</code>
     * @see SecurityManager#checkPermission
     * @see Executor
     * @since 1.7
     */
    void abort(Executor executor) throws SQLException;

    /**
     *
     * 设置 <code>Connection</code> 或
     * 从 <code>Connection</code> 创建的对象
     * 等待数据库响应任何请求的最大时间。如果任何
     * 请求未得到响应，等待的方法将
     * 返回 <code>SQLException</code>，并且 <code>Connection</code>
     * 或从 <code>Connection</code> 创建的对象将被标记为
     * 已关闭。任何后续使用
     * 这些对象的行为，除了 <code>close</code>、
     * <code>isClosed</code> 或 <code>Connection.isValid</code>
     * 方法外，将导致 <code>SQLException</code>。
     * <p>
     * <b>注意</b>：此方法旨在解决一个罕见但严重的问题，即网络分区可能导致发出 JDBC 调用的线程
     * 在套接字读取中无限期挂起，直到操作系统 TCP 超时
     * （通常为 10 分钟）。此方法与
     * {@link #abort abort() } 方法相关，后者为管理员线程提供了一种释放任何此类线程的方法，当
     * JDBC 连接对管理员线程可访问时。如果不存在管理员线程，或管理员线程无法访问
     * 连接，<code>setNetworkTimeout</code> 方法将覆盖这些情况。此方法的效果非常严重，应
     * 设置足够高的值，以确保在任何更正常的超时（如事务超时）触发之前不会触发此方法。
     * <p>
     * JDBC 驱动程序实现也可能选择支持
     * {@code setNetworkTimeout} 方法，以在没有网络的环境中限制数据库响应时间。
     * <p>
     * 驱动程序可能在内部通过多个内部驱动程序-数据库传输实现其 API 调用的一部分或全部，具体取决于
     * 驱动程序实现，确定限制是否始终应用于 API 调用的响应，或应用于
     * API 调用期间的任何单个请求。
     * <p>
     *
     * 可以多次调用此方法，例如为某个 JDBC 代码区域设置限制，并在退出该区域时重置为默认值。
     * 调用此方法不会影响已发出的请求。
     * <p>
     * {@code Statement.setQueryTimeout()} 超时值独立于
     * {@code setNetworkTimeout} 中指定的超时值。如果查询超时
     * 在网络超时之前到期，则
     * 语句执行将被取消。如果网络仍然
     * 活跃，结果将是语句和连接
     * 仍然可用。但是，如果网络超时在
     * 查询超时之前到期，或者由于网络问题导致语句超时失败，连接将被标记为已关闭，连接持有的任何资源将被释放，连接和
     * 语句将无法使用。
     *<p>
     * 当驱动程序确定 {@code setNetworkTimeout} 超时
     * 值已到期时，JDBC 驱动程序将标记连接
     * 为已关闭并释放连接持有的任何资源。
     * <p>
     *
     * 此方法检查是否存在 <code>SQLPermission</code>
     * 对象，然后才允许方法继续。如果存在
     * <code>SecurityManager</code> 并且其
     * <code>checkPermission</code> 方法拒绝调用
     * <code>setNetworkTimeout</code>，此方法将抛出
     * <code>java.lang.SecurityException</code>。
     *
     * @param executor 用于 <code>setNetworkTimeout</code> 的 <code>Executor</code> 实现。
     * @param milliseconds 等待数据库操作完成的时间（以毫秒为单位）。如果 JDBC 驱动程序不支持毫秒，JDBC 驱动程序将
     * 将值向上舍入到最接近的秒。如果超时时间在操作
     * 完成之前到期，将抛出 <code>SQLException</code>。
     * 值为 0 表示数据库操作没有超时。
     * @throws java.sql.SQLException 如果发生数据库访问错误，此方法在已关闭的连接上调用，
     * {@code executor} 为 {@code null}，
     * 或为 <code>seconds</code> 指定的值小于 0。
     * @throws java.lang.SecurityException 如果存在安全经理并且其
     *    <code>checkPermission</code> 方法拒绝调用
     * <code>setNetworkTimeout</code>。
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see SecurityManager#checkPermission
     * @see Statement#setQueryTimeout
     * @see #getNetworkTimeout
     * @see #abort
     * @see Executor
     * @since 1.7
     */
    void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException;


    /**
     * 检索驱动程序等待数据库请求完成的时间（以毫秒为单位）。
     * 如果超过限制，将
     * 抛出 <code>SQLException</code>。
     *
     * @return 当前超时限制（以毫秒为单位）；零表示没有超时
     * @throws SQLException 如果发生数据库访问错误或
     * 此方法在已关闭的 <code>Connection</code> 上调用
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持
     * 此方法
     * @see #setNetworkTimeout
     * @since 1.7
     */
    int getNetworkTimeout() throws SQLException;
}
