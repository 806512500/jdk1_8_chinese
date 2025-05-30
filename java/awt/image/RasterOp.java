/*
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
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
 * 此接口描述了在 Raster 对象上执行的单输入/单输出操作。它由 AffineTransformOp、ConvolveOp 和 LookupOp 等类实现。
 * 源和目标对象必须包含特定类实现此接口所需的适当数量的波段。否则，将抛出异常。此接口不能用于描述更复杂的操作，例如多源操作。
 * 每个实现此接口的类将指定是否允许原地过滤操作（即源对象等于目标对象）。请注意，限制为单输入操作意味着在操作之前目标像素的值不会用作过滤操作的输入。
 * @see AffineTransformOp
 * @see BandCombineOp
 * @see ColorConvertOp
 * @see ConvolveOp
 * @see LookupOp
 * @see RescaleOp
 */
public interface RasterOp {
    /**
     * 从源 Raster 执行单输入/单输出操作到目标 Raster。如果目标 Raster 为 null，将创建一个新的 Raster。
     * 如果源和/或目标 Raster 与实现此过滤器的类允许的 Raster 类型不兼容，可能会抛出 IllegalArgumentException。
     * @param src 源 <code>Raster</code>
     * @param dest 目标 <code>WritableRaster</code>
     * @return 代表过滤操作结果的 <code>WritableRaster</code>。
     */
    public WritableRaster filter(Raster src, WritableRaster dest);

    /**
     * 返回过滤后的目标 Raster 的边界框。如果源 Raster 与实现此过滤器的类允许的 Raster 类型不兼容，可能会抛出 IllegalArgumentException。
     * @param src 源 <code>Raster</code>
     * @return 一个 <code>Rectangle2D</code>，表示过滤操作后 <code>Raster</code> 的边界框。
     */
    public Rectangle2D getBounds2D(Raster src);

    /**
     * 创建一个具有正确大小和波段数量的零目标 Raster。如果源 Raster 与实现此过滤器的类允许的 Raster 类型不兼容，可能会抛出 IllegalArgumentException。
     * @param src 源 <code>Raster</code>
     * @return 与 <code>src</code> 兼容的 <code>WritableRaster</code>。
     */
    public WritableRaster createCompatibleDestRaster(Raster src);

    /**
     * 返回给定源 Raster 中点的目标点位置。如果 dstPt 非空，它将用于保存返回值。
     * @param srcPt 源 <code>Point2D</code>
     * @param dstPt 目标 <code>Point2D</code>
     * @return 目标点的位置。
     */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt);

    /**
     * 返回此 RasterOp 的渲染提示。如果没有设置提示，则返回 null。
     * @return 此 <code>RasterOp</code> 的 <code>RenderingHints</code> 对象。
     */
    public RenderingHints getRenderingHints();
}
