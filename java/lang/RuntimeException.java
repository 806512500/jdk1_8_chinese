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
 * {@code RuntimeException} 是那些在 Java 虚拟机正常操作期间可以抛出的异常的超类。
 *
 * <p>{@code RuntimeException} 及其子类是 <em>未检查异常</em>。未检查异常 <em>不需要</em>
 * 在方法或构造器的 {@code throws} 子句中声明，即使它们可以由方法或构造器的执行抛出并传播到方法或构造器边界之外。
 *
 * @author  Frank Yellin
 * @jls 11.2 编译时异常检查
 * @since   JDK1.0
 */
public class RuntimeException extends Exception {
    static final long serialVersionUID = -7034897190745766939L;

    /** 构造一个新的运行时异常，其详细消息为 {@code null}。原因未初始化，但可以通过调用 {@link #initCause} 来初始化。 */
    public RuntimeException() {
        super();
    }

    /** 构造一个新的运行时异常，具有指定的详细消息。原因未初始化，但可以通过调用 {@link #initCause} 来初始化。
     *
     * @param   message   详细消息。详细消息将保存以供 {@link #getMessage()} 方法稍后检索。
     */
    public RuntimeException(String message) {
        super(message);
    }

    /**
     * 构造一个新的运行时异常，具有指定的详细消息和原因。请注意，与 {@code cause} 关联的详细消息 <i>不会</i>
     * 自动包含在此运行时异常的详细消息中。
     *
     * @param  message 详细消息（稍后由 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt>，表示原因不存在或未知。
     * @since  1.4
     */
    public RuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 构造一个新的运行时异常，具有指定的原因和详细消息 <tt>(cause==null ? null : cause.toString())</tt>
     * （通常包含 <tt>cause</tt> 的类和详细消息）。此构造器适用于运行时异常，这些异常基本上是其他可抛出对象的包装器。
     *
     * @param  cause 原因（稍后由 {@link #getCause()} 方法检索）。允许为 <tt>null</tt>，表示原因不存在或未知。
     * @since  1.4
     */
    public RuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个新的运行时异常，具有指定的详细消息、原因、是否启用抑制和是否启用可写堆栈跟踪。
     *
     * @param  message 详细消息。
     * @param cause 原因。允许为 {@code null}，表示原因不存在或未知。
     * @param enableSuppression 是否启用或禁用抑制
     * @param writableStackTrace 是否启用或禁用堆栈跟踪的可写性
     *
     * @since 1.7
     */
    protected RuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
