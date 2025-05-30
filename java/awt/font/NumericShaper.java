
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.font;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

/**
 * <code>NumericShaper</code> 类用于将 Latin-1（欧洲）数字转换为其他 Unicode 十进制数字。使用此类的用户主要是希望在呈现数据时使用国家数字形状，但发现使用 Latin-1（欧洲）数字表示数据更为方便。此功能不解释已废弃的数字形状选择字符（U+206E）。
 * <p>
 * <code>NumericShaper</code> 的实例通常作为 <code>TextAttribute</code> 类的 <code>NUMERIC_SHAPING</code> 属性应用于文本。例如，以下代码片段会使 <code>TextLayout</code> 在阿拉伯语上下文中将欧洲数字转换为阿拉伯数字：<br>
 * <blockquote><pre>
 * Map map = new HashMap();
 * map.put(TextAttribute.NUMERIC_SHAPING,
 *     NumericShaper.getContextualShaper(NumericShaper.ARABIC));
 * FontRenderContext frc = ...;
 * TextLayout layout = new TextLayout(text, map, frc);
 * layout.draw(g2d, x, y);
 * </pre></blockquote>
 * <br>
 * 也可以显式地使用 <code>NumericShaper</code> 的实例进行数字整形，如下代码片段所示：<br>
 * <blockquote><pre>
 * char[] text = ...;
 * // 将所有欧洲数字（零除外）转换为阿拉伯数字
 * NumericShaper shaper = NumericShaper.getShaper(NumericShaper.ARABIC);
 * shaper.shape(text, start, count);
 *
 * // 如果前文是阿拉伯文，则将欧洲数字转换为阿拉伯数字；如果前文是泰米尔文，则将欧洲数字转换为泰米尔数字；如果没有前文或前文既不是阿拉伯文也不是泰米尔文，则保留欧洲数字
 * NumericShaper shaper =
 *     NumericShaper.getContextualShaper(NumericShaper.ARABIC |
 *                                         NumericShaper.TAMIL,
 *                                       NumericShaper.EUROPEAN);
 * shaper.shape(text, start, count);
 * </pre></blockquote>
 *
 * <p><b>位掩码和枚举基础的 Unicode 范围</b></p>
 *
 * <p>此类支持两种不同的编程接口来表示特定脚本的数字的 Unicode 范围：位掩码基础的，如 {@link #ARABIC NumericShaper.ARABIC}，和枚举基础的，如 {@link NumericShaper.Range#ARABIC}。可以通过 OR 运算符组合多个位掩码基础的常量，例如：
 * <blockquote><pre>
 * NumericShaper.ARABIC | NumericShaper.TAMIL
 * </pre></blockquote>
 * 或者创建一个包含 {@link NumericShaper.Range} 常量的 {@code Set}，例如：
 * <blockquote><pre>
 * EnumSet.of(NumericShaper.Scirpt.ARABIC, NumericShaper.Range.TAMIL)
 * </pre></blockquote>
 * 枚举基础的范围是位掩码基础的范围的超集。
 *
 * <p>如果混合使用两种接口（包括序列化），Unicode 范围值将被映射到其对应的值，如果可能的话，例如从 {@code NumericShaper.Range.ARABIC} 映射到 {@code NumericShaper.ARABIC}。如果指定了任何无法映射的范围值，例如 {@code NumericShaper.Range.BALINESE}，这些范围将被忽略。
 *
 * <p><b>十进制数字优先级</b></p>
 *
 * <p>一个 Unicode 范围可能有多个十进制数字集。如果为同一个 Unicode 范围指定了多个十进制数字集，其中一个集将具有优先级，如下所示。
 *
 * <table border=1 cellspacing=3 cellpadding=0 summary="NumericShaper constants precedence.">
 *    <tr>
 *       <th class="TableHeadingColor">Unicode 范围</th>
 *       <th class="TableHeadingColor"><code>NumericShaper</code> 常量</th>
 *       <th class="TableHeadingColor">优先级</th>
 *    </tr>
 *    <tr>
 *       <td rowspan="2">阿拉伯文</td>
 *       <td>{@link NumericShaper#ARABIC NumericShaper.ARABIC}<br>
 *           {@link NumericShaper#EASTERN_ARABIC NumericShaper.EASTERN_ARABIC}</td>
 *       <td>{@link NumericShaper#EASTERN_ARABIC NumericShaper.EASTERN_ARABIC}</td>
 *    </tr>
 *    <tr>
 *       <td>{@link NumericShaper.Range#ARABIC}<br>
 *           {@link NumericShaper.Range#EASTERN_ARABIC}</td>
 *       <td>{@link NumericShaper.Range#EASTERN_ARABIC}</td>
 *    </tr>
 *    <tr>
 *       <td>泰文</td>
 *       <td>{@link NumericShaper.Range#TAI_THAM_HORA}<br>
 *           {@link NumericShaper.Range#TAI_THAM_THAM}</td>
 *       <td>{@link NumericShaper.Range#TAI_THAM_THAM}</td>
 *    </tr>
 * </table>
 *
 * @since 1.4
 */

