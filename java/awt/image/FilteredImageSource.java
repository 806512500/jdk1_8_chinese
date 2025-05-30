/*
 * Copyright (c) 1995, 2018, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.image.ImageFilter;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.util.Hashtable;
import java.awt.image.ColorModel;

/**
 * 该类是 ImageProducer 接口的实现，它使用现有的图像和过滤器对象生成
 * 原始图像的新过滤版本的图像数据。
 * 以下是一个通过交换红色和蓝色组件来过滤图像的示例：
 * <pre>
 *
 *      Image src = getImage("doc:///demo/images/duke/T1.gif");
 *      ImageFilter colorfilter = new RedBlueSwapFilter();
 *      Image img = createImage(new FilteredImageSource(src.getSource(),
 *                                                      colorfilter));
 *
 * </pre>
 *
 * @see ImageProducer
 *
 * @author      Jim Graham
 */
public class FilteredImageSource implements ImageProducer {
    ImageProducer src;
    ImageFilter filter;

    /**
     * 从现有的 ImageProducer 和过滤器对象构造 ImageProducer 对象。
     * @param orig 指定的 <code>ImageProducer</code>
     * @param imgf 指定的 <code>ImageFilter</code>
     * @see ImageFilter
     * @see java.awt.Component#createImage
     */
    public FilteredImageSource(ImageProducer orig, ImageFilter imgf) {
        src = orig;
        filter = imgf;
    }

    private Hashtable proxies;

    /**
     * 将指定的 <code>ImageConsumer</code> 添加到对过滤图像数据感兴趣的消费者列表中。
     * 通过调用过滤器的 <code>getFilterInstance</code> 方法创建原始 <code>ImageFilter</code> 的实例
     * 来操作指定 <code>ImageConsumer</code> 的图像数据。
     * 然后将新创建的过滤器实例传递给原始 <code>ImageProducer</code> 的 <code>addConsumer</code> 方法。
     *
     * <p>
     * 该方法是由于此类实现了 <code>ImageProducer</code> 接口而公开的。
     * 不应从用户代码中调用此方法，如果从用户代码中调用，其行为是未指定的。
     *
     * @param ic  过滤图像的消费者
     * @see ImageConsumer
     */
    public synchronized void addConsumer(ImageConsumer ic) {
        if (proxies == null) {
            proxies = new Hashtable();
        }
        if (!proxies.containsKey(ic)) {
            ImageFilter imgf = filter.getFilterInstance(ic);
            proxies.put(ic, imgf);
            src.addConsumer(imgf);
        }
    }

    /**
     * 确定 ImageConsumer 是否在当前对本图像数据感兴趣的消费者列表中。
     *
     * <p>
     * 该方法是由于此类实现了 <code>ImageProducer</code> 接口而公开的。
     * 不应从用户代码中调用此方法，如果从用户代码中调用，其行为是未指定的。
     *
     * @param ic 指定的 <code>ImageConsumer</code>
     * @return 如果 ImageConsumer 在列表中，则返回 true；否则返回 false
     * @see ImageConsumer
     */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return (proxies != null && proxies.containsKey(ic));
    }

    /**
     * 从对本图像数据感兴趣的消费者列表中移除 ImageConsumer。
     *
     * <p>
     * 该方法是由于此类实现了 <code>ImageProducer</code> 接口而公开的。
     * 不应从用户代码中调用此方法，如果从用户代码中调用，其行为是未指定的。
     *
     * @see ImageConsumer
     */
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (proxies != null) {
            ImageFilter imgf = (ImageFilter) proxies.get(ic);
            if (imgf != null) {
                src.removeConsumer(imgf);
                proxies.remove(ic);
                if (proxies.isEmpty()) {
                    proxies = null;
                }
            }
        }
    }

    /**
     * 开始生成过滤图像。
     * 如果指定的 <code>ImageConsumer</code> 尚未是过滤图像的消费者，
     * 则通过调用过滤器的 <code>getFilterInstance</code> 方法创建原始 <code>ImageFilter</code> 的实例
     * 来操作 <code>ImageConsumer</code> 的图像数据。
     * 然后将 <code>ImageConsumer</code> 的过滤器实例传递给原始 <code>ImageProducer</code> 的 <code>startProduction</code> 方法。
     *
     * <p>
     * 该方法是由于此类实现了 <code>ImageProducer</code> 接口而公开的。
     * 不应从用户代码中调用此方法，如果从用户代码中调用，其行为是未指定的。
     *
     * @param ic  过滤图像的消费者
     * @see ImageConsumer
     */
    public synchronized void startProduction(ImageConsumer ic) {
        if (proxies == null) {
            proxies = new Hashtable();
        }
        ImageFilter imgf = (ImageFilter) proxies.get(ic);
        if (imgf == null) {
            imgf = filter.getFilterInstance(ic);
            proxies.put(ic, imgf);
        }
        src.startProduction(imgf);
    }

    /**
     * 请求给定的 ImageConsumer 以从上到下、从左到右的顺序重新发送图像数据。请求传递给 ImageFilter 进行进一步处理，
     * 因为保持像素顺序的能力取决于过滤器。
     *
     * <p>
     * 该方法是由于此类实现了 <code>ImageProducer</code> 接口而公开的。
     * 不应从用户代码中调用此方法，如果从用户代码中调用，其行为是未指定的。
     *
     * @see ImageConsumer
     */
    public synchronized void requestTopDownLeftRightResend(ImageConsumer ic) {
        if (proxies != null) {
            ImageFilter imgf = (ImageFilter) proxies.get(ic);
            if (imgf != null) {
                imgf.resendTopDownLeftRight(src);
            }
        }
    }
}
