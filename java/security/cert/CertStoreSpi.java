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
import java.util.Collection;

/**
 * 《服务提供者接口》（<b>SPI</b>）
 * 用于 {@link CertStore CertStore} 类。所有 {@code CertStore}
 * 实现都必须包含一个类（SPI 类），该类扩展了
 * 此类（{@code CertStoreSpi}），提供一个类型为 {@code CertStoreParameters} 的单参数构造器，并实现
 * 其所有方法。通常，此类的实例应仅通过 {@code CertStore} 类访问。
 * 有关详细信息，请参阅 Java 密码架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 所有 {@code CertStoreSpi} 对象的公共方法必须是线程安全的。也就是说，多个线程可以同时调用这些
 * 方法，而不会产生不良影响。这允许 {@code CertPathBuilder} 在同时搜索更多证书的同时搜索 CRL，例如。
 * <p>
 * 简单的 {@code CertStoreSpi} 实现可能通过在其
 * {@code engineGetCertificates} 和 {@code engineGetCRLs} 方法中添加 {@code synchronized} 关键字来确保线程安全。
 * 更复杂的实现可能允许真正的并发访问。
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public abstract class CertStoreSpi {

    /**
     * 唯一的构造器。
     *
     * @param params 初始化参数（可以为 {@code null}）
     * @throws InvalidAlgorithmParameterException 如果初始化
     * 参数不适合此 {@code CertStoreSpi}
     */
    public CertStoreSpi(CertStoreParameters params)
    throws InvalidAlgorithmParameterException { }

    /**
     * 返回与指定选择器匹配的 {@code Certificate} 集合。如果没有
     * 与选择器匹配的 {@code Certificate}，将返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，结果
     * {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code Certificate}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。相反，它可能只搜索可能包含
     * 所需 {@code Certificate} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（特别是 LDAP
     * {@code CertStore}）可能在未提供包含可用于查找证书的具体标准的非空 {@code CertSelector} 时抛出 {@code CertStoreException}。发行者和/或主体名称是特别有用的条件。
     *
     * @param selector 用于选择应返回哪些
     *  {@code Certificate} 的 {@code CertSelector}。指定 {@code null}
     *  以返回所有 {@code Certificate}（如果支持）。
     * @return 与指定选择器匹配的 {@code Certificate} 集合（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public abstract Collection<? extends Certificate> engineGetCertificates
            (CertSelector selector) throws CertStoreException;

    /**
     * 返回与指定选择器匹配的 {@code CRL} 集合。如果没有
     * 与选择器匹配的 {@code CRL}，将返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，结果
     * {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code CRL}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。相反，它可能只搜索可能包含
     * 所需 {@code CRL} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（特别是 LDAP
     * {@code CertStore}）可能在未提供包含可用于查找 CRL 的具体标准的非空 {@code CRLSelector} 时抛出 {@code CertStoreException}。发行者名称和/或要检查的证书是特别有用的条件。
     *
     * @param selector 用于选择应返回哪些
     *  {@code CRL} 的 {@code CRLSelector}。指定 {@code null}
     *  以返回所有 {@code CRL}（如果支持）。
     * @return 与指定选择器匹配的 {@code CRL} 集合（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public abstract Collection<? extends CRL> engineGetCRLs
            (CRLSelector selector) throws CertStoreException;
}
