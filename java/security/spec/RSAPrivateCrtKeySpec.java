/*
 * Copyright (c) 1998, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.security.spec;

import java.math.BigInteger;

/**
 * 该类根据 PKCS#1 v2.2 标准定义了一个 RSA 私钥，使用中国剩余定理 (CRT) 信息值以提高效率。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see PKCS8EncodedKeySpec
 * @see RSAPrivateKeySpec
 * @see RSAPublicKeySpec
 */

public class RSAPrivateCrtKeySpec extends RSAPrivateKeySpec {

    private final BigInteger publicExponent;
    private final BigInteger primeP;
    private final BigInteger primeQ;
    private final BigInteger primeExponentP;
    private final BigInteger primeExponentQ;
    private final BigInteger crtCoefficient;

   /**
    * 创建一个新的 {@code RSAPrivateCrtKeySpec}。
    *
    * @param modulus 模数 n
    * @param publicExponent 公钥指数 e
    * @param privateExponent 私钥指数 d
    * @param primeP n 的质因数 p
    * @param primeQ n 的质因数 q
    * @param primeExponentP 这是 d mod (p-1)
    * @param primeExponentQ 这是 d mod (q-1)
    * @param crtCoefficient 中国剩余定理系数 q-1 mod p
    */
    public RSAPrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient) {
        this(modulus, publicExponent, privateExponent, primeP, primeQ,
             primeExponentP, primeExponentQ, crtCoefficient, null);
    }

   /**
    * 创建一个新的 {@code RSAPrivateCrtKeySpec}，带附加的密钥参数。
    *
    * @apiNote 该方法定义于 Java SE 8 Maintenance Release 3。
    * @param modulus 模数 n
    * @param publicExponent 公钥指数 e
    * @param privateExponent 私钥指数 d
    * @param primeP n 的质因数 p
    * @param primeQ n 的质因数 q
    * @param primeExponentP 这是 d mod (p-1)
    * @param primeExponentQ 这是 d mod (q-1)
    * @param crtCoefficient 中国剩余定理系数 q-1 mod p
    * @param keyParams 与密钥关联的参数
    * @since 8
    */
    public RSAPrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient,
                                AlgorithmParameterSpec keyParams) {
        super(modulus, privateExponent, keyParams);
        this.publicExponent = publicExponent;
        this.primeP = primeP;
        this.primeQ = primeQ;
        this.primeExponentP = primeExponentP;
        this.primeExponentQ = primeExponentQ;
        this.crtCoefficient = crtCoefficient;
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
     * 返回 primeP。

     * @return primeP
     */
    public BigInteger getPrimeP() {
        return this.primeP;
    }

    /**
     * 返回 primeQ。
     *
     * @return primeQ
     */
    public BigInteger getPrimeQ() {
        return this.primeQ;
    }

    /**
     * 返回 primeExponentP。
     *
     * @return primeExponentP
     */
    public BigInteger getPrimeExponentP() {
        return this.primeExponentP;
    }

    /**
     * 返回 primeExponentQ。
     *
     * @return primeExponentQ
     */
    public BigInteger getPrimeExponentQ() {
        return this.primeExponentQ;
    }

    /**
     * 返回 crtCoefficient。
     *
     * @return crtCoefficient
     */
    public BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }
}
