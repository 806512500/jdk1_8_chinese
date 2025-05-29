
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

/**
 * 关于整个数据库的综合信息。
 * <P>
 * 该接口由驱动程序供应商实现，以让用户了解数据库管理系统 (DBMS) 与基于 JDBC&trade; 技术的驱动程序
 * ("JDBC 驱动程序") 结合使用时的功能。不同的关系型 DBMS 通常支持不同的功能，实现功能的方式不同，使用不同的
 * 数据类型。此外，驱动程序可能在 DBMS 提供的基础上实现某些功能。此接口方法返回的信息适用于特定驱动程序和特定 DBMS
 * 一起使用的能力。注意，在此文档中使用的术语“数据库”通常指代驱动程序和 DBMS。
 * <P>
 * 该接口的用户通常是指需要发现如何处理底层 DBMS 的工具。这尤其适用于打算用于多个 DBMS 的应用程序。例如，工具可能会使用
 * <code>getTypeInfo</code> 方法来了解可以在 <code>CREATE TABLE</code> 语句中使用哪些数据类型。或者用户可能会调用
 * <code>supportsCorrelatedSubqueries</code> 方法来查看是否可以使用相关子查询，或者调用 <code>supportsBatchUpdates</code> 方法来查看
 * 是否可以使用批量更新。
 * <P>
 * 一些 <code>DatabaseMetaData</code> 方法返回形式为 <code>ResultSet</code> 对象的信息列表。
 * 可以使用常规的 <code>ResultSet</code> 方法，如 <code>getString</code> 和 <code>getInt</code>，
 * 来检索这些 <code>ResultSet</code> 对象中的数据。如果给定的元数据形式不可用，将返回一个空的 <code>ResultSet</code>。
 * 驱动程序供应商可以定义超出给定方法的 <code>ResultSet</code> 对象定义的列，并且必须通过它们的 <B>列标签</B> 访问这些列。
 * <P>
 * 一些 <code>DatabaseMetaData</code> 方法接受字符串模式作为参数。这些参数的名称都类似于 fooPattern。
 * 在模式字符串中，“%” 表示匹配 0 个或多个字符的任何子串，“_” 表示匹配任何单个字符。只有与搜索模式匹配的元数据条目才会返回。
 * 如果搜索模式参数设置为 <code>null</code>，则该参数的条件将从搜索中删除。
 *
 */
public interface DatabaseMetaData extends Wrapper {

    //----------------------------------------------------------------------
    // 首先，关于目标数据库的各种次要信息。

    /**
     * 检索当前用户是否可以调用由 <code>getProcedures</code> 方法返回的所有过程。
     *
     * @return 如果可以，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean allProceduresAreCallable() throws SQLException;

    /**
     * 检索当前用户是否可以在 <code>SELECT</code> 语句中使用由 <code>getTables</code> 方法返回的所有表。
     *
     * @return 如果可以，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean allTablesAreSelectable() throws SQLException;

    /**
     * 检索此 DBMS 的 URL。
     *
     * @return 此 DBMS 的 URL，如果无法生成则返回 <code>null</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    String getURL() throws SQLException;

    /**
     * 检索此数据库中已知的用户名。
     *
     * @return 数据库用户名
     * @exception SQLException 如果发生数据库访问错误
     */
    String getUserName() throws SQLException;

    /**
     * 检索此数据库是否处于只读模式。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isReadOnly() throws SQLException;

    /**
     * 检索 <code>NULL</code> 值是否按高排序。
     * 按高排序意味着 <code>NULL</code> 值
     * 在域中比任何其他值排序更高。在升序中，
     * 如果此方法返回 <code>true</code>，则 <code>NULL</code> 值
     * 将出现在末尾。相比之下，<code>nullsAreSortedAtEnd</code> 方法表示 <code>NULL</code> 值
     * 是否无论排序顺序如何都排在末尾。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean nullsAreSortedHigh() throws SQLException;

    /**
     * 检索 <code>NULL</code> 值是否按低排序。
     * 按低排序意味着 <code>NULL</code> 值
     * 在域中比任何其他值排序更低。在升序中，
     * 如果此方法返回 <code>true</code>，则 <code>NULL</code> 值
     * 将出现在开头。相比之下，<code>nullsAreSortedAtStart</code> 方法表示 <code>NULL</code> 值
     * 是否无论排序顺序如何都排在开头。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean nullsAreSortedLow() throws SQLException;

    /**
     * 检索 <code>NULL</code> 值是否无论排序顺序如何都排在开头。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean nullsAreSortedAtStart() throws SQLException;

    /**
     * 检索 <code>NULL</code> 值是否无论排序顺序如何都排在末尾。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean nullsAreSortedAtEnd() throws SQLException;

                /**
     * 获取此数据库产品的名称。
     *
     * @return 数据库产品名称
     * @exception SQLException 如果发生数据库访问错误
     */
    String getDatabaseProductName() throws SQLException;

    /**
     * 获取此数据库产品的版本号。
     *
     * @return 数据库版本号
     * @exception SQLException 如果发生数据库访问错误
     */
    String getDatabaseProductVersion() throws SQLException;

    /**
     * 获取此 JDBC 驱动程序的名称。
     *
     * @return JDBC 驱动程序名称
     * @exception SQLException 如果发生数据库访问错误
     */
    String getDriverName() throws SQLException;

    /**
     * 获取此 JDBC 驱动程序的版本号，以 <code>String</code> 形式返回。
     *
     * @return JDBC 驱动程序版本
     * @exception SQLException 如果发生数据库访问错误
     */
    String getDriverVersion() throws SQLException;

    /**
     * 获取此 JDBC 驱动程序的主要版本号。
     *
     * @return JDBC 驱动程序主要版本号
     */
    int getDriverMajorVersion();

    /**
     * 获取此 JDBC 驱动程序的次要版本号。
     *
     * @return JDBC 驱动程序次要版本号
     */
    int getDriverMinorVersion();

    /**
     * 获取此数据库是否将表存储在本地文件中。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean usesLocalFiles() throws SQLException;

    /**
     * 获取此数据库是否为每个表使用一个本地文件。
     *
     * @return 如果此数据库为每个表使用一个本地文件，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean usesLocalFilePerTable() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的未加引号的 SQL 标识符视为大小写敏感，并因此以混合大小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsMixedCaseIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的未加引号的 SQL 标识符视为大小写不敏感，并以大写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesUpperCaseIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的未加引号的 SQL 标识符视为大小写不敏感，并以小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesLowerCaseIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的未加引号的 SQL 标识符视为大小写不敏感，并以混合大小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesMixedCaseIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的加引号的 SQL 标识符视为大小写敏感，并因此以混合大小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的加引号的 SQL 标识符视为大小写不敏感，并以大写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesUpperCaseQuotedIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的加引号的 SQL 标识符视为大小写不敏感，并以小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesLowerCaseQuotedIdentifiers() throws SQLException;

    /**
     * 获取此数据库是否将混合大小写的加引号的 SQL 标识符视为大小写不敏感，并以混合大小写形式存储。
     *
     * @return 如果是，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean storesMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * 获取用于引用 SQL 标识符的字符串。如果标识符引用不受支持，则此方法返回空格 " "。
     *
     * @return 引用字符串或空格，如果引用不受支持
     * @exception SQLException 如果发生数据库访问错误
     */
    String getIdentifierQuoteString() throws SQLException;

    /**
     * 获取此数据库的所有 SQL 关键字的逗号分隔列表，这些关键字不是 SQL:2003 关键字。
     *
     * @return 此数据库的关键字列表，这些关键字不是 SQL:2003 关键字
     * @exception SQLException 如果发生数据库访问错误
     */
    String getSQLKeywords() throws SQLException;

    /**
     * 获取此数据库可用的数学函数的逗号分隔列表。这些是 JDBC 函数转义子句中使用的 Open /Open CLI 数学函数名称。
     *
     * @return 此数据库支持的数学函数列表
     * @exception SQLException 如果发生数据库访问错误
     */
    String getNumericFunctions() throws SQLException;

    /**
     * 获取此数据库可用的字符串函数的逗号分隔列表。这些是 JDBC 函数转义子句中使用的 Open Group CLI 字符串函数名称。
     *
     * @return 此数据库支持的字符串函数列表
     * @exception SQLException 如果发生数据库访问错误
     */
    String getStringFunctions() throws SQLException;

                /**
     * 获取此数据库可用的系统函数的逗号分隔列表。这些是用于JDBC函数转义子句的Open Group CLI系统函数名称。
     *
     * @return 此数据库支持的系统函数列表
     * @exception SQLException 如果发生数据库访问错误
     */
    String getSystemFunctions() throws SQLException;

    /**
     * 获取此数据库可用的时间和日期函数的逗号分隔列表。
     *
     * @return 此数据库支持的时间和日期函数列表
     * @exception SQLException 如果发生数据库访问错误
     */
    String getTimeDateFunctions() throws SQLException;

    /**
     * 获取可用于转义通配符字符的字符串。这是可以在模式中（因此使用通配符字符之一）的目录搜索参数中转义'_'或'%'的字符串。
     *
     * <P>'_'字符代表任何单个字符；
     * '%'字符代表任何零个或多个字符的序列。
     *
     * @return 用于转义通配符字符的字符串
     * @exception SQLException 如果发生数据库访问错误
     */
    String getSearchStringEscape() throws SQLException;

    /**
     * 获取可以在未加引号的标识符名称中使用的“额外”字符（超出a-z、A-Z、0-9和_的字符）。
     *
     * @return 包含额外字符的字符串
     * @exception SQLException 如果发生数据库访问错误
     */
    String getExtraNameCharacters() throws SQLException;

    //--------------------------------------------------------------------
    // 描述支持哪些特性的函数。

    /**
     * 获取此数据库是否支持带有添加列的<code>ALTER TABLE</code>。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsAlterTableWithAddColumn() throws SQLException;

    /**
     * 获取此数据库是否支持带有删除列的<code>ALTER TABLE</code>。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsAlterTableWithDropColumn() throws SQLException;

    /**
     * 获取此数据库是否支持列别名。
     *
     * <P>如果支持，可以使用SQL AS子句为计算列提供名称，或者根据需要为列提供别名名称。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsColumnAliasing() throws SQLException;

    /**
     * 获取此数据库是否支持<code>NULL</code>与非<code>NULL</code>值的连接结果为<code>NULL</code>。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean nullPlusNonNullIsNull() throws SQLException;

    /**
     * 获取此数据库是否支持JDBC标量函数<code>CONVERT</code>，用于将一种JDBC类型转换为另一种。JDBC类型是在<code>java.sql.Types</code>中定义的通用SQL数据类型。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsConvert() throws SQLException;

    /**
     * 获取此数据库是否支持JDBC标量函数<code>CONVERT</code>，用于将JDBC类型<code>fromType</code>转换为<code>toType</code>。JDBC类型是在<code>java.sql.Types</code>中定义的通用SQL数据类型。
     *
     * @param fromType 要转换的类型；来自<code>java.sql.Types</code>类的类型代码之一
     * @param toType 要转换到的类型；来自<code>java.sql.Types</code>类的类型代码之一
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @see Types
     */
    boolean supportsConvert(int fromType, int toType) throws SQLException;

    /**
     * 获取此数据库是否支持表关联名称。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsTableCorrelationNames() throws SQLException;

    /**
     * 获取当支持表关联名称时，它们是否被限制为与表名不同。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsDifferentTableCorrelationNames() throws SQLException;

    /**
     * 获取此数据库是否支持在<code>ORDER BY</code>列表中使用表达式。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsExpressionsInOrderBy() throws SQLException;

    /**
     * 获取此数据库是否支持在<code>ORDER BY</code>子句中使用不在<code>SELECT</code>语句中的列。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOrderByUnrelated() throws SQLException;

    /**
     * 获取此数据库是否支持某种形式的<code>GROUP BY</code>子句。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsGroupBy() throws SQLException;

                /**
     * 获取此数据库是否支持在 <code>GROUP BY</code> 子句中使用未在 <code>SELECT</code> 语句中的列。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsGroupByUnrelated() throws SQLException;

    /**
     * 获取此数据库是否支持在 <code>GROUP BY</code> 子句中使用未在 <code>SELECT</code> 语句中的列，
     * 前提是 <code>SELECT</code> 语句中的所有列都包含在 <code>GROUP BY</code> 子句中。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsGroupByBeyondSelect() throws SQLException;

    /**
     * 获取此数据库是否支持指定 <code>LIKE</code> 逃逸子句。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsLikeEscapeClause() throws SQLException;

    /**
     * 获取此数据库是否支持从单个调用 <code>execute</code> 方法中获取多个 <code>ResultSet</code> 对象。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsMultipleResultSets() throws SQLException;

    /**
     * 获取此数据库是否允许同时打开多个事务（在不同的连接上）。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsMultipleTransactions() throws SQLException;

    /**
     * 获取此数据库中的列是否可以定义为非空。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsNonNullableColumns() throws SQLException;

    /**
     * 获取此数据库是否支持 ODBC 最小 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsMinimumSQLGrammar() throws SQLException;

    /**
     * 获取此数据库是否支持 ODBC 核心 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCoreSQLGrammar() throws SQLException;

    /**
     * 获取此数据库是否支持 ODBC 扩展 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsExtendedSQLGrammar() throws SQLException;

    /**
     * 获取此数据库是否支持 ANSI92 入门级 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsANSI92EntryLevelSQL() throws SQLException;

    /**
     * 获取此数据库是否支持 ANSI92 中级 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsANSI92IntermediateSQL() throws SQLException;

    /**
     * 获取此数据库是否支持 ANSI92 完整 SQL 语法。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsANSI92FullSQL() throws SQLException;

    /**
     * 获取此数据库是否支持 SQL 完整性增强设施。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsIntegrityEnhancementFacility() throws SQLException;

    /**
     * 获取此数据库是否支持某种形式的外部连接。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOuterJoins() throws SQLException;

    /**
     * 获取此数据库是否支持完整的嵌套外部连接。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsFullOuterJoins() throws SQLException;

    /**
     * 获取此数据库是否提供对外部连接的有限支持。 （如果方法 <code>supportsFullOuterJoins</code> 返回 <code>true</code>，则此方法也将返回 <code>true</code>）。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsLimitedOuterJoins() throws SQLException;

    /**
     * 获取数据库供应商对“模式”的首选术语。
     *
     * @return 供应商对“模式”的术语
     * @exception SQLException 如果发生数据库访问错误
     */
    String getSchemaTerm() throws SQLException;

