/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * CRL（证书吊销列表）异常。
 *
 * @author Hemma Prafullchandra
 */
public class CRLException extends GeneralSecurityException {

    private static final long serialVersionUID = -6694728944094197147L;

   /**
     * 构造一个没有详细消息的 CRLException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CRLException() {
        super();
    }

    /**
     * 构造一个具有指定详细消息的 CRLException。详细消息是一个描述此
     * 特定异常的字符串。
     *
     * @param message 详细消息。
     */
    public CRLException(String message) {
        super(message);
    }

    /**
     * 创建一个具有指定详细消息和原因的 {@code CRLException}。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CRLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个具有指定原因和详细消息的 {@code CRLException}，详细消息为
     * {@code (cause==null ? null : cause.toString())}（通常包含
     * {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CRLException(Throwable cause) {
        super(cause);
    }
}
