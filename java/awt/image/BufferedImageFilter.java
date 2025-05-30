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

import java.util.Hashtable;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;

/**
 * <code>BufferedImageFilter</code> 类继承自 <code>ImageFilter</code>，提供了一种简单的方法，
 * 使用单源/单目标图像操作符（{@link BufferedImageOp}）来过滤 <code>BufferedImage</code>
 * 在图像生产者/消费者/观察者范式中。这些图像操作符的示例包括：{@link ConvolveOp}，
 * {@link AffineTransformOp} 和 {@link LookupOp}。
 *
 * @see ImageFilter
 * @see BufferedImage
 * @see BufferedImageOp
 */

public class BufferedImageFilter extends ImageFilter implements Cloneable {
    BufferedImageOp bufferedImageOp;
    ColorModel model;
    int width;
    int height;
    byte[] bytePixels;
    int[] intPixels;

    /**
     * 构造一个 <code>BufferedImageFilter</code>，使用指定的单源/单目标操作符。
     * @param op 指定的 <code>BufferedImageOp</code>，用于过滤 <code>BufferedImage</code>
     * @throws NullPointerException 如果 op 为 null
     */
    public BufferedImageFilter (BufferedImageOp op) {
        super();
        if (op == null) {
            throw new NullPointerException("Operation cannot be null");
        }
        bufferedImageOp = op;
    }

    /**
     * 返回 <code>BufferedImageOp</code>。
     * @return 此 <code>BufferedImageFilter</code> 的操作符。
     */
    public BufferedImageOp getBufferedImageOp() {
        return bufferedImageOp;
    }

