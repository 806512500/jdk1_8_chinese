
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator;
import java.awt.font.FontRenderContext;

/**
 * <code>LineBreakMeasurer</code> 类允许对带样式的文本进行分段，使其适合特定的视觉宽度。这对于希望显示一段特定宽度的文本的客户端非常有用，这个宽度称为 <b>包裹宽度</b>。
 * <p>
 * <code>LineBreakMeasurer</code> 通过一个带样式文本的迭代器构建。迭代器的范围应该是文本中的一个段落。
 * <code>LineBreakMeasurer</code> 维护一个文本中下一个文本段的起始位置。初始时，这个位置是文本的起始位置。段落根据双向格式规则被分配一个总体方向（左到右或右到左）。从段落中获取的所有段落都具有与段落相同的方向。
 * <p>
 * 通过调用 <code>nextLayout</code> 方法获取文本段，该方法返回一个 {@link TextLayout}，表示适合包裹宽度的文本。
 * <code>nextLayout</code> 方法将当前位置移动到从 <code>nextLayout</code> 返回的布局的末尾。
 * <p>
 * <code>LineBreakMeasurer</code> 实现了最常用的行断开策略：每个适合包裹宽度的单词都被放在行上。如果第一个单词不适合，那么所有适合包裹宽度的字符都被放在行上。每行至少放置一个字符。
 * <p>
 * <code>TextLayout</code> 实例由 <code>LineBreakMeasurer</code> 返回，将制表符视为 0 宽度的空格。希望获取制表符分隔的段落以进行定位的客户端应使用带有文本限制偏移量的 <code>nextLayout</code> 重载方法。
 * 限制偏移量应该是制表符后的第一个字符。从这个方法返回的 <code>TextLayout</code> 对象在提供的限制处结束（或在此之前，如果从当前位置到限制之间的文本不能完全适合包裹宽度）。
 * <p>
 * 布局制表符分隔文本的客户端在第一段放置后需要稍微不同的行断开策略。他们应该将不适合剩余空间的单词完全放在下一行，而不是在剩余空间中放置部分单词。这个策略的改变可以在带有 <code>boolean</code> 参数的 <code>nextLayout</code> 重载方法中请求。如果此参数为 <code>true</code>，<code>nextLayout</code> 返回 <code>null</code>，如果第一个单词不适合给定的空间。请参见下面的制表符示例。
 * <p>
 * 一般来说，如果用于构建 <code>LineBreakMeasurer</code> 的文本发生变化，必须构建一个新的 <code>LineBreakMeasurer</code> 以反映这一变化。（旧的 <code>LineBreakMeasurer</code> 仍然可以正常工作，但它不会意识到文本的变化。）然而，如果文本变化是插入或删除一个字符，可以通过调用 <code>insertChar</code> 或 <code>deleteChar</code> 来“更新”现有的 <code>LineBreakMeasurer</code>。更新现有的 <code>LineBreakMeasurer</code> 比创建一个新的要快得多。基于用户输入修改文本的客户端应利用这些方法。
 * <p>
 * <strong>示例</strong>：<p>
 * 在组件中渲染一个段落
 * <blockquote>
 * <pre>{@code
 * public void paint(Graphics graphics) {
 *
 *     Point2D pen = new Point2D(10, 20);
 *     Graphics2D g2d = (Graphics2D)graphics;
 *     FontRenderContext frc = g2d.getFontRenderContext();
 *
 *     // 假设 styledText 是一个包含至少一个字符的 AttributedCharacterIterator
 *
 *     LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
 *     float wrappingWidth = getSize().width - 15;
 *
 *     while (measurer.getPosition() < fStyledText.length()) {
 *
 *         TextLayout layout = measurer.nextLayout(wrappingWidth);
 *
 *         pen.y += (layout.getAscent());
 *         float dx = layout.isLeftToRight() ?
 *             0 : (wrappingWidth - layout.getAdvance());
 *
 *         layout.draw(graphics, pen.x + dx, pen.y);
 *         pen.y += layout.getDescent() + layout.getLeading();
 *     }
 * }
 * }</pre>
 * </blockquote>
 * <p>
 * 渲染带有制表符的文本。为简单起见，假设文本的总体方向为左到右
 * <blockquote>
 * <pre>{@code
 * public void paint(Graphics graphics) {
 *
 *     float leftMargin = 10, rightMargin = 310;
 *     float[] tabStops = { 100, 250 };
 *
 *     // 假设 styledText 是一个 AttributedCharacterIterator，且 styledText 中的制表符数量为 tabCount
 *
 *     int[] tabLocations = new int[tabCount+1];
 *
 *     int i = 0;
 *     for (char c = styledText.first(); c != styledText.DONE; c = styledText.next()) {
 *         if (c == '\t') {
 *             tabLocations[i++] = styledText.getIndex();
 *         }
 *     }
 *     tabLocations[tabCount] = styledText.getEndIndex() - 1;
 *
 *     // 现在 tabLocations 包含了文本中每个制表符的偏移量。为了方便起见，最后一个条目是文本中最后一个字符的偏移量。
 *
 *     LineBreakMeasurer measurer = new LineBreakMeasurer(styledText);
 *     int currentTab = 0;
 *     float verticalPos = 20;
 *
 *     while (measurer.getPosition() < styledText.getEndIndex()) {
 *
 *         // 布局并绘制每一行。一行上的所有段落必须在任何绘制发生之前计算，因为必须知道行的最大上升高度。
 *         // TextLayouts 被计算并存储在一个 Vector 中；它们的水平位置存储在一个并行的 Vector 中。
 *
 *         // lineContainsText 在绘制第一个段落后为 true
 *         boolean lineContainsText = false;
 *         boolean lineComplete = false;
 *         float maxAscent = 0, maxDescent = 0;
 *         float horizontalPos = leftMargin;
 *         Vector layouts = new Vector(1);
 *         Vector penPositions = new Vector(1);
 *
 *         while (!lineComplete) {
 *             float wrappingWidth = rightMargin - horizontalPos;
 *             TextLayout layout =
 *                     measurer.nextLayout(wrappingWidth,
 *                                         tabLocations[currentTab]+1,
 *                                         lineContainsText);
 *
 *             // 如果 lineContainsText 为 true，layout 可能为 null
 *             if (layout != null) {
 *                 layouts.addElement(layout);
 *                 penPositions.addElement(new Float(horizontalPos));
 *                 horizontalPos += layout.getAdvance();
 *                 maxAscent = Math.max(maxAscent, layout.getAscent());
 *                 maxDescent = Math.max(maxDescent,
 *                     layout.getDescent() + layout.getLeading());
 *             } else {
 *                 lineComplete = true;
 *             }
 *
 *             lineContainsText = true;
 *
 *             if (measurer.getPosition() == tabLocations[currentTab]+1) {
 *                 currentTab++;
 *             }
 *
 *             if (measurer.getPosition() == styledText.getEndIndex())
 *                 lineComplete = true;
 *             else if (horizontalPos >= tabStops[tabStops.length-1])
 *                 lineComplete = true;
 *
 *             if (!lineComplete) {
 *                 // 移动到下一个制表符停止位置
 *                 int j;
 *                 for (j=0; horizontalPos >= tabStops[j]; j++) {}
 *                 horizontalPos = tabStops[j];
 *             }
 *         }
 *
 *         verticalPos += maxAscent;
 *
 *         Enumeration layoutEnum = layouts.elements();
 *         Enumeration positionEnum = penPositions.elements();
 *
 *         // 现在迭代布局并绘制它们
 *         while (layoutEnum.hasMoreElements()) {
 *             TextLayout nextLayout = (TextLayout) layoutEnum.nextElement();
 *             Float nextPosition = (Float) positionEnum.nextElement();
 *             nextLayout.draw(graphics, nextPosition.floatValue(), verticalPos);
 *         }
 *
 *         verticalPos += maxDescent;
 *     }
 * }
 * }</pre>
 * </blockquote>
 * @see TextLayout
 */

