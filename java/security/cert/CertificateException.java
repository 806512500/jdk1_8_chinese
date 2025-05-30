/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * 此异常指示各种证书问题。
 *
 * @author Hemma Prafullchandra
 * @see Certificate
 */
public class CertificateException extends GeneralSecurityException {

    private static final long serialVersionUID = 3192535253797119798L;

    /**
     * 构造一个没有详细消息的证书异常。详细消息是一个描述此特定异常的字符串。
     */
    public CertificateException() {
        super();
    }

    /**
     * 使用给定的详细消息构造一个证书异常。详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public CertificateException(String msg) {
        super(msg);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertificateException}。
     *
     * @param message 详细消息（通过 {@link #getMessage()} 方法稍后检索）。
     * @param cause 原因（通过 {@link #getCause()} 方法稍后检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息 {@code (cause==null ? null : cause.toString())} 创建一个 {@code CertificateException}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（通过 {@link #getCause()} 方法稍后检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateException(Throwable cause) {
        super(cause);
    }
}
