/*
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * {@link SQLException} 的子类，在以下情况下抛出：先前失败的操作可能在应用程序执行某些恢复步骤并重试整个事务或在分布式事务中重试事务分支时能够成功。至少，
 * 恢复操作必须包括关闭当前连接并获取新连接。
 *<p>
 *
 * @since 1.6
 */
public class SQLRecoverableException extends java.sql.SQLException {

        /**
         * 构造一个 <code>SQLRecoverableException</code> 对象。
         * <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
         * <p>
         * @since 1.6
        */
        public SQLRecoverableException() {
                super();
        }

        /**
         * 使用给定的 <code>reason</code> 构造一个 <code>SQLRecoverableException</code> 对象。
         * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
         * <p>
         * @param reason 异常的描述
         * @since 1.6
         */
        public SQLRecoverableException(String reason) {
                super(reason);
        }

        /**
         * 使用给定的 <code>reason</code> 和 <code>SQLState</code> 构造一个 <code>SQLRecoverableException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。供应商代码初始化为 0。
         * <p>
         * @param reason 异常的描述
         * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
         * @since 1.6
         */
        public SQLRecoverableException(String reason, String SQLState) {
                super(reason, SQLState);
        }

        /**
         * 使用给定的 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 构造一个 <code>SQLRecoverableException</code> 对象。
         *
         * <code>cause</code> 未初始化，可以随后通过调用
         * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
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
     * 使用给定的 <code>cause</code> 构造一个 <code>SQLRecoverableException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，否则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 导致此 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用给定的 <code>reason</code> 和 <code>cause</code> 构造一个 <code>SQLRecoverableException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 导致此 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * 使用给定的 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 构造一个 <code>SQLRecoverableException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param cause 导致此 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    /**
     * 使用给定的 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 构造一个 <code>SQLRecoverableException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 导致此 <code>SQLException</code> 的根本原因（稍后可通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLRecoverableException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }

   private static final long serialVersionUID = -4144386502923131579L;
}
