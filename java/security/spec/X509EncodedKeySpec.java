/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.spec;

/**
 * 该类表示公钥的ASN.1编码，根据ASN.1类型{@code SubjectPublicKeyInfo}进行编码。
 * {@code SubjectPublicKeyInfo}语法在X.509标准中定义如下：
 *
 * <pre>
 * SubjectPublicKeyInfo ::= SEQUENCE {
 *   algorithm AlgorithmIdentifier,
 *   subjectPublicKey BIT STRING }
 * </pre>
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 * @see PKCS8EncodedKeySpec
 *
 * @since 1.2
 */

public class X509EncodedKeySpec extends EncodedKeySpec {

    /**
     * 使用给定的编码密钥创建一个新的X509EncodedKeySpec。
     *
     * @param encodedKey 密钥，假定其编码符合X.509标准。数组的内容将被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code encodedKey} 为null。
     */
    public X509EncodedKeySpec(byte[] encodedKey) {
        super(encodedKey);
    }

    /**
     * 返回根据X.509标准编码的密钥字节。
     *
     * @return 密钥的X.509编码。每次调用此方法时返回一个新的数组。
     */
    public byte[] getEncoded() {
        return super.getEncoded();
    }

    /**
     * 返回与此密钥规范关联的编码格式的名称。
     *
     * @return 字符串 {@code "X.509"}。
     */
    public final String getFormat() {
        return "X.509";
    }
}