public final class NumericShaper implements java.io.Serializable {
    /**
     * {@code NumericShaper.Range} 表示一个具有自己十进制数字的脚本的 Unicode 范围。例如，{@link
     * NumericShaper.Range#THAI} 范围包含泰文数字，从 THAI DIGIT ZERO (U+0E50) 到 THAI DIGIT NINE (U+0E59)。
     *
     * <p>{@code Range} 枚举取代了传统的位掩码基础的值（例如 {@link NumericShaper#ARABIC}），并支持比位掩码基础的值更多的 Unicode 范围。例如，以下使用位掩码的代码：
     * <blockquote><pre>
     * NumericShaper.getContextualShaper(NumericShaper.ARABIC |
     *                                     NumericShaper.TAMIL,
     *                                   NumericShaper.EUROPEAN);
     * </pre></blockquote>
     * 可以使用此枚举重写为：
     * <blockquote><pre>
     * NumericShaper.getContextualShaper(EnumSet.of(
     *                                     NumericShaper.Range.ARABIC,
     *                                     NumericShaper.Range.TAMIL),
     *                                   NumericShaper.Range.EUROPEAN);
     * </pre></blockquote>
     *
     * @since 1.7
     */
    public static enum Range {
        // EUROPEAN 到 MOGOLIAN 的顺序必须与位掩码基础的常量一致
        /**
         * 拉丁（欧洲）范围，包含拉丁（ASCII）数字。
         */
        EUROPEAN        ('\u0030', '\u0000', '\u0300'),
        /**
         * 阿拉伯范围，包含阿拉伯-印度数字。
         */
        ARABIC          ('\u0660', '\u0600', '\u0780'),
        /**
         * 阿拉伯范围，包含东阿拉伯-印度数字。
         */
        EASTERN_ARABIC  ('\u06f0', '\u0600', '\u0780'),
        /**
         * 印地范围，包含印地数字。
         */
        DEVANAGARI      ('\u0966', '\u0900', '\u0980'),
        /**
         * 孟加拉范围，包含孟加拉数字。
         */
        BENGALI         ('\u09e6', '\u0980', '\u0a00'),
        /**
         * 古鲁木基范围，包含古鲁木基数字。
         */
        GURMUKHI        ('\u0a66', '\u0a00', '\u0a80'),
        /**
         * 古吉拉特范围，包含古吉拉特数字。
         */
        GUJARATI        ('\u0ae6', '\u0b00', '\u0b80'),
        /**
         * 奥里亚范围，包含奥里亚数字。
         */
        ORIYA           ('\u0b66', '\u0b00', '\u0b80'),
        /**
         * 泰米尔范围，包含泰米尔数字。
         */
        TAMIL           ('\u0be6', '\u0b80', '\u0c00'),
        /**
         * 泰卢固范围，包含泰卢固数字。
         */
        TELUGU          ('\u0c66', '\u0c00', '\u0c80'),
        /**
         * 卡纳达范围，包含卡纳达数字。
         */
        KANNADA         ('\u0ce6', '\u0c80', '\u0d00'),
        /**
         * 马拉雅拉姆范围，包含马拉雅拉姆数字。
         */
        MALAYALAM       ('\u0d66', '\u0d00', '\u0d80'),
        /**
         * 泰范围，包含泰数字。
         */
        THAI            ('\u0e50', '\u0e00', '\u0e80'),
        /**
         * 老挝范围，包含老挝数字。
         */
        LAO             ('\u0ed0', '\u0e80', '\u0f00'),
        /**
         * 藏范围，包含藏数字。
         */
        TIBETAN         ('\u0f20', '\u0f00', '\u1000'),
        /**
         * 缅甸范围，包含缅甸数字。
         */
        MYANMAR         ('\u1040', '\u1000', '\u1080'),
        /**
         * 埃塞俄比亚范围，包含埃塞俄比亚数字。埃塞俄比亚没有十进制数字 0，因此使用拉丁（欧洲）0。
         */
        ETHIOPIC        ('\u1369', '\u1200', '\u1380') {
            @Override
            char getNumericBase() { return 1; }
        },
        /**
         * 高棉范围，包含高棉数字。
         */
        KHMER           ('\u17e0', '\u1780', '\u1800'),
        /**
         * 蒙古范围，包含蒙古数字。
         */
        MONGOLIAN       ('\u1810', '\u1800', '\u1900'),
        // EUROPEAN 到 MOGOLIAN 的顺序必须与位掩码基础的常量一致。

        /**
         * N'Ko 范围，包含 N'Ko 数字。
         */
        NKO             ('\u07c0', '\u07c0', '\u0800'),
        /**
         * 缅甸范围，包含缅甸掸族数字。
         */
        MYANMAR_SHAN    ('\u1090', '\u1000', '\u10a0'),
        /**
         * 利姆布范围，包含利姆布数字。
         */
        LIMBU           ('\u1946', '\u1900', '\u1950'),
        /**
         * 新傣仂范围，包含新傣仂数字。
         */
        NEW_TAI_LUE     ('\u19d0', '\u1980', '\u19e0'),
        /**
         * 巴厘范围，包含巴厘数字。
         */
        BALINESE        ('\u1b50', '\u1b00', '\u1b80'),
        /**
         * 苏丹范围，包含苏丹数字。
         */
        SUNDANESE       ('\u1bb0', '\u1b80', '\u1bc0'),
        /**
         * 莱普查范围，包含莱普查数字。
         */
        LEPCHA          ('\u1c40', '\u1c00', '\u1c50'),
        /**
         * 奥尔奇基范围，包含奥尔奇基数字。
         */
        OL_CHIKI        ('\u1c50', '\u1c50', '\u1c80'),
        /**
         * 瓦伊范围，包含瓦伊数字。
         */
        VAI             ('\ua620', '\ua500', '\ua640'),
        /**
         * 萨拉什特拉范围，包含萨拉什特拉数字。
         */
        SAURASHTRA      ('\ua8d0', '\ua880', '\ua8e0'),
        /**
         * 卡亚利范围，包含卡亚利数字。
         */
        KAYAH_LI        ('\ua900', '\ua900', '\ua930'),
        /**
         * 蔑蒙范围，包含蔑蒙数字。
         */
        CHAM            ('\uaa50', '\uaa00', '\uaa60'),
        /**
         * 泰坦霍拉范围，包含泰坦霍拉数字。
         */
        TAI_THAM_HORA   ('\u1a80', '\u1a20', '\u1ab0'),
        /**
         * 泰坦塔姆范围，包含泰坦塔姆数字。
         */
        TAI_THAM_THAM   ('\u1a90', '\u1a20', '\u1ab0'),
        /**
         * 爪哇范围，包含爪哇数字。
         */
        JAVANESE        ('\ua9d0', '\ua980', '\ua9e0'),
        /**
         * 米特梅伊范围，包含米特梅伊数字。
         */
        MEETEI_MAYEK    ('\uabf0', '\uabc0', '\uac00');

        private static int toRangeIndex(Range script) {
            int index = script.ordinal();
            return index < NUM_KEYS ? index : -1;
        }

        private static Range indexToRange(int index) {
            return index < NUM_KEYS ? Range.values()[index] : null;
        }

        private static int toRangeMask(Set<Range> ranges) {
            int m = 0;
            for (Range range : ranges) {
                int index = range.ordinal();
                if (index < NUM_KEYS) {
                    m |= 1 << index;
                }
            }
            return m;
        }

        private static Set<Range> maskToRangeSet(int mask) {
            Set<Range> set = EnumSet.noneOf(Range.class);
            Range[] a = Range.values();
            for (int i = 0; i < NUM_KEYS; i++) {
                if ((mask & (1 << i)) != 0) {
                    set.add(a[i]);
                }
            }
            return set;
        }

        // 范围数字的基字符
        private final int base;
        // Unicode 范围
        private final int start, // 包含
                          end;   // 不包含

        private Range(int base, int start, int end) {
            this.base = base - ('0' + getNumericBase());
            this.start = start;
            this.end = end;
        }

        private int getDigitBase() {
            return base;
        }

        char getNumericBase() {
            return 0;
        }

        private boolean inRange(int c) {
            return start <= c && c < end;
        }
    }

