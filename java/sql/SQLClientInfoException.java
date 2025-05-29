
/*
 * 版权所有 (c) 2006, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Map;

/**
 * {@link SQLException} 的子类，在一个或多个客户端信息属性无法设置在 <code>Connection</code> 上时抛出。除了 <code>SQLException</code> 提供的信息外，
 * <code>SQLClientInfoException</code> 还提供了一个未设置的客户端信息属性列表。
 *
 * 一些数据库不允许原子地设置多个客户端信息属性。对于这些数据库，即使 <code>Connection.setClientInfo</code>
 * 方法抛出异常，某些客户端信息属性也可能已被设置。应用程序可以使用 <code>getFailedProperties </code>
 * 方法检索未设置的客户端信息属性列表。属性通过传递一个
 * <code>Map&lt;String,ClientInfoStatus&gt;</code> 到
 * 适当的 <code>SQLClientInfoException</code> 构造函数来标识。
 * <p>
 * @see ClientInfoStatus
 * @see Connection#setClientInfo
 * @since 1.6
 */
public class SQLClientInfoException extends SQLException {




        private Map<String, ClientInfoStatus>   failedProperties;

        /**
     * 构造一个 <code>SQLClientInfoException</code> 对象。
     * <code>reason</code>，
     * <code>SQLState</code> 和 failedProperties 列表被初始化为
     * <code> null</code>，供应商代码被初始化为 0。
     * <code>cause</code> 未被初始化，可以通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     * <p>
     *
     * @since 1.6
     */
        public SQLClientInfoException() {

                this.failedProperties = null;
        }

        /**
     * 使用给定的 <code>failedProperties</code> 初始化一个 <code>SQLClientInfoException</code> 对象。
     * <code>reason</code> 和 <code>SQLState</code> 被初始化
     * 为 <code>null</code>，供应商代码被初始化为 0。
     *
     * <code>cause</code> 未被初始化，可以通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     * <p>
     *
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties) {

                this.failedProperties = failedProperties;
        }

        /**
     * 使用给定的 <code>cause</code> 和 <code>failedProperties</code> 初始化一个 <code>SQLClientInfoException</code> 对象。
     *
     * 如果 <code>cause==null</code>，<code>reason</code> 被初始化为 <code>null</code>；
     * 如果 <code>cause!=null</code>，<code>reason</code> 被初始化为 <code>cause.toString()</code>，供应商代码被初始化为 0。
     *
     * <p>
     *
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码
     * @param cause                                     （通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties,
                                                           Throwable cause) {

                super(cause != null?cause.toString():null);
                initCause(cause);
                this.failedProperties = failedProperties;
        }

        /**
     * 使用给定的 <code>reason</code> 和 <code>failedProperties</code> 初始化一个 <code>SQLClientInfoException</code> 对象。
     * <code>SQLState</code> 被初始化
     * 为 <code>null</code>，供应商代码被初始化为 0。
     *
     * <code>cause</code> 未被初始化，可以通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     * <p>
     *
     * @param reason                            异常的描述
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                Map<String, ClientInfoStatus> failedProperties) {

                super(reason);
                this.failedProperties = failedProperties;
        }

        /**
     * 使用给定的 <code>reason</code>，<code>cause</code> 和
     * <code>failedProperties</code> 初始化一个 <code>SQLClientInfoException</code> 对象。
     * <code>SQLState</code> 被初始化
     * 为 <code>null</code>，供应商代码被初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码
     * @param cause                                     该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                                                           Map<String, ClientInfoStatus> failedProperties,
                                                           Throwable cause) {

                            super(reason);
                initCause(cause);
                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个用给定的 <code>reason</code>，<code>SQLState</code> 和
     * <code>failedProperties</code> 初始化的 <code>SQLClientInfoException</code> 对象。
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。供应商代码初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，值包含
     *                                  <code>ClientInfoStatus</code> 中定义的原因代码之一
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                                                           String SQLState,
                                                           Map<String, ClientInfoStatus> failedProperties) {

                super(reason, SQLState);
                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个用给定的 <code>reason</code>，<code>SQLState</code>，<code>cause</code>
     * 和 <code>failedProperties</code> 初始化的 <code>SQLClientInfoException</code> 对象。供应商代码初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，值包含
     *                                  <code>ClientInfoStatus</code> 中定义的原因代码之一
     * @param cause                                     导致此 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可能是 null，表示原因不存在或未知。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                                                           String SQLState,
                                                           Map<String, ClientInfoStatus> failedProperties,
                                                           Throwable cause) {

                super(reason, SQLState);
                initCause(cause);
                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个用给定的 <code>reason</code>，<code>SQLState</code>，
     * <code>vendorCode</code> 和 <code>failedProperties</code> 初始化的 <code>SQLClientInfoException</code> 对象。
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode                        数据库供应商特定的异常代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，值包含
     *                                  <code>ClientInfoStatus</code> 中定义的原因代码之一
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                                                           String SQLState,
                                                           int vendorCode,
                                                           Map<String, ClientInfoStatus> failedProperties) {

                super(reason, SQLState, vendorCode);
                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个用给定的 <code>reason</code>，<code>SQLState</code>，
     * <code>cause</code>，<code>vendorCode</code> 和
     * <code>failedProperties</code> 初始化的 <code>SQLClientInfoException</code> 对象。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode                        数据库供应商特定的异常代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键
     *                                  包含无法设置的客户端信息
     *                                  属性的名称，值包含
     *                                  <code>ClientInfoStatus</code> 中定义的原因代码之一
     * @param cause                     导致此 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可能是 null，表示原因不存在或未知。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                                                           String SQLState,
                                                           int vendorCode,
                                                           Map<String, ClientInfoStatus> failedProperties,
                                                           Throwable cause) {

                            super(reason, SQLState, vendorCode);
                initCause(cause);
                this.failedProperties = failedProperties;
        }

    /**
     * 返回无法设置的客户端信息属性列表。Map 中的键包含无法设置的客户端信息属性的名称，值包含定义在 <code>ClientInfoStatus</code> 中的一个原因代码
     * <p>
     *
     * @return 包含无法设置的客户端信息属性的 Map 列表
     * <p>
     * @since 1.6
     */
        public Map<String, ClientInfoStatus> getFailedProperties() {

                return this.failedProperties;
        }

    private static final long serialVersionUID = -4319604256824655880L;
}
