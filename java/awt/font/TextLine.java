
/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright IBM Corp. 1998-2003, All Rights Reserved
 *
 */

package java.awt.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.Bidi;
import java.text.CharacterIterator;
import java.util.Hashtable;
import java.util.Map;
import sun.font.AttributeValues;
import sun.font.BidiUtils;
import sun.font.CoreMetrics;
import sun.font.Decoration;
import sun.font.FontLineMetrics;
import sun.font.FontResolver;
import sun.font.GraphicComponent;
import sun.font.LayoutPathImpl;
import sun.font.LayoutPathImpl.EmptyPath;
import sun.font.LayoutPathImpl.SegmentPathBuilder;
import sun.font.TextLabelFactory;
import sun.font.TextLineComponent;
import sun.text.CodePointIterator;

import java.awt.geom.Line2D;

final class TextLine {

    static final class TextLineMetrics {
        public final float ascent;
        public final float descent;
        public final float leading;
        public final float advance;

        public TextLineMetrics(float ascent,
                           float descent,
                           float leading,
                           float advance) {
            this.ascent = ascent;
            this.descent = descent;
            this.leading = leading;
            this.advance = advance;
        }
    }

    private TextLineComponent[] fComponents;
    private float[] fBaselineOffsets;
    private int[] fComponentVisualOrder; // 如果为 null，则为从左到右
    private float[] locs; // 按视觉顺序排列的组件的 x, y 对
    private char[] fChars;
    private int fCharsStart;
    private int fCharsLimit;
    private int[] fCharVisualOrder;  // 如果为 null，则为从左到右
    private int[] fCharLogicalOrder; // 如果为 null，则为从左到右
    private byte[] fCharLevels;     // 如果为 null，则为 0
    private boolean fIsDirectionLTR;
    private LayoutPathImpl lp;
    private boolean isSimple;
    private Rectangle pixelBounds;
    private FontRenderContext frc;

    private TextLineMetrics fMetrics = null; // 按需在 getMetrics 中构建

    public TextLine(FontRenderContext frc,
                    TextLineComponent[] components,
                    float[] baselineOffsets,
                    char[] chars,
                    int charsStart,
                    int charsLimit,
                    int[] charLogicalOrder,
                    byte[] charLevels,
                    boolean isDirectionLTR) {

        int[] componentVisualOrder = computeComponentOrder(components,
                                                           charLogicalOrder);

        this.frc = frc;
        fComponents = components;
        fBaselineOffsets = baselineOffsets;
        fComponentVisualOrder = componentVisualOrder;
        fChars = chars;
        fCharsStart = charsStart;
        fCharsLimit = charsLimit;
        fCharLogicalOrder = charLogicalOrder;
        fCharLevels = charLevels;
        fIsDirectionLTR = isDirectionLTR;
        checkCtorArgs();

        init();
    }

    private void checkCtorArgs() {

        int checkCharCount = 0;
        for (int i=0; i < fComponents.length; i++) {
            checkCharCount += fComponents[i].getNumCharacters();
        }

        if (checkCharCount != this.characterCount()) {
            throw new IllegalArgumentException("无效的 TextLine！ " +
                                "字符计数与组件字符计数之和不同。");
        }
    }

