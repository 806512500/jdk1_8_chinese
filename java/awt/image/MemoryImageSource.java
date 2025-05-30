
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
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * 该类是 ImageProducer 接口的实现，使用数组来生成 Image 的像素值。以下是一个示例，计算一个 100x100 的图像，沿 X 轴从黑色渐变为蓝色，沿 Y 轴从黑色渐变为红色：
 * <pre>{@code
 *
 *      int w = 100;
 *      int h = 100;
 *      int pix[] = new int[w * h];
 *      int index = 0;
 *      for (int y = 0; y < h; y++) {
 *          int red = (y * 255) / (h - 1);
 *          for (int x = 0; x < w; x++) {
 *              int blue = (x * 255) / (w - 1);
 *              pix[index++] = (255 << 24) | (red << 16) | blue;
 *          }
 *      }
 *      Image img = createImage(new MemoryImageSource(w, h, pix, 0, w));
 *
 * }</pre>
 * MemoryImageSource 还可以管理随时间变化的内存图像，以实现动画或自定义渲染。以下是一个示例，展示了如何设置动画源并信号数据变化（改编自 Garth Dickie 的 MemoryAnimationSourceDemo）：
 * <pre>{@code
 *
 *      int pixels[];
 *      MemoryImageSource source;
 *
 *      public void init() {
 *          int width = 50;
 *          int height = 50;
 *          int size = width * height;
 *          pixels = new int[size];
 *
 *          int value = getBackground().getRGB();
 *          for (int i = 0; i < size; i++) {
 *              pixels[i] = value;
 *          }
 *
 *          source = new MemoryImageSource(width, height, pixels, 0, width);
 *          source.setAnimated(true);
 *          image = createImage(source);
 *      }
 *
 *      public void run() {
 *          Thread me = Thread.currentThread( );
 *          me.setPriority(Thread.MIN_PRIORITY);
 *
 *          while (true) {
 *              try {
 *                  Thread.sleep(10);
 *              } catch( InterruptedException e ) {
 *                  return;
 *              }
 *
 *              // 修改 (x, y, w, h) 处的像素数组值
 *
 *              // 将新数据发送给感兴趣的 ImageConsumers
 *              source.newPixels(x, y, w, h);
 *          }
 *      }
 *
 * }</pre>
 *
 * @see ImageProducer
 *
 * @author      Jim Graham
 * @author      动画功能受到 Garth Dickie 编写的 MemoryAnimationSource 类的启发
 */
public class MemoryImageSource implements ImageProducer {
    int width;
    int height;
    ColorModel model;
    Object pixels;
    int pixeloffset;
    int pixelscan;
    Hashtable properties;
    Vector theConsumers = new Vector();
    boolean animating;
    boolean fullbuffers;

    /**
     * 构造一个使用字节数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param cm 指定的 <code>ColorModel</code>
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @see java.awt.Component#createImage
     */
    public MemoryImageSource(int w, int h, ColorModel cm,
                             byte[] pix, int off, int scan) {
        initialize(w, h, cm, (Object) pix, off, scan, null);
    }

    /**
     * 构造一个使用字节数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param cm 指定的 <code>ColorModel</code>
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @param props <code>ImageProducer</code> 用于处理图像的属性列表
     * @see java.awt.Component#createImage
     */
    public MemoryImageSource(int w, int h, ColorModel cm,
                             byte[] pix, int off, int scan,
                             Hashtable<?,?> props)
    {
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }

    /**
     * 构造一个使用整数数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param cm 指定的 <code>ColorModel</code>
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @see java.awt.Component#createImage
     */
    public MemoryImageSource(int w, int h, ColorModel cm,
                             int[] pix, int off, int scan) {
        initialize(w, h, cm, (Object) pix, off, scan, null);
    }

