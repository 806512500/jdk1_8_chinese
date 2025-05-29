/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Set;

/**
 * X.509 扩展的接口。
 *
 * <p>为 X.509 v3
 * {@link X509Certificate 证书} 和 v2
 * {@link X509CRL CRL}（证书撤销
 * 列表）定义的扩展提供了方法
 * 用于将附加属性与用户或公钥关联，管理证书层次结构，以及管理 CRL
 * 分发。X.509 扩展格式还允许社区
 * 定义私有扩展以携带那些
 * 社区独有的信息。
 *
 * <p>证书/CRL 中的每个扩展都可能被指定为
 * 关键或非关键。证书/CRL 使用系统（应用程序
 * 验证证书/CRL）必须拒绝证书/CRL 如果它
 * 遇到一个它不认识的关键扩展。如果它不认识非关键
 * 扩展，则可以忽略。
 * <p>
 * ASN.1 定义如下：
 * <pre>
 * Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
 *
 * Extension  ::=  SEQUENCE  {
 *     extnId        OBJECT IDENTIFIER,
 *     critical      BOOLEAN DEFAULT FALSE,
 *     extnValue     OCTET STRING
 *                   -- 包含注册用于
 *                   -- extnId 对象标识符值的类型的 DER 编码
 * }
 * </pre>
 * 由于并非所有扩展都是已知的，因此 {@code getExtensionValue}
 * 方法返回扩展值的 DER 编码 OCTET 字符串（即，
 * {@code extnValue}）。然后可以由一个 <em>类</em> 处理该字符串，该类理解该扩展。
 *
 * @author Hemma Prafullchandra
 */

public interface X509Extension {

    /**
     * 检查是否有不支持的关键扩展。
     *
     * @return 如果发现不支持的关键扩展，则返回 {@code true}，否则返回 {@code false}。
     */
    public boolean hasUnsupportedCriticalExtension();

    /**
     * 获取由实现此接口的对象管理的证书/CRL 中标记为
     * 关键的扩展的 OID 字符串集。
     *
     * 以下是从 X509Certificate 获取关键扩展集并打印 OID 的示例代码：
     * <pre>{@code
     * X509Certificate cert = null;
     * try (InputStream inStrm = new FileInputStream("DER-encoded-Cert")) {
     *     CertificateFactory cf = CertificateFactory.getInstance("X.509");
     *     cert = (X509Certificate)cf.generateCertificate(inStrm);
     * }
     *
     * Set<String> critSet = cert.getCriticalExtensionOIDs();
     * if (critSet != null && !critSet.isEmpty()) {
     *     System.out.println("关键扩展集：");
     *     for (String oid : critSet) {
     *         System.out.println(oid);
     *     }
     * }
     * }</pre>
     * @return 一个集（或一个空集，如果没有任何标记为关键的扩展），
     * 包含标记为关键的扩展的扩展 OID 字符串。如果没有扩展存在，则此方法返回
     * null。
     */
    public Set<String> getCriticalExtensionOIDs();

    /**
     * 获取由实现此接口的对象管理的证书/CRL 中标记为
     * 非关键的扩展的 OID 字符串集。
     *
     * 以下是从 X509CRL 被撤销的证书条目中获取非关键扩展集并打印 OID 的示例代码：
     * <pre>{@code
     * CertificateFactory cf = null;
     * X509CRL crl = null;
     * try (InputStream inStrm = new FileInputStream("DER-encoded-CRL")) {
     *     cf = CertificateFactory.getInstance("X.509");
     *     crl = (X509CRL)cf.generateCRL(inStrm);
     * }
     *
     * byte[] certData = <DER-encoded certificate data>
     * ByteArrayInputStream bais = new ByteArrayInputStream(certData);
     * X509Certificate cert = (X509Certificate)cf.generateCertificate(bais);
     * X509CRLEntry badCert =
     *              crl.getRevokedCertificate(cert.getSerialNumber());
     *
     * if (badCert != null) {
     *     Set<String> nonCritSet = badCert.getNonCriticalExtensionOIDs();
     *     if (nonCritSet != null)
     *         for (String oid : nonCritSet) {
     *             System.out.println(oid);
     *         }
     * }
     * }</pre>
     *
     * @return 一个集（或一个空集，如果没有任何标记为非关键的扩展），
     * 包含标记为非关键的扩展的扩展 OID 字符串。如果没有扩展存在，则此方法返回
     * null。
     */
    public Set<String> getNonCriticalExtensionOIDs();

    /**
     * 获取由传入的 {@code oid} 字符串标识的扩展值
     * （<em>extnValue</em>）的 DER 编码 OCTET 字符串。
     * {@code oid} 字符串由
     * 以点分隔的非负整数组成。
     *
     * <p>例如：<br>
     * <table border=groove summary="OID 和扩展名的示例">
     * <tr>
     * <th>OID <em>(对象标识符)</em></th>
     * <th>扩展名</th></tr>
     * <tr><td>2.5.29.14</td>
     * <td>SubjectKeyIdentifier</td></tr>
     * <tr><td>2.5.29.15</td>
     * <td>KeyUsage</td></tr>
     * <tr><td>2.5.29.16</td>
     * <td>PrivateKeyUsage</td></tr>
     * <tr><td>2.5.29.17</td>
     * <td>SubjectAlternativeName</td></tr>
     * <tr><td>2.5.29.18</td>
     * <td>IssuerAlternativeName</td></tr>
     * <tr><td>2.5.29.19</td>
     * <td>BasicConstraints</td></tr>
     * <tr><td>2.5.29.30</td>
     * <td>NameConstraints</td></tr>
     * <tr><td>2.5.29.33</td>
     * <td>PolicyMappings</td></tr>
     * <tr><td>2.5.29.35</td>
     * <td>AuthorityKeyIdentifier</td></tr>
     * <tr><td>2.5.29.36</td>
     * <td>PolicyConstraints</td></tr>
     * </table>
     *
     * @param oid 扩展的对象标识符值。
     * @return 扩展值的 DER 编码八位字节字符串，如果不存在则返回
     * null。
     */
    public byte[] getExtensionValue(String oid);
}
