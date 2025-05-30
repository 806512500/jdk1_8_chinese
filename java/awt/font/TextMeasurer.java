
/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.awt.font;

import java.awt.Font;

import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.text.Bidi;
import java.text.BreakIterator;
import java.text.CharacterIterator;

import java.awt.font.FontRenderContext;

import java.util.Hashtable;
import java.util.Map;

import sun.font.AttributeValues;
import sun.font.BidiUtils;
import sun.font.TextLineComponent;
import sun.font.TextLabelFactory;
import sun.font.FontResolver;

/**
 * <code>TextMeasurer</code> 类提供了行断行所需的原始操作：测量给定进位范围内的内容，确定字符范围的进位，以及为字符范围生成 <code>TextLayout</code>。它还提供了段落增量编辑的方法。
 * <p>
 * <code>TextMeasurer</code> 对象是使用表示单个段落文本的 {@link java.text.AttributedCharacterIterator AttributedCharacterIterator} 构建的。<code>AttributedCharacterIterator</code>
 * 的 {@link AttributedCharacterIterator#getBeginIndex() getBeginIndex} 方法返回的值定义了第一个字符的绝对索引。<code>AttributedCharacterIterator</code>
 * 的 {@link AttributedCharacterIterator#getEndIndex() getEndIndex} 方法返回的值定义了最后一个字符之后的索引。这些值定义了在调用 <code>TextMeasurer</code> 时使用的索引范围。例如，调用
 * 获取文本范围的进位或文本范围的行断点时，必须使用起始索引和结束索引之间的索引。调用
 * {@link #insertChar(java.text.AttributedCharacterIterator, int) insertChar}
 * 和
 * {@link #deleteChar(java.text.AttributedCharacterIterator, int) deleteChar}
 * 会重置 <code>TextMeasurer</code> 以使用传递给这些调用的 <code>AttributedCharacterIterator</code> 的起始索引和结束索引。
 * <p>
 * 大多数客户端将使用更方便的 <code>LineBreakMeasurer</code>，它实现了标准的行断行策略（在每行上放置尽可能多的单词）。
 *
 * @author John Raley
 * @see LineBreakMeasurer
 * @since 1.3
 */

public final class TextMeasurer implements Cloneable {

    // 要格式化的行数。
    private static float EST_LINES = (float) 2.1;

    /*
    static {
        String s = System.getProperty("estLines");
        if (s != null) {
            try {
                Float f = new Float(s);
                EST_LINES = f.floatValue();
            }
            catch(NumberFormatException e) {
            }
        }
        //System.out.println("EST_LINES="+EST_LINES);
    }
    */

    private FontRenderContext fFrc;

    private int fStart;

    // 源文本中的字符
    private char[] fChars;

    // 该段落的 Bidi
    private Bidi fBidi;

    // 该段落中字符的级别数组 - 用于重新排序尾随反向空白
    private byte[] fLevels;

    // 逻辑顺序的行组件
    private TextLineComponent[] fComponents;

    // 组件开始的索引
    private int fComponentStart;

    // 组件结束的索引
    private int fComponentLimit;

    private boolean haveLayoutWindow;

    // 用于查找行组件的有效起始点
    private BreakIterator fLineBreak = null;
    private CharArrayIterator charIter = null;
    int layoutCount = 0;
    int layoutCharCount = 0;

    // 带有已解析字体和样式的段落
    private StyledParagraph fParagraph;

    // 段落数据 - 在所有布局中相同
    private boolean fIsDirectionLTR;
    private byte fBaseline;
    private float[] fBaselineOffsets;
    private float fJustifyRatio = 1;

    /**
     * 从源文本构造 <code>TextMeasurer</code>。
     * 源文本应为单个完整的段落。
     * @param text 源段落。不能为 null。
     * @param frc 用于正确测量文本所需的信息，包括图形设备的信息。不能为 null。
     */
    public TextMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {

        fFrc = frc;
        initAll(text);
    }

