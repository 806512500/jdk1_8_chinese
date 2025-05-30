/*
 * Copyright (c) 1999, 2022, Oracle and/or its affiliates. All rights reserved.
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
import java.security.spec.AlgorithmParameterSpec;

/**
 * 该类指定了用于生成 RSA 密钥对的一组参数。
 *
 * @author Jan Luehe
 *
 * @see java.security.KeyPairGenerator#initialize(java.security.spec.AlgorithmParameterSpec)
 *
 * @since 1.3
 */

public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec {

    private int keysize;
    private BigInteger publicExponent;
    private AlgorithmParameterSpec keyParams;

    /**
     * 公钥指数值 F0 = 3。
     */
    public static final BigInteger F0 = BigInteger.valueOf(3);

    /**
     * 公钥指数值 F4 = 65537。
     */
    public static final BigInteger F4 = BigInteger.valueOf(65537);

    /**
     * 从给定的密钥大小、公钥指数值和 null 密钥参数构造一个新的 {@code RSAKeyGenParameterSpec} 对象。
     *
     * @param keysize 密钥大小（以位数指定）
     * @param publicExponent 公钥指数
     */
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent) {
        this(keysize, publicExponent, null);
    }

    /**
     * 从给定的密钥大小、公钥指数值和密钥参数构造一个新的 {@code RSAKeyGenParameterSpec} 对象。
     *
     * @apiNote 该方法在 Java SE 8 Maintenance Release 3 中定义。
     * @param keysize 密钥大小（以位数指定）
     * @param publicExponent 公钥指数
     * @param keyParams 密钥参数，可以为 null
     * @since 8
     */
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent,
            AlgorithmParameterSpec keyParams) {
        this.keysize = keysize;
        this.publicExponent = publicExponent;
        this.keyParams = keyParams;
    }

    /**
     * 返回密钥大小。
     *
     * @return 密钥大小。
     */
    public int getKeysize() {
        return keysize;
    }

    /**
     * 返回公钥指数值。
     *
     * @return 公钥指数值。
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    /**
     * 返回与密钥关联的参数。
     *
     * @apiNote 该方法在 Java SE 8 Maintenance Release 3 中定义。
     * @return 关联的参数，如果不存在则可能为 null
     * @since 8
     */
    public AlgorithmParameterSpec getKeyParams() {
        return keyParams;
    }
}