    /**
     * 构造一个使用整数数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param cm 指定的 <code>ColorModel</code>
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @param props <code>ImageProducer</code> 用于处理图像的属性列表
     * @see java.awt.Component#createImage
     */
    public MemoryImageSource(int w, int h, ColorModel cm,
                             int[] pix, int off, int scan,
                             Hashtable<?,?> props)
    {
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }

    private void initialize(int w, int h, ColorModel cm,
                            Object pix, int off, int scan, Hashtable props) {
        width = w;
        height = h;
        model = cm;
        pixels = pix;
        pixeloffset = off;
        pixelscan = scan;
        if (props == null) {
            props = new Hashtable();
        }
        properties = props;
    }

    /**
     * 构造一个使用默认 RGB ColorModel 的整数数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @see java.awt.Component#createImage
     * @see ColorModel#getRGBdefault
     */
    public MemoryImageSource(int w, int h, int pix[], int off, int scan) {
        initialize(w, h, ColorModel.getRGBdefault(),
                   (Object) pix, off, scan, null);
    }

    /**
     * 构造一个使用默认 RGB ColorModel 的整数数组生成 Image 对象数据的 ImageProducer 对象。
     * @param w 像素矩形的宽度
     * @param h 像素矩形的高度
     * @param pix 像素数组
     * @param off 存储第一个像素的数组偏移量
     * @param scan 从一行像素到下一行像素的数组距离
     * @param props <code>ImageProducer</code> 用于处理图像的属性列表
     * @see java.awt.Component#createImage
     * @see ColorModel#getRGBdefault
     */
    public MemoryImageSource(int w, int h, int pix[], int off, int scan,
                             Hashtable<?,?> props)
    {
        initialize(w, h, ColorModel.getRGBdefault(),
                   (Object) pix, off, scan, props);
    }

    /**
     * 将 ImageConsumer 添加到对本图像数据感兴趣的消费者列表中。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @throws NullPointerException 如果指定的 <code>ImageConsumer</code> 为 null
     * @see ImageConsumer
     */
    public synchronized void addConsumer(ImageConsumer ic) {
        if (theConsumers.contains(ic)) {
            return;
        }
        theConsumers.addElement(ic);
        try {
            initConsumer(ic);
            sendPixels(ic, 0, 0, width, height);
            if (isConsumer(ic)) {
                ic.imageComplete(animating
                                 ? ImageConsumer.SINGLEFRAMEDONE
                                 : ImageConsumer.STATICIMAGEDONE);
                if (!animating && isConsumer(ic)) {
                    ic.imageComplete(ImageConsumer.IMAGEERROR);
                    removeConsumer(ic);
                }
            }
        } catch (Exception e) {
            if (isConsumer(ic)) {
                ic.imageComplete(ImageConsumer.IMAGEERROR);
            }
        }
    }

