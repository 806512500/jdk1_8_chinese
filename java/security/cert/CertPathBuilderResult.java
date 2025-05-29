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

/**
 * 认证路径构建算法结果的规范。
 * {@link CertPathBuilder#build CertPathBuilder.build} 方法返回的所有结果都必须实现此接口。
 * <p>
 * 至少，一个 {@code CertPathBuilderResult} 包含由 {@code CertPathBuilder} 实例构建的 {@code CertPath}。
 * 该接口的实现可以添加方法以返回特定于实现或算法的信息，例如调试信息或认证路径验证结果。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则此接口中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
 *
 * @see CertPathBuilder
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public interface CertPathBuilderResult extends Cloneable {

    /**
     * 返回构建的认证路径。
     *
     * @return 认证路径（从不为 {@code null}）
     */
    CertPath getCertPath();

    /**
     * 复制此 {@code CertPathBuilderResult}。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CertPathBuilderResult} 的副本
     */
    Object clone();
}
