
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
 * 该类实现了 Unicode 双向算法。
 * <p>
 * Bidi 对象提供了有关用于创建它的文本的双向重新排序的信息。例如，正确显示阿拉伯语或希伯来语文本时需要这些信息。
 * 这些语言本质上是混合方向的，因为它们将数字从左到右排序，而将大多数其他文本从右到左排序。
 * <p>
 * 创建后，可以查询 Bidi 对象以查看它表示的文本是否全部为左到右或全部为右到左。这样的对象非常轻量级，处理起来相对简单。
 * <p>
 * 如果有多个文本段，可以通过索引访问有关这些段的信息，以获取段的起始、结束和级别。级别表示方向和方向段的“嵌套级别”。
 * 奇数级别是右到左，而偶数级别是左到右。例如，级别 0 表示左到右的文本，而级别 1 表示右到左的文本，级别 2 表示嵌入在右到左段中的左到右文本。
 *
 * @since 1.4
 */
public final class Bidi {

    /** 常量，表示基本方向是从左到右。 */
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;

    /** 常量，表示基本方向是从右到左。 */
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;

    /**
     * 常量，表示基本方向取决于文本中的第一个强方向字符，根据 Unicode
     * 双向算法。如果没有强方向字符，基本方向为左到右。
     */
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;

    /**
     * 常量，表示基本方向取决于文本中的第一个强方向字符，根据 Unicode
     * 双向算法。如果没有强方向字符，基本方向为右到左。
     */
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;

    private BidiBase bidiBase;

