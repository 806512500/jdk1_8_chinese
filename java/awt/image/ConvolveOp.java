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

package java.awt.image;

import java.awt.color.ICC_Profile;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.lang.annotation.Native;
import sun.awt.image.ImagingLib;

/**
 * 该类实现了从源到目标的卷积。
 * 使用卷积核进行卷积是一种空间操作，通过将卷积核与输入像素的邻域相乘来计算输出像素。
 * 这允许输出像素以数学方式指定的核来影响其邻域。
 *<p>
 * 该类操作的 BufferedImage 数据中，颜色分量是与 alpha 分量预乘的。如果源 BufferedImage 有 alpha 分量，
 * 且颜色分量未与 alpha 分量预乘，则在卷积前会进行预乘。如果目标没有预乘的颜色分量，则在存储到目标前会将 alpha 分量除出
 * （如果 alpha 为 0，颜色分量将被设置为 0）。如果目标没有 alpha 分量，则会先将 alpha 除出颜色分量后再丢弃。
 * <p>
 * Raster 被视为没有 alpha 通道。如果不想对 BufferedImage 的 alpha 通道进行上述处理，可以通过获取源 BufferedImage 的 Raster
 * 并使用该类的 filter 方法来处理 Raster。
 * <p>
 * 如果在构造函数中指定了 RenderingHints 对象，当需要颜色转换时，可以使用颜色渲染提示和抖动提示。
 *<p>
 * 注意，源和目标不能是同一个对象。
 * @see Kernel
 * @see java.awt.RenderingHints#KEY_COLOR_RENDERING
 * @see java.awt.RenderingHints#KEY_DITHERING
 */
public class ConvolveOp implements BufferedImageOp, RasterOp {
    Kernel kernel;
    int edgeHint;
    RenderingHints hints;
    /**
     * 边缘条件常量。
     */

    /**
     * 目标图像边缘的像素被设置为零。这是默认值。
     */

    @Native public static final int EDGE_ZERO_FILL = 0;

    /**
     * 源图像边缘的像素被复制到目标图像的相应像素，不进行修改。
     */
    @Native public static final int EDGE_NO_OP     = 1;

    /**
     * 构造一个 ConvolveOp，给定一个卷积核、一个边缘条件和一个 RenderingHints 对象（可以为 null）。
     * @param kernel 指定的 <code>Kernel</code>
     * @param edgeCondition 指定的边缘条件
     * @param hints 指定的 <code>RenderingHints</code> 对象
     * @see Kernel
     * @see #EDGE_NO_OP
     * @see #EDGE_ZERO_FILL
     * @see java.awt.RenderingHints
     */
    public ConvolveOp(Kernel kernel, int edgeCondition, RenderingHints hints) {
        this.kernel   = kernel;
        this.edgeHint = edgeCondition;
        this.hints    = hints;
    }

    /**
     * 构造一个 ConvolveOp，给定一个卷积核。边缘条件将为 EDGE_ZERO_FILL。
     * @param kernel 指定的 <code>Kernel</code>
     * @see Kernel
     * @see #EDGE_ZERO_FILL
     */
    public ConvolveOp(Kernel kernel) {
        this.kernel   = kernel;
        this.edgeHint = EDGE_ZERO_FILL;
    }

    /**
     * 返回边缘条件。
     * @return 此 <code>ConvolveOp</code> 的边缘条件。
     * @see #EDGE_NO_OP
     * @see #EDGE_ZERO_FILL
     */
    public int getEdgeCondition() {
        return edgeHint;
    }

    /**
     * 返回卷积核。
     * @return 此 <code>ConvolveOp</code> 的 <code>Kernel</code>。
     */
    public final Kernel getKernel() {
        return (Kernel) kernel.clone();
    }

