/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.image;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;

/**
 * 此接口描述了对 <CODE>BufferedImage</CODE> 对象执行的单输入/单输出操作。
 * 它由 <CODE>AffineTransformOp</CODE>、<CODE>ConvolveOp</CODE>、<CODE>ColorConvertOp</CODE>、<CODE>RescaleOp</CODE>
 * 和 <CODE>LookupOp</CODE> 实现。这些对象可以传递给 <CODE>BufferedImageFilter</CODE>，以在
 * ImageProducer-ImageFilter-ImageConsumer 范式中操作 <CODE>BufferedImage</CODE>。
 * <p>
 * 实现此接口的类必须指定它们是否允许原地过滤——即源对象等于目标对象的过滤操作。
 * <p>
 * 该接口不能用于描述更复杂的操作，例如多源操作。请注意，此限制还意味着在操作之前目标像素的值不会作为过滤操作的输入使用。
 *
 * @see BufferedImage
 * @see BufferedImageFilter
 * @see AffineTransformOp
 * @see BandCombineOp
 * @see ColorConvertOp
 * @see ConvolveOp
 * @see LookupOp
 * @see RescaleOp
 */
public interface BufferedImageOp {
    /**
     * 对 <CODE>BufferedImage</CODE> 执行单输入/单输出操作。
     * 如果两个图像的颜色模型不匹配，则会执行颜色转换，转换为目标颜色模型。
     * 如果目标图像为 null，则会创建一个具有适当 <CODE>ColorModel</CODE> 的 <CODE>BufferedImage</CODE>。
     * <p>
     * 如果源和/或目标图像与实现此过滤器的类允许的图像类型不兼容，则可能会抛出 <CODE>IllegalArgumentException</CODE>。
     *
     * @param src 要过滤的 <CODE>BufferedImage</CODE>
     * @param dest 存储结果的 <CODE>BufferedImage</CODE>
     *
     * @return 过滤后的 <CODE>BufferedImage</CODE>。
     *
     * @throws IllegalArgumentException 如果源和/或目标图像与实现此过滤器的类允许的图像类型不兼容。
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dest);

    /**
     * 返回过滤后目标图像的边界框。
     * 如果源图像与实现此过滤器的类允许的图像类型不兼容，则可能会抛出 <CODE>IllegalArgumentException</CODE>。
     *
     * @param src 要过滤的 <CODE>BufferedImage</CODE>
     *
     * @return 表示目标图像边界框的 <CODE>Rectangle2D</CODE>。
     */
    public Rectangle2D getBounds2D (BufferedImage src);

    /**
     * 创建具有正确大小和波段数的零化目标图像。
     * 如果源图像与实现此过滤器的类允许的图像类型不兼容，则可能会抛出 <CODE>IllegalArgumentException</CODE>。
     *
     * @param src 要过滤的 <CODE>BufferedImage</CODE>
     * @param destCM 目标图像的 <CODE>ColorModel</CODE>。如果为 null，则使用源图像的 <CODE>ColorModel</CODE>。
     *
     * @return 零化的目标图像。
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM);

    /**
     * 返回给定源图像中点对应的目标图像中的点。如果指定了 <CODE>dstPt</CODE>，则使用它来存储返回值。
     * @param srcPt 代表源图像中点的 <code>Point2D</code>
     * @param dstPt 存储结果的 <CODE>Point2D</CODE>
     *
     * @return 对应于源图像中指定点的目标图像中的 <CODE>Point2D</CODE>。
     */
    public Point2D getPoint2D (Point2D srcPt, Point2D dstPt);

    /**
     * 返回此操作的渲染提示。
     *
     * @return 此 <CODE>BufferedImageOp</CODE> 的 <CODE>RenderingHints</CODE> 对象。如果没有设置提示，则返回 null。
     */
    public RenderingHints getRenderingHints();
}
