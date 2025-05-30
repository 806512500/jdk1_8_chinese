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

/*
 * (C) Copyright IBM Corp. 1999-2003 - All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by IBM. These materials are provided
 * under terms of a License Agreement between IBM and Sun.
 * This technology is protected by multiple US and International
 * patents. This notice and attribution to IBM may not be removed.
 */

package java.text;

import sun.text.bidi.BidiBase;

/**
 * 此类实现了 Unicode 双向算法。
 * <p>
 * Bidi 对象提供有关用于创建它的文本的双向重新排序的信息。例如，正确显示阿拉伯语或希伯来语文本需要这些信息。这些语言本质上是混合方向的，因为它们将数字从左到右排序，而将大多数其他文本从右到左排序。
 * <p>
 * 一旦创建，Bidi 对象可以查询以查看它表示的文本是否全部为左到右或全部为右到左。这样的对象非常轻量级，且处理这种文本相对简单。
 * <p>
 * 如果有多个文本段，可以通过索引访问有关这些段的信息，以获取段的起始、结束和级别。级别表示段的方向和“嵌套级别”。奇数级别表示右到左，而偶数级别表示左到右。例如，级别 0 表示左到右的文本，而级别 1 表示右到左的文本，级别 2 表示嵌入在右到左段中的左到右文本。
 *
 * @since 1.4
 */
public final class Bidi {

    /** 常量表示基本方向是左到右。 */
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;

    /** 常量表示基本方向是右到左。 */
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;

    /**
     * 常量表示基本方向取决于文本中的第一个强方向字符，根据 Unicode 双向算法。如果不存在强方向字符，则基本方向为左到右。
     */
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;

    /**
     * 常量表示基本方向取决于文本中的第一个强方向字符，根据 Unicode 双向算法。如果不存在强方向字符，则基本方向为右到左。
     */
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;

    private BidiBase bidiBase;

    /**
     * 从给定的段落文本和基本方向创建 Bidi。
     * @param paragraph 一个段落的文本
     * @param flags 一组控制算法的标志。算法理解的标志有 DIRECTION_LEFT_TO_RIGHT、DIRECTION_RIGHT_TO_LEFT、DIRECTION_DEFAULT_LEFT_TO_RIGHT 和 DIRECTION_DEFAULT_RIGHT_TO_LEFT。其他值保留。
     */
    public Bidi(String paragraph, int flags) {
        if (paragraph == null) {
            throw new IllegalArgumentException("paragraph is null");
        }

        bidiBase = new BidiBase(paragraph.toCharArray(), 0, null, 0, paragraph.length(), flags);
    }

    /**
     * 从给定的段落文本创建 Bidi。
     * <p>
     * 文本中的 RUN_DIRECTION 属性（如果存在）确定基本方向（左到右或右到左）。如果不存在，基本方向根据 Unicode 双向算法计算，默认为左到右，如果文本中没有强方向字符。此属性（如果存在）必须应用于段落中的所有文本。
     * <p>
     * 文本中的 BIDI_EMBEDDING 属性（如果存在）表示嵌入级别信息。负值 -1 到 -62 表示绝对值级别的覆盖。正值 1 到 62 表示嵌入。值为零或未定义的地方，假设为由基本方向确定的基本嵌入级别。
     * <p>
     * 文本中的 NUMERIC_SHAPING 属性（如果存在）在运行双向算法之前将欧洲数字转换为其他十进制数字。此属性（如果存在）必须应用于段落中的所有文本。
     *
     * @param paragraph 一个段落的文本，可选包含字符和段落属性信息
     *
     * @see java.awt.font.TextAttribute#BIDI_EMBEDDING
     * @see java.awt.font.TextAttribute#NUMERIC_SHAPING
     * @see java.awt.font.TextAttribute#RUN_DIRECTION
     */
    public Bidi(AttributedCharacterIterator paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("paragraph is null");
        }

