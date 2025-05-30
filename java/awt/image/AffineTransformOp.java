
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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.AlphaComposite;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.lang.annotation.Native;
import sun.awt.image.ImagingLib;

/**
 * 此类使用仿射变换执行从源图像或 <CODE>Raster</CODE> 的 2D 坐标到目标图像或 <CODE>Raster</CODE> 的 2D 坐标的线性映射。
 * 使用的插值类型通过构造函数指定，可以是 <CODE>RenderingHints</CODE> 对象或本类中定义的整数插值类型之一。
 * <p>
 * 如果在构造函数中指定了 <CODE>RenderingHints</CODE> 对象，则使用插值提示和渲染质量提示来设置此操作的插值类型。如果需要颜色转换，可以使用颜色渲染提示和抖动提示。
 * <p>
 * 注意，必须满足以下约束条件：
 * <ul>
 * <li>源和目标必须不同。
 * <li>对于 <CODE>Raster</CODE> 对象，源中的带数必须等于目标中的带数。
 * </ul>
 * @see AffineTransform
 * @see BufferedImageFilter
 * @see java.awt.RenderingHints#KEY_INTERPOLATION
 * @see java.awt.RenderingHints#KEY_RENDERING
 * @see java.awt.RenderingHints#KEY_COLOR_RENDERING
 * @see java.awt.RenderingHints#KEY_DITHERING
 */
public class AffineTransformOp implements BufferedImageOp, RasterOp {
    private AffineTransform xform;
    RenderingHints hints;

    /**
     * 最近邻插值类型。
     */
    @Native public static final int TYPE_NEAREST_NEIGHBOR = 1;

    /**
     * 双线性插值类型。
     */
    @Native public static final int TYPE_BILINEAR = 2;

    /**
     * 双三次插值类型。
     */
    @Native public static final int TYPE_BICUBIC = 3;

    int interpolationType = TYPE_NEAREST_NEIGHBOR;

    /**
     * 给定一个仿射变换，构造一个 <CODE>AffineTransformOp</CODE>。
     * 插值类型由 <CODE>RenderingHints</CODE> 对象确定。如果定义了插值提示，则将使用该提示。否则，如果定义了渲染质量提示，则根据其值确定插值类型。如果没有指定任何提示（<CODE>hints</CODE> 为 null），则插值类型为 {@link #TYPE_NEAREST_NEIGHBOR TYPE_NEAREST_NEIGHBOR}。
     *
     * @param xform 用于操作的 <CODE>AffineTransform</CODE>。
     *
     * @param hints 用于指定操作插值类型的 <CODE>RenderingHints</CODE> 对象。
     *
     * @throws ImagingOpException 如果变换不可逆。
     * @see java.awt.RenderingHints#KEY_INTERPOLATION
     * @see java.awt.RenderingHints#KEY_RENDERING
     */
    public AffineTransformOp(AffineTransform xform, RenderingHints hints){
        validateTransform(xform);
        this.xform = (AffineTransform) xform.clone();
        this.hints = hints;

        if (hints != null) {
            Object value = hints.get(hints.KEY_INTERPOLATION);
            if (value == null) {
                value = hints.get(hints.KEY_RENDERING);
                if (value == hints.VALUE_RENDER_SPEED) {
                    interpolationType = TYPE_NEAREST_NEIGHBOR;
                }
                else if (value == hints.VALUE_RENDER_QUALITY) {
                    interpolationType = TYPE_BILINEAR;
                }
            }
            else if (value == hints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
                interpolationType = TYPE_NEAREST_NEIGHBOR;
            }
            else if (value == hints.VALUE_INTERPOLATION_BILINEAR) {
                interpolationType = TYPE_BILINEAR;
            }
            else if (value == hints.VALUE_INTERPOLATION_BICUBIC) {
                interpolationType = TYPE_BICUBIC;
            }
        }
        else {
            interpolationType = TYPE_NEAREST_NEIGHBOR;
        }
    }

