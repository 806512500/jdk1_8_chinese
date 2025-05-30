
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

package java.awt;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.ref.WeakReference;
import sun.awt.image.SunWritableRaster;
import sun.awt.image.IntegerInterleavedRaster;
import sun.awt.image.ByteInterleavedRaster;

abstract class TexturePaintContext implements PaintContext {
    public static ColorModel xrgbmodel =
        new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
    public static ColorModel argbmodel = ColorModel.getRGBdefault();

    ColorModel colorModel;
    int bWidth;
    int bHeight;
    int maxWidth;

    WritableRaster outRas;

    double xOrg;
    double yOrg;
    double incXAcross;
    double incYAcross;
    double incXDown;
    double incYDown;

    int colincx;
    int colincy;
    int colincxerr;
    int colincyerr;
    int rowincx;
    int rowincy;
    int rowincxerr;
    int rowincyerr;

    public static PaintContext getContext(BufferedImage bufImg,
                                          AffineTransform xform,
                                          RenderingHints hints,
                                          Rectangle devBounds) {
        WritableRaster raster = bufImg.getRaster();
        ColorModel cm = bufImg.getColorModel();
        int maxw = devBounds.width;
        Object val = hints.get(RenderingHints.KEY_INTERPOLATION);
        boolean filter =
            (val == null
             ? (hints.get(RenderingHints.KEY_RENDERING) == RenderingHints.VALUE_RENDER_QUALITY)
             : (val != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
        if (raster instanceof IntegerInterleavedRaster &&
            (!filter || isFilterableDCM(cm)))
        {
            IntegerInterleavedRaster iir = (IntegerInterleavedRaster) raster;
            if (iir.getNumDataElements() == 1 && iir.getPixelStride() == 1) {
                return new Int(iir, cm, xform, maxw, filter);
            }
        } else if (raster instanceof ByteInterleavedRaster) {
            ByteInterleavedRaster bir = (ByteInterleavedRaster) raster;
            if (bir.getNumDataElements() == 1 && bir.getPixelStride() == 1) {
                if (filter) {
                    if (isFilterableICM(cm)) {
                        return new ByteFilter(bir, cm, xform, maxw);
                    }
                } else {
                    return new Byte(bir, cm, xform, maxw);
                }
            }
        }
        return new Any(raster, cm, xform, maxw, filter);
    }

    public static boolean isFilterableICM(ColorModel cm) {
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;
            if (icm.getMapSize() <= 256) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFilterableDCM(ColorModel cm) {
        if (cm instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel) cm;
            return (isMaskOK(dcm.getAlphaMask(), true) &&
                    isMaskOK(dcm.getRedMask(), false) &&
                    isMaskOK(dcm.getGreenMask(), false) &&
                    isMaskOK(dcm.getBlueMask(), false));
        }
        return false;
    }

    public static boolean isMaskOK(int mask, boolean canbezero) {
        if (canbezero && mask == 0) {
            return true;
        }
        return (mask == 0xff ||
                mask == 0xff00 ||
                mask == 0xff0000 ||
                mask == 0xff000000);
    }

    public static ColorModel getInternedColorModel(ColorModel cm) {
        if (xrgbmodel == cm || xrgbmodel.equals(cm)) {
            return xrgbmodel;
        }
        if (argbmodel == cm || argbmodel.equals(cm)) {
            return argbmodel;
        }
        return cm;
    }

    TexturePaintContext(ColorModel cm, AffineTransform xform,
                        int bWidth, int bHeight, int maxw) {
        this.colorModel = getInternedColorModel(cm);
        this.bWidth = bWidth;
        this.bHeight = bHeight;
        this.maxWidth = maxw;

        try {
            xform = xform.createInverse();
        } catch (NoninvertibleTransformException e) {
            xform.setToScale(0, 0);
        }
        this.incXAcross = mod(xform.getScaleX(), bWidth);
        this.incYAcross = mod(xform.getShearY(), bHeight);
        this.incXDown = mod(xform.getShearX(), bWidth);
        this.incYDown = mod(xform.getScaleY(), bHeight);
        this.xOrg = xform.getTranslateX();
        this.yOrg = xform.getTranslateY();
        this.colincx = (int) incXAcross;
        this.colincy = (int) incYAcross;
        this.colincxerr = fractAsInt(incXAcross);
        this.colincyerr = fractAsInt(incYAcross);
        this.rowincx = (int) incXDown;
        this.rowincy = (int) incYDown;
        this.rowincxerr = fractAsInt(incXDown);
        this.rowincyerr = fractAsInt(incYDown);

    }

    static int fractAsInt(double d) {
        return (int) ((d % 1.0) * Integer.MAX_VALUE);
    }

    static double mod(double num, double den) {
        num = num % den;
        if (num < 0) {
            num += den;
            if (num >= den) {
                // 对于非常小的负数，结果可能比 den 小一点点，以至于 double 的尾数部分无法表示这个差异，结果会被四舍五入到 den。如果出现这种情况，我们将这个数映射到 0，作为最近的模数表示。
                num = 0;
            }
        }
        return num;
    }

    /**
     * 释放为操作分配的资源。
     */
    public void dispose() {
        dropRaster(colorModel, outRas);
    }

    /**
     * 返回输出的 ColorModel。
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * 返回一个包含为图形操作生成的颜色的 Raster。
     * @param x,y,w,h 设备空间中生成颜色的区域。
     */
    public Raster getRaster(int x, int y, int w, int h) {
        if (outRas == null ||
            outRas.getWidth() < w ||
            outRas.getHeight() < h)
        {
            // 如果 h==1，我们可能会得到很多“扫描线”矩形
            outRas = makeRaster((h == 1 ? Math.max(w, maxWidth) : w), h);
        }
        double X = mod(xOrg + x * incXAcross + y * incXDown, bWidth);
        double Y = mod(yOrg + x * incYAcross + y * incYDown, bHeight);

        setRaster((int) X, (int) Y, fractAsInt(X), fractAsInt(Y),
                  w, h, bWidth, bHeight,
                  colincx, colincxerr,
                  colincy, colincyerr,
                  rowincx, rowincxerr,
                  rowincy, rowincyerr);

        SunWritableRaster.markDirty(outRas);

        return outRas;
    }

    private static WeakReference<Raster> xrgbRasRef;
    private static WeakReference<Raster> argbRasRef;

    synchronized static WritableRaster makeRaster(ColorModel cm,
                                                  Raster srcRas,
                                                  int w, int h)
    {
        if (xrgbmodel == cm) {
            if (xrgbRasRef != null) {
                WritableRaster wr = (WritableRaster) xrgbRasRef.get();
                if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                    xrgbRasRef = null;
                    return wr;
                }
            }
            // 如果要缓存这个 Raster，使其不那么小
            if (w <= 32 && h <= 32) {
                w = h = 32;
            }
        } else if (argbmodel == cm) {
            if (argbRasRef != null) {
                WritableRaster wr = (WritableRaster) argbRasRef.get();
                if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                    argbRasRef = null;
                    return wr;
                }
            }
            // 如果要缓存这个 Raster，使其不那么小
            if (w <= 32 && h <= 32) {
                w = h = 32;
            }
        }
        if (srcRas != null) {
            return srcRas.createCompatibleWritableRaster(w, h);
        } else {
            return cm.createCompatibleWritableRaster(w, h);
        }
    }