    /**
     * 确定 ImageConsumer 是否在当前对本图像数据感兴趣的消费者列表中。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @return 如果 <code>ImageConsumer</code> 在列表中，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see ImageConsumer
     */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return theConsumers.contains(ic);
    }

    /**
     * 从对本图像数据感兴趣的消费者列表中移除 ImageConsumer。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see ImageConsumer
     */
    public synchronized void removeConsumer(ImageConsumer ic) {
        theConsumers.removeElement(ic);
    }

    /**
     * 将 ImageConsumer 添加到对本图像数据感兴趣的消费者列表中，并立即通过 ImageConsumer 接口开始传输图像数据。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see ImageConsumer
     */
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    /**
     * 请求给定的 ImageConsumer 以从上到下、从左到右的顺序重新传输图像数据。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see ImageConsumer
     */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        // 忽略。数据要么是单帧且已经是 TDLR 格式，要么是多帧且 TDLR 重传不重要。
    }

    /**
     * 根据 animated 参数将此内存图像更改为多帧动画或单帧静态图像。
     * <p>此方法应在 MemoryImageSource 构造后立即调用，并在使用它创建图像之前调用，以确保所有 ImageConsumers 都能接收到正确的多帧数据。如果在设置此标志之前将 ImageConsumer 添加到此 ImageProducer，则该 ImageConsumer 将只能看到连接时可用的像素数据快照。
     * @param animated 如果图像是多帧动画，则为 <code>true</code>
     */
    public synchronized void setAnimated(boolean animated) {
        this.animating = animated;
        if (!animating) {
            Enumeration enum_ = theConsumers.elements();
            while (enum_.hasMoreElements()) {
                ImageConsumer ic = (ImageConsumer) enum_.nextElement();
                ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                if (isConsumer(ic)) {
                    ic.imageComplete(ImageConsumer.IMAGEERROR);
                }
            }
            theConsumers.removeAllElements();
        }
    }

    /**
     * 指定此动画内存图像在每次更改时是否应始终通过发送完整的像素缓冲区来更新。
     * 如果通过 setAnimated() 方法未启用动画标志，则忽略此标志。
     * <p>此方法应在 MemoryImageSource 构造后立即调用，并在使用它创建图像之前调用，以确保所有 ImageConsumers 都能接收到正确的像素传输提示。
     * @param fullbuffers 如果应始终发送完整的像素缓冲区，则为 <code>true</code>
     * @see #setAnimated
     */
    public synchronized void setFullBufferUpdates(boolean fullbuffers) {
        if (this.fullbuffers == fullbuffers) {
            return;
        }
        this.fullbuffers = fullbuffers;
        if (animating) {
            Enumeration enum_ = theConsumers.elements();
            while (enum_.hasMoreElements()) {
                ImageConsumer ic = (ImageConsumer) enum_.nextElement();
                ic.setHints(fullbuffers
                            ? (ImageConsumer.TOPDOWNLEFTRIGHT |
                               ImageConsumer.COMPLETESCANLINES)
                            : ImageConsumer.RANDOMPIXELORDER);
            }
        }
    }

    /**
     * 将整个新的像素缓冲区发送给当前对本图像数据感兴趣的任何 ImageConsumers，并通知它们动画帧已完成。
     * 如果通过 setAnimated() 方法未启用动画标志，则此方法无效。
     * @see #newPixels(int, int, int, int, boolean)
     * @see ImageConsumer
     * @see #setAnimated
     */
    public void newPixels() {
        newPixels(0, 0, width, height, true);
    }

    /**
     * 将像素缓冲区的矩形区域发送给当前对本图像数据感兴趣的任何 ImageConsumers，并通知它们动画帧已完成。
     * 如果通过 setAnimated() 方法未启用动画标志，则此方法无效。
     * 如果通过 setFullBufferUpdates() 方法启用了完整缓冲区更新标志，则将忽略矩形参数并始终发送整个缓冲区。
     * @param x 要发送的像素矩形的左上角 x 坐标
     * @param y 要发送的像素矩形的左上角 y 坐标
     * @param w 要发送的像素矩形的宽度
     * @param h 要发送的像素矩形的高度
     * @see #newPixels(int, int, int, int, boolean)
     * @see ImageConsumer
     * @see #setAnimated
     * @see #setFullBufferUpdates
     */
    public synchronized void newPixels(int x, int y, int w, int h) {
        newPixels(x, y, w, h, true);
    }


                /**
     * 将缓冲区的像素矩形区域发送给当前对此图像数据感兴趣的任何
     * ImageConsumers。
     * 如果 framenotify 参数为 true，则消费者还会收到一个动画帧完成的通知。
     * 如果通过 setAnimated() 方法启用了动画标志，则此方法才有效。
     * 如果通过 setFullBufferUpdates() 方法启用了完整缓冲区更新标志，则会忽略矩形参数
     * 并始终发送整个缓冲区。
     * @param x 要发送的像素矩形的左上角的 x 坐标
     * @param y 要发送的像素矩形的左上角的 y 坐标
     * @param w 要发送的像素矩形的宽度
     * @param h 要发送的像素矩形的高度
     * @param framenotify 如果消费者应收到
     * {@link ImageConsumer#SINGLEFRAMEDONE SINGLEFRAMEDONE} 通知，则为 <code>true</code>
     * @see ImageConsumer
     * @see #setAnimated
     * @see #setFullBufferUpdates
     */
    public synchronized void newPixels(int x, int y, int w, int h,
                                       boolean framenotify) {
        if (animating) {
            if (fullbuffers) {
                x = y = 0;
                w = width;
                h = height;
            } else {
                if (x < 0) {
                    w += x;
                    x = 0;
                }
                if (x + w > width) {
                    w = width - x;
                }
                if (y < 0) {
                    h += y;
                    y = 0;
                }
                if (y + h > height) {
                    h = height - y;
                }
            }
            if ((w <= 0 || h <= 0) && !framenotify) {
                return;
            }
            Enumeration enum_ = theConsumers.elements();
            while (enum_.hasMoreElements()) {
                ImageConsumer ic = (ImageConsumer) enum_.nextElement();
                if (w > 0 && h > 0) {
                    sendPixels(ic, x, y, w, h);
                }
                if (framenotify && isConsumer(ic)) {
                    ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
                }
            }
        }
    }

    /**
     * 更改为新的字节数组来保存此图像的像素。
     * 如果通过 setAnimated() 方法启用了动画标志，则新的像素将立即发送给当前对此图像数据感兴趣的任何
     * ImageConsumers。
     * @param newpix 新的像素数组
     * @param newmodel 指定的 <code>ColorModel</code>
     * @param offset 数组中的偏移量
     * @param scansize 数组中从一行像素到下一行的距离
     * @see #newPixels(int, int, int, int, boolean)
     * @see #setAnimated
     */
    public synchronized void newPixels(byte[] newpix, ColorModel newmodel,
                                       int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        newPixels();
    }

    /**
     * 更改为新的整数数组来保存此图像的像素。
     * 如果通过 setAnimated() 方法启用了动画标志，则新的像素将立即发送给当前对此图像数据感兴趣的任何
     * ImageConsumers。
     * @param newpix 新的像素数组
     * @param newmodel 指定的 <code>ColorModel</code>
     * @param offset 数组中的偏移量
     * @param scansize 数组中从一行像素到下一行的距离
     * @see #newPixels(int, int, int, int, boolean)
     * @see #setAnimated
     */
    public synchronized void newPixels(int[] newpix, ColorModel newmodel,
                                       int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        newPixels();
    }

    private void initConsumer(ImageConsumer ic) {
        if (isConsumer(ic)) {
            ic.setDimensions(width, height);
        }
        if (isConsumer(ic)) {
            ic.setProperties(properties);
        }
        if (isConsumer(ic)) {
            ic.setColorModel(model);
        }
        if (isConsumer(ic)) {
            ic.setHints(animating
                        ? (fullbuffers
                           ? (ImageConsumer.TOPDOWNLEFTRIGHT |
                              ImageConsumer.COMPLETESCANLINES)
                           : ImageConsumer.RANDOMPIXELORDER)
                        : (ImageConsumer.TOPDOWNLEFTRIGHT |
                           ImageConsumer.COMPLETESCANLINES |
                           ImageConsumer.SINGLEPASS |
                           ImageConsumer.SINGLEFRAME));
        }
    }

    private void sendPixels(ImageConsumer ic, int x, int y, int w, int h) {
        int off = pixeloffset + pixelscan * y + x;
        if (isConsumer(ic)) {
            if (pixels instanceof byte[]) {
                ic.setPixels(x, y, w, h, model,
                             ((byte[]) pixels), off, pixelscan);
            } else {
                ic.setPixels(x, y, w, h, model,
                             ((int[]) pixels), off, pixelscan);
            }
        }
    }
}
