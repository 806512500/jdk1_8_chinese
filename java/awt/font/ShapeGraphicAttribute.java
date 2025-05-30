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

import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * <code>ShapeGraphicAttribute</code> 类是 {@link GraphicAttribute} 的一个实现，
 * 用于在 {@link TextLayout} 中绘制形状。
 * @see GraphicAttribute
 */
public final class ShapeGraphicAttribute extends GraphicAttribute {

    private Shape fShape;
    private boolean fStroke;

    /**
     * 表示形状应使用 1 像素宽的描边绘制的键。
     */
    public static final boolean STROKE = true;

    /**
     * 表示形状应填充的键。
     */
    public static final boolean FILL = false;

    // 缓存形状边界，因为 GeneralPath 没有
    private Rectangle2D fShapeBounds;

    /**
     * 构造一个指定 {@link Shape} 的 <code>ShapeGraphicAttribute</code>。
     * @param shape 要渲染的 <code>Shape</code>。此 <code>Shape</code> 以其在宿主 <code>TextLayout</code> 中的
     * <code>ShapeGraphicAttribute</code> 原点为起点进行渲染。此对象保留对 <code>shape</code> 的引用。
     * @param alignment 来自此 <code>ShapeGraphicAttribute</code> 的对齐方式之一。
     * @param stroke <code>true</code> 表示应描边 <code>Shape</code>；<code>false</code> 表示应填充 <code>Shape</code>。
     */
    public ShapeGraphicAttribute(Shape shape,
                                 int alignment,
                                 boolean stroke) {

        super(alignment);

        fShape = shape;
        fStroke = stroke;
        fShapeBounds = fShape.getBounds2D();
    }

    /**
     * 返回此 <code>ShapeGraphicAttribute</code> 的上升高度。上升高度是从其 <code>Shape</code> 的原点到
     * <code>Shape</code> 边界的顶部的正距离。
     * @return 此 <code>ShapeGraphicAttribute</code> 的上升高度。
     */
    public float getAscent() {

        return (float) Math.max(0, -fShapeBounds.getMinY());
    }

    /**
     * 返回此 <code>ShapeGraphicAttribute</code> 的下降高度。下降高度是从其 <code>Shape</code> 的原点到
     * <code>Shape</code> 边界的底部的距离。
     * @return 此 <code>ShapeGraphicAttribute</code> 的下降高度。
     */
    public float getDescent() {

        return (float) Math.max(0, fShapeBounds.getMaxY());
    }

    /**
     * 返回此 <code>ShapeGraphicAttribute</code> 的前进距离。前进距离是从其 <code>Shape</code> 的原点到
     * <code>Shape</code> 边界的右侧的距离。
     * @return 此 <code>ShapeGraphicAttribute</code> 的前进距离。
     */
    public float getAdvance() {

        return (float) Math.max(0, fShapeBounds.getMaxX());
    }

    /**
     * {@inheritDoc}
     */
    public void draw(Graphics2D graphics, float x, float y) {

        // 将图形平移以绘制形状 !!!
        graphics.translate((int)x, (int)y);

        try {
            if (fStroke == STROKE) {
                // 提醒：设置描边的正确大小
                graphics.draw(fShape);
            }
            else {
                graphics.fill(fShape);
            }
        }
        finally {
            graphics.translate(-(int)x, -(int)y);
        }
    }

    /**
     * 返回一个 {@link Rectangle2D}，该矩形包围了此 <code>ShapeGraphicAttribute</code> 相对于渲染位置绘制的所有位。
     * 图形可以在其原点、上升高度、下降高度或前进距离之外绘制；但如果这样做，此方法的实现应指示图形的绘制位置。
     * @return 一个 <code>Rectangle2D</code>，包围了此 <code>ShapeGraphicAttribute</code> 绘制的所有位。
     */
    public Rectangle2D getBounds() {

        Rectangle2D.Float bounds = new Rectangle2D.Float();
        bounds.setRect(fShapeBounds);

        if (fStroke == STROKE) {
            ++bounds.width;
            ++bounds.height;
        }

        return bounds;
    }

    /**
     * 返回一个 {@link java.awt.Shape}，表示此 <code>ShapeGraphicAttribute</code> 渲染的区域。当
     * {@link TextLayout} 请求返回文本的轮廓时，会使用此方法。未经变换的形状不得超出 <code>getBounds</code>
     * 返回的矩形边界。
     * @param tx 一个可选的 {@link AffineTransform}，应用于此 <code>ShapeGraphicAttribute</code>。可以为 null。
     * @return 一个 <code>Shape</code>，表示此图形属性，适用于描边或填充。
     * @since 1.6
     */
    public Shape getOutline(AffineTransform tx) {
        return tx == null ? fShape : tx.createTransformedShape(fShape);
    }

    /**
     * 返回此 <code>ShapeGraphicAttribute</code> 的哈希码。
     * @return 此 <code>ShapeGraphicAttribute</code> 的哈希码值。
     */
    public int hashCode() {

        return fShape.hashCode();
    }

    /**
     * 比较此 <code>ShapeGraphicAttribute</code> 与指定的 <code>Object</code> 是否相等。
     * @param rhs 要比较的 <code>Object</code>
     * @return 如果此 <code>ShapeGraphicAttribute</code> 等于 <code>rhs</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    public boolean equals(Object rhs) {

        try {
            return equals((ShapeGraphicAttribute) rhs);
        }
        catch(ClassCastException e) {
            return false;
        }
    }

    /**
     * 比较此 <code>ShapeGraphicAttribute</code> 与指定的 <code>ShapeGraphicAttribute</code> 是否相等。
     * @param rhs 要比较的 <code>ShapeGraphicAttribute</code>
     * @return 如果此 <code>ShapeGraphicAttribute</code> 等于 <code>rhs</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    public boolean equals(ShapeGraphicAttribute rhs) {

        if (rhs == null) {
            return false;
        }

        if (this == rhs) {
            return true;
        }

        if (fStroke != rhs.fStroke) {
            return false;
        }

        if (getAlignment() != rhs.getAlignment()) {
            return false;
        }

        if (!fShape.equals(rhs.fShape)) {
            return false;
        }

        return true;
    }
}
