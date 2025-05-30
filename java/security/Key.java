/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * Key 接口是所有密钥的顶级接口。它定义了所有密钥对象共享的功能。所有密钥都有三个特征：
 *
 * <UL>
 *
 * <LI>算法
 *
 * <P>这是该密钥的密钥算法。密钥算法通常是加密或非对称操作算法（如 DSA 或 RSA），这些算法可以与这些算法本身以及相关算法（如 MD5 与 RSA、SHA-1 与 RSA、原始 DSA 等）一起使用。密钥的算法名称可以通过 {@link #getAlgorithm() getAlgorithm} 方法获取。
 *
 * <LI>编码形式
 *
 * <P>这是密钥在 Java 虚拟机外部需要标准表示形式时使用的外部编码形式，例如在将密钥传输给其他方时。密钥根据标准格式（如 X.509 {@code SubjectPublicKeyInfo} 或 PKCS#8）进行编码，并通过 {@link #getEncoded() getEncoded} 方法返回。注意：ASN.1 类型 {@code SubjectPublicKeyInfo} 的语法定义如下：
 *
 * <pre>
 * SubjectPublicKeyInfo ::= SEQUENCE {
 *   algorithm AlgorithmIdentifier,
 *   subjectPublicKey BIT STRING }
 *
 * AlgorithmIdentifier ::= SEQUENCE {
 *   algorithm OBJECT IDENTIFIER,
 *   parameters ANY DEFINED BY algorithm OPTIONAL }
 * </pre>
 *
 * 更多信息，请参见
 * <a href="http://tools.ietf.org/html/rfc5280">RFC 5280:
 * Internet X.509 Public Key Infrastructure Certificate and CRL Profile</a>。
 *
 * <LI>格式
 *
 * <P>这是编码密钥的格式名称。它通过 {@link #getFormat() getFormat} 方法返回。
 *
 * </UL>
 *
 * 密钥通常通过密钥生成器、证书或各种用于管理密钥的身份类获得。密钥也可以通过密钥工厂（参见 {@link KeyFactory}）从密钥规范（密钥材料的透明表示）中获得。
 *
 * <p> 密钥应使用 KeyRep 作为其序列化表示。注意，序列化的密钥可能包含不应在不受信任环境中暴露的敏感信息。有关更多信息，请参见
 * <a href="../../../platform/serialization/spec/security.html">
 * 序列化规范的安全附录</a>。
 *
 * @see PublicKey
 * @see PrivateKey
 * @see KeyPair
 * @see KeyPairGenerator
 * @see KeyFactory
 * @see KeyRep
 * @see java.security.spec.KeySpec
 * @see Identity
 * @see Signer
 *
 * @author Benjamin Renaud
 */

public interface Key extends java.io.Serializable {

    // 声明 serialVersionUID 以与 JDK1.1 兼容

   /**
    * 设置的类指纹，用于表示与类的先前版本的序列化兼容性。
    */
    static final long serialVersionUID = 6603384152749567654L;

    /**
     * 返回此密钥的标准算法名称。例如，“DSA”表示此密钥是 DSA 密钥。
     * 有关标准算法名称的信息，请参见 <a href=
     * "../../../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java 密码架构 API 规范与参考 </a> 附录 A。
     *
     * @return 与此密钥关联的算法名称。
     */
    public String getAlgorithm();

    /**
     * 返回此密钥的主要编码格式名称，如果此密钥不支持编码，则返回 null。
     * 主要编码格式以适当的 ASN.1 数据格式命名，如果存在此密钥的 ASN.1 规范。
     * 例如，公钥的 ASN.1 数据格式名称为 <I>SubjectPublicKeyInfo</I>，如 X.509 标准所定义；在这种情况下，返回的格式为
     * {@code "X.509"}。类似地，私钥的 ASN.1 数据格式名称为
     * <I>PrivateKeyInfo</I>，如 PKCS #8 标准所定义；在这种情况下，返回的格式为
     * {@code "PKCS#8"}。
     *
     * @return 密钥的主要编码格式。
     */
    public String getFormat();

    /**
     * 返回密钥的主要编码格式，如果此密钥不支持编码，则返回 null。
     *
     * @return 编码后的密钥，如果密钥不支持编码，则返回 null。
     */
    public byte[] getEncoded();
}
