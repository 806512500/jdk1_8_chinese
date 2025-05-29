/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其关联公司。保留所有权利。
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

package java.security.spec;

/**
 * 该类表示根据 ASN.1 类型 {@code PrivateKeyInfo} 编码的私钥的 ASN.1 编码。
 * {@code PrivateKeyInfo} 语法在 PKCS#8 标准中定义如下：
 *
 * <pre>
 * PrivateKeyInfo ::= SEQUENCE {
 *   version Version,
 *   privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
 *   privateKey PrivateKey,
 *   attributes [0] IMPLICIT Attributes OPTIONAL }
 *
 * Version ::= INTEGER
 *
 * PrivateKeyAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * PrivateKey ::= OCTET STRING
 *
 * Attributes ::= SET OF Attribute
 * </pre>
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 * @see X509EncodedKeySpec
 *
 * @since 1.2
 */

public class PKCS8EncodedKeySpec extends EncodedKeySpec {

    /**
     * 使用给定的编码密钥创建一个新的 PKCS8EncodedKeySpec。
     *
     * @param encodedKey 假定根据 PKCS #8 标准编码的密钥。数组的内容被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code encodedKey} 为 null。
     */
    public PKCS8EncodedKeySpec(byte[] encodedKey) {
        super(encodedKey);
    }

    /**
     * 返回根据 PKCS #8 标准编码的密钥字节。
     *
     * @return PKCS #8 编码的密钥。每次调用此方法时返回一个新的数组。
     */
    public byte[] getEncoded() {
        return super.getEncoded();
    }

    /**
     * 返回与此密钥规范关联的编码格式的名称。
     *
     * @return 字符串 {@code "PKCS#8"}。
     */
    public final String getFormat() {
        return "PKCS#8";
    }
}