    protected Object clone() {
        TextMeasurer other;
        try {
            other = (TextMeasurer) super.clone();
        }
        catch(CloneNotSupportedException e) {
            throw new Error();
        }
        if (fComponents != null) {
            other.fComponents = fComponents.clone();
        }
        return other;
    }

    private void invalidateComponents() {
        fComponentStart = fComponentLimit = fChars.length;
        fComponents = null;
        haveLayoutWindow = false;
    }

    /**
     * 初始化状态，包括 fChars 数组、方向和 fBidi。
     */
    private void initAll(AttributedCharacterIterator text) {

        fStart = text.getBeginIndex();

        // 提取字符
        fChars = new char[text.getEndIndex() - fStart];

        int n = 0;
        for (char c = text.first();
             c != CharacterIterator.DONE;
             c = text.next())
        {
            fChars[n++] = c;
        }

        text.first();

        fBidi = new Bidi(text);
        if (fBidi.isLeftToRight()) {
            fBidi = null;
        }

        text.first();
        Map<? extends Attribute, ?> paragraphAttrs = text.getAttributes();
        NumericShaper shaper = AttributeValues.getNumericShaping(paragraphAttrs);
        if (shaper != null) {
            shaper.shape(fChars, 0, fChars.length);
        }

        fParagraph = new StyledParagraph(text, fChars);

        // 设置段落属性
        {
            // 如果段落开头有嵌入图形，查找第一个非图形字符
            // 并使用它及其字体初始化段落。如果没有，使用第一个图形初始化。
            fJustifyRatio = AttributeValues.getJustification(paragraphAttrs);

            boolean haveFont = TextLine.advanceToFirstFont(text);

            if (haveFont) {
                Font defaultFont = TextLine.getFontAtCurrentPos(text);
                int charsStart = text.getIndex() - text.getBeginIndex();
                LineMetrics lm = defaultFont.getLineMetrics(fChars, charsStart, charsStart+1, fFrc);
                fBaseline = (byte) lm.getBaselineIndex();
                fBaselineOffsets = lm.getBaselineOffsets();
            }
            else {
                // 嗯，这里该怎么做？只能尝试提供合理的值。

                GraphicAttribute graphic = (GraphicAttribute)
                                paragraphAttrs.get(TextAttribute.CHAR_REPLACEMENT);
                fBaseline = TextLayout.getBaselineFromGraphic(graphic);
                Hashtable<Attribute, ?> fmap = new Hashtable<>(5, (float)0.9);
                Font dummyFont = new Font(fmap);
                LineMetrics lm = dummyFont.getLineMetrics(" ", 0, 1, fFrc);
                fBaselineOffsets = lm.getBaselineOffsets();
            }
            fBaselineOffsets = TextLine.getNormalizedOffsets(fBaselineOffsets, fBaseline);
        }

        invalidateComponents();
    }

    /**
     * 为段落生成组件。fChars 和 fBidi 应已初始化。
     */
    private void generateComponents(int startingAt, int endingAt) {

        if (collectStats) {
            formattedChars += (endingAt-startingAt);
        }
        int layoutFlags = 0; // 暂无额外信息，bidi 确定运行和行方向
        TextLabelFactory factory = new TextLabelFactory(fFrc, fChars, fBidi, layoutFlags);

        int[] charsLtoV = null;

        if (fBidi != null) {
            fLevels = BidiUtils.getLevels(fBidi);
            int[] charsVtoL = BidiUtils.createVisualToLogicalMap(fLevels);
            charsLtoV = BidiUtils.createInverseMap(charsVtoL);
            fIsDirectionLTR = fBidi.baseIsLeftToRight();
        }
        else {
            fLevels = null;
            fIsDirectionLTR = true;
        }

        try {
            fComponents = TextLine.getComponents(
                fParagraph, fChars, startingAt, endingAt, charsLtoV, fLevels, factory);
        }
        catch(IllegalArgumentException e) {
            System.out.println("startingAt="+startingAt+"; endingAt="+endingAt);
            System.out.println("fComponentLimit="+fComponentLimit);
            throw e;
        }

        fComponentStart = startingAt;
        fComponentLimit = endingAt;
        //debugFormatCount += (endingAt-startingAt);
    }

