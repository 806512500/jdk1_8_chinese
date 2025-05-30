/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;

/**
 * 《服务提供者接口》（<b>SPI</b>）
 * 用于 {@link CertPathBuilder CertPathBuilder} 类。所有
 * {@code CertPathBuilder} 实现都必须包含一个类（SPI 类），该类扩展此类（{@code CertPathBuilderSpi}）并
 * 实现其所有方法。通常，此类的实例应仅通过 {@code CertPathBuilder} 类访问。有关详细信息，请参阅 Java 加密架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 本类的实例不必受多线程并发访问的保护。需要并发访问单个
 * {@code CertPathBuilderSpi} 实例的线程应自行同步
 * 并在调用包装的 {@code CertPathBuilder} 对象之前提供必要的锁定。
 * <p>
 * 然而，{@code CertPathBuilderSpi} 的实现仍可能遇到并发问题，因为多个线程各自
 * 操控不同的 {@code CertPathBuilderSpi} 实例时无需同步。
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public abstract class CertPathBuilderSpi {

    /**
     * 默认构造函数。
     */
    public CertPathBuilderSpi() { }

    /**
     * 尝试使用指定的算法参数集构建证书路径。
     *
     * @param params 算法参数
     * @return 构建算法的结果
     * @throws CertPathBuilderException 如果构建器无法构建满足指定参数的证书路径
     * @throws InvalidAlgorithmParameterException 如果指定的参数不适合此 {@code CertPathBuilder}
     */
    public abstract CertPathBuilderResult engineBuild(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException;

    /**
     * 返回此实现用于检查证书撤销状态的 {@code CertPathChecker}。PKIX 实现
     * 返回 {@code PKIXRevocationChecker} 类型的对象。
     *
     * <p>此方法的主要目的是允许调用者指定特定于撤销检查的附加输入参数和选项。
     * 有关示例，请参阅 {@code CertPathBuilder} 的类描述。
     *
     * <p>此方法是在 Java 平台标准版 1.8 版本中添加的。为了保持与现有服务提供者的向后兼容性，
     * 此方法不能是抽象的，默认情况下会抛出一个 {@code UnsupportedOperationException}。
     *
     * @return 此实现用于检查证书撤销状态的 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果此方法不受支持
     * @since 1.8
     */
    public CertPathChecker engineGetRevocationChecker() {
        throw new UnsupportedOperationException();
    }
}
