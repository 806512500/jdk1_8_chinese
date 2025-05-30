
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.image.ImageProducer;
import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.awt.Image;

/**
 * PixelGrabber 类实现了 ImageConsumer，可以附加到 Image 或 ImageProducer 对象以检索该图像中的部分像素。以下是一个示例：
 * <pre>{@code
 *
 * public void handlesinglepixel(int x, int y, int pixel) {
 *      int alpha = (pixel >> 24) & 0xff;
 *      int red   = (pixel >> 16) & 0xff;
 *      int green = (pixel >>  8) & 0xff;
 *      int blue  = (pixel      ) & 0xff;
 *      // 处理像素...
 * }
 *
 * public void handlepixels(Image img, int x, int y, int w, int h) {
 *      int[] pixels = new int[w * h];
 *      PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
 *      try {
 *          pg.grabPixels();
 *      } catch (InterruptedException e) {
 *          System.err.println("等待像素时被中断！");
 *          return;
 *      }
 *      if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
 *          System.err.println("图像获取被中止或出错");
 *          return;
 *      }
 *      for (int j = 0; j < h; j++) {
 *          for (int i = 0; i < w; i++) {
 *              handlesinglepixel(x+i, y+j, pixels[j * w + i]);
 *          }
 *      }
 * }
 *
 * }</pre>
 *
 * @see ColorModel#getRGBdefault
 *
 * @author      Jim Graham
 */
public class PixelGrabber implements ImageConsumer {
    ImageProducer producer;

    int dstX;
    int dstY;
    int dstW;
    int dstH;

    ColorModel imageModel;
    byte[] bytePixels;
    int[] intPixels;
    int dstOff;
    int dstScan;

    private boolean grabbing;
    private int flags;

    private static final int GRABBEDBITS = (ImageObserver.FRAMEBITS
                                            | ImageObserver.ALLBITS);
    private static final int DONEBITS = (GRABBEDBITS
                                         | ImageObserver.ERROR);

    /**
     * 创建一个 PixelGrabber 对象，用于从指定图像中抓取 (x, y, w, h) 矩形区域的像素，并存储到给定数组中。
     * 像素以默认的 RGB ColorModel 存储。像素 (i, j) 的 RGB 数据（其中 (i, j) 在矩形 (x, y, w, h) 内）存储在数组中的位置为
     * <tt>pix[(j - y) * scansize + (i - x) + off]</tt>。
     * @see ColorModel#getRGBdefault
     * @param img 要从中检索像素的图像
     * @param x 矩形左上角的 x 坐标，相对于图像的默认（未缩放）大小
     * @param y 矩形左上角的 y 坐标
     * @param w 要检索的矩形像素的宽度
     * @param h 要检索的矩形像素的高度
     * @param pix 用于存储从图像中检索到的 RGB 像素的整数数组
     * @param off 存储第一个像素的数组偏移量
     * @param scansize 数组中一行像素到下一行像素的距离
     */
    public PixelGrabber(Image img, int x, int y, int w, int h,
                        int[] pix, int off, int scansize) {
        this(img.getSource(), x, y, w, h, pix, off, scansize);
    }

    /**
     * 创建一个 PixelGrabber 对象，用于从指定 ImageProducer 生成的图像中抓取 (x, y, w, h) 矩形区域的像素，并存储到给定数组中。
     * 像素以默认的 RGB ColorModel 存储。像素 (i, j) 的 RGB 数据（其中 (i, j) 在矩形 (x, y, w, h) 内）存储在数组中的位置为
     * <tt>pix[(j - y) * scansize + (i - x) + off]</tt>。
     * @param ip 生成图像的 ImageProducer
     * @param x 矩形左上角的 x 坐标，相对于图像的默认（未缩放）大小
     * @param y 矩形左上角的 y 坐标
     * @param w 要检索的矩形像素的宽度
     * @param h 要检索的矩形像素的高度
     * @param pix 用于存储从图像中检索到的 RGB 像素的整数数组
     * @param off 存储第一个像素的数组偏移量
     * @param scansize 数组中一行像素到下一行像素的距离
     * @see ColorModel#getRGBdefault
     */
    public PixelGrabber(ImageProducer ip, int x, int y, int w, int h,
                        int[] pix, int off, int scansize) {
        producer = ip;
        dstX = x;
        dstY = y;
        dstW = w;
        dstH = h;
        dstOff = off;
        dstScan = scansize;
        intPixels = pix;
        imageModel = ColorModel.getRGBdefault();
    }