    synchronized static void dropRaster(ColorModel cm, Raster outRas) {
        if (outRas == null) {
            return;
        }
        if (xrgbmodel == cm) {
            xrgbRasRef = new WeakReference<>(outRas);
        } else if (argbmodel == cm) {
            argbRasRef = new WeakReference<>(outRas);
        }
    }

    private static WeakReference<Raster> byteRasRef;

    synchronized static WritableRaster makeByteRaster(Raster srcRas,
                                                      int w, int h)
    {
        if (byteRasRef != null) {
            WritableRaster wr = (WritableRaster) byteRasRef.get();
            if (wr != null && wr.getWidth() >= w && wr.getHeight() >= h) {
                byteRasRef = null;
                return wr;
            }
        }
        // 如果要缓存这个 Raster，使其不那么小
        if (w <= 32 && h <= 32) {
            w = h = 32;
        }
        return srcRas.createCompatibleWritableRaster(w, h);
    }

    synchronized static void dropByteRaster(Raster outRas) {
        if (outRas == null) {
            return;
        }
        byteRasRef = new WeakReference<>(outRas);
    }

    public abstract WritableRaster makeRaster(int w, int h);
    public abstract void setRaster(int x, int y, int xerr, int yerr,
                                   int w, int h, int bWidth, int bHeight,
                                   int colincx, int colincxerr,
                                   int colincy, int colincyerr,
                                   int rowincx, int rowincxerr,
                                   int rowincy, int rowincyerr);

