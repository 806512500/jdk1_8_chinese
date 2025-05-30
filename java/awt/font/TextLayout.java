
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright IBM Corp. 1996-2003, All Rights Reserved
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.NumericShaper;
import java.awt.font.TextLine.TextLineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import sun.font.AttributeValues;
import sun.font.CoreMetrics;
import sun.font.Decoration;
import sun.font.FontLineMetrics;
import sun.font.FontResolver;
import sun.font.GraphicComponent;
import sun.font.LayoutPathImpl;
import sun.text.CodePointIterator;

/**
 *
 * <code>TextLayout</code> 是一个不可变的图形表示，用于表示带样式的字符数据。
 * <p>
 * 它提供了以下功能：
 * <ul>
 * <li>隐式双向分析和重新排序，
 * <li>光标定位和移动，包括混合方向文本的分裂光标，
 * <li>高亮显示，包括混合方向文本的逻辑和视觉高亮显示，
 * <li>多种基线（罗马、悬挂和居中），
 * <li>命中测试，
 * <li>对齐，
 * <li>默认字体替换，
 * <li>度量信息，如上升、下降和前进，以及
 * <li>渲染
 * </ul>
 * <p>
 * 可以使用 <code>TextLayout</code> 的 <code>draw</code> 方法来渲染 <code>TextLayout</code>。
 * <p>
 * <code>TextLayout</code> 可以直接构造或通过使用 {@link LineBreakMeasurer} 构造。直接构造时，源文本表示一个段落。<code>LineBreakMeasurer</code> 允许带样式的文本被分解成适合特定宽度的行。有关更多信息，请参阅 <code>LineBreakMeasurer</code> 文档。
 * <p>
 * <code>TextLayout</code> 的构造逻辑上按以下步骤进行：
 * <ul>
 * <li>提取并检查段落属性，
 * <li>对文本进行双向重新排序分析，并在需要时计算重新排序信息，
 * <li>将文本分段为样式运行
 * <li>为样式运行选择字体，首先使用属性 {@link TextAttribute#FONT} 中指定的字体，否则使用已定义的属性计算默认字体
 * <li>如果文本在多个基线上，进一步将运行或子运行分解为共享相同基线的子运行，
 * <li>使用所选字体为每个运行生成 glyphvectors，
 * <li>对 glyphvectors 进行最终的双向重新排序
 * </ul>
 * <p>
 * 从 <code>TextLayout</code> 对象的方法返回的所有图形信息都是相对于 <code>TextLayout</code> 的原点的，即 <code>TextLayout</code> 对象的基线与左边缘的交点。同样，传递给 <code>TextLayout</code> 对象的方法的坐标假定是相对于 <code>TextLayout</code> 对象的原点的。客户端通常需要在 <code>TextLayout</code> 对象的坐标系和其他对象（如 {@link java.awt.Graphics Graphics} 对象）的坐标系之间进行转换。
 * <p>
 * <code>TextLayout</code> 对象是从带样式的文本构造的，但它们不会保留对源文本的引用。因此，用于生成 <code>TextLayout</code> 的文本的更改不会影响 <code>TextLayout</code>。
 * <p>
 * <code>TextLayout</code> 对象上的三个方法（<code>getNextRightHit</code>、<code>getNextLeftHit</code> 和 <code>hitTestChar</code>）返回 {@link TextHitInfo} 的实例。这些 <code>TextHitInfo</code> 对象中包含的偏移量是相对于 <code>TextLayout</code> 的开始位置的，<b>而不是</b> 用于创建 <code>TextLayout</code> 的文本。同样，接受 <code>TextHitInfo</code> 实例作为参数的 <code>TextLayout</code> 方法期望 <code>TextHitInfo</code> 对象的偏移量是相对于 <code>TextLayout</code> 的，而不是任何底层文本存储模型。
 * <p>
 * <strong>示例</strong>：<p>
 * 构造并绘制 <code>TextLayout</code> 及其边界矩形：
 * <blockquote><pre>
 *   Graphics2D g = ...;
 *   Point2D loc = ...;
 *   Font font = Font.getFont("Helvetica-bold-italic");
 *   FontRenderContext frc = g.getFontRenderContext();
 *   TextLayout layout = new TextLayout("This is a string", font, frc);
 *   layout.draw(g, (float)loc.getX(), (float)loc.getY());
 *
 *   Rectangle2D bounds = layout.getBounds();
 *   bounds.setRect(bounds.getX()+loc.getX(),
 *                  bounds.getY()+loc.getY(),
 *                  bounds.getWidth(),
 *                  bounds.getHeight());
 *   g.draw(bounds);
 * </pre>
 * </blockquote>
 * <p>
 * 命中测试 <code>TextLayout</code>（确定特定图形位置的字符）：
 * <blockquote><pre>
 *   Point2D click = ...;
 *   TextHitInfo hit = layout.hitTestChar(
 *                         (float) (click.getX() - loc.getX()),
 *                         (float) (click.getY() - loc.getY()));
 * </pre>
 * </blockquote>
 * <p>
 * 响应右箭头键按压：
 * <blockquote><pre>
 *   int insertionIndex = ...;
 *   TextHitInfo next = layout.getNextRightHit(insertionIndex);
 *   if (next != null) {
 *       // 将图形转换到屏幕上的布局原点
 *       g.translate(loc.getX(), loc.getY());
 *       Shape[] carets = layout.getCaretShapes(next.getInsertionIndex());
 *       g.draw(carets[0]);
 *       if (carets[1] != null) {
 *           g.draw(carets[1]);
 *       }
 *   }
 * </pre></blockquote>
 * <p>
 * 绘制对应于源文本子字符串的选择范围。选择区域可能不是视觉上连续的：
 * <blockquote><pre>
 *   // selStart, selLimit 应该相对于布局，而不是源文本
 *
 *   int selStart = ..., selLimit = ...;
 *   Color selectionColor = ...;
 *   Shape selection = layout.getLogicalHighlightShape(selStart, selLimit);
 *   // 选择区域可能由不连续的区域组成
 *   // 假设图形已转换到布局的原点
 *   g.setColor(selectionColor);
 *   g.fill(selection);
 * </pre></blockquote>
 * <p>
 * 绘制视觉上连续的选择范围。选择范围可能对应于源文本中的多个子字符串。可以通过 <code>getLogicalRangesForVisualSelection()</code> 获取对应源文本子字符串的范围：
 * <blockquote><pre>
 *   TextHitInfo selStart = ..., selLimit = ...;
 *   Shape selection = layout.getVisualHighlightShape(selStart, selLimit);
 *   g.setColor(selectionColor);
 *   g.fill(selection);
 *   int[] ranges = getLogicalRangesForVisualSelection(selStart, selLimit);
 *   // ranges[0], ranges[1] 是第一个选择范围，
 *   // ranges[2], ranges[3] 是第二个选择范围，等等。
 * </pre></blockquote>
 * <p>
 * 注意：字体旋转可能导致文本基线旋转，不同旋转的多个运行可能导致基线弯曲或锯齿。为了考虑这种（罕见的）可能性，某些 API 指定返回度量值和采用 '基线相对坐标'（例如上升、前进），而其他 API 采用 '标准坐标'（例如 getBounds）。基线相对坐标将 'x' 坐标映射到沿基线的距离（正 x 沿基线前进），将 'y' 坐标映射到 'x' 处基线向量的垂直距离（正 y 与基线向量成 90 度顺时针方向）。标准坐标沿 x 和 y 轴测量，0,0 位于 TextLayout 的原点。每个相关 API 的文档指明了值在哪个坐标系中。通常，测量相关的 API 采用基线相对坐标，而显示相关的 API 采用标准坐标。
 *
 * @see LineBreakMeasurer
 * @see TextAttribute
 * @see TextHitInfo
 * @see LayoutPath
 */
public final class TextLayout implements Cloneable {

    private int characterCount;
    private boolean isVerticalLine = false;
    private byte baseline;
    private float[] baselineOffsets;  // 为什么需要这些？
    private TextLine textLine;

    // 从 GlyphSets 和设置信息中计算的缓存值：
    // 所有这些值都在 buildCache() 中从头开始重新计算
    private TextLine.TextLineMetrics lineMetrics = null;
    private float visibleAdvance;
    private int hashCodeCache;

    /*
     * TextLayouts 据说是不可变的。如果你在内部修改了 TextLayout（如对齐代码所做的那样），你需要将其设置回 false。可以用 textLine != null <--> cacheIsValid 替换。
     */
    private boolean cacheIsValid = false;


    // 这个值从属性中获取，并限制在 [0,1] 区间内。如果为 0，则布局不能对齐。
    private float justifyRatio;

    // 如果布局是由对齐产生的，那么该布局不能再次对齐。为了强制这个约束，将已对齐布局的 justifyRatio 设置为这个值。
    private static final float ALREADY_JUSTIFIED = -53.9f;

    // dx 和 dy 指定 TextLayout 的原点与最左侧 GlyphSet（实际上是 TextLayoutComponent）的原点之间的距离。它们曾用于悬挂标点支持，但现在不再实现。目前它们始终为 0，TextLayout 当前不保证在非零 dx、dy 值下正常工作。它们现在是静态的，因此不会占用 TextLayout 实例的空间。
    private static float dx;
    private static float dy;

    /*
     * 自然边界用于内部。它在 getNaturalBounds 中按需构建。
     */
    private Rectangle2D naturalBounds = null;

    /*
     * boundsRect 包含 TextLayout 可以绘制的所有部分。它在 getBounds 中按需构建。
     */
    private Rectangle2D boundsRect = null;

    /*
     * 标志，用于在命中测试或箭头键操作时允许/禁止在连字内的光标
     */
    private boolean caretsInLigaturesAreAllowed = false;

    /**
     * 定义确定强光标位置的策略。
     * 这个类包含一个方法，<code>getStrongCaret</code>，用于指定确定双光标文本中强光标的策略。强光标用于将光标向左或向右移动。这个类的实例可以传递给 <code>getCaretShapes</code>、<code>getNextLeftHit</code> 和 <code>getNextRightHit</code> 以自定义强光标选择。
     * <p>
     * 要指定其他光标策略，可以继承 <code>CaretPolicy</code> 并覆盖 <code>getStrongCaret</code>。<code>getStrongCaret</code> 应检查两个 <code>TextHitInfo</code> 参数并选择其中一个作为强光标。
     * <p>
     * 大多数客户端不需要使用这个类。
     */
    public static class CaretPolicy {

        /**
         * 构造一个 <code>CaretPolicy</code>。
         */
         public CaretPolicy() {
         }

        /**
         * 在指定的 <code>TextLayout</code> 中选择一个指定的 <code>TextHitInfo</code> 实例作为强光标。
         * @param hit1 <code>layout</code> 中的有效命中
         * @param hit2 <code>layout</code> 中的有效命中
         * @param layout 使用 <code>hit1</code> 和 <code>hit2</code> 的 <code>TextLayout</code>
         * @return <code>hit1</code> 或 <code>hit2</code>（或等效的 <code>TextHitInfo</code>），表示强光标。
         */
        public TextHitInfo getStrongCaret(TextHitInfo hit1,
                                          TextHitInfo hit2,
                                          TextLayout layout) {

            // 默认实现只是调用 layout 上的私有方法
            return layout.getStrongHit(hit1, hit2);
        }
    }