    /**
     * 获取数据库供应商对“过程”的首选术语。
     *
     * @return 供应商对“过程”的术语
     * @exception SQLException 如果发生数据库访问错误
     */
    String getProcedureTerm() throws SQLException;

    /**
     * 获取数据库供应商对“目录”的首选术语。
     *
     * @return 供应商对“目录”的术语
     * @exception SQLException 如果发生数据库访问错误
     */
    String getCatalogTerm() throws SQLException;

    /**
     * 获取目录是否出现在完全限定的表名的开始位置。 如果不是，则目录出现在末尾。
     *
     * @return 如果目录名称出现在完全限定的表名的开始位置，则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean isCatalogAtStart() throws SQLException;

                /**
     * 获取此数据库用于分隔目录和表名的<code>String</code>。
     *
     * @return 分隔符字符串
     * @exception SQLException 如果发生数据库访问错误
     */
    String getCatalogSeparator() throws SQLException;

    /**
     * 获取是否可以在数据操作语句中使用模式名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSchemasInDataManipulation() throws SQLException;

    /**
     * 获取是否可以在过程调用语句中使用模式名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSchemasInProcedureCalls() throws SQLException;

    /**
     * 获取是否可以在表定义语句中使用模式名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSchemasInTableDefinitions() throws SQLException;

    /**
     * 获取是否可以在索引定义语句中使用模式名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSchemasInIndexDefinitions() throws SQLException;

    /**
     * 获取是否可以在权限定义语句中使用模式名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;

    /**
     * 获取是否可以在数据操作语句中使用目录名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCatalogsInDataManipulation() throws SQLException;

    /**
     * 获取是否可以在过程调用语句中使用目录名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCatalogsInProcedureCalls() throws SQLException;

    /**
     * 获取是否可以在表定义语句中使用目录名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCatalogsInTableDefinitions() throws SQLException;

    /**
     * 获取是否可以在索引定义语句中使用目录名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCatalogsInIndexDefinitions() throws SQLException;

    /**
     * 获取是否可以在权限定义语句中使用目录名称。
     *
     * @return 如果可以则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;


    /**
     * 获取此数据库是否支持定位的<code>DELETE</code>语句。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsPositionedDelete() throws SQLException;

    /**
     * 获取此数据库是否支持定位的<code>UPDATE</code>语句。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsPositionedUpdate() throws SQLException;

    /**
     * 获取此数据库是否支持<code>SELECT FOR UPDATE</code>语句。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSelectForUpdate() throws SQLException;

    /**
     * 获取此数据库是否支持使用存储过程转义语法的存储过程调用。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsStoredProcedures() throws SQLException;

    /**
     * 获取此数据库是否支持在比较表达式中使用子查询。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSubqueriesInComparisons() throws SQLException;

    /**
     * 获取此数据库是否支持在<code>EXISTS</code>表达式中使用子查询。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSubqueriesInExists() throws SQLException;

    /**
     * 获取此数据库是否支持在<code>IN</code>表达式中使用子查询。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSubqueriesInIns() throws SQLException;

    /**
     * 获取此数据库是否支持在量化表达式中使用子查询。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsSubqueriesInQuantifieds() throws SQLException;

    /**
     * 获取此数据库是否支持相关子查询。
     *
     * @return 如果支持则为<code>true</code>；否则为<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsCorrelatedSubqueries() throws SQLException;


                /**
     * 获取此数据库是否支持 SQL <code>UNION</code>。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsUnion() throws SQLException;

    /**
     * 获取此数据库是否支持 SQL <code>UNION ALL</code>。
     *
     * @return 如果支持则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsUnionAll() throws SQLException;

    /**
     * 获取此数据库是否支持在提交时保持游标打开。
     *
     * @return 如果游标总是保持打开则返回 <code>true</code>；
     *       如果游标可能不会保持打开则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOpenCursorsAcrossCommit() throws SQLException;

    /**
     * 获取此数据库是否支持在回滚时保持游标打开。
     *
     * @return 如果游标总是保持打开则返回 <code>true</code>；
     *       如果游标可能不会保持打开则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOpenCursorsAcrossRollback() throws SQLException;

    /**
     * 获取此数据库是否支持在提交时保持语句打开。
     *
     * @return 如果语句总是保持打开则返回 <code>true</code>；
     *       如果语句可能不会保持打开则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOpenStatementsAcrossCommit() throws SQLException;

    /**
     * 获取此数据库是否支持在回滚时保持语句打开。
     *
     * @return 如果语句总是保持打开则返回 <code>true</code>；
     *       如果语句可能不会保持打开则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsOpenStatementsAcrossRollback() throws SQLException;



    //----------------------------------------------------------------------
    // 以下方法组暴露了基于目标数据库和当前驱动程序的各种限制。
    // 除非另有说明，结果为零表示没有限制或未知限制。

    /**
     * 获取此数据库在内联二进制字面量中允许的最大十六进制字符数。
     *
     * @return 二进制字面量允许的最大长度（以十六进制字符为单位）；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxBinaryLiteralLength() throws SQLException;

    /**
     * 获取此数据库允许的字符字面量的最大字符数。
     *
     * @return 字符字面量允许的最大字符数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxCharLiteralLength() throws SQLException;

    /**
     * 获取此数据库允许的列名的最大字符数。
     *
     * @return 列名允许的最大字符数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnNameLength() throws SQLException;

    /**
     * 获取此数据库在 <code>GROUP BY</code> 子句中允许的最大列数。
     *
     * @return 允许的最大列数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnsInGroupBy() throws SQLException;

    /**
     * 获取此数据库在索引中允许的最大列数。
     *
     * @return 允许的最大列数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnsInIndex() throws SQLException;

    /**
     * 获取此数据库在 <code>ORDER BY</code> 子句中允许的最大列数。
     *
     * @return 允许的最大列数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnsInOrderBy() throws SQLException;

    /**
     * 获取此数据库在 <code>SELECT</code> 列表中允许的最大列数。
     *
     * @return 允许的最大列数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnsInSelect() throws SQLException;

    /**
     * 获取此数据库在表中允许的最大列数。
     *
     * @return 允许的最大列数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxColumnsInTable() throws SQLException;

    /**
     * 获取此数据库可能的最大并发连接数。
     *
     * @return 一次可能的最大活动连接数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxConnections() throws SQLException;

    /**
     * 获取此数据库在游标名称中允许的最大字符数。
     *
     * @return 游标名称允许的最大字符数；
     *      结果为零表示没有限制或未知限制
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxCursorNameLength() throws SQLException;


                /**
     * 获取此数据库允许的索引最大字节数，包括索引的所有部分。
     *
     * @return 允许的最大字节数；此限制包括索引的所有组成部分；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxIndexLength() throws SQLException;

    /**
     * 获取此数据库允许的模式名称最大字符数。
     *
     * @return 允许的模式名称最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxSchemaNameLength() throws SQLException;

    /**
     * 获取此数据库允许的过程名称最大字符数。
     *
     * @return 允许的过程名称最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxProcedureNameLength() throws SQLException;

    /**
     * 获取此数据库允许的目录名称最大字符数。
     *
     * @return 允许的目录名称最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxCatalogNameLength() throws SQLException;

    /**
     * 获取此数据库允许的单行最大字节数。
     *
     * @return 允许的行最大字节数；结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxRowSize() throws SQLException;

    /**
     * 获取方法 <code>getMaxRowSize</code> 的返回值是否包括 SQL 数据类型
     * <code>LONGVARCHAR</code> 和 <code>LONGVARBINARY</code>。
     *
     * @return 如果包括则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean doesMaxRowSizeIncludeBlobs() throws SQLException;

    /**
     * 获取此数据库允许的 SQL 语句最大字符数。
     *
     * @return 允许的 SQL 语句最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxStatementLength() throws SQLException;

    /**
     * 获取此数据库允许的同时打开的活动语句的最大数量。
     *
     * @return 允许的同时打开的语句最大数量；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxStatements() throws SQLException;

    /**
     * 获取此数据库允许的表名称最大字符数。
     *
     * @return 允许的表名称最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxTableNameLength() throws SQLException;

    /**
     * 获取此数据库允许的 <code>SELECT</code> 语句中的表的最大数量。
     *
     * @return 允许的 <code>SELECT</code> 语句中的表的最大数量；
     *         结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxTablesInSelect() throws SQLException;

    /**
     * 获取此数据库允许的用户名称最大字符数。
     *
     * @return 允许的用户名称最大字符数；
     *      结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     */
    int getMaxUserNameLength() throws SQLException;

    //----------------------------------------------------------------------

    /**
     * 获取此数据库的默认事务隔离级别。可能的值在 <code>java.sql.Connection</code> 中定义。
     *
     * @return 默认隔离级别
     * @exception SQLException 如果发生数据库访问错误
     * @see Connection
     */
    int getDefaultTransactionIsolation() throws SQLException;

    /**
     * 获取此数据库是否支持事务。如果不支持，调用 <code>commit</code> 方法将不起作用，隔离级别为
     * <code>TRANSACTION_NONE</code>。
     *
     * @return 如果支持事务则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsTransactions() throws SQLException;

    /**
     * 获取此数据库是否支持给定的事务隔离级别。
     *
     * @param level 在 <code>java.sql.Connection</code> 中定义的事务隔离级别之一
     * @return 如果支持则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @see Connection
     */
    boolean supportsTransactionIsolationLevel(int level)
        throws SQLException;

    /**
     * 获取此数据库是否支持事务中的数据定义和数据操作语句。
     *
     * @return 如果支持则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsDataDefinitionAndDataManipulationTransactions()
        throws SQLException;
    /**
     * 获取此数据库是否仅支持事务中的数据操作语句。
     *
     * @return 如果支持则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean supportsDataManipulationTransactionsOnly()
        throws SQLException;


    /**
     * 获取事务中的数据定义语句是否强制事务提交。
     *
     * @return 如果是，则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean dataDefinitionCausesTransactionCommit()
        throws SQLException;

    /**
     * 获取此数据库是否忽略事务中的数据定义语句。
     *
     * @return 如果是，则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     */
    boolean dataDefinitionIgnoredInTransactions()
        throws SQLException;

    /**
     * 获取给定目录中可用的存储过程的描述。
     * <P>
     * 只返回符合模式和过程名称条件的过程描述。它们按<code>PROCEDURE_CAT</code>、<code>PROCEDURE_SCHEM</code>、
     * <code>PROCEDURE_NAME</code>和<code>SPECIFIC_ NAME</code>排序。
     *
     * <P>每个过程描述包含以下列：
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String {@code =>} 过程目录（可能是<code>null</code>）
     *  <LI><B>PROCEDURE_SCHEM</B> String {@code =>} 过程模式（可能是<code>null</code>）
     *  <LI><B>PROCEDURE_NAME</B> String {@code =>} 过程名称
     *  <LI> 为将来使用保留
     *  <LI> 为将来使用保留
     *  <LI> 为将来使用保留
     *  <LI><B>REMARKS</B> String {@code =>} 对过程的解释性注释
     *  <LI><B>PROCEDURE_TYPE</B> short {@code =>} 过程类型：
     *      <UL>
     *      <LI> procedureResultUnknown - 无法确定是否返回值
     *      <LI> procedureNoResult - 不返回值
     *      <LI> procedureReturnsResult - 返回值
     *      </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  {@code =>} 在其模式中唯一标识此过程的名称。
     *  </OL>
     * <p>
     * 用户可能没有执行<code>getProcedures</code>返回的任何过程的权限。
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；""检索没有目录的；<code>null</code>表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；""检索没有模式的；<code>null</code>表示不应使用模式名称来缩小搜索范围
     * @param procedureNamePattern 过程名称模式；必须与数据库中存储的过程名称匹配
     * @return <code>ResultSet</code> - 每行是一个过程描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getProcedures(String catalog, String schemaPattern,
                            String procedureNamePattern) throws SQLException;

    /**
     * 表示不知道过程是否返回结果。
     * <P>
     * <code>getProcedures</code>方法返回的<code>ResultSet</code>对象中<code>PROCEDURE_TYPE</code>列的可能值。
     */
    int procedureResultUnknown  = 0;

    /**
     * 表示过程不返回结果。
     * <P>
     * <code>getProcedures</code>方法返回的<code>ResultSet</code>对象中<code>PROCEDURE_TYPE</code>列的可能值。
     */
    int procedureNoResult               = 1;

