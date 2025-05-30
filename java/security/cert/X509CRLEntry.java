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

import java.math.BigInteger;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

import sun.security.x509.X509CRLEntryImpl;

/**
 * <p>抽象类，表示 CRL（证书吊销列表）中的已吊销证书。
 *
 * ASN.1 定义为 <em>revokedCertificates</em>：
 * <pre>
 * revokedCertificates    SEQUENCE OF SEQUENCE  {
 *     userCertificate    CertificateSerialNumber,
 *     revocationDate     ChoiceOfTime,
 *     crlEntryExtensions Extensions OPTIONAL
 *                        -- 如果存在，必须是 v2
 * }  OPTIONAL
 *
 * CertificateSerialNumber  ::=  INTEGER
 *
 * Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
 *
 * Extension  ::=  SEQUENCE  {
 *     extnId        OBJECT IDENTIFIER,
 *     critical      BOOLEAN DEFAULT FALSE,
 *     extnValue     OCTET STRING
 *                   -- 包含与 extnId 对象标识符值注册的类型的 DER 编码
 * }
 * </pre>
 *
 * @see X509CRL
 * @see X509Extension
 *
 * @author Hemma Prafullchandra
 */

public abstract class X509CRLEntry implements X509Extension {

    /**
     * 比较此 CRL 条目与给定对象是否相等。如果 {@code other} 对象是
     * {@code X509CRLEntry} 的实例，则检索其编码形式（内部 SEQUENCE）并
     * 与此 CRL 条目的编码形式进行比较。
     *
     * @param other 要与本 CRL 条目进行比较的对象。
     * @return 如果两个 CRL 条目的编码形式匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof X509CRLEntry))
            return false;
        try {
            byte[] thisCRLEntry = this.getEncoded();
            byte[] otherCRLEntry = ((X509CRLEntry)other).getEncoded();

            if (thisCRLEntry.length != otherCRLEntry.length)
                return false;
            for (int i = 0; i < thisCRLEntry.length; i++)
                 if (thisCRLEntry[i] != otherCRLEntry[i])
                     return false;
        } catch (CRLException ce) {
            return false;
        }
        return true;
    }

    /**
     * 从其编码形式返回此 CRL 条目的哈希码值。
     *
     * @return 哈希码值。
     */
    public int hashCode() {
        int     retval = 0;
        try {
            byte[] entryData = this.getEncoded();
            for (int i = 1; i < entryData.length; i++)
                 retval += entryData[i] * i;

        } catch (CRLException ce) {
            return(retval);
        }
        return(retval);
    }

    /**
     * 返回此 CRL 条目的 ASN.1 DER 编码形式，即内部 SEQUENCE。
     *
     * @return 证书的编码形式
     * @exception CRLException 如果发生编码错误。
     */
    public abstract byte[] getEncoded() throws CRLException;

    /**
     * 获取此 X509CRLEntry 的序列号，即 <em>userCertificate</em>。
     *
     * @return 序列号。
     */
    public abstract BigInteger getSerialNumber();

    /**
     * 获取此条目描述的 X509Certificate 的发行者。如果证书发行者也是 CRL 发行者，
     * 则此方法返回 null。
     *
     * <p>此方法用于间接 CRL。默认实现总是返回 null。希望支持间接 CRL 的子类应覆盖此方法。
     *
     * @return 此条目描述的 X509Certificate 的发行者，或如果由 CRL 发行者签发，则返回 null。
     *
     * @since 1.5
     */
    public X500Principal getCertificateIssuer() {
        return null;
    }

    /**
     * 获取此 X509CRLEntry 的吊销日期，即 <em>revocationDate</em>。
     *
     * @return 吊销日期。
     */
    public abstract Date getRevocationDate();

    /**
     * 如果此 CRL 条目有扩展，则返回 true。
     *
     * @return 如果此条目有扩展，则返回 true，否则返回 false。
     */
    public abstract boolean hasExtensions();

    /**
     * 返回此 CRL 条目的字符串表示形式。
     *
     * @return 此 CRL 条目的字符串表示形式。
     */
    public abstract String toString();

    /**
     * 返回证书被吊销的原因，如本 CRL 条目的原因代码扩展中所指定。
     *
     * @return 证书被吊销的原因，或如果此 CRL 条目没有原因代码扩展，则返回
     *    {@code null}
     * @since 1.7
     */
    public CRLReason getRevocationReason() {
        if (!hasExtensions()) {
            return null;
        }
        return X509CRLEntryImpl.getRevocationReason(this);
    }
}
