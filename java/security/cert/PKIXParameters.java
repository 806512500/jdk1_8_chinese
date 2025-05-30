
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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 用于 PKIX {@code CertPathValidator} 算法的输入参数。
 * <p>
 * PKIX {@code CertPathValidator} 使用这些参数根据 PKIX 认证路径验证算法验证 {@code CertPath}。
 *
 * <p>要实例化一个 {@code PKIXParameters} 对象，应用程序必须指定一个或多个 PKIX 认证路径验证算法定义的 <i>最可信的 CA</i>。最可信的 CA 可以使用两种构造函数之一指定。应用程序可以调用 {@link #PKIXParameters(Set) PKIXParameters(Set)}，指定一个包含 {@code TrustAnchor} 对象的 {@code Set}，每个对象标识一个最可信的 CA。或者，应用程序可以调用 {@link #PKIXParameters(KeyStore) PKIXParameters(KeyStore)}，指定一个包含受信任证书条目的 {@code KeyStore} 实例，每个条目将被视为一个最可信的 CA。
 * <p>
 * 创建 {@code PKIXParameters} 对象后，可以指定其他参数（例如调用 {@link #setInitialPolicies setInitialPolicies} 或 {@link #setDate setDate}），然后将 {@code PKIXParameters} 与要验证的 {@code CertPath} 一起传递给 {@link CertPathValidator#validate CertPathValidator.validate}。
 * <p>
 * 任何未设置（或设置为 {@code null}）的参数将被设置为该参数的默认值。{@code date} 参数的默认值为 {@code null}，表示路径验证时的当前时间。其余参数的默认值是最不严格的。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应自行同步并提供必要的锁定。每个操作单独对象的多个线程不需要同步。
 *
 * @see CertPathValidator
 *
 * @since       1.4
 * @author      Sean Mullan
 * @author      Yassir Elley
 */
public class PKIXParameters implements CertPathParameters {

    private Set<TrustAnchor> unmodTrustAnchors;
    private Date date;
    private List<PKIXCertPathChecker> certPathCheckers;
    private String sigProvider;
    private boolean revocationEnabled = true;
    private Set<String> unmodInitialPolicies;
    private boolean explicitPolicyRequired = false;
    private boolean policyMappingInhibited = false;
    private boolean anyPolicyInhibited = false;
    private boolean policyQualifiersRejected = true;
    private List<CertStore> certStores;
    private CertSelector certSelector;

    /**
     * 使用指定的 {@code Set} 最可信的 CA 创建 {@code PKIXParameters} 的实例。集合中的每个元素都是一个 {@link TrustAnchor TrustAnchor}。
     * <p>
     * 注意，集合被复制以防止后续修改。
     *
     * @param trustAnchors 一个包含 {@code TrustAnchor} 的 {@code Set}
     * @throws InvalidAlgorithmParameterException 如果指定的 {@code Set} 为空 {@code (trustAnchors.isEmpty() == true)}
     * @throws NullPointerException 如果指定的 {@code Set} 为 {@code null}
     * @throws ClassCastException 如果集合中的任何元素不是 {@code java.security.cert.TrustAnchor} 类型
     */
    public PKIXParameters(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException
    {
        setTrustAnchors(trustAnchors);

        this.unmodInitialPolicies = Collections.<String>emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }

    /**
     * 从指定的 {@code KeyStore} 中的受信任证书条目创建 {@code PKIXParameters} 的实例，以填充最可信的 CA 集合。仅考虑包含受信任的 {@code X509Certificates} 的密钥库条目；所有其他证书类型将被忽略。
     *
     * @param keystore 一个 {@code KeyStore}，从中填充最可信的 CA 集合
     * @throws KeyStoreException 如果密钥库未初始化
     * @throws InvalidAlgorithmParameterException 如果密钥库中不包含至少一个受信任的证书条目
     * @throws NullPointerException 如果密钥库为 {@code null}
     */
    public PKIXParameters(KeyStore keystore)
        throws KeyStoreException, InvalidAlgorithmParameterException
    {
        if (keystore == null)
            throw new NullPointerException("密钥库参数必须为非空");
        Set<TrustAnchor> hashSet = new HashSet<TrustAnchor>();
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keystore.isCertificateEntry(alias)) {
                Certificate cert = keystore.getCertificate(alias);
                if (cert instanceof X509Certificate)
                    hashSet.add(new TrustAnchor((X509Certificate)cert, null));
            }
        }
        setTrustAnchors(hashSet);
        this.unmodInitialPolicies = Collections.<String>emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }

    /**
     * 返回一个不可变的最可信 CA 的 {@code Set}。
     *
     * @return 一个不可变的 {@code TrustAnchor} 的 {@code Set}（从不为 {@code null}）
     *
     * @see #setTrustAnchors
     */
    public Set<TrustAnchor> getTrustAnchors() {
        return this.unmodTrustAnchors;
    }

    /**
     * 设置最可信 CA 的 {@code Set}。
     * <p>
     * 注意，集合被复制以防止后续修改。
     *
     * @param trustAnchors 一个包含 {@code TrustAnchor} 的 {@code Set}
     * @throws InvalidAlgorithmParameterException 如果指定的 {@code Set} 为空 {@code (trustAnchors.isEmpty() == true)}
     * @throws NullPointerException 如果指定的 {@code Set} 为 {@code null}
     * @throws ClassCastException 如果集合中的任何元素不是 {@code java.security.cert.TrustAnchor} 类型
     *
     * @see #getTrustAnchors
     */
    public void setTrustAnchors(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException
    {
        if (trustAnchors == null) {
            throw new NullPointerException("信任锚参数必须为非空");
        }
        if (trustAnchors.isEmpty()) {
            throw new InvalidAlgorithmParameterException("信任锚参数必须为非空");
        }
        for (Iterator<TrustAnchor> i = trustAnchors.iterator(); i.hasNext(); ) {
            if (!(i.next() instanceof TrustAnchor)) {
                throw new ClassCastException("集合中的所有元素必须是 java.security.cert.TrustAnchor 类型");
            }
        }
        this.unmodTrustAnchors = Collections.unmodifiableSet
                (new HashSet<TrustAnchor>(trustAnchors));
    }

    /**
     * 返回一个不可变的初始策略标识符（OID 字符串）的 {@code Set}，表示这些策略中的任何一个对于证书用户来说在认证路径处理中都是可接受的。默认返回值是一个空的 {@code Set}，表示任何策略都是可接受的。
     *
     * @return 一个不可变的初始策略 OID 的 {@code Set}，格式为 {@code String}，或一个空的 {@code Set}（表示任何策略都是可接受的）。从不返回 {@code null}。
     *
     * @see #setInitialPolicies
     */
    public Set<String> getInitialPolicies() {
        return this.unmodInitialPolicies;
    }

    /**
     * 设置初始策略标识符（OID 字符串）的 {@code Set}，表示这些策略中的任何一个对于证书用户来说在认证路径处理中都是可接受的。默认情况下，任何策略都是可接受的（即所有策略），因此希望允许任何策略的用户不需要调用此方法，或者可以调用此方法并传入一个空的 {@code Set}（或 {@code null}）。
     * <p>
     * 注意，集合被复制以防止后续修改。
     *
     * @param initialPolicies 一个初始策略 OID 的 {@code Set}，格式为 {@code String}（或 {@code null}）
     * @throws ClassCastException 如果集合中的任何元素不是 {@code String} 类型
     *
     * @see #getInitialPolicies
     */
    public void setInitialPolicies(Set<String> initialPolicies) {
        if (initialPolicies != null) {
            for (Iterator<String> i = initialPolicies.iterator();
                        i.hasNext();) {
                if (!(i.next() instanceof String))
                    throw new ClassCastException("集合中的所有元素必须是 java.lang.String 类型");
            }
            this.unmodInitialPolicies =
                Collections.unmodifiableSet(new HashSet<String>(initialPolicies));
        } else
            this.unmodInitialPolicies = Collections.<String>emptySet();
    }

    /**
     * 设置用于查找证书和 CRL 的 {@code CertStore} 列表。可以为 {@code null}，在这种情况下不会使用任何 {@code CertStore}。列表中的第一个 {@code CertStore} 可能比后面的更优先。
     * <p>
     * 注意，列表被复制以防止后续修改。
     *
     * @param stores 一个 {@code CertStore} 的 {@code List}（或 {@code null}）
     * @throws ClassCastException 如果列表中的任何元素不是 {@code java.security.cert.CertStore} 类型
     *
     * @see #getCertStores
     */
    public void setCertStores(List<CertStore> stores) {
        if (stores == null) {
            this.certStores = new ArrayList<CertStore>();
        } else {
            for (Iterator<CertStore> i = stores.iterator(); i.hasNext();) {
                if (!(i.next() instanceof CertStore)) {
                    throw new ClassCastException("列表中的所有元素必须是 java.security.cert.CertStore 类型");
                }
            }
            this.certStores = new ArrayList<CertStore>(stores);
        }
    }

    /**
     * 将一个 {@code CertStore} 添加到用于查找证书和 CRL 的 {@code CertStore} 列表的末尾。
     *
     * @param store 要添加的 {@code CertStore}。如果为 {@code null}，则忽略该存储（不添加到列表中）。
     */
    public void addCertStore(CertStore store) {
        if (store != null) {
            this.certStores.add(store);
        }
    }

    /**
     * 返回一个不可变的用于查找证书和 CRL 的 {@code CertStore} 列表。
     *
     * @return 一个不可变的 {@code CertStore} 的 {@code List}（可能为空，但不会为 {@code null}）
     *
     * @see #setCertStores
     */
    public List<CertStore> getCertStores() {
        return Collections.unmodifiableList
                (new ArrayList<CertStore>(this.certStores));
    }

    /**
     * 设置 RevocationEnabled 标志。如果此标志为 true，则将使用底层 PKIX 服务提供者的默认撤销检查机制。如果此标志为 false，则将禁用默认的撤销检查机制（不使用）。
     * <p>
     * 创建 {@code PKIXParameters} 对象时，此标志默认设置为 true。此设置反映了最常用的撤销检查策略，因为每个服务提供者必须支持撤销检查才能符合 PKIX 标准。高级应用程序应在不实用 PKIX 服务提供者的默认撤销检查机制或要替代撤销检查机制时（通过调用 {@link #addCertPathChecker addCertPathChecker} 或 {@link #setCertPathCheckers setCertPathCheckers} 方法）将此标志设置为 false。
     *
     * @param val RevocationEnabled 标志的新值
     */
    public void setRevocationEnabled(boolean val) {
        revocationEnabled = val;
    }

    /**
     * 检查 RevocationEnabled 标志。如果此标志为 true，则将使用底层 PKIX 服务提供者的默认撤销检查机制。如果此标志为 false，则将禁用默认的撤销检查机制（不使用）。有关设置此标志值的更多详细信息，请参阅 {@link #setRevocationEnabled setRevocationEnabled} 方法。
     *
     * @return 当前的 RevocationEnabled 标志值
     */
    public boolean isRevocationEnabled() {
        return revocationEnabled;
    }

    /**
     * 设置 ExplicitPolicyRequired 标志。如果此标志为 true，则每个证书中都需要显式标识一个可接受的策略。默认情况下，ExplicitPolicyRequired 标志为 false。
     *
     * @param val 如果需要显式策略，则为 {@code true}，否则为 {@code false}
     */
    public void setExplicitPolicyRequired(boolean val) {
        explicitPolicyRequired = val;
    }

    /**
     * 检查是否需要显式策略。如果此标志为 true，则每个证书中都需要显式标识一个可接受的策略。默认情况下，ExplicitPolicyRequired 标志为 false。
     *
     * @return 如果需要显式策略，则为 {@code true}，否则为 {@code false}
     */
    public boolean isExplicitPolicyRequired() {
        return explicitPolicyRequired;
    }


                /**
     * 设置 PolicyMappingInhibited 标志。如果此标志为 true，则禁止策略映射。默认情况下，策略映射不禁用（标志为 false）。
     *
     * @param val 如果要禁止策略映射，则为 {@code true}，否则为 {@code false}
     */
    public void setPolicyMappingInhibited(boolean val) {
        policyMappingInhibited = val;
    }

    /**
     * 检查策略映射是否被禁止。如果此标志为 true，则禁止策略映射。默认情况下，策略映射不禁用（标志为 false）。
     *
     * @return 如果策略映射被禁止，则返回 true，否则返回 false
     */
    public boolean isPolicyMappingInhibited() {
        return policyMappingInhibited;
    }

    /**
     * 设置状态以确定是否处理证书中包含的任何策略 OID。默认情况下，任何策略 OID 不被禁止（{@link #isAnyPolicyInhibited isAnyPolicyInhibited()}
     * 返回 {@code false}）。
     *
     * @param val 如果要禁止任何策略 OID，则为 {@code true}，否则为 {@code false}
     */
    public void setAnyPolicyInhibited(boolean val) {
        anyPolicyInhibited = val;
    }

    /**
     * 检查是否应处理证书中包含的任何策略 OID。
     *
     * @return 如果任何策略 OID 被禁止，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isAnyPolicyInhibited() {
        return anyPolicyInhibited;
    }

    /**
     * 设置 PolicyQualifiersRejected 标志。如果此标志为 true，则拒绝包含在证书策略扩展中且标记为关键的策略限定符的证书。
     * 如果标志为 false，则不基于此原因拒绝证书。
     *
     * <p> 当创建 {@code PKIXParameters} 对象时，此标志被设置为 true。此设置反映了处理策略限定符的最常见（也是最简单的）策略。
     * 希望使用更复杂策略的应用程序必须将此标志设置为 false。
     * <p>
     * 注意，PKIX 认证路径验证算法规定，必须处理和验证标记为关键的证书策略扩展中的任何策略限定符。否则，认证路径必须被拒绝。
     * 如果 policyQualifiersRejected 标志设置为 false，则应用程序必须以这种方式验证所有策略限定符，以符合 PKIX 标准。
     *
     * @param qualifiersRejected PolicyQualifiersRejected 标志的新值
     * @see #getPolicyQualifiersRejected
     * @see PolicyQualifierInfo
     */
    public void setPolicyQualifiersRejected(boolean qualifiersRejected) {
        policyQualifiersRejected = qualifiersRejected;
    }

    /**
     * 获取 PolicyQualifiersRejected 标志。如果此标志为 true，则拒绝包含在证书策略扩展中且标记为关键的策略限定符的证书。
     * 如果标志为 false，则不基于此原因拒绝证书。
     *
     * <p> 当创建 {@code PKIXParameters} 对象时，此标志被设置为 true。此设置反映了处理策略限定符的最常见（也是最简单的）策略。
     * 希望使用更复杂策略的应用程序必须将此标志设置为 false。
     *
     * @return PolicyQualifiersRejected 标志的当前值
     * @see #setPolicyQualifiersRejected
     */
    public boolean getPolicyQualifiersRejected() {
        return policyQualifiersRejected;
    }

    /**
     * 返回确定认证路径有效性的日期。如果为 {@code null}，则使用当前时间。
     * <p>
     * 注意，返回的 {@code Date} 被复制以防止后续修改。
     *
     * @return {@code Date}，或如果未设置则为 {@code null}
     * @see #setDate
     */
    public Date getDate() {
        if (date == null)
            return null;
        else
            return (Date) this.date.clone();
    }

    /**
     * 设置确定认证路径有效性的日期。如果为 {@code null}，则使用当前时间。
     * <p>
     * 注意，提供的 {@code Date} 被复制以防止后续修改。
     *
     * @param date {@code Date}，或为 {@code null} 以使用当前时间
     * @see #getDate
     */
    public void setDate(Date date) {
        if (date != null)
            this.date = (Date) date.clone();
        else
            date = null;
    }

    /**
     * 设置一个附加的认证路径检查器列表。如果指定的列表包含不是 {@code PKIXCertPathChecker} 的对象，则忽略这些对象。
     * <p>
     * 每个指定的 {@code PKIXCertPathChecker} 实现对证书的附加检查。通常，这些检查用于处理和验证证书中包含的私有扩展。
     * 每个 {@code PKIXCertPathChecker} 应使用执行检查所需的任何初始化参数进行实例化。
     * <p>
     * 此方法允许复杂的应用程序扩展 PKIX {@code CertPathValidator} 或 {@code CertPathBuilder}。
     * 每个指定的 {@code PKIXCertPathChecker} 将依次由 PKIX {@code CertPathValidator} 或
     * {@code CertPathBuilder} 调用，以处理或验证每个证书。
     * <p>
     * 无论是否设置了这些附加的 {@code PKIXCertPathChecker}，PKIX {@code CertPathValidator} 或
     * {@code CertPathBuilder} 必须对每个证书执行所有必需的 PKIX 检查。唯一的例外是如果 RevocationEnabled 标志设置为 false
     * （参见 {@link #setRevocationEnabled setRevocationEnabled} 方法）。
     * <p>
     * 注意，提供的列表被复制，列表中的每个 {@code PKIXCertPathChecker} 被克隆以防止后续修改。
     *
     * @param checkers 一个 {@code PKIXCertPathChecker} 列表。可以为 {@code null}，在这种情况下不会使用任何附加检查器。
     * @throws ClassCastException 如果列表中的任何元素不是 {@code java.security.cert.PKIXCertPathChecker} 类型
     * @see #getCertPathCheckers
     */
    public void setCertPathCheckers(List<PKIXCertPathChecker> checkers) {
        if (checkers != null) {
            List<PKIXCertPathChecker> tmpList =
                        new ArrayList<PKIXCertPathChecker>();
            for (PKIXCertPathChecker checker : checkers) {
                tmpList.add((PKIXCertPathChecker)checker.clone());
            }
            this.certPathCheckers = tmpList;
        } else {
            this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        }
    }

    /**
     * 返回认证路径检查器的列表。返回的列表是不可变的，列表中的每个
     * {@code PKIXCertPathChecker} 被克隆以防止后续修改。
     *
     * @return 一个不可变的 {@code PKIXCertPathChecker} 列表（可能为空，但不会为
     * {@code null}）
     * @see #setCertPathCheckers
     */
    public List<PKIXCertPathChecker> getCertPathCheckers() {
        List<PKIXCertPathChecker> tmpList = new ArrayList<PKIXCertPathChecker>();
        for (PKIXCertPathChecker ck : certPathCheckers) {
            tmpList.add((PKIXCertPathChecker)ck.clone());
        }
        return Collections.unmodifiableList(tmpList);
    }

    /**
     * 将一个 {@code PKIXCertPathChecker} 添加到认证路径检查器列表中。有关更多详细信息，请参见
     * {@link #setCertPathCheckers setCertPathCheckers} 方法。
     * <p>
     * 注意，提供的 {@code PKIXCertPathChecker} 被克隆以防止后续修改。
     *
     * @param checker 要添加到检查列表中的 {@code PKIXCertPathChecker}。如果为 {@code null}，则忽略此检查器（不添加到列表中）。
     */
    public void addCertPathChecker(PKIXCertPathChecker checker) {
        if (checker != null) {
            certPathCheckers.add((PKIXCertPathChecker)checker.clone());
        }
    }

    /**
     * 返回签名提供者的名称，或如果未设置则为 {@code null}。
     *
     * @return 签名提供者的名称（或 {@code null}）
     * @see #setSigProvider
     */
    public String getSigProvider() {
        return this.sigProvider;
    }

    /**
     * 设置签名提供者的名称。指定的提供者在创建 {@link java.security.Signature Signature}
     * 对象时将被优先使用。如果为 {@code null} 或未设置，则使用支持算法的第一个提供者。
     *
     * @param sigProvider 签名提供者的名称（或 {@code null}）
     * @see #getSigProvider
    */
    public void setSigProvider(String sigProvider) {
        this.sigProvider = sigProvider;
    }

    /**
     * 返回目标证书的约束条件。约束条件作为 {@code CertSelector} 的实例返回。
     * 如果为 {@code null}，则未定义任何约束条件。
     *
     * <p>注意，返回的 {@code CertSelector} 被克隆以防止后续修改。
     *
     * @return 一个指定目标证书约束条件的 {@code CertSelector}（或 {@code null}）
     * @see #setTargetCertConstraints
     */
    public CertSelector getTargetCertConstraints() {
        if (certSelector != null) {
            return (CertSelector) certSelector.clone();
        } else {
            return null;
        }
    }

    /**
     * 设置目标证书的约束条件。约束条件作为 {@code CertSelector} 的实例指定。
     * 如果为 {@code null}，则未定义任何约束条件。
     *
     * <p>注意，指定的 {@code CertSelector} 被克隆以防止后续修改。
     *
     * @param selector 一个指定目标证书约束条件的 {@code CertSelector}（或 {@code null}）
     * @see #getTargetCertConstraints
     */
    public void setTargetCertConstraints(CertSelector selector) {
        if (selector != null)
            certSelector = (CertSelector) selector.clone();
        else
            certSelector = null;
    }

    /**
     * 创建此 {@code PKIXParameters} 对象的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code PKIXParameters} 对象的副本
     */
    public Object clone() {
        try {
            PKIXParameters copy = (PKIXParameters)super.clone();

            // 必须克隆这些，因为 addCertStore 等方法会修改它们
            if (certStores != null) {
                copy.certStores = new ArrayList<CertStore>(certStores);
            }
            if (certPathCheckers != null) {
                copy.certPathCheckers =
                    new ArrayList<PKIXCertPathChecker>(certPathCheckers.size());
                for (PKIXCertPathChecker checker : certPathCheckers) {
                    copy.certPathCheckers.add(
                                    (PKIXCertPathChecker)checker.clone());
                }
            }

            // 其他类字段对公共部分是不可变的，不必克隆只读字段。
            return copy;
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");

        /* 从信任锚信息开始 */
        if (unmodTrustAnchors != null) {
            sb.append("  Trust Anchors: " + unmodTrustAnchors.toString()
                + "\n");
        }

        /* 现在，附加初始状态信息 */
        if (unmodInitialPolicies != null) {
            if (unmodInitialPolicies.isEmpty()) {
                sb.append("  Initial Policy OIDs: any\n");
            } else {
                sb.append("  Initial Policy OIDs: ["
                    + unmodInitialPolicies.toString() + "]\n");
            }
        }

        /* 现在，附加路径中所有证书的约束条件 */
        sb.append("  Validity Date: " + String.valueOf(date) + "\n");
        sb.append("  Signature Provider: " + String.valueOf(sigProvider) + "\n");
        sb.append("  Default Revocation Enabled: " + revocationEnabled + "\n");
        sb.append("  Explicit Policy Required: " + explicitPolicyRequired + "\n");
        sb.append("  Policy Mapping Inhibited: " + policyMappingInhibited + "\n");
        sb.append("  Any Policy Inhibited: " + anyPolicyInhibited + "\n");
        sb.append("  Policy Qualifiers Rejected: " + policyQualifiersRejected + "\n");

        /* 现在，附加目标证书要求 */
        sb.append("  Target Cert Constraints: " + String.valueOf(certSelector) + "\n");

        /* 最后，附加其他参数 */
        if (certPathCheckers != null)
            sb.append("  Certification Path Checkers: ["
                + certPathCheckers.toString() + "]\n");
        if (certStores != null)
            sb.append("  CertStores: [" + certStores.toString() + "]\n");
        sb.append("]");
        return sb.toString();
    }
}
