/*
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Objects;

/**
 * 该类根据 PKCS#1 v2.2 标准使用中国剩余定理 (CRT) 信息值定义了一个 RSA 多素数私钥，以提高效率。
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
    * <p>注意，当构造此对象时，会复制 {@code otherPrimeInfo} 的内容，以防止后续修改。
    *
    * @param modulus         模数 n
    * @param publicExponent  公钥指数 e
    * @param privateExponent 私钥指数 d
    * @param primeP          模数 n 的素数因子 p
    * @param primeQ          模数 n 的素数因子 q
    * @param primeExponentP  这是 d mod (p-1)
    * @param primeExponentQ  这是 d mod (q-1)
    * @param crtCoefficient  中国剩余定理系数 q-1 mod p
    * @param otherPrimeInfo  其余素数的三元组，如果只有两个素数因子 (p 和 q)，则可以指定为 null
    * @throws NullPointerException     如果除 {@code otherPrimeInfo} 之外的任何指定参数为 null
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
    * 创建一个新的 {@code RSAMultiPrimePrivateCrtKeySpec}，并带有额外的密钥参数。
    *
    * <p>注意，当构造此对象时，会复制 {@code otherPrimeInfo} 的内容，以防止后续修改。
    *
    * @apiNote 该方法在 Java SE 8 Maintenance Release 3 中定义。
    * @param modulus          模数 n
    * @param publicExponent   公钥指数 e
    * @param privateExponent  私钥指数 d
    * @param primeP           模数 n 的素数因子 p
    * @param primeQ           模数 n 的素数因子 q
    * @param primeExponentP   这是 d mod (p-1)
    * @param primeExponentQ   这是 d mod (q-1)
    * @param crtCoefficient   中国剩余定理系数 q-1 mod p
    * @param otherPrimeInfo   其余素数的三元组，如果只有两个素数因子 (p 和 q)，则可以指定为 null
    * @param keyParams        与密钥关联的参数
    * @throws NullPointerException     如果除 {@code otherPrimeInfo} 和 {@code keyParams} 之外的任何指定参数为 null
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
            "素数 p 参数必须非空");
        this.primeQ = Objects.requireNonNull(primeQ,
            "素数 q 参数必须非空");
        this.primeExponentP = Objects.requireNonNull(primeExponentP,
            "素数 p 的指数参数必须非空");
        this.primeExponentQ = Objects.requireNonNull(primeExponentQ,
            "素数 q 的指数参数必须非空");
        this.crtCoefficient = Objects.requireNonNull(crtCoefficient,
            "中国剩余定理系数参数必须非空");

        if (otherPrimeInfo == null)  {
            this.otherPrimeInfo = null;
        } else if (otherPrimeInfo.length == 0) {
            throw new IllegalArgumentException("otherPrimeInfo " +
                                                "参数不能为空");
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
     * 返回素数 p。
     *
     * @return 素数 p。
     */
    public BigInteger getPrimeP() {
        return this.primeP;
    }

    /**
     * 返回素数 q。
     *
     * @return 素数 q。
     */
    public BigInteger getPrimeQ() {
        return this.primeQ;
    }

    /**
     * 返回素数 p 的指数。
     *
     * @return 素数 p 的指数。
     */
    public BigInteger getPrimeExponentP() {
        return this.primeExponentP;
    }

    /**
     * 返回素数 q 的指数。
     *
     * @return 素数 q 的指数。
     */
    public BigInteger getPrimeExponentQ() {
        return this.primeExponentQ;
    }

    /**
     * 返回中国剩余定理系数。
     *
     * @return 中国剩余定理系数。
     */
    public BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }

    /**
     * 返回 otherPrimeInfo 的副本，如果只有两个素数因子 (p 和 q)，则返回 null。
     *
     * @return otherPrimeInfo。每次调用此方法时返回一个新的数组。
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        if (otherPrimeInfo == null) return null;
        return otherPrimeInfo.clone();
    }
}