    /**
     * 当客户端未指定策略时使用此 <code>CaretPolicy</code>。使用此策略时，命中方向与行方向相同的字符比命中反向字符更强。如果字符的方向相同，则命中字符的前缘比命中字符的后缘更强。
     */
    public static final CaretPolicy DEFAULT_CARET_POLICY = new CaretPolicy();

    /**
     * 从 <code>String</code> 和 {@link Font} 构造一个 <code>TextLayout</code>。所有文本都使用指定的 <code>Font</code> 进行样式化。
     * <p>
     * <code>String</code> 必须指定一个段落的文本，因为整个段落是双向算法所必需的。
     * @param string 要显示的文本
     * @param font 用于样式化文本的 <code>Font</code>
     * @param frc 包含测量文本所需的信息，这些信息与图形设备有关。文本度量可能会因设备分辨率和抗锯齿等属性而略有不同。此参数不指定 <code>TextLayout</code> 和用户空间之间的转换。
     */
    public TextLayout(String string, Font font, FontRenderContext frc) {


                    if (font == null) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了空字体。");
        }

        if (string == null) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了空字符串。");
        }

        if (string.length() == 0) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了零长度字符串。");
        }

        Map<? extends Attribute, ?> attributes = null;
        if (font.hasLayoutAttributes()) {
            attributes = font.getAttributes();
        }

        char[] text = string.toCharArray();
        if (sameBaselineUpTo(font, text, 0, text.length) == text.length) {
            fastInit(text, font, attributes, frc);
        } else {
            AttributedString as = attributes == null
                ? new AttributedString(string)
                : new AttributedString(string, attributes);
            as.addAttribute(TextAttribute.FONT, font);
            standardInit(as.getIterator(), text, frc);
        }
    }

    /**
     * 从一个 <code>String</code> 和一个属性集构造一个 <code>TextLayout</code>。
     * <p>
     * 所有文本都使用提供的属性进行样式化。
     * <p>
     * <code>string</code> 必须指定一个段落的文本，因为整个段落是双向算法所必需的。
     * @param string 要显示的文本
     * @param attributes 用于样式化文本的属性
     * @param frc 包含测量文本所需图形设备信息。文本测量可能会因设备分辨率和抗锯齿等属性而略有不同。此参数不指定 <code>TextLayout</code> 和用户空间之间的转换。
     */
    public TextLayout(String string, Map<? extends Attribute,?> attributes,
                      FontRenderContext frc)
    {
        if (string == null) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了空字符串。");
        }

        if (attributes == null) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了空映射。");
        }

        if (string.length() == 0) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了零长度字符串。");
        }

        char[] text = string.toCharArray();
        Font font = singleFont(text, 0, text.length, attributes);
        if (font != null) {
            fastInit(text, font, attributes, frc);
        } else {
            AttributedString as = new AttributedString(string, attributes);
            standardInit(as.getIterator(), text, frc);
        }
    }

    /*
     * 确定一个字体，如果一个字体可以渲染所有文本在同一基线上，则返回该字体，否则返回 null。如果属性集指定了一个字体，假设它可以显示所有文本而不进行检查。
     * 如果属性集中包含嵌入图形，则返回 null。
     */
    private static Font singleFont(char[] text,
                                   int start,
                                   int limit,
                                   Map<? extends Attribute, ?> attributes) {

        if (attributes.get(TextAttribute.CHAR_REPLACEMENT) != null) {
            return null;
        }

        Font font = null;
        try {
            font = (Font)attributes.get(TextAttribute.FONT);
        }
        catch (ClassCastException e) {
        }
        if (font == null) {
            if (attributes.get(TextAttribute.FAMILY) != null) {
                font = Font.getFont(attributes);
                if (font.canDisplayUpTo(text, start, limit) != -1) {
                    return null;
                }
            } else {
                FontResolver resolver = FontResolver.getInstance();
                CodePointIterator iter = CodePointIterator.create(text, start, limit);
                int fontIndex = resolver.nextFontRunIndex(iter);
                if (iter.charIndex() == limit) {
                    font = resolver.getFont(fontIndex, attributes);
                }
            }
        }

        if (sameBaselineUpTo(font, text, start, limit) != limit) {
            return null;
        }

        return font;
    }

    /**
     * 从一个样式化文本的迭代器构造一个 <code>TextLayout</code>。
     * <p>
     * 迭代器必须指定一个段落的文本，因为整个段落是双向算法所必需的。
     * @param text 要显示的样式化文本
     * @param frc 包含测量文本所需图形设备信息。文本测量可能会因设备分辨率和抗锯齿等属性而略有不同。此参数不指定 <code>TextLayout</code> 和用户空间之间的转换。
     */
    public TextLayout(AttributedCharacterIterator text, FontRenderContext frc) {

        if (text == null) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了空迭代器。");
        }

        int start = text.getBeginIndex();
        int limit = text.getEndIndex();
        if (start == limit) {
            throw new IllegalArgumentException("向 TextLayout 构造函数传递了零长度迭代器。");
        }

        int len = limit - start;
        text.first();
        char[] chars = new char[len];
        int n = 0;
        for (char c = text.first();
             c != CharacterIterator.DONE;
             c = text.next())
        {
            chars[n++] = c;
        }

        text.first();
        if (text.getRunLimit() == limit) {

            Map<? extends Attribute, ?> attributes = text.getAttributes();
            Font font = singleFont(chars, 0, len, attributes);
            if (font != null) {
                fastInit(chars, font, attributes, frc);
                return;
            }
        }

        standardInit(text, chars, frc);
    }

    /**
     * 从一个 {@link TextLine} 和一些段落数据创建一个 <code>TextLayout</code>。此方法由 {@link TextMeasurer} 使用。
     * @param textLine 要应用于结果 <code>TextLayout</code> 的行测量属性
     * @param baseline 文本的基线
     * @param baselineOffsets 此 <code>TextLayout</code> 的基线偏移。这应该已经归一化为 <code>baseline</code>
     * @param justifyRatio <code>0</code> 表示 <code>TextLayout</code> 不能对齐；<code>1</code> 表示可以对齐。
     */
    TextLayout(TextLine textLine,
               byte baseline,
               float[] baselineOffsets,
               float justifyRatio) {

        this.characterCount = textLine.characterCount();
        this.baseline = baseline;
        this.baselineOffsets = baselineOffsets;
        this.textLine = textLine;
        this.justifyRatio = justifyRatio;
    }

    /**
     * 初始化段落特定的数据。
     */
    private void paragraphInit(byte aBaseline, CoreMetrics lm,
                               Map<? extends Attribute, ?> paragraphAttrs,
                               char[] text) {

        baseline = aBaseline;

        // 归一化到当前基线
        baselineOffsets = TextLine.getNormalizedOffsets(lm.baselineOffsets, baseline);

        justifyRatio = AttributeValues.getJustification(paragraphAttrs);
        NumericShaper shaper = AttributeValues.getNumericShaping(paragraphAttrs);
        if (shaper != null) {
            shaper.shape(text, 0, text.length);
        }
    }

    /*
     * 快速初始化生成一个单一的字形集。这需要：
     * 全部一个样式
     * 全部由一个字体渲染（即没有嵌入图形）
     * 全部在同一基线上
     */
    private void fastInit(char[] chars, Font font,
                          Map<? extends Attribute, ?> attrs,
                          FontRenderContext frc) {

        // Object vf = attrs.get(TextAttribute.ORIENTATION);
        // isVerticalLine = TextAttribute.ORIENTATION_VERTICAL.equals(vf);
        isVerticalLine = false;

        LineMetrics lm = font.getLineMetrics(chars, 0, chars.length, frc);
        CoreMetrics cm = CoreMetrics.get(lm);
        byte glyphBaseline = (byte) cm.baselineIndex;

        if (attrs == null) {
            baseline = glyphBaseline;
            baselineOffsets = cm.baselineOffsets;
            justifyRatio = 1.0f;
        } else {
            paragraphInit(glyphBaseline, cm, attrs, chars);
        }

        characterCount = chars.length;

        textLine = TextLine.fastCreateTextLine(frc, chars, font, cm, attrs);
    }

    /*
     * 标准初始化根据样式、可渲染性和基线运行生成多个字形集。
     * @param chars 迭代器中的文本，提取到一个字符数组中
     */
    private void standardInit(AttributedCharacterIterator text, char[] chars, FontRenderContext frc) {

        characterCount = chars.length;

        // 设置段落属性
        {
            // 如果段落开头有嵌入图形，查找第一个非图形字符
            // 并使用它及其字体初始化段落。如果没有，使用第一个图形初始化。

            Map<? extends Attribute, ?> paragraphAttrs = text.getAttributes();

            boolean haveFont = TextLine.advanceToFirstFont(text);

            if (haveFont) {
                Font defaultFont = TextLine.getFontAtCurrentPos(text);
                int charsStart = text.getIndex() - text.getBeginIndex();
                LineMetrics lm = defaultFont.getLineMetrics(chars, charsStart, charsStart+1, frc);
                CoreMetrics cm = CoreMetrics.get(lm);
                paragraphInit((byte)cm.baselineIndex, cm, paragraphAttrs, chars);
            }
            else {
                // 嗯，这里该怎么做呢？大概提供一些合理的值吧。

                GraphicAttribute graphic = (GraphicAttribute)
                                paragraphAttrs.get(TextAttribute.CHAR_REPLACEMENT);
                byte defaultBaseline = getBaselineFromGraphic(graphic);
                CoreMetrics cm = GraphicComponent.createCoreMetrics(graphic);
                paragraphInit(defaultBaseline, cm, paragraphAttrs, chars);
            }
        }

        textLine = TextLine.standardCreateTextLine(frc, text, chars, baselineOffsets);
    }

    /*
     * 一个用于重建上升、下降、前导和前进缓存的工具。
     * 如果你克隆并变异（如对齐、编辑方法所做的），你需要调用这个方法。
     */
    private void ensureCache() {
        if (!cacheIsValid) {
            buildCache();
        }
    }

    private void buildCache() {
        lineMetrics = textLine.getMetrics();

        // 计算 visibleAdvance
        if (textLine.isDirectionLTR()) {

            int lastNonSpace = characterCount-1;
            while (lastNonSpace != -1) {
                int logIndex = textLine.visualToLogical(lastNonSpace);
                if (!textLine.isCharSpace(logIndex)) {
                    break;
                }
                else {
                    --lastNonSpace;
                }
            }
            if (lastNonSpace == characterCount-1) {
                visibleAdvance = lineMetrics.advance;
            }
            else if (lastNonSpace == -1) {
                visibleAdvance = 0;
            }
            else {
                int logIndex = textLine.visualToLogical(lastNonSpace);
                visibleAdvance = textLine.getCharLinePosition(logIndex)
                                        + textLine.getCharAdvance(logIndex);
            }
        }
        else {

            int leftmostNonSpace = 0;
            while (leftmostNonSpace != characterCount) {
                int logIndex = textLine.visualToLogical(leftmostNonSpace);
                if (!textLine.isCharSpace(logIndex)) {
                    break;
                }
                else {
                    ++leftmostNonSpace;
                }
            }
            if (leftmostNonSpace == characterCount) {
                visibleAdvance = 0;
            }
            else if (leftmostNonSpace == 0) {
                visibleAdvance = lineMetrics.advance;
            }
            else {
                int logIndex = textLine.visualToLogical(leftmostNonSpace);
                float pos = textLine.getCharLinePosition(logIndex);
                visibleAdvance = lineMetrics.advance - pos;
            }
        }

        // naturalBounds, boundsRect 将按需生成
        naturalBounds = null;
        boundsRect = null;

        // hashCode 将按需重新生成
        hashCodeCache = 0;

        cacheIsValid = true;
    }

    /**
     * '自然边界' 包含布局可以绘制的所有光标。
     *
     */
    private Rectangle2D getNaturalBounds() {
        ensureCache();

        if (naturalBounds == null) {
            naturalBounds = textLine.getItalicBounds();
        }

        return naturalBounds;
    }

    /**
     * 创建此 <code>TextLayout</code> 的副本。
     */
    protected Object clone() {
        /*
         * !!! 我认为这是安全的。一旦创建，没有任何东西会变异字形向量或数组。但我们需要确保这一点。
         * {jbr} 实际上，这并不完全正确。对齐代码在克隆后会变异。它实际上不会改变字形向量
         * （这是不可能的），但会用对齐后的集替换它们。这是一个问题，因为新的 GlyphIterator
         * 是通过克隆原型创建的。如果原型有旧的字形向量，新的也会有。如果忘记了一次，你就完了。
         */
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /*
     * 如果传递的参数是一个无效的 TextHitInfo，抛出异常。避免代码重复。
     */
    private void checkTextHit(TextHitInfo hit) {
        if (hit == null) {
            throw new IllegalArgumentException("TextHitInfo 为空。");
        }

        if (hit.getInsertionIndex() < 0 ||
            hit.getInsertionIndex() > characterCount) {
            throw new IllegalArgumentException("TextHitInfo 超出范围");
        }
    }

    /**
     * 创建此 <code>TextLayout</code> 的副本，并对齐到指定的宽度。
     * <p>
     * 如果此 <code>TextLayout</code> 已经对齐，将抛出异常。如果此 <code>TextLayout</code> 对象的对齐比率是零，将返回一个与此 <code>TextLayout</code> 相同的 <code>TextLayout</code>。
     * @param justificationWidth 用于对齐行的宽度。为了获得最佳效果，它不应与当前行的前进量相差太大。
     * @return 对齐到指定宽度的 <code>TextLayout</code>。
     * @exception Error 如果此布局已经对齐，将抛出 Error。
     */
    public TextLayout getJustifiedLayout(float justificationWidth) {


                    if (justificationWidth <= 0) {
            throw new IllegalArgumentException("justificationWidth <= 0 传递给 TextLayout.getJustifiedLayout()");
        }

        if (justifyRatio == ALREADY_JUSTIFIED) {
            throw new Error("不能再次对齐。");
        }

        ensureCache(); // 确保 textLine 不为 null

        // 默认对齐范围，排除尾部逻辑空白
        int limit = characterCount;
        while (limit > 0 && textLine.isCharWhitespace(limit-1)) {
            --limit;
        }

        TextLine newLine = textLine.getJustifiedLine(justificationWidth, justifyRatio, 0, limit);
        if (newLine != null) {
            return new TextLayout(newLine, baseline, baselineOffsets, ALREADY_JUSTIFIED);
        }

        return this;
    }

    /**
     * 对此布局进行对齐。子类可以重写此方法以控制对齐方式
     * （如果有子类的话……）
     *
     * 如果段落属性（来自源文本，可能由布局属性默认）指示非零对齐比率，
     * 则布局将进行对齐。文本将对齐到指定的宽度。当前实现还调整了悬挂标点和尾部空白，
     * 使其超出对齐宽度。一旦对齐，布局将不能重新对齐。
     * <p>
     * 一些代码可能依赖于布局的不可变性。子类不应直接调用此方法，
     * 而应调用 getJustifiedLayout，该方法将在此布局的克隆上调用此方法，
     * 以保留原始布局。
     *
     * @param justificationWidth 用于对齐行的宽度。为了获得最佳效果，它不应与当前行的前进距离相差太大。
     * @see #getJustifiedLayout(float)
     */
    protected void handleJustify(float justificationWidth) {
      // 从未调用
    }


    /**
     * 返回此 <code>TextLayout</code> 的基线。
     * 基线是 <code>Font</code> 中定义的值之一，即罗马、居中和悬挂。上升和下降相对于此基线。
     * <code>baselineOffsets</code> 也相对于此基线。
     * @return 此 <code>TextLayout</code> 的基线。
     * @see #getBaselineOffsets()
     * @see Font
     */
    public byte getBaseline() {
        return baseline;
    }

    /**
     * 返回此 <code>TextLayout</code> 使用的基线偏移数组。
     * <p>
     * 数组由 <code>Font</code> 中定义的值之一索引，即罗马、居中和悬挂。这些值相对于此 <code>TextLayout</code> 对象的基线，
     * 因此 <code>getBaselineOffsets[getBaseline()] == 0</code>。偏移量加到 <code>TextLayout</code> 对象的基线位置上，
     * 以获取新基线的位置。
     * @return 包含此 <code>TextLayout</code> 使用的基线的偏移数组。
     * @see #getBaseline()
     * @see Font
     */
    public float[] getBaselineOffsets() {
        float[] offsets = new float[baselineOffsets.length];
        System.arraycopy(baselineOffsets, 0, offsets, 0, offsets.length);
        return offsets;
    }

    /**
     * 返回此 <code>TextLayout</code> 的前进距离。
     * 前进距离是从原点到最右侧（最底部）字符的前进距离。这是相对于基线的坐标。
     * @return 此 <code>TextLayout</code> 的前进距离。
     */
    public float getAdvance() {
        ensureCache();
        return lineMetrics.advance;
    }

    /**
     * 返回此 <code>TextLayout</code> 的前进距离，不包括尾部空白。这是相对于基线的坐标。
     * @return 此 <code>TextLayout</code> 的前进距离，不包括尾部空白。
     * @see #getAdvance()
     */
    public float getVisibleAdvance() {
        ensureCache();
        return visibleAdvance;
    }

    /**
     * 返回此 <code>TextLayout</code> 的上升距离。
     * 上升距离是从 <code>TextLayout</code> 的顶部（右侧）到基线的距离。它总是正数或零。
     * 上升距离足以容纳上标文本，是每个字形的上升距离、偏移量和基线之和的最大值。
     * 上升距离是从基线到 <code>TextLayout</code> 中所有文本的最大上升距离。这是相对于基线的坐标。
     * @return 此 <code>TextLayout</code> 的上升距离。
     */
    public float getAscent() {
        ensureCache();
        return lineMetrics.ascent;
    }

    /**
     * 返回此 <code>TextLayout</code> 的下降距离。
     * 下降距离是从基线到 <code>TextLayout</code> 的底部（左侧）的距离。它总是正数或零。
     * 下降距离足以容纳下标文本，是每个字形的下降距离、偏移量和基线之和的最大值。
     * 这是从基线到 <code>TextLayout</code> 中所有文本的最大下降距离。这是相对于基线的坐标。
     * @return 此 <code>TextLayout</code> 的下降距离。
     */
    public float getDescent() {
        ensureCache();
        return lineMetrics.descent;
    }

    /**
     * 返回此 <code>TextLayout</code> 的行间距。
     * 行间距是此 <code>TextLayout</code> 建议的行间间距。这是相对于基线的坐标。
     * <p>
     * 行间距是从所有字形向量的行间距、下降距离和基线计算得出的。算法大致如下：
     * <blockquote><pre>
     * maxD = 0;
     * maxDL = 0;
     * for (GlyphVector g in all glyphvectors) {
     *    maxD = max(maxD, g.getDescent() + offsets[g.getBaseline()]);
     *    maxDL = max(maxDL, g.getDescent() + g.getLeading() +
     *                       offsets[g.getBaseline()]);
     * }
     * return maxDL - maxD;
     * </pre></blockquote>
     * @return 此 <code>TextLayout</code> 的行间距。
     */
    public float getLeading() {
        ensureCache();
        return lineMetrics.leading;
    }

    /**
     * 返回此 <code>TextLayout</code> 的边界。
     * 边界是标准坐标系中的。
     * <p>由于光栅化效果，此边界可能不会完全包围 <code>TextLayout</code> 渲染的所有像素。</p>
     * 它可能不会与 <code>TextLayout</code> 的上升距离、下降距离、原点或前进距离完全一致。
     * @return 一个 {@link Rectangle2D}，表示此 <code>TextLayout</code> 的边界。
     */
    public Rectangle2D getBounds() {
        ensureCache();

        if (boundsRect == null) {
            Rectangle2D vb = textLine.getVisualBounds();
            if (dx != 0 || dy != 0) {
                vb.setRect(vb.getX() - dx,
                           vb.getY() - dy,
                           vb.getWidth(),
                           vb.getHeight());
            }
            boundsRect = vb;
        }

        Rectangle2D bounds = new Rectangle2D.Float();
        bounds.setRect(boundsRect);

        return bounds;
    }

    /**
     * 返回此 <code>TextLayout</code> 在给定 <code>FontRenderContext</code> 下渲染时的像素边界。
     * 给定的 <code>FontRenderContext</code> 不必与创建此 <code>TextLayout</code> 时使用的 <code>FontRenderContext</code> 相同，可以为 null。
     * 如果为 null，则使用此 <code>TextLayout</code> 的 <code>FontRenderContext</code>。
     * @param frc <code>Graphics</code> 的 <code>FontRenderContext</code>。
     * @param x 渲染此 <code>TextLayout</code> 的 x 坐标。
     * @param y 渲染此 <code>TextLayout</code> 的 y 坐标。
     * @return 一个 <code>Rectangle</code>，表示受影响的像素边界。
     * @see GlyphVector#getPixelBounds
     * @since 1.6
     */
    public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
        return textLine.getPixelBounds(frc, x, y);
    }

    /**
     * 如果此 <code>TextLayout</code> 的基本方向是从左到右，则返回 <code>true</code>，否则返回 <code>false</code>。
     * <code>TextLayout</code> 的基本方向可以是左到右（LTR）或右到左（RTL）。基本方向独立于行中实际文本的方向，
     * 实际文本的方向可以是 LTR、RTL 或混合。默认情况下，左到右的布局应左对齐。如果布局在制表位行上，
     * 制表位从左到右运行，因此逻辑上连续的布局从左到右定位。对于 RTL 布局，情况相反。默认情况下，它们应左对齐，
     * 制表位从右到左运行。
     * @return 如果此 <code>TextLayout</code> 的基本方向是从左到右，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isLeftToRight() {
        return textLine.isDirectionLTR();
    }

    /**
     * 如果此 <code>TextLayout</code> 是垂直的，则返回 <code>true</code>。
     * @return 如果此 <code>TextLayout</code> 是垂直的，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isVertical() {
        return isVerticalLine;
    }

    /**
     * 返回此 <code>TextLayout</code> 表示的字符数。
     * @return 此 <code>TextLayout</code> 中的字符数。
     */
    public int getCharacterCount() {
        return characterCount;
    }

    /*
     * 光标和命中测试
     *
     * 文本行上的位置由 TextHitInfo 的实例表示。
     * 任何字符偏移量在 0 到 characterCount-1 之间（包括）的 TextHitInfo 都表示行上的有效位置。
     * 另外，[-1, trailing] 和 [characterCount, leading] 也是有效位置，分别表示行的逻辑开始和结束位置。
     *
     * TextLayout 使用和返回的 TextHitInfo 中的字符偏移量相对于文本布局的开始位置，不一定相对于客户端使用的文本存储的开始位置。
     *
     *
     * 每个有效的 TextHitInfo 都有一个或两个光标与之关联。
     * 光标是 <code>TextLayout</code> 中的一个视觉位置，表示在 TextHitInfo 处的文本在屏幕上显示的位置。
     * 如果 TextHitInfo 表示一个方向边界上的位置，那么新插入的文本可能有两个可能的可见位置。考虑以下示例，
     * 其中大写字母表示从右到左的文本，行的整体方向是从左到右：
     *
     * 文本存储: [ a, b, C, D, E, f ]
     * 显示:        a b E D C f
     *
     * 文本命中信息 (1, t) 表示 'b' 的尾部。如果在该位置插入一个左到右的字符 'q'，它将显示在 'b' 和 'E' 之间：
     *
     * 文本存储: [ a, b, q, C, D, E, f ]
     * 显示:        a b q E D C f
     *
     * 然而，如果在 'b' 后插入一个右到左的字符 'W'，存储和显示将如下所示：
     *
     * 文本存储: [ a, b, W, C, D, E, f ]
     * 显示:        a b E D C W f
     *
     * 因此，对于原始文本存储，位置 (1, t) 应显示两个光标：一个在 'b' 和 'E' 之间，另一个在 'C' 和 'f' 之间。
     *
     *
     * 当为一个 TextHitInfo 显示两个光标时，一个光标是“强”光标，另一个是“弱”光标。
     * 强光标表示插入的字符的方向与 <code>TextLayout</code> 的方向相同的情况下，该字符将显示的位置。
     * 弱光标表示插入的字符的方向与 <code>TextLayout</code> 的方向相反的情况下，该字符将显示的位置。
     *
     *
     * 客户端不应过于关注正确的光标显示细节。TextLayout.getCaretShapes(TextHitInfo) 将返回一个包含两个路径的数组，
     * 表示光标应显示的位置。数组中的第一个路径是强光标；第二个元素（如果非 null），是弱光标。如果第二个元素为 null，
     * 则给定的 TextHitInfo 没有弱光标。
     *
     *
     * 由于文本可以视觉上重新排序，逻辑上连续的 TextHitInfo 可能不是视觉上连续的。这意味着客户端不能仅通过检查 TextHitInfo
     * 来判断命中是否代表布局中的第一个（或最后一个）光标。客户端可以调用 getVisualOtherHit()；如果视觉同伴是
     * (-1, TRAILING) 或 (characterCount, LEADING)，则命中位于布局的第一个（最后一个）光标位置。
     */

    private float[] getCaretInfo(int caret,
                                 Rectangle2D bounds,
                                 float[] info) {

        float top1X, top2X;
        float bottom1X, bottom2X;

        if (caret == 0 || caret == characterCount) {

            float pos;
            int logIndex;
            if (caret == characterCount) {
                logIndex = textLine.visualToLogical(characterCount-1);
                pos = textLine.getCharLinePosition(logIndex)
                                        + textLine.getCharAdvance(logIndex);
            }
            else {
                logIndex = textLine.visualToLogical(caret);
                pos = textLine.getCharLinePosition(logIndex);
            }
            float angle = textLine.getCharAngle(logIndex);
            float shift = textLine.getCharShift(logIndex);
            pos += angle * shift;
            top1X = top2X = pos + angle*textLine.getCharAscent(logIndex);
            bottom1X = bottom2X = pos - angle*textLine.getCharDescent(logIndex);
        }
        else {

            {
                int logIndex = textLine.visualToLogical(caret-1);
                float angle1 = textLine.getCharAngle(logIndex);
                float pos1 = textLine.getCharLinePosition(logIndex)
                                    + textLine.getCharAdvance(logIndex);
                if (angle1 != 0) {
                    pos1 += angle1 * textLine.getCharShift(logIndex);
                    top1X = pos1 + angle1*textLine.getCharAscent(logIndex);
                    bottom1X = pos1 - angle1*textLine.getCharDescent(logIndex);
                }
                else {
                    top1X = bottom1X = pos1;
                }
            }
            {
                int logIndex = textLine.visualToLogical(caret);
                float angle2 = textLine.getCharAngle(logIndex);
                float pos2 = textLine.getCharLinePosition(logIndex);
                if (angle2 != 0) {
                    pos2 += angle2*textLine.getCharShift(logIndex);
                    top2X = pos2 + angle2*textLine.getCharAscent(logIndex);
                    bottom2X = pos2 - angle2*textLine.getCharDescent(logIndex);
                }
                else {
                    top2X = bottom2X = pos2;
                }
            }
        }


                    float topX = (top1X + top2X) / 2;
        float bottomX = (bottom1X + bottom2X) / 2;

        if (info == null) {
            info = new float[2];
        }

        if (isVerticalLine) {
            info[1] = (float) ((topX - bottomX) / bounds.getWidth());
            info[0] = (float) (topX + (info[1]*bounds.getX()));
        }
        else {
            info[1] = (float) ((topX - bottomX) / bounds.getHeight());
            info[0] = (float) (bottomX + (info[1]*bounds.getMaxY()));
        }

        return info;
    }

    /**
     * 返回与 <code>hit</code> 对应的光标信息。
     * 数组的第一个元素是光标与基线的交点，表示为沿基线的距离。数组的第二个元素
     * 是光标的逆斜率（run/rise），以基线在该点的斜率来测量。
     * <p>
     * 此方法用于信息用途。要显示光标，最好使用 <code>getCaretShapes</code>。
     * @param hit 在此 <code>TextLayout</code> 中的一个字符上的点击。
     * @param bounds 构建光标信息的边界。边界是相对于基线的坐标。
     * @return 一个包含光标位置和斜率的两元素数组。返回的光标信息是相对于基线的坐标。
     * @see #getCaretShapes(int, Rectangle2D, TextLayout.CaretPolicy)
     * @see Font#getItalicAngle
     */
    public float[] getCaretInfo(TextHitInfo hit, Rectangle2D bounds) {
        ensureCache();
        checkTextHit(hit);

        return getCaretInfoTestInternal(hit, bounds);
    }

    // 此版本在浮点数组中提供额外的信息
    // 前两个值与上述相同
    // 接下来的四个值是使用点击字符的偏移（基线 + ssoffset）和
    // 自然上升和下降计算的光标的端点。
    // 这些值根据需要修剪以适应边界，但除此之外与边界无关。
    private float[] getCaretInfoTestInternal(TextHitInfo hit, Rectangle2D bounds) {
        ensureCache();
        checkTextHit(hit);

        float[] info = new float[6];

        // 首先获取旧数据
        getCaretInfo(hitToCaret(hit), bounds, info);

        // 然后添加新数据
        double iangle, ixbase, p1x, p1y, p2x, p2y;

        int charix = hit.getCharIndex();
        boolean lead = hit.isLeadingEdge();
        boolean ltr = textLine.isDirectionLTR();
        boolean horiz = !isVertical();

        if (charix == -1 || charix == characterCount) {
            // !!! 注意：这里需要非移位的基线上升和下降！
            // TextLine 应返回适当的行度量对象以获取这些值
            TextLineMetrics m = textLine.getMetrics();
            boolean low = ltr == (charix == -1);
            iangle = 0;
            if (horiz) {
                p1x = p2x = low ? 0 : m.advance;
                p1y = -m.ascent;
                p2y = m.descent;
            } else {
                p1y = p2y = low ? 0 : m.advance;
                p1x = m.descent;
                p2x = m.ascent;
            }
        } else {
            CoreMetrics thiscm = textLine.getCoreMetricsAt(charix);
            iangle = thiscm.italicAngle;
            ixbase = textLine.getCharLinePosition(charix, lead);
            if (thiscm.baselineIndex < 0) {
                // 这是一个图形，没有斜体，使用整个行高作为光标
                TextLineMetrics m = textLine.getMetrics();
                if (horiz) {
                    p1x = p2x = ixbase;
                    if (thiscm.baselineIndex == GraphicAttribute.TOP_ALIGNMENT) {
                        p1y = -m.ascent;
                        p2y = p1y + thiscm.height;
                    } else {
                        p2y = m.descent;
                        p1y = p2y - thiscm.height;
                    }
                } else {
                    p1y = p2y = ixbase;
                    p1x = m.descent;
                    p2x = m.ascent;
                    // !!! 顶部/底部调整在垂直方向上未实现
                }
            } else {
                float bo = baselineOffsets[thiscm.baselineIndex];
                if (horiz) {
                    ixbase += iangle * thiscm.ssOffset;
                    p1x = ixbase + iangle * thiscm.ascent;
                    p2x = ixbase - iangle * thiscm.descent;
                    p1y = bo - thiscm.ascent;
                    p2y = bo + thiscm.descent;
                } else {
                    ixbase -= iangle * thiscm.ssOffset;
                    p1y = ixbase + iangle * thiscm.ascent;
                    p2y = ixbase - iangle * thiscm.descent;
                    p1x = bo + thiscm.ascent;
                    p2x = bo + thiscm.descent;
                }
            }
        }

        info[2] = (float)p1x;
        info[3] = (float)p1y;
        info[4] = (float)p2x;
        info[5] = (float)p2y;

        return info;
    }

    /**
     * 返回与 <code>hit</code> 对应的光标信息。
     * 此方法是 <code>getCaretInfo</code> 的便利重载，并使用此 <code>TextLayout</code> 的自然边界。
     * @param hit 在此 <code>TextLayout</code> 中的一个字符上的点击。
     * @return 一个包含与点击对应的光标信息的数组。返回的光标信息是相对于基线的坐标。
     */
    public float[] getCaretInfo(TextHitInfo hit) {

        return getCaretInfo(hit, getNaturalBounds());
    }

    /**
     * 返回与 <code>hit</code> 对应的光标索引。
     * 光标从左到右（从上到下）编号，从零开始。此方法总是将光标放置在点击字符的旁边，
     * 在字符的指定侧。
     * @param hit 在此 <code>TextLayout</code> 中的一个字符上的点击。
     * @return 与指定点击对应的光标索引。
     */
    private int hitToCaret(TextHitInfo hit) {

        int hitIndex = hit.getCharIndex();

        if (hitIndex < 0) {
            return textLine.isDirectionLTR() ? 0 : characterCount;
        } else if (hitIndex >= characterCount) {
            return textLine.isDirectionLTR() ? characterCount : 0;
        }

        int visIndex = textLine.logicalToVisual(hitIndex);

        if (hit.isLeadingEdge() != textLine.isCharLTR(hitIndex)) {
            ++visIndex;
        }

        return visIndex;
    }

    /**
     * 给定一个光标索引，返回一个光标位于该索引的点击。
     * 该点击不保证是强点击！！！
     *
     * @param caret 一个光标索引。
     * @return 一个光标位于请求索引的布局点击。
     */
    private TextHitInfo caretToHit(int caret) {

        if (caret == 0 || caret == characterCount) {

            if ((caret == characterCount) == textLine.isDirectionLTR()) {
                return TextHitInfo.leading(characterCount);
            }
            else {
                return TextHitInfo.trailing(-1);
            }
        }
        else {

            int charIndex = textLine.visualToLogical(caret);
            boolean leading = textLine.isCharLTR(charIndex);

            return leading? TextHitInfo.leading(charIndex)
                            : TextHitInfo.trailing(charIndex);
        }
    }

    private boolean caretIsValid(int caret) {

        if (caret == characterCount || caret == 0) {
            return true;
        }

        int offset = textLine.visualToLogical(caret);

        if (!textLine.isCharLTR(offset)) {
            offset = textLine.visualToLogical(caret-1);
            if (textLine.isCharLTR(offset)) {
                return true;
            }
        }

        // 此时，位于偏移处的字符的前缘
        // 在给定的光标处。

        return textLine.caretAtOffsetIsValid(offset);
    }

    /**
     * 返回右侧（底部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 如果点击字符索引超出范围，将抛出 <code>IllegalArgumentException</code>。
     * @param hit 在此布局中的一个点击。
     * @return 一个光标出现在提供的点击光标的右侧（底部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextRightHit(TextHitInfo hit) {
        ensureCache();
        checkTextHit(hit);

        int caret = hitToCaret(hit);

        if (caret == characterCount) {
            return null;
        }

        do {
            ++caret;
        } while (!caretIsValid(caret));

        return caretToHit(caret);
    }

    /**
     * 返回右侧（底部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 该点击位于指定偏移处的强光标的右侧，由指定的策略确定。
     * 返回的点击是两个可能的点击中较强的一个，由指定的策略确定。
     * @param offset 在此 <code>TextLayout</code> 中的一个插入偏移。
     * 不能小于 0 或大于此 <code>TextLayout</code> 对象的字符数。
     * @param policy 用于选择强光标的策略。
     * @return 一个光标出现在提供的点击光标的右侧（底部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextRightHit(int offset, CaretPolicy policy) {

        if (offset < 0 || offset > characterCount) {
            throw new IllegalArgumentException("Offset out of bounds in TextLayout.getNextRightHit()");
        }

        if (policy == null) {
            throw new IllegalArgumentException("Null CaretPolicy passed to TextLayout.getNextRightHit()");
        }

        TextHitInfo hit1 = TextHitInfo.afterOffset(offset);
        TextHitInfo hit2 = hit1.getOtherHit();

        TextHitInfo nextHit = getNextRightHit(policy.getStrongCaret(hit1, hit2, this));

        if (nextHit != null) {
            TextHitInfo otherHit = getVisualOtherHit(nextHit);
            return policy.getStrongCaret(otherHit, nextHit, this);
        }
        else {
            return null;
        }
    }

    /**
     * 返回右侧（底部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 该点击位于指定偏移处的强光标的右侧，由默认策略确定。
     * 返回的点击是两个可能的点击中较强的一个，由默认策略确定。
     * @param offset 在此 <code>TextLayout</code> 中的一个插入偏移。
     * 不能小于 0 或大于此 <code>TextLayout</code> 对象的字符数。
     * @return 一个光标出现在提供的点击光标的右侧（底部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextRightHit(int offset) {

        return getNextRightHit(offset, DEFAULT_CARET_POLICY);
    }

    /**
     * 返回左侧（顶部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 如果点击字符索引超出范围，将抛出 <code>IllegalArgumentException</code>。
     * @param hit 在此 <code>TextLayout</code> 中的一个点击。
     * @return 一个光标出现在提供的点击光标的左侧（顶部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextLeftHit(TextHitInfo hit) {
        ensureCache();
        checkTextHit(hit);

        int caret = hitToCaret(hit);

        if (caret == 0) {
            return null;
        }

        do {
            --caret;
        } while(!caretIsValid(caret));

        return caretToHit(caret);
    }

    /**
     * 返回左侧（顶部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 该点击位于指定偏移处的强光标的左侧，由指定的策略确定。
     * 返回的点击是两个可能的点击中较强的一个，由指定的策略确定。
     * @param offset 在此 <code>TextLayout</code> 中的一个插入偏移。
     * 不能小于 0 或大于此 <code>TextLayout</code> 对象的字符数。
     * @param policy 用于选择强光标的策略。
     * @return 一个光标出现在提供的点击光标的左侧（顶部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextLeftHit(int offset, CaretPolicy policy) {

        if (policy == null) {
            throw new IllegalArgumentException("Null CaretPolicy passed to TextLayout.getNextLeftHit()");
        }

        if (offset < 0 || offset > characterCount) {
            throw new IllegalArgumentException("Offset out of bounds in TextLayout.getNextLeftHit()");
        }

        TextHitInfo hit1 = TextHitInfo.afterOffset(offset);
        TextHitInfo hit2 = hit1.getOtherHit();

        TextHitInfo nextHit = getNextLeftHit(policy.getStrongCaret(hit1, hit2, this));

        if (nextHit != null) {
            TextHitInfo otherHit = getVisualOtherHit(nextHit);
            return policy.getStrongCaret(otherHit, nextHit, this);
        }
        else {
            return null;
        }
    }

    /**
     * 返回左侧（顶部）下一个光标的点击；如果没有这样的点击，返回 <code>null</code>。
     * 该点击位于指定偏移处的强光标的左侧，由默认策略确定。
     * 返回的点击是两个可能的点击中较强的一个，由默认策略确定。
     * @param offset 在此 <code>TextLayout</code> 中的一个插入偏移。
     * 不能小于 0 或大于此 <code>TextLayout</code> 对象的字符数。
     * @return 一个光标出现在提供的点击光标的左侧（顶部）的下一个位置的点击，或 <code>null</code>。
     */
    public TextHitInfo getNextLeftHit(int offset) {

        return getNextLeftHit(offset, DEFAULT_CARET_POLICY);
    }

    /**
     * 返回指定点击光标的另一侧的点击。
     * @param hit 指定的点击
     * @return 一个位于指定点击光标的另一侧的点击。
     */
    public TextHitInfo getVisualOtherHit(TextHitInfo hit) {

        ensureCache();
        checkTextHit(hit);

        int hitCharIndex = hit.getCharIndex();

        int charIndex;
        boolean leading;

        if (hitCharIndex == -1 || hitCharIndex == characterCount) {

            int visIndex;
            if (textLine.isDirectionLTR() == (hitCharIndex == -1)) {
                visIndex = 0;
            }
            else {
                visIndex = characterCount-1;
            }

            charIndex = textLine.visualToLogical(visIndex);

            if (textLine.isDirectionLTR() == (hitCharIndex == -1)) {
                // 在左端
                leading = textLine.isCharLTR(charIndex);
            }
            else {
                // 在右端
                leading = !textLine.isCharLTR(charIndex);
            }
        }
        else {


                        int visIndex = textLine.logicalToVisual(hitCharIndex);

            boolean movedToRight;
            if (textLine.isCharLTR(hitCharIndex) == hit.isLeadingEdge()) {
                --visIndex;
                movedToRight = false;
            }
            else {
                ++visIndex;
                movedToRight = true;
            }

            if (visIndex > -1 && visIndex < characterCount) {
                charIndex = textLine.visualToLogical(visIndex);
                leading = movedToRight == textLine.isCharLTR(charIndex);
            }
            else {
                charIndex =
                    (movedToRight == textLine.isDirectionLTR())? characterCount : -1;
                leading = charIndex == characterCount;
            }
        }

        return leading? TextHitInfo.leading(charIndex) :
                                TextHitInfo.trailing(charIndex);
    }

    private double[] getCaretPath(TextHitInfo hit, Rectangle2D bounds) {
        float[] info = getCaretInfo(hit, bounds);
        return new double[] { info[2], info[3], info[4], info[5] };
    }

    /**
     * 返回一个包含四个浮点数的数组，对应于光标的端点
     * x0, y0, x1, y1。
     *
     * 这个方法沿着光标的斜率创建一条线，这条线在光标位置与基线相交，
     * 并从基线上方的上升高度延伸到基线下方的下降高度。
     */
    private double[] getCaretPath(int caret, Rectangle2D bounds,
                                  boolean clipToBounds) {

        float[] info = getCaretInfo(caret, bounds, null);

        double pos = info[0];
        double slope = info[1];

        double x0, y0, x1, y1;
        double x2 = -3141.59, y2 = -2.7; // 这些值是为了让编译器满意

        double left = bounds.getX();
        double right = left + bounds.getWidth();
        double top = bounds.getY();
        double bottom = top + bounds.getHeight();

        boolean threePoints = false;

        if (isVerticalLine) {

            if (slope >= 0) {
                x0 = left;
                x1 = right;
            }
            else {
                x1 = left;
                x0 = right;
            }

            y0 = pos + x0 * slope;
            y1 = pos + x1 * slope;

            // y0 <= y1, 总是

            if (clipToBounds) {
                if (y0 < top) {
                    if (slope <= 0 || y1 <= top) {
                        y0 = y1 = top;
                    }
                    else {
                        threePoints = true;
                        y0 = top;
                        y2 = top;
                        x2 = x1 + (top-y1)/slope;
                        if (y1 > bottom) {
                            y1 = bottom;
                        }
                    }
                }
                else if (y1 > bottom) {
                    if (slope >= 0 || y0 >= bottom) {
                        y0 = y1 = bottom;
                    }
                    else {
                        threePoints = true;
                        y1 = bottom;
                        y2 = bottom;
                        x2 = x0 + (bottom-x1)/slope;
                    }
                }
            }

        }
        else {

            if (slope >= 0) {
                y0 = bottom;
                y1 = top;
            }
            else {
                y1 = bottom;
                y0 = top;
            }

            x0 = pos - y0 * slope;
            x1 = pos - y1 * slope;

            // x0 <= x1, 总是

            if (clipToBounds) {
                if (x0 < left) {
                    if (slope <= 0 || x1 <= left) {
                        x0 = x1 = left;
                    }
                    else {
                        threePoints = true;
                        x0 = left;
                        x2 = left;
                        y2 = y1 - (left-x1)/slope;
                        if (x1 > right) {
                            x1 = right;
                        }
                    }
                }
                else if (x1 > right) {
                    if (slope >= 0 || x0 >= right) {
                        x0 = x1 = right;
                    }
                    else {
                        threePoints = true;
                        x1 = right;
                        x2 = right;
                        y2 = y0 - (right-x0)/slope;
                    }
                }
            }
        }

        return threePoints?
                    new double[] { x0, y0, x2, y2, x1, y1 } :
                    new double[] { x0, y0, x1, y1 };
    }


    private static GeneralPath pathToShape(double[] path, boolean close, LayoutPathImpl lp) {
        GeneralPath result = new GeneralPath(GeneralPath.WIND_EVEN_ODD, path.length);
        result.moveTo((float)path[0], (float)path[1]);
        for (int i = 2; i < path.length; i += 2) {
            result.lineTo((float)path[i], (float)path[i+1]);
        }
        if (close) {
            result.closePath();
        }

        if (lp != null) {
            result = (GeneralPath)lp.mapShape(result);
        }
        return result;
    }

    /**
     * 返回一个表示在指定范围内指定位置的光标的 {@link Shape}。
     * @param hit 生成光标的位置
     * @param bounds 用于生成光标的 {@code TextLayout} 的范围。范围以基线为基准。
     * @return 一个表示光标的 {@code Shape}。返回的形状在标准坐标系中。
     */
    public Shape getCaretShape(TextHitInfo hit, Rectangle2D bounds) {
        ensureCache();
        checkTextHit(hit);

        if (bounds == null) {
            throw new IllegalArgumentException("传递给 TextLayout.getCaret() 的 Rectangle2D 为空");
        }

        return pathToShape(getCaretPath(hit, bounds), false, textLine.getLayoutPath());
    }

    /**
     * 返回一个表示在指定位置的光标的 {@code Shape}，该位置在该 {@code TextLayout} 的自然范围内。
     * @param hit 生成光标的位置
     * @return 一个表示光标的 {@code Shape}。返回的形状在标准坐标系中。
     */
    public Shape getCaretShape(TextHitInfo hit) {

        return getCaretShape(hit, getNaturalBounds());
    }

    /**
     * 返回两个 {@code TextHitInfo} 中的“较强”者。这两个 {@code TextHitInfo} 应该是逻辑或视觉上的对应者。它们的有效性不会被检查。
     */
    private final TextHitInfo getStrongHit(TextHitInfo hit1, TextHitInfo hit2) {

        // 目前我们使用以下规则来确定强击：
        // 位于较低级别的字符上的击中比位于较高级别的字符上的击中更强。
        // 如果这个规则打成平手，位于字符前缘的击中获胜。
        // 如果这个规则也打成平手，hit1 获胜。这两个规则不应该打成平手，除非这些信息不是某种意义上的对应者。

        byte hit1Level = getCharacterLevel(hit1.getCharIndex());
        byte hit2Level = getCharacterLevel(hit2.getCharIndex());

        if (hit1Level == hit2Level) {
            if (hit2.isLeadingEdge() && !hit1.isLeadingEdge()) {
                return hit2;
            }
            else {
                return hit1;
            }
        }
        else {
            return (hit1Level < hit2Level)? hit1 : hit2;
        }
    }

    /**
     * 返回索引处字符的级别。索引 -1 和 {@code characterCount} 被分配为该 {@code TextLayout} 的基础级别。
     * @param index 要获取级别的字符的索引
     * @return 指定索引处字符的级别。
     */
    public byte getCharacterLevel(int index) {

        // 嗯，允许在端点处的索引？目前是的。
        if (index < -1 || index > characterCount) {
            throw new IllegalArgumentException("在 getCharacterLevel 中索引超出范围。");
        }

        ensureCache();
        if (index == -1 || index == characterCount) {
             return (byte) (textLine.isDirectionLTR()? 0 : 1);
        }

        return textLine.getCharLevel(index);
    }

    /**
     * 返回对应于强光标和弱光标的两条路径。
     * @param offset 该 {@code TextLayout} 中的一个偏移量
     * @param bounds 要扩展光标的范围。范围以基线为基准。
     * @param policy 指定的 {@code CaretPolicy}
     * @return 一个包含两条路径的数组。元素零是强光标。如果有两个光标，元素一是弱光标，否则为 {@code null}。返回的形状在标准坐标系中。
     */
    public Shape[] getCaretShapes(int offset, Rectangle2D bounds, CaretPolicy policy) {

        ensureCache();

        if (offset < 0 || offset > characterCount) {
            throw new IllegalArgumentException("在 TextLayout.getCaretShapes() 中偏移量超出范围");
        }

        if (bounds == null) {
            throw new IllegalArgumentException("传递给 TextLayout.getCaretShapes() 的 Rectangle2D 为空");
        }

        if (policy == null) {
            throw new IllegalArgumentException("传递给 TextLayout.getCaretShapes() 的 CaretPolicy 为空");
        }

        Shape[] result = new Shape[2];

        TextHitInfo hit = TextHitInfo.afterOffset(offset);

        int hitCaret = hitToCaret(hit);

        LayoutPathImpl lp = textLine.getLayoutPath();
        Shape hitShape = pathToShape(getCaretPath(hit, bounds), false, lp);
        TextHitInfo otherHit = hit.getOtherHit();
        int otherCaret = hitToCaret(otherHit);

        if (hitCaret == otherCaret) {
            result[0] = hitShape;
        }
        else { // 多于一个光标
            Shape otherShape = pathToShape(getCaretPath(otherHit, bounds), false, lp);

            TextHitInfo strongHit = policy.getStrongCaret(hit, otherHit, this);
            boolean hitIsStrong = strongHit.equals(hit);

            if (hitIsStrong) { // 那么 other 是弱的
                result[0] = hitShape;
                result[1] = otherShape;
            }
            else {
                result[0] = otherShape;
                result[1] = hitShape;
            }
        }

        return result;
    }

    /**
     * 返回对应于强光标和弱光标的两条路径。此方法是 {@code getCaretShapes} 的便利重载，使用默认的光标策略。
     * @param offset 该 {@code TextLayout} 中的一个偏移量
     * @param bounds 要扩展光标的范围。此范围以基线为基准。
     * @return 两条路径，对应于由 {@code DEFAULT_CARET_POLICY} 定义的强光标和弱光标。这些路径在标准坐标系中。
     */
    public Shape[] getCaretShapes(int offset, Rectangle2D bounds) {
        // {sfb} 参数检查在重载版本中完成
        return getCaretShapes(offset, bounds, DEFAULT_CARET_POLICY);
    }

    /**
     * 返回对应于强光标和弱光标的两条路径。此方法是 {@code getCaretShapes} 的便利重载，使用默认的光标策略和该 {@code TextLayout} 对象的自然范围。
     * @param offset 该 {@code TextLayout} 中的一个偏移量
     * @return 两条路径，对应于由 {@code DEFAULT_CARET_POLICY} 定义的强光标和弱光标。这些路径在标准坐标系中。
     */
    public Shape[] getCaretShapes(int offset) {
        // {sfb} 参数检查在重载版本中完成
        return getCaretShapes(offset, getNaturalBounds(), DEFAULT_CARET_POLICY);
    }

    // 一个返回包含给定路径的路径的工具
    // Path0 必须是 path1 的左侧或顶部
    // {jbr} 不再假设 path0 和 path1 的大小。
    private GeneralPath boundingShape(double[] path0, double[] path1) {

        // 实际上，我们希望路径是一个包含 path0 和 path1 中所有点的凸包。但我们可以用更少的点来实现。我们需要防止连接 path0 和 path1 的两个线段交叉。因此，如果我们从上到下遍历 path0，我们将从下到上遍历 path1（反之亦然）。

        GeneralPath result = pathToShape(path0, false, null);

        boolean sameDirection;

        if (isVerticalLine) {
            sameDirection = (path0[1] > path0[path0.length-1]) ==
                            (path1[1] > path1[path1.length-1]);
        }
        else {
            sameDirection = (path0[0] > path0[path0.length-2]) ==
                            (path1[0] > path1[path1.length-2]);
        }

        int start;
        int limit;
        int increment;

        if (sameDirection) {
            start = path1.length-2;
            limit = -2;
            increment = -2;
        }
        else {
            start = 0;
            limit = path1.length;
            increment = 2;
        }

        for (int i = start; i != limit; i += increment) {
            result.lineTo((float)path1[i], (float)path1[i+1]);
        }

        result.closePath();

        return result;
    }

    // 一个将一对光标转换为边界路径的工具
    // {jbr} 形状永远不会超出范围。
    private GeneralPath caretBoundingShape(int caret0,
                                           int caret1,
                                           Rectangle2D bounds) {

        if (caret0 > caret1) {
            int temp = caret0;
            caret0 = caret1;
            caret1 = temp;
        }

        return boundingShape(getCaretPath(caret0, bounds, true),
                             getCaretPath(caret1, bounds, true));
    }

    /*
     * 一个返回布局左侧（顶部）区域的边界路径的工具。
     * 形状永远不会超出范围。
     */
    private GeneralPath leftShape(Rectangle2D bounds) {

        double[] path0;
        if (isVerticalLine) {
            path0 = new double[] { bounds.getX(), bounds.getY(),
                                       bounds.getX() + bounds.getWidth(),
                                       bounds.getY() };
        } else {
            path0 = new double[] { bounds.getX(),
                                       bounds.getY() + bounds.getHeight(),
                                       bounds.getX(), bounds.getY() };
        }

        double[] path1 = getCaretPath(0, bounds, true);

        return boundingShape(path0, path1);
    }

    /*
     * 一个返回布局右侧（底部）区域的边界路径的工具。
     */
    private GeneralPath rightShape(Rectangle2D bounds) {
        double[] path1;
        if (isVerticalLine) {
            path1 = new double[] {
                bounds.getX(),
                bounds.getY() + bounds.getHeight(),
                bounds.getX() + bounds.getWidth(),
                bounds.getY() + bounds.getHeight()
            };
        } else {
            path1 = new double[] {
                bounds.getX() + bounds.getWidth(),
                bounds.getY() + bounds.getHeight(),
                bounds.getX() + bounds.getWidth(),
                bounds.getY()
            };
        }


                    double[] path0 = getCaretPath(characterCount, bounds, true);

        return boundingShape(path0, path1);
    }

    /**
     * 返回与视觉选择对应的文本的逻辑范围。
     * @param firstEndpoint 视觉范围的一个端点
     * @param secondEndpoint 视觉范围的另一个端点。
     * 这个端点可以小于 <code>firstEndpoint</code>。
     * @return 一个表示所选范围的起始/限制对的整数数组。
     * @see #getVisualHighlightShape(TextHitInfo, TextHitInfo, Rectangle2D)
     */
    public int[] getLogicalRangesForVisualSelection(TextHitInfo firstEndpoint,
                                                    TextHitInfo secondEndpoint) {
        ensureCache();

        checkTextHit(firstEndpoint);
        checkTextHit(secondEndpoint);

        // !!! 可能需要优化所有从左到右的文本

        boolean[] included = new boolean[characterCount];

        int startIndex = hitToCaret(firstEndpoint);
        int limitIndex = hitToCaret(secondEndpoint);

        if (startIndex > limitIndex) {
            int t = startIndex;
            startIndex = limitIndex;
            limitIndex = t;
        }

        /*
         * 现在我们有了选择范围开始和结束处的视觉索引
         * 遍历运行，标记包含在视觉范围内的字符
         * 可能有更有效的方法，但这种方法应该可以工作，所以嘿
         */

        if (startIndex < limitIndex) {
            int visIndex = startIndex;
            while (visIndex < limitIndex) {
                included[textLine.visualToLogical(visIndex)] = true;
                ++visIndex;
            }
        }

        /*
         * 计算我们有多少个运行，应该是1或2，但也许情况特别奇怪
         */
        int count = 0;
        boolean inrun = false;
        for (int i = 0; i < characterCount; i++) {
            if (included[i] != inrun) {
                inrun = !inrun;
                if (inrun) {
                    count++;
                }
            }
        }

        int[] ranges = new int[count * 2];
        count = 0;
        inrun = false;
        for (int i = 0; i < characterCount; i++) {
            if (included[i] != inrun) {
                ranges[count++] = i;
                inrun = !inrun;
            }
        }
        if (inrun) {
            ranges[count++] = characterCount;
        }

        return ranges;
    }

    /**
     * 返回指定范围内视觉选择的路径，扩展到 <code>bounds</code>。
     * <p>
     * 如果选择包括最左边（最顶部）的位置，选择将扩展到 <code>bounds</code> 的左边（顶部）。
     * 如果选择包括最右边（最底部）的位置，选择将扩展到 <code>bounds</code> 的右边（底部）。
     * 选择的高度（垂直线上的宽度）总是扩展到 <code>bounds</code>。
     * <p>
     * 虽然选择总是连续的，但在包含混合方向文本的行上，逻辑上选择的文本可能是不连续的。
     * 可以使用 <code>getLogicalRangesForVisualSelection</code> 检索逻辑上选择的文本范围。
     * 例如，考虑文本 'ABCdef'，其中大写字母表示从右到左的文本，在从右到左的行上渲染，视觉选择从 0L（'A' 的前缘）到 3T（'d' 的后缘）。
     * 文本显示如下，加粗下划线区域表示选择：
     * <br><pre>
     *    d<u><b>efCBA  </b></u>
     * </pre>
     * 逻辑选择范围是 0-3，4-6（ABC，ef），因为视觉上连续的文本在逻辑上是不连续的。
     * 还要注意，由于布局的最右位置（'A' 的右边）被选中，选择扩展到了 <code>bounds</code> 的右边。
     * @param firstEndpoint 视觉选择的一个端点
     * @param secondEndpoint 视觉选择的另一个端点
     * @param bounds 要扩展选择的边界矩形。
     *     这是在基线相对坐标中。
     * @return 包围选择的 <code>Shape</code>。这是在标准坐标中。
     * @see #getLogicalRangesForVisualSelection(TextHitInfo, TextHitInfo)
     * @see #getLogicalHighlightShape(int, int, Rectangle2D)
     */
    public Shape getVisualHighlightShape(TextHitInfo firstEndpoint,
                                        TextHitInfo secondEndpoint,
                                        Rectangle2D bounds)
    {
        ensureCache();

        checkTextHit(firstEndpoint);
        checkTextHit(secondEndpoint);

        if(bounds == null) {
                throw new IllegalArgumentException("Null Rectangle2D passed to TextLayout.getVisualHighlightShape()");
        }

        GeneralPath result = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        int firstCaret = hitToCaret(firstEndpoint);
        int secondCaret = hitToCaret(secondEndpoint);

        result.append(caretBoundingShape(firstCaret, secondCaret, bounds),
                      false);

        if (firstCaret == 0 || secondCaret == 0) {
            GeneralPath ls = leftShape(bounds);
            if (!ls.getBounds().isEmpty())
                result.append(ls, false);
        }

        if (firstCaret == characterCount || secondCaret == characterCount) {
            GeneralPath rs = rightShape(bounds);
            if (!rs.getBounds().isEmpty()) {
                result.append(rs, false);
            }
        }

        LayoutPathImpl lp = textLine.getLayoutPath();
        if (lp != null) {
            result = (GeneralPath)lp.mapShape(result); // dlf cast safe?
        }

        return  result;
    }

    /**
     * 返回指定范围内视觉选择的 <code>Shape</code>，扩展到边界。
     * 这是 <code>getVisualHighlightShape</code> 的便捷重载方法，使用此 <code>TextLayout</code> 的自然边界。
     * @param firstEndpoint 视觉选择的一个端点
     * @param secondEndpoint 视觉选择的另一个端点
     * @return 包围选择的 <code>Shape</code>。这是在标准坐标中。
     */
    public Shape getVisualHighlightShape(TextHitInfo firstEndpoint,
                                             TextHitInfo secondEndpoint) {
        return getVisualHighlightShape(firstEndpoint, secondEndpoint, getNaturalBounds());
    }

    /**
     * 返回指定范围内逻辑选择的 <code>Shape</code>，扩展到指定的 <code>bounds</code>。
     * <p>
     * 如果选择范围包括第一个逻辑字符，选择将扩展到 <code>bounds</code> 的前部分。
     * 如果范围包括最后一个逻辑字符，选择将扩展到 <code>bounds</code> 的后部分。
     * 选择的高度（垂直线上的宽度）总是扩展到 <code>bounds</code>。
     * <p>
     * 在包含混合方向文本的行上，选择可能是不连续的。
     * 只有在逻辑范围内的字符才会显示为已选择。例如，考虑文本 'ABCdef'，其中大写字母表示从右到左的文本，在从右到左的行上渲染，逻辑选择从 0 到 4（'ABCd'）。
     * 文本显示如下，加粗表示选择，下划线表示扩展：
     * <br><pre>
     *    <u><b>d</b></u>ef<u><b>CBA  </b></u>
     * </pre>
     * 选择是不连续的，因为选中的字符在视觉上是不连续的。
     * 还要注意，由于范围包括第一个逻辑字符（A），选择扩展到了 <code>bounds</code> 的前部分，在这种情况下（从右到左的行）是 <code>bounds</code> 的右部分。
     * @param firstEndpoint 要选择的字符范围的一个端点
     * @param secondEndpoint 要选择的字符范围的另一个端点。
     * 可以小于 <code>firstEndpoint</code>。范围包括 <code>min(firstEndpoint, secondEndpoint)</code> 处的字符，但不包括 <code>max(firstEndpoint, secondEndpoint)</code> 处的字符。
     * @param bounds 要扩展选择的边界矩形。
     *     这是在基线相对坐标中。
     * @return 包围选择的区域。这是在标准坐标中。
     * @see #getVisualHighlightShape(TextHitInfo, TextHitInfo, Rectangle2D)
     */
    public Shape getLogicalHighlightShape(int firstEndpoint,
                                         int secondEndpoint,
                                         Rectangle2D bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("Null Rectangle2D passed to TextLayout.getLogicalHighlightShape()");
        }

        ensureCache();

        if (firstEndpoint > secondEndpoint) {
            int t = firstEndpoint;
            firstEndpoint = secondEndpoint;
            secondEndpoint = t;
        }

        if(firstEndpoint < 0 || secondEndpoint > characterCount) {
            throw new IllegalArgumentException("Range is invalid in TextLayout.getLogicalHighlightShape()");
        }

        GeneralPath result = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        int[] carets = new int[10]; // 这种情况会处理所有情况吗？
        int count = 0;

        if (firstEndpoint < secondEndpoint) {
            int logIndex = firstEndpoint;
            do {
                carets[count++] = hitToCaret(TextHitInfo.leading(logIndex));
                boolean ltr = textLine.isCharLTR(logIndex);

                do {
                    logIndex++;
                } while (logIndex < secondEndpoint && textLine.isCharLTR(logIndex) == ltr);

                int hitCh = logIndex;
                carets[count++] = hitToCaret(TextHitInfo.trailing(hitCh - 1));

                if (count == carets.length) {
                    int[] temp = new int[carets.length + 10];
                    System.arraycopy(carets, 0, temp, 0, count);
                    carets = temp;
                }
            } while (logIndex < secondEndpoint);
        }
        else {
            count = 2;
            carets[0] = carets[1] = hitToCaret(TextHitInfo.leading(firstEndpoint));
        }

        // 现在为每对光标创建路径

        for (int i = 0; i < count; i += 2) {
            result.append(caretBoundingShape(carets[i], carets[i+1], bounds),
                          false);
        }

        if (firstEndpoint != secondEndpoint) {
            if ((textLine.isDirectionLTR() && firstEndpoint == 0) || (!textLine.isDirectionLTR() &&
                                                                      secondEndpoint == characterCount)) {
                GeneralPath ls = leftShape(bounds);
                if (!ls.getBounds().isEmpty()) {
                    result.append(ls, false);
                }
            }

            if ((textLine.isDirectionLTR() && secondEndpoint == characterCount) ||
                (!textLine.isDirectionLTR() && firstEndpoint == 0)) {

                GeneralPath rs = rightShape(bounds);
                if (!rs.getBounds().isEmpty()) {
                    result.append(rs, false);
                }
            }
        }

        LayoutPathImpl lp = textLine.getLayoutPath();
        if (lp != null) {
            result = (GeneralPath)lp.mapShape(result); // dlf cast safe?
        }
        return result;
    }

    /**
     * 返回指定范围内逻辑选择的 <code>Shape</code>，扩展到此 <code>TextLayout</code> 的自然边界。
     * 这是 <code>getLogicalHighlightShape</code> 的便捷重载方法，使用此 <code>TextLayout</code> 的自然边界。
     * @param firstEndpoint 要选择的字符范围的一个端点
     * @param secondEndpoint 要选择的字符范围的另一个端点。
     * 可以小于 <code>firstEndpoint</code>。范围包括 <code>min(firstEndpoint, secondEndpoint)</code> 处的字符，但不包括 <code>max(firstEndpoint, secondEndpoint)</code> 处的字符。
     * @return 包围选择的 <code>Shape</code>。这是在标准坐标中。
     */
    public Shape getLogicalHighlightShape(int firstEndpoint, int secondEndpoint) {

        return getLogicalHighlightShape(firstEndpoint, secondEndpoint, getNaturalBounds());
    }

    /**
     * 返回指定范围内字符的黑色框边界。
     * 黑色框边界是由起始和结束之间的所有字符对应的字形的边界框的并集组成的区域。这个区域可能是不连续的。
     * @param firstEndpoint 字符范围的一个端点
     * @param secondEndpoint 字符范围的另一个端点。可以小于 <code>firstEndpoint</code>。
     * @return 包围黑色框边界的 <code>Shape</code>。这是在标准坐标中。
     */
    public Shape getBlackBoxBounds(int firstEndpoint, int secondEndpoint) {
        ensureCache();

        if (firstEndpoint > secondEndpoint) {
            int t = firstEndpoint;
            firstEndpoint = secondEndpoint;
            secondEndpoint = t;
        }

        if (firstEndpoint < 0 || secondEndpoint > characterCount) {
            throw new IllegalArgumentException("Invalid range passed to TextLayout.getBlackBoxBounds()");
        }

        /*
         * 返回一个由从 firstEndpoint 到 limit 的所有字符的边界框组成的区域
         */

        GeneralPath result = new GeneralPath(GeneralPath.WIND_NON_ZERO);

        if (firstEndpoint < characterCount) {
            for (int logIndex = firstEndpoint;
                        logIndex < secondEndpoint;
                        logIndex++) {

                Rectangle2D r = textLine.getCharBounds(logIndex);
                if (!r.isEmpty()) {
                    result.append(r, false);
                }
            }
        }

        if (dx != 0 || dy != 0) {
            AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);
            result = (GeneralPath)tx.createTransformedShape(result);
        }
        LayoutPathImpl lp = textLine.getLayoutPath();
        if (lp != null) {
            result = (GeneralPath)lp.mapShape(result);
        }

        //return new Highlight(result, false);
        return result;
    }

    /**
     * 返回从点 (x,&nbsp;y) 到 <code>caretInfo</code> 定义的行方向上的光标距离。
     * 如果点在水平线上的光标的左边，或在垂直线上的光标的上方，距离为负。
     * 用于 hitTestChar 的工具方法。
     */
    private float caretToPointDistance(float[] caretInfo, float x, float y) {
        // distanceOffBaseline 为负表示你在基线之上


                    float lineDistance = isVerticalLine? y : x;
        float distanceOffBaseline = isVerticalLine? -x : y;

        return lineDistance - caretInfo[0] +
            (distanceOffBaseline*caretInfo[1]);
    }

    /**
     * 返回与指定点对应的 <code>TextHitInfo</code>。
     * 超出 <code>TextLayout</code> 边界的坐标将映射到第一个逻辑字符的前缘或最后一个逻辑字符的后缘，
     * 具体取决于该字符在行中的位置。仅使用沿基线的方向进行此评估。
     * @param x 从该 <code>TextLayout</code> 原点的 x 偏移量。这是标准坐标。
     * @param y 从该 <code>TextLayout</code> 原点的 y 偏移量。这是标准坐标。
     * @param bounds <code>TextLayout</code> 的边界。这是相对于基线的坐标。
     * @return 描述指定点下的字符及其边缘（前缘或后缘）的命中。
     */
    public TextHitInfo hitTestChar(float x, float y, Rectangle2D bounds) {
        // 检查边界条件

        LayoutPathImpl lp = textLine.getLayoutPath();
        boolean prev = false;
        if (lp != null) {
            Point2D.Float pt = new Point2D.Float(x, y);
            prev = lp.pointToPath(pt, pt);
            x = pt.x;
            y = pt.y;
        }

        if (isVertical()) {
            if (y < bounds.getMinY()) {
                return TextHitInfo.leading(0);
            } else if (y >= bounds.getMaxY()) {
                return TextHitInfo.trailing(characterCount-1);
            }
        } else {
            if (x < bounds.getMinX()) {
                return isLeftToRight() ? TextHitInfo.leading(0) : TextHitInfo.trailing(characterCount-1);
            } else if (x >= bounds.getMaxX()) {
                return isLeftToRight() ? TextHitInfo.trailing(characterCount-1) : TextHitInfo.leading(0);
            }
        }

        // 修订后的命中测试
        // 原始方法似乎过于复杂，并且在斜体偏移时表现不佳
        // 自然倾向是向你想要命中的字符移动
        // 因此我们将测量到每个字符视觉边界中心的距离，选择最近的一个，然后查看该点位于字符中心线（斜体）的哪一侧。
        // 这使得击中小字符更容易，这在视觉上位于相邻的大字符上方时可能会有些奇怪。这在双向文本中有所不同，因此我可能需要再次重新审视这一点。

        double distance = Double.MAX_VALUE;
        int index = 0;
        int trail = -1;
        CoreMetrics lcm = null;
        float icx = 0, icy = 0, ia = 0, cy = 0, dya = 0, ydsq = 0;

        for (int i = 0; i < characterCount; ++i) {
            if (!textLine.caretAtOffsetIsValid(i)) {
                continue;
            }
            if (trail == -1) {
                trail = i;
            }
            CoreMetrics cm = textLine.getCoreMetricsAt(i);
            if (cm != lcm) {
                lcm = cm;
                // 暂时绕过基线混乱
                if (cm.baselineIndex == GraphicAttribute.TOP_ALIGNMENT) {
                    cy = -(textLine.getMetrics().ascent - cm.ascent) + cm.ssOffset;
                } else if (cm.baselineIndex == GraphicAttribute.BOTTOM_ALIGNMENT) {
                    cy = textLine.getMetrics().descent - cm.descent + cm.ssOffset;
                } else {
                    cy = cm.effectiveBaselineOffset(baselineOffsets) + cm.ssOffset;
                }
                float dy = (cm.descent - cm.ascent) / 2 - cy;
                dya = dy * cm.italicAngle;
                cy += dy;
                ydsq = (cy - y)*(cy - y);
            }
            float cx = textLine.getCharXPosition(i);
            float ca = textLine.getCharAdvance(i);
            float dx = ca / 2;
            cx += dx - dya;

            // 沿基线（x 方向）的接近度是 y 方向接近度的两倍重要
            double nd = Math.sqrt(4*(cx - x)*(cx - x) + ydsq);
            if (nd < distance) {
                distance = nd;
                index = i;
                trail = -1;
                icx = cx; icy = cy; ia = cm.italicAngle;
            }
        }
        boolean left = x < icx - (y - icy) * ia;
        boolean leading = textLine.isCharLTR(index) == left;
        if (trail == -1) {
            trail = characterCount;
        }
        TextHitInfo result = leading ? TextHitInfo.leading(index) :
            TextHitInfo.trailing(trail-1);
        return result;
    }

    /**
     * 返回与指定点对应的 <code>TextHitInfo</code>。此方法是 <code>hitTestChar</code> 的便利重载，
     * 使用此 <code>TextLayout</code> 的自然边界。
     * @param x 从该 <code>TextLayout</code> 原点的 x 偏移量。这是标准坐标。
     * @param y 从该 <code>TextLayout</code> 原点的 y 偏移量。这是标准坐标。
     * @return 描述指定点下的字符及其边缘（前缘或后缘）的命中。
     */
    public TextHitInfo hitTestChar(float x, float y) {

        return hitTestChar(x, y, getNaturalBounds());
    }

    /**
     * 返回此 <code>TextLayout</code> 的哈希码。
     * @return 此 <code>TextLayout</code> 的哈希码。
     */
    public int hashCode() {
        if (hashCodeCache == 0) {
            ensureCache();
            hashCodeCache = textLine.hashCode();
        }
        return hashCodeCache;
    }

    /**
     * 如果指定的 <code>Object</code> 是 <code>TextLayout</code> 对象，并且该 <code>Object</code>
     * 等于此 <code>TextLayout</code>，则返回 <code>true</code>。
     * @param obj 要测试的 <code>Object</code>
     * @return 如果指定的 <code>Object</code> 等于此 <code>TextLayout</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        return (obj instanceof TextLayout) && equals((TextLayout)obj);
    }

    /**
     * 如果两个布局相等，则返回 <code>true</code>。两个布局相等，如果它们包含相同顺序的相等的 glyphvectors。
     * @param rhs 要与此 <code>TextLayout</code> 比较的 <code>TextLayout</code>
     * @return 如果指定的 <code>TextLayout</code> 等于此 <code>TextLayout</code>，则返回 <code>true</code>。
     *
     */
    public boolean equals(TextLayout rhs) {

        if (rhs == null) {
            return false;
        }
        if (rhs == this) {
            return true;
        }

        ensureCache();
        return textLine.equals(rhs.textLine);
    }

    /**
     * 返回此 <code>TextLayout</code> 的调试信息。
     * @return 此 <code>TextLayout</code> 的 <code>textLine</code> 作为 <code>String</code>。
     */
    public String toString() {
        ensureCache();
        return textLine.toString();
     }

    /**
     * 在指定的 {@link java.awt.Graphics2D Graphics2D} 上下文中，在指定位置渲染此 <code>TextLayout</code>。
     * 布局的原点放置在 x, y 处。渲染可能触及此位置的 <code>getBounds()</code> 内的任何点。这不会改变 <code>g2</code>。
     * 文本沿基线路径渲染。
     * @param g2 要渲染布局的 <code>Graphics2D</code> 上下文
     * @param x 此 <code>TextLayout</code> 的原点的 X 坐标
     * @param y 此 <code>TextLayout</code> 的原点的 Y 坐标
     * @see #getBounds()
     */
    public void draw(Graphics2D g2, float x, float y) {

        if (g2 == null) {
            throw new IllegalArgumentException("Null Graphics2D passed to TextLayout.draw()");
        }

        textLine.draw(g2, x - dx, y - dy);
    }

    /**
     * 仅供测试使用。请勿滥用。
     */
    TextLine getTextLineForTesting() {

        return textLine;
    }

    /**
     *
     * 返回从 start 到 limit 之间具有不同基线的第一个字符的索引，或如果所有字符具有相同基线则返回 limit。
     */
    private static int sameBaselineUpTo(Font font, char[] text,
                                        int start, int limit) {
        // 当前实现不支持多个基线
        return limit;
        /*
        byte bl = font.getBaselineFor(text[start++]);
        while (start < limit && font.getBaselineFor(text[start]) == bl) {
            ++start;
        }
        return start;
        */
    }

    static byte getBaselineFromGraphic(GraphicAttribute graphic) {

        byte alignment = (byte) graphic.getAlignment();

        if (alignment == GraphicAttribute.BOTTOM_ALIGNMENT ||
                alignment == GraphicAttribute.TOP_ALIGNMENT) {

            return (byte)GraphicAttribute.ROMAN_BASELINE;
        }
        else {
            return alignment;
        }
    }

    /**
     * 返回表示此 <code>TextLayout</code> 轮廓的 <code>Shape</code>。
     * @param tx 可选的 {@link AffineTransform}，应用于此 <code>TextLayout</code> 的轮廓。
     * @return 一个 <code>Shape</code>，表示此 <code>TextLayout</code> 的轮廓。这是标准坐标。
     */
    public Shape getOutline(AffineTransform tx) {
        ensureCache();
        Shape result = textLine.getOutline(tx);
        LayoutPathImpl lp = textLine.getLayoutPath();
        if (lp != null) {
            result = lp.mapShape(result);
        }
        return result;
    }

    /**
     * 返回布局路径，如果布局路径是默认路径（x 映射到 advance，y 映射到 offset）则返回 null。
     * @return 布局路径
     * @since 1.6
     */
    public LayoutPath getLayoutPath() {
        return textLine.getLayoutPath();
    }

   /**
     * 将命中转换为标准坐标中的点。该点位于字符的基线上，位于字符的前缘或后缘，具体取决于命中。如果路径在命中表示的字符的侧面断开，
     * 该点将紧邻字符。
     * @param hit 要检查的命中。这必须是 <code>TextLayout</code> 上的有效命中。
     * @param point 返回的点。该点是标准坐标。
     * @throws IllegalArgumentException 如果命中对 <code>TextLayout</code> 无效。
     * @throws NullPointerException 如果命中或点为 null。
     * @since 1.6
     */
    public void hitToPoint(TextHitInfo hit, Point2D point) {
        if (hit == null || point == null) {
            throw new NullPointerException((hit == null ? "hit" : "point") +
                                           " can't be null");
        }
        ensureCache();
        checkTextHit(hit);

        float adv = 0;
        float off = 0;

        int ix = hit.getCharIndex();
        boolean leading = hit.isLeadingEdge();
        boolean ltr;
        if (ix == -1 || ix == textLine.characterCount()) {
            ltr = textLine.isDirectionLTR();
            adv = (ltr == (ix == -1)) ? 0 : lineMetrics.advance;
        } else {
            ltr = textLine.isCharLTR(ix);
            adv = textLine.getCharLinePosition(ix, leading);
            off = textLine.getCharYPosition(ix);
        }
        point.setLocation(adv, off);
        LayoutPath lp = textLine.getLayoutPath();
        if (lp != null) {
            lp.pathToPoint(point, ltr != leading, point);
        }
    }
}
