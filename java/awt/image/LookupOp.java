
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

import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import sun.awt.image.ImagingLib;

/**
 * 该类实现了从源到目标的查找操作。查找表对象可能包含单个数组或多个数组，但需遵循以下限制。
 * <p>
 * 对于 Raster，查找操作作用于波段。查找数组的数量可以是一个，此时相同的数组应用于所有波段，或者必须等于源 Raster 波段的数量。
 * <p>
 * 对于 BufferedImage，查找操作作用于颜色和 alpha 组件。查找数组的数量可以是一个，此时相同的数组应用于所有颜色（但不包括 alpha）组件。
 * 否则，查找数组的数量可以等于源颜色组件的数量，此时不执行 alpha 组件（如果存在）的查找。
 * 如果以上两种情况都不适用，查找数组的数量必须等于源颜色组件和 alpha 组件的数量，此时对所有颜色和 alpha 组件执行查找。
 * 这允许对多波段 BufferedImage 进行非均匀重缩放。
 * <p>
 * 具有预乘 alpha 数据的 BufferedImage 与非预乘图像在查找时处理方式相同。也就是说，查找是在 BufferedImage 源的原始数据上按波段进行的，
 * 不考虑数据是否预乘。如果需要进行颜色转换以适应目标 ColorModel，将考虑源和目标的预乘状态。
 * <p>
 * 不能使用具有 IndexColorModel 的图像。
 * <p>
 * 如果在构造函数中指定了 RenderingHints 对象，则在需要颜色转换时可能会使用颜色渲染提示和抖动提示。
 * <p>
 * 该类允许源与目标相同。
 *
 * @see LookupTable
 * @see java.awt.RenderingHints#KEY_COLOR_RENDERING
 * @see java.awt.RenderingHints#KEY_DITHERING
 */

public class LookupOp implements BufferedImageOp, RasterOp {
    private LookupTable ltable;
    private int numComponents;
    RenderingHints hints;

    /**
     * 构造一个给定查找表和 RenderingHints 对象的 LookupOp 对象，该 RenderingHints 对象可以为 null。
     * @param lookup 指定的 LookupTable
     * @param hints 指定的 RenderingHints，可以为 null
     */
    public LookupOp(LookupTable lookup, RenderingHints hints) {
        this.ltable = lookup;
        this.hints  = hints;
        numComponents = ltable.getNumComponents();
    }

    /**
     * 返回 LookupTable。
     * @return 此 LookupOp 的 LookupTable。
     */
    public final LookupTable getTable() {
        return ltable;
    }

    /**
     * 对 BufferedImage 执行查找操作。
     * 如果源图像的颜色模型与目标图像的颜色模型不同，将在目标中转换像素。
     * 如果目标图像为 null，将创建一个具有适当 ColorModel 的 BufferedImage。
     * 如果查找表中的数组数量不符合类注释中所述的限制，或者源图像具有 IndexColorModel，可能会抛出 IllegalArgumentException。
     * @param src 要过滤的 BufferedImage
     * @param dst 存储过滤操作结果的 BufferedImage
     * @return 过滤后的 BufferedImage。
     * @throws IllegalArgumentException 如果查找表中的数组数量不符合类注释中所述的限制，或者源图像具有 IndexColorModel。
     */
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        ColorModel srcCM = src.getColorModel();
        int numBands = srcCM.getNumColorComponents();
        ColorModel dstCM;
        if (srcCM instanceof IndexColorModel) {
            throw new
                IllegalArgumentException("LookupOp 不能应用于索引图像");
        }
        int numComponents = ltable.getNumComponents();
        if (numComponents != 1 &&
            numComponents != srcCM.getNumComponents() &&
            numComponents != srcCM.getNumColorComponents())
        {
            throw new IllegalArgumentException("查找表中的数组数量 (" +
                                               numComponents +
                                               ") 与源图像不兼容: " + src);
        }


