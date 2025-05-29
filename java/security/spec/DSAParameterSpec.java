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
 * 该类指定了与 DSA 算法一起使用的一组参数。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameterSpec
 *
 * @since 1.2
 */

public class DSAParameterSpec implements AlgorithmParameterSpec,
java.security.interfaces.DSAParams {

    BigInteger p;
    BigInteger q;
    BigInteger g;

    /**
     * 使用指定的参数值创建一个新的 DSAParameterSpec。
     *
     * @param p 素数。
     *
     * @param q 子素数。
     *
     * @param g 基数。
     */
    public DSAParameterSpec(BigInteger p, BigInteger q, BigInteger g) {
        this.p = p;
        this.q = q;
        this.g = g;
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
