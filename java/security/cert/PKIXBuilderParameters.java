
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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.util.Set;

/**
 * 用于 PKIX {@code CertPathBuilder} 算法的输入参数。
 * <p>
 * PKIX {@code CertPathBuilder} 使用这些参数来 {@link
 * CertPathBuilder#build 构建} 一个已根据 PKIX 认证路径验证算法验证的 {@code CertPath}。
 *
 * <p>要实例化一个 {@code PKIXBuilderParameters} 对象，应用程序必须指定一个或多个由 PKIX 认证路径验证算法定义的<i>最可信的 CA</i>。最可信的 CA 可以使用两种构造函数之一指定。应用程序可以调用 {@link #PKIXBuilderParameters(Set, CertSelector)
 * PKIXBuilderParameters(Set, CertSelector)}，指定一个包含 {@code TrustAnchor} 对象的 {@code Set}，每个对象都标识一个最可信的 CA。或者，应用程序可以调用 {@link #PKIXBuilderParameters(KeyStore, CertSelector)
 * PKIXBuilderParameters(KeyStore, CertSelector)}，指定一个包含受信任证书条目的 {@code KeyStore} 实例，每个条目都将被视为一个最可信的 CA。
 *
 * <p>此外，应用程序必须指定 {@code CertPathBuilder} 将尝试构建路径的目标证书的约束条件。这些约束条件指定为一个 {@code CertSelector} 对象。这些约束条件应为 {@code CertPathBuilder} 提供足够的搜索标准以找到目标证书。对于 {@code X509Certificate}，通常的最小标准包括主题名称和/或一个或多个主题备用名称。如果未指定足够的标准，{@code CertPathBuilder} 可能会抛出一个 {@code CertPathBuilderException}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。每个操作单独对象的多个线程不需要同步。
 *
 * @see CertPathBuilder
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public class PKIXBuilderParameters extends PKIXParameters {

    private int maxPathLength = 5;

    /**
     * 使用指定的 {@code Set} 最可信的 CA 创建 {@code PKIXBuilderParameters} 的实例。
     * 集合中的每个元素都是一个 {@link TrustAnchor TrustAnchor}。
     *
     * <p>注意，集合会被复制以防止后续修改。
     *
     * @param trustAnchors 一个包含 {@code TrustAnchor} 的 {@code Set}
     * @param targetConstraints 一个 {@code CertSelector}，指定目标证书的约束条件
     * @throws InvalidAlgorithmParameterException 如果 {@code trustAnchors} 为空 {@code (trustAnchors.isEmpty() == true)}
     * @throws NullPointerException 如果 {@code trustAnchors} 为 {@code null}
     * @throws ClassCastException 如果 {@code trustAnchors} 中的任何元素不是 {@code java.security.cert.TrustAnchor} 类型
     */
    public PKIXBuilderParameters(Set<TrustAnchor> trustAnchors, CertSelector
        targetConstraints) throws InvalidAlgorithmParameterException
    {
        super(trustAnchors);
        setTargetCertConstraints(targetConstraints);
    }

    /**
     * 创建一个 {@code PKIXBuilderParameters} 实例，从指定的 {@code KeyStore} 中包含的受信任证书条目填充最可信的 CA 集合。
     * 只考虑包含受信任的 {@code X509Certificate} 的密钥库条目；所有其他证书类型将被忽略。
     *
     * @param keystore 一个 {@code KeyStore}，从中填充最可信的 CA 集合
     * @param targetConstraints 一个 {@code CertSelector}，指定目标证书的约束条件
     * @throws KeyStoreException 如果 {@code keystore} 未初始化
     * @throws InvalidAlgorithmParameterException 如果 {@code keystore} 不包含至少一个受信任的证书条目
     * @throws NullPointerException 如果 {@code keystore} 为 {@code null}
     */
    public PKIXBuilderParameters(KeyStore keystore,
        CertSelector targetConstraints)
        throws KeyStoreException, InvalidAlgorithmParameterException
    {
        super(keystore);
        setTargetCertConstraints(targetConstraints);
    }

    /**
     * 设置认证路径中可能存在的非自签发中间证书的最大数量。如果证书的主体和颁发者字段中的 DNs 相同且不为空，则该证书为自签发。注意，认证路径中的最后一个证书不是中间证书，不包括在此限制内。通常，认证路径中的最后一个证书是终端实体证书，但也可以是 CA 证书。PKIX {@code CertPathBuilder} 实例不得构建超过指定长度的路径。
     *
     * <p>值为 0 表示路径只能包含一个证书。值为 -1 表示路径长度不受限制（即没有最大值）。如果未指定，默认的最大路径长度为 5。设置小于 -1 的值将导致抛出异常。
     *
     * <p>如果任何 CA 证书包含 {@code BasicConstraintsExtension}，则扩展中的 {@code pathLenConstraint} 字段的值在结果为更短的认证路径时将覆盖最大路径长度参数。
     *
     * @param maxPathLength 认证路径中可能存在的非自签发中间证书的最大数量
     * @throws InvalidParameterException 如果 {@code maxPathLength} 设置为小于 -1 的值
     *
     * @see #getMaxPathLength
     */
    public void setMaxPathLength(int maxPathLength) {
        if (maxPathLength < -1) {
            throw new InvalidParameterException("最大路径长度参数不能小于 -1");
        }
        this.maxPathLength = maxPathLength;
    }

                /**
     * 返回认证路径中可能存在的最大中间非自签名证书数量。有关更多详细信息，请参阅
     * {@link #setMaxPathLength} 方法。
     *
     * @return 认证路径中可能存在的最大非自签名中间证书数量，如果无限制则返回 -1
     *
     * @see #setMaxPathLength
     */
    public int getMaxPathLength() {
        return maxPathLength;
    }

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        sb.append(super.toString());
        sb.append("  Maximum Path Length: " + maxPathLength + "\n");
        sb.append("]\n");
        return sb.toString();
    }
}
