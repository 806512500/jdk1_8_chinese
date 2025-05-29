
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
 * {@link SQLException} 的子类，在先前失败的操作可能在没有应用程序级功能干预的情况下重试时能够成功的情况下抛出。
 *<p>
 *
 * @since 1.6
 */
public class SQLTransientException extends java.sql.SQLException {

        /**
         * 构造一个 <code>SQLTransientException</code> 对象。
         * <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法后续初始化。
         * <p>
         * @since 1.6
        */
        public SQLTransientException() {
                super();
        }

        /**
         * 构造一个带有给定 <code>reason</code> 的 <code>SQLTransientException</code> 对象。 <code>SQLState</code>
         * 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法后续初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLTransientException(String reason) {
                super(reason);
        }

        /**
         * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLTransientException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法后续初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @since 1.6
         */
        public SQLTransientException(String reason, String SQLState) {
                super(reason,SQLState);
        }

        /**
         * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLTransientException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法后续初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @param vendorCode 数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLTransientException(String reason, String SQLState, int vendorCode) {
                super(reason,SQLState,vendorCode);
        }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLTransientException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，如果 <code>cause!=null</code> 则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLTransientException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientException(String reason, Throwable cause) {
        super(reason,cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLTransientException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientException(String reason, String SQLState, Throwable cause) {
        super(reason,SQLState,cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLTransientException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLTransientException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason,SQLState,vendorCode,cause);
    }

    private static final long serialVersionUID = -9042733978262274539L;
}
