/*
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * {@code Error} 是 {@code Throwable} 的子类，表示一个合理的应用程序不应尝试捕获的严重问题。大多数此类错误都是异常条件。
 * {@code ThreadDeath} 错误虽然是“正常”条件，但也是 {@code Error} 的子类，因为大多数应用程序不应尝试捕获它。
 * <p>
 * 方法不需要在其 {@code throws} 子句中声明在方法执行期间可能抛出但未捕获的任何 {@code Error} 子类，因为这些错误是不应发生的异常条件。
 *
 * 也就是说，为了编译时检查异常的目的，{@code Error} 及其子类被视为未检查的异常。
 *
 * @author  Frank Yellin
 * @see     java.lang.ThreadDeath
 * @jls 11.2 Compile-Time Checking of Exceptions
 * @since   JDK1.0
 */
public class Error extends Throwable {
    static final long serialVersionUID = 4980196508277280342L;

    /**
     * 构造一个新的错误，其详细消息为 {@code null}。原因未初始化，但可以通过调用 {@link #initCause} 进行初始化。
     */
    public Error() {
        super();
    }

    /**
     * 构造一个新的错误，具有指定的详细消息。原因未初始化，但可以通过调用 {@link #initCause} 进行初始化。
     *
     * @param   message   详细消息。详细消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public Error(String message) {
        super(message);
    }

    /**
     * 构造一个新的错误，具有指定的详细消息和原因。请注意，与 {@code cause} 关联的详细消息不会自动包含在本错误的详细消息中。
     *
     * @param  message 详细消息（稍后由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since  1.4
     */
    public Error(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个新的错误，具有指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     * 当错误只是其他可抛出对象的简单包装时，此构造函数非常有用。
     *
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since  1.4
     */
    public Error(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个新的错误，具有指定的详细消息、原因、是否启用抑制以及是否启用可写堆栈跟踪。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null}，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     *
     * @since 1.7
     */
    protected Error(String message, Throwable cause,
                    boolean enableSuppression,
                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
