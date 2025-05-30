
/*
 * Copyright (c) 2002, 2023, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

/**
 * {@code Character} 类将原始类型 {@code char} 的值包装在一个对象中。一个 {@code Character} 类的对象包含一个类型为 {@code char} 的单个字段。
 * <p>
 * 此外，该类提供了大量静态方法，用于确定字符的类别（小写字母、数字等）以及将字符从大写转换为小写，反之亦然。
 *
 * <h3><a id="conformance">Unicode 兼容性</a></h3>
 * <p>
 * {@code Character} 类的字段和方法是根据 Unicode 标准中定义的字符信息定义的，特别是 Unicode 字符数据库中的 <i>UnicodeData</i> 文件。
 * 该文件为每个分配的 Unicode 代码点或字符范围指定名称和类别。该文件可从 Unicode 联盟的网站获取：
 * <a href="http://www.unicode.org">http://www.unicode.org</a>。
 * <p>
 * Java SE 8 平台使用 Unicode 标准 6.2 版中的字符信息，并有三个扩展。首先，鉴于新货币频繁出现，Java SE 8 平台允许 {@code Character} 类的实现使用 Unicode 标准 10.0 版中的货币符号块。
 * 其次，Java SE 8 平台允许 {@code Character} 类的实现使用 Unicode 标准 11.0 版中的 {@code U+9FCD} 到 {@code U+9FEF} 代码点范围，以及 Unicode 标准 8.0 版中的 {@code CJK Unified Ideographs Extension E} 块，以便该类支持中文 GB18030-2022 标准的“实现级别 2”。
 * 第三，Java SE 8 平台允许 {@code Character} 类的实现使用 Unicode 标准 12.1 版中的日本纪年代码点 {@code U+32FF}。
 * 因此，当处理上述代码点（超出 6.2 版）时，{@code Character} 类的字段和方法的行为可能因 Java SE 8 平台的不同实现而异，但以下定义 Java 标识符的方法除外：
 * {@link #isJavaIdentifierStart(int)}，{@link #isJavaIdentifierStart(char)}，{@link #isJavaIdentifierPart(int)} 和 {@link #isJavaIdentifierPart(char)}。
 * Java 标识符中的代码点必须来自 Unicode 标准 6.2 版。
 *
 * <h3><a name="unicode">Unicode 字符表示</a></h3>
 *
 * <p>{@code char} 数据类型（因此 {@code Character} 对象封装的值）基于原始 Unicode 规范，该规范将字符定义为固定宽度的 16 位实体。Unicode 标准后来进行了更改，允许表示需要超过 16 位的字符。
 * 合法的 <em>代码点</em> 范围现在是 U+0000 到 U+10FFFF，称为 <em>Unicode 标量值</em>。
 * （有关 U+<i>n</i> 表示法的定义，请参阅 Unicode 标准中的 <a
 * href="http://www.unicode.org/reports/tr27/#notation"><i>
 * 定义</i></a>。）
 *
 * <p><a name="BMP">U+0000 到 U+FFFF 范围内的字符集</a> 有时被称为 <em>基本多文种平面 (BMP)</em>。
 * <a name="supplementary">代码点大于 U+FFFF 的字符</a> 称为 <em>补充字符</em>。Java 平台在 {@code char} 数组和 {@code String} 及 {@code StringBuffer} 类中使用 UTF-16 表示法。
 * 在这种表示法中，补充字符表示为一对 {@code char} 值，第一个值来自 <em>高代理</em> 范围（&#92;uD800-&#92;uDBFF），第二个值来自 <em>低代理</em> 范围（&#92;uDC00-&#92;uDFFF）。
 *
 * <p>{@code char} 值因此表示基本多文种平面 (BMP) 代码点，包括代理代码点，或 UTF-16 编码的代码单元。{@code int} 值表示所有 Unicode 代码点，包括补充代码点。
 * {@code int} 的较低（最低有效）21 位用于表示 Unicode 代码点，较高（最高有效）11 位必须为零。除非另有说明，否则补充字符和代理 {@code char} 值的行为如下：
 *
 * <ul>
 * <li>仅接受 {@code char} 值的方法不支持补充字符。它们将代理范围内的 {@code char} 值视为未定义的字符。例如，{@code Character.isLetter('\u005CuD840')} 返回 {@code false}，即使这个特定值在字符串中跟随任何低代理值时代表一个字母。
 *
 * <li>接受 {@code int} 值的方法支持所有 Unicode 字符，包括补充字符。例如，{@code Character.isLetter(0x2F81A)} 返回 {@code true}，因为代码点值代表一个字母（一个 CJK 汉字）。
 * </ul>
 *
 * <p>在 Java SE API 文档中，<em>Unicode 代码点</em> 用于表示 U+0000 到 U+10FFFF 范围内的字符值，而 <em>Unicode 代码单元</em> 用于表示 16 位的 {@code char} 值，这些值是 <em>UTF-16</em> 编码的代码单元。有关 Unicode 术语的更多信息，请参阅 <a href="http://www.unicode.org/glossary/">Unicode 术语表</a>。
 *
 * @author  Lee Boynton
 * @author  Guy Steele
 * @author  Akira Tanaka
 * @author  Martin Buchholz
 * @author  Ulf Zibis
 * @since   1.0
 */
public final
class Character implements java.io.Serializable, Comparable<Character> {
    /**
     * 转换为字符串时可用的最小基数。此字段的常量值是允许用于基数转换方法（如 {@code digit} 方法、{@code forDigit} 方法和 {@code Integer} 类的 {@code toString} 方法）的基数参数的最小值。
     *
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Integer#toString(int, int)
     * @see     Integer#valueOf(String)
     */
    public static final int MIN_RADIX = 2;

    /**
     * 转换为字符串时可用的最大基数。此字段的常量值是允许用于基数转换方法（如 {@code digit} 方法、{@code forDigit} 方法和 {@code Integer} 类的 {@code toString} 方法）的基数参数的最大值。
     *
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Integer#toString(int, int)
     * @see     Integer#valueOf(String)
     */
    public static final int MAX_RADIX = 36;

    /**
     * 此字段的常量值是 {@code char} 类型的最小值，即 {@code '\u005Cu0000'}。
     *
     * @since   1.0.2
     */
    public static final char MIN_VALUE = '\u0000';

    /**
     * 此字段的常量值是 {@code char} 类型的最大值，即 {@code '\u005CuFFFF'}。
     *
     * @since   1.0.2
     */
    public static final char MAX_VALUE = '\uFFFF';

    /**
     * 表示原始类型 {@code char} 的 {@code Class} 实例。
     *
     * @since   1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Character> TYPE = (Class<Character>) Class.getPrimitiveClass("char");

    /*
     * 规范性一般类型
     */

    /*
     * 一般字符类型
     */

    /**
     * Unicode 规范中的通用类别 "Cn"。
     * @since   1.1
     */
    public static final byte UNASSIGNED = 0;

    /**
     * Unicode 规范中的通用类别 "Lu"。
     * @since   1.1
     */
    public static final byte UPPERCASE_LETTER = 1;

    /**
     * Unicode 规范中的通用类别 "Ll"。
     * @since   1.1
     */
    public static final byte LOWERCASE_LETTER = 2;

    /**
     * Unicode 规范中的通用类别 "Lt"。
     * @since   1.1
     */
    public static final byte TITLECASE_LETTER = 3;

    /**
     * Unicode 规范中的通用类别 "Lm"。
     * @since   1.1
     */
    public static final byte MODIFIER_LETTER = 4;

    /**
     * Unicode 规范中的通用类别 "Lo"。
     * @since   1.1
     */
    public static final byte OTHER_LETTER = 5;

    /**
     * Unicode 规范中的通用类别 "Mn"。
     * @since   1.1
     */
    public static final byte NON_SPACING_MARK = 6;

    /**
     * Unicode 规范中的通用类别 "Me"。
     * @since   1.1
     */
    public static final byte ENCLOSING_MARK = 7;

    /**
     * Unicode 规范中的通用类别 "Mc"。
     * @since   1.1
     */
    public static final byte COMBINING_SPACING_MARK = 8;

    /**
     * Unicode 规范中的通用类别 "Nd"。
     * @since   1.1
     */
    public static final byte DECIMAL_DIGIT_NUMBER        = 9;

    /**
     * Unicode 规范中的通用类别 "Nl"。
     * @since   1.1
     */
    public static final byte LETTER_NUMBER = 10;

    /**
     * Unicode 规范中的通用类别 "No"。
     * @since   1.1
     */
    public static final byte OTHER_NUMBER = 11;

    /**
     * Unicode 规范中的通用类别 "Zs"。
     * @since   1.1
     */
    public static final byte SPACE_SEPARATOR = 12;

    /**
     * Unicode 规范中的通用类别 "Zl"。
     * @since   1.1
     */
    public static final byte LINE_SEPARATOR = 13;

    /**
     * Unicode 规范中的通用类别 "Zp"。
     * @since   1.1
     */
    public static final byte PARAGRAPH_SEPARATOR = 14;

    /**
     * Unicode 规范中的通用类别 "Cc"。
     * @since   1.1
     */
    public static final byte CONTROL = 15;

    /**
     * Unicode 规范中的通用类别 "Cf"。
     * @since   1.1
     */
    public static final byte FORMAT = 16;

    /**
     * Unicode 规范中的通用类别 "Co"。
     * @since   1.1
     */
    public static final byte PRIVATE_USE = 18;

    /**
     * Unicode 规范中的通用类别 "Cs"。
     * @since   1.1
     */
    public static final byte SURROGATE = 19;

    /**
     * Unicode 规范中的通用类别 "Pd"。
     * @since   1.1
     */
    public static final byte DASH_PUNCTUATION = 20;

    /**
     * Unicode 规范中的通用类别 "Ps"。
     * @since   1.1
     */
    public static final byte START_PUNCTUATION = 21;

    /**
     * Unicode 规范中的通用类别 "Pe"。
     * @since   1.1
     */
    public static final byte END_PUNCTUATION = 22;

    /**
     * Unicode 规范中的通用类别 "Pc"。
     * @since   1.1
     */
    public static final byte CONNECTOR_PUNCTUATION = 23;

    /**
     * Unicode 规范中的通用类别 "Po"。
     * @since   1.1
     */
    public static final byte OTHER_PUNCTUATION = 24;

    /**
     * Unicode 规范中的通用类别 "Sm"。
     * @since   1.1
     */
    public static final byte MATH_SYMBOL = 25;

    /**
     * Unicode 规范中的通用类别 "Sc"。
     * @since   1.1
     */
    public static final byte CURRENCY_SYMBOL = 26;

    /**
     * Unicode 规范中的通用类别 "Sk"。
     * @since   1.1
     */
    public static final byte MODIFIER_SYMBOL = 27;

    /**
     * Unicode 规范中的通用类别 "So"。
     * @since   1.1
     */
    public static final byte OTHER_SYMBOL = 28;

    /**
     * Unicode 规范中的通用类别 "Pi"。
     * @since   1.4
     */
    public static final byte INITIAL_QUOTE_PUNCTUATION = 29;

    /**
     * Unicode 规范中的通用类别 "Pf"。
     * @since   1.4
     */
    public static final byte FINAL_QUOTE_PUNCTUATION = 30;

    /**
     * 错误标志。使用 int（代码点）以避免与 U+FFFF 混淆。
     */
    static final int ERROR = 0xFFFFFFFF;

    /**
     * 未定义的双向字符类型。Unicode 规范中未定义的 {@code char} 值具有未定义的方向性。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_UNDEFINED = -1;

    /**
     * Unicode 规范中的强双向字符类型 "L"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    /**
     * Unicode 规范中的强双向字符类型 "R"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;

    /**
     * Unicode 规范中的强双向字符类型 "AL"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

    /**
     * Unicode 规范中的弱双向字符类型 "EN"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;

    /**
     * Unicode 规范中的弱双向字符类型 "ES"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

    /**
     * Unicode 规范中的弱双向字符类型 "ET"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

    /**
     * Unicode 规范中的弱双向字符类型 "AN"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;

    /**
     * Unicode 规范中的弱双向字符类型 "CS"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

    /**
     * Unicode 规范中的弱双向字符类型 "NSM"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;

    /**
     * Unicode 规范中的弱双向字符类型 "BN"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;


                /**
     * Unicode 规范中的中性双向字符类型 "B"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

    /**
     * Unicode 规范中的中性双向字符类型 "S"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

    /**
     * Unicode 规范中的中性双向字符类型 "WS"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_WHITESPACE = 12;

    /**
     * Unicode 规范中的中性双向字符类型 "ON"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;

    /**
     * Unicode 规范中的强双向字符类型 "LRE"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

    /**
     * Unicode 规范中的强双向字符类型 "LRO"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

    /**
     * Unicode 规范中的强双向字符类型 "RLE"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

    /**
     * Unicode 规范中的强双向字符类型 "RLO"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

    /**
     * Unicode 规范中的弱双向字符类型 "PDF"。
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;

    /**
     * UTF-16 编码中
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode 高代理码单元</a>
     * 的最小值，常量 {@code '\u005CuD800'}。
     * 高代理也称为 <i>前导代理</i>。
     *
     * @since 1.5
     */
    public static final char MIN_HIGH_SURROGATE = '\uD800';

    /**
     * UTF-16 编码中
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode 高代理码单元</a>
     * 的最大值，常量 {@code '\u005CuDBFF'}。
     * 高代理也称为 <i>前导代理</i>。
     *
     * @since 1.5
     */
    public static final char MAX_HIGH_SURROGATE = '\uDBFF';

    /**
     * UTF-16 编码中
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode 低代理码单元</a>
     * 的最小值，常量 {@code '\u005CuDC00'}。
     * 低代理也称为 <i>尾随代理</i>。
     *
     * @since 1.5
     */
    public static final char MIN_LOW_SURROGATE  = '\uDC00';

    /**
     * UTF-16 编码中
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode 低代理码单元</a>
     * 的最大值，常量 {@code '\u005CuDFFF'}。
     * 低代理也称为 <i>尾随代理</i>。
     *
     * @since 1.5
     */
    public static final char MAX_LOW_SURROGATE  = '\uDFFF';

    /**
     * UTF-16 编码中 Unicode 代理码单元的最小值，常量 {@code '\u005CuD800'}。
     *
     * @since 1.5
     */
    public static final char MIN_SURROGATE = MIN_HIGH_SURROGATE;

    /**
     * UTF-16 编码中 Unicode 代理码单元的最大值，常量 {@code '\u005CuDFFF'}。
     *
     * @since 1.5
     */
    public static final char MAX_SURROGATE = MAX_LOW_SURROGATE;

    /**
     * Unicode 补充码点的最小值，常量 {@code U+10000}。
     *
     * @since 1.5
     */
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

    /**
     * Unicode 码点的最小值，常量 {@code U+0000}。
     *
     * @since 1.5
     */
    public static final int MIN_CODE_POINT = 0x000000;

    /**
     * Unicode 码点的最大值，常量 {@code U+10FFFF}。
     *
     * @since 1.5
     */
    public static final int MAX_CODE_POINT = 0X10FFFF;

    /**
     * 该类的实例表示 Unicode 字符集的特定子集。在 {@code Character} 类中定义的唯一子集系列是 {@link Character.UnicodeBlock}。
     * Java API 的其他部分可能会为自己的目的定义其他子集。
     *
     * @since 1.2
     */
    public static class Subset  {

        private String name;

        /**
         * 构造一个新的 {@code Subset} 实例。
         *
         * @param  name  该子集的名称
         * @exception NullPointerException 如果名称为 {@code null}
         */
        protected Subset(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.name = name;
        }

        /**
         * 比较两个 {@code Subset} 对象是否相等。
         * 仅当 {@code this} 和参数引用同一个对象时，此方法才返回 {@code true}；由于此方法是 {@code final} 的，因此此保证适用于所有子类。
         */
        public final boolean equals(Object obj) {
            return (this == obj);
        }

        /**
         * 返回由 {@link Object#hashCode} 方法定义的标准哈希码。为了确保 {@code equals} 和 {@code hashCode} 方法在所有子类中保持一致，此方法是 {@code final} 的。
         */
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * 返回该子集的名称。
         */
        public final String toString() {
            return name;
        }
    }

    // 有关 Unicode 块的最新规范，请参阅 http://www.unicode.org/Public/UNIDATA/Blocks.txt

    /**
     * 表示 Unicode 规范中字符块的字符子集系列。字符块通常定义用于特定脚本或目的的字符。一个字符最多包含在一个 Unicode 块中。
     *
     * @since 1.2
     */
    public static final class UnicodeBlock extends Subset {

        private static Map<String, UnicodeBlock> map = new HashMap<>(256);

        /**
         * 使用给定的标识名称创建一个 UnicodeBlock。
         * 该名称必须与块标识符相同。
         */
        private UnicodeBlock(String idName) {
            super(idName);
            map.put(idName, this);
        }

        /**
         * 使用给定的标识名称和别名创建一个 UnicodeBlock。
         */
        private UnicodeBlock(String idName, String alias) {
            this(idName);
            map.put(alias, this);
        }

        /**
         * 使用给定的标识名称和别名创建一个 UnicodeBlock。
         */
        private UnicodeBlock(String idName, String... aliases) {
            this(idName);
            for (String alias : aliases)
                map.put(alias, this);
        }

        /**
         * "Basic Latin" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock  BASIC_LATIN =
            new UnicodeBlock("BASIC_LATIN",
                             "BASIC LATIN",
                             "BASICLATIN");

        /**
         * "Latin-1 Supplement" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LATIN_1_SUPPLEMENT =
            new UnicodeBlock("LATIN_1_SUPPLEMENT",
                             "LATIN-1 SUPPLEMENT",
                             "LATIN-1SUPPLEMENT");

        /**
         * "Latin Extended-A" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LATIN_EXTENDED_A =
            new UnicodeBlock("LATIN_EXTENDED_A",
                             "LATIN EXTENDED-A",
                             "LATINEXTENDED-A");

        /**
         * "Latin Extended-B" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LATIN_EXTENDED_B =
            new UnicodeBlock("LATIN_EXTENDED_B",
                             "LATIN EXTENDED-B",
                             "LATINEXTENDED-B");

        /**
         * "IPA Extensions" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock IPA_EXTENSIONS =
            new UnicodeBlock("IPA_EXTENSIONS",
                             "IPA EXTENSIONS",
                             "IPAEXTENSIONS");

        /**
         * "Spacing Modifier Letters" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS =
            new UnicodeBlock("SPACING_MODIFIER_LETTERS",
                             "SPACING MODIFIER LETTERS",
                             "SPACINGMODIFIERLETTERS");

        /**
         * "Combining Diacritical Marks" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS =
            new UnicodeBlock("COMBINING_DIACRITICAL_MARKS",
                             "COMBINING DIACRITICAL MARKS",
                             "COMBININGDIACRITICALMARKS");

        /**
         * "Greek and Coptic" Unicode 字符块的常量。
         * <p>
         * 该块以前称为 "Greek" 块。
         *
         * @since 1.2
         */
        public static final UnicodeBlock GREEK =
            new UnicodeBlock("GREEK",
                             "GREEK AND COPTIC",
                             "GREEKANDCOPTIC");

        /**
         * "Cyrillic" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CYRILLIC =
            new UnicodeBlock("CYRILLIC");

        /**
         * "Armenian" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ARMENIAN =
            new UnicodeBlock("ARMENIAN");

        /**
         * "Hebrew" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HEBREW =
            new UnicodeBlock("HEBREW");

        /**
         * "Arabic" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ARABIC =
            new UnicodeBlock("ARABIC");

        /**
         * "Devanagari" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock DEVANAGARI =
            new UnicodeBlock("DEVANAGARI");

        /**
         * "Bengali" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock BENGALI =
            new UnicodeBlock("BENGALI");

        /**
         * "Gurmukhi" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GURMUKHI =
            new UnicodeBlock("GURMUKHI");

        /**
         * "Gujarati" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GUJARATI =
            new UnicodeBlock("GUJARATI");

        /**
         * "Oriya" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ORIYA =
            new UnicodeBlock("ORIYA");

        /**
         * "Tamil" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock TAMIL =
            new UnicodeBlock("TAMIL");

        /**
         * "Telugu" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock TELUGU =
            new UnicodeBlock("TELUGU");

        /**
         * "Kannada" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock KANNADA =
            new UnicodeBlock("KANNADA");

        /**
         * "Malayalam" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock MALAYALAM =
            new UnicodeBlock("MALAYALAM");

        /**
         * "Thai" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock THAI =
            new UnicodeBlock("THAI");

        /**
         * "Lao" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LAO =
            new UnicodeBlock("LAO");

        /**
         * "Tibetan" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock TIBETAN =
            new UnicodeBlock("TIBETAN");

        /**
         * "Georgian" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GEORGIAN =
            new UnicodeBlock("GEORGIAN");

        /**
         * "Hangul Jamo" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HANGUL_JAMO =
            new UnicodeBlock("HANGUL_JAMO",
                             "HANGUL JAMO",
                             "HANGULJAMO");

        /**
         * "Latin Extended Additional" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL =
            new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL",
                             "LATIN EXTENDED ADDITIONAL",
                             "LATINEXTENDEDADDITIONAL");

        /**
         * "Greek Extended" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GREEK_EXTENDED =
            new UnicodeBlock("GREEK_EXTENDED",
                             "GREEK EXTENDED",
                             "GREEKEXTENDED");

        /**
         * "General Punctuation" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GENERAL_PUNCTUATION =
            new UnicodeBlock("GENERAL_PUNCTUATION",
                             "GENERAL PUNCTUATION",
                             "GENERALPUNCTUATION");

        /**
         * "Superscripts and Subscripts" Unicode 字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS =
            new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS",
                             "SUPERSCRIPTS AND SUBSCRIPTS",
                             "SUPERSCRIPTSANDSUBSCRIPTS");


                    /**
         * “货币符号”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CURRENCY_SYMBOLS =
            new UnicodeBlock("CURRENCY_SYMBOLS",
                             "CURRENCY SYMBOLS",
                             "CURRENCYSYMBOLS");

        /**
         * “符号组合变音符号”Unicode字符块的常量。
         * <p>
         * 该块以前称为“符号组合符号”。
         * @since 1.2
         */
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS =
            new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS",
                             "COMBINING DIACRITICAL MARKS FOR SYMBOLS",
                             "COMBININGDIACRITICALMARKSFORSYMBOLS",
                             "COMBINING MARKS FOR SYMBOLS",
                             "COMBININGMARKSFORSYMBOLS");

        /**
         * “字母符号”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock LETTERLIKE_SYMBOLS =
            new UnicodeBlock("LETTERLIKE_SYMBOLS",
                             "LETTERLIKE SYMBOLS",
                             "LETTERLIKESYMBOLS");

        /**
         * “数字形式”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock NUMBER_FORMS =
            new UnicodeBlock("NUMBER_FORMS",
                             "NUMBER FORMS",
                             "NUMBERFORMS");

        /**
         * “箭头”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ARROWS =
            new UnicodeBlock("ARROWS");

        /**
         * “数学运算符”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock MATHEMATICAL_OPERATORS =
            new UnicodeBlock("MATHEMATICAL_OPERATORS",
                             "MATHEMATICAL OPERATORS",
                             "MATHEMATICALOPERATORS");

        /**
         * “杂项技术”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL =
            new UnicodeBlock("MISCELLANEOUS_TECHNICAL",
                             "MISCELLANEOUS TECHNICAL",
                             "MISCELLANEOUSTECHNICAL");

        /**
         * “控制图”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CONTROL_PICTURES =
            new UnicodeBlock("CONTROL_PICTURES",
                             "CONTROL PICTURES",
                             "CONTROLPICTURES");

        /**
         * “光学字符识别”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION =
            new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION",
                             "OPTICAL CHARACTER RECOGNITION",
                             "OPTICALCHARACTERRECOGNITION");

        /**
         * “封闭的字母数字”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS =
            new UnicodeBlock("ENCLOSED_ALPHANUMERICS",
                             "ENCLOSED ALPHANUMERICS",
                             "ENCLOSEDALPHANUMERICS");

        /**
         * “框线”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock BOX_DRAWING =
            new UnicodeBlock("BOX_DRAWING",
                             "BOX DRAWING",
                             "BOXDRAWING");

        /**
         * “块元素”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock BLOCK_ELEMENTS =
            new UnicodeBlock("BLOCK_ELEMENTS",
                             "BLOCK ELEMENTS",
                             "BLOCKELEMENTS");

        /**
         * “几何形状”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock GEOMETRIC_SHAPES =
            new UnicodeBlock("GEOMETRIC_SHAPES",
                             "GEOMETRIC SHAPES",
                             "GEOMETRICSHAPES");

        /**
         * “杂项符号”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS",
                             "MISCELLANEOUS SYMBOLS",
                             "MISCELLANEOUSSYMBOLS");

        /**
         * “花饰”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock DINGBATS =
            new UnicodeBlock("DINGBATS");

        /**
         * “CJK符号和标点”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION =
            new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION",
                             "CJK SYMBOLS AND PUNCTUATION",
                             "CJKSYMBOLSANDPUNCTUATION");

        /**
         * “平假名”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HIRAGANA =
            new UnicodeBlock("HIRAGANA");

        /**
         * “片假名”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock KATAKANA =
            new UnicodeBlock("KATAKANA");

        /**
         * “注音符号”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock BOPOMOFO =
            new UnicodeBlock("BOPOMOFO");

        /**
         * “韩语兼容字母”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO =
            new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO",
                             "HANGUL COMPATIBILITY JAMO",
                             "HANGULCOMPATIBILITYJAMO");

        /**
         * “训读”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock KANBUN =
            new UnicodeBlock("KANBUN");

        /**
         * “封闭的CJK字母和月份”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS =
            new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS",
                             "ENCLOSED CJK LETTERS AND MONTHS",
                             "ENCLOSEDCJKLETTERSANDMONTHS");

        /**
         * “CJK兼容”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CJK_COMPATIBILITY =
            new UnicodeBlock("CJK_COMPATIBILITY",
                             "CJK COMPATIBILITY",
                             "CJKCOMPATIBILITY");

        /**
         * “CJK统一汉字”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS",
                             "CJK UNIFIED IDEOGRAPHS",
                             "CJKUNIFIEDIDEOGRAPHS");

        /**
         * “韩语音节”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HANGUL_SYLLABLES =
            new UnicodeBlock("HANGUL_SYLLABLES",
                             "HANGUL SYLLABLES",
                             "HANGULSYLLABLES");

        /**
         * “私有使用区”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock PRIVATE_USE_AREA =
            new UnicodeBlock("PRIVATE_USE_AREA",
                             "PRIVATE USE AREA",
                             "PRIVATEUSEAREA");

        /**
         * “CJK兼容汉字”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS =
            new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS",
                             "CJK COMPATIBILITY IDEOGRAPHS",
                             "CJKCOMPATIBILITYIDEOGRAPHS");

        /**
         * “字母呈现形式”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS =
            new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS",
                             "ALPHABETIC PRESENTATION FORMS",
                             "ALPHABETICPRESENTATIONFORMS");

        /**
         * “阿拉伯文呈现形式A”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A =
            new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A",
                             "ARABIC PRESENTATION FORMS-A",
                             "ARABICPRESENTATIONFORMS-A");

        /**
         * “组合半标记”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock COMBINING_HALF_MARKS =
            new UnicodeBlock("COMBINING_HALF_MARKS",
                             "COMBINING HALF MARKS",
                             "COMBININGHALFMARKS");

        /**
         * “CJK兼容形式”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS =
            new UnicodeBlock("CJK_COMPATIBILITY_FORMS",
                             "CJK COMPATIBILITY FORMS",
                             "CJKCOMPATIBILITYFORMS");

        /**
         * “小型形式变体”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock SMALL_FORM_VARIANTS =
            new UnicodeBlock("SMALL_FORM_VARIANTS",
                             "SMALL FORM VARIANTS",
                             "SMALLFORMVARIANTS");

        /**
         * “阿拉伯文呈现形式B”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B =
            new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B",
                             "ARABIC PRESENTATION FORMS-B",
                             "ARABICPRESENTATIONFORMS-B");

        /**
         * “半宽和全宽形式”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS =
            new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS",
                             "HALFWIDTH AND FULLWIDTH FORMS",
                             "HALFWIDTHANDFULLWIDTHFORMS");

        /**
         * “特殊字符”Unicode字符块的常量。
         * @since 1.2
         */
        public static final UnicodeBlock SPECIALS =
            new UnicodeBlock("SPECIALS");