    private void init() {

        // 首先，我们需要检查是否有位于顶部或底部基线的图形组件。因此
        // 我们在这里执行以前在 getMetrics 中的工作。

        float ascent = 0;
        float descent = 0;
        float leading = 0;
        float advance = 0;

        // ascent + descent 不能小于这个值
        float maxGraphicHeight = 0;
        float maxGraphicHeightWithLeading = 0;

        // 遍历 EGA
        TextLineComponent tlc;
        boolean fitTopAndBottomGraphics = false;

        isSimple = true;

        for (int i = 0; i < fComponents.length; i++) {
            tlc = fComponents[i];

            isSimple &= tlc.isSimple();

            CoreMetrics cm = tlc.getCoreMetrics();

            byte baseline = (byte)cm.baselineIndex;

            if (baseline >= 0) {
                float baselineOffset = fBaselineOffsets[baseline];

                ascent = Math.max(ascent, -baselineOffset + cm.ascent);

                float gd = baselineOffset + cm.descent;
                descent = Math.max(descent, gd);

                leading = Math.max(leading, gd + cm.leading);
            }
            else {
                fitTopAndBottomGraphics = true;
                float graphicHeight = cm.ascent + cm.descent;
                float graphicHeightWithLeading = graphicHeight + cm.leading;
                maxGraphicHeight = Math.max(maxGraphicHeight, graphicHeight);
                maxGraphicHeightWithLeading = Math.max(maxGraphicHeightWithLeading,
                                                       graphicHeightWithLeading);
            }
        }

        if (fitTopAndBottomGraphics) {
            if (maxGraphicHeight > ascent + descent) {
                descent = maxGraphicHeight - ascent;
            }
            if (maxGraphicHeightWithLeading > ascent + leading) {
                leading = maxGraphicHeightWithLeading - ascent;
            }
        }

        leading -= descent;

        // 现在我们已经知道足够的信息来计算 locs，但在创建 metrics 对象之前
        // 我们需要最终的 loc 来计算 advance

        if (fitTopAndBottomGraphics) {
            // 我们有顶部或底部基线，因此扩展基线数组
            // 需要完整的偏移量，由 CoreMetrics.effectiveBaselineOffset 提供
            fBaselineOffsets = new float[] {
                fBaselineOffsets[0],
                fBaselineOffsets[1],
                fBaselineOffsets[2],
                descent,
                -ascent
            };
        }

        float x = 0;
        float y = 0;
        CoreMetrics pcm = null;

        boolean needPath = false;
        locs = new float[fComponents.length * 2 + 2];

        for (int i = 0, n = 0; i < fComponents.length; ++i, n += 2) {
            tlc = fComponents[getComponentLogicalIndex(i)];
            CoreMetrics cm = tlc.getCoreMetrics();

            if ((pcm != null) &&
                (pcm.italicAngle != 0 || cm.italicAngle != 0) &&  // 调整斜体
                (pcm.italicAngle != cm.italicAngle ||
                 pcm.baselineIndex != cm.baselineIndex ||
                 pcm.ssOffset != cm.ssOffset)) {

                // 1) 计算重叠区域 - 最小有效上升和最小有效下降
                // 2) 计算上升和下降在斜体角度上的 x 位置
                // 3) 计算最大左 - 右，调整右位置
                // 这是一种粗略的文本组件之间的字距调整形式

                // 注意 GlyphVectors 根据偏移量预定位字形，
                // 因此 tl 不需要调整 GlyphVector 位置
                // 1)
                float pb = pcm.effectiveBaselineOffset(fBaselineOffsets);
                float pa = pb - pcm.ascent;
                float pd = pb + pcm.descent;
                // pb += pcm.ssOffset;

                float cb = cm.effectiveBaselineOffset(fBaselineOffsets);
                float ca = cb - cm.ascent;
                float cd = cb + cm.descent;
                // cb += cm.ssOffset;

                float a = Math.max(pa, ca);
                float d = Math.min(pd, cd);

                // 2)
                float pax = pcm.italicAngle * (pb - a);
                float pdx = pcm.italicAngle * (pb - d);

                float cax = cm.italicAngle * (cb - a);
                float cdx = cm.italicAngle * (cb - d);

                // 3)
                float dax = pax - cax;
                float ddx = pdx - cdx;
                float dx = Math.max(dax, ddx);

                x += dx;
                y = cb;
            } else {
                // x 不需要斜体调整，但仍然需要计算 y
                y = cm.effectiveBaselineOffset(fBaselineOffsets); // + cm.ssOffset;
            }

            locs[n] = x;
            locs[n+1] = y;

            x += tlc.getAdvance();
            pcm = cm;

            needPath |= tlc.getBaselineTransform() != null;
        }

        // 行右侧是否需要斜体填充？
        if (pcm.italicAngle != 0) {
            float pb = pcm.effectiveBaselineOffset(fBaselineOffsets);
            float pa = pb - pcm.ascent;
            float pd = pb + pcm.descent;
            pb += pcm.ssOffset;

            float d;
            if (pcm.italicAngle > 0) {
                d = pb + pcm.ascent;
            } else {
                d = pb - pcm.descent;
            }
            d *= pcm.italicAngle;

            x += d;
        }
        locs[locs.length - 2] = x;
        // locs[locs.length - 1] = 0; // 最终偏移量始终回到基线

        // 好的，现在我们有了最终的 advance，构建 fMetrics
        advance = x;
        fMetrics = new TextLineMetrics(ascent, descent, leading, advance);

        // 如果需要，构建路径
        if (needPath) {
            isSimple = false;

            Point2D.Double pt = new Point2D.Double();
            double tx = 0, ty = 0;
            SegmentPathBuilder builder = new SegmentPathBuilder();
            builder.moveTo(locs[0], 0);
            for (int i = 0, n = 0; i < fComponents.length; ++i, n += 2) {
                tlc = fComponents[getComponentLogicalIndex(i)];
                AffineTransform at = tlc.getBaselineTransform();
                if (at != null &&
                    ((at.getType() & AffineTransform.TYPE_TRANSLATION) != 0)) {
                    double dx = at.getTranslateX();
                    double dy = at.getTranslateY();
                    builder.moveTo(tx += dx, ty += dy);
                }
                pt.x = locs[n+2] - locs[n];
                pt.y = 0;
                if (at != null) {
                    at.deltaTransform(pt, pt);
                }
                builder.lineTo(tx += pt.x, ty += pt.y);
            }
            lp = builder.complete();

            if (lp == null) { // 空路径
                tlc = fComponents[getComponentLogicalIndex(0)];
                AffineTransform at = tlc.getBaselineTransform();
                if (at != null) {
                    lp = new EmptyPath(at);
                }
            }
        }
    }

    public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
        Rectangle result = null;

        // 如果我们有一个匹配的 frc，将其设置为 null 以避免每次组件测试
        if (frc != null && frc.equals(this.frc)) {
            frc = null;
        }

        // 仅缓存默认 frc 的整数位置，这有点严格
        int ix = (int)Math.floor(x);
        int iy = (int)Math.floor(y);
        float rx = x - ix;
        float ry = y - iy;
        boolean canCache = frc == null && rx == 0 && ry == 0;

