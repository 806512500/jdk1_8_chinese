
/*
 * 版权所有 (c) 2000, 2015, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import javax.security.auth.x500.X500Principal;

import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.x509.CRLNumberExtension;
import sun.security.x509.X500Name;

/**
 * 一个 {@code CRLSelector}，用于选择与所有指定标准匹配的 {@code X509CRLs}。
 * 该类在从 {@code CertStore} 中选择 CRL 以检查特定证书的撤销状态时特别有用。
 * <p>
 * 刚构造时，一个 {@code X509CRLSelector} 没有启用任何标准，每个 {@code get} 方法返回一个默认值
 * ({@code null})。因此，{@link #match match} 方法对任何 {@code X509CRL} 都会返回 {@code true}。
 * 通常，启用几个标准（例如调用 {@link #setIssuers setIssuers} 或 {@link #setDateAndTime setDateAndTime}）
 * 然后将 {@code X509CRLSelector} 传递给 {@link CertStore#getCRLs CertStore.getCRLs} 或类似方法。
 * <p>
 * 请参阅 <a href="http://www.ietf.org/rfc/rfc3280.txt">RFC 3280: Internet X.509 Public Key Infrastructure Certificate and CRL Profile</a>
 * 了解下面提到的 X.509 CRL 字段和扩展的定义。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应同步并提供必要的锁定。
 * 每个操作单独对象的多个线程不需要同步。
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
     * 设置 issuerNames 标准。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称之一匹配。
     * 如果为 {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 此方法允许调用者通过单个方法调用指定 {@code X509CRLs} 可能包含的完整发行者名称集。
     * 指定的值将替换 issuerNames 标准的先前值。
     * <p>
     * {@code names} 参数（如果不是 {@code null}）是一个 {@code X500Principal} 的 {@code Collection}。
     * <p>
     * 注意，{@code names} 参数可以包含重复的区分名称，但它们可能会从 {@link #getIssuers getIssuers} 方法返回的名称集合中移除。
     * <p>
     * 注意，对 {@code Collection} 进行了复制以防止后续修改。
     *
     * @param issuers 一个 X500Principals 的 {@code Collection} (或 {@code null})
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
     * <strong>注意：</strong> 使用 {@linkplain #setIssuers(Collection)} 代替，或仅在使用此方法时指定区分名称的字节数组形式。
     * 有关更多信息，请参阅 {@link #addIssuerName(String)}。
     * <p>
     * 设置 issuerNames 标准。{@code X509CRL} 中的发行者区分名称必须与指定的区分名称之一匹配。
     * 如果为 {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 此方法允许调用者通过单个方法调用指定 {@code X509CRLs} 可能包含的完整发行者名称集。
     * 指定的值将替换 issuerNames 标准的先前值。
     * <p>
     * {@code names} 参数（如果不是 {@code null}）是一个名称的 {@code Collection}。每个名称是一个 {@code String}
     * 或表示区分名称的字节数组（分别为 <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a> 或
     * ASN.1 DER 编码形式）。如果此参数的值为 {@code null}，则不执行 issuerNames 检查。
     * <p>
     * 注意，{@code names} 参数可以包含重复的区分名称，但它们可能会从 {@link #getIssuerNames getIssuerNames} 方法返回的名称集合中移除。
     * <p>
     * 如果名称指定为字节数组，它应包含单个 DER 编码的区分名称，如 X.501 中定义。该结构的 ASN.1 表示如下。
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
     * 注意，对 {@code Collection} 进行了深度复制以防止后续修改。
     *
     * @param names 一个名称的 {@code Collection} (或 {@code null})
     * @throws IOException 如果发生解析错误
     * @see #getIssuerNames
     */
    public void setIssuerNames(Collection<?> names) throws IOException {
        if (names == null || names.size() == 0) {
            issuerNames = null;
            issuerX500Principals = null;
        } else {
            HashSet<Object> tempNames = cloneAndCheckIssuerNames(names);
            // 确保我们设置或不设置这两个
            issuerX500Principals = parseIssuerNames(tempNames);
            issuerNames = tempNames;
        }
    }

                /**
     * 向issuerNames标准中添加一个名称。在{@code X509CRL}中的发行者区分名称必须与指定的区分名称之一匹配。
     * <p>
     * 该方法允许调用者向可能包含在{@code X509CRLs}中的发行者名称集中添加一个名称。指定的名称将添加到
     * issuerNames标准的任何先前值中。如果指定的名称是重复的，可能会被忽略。
     *
     * @param issuer 作为X500Principal的发行者
     * @since 1.5
     */
    public void addIssuer(X500Principal issuer) {
        addIssuerNameInternal(issuer.getEncoded(), issuer);
    }

    /**
     * <strong>不推荐使用</strong>，请使用
     * {@linkplain #addIssuer(X500Principal)} 或
     * {@linkplain #addIssuerName(byte[])} 代替。不应依赖此方法，因为它可能会因为RFC 2253字符串形式的某些区分名称编码信息的丢失而无法匹配某些CRL。
     * <p>
     * 向issuerNames标准中添加一个名称。在{@code X509CRL}中的发行者区分名称必须与指定的区分名称之一匹配。
     * <p>
     * 该方法允许调用者向可能包含在{@code X509CRLs}中的发行者名称集中添加一个名称。指定的名称将添加到
     * issuerNames标准的任何先前值中。如果指定的名称是重复的，可能会被忽略。
     *
     * @param name RFC 2253形式的名称
     * @throws IOException 如果解析错误发生
     */
    public void addIssuerName(String name) throws IOException {
        addIssuerNameInternal(name, new X500Name(name).asX500Principal());
    }

    /**
     * 向issuerNames标准中添加一个名称。在{@code X509CRL}中的发行者区分名称必须与指定的区分名称之一匹配。
     * <p>
     * 该方法允许调用者向可能包含在{@code X509CRLs}中的发行者名称集中添加一个名称。指定的名称将添加到
     * issuerNames标准的任何先前值中。如果指定的名称是重复的，可能会被忽略。
     * 如果名称以字节数组的形式指定，它应该包含一个根据X.501定义的单个DER编码的区分名称。ASN.1对此结构的表示如下。
     * <p>
     * 名称以字节数组的形式提供。此字节数组应包含一个根据X.501定义的单个DER编码的区分名称。ASN.1对此结构的表示出现在
     * {@link #setIssuerNames setIssuerNames(Collection names)}的文档中。
     * <p>
     * 注意，这里提供的字节数组会被克隆以防止后续修改。
     *
     * @param name 包含以ASN.1 DER编码形式的名称的字节数组
     * @throws IOException 如果解析错误发生
     */
    public void addIssuerName(byte[] name) throws IOException {
        // 克隆，因为字节数组是可修改的
        addIssuerNameInternal(name.clone(), new X500Name(name).asX500Principal());
    }

    /**
     * 一个私有方法，用于向issuerNames标准中添加一个名称（字符串或字节数组形式）。在{@code X509CRL}中的发行者区分名称必须与指定的区分名称之一匹配。
     *
     * @param name 以字符串或字节数组形式的名称
     * @param principal 以X500Principal形式的名称
     * @throws IOException 如果解析错误发生
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
     * 克隆并检查传递给setIssuerNames的形式的参数。如果参数格式错误，则抛出IOException。
     *
     * @param names 一个名称的{@code Collection}。每个条目是一个字符串或字节数组（名称，分别以字符串或ASN.1
     *              DER编码形式）。{@code null}不是一个可接受的值。
     * @return 指定{@code Collection}的深层副本
     * @throws IOException 如果解析错误发生
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
     * 克隆传递给setIssuerNames的形式的参数。如果参数格式错误，则抛出RuntimeException。
     * <p>
     * 该方法包装了cloneAndCheckIssuerNames，将任何IOException转换为RuntimeException。当克隆的对象已经被检查，因此不应该有任何异常时，应该使用此方法。
     *
     * @param names 一个名称的{@code Collection}。每个条目是一个字符串或字节数组（名称，分别以字符串或ASN.1
     *              DER编码形式）。{@code null}不是一个可接受的值。
     * @return 指定{@code Collection}的深层副本
     * @throws RuntimeException 如果解析错误发生
     */
    private static HashSet<Object> cloneIssuerNames(Collection<Object> names) {
        try {
            return cloneAndCheckIssuerNames(names);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


                /**
     * 解析传递给 setIssuerNames 的形式的参数，
     * 返回一个 issuerX500Principals 的 Collection。
     * 如果参数格式不正确，则抛出 IOException。
     *
     * @param names 一个 {@code Collection} 的名字。每个条目是一个
     *              字符串或字节数组（分别是字符串或 ASN.1
     *              DER 编码形式的名字）。{@code Null} 不是
     *              可接受的值。
     * @return 一个 HashSet 的 issuerX500Principals
     * @throws IOException 如果解析错误发生
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
                    throw (IOException)new IOException("无效的名字").initCause(e);
                }
            }
        }
        return x500Principals;
    }

    /**
     * 设置 minCRLNumber 标准。{@code X509CRL} 必须有一个
     * CRL number 扩展，其值大于或等于
     * 指定的值。如果为 {@code null}，则不进行 minCRLNumber 检查。
     *
     * @param minCRL 接受的最小 CRL 编号（或 {@code null}）
     */
    public void setMinCRLNumber(BigInteger minCRL) {
        this.minCRL = minCRL;
    }

    /**
     * 设置 maxCRLNumber 标准。{@code X509CRL} 必须有一个
     * CRL number 扩展，其值小于或等于
     * 指定的值。如果为 {@code null}，则不进行 maxCRLNumber 检查。
     *
     * @param maxCRL 接受的最大 CRL 编号（或 {@code null}）
     */
    public void setMaxCRLNumber(BigInteger maxCRL) {
        this.maxCRL = maxCRL;
    }

    /**
     * 设置 dateAndTime 标准。指定的日期必须
     * 等于或晚于 {@code X509CRL} 的 thisUpdate 组件的值
     * 并且早于 nextUpdate 组件的值。如果 {@code X509CRL}
     * 不包含 nextUpdate 组件，则没有匹配。
     * 如果为 {@code null}，则不进行 dateAndTime 检查。
     * <p>
     * 注意，这里提供的 {@code Date} 会被克隆以防止
     * 后续的修改。
     *
     * @param dateAndTime 要匹配的 {@code Date}
     *                    （或 {@code null}）
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
     * 设置 dateAndTime 标准，并允许在检查 CRL 的有效期时
     * 指定的时钟偏差（以毫秒为单位）。
     */
    void setDateAndTime(Date dateAndTime, long skew) {
        this.dateAndTime =
            (dateAndTime == null ? null : new Date(dateAndTime.getTime()));
        this.skew = skew;
    }

    /**
     * 设置正在检查的证书。这不是一个标准。而是，
     * 它是可选信息，可能有助于 {@code CertStore}
     * 找到在检查指定证书的撤销时相关的 CRL。如果指定
     * 为 {@code null}，则不提供此类可选信息。
     *
     * @param cert 正在检查的 {@code X509Certificate}
     *             （或 {@code null}）
     * @see #getCertificateChecking
     */
    public void setCertificateChecking(X509Certificate cert) {
        certChecking = cert;
    }

    /**
     * 返回 issuerNames 标准。{@code X509CRL} 的发行者区分
     * 名称必须与指定的区分名称中的至少一个匹配。如果返回的值为
     * {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个
     * 不可修改的 {@code Collection} 的 {@code X500Principal}。
     *
     * @return 一个不可修改的 {@code Collection} 的名字
     *   （或 {@code null}）
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
     * 返回 issuerNames 标准的副本。{@code X509CRL} 的发行者区分
     * 名称必须与指定的区分名称中的至少一个匹配。如果返回的值为
     * {@code null}，则任何发行者区分名称都可以。
     * <p>
     * 如果返回的值不是 {@code null}，则它是一个
     * 名字的 {@code Collection}。每个名字是一个 {@code String}
     * 或表示区分名称的字节数组（分别是 RFC 2253 或
     * ASN.1 DER 编码形式）。注意，返回的
     * {@code Collection} 可能包含重复的名字。
     * <p>
     * 如果名字被指定为字节数组，它应该包含一个 DER
     * 编码的区分名称，如 X.501 中定义的。此结构的 ASN.1 表示法
     * 在 {@link #setIssuerNames setIssuerNames(Collection names)} 的文档中给出。
     * <p>
     * 注意，对 {@code Collection} 进行了深度复制以防止
     * 后续的修改。
     *
     * @return 一个名字的 {@code Collection}（或 {@code null}）
     * @see #setIssuerNames
     */
    public Collection<Object> getIssuerNames() {
        if (issuerNames == null) {
            return null;
        }
        return cloneIssuerNames(issuerNames);
    }

    /**
     * 返回 minCRLNumber 标准。{@code X509CRL} 必须有一个
     * CRL number 扩展，其值大于或等于
     * 指定的值。如果为 {@code null}，则不进行 minCRLNumber 检查。
     *
     * @return 接受的最小 CRL 编号（或 {@code null}）
     */
    public BigInteger getMinCRL() {
        return minCRL;
    }

                /**
     * 返回 maxCRLNumber 标准。{@code X509CRL} 必须有一个 CRL 编号扩展，其值小于或等于
     * 指定的值。如果为 {@code null}，则不进行 maxCRLNumber 检查。
     *
     * @return 可接受的最大 CRL 编号（或 {@code null}）
     */
    public BigInteger getMaxCRL() {
        return maxCRL;
    }

    /**
     * 返回 dateAndTime 标准。指定的日期必须等于或晚于 {@code X509CRL} 的 thisUpdate 组件的值，
     * 并且早于 nextUpdate 组件的值。如果 {@code X509CRL} 不包含 nextUpdate 组件，则没有匹配。
     * 如果为 {@code null}，则不进行 dateAndTime 检查。
     * <p>
     * 注意：返回的 {@code Date} 是克隆的，以防止后续修改。
     *
     * @return 用于匹配的 {@code Date}（或 {@code null}）
     * @see #setDateAndTime
     */
    public Date getDateAndTime() {
        if (dateAndTime == null)
            return null;
        return (Date) dateAndTime.clone();
    }

    /**
     * 返回正在检查的证书。这不是一个标准。相反，它是可选信息，可以帮助 {@code CertStore}
     * 找到在检查指定证书的撤销状态时相关的 CRL。如果返回的值为 {@code null}，则不提供此类可选信息。
     *
     * @return 正在检查的证书（或 {@code null}）
     * @see #setCertificateChecking
     */
    public X509Certificate getCertificateChecking() {
        return certChecking;
    }

    /**
     * 返回 {@code X509CRLSelector} 的可打印表示。
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
     * @return 如果应该选择 {@code CRL}，则返回 {@code true}，否则返回 {@code false}
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
                    debug.println("X509CRLSelector.match: issuer DNs "
                        + "don't match");
                }
                return false;
            }
        }

        if ((minCRL != null) || (maxCRL != null)) {
            /* 从 CRL 获取 CRL 编号扩展 */
            byte[] crlNumExtVal = xcrl.getExtensionValue("2.5.29.20");
            if (crlNumExtVal == null) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: no CRLNumber");
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
                    debug.println("X509CRLSelector.match: exception in "
                        + "decoding CRL number");
                }
                return false;
            }

            /* 匹配 minCRLNumber */
            if (minCRL != null) {
                if (crlNum.compareTo(minCRL) < 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber too small");
                    }
                    return false;
                }
            }

            /* 匹配 maxCRLNumber */
            if (maxCRL != null) {
                if (crlNum.compareTo(maxCRL) > 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber too large");
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
                    debug.println("X509CRLSelector.match: nextUpdate null");
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
                    debug.println("X509CRLSelector.match: update out-of-range");
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
