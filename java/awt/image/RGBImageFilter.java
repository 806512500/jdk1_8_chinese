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

import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;

/**
 * 该类提供了一种简单的方法来创建一个修改图像像素的 ImageFilter。它旨在与 FilteredImageSource 对象结合使用，以生成现有图像的过滤版本。它是一个抽象类，提供了将所有像素数据通过一个方法的调用，该方法将像素逐个转换为默认的 RGB ColorModel，无论 ImageProducer 使用的 ColorModel 是什么。要创建一个可用的图像过滤器，唯一需要定义的方法是 filterRGB 方法。以下是一个定义交换图像红色和蓝色组件的过滤器的示例：
 * <pre>{@code
 *
 *      class RedBlueSwapFilter extends RGBImageFilter {
 *          public RedBlueSwapFilter() {
 *              // 过滤器的操作不依赖于像素的位置，因此可以直接过滤 IndexColorModels。
 *              canFilterIndexColorModel = true;
 *          }
 *
 *          public int filterRGB(int x, int y, int rgb) {
 *              return ((rgb & 0xff00ff00)
 *                      | ((rgb & 0xff0000) >> 16)
 *                      | ((rgb & 0xff) << 16));
 *          }
 *      }
 *
 * }</pre>
 *
 * @see FilteredImageSource
 * @see ImageFilter
 * @see ColorModel#getRGBdefault
 *
 * @author      Jim Graham
 */
public abstract class RGBImageFilter extends ImageFilter {

    /**
     * 当用户调用 {@link #substituteColorModel(ColorModel, ColorModel) substituteColorModel} 方法时，要被替换的 <code>ColorModel</code>。
     */
    protected ColorModel origmodel;

    /**
     * 当用户调用 <code>substituteColorModel</code> 方法时，用于替换 <code>origmodel</code> 的 <code>ColorModel</code>。
     */
    protected ColorModel newmodel;

    /**
     * 该布尔值指示是否可以将 filterRGB 方法的颜色过滤应用于 IndexColorModel 对象的颜色表条目，而不是逐像素过滤。子类应在构造函数中将此变量设置为 true，如果它们的 filterRGB 方法不依赖于被过滤像素的坐标。
     * @see #substituteColorModel
     * @see #filterRGB
     * @see IndexColorModel
     */
    protected boolean canFilterIndexColorModel;

    /**
     * 如果 ColorModel 是 IndexColorModel，并且子类已将 canFilterIndexColorModel 标志设置为 true，则在此处替换为过滤后的颜色模型，并在 setPixels 方法中出现原始 ColorModel 对象时替换。如果 ColorModel 不是 IndexColorModel 或为 null，则此方法覆盖 ImageProducer 使用的默认 ColorModel，并指定默认的 RGB ColorModel。
     * <p>
     * 注意：此方法应由图像的 <code>ImageProducer</code> 调用。使用此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer
     * @see ColorModel#getRGBdefault
     */
    public void setColorModel(ColorModel model) {
        if (canFilterIndexColorModel && (model instanceof IndexColorModel)) {
            ColorModel newcm = filterIndexColorModel((IndexColorModel)model);
            substituteColorModel(model, newcm);
            consumer.setColorModel(newcm);
        } else {
            consumer.setColorModel(ColorModel.getRGBdefault());
        }
    }

    /**
     * 注册两个 ColorModel 对象以进行替换。如果在 setPixels 方法中遇到 oldcm，则用 newcm 替换，并且像素将不经过滤直接传递（但使用新的 ColorModel 对象）。
     * @param oldcm 要替换的 ColorModel 对象
     * @param newcm 用于替换 oldcm 的 ColorModel 对象
     */
    public void substituteColorModel(ColorModel oldcm, ColorModel newcm) {
        origmodel = oldcm;
        newmodel = newcm;
    }