    /**
     * 表示过程返回结果。
     * <P>
     * <code>getProcedures</code>方法返回的<code>ResultSet</code>对象中<code>PROCEDURE_TYPE</code>列的可能值。
     */
    int procedureReturnsResult  = 2;

    /**
     * 获取给定目录的存储过程参数和结果列的描述。
     *
     * <P>只返回符合模式、过程和参数名称条件的描述。它们按PROCEDURE_CAT、PROCEDURE_SCHEM、PROCEDURE_NAME和SPECIFIC_NAME排序。在此基础上，如果有返回值，则首先返回。接下来是按调用顺序的参数描述。列描述按列号顺序排列。
     *
     * <P><code>ResultSet</code>中的每一行是一个参数描述或列描述，包含以下字段：
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String {@code =>} 过程目录（可能是<code>null</code>）
     *  <LI><B>PROCEDURE_SCHEM</B> String {@code =>} 过程模式（可能是<code>null</code>）
     *  <LI><B>PROCEDURE_NAME</B> String {@code =>} 过程名称
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列/参数名称
     *  <LI><B>COLUMN_TYPE</B> Short {@code =>} 列/参数类型：
     *      <UL>
     *      <LI> procedureColumnUnknown - 无人知道
     *      <LI> procedureColumnIn - 输入参数
     *      <LI> procedureColumnInOut - 输入输出参数
     *      <LI> procedureColumnOut - 输出参数
     *      <LI> procedureColumnReturn - 过程返回值
     *      <LI> procedureColumnResult - <code>ResultSet</code>中的结果列
     *      </UL>
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自java.sql.Types的SQL类型
     *  <LI><B>TYPE_NAME</B> String {@code =>} SQL类型名称，对于UDT类型，类型名称是完全限定的
     *  <LI><B>PRECISION</B> int {@code =>} 精度
     *  <LI><B>LENGTH</B> int {@code =>} 数据的字节长度
     *  <LI><B>SCALE</B> short {@code =>} 小数位数 - 对于不适用小数位数的数据类型，返回null。
     *  <LI><B>RADIX</B> short {@code =>} 基数
     *  <LI><B>NULLABLE</B> short {@code =>} 是否可以包含NULL。
     *      <UL>
     *      <LI> procedureNoNulls - 不允许NULL值
     *      <LI> procedureNullable - 允许NULL值
     *      <LI> procedureNullableUnknown - NULL性未知
     *      </UL>
     *  <LI><B>REMARKS</B> String {@code =>} 描述参数/列的注释
     *  <LI><B>COLUMN_DEF</B> String {@code =>} 列的默认值，当值被单引号包围时应解释为字符串（可能是<code>null</code>）
     *      <UL>
     *      <LI> 字符串NULL（不被引号包围） - 如果指定的默认值为NULL
     *      <LI> TRUNCATE（不被引号包围） - 如果指定的默认值无法表示而不被截断
     *      <LI> NULL - 如果未指定默认值
     *      </UL>
     *  <LI><B>SQL_DATA_TYPE</B> int  {@code =>} 为将来使用保留
     *  <LI><B>SQL_DATETIME_SUB</B> int  {@code =>} 为将来使用保留
     *  <LI><B>CHAR_OCTET_LENGTH</B> int  {@code =>} 二进制和基于字符的列的最大长度。对于任何其他数据类型，返回值为NULL。
     *  <LI><B>ORDINAL_POSITION</B> int  {@code =>} 过程的输入和输出参数的序号位置，从1开始。如果此行描述过程的返回值，则返回0。对于结果集列，它是结果集中列的序号位置，从1开始。如果有多个结果集，列的序号位置由实现定义。
     *  <LI><B>IS_NULLABLE</B> String  {@code =>} 使用ISO规则确定列的NULL性。
     *       <UL>
     *       <LI> YES           --- 如果列可以包含NULL
     *       <LI> NO            --- 如果列不能包含NULL
     *       <LI> 空字符串  --- 如果列的NULL性未知
     *       </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  {@code =>} 在其模式中唯一标识此过程的名称。
     *  </OL>
     *
     * <P><B>注意：</B> 一些数据库可能不会返回过程的列描述。
     *
     * <p>PRECISION列表示给定列的指定列大小。对于数值数据，这是最大精度。对于字符数据，这是字符长度。对于日期时间数据类型，这是字符串表示形式的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于ROWID数据类型，这是字节长度。对于不适用列大小的数据类型，返回null。
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；""检索没有目录的；<code>null</code>表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；""检索没有模式的；<code>null</code>表示不应使用模式名称来缩小搜索范围
     * @param procedureNamePattern 过程名称模式；必须与数据库中存储的过程名称匹配
     * @param columnNamePattern 列名称模式；必须与数据库中存储的列名称匹配
     * @return <code>ResultSet</code> - 每行描述一个存储过程参数或列
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getProcedureColumns(String catalog,
                                  String schemaPattern,
                                  String procedureNamePattern,
                                  String columnNamePattern) throws SQLException;


    /**
     * 表示列的类型未知。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnUnknown = 0;

    /**
     * 表示列存储 IN 参数。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnIn = 1;

    /**
     * 表示列存储 INOUT 参数。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnInOut = 2;

    /**
     * 表示列存储 OUT 参数。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnOut = 4;
    /**
     * 表示列存储返回值。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnReturn = 5;

    /**
     * 表示列存储结果。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int procedureColumnResult = 3;

    /**
     * 表示不允许 <code>NULL</code> 值。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     */
    int procedureNoNulls = 0;

    /**
     * 表示允许 <code>NULL</code> 值。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     */
    int procedureNullable = 1;

    /**
     * 表示是否允许 <code>NULL</code> 值未知。
     * <P>
     * 方法 <code>getProcedureColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     */
    int procedureNullableUnknown = 2;


    /**
     * 检索给定目录中可用的表的描述。
     * 只返回与目录、模式、表名和类型标准匹配的表描述。它们按
     * <code>TABLE_TYPE</code>、<code>TABLE_CAT</code>、
     * <code>TABLE_SCHEM</code> 和 <code>TABLE_NAME</code> 排序。
     * <P>
     * 每个表描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>TABLE_TYPE</B> String {@code =>} 表类型。常见类型有 "TABLE"、
     *                  "VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、
     *                  "LOCAL TEMPORARY"、"ALIAS"、"SYNONYM"。
     *  <LI><B>REMARKS</B> String {@code =>} 表的解释性注释
     *  <LI><B>TYPE_CAT</B> String {@code =>} 类型目录（可能是 <code>null</code>）
     *  <LI><B>TYPE_SCHEM</B> String {@code =>} 类型模式（可能是 <code>null</code>）
     *  <LI><B>TYPE_NAME</B> String {@code =>} 类型名（可能是 <code>null</code>）
     *  <LI><B>SELF_REFERENCING_COL_NAME</B> String {@code =>} 指定的
     *                  "标识符" 列的名称（可能是 <code>null</code>）
     *  <LI><B>REF_GENERATION</B> String {@code =>} 指定
     *                  SELF_REFERENCING_COL_NAME 中的值如何创建。值为
     *                  "SYSTEM"、"USER"、"DERIVED"。（可能是 <code>null</code>）
     *  </OL>
     *
     * <P><B>注意：</B> 一些数据库可能不会返回所有表的信息。
     *
     * @param catalog 目录名；必须与数据库中存储的目录名匹配；"" 检索没有目录的表；
     *        <code>null</code> 表示不应使用目录名来缩小搜索范围
     * @param schemaPattern 模式名模式；必须与数据库中存储的模式名匹配；"" 检索没有模式的表；
     *        <code>null</code> 表示不应使用模式名来缩小搜索范围
     * @param tableNamePattern 表名模式；必须与数据库中存储的表名匹配
     * @param types 表类型列表，必须从 {@link #getTableTypes} 返回的表类型列表中选择；<code>null</code> 返回所有类型
     * @return <code>ResultSet</code> - 每行是一个表描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getTables(String catalog, String schemaPattern,
                        String tableNamePattern, String types[]) throws SQLException;

    /**
     * 检索此数据库中可用的模式名。结果
     * 按 <code>TABLE_CATALOG</code> 和
     * <code>TABLE_SCHEM</code> 排序。
     *
     * <P>模式列是：
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 模式名
     *  <LI><B>TABLE_CATALOG</B> String {@code =>} 目录名（可能是 <code>null</code>）
     *  </OL>
     *
     * @return 一个 <code>ResultSet</code> 对象，其中每行是一个
     *         模式描述
     * @exception SQLException 如果发生数据库访问错误
     *
     */
    ResultSet getSchemas() throws SQLException;

