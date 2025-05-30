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

/**
 * 用于生成图像数据的对象的接口。
 * 每个图像包含一个 ImageProducer，用于在需要时重建图像，
 * 例如，当图像被缩放或请求图像的宽度或高度时。
 *
 * @see ImageConsumer
 *
 * @author      Jim Graham
 */
public interface ImageProducer {
    /**
     * 将指定的 <code>ImageConsumer</code> 注册到 <code>ImageProducer</code>，
     * 以便在稍后重建 <code>Image</code> 时访问图像数据。
     * <code>ImageProducer</code> 可以自行决定立即开始向消费者发送图像数据，
     * 或者在调用 <code>startProduction</code> 方法触发下一次图像重建时开始发送。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see #startProduction
     */
    public void addConsumer(ImageConsumer ic);

    /**
     * 确定指定的 <code>ImageConsumer</code> 对象是否当前已注册为
     * 该 <code>ImageProducer</code> 的消费者之一。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @return 如果指定的 <code>ImageConsumer</code> 已注册到
     *         该 <code>ImageProducer</code>，则返回 <code>true</code>；
     *         否则返回 <code>false</code>。
     */
    public boolean isConsumer(ImageConsumer ic);

    /**
     * 从当前注册以接收图像数据的消费者列表中移除指定的 <code>ImageConsumer</code> 对象。
     * 移除未注册的消费者不会被视为错误。
     * <code>ImageProducer</code> 应尽快停止向此消费者发送数据。
     * @param ic 指定的 <code>ImageConsumer</code>
     */
    public void removeConsumer(ImageConsumer ic);

    /**
     * 将指定的 <code>ImageConsumer</code> 对象注册为消费者，并立即开始重建图像数据，
     * 然后将数据发送给此消费者以及可能已注册的任何其他消费者。
     * 该方法与 addConsumer 方法的不同之处在于，应尽快触发图像数据的重建。
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see #addConsumer
     */
    public void startProduction(ImageConsumer ic);

    /**
     * 代表 <code>ImageConsumer</code> 请求 <code>ImageProducer</code> 尝试重新发送
     * 一次图像数据，顺序为从上到下、从左到右，以便使用依赖于按顺序接收像素的高质量转换算法
     * 生成更好的图像输出版本。如果 <code>ImageProducer</code> 无法按此顺序重新发送数据，
     * 则可以忽略此调用。如果可以重新发送数据，<code>ImageProducer</code> 应通过执行以下
     * 最小集的 <code>ImageConsumer</code> 方法调用来响应：
     * <pre>{@code
     *  ic.setHints(TOPDOWNLEFTRIGHT | < otherhints >);
     *  ic.setPixels(...);      // 根据需要调用多次
     *  ic.imageComplete();
     * }</pre>
     * @param ic 指定的 <code>ImageConsumer</code>
     * @see ImageConsumer#setHints
     */
    public void requestTopDownLeftRightResend(ImageConsumer ic);
}
