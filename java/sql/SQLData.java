/*
 * 版权所有 (c) 1998, 2006, Oracle 和/或其附属公司。保留所有权利。
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
 * 用于将 SQL 用户定义类型 (UDT) 映射到 Java 编程语言中的类的接口。实现 <code>SQLData</code> 接口的类对象将与 UDT 的 SQL 名称一起注册到适当的 <code>Connection</code> 对象的类型映射中。
 * <P>
 * 通常，<code>SQLData</code> 实现将为 SQL 结构化类型的每个属性定义一个字段，或者为 SQL <code>DISTINCT</code> 类型定义一个字段。当使用 <code>ResultSet.getObject</code> 方法从数据源检索 UDT 时，它将被映射为该类的一个实例。程序员可以像操作 Java 编程语言中的任何其他对象一样操作此类实例，并通过调用 <code>PreparedStatement.setObject</code> 方法将任何更改存储回 SQL 类型。
 * <p>
 * 预期自定义映射类的实现将由工具完成。在典型的实现中，程序员只需提供 SQL UDT 的名称、要映射到的类的名称以及 UDT 的每个属性要映射到的字段名称。工具将使用这些信息来实现 <code>SQLData.readSQL</code> 和 <code>SQLData.writeSQL</code> 方法。<code>readSQL</code> 方法调用适当的 <code>SQLInput</code> 方法从 <code>SQLInput</code> 对象读取每个属性，而 <code>writeSQL</code> 方法调用 <code>SQLOutput</code> 方法通过 <code>SQLOutput</code> 对象将每个属性写回数据源。
 * <P>
 * 应用程序程序员通常不会直接调用 <code>SQLData</code> 方法，<code>SQLInput</code> 和 <code>SQLOutput</code> 方法由 <code>SQLData</code> 方法内部调用，而不是由应用程序代码调用。
 *
 * @since 1.2
 */
public interface SQLData {

 /**
  * 返回此对象表示的 SQL 用户定义类型的完全限定名称。
  * 此方法由 JDBC 驱动程序调用，以获取映射到此 <code>SQLData</code> 实例的 UDT 实例的名称。
  *
  * @return 传递给方法 <code>readSQL</code> 的类型名称，当此对象被构造和填充时
  * @exception SQLException 如果发生数据库访问错误
  * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
  * @since 1.2
  */
  String getSQLTypeName() throws SQLException;

 /**
  * 从数据库读取数据并填充此对象。
  * 该方法的实现必须遵循以下协议：
  * <UL>
  * <LI>必须从给定的输入流中读取 SQL 类型的每个属性或元素。这是通过调用输入流的方法按顺序读取每个项目来完成的，顺序与 SQL 类型定义中的顺序相同。
  * <LI><code>readSQL</code> 方法然后将数据分配给适当的字段或元素（本对象或其他对象的）。
  * 具体来说，它必须调用适当的 <i>读取器</i> 方法（<code>SQLInput.readString</code>，<code>SQLInput.readBigDecimal</code> 等）来完成以下操作：
  * 对于 DISTINCT 类型，读取其单个数据元素；
  * 对于结构化类型，为 SQL 类型的每个属性读取一个值。
  * </UL>
  * JDBC 驱动程序在调用此方法之前使用类型映射初始化输入流，该映射由流上的适当 <code>SQLInput</code> 读取器方法使用。
  *
  * @param stream 用于读取自定义映射值数据的 <code>SQLInput</code> 对象
  * @param typeName 数据流上的值的 SQL 类型名称
  * @exception SQLException 如果发生数据库访问错误
  * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
  * @see SQLInput
  * @since 1.2
  */
  void readSQL (SQLInput stream, String typeName) throws SQLException;

  /**
  * 将此对象写入给定的 SQL 数据流，将其转换回数据源中的 SQL 值。
  * 该方法的实现必须遵循以下协议：<BR>
  * 必须将 SQL 类型的每个属性写入给定的输出流。这是通过调用输出流的方法按顺序写入每个项目来完成的，顺序与 SQL 类型定义中的顺序相同。
  * 具体来说，它必须调用适当的 <code>SQLOutput</code> 写入器方法（<code>writeInt</code>，<code>writeString</code> 等）来完成以下操作：对于 DISTINCT 类型，写入其单个数据元素；对于结构化类型，为 SQL 类型的每个属性写入一个值。
  *
  * @param stream 用于写入自定义映射值数据的 <code>SQLOutput</code> 对象
  * @exception SQLException 如果发生数据库访问错误
  * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
  * @see SQLOutput
  * @since 1.2
  */
  void writeSQL (SQLOutput stream) throws SQLException;
}
