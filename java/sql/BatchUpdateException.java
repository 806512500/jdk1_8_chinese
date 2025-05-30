
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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * {@link SQLException} 的子类，在批处理更新操作中发生错误时抛出。除了 {@link SQLException} 提供的信息外，
 * <code>BatchUpdateException</code> 还提供了批处理更新中成功执行的所有命令的更新计数。这些命令是在错误发生之前执行的。
 * 更新计数数组中的元素顺序与命令添加到批处理中的顺序相对应。
 * <P>
 * 在批处理更新中某个命令执行失败并抛出 <code>BatchUpdateException</code> 时，驱动程序可能会继续处理批处理中的剩余命令。
 * 如果驱动程序在失败后继续处理，方法 <code>BatchUpdateException.getUpdateCounts</code> 返回的数组将包含批处理中每个命令的元素，
 * 而不仅仅是成功执行的命令的元素。对于任何失败的命令，数组元素为 <code>Statement.EXECUTE_FAILED</code>。
 * <P>
 * JDBC 驱动程序实现应使用构造函数 {@code BatchUpdateException(String reason, String SQLState,
 * int vendorCode, long []updateCounts,Throwable cause) } 而不是接受 {@code int[]} 的构造函数，以避免更新计数溢出的可能性。
 * <p>
 * 如果调用了 {@code Statement.executeLargeBatch} 方法，建议调用 {@code getLargeUpdateCounts} 而不是 {@code getUpdateCounts}，
 * 以避免整数更新计数的可能溢出。
 * @since 1.2
 */