    /**
     * 对 BufferedImage 进行卷积。源图像的每个分量（包括 alpha 分量，如果存在的话）都将被卷积。
     * 如果源图像的颜色模型与目标图像不同，像素将在目标中进行转换。如果目标图像为 null，
     * 将创建一个具有源 ColorModel 的 BufferedImage。如果源和目标是同一个对象，将抛出 IllegalArgumentException。
     * @param src 要过滤的源 <code>BufferedImage</code>
     * @param dst 用于过滤 <code>src</code> 的目标 <code>BufferedImage</code>
     * @return 过滤后的 <code>BufferedImage</code>
     * @throws NullPointerException 如果 <code>src</code> 为 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>src</code> 等于 <code>dst</code>
     * @throws ImagingOpException 如果 <code>src</code> 无法过滤
     */
    public final BufferedImage filter (BufferedImage src, BufferedImage dst) {
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the "+
                                               "same as the dst image");
        }

        boolean needToConvert = false;
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM;
        BufferedImage origDst = dst;

        // 不能对 IndexColorModel 进行卷积。需要扩展它
        if (srcCM instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) srcCM;
            src = icm.convertToIntDiscrete(src.getRaster(), false);
            srcCM = src.getColorModel();
        }

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
            dstCM = srcCM;
            origDst = dst;
        }
        else {
            dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() !=
                dstCM.getColorSpace().getType())
            {
                needToConvert = true;
                dst = createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            }
            else if (dstCM instanceof IndexColorModel) {
                dst = createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            }
        }

        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException ("Unable to convolve src image");
        }

        if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(hints);
            ccop.filter(dst, origDst);
        }
        else if (origDst != dst) {
            java.awt.Graphics2D g = origDst.createGraphics();
            try {
                g.drawImage(dst, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        return origDst;
    }

    /**
     * 对 Raster 进行卷积。源 Raster 的每个波段都将被卷积。
     * 源和目标必须具有相同数量的波段。如果目标 Raster 为 null，将创建一个新的 Raster。
     * 如果源和目标是同一个对象，将抛出 IllegalArgumentException。
     * @param src 要过滤的源 <code>Raster</code>
     * @param dst 用于过滤 <code>src</code> 的目标 <code>WritableRaster</code>
     * @return 过滤后的 <code>WritableRaster</code>
     * @throws NullPointerException 如果 <code>src</code> 为 <code>null</code>
     * @throws ImagingOpException 如果 <code>src</code> 和 <code>dst</code> 的波段数量不同
     * @throws ImagingOpException 如果 <code>src</code> 无法过滤
     * @throws IllegalArgumentException 如果 <code>src</code> 等于 <code>dst</code>
     */
    public final WritableRaster filter (Raster src, WritableRaster dst) {
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        }
        else if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the "+
                                               "same as the dst image");
        }
        else if (src.getNumBands() != dst.getNumBands()) {
            throw new ImagingOpException("Different number of bands in src "+
                                         " and dst Rasters");
        }

        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException ("Unable to convolve src image");
        }

        return dst;
    }

    /**
     * 创建一个具有正确大小和波段数量的零化目标图像。如果 destCM 为 null，将使用适当的 ColorModel。
     * @param src       过滤操作的源图像。
     * @param destCM    目标的颜色模型。可以为 null。
     * @return 具有正确大小和波段数量的目标 <code>BufferedImage</code>。
     */
    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel destCM) {
        BufferedImage image;

        int w = src.getWidth();
        int h = src.getHeight();

        WritableRaster wr = null;

        if (destCM == null) {
            destCM = src.getColorModel();
            // 对 ICM 的支持不多
            if (destCM instanceof IndexColorModel) {
                destCM = ColorModel.getRGBdefault();
            } else {
                /* 尽可能创建与源相似的目标图像
                 */
                wr = src.getData().createCompatibleWritableRaster(w, h);
            }
        }

        if (wr == null) {
            /* 这种情况是当目标颜色模型被显式指定（可能与源图像结构不兼容）或源图像是索引图像时。
             * 我们应该使用目标颜色模型来创建兼容的目标图像。
             */
            wr = destCM.createCompatibleWritableRaster(w, h);
        }

        image = new BufferedImage (destCM, wr,
                                   destCM.isAlphaPremultiplied(), null);

        return image;
    }

    /**
     * 创建一个具有正确大小和波段数量的零化目标 Raster。
     */
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }

    /**
     * 返回过滤后目标图像的边界框。由于这不是几何操作，边界框不会改变。
     */
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return getBounds2D(src.getRaster());
    }

    /**
     * 返回过滤后目标 Raster 的边界框。由于这不是几何操作，边界框不会改变。
     */
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }

    /**
     * 返回给定源点的目标点的位置。如果 dstPt 不为 null，将用于保存返回值。由于这不是几何操作，srcPt 将等于 dstPt。
     */
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());

        return dstPt;
    }

    /**
     * 返回此操作的渲染提示。
     */
    public final RenderingHints getRenderingHints() {
        return hints;
    }
}
