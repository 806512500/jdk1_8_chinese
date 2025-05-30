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

import java.security.spec.AlgorithmParameterSpec;

/**
 * 该类定义了 {@code AlgorithmParameterGenerator} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于生成要与特定算法一起使用的一组参数。
 *
 * <p> 本类中的所有抽象方法都必须由每个希望为特定算法提供参数生成器实现的
 * 密码服务提供商实现（并记录）。
 *
 * <p> 如果客户端未显式初始化 AlgorithmParameterGenerator（通过调用 {@code engineInit}
 * 方法），则每个提供者必须提供（并记录）默认初始化。例如，Sun 提供者使用 1024 位的默认模数素数大小
 * 生成 DSA 参数。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameterGenerator
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 *
 * @since 1.2
 */

public abstract class AlgorithmParameterGeneratorSpi {

    /**
     * 使用特定的大小和随机源初始化此参数生成器。
     *
     * @param size 大小（位数）。
     * @param random 随机源。
     */
    protected abstract void engineInit(int size, SecureRandom random);

    /**
     * 使用一组算法特定的参数生成值初始化此参数生成器。
     *
     * @param genParamSpec 算法特定的参数生成值集。
     * @param random 随机源。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数生成值不适用于此参数生成器。
     */
    protected abstract void engineInit(AlgorithmParameterSpec genParamSpec,
                                       SecureRandom random)
        throws InvalidAlgorithmParameterException;

    /**
     * 生成参数。
     *
     * @return 新的 AlgorithmParameters 对象。
     */
    protected abstract AlgorithmParameters engineGenerateParameters();
}
