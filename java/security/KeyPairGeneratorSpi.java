/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.security.spec.AlgorithmParameterSpec;

/**
 * <p> 此类定义了 {@code KeyPairGenerator} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于生成公钥和私钥对。
 *
 * <p> 每个希望为特定算法提供密钥对生成器实现的加密服务提供者都必须实现此类中的所有抽象方法。
 *
 * <p> 如果客户端没有显式初始化 KeyPairGenerator（通过调用 {@code initialize} 方法），
 * 每个提供者必须提供（并记录）默认初始化。例如，<i>Sun</i> 提供者使用 1024 位的默认模数大小（密钥大小）。
 *
 * @author Benjamin Renaud
 *
 *
 * @see KeyPairGenerator
 * @see java.security.spec.AlgorithmParameterSpec
 */

public abstract class KeyPairGeneratorSpi {

    /**
     * 使用默认参数集初始化密钥对生成器。
     *
     * @param keysize 密钥大小。这是算法特定的度量，例如以位为单位的模数长度。
     *
     * @param random 该生成器的随机源。
     *
     * @exception InvalidParameterException 如果此 KeyPairGeneratorSpi 对象不支持 {@code keysize}。
     */
    public abstract void initialize(int keysize, SecureRandom random);

    /**
     * 使用指定的参数集和用户提供的随机源初始化密钥对生成器。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     * 提供者可以覆盖此方法以初始化密钥对生成器。如果参数不适合此密钥对生成器，预期会抛出 InvalidAlgorithmParameterException。
     * 如果此方法未被覆盖，则始终抛出 UnsupportedOperationException。
     *
     * @param params 用于生成密钥的参数集。
     *
     * @param random 该生成器的随机源。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数不适合此密钥对生成器。
     *
     * @since 1.2
     */
    public void initialize(AlgorithmParameterSpec params,
                           SecureRandom random)
        throws InvalidAlgorithmParameterException {
            throw new UnsupportedOperationException();
    }

    /**
     * 生成一个密钥对。除非使用 KeyPairGenerator 接口调用初始化方法，
     * 否则将使用算法特定的默认值。每次调用此方法都会生成一个新的密钥对。
     *
     * @return 新生成的 {@code KeyPair}
     */
    public abstract KeyPair generateKeyPair();
}
