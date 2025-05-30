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
 * 该类指定一个 RSA 公钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see X509EncodedKeySpec
 * @see RSAPrivateKeySpec
 * @see RSAPrivateCrtKeySpec
 */

public class RSAPublicKeySpec implements KeySpec {

    private final BigInteger modulus;
    private final BigInteger publicExponent;
    private final AlgorithmParameterSpec params;

    /**
     * 创建一个新的 RSAPublicKeySpec。
     *
     * @param modulus 模数
     * @param publicExponent 公钥指数
     */
    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent) {
        this(modulus, publicExponent, null);
    }

    /**
     * 创建一个新的 RSAPublicKeySpec，带有额外的密钥参数。
     *
     * @apiNote 该方法在 Java SE 8 Maintenance Release 3 中定义。
     * @param modulus 模数
     * @param publicExponent 公钥指数
     * @param params 与该密钥关联的参数，可能为 null
     * @since 8
     */
    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent,
            AlgorithmParameterSpec params) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.params = params;
    }


    /**
     * 返回模数。
     *
     * @return 模数
     */
    public BigInteger getModulus() {
        return this.modulus;
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
     * 返回与该密钥关联的参数，如果不存在则可能为 null。
     *
     * @apiNote 该方法在 Java SE 8 Maintenance Release 3 中定义。
     * @return 与该密钥关联的参数
     * @since 8
     */
    public AlgorithmParameterSpec getParams() {
        return this.params;
    }

}
