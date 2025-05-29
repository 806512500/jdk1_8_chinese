
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
 * Java 编程语言中 SQL 类型 <code>ARRAY</code> 的映射。
 * 默认情况下，<code>Array</code> 值是对 SQL <code>ARRAY</code> 值的事务持续时间引用。默认情况下，<code>Array</code>
 * 对象使用内部的 SQL LOCATOR(array) 实现，这意味着 <code>Array</code> 对象包含指向 SQL <code>ARRAY</code> 值数据的逻辑指针，
 * 而不是包含 <code>ARRAY</code> 值的数据。
 * <p>
 * <code>Array</code> 接口提供了将 SQL <code>ARRAY</code> 值的数据作为数组或 <code>ResultSet</code> 对象带入客户端的方法。
 * 如果 SQL <code>ARRAY</code> 的元素是 UDT，则可以进行自定义映射。要创建自定义映射，程序员必须做两件事：
 * <ul>
 * <li>为要自定义映射的 UDT 创建一个实现 {@link SQLData} 接口的类。
 * <li>在类型映射中进行条目，其中包含：
 *   <ul>
 *   <li>UDT 的完全限定 SQL 类型名称
 *   <li>实现 <code>SQLData</code> 的类的 <code>Class</code> 对象
 *   </ul>
 * </ul>
 * <p>
 * 当为基本类型提供包含条目的类型映射时，<code>getArray</code> 和 <code>getResultSet</code> 方法将使用该映射来映射 <code>ARRAY</code> 值的元素。
 * 如果没有提供类型映射，这通常是情况，那么将使用连接的类型映射。如果连接的类型映射或提供给方法的类型映射没有基本类型的条目，则根据标准映射映射元素。
 * <p>
 * 如果 JDBC 驱动程序支持数据类型，则 <code>Array</code> 接口上的所有方法都必须完全实现。
 *
 * @since 1.2
 */

public interface Array {

