
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
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import sun.awt.image.ImagingLib;

/**
 * 该类对源图像中的数据进行逐像素的重新缩放，通过将每个像素的样本值乘以一个缩放因子，然后加上一个偏移量。缩放后的样本值会被裁剪到目的地图像中可表示的最小/最大值。
 * <p>
 * 重新缩放操作的伪代码如下：
 * <pre>
 *for each pixel from Source object {
 *    for each band/component of the pixel {
 *        dstElement = (srcElement*scaleFactor) + offset
 *    }
 *}
 * </pre>
 * <p>
 * 对于 Raster，重新缩放操作在带上进行。缩放常量集的数量可以是一个，这种情况下所有带都使用相同的常量，或者它必须等于源 Raster 带的数量。
 * <p>
 * 对于 BufferedImage，重新缩放操作在颜色和 alpha 组件上进行。缩放常量集的数量可以是一个，这种情况下所有颜色（但不是 alpha）组件都使用相同的常量。
 * 否则，缩放常量集的数量可以等于源颜色组件的数量，这种情况下不重新缩放 alpha 组件（如果存在）。如果以上两种情况都不适用，缩放常量集的数量必须等于源颜色组件和 alpha 组件的数量，这种情况下所有颜色和 alpha 组件都会被重新缩放。
 * <p>
 * 带有预乘 alpha 数据的 BufferedImage 在重新缩放时与非预乘图像处理方式相同。也就是说，重新缩放在每个带上对 BufferedImage 源的原始数据进行，而不考虑数据是否预乘。如果需要将颜色转换为目标 ColorModel，源和目标的预乘状态将在此步骤中被考虑。
 * <p>
 * 带有 IndexColorModel 的图像不能被重新缩放。
 * <p>
 * 如果在构造函数中指定了 RenderingHints 对象，当需要颜色转换时，颜色渲染提示和抖动提示可能会被使用。
 * <p>
 * 注意，允许就地操作（即源和目标可以是同一个对象）。
 * @see java.awt.RenderingHints#KEY_COLOR_RENDERING
 * @see java.awt.RenderingHints#KEY_DITHERING
 */
public class RescaleOp implements BufferedImageOp, RasterOp {
    float[] scaleFactors;
    float[] offsets;
    int length = 0;
    RenderingHints hints;

    private int srcNbits;
    private int dstNbits;


    /**
     * 构造一个新的 RescaleOp，具有所需的缩放因子和偏移量。缩放因子和偏移数组的长度必须满足上述类注释中的限制。RenderingHints 参数可以为 null。
     * @param scaleFactors 指定的缩放因子
     * @param offsets 指定的偏移量
     * @param hints 指定的 <code>RenderingHints</code>，或 <code>null</code>
     */
    public RescaleOp (float[] scaleFactors, float[] offsets,
                      RenderingHints hints) {
        length = scaleFactors.length;
        if (length > offsets.length) length = offsets.length;

        this.scaleFactors = new float[length];
        this.offsets      = new float[length];
        for (int i=0; i < length; i++) {
            this.scaleFactors[i] = scaleFactors[i];
            this.offsets[i]      = offsets[i];
        }
        this.hints = hints;
    }

    /**
     * 构造一个新的 RescaleOp，具有所需的缩放因子和偏移量。缩放因子和偏移量将应用于源 Raster 的所有带和 BufferedImage 的所有颜色（但不是 alpha）组件。
     * RenderingHints 参数可以为 null。
     * @param scaleFactor 指定的缩放因子
     * @param offset 指定的偏移量
     * @param hints 指定的 <code>RenderingHints</code>，或 <code>null</code>
     */
    public RescaleOp (float scaleFactor, float offset, RenderingHints hints) {
        length = 1;
        this.scaleFactors = new float[1];
        this.offsets      = new float[1];
        this.scaleFactors[0] = scaleFactor;
        this.offsets[0]       = offset;
        this.hints = hints;
    }