                /**
     * 获取此数据库中可用的目录名称。结果按目录名称排序。
     *
     * <P>目录列是：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 目录名称
     *  </OL>
     *
     * @return 一个 <code>ResultSet</code> 对象，其中每一行都有一个
     *         单个 <code>String</code> 列，该列是一个目录名称
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getCatalogs() throws SQLException;

    /**
     * 获取此数据库中可用的表类型。结果按表类型排序。
     *
     * <P>表类型是：
     *  <OL>
     *  <LI><B>TABLE_TYPE</B> String {@code =>} 表类型。典型的类型有 "TABLE"，
     *                  "VIEW"，"SYSTEM TABLE"，"GLOBAL TEMPORARY"，
     *                  "LOCAL TEMPORARY"，"ALIAS"，"SYNONYM"。
     *  </OL>
     *
     * @return 一个 <code>ResultSet</code> 对象，其中每一行都有一个
     *         单个 <code>String</code> 列，该列是一个表类型
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getTableTypes() throws SQLException;

    /**
     * 获取指定目录中可用的表列描述。
     *
     * <P>仅返回与目录、模式、表和列名称标准匹配的列描述。它们按
     * <code>TABLE_CAT</code>，<code>TABLE_SCHEM</code>，
     * <code>TABLE_NAME</code> 和 <code>ORDINAL_POSITION</code> 排序。
     *
     * <P>每个列描述有以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名称
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名称
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自 java.sql.Types 的 SQL 类型
     *  <LI><B>TYPE_NAME</B> String {@code =>} 数据源依赖的类型名称，
     *  对于 UDT，类型名称是完全限定的
     *  <LI><B>COLUMN_SIZE</B> int {@code =>} 列大小。
     *  <LI><B>BUFFER_LENGTH</B> 未使用。
     *  <LI><B>DECIMAL_DIGITS</B> int {@code =>} 小数位数。对于不适用小数位数的数据类型，返回 Null。
     *  <LI><B>NUM_PREC_RADIX</B> int {@code =>} 基数（通常是 10 或 2）
     *  <LI><B>NULLABLE</B> int {@code =>} 是否允许 NULL。
     *      <UL>
     *      <LI> columnNoNulls - 可能不允许 <code>NULL</code> 值
     *      <LI> columnNullable - 肯定允许 <code>NULL</code> 值
     *      <LI> columnNullableUnknown - 可能性未知
     *      </UL>
     *  <LI><B>REMARKS</B> String {@code =>} 描述列的注释（可能是 <code>null</code>）
     *  <LI><B>COLUMN_DEF</B> String {@code =>} 列的默认值，当值被单引号包围时应解释为字符串（可能是 <code>null</code>）
     *  <LI><B>SQL_DATA_TYPE</B> int {@code =>} 未使用
     *  <LI><B>SQL_DATETIME_SUB</B> int {@code =>} 未使用
     *  <LI><B>CHAR_OCTET_LENGTH</B> int {@code =>} 对于字符类型，列中的最大字节数
     *  <LI><B>ORDINAL_POSITION</B> int {@code =>} 列在表中的索引
     *      （从 1 开始）
     *  <LI><B>IS_NULLABLE</B> String  {@code =>} 使用 ISO 规则确定列的可空性。
     *       <UL>
     *       <LI> YES           --- 如果列可以包含 NULLs
     *       <LI> NO            --- 如果列不能包含 NULLs
     *       <LI> 空字符串  --- 如果列的可空性未知
     *       </UL>
     *  <LI><B>SCOPE_CATALOG</B> String {@code =>} 引用属性范围的表的目录
     *      （如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
     *  <LI><B>SCOPE_SCHEMA</B> String {@code =>} 引用属性范围的表的模式
     *      （如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
     *  <LI><B>SCOPE_TABLE</B> String {@code =>} 引用属性范围的表名称
     *      （如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
     *  <LI><B>SOURCE_DATA_TYPE</B> short {@code =>} 区分类型或用户生成的
     *      Ref 类型的源类型，来自 java.sql.Types 的 SQL 类型（如果 DATA_TYPE
     *      不是 DISTINCT 或用户生成的 REF，则为 <code>null</code>）
     *   <LI><B>IS_AUTOINCREMENT</B> String  {@code =>} 指示此列是否自动递增
     *       <UL>
     *       <LI> YES           --- 如果列是自动递增的
     *       <LI> NO            --- 如果列不是自动递增的
     *       <LI> 空字符串  --- 如果无法确定列是否自动递增
     *       </UL>
     *   <LI><B>IS_GENERATEDCOLUMN</B> String  {@code =>} 指示这是否是生成的列
     *       <UL>
     *       <LI> YES           --- 如果这是生成的列
     *       <LI> NO            --- 如果这不是生成的列
     *       <LI> 空字符串  --- 如果无法确定这是否是生成的列
     *       </UL>
     *  </OL>
     *
     * <p>COLUMN_SIZE 列指定了给定列的列大小。
     * 对于数值数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示形式的长度（假设小数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，
     * 这是字节长度。对于不适用列大小的数据类型，返回 Null。
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的；
     *        <code>null</code> 表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；"" 检索没有模式的；
     *        <code>null</code> 表示不应使用模式名称来缩小搜索范围
     * @param tableNamePattern 表名称模式；必须与
     *        数据库中存储的表名称匹配
     * @param columnNamePattern 列名称模式；必须与列
     *        名称在数据库中的存储方式匹配
     * @return <code>ResultSet</code> - 每一行都是一个列描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getColumns(String catalog, String schemaPattern,
                         String tableNamePattern, String columnNamePattern)
        throws SQLException;

                /**
     * 表示该列可能不允许 <code>NULL</code> 值。
     * <P>
     * 通过方法 <code>getColumns</code> 返回的 <code>ResultSet</code> 中
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int columnNoNulls = 0;

    /**
     * 表示该列肯定允许 <code>NULL</code> 值。
     * <P>
     * 通过方法 <code>getColumns</code> 返回的 <code>ResultSet</code> 中
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int columnNullable = 1;

    /**
     * 表示列的可空性未知。
     * <P>
     * 通过方法 <code>getColumns</code> 返回的 <code>ResultSet</code> 中
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int columnNullableUnknown = 2;

    /**
     * 检索表列的访问权限描述。
     *
     * <P>仅返回与列名条件匹配的权限。它们按 COLUMN_NAME 和 PRIVILEGE 排序。
     *
     * <P>每个权限描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名
     *  <LI><B>GRANTOR</B> String {@code =>} 授予权限的主体（可能是 <code>null</code>）
     *  <LI><B>GRANTEE</B> String {@code =>} 被授予权限的主体
     *  <LI><B>PRIVILEGE</B> String {@code =>} 访问名称（SELECT, INSERT, UPDATE, REFRENCES, ...）
     *  <LI><B>IS_GRANTABLE</B> String {@code =>} 如果被授予权限的主体可以授予他人，则为 "YES"；如果不能，则为 "NO"；如果未知，则为 <code>null</code>
     *  </OL>
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示目录名称不应用于缩小搜索范围
     * @param schema 模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示模式名称不应用于缩小搜索范围
     * @param table 表名称；必须与数据库中存储的表名称匹配
     * @param columnNamePattern 列名称模式；必须与数据库中存储的列名称匹配
     * @return <code>ResultSet</code> - 每行是一个列权限描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getColumnPrivileges(String catalog, String schema,
                                  String table, String columnNamePattern) throws SQLException;

    /**
     * 检索目录中每个表的访问权限描述。注意，表权限适用于表中的一个或多个列。假设此权限适用于所有列是错误的（这可能适用于某些系统，但不适用于所有系统）。
     *
     * <P>仅返回与模式和表名称条件匹配的权限。它们按
     * <code>TABLE_CAT</code>、
     * <code>TABLE_SCHEM</code>、<code>TABLE_NAME</code> 和 <code>PRIVILEGE</code> 排序。
     *
     * <P>每个权限描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>GRANTOR</B> String {@code =>} 授予权限的主体（可能是 <code>null</code>）
     *  <LI><B>GRANTEE</B> String {@code =>} 被授予权限的主体
     *  <LI><B>PRIVILEGE</B> String {@code =>} 访问名称（SELECT, INSERT, UPDATE, REFRENCES, ...）
     *  <LI><B>IS_GRANTABLE</B> String {@code =>} 如果被授予权限的主体可以授予他人，则为 "YES"；如果不能，则为 "NO"；如果未知，则为 <code>null</code>
     *  </OL>
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示目录名称不应用于缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示模式名称不应用于缩小搜索范围
     * @param tableNamePattern 表名称模式；必须与数据库中存储的表名称匹配
     * @return <code>ResultSet</code> - 每行是一个表权限描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     */
    ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                 String tableNamePattern) throws SQLException;

    /**
     * 检索表的一组最佳列，这些列可以唯一地标识一行。它们按 SCOPE 排序。
     *
     * <P>每个列描述包含以下列：
     *  <OL>
     *  <LI><B>SCOPE</B> short {@code =>} 结果的实际范围
     *      <UL>
     *      <LI> bestRowTemporary - 非常临时，仅在使用行时有效
     *      <LI> bestRowTransaction - 在当前事务的剩余时间内有效
     *      <LI> bestRowSession - 在当前会话的剩余时间内有效
     *      </UL>
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自 java.sql.Types 的 SQL 数据类型
     *  <LI><B>TYPE_NAME</B> String {@code =>} 数据源依赖的类型名称，对于 UDT，类型名称是完全限定的
     *  <LI><B>COLUMN_SIZE</B> int {@code =>} 精度
     *  <LI><B>BUFFER_LENGTH</B> int {@code =>} 未使用
     *  <LI><B>DECIMAL_DIGITS</B> short  {@code =>} 小数位数 - 对于不适用 DECIMAL_DIGITS 的数据类型，返回 Null
     *  <LI><B>PSEUDO_COLUMN</B> short {@code =>} 是否为伪列，如 Oracle ROWID
     *      <UL>
     *      <LI> bestRowUnknown - 可能是或不是伪列
     *      <LI> bestRowNotPseudo - 不是伪列
     *      <LI> bestRowPseudo - 是伪列
     *      </UL>
     *  </OL>
     *
     * <p>COLUMN_SIZE 列表示给定列的指定列大小。对于数值数据，这是最大精度。对于字符数据，这是字符长度。对于日期时间数据类型，这是字符串表示的长度（假设分数秒组件的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，这是字节长度。对于列大小不适用的数据类型，返回 Null。
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示目录名称不应用于缩小搜索范围
     * @param schema 模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示模式名称不应用于缩小搜索范围
     * @param table 表名称；必须与数据库中存储的表名称匹配
     * @param scope 感兴趣的范围；使用与 SCOPE 相同的值
     * @param nullable 包括可为空的列。
     * @return <code>ResultSet</code> - 每行是一个列描述
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getBestRowIdentifier(String catalog, String schema,
                                   String table, int scope, boolean nullable) throws SQLException;


                /**
     * 表示最佳行标识符的范围非常临时，仅在行被使用时有效。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>SCOPE</code> 的一个可能值。
     */
    int bestRowTemporary   = 0;

    /**
     * 表示最佳行标识符的范围是当前事务的剩余部分。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>SCOPE</code> 的一个可能值。
     */
    int bestRowTransaction = 1;

    /**
     * 表示最佳行标识符的范围是当前会话的剩余部分。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>SCOPE</code> 的一个可能值。
     */
    int bestRowSession     = 2;

    /**
     * 表示最佳行标识符可能是或可能不是伪列。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int bestRowUnknown  = 0;

    /**
     * 表示最佳行标识符不是伪列。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int bestRowNotPseudo        = 1;

    /**
     * 表示最佳行标识符是伪列。
     * <P>
     * 方法 <code>getBestRowIdentifier</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int bestRowPseudo   = 2;

    /**
     * 检索当行中的任何值更新时自动更新的表的列的描述。它们是无序的。
     *
     * <P>每个列描述包含以下列：
     *  <OL>
     *  <LI><B>SCOPE</B> short {@code =>} 未使用
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自 <code>java.sql.Types</code> 的 SQL 数据类型
     *  <LI><B>TYPE_NAME</B> String {@code =>} 数据源依赖的类型名
     *  <LI><B>COLUMN_SIZE</B> int {@code =>} 精度
     *  <LI><B>BUFFER_LENGTH</B> int {@code =>} 列值的字节长度
     *  <LI><B>DECIMAL_DIGITS</B> short  {@code =>} 小数位数 - 对于不适用小数位数的数据类型，返回 Null。
     *  <LI><B>PSEUDO_COLUMN</B> short {@code =>} 是否为伪列，如 Oracle 的 ROWID
     *      <UL>
     *      <LI> versionColumnUnknown - 可能是或可能不是伪列
     *      <LI> versionColumnNotPseudo - 不是伪列
     *      <LI> versionColumnPseudo - 是伪列
     *      </UL>
     *  </OL>
     *
     * <p>COLUMN_SIZE 列表示给定列的指定列大小。
     * 对于数值数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示的长度（假设小数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，
     * 这是字节长度。对于不适用列大小的数据类型，返回 Null。
     * @param catalog 目录名；必须与数据库中存储的目录名匹配；"" 检索没有目录的；<code>null</code> 表示不应使用目录名来缩小搜索范围
     * @param schema 模式名；必须与数据库中存储的模式名匹配；"" 检索没有模式的；<code>null</code> 表示不应使用模式名来缩小搜索范围
     * @param table 表名；必须与数据库中存储的表名匹配
     * @return 一个 <code>ResultSet</code> 对象，其中每一行都是一个列描述
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getVersionColumns(String catalog, String schema,
                                String table) throws SQLException;

    /**
     * 表示此版本列可能是或可能不是伪列。
     * <P>
     * 方法 <code>getVersionColumns</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int versionColumnUnknown    = 0;

    /**
     * 表示此版本列不是伪列。
     * <P>
     * 方法 <code>getVersionColumns</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int versionColumnNotPseudo  = 1;

    /**
     * 表示此版本列是伪列。
     * <P>
     * 方法 <code>getVersionColumns</code> 返回的 <code>ResultSet</code> 对象中
     * 列 <code>PSEUDO_COLUMN</code> 的一个可能值。
     */
    int versionColumnPseudo     = 2;

    /**
     * 检索给定表的主键列的描述。它们按 COLUMN_NAME 排序。
     *
     * <P>每个主键列描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名
     *  <LI><B>KEY_SEQ</B> short {@code =>} 在主键中的序列号（值 1 表示主键中的第一列，值 2 表示主键中的第二列）。
     *  <LI><B>PK_NAME</B> String {@code =>} 主键名（可能是 <code>null</code>）
     *  </OL>
     *
     * @param catalog 目录名；必须与数据库中存储的目录名匹配；"" 检索没有目录的；<code>null</code> 表示不应使用目录名来缩小搜索范围
     * @param schema 模式名；必须与数据库中存储的模式名匹配；"" 检索没有模式的；<code>null</code> 表示不应使用模式名来缩小搜索范围
     * @param table 表名；必须与数据库中存储的表名匹配
     * @return <code>ResultSet</code> - 每一行都是一个主键列描述
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getPrimaryKeys(String catalog, String schema,
                             String table) throws SQLException;

                /**
     * 获取由给定表的外键列引用的主键列的描述（即表导入的主键）。它们按照 PKTABLE_CAT、
     * PKTABLE_SCHEM、PKTABLE_NAME 和 KEY_SEQ 排序。
     *
     * <P>每个主键列描述包含以下列：
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String {@code =>} 被导入的主键表目录（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_SCHEM</B> String {@code =>} 被导入的主键表模式（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_NAME</B> String {@code =>} 被导入的主键表名称
     *  <LI><B>PKCOLUMN_NAME</B> String {@code =>} 被导入的主键列名称
     *  <LI><B>FKTABLE_CAT</B> String {@code =>} 外键表目录（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_SCHEM</B> String {@code =>} 外键表模式（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_NAME</B> String {@code =>} 外键表名称
     *  <LI><B>FKCOLUMN_NAME</B> String {@code =>} 外键列名称
     *  <LI><B>KEY_SEQ</B> short {@code =>} 外键中的序列号（值 1 表示外键中的第一列，值 2 表示外键中的第二列）。
     *  <LI><B>UPDATE_RULE</B> short {@code =>} 当主键更新时，外键会发生什么：
     *      <UL>
     *      <LI> importedNoAction - 如果主键已被导入，则不允许更新主键
     *      <LI> importedKeyCascade - 将导入的键更改为与主键更新一致
     *      <LI> importedKeySetNull - 如果主键已被更新，则将导入的键更改为 <code>NULL</code>
     *      <LI> importedKeySetDefault - 如果主键已被更新，则将导入的键更改为默认值
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同（为了 ODBC 2.x 兼容性）
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short {@code =>} 当主键被删除时，外键会发生什么。
     *      <UL>
     *      <LI> importedKeyNoAction - 如果主键已被导入，则不允许删除主键
     *      <LI> importedKeyCascade - 删除导入了已删除键的行
     *      <LI> importedKeySetNull - 如果主键已被删除，则将导入的键更改为 NULL
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同（为了 ODBC 2.x 兼容性）
     *      <LI> importedKeySetDefault - 如果主键已被删除，则将导入的键更改为默认值
     *      </UL>
     *  <LI><B>FK_NAME</B> String {@code =>} 外键名称（可能是 <code>null</code>）
     *  <LI><B>PK_NAME</B> String {@code =>} 主键名称（可能是 <code>null</code>）
     *  <LI><B>DEFERRABILITY</B> short {@code =>} 是否可以将外键约束的评估推迟到提交时
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - 请参阅 SQL92 的定义
     *      <LI> importedKeyInitiallyImmediate - 请参阅 SQL92 的定义
     *      <LI> importedKeyNotDeferrable - 请参阅 SQL92 的定义
     *      </UL>
     *  </OL>
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示不应使用目录名称来缩小搜索范围
     * @param schema 模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示不应使用模式名称来缩小搜索范围
     * @param table 表名称；必须与数据库中存储的表名称匹配
     * @return <code>ResultSet</code> - 每行是一个主键列描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getExportedKeys
     */
    ResultSet getImportedKeys(String catalog, String schema,
                              String table) throws SQLException;

    /**
     * 对于列 <code>UPDATE_RULE</code>，表示
     * 当主键被更新时，外键（导入的键）将更改为与之匹配。
     * 对于列 <code>DELETE_RULE</code>，表示
     * 当主键被删除时，导入该键的行将被删除。
     * <P>
     * <code>ResultSet</code> 对象中列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code> 的可能值，这些对象由方法
     * <code>getImportedKeys</code>、<code>getExportedKeys</code> 和
     * <code>getCrossReference</code> 返回。
     */
    int importedKeyCascade      = 0;

    /**
     * 对于列 <code>UPDATE_RULE</code>，表示
     * 如果主键已被其他表作为外键导入，则不允许更新主键。
     * 对于列 <code>DELETE_RULE</code>，表示
     * 如果主键已被其他表作为外键导入，则不允许删除主键。
     * <P>
     * <code>ResultSet</code> 对象中列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code> 的可能值，这些对象由方法
     * <code>getImportedKeys</code>、<code>getExportedKeys</code> 和
     * <code>getCrossReference</code> 返回。
     */
    int importedKeyRestrict = 1;

    /**
     * 对于列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code>，表示
     * 当主键被更新或删除时，外键（导入的键）将更改为 <code>NULL</code>。
     * <P>
     * <code>ResultSet</code> 对象中列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code> 的可能值，这些对象由方法
     * <code>getImportedKeys</code>、<code>getExportedKeys</code> 和
     * <code>getCrossReference</code> 返回。
     */
    int importedKeySetNull  = 2;

                /**
     * 对于列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code>，表示
     * 如果主键已被导入，则不能更新或删除。
     * <P>
     * 由方法 <code>getImportedKeys</code>，<code>getExportedKeys</code>，
     * 和 <code>getCrossReference</code> 返回的 <code>ResultSet</code>
     * 对象中 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code> 列的可能值。
     */
    int importedKeyNoAction = 3;

    /**
     * 对于列 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code>，表示
     * 如果主键被更新或删除，外键（导入的键）
     * 将被设置为默认值。
     * <P>
     * 由方法 <code>getImportedKeys</code>，<code>getExportedKeys</code>，
     * 和 <code>getCrossReference</code> 返回的 <code>ResultSet</code>
     * 对象中 <code>UPDATE_RULE</code>
     * 和 <code>DELETE_RULE</code> 列的可能值。
     */
    int importedKeySetDefault  = 4;

    /**
     * 表示可延迟性。参见 SQL-92 的定义。
     * <P>
     * 由方法 <code>getImportedKeys</code>，<code>getExportedKeys</code>，
     * 和 <code>getCrossReference</code> 返回的 <code>ResultSet</code>
     * 对象中 <code>DEFERRABILITY</code> 列的可能值。
     */
    int importedKeyInitiallyDeferred  = 5;

    /**
     * 表示可延迟性。参见 SQL-92 的定义。
     * <P>
     * 由方法 <code>getImportedKeys</code>，<code>getExportedKeys</code>，
     * 和 <code>getCrossReference</code> 返回的 <code>ResultSet</code>
     * 对象中 <code>DEFERRABILITY</code> 列的可能值。
     */
    int importedKeyInitiallyImmediate  = 6;

    /**
     * 表示可延迟性。参见 SQL-92 的定义。
     * <P>
     * 由方法 <code>getImportedKeys</code>，<code>getExportedKeys</code>，
     * 和 <code>getCrossReference</code> 返回的 <code>ResultSet</code>
     * 对象中 <code>DEFERRABILITY</code> 列的可能值。
     */
    int importedKeyNotDeferrable  = 7;

    /**
     * 检索描述引用给定表的主键列的外键列（由表导出的外键）。它们按 FKTABLE_CAT、FKTABLE_SCHEM、
     * FKTABLE_NAME 和 KEY_SEQ 排序。
     *
     * <P>每个外键列描述包含以下列：
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String {@code =>} 主键表目录（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_SCHEM</B> String {@code =>} 主键表模式（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_NAME</B> String {@code =>} 主键表名称
     *  <LI><B>PKCOLUMN_NAME</B> String {@code =>} 主键列名称
     *  <LI><B>FKTABLE_CAT</B> String {@code =>} 被导出的外键表目录（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_SCHEM</B> String {@code =>} 被导出的外键表模式（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_NAME</B> String {@code =>} 被导出的外键表名称
     *  <LI><B>FKCOLUMN_NAME</B> String {@code =>} 被导出的外键列名称
     *  <LI><B>KEY_SEQ</B> short {@code =>} 在外键中的序列号（值为 1 表示外键中的第一列，值为 2 表示外键中的第二列）。
     *  <LI><B>UPDATE_RULE</B> short {@code =>} 当主键更新时对外键发生的情况：
     *      <UL>
     *      <LI> importedNoAction - 如果主键已被导入，则不允许更新主键
     *      <LI> importedKeyCascade - 更改导入的键以与主键更新一致
     *      <LI> importedKeySetNull - 如果主键已被更新，则将导入的键更改为 <code>NULL</code>
     *      <LI> importedKeySetDefault - 如果主键已被更新，则将导入的键更改为默认值
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同
     *                                 （为了 ODBC 2.x 兼容性）
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short {@code =>} 当主键删除时对外键发生的情况。
     *      <UL>
     *      <LI> importedKeyNoAction - 如果主键已被导入，则不允许删除主键
     *      <LI> importedKeyCascade - 删除导入了已删除键的行
     *      <LI> importedKeySetNull - 如果主键已被删除，则将导入的键更改为 <code>NULL</code>
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同
     *                                 （为了 ODBC 2.x 兼容性）
     *      <LI> importedKeySetDefault - 如果主键已被删除，则将导入的键更改为默认值
     *      </UL>
     *  <LI><B>FK_NAME</B> String {@code =>} 外键名称（可能是 <code>null</code>）
     *  <LI><B>PK_NAME</B> String {@code =>} 主键名称（可能是 <code>null</code>）
     *  <LI><B>DEFERRABILITY</B> short {@code =>} 是否可以将外键约束的评估延迟到提交
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - 参见 SQL92 的定义
     *      <LI> importedKeyInitiallyImmediate - 参见 SQL92 的定义
     *      <LI> importedKeyNotDeferrable - 参见 SQL92 的定义
     *      </UL>
     *  </OL>
     *
     * @param catalog 目录名称；必须与存储在数据库中的目录名称匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示不应使用目录名称来缩小搜索范围
     * @param schema 模式名称；必须与存储在数据库中的模式名称匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示不应使用模式名称来缩小搜索范围
     * @param table 表名称；必须与存储在数据库中的表名称匹配
     * @return 一个 <code>ResultSet</code> 对象，其中每一行都是一个
     *         外键列描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getImportedKeys
     */
    ResultSet getExportedKeys(String catalog, String schema,
                              String table) throws SQLException;


                /**
     * 获取给定外键表中引用父表主键或唯一约束的列的描述（可能是同一张表或不同的表）。
     * 从父表返回的列数必须与组成外键的列数相匹配。它们
     * 按 FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME 和
     * KEY_SEQ 排序。
     *
     * <P>每个外键列描述包含以下列：
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String {@code =>} 父键表目录（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_SCHEM</B> String {@code =>} 父键表模式（可能是 <code>null</code>）
     *  <LI><B>PKTABLE_NAME</B> String {@code =>} 父键表名
     *  <LI><B>PKCOLUMN_NAME</B> String {@code =>} 父键列名
     *  <LI><B>FKTABLE_CAT</B> String {@code =>} 被导出的外键表目录（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_SCHEM</B> String {@code =>} 被导出的外键表模式（可能是 <code>null</code>）
     *  <LI><B>FKTABLE_NAME</B> String {@code =>} 被导出的外键表名
     *  <LI><B>FKCOLUMN_NAME</B> String {@code =>} 被导出的外键列名
     *  <LI><B>KEY_SEQ</B> short {@code =>} 在外键中的序列号（值为 1 表示外键的第一列，值为 2 表示外键的第二列）。
     *  <LI><B>UPDATE_RULE</B> short {@code =>} 当父键更新时对外键的影响：
     *      <UL>
     *      <LI> importedNoAction - 如果已导入则不允许更新父键
     *      <LI> importedKeyCascade - 更改导入的键以与父键更新一致
     *      <LI> importedKeySetNull - 如果其父键已更新，则将导入的键更改为 <code>NULL</code>
     *      <LI> importedKeySetDefault - 如果其父键已更新，则将导入的键更改为默认值
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同
     *                                 （为了 ODBC 2.x 兼容性）
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short {@code =>} 当父键删除时对外键的影响。
     *      <UL>
     *      <LI> importedKeyNoAction - 如果已导入则不允许删除父键
     *      <LI> importedKeyCascade - 删除导入了已删除键的行
     *      <LI> importedKeySetNull - 如果其主键已删除，则将导入的键更改为 <code>NULL</code>
     *      <LI> importedKeyRestrict - 与 importedKeyNoAction 相同
     *                                 （为了 ODBC 2.x 兼容性）
     *      <LI> importedKeySetDefault - 如果其父键已删除，则将导入的键更改为默认值
     *      </UL>
     *  <LI><B>FK_NAME</B> String {@code =>} 外键名称（可能是 <code>null</code>）
     *  <LI><B>PK_NAME</B> String {@code =>} 父键名称（可能是 <code>null</code>）
     *  <LI><B>DEFERRABILITY</B> short {@code =>} 是否可以将外键约束的评估延迟到提交时
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - 请参见 SQL92 定义
     *      <LI> importedKeyInitiallyImmediate - 请参见 SQL92 定义
     *      <LI> importedKeyNotDeferrable - 请参见 SQL92 定义
     *      </UL>
     *  </OL>
     *
     * @param parentCatalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；<code>null</code> 表示从选择条件中删除目录名称
     * @param parentSchema 模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     * <code>null</code> 表示从选择条件中删除模式名称
     * @param parentTable 导出键的表名；必须与数据库中存储的表名匹配
     * @param foreignCatalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的项；<code>null</code> 表示从选择条件中删除目录名称
     * @param foreignSchema 模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的项；
     * <code>null</code> 表示从选择条件中删除模式名称
     * @param foreignTable 导入键的表名；必须与数据库中存储的表名匹配
     * @return <code>ResultSet</code> - 每行是一个外键列描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getImportedKeys
     */
    ResultSet getCrossReference(
                                String parentCatalog, String parentSchema, String parentTable,
                                String foreignCatalog, String foreignSchema, String foreignTable
                                ) throws SQLException;

    /**
     * 获取此数据库支持的所有数据类型的描述。它们按 DATA_TYPE 排序，然后按数据类型与相应 JDBC SQL 类型的映射程度排序。
     *
     * <P>如果数据库支持 SQL 区分类型，则 getTypeInfo() 将返回一行，其 TYPE_NAME 为 DISTINCT，DATA_TYPE 为 Types.DISTINCT。
     * 如果数据库支持 SQL 结构化类型，则 getTypeInfo() 将返回一行，其 TYPE_NAME 为 STRUCT，DATA_TYPE 为 Types.STRUCT。
     *
     * <P>如果支持 SQL 区分或结构化类型，则可以通过 getUDTs() 方法获取单个类型的信息。
     *

     *
     * <P>每个类型描述包含以下列：
     *  <OL>
     *  <LI><B>TYPE_NAME</B> String {@code =>} 类型名称
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自 java.sql.Types 的 SQL 数据类型
     *  <LI><B>PRECISION</B> int {@code =>} 最大精度
     *  <LI><B>LITERAL_PREFIX</B> String {@code =>} 用于引用字面量的前缀
     *      （可能是 <code>null</code>）
     *  <LI><B>LITERAL_SUFFIX</B> String {@code =>} 用于引用字面量的后缀
     (可能是 <code>null</code>)
     *  <LI><B>CREATE_PARAMS</B> String {@code =>} 创建类型时使用的参数
     *      （可能是 <code>null</code>）
     *  <LI><B>NULLABLE</B> short {@code =>} 是否可以使用 NULL 为此类型。
     *      <UL>
     *      <LI> typeNoNulls - 不允许 NULL 值
     *      <LI> typeNullable - 允许 NULL 值
     *      <LI> typeNullableUnknown - NULL 性未知
     *      </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean{@code =>} 是否区分大小写。
     *  <LI><B>SEARCHABLE</B> short {@code =>} 是否可以基于此类型使用 "WHERE"：
     *      <UL>
     *      <LI> typePredNone - 不支持
     *      <LI> typePredChar - 仅支持 WHERE .. LIKE
     *      <LI> typePredBasic - 除 WHERE .. LIKE 外均支持
     *      <LI> typeSearchable - 支持所有 WHERE ..
     *      </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean {@code =>} 是否为无符号。
     *  <LI><B>FIXED_PREC_SCALE</B> boolean {@code =>} 是否可以是货币值。
     *  <LI><B>AUTO_INCREMENT</B> boolean {@code =>} 是否可以用于自增值。
     *  <LI><B>LOCAL_TYPE_NAME</B> String {@code =>} 类型名称的本地化版本
     *      （可能是 <code>null</code>）
     *  <LI><B>MINIMUM_SCALE</B> short {@code =>} 支持的最小比例
     *  <LI><B>MAXIMUM_SCALE</B> short {@code =>} 支持的最大比例
     *  <LI><B>SQL_DATA_TYPE</B> int {@code =>} 未使用
     *  <LI><B>SQL_DATETIME_SUB</B> int {@code =>} 未使用
     *  <LI><B>NUM_PREC_RADIX</B> int {@code =>} 通常是 2 或 10
     *  </OL>
     *
     * <p>PRECISION 列表示服务器支持给定数据类型的最大列大小。
     * 对于数字数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，
     * 这是字节长度。对于列大小不适用的数据类型，返回 null。
     *
     * @return 一个 <code>ResultSet</code> 对象，其中每行是一个 SQL
     *         类型描述
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getTypeInfo() throws SQLException;

                /**
     * 表示此数据类型不允许 <code>NULL</code> 值。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int typeNoNulls = 0;

    /**
     * 表示此数据类型允许 <code>NULL</code> 值。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int typeNullable = 1;

    /**
     * 表示不确定此数据类型是否允许 <code>NULL</code> 值。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>NULLABLE</code> 的一个可能值。
     */
    int typeNullableUnknown = 2;

    /**
     * 表示此类型不支持 <code>WHERE</code> 搜索子句。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>SEARCHABLE</code> 的一个可能值。
     */
    int typePredNone = 0;

    /**
     * 表示数据类型只能用于使用 <code>LIKE</code> 谓词的 <code>WHERE</code> 搜索子句。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>SEARCHABLE</code> 的一个可能值。
     */
    int typePredChar = 1;

    /**
     * 表示数据类型只能用于不使用 <code>LIKE</code> 谓词的 <code>WHERE</code> 搜索子句。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>SEARCHABLE</code> 的一个可能值。
     */
    int typePredBasic = 2;

    /**
     * 表示所有 <code>WHERE</code> 搜索子句都可以基于此类型。
     * <P>
     * 通过方法 <code>getTypeInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>SEARCHABLE</code> 的一个可能值。
     */
    int typeSearchable  = 3;

    /**
     * 检索给定表的索引和统计信息的描述。它们按 NON_UNIQUE, TYPE, INDEX_NAME 和 ORDINAL_POSITION 排序。
     *
     * <P>每个索引列描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>NON_UNIQUE</B> boolean {@code =>} 索引值是否可以是非唯一的。
     *      当 TYPE 为 tableIndexStatistic 时为 false
     *  <LI><B>INDEX_QUALIFIER</B> String {@code =>} 索引目录（可能是 <code>null</code>）；
     *      当 TYPE 为 tableIndexStatistic 时为 <code>null</code>
     *  <LI><B>INDEX_NAME</B> String {@code =>} 索引名；当 TYPE 为
     *      tableIndexStatistic 时为 <code>null</code>
     *  <LI><B>TYPE</B> short {@code =>} 索引类型：
     *      <UL>
     *      <LI> tableIndexStatistic - 与表的索引描述一起返回的表统计信息
     *      <LI> tableIndexClustered - 这是一个聚簇索引
     *      <LI> tableIndexHashed - 这是一个哈希索引
     *      <LI> tableIndexOther - 这是其他类型的索引
     *      </UL>
     *  <LI><B>ORDINAL_POSITION</B> short {@code =>} 索引内的列顺序号；
     *      当 TYPE 为 tableIndexStatistic 时为零
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名；当 TYPE 为
     *      tableIndexStatistic 时为 <code>null</code>
     *  <LI><B>ASC_OR_DESC</B> String {@code =>} 列排序顺序，"A" {@code =>} 升序，
     *      "D" {@code =>} 降序，如果排序顺序不支持则可能为 <code>null</code>；
     *      当 TYPE 为 tableIndexStatistic 时为 <code>null</code>
     *  <LI><B>CARDINALITY</B> long {@code =>} 当 TYPE 为 tableIndexStatistic 时，
     *      这是表中的行数；否则，这是索引中的唯一值数。
     *  <LI><B>PAGES</B> long {@code =>} 当 TYPE 为 tableIndexStatistic 时，
     *      这是表使用的页数；否则，这是当前索引使用的页数。
     *  <LI><B>FILTER_CONDITION</B> String {@code =>} 过滤条件，如果有。
     *      （可能是 <code>null</code>）
     *  </OL>
     *
     * @param catalog 目录名；必须与存储在数据库中的目录名匹配；"" 检索那些没有目录的；
     *        <code>null</code> 表示目录名不应用于缩小搜索范围
     * @param schema 模式名；必须与存储在数据库中的模式名匹配；"" 检索那些没有模式的；
     *        <code>null</code> 表示模式名不应用于缩小搜索范围
     * @param table 表名；必须与存储在数据库中的表名匹配
     * @param unique 当为 true 时，仅返回唯一值的索引；
     *     当为 false 时，无论是否唯一都返回索引
     * @param approximate 当为 true 时，结果可以反映近似或过时的值；当为 false 时，请求结果准确
     * @return <code>ResultSet</code> - 每行是一个索引列描述
     * @exception SQLException 如果发生数据库访问错误
     */
    ResultSet getIndexInfo(String catalog, String schema, String table,
                           boolean unique, boolean approximate)
        throws SQLException;

    /**
     * 表示此列包含与表的索引描述一起返回的表统计信息。
     * <P>
     * 通过方法 <code>getIndexInfo</code> 返回的 <code>ResultSet</code> 对象中，
     * 列 <code>TYPE</code> 的一个可能值。
     */
    short tableIndexStatistic = 0;

                /**
     * 表示此表索引是聚集索引。
     * <P>
     * 方法 <code>getIndexInfo</code> 返回的 <code>ResultSet</code> 对象中
     * <code>TYPE</code> 列的可能值。
     */
    short tableIndexClustered = 1;

    /**
     * 表示此表索引是哈希索引。
     * <P>
     * 方法 <code>getIndexInfo</code> 返回的 <code>ResultSet</code> 对象中
     * <code>TYPE</code> 列的可能值。
     */
    short tableIndexHashed    = 2;

    /**
     * 表示此表索引既不是聚集索引，也不是哈希索引或表统计信息；
     * 它是其他类型。
     * <P>
     * 方法 <code>getIndexInfo</code> 返回的 <code>ResultSet</code> 对象中
     * <code>TYPE</code> 列的可能值。
     */
    short tableIndexOther     = 3;

    //--------------------------JDBC 2.0-----------------------------

    /**
     * 检索此数据库是否支持给定的结果集类型。
     *
     * @param type 定义在 <code>java.sql.ResultSet</code> 中
     * @return 如果支持则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @see Connection
     * @since 1.2
     */
    boolean supportsResultSetType(int type) throws SQLException;

    /**
     * 检索此数据库是否支持给定的结果集类型与给定的并发类型组合。
     *
     * @param type 定义在 <code>java.sql.ResultSet</code> 中
     * @param concurrency 类型定义在 <code>java.sql.ResultSet</code> 中
     * @return 如果支持则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @see Connection
     * @since 1.2
     */
    boolean supportsResultSetConcurrency(int type, int concurrency)
        throws SQLException;

    /**
     * 检索给定类型的结果集对象中，结果集自身的更新是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型的更新可见则为 <code>true</code>；
     *        否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean ownUpdatesAreVisible(int type) throws SQLException;

    /**
     * 检索结果集自身的删除是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型的删除可见则为 <code>true</code>；
     *        否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean ownDeletesAreVisible(int type) throws SQLException;

    /**
     * 检索结果集自身的插入是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型的插入可见则为 <code>true</code>；
     *        否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean ownInsertsAreVisible(int type) throws SQLException;

    /**
     * 检索他人所做的更新是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型中他人所做的更新可见则为 <code>true</code>；
     *        否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean othersUpdatesAreVisible(int type) throws SQLException;

    /**
     * 检索他人所做的删除是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型中他人所做的删除可见则为 <code>true</code>；
     *        否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean othersDeletesAreVisible(int type) throws SQLException;

    /**
     * 检索他人所做的插入是否可见。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型中他人所做的插入可见则为 <code>true</code>；
     *         否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean othersInsertsAreVisible(int type) throws SQLException;

    /**
     * 检索是否可以通过调用方法 <code>ResultSet.rowUpdated</code> 检测到可见的行更新。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定结果集类型可以检测到更改则为 <code>true</code>；
     *         否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean updatesAreDetected(int type) throws SQLException;

                /**
     * 获取通过调用方法 <code>ResultSet.rowDeleted</code> 是否可以检测到可见行删除。
     * 如果方法 <code>deletesAreDetected</code> 返回 <code>false</code>，则表示删除的行已从结果集中移除。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果给定的结果集类型可以检测到删除，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean deletesAreDetected(int type) throws SQLException;

    /**
     * 获取通过调用方法 <code>ResultSet.rowInserted</code> 是否可以检测到可见行插入。
     *
     * @param type <code>ResultSet</code> 类型；可以是
     *        <code>ResultSet.TYPE_FORWARD_ONLY</code>、
     *        <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> 或
     *        <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return 如果指定的结果集类型可以检测到更改，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean insertsAreDetected(int type) throws SQLException;

    /**
     * 获取此数据库是否支持批处理更新。
     *
     * @return 如果此数据库支持批处理更新，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    boolean supportsBatchUpdates() throws SQLException;

    /**
     * 获取特定模式中定义的用户定义类型（UDTs）的描述。特定模式的 UDTs 可能具有类型
     * <code>JAVA_OBJECT</code>、<code>STRUCT</code> 或 <code>DISTINCT</code>。
     *
     * <P>仅返回与目录、模式、类型名称和类型标准匹配的类型。它们按 <code>DATA_TYPE</code>、
     * <code>TYPE_CAT</code>、<code>TYPE_SCHEM</code> 和 <code>TYPE_NAME</code> 排序。
     * 类型名称参数可以是完全限定的名称。在这种情况下，将忽略目录和模式参数。
     *
     * <P>每个类型描述包含以下列：
     *  <OL>
     *  <LI><B>TYPE_CAT</B> String {@code =>} 类型的目录（可能是 <code>null</code>）
     *  <LI><B>TYPE_SCHEM</B> String {@code =>} 类型的模式（可能是 <code>null</code>）
     *  <LI><B>TYPE_NAME</B> String {@code =>} 类型名称
     *  <LI><B>CLASS_NAME</B> String {@code =>} Java 类名称
     *  <LI><B>DATA_TYPE</B> int {@code =>} 在 java.sql.Types 中定义的类型值。
     *     可以是 JAVA_OBJECT、STRUCT 或 DISTINCT 之一
     *  <LI><B>REMARKS</B> String {@code =>} 类型的解释性注释
     *  <LI><B>BASE_TYPE</B> short {@code =>} DISTINCT 类型的源类型代码或实现用户生成的
     *     自引用列的引用类型的类型代码，如 java.sql.Types 中定义的结构化类型（如果 DATA_TYPE 不是
     *     DISTINCT 或不是 REFERENCE_GENERATION = USER_DEFINED 的 STRUCT，则为 <code>null</code>）
     *  </OL>
     *
     * <P><B>注意：</B> 如果驱动程序不支持 UDTs，则返回空的结果集。
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的；
     *        <code>null</code> 表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式模式名称；必须与数据库中存储的模式名称匹配；"" 检索没有模式的；
     *        <code>null</code> 表示不应使用模式名称来缩小搜索范围
     * @param typeNamePattern 类型名称模式；必须与数据库中存储的类型名称匹配；可以是完全限定的名称
     * @param types 要包含的用户定义类型（JAVA_OBJECT、STRUCT 或 DISTINCT）列表；<code>null</code> 返回所有类型
     * @return <code>ResultSet</code> 对象，其中每一行描述一个 UDT
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     * @since 1.2
     */
    ResultSet getUDTs(String catalog, String schemaPattern,
                      String typeNamePattern, int[] types)
        throws SQLException;

    /**
     * 获取生成此元数据对象的连接。
     * <P>
     * @return 生成此元数据对象的连接
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.2
     */
    Connection getConnection() throws SQLException;

    // ------------------- JDBC 3.0 -------------------------

    /**
     * 获取此数据库是否支持保存点。
     *
     * @return 如果支持保存点，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean supportsSavepoints() throws SQLException;

    /**
     * 获取此数据库是否支持可调用语句中的命名参数。
     *
     * @return 如果支持命名参数，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean supportsNamedParameters() throws SQLException;

    /**
     * 获取是否可以从 <code>CallableStatement</code> 对象同时返回多个 <code>ResultSet</code> 对象。
     *
     * @return 如果 <code>CallableStatement</code> 对象可以同时返回多个 <code>ResultSet</code> 对象，
     *         则返回 <code>true</code>；否则返回 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean supportsMultipleOpenResults() throws SQLException;


/**
 * 获取在执行语句后是否可以检索自动生成的键
 *
 * @return 如果在执行语句后可以检索自动生成的键，则返回 <code>true</code>；否则返回 <code>false</code>
 *<p>如果返回 <code>true</code>，则 JDBC 驱动程序必须支持至少对 SQL INSERT 语句返回自动生成的键
 *<p>
 * @exception SQLException 如果发生数据库访问错误
 * @since 1.4
 */
