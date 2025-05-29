/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;
import javax.security.auth.x500.X500Principal;

import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.*;

/**
 * 一个选择与所有指定标准匹配的 {@code X509Certificates} 的 {@code CertSelector}。当从 {@code CertStore} 中选择证书以构建
 * PKIX 合规的认证路径时，此类特别有用。
 * <p>
 * 刚构造时，一个 {@code X509CertSelector} 没有启用任何标准，每个 {@code get} 方法返回一个默认值
 * ({@code null}，或对于 {@link #getBasicConstraints getBasicConstraints} 方法为 {@code -1})。因此，
 * {@link #match match} 方法会对任何 {@code X509Certificate} 返回 {@code true}。通常，启用几个标准（通过调用
 * {@link #setIssuer setIssuer} 或 {@link #setKeyUsage setKeyUsage} 等方法）然后将
 * {@code X509CertSelector} 传递给 {@link CertStore#getCertificates CertStore.getCertificates} 或类似方法。
 * <p>
 * 可以启用多个标准（例如调用 {@link #setIssuer setIssuer} 和 {@link #setSerialNumber setSerialNumber}），
 * 使得 {@code match} 方法通常唯一匹配一个 {@code X509Certificate}。我们说通常，因为两个发行 CA 可能有相同的
 * 区分名称，每个发行的证书具有相同的序列号。其他唯一组合包括发行者、主体、subjectKeyIdentifier 和/或
 * subjectPublicKey 标准。
 * <p>
 * 请参阅 <a href="http://www.ietf.org/rfc/rfc3280.txt">RFC 3280: Internet X.509 公钥基础设施证书和 CRL 配置文件</a>
 * 以获取下面提到的 X.509 证书扩展的定义。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。多个线程需要并发访问单个对象时，应同步并提供必要的锁定。
 * 每个操作独立对象的多个线程不需要同步。
 *
 * @see CertSelector
 * @see X509Certificate
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public class X509CertSelector implements CertSelector {

    private static final Debug debug = Debug.getInstance("certpath");

    private final static ObjectIdentifier ANY_EXTENDED_KEY_USAGE =
        ObjectIdentifier.newInternal(new int[] {2, 5, 29, 37, 0});

    static {
        CertPathHelperImpl.initialize();
    }

    private BigInteger serialNumber;
    private X500Principal issuer;
    private X500Principal subject;
    private byte[] subjectKeyID;
    private byte[] authorityKeyID;
    private Date certificateValid;
    private Date privateKeyValid;
    private ObjectIdentifier subjectPublicKeyAlgID;
    private PublicKey subjectPublicKey;
    private byte[] subjectPublicKeyBytes;
    private boolean[] keyUsage;
    private Set<String> keyPurposeSet;
    private Set<ObjectIdentifier> keyPurposeOIDSet;
    private Set<List<?>> subjectAlternativeNames;
    private Set<GeneralNameInterface> subjectAlternativeGeneralNames;
    private CertificatePolicySet policy;
    private Set<String> policySet;
    private Set<List<?>> pathToNames;
    private Set<GeneralNameInterface> pathToGeneralNames;
    private NameConstraintsExtension nc;
    private byte[] ncBytes;
    private int basicConstraints = -1;
    private X509Certificate x509Cert;
    private boolean matchAllSubjectAltNames = true;

    private static final Boolean FALSE = Boolean.FALSE;

    private static final int PRIVATE_KEY_USAGE_ID = 0;
    private static final int SUBJECT_ALT_NAME_ID = 1;
    private static final int NAME_CONSTRAINTS_ID = 2;
    private static final int CERT_POLICIES_ID = 3;
    private static final int EXTENDED_KEY_USAGE_ID = 4;
    private static final int NUM_OF_EXTENSIONS = 5;
    private static final String[] EXTENSION_OIDS = new String[NUM_OF_EXTENSIONS];

    static {
        EXTENSION_OIDS[PRIVATE_KEY_USAGE_ID]  = "2.5.29.16";
        EXTENSION_OIDS[SUBJECT_ALT_NAME_ID]   = "2.5.29.17";
        EXTENSION_OIDS[NAME_CONSTRAINTS_ID]   = "2.5.29.30";
        EXTENSION_OIDS[CERT_POLICIES_ID]      = "2.5.29.32";
        EXTENSION_OIDS[EXTENDED_KEY_USAGE_ID] = "2.5.29.37";
    };

    /* 代表 GeneralName 类型的常量 */
    static final int NAME_ANY = 0;
    static final int NAME_RFC822 = 1;
    static final int NAME_DNS = 2;
    static final int NAME_X400 = 3;
    static final int NAME_DIRECTORY = 4;
    static final int NAME_EDI = 5;
    static final int NAME_URI = 6;
    static final int NAME_IP = 7;
    static final int NAME_OID = 8;

    /**
     * 创建一个 {@code X509CertSelector}。最初，没有设置任何标准，因此任何 {@code X509Certificate} 都会匹配。
     */
    public X509CertSelector() {
        // 空
    }

    /**
     * 设置 certificateEquals 标准。指定的 {@code X509Certificate} 必须等于传递给 {@code match} 方法的
     * {@code X509Certificate}。如果为 {@code null}，则不应用此检查。
     *
     * <p>当需要匹配单个证书时，此方法特别有用。虽然可以与其他标准一起指定 certificateEquals 标准，但通常不实用或不必要。
     *
     * @param cert 要匹配的 {@code X509Certificate}（或 {@code null}）
     * @see #getCertificate
     */
    public void setCertificate(X509Certificate cert) {
        x509Cert = cert;
    }

    /**
     * 设置 serialNumber 标准。指定的序列号必须与 {@code X509Certificate} 中的证书序列号匹配。如果为 {@code null}，则任何证书序列号都可以。
     *
     * @param serial 要匹配的证书序列号（或 {@code null}）
     * @see #getSerialNumber
     */
    public void setSerialNumber(BigInteger serial) {
        serialNumber = serial;
    }


                /**
     * 设置发行者标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的发行者可区分名称匹配。如果为 {@code null}，则任何发行者
     * 可区分名称都可以。
     *
     * @param issuer 作为 X500Principal 的可区分名称
     *                 （或 {@code null}）
     * @since 1.5
     */
    public void setIssuer(X500Principal issuer) {
        this.issuer = issuer;
    }

    /**
     * <strong>不推荐使用</strong>，请使用 {@linkplain #setIssuer(X500Principal)}
     * 或 {@linkplain #setIssuer(byte[])} 代替。不应依赖此方法，因为它可能会因为
     * <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a> 字符串形式的某些可区分名称的编码信息丢失而无法匹配某些证书。
     * <p>
     * 设置发行者标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的发行者可区分名称匹配。如果为 {@code null}，则任何发行者
     * 可区分名称都可以。
     * <p>
     * 如果 {@code issuerDN} 不为 {@code null}，则应包含一个
     * RFC 2253 格式的可区分名称。
     *
     * @param issuerDN RFC 2253 格式的可区分名称
     *                 （或 {@code null}）
     * @throws IOException 如果解析错误（DN 格式不正确）
     */
    public void setIssuer(String issuerDN) throws IOException {
        if (issuerDN == null) {
            issuer = null;
        } else {
            issuer = new X500Name(issuerDN).asX500Principal();
        }
    }

    /**
     * 设置发行者标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的发行者可区分名称匹配。如果指定为 {@code null}，
     * 则禁用发行者标准，任何发行者可区分名称都可以。
     * <p>
     * 如果 {@code issuerDN} 不为 {@code null}，则应包含一个
     * 单个 DER 编码的可区分名称，如 X.501 所定义。ASN.1
     * 该结构的符号如下。
     * <pre>{@code
     * Name ::= CHOICE {
     *   RDNSequence }
     *
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     * RelativeDistinguishedName ::=
     *   SET SIZE (1 .. MAX) OF AttributeTypeAndValue
     *
     * AttributeTypeAndValue ::= SEQUENCE {
     *   type     AttributeType,
     *   value    AttributeValue }
     *
     * AttributeType ::= OBJECT IDENTIFIER
     *
     * AttributeValue ::= ANY DEFINED BY AttributeType
     * ....
     * DirectoryString ::= CHOICE {
     *       teletexString           TeletexString (SIZE (1..MAX)),
     *       printableString         PrintableString (SIZE (1..MAX)),
     *       universalString         UniversalString (SIZE (1..MAX)),
     *       utf8String              UTF8String (SIZE (1.. MAX)),
     *       bmpString               BMPString (SIZE (1..MAX)) }
     * }</pre>
     * <p>
     * 注意，这里指定的字节数组会被克隆以防止后续修改。
     *
     * @param issuerDN 包含可区分名称的字节数组
     *                 （或 {@code null}）
     * @throws IOException 如果编码错误（DN 格式不正确）
     */
    public void setIssuer(byte[] issuerDN) throws IOException {
        try {
            issuer = (issuerDN == null ? null : new X500Principal(issuerDN));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

    /**
     * 设置主题标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的主题可区分名称匹配。如果为 {@code null}，则任何主题
     * 可区分名称都可以。
     *
     * @param subject 作为 X500Principal 的可区分名称
     *                  （或 {@code null}）
     * @since 1.5
     */
    public void setSubject(X500Principal subject) {
        this.subject = subject;
    }

    /**
     * <strong>不推荐使用</strong>，请使用 {@linkplain #setSubject(X500Principal)}
     * 或 {@linkplain #setSubject(byte[])} 代替。不应依赖此方法，因为它可能会因为
     * RFC 2253 字符串形式的某些可区分名称的编码信息丢失而无法匹配某些证书。
     * <p>
     * 设置主题标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的主题可区分名称匹配。如果为 {@code null}，则任何主题
     * 可区分名称都可以。
     * <p>
     * 如果 {@code subjectDN} 不为 {@code null}，则应包含一个
     * RFC 2253 格式的可区分名称。
     *
     * @param subjectDN RFC 2253 格式的可区分名称
     *                  （或 {@code null}）
     * @throws IOException 如果解析错误（DN 格式不正确）
     */
    public void setSubject(String subjectDN) throws IOException {
        if (subjectDN == null) {
            subject = null;
        } else {
            subject = new X500Name(subjectDN).asX500Principal();
        }
    }

    /**
     * 设置主题标准。指定的可区分名称
     * 必须与 {@code X509Certificate} 中的主题可区分名称匹配。如果为 {@code null}，则任何主题
     * 可区分名称都可以。
     * <p>
     * 如果 {@code subjectDN} 不为 {@code null}，则应包含一个
     * 单个 DER 编码的可区分名称，如 X.501 所定义。有关此结构的 ASN.1
     * 符号，请参见 {@link #setIssuer(byte [] issuerDN) setIssuer(byte [] issuerDN)}。
     *
     * @param subjectDN 包含可区分名称的字节数组
     *                  （或 {@code null}）
     * @throws IOException 如果编码错误（DN 格式不正确）
     */
    public void setSubject(byte[] subjectDN) throws IOException {
        try {
            subject = (subjectDN == null ? null : new X500Principal(subjectDN));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid name", e);
        }
    }

                /**
     * 设置 subjectKeyIdentifier 标准。该 {@code X509Certificate} 必须包含一个 SubjectKeyIdentifier
     * 扩展，该扩展的内容必须与指定的标准值匹配。
     * 如果标准值为 {@code null}，则不会进行 subjectKeyIdentifier 检查。
     * <p>
     * 如果 {@code subjectKeyID} 不为 {@code null}，它应该包含一个与扩展值内容相对应的单个 DER 编码值
     * （不包括对象标识符、关键性设置和封装的 OCTET STRING）。
     * SubjectKeyIdentifier 扩展的 ASN.1 表示如下。
     *
     * <pre>{@code
     * SubjectKeyIdentifier ::= KeyIdentifier
     *
     * KeyIdentifier ::= OCTET STRING
     * }</pre>
     * <p>
     * 由于主体密钥标识符的格式不受任何标准的强制规定，因此 {@code X509CertSelector} 不解析主体密钥标识符。
     * 相反，值是通过逐字节比较来进行的。
     * <p>
     * 请注意，这里提供的字节数组会被克隆以防止后续修改。
     *
     * @param subjectKeyID 主体密钥标识符（或 {@code null}）
     * @see #getSubjectKeyIdentifier
     */
    public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
        if (subjectKeyID == null) {
            this.subjectKeyID = null;
        } else {
            this.subjectKeyID = subjectKeyID.clone();
        }
    }

    /**
     * 设置 authorityKeyIdentifier 标准。该 {@code X509Certificate} 必须包含一个 AuthorityKeyIdentifier
     * 扩展，该扩展的内容必须与指定的标准值匹配。
     * 如果标准值为 {@code null}，则不会进行 authorityKeyIdentifier 检查。
     * <p>
     * 如果 {@code authorityKeyID} 不为 {@code null}，它应该包含一个与扩展值内容相对应的单个 DER 编码值
     * （不包括对象标识符、关键性设置和封装的 OCTET STRING）。
     * AuthorityKeyIdentifier 扩展的 ASN.1 表示如下。
     *
     * <pre>{@code
     * AuthorityKeyIdentifier ::= SEQUENCE {
     *    keyIdentifier             [0] KeyIdentifier           OPTIONAL,
     *    authorityCertIssuer       [1] GeneralNames            OPTIONAL,
     *    authorityCertSerialNumber [2] CertificateSerialNumber OPTIONAL  }
     *
     * KeyIdentifier ::= OCTET STRING
     * }</pre>
     * <p>
     * 权威密钥标识符不由 {@code X509CertSelector} 解析。相反，值是通过逐字节比较来进行的。
     * <p>
     * 当 {@code AuthorityKeyIdentifier} 的 {@code keyIdentifier} 字段被填充时，其值通常取自发行者证书中的
     * {@code SubjectKeyIdentifier} 扩展。但是，需要注意的是，对发行者证书调用
     * {@code X509Certificate.getExtensionValue(<SubjectKeyIdentifier Object
     * Identifier>)} 的结果不能直接用作 {@code setAuthorityKeyIdentifier} 的输入。
     * 这是因为 SubjectKeyIdentifier 仅包含一个 KeyIdentifier OCTET STRING，而不是一个包含
     * KeyIdentifier、GeneralNames 和 CertificateSerialNumber 的 SEQUENCE。
     * 为了使用发行者证书的 {@code SubjectKeyIdentifier} 扩展值，需要提取嵌入的
     * {@code KeyIdentifier} OCTET STRING，然后将其 DER 编码到 SEQUENCE 中。
     * 有关 SubjectKeyIdentifier 的更多详细信息，请参见
     * {@link #setSubjectKeyIdentifier(byte[] subjectKeyID)}。
     * <p>
     * 请注意，这里提供的字节数组会被克隆以防止后续修改。
     *
     * @param authorityKeyID 权威密钥标识符（或 {@code null}）
     * @see #getAuthorityKeyIdentifier
     */
    public void setAuthorityKeyIdentifier(byte[] authorityKeyID) {
        if (authorityKeyID == null) {
            this.authorityKeyID = null;
        } else {
            this.authorityKeyID = authorityKeyID.clone();
        }
    }

    /**
     * 设置 certificateValid 标准。指定的日期必须在 {@code X509Certificate} 的证书有效期内。
     * 如果为 {@code null}，则不会进行 certificateValid 检查。
     * <p>
     * 请注意，这里提供的 {@code Date} 会被克隆以防止后续修改。
     *
     * @param certValid 要检查的 {@code Date}（或 {@code null}）
     * @see #getCertificateValid
     */
    public void setCertificateValid(Date certValid) {
        if (certValid == null) {
            certificateValid = null;
        } else {
            certificateValid = (Date)certValid.clone();
        }
    }

    /**
     * 设置 privateKeyValid 标准。指定的日期必须在 {@code X509Certificate} 的私钥有效期内。
     * 如果为 {@code null}，则不会进行 privateKeyValid 检查。
     * <p>
     * 请注意，这里提供的 {@code Date} 会被克隆以防止后续修改。
     *
     * @param privateKeyValid 要检查的 {@code Date}（或 {@code null}）
     * @see #getPrivateKeyValid
     */
    public void setPrivateKeyValid(Date privateKeyValid) {
        if (privateKeyValid == null) {
            this.privateKeyValid = null;
        } else {
            this.privateKeyValid = (Date)privateKeyValid.clone();
        }
    }

    /**
     * 设置 subjectPublicKeyAlgID 标准。该 {@code X509Certificate} 必须包含一个具有指定算法的主体公钥。
     * 如果为 {@code null}，则不会进行 subjectPublicKeyAlgID 检查。
     *
     * @param oid 要检查的算法的对象标识符（OID）（或 {@code null}）。OID 由一系列非负整数组成，用点分隔。
     * @throws IOException 如果 OID 无效，例如第一个组件不是 0、1 或 2，或者第二个组件大于 39。
     *
     * @see #getSubjectPublicKeyAlgID
     */
    public void setSubjectPublicKeyAlgID(String oid) throws IOException {
        if (oid == null) {
            subjectPublicKeyAlgID = null;
        } else {
            subjectPublicKeyAlgID = new ObjectIdentifier(oid);
        }
    }

                /**
     * 设置 subjectPublicKey 标准。{@code X509Certificate} 必须包含指定的主体公钥。
     * 如果为 {@code null}，则不进行 subjectPublicKey 检查。
     *
     * @param key 要检查的主体公钥（或 {@code null}）
     * @see #getSubjectPublicKey
     */
    public void setSubjectPublicKey(PublicKey key) {
        if (key == null) {
            subjectPublicKey = null;
            subjectPublicKeyBytes = null;
        } else {
            subjectPublicKey = key;
            subjectPublicKeyBytes = key.getEncoded();
        }
    }

    /**
     * 设置 subjectPublicKey 标准。{@code X509Certificate} 必须包含指定的主体公钥。
     * 如果为 {@code null}，则不进行 subjectPublicKey 检查。
     * <p>
     * 由于此方法允许以字节数组的形式指定公钥，因此可以用于未知的密钥类型。
     * <p>
     * 如果 {@code key} 不为 {@code null}，则应包含一个 DER 编码的 SubjectPublicKeyInfo 结构，如 X.509 所定义。
     * ASN.1 表示法如下。
     * <pre>{@code
     * SubjectPublicKeyInfo  ::=  SEQUENCE  {
     *   algorithm            AlgorithmIdentifier,
     *   subjectPublicKey     BIT STRING  }
     *
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *   algorithm               OBJECT IDENTIFIER,
     *   parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                              -- 包含与算法对象标识符值注册的类型
     *                              -- 的值
     * }</pre>
     * <p>
     * 注意，这里提供的字节数组将被克隆以防止后续修改。
     *
     * @param key 包含主体公钥的 ASN.1 DER 形式的字节数组（或 {@code null}）
     * @throws IOException 如果发生编码错误（主体公钥形式不正确）
     * @see #getSubjectPublicKey
     */
    public void setSubjectPublicKey(byte[] key) throws IOException {
        if (key == null) {
            subjectPublicKey = null;
            subjectPublicKeyBytes = null;
        } else {
            subjectPublicKeyBytes = key.clone();
            subjectPublicKey = X509Key.parse(new DerValue(subjectPublicKeyBytes));
        }
    }

    /**
     * 设置 keyUsage 标准。{@code X509Certificate} 必须允许指定的 keyUsage 值。
     * 如果为 {@code null}，则不进行 keyUsage 检查。注意，没有 keyUsage 扩展的
     * {@code X509Certificate} 隐式允许所有 keyUsage 值。
     * <p>
     * 注意，提供的布尔数组将被克隆以防止后续修改。
     *
     * @param keyUsage 与
     * {@link X509Certificate#getKeyUsage() X509Certificate.getKeyUsage()} 返回的布尔数组
     * 格式相同的布尔数组。或 {@code null}。
     * @see #getKeyUsage
     */
    public void setKeyUsage(boolean[] keyUsage) {
        if (keyUsage == null) {
            this.keyUsage = null;
        } else {
            this.keyUsage = keyUsage.clone();
        }
    }

    /**
     * 设置 extendedKeyUsage 标准。{@code X509Certificate} 必须在其扩展密钥用途扩展中
     * 允许指定的密钥用途。如果 {@code keyPurposeSet} 为空或 {@code null}，
     * 则不进行 extendedKeyUsage 检查。注意，没有 extendedKeyUsage 扩展的
     * {@code X509Certificate} 隐式允许所有密钥用途。
     * <p>
     * 注意，{@code Set} 将被克隆以防止后续修改。
     *
     * @param keyPurposeSet 以字符串格式表示的密钥用途 OID 的 {@code Set}（或 {@code null}）。
     * 每个 OID 由以点分隔的非负整数表示。
     * @throws IOException 如果 OID 无效，例如第一个组件不是 0、1 或 2，或者第二个组件大于 39。
     * @see #getExtendedKeyUsage
     */
    public void setExtendedKeyUsage(Set<String> keyPurposeSet) throws IOException {
        if ((keyPurposeSet == null) || keyPurposeSet.isEmpty()) {
            this.keyPurposeSet = null;
            keyPurposeOIDSet = null;
        } else {
            this.keyPurposeSet =
                Collections.unmodifiableSet(new HashSet<String>(keyPurposeSet));
            keyPurposeOIDSet = new HashSet<ObjectIdentifier>();
            for (String s : this.keyPurposeSet) {
                keyPurposeOIDSet.add(new ObjectIdentifier(s));
            }
        }
    }

    /**
     * 启用/禁用在 {@link #setSubjectAlternativeNames
     * setSubjectAlternativeNames} 或 {@link #addSubjectAlternativeName
     * addSubjectAlternativeName} 方法中指定的所有 subjectAlternativeNames 的匹配。
     * 如果启用，{@code X509Certificate} 必须包含所有指定的主体备用名称。
     * 如果禁用，{@code X509Certificate} 必须包含至少一个指定的主体备用名称。
     *
     * <p>matchAllNames 标志默认为 {@code true}。
     *
     * @param matchAllNames 如果为 {@code true}，则启用标志；
     * 如果为 {@code false}，则禁用标志。
     * @see #getMatchAllSubjectAltNames
     */
    public void setMatchAllSubjectAltNames(boolean matchAllNames) {
        this.matchAllSubjectAltNames = matchAllNames;
    }

    /**
     * 设置 subjectAlternativeNames 标准。{@code X509Certificate} 必须包含所有或至少一个
     * 指定的 subjectAlternativeNames，具体取决于 matchAllNames 标志的值
     * （参见 {@link #setMatchAllSubjectAltNames setMatchAllSubjectAltNames}）。
     * <p>
     * 该方法允许调用者通过单个方法调用指定 subjectAlternativeNames 标准的完整集。
     * 指定的值将替换 subjectAlternativeNames 标准的先前值。
     * <p>
     * {@code names} 参数（如果不为 {@code null}）是一个 {@code Collection}，
     * 其中每个条目都是一个 {@code List}，其第一个条目是 {@code Integer}（名称类型，0-8），
     * 第二个条目是 {@code String} 或字节数组（名称，分别为字符串形式或 ASN.1 DER 编码形式）。
     * 可以有多个相同类型的名称。如果为此参数提供 {@code null}，则不执行 subjectAlternativeNames 检查。
     * <p>
     * {@code Collection} 中的每个主体备用名称可以指定为 {@code String} 或 ASN.1 编码的字节数组。
     * 有关使用的格式的更多详细信息，请参见
     * {@link #addSubjectAlternativeName(int type, String name)
     * addSubjectAlternativeName(int type, String name)} 和
     * {@link #addSubjectAlternativeName(int type, byte [] name)
     * addSubjectAlternativeName(int type, byte [] name)}。
     * <p>
     * <strong>注意：</strong>对于可区分名称，应指定字节数组形式而不是字符串形式。
     * 有关更多信息，请参见 {@link #addSubjectAlternativeName(int, String)} 中的注释。
     * <p>
     * 注意，{@code names} 参数可以包含重复的名称（名称和名称类型相同），但它们可能会从
     * {@link #getSubjectAlternativeNames getSubjectAlternativeNames} 方法返回的
     * {@code Collection} 中删除。
     * <p>
     * 注意，将对 {@code Collection} 执行深度复制以防止后续修改。
     *
     * @param names 名称的 {@code Collection}（或 {@code null}）
     * @throws IOException 如果发生解析错误
     * @see #getSubjectAlternativeNames
     */
    public void setSubjectAlternativeNames(Collection<List<?>> names)
            throws IOException {
        if (names == null) {
            subjectAlternativeNames = null;
            subjectAlternativeGeneralNames = null;
        } else {
            if (names.isEmpty()) {
                subjectAlternativeNames = null;
                subjectAlternativeGeneralNames = null;
                return;
            }
            Set<List<?>> tempNames = cloneAndCheckNames(names);
            // 确保这两个都设置或都不设置
            subjectAlternativeGeneralNames = parseNames(tempNames);
            subjectAlternativeNames = tempNames;
        }
    }

                /**
     * 向 subjectAlternativeNames 标准中添加一个名称。根据 matchAllNames 标志的值（参见 {@link #setMatchAllSubjectAltNames
     * setMatchAllSubjectAltNames}），{@code X509Certificate} 必须包含所有或至少一个指定的 subjectAlternativeNames。
     * <p>
     * 此方法允许调用者向 subject alternative names 集合中添加一个名称。
     * 指定的名称将添加到 subjectAlternativeNames 标准的任何先前值。如果指定的名称是重复的，可能会被忽略。
     * <p>
     * 名称以字符串格式提供。
     * <a href="http://www.ietf.org/rfc/rfc822.txt">RFC 822</a>、DNS 和 URI
     * 名称使用这些类型（受 RFC 3280 限制）的既定字符串格式。IPv4 地址名称使用点分四组表示法。OID 地址名称表示为一系列由点分隔的非负整数。并且
     * 目录名称（区分名称）以 RFC 2253 格式提供。
     * 对于 otherNames、X.400 名称、EDI 当事人名称、IPv6 地址名称或任何其他类型的名称，没有定义标准的字符串格式。它们
     * 应使用 {@link #addSubjectAlternativeName(int type, byte [] name)
     * addSubjectAlternativeName(int type, byte [] name)}
     * 方法指定。
     * <p>
     * <strong>注意：</strong>对于区分名称，使用
     * {@linkplain #addSubjectAlternativeName(int, byte[])} 代替。
     * 不应依赖此方法，因为它可能会因为某些区分名称的 RFC 2253 字符串形式中的编码信息丢失而无法匹配某些证书。
     *
     * @param type 名称类型（0-8，如 RFC 3280 第 4.2.1.7 节所指定）
     * @param name 以字符串形式提供的名称（非 {@code null}）
     * @throws IOException 如果发生解析错误
     */
    public void addSubjectAlternativeName(int type, String name)
            throws IOException {
        addSubjectAlternativeNameInternal(type, name);
    }

    /**
     * 向 subjectAlternativeNames 标准中添加一个名称。根据 matchAllNames 标志的值（参见 {@link #setMatchAllSubjectAltNames
     * setMatchAllSubjectAltNames}），{@code X509Certificate} 必须包含所有或至少一个指定的 subjectAlternativeNames。
     * <p>
     * 此方法允许调用者向 subject alternative names 集合中添加一个名称。
     * 指定的名称将添加到 subjectAlternativeNames 标准的任何先前值。如果指定的名称是重复的，可能会被忽略。
     * <p>
     * 名称以字节数组形式提供。此字节数组应包含 RFC 3280 和 X.509 中定义的 GeneralName 结构中出现的 DER 编码名称。编码的字节数组应仅包含名称的编码值，不应包括 GeneralName 结构中与名称关联的标签。此结构的 ASN.1 定义如下。
     * <pre>{@code
     *  GeneralName ::= CHOICE {
     *       otherName                       [0]     OtherName,
     *       rfc822Name                      [1]     IA5String,
     *       dNSName                         [2]     IA5String,
     *       x400Address                     [3]     ORAddress,
     *       directoryName                   [4]     Name,
     *       ediPartyName                    [5]     EDIPartyName,
     *       uniformResourceIdentifier       [6]     IA5String,
     *       iPAddress                       [7]     OCTET STRING,
     *       registeredID                    [8]     OBJECT IDENTIFIER}
     * }</pre>
     * <p>
     * 注意，这里提供的字节数组会被克隆以防止后续修改。
     *
     * @param type 名称类型（0-8，如上所述）
     * @param name 包含以 ASN.1 DER 编码形式的名称的字节数组
     * @throws IOException 如果发生解析错误
     */
    public void addSubjectAlternativeName(int type, byte[] name)
            throws IOException {
        // 克隆，因为字节数组是可修改的
        addSubjectAlternativeNameInternal(type, name.clone());
    }

    /**
     * 一个私有方法，用于向 subjectAlternativeNames 标准中添加一个名称（字符串或字节数组）。{@code X509Certificate}
     * 必须包含指定的 subjectAlternativeName。
     *
     * @param type 名称类型（0-8，如 RFC 3280 第 4.2.1.7 节所指定）
     * @param name 以字符串或字节数组形式提供的名称
     * @throws IOException 如果发生解析错误
     */
    private void addSubjectAlternativeNameInternal(int type, Object name)
            throws IOException {
        // 首先，确保名称可以解析
        GeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (subjectAlternativeNames == null) {
            subjectAlternativeNames = new HashSet<List<?>>();
        }
        if (subjectAlternativeGeneralNames == null) {
            subjectAlternativeGeneralNames = new HashSet<GeneralNameInterface>();
        }
        List<Object> list = new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        subjectAlternativeNames.add(list);
        subjectAlternativeGeneralNames.add(tempName);
    }

    /**
     * 解析传递给 setSubjectAlternativeNames 的形式的参数，返回一个
     * {@code GeneralNameInterface} 的 {@code Collection}。
     * 如果参数格式错误，则抛出 IllegalArgumentException 或 ClassCastException。
     *
     * @param names 每个名称一个条目的集合。
     *              每个条目是一个 {@code List}，其第一个条目
     *              是一个 Integer（名称类型，0-8），第二个条目
     *              是一个字符串或字节数组（名称，分别以字符串或 ASN.1 DER 编码形式）。
     *              可以有多个相同类型的名称。不允许为 null。
     * @return 一个 {@code GeneralNameInterface} 的集合
     * @throws IOException 如果发生解析错误
     */
    private static Set<GeneralNameInterface> parseNames(Collection<List<?>> names) throws IOException {
        Set<GeneralNameInterface> genNames = new HashSet<GeneralNameInterface>();
        for (List<?> nameList : names) {
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o =  nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            int nameType = ((Integer)o).intValue();
            o = nameList.get(1);
            genNames.add(makeGeneralNameInterface(nameType, o));
        }


                    return genNames;
    }

    /**
     * 比较两个以 setSubjectAlternativeNames（或 X509CRLSelector.setIssuerNames）形式传递的对象是否相等。
     * 如果其中一个对象格式不正确，则抛出 {@code IllegalArgumentException} 或 {@code ClassCastException}。
     *
     * @param object1 包含要比较的第一个对象的 Collection
     * @param object2 包含要比较的第二个对象的 Collection
     * @return 如果对象相等则返回 true，否则返回 false
     */
    static boolean equalNames(Collection<?> object1, Collection<?> object2) {
        if ((object1 == null) || (object2 == null)) {
            return object1 == object2;
        }
        return object1.equals(object2);
    }

    /**
     * 从名称类型（0-8）和一个可能是包含 ASN.1 DER 编码名称的字节数组或名称的字符串形式的对象中创建一个 {@code GeneralNameInterface}。
     * 除了 X.509 区分名称外，名称的字符串形式不能是现有实现 GeneralNameInterface 类的 toString 方法的结果。
     * 除区分名称外，其他名称的 toString 输出与字符串构造函数不兼容。
     *
     * @param type 名称类型（0-8）
     * @param name 名称，可以是 ASN.1 Der 编码的字节数组或字符串
     * @return 一个 GeneralNameInterface 名称
     * @throws IOException 如果解析错误发生
     */
    static GeneralNameInterface makeGeneralNameInterface(int type, Object name)
            throws IOException {
        GeneralNameInterface result;
        if (debug != null) {
            debug.println("X509CertSelector.makeGeneralNameInterface("
                + type + ")...");
        }

        if (name instanceof String) {
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() "
                    + "name is String: " + name);
            }
            switch (type) {
            case NAME_RFC822:
                result = new RFC822Name((String)name);
                break;
            case NAME_DNS:
                result = new DNSName((String)name);
                break;
            case NAME_DIRECTORY:
                result = new X500Name((String)name);
                break;
            case NAME_URI:
                result = new URIName((String)name);
                break;
            case NAME_IP:
                result = new IPAddressName((String)name);
                break;
            case NAME_OID:
                result = new OIDName((String)name);
                break;
            default:
                throw new IOException("unable to parse String names of type "
                                      + type);
            }
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() "
                    + "result: " + result.toString());
            }
        } else if (name instanceof byte[]) {
            DerValue val = new DerValue((byte[]) name);
            if (debug != null) {
                debug.println
                    ("X509CertSelector.makeGeneralNameInterface() is byte[]");
            }

            switch (type) {
            case NAME_ANY:
                result = new OtherName(val);
                break;
            case NAME_RFC822:
                result = new RFC822Name(val);
                break;
            case NAME_DNS:
                result = new DNSName(val);
                break;
            case NAME_X400:
                result = new X400Address(val);
                break;
            case NAME_DIRECTORY:
                result = new X500Name(val);
                break;
            case NAME_EDI:
                result = new EDIPartyName(val);
                break;
            case NAME_URI:
                result = new URIName(val);
                break;
            case NAME_IP:
                result = new IPAddressName(val);
                break;
            case NAME_OID:
                result = new OIDName(val);
                break;
            default:
                throw new IOException("unable to parse byte array names of "
                    + "type " + type);
            }
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() result: "
                    + result.toString());
            }
        } else {
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralName() input name "
                    + "not String or byte array");
            }
            throw new IOException("name not String or byte array");
        }
        return result;
    }


    /**
     * 设置名称约束标准。{@code X509Certificate} 必须具有满足指定名称约束的主体和主体备用名称。
     * <p>
     * 名称约束以字节数组的形式指定。此字节数组应包含名称约束的 DER 编码形式，如 RFC 3280 和 X.509 中定义的 NameConstraints 结构所示。
     * 下面显示了此结构的 ASN.1 定义。
     *
     * <pre>{@code
     *  NameConstraints ::= SEQUENCE {
     *       permittedSubtrees       [0]     GeneralSubtrees OPTIONAL,
     *       excludedSubtrees        [1]     GeneralSubtrees OPTIONAL }
     *
     *  GeneralSubtrees ::= SEQUENCE SIZE (1..MAX) OF GeneralSubtree
     *
     *  GeneralSubtree ::= SEQUENCE {
     *       base                    GeneralName,
     *       minimum         [0]     BaseDistance DEFAULT 0,
     *       maximum         [1]     BaseDistance OPTIONAL }
     *
     *  BaseDistance ::= INTEGER (0..MAX)
     *
     *  GeneralName ::= CHOICE {
     *       otherName                       [0]     OtherName,
     *       rfc822Name                      [1]     IA5String,
     *       dNSName                         [2]     IA5String,
     *       x400Address                     [3]     ORAddress,
     *       directoryName                   [4]     Name,
     *       ediPartyName                    [5]     EDIPartyName,
     *       uniformResourceIdentifier       [6]     IA5String,
     *       iPAddress                       [7]     OCTET STRING,
     *       registeredID                    [8]     OBJECT IDENTIFIER}
     * }</pre>
     * <p>
     * 注意，这里提供的字节数组会被克隆以防止后续修改。
     *
     * @param bytes 包含用于检查名称约束的 NameConstraints 扩展的 ASN.1 DER 编码的字节数组。
     *              仅包括扩展的值，不包括 OID 或关键性标志。可以是 {@code null}，
     *              在这种情况下，不会执行名称约束检查。
     * @throws IOException 如果解析错误发生
     * @see #getNameConstraints
     */
    public void setNameConstraints(byte[] bytes) throws IOException {
        if (bytes == null) {
            ncBytes = null;
            nc = null;
        } else {
            ncBytes = bytes.clone();
            nc = new NameConstraintsExtension(FALSE, bytes);
        }
    }

                /**
     * 设置基本约束条件。如果值大于或等于零，则 {@code X509Certificates} 必须包含一个
     * 基本约束扩展，其路径长度至少为该值。如果值为 -2，则仅接受终端实体证书。如果值为 -1，则不进行检查。
     * <p>
     * 当从目标向信任锚点构建认证路径时，此约束条件非常有用。如果已构建了部分路径，则任何候选证书的 maxPathLen 值必须大于或等于部分路径中的证书数量。
     *
     * @param minMaxPathLen 基本约束条件的值
     * @throws IllegalArgumentException 如果值小于 -2
     * @see #getBasicConstraints
     */
    public void setBasicConstraints(int minMaxPathLen) {
        if (minMaxPathLen < -2) {
            throw new IllegalArgumentException("基本约束小于 -2");
        }
        basicConstraints = minMaxPathLen;
    }

    /**
     * 设置策略约束条件。{@code X509Certificate} 必须在其证书策略扩展中包含指定的策略之一。
     * 如果 {@code certPolicySet} 为空，则 {@code X509Certificate} 必须在其证书策略扩展中包含至少一个指定的策略。
     * 如果 {@code certPolicySet} 为 {@code null}，则不进行策略检查。
     * <p>
     * 注意，{@code Set} 被克隆以防止后续修改。
     *
     * @param certPolicySet 证书策略 OID 的 {@code Set}（或 {@code null}）。每个 OID 由一系列非负整数表示，用点分隔。
     * @throws IOException 如果解析 OID 时发生错误，例如第一个组件不是 0、1 或 2，或者第二个组件大于 39。
     * @see #getPolicy
     */
    public void setPolicy(Set<String> certPolicySet) throws IOException {
        if (certPolicySet == null) {
            policySet = null;
            policy = null;
        } else {
            // 快照集并解析
            Set<String> tempSet = Collections.unmodifiableSet
                                        (new HashSet<String>(certPolicySet));
            /* 转换为 ObjectIdentifiers 的 Vector */
            Iterator<String> i = tempSet.iterator();
            Vector<CertificatePolicyId> polIdVector = new Vector<CertificatePolicyId>();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof String)) {
                    throw new IOException("certPolicySet 中存在非 String 类型");
                }
                polIdVector.add(new CertificatePolicyId(new ObjectIdentifier(
                  (String)o)));
            }
            // 如果一切顺利，进行更改
            policySet = tempSet;
            policy = new CertificatePolicySet(polIdVector);
        }
    }

    /**
     * 设置路径到名称的标准。{@code X509Certificate} 不得包含会阻止构建到指定名称的路径的名称约束。
     * <p>
     * 该方法允许调用者通过单个方法调用指定 {@code X509Certificates} 的名称约束必须允许的完整名称集。
     * 指定的值将替换路径到名称标准的先前值。
     * <p>
     * 当从目标向信任锚点构建认证路径时，此约束条件非常有用。如果已构建了部分路径，则任何候选证书不得包含会阻止构建到部分路径中任何名称的路径的名称约束。
     * <p>
     * {@code names} 参数（如果不为 {@code null}）是一个 {@code Collection}，每个名称对应一个条目。
     * 每个条目是一个 {@code List}，其第一个条目是一个 {@code Integer}（名称类型，0-8），第二个条目是一个 {@code String} 或字节数组（名称，分别以字符串或 ASN.1 DER 编码形式表示）。
     * 可以有多个相同类型的名称。如果此参数的值为 {@code null}，则不进行路径到名称检查。
     * <p>
     * {@code Collection} 中的每个名称可以指定为 {@code String} 或 ASN.1 编码的字节数组。有关使用的格式的更多详细信息，请参见
     * {@link #addPathToName(int type, String name)
     * addPathToName(int type, String name)} 和
     * {@link #addPathToName(int type, byte [] name)
     * addPathToName(int type, byte [] name)}。
     * <p>
     * <strong>注意：</strong> 对于可分辨名称，应指定字节数组形式而不是字符串形式。有关更多信息，请参见
     * {@link #addPathToName(int, String)} 中的说明。
     * <p>
     * 注意，{@code names} 参数可以包含重复的名称（名称和名称类型相同），但它们可能会从
     * {@link #getPathToNames getPathToNames} 方法返回的名称 {@code Collection} 中移除。
     * <p>
     * 注意，对 {@code Collection} 进行了深度复制以防止后续修改。
     *
     * @param names 每个名称对应一个条目的 {@code Collection}（或 {@code null}）
     * @throws IOException 如果解析时发生错误
     * @see #getPathToNames
     */
    public void setPathToNames(Collection<List<?>> names) throws IOException {
        if ((names == null) || names.isEmpty()) {
            pathToNames = null;
            pathToGeneralNames = null;
        } else {
            Set<List<?>> tempNames = cloneAndCheckNames(names);
            pathToGeneralNames = parseNames(tempNames);
            // 确保这两个都设置或都不设置
            pathToNames = tempNames;
        }
    }

                // 调用自 CertPathHelper
    void setPathToNamesInternal(Set<GeneralNameInterface> names) {
        // 将 names 设置为非空的虚拟值
        // 这将破坏 getPathToNames()
        pathToNames = Collections.<List<?>>emptySet();
        pathToGeneralNames = names;
    }

    /**
     * 向 pathToNames 标准中添加一个名称。{@code X509Certificate}
     * 不得包含会禁止构建到指定名称路径的名称约束。
     * <p>
     * 该方法允许调用者向 {@code X509Certificates} 的名称约束必须允许的名称集合中添加一个名称。
     * 指定的名称将添加到 pathToNames 标准的任何先前值。如果名称是重复的，可以忽略。
     * <p>
     * 名称以字符串格式提供。RFC 822、DNS 和 URI 名称使用这些类型（受 RFC 3280 限制）的既定字符串格式。
     * IPv4 地址名称使用点分十进制表示。OID 地址名称表示为一系列由点分隔的非负整数。
     * 目录名称（可分辨名称）以 RFC 2253 格式提供。对于 otherNames、X.400 名称、EDI 方式名称、IPv6 地址名称或其他类型的名称，没有定义标准的字符串格式。
     * 应使用 {@link #addPathToName(int type, byte [] name)
     * addPathToName(int type, byte [] name)} 方法指定。
     * <p>
     * <strong>注意：</strong> 对于可分辨名称，请使用
     * {@linkplain #addPathToName(int, byte[])}。
     * 不应依赖此方法，因为它可能会因某些可分辨名称的 RFC 2253 字符串形式中的编码信息丢失而无法匹配某些证书。
     *
     * @param type 名称类型（0-8，如 RFC 3280 第 4.2.1.7 节所指定）
     * @param name 字符串形式的名称
     * @throws IOException 如果发生解析错误
     */
    public void addPathToName(int type, String name) throws IOException {
        addPathToNameInternal(type, name);
    }

    /**
     * 向 pathToNames 标准中添加一个名称。{@code X509Certificate}
     * 不得包含会禁止构建到指定名称路径的名称约束。
     * <p>
     * 该方法允许调用者向 {@code X509Certificates} 的名称约束必须允许的名称集合中添加一个名称。
     * 指定的名称将添加到 pathToNames 标准的任何先前值。如果名称是重复的，可以忽略。
     * <p>
     * 名称以字节数组的形式提供。该字节数组应包含如 RFC 3280 和 X.509 中定义的 GeneralName 结构中出现的 DER 编码名称。
     * 该结构的 ASN.1 定义出现在
     * {@link #addSubjectAlternativeName(int type, byte [] name)
     * addSubjectAlternativeName(int type, byte [] name)} 的文档中。
     * <p>
     * 注意，这里提供的字节数组被克隆以防止后续修改。
     *
     * @param type 名称类型（0-8，如 RFC 3280 第 4.2.1.7 节所指定）
     * @param name 包含 ASN.1 DER 编码形式名称的字节数组
     * @throws IOException 如果发生解析错误
     */
    public void addPathToName(int type, byte [] name) throws IOException {
        // 克隆，因为字节数组是可修改的
        addPathToNameInternal(type, name.clone());
    }

    /**
     * 一个私有方法，向 pathToNames 标准中添加一个名称（字符串或字节数组）。{@code X509Certificate} 必须包含指定的 pathToName。
     *
     * @param type 名称类型（0-8，如 RFC 3280 第 4.2.1.7 节所指定）
     * @param name 字符串或字节数组形式的名称
     * @throws IOException 如果发生编码错误（DN 形式不正确）
     */
    private void addPathToNameInternal(int type, Object name)
            throws IOException {
        // 首先，确保名称解析正确
        GeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (pathToGeneralNames == null) {
            pathToNames = new HashSet<List<?>>();
            pathToGeneralNames = new HashSet<GeneralNameInterface>();
        }
        List<Object> list = new ArrayList<Object>(2);
        list.add(Integer.valueOf(type));
        list.add(name);
        pathToNames.add(list);
        pathToGeneralNames.add(tempName);
    }

    /**
     * 返回 certificateEquals 标准。指定的
     * {@code X509Certificate} 必须等于传递给 {@code match} 方法的
     * {@code X509Certificate}。如果为 {@code null}，则不应用此检查。
     *
     * @return 要匹配的 {@code X509Certificate}（或 {@code null}）
     * @see #setCertificate
     */
    public X509Certificate getCertificate() {
        return x509Cert;
    }

    /**
     * 返回 serialNumber 标准。指定的序列号
     * 必须与 {@code X509Certificate} 中的证书序列号匹配。如果为 {@code null}，则任何证书序列号都可以。
     *
     * @return 要匹配的证书序列号
     *                （或 {@code null}）
     * @see #setSerialNumber
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * 以 {@code X500Principal} 的形式返回发行者标准。此
     * 可分辨名称必须与 {@code X509Certificate} 中的发行者可分辨名称匹配。如果为 {@code null}，则发行者标准被禁用，任何发行者可分辨名称都可以。
     *
     * @return 所需的发行者可分辨名称作为 X500Principal
     *         （或 {@code null}）
     * @since 1.5
     */
    public X500Principal getIssuer() {
        return issuer;
    }

    /**
     * <strong>不推荐使用</strong>，请使用 {@linkplain #getIssuer()} 或
     * {@linkplain #getIssuerAsBytes()} 代替。不应依赖此方法，因为它可能会因某些可分辨名称的 RFC 2253 字符串形式中的编码信息丢失而无法匹配某些证书。
     * <p>
     * 以 {@code String} 的形式返回发行者标准。此
     * 可分辨名称必须与 {@code X509Certificate} 中的发行者可分辨名称匹配。如果为 {@code null}，则发行者标准被禁用，任何发行者可分辨名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个
     * 可分辨名称，以 RFC 2253 格式表示。
     *
     * @return 所需的发行者可分辨名称以 RFC 2253 格式
     *         （或 {@code null}）
     */
    public String getIssuerAsString() {
        return (issuer == null ? null : issuer.getName());
    }


                /**
     * 返回发行者标准作为字节数组。此可区分名称必须与
     * {@code X509Certificate} 中的发行者可区分名称匹配。如果为 {@code null}，则发行者标准
     * 被禁用，任何发行者可区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个包含单个 DER 编码可区分名称的字节数组，如
     * X.501 中定义。此结构的 ASN.1 表示形式在
     * {@link #setIssuer(byte [] issuerDN) setIssuer(byte [] issuerDN)} 的文档中提供。
     * <p>
     * 请注意，返回的字节数组被克隆以防止后续修改。
     *
     * @return 包含所需发行者可区分名称的字节数组
     *         （或 {@code null}）
     * @throws IOException 如果发生编码错误
     */
    public byte[] getIssuerAsBytes() throws IOException {
        return (issuer == null ? null: issuer.getEncoded());
    }

    /**
     * 返回主体标准作为 {@code X500Principal}。此可区分名称必须与
     * {@code X509Certificate} 中的主体可区分名称匹配。如果为 {@code null}，则主体标准
     * 被禁用，任何主体可区分名称都可以。
     *
     * @return 作为 X500Principal 的所需主体可区分名称
     *         （或 {@code null}）
     * @since 1.5
     */
    public X500Principal getSubject() {
        return subject;
    }

    /**
     * <strong>不推荐使用</strong>，请改用 {@linkplain #getSubject()} 或
     * {@linkplain #getSubjectAsBytes()}。不应依赖此方法，因为它可能会因为某些可区分名称的
     * RFC 2253 字符串形式中编码信息的丢失而无法匹配某些证书。
     * <p>
     * 返回主体标准作为 {@code String}。此可区分名称必须与
     * {@code X509Certificate} 中的主体可区分名称匹配。如果为 {@code null}，则主体标准
     * 被禁用，任何主体可区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个
     * RFC 2253 格式的可区分名称。
     *
     * @return 以 RFC 2253 格式表示的所需主体可区分名称
     *         （或 {@code null}）
     */
    public String getSubjectAsString() {
        return (subject == null ? null : subject.getName());
    }

    /**
     * 返回主体标准作为字节数组。此可区分名称必须与
     * {@code X509Certificate} 中的主体可区分名称匹配。如果为 {@code null}，则主体标准
     * 被禁用，任何主体可区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个包含单个 DER 编码可区分名称的字节数组，如
     * X.501 中定义。此结构的 ASN.1 表示形式在
     * {@link #setSubject(byte [] subjectDN) setSubject(byte [] subjectDN)} 的文档中提供。
     * <p>
     * 请注意，返回的字节数组被克隆以防止后续修改。
     *
     * @return 包含所需主体可区分名称的字节数组
     *         （或 {@code null}）
     * @throws IOException 如果发生编码错误
     */
    public byte[] getSubjectAsBytes() throws IOException {
        return (subject == null ? null : subject.getEncoded());
    }

    /**
     * 返回 subjectKeyIdentifier 标准。指定的 {@code X509Certificate} 必须包含一个
     * SubjectKeyIdentifier 扩展，其值为指定值。如果为 {@code null}，则不进行
     * subjectKeyIdentifier 检查。
     * <p>
     * 请注意，返回的字节数组被克隆以防止后续修改。
     *
     * @return 密钥标识符（或 {@code null}）
     * @see #setSubjectKeyIdentifier
     */
    public byte[] getSubjectKeyIdentifier() {
        if (subjectKeyID == null) {
            return null;
        }
        return subjectKeyID.clone();
    }

    /**
     * 返回 authorityKeyIdentifier 标准。指定的 {@code X509Certificate} 必须包含一个
     * AuthorityKeyIdentifier 扩展，其值为指定值。如果为 {@code null}，则不进行
     * authorityKeyIdentifier 检查。
     * <p>
     * 请注意，返回的字节数组被克隆以防止后续修改。
     *
     * @return 密钥标识符（或 {@code null}）
     * @see #setAuthorityKeyIdentifier
     */
    public byte[] getAuthorityKeyIdentifier() {
        if (authorityKeyID == null) {
          return null;
        }
        return authorityKeyID.clone();
    }

    /**
     * 返回 certificateValid 标准。指定的日期必须在
     * {@code X509Certificate} 的证书有效期内。如果为 {@code null}，则不进行
     * certificateValid 检查。
     * <p>
     * 请注意，返回的 {@code Date} 被克隆以防止后续修改。
     *
     * @return 要检查的 {@code Date}（或 {@code null}）
     * @see #setCertificateValid
     */
    public Date getCertificateValid() {
        if (certificateValid == null) {
            return null;
        }
        return (Date)certificateValid.clone();
    }

    /**
     * 返回 privateKeyValid 标准。指定的日期必须在
     * {@code X509Certificate} 的私钥有效期内。如果为 {@code null}，则不进行
     * privateKeyValid 检查。
     * <p>
     * 请注意，返回的 {@code Date} 被克隆以防止后续修改。
     *
     * @return 要检查的 {@code Date}（或 {@code null}）
     * @see #setPrivateKeyValid
     */
    public Date getPrivateKeyValid() {
        if (privateKeyValid == null) {
            return null;
        }
        return (Date)privateKeyValid.clone();
    }

    /**
     * 返回 subjectPublicKeyAlgID 标准。指定的 {@code X509Certificate} 必须包含一个
     * 具有指定算法的主体公钥。如果为 {@code null}，则不进行
     * subjectPublicKeyAlgID 检查。
     *
     * @return 要检查的签名算法的对象标识符（OID）（或 {@code null}）。OID 由一系列
     *         以点分隔的非负整数组成。
     * @see #setSubjectPublicKeyAlgID
     */
    public String getSubjectPublicKeyAlgID() {
        if (subjectPublicKeyAlgID == null) {
            return null;
        }
        return subjectPublicKeyAlgID.toString();
    }


                /**
     * 返回 subjectPublicKey 标准。该 {@code X509Certificate} 必须包含指定的主体公钥。
     * 如果为 {@code null}，则不执行主体公钥检查。
     *
     * @return 要检查的主体公钥（或 {@code null}）
     * @see #setSubjectPublicKey
     */
    public PublicKey getSubjectPublicKey() {
        return subjectPublicKey;
    }

    /**
     * 返回 keyUsage 标准。该 {@code X509Certificate} 必须允许指定的 keyUsage 值。
     * 如果为 null，则不执行 keyUsage 检查。
     * <p>
     * 注意，返回的布尔数组是克隆的，以防止后续修改。
     *
     * @return 与 {@link X509Certificate#getKeyUsage() X509Certificate.getKeyUsage()}
     * 返回的布尔数组格式相同的布尔数组。或 {@code null}。
     * @see #setKeyUsage
     */
    public boolean[] getKeyUsage() {
        if (keyUsage == null) {
            return null;
        }
        return keyUsage.clone();
    }

    /**
     * 返回 extendedKeyUsage 标准。该 {@code X509Certificate} 必须在其扩展密钥用法扩展中允许指定的密钥用途。
     * 如果返回的 {@code keyPurposeSet} 为空或 {@code null}，则不执行 extendedKeyUsage 检查。
     * 注意，没有 extendedKeyUsage 扩展的 {@code X509Certificate} 隐式允许所有密钥用途。
     *
     * @return 以字符串格式表示的密钥用途 OID 的不可变 {@code Set}（或 {@code null}）
     * @see #setExtendedKeyUsage
     */
    public Set<String> getExtendedKeyUsage() {
        return keyPurposeSet;
    }

    /**
     * 指示 {@code X509Certificate} 是否必须包含所有或至少一个在
     * {@link #setSubjectAlternativeNames setSubjectAlternativeNames} 或
     * {@link #addSubjectAlternativeName addSubjectAlternativeName} 方法中指定的 subjectAlternativeNames。
     * 如果为 {@code true}，则 {@code X509Certificate} 必须包含所有指定的主体备用名称。
     * 如果为 {@code false}，则 {@code X509Certificate} 必须包含至少一个指定的主体备用名称。
     *
     * @return 如果标志启用则返回 {@code true}；如果标志禁用则返回 {@code false}。默认情况下，该标志为 {@code true}。
     * @see #setMatchAllSubjectAltNames
     */
    public boolean getMatchAllSubjectAltNames() {
        return matchAllSubjectAltNames;
    }

    /**
     * 返回 subjectAlternativeNames 标准的副本。根据 matchAllNames 标志的值（参见
     * {@link #getMatchAllSubjectAltNames getMatchAllSubjectAltNames}），该 {@code X509Certificate}
     * 必须包含所有或至少一个指定的 subjectAlternativeNames。如果返回的值为 {@code null}，
     * 则不执行 subjectAlternativeNames 检查。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个 {@code Collection}，其中每个名称包含一个条目。
     * 每个条目是一个 {@code List}，其第一个条目是一个 {@code Integer}（名称类型，0-8），第二个条目是一个
     * {@code String} 或字节数组（名称，分别为字符串或 ASN.1 DER 编码形式）。
     * 可以有多个相同类型的名称。注意，返回的 {@code Collection} 可能包含重复的名称（相同的名称和名称类型）。
     * <p>
     * {@code Collection} 中的每个主体备用名称可以指定为 {@code String} 或 ASN.1 编码的字节数组。
     * 有关使用的格式的更多详细信息，请参见
     * {@link #addSubjectAlternativeName(int type, String name) addSubjectAlternativeName(int type, String name)}
     * 和
     * {@link #addSubjectAlternativeName(int type, byte [] name) addSubjectAlternativeName(int type, byte [] name)}。
     * <p>
     * 注意，对 {@code Collection} 执行深度复制以防止后续修改。
     *
     * @return 名称的 {@code Collection}（或 {@code null}）
     * @see #setSubjectAlternativeNames
     */
    public Collection<List<?>> getSubjectAlternativeNames() {
        if (subjectAlternativeNames == null) {
            return null;
        }
        return cloneNames(subjectAlternativeNames);
    }

    /**
     * 克隆传递给 setSubjectAlternativeNames 和 setPathToNames 的形式的对象。
     * 如果参数格式不正确，则抛出 {@code RuntimeException}。
     * <p>
     * 此方法包装了 cloneAndCheckNames，将任何 {@code IOException} 转换为 {@code RuntimeException}。
     * 当克隆的对象已经被检查过，因此不应该有任何异常时，应使用此方法。
     *
     * @param names 每个名称包含一个条目的 {@code Collection}。
     *              每个条目是一个 {@code List}，其第一个条目是一个 Integer（名称类型，0-8），第二个条目是一个
     *              String 或字节数组（名称，分别为字符串或 ASN.1 DER 编码形式）。
     *              可以有多个相同类型的名称。不允许为 null。
     * @return 指定的 {@code Collection} 的深度副本
     * @throws RuntimeException 如果发生解析错误
     */
    private static Set<List<?>> cloneNames(Collection<List<?>> names) {
        try {
            return cloneAndCheckNames(names);
        } catch (IOException e) {
            throw new RuntimeException("cloneNames encountered IOException: " +
                                       e.getMessage());
        }
    }

    /**
     * 克隆并检查传递给 setSubjectAlternativeNames 和 setPathToNames 的形式的参数。
     * 如果参数格式不正确，则抛出 {@code IOException}。
     *
     * @param names 每个名称包含一个条目的 {@code Collection}。
     *              每个条目是一个 {@code List}，其第一个条目是一个 Integer（名称类型，0-8），第二个条目是一个
     *              String 或字节数组（名称，分别为字符串或 ASN.1 DER 编码形式）。
     *              可以有多个相同类型的名称。不允许为 null。
     * @return 指定的 {@code Collection} 的深度副本
     * @throws IOException 如果发生解析错误
     */
    private static Set<List<?>> cloneAndCheckNames(Collection<List<?>> names) throws IOException {
        // 复制 Lists 和 Collection
        Set<List<?>> namesCopy = new HashSet<List<?>>();
        for (List<?> o : names)
        {
            namesCopy.add(new ArrayList<Object>(o));
        }


                    // 检查列表内容并克隆任何字节数组
        for (List<?> list : namesCopy) {
            @SuppressWarnings("unchecked") // 请参阅参数 "names" 的 javadoc。
            List<Object> nameList = (List<Object>)list;
            if (nameList.size() != 2) {
                throw new IOException("name list size not 2");
            }
            Object o = nameList.get(0);
            if (!(o instanceof Integer)) {
                throw new IOException("expected an Integer");
            }
            int nameType = ((Integer)o).intValue();
            if ((nameType < 0) || (nameType > 8)) {
                throw new IOException("name type not 0-8");
            }
            Object nameObject = nameList.get(1);
            if (!(nameObject instanceof byte[]) &&
                !(nameObject instanceof String)) {
                if (debug != null) {
                    debug.println("X509CertSelector.cloneAndCheckNames() "
                        + "name not byte array");
                }
                throw new IOException("name not byte array or String");
            }
            if (nameObject instanceof byte[]) {
                nameList.set(1, ((byte[]) nameObject).clone());
            }
        }
        return namesCopy;
    }

    /**
     * 返回名称约束标准。{@code X509Certificate} 必须具有满足指定名称约束的主体和主体备用名称。
     * <p>
     * 名称约束以字节数组的形式返回。此字节数组包含名称约束的 DER 编码形式，如 RFC 3280 和 X.509 中定义的 NameConstraints 结构所示。
     * ASN.1 表示法在 {@link #setNameConstraints(byte [] bytes) setNameConstraints(byte [] bytes)} 的文档中提供。
     * <p>
     * 注意返回的字节数组是克隆的，以防止后续修改。
     *
     * @return 包含用于检查名称约束的 NameConstraints 扩展的 ASN.1 DER 编码的字节数组。
     *         如果不执行名称约束检查，则返回 {@code null}。
     * @see #setNameConstraints
     */
    public byte[] getNameConstraints() {
        if (ncBytes == null) {
            return null;
        } else {
            return ncBytes.clone();
        }
    }

    /**
     * 返回基本约束标准。如果值大于或等于零，则 {@code X509Certificates} 必须包含一个路径长度至少为该值的基本约束扩展。
     * 如果值为 -2，则仅接受终端实体证书。如果值为 -1，则不执行基本约束检查。
     *
     * @return 基本约束标准的值
     * @see #setBasicConstraints
     */
    public int getBasicConstraints() {
        return basicConstraints;
    }

    /**
     * 返回策略标准。{@code X509Certificate} 必须在其证书策略扩展中包含至少一个指定的策略。
     * 如果返回的 {@code Set} 为空，则 {@code X509Certificate} 必须在其证书策略扩展中包含至少一些指定的策略。
     * 如果返回的 {@code Set} 为 {@code null}，则不执行策略检查。
     *
     * @return 证书策略 OID 的不可变 {@code Set}（或 {@code null}）
     * @see #setPolicy
     */
    public Set<String> getPolicy() {
        return policySet;
    }

    /**
     * 返回路径到名称标准的副本。{@code X509Certificate} 不得包含会阻止构建到指定名称的路径的名称约束。
     * 如果返回的值为 {@code null}，则不执行路径到名称检查。
     * <p>
     * 如果返回的值不为 {@code null}，则它是一个 {@code Collection}，其中每个名称对应一个条目。
     * 每个条目是一个 {@code List}，其第一个条目是 {@code Integer}（名称类型，0-8），第二个条目是 {@code String} 或字节数组（名称，分别为字符串形式或 ASN.1 DER 编码形式）。
     * 可以有多个相同类型的名称。注意返回的 {@code Collection} 可能包含重复的名称（相同的名称和名称类型）。
     * <p>
     * {@code Collection} 中的每个名称可以指定为 {@code String} 或 ASN.1 编码的字节数组。
     * 有关使用的格式的更多详细信息，请参阅 {@link #addPathToName(int type, String name)
     * addPathToName(int type, String name)} 和 {@link #addPathToName(int type, byte [] name)
     * addPathToName(int type, byte [] name)}。
     * <p>
     * 注意对 {@code Collection} 执行了深拷贝，以防止后续修改。
     *
     * @return 名称的 {@code Collection}（或 {@code null}）
     * @see #setPathToNames
     */
    public Collection<List<?>> getPathToNames() {
        if (pathToNames == null) {
            return null;
        }
        return cloneNames(pathToNames);
    }

    /**
     * 返回 {@code CertSelector} 的可打印表示形式。
     *
     * @return 描述 {@code CertSelector} 内容的 {@code String}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X509CertSelector: [\n");
        if (x509Cert != null) {
            sb.append("  Certificate: " + x509Cert.toString() + "\n");
        }
        if (serialNumber != null) {
            sb.append("  Serial Number: " + serialNumber.toString() + "\n");
        }
        if (issuer != null) {
            sb.append("  Issuer: " + getIssuerAsString() + "\n");
        }
        if (subject != null) {
            sb.append("  Subject: " + getSubjectAsString() + "\n");
        }
        sb.append("  matchAllSubjectAltNames flag: "
                  + String.valueOf(matchAllSubjectAltNames) + "\n");
        if (subjectAlternativeNames != null) {
            sb.append("  SubjectAlternativeNames:\n");
            Iterator<List<?>> i = subjectAlternativeNames.iterator();
            while (i.hasNext()) {
                List<?> list = i.next();
                sb.append("    type " + list.get(0) +
                          ", name " + list.get(1) + "\n");
            }
        }
        if (subjectKeyID != null) {
            HexDumpEncoder enc = new HexDumpEncoder();
            sb.append("  Subject Key Identifier: " +
                      enc.encodeBuffer(subjectKeyID) + "\n");
        }
        if (authorityKeyID != null) {
            HexDumpEncoder enc = new HexDumpEncoder();
            sb.append("  Authority Key Identifier: " +
                      enc.encodeBuffer(authorityKeyID) + "\n");
        }
        if (certificateValid != null) {
            sb.append("  Certificate Valid: " +
                      certificateValid.toString() + "\n");
        }
        if (privateKeyValid != null) {
            sb.append("  Private Key Valid: " +
                      privateKeyValid.toString() + "\n");
        }
        if (subjectPublicKeyAlgID != null) {
            sb.append("  Subject Public Key AlgID: " +
                      subjectPublicKeyAlgID.toString() + "\n");
        }
        if (subjectPublicKey != null) {
            sb.append("  Subject Public Key: " +
                      subjectPublicKey.toString() + "\n");
        }
        if (keyUsage != null) {
            sb.append("  Key Usage: " + keyUsageToString(keyUsage) + "\n");
        }
        if (keyPurposeSet != null) {
            sb.append("  Extended Key Usage: " +
                      keyPurposeSet.toString() + "\n");
        }
        if (policy != null) {
            sb.append("  Policy: " + policy.toString() + "\n");
        }
        if (pathToGeneralNames != null) {
            sb.append("  Path to names:\n");
            Iterator<GeneralNameInterface> i = pathToGeneralNames.iterator();
            while (i.hasNext()) {
                sb.append("    " + i.next() + "\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

                // 从 sun.security.x509.KeyUsageExtension 复制
    // （不调用超类）
    /**
     * 返回 KeyUsage 的可打印表示形式。
     */
    private static String keyUsageToString(boolean[] k) {
        String s = "KeyUsage [\n";
        try {
            if (k[0]) {
                s += "  DigitalSignature\n";
            }
            if (k[1]) {
                s += "  Non_repudiation\n";
            }
            if (k[2]) {
                s += "  Key_Encipherment\n";
            }
            if (k[3]) {
                s += "  Data_Encipherment\n";
            }
            if (k[4]) {
                s += "  Key_Agreement\n";
            }
            if (k[5]) {
                s += "  Key_CertSign\n";
            }
            if (k[6]) {
                s += "  Crl_Sign\n";
            }
            if (k[7]) {
                s += "  Encipher_Only\n";
            }
            if (k[8]) {
                s += "  Decipher_Only\n";
            }
        } catch (ArrayIndexOutOfBoundsException ex) {}

        s += "]\n";

        return (s);
    }

    /**
     * 给定任何 X509Certificate 和扩展 oid，返回一个 Extension 对象。
     * 如果扩展字节值格式不正确，则抛出 {@code IOException}。
     *
     * @param cert 一个 {@code X509Certificate}
     * @param extId 一个 {@code integer}，指定扩展索引。
     * 当前支持的扩展如下：
     * 索引 0 - PrivateKeyUsageExtension
     * 索引 1 - SubjectAlternativeNameExtension
     * 索引 2 - NameConstraintsExtension
     * 索引 3 - CertificatePoliciesExtension
     * 索引 4 - ExtendedKeyUsageExtension
     * @return 一个 {@code Extension} 对象，其实际类型由扩展 oid 指定。
     * @throws IOException 如果无法使用从传递的 {@code X509Certificate} 中检索到的扩展编码构造 {@code Extension} 对象。
     */
    private static Extension getExtensionObject(X509Certificate cert, int extId)
            throws IOException {
        if (cert instanceof X509CertImpl) {
            X509CertImpl impl = (X509CertImpl)cert;
            switch (extId) {
            case PRIVATE_KEY_USAGE_ID:
                return impl.getPrivateKeyUsageExtension();
            case SUBJECT_ALT_NAME_ID:
                return impl.getSubjectAlternativeNameExtension();
            case NAME_CONSTRAINTS_ID:
                return impl.getNameConstraintsExtension();
            case CERT_POLICIES_ID:
                return impl.getCertificatePoliciesExtension();
            case EXTENDED_KEY_USAGE_ID:
                return impl.getExtendedKeyUsageExtension();
            default:
                return null;
            }
        }
        byte[] rawExtVal = cert.getExtensionValue(EXTENSION_OIDS[extId]);
        if (rawExtVal == null) {
            return null;
        }
        DerInputStream in = new DerInputStream(rawExtVal);
        byte[] encoded = in.getOctetString();
        switch (extId) {
        case PRIVATE_KEY_USAGE_ID:
            try {
                return new PrivateKeyUsageExtension(FALSE, encoded);
            } catch (CertificateException ex) {
                throw new IOException(ex.getMessage());
            }
        case SUBJECT_ALT_NAME_ID:
            return new SubjectAlternativeNameExtension(FALSE, encoded);
        case NAME_CONSTRAINTS_ID:
            return new NameConstraintsExtension(FALSE, encoded);
        case CERT_POLICIES_ID:
            return new CertificatePoliciesExtension(FALSE, encoded);
        case EXTENDED_KEY_USAGE_ID:
            return new ExtendedKeyUsageExtension(FALSE, encoded);
        default:
            return null;
        }
    }

    /**
     * 决定是否应选择一个 {@code Certificate}。
     *
     * @param cert 要检查的 {@code Certificate}
     * @return 如果应选择 {@code Certificate}，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean match(Certificate cert) {
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate xcert = (X509Certificate)cert;

        if (debug != null) {
            debug.println("X509CertSelector.match(SN: "
                + (xcert.getSerialNumber()).toString(16) + "\n  Issuer: "
                + xcert.getIssuerDN() + "\n  Subject: " + xcert.getSubjectDN()
                + ")");
        }

        /* 匹配 X509Certificate */
        if (x509Cert != null) {
            if (!x509Cert.equals(xcert)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "certs don't match");
                }
                return false;
            }
        }

        /* 匹配序列号 */
        if (serialNumber != null) {
            if (!serialNumber.equals(xcert.getSerialNumber())) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "serial numbers don't match");
                }
                return false;
            }
        }

        /* 匹配发行者名称 */
        if (issuer != null) {
            if (!issuer.equals(xcert.getIssuerX500Principal())) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "issuer DNs don't match");
                }
                return false;
            }
        }

        /* 匹配主题名称 */
        if (subject != null) {
            if (!subject.equals(xcert.getSubjectX500Principal())) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "subject DNs don't match");
                }
                return false;
            }
        }

        /* 匹配证书有效期 */
        if (certificateValid != null) {
            try {
                xcert.checkValidity(certificateValid);
            } catch (CertificateException e) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "certificate not within validity period");
                }
                return false;
            }
        }


                    /* 匹配主题公钥 */
        if (subjectPublicKeyBytes != null) {
            byte[] certKey = xcert.getPublicKey().getEncoded();
            if (!Arrays.equals(subjectPublicKeyBytes, certKey)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "主题公钥不匹配");
                }
                return false;
            }
        }

        boolean result = matchBasicConstraints(xcert)
                      && matchKeyUsage(xcert)
                      && matchExtendedKeyUsage(xcert)
                      && matchSubjectKeyID(xcert)
                      && matchAuthorityKeyID(xcert)
                      && matchPrivateKeyValid(xcert)
                      && matchSubjectPublicKeyAlgID(xcert)
                      && matchPolicy(xcert)
                      && matchSubjectAlternativeNames(xcert)
                      && matchPathToNames(xcert)
                      && matchNameConstraints(xcert);

        if (result && (debug != null)) {
            debug.println("X509CertSelector.match 返回: true");
        }
        return result;
    }

    /* 匹配主题密钥标识符扩展值 */
    private boolean matchSubjectKeyID(X509Certificate xcert) {
        if (subjectKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.14");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "没有主题密钥标识符扩展");
                }
                return false;
            }
            DerInputStream in = new DerInputStream(extVal);
            byte[] certSubjectKeyID = in.getOctetString();
            if (certSubjectKeyID == null ||
                    !Arrays.equals(subjectKeyID, certSubjectKeyID)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: 主题密钥标识符不匹配\nX509CertSelector.match: subjectKeyID: " +
                        Arrays.toString(subjectKeyID) +
                        "\nX509CertSelector.match: certSubjectKeyID: " +
                        Arrays.toString(certSubjectKeyID));
                }
                return false;
            }
        } catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "主题密钥标识符检查时发生异常");
            }
            return false;
        }
        return true;
    }

    /* 匹配权威密钥标识符扩展值 */
    private boolean matchAuthorityKeyID(X509Certificate xcert) {
        if (authorityKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.35");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "没有权威密钥标识符扩展");
                }
                return false;
            }
            DerInputStream in = new DerInputStream(extVal);
            byte[] certAuthKeyID = in.getOctetString();
            if (certAuthKeyID == null ||
                    !Arrays.equals(authorityKeyID, certAuthKeyID)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "权威密钥标识符不匹配");
                }
                return false;
            }
        } catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "权威密钥标识符检查时发生异常");
            }
            return false;
        }
        return true;
    }

    /* 匹配私钥使用范围 */
    private boolean matchPrivateKeyValid(X509Certificate xcert) {
        if (privateKeyValid == null) {
            return true;
        }
        PrivateKeyUsageExtension ext = null;
        try {
            ext = (PrivateKeyUsageExtension)
                getExtensionObject(xcert, PRIVATE_KEY_USAGE_ID);
            if (ext != null) {
                ext.valid(privateKeyValid);
            }
        } catch (CertificateExpiredException e1) {
            if (debug != null) {
                String time = "n/a";
                try {
                    Date notAfter = ext.get(PrivateKeyUsageExtension.NOT_AFTER);
                    time = notAfter.toString();
                } catch (CertificateException ex) {
                    // 无法检索 notAfter 值
                }
                debug.println("X509CertSelector.match: 私钥使用不在有效期内; ext.NOT_After: "
                    + time + "; X509CertSelector: "
                    + this.toString());
                e1.printStackTrace();
            }
            return false;
        } catch (CertificateNotYetValidException e2) {
            if (debug != null) {
                String time = "n/a";
                try {
                    Date notBefore = ext.get(PrivateKeyUsageExtension.NOT_BEFORE);
                    time = notBefore.toString();
                } catch (CertificateException ex) {
                    // 无法检索 notBefore 值
                }
                debug.println("X509CertSelector.match: 私钥使用不在有效期内; ext.NOT_BEFORE: "
                    + time + "; X509CertSelector: "
                    + this.toString());
                e2.printStackTrace();
            }
            return false;
        } catch (IOException e4) {
            if (debug != null) {
                debug.println("X509CertSelector.match: 私钥使用检查时发生 IOException; X509CertSelector: "
                    + this.toString());
                e4.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /* 匹配主题公钥算法 OID */
    private boolean matchSubjectPublicKeyAlgID(X509Certificate xcert) {
        if (subjectPublicKeyAlgID == null) {
            return true;
        }
        try {
            byte[] encodedKey = xcert.getPublicKey().getEncoded();
            DerValue val = new DerValue(encodedKey);
            if (val.tag != DerValue.tag_Sequence) {
                throw new IOException("无效的密钥格式");
            }


                        AlgorithmId algID = AlgorithmId.parse(val.data.getDerValue());
            if (debug != null) {
                debug.println("X509CertSelector.match: subjectPublicKeyAlgID = "
                    + subjectPublicKeyAlgID + ", xcert subjectPublicKeyAlgID = "
                    + algID.getOID());
            }
            if (!subjectPublicKeyAlgID.equals((Object)algID.getOID())) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "subject public key alg IDs don't match");
                }
                return false;
            }
        } catch (IOException e5) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in subject "
                    + "public key algorithm OID check");
            }
            return false;
        }
        return true;
    }

    /* 匹配密钥用法扩展值 */
    private boolean matchKeyUsage(X509Certificate xcert) {
        if (keyUsage == null) {
            return true;
        }
        boolean[] certKeyUsage = xcert.getKeyUsage();
        if (certKeyUsage != null) {
            for (int keyBit = 0; keyBit < keyUsage.length; keyBit++) {
                if (keyUsage[keyBit] &&
                    ((keyBit >= certKeyUsage.length) || !certKeyUsage[keyBit])) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: "
                            + "key usage bits don't match");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /* 匹配扩展密钥用法目的OID */
    private boolean matchExtendedKeyUsage(X509Certificate xcert) {
        if ((keyPurposeSet == null) || keyPurposeSet.isEmpty()) {
            return true;
        }
        try {
            ExtendedKeyUsageExtension ext =
                (ExtendedKeyUsageExtension)getExtensionObject(xcert,
                                                EXTENDED_KEY_USAGE_ID);
            if (ext != null) {
                Vector<ObjectIdentifier> certKeyPurposeVector =
                    ext.get(ExtendedKeyUsageExtension.USAGES);
                if (!certKeyPurposeVector.contains(ANY_EXTENDED_KEY_USAGE)
                        && !certKeyPurposeVector.containsAll(keyPurposeOIDSet)) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: cert failed "
                            + "extendedKeyUsage criterion");
                    }
                    return false;
                }
            }
        } catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "IOException in extended key usage check");
            }
            return false;
        }
        return true;
    }

    /* 匹配主题备用名称扩展名称 */
    private boolean matchSubjectAlternativeNames(X509Certificate xcert) {
        if ((subjectAlternativeNames == null) || subjectAlternativeNames.isEmpty()) {
            return true;
        }
        try {
            SubjectAlternativeNameExtension sanExt =
                (SubjectAlternativeNameExtension) getExtensionObject(xcert,
                                                      SUBJECT_ALT_NAME_ID);
            if (sanExt == null) {
                if (debug != null) {
                  debug.println("X509CertSelector.match: "
                      + "no subject alternative name extension");
                }
                return false;
            }
            GeneralNames certNames =
                    sanExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
            Iterator<GeneralNameInterface> i =
                                subjectAlternativeGeneralNames.iterator();
            while (i.hasNext()) {
                GeneralNameInterface matchName = i.next();
                boolean found = false;
                for (Iterator<GeneralName> t = certNames.iterator();
                                                t.hasNext() && !found; ) {
                    GeneralNameInterface certName = (t.next()).getName();
                    found = certName.equals(matchName);
                }
                if (!found && (matchAllSubjectAltNames || !i.hasNext())) {
                    if (debug != null) {
                      debug.println("X509CertSelector.match: subject alternative "
                          + "name " + matchName + " not found");
                    }
                    return false;
                } else if (found && !matchAllSubjectAltNames) {
                    break;
                }
            }
        } catch (IOException ex) {
            if (debug != null)
                debug.println("X509CertSelector.match: IOException in subject "
                    + "alternative name check");
            return false;
        }
        return true;
    }

    /* 匹配名称约束 */
    private boolean matchNameConstraints(X509Certificate xcert) {
        if (nc == null) {
            return true;
        }
        try {
            if (!nc.verify(xcert)) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: "
                        + "name constraints not satisfied");
                }
                return false;
            }
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    /* 匹配策略OID */
    private boolean matchPolicy(X509Certificate xcert) {
        if (policy == null) {
            return true;
        }
        try {
            CertificatePoliciesExtension ext = (CertificatePoliciesExtension)
                getExtensionObject(xcert, CERT_POLICIES_ID);
            if (ext == null) {
                if (debug != null) {
                  debug.println("X509CertSelector.match: "
                      + "no certificate policy extension");
                }
                return false;
            }
            List<PolicyInformation> policies = ext.get(CertificatePoliciesExtension.POLICIES);
            /*
             * 将PolicyInformation的Vector转换为CertificatePolicyIds的Vector
             * 以便于比较。
             */
            List<CertificatePolicyId> policyIDs = new ArrayList<CertificatePolicyId>(policies.size());
            for (PolicyInformation info : policies) {
                policyIDs.add(info.getPolicyIdentifier());
            }
            if (policy != null) {
                boolean foundOne = false;
                /*
                 * 如果用户传递一个空的策略Set，则
                 * 我们只是想确保候选证书
                 * 在其CertPoliciesExtension中有一些策略OID
                 */
                if (policy.getCertPolicyIds().isEmpty()) {
                    if (policyIDs.isEmpty()) {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: "
                                + "cert failed policyAny criterion");
                        }
                        return false;
                    }
                } else {
                    for (CertificatePolicyId id : policy.getCertPolicyIds()) {
                        if (policyIDs.contains(id)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if (!foundOne) {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: "
                                + "cert failed policyAny criterion");
                        }
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "IOException in certificate policy ID check");
            }
            return false;
        }
        return true;
    }


                /* 在路径名称上进行匹配 */
    private boolean matchPathToNames(X509Certificate xcert) {
        if (pathToGeneralNames == null) {
            return true;
        }
        try {
            NameConstraintsExtension ext = (NameConstraintsExtension)
                getExtensionObject(xcert, NAME_CONSTRAINTS_ID);
            if (ext == null) {
                return true;
            }
            if ((debug != null) && Debug.isOn("certpath")) {
                debug.println("X509CertSelector.match pathToNames:\n");
                Iterator<GeneralNameInterface> i =
                                        pathToGeneralNames.iterator();
                while (i.hasNext()) {
                    debug.println("    " + i.next() + "\n");
                }
            }

            GeneralSubtrees permitted =
                    ext.get(NameConstraintsExtension.PERMITTED_SUBTREES);
            GeneralSubtrees excluded =
                    ext.get(NameConstraintsExtension.EXCLUDED_SUBTREES);
            if (excluded != null) {
                if (matchExcluded(excluded) == false) {
                    return false;
                }
            }
            if (permitted != null) {
                if (matchPermitted(permitted) == false) {
                    return false;
                }
            }
        } catch (IOException ex) {
            if (debug != null) {
                debug.println("X509CertSelector.match: "
                    + "IOException in name constraints check");
            }
            return false;
        }
        return true;
    }

    private boolean matchExcluded(GeneralSubtrees excluded) {
        /*
         * 遍历排除列表中的每个条目，并将其与所有路径名称进行比较。
         * 如果任何路径名称在排除列表中的任何子树内，则返回 false。
         */
        for (Iterator<GeneralSubtree> t = excluded.iterator(); t.hasNext(); ) {
            GeneralSubtree tree = t.next();
            GeneralNameInterface excludedName = tree.getName().getName();
            Iterator<GeneralNameInterface> i = pathToGeneralNames.iterator();
            while (i.hasNext()) {
                GeneralNameInterface pathToName = i.next();
                if (excludedName.getType() == pathToName.getType()) {
                    switch (pathToName.constrains(excludedName)) {
                    case GeneralNameInterface.NAME_WIDENS:
                    case GeneralNameInterface.NAME_MATCH:
                        if (debug != null) {
                            debug.println("X509CertSelector.match: name constraints "
                                + "inhibit path to specified name");
                            debug.println("X509CertSelector.match: excluded name: " +
                                pathToName);
                        }
                        return false;
                    default:
                    }
                }
            }
        }
        return true;
    }

    private boolean matchPermitted(GeneralSubtrees permitted) {
        /*
         * 遍历路径名称，检查每个路径名称是否在允许列表中的至少一个子树内。
         * 如果不在，则返回 false。但是，如果未列出给定类型的任何子树，则允许所有该类型的名称。
         */
        Iterator<GeneralNameInterface> i = pathToGeneralNames.iterator();
        while (i.hasNext()) {
            GeneralNameInterface pathToName = i.next();
            Iterator<GeneralSubtree> t = permitted.iterator();
            boolean permittedNameFound = false;
            boolean nameTypeFound = false;
            String names = "";
            while (t.hasNext() && !permittedNameFound) {
                GeneralSubtree tree = t.next();
                GeneralNameInterface permittedName = tree.getName().getName();
                if (permittedName.getType() == pathToName.getType()) {
                    nameTypeFound = true;
                    names = names + "  " + permittedName;
                    switch (pathToName.constrains(permittedName)) {
                    case GeneralNameInterface.NAME_WIDENS:
                    case GeneralNameInterface.NAME_MATCH:
                        permittedNameFound = true;
                        break;
                    default:
                    }
                }
            }
            if (!permittedNameFound && nameTypeFound) {
                if (debug != null)
                  debug.println("X509CertSelector.match: " +
                            "name constraints inhibit path to specified name; " +
                            "permitted names of type " + pathToName.getType() +
                            ": " + names);
                return false;
            }
        }
        return true;
    }

    /* 在基本约束上进行匹配 */
    private boolean matchBasicConstraints(X509Certificate xcert) {
        if (basicConstraints == -1) {
            return true;
        }
        int maxPathLen = xcert.getBasicConstraints();
        if (basicConstraints == -2) {
            if (maxPathLen != -1) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: not an EE cert");
                }
                return false;
            }
        } else {
            if (maxPathLen < basicConstraints) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: cert's maxPathLen " +
                            "is less than the min maxPathLen set by " +
                            "basicConstraints. " +
                            "(" + maxPathLen + " < " + basicConstraints + ")");
                }
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked") // 假设 clone() 正确工作时的安全类型转换
    private static <T> Set<T> cloneSet(Set<T> set) {
        if (set instanceof HashSet) {
            Object clone = ((HashSet<T>)set).clone();
            return (Set<T>)clone;
        } else {
            return new HashSet<T>(set);
        }
    }

                /**
     * 返回此对象的副本。
     *
     * @return 副本
     */
    public Object clone() {
        try {
            X509CertSelector copy = (X509CertSelector)super.clone();
            // 必须克隆这些，因为 addPathToName 等方法会修改它们
            if (subjectAlternativeNames != null) {
                copy.subjectAlternativeNames =
                        cloneSet(subjectAlternativeNames);
                copy.subjectAlternativeGeneralNames =
                        cloneSet(subjectAlternativeGeneralNames);
            }
            if (pathToGeneralNames != null) {
                copy.pathToNames = cloneSet(pathToNames);
                copy.pathToGeneralNames = cloneSet(pathToGeneralNames);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }
}
