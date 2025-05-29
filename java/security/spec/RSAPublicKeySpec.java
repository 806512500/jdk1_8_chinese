/*
 * 版权所有 (c) 1998, 2020，Oracle 和/或其附属公司。保留所有权利。
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

import java.math.BigInteger;

/**
 * 该类指定一个 RSA 公钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see X509EncodedKeySpec
 * @see RSAPrivateKeySpec
 * @see RSAPrivateCrtKeySpec
 */

public class RSAPublicKeySpec implements KeySpec {

    private final BigInteger modulus;
    private final BigInteger publicExponent;
    private final AlgorithmParameterSpec params;

    /**
     * 创建一个新的 RSAPublicKeySpec。
     *
     * @param modulus 模数
     * @param publicExponent 公钥指数
     */
    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent) {
        this(modulus, publicExponent, null);
    }

    /**
     * 创建一个新的 RSAPublicKeySpec，包含额外的密钥参数。
     *
     * @param modulus 模数
     * @param publicExponent 公钥指数
     * @param params 与此密钥关联的参数，可以为 null
     * @since 8
     */
    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent,
            AlgorithmParameterSpec params) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.params = params;
    }


    /**
     * 返回模数。
     *
     * @return 模数
     */
    public BigInteger getModulus() {
        return this.modulus;
    }

    /**
     * 返回公钥指数。
     *
     * @return 公钥指数
     */
    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    /**
     * 返回与此密钥关联的参数，如果不存在则可能为 null。
     *
     * @return 与此密钥关联的参数
     * @since 8
     */
    public AlgorithmParameterSpec getParams() {
        return this.params;
    }

}
