/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;

/**
 * 为 {@link CertPathBuilder CertPathBuilder} 类提供的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 所有 {@code CertPathBuilder} 实现都必须包含一个扩展此类 ({@code CertPathBuilderSpi}) 的类（SPI 类）并实现其所有方法。
 * 通常，此类的实例应仅通过 {@code CertPathBuilder} 类访问。详情请参阅 Java 密码架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 本类的实例无需保护以防止多个线程的并发访问。需要并发访问单个 {@code CertPathBuilderSpi} 实例的线程应自行同步，并在调用包装的 {@code CertPathBuilder} 对象之前提供必要的锁定。
 * <p>
 * 然而，{@code CertPathBuilderSpi} 的实现仍可能遇到并发问题，因为每个操作不同 {@code CertPathBuilderSpi} 实例的多个线程无需同步。
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
     * @throws CertPathBuilderException 如果构建器无法构造满足指定参数的证书路径
     * @throws InvalidAlgorithmParameterException 如果指定的参数对于此 {@code CertPathBuilder} 不合适
     */
    public abstract CertPathBuilderResult engineBuild(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException;

    /**
     * 返回此实现用于检查证书撤销状态的 {@code CertPathChecker}。PKIX 实现返回类型为 {@code PKIXRevocationChecker} 的对象。
     *
     * <p>此方法的主要目的是允许调用者指定特定于撤销检查的附加输入参数和选项。有关示例，请参阅 {@code CertPathBuilder} 的类描述。
     *
     * <p>此方法是在 Java 平台标准版 1.8 版本中添加的。为了保持与现有服务提供者的向后兼容性，此方法不能是抽象的，默认情况下会抛出一个 {@code UnsupportedOperationException}。
     *
     * @return 本实现用于检查证书撤销状态的 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果此方法不受支持
     * @since 1.8
     */
    public CertPathChecker engineGetRevocationChecker() {
        throw new UnsupportedOperationException();
    }
}
