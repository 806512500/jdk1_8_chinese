/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.im;


/**
 * 定义输入方法使用的附加 Unicode 子集。与 <code>{@link
 * java.lang.Character.UnicodeBlock}</code> 类中定义的 UnicodeBlock 子集不同，这些常量不
 * 直接对应于 Unicode 代码块。
 *
 * @since   1.2
 */

public final class InputSubset extends Character.Subset {

    private InputSubset(String name) {
        super(name);
    }

    /**
     * 包含所有拉丁字符的常量，包括 BASIC_LATIN、LATIN_1_SUPPLEMENT、LATIN_EXTENDED_A、
     * LATIN_EXTENDED_B Unicode 字符块中的字符。
     */
    public static final InputSubset LATIN
        = new InputSubset("LATIN");

    /**
     * 包含 BASIC_LATIN Unicode 字符块中的数字的常量。
     */
    public static final InputSubset LATIN_DIGITS
        = new InputSubset("LATIN_DIGITS");

    /**
     * 包含用于书写繁体中文的所有汉字的常量，包括 CJK 统一汉字的一部分以及可能定义为代理字符的繁体中文汉字。
     */
    public static final InputSubset TRADITIONAL_HANZI
        = new InputSubset("TRADITIONAL_HANZI");

    /**
     * 包含用于书写简体中文的所有汉字的常量，包括 CJK 统一汉字的一部分以及可能定义为代理字符的简体中文汉字。
     */
    public static final InputSubset SIMPLIFIED_HANZI
        = new InputSubset("SIMPLIFIED_HANZI");

    /**
     * 包含用于书写日语的所有汉字的常量，包括 CJK 统一汉字的一部分以及可能定义为代理字符的日语汉字。
     */
    public static final InputSubset KANJI
        = new InputSubset("KANJI");

    /**
     * 包含用于书写韩语的所有汉字的常量，包括 CJK 统一汉字的一部分以及可能定义为代理字符的韩语汉字。
     */
    public static final InputSubset HANJA
        = new InputSubset("HANJA");

    /**
     * 包含 Unicode 半宽和全宽形式字符块中的半宽片假名子集的常量。
     */
    public static final InputSubset HALFWIDTH_KATAKANA
        = new InputSubset("HALFWIDTH_KATAKANA");

    /**
     * 包含 Unicode 半宽和全宽形式字符块中的全宽 ASCII 变体子集的常量。
     * @since 1.3
     */
    public static final InputSubset FULLWIDTH_LATIN
        = new InputSubset("FULLWIDTH_LATIN");

    /**
     * 包含 Unicode 半宽和全宽形式字符块中的全宽数字的常量。
     * @since 1.3
     */
    public static final InputSubset FULLWIDTH_DIGITS
        = new InputSubset("FULLWIDTH_DIGITS");

}