        if (canCache && pixelBounds != null) {
            result = new Rectangle(pixelBounds);
            result.x += ix;
            result.y += iy;
            return result;
        }

        // 无法使用缓存或没有缓存，因此计算

        if (isSimple) { // 所有 GlyphVectors 没有装饰，没有布局路径
            for (int i = 0, n = 0; i < fComponents.length; i++, n += 2) {
                TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];
                Rectangle pb = tlc.getPixelBounds(frc, locs[n] + rx, locs[n+1] + ry);
                if (!pb.isEmpty()) {
                    if (result == null) {
                        result = pb;
                    } else {
                        result.add(pb);
                    }
                }
            }
            if (result == null) {
                result = new Rectangle(0, 0, 0, 0);
            }
        } else { // 绘制并测试
            final int MARGIN = 3;
            Rectangle2D r2d = getVisualBounds();
            if (lp != null) {
                r2d = lp.mapShape(r2d).getBounds();
            }
            Rectangle bounds = r2d.getBounds();
            BufferedImage im = new BufferedImage(bounds.width + MARGIN * 2,
                                                 bounds.height + MARGIN * 2,
                                                 BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = im.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, im.getWidth(), im.getHeight());

            g2d.setColor(Color.BLACK);
            draw(g2d, rx + MARGIN - bounds.x, ry + MARGIN - bounds.y);

            result = computePixelBounds(im);
            result.x -= MARGIN - bounds.x;
            result.y -= MARGIN - bounds.y;
        }

        if (canCache) {
            pixelBounds = new Rectangle(result);
        }

        result.x += ix;
        result.y += iy;
        return result;
    }

    static Rectangle computePixelBounds(BufferedImage im) {
        int w = im.getWidth();
        int h = im.getHeight();

        int l = -1, t = -1, r = w, b = h;

        {
            // 获取顶部
            int[] buf = new int[w];
            loop: while (++t < h) {
                im.getRGB(0, t, buf.length, 1, buf, 0, w); // w 被忽略
                for (int i = 0; i < buf.length; i++) {
                    if (buf[i] != -1) {
                        break loop;
                    }
                }
            }
        }

        // 获取底部
        {
            int[] buf = new int[w];
            loop: while (--b > t) {
                im.getRGB(0, b, buf.length, 1, buf, 0, w); // w 被忽略
                for (int i = 0; i < buf.length; ++i) {
                    if (buf[i] != -1) {
                        break loop;
                    }
                }
            }
            ++b;
        }


                    // 获取左边界
        {
            loop: while (++l < r) {
                for (int i = t; i < b; ++i) {
                    int v = im.getRGB(l, i);
                    if (v != -1) {
                        break loop;
                    }
                }
            }
        }

        // 获取右边界
        {
            loop: while (--r > l) {
                for (int i = t; i < b; ++i) {
                    int v = im.getRGB(r, i);
                    if (v != -1) {
                        break loop;
                    }
                }
            }
            ++r;
        }

        return new Rectangle(l, t, r-l, b-t);
    }

    private abstract static class Function {

        abstract float computeFunction(TextLine line,
                                       int componentIndex,
                                       int indexInArray);
    }

    private static Function fgPosAdvF = new Function() {
        float computeFunction(TextLine line,
                              int componentIndex,
                              int indexInArray) {

            TextLineComponent tlc = line.fComponents[componentIndex];
                int vi = line.getComponentVisualIndex(componentIndex);
            return line.locs[vi * 2] + tlc.getCharX(indexInArray) + tlc.getCharAdvance(indexInArray);
        }
    };

    private static Function fgAdvanceF = new Function() {

        float computeFunction(TextLine line,
                              int componentIndex,
                              int indexInArray) {

            TextLineComponent tlc = line.fComponents[componentIndex];
            return tlc.getCharAdvance(indexInArray);
        }
    };

    private static Function fgXPositionF = new Function() {

        float computeFunction(TextLine line,
                              int componentIndex,
                              int indexInArray) {

                int vi = line.getComponentVisualIndex(componentIndex);
            TextLineComponent tlc = line.fComponents[componentIndex];
            return line.locs[vi * 2] + tlc.getCharX(indexInArray);
        }
    };

    private static Function fgYPositionF = new Function() {

        float computeFunction(TextLine line,
                              int componentIndex,
                              int indexInArray) {

            TextLineComponent tlc = line.fComponents[componentIndex];
            float charPos = tlc.getCharY(indexInArray);

            // charPos 是相对于组件的 - 调整为基线

            return charPos + line.getComponentShift(componentIndex);
        }
    };

    public int characterCount() {

        return fCharsLimit - fCharsStart;
    }

    public boolean isDirectionLTR() {

        return fIsDirectionLTR;
    }

    public TextLineMetrics getMetrics() {
        return fMetrics;
    }

    public int visualToLogical(int visualIndex) {

        if (fCharLogicalOrder == null) {
            return visualIndex;
        }

        if (fCharVisualOrder == null) {
            fCharVisualOrder = BidiUtils.createInverseMap(fCharLogicalOrder);
        }

        return fCharVisualOrder[visualIndex];
    }

    public int logicalToVisual(int logicalIndex) {

        return (fCharLogicalOrder == null)?
            logicalIndex : fCharLogicalOrder[logicalIndex];
    }

    public byte getCharLevel(int logicalIndex) {

        return fCharLevels==null? 0 : fCharLevels[logicalIndex];
    }

    public boolean isCharLTR(int logicalIndex) {

        return (getCharLevel(logicalIndex) & 0x1) == 0;
    }

    public int getCharType(int logicalIndex) {

        return Character.getType(fChars[logicalIndex + fCharsStart]);
    }

    public boolean isCharSpace(int logicalIndex) {

        return Character.isSpaceChar(fChars[logicalIndex + fCharsStart]);
    }

    public boolean isCharWhitespace(int logicalIndex) {

        return Character.isWhitespace(fChars[logicalIndex + fCharsStart]);
    }

    public float getCharAngle(int logicalIndex) {

        return getCoreMetricsAt(logicalIndex).italicAngle;
    }

    public CoreMetrics getCoreMetricsAt(int logicalIndex) {

        if (logicalIndex < 0) {
            throw new IllegalArgumentException("负的 logicalIndex。");
        }

        if (logicalIndex > fCharsLimit - fCharsStart) {
            throw new IllegalArgumentException("logicalIndex 太大。");
        }

        int currentTlc = 0;
        int tlcStart = 0;
        int tlcLimit = 0;

        do {
            tlcLimit += fComponents[currentTlc].getNumCharacters();
            if (tlcLimit > logicalIndex) {
                break;
            }
            ++currentTlc;
            tlcStart = tlcLimit;
        } while(currentTlc < fComponents.length);

        return fComponents[currentTlc].getCoreMetrics();
    }

    public float getCharAscent(int logicalIndex) {

        return getCoreMetricsAt(logicalIndex).ascent;
    }

    public float getCharDescent(int logicalIndex) {

        return getCoreMetricsAt(logicalIndex).descent;
    }

    public float getCharShift(int logicalIndex) {

        return getCoreMetricsAt(logicalIndex).ssOffset;
    }

    private float applyFunctionAtIndex(int logicalIndex, Function f) {

        if (logicalIndex < 0) {
            throw new IllegalArgumentException("负的 logicalIndex。");
        }

        int tlcStart = 0;

        for(int i=0; i < fComponents.length; i++) {

            int tlcLimit = tlcStart + fComponents[i].getNumCharacters();
            if (tlcLimit > logicalIndex) {
                return f.computeFunction(this, i, logicalIndex - tlcStart);
            }
            else {
                tlcStart = tlcLimit;
            }
        }

        throw new IllegalArgumentException("logicalIndex 太大。");
    }

    public float getCharAdvance(int logicalIndex) {

        return applyFunctionAtIndex(logicalIndex, fgAdvanceF);
    }

    public float getCharXPosition(int logicalIndex) {

        return applyFunctionAtIndex(logicalIndex, fgXPositionF);
    }

    public float getCharYPosition(int logicalIndex) {

        return applyFunctionAtIndex(logicalIndex, fgYPositionF);
    }

    public float getCharLinePosition(int logicalIndex) {

        return getCharXPosition(logicalIndex);
    }

    public float getCharLinePosition(int logicalIndex, boolean leading) {
        Function f = isCharLTR(logicalIndex) == leading ? fgXPositionF : fgPosAdvF;
        return applyFunctionAtIndex(logicalIndex, f);
    }

    public boolean caretAtOffsetIsValid(int offset) {

        if (offset < 0) {
            throw new IllegalArgumentException("负的 offset。");
        }

        int tlcStart = 0;

        for(int i=0; i < fComponents.length; i++) {

            int tlcLimit = tlcStart + fComponents[i].getNumCharacters();
            if (tlcLimit > offset) {
                return fComponents[i].caretAtOffsetIsValid(offset-tlcStart);
            }
            else {
                tlcStart = tlcLimit;
            }
        }

        throw new IllegalArgumentException("logicalIndex 太大。");
    }

    /**
     * 将组件的视觉索引映射到逻辑索引。
     */
    private int getComponentLogicalIndex(int vi) {
        if (fComponentVisualOrder == null) {
            return vi;
        }
        return fComponentVisualOrder[vi];
    }

    /**
     * 将组件的逻辑索引映射到视觉索引。
     */
    private int getComponentVisualIndex(int li) {
        if (fComponentVisualOrder == null) {
                return li;
        }
        for (int i = 0; i < fComponentVisualOrder.length; ++i) {
                if (fComponentVisualOrder[i] == li) {
                    return i;
                }
        }
        throw new IndexOutOfBoundsException("无效的组件索引: " + li);
    }

    public Rectangle2D getCharBounds(int logicalIndex) {

        if (logicalIndex < 0) {
            throw new IllegalArgumentException("负的 logicalIndex。");
        }

        int tlcStart = 0;

        for (int i=0; i < fComponents.length; i++) {

            int tlcLimit = tlcStart + fComponents[i].getNumCharacters();
            if (tlcLimit > logicalIndex) {

                TextLineComponent tlc = fComponents[i];
                int indexInTlc = logicalIndex - tlcStart;
                Rectangle2D chBounds = tlc.getCharVisualBounds(indexInTlc);

                        int vi = getComponentVisualIndex(i);
                chBounds.setRect(chBounds.getX() + locs[vi * 2],
                                 chBounds.getY() + locs[vi * 2 + 1],
                                 chBounds.getWidth(),
                                 chBounds.getHeight());
                return chBounds;
            }
            else {
                tlcStart = tlcLimit;
            }
        }

        throw new IllegalArgumentException("logicalIndex 太大。");
    }

    private float getComponentShift(int index) {
        CoreMetrics cm = fComponents[index].getCoreMetrics();
        return cm.effectiveBaselineOffset(fBaselineOffsets);
    }

    public void draw(Graphics2D g2, float x, float y) {
        if (lp == null) {
            for (int i = 0, n = 0; i < fComponents.length; i++, n += 2) {
                TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];
                tlc.draw(g2, locs[n] + x, locs[n+1] + y);
            }
        } else {
            AffineTransform oldTx = g2.getTransform();
            Point2D.Float pt = new Point2D.Float();
            for (int i = 0, n = 0; i < fComponents.length; i++, n += 2) {
                TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];
                lp.pathToPoint(locs[n], locs[n+1], false, pt);
                pt.x += x;
                pt.y += y;
                AffineTransform at = tlc.getBaselineTransform();

                if (at != null) {
                    g2.translate(pt.x - at.getTranslateX(), pt.y - at.getTranslateY());
                    g2.transform(at);
                    tlc.draw(g2, 0, 0);
                    g2.setTransform(oldTx);
                } else {
                    tlc.draw(g2, pt.x, pt.y);
                }
            }
        }
    }

    /**
     * 返回所有组件的视觉边界并集。
     * 这包括路径。 不包括逻辑边界（用于光标）。
     */
    public Rectangle2D getVisualBounds() {
        Rectangle2D result = null;

        for (int i = 0, n = 0; i < fComponents.length; i++, n += 2) {
            TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];
            Rectangle2D r = tlc.getVisualBounds();

            Point2D.Float pt = new Point2D.Float(locs[n], locs[n+1]);
            if (lp == null) {
                r.setRect(r.getMinX() + pt.x, r.getMinY() + pt.y,
                          r.getWidth(), r.getHeight());
            } else {
                lp.pathToPoint(pt, false, pt);

                AffineTransform at = tlc.getBaselineTransform();
                if (at != null) {
                    AffineTransform tx = AffineTransform.getTranslateInstance
                        (pt.x - at.getTranslateX(), pt.y - at.getTranslateY());
                    tx.concatenate(at);
                    r = tx.createTransformedShape(r).getBounds2D();
                } else {
                    r.setRect(r.getMinX() + pt.x, r.getMinY() + pt.y,
                              r.getWidth(), r.getHeight());
                }
            }

            if (result == null) {
                result = r;
            } else {
                result.add(r);
            }
        }

        if (result == null) {
            result = new Rectangle2D.Float(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        }

        return result;
    }

    public Rectangle2D getItalicBounds() {

        float left = Float.MAX_VALUE, right = -Float.MAX_VALUE;
        float top = Float.MAX_VALUE, bottom = -Float.MAX_VALUE;

        for (int i=0, n = 0; i < fComponents.length; i++, n += 2) {
            TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];

            Rectangle2D tlcBounds = tlc.getItalicBounds();
            float x = locs[n];
            float y = locs[n+1];

            left = Math.min(left, x + (float)tlcBounds.getX());
            right = Math.max(right, x + (float)tlcBounds.getMaxX());

            top = Math.min(top, y + (float)tlcBounds.getY());
            bottom = Math.max(bottom, y + (float)tlcBounds.getMaxY());
        }

        return new Rectangle2D.Float(left, top, right-left, bottom-top);
    }

    public Shape getOutline(AffineTransform tx) {

        GeneralPath dstShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);

        for (int i=0, n = 0; i < fComponents.length; i++, n += 2) {
            TextLineComponent tlc = fComponents[getComponentLogicalIndex(i)];

            dstShape.append(tlc.getOutline(locs[n], locs[n+1]), false);
        }

        if (tx != null) {
            dstShape.transform(tx);
        }
        return dstShape;
    }

    public int hashCode() {
        return (fComponents.length << 16) ^
                    (fComponents[0].hashCode() << 3) ^ (fCharsLimit-fCharsStart);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < fComponents.length; i++) {
            buf.append(fComponents[i]);
        }

        return buf.toString();
    }

    /**
     * 从文本创建一个 TextLine。 字体必须能够显示所有文本。
     * attributes==null 等同于使用一个空的 Map 作为属性。
     */
    public static TextLine fastCreateTextLine(FontRenderContext frc,
                                              char[] chars,
                                              Font font,
                                              CoreMetrics lm,
                                              Map<? extends Attribute, ?> attributes) {

        boolean isDirectionLTR = true;
        byte[] levels = null;
        int[] charsLtoV = null;
        Bidi bidi = null;
        int characterCount = chars.length;

        boolean requiresBidi = false;
        byte[] embs = null;

        AttributeValues values = null;
        if (attributes != null) {
            values = AttributeValues.fromMap(attributes);
            if (values.getRunDirection() >= 0) {
                isDirectionLTR = values.getRunDirection() == 0;
                requiresBidi = !isDirectionLTR;
            }
            if (values.getBidiEmbedding() != 0) {
                requiresBidi = true;
                byte level = (byte)values.getBidiEmbedding();
                embs = new byte[characterCount];
                for (int i = 0; i < embs.length; ++i) {
                    embs[i] = level;
                }
            }
        }

        // dlf: 从字体获取 baseRot

        if (!requiresBidi) {
            requiresBidi = Bidi.requiresBidi(chars, 0, chars.length);
        }

        if (requiresBidi) {
          int bidiflags = values == null
              ? Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT
              : values.getRunDirection();

          bidi = new Bidi(chars, 0, embs, 0, chars.length, bidiflags);
          if (!bidi.isLeftToRight()) {
              levels = BidiUtils.getLevels(bidi);
              int[] charsVtoL = BidiUtils.createVisualToLogicalMap(levels);
              charsLtoV = BidiUtils.createInverseMap(charsVtoL);
              isDirectionLTR = bidi.baseIsLeftToRight();
          }
        }


    /**
     * 刷新流。
     */
    public void flush() { }

    /**
     * 关闭流。
     */
    public void close() { }

}

