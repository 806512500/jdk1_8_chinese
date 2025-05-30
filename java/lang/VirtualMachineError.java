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
 * 抛出以指示 Java 虚拟机已损坏或已耗尽继续运行所需的资源。
 *
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
abstract public class VirtualMachineError extends Error {
    private static final long serialVersionUID = 4161983926571568670L;

    /**
     * 构造一个没有详细消息的 <code>VirtualMachineError</code>。
     */
    public VirtualMachineError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>VirtualMachineError</code>。
     *
     * @param   message   详细消息。
     */
    public VirtualMachineError(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code VirtualMachineError}。请注意，与 {@code cause} 关联的详细消息
     * <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since  1.8
     */
    public VirtualMachineError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和
     * 详细消息）构造一个 {@code VirtualMachineError}。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许 {@code null} 值，表示原因不存在或未知。
     * @since  1.8
     */
    public VirtualMachineError(Throwable cause) {
        super(cause);
    }
}
