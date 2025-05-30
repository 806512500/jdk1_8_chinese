/*
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.GraphicsEnvironment;
import java.awt.color.ICC_Profile;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import sun.awt.image.ImagingLib;
import java.util.Arrays;

/**
 * 该类执行一个任意的线性组合，使用指定的矩阵对 <CODE>Raster</CODE> 中的带进行操作。
 * <p>
 * 矩阵的宽度必须等于源 <CODE>Raster</CODE> 中的带数，可选地加一。如果矩阵中的列数比带数多一，
 * 则在表示像素的带样本向量的末尾隐含一个 1。矩阵的高度必须等于目标中的带数。
 * <p>
 * 例如，一个 3 带的 <CODE>Raster</CODE> 可能会对每个像素应用以下变换，以反转 <CODE>Raster</CODE> 的第二带。
 * <pre>
 *   [ 1.0   0.0   0.0    0.0  ]     [ b1 ]
 *   [ 0.0  -1.0   0.0  255.0  ]  x  [ b2 ]
 *   [ 0.0   0.0   1.0    0.0  ]     [ b3 ]
 *                                   [ 1 ]
 * </pre>
 *
 * <p>
 * 注意，源和目标可以是同一个对象。
 */
public class BandCombineOp implements  RasterOp {
    float[][] matrix;
    int nrows = 0;
    int ncols = 0;
    RenderingHints hints;

    /**
     * 使用指定的矩阵构造一个 <CODE>BandCombineOp</CODE>。
     * 矩阵的宽度必须等于源 <CODE>Raster</CODE> 中的带数，可选地加一。如果矩阵中的列数比带数多一，
     * 则在表示像素的带样本向量的末尾隐含一个 1。矩阵的高度必须等于目标中的带数。
     * <p>
     * 第一个下标是行索引，第二个是列索引。此操作不使用当前定义的任何渲染提示；
     * <CODE>RenderingHints</CODE> 参数可以为 null。
     *
     * @param matrix 用于带组合操作的矩阵。
     * @param hints 与此操作相关的 <CODE>RenderingHints</CODE> 对象。目前未使用，因此可以为 null。
     */
    public BandCombineOp (float[][] matrix, RenderingHints hints) {
        nrows = matrix.length;
        ncols = matrix[0].length;
        this.matrix = new float[nrows][];
        for (int i=0; i < nrows; i++) {
            /* Arrays.copyOf 对源数组过短的情况很宽容，但它的速度也比其他克隆方法快，
             * 因此我们为短矩阵行提供自己的保护。
             */
            if (ncols > matrix[i].length) {
                throw new IndexOutOfBoundsException("行 "+i+" 过短");
            }
            this.matrix[i] = Arrays.copyOf(matrix[i], ncols);
        }
        this.hints  = hints;
    }

    /**
     * 返回线性组合矩阵的副本。
     *
     * @return 与此带组合操作相关的矩阵。
     */
    public final float[][] getMatrix() {
        float[][] ret = new float[nrows][];
        for (int i = 0; i < nrows; i++) {
            ret[i] = Arrays.copyOf(matrix[i], ncols);
        }
        return ret;
    }

