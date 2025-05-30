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
 *
 * 《服务提供者接口》（<b>SPI</b>）
 * 用于 {@link CertPathValidator CertPathValidator} 类。所有
 * {@code CertPathValidator} 实现都必须包含一个类（SPI 类），该类扩展了此类（{@code CertPathValidatorSpi}）
 * 并实现其所有方法。通常，此类的实例应仅通过 {@code CertPathValidator} 类访问。
 * 有关详细信息，请参阅 Java 密码架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 本类的实例不必受多个线程并发访问的保护。需要并发访问单个
 * {@code CertPathValidatorSpi} 实例的线程应自行同步
 * 并在调用包装的 {@code CertPathValidator} 对象之前提供必要的锁定。
 * <p>
 * 然而，{@code CertPathValidatorSpi} 的实现仍可能遇到并发问题，因为多个线程各自
 * 操纵不同的 {@code CertPathValidatorSpi} 实例时不需要同步。
 *
 * @since       1.4
 * @author      Yassir Elley
 */
public abstract class CertPathValidatorSpi {

    /**
     * 默认构造函数。
     */
    public CertPathValidatorSpi() {}

    /**
     * 使用指定的算法参数集验证指定的证书路径。
     * <p>
     * 指定的 {@code CertPath} 必须是验证算法支持的类型，否则将抛出
     * {@code InvalidAlgorithmParameterException}。例如，实现 PKIX
     * 算法的 {@code CertPathValidator} 验证类型为 X.509 的 {@code CertPath} 对象。
     *
     * @param certPath 要验证的 {@code CertPath}
     * @param params 算法参数
     * @return 验证算法的结果
     * @exception CertPathValidatorException 如果 {@code CertPath}
     * 无法验证
     * @exception InvalidAlgorithmParameterException 如果指定的
     * 参数或指定的 {@code CertPath} 类型不适用于此 {@code CertPathValidator}
     */
    public abstract CertPathValidatorResult
        engineValidate(CertPath certPath, CertPathParameters params)
        throws CertPathValidatorException, InvalidAlgorithmParameterException;

    /**
     * 返回此实现用于检查证书撤销状态的 {@code CertPathChecker}。PKIX 实现
     * 返回类型为 {@code PKIXRevocationChecker} 的对象。
     *
     * <p>此方法的主要目的是允许调用者指定
     * 与撤销检查相关的附加输入参数和选项。有关示例，请参阅
     * {@code CertPathValidator} 类的描述。
     *
     * <p>此方法是在 Java 平台标准版 1.8 版本中添加的。为了保持与现有
     * 服务提供者的向后兼容性，此方法不能是抽象的，默认情况下抛出
     * {@code UnsupportedOperationException}。
     *
     * @return 此实现用于检查证书撤销状态的 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果此方法不受支持
     * @since 1.8
     */
    public CertPathChecker engineGetRevocationChecker() {
        throw new UnsupportedOperationException();
    }
}