/**
 * 返回一个数组，该数组按逻辑顺序包含给定范围内的 TextLineComponents，具有给定的属性。
 */
public static TextLineComponent[] createComponentsOnRun(int runStart,
                                                        int runLimit,
                                                        char[] chars,
                                                        int[] charsLtoV,
                                                        byte[] levels,
                                                        TextLabelFactory factory,
                                                        Font font,
                                                        CoreMetrics cm,
                                                        FontRenderContext frc,
                                                        Decoration decorator,
                                                        TextLineComponent[] components,
                                                        int numComponents) {

    int pos = runStart;
    do {
        int chunkLimit = firstVisualChunk(charsLtoV, levels, pos, runLimit); // <= displayLimit

        do {
            int startPos = pos;
            int lmCount;

            if (cm == null) {
                LineMetrics lineMetrics = font.getLineMetrics(chars, startPos, chunkLimit, frc);
                cm = CoreMetrics.get(lineMetrics);
                lmCount = lineMetrics.getNumChars();
            }
            else {
                lmCount = (chunkLimit - startPos);
            }

            TextLineComponent nextComponent =
                factory.createExtended(font, cm, decorator, startPos, startPos + lmCount);

            ++numComponents;
            if (numComponents >= components.length) {
                components = expandArray(components);
            }

            components[numComponents - 1] = nextComponent;

            pos += lmCount;
        } while (pos < chunkLimit);

    } while (pos < runLimit);

    return components;
}

