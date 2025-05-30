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

package java.security;

import java.io.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

/**
 * 该类用于表示加密参数的不透明形式。
 *
 * <p>通过调用 {@code getInstance} 工厂方法（静态方法，返回给定类的实例）之一，可以获取用于管理特定算法参数的 {@code AlgorithmParameters} 对象。
 *
 * <p>一旦获取了 {@code AlgorithmParameters} 对象，必须通过调用 {@code init} 并使用适当的参数规范或参数编码来初始化它。
 *
 * <p>通过调用 {@code getParameterSpec} 从 {@code AlgorithmParameters} 对象中获取透明的参数规范，通过调用 {@code getEncoded} 获取参数的字节编码。
 *
 * <p>每个 Java 平台的实现都必须支持以下标准 {@code AlgorithmParameters} 算法：
 * <ul>
 * <li>{@code AES}</li>
 * <li>{@code DES}</li>
 * <li>{@code DESede}</li>
 * <li>{@code DiffieHellman}</li>
 * <li>{@code DSA}</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
 * Java 加密架构标准算法名称文档的 AlgorithmParameters 部分</a> 中描述。
 * 请参阅您的实现的发行文档，以了解是否支持其他算法。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.spec.AlgorithmParameterSpec
 * @see java.security.spec.DSAParameterSpec
 * @see KeyPairGenerator
 *
 * @since 1.2
 */

public class AlgorithmParameters {

    // 提供者
    private Provider provider;

    // 提供者实现（委托）
    private AlgorithmParametersSpi paramSpi;

    // 算法
    private String algorithm;

    // 该对象是否已初始化？
    private boolean initialized = false;

