
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

package java.lang;

import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import sun.text.Normalizer;


/**
 * 这是一个用于 <code>String.toLowerCase()</code> 和
 * <code>String.toUpperCase()</code> 的工具类，处理带有条件的特殊大小写转换。
 * 换句话说，它处理在
 * <a href="http://www.unicode.org/Public/UNIDATA/SpecialCasing.txt">Special
 * Casing Properties</a> 文件中定义的带有条件的映射。
 * <p>
 * 注意，无条件的大小写映射（包括 1:M 映射）在 <code>Character.toLower/UpperCase()</code> 中处理。
 */
final class ConditionalSpecialCasing {

    // 上下文条件。
    final static int FINAL_CASED =              1;
    final static int AFTER_SOFT_DOTTED =        2;
    final static int MORE_ABOVE =               3;
    final static int AFTER_I =                  4;
    final static int NOT_BEFORE_DOT =           5;

    // 组合类定义
    final static int COMBINING_CLASS_ABOVE = 230;

    // 特殊大小写映射条目
    static Entry[] entry = {
        //# ================================================================================
        //# 条件映射
        //# ================================================================================
        new Entry(0x03A3, new char[]{0x03C2}, new char[]{0x03A3}, null, FINAL_CASED), // # 希腊大写字母 SIGMA
        new Entry(0x0130, new char[]{0x0069, 0x0307}, new char[]{0x0130}, null, 0), // # 带点大写字母 I

        //# ================================================================================
        //# 语言敏感映射
        //# ================================================================================
        //# 立陶宛语
        new Entry(0x0307, new char[]{0x0307}, new char[]{}, "lt",  AFTER_SOFT_DOTTED), // # 组合点号
        new Entry(0x0049, new char[]{0x0069, 0x0307}, new char[]{0x0049}, "lt", MORE_ABOVE), // # 大写字母 I
        new Entry(0x004A, new char[]{0x006A, 0x0307}, new char[]{0x004A}, "lt", MORE_ABOVE), // # 大写字母 J
        new Entry(0x012E, new char[]{0x012F, 0x0307}, new char[]{0x012E}, "lt", MORE_ABOVE), // # 带鼻音符大写字母 I
        new Entry(0x00CC, new char[]{0x0069, 0x0307, 0x0300}, new char[]{0x00CC}, "lt", 0), // # 带重音符大写字母 I
        new Entry(0x00CD, new char[]{0x0069, 0x0307, 0x0301}, new char[]{0x00CD}, "lt", 0), // # 带尖音符大写字母 I
        new Entry(0x0128, new char[]{0x0069, 0x0307, 0x0303}, new char[]{0x0128}, "lt", 0), // # 带波浪音符大写字母 I

        //# ================================================================================
        //# 土耳其语和阿塞拜疆语
        new Entry(0x0130, new char[]{0x0069}, new char[]{0x0130}, "tr", 0), // # 带点大写字母 I
        new Entry(0x0130, new char[]{0x0069}, new char[]{0x0130}, "az", 0), // # 带点大写字母 I
        new Entry(0x0307, new char[]{}, new char[]{0x0307}, "tr", AFTER_I), // # 组合点号
        new Entry(0x0307, new char[]{}, new char[]{0x0307}, "az", AFTER_I), // # 组合点号
        new Entry(0x0049, new char[]{0x0131}, new char[]{0x0049}, "tr", NOT_BEFORE_DOT), // # 大写字母 I
        new Entry(0x0049, new char[]{0x0131}, new char[]{0x0049}, "az", NOT_BEFORE_DOT), // # 大写字母 I
        new Entry(0x0069, new char[]{0x0069}, new char[]{0x0130}, "tr", 0), // # 小写字母 i
        new Entry(0x0069, new char[]{0x0069}, new char[]{0x0130}, "az", 0)  // # 小写字母 i
    };

    // 包含上述条目的哈希表
    static Hashtable<Integer, HashSet<Entry>> entryTable = new Hashtable<>();
    static {
        // 从条目创建哈希表
        for (int i = 0; i < entry.length; i ++) {
            Entry cur = entry[i];
            Integer cp = new Integer(cur.getCodePoint());
            HashSet<Entry> set = entryTable.get(cp);
            if (set == null) {
                set = new HashSet<Entry>();
            }
            set.add(cur);
            entryTable.put(cp, set);
        }
    }

    static int toLowerCaseEx(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, true);

