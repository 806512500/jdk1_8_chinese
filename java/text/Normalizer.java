/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 *******************************************************************************
 * (C) Copyright IBM Corp. 1996-2005 - All Rights Reserved                     *
 *                                                                             *
 * The original version of this source code and documentation is copyrighted   *
 * and owned by IBM, These materials are provided under terms of a License     *
 * Agreement between IBM and Sun. This technology is protected by multiple     *
 * US and International patents. This notice and attribution to IBM may not    *
 * to removed.                                                                 *
 *******************************************************************************
 */

package java.text;

import sun.text.normalizer.NormalizerBase;
import sun.text.normalizer.NormalizerImpl;

/**
 * 该类提供了 <code>normalize</code> 方法，该方法将 Unicode 文本转换为等效的组合形式或分解形式，以便更容易地对文本进行排序和搜索。
 * <code>normalize</code> 方法支持在
 * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">
 * Unicode 标准附录 #15 &mdash; Unicode 规范化形式</a> 中描述的标准规范化形式。
 * <p>
 * 带有重音符号或其他装饰的字符在 Unicode 中可以以几种不同的方式编码。例如，以带重音的 A 字符为例。在 Unicode 中，这可以编码为单个字符（“组合”形式）：
 *
 * <pre>
 *      U+00C1    LATIN CAPITAL LETTER A WITH ACUTE</pre>
 *
 * 或作为两个单独的字符（“分解”形式）：
 *
 * <pre>
 *      U+0041    LATIN CAPITAL LETTER A
 *      U+0301    COMBINING ACUTE ACCENT</pre>
 *
 * 但是，对于您的程序的用户来说，这两个序列应该被视为相同的“用户级”字符“A 带重音”。在搜索或比较文本时，您必须确保这两个序列被视为等效的。此外，您还必须处理带有多个重音的字符。有时字符的组合重音的顺序是重要的，而在其他情况下，不同顺序的重音序列实际上是等效的。
 * <p>
 * 类似地，字符串“ffi”可以编码为三个单独的字母：
 *
 * <pre>
 *      U+0066    LATIN SMALL LETTER F
 *      U+0066    LATIN SMALL LETTER F
 *      U+0069    LATIN SMALL LETTER I</pre>
 *
 * 或作为单个字符
 *
 * <pre>
 *      U+FB03    LATIN SMALL LIGATURE FFI</pre>
 *
 * “ffi”连字不是一种独立的语义字符，严格来说，它根本不应该包含在 Unicode 中，但为了与已经提供该字符的现有字符集兼容，它被包括在内。Unicode 标准通过为这些字符提供“兼容性”分解来识别这些字符，这些字符可以映射到相应的语义字符。在排序和搜索时，您通常希望使用这些映射。
 * <p>
 * <code>normalize</code> 方法通过将文本转换为上述第一个示例中所示的规范组合和分解形式来帮助解决这些问题。此外，您可以要求它执行兼容性分解，以便您可以将兼容性字符与其等效字符视为相同。最后，<code>normalize</code> 方法会重新排列重音，使其处于正确的规范顺序，因此您不必自己担心重音的重新排列。
 * <p>
 * W3C 通常建议使用 NFC 交换文本。
 * 还需注意，大多数旧字符编码仅使用预组合形式，通常不单独编码任何组合标记。对于转换为此类字符编码，Unicode 文本需要规范化为 NFC。
 * 更多使用示例，请参见 Unicode 标准附录。
 *
 * @since 1.6
 */
public final class Normalizer {

   private Normalizer() {};

    /**
     * 该枚举提供了在
     * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">
     * Unicode 标准附录 #15 &mdash; Unicode 规范化形式</a> 中描述的四种 Unicode 规范化形式的常量
     * 和两种访问它们的方法。
     *
     * @since 1.6
     */
    public static enum Form {

        /**
         * 规范分解。
         */
        NFD,

        /**
         * 规范分解，后跟规范组合。
         */
        NFC,

        /**
         * 兼容性分解。
         */
        NFKD,

        /**
         * 兼容性分解，后跟规范组合。
         */
        NFKC
    }

    /**
     * 规范化一系列 char 值。
     * 该序列将根据指定的规范化形式进行规范化。
     * @param src        要规范化的 char 值序列。
     * @param form       规范化形式；可以是
     *                   {@link java.text.Normalizer.Form#NFC}，
     *                   {@link java.text.Normalizer.Form#NFD}，
     *                   {@link java.text.Normalizer.Form#NFKC}，
     *                   {@link java.text.Normalizer.Form#NFKD}
     * @return 规范化后的 String
     * @throws NullPointerException 如果 <code>src</code> 或 <code>form</code>
     * 为 null。
     */
    public static String normalize(CharSequence src, Form form) {
        return NormalizerBase.normalize(src.toString(), form);
    }

    /**
     * 确定给定的 char 值序列是否已规范化。
     * @param src        要检查的 char 值序列。
     * @param form       规范化形式；可以是
     *                   {@link java.text.Normalizer.Form#NFC}，
     *                   {@link java.text.Normalizer.Form#NFD}，
     *                   {@link java.text.Normalizer.Form#NFKC}，
     *                   {@link java.text.Normalizer.Form#NFKD}
     * @return 如果 char 值序列已规范化，则返回 true；
     * 否则返回 false。
     * @throws NullPointerException 如果 <code>src</code> 或 <code>form</code>
     * 为 null。
     */
    public static boolean isNormalized(CharSequence src, Form form) {
        return NormalizerBase.isNormalized(src.toString(), form);
    }
}