public class BatchUpdateException extends SQLException {

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>reason</code>、<code>SQLState</code>、
   * <code>vendorCode</code> 和 <code>updateCounts</code>。未初始化 <code>cause</code>，可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param reason 错误的描述
   * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
   * @param vendorCode 特定数据库供应商使用的异常代码
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException( String reason, String SQLState, int vendorCode,
                               int[] updateCounts ) {
      super(reason, SQLState, vendorCode);
      this.updateCounts  = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
      this.longUpdateCounts = (updateCounts == null) ? null : copyUpdateCount(updateCounts);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>reason</code>、<code>SQLState</code> 和
   * <code>updateCounts</code>。未初始化 <code>cause</code>，可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。供应商代码初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param reason 异常的描述
   * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(String reason, String SQLState,
                              int[] updateCounts) {
      this(reason, SQLState, 0, updateCounts);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>reason</code> 和 <code>updateCounts</code>。
   * 未初始化 <code>cause</code>，可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。 <code>SQLState</code> 初始化为 <code>null</code>，
   * 供应商代码初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param reason 异常的描述
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public  BatchUpdateException(String reason, int[] updateCounts) {
      this(reason, null, 0, updateCounts);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>updateCounts</code>。
   * 未初始化 <code>cause</code>，可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。 <code>reason</code> 和 <code>SQLState</code>
   * 初始化为 null，供应商代码初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(int[] updateCounts) {
      this(null, null, 0, updateCounts);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象。 <code>reason</code>、<code>SQLState</code> 和
   * <code>updateCounts</code> 初始化为 <code>null</code>，供应商代码初始化为 0。未初始化 <code>cause</code>，
   * 可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
   * <p>
   *
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException() {
        this(null, null, 0, null);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>cause</code>。 <code>SQLState</code> 和
   * <code>updateCounts</code> 初始化为 <code>null</code>，供应商代码初始化为 0。 <code>reason</code> 初始化为
   * <code>null</code> 如果 <code>cause==null</code> 或初始化为 <code>cause.toString()</code> 如果
   * <code>cause!=null</code>。
   * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；
   * 可能为 null，表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(Throwable cause) {
      this((cause == null ? null : cause.toString()), null, 0, (int[])null, cause);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>cause</code> 和 <code>updateCounts</code>。
   * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。 <code>reason</code> 初始化为
   * <code>null</code> 如果 <code>cause==null</code> 或初始化为 <code>cause.toString()</code> 如果
   * <code>cause!=null</code>。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可能为 null，
   * 表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(int []updateCounts , Throwable cause) {
      this((cause == null ? null : cause.toString()), null, 0, updateCounts, cause);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>reason</code>、<code>cause</code>
   * 和 <code>updateCounts</code>。<code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param reason 异常的描述
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；
   * 可能为 null，表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(String reason, int []updateCounts, Throwable cause) {
      this(reason, null, 0, updateCounts, cause);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化给定的 <code>reason</code>、<code>SQLState</code>、
   * <code>cause</code> 和 <code>updateCounts</code>。供应商代码初始化为 0。
   *
   * @param reason 异常的描述
   * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中每个 SQL 命令的更新计数、
   * <code>Statement.SUCCESS_NO_INFO</code> 或 <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，
   * 包含每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，
   * 包含失败前每个 SQL 命令的更新计数或 <code>Statement.SUCCESS_NO_INFO</code>。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；
   * 可能为 null，表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(String reason, String SQLState,
          int []updateCounts, Throwable cause) {
      this(reason, SQLState, 0, updateCounts, cause);
  }


              /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>reason</code>，<code>SQLState</code>，<code>vendorCode</code>
   * <code>cause</code> 和 <code>updateCounts</code>。
   *
   * @param reason 错误的描述
   * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
   * @param vendorCode 特定数据库供应商使用的异常代码
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示批处理中的每个 SQL 命令的更新计数、<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，此数组包含批处理中的每个命令的信息；对于在命令失败后停止处理的 JDBC 驱动程序，此数组包含失败前成功执行的每个命令的信息
   * <p>
   * <strong>注意：</strong> 不会对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可能为 null，表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(String reason, String SQLState, int vendorCode,
                                int []updateCounts,Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
        this.updateCounts  = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
        this.longUpdateCounts = (updateCounts == null) ? null : copyUpdateCount(updateCounts);
  }

  /**
   * 检索在发生此异常之前成功执行的批处理更新中的每个更新语句的更新计数。
   * 实现批处理更新的驱动程序可能在命令执行失败后继续处理剩余的命令，也可能停止处理。如果驱动程序继续处理命令，
   * 该方法返回的数组将包含与批处理中的命令数量相同数量的元素；否则，它将包含在抛出 <code>BatchUpdateException</code> 之前成功执行的每个命令的更新计数。
   *<P>
   * 该方法的可能返回值在 Java 2 SDK, Standard Edition, version 1.3 中进行了修改。这是为了适应在抛出 <code>BatchUpdateException</code> 对象后继续处理批处理中命令的新选项。
   *
   * @return 一个 <code>int</code> 数组，包含在发生此错误之前成功执行的更新的更新计数。或者，如果驱动程序在错误后继续处理命令，对于批处理中的每个命令，返回以下内容之一：
   * <OL>
   * <LI>更新计数
   *  <LI><code>Statement.SUCCESS_NO_INFO</code> 表示命令成功执行，但受影响的行数未知
   *  <LI><code>Statement.EXECUTE_FAILED</code> 表示命令未能成功执行
   * </OL>
   * @since 1.3
   * @see #getLargeUpdateCounts()
   */
  public int[] getUpdateCounts() {
      return (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>reason</code>，<code>SQLState</code>，<code>vendorCode</code>
   * <code>cause</code> 和 <code>updateCounts</code>。
   * <p>
   * 当返回的更新计数可能超过 {@link Integer#MAX_VALUE} 时，应使用此构造函数。
   * <p>
   * @param reason 错误的描述
   * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
   * @param vendorCode 特定数据库供应商使用的异常代码
   * @param updateCounts 一个 <code>long</code> 数组，每个元素表示批处理中的每个 SQL 命令的更新计数、<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>。对于在命令失败后继续处理的 JDBC 驱动程序，此数组包含批处理中的每个命令的信息；对于在命令失败后停止处理的 JDBC 驱动程序，此数组包含失败前成功执行的每个命令的信息
   * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可能为 null，表示原因不存在或未知。
   * @since 1.8
   */
  public BatchUpdateException(String reason, String SQLState, int vendorCode,
          long []updateCounts,Throwable cause) {
      super(reason, SQLState, vendorCode, cause);
      this.longUpdateCounts  = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
      this.updateCounts = (longUpdateCounts == null) ? null : copyUpdateCount(longUpdateCounts);
  }

  /**
   * 检索在发生此异常之前成功执行的批处理更新中的每个更新语句的更新计数。
   * 实现批处理更新的驱动程序可能在命令执行失败后继续处理剩余的命令，也可能停止处理。如果驱动程序继续处理命令，
   * 该方法返回的数组将包含与批处理中的命令数量相同数量的元素；否则，它将包含在抛出 <code>BatchUpdateException</code> 之前成功执行的每个命令的更新计数。
   * <p>
   * 当调用 {@code Statement.executeLargeBatch} 且返回的更新计数可能超过 {@link Integer#MAX_VALUE} 时，应使用此方法。
   * <p>
   * @return 一个 <code>long</code> 数组，包含在发生此错误之前成功执行的更新的更新计数。或者，如果驱动程序在错误后继续处理命令，对于批处理中的每个命令，返回以下内容之一：
   * <OL>
   * <LI>更新计数
   *  <LI><code>Statement.SUCCESS_NO_INFO</code> 表示命令成功执行，但受影响的行数未知
   *  <LI><code>Statement.EXECUTE_FAILED</code> 表示命令未能成功执行
   * </OL>
   * @since 1.8
   */
  public long[] getLargeUpdateCounts() {
      return (longUpdateCounts == null) ? null :
              Arrays.copyOf(longUpdateCounts, longUpdateCounts.length);
  }

  /**
   * 描述批处理执行结果的数组。
   * @serial
   * @since 1.2
   */
  private  int[] updateCounts;

  /*
   * 从 Java SE 8 开始，JDBC 增加了支持返回更新计数 > Integer.MAX_VALUE。因此，对 BatchUpdateException 进行了以下更改：
   * <ul>
   * <li>添加字段 longUpdateCounts</li>
   * <li>添加接受 long[] 作为更新计数的构造函数</li>
   * <li>添加 getLargeUpdateCounts 方法</li>
   * </ul>
   * 调用任何构造函数时，int[] 和 long[] updateCount 字段将通过将一个数组复制到另一个数组来填充。
   *
   * 由于 JDBC 驱动程序传递更新计数，因此始终存在溢出的可能性，而 BatchUpdateException 不需要对此进行处理，它只是复制数组。
   *
   * JDBC 驱动程序应始终使用指定 long[] 的构造函数，而 JDBC 应用程序开发人员应调用 getLargeUpdateCounts。
   */

  /**
   * 描述批处理执行结果的数组。
   * @serial
   * @since 1.8
   */
  private  long[] longUpdateCounts;

  private static final long serialVersionUID = 5977529877145521757L;

  /*
   * 将 int[] updateCount 复制到 long[] updateCount 的实用方法
   */
  private static long[] copyUpdateCount(int[] uc) {
      long[] copy = new long[uc.length];
      for(int i= 0; i< uc.length; i++) {
          copy[i] = uc[i];
      }
      return copy;
  }

  /*
   * 将 long[] updateCount 复制到 int[] updateCount 的实用方法。
   * 不会进行溢出检查，因为预计用户将调用 getLargeUpdateCounts。
   */
  private static int[] copyUpdateCount(long[] uc) {
      int[] copy = new int[uc.length];
      for(int i= 0; i< uc.length; i++) {
          copy[i] = (int) uc[i];
      }
      return copy;
  }
    /**
     * readObject 被调用以从流中恢复 {@code BatchUpdateException} 的状态。
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {

       ObjectInputStream.GetField fields = s.readFields();
       int[] tmp = (int[])fields.get("updateCounts", null);
       long[] tmp2 = (long[])fields.get("longUpdateCounts", null);
       if(tmp != null && tmp2 != null && tmp.length != tmp2.length)
           throw new InvalidObjectException("update counts are not the expected size");
       if (tmp != null)
           updateCounts = tmp.clone();
       if (tmp2 != null)
           longUpdateCounts = tmp2.clone();
       if(updateCounts == null && longUpdateCounts != null)
           updateCounts = copyUpdateCount(longUpdateCounts);
       if(longUpdateCounts == null && updateCounts != null)
           longUpdateCounts = copyUpdateCount(updateCounts);

    }

    /**
     * writeObject 被调用以将 {@code BatchUpdateException} 的状态保存到流中。
     */
    private void writeObject(ObjectOutputStream s)
            throws IOException, ClassNotFoundException {

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("updateCounts", updateCounts);
        fields.put("longUpdateCounts", longUpdateCounts);
        s.writeFields();
    }
}