        bidiBase = new BidiBase(0, 0);
        bidiBase.setPara(paragraph);
    }

    /**
     * 从给定的文本、嵌入和方向信息创建 Bidi。
     * 嵌入数组可以为 null。如果存在，值表示嵌入级别信息。负值 -1 到 -61 表示绝对值级别的覆盖。正值 1 到 61 表示嵌入。值为零的地方，假设为由基本方向确定的基本嵌入级别。
     * @param text 包含要处理的段落文本的数组。
     * @param textStart 文本数组中段落的起始索引。
     * @param embeddings 包含段落中每个字符的嵌入值的数组。可以为 null，表示假设没有外部嵌入信息。
     * @param embStart 嵌入数组中段落的起始索引。
     * @param paragraphLength 文本和嵌入数组中段落的长度。
     * @param flags 一组控制算法的标志。算法理解的标志有 DIRECTION_LEFT_TO_RIGHT、DIRECTION_RIGHT_TO_LEFT、DIRECTION_DEFAULT_LEFT_TO_RIGHT 和 DIRECTION_DEFAULT_RIGHT_TO_LEFT。其他值保留。
     */
    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }
        if (paragraphLength < 0) {
            throw new IllegalArgumentException("bad length: " + paragraphLength);
        }
        if (textStart < 0 || paragraphLength > text.length - textStart) {
            throw new IllegalArgumentException("bad range: " + textStart +
                                               " length: " + paragraphLength +
                                               " for text of length: " + text.length);
        }
        if (embeddings != null && (embStart < 0 || paragraphLength > embeddings.length - embStart)) {
            throw new IllegalArgumentException("bad range: " + embStart +
                                               " length: " + paragraphLength +
                                               " for embeddings of length: " + text.length);
        }

        bidiBase = new BidiBase(text, textStart, embeddings, embStart, paragraphLength, flags);
    }

    /**
     * 创建一个 Bidi 对象，表示当前 Bidi 表示的段落中一行文本的双向信息。如果整个段落适合一行，则不需要此调用。
     *
     * @param lineStart 从段落起始到行起始的偏移量。
     * @param lineLimit 从段落起始到行结束的偏移量。
     * @return 一个 {@code Bidi} 对象
     */
    public Bidi createLineBidi(int lineStart, int lineLimit) {
        AttributedString astr = new AttributedString("");
        Bidi newBidi = new Bidi(astr.getIterator());

        return bidiBase.setLine(this, bidiBase, newBidi, newBidi.bidiBase, lineStart, lineLimit);
    }

    /**
     * 如果行不是左到右或右到左，则返回 true。这意味着它要么包含混合的左到右和右到左的文本段，要么基本方向与唯一文本段的方向不同。
     *
     * @return 如果行不是左到右或右到左，则返回 true。
     */
    public boolean isMixed() {
        return bidiBase.isMixed();
    }

    /**
     * 如果行全部为左到右文本且基本方向为左到右，则返回 true。
     *
     * @return 如果行全部为左到右文本且基本方向为左到右，则返回 true
     */
    public boolean isLeftToRight() {
        return bidiBase.isLeftToRight();
    }

    /**
     * 如果行全部为右到左文本且基本方向为右到左，则返回 true。
     * @return 如果行全部为右到左文本且基本方向为右到左，则返回 true
     */
    public boolean isRightToLeft() {
        return bidiBase.isRightToLeft();
    }

    /**
     * 返回行中文本的长度。
     * @return 行中文本的长度
     */
    public int getLength() {
        return bidiBase.getLength();
    }

    /**
     * 如果基本方向为左到右，则返回 true。
     * @return 如果基本方向为左到右，则返回 true
     */
    public boolean baseIsLeftToRight() {
        return bidiBase.baseIsLeftToRight();
    }

    /**
     * 返回基本级别（0 表示左到右，1 表示右到左）。
     * @return 基本级别
     */
    public int getBaseLevel() {
        return bidiBase.getParaLevel();
    }

    /**
     * 返回偏移处字符的解析级别。如果偏移 < 0 或 ≥ 行的长度，返回基本方向级别。
     *
     * @param offset 要返回级别的字符的索引
     * @return 偏移处字符的解析级别
     */
    public int getLevelAt(int offset) {
        return bidiBase.getLevelAt(offset);
    }

    /**
     * 返回级别的运行次数。
     * @return 级别的运行次数
     */
    public int getRunCount() {
        return bidiBase.countRuns();
    }

    /**
     * 返回此行中第 n 个逻辑运行的级别。
     * @param run 运行的索引，介于 0 和 <code>getRunCount()</code> 之间
     * @return 运行的级别
     */
    public int getRunLevel(int run) {
        return bidiBase.getRunLevel(run);
    }

    /**
     * 返回此行中第 n 个逻辑运行的起始字符的索引，作为从行起始的偏移量。
     * @param run 运行的索引，介于 0 和 <code>getRunCount()</code> 之间
     * @return 运行的起始
     */
    public int getRunStart(int run) {
        return bidiBase.getRunStart(run);
    }

    /**
     * 返回此行中第 n 个逻辑运行的结束字符的索引，作为从行起始的偏移量。例如，对于行上的最后一个运行，这将返回行的长度。
     * @param run 运行的索引，介于 0 和 <code>getRunCount()</code> 之间
     * @return 运行的结束
     */
    public int getRunLimit(int run) {
        return bidiBase.getRunLimit(run);
    }

    /**
     * 如果指定的文本需要双向分析，则返回 true。如果此方法返回 false，则文本将从左到右显示。客户端可以避免构建 Bidi 对象。Unicode 的阿拉伯文表示形式区域中的文本假定已经塑形并排序以供显示，因此不会导致此函数返回 true。
     *
     * @param text 包含要测试的字符的文本
     * @param start 要测试的字符范围的起始位置
     * @param limit 要测试的字符范围的结束位置
     * @return 如果字符范围需要双向分析，则返回 true
     */
    public static boolean requiresBidi(char[] text, int start, int limit) {
        return BidiBase.requiresBidi(text, start, limit);
    }

    /**
     * 根据它们的级别将数组中的对象重新排序为视觉顺序。这是一个实用函数，用于当你有一组表示逻辑顺序文本运行的对象集合，每个运行包含单个级别的文本时。数组中从 <code>objectStart</code> 到 <code>objectStart + count</code> 的元素将根据 levels 数组中对应的元素（位于 <code>index - objectStart + levelStart</code>）指示的每个文本运行的级别重新排序为视觉顺序。
     *
     * @param levels 表示每个对象的双向级别的数组
     * @param levelStart levels 数组的起始位置
     * @param objects 要重新排序为视觉顺序的对象数组
     * @param objectStart objects 数组的起始位置
     * @param count 要重新排序的对象数量
     */
    public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
        BidiBase.reorderVisually(levels, levelStart, objects, objectStart, count);
    }

    /**
     * 显示双向内部状态，用于调试。
     */
    public String toString() {
        return bidiBase.toString();
    }

}
