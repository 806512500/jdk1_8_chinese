/*
 * 版权所有 (c) 2001, 2020, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类表示 RSA 的 OtherPrimeInfo 结构中的三元组（素数、指数和系数），如
 * <a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a> 标准中定义的那样。
 * RSA 的 OtherPrimeInfo 的 ASN.1 语法如下：
 *
 * <pre>
 * OtherPrimeInfo ::= SEQUENCE {
 *   prime        INTEGER,
 *   exponent     INTEGER,
 *   coefficient  INTEGER
 * }
 *
 * </pre>
 *
 * @author Valerie Peng
 *
 *
 * @see RSAPrivateCrtKeySpec
 * @see java.security.interfaces.RSAMultiPrimePrivateCrtKey
 *
 * @since 1.4
 */

public class RSAOtherPrimeInfo {

    private BigInteger prime;
    private BigInteger primeExponent;
    private BigInteger crtCoefficient;


   /**
    * 根据 PKCS#1 中定义的素数、素数指数和
    * crtCoefficient 创建一个新的 {@code RSAOtherPrimeInfo}。
    *
    * @param prime 素数因子 n。
    * @param primeExponent 指数。
    * @param crtCoefficient 中国剩余定理
    * 系数。
    * @exception NullPointerException 如果任何参数，即
    * {@code prime}, {@code primeExponent},
    * {@code crtCoefficient} 为 null。
    *
    */
    public RSAOtherPrimeInfo(BigInteger prime,
                          BigInteger primeExponent,
                          BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException("素数参数必须 " +
                                            "非空");
        }
        if (primeExponent == null) {
            throw new NullPointerException("素数指数参数 " +
                                            "必须非空");
        }
        if (crtCoefficient == null) {
            throw new NullPointerException("crtCoefficient 参数 " +
                                            "必须非空");
        }
        this.prime = prime;
        this.primeExponent = primeExponent;
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * 返回素数。
     *
     * @return 素数。
     */
    public final BigInteger getPrime() {
        return this.prime;
    }

    /**
     * 返回素数的指数。
     *
     * @return 素数指数。
     */
    public final BigInteger getExponent() {
        return this.primeExponent;
    }

    /**
     * 返回素数的 crtCoefficient。
     *
     * @return crtCoefficient。
     */
    public final BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }
}
