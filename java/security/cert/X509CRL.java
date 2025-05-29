
/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import java.security.*;
import java.security.spec.*;

import javax.security.auth.x500.X500Principal;

import java.math.BigInteger;
import java.util.Date;
import java.util.Set;
import java.util.Arrays;

import sun.security.x509.X509CRLImpl;
import sun.security.util.SignatureUtil;

/**
 * <p>
 * 抽象类，用于表示 X.509 证书撤销列表 (CRL)。
 * 证书撤销列表 (CRL) 是一个带有时间戳的列表，用于标识已撤销的证书。
 * 它由证书颁发机构 (CA) 签名，并在公共存储库中公开提供。
 *
 * <p>每个已撤销的证书在 CRL 中通过其证书序列号来标识。当证书使用系统使用证书（例如，验证远程用户的数字签名）时，
 * 该系统不仅会检查证书签名和有效期，还会获取一个足够新的 CRL，并检查证书序列号是否不在该 CRL 上。
 * “足够新”的含义可能因本地策略而异，但通常指的是最近发布的 CRL。CA 会定期（例如，每小时、每天或每周）发布新的 CRL。
 * 当证书被撤销时，条目会被添加到 CRL 中，当证书到期日期到达时，条目可能会被移除。
 * <p>
 * X.509 v2 CRL 格式如下所示，使用 ASN.1 描述：
 * <pre>
 * CertificateList  ::=  SEQUENCE  {
 *     tbsCertList          TBSCertList,
 *     signatureAlgorithm   AlgorithmIdentifier,
 *     signature            BIT STRING  }
 * </pre>
 * <p>
 * 更多信息可以在
 * <a href="http://www.ietf.org/rfc/rfc3280.txt">RFC 3280: Internet X.509
 * 公钥基础设施证书和 CRL 配置文件</a> 中找到。
 * <p>
 * {@code tbsCertList} 的 ASN.1 定义如下：
 * <pre>
 * TBSCertList  ::=  SEQUENCE  {
 *     version                 Version OPTIONAL,
 *                             -- 如果存在，必须是 v2
 *     signature               AlgorithmIdentifier,
 *     issuer                  Name,
 *     thisUpdate              ChoiceOfTime,
 *     nextUpdate              ChoiceOfTime OPTIONAL,
 *     revokedCertificates     SEQUENCE OF SEQUENCE  {
 *         userCertificate         CertificateSerialNumber,
 *         revocationDate          ChoiceOfTime,
 *         crlEntryExtensions      Extensions OPTIONAL
 *                                 -- 如果存在，必须是 v2
 *         }  OPTIONAL,
 *     crlExtensions           [0]  EXPLICIT Extensions OPTIONAL
 *                                  -- 如果存在，必须是 v2
 *     }
 * </pre>
 * <p>
 * CRL 通过证书工厂实例化。以下是一个实例化 X.509 CRL 的示例：
 * <pre>{@code
 * try (InputStream inStream = new FileInputStream("fileName-of-crl")) {
 *     CertificateFactory cf = CertificateFactory.getInstance("X.509");
 *     X509CRL crl = (X509CRL)cf.generateCRL(inStream);
 * }
 * }</pre>
 *
 * @author Hemma Prafullchandra
 *
 *
 * @see CRL
 * @see CertificateFactory
 * @see X509Extension
 */

public abstract class X509CRL extends CRL implements X509Extension {

    private transient X500Principal issuerPrincipal;

    /**
     * X.509 CRL 的构造函数。
     */
    protected X509CRL() {
        super("X.509");
    }

    /**
     * 比较此 CRL 与给定对象是否相等。如果 {@code other} 对象是 {@code X509CRL} 的实例，
     * 则检索其编码形式并与此 CRL 的编码形式进行比较。
     *
     * @param other 要与此 CRL 进行比较的对象。
     *
     * @return 如果两个 CRL 的编码形式匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof X509CRL)) {
            return false;
        }
        try {
            byte[] thisCRL = X509CRLImpl.getEncodedInternal(this);
            byte[] otherCRL = X509CRLImpl.getEncodedInternal((X509CRL)other);

            return Arrays.equals(thisCRL, otherCRL);
        } catch (CRLException e) {
            return false;
        }
    }

    /**
     * 从其编码形式返回此 CRL 的哈希码值。
     *
     * @return 哈希码值。
     */
    public int hashCode() {
        int retval = 0;
        try {
            byte[] crlData = X509CRLImpl.getEncodedInternal(this);
            for (int i = 1; i < crlData.length; i++) {
                 retval += crlData[i] * i;
            }
            return retval;
        } catch (CRLException e) {
            return retval;
        }
    }

    /**
     * 返回此 CRL 的 ASN.1 DER 编码形式。
     *
     * @return 此证书的编码形式
     * @exception CRLException 如果发生编码错误。
     */
    public abstract byte[] getEncoded()
        throws CRLException;

