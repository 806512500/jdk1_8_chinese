/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * {@link SQLException} 的子类，用于 SQLState 类值为 '<i>08</i>' 或在供应商指定条件下。这表明失败的连接操作可能在不进行应用程序级别更改的情况下重试后能够成功。
 * <p>
 * 请参阅您的驱动程序供应商文档，了解可能抛出此 <code>Exception</code> 的供应商指定条件。
 * @since 1.6
 */
public class SQLTransientConnectionException extends java.sql.SQLTransientException {

        /**
         * 构造一个 <code>SQLTransientConnectionException</code> 对象。
         * <code>reason</code>，<code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @since 1.6
         */
        public SQLTransientConnectionException() {
                super();
        }

        /**
         * 使用给定的 <code>reason</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。将 <code>SQLState</code>
         * 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLTransientConnectionException(String reason) {
                super(reason);
        }

        /**
         * 使用给定的 <code>reason</code> 和 <code>SQLState</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @since 1.6
         */
        public SQLTransientConnectionException(String reason, String SQLState) {
                super(reason,SQLState);
        }

        /**
         * 使用给定的 <code>reason</code>，<code>SQLState</code> 和 <code>vendorCode</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @param vendorCode 数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLTransientConnectionException(String reason, String SQLState, int vendorCode) {
                super(reason,SQLState,vendorCode);
        }

   /**
     * 使用给定的 <code>cause</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，如果 <code>cause!=null</code> 则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
    */
    public SQLTransientConnectionException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用给定的 <code>reason</code> 和 <code>cause</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
    * @since 1.6
     */
    public SQLTransientConnectionException(String reason, Throwable cause) {
        super(reason,cause);
    }

    /**
     * 使用给定的 <code>reason</code>，<code>SQLState</code> 和 <code>cause</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientConnectionException(String reason, String SQLState, Throwable cause) {
        super(reason,SQLState,cause);
    }

    /**
     * 使用给定的 <code>reason</code>，<code>SQLState</code>，<code>vendorCode</code> 和 <code>cause</code> 构造一个 <code>SQLTransientConnectionException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientConnectionException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason,SQLState,vendorCode,cause);
    }

    private static final long serialVersionUID = -2520155553543391200L;
}
