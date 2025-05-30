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


/**
 * 表达对图像数据感兴趣的对象的接口，通过 ImageProducer 接口。当一个消费者被添加到图像生产者时，生产者使用此接口中定义的方法调用来传递有关图像的所有数据。
 *
 * @see ImageProducer
 *
 * @author      Jim Graham
 */
public interface ImageConsumer {
    /**
     * 使用 setDimensions 方法调用报告源图像的尺寸。
     * @param width 源图像的宽度
     * @param height 源图像的高度
     */
    void setDimensions(int width, int height);

    /**
     * 设置与此图像关联的可扩展属性列表。
     * @param props 要与此图像关联的属性列表
     */
    void setProperties(Hashtable<?,?> props);

    /**
     * 设置用于通过 setPixels 方法调用报告的大多数像素的 ColorModel 对象。注意，每个通过 setPixels 传递的像素集都包含自己的 ColorModel 对象，因此不应假设此模型将是传递像素值时使用的唯一模型。一个值得注意的情况是在过滤图像时，对于每个过滤的像素集，过滤器会确定这些像素是否可以使用原始 ColorModel 无改动地发送，或者这些像素是否应该被修改（过滤）并使用更方便过滤过程的 ColorModel 发送。
     * @param model 指定的 <code>ColorModel</code>
     * @see ColorModel
     */
    void setColorModel(ColorModel model);

    /**
     * 设置 ImageConsumer 用于处理 ImageProducer 传递的像素的提示。ImageProducer 可以以任何顺序传递像素，但 ImageConsumer 如果在传递像素前知道一些关于像素传递方式的信息，可能会更高效或更高质量地缩放或转换像素。setHints 方法应在任何 setPixels 方法调用前使用一个提示位掩码调用。如果 ImageProducer 没有遵循指示的提示，结果是未定义的。
     * @param hintflags ImageConsumer 用于处理像素的提示集
     */
    void setHints(int hintflags);

    /**
     * 像素将以随机顺序传递。这告诉 ImageConsumer 不要使用依赖于像素传递顺序的任何优化，这应该是没有调用 setHints 方法时的默认假设。
     * @see #setHints
     */
    int RANDOMPIXELORDER = 1;

    /**
     * 像素将以从上到下、从左到右的顺序传递。
     * @see #setHints
     */
    int TOPDOWNLEFTRIGHT = 2;

    /**
     * 像素将以（多个）完整的扫描行一次传递。
     * @see #setHints
     */
    int COMPLETESCANLINES = 4;

    /**
     * 像素将以单次传递。每个像素将只出现在对 setPixels 方法的调用中一次。一个不符合此标准的图像格式示例是渐进式 JPEG 图像，它在多次传递中定义像素，每次传递都比前一次更精细。
     * @see #setHints
     */
    int SINGLEPASS = 8;

    /**
     * 图像包含单个静态图像。像素将在对 setPixels 方法的调用中定义，然后调用 imageComplete 方法并带有 STATICIMAGEDONE 标志，之后将不再传递更多图像数据。不符合这些标准的图像类型示例是视频馈送的输出，或用户操作的 3D 渲染的表示。这些类型图像的每一帧的结束将通过调用带有 SINGLEFRAMEDONE 标志的 imageComplete 来指示。
     * @see #setHints
     * @see #imageComplete
     */
    int SINGLEFRAME = 16;

    /**
     * 通过一个或多个对此方法的调用传递图像的像素。每个调用指定包含在像素数组中的源像素矩形的位置和大小。应使用指定的 ColorModel 对象将像素转换为相应的颜色和 alpha 组件。像素 (m,n) 存储在 pixels 数组的 (n * scansize + m + off) 索引处。使用此方法传递的像素都存储为字节。
     * @param x 要设置的像素区域的左上角 X 坐标
     * @param y 要设置的像素区域的左上角 Y 坐标
     * @param w 像素区域的宽度
     * @param h 像素区域的高度
     * @param model 指定的 <code>ColorModel</code>
     * @param pixels 像素数组
     * @param off 像素数组中的偏移量
     * @param scansize 像素数组中一行像素到下一行像素的距离
     * @see ColorModel
     */
    void setPixels(int x, int y, int w, int h,
                   ColorModel model, byte pixels[], int off, int scansize);

    /**
     * 通过一个或多个对 setPixels 方法的调用传递图像的像素。每个调用指定包含在像素数组中的源像素矩形的位置和大小。应使用指定的 ColorModel 对象将像素转换为相应的颜色和 alpha 组件。像素 (m,n) 存储在 pixels 数组的 (n * scansize + m + off) 索引处。使用此方法传递的像素都存储为整数。
     * @param x 要设置的像素区域的左上角 X 坐标
     * @param y 要设置的像素区域的左上角 Y 坐标
     * @param w 像素区域的宽度
     * @param h 像素区域的高度
     * @param model 指定的 <code>ColorModel</code>
     * @param pixels 像素数组
     * @param off 像素数组中的偏移量
     * @param scansize 像素数组中一行像素到下一行像素的距离
     * @see ColorModel
     */
    void setPixels(int x, int y, int w, int h,
                   ColorModel model, int pixels[], int off, int scansize);

    /**
     * 当 ImageProducer 完成传递源图像包含的所有像素，或完成多帧动画的单帧，或在加载或生成图像时遇到错误时，调用 imageComplete 方法。此时，除非对后续帧感兴趣，ImageConsumer 应从 ImageProducer 注册的消费者列表中移除自己。
     * @param status 图像加载的状态
     * @see ImageProducer#removeConsumer
     */
    void imageComplete(int status);

    /**
     * 在生成图像时遇到错误。
     * @see #imageComplete
     */
    int IMAGEERROR = 1;

    /**
     * 图像的一帧已完成，但还有更多帧要传递。
     * @see #imageComplete
     */
    int SINGLEFRAMEDONE = 2;

    /**
     * 图像已完成，没有更多像素或帧要传递。
     * @see #imageComplete
     */
    int STATICIMAGEDONE = 3;

    /**
     * 图像创建过程被故意中止。
     * @see #imageComplete
     */
    int IMAGEABORTED = 4;
}
