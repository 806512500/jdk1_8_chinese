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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * 当批处理更新操作中发生错误时抛出的 {@link SQLException} 的子类。除了 {@link SQLException} 提供的信息之外，
 * <code>BatchUpdateException</code> 还提供了在批处理更新期间成功执行的所有命令的更新计数，
 * 即在错误发生之前执行的所有命令。更新计数数组中的元素顺序对应于命令添加到批处理的顺序。
 * <P>
 * 当批处理更新中的命令执行失败并抛出 <code>BatchUpdateException</code> 时，驱动程序可能会或可能不会继续处理批处理中的剩余命令。
 * 如果驱动程序在失败后继续处理，<code>BatchUpdateException.getUpdateCounts</code> 方法返回的数组将包含批处理中每个命令的元素，
 * 而不仅仅是错误发生前成功执行的命令的元素。在驱动程序继续处理命令的情况下，任何失败命令的数组元素为 <code>Statement.EXECUTE_FAILED</code>。
 * <P>
 * JDBC 驱动程序实现应使用构造函数 {@code BatchUpdateException(String reason, String SQLState,
 * int vendorCode, long []updateCounts,Throwable cause) } 而不是接受 {@code int[]} 的构造函数，以避免更新计数溢出的可能性。
 * <p>
 * 如果调用了 {@code Statement.executeLargeBatch} 方法，建议调用 {@code getLargeUpdateCounts} 而不是 {@code getUpdateCounts}，
 * 以避免整数更新计数可能溢出。
 * @since 1.2
 */

public class BatchUpdateException extends SQLException {

  /**
   * 构造一个初始化为给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和
   * <code>updateCounts</code> 的 <code>BatchUpdateException</code> 对象。
   * <code>cause</code> 未初始化，可以通过调用
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
   * 构造一个初始化为给定 <code>reason</code>、<code>SQLState</code> 和
   * <code>updateCounts</code> 的 <code>BatchUpdateException</code> 对象。
   * <code>cause</code> 未初始化，可以通过调用
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
   * 构造一个初始化为给定 <code>reason</code> 和 <code>updateCounts</code> 的 <code>BatchUpdateException</code> 对象。
   * <code>cause</code> 未初始化，可以通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。<code>SQLState</code> 初始化为 <code>null</code>，
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
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的
   * <code>updateCounts</code>。
   * 通过调用
   * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化 <code>cause</code>。
   * <code>reason</code>
   * 和 <code>SQLState</code> 初始化为 null，供应商代码
   * 初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，
   * 因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param updateCounts 一个 <code>int</code> 数组，每个元素
   * 表示批处理中每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序；
   * 对于在命令失败后停止处理的 JDBC 驱动程序，批处理中每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * @since 1.2
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(int[] updateCounts) {
      this(null, null, 0, updateCounts);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象。
   * <code>reason</code>，<code>SQLState</code> 和 <code>updateCounts</code>
   * 初始化为 <code>null</code>，供应商代码初始化为 0。
   * <code>cause</code> 未初始化，可以随后通过调用
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
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>cause</code>。
   * <code>SQLState</code> 和 <code>updateCounts</code>
   * 初始化
   * 为 <code>null</code>，供应商代码初始化为 0。
   * <code>reason</code> 初始化为 <code>null</code> 如果
   * <code>cause==null</code> 或初始化为 <code>cause.toString()</code> 如果
   * <code>cause!=null</code>。
   * @param cause 导致此 <code>SQLException</code> 的根本原因
   * （稍后通过 <code>getCause()</code> 方法检索）；
   * 可能为 null，表示原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(Throwable cause) {
      this((cause == null ? null : cause.toString()), null, 0, (int[])null, cause);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>cause</code> 和 <code>updateCounts</code>。
   * <code>SQLState</code> 初始化
   * 为 <code>null</code>，供应商代码初始化为 0。
   * <code>reason</code> 初始化为 <code>null</code> 如果
   * <code>cause==null</code> 或初始化为 <code>cause.toString()</code> 如果
   * <code>cause!=null</code>。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，
   * 因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param updateCounts 一个 <code>int</code> 数组，每个元素
   * 表示批处理中每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序；
   * 对于在命令失败后停止处理的 JDBC 驱动程序，批处理中每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * @param cause 导致此 <code>SQLException</code> 的根本原因
   * （稍后通过 <code>getCause()</code> 方法检索）；可能为 null，表示
   * 原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(int []updateCounts , Throwable cause) {
      this((cause == null ? null : cause.toString()), null, 0, updateCounts, cause);
  }

  /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>reason</code>，<code>cause</code>
   * 和 <code>updateCounts</code>。 <code>SQLState</code> 初始化
   * 为 <code>null</code>，供应商代码初始化为 0。
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，
   * 因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param reason 异常的描述
   * @param updateCounts 一个 <code>int</code> 数组，每个元素
   * 表示批处理中每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序；
   * 对于在命令失败后停止处理的 JDBC 驱动程序，批处理中每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * @param cause 导致此 <code>SQLException</code> 的根本原因（稍后通过 <code>getCause()</code> 方法检索）；
   * 可能为 null，表示
   * 原因不存在或未知。
   * @since 1.6
   * @see #BatchUpdateException(java.lang.String, java.lang.String, int, long[],
   * java.lang.Throwable)
   */
  public BatchUpdateException(String reason, int []updateCounts, Throwable cause) {
      this(reason, null, 0, updateCounts, cause);
  }