    /**
     * 创建一个 PixelGrabber 对象，用于从指定图像中抓取 (x, y, w, h) 矩形区域的像素。如果每次调用 setPixels 时都使用相同的 ColorModel，
     * 则像素将累积在原始 ColorModel 中，否则将累积在默认的 RGB ColorModel 中。如果 forceRGB 参数为 true，则像素将始终转换为默认的 RGB ColorModel。
     * 在任何情况下，PixelGrabber 都会分配一个缓冲区来存储像素。如果 {@code (w < 0)} 或 {@code (h < 0)}，则它们将默认为源数据的剩余宽度和高度。
     * @param img 要从中检索图像数据的图像
     * @param x 矩形左上角的 x 坐标，相对于图像的默认（未缩放）大小
     * @param y 矩形左上角的 y 坐标
     * @param w 要检索的矩形像素的宽度
     * @param h 要检索的矩形像素的高度
     * @param forceRGB 如果像素应始终转换为默认的 RGB ColorModel，则为 true
     */
    public PixelGrabber(Image img, int x, int y, int w, int h,
                        boolean forceRGB)
    {
        producer = img.getSource();
        dstX = x;
        dstY = y;
        dstW = w;
        dstH = h;
        if (forceRGB) {
            imageModel = ColorModel.getRGBdefault();
        }
    }

    /**
     * 请求 PixelGrabber 开始获取像素。
     */
    public synchronized void startGrabbing() {
        if ((flags & DONEBITS) != 0) {
            return;
        }
        if (!grabbing) {
            grabbing = true;
            flags &= ~(ImageObserver.ABORT);
            producer.startProduction(this);
        }
    }

    /**
     * 请求 PixelGrabber 中止图像获取。
     */
    public synchronized void abortGrabbing() {
        imageComplete(IMAGEABORTED);
    }

    /**
     * 请求 Image 或 ImageProducer 开始交付像素，并等待矩形区域中的所有像素被交付。
     * @return 如果像素成功获取，则返回 true；在中止、错误或超时时返回 false
     * @exception InterruptedException
     *            其他线程已中断此线程。
     */
    public boolean grabPixels() throws InterruptedException {
        return grabPixels(0);
    }

