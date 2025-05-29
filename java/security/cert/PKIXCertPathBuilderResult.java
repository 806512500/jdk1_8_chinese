/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

import java.security.PublicKey;

/**
 * 该类表示 PKIX 证书路径构建算法的成功结果。使用此算法构建和返回的所有证书路径也根据 PKIX 证书路径验证算法进行验证。
 *
 * <p>{@code PKIXCertPathBuilderResult} 的实例由实现 PKIX 算法的 {@code CertPathBuilder} 对象的 {@code build} 方法返回。
 *
 * <p>所有 {@code PKIXCertPathBuilderResult} 对象包含由构建算法构造的证书路径、构建算法产生的有效策略树和主体公钥，以及描述作为证书路径信任锚点的证书颁发机构 (CA) 的 {@code TrustAnchor}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应相互同步并提供必要的锁定。每个操作单独对象的多个线程不需要同步。
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
     * @param trustAnchor 描述作为证书路径信任锚点的 CA 的 {@code TrustAnchor}
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
     * 返回构建和验证的证书路径。{@code CertPath} 对象不包括信任锚点。
     * 相反，使用 {@link #getTrustAnchor() getTrustAnchor()} 方法来
     * 获取作为证书路径信任锚点的 {@code TrustAnchor}。
     *
     * @return 构建和验证的 {@code CertPath}（从不为 {@code null}）
     */
    public CertPath getCertPath() {
        return certPath;
    }

    /**
     * 返回此 {@code PKIXCertPathBuilderResult} 的可打印表示。
     *
     * @return 描述此 {@code PKIXCertPathBuilderResult} 内容的 {@code String}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PKIXCertPathBuilderResult: [\n");
        sb.append("  Certification Path: " + certPath + "\n");
        sb.append("  Trust Anchor: " + getTrustAnchor().toString() + "\n");
        sb.append("  Policy Tree: " + String.valueOf(getPolicyTree()) + "\n");
        sb.append("  Subject Public Key: " + getPublicKey() + "\n");
        sb.append("]");
        return sb.toString();
    }
}