        if (result != null) {
            if (result.length == 1) {
                return result[0];
            } else {
                return Character.ERROR;
            }
        } else {
            // 默认使用 Character 类的方法
            return Character.toLowerCase(src.codePointAt(index));
        }
    }

    static int toUpperCaseEx(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, false);

        if (result != null) {
            if (result.length == 1) {
                return result[0];
            } else {
                return Character.ERROR;
            }
        } else {
            // 默认使用 Character 类的方法
            return Character.toUpperCaseEx(src.codePointAt(index));
        }
    }

    static char[] toLowerCaseCharArray(String src, int index, Locale locale) {
        return lookUpTable(src, index, locale, true);
    }

    static char[] toUpperCaseCharArray(String src, int index, Locale locale) {
        char[] result = lookUpTable(src, index, locale, false);
        if (result != null) {
            return result;
        } else {
            return Character.toUpperCaseCharArray(src.codePointAt(index));
        }
    }

    private static char[] lookUpTable(String src, int index, Locale locale, boolean bLowerCasing) {
        HashSet<Entry> set = entryTable.get(new Integer(src.codePointAt(index)));
        char[] ret = null;

        if (set != null) {
            Iterator<Entry> iter = set.iterator();
            String currentLang = locale.getLanguage();
            while (iter.hasNext()) {
                Entry entry = iter.next();
                String conditionLang = entry.getLanguage();
                if (((conditionLang == null) || (conditionLang.equals(currentLang))) &&
                        isConditionMet(src, index, locale, entry.getCondition())) {
                    ret = bLowerCasing ? entry.getLowerCase() : entry.getUpperCase();
                    if (conditionLang != null) {
                        break;
                    }
                }
            }
        }

        return ret;
    }

    private static boolean isConditionMet(String src, int index, Locale locale, int condition) {
        switch (condition) {
        case FINAL_CASED:
            return isFinalCased(src, index, locale);

        case AFTER_SOFT_DOTTED:
            return isAfterSoftDotted(src, index);

        case MORE_ABOVE:
            return isMoreAbove(src, index);

        case AFTER_I:
            return isAfterI(src, index);

        case NOT_BEFORE_DOT:
            return !isBeforeDot(src, index);

        default:
            return true;
        }
    }

    /**
     * 实现 "Final_Cased" 条件
     *
     * 规范：在包含 C 的最近的单词边界内，C 之前有一个带大小写的字母，而 C 之后没有带大小写的字母。
     *
     * 正则表达式：
     *   C 之前: [{cased==true}][{wordBoundary!=true}]*
     *   C 之后: !([{wordBoundary!=true}]*[{cased}])
     */
    private static boolean isFinalCased(String src, int index, Locale locale) {
        BreakIterator wordBoundary = BreakIterator.getWordInstance(locale);
        wordBoundary.setText(src);
        int ch;

        // 查找前面的带大小写的字母
        for (int i = index; (i >= 0) && !wordBoundary.isBoundary(i);
                i -= Character.charCount(ch)) {

            ch = src.codePointBefore(i);
            if (isCased(ch)) {

                int len = src.length();
                // 检查索引之后没有带大小写的字母
                for (i = index + Character.charCount(src.codePointAt(index));
                        (i < len) && !wordBoundary.isBoundary(i);
                        i += Character.charCount(ch)) {

                    ch = src.codePointAt(i);
                    if (isCased(ch)) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * 实现 "After_I" 条件
     *
     * 规范：最后一个前面的基本字符是大写字母 I，且中间没有组合字符类 230 (ABOVE)。
     *
     * 正则表达式：
     *   C 之前: [I]([{cc!=230}&{cc!=0}])*
     */
    private static boolean isAfterI(String src, int index) {
        int ch;
        int cc;

        // 查找最后一个前面的基本字符
        for (int i = index; i > 0; i -= Character.charCount(ch)) {

            ch = src.codePointBefore(i);

            if (ch == 'I') {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * 实现 "After_Soft_Dotted" 条件
     *
     * 规范：最后一个前面的组合类为零的字符是 Soft_Dotted，且中间没有组合字符类 230 (ABOVE)。
     *
     * 正则表达式：
     *   C 之前: [{Soft_Dotted==true}]([{cc!=230}&{cc!=0}])*
     */
    private static boolean isAfterSoftDotted(String src, int index) {
        int ch;
        int cc;

        // 查找最后一个前面的字符
        for (int i = index; i > 0; i -= Character.charCount(ch)) {

            ch = src.codePointBefore(i);

            if (isSoftDotted(ch)) {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * 实现 "More_Above" 条件
     *
     * 规范：C 后面有一个或多个组合类 230 (ABOVE) 的字符。
     *
     * 正则表达式：
     *   C 之后: [{cc!=0}]*[{cc==230}]
     */
    private static boolean isMoreAbove(String src, int index) {
        int ch;
        int cc;
        int len = src.length();

        // 查找后面的组合类为 ABOVE 的字符
        for (int i = index + Character.charCount(src.codePointAt(index));
                i < len; i += Character.charCount(ch)) {

            ch = src.codePointAt(i);
            cc = Normalizer.getCombiningClass(ch);

            if (cc == COMBINING_CLASS_ABOVE) {
                return true;
            } else if (cc == 0) {
                return false;
            }
        }

        return false;
    }

    /**
     * 实现 "Before_Dot" 条件
     *
     * 规范：C 后面是 <code>U+0307 COMBINING DOT ABOVE</code>。
     * C 和组合点号之间可以有任意序列的组合类既不是 0 也不是 230 的字符。
     *
     * 正则表达式：
     *   C 之后: ([{cc!=230}&{cc!=0}])*[\u0307]
     */
    private static boolean isBeforeDot(String src, int index) {
        int ch;
        int cc;
        int len = src.length();

        // 查找后面的组合点号
        for (int i = index + Character.charCount(src.codePointAt(index));
                i < len; i += Character.charCount(ch)) {

            ch = src.codePointAt(i);

            if (ch == '\u0307') {
                return true;
            } else {
                cc = Normalizer.getCombiningClass(ch);
                if ((cc == 0) || (cc == COMBINING_CLASS_ABOVE)) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * 检查一个字符是否为 'cased'。
     *
     * 如果 C 满足以下条件之一，则定义 C 为 'cased'：uppercase==true，或 lowercase==true，或
     * general_category==titlecase_letter。
     *
     * 大小写属性值在 Unicode 字符数据库的 DerivedCoreProperties.txt 数据文件中指定。
     */
    private static boolean isCased(int ch) {
        int type = Character.getType(ch);
        if (type == Character.LOWERCASE_LETTER ||
                type == Character.UPPERCASE_LETTER ||
                type == Character.TITLECASE_LETTER) {
            return true;
        } else {
            // 检查 Other_Lowercase 和 Other_Uppercase
            //
            if ((ch >= 0x02B0) && (ch <= 0x02B8)) {
                // 小写修饰字母 H..小写修饰字母 Y
                return true;
            } else if ((ch >= 0x02C0) && (ch <= 0x02C1)) {
                // 修饰字母喉塞音..修饰字母反向喉塞音
                return true;
            } else if ((ch >= 0x02E0) && (ch <= 0x02E4)) {
                // 小写修饰字母 GAMMA..小写修饰字母反向喉塞音
                return true;
            } else if (ch == 0x0345) {
                // 组合希腊语下标
                return true;
            } else if (ch == 0x037A) {
                // 希腊语下标
                return true;
            } else if ((ch >= 0x1D2C) && (ch <= 0x1D61)) {
                // 大写修饰字母 A..小写修饰字母 CHI
                return true;
            } else if ((ch >= 0x2160) && (ch <= 0x217F)) {
                // 罗马数字 I..罗马数字 M
                // 小写罗马数字 I..小写罗马数字 M
                return true;
            } else if ((ch >= 0x24B6) && (ch <= 0x24E9)) {
                // 圆圈拉丁大写字母 A..圆圈拉丁大写字母 Z
                // 圆圈拉丁小写字母 A..圆圈拉丁小写字母 Z
                return true;
            } else {
                return false;
            }
        }
    }


                private static boolean isSoftDotted(int ch) {
        switch (ch) {
        case 0x0069: // Soft_Dotted # L&       LATIN SMALL LETTER I
        case 0x006A: // Soft_Dotted # L&       LATIN SMALL LETTER J
        case 0x012F: // Soft_Dotted # L&       LATIN SMALL LETTER I WITH OGONEK
        case 0x0268: // Soft_Dotted # L&       LATIN SMALL LETTER I WITH STROKE
        case 0x0456: // Soft_Dotted # L&       CYRILLIC SMALL LETTER BYELORUSSIAN-UKRAINIAN I
        case 0x0458: // Soft_Dotted # L&       CYRILLIC SMALL LETTER JE
        case 0x1D62: // Soft_Dotted # L&       LATIN SUBSCRIPT SMALL LETTER I
        case 0x1E2D: // Soft_Dotted # L&       LATIN SMALL LETTER I WITH TILDE BELOW
        case 0x1ECB: // Soft_Dotted # L&       LATIN SMALL LETTER I WITH DOT BELOW
        case 0x2071: // Soft_Dotted # L&       SUPERSCRIPT LATIN SMALL LETTER I
            return true;
        default:
            return false;
        }
    }

    /**
     * 一个内部类，表示 Special Casing Properties 中的一个条目。
     */
    static class Entry {
        int ch;
        char [] lower;
        char [] upper;
        String lang;
        int condition;

        Entry(int ch, char[] lower, char[] upper, String lang, int condition) {
            this.ch = ch;
            this.lower = lower;
            this.upper = upper;
            this.lang = lang;
            this.condition = condition;
        }

        int getCodePoint() {
            return ch;
        }

        char[] getLowerCase() {
            return lower;
        }

        char[] getUpperCase() {
            return upper;
        }

        String getLanguage() {
            return lang;
        }

        int getCondition() {
            return condition;
        }
    }
}