  /**
   * 检索由该 <code>Array</code> 对象指定的数组中的元素的 SQL 类型名称。
   * 如果元素是内置类型，则返回元素的数据库特定类型名称。
   * 如果元素是用户定义类型 (UDT)，则此方法返回完全限定的 SQL 类型名称。
   *
   * @return 一个 <code>String</code>，表示内置基本类型的数据库特定名称；或表示基本类型为 UDT 的完全限定 SQL 类型名称
   * @exception SQLException 如果在尝试访问类型名称时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  String getBaseTypeName() throws SQLException;

  /**
   * 检索由该 <code>Array</code> 对象指定的数组中的元素的 JDBC 类型。
   *
   * @return 一个来自 {@link java.sql.Types} 类的常量，表示由该 <code>Array</code> 对象指定的数组中的元素的类型代码
   * @exception SQLException 如果在尝试访问基本类型时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  int getBaseType() throws SQLException;

  /**
   * 以 Java 编程语言中的数组形式检索由该 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的内容。
   * 此方法的版本使用与连接关联的类型映射进行类型映射的自定义。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于实现映射到基本数据类型的基类型时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @return 一个 Java 编程语言中的数组，包含由该 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的有序元素
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray() throws SQLException;

  /**
   * 检索由该 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的内容。
   * 此方法使用指定的 <code>map</code> 进行类型映射的自定义，除非数组的基本类型不匹配 <code>map</code> 中的用户定义类型，此时它
   * 使用标准映射。此版本的 <code>getArray</code> 方法使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于实现映射到基本数据类型的基类型时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @param map 一个包含 SQL 类型名称到 Java 编程语言类的映射的 <code>java.util.Map</code> 对象
   * @return 一个 Java 编程语言中的数组，包含由该对象指定的 SQL 数组的有序元素
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(java.util.Map<String,Class<?>> map) throws SQLException;

  /**
   * 检索由该 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的切片，从指定的 <code>index</code> 开始，包含 SQL 数组中的最多 <code>count</code>
   * 个连续元素。此方法使用与连接关联的类型映射进行类型映射的自定义。
   * <p>
   * <strong>注意：</strong> 当 <code>getArray</code> 用于实现映射到基本数据类型的基类型时，返回的数组是该基本数据类型的数组还是 <code>Object</code> 的数组是实现定义的。
   *
   * @param index 要检索的第一个元素的数组索引；第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @return 一个包含最多 <code>count</code> 个连续元素的数组，从元素 <code>index</code> 开始
   * @exception SQLException 如果在尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(long index, int count) throws SQLException;

              /**
   * 获取由这个 <code>Array</code> 对象指定的 SQL <code>ARRAY</code> 值的切片，
   * 从指定的 <code>index</code> 开始，包含 SQL 数组中的最多 <code>count</code>
   * 个连续元素。
   * <P>
   * 该方法使用指定的 <code>map</code> 进行类型映射自定义，
   * 除非数组的基本类型不匹配 <code>map</code> 中的用户定义类型，
   * 在这种情况下，它使用标准映射。此版本的 <code>getArray</code> 方法
   * 使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * <strong>注意：</strong>当 <code>getArray</code> 用于物化映射到原始数据类型的基类型时，
   * 实现定义返回的数组是该原始数据类型的数组还是 <code>Object</code> 的数组。
   *
   * @param index 要检索的第一个元素的数组索引；
   *              第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @param map 一个 <code>java.util.Map</code> 对象
   * 包含 SQL 类型名称及其在 Java 编程语言中的映射类
   * @return 一个数组，包含由这个 <code>Array</code> 对象指定的
   * SQL <code>ARRAY</code> 值中从 <code>index</code> 开始的最多 <code>count</code>
   * 个连续元素
   * @exception SQLException 如果尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  Object getArray(long index, int count, java.util.Map<String,Class<?>> map)
    throws SQLException;

  /**
   * 获取包含由这个 <code>Array</code> 对象指定的 SQL
   * <code>ARRAY</code> 值的元素的结果集。如果适用，
   * 数组的元素使用连接的类型映射进行映射；否则，使用标准映射。
   * <p>
   * 结果集包含数组每个元素的一行，
   * 每行有两列。第二列存储元素值；第一列存储该元素在数组中的索引
   * （第一个数组元素的索引为 1）。行按索引的升序排列。
   *
   * @return 一个包含由这个 <code>Array</code> 对象指定的数组中每个元素的一行的
   * {@link ResultSet} 对象，行按索引的升序排列。
   * @exception SQLException 如果尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet () throws SQLException;

  /**
   * 获取包含由这个 <code>Array</code> 对象指定的 SQL
   * <code>ARRAY</code> 值的元素的结果集。
   * 该方法使用指定的 <code>map</code> 进行类型映射自定义，
   * 除非数组的基本类型不匹配 <code>map</code> 中的用户定义类型，
   * 在这种情况下，它使用标准映射。此版本的 <code>getResultSet</code> 方法
   * 使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <p>
   * 结果集包含数组每个元素的一行，
   * 每行有两列。第二列存储元素值；第一列存储该元素在数组中的索引
   * （第一个数组元素的索引为 1）。行按索引的升序排列。
   *
   * @param map 包含 SQL 用户定义类型到
   * Java 编程语言中类的映射
   * @return 一个包含由这个 <code>Array</code> 对象指定的数组中每个元素的一行的
   * <code>ResultSet</code> 对象，行按索引的升序排列。
   * @exception SQLException 如果尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet (java.util.Map<String,Class<?>> map) throws SQLException;

  /**
   * 获取包含从索引 <code>index</code> 开始并包含最多
   * <code>count</code> 个连续元素的子数组的元素的结果集。该方法使用
   * 连接的类型映射来映射数组的元素，如果映射中包含基类型的条目。否则，
   * 使用标准映射。
   * <P>
   * 结果集包含由这个对象指定的 SQL 数组的每个元素的一行，
   * 第一行包含索引 <code>index</code> 处的元素。结果集包含
   * 基于索引的升序排列的最多 <code>count</code> 行。每行有两列：第二列存储
   * 元素值；第一列存储该元素在数组中的索引。
   *
   * @param index 要检索的第一个元素的数组索引；
   *              第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @return 一个包含由这个 <code>Array</code> 对象指定的 SQL 数组
   * 从索引 <code>index</code> 开始的最多 <code>count</code> 个连续元素的
   * <code>ResultSet</code> 对象。
   * @exception SQLException 如果尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet(long index, int count) throws SQLException;

  /**
   * 获取包含从索引 <code>index</code> 开始并包含最多
   * <code>count</code> 个连续元素的子数组的元素的结果集。
   * 该方法使用指定的 <code>map</code> 进行类型映射自定义，
   * 除非数组的基本类型不匹配 <code>map</code> 中的用户定义类型，
   * 在这种情况下，它使用标准映射。此版本的 <code>getResultSet</code> 方法
   * 使用给定的类型映射或标准映射；它从不使用与连接关联的类型映射。
   * <P>
   * 结果集包含由这个对象指定的 SQL 数组的每个元素的一行，
   * 第一行包含索引 <code>index</code> 处的元素。结果集包含
   * 基于索引的升序排列的最多 <code>count</code> 行。每行有两列：第二列存储
   * 元素值；第一列存储该元素在数组中的索引。
   *
   * @param index 要检索的第一个元素的数组索引；
   *              第一个元素的索引为 1
   * @param count 要检索的连续 SQL 数组元素的数量
   * @param map 包含 SQL 类型名称到 Java 编程语言中类的映射的
   * <code>Map</code> 对象
   * @return 一个包含由这个 <code>Array</code> 对象指定的 SQL 数组
   * 从索引 <code>index</code> 开始的最多 <code>count</code> 个连续元素的
   * <code>ResultSet</code> 对象。
   * @exception SQLException 如果尝试访问数组时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  ResultSet getResultSet (long index, int count,
                          java.util.Map<String,Class<?>> map)
    throws SQLException;
    /**
     * 释放 <code>Array</code> 对象并释放其持有的资源。调用 <code>free</code>
     * 方法后，对象无效。
     *<p>
     * 调用 <code>free</code> 后，任何尝试调用除 <code>free</code> 之外的方法
     * 都将导致抛出 <code>SQLException</code>。如果 <code>free</code> 被多次调用，
     * 后续的 <code>free</code> 调用被视为无操作。
     *<p>
     *
     * @throws SQLException 如果释放 Array 的资源时发生错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void free() throws SQLException;

}