/**
 * 返回一个数组（按逻辑顺序），该数组表示文本的 TextLineComponents。这些组件在逻辑上和视觉上都是连续的。
 */
public static TextLineComponent[] getComponents(StyledParagraph styledParagraph,
                                                char[] chars,
                                                int textStart,
                                                int textLimit,
                                                int[] charsLtoV,
                                                byte[] levels,
                                                TextLabelFactory factory) {

    FontRenderContext frc = factory.getFontRenderContext();

    int numComponents = 0;
    TextLineComponent[] tempComponents = new TextLineComponent[1];

    int pos = textStart;
    do {
        int runLimit = Math.min(styledParagraph.getRunLimit(pos), textLimit);

        Decoration decorator = styledParagraph.getDecorationAt(pos);

        Object graphicOrFont = styledParagraph.getFontOrGraphicAt(pos);

        if (graphicOrFont instanceof GraphicAttribute) {
            // AffineTransform baseRot = styledParagraph.getBaselineRotationAt(pos);
            // !!! 暂时，让我们将同时具有字体和图形属性的文本段分配为 null 旋转（例如，当应用图形时，基线旋转消失）。
            AffineTransform baseRot = null;
            GraphicAttribute graphicAttribute = (GraphicAttribute) graphicOrFont;
            do {
                int chunkLimit = firstVisualChunk(charsLtoV, levels,
                                pos, runLimit);

                GraphicComponent nextGraphic =
                    new GraphicComponent(graphicAttribute, decorator, charsLtoV, levels, pos, chunkLimit, baseRot);
                pos = chunkLimit;

                ++numComponents;
                if (numComponents >= tempComponents.length) {
                    tempComponents = expandArray(tempComponents);
                }

                tempComponents[numComponents - 1] = nextGraphic;

            } while (pos < runLimit);
        }
        else {
            Font font = (Font) graphicOrFont;

            tempComponents = createComponentsOnRun(pos, runLimit,
                                                    chars,
                                                    charsLtoV, levels,
                                                    factory, font, null,
                                                    frc,
                                                    decorator,
                                                    tempComponents,
                                                    numComponents);
            pos = runLimit;
            numComponents = tempComponents.length;
            while (tempComponents[numComponents - 1] == null) {
                numComponents -= 1;
            }
        }

    } while (pos < textLimit);

    TextLineComponent[] components;
    if (tempComponents.length == numComponents) {
        components = tempComponents;
    }
    else {
        components = new TextLineComponent[numComponents];
        System.arraycopy(tempComponents, 0, components, 0, numComponents);
    }

    return components;
}