    /*
     * 使用 xmul 和 ymul 描述的因子混合 rgbs 数组中的四个 ARGB 值，比例如下：
     *
     *     rgbs[0] * (1-xmul) * (1-ymul) +
     *     rgbs[1] * (  xmul) * (1-ymul) +
     *     rgbs[2] * (1-xmul) * (  ymul) +
     *     rgbs[3] * (  xmul) * (  ymul)
     *
     * xmul 和 ymul 是半开区间 [0, 2^31) 中的整数值，其中 0 == 0.0 且 2^31 == 1.0。
     *
     * 由于范围是半开的，值总是逻辑上小于 1.0。这在选择要混合的像素时是有意义的，因为当误差值达到 1.0 时，我们移动到下一个像素并将其重置为 0.0。
     */
    public static int blend(int rgbs[], int xmul, int ymul) {
        // xmul/ymul 是 31 位宽，(0 => 2^31-1)
        // 将它们移位到 12 位宽，(0 => 2^12-1)
        xmul = (xmul >>> 19);
        ymul = (ymul >>> 19);
        int accumA, accumR, accumG, accumB;
        accumA = accumR = accumG = accumB = 0;
        for (int i = 0; i < 4; i++) {
            int rgb = rgbs[i];
            // [xy]mul 值的补码 (1-[xy]mul) 可能导致新值在 (1 => 2^12) 范围内。因此，在任何给定的循环迭代中，值可能在 (0 => 2^12) 范围内。
            xmul = (1<<12) - xmul;
            if ((i & 1) == 0) {
                ymul = (1<<12) - ymul;
            }
            // xmul 和 ymul 每个都是 12 位 (0 => 2^12)
            // 因子因此是 24 位 (0 => 2^24)
            int factor = xmul * ymul;
            if (factor != 0) {
                // accum 变量将累积 32 位
                // 从 rgb 提取的字节适合 8 位 (0 => 255)
                // 字节 * 因子因此适合 32 位 (0 => 255 * 2^24)
                accumA += (((rgb >>> 24)       ) * factor);
                accumR += (((rgb >>> 16) & 0xff) * factor);
                accumG += (((rgb >>>  8) & 0xff) * factor);
                accumB += (((rgb       ) & 0xff) * factor);
            }
        }
        return ((((accumA + (1<<23)) >>> 24) << 24) |
                (((accumR + (1<<23)) >>> 24) << 16) |
                (((accumG + (1<<23)) >>> 24) <<  8) |
                (((accumB + (1<<23)) >>> 24)      ));
    }

    static class Int extends TexturePaintContext {
        IntegerInterleavedRaster srcRas;
        int inData[];
        int inOff;
        int inSpan;
        int outData[];
        int outOff;
        int outSpan;
        boolean filter;

        public Int(IntegerInterleavedRaster srcRas, ColorModel cm,
                   AffineTransform xform, int maxw, boolean filter)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
            this.filter = filter;
        }

