
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Reader;

/**
 * 在 Java&trade; 编程语言中
 * 对 SQL <code>CLOB</code> 类型的映射。
 * 一个 SQL <code>CLOB</code> 是一个内置类型，
 * 用于在数据库表的行中存储字符大对象。
 * 默认情况下，驱动程序使用 SQL <code>locator(CLOB)</code> 实现一个 <code>Clob</code> 对象，
 * 这意味着一个 <code>Clob</code> 对象包含指向 SQL <code>CLOB</code> 数据的逻辑指针，而不是数据本身。
 * 一个 <code>Clob</code> 对象在其创建的事务期间有效。
 * <P><code>Clob</code> 接口提供了获取 SQL <code>CLOB</code>（字符大对象）值的长度、
 * 在客户端实现 <code>CLOB</code> 值、以及在 <code>CLOB</code> 值中搜索子字符串或 <code>CLOB</code> 对象的方法。
 * 在 {@link ResultSet}、{@link CallableStatement} 和 {@link PreparedStatement} 接口中，
 * 如 <code>getClob</code> 和 <code>setClob</code> 等方法允许程序员访问 SQL <code>CLOB</code> 值。
 * 此外，此接口还提供了更新 <code>CLOB</code> 值的方法。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则必须完全实现 <code>Clob</code> 接口中的所有方法。
 *
 * @since 1.2
 */

public interface Clob {

  /**
   * 获取由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值的字符数。
   *
   * @return <code>CLOB</code> 的长度（以字符为单位）
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long length() throws SQLException;

  /**
   * 获取由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值的指定子字符串的副本。
   * 子字符串从位置 <code>pos</code> 开始，包含最多 <code>length</code> 个连续字符。
   *
   * @param pos 要提取的子字符串的第一个字符的位置。
   *            第一个字符的位置是 1。
   * @param length 要复制的连续字符数；
   *            length 的值必须为 0 或更大
   * @return 一个 <code>String</code>，表示由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值中的指定子字符串
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误；如果 pos 小于 1 或 length 小于 0
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  String getSubString(long pos, int length) throws SQLException;

  /**
   * 获取由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值作为 <code>java.io.Reader</code> 对象（或字符流）。
   *
   * @return 一个包含 <code>CLOB</code> 数据的 <code>java.io.Reader</code> 对象
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @see #setCharacterStream
   * @since 1.2
   */
  java.io.Reader getCharacterStream() throws SQLException;

  /**
   * 获取由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值作为 ASCII 流。
   *
   * @return 一个包含 <code>CLOB</code> 数据的 <code>java.io.InputStream</code> 对象
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @see #setAsciiStream
   * @since 1.2
   */
  java.io.InputStream getAsciiStream() throws SQLException;

  /**
   * 获取由这个 <code>Clob</code> 对象表示的 SQL <code>CLOB</code> 值中指定子字符串 <code>searchstr</code> 出现的字符位置。
   * 搜索从位置 <code>start</code> 开始。
   *
   * @param searchstr 要搜索的子字符串
   * @param start 开始搜索的位置；第一个位置是 1
   * @return 子字符串出现的位置或 -1（如果不存在）；第一个位置是 1
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 pos 小于 1
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long position(String searchstr, long start) throws SQLException;

  /**
   * 获取由这个 <code>Clob</code> 对象表示的 SQL <code>CLOB</code> 值中指定的 <code>Clob</code> 对象 <code>searchstr</code> 出现的字符位置。
   * 搜索从位置 <code>start</code> 开始。
   *
   * @param searchstr 要搜索的 <code>Clob</code> 对象
   * @param start 开始搜索的位置；第一个位置是 1
   * @return <code>Clob</code> 对象出现的位置或 -1（如果不存在）；第一个位置是 1
   * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 start 小于 1
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long position(Clob searchstr, long start) throws SQLException;

    //---------------------------- jdbc 3.0 -----------------------------------

    /**
     * 将给定的 Java <code>String</code> 写入由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值的位置 <code>pos</code>。
     * 该字符串将覆盖 <code>Clob</code> 对象中从位置 <code>pos</code> 开始的现有字符。
     * 如果在写入给定字符串时到达了 <code>Clob</code> 值的末尾，则 <code>Clob</code> 值的长度将增加以容纳额外的字符。
     * <p>
     * <b>注意：</b> 如果指定的 <code>pos</code> 值大于 <code>CLOB</code> 值的长度+1，则行为是未定义的。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能会支持此操作。
     *
     * @param pos 开始写入由这个 <code>Clob</code> 对象表示的 <code>CLOB</value> 值的位置；
     *            第一个位置是 1
     * @param str 要写入由这个 <code>Clob</code> 对象指定的 <code>CLOB</code> 值的字符串
     * @return 写入的字符数
     * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    int setString(long pos, String str) throws SQLException;

    /**
     * 将 <code>str</code> 的 <code>len</code> 个字符，从字符 <code>offset</code> 开始，写入由这个 <code>Clob</code> 对象表示的 <code>CLOB</code> 值。
     * 该字符串将覆盖 <code>Clob</code> 对象中从位置 <code>pos</code> 开始的现有字符。
     * 如果在写入给定字符串时到达了 <code>Clob</code> 值的末尾，则 <code>Clob</code> 值的长度将增加以容纳额外的字符。
     * <p>
     * <b>注意：</b> 如果指定的 <code>pos</code> 值大于 <code>CLOB</code> 值的长度+1，则行为是未定义的。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能会支持此操作。
     *
     * @param pos 开始写入由这个 <code>CLOB</code> 对象表示的 <code>CLOB</code> 值的位置；
     *            第一个位置是 1
     * @param str 要写入由这个 <code>Clob</code> 对象表示的 <code>CLOB</code> 值的字符串
     * @param offset 从 <code>str</code> 开始读取要写入的字符的偏移量
     * @param len 要写入的字符数
     * @return 写入的字符数
     * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    int setString(long pos, String str, int offset, int len) throws SQLException;

    /**
     * 获取一个流，用于将 ASCII 字符写入由这个 <code>Clob</code> 对象表示的 <code>CLOB</code> 值，从位置 <code>pos</code> 开始。
     * 写入流的字符将覆盖 <code>Clob</code> 对象中从位置 <code>pos</code> 开始的现有字符。
     * 如果在写入字符时到达了 <code>Clob</code> 值的末尾，则 <code>Clob</code> 值的长度将增加以容纳额外的字符。
     * <p>
     * <b>注意：</b> 如果指定的 <code>pos</code> 值大于 <code>CLOB</code> 值的长度+1，则行为是未定义的。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能会支持此操作。
     *
     * @param pos 开始写入由这个 <code>CLOB</code> 对象表示的 <code>CLOB</code> 值的位置；
     *            第一个位置是 1
     * @return 可以写入 ASCII 编码字符的流
     * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getAsciiStream
     * @since 1.4
     */
    java.io.OutputStream setAsciiStream(long pos) throws SQLException;

