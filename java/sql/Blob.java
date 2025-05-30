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

import java.io.InputStream;

/**
 * 在 Java 编程语言中表示 SQL <code>BLOB</code> 值的表示（映射）。SQL <code>BLOB</code> 是一种内置类型，
 * 用于将二进制大对象存储为数据库表中的一列值。默认情况下，驱动程序使用 SQL <code>locator(BLOB)</code>
 * 实现 <code>Blob</code>，这意味着 <code>Blob</code> 对象包含指向 SQL <code>BLOB</code> 数据的逻辑指针，
 * 而不是数据本身。一个 <code>Blob</code> 对象在其创建的事务期间有效。
 *
 * <P>接口 {@link ResultSet}、{@link CallableStatement} 和 {@link PreparedStatement} 中的方法，如
 * <code>getBlob</code> 和 <code>setBlob</code> 允许程序员访问 SQL <code>BLOB</code> 值。
 * <code>Blob</code> 接口提供了获取 SQL <code>BLOB</code>（二进制大对象）值的长度、
 * 在客户端实现 <code>BLOB</code> 值以及确定字节模式在 <code>BLOB</code> 值中的位置的方法。
 * 此外，此接口还提供了更新 <code>BLOB</code> 值的方法。
 * <p>
 * 如果 JDBC 驱动程序支持该数据类型，则 <code>Blob</code> 接口上的所有方法都必须完全实现。
 *
 * @since 1.2
 */

public interface Blob {

  /**
   * 返回由该 <code>Blob</code> 对象指定的 <code>BLOB</code> 值的字节数。
   * @return <code>BLOB</code> 的长度（以字节为单位）
   * @exception SQLException 如果访问 <code>BLOB</code> 的长度时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long length() throws SQLException;

  /**
   * 检索该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值的全部或部分，作为字节数组。
   * 此 <code>byte</code> 数组包含从位置 <code>pos</code> 开始的最多 <code>length</code> 个连续字节。
   *
   * @param pos 要提取的第一个字节在 <code>BLOB</code> 值中的序号位置；第一个字节的位置为 1
   * @param length 要复制的连续字节数；length 的值必须为 0 或更大
   * @return 一个包含从该 <code>Blob</code> 对象指定的 <code>BLOB</code> 值中，从位置 <code>pos</code>
   *         开始的最多 <code>length</code> 个连续字节的字节数组
   * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误；如果 pos 小于 1 或 length 小于 0
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @see #setBytes
   * @since 1.2
   */
  byte[] getBytes(long pos, int length) throws SQLException;

  /**
   * 以流的形式检索该 <code>Blob</code> 实例指定的 <code>BLOB</code> 值。
   *
   * @return 包含 <code>BLOB</code> 数据的流
   * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @see #setBinaryStream
   * @since 1.2
   */
  java.io.InputStream getBinaryStream () throws SQLException;

  /**
   * 检索该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值中指定字节数组 <code>pattern</code>
   * 开始的位置。从位置 <code>start</code> 开始搜索 <code>pattern</code>。
   *
   * @param pattern 要搜索的字节数组
   * @param start 开始搜索的位置；第一个位置为 1
   * @return 模式出现的位置，否则为 -1
   * @exception SQLException 如果访问 <code>BLOB</code> 时发生错误或 start 小于 1
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long position(byte pattern[], long start) throws SQLException;

  /**
   * 检索该 <code>Blob</code> 对象指定的 <code>BLOB</code> 值中 <code>pattern</code> 开始的位置。
   * 从位置 <code>start</code> 开始搜索。
   *
   * @param pattern 指定要搜索的 <code>BLOB</code> 值的 <code>Blob</code> 对象
   * @param start 在 <code>BLOB</code> 值中开始搜索的位置；第一个位置为 1
   * @return 模式开始的位置，否则为 -1
   * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误或 start 小于 1
   * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
   * @since 1.2
   */
  long position(Blob pattern, long start) throws SQLException;

    // -------------------------- JDBC 3.0 -----------------------------------

    /**
     * 将给定的字节数组写入该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值，从位置 <code>pos</code>
     * 开始，并返回写入的字节数。字节数组将覆盖 <code>Blob</code> 对象中从位置 <code>pos</code>
     * 开始的现有字节。如果在写入字节数组时达到 <code>Blob</code> 值的末尾，则 <code>Blob</code>
     * 值的长度将增加以容纳额外的字节。
     * <p>
     * <b>注意：</b> 如果为 <code>pos</code> 指定的值大于 <code>BLOB</code> 值的长度+1，则行为未定义。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能支持此操作。
     *
     * @param pos 在 <code>BLOB</code> 对象中开始写入的位置；第一个位置为 1
     * @param bytes 要写入该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值的字节数组
     * @return 写入的字节数
     * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getBytes
     * @since 1.4
     */
    int setBytes(long pos, byte[] bytes) throws SQLException;

