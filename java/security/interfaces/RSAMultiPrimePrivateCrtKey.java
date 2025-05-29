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

package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * 如 <a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a> 标准中定义的，
 * 使用 <i>中国剩余定理</i> (CRT) 信息值的 RSA 多素数私钥接口。
 *
 * @author Valerie Peng
 *
 *
 * @see java.security.spec.RSAPrivateKeySpec
 * @see java.security.spec.RSAMultiPrimePrivateCrtKeySpec
 * @see RSAPrivateKey
 * @see RSAPrivateCrtKey
 *
 * @since 1.4
 */

public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {

    /**
     * 用于表示与类型早期版本序列化兼容性的类型指纹。
     */
    static final long serialVersionUID = 618058533534628008L;

    /**
     * 返回公指数。
     *
     * @return 公指数。
     */
    public BigInteger getPublicExponent();

    /**
     * 返回 primeP。
     *
     * @return primeP。
     */
    public BigInteger getPrimeP();

    /**
     * 返回 primeQ。
     *
     * @return primeQ。
     */
    public BigInteger getPrimeQ();

    /**
     * 返回 primeExponentP。
     *
     * @return primeExponentP。
     */
    public BigInteger getPrimeExponentP();

    /**
     * 返回 primeExponentQ。
     *
     * @return primeExponentQ。
     */
    public BigInteger getPrimeExponentQ();

    /**
     * 返回 crtCoefficient。
     *
     * @return crtCoefficient。
     */
    public BigInteger getCrtCoefficient();

    /**
     * 返回 otherPrimeInfo，如果只有两个素数因子 (p 和 q)，则返回 null。
     *
     * @return otherPrimeInfo。
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo();
}
