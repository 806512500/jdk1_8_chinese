/*
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
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
 * 该类表示 RSA 的 OtherPrimeInfo 结构中的三元组（素数、指数和系数），如
 * <a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a> 标准中所定义。
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
    * 给定 PKCS#1 中定义的素数、素数指数和 crt 系数，创建一个新的 {@code RSAOtherPrimeInfo}。
    *
    * @param prime 素数因子 n。
    * @param primeExponent 指数。
    * @param crtCoefficient 中国剩余定理系数。
    * @exception NullPointerException 如果任何参数，即
    * {@code prime}、{@code primeExponent}、
    * {@code crtCoefficient} 为 null。
    *
    */
    public RSAOtherPrimeInfo(BigInteger prime,
                          BigInteger primeExponent,
                          BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException("素数参数必须非空" +
                                            "非空");
        }
        if (primeExponent == null) {
            throw new NullPointerException("素数指数参数必须" +
                                            "非空");
        }
        if (crtCoefficient == null) {
            throw new NullPointerException("crt 系数参数必须" +
                                            "非空");
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
     * 返回素数的 crt 系数。
     *
     * @return crt 系数。
     */
    public final BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }
}
