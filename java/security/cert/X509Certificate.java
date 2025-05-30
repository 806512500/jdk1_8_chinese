
/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates. All rights reserved.
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
import java.security.*;
import java.security.spec.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.security.auth.x500.X500Principal;

import sun.security.x509.X509CertImpl;
import sun.security.util.SignatureUtil;

/**
 * <p>
 * X.509 证书的抽象类。这提供了一种标准的方式来访问 X.509 证书的所有属性。
 * <p>
 * 1996 年 6 月，ISO/IEC 和 ANSI X9 完成了基本的 X.509 v3 格式，以下是以 ASN.1 描述的：
 * <pre>
 * Certificate  ::=  SEQUENCE  {
 *     tbsCertificate       TBSCertificate,
 *     signatureAlgorithm   AlgorithmIdentifier,
 *     signature            BIT STRING  }
 * </pre>
 * <p>
 * 这些证书广泛用于支持互联网安全系统中的身份验证和其他功能。常见的应用包括增强隐私邮件 (PEM)、传输层安全 (SSL)、
 * 可信软件分发的代码签名，以及安全电子交易 (SET)。
 * <p>
 * 这些证书由 <em>证书颁发机构</em> (CAs) 管理和认证。CAs 是提供证书服务的机构，通过将数据放入 X.509 标准格式并
 * 对这些数据进行数字签名来创建证书。CAs 作为受信任的第三方，为没有直接了解彼此的主体进行介绍。
 * CA 证书可以由自己或其他 CA（如“根”CA）签署。
 * <p>
 * 更多信息可以在
 * <a href="http://tools.ietf.org/html/rfc5280">RFC 5280: Internet X.509
 * 公钥基础设施证书和 CRL 配置文件</a> 中找到。
 * <p>
 * {@code tbsCertificate} 的 ASN.1 定义是：
 * <pre>
 * TBSCertificate  ::=  SEQUENCE  {
 *     version         [0]  EXPLICIT Version DEFAULT v1,
 *     serialNumber         CertificateSerialNumber,
 *     signature            AlgorithmIdentifier,
 *     issuer               Name,
 *     validity             Validity,
 *     subject              Name,
 *     subjectPublicKeyInfo SubjectPublicKeyInfo,
 *     issuerUniqueID  [1]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                          -- 如果存在，版本必须是 v2 或 v3
 *     subjectUniqueID [2]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                          -- 如果存在，版本必须是 v2 或 v3
 *     extensions      [3]  EXPLICIT Extensions OPTIONAL
 *                          -- 如果存在，版本必须是 v3
 *     }
 * </pre>
 * <p>
 * 证书的实例化使用证书工厂。以下是如何实例化 X.509 证书的示例：
 * <pre>
 * try (InputStream inStream = new FileInputStream("fileName-of-cert")) {
 *     CertificateFactory cf = CertificateFactory.getInstance("X.509");
 *     X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
 * }
 * </pre>
 *
 * @author Hemma Prafullchandra
 *
 *
 * @see Certificate
 * @see CertificateFactory
 * @see X509Extension
 */

