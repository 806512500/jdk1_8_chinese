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

import java.util.Collection;
import java.util.Set;

/**
 * 一个执行一个或多个检查的抽象类，针对 {@code X509Certificate}。
 *
 * <p>可以创建 {@code PKIXCertPathChecker} 类的具体实现以扩展 PKIX 认证路径验证算法。例如，实现可能检查并处理认证路径中每个证书的关键私有扩展。
 *
 * <p>{@code PKIXCertPathChecker} 的实例作为参数传递，使用 {@code PKIXParameters} 和 {@code PKIXBuilderParameters}
 * 类的 {@link PKIXParameters#setCertPathCheckers setCertPathCheckers} 或 {@link PKIXParameters#addCertPathChecker addCertPathChecker} 方法。
 * 每个 {@code PKIXCertPathChecker} 的 {@link #check check} 方法将依次被 PKIX {@code CertPathValidator} 或 {@code CertPathBuilder}
 * 实现调用，以处理每个证书。
 *
 * <p>{@code PKIXCertPathChecker} 可能会被多次调用，以处理认证路径中的连续证书。具体子类应维护任何必要的内部状态，以检查连续的证书。
 * {@link #init init} 方法用于初始化检查器的内部状态，以便检查新认证路径的证书。如果需要，状态化实现 <b>必须</b> 覆盖 {@link #clone clone} 方法，
 * 以便 PKIX {@code CertPathBuilder} 能够高效地回溯并尝试其他路径。在这种情况下，{@code CertPathBuilder} 可以通过恢复克隆的 {@code PKIXCertPathChecker} 来恢复之前的路径验证状态。
 *
 * <p>证书可以按正向（从目标到最信任的 CA）或反向（从最信任的 CA 到目标）的顺序呈现给 {@code PKIXCertPathChecker}。
 * {@code PKIXCertPathChecker} 实现 <b>必须</b> 支持反向检查（当证书以反向顺序呈现时能够执行检查），并且 <b>可以</b> 支持正向检查（当证书以正向顺序呈现时能够执行检查）。
 * {@link #isForwardCheckingSupported isForwardCheckingSupported} 方法指示是否支持正向检查。
 * <p>
 * 执行检查所需的附加输入参数可以通过此类的具体实现的构造函数指定。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则此类定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。
 * 每个操作单独对象的多个线程不需要同步。
 *
 * @see PKIXParameters
 * @see PKIXBuilderParameters
 *
 * @since       1.4
 * @author      Yassir Elley
 * @author      Sean Mullan
 */
public abstract class PKIXCertPathChecker
    implements CertPathChecker, Cloneable {

    /**
     * 默认构造函数。
     */
    protected PKIXCertPathChecker() {}

    /**
     * 初始化此 {@code PKIXCertPathChecker} 的内部状态。
     * <p>
     * {@code forward} 标志指定证书将传递给 {@link #check check} 方法的顺序（正向或反向）。
     * {@code PKIXCertPathChecker} <b>必须</b> 支持反向检查，并 <b>可以</b> 支持正向检查。
     *
     * @param forward 证书呈现给 {@code check} 方法的顺序。如果为 {@code true}，证书从目标到最信任的 CA（正向）；如果为 {@code false}，从最信任的 CA 到目标（反向）。
     * @throws CertPathValidatorException 如果此 {@code PKIXCertPathChecker} 无法以指定顺序检查证书；如果正向标志为 false，则不应抛出此异常，因为必须支持反向检查。
     */
    @Override
    public abstract void init(boolean forward)
        throws CertPathValidatorException;

    /**
     * 指示是否支持正向检查。正向检查是指 {@code PKIXCertPathChecker} 在证书以正向顺序（从目标到最信任的 CA）呈现给 {@code check} 方法时执行检查的能力。
     *
     * @return 如果支持正向检查，则返回 {@code true}，否则返回 {@code false}。
     */
    @Override
    public abstract boolean isForwardCheckingSupported();

    /**
     * 返回此 {@code PKIXCertPathChecker} 支持的（即识别并能够处理的）X.509 证书扩展的不可变 {@code Set}，或如果没有任何扩展支持，则返回 {@code null}。
     * <p>
     * 集合中的每个元素都是一个表示支持的 X.509 扩展的对象标识符（OID）的 {@code String}。OID 由一组非负整数表示，用句点分隔。
     * <p>
     * {@code PKIXCertPathChecker} 可能能够处理的所有 X.509 证书扩展都应包含在集合中。
     *
     * @return 此 {@code PKIXCertPathChecker} 支持的 X.509 扩展 OID（以 {@code String} 格式）的不可变 {@code Set}，或如果没有任何扩展支持，则返回 {@code null}。
     */
    public abstract Set<String> getSupportedExtensions();

    /**
     * 使用其内部状态对指定的证书执行检查，并从表示未解决的关键扩展的 OID 字符串集合中移除任何已处理的关键扩展。
     * 证书按 {@code init} 方法指定的顺序呈现。
     *
     * @param cert 要检查的 {@code Certificate}
     * @param unresolvedCritExts 表示当前未解决的关键扩展的 OID 字符串的 {@code Collection}
     * @exception CertPathValidatorException 如果指定的证书未通过检查
     */
    public abstract void check(Certificate cert,
            Collection<String> unresolvedCritExts)
            throws CertPathValidatorException;

    /**
     * {@inheritDoc}
     *
     * <p>此实现调用 {@code check(cert, java.util.Collections.<String>emptySet())}。
     */
    @Override
    public void check(Certificate cert) throws CertPathValidatorException {
        check(cert, java.util.Collections.<String>emptySet());
    }

    /**
     * 返回此对象的克隆。调用 {@code Object.clone()} 方法。
     * 所有维护状态的子类必须支持并覆盖此方法，如果需要的话。
     *
     * @return 此 {@code PKIXCertPathChecker} 的副本
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }
}
