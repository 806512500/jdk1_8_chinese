/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * 该类用于与 CHAR_REPLACEMENT 属性一起使用。
 * <p>
 * <code>GraphicAttribute</code> 类表示嵌入在文本中的图形。客户端可以继承此类来实现自己的字符替换图形。希望在文本中嵌入形状和图像的客户端不必继承此类。相反，客户端可以使用 {@link ShapeGraphicAttribute} 和 {@link ImageGraphicAttribute} 类。
 * <p>
 * 子类必须确保其对象在构造后不可变。在 {@link TextLayout} 中使用的 <code>GraphicAttribute</code> 发生变化会导致 <code>TextLayout</code> 的行为未定义。
 */
public abstract class GraphicAttribute {

    private int fAlignment;

    /**
     * 将图形的顶部对齐到行的顶部。
     */
    public static final int TOP_ALIGNMENT = -1;

    /**
     * 将图形的底部对齐到行的底部。
     */
    public static final int BOTTOM_ALIGNMENT = -2;

    /**
     * 将图形的原点对齐到行的罗马基线。
     */
    public static final int ROMAN_BASELINE = Font.ROMAN_BASELINE;

    /**
     * 将图形的原点对齐到行的中心基线。
     */
    public static final int CENTER_BASELINE = Font.CENTER_BASELINE;

    /**
     * 将图形的原点对齐到行的悬挂基线。
     */
    public static final int HANGING_BASELINE = Font.HANGING_BASELINE;

    /**
     * 构造一个 <code>GraphicAttribute</code>。
     * 子类使用此方法定义图形的对齐方式。
     * @param alignment 一个表示 <code>GraphicAttribute</code> 对齐字段之一的整数
     * @throws IllegalArgumentException 如果对齐方式不是五个定义值之一。
     */
    protected GraphicAttribute(int alignment) {
        if (alignment < BOTTOM_ALIGNMENT || alignment > HANGING_BASELINE) {
          throw new IllegalArgumentException("bad alignment");
        }
        fAlignment = alignment;
    }

    /**
     * 返回此 <code>GraphicAttribute</code> 的上升高度。图形可以在其上升高度之上绘制。
     * @return 此 <code>GraphicAttribute</code> 的上升高度。
     * @see #getBounds()
     */
    public abstract float getAscent();


    /**
     * 返回此 <code>GraphicAttribute</code> 的下降高度。图形可以在其下降高度之下绘制。
     * @return 此 <code>GraphicAttribute</code> 的下降高度。
     * @see #getBounds()
     */
    public abstract float getDescent();

    /**
     * 返回此 <code>GraphicAttribute</code> 的前进距离。前进距离是从图形绘制点到下一个字符或图形绘制点的距离。图形可以在其前进距离之外绘制。
     * @return 此 <code>GraphicAttribute</code> 的前进距离。
     * @see #getBounds()
     */
    public abstract float getAdvance();

    /**
     * 返回一个 {@link Rectangle2D}，该矩形包含此 <code>GraphicAttribute</code> 相对于渲染位置绘制的所有位图。图形可能在其原点、上升高度、下降高度或前进距离之外绘制；但如果这样做，此方法的实现必须指示图形的绘制位置。默认边界是矩形 (0, -ascent, advance, ascent+descent)。
     * @return 一个包含此 <code>GraphicAttribute</code> 绘制的所有位图的 <code>Rectangle2D</code>。
     */
    public Rectangle2D getBounds() {
        float ascent = getAscent();
        return new Rectangle2D.Float(0, -ascent,
                                        getAdvance(), ascent+getDescent());
    }

    /**
     * 返回一个 {@link java.awt.Shape}，表示此 <code>GraphicAttribute</code> 渲染的区域。当 {@link TextLayout} 被请求返回文本的轮廓时，使用此方法。未经变换的形状不得超出 <code>getBounds</code> 返回的矩形边界。默认实现返回由提供的 {@link AffineTransform} 变换的 <code>getBounds</code> 返回的矩形。
     * @param tx 一个可选的 {@link AffineTransform}，用于变换此 <code>GraphicAttribute</code> 的轮廓。可以为 null。
     * @return 一个表示此图形属性的 <code>Shape</code>，适合描边或填充。
     * @since 1.6
     */
    public Shape getOutline(AffineTransform tx) {
        Shape b = getBounds();
        if (tx != null) {
            b = tx.createTransformedShape(b);
        }
        return b;
    }

    /**
     * 在指定位置渲染此 <code>GraphicAttribute</code>。
     * @param graphics 用于渲染图形的 {@link Graphics2D}
     * @param x 图形渲染的用户空间 X 坐标
     * @param y 图形渲染的用户空间 Y 坐标
     */
    public abstract void draw(Graphics2D graphics, float x, float y);

    /**
     * 返回此 <code>GraphicAttribute</code> 的对齐方式。对齐方式可以是特定的基线，也可以是行的绝对顶部或底部。
     * @return 此 <code>GraphicAttribute</code> 的对齐方式。
     */
    public final int getAlignment() {

        return fAlignment;
    }

    /**
     * 返回此 <code>GraphicAttribute</code> 的对齐信息。子类可以重写此方法以提供不同的对齐信息。
     * @return 一个包含此 <code>GraphicAttribute</code> 对齐信息的 {@link GlyphJustificationInfo} 对象。
     */
    public GlyphJustificationInfo getJustificationInfo() {

        // 我们应该缓存这个吗？
        float advance = getAdvance();

        return new GlyphJustificationInfo(
                                     advance,   // 权重
                                     false,     // 增长吸收
                                     2,         // 增长优先级
                                     advance/3, // 左增长限制
                                     advance/3, // 右增长限制
                                     false,     // 收缩吸收
                                     1,         // 收缩优先级
                                     0,         // 左收缩限制
                                     0);        // 右收缩限制
    }
}
