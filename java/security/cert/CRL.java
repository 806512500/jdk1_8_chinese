/*
 * 版权所有 (c) 1998, 2006, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 该类是对具有不同格式但重要共同用途的证书吊销列表 (CRLs) 的抽象。例如，所有 CRLs
 * 都具有列出已吊销证书的功能，并且可以查询它们是否列出了给定的证书。
 * <p>
 * 可以通过继承此抽象类来定义专门的 CRL 类型。
 *
 * @author Hemma Prafullchandra
 *
 *
 * @see X509CRL
 * @see CertificateFactory
 *
 * @since 1.2
 */

public abstract class CRL {

    // CRL 类型
    private String type;

    /**
     * 创建指定类型的 CRL。
     *
     * @param type CRL 类型的标准名称。
     * 有关标准 CRL 类型的信息，请参阅 <a href=
     * "../../../../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java 加密架构 API 规范和参考 </a> 附录 A。
     */
    protected CRL(String type) {
        this.type = type;
    }

    /**
     * 返回此 CRL 的类型。
     *
     * @return 此 CRL 的类型。
     */
    public final String getType() {
        return this.type;
    }

    /**
     * 返回此 CRL 的字符串表示形式。
     *
     * @return 此 CRL 的字符串表示形式。
     */
    public abstract String toString();

    /**
     * 检查给定的证书是否在此 CRL 上。
     *
     * @param cert 要检查的证书。
     * @return 如果给定的证书在此 CRL 上，则返回 true，否则返回 false。
     */
    public abstract boolean isRevoked(Certificate cert);
}