    /**
     * 请求 Image 或 ImageProducer 开始交付像素，并等待矩形区域中的所有像素被交付或指定的超时时间已过期。此方法根据
     * <code>ms</code> 的值以以下方式行为：
     * <ul>
     * <li> 如果 {@code ms == 0}，则等待所有像素被交付
     * <li> 如果 {@code ms > 0}，则等待所有像素被交付或超时到期。
     * <li> 如果 {@code ms < 0}，则如果所有像素都被获取则返回 <code>true</code>，否则返回 <code>false</code>，并且不等待。
     * </ul>
     * @param ms 等待图像像素到达的毫秒数
     * @return 如果像素成功获取，则返回 true；在中止、错误或超时时返回 false
     * @exception InterruptedException
     *            其他线程已中断此线程。
     */
    public synchronized boolean grabPixels(long ms)
        throws InterruptedException
    {
        if ((flags & DONEBITS) != 0) {
            return (flags & GRABBEDBITS) != 0;
        }
        long end = ms + System.currentTimeMillis();
        if (!grabbing) {
            grabbing = true;
            flags &= ~(ImageObserver.ABORT);
            producer.startProduction(this);
        }
        while (grabbing) {
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    break;
                }
            }
            wait(timeout);
        }
        return (flags & GRABBEDBITS) != 0;
    }

    /**
     * 返回像素的状态。返回表示可用像素信息的 ImageObserver 标志。
     * @return 所有相关 ImageObserver 标志的按位或
     * @see ImageObserver
     */
    public synchronized int getStatus() {
        return flags;
    }

    /**
     * 获取像素缓冲区的宽度（调整图像宽度后）。如果未为要抓取的像素矩形指定宽度，则在图像交付尺寸后此信息才可用。
     * @return 用于像素缓冲区的最终宽度，如果宽度未知则返回 -1
     * @see #getStatus
     */
    public synchronized int getWidth() {
        return (dstW < 0) ? -1 : dstW;
    }

    /**
     * 获取像素缓冲区的高度（调整图像高度后）。如果未为要抓取的像素矩形指定高度，则在图像交付尺寸后此信息才可用。
     * @return 用于像素缓冲区的最终高度，如果高度未知则返回 -1
     * @see #getStatus
     */
    public synchronized int getHeight() {
        return (dstH < 0) ? -1 : dstH;
    }

    /**
     * 获取像素缓冲区。如果 PixelGrabber 未使用显式的像素缓冲区构造，则在知道图像数据的大小和格式之前，此方法将返回 null。
     * 由于 PixelGrabber 可能随时在源图像使用多个 ColorModel 交付数据时回退到在默认的 RGB ColorModel 中累积数据，
     * 因此此方法返回的数组对象在图像抓取完成之前可能会随时间变化。
     * @return 一个字节数组或一个整数数组
     * @see #getStatus
     * @see #setPixels(int, int, int, int, ColorModel, byte[], int, int)
     * @see #setPixels(int, int, int, int, ColorModel, int[], int, int)
     */
    public synchronized Object getPixels() {
        return (bytePixels == null)
            ? ((Object) intPixels)
            : ((Object) bytePixels);
    }

    /**
     * 获取存储在数组中的像素的 ColorModel。如果 PixelGrabber 使用显式的像素缓冲区构造，则此方法将始终返回默认的 RGB ColorModel，
     * 否则在知道 ImageProducer 使用的 ColorModel 之前可能返回 null。
     * 由于 PixelGrabber 可能随时在源图像使用多个 ColorModel 交付数据时回退到在默认的 RGB ColorModel 中累积数据，
     * 因此此方法返回的 ColorModel 对象在图像抓取完成之前可能会随时间变化，并且可能不反映 ImageProducer 用于交付像素的任何 ColorModel 对象。
     * @return 用于存储像素的 ColorModel 对象
     * @see #getStatus
     * @see ColorModel#getRGBdefault
     * @see #setColorModel(ColorModel)
     */
    public synchronized ColorModel getColorModel() {
        return imageModel;
    }

    /**
     * setDimensions 方法是 ImageConsumer API 的一部分，此类必须实现该 API 以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，
     * 因为该操作可能导致无法正确检索请求的像素。
     * @param width 尺寸的宽度
     * @param height 尺寸的高度
     */
    public void setDimensions(int width, int height) {
        if (dstW < 0) {
            dstW = width - dstX;
        }
        if (dstH < 0) {
            dstH = height - dstY;
        }
        if (dstW <= 0 || dstH <= 0) {
            imageComplete(STATICIMAGEDONE);
        } else if (intPixels == null &&
                   imageModel == ColorModel.getRGBdefault()) {
            intPixels = new int[dstW * dstH];
            dstScan = dstW;
            dstOff = 0;
        }
        flags |= (ImageObserver.WIDTH | ImageObserver.HEIGHT);
    }

    /**
     * setHints 方法是 ImageConsumer API 的一部分，此类必须实现该 API 以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，
     * 因为该操作可能导致无法正确检索请求的像素。
     * @param hints 用于处理像素的提示集
     */
    public void setHints(int hints) {
        return;
    }


                /**
     * setProperties 方法是 ImageConsumer API 的一部分，该类必须实现此方法以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为该操作可能导致无法正确检索请求的像素。
     * @param props 属性列表
     */
    public void setProperties(Hashtable<?,?> props) {
        return;
    }

    /**
     * setColorModel 方法是 ImageConsumer API 的一部分，该类必须实现此方法以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为该操作可能导致无法正确检索请求的像素。
     * @param model 指定的 <code>ColorModel</code>
     * @see #getColorModel
     */
    public void setColorModel(ColorModel model) {
        return;
    }

    private void convertToRGB() {
        int size = dstW * dstH;
        int newpixels[] = new int[size];
        if (bytePixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = imageModel.getRGB(bytePixels[i] & 0xff);
            }
        } else if (intPixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = imageModel.getRGB(intPixels[i]);
            }
        }
        bytePixels = null;
        intPixels = newpixels;
        dstScan = dstW;
        dstOff = 0;
        imageModel = ColorModel.getRGBdefault();
    }

    /**
     * setPixels 方法是 ImageConsumer API 的一部分，该类必须实现此方法以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为该操作可能导致无法正确检索请求的像素。
     * @param srcX 要设置的像素区域左上角的 X 坐标
     * @param srcY 要设置的像素区域左上角的 Y 坐标
     * @param srcW 要设置的像素区域的宽度
     * @param srcH 要设置的像素区域的高度
     * @param model 指定的 <code>ColorModel</code>
     * @param pixels 像素数组
     * @param srcOff 像素数组中的偏移量
     * @param srcScan 像素数组中一行像素到下一行像素的距离
     * @see #getPixels
     */
    public void setPixels(int srcX, int srcY, int srcW, int srcH,
                          ColorModel model,
                          byte pixels[], int srcOff, int srcScan) {
        if (srcY < dstY) {
            int diff = dstY - srcY;
            if (diff >= srcH) {
                return;
            }
            srcOff += srcScan * diff;
            srcY += diff;
            srcH -= diff;
        }
        if (srcY + srcH > dstY + dstH) {
            srcH = (dstY + dstH) - srcY;
            if (srcH <= 0) {
                return;
            }
        }
        if (srcX < dstX) {
            int diff = dstX - srcX;
            if (diff >= srcW) {
                return;
            }
            srcOff += diff;
            srcX += diff;
            srcW -= diff;
        }
        if (srcX + srcW > dstX + dstW) {
            srcW = (dstX + dstW) - srcX;
            if (srcW <= 0) {
                return;
            }
        }
        int dstPtr = dstOff + (srcY - dstY) * dstScan + (srcX - dstX);
        if (intPixels == null) {
            if (bytePixels == null) {
                bytePixels = new byte[dstW * dstH];
                dstScan = dstW;
                dstOff = 0;
                imageModel = model;
            } else if (imageModel != model) {
                convertToRGB();
            }
            if (bytePixels != null) {
                for (int h = srcH; h > 0; h--) {
                    System.arraycopy(pixels, srcOff, bytePixels, dstPtr, srcW);
                    srcOff += srcScan;
                    dstPtr += dstScan;
                }
            }
        }
        if (intPixels != null) {
            int dstRem = dstScan - srcW;
            int srcRem = srcScan - srcW;
            for (int h = srcH; h > 0; h--) {
                for (int w = srcW; w > 0; w--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[srcOff++]&0xff);
                }
                srcOff += srcRem;
                dstPtr += dstRem;
            }
        }
        flags |= ImageObserver.SOMEBITS;
    }

    /**
     * setPixels 方法是 ImageConsumer API 的一部分，该类必须实现此方法以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为该操作可能导致无法正确检索请求的像素。
     * @param srcX 要设置的像素区域左上角的 X 坐标
     * @param srcY 要设置的像素区域左上角的 Y 坐标
     * @param srcW 要设置的像素区域的宽度
     * @param srcH 要设置的像素区域的高度
     * @param model 指定的 <code>ColorModel</code>
     * @param pixels 像素数组
     * @param srcOff 像素数组中的偏移量
     * @param srcScan 像素数组中一行像素到下一行像素的距离
     * @see #getPixels
     */
    public void setPixels(int srcX, int srcY, int srcW, int srcH,
                          ColorModel model,
                          int pixels[], int srcOff, int srcScan) {
        if (srcY < dstY) {
            int diff = dstY - srcY;
            if (diff >= srcH) {
                return;
            }
            srcOff += srcScan * diff;
            srcY += diff;
            srcH -= diff;
        }
        if (srcY + srcH > dstY + dstH) {
            srcH = (dstY + dstH) - srcY;
            if (srcH <= 0) {
                return;
            }
        }
        if (srcX < dstX) {
            int diff = dstX - srcX;
            if (diff >= srcW) {
                return;
            }
            srcOff += diff;
            srcX += diff;
            srcW -= diff;
        }
        if (srcX + srcW > dstX + dstW) {
            srcW = (dstX + dstW) - srcX;
            if (srcW <= 0) {
                return;
            }
        }
        if (intPixels == null) {
            if (bytePixels == null) {
                intPixels = new int[dstW * dstH];
                dstScan = dstW;
                dstOff = 0;
                imageModel = model;
            } else {
                convertToRGB();
            }
        }
        int dstPtr = dstOff + (srcY - dstY) * dstScan + (srcX - dstX);
        if (imageModel == model) {
            for (int h = srcH; h > 0; h--) {
                System.arraycopy(pixels, srcOff, intPixels, dstPtr, srcW);
                srcOff += srcScan;
                dstPtr += dstScan;
            }
        } else {
            if (imageModel != ColorModel.getRGBdefault()) {
                convertToRGB();
            }
            int dstRem = dstScan - srcW;
            int srcRem = srcScan - srcW;
            for (int h = srcH; h > 0; h--) {
                for (int w = srcW; w > 0; w--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[srcOff++]);
                }
                srcOff += srcRem;
                dstPtr += dstRem;
            }
        }
        flags |= ImageObserver.SOMEBITS;
    }

    /**
     * imageComplete 方法是 ImageConsumer API 的一部分，该类必须实现此方法以检索像素。
     * <p>
     * 注意：此方法应由正在抓取像素的图像的 ImageProducer 调用。使用此类从图像中检索像素的开发人员应避免直接调用此方法，因为该操作可能导致无法正确检索请求的像素。
     * @param status 图像加载的状态
     */
    public synchronized void imageComplete(int status) {
        grabbing = false;
        switch (status) {
        default:
        case IMAGEERROR:
            flags |= ImageObserver.ERROR | ImageObserver.ABORT;
            break;
        case IMAGEABORTED:
            flags |= ImageObserver.ABORT;
            break;
        case STATICIMAGEDONE:
            flags |= ImageObserver.ALLBITS;
            break;
        case SINGLEFRAMEDONE:
            flags |= ImageObserver.FRAMEBITS;
            break;
        }
        producer.removeConsumer(this);
        notifyAll();
    }

    /**
     * 返回像素的状态。返回表示可用像素信息的 ImageObserver 标志。
     * 此方法和 {@link #getStatus() getStatus} 具有相同的实现，但 <code>getStatus</code> 是首选方法，因为它符合命名信息检索方法的“getXXX”形式。
     * @return 所有相关 ImageObserver 标志的按位或
     * @see ImageObserver
     * @see #getStatus()
     */
    public synchronized int status() {
        return flags;
    }
}
