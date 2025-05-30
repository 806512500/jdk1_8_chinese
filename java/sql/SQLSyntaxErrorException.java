/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * {@link SQLException} 的子类，当 SQLState 类值为 '<i>42</i>' 或在供应商指定的条件下抛出。这表示正在进行的查询违反了 SQL 语法规则。
 * <p>
 * 请参阅您的驱动程序供应商文档，了解可能抛出此 <code>Exception</code> 的供应商指定条件。
 * @since 1.6
 */
public class SQLSyntaxErrorException extends SQLNonTransientException {

        /**
         * 构造一个 <code>SQLSyntaxErrorException</code> 对象。
         *  <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
         * <p>
         * @since 1.6
         */
        public SQLSyntaxErrorException() {
                super();
        }

        /**
         * 构造一个带有给定 <code>reason</code> 的 <code>SQLSyntaxErrorException</code> 对象。 <code>SQLState</code>
         * 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLSyntaxErrorException(String reason) {
                super(reason);
        }

        /**
         * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLSyntaxErrorException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @since 1.6
         */
        public SQLSyntaxErrorException(String reason, String SQLState) {
                super(reason, SQLState);
        }

        /**
         * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLSyntaxErrorException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
         * @param vendorCode 数据库供应商特定的异常代码
         * @since 1.6
         */
        public SQLSyntaxErrorException(String reason, String SQLState, int vendorCode) {
                super(reason, SQLState, vendorCode);
        }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLSyntaxErrorException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，否则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLSyntaxErrorException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLSyntaxErrorException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLSyntaxErrorException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLSyntaxErrorException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLSyntaxErrorException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLSyntaxErrorException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 一个 XOPEN 或 SQL:2003 代码，用于标识异常
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因（通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLSyntaxErrorException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }

    private static final long serialVersionUID = -1843832610477496053L;
}
