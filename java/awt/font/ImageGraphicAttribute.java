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

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * <code>ImageGraphicAttribute</code> 类是 {@link GraphicAttribute} 的一个实现，
 * 用于在 {@link TextLayout} 中绘制图像。
 * @see GraphicAttribute
 */

public final class ImageGraphicAttribute extends GraphicAttribute {

    private Image fImage;
    private float fImageWidth, fImageHeight;
    private float fOriginX, fOriginY;

    /**
     * 从指定的 {@link Image} 构造一个 <code>ImageGraphicAttribute</code>。
     * 原点位于 (0,&nbsp;0)。
     * @param image 由这个 <code>ImageGraphicAttribute</code> 渲染的 <code>Image</code>。
     * 该对象保留对 <code>image</code> 的引用。
     * @param alignment 该 <code>ImageGraphicAttribute</code> 的对齐方式之一。
     */
    public ImageGraphicAttribute(Image image, int alignment) {

        this(image, alignment, 0, 0);
    }

    /**
     * 从指定的 <code>Image</code> 构造一个 <code>ImageGraphicAttribute</code>。
     * <code>Image</code> 中的点 (<code>originX</code>,&nbsp;<code>originY</code>)
     * 出现在文本中的 <code>ImageGraphicAttribute</code> 的原点。
     * @param image 由这个 <code>ImageGraphicAttribute</code> 渲染的 <code>Image</code>。
     * 该对象保留对 <code>image</code> 的引用。
     * @param alignment 该 <code>ImageGraphicAttribute</code> 的对齐方式之一。
     * @param originX 在 <code>Image</code> 中出现在文本行中 <code>ImageGraphicAttribute</code> 原点的 X 坐标。
     * @param originY 在 <code>Image</code> 中出现在文本行中 <code>ImageGraphicAttribute</code> 原点的 Y 坐标。
     */
    public ImageGraphicAttribute(Image image,
                                 int alignment,
                                 float originX,
                                 float originY) {

        super(alignment);

        // 无法克隆图像
        // fImage = (Image) image.clone();
        fImage = image;

        fImageWidth = image.getWidth(null);
        fImageHeight = image.getHeight(null);

        // 确保原点在图像内？
        fOriginX = originX;
        fOriginY = originY;
    }

    /**
     * 返回此 <code>ImageGraphicAttribute</code> 的上升高度。上升高度是从图像顶部到原点的距离。
     * @return 此 <code>ImageGraphicAttribute</code> 的上升高度。
     */
    public float getAscent() {

        return Math.max(0, fOriginY);
    }

    /**
     * 返回此 <code>ImageGraphicAttribute</code> 的下降高度。下降高度是从原点到图像底部的距离。
     * @return 此 <code>ImageGraphicAttribute</code> 的下降高度。
     */
    public float getDescent() {

        return Math.max(0, fImageHeight-fOriginY);
    }

    /**
     * 返回此 <code>ImageGraphicAttribute</code> 的前进宽度。前进宽度是从原点到图像右侧的距离。
     * @return 此 <code>ImageGraphicAttribute</code> 的前进宽度。
     */
    public float getAdvance() {

        return Math.max(0, fImageWidth-fOriginX);
    }

    /**
     * 返回一个 {@link Rectangle2D}，该矩形包围了此 <code>ImageGraphicAttribute</code> 渲染的所有位图，
     * 相对于渲染位置。图形可以在其原点、上升高度、下降高度或前进宽度之外渲染；
     * 但如果这样，此方法的实现必须指示图形的渲染位置。
     * @return 包围此 <code>ImageGraphicAttribute</code> 渲染的所有位图的 <code>Rectangle2D</code>。
     */
    public Rectangle2D getBounds() {

        return new Rectangle2D.Float(
                        -fOriginX, -fOriginY, fImageWidth, fImageHeight);
    }

    /**
     * {@inheritDoc}
     */
    public void draw(Graphics2D graphics, float x, float y) {

        graphics.drawImage(fImage, (int) (x-fOriginX), (int) (y-fOriginY), null);
    }

    /**
     * 返回此 <code>ImageGraphicAttribute</code> 的哈希码。
     * @return 该对象的哈希码值。
     */
    public int hashCode() {

        return fImage.hashCode();
    }

    /**
     * 比较此 <code>ImageGraphicAttribute</code> 与指定的 {@link Object} 是否相等。
     * @param rhs 要比较的 <code>Object</code>。
     * @return 如果此 <code>ImageGraphicAttribute</code> 等于 <code>rhs</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    public boolean equals(Object rhs) {

        try {
            return equals((ImageGraphicAttribute) rhs);
        }
        catch(ClassCastException e) {
            return false;
        }
    }

    /**
     * 比较此 <code>ImageGraphicAttribute</code> 与指定的 <code>ImageGraphicAttribute</code> 是否相等。
     * @param rhs 要比较的 <code>ImageGraphicAttribute</code>。
     * @return 如果此 <code>ImageGraphicAttribute</code> 等于 <code>rhs</code>，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    public boolean equals(ImageGraphicAttribute rhs) {

        if (rhs == null) {
            return false;
        }

        if (this == rhs) {
            return true;
        }

        if (fOriginX != rhs.fOriginX || fOriginY != rhs.fOriginY) {
            return false;
        }

        if (getAlignment() != rhs.getAlignment()) {
            return false;
        }

        if (!fImage.equals(rhs.fImage)) {
            return false;
        }

        return true;
    }
}
