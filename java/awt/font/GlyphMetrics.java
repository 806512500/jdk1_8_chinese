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

import java.awt.geom.Rectangle2D;

/**
 * <code>GlyphMetrics</code> 类表示单个字形的信息。字形是字符或多个字符的视觉表示。许多不同的字形可以用来表示单个字符或字符组合。<code>GlyphMetrics</code> 实例由 {@link java.awt.Font Font} 生成，并适用于特定字体中的特定字形。
 * <p>
 * 字形可以是 STANDARD、LIGATURE、COMBINING 或 COMPONENT。
 * <ul>
 * <li>STANDARD 字形通常用于表示单个字符。
 * <li>LIGATURE 字形用于表示字符序列。
 * <li>COMPONENT 字形在 {@link GlyphVector} 中不对应文本模型中的特定字符。相反，COMPONENT 字形是出于排版原因添加的，例如阿拉伯文对齐。
 * <li>COMBINING 字形装饰 STANDARD 或 LIGATURE 字形，例如重音符号。光标不会出现在 COMBINING 字形之前。
 * </ul>
 * <p>
 * 通过 <code>GlyphMetrics</code> 可以获得的其他度量包括前进步长的组成部分、视觉边界以及左右边距。
 * <p>
 * 旋转字体的字形，或从已对字形应用旋转的 <code>GlyphVector</code> 中获得的字形，其前进步长可能包含 X 和 Y 组件。通常，前进步长只有一个组件。
 * <p>
 * 字形的前进步长是从字形的原点到沿基线的下一个字形原点的距离，基线可以是垂直或水平的。注意，在 <code>GlyphVector</code> 中，从一个字形到其后一个字形的距离可能不是该字形的前进步长，因为可能进行了字距调整或其他定位调整。
 * <p>
 * 边界是最小的完全包含字形轮廓的矩形。边界矩形相对于字形的原点。左边距是从字形原点到边界矩形左侧的距离。如果左边距为负，则字形的一部分绘制在原点的左侧。右边距是从边界矩形右侧到下一个字形原点（原点加上前进步长）的距离。如果为负，则字形的一部分绘制在下一个字形原点的右侧。注意，边界不一定完全包含渲染字形时影响的所有像素，因为存在光栅化和像素调整效果。
 * <p>
 * 虽然可以直接构造 <code>GlyphMetrics</code> 实例，但几乎总是从 <code>GlyphVector</code> 中获得。构造后，<code>GlyphMetrics</code> 对象是不可变的。
 * <p>
 * <strong>示例</strong>：<p>
 * 查询 <code>Font</code> 以获取字形信息
 * <blockquote><pre>
 * Font font = ...;
 * int glyphIndex = ...;
 * GlyphMetrics metrics = GlyphVector.getGlyphMetrics(glyphIndex);
 * int isStandard = metrics.isStandard();
 * float glyphAdvance = metrics.getAdvance();
 * </pre></blockquote>
 * @see java.awt.Font
 * @see GlyphVector
 */

public final class GlyphMetrics {
    /**
     * 表示度量是针对水平基线还是垂直基线。
     */
    private boolean horizontal;

    /**
     * 前进步长的 X 组件。
     */
    private float advanceX;

    /**
     * 前进步长的 Y 组件。
     */
    private float advanceY;

    /**
     * 关联字形的边界。
     */
    private Rectangle2D.Float bounds;

    /**
     * 以字节形式编码的关于字形的附加信息。
     */
    private byte glyphType;

    /**
     * 表示表示单个标准字符的字形。
     */
    public static final byte STANDARD = 0;

    /**
     * 表示作为连字表示多个字符的字形，例如 'fi' 或 'ffi'。它后面是剩余字符的填充字形。填充字形和组合字形可以混合使用，以控制重音符号在逻辑上前面的连字上的位置。
     */
    public static final byte LIGATURE = 1;

    /**
     * 表示表示组合字符的字形，例如分音符。此字形与前面的字形之间没有光标位置。
     */
    public static final byte COMBINING = 2;