    /**
     * 给定一个仿射变换和插值类型，构造一个 <CODE>AffineTransformOp</CODE>。
     *
     * @param xform 用于操作的 <CODE>AffineTransform</CODE>。
     * @param interpolationType 本类定义的整数插值类型常量之一：
     * {@link #TYPE_NEAREST_NEIGHBOR TYPE_NEAREST_NEIGHBOR}，
     * {@link #TYPE_BILINEAR TYPE_BILINEAR}，
     * {@link #TYPE_BICUBIC TYPE_BICUBIC}。
     * @throws ImagingOpException 如果变换不可逆。
     */
    public AffineTransformOp(AffineTransform xform, int interpolationType) {
        validateTransform(xform);
        this.xform = (AffineTransform)xform.clone();
        switch(interpolationType) {
            case TYPE_NEAREST_NEIGHBOR:
            case TYPE_BILINEAR:
            case TYPE_BICUBIC:
                break;
        default:
            throw new IllegalArgumentException("未知的插值类型: "+
                                               interpolationType);
        }
        this.interpolationType = interpolationType;
    }

    /**
     * 返回此操作使用的插值类型。
     * @return 插值类型。
     * @see #TYPE_NEAREST_NEIGHBOR
     * @see #TYPE_BILINEAR
     * @see #TYPE_BICUBIC
     */
    public final int getInterpolationType() {
        return interpolationType;
    }

    /**
     * 转换源 <CODE>BufferedImage</CODE> 并将结果存储在目标 <CODE>BufferedImage</CODE> 中。
     * 如果两个图像的颜色模型不匹配，则会进行颜色转换到目标颜色模型。
     * 如果目标图像为 null，则会创建一个具有源 <CODE>ColorModel</CODE> 的 <CODE>BufferedImage</CODE>。
     * <p>
     * 由 <code>getBounds2D(BufferedImage)</code> 返回的矩形的坐标不一定与由此方法返回的 <code>BufferedImage</code> 的坐标相同。如果矩形的左上角坐标为负，则该部分矩形不会被绘制。如果矩形的左上角坐标为正，则过滤后的图像将在目标 <code>BufferedImage</code> 的该位置绘制。
     * <p>
     * 如果源与目标相同，则抛出 <CODE>IllegalArgumentException</CODE>。
     *
     * @param src 要转换的 <CODE>BufferedImage</CODE>。
     * @param dst 存储转换结果的 <CODE>BufferedImage</CODE>。
     *
     * @return 过滤后的 <CODE>BufferedImage</CODE>。
     * @throws IllegalArgumentException 如果 <code>src</code> 和 <code>dst</code> 相同
     * @throws ImagingOpException 如果由于无效的图像格式、瓦片格式或图像处理操作等数据处理错误导致图像无法转换，或任何其他不受支持的操作。
     */
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {

        if (src == null) {
            throw new NullPointerException("源图像为 null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("源图像不能与目标图像相同");
        }

        boolean needToConvert = false;
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM;
        BufferedImage origDst = dst;

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
                int type = xform.getType();
                boolean needTrans = ((type&
                                      (xform.TYPE_MASK_ROTATION|
                                       xform.TYPE_GENERAL_TRANSFORM))
                                     != 0);
                if (! needTrans && type != xform.TYPE_TRANSLATION && type != xform.TYPE_IDENTITY)
                {
                    double[] mtx = new double[4];
                    xform.getMatrix(mtx);
                    // 检查矩阵。非整数缩放将强制使用 ARGB，因为边缘条件无法保证。
                    needTrans = (mtx[0] != (int)mtx[0] || mtx[3] != (int)mtx[3]);
                }

                if (needTrans &&
                    srcCM.getTransparency() == Transparency.OPAQUE)
                {
                    // 需要先转换
                    ColorConvertOp ccop = new ColorConvertOp(hints);
                    BufferedImage tmpSrc = null;
                    int sw = src.getWidth();
                    int sh = src.getHeight();
                    if (dstCM.getTransparency() == Transparency.OPAQUE) {
                        tmpSrc = new BufferedImage(sw, sh,
                                                  BufferedImage.TYPE_INT_ARGB);
                    }
                    else {
                        WritableRaster r =
                            dstCM.createCompatibleWritableRaster(sw, sh);
                        tmpSrc = new BufferedImage(dstCM, r,
                                                  dstCM.isAlphaPremultiplied(),
                                                  null);
                    }
                    src = ccop.filter(src, tmpSrc);
                }
                else {
                    needToConvert = true;
                    dst = createCompatibleDestImage(src, null);
                }
            }

        }

