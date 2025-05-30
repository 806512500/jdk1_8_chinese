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

package java.security.interfaces;

import java.security.*;

/**
 * 一个能够生成 DSA 密钥对的对象的接口。
 *
 * <p>{@code initialize} 方法可以调用任意次数。如果 DSAKeyPairGenerator 没有调用任何 {@code initialize} 方法，默认是生成 1024 位的密钥，使用预计算的 p、q 和 g 参数，并使用 SecureRandom 实例作为随机位源。
 *
 * <p>希望指定 DSA 特定参数并生成适合 DSA 算法的密钥对的用户通常会：
 *
 * <ol>
 *
 * <li>通过调用 KeyPairGenerator 的 {@code getInstance} 方法并传入 "DSA" 作为参数来获取 DSA 算法的密钥对生成器。
 *
 * <li>通过将结果转换为 DSAKeyPairGenerator 并调用此 DSAKeyPairGenerator 接口中的一个 {@code initialize} 方法来初始化生成器。
 *
 * <li>通过调用 KeyPairGenerator 类中的 {@code generateKeyPair} 方法来生成密钥对。
 *
 * </ol>
 *
 * <p>注意：有时不需要对 DSA 密钥对生成器进行算法特定的初始化。也就是说，有时不需要调用此接口中的 {@code initialize} 方法。当接受算法特定参数的默认值时，使用 KeyPairGenerator 接口中的 {@code initialize} 方法进行算法独立的初始化就足够了。
 *
 * <p>注意：某些早期实现可能不支持较大的 DSA 参数，如 2048 位和 3072 位。
 *
 * @see java.security.KeyPairGenerator
 */
public interface DSAKeyPairGenerator {

    /**
     * 使用 DSA 家族参数 (p, q 和 g) 和一个可选的 SecureRandom 位源初始化密钥对生成器。如果需要 SecureRandom 位源但未提供，即为 null，则使用默认的 SecureRandom 实例。
     *
     * @param params 用于生成密钥的参数。
     *
     * @param random 用于生成密钥位的随机位源；可以为 null。
     *
     * @exception InvalidParameterException 如果 {@code params} 值无效、为 null 或不受支持。
     */
   public void initialize(DSAParams params, SecureRandom random)
   throws InvalidParameterException;

    /**
     * 使用给定的模长（而不是参数）和一个可选的 SecureRandom 位源初始化密钥对生成器。如果需要 SecureRandom 位源但未提供，即为 null，则使用默认的 SecureRandom 实例。
     *
     * <p>如果 {@code genParams} 为 true，此方法将生成新的 p、q 和 g 参数。如果为 false，则使用请求的模长的预计算参数。如果请求的模长没有预计算参数，则会抛出异常。可以保证 512 位和 1024 位模长始终有默认参数。
     *
     * @param modlen 模长（以位为单位）。有效值是 512 到 1024 之间的 64 的倍数，包括 2048 和 3072。
     *
     * @param random 用于生成密钥位的随机位源；可以为 null。
     *
     * @param genParams 是否为请求的模长生成新的参数。
     *
     * @exception InvalidParameterException 如果 {@code modlen} 无效或不受支持，或者如果 {@code genParams} 为 false 且没有请求的模长的预计算参数。
     */
    public void initialize(int modlen, boolean genParams, SecureRandom random)
    throws InvalidParameterException;
}
