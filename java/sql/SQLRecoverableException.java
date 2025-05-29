
/*
 * 版权所有 (c) 2006, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 在以下情况下抛出的 {@link SQLException} 子类：
 * 之前失败的操作可能在应用程序执行某些恢复步骤后能够成功，例如重试整个事务或在分布式事务中重试事务分支。
 * 最小的恢复操作必须包括关闭当前连接并获取新连接。
 *<p>
 *
 * @since 1.6
 */
public class SQLRecoverableException extends java.sql.SQLException {

        /**
         * 构造一个 <code>SQLRecoverableException</code> 对象。
         * <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
         * <p>
         * @since 1.6
        */
        public SQLRecoverableException() {
                super();
        }

        /**
         * 构造一个带有给定 <code>reason</code> 的 <code>SQLRecoverableException</code> 对象。
         * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLRecoverableException(String reason) {
                super(reason);
        }

        /**
         * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLRecoverableException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @since 1.6
         */
        public SQLRecoverableException(String reason, String SQLState) {
                super(reason, SQLState);
        }

        /**
         * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLRecoverableException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @param vendorCode 数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLRecoverableException(String reason, String SQLState, int vendorCode) {
                super(reason, SQLState, vendorCode);
        }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLRecoverableException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，如果 <code>cause!=null</code> 则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLRecoverableException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLRecoverableException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLRecoverableException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }

   private static final long serialVersionUID = -4144386502923131579L;
}
