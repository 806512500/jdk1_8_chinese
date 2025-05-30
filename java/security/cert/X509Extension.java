/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Set;

/**
 * X.509 扩展的接口。
 *
 * <p>X.509 v3 {@link X509Certificate 证书} 和 v2 {@link X509CRL CRL}（证书撤销列表）定义的扩展提供了方法，
 * 用于将附加属性与用户或公钥关联，管理认证层次结构，以及管理 CRL 分发。X.509 扩展格式还允许社区定义私有扩展，
 * 以携带特定于这些社区的信息。
 *
 * <p>证书/CRL 中的每个扩展可以被指定为关键或非关键。证书/CRL 使用系统（验证证书/CRL 的应用程序）如果遇到
 * 不识别的关键扩展，则必须拒绝该证书/CRL。如果未识别非关键扩展，则可以忽略。
 * <p>
 * ASN.1 定义如下：
 * <pre>
 * Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
 *
 * Extension  ::=  SEQUENCE  {
 *     extnId        OBJECT IDENTIFIER,
 *     critical      BOOLEAN DEFAULT FALSE,
 *     extnValue     OCTET STRING
 *                   -- 包含为 extnId 对象标识符值注册的类型的 DER 编码
 * }
 * </pre>
 * 由于并非所有扩展都是已知的，{@code getExtensionValue} 方法返回扩展值（即 {@code extnValue}）的 DER 编码 OCTET 字符串。
 * 然后可以由理解该扩展的 <em>类</em> 处理。
 *
 * @author Hemma Prafullchandra
 */

public interface X509Extension {

    /**
     * 检查是否存在不支持的关键扩展。
     *
     * @return 如果找到不支持的关键扩展，则返回 {@code true}，否则返回 {@code false}。
     */
    public boolean hasUnsupportedCriticalExtension();

    /**
     * 获取证书/CRL 中标记为 CRITICAL 的扩展的 OID 字符串集。
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
     * @return 标记为关键的扩展的 OID 字符串集（如果没有任何扩展被标记为关键，则返回空集）。
     * 如果没有任何扩展存在，则此方法返回 null。
     */
    public Set<String> getCriticalExtensionOIDs();

    /**
     * 获取证书/CRL 中标记为 NON-CRITICAL 的扩展的 OID 字符串集。
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
     * @return 标记为非关键的扩展的 OID 字符串集（如果没有任何扩展被标记为非关键，则返回空集）。
     * 如果没有任何扩展存在，则此方法返回 null。
     */
    public Set<String> getNonCriticalExtensionOIDs();

    /**
     * 获取由传入的 {@code oid} 字符串标识的扩展值（<em>extnValue</em>）的 DER 编码 OCTET 字符串。
     * {@code oid} 字符串由以点分隔的非负整数组成。
     *
     * <p>例如：<br>
     * <table border=groove summary="OID 和扩展名称的示例">
     * <tr>
     * <th>OID <em>(对象标识符)</em></th>
     * <th>扩展名称</th></tr>
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
     * @return 扩展值的 DER 编码 OCTET 字符串，如果不存在则返回 null。
     */
    public byte[] getExtensionValue(String oid);
}
