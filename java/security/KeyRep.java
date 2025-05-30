/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Locale;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * 序列化密钥对象的标准表示。
 *
 * <p>
 *
 * 注意，序列化的密钥可能包含不应在不受信任的环境中暴露的敏感信息。有关更多信息，请参阅
 * <a href="../../../platform/serialization/spec/security.html">
 * 序列化规范的安全附录</a>。
 *
 * @see Key
 * @see KeyFactory
 * @see javax.crypto.spec.SecretKeySpec
 * @see java.security.spec.X509EncodedKeySpec
 * @see java.security.spec.PKCS8EncodedKeySpec
 *
 * @since 1.5
 */

public class KeyRep implements Serializable {

    private static final long serialVersionUID = -4757683898830641853L;

    /**
     * 密钥类型。
     *
     * @since 1.5
     */
    public static enum Type {

        /** 秘密密钥的类型。 */
        SECRET,

        /** 公钥的类型。 */
        PUBLIC,

        /** 私钥的类型。 */
        PRIVATE,

    }

    private static final String PKCS8 = "PKCS#8";
    private static final String X509 = "X.509";
    private static final String RAW = "RAW";

    /**
     * Type.SECRET、Type.PUBLIC 或 Type.PRIVATE 中的一个。
     *
     * @serial
     */
    private Type type;

    /**
     * 密钥算法。
     *
     * @serial
     */
    private String algorithm;

    /**
     * 密钥编码格式。
     *
     * @serial
     */
    private String format;

    /**
     * 编码后的密钥字节。
     *
     * @serial
     */
    private byte[] encoded;

    /**
     * 构建备用的密钥类。
     *
     * <p>
     *
     * @param type Type.SECRET、Type.PUBLIC 或 Type.PRIVATE 中的一个。
     * @param algorithm 从 {@code Key.getAlgorithm()} 返回的算法。
     * @param format 从 {@code Key.getFormat()} 返回的编码格式。
     * @param encoded 从 {@code Key.getEncoded()} 返回的编码字节。
     *
     * @exception NullPointerException
     *          如果 type 为 {@code null}，
     *          如果 algorithm 为 {@code null}，
     *          如果 format 为 {@code null}，
     *          或者如果 encoded 为 {@code null}
     */
    public KeyRep(Type type, String algorithm,
                String format, byte[] encoded) {

        if (type == null || algorithm == null ||
            format == null || encoded == null) {
            throw new NullPointerException("无效的 null 输入");
        }

        this.type = type;
        this.algorithm = algorithm;
        this.format = format.toUpperCase(Locale.ENGLISH);
        this.encoded = encoded.clone();
    }

    /**
     * 解析密钥对象。
     *
     * <p> 该方法支持三种 Type/format 组合：
     * <ul>
     * <li> Type.SECRET/"RAW" - 返回一个使用编码密钥字节和算法构建的 SecretKeySpec 对象
     * <li> Type.PUBLIC/"X.509" - 获取密钥算法的 KeyFactory 实例，使用编码密钥字节构建一个 X509EncodedKeySpec，并从 spec 生成公钥
     * <li> Type.PRIVATE/"PKCS#8" - 获取密钥算法的 KeyFactory 实例，使用编码密钥字节构建一个 PKCS8EncodedKeySpec，并从 spec 生成私钥
     * </ul>
     *
     * <p>
     *
     * @return 解析后的密钥对象
     *
     * @exception ObjectStreamException 如果 Type/format 组合未被识别，如果算法、密钥格式或编码密钥字节未被识别/无效，或者如果密钥解析因任何原因失败
     */
    protected Object readResolve() throws ObjectStreamException {
        try {
            if (type == Type.SECRET && RAW.equals(format)) {
                return new SecretKeySpec(encoded, algorithm);
            } else if (type == Type.PUBLIC && X509.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePublic(new X509EncodedKeySpec(encoded));
            } else if (type == Type.PRIVATE && PKCS8.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            } else {
                throw new NotSerializableException
                        ("未识别的 type/format 组合: " +
                        type + "/" + format);
            }
        } catch (NotSerializableException nse) {
            throw nse;
        } catch (Exception e) {
            NotSerializableException nse = new NotSerializableException
                                        ("java.security.Key: " +
                                        "[" + type + "] " +
                                        "[" + algorithm + "] " +
                                        "[" + format + "]");
            nse.initCause(e);
            throw nse;
        }
    }
}