    /**
     * 过滤在 {@link ImageConsumer#setDimensions(int, int) setDimensions } 方法中提供的信息。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为这可能导致检索请求的像素时出现问题。
     * <p>
     * @param width 要设置的宽度
     * @param height 要设置的高度
     * @see ImageConsumer#setDimensions
     */
    public void setDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            imageComplete(STATICIMAGEDONE);
            return;
        }
        this.width  = width;
        this.height = height;
    }

    /**
     * 过滤在 {@link ImageConsumer#setColorModel(ColorModel) setColorModel} 方法中提供的信息。
     * <p>
     * 如果 <code>model</code> 为 <code>null</code>，此方法将清除此 <code>BufferedImageFilter</code> 的当前 <code>ColorModel</code>。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为这可能导致检索请求的像素时出现问题。
     * @param model 要设置的 <code>ColorModel</code>
     * @see ImageConsumer#setColorModel
     */
    public void setColorModel(ColorModel model) {
        this.model = model;
    }

    private void convertToRGB() {
        int size = width * height;
        int newpixels[] = new int[size];
        if (bytePixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = this.model.getRGB(bytePixels[i] & 0xff);
            }
        } else if (intPixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = this.model.getRGB(intPixels[i]);
            }
        }
        bytePixels = null;
        intPixels = newpixels;
        this.model = ColorModel.getRGBdefault();
    }

    /**
     * 过滤在 <code>ImageConsumer</code> 接口的 <code>setPixels</code> 方法中提供的信息，该方法接受一个字节数组。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为这可能导致检索请求的像素时出现问题。
     * @throws IllegalArgumentException 如果宽度或高度小于零。
     * @see ImageConsumer#setPixels(int, int, int, int, ColorModel, byte[],
                                    int, int)
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        // Fix 4184230
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("Width ("+w+
                                                ") and height ("+h+
                                                ") must be > 0");
        }
        // Nothing to do
        if (w == 0 || h == 0) {
            return;
        }
        if (y < 0) {
            int diff = -y;
            if (diff >= h) {
                return;
            }
            off += scansize * diff;
            y += diff;
            h -= diff;
        }
        if (y + h > height) {
            h = height - y;
            if (h <= 0) {
                return;
            }
        }
        if (x < 0) {
            int diff = -x;
            if (diff >= w) {
                return;
            }
            off += diff;
            x += diff;
            w -= diff;
        }
        if (x + w > width) {
            w = width - x;
            if (w <= 0) {
                return;
            }
        }
        int dstPtr = y * width + x;
        if (intPixels == null) {
            if (bytePixels == null) {
                bytePixels = new byte[width * height];
                this.model = model;
            } else if (this.model != model) {
                convertToRGB();
            }
            if (bytePixels != null) {
                for (int sh = h; sh > 0; sh--) {
                    System.arraycopy(pixels, off, bytePixels, dstPtr, w);
                    off += scansize;
                    dstPtr += width;
                }
            }
        }
        if (intPixels != null) {
            int dstRem = width - w;
            int srcRem = scansize - w;
            for (int sh = h; sh > 0; sh--) {
                for (int sw = w; sw > 0; sw--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[off++] & 0xff);
                }
                off += srcRem;
                dstPtr += dstRem;
            }
        }
    }

    /**
     * 过滤在 <code>ImageConsumer</code> 接口的 <code>setPixels</code> 方法中提供的信息，该方法接受一个整数数组。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为这可能导致检索请求的像素时出现问题。
     * @throws IllegalArgumentException 如果宽度或高度小于零。
     * @see ImageConsumer#setPixels(int, int, int, int, ColorModel, int[],
                                    int, int)
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        // Fix 4184230
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("Width ("+w+
                                                ") and height ("+h+
                                                ") must be > 0");
        }
        // Nothing to do
        if (w == 0 || h == 0) {
            return;
        }
        if (y < 0) {
            int diff = -y;
            if (diff >= h) {
                return;
            }
            off += scansize * diff;
            y += diff;
            h -= diff;
        }
        if (y + h > height) {
            h = height - y;
            if (h <= 0) {
                return;
            }
        }
        if (x < 0) {
            int diff = -x;
            if (diff >= w) {
                return;
            }
            off += diff;
            x += diff;
            w -= diff;
        }
        if (x + w > width) {
            w = width - x;
            if (w <= 0) {
                return;
            }
        }

        if (intPixels == null) {
            if (bytePixels == null) {
                intPixels = new int[width * height];
                this.model = model;
            } else {
                convertToRGB();
            }
        }
        int dstPtr = y * width + x;
        if (this.model == model) {
            for (int sh = h; sh > 0; sh--) {
                System.arraycopy(pixels, off, intPixels, dstPtr, w);
                off += scansize;
                dstPtr += width;
            }
        } else {
            if (this.model != ColorModel.getRGBdefault()) {
                convertToRGB();
            }
            int dstRem = width - w;
            int srcRem = scansize - w;
            for (int sh = h; sh > 0; sh--) {
                for (int sw = w; sw > 0; sw--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[off++]);
                }
                off += srcRem;
                dstPtr += dstRem;
            }
        }
    }

    /**
     * 过滤在 <code>ImageConsumer</code> 接口的 <code>imageComplete</code> 方法中提供的信息。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为这可能导致检索请求的像素时出现问题。
     * @param status 图像加载的状态
     * @throws ImagingOpException 如果调用与此实例关联的 <code>BufferedImageOp</code> 的过滤方法时出现问题。
     * @see ImageConsumer#imageComplete
     */
    public void imageComplete(int status) {
        WritableRaster wr;
        switch (status) {
        case IMAGEERROR:
        case IMAGEABORTED:
            // 重新初始化参数
            model  = null;
            width  = -1;
            height = -1;
            intPixels  = null;
            bytePixels = null;
            break;

        case SINGLEFRAMEDONE:
        case STATICIMAGEDONE:
            if (width <= 0 || height <= 0) break;
            if (model instanceof DirectColorModel) {
                if (intPixels == null) break;
                wr = createDCMraster();
            }
            else if (model instanceof IndexColorModel) {
                int[] bandOffsets = {0};
                if (bytePixels == null) break;
                DataBufferByte db = new DataBufferByte(bytePixels,
                                                       width * height);
                wr = Raster.createInterleavedRaster(db, width, height, width,
                                                    1, bandOffsets, null);
            }
            else {
                convertToRGB();
                if (intPixels == null) break;
                wr = createDCMraster();
            }
            BufferedImage bi = new BufferedImage(model, wr,
                                                 model.isAlphaPremultiplied(),
                                                 null);
            bi = bufferedImageOp.filter(bi, null);
            WritableRaster r = bi.getRaster();
            ColorModel cm = bi.getColorModel();
            int w = r.getWidth();
            int h = r.getHeight();
            consumer.setDimensions(w, h);
            consumer.setColorModel(cm);
            if (cm instanceof DirectColorModel) {
                DataBufferInt db = (DataBufferInt) r.getDataBuffer();
                consumer.setPixels(0, 0, w, h,
                                   cm, db.getData(), 0, w);
            }
            else if (cm instanceof IndexColorModel) {
                DataBufferByte db = (DataBufferByte) r.getDataBuffer();
                consumer.setPixels(0, 0, w, h,
                                   cm, db.getData(), 0, w);
            }
            else {
                throw new InternalError("Unknown color model " + cm);
            }
            break;
        }
        consumer.imageComplete(status);
    }

    private final WritableRaster createDCMraster() {
        WritableRaster wr;
        DirectColorModel dcm = (DirectColorModel) model;
        boolean hasAlpha = model.hasAlpha();
        int[] bandMasks = new int[3 + (hasAlpha ? 1 : 0)];
        bandMasks[0] = dcm.getRedMask();
        bandMasks[1] = dcm.getGreenMask();
        bandMasks[2] = dcm.getBlueMask();
        if (hasAlpha) {
            bandMasks[3] = dcm.getAlphaMask();
        }
        DataBufferInt db = new DataBufferInt(intPixels, width * height);
        wr = Raster.createPackedRaster(db, width, height, width,
                                       bandMasks, null);
        return wr;
    }

}
