/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

/* ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/

package java.awt.image.renderable;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Enumeration;
import java.util.Vector;

/**
 * 一个适配器类，实现 ImageProducer 以允许异步生成 RenderableImage。ImageConsumer 的大小由 RenderContext 中的 usr2dev 变换的比例因子决定。如果 RenderContext 为 null，则使用 RenderableImage 的默认渲染。此类实现了一个异步生成，以一个线程和一个分辨率生成图像。此类可以被子类化以实现使用多个线程生成图像的版本。这些线程可以以逐渐提高的质量生成同一图像，或者以单个分辨率生成图像的不同部分。
 */
public class RenderableImageProducer implements ImageProducer, Runnable {

    /** 生成器的 RenderableImage 源。 */
    RenderableImage rdblImage;

    /** 用于生成图像的 RenderContext。 */
    RenderContext rc;

    /** 一个图像消费者的 Vector。 */
    Vector ics = new Vector();

    /**
     * 从 RenderableImage 和 RenderContext 构造一个新的 RenderableImageProducer。
     *
     * @param rdblImage 要渲染的 RenderableImage。
     * @param rc 用于生成像素的 RenderContext。
     */
    public RenderableImageProducer(RenderableImage rdblImage,
                                   RenderContext rc) {
        this.rdblImage = rdblImage;
        this.rc = rc;
    }

    /**
     * 为下一个 startProduction() 调用设置新的 RenderContext。
     *
     * @param rc 新的 RenderContext。
     */
    public synchronized void setRenderContext(RenderContext rc) {
        this.rc = rc;
    }

   /**
     * 将 ImageConsumer 添加到对此图像数据感兴趣的消费者列表中。
     *
     * @param ic 要添加到兴趣列表中的 ImageConsumer。
     */
    public synchronized void addConsumer(ImageConsumer ic) {
        if (!ics.contains(ic)) {
            ics.addElement(ic);
        }
    }

    /**
     * 确定 ImageConsumer 是否在当前对此图像数据感兴趣的消费者列表中。
     *
     * @param ic 要检查的 ImageConsumer。
     * @return 如果 ImageConsumer 在列表中，则返回 true；否则返回 false。
     */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return ics.contains(ic);
    }

    /**
     * 从对此图像数据感兴趣的消费者列表中移除 ImageConsumer。
     *
     * @param ic 要移除的 ImageConsumer。
     */
    public synchronized void removeConsumer(ImageConsumer ic) {
        ics.removeElement(ic);
    }

    /**
     * 将 ImageConsumer 添加到对此图像数据感兴趣的消费者列表中，并立即通过 ImageConsumer 接口开始传递图像数据。
     *
     * @param ic 要添加到消费者列表中的 ImageConsumer。
     */
    public synchronized void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        // 需要为线程构建一个可运行对象。
        Thread thread = new Thread(this, "RenderableImageProducer Thread");
        thread.start();
    }

    /**
     * 请求给定的 ImageConsumer 以从上到下、从左到右的顺序重新发送图像数据。
     *
     * @param ic 请求重新发送的 ImageConsumer。
     */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        // 到目前为止，所有像素都已按 TDLR 顺序发送
    }

    /**
     * 该类的可运行方法。这将使用当前的 RenderableImage 和 RenderContext 生成图像，并将其发送给当前注册到此类的所有 ImageConsumer。
     */
    public void run() {
        // 首先获取渲染的图像
        RenderedImage rdrdImage;
        if (rc != null) {
            rdrdImage = rdblImage.createRendering(rc);
        } else {
            rdrdImage = rdblImage.createDefaultRendering();
        }

        // 获取其 ColorModel
        ColorModel colorModel = rdrdImage.getColorModel();
        Raster raster = rdrdImage.getData();
        SampleModel sampleModel = raster.getSampleModel();
        DataBuffer dataBuffer = raster.getDataBuffer();

        if (colorModel == null) {
            colorModel = ColorModel.getRGBdefault();
        }
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        int width = raster.getWidth();
        int height = raster.getHeight();

        Enumeration icList;
        ImageConsumer ic;
        // 设置 ImageConsumers
        icList = ics.elements();
        while (icList.hasMoreElements()) {
            ic = (ImageConsumer)icList.nextElement();
            ic.setDimensions(width,height);
            ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                        ImageConsumer.COMPLETESCANLINES |
                        ImageConsumer.SINGLEPASS |
                        ImageConsumer.SINGLEFRAME);
        }

        // 从光栅中逐行获取 RGB 像素并发送给消费者。
        int pix[] = new int[width];
        int i,j;
        int numBands = sampleModel.getNumBands();
        int tmpPixel[] = new int[numBands];
        for (j = 0; j < height; j++) {
            for(i = 0; i < width; i++) {
                sampleModel.getPixel(i, j, tmpPixel, dataBuffer);
                pix[i] = colorModel.getDataElement(tmpPixel, 0);
            }
            // 现在将扫描行发送给消费者
            icList = ics.elements();
            while (icList.hasMoreElements()) {
                ic = (ImageConsumer)icList.nextElement();
                ic.setPixels(0, j, width, 1, colorModel, pix, 0, width);
            }
        }

        // 现在告诉消费者我们已经完成。
        icList = ics.elements();
        while (icList.hasMoreElements()) {
            ic = (ImageConsumer)icList.nextElement();
            ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
    }
}
