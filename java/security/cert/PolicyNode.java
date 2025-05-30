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

import java.util.Iterator;
import java.util.Set;

/**
 * 由 PKIX 证书路径验证算法定义的不可变有效策略树节点。
 *
 * <p>PKIX 证书路径验证算法的一个输出是有效策略树，该树包括确定为有效的策略、如何做出此确定以及遇到的任何策略限定符。该树的深度为 <i>n</i>，其中 <i>n</i> 是已验证的证书路径的长度。
 *
 * <p>大多数应用程序不需要检查有效策略树。它们可以通过在 {@code PKIXParameters} 中设置与策略相关的参数来实现其策略处理目标。但是，有效策略树可用于更复杂的应用程序，特别是那些处理策略限定符的应用程序。
 *
 * <p>{@link PKIXCertPathValidatorResult#getPolicyTree()
 * PKIXCertPathValidatorResult.getPolicyTree} 返回有效策略树的根节点。可以使用 {@link #getChildren getChildren} 和 {@link #getParent getParent} 方法遍历该树。可以使用 {@code PolicyNode} 的其他方法检索特定节点的数据。
 *
 * <p><b>并发访问</b>
 * <p>所有 {@code PolicyNode} 对象都必须是不可变的和线程安全的。多个线程可以同时调用此类定义的方法（或多个）上的单个 {@code PolicyNode} 对象，而不会产生不良影响。此规定适用于此类的所有公共字段和方法以及子类添加或覆盖的任何方法。
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public interface PolicyNode {

    /**
     * 返回此节点的父节点，如果这是根节点，则返回 {@code null}。
     *
     * @return 此节点的父节点，如果这是根节点，则返回 {@code null}
     */
    PolicyNode getParent();

    /**
     * 返回此节点的子节点的迭代器。任何尝试通过 {@code Iterator} 的 remove 方法修改此节点的子节点都必须抛出 {@code UnsupportedOperationException}。
     *
     * @return 此节点的子节点的迭代器
     */
    Iterator<? extends PolicyNode> getChildren();

    /**
     * 返回此节点在有效策略树中的深度。
     *
     * @return 此节点的深度（根节点为 0，其子节点为 1，依此类推）
     */
    int getDepth();

    /**
     * 返回此节点表示的有效策略。
     *
     * @return 此节点表示的有效策略的 {@code String} OID。对于根节点，此方法始终返回特殊的 anyPolicy OID："2.5.29.32.0"。
     */
    String getValidPolicy();

    /**
     * 返回与此节点表示的有效策略关联的策略限定符集。
     *
     * @return 不可变的 {@code Set}，包含 {@code PolicyQualifierInfo}。对于根节点，这始终是一个空的 {@code Set}。
     */
    Set<? extends PolicyQualifierInfo> getPolicyQualifiers();

    /**
     * 返回在处理下一个证书时满足此节点有效策略的预期策略集。
     *
     * @return 不可变的 {@code Set}，包含预期策略的 {@code String} OID。对于根节点，此方法始终返回一个包含一个元素的 {@code Set}，即特殊的 anyPolicy OID："2.5.29.32.0"。
     */
    Set<String> getExpectedPolicies();

    /**
     * 返回最近处理的证书中证书策略扩展的关键性指示符。
     *
     * @return 如果扩展标记为关键，则返回 {@code true}，否则返回 {@code false}。对于根节点，始终返回 {@code false}。
     */
    boolean isCritical();
}
