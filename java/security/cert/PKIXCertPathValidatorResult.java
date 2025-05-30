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

import java.security.PublicKey;

/**
 * 该类表示 PKIX 证书路径验证算法的成功结果。
 *
 * <p>{@code PKIXCertPathValidatorResult} 的实例由实现 PKIX 算法的
 * {@code CertPathValidator} 对象的 {@link CertPathValidator#validate validate} 方法返回。
 *
 * <p>所有 {@code PKIXCertPathValidatorResult} 对象包含验证算法生成的有效策略树和主体公钥，
 * 以及描述作为证书路径信任锚点的证书颁发机构 (CA) 的 {@code TrustAnchor}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则此类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。
 * 每个操作独立对象的多个线程不需要同步。
 *
 * @see CertPathValidatorResult
 *
 * @since       1.4
 * @author      Yassir Elley
 * @author      Sean Mullan
 */
public class PKIXCertPathValidatorResult implements CertPathValidatorResult {

    private TrustAnchor trustAnchor;
    private PolicyNode policyTree;
    private PublicKey subjectPublicKey;

    /**
     * 创建一个包含指定参数的 {@code PKIXCertPathValidatorResult} 实例。
     *
     * @param trustAnchor 描述作为证书路径信任锚点的 CA 的 {@code TrustAnchor}
     * @param policyTree 不可变的有效策略树，如果没有有效策略则为 {@code null}
     * @param subjectPublicKey 主体的公钥
     * @throws NullPointerException 如果 {@code subjectPublicKey} 或
     * {@code trustAnchor} 参数为 {@code null}
     */
    public PKIXCertPathValidatorResult(TrustAnchor trustAnchor,
        PolicyNode policyTree, PublicKey subjectPublicKey)
    {
        if (subjectPublicKey == null)
            throw new NullPointerException("subjectPublicKey must be non-null");
        if (trustAnchor == null)
            throw new NullPointerException("trustAnchor must be non-null");
        this.trustAnchor = trustAnchor;
        this.policyTree = policyTree;
        this.subjectPublicKey = subjectPublicKey;
    }

    /**
     * 返回描述作为证书路径信任锚点的 CA 的 {@code TrustAnchor}。
     *
     * @return {@code TrustAnchor}（从不为 {@code null}）
     */
    public TrustAnchor getTrustAnchor() {
        return trustAnchor;
    }

    /**
     * 返回 PKIX 证书路径验证算法生成的有效策略树的根节点。返回的
     * {@code PolicyNode} 对象及其通过公共方法返回的任何对象都是不可变的。
     *
     * <p>大多数应用程序不需要检查有效策略树。它们可以通过在
     * {@code PKIXParameters} 中设置与策略相关的参数来实现其策略处理目标。但是，更复杂的应用程序，
     * 尤其是处理策略限定符的应用程序，可能需要使用
     * {@link PolicyNode#getParent PolicyNode.getParent} 和
     * {@link PolicyNode#getChildren PolicyNode.getChildren} 方法遍历有效策略树。
     *
     * @return 有效策略树的根节点，如果没有有效策略则为 {@code null}
     */
    public PolicyNode getPolicyTree() {
        return policyTree;
    }

    /**
     * 返回证书路径的主体（目标）的公钥，包括适用的继承公钥参数。
     *
     * @return 主体的公钥（从不为 {@code null}）
     */
    public PublicKey getPublicKey() {
        return subjectPublicKey;
    }

    /**
     * 返回此对象的副本。
     *
     * @return 副本
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* 不会发生 */
            throw new InternalError(e.toString(), e);
        }
    }

    /**
     * 返回此 {@code PKIXCertPathValidatorResult} 的可打印表示形式。
     *
     * @return 描述此 {@code PKIXCertPathValidatorResult} 内容的 {@code String}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PKIXCertPathValidatorResult: [\n");
        sb.append("  Trust Anchor: " + trustAnchor.toString() + "\n");
        sb.append("  Policy Tree: " + String.valueOf(policyTree) + "\n");
        sb.append("  Subject Public Key: " + subjectPublicKey + "\n");
        sb.append("]");
        return sb.toString();
    }
}
