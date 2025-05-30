
/*
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import javax.security.auth.x500.X500Principal;

import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.x509.CRLNumberExtension;
import sun.security.x509.X500Name;

/**
 * 一个 {@code CRLSelector}，用于选择符合所有指定标准的 {@code X509CRLs}。此类特别适用于从 {@code CertStore} 中选择 CRL 以检查特定证书的撤销状态。
 * <p>
 * 刚构造时，一个 {@code X509CRLSelector} 没有启用任何标准，每个 {@code get} 方法返回一个默认值（{@code null}）。因此，{@link #match match} 方法
 * 会对任何 {@code X509CRL} 返回 {@code true}。通常，启用几个标准（通过调用 {@link #setIssuers setIssuers}
 * 或 {@link #setDateAndTime setDateAndTime} 等方法）然后将 {@code X509CRLSelector} 传递给
 * {@link CertStore#getCRLs CertStore.getCRLs} 或类似方法。
 * <p>
 * 请参阅 <a href="http://tools.ietf.org/html/rfc5280">RFC 5280:
 * Internet X.509 Public Key Infrastructure Certificate and CRL Profile</a>
 * 了解下面提到的 X.509 CRL 字段和扩展的定义。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，本类定义的方法不是线程安全的。多个线程需要并发访问单个对象时应同步并提供必要的锁定。每个线程操作不同对象时无需同步。
 *
 * @see CRLSelector
 * @see X509CRL
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public class X509CRLSelector implements CRLSelector {

    static {
        CertPathHelperImpl.initialize();
    }

    private static final Debug debug = Debug.getInstance("certpath");
    private HashSet<Object> issuerNames;
    private HashSet<X500Principal> issuerX500Principals;
    private BigInteger minCRL;
    private BigInteger maxCRL;
    private Date dateAndTime;
    private X509Certificate certChecking;
    private long skew = 0;

    /**
     * 创建一个 {@code X509CRLSelector}。最初，没有设置任何标准，因此任何 {@code X509CRL} 都会匹配。
     */
    public X509CRLSelector() {}

    /**
     * 设置 issuerNames 标准。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。如果为 {@code null}，任何发行者区分名称都可以。
     * <p>
     * 该方法允许调用者通过单个方法调用指定 {@code X509CRLs} 可能包含的完整发行者名称集。指定的值将替换 issuerNames 标准的先前值。
     * <p>
     * {@code names} 参数（如果不是 {@code null}）是一个 {@code X500Principal} 的 {@code Collection}。
     * <p>
     * 注意，{@code names} 参数可以包含重复的区分名称，但它们可能会从 {@link #getIssuers getIssuers} 方法返回的名称 {@code Collection} 中移除。
     * <p>
     * 注意，对 {@code Collection} 进行了复制以防止后续修改。
     *
     * @param issuers 一个 X500Principals 的 {@code Collection}（或 {@code null}）
     * @see #getIssuers
     * @since 1.5
     */
    public void setIssuers(Collection<X500Principal> issuers) {
        if ((issuers == null) || issuers.isEmpty()) {
            issuerNames = null;
            issuerX500Principals = null;
        } else {
            // 克隆
            issuerX500Principals = new HashSet<X500Principal>(issuers);
            issuerNames = new HashSet<Object>();
            for (X500Principal p : issuerX500Principals) {
                issuerNames.add(p.getEncoded());
            }
        }
    }

    /**
     * <strong>注意：</strong> 使用 {@linkplain #setIssuers(Collection)} 或仅在使用此方法时指定区分名称的字节数组形式。有关更多信息，请参阅 {@link #addIssuerName(String)}。
     * <p>
     * 设置 issuerNames 标准。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。如果为 {@code null}，任何发行者区分名称都可以。
     * <p>
     * 该方法允许调用者通过单个方法调用指定 {@code X509CRLs} 可能包含的完整发行者名称集。指定的值将替换 issuerNames 标准的先前值。
     * <p>
     * {@code names} 参数（如果不是 {@code null}）是一个名称的 {@code Collection}。每个名称是一个 {@code String}
     * 或表示区分名称的字节数组（分别为 <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a> 或 ASN.1 DER 编码形式）。如果此参数的值为 {@code null}，
     * 则不会执行 issuerNames 检查。
     * <p>
     * 注意，{@code names} 参数可以包含重复的区分名称，但它们可能会从 {@link #getIssuerNames getIssuerNames} 方法返回的名称 {@code Collection} 中移除。
     * <p>
     * 如果名称以字节数组形式指定，它应包含一个 DER 编码的区分名称，如 X.501 中定义。该结构的 ASN.1 表示如下。
     * <pre>{@code
     * Name ::= CHOICE {
     *   RDNSequence }
     *
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     * RelativeDistinguishedName ::=
     *   SET SIZE (1 .. MAX) OF AttributeTypeAndValue
     *
     * AttributeTypeAndValue ::= SEQUENCE {
     *   type     AttributeType,
     *   value    AttributeValue }
     *
     * AttributeType ::= OBJECT IDENTIFIER
     *
     * AttributeValue ::= ANY DEFINED BY AttributeType
     * ....
     * DirectoryString ::= CHOICE {
     *       teletexString           TeletexString (SIZE (1..MAX)),
     *       printableString         PrintableString (SIZE (1..MAX)),
     *       universalString         UniversalString (SIZE (1..MAX)),
     *       utf8String              UTF8String (SIZE (1.. MAX)),
     *       bmpString               BMPString (SIZE (1..MAX)) }
     * }</pre>
     * <p>
     * 注意，对 {@code Collection} 进行了深复制以防止后续修改。
     *
     * @param names 一个名称的 {@code Collection}（或 {@code null}）
     * @throws IOException 如果解析错误
     * @see #getIssuerNames
     */
    public void setIssuerNames(Collection<?> names) throws IOException {
        if (names == null || names.size() == 0) {
            issuerNames = null;
            issuerX500Principals = null;
        } else {
            HashSet<Object> tempNames = cloneAndCheckIssuerNames(names);
            // 确保这两个都设置或都不设置
            issuerX500Principals = parseIssuerNames(tempNames);
            issuerNames = tempNames;
        }
    }

    /**
     * 向 issuerNames 标准中添加一个名称。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。
     * <p>
     * 该方法允许调用者向 {@code X509CRLs} 可能包含的发行者名称集中添加一个名称。指定的名称将添加到 issuerNames 标准的任何先前值中。
     * 如果指定的名称是重复的，可能会被忽略。
     *
     * @param issuer 作为 X500Principal 的发行者
     * @since 1.5
     */
    public void addIssuer(X500Principal issuer) {
        addIssuerNameInternal(issuer.getEncoded(), issuer);
    }

    /**
     * <strong>不推荐使用</strong>，使用 {@linkplain #addIssuer(X500Principal)} 或 {@linkplain #addIssuerName(byte[])} 代替。不应依赖此方法，因为它可能会因 RFC 2253 字符串形式的某些区分名称的编码信息丢失而无法匹配某些 CRL。
     * <p>
     * 向 issuerNames 标准中添加一个名称。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。
     * <p>
     * 该方法允许调用者向 {@code X509CRLs} 可能包含的发行者名称集中添加一个名称。指定的名称将添加到 issuerNames 标准的任何先前值中。
     * 如果指定的名称是重复的，可能会被忽略。
     *
     * @param name 以 RFC 2253 形式表示的名称
     * @throws IOException 如果解析错误
     */
    public void addIssuerName(String name) throws IOException {
        addIssuerNameInternal(name, new X500Name(name).asX500Principal());
    }

    /**
     * 向 issuerNames 标准中添加一个名称。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。
     * <p>
     * 该方法允许调用者向 {@code X509CRLs} 可能包含的发行者名称集中添加一个名称。指定的名称将添加到 issuerNames 标准的任何先前值中。如果指定的名称是重复的，可能会被忽略。
     * 如果名称以字节数组形式指定，它应包含一个 DER 编码的区分名称，如 X.501 中定义。该结构的 ASN.1 表示如下。
     * <p>
     * 名称以字节数组形式提供。该字节数组应包含一个 DER 编码的区分名称，如 X.501 中定义。该结构的 ASN.1 表示出现在 {@link #setIssuerNames setIssuerNames(Collection names)} 的文档中。
     * <p>
     * 注意，此处提供的字节数组进行了复制以防止后续修改。
     *
     * @param name 包含名称的 ASN.1 DER 编码形式的字节数组
     * @throws IOException 如果解析错误
     */
    public void addIssuerName(byte[] name) throws IOException {
        // 克隆，因为字节数组是可修改的
        addIssuerNameInternal(name.clone(), new X500Name(name).asX500Principal());
    }

    /**
     * 一个私有方法，向 issuerNames 标准中添加一个名称（字符串或字节数组）。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称中的至少一个匹配。
     *
     * @param name 以字符串或字节数组形式表示的名称
     * @param principal 以 X500Principal 形式表示的名称
     * @throws IOException 如果解析错误
     */
    private void addIssuerNameInternal(Object name, X500Principal principal) {
        if (issuerNames == null) {
            issuerNames = new HashSet<Object>();
        }
        if (issuerX500Principals == null) {
            issuerX500Principals = new HashSet<X500Principal>();
        }
        issuerNames.add(name);
        issuerX500Principals.add(principal);
    }

    /**
     * 克隆并检查传递给 setIssuerNames 的参数形式。如果参数格式错误，则抛出 IOException。
     *
     * @param names 一个名称的 {@code Collection}。每个条目是一个 String 或字节数组（分别为字符串或 ASN.1 DER 编码形式的名称）。{@code null} 不是可接受的值。
     * @return 指定 {@code Collection} 的深复制
     * @throws IOException 如果解析错误
     */
    private static HashSet<Object> cloneAndCheckIssuerNames(Collection<?> names)
        throws IOException
    {
        HashSet<Object> namesCopy = new HashSet<Object>();
        Iterator<?> i = names.iterator();
        while (i.hasNext()) {
            Object nameObject = i.next();
            if (!(nameObject instanceof byte []) &&
                !(nameObject instanceof String))
                throw new IOException("name not byte array or String");
            if (nameObject instanceof byte [])
                namesCopy.add(((byte []) nameObject).clone());
            else
                namesCopy.add(nameObject);
        }
        return(namesCopy);
    }

    /**
     * 克隆传递给 setIssuerNames 的参数形式。如果参数格式错误，则抛出 RuntimeException。
     * <p>
     * 该方法包装了 cloneAndCheckIssuerNames，将任何 IOException 转换为 RuntimeException。当克隆的对象已经经过检查，因此不应有任何异常时，应使用此方法。
     *
     * @param names 一个名称的 {@code Collection}。每个条目是一个 String 或字节数组（分别为字符串或 ASN.1 DER 编码形式的名称）。{@code null} 不是可接受的值。
     * @return 指定 {@code Collection} 的深复制
     * @throws RuntimeException 如果解析错误
     */
    private static HashSet<Object> cloneIssuerNames(Collection<Object> names) {
        try {
            return cloneAndCheckIssuerNames(names);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * 解析传递给 setIssuerNames 的参数形式，返回一个 issuerX500Principals 的 Collection。如果参数格式错误，则抛出 IOException。
     *
     * @param names 一个名称的 {@code Collection}。每个条目是一个 String 或字节数组（分别为字符串或 ASN.1 DER 编码形式的名称）。<Code>Null</Code> 不是可接受的值。
     * @return 一个 issuerX500Principals 的 HashSet
     * @throws IOException 如果解析错误
     */
    private static HashSet<X500Principal> parseIssuerNames(Collection<Object> names)
    throws IOException {
        HashSet<X500Principal> x500Principals = new HashSet<X500Principal>();
        for (Iterator<Object> t = names.iterator(); t.hasNext(); ) {
            Object nameObject = t.next();
            if (nameObject instanceof String) {
                x500Principals.add(new X500Name((String)nameObject).asX500Principal());
            } else {
                try {
                    x500Principals.add(new X500Principal((byte[])nameObject));
                } catch (IllegalArgumentException e) {
                    throw (IOException)new IOException("Invalid name").initCause(e);
                }
            }
        }
        return x500Principals;
    }


                /**
     * 设置 minCRLNumber 标准。{@code X509CRL} 必须具有 CRL 编号扩展，其值必须大于或等于指定的值。如果为 {@code null}，则不执行 minCRLNumber 检查。
     *
     * @param minCRL 接受的最小 CRL 编号（或 {@code null}）
     */
    public void setMinCRLNumber(BigInteger minCRL) {
        this.minCRL = minCRL;
    }

    /**
     * 设置 maxCRLNumber 标准。{@code X509CRL} 必须具有 CRL 编号扩展，其值必须小于或等于指定的值。如果为 {@code null}，则不执行 maxCRLNumber 检查。
     *
     * @param maxCRL 接受的最大 CRL 编号（或 {@code null}）
     */
    public void setMaxCRLNumber(BigInteger maxCRL) {
        this.maxCRL = maxCRL;
    }

    /**
     * 设置 dateAndTime 标准。指定的日期必须等于或晚于 {@code X509CRL} 的 thisUpdate 组件的值，并且早于 nextUpdate 组件的值。如果 {@code X509CRL} 不包含 nextUpdate 组件，则没有匹配项。如果为 {@code null}，则不执行 dateAndTime 检查。
     * <p>
     * 注意：提供的 {@code Date} 会被克隆以防止后续修改。
     *
     * @param dateAndTime 要匹配的 {@code Date}（或 {@code null}）
     * @see #getDateAndTime
     */
    public void setDateAndTime(Date dateAndTime) {
        if (dateAndTime == null)
            this.dateAndTime = null;
        else
            this.dateAndTime = new Date(dateAndTime.getTime());
        this.skew = 0;
    }

    /**
     * 设置 dateAndTime 标准，并在检查 CRL 的有效期时允许指定的时钟偏差（以毫秒为单位）。
     */
    void setDateAndTime(Date dateAndTime, long skew) {
        this.dateAndTime =
            (dateAndTime == null ? null : new Date(dateAndTime.getTime()));
        this.skew = skew;
    }

    /**
     * 设置要检查的证书。这不是一个标准。而是可选信息，可能有助于 {@code CertStore} 找到与指定证书撤销检查相关的 CRL。如果指定为 {@code null}，则不提供此类可选信息。
     *
     * @param cert 要检查的 {@code X509Certificate}（或 {@code null}）
     * @see #getCertificateChecking
     */
    public void setCertificateChecking(X509Certificate cert) {
        certChecking = cert;
    }

    /**
     * 返回 issuerNames 标准。{@code X509CRL} 的发行者区分名称必须与指定的区分名称之一匹配。如果返回的值为 {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个不可修改的 {@code Collection}，其中包含 {@code X500Principal}。
     *
     * @return 包含名称的不可修改的 {@code Collection}（或 {@code null}）
     * @see #setIssuers
     * @since 1.5
     */
    public Collection<X500Principal> getIssuers() {
        if (issuerX500Principals == null) {
            return null;
        }
        return Collections.unmodifiableCollection(issuerX500Principals);
    }

    /**
     * 返回 issuerNames 标准的副本。{@code X509CRL} 的发行者区分名称必须与指定的区分名称之一匹配。如果返回的值为 {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个包含名称的 {@code Collection}。每个名称是一个 {@code String} 或表示区分名称的字节数组（分别以 RFC 2253 或 ASN.1 DER 编码形式）。注意，返回的 {@code Collection} 可能包含重复的名称。
     * <p>
     * 如果名称以字节数组的形式指定，它应包含一个 DER 编码的区分名称，如 X.501 中定义。此结构的 ASN.1 表示法在 {@link #setIssuerNames setIssuerNames(Collection names)} 的文档中给出。
     * <p>
     * 注意：对 {@code Collection} 执行了深拷贝以防止后续修改。
     *
     * @return 包含名称的 {@code Collection}（或 {@code null}）
     * @see #setIssuerNames
     */
    public Collection<Object> getIssuerNames() {
        if (issuerNames == null) {
            return null;
        }
        return cloneIssuerNames(issuerNames);
    }

    /**
     * 返回 minCRLNumber 标准。{@code X509CRL} 必须具有 CRL 编号扩展，其值必须大于或等于指定的值。如果为 {@code null}，则不执行 minCRLNumber 检查。
     *
     * @return 接受的最小 CRL 编号（或 {@code null}）
     */
    public BigInteger getMinCRL() {
        return minCRL;
    }

    /**
     * 返回 maxCRLNumber 标准。{@code X509CRL} 必须具有 CRL 编号扩展，其值必须小于或等于指定的值。如果为 {@code null}，则不执行 maxCRLNumber 检查。
     *
     * @return 接受的最大 CRL 编号（或 {@code null}）
     */
    public BigInteger getMaxCRL() {
        return maxCRL;
    }

    /**
     * 返回 dateAndTime 标准。指定的日期必须等于或晚于 {@code X509CRL} 的 thisUpdate 组件的值，并且早于 nextUpdate 组件的值。如果 {@code X509CRL} 不包含 nextUpdate 组件，则没有匹配项。如果为 {@code null}，则不执行 dateAndTime 检查。
     * <p>
     * 注意：返回的 {@code Date} 会被克隆以防止后续修改。
     *
     * @return 要匹配的 {@code Date}（或 {@code null}）
     * @see #setDateAndTime
     */
    public Date getDateAndTime() {
        if (dateAndTime == null)
            return null;
        return (Date) dateAndTime.clone();
    }

    /**
     * 返回要检查的证书。这不是一个标准。而是可选信息，可能有助于 {@code CertStore} 找到与指定证书撤销检查相关的 CRL。如果返回的值为 {@code null}，则不提供此类可选信息。
     *
     * @return 要检查的证书（或 {@code null}）
     * @see #setCertificateChecking
     */
    public X509Certificate getCertificateChecking() {
        return certChecking;
    }

    /**
     * 返回 {@code X509CRLSelector} 的可打印表示形式。
     *
     * @return 描述 {@code X509CRLSelector} 内容的 {@code String}。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X509CRLSelector: [\n");
        if (issuerNames != null) {
            sb.append("  IssuerNames:\n");
            Iterator<Object> i = issuerNames.iterator();
            while (i.hasNext())
                sb.append("    " + i.next() + "\n");
        }
        if (minCRL != null)
            sb.append("  minCRLNumber: " + minCRL + "\n");
        if (maxCRL != null)
            sb.append("  maxCRLNumber: " + maxCRL + "\n");
        if (dateAndTime != null)
            sb.append("  dateAndTime: " + dateAndTime + "\n");
        if (certChecking != null)
            sb.append("  Certificate being checked: " + certChecking + "\n");
        sb.append("]");
        return sb.toString();
    }

    /**
     * 决定是否选择一个 {@code CRL}。
     *
     * @param crl 要检查的 {@code CRL}
     * @return 如果应选择 {@code CRL}，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean match(CRL crl) {
        if (!(crl instanceof X509CRL)) {
            return false;
        }
        X509CRL xcrl = (X509CRL)crl;

        /* 匹配发行者名称 */
        if (issuerNames != null) {
            X500Principal issuer = xcrl.getIssuerX500Principal();
            Iterator<X500Principal> i = issuerX500Principals.iterator();
            boolean found = false;
            while (!found && i.hasNext()) {
                if (i.next().equals(issuer)) {
                    found = true;
                }
            }
            if (!found) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: 发行者 DNs 不匹配");
                }
                return false;
            }
        }

        if ((minCRL != null) || (maxCRL != null)) {
            /* 从 CRL 获取 CRL 编号扩展 */
            byte[] crlNumExtVal = xcrl.getExtensionValue("2.5.29.20");
            if (crlNumExtVal == null) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: 没有 CRLNumber");
                }
            }
            BigInteger crlNum;
            try {
                DerInputStream in = new DerInputStream(crlNumExtVal);
                byte[] encoded = in.getOctetString();
                CRLNumberExtension crlNumExt =
                    new CRLNumberExtension(Boolean.FALSE, encoded);
                crlNum = crlNumExt.get(CRLNumberExtension.NUMBER);
            } catch (IOException ex) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: 解码 CRL number 时发生异常");
                }
                return false;
            }

            /* 匹配 minCRLNumber */
            if (minCRL != null) {
                if (crlNum.compareTo(minCRL) < 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber 太小");
                    }
                    return false;
                }
            }

            /* 匹配 maxCRLNumber */
            if (maxCRL != null) {
                if (crlNum.compareTo(maxCRL) > 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber 太大");
                    }
                    return false;
                }
            }
        }


        /* 匹配 dateAndTime */
        if (dateAndTime != null) {
            Date crlThisUpdate = xcrl.getThisUpdate();
            Date nextUpdate = xcrl.getNextUpdate();
            if (nextUpdate == null) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: nextUpdate 为 null");
                }
                return false;
            }
            Date nowPlusSkew = dateAndTime;
            Date nowMinusSkew = dateAndTime;
            if (skew > 0) {
                nowPlusSkew = new Date(dateAndTime.getTime() + skew);
                nowMinusSkew = new Date(dateAndTime.getTime() - skew);
            }

            // 检查测试日期是否在有效期内：
            //   [ thisUpdate - MAX_CLOCK_SKEW,
            //     nextUpdate + MAX_CLOCK_SKEW ]
            if (nowMinusSkew.after(nextUpdate)
                || nowPlusSkew.before(crlThisUpdate)) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: 更新超出范围");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * 返回此对象的副本。
     *
     * @return 副本
     */
    public Object clone() {
        try {
            X509CRLSelector copy = (X509CRLSelector)super.clone();
            if (issuerNames != null) {
                copy.issuerNames =
                        new HashSet<Object>(issuerNames);
                copy.issuerX500Principals =
                        new HashSet<X500Principal>(issuerX500Principals);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }
}