    /**
     * 通过将每个颜色表条目通过 RGBImageFilter 子类必须提供的 filterRGB 函数来过滤 IndexColorModel 对象。使用坐标 -1 表示正在过滤颜色表条目，而不是实际的像素值。
     * @param icm 要过滤的 IndexColorModel 对象
     * @exception NullPointerException 如果 <code>icm</code> 为 null
     * @return 代表过滤后颜色的新 IndexColorModel
     */
    public IndexColorModel filterIndexColorModel(IndexColorModel icm) {
        int mapsize = icm.getMapSize();
        byte r[] = new byte[mapsize];
        byte g[] = new byte[mapsize];
        byte b[] = new byte[mapsize];
        byte a[] = new byte[mapsize];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        icm.getAlphas(a);
        int trans = icm.getTransparentPixel();
        boolean needalpha = false;
        for (int i = 0; i < mapsize; i++) {
            int rgb = filterRGB(-1, -1, icm.getRGB(i));
            a[i] = (byte) (rgb >> 24);
            if (a[i] != ((byte)0xff) && i != trans) {
                needalpha = true;
            }
            r[i] = (byte) (rgb >> 16);
            g[i] = (byte) (rgb >> 8);
            b[i] = (byte) (rgb >> 0);
        }
        if (needalpha) {
            return new IndexColorModel(icm.getPixelSize(), mapsize,
                                       r, g, b, a);
        } else {
            return new IndexColorModel(icm.getPixelSize(), mapsize,
                                       r, g, b, trans);
        }
    }

    /**
     * 通过将它们逐个传递给 filterRGB 方法来过滤默认 RGB ColorModel 的像素缓冲区。
     * @param x 像素区域左上角的 X 坐标
     * @param y 像素区域左上角的 Y 坐标
     * @param w 像素区域的宽度
     * @param h 像素区域的高度
     * @param pixels 像素数组
     * @param off 像素数组中的偏移量
     * @param scansize 从一行像素到下一行像素在数组中的距离
     * @see ColorModel#getRGBdefault
     * @see #filterRGB
     */
    public void filterRGBPixels(int x, int y, int w, int h,
                                int pixels[], int off, int scansize) {
        int index = off;
        for (int cy = 0; cy < h; cy++) {
            for (int cx = 0; cx < w; cx++) {
                pixels[index] = filterRGB(x + cx, y + cy, pixels[index]);
                index++;
            }
            index += scansize - w;
        }
        consumer.setPixels(x, y, w, h, ColorModel.getRGBdefault(),
                           pixels, off, scansize);
    }

    /**
     * 如果 ColorModel 对象是已经转换过的同一个对象，则直接使用转换后的 ColorModel 传递像素。否则，将字节像素缓冲区转换为默认的 RGB ColorModel，并将转换后的缓冲区传递给 filterRGBPixels 方法逐个转换。
     * <p>
     * 注意：此方法应由图像的 <code>ImageProducer</code> 调用。使用此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ColorModel#getRGBdefault
     * @see #filterRGBPixels
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        if (model == origmodel) {
            consumer.setPixels(x, y, w, h, newmodel, pixels, off, scansize);
        } else {
            int filteredpixels[] = new int[w];
            int index = off;
            for (int cy = 0; cy < h; cy++) {
                for (int cx = 0; cx < w; cx++) {
                    filteredpixels[cx] = model.getRGB((pixels[index] & 0xff));
                    index++;
                }
                index += scansize - w;
                filterRGBPixels(x, y + cy, w, 1, filteredpixels, 0, w);
            }
        }
    }

    /**
     * 如果 ColorModel 对象是已经转换过的同一个对象，则直接使用转换后的 ColorModel 传递像素。否则，将整数像素缓冲区转换为默认的 RGB ColorModel，并将转换后的缓冲区传递给 filterRGBPixels 方法逐个转换。
     * <p>
     * 注意：此方法应由图像的 <code>ImageProducer</code> 调用。使用此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ColorModel#getRGBdefault
     * @see #filterRGBPixels
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        if (model == origmodel) {
            consumer.setPixels(x, y, w, h, newmodel, pixels, off, scansize);
        } else {
            int filteredpixels[] = new int[w];
            int index = off;
            for (int cy = 0; cy < h; cy++) {
                for (int cx = 0; cx < w; cx++) {
                    filteredpixels[cx] = model.getRGB(pixels[index]);
                    index++;
                }
                index += scansize - w;
                filterRGBPixels(x, y + cy, w, 1, filteredpixels, 0, w);
            }
        }
    }

    /**
     * 子类必须指定一个方法，将默认 RGB ColorModel 中的单个输入像素转换为单个输出像素。
     * @param x 像素的 X 坐标
     * @param y 像素的 Y 坐标
     * @param rgb 默认 RGB 颜色模型中的整数像素表示
     * @return 默认 RGB 颜色模型中的过滤后的像素。
     * @see ColorModel#getRGBdefault
     * @see #filterRGBPixels
     */
    public abstract int filterRGB(int x, int y, int rgb);
}
