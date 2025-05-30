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
import sun.security.util.Debug;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * 用于构建认证路径（也称为证书链）的类。
 * <p>
 * 本类采用基于提供者的架构。
 * 要创建一个 {@code CertPathBuilder}，请调用
 * 其中一个静态 {@code getInstance} 方法，传入所需的
 * {@code CertPathBuilder} 算法名称，以及可选的提供者名称。
 *
 * <p>一旦创建了 {@code CertPathBuilder} 对象，就可以通过调用
 * {@link #build build} 方法并传入特定于算法的参数来构建认证路径。
 * 如果成功，结果（包括构建的 {@code CertPath}）将
 * 以实现 {@code CertPathBuilderResult} 接口的对象形式返回。
 *
 * <p>{@link #getRevocationChecker} 方法允许应用程序指定
 * 由 {@code CertPathBuilder} 在检查证书撤销状态时使用的
 * 额外的特定于算法的参数和选项。
 * 以下是一个使用 PKIX 算法的示例：
 *
 * <pre>
 * CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
 * PKIXRevocationChecker rc = (PKIXRevocationChecker)cpb.getRevocationChecker();
 * rc.setOptions(EnumSet.of(Option.PREFER_CRLS));
 * params.addCertPathChecker(rc);
 * CertPathBuilderResult cpbr = cpb.build(params);
 * </pre>
 *
 * <p>每个 Java 平台的实现都必须支持以下标准的
 * {@code CertPathBuilder} 算法：
 * <ul>
 * <li>{@code PKIX}</li>
 * </ul>
 * 该算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 * 的 CertPathBuilder 部分中描述。
 * 请参阅您实现的发行文档，了解是否支持其他算法。
 *
 * <p>
 * <b>并发访问</b>
 * <p>
 * 本类的静态方法保证是线程安全的。
 * 多个线程可以并发调用本类定义的静态方法，而不会产生不良影响。
 * <p>
 * 但是，对于本类定义的非静态方法则不成立。
 * 除非特定提供者另有文档说明，否则需要并发访问单个
 * {@code CertPathBuilder} 实例的线程应进行同步并提供必要的锁定。
 * 每个操作不同 {@code CertPathBuilder} 实例的多个线程不需要同步。
 *
 * @see CertPath
 *
 * @since       1.4
 * @author      Sean Mullan
 * @author      Yassir Elley
 */
public class CertPathBuilder {

    /*
     * 用于在 Security 属性文件中查找以确定
     * 默认 certpathbuilder 类型的常量。在 Security 属性文件中，
     * 默认 certpathbuilder 类型如下所示：
     * <pre>
     * certpathbuilder.type=PKIX
     * </pre>
     */
    private static final String CPB_TYPE = "certpathbuilder.type";
    private final CertPathBuilderSpi builderSpi;
    private final Provider provider;
    private final String algorithm;

    /**
     * 创建给定算法的 {@code CertPathBuilder} 对象，
     * 并将给定的提供者实现（SPI 对象）封装在其中。
     *
     * @param builderSpi 提供者实现
     * @param provider 提供者
     * @param algorithm 算法名称
     */
    protected CertPathBuilder(CertPathBuilderSpi builderSpi, Provider provider,
        String algorithm)
    {
        this.builderSpi = builderSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个封装了第一个支持指定算法的提供者的
     * CertPathBuilderSpi 实现的新 CertPathBuilder 对象。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 的 CertPathBuilder 部分。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @throws NoSuchAlgorithmException 如果没有提供者支持指定算法的
     *          CertPathBuilderSpi 实现。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p>返回一个封装了指定提供者的
     * CertPathBuilderSpi 实现的新 CertPathBuilder 对象。
     * 指定的提供者必须注册在安全提供者列表中。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 的 CertPathBuilder 部分。
     *
     * @param provider 提供者名称。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @throws NoSuchAlgorithmException 如果指定提供者不支持指定算法的
     *          CertPathBuilderSpi 实现。
     *
     * @throws NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为空或 null。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm, String provider)
           throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p>返回一个封装了指定 Provider 对象的
     * CertPathBuilderSpi 实现的新 CertPathBuilder 对象。
     * 注意，指定的 Provider 对象不必注册在提供者列表中。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 的 CertPathBuilder 部分。
     *
     * @param provider 提供者。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的
     *          CertPathBuilderSpi 实现。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为 null。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回此 {@code CertPathBuilder} 的提供者。
     *
     * @return 此 {@code CertPathBuilder} 的提供者
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 返回此 {@code CertPathBuilder} 的算法名称。
     *
     * @return 此 {@code CertPathBuilder} 的算法名称
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 尝试使用指定的算法参数集构建认证路径。
     *
     * @param params 算法参数
     * @return 构建算法的结果
     * @throws CertPathBuilderException 如果构建器无法构建满足指定参数的认证路径
     * @throws InvalidAlgorithmParameterException 如果指定的参数不适合此
     *          {@code CertPathBuilder}
     */
    public final CertPathBuilderResult build(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException
    {
        return builderSpi.engineBuild(params);
    }

    /**
     * 返回由 {@code certpathbuilder.type} 安全属性指定的默认
     * {@code CertPathBuilder} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "PKIX"}。
     *
     * <p>默认的 {@code CertPathBuilder} 类型可以用于
     * 不希望在调用 {@code getInstance} 方法时使用硬编码类型的应用程序，
     * 并且在用户未指定自己的类型时提供默认类型。
     *
     * <p>可以通过将 {@code certpathbuilder.type} 安全属性的值设置为所需的类型来
     * 更改默认的 {@code CertPathBuilder} 类型。
     *
     * @see java.security.Security 安全属性
     * @return 由 {@code certpathbuilder.type} 安全属性指定的默认
     * {@code CertPathBuilder} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "PKIX"}。
     */
    public final static String getDefaultType() {
        String cpbtype =
            AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(CPB_TYPE);
                }
            });
        return (cpbtype == null) ? "PKIX" : cpbtype;
    }

    /**
     * 返回封装的 {@code CertPathBuilderSpi} 实现用于检查证书撤销状态的
     * {@code CertPathChecker}。PKIX 实现返回
     * 类型为 {@code PKIXRevocationChecker} 的对象。每次调用此方法
     * 都会返回一个新的 {@code CertPathChecker} 实例。
     *
     * <p>此方法的主要目的是允许调用者指定特定于撤销检查的
     * 额外输入参数和选项。请参阅类描述中的示例。
     *
     * @return 一个 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果服务提供者不支持此方法
     * @since 1.8
     */
    public final CertPathChecker getRevocationChecker() {
        return builderSpi.engineGetRevocationChecker();
    }
}