        /**
         * @deprecated 从J2SE 5开始，使用 {@link #HIGH_SURROGATES}，
         *             {@link #HIGH_PRIVATE_USE_SURROGATES}，和
         *             {@link #LOW_SURROGATES}。这些新的常量与
         *             Unicode标准的块定义匹配。
         *             {@link #of(char)} 和 {@link #of(int)} 方法
         *             返回新的常量，而不是 SURROGATES_AREA。
         */
        @Deprecated
        public static final UnicodeBlock SURROGATES_AREA =
            new UnicodeBlock("SURROGATES_AREA");

        /**
         * “叙利亚文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock SYRIAC =
            new UnicodeBlock("SYRIAC");

        /**
         * “塔纳文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock THAANA =
            new UnicodeBlock("THAANA");

        /**
         * “僧伽罗文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock SINHALA =
            new UnicodeBlock("SINHALA");

        /**
         * “缅甸文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock MYANMAR =
            new UnicodeBlock("MYANMAR");

        /**
         * “埃塞俄比亚文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock ETHIOPIC =
            new UnicodeBlock("ETHIOPIC");

        /**
         * “切罗基文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock CHEROKEE =
            new UnicodeBlock("CHEROKEE");

        /**
         * “统一加拿大土著音节”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS =
            new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS",
                             "UNIFIED CANADIAN ABORIGINAL SYLLABICS",
                             "UNIFIEDCANADIANABORIGINALSYLLABICS");

        /**
         * “奥加姆文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock OGHAM =
            new UnicodeBlock("OGHAM");

        /**
         * “卢尼文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock RUNIC =
            new UnicodeBlock("RUNIC");

        /**
         * “高棉文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock KHMER =
            new UnicodeBlock("KHMER");

        /**
         * “蒙古文”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock MONGOLIAN =
            new UnicodeBlock("MONGOLIAN");

        /**
         * “盲文图案”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock BRAILLE_PATTERNS =
            new UnicodeBlock("BRAILLE_PATTERNS",
                             "BRAILLE PATTERNS",
                             "BRAILLEPATTERNS");

        /**
         * “CJK部首补充”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT =
            new UnicodeBlock("CJK_RADICALS_SUPPLEMENT",
                             "CJK RADICALS SUPPLEMENT",
                             "CJKRADICALSSUPPLEMENT");

        /**
         * “康熙部首”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock KANGXI_RADICALS =
            new UnicodeBlock("KANGXI_RADICALS",
                             "KANGXI RADICALS",
                             "KANGXIRADICALS");


                    /**
         * “意符描述字符”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS =
            new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS",
                             "IDEOGRAPHIC DESCRIPTION CHARACTERS",
                             "IDEOGRAPHICDESCRIPTIONCHARACTERS");

        /**
         * “注音符号扩展”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock BOPOMOFO_EXTENDED =
            new UnicodeBlock("BOPOMOFO_EXTENDED",
                             "BOPOMOFO EXTENDED",
                             "BOPOMOFOEXTENDED");

        /**
         * “CJK统一汉字扩展A”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A",
                             "CJK UNIFIED IDEOGRAPHS EXTENSION A",
                             "CJKUNIFIEDIDEOGRAPHSEXTENSIONA");

        /**
         * “彝文音节”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock YI_SYLLABLES =
            new UnicodeBlock("YI_SYLLABLES",
                             "YI SYLLABLES",
                             "YISYLLABLES");

        /**
         * “彝文部首”Unicode字符块的常量。
         * @since 1.4
         */
        public static final UnicodeBlock YI_RADICALS =
            new UnicodeBlock("YI_RADICALS",
                             "YI RADICALS",
                             "YIRADICALS");

