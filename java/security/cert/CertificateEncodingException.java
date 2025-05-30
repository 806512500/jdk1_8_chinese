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
 * 证书编码异常。当尝试编码证书时发生错误时抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateEncodingException extends CertificateException {

    private static final long serialVersionUID = 6219492851589449162L;

    /**
     * 构造一个没有详细消息的 CertificateEncodingException。详细消息是描述
     * 此特定异常的字符串。
     */
    public CertificateEncodingException() {
        super();
    }

    /**
     * 使用指定的详细消息构造 CertificateEncodingException。详细消息是描述
     * 此特定异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateEncodingException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建 CertificateEncodingException。
     *
     * @param message 详细消息（稍后通过 {@link #getMessage()} 方法检索）。
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因和详细消息（如果 {@code cause} 为 {@code null} 则为 {@code null}，
     * 否则为 {@code cause.toString()}）创建 CertificateEncodingException。
     * 详细消息通常包含 {@code cause} 的类和详细消息。
     *
     * @param cause 原因（稍后通过 {@link #getCause()} 方法检索）。允许为 {@code null}，
     *        表示原因不存在或未知。
     * @since 1.5
     */
    public CertificateEncodingException(Throwable cause) {
        super(cause);
    }
}
