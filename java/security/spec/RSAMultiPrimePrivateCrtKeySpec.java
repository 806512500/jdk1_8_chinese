
/*
 * 版权所有 (c) 2001, 2020，Oracle 和/或其附属公司。保留所有权利。
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
import java.util.Objects;

/**
 * 该类根据 PKCS#1 v2.2 标准定义了一个使用中国剩余定理 (CRT) 信息值的 RSA 多素数私钥，
 * 以提高效率。
 *
 * @author Valerie Peng
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see PKCS8EncodedKeySpec
 * @see RSAPrivateKeySpec
 * @see RSAPublicKeySpec
 * @see RSAOtherPrimeInfo
 *
 * @since 1.4
 */

public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {

    private final BigInteger publicExponent;
    private final BigInteger primeP;
    private final BigInteger primeQ;
    private final BigInteger primeExponentP;
    private final BigInteger primeExponentQ;
    private final BigInteger crtCoefficient;
    private final RSAOtherPrimeInfo otherPrimeInfo[];

   /**
    * 创建一个新的 {@code RSAMultiPrimePrivateCrtKeySpec}。
    *
    * <p>注意，当构造此对象时，会复制 {@code otherPrimeInfo} 的内容以防止后续修改。
    *
    * @param modulus         模数 n
    * @param publicExponent  公钥指数 e
    * @param privateExponent 私钥指数 d
    * @param primeP          模数 n 的素数因子 p
    * @param primeQ          模数 n 的素数因子 q
    * @param primeExponentP  这是 d 模 (p-1)
    * @param primeExponentQ  这是 d 模 (q-1)
    * @param crtCoefficient  中国剩余定理系数 q-1 模 p
    * @param otherPrimeInfo  其余素数的三元组，如果只有两个素数因子 (p 和 q) 可以指定为 null
    * @throws NullPointerException     如果指定的参数中除 {@code otherPrimeInfo} 之外的任何参数为 null
    * @throws IllegalArgumentException 如果指定了空的，即 0 长度的 {@code otherPrimeInfo}
    */
    public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient,
                                RSAOtherPrimeInfo[] otherPrimeInfo) {
        this(modulus, publicExponent, privateExponent, primeP, primeQ,
             primeExponentP, primeExponentQ, crtCoefficient, otherPrimeInfo,
             null);
    }

   /**
    * 创建一个新的带有额外密钥参数的 {@code RSAMultiPrimePrivateCrtKeySpec}。
    *
    * <p>注意，当构造此对象时，会复制 {@code otherPrimeInfo} 的内容以防止后续修改。
    *
    * @param modulus          模数 n
    * @param publicExponent   公钥指数 e
    * @param privateExponent  私钥指数 d
    * @param primeP           模数 n 的素数因子 p
    * @param primeQ           模数 n 的素数因子 q
    * @param primeExponentP   这是 d 模 (p-1)
    * @param primeExponentQ   这是 d 模 (q-1)
    * @param crtCoefficient   中国剩余定理系数 q-1 模 p
    * @param otherPrimeInfo   其余素数的三元组，如果只有两个素数因子 (p 和 q) 可以指定为 null
    * @param keyParams        与密钥关联的参数
    * @throws NullPointerException     如果指定的参数中除 {@code otherPrimeInfo} 和 {@code keyParams} 之外的任何参数为 null
    * @throws IllegalArgumentException 如果指定了空的，即 0 长度的 {@code otherPrimeInfo}
    * @since 8
    */
    public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient,
                                RSAOtherPrimeInfo[] otherPrimeInfo,
                                AlgorithmParameterSpec keyParams) {
        super(modulus, privateExponent, keyParams);
        Objects.requireNonNull(modulus,
            "模数参数必须非空");
        Objects.requireNonNull(privateExponent,
            "私钥指数参数必须非空");
        this.publicExponent = Objects.requireNonNull(publicExponent,
            "公钥指数参数必须非空");
        this.primeP = Objects.requireNonNull(primeP,
            "素数 P 参数必须非空");
        this.primeQ = Objects.requireNonNull(primeQ,
            "素数 Q 参数必须非空");
        this.primeExponentP = Objects.requireNonNull(primeExponentP,
            "素数指数 P 参数必须非空");
        this.primeExponentQ = Objects.requireNonNull(primeExponentQ,
            "素数指数 Q 参数必须非空");
        this.crtCoefficient = Objects.requireNonNull(crtCoefficient,
            "中国剩余定理系数参数必须非空");

        if (otherPrimeInfo == null)  {
            this.otherPrimeInfo = null;
        } else if (otherPrimeInfo.length == 0) {
            throw new IllegalArgumentException("otherPrimeInfo 参数不能为空");
        } else {
            this.otherPrimeInfo = otherPrimeInfo.clone();
        }
    }

                /**
     * 返回公钥指数。
     *
     * @return 公钥指数。
     */
    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    /**
     * 返回素数P。
     *
     * @return 素数P。
     */
    public BigInteger getPrimeP() {
        return this.primeP;
    }

    /**
     * 返回素数Q。
     *
     * @return 素数Q。
     */
    public BigInteger getPrimeQ() {
        return this.primeQ;
    }

    /**
     * 返回素数P的指数。
     *
     * @return 素数P的指数。
     */
    public BigInteger getPrimeExponentP() {
        return this.primeExponentP;
    }

    /**
     * 返回素数Q的指数。
     *
     * @return 素数Q的指数。
     */
    public BigInteger getPrimeExponentQ() {
        return this.primeExponentQ;
    }

    /**
     * 返回CRT系数。
     *
     * @return CRT系数。
     */
    public BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }

    /**
     * 返回其他素数信息的副本，如果只有两个素数因子（p和q），则返回null。
     *
     * @return 其他素数信息。每次调用此方法时都会返回一个新的数组。
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        if (otherPrimeInfo == null) return null;
        return otherPrimeInfo.clone();
    }
}