    private int calcLineBreak(final int pos, final float maxAdvance) {

        // 以下任一语句可移除错误：
        //generateComponents(0, fChars.length);
        //generateComponents(pos, fChars.length);

        int startPos = pos;
        float width = maxAdvance;

        int tlcIndex;
        int tlcStart = fComponentStart;

        for (tlcIndex = 0; tlcIndex < fComponents.length; tlcIndex++) {
            int gaLimit = tlcStart + fComponents[tlcIndex].getNumCharacters();
            if (gaLimit > startPos) {
                break;
            }
            else {
                tlcStart = gaLimit;
            }
        }

        // tlcStart 现在是 tlcIndex 处 tlc 的开始位置

        for (; tlcIndex < fComponents.length; tlcIndex++) {

            TextLineComponent tlc = fComponents[tlcIndex];
            int numCharsInGa = tlc.getNumCharacters();

            int lineBreak = tlc.getLineBreakIndex(startPos - tlcStart, width);
            if (lineBreak == numCharsInGa && tlcIndex < fComponents.length) {
                width -= tlc.getAdvanceBetween(startPos - tlcStart, lineBreak);
                tlcStart += numCharsInGa;
                startPos = tlcStart;
            }
            else {
                return tlcStart + lineBreak;
            }
        }

        if (fComponentLimit < fChars.length) {
            // 格式化更多文本并重试
            //if (haveLayoutWindow) {
            //    outOfWindow++;
            //}

            generateComponents(pos, fChars.length);
            return calcLineBreak(pos, maxAdvance);
        }

        return fChars.length;
    }

    /**
     * 根据 Unicode 双向行为规范 (Unicode Standard 2.0, section 3.11)，行尾自然流向与基方向相反的空白必须调整为与行方向一致，并移动到行尾。
     * 此方法返回从给定范围中提取的行尾空白字符序列的起始位置。
     */
    private int trailingCdWhitespaceStart(int startPos, int limitPos) {

        if (fLevels != null) {
            // 向后遍历反向空白
            final byte baseLevel = (byte) (fIsDirectionLTR? 0 : 1);
            for (int cdWsStart = limitPos; --cdWsStart >= startPos;) {
                if ((fLevels[cdWsStart] % 2) == baseLevel ||
                        Character.getDirectionality(fChars[cdWsStart]) != Character.DIRECTIONALITY_WHITESPACE) {
                    return ++cdWsStart;
                }
            }
        }

        return startPos;
    }