        boolean needToConvert = false;

        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
            dstCM = srcCM;
        }
        else {
            if (width != dst.getWidth()) {
                throw new
                    IllegalArgumentException("源宽度 (" + width +
                                             ") 不等于目标宽度 (" +
                                             dst.getWidth() + ")");
            }
            if (height != dst.getHeight()) {
                throw new
                    IllegalArgumentException("源高度 (" + height +
                                             ") 不等于目标高度 (" +
                                             dst.getHeight() + ")");
            }

            dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() !=
                dstCM.getColorSpace().getType())
            {
                needToConvert = true;
                dst = createCompatibleDestImage(src, null);
            }

        }

        BufferedImage origDst = dst;

        if (ImagingLib.filter(this, src, dst) == null) {
            // 慢速处理
            WritableRaster srcRaster = src.getRaster();
            WritableRaster dstRaster = dst.getRaster();

            if (srcCM.hasAlpha()) {
                if (numBands - 1 == numComponents || numComponents == 1) {
                    int minx = srcRaster.getMinX();
                    int miny = srcRaster.getMinY();
                    int[] bands = new int[numBands - 1];
                    for (int i = 0; i < numBands - 1; i++) {
                        bands[i] = i;
                    }
                    srcRaster =
                        srcRaster.createWritableChild(minx, miny,
                                                      srcRaster.getWidth(),
                                                      srcRaster.getHeight(),
                                                      minx, miny,
                                                      bands);
                }
            }
            if (dstCM.hasAlpha()) {
                int dstNumBands = dstRaster.getNumBands();
                if (dstNumBands - 1 == numComponents || numComponents == 1) {
                    int minx = dstRaster.getMinX();
                    int miny = dstRaster.getMinY();
                    int[] bands = new int[numBands - 1];
                    for (int i = 0; i < numBands - 1; i++) {
                        bands[i] = i;
                    }
                    dstRaster =
                        dstRaster.createWritableChild(minx, miny,
                                                      dstRaster.getWidth(),
                                                      dstRaster.getHeight(),
                                                      minx, miny,
                                                      bands);
                }
            }

            filter(srcRaster, dstRaster);
        }

        if (needToConvert) {
            // 颜色模型不同
            ColorConvertOp ccop = new ColorConvertOp(hints);
            ccop.filter(dst, origDst);
        }

        return origDst;
    }

    /**
     * 对 Raster 执行查找操作。
     * 如果目标 Raster 为 null，将创建一个新的 Raster。
     * 如果源 Raster 和目标 Raster 的波段数量不同，或者查找表中的数组数量不符合类注释中所述的限制，可能会抛出 IllegalArgumentException。
     * @param src 要过滤的源 Raster
     * @param dst 存储过滤后的 src 的目标 WritableRaster
     * @return 过滤后的 WritableRaster。
     * @throws IllegalArgumentException 如果源和目标 Raster 的波段数量不同，或者查找表中的数组数量不符合类注释中所述的限制。
     *
     */
    public final WritableRaster filter (Raster src, WritableRaster dst) {
        int numBands  = src.getNumBands();
        int dstLength = dst.getNumBands();
        int height    = src.getHeight();
        int width     = src.getWidth();
        int srcPix[]  = new int[numBands];

        // 如果需要，创建一个新的目标 Raster

        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        }
        else if (height != dst.getHeight() || width != dst.getWidth()) {
            throw new
                IllegalArgumentException ("Rasters 的宽度或高度不匹配");
        }
        dstLength = dst.getNumBands();

        if (numBands != dstLength) {
            throw new
                IllegalArgumentException ("源的通道数量 ("
                                          + numBands +
                                          ") 与目标的通道数量不匹配 ("
                                          + dstLength + ")");
        }
        int numComponents = ltable.getNumComponents();
        if (numComponents != 1 && numComponents != src.getNumBands()) {
            throw new IllegalArgumentException("查找表中的数组数量 (" +
                                               numComponents +
                                               " 与源 Raster 不兼容: " + src);
        }


        if (ImagingLib.filter(this, src, dst) != null) {
            return dst;
        }

        // 优化已知情况
        if (ltable instanceof ByteLookupTable) {
            byteFilter ((ByteLookupTable) ltable, src, dst,
                        width, height, numBands);
        }
        else if (ltable instanceof ShortLookupTable) {
            shortFilter ((ShortLookupTable) ltable, src, dst, width,
                         height, numBands);
        }
        else {
            // 未知情况，慢速处理
            int sminX = src.getMinX();
            int sY = src.getMinY();
            int dminX = dst.getMinX();
            int dY = dst.getMinY();
            for (int y = 0; y < height; y++, sY++, dY++) {
                int sX = sminX;
                int dX = dminX;
                for (int x = 0; x < width; x++, sX++, dX++) {
                    // 获取此 x,y 位置的所有波段的数据
                    src.getPixel(sX, sY, srcPix);

                    // 查找此 x,y 位置的所有波段的数据
                    ltable.lookupPixel(srcPix, srcPix);

                    // 将数据放回所有波段
                    dst.setPixel(dX, dY, srcPix);
                }
            }
        }

        return dst;
    }

    /**
     * 返回过滤后目标图像的边界框。由于这不是几何操作，边界框不会改变。
     * @param src 要过滤的 BufferedImage
     * @return 过滤后定义图像的边界。
     */
    public final Rectangle2D getBounds2D (BufferedImage src) {
        return getBounds2D(src.getRaster());
    }

    /**
     * 返回过滤后目标 Raster 的边界框。由于这不是几何操作，边界框不会改变。
     * @param src 要过滤的 Raster
     * @return 过滤后定义 Raster 的边界。
     */
    public final Rectangle2D getBounds2D (Raster src) {
        return src.getBounds();

    }

    /**
     * 创建具有正确大小和波段数量的零化目标图像。如果 destCM 为 null，将使用适当的 ColorModel。
     * @param src       过滤操作的源图像。
     * @param destCM    目标的 ColorModel，可以为 null。
     * @return 过滤后的目标 BufferedImage。
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM) {
        BufferedImage image;
        int w = src.getWidth();
        int h = src.getHeight();
        int transferType = DataBuffer.TYPE_BYTE;
        if (destCM == null) {
            ColorModel cm = src.getColorModel();
            Raster raster = src.getRaster();
            if (cm instanceof ComponentColorModel) {
                DataBuffer db = raster.getDataBuffer();
                boolean hasAlpha = cm.hasAlpha();
                boolean isPre    = cm.isAlphaPremultiplied();
                int trans        = cm.getTransparency();
                int[] nbits = null;
                if (ltable instanceof ByteLookupTable) {
                    if (db.getDataType() == db.TYPE_USHORT) {
                        // 目标 Raster 应为 byte 类型
                        if (hasAlpha) {
                            nbits = new int[2];
                            if (trans == cm.BITMASK) {
                                nbits[1] = 1;
                            }
                            else {
                                nbits[1] = 8;
                            }
                        }
                        else {
                            nbits = new int[1];
                        }
                        nbits[0] = 8;
                    }
                    // 对于 byte，无需更改 cm
                }
                else if (ltable instanceof ShortLookupTable) {
                    transferType = DataBuffer.TYPE_USHORT;
                    if (db.getDataType() == db.TYPE_BYTE) {
                        if (hasAlpha) {
                            nbits = new int[2];
                            if (trans == cm.BITMASK) {
                                nbits[1] = 1;
                            }
                            else {
                                nbits[1] = 16;
                            }
                        }
                        else {
                            nbits = new int[1];
                        }
                        nbits[0] = 16;
                    }
                }
                if (nbits != null) {
                    cm = new ComponentColorModel(cm.getColorSpace(),
                                                 nbits, hasAlpha, isPre,
                                                 trans, transferType);
                }
            }
            image = new BufferedImage(cm,
                                      cm.createCompatibleWritableRaster(w, h),
                                      cm.isAlphaPremultiplied(),
                                      null);
        }
        else {
            image = new BufferedImage(destCM,
                                      destCM.createCompatibleWritableRaster(w,
                                                                            h),
                                      destCM.isAlphaPremultiplied(),
                                      null);
        }


                    return image;
    }

    /**
     * 创建一个与源兼容的零初始化 <code>Raster</code>，具有正确的大小和波段数。
     * @param src 要转换的 <code>Raster</code>
     * @return 零初始化的目标 <code>Raster</code>。
     */
    public WritableRaster createCompatibleDestRaster (Raster src) {
        return src.createCompatibleWritableRaster();
    }

    /**
     * 返回给定源点的目标点的位置。如果 <code>dstPt</code> 不为
     * <code>null</code>，它将用于保存返回值。
     * 由于这不是几何操作，<code>srcPt</code> 将等于 <code>dstPt</code>。
     * @param srcPt 代表源图像中的点的 <code>Point2D</code>
     * @param dstPt 代表目标中的位置的 <code>Point2D</code>
     * @return 对应于源中指定点的目标中的 <code>Point2D</code>。
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
     * @return 与此操作关联的 <code>RenderingHints</code> 对象。
     */
    public final RenderingHints getRenderingHints() {
        return hints;
    }

    private final void byteFilter(ByteLookupTable lookup, Raster src,
                                  WritableRaster dst,
                                  int width, int height, int numBands) {
        int[] srcPix = null;

        // 查找表和偏移量的引用
        byte[][] table = lookup.getTable();
        int offset = lookup.getOffset();
        int tidx;
        int step=1;

        // 检查是否是应用于所有波段的单个查找
        if (table.length == 1) {
            step=0;
        }

        int x;
        int y;
        int band;
        int len = table[0].length;

        // 遍历数据
        for ( y=0; y < height; y++) {
            tidx = 0;
            for ( band=0; band < numBands; band++, tidx+=step) {
                // 查找此波段、扫描线的数据
                srcPix = src.getSamples(0, y, width, 1, band, srcPix);

                for ( x=0; x < width; x++) {
                    int index = srcPix[x]-offset;
                    if (index < 0 || index > len) {
                        throw new
                            IllegalArgumentException("索引 ("+index+
                                                     ") 超出范围: "+
                                                     " srcPix["+x+
                                                     "]="+ srcPix[x]+
                                                     " 偏移量="+ offset);
                    }
                    // 执行查找
                    srcPix[x] = table[tidx][index];
                }
                // 保存回去
                dst.setSamples(0, y, width, 1, band, srcPix);
            }
        }
    }

    private final void shortFilter(ShortLookupTable lookup, Raster src,
                                   WritableRaster dst,
                                   int width, int height, int numBands) {
        int band;
        int[] srcPix = null;

        // 查找表和偏移量的引用
        short[][] table = lookup.getTable();
        int offset = lookup.getOffset();
        int tidx;
        int step=1;

        // 检查是否是应用于所有波段的单个查找
        if (table.length == 1) {
            step=0;
        }

        int x = 0;
        int y = 0;
        int index;
        int maxShort = (1<<16)-1;
        // 遍历数据
        for (y=0; y < height; y++) {
            tidx = 0;
            for ( band=0; band < numBands; band++, tidx+=step) {
                // 查找此波段、扫描线的数据
                srcPix = src.getSamples(0, y, width, 1, band, srcPix);

                for ( x=0; x < width; x++) {
                    index = srcPix[x]-offset;
                    if (index < 0 || index > maxShort) {
                        throw new
                            IllegalArgumentException("索引超出范围 "+
                                                     index+" x is "+x+
                                                     "srcPix[x]="+srcPix[x]
                                                     +" 偏移量="+ offset);
                    }
                    // 执行查找
                    srcPix[x] = table[tidx][index];
                }
                // 保存回去
                dst.setSamples(0, y, width, 1, band, srcPix);
            }
        }
    }
}