    /**
     * 验证此 CRL 是否使用给定公钥对应的私钥签名。
     *
     * @param key 用于执行验证的公钥。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception NoSuchProviderException 如果没有默认提供者。
     * @exception SignatureException 如果签名错误。
     * @exception CRLException 如果发生编码错误。
     */
    public abstract void verify(PublicKey key)
        throws CRLException,  NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * 验证此 CRL 是否使用给定公钥对应的私钥签名。
     * 此方法使用给定提供者提供的签名验证引擎。
     *
     * @param key 用于执行验证的公钥。
     * @param sigProvider 签名提供者的名称。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception NoSuchProviderException 如果提供者不正确。
     * @exception SignatureException 如果签名错误。
     * @exception CRLException 如果发生编码错误。
     */
    public abstract void verify(PublicKey key, String sigProvider)
        throws CRLException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;


    /**
     * 验证此 CRL 是否使用给定公钥对应的私钥签名。
     * 该方法使用由给定提供程序提供的签名验证引擎。
     * 注意，指定的 Provider 对象不必在提供程序列表中注册。
     *
     * 该方法是在 Java 平台标准版 1.8 版本中添加的。
     * 为了保持与现有服务提供程序的向后兼容性，此方法不是 {@code abstract}
     * 并且它提供了默认实现。
     *
     * @param key 用于执行验证的 PublicKey。
     * @param sigProvider 签名提供程序。
     *
     * @exception NoSuchAlgorithmException 当签名算法不受支持时。
     * @exception InvalidKeyException 当密钥不正确时。
     * @exception SignatureException 当签名错误时。
     * @exception CRLException 当编码错误时。
     * @since 1.8
     */
    public void verify(PublicKey key, Provider sigProvider)
        throws CRLException, NoSuchAlgorithmException,
        InvalidKeyException, SignatureException {
        String sigAlgName = getSigAlgName();
        Signature sig = (sigProvider == null)
            ? Signature.getInstance(sigAlgName)
            : Signature.getInstance(sigAlgName, sigProvider);

        try {
            byte[] paramBytes = getSigAlgParams();
            SignatureUtil.initVerifyWithParam(sig, key,
                SignatureUtil.getParamSpec(sigAlgName, paramBytes));
        } catch (ProviderException e) {
            throw new CRLException(e.getMessage(), e.getCause());
        } catch (InvalidAlgorithmParameterException e) {
            throw new CRLException(e);
        }

        byte[] tbsCRL = getTBSCertList();
        sig.update(tbsCRL, 0, tbsCRL.length);

        if (sig.verify(getSignature()) == false) {
            throw new SignatureException("签名不匹配。");
        }
    }

    /**
     * 从 CRL 中获取 {@code version}（版本号）值。
     * ASN.1 定义如下：
     * <pre>
     * version    Version 可选，
     *             -- 如果存在，必须是 v2
     *
     * Version  ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     *             -- v3 不适用于 CRL，但为了与证书的 Version 定义保持一致而出现
     * </pre>
     *
     * @return 版本号，即 1 或 2。
     */
    public abstract int getVersion();

    /**
     * <strong>不推荐使用</strong>，已被 {@linkplain
     * #getIssuerX500Principal()} 替代。此方法返回一个实现特定的 Principal 对象，不应被可移植代码依赖。
     *
     * <p>
     * 从 CRL 中获取 {@code issuer}（发行者区分名称）值。发行者名称标识签署（并发行）CRL 的实体。
     *
     * <p>发行者名称字段包含一个
     * X.500 区分名称（DN）。
     * ASN.1 定义如下：
     * <pre>
     * issuer    Name
     *
     * Name ::= CHOICE { RDNSequence }
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     * RelativeDistinguishedName ::=
     *     SET OF AttributeValueAssertion
     *
     * AttributeValueAssertion ::= SEQUENCE {
     *                               AttributeType,
     *                               AttributeValue }
     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY
     * </pre>
     * {@code Name} 描述了一个由属性（如国家名称）及其对应值（如 US）组成的层次名称。
     * {@code AttributeValue} 组件的类型由 {@code AttributeType} 确定；通常它是一个
     * {@code directoryString}。一个 {@code directoryString} 通常是
     * {@code PrintableString}、
     * {@code TeletexString} 或 {@code UniversalString} 之一。
     *
     * @return 一个 Principal，其名称是发行者区分名称。
     */
    public abstract Principal getIssuerDN();

    /**
     * 以 {@code X500Principal} 形式返回 CRL 中的发行者（发行者区分名称）值。
     * <p>
     * 建议子类覆盖此方法。
     *
     * @return 一个表示发行者区分名称的 {@code X500Principal}
     * @since 1.4
     */
    public X500Principal getIssuerX500Principal() {
        if (issuerPrincipal == null) {
            issuerPrincipal = X509CRLImpl.getIssuerX500Principal(this);
        }
        return issuerPrincipal;
    }

