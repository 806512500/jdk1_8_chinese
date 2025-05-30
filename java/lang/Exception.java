/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 类 {@code Exception} 及其子类是 {@code Throwable} 的一种形式，表示合理的应用程序可能希望捕获的条件。
 *
 * <p>类 {@code Exception} 及其不是 {@link RuntimeException} 子类的任何子类是 <em>检查异常</em>。检查异常需要在方法或构造函数的 {@code throws} 子句中声明，如果它们可以由方法或构造函数的执行抛出并且传播到方法或构造函数边界之外。
 *
 * @author  Frank Yellin
 * @see     java.lang.Error
 * @jls 11.2 Compile-Time Checking of Exceptions
 * @since   JDK1.0
 */
public class Exception extends Throwable {
    static final long serialVersionUID = -3387516993124229948L;

    /**
     * 构造一个带有 {@code null} 作为其详细消息的新异常。原因未初始化，可以通过调用 {@link #initCause} 随后初始化。
     */
    public Exception() {
        super();
    }

    /**
     * 构造一个带有指定详细消息的新异常。原因未初始化，可以通过调用 {@link #initCause} 随后初始化。
     *
     * @param   message   详细消息。详细消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public Exception(String message) {
        super(message);
    }

    /**
     * 构造一个带有指定详细消息和原因的新异常。请注意，与 {@code cause} 关联的详细消息 <i>不会</i> 自动包含在该异常的详细消息中。
     *
     * @param  message 详细消息（稍后由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt>，表示原因不存在或未知。
     * @since  1.4
     */
    public Exception(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个带有指定原因和详细消息为 <tt>(cause==null ? null : cause.toString())</tt> 的新异常（通常包含 {@code cause} 的类和详细消息）。
     * 此构造函数对于异常只是其他可抛出对象的简单包装器的情况非常有用（例如，{@link java.security.PrivilegedActionException}）。
     *
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt>，表示原因不存在或未知。
     * @since  1.4
     */
    public Exception(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带有指定详细消息、原因、抑制启用或禁用以及可写堆栈跟踪启用或禁用的新异常。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null}，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     * @since 1.7
     */
    protected Exception(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
