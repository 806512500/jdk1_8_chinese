/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 该不可变类指定了用于生成 DSA 参数的参数集，如
 * <a href="http://csrc.nist.gov/publications/fips/fips186-3/fips_186-3.pdf">FIPS 186-3 数字签名标准 (DSS)</a> 中所述。
 *
 * @see AlgorithmParameterSpec
 *
 * @since 8
 */
public final class DSAGenParameterSpec implements AlgorithmParameterSpec {

    private final int pLen;
    private final int qLen;
    private final int seedLen;

    /**
     * 使用 {@code primePLen} 和 {@code subprimeQLen} 创建 DSA 参数生成的域参数规范。
     * {@code subprimeQLen} 的值也用作域参数种子的默认位长度。
     * @param primePLen 所需的素数 P 的位长度。
     * @param subprimeQLen 所需的子素数 Q 的位长度。
     * @exception IllegalArgumentException 如果 {@code primePLen}
     * 或 {@code subprimeQLen} 不符合 FIPS 186-3 的规范。
     */
    public DSAGenParameterSpec(int primePLen, int subprimeQLen) {
        this(primePLen, subprimeQLen, subprimeQLen);
    }

    /**
     * 使用 {@code primePLen}、{@code subprimeQLen} 和 {@code seedLen} 创建 DSA 参数生成的域参数规范。
     * @param primePLen 所需的素数 P 的位长度。
     * @param subprimeQLen 所需的子素数 Q 的位长度。
     * @param seedLen 所需的域参数种子的位长度，应等于或大于 {@code subprimeQLen}。
     * @exception IllegalArgumentException 如果 {@code primePLenLen}、
     * {@code subprimeQLen} 或 {@code seedLen} 不符合 FIPS 186-3 的规范。
     */
    public DSAGenParameterSpec(int primePLen, int subprimeQLen, int seedLen) {
        switch (primePLen) {
        case 1024:
            if (subprimeQLen != 160) {
                throw new IllegalArgumentException
                    ("subprimeQLen 必须为 160，当 primePLen=1024 时");
            }
            break;
        case 2048:
            if (subprimeQLen != 224 && subprimeQLen != 256) {
               throw new IllegalArgumentException
                   ("subprimeQLen 必须为 224 或 256，当 primePLen=2048 时");
            }
            break;
        case 3072:
            if (subprimeQLen != 256) {
                throw new IllegalArgumentException
                    ("subprimeQLen 必须为 256，当 primePLen=3072 时");
            }
            break;
        default:
            throw new IllegalArgumentException
                ("primePLen 必须为 1024、2048 或 3072");
        }
        if (seedLen < subprimeQLen) {
            throw new IllegalArgumentException
                ("seedLen 必须等于或大于 subprimeQLen");
        }
        this.pLen = primePLen;
        this.qLen = subprimeQLen;
        this.seedLen = seedLen;
    }

    /**
     * 返回待生成的 DSA 域参数的素数 P 的所需位长度。
     * @return 素数 P 的长度。
     */
    public int getPrimePLength() {
        return pLen;
    }

    /**
     * 返回待生成的 DSA 域参数的子素数 Q 的所需位长度。
     * @return 子素数 Q 的长度。
     */
    public int getSubprimeQLength() {
        return qLen;
    }

    /**
     * 返回域参数种子的所需位长度。
     * @return 域参数种子的长度。
     */
    public int getSeedLength() {
        return seedLen;
    }
}