    private TextLineComponent[] makeComponentsOnRange(int startPos,
                                                      int limitPos) {

        // 叹息，我真的很不愿意在这里做这件事，因为它是双向算法的一部分。
        // cdWsStart 是尾随反向空白的起始位置
        final int cdWsStart = trailingCdWhitespaceStart(startPos, limitPos);

        int tlcIndex;
        int tlcStart = fComponentStart;

        for (tlcIndex = 0; tlcIndex < fComponents.length; tlcIndex++) {
            int gaLimit = tlcStart + fComponents[tlcIndex].getNumCharacters();
            if (gaLimit > startPos) {
                break;
            }
            else {
                tlcStart = gaLimit;
            }
        }

        // tlcStart 现在是 tlcIndex 处 tlc 的开始位置

        int componentCount;
        {
            boolean split = false;
            int compStart = tlcStart;
            int lim=tlcIndex;
            for (boolean cont=true; cont; lim++) {
                int gaLimit = compStart + fComponents[lim].getNumCharacters();
                if (cdWsStart > Math.max(compStart, startPos)
                            && cdWsStart < Math.min(gaLimit, limitPos)) {
                    split = true;
                }
                if (gaLimit >= limitPos) {
                    cont=false;
                }
                else {
                    compStart = gaLimit;
                }
            }
            componentCount = lim-tlcIndex;
            if (split) {
                componentCount++;
            }
        }

        TextLineComponent[] components = new TextLineComponent[componentCount];
        int newCompIndex = 0;
        int linePos = startPos;

        int breakPt = cdWsStart;

        int subsetFlag;
        if (breakPt == startPos) {
            subsetFlag = fIsDirectionLTR? TextLineComponent.LEFT_TO_RIGHT :
                                          TextLineComponent.RIGHT_TO_LEFT;
            breakPt = limitPos;
        }
        else {
            subsetFlag = TextLineComponent.UNCHANGED;
        }


/**
 * 返回从 <code>start</code> 开始测量的第一个字符的索引，该字符在图形宽度上无法适应 <code>maxAdvance</code>。
 *
 * @param start 开始测量的字符索引。 <code>start</code> 是绝对索引，而不是相对于段落的起始位置
 * @param maxAdvance 线必须适应的图形宽度
 * @return 从 <code>start</code> 开始的线上最后一个能适应的字符的索引之后的索引，该线的图形宽度不超过 <code>maxAdvance</code>
 * @throws IllegalArgumentException 如果 <code>start</code> 小于段落的起始位置。
 */
public int getLineBreakIndex(int start, float maxAdvance) {

    int localStart = start - fStart;

    if (!haveLayoutWindow ||
            localStart < fComponentStart ||
            localStart >= fComponentLimit) {
        makeLayoutWindow(localStart);
    }

    return calcLineBreak(localStart, maxAdvance) + fStart;
}

/**
 * 返回从 <code>start</code> 开始并包含到 <code>limit</code> 的字符的图形宽度。
 * <code>start</code> 和 <code>limit</code> 是绝对索引，而不是相对于段落的起始位置。
 *
 * @param start 开始测量的字符索引
 * @param limit 停止测量的字符索引
 * @return 从 <code>start</code> 开始并包含到 <code>limit</code> 的字符的图形宽度
 * @throws IndexOutOfBoundsException 如果 <code>limit</code> 小于 <code>start</code>
 * @throws IllegalArgumentException 如果 <code>start</code> 或 <code>limit</code> 不在段落的起始位置和结束位置之间。
 */
public float getAdvanceBetween(int start, int limit) {

    int localStart = start - fStart;
    int localLimit = limit - fStart;

    ensureComponents(localStart, localLimit);
    TextLine line = makeTextLineOnRange(localStart, localLimit);
    return line.getMetrics().advance;
    // 可以缓存 line，以防 getLayout 被调用时使用相同的 start 和 limit
}

/**
 * 返回给定字符范围的 <code>TextLayout</code>。
 *
 * @param start 第一个字符的索引
 * @param limit 最后一个字符之后的索引。 必须大于 <code>start</code>
 * @return 从 <code>start</code> 开始到（但不包括）<code>limit</code> 的字符的 <code>TextLayout</code>
 * @throws IndexOutOfBoundsException 如果 <code>limit</code> 小于 <code>start</code>
 * @throws IllegalArgumentException 如果 <code>start</code> 或 <code>limit</code> 不在段落的起始位置和结束位置之间。
 */
public TextLayout getLayout(int start, int limit) {

    int localStart = start - fStart;
    int localLimit = limit - fStart;

    ensureComponents(localStart, localLimit);
    TextLine textLine = makeTextLineOnRange(localStart, localLimit);

    if (localLimit < fChars.length) {
        layoutCharCount += limit-start;
        layoutCount++;
    }

    return new TextLayout(textLine,
                          fBaseline,
                          fBaselineOffsets,
                          fJustifyRatio);
}

private int formattedChars = 0;
private static boolean wantStats = false;/*"true".equals(System.getProperty("collectStats"));*/
private boolean collectStats = false;

private void printStats() {
    System.out.println("formattedChars: " + formattedChars);
    //formattedChars = 0;
    collectStats = false;
}

/**
 * 在当前由这个 <code>TextMeasurer</code> 表示的段落中插入一个字符后更新 <code>TextMeasurer</code>。
 * 调用此方法后，这个 <code>TextMeasurer</code> 相当于从文本创建的新 <code>TextMeasurer</code>；
 * 但是，通常更新现有的 <code>TextMeasurer</code> 比从头开始创建新的更高效。
 *
 * @param newParagraph 执行插入后的段落文本。 不能为 null。
 * @param insertPos 插入字符的位置。 必须不小于 <code>newParagraph</code> 的起始位置，并且必须小于 <code>newParagraph</code> 的结束位置。
 * @throws IndexOutOfBoundsException 如果 <code>insertPos</code> 小于 <code>newParagraph</code> 的起始位置或大于等于 <code>newParagraph</code> 的结束位置
 * @throws NullPointerException 如果 <code>newParagraph</code> 为 <code>null</code>
 */
public void insertChar(AttributedCharacterIterator newParagraph, int insertPos) {

    if (collectStats) {
        printStats();
    }
    if (wantStats) {
        collectStats = true;
    }

    fStart = newParagraph.getBeginIndex();
    int end = newParagraph.getEndIndex();
    if (end - fStart != fChars.length+1) {
        initAll(newParagraph);
    }

    char[] newChars = new char[end-fStart];
    int newCharIndex = insertPos - fStart;
    System.arraycopy(fChars, 0, newChars, 0, newCharIndex);

    char newChar = newParagraph.setIndex(insertPos);
    newChars[newCharIndex] = newChar;
    System.arraycopy(fChars,
                     newCharIndex,
                     newChars,
                     newCharIndex+1,
                     end-insertPos-1);
    fChars = newChars;

    if (fBidi != null || Bidi.requiresBidi(newChars, newCharIndex, newCharIndex + 1) ||
            newParagraph.getAttribute(TextAttribute.BIDI_EMBEDDING) != null) {

        fBidi = new Bidi(newParagraph);
        if (fBidi.isLeftToRight()) {
            fBidi = null;
        }
    }

    fParagraph = StyledParagraph.insertChar(newParagraph,
                                            fChars,
                                            insertPos,
                                            fParagraph);
    invalidateComponents();
}

/**
 * 在当前由这个 <code>TextMeasurer</code> 表示的段落中删除一个字符后更新 <code>TextMeasurer</code>。
 * 调用此方法后，这个 <code>TextMeasurer</code> 相当于从文本创建的新 <code>TextMeasurer</code>；
 * 但是，通常更新现有的 <code>TextMeasurer</code> 比从头开始创建新的更高效。
 *
 * @param newParagraph 执行删除后的段落文本。 不能为 null。
 * @param deletePos 删除字符的位置。 必须不小于 <code>newParagraph</code> 的起始位置，并且必须不大于 <code>newParagraph</code> 的结束位置。
 * @throws IndexOutOfBoundsException 如果 <code>deletePos</code> 小于 <code>newParagraph</code> 的起始位置或大于 <code>newParagraph</code> 的结束位置
 * @throws NullPointerException 如果 <code>newParagraph</code> 为 <code>null</code>
 */
public void deleteChar(AttributedCharacterIterator newParagraph, int deletePos) {

    fStart = newParagraph.getBeginIndex();
    int end = newParagraph.getEndIndex();
    if (end - fStart != fChars.length-1) {
        initAll(newParagraph);
    }

    char[] newChars = new char[end-fStart];
    int changedIndex = deletePos-fStart;

    System.arraycopy(fChars, 0, newChars, 0, deletePos-fStart);
    System.arraycopy(fChars, changedIndex+1, newChars, changedIndex, end-deletePos);
    fChars = newChars;

    if (fBidi != null) {
        fBidi = new Bidi(newParagraph);
        if (fBidi.isLeftToRight()) {
            fBidi = null;
        }
    }

    fParagraph = StyledParagraph.deleteChar(newParagraph,
                                            fChars,
                                            deletePos,
                                            fParagraph);
    invalidateComponents();
}

/**
 * 注意：此方法仅供 LineBreakMeasurer 使用。它是包私有的，因为它返回内部数据。
 */
char[] getChars() {

    return fChars;
}
}
