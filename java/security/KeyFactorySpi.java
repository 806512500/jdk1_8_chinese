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

import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;

/**
 * 此类定义了 {@code KeyFactory} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 所有此类中的抽象方法都必须由每个希望为特定算法提供密钥工厂实现的加密服务提供者实现。
 *
 * <P> 密钥工厂用于将 <I>密钥</I>（不透明的加密密钥，类型为 {@code Key}）转换为 <I>密钥规范</I>
 * （透明的底层密钥材料表示），反之亦然。
 *
 * <P> 密钥工厂是双向的。也就是说，它们允许您从给定的密钥规范（密钥材料）构建一个不透明的密钥对象，
 * 或者以合适的格式检索密钥对象的底层密钥材料。
 *
 * <P> 同一个密钥可能存在多个兼容的密钥规范。例如，DSA 公钥可以使用
 * {@code DSAPublicKeySpec} 或
 * {@code X509EncodedKeySpec} 指定。密钥工厂可以用于在兼容的密钥规范之间进行转换。
 *
 * <P> 提供者应记录其密钥工厂支持的所有密钥规范。
 *
 * @author Jan Luehe
 *
 *
 * @see KeyFactory
 * @see Key
 * @see PublicKey
 * @see PrivateKey
 * @see java.security.spec.KeySpec
 * @see java.security.spec.DSAPublicKeySpec
 * @see java.security.spec.X509EncodedKeySpec
 *
 * @since 1.2
 */

public abstract class KeyFactorySpi {

    /**
     * 从提供的密钥规范（密钥材料）生成公钥对象。
     *
     * @param keySpec 公钥的规范（密钥材料）。
     *
     * @return 公钥。
     *
     * @exception InvalidKeySpecException 如果给定的密钥规范不适合此密钥工厂生成公钥。
     */
    protected abstract PublicKey engineGeneratePublic(KeySpec keySpec)
        throws InvalidKeySpecException;

    /**
     * 从提供的密钥规范（密钥材料）生成私钥对象。
     *
     * @param keySpec 私钥的规范（密钥材料）。
     *
     * @return 私钥。
     *
     * @exception InvalidKeySpecException 如果给定的密钥规范不适合此密钥工厂生成私钥。
     */
    protected abstract PrivateKey engineGeneratePrivate(KeySpec keySpec)
        throws InvalidKeySpecException;

    /**
     * 返回给定密钥对象的规范（密钥材料）。
     * {@code keySpec} 用于标识应返回的密钥材料的规范类。例如，可以是
     * {@code DSAPublicKeySpec.class}，表示密钥材料应返回为
     * {@code DSAPublicKeySpec} 类的实例。
     *
     * @param <T> 要返回的密钥规范的类型
     *
     * @param key 密钥。
     *
     * @param keySpec 应返回密钥材料的规范类。
     *
     * @return 请求的规范类的实例中的底层密钥规范（密钥材料）。
     *
     * @exception InvalidKeySpecException 如果请求的密钥规范不适合给定的密钥，或者给定的密钥无法处理
     * （例如，给定的密钥具有无法识别的格式）。
     */
    protected abstract <T extends KeySpec>
        T engineGetKeySpec(Key key, Class<T> keySpec)
        throws InvalidKeySpecException;

    /**
     * 将一个提供者可能未知或潜在不可信的密钥对象转换为与此密钥工厂对应的密钥对象。
     *
     * @param key 提供者未知或不可信的密钥。
     *
     * @return 转换后的密钥。
     *
     * @exception InvalidKeyException 如果给定的密钥无法由此密钥工厂处理。
     */
    protected abstract Key engineTranslateKey(Key key)
        throws InvalidKeyException;

}
