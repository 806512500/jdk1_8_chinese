/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在非法或不适当的时间调用了方法。换句话说，Java 环境或
 * Java 应用程序不在请求操作的适当状态。
 *
 * @author  Jonni Kanerva
 * @since   JDK1.1
 */
public
class IllegalStateException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 IllegalStateException。
     * 详细消息是一个描述此特定异常的字符串。
     */
    public IllegalStateException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 IllegalStateException。
     * 详细消息是一个描述此特定异常的字符串。
     *
     * @param s 包含详细消息的字符串
     */
    public IllegalStateException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的异常。
     *
     * <p>请注意，与 <code>cause</code> 关联的详细消息
     * <i>不会</i> 自动包含在此异常的详细消息中。
     *
     * @param  message 由 {@link Throwable#getMessage()} 方法稍后检索的详细消息。
     * @param  cause 由 {@link Throwable#getCause()} 方法稍后检索的原因。  (允许 <tt>null</tt> 值，表示原因不存在或未知。)
     * @since 1.5
     */
    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt> 构造一个新的异常
     * (通常包含 <tt>cause</tt> 的类和详细消息)。
     * 当异常主要是其他可抛出对象的包装器时，此构造函数非常有用 (例如，{@link
     * java.security.PrivilegedActionException})。
     *
     * @param  cause 由 {@link Throwable#getCause()} 方法稍后检索的原因。  (允许 <tt>null</tt> 值，表示原因不存在或未知。)
     * @since  1.5
     */
    public IllegalStateException(Throwable cause) {
        super(cause);
    }

    static final long serialVersionUID = -1848914673093119416L;
}