/**
 * 从字体和字符数据的范围内创建一个 TextLine。该范围相对于 StyledParagraph 和字符数组。
 */
public static TextLine createLineFromText(char[] chars,
                                          StyledParagraph styledParagraph,
                                          TextLabelFactory factory,
                                          boolean isDirectionLTR,
                                          float[] baselineOffsets) {

    factory.setLineContext(0, chars.length);

    Bidi lineBidi = factory.getLineBidi();
    int[] charsLtoV = null;
    byte[] levels = null;

    if (lineBidi != null) {
        levels = BidiUtils.getLevels(lineBidi);
        int[] charsVtoL = BidiUtils.createVisualToLogicalMap(levels);
        charsLtoV = BidiUtils.createInverseMap(charsVtoL);
    }

    TextLineComponent[] components =
        getComponents(styledParagraph, chars, 0, chars.length, charsLtoV, levels, factory);

    return new TextLine(factory.getFontRenderContext(), components, baselineOffsets,
                        chars, 0, chars.length, charsLtoV, levels, isDirectionLTR);
}

/**
 * 从给定的组件数组和逻辑到视觉字符映射中计算组件的顺序。如果为规范形式，则可能返回 null。
 */
private static int[] computeComponentOrder(TextLineComponent[] components,
                                           int[] charsLtoV) {

    /*
     * 为字形集创建一个视觉顺序。这里重要的是值相对于彼此的排名，而不是确切的值。例如，第一个视觉上出现的字形集应该有最低的值。最后一个应该有最高的值。然后将这些值规范化，以与字形的位置一一对应。
     *
     */
    int[] componentOrder = null;
    if (charsLtoV != null && components.length > 1) {
        componentOrder = new int[components.length];
        int gStart = 0;
        for (int i = 0; i < components.length; i++) {
            componentOrder[i] = charsLtoV[gStart];
            gStart += components[i].getNumCharacters();
        }

        componentOrder = BidiUtils.createContiguousOrder(componentOrder);
        componentOrder = BidiUtils.createInverseMap(componentOrder);
    }
    return componentOrder;
}