boolean supportsGetGeneratedKeys() throws SQLException;

/**
 * 获取在此数据库中特定模式定义的用户定义类型 (UDT) 层次结构的描述。仅建模直接的超类型/子类型关系。
 * <P>
 * 仅返回与目录、模式和类型名称匹配的 UDT 的超类型信息。类型名称参数可以是完全限定的名称。当提供的 UDT 名称是完全限定的名称时，忽略目录和模式参数。
 * <P>
 * 如果 UDT 没有直接的超类型，则不会在此列出。此方法返回的 <code>ResultSet</code> 对象中的每一行描述指定的 UDT 和直接的超类型。每一行有以下列：
 *  <OL>
 *  <LI><B>TYPE_CAT</B> String {@code =>} UDT 的目录（可能是 <code>null</code>）
 *  <LI><B>TYPE_SCHEM</B> String {@code =>} UDT 的模式（可能是 <code>null</code>）
 *  <LI><B>TYPE_NAME</B> String {@code =>} UDT 的类型名称
 *  <LI><B>SUPERTYPE_CAT</B> String {@code =>} 直接超类型的目录（可能是 <code>null</code>）
 *  <LI><B>SUPERTYPE_SCHEM</B> String {@code =>} 直接超类型的模式（可能是 <code>null</code>）
 *  <LI><B>SUPERTYPE_NAME</B> String {@code =>} 直接超类型的名称
 *  </OL>
 *
 * <P><B>注意：</B> 如果驱动程序不支持类型层次结构，则返回空的结果集。
 *
 * @param catalog 目录名称；"" 检索没有目录的；<code>null</code> 表示从选择条件中删除目录名称
 * @param schemaPattern 模式名称模式；"" 检索没有模式的
 * @param typeNamePattern UDT 名称模式；可以是完全限定的名称
 * @return 一个 <code>ResultSet</code> 对象，其中每一行提供关于指定 UDT 的信息
 * @throws SQLException 如果发生数据库访问错误
 * @see #getSearchStringEscape
 * @since 1.4
 */