    /**
     * 从 CRL 中获取 {@code thisUpdate} 日期。
     * ASN.1 定义如下：
     * <pre>
     * thisUpdate   ChoiceOfTime
     * ChoiceOfTime ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @return 从 CRL 中获取的 {@code thisUpdate} 日期。
     */
    public abstract Date getThisUpdate();

    /**
     * 从 CRL 中获取 {@code nextUpdate} 日期。
     *
     * @return 从 CRL 中获取的 {@code nextUpdate} 日期，如果不存在则返回 null。
     */
    public abstract Date getNextUpdate();

    /**
     * 获取具有给定证书序列号的 CRL 条目（如果有）。
     *
     * @param serialNumber 要查找 CRL 条目的证书的序列号
     * @return 具有给定序列号的条目，如果此 CRL 中不存在这样的条目，则返回 null。
     * @see X509CRLEntry
     */
    public abstract X509CRLEntry
        getRevokedCertificate(BigInteger serialNumber);

    /**
     * 获取给定证书的 CRL 条目（如果有）。
     *
     * <p>此方法可用于查找间接 CRL 中的 CRL 条目，
     * 即包含来自 CRL 发行者以外的发行者的条目的 CRL。
     * 默认实现仅返回由 CRL 发行者发行的证书的条目。希望
     * 支持间接 CRL 的子类应覆盖此方法。
     *
     * @param certificate 要查找 CRL 条目的证书
     * @return 给定证书的条目，如果此 CRL 中不存在这样的条目，则返回 null。
     * @exception NullPointerException 如果证书为 null
     *
     * @since 1.5
     */
    public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        X500Principal certIssuer = certificate.getIssuerX500Principal();
        X500Principal crlIssuer = getIssuerX500Principal();
        if (certIssuer.equals(crlIssuer) == false) {
            return null;
        }
        return getRevokedCertificate(certificate.getSerialNumber());
    }

                /**
     * 获取此CRL中的所有条目。
     * 这将返回一个X509CRLEntry对象的集合。
     *
     * @return 所有条目，如果没有条目存在则返回null。
     * @see X509CRLEntry
     */
    public abstract Set<? extends X509CRLEntry> getRevokedCertificates();

    /**
     * 获取此CRL的DER编码CRL信息，即
     * {@code tbsCertList}。
     * 这可以用于独立验证签名。
     *
     * @return DER编码的CRL信息。
     * @exception CRLException 如果发生编码错误。
     */
    public abstract byte[] getTBSCertList() throws CRLException;

    /**
     * 获取CRL中的签名值（原始签名位）。
     * ASN.1定义如下：
     * <pre>
     * signature     BIT STRING
     * </pre>
     *
     * @return 签名。
     */
    public abstract byte[] getSignature();

    /**
     * 获取CRL签名算法的签名算法名称。例如，字符串"SHA256withRSA"。
     * ASN.1定义如下：
     * <pre>
     * signatureAlgorithm   AlgorithmIdentifier
     *
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *     algorithm               OBJECT IDENTIFIER,
     *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                             -- 包含与算法对象标识符值注册的类型
     *                             -- 相关的值
     * </pre>
     *
     * <p>算法名称是从{@code algorithm}
     * OID字符串中确定的。
     *
     * @return 签名算法名称。
     */
    public abstract String getSigAlgName();

    /**
     * 获取CRL中的签名算法OID字符串。
     * OID由一系列非负整数组成，中间用点分隔。
     * 例如，字符串"1.2.840.10040.4.3"标识了在
     * <a href="http://www.ietf.org/rfc/rfc3279.txt">RFC 3279: Algorithms and
     * Identifiers for the Internet X.509 Public Key Infrastructure Certificate
     * and CRL Profile</a>中定义的SHA-1与DSA签名算法。
     *
     * <p>有关ASN.1定义，请参见 {@link #getSigAlgName() getSigAlgName}。
     *
     * @return 签名算法OID字符串。
     */
    public abstract String getSigAlgOID();

    /**
     * 获取此CRL签名算法的DER编码签名算法参数。在大多数情况下，签名
     * 算法参数为null；参数通常与公钥一起提供。
     * 如果需要访问单个参数值，则使用
     * {@link java.security.AlgorithmParameters AlgorithmParameters}
     * 并使用由{@link #getSigAlgName() getSigAlgName}返回的名称实例化。
     *
     * <p>有关ASN.1定义，请参见 {@link #getSigAlgName() getSigAlgName}。
     *
     * @return DER编码的签名算法参数，如果没有参数则返回null。
     */
    public abstract byte[] getSigAlgParams();
}