    /**
     * 返回给定数组中的缩放因子。为了方便，也返回该数组。如果 scaleFactors 为 null，将分配一个新的数组。
     * @param scaleFactors 用于包含此 <code>RescaleOp</code> 缩放因子的数组
     * @return 此 <code>RescaleOp</code> 的缩放因子。
     */
    final public float[] getScaleFactors (float scaleFactors[]) {
        if (scaleFactors == null) {
            return (float[]) this.scaleFactors.clone();
        }
        System.arraycopy (this.scaleFactors, 0, scaleFactors, 0,
                          Math.min(this.scaleFactors.length,
                                   scaleFactors.length));
        return scaleFactors;
    }

    /**
     * 返回给定数组中的偏移量。为了方便，也返回该数组。如果 offsets 为 null，将分配一个新的数组。
     * @param offsets 用于包含此 <code>RescaleOp</code> 偏移量的数组
     * @return 此 <code>RescaleOp</code> 的偏移量。
     */
    final public float[] getOffsets(float offsets[]) {
        if (offsets == null) {
            return (float[]) this.offsets.clone();
        }

        System.arraycopy (this.offsets, 0, offsets, 0,
                          Math.min(this.offsets.length, offsets.length));
        return offsets;
    }

    /**
     * 返回此 RescaleOp 使用的缩放因子和偏移量的数量。
     * @return 此 <code>RescaleOp</code> 的缩放因子和偏移量的数量。
     */
    final public int getNumFactors() {
        return length;
    }


    /**
     * 创建一个 ByteLookupTable 以实现重新缩放。该表可以具有 SHORT 或 BYTE 输入。
     * @param nElems 表中要具有的元素数量。这通常为 256（对于 byte）和 65536（对于 short）。
     */
    private ByteLookupTable createByteLut(float scale[],
                                          float off[],
                                          int   nBands,
                                          int   nElems) {

        byte[][]        lutData = new byte[nBands][nElems];
        int band;

        for (band=0; band<scale.length; band++) {
            float  bandScale   = scale[band];
            float  bandOff     = off[band];
            byte[] bandLutData = lutData[band];
            for (int i=0; i<nElems; i++) {
                int val = (int)(i*bandScale + bandOff);
                if ((val & 0xffffff00) != 0) {
                    if (val < 0) {
                        val = 0;
                    } else {
                        val = 255;
                    }
                }
                bandLutData[i] = (byte)val;
            }

        }
        int maxToCopy = (nBands == 4 && scale.length == 4) ? 4 : 3;
        while (band < lutData.length && band < maxToCopy) {
           System.arraycopy(lutData[band-1], 0, lutData[band], 0, nElems);
           band++;
        }
        if (nBands == 4 && band < nBands) {
           byte[] bandLutData = lutData[band];
           for (int i=0; i<nElems; i++) {
              bandLutData[i] = (byte)i;
           }
        }

        return new ByteLookupTable(0, lutData);
    }

    /**
     * 创建一个 ShortLookupTable 以实现重新缩放。该表可以具有 SHORT 或 BYTE 输入。
     * @param nElems 表中要具有的元素数量。这通常为 256（对于 byte）和 65536（对于 short）。
     */
    private ShortLookupTable createShortLut(float scale[],
                                            float off[],
                                            int   nBands,
                                            int   nElems) {

        short[][]        lutData = new short[nBands][nElems];
        int band = 0;

        for (band=0; band<scale.length; band++) {
            float   bandScale   = scale[band];
            float   bandOff     = off[band];
            short[] bandLutData = lutData[band];
            for (int i=0; i<nElems; i++) {
                int val = (int)(i*bandScale + bandOff);
                if ((val & 0xffff0000) != 0) {
                    if (val < 0) {
                        val = 0;
                    } else {
                        val = 65535;
                    }
                }
                bandLutData[i] = (short)val;
            }
        }
        int maxToCopy = (nBands == 4 && scale.length == 4) ? 4 : 3;
        while (band < lutData.length && band < maxToCopy) {
           System.arraycopy(lutData[band-1], 0, lutData[band], 0, nElems);
           band++;
        }
        if (nBands == 4 && band < nBands) {
           short[] bandLutData = lutData[band];
           for (int i=0; i<nElems; i++) {
              bandLutData[i] = (short)i;
           }
        }

        return new ShortLookupTable(0, lutData);
    }


