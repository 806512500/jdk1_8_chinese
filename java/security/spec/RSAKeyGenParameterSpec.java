/*
 * 版权所有 (c) 1999, 2020, Oracle 和/或其附属公司。保留所有权利。
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
import java.security.spec.AlgorithmParameterSpec;

/**
 * 该类指定了用于生成 RSA 密钥对的一组参数。
 *
 * @author Jan Luehe
 *
 * @see java.security.KeyPairGenerator#initialize(java.security.spec.AlgorithmParameterSpec)
 *
 * @since 1.3
 */

public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec {

    private int keysize;
    private BigInteger publicExponent;
    private AlgorithmParameterSpec keyParams;

    /**
     * 公钥指数值 F0 = 3。
     */
    public static final BigInteger F0 = BigInteger.valueOf(3);

    /**
     * 公钥指数值 F4 = 65537。
     */
    public static final BigInteger F4 = BigInteger.valueOf(65537);

    /**
     * 从给定的密钥大小、公钥指数值和空密钥参数构造一个新的 {@code RSAKeyGenParameterSpec} 对象。
     *
     * @param keysize 密钥大小（以位数指定）
     * @param publicExponent 公钥指数
     */
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent) {
        this(keysize, publicExponent, null);
    }

    /**
     * 从给定的密钥大小、公钥指数值和密钥参数构造一个新的 {@code RSAKeyGenParameterSpec} 对象。
     *
     * @param keysize 密钥大小（以位数指定）
     * @param publicExponent 公钥指数
     * @param keyParams 密钥参数，可以为 null
     * @since 8
     */
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent,
            AlgorithmParameterSpec keyParams) {
        this.keysize = keysize;
        this.publicExponent = publicExponent;
        this.keyParams = keyParams;
    }

    /**
     * 返回密钥大小。
     *
     * @return 密钥大小。
     */
    public int getKeysize() {
        return keysize;
    }

    /**
     * 返回公钥指数值。
     *
     * @return 公钥指数值。
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    /**
     * 返回与密钥关联的参数。
     *
     * @return 关联的参数，如果不存在则可能为 null
     * @since 8
     */
    public AlgorithmParameterSpec getKeyParams() {
        return keyParams;
    }
}
