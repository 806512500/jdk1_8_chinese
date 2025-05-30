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

package java.rmi.server;

/**
 * 当在克隆 <code>UnicastRemoteObject</code> 时发生远程异常，将抛出 <code>ServerCloneException</code>。
 *
 * <p>从 1.4 版本开始，此异常已更新以符合通用的异常链机制。构造时可以提供的“嵌套异常”现在被称为 <i>原因</i>，可以通过 {@link Throwable#getCause()} 方法以及上述“遗留字段”访问。
 *
 * <p>在 <code>ServerCloneException</code> 的实例上调用 {@link Throwable#initCause(Throwable)} 方法总是会抛出 {@link IllegalStateException}。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see     java.rmi.server.UnicastRemoteObject#clone()
 */
public class ServerCloneException extends CloneNotSupportedException {

    /**
     * 异常的原因。
     *
     * <p>此字段早于通用的异常链机制。现在推荐使用 {@link Throwable#getCause()} 方法获取此信息。
     *
     * @serial
     */
    public Exception detail;

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 6617456357664815945L;

    /**
     * 使用指定的详细信息消息构造 <code>ServerCloneException</code>。
     *
     * @param s 详细信息消息。
     */
    public ServerCloneException(String s) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 使用指定的详细信息消息和原因构造 <code>ServerCloneException</code>。
     *
     * @param s 详细信息消息。
     * @param cause 原因
     */
    public ServerCloneException(String s, Exception cause) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
        detail = cause;
    }

    /**
     * 返回详细信息消息，包括此异常的任何原因的消息。
     *
     * @return 详细信息消息
     */
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else
            return super.getMessage() +
                "; 嵌套异常是: \n\t" +
                detail.toString();
    }

    /**
     * 返回此异常的原因。此方法返回 {@link #detail} 字段的值。
     *
     * @return 原因，可能是 <tt>null</tt>。
     * @since   1.4
     */
    public Throwable getCause() {
        return detail;
    }
}