public abstract class X509Certificate extends Certificate
implements X509Extension {

    private static final long serialVersionUID = -2491127588187038216L;

    private transient X500Principal subjectX500Principal, issuerX500Principal;

    /**
     * X.509 证书的构造函数。
     */
    protected X509Certificate() {
        super("X.509");
    }

    /**
     * 检查证书当前是否有效。如果当前日期和时间在证书的有效期内，则证书有效。
     * <p>
     * 有效期由两个日期/时间值组成：证书有效的开始日期和结束日期（和时间）。它在
     * ASN.1 中定义为：
     * <pre>
     * validity             Validity
     *
     * Validity ::= SEQUENCE {
     *     notBefore      CertificateValidityDate,
     *     notAfter       CertificateValidityDate }
     *
     * CertificateValidityDate ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @exception CertificateExpiredException 如果证书已过期。
     * @exception CertificateNotYetValidException 如果证书尚未生效。
     */
    public abstract void checkValidity()
        throws CertificateExpiredException, CertificateNotYetValidException;

    /**
     * 检查给定日期是否在证书的有效期内。换句话说，这确定证书在给定的日期/时间是否有效。
     *
     * @param date 要检查的日期，以确定该证书在该日期/时间是否有效。
     *
     * @exception CertificateExpiredException 如果证书相对于提供的 {@code date} 已过期。
     * @exception CertificateNotYetValidException 如果证书相对于提供的 {@code date} 尚未生效。
     *
     * @see #checkValidity()
     */
    public abstract void checkValidity(Date date)
        throws CertificateExpiredException, CertificateNotYetValidException;

    /**
     * 从证书中获取 {@code version}（版本号）值。
     * 该值在 ASN.1 中的定义为：
     * <pre>
     * version  [0] EXPLICIT Version DEFAULT v1
     *
     * Version ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     * </pre>
     * @return 版本号，即 1, 2 或 3。
     */
    public abstract int getVersion();

    /**
     * 从证书中获取 {@code serialNumber} 值。
     * 证书序列号是由证书颁发机构分配给每个证书的整数。它必须是每个由给定 CA 发行的证书的唯一值
     * （即，颁发者名称和序列号标识一个唯一的证书）。
     * 该值在 ASN.1 中的定义为：
     * <pre>
     * serialNumber     CertificateSerialNumber
     *
     * CertificateSerialNumber  ::=  INTEGER
     * </pre>
     *
     * @return 序列号。
     */
    public abstract BigInteger getSerialNumber();

    /**
     * <strong>已弃用</strong>，替换为 {@linkplain
     * #getIssuerX500Principal()}. 此方法返回 {@code issuer}
     * 作为特定于实现的 Principal 对象，便携式代码不应依赖于此。
     *
     * <p>
     * 从证书中获取 {@code issuer}（颁发者可区分名称）值。颁发者名称标识签署（并颁发）证书的实体。
     *
     * <p>颁发者名称字段包含一个
     * X.500 可区分名称 (DN)。
     * 该值在 ASN.1 中的定义为：
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
     * {@code Name} 描述了一个由属性及其对应值组成的层次名称，
     * 例如国家名称和对应的值 US。
     * {@code AttributeValue} 的类型由 {@code AttributeType} 确定；通常它是一个
     * {@code directoryString}。一个 {@code directoryString} 通常是
     * {@code PrintableString}、
     * {@code TeletexString} 或 {@code UniversalString}。
     *
     * @return 一个 Principal，其名称是颁发者可区分名称。
     */
    public abstract Principal getIssuerDN();

    /**
     * 从证书中返回颁发者（颁发者可区分名称）值作为 {@code X500Principal}。
     * <p>
     * 建议子类重写此方法。
     *
     * @return 一个 {@code X500Principal}，表示颁发者可区分名称
     * @since 1.4
     */
    public X500Principal getIssuerX500Principal() {
        if (issuerX500Principal == null) {
            issuerX500Principal = X509CertImpl.getIssuerX500Principal(this);
        }
        return issuerX500Principal;
    }

    /**
     * <strong>已弃用</strong>，替换为 {@linkplain
     * #getSubjectX500Principal()}. 此方法返回 {@code subject}
     * 作为特定于实现的 Principal 对象，便携式代码不应依赖于此。
     *
     * <p>
     * 从证书中获取 {@code subject}（主体可区分名称）值。如果 {@code subject} 值为空，
     * 则返回的 {@code Principal} 对象的 {@code getName()} 方法返回一个空字符串 ("")。
     *
     * <p>该值在 ASN.1 中的定义为：
     * <pre>
     * subject    Name
     * </pre>
     *
     * <p>有关 {@code Name} 和其他相关定义，请参见 {@link #getIssuerDN() getIssuerDN}。
     *
     * @return 一个 Principal，其名称是主体名称。
     */
    public abstract Principal getSubjectDN();

    /**
     * 从证书中返回主体（主体可区分名称）值作为 {@code X500Principal}。如果主体值为空，
     * 则返回的 {@code X500Principal} 对象的 {@code getName()} 方法返回一个空字符串 ("")。
     * <p>
     * 建议子类重写此方法。
     *
     * @return 一个 {@code X500Principal}，表示主体可区分名称
     * @since 1.4
     */
    public X500Principal getSubjectX500Principal() {
        if (subjectX500Principal == null) {
            subjectX500Principal = X509CertImpl.getSubjectX500Principal(this);
        }
        return subjectX500Principal;
    }

    /**
     * 从证书的有效期内获取 {@code notBefore} 日期。
     * 相关的 ASN.1 定义为：
     * <pre>
     * validity             Validity
     *
     * Validity ::= SEQUENCE {
     *     notBefore      CertificateValidityDate,
     *     notAfter       CertificateValidityDate }
     *
     * CertificateValidityDate ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @return 有效期的开始日期。
     * @see #checkValidity
     */
    public abstract Date getNotBefore();

    /**
     * 从证书的有效期内获取 {@code notAfter} 日期。有关相关的 ASN.1 定义，请参见 {@link #getNotBefore() getNotBefore}。
     *
     * @return 有效期的结束日期。
     * @see #checkValidity
     */
    public abstract Date getNotAfter();

    /**
     * 获取此证书的 DER 编码证书信息，即 {@code tbsCertificate}。
     * 这可以用于独立验证签名。
     *
     * @return DER 编码的证书信息。
     * @exception CertificateEncodingException 如果编码错误。
     */
    public abstract byte[] getTBSCertificate()
        throws CertificateEncodingException;

    /**
     * 从证书中获取 {@code signature} 值（原始签名位）。
     * 该值在 ASN.1 中的定义为：
     * <pre>
     * signature     BIT STRING
     * </pre>
     *
     * @return 签名。
     */
    public abstract byte[] getSignature();

    /**
     * 获取证书签名算法的签名算法名称。例如，字符串 "SHA256withRSA"。
     * 该值在 ASN.1 中的定义为：
     * <pre>
     * signatureAlgorithm   AlgorithmIdentifier
     *
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *     algorithm               OBJECT IDENTIFIER,
     *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                             -- 包含与算法对象标识符值注册的类型
     *                             -- 的值
     * </pre>
     *
     * <p>签名算法名称从 {@code algorithm} OID 字符串中确定。
     *
     * @return 签名算法名称。
     */
    public abstract String getSigAlgName();

    /**
     * 从证书中获取签名算法 OID 字符串。OID 由一系列非负整数组成，用点分隔。
     * 例如，字符串 "1.2.840.10040.4.3" 标识
     * <a href="http://www.ietf.org/rfc/rfc3279.txt">RFC 3279: Internet X.509 公钥基础设施证书
     * 和 CRL 配置文件</a> 中定义的 SHA-1 与 DSA 签名算法。
     *
     * <p>有关相关的 ASN.1 定义，请参见 {@link #getSigAlgName() getSigAlgName}。
     *
     * @return 签名算法 OID 字符串。
     */
    public abstract String getSigAlgOID();

    /**
     * 从证书的签名算法中获取 DER 编码的签名算法参数。在大多数情况下，签名算法参数为 null；
     * 参数通常与证书的公钥一起提供。如果需要访问单个参数值，则可以使用
     * {@link java.security.AlgorithmParameters AlgorithmParameters}
     * 并使用 {@link #getSigAlgName() getSigAlgName} 返回的名称实例化。
     *
     * <p>有关相关的 ASN.1 定义，请参见 {@link #getSigAlgName() getSigAlgName}。
     *
     * @return DER 编码的签名算法参数，如果不存在参数则返回 null。
     */
    public abstract byte[] getSigAlgParams();

    /**
     * 从证书中获取 {@code issuerUniqueID} 值。颁发者唯一标识符存在于证书中，以处理随时间重用颁发者名称的可能性。
     * RFC 5280 建议不要重用名称，并且符合规范的证书不应使用唯一标识符。符合该配置文件的应用程序应能够
     * 解析唯一标识符并进行比较。
     *
     * <p>该值在 ASN.1 中的定义为：
     * <pre>
     * issuerUniqueID  [1]  IMPLICIT UniqueIdentifier OPTIONAL
     *
     * UniqueIdentifier  ::=  BIT STRING
     * </pre>
     *
     * @return 颁发者唯一标识符，如果证书中不存在则返回 null。
     */
    public abstract boolean[] getIssuerUniqueID();


                /**
     * 从证书中获取 {@code subjectUniqueID} 值。
     *
     * <p>此值的 ASN.1 定义为：
     * <pre>
     * subjectUniqueID  [2]  IMPLICIT UniqueIdentifier OPTIONAL
     *
     * UniqueIdentifier  ::=  BIT STRING
     * </pre>
     *
     * @return 证书中包含的主体唯一标识符，如果证书中未包含该标识符，则返回 null。
     */
    public abstract boolean[] getSubjectUniqueID();

    /**
     * 获取表示 {@code KeyUsage} 扩展（OID = 2.5.29.15）位的布尔数组。
     * KeyUsage 扩展定义了证书中包含的密钥的用途（例如，加密、签名、证书签名）。
     * 该扩展的 ASN.1 定义为：
     * <pre>
     * KeyUsage ::= BIT STRING {
     *     digitalSignature        (0),
     *     nonRepudiation          (1),
     *     keyEncipherment         (2),
     *     dataEncipherment        (3),
     *     keyAgreement            (4),
     *     keyCertSign             (5),
     *     cRLSign                 (6),
     *     encipherOnly            (7),
     *     decipherOnly            (8) }
     * </pre>
     * RFC 5280 建议使用此扩展时将其标记为关键扩展。
     *
     * @return 该证书的 KeyUsage 扩展，表示为布尔数组。数组中的 KeyUsage 值的顺序与上述 ASN.1 定义中的顺序相同。数组将包含上述每个 KeyUsage 的值。如果证书中编码的 KeyUsage 列表比上述列表长，则不会被截断。如果证书中不包含 KeyUsage 扩展，则返回 null。
     */
    public abstract boolean[] getKeyUsage();

    /**
     * 获取表示 {@code ExtKeyUsageSyntax} 字段的扩展密钥用法扩展（OID = 2.5.29.37）的不可修改字符串列表。
     * 它指示了证书中公钥的使用目的，除了或代替基本用途指示外，还可以用于其他用途。该扩展的 ASN.1 定义为：
     * <pre>
     * ExtKeyUsageSyntax ::= SEQUENCE SIZE (1..MAX) OF KeyPurposeId
     *
     * KeyPurposeId ::= OBJECT IDENTIFIER
     * </pre>
     *
     * 密钥用途可以由任何有需要的组织定义。用于标识密钥用途的对象标识符应按照 IANA 或 ITU-T Rec. X.660 | ISO/IEC/ITU 9834-1 的规定分配。
     * <p>
     * 该方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，该方法不是 {@code abstract} 的，并且提供了一个默认实现。子类应覆盖此方法以提供正确的实现。
     *
     * @return 该证书的 ExtendedKeyUsage 扩展，表示为不可修改的字符串列表。如果证书中不包含 ExtendedKeyUsage 扩展，则返回 null。
     * @throws CertificateParsingException 如果扩展无法解码
     * @since 1.4
     */
    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        return X509CertImpl.getExtendedKeyUsage(this);
    }

    /**
     * 从关键的 {@code BasicConstraints} 扩展（OID = 2.5.29.19）中获取证书约束路径长度。
     * <p>
     * 基本约束扩展标识证书的主体是否为证书颁发机构（CA）以及通过该 CA 可能存在的认证路径的深度。只有在 {@code cA} 设置为 TRUE 时，{@code pathLenConstraint} 字段（见下文）才有意义。在这种情况下，它给出了认证路径中可以跟随此证书的最大 CA 证书数量。值为零表示路径中只能跟随一个最终实体证书。
     * <p>
     * 该扩展的 ASN.1 定义为：
     * <pre>
     * BasicConstraints ::= SEQUENCE {
     *     cA                  BOOLEAN DEFAULT FALSE,
     *     pathLenConstraint   INTEGER (0..MAX) OPTIONAL }
     * </pre>
     *
     * @return 如果证书中包含 BasicConstraints 扩展且证书的主体为 CA，则返回 {@code pathLenConstraint} 的值，否则返回 -1。
     * 如果证书的主体为 CA 且 {@code pathLenConstraint} 未出现，则返回 {@code Integer.MAX_VALUE}，表示认证路径的长度没有限制。
     */
    public abstract int getBasicConstraints();

    /**
     * 从 {@code SubjectAltName} 扩展（OID = 2.5.29.17）中获取不可修改的主体备用名称集合。
     * <p>
     * {@code SubjectAltName} 扩展的 ASN.1 定义为：
     * <pre>
     * SubjectAltName ::= GeneralNames
     *
     * GeneralNames :: = SEQUENCE SIZE (1..MAX) OF GeneralName
     *
     * GeneralName ::= CHOICE {
     *      otherName                       [0]     OtherName,
     *      rfc822Name                      [1]     IA5String,
     *      dNSName                         [2]     IA5String,
     *      x400Address                     [3]     ORAddress,
     *      directoryName                   [4]     Name,
     *      ediPartyName                    [5]     EDIPartyName,
     *      uniformResourceIdentifier       [6]     IA5String,
     *      iPAddress                       [7]     OCTET STRING,
     *      registeredID                    [8]     OBJECT IDENTIFIER}
     * </pre>
     * <p>
     * 如果证书中不包含 {@code SubjectAltName} 扩展，则返回 {@code null}。否则，返回一个包含扩展中每个 {@code GeneralName} 的 {@code Collection}。每个条目是一个 {@code List}，其第一个条目是一个 {@code Integer}（名称类型，0-8），第二个条目是一个 {@code String} 或字节数组（名称，以字符串或 ASN.1 DER 编码形式表示）。
     * <p>
     * <a href="http://www.ietf.org/rfc/rfc822.txt">RFC 822</a>、DNS 和 URI 名称以字符串形式返回，使用这些类型的标准字符串格式（受 RFC 5280 中包含的限制）。IPv4 地址名称使用点分十进制表示法返回。IPv6 地址名称以 "a1:a2:...:a8" 的形式返回，其中 a1-a8 是表示地址的八个 16 位部分的十六进制值。OID 名称以一系列由点分隔的非负整数字符串形式返回。目录名称（可分辨名称）以 <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a> 字符串格式返回。其他名称、X.400 名称、EDI 党名或任何其他类型的名称没有定义标准的字符串格式。它们以包含名称的 ASN.1 DER 编码形式的字节数组返回。
     * <p>
     * 注意，返回的 {@code Collection} 可能包含多个相同类型的名称。此外，返回的 {@code Collection} 是不可修改的，任何包含字节数组的条目都会被克隆以防止后续修改。
     * <p>
     * 该方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，该方法不是 {@code abstract} 的，并且提供了一个默认实现。子类应覆盖此方法以提供正确的实现。
     *
     * @return 不可修改的主体备用名称集合（或 {@code null}）
     * @throws CertificateParsingException 如果扩展无法解码
     * @since 1.4
     */
    public Collection<List<?>> getSubjectAlternativeNames()
        throws CertificateParsingException {
        return X509CertImpl.getSubjectAlternativeNames(this);
    }

    /**
     * 从 {@code IssuerAltName} 扩展（OID = 2.5.29.18）中获取不可修改的发行者备用名称集合。
     * <p>
     * {@code IssuerAltName} 扩展的 ASN.1 定义为：
     * <pre>
     * IssuerAltName ::= GeneralNames
     * </pre>
     * {@code GeneralNames} 的 ASN.1 定义在 {@link #getSubjectAlternativeNames getSubjectAlternativeNames} 中定义。
     * <p>
     * 如果证书中不包含 {@code IssuerAltName} 扩展，则返回 {@code null}。否则，返回一个包含扩展中每个 {@code GeneralName} 的 {@code Collection}。每个条目是一个 {@code List}，其第一个条目是一个 {@code Integer}（名称类型，0-8），第二个条目是一个 {@code String} 或字节数组（名称，以字符串或 ASN.1 DER 编码形式表示）。有关每种名称类型的格式的更多详细信息，请参见 {@code getSubjectAlternativeNames} 方法。
     * <p>
     * 注意，返回的 {@code Collection} 可能包含多个相同类型的名称。此外，返回的 {@code Collection} 是不可修改的，任何包含字节数组的条目都会被克隆以防止后续修改。
     * <p>
     * 该方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，该方法不是 {@code abstract} 的，并且提供了一个默认实现。子类应覆盖此方法以提供正确的实现。
     *
     * @return 不可修改的发行者备用名称集合（或 {@code null}）
     * @throws CertificateParsingException 如果扩展无法解码
     * @since 1.4
     */
    public Collection<List<?>> getIssuerAlternativeNames()
        throws CertificateParsingException {
        return X509CertImpl.getIssuerAlternativeNames(this);
    }

    /**
     * 验证此证书是否使用指定公钥对应的私钥签名。
     * 此方法使用指定提供者提供的签名验证引擎。注意，指定的 Provider 对象不必在提供者列表中注册。
     *
     * 该方法是在 Java 平台标准版 1.8 版本中添加的。为了保持与现有服务提供者的向后兼容性，该方法不是 {@code abstract} 的，并且提供了一个默认实现。
     *
     * @param key 用于执行验证的 PublicKey。
     * @param sigProvider 签名提供者。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception SignatureException 如果签名错误。
     * @exception CertificateException 如果编码错误。
     * @exception UnsupportedOperationException 如果方法不受支持
     * @since 1.8
     */
    public void verify(PublicKey key, Provider sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, SignatureException {
        String sigName = getSigAlgName();
        Signature sig = (sigProvider == null)
            ? Signature.getInstance(sigName)
            : Signature.getInstance(sigName, sigProvider);

        try {
            SignatureUtil.initVerifyWithParam(sig, key,
                SignatureUtil.getParamSpec(sigName, getSigAlgParams()));
        } catch (ProviderException e) {
            throw new CertificateException(e.getMessage(), e.getCause());
        } catch (InvalidAlgorithmParameterException e) {
            throw new CertificateException(e);
        }

        byte[] tbsCert = getTBSCertificate();
        sig.update(tbsCert, 0, tbsCert.length);

        if (sig.verify(getSignature()) == false) {
            throw new SignatureException("签名不匹配。");
        }
    }
}
