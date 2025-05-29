/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
 * <p>对 {@code CertPath} 中的每个 {@code Certificate} 执行一个或多个检查。
 *
 * <p>{@code CertPathChecker} 实现通常用于扩展认证路径验证算法。例如，一个实现可能检查并处理认证路径中每个证书的关键私有扩展。
 *
 * @since 1.8
 */
public interface CertPathChecker {

    /**
     * 初始化此 {@code CertPathChecker} 的内部状态。
     *
     * <p>{@code forward} 标志指定了证书将被传递给 {@link #check check} 方法的顺序（正向或反向）。
     *
     * @param forward 证书呈现给 {@code check} 方法的顺序。如果为 {@code true}，证书从目标到信任锚点（正向）呈现；如果为 {@code false}，从信任锚点到目标（反向）呈现。
     * @throws CertPathValidatorException 如果此 {@code CertPathChecker} 无法以指定顺序检查证书
     */
    void init(boolean forward) throws CertPathValidatorException;

    /**
     * 指示是否支持正向检查。正向检查是指 {@code CertPathChecker} 在证书以正向顺序（从目标到信任锚点）呈现给 {@code check} 方法时执行其检查的能力。
     *
     * @return 如果支持正向检查，则返回 {@code true}，否则返回 {@code false}
     */
    boolean isForwardCheckingSupported();

    /**
     * 使用其内部状态对指定的证书执行检查。证书的呈现顺序由 {@code init} 方法指定。
     *
     * @param cert 要检查的 {@code Certificate}
     * @throws CertPathValidatorException 如果指定的证书未通过检查
     */
    void check(Certificate cert) throws CertPathValidatorException;
}
