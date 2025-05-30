/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * <code>TexturePaint</code> 类提供了一种使用指定的 {@link BufferedImage} 填充 {@link Shape} 的方法。
 * <code>BufferedImage</code> 对象的大小应该较小，因为 <code>BufferedImage</code> 数据会被 <code>TexturePaint</code> 对象复制。
 * 在构造时，纹理被锚定到用户空间中指定的 {@link Rectangle2D} 的左上角。通过在用户空间中无限地复制指定的 <code>Rectangle2D</code>
 * 并将 <code>BufferedImage</code> 映射到每个复制的 <code>Rectangle2D</code>，来计算设备空间中的位置的纹理。
 * @see Paint
 * @see Graphics2D#setPaint
 * @version 1.48, 06/05/07
 */

public class TexturePaint implements Paint {

    BufferedImage bufImg;
    double tx;
    double ty;
    double sx;
    double sy;

    /**
     * 构造一个 <code>TexturePaint</code> 对象。
     * @param txtr 用于绘制的 <code>BufferedImage</code> 对象
     * @param anchor 用于锚定和复制纹理的用户空间中的 <code>Rectangle2D</code>
     */
    public TexturePaint(BufferedImage txtr,
                        Rectangle2D anchor) {
        this.bufImg = txtr;
        this.tx = anchor.getX();
        this.ty = anchor.getY();
        this.sx = anchor.getWidth() / bufImg.getWidth();
        this.sy = anchor.getHeight() / bufImg.getHeight();
    }

    /**
     * 返回用于填充形状的 <code>BufferedImage</code> 纹理。
     * @return 一个 <code>BufferedImage</code>。
     */
    public BufferedImage getImage() {
        return bufImg;
    }

    /**
     * 返回一个复制的锚定矩形，该矩形定位并调整纹理图像的大小。
     * @return 用于锚定和调整此 <code>TexturePaint</code> 大小的 <code>Rectangle2D</code>。
     */
    public Rectangle2D getAnchorRect() {
        return new Rectangle2D.Double(tx, ty,
                                      sx * bufImg.getWidth(),
                                      sy * bufImg.getHeight());
    }

    /**
     * 创建并返回一个用于生成平铺图像模式的 {@link PaintContext}。
     * 有关 null 参数处理的信息，请参见 {@link Paint} 接口中的 {@link Paint#createContext} 方法说明。
     *
     * @param cm 一个表示调用者接收像素数据最方便格式的首选 {@link ColorModel}，或如果无偏好则为 {@code null}。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框。
     * @param userBounds 被渲染的图形基元的用户空间边界框。
     * @param xform 从用户空间到设备空间的 {@link AffineTransform}。
     * @param hints 上下文对象可以用来在渲染选项之间进行选择的一组提示。
     * @return 用于生成颜色模式的 {@code PaintContext}。
     * @see Paint
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform xform,
                                      RenderingHints hints) {
        if (xform == null) {
            xform = new AffineTransform();
        } else {
            xform = (AffineTransform) xform.clone();
        }
        xform.translate(tx, ty);
        xform.scale(sx, sy);

        return TexturePaintContext.getContext(bufImg, xform, hints,
                                              deviceBounds);
    }

    /**
     * 返回此 <code>TexturePaint</code> 的透明度模式。
     * @return 此 <code>TexturePaint</code> 的透明度模式，以整数值表示。
     * @see Transparency
     */
    public int getTransparency() {
        return (bufImg.getColorModel()).getTransparency();
    }

}