              /**
   * 构造一个 <code>BatchUpdateException</code> 对象，初始化为给定的 <code>reason</code>，<code>SQLState</code>，<code>cause</code> 和
   * <code>updateCounts</code>。供应商代码初始化为 0。
   *
   * @param reason 异常的描述
   * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示
   * 每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序，该数组包含每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，该数组包含失败前每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；
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
   * @param updateCounts 一个 <code>int</code> 数组，每个元素表示
   * 每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序，该数组包含每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，该数组包含失败前每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * <p>
   * <strong>注意：</strong> 没有对 {@code updateCounts} 进行溢出验证，因此建议使用构造函数
   * {@code BatchUpdateException(String reason, String SQLState,
   * int vendorCode, long []updateCounts,Throwable cause) }。
   * </p>
   * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；
   * 可能为 null，表示原因不存在或未知。
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
   * 检索在发生此异常之前成功执行的批处理更新中每个更新语句的更新计数。
   * 实现批处理更新的驱动程序在其中一个命令执行失败时可能会继续处理剩余的命令，也可能不会。
   * 如果驱动程序继续处理命令，则此方法返回的数组将与批处理中的命令数量相同；否则，它将包含在抛出 <code>BatchUpdateException</code> 之前成功执行的每个命令的更新计数。
   *<P>
   * 为了适应在抛出 <code>BatchUpdateException</code> 对象后继续处理批处理更新中的命令的新选项，此方法的可能返回值在
   * Java 2 SDK, Standard Edition, version 1.3 中进行了修改。
   *
   * @return 一个 <code>int</code> 数组，包含在发生此错误之前成功执行的更新的更新计数。
   * 或者，如果驱动程序在错误后继续处理命令，则为批处理中的每个命令返回以下内容之一：
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
   * @param updateCounts 一个 <code>long</code> 数组，每个元素表示
   * 每个 SQL 命令的更新计数，<code>Statement.SUCCESS_NO_INFO</code> 或
   * <code>Statement.EXECUTE_FAILED</code>，对于在命令失败后继续处理的 JDBC 驱动程序，该数组包含每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>；对于在命令失败后停止处理的 JDBC 驱动程序，该数组包含失败前每个 SQL 命令的更新计数或
   * <code>Statement.SUCCESS_NO_INFO</code>
   * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；
   * 可能为 null，表示原因不存在或未知。
   * @since 1.8
   */
  public BatchUpdateException(String reason, String SQLState, int vendorCode,
          long []updateCounts,Throwable cause) {
      super(reason, SQLState, vendorCode, cause);
      this.longUpdateCounts  = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
      this.updateCounts = (longUpdateCounts == null) ? null : copyUpdateCount(longUpdateCounts);
  }

              /**
   * 获取在发生此异常之前成功执行的批处理更新语句的更新计数。
   * 实现批处理更新的驱动程序在命令执行失败时，可能会继续处理批处理中的剩余命令，也可能不会。
   * 如果驱动程序继续处理命令，此方法返回的数组将与批处理中的命令数量相同；否则，它将包含在抛出
   * <code>BatchUpdateException</code> 之前成功执行的每个命令的更新计数。
   * <p>
   * 当调用 {@code Statement.executeLargeBatch} 且返回的更新计数可能超过 {@link Integer#MAX_VALUE} 时，应使用此方法。
   * <p>
   * @return 包含在发生此错误之前成功执行的更新的更新计数的 <code>long</code> 数组。如果驱动程序在错误后继续处理命令，
   * 则对于批处理中的每个命令，返回以下内容之一：
   * <OL>
   * <LI>更新计数
   *  <LI><code>Statement.SUCCESS_NO_INFO</code> 表示命令执行成功但受影响的行数未知
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
   * 从 Java SE 8 开始，JDBC 增加了支持返回更新计数 > Integer.MAX_VALUE 的功能。因此，对 BatchUpdateException 进行了以下更改：
   * <ul>
   * <li>添加 longUpdateCounts 字段</li>
   * <li>添加接受 long[] 作为更新计数的构造函数</li>
   * <li>添加 getLargeUpdateCounts 方法</li>
   * </ul>
   * 调用任何构造函数时，int[] 和 long[] 更新计数字段将通过将一个数组复制到另一个数组来填充。
   *
   * 由于 JDBC 驱动程序传递更新计数，因此始终存在溢出的可能，而 BatchUpdateException 不需要为此负责，它只是复制数组。
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
     * readObject 被调用来从流中恢复 {@code BatchUpdateException} 的状态。
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {

       ObjectInputStream.GetField fields = s.readFields();
       int[] tmp = (int[])fields.get("updateCounts", null);
       long[] tmp2 = (long[])fields.get("longUpdateCounts", null);
       if(tmp != null && tmp2 != null && tmp.length != tmp2.length)
           throw new InvalidObjectException("更新计数不是预期的大小");
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
     * writeObject 被调用来将 {@code BatchUpdateException} 的状态保存到流中。
     */
    private void writeObject(ObjectOutputStream s)
            throws IOException, ClassNotFoundException {

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("updateCounts", updateCounts);
        fields.put("longUpdateCounts", longUpdateCounts);
        s.writeFields();
    }
}