ResultSet getSuperTypes(String catalog, String schemaPattern,
                        String typeNamePattern) throws SQLException;

/**
 * 获取在此数据库中特定模式定义的表层次结构的描述。
 *
 * <P>仅返回与目录、模式和表名称匹配的表的超表信息。表名称参数可以是完全限定的名称，在这种情况下，忽略目录和模式参数。如果表没有超表，则不会在此列出。超表必须定义在与子表相同的目录和模式中。因此，类型描述不需要包含超表的此信息。
 *
 * <P>每个类型描述有以下列：
 *  <OL>
 *  <LI><B>TABLE_CAT</B> String {@code =>} 类型的目录（可能是 <code>null</code>）
 *  <LI><B>TABLE_SCHEM</B> String {@code =>} 类型的模式（可能是 <code>null</code>）
 *  <LI><B>TABLE_NAME</B> String {@code =>} 类型名称
 *  <LI><B>SUPERTABLE_NAME</B> String {@code =>} 直接超类型的名称
 *  </OL>
 *
 * <P><B>注意：</B> 如果驱动程序不支持类型层次结构，则返回空的结果集。
 *
 * @param catalog 目录名称；"" 检索没有目录的；<code>null</code> 表示从选择条件中删除目录名称
 * @param schemaPattern 模式名称模式；"" 检索没有模式的
 * @param tableNamePattern 表名称模式；可以是完全限定的名称
 * @return 一个 <code>ResultSet</code> 对象，其中每一行是一个类型描述
 * @throws SQLException 如果发生数据库访问错误
 * @see #getSearchStringEscape
 * @since 1.4
 */