        if (interpolationType != TYPE_NEAREST_NEIGHBOR &&
            dst.getColorModel() instanceof IndexColorModel) {
            dst = new BufferedImage(dst.getWidth(), dst.getHeight(),
                                    BufferedImage.TYPE_INT_ARGB);
        }
        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException ("无法转换源图像");
        }

        if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(hints);
            ccop.filter(dst, origDst);
        }
        else if (origDst != dst) {
            java.awt.Graphics2D g = origDst.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(dst, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        return origDst;
    }

    /**
     * 转换源 <CODE>Raster</CODE> 并将结果存储在目标 <CODE>Raster</CODE> 中。此操作逐带进行转换。
     * <p>
     * 如果目标 <CODE>Raster</CODE> 为 null，则会创建一个新的 <CODE>Raster</CODE>。
     * 如果源与目标相同或源中的带数不等于目标中的带数，则可能抛出 <CODE>IllegalArgumentException</CODE>。
     * <p>
     * 由 <code>getBounds2D(Raster)</code> 返回的矩形的坐标不一定与由此方法返回的 <code>WritableRaster</code> 的坐标相同。如果矩形的左上角坐标为负，则该部分矩形不会被绘制。如果矩形的坐标为正，则过滤后的图像将在目标 <code>Raster</code> 的该位置绘制。
     * <p>
     * @param src 要转换的 <CODE>Raster</CODE>。
     * @param dst 存储转换结果的 <CODE>Raster</CODE>。
     *
     * @return 转换后的 <CODE>Raster</CODE>。
     *
     * @throws ImagingOpException 如果由于无效的图像格式、瓦片格式或图像处理操作等数据处理错误导致图像无法转换，或任何其他不受支持的操作。
     */
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (src == null) {
            throw new NullPointerException("源图像为 null");
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        }
        if (src == dst) {
            throw new IllegalArgumentException("源图像不能与目标图像相同");
        }
        if (src.getNumBands() != dst.getNumBands()) {
            throw new IllegalArgumentException("源带数 ("+
                                               src.getNumBands()+
                                               ") 与目标带数 ("+
                                               dst.getNumBands()+") 不匹配");
        }

        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException ("无法转换源图像");
        }
        return dst;
    }

    /**
     * 返回转换后目标的边界框。返回的矩形是转换点的实际边界框。返回的矩形的左上角坐标可能不是 (0,&nbsp;0)。
     *
     * @param src 要转换的 <CODE>BufferedImage</CODE>。
     *
     * @return 表示目标边界框的 <CODE>Rectangle2D</CODE>。
     */
    public final Rectangle2D getBounds2D (BufferedImage src) {
        return getBounds2D(src.getRaster());
    }


                /**
     * 返回变换后的目标的边界框。返回的矩形将是变换点的实际边界框。返回的矩形的左上角坐标可能不是 (0,&nbsp;0)。
     *
     * @param src 要变换的 <CODE>Raster</CODE>。
     *
     * @return 表示目标边界框的 <CODE>Rectangle2D</CODE>。
     */
    public final Rectangle2D getBounds2D (Raster src) {
        int w = src.getWidth();
        int h = src.getHeight();

        // 获取 src 的边界框并变换角点
        float[] pts = {0, 0, w, 0, w, h, 0, h};
        xform.transform(pts, 0, pts, 0, 4);

        // 获取 dst 的最小值和最大值
        float fmaxX = pts[0];
        float fmaxY = pts[1];
        float fminX = pts[0];
        float fminY = pts[1];
        for (int i=2; i < 8; i+=2) {
            if (pts[i] > fmaxX) {
                fmaxX = pts[i];
            }
            else if (pts[i] < fminX) {
                fminX = pts[i];
            }
            if (pts[i+1] > fmaxY) {
                fmaxY = pts[i+1];
            }
            else if (pts[i+1] < fminY) {
                fminY = pts[i+1];
            }
        }

        return new Rectangle2D.Float(fminX, fminY, fmaxX-fminX, fmaxY-fminY);
    }

    /**
     * 创建一个具有正确大小和带数的零化目标图像。如果变换后的宽度或高度等于 0，可能会抛出 <CODE>RasterFormatException</CODE>。
     * <p>
     * 如果 <CODE>destCM</CODE> 为 null，则使用适当的 <CODE>ColorModel</CODE>；此 <CODE>ColorModel</CODE> 可能具有 alpha 通道，即使源 <CODE>ColorModel</CODE> 是不透明的。
     *
     * @param src  要变换的 <CODE>BufferedImage</CODE>。
     * @param destCM  目标的 <CODE>ColorModel</CODE>。如果为 null，则使用适当的 <CODE>ColorModel</CODE>。
     *
     * @return 零化的目标图像。
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM) {
        BufferedImage image;
        Rectangle r = getBounds2D(src).getBounds();

        // 如果 r.x (或 r.y) < 0，则我们只想创建一个在正范围内的图像。
        // 如果 r.x (或 r.y) > 0，则我们需要创建一个包括平移的图像。
        int w = r.x + r.width;
        int h = r.y + r.height;
        if (w <= 0) {
            throw new RasterFormatException("变换后的宽度 ("+w+
                                            ") 小于或等于 0。");
        }
        if (h <= 0) {
            throw new RasterFormatException("变换后的高度 ("+h+
                                            ") 小于或等于 0。");
        }

        if (destCM == null) {
            ColorModel cm = src.getColorModel();
            if (interpolationType != TYPE_NEAREST_NEIGHBOR &&
                (cm instanceof IndexColorModel ||
                 cm.getTransparency() == Transparency.OPAQUE))
            {
                image = new BufferedImage(w, h,
                                          BufferedImage.TYPE_INT_ARGB);
            }
            else {
                image = new BufferedImage(cm,
                          src.getRaster().createCompatibleWritableRaster(w,h),
                          cm.isAlphaPremultiplied(), null);
            }
        }
        else {
            image = new BufferedImage(destCM,
                                    destCM.createCompatibleWritableRaster(w,h),
                                    destCM.isAlphaPremultiplied(), null);
        }

        return image;
    }

    /**
     * 创建一个具有正确大小和带数的零化目标 <CODE>Raster</CODE>。如果变换后的宽度或高度等于 0，可能会抛出 <CODE>RasterFormatException</CODE>。
     *
     * @param src 要变换的 <CODE>Raster</CODE>。
     *
     * @return 零化的目标 <CODE>Raster</CODE>。
     */
    public WritableRaster createCompatibleDestRaster (Raster src) {
        Rectangle2D r = getBounds2D(src);

        return src.createCompatibleWritableRaster((int)r.getX(),
                                                  (int)r.getY(),
                                                  (int)r.getWidth(),
                                                  (int)r.getHeight());
    }

    /**
     * 返回给定源点对应的目标点的位置。如果指定了 <CODE>dstPt</CODE>，则使用它来存储返回值。
     *
     * @param srcPt 代表源点的 <code>Point2D</code>。
     * @param dstPt 用于存储结果的 <CODE>Point2D</CODE>。
     *
     * @return 与源中指定点对应的目标中的 <CODE>Point2D</CODE>。
     */
    public final Point2D getPoint2D (Point2D srcPt, Point2D dstPt) {
        return xform.transform (srcPt, dstPt);
    }

    /**
     * 返回此变换操作使用的仿射变换。
     *
     * @return 与此操作关联的 <CODE>AffineTransform</CODE>。
     */
    public final AffineTransform getTransform() {
        return (AffineTransform) xform.clone();
    }

    /**
     * 返回此变换操作使用的渲染提示。
     *
     * @return 与此操作关联的 <CODE>RenderingHints</CODE> 对象。
     */
    public final RenderingHints getRenderingHints() {
        if (hints == null) {
            Object val;
            switch(interpolationType) {
            case TYPE_NEAREST_NEIGHBOR:
                val = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
            case TYPE_BILINEAR:
                val = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                break;
            case TYPE_BICUBIC:
                val = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                break;
            default:
                // 不应到达此处
                throw new InternalError("未知插值类型 "+
                                         interpolationType);

            }
            hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, val);
        }

        return hints;
    }

    // 如果我们想要变换图像，我们需要能够反转变换。如果矩阵的行列式为 0，则无法反转变换。
    void validateTransform(AffineTransform xform) {
        if (Math.abs(xform.getDeterminant()) <= Double.MIN_VALUE) {
            throw new ImagingOpException("无法反转变换 "+xform);
        }
    }
}
