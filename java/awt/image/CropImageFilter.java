/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于裁剪图像的 ImageFilter 类。
 * 该类扩展了基本的 ImageFilter 类，用于从现有图像中提取指定的矩形区域，并提供一个包含仅提取区域的新图像的源。它旨在与 FilteredImageSource 对象一起使用，以生成现有图像的裁剪版本。
 *
 * @see FilteredImageSource
 * @see ImageFilter
 *
 * @author      Jim Graham
 */
public class CropImageFilter extends ImageFilter {
    int cropX;
    int cropY;
    int cropW;
    int cropH;

    /**
     * 构造一个 CropImageFilter，从源图像中提取由 x, y, w, h 参数指定的绝对矩形区域的像素。
     * @param x 要提取的矩形的顶部 x 位置
     * @param y 要提取的矩形的顶部 y 位置
     * @param w 要提取的矩形的宽度
     * @param h 要提取的矩形的高度
     */
    public CropImageFilter(int x, int y, int w, int h) {
        cropX = x;
        cropY = y;
        cropW = w;
        cropH = h;
    }

    /**
     * 传递源对象的属性，并添加一个指示裁剪区域的属性。
     * 此方法调用 <code>super.setProperties</code>，可能会导致添加额外的属性。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setProperties(Hashtable<?,?> props) {
        Hashtable<Object,Object> p = (Hashtable<Object,Object>)props.clone();
        p.put("croprect", new Rectangle(cropX, cropY, cropW, cropH));
        super.setProperties(p);
    }

    /**
     * 覆盖源图像的尺寸，并将裁剪区域的尺寸传递给 ImageConsumer。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer
     */
    public void setDimensions(int w, int h) {
        consumer.setDimensions(cropW, cropH);
    }

    /**
     * 确定传递的字节像素是否与要提取的区域相交，并仅传递出现在输出区域中的那部分像素。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        int x1 = x;
        if (x1 < cropX) {
            x1 = cropX;
        }
    int x2 = addWithoutOverflow(x, w);
        if (x2 > cropX + cropW) {
            x2 = cropX + cropW;
        }
        int y1 = y;
        if (y1 < cropY) {
            y1 = cropY;
        }

    int y2 = addWithoutOverflow(y, h);
        if (y2 > cropY + cropH) {
            y2 = cropY + cropH;
        }
        if (x1 >= x2 || y1 >= y2) {
            return;
        }
        consumer.setPixels(x1 - cropX, y1 - cropY, (x2 - x1), (y2 - y1),
                           model, pixels,
                           off + (y1 - y) * scansize + (x1 - x), scansize);
    }

    /**
     * 确定传递的 int 像素是否与要提取的区域相交，并仅传递出现在输出区域中的那部分像素。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类从图像中过滤像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        int x1 = x;
        if (x1 < cropX) {
            x1 = cropX;
        }
    int x2 = addWithoutOverflow(x, w);
        if (x2 > cropX + cropW) {
            x2 = cropX + cropW;
        }
        int y1 = y;
        if (y1 < cropY) {
            y1 = cropY;
        }

    int y2 = addWithoutOverflow(y, h);
        if (y2 > cropY + cropH) {
            y2 = cropY + cropH;
        }
        if (x1 >= x2 || y1 >= y2) {
            return;
        }
        consumer.setPixels(x1 - cropX, y1 - cropY, (x2 - x1), (y2 - y1),
                           model, pixels,
                           off + (y1 - y) * scansize + (x1 - x), scansize);
    }

    // 检查潜在的溢出（参见 bug 4801285）
    private int addWithoutOverflow(int x, int w) {
        int x2 = x + w;
        if ( x > 0 && w > 0 && x2 < 0 ) {
            x2 = Integer.MAX_VALUE;
        } else if( x < 0 && w < 0 && x2 > 0 ) {
            x2 = Integer.MIN_VALUE;
        }
        return x2;
    }
}
