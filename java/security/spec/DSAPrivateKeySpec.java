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

package java.security.spec;

import java.math.BigInteger;

/**
 * 该类指定了与关联参数一起的 DSA 私钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see DSAPublicKeySpec
 * @see PKCS8EncodedKeySpec
 *
 * @since 1.2
 */

public class DSAPrivateKeySpec implements KeySpec {

    private BigInteger x;
    private BigInteger p;
    private BigInteger q;
    private BigInteger g;

    /**
     * 使用指定的参数值创建一个新的 DSAPrivateKeySpec。
     *
     * @param x 私钥。
     *
     * @param p 素数。
     *
     * @param q 子素数。
     *
     * @param g 基数。
     */
    public DSAPrivateKeySpec(BigInteger x, BigInteger p, BigInteger q,
                             BigInteger g) {
        this.x = x;
        this.p = p;
        this.q = q;
        this.g = g;
    }

    /**
     * 返回私钥 {@code x}。
     *
     * @return 私钥 {@code x}。
     */
    public BigInteger getX() {
        return this.x;
    }

    /**
     * 返回素数 {@code p}。
     *
     * @return 素数 {@code p}。
     */
    public BigInteger getP() {
        return this.p;
    }

    /**
     * 返回子素数 {@code q}。
     *
     * @return 子素数 {@code q}。
     */
    public BigInteger getQ() {
        return this.q;
    }

    /**
     * 返回基数 {@code g}。
     *
     * @return 基数 {@code g}。
     */
    public BigInteger getG() {
        return this.g;
    }
}
