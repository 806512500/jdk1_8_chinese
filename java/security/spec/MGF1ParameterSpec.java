/*
 * Copyright (c) 2003, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.security.spec.AlgorithmParameterSpec;

/**
 * 该类指定了在OAEP填充和RSASSA-PSS签名方案中使用的掩码生成函数MGF1的参数集，
 * 如<a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a>标准中定义的。
 *
 * <p>其在PKCS#1标准中的ASN.1定义如下：
 * <pre>
 * PKCS1MGFAlgorithms    ALGORITHM-IDENTIFIER ::= {
 *   { OID id-mgf1 PARAMETERS HashAlgorithm },
 *   ...  -- 允许未来的扩展 --
 * }
 * </pre>
 * 其中
 * <pre>
 * HashAlgorithm ::= AlgorithmIdentifier {
 *   {OAEP-PSSDigestAlgorithms}
 * }
 *
 * OAEP-PSSDigestAlgorithms    ALGORITHM-IDENTIFIER ::= {
 *   { OID id-sha1       PARAMETERS NULL }|
 *   { OID id-sha224     PARAMETERS NULL }|
 *   { OID id-sha256     PARAMETERS NULL }|
 *   { OID id-sha384     PARAMETERS NULL }|
 *   { OID id-sha512     PARAMETERS NULL }|
 *   { OID id-sha512-224 PARAMETERS NULL }|
 *   { OID id-sha512-256 PARAMETERS NULL },
 *   ...  -- 允许未来的扩展 --
 * }
 * </pre>
 * @see PSSParameterSpec
 * @see javax.crypto.spec.OAEPParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class MGF1ParameterSpec implements AlgorithmParameterSpec {

    /**
     * 使用"SHA-1"消息摘要的MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA1 =
        new MGF1ParameterSpec("SHA-1");

    /**
     * 使用"SHA-224"消息摘要的MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA224 =
        new MGF1ParameterSpec("SHA-224");

    /**
     * 使用"SHA-256"消息摘要的MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA256 =
        new MGF1ParameterSpec("SHA-256");

    /**
     * 使用"SHA-384"消息摘要的MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA384 =
        new MGF1ParameterSpec("SHA-384");

    /**
     * 使用SHA-512消息摘要的MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA512 =
        new MGF1ParameterSpec("SHA-512");

    /**
     * 使用SHA-512/224消息摘要的MGF1ParameterSpec
     *
     * @apiNote 该字段定义在Java SE 8 Maintenance Release 3中。
     * @since 8
     */
    public static final MGF1ParameterSpec SHA512_224 =
        new MGF1ParameterSpec("SHA-512/224");

    /**
     * 使用SHA-512/256消息摘要的MGF1ParameterSpec
     *
     * @apiNote 该字段定义在Java SE 8 Maintenance Release 3中。
     * @since 8
     */
    public static final MGF1ParameterSpec SHA512_256 =
        new MGF1ParameterSpec("SHA-512/256");

    private String mdName;

    /**
     * 构造一个在PKCS #1标准中定义的掩码生成函数MGF1的参数集。
     *
     * @param mdName 用于此掩码生成函数MGF1的消息摘要算法名称。
     * @exception NullPointerException 如果 {@code mdName} 为 null。
     */
    public MGF1ParameterSpec(String mdName) {
        if (mdName == null) {
            throw new NullPointerException("消息摘要算法为 null");
        }
        this.mdName = mdName;
    }

    /**
     * 返回掩码生成函数使用的消息摘要算法名称。
     *
     * @return 消息摘要算法的名称。
     */
    public String getDigestAlgorithm() {
        return mdName;
    }
}
