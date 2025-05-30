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
 * 证书尚未有效异常。当当前 {@code Date} 或指定的 {@code Date}
 * 在证书有效期的 {@code notBefore} 日期/时间之前时抛出此异常。
 *
 * @author Hemma Prafullchandra
 */
public class CertificateNotYetValidException extends CertificateException {

    static final long serialVersionUID = 4355919900041064702L;

    /**
     * 构造一个没有详细消息的 CertificateNotYetValidException。详细消息是描述
     * 此特定异常的字符串。
     */
    public CertificateNotYetValidException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 CertificateNotYetValidException。详细消息是描述
     * 此特定异常的字符串。
     *
     * @param message 详细消息。
     */
    public CertificateNotYetValidException(String message) {
        super(message);
    }
}