/**
 * 从文本创建一个 TextLine。chars 只是迭代器中的文本。
 */
public static TextLine standardCreateTextLine(FontRenderContext frc,
                                              AttributedCharacterIterator text,
                                              char[] chars,
                                              float[] baselineOffsets) {

    StyledParagraph styledParagraph = new StyledParagraph(text, chars);
    Bidi bidi = new Bidi(text);
    if (bidi.isLeftToRight()) {
        bidi = null;
    }
    int layoutFlags = 0; // 暂无额外信息，bidi 确定运行和行的方向
    TextLabelFactory factory = new TextLabelFactory(frc, chars, bidi, layoutFlags);

    boolean isDirectionLTR = true;
    if (bidi != null) {
        isDirectionLTR = bidi.baseIsLeftToRight();
    }
    return createLineFromText(chars, styledParagraph, factory, isDirectionLTR, baselineOffsets);
}

/*
 * 一个实用工具，用于获取逻辑上和视觉上都连续的文本范围。
 * 如果整个范围都符合条件，返回 limit，否则返回 start 之后的第一个方向变化。目前这样做已经足够，但可以进一步优化。
 */
private static int firstVisualChunk(int order[], byte direction[],
                                    int start, int limit)
{
    if (order != null) {
        int min = order[start];
        int max = order[start];
        int count = limit - start;
        for (int i = start + 1; i < limit; i++) {
            min = Math.min(min, order[i]);
            max = Math.max(max, order[i]);
            if (max - min >= count) {
                if (direction != null) {
                    byte baseLevel = direction[start];
                    for (int j = start + 1; j < i; j++) {
                        if (direction[j] != baseLevel) {
                            return j;
                        }
                    }
                }
                return i;
            }
        }
    }
    return limit;
}

/**
 * 当此方法返回时，ACI 的当前位置将位于第一个不包含 GraphicAttribute 的运行的起始位置。如果不存在这样的运行，ACI 的位置将在末尾，此方法将返回 false。
 */
static boolean advanceToFirstFont(AttributedCharacterIterator aci) {

    for (char ch = aci.first();
         ch != CharacterIterator.DONE;
         ch = aci.setIndex(aci.getRunLimit()))
    {

        if (aci.getAttribute(TextAttribute.CHAR_REPLACEMENT) == null) {
            return true;
        }
    }

    return false;
}

static float[] getNormalizedOffsets(float[] baselineOffsets, byte baseline) {

    if (baselineOffsets[baseline] != 0) {
        float base = baselineOffsets[baseline];
        float[] temp = new float[baselineOffsets.length];
        for (int i = 0; i < temp.length; i++)
            temp[i] = baselineOffsets[i] - base;
        baselineOffsets = temp;
    }
    return baselineOffsets;
}

