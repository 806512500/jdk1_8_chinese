/*
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
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

package java.security.interfaces;

import java.math.BigInteger;

/**
 * 根据 <a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a> 标准定义的 RSA 私钥接口，
 * 使用 <i>中国剩余定理</i> (CRT) 信息值。
 *
 * @author Jan Luehe
 *
 *
 * @see RSAPrivateKey
 */

public interface RSAPrivateCrtKey extends RSAPrivateKey {

    /**
     * 用于表示与类型先前版本的序列化兼容性的类型指纹。
     */
    static final long serialVersionUID = -5682214253527700368L;

    /**
     * 返回公钥指数。
     *
     * @return 公钥指数
     */
    public BigInteger getPublicExponent();

    /**
     * 返回 primeP。

     * @return primeP
     */
    public BigInteger getPrimeP();

    /**
     * 返回 primeQ。
     *
     * @return primeQ
     */
    public BigInteger getPrimeQ();

    /**
     * 返回 primeExponentP。
     *
     * @return primeExponentP
     */
    public BigInteger getPrimeExponentP();

    /**
     * 返回 primeExponentQ。
     *
     * @return primeExponentQ
     */
    public BigInteger getPrimeExponentQ();

    /**
     * 返回 crtCoefficient。
     *
     * @return crtCoefficient
     */
    public BigInteger getCrtCoefficient();
}
