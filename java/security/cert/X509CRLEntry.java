/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.security.cert;

import java.math.BigInteger;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

import sun.security.x509.X509CRLEntryImpl;

/**
 * <p>证书吊销列表 (CRL) 中已吊销证书的抽象类。
 *
 * <em>revokedCertificates</em> 的 ASN.1 定义为：
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
 *                   -- 包含与 extnId 对象标识符值注册的类型的 DER 编码值
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
     * 比较此 CRL 条目与给定对象的相等性。如果 {@code other} 对象是
     * {@code X509CRLEntry} 的实例，则获取其编码形式（内部 SEQUENCE）并
     * 与此 CRL 条目的编码形式进行比较。
     *
     * @param other 要与该 CRL 条目进行相等性测试的对象。
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
     * @return 该证书的编码形式
     * @exception CRLException 如果发生编码错误。
     */
    public abstract byte[] getEncoded() throws CRLException;

    /**
     * 从此 X509CRLEntry 获取 <em>userCertificate</em> 的序列号。
     *
     * @return 序列号。
     */
    public abstract BigInteger getSerialNumber();

    /**
     * 获取此条目描述的 X509Certificate 的发行者。如果证书发行者也是 CRL 发行者，
     * 则此方法返回 null。
     *
     * <p>此方法用于间接 CRL。默认实现总是返回 null。希望支持间接 CRL 的子类
     * 应该覆盖它。
     *
     * @return 此条目描述的 X509Certificate 的发行者，如果由 CRL 发行者签发，则返回 null。
     *
     * @since 1.5
     */
    public X500Principal getCertificateIssuer() {
        return null;
    }

    /**
     * 从此 X509CRLEntry 获取 <em>revocationDate</em> 的吊销日期。
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
     * @return 证书被吊销的原因，如果此 CRL 条目没有原因代码扩展，则返回
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
