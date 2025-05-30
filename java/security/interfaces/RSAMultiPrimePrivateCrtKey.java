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

package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * 根据 <a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a> 标准定义的 RSA 多素数私钥接口，
 * 使用 <i>中国剩余定理</i> (CRT) 信息值。
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
     * 用于表示与类型先前版本的序列化兼容性的类型指纹。
     */
    static final long serialVersionUID = 618058533534628008L;

    /**
     * 返回公钥指数。
     *
     * @return 公钥指数。
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
     * 返回 otherPrimeInfo，如果只有两个素数因子 (p 和 q) 则返回 null。
     *
     * @return otherPrimeInfo。
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo();
}
