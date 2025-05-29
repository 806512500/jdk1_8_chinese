/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.security.InvalidAlgorithmParameterException;
import java.util.Collection;

/**
 * 《服务提供者接口》（SPI）为 {@link CertStore CertStore} 类提供支持。所有 {@code CertStore}
 * 实现都必须包含一个类（SPI 类），该类扩展了此类（{@code CertStoreSpi}），提供一个类型为
 * {@code CertStoreParameters} 的单参数构造函数，并实现所有方法。通常，此类的实例应仅通过
 * {@code CertStore} 类访问。详情请参阅 Java 密码架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 所有 {@code CertStoreSpi} 对象的公共方法必须是线程安全的。也就是说，多个线程可以同时调用这些
 * 方法（一个或多个）的单个 {@code CertStoreSpi} 对象，而不会产生不良影响。例如，这允许
 * {@code CertPathBuilder} 在同时搜索更多证书的同时搜索 CRL。
 * <p>
 * 简单的 {@code CertStoreSpi} 实现可能会通过在其 {@code engineGetCertificates} 和
 * {@code engineGetCRLs} 方法中添加 {@code synchronized} 关键字来确保线程安全。更复杂的实现可能允许真正的并发访问。
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public abstract class CertStoreSpi {

    /**
     * 唯一的构造函数。
     *
     * @param params 初始化参数（可以为 {@code null}）
     * @throws InvalidAlgorithmParameterException 如果初始化参数不适用于此 {@code CertStoreSpi}
     */
    public CertStoreSpi(CertStoreParameters params)
    throws InvalidAlgorithmParameterException { }

    /**
     * 返回与指定选择器匹配的 {@code Certificate} 集合。如果没有 {@code Certificate} 与选择器匹配，
     * 将返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，返回的 {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code Certificate}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。相反，它可能只搜索可能包含
     * 所需 {@code Certificate} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（尤其是 LDAP {@code CertStore}）可能在不提供包含特定查找证书标准的非空
     * {@code CertSelector} 时抛出 {@code CertStoreException}。发行者和/或主题名称是特别有用的查找标准。
     *
     * @param selector 用于选择应返回哪些 {@code Certificate} 的 {@code CertSelector}。指定 {@code null}
     * 以返回所有 {@code Certificate}（如果支持）。
     * @return 与指定选择器匹配的 {@code Certificate} 集合（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public abstract Collection<? extends Certificate> engineGetCertificates
            (CertSelector selector) throws CertStoreException;

    /**
     * 返回与指定选择器匹配的 {@code CRL} 集合。如果没有 {@code CRL} 与选择器匹配，
     * 将返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，返回的 {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code CRL}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。相反，它可能只搜索可能包含
     * 所需 {@code CRL} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（尤其是 LDAP {@code CertStore}）可能在不提供包含特定查找 CRL 标准的非空
     * {@code CRLSelector} 时抛出 {@code CertStoreException}。发行者名称和/或要检查的证书是特别有用的查找标准。
     *
     * @param selector 用于选择应返回哪些 {@code CRL} 的 {@code CRLSelector}。指定 {@code null}
     * 以返回所有 {@code CRL}（如果支持）。
     * @return 与指定选择器匹配的 {@code CRL} 集合（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public abstract Collection<? extends CRL> engineGetCRLs
            (CRLSelector selector) throws CertStoreException;
}
