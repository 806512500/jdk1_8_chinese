/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其关联公司。保留所有权利。
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
 * 当 SQLState 类值为 '<i>40</i>' 或在供应商指定的条件下抛出的 {@link SQLException} 的子类。这表明由于死锁或其他事务序列化失败，数据库自动回滚了当前语句。
 * <p>
 * 请参阅您的驱动程序供应商文档，了解可能抛出此 <code>Exception</code> 的供应商指定条件。
 * @since 1.6
 */
public class SQLTransactionRollbackException extends SQLTransientException {
        /**
         * 构造一个 <code>SQLTransactionRollbackException</code> 对象。
         * <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @since 1.6
         */
        public SQLTransactionRollbackException() {
                super();
        }

        /**
         * 构造一个带有给定 <code>reason</code> 的 <code>SQLTransactionRollbackException</code> 对象。 <code>SQLState</code>
         * 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLTransactionRollbackException(String reason) {
                super(reason);
        }

        /**
         * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLTransactionRollbackException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @since 1.6
         */
        public SQLTransactionRollbackException(String reason, String SQLState) {
                super(reason, SQLState);
        }

        /**
         * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLTransactionRollbackException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @param vendorCode 一个数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLTransactionRollbackException(String reason, String SQLState, int vendorCode) {
                super(reason, SQLState, vendorCode);
        }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLTransactionRollbackException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，如果 <code>cause!=null</code> 则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransactionRollbackException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLTransactionRollbackException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransactionRollbackException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLTransactionRollbackException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransactionRollbackException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLTransactionRollbackException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param vendorCode 一个数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransactionRollbackException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }

    private static final long serialVersionUID = 5246680841170837229L;
}
