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

import java.util.Arrays;

import java.security.Provider;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import sun.security.x509.X509CertImpl;

/**
 * <p>抽象类，用于管理各种身份证书。身份证书是将一个主体绑定到一个公钥，并由另一个主体担保的绑定。主体代表一个实体，如个人用户、组或公司。
 *<p>
 * 该类是一个抽象类，用于管理具有不同格式但重要共同用途的证书。例如，不同类型的证书，如X.509和PGP，共享通用的证书功能（如编码和验证）和一些类型的信息（如公钥）。
 * <p>
 * X.509、PGP和SDSI证书都可以通过继承Certificate类来实现，即使它们包含不同的信息集，并且以不同的方式存储和检索信息。
 *
 * @see X509Certificate
 * @see CertificateFactory
 *
 * @author Hemma Prafullchandra
 */

public abstract class Certificate implements java.io.Serializable {

    private static final long serialVersionUID = -3585440601605666277L;

    // 证书类型
    private final String type;

    /** 为证书缓存哈希码 */
    private int hash = -1; // 默认为 -1

    /**
     * 创建指定类型的证书。
     *
     * @param type 证书类型的标准化名称。
     * 参见<a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 了解标准证书类型的详细信息。
     */
    protected Certificate(String type) {
        this.type = type;
    }

    /**
     * 返回此证书的类型。
     *
     * @return 此证书的类型。
     */
    public final String getType() {
        return this.type;
    }

    /**
     * 比较此证书与指定对象是否相等。如果指定对象是 {@code Certificate} 的实例，则检索其编码形式并将其与本证书的编码形式进行比较。
     *
     * @param other 要与本证书进行比较的对象。
     * @return 如果两个证书的编码形式匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Certificate)) {
            return false;
        }
        try {
            byte[] thisCert = X509CertImpl.getEncodedInternal(this);
            byte[] otherCert = X509CertImpl.getEncodedInternal((Certificate)other);

            return Arrays.equals(thisCert, otherCert);
        } catch (CertificateException e) {
            return false;
        }
    }

    /**
     * 从证书的编码形式返回其哈希码。
     *
     * @return 哈希码值。
     */
    public int hashCode() {
        int h = hash;
        if (h == -1) {
            try {
                h = Arrays.hashCode(X509CertImpl.getEncodedInternal(this));
            } catch (CertificateException e) {
                h = 0;
            }
            hash = h;
        }
        return h;
    }

    /**
     * 返回此证书的编码形式。假设每种证书类型只有一种编码形式；例如，X.509 证书将被编码为 ASN.1 DER。
     *
     * @return 此证书的编码形式
     *
     * @exception CertificateEncodingException 如果发生编码错误。
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * 验证此证书是否使用指定公钥对应的私钥签名。
     *
     * @param key 用于执行验证的公钥。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception NoSuchProviderException 如果没有默认提供者。
     * @exception SignatureException 如果签名错误。
     * @exception CertificateException 如果编码错误。
     */
    public abstract void verify(PublicKey key)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * 验证此证书是否使用指定公钥对应的私钥签名。此方法使用指定提供者提供的签名验证引擎。
     *
     * @param key 用于执行验证的公钥。
     * @param sigProvider 签名提供者的名称。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception NoSuchProviderException 如果提供者不正确。
     * @exception SignatureException 如果签名错误。
     * @exception CertificateException 如果编码错误。
     */
    public abstract void verify(PublicKey key, String sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * 验证此证书是否使用指定公钥对应的私钥签名。此方法使用指定提供者提供的签名验证引擎。注意，指定的 Provider 对象不必注册在提供者列表中。
     *
     * <p> 该方法是在 Java 平台标准版 1.8 中添加的，为了保持与现有服务提供者的向后兼容性，该方法不能是 {@code abstract}，默认情况下会抛出 {@code UnsupportedOperationException}。
     *
     * @param key 用于执行验证的公钥。
     * @param sigProvider 签名提供者。
     *
     * @exception NoSuchAlgorithmException 如果签名算法不受支持。
     * @exception InvalidKeyException 如果密钥不正确。
     * @exception SignatureException 如果签名错误。
     * @exception CertificateException 如果编码错误。
     * @exception UnsupportedOperationException 如果方法不支持
     * @since 1.8
     */
    public void verify(PublicKey key, Provider sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, SignatureException {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回此证书的字符串表示形式。
     *
     * @return 此证书的字符串表示形式。
     */
    public abstract String toString();

    /**
     * 从证书中获取公钥。
     *
     * @return 公钥。
     */
    public abstract PublicKey getPublicKey();

    /**
     * 用于序列化的备用 Certificate 类。
     * @since 1.3
     */
    protected static class CertificateRep implements java.io.Serializable {

        private static final long serialVersionUID = -8563758940495660020L;

        private String type;
        private byte[] data;

        /**
         * 使用证书类型和证书编码字节构造备用 Certificate 类。
         *
         * <p>
         *
         * @param type 证书类型的标准化名称。 <p>
         *
         * @param data 证书数据。
         */
        protected CertificateRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * 解析证书对象。
         *
         * <p>
         *
         * @return 解析后的证书对象
         *
         * @throws java.io.ObjectStreamException 如果证书无法解析
         */
        protected Object readResolve() throws java.io.ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertificate
                        (new java.io.ByteArrayInputStream(data));
            } catch (CertificateException e) {
                throw new java.io.NotSerializableException
                                ("java.security.cert.Certificate: " +
                                type +
                                ": " +
                                e.getMessage());
            }
        }
    }

    /**
     * 替换要序列化的证书。
     *
     * @return 要序列化的备用证书对象
     *
     * @throws java.io.ObjectStreamException 如果无法创建表示此证书的新对象
     * @since 1.3
     */
    protected Object writeReplace() throws java.io.ObjectStreamException {
        try {
            return new CertificateRep(type, getEncoded());
        } catch (CertificateException e) {
            throw new java.io.NotSerializableException
                                ("java.security.cert.Certificate: " +
                                type +
                                ": " +
                                e.getMessage());
        }
    }
}