    /**
     * 创建一个 AlgorithmParameters 对象。
     *
     * @param paramSpi 委托
     * @param provider 提供者
     * @param algorithm 算法
     */
    protected AlgorithmParameters(AlgorithmParametersSpi paramSpi,
                                  Provider provider, String algorithm)
    {
        this.paramSpi = paramSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * 返回与此参数对象关联的算法名称。
     *
     * @return 算法名称。
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 返回指定算法的参数对象。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 AlgorithmParameters 对象，封装了支持指定算法的第一个提供者的 AlgorithmParametersSpi 实现。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p>返回的参数对象必须通过调用 {@code init} 并使用适当的参数规范或参数编码来初始化。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java 加密架构标准算法名称文档的 AlgorithmParameters 部分</a>。
     *
     * @return 新的参数对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的 AlgorithmParametersSpi 实现。
     *
     * @see Provider
     */
    public static AlgorithmParameters getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm, "AlgorithmParameters",
                                             (String)null);
            return new AlgorithmParameters((AlgorithmParametersSpi)objs[0],
                                           (Provider)objs[1],
                                           algorithm);
        } catch(NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        }
    }

    /**
     * 返回指定算法的参数对象。
     *
     * <p>返回一个新的 AlgorithmParameters 对象，封装了指定提供者的 AlgorithmParametersSpi 实现。指定的提供者必须注册在安全提供者列表中。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p>返回的参数对象必须通过调用 {@code init} 并使用适当的参数规范或参数编码来初始化。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java 加密架构标准算法名称文档的 AlgorithmParameters 部分</a>。
     *
     * @param provider 提供者的名称。
     *
     * @return 新的参数对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的 AlgorithmParametersSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为空字符串。
     *
     * @see Provider
     */
    public static AlgorithmParameters getInstance(String algorithm,
                                                  String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("缺少提供者");
        Object[] objs = Security.getImpl(algorithm, "AlgorithmParameters",
                                         provider);
        return new AlgorithmParameters((AlgorithmParametersSpi)objs[0],
                                       (Provider)objs[1],
                                       algorithm);
    }

    /**
     * 返回指定算法的参数对象。
     *
     * <p>返回一个新的 AlgorithmParameters 对象，封装了指定 Provider 对象的 AlgorithmParametersSpi 实现。注意，指定的 Provider 对象不必注册在提供者列表中。
     *
     * <p>返回的参数对象必须通过调用 {@code init} 并使用适当的参数规范或参数编码来初始化。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java 加密架构标准算法名称文档的 AlgorithmParameters 部分</a>。
     *
     * @param provider 提供者的名称。
     *
     * @return 新的参数对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的 AlgorithmParameterGeneratorSpi 实现。
     *
     * @exception IllegalArgumentException 如果提供者为空。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static AlgorithmParameters getInstance(String algorithm,
                                                  Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("缺少提供者");
        Object[] objs = Security.getImpl(algorithm, "AlgorithmParameters",
                                         provider);
        return new AlgorithmParameters((AlgorithmParametersSpi)objs[0],
                                       (Provider)objs[1],
                                       algorithm);
    }

    /**
     * 返回此参数对象的提供者。
     *
     * @return 此参数对象的提供者
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 使用 {@code paramSpec} 中指定的参数初始化此参数对象。
     *
     * @param paramSpec 参数规范。
     *
     * @exception InvalidParameterSpecException 如果给定的参数规范不适合初始化此参数对象，或者此参数对象已初始化。
     */
    public final void init(AlgorithmParameterSpec paramSpec)
        throws InvalidParameterSpecException
    {
        if (this.initialized)
            throw new InvalidParameterSpecException("已初始化");
        paramSpi.engineInit(paramSpec);
        this.initialized = true;
    }

    /**
     * 导入指定的参数并根据参数的主要解码格式进行解码。参数的主要解码格式是 ASN.1，如果存在该类型参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @exception IOException 在解码错误时，或者此参数对象已初始化时。
     */
    public final void init(byte[] params) throws IOException {
        if (this.initialized)
            throw new IOException("已初始化");
        paramSpi.engineInit(params);
        this.initialized = true;
    }

    /**
     * 从 {@code params} 导入参数并根据指定的解码方案进行解码。
     * 如果 {@code format} 为 null，则使用参数的主要解码格式。参数的主要解码格式是 ASN.1，如果存在该类型参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @param format 解码方案的名称。
     *
     * @exception IOException 在解码错误时，或者此参数对象已初始化时。
     */
    public final void init(byte[] params, String format) throws IOException {
        if (this.initialized)
            throw new IOException("已初始化");
        paramSpi.engineInit(params, format);
        this.initialized = true;
    }

    /**
     * 返回此参数对象的（透明）规范。
     * {@code paramSpec} 识别参数应返回的规范类。例如，可以是 {@code DSAParameterSpec.class}，表示参数应返回为 {@code DSAParameterSpec} 类的实例。
     *
     * @param <T> 要返回的参数规范的类型
     * @param paramSpec 参数应返回的规范类。
     *
     * @return 参数规范。
     *
     * @exception InvalidParameterSpecException 如果请求的参数规范不适合此参数对象，或者此参数对象未初始化。
     */
    public final <T extends AlgorithmParameterSpec>
        T getParameterSpec(Class<T> paramSpec)
        throws InvalidParameterSpecException
    {
        if (this.initialized == false) {
            throw new InvalidParameterSpecException("未初始化");
        }
        return paramSpi.engineGetParameterSpec(paramSpec);
    }

    /**
     * 返回参数的主要编码格式。
     * 参数的主要编码格式是 ASN.1，如果存在该类型参数的 ASN.1 规范。
     *
     * @return 使用主要编码格式编码的参数。
     *
     * @exception IOException 在编码错误时，或者此参数对象未初始化时。
     */
    public final byte[] getEncoded() throws IOException
    {
        if (this.initialized == false) {
            throw new IOException("未初始化");
        }
        return paramSpi.engineGetEncoded();
    }

    /**
     * 返回使用指定方案编码的参数。
     * 如果 {@code format} 为 null，则使用参数的主要编码格式。参数的主要编码格式是 ASN.1，如果存在该类型参数的 ASN.1 规范。
     *
     * @param format 编码格式的名称。
     *
     * @return 使用指定编码方案编码的参数。
     *
     * @exception IOException 在编码错误时，或者此参数对象未初始化时。
     */
    public final byte[] getEncoded(String format) throws IOException
    {
        if (this.initialized == false) {
            throw new IOException("未初始化");
        }
        return paramSpi.engineGetEncoded(format);
    }

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串，如果此参数对象未初始化，则返回 null。
     */
    public final String toString() {
        if (this.initialized == false) {
            return null;
        }
        return paramSpi.engineToString();
    }
}