    /**
     * 获取一个流，用于将 Unicode 字符流写入由这个 <code>Clob</code> 对象表示的 <code>CLOB</code> 值，从位置 <code>pos</code> 开始。
     * 写入流的字符将覆盖 <code>Clob</code> 对象中从位置 <code>pos</code> 开始的现有字符。
     * 如果在写入字符时到达了 <code>Clob</code> 值的末尾，则 <code>Clob</code> 值的长度将增加以容纳额外的字符。
     * <p>
     * <b>注意：</b> 如果指定的 <code>pos</code> 值大于 <code>CLOB</code> 值的长度+1，则行为是未定义的。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能会支持此操作。
     *
     * @param pos 开始写入由这个 <code>CLOB</code> 对象表示的 <code>CLOB</code> 值的位置；
     *            第一个位置是 1
     * @return 可以写入 Unicode 编码字符的流
     * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getCharacterStream
     * @since 1.4
     */
    java.io.Writer setCharacterStream(long pos) throws SQLException;

    /**
     * 将由这个 <code>Clob</code> 指定的 <code>CLOB</code> 值截断为 <code>len</code> 个字符的长度。
     * <p>
     * <b>注意：</b> 如果指定的 <code>pos</code> 值大于 <code>CLOB</code> 值的长度+1，则行为是未定义的。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能会支持此操作。
     *
     * @param len <code>CLOB</code> 值应被截断的长度（以字符为单位）
     * @exception SQLException 如果访问 <code>CLOB</code> 值时发生错误或 len 小于 0
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void truncate(long len) throws SQLException;

    /**
     * 释放 <code>Clob</code> 对象并释放其持有的资源。
     * 一旦调用 <code>free</code> 方法，对象即无效。
     * <p>
     * 调用 <code>free</code> 之后，任何尝试调用除 <code>free</code> 之外的方法都将导致抛出 <code>SQLException</code>。
     * 如果多次调用 <code>free</code>，后续的调用将被视为无操作。
     * <p>
     * @throws SQLException 如果释放 <code>Clob</code> 的资源时发生错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void free() throws SQLException;

    /**
     * 返回一个包含部分 <code>Clob</code> 值的 <code>Reader</code> 对象，从位置 <code>pos</code> 开始，长度为 <code>length</code> 个字符。
     *
     * @param pos 指定要检索的部分值的第一个字符的偏移量。
     *            <code>Clob</code> 中的第一个字符的位置是 1。
     * @param length 指定要检索的部分值的长度（以字符为单位）。
     * @return 可以读取部分 <code>Clob</code> 值的 <code>Reader</code>。
     * @throws SQLException 如果 pos 小于 1 或 pos 大于 <code>Clob</code> 中的字符数或 pos + length 大于 <code>Clob</code> 中的字符数
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    Reader getCharacterStream(long pos, long length) throws SQLException;

}
