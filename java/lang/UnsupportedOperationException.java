/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 抛出以表示请求的操作不受支持。<p>
 *
 * 该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @author  Josh Bloch
 * @since   1.2
 */
public class UnsupportedOperationException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 UnsupportedOperationException。
     */
    public UnsupportedOperationException() {
    }

    /**
     * 构造一个具有指定详细消息的 UnsupportedOperationException。
     *
     * @param message 详细消息
     */
    public UnsupportedOperationException(String message) {
        super(message);
    }

    /**
     * 构造一个具有指定详细消息和原因的新异常。
     *
     * <p>请注意，与 <code>cause</code> 关联的详细消息
     * <i>不会</i> 自动包含在该异常的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link Throwable#getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。  （允许 <tt>null</tt> 值，表示原因不存在或未知。）
     * @since 1.5
     */
    public UnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个具有指定原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt> 的新异常
     * （通常包含 <tt>cause</tt> 的类和详细消息）。
     * 当异常只是其他可抛出对象的简单包装器时，此构造函数非常有用（例如，{@link
     * java.security.PrivilegedActionException}）。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。  （允许 <tt>null</tt> 值，表示原因不存在或未知。）
     * @since  1.5
     */
    public UnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    static final long serialVersionUID = -1242599979055084673L;
}
