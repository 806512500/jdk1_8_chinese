/*
 * 版权 (c) 2007, Oracle 和/或其附属公司。保留所有权利。
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
 * CRLReason 枚举指定了证书被吊销的原因，如 <a href="http://www.ietf.org/rfc/rfc3280.txt">
 * RFC 3280: Internet X.509 公钥基础设施证书和 CRL
 * 配置文件</a> 中定义的。
 *
 * @author Sean Mullan
 * @since 1.7
 * @see X509CRLEntry#getRevocationReason
 * @see CertificateRevokedException#getRevocationReason
 */
public enum CRLReason {
    /**
     * 此原因表示证书被吊销的原因未指定。
     */
    UNSPECIFIED,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于终端实体证书。
     */
    KEY_COMPROMISE,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于证书颁发机构 (CA) 证书。
     */
    CA_COMPROMISE,

    /**
     * 此原因表示主体的名称或其他信息已更改。
     */
    AFFILIATION_CHANGED,

    /**
     * 此原因表示证书已被替代。
     */
    SUPERSEDED,

    /**
     * 此原因表示证书不再需要。
     */
    CESSATION_OF_OPERATION,

    /**
     * 此原因表示证书已被暂停。
     */
    CERTIFICATE_HOLD,

    /**
     * 未使用的原因。
     */
    UNUSED,

    /**
     * 此原因表示证书之前已被暂停，应从 CRL 中移除。仅用于增量 CRL。
     */
    REMOVE_FROM_CRL,

    /**
     * 此原因表示证书主体的权限已被撤销。
     */
    PRIVILEGE_WITHDRAWN,

    /**
     * 此原因表示已知或怀疑证书主体的私钥已被泄露。仅适用于属性授权 (AA) 证书。
     */
    AA_COMPROMISE
}