    /**
     * 确定重新缩放是否可以作为查找表执行。目标必须是 byte 或 short 类型。源必须小于 16 位。所有源带大小必须相同，所有目标带大小也必须相同。
     */
    private boolean canUseLookup(Raster src, Raster dst) {

        //
        // 检查源数据类型是否为 BYTE 或 SHORT
        //
        int datatype = src.getDataBuffer().getDataType();
        if(datatype != DataBuffer.TYPE_BYTE &&
           datatype != DataBuffer.TYPE_USHORT) {
            return false;
        }

        //
        // 检查目标样本大小。所有样本大小必须为 8 或 16 位。
        //
        SampleModel dstSM = dst.getSampleModel();
        dstNbits = dstSM.getSampleSize(0);

        if (!(dstNbits == 8 || dstNbits == 16)) {
            return false;
        }
        for (int i=1; i<src.getNumBands(); i++) {
            int bandSize = dstSM.getSampleSize(i);
            if (bandSize != dstNbits) {
                return false;
            }
        }

        //
        // 检查源样本大小。所有样本大小必须相同
        //
        SampleModel srcSM = src.getSampleModel();
        srcNbits = srcSM.getSampleSize(0);
        if (srcNbits > 16) {
            return false;
        }
        for (int i=1; i<src.getNumBands(); i++) {
            int bandSize = srcSM.getSampleSize(i);
            if (bandSize != srcNbits) {
                return false;
            }
        }

      if (dstSM instanceof ComponentSampleModel) {
           ComponentSampleModel dsm = (ComponentSampleModel)dstSM;
           if (dsm.getPixelStride() != dst.getNumBands()) {
               return false;
           }
        }
        if (srcSM instanceof ComponentSampleModel) {
           ComponentSampleModel csm = (ComponentSampleModel)srcSM;
           if (csm.getPixelStride() != src.getNumBands()) {
               return false;
           }
        }

        return true;
    }

    /**
     * 重新缩放源 BufferedImage。如果源图像的颜色模型与目标图像的颜色模型不同，像素将在目标中进行转换。如果目标图像为 null，将创建一个具有源 ColorModel 的 BufferedImage。
     * 如果此对象中的缩放因子/偏移量数量不符合上述类注释中的限制，或者源图像具有 IndexColorModel，则可能抛出 IllegalArgumentException。
     * @param src 要过滤的 <code>BufferedImage</code>
     * @param dst 过滤操作的目标或 <code>null</code>
     * @return 过滤后的 <code>BufferedImage</code>。
     * @throws IllegalArgumentException 如果 <code>src</code> 的 <code>ColorModel</code> 是 <code>IndexColorModel</code>，
     *         或者此 <code>RescaleOp</code> 中的缩放因子和偏移量数量不符合类注释中所述的要求。
     */
    public final BufferedImage filter (BufferedImage src, BufferedImage dst) {
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM;
        int numSrcColorComp = srcCM.getNumColorComponents();
        int scaleConst = length;

        if (srcCM instanceof IndexColorModel) {
            throw new
                IllegalArgumentException("不能对索引图像进行重新缩放");
        }
        if (scaleConst != 1 && scaleConst != numSrcColorComp &&
            scaleConst != srcCM.getNumComponents())
        {
            throw new IllegalArgumentException("缩放常量的数量不等于颜色或颜色/alpha 组件的数量");
        }

        boolean needToConvert = false;
        boolean needToDraw = false;

        // 包括 alpha
        if (scaleConst > numSrcColorComp && srcCM.hasAlpha()) {
            scaleConst = numSrcColorComp+1;
        }

        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage origDst = dst;
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
            dstCM = srcCM;
        }
        else {
            if (width != dst.getWidth()) {
                throw new
                    IllegalArgumentException("源宽度 ("+width+
                                             ") 不等于目标宽度 ("+
                                             dst.getWidth()+")");
            }
            if (height != dst.getHeight()) {
                throw new
                    IllegalArgumentException("源高度 ("+height+
                                             ") 不等于目标高度 ("+
                                             dst.getHeight()+")");
            }


                        dstCM = dst.getColorModel();
            if(srcCM.getColorSpace().getType() !=
                 dstCM.getColorSpace().getType()) {
                needToConvert = true;
                dst = createCompatibleDestImage(src, null);
            }

        }