public final class LineBreakMeasurer {

    private BreakIterator breakIter;
    private int start;
    private int pos;
    private int limit;
    private TextMeasurer measurer;
    private CharArrayIterator charIter;

    /**
     * 构造一个 <code>LineBreakMeasurer</code>，用于指定的文本。
     *
     * @param text 用于此 <code>LineBreakMeasurer</code> 生成 <code>TextLayout</code> 对象的文本；文本必须至少包含一个字符；如果通过 <code>iter</code> 可用的文本发生变化，对此 <code>LineBreakMeasurer</code> 实例的进一步调用是未定义的（在某些情况下，当调用 <code>insertChar</code> 或 <code>deleteChar</code> 之后除外 - 请参见下文）
     * @param frc 包含有关图形设备的信息，这些信息对于正确测量文本是必需的；文本测量可能会因设备分辨率和抗锯齿等属性而略有不同；此参数不指定 <code>LineBreakMeasurer</code> 和用户空间之间的转换
     * @see LineBreakMeasurer#insertChar
     * @see LineBreakMeasurer#deleteChar
     */
    public LineBreakMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
        this(text, BreakIterator.getLineInstance(), frc);
    }

    /**
     * 构造一个 <code>LineBreakMeasurer</code>，用于指定的文本。
     *
     * @param text 用于此 <code>LineBreakMeasurer</code> 生成 <code>TextLayout</code> 对象的文本；文本必须至少包含一个字符；如果通过 <code>iter</code> 可用的文本发生变化，对此 <code>LineBreakMeasurer</code> 实例的进一步调用是未定义的（在某些情况下，当调用 <code>insertChar</code> 或 <code>deleteChar</code> 之后除外 - 请参见下文）
     * @param breakIter 定义行断点的 {@link BreakIterator}
     * @param frc 包含有关图形设备的信息，这些信息对于正确测量文本是必需的；文本测量可能会因设备分辨率和抗锯齿等属性而略有不同；此参数不指定 <code>LineBreakMeasurer</code> 和用户空间之间的转换
     * @throws IllegalArgumentException 如果文本少于一个字符
     * @see LineBreakMeasurer#insertChar
     * @see LineBreakMeasurer#deleteChar
     */
    public LineBreakMeasurer(AttributedCharacterIterator text,
                             BreakIterator breakIter,
                             FontRenderContext frc) {
        if (text.getEndIndex() - text.getBeginIndex() < 1) {
            throw new IllegalArgumentException("Text must contain at least one character.");
        }

        this.breakIter = breakIter;
        this.measurer = new TextMeasurer(text, frc);
        this.limit = text.getEndIndex();
        this.pos = this.start = text.getBeginIndex();

        charIter = new CharArrayIterator(measurer.getChars(), this.start);
        this.breakIter.setText(charIter);
    }

    /**
     * 返回下一个布局末尾的位置。不会更新此 <code>LineBreakMeasurer</code> 的当前位置。
     *
     * @param wrappingWidth 下一个布局中文本的最大可见宽度
     * @return 表示下一个 <code>TextLayout</code> 限制的文本中的偏移量。
     */
    public int nextOffset(float wrappingWidth) {
        return nextOffset(wrappingWidth, limit, false);
    }

    /**
     * 返回下一个布局末尾的位置。不会更新此 <code>LineBreakMeasurer</code> 的当前位置。
     *
     * @param wrappingWidth 下一个布局中文本的最大可见宽度
     * @param offsetLimit 不能包含在下一个布局中的第一个字符，即使文本在限制之后适合包裹宽度；<code>offsetLimit</code> 必须大于当前位置
     * @param requireNextWord 如果为 <code>true</code>，则返回的当前位置是下一个单词完全不适合 <code>wrappingWidth</code> 时的位置；如果为 <code>false</code>，则返回的偏移量至少比当前位置大 1
     * @return 表示下一个 <code>TextLayout</code> 限制的文本中的偏移量
     */
    public int nextOffset(float wrappingWidth, int offsetLimit,
                          boolean requireNextWord) {

        int nextOffset = pos;

        if (pos < limit) {
            if (offsetLimit <= pos) {
                    throw new IllegalArgumentException("offsetLimit must be after current position");
            }


                        int charAtMaxAdvance =
                            measurer.getLineBreakIndex(pos, wrappingWidth);

            if (charAtMaxAdvance == limit) {
                nextOffset = limit;
            }
            else if (Character.isWhitespace(measurer.getChars()[charAtMaxAdvance-start])) {
                nextOffset = breakIter.following(charAtMaxAdvance);
            }
            else {
            // 断行位于单词内；回退到前一个断行点。

                // 注意：我认为 breakIter.preceding(limit) 应该等同于
                // breakIter.last(), breakIter.previous()，但 BreakIterator
                // 的作者们认为并非如此...
                // 如果它们等同，那么第一个分支将是不必要的。
                int testPos = charAtMaxAdvance + 1;
                if (testPos == limit) {
                    breakIter.last();
                    nextOffset = breakIter.previous();
                }
                else {
                    nextOffset = breakIter.preceding(testPos);
                }

                if (nextOffset <= pos) {
                    // 第一个单词不适合行
                    if (requireNextWord) {
                        nextOffset = pos;
                    }
                    else {
                        nextOffset = Math.max(pos+1, charAtMaxAdvance);
                    }
                }
            }
        }

        if (nextOffset > offsetLimit) {
            nextOffset = offsetLimit;
        }

        return nextOffset;
    }

    /**
     * 返回下一个布局，并更新当前位置。
     *
     * @param wrappingWidth 下一个布局中文本允许的最大可见前进量
     * @return 一个 <code>TextLayout</code>，从当前位置开始，表示在
     *     <code>wrappingWidth</code> 内适合的下一行
     */
    public TextLayout nextLayout(float wrappingWidth) {
        return nextLayout(wrappingWidth, limit, false);
    }

    /**
     * 返回下一个布局，并更新当前位置。
     *
     * @param wrappingWidth 下一个布局中文本允许的最大可见前进量
     * @param offsetLimit 不能包含在下一个布局中的第一个字符，即使文本超过限制
     *    也会适合在 <code>wrappingWidth</code> 内；<code>offsetLimit</code>
     *    必须大于当前位置
     * @param requireNextWord 如果 <code>true</code>，并且当前位置的整个单词
     *    不适合在 <code>wrappingWidth</code> 内，返回 <code>null</code>。如果
     *    <code>false</code>，返回一个有效的布局，至少包括当前位置的字符
     * @return 一个 <code>TextLayout</code>，从当前位置开始，表示在
     *    <code>wrappingWidth</code> 内适合的下一行。如果当前位置是此
     *    <code>LineBreakMeasurer</code> 使用的文本的末尾，返回 <code>null</code>
     */
    public TextLayout nextLayout(float wrappingWidth, int offsetLimit,
                                 boolean requireNextWord) {

        if (pos < limit) {
            int layoutLimit = nextOffset(wrappingWidth, offsetLimit, requireNextWord);
            if (layoutLimit == pos) {
                return null;
            }

            TextLayout result = measurer.getLayout(pos, layoutLimit);
            pos = layoutLimit;

            return result;
        } else {
            return null;
        }
    }

    /**
     * 返回此 <code>LineBreakMeasurer</code> 的当前位置。
     *
     * @return 此 <code>LineBreakMeasurer</code> 的当前位置
     * @see #setPosition
     */
    public int getPosition() {
        return pos;
    }

    /**
     * 设置此 <code>LineBreakMeasurer</code> 的当前位置。
     *
     * @param newPosition 此 <code>LineBreakMeasurer</code> 的当前位置；位置应在构造此
     *    <code>LineBreakMeasurer</code> 时使用的文本范围内（或在最近传递给
     *    <code>insertChar</code> 或 <code>deleteChar</code> 的文本范围内）
     * @see #getPosition
     */
    public void setPosition(int newPosition) {
        if (newPosition < start || newPosition > limit) {
            throw new IllegalArgumentException("position is out of range");
        }
        pos = newPosition;
    }

    /**
     * 在文本中插入一个字符后更新此 <code>LineBreakMeasurer</code>，并将当前位置设置为段落的开头。
     *
     * @param newParagraph 插入后的文本
     * @param insertPos 插入字符在文本中的位置
     * @throws IndexOutOfBoundsException 如果 <code>insertPos</code> 小于 <code>newParagraph</code>
     *         的起始位置或大于等于 <code>newParagraph</code> 的结束位置
     * @throws NullPointerException 如果 <code>newParagraph</code> 为 <code>null</code>
     * @see #deleteChar
     */
    public void insertChar(AttributedCharacterIterator newParagraph,
                           int insertPos) {

        measurer.insertChar(newParagraph, insertPos);

        limit = newParagraph.getEndIndex();
        pos = start = newParagraph.getBeginIndex();

        charIter.reset(measurer.getChars(), newParagraph.getBeginIndex());
        breakIter.setText(charIter);
    }

    /**
     * 在文本中删除一个字符后更新此 <code>LineBreakMeasurer</code>，并将当前位置设置为段落的开头。
     * @param newParagraph 删除后的文本
     * @param deletePos 删除字符在文本中的位置
     * @throws IndexOutOfBoundsException 如果 <code>deletePos</code> 小于 <code>newParagraph</code>
     *         的起始位置或大于 <code>newParagraph</code> 的结束位置
     * @throws NullPointerException 如果 <code>newParagraph</code> 为 <code>null</code>
     * @see #insertChar
     */
    public void deleteChar(AttributedCharacterIterator newParagraph,
                           int deletePos) {

        measurer.deleteChar(newParagraph, deletePos);

        limit = newParagraph.getEndIndex();
        pos = start = newParagraph.getBeginIndex();

        charIter.reset(measurer.getChars(), start);
        breakIter.setText(charIter);
    }
}
