
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

import java.util.Arrays;

import java.security.Provider;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import sun.security.x509.X509CertImpl;

/**
 * <p>用于管理各种身份证书的抽象类。
 * 身份证书是将一个主体绑定到一个公钥的凭证，该绑定由另一个主体担保。 (主体代表
 * 一个实体，如个人用户、组或公司。)
 *<p>
 * 该类是对具有不同格式但重要共同用途的证书的抽象。例如，不同类型的
 * 证书，如 X.509 和 PGP，共享一般的证书功能（如编码和验证）和
 * 一些类型的信息（如公钥）。
 * <p>
 * X.509、PGP 和 SDSI 证书都可以通过
 * 继承 Certificate 类来实现，即使它们包含不同的
 * 信息集，并且以不同的方式存储和检索信息。
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

    /** 缓存证书的哈希码 */
    private int hash = -1; // 默认为 -1

    /**
     * 创建指定类型的证书。
     *
     * @param type 证书类型的标准名称。
     * 有关标准证书类型的更多信息，请参见 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java 加密架构标准算法名称文档</a> 中的 CertificateFactory 部分。
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
     * 比较此证书与指定对象的相等性。如果 {@code other} 对象是
     * {@code instanceof} {@code Certificate}，则
     * 获取其编码形式并与此证书的编码形式进行比较。
     *
     * @param other 要与此证书测试相等性的对象。
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
     * 从证书的编码形式返回哈希码值。
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
     * 返回此证书的编码形式。假设每种证书类型只有一种
     * 编码形式；例如，X.509 证书将
     * 编码为 ASN.1 DER。
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
     * @param key 用于执行验证的 PublicKey。
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
     * 验证此证书是否使用指定公钥对应的私钥签名。
     * 此方法使用指定提供者提供的签名验证引擎。
     *
     * @param key 用于执行验证的 PublicKey。
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
     * 验证此证书是否使用指定公钥对应的私钥签名。
     * 此方法使用指定提供者提供的签名验证引擎。注意，指定的
     * Provider 对象不必注册在提供者列表中。
     *
     * <p> 该方法是在 Java 平台标准版 1.8 中添加的。为了保持与
     * 现有服务提供者的向后兼容性，此方法不能是 {@code abstract}
     * 并且默认情况下抛出一个 {@code UnsupportedOperationException}。
     *
     * @param key 用于执行验证的 PublicKey。
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
     * 从此证书中获取公钥。
     *
     * @return 公钥。
     */
    public abstract PublicKey getPublicKey();

    /**
     * 用于序列化的备用证书类。
     * @since 1.3
     */
    protected static class CertificateRep implements java.io.Serializable {

        private static final long serialVersionUID = -8563758940495660020L;

        private String type;
        private byte[] data;

        /**
         * 使用证书类型和证书编码字节构造备用证书类。
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
