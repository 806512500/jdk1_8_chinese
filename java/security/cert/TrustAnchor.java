/*
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import sun.security.util.AnchorCertificates;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.X500Name;

/**
 * 一个信任锚点或最值得信赖的认证机构（CA）。
 * <p>
 * 该类表示一个“最值得信赖的CA”，用于作为验证X.509认证路径的信任锚点。
 * 一个最值得信赖的CA包括CA的公钥、CA的名称以及使用此密钥验证的路径集的任何约束。
 * 这些参数可以以受信任的{@code X509Certificate}形式或作为单独的参数指定。
 * <p>
 * <b>并发访问</b>
 * <p>所有{@code TrustAnchor}对象必须是不可变的并且是线程安全的。
 * 即，多个线程可以同时调用定义在该类中的方法（单个或多个）而不会产生不良影响。
 * 要求{@code TrustAnchor}对象是不可变的和线程安全的，允许它们被传递到各种代码片段中而无需担心协调访问。
 * 此要求适用于该类的所有公共字段和方法以及子类添加或覆盖的任何方法。
 *
 * @see PKIXParameters#PKIXParameters(Set)
 * @see PKIXBuilderParameters#PKIXBuilderParameters(Set, CertSelector)
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public class TrustAnchor {

    private final PublicKey pubKey;
    private final String caName;
    private final X500Principal caPrincipal;
    private final X509Certificate trustedCert;
    private byte[] ncBytes;
    private NameConstraintsExtension nc;
    private boolean jdkCA;
    private boolean hasJdkCABeenChecked;

    static {
        CertPathHelperImpl.initialize();
    }

    /**
     * 使用指定的{@code X509Certificate}和可选的名称约束创建{@code TrustAnchor}的实例，
     * 这些名称约束在验证X.509认证路径时用作附加约束。
     * <p>
     * 名称约束以字节数组的形式指定。该字节数组应包含名称约束的DER编码形式，如它们在
     * <a href="http://tools.ietf.org/html/rfc5280">RFC 5280</a>和X.509中定义的NameConstraints结构中出现的那样。
     * 该结构的ASN.1定义如下所示。
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
     * 注意，提供的名称约束字节数组将被克隆以防止后续修改。
     *
     * @param trustedCert 一个受信任的{@code X509Certificate}
     * @param nameConstraints 包含NameConstraints扩展的ASN.1 DER编码的字节数组，用于检查名称约束。
     * 只包括扩展的值，不包括OID或关键性标志。指定{@code null}以省略该参数。
     * @throws IllegalArgumentException 如果名称约束无法解码
     * @throws NullPointerException 如果指定的{@code X509Certificate}为{@code null}
     */
    public TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints)
    {
        if (trustedCert == null)
            throw new NullPointerException("the trustedCert parameter must " +
                "be non-null");
        this.trustedCert = trustedCert;
        this.pubKey = null;
        this.caName = null;
        this.caPrincipal = null;
        setNameConstraints(nameConstraints);
    }

    /**
     * 创建一个{@code TrustAnchor}的实例，其中最值得信赖的CA以X500Principal和公钥的形式指定。
     * 名称约束是一个可选参数，在验证X.509认证路径时用作附加约束。
     * <p>
     * 名称约束以字节数组的形式指定。该字节数组包含名称约束的DER编码形式，如它们在RFC 5280
     * 和X.509中定义的NameConstraints结构中出现的那样。该结构的ASN.1表示法在
     * {@link #TrustAnchor(X509Certificate, byte[])
     * TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) }的文档中提供。
     * <p>
     * 注意，提供的名称约束字节数组将被克隆以防止后续修改。
     *
     * @param caPrincipal 最值得信赖的CA的名称，作为X500Principal
     * @param pubKey 最值得信赖的CA的公钥
     * @param nameConstraints 包含NameConstraints扩展的ASN.1 DER编码的字节数组，用于检查名称约束。
     * 只包括扩展的值，不包括OID或关键性标志。指定{@code null}以省略该参数。
     * @throws NullPointerException 如果指定的{@code caPrincipal}或{@code pubKey}参数为{@code null}
     * @since 1.5
     */
    public TrustAnchor(X500Principal caPrincipal, PublicKey pubKey,
            byte[] nameConstraints) {
        if ((caPrincipal == null) || (pubKey == null)) {
            throw new NullPointerException();
        }
        this.trustedCert = null;
        this.caPrincipal = caPrincipal;
        this.caName = caPrincipal.getName();
        this.pubKey = pubKey;
        setNameConstraints(nameConstraints);
    }

    /**
     * 创建一个{@code TrustAnchor}的实例，其中最值得信赖的CA以可区分名称和公钥的形式指定。
     * 名称约束是一个可选参数，在验证X.509认证路径时用作附加约束。
     * <p>
     * 名称约束以字节数组的形式指定。该字节数组包含名称约束的DER编码形式，如它们在RFC 5280
     * 和X.509中定义的NameConstraints结构中出现的那样。该结构的ASN.1表示法在
     * {@link #TrustAnchor(X509Certificate, byte[])
     * TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) }的文档中提供。
     * <p>
     * 注意，提供的名称约束字节数组将被克隆以防止后续修改。
     *
     * @param caName 最值得信赖的CA的X.500可区分名称，格式为
     * <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a>
     * {@code String}
     * @param pubKey 最值得信赖的CA的公钥
     * @param nameConstraints 包含NameConstraints扩展的ASN.1 DER编码的字节数组，用于检查名称约束。
     * 只包括扩展的值，不包括OID或关键性标志。指定{@code null}以省略该参数。
     * @throws IllegalArgumentException 如果指定的{@code caName}参数为空{@code (caName.length() == 0)}
     * 或格式不正确，或名称约束无法解码
     * @throws NullPointerException 如果指定的{@code caName}或{@code pubKey}参数为{@code null}
     */
    public TrustAnchor(String caName, PublicKey pubKey, byte[] nameConstraints)
    {
        if (pubKey == null)
            throw new NullPointerException("the pubKey parameter must be " +
                "non-null");
        if (caName == null)
            throw new NullPointerException("the caName parameter must be " +
                "non-null");
        if (caName.length() == 0)
            throw new IllegalArgumentException("the caName " +
                "parameter must be a non-empty String");
        // 检查caName是否格式正确
        this.caPrincipal = new X500Principal(caName);
        this.pubKey = pubKey;
        this.caName = caName;
        this.trustedCert = null;
        setNameConstraints(nameConstraints);
    }

    /**
     * 返回最值得信赖的CA证书。
     *
     * @return 一个受信任的{@code X509Certificate}或{@code null}
     * 如果信任锚点未指定为受信任的证书
     */
    public final X509Certificate getTrustedCert() {
        return this.trustedCert;
    }

    /**
     * 返回最值得信赖的CA的名称，作为X500Principal。
     *
     * @return 最值得信赖的CA的X.500可区分名称，或
     * {@code null} 如果信任锚点未指定为受信任的公钥和名称或X500Principal对
     * @since 1.5
     */
    public final X500Principal getCA() {
        return this.caPrincipal;
    }

    /**
     * 返回最值得信赖的CA的名称，格式为RFC 2253 {@code String}。
     *
     * @return 最值得信赖的CA的X.500可区分名称，或
     * {@code null} 如果信任锚点未指定为受信任的公钥和名称或X500Principal对
     */
    public final String getCAName() {
        return this.caName;
    }

    /**
     * 返回最值得信赖的CA的公钥。
     *
     * @return 最值得信赖的CA的公钥，或{@code null}
     * 如果信任锚点未指定为受信任的公钥和名称或X500Principal对
     */
    public final PublicKey getCAPublicKey() {
        return this.pubKey;
    }

    /**
     * 解码名称约束并克隆它们（如果非空）。
     */
    private void setNameConstraints(byte[] bytes) {
        if (bytes == null) {
            ncBytes = null;
            nc = null;
        } else {
            ncBytes = bytes.clone();
            // 验证DER编码
            try {
                nc = new NameConstraintsExtension(Boolean.FALSE, bytes);
            } catch (IOException ioe) {
                IllegalArgumentException iae =
                    new IllegalArgumentException(ioe.getMessage());
                iae.initCause(ioe);
                throw iae;
            }
        }
    }

    /**
     * 返回名称约束参数。指定的名称约束与该信任锚点相关联，并在验证X.509认证路径时用作附加约束。
     * <p>
     * 名称约束以字节数组的形式返回。该字节数组包含名称约束的DER编码形式，如它们在RFC 5280
     * 和X.509中定义的NameConstraints结构中出现的那样。该结构的ASN.1表示法在
     * {@link #TrustAnchor(X509Certificate, byte[])
     * TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) }的文档中提供。
     * <p>
     * 注意，返回的字节数组将被克隆以防止后续修改。
     *
     * @return 包含用于检查名称约束的NameConstraints扩展的ASN.1 DER编码的字节数组，
     * 或者如果未设置则返回{@code null}。
     */
    public final byte [] getNameConstraints() {
        return ncBytes == null ? null : ncBytes.clone();
    }

    /**
     * 返回描述{@code TrustAnchor}的格式化字符串。
     *
     * @return 描述{@code TrustAnchor}的格式化字符串
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        if (pubKey != null) {
            sb.append("  Trusted CA Public Key: " + pubKey.toString() + "\n");
            sb.append("  Trusted CA Issuer Name: "
                + String.valueOf(caName) + "\n");
        } else {
            sb.append("  Trusted CA cert: " + trustedCert.toString() + "\n");
        }
        if (nc != null)
            sb.append("  Name Constraints: " + nc.toString() + "\n");
        return sb.toString();
    }

    /**
     * 如果锚点是JDK CA（默认包含在cacerts密钥库中的根CA），则返回true。
     */
    synchronized boolean isJdkCA() {
        if (!hasJdkCABeenChecked) {
            if (trustedCert != null) {
                jdkCA = AnchorCertificates.contains(trustedCert);
            }
            hasJdkCABeenChecked = true;
        }
        return jdkCA;
    }
}
