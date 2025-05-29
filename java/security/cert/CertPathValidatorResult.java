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
 * 证书路径验证算法结果的规范。
 * <p>
 * 此接口的目的是将所有证书路径验证结果分组（并提供类型安全）。由
 * {@link CertPathValidator#validate CertPathValidator.validate}
 * 方法返回的所有结果都必须实现此接口。
 *
 * @see CertPathValidator
 *
 * @since       1.4
 * @author      Yassir Elley
 */
public interface CertPathValidatorResult extends Cloneable {

    /**
     * 创建此 {@code CertPathValidatorResult} 的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CertPathValidatorResult} 的副本
     */
    Object clone();
}
