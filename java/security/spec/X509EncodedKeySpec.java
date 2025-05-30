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
 * 该类表示公钥的 ASN.1 编码，根据 ASN.1 类型 {@code SubjectPublicKeyInfo} 编码。
 * {@code SubjectPublicKeyInfo} 语法在 X.509 标准中定义如下：
 *
 * <pre>
 * SubjectPublicKeyInfo ::= SEQUENCE {
 *   algorithm AlgorithmIdentifier,
 *   subjectPublicKey BIT STRING }
 * </pre>
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 * @see PKCS8EncodedKeySpec
 *
 * @since 1.2
 */

public class X509EncodedKeySpec extends EncodedKeySpec {

    /**
     * 使用给定的编码密钥创建新的 X509EncodedKeySpec。
     *
     * @param encodedKey 编码的密钥，假定其编码符合 X.509 标准。数组的内容将被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code encodedKey} 为 null。
     */
    public X509EncodedKeySpec(byte[] encodedKey) {
        super(encodedKey);
    }

    /**
     * 返回根据 X.509 标准编码的密钥字节。
     *
     * @return X.509 编码的密钥。每次调用此方法时返回一个新的数组。
     */
    public byte[] getEncoded() {
        return super.getEncoded();
    }

    /**
     * 返回与此密钥规范关联的编码格式的名称。
     *
     * @return 字符串 {@code "X.509"}。
     */
    public final String getFormat() {
        return "X.509";
    }
}
