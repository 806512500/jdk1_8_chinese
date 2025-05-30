/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 该类定义了 {@code SecureRandom} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 每个希望提供加密强伪随机数生成器实现的服务提供者都必须实现此类中的所有抽象方法。
 *
 *
 * @see SecureRandom
 * @since 1.2
 */

public abstract class SecureRandomSpi implements java.io.Serializable {

    private static final long serialVersionUID = -2991854161009191830L;

    /**
     * 重新播种此随机对象。给定的种子补充而不是替换现有的种子。因此，重复调用保证永远不会减少随机性。
     *
     * @param seed 种子。
     */
    protected abstract void engineSetSeed(byte[] seed);

    /**
     * 生成用户指定数量的随机字节。
     *
     * <p> 如果之前没有调用过 {@code engineSetSeed}，则第一次调用此方法将强制此 SecureRandom 实现进行自我播种。
     * 如果之前调用过 {@code engineSetSeed}，则不会发生自我播种。
     *
     * @param bytes 要填充随机字节的数组。
     */
    protected abstract void engineNextBytes(byte[] bytes);

    /**
     * 生成指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * @param numBytes 要生成的种子字节数。
     *
     * @return 种子字节。
     */
     protected abstract byte[] engineGenerateSeed(int numBytes);
}