    /**
     * 将给定的字节数组的全部或部分写入该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值，并返回写入的字节数。
     * 从 <code>BLOB</code> 值的 <code>pos</code> 位置开始写入；从给定的字节数组中写入 <code>len</code> 个字节。
     * 字节数组将覆盖 <code>Blob</code> 对象中从位置 <code>pos</code> 开始的现有字节。如果在写入字节数组时达到
     * <code>Blob</code> 值的末尾，则 <code>Blob</code> 值的长度将增加以容纳额外的字节。
     * <p>
     * <b>注意：</b> 如果为 <code>pos</code> 指定的值大于 <code>BLOB</code> 值的长度+1，则行为未定义。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能支持此操作。
     *
     * @param pos 在 <code>BLOB</code> 对象中开始写入的位置；第一个位置为 1
     * @param bytes 要写入此 <code>BLOB</code> 对象的字节数组
     * @param offset 从字节数组 <code>bytes</code> 中开始读取要设置的字节的偏移量
     * @param len 从字节数组 <code>bytes</code> 中写入 <code>BLOB</code> 值的字节数
     * @return 写入的字节数
     * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getBytes
     * @since 1.4
     */
    int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException;

    /**
     * 获取一个可以用于写入该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值的流。流从位置 <code>pos</code>
     * 开始。写入流的字节将覆盖 <code>Blob</code> 对象中从位置 <code>pos</code> 开始的现有字节。
     * 如果在写入流时达到 <code>Blob</code> 值的末尾，则 <code>Blob</code> 值的长度将增加以容纳额外的字节。
     * <p>
     * <b>注意：</b> 如果为 <code>pos</code> 指定的值大于 <code>BLOB</code> 值的长度+1，则行为未定义。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能支持此操作。
     *
     * @param pos 在 <code>BLOB</code> 值中开始写入的位置；第一个位置为 1
     * @return 可以写入数据的 <code>java.io.OutputStream</code> 对象
     * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误或 pos 小于 1
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @see #getBinaryStream
     * @since 1.4
     */
    java.io.OutputStream setBinaryStream(long pos) throws SQLException;

    /**
     * 将该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值截断为 <code>len</code> 字节的长度。
     * <p>
     * <b>注意：</b> 如果为 <code>pos</code> 指定的值大于 <code>BLOB</code> 值的长度+1，则行为未定义。
     * 一些 JDBC 驱动程序可能会抛出 <code>SQLException</code>，而其他驱动程序可能支持此操作。
     *
     * @param len 该 <code>Blob</code> 对象表示的 <code>BLOB</code> 值应截断的长度（以字节为单位）
     * @exception SQLException 如果访问 <code>BLOB</code> 值时发生错误或 len 小于 0
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.4
     */
    void truncate(long len) throws SQLException;

    /**
     * 释放该 <code>Blob</code> 对象并释放其持有的资源。调用 <code>free</code> 方法后，对象将无效。
     * <p>
     * 调用 <code>free</code> 后，任何尝试调用除 <code>free</code> 之外的方法都将导致抛出 <code>SQLException</code>。
     * 如果多次调用 <code>free</code>，后续的调用将被视为无操作。
     *<p>
     *
     * @throws SQLException 如果释放 <code>Blob</code> 的资源时发生错误
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    void free() throws SQLException;

    /**
     * 返回一个包含部分 <code>Blob</code> 值的 <code>InputStream</code> 对象，从 pos 指定的字节开始，
     * 长度为 length 字节。
     *
     * @param pos 要检索的部分值的第一个字节的偏移量。<code>Blob</code> 中的第一个字节的位置为 1
     * @param length 要检索的部分值的长度（以字节为单位）
     * @return 可以读取部分 <code>Blob</code> 值的 <code>InputStream</code>
     * @throws SQLException 如果 pos 小于 1 或 pos 大于 <code>Blob</code> 的字节数或 pos + length 大于
     * <code>Blob</code> 的字节数
     *
     * @exception SQLFeatureNotSupportedException 如果 JDBC 驱动程序不支持此方法
     * @since 1.6
     */
    InputStream getBinaryStream(long pos, long length) throws SQLException;
}
