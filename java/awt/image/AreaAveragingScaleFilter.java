/*
 * Copyright (c) 1996, 2002, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.util.Hashtable;
import java.awt.Rectangle;

/**
 * 一个使用简单区域平均算法缩放图像的 ImageFilter 类，该算法产生的结果比最近邻算法更平滑。
 * <p>此类扩展了基本的 ImageFilter 类，以缩放现有图像并提供包含重新采样图像的新图像的源。源图像中的像素经过混合以生成指定大小的图像。混合过程类似于使用像素复制将源图像放大到目标大小的倍数，然后通过简单地平均超大图像中落在目标图像给定像素内的所有像素，将其缩小到目标大小。如果源数据不是以 TopDownLeftRight 顺序传递的，则过滤器将退回到简单的像素复制行为，并使用 requestTopDownLeftRightResend() 方法在最后以更好的方式重新过滤像素。
 * <p>它旨在与 FilteredImageSource 对象结合使用，以生成现有图像的缩放版本。由于实现依赖性，不同平台上的图像过滤结果可能在像素值上有所不同。
 *
 * @see FilteredImageSource
 * @see ReplicateScaleFilter
 * @see ImageFilter
 *
 * @author      Jim Graham
 */
public class AreaAveragingScaleFilter extends ReplicateScaleFilter {
    private static final ColorModel rgbmodel = ColorModel.getRGBdefault();
    private static final int neededHints = (TOPDOWNLEFTRIGHT
                                            | COMPLETESCANLINES);

    private boolean passthrough;
    private float reds[], greens[], blues[], alphas[];
    private int savedy;
    private int savedyrem;

    /**
     * 构造一个 AreaAveragingScaleFilter，根据宽度和高度参数缩放其源图像的像素。
     * @param width 要缩放的图像的目标宽度
     * @param height 要缩放的图像的目标高度
     */
    public AreaAveragingScaleFilter(int width, int height) {
        super(width, height);
    }

    /**
     * 检测数据是否以允许平均算法工作的必要提示传递。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用，其像素正在被过滤。使用此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setHints
     */
    public void setHints(int hints) {
        passthrough = ((hints & neededHints) != neededHints);
        super.setHints(hints);
    }

    private void makeAccumBuffers() {
        reds = new float[destWidth];
        greens = new float[destWidth];
        blues = new float[destWidth];
        alphas = new float[destWidth];
    }

    private int[] calcRow() {
        float origmult = ((float) srcWidth) * srcHeight;
        if (outpixbuf == null || !(outpixbuf instanceof int[])) {
            outpixbuf = new int[destWidth];
        }
        int[] outpix = (int[]) outpixbuf;
        for (int x = 0; x < destWidth; x++) {
            float mult = origmult;
            int a = Math.round(alphas[x] / mult);
            if (a <= 0) {
                a = 0;
            } else if (a >= 255) {
                a = 255;
            } else {
                // 通过修改 mult，我们实际上是在同一个步骤中执行除以 mult 和除以 alpha 的操作
                mult = alphas[x] / 255;
            }
            int r = Math.round(reds[x] / mult);
            int g = Math.round(greens[x] / mult);
            int b = Math.round(blues[x] / mult);
            if (r < 0) {r = 0;} else if (r > 255) {r = 255;}
            if (g < 0) {g = 0;} else if (g > 255) {g = 255;}
            if (b < 0) {b = 0;} else if (b > 255) {b = 255;}
            outpix[x] = (a << 24 | r << 16 | g << 8 | b);
        }
        return outpix;
    }

    private void accumPixels(int x, int y, int w, int h,
                             ColorModel model, Object pixels, int off,
                             int scansize) {
        if (reds == null) {
            makeAccumBuffers();
        }
        int sy = y;
        int syrem = destHeight;
        int dy, dyrem;
        if (sy == 0) {
            dy = 0;
            dyrem = 0;
        } else {
            dy = savedy;
            dyrem = savedyrem;
        }
        while (sy < y + h) {
            int amty;
            if (dyrem == 0) {
                for (int i = 0; i < destWidth; i++) {
                    alphas[i] = reds[i] = greens[i] = blues[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if (syrem < dyrem) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float a = 0f, r = 0f, g = 0f, b = 0f;
            while (sx < w) {
                if (sxrem == 0) {
                    sxrem = destWidth;
                    int rgb;
                    if (pixels instanceof byte[]) {
                        rgb = ((byte[]) pixels)[off + sx] & 0xff;
                    } else {
                        rgb = ((int[]) pixels)[off + sx];
                    }
                    // getRGB() 始终返回非预乘分量
                    rgb = model.getRGB(rgb);
                    a = rgb >>> 24;
                    r = (rgb >> 16) & 0xff;
                    g = (rgb >>  8) & 0xff;
                    b = rgb & 0xff;
                    // 如果必要，预乘分量
                    if (a != 255.0f) {
                        float ascale = a / 255.0f;
                        r *= ascale;
                        g *= ascale;
                        b *= ascale;
                    }
                }
                int amtx;
                if (sxrem < dxrem) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float) amtx) * amty;
                alphas[dx] += mult * a;
                reds[dx] += mult * r;
                greens[dx] += mult * g;
                blues[dx] += mult * b;
                if ((sxrem -= amtx) == 0) {
                    sx++;
                }
                if ((dxrem -= amtx) == 0) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ((dyrem -= amty) == 0) {
                int outpix[] = calcRow();
                do {
                    consumer.setPixels(0, dy, destWidth, 1,
                                       rgbmodel, outpix, 0, destWidth);
                    dy++;
                } while ((syrem -= amty) >= amty && amty == srcHeight);
            } else {
                syrem -= amty;
            }
            if (syrem == 0) {
                syrem = destHeight;
                sy++;
                off += scansize;
            }
        }
        savedyrem = dyrem;
        savedy = dy;
    }

    /**
     * 将传递的字节像素的分量组合到累加数组中，并发送任何已完成的像素行的平均数据。如果 setHints 调用中没有指定正确的提示，则将工作传递给我们的超类，该超类能够根据传递提示缩放像素。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用，其像素正在被过滤。使用此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ReplicateScaleFilter
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        if (passthrough) {
            super.setPixels(x, y, w, h, model, pixels, off, scansize);
        } else {
            accumPixels(x, y, w, h, model, pixels, off, scansize);
        }
    }

    /**
     * 将传递的整数像素的分量组合到累加数组中，并发送任何已完成的像素行的平均数据。如果 setHints 调用中没有指定正确的提示，则将工作传递给我们的超类，该超类能够根据传递提示缩放像素。
     * <p>
     * 注意：此方法应由 <code>Image</code> 的 <code>ImageProducer</code> 调用，其像素正在被过滤。使用此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ReplicateScaleFilter
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        if (passthrough) {
            super.setPixels(x, y, w, h, model, pixels, off, scansize);
        } else {
            accumPixels(x, y, w, h, model, pixels, off, scansize);
        }
    }
}