    /**
     * 使用构造函数中指定的矩阵转换 <CODE>Raster</CODE>。如果源或目标中的带数与矩阵不兼容，
     * 可能会抛出 <CODE>IllegalArgumentException</CODE>。有关更多详细信息，请参见类注释。
     * <p>
     * 如果目标为 null，将创建一个带数等于矩阵行数的 <CODE>Raster</CODE>。如果操作导致数据溢出，不会抛出异常。
     *
     * @param src 要过滤的 <CODE>Raster</CODE>。
     * @param dst 存储过滤操作结果的 <CODE>Raster</CODE>。
     *
     * @return 过滤后的 <CODE>Raster</CODE>。
     *
     * @throws IllegalArgumentException 如果源或目标中的带数与矩阵不兼容。
     */
    public WritableRaster filter(Raster src, WritableRaster dst) {
        int nBands = src.getNumBands();
        if (ncols != nBands && ncols != (nBands+1)) {
            throw new IllegalArgumentException("矩阵中的列数 ("+ncols+
                                               ") 必须等于源中的带数 ([+1]) ("+
                                               nBands+").");
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        }
        else if (nrows != dst.getNumBands()) {
            throw new IllegalArgumentException("矩阵中的行数 ("+nrows+
                                               ") 必须等于目标中的带数 ([+1]) ("+
                                               nBands+").");
        }

        if (ImagingLib.filter(this, src, dst) != null) {
            return dst;
        }

        int[] pixel = null;
        int[] dstPixel = new int[dst.getNumBands()];
        float accum;
        int sminX = src.getMinX();
        int sY = src.getMinY();
        int dminX = dst.getMinX();
        int dY = dst.getMinY();
        int sX;
        int dX;
        if (ncols == nBands) {
            for (int y=0; y < src.getHeight(); y++, sY++, dY++) {
                dX = dminX;
                sX = sminX;
                for (int x=0; x < src.getWidth(); x++, sX++, dX++) {
                    pixel = src.getPixel(sX, sY, pixel);
                    for (int r=0; r < nrows; r++) {
                        accum = 0.f;
                        for (int c=0; c < ncols; c++) {
                            accum += matrix[r][c]*pixel[c];
                        }
                        dstPixel[r] = (int) accum;
                    }
                    dst.setPixel(dX, dY, dstPixel);
                }
            }
        }
        else {
            // 需要添加常数
            for (int y=0; y < src.getHeight(); y++, sY++, dY++) {
                dX = dminX;
                sX = sminX;
                for (int x=0; x < src.getWidth(); x++, sX++, dX++) {
                    pixel = src.getPixel(sX, sY, pixel);
                    for (int r=0; r < nrows; r++) {
                        accum = 0.f;
                        for (int c=0; c < nBands; c++) {
                            accum += matrix[r][c]*pixel[c];
                        }
                        dstPixel[r] = (int) (accum+matrix[r][nBands]);
                    }
                    dst.setPixel(dX, dY, dstPixel);
                }
            }
        }

        return dst;
    }

    /**
     * 返回转换后目标的边界框。由于这不是几何操作，源和目标的边界框相同。
     * 如果源中的带数与矩阵不兼容，可能会抛出 <CODE>IllegalArgumentException</CODE>。有关更多详细信息，请参见类注释。
     *
     * @param src 要过滤的 <CODE>Raster</CODE>。
     *
     * @return 表示目标图像边界框的 <CODE>Rectangle2D</CODE>。
     *
     * @throws IllegalArgumentException 如果源中的带数与矩阵不兼容。
     */
    public final Rectangle2D getBounds2D (Raster src) {
        return src.getBounds();
    }


    /**
     * 创建一个大小和带数正确的零化目标 <CODE>Raster</CODE>。
     * 如果源中的带数与矩阵不兼容，可能会抛出 <CODE>IllegalArgumentException</CODE>。有关更多详细信息，请参见类注释。
     *
     * @param src 要过滤的 <CODE>Raster</CODE>。
     *
     * @return 零化的目标 <CODE>Raster</CODE>。
     */
    public WritableRaster createCompatibleDestRaster (Raster src) {
        int nBands = src.getNumBands();
        if ((ncols != nBands) && (ncols != (nBands+1))) {
            throw new IllegalArgumentException("矩阵中的列数 ("+ncols+
                                               ") 必须等于源中的带数 ([+1]) ("+
                                               nBands+").");
        }
        if (src.getNumBands() == nrows) {
            return src.createCompatibleWritableRaster();
        }
        else {
            throw new IllegalArgumentException("不知道如何创建一个带有 "+
                                               nrows+" 带的兼容 Raster。");
        }
    }

    /**
     * 返回给定源 <CODE>Raster</CODE> 中点对应的目标点的位置。如果指定了 <CODE>dstPt</CODE>，则使用它来保存返回值。
     * 由于这不是几何操作，返回的点与指定的 <CODE>srcPt</CODE> 相同。
     *
     * @param srcPt 表示源 <CODE>Raster</CODE> 中点的 <code>Point2D</code>
     * @param dstPt 用于存储结果的 <CODE>Point2D</CODE>。
     *
     * @return 与源图像中指定点对应的目标图像中的 <CODE>Point2D</CODE>。
     */
    public final Point2D getPoint2D (Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());

        return dstPt;
    }

    /**
     * 返回此操作的渲染提示。
     *
     * @return 与此操作相关的 <CODE>RenderingHints</CODE> 对象。如果没有设置提示，则返回 null。
     */
    public final RenderingHints getRenderingHints() {
        return hints;
    }
}