ResultSet getSuperTables(String catalog, String schemaPattern,
                         String tableNamePattern) throws SQLException;

/**
 * 表示 <code>NULL</code> 值可能不允许。
 * <P>
 * 方法 <code>getAttributes</code> 返回的 <code>ResultSet</code> 对象中 <code>NULLABLE</code> 列的可能值。
 */
short attributeNoNulls = 0;

/**
 * 表示 <code>NULL</code> 值肯定允许。
 * <P>
 * 方法 <code>getAttributes</code> 返回的 <code>ResultSet</code> 对象中 <code>NULLABLE</code> 列的可能值。
 */
short attributeNullable = 1;

/**
 * 表示是否允许 <code>NULL</code> 值未知。
 * <P>
 * 方法 <code>getAttributes</code> 返回的 <code>ResultSet</code> 对象中 <code>NULLABLE</code> 列的可能值。
 */
short attributeNullableUnknown = 2;

/**
 * 获取在给定目录和模式中可用的用户定义类型 (UDT) 的给定类型的给定属性的描述。
 * <P>
 * 仅返回与目录、模式、类型和属性名称标准匹配的 UDT 的属性描述。它们按 <code>TYPE_CAT</code>、<code>TYPE_SCHEM</code>、<code>TYPE_NAME</code> 和 <code>ORDINAL_POSITION</code> 排序。此描述不包含继承的属性。
 * <P>
 * 返回的 <code>ResultSet</code> 对象有以下列：
 * <OL>
 *  <LI><B>TYPE_CAT</B> String {@code =>} 类型目录（可能是 <code>null</code>）
 *  <LI><B>TYPE_SCHEM</B> String {@code =>} 类型模式（可能是 <code>null</code>）
 *  <LI><B>TYPE_NAME</B> String {@code =>} 类型名称
 *  <LI><B>ATTR_NAME</B> String {@code =>} 属性名称
 *  <LI><B>DATA_TYPE</B> int {@code =>} 属性类型，来自 java.sql.Types 的 SQL 类型
 *  <LI><B>ATTR_TYPE_NAME</B> String {@code =>} 数据源依赖的类型名称。对于 UDT，类型名称是完全限定的。对于 REF，类型名称是完全限定的，并表示引用类型的靶类型。
 *  <LI><B>ATTR_SIZE</B> int {@code =>} 列大小。对于字符或日期类型，这是最大字符数；对于数字或十进制类型，这是精度。
 *  <LI><B>DECIMAL_DIGITS</B> int {@code =>} 小数位数。对于不适用 DECIMAL_DIGITS 的数据类型，返回 null。
 *  <LI><B>NUM_PREC_RADIX</B> int {@code =>} 基数（通常是 10 或 2）
 *  <LI><B>NULLABLE</B> int {@code =>} 是否允许 NULL
 *      <UL>
 *      <LI> attributeNoNulls - 可能不允许 NULL 值
 *      <LI> attributeNullable - 肯定允许 NULL 值
 *      <LI> attributeNullableUnknown - NULL 性未知
 *      </UL>
 *  <LI><B>REMARKS</B> String {@code =>} 描述列的注释（可能是 <code>null</code>）
 *  <LI><B>ATTR_DEF</B> String {@code =>} 默认值（可能是 <code>null</code>）
 *  <LI><B>SQL_DATA_TYPE</B> int {@code =>} 未使用
 *  <LI><B>SQL_DATETIME_SUB</B> int {@code =>} 未使用
 *  <LI><B>CHAR_OCTET_LENGTH</B> int {@code =>} 对于字符类型，列中的最大字节数
 *  <LI><B>ORDINAL_POSITION</B> int {@code =>} 属性在 UDT 中的索引（从 1 开始）
 *  <LI><B>IS_NULLABLE</B> String  {@code =>} 使用 ISO 规则确定属性的 NULL 性。
 *       <UL>
 *       <LI> YES           --- 如果属性可以包含 NULL
 *       <LI> NO            --- 如果属性不能包含 NULL
 *       <LI> 空字符串  --- 如果属性的 NULL 性未知
 *       </UL>
 *  <LI><B>SCOPE_CATALOG</B> String {@code =>} 引用属性的范围表的目录（如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
 *  <LI><B>SCOPE_SCHEMA</B> String {@code =>} 引用属性的范围表的模式（如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
 *  <LI><B>SCOPE_TABLE</B> String {@code =>} 引用属性的范围表的表名（如果 DATA_TYPE 不是 REF，则为 <code>null</code>）
 * <LI><B>SOURCE_DATA_TYPE</B> short {@code =>} 区分类型或用户生成的 Ref 类型的源类型，来自 java.sql.Types 的 SQL 类型（如果 DATA_TYPE 不是 DISTINCT 或用户生成的 REF，则为 <code>null</code>）
 *  </OL>
 * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；"" 检索没有目录的；<code>null</code> 表示不应使用目录名称来缩小搜索范围
 * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；"" 检索没有模式的；<code>null</code> 表示不应使用模式名称来缩小搜索范围
 * @param typeNamePattern 类型名称模式；必须与数据库中存储的类型名称匹配
 * @param attributeNamePattern 属性名称模式；必须与数据库中声明的属性名称匹配
 * @return 一个 <code>ResultSet</code> 对象，其中每一行是一个属性描述
 * @exception SQLException 如果发生数据库访问错误
 * @see #getSearchStringEscape
 * @since 1.4
 */