        //
        // 尝试首先使用本机 BI 重缩放操作
        //
        if (ImagingLib.filter(this, src, dst) == null) {
            if (src.getRaster().getNumBands() !=
                dst.getRaster().getNumBands()) {
                needToDraw = true;
                dst = createCompatibleDestImage(src, null);
            }

            //
            // 本机 BI 重缩放失败 - 转换为栅格
            //
            WritableRaster srcRaster = src.getRaster();
            WritableRaster dstRaster = dst.getRaster();

            //
            // 调用栅格过滤方法
            //
            filterRasterImpl(srcRaster, dstRaster, scaleConst, false);
        }

        if (needToDraw) {
             Graphics2D g = origDst.createGraphics();
             g.setComposite(AlphaComposite.Src);
             g.drawImage(dst, 0, 0, width, height, null);
             g.dispose();
        }
        if (needToConvert) {
            // 颜色模型不相同
            ColorConvertOp ccop = new ColorConvertOp(hints);
            dst = ccop.filter(dst, origDst);
        }
        return dst;
    }

    /**
     * 重缩放源栅格中的像素数据。
     * 如果目标栅格为 null，将创建一个新的栅格。
     * 源和目标必须具有相同数量的波段。
     * 否则，将抛出 IllegalArgumentException。
     * 注意，此对象中的缩放因子/偏移量的数量必须
     * 满足上述类注释中所述的限制。
     * 否则，将抛出 IllegalArgumentException。
     * @param src 要过滤的 <code>Raster</code>
     * @param dst 过滤操作的目标
     *            或 <code>null</code>
     * @return 过滤后的 <code>WritableRaster</code>。
     * @throws IllegalArgumentException 如果 <code>src</code> 和
     *         <code>dst</code> 没有相同数量的波段，
     *         或此 <code>RescaleOp</code> 中的缩放因子和偏移量的数量
     *         不满足上述类注释中所述的要求。
     */
    public final WritableRaster filter (Raster src, WritableRaster dst)  {
        return filterRasterImpl(src, dst, length, true);
    }

    private WritableRaster filterRasterImpl(Raster src, WritableRaster dst,
                                            int scaleConst, boolean sCheck) {
        int numBands = src.getNumBands();
        int width  = src.getWidth();
        int height = src.getHeight();
        int[] srcPix = null;
        int step = 0;
        int tidx = 0;

        // 如果需要，创建一个新的目标栅格
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        }
        else if (height != dst.getHeight() || width != dst.getWidth()) {
            throw new
               IllegalArgumentException("栅格的宽度或高度不匹配");
        }
        else if (numBands != dst.getNumBands()) {
            // 确保波段数量相等
            throw new IllegalArgumentException("源中的波段数量 "
                            + numBands
                            + " 不等于目标中的波段数量 "
                            + dst.getNumBands());
        }

        // 确保数组匹配
        // 确保低/高/常量数组匹配
        if (sCheck && scaleConst != 1 && scaleConst != src.getNumBands()) {
            throw new IllegalArgumentException("缩放常量的数量 "
                                               "不等于源栅格中的波段数量");
        }

        //
        // 尝试首先使用本机栅格重缩放
        //
        if (ImagingLib.filter(this, src, dst) != null) {
            return dst;
        }

        //
        // 本机栅格重缩放失败。
        // 尝试查看是否可以使用查找操作
        //
        if (canUseLookup(src, dst)) {
            int srcNgray = (1 << srcNbits);
            int dstNgray = (1 << dstNbits);

            if (dstNgray == 256) {
                ByteLookupTable lut = createByteLut(scaleFactors, offsets,
                                                    numBands, srcNgray);
                LookupOp op = new LookupOp(lut, hints);
                op.filter(src, dst);
            } else {
                ShortLookupTable lut = createShortLut(scaleFactors, offsets,
                                                      numBands, srcNgray);
                LookupOp op = new LookupOp(lut, hints);
                op.filter(src, dst);
            }
        } else {
            //
            // 回退到慢速代码
            //
            if (scaleConst > 1) {
                step = 1;
            }

            int sminX = src.getMinX();
            int sY = src.getMinY();
            int dminX = dst.getMinX();
            int dY = dst.getMinY();
            int sX;
            int dX;

            //
            // 确定每个波段的位数以确定 clamps 的最大值。
            // 假设最小值为零。
            // 提醒：如果支持有符号数据类型，这必须更改。
            //
            int nbits;
            int dstMax[] = new int[numBands];
            int dstMask[] = new int[numBands];
            SampleModel dstSM = dst.getSampleModel();
            for (int z=0; z<numBands; z++) {
                nbits = dstSM.getSampleSize(z);
                dstMax[z] = (1 << nbits) - 1;
                dstMask[z] = ~(dstMax[z]);
            }

            int val;
            for (int y=0; y < height; y++, sY++, dY++) {
                dX = dminX;
                sX = sminX;
                for (int x = 0; x < width; x++, sX++, dX++) {
                    // 获取此 x,y 位置的所有波段的数据
                    srcPix = src.getPixel(sX, sY, srcPix);
                    tidx = 0;
                    for (int z=0; z<numBands; z++, tidx += step) {
                        if ((scaleConst == 1 || scaleConst == 3) &&
                            (z == 3) && (numBands == 4)) {
                           val = srcPix[z];
                        } else {
                            val = (int)(srcPix[z]*scaleFactors[tidx]
                                              + offsets[tidx]);

                        }
                        // 钳位
                        if ((val & dstMask[z]) != 0) {
                            if (val < 0) {
                                val = 0;
                            } else {
                                val = dstMax[z];
                            }
                        }
                        srcPix[z] = val;

                    }

                    // 将其放回所有波段
                    dst.setPixel(dX, dY, srcPix);
                }
            }
        }
        return dst;
    }

    /**
     * 返回重缩放目标图像的边界框。由于
     * 这不是几何操作，边界框不会改变。
     */
    public final Rectangle2D getBounds2D (BufferedImage src) {
         return getBounds2D(src.getRaster());
    }

    /**
     * 返回重缩放目标栅格的边界框。由于
     * 这不是几何操作，边界框不会改变。
     * @param src 重缩放目标 <code>Raster</code>
     * @return 指定 <code>Raster</code> 的边界。
     */
    public final Rectangle2D getBounds2D (Raster src) {
        return src.getBounds();
    }

    /**
     * 创建具有正确大小和波段数量的零化目标图像。
     * @param src       过滤操作的源图像。
     * @param destCM    目标的颜色模型。如果为 null，将使用源的颜色模型。
     * @return 零化目标图像。
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM) {
        BufferedImage image;
        if (destCM == null) {
            ColorModel cm = src.getColorModel();
            image = new BufferedImage(cm,
                                      src.getRaster().createCompatibleWritableRaster(),
                                      cm.isAlphaPremultiplied(),
                                      null);
        }
        else {
            int w = src.getWidth();
            int h = src.getHeight();
            image = new BufferedImage (destCM,
                                   destCM.createCompatibleWritableRaster(w, h),
                                   destCM.isAlphaPremultiplied(), null);
        }

        return image;
    }

    /**
     * 创建具有正确大小和波段数量的零化目标 <code>Raster</code>。
     * @param src       源 <code>Raster</code>
     * @return 零化目标 <code>Raster</code>。
     */
    public WritableRaster createCompatibleDestRaster (Raster src) {
        return src.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
    }

    /**
     * 返回给定源点的目标点的位置。如果 dstPt 不为 null，它将
     * 用于保存返回值。由于这不是几何操作，srcPt 将等于 dstPt。
     * @param srcPt 源图像中的一个点
     * @param dstPt 目标点或 <code>null</code>
     * @return 目标点的位置。
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
     * @return 此 <code>RescaleOp</code> 的渲染提示。
     */
    public final RenderingHints getRenderingHints() {
        return hints;
    }
}