        /**
         * “西里尔文补充”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY =
            new UnicodeBlock("CYRILLIC_SUPPLEMENTARY",
                             "CYRILLIC SUPPLEMENTARY",
                             "CYRILLICSUPPLEMENTARY",
                             "CYRILLIC SUPPLEMENT",
                             "CYRILLICSUPPLEMENT");

        /**
         * “他加禄文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock TAGALOG =
            new UnicodeBlock("TAGALOG");

        /**
         * “哈努诺文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock HANUNOO =
            new UnicodeBlock("HANUNOO");

        /**
         * “布希德文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock BUHID =
            new UnicodeBlock("BUHID");

        /**
         * “塔格班瓦文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock TAGBANWA =
            new UnicodeBlock("TAGBANWA");

        /**
         * “利姆布文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock LIMBU =
            new UnicodeBlock("LIMBU");

        /**
         * “泰卢文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock TAI_LE =
            new UnicodeBlock("TAI_LE",
                             "TAI LE",
                             "TAILE");

        /**
         * “高棉符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock KHMER_SYMBOLS =
            new UnicodeBlock("KHMER_SYMBOLS",
                             "KHMER SYMBOLS",
                             "KHMERSYMBOLS");

        /**
         * “音标扩展”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock PHONETIC_EXTENSIONS =
            new UnicodeBlock("PHONETIC_EXTENSIONS",
                             "PHONETIC EXTENSIONS",
                             "PHONETICEXTENSIONS");

        /**
         * “杂项数学符号-A”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A =
            new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A",
                             "MISCELLANEOUS MATHEMATICAL SYMBOLS-A",
                             "MISCELLANEOUSMATHEMATICALSYMBOLS-A");

        /**
         * “补充箭头-A”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A =
            new UnicodeBlock("SUPPLEMENTAL_ARROWS_A",
                             "SUPPLEMENTAL ARROWS-A",
                             "SUPPLEMENTALARROWS-A");

        /**
         * “补充箭头-B”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B =
            new UnicodeBlock("SUPPLEMENTAL_ARROWS_B",
                             "SUPPLEMENTAL ARROWS-B",
                             "SUPPLEMENTALARROWS-B");

        /**
         * “杂项数学符号-B”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B =
            new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B",
                             "MISCELLANEOUS MATHEMATICAL SYMBOLS-B",
                             "MISCELLANEOUSMATHEMATICALSYMBOLS-B");

        /**
         * “补充数学运算符”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS =
            new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS",
                             "SUPPLEMENTAL MATHEMATICAL OPERATORS",
                             "SUPPLEMENTALMATHEMATICALOPERATORS");

        /**
         * “杂项符号和箭头”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS",
                             "MISCELLANEOUS SYMBOLS AND ARROWS",
                             "MISCELLANEOUSSYMBOLSANDARROWS");

        /**
         * “片假名音标扩展”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS =
            new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS",
                             "KATAKANA PHONETIC EXTENSIONS",
                             "KATAKANAPHONETICEXTENSIONS");

        /**
         * “易经六十四卦符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS =
            new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS",
                             "YIJING HEXAGRAM SYMBOLS",
                             "YIJINGHEXAGRAMSYMBOLS");

        /**
         * “变体选择符”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock VARIATION_SELECTORS =
            new UnicodeBlock("VARIATION_SELECTORS",
                             "VARIATION SELECTORS",
                             "VARIATIONSELECTORS");

        /**
         * “线形文字B音节表”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock LINEAR_B_SYLLABARY =
            new UnicodeBlock("LINEAR_B_SYLLABARY",
                             "LINEAR B SYLLABARY",
                             "LINEARBSYLLABARY");

        /**
         * “线形文字B意符”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS =
            new UnicodeBlock("LINEAR_B_IDEOGRAMS",
                             "LINEAR B IDEOGRAMS",
                             "LINEARBIDEOGRAMS");

        /**
         * “爱琴数字”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock AEGEAN_NUMBERS =
            new UnicodeBlock("AEGEAN_NUMBERS",
                             "AEGEAN NUMBERS",
                             "AEGEANNUMBERS");

        /**
         * “古意大利文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock OLD_ITALIC =
            new UnicodeBlock("OLD_ITALIC",
                             "OLD ITALIC",
                             "OLDITALIC");

        /**
         * “哥特文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock GOTHIC =
            new UnicodeBlock("GOTHIC");

        /**
         * “乌加里特文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock UGARITIC =
            new UnicodeBlock("UGARITIC");

        /**
         * “德瑟雷特文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock DESERET =
            new UnicodeBlock("DESERET");

        /**
         * “沙维文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SHAVIAN =
            new UnicodeBlock("SHAVIAN");

        /**
         * “奥斯曼尼亚文”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock OSMANYA =
            new UnicodeBlock("OSMANYA");

        /**
         * “塞浦路斯音节文字”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock CYPRIOT_SYLLABARY =
            new UnicodeBlock("CYPRIOT_SYLLABARY",
                             "CYPRIOT SYLLABARY",
                             "CYPRIOTSYLLABARY");

        /**
         * “拜占庭音乐符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS =
            new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS",
                             "BYZANTINE MUSICAL SYMBOLS",
                             "BYZANTINEMUSICALSYMBOLS");

        /**
         * “音乐符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock MUSICAL_SYMBOLS =
            new UnicodeBlock("MUSICAL_SYMBOLS",
                             "MUSICAL SYMBOLS",
                             "MUSICALSYMBOLS");

        /**
         * “太玄经符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS =
            new UnicodeBlock("TAI_XUAN_JING_SYMBOLS",
                             "TAI XUAN JING SYMBOLS",
                             "TAIXUANJINGSYMBOLS");

        /**
         * “数学字母数字符号”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS =
            new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS",
                             "MATHEMATICAL ALPHANUMERIC SYMBOLS",
                             "MATHEMATICALALPHANUMERICSYMBOLS");

        /**
         * “CJK统一汉字扩展B”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B",
                             "CJK UNIFIED IDEOGRAPHS EXTENSION B",
                             "CJKUNIFIEDIDEOGRAPHSEXTENSIONB");

        /**
         * “CJK兼容汉字补充”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT =
            new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT",
                             "CJK COMPATIBILITY IDEOGRAPHS SUPPLEMENT",
                             "CJKCOMPATIBILITYIDEOGRAPHSSUPPLEMENT");

        /**
         * “标签”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock TAGS =
            new UnicodeBlock("TAGS");

        /**
         * “变体选择符补充”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT =
            new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT",
                             "VARIATION SELECTORS SUPPLEMENT",
                             "VARIATIONSELECTORSSUPPLEMENT");

        /**
         * “补充专用区-A”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A =
            new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A",
                             "SUPPLEMENTARY PRIVATE USE AREA-A",
                             "SUPPLEMENTARYPRIVATEUSEAREA-A");

        /**
         * “补充专用区-B”Unicode字符块的常量。
         * @since 1.5
         */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B =
            new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B",
                             "SUPPLEMENTARY PRIVATE USE AREA-B",
                             "SUPPLEMENTARYPRIVATEUSEAREA-B");

        /**
         * “高位代理”Unicode字符块的常量。
         * 该块表示高位代理范围内的代码点值：U+D800 至 U+DB7F
         *
         * @since 1.5
         */
        public static final UnicodeBlock HIGH_SURROGATES =
            new UnicodeBlock("HIGH_SURROGATES",
                             "HIGH SURROGATES",
                             "HIGHSURROGATES");

        /**
         * “高位专用代理”Unicode字符块的常量。
         * 该块表示专用高位代理范围内的代码点值：U+DB80 至 U+DBFF
         *
         * @since 1.5
         */
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES =
            new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES",
                             "HIGH PRIVATE USE SURROGATES",
                             "HIGHPRIVATEUSESURROGATES");

        /**
         * “低位代理”Unicode字符块的常量。
         * 该块表示低位代理范围内的代码点值：U+DC00 至 U+DFFF
         *
         * @since 1.5
         */
        public static final UnicodeBlock LOW_SURROGATES =
            new UnicodeBlock("LOW_SURROGATES",
                             "LOW SURROGATES",
                             "LOWSURROGATES");


                    /**
         * “阿拉伯补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ARABIC_SUPPLEMENT =
            new UnicodeBlock("ARABIC_SUPPLEMENT",
                             "ARABIC SUPPLEMENT",
                             "ARABICSUPPLEMENT");

        /**
         * “Nko”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock NKO =
            new UnicodeBlock("NKO");

        /**
         * “撒马利亚”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock SAMARITAN =
            new UnicodeBlock("SAMARITAN");

        /**
         * “曼达”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MANDAIC =
            new UnicodeBlock("MANDAIC");

        /**
         * “埃塞俄比亚补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT =
            new UnicodeBlock("ETHIOPIC_SUPPLEMENT",
                             "ETHIOPIC SUPPLEMENT",
                             "ETHIOPICSUPPLEMENT");

        /**
         * “统一加拿大土著音节扩展”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED =
            new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED",
                             "UNIFIED CANADIAN ABORIGINAL SYLLABICS EXTENDED",
                             "UNIFIEDCANADIANABORIGINALSYLLABICSEXTENDED");

        /**
         * “新傣仂文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock NEW_TAI_LUE =
            new UnicodeBlock("NEW_TAI_LUE",
                             "NEW TAI LUE",
                             "NEWTAILUE");

        /**
         * “布吉文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BUGINESE =
            new UnicodeBlock("BUGINESE");

        /**
         * “傣仂文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock TAI_THAM =
            new UnicodeBlock("TAI_THAM",
                             "TAI THAM",
                             "TAITHAM");

        /**
         * “巴厘文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BALINESE =
            new UnicodeBlock("BALINESE");

        /**
         * “巽他文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock SUNDANESE =
            new UnicodeBlock("SUNDANESE");

        /**
         * “巴塔克文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BATAK =
            new UnicodeBlock("BATAK");

        /**
         * “尔恰文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LEPCHA =
            new UnicodeBlock("LEPCHA");

        /**
         * “奥尔奇基文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock OL_CHIKI =
            new UnicodeBlock("OL_CHIKI",
                             "OL CHIKI",
                             "OLCHIKI");

        /**
         * “吠陀扩展”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock VEDIC_EXTENSIONS =
            new UnicodeBlock("VEDIC_EXTENSIONS",
                             "VEDIC EXTENSIONS",
                             "VEDICEXTENSIONS");

        /**
         * “音标扩展补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT =
            new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT",
                             "PHONETIC EXTENSIONS SUPPLEMENT",
                             "PHONETICEXTENSIONSSUPPLEMENT");

        /**
         * “组合附加符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT =
            new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT",
                             "COMBINING DIACRITICAL MARKS SUPPLEMENT",
                             "COMBININGDIACRITICALMARKSSUPPLEMENT");

        /**
         * “格拉哥里文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock GLAGOLITIC =
            new UnicodeBlock("GLAGOLITIC");

        /**
         * “拉丁文扩展-C”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LATIN_EXTENDED_C =
            new UnicodeBlock("LATIN_EXTENDED_C",
                             "LATIN EXTENDED-C",
                             "LATINEXTENDED-C");

        /**
         * “科普特文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock COPTIC =
            new UnicodeBlock("COPTIC");

        /**
         * “格鲁吉亚文补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT =
            new UnicodeBlock("GEORGIAN_SUPPLEMENT",
                             "GEORGIAN SUPPLEMENT",
                             "GEORGIANSUPPLEMENT");

        /**
         * “提非纳文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock TIFINAGH =
            new UnicodeBlock("TIFINAGH");

        /**
         * “埃塞俄比亚文扩展”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ETHIOPIC_EXTENDED =
            new UnicodeBlock("ETHIOPIC_EXTENDED",
                             "ETHIOPIC EXTENDED",
                             "ETHIOPICEXTENDED");

        /**
         * “西里尔文扩展-A”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CYRILLIC_EXTENDED_A =
            new UnicodeBlock("CYRILLIC_EXTENDED_A",
                             "CYRILLIC EXTENDED-A",
                             "CYRILLICEXTENDED-A");

        /**
         * “补充标点符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION =
            new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION",
                             "SUPPLEMENTAL PUNCTUATION",
                             "SUPPLEMENTALPUNCTUATION");

        /**
         * “CJK笔画”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CJK_STROKES =
            new UnicodeBlock("CJK_STROKES",
                             "CJK STROKES",
                             "CJKSTROKES");

        /**
         * “傈僳文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LISU =
            new UnicodeBlock("LISU");

        /**
         * “瓦伊文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock VAI =
            new UnicodeBlock("VAI");

        /**
         * “西里尔文扩展-B”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CYRILLIC_EXTENDED_B =
            new UnicodeBlock("CYRILLIC_EXTENDED_B",
                             "CYRILLIC EXTENDED-B",
                             "CYRILLICEXTENDED-B");

        /**
         * “巴姆文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BAMUM =
            new UnicodeBlock("BAMUM");

        /**
         * “声调修饰字母”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MODIFIER_TONE_LETTERS =
            new UnicodeBlock("MODIFIER_TONE_LETTERS",
                             "MODIFIER TONE LETTERS",
                             "MODIFIERTONELETTERS");

        /**
         * “拉丁文扩展-D”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LATIN_EXTENDED_D =
            new UnicodeBlock("LATIN_EXTENDED_D",
                             "LATIN EXTENDED-D",
                             "LATINEXTENDED-D");

        /**
         * “西罗提那格里文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock SYLOTI_NAGRI =
            new UnicodeBlock("SYLOTI_NAGRI",
                             "SYLOTI NAGRI",
                             "SYLOTINAGRI");

        /**
         * “印度数字形式”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS =
            new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS",
                             "COMMON INDIC NUMBER FORMS",
                             "COMMONINDICNUMBERFORMS");

        /**
         * “八思巴文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock PHAGS_PA =
            new UnicodeBlock("PHAGS_PA",
                             "PHAGS-PA");

        /**
         * “苏拉斯特拉文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock SAURASHTRA =
            new UnicodeBlock("SAURASHTRA");

        /**
         * “天城文扩展”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock DEVANAGARI_EXTENDED =
            new UnicodeBlock("DEVANAGARI_EXTENDED",
                             "DEVANAGARI EXTENDED",
                             "DEVANAGARIEXTENDED");

        /**
         * “卡雅利文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock KAYAH_LI =
            new UnicodeBlock("KAYAH_LI",
                             "KAYAH LI",
                             "KAYAHLI");

        /**
         * “林江文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock REJANG =
            new UnicodeBlock("REJANG");

        /**
         * “韩文音节扩展-A”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A =
            new UnicodeBlock("HANGUL_JAMO_EXTENDED_A",
                             "HANGUL JAMO EXTENDED-A",
                             "HANGULJAMOEXTENDED-A");

        /**
         * “爪哇文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock JAVANESE =
            new UnicodeBlock("JAVANESE");

        /**
         * “占文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CHAM =
            new UnicodeBlock("CHAM");

        /**
         * “缅甸文扩展-A”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MYANMAR_EXTENDED_A =
            new UnicodeBlock("MYANMAR_EXTENDED_A",
                             "MYANMAR EXTENDED-A",
                             "MYANMAREXTENDED-A");

        /**
         * “泰越文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock TAI_VIET =
            new UnicodeBlock("TAI_VIET",
                             "TAI VIET",
                             "TAIVIET");

        /**
         * “埃塞俄比亚文扩展-A”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A =
            new UnicodeBlock("ETHIOPIC_EXTENDED_A",
                             "ETHIOPIC EXTENDED-A",
                             "ETHIOPICEXTENDED-A");

        /**
         * “梅特梅文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MEETEI_MAYEK =
            new UnicodeBlock("MEETEI_MAYEK",
                             "MEETEI MAYEK",
                             "MEETEIMAYEK");

        /**
         * “韩文音节扩展-B”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B =
            new UnicodeBlock("HANGUL_JAMO_EXTENDED_B",
                             "HANGUL JAMO EXTENDED-B",
                             "HANGULJAMOEXTENDED-B");

        /**
         * “竖排形式”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock VERTICAL_FORMS =
            new UnicodeBlock("VERTICAL_FORMS",
                             "VERTICAL FORMS",
                             "VERTICALFORMS");

        /**
         * “古希腊数字”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS =
            new UnicodeBlock("ANCIENT_GREEK_NUMBERS",
                             "ANCIENT GREEK NUMBERS",
                             "ANCIENTGREEKNUMBERS");

        /**
         * “古代符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ANCIENT_SYMBOLS =
            new UnicodeBlock("ANCIENT_SYMBOLS",
                             "ANCIENT SYMBOLS",
                             "ANCIENTSYMBOLS");

        /**
         * “菲斯托斯圆盘”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock PHAISTOS_DISC =
            new UnicodeBlock("PHAISTOS_DISC",
                             "PHAISTOS DISC",
                             "PHAISTOSDISC");

        /**
         * “吕西亚文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LYCIAN =
            new UnicodeBlock("LYCIAN");

        /**
         * “卡里安文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CARIAN =
            new UnicodeBlock("CARIAN");

        /**
         * “古波斯文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock OLD_PERSIAN =
            new UnicodeBlock("OLD_PERSIAN",
                             "OLD PERSIAN",
                             "OLDPERSIAN");


                    /**
         * “帝国阿拉姆语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock IMPERIAL_ARAMAIC =
            new UnicodeBlock("IMPERIAL_ARAMAIC",
                             "IMPERIAL ARAMAIC",
                             "IMPERIALARAMAIC");

        /**
         * “腓尼基语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock PHOENICIAN =
            new UnicodeBlock("PHOENICIAN");

        /**
         * “吕底亚语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock LYDIAN =
            new UnicodeBlock("LYDIAN");

        /**
         * “佉卢文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock KHAROSHTHI =
            new UnicodeBlock("KHAROSHTHI");

        /**
         * “古南阿拉伯语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock OLD_SOUTH_ARABIAN =
            new UnicodeBlock("OLD_SOUTH_ARABIAN",
                             "OLD SOUTH ARABIAN",
                             "OLDSOUTHARABIAN");

        /**
         * “阿维斯陀语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock AVESTAN =
            new UnicodeBlock("AVESTAN");

        /**
         * “帕提亚铭文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN =
            new UnicodeBlock("INSCRIPTIONAL_PARTHIAN",
                             "INSCRIPTIONAL PARTHIAN",
                             "INSCRIPTIONALPARTHIAN");

        /**
         * “帕拉维铭文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI =
            new UnicodeBlock("INSCRIPTIONAL_PAHLAVI",
                             "INSCRIPTIONAL PAHLAVI",
                             "INSCRIPTIONALPAHLAVI");

        /**
         * “古突厥语”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock OLD_TURKIC =
            new UnicodeBlock("OLD_TURKIC",
                             "OLD TURKIC",
                             "OLDTURKIC");

        /**
         * “鲁米数字符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS =
            new UnicodeBlock("RUMI_NUMERAL_SYMBOLS",
                             "RUMI NUMERAL SYMBOLS",
                             "RUMINUMERALSYMBOLS");

        /**
         * “婆罗米文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BRAHMI =
            new UnicodeBlock("BRAHMI");

        /**
         * “凯伊提文”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock KAITHI =
            new UnicodeBlock("KAITHI");

        /**
         * “楔形文字”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CUNEIFORM =
            new UnicodeBlock("CUNEIFORM");

        /**
         * “楔形文字数字和标点”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION =
            new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION",
                             "CUNEIFORM NUMBERS AND PUNCTUATION",
                             "CUNEIFORMNUMBERSANDPUNCTUATION");

        /**
         * “埃及象形文字”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS =
            new UnicodeBlock("EGYPTIAN_HIEROGLYPHS",
                             "EGYPTIAN HIEROGLYPHS",
                             "EGYPTIANHIEROGLYPHS");

        /**
         * “巴姆姆补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock BAMUM_SUPPLEMENT =
            new UnicodeBlock("BAMUM_SUPPLEMENT",
                             "BAMUM SUPPLEMENT",
                             "BAMUMSUPPLEMENT");

        /**
         * “假名补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock KANA_SUPPLEMENT =
            new UnicodeBlock("KANA_SUPPLEMENT",
                             "KANA SUPPLEMENT",
                             "KANASUPPLEMENT");

        /**
         * “古希腊音乐符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION =
            new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION",
                             "ANCIENT GREEK MUSICAL NOTATION",
                             "ANCIENTGREEKMUSICALNOTATION");

        /**
         * “算筹数字”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock COUNTING_ROD_NUMERALS =
            new UnicodeBlock("COUNTING_ROD_NUMERALS",
                             "COUNTING ROD NUMERALS",
                             "COUNTINGRODNUMERALS");

        /**
         * “麻将牌”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MAHJONG_TILES =
            new UnicodeBlock("MAHJONG_TILES",
                             "MAHJONG TILES",
                             "MAHJONGTILES");

        /**
         * “多米诺骨牌”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock DOMINO_TILES =
            new UnicodeBlock("DOMINO_TILES",
                             "DOMINO TILES",
                             "DOMINOTILES");

        /**
         * “纸牌”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock PLAYING_CARDS =
            new UnicodeBlock("PLAYING_CARDS",
                             "PLAYING CARDS",
                             "PLAYINGCARDS");

        /**
         * “带圈字母数字补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT =
            new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT",
                             "ENCLOSED ALPHANUMERIC SUPPLEMENT",
                             "ENCLOSEDALPHANUMERICSUPPLEMENT");

        /**
         * “带圈表意文字补充”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT =
            new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT",
                             "ENCLOSED IDEOGRAPHIC SUPPLEMENT",
                             "ENCLOSEDIDEOGRAPHICSUPPLEMENT");

        /**
         * “杂项符号和图画”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS",
                             "MISCELLANEOUS SYMBOLS AND PICTOGRAPHS",
                             "MISCELLANEOUSSYMBOLSANDPICTOGRAPHS");

        /**
         * “表情符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock EMOTICONS =
            new UnicodeBlock("EMOTICONS");

        /**
         * “交通和地图符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS =
            new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS",
                             "TRANSPORT AND MAP SYMBOLS",
                             "TRANSPORTANDMAPSYMBOLS");

        /**
         * “炼金术符号”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS =
            new UnicodeBlock("ALCHEMICAL_SYMBOLS",
                             "ALCHEMICAL SYMBOLS",
                             "ALCHEMICALSYMBOLS");

        /**
         * “CJK统一汉字扩展C”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C",
                             "CJK UNIFIED IDEOGRAPHS EXTENSION C",
                             "CJKUNIFIEDIDEOGRAPHSEXTENSIONC");

        /**
         * “CJK统一汉字扩展D”Unicode字符块的常量。
         * @since 1.7
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D",
                             "CJK UNIFIED IDEOGRAPHS EXTENSION D",
                             "CJKUNIFIEDIDEOGRAPHSEXTENSIOND");

        /**
         * “阿拉伯语扩展A”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock ARABIC_EXTENDED_A =
            new UnicodeBlock("ARABIC_EXTENDED_A",
                             "ARABIC EXTENDED-A",
                             "ARABICEXTENDED-A");

        /**
         * “巽他语补充”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock SUNDANESE_SUPPLEMENT =
            new UnicodeBlock("SUNDANESE_SUPPLEMENT",
                             "SUNDANESE SUPPLEMENT",
                             "SUNDANESESUPPLEMENT");

        /**
         * “梅特伊梅克扩展”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS =
            new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS",
                             "MEETEI MAYEK EXTENSIONS",
                             "MEETEIMAYEKEXTENSIONS");

        /**
         * “麦罗伊象形文字”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock MEROITIC_HIEROGLYPHS =
            new UnicodeBlock("MEROITIC_HIEROGLYPHS",
                             "MEROITIC HIEROGLYPHS",
                             "MEROITICHIEROGLYPHS");

        /**
         * “麦罗伊草书”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock MEROITIC_CURSIVE =
            new UnicodeBlock("MEROITIC_CURSIVE",
                             "MEROITIC CURSIVE",
                             "MEROITICCURSIVE");

        /**
         * “索拉松彭”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock SORA_SOMPENG =
            new UnicodeBlock("SORA_SOMPENG",
                             "SORA SOMPENG",
                             "SORASOMPENG");

        /**
         * “查克马文”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock CHAKMA =
            new UnicodeBlock("CHAKMA");

        /**
         * “夏拉达文”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock SHARADA =
            new UnicodeBlock("SHARADA");

        /**
         * “塔克里文”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock TAKRI =
            new UnicodeBlock("TAKRI");

        /**
         * “苗文”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock MIAO =
            new UnicodeBlock("MIAO");

        /**
         * “阿拉伯数学字母符号”Unicode字符块的常量。
         * @since 1.8
         */
        public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS =
            new UnicodeBlock("ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS",
                             "ARABIC MATHEMATICAL ALPHABETIC SYMBOLS",
                             "ARABICMATHEMATICALALPHABETICSYMBOLS");

        /**
         * “CJK统一汉字扩展E”Unicode字符块的常量。
         * @apiNote 此字段在Java SE 8维护版本5中定义。
         * @since 1.8
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E =
                new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E",
                        "CJK UNIFIED IDEOGRAPHS EXTENSION E",
                        "CJKUNIFIEDIDEOGRAPHSEXTENSIONE");

        private static final int blockStarts[] = {
            0x0000,   // 0000..007F; 基本拉丁文
            0x0080,   // 0080..00FF; 拉丁文-1补充
            0x0100,   // 0100..017F; 拉丁文扩展-A
            0x0180,   // 0180..024F; 拉丁文扩展-B
            0x0250,   // 0250..02AF; 国际音标扩展
            0x02B0,   // 02B0..02FF; 间距修饰字母
            0x0300,   // 0300..036F; 组合变音符号
            0x0370,   // 0370..03FF; 希腊语和科普特语
            0x0400,   // 0400..04FF; 俄语
            0x0500,   // 0500..052F; 俄语补充
            0x0530,   // 0530..058F; 亚美尼亚语
            0x0590,   // 0590..05FF; 希伯来语
            0x0600,   // 0600..06FF; 阿拉伯语
            0x0700,   // 0700..074F; 叙利亚语
            0x0750,   // 0750..077F; 阿拉伯语补充
            0x0780,   // 0780..07BF; 塔阿纳语
            0x07C0,   // 07C0..07FF; N'Ko
            0x0800,   // 0800..083F; 撒马利亚语
            0x0840,   // 0840..085F; 曼达语
            0x0860,   //             未分配
            0x08A0,   // 08A0..08FF; 阿拉伯语扩展-A
            0x0900,   // 0900..097F; 印地语
            0x0980,   // 0980..09FF; 孟加拉语
            0x0A00,   // 0A00..0A7F; 高卢语
            0x0A80,   // 0A80..0AFF; 古吉拉特语
            0x0B00,   // 0B00..0B7F; 奥里亚语
            0x0B80,   // 0B80..0BFF; 泰米尔语
            0x0C00,   // 0C00..0C7F; 泰卢固语
            0x0C80,   // 0C80..0CFF; 卡纳达语
            0x0D00,   // 0D00..0D7F; 马拉雅拉姆语
            0x0D80,   // 0D80..0DFF; 僧伽罗语
            0x0E00,   // 0E00..0E7F; 泰语
            0x0E80,   // 0E80..0EFF; 老挝语
            0x0F00,   // 0F00..0FFF; 藏语
            0x1000,   // 1000..109F; 缅甸语
            0x10A0,   // 10A0..10FF; 格鲁吉亚语
            0x1100,   // 1100..11FF; 韩国语字母
            0x1200,   // 1200..137F; 埃塞俄比亚语
            0x1380,   // 1380..139F; 埃塞俄比亚语补充
            0x13A0,   // 13A0..13FF; 切罗基语
            0x1400,   // 1400..167F; 统一加拿大土著音节
            0x1680,   // 1680..169F; 奥甘姆文
            0x16A0,   // 16A0..16FF; 鲁尼文
            0x1700,   // 1700..171F; 塔加洛语
            0x1720,   // 1720..173F; 汉努诺语
            0x1740,   // 1740..175F; 布希德语
            0x1760,   // 1760..177F; 塔格班瓦语
            0x1780,   // 1780..17FF; 高棉语
            0x1800,   // 1800..18AF; 蒙古语
            0x18B0,   // 18B0..18FF; 统一加拿大土著音节扩展
            0x1900,   // 1900..194F; 林布语
            0x1950,   // 1950..197F; 泰勒语
            0x1980,   // 1980..19DF; 新泰语
            0x19E0,   // 19E0..19FF; 高棉语符号
            0x1A00,   // 1A00..1A1F; 布吉内语
            0x1A20,   // 1A20..1AAF; 泰坦语
            0x1AB0,   //             未分配
            0x1B00,   // 1B00..1B7F; 巴厘语
            0x1B80,   // 1B80..1BBF; 巽他语
            0x1BC0,   // 1BC0..1BFF; 巴塔克语
            0x1C00,   // 1C00..1C4F; 莱普查语
            0x1C50,   // 1C50..1C7F; 奥尔奇基语
            0x1C80,   //             未分配
            0x1CC0,   // 1CC0..1CCF; 巽他语补充
            0x1CD0,   // 1CD0..1CFF; 吠陀扩展
            0x1D00,   // 1D00..1D7F; 音标扩展
            0x1D80,   // 1D80..1DBF; 音标扩展补充
            0x1DC0,   // 1DC0..1DFF; 组合变音符号补充
            0x1E00,   // 1E00..1EFF; 拉丁文扩展附加
            0x1F00,   // 1F00..1FFF; 希腊语扩展
            0x2000,   // 2000..206F; 通用标点符号
            0x2070,   // 2070..209F; 上标和下标
            0x20A0,   // 20A0..20CF; 货币符号
            0x20D0,   // 20D0..20FF; 符号组合变音符号
            0x2100,   // 2100..214F; 字母样符号
            0x2150,   // 2150..218F; 数字形式
            0x2190,   // 2190..21FF; 箭头
            0x2200,   // 2200..22FF; 数学运算符
            0x2300,   // 2300..23FF; 杂项技术符号
            0x2400,   // 2400..243F; 控制图片
            0x2440,   // 2440..245F; 光学字符识别
            0x2460,   // 2460..24FF; 带圈字母数字
            0x2500,   // 2500..257F; 框线
            0x2580,   // 2580..259F; 块元素
            0x25A0,   // 25A0..25FF; 几何形状
            0x2600,   // 2600..26FF; 杂项符号
            0x2700,   // 2700..27BF; 丁巴茨
            0x27C0,   // 27C0..27EF; 杂项数学符号-A
            0x27F0,   // 27F0..27FF; 补充箭头-A
            0x2800,   // 2800..28FF; 盲文图案
            0x2900,   // 2900..297F; 补充箭头-B
            0x2980,   // 2980..29FF; 杂项数学符号-B
            0x2A00,   // 2A00..2AFF; 补充数学运算符
            0x2B00,   // 2B00..2BFF; 杂项符号和箭头
            0x2C00,   // 2C00..2C5F; 格拉哥利语
            0x2C60,   // 2C60..2C7F; 拉丁文扩展-C
            0x2C80,   // 2C80..2CFF; 科普特语
            0x2D00,   // 2D00..2D2F; 格鲁吉亚语补充
            0x2D30,   // 2D30..2D7F; 提菲纳语
            0x2D80,   // 2D80..2DDF; 埃塞俄比亚语扩展
            0x2DE0,   // 2DE0..2DFF; 俄语扩展-A
            0x2E00,   // 2E00..2E7F; 补充标点符号
            0x2E80,   // 2E80..2EFF; CJK部首补充
            0x2F00,   // 2F00..2FDF; 康熙部首
            0x2FE0,   //             未分配
            0x2FF0,   // 2FF0..2FFF; 汉字描述字符
            0x3000,   // 3000..303F; CJK符号和标点
            0x3040,   // 3040..309F; 平假名
            0x30A0,   // 30A0..30FF; 片假名
            0x3100,   // 3100..312F; 注音符号
            0x3130,   // 3130..318F; 韩国语兼容字母
            0x3190,   // 3190..319F; 汉文
            0x31A0,   // 31A0..31BF; 注音符号扩展
            0x31C0,   // 31C0..31EF; CJK笔画
            0x31F0,   // 31F0..31FF; 片假名音标扩展
            0x3200,   // 3200..32FF; 带圈CJK字母和月份
            0x3300,   // 3300..33FF; CJK兼容
            0x3400,   // 3400..4DBF; CJK统一汉字扩展A
            0x4DC0,   // 4DC0..4DFF; 易经六十四卦符号
            0x4E00,   // 4E00..9FFF; CJK统一汉字
            0xA000,   // A000..A48F; 彝语音节
            0xA490,   // A490..A4CF; 彝语部首
            0xA4D0,   // A4D0..A4FF; 历苏语
            0xA500,   // A500..A63F; 瓦伊语
            0xA640,   // A640..A69F; 俄语扩展-B
            0xA6A0,   // A6A0..A6FF; 巴姆姆语
            0xA700,   // A700..A71F; 修饰音调字母
            0xA720,   // A720..A7FF; 拉丁文扩展-D
            0xA800,   // A800..A82F; 西罗提-那格里语
            0xA830,   // A830..A83F; 印度通用数字形式
            0xA840,   // A840..A87F; 八思巴文
            0xA880,   // A880..A8DF; 萨乌拉什特拉语
            0xA8E0,   // A8E0..A8FF; 印地语扩展
            0xA900,   // A900..A92F; 卡雅利语
            0xA930,   // A930..A95F; 仁让语
            0xA960,   // A960..A97F; 韩国语字母扩展-A
            0xA980,   // A980..A9DF; 爪哇语
            0xA9E0,   //             未分配
            0xAA00,   // AA00..AA5F; 柬埔寨语
            0xAA60,   // AA60..AA7F; 缅甸语扩展-A
            0xAA80,   // AA80..AADF; 泰维语
            0xAAE0,   // AAE0..AAFF; 梅特伊梅克扩展
            0xAB00,   // AB00..AB2F; 埃塞俄比亚语扩展-A
            0xAB30,   //             未分配
            0xABC0,   // ABC0..ABFF; 梅特伊梅克语
            0xAC00,   // AC00..D7AF; 韩国语音节
            0xD7B0,   // D7B0..D7FF; 韩国语字母扩展-B
            0xD800,   // D800..DB7F; 高位代理
            0xDB80,   // DB80..DBFF; 高位私用代理
            0xDC00,   // DC00..DFFF; 低位代理
            0xE000,   // E000..F8FF; 私用区
            0xF900,   // F900..FAFF; CJK兼容汉字
            0xFB00,   // FB00..FB4F; 字母呈现形式
            0xFB50,   // FB50..FDFF; 阿拉伯语呈现形式-A
            0xFE00,   // FE00..FE0F; 变体选择器
            0xFE10,   // FE10..FE1F; 垂直形式
            0xFE20,   // FE20..FE2F; 组合半标记
            0xFE30,   // FE30..FE4F; CJK兼容形式
            0xFE50,   // FE50..FE6F; 小形式变体
            0xFE70,   // FE70..FEFF; 阿拉伯语呈现形式-B
            0xFF00,   // FF00..FFEF; 半宽和全宽形式
            0xFFF0,   // FFF0..FFFF; 特殊字符
            0x10000,  // 10000..1007F; 线形文字B音节
            0x10080,  // 10080..100FF; 线形文字B象形文字
            0x10100,  // 10100..1013F; 爱琴数字
            0x10140,  // 10140..1018F; 古希腊数字
            0x10190,  // 10190..101CF; 古代符号
            0x101D0,  // 101D0..101FF; 法伊斯特斯圆盘
            0x10200,  //               未分配
            0x10280,  // 10280..1029F; 吕基亚语
            0x102A0,  // 102A0..102DF; 卡里安语
            0x102E0,  //               未分配
            0x10300,  // 10300..1032F; 古意大利文
            0x10330,  // 10330..1034F; 哥特语
            0x10350,  //               未分配
            0x10380,  // 10380..1039F; 乌加里特语
            0x103A0,  // 103A0..103DF; 古波斯语
            0x103E0,  //               未分配
            0x10400,  // 10400..1044F; 德瑟雷特文
            0x10450,  // 10450..1047F; 沙维安文
            0x10480,  // 10480..104AF; 奥斯曼亚文
            0x104B0,  //               未分配
            0x10800,  // 10800..1083F; 塞浦路斯音节
            0x10840,  // 10840..1085F; 帝国阿拉姆语
            0x10860,  //               未分配
            0x10900,  // 10900..1091F; 腓尼基语
            0x10920,  // 10920..1093F; 吕底亚语
            0x10940,  //               未分配
            0x10980,  // 10980..1099F; 麦罗伊象形文字
            0x109A0,  // 109A0..109FF; 麦罗伊草书
            0x10A00,  // 10A00..10A5F; 佉卢文
            0x10A60,  // 10A60..10A7F; 古南阿拉伯语
            0x10A80,  //               未分配
            0x10B00,  // 10B00..10B3F; 阿维斯陀语
            0x10B40,  // 10B40..10B5F; 帕提亚铭文
            0x10B60,  // 10B60..10B7F; 帕拉维铭文
            0x10B80,  //               未分配
            0x10C00,  // 10C00..10C4F; 古突厥语
            0x10C50,  //               未分配
            0x10E60,  // 10E60..10E7F; 鲁米数字符号
            0x10E80,  //               未分配
            0x11000,  // 11000..1107F; 婆罗米文
            0x11080,  // 11080..110CF; 凯伊提文
            0x110D0,  // 110D0..110FF; 索拉松彭
            0x11100,  // 11100..1114F; 查克马文
            0x11150,  //               未分配
            0x11180,  // 11180..111DF; 夏拉达文
            0x111E0,  //               未分配
            0x11680,  // 11680..116CF; 塔克里文
            0x116D0,  //               未分配
            0x12000,  // 12000..123FF; 楔形文字
            0x12400,  // 12400..1247F; 楔形文字数字和标点
            0x12480,  //               未分配
            0x13000,  // 13000..1342F; 埃及象形文字
            0x13430,  //               未分配
            0x16800,  // 16800..16A3F; 巴姆姆补充
            0x16A40,  //               未分配
            0x16F00,  // 16F00..16F9F; 苗文
            0x16FA0,  //               未分配
            0x1B000,  // 1B000..1B0FF; 假名补充
            0x1B100,  //               未分配
            0x1D000,  // 1D000..1D0FF; 拜占庭音乐符号
            0x1D100,  // 1D100..1D1FF; 音乐符号
            0x1D200,  // 1D200..1D24F; 古希腊音乐符号
            0x1D250,  //               未分配
            0x1D300,  // 1D300..1D35F; 太玄经符号
            0x1D360,  // 1D360..1D37F; 算筹数字
            0x1D380,  //               未分配
            0x1D400,  // 1D400..1D7FF; 数学字母数字符号
            0x1D800,  //               未分配
            0x1EE00,  // 1EE00..1EEFF; 阿拉伯数学字母符号
            0x1EF00,  //               未分配
            0x1F000,  // 1F000..1F02F; 麻将牌
            0x1F030,  // 1F030..1F09F; 多米诺骨牌
            0x1F0A0,  // 1F0A0..1F0FF; 纸牌
            0x1F100,  // 1F100..1F1FF; 带圈字母数字补充
            0x1F200,  // 1F200..1F2FF; 带圈表意文字补充
            0x1F300,  // 1F300..1F5FF; 杂项符号和图画
            0x1F600,  // 1F600..1F64F; 表情符号
            0x1F650,  //               未分配
            0x1F680,  // 1F680..1F6FF; 交通和地图符号
            0x1F700,  // 1F700..1F77F; 炼金术符号
            0x1F780,  //               未分配
            0x20000,  // 20000..2A6DF; CJK统一汉字扩展B
            0x2A6E0,  //               未分配
            0x2A700,  // 2A700..2B73F; CJK统一汉字扩展C
            0x2B740,  // 2B740..2B81F; CJK统一汉字扩展D
            0x2B820,  // 2B820..2CEAF; CJK统一汉字扩展E
            0x2CEB0,  //               未分配
            0x2F800,  // 2F800..2FA1F; CJK兼容汉字补充
            0x2FA20,  //               未分配
            0xE0000,  // E0000..E007F; 标签
            0xE0080,  //               未分配
            0xE0100,  // E0100..E01EF; 变体选择器补充
            0xE01F0,  //               未分配
            0xF0000,  // F0000..FFFFF; 补充私用区-A
            0x100000  // 100000..10FFFF; 补充私用区-B
        };


                    private static final UnicodeBlock[] blocks = {
            BASIC_LATIN,
            LATIN_1_SUPPLEMENT,
            LATIN_EXTENDED_A,
            LATIN_EXTENDED_B,
            IPA_EXTENSIONS,
            SPACING_MODIFIER_LETTERS,
            COMBINING_DIACRITICAL_MARKS,
            GREEK,
            CYRILLIC,
            CYRILLIC_SUPPLEMENTARY,
            ARMENIAN,
            HEBREW,
            ARABIC,
            SYRIAC,
            ARABIC_SUPPLEMENT,
            THAANA,
            NKO,
            SAMARITAN,
            MANDAIC,
            null,
            ARABIC_EXTENDED_A,
            DEVANAGARI,
            BENGALI,
            GURMUKHI,
            GUJARATI,
            ORIYA,
            TAMIL,
            TELUGU,
            KANNADA,
            MALAYALAM,
            SINHALA,
            THAI,
            LAO,
            TIBETAN,
            MYANMAR,
            GEORGIAN,
            HANGUL_JAMO,
            ETHIOPIC,
            ETHIOPIC_SUPPLEMENT,
            CHEROKEE,
            UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
            OGHAM,
            RUNIC,
            TAGALOG,
            HANUNOO,
            BUHID,
            TAGBANWA,
            KHMER,
            MONGOLIAN,
            UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED,
            LIMBU,
            TAI_LE,
            NEW_TAI_LUE,
            KHMER_SYMBOLS,
            BUGINESE,
            TAI_THAM,
            null,
            BALINESE,
            SUNDANESE,
            BATAK,
            LEPCHA,
            OL_CHIKI,
            null,
            SUNDANESE_SUPPLEMENT,
            VEDIC_EXTENSIONS,
            PHONETIC_EXTENSIONS,
            PHONETIC_EXTENSIONS_SUPPLEMENT,
            COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,
            LATIN_EXTENDED_ADDITIONAL,
            GREEK_EXTENDED,
            GENERAL_PUNCTUATION,
            SUPERSCRIPTS_AND_SUBSCRIPTS,
            CURRENCY_SYMBOLS,
            COMBINING_MARKS_FOR_SYMBOLS,
            LETTERLIKE_SYMBOLS,
            NUMBER_FORMS,
            ARROWS,
            MATHEMATICAL_OPERATORS,
            MISCELLANEOUS_TECHNICAL,
            CONTROL_PICTURES,
            OPTICAL_CHARACTER_RECOGNITION,
            ENCLOSED_ALPHANUMERICS,
            BOX_DRAWING,
            BLOCK_ELEMENTS,
            GEOMETRIC_SHAPES,
            MISCELLANEOUS_SYMBOLS,
            DINGBATS,
            MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,
            SUPPLEMENTAL_ARROWS_A,
            BRAILLE_PATTERNS,
            SUPPLEMENTAL_ARROWS_B,
            MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
            SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
            MISCELLANEOUS_SYMBOLS_AND_ARROWS,
            GLAGOLITIC,
            LATIN_EXTENDED_C,
            COPTIC,
            GEORGIAN_SUPPLEMENT,
            TIFINAGH,
            ETHIOPIC_EXTENDED,
            CYRILLIC_EXTENDED_A,
            SUPPLEMENTAL_PUNCTUATION,
            CJK_RADICALS_SUPPLEMENT,
            KANGXI_RADICALS,
            null,
            IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
            CJK_SYMBOLS_AND_PUNCTUATION,
            HIRAGANA,
            KATAKANA,
            BOPOMOFO,
            HANGUL_COMPATIBILITY_JAMO,
            KANBUN,
            BOPOMOFO_EXTENDED,
            CJK_STROKES,
            KATAKANA_PHONETIC_EXTENSIONS,
            ENCLOSED_CJK_LETTERS_AND_MONTHS,
            CJK_COMPATIBILITY,
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
            YIJING_HEXAGRAM_SYMBOLS,
            CJK_UNIFIED_IDEOGRAPHS,
            YI_SYLLABLES,
            YI_RADICALS,
            LISU,
            VAI,
            CYRILLIC_EXTENDED_B,
            BAMUM,
            MODIFIER_TONE_LETTERS,
            LATIN_EXTENDED_D,
            SYLOTI_NAGRI,
            COMMON_INDIC_NUMBER_FORMS,
            PHAGS_PA,
            SAURASHTRA,
            DEVANAGARI_EXTENDED,
            KAYAH_LI,
            REJANG,
            HANGUL_JAMO_EXTENDED_A,
            JAVANESE,
            null,
            CHAM,
            MYANMAR_EXTENDED_A,
            TAI_VIET,
            MEETEI_MAYEK_EXTENSIONS,
            ETHIOPIC_EXTENDED_A,
            null,
            MEETEI_MAYEK,
            HANGUL_SYLLABLES,
            HANGUL_JAMO_EXTENDED_B,
            HIGH_SURROGATES,
            HIGH_PRIVATE_USE_SURROGATES,
            LOW_SURROGATES,
            PRIVATE_USE_AREA,
            CJK_COMPATIBILITY_IDEOGRAPHS,
            ALPHABETIC_PRESENTATION_FORMS,
            ARABIC_PRESENTATION_FORMS_A,
            VARIATION_SELECTORS,
            VERTICAL_FORMS,
            COMBINING_HALF_MARKS,
            CJK_COMPATIBILITY_FORMS,
            SMALL_FORM_VARIANTS,
            ARABIC_PRESENTATION_FORMS_B,
            HALFWIDTH_AND_FULLWIDTH_FORMS,
            SPECIALS,
            LINEAR_B_SYLLABARY,
            LINEAR_B_IDEOGRAMS,
            AEGEAN_NUMBERS,
            ANCIENT_GREEK_NUMBERS,
            ANCIENT_SYMBOLS,
            PHAISTOS_DISC,
            null,
            LYCIAN,
            CARIAN,
            null,
            OLD_ITALIC,
            GOTHIC,
            null,
            UGARITIC,
            OLD_PERSIAN,
            null,
            DESERET,
            SHAVIAN,
            OSMANYA,
            null,
            CYPRIOT_SYLLABARY,
            IMPERIAL_ARAMAIC,
            null,
            PHOENICIAN,
            LYDIAN,
            null,
            MEROITIC_HIEROGLYPHS,
            MEROITIC_CURSIVE,
            KHAROSHTHI,
            OLD_SOUTH_ARABIAN,
            null,
            AVESTAN,
            INSCRIPTIONAL_PARTHIAN,
            INSCRIPTIONAL_PAHLAVI,
            null,
            OLD_TURKIC,
            null,
            RUMI_NUMERAL_SYMBOLS,
            null,
            BRAHMI,
            KAITHI,
            SORA_SOMPENG,
            CHAKMA,
            null,
            SHARADA,
            null,
            TAKRI,
            null,
            CUNEIFORM,
            CUNEIFORM_NUMBERS_AND_PUNCTUATION,
            null,
            EGYPTIAN_HIEROGLYPHS,
            null,
            BAMUM_SUPPLEMENT,
            null,
            MIAO,
            null,
            KANA_SUPPLEMENT,
            null,
            BYZANTINE_MUSICAL_SYMBOLS,
            MUSICAL_SYMBOLS,
            ANCIENT_GREEK_MUSICAL_NOTATION,
            null,
            TAI_XUAN_JING_SYMBOLS,
            COUNTING_ROD_NUMERALS,
            null,
            MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
            null,
            ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS,
            null,
            MAHJONG_TILES,
            DOMINO_TILES,
            PLAYING_CARDS,
            ENCLOSED_ALPHANUMERIC_SUPPLEMENT,
            ENCLOSED_IDEOGRAPHIC_SUPPLEMENT,
            MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
            EMOTICONS,
            null,
            TRANSPORT_AND_MAP_SYMBOLS,
            ALCHEMICAL_SYMBOLS,
            null,
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            null,
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E,
            null,
            CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
            null,
            TAGS,
            null,
            VARIATION_SELECTORS_SUPPLEMENT,
            null,
            SUPPLEMENTARY_PRIVATE_USE_AREA_A,
            SUPPLEMENTARY_PRIVATE_USE_AREA_B
        };


        /**
         * 返回包含给定字符的 Unicode 块对象，如果该字符不是定义的块的成员，则返回 {@code null}。
         *
         * <p><b>注意：</b> 此方法无法处理
         * <a href="Character.html#supplementary"> 补充字符 </a>。 若要支持所有 Unicode 字符，包括
         * 补充字符，请使用 {@link #of(int)} 方法。
         *
         * @param   c  问题字符
         * @return  该字符所属的 {@code UnicodeBlock} 实例，如果该字符不属于任何
         *          Unicode 块，则返回 {@code null}
         */
        public static UnicodeBlock of(char c) {
            return of((int)c);
        }

        /**
         * 返回包含给定字符（Unicode 代码点）的 Unicode 块对象，如果该字符不是定义的块的成员，则返回
         * {@code null}。
         *
         * @param   codePoint  问题字符（Unicode 代码点）。
         * @return  该字符所属的 {@code UnicodeBlock} 实例，如果该字符不属于任何
         *          Unicode 块，则返回 {@code null}
         * @exception IllegalArgumentException 如果指定的
         * {@code codePoint} 是无效的 Unicode 代码点。
         * @see Character#isValidCodePoint(int)
         * @since   1.5
         */
        public static UnicodeBlock of(int codePoint) {
            if (!isValidCodePoint(codePoint)) {
                throw new IllegalArgumentException();
            }

            int top, bottom, current;
            bottom = 0;
            top = blockStarts.length;
            current = top/2;

            // 不变量：top > current >= bottom && codePoint >= unicodeBlockStarts[bottom]
            while (top - bottom > 1) {
                if (codePoint >= blockStarts[current]) {
                    bottom = current;
                } else {
                    top = current;
                }
                current = (top + bottom) / 2;
            }
            return blocks[current];
        }

        /**
         * 返回具有给定名称的 UnicodeBlock。块名称由 Unicode 标准确定。文件
         * Blocks-&lt;version&gt;.txt 为特定版本的标准定义了块。 {@link Character} 类指定了
         * 它支持的标准版本。
         * <p>
         * 此方法接受以下形式的块名称：
         * <ol>
         * <li> Unicode 标准定义的规范块名称。例如，标准定义了一个 "Basic Latin" 块。因此，此
         * 方法接受 "Basic Latin" 作为有效的块名称。每个 UnicodeBlock 的文档提供了规范名称。
         * <li> 规范块名称中所有空格被移除。例如，"BasicLatin" 是 "Basic Latin" 块的有效名称。
         * <li> 每个常量 UnicodeBlock 标识符的文本表示。例如，如果提供 "BASIC_LATIN" 名称，此方法将返回
         * {@link #BASIC_LATIN} 块。此形式将规范名称中的所有空格和连字符替换为下划线。
         * </ol>
         * 最后，所有有效的块名称形式都忽略字符大小写。例如，"BASIC_LATIN" 和 "basic_latin" 都是有效的块名称。
         * 使用 en_US 语言环境的大小写映射规则为块名称验证提供不区分大小写的字符串比较。
         * <p>
         * 如果 Unicode 标准更改了块名称，将接受以前和当前的名称。
         *
         * @param blockName 一个 {@code UnicodeBlock} 名称。
         * @return 由 {@code blockName} 识别的 {@code UnicodeBlock} 实例
         * @throws IllegalArgumentException 如果 {@code blockName} 是无效名称
         * @throws NullPointerException 如果 {@code blockName} 为 null
         * @since 1.5
         */
        public static final UnicodeBlock forName(String blockName) {
            UnicodeBlock block = map.get(blockName.toUpperCase(Locale.US));
            if (block == null) {
                throw new IllegalArgumentException();
            }
            return block;
        }
    }


    /**
     * 代表 <a href="http://www.unicode.org/reports/tr24/">
     * <i>Unicode 标准附录 #24：脚本名称</i></a> 中定义的字符脚本的字符子集系列。每个 Unicode
     * 字符都被分配到一个 Unicode 脚本，要么是一个特定的脚本，如 {@link Character.UnicodeScript#LATIN Latin}，要么
     * 是以下三个特殊值之一，
     * {@link Character.UnicodeScript#INHERITED Inherited}，
     * {@link Character.UnicodeScript#COMMON Common} 或
     * {@link Character.UnicodeScript#UNKNOWN Unknown}。
     *
     * @since 1.7
     */
    public static enum UnicodeScript {
        /**
         * Unicode 脚本 "Common"。
         */
        COMMON,

        /**
         * Unicode 脚本 "Latin"。
         */
        LATIN,

        /**
         * Unicode 脚本 "Greek"。
         */
        GREEK,

        /**
         * Unicode 脚本 "Cyrillic"。
         */
        CYRILLIC,

        /**
         * Unicode 脚本 "Armenian"。
         */
        ARMENIAN,

        /**
         * Unicode 脚本 "Hebrew"。
         */
        HEBREW,

        /**
         * Unicode 脚本 "Arabic"。
         */
        ARABIC,

        /**
         * Unicode 脚本 "Syriac"。
         */
        SYRIAC,

        /**
         * Unicode 脚本 "Thaana"。
         */
        THAANA,

        /**
         * Unicode 脚本 "Devanagari"。
         */
        DEVANAGARI,

        /**
         * Unicode 脚本 "Bengali"。
         */
        BENGALI,

        /**
         * Unicode 脚本 "Gurmukhi"。
         */
        GURMUKHI,

        /**
         * Unicode 脚本 "Gujarati"。
         */
        GUJARATI,

        /**
         * Unicode 脚本 "Oriya"。
         */
        ORIYA,

        /**
         * Unicode 脚本 "Tamil"。
         */
        TAMIL,

        /**
         * Unicode 脚本 "Telugu"。
         */
        TELUGU,

        /**
         * Unicode 脚本 "Kannada"。
         */
        KANNADA,

        /**
         * Unicode 脚本 "Malayalam"。
         */
        MALAYALAM,

        /**
         * Unicode 脚本 "Sinhala"。
         */
        SINHALA,

        /**
         * Unicode 脚本 "Thai"。
         */
        THAI,

        /**
         * Unicode 脚本 "Lao"。
         */
        LAO,

        /**
         * Unicode 脚本 "Tibetan"。
         */
        TIBETAN,

        /**
         * Unicode 脚本 "Myanmar"。
         */
        MYANMAR,

        /**
         * Unicode 脚本 "Georgian"。
         */
        GEORGIAN,

        /**
         * Unicode 脚本 "Hangul"。
         */
        HANGUL,

        /**
         * Unicode 脚本 "Ethiopic"。
         */
        ETHIOPIC,

        /**
         * Unicode 脚本 "Cherokee"。
         */
        CHEROKEE,

        /**
         * Unicode 脚本 "Canadian_Aboriginal"。
         */
        CANADIAN_ABORIGINAL,

        /**
         * Unicode 脚本 "Ogham"。
         */
        OGHAM,

        /**
         * Unicode 脚本 "Runic"。
         */
        RUNIC,

        /**
         * Unicode 脚本 "Khmer"。
         */
        KHMER,

        /**
         * Unicode 脚本 "Mongolian"。
         */
        MONGOLIAN,

        /**
         * Unicode 脚本 "Hiragana"。
         */
        HIRAGANA,

        /**
         * Unicode 脚本 "Katakana"。
         */
        KATAKANA,


                    /**
         * Unicode 脚本 "Bopomofo"。
         */
        BOPOMOFO,

        /**
         * Unicode 脚本 "Han"。
         */
        HAN,

        /**
         * Unicode 脚本 "Yi"。
         */
        YI,

        /**
         * Unicode 脚本 "Old_Italic"。
         */
        OLD_ITALIC,

        /**
         * Unicode 脚本 "Gothic"。
         */
        GOTHIC,

        /**
         * Unicode 脚本 "Deseret"。
         */
        DESERET,

        /**
         * Unicode 脚本 "Inherited"。
         */
        INHERITED,

        /**
         * Unicode 脚本 "Tagalog"。
         */
        TAGALOG,

        /**
         * Unicode 脚本 "Hanunoo"。
         */
        HANUNOO,

        /**
         * Unicode 脚本 "Buhid"。
         */
        BUHID,

        /**
         * Unicode 脚本 "Tagbanwa"。
         */
        TAGBANWA,

        /**
         * Unicode 脚本 "Limbu"。
         */
        LIMBU,

        /**
         * Unicode 脚本 "Tai_Le"。
         */
        TAI_LE,

        /**
         * Unicode 脚本 "Linear_B"。
         */
        LINEAR_B,

        /**
         * Unicode 脚本 "Ugaritic"。
         */
        UGARITIC,

        /**
         * Unicode 脚本 "Shavian"。
         */
        SHAVIAN,

        /**
         * Unicode 脚本 "Osmanya"。
         */
        OSMANYA,

        /**
         * Unicode 脚本 "Cypriot"。
         */
        CYPRIOT,

        /**
         * Unicode 脚本 "Braille"。
         */
        BRAILLE,

        /**
         * Unicode 脚本 "Buginese"。
         */
        BUGINESE,

        /**
         * Unicode 脚本 "Coptic"。
         */
        COPTIC,

        /**
         * Unicode 脚本 "New_Tai_Lue"。
         */
        NEW_TAI_LUE,

        /**
         * Unicode 脚本 "Glagolitic"。
         */
        GLAGOLITIC,

        /**
         * Unicode 脚本 "Tifinagh"。
         */
        TIFINAGH,

        /**
         * Unicode 脚本 "Syloti_Nagri"。
         */
        SYLOTI_NAGRI,

        /**
         * Unicode 脚本 "Old_Persian"。
         */
        OLD_PERSIAN,

        /**
         * Unicode 脚本 "Kharoshthi"。
         */
        KHAROSHTHI,

        /**
         * Unicode 脚本 "Balinese"。
         */
        BALINESE,

        /**
         * Unicode 脚本 "Cuneiform"。
         */
        CUNEIFORM,

        /**
         * Unicode 脚本 "Phoenician"。
         */
        PHOENICIAN,

        /**
         * Unicode 脚本 "Phags_Pa"。
         */
        PHAGS_PA,

        /**
         * Unicode 脚本 "Nko"。
         */
        NKO,

        /**
         * Unicode 脚本 "Sundanese"。
         */
        SUNDANESE,

        /**
         * Unicode 脚本 "Batak"。
         */
        BATAK,

        /**
         * Unicode 脚本 "Lepcha"。
         */
        LEPCHA,

        /**
         * Unicode 脚本 "Ol_Chiki"。
         */
        OL_CHIKI,

        /**
         * Unicode 脚本 "Vai"。
         */
        VAI,

        /**
         * Unicode 脚本 "Saurashtra"。
         */
        SAURASHTRA,

        /**
         * Unicode 脚本 "Kayah_Li"。
         */
        KAYAH_LI,

        /**
         * Unicode 脚本 "Rejang"。
         */
        REJANG,

        /**
         * Unicode 脚本 "Lycian"。
         */
        LYCIAN,

        /**
         * Unicode 脚本 "Carian"。
         */
        CARIAN,

        /**
         * Unicode 脚本 "Lydian"。
         */
        LYDIAN,

        /**
         * Unicode 脚本 "Cham"。
         */
        CHAM,

        /**
         * Unicode 脚本 "Tai_Tham"。
         */
        TAI_THAM,

        /**
         * Unicode 脚本 "Tai_Viet"。
         */
        TAI_VIET,

        /**
         * Unicode 脚本 "Avestan"。
         */
        AVESTAN,

        /**
         * Unicode 脚本 "Egyptian_Hieroglyphs"。
         */
        EGYPTIAN_HIEROGLYPHS,

        /**
         * Unicode 脚本 "Samaritan"。
         */
        SAMARITAN,

        /**
         * Unicode 脚本 "Mandaic"。
         */
        MANDAIC,

        /**
         * Unicode 脚本 "Lisu"。
         */
        LISU,

        /**
         * Unicode 脚本 "Bamum"。
         */
        BAMUM,

        /**
         * Unicode 脚本 "Javanese"。
         */
        JAVANESE,

        /**
         * Unicode 脚本 "Meetei_Mayek"。
         */
        MEETEI_MAYEK,

        /**
         * Unicode 脚本 "Imperial_Aramaic"。
         */
        IMPERIAL_ARAMAIC,

        /**
         * Unicode 脚本 "Old_South_Arabian"。
         */
        OLD_SOUTH_ARABIAN,

        /**
         * Unicode 脚本 "Inscriptional_Parthian"。
         */
        INSCRIPTIONAL_PARTHIAN,

        /**
         * Unicode 脚本 "Inscriptional_Pahlavi"。
         */
        INSCRIPTIONAL_PAHLAVI,

        /**
         * Unicode 脚本 "Old_Turkic"。
         */
        OLD_TURKIC,

        /**
         * Unicode 脚本 "Brahmi"。
         */
        BRAHMI,

        /**
         * Unicode 脚本 "Kaithi"。
         */
        KAITHI,

        /**
         * Unicode 脚本 "Meroitic Hieroglyphs"。
         */
        MEROITIC_HIEROGLYPHS,

        /**
         * Unicode 脚本 "Meroitic Cursive"。
         */
        MEROITIC_CURSIVE,

        /**
         * Unicode 脚本 "Sora Sompeng"。
         */
        SORA_SOMPENG,

        /**
         * Unicode 脚本 "Chakma"。
         */
        CHAKMA,

        /**
         * Unicode 脚本 "Sharada"。
         */
        SHARADA,

        /**
         * Unicode 脚本 "Takri"。
         */
        TAKRI,

        /**
         * Unicode 脚本 "Miao"。
         */
        MIAO,

        /**
         * Unicode 脚本 "Unknown"。
         */
        UNKNOWN;

        private static final int[] scriptStarts = {
            0x0000,   // 0000..0040; COMMON
            0x0041,   // 0041..005A; LATIN
            0x005B,   // 005B..0060; COMMON
            0x0061,   // 0061..007A; LATIN
            0x007B,   // 007B..00A9; COMMON
            0x00AA,   // 00AA..00AA; LATIN
            0x00AB,   // 00AB..00B9; COMMON
            0x00BA,   // 00BA..00BA; LATIN
            0x00BB,   // 00BB..00BF; COMMON
            0x00C0,   // 00C0..00D6; LATIN
            0x00D7,   // 00D7..00D7; COMMON
            0x00D8,   // 00D8..00F6; LATIN
            0x00F7,   // 00F7..00F7; COMMON
            0x00F8,   // 00F8..02B8; LATIN
            0x02B9,   // 02B9..02DF; COMMON
            0x02E0,   // 02E0..02E4; LATIN
            0x02E5,   // 02E5..02E9; COMMON
            0x02EA,   // 02EA..02EB; BOPOMOFO
            0x02EC,   // 02EC..02FF; COMMON
            0x0300,   // 0300..036F; INHERITED
            0x0370,   // 0370..0373; GREEK
            0x0374,   // 0374..0374; COMMON
            0x0375,   // 0375..037D; GREEK
            0x037E,   // 037E..0383; COMMON
            0x0384,   // 0384..0384; GREEK
            0x0385,   // 0385..0385; COMMON
            0x0386,   // 0386..0386; GREEK
            0x0387,   // 0387..0387; COMMON
            0x0388,   // 0388..03E1; GREEK
            0x03E2,   // 03E2..03EF; COPTIC
            0x03F0,   // 03F0..03FF; GREEK
            0x0400,   // 0400..0484; CYRILLIC
            0x0485,   // 0485..0486; INHERITED
            0x0487,   // 0487..0530; CYRILLIC
            0x0531,   // 0531..0588; ARMENIAN
            0x0589,   // 0589..0589; COMMON
            0x058A,   // 058A..0590; ARMENIAN
            0x0591,   // 0591..05FF; HEBREW
            0x0600,   // 0600..060B; ARABIC
            0x060C,   // 060C..060C; COMMON
            0x060D,   // 060D..061A; ARABIC
            0x061B,   // 061B..061D; COMMON
            0x061E,   // 061E..061E; ARABIC
            0x061F,   // 061F..061F; COMMON
            0x0620,   // 0620..063F; ARABIC
            0x0640,   // 0640..0640; COMMON
            0x0641,   // 0641..064A; ARABIC
            0x064B,   // 064B..0655; INHERITED
            0x0656,   // 0656..065F; ARABIC
            0x0660,   // 0660..0669; COMMON
            0x066A,   // 066A..066F; ARABIC
            0x0670,   // 0670..0670; INHERITED
            0x0671,   // 0671..06DC; ARABIC
            0x06DD,   // 06DD..06DD; COMMON
            0x06DE,   // 06DE..06FF; ARABIC
            0x0700,   // 0700..074F; SYRIAC
            0x0750,   // 0750..077F; ARABIC
            0x0780,   // 0780..07BF; THAANA
            0x07C0,   // 07C0..07FF; NKO
            0x0800,   // 0800..083F; SAMARITAN
            0x0840,   // 0840..089F; MANDAIC
            0x08A0,   // 08A0..08FF; ARABIC
            0x0900,   // 0900..0950; DEVANAGARI
            0x0951,   // 0951..0952; INHERITED
            0x0953,   // 0953..0963; DEVANAGARI
            0x0964,   // 0964..0965; COMMON
            0x0966,   // 0966..0980; DEVANAGARI
            0x0981,   // 0981..0A00; BENGALI
            0x0A01,   // 0A01..0A80; GURMUKHI
            0x0A81,   // 0A81..0B00; GUJARATI
            0x0B01,   // 0B01..0B81; ORIYA
            0x0B82,   // 0B82..0C00; TAMIL
            0x0C01,   // 0C01..0C81; TELUGU
            0x0C82,   // 0C82..0CF0; KANNADA
            0x0D02,   // 0D02..0D81; MALAYALAM
            0x0D82,   // 0D82..0E00; SINHALA
            0x0E01,   // 0E01..0E3E; THAI
            0x0E3F,   // 0E3F..0E3F; COMMON
            0x0E40,   // 0E40..0E80; THAI
            0x0E81,   // 0E81..0EFF; LAO
            0x0F00,   // 0F00..0FD4; TIBETAN
            0x0FD5,   // 0FD5..0FD8; COMMON
            0x0FD9,   // 0FD9..0FFF; TIBETAN
            0x1000,   // 1000..109F; MYANMAR
            0x10A0,   // 10A0..10FA; GEORGIAN
            0x10FB,   // 10FB..10FB; COMMON
            0x10FC,   // 10FC..10FF; GEORGIAN
            0x1100,   // 1100..11FF; HANGUL
            0x1200,   // 1200..139F; ETHIOPIC
            0x13A0,   // 13A0..13FF; CHEROKEE
            0x1400,   // 1400..167F; CANADIAN_ABORIGINAL
            0x1680,   // 1680..169F; OGHAM
            0x16A0,   // 16A0..16EA; RUNIC
            0x16EB,   // 16EB..16ED; COMMON
            0x16EE,   // 16EE..16FF; RUNIC
            0x1700,   // 1700..171F; TAGALOG
            0x1720,   // 1720..1734; HANUNOO
            0x1735,   // 1735..173F; COMMON
            0x1740,   // 1740..175F; BUHID
            0x1760,   // 1760..177F; TAGBANWA
            0x1780,   // 1780..17FF; KHMER
            0x1800,   // 1800..1801; MONGOLIAN
            0x1802,   // 1802..1803; COMMON
            0x1804,   // 1804..1804; MONGOLIAN
            0x1805,   // 1805..1805; COMMON
            0x1806,   // 1806..18AF; MONGOLIAN
            0x18B0,   // 18B0..18FF; CANADIAN_ABORIGINAL
            0x1900,   // 1900..194F; LIMBU
            0x1950,   // 1950..197F; TAI_LE
            0x1980,   // 1980..19DF; NEW_TAI_LUE
            0x19E0,   // 19E0..19FF; KHMER
            0x1A00,   // 1A00..1A1F; BUGINESE
            0x1A20,   // 1A20..1AFF; TAI_THAM
            0x1B00,   // 1B00..1B7F; BALINESE
            0x1B80,   // 1B80..1BBF; SUNDANESE
            0x1BC0,   // 1BC0..1BFF; BATAK
            0x1C00,   // 1C00..1C4F; LEPCHA
            0x1C50,   // 1C50..1CBF; OL_CHIKI
            0x1CC0,   // 1CC0..1CCF; SUNDANESE
            0x1CD0,   // 1CD0..1CD2; INHERITED
            0x1CD3,   // 1CD3..1CD3; COMMON
            0x1CD4,   // 1CD4..1CE0; INHERITED
            0x1CE1,   // 1CE1..1CE1; COMMON
            0x1CE2,   // 1CE2..1CE8; INHERITED
            0x1CE9,   // 1CE9..1CEC; COMMON
            0x1CED,   // 1CED..1CED; INHERITED
            0x1CEE,   // 1CEE..1CF3; COMMON
            0x1CF4,   // 1CF4..1CF4; INHERITED
            0x1CF5,   // 1CF5..1CFF; COMMON
            0x1D00,   // 1D00..1D25; LATIN
            0x1D26,   // 1D26..1D2A; GREEK
            0x1D2B,   // 1D2B..1D2B; CYRILLIC
            0x1D2C,   // 1D2C..1D5C; LATIN
            0x1D5D,   // 1D5D..1D61; GREEK
            0x1D62,   // 1D62..1D65; LATIN
            0x1D66,   // 1D66..1D6A; GREEK
            0x1D6B,   // 1D6B..1D77; LATIN
            0x1D78,   // 1D78..1D78; CYRILLIC
            0x1D79,   // 1D79..1DBE; LATIN
            0x1DBF,   // 1DBF..1DBF; GREEK
            0x1DC0,   // 1DC0..1DFF; INHERITED
            0x1E00,   // 1E00..1EFF; LATIN
            0x1F00,   // 1F00..1FFF; GREEK
            0x2000,   // 2000..200B; COMMON
            0x200C,   // 200C..200D; INHERITED
            0x200E,   // 200E..2070; COMMON
            0x2071,   // 2071..2073; LATIN
            0x2074,   // 2074..207E; COMMON
            0x207F,   // 207F..207F; LATIN
            0x2080,   // 2080..208F; COMMON
            0x2090,   // 2090..209F; LATIN
            0x20A0,   // 20A0..20CF; COMMON
            0x20D0,   // 20D0..20FF; INHERITED
            0x2100,   // 2100..2125; COMMON
            0x2126,   // 2126..2126; GREEK
            0x2127,   // 2127..2129; COMMON
            0x212A,   // 212A..212B; LATIN
            0x212C,   // 212C..2131; COMMON
            0x2132,   // 2132..2132; LATIN
            0x2133,   // 2133..214D; COMMON
            0x214E,   // 214E..214E; LATIN
            0x214F,   // 214F..215F; COMMON
            0x2160,   // 2160..2188; LATIN
            0x2189,   // 2189..27FF; COMMON
            0x2800,   // 2800..28FF; BRAILLE
            0x2900,   // 2900..2BFF; COMMON
            0x2C00,   // 2C00..2C5F; GLAGOLITIC
            0x2C60,   // 2C60..2C7F; LATIN
            0x2C80,   // 2C80..2CFF; COPTIC
            0x2D00,   // 2D00..2D2F; GEORGIAN
            0x2D30,   // 2D30..2D7F; TIFINAGH
            0x2D80,   // 2D80..2DDF; ETHIOPIC
            0x2DE0,   // 2DE0..2DFF; CYRILLIC
            0x2E00,   // 2E00..2E7F; COMMON
            0x2E80,   // 2E80..2FEF; HAN
            0x2FF0,   // 2FF0..3004; COMMON
            0x3005,   // 3005..3005; HAN
            0x3006,   // 3006..3006; COMMON
            0x3007,   // 3007..3007; HAN
            0x3008,   // 3008..3020; COMMON
            0x3021,   // 3021..3029; HAN
            0x302A,   // 302A..302D; INHERITED
            0x302E,   // 302E..302F; HANGUL
            0x3030,   // 3030..3037; COMMON
            0x3038,   // 3038..303B; HAN
            0x303C,   // 303C..3040; COMMON
            0x3041,   // 3041..3098; HIRAGANA
            0x3099,   // 3099..309A; INHERITED
            0x309B,   // 309B..309C; COMMON
            0x309D,   // 309D..309F; HIRAGANA
            0x30A0,   // 30A0..30A0; COMMON
            0x30A1,   // 30A1..30FA; KATAKANA
            0x30FB,   // 30FB..30FC; COMMON
            0x30FD,   // 30FD..3104; KATAKANA
            0x3105,   // 3105..3130; BOPOMOFO
            0x3131,   // 3131..318F; HANGUL
            0x3190,   // 3190..319F; COMMON
            0x31A0,   // 31A0..31BF; BOPOMOFO
            0x31C0,   // 31C0..31EF; COMMON
            0x31F0,   // 31F0..31FF; KATAKANA
            0x3200,   // 3200..321F; HANGUL
            0x3220,   // 3220..325F; COMMON
            0x3260,   // 3260..327E; HANGUL
            0x327F,   // 327F..32CF; COMMON
            0x32D0,   // 32D0..32FE; KATAKANA
            0x32FF,   // 32FF      ; COMMON
            0x3300,   // 3300..3357; KATAKANA
            0x3358,   // 3358..33FF; COMMON
            0x3400,   // 3400..4DBF; HAN
            0x4DC0,   // 4DC0..4DFF; COMMON
            0x4E00,   // 4E00..9FFF; HAN
            0xA000,   // A000..A4CF; YI
            0xA4D0,   // A4D0..A4FF; LISU
            0xA500,   // A500..A63F; VAI
            0xA640,   // A640..A69F; CYRILLIC
            0xA6A0,   // A6A0..A6FF; BAMUM
            0xA700,   // A700..A721; COMMON
            0xA722,   // A722..A787; LATIN
            0xA788,   // A788..A78A; COMMON
            0xA78B,   // A78B..A7FF; LATIN
            0xA800,   // A800..A82F; SYLOTI_NAGRI
            0xA830,   // A830..A83F; COMMON
            0xA840,   // A840..A87F; PHAGS_PA
            0xA880,   // A880..A8DF; SAURASHTRA
            0xA8E0,   // A8E0..A8FF; DEVANAGARI
            0xA900,   // A900..A92F; KAYAH_LI
            0xA930,   // A930..A95F; REJANG
            0xA960,   // A960..A97F; HANGUL
            0xA980,   // A980..A9FF; JAVANESE
            0xAA00,   // AA00..AA5F; CHAM
            0xAA60,   // AA60..AA7F; MYANMAR
            0xAA80,   // AA80..AADF; TAI_VIET
            0xAAE0,   // AAE0..AB00; MEETEI_MAYEK
            0xAB01,   // AB01..ABBF; ETHIOPIC
            0xABC0,   // ABC0..ABFF; MEETEI_MAYEK
            0xAC00,   // AC00..D7FB; HANGUL
            0xD7FC,   // D7FC..F8FF; UNKNOWN
            0xF900,   // F900..FAFF; HAN
            0xFB00,   // FB00..FB12; LATIN
            0xFB13,   // FB13..FB1C; ARMENIAN
            0xFB1D,   // FB1D..FB4F; HEBREW
            0xFB50,   // FB50..FD3D; ARABIC
            0xFD3E,   // FD3E..FD4F; COMMON
            0xFD50,   // FD50..FDFC; ARABIC
            0xFDFD,   // FDFD..FDFF; COMMON
            0xFE00,   // FE00..FE0F; INHERITED
            0xFE10,   // FE10..FE1F; COMMON
            0xFE20,   // FE20..FE2F; INHERITED
            0xFE30,   // FE30..FE6F; COMMON
            0xFE70,   // FE70..FEFE; ARABIC
            0xFEFF,   // FEFF..FF20; COMMON
            0xFF21,   // FF21..FF3A; LATIN
            0xFF3B,   // FF3B..FF40; COMMON
            0xFF41,   // FF41..FF5A; LATIN
            0xFF5B,   // FF5B..FF65; COMMON
            0xFF66,   // FF66..FF6F; KATAKANA
            0xFF70,   // FF70..FF70; COMMON
            0xFF71,   // FF71..FF9D; KATAKANA
            0xFF9E,   // FF9E..FF9F; COMMON
            0xFFA0,   // FFA0..FFDF; HANGUL
            0xFFE0,   // FFE0..FFFF; COMMON
            0x10000,  // 10000..100FF; LINEAR_B
            0x10100,  // 10100..1013F; COMMON
            0x10140,  // 10140..1018F; GREEK
            0x10190,  // 10190..101FC; COMMON
            0x101FD,  // 101FD..1027F; INHERITED
            0x10280,  // 10280..1029F; LYCIAN
            0x102A0,  // 102A0..102FF; CARIAN
            0x10300,  // 10300..1032F; OLD_ITALIC
            0x10330,  // 10330..1037F; GOTHIC
            0x10380,  // 10380..1039F; UGARITIC
            0x103A0,  // 103A0..103FF; OLD_PERSIAN
            0x10400,  // 10400..1044F; DESERET
            0x10450,  // 10450..1047F; SHAVIAN
            0x10480,  // 10480..107FF; OSMANYA
            0x10800,  // 10800..1083F; CYPRIOT
            0x10840,  // 10840..108FF; IMPERIAL_ARAMAIC
            0x10900,  // 10900..1091F; PHOENICIAN
            0x10920,  // 10920..1097F; LYDIAN
            0x10980,  // 10980..1099F; MEROITIC_HIEROGLYPHS
            0x109A0,  // 109A0..109FF; MEROITIC_CURSIVE
            0x10A00,  // 10A00..10A5F; KHAROSHTHI
            0x10A60,  // 10A60..10AFF; OLD_SOUTH_ARABIAN
            0x10B00,  // 10B00..10B3F; AVESTAN
            0x10B40,  // 10B40..10B5F; INSCRIPTIONAL_PARTHIAN
            0x10B60,  // 10B60..10BFF; INSCRIPTIONAL_PAHLAVI
            0x10C00,  // 10C00..10E5F; OLD_TURKIC
            0x10E60,  // 10E60..10FFF; ARABIC
            0x11000,  // 11000..1107F; BRAHMI
            0x11080,  // 11080..110CF; KAITHI
            0x110D0,  // 110D0..110FF; SORA_SOMPENG
            0x11100,  // 11100..1117F; CHAKMA
            0x11180,  // 11180..1167F; SHARADA
            0x11680,  // 11680..116CF; TAKRI
            0x12000,  // 12000..12FFF; CUNEIFORM
            0x13000,  // 13000..167FF; EGYPTIAN_HIEROGLYPHS
            0x16800,  // 16800..16A38; BAMUM
            0x16F00,  // 16F00..16F9F; MIAO
            0x1B000,  // 1B000..1B000; KATAKANA
            0x1B001,  // 1B001..1CFFF; HIRAGANA
            0x1D000,  // 1D000..1D166; COMMON
            0x1D167,  // 1D167..1D169; INHERITED
            0x1D16A,  // 1D16A..1D17A; COMMON
            0x1D17B,  // 1D17B..1D182; INHERITED
            0x1D183,  // 1D183..1D184; COMMON
            0x1D185,  // 1D185..1D18B; INHERITED
            0x1D18C,  // 1D18C..1D1A9; COMMON
            0x1D1AA,  // 1D1AA..1D1AD; INHERITED
            0x1D1AE,  // 1D1AE..1D1FF; COMMON
            0x1D200,  // 1D200..1D2FF; GREEK
            0x1D300,  // 1D300..1EDFF; COMMON
            0x1EE00,  // 1EE00..1EFFF; ARABIC
            0x1F000,  // 1F000..1F1FF; COMMON
            0x1F200,  // 1F200..1F200; HIRAGANA
            0x1F201,  // 1F210..1FFFF; COMMON
            0x20000,  // 20000..E0000; HAN
            0xE0001,  // E0001..E00FF; COMMON
            0xE0100,  // E0100..E01EF; INHERITED
            0xE01F0   // E01F0..10FFFF; UNKNOWN


        };

        private static final UnicodeScript[] scripts = {
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            BOPOMOFO,
            COMMON,
            INHERITED,
            GREEK,
            COMMON,
            GREEK,
            COMMON,
            GREEK,
            COMMON,
            GREEK,
            COMMON,
            GREEK,
            COPTIC,
            GREEK,
            CYRILLIC,
            INHERITED,
            CYRILLIC,
            ARMENIAN,
            COMMON,
            ARMENIAN,
            HEBREW,
            ARABIC,
            COMMON,
            ARABIC,
            COMMON,
            ARABIC,
            COMMON,
            ARABIC,
            COMMON,
            ARABIC,
            INHERITED,
            ARABIC,
            COMMON,
            ARABIC,
            INHERITED,
            ARABIC,
            COMMON,
            ARABIC,
            SYRIAC,
            ARABIC,
            THAANA,
            NKO,
            SAMARITAN,
            MANDAIC,
            ARABIC,
            DEVANAGARI,
            INHERITED,
            DEVANAGARI,
            COMMON,
            DEVANAGARI,
            BENGALI,
            GURMUKHI,
            GUJARATI,
            ORIYA,
            TAMIL,
            TELUGU,
            KANNADA,
            MALAYALAM,
            SINHALA,
            THAI,
            COMMON,
            THAI,
            LAO,
            TIBETAN,
            COMMON,
            TIBETAN,
            MYANMAR,
            GEORGIAN,
            COMMON,
            GEORGIAN,
            HANGUL,
            ETHIOPIC,
            CHEROKEE,
            CANADIAN_ABORIGINAL,
            OGHAM,
            RUNIC,
            COMMON,
            RUNIC,
            TAGALOG,
            HANUNOO,
            COMMON,
            BUHID,
            TAGBANWA,
            KHMER,
            MONGOLIAN,
            COMMON,
            MONGOLIAN,
            COMMON,
            MONGOLIAN,
            CANADIAN_ABORIGINAL,
            LIMBU,
            TAI_LE,
            NEW_TAI_LUE,
            KHMER,
            BUGINESE,
            TAI_THAM,
            BALINESE,
            SUNDANESE,
            BATAK,
            LEPCHA,
            OL_CHIKI,
            SUNDANESE,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            LATIN,
            GREEK,
            CYRILLIC,
            LATIN,
            GREEK,
            LATIN,
            GREEK,
            LATIN,
            CYRILLIC,
            LATIN,
            GREEK,
            INHERITED,
            LATIN,
            GREEK,
            COMMON,
            INHERITED,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            INHERITED,
            COMMON,
            GREEK,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            BRAILLE,
            COMMON,
            GLAGOLITIC,
            LATIN,
            COPTIC,
            GEORGIAN,
            TIFINAGH,
            ETHIOPIC,
            CYRILLIC,
            COMMON,
            HAN,
            COMMON,
            HAN,
            COMMON,
            HAN,
            COMMON,
            HAN,
            INHERITED,
            HANGUL,
            COMMON,
            HAN,
            COMMON,
            HIRAGANA,
            INHERITED,
            COMMON,
            HIRAGANA,
            COMMON,
            KATAKANA,
            COMMON,
            KATAKANA,
            BOPOMOFO,
            HANGUL,
            COMMON,
            BOPOMOFO,
            COMMON,
            KATAKANA,
            HANGUL,
            COMMON,
            HANGUL,
            COMMON,
            KATAKANA,  // 32D0..32FE
            COMMON,    // 32FF
            KATAKANA,  // 3300..3357
            COMMON,
            HAN,
            COMMON,
            HAN,
            YI,
            LISU,
            VAI,
            CYRILLIC,
            BAMUM,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            SYLOTI_NAGRI,
            COMMON,
            PHAGS_PA,
            SAURASHTRA,
            DEVANAGARI,
            KAYAH_LI,
            REJANG,
            HANGUL,
            JAVANESE,
            CHAM,
            MYANMAR,
            TAI_VIET,
            MEETEI_MAYEK,
            ETHIOPIC,
            MEETEI_MAYEK,
            HANGUL,
            UNKNOWN     ,
            HAN,
            LATIN,
            ARMENIAN,
            HEBREW,
            ARABIC,
            COMMON,
            ARABIC,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            ARABIC,
            COMMON,
            LATIN,
            COMMON,
            LATIN,
            COMMON,
            KATAKANA,
            COMMON,
            KATAKANA,
            COMMON,
            HANGUL,
            COMMON,
            LINEAR_B,
            COMMON,
            GREEK,
            COMMON,
            INHERITED,
            LYCIAN,
            CARIAN,
            OLD_ITALIC,
            GOTHIC,
            UGARITIC,
            OLD_PERSIAN,
            DESERET,
            SHAVIAN,
            OSMANYA,
            CYPRIOT,
            IMPERIAL_ARAMAIC,
            PHOENICIAN,
            LYDIAN,
            MEROITIC_HIEROGLYPHS,
            MEROITIC_CURSIVE,
            KHAROSHTHI,
            OLD_SOUTH_ARABIAN,
            AVESTAN,
            INSCRIPTIONAL_PARTHIAN,
            INSCRIPTIONAL_PAHLAVI,
            OLD_TURKIC,
            ARABIC,
            BRAHMI,
            KAITHI,
            SORA_SOMPENG,
            CHAKMA,
            SHARADA,
            TAKRI,
            CUNEIFORM,
            EGYPTIAN_HIEROGLYPHS,
            BAMUM,
            MIAO,
            KATAKANA,
            HIRAGANA,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            INHERITED,
            COMMON,
            GREEK,
            COMMON,
            ARABIC,
            COMMON,
            HIRAGANA,
            COMMON,
            HAN,
            COMMON,
            INHERITED,
            UNKNOWN
        };

        private static final HashMap<String, Character.UnicodeScript> aliases;
        static {
            aliases = new HashMap<>(128);
            aliases.put("ARAB", ARABIC);
            aliases.put("ARMI", IMPERIAL_ARAMAIC);
            aliases.put("ARMN", ARMENIAN);
            aliases.put("AVST", AVESTAN);
            aliases.put("BALI", BALINESE);
            aliases.put("BAMU", BAMUM);
            aliases.put("BATK", BATAK);
            aliases.put("BENG", BENGALI);
            aliases.put("BOPO", BOPOMOFO);
            aliases.put("BRAI", BRAILLE);
            aliases.put("BRAH", BRAHMI);
            aliases.put("BUGI", BUGINESE);
            aliases.put("BUHD", BUHID);
            aliases.put("CAKM", CHAKMA);
            aliases.put("CANS", CANADIAN_ABORIGINAL);
            aliases.put("CARI", CARIAN);
            aliases.put("CHAM", CHAM);
            aliases.put("CHER", CHEROKEE);
            aliases.put("COPT", COPTIC);
            aliases.put("CPRT", CYPRIOT);
            aliases.put("CYRL", CYRILLIC);
            aliases.put("DEVA", DEVANAGARI);
            aliases.put("DSRT", DESERET);
            aliases.put("EGYP", EGYPTIAN_HIEROGLYPHS);
            aliases.put("ETHI", ETHIOPIC);
            aliases.put("GEOR", GEORGIAN);
            aliases.put("GLAG", GLAGOLITIC);
            aliases.put("GOTH", GOTHIC);
            aliases.put("GREK", GREEK);
            aliases.put("GUJR", GUJARATI);
            aliases.put("GURU", GURMUKHI);
            aliases.put("HANG", HANGUL);
            aliases.put("HANI", HAN);
            aliases.put("HANO", HANUNOO);
            aliases.put("HEBR", HEBREW);
            aliases.put("HIRA", HIRAGANA);
            // 看起来我们没有 KATAKANA_OR_HIRAGANA
            //aliases.put("HRKT", KATAKANA_OR_HIRAGANA);
            aliases.put("ITAL", OLD_ITALIC);
            aliases.put("JAVA", JAVANESE);
            aliases.put("KALI", KAYAH_LI);
            aliases.put("KANA", KATAKANA);
            aliases.put("KHAR", KHAROSHTHI);
            aliases.put("KHMR", KHMER);
            aliases.put("KNDA", KANNADA);
            aliases.put("KTHI", KAITHI);
            aliases.put("LANA", TAI_THAM);
            aliases.put("LAOO", LAO);
            aliases.put("LATN", LATIN);
            aliases.put("LEPC", LEPCHA);
            aliases.put("LIMB", LIMBU);
            aliases.put("LINB", LINEAR_B);
            aliases.put("LISU", LISU);
            aliases.put("LYCI", LYCIAN);
            aliases.put("LYDI", LYDIAN);
            aliases.put("MAND", MANDAIC);
            aliases.put("MERC", MEROITIC_CURSIVE);
            aliases.put("MERO", MEROITIC_HIEROGLYPHS);
            aliases.put("MLYM", MALAYALAM);
            aliases.put("MONG", MONGOLIAN);
            aliases.put("MTEI", MEETEI_MAYEK);
            aliases.put("MYMR", MYANMAR);
            aliases.put("NKOO", NKO);
            aliases.put("OGAM", OGHAM);
            aliases.put("OLCK", OL_CHIKI);
            aliases.put("ORKH", OLD_TURKIC);
            aliases.put("ORYA", ORIYA);
            aliases.put("OSMA", OSMANYA);
            aliases.put("PHAG", PHAGS_PA);
            aliases.put("PLRD", MIAO);
            aliases.put("PHLI", INSCRIPTIONAL_PAHLAVI);
            aliases.put("PHNX", PHOENICIAN);
            aliases.put("PRTI", INSCRIPTIONAL_PARTHIAN);
            aliases.put("RJNG", REJANG);
            aliases.put("RUNR", RUNIC);
            aliases.put("SAMR", SAMARITAN);
            aliases.put("SARB", OLD_SOUTH_ARABIAN);
            aliases.put("SAUR", SAURASHTRA);
            aliases.put("SHAW", SHAVIAN);
            aliases.put("SHRD", SHARADA);
            aliases.put("SINH", SINHALA);
            aliases.put("SORA", SORA_SOMPENG);
            aliases.put("SUND", SUNDANESE);
            aliases.put("SYLO", SYLOTI_NAGRI);
            aliases.put("SYRC", SYRIAC);
            aliases.put("TAGB", TAGBANWA);
            aliases.put("TALE", TAI_LE);
            aliases.put("TAKR", TAKRI);
            aliases.put("TALU", NEW_TAI_LUE);
            aliases.put("TAML", TAMIL);
            aliases.put("TAVT", TAI_VIET);
            aliases.put("TELU", TELUGU);
            aliases.put("TFNG", TIFINAGH);
            aliases.put("TGLG", TAGALOG);
            aliases.put("THAA", THAANA);
            aliases.put("THAI", THAI);
            aliases.put("TIBT", TIBETAN);
            aliases.put("UGAR", UGARITIC);
            aliases.put("VAII", VAI);
            aliases.put("XPEO", OLD_PERSIAN);
            aliases.put("XSUX", CUNEIFORM);
            aliases.put("YIII", YI);
            aliases.put("ZINH", INHERITED);
            aliases.put("ZYYY", COMMON);
            aliases.put("ZZZZ", UNKNOWN);
        }

        /**
         * 返回给定字符（Unicode 代码点）所属的 Unicode 脚本的枚举常量。
         *
         * @param   codePoint 要查询的字符（Unicode 代码点）。
         * @return  表示此字符所属的 Unicode 脚本的 {@code UnicodeScript} 常量。
         *
         * @exception IllegalArgumentException 如果指定的 {@code codePoint} 是无效的 Unicode 代码点。
         * @see Character#isValidCodePoint(int)
         *
         */
        public static UnicodeScript of(int codePoint) {
            if (!isValidCodePoint(codePoint))
                throw new IllegalArgumentException();
            int type = getType(codePoint);
            // 保留 SURROGATE 和 PRIVATE_USE 以供表查找
            if (type == UNASSIGNED)
                return UNKNOWN;
            int index = Arrays.binarySearch(scriptStarts, codePoint);
            if (index < 0)
                index = -index - 2;
            return scripts[index];
        }

        /**
         * 返回具有给定 Unicode 脚本名称或脚本名称别名的 UnicodeScript 常量。脚本名称及其别名由 Unicode 标准确定。
         * 文件 Scripts&lt;version&gt;.txt 和 PropertyValueAliases&lt;version&gt;.txt 定义了特定版本标准的脚本名称和脚本名称别名。
         * {@link Character} 类指定了它支持的标准版本。
         * <p>
         * 所有有效的脚本名称的大小写都被忽略。使用 en_US 语言环境的大小写映射规则为脚本名称验证提供不区分大小写的字符串比较。
         * <p>
         *
         * @param scriptName 一个 {@code UnicodeScript} 名称。
         * @return 由 {@code scriptName} 标识的 {@code UnicodeScript} 常量
         * @throws IllegalArgumentException 如果 {@code scriptName} 是无效名称
         * @throws NullPointerException 如果 {@code scriptName} 为 null
         */
        public static final UnicodeScript forName(String scriptName) {
            scriptName = scriptName.toUpperCase(Locale.ENGLISH);
                                 //.replace(' ', '_'));
            UnicodeScript sc = aliases.get(scriptName);
            if (sc != null)
                return sc;
            return valueOf(scriptName);
        }
    }

    /**
     * {@code Character} 的值。
     *
     * @serial
     */
    private final char value;

    /** 为了互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = 3786198910865385080L;

    /**
     * 构造一个新的分配的 {@code Character} 对象，表示指定的 {@code char} 值。
     *
     * @param  value   要由 {@code Character} 对象表示的值。
     */
    public Character(char value) {
        this.value = value;
    }

    private static class CharacterCache {
        private CharacterCache(){}

        static final Character cache[] = new Character[127 + 1];

        static {
            for (int i = 0; i < cache.length; i++)
                cache[i] = new Character((char)i);
        }
    }

    /**
     * 返回表示指定 <tt>char</tt> 值的 <tt>Character</tt> 实例。
     * 如果不需要新的 <tt>Character</tt> 实例，通常应优先使用此方法而不是构造函数
     * {@link #Character(char)}，因为此方法通过缓存经常请求的值，可能会显著提高空间和时间性能。
     *
     * 此方法将始终缓存值在 {@code
     * '\u005Cu0000'} 到 {@code '\u005Cu007F'} 范围内（包括），并且可能会缓存此范围之外的其他值。
     *
     * @param  c 一个 char 值。
     * @return 一个表示 <tt>c</tt> 的 <tt>Character</tt> 实例。
     * @since  1.5
     */
    public static Character valueOf(char c) {
        if (c <= 127) { // 必须缓存
            return CharacterCache.cache[(int)c];
        }
        return new Character(c);
    }


                /**
     * 返回此 {@code Character} 对象的值。
     * @return  此对象表示的原始 {@code char} 值。
     */
    public char charValue() {
        return value;
    }

    /**
     * 返回此 {@code Character} 的哈希码；等于调用 {@code charValue()} 的结果。
     *
     * @return 此 {@code Character} 的哈希码值。
     */
    @Override
    public int hashCode() {
        return Character.hashCode(value);
    }

    /**
     * 返回一个 {@code char} 值的哈希码；与 {@code Character.hashCode()} 兼容。
     *
     * @since 1.8
     *
     * @param value 要返回哈希码的 {@code char}。
     * @return 一个 {@code char} 值的哈希码值。
     */
    public static int hashCode(char value) {
        return (int)value;
    }

    /**
     * 比较此对象与指定对象。结果为 {@code true} 当且仅当参数不为
     * {@code null} 并且是一个表示与此对象相同的 {@code char} 值的
     * {@code Character} 对象。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Character) {
            return value == ((Character)obj).charValue();
        }
        return false;
    }

    /**
     * 返回一个表示此 {@code Character} 值的 {@code String} 对象。结果是一个
     * 长度为 1 的字符串，其唯一组件是此 {@code Character} 对象表示的原始
     * {@code char} 值。
     *
     * @return 此对象的字符串表示。
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * 返回一个表示指定 {@code char} 的 {@code String} 对象。结果是一个长度
     * 为 1 的字符串，仅包含指定的 {@code char}。
     *
     * @param c 要转换的 {@code char}。
     * @return 指定 {@code char} 的字符串表示。
     * @since 1.4
     */
    public static String toString(char c) {
        return String.valueOf(c);
    }

    /**
     * 确定指定的代码点是否是有效的
     * <a href="http://www.unicode.org/glossary/#code_point">
     * Unicode 代码点值</a>。
     *
     * @param  codePoint 要测试的 Unicode 代码点。
     * @return 如果指定的代码点值在
     *         {@link #MIN_CODE_POINT} 和
     *         {@link #MAX_CODE_POINT} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @since  1.5
     */
    public static boolean isValidCodePoint(int codePoint) {
        // 优化形式：
        //     codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT
        int plane = codePoint >>> 16;
        return plane < ((MAX_CODE_POINT + 1) >>> 16);
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否在
     * <a href="#BMP">基本多文种平面 (BMP)</a> 中。
     * 这样的代码点可以用一个 {@code char} 表示。
     *
     * @param  codePoint 要测试的字符（Unicode 代码点）。
     * @return 如果指定的代码点在
     *         {@link #MIN_VALUE} 和 {@link #MAX_VALUE} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @since  1.7
     */
    public static boolean isBmpCodePoint(int codePoint) {
        return codePoint >>> 16 == 0;
        // 优化形式：
        //     codePoint >= MIN_VALUE && codePoint <= MAX_VALUE
        // 我们始终使用逻辑右移 (>>>) 以促进额外的运行时优化。
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否在
     * <a href="#supplementary">补充字符</a> 范围内。
     *
     * @param  codePoint 要测试的字符（Unicode 代码点）。
     * @return 如果指定的代码点在
     *         {@link #MIN_SUPPLEMENTARY_CODE_POINT} 和
     *         {@link #MAX_CODE_POINT} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @since  1.5
     */
    public static boolean isSupplementaryCodePoint(int codePoint) {
        return codePoint >= MIN_SUPPLEMENTARY_CODE_POINT
            && codePoint <  MAX_CODE_POINT + 1;
    }

    /**
     * 确定给定的 {@code char} 值是否是
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode 高代理码单元</a>
     * （也称为 <i>前导代理码单元</i>）。
     *
     * <p>这样的值本身不表示字符，但在 UTF-16 编码中用于表示
     * <a href="#supplementary">补充字符</a>。
     *
     * @param  ch 要测试的 {@code char} 值。
     * @return 如果 {@code char} 值在
     *         {@link #MIN_HIGH_SURROGATE} 和
     *         {@link #MAX_HIGH_SURROGATE} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @see    Character#isLowSurrogate(char)
     * @see    Character.UnicodeBlock#of(int)
     * @since  1.5
     */
    public static boolean isHighSurrogate(char ch) {
        // 帮助 VM 常量折叠；MAX_HIGH_SURROGATE + 1 == MIN_LOW_SURROGATE
        return ch >= MIN_HIGH_SURROGATE && ch < (MAX_HIGH_SURROGATE + 1);
    }

    /**
     * 确定给定的 {@code char} 值是否是
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode 低代理码单元</a>
     * （也称为 <i>尾随代理码单元</i>）。
     *
     * <p>这样的值本身不表示字符，但在 UTF-16 编码中用于表示
     * <a href="#supplementary">补充字符</a>。
     *
     * @param  ch 要测试的 {@code char} 值。
     * @return 如果 {@code char} 值在
     *         {@link #MIN_LOW_SURROGATE} 和
     *         {@link #MAX_LOW_SURROGATE} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @see    Character#isHighSurrogate(char)
     * @since  1.5
     */
    public static boolean isLowSurrogate(char ch) {
        return ch >= MIN_LOW_SURROGATE && ch < (MAX_LOW_SURROGATE + 1);
    }

    /**
     * 确定给定的 {@code char} 值是否是 Unicode
     * <i>代理码单元</i>。
     *
     * <p>这样的值本身不表示字符，但在 UTF-16 编码中用于表示
     * <a href="#supplementary">补充字符</a>。
     *
     * <p>{@code char} 值是代理码单元当且仅当它是
     * 一个 {@linkplain #isLowSurrogate(char) 低代理码单元} 或
     * 一个 {@linkplain #isHighSurrogate(char) 高代理码单元}。
     *
     * @param  ch 要测试的 {@code char} 值。
     * @return 如果 {@code char} 值在
     *         {@link #MIN_SURROGATE} 和
     *         {@link #MAX_SURROGATE} 之间（包括），则返回 {@code true}；
     *         否则返回 {@code false}。
     * @since  1.7
     */
    public static boolean isSurrogate(char ch) {
        return ch >= MIN_SURROGATE && ch < (MAX_SURROGATE + 1);
    }

    /**
     * 确定指定的 {@code char} 对是否是一个有效的
     * <a href="http://www.unicode.org/glossary/#surrogate_pair">
     * Unicode 代理对</a>。
     *
     * <p>此方法等价于以下表达式：
     * <blockquote><pre>{@code
     * isHighSurrogate(high) && isLowSurrogate(low)
     * }</pre></blockquote>
     *
     * @param  high 要测试的高代理码值
     * @param  low 要测试的低代理码值
     * @return 如果指定的高代理码值和低代理码值表示一个有效的代理对，则返回 {@code true}；
     *         否则返回 {@code false}。
     * @since  1.5
     */
    public static boolean isSurrogatePair(char high, char low) {
        return isHighSurrogate(high) && isLowSurrogate(low);
    }

    /**
     * 确定表示指定字符（Unicode 代码点）所需的 {@code char} 值的数量。如果指定的字符
     * 大于或等于 0x10000，则该方法返回 2。否则，该方法返回 1。
     *
     * <p>此方法不验证指定的字符是否为有效的 Unicode 代码点。如果需要，调用者必须使用
     * {@link #isValidCodePoint(int) isValidCodePoint} 验证字符值。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是有效的补充字符，则返回 2；否则返回 1。
     * @see     Character#isSupplementaryCodePoint(int)
     * @since   1.5
     */
    public static int charCount(int codePoint) {
        return codePoint >= MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1;
    }

    /**
     * 将指定的代理对转换为其补充代码点值。此方法不验证指定的
     * 代理对。如果需要，调用者必须使用 {@link
     * #isSurrogatePair(char, char) isSurrogatePair} 验证它。
     *
     * @param  high 高代理码单元
     * @param  low 低代理码单元
     * @return 由指定代理对组成的补充代码点。
     * @since  1.5
     */
    public static int toCodePoint(char high, char low) {
        // 优化形式：
        // return ((high - MIN_HIGH_SURROGATE) << 10)
        //         + (low - MIN_LOW_SURROGATE)
        //         + MIN_SUPPLEMENTARY_CODE_POINT;
        return ((high << 10) + low) + (MIN_SUPPLEMENTARY_CODE_POINT
                                       - (MIN_HIGH_SURROGATE << 10)
                                       - MIN_LOW_SURROGATE);
    }

    /**
     * 返回 {@code CharSequence} 中给定索引处的代码点。如果 {@code CharSequence} 中
     * 给定索引处的 {@code char} 值在高代理范围，后一个索引小于
     * {@code CharSequence} 的长度，且后一个索引处的
     * {@code char} 值在低代理范围，则返回与此代理对对应的补充代码点。
     * 否则，返回给定索引处的 {@code char} 值。
     *
     * @param seq 一个 {@code char} 值序列（Unicode 代码单元）
     * @param index 要转换的 {@code char} 值（Unicode 代码单元）在 {@code seq} 中的索引
     * @return 给定索引处的 Unicode 代码点
     * @exception NullPointerException 如果 {@code seq} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index} 值为负或不小于
     * {@link CharSequence#length() seq.length()}。
     * @since  1.5
     */
    public static int codePointAt(CharSequence seq, int index) {
        char c1 = seq.charAt(index);
        if (isHighSurrogate(c1) && ++index < seq.length()) {
            char c2 = seq.charAt(index);
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    /**
     * 返回 {@code char} 数组中给定索引处的代码点。如果 {@code char} 数组中
     * 给定索引处的 {@code char} 值在高代理范围，后一个索引小于
     * {@code char} 数组的长度，且后一个索引处的
     * {@code char} 值在低代理范围，则返回与此代理对对应的补充代码点。
     * 否则，返回给定索引处的 {@code char} 值。
     *
     * @param a {@code char} 数组
     * @param index 要转换的 {@code char} 值（Unicode 代码单元）在 {@code char} 数组中的索引
     * @return 给定索引处的 Unicode 代码点
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index} 值为负或不小于
     * {@code char} 数组的长度。
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index) {
        return codePointAtImpl(a, index, a.length);
    }

    /**
     * 返回 {@code char} 数组中给定索引处的代码点，其中仅使用索引小于
     * {@code limit} 的数组元素。如果 {@code char} 数组中
     * 给定索引处的 {@code char} 值在高代理范围，后一个索引小于
     * {@code limit}，且后一个索引处的
     * {@code char} 值在低代理范围，则返回与此代理对对应的补充代码点。
     * 否则，返回给定索引处的 {@code char} 值。
     *
     * @param a {@code char} 数组
     * @param index 要转换的 {@code char} 值（Unicode 代码单元）在 {@code char} 数组中的索引
     * @param limit {@code char} 数组中可以使用的最后一个数组元素的索引后一位
     * @return 给定索引处的 Unicode 代码点
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index}
     * 参数为负或不小于 {@code limit} 参数，或 {@code limit} 参数为负或
     * 大于 {@code char} 数组的长度。
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index, int limit) {
        if (index >= limit || limit < 0 || limit > a.length) {
            throw new IndexOutOfBoundsException();
        }
        return codePointAtImpl(a, index, limit);
    }

    // 如果索引越界，则抛出 ArrayIndexOutOfBoundsException
    static int codePointAtImpl(char[] a, int index, int limit) {
        char c1 = a[index];
        if (isHighSurrogate(c1) && ++index < limit) {
            char c2 = a[index];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    /**
     * 返回 {@code CharSequence} 中给定索引前的代码点。如果 {@code (index - 1)} 处的
     * {@code char} 值在低代理范围，{@code (index - 2)} 不为负，且
     * {@code (index - 2)} 处的 {@code char} 值在高代理范围，则返回与此代理对对应的补充代码点。
     * 否则，返回 {@code (index - 1)} 处的 {@code char} 值。
     *
     * @param seq {@code CharSequence} 实例
     * @param index 应返回的代码点后的索引
     * @return 给定索引前的 Unicode 代码点值。
     * @exception NullPointerException 如果 {@code seq} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index}
     * 参数小于 1 或大于 {@link
     * CharSequence#length() seq.length()}。
     * @since  1.5
     */
    public static int codePointBefore(CharSequence seq, int index) {
        char c2 = seq.charAt(--index);
        if (isLowSurrogate(c2) && index > 0) {
            char c1 = seq.charAt(--index);
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }


                /**
     * 返回给定索引之前的 {@code char} 数组中的代码点。如果 {@code char} 数组中 {@code (index - 1)} 位置的值在低代理范围，且 {@code (index - 2)} 不为负数，且 {@code char} 数组中 {@code (index - 2)} 位置的值在高代理范围，那么返回与此代理对对应的补充代码点。否则，返回 {@code char} 数组中 {@code (index - 1)} 位置的值。
     *
     * @param a {@code char} 数组
     * @param index 应返回的代码点之后的索引
     * @return 给定索引之前的 Unicode 代码点值。
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index} 参数小于 1 或大于 {@code char} 数组的长度
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index) {
        return codePointBeforeImpl(a, index, 0);
    }

    /**
     * 返回给定索引之前的 {@code char} 数组中的代码点，其中仅使用索引大于或等于 {@code start} 的数组元素。如果 {@code char} 数组中 {@code (index - 1)} 位置的值在低代理范围，且 {@code (index - 2)} 不小于 {@code start}，且 {@code char} 数组中 {@code (index - 2)} 位置的值在高代理范围，那么返回与此代理对对应的补充代码点。否则，返回 {@code char} 数组中 {@code (index - 1)} 位置的值。
     *
     * @param a {@code char} 数组
     * @param index 应返回的代码点之后的索引
     * @param start {@code char} 数组中第一个数组元素的索引
     * @return 给定索引之前的 Unicode 代码点值。
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index} 参数不大于 {@code start} 参数或大于 {@code char} 数组的长度，或者 {@code start} 参数为负数或不小于 {@code char} 数组的长度。
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index, int start) {
        if (index <= start || start < 0 || start >= a.length) {
            throw new IndexOutOfBoundsException();
        }
        return codePointBeforeImpl(a, index, start);
    }

    // 如果 index-1 超出边界，则抛出 ArrayIndexOutOfBoundsException
    static int codePointBeforeImpl(char[] a, int index, int start) {
        char c2 = a[--index];
        if (isLowSurrogate(c2) && index > start) {
            char c1 = a[--index];
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    /**
     * 返回表示指定补充字符（Unicode 代码点）在 UTF-16 编码中的代理对的前导代理（高代理代码单元）。如果指定的字符不是补充字符，则返回一个未指定的 {@code char}。
     *
     * <p>如果
     * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
     * 为 {@code true}，那么
     * {@link #isHighSurrogate isHighSurrogate}{@code (highSurrogate(x))} 和
     * {@link #toCodePoint toCodePoint}{@code (highSurrogate(x), }{@link #lowSurrogate lowSurrogate}{@code (x)) == x}
     * 也总是 {@code true}。
     *
     * @param   codePoint 一个补充字符（Unicode 代码点）
     * @return 用于表示字符在 UTF-16 编码中的前导代理代码单元
     * @since   1.7
     */
    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10)
            + (MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
    }

    /**
     * 返回表示指定补充字符（Unicode 代码点）在 UTF-16 编码中的代理对的尾随代理（低代理代码单元）。如果指定的字符不是补充字符，则返回一个未指定的 {@code char}。
     *
     * <p>如果
     * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
     * 为 {@code true}，那么
     * {@link #isLowSurrogate isLowSurrogate}{@code (lowSurrogate(x))} 和
     * {@link #toCodePoint toCodePoint}{@code (}{@link #highSurrogate highSurrogate}{@code (x), lowSurrogate(x)) == x}
     * 也总是 {@code true}。
     *
     * @param   codePoint 一个补充字符（Unicode 代码点）
     * @return 用于表示字符在 UTF-16 编码中的尾随代理代码单元
     * @since   1.7
     */
    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + MIN_LOW_SURROGATE);
    }

    /**
     * 将指定的字符（Unicode 代码点）转换为其 UTF-16 表示形式。如果指定的代码点是 BMP（基本多文种平面或平面 0）值，则相同的值存储在 {@code dst[dstIndex]} 中，返回 1。如果指定的代码点是补充字符，其代理值存储在 {@code dst[dstIndex]}（高代理）和 {@code dst[dstIndex+1]}（低代理）中，返回 2。
     *
     * @param  codePoint 要转换的字符（Unicode 代码点）。
     * @param  dst 一个 {@code char} 数组，用于存储 {@code codePoint} 的 UTF-16 值。
     * @param dstIndex 存储转换值的 {@code dst} 数组的起始索引。
     * @return 如果代码点是 BMP 代码点，则返回 1，如果是补充代码点，则返回 2。
     * @exception IllegalArgumentException 如果指定的 {@code codePoint} 不是有效的 Unicode 代码点。
     * @exception NullPointerException 如果指定的 {@code dst} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code dstIndex} 为负数或不小于 {@code dst.length}，或者 {@code dst} 在 {@code dstIndex} 处没有足够的数组元素来存储结果的 {@code char} 值。 （如果 {@code dstIndex} 等于 {@code dst.length-1} 且指定的 {@code codePoint} 是一个补充字符，则高代理值不会存储在 {@code dst[dstIndex]} 中。）
     * @since  1.5
     */
    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        if (isBmpCodePoint(codePoint)) {
            dst[dstIndex] = (char) codePoint;
            return 1;
        } else if (isValidCodePoint(codePoint)) {
            toSurrogates(codePoint, dst, dstIndex);
            return 2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 将指定的字符（Unicode 代码点）转换为其 UTF-16 表示形式并存储在 {@code char} 数组中。如果指定的代码点是 BMP（基本多文种平面或平面 0）值，则结果的 {@code char} 数组具有与 {@code codePoint} 相同的值。如果指定的代码点是补充代码点，则结果的 {@code char} 数组具有相应的代理对。
     *
     * @param  codePoint 一个 Unicode 代码点
     * @return 一个具有 {@code codePoint} 的 UTF-16 表示形式的 {@code char} 数组。
     * @exception IllegalArgumentException 如果指定的 {@code codePoint} 不是有效的 Unicode 代码点。
     * @since  1.5
     */
    public static char[] toChars(int codePoint) {
        if (isBmpCodePoint(codePoint)) {
            return new char[] { (char) codePoint };
        } else if (isValidCodePoint(codePoint)) {
            char[] result = new char[2];
            toSurrogates(codePoint, result, 0);
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }

    static void toSurrogates(int codePoint, char[] dst, int index) {
        // 我们“反向”写入元素以保证全部或无
        dst[index+1] = lowSurrogate(codePoint);
        dst[index] = highSurrogate(codePoint);
    }

    /**
     * 返回指定字符序列的文本范围中的 Unicode 代码点数。文本范围从指定的 {@code beginIndex} 开始，延伸到索引为 {@code endIndex - 1} 的 {@code char}。因此，文本范围的长度（以 {@code char} 为单位）为 {@code endIndex-beginIndex}。文本范围内的未配对代理计为一个代码点。
     *
     * @param seq 字符序列
     * @param beginIndex 文本范围中第一个 {@code char} 的索引。
     * @param endIndex 文本范围中最后一个 {@code char} 之后的索引。
     * @return 指定文本范围中的 Unicode 代码点数
     * @exception NullPointerException 如果 {@code seq} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code beginIndex} 为负数，或者 {@code endIndex} 大于给定序列的长度，或者 {@code beginIndex} 大于 {@code endIndex}。
     * @since  1.5
     */
    public static int codePointCount(CharSequence seq, int beginIndex, int endIndex) {
        int length = seq.length();
        if (beginIndex < 0 || endIndex > length || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        int n = endIndex - beginIndex;
        for (int i = beginIndex; i < endIndex; ) {
            if (isHighSurrogate(seq.charAt(i++)) && i < endIndex &&
                isLowSurrogate(seq.charAt(i))) {
                n--;
                i++;
            }
        }
        return n;
    }

    /**
     * 返回 {@code char} 数组参数的子数组中的 Unicode 代码点数。{@code offset} 参数是子数组中第一个 {@code char} 的索引，{@code count} 参数指定子数组在 {@code char} 中的长度。子数组中的未配对代理计为一个代码点。
     *
     * @param a {@code char} 数组
     * @param offset 给定 {@code char} 数组中第一个 {@code char} 的索引
     * @param count 子数组在 {@code char} 中的长度
     * @return 指定子数组中的 Unicode 代码点数
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code offset} 或 {@code count} 为负数，或者 {@code offset + count} 大于给定数组的长度。
     * @since  1.5
     */
    public static int codePointCount(char[] a, int offset, int count) {
        if (count > a.length - offset || offset < 0 || count < 0) {
            throw new IndexOutOfBoundsException();
        }
        return codePointCountImpl(a, offset, count);
    }

    static int codePointCountImpl(char[] a, int offset, int count) {
        int endIndex = offset + count;
        int n = count;
        for (int i = offset; i < endIndex; ) {
            if (isHighSurrogate(a[i++]) && i < endIndex &&
                isLowSurrogate(a[i])) {
                n--;
                i++;
            }
        }
        return n;
    }

    /**
     * 返回给定字符序列中从给定 {@code index} 偏移 {@code codePointOffset} 代码点的索引。由 {@code index} 和 {@code codePointOffset} 给定的文本范围中的未配对代理计为一个代码点。
     *
     * @param seq 字符序列
     * @param index 要偏移的索引
     * @param codePointOffset 代码点的偏移量
     * @return 字符序列中的索引
     * @exception NullPointerException 如果 {@code seq} 为 null。
     * @exception IndexOutOfBoundsException 如果 {@code index} 为负数或大于字符序列的长度，或者 {@code codePointOffset} 为正数且从 {@code index} 开始的子序列少于 {@code codePointOffset} 个代码点，或者 {@code codePointOffset} 为负数且在 {@code index} 之前的子序列少于 {@code codePointOffset} 绝对值个代码点。
     * @since 1.5
     */
    public static int offsetByCodePoints(CharSequence seq, int index,
                                         int codePointOffset) {
        int length = seq.length();
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }

        int x = index;
        if (codePointOffset >= 0) {
            int i;
            for (i = 0; x < length && i < codePointOffset; i++) {
                if (isHighSurrogate(seq.charAt(x++)) && x < length &&
                    isLowSurrogate(seq.charAt(x))) {
                    x++;
                }
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i;
            for (i = codePointOffset; x > 0 && i < 0; i++) {
                if (isLowSurrogate(seq.charAt(--x)) && x > 0 &&
                    isHighSurrogate(seq.charAt(x-1))) {
                    x--;
                }
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    /**
     * 返回给定 {@code char} 子数组中从给定 {@code index} 偏移 {@code codePointOffset} 代码点的索引。{@code start} 和 {@code count} 参数指定 {@code char} 数组的子数组。由 {@code index} 和 {@code codePointOffset} 给定的文本范围中的未配对代理计为一个代码点。
     *
     * @param a {@code char} 数组
     * @param start 子数组中第一个 {@code char} 的索引
     * @param count 子数组在 {@code char} 中的长度
     * @param index 要偏移的索引
     * @param codePointOffset 代码点的偏移量
     * @return 子数组中的索引
     * @exception NullPointerException 如果 {@code a} 为 null。
     * @exception IndexOutOfBoundsException
     *   如果 {@code start} 或 {@code count} 为负数，
     *   或者 {@code start + count} 大于给定数组的长度，
     *   或者 {@code index} 小于 {@code start} 或大于 {@code start + count}，
     *   或者 {@code codePointOffset} 为正数且从 {@code index} 开始到 {@code start + count - 1} 结束的文本范围少于 {@code codePointOffset} 个代码点，
     *   或者 {@code codePointOffset} 为负数且从 {@code start} 开始到 {@code index - 1} 结束的文本范围少于 {@code codePointOffset} 绝对值个代码点。
     * @since 1.5
     */
    public static int offsetByCodePoints(char[] a, int start, int count,
                                         int index, int codePointOffset) {
        if (count > a.length-start || start < 0 || count < 0
            || index < start || index > start+count) {
            throw new IndexOutOfBoundsException();
        }
        return offsetByCodePointsImpl(a, start, count, index, codePointOffset);
    }


                static int offsetByCodePointsImpl(char[]a, int start, int count,
                                      int index, int codePointOffset) {
        int x = index;
        if (codePointOffset >= 0) {
            int limit = start + count;
            int i;
            for (i = 0; x < limit && i < codePointOffset; i++) {
                if (isHighSurrogate(a[x++]) && x < limit &&
                    isLowSurrogate(a[x])) {
                    x++;
                }
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i;
            for (i = codePointOffset; x > start && i < 0; i++) {
                if (isLowSurrogate(a[--x]) && x > start &&
                    isHighSurrogate(a[x-1])) {
                    x--;
                }
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    /**
     * 确定指定字符是否为小写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@code Character.getType(ch)} 提供）为
     * {@code LOWERCASE_LETTER}，或者它具有 Unicode 标准定义的 Other_Lowercase 属性，则该字符为小写字母。
     * <p>
     * 以下是一些小写字母的示例：
     * <blockquote><pre>
     * a b c d e f g h i j k l m n o p q r s t u v w x y z
     * '&#92;u00DF' '&#92;u00E0' '&#92;u00E1' '&#92;u00E2' '&#92;u00E3' '&#92;u00E4' '&#92;u00E5' '&#92;u00E6'
     * '&#92;u00E7' '&#92;u00E8' '&#92;u00E9' '&#92;u00EA' '&#92;u00EB' '&#92;u00EC' '&#92;u00ED' '&#92;u00EE'
     * '&#92;u00EF' '&#92;u00F0' '&#92;u00F1' '&#92;u00F2' '&#92;u00F3' '&#92;u00F4' '&#92;u00F5' '&#92;u00F6'
     * '&#92;u00F8' '&#92;u00F9' '&#92;u00FA' '&#92;u00FB' '&#92;u00FC' '&#92;u00FD' '&#92;u00FE' '&#92;u00FF'
     * </pre></blockquote>
     * <p> 许多其他 Unicode 字符也是小写字母。
     *
     * <p><b>注意：</b> 此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isLowerCase(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是小写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#toLowerCase(char)
     * @see     Character#getType(char)
     */
    public static boolean isLowerCase(char ch) {
        return isLowerCase((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为小写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@link Character#getType getType(codePoint)} 提供）为
     * {@code LOWERCASE_LETTER}，或者它具有 Unicode 标准定义的 Other_Lowercase 属性，则该字符为小写字母。
     * <p>
     * 以下是一些小写字母的示例：
     * <blockquote><pre>
     * a b c d e f g h i j k l m n o p q r s t u v w x y z
     * '&#92;u00DF' '&#92;u00E0' '&#92;u00E1' '&#92;u00E2' '&#92;u00E3' '&#92;u00E4' '&#92;u00E5' '&#92;u00E6'
     * '&#92;u00E7' '&#92;u00E8' '&#92;u00E9' '&#92;u00EA' '&#92;u00EB' '&#92;u00EC' '&#92;u00ED' '&#92;u00EE'
     * '&#92;u00EF' '&#92;u00F0' '&#92;u00F1' '&#92;u00F2' '&#92;u00F3' '&#92;u00F4' '&#92;u00F5' '&#92;u00F6'
     * '&#92;u00F8' '&#92;u00F9' '&#92;u00FA' '&#92;u00FB' '&#92;u00FC' '&#92;u00FD' '&#92;u00FE' '&#92;u00FF'
     * </pre></blockquote>
     * <p> 许多其他 Unicode 字符也是小写字母。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是小写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#toLowerCase(int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isLowerCase(int codePoint) {
        return getType(codePoint) == Character.LOWERCASE_LETTER ||
               CharacterData.of(codePoint).isOtherLowercase(codePoint);
    }

    /**
     * 确定指定字符是否为大写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@code Character.getType(ch)} 提供）为
     * {@code UPPERCASE_LETTER}，或者它具有 Unicode 标准定义的 Other_Uppercase 属性，则该字符为大写字母。
     * <p>
     * 以下是一些大写字母的示例：
     * <blockquote><pre>
     * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     * '&#92;u00C0' '&#92;u00C1' '&#92;u00C2' '&#92;u00C3' '&#92;u00C4' '&#92;u00C5' '&#92;u00C6' '&#92;u00C7'
     * '&#92;u00C8' '&#92;u00C9' '&#92;u00CA' '&#92;u00CB' '&#92;u00CC' '&#92;u00CD' '&#92;u00CE' '&#92;u00CF'
     * '&#92;u00D0' '&#92;u00D1' '&#92;u00D2' '&#92;u00D3' '&#92;u00D4' '&#92;u00D5' '&#92;u00D6' '&#92;u00D8'
     * '&#92;u00D9' '&#92;u00DA' '&#92;u00DB' '&#92;u00DC' '&#92;u00DD' '&#92;u00DE'
     * </pre></blockquote>
     * <p> 许多其他 Unicode 字符也是大写字母。
     *
     * <p><b>注意：</b> 此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isUpperCase(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是大写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#toUpperCase(char)
     * @see     Character#getType(char)
     * @since   1.0
     */
    public static boolean isUpperCase(char ch) {
        return isUpperCase((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为大写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@link Character#getType(int) getType(codePoint)} 提供）为
     * {@code UPPERCASE_LETTER}，或者它具有 Unicode 标准定义的 Other_Uppercase 属性，则该字符为大写字母。
     * <p>
     * 以下是一些大写字母的示例：
     * <blockquote><pre>
     * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     * '&#92;u00C0' '&#92;u00C1' '&#92;u00C2' '&#92;u00C3' '&#92;u00C4' '&#92;u00C5' '&#92;u00C6' '&#92;u00C7'
     * '&#92;u00C8' '&#92;u00C9' '&#92;u00CA' '&#92;u00CB' '&#92;u00CC' '&#92;u00CD' '&#92;u00CE' '&#92;u00CF'
     * '&#92;u00D0' '&#92;u00D1' '&#92;u00D2' '&#92;u00D3' '&#92;u00D4' '&#92;u00D5' '&#92;u00D6' '&#92;u00D8'
     * '&#92;u00D9' '&#92;u00DA' '&#92;u00DB' '&#92;u00DC' '&#92;u00DD' '&#92;u00DE'
     * </pre></blockquote>
     * <p> 许多其他 Unicode 字符也是大写字母。<p>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是大写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#toUpperCase(int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isUpperCase(int codePoint) {
        return getType(codePoint) == Character.UPPERCASE_LETTER ||
               CharacterData.of(codePoint).isOtherUppercase(codePoint);
    }

    /**
     * 确定指定字符是否为标题大小写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@code Character.getType(ch)} 提供）为
     * {@code TITLECASE_LETTER}，则该字符为标题大小写字母。
     * <p>
     * 有些字符看起来像拉丁字母对。例如，有一个大写字母看起来像 "LJ"，其对应的小写字母看起来像 "lj"。第三种形式，看起来像 "Lj"，
     * 是在渲染小写单词时使用初始大写字母（如书名）的适当形式。
     * <p>
     * 以下是一些此方法返回 {@code true} 的 Unicode 字符：
     * <ul>
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
     * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
     * </ul>
     * <p> 许多其他 Unicode 字符也是标题大小写字母。
     *
     * <p><b>注意：</b> 此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isTitleCase(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是标题大小写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(char)
     * @see     Character#isUpperCase(char)
     * @see     Character#toTitleCase(char)
     * @see     Character#getType(char)
     * @since   1.0.2
     */
    public static boolean isTitleCase(char ch) {
        return isTitleCase((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为标题大小写字母。
     * <p>
     * 如果字符的通用类别类型（由 {@link Character#getType(int) getType(codePoint)} 提供）为
     * {@code TITLECASE_LETTER}，则该字符为标题大小写字母。
     * <p>
     * 有些字符看起来像拉丁字母对。例如，有一个大写字母看起来像 "LJ"，其对应的小写字母看起来像 "lj"。第三种形式，看起来像 "Lj"，
     * 是在渲染小写单词时使用初始大写字母（如书名）的适当形式。
     * <p>
     * 以下是一些此方法返回 {@code true} 的 Unicode 字符：
     * <ul>
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
     * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
     * </ul>
     * <p> 许多其他 Unicode 字符也是标题大小写字母。<p>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是标题大小写字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isLowerCase(int)
     * @see     Character#isUpperCase(int)
     * @see     Character#toTitleCase(int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isTitleCase(int codePoint) {
        return getType(codePoint) == Character.TITLECASE_LETTER;
    }

    /**
     * 确定指定字符是否为数字。
     * <p>
     * 如果字符的通用类别类型（由 {@code Character.getType(ch)} 提供）为
     * {@code DECIMAL_DIGIT_NUMBER}，则该字符为数字。
     * <p>
     * 以下是一些包含数字的 Unicode 字符范围：
     * <ul>
     * <li>{@code '\u005Cu0030'} 通过 {@code '\u005Cu0039'}，
     *     ISO-LATIN-1 数字 ({@code '0'} 通过 {@code '9'})
     * <li>{@code '\u005Cu0660'} 通过 {@code '\u005Cu0669'}，
     *     阿拉伯-印度数字
     * <li>{@code '\u005Cu06F0'} 通过 {@code '\u005Cu06F9'}，
     *     扩展的阿拉伯-印度数字
     * <li>{@code '\u005Cu0966'} 通过 {@code '\u005Cu096F'}，
     *     印地数字
     * <li>{@code '\u005CuFF10'} 通过 {@code '\u005CuFF19'}，
     *     全角数字
     * </ul>
     *
     * 许多其他字符范围也包含数字。
     *
     * <p><b>注意：</b> 此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isDigit(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是数字，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Character#getType(char)
     */
    public static boolean isDigit(char ch) {
        return isDigit((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为数字。
     * <p>
     * 如果字符的通用类别类型（由 {@link Character#getType(int) getType(codePoint)} 提供）为
     * {@code DECIMAL_DIGIT_NUMBER}，则该字符为数字。
     * <p>
     * 以下是一些包含数字的 Unicode 字符范围：
     * <ul>
     * <li>{@code '\u005Cu0030'} 通过 {@code '\u005Cu0039'}，
     *     ISO-LATIN-1 数字 ({@code '0'} 通过 {@code '9'})
     * <li>{@code '\u005Cu0660'} 通过 {@code '\u005Cu0669'}，
     *     阿拉伯-印度数字
     * <li>{@code '\u005Cu06F0'} 通过 {@code '\u005Cu06F9'}，
     *     扩展的阿拉伯-印度数字
     * <li>{@code '\u005Cu0966'} 通过 {@code '\u005Cu096F'}，
     *     印地数字
     * <li>{@code '\u005CuFF10'} 通过 {@code '\u005CuFF19'}，
     *     全角数字
     * </ul>
     *
     * 许多其他字符范围也包含数字。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是数字，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#forDigit(int, int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isDigit(int codePoint) {
        return getType(codePoint) == Character.DECIMAL_DIGIT_NUMBER;
    }

    /**
     * 确定字符是否在 Unicode 中定义。
     * <p>
     * 如果以下任一条件为真，则字符已定义：
     * <ul>
     * <li>它在 UnicodeData 文件中有条目。
     * <li>它在 UnicodeData 文件定义的范围内有值。
     * </ul>
     *
     * <p><b>注意：</b> 此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isDefined(int)} 方法。
     *
     * @param   ch   要测试的字符
     * @return  如果字符在 Unicode 中有定义的含义，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(char)
     * @see     Character#isLetter(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#isUpperCase(char)
     * @since   1.0.2
     */
    public static boolean isDefined(char ch) {
        return isDefined((int)ch);
    }

    /**
     * 确定字符（Unicode 代码点）是否在 Unicode 中定义。
     * <p>
     * 如果以下任一条件为真，则字符已定义：
     * <ul>
     * <li>它在 UnicodeData 文件中有条目。
     * <li>它在 UnicodeData 文件定义的范围内有值。
     * </ul>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符在 Unicode 中有定义的含义，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(int)
     * @see     Character#isLetter(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#isUpperCase(int)
     * @since   1.5
     */
    public static boolean isDefined(int codePoint) {
        return getType(codePoint) != Character.UNASSIGNED;
    }


                /**
     * 确定指定的字符是否为字母。
     * <p>
     * 如果字符的一般类别类型，由 {@code Character.getType(ch)} 提供，
     * 是以下任何一种，则认为该字符是字母：
     * <ul>
     * <li> {@code UPPERCASE_LETTER}
     * <li> {@code LOWERCASE_LETTER}
     * <li> {@code TITLECASE_LETTER}
     * <li> {@code MODIFIER_LETTER}
     * <li> {@code OTHER_LETTER}
     * </ul>
     *
     * 并非所有字母都有大小写。许多字符是字母，但既不是大写也不是小写或标题大小写。
     *
     * <p><b>注意：</b> 该方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isLetter(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isJavaLetter(char)
     * @see     Character#isJavaLetterOrDigit(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @see     Character#isUpperCase(char)
     */
    public static boolean isLetter(char ch) {
        return isLetter((int)ch);
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否为字母。
     * <p>
     * 如果字符的一般类别类型，由 {@link Character#getType(int) getType(codePoint)} 提供，
     * 是以下任何一种，则认为该字符是字母：
     * <ul>
     * <li> {@code UPPERCASE_LETTER}
     * <li> {@code LOWERCASE_LETTER}
     * <li> {@code TITLECASE_LETTER}
     * <li> {@code MODIFIER_LETTER}
     * <li> {@code OTHER_LETTER}
     * </ul>
     *
     * 并非所有字母都有大小写。许多字符是字母，但既不是大写也不是小写或标题大小写。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是字母，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(int)
     * @see     Character#isJavaIdentifierStart(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#isUnicodeIdentifierStart(int)
     * @see     Character#isUpperCase(int)
     * @since   1.5
     */
    public static boolean isLetter(int codePoint) {
        return ((((1 << Character.UPPERCASE_LETTER) |
            (1 << Character.LOWERCASE_LETTER) |
            (1 << Character.TITLECASE_LETTER) |
            (1 << Character.MODIFIER_LETTER) |
            (1 << Character.OTHER_LETTER)) >> getType(codePoint)) & 1)
            != 0;
    }

    /**
     * 确定指定的字符是否为字母或数字。
     * <p>
     * 如果 {@code Character.isLetter(char ch)} 或
     * {@code Character.isDigit(char ch)} 任一方法对字符返回
     * {@code true}，则认为该字符是字母或数字。
     *
     * <p><b>注意：</b> 该方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isLetterOrDigit(int)} 方法。
     *
     * @param   ch   要测试的字符。
     * @return  如果字符是字母或数字，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(char)
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isJavaLetter(char)
     * @see     Character#isJavaLetterOrDigit(char)
     * @see     Character#isLetter(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @since   1.0.2
     */
    public static boolean isLetterOrDigit(char ch) {
        return isLetterOrDigit((int)ch);
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否为字母或数字。
     * <p>
     * 如果 {@link #isLetter(int) isLetter(codePoint)} 或
     * {@link #isDigit(int) isDigit(codePoint)} 任一方法对字符返回
     * {@code true}，则认为该字符是字母或数字。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是字母或数字，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isDigit(int)
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isLetter(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @since   1.5
     */
    public static boolean isLetterOrDigit(int codePoint) {
        return ((((1 << Character.UPPERCASE_LETTER) |
            (1 << Character.LOWERCASE_LETTER) |
            (1 << Character.TITLECASE_LETTER) |
            (1 << Character.MODIFIER_LETTER) |
            (1 << Character.OTHER_LETTER) |
            (1 << Character.DECIMAL_DIGIT_NUMBER)) >> getType(codePoint)) & 1)
            != 0;
    }

    /**
     * 确定指定的字符是否可以作为 Java 标识符的第一个字符。
     * <p>
     * 如果且仅当以下任一条件为真时，字符可以作为 Java 标识符的开头：
     * <ul>
     * <li> {@link #isLetter(char) isLetter(ch)} 返回 {@code true}
     * <li> {@link #getType(char) getType(ch)} 返回 {@code LETTER_NUMBER}
     * <li> {@code ch} 是货币符号（如 {@code '$'}）
     * <li> {@code ch} 是连接标点符号（如 {@code '_'}）。
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * @param   ch 要测试的字符。
     * @return  如果字符可以作为 Java 标识符的开头，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaLetterOrDigit(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isLetter(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @since   1.02
     * @deprecated 被 isJavaIdentifierStart(char) 替代。
     */
    @Deprecated
    public static boolean isJavaLetter(char ch) {
        return isJavaIdentifierStart(ch);
    }

    /**
     * 确定指定的字符是否可以作为 Java 标识符的非第一个字符。
     * <p>
     * 如果且仅当以下任一条件为真时，字符可以作为 Java 标识符的一部分：
     * <ul>
     * <li>  它是字母
     * <li>  它是货币符号（如 {@code '$'}）
     * <li>  它是连接标点符号（如 {@code '_'}）
     * <li>  它是数字
     * <li>  它是数字字母（如罗马数字字符）
     * <li>  它是组合标记
     * <li>  它是非间距标记
     * <li> {@code isIdentifierIgnorable} 返回
     * {@code true} 对于该字符。
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * @param   ch 要测试的字符。
     * @return  如果字符可以作为 Java 标识符的一部分，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaLetter(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isLetter(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @see     Character#isIdentifierIgnorable(char)
     * @since   1.02
     * @deprecated 被 isJavaIdentifierPart(char) 替代。
     */
    @Deprecated
    public static boolean isJavaLetterOrDigit(char ch) {
        return isJavaIdentifierPart(ch);
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否为字母。
     * <p>
     * 如果字符的一般类别类型，由 {@link Character#getType(int) getType(codePoint)} 提供，
     * 是以下任何一种，则认为该字符是字母：
     * <ul>
     * <li> <code>UPPERCASE_LETTER</code>
     * <li> <code>LOWERCASE_LETTER</code>
     * <li> <code>TITLECASE_LETTER</code>
     * <li> <code>MODIFIER_LETTER</code>
     * <li> <code>OTHER_LETTER</code>
     * <li> <code>LETTER_NUMBER</code>
     * </ul>
     * 或者它具有 Unicode 标准定义的其他字母属性。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  <code>true</code> 如果字符是 Unicode 字母字符，
     *          <code>false</code> 否则。
     * @since   1.7
     */
    public static boolean isAlphabetic(int codePoint) {
        return (((((1 << Character.UPPERCASE_LETTER) |
            (1 << Character.LOWERCASE_LETTER) |
            (1 << Character.TITLECASE_LETTER) |
            (1 << Character.MODIFIER_LETTER) |
            (1 << Character.OTHER_LETTER) |
            (1 << Character.LETTER_NUMBER)) >> getType(codePoint)) & 1) != 0) ||
            CharacterData.of(codePoint).isOtherAlphabetic(codePoint);
    }

    /**
     * 确定指定的字符（Unicode 代码点）是否为 CJKV（中文、日文、韩文和越南文）表意文字，如
     * Unicode 标准所定义。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  <code>true</code> 如果字符是 Unicode 表意文字字符，
     *          <code>false</code> 否则。
     * @since   1.7
     */
    public static boolean isIdeographic(int codePoint) {
        return CharacterData.of(codePoint).isIdeographic(codePoint);
    }

    /**
     * 确定指定的字符是否可以作为 Java 标识符的第一个字符。
     * <p>
     * 如果且仅当以下任一条件为真时，字符可以作为 Java 标识符的开头：
     * <ul>
     * <li> {@link #isLetter(char) isLetter(ch)} 返回 {@code true}
     * <li> {@link #getType(char) getType(ch)} 返回 {@code LETTER_NUMBER}
     * <li> {@code ch} 是货币符号（如 {@code '$'}）
     * <li> {@code ch} 是连接标点符号（如 {@code '_'}）。
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * <p><b>注意：</b> 该方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isJavaIdentifierStart(int)} 方法。
     *
     * @param   ch 要测试的字符。
     * @return  如果字符可以作为 Java 标识符的开头，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isLetter(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.1
     */
    public static boolean isJavaIdentifierStart(char ch) {
        return isJavaIdentifierStart((int)ch);
    }

    /**
     * 确定字符（Unicode 代码点）是否可以作为 Java 标识符的第一个字符。
     * <p>
     * 如果且仅当以下任一条件为真时，字符可以作为 Java 标识符的开头：
     * <ul>
     * <li> {@link #isLetter(int) isLetter(codePoint)}
     *      返回 {@code true}
     * <li> {@link #getType(int) getType(codePoint)}
     *      返回 {@code LETTER_NUMBER}
     * <li> 引用的字符是货币符号（如 {@code '$'}）
     * <li> 引用的字符是连接标点符号（如 {@code '_'}）。
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符可以作为 Java 标识符的开头，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isLetter(int)
     * @see     Character#isUnicodeIdentifierStart(int)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.5
     */
    public static boolean isJavaIdentifierStart(int codePoint) {
        return CharacterData.of(codePoint).isJavaIdentifierStart(codePoint);
    }

    /**
     * 确定指定的字符是否可以作为 Java 标识符的非第一个字符。
     * <p>
     * 如果以下任一条件为真，字符可以作为 Java 标识符的一部分：
     * <ul>
     * <li>  它是字母
     * <li>  它是货币符号（如 {@code '$'}）
     * <li>  它是连接标点符号（如 {@code '_'}）
     * <li>  它是数字
     * <li>  它是数字字母（如罗马数字字符）
     * <li>  它是组合标记
     * <li>  它是非间距标记
     * <li> {@code isIdentifierIgnorable} 返回
     * {@code true} 对于该字符
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * <p><b>注意：</b> 该方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isJavaIdentifierPart(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return {@code true} 如果字符可以作为 Java 标识符的一部分；
     *          否则返回 {@code false}。
     * @see     Character#isIdentifierIgnorable(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.1
     */
    public static boolean isJavaIdentifierPart(char ch) {
        return isJavaIdentifierPart((int)ch);
    }

    /**
     * 确定字符（Unicode 代码点）是否可以作为 Java 标识符的非第一个字符。
     * <p>
     * 如果以下任一条件为真，字符可以作为 Java 标识符的一部分：
     * <ul>
     * <li>  它是字母
     * <li>  它是货币符号（如 {@code '$'}）
     * <li>  它是连接标点符号（如 {@code '_'}）
     * <li>  它是数字
     * <li>  它是数字字母（如罗马数字字符）
     * <li>  它是组合标记
     * <li>  它是非间距标记
     * <li> {@link #isIdentifierIgnorable(int)
     * isIdentifierIgnorable(codePoint)} 返回 {@code true} 对于
     * 该代码点
     * </ul>
     *
     * 这些条件是根据 Unicode 6.2 版本的字符信息进行测试的。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return {@code true} 如果字符可以作为 Java 标识符的一部分；
     *          否则返回 {@code false}。
     * @see     Character#isIdentifierIgnorable(int)
     * @see     Character#isJavaIdentifierStart(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.5
     */
    public static boolean isJavaIdentifierPart(int codePoint) {
        return CharacterData.of(codePoint).isJavaIdentifierPart(codePoint);
    }


                /**
     * 确定指定字符是否可以作为 Unicode 标识符的第一个字符。
     * <p>
     * 一个字符可以作为 Unicode 标识符的第一个字符，当且仅当以下条件之一为真：
     * <ul>
     * <li> {@link #isLetter(char) isLetter(ch)} 返回 {@code true}
     * <li> {@link #getType(char) getType(ch)} 返回
     *      {@code LETTER_NUMBER}。
     * </ul>
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isUnicodeIdentifierStart(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  如果字符可以作为 Unicode 标识符的开头，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isLetter(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @since   1.1
     */
    public static boolean isUnicodeIdentifierStart(char ch) {
        return isUnicodeIdentifierStart((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否可以作为 Unicode 标识符的第一个字符。
     * <p>
     * 一个字符可以作为 Unicode 标识符的第一个字符，当且仅当以下条件之一为真：
     * <ul>
     * <li> {@link #isLetter(int) isLetter(codePoint)}
     *      返回 {@code true}
     * <li> {@link #getType(int) getType(codePoint)}
     *      返回 {@code LETTER_NUMBER}。
     * </ul>
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符可以作为 Unicode 标识符的开头，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaIdentifierStart(int)
     * @see     Character#isLetter(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @since   1.5
     */
    public static boolean isUnicodeIdentifierStart(int codePoint) {
        return CharacterData.of(codePoint).isUnicodeIdentifierStart(codePoint);
    }

    /**
     * 确定指定字符是否可以作为 Unicode 标识符的非第一个字符。
     * <p>
     * 一个字符可以作为 Unicode 标识符的非第一个字符，当且仅当以下条件之一为真：
     * <ul>
     * <li> 它是一个字母
     * <li> 它是一个连接标点字符（如 {@code '_'}）
     * <li> 它是一个数字
     * <li> 它是一个数字字母（如罗马数字字符）
     * <li> 它是一个组合标记
     * <li> 它是一个非间距标记
     * <li> {@code isIdentifierIgnorable} 返回
     * {@code true} 对于此字符。
     * </ul>
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isUnicodeIdentifierPart(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  如果字符可以作为 Unicode 标识符的非第一个字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isIdentifierIgnorable(char)
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @since   1.1
     */
    public static boolean isUnicodeIdentifierPart(char ch) {
        return isUnicodeIdentifierPart((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否可以作为 Unicode 标识符的非第一个字符。
     * <p>
     * 一个字符可以作为 Unicode 标识符的非第一个字符，当且仅当以下条件之一为真：
     * <ul>
     * <li> 它是一个字母
     * <li> 它是一个连接标点字符（如 {@code '_'}）
     * <li> 它是一个数字
     * <li> 它是一个数字字母（如罗马数字字符）
     * <li> 它是一个组合标记
     * <li> 它是一个非间距标记
     * <li> {@code isIdentifierIgnorable} 返回
     * {@code true} 对于此字符。
     * </ul>
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符可以作为 Unicode 标识符的非第一个字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isIdentifierIgnorable(int)
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isUnicodeIdentifierStart(int)
     * @since   1.5
     */
    public static boolean isUnicodeIdentifierPart(int codePoint) {
        return CharacterData.of(codePoint).isUnicodeIdentifierPart(codePoint);
    }

    /**
     * 确定指定字符是否应被视为 Java 标识符或 Unicode 标识符中的可忽略字符。
     * <p>
     * 以下 Unicode 字符在 Java 标识符或 Unicode 标识符中是可忽略的：
     * <ul>
     * <li>不是空白的 ISO 控制字符
     * <ul>
     * <li>{@code '\u005Cu0000'} 到 {@code '\u005Cu0008'}
     * <li>{@code '\u005Cu000E'} 到 {@code '\u005Cu001B'}
     * <li>{@code '\u005Cu007F'} 到 {@code '\u005Cu009F'}
     * </ul>
     *
     * <li>所有具有 {@code FORMAT} 一般类别值的字符
     * </ul>
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isIdentifierIgnorable(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  如果字符是一个可忽略的控制字符，可以作为 Java 或 Unicode 标识符的一部分，则返回 {@code true}；
     *           否则返回 {@code false}。
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @since   1.1
     */
    public static boolean isIdentifierIgnorable(char ch) {
        return isIdentifierIgnorable((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否应被视为 Java 标识符或 Unicode 标识符中的可忽略字符。
     * <p>
     * 以下 Unicode 字符在 Java 标识符或 Unicode 标识符中是可忽略的：
     * <ul>
     * <li>不是空白的 ISO 控制字符
     * <ul>
     * <li>{@code '\u005Cu0000'} 到 {@code '\u005Cu0008'}
     * <li>{@code '\u005Cu000E'} 到 {@code '\u005Cu001B'}
     * <li>{@code '\u005Cu007F'} 到 {@code '\u005Cu009F'}
     * </ul>
     *
     * <li>所有具有 {@code FORMAT} 一般类别值的字符
     * </ul>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是一个可忽略的控制字符，可以作为 Java 或 Unicode 标识符的一部分，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @since   1.5
     */
    public static boolean isIdentifierIgnorable(int codePoint) {
        return CharacterData.of(codePoint).isIdentifierIgnorable(codePoint);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符参数转换为小写。
     * <p>
     * 注意，对于某些字符范围，特别是符号或表意文字，
     * {@code Character.isLowerCase(Character.toLowerCase(ch))}
     * 并不总是返回 {@code true}。
     *
     * <p>通常情况下，应使用 {@link String#toLowerCase()} 将字符转换为小写。
     * {@code String} 大小写映射方法比 {@code Character} 大小写映射方法具有多个优势。
     * {@code String} 大小写映射方法可以执行与区域设置相关的映射、上下文相关的映射和 1:M 字符映射，而
     * {@code Character} 大小写映射方法则不能。
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #toLowerCase(int)} 方法。
     *
     * @param   ch   要转换的字符。
     * @return  字符的小写形式，如果有的话；否则返回字符本身。
     * @see     Character#isLowerCase(char)
     * @see     String#toLowerCase()
     */
    public static char toLowerCase(char ch) {
        return (char)toLowerCase((int)ch);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符（Unicode 代码点）参数转换为小写。
     *
     * <p> 注意，对于某些字符范围，特别是符号或表意文字，
     * {@code Character.isLowerCase(Character.toLowerCase(codePoint))}
     * 并不总是返回 {@code true}。
     *
     * <p>通常情况下，应使用 {@link String#toLowerCase()} 将字符转换为小写。
     * {@code String} 大小写映射方法比 {@code Character} 大小写映射方法具有多个优势。
     * {@code String} 大小写映射方法可以执行与区域设置相关的映射、上下文相关的映射和 1:M 字符映射，而
     * {@code Character} 大小写映射方法则不能。
     *
     * @param   codePoint   要转换的字符（Unicode 代码点）。
     * @return  字符的小写形式（Unicode 代码点），如果有的话；否则返回字符本身。
     * @see     Character#isLowerCase(int)
     * @see     String#toLowerCase()
     *
     * @since   1.5
     */
    public static int toLowerCase(int codePoint) {
        return CharacterData.of(codePoint).toLowerCase(codePoint);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符参数转换为大写。
     * <p>
     * 注意，对于某些字符范围，特别是符号或表意文字，
     * {@code Character.isUpperCase(Character.toUpperCase(ch))}
     * 并不总是返回 {@code true}。
     *
     * <p>通常情况下，应使用 {@link String#toUpperCase()} 将字符转换为大写。
     * {@code String} 大小写映射方法比 {@code Character} 大小写映射方法具有多个优势。
     * {@code String} 大小写映射方法可以执行与区域设置相关的映射、上下文相关的映射和 1:M 字符映射，而
     * {@code Character} 大小写映射方法则不能。
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #toUpperCase(int)} 方法。
     *
     * @param   ch   要转换的字符。
     * @return  字符的大写形式，如果有的话；否则返回字符本身。
     * @see     Character#isUpperCase(char)
     * @see     String#toUpperCase()
     */
    public static char toUpperCase(char ch) {
        return (char)toUpperCase((int)ch);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符（Unicode 代码点）参数转换为大写。
     *
     * <p>注意，对于某些字符范围，特别是符号或表意文字，
     * {@code Character.isUpperCase(Character.toUpperCase(codePoint))}
     * 并不总是返回 {@code true}。
     *
     * <p>通常情况下，应使用 {@link String#toUpperCase()} 将字符转换为大写。
     * {@code String} 大小写映射方法比 {@code Character} 大小写映射方法具有多个优势。
     * {@code String} 大小写映射方法可以执行与区域设置相关的映射、上下文相关的映射和 1:M 字符映射，而
     * {@code Character} 大小写映射方法则不能。
     *
     * @param   codePoint   要转换的字符（Unicode 代码点）。
     * @return  字符的大写形式，如果有的话；否则返回字符本身。
     * @see     Character#isUpperCase(int)
     * @see     String#toUpperCase()
     *
     * @since   1.5
     */
    public static int toUpperCase(int codePoint) {
        return CharacterData.of(codePoint).toUpperCase(codePoint);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符参数转换为标题大小写。如果字符没有显式的标题大小写映射，并且根据 UnicodeData 不是标题大小写字符，则返回大写映射作为等效的标题大小写映射。如果
     * {@code char} 参数已经是标题大小写
     * {@code char}，则返回相同的 {@code char} 值。
     * <p>
     * 注意，对于某些字符范围，
     * {@code Character.isTitleCase(Character.toTitleCase(ch))}
     * 并不总是返回 {@code true}。
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #toTitleCase(int)} 方法。
     *
     * @param   ch   要转换的字符。
     * @return  字符的标题大小写形式，如果有的话；否则返回字符本身。
     * @see     Character#isTitleCase(char)
     * @see     Character#toLowerCase(char)
     * @see     Character#toUpperCase(char)
     * @since   1.0.2
     */
    public static char toTitleCase(char ch) {
        return (char)toTitleCase((int)ch);
    }

    /**
     * 使用 UnicodeData 文件中的大小写映射信息将字符（Unicode 代码点）参数转换为标题大小写。如果字符没有显式的标题大小写映射，并且根据 UnicodeData 不是标题大小写字符，则返回大写映射作为等效的标题大小写映射。如果
     * 字符参数已经是标题大小写
     * 字符，则返回相同的字符值。
     *
     * <p>注意，对于某些字符范围，
     * {@code Character.isTitleCase(Character.toTitleCase(codePoint))}
     * 并不总是返回 {@code true}。
     *
     * @param   codePoint   要转换的字符（Unicode 代码点）。
     * @return  字符的标题大小写形式，如果有的话；否则返回字符本身。
     * @see     Character#isTitleCase(int)
     * @see     Character#toLowerCase(int)
     * @see     Character#toUpperCase(int)
     * @since   1.5
     */
    public static int toTitleCase(int codePoint) {
        return CharacterData.of(codePoint).toTitleCase(codePoint);
    }

    /**
     * 返回指定基数中字符 {@code ch} 的数值。
     * <p>
     * 如果基数不在范围 {@code MIN_RADIX} &le;
     * {@code radix} &le; {@code MAX_RADIX} 内，或者
     * {@code ch} 的值在指定的基数中不是有效的数字，则返回 {@code -1}。一个字符是有效数字，如果以下条件之一为真：
     * <ul>
     * <li>方法 {@code isDigit} 对字符返回 {@code true}
     *     且字符的 Unicode 十进制数字值（或其单字符分解）小于指定的基数。
     *     在这种情况下返回十进制数字值。
     * <li>字符是大写字母
     *     {@code 'A'} 到 {@code 'Z'} 之一，且其代码小于
     *     {@code radix + 'A' - 10}。
     *     在这种情况下，返回 {@code ch - 'A' + 10}。
     * <li>字符是小写字母
     *     {@code 'a'} 到 {@code 'z'} 之一，且其代码小于
     *     {@code radix + 'a' - 10}。
     *     在这种情况下，返回 {@code ch - 'a' + 10}。
     * <li>字符是全角大写字母 A
     *     ({@code '\u005CuFF21'}) 到 Z ({@code '\u005CuFF3A'})
     *     之一，且其代码小于
     *     {@code radix + '\u005CuFF21' - 10}。
     *     在这种情况下，返回 {@code ch - '\u005CuFF21' + 10}。
     * <li>字符是全角小写字母 a
     *     ({@code '\u005CuFF41'}) 到 z ({@code '\u005CuFF5A'})
     *     之一，且其代码小于
     *     {@code radix + '\u005CuFF41' - 10}。
     *     在这种情况下，返回 {@code ch - '\u005CuFF41' + 10}。
     * </ul>
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #digit(int, int)} 方法。
     *
     * @param   ch      要转换的字符。
     * @param   radix   基数。
     * @return  字符在指定基数中表示的数值。
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(char)
     */
    public static int digit(char ch, int radix) {
        return digit((int)ch, radix);
    }


                /**
     * 返回指定字符（Unicode 代码点）在指定基数中的数值。
     *
     * <p>如果基数不在 {@code MIN_RADIX} &le;
     * {@code radix} &le; {@code MAX_RADIX} 范围内，或者
     * 字符在指定基数中不是有效的数字，返回 {@code -1}。字符是有效数字
     * 如果至少满足以下条件之一：
     * <ul>
     * <li>方法 {@link #isDigit(int) isDigit(codePoint)} 对字符返回 {@code true}
     *     且字符（或其单字符分解）的 Unicode 十进制数字值小于指定基数。
     *     在这种情况下返回十进制数字值。
     * <li>字符是大写拉丁字母
     *     {@code 'A'} 到 {@code 'Z'} 且其代码小于
     *     {@code radix + 'A' - 10}。
     *     在这种情况下，返回 {@code codePoint - 'A' + 10}。
     * <li>字符是小写拉丁字母
     *     {@code 'a'} 到 {@code 'z'} 且其代码小于
     *     {@code radix + 'a' - 10}。
     *     在这种情况下，返回 {@code codePoint - 'a' + 10}。
     * <li>字符是全角大写拉丁字母 A
     *     ({@code '\u005CuFF21'}) 到 Z ({@code '\u005CuFF3A'})
     *     且其代码小于
     *     {@code radix + '\u005CuFF21' - 10}。
     *     在这种情况下，
     *     返回 {@code codePoint - '\u005CuFF21' + 10}。
     * <li>字符是全角小写拉丁字母 a
     *     ({@code '\u005CuFF41'}) 到 z ({@code '\u005CuFF5A'})
     *     且其代码小于
     *     {@code radix + '\u005CuFF41'- 10}。
     *     在这种情况下，
     *     返回 {@code codePoint - '\u005CuFF41' + 10}。
     * </ul>
     *
     * @param   codePoint 要转换的字符（Unicode 代码点）。
     * @param   radix   基数。
     * @return  该字符在指定基数中表示的数值。
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(int)
     * @since   1.5
     */
    public static int digit(int codePoint, int radix) {
        return CharacterData.of(codePoint).digit(codePoint, radix);
    }

    /**
     * 返回指定 Unicode 字符表示的 {@code int} 值。例如，字符
     * {@code '\u005Cu216C'}（罗马数字五十）将返回
     * 一个值为 50 的 {@code int}。
     * <p>
     * 大写 ({@code '\u005Cu0041'} 到
     * {@code '\u005Cu005A'})、小写
     * ({@code '\u005Cu0061'} 到 {@code '\u005Cu007A'}) 和
     * 全角变体 ({@code '\u005CuFF21'} 到
     * {@code '\u005CuFF3A'} 和 {@code '\u005CuFF41'} 到
     * {@code '\u005CuFF5A'}) 形式的字母 A-Z 的数值从 10
     * 到 35。这独立于 Unicode 规范，
     * 该规范不为这些 {@code char} 值分配数值。
     * <p>
     * 如果字符没有数值，则返回 -1。
     * 如果字符的数值不能表示为非负整数（例如，分数值），则返回 -2。
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持
     * 所有 Unicode 字符，包括补充字符，使用
     * {@link #getNumericValue(int)} 方法。
     *
     * @param   ch      要转换的字符。
     * @return  字符的数值，作为非负 {@code int}
     *           值；如果字符的数值不是非负整数，则返回 -2；
     *          如果字符没有数值，则返回 -1。
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(char)
     * @since   1.1
     */
    public static int getNumericValue(char ch) {
        return getNumericValue((int)ch);
    }

    /**
     * 返回指定字符（Unicode 代码点）表示的 {@code int} 值。例如，字符
     * {@code '\u005Cu216C'}（罗马数字五十）将返回
     * 一个值为 50 的 {@code int}。
     * <p>
     * 大写 ({@code '\u005Cu0041'} 到
     * {@code '\u005Cu005A'})、小写
     * ({@code '\u005Cu0061'} 到 {@code '\u005Cu007A'}) 和
     * 全角变体 ({@code '\u005CuFF21'} 到
     * {@code '\u005CuFF3A'} 和 {@code '\u005CuFF41'} 到
     * {@code '\u005CuFF5A'}) 形式的字母 A-Z 的数值从 10
     * 到 35。这独立于 Unicode 规范，
     * 该规范不为这些 {@code char} 值分配数值。
     * <p>
     * 如果字符没有数值，则返回 -1。
     * 如果字符的数值不能表示为非负整数（例如，分数值），则返回 -2。
     *
     * @param   codePoint 要转换的字符（Unicode 代码点）。
     * @return  字符的数值，作为非负 {@code int}
     *          值；如果字符的数值不是非负整数，则返回 -2；
     *          如果字符没有数值，则返回 -1。
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(int)
     * @since   1.5
     */
    public static int getNumericValue(int codePoint) {
        return CharacterData.of(codePoint).getNumericValue(codePoint);
    }

    /**
     * 确定指定字符是否为 ISO-LATIN-1 空白字符。
     * 该方法仅对以下五个字符返回 {@code true}：
     * <table summary="truechars">
     * <tr><td>{@code '\t'}</td>            <td>{@code U+0009}</td>
     *     <td>{@code 水平制表符}</td></tr>
     * <tr><td>{@code '\n'}</td>            <td>{@code U+000A}</td>
     *     <td>{@code 新行}</td></tr>
     * <tr><td>{@code '\f'}</td>            <td>{@code U+000C}</td>
     *     <td>{@code 换页符}</td></tr>
     * <tr><td>{@code '\r'}</td>            <td>{@code U+000D}</td>
     *     <td>{@code 回车}</td></tr>
     * <tr><td>{@code ' '}</td>             <td>{@code U+0020}</td>
     *     <td>{@code 空格}</td></tr>
     * </table>
     *
     * @param      ch   要测试的字符。
     * @return     如果字符是 ISO-LATIN-1 空白字符，则返回 {@code true}；
     *             否则返回 {@code false}。
     * @see        Character#isSpaceChar(char)
     * @see        Character#isWhitespace(char)
     * @deprecated 由 isWhitespace(char) 替代。
     */
    @Deprecated
    public static boolean isSpace(char ch) {
        return (ch <= 0x0020) &&
            (((((1L << 0x0009) |
            (1L << 0x000A) |
            (1L << 0x000C) |
            (1L << 0x000D) |
            (1L << 0x0020)) >> ch) & 1L) != 0);
    }


    /**
     * 确定指定字符是否为 Unicode 空白字符。
     * 如果且仅当字符被 Unicode 标准指定为空白字符时，该方法返回 true。此方法在字符的一般类别类型为以下之一时返回 true：
     * <ul>
     * <li> {@code SPACE_SEPARATOR}
     * <li> {@code LINE_SEPARATOR}
     * <li> {@code PARAGRAPH_SEPARATOR}
     * </ul>
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持
     * 所有 Unicode 字符，包括补充字符，使用
     * {@link #isSpaceChar(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  如果字符是空白字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isWhitespace(char)
     * @since   1.1
     */
    public static boolean isSpaceChar(char ch) {
        return isSpaceChar((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为 Unicode 空白字符。如果且仅当字符被 Unicode 标准指定为空白字符时，该方法返回 true。此方法在字符的一般类别类型为以下之一时返回 true：
     *
     * <ul>
     * <li> {@link #SPACE_SEPARATOR}
     * <li> {@link #LINE_SEPARATOR}
     * <li> {@link #PARAGRAPH_SEPARATOR}
     * </ul>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是空白字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isWhitespace(int)
     * @since   1.5
     */
    public static boolean isSpaceChar(int codePoint) {
        return ((((1 << Character.SPACE_SEPARATOR) |
                  (1 << Character.LINE_SEPARATOR) |
                  (1 << Character.PARAGRAPH_SEPARATOR)) >> getType(codePoint)) & 1)
            != 0;
    }

    /**
     * 确定指定字符是否为 Java 空白字符。如果且仅当字符满足以下条件之一时，该字符是 Java 空白字符：
     * <ul>
     * <li> 它是 Unicode 空白字符 ({@code SPACE_SEPARATOR}，
     *      {@code LINE_SEPARATOR}，或 {@code PARAGRAPH_SEPARATOR})
     *      但不是非断行空格 ({@code '\u005Cu00A0'}，
     *      {@code '\u005Cu2007'}， {@code '\u005Cu202F'})。
     * <li> 它是 {@code '\u005Ct'}，U+0009 水平制表符。
     * <li> 它是 {@code '\u005Cn'}，U+000A 新行。
     * <li> 它是 {@code '\u005Cu000B'}，U+000B 垂直制表符。
     * <li> 它是 {@code '\u005Cf'}，U+000C 换页符。
     * <li> 它是 {@code '\u005Cr'}，U+000D 回车。
     * <li> 它是 {@code '\u005Cu001C'}，U+001C 文件分隔符。
     * <li> 它是 {@code '\u005Cu001D'}，U+001D 组分隔符。
     * <li> 它是 {@code '\u005Cu001E'}，U+001E 记录分隔符。
     * <li> 它是 {@code '\u005Cu001F'}，U+001F 单位分隔符。
     * </ul>
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持
     * 所有 Unicode 字符，包括补充字符，使用
     * {@link #isWhitespace(int)} 方法。
     *
     * @param   ch 要测试的字符。
     * @return  如果字符是 Java 空白字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isSpaceChar(char)
     * @since   1.1
     */
    public static boolean isWhitespace(char ch) {
        return isWhitespace((int)ch);
    }

    /**
     * 确定指定字符（Unicode 代码点）是否为 Java 空白字符。如果且仅当字符满足以下条件之一时，该字符是 Java 空白字符：
     * <ul>
     * <li> 它是 Unicode 空白字符 ({@link #SPACE_SEPARATOR}，
     *      {@link #LINE_SEPARATOR}，或 {@link #PARAGRAPH_SEPARATOR})
     *      但不是非断行空格 ({@code '\u005Cu00A0'}，
     *      {@code '\u005Cu2007'}， {@code '\u005Cu202F'})。
     * <li> 它是 {@code '\u005Ct'}，U+0009 水平制表符。
     * <li> 它是 {@code '\u005Cn'}，U+000A 新行。
     * <li> 它是 {@code '\u005Cu000B'}，U+000B 垂直制表符。
     * <li> 它是 {@code '\u005Cf'}，U+000C 换页符。
     * <li> 它是 {@code '\u005Cr'}，U+000D 回车。
     * <li> 它是 {@code '\u005Cu001C'}，U+001C 文件分隔符。
     * <li> 它是 {@code '\u005Cu001D'}，U+001D 组分隔符。
     * <li> 它是 {@code '\u005Cu001E'}，U+001E 记录分隔符。
     * <li> 它是 {@code '\u005Cu001F'}，U+001F 单位分隔符。
     * </ul>
     * <p>
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是 Java 空白字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isSpaceChar(int)
     * @since   1.5
     */
    public static boolean isWhitespace(int codePoint) {
        return CharacterData.of(codePoint).isWhitespace(codePoint);
    }

    /**
     * 确定指定字符是否为 ISO 控制字符。如果字符的代码在范围 {@code '\u005Cu0000'}
     * 到 {@code '\u005Cu001F'} 或在范围
     * {@code '\u005Cu007F'} 到 {@code '\u005Cu009F'} 之间，则认为该字符是 ISO 控制字符。
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持
     * 所有 Unicode 字符，包括补充字符，使用
     * {@link #isISOControl(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  如果字符是 ISO 控制字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     *
     * @see     Character#isSpaceChar(char)
     * @see     Character#isWhitespace(char)
     * @since   1.1
     */
    public static boolean isISOControl(char ch) {
        return isISOControl((int)ch);
    }

    /**
     * 确定引用的字符（Unicode 代码点）是否为 ISO 控制字符。如果字符的代码在范围 {@code '\u005Cu0000'}
     * 到 {@code '\u005Cu001F'} 或在范围
     * {@code '\u005Cu007F'} 到 {@code '\u005Cu009F'} 之间，则认为该字符是 ISO 控制字符。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符是 ISO 控制字符，则返回 {@code true}；
     *          否则返回 {@code false}。
     * @see     Character#isSpaceChar(int)
     * @see     Character#isWhitespace(int)
     * @since   1.5
     */
    public static boolean isISOControl(int codePoint) {
        // 优化形式：
        //     (codePoint >= 0x00 && codePoint <= 0x1F) ||
        //     (codePoint >= 0x7F && codePoint <= 0x9F);
        return codePoint <= 0x9F &&
            (codePoint >= 0x7F || (codePoint >>> 5 == 0));
    }

    /**
     * 返回表示字符一般类别的值。
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持
     * 所有 Unicode 字符，包括补充字符，使用
     * {@link #getType(int)} 方法。
     *
     * @param   ch      要测试的字符。
     * @return  表示字符一般类别的 {@code int} 值。
     * @see     Character#COMBINING_SPACING_MARK
     * @see     Character#CONNECTOR_PUNCTUATION
     * @see     Character#CONTROL
     * @see     Character#CURRENCY_SYMBOL
     * @see     Character#DASH_PUNCTUATION
     * @see     Character#DECIMAL_DIGIT_NUMBER
     * @see     Character#ENCLOSING_MARK
     * @see     Character#END_PUNCTUATION
     * @see     Character#FINAL_QUOTE_PUNCTUATION
     * @see     Character#FORMAT
     * @see     Character#INITIAL_QUOTE_PUNCTUATION
     * @see     Character#LETTER_NUMBER
     * @see     Character#LINE_SEPARATOR
     * @see     Character#LOWERCASE_LETTER
     * @see     Character#MATH_SYMBOL
     * @see     Character#MODIFIER_LETTER
     * @see     Character#MODIFIER_SYMBOL
     * @see     Character#NON_SPACING_MARK
     * @see     Character#OTHER_LETTER
     * @see     Character#OTHER_NUMBER
     * @see     Character#OTHER_PUNCTUATION
     * @see     Character#OTHER_SYMBOL
     * @see     Character#PARAGRAPH_SEPARATOR
     * @see     Character#PRIVATE_USE
     * @see     Character#SPACE_SEPARATOR
     * @see     Character#START_PUNCTUATION
     * @see     Character#SURROGATE
     * @see     Character#TITLECASE_LETTER
     * @see     Character#UNASSIGNED
     * @see     Character#UPPERCASE_LETTER
     * @since   1.1
     */
    public static int getType(char ch) {
        return getType((int)ch);
    }


                /**
     * 返回一个表示字符通用类别的值。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  一个表示字符通用类别的 {@code int} 值。
     * @see     Character#COMBINING_SPACING_MARK COMBINING_SPACING_MARK
     * @see     Character#CONNECTOR_PUNCTUATION CONNECTOR_PUNCTUATION
     * @see     Character#CONTROL CONTROL
     * @see     Character#CURRENCY_SYMBOL CURRENCY_SYMBOL
     * @see     Character#DASH_PUNCTUATION DASH_PUNCTUATION
     * @see     Character#DECIMAL_DIGIT_NUMBER DECIMAL_DIGIT_NUMBER
     * @see     Character#ENCLOSING_MARK ENCLOSING_MARK
     * @see     Character#END_PUNCTUATION END_PUNCTUATION
     * @see     Character#FINAL_QUOTE_PUNCTUATION FINAL_QUOTE_PUNCTUATION
     * @see     Character#FORMAT FORMAT
     * @see     Character#INITIAL_QUOTE_PUNCTUATION INITIAL_QUOTE_PUNCTUATION
     * @see     Character#LETTER_NUMBER LETTER_NUMBER
     * @see     Character#LINE_SEPARATOR LINE_SEPARATOR
     * @see     Character#LOWERCASE_LETTER LOWERCASE_LETTER
     * @see     Character#MATH_SYMBOL MATH_SYMBOL
     * @see     Character#MODIFIER_LETTER MODIFIER_LETTER
     * @see     Character#MODIFIER_SYMBOL MODIFIER_SYMBOL
     * @see     Character#NON_SPACING_MARK NON_SPACING_MARK
     * @see     Character#OTHER_LETTER OTHER_LETTER
     * @see     Character#OTHER_NUMBER OTHER_NUMBER
     * @see     Character#OTHER_PUNCTUATION OTHER_PUNCTUATION
     * @see     Character#OTHER_SYMBOL OTHER_SYMBOL
     * @see     Character#PARAGRAPH_SEPARATOR PARAGRAPH_SEPARATOR
     * @see     Character#PRIVATE_USE PRIVATE_USE
     * @see     Character#SPACE_SEPARATOR SPACE_SEPARATOR
     * @see     Character#START_PUNCTUATION START_PUNCTUATION
     * @see     Character#SURROGATE SURROGATE
     * @see     Character#TITLECASE_LETTER TITLECASE_LETTER
     * @see     Character#UNASSIGNED UNASSIGNED
     * @since   1.5
     */
    public static int getType(int codePoint) {
        return CharacterData.of(codePoint).getType(codePoint);
    }

    /**
     * 确定指定基数中特定数字的字符表示。如果 {@code radix} 的值不是有效的基数，或者 {@code digit} 的值不是指定基数中的有效数字，则返回空字符
     * ({@code '\u005Cu0000'})。
     * <p>
     * 如果 {@code radix} 的值大于或等于 {@code MIN_RADIX} 且小于或等于
     * {@code MAX_RADIX}，则该值有效。如果 {@code 0 <= digit < radix}，则 {@code digit} 的值有效。
     * <p>
     * 如果数字小于 10，则返回 {@code '0' + digit}。否则，返回 {@code 'a' + digit - 10}。
     *
     * @param   digit   要转换为字符的数字。
     * @param   radix   基数。
     * @return  指定基数中指定数字的 {@code char} 表示。
     * @see     Character#MIN_RADIX
     * @see     Character#MAX_RADIX
     * @see     Character#digit(char, int)
     */
    public static char forDigit(int digit, int radix) {
        if ((digit >= radix) || (digit < 0)) {
            return '\0';
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            return '\0';
        }
        if (digit < 10) {
            return (char)('0' + digit);
        }
        return (char)('a' - 10 + digit);
    }

    /**
     * 返回给定字符的 Unicode 方向性属性。字符方向性用于计算文本的视觉顺序。未定义的 {@code char} 值的方向性值为 {@code DIRECTIONALITY_UNDEFINED}。
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #getDirectionality(int)} 方法。
     *
     * @param  ch 请求方向性属性的 {@code char}。
     * @return 请求的 {@code char} 值的方向性属性。
     *
     * @see Character#DIRECTIONALITY_UNDEFINED
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR
     * @see Character#DIRECTIONALITY_ARABIC_NUMBER
     * @see Character#DIRECTIONALITY_COMMON_NUMBER_SEPARATOR
     * @see Character#DIRECTIONALITY_NONSPACING_MARK
     * @see Character#DIRECTIONALITY_BOUNDARY_NEUTRAL
     * @see Character#DIRECTIONALITY_PARAGRAPH_SEPARATOR
     * @see Character#DIRECTIONALITY_SEGMENT_SEPARATOR
     * @see Character#DIRECTIONALITY_WHITESPACE
     * @see Character#DIRECTIONALITY_OTHER_NEUTRALS
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
     * @see Character#DIRECTIONALITY_POP_DIRECTIONAL_FORMAT
     * @since 1.4
     */
    public static byte getDirectionality(char ch) {
        return getDirectionality((int)ch);
    }

    /**
     * 返回给定字符（Unicode 代码点）的 Unicode 方向性属性。字符方向性用于计算文本的视觉顺序。未定义字符的方向性值为 {@link
     * #DIRECTIONALITY_UNDEFINED}。
     *
     * @param   codePoint 请求方向性属性的字符（Unicode 代码点）。
     * @return 请求的字符的方向性属性。
     *
     * @see Character#DIRECTIONALITY_UNDEFINED DIRECTIONALITY_UNDEFINED
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT DIRECTIONALITY_LEFT_TO_RIGHT
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT DIRECTIONALITY_RIGHT_TO_LEFT
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER DIRECTIONALITY_EUROPEAN_NUMBER
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR
     * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR
     * @see Character#DIRECTIONALITY_ARABIC_NUMBER DIRECTIONALITY_ARABIC_NUMBER
     * @see Character#DIRECTIONALITY_COMMON_NUMBER_SEPARATOR DIRECTIONALITY_COMMON_NUMBER_SEPARATOR
     * @see Character#DIRECTIONALITY_NONSPACING_MARK DIRECTIONALITY_NONSPACING_MARK
     * @see Character#DIRECTIONALITY_BOUNDARY_NEUTRAL DIRECTIONALITY_BOUNDARY_NEUTRAL
     * @see Character#DIRECTIONALITY_PARAGRAPH_SEPARATOR DIRECTIONALITY_PARAGRAPH_SEPARATOR
     * @see Character#DIRECTIONALITY_SEGMENT_SEPARATOR DIRECTIONALITY_SEGMENT_SEPARATOR
     * @see Character#DIRECTIONALITY_WHITESPACE DIRECTIONALITY_WHITESPACE
     * @see Character#DIRECTIONALITY_OTHER_NEUTRALS DIRECTIONALITY_OTHER_NEUTRALS
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING
     * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
     * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
     * @see Character#DIRECTIONALITY_POP_DIRECTIONAL_FORMAT DIRECTIONALITY_POP_DIRECTIONAL_FORMAT
     * @since    1.5
     */
    public static byte getDirectionality(int codePoint) {
        return CharacterData.of(codePoint).getDirectionality(codePoint);
    }

    /**
     * 根据 Unicode 规范确定字符是否镜像。镜像字符在右到左的文本中显示时，其字形应水平镜像。例如，{@code '\u005Cu0028'} 左括号在左到右的文本中显示为 "(", 但在右到左的文本中显示为 ")"。
     *
     * <p><b>注意：</b>此方法不能处理 <a
     * href="#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用
     * {@link #isMirrored(int)} 方法。
     *
     * @param  ch 请求镜像属性的 {@code char}。
     * @return 如果字符镜像，则返回 {@code true}；如果字符不镜像或未定义，则返回 {@code false}。
     * @since 1.4
     */
    public static boolean isMirrored(char ch) {
        return isMirrored((int)ch);
    }

    /**
     * 根据 Unicode 规范确定指定字符（Unicode 代码点）是否镜像。镜像字符在右到左的文本中显示时，其字形应水平镜像。例如，{@code '\u005Cu0028'} 左括号在左到右的文本中显示为 "(", 但在右到左的文本中显示为 ")"。
     *
     * @param   codePoint 要测试的字符（Unicode 代码点）。
     * @return  如果字符镜像，则返回 {@code true}；如果字符不镜像或未定义，则返回 {@code false}。
     * @since   1.5
     */
    public static boolean isMirrored(int codePoint) {
        return CharacterData.of(codePoint).isMirrored(codePoint);
    }

    /**
     * 按数值比较两个 {@code Character} 对象。
     *
     * @param   anotherCharacter   要比较的 {@code Character}。
     * @return  如果参数 {@code Character} 等于此 {@code Character}，则返回值为 {@code 0}；如果此 {@code Character} 数值上小于参数 {@code Character}，则返回值小于 {@code 0}；如果此 {@code Character} 数值上大于参数 {@code Character}，则返回值大于 {@code 0}（无符号比较）。注意，这完全是数值比较；与区域设置无关。
     * @since   1.2
     */
    public int compareTo(Character anotherCharacter) {
        return compare(this.value, anotherCharacter.value);
    }

    /**
     * 按数值比较两个 {@code char} 值。返回的值与以下表达式返回的值相同：
     * <pre>
     *    Character.valueOf(x).compareTo(Character.valueOf(y))
     * </pre>
     *
     * @param  x 要比较的第一个 {@code char}。
     * @param  y 要比较的第二个 {@code char}。
     * @return 如果 {@code x == y}，则返回值为 {@code 0}；如果 {@code x < y}，则返回值小于 {@code 0}；如果 {@code x > y}，则返回值大于 {@code 0}。
     * @since 1.7
     */
    public static int compare(char x, char y) {
        return x - y;
    }

    /**
     * 使用 UnicodeData 文件中的信息将字符（Unicode 代码点）参数转换为大写。
     * <p>
     *
     * @param   codePoint   要转换的字符（Unicode 代码点）。
     * @return  如果存在，则返回字符的大写形式，否则返回错误标志 ({@code Character.ERROR})
     *          表示存在 1:M {@code char} 映射。
     * @see     Character#isLowerCase(char)
     * @see     Character#isUpperCase(char)
     * @see     Character#toLowerCase(char)
     * @see     Character#toTitleCase(char)
     * @since 1.4
     */
    static int toUpperCaseEx(int codePoint) {
        assert isValidCodePoint(codePoint);
        return CharacterData.of(codePoint).toUpperCaseEx(codePoint);
    }

    /**
     * 使用 Unicode 规范中的 SpecialCasing 文件中的大小写映射信息将字符（Unicode 代码点）参数转换为大写。如果字符没有显式的大写映射，则返回字符本身。
     *
     * @param   codePoint   要转换的字符（Unicode 代码点）。
     * @return  包含大写字符的 {@code char[]}。
     * @since 1.4
     */
    static char[] toUpperCaseCharArray(int codePoint) {
        // 截至 Unicode 6.0，1:M 大写转换仅发生在 BMP 中。
        assert isBmpCodePoint(codePoint);
        return CharacterData.of(codePoint).toUpperCaseCharArray(codePoint);
    }

    /**
     * 用于表示 <tt>char</tt> 值在无符号二进制形式中的位数，常量 {@code 16}。
     *
     * @since 1.5
     */
    public static final int SIZE = 16;

    /**
     * 用于表示 {@code char} 值在无符号二进制形式中的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回通过反转指定 <tt>char</tt> 值的字节顺序得到的值。
     *
     * @param ch 要反转字节顺序的 {@code char}。
     * @return 通过反转（或等效地交换）指定 <tt>char</tt> 值的字节顺序得到的值。
     * @since 1.5
     */
    public static char reverseBytes(char ch) {
        return (char) (((ch & 0xFF00) >> 8) | (ch << 8));
    }

    /**
     * 返回指定字符 {@code codePoint} 的 Unicode 名称，如果代码点未分配，则返回 null。
     * <p>
     * 注意：如果指定字符未被 <i>UnicodeData</i> 文件（Unicode 联盟维护的 Unicode 字符数据库的一部分）分配名称，则返回的名称与以下表达式的结果相同。
     *
     * <blockquote>{@code
     *     Character.UnicodeBlock.of(codePoint).toString().replace('_', ' ')
     *     + " "
     *     + Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
     *
     * }</blockquote>
     *
     * @param  codePoint 字符（Unicode 代码点）。
     * @return 指定字符的 Unicode 名称，如果代码点未分配，则返回 null。
     * @exception IllegalArgumentException 如果指定的 {@code codePoint} 不是有效的 Unicode 代码点。
     * @since 1.7
     */
    public static String getName(int codePoint) {
        if (!isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException();
        }
        String name = CharacterName.get(codePoint);
        if (name != null)
            return name;
        if (getType(codePoint) == UNASSIGNED)
            return null;
        UnicodeBlock block = UnicodeBlock.of(codePoint);
        if (block != null)
            return block.toString().replace('_', ' ') + " "
                   + Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
        // 应该永远不会到这里
        return Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
    }
}
