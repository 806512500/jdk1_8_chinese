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

/**
 * 证书解析异常。当解析无效的 DER 编码证书或在证书中发现不支持的 DER 特性时抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateParsingException extends CertificateException {

    private static final long serialVersionUID = -7989222416793322029L;

    /**
     * 构造一个没有详细消息的 CertificateParsingException。详细消息是一个描述此特定
     * 异常的字符串。
     */
    public CertificateParsingException() {
        super();
    }

    /**
     * 使用指定的详细消息构造 CertificateParsingException。详细消息是一个描述此特定
     * 异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateParsingException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建 CertificateParsingException。
     *
     * @param message 详细消息（通过 {@link #getMessage()} 方法稍后检索）。
     * @param cause 原因（通过 {@link #getCause()} 方法稍后检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息创建 CertificateParsingException，详细消息为
     * {@code (cause==null ? null : cause.toString())}
     * （通常包含 {@code cause} 的类和详细消息）。
     *
     * @param cause 原因（通过 {@link #getCause()} 方法稍后检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateParsingException(Throwable cause) {
        super(cause);
    }
}
