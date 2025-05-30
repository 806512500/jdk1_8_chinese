/*
 * Copyright (c) 2007, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * CRLReason 枚举指定了证书被撤销的原因，如 <a href="http://tools.ietf.org/html/rfc5280">
 * RFC 5280: Internet X.509 Public Key Infrastructure Certificate and CRL
 * Profile</a> 中定义的。
 *
 * @author Sean Mullan
 * @since 1.7
 * @see X509CRLEntry#getRevocationReason
 * @see CertificateRevokedException#getRevocationReason
 */
public enum CRLReason {
    /**
     * 此原因表示证书被撤销的原因未指定。
     */
    UNSPECIFIED,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于终端实体证书。
     */
    KEY_COMPROMISE,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于证书颁发机构 (CA) 证书。
     */
    CA_COMPROMISE,

    /**
     * 此原因表示证书主体的名称或其他信息已更改。
     */
    AFFILIATION_CHANGED,

    /**
     * 此原因表示证书已被替代。
     */
    SUPERSEDED,

    /**
     * 此原因表示证书不再需要。
     */
    CESSATION_OF_OPERATION,

    /**
     * 此原因表示证书已被暂停。
     */
    CERTIFICATE_HOLD,

    /**
     * 未使用的原因。
     */
    UNUSED,

    /**
     * 此原因表示证书之前已被暂停，应从 CRL 中移除。仅用于增量 CRL。
     */
    REMOVE_FROM_CRL,

    /**
     * 此原因表示证书主体的权限已被撤销。
     */
    PRIVILEGE_WITHDRAWN,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于属性权威 (AA) 证书。
     */
    AA_COMPROMISE
}
