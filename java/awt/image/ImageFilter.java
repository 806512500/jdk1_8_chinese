/*
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 该类实现了用于从 ImageProducer 向 ImageConsumer 传递数据的接口方法集的过滤器。
 * 它旨在与 FilteredImageSource 对象结合使用，以生成现有图像的过滤版本。它是一个基类，
 * 提供了实现“空过滤器”所需的方法，该过滤器对通过的数据没有影响。过滤器应继承此类并覆盖
 * 与需要过滤的数据相关的处理方法，并根据需要进行修改。
 *
 * @see FilteredImageSource
 * @see ImageConsumer
 *
 * @author      Jim Graham
 */
public class ImageFilter implements ImageConsumer, Cloneable {
    /**
     * 该实例的 ImageFilter 正在为特定的图像数据流过滤数据的消费者。
     * 它在构造函数中未初始化，而是在 getFilterInstance() 方法调用期间初始化，
     * 当 FilteredImageSource 为特定的图像数据流创建此对象的唯一实例时。
     * @see #getFilterInstance
     * @see ImageConsumer
     */
    protected ImageConsumer consumer;

    /**
     * 返回一个 ImageFilter 对象的唯一实例，该实例将为指定的 ImageConsumer 执行过滤。
     * 默认实现只是克隆此对象。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @return 用于为指定的 <code>ImageConsumer</code> 执行过滤的 <code>ImageFilter</code>。
     */
    public ImageFilter getFilterInstance(ImageConsumer ic) {
        ImageFilter instance = (ImageFilter) clone();
        instance.consumer = ic;
        return instance;
    }

    /**
     * 过滤 ImageConsumer 接口的 setDimensions 方法提供的信息。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setDimensions
     */
    public void setDimensions(int width, int height) {
        consumer.setDimensions(width, height);
    }

    /**
     * 传递源对象的属性，同时添加一个属性，指示数据已通过的过滤器流。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     *
     * @param props 源对象的属性
     * @exception NullPointerException 如果 <code>props</code> 为 null
     */
    public void setProperties(Hashtable<?,?> props) {
        Hashtable<Object,Object> p = (Hashtable<Object,Object>)props.clone();
        Object o = p.get("filters");
        if (o == null) {
            p.put("filters", toString());
        } else if (o instanceof String) {
            p.put("filters", ((String) o)+toString());
        }
        consumer.setProperties(p);
    }

    /**
     * 过滤 ImageConsumer 接口的 setColorModel 方法提供的信息。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setColorModel
     */
    public void setColorModel(ColorModel model) {
        consumer.setColorModel(model);
    }

    /**
     * 过滤 ImageConsumer 接口的 setHints 方法提供的信息。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setHints
     */
    public void setHints(int hints) {
        consumer.setHints(hints);
    }

    /**
     * 过滤 ImageConsumer 接口的 setPixels 方法提供的信息，该方法接受一个字节数组。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setPixels
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {
        consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }

    /**
     * 过滤 ImageConsumer 接口的 setPixels 方法提供的信息，该方法接受一个整数数组。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#setPixels
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }

    /**
     * 过滤 ImageConsumer 接口的 imageComplete 方法提供的信息。
     * <p>
     * 注意：此方法旨在由要过滤像素的图像的 ImageProducer 调用。使用此类从图像过滤像素的开发人员
     * 应避免直接调用此方法，因为该操作可能会干扰过滤操作。
     * @see ImageConsumer#imageComplete
     */
    public void imageComplete(int status) {
        consumer.imageComplete(status);
    }

    /**
     * 响应来自 ImageConsumer 请求的 TopDownLeftRight (TDLR) 顺序重新发送像素数据。
     * 当由该 ImageFilter 实例供料的 ImageConsumer 请求以 TDLR 顺序重新发送数据时，
     * FilteredImageSource 将调用此 ImageFilter 的此方法。
     *
     * <p>
     *
     * ImageFilter 子类可能会覆盖此方法或不覆盖，具体取决于它是否以及如何可以以 TDLR 顺序发送数据。
     * 存在三种可能性：
     *
     * <ul>
     * <li>
     * 不覆盖此方法。
     * 这使得子类使用默认实现，即使用此过滤器作为请求的 ImageConsumer 向指定的 ImageProducer 转发请求。
     * 如果过滤器可以确定，如果其上游生产者对象以 TDLR 顺序发送像素，它将转发像素，则此行为是适当的。
     *
     * <li>
     * 覆盖该方法以直接发送数据。
     * 如果过滤器可以自己处理请求——例如，如果生成的像素已保存在某种缓冲区中——这是适当的。
     *
     * <li>
     * 覆盖该方法以不执行任何操作。
     * 如果过滤器不能以 TDLR 顺序生成过滤数据，这是适当的。
     * </ul>
     *
     * @see ImageProducer#requestTopDownLeftRightResend
     * @param ip 为该过滤器实例供料的 ImageProducer - 也是必要时应转发请求的 ImageProducer
     * @exception NullPointerException 如果 <code>ip</code> 为 null
     */
    public void resendTopDownLeftRight(ImageProducer ip) {
        ip.requestTopDownLeftRightResend(this);
    }

    /**
     * 克隆此对象。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是 Cloneable
            throw new InternalError(e);
        }
    }
}
