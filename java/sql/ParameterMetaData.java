/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 一个可以用于获取 <code>PreparedStatement</code> 对象中每个参数标记的类型和属性信息的对象。对于某些查询和驱动程序实现，<code>ParameterMetaData</code> 对象返回的数据可能在 <code>PreparedStatement</code> 执行之前不可用。
 *<p>
 * 一些驱动程序实现可能无法提供 <code>CallableStatement</code> 对象中每个参数标记的类型和属性信息。
 *
 * @since 1.4
 */

public interface ParameterMetaData extends Wrapper {

    /**
     * 获取此 <code>ParameterMetaData</code> 对象包含信息的 <code>PreparedStatement</code> 对象中的参数数量。
     *
     * @return 参数数量
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getParameterCount() throws SQLException;

    /**
     * 获取指定参数是否允许空值。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 给定参数的可空性状态；可能是
     *        <code>ParameterMetaData.parameterNoNulls</code>,
     *        <code>ParameterMetaData.parameterNullable</code>，或
     *        <code>ParameterMetaData.parameterNullableUnknown</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int isNullable(int param) throws SQLException;

    /**
     * 表示参数不允许 <code>NULL</code> 值的常量。
     */
    int parameterNoNulls = 0;

    /**
     * 表示参数允许 <code>NULL</code> 值的常量。
     */
    int parameterNullable = 1;

    /**
     * 表示参数的可空性未知的常量。
     */
    int parameterNullableUnknown = 2;

    /**
     * 获取指定参数是否可以是带符号的数字。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 如果可以则为 <code>true</code>；否则为 <code>false</code>
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    boolean isSigned(int param) throws SQLException;

    /**
     * 获取指定参数的指定列大小。
     *
     * <P>返回的值表示给定参数的最大列大小。对于数值数据，这是最大精度。对于字符数据，这是字符长度。对于日期时间数据类型，这是字符串表示形式的长度（假设分数秒部分的最大允许精度）。对于二进制数据，这是字节长度。对于 ROWID 数据类型，这是字节长度。对于不适用列大小的数据类型，返回 0。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 精度
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getPrecision(int param) throws SQLException;

    /**
     * 获取指定参数的小数点右侧的数字位数。对于不适用小数位数的数据类型，返回 0。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 小数位数
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getScale(int param) throws SQLException;

    /**
     * 获取指定参数的 SQL 类型。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 来自 <code>java.sql.Types</code> 的 SQL 类型
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     * @see Types
     */
    int getParameterType(int param) throws SQLException;

    /**
     * 获取指定参数的数据库特定类型名称。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 数据库使用的类型名称。如果参数类型是用户定义的类型，则返回完全限定的类型名称。
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    String getParameterTypeName(int param) throws SQLException;

    /**
     * 获取应传递给方法 <code>PreparedStatement.setObject</code> 的 Java 类的完全限定名称。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 用于设置指定参数值的方法 <code>PreparedStatement.setObject</code> 在 Java 编程语言中使用的类的完全限定名称。这是用于自定义映射的类名称。
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    String getParameterClassName(int param) throws SQLException;

    /**
     * 表示参数模式未知的常量。
     */
    int parameterModeUnknown = 0;

    /**
     * 表示参数模式为 IN 的常量。
     */
    int parameterModeIn = 1;

    /**
     * 表示参数模式为 INOUT 的常量。
     */
    int parameterModeInOut = 2;

    /**
     * 表示参数模式为 OUT 的常量。
     */
    int parameterModeOut = 4;

    /**
     * 获取指定参数的模式。
     *
     * @param param 第一个参数是 1，第二个是 2，...
     * @return 参数模式；可能是
     *        <code>ParameterMetaData.parameterModeIn</code>,
     *        <code>ParameterMetaData.parameterModeOut</code>，或
     *        <code>ParameterMetaData.parameterModeInOut</code>
     *        <code>ParameterMetaData.parameterModeUnknown</code>。
     * @exception SQLException 如果发生数据库访问错误
     * @since 1.4
     */
    int getParameterMode(int param) throws SQLException;
}
