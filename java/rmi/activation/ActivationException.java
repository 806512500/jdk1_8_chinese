/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

/**
 * 由激活接口使用的通用异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链接机制。构造时可以提供的“详细异常”并可通过公共
 * {@link #detail} 字段访问，现在被称为 <i>原因</i>，可以通过 {@link Throwable#getCause()} 方法访问，
 * 以及上述“遗留字段”。
 *
 * <p>在 <code>ActivationException</code> 的实例上调用 {@link Throwable#initCause(Throwable)} 方法
 * 始终会抛出 {@link IllegalStateException}。
 *
 * @author      Ann Wollrath
 * @since       1.2
 */
public class ActivationException extends Exception {

    /**
     * 激活异常的原因。
     *
     * <p>此字段早于通用异常链接设施。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Throwable detail;

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -4320118837291406071L;

    /**
     * 构造一个 <code>ActivationException</code>。
     */
    public ActivationException() {
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 使用指定的详细消息构造一个 <code>ActivationException</code>。
     *
     * @param s 详细消息
     */
    public ActivationException(String s) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 使用指定的详细消息和原因构造一个 <code>ActivationException</code>。此构造函数将 {@link #detail}
     * 字段设置为指定的 <code>Throwable</code>。
     *
     * @param s 详细消息
     * @param cause 原因
     */
    public ActivationException(String s, Throwable cause) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
        detail = cause;
    }

    /**
     * 返回详细消息，包括此异常的任何原因的消息。
     *
     * @return 详细消息
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