static Font getFontAtCurrentPos(AttributedCharacterIterator aci) {

    Object value = aci.getAttribute(TextAttribute.FONT);
    if (value != null) {
        return (Font) value;
    }
    if (aci.getAttribute(TextAttribute.FAMILY) != null) {
        return Font.getFont(aci.getAttributes());
    }

    int ch = CodePointIterator.create(aci).next();
    if (ch != CodePointIterator.DONE) {
        FontResolver resolver = FontResolver.getInstance();
        return resolver.getFont(resolver.getFontIndex(ch), aci.getAttributes());
    }
    return null;
}

/*
 * 新版本要求块处于同一级别。
 */
private static int firstVisualChunk(int order[], byte direction[],
                                    int start, int limit)
{
    if (order != null && direction != null) {
        byte dir = direction[start];
        while (++start < limit && direction[start] == dir) {}
        return start;
    }
    return limit;
}

/*
 * 使用提供的宽度和比率对 charStart 和 charLimit 之间的字符创建一个新的行。
 */
public TextLine getJustifiedLine(float justificationWidth, float justifyRatio, int justStart, int justLimit) {

    TextLineComponent[] newComponents = new TextLineComponent[fComponents.length];
    System.arraycopy(fComponents, 0, newComponents, 0, fComponents.length);

    float leftHang = 0;
    float adv = 0;
    float justifyDelta = 0;
    boolean rejustify = false;
    do {
        adv = getAdvanceBetween(newComponents, 0, characterCount());

        // 所有在对齐范围之外的字符必须在布局的基本方向中，否则对齐没有意义。

        float justifyAdvance = getAdvanceBetween(newComponents, justStart, justLimit);

        // 获取实际的对齐增量
        justifyDelta = (justificationWidth - justifyAdvance) * justifyRatio;

        // 生成一个 GlyphJustificationInfo 记录数组，传递给对齐器。数组按视觉顺序排列。

        // 获取每个组件将使用的位置
        int[] infoPositions = new int[newComponents.length];
        int infoCount = 0;
        for (int visIndex = 0; visIndex < newComponents.length; visIndex++) {
            int logIndex = getComponentLogicalIndex(visIndex);
            infoPositions[logIndex] = infoCount;
            infoCount += newComponents[logIndex].getNumJustificationInfos();
        }
        GlyphJustificationInfo[] infos = new GlyphJustificationInfo[infoCount];


                        // 获取对齐信息
            int compStart = 0;
            for (int i = 0; i < newComponents.length; i++) {
                TextLineComponent comp = newComponents[i];
                int compLength = comp.getNumCharacters();
                int compLimit = compStart + compLength;
                if (compLimit > justStart) {
                    int rangeMin = Math.max(0, justStart - compStart);
                    int rangeMax = Math.min(compLength, justLimit - compStart);
                    comp.getJustificationInfos(infos, infoPositions[i], rangeMin, rangeMax);

                    if (compLimit >= justLimit) {
                        break;
                    }
                }
            }

            // 记录是按视觉顺序排列且连续的，因此开始和结束位置
            // 只需确定我们没有获取记录的地方
            int infoStart = 0;
            int infoLimit = infoCount;
            while (infoStart < infoLimit && infos[infoStart] == null) {
                ++infoStart;
            }

            while (infoLimit > infoStart && infos[infoLimit - 1] == null) {
                --infoLimit;
            }

            // 调用对齐器处理记录
            TextJustifier justifier = new TextJustifier(infos, infoStart, infoLimit);

            float[] deltas = justifier.justify(justifyDelta);

            boolean canRejustify = rejustify == false;
            boolean wantRejustify = false;
            boolean[] flags = new boolean[1];

            // 应用对齐增量
            compStart = 0;
            for (int i = 0; i < newComponents.length; i++) {
                TextLineComponent comp = newComponents[i];
                int compLength = comp.getNumCharacters();
                int compLimit = compStart + compLength;
                if (compLimit > justStart) {
                    int rangeMin = Math.max(0, justStart - compStart);
                    int rangeMax = Math.min(compLength, justLimit - compStart);
                    newComponents[i] = comp.applyJustificationDeltas(deltas, infoPositions[i] * 2, flags);

                    wantRejustify |= flags[0];

                    if (compLimit >= justLimit) {
                        break;
                    }
                }
            }

            rejustify = wantRejustify && !rejustify; // 只进行两次遍历
        } while (rejustify);

        return new TextLine(frc, newComponents, fBaselineOffsets, fChars, fCharsStart,
                            fCharsLimit, fCharLogicalOrder, fCharLevels,
                            fIsDirectionLTR);
    }

    // 返回逻辑起始和结束之间的文本的总进位
    public static float getAdvanceBetween(TextLineComponent[] components, int start, int limit) {
        float advance = 0;

        int tlcStart = 0;
        for(int i = 0; i < components.length; i++) {
            TextLineComponent comp = components[i];

            int tlcLength = comp.getNumCharacters();
            int tlcLimit = tlcStart + tlcLength;
            if (tlcLimit > start) {
                int measureStart = Math.max(0, start - tlcStart);
                int measureLimit = Math.min(tlcLength, limit - tlcStart);
                advance += comp.getAdvanceBetween(measureStart, measureLimit);
                if (tlcLimit >= limit) {
                    break;
                }
            }

            tlcStart = tlcLimit;
        }

        return advance;
    }

    LayoutPathImpl getLayoutPath() {
        return lp;
    }
}
