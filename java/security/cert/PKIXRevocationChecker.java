
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 用于使用 PKIX 算法检查证书撤销状态的 {@code PKIXCertPathChecker}。
 *
 * <p>{@code PKIXRevocationChecker} 使用在线证书状态协议 (OCSP) 或证书撤销列表 (CRL) 检查证书的撤销状态。OCSP 在 RFC 2560 中描述，是一种用于确定证书状态的网络协议。CRL 是一个带有时间戳的列表，列出了被撤销的证书，RFC 5280 描述了使用 CRL 确定证书撤销状态的算法。
 *
 * <p>每个 {@code PKIXRevocationChecker} 必须能够使用 OCSP 和 CRL 检查证书的撤销状态。默认情况下，OCSP 是检查撤销状态的首选机制，CRL 是备用机制。但是，这种偏好可以通过 {@link Option#PREFER_CRLS PREFER_CRLS} 选项切换到 CRL。此外，可以通过 {@link Option#NO_FALLBACK NO_FALLBACK} 选项禁用备用机制。
 *
 * <p>通过调用 PKIX {@code CertPathValidator} 的 {@link CertPathValidator#getRevocationChecker getRevocationChecker} 方法来获取 {@code PKIXRevocationChecker}。可以设置特定于撤销的附加参数和选项（例如调用 {@link #setOcspResponder setOcspResponder} 方法）。使用 {@link PKIXParameters#addCertPathChecker addCertPathChecker} 或 {@link PKIXParameters#setCertPathCheckers setCertPathCheckers} 方法将 {@code PKIXRevocationChecker} 添加到 {@code PKIXParameters} 对象中，然后将 {@code PKIXParameters} 与要验证的 {@code CertPath} 一起传递给 PKIX {@code CertPathValidator} 的 {@link CertPathValidator#validate validate} 方法。以这种方式提供撤销检查器时，无论 {@link PKIXParameters#isRevocationEnabled RevocationEnabled} 标志的设置如何，都将使用该撤销检查器。类似地，可以将 {@code PKIXRevocationChecker} 添加到 {@code PKIXBuilderParameters} 对象中，以与 PKIX {@code CertPathBuilder} 一起使用。
 *
 * <p>注意，当将 {@code PKIXRevocationChecker} 添加到 {@code PKIXParameters} 时，它会克隆 {@code PKIXRevocationChecker}；因此，对 {@code PKIXRevocationChecker} 的任何后续修改都不会产生影响。
 *
 * <p>任何未设置（或设置为 {@code null}）的参数将被设置为该参数的默认值。
 *
 * <p><b>并发访问</b>
 *
 * <p>除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应自行同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
 *
 * @since 1.8
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2560.txt"><i>RFC&nbsp;2560: X.509
 * Internet Public Key Infrastructure Online Certificate Status Protocol -
 * OCSP</i></a>, <br><a
 * href="http://www.ietf.org/rfc/rfc5280.txt"><i>RFC&nbsp;5280: Internet X.509
 * Public Key Infrastructure Certificate and Certificate Revocation List (CRL)
 * Profile</i></a>
 */
public abstract class PKIXRevocationChecker extends PKIXCertPathChecker {
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private List<Extension> ocspExtensions = Collections.<Extension>emptyList();
    private Map<X509Certificate, byte[]> ocspResponses = Collections.emptyMap();
    private Set<Option> options = Collections.emptySet();

    /**
     * 默认构造函数。
     */
    protected PKIXRevocationChecker() {}

    /**
     * 设置标识 OCSP 响应者位置的 URI。这将覆盖 {@code ocsp.responderURL} 安全属性和证书的权威信息访问扩展中指定的响应者，如 RFC 5280 所定义。
     *
     * @param uri 响应者的 URI
     */
    public void setOcspResponder(URI uri) {
        this.ocspResponder = uri;
    }

    /**
     * 获取标识 OCSP 响应者位置的 URI。这将覆盖 {@code ocsp.responderURL} 安全属性。如果此参数或 {@code ocsp.responderURL} 属性未设置，则位置将从证书的权威信息访问扩展中确定，如 RFC 5280 所定义。
     *
     * @return 响应者的 URI，如果未设置则返回 {@code null}
     */
    public URI getOcspResponder() {
        return ocspResponder;
    }

    /**
     * 设置 OCSP 响应者的证书。这将覆盖 {@code ocsp.responderCertSubjectName}、
     * {@code ocsp.responderCertIssuerName} 和 {@code ocsp.responderCertSerialNumber} 安全属性。
     *
     * @param cert 响应者的证书
     */
    public void setOcspResponderCert(X509Certificate cert) {
        this.ocspResponderCert = cert;
    }

    /**
     * 获取 OCSP 响应者的证书。这将覆盖 {@code ocsp.responderCertSubjectName}、
     * {@code ocsp.responderCertIssuerName} 和 {@code ocsp.responderCertSerialNumber} 安全属性。如果此参数或上述属性未设置，则响应者的证书将按照 RFC 2560 中的指定确定。
     *
     * @return 响应者的证书，如果未设置则返回 {@code null}
     */
    public X509Certificate getOcspResponderCert() {
        return ocspResponderCert;
    }

    // 请求扩展；不支持单个扩展
    /**
     * 设置可选的 OCSP 请求扩展。
     *
     * @param extensions 扩展列表。列表将被复制以防止后续修改。
     */
    public void setOcspExtensions(List<Extension> extensions)
    {
        this.ocspExtensions = (extensions == null)
                              ? Collections.<Extension>emptyList()
                              : new ArrayList<Extension>(extensions);
    }


/**
 * 获取可选的OCSP请求扩展。
 *
 * @return 一个不可修改的扩展列表。如果未指定任何扩展，则列表为空。
 */
public List<Extension> getOcspExtensions() {
    return Collections.unmodifiableList(ocspExtensions);
}

/**
 * 设置OCSP响应。这些响应用于在使用OCSP时确定指定证书的撤销状态。
 *
 * @param responses 一个OCSP响应的映射。每个键是一个 {@code X509Certificate}，映射到该证书对应的DER编码的OCSP响应。对映射进行深拷贝以防止后续修改。
 */
public void setOcspResponses(Map<X509Certificate, byte[]> responses)
{
    if (responses == null) {
        this.ocspResponses = Collections.<X509Certificate, byte[]>emptyMap();
    } else {
        Map<X509Certificate, byte[]> copy = new HashMap<>(responses.size());
        for (Map.Entry<X509Certificate, byte[]> e : responses.entrySet()) {
            copy.put(e.getKey(), e.getValue().clone());
        }
        this.ocspResponses = copy;
    }
}

/**
 * 获取OCSP响应。这些响应用于在使用OCSP时确定指定证书的撤销状态。
 *
 * @return 一个OCSP响应的映射。每个键是一个 {@code X509Certificate}，映射到该证书对应的DER编码的OCSP响应。返回映射的深拷贝以防止后续修改。如果未指定任何响应，则返回空映射。
 */
public Map<X509Certificate, byte[]> getOcspResponses() {
    Map<X509Certificate, byte[]> copy = new HashMap<>(ocspResponses.size());
    for (Map.Entry<X509Certificate, byte[]> e : ocspResponses.entrySet()) {
        copy.put(e.getKey(), e.getValue().clone());
    }
    return copy;
}

/**
 * 设置撤销选项。
 *
 * @param options 一组撤销选项。复制该集合以防止后续修改。
 */
public void setOptions(Set<Option> options) {
    this.options = (options == null)
                   ? Collections.<Option>emptySet()
                   : new HashSet<Option>(options);
}

/**
 * 获取撤销选项。
 *
 * @return 一个不可修改的撤销选项集合。如果未指定任何选项，则集合为空。
 */
public Set<Option> getOptions() {
    return Collections.unmodifiableSet(options);
}

/**
 * 返回一个列表，其中包含在设置 {@link Option#SOFT_FAIL SOFT_FAIL} 选项时撤销检查器忽略的异常。每次调用 {@link #init init} 时，列表都会被清空。
 * 列表中的条目按照 {@link CertPathValidatorException#getIndex getIndex} 方法返回的证书索引的升序排列。
 * <p>
 * {@code PKIXRevocationChecker} 的实现负责将忽略的异常添加到列表中。
 *
 * @return 一个包含忽略的异常的不可修改列表。如果未忽略任何异常，则列表为空。
 */
public abstract List<CertPathValidatorException> getSoftFailExceptions();

@Override
public PKIXRevocationChecker clone() {
    PKIXRevocationChecker copy = (PKIXRevocationChecker)super.clone();
    copy.ocspExtensions = new ArrayList<>(ocspExtensions);
    copy.ocspResponses = new HashMap<>(ocspResponses);
    // 深拷贝编码的响应，因为它们是可变的
    for (Map.Entry<X509Certificate, byte[]> entry :
             copy.ocspResponses.entrySet())
    {
        byte[] encoded = entry.getValue();
        entry.setValue(encoded.clone());
    }
    copy.options = new HashSet<>(options);
    return copy;
}

/**
 * 可以为撤销检查机制指定的各种撤销选项。
 */
public enum Option {
    /**
     * 仅检查最终实体证书的撤销状态。
     */
    ONLY_END_ENTITY,
    /**
     * 优先使用CRL而不是OSCP。默认行为是优先使用OCSP。每个PKIX实现应记录其特定的偏好规则和回退策略的进一步细节。
     */
    PREFER_CRLS,
    /**
     * 禁用回退机制。
     */
    NO_FALLBACK,
    /**
     * 如果由于以下原因之一无法确定撤销状态，允许撤销检查成功：
     * <ul>
     *  <li>由于网络错误无法获取CRL或OCSP响应。
     *  <li>OCSP响应者返回RFC 2560第2.3节中指定的以下错误之一：internalError或tryLater。
     * </ul><br>
     * 请注意，这些条件适用于OCSP和CRL，除非设置了 {@code NO_FALLBACK} 选项，否则只有当两种机制都因上述条件之一失败时，撤销检查才允许成功。
     * 导致网络错误的异常将被忽略，但可以通过调用 {@link #getSoftFailExceptions getSoftFailExceptions} 方法稍后检索。
     */
    SOFT_FAIL
}
