/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * {@code PKIXReason} 枚举了根据 PKIX (RFC 5280) 标准，X.509 证书路径可能无效的特定于 PKIX 的原因。这些原因是对 {@code CertPathValidatorException.BasicReason} 枚举的补充。
 *
 * @since 1.7
 */
public enum PKIXReason implements CertPathValidatorException.Reason {
    /**
     * 证书无法正确链接。
     */
    NAME_CHAINING,

    /**
     * 证书的密钥用法无效。
     */
    INVALID_KEY_USAGE,

    /**
     * 策略约束被违反。
     */
    INVALID_POLICY,

    /**
     * 未找到可接受的信任锚。
     */
    NO_TRUST_ANCHOR,

    /**
     * 证书包含一个或多个未识别的关键扩展。
     */
    UNRECOGNIZED_CRIT_EXT,

    /**
     * 证书不是 CA 证书。
     */
    NOT_CA_CERT,

    /**
     * 路径长度约束被违反。
     */
    PATH_TOO_LONG,

    /**
     * 名称约束被违反。
     */
    INVALID_NAME
}
