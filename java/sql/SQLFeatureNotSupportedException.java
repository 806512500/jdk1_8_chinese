/*
 * 版权所有 (c) 2005, 2013，Oracle 和/或其附属公司。保留所有权利。
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
 * 当 SQLState 类值为 '<i>0A</i>'（值为 '零' A）时抛出的 {@link SQLException} 子类。
 * 这表明 JDBC 驱动程序不支持某个可选的 JDBC 功能。
 * 可选的 JDBC 功能可以分为以下几类：
 *
 *<UL>
 *<LI>不支持某个可选功能
 *<LI>不支持某个可选的重载方法
 *<LI>不支持某个方法的可选模式。方法的模式是根据传递给方法的参数值确定的
 *</UL>
 *
 * @since 1.6
 */
public class SQLFeatureNotSupportedException extends SQLNonTransientException {

        /**
         * 构造一个 <code>SQLFeatureNotSupportedException</code> 对象。
         * <code>reason</code>，<code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @since 1.6
         */
        public SQLFeatureNotSupportedException() {
                super();
        }

        /**
         * 构造一个带有给定 <code>reason</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
         * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLFeatureNotSupportedException(String reason) {
                super(reason);
        }

        /**
         * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @since 1.6
         */
        public SQLFeatureNotSupportedException(String reason, String SQLState) {
                super(reason,SQLState);
        }

        /**
         * 构造一个带有给定 <code>reason</code>，<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法随后初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @param vendorCode 数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLFeatureNotSupportedException(String reason, String SQLState, int vendorCode) {
                super(reason,SQLState,vendorCode);
        }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，如果 <code>cause!=null</code> 则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLFeatureNotSupportedException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLFeatureNotSupportedException(String reason, Throwable cause) {
        super(reason,cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>，<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLFeatureNotSupportedException(String reason, String SQLState, Throwable cause) {
        super(reason,SQLState,cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>，<code>SQLState</code>，<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLFeatureNotSupportedException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLFeatureNotSupportedException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason,SQLState,vendorCode,cause);
    }

    private static final long serialVersionUID = -1026510870282316051L;
}
