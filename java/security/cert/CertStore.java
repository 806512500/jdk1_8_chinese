
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

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * 用于从存储库中检索 {@code Certificate} 和 {@code CRL} 的类。
 * <p>
 * 该类使用基于提供者的架构。
 * 要创建一个 {@code CertStore}，请调用其中一个静态的
 * {@code getInstance} 方法，传入所需的 {@code CertStore} 类型、
 * 适用的初始化参数以及可选的提供者名称。
 * <p>
 * 创建 {@code CertStore} 后，可以通过调用其
 * {@link #getCertificates(CertSelector selector) getCertificates} 和
 * {@link #getCRLs(CRLSelector selector) getCRLs} 方法来检索 {@code Certificate} 和 {@code CRL}。
 * <p>
 * 与提供对私钥和受信任证书缓存访问的 {@link java.security.KeyStore KeyStore} 不同，
 * {@code CertStore} 旨在提供对大量不受信任的证书和 CRL 的访问。例如，LDAP
 * 实现的 {@code CertStore} 通过 LDAP 协议和 RFC 服务属性定义的模式访问存储在目录中的证书和 CRL。
 *
 * <p> 每个 Java 平台的实现都必须支持以下标准的 {@code CertStore} 类型：
 * <ul>
 * <li>{@code Collection}</li>
 * </ul>
 * 该类型在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertStore">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 * 的 {@code CertStore} 部分中描述。请参阅您的实现的发行文档，了解是否支持其他类型。
 *
 * <p>
 * <b>并发访问</b>
 * <p>
 * 所有 {@code CertStore} 对象的公共方法都必须是线程安全的。
 * 即，多个线程可以同时调用这些方法，而不会产生不良影响。这允许 {@code CertPathBuilder}
 * 同时搜索 CRL 和进一步的证书，例如。
 * <p>
 * 该类的静态方法也保证是线程安全的。
 * 多个线程可以同时调用该类中定义的静态方法，而不会产生不良影响。
 *
 * @since       1.4
 * @author      Sean Mullan, Steve Hanna
 */
public class CertStore {
    /*
     * 用于在 Security 属性文件中查找默认的 certstore 类型的常量。
     * 在 Security 属性文件中，默认的 certstore 类型如下所示：
     * <pre>
     * certstore.type=LDAP
     * </pre>
     */
    private static final String CERTSTORE_TYPE = "certstore.type";
    private CertStoreSpi storeSpi;
    private Provider provider;
    private String type;
    private CertStoreParameters params;

    /**
     * 创建一个给定类型的 {@code CertStore} 对象，并将给定的提供者实现（SPI 对象）封装在其中。
     *
     * @param storeSpi 提供者实现
     * @param provider 提供者
     * @param type 类型
     * @param params 初始化参数（可以为 {@code null}）
     */
    protected CertStore(CertStoreSpi storeSpi, Provider provider,
                        String type, CertStoreParameters params) {
        this.storeSpi = storeSpi;
        this.provider = provider;
        this.type = type;
        if (params != null)
            this.params = (CertStoreParameters) params.clone();
    }

    /**
     * 返回与指定选择器匹配的 {@code Certificate} 的 {@code Collection}。
     * 如果没有 {@code Certificate} 与选择器匹配，则返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，结果的
     * {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code Certificate}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。
     * 相反，它可能只搜索可能包含所需 {@code Certificate} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（特别是 LDAP
     * {@code CertStore}）可能在未提供非空的包含用于查找证书的具体标准的
     * {@code CertSelector} 时抛出 {@code CertStoreException}。
     * 发行者和/或主体名称是特别有用的筛选标准。
     *
     * @param selector 用于选择应返回哪些 {@code Certificate} 的 {@code CertSelector}。
     *  指定 {@code null} 以返回所有 {@code Certificate}（如果支持）。
     * @return 与指定选择器匹配的 {@code Certificate} 的 {@code Collection}（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public final Collection<? extends Certificate> getCertificates
            (CertSelector selector) throws CertStoreException {
        return storeSpi.engineGetCertificates(selector);
    }

    /**
     * 返回与指定选择器匹配的 {@code CRL} 的 {@code Collection}。
     * 如果没有 {@code CRL} 与选择器匹配，则返回一个空的 {@code Collection}。
     * <p>
     * 对于某些 {@code CertStore} 类型，结果的
     * {@code Collection} 可能不包含 <b>所有</b> 与选择器匹配的
     * {@code CRL}。例如，LDAP {@code CertStore} 可能不会搜索目录中的所有条目。
     * 相反，它可能只搜索可能包含所需 {@code CRL} 的条目。
     * <p>
     * 一些 {@code CertStore} 实现（特别是 LDAP
     * {@code CertStore}）可能在未提供非空的包含用于查找 CRL 的具体标准的
     * {@code CRLSelector} 时抛出 {@code CertStoreException}。
     * 发行者名称和/或要检查的证书是特别有用的筛选标准。
     *
     * @param selector 用于选择应返回哪些 {@code CRL} 的 {@code CRLSelector}。
     *  指定 {@code null} 以返回所有 {@code CRL}（如果支持）。
     * @return 与指定选择器匹配的 {@code CRL} 的 {@code Collection}（从不为 {@code null}）
     * @throws CertStoreException 如果发生异常
     */
    public final Collection<? extends CRL> getCRLs(CRLSelector selector)
            throws CertStoreException {
        return storeSpi.engineGetCRLs(selector);
    }

    /**
     * 返回实现指定的 {@code CertStore} 类型并使用指定参数初始化的 {@code CertStore} 对象。
     *
     * <p> 该方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 {@code CertStore} 对象，封装了支持指定类型的第一个
     * 提供者的 {@code CertStoreSpi} 实现。
     *
     * <p> 注册提供者的列表可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法获取。
     *
     * <p> 返回的 {@code CertStore} 使用指定的 {@code CertStoreParameters} 初始化。
     * 不同类型的 {@code CertStore} 可能需要不同类型的参数。
     * 注意，指定的 {@code CertStoreParameters} 对象会被克隆。
     *
     * @param type 请求的 {@code CertStore} 类型的名称。
     *  有关标准类型的详细信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertStore">
     *  Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *  中的 {@code CertStore} 部分。
     *
     * @param params 初始化参数（可以为 {@code null}）。
     *
     * @return 实现指定的 {@code CertStore} 类型的 {@code CertStore} 对象。
     *
     * @throws NoSuchAlgorithmException 如果没有提供者支持指定类型的
     *  {@code CertStoreSpi} 实现。
     *
     * @throws InvalidAlgorithmParameterException 如果指定的初始化参数不适用于此
     *  {@code CertStore}。
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type, CertStoreParameters params)
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException {
        try {
            Instance instance = GetInstance.getInstance("CertStore",
                CertStoreSpi.class, type, params);
            return new CertStore((CertStoreSpi)instance.impl,
                instance.provider, type, params);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    private static CertStore handleException(NoSuchAlgorithmException e)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidAlgorithmParameterException) {
            throw (InvalidAlgorithmParameterException)cause;
        }
        throw e;
    }

    /**
     * 返回实现指定的 {@code CertStore} 类型的 {@code CertStore} 对象。
     *
     * <p> 返回一个新的 {@code CertStore} 对象，封装了指定提供者的
     * {@code CertStoreSpi} 实现。指定的提供者必须注册在安全提供者列表中。
     *
     * <p> 注册提供者的列表可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法获取。
     *
     * <p> 返回的 {@code CertStore} 使用指定的 {@code CertStoreParameters} 初始化。
     * 不同类型的 {@code CertStore} 可能需要不同类型的参数。
     * 注意，指定的 {@code CertStoreParameters} 对象会被克隆。
     *
     * @param type 请求的 {@code CertStore} 类型。
     *  有关标准类型的详细信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertStore">
     *  Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *  中的 {@code CertStore} 部分。
     *
     * @param params 初始化参数（可以为 {@code null}）。
     *
     * @param provider 提供者的名称。
     *
     * @return 实现指定类型的 {@code CertStore} 对象。
     *
     * @throws NoSuchAlgorithmException 如果指定提供者不支持指定类型的
     *  {@code CertStoreSpi} 实现。
     *
     * @throws InvalidAlgorithmParameterException 如果指定的初始化参数不适用于此
     *  {@code CertStore}。
     *
     * @throws NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *  null 或空。
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type,
            CertStoreParameters params, String provider)
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        try {
            Instance instance = GetInstance.getInstance("CertStore",
                CertStoreSpi.class, type, params, provider);
            return new CertStore((CertStoreSpi)instance.impl,
                instance.provider, type, params);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }

    /**
     * 返回实现指定的 {@code CertStore} 类型的 {@code CertStore} 对象。
     *
     * <p> 返回一个新的 {@code CertStore} 对象，封装了指定 {@code Provider} 对象的
     * {@code CertStoreSpi} 实现。注意，指定的 {@code Provider} 对象
     * 不必注册在提供者列表中。
     *
     * <p> 返回的 {@code CertStore} 使用指定的 {@code CertStoreParameters} 初始化。
     * 不同类型的 {@code CertStore} 可能需要不同类型的参数。
     * 注意，指定的 {@code CertStoreParameters} 对象会被克隆。
     *
     * @param type 请求的 {@code CertStore} 类型。
     *  有关标准类型的详细信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertStore">
     *  Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *  中的 {@code CertStore} 部分。
     *
     * @param params 初始化参数（可以为 {@code null}）。
     *
     * @param provider 提供者。
     *
     * @return 实现指定类型的 {@code CertStore} 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定的 {@code Provider} 对象不支持指定类型的
     *  {@code CertStoreSpi} 实现。
     *
     * @throws InvalidAlgorithmParameterException 如果指定的初始化参数不适用于此
     *  {@code CertStore}
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *  null。
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type, CertStoreParameters params,
            Provider provider) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            Instance instance = GetInstance.getInstance("CertStore",
                CertStoreSpi.class, type, params, provider);
            return new CertStore((CertStoreSpi)instance.impl,
                instance.provider, type, params);
        } catch (NoSuchAlgorithmException e) {
            return handleException(e);
        }
    }


                /**
     * 返回用于初始化此 {@code CertStore} 的参数。
     * 注意，返回之前会克隆 {@code CertStoreParameters} 对象。
     *
     * @return 用于初始化此 {@code CertStore} 的参数（可能是 {@code null}）
     */
    public final CertStoreParameters getCertStoreParameters() {
        return (params == null ? null : (CertStoreParameters) params.clone());
    }

    /**
     * 返回此 {@code CertStore} 的类型。
     *
     * @return 此 {@code CertStore} 的类型
     */
    public final String getType() {
        return this.type;
    }

    /**
     * 返回此 {@code CertStore} 的提供者。
     *
     * @return 此 {@code CertStore} 的提供者
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 返回由 {@code certstore.type} 安全属性指定的默认 {@code CertStore} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "LDAP"}。
     *
     * <p>默认的 {@code CertStore} 类型可以用于不想在调用 {@code getInstance} 方法时使用硬编码类型的应用程序，并且在用户未指定类型时提供一个默认的
     * {@code CertStore} 类型。
     *
     * <p>可以通过将 {@code certstore.type} 安全属性的值设置为所需的类型来更改默认的 {@code CertStore} 类型。
     *
     * @see java.security.Security 安全属性
     * @return 由 {@code certstore.type} 安全属性指定的默认 {@code CertStore} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "LDAP"}。
     */
    public final static String getDefaultType() {
        String cstype;
        cstype = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(CERTSTORE_TYPE);
            }
        });
        if (cstype == null) {
            cstype = "LDAP";
        }
        return cstype;
    }
}
