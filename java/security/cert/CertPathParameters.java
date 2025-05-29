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
 * 认证路径算法参数的规范。
 * 该接口的目的是将所有 {@code CertPath} 参数规范分组（并提供类型安全）。所有
 * {@code CertPath} 参数规范都必须实现此接口。
 *
 * @author      Yassir Elley
 * @see         CertPathValidator#validate(CertPath, CertPathParameters)
 * @see         CertPathBuilder#build(CertPathParameters)
 * @since       1.4
 */
public interface CertPathParameters extends Cloneable {

  /**
   * 创建此 {@code CertPathParameters} 的副本。对副本的修改不会影响原始对象，反之亦然。
   *
   * @return 此 {@code CertPathParameters} 的副本
   */
  Object clone();
}