    /** 用于上下文整形的上下文索引 - 值范围从 0 到 18 */
    private int key;

    /** 标志，指示是否上下文整形（最高位）以及要整形的数字范围（位 0-18） */
    private int mask;

    /**
     * 用于上下文整形的上下文 {@code Range} 或用于非上下文整形的 {@code Range}。位掩码基础 API 为 {@code null}。
     *
     * @since 1.7
     */
    private Range shapingRange;

    /**
     * 指示要整形的 Unicode 范围的 {@code Set<Range>}。位掩码基础 API 为 {@code null}。
     */
    private transient Set<Range> rangeSet;

    /**
     * rangeSet.toArray() 的值。当元素数量大于 BSEARCH_THRESHOLD 时，按 Range.base 排序。
     */
    private transient Range[] rangeArray;

    /**
     * 如果指定的范围数量大于 BSEARCH_THRESHOLD，则使用二分查找。
     */
    private static final int BSEARCH_THRESHOLD = 3;

    private static final long serialVersionUID = -8022764705923730308L;

    /** 标识 Latin-1（欧洲）和扩展范围，以及 Latin-1（欧洲）十进制基 */
    public static final int EUROPEAN = 1<<0;

    /** 标识 ARABIC 范围和十进制基。 */
    public static final int ARABIC = 1<<1;

    /** 标识 ARABIC 范围和 ARABIC_EXTENDED 十进制基。 */
    public static final int EASTERN_ARABIC = 1<<2;

    /** 标识 DEVANAGARI 范围和十进制基。 */
    public static final int DEVANAGARI = 1<<3;


                /** Identifies the BENGALI range and decimal base. */
    public static final int BENGALI = 1<<4;

    /** Identifies the GURMUKHI range and decimal base. */
    public static final int GURMUKHI = 1<<5;

    /** Identifies the GUJARATI range and decimal base. */
    public static final int GUJARATI = 1<<6;

    /** Identifies the ORIYA range and decimal base. */
    // TAMIL DIGIT ZERO was added in Unicode 4.1
    public static final int ORIYA = 1<<7;

    /** Identifies the TAMIL range and decimal base. */
    public static final int TAMIL = 1<<8;

    /** Identifies the TELUGU range and decimal base. */
    public static final int TELUGU = 1<<9;

    /** Identifies the KANNADA range and decimal base. */
    public static final int KANNADA = 1<<10;

    /** Identifies the MALAYALAM range and decimal base. */
    public static final int MALAYALAM = 1<<11;

    /** Identifies the THAI range and decimal base. */
    public static final int THAI = 1<<12;

    /** Identifies the LAO range and decimal base. */
    public static final int LAO = 1<<13;

    /** Identifies the TIBETAN range and decimal base. */
    public static final int TIBETAN = 1<<14;

    /** Identifies the MYANMAR range and decimal base. */
    public static final int MYANMAR = 1<<15;

    /** Identifies the ETHIOPIC range and decimal base. */
    public static final int ETHIOPIC = 1<<16;

    /** Identifies the KHMER range and decimal base. */
    public static final int KHMER = 1<<17;

    /** Identifies the MONGOLIAN range and decimal base. */
    public static final int MONGOLIAN = 1<<18;

    /** Identifies all ranges, for full contextual shaping.
     *
     * <p>此常量指定了所有基于位掩码的范围。使用 {@code EmunSet.allOf(NumericShaper.Range.class)} 来
     * 指定所有基于枚举的范围。
     */
    public static final int ALL_RANGES = 0x0007ffff;

    private static final int EUROPEAN_KEY = 0;
    private static final int ARABIC_KEY = 1;
    private static final int EASTERN_ARABIC_KEY = 2;
    private static final int DEVANAGARI_KEY = 3;
    private static final int BENGALI_KEY = 4;
    private static final int GURMUKHI_KEY = 5;
    private static final int GUJARATI_KEY = 6;
    private static final int ORIYA_KEY = 7;
    private static final int TAMIL_KEY = 8;
    private static final int TELUGU_KEY = 9;
    private static final int KANNADA_KEY = 10;
    private static final int MALAYALAM_KEY = 11;
    private static final int THAI_KEY = 12;
    private static final int LAO_KEY = 13;
    private static final int TIBETAN_KEY = 14;
    private static final int MYANMAR_KEY = 15;
    private static final int ETHIOPIC_KEY = 16;
    private static final int KHMER_KEY = 17;
    private static final int MONGOLIAN_KEY = 18;

    private static final int NUM_KEYS = MONGOLIAN_KEY + 1; // fixed

    private static final int CONTEXTUAL_MASK = 1<<31;

    private static final char[] bases = {
        '\u0030' - '\u0030', // EUROPEAN
        '\u0660' - '\u0030', // ARABIC-INDIC
        '\u06f0' - '\u0030', // EXTENDED ARABIC-INDIC (EASTERN_ARABIC)
        '\u0966' - '\u0030', // DEVANAGARI
        '\u09e6' - '\u0030', // BENGALI
        '\u0a66' - '\u0030', // GURMUKHI
        '\u0ae6' - '\u0030', // GUJARATI
        '\u0b66' - '\u0030', // ORIYA
        '\u0be6' - '\u0030', // TAMIL - zero was added in Unicode 4.1
        '\u0c66' - '\u0030', // TELUGU
        '\u0ce6' - '\u0030', // KANNADA
        '\u0d66' - '\u0030', // MALAYALAM
        '\u0e50' - '\u0030', // THAI
        '\u0ed0' - '\u0030', // LAO
        '\u0f20' - '\u0030', // TIBETAN
        '\u1040' - '\u0030', // MYANMAR
        '\u1369' - '\u0031', // ETHIOPIC - no zero
        '\u17e0' - '\u0030', // KHMER
        '\u1810' - '\u0030', // MONGOLIAN
    };

    // 一些范围相邻或重叠，重新考虑是否要对此进行二分查找

    private static final char[] contexts = {
        '\u0000', '\u0300', // 'EUROPEAN' (实际上是指 latin-1 和扩展)
        '\u0600', '\u0780', // ARABIC
        '\u0600', '\u0780', // EASTERN_ARABIC -- 注意与阿拉伯语重叠
        '\u0900', '\u0980', // DEVANAGARI
        '\u0980', '\u0a00', // BENGALI
        '\u0a00', '\u0a80', // GURMUKHI
        '\u0a80', '\u0b00', // GUJARATI
        '\u0b00', '\u0b80', // ORIYA
        '\u0b80', '\u0c00', // TAMIL
        '\u0c00', '\u0c80', // TELUGU
        '\u0c80', '\u0d00', // KANNADA
        '\u0d00', '\u0d80', // MALAYALAM
        '\u0e00', '\u0e80', // THAI
        '\u0e80', '\u0f00', // LAO
        '\u0f00', '\u1000', // TIBETAN
        '\u1000', '\u1080', // MYANMAR
        '\u1200', '\u1380', // ETHIOPIC - 注意缺少零
        '\u1780', '\u1800', // KHMER
        '\u1800', '\u1900', // MONGOLIAN
        '\uffff',
    };