    /**
     * 表示在后备存储中没有对应字符的字形。该字形与逻辑上前面的非组件字形相关联。这用于卡西达对齐或其他对现有字形的视觉修改。此字形与前面的字形之间没有光标位置。
     */
    public static final byte COMPONENT = 3;

    /**
     * 表示没有视觉表示的字形。它可以添加到其他代码值中，以表示不可见的字形。
     */
    public static final byte WHITESPACE = 4;

    /**
     * 构造 <code>GlyphMetrics</code> 对象。
     * @param advance 字形的前进步长宽度
     * @param bounds 字形的黑色框边界
     * @param glyphType 字形的类型
     */
    public GlyphMetrics(float advance, Rectangle2D bounds, byte glyphType) {
        this.horizontal = true;
        this.advanceX = advance;
        this.advanceY = 0;
        this.bounds = new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType = glyphType;
    }

    /**
     * 构造 <code>GlyphMetrics</code> 对象。
     * @param horizontal 如果为 true，则度量是针对水平基线的，否则是针对垂直基线的
     * @param advanceX 字形前进步长的 X 组件
     * @param advanceY 字形前进步长的 Y 组件
     * @param bounds 字形的视觉边界
     * @param glyphType 字形的类型
     * @since 1.4
     */
    public GlyphMetrics(boolean horizontal, float advanceX, float advanceY,
                        Rectangle2D bounds, byte glyphType) {

        this.horizontal = horizontal;
        this.advanceX = advanceX;
        this.advanceY = advanceY;
        this.bounds = new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType = glyphType;
    }

    /**
     * 返回字形沿基线（水平或垂直）的前进步长。
     * @return 字形的前进步长
     */
    public float getAdvance() {
        return horizontal ? advanceX : advanceY;
    }

    /**
     * 返回字形前进步长的 X 组件。
     * @return 字形前进步长的 X 组件
     * @since 1.4
     */
    public float getAdvanceX() {
        return advanceX;
    }

    /**
     * 返回字形前进步长的 Y 组件。
     * @return 字形前进步长的 Y 组件
     * @since 1.4
     */
    public float getAdvanceY() {
        return advanceY;
    }

    /**
     * 返回字形的边界。这是字形轮廓的边界框。由于光栅化和像素对齐效果，它不一定包含渲染字形时影响的所有像素。
     * @return 字形的边界，类型为 {@link Rectangle2D}。
     */
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * 返回字形的左（顶）边距。
     * <p>
     * 这是从 0,0 到字形边界左侧（顶部）的距离。如果字形边界在原点的左侧（上方），则 LSB 为负。
     * @return 字形的左（顶）边距。
     */
    public float getLSB() {
        return horizontal ? bounds.x : bounds.y;
    }

    /**
     * 返回字形的右（底）边距。
     * <p>
     * 这是从字形边界右侧（底部）到前进步长的距离。如果字形边界在前进步长的右侧（下方），则 RSB 为负。
     * @return 字形的右（底）边距。
     */
    public float getRSB() {
        return horizontal ?
            advanceX - bounds.x - bounds.width :
            advanceY - bounds.y - bounds.height;
    }

    /**
     * 返回原始字形类型代码。
     * @return 原始字形类型代码。
     */
    public int getType() {
        return glyphType;
    }

    /**
     * 如果这是标准字形，则返回 <code>true</code>。
     * @return 如果这是标准字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isStandard() {
        return (glyphType & 0x3) == STANDARD;
    }

    /**
     * 如果这是连字字形，则返回 <code>true</code>。
     * @return 如果这是连字字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isLigature() {
        return (glyphType & 0x3) == LIGATURE;
    }

    /**
     * 如果这是组合字形，则返回 <code>true</code>。
     * @return 如果这是组合字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isCombining() {
        return (glyphType & 0x3) == COMBINING;
    }

    /**
     * 如果这是组件字形，则返回 <code>true</code>。
     * @return 如果这是组件字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isComponent() {
        return (glyphType & 0x3) == COMPONENT;
    }

    /**
     * 如果这是空白字形，则返回 <code>true</code>。
     * @return 如果这是空白字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isWhitespace() {
        return (glyphType & 0x4) == WHITESPACE;
    }
}
