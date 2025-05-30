/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Set;

/**
 * 此接口指定加密算法、密钥（密钥大小）和其他算法参数的约束。
 * <p>
 * {@code AlgorithmConstraints} 对象是不可变的。此接口的实现不应提供可以改变实例状态的方法，一旦实例被创建。
 * <p>
 * 注意，{@code AlgorithmConstraints} 可以用于表示由安全属性
 * {@code jdk.certpath.disabledAlgorithms} 和
 * {@code jdk.tls.disabledAlgorithms} 描述的限制，或者可以由具体的
 * {@code PKIXCertPathChecker} 用于检查认证路径中指定的证书是否包含所需的算法约束。
 *
 * @see javax.net.ssl.SSLParameters#getAlgorithmConstraints
 * @see javax.net.ssl.SLLParameters#setAlgorithmConstraints(AlgorithmConstraints)
 *
 * @since 1.7
 */

public interface AlgorithmConstraints {

    /**
     * 确定指定的加密原语是否被授予算法的权限。
     *
     * @param primitives 一组加密原语
     * @param algorithm 算法名称
     * @param parameters 算法参数，如果不需要额外参数则为 null
     *
     * @return 如果算法被允许并且可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 或 algorithm 为 null 或空
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
            String algorithm, AlgorithmParameters parameters);

    /**
     * 确定指定的加密原语是否被授予密钥的权限。
     * <p>
     * 通常用于检查密钥大小和密钥用途。
     *
     * @param primitives 一组加密原语
     * @param key 密钥
     *
     * @return 如果密钥可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 为 null 或空，或者 key 为 null
     */
    public boolean permits(Set<CryptoPrimitive> primitives, Key key);

    /**
     * 确定指定的加密原语是否被授予算法及其对应密钥的权限。
     *
     * @param primitives 一组加密原语
     * @param algorithm 算法名称
     * @param key 密钥
     * @param parameters 算法参数，如果不需要额外参数则为 null
     *
     * @return 如果密钥和算法可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 或 algorithm 为 null 或空，或者 key 为 null
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
                String algorithm, Key key, AlgorithmParameters parameters);

}