    // 假设大多数字符彼此相邻，因此缓存的探测频率较低，
    // 线性探测是可以接受的。

    private static int ctCache = 0;
    private static int ctCacheLimit = contexts.length - 2;

    // 警告，同步访问此方法，因为它修改了状态
    private static int getContextKey(char c) {
        if (c < contexts[ctCache]) {
            while (ctCache > 0 && c < contexts[ctCache]) --ctCache;
        } else if (c >= contexts[ctCache + 1]) {
            while (ctCache < ctCacheLimit && c >= contexts[ctCache + 1]) ++ctCache;
        }

        // 如果不在已知范围内，则返回 EUROPEAN 作为范围键
        return (ctCache & 0x1) == 0 ? (ctCache / 2) : EUROPEAN_KEY;
    }

    // 用于 NumericShaper.Range 版本的缓存
    private transient volatile Range currentRange = Range.EUROPEAN;

    private Range rangeForCodePoint(final int codepoint) {
        if (currentRange.inRange(codepoint)) {
            return currentRange;
        }

        final Range[] ranges = rangeArray;
        if (ranges.length > BSEARCH_THRESHOLD) {
            int lo = 0;
            int hi = ranges.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                Range range = ranges[mid];
                if (codepoint < range.start) {
                    hi = mid - 1;
                } else if (codepoint >= range.end) {
                    lo = mid + 1;
                } else {
                    currentRange = range;
                    return range;
                }
            }
        } else {
            for (int i = 0; i < ranges.length; i++) {
                if (ranges[i].inRange(codepoint)) {
                    return ranges[i];
                }
            }
        }
        return Range.EUROPEAN;
    }

    /*
     * 强方向字符（类型 L, R, AL）的范围表。
     * 偶数（左）索引是非强方向字符（或未定义）的范围的开始，
     * 奇数（右）索引是强方向字符的范围的开始。
     */
    private static int[] strongTable = {
        0x0000, 0x0041,
        0x005b, 0x0061,
        0x007b, 0x00aa,
        0x00ab, 0x00b5,
        0x00b6, 0x00ba,
        0x00bb, 0x00c0,
        0x00d7, 0x00d8,
        0x00f7, 0x00f8,
        0x02b9, 0x02bb,
        0x02c2, 0x02d0,
        0x02d2, 0x02e0,
        0x02e5, 0x02ee,
        0x02ef, 0x0370,
        0x0374, 0x0376,
        0x037e, 0x0386,
        0x0387, 0x0388,
        0x03f6, 0x03f7,
        0x0483, 0x048a,
        0x058a, 0x05be,
        0x05bf, 0x05c0,
        0x05c1, 0x05c3,
        0x05c4, 0x05c6,
        0x05c7, 0x05d0,
        0x0600, 0x0608,
        0x0609, 0x060b,
        0x060c, 0x060d,
        0x060e, 0x061b,
        0x064b, 0x066d,
        0x0670, 0x0671,
        0x06d6, 0x06e5,
        0x06e7, 0x06ee,
        0x06f0, 0x06fa,
        0x0711, 0x0712,
        0x0730, 0x074d,
        0x07a6, 0x07b1,
        0x07eb, 0x07f4,
        0x07f6, 0x07fa,
        0x0816, 0x081a,
        0x081b, 0x0824,
        0x0825, 0x0828,
        0x0829, 0x0830,
        0x0859, 0x085e,
        0x08e4, 0x0903,
        0x093a, 0x093b,
        0x093c, 0x093d,
        0x0941, 0x0949,
        0x094d, 0x094e,
        0x0951, 0x0958,
        0x0962, 0x0964,
        0x0981, 0x0982,
        0x09bc, 0x09bd,
        0x09c1, 0x09c7,
        0x09cd, 0x09ce,
        0x09e2, 0x09e6,
        0x09f2, 0x09f4,
        0x09fb, 0x0a03,
        0x0a3c, 0x0a3e,
        0x0a41, 0x0a59,
        0x0a70, 0x0a72,
        0x0a75, 0x0a83,
        0x0abc, 0x0abd,
        0x0ac1, 0x0ac9,
        0x0acd, 0x0ad0,
        0x0ae2, 0x0ae6,
        0x0af1, 0x0b02,
        0x0b3c, 0x0b3d,
        0x0b3f, 0x0b40,
        0x0b41, 0x0b47,
        0x0b4d, 0x0b57,
        0x0b62, 0x0b66,
        0x0b82, 0x0b83,
        0x0bc0, 0x0bc1,
        0x0bcd, 0x0bd0,
        0x0bf3, 0x0c01,
        0x0c3e, 0x0c41,
        0x0c46, 0x0c58,
        0x0c62, 0x0c66,
        0x0c78, 0x0c7f,
        0x0cbc, 0x0cbd,
        0x0ccc, 0x0cd5,
        0x0ce2, 0x0ce6,
        0x0d41, 0x0d46,
        0x0d4d, 0x0d4e,
        0x0d62, 0x0d66,
        0x0dca, 0x0dcf,
        0x0dd2, 0x0dd8,
        0x0e31, 0x0e32,
        0x0e34, 0x0e40,
        0x0e47, 0x0e4f,
        0x0eb1, 0x0eb2,
        0x0eb4, 0x0ebd,
        0x0ec8, 0x0ed0,
        0x0f18, 0x0f1a,
        0x0f35, 0x0f36,
        0x0f37, 0x0f38,
        0x0f39, 0x0f3e,
        0x0f71, 0x0f7f,
        0x0f80, 0x0f85,
        0x0f86, 0x0f88,
        0x0f8d, 0x0fbe,
        0x0fc6, 0x0fc7,
        0x102d, 0x1031,
        0x1032, 0x1038,
        0x1039, 0x103b,
        0x103d, 0x103f,
        0x1058, 0x105a,
        0x105e, 0x1061,
        0x1071, 0x1075,
        0x1082, 0x1083,
        0x1085, 0x1087,
        0x108d, 0x108e,
        0x109d, 0x109e,
        0x135d, 0x1360,
        0x1390, 0x13a0,
        0x1400, 0x1401,
        0x1680, 0x1681,
        0x169b, 0x16a0,
        0x1712, 0x1720,
        0x1732, 0x1735,
        0x1752, 0x1760,
        0x1772, 0x1780,
        0x17b4, 0x17b6,
        0x17b7, 0x17be,
        0x17c6, 0x17c7,
        0x17c9, 0x17d4,
        0x17db, 0x17dc,
        0x17dd, 0x17e0,
        0x17f0, 0x1810,
        0x18a9, 0x18aa,
        0x1920, 0x1923,
        0x1927, 0x1929,
        0x1932, 0x1933,
        0x1939, 0x1946,
        0x19de, 0x1a00,
        0x1a17, 0x1a19,
        0x1a56, 0x1a57,
        0x1a58, 0x1a61,
        0x1a62, 0x1a63,
        0x1a65, 0x1a6d,
        0x1a73, 0x1a80,
        0x1b00, 0x1b04,
        0x1b34, 0x1b35,
        0x1b36, 0x1b3b,
        0x1b3c, 0x1b3d,
        0x1b42, 0x1b43,
        0x1b6b, 0x1b74,
        0x1b80, 0x1b82,
        0x1ba2, 0x1ba6,
        0x1ba8, 0x1baa,
        0x1bab, 0x1bac,
        0x1be6, 0x1be7,
        0x1be8, 0x1bea,
        0x1bed, 0x1bee,
        0x1bef, 0x1bf2,
        0x1c2c, 0x1c34,
        0x1c36, 0x1c3b,
        0x1cd0, 0x1cd3,
        0x1cd4, 0x1ce1,
        0x1ce2, 0x1ce9,
        0x1ced, 0x1cee,
        0x1cf4, 0x1cf5,
        0x1dc0, 0x1e00,
        0x1fbd, 0x1fbe,
        0x1fbf, 0x1fc2,
        0x1fcd, 0x1fd0,
        0x1fdd, 0x1fe0,
        0x1fed, 0x1ff2,
        0x1ffd, 0x200e,
        0x2010, 0x2071,
        0x2074, 0x207f,
        0x2080, 0x2090,
        0x20a0, 0x2102,
        0x2103, 0x2107,
        0x2108, 0x210a,
        0x2114, 0x2115,
        0x2116, 0x2119,
        0x211e, 0x2124,
        0x2125, 0x2126,
        0x2127, 0x2128,
        0x2129, 0x212a,
        0x212e, 0x212f,
        0x213a, 0x213c,
        0x2140, 0x2145,
        0x214a, 0x214e,
        0x2150, 0x2160,
        0x2189, 0x2336,
        0x237b, 0x2395,
        0x2396, 0x249c,
        0x24ea, 0x26ac,
        0x26ad, 0x2800,
        0x2900, 0x2c00,
        0x2ce5, 0x2ceb,
        0x2cef, 0x2cf2,
        0x2cf9, 0x2d00,
        0x2d7f, 0x2d80,
        0x2de0, 0x3005,
        0x3008, 0x3021,
        0x302a, 0x3031,
        0x3036, 0x3038,
        0x303d, 0x3041,
        0x3099, 0x309d,
        0x30a0, 0x30a1,
        0x30fb, 0x30fc,
        0x31c0, 0x31f0,
        0x321d, 0x3220,
        0x3250, 0x3260,
        0x327c, 0x327f,
        0x32b1, 0x32c0,
        0x32cc, 0x32d0,
        0x3377, 0x337b,
        0x33de, 0x33e0,
        0x33ff, 0x3400,
        0x4dc0, 0x4e00,
        0xa490, 0xa4d0,
        0xa60d, 0xa610,
        0xa66f, 0xa680,
        0xa69f, 0xa6a0,
        0xa6f0, 0xa6f2,
        0xa700, 0xa722,
        0xa788, 0xa789,
        0xa802, 0xa803,
        0xa806, 0xa807,
        0xa80b, 0xa80c,
        0xa825, 0xa827,
        0xa828, 0xa830,
        0xa838, 0xa840,
        0xa874, 0xa880,
        0xa8c4, 0xa8ce,
        0xa8e0, 0xa8f2,
        0xa926, 0xa92e,
        0xa947, 0xa952,
        0xa980, 0xa983,
        0xa9b3, 0xa9b4,
        0xa9b6, 0xa9ba,
        0xa9bc, 0xa9bd,
        0xaa29, 0xaa2f,
        0xaa31, 0xaa33,
        0xaa35, 0xaa40,
        0xaa43, 0xaa44,
        0xaa4c, 0xaa4d,
        0xaab0, 0xaab1,
        0xaab2, 0xaab5,
        0xaab7, 0xaab9,
        0xaabe, 0xaac0,
        0xaac1, 0xaac2,
        0xaaec, 0xaaee,
        0xaaf6, 0xab01,
        0xabe5, 0xabe6,
        0xabe8, 0xabe9,
        0xabed, 0xabf0,
        0xfb1e, 0xfb1f,
        0xfb29, 0xfb2a,
        0xfd3e, 0xfd50,
        0xfdfd, 0xfe70,
        0xfeff, 0xff21,
        0xff3b, 0xff41,
        0xff5b, 0xff66,
        0xffe0, 0x10000,
        0x10101, 0x10102,
        0x10140, 0x101d0,
        0x101fd, 0x10280,
        0x1091f, 0x10920,
        0x10a01, 0x10a10,
        0x10a38, 0x10a40,
        0x10b39, 0x10b40,
        0x10e60, 0x11000,
        0x11001, 0x11002,
        0x11038, 0x11047,
        0x11052, 0x11066,
        0x11080, 0x11082,
        0x110b3, 0x110b7,
        0x110b9, 0x110bb,
        0x11100, 0x11103,
        0x11127, 0x1112c,
        0x1112d, 0x11136,
        0x11180, 0x11182,
        0x111b6, 0x111bf,
        0x116ab, 0x116ac,
        0x116ad, 0x116ae,
        0x116b0, 0x116b6,
        0x116b7, 0x116c0,
        0x16f8f, 0x16f93,
        0x1d167, 0x1d16a,
        0x1d173, 0x1d183,
        0x1d185, 0x1d18c,
        0x1d1aa, 0x1d1ae,
        0x1d200, 0x1d360,
        0x1d6db, 0x1d6dc,
        0x1d715, 0x1d716,
        0x1d74f, 0x1d750,
        0x1d789, 0x1d78a,
        0x1d7c3, 0x1d7c4,
        0x1d7ce, 0x1ee00,
        0x1eef0, 0x1f110,
        0x1f16a, 0x1f170,
        0x1f300, 0x1f48c,
        0x1f48d, 0x1f524,
        0x1f525, 0x20000,
        0xe0001, 0xf0000,
        0x10fffe, 0x10ffff // sentinel
    };


    // 使用带有缓存的二分查找

    private transient volatile int stCache = 0;

    private boolean isStrongDirectional(char c) {
        int cachedIndex = stCache;
        if (c < strongTable[cachedIndex]) {
            cachedIndex = search(c, strongTable, 0, cachedIndex);
        } else if (c >= strongTable[cachedIndex + 1]) {
            cachedIndex = search(c, strongTable, cachedIndex + 1,
                                 strongTable.length - cachedIndex - 1);
        }
        boolean val = (cachedIndex & 0x1) == 1;
        stCache = cachedIndex;
        return val;
    }

    private static int getKeyFromMask(int mask) {
        int key = 0;
        while (key < NUM_KEYS && ((mask & (1<<key)) == 0)) {
            ++key;
        }
        if (key == NUM_KEYS || ((mask & ~(1<<key)) != 0)) {
            throw new IllegalArgumentException("invalid shaper: " + Integer.toHexString(mask));
        }
        return key;
    }

    /**
     * 返回指定 Unicode 范围的整形器。所有
     * Latin-1 (EUROPEAN) 数字都被转换为
     * 相应的十进制 Unicode 数字。
     * @param singleRange 指定的 Unicode 范围
     * @return 一个非上下文的数字整形器
     * @throws IllegalArgumentException 如果范围不是一个单一范围
     */
    public static NumericShaper getShaper(int singleRange) {
        int key = getKeyFromMask(singleRange);
        return new NumericShaper(key, singleRange);
    }


                /**
     * 返回提供的 Unicode 范围的整形器。所有 Latin-1 (EUROPEAN) 数字都会转换为指定 Unicode 范围对应的十进制数字。
     *
     * @param singleRange 由 {@link
     *                    NumericShaper.Range} 常量给出的 Unicode 范围。
     * @return 一个非上下文的 {@code NumericShaper}。
     * @throws NullPointerException 如果 {@code singleRange} 是 {@code null}
     * @since 1.7
     */
    public static NumericShaper getShaper(Range singleRange) {
        return new NumericShaper(singleRange, EnumSet.of(singleRange));
    }

    /**
     * 返回提供的 Unicode 范围的上下文整形器。Latin-1 (EUROPEAN) 数字将转换为前导文本范围对应的十进制数字，
     * 如果该范围是提供的范围之一。多个范围可以通过或运算符组合在一起，例如，
     * <code>NumericShaper.ARABIC | NumericShaper.THAI</code>。整形器假设 EUROPEAN 作为起始上下文，也就是说，
     * 如果在字符串中遇到 EUROPEAN 数字之前没有强方向性文本，则上下文被假定为 EUROPEAN，因此数字不会进行整形。
     * @param ranges 指定的 Unicode 范围
     * @return 指定范围的整形器
     */
    public static NumericShaper getContextualShaper(int ranges) {
        ranges |= CONTEXTUAL_MASK;
        return new NumericShaper(EUROPEAN_KEY, ranges);
    }

    /**
     * 返回提供的 Unicode 范围的上下文整形器。Latin-1 (EUROPEAN) 数字将转换为前导文本范围对应的十进制数字，
     * 如果该范围是提供的范围之一。
     *
     * <p>整形器假设 EUROPEAN 作为起始上下文，也就是说，如果在字符串中遇到 EUROPEAN 数字之前没有强方向性文本，
     * 则上下文被假定为 EUROPEAN，因此数字不会进行整形。
     *
     * @param ranges 指定的 Unicode 范围
     * @return 指定范围的上下文整形器
     * @throws NullPointerException 如果 {@code ranges} 是 {@code null}。
     * @since 1.7
     */
    public static NumericShaper getContextualShaper(Set<Range> ranges) {
        NumericShaper shaper = new NumericShaper(Range.EUROPEAN, ranges);
        shaper.mask = CONTEXTUAL_MASK;
        return shaper;
    }

    /**
     * 返回提供的 Unicode 范围的上下文整形器。Latin-1 (EUROPEAN) 数字将转换为前导文本范围对应的十进制数字，
     * 如果该范围是提供的范围之一。多个范围可以通过或运算符组合在一起，例如，
     * <code>NumericShaper.ARABIC | NumericShaper.THAI</code>。整形器使用 defaultContext 作为起始上下文。
     * @param ranges 指定的 Unicode 范围
     * @param defaultContext 起始上下文，例如
     * <code>NumericShaper.EUROPEAN</code>
     * @return 指定 Unicode 范围的整形器。
     * @throws IllegalArgumentException 如果指定的
     * <code>defaultContext</code> 不是单个有效的范围。
     */
    public static NumericShaper getContextualShaper(int ranges, int defaultContext) {
        int key = getKeyFromMask(defaultContext);
        ranges |= CONTEXTUAL_MASK;
        return new NumericShaper(key, ranges);
    }

    /**
     * 返回提供的 Unicode 范围的上下文整形器。Latin-1 (EUROPEAN) 数字将转换为前导文本范围对应的十进制数字，
     * 如果该范围是提供的范围之一。整形器使用 {@code
     * defaultContext} 作为起始上下文。
     *
     * @param ranges 指定的 Unicode 范围
     * @param defaultContext 起始上下文，例如
     *                       {@code NumericShaper.Range.EUROPEAN}
     * @return 指定 Unicode 范围的上下文整形器。
     * @throws NullPointerException
     *         如果 {@code ranges} 或 {@code defaultContext} 是 {@code null}
     * @since 1.7
     */
    public static NumericShaper getContextualShaper(Set<Range> ranges,
                                                    Range defaultContext) {
        if (defaultContext == null) {
            throw new NullPointerException();
        }
        NumericShaper shaper = new NumericShaper(defaultContext, ranges);
        shaper.mask = CONTEXTUAL_MASK;
        return shaper;
    }

    /**
     * 私有构造函数。
     */
    private NumericShaper(int key, int mask) {
        this.key = key;
        this.mask = mask;
    }

    private NumericShaper(Range defaultContext, Set<Range> ranges) {
        shapingRange = defaultContext;
        rangeSet = EnumSet.copyOf(ranges); // 如果 ranges 为 null，则抛出 NPE。

        // 如果同时指定了 ARABIC 和 EASTERN_ARABIC，则优先使用 EASTERN_ARABIC。
        if (rangeSet.contains(Range.EASTERN_ARABIC)
            && rangeSet.contains(Range.ARABIC)) {
            rangeSet.remove(Range.ARABIC);
        }

        // 除了上述情况，如果同时指定了 TAI_THAM_HORA 和 TAI_THAM_THAM，则优先使用 TAI_THAM_THAM。
        if (rangeSet.contains(Range.TAI_THAM_THAM)
            && rangeSet.contains(Range.TAI_THAM_HORA)) {
            rangeSet.remove(Range.TAI_THAM_HORA);
        }

        rangeArray = rangeSet.toArray(new Range[rangeSet.size()]);
        if (rangeArray.length > BSEARCH_THRESHOLD) {
            // 对 rangeArray 进行排序以便进行二分查找
            Arrays.sort(rangeArray,
                        new Comparator<Range>() {
                            public int compare(Range s1, Range s2) {
                                return s1.base > s2.base ? 1 : s1.base == s2.base ? 0 : -1;
                            }
                        });
        }
    }

    /**
     * 转换文本中从 start 到 start + count 之间的数字。
     * @param text 要转换的字符数组
     * @param start 开始转换的索引
     * @param count 要转换的字符数
     * @throws IndexOutOfBoundsException 如果 start 或 start + count 超出范围
     * @throws NullPointerException 如果 text 为 null
     */
    public void shape(char[] text, int start, int count) {
        checkParams(text, start, count);
        if (isContextual()) {
            if (rangeSet == null) {
                shapeContextually(text, start, count, key);
            } else {
                shapeContextually(text, start, count, shapingRange);
            }
        } else {
            shapeNonContextually(text, start, count);
        }
    }

    /**
     * 使用提供的上下文转换文本中从 start 到 start + count 之间的数字。
     * 如果整形器不是上下文整形器，则忽略上下文。
     * @param text 字符数组
     * @param start 开始转换的索引
     * @param count 要转换的字符数
     * @param context 要转换的字符的上下文，例如 <code>NumericShaper.EUROPEAN</code>
     * @throws IndexOutOfBoundsException 如果 start 或 start + count 超出范围
     * @throws NullPointerException 如果 text 为 null
     * @throws IllegalArgumentException 如果这是上下文整形器
     * 并且指定的 <code>context</code> 不是单个有效的范围。
     */
    public void shape(char[] text, int start, int count, int context) {
        checkParams(text, start, count);
        if (isContextual()) {
            int ctxKey = getKeyFromMask(context);
            if (rangeSet == null) {
                shapeContextually(text, start, count, ctxKey);
            } else {
                shapeContextually(text, start, count, Range.values()[ctxKey]);
            }
        } else {
            shapeNonContextually(text, start, count);
        }
    }

    /**
     * 使用提供的上下文转换文本中从 {@code
     * start} 到 {@code start + count} 之间的数字。如果整形器不是上下文整形器，则忽略上下文。
     *
     * @param text  字符数组
     * @param start 开始转换的索引
     * @param count 要转换的字符数
     * @param context 要转换的字符的上下文，例如 {@code NumericShaper.Range.EUROPEAN}
     * @throws IndexOutOfBoundsException
     *         如果 {@code start} 或 {@code start + count} 超出范围
     * @throws NullPointerException
     *         如果 {@code text} 或 {@code context} 为 null
     * @since 1.7
     */
    public void shape(char[] text, int start, int count, Range context) {
        checkParams(text, start, count);
        if (context == null) {
            throw new NullPointerException("context is null");
        }

        if (isContextual()) {
            if (rangeSet != null) {
                shapeContextually(text, start, count, context);
            } else {
                int key = Range.toRangeIndex(context);
                if (key >= 0) {
                    shapeContextually(text, start, count, key);
                } else {
                    shapeContextually(text, start, count, shapingRange);
                }
            }
        } else {
            shapeNonContextually(text, start, count);
        }
    }

    private void checkParams(char[] text, int start, int count) {
        if (text == null) {
            throw new NullPointerException("text is null");
        }
        if ((start < 0)
            || (start > text.length)
            || ((start + count) < 0)
            || ((start + count) > text.length)) {
            throw new IndexOutOfBoundsException(
                "bad start or count for text of length " + text.length);
        }
    }

    /**
     * 返回一个 <code>boolean</code> 值，指示此整形器是否进行上下文整形。
     * @return 如果此整形器是上下文整形器，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isContextual() {
        return (mask & CONTEXTUAL_MASK) != 0;
    }

    /**
     * 返回一个 <code>int</code> 值，该值是所有要整形的范围的值的或运算结果。
     * <p>
     * 例如，要检查整形器是否整形为阿拉伯数字，可以使用以下代码：
     * <blockquote>
     *   {@code if ((shaper.getRanges() & shaper.ARABIC) != 0) &#123; ... }
     * </blockquote>
     *
     * <p>注意，此方法仅支持位掩码范围。调用 {@link #getRangeSet()} 以获取枚举范围。
     *
     * @return 所有要整形的范围的值。
     */
    public int getRanges() {
        return mask & ~CONTEXTUAL_MASK;
    }

    /**
     * 返回一个 {@code Set}，表示此 {@code NumericShaper} 中所有要整形的 Unicode 范围。
     *
     * @return 所有要整形的 Unicode 范围。
     * @since 1.7
     */
    public Set<Range> getRangeSet() {
        if (rangeSet != null) {
            return EnumSet.copyOf(rangeSet);
        }
        return Range.maskToRangeSet(mask);
    }

    /**
     * 执行非上下文整形。
     */
    private void shapeNonContextually(char[] text, int start, int count) {
        int base;
        char minDigit = '0';
        if (shapingRange != null) {
            base = shapingRange.getDigitBase();
            minDigit += shapingRange.getNumericBase();
        } else {
            base = bases[key];
            if (key == ETHIOPIC_KEY) {
                minDigit++; // Ethiopic 不使用十进制零
            }
        }
        for (int i = start, e = start + count; i < e; ++i) {
            char c = text[i];
            if (c >= minDigit && c <= '\u0039') {
                text[i] = (char)(c + base);
            }
        }
    }

    /**
     * 执行上下文整形。
     * 同步以保护在 getContextKey 中使用的缓存。
     */
    private synchronized void shapeContextually(char[] text, int start, int count, int ctxKey) {

        // 如果不支持此上下文，则不进行整形
        if ((mask & (1<<ctxKey)) == 0) {
            ctxKey = EUROPEAN_KEY;
        }
        int lastkey = ctxKey;

        int base = bases[ctxKey];
        char minDigit = ctxKey == ETHIOPIC_KEY ? '1' : '0'; // Ethiopic 不使用十进制零

        synchronized (NumericShaper.class) {
            for (int i = start, e = start + count; i < e; ++i) {
                char c = text[i];
                if (c >= minDigit && c <= '\u0039') {
                    text[i] = (char)(c + base);
                }

                if (isStrongDirectional(c)) {
                    int newkey = getContextKey(c);
                    if (newkey != lastkey) {
                        lastkey = newkey;

                        ctxKey = newkey;
                        if (((mask & EASTERN_ARABIC) != 0) &&
                             (ctxKey == ARABIC_KEY ||
                              ctxKey == EASTERN_ARABIC_KEY)) {
                            ctxKey = EASTERN_ARABIC_KEY;
                        } else if (((mask & ARABIC) != 0) &&
                             (ctxKey == ARABIC_KEY ||
                              ctxKey == EASTERN_ARABIC_KEY)) {
                            ctxKey = ARABIC_KEY;
                        } else if ((mask & (1<<ctxKey)) == 0) {
                            ctxKey = EUROPEAN_KEY;
                        }

                        base = bases[ctxKey];

                        minDigit = ctxKey == ETHIOPIC_KEY ? '1' : '0'; // Ethiopic 不使用十进制零
                    }
                }
            }
        }
    }

    private void shapeContextually(char[] text, int start, int count, Range ctxKey) {
        // 如果不支持指定的上下文，则不进行整形。
        if (ctxKey == null || !rangeSet.contains(ctxKey)) {
            ctxKey = Range.EUROPEAN;
        }

        Range lastKey = ctxKey;
        int base = ctxKey.getDigitBase();
        char minDigit = (char)('0' + ctxKey.getNumericBase());
        final int end = start + count;
        for (int i = start; i < end; ++i) {
            char c = text[i];
            if (c >= minDigit && c <= '9') {
                text[i] = (char)(c + base);
                continue;
            }
            if (isStrongDirectional(c)) {
                ctxKey = rangeForCodePoint(c);
                if (ctxKey != lastKey) {
                    lastKey = ctxKey;
                    base = ctxKey.getDigitBase();
                    minDigit = (char)('0' + ctxKey.getNumericBase());
                }
            }
        }
    }

    /**
     * 返回此整形器的哈希码。
     * @return 此整形器的哈希码。
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int hash = mask;
        if (rangeSet != null) {
            // 仅对枚举基础的 NumericShaper 使用 CONTEXTUAL_MASK 位。反序列化的 NumericShaper 可能有位掩码。
            hash &= CONTEXTUAL_MASK;
            hash ^= rangeSet.hashCode();
        }
        return hash;
    }


                /**
     * 如果指定的对象是 <code>NumericShaper</code> 的实例，并且以相同的方式进行形状转换，则返回 {@code true}。
     * 无论范围表示、位掩码或枚举如何，只要形状转换相同即可。例如，以下代码将输出 {@code "true"}。
     * <blockquote><pre>
     * NumericShaper ns1 = NumericShaper.getShaper(NumericShaper.ARABIC);
     * NumericShaper ns2 = NumericShaper.getShaper(NumericShaper.Range.ARABIC);
     * System.out.println(ns1.equals(ns2));
     * </pre></blockquote>
     *
     * @param o 要与当前 <code>NumericShaper</code> 对象进行比较的指定对象
     * @return 如果 <code>o</code> 是 <code>NumericShaper</code> 的实例并且以相同的方式进行形状转换，则返回 <code>true</code>；
     *         否则返回 <code>false</code>。
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o != null) {
            try {
                NumericShaper rhs = (NumericShaper)o;
                if (rangeSet != null) {
                    if (rhs.rangeSet != null) {
                        return isContextual() == rhs.isContextual()
                            && rangeSet.equals(rhs.rangeSet)
                            && shapingRange == rhs.shapingRange;
                    }
                    return isContextual() == rhs.isContextual()
                        && rangeSet.equals(Range.maskToRangeSet(rhs.mask))
                        && shapingRange == Range.indexToRange(rhs.key);
                } else if (rhs.rangeSet != null) {
                    Set<Range> rset = Range.maskToRangeSet(mask);
                    Range srange = Range.indexToRange(key);
                    return isContextual() == rhs.isContextual()
                        && rset.equals(rhs.rangeSet)
                        && srange == rhs.shapingRange;
                }
                return rhs.mask == mask && rhs.key == key;
            }
            catch (ClassCastException e) {
            }
        }
        return false;
    }

    /**
     * 返回描述此形状转换器的 <code>String</code>。此方法仅用于调试目的。
     * @return 描述此形状转换器的 <code>String</code>。
     */
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());

        buf.append("[contextual:").append(isContextual());

        String[] keyNames = null;
        if (isContextual()) {
            buf.append(", context:");
            buf.append(shapingRange == null ? Range.values()[key] : shapingRange);
        }

        if (rangeSet == null) {
            buf.append(", range(s): ");
            boolean first = true;
            for (int i = 0; i < NUM_KEYS; ++i) {
                if ((mask & (1 << i)) != 0) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append(", ");
                    }
                    buf.append(Range.values()[i]);
                }
            }
        } else {
            buf.append(", range set: ").append(rangeSet);
        }
        buf.append(']');

        return buf.toString();
    }

    /**
     * 返回值中最高位的索引（假设值为正，实际上是大于等于值的2的幂）。值必须为正。
     */
    private static int getHighBit(int value) {
        if (value <= 0) {
            return -32;
        }

        int bit = 0;

        if (value >= 1 << 16) {
            value >>= 16;
            bit += 16;
        }

        if (value >= 1 << 8) {
            value >>= 8;
            bit += 8;
        }

        if (value >= 1 << 4) {
            value >>= 4;
            bit += 4;
        }

        if (value >= 1 << 2) {
            value >>= 2;
            bit += 2;
        }

        if (value >= 1 << 1) {
            bit += 1;
        }

        return bit;
    }

    /**
     * 在数组的子范围内进行快速二分查找。
     */
    private static int search(int value, int[] array, int start, int length)
    {
        int power = 1 << getHighBit(length);
        int extra = length - power;
        int probe = power;
        int index = start;

        if (value >= array[index + extra]) {
            index += extra;
        }

        while (probe > 1) {
            probe >>= 1;

            if (value >= array[index + probe]) {
                index += probe;
            }
        }

        return index;
    }

    /**
     * 将 {@code NumericShaper.Range} 枚举参数（如果有）转换为位掩码参数，并将此对象写入 {@code stream}。
     * 任何没有位掩码对应项的枚举常量在转换中将被忽略。
     *
     * @param stream 要写入的输出流
     * @throws IOException 如果在写入 {@code stream} 时发生 I/O 错误
     * @since 1.7
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (shapingRange != null) {
            int index = Range.toRangeIndex(shapingRange);
            if (index >= 0) {
                key = index;
            }
        }
        if (rangeSet != null) {
            mask |= Range.toRangeMask(rangeSet);
        }
        stream.defaultWriteObject();
    }
}
