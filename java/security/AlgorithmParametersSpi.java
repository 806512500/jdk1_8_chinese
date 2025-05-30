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
 * 本类定义了 {@code AlgorithmParameters} 类的 <i>服务提供者接口</i>（<b>SPI</b>），用于管理算法参数。
 *
 * <p> 所有在本类中的抽象方法都必须由每个希望为特定算法提供参数管理的加密服务提供者实现。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 * @see java.security.spec.DSAParameterSpec
 *
 * @since 1.2
 */

public abstract class AlgorithmParametersSpi {

    /**
     * 使用 {@code paramSpec} 中指定的参数初始化此参数对象。
     *
     * @param paramSpec 参数规范。
     *
     * @exception InvalidParameterSpecException 如果给定的参数规范不适用于此参数对象的初始化。
     */
    protected abstract void engineInit(AlgorithmParameterSpec paramSpec)
        throws InvalidParameterSpecException;

    /**
     * 导入指定的参数并根据参数的主要解码格式进行解码。
     * 参数的主要解码格式是 ASN.1，如果存在此类型参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @exception IOException 解码错误
     */
    protected abstract void engineInit(byte[] params)
        throws IOException;

    /**
     * 从 {@code params} 导入参数并根据指定的解码格式进行解码。
     * 如果 {@code format} 为 null，则使用参数的主要解码格式。主要解码格式是 ASN.1，如果存在这些参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @param format 解码格式的名称。
     *
     * @exception IOException 解码错误
     */
    protected abstract void engineInit(byte[] params, String format)
        throws IOException;

    /**
     * 返回此参数对象的（透明）规范。
     * {@code paramSpec} 识别参数应返回的规范类。例如，可以是 {@code DSAParameterSpec.class}，表示参数应返回为 {@code DSAParameterSpec} 类的实例。
     *
     * @param <T> 要返回的参数规范的类型
     *
     * @param paramSpec 参数应返回的规范类。
     *
     * @return 参数规范。
     *
     * @exception InvalidParameterSpecException 如果请求的参数规范不适用于此参数对象。
     */
    protected abstract
        <T extends AlgorithmParameterSpec>
        T engineGetParameterSpec(Class<T> paramSpec)
        throws InvalidParameterSpecException;

    /**
     * 返回参数的主要编码格式。
     * 参数的主要编码格式是 ASN.1，如果存在此类型参数的 ASN.1 规范。
     *
     * @return 使用其主要编码格式编码的参数。
     *
     * @exception IOException 编码错误。
     */
    protected abstract byte[] engineGetEncoded() throws IOException;

    /**
     * 返回使用指定格式编码的参数。
     * 如果 {@code format} 为 null，则使用参数的主要编码格式。主要编码格式是 ASN.1，如果存在这些参数的 ASN.1 规范。
     *
     * @param format 编码格式的名称。
     *
     * @return 使用指定编码方案编码的参数。
     *
     * @exception IOException 编码错误。
     */
    protected abstract byte[] engineGetEncoded(String format)
        throws IOException;

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串。
     */
    protected abstract String engineToString();
}