    /**
     * 从给定的段落文本和基本方向创建 Bidi。
     * @param paragraph 一个段落的文本
     * @param flags 一组控制算法的标志。算法理解的标志有 DIRECTION_LEFT_TO_RIGHT、DIRECTION_RIGHT_TO_LEFT、
     * DIRECTION_DEFAULT_LEFT_TO_RIGHT 和 DIRECTION_DEFAULT_RIGHT_TO_LEFT。
     * 其他值保留。
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
     * 文本中的 RUN_DIRECTION 属性（如果存在）确定基本方向（左到右或右到左）。如果不存在，基本方向根据 Unicode 双向算法计算，默认为左到右，
     * 如果文本中没有强方向字符。如果存在此属性，必须应用于段落中的所有文本。
     * <p>
     * 文本中的 BIDI_EMBEDDING 属性（如果存在）表示嵌套级别信息。-1 到 -62 的负值表示绝对值级别的覆盖。1 到 62 的正值表示嵌套。
     * 值为零或未定义的地方，假定为由基本方向确定的基本嵌套级别。
     * <p>
     * 文本中的 NUMERIC_SHAPING 属性（如果存在）在运行双向算法之前将欧洲数字转换为其他十进制数字。如果存在此属性，必须应用于段落中的所有文本。
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
     * 从给定的文本、嵌套和方向信息创建 Bidi。
     * 嵌套数组可以为 null。如果存在，值表示嵌套级别信息。-1 到 -61 的负值表示绝对值级别的覆盖。1 到 61 的正值表示嵌套。
     * 值为零的地方，假定为由基本方向确定的基本嵌套级别。
     * @param text 包含要处理的段落文本的数组。
     * @param textStart 文本数组中段落的起始索引。
     * @param embeddings 包含段落中每个字符的嵌套值的数组。可以为 null，表示没有外部嵌套信息。
     * @param embStart 嵌套数组中段落的起始索引。
     * @param paragraphLength 文本和嵌套数组中段落的长度。
     * @param flags 一组控制算法的标志。算法理解的标志有 DIRECTION_LEFT_TO_RIGHT、DIRECTION_RIGHT_TO_LEFT、
     * DIRECTION_DEFAULT_LEFT_TO_RIGHT 和 DIRECTION_DEFAULT_RIGHT_TO_LEFT。
     * 其他值保留。
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
 * 创建一个表示当前 Bidi 段落中某行文本的双向信息的 Bidi 对象。如果整个段落只有一行，则不需要调用此方法。
 *
 * @param lineStart 从段落开始到行开始的偏移量。
 * @param lineLimit 从段落开始到行结束的偏移量。
 * @return 一个 {@code Bidi} 对象
 */
public Bidi createLineBidi(int lineStart, int lineLimit) {
    AttributedString astr = new AttributedString("");
    Bidi newBidi = new Bidi(astr.getIterator());

    return bidiBase.setLine(this, bidiBase, newBidi, newBidi.bidiBase, lineStart, lineLimit);
}

/**
 * 如果行不是从左到右或从右到左，则返回 true。这意味着它要么包含混合的从左到右和从右到左的文本，要么基础方向与唯一文本段的方向不同。
 *
 * @return 如果行不是从左到右或从右到左，则返回 true。
 */
public boolean isMixed() {
    return bidiBase.isMixed();
}

/**
 * 如果行全部是左对齐文本且基础方向是左对齐，则返回 true。
 *
 * @return 如果行全部是左对齐文本且基础方向是左对齐，则返回 true。
 */
public boolean isLeftToRight() {
    return bidiBase.isLeftToRight();
}

/**
 * 如果行全部是右对齐文本，且基础方向是右对齐，则返回 true。
 * @return 如果行全部是右对齐文本，且基础方向是右对齐，则返回 true。
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
 * 如果基础方向是左对齐，则返回 true。
 * @return 如果基础方向是左对齐，则返回 true。
 */
public boolean baseIsLeftToRight() {
    return bidiBase.baseIsLeftToRight();
}

/**
 * 返回基础级别（左对齐为 0，右对齐为 1）。
 * @return 基础级别
 */
public int getBaseLevel() {
    return bidiBase.getParaLevel();
}

/**
 * 返回偏移处字符的解析级别。如果偏移量小于 0 或大于等于行的长度，则返回基础方向级别。
 *
 * @param offset 要返回级别的字符的索引
 * @return 偏移处字符的解析级别
 */
public int getLevelAt(int offset) {
    return bidiBase.getLevelAt(offset);
}

/**
 * 返回级别运行的数量。
 * @return 级别运行的数量
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
 * 返回此行中第 n 个逻辑运行的开始字符的索引，作为从行开始的偏移量。
 * @param run 运行的索引，介于 0 和 <code>getRunCount()</code> 之间
 * @return 运行的开始
 */
public int getRunStart(int run) {
    return bidiBase.getRunStart(run);
}

/**
 * 返回此行中第 n 个逻辑运行的结束字符的索引，作为从行开始的偏移量。例如，对于行上的最后一个运行，这将返回行的长度。
 * @param run 运行的索引，介于 0 和 <code>getRunCount()</code> 之间
 * @return 运行的结束
 */
public int getRunLimit(int run) {
    return bidiBase.getRunLimit(run);
}

/**
 * 如果指定的文本需要双向分析，则返回 true。如果此方法返回 false，则文本将从左到右显示。客户端可以避免构建 Bidi 对象。
 * Unicode 的阿拉伯文表示形式区域中的文本假定已经塑形和排序以供显示，因此不会导致此函数返回 true。
 *
 * @param text 包含要测试的字符的文本
 * @param start 要测试的字符范围的开始
 * @param limit 要测试的字符范围的结束
 * @return 如果字符范围需要双向分析，则返回 true
 */
public static boolean requiresBidi(char[] text, int start, int limit) {
    return BidiBase.requiresBidi(text, start, limit);
}

/**
 * 根据它们的级别将数组中的对象重新排序为视觉顺序。这是一个实用函数，用于当您有一组表示逻辑顺序文本运行的对象集合时，
 * 每个运行包含单一级别的文本。数组中从 <code>objectStart</code> 到 <code>objectStart + count</code>
 * 的元素将被重新排序为视觉顺序，假设每个文本运行的级别由级别数组中对应的元素表示
 * （在 <code>index - objectStart + levelStart</code>）。
 *
 * @param levels 表示每个对象的双向级别的数组
 * @param levelStart 级别数组的开始位置
 * @param objects 要重新排序为视觉顺序的对象数组
 * @param objectStart 对象数组的开始位置
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
