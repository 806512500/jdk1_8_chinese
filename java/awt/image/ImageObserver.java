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

import java.awt.Image;


/**
 * 用于在图像构建过程中接收图像信息通知的异步更新接口。
 *
 * @author      Jim Graham
 */
public interface ImageObserver {
    /**
     * 当之前使用异步接口请求的图像信息可用时，此方法将被调用。异步接口是如
     * getWidth(ImageObserver) 和 drawImage(img, x, y, ImageObserver)
     * 等方法，这些方法将调用者注册为对图像本身的信息（如 getWidth(ImageObserver)）
     * 或图像输出版本的信息（如 drawImage(img, x, y, [w, h,] ImageObserver)）感兴趣的。
     *
     * <p>此方法
     * 应返回 true 如果需要进一步更新，或返回 false 如果已获取所需信息。正在跟踪的图像是通过 img 参数传递的。
     * 各种常量组合形成 infoflags 参数，指示现在可用的图像信息。x、y、width 和 height 参数的解释
     * 取决于 infoflags 参数的内容。
     * <p>
     * infoflags 参数应该是以下标志的按位或：WIDTH、HEIGHT、PROPERTIES、SOMEBITS、
     * FRAMEBITS、ALLBITS、ERROR、ABORT。
     *
     * @param     img   被观察的图像。
     * @param     infoflags   以下标志的按位或：WIDTH、HEIGHT、PROPERTIES、SOMEBITS、
     *               FRAMEBITS、ALLBITS、ERROR、ABORT。
     * @param     x   x 坐标。
     * @param     y   y 坐标。
     * @param     width    宽度。
     * @param     height   高度。
     * @return    如果 infoflags 表示图像已完全加载，则返回 false；否则返回 true。
     *
     * @see #WIDTH
     * @see #HEIGHT
     * @see #PROPERTIES
     * @see #SOMEBITS
     * @see #FRAMEBITS
     * @see #ALLBITS
     * @see #ERROR
     * @see #ABORT
     * @see Image#getWidth
     * @see Image#getHeight
     * @see java.awt.Graphics#drawImage
     */
    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int width, int height);

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示基础图像的宽度现在可用，
     * 可以从 imageUpdate 回调方法的 width 参数中获取。
     * @see Image#getWidth
     * @see #imageUpdate
     */
    public static final int WIDTH = 1;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示基础图像的高度现在可用，
     * 可以从 imageUpdate 回调方法的 height 参数中获取。
     * @see Image#getHeight
     * @see #imageUpdate
     */
    public static final int HEIGHT = 2;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示图像的属性现在可用。
     * @see Image#getProperty
     * @see #imageUpdate
     */
    public static final int PROPERTIES = 4;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示用于绘制图像缩放变体的更多像素可用。
     * 新像素的边界框可以从 imageUpdate 回调方法的 x、y、width 和 height 参数中获取。
     * @see java.awt.Graphics#drawImage
     * @see #imageUpdate
     */
    public static final int SOMEBITS = 8;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示之前绘制的多帧图像的另一完整帧现在可用以再次绘制。
     * imageUpdate 回调方法的 x、y、width 和 height 参数应被忽略。
     * @see java.awt.Graphics#drawImage
     * @see #imageUpdate
     */
    public static final int FRAMEBITS = 16;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示之前绘制的静态图像现在已完成，可以再次以最终形式绘制。
     * imageUpdate 回调方法的 x、y、width 和 height 参数应被忽略。
     * @see java.awt.Graphics#drawImage
     * @see #imageUpdate
     */
    public static final int ALLBITS = 32;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示异步跟踪的图像遇到错误。
     * 不会再有更多信息可用，绘制图像将失败。
     * 为方便起见，ABORT 标志将同时被指示，表示图像生产已中止。
     * @see #imageUpdate
     */
    public static final int ERROR = 64;

    /**
     * imageUpdate 方法中的 infoflags 参数中的此标志表示异步跟踪的图像在生产完成前被中止。
     * 不再有更多信息可用，除非采取进一步行动以触发另一个图像生产序列。
     * 如果此图像更新中未设置 ERROR 标志，则访问图像中的任何数据将重新启动生产，
     * 可能从头开始。
     * @see #imageUpdate
     */
    public static final int ABORT = 128;
}
