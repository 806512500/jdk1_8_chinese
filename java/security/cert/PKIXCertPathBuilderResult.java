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
 * 该类表示 PKIX 认证路径构建算法的成功结果。使用此算法构建并返回的所有认证路径也根据 PKIX 认证路径验证算法进行验证。
 *
 * <p>{@code PKIXCertPathBuilderResult} 的实例由实现 PKIX 算法的 {@code CertPathBuilder} 对象的 {@code build} 方法返回。
 *
 * <p>所有 {@code PKIXCertPathBuilderResult} 对象包含由构建算法构造的认证路径、构建算法生成的有效策略树和主体公钥，以及描述作为认证路径的信任锚点的认证机构 (CA) 的 {@code TrustAnchor}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则此类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。每个操作单独对象的多个线程不需要同步。
 *
 * @see CertPathBuilderResult
 *
 * @since       1.4
 * @author      Anne Anderson
 */
public class PKIXCertPathBuilderResult extends PKIXCertPathValidatorResult
    implements CertPathBuilderResult {

    private CertPath certPath;

    /**
     * 创建一个包含指定参数的 {@code PKIXCertPathBuilderResult} 实例。
     *
     * @param certPath 验证的 {@code CertPath}
     * @param trustAnchor 描述作为认证路径的信任锚点的 CA 的 {@code TrustAnchor}
     * @param policyTree 不可变的有效策略树，如果没有有效策略则为 {@code null}
     * @param subjectPublicKey 主体的公钥
     * @throws NullPointerException 如果 {@code certPath}、
     * {@code trustAnchor} 或 {@code subjectPublicKey} 参数为 {@code null}
     */
    public PKIXCertPathBuilderResult(CertPath certPath,
        TrustAnchor trustAnchor, PolicyNode policyTree,
        PublicKey subjectPublicKey)
    {
        super(trustAnchor, policyTree, subjectPublicKey);
        if (certPath == null)
            throw new NullPointerException("certPath must be non-null");
        this.certPath = certPath;
    }

    /**
     * 返回构建并验证的认证路径。{@code CertPath} 对象不包括信任锚点。
     * 相反，使用 {@link #getTrustAnchor() getTrustAnchor()} 方法来
     * 获取作为认证路径的信任锚点的 {@code TrustAnchor}。
     *
     * @return 构建并验证的 {@code CertPath}（从不为 {@code null}）
     */
    public CertPath getCertPath() {
        return certPath;
    }

    /**
     * 返回此 {@code PKIXCertPathBuilderResult} 的可打印表示形式。
     *
     * @return 描述此 {@code PKIXCertPathBuilderResult} 内容的 {@code String}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PKIXCertPathBuilderResult: [\n");
        sb.append("  认证路径: " + certPath + "\n");
        sb.append("  信任锚点: " + getTrustAnchor().toString() + "\n");
        sb.append("  策略树: " + String.valueOf(getPolicyTree()) + "\n");
        sb.append("  主体公钥: " + getPublicKey() + "\n");
        sb.append("]");
        return sb.toString();
    }
}
