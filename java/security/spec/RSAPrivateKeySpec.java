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
 * 该类指定一个 RSA 私钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see PKCS8EncodedKeySpec
 * @see RSAPublicKeySpec
 * @see RSAPrivateCrtKeySpec
 */

public class RSAPrivateKeySpec implements KeySpec {

    private final BigInteger modulus;
    private final BigInteger privateExponent;
    private final AlgorithmParameterSpec params;

    /**
     * 创建一个新的 RSAPrivateKeySpec。
     *
     * @param modulus 模数
     * @param privateExponent 私钥指数
     */
    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent) {
        this(modulus, privateExponent, null);
    }

    /**
     * 创建一个新的 RSAPrivateKeySpec 并包含额外的密钥参数。
     *
     * @param modulus 模数
     * @param privateExponent 私钥指数
     * @param params 与此密钥关联的参数，可能为 null
     * @since 8
     */
    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent,
            AlgorithmParameterSpec params) {
        this.modulus = modulus;
        this.privateExponent = privateExponent;
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
     * 返回私钥指数。
     *
     * @return 私钥指数
     */
    public BigInteger getPrivateExponent() {
        return this.privateExponent;
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