        public WritableRaster makeRaster(int w, int h) {
            WritableRaster ras = makeRaster(colorModel, srcRas, w, h);
            IntegerInterleavedRaster iiRas = (IntegerInterleavedRaster) ras;
            outData = iiRas.getDataStorage();
            outSpan = iiRas.getScanlineStride();
            outOff = iiRas.getDataOffset(0);
            return ras;
        }

        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            int[] inData = this.inData;
            int[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            boolean filter = this.filter;
            boolean normalx = (colincx == 1 && colincxerr == 0 &&
                               colincy == 0 && colincyerr == 0) && !filter;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            if (normalx) {
                outSpan -= w;
            }
            int rgbs[] = filter ? new int[4] : null;
            for (int j = 0; j < h; j++) {
                if (normalx) {
                    int in = inOff + rowy * inSpan + bWidth;
                    x = bWidth - rowx;
                    out += w;
                    if (bWidth >= 32) {
                        int i = w;
                        while (i > 0) {
                            int copyw = (i < x) ? i : x;
                            System.arraycopy(inData, in - x,
                                             outData, out - i,
                                             copyw);
                            i -= copyw;
                            if ((x -= copyw) == 0) {
                                x = bWidth;
                            }
                        }
                    } else {
                        for (int i = w; i > 0; i--) {
                            outData[out - i] = inData[in - x];
                            if (--x == 0) {
                                x = bWidth;
                            }
                        }
                    }
                } else {
                    x = rowx;
                    y = rowy;
                    xerr = rowxerr;
                    yerr = rowyerr;
                    for (int i = 0; i < w; i++) {
                        if (filter) {
                            int nextx, nexty;
                            if ((nextx = x + 1) >= bWidth) {
                                nextx = 0;
                            }
                            if ((nexty = y + 1) >= bHeight) {
                                nexty = 0;
                            }
                            rgbs[0] = inData[inOff + y * inSpan + x];
                            rgbs[1] = inData[inOff + y * inSpan + nextx];
                            rgbs[2] = inData[inOff + nexty * inSpan + x];
                            rgbs[3] = inData[inOff + nexty * inSpan + nextx];
                            outData[out + i] =
                                TexturePaintContext.blend(rgbs, xerr, yerr);
                        } else {
                            outData[out + i] = inData[inOff + y * inSpan + x];
                        }
                        if ((xerr += colincxerr) < 0) {
                            xerr &= Integer.MAX_VALUE;
                            x++;
                        }
                        if ((x += colincx) >= bWidth) {
                            x -= bWidth;
                        }
                        if ((yerr += colincyerr) < 0) {
                            yerr &= Integer.MAX_VALUE;
                            y++;
                        }
                        if ((y += colincy) >= bHeight) {
                            y -= bHeight;
                        }
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }


                static class Byte extends TexturePaintContext {
        ByteInterleavedRaster srcRas;
        byte inData[];
        int inOff;
        int inSpan;
        byte outData[];
        int outOff;
        int outSpan;

        public Byte(ByteInterleavedRaster srcRas, ColorModel cm,
                    AffineTransform xform, int maxw)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
        }

        public WritableRaster makeRaster(int w, int h) {
            WritableRaster ras = makeByteRaster(srcRas, w, h);
            ByteInterleavedRaster biRas = (ByteInterleavedRaster) ras;
            outData = biRas.getDataStorage();
            outSpan = biRas.getScanlineStride();
            outOff = biRas.getDataOffset(0);
            return ras;
        }

        public void dispose() {
            dropByteRaster(outRas);
        }

        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            byte[] inData = this.inData;
            byte[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            boolean normalx = (colincx == 1 && colincxerr == 0 &&
                               colincy == 0 && colincyerr == 0);
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            if (normalx) {
                outSpan -= w;
            }
            for (int j = 0; j < h; j++) {
                if (normalx) {
                    int in = inOff + rowy * inSpan + bWidth;
                    x = bWidth - rowx;
                    out += w;
                    if (bWidth >= 32) {
                        int i = w;
                        while (i > 0) {
                            int copyw = (i < x) ? i : x;
                            System.arraycopy(inData, in - x,
                                             outData, out - i,
                                             copyw);
                            i -= copyw;
                            if ((x -= copyw) == 0) {
                                x = bWidth;
                            }
                        }
                    } else {
                        for (int i = w; i > 0; i--) {
                            outData[out - i] = inData[in - x];
                            if (--x == 0) {
                                x = bWidth;
                            }
                        }
                    }
                } else {
                    x = rowx;
                    y = rowy;
                    xerr = rowxerr;
                    yerr = rowyerr;
                    for (int i = 0; i < w; i++) {
                        outData[out + i] = inData[inOff + y * inSpan + x];
                        if ((xerr += colincxerr) < 0) {
                            xerr &= Integer.MAX_VALUE;
                            x++;
                        }
                        if ((x += colincx) >= bWidth) {
                            x -= bWidth;
                        }
                        if ((yerr += colincyerr) < 0) {
                            yerr &= Integer.MAX_VALUE;
                            y++;
                        }
                        if ((y += colincy) >= bHeight) {
                            y -= bHeight;
                        }
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }

    static class ByteFilter extends TexturePaintContext {
        ByteInterleavedRaster srcRas;
        int inPalette[];
        byte inData[];
        int inOff;
        int inSpan;
        int outData[];
        int outOff;
        int outSpan;

        public ByteFilter(ByteInterleavedRaster srcRas, ColorModel cm,
                          AffineTransform xform, int maxw)
        {
            super((cm.getTransparency() == Transparency.OPAQUE
                   ? xrgbmodel : argbmodel),
                  xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.inPalette = new int[256];
            ((IndexColorModel) cm).getRGBs(this.inPalette);
            this.srcRas = srcRas;
            this.inData = srcRas.getDataStorage();
            this.inSpan = srcRas.getScanlineStride();
            this.inOff = srcRas.getDataOffset(0);
        }

        public WritableRaster makeRaster(int w, int h) {
            // 注意我们不将 srcRas 传递给 makeRaster，因为它是 Byte Raster，
            // 而这个 colorModel 需要一个 Int Raster
            WritableRaster ras = makeRaster(colorModel, null, w, h);
            IntegerInterleavedRaster iiRas = (IntegerInterleavedRaster) ras;
            outData = iiRas.getDataStorage();
            outSpan = iiRas.getScanlineStride();
            outOff = iiRas.getDataOffset(0);
            return ras;
        }

        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            byte[] inData = this.inData;
            int[] outData = this.outData;
            int out = outOff;
            int inSpan = this.inSpan;
            int inOff = this.inOff;
            int outSpan = this.outSpan;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            int rgbs[] = new int[4];
            for (int j = 0; j < h; j++) {
                x = rowx;
                y = rowy;
                xerr = rowxerr;
                yerr = rowyerr;
                for (int i = 0; i < w; i++) {
                    int nextx, nexty;
                    if ((nextx = x + 1) >= bWidth) {
                        nextx = 0;
                    }
                    if ((nexty = y + 1) >= bHeight) {
                        nexty = 0;
                    }
                    rgbs[0] = inPalette[0xff & inData[inOff + x +
                                                      inSpan * y]];
                    rgbs[1] = inPalette[0xff & inData[inOff + nextx +
                                                      inSpan * y]];
                    rgbs[2] = inPalette[0xff & inData[inOff + x +
                                                      inSpan * nexty]];
                    rgbs[3] = inPalette[0xff & inData[inOff + nextx +
                                                      inSpan * nexty]];
                    outData[out + i] =
                        TexturePaintContext.blend(rgbs, xerr, yerr);
                    if ((xerr += colincxerr) < 0) {
                        xerr &= Integer.MAX_VALUE;
                        x++;
                    }
                    if ((x += colincx) >= bWidth) {
                        x -= bWidth;
                    }
                    if ((yerr += colincyerr) < 0) {
                        yerr &= Integer.MAX_VALUE;
                        y++;
                    }
                    if ((y += colincy) >= bHeight) {
                        y -= bHeight;
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
                out += outSpan;
            }
        }
    }

    static class Any extends TexturePaintContext {
        WritableRaster srcRas;
        boolean filter;

        public Any(WritableRaster srcRas, ColorModel cm,
                   AffineTransform xform, int maxw, boolean filter)
        {
            super(cm, xform, srcRas.getWidth(), srcRas.getHeight(), maxw);
            this.srcRas = srcRas;
            this.filter = filter;
        }

        public WritableRaster makeRaster(int w, int h) {
            return makeRaster(colorModel, srcRas, w, h);
        }

        public void setRaster(int x, int y, int xerr, int yerr,
                              int w, int h, int bWidth, int bHeight,
                              int colincx, int colincxerr,
                              int colincy, int colincyerr,
                              int rowincx, int rowincxerr,
                              int rowincy, int rowincyerr) {
            Object data = null;
            int rowx = x;
            int rowy = y;
            int rowxerr = xerr;
            int rowyerr = yerr;
            WritableRaster srcRas = this.srcRas;
            WritableRaster outRas = this.outRas;
            int rgbs[] = filter ? new int[4] : null;
            for (int j = 0; j < h; j++) {
                x = rowx;
                y = rowy;
                xerr = rowxerr;
                yerr = rowyerr;
                for (int i = 0; i < w; i++) {
                    data = srcRas.getDataElements(x, y, data);
                    if (filter) {
                        int nextx, nexty;
                        if ((nextx = x + 1) >= bWidth) {
                            nextx = 0;
                        }
                        if ((nexty = y + 1) >= bHeight) {
                            nexty = 0;
                        }
                        rgbs[0] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(nextx, y, data);
                        rgbs[1] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(x, nexty, data);
                        rgbs[2] = colorModel.getRGB(data);
                        data = srcRas.getDataElements(nextx, nexty, data);
                        rgbs[3] = colorModel.getRGB(data);
                        int rgb =
                            TexturePaintContext.blend(rgbs, xerr, yerr);
                        data = colorModel.getDataElements(rgb, data);
                    }
                    outRas.setDataElements(i, j, data);
                    if ((xerr += colincxerr) < 0) {
                        xerr &= Integer.MAX_VALUE;
                        x++;
                    }
                    if ((x += colincx) >= bWidth) {
                        x -= bWidth;
                    }
                    if ((yerr += colincyerr) < 0) {
                        yerr &= Integer.MAX_VALUE;
                        y++;
                    }
                    if ((y += colincy) >= bHeight) {
                        y -= bHeight;
                    }
                }
                if ((rowxerr += rowincxerr) < 0) {
                    rowxerr &= Integer.MAX_VALUE;
                    rowx++;
                }
                if ((rowx += rowincx) >= bWidth) {
                    rowx -= bWidth;
                }
                if ((rowyerr += rowincyerr) < 0) {
                    rowyerr &= Integer.MAX_VALUE;
                    rowy++;
                }
                if ((rowy += rowincy) >= bHeight) {
                    rowy -= bHeight;
                }
            }
        }
    }
}
