/*
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 * Java 编程语言中 SQL ROWID 值的表示（映射）。SQL ROWID 是一种内置类型，其值可以被视为数据库表中标识行的地址。该地址是逻辑的还是物理的，由其来源的数据源决定。
 * <p>
 * <code>ResultSet</code>、<code>CallableStatement</code> 和 <code>PreparedStatement</code> 接口中的方法，如 <code>getRowId</code> 和 <code>setRowId</code>，
 * 允许程序员访问 SQL <code>ROWID</code> 值。<code>RowId</code> 接口提供了一种方法，用于将 <code>ROWID</code> 的值表示为字节数组或 <code>String</code>。
 * <p>
 * <code>DatabaseMetaData</code> 接口中的 <code>getRowIdLifetime</code> 方法可以用来确定 <code>RowId</code> 对象是否在其创建的事务期间、在其创建的会话期间，或在其标识的行未被删除的情况下有效。此外，
 * <code>getRowIdLifetime</code> 还指定了 <code>ROWID</code> 值在其来源数据源内的有效生命周期。这与大对象不同，因为大对象在其来源数据源内没有有效生命周期的限制。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则 <code>RowId</code> 接口上的所有方法都必须完全实现。
 *
 * @see java.sql.DatabaseMetaData
 * @since 1.6
 */

public interface RowId {
    /**
     * 将此 <code>RowId</code> 与指定的对象进行比较。结果为 <code>true</code> 当且仅当参数不为 null 且是一个表示与该对象相同的 ROWID 的 RowId 对象。
     * <p>
     * 在比较一个 <code>RowId</code> 与另一个 <code>RowId</code> 时，考虑其来源和有效生命周期是很重要的。如果两者都有效，并且都来自同一数据源的同一表，那么如果它们相等，它们就标识相同的行；
     * 如果一个或多个不再保证有效，或者它们来自不同的数据源，或同一数据源的不同表，它们可能相等但仍然不标识相同的行。
     *
     * @param obj 要与此 <code>RowId</code> 对象进行比较的 <code>Object</code>。
     * @return 如果 <code>RowId</code> 相等，则返回 true；否则返回 false
     * @since 1.6
     */
    boolean equals(Object obj);

    /**
     * 返回一个字节数组，表示由该 <code>java.sql.RowId</code> 对象指定的 SQL <code>ROWID</code> 的值。
     *
     * @return 一个字节数组，其长度由提供连接的驱动程序确定，表示由该 java.sql.RowId 对象指定的 ROWID 的值。
     */
     byte[] getBytes();

     /**
      * 返回一个字符串，表示由该 <code>java.sql.RowId</code> 对象指定的 SQL ROWID 的值。
      * <p>
      * 类似于 <code>java.sql.Date.toString()</code> 返回其 DATE 的内容为字符串 "2004-03-17" 而不是 SQL 中的 DATE 字面量（即字符串 DATE "2004-03-17"），toString()
      * 返回其 ROWID 的内容，形式由提供连接的驱动程序确定，可能不是 <code>ROWID</code> 字面量。
      *
      * @return 一个字符串，其格式由提供连接的驱动程序确定，表示由该 <code>java.sql.RowId</code> 对象指定的 <code>ROWID</code> 的值。
      */
     String toString();

     /**
      * 返回此 <code>RowId</code> 对象的哈希码值。
      *
      * @return <code>RowId</code> 的哈希码
      */
     int hashCode();

}