ResultSet getAttributes(String catalog, String schemaPattern,
                        String typeNamePattern, String attributeNamePattern)
    throws SQLException;

                /**
     * 获取此数据库是否支持给定的结果集保持性。
     *
     * @param holdability 以下常量之一：
     *          <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *          <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return 如果支持则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @see Connection
     * @since 1.4
     */
    boolean supportsResultSetHoldability(int holdability) throws SQLException;

    /**
     * 获取此数据库的<code>ResultSet</code>对象的默认保持性。
     *
     * @return 默认的保持性；可能是
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> 或
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getResultSetHoldability() throws SQLException;

    /**
     * 获取底层数据库的主要版本号。
     *
     * @return 底层数据库的主要版本号
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getDatabaseMajorVersion() throws SQLException;

    /**
     * 获取底层数据库的次要版本号。
     *
     * @return 底层数据库的次要版本号
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getDatabaseMinorVersion() throws SQLException;

    /**
     * 获取此驱动程序的主要JDBC版本号。
     *
     * @return JDBC版本的主要号
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getJDBCMajorVersion() throws SQLException;

    /**
     * 获取此驱动程序的次要JDBC版本号。
     *
     * @return JDBC版本的次要号
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getJDBCMinorVersion() throws SQLException;

    /**
     * 方法<code>DatabaseMetaData.getSQLStateType</code>的可能返回值之一，用于指示
     * 方法<code>SQLException.getSQLState</code>返回的值是否为
     * X/Open（现称为Open Group）SQL CLI SQLSTATE值。
     * <P>
     * @since 1.4
     */
    int sqlStateXOpen = 1;

    /**
     * 方法<code>DatabaseMetaData.getSQLStateType</code>的可能返回值之一，用于指示
     * 方法<code>SQLException.getSQLState</code>返回的值是否为SQLSTATE值。
     * <P>
     * @since 1.6
     */
    int sqlStateSQL = 2;

     /**
     * 方法<code>DatabaseMetaData.getSQLStateType</code>的可能返回值之一，用于指示
     * 方法<code>SQLException.getSQLState</code>返回的值是否为SQL99 SQLSTATE值。
     * <P>
     * <b>注意：</b>此常量仅保留用于兼容性。开发人员应使用常量<code>sqlStateSQL</code>。
     *
     * @since 1.4
     */
    int sqlStateSQL99 = sqlStateSQL;

    /**
     * 指示<code>SQLException.getSQLState</code>返回的SQLSTATE是否为X/Open（现称为Open Group）SQL CLI或SQL:2003。
     * @return SQLSTATE的类型；可能是：
     *        sqlStateXOpen 或
     *        sqlStateSQL
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getSQLStateType() throws SQLException;

    /**
     * 指示对LOB的更新是直接进行还是在LOB的副本上进行。
     * @return 如果更新是在LOB的副本上进行则返回<code>true</code>；
     *         如果更新直接对LOB进行则返回<code>false</code>
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean locatorsUpdateCopy() throws SQLException;

    /**
     * 获取此数据库是否支持语句池。
     *
     * @return 如果支持则返回<code>true</code>；否则返回<code>false</code>
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean supportsStatementPooling() throws SQLException;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * 指示此数据源是否支持SQL <code>ROWID</code>类型，如果支持，则返回<code>RowId</code>对象的有效期。
     * <p>
     * 返回的int值具有以下关系：
     * <pre>{@code
     *     ROWID_UNSUPPORTED < ROWID_VALID_OTHER < ROWID_VALID_TRANSACTION
     *         < ROWID_VALID_SESSION < ROWID_VALID_FOREVER
     * }</pre>
     * 因此可以使用如下条件逻辑：
     * <pre>{@code
     *     if (metadata.getRowIdLifetime() > DatabaseMetaData.ROWID_VALID_TRANSACTION)
     * }</pre>
     * Valid Forever表示跨所有会话有效，而有效于会话表示跨其包含的所有事务有效。
     *
     * @return 表示<code>RowId</code>有效期的状态
     * @throws SQLException 如果发生数据库访问错误
     * @since 1.6
     */
    RowIdLifetime getRowIdLifetime() throws SQLException;

    /**
     * 获取此数据库中可用的模式名称。结果
     * 按<code>TABLE_CATALOG</code>和
     * <code>TABLE_SCHEM</code>排序。
     *
     * <P>模式列如下：
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 模式名称
     *  <LI><B>TABLE_CATALOG</B> String {@code =>} 目录名称（可能是<code>null</code>）
     *  </OL>
     *
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；
     * ""检索没有目录的那些；null表示目录名称不应用于缩小搜索范围。
     * @param schemaPattern 模式名称；必须与数据库中存储的模式名称匹配；null表示
     * 模式名称不应用于缩小搜索范围。
     * @return 每行都是一个
     *         模式描述的<code>ResultSet</code>对象
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     * @since 1.6
     */
    ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException;


    /**
     * 获取此数据库是否支持使用存储过程转义语法调用用户定义或供应商函数。
     *
     * @return 如果支持则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.6
     */
    boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException;

    /**
     * 获取当自动提交为<code>true</code>时，<code>SQLException</code>是否表示所有打开的结果集都已关闭，即使这些结果集是可保持的。
     * 当自动提交为<code>true</code>时发生<code>SQLException</code>，JDBC驱动程序响应提交操作、回滚操作或不执行提交或回滚操作，这取决于供应商。这种差异可能导致可保持的结果集是否关闭。
     *
     * @return 如果支持则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.6
     */
    boolean autoCommitFailureClosesAllResultSets() throws SQLException;
        /**
         * 获取驱动程序支持的客户端信息属性列表。结果集包含以下列
         *
         * <ol>
         * <li><b>NAME</b> String{@code =>} 客户端信息属性的名称<br>
         * <li><b>MAX_LEN</b> int{@code =>} 属性值的最大长度<br>
         * <li><b>DEFAULT_VALUE</b> String{@code =>} 属性的默认值<br>
         * <li><b>DESCRIPTION</b> String{@code =>} 属性的描述。通常包含有关此属性在数据库中存储位置的信息。
         * </ol>
         * <p>
         * 结果集按NAME列排序
         * <p>
         * @return      一个<code>ResultSet</code>对象；每一行是一个支持的客户端信息属性
         * <p>
         *  @exception SQLException 如果发生数据库访问错误
         * <p>
         * @since 1.6
         */
        ResultSet getClientInfoProperties()
                throws SQLException;

    /**
     * 获取给定目录中可用的系统和用户函数的描述。
     * <P>
     * 仅返回与模式和函数名称条件匹配的系统和用户函数描述。它们按
     * <code>FUNCTION_CAT</code>、<code>FUNCTION_SCHEM</code>、
     * <code>FUNCTION_NAME</code> 和
     * <code>SPECIFIC_ NAME</code> 排序。
     *
     * <P>每个函数描述包含以下列：
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String {@code =>} 函数目录（可能是<code>null</code>）
     *  <LI><B>FUNCTION_SCHEM</B> String {@code =>} 函数模式（可能是<code>null</code>）
     *  <LI><B>FUNCTION_NAME</B> String {@code =>} 函数名称。这是调用函数时使用的名称
     *  <LI><B>REMARKS</B> String {@code =>} 函数的解释性注释
     * <LI><B>FUNCTION_TYPE</B> short {@code =>} 函数类型：
     *      <UL>
     *      <LI>functionResultUnknown - 无法确定是否返回值或表
     *      <LI> functionNoTable- 不返回表
     *      <LI> functionReturnsTable - 返回表
     *      </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  {@code =>} 在其模式中唯一标识此函数的名称。这是用户指定或DBMS生成的名称，可能与<code>FUNCTION_NAME</code>不同，例如重载函数
     *  </OL>
     * <p>
     * 用户可能没有权限执行由<code>getFunctions</code>返回的任何函数
     *
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；""检索没有目录的；<code>null</code>表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；""检索没有模式的；<code>null</code>表示不应使用模式名称来缩小搜索范围
     * @param functionNamePattern 函数名称模式；必须与数据库中存储的函数名称匹配
     * @return <code>ResultSet</code> - 每一行是一个函数描述
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     * @since 1.6
     */
    ResultSet getFunctions(String catalog, String schemaPattern,
                            String functionNamePattern) throws SQLException;
    /**
     * 获取给定目录的系统或用户函数参数和返回类型的描述。
     *
     * <P>仅返回与模式、函数和参数名称条件匹配的描述。它们按
     * <code>FUNCTION_CAT</code>、<code>FUNCTION_SCHEM</code>、
     * <code>FUNCTION_NAME</code> 和
     * <code>SPECIFIC_ NAME</code> 排序。在此范围内，如果有返回值，则首先返回。接下来是按调用顺序的参数描述。列描述按列号顺序跟随。
     *
     * <P><code>ResultSet</code>中的每一行
     * 是参数描述、列描述或返回类型描述，包含以下字段：
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String {@code =>} 函数目录（可能是<code>null</code>）
     *  <LI><B>FUNCTION_SCHEM</B> String {@code =>} 函数模式（可能是<code>null</code>）
     *  <LI><B>FUNCTION_NAME</B> String {@code =>} 函数名称。这是调用函数时使用的名称
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列/参数名称
     *  <LI><B>COLUMN_TYPE</B> Short {@code =>} 列/参数类型：
     *      <UL>
     *      <LI> functionColumnUnknown - 未知
     *      <LI> functionColumnIn - 输入参数
     *      <LI> functionColumnInOut - 输入输出参数
     *      <LI> functionColumnOut - 输出参数
     *      <LI> functionColumnReturn - 函数返回值
     *      <LI> functionColumnResult - 表示参数或列是<code>ResultSet</code>中的列
     *      </UL>
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自java.sql.Types的SQL类型
     *  <LI><B>TYPE_NAME</B> String {@code =>} SQL类型名称，对于UDT类型，类型名称是完全限定的
     *  <LI><B>PRECISION</B> int {@code =>} 精度
     *  <LI><B>LENGTH</B> int {@code =>} 数据的字节长度
     *  <LI><B>SCALE</B> short {@code =>} 小数位数 - 对于不适用小数位数的数据类型返回null
     *  <LI><B>RADIX</B> short {@code =>} 基数
     *  <LI><B>NULLABLE</B> short {@code =>} 是否可以包含NULL。
     *      <UL>
     *      <LI> functionNoNulls - 不允许NULL值
     *      <LI> functionNullable - 允许NULL值
     *      <LI> functionNullableUnknown - 可空性未知
     *      </UL>
     *  <LI><B>REMARKS</B> String {@code =>} 描述列/参数的注释
     *  <LI><B>CHAR_OCTET_LENGTH</B> int  {@code =>} 二进制和基于字符的参数或列的最大长度。对于任何其他数据类型，返回值为NULL
     *  <LI><B>ORDINAL_POSITION</B> int  {@code =>} 输入和输出参数的顺序位置，从1开始。如果此行描述函数的返回值，则返回0。
     * 对于结果集列，它是结果集中列的顺序位置，从1开始。
     *  <LI><B>IS_NULLABLE</B> String  {@code =>} 使用ISO规则确定参数或列的可空性。
     *       <UL>
     *       <LI> YES           --- 如果参数或列可以包含NULLs
     *       <LI> NO            --- 如果参数或列不能包含NULLs
     *       <LI> 空字符串  --- 如果参数或列的可空性未知
     *       </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  {@code =>} 在其模式中唯一标识此函数的名称。这是用户指定或DBMS生成的名称，可能与<code>FUNCTION_NAME</code>不同，例如重载函数
     *  </OL>
     *
     * <p>PRECISION列表示给定参数或列的指定列大小。
     * 对于数字数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示形式的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于ROWID数据类型，这是字节长度。对于不适用列大小的数据类型，返回null。
     * @param catalog 目录名称；必须与数据库中存储的目录名称匹配；""检索没有目录的；<code>null</code>表示不应使用目录名称来缩小搜索范围
     * @param schemaPattern 模式名称模式；必须与数据库中存储的模式名称匹配；""检索没有模式的；<code>null</code>表示不应使用模式名称来缩小搜索范围
     * @param functionNamePattern 函数名称模式；必须与数据库中存储的函数名称匹配
     * @param columnNamePattern 参数名称模式；必须与数据库中存储的参数或列名称匹配
     * @return <code>ResultSet</code> - 每一行描述一个用户函数参数、列或返回类型
     *
     * @exception SQLException 如果发生数据库访问错误
     * @see #getSearchStringEscape
     * @since 1.6
     */
    ResultSet getFunctionColumns(String catalog,
                                  String schemaPattern,
                                  String functionNamePattern,
                                  String columnNamePattern) throws SQLException;

    /**
     * 表示参数或列的类型未知。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     */
    int functionColumnUnknown = 0;

    /**
     * 表示参数或列是 IN 参数。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionColumnIn = 1;

    /**
     * 表示参数或列是 INOUT 参数。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionColumnInOut = 2;

    /**
     * 表示参数或列是 OUT 参数。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionColumnOut = 3;
    /**
     * 表示参数或列是返回值。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionReturn = 4;

       /**
     * 表示参数或列是结果集中的列。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code>
     * 中 <code>COLUMN_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionColumnResult = 5;


    /**
     * 表示不允许 <code>NULL</code> 值。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     * @since 1.6
     */
    int functionNoNulls = 0;

    /**
     * 表示允许 <code>NULL</code> 值。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     * @since 1.6
     */
    int functionNullable = 1;

    /**
     * 表示是否允许 <code>NULL</code> 值未知。
     * <P>
     * 由方法 <code>getFunctionColumns</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>NULLABLE</code> 列的可能值。
     * @since 1.6
     */
    int functionNullableUnknown = 2;

    /**
     * 表示未知函数是否返回结果或表。
     * <P>
     * 由方法 <code>getFunctions</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>FUNCTION_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionResultUnknown   = 0;

    /**
     * 表示函数不返回表。
     * <P>
     * 由方法 <code>getFunctions</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>FUNCTION_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionNoTable         = 1;

    /**
     * 表示函数返回表。
     * <P>
     * 由方法 <code>getFunctions</code> 返回的 <code>ResultSet</code> 对象
     * 中 <code>FUNCTION_TYPE</code> 列的可能值。
     * @since 1.6
     */
    int functionReturnsTable    = 2;

    //--------------------------JDBC 4.1 -----------------------------

    /**
     * 检索指定目录和模式中给定表的伪列或隐藏列的描述。
     * 伪列或隐藏列可能不会始终存储在表中，并且除非在查询的最外层 SELECT 列表中指定，否则在 ResultSet 中不可见。
     * 伪列或隐藏列可能不一定能够被修改。如果没有伪列或隐藏列，则返回空的 ResultSet。
     *
     * <P>仅返回与目录、模式、表和列名标准匹配的列描述。它们按
     * <code>TABLE_CAT</code>、<code>TABLE_SCHEM</code>、<code>TABLE_NAME</code>
     * 和 <code>COLUMN_NAME</code> 排序。
     *
     * <P>每个列描述包含以下列：
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String {@code =>} 表目录（可能是 <code>null</code>）
     *  <LI><B>TABLE_SCHEM</B> String {@code =>} 表模式（可能是 <code>null</code>）
     *  <LI><B>TABLE_NAME</B> String {@code =>} 表名
     *  <LI><B>COLUMN_NAME</B> String {@code =>} 列名
     *  <LI><B>DATA_TYPE</B> int {@code =>} 来自 java.sql.Types 的 SQL 类型
     *  <LI><B>COLUMN_SIZE</B> int {@code =>} 列大小。
     *  <LI><B>DECIMAL_DIGITS</B> int {@code =>} 小数位数。对于不适用 DECIMAL_DIGITS 的数据类型，返回 Null。
     *  <LI><B>NUM_PREC_RADIX</B> int {@code =>} 基数（通常是 10 或 2）
     *  <LI><B>COLUMN_USAGE</B> String {@code =>} 列的允许使用方式。返回的值将对应于 {@link PseudoColumnUsage#name PseudoColumnUsage.name()} 返回的枚举名称。
     *  <LI><B>REMARKS</B> String {@code =>} 描述列的注释（可能是 <code>null</code>）
     *  <LI><B>CHAR_OCTET_LENGTH</B> int {@code =>} 对于字符类型，列中的最大字节数
     *  <LI><B>IS_NULLABLE</B> String  {@code =>} 使用 ISO 规则确定列的可空性。
     *       <UL>
     *       <LI> YES           --- 如果列可以包含 NULL
     *       <LI> NO            --- 如果列不能包含 NULL
     *       <LI> 空字符串  --- 如果列的可空性未知
     *       </UL>
     *  </OL>
     *
     * <p>COLUMN_SIZE 列指定了给定列的列大小。对于数字数据，这是最大精度。对于字符数据，这是字符长度。
     * 对于日期时间数据类型，这是字符串表示形式的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，
     * 这是字节长度。对于不适用列大小的数据类型，返回 Null。
     *
     * @param catalog 目录名；必须与数据库中存储的目录名匹配；"" 检索没有目录的项；
     *        <code>null</code> 表示不应使用目录名来缩小搜索范围
     * @param schemaPattern 模式名模式；必须与数据库中存储的模式名匹配；"" 检索没有模式的项；
     *        <code>null</code> 表示不应使用模式名来缩小搜索范围
     * @param tableNamePattern 表名模式；必须与数据库中存储的表名匹配
     * @param columnNamePattern 列名模式；必须与数据库中存储的列名匹配
     * @return <code>ResultSet</code> - 每行是一个列描述
     * @exception SQLException 如果发生数据库访问错误
     * @see PseudoColumnUsage
     * @since 1.7
     */
    ResultSet getPseudoColumns(String catalog, String schemaPattern,
                         String tableNamePattern, String columnNamePattern)
        throws SQLException;

                /**
     * 获取如果指定的自动生成键列的列名或索引有效且语句成功执行时，是否总是返回生成的键。返回的键可能基于自动生成键的列，也可能不基于。
     * 请参阅您的JDBC驱动程序文档以获取更多详细信息。
     * @return 如果是，则返回<code>true</code>；否则返回<code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.7
     */
    boolean  generatedKeyAlwaysReturned() throws SQLException;

    //--------------------------JDBC 4.2 -----------------------------

    /**
     *
     * 获取此数据库允许的逻辑大小为 {@code LOB} 的最大字节数。
     *<p>
     * 默认实现将返回 {@code 0}
     *
     * @return 允许的最大字节数；结果为零表示没有限制或未知
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.8
     */
    default long getMaxLogicalLobSize() throws SQLException {
        return 0;
    }

    /**
     * 获取此数据库是否支持 REF CURSOR。
     *<p>
     * 默认实现将返回 {@code false}
     *
     * @return 如果此数据库支持 REF CURSOR，则返回 {@code true}；
     *         否则返回 {@code false}
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.8
     */
    default boolean supportsRefCursors() throws SQLException{
        return false;
    }

}
