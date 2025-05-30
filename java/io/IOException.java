/*
 * Copyright (c) 1994, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 表示某种 I/O 异常已发生。此类是由于失败或中断的 I/O 操作产生的异常的通用类。
 *
 * @author  未署名
 * @see     java.io.InputStream
 * @see     java.io.OutputStream
 * @since   JDK1.0
 */
public
class IOException extends Exception {
    static final long serialVersionUID = 7818375828146090155L;

    /**
     * 构造一个带有 {@code null} 作为其错误详细信息消息的 {@code IOException}。
     */
    public IOException() {
        super();
    }

    /**
     * 构造一个带有指定详细信息消息的 {@code IOException}。
     *
     * @param message
     *        详细信息消息（稍后通过 {@link #getMessage()} 方法检索）
     */
    public IOException(String message) {
        super(message);
    }

    /**
     * 构造一个带有指定详细信息消息和原因的 {@code IOException}。
     *
     * <p> 注意，与 {@code cause} 关联的详细信息消息
     * <i>不会</i> 自动纳入此异常的详细信息消息。
     *
     * @param message
     *        详细信息消息（稍后通过 {@link #getMessage()} 方法检索）
     *
     * @param cause
     *        原因（稍后通过 {@link #getCause()} 方法检索）。  （允许为 null，
     *        表示原因不存在或未知。）
     *
     * @since 1.6
     */
    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个带有指定原因和详细信息消息的 {@code IOException}，详细信息消息为
     * {@code (cause==null ? null : cause.toString())}（通常包含 {@code cause} 的类和详细信息消息）。
     * 此构造函数对于基本上只是其他可抛出对象的包装器的 IO 异常非常有用。
     *
     * @param cause
     *        原因（稍后通过 {@link #getCause()} 方法检索）。  （允许为 null，
     *        表示原因不存在或未知。）
     *
     * @since 1.6
     */
    public IOException(Throwable cause) {
        super(cause);
    }
}
