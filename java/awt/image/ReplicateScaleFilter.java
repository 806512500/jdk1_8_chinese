/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于使用最简单算法缩放图像的 ImageFilter 类。
 * 该类扩展了基本的 ImageFilter 类，以缩放现有图像并提供包含重新采样图像的新图像的源。源图像中的像素被采样以生成指定大小图像的像素，通过复制行和列的像素来放大，或通过省略行和列的像素来缩小。
 * <p>它旨在与 FilteredImageSource 对象一起使用，以生成现有图像的缩放版本。由于实现依赖性，不同平台上的过滤图像的像素值可能会有所不同。
 *
 * @see FilteredImageSource
 * @see ImageFilter
 *
 * @author      Jim Graham
 */
public class ReplicateScaleFilter extends ImageFilter {

    /**
     * 源图像的宽度。
     */
    protected int srcWidth;

    /**
     * 源图像的高度。
     */
    protected int srcHeight;

    /**
     * 要缩放图像的目标宽度。
     */
    protected int destWidth;

    /**
     * 要缩放图像的目标高度。
     */
    protected int destHeight;

    /**
     * 一个包含一行像素信息的 int 数组。
     */
    protected int srcrows[];

    /**
     * 一个包含一列像素信息的 int 数组。
     */
    protected int srccols[];

    /**
     * 一个初始化大小为 {@link #destWidth} 的 byte 数组，用于向 {@link ImageConsumer} 提供一行像素数据。
     */
    protected Object outpixbuf;

    /**
     * 构造一个 ReplicateScaleFilter，根据宽度和高度参数缩放其源图像的像素。
     * @param width 要缩放图像的目标宽度
     * @param height 要缩放图像的目标高度
     * @throws IllegalArgumentException 如果 <code>width</code> 等于零或 <code>height</code> 等于零
     */
    public ReplicateScaleFilter(int width, int height) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Width ("+width+
                                                ") and height ("+height+
                                                ") must be non-zero");
        }
        destWidth = width;
        destHeight = height;
    }

    /**
     * 传递源对象的属性，并添加一个指示应用的缩放比例的属性。
     * 此方法调用 <code>super.setProperties</code>，可能会导致添加额外的属性。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setProperties(Hashtable<?,?> props) {
        Hashtable<Object,Object> p = (Hashtable<Object,Object>)props.clone();
        String key = "rescale";
        String val = destWidth + "x" + destHeight;
        Object o = p.get(key);
        if (o != null && o instanceof String) {
            val = ((String) o) + ", " + val;
        }
        p.put(key, val);
        super.setProperties(p);
    }

    /**
     * 覆盖源图像的尺寸，并将新缩放尺寸传递给 ImageConsumer。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer
     */
    public void setDimensions(int w, int h) {
        srcWidth = w;
        srcHeight = h;
        if (destWidth < 0) {
            if (destHeight < 0) {
                destWidth = srcWidth;
                destHeight = srcHeight;
            } else {
                destWidth = srcWidth * destHeight / srcHeight;
            }
        } else if (destHeight < 0) {
            destHeight = srcHeight * destWidth / srcWidth;
        }
        consumer.setDimensions(destWidth, destHeight);
    }

    private void calculateMaps() {
        srcrows = new int[destHeight + 1];
        for (int y = 0; y <= destHeight; y++) {
            srcrows[y] = (2 * y * srcHeight + srcHeight) / (2 * destHeight);
        }
        srccols = new int[destWidth + 1];
        for (int x = 0; x <= destWidth; x++) {
            srccols[x] = (2 * x * srcWidth + srcWidth) / (2 * destWidth);
        }
    }

    /**
     * 选择传递的字节像素中哪些行和列是目标缩放图像所需的，并仅传递那些所需的行和列，必要时进行复制。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        if (srcrows == null || srccols == null) {
            calculateMaps();
        }
        int sx, sy;
        int dx1 = (2 * x * destWidth + srcWidth - 1) / (2 * srcWidth);
        int dy1 = (2 * y * destHeight + srcHeight - 1) / (2 * srcHeight);
        byte outpix[];
        if (outpixbuf != null && outpixbuf instanceof byte[]) {
            outpix = (byte[]) outpixbuf;
        } else {
            outpix = new byte[destWidth];
            outpixbuf = outpix;
        }
        for (int dy = dy1; (sy = srcrows[dy]) < y + h; dy++) {
            int srcoff = off + scansize * (sy - y);
            int dx;
            for (dx = dx1; (sx = srccols[dx]) < x + w; dx++) {
                outpix[dx] = pixels[srcoff + sx - x];
            }
            if (dx > dx1) {
                consumer.setPixels(dx1, dy, dx - dx1, 1,
                                   model, outpix, dx1, destWidth);
            }
        }
    }

    /**
     * 选择传递的 int 像素中哪些行和列是目标缩放图像所需的，并仅传递那些所需的行和列，必要时进行复制。
     * <p>
     * 注意：此方法旨在由 <code>Image</code> 的 <code>ImageProducer</code> 调用，该 <code>Image</code> 的像素正在被过滤。使用
     * 此类过滤图像像素的开发人员应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        if (srcrows == null || srccols == null) {
            calculateMaps();
        }
        int sx, sy;
        int dx1 = (2 * x * destWidth + srcWidth - 1) / (2 * srcWidth);
        int dy1 = (2 * y * destHeight + srcHeight - 1) / (2 * srcHeight);
        int outpix[];
        if (outpixbuf != null && outpixbuf instanceof int[]) {
            outpix = (int[]) outpixbuf;
        } else {
            outpix = new int[destWidth];
            outpixbuf = outpix;
        }
        for (int dy = dy1; (sy = srcrows[dy]) < y + h; dy++) {
            int srcoff = off + scansize * (sy - y);
            int dx;
            for (dx = dx1; (sx = srccols[dx]) < x + w; dx++) {
                outpix[dx] = pixels[srcoff + sx - x];
            }
            if (dx > dx1) {
                consumer.setPixels(dx1, dy, dx - dx1, 1,
                                   model, outpix, dx1, destWidth);
            }
        }
    }
}
