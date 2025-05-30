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

package java.security.spec;

/**
 * 此类表示以编码格式表示的公钥或私钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see X509EncodedKeySpec
 * @see PKCS8EncodedKeySpec
 *
 * @since 1.2
 */

public abstract class EncodedKeySpec implements KeySpec {

    private byte[] encodedKey;

    /**
     * 使用给定的编码密钥创建新的 EncodedKeySpec。
     *
     * @param encodedKey 编码密钥。数组的内容被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code encodedKey}
     * 为 null。
     */
    public EncodedKeySpec(byte[] encodedKey) {
        this.encodedKey = encodedKey.clone();
    }

    /**
     * 返回编码密钥。
     *
     * @return 编码密钥。每次调用此方法时返回一个新的数组。
     */
    public byte[] getEncoded() {
        return this.encodedKey.clone();
    }

    /**
     * 返回与此密钥规范关联的编码格式的名称。
     *
     * <p>如果密钥的不透明表示形式
     * （参见 {@link java.security.Key Key}）可以转换
     * （参见 {@link java.security.KeyFactory KeyFactory}）
     * 为此密钥规范（或其子类），
     * 则调用不透明密钥的 {@code getFormat}
     * 返回与该密钥规范的 {@code getFormat} 方法
     * 返回的相同值。
     *
     * @return 编码格式的字符串表示形式。
     */
    public abstract String getFormat();
}
