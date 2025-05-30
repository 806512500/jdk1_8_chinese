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
 * 抛出以指示 Java 虚拟机中发生了一些意外的内部错误。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class InternalError extends VirtualMachineError {
    private static final long serialVersionUID = -9062593416125562365L;

    /**
     * 构造一个没有详细消息的 <code>InternalError</code>。
     */
    public InternalError() {
        super();
    }

    /**
     * 构造一个具有指定详细消息的 <code>InternalError</code>。
     *
     * @param   message   详细消息。
     */
    public InternalError(String message) {
        super(message);
    }


    /**
     * 构造一个具有指定详细消息和原因的 {@code InternalError}。请注意，与 {@code cause} 关联的详细消息
     * <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since  1.8
     */
    public InternalError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个具有指定原因和详细消息的 {@code InternalError}，详细消息为 {@code (cause==null ? null :
     * cause.toString())}（通常包含 {@code cause} 的类和详细消息）。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，表示原因不存在或未知。
     * @since  1.8
     */
    public InternalError(Throwable cause) {
        super(cause);
    }

}
