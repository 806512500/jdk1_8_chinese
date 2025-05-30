
/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;

/**
 * {@link SQLException} 的子类，当一个或多个客户端信息属性无法在 <code>Connection</code> 上设置时抛出。除了 <code>SQLException</code> 提供的信息外，
 * <code>SQLClientInfoException</code> 还提供了一个客户端信息属性列表，这些属性未被设置。
 *
 * 一些数据库不允许多个客户端信息属性原子性地设置。对于这些数据库，即使 <code>Connection.setClientInfo</code> 方法抛出异常，
 * 也可能有一些客户端信息属性已被设置。应用程序可以使用 <code>getFailedProperties</code> 方法检索未设置的客户端信息属性列表。
 * 属性通过传递一个 <code>Map&lt;String,ClientInfoStatus&gt;</code> 给相应的 <code>SQLClientInfoException</code> 构造函数来标识。
 * <p>
 * @see ClientInfoStatus
 * @see Connection#setClientInfo
 * @since 1.6
 */
public class SQLClientInfoException extends SQLException {




        private Map<String, ClientInfoStatus>   failedProperties;

        /**
     * 构造一个 <code>SQLClientInfoException</code> 对象。
     * <code>reason</code>、<code>SQLState</code> 和 failedProperties 列表被初始化为 <code>null</code>，供应商代码被初始化为 0。
     * <code>cause</code> 未被初始化，可以随后通过调用 <code>Throwable#initCause(java.lang.Throwable)</code> 方法进行初始化。
     * <p>
     *
     * @since 1.6
     */
        public SQLClientInfoException() {

                this.failedProperties = null;
        }

        /**
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>failedProperties</code>。
     * <code>reason</code> 和 <code>SQLState</code> 被初始化为 <code>null</code>，供应商代码被初始化为 0。
     *
     * <code>cause</code> 未被初始化，可以随后通过调用 <code>Throwable#initCause(java.lang.Throwable)</code> 方法进行初始化。
     * <p>
     *
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties) {

                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>cause</code> 和 <code>failedProperties</code>。
     *
     * <code>reason</code> 被初始化为 <code>null</code>（如果 <code>cause==null</code>）或 <code>cause.toString()</code>（如果 <code>cause!=null</code>），供应商代码被初始化为 0。
     *
     * <p>
     *
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * @param cause                                     保存以供 <code>getCause()</code> 方法稍后检索的（可能为 null，表示原因不存在或未知）。
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
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code> 和 <code>failedProperties</code>。
     * <code>SQLState</code> 被初始化为 <code>null</code>，供应商代码被初始化为 0。
     *
     * <code>cause</code> 未被初始化，可以随后通过调用 <code>Throwable#initCause(java.lang.Throwable)</code> 方法进行初始化。
     * <p>
     *
     * @param reason                            异常的描述
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * <p>
     * @since 1.6
     */
        public SQLClientInfoException(String reason,
                Map<String, ClientInfoStatus> failedProperties) {

                super(reason);
                this.failedProperties = failedProperties;
        }

        /**
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code>、<code>cause</code> 和 <code>failedProperties</code>。
     * <code>SQLState</code> 被初始化为 <code>null</code>，供应商代码被初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * @param cause                                     保存以供 <code>getCause()</code> 方法稍后检索的（可能为 null，表示原因不存在或未知）。
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
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code>、<code>SQLState</code> 和 <code>failedProperties</code>。
     * <code>cause</code> 未被初始化，可以随后通过调用 <code>Throwable#initCause(java.lang.Throwable)</code> 方法进行初始化。供应商代码被初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
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
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code>、<code>SQLState</code>、<code>cause</code> 和 <code>failedProperties</code>。供应商代码被初始化为 0。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * @param cause                                     保存以供 <code>getCause()</code> 方法稍后检索的（可能为 null，表示原因不存在或未知）。
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
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>failedProperties</code>。
     * <code>cause</code> 未被初始化，可以随后通过调用 <code>Throwable#initCause(java.lang.Throwable)</code> 方法进行初始化。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode                        数据库供应商特定的异常代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
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
     * 构造一个 <code>SQLClientInfoException</code> 对象，并初始化给定的 <code>reason</code>、<code>SQLState</code>、<code>cause</code>、<code>vendorCode</code> 和 <code>failedProperties</code>。
     * <p>
     *
     * @param reason                            异常的描述
     * @param SQLState                          识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode                        数据库供应商特定的异常代码
     * @param failedProperties          包含无法设置的属性值的 Map。Map 中的键包含无法设置的客户端信息属性的名称，
     *                                  值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
     * @param cause                     保存以供 <code>getCause()</code> 方法稍后检索的（可能为 null，表示原因不存在或未知）。
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
     * 返回无法设置的客户端信息属性列表。Map 中的键包含无法设置的客户端信息属性的名称，值包含 <code>ClientInfoStatus</code> 中定义的一个原因代码。
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
