/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.awt.image.ColorModel;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * 此 <code>Paint</code> 接口定义了如何为 {@link Graphics2D} 操作生成颜色模式。一个实现了
 * <code>Paint</code> 接口的类被添加到 <code>Graphics2D</code> 上下文中，以定义 <code>draw</code>
 * 和 <code>fill</code> 方法使用的颜色模式。
 * <p>
 * 实现 <code>Paint</code> 接口的类的实例必须是只读的，因为 <code>Graphics2D</code> 在将其作为属性设置
 * 时不会克隆这些对象，或者在 <code>Graphics2D</code> 对象本身被克隆时也不会克隆这些对象。
 * @see PaintContext
 * @see Color
 * @see GradientPaint
 * @see TexturePaint
 * @see Graphics2D#setPaint
 * @version 1.36, 06/05/07
 */

public interface Paint extends Transparency {
    /**
     * 创建并返回一个用于生成颜色模式的 {@link PaintContext}。
     * 该方法的参数提供了关于渲染操作的附加信息，这些信息可以在 <code>Paint</code> 接口的各种实现中使用或忽略。
     * 调用者必须为所有参数传递非-{@code null} 值，除了 {@code ColorModel} 参数，它可以为 {@code null}，
     * 表示没有特定的 {@code ColorModel} 类型偏好。
     * <code>Paint</code> 接口的实现可以使用或忽略任何参数，即使指定了 {@code ColorModel} 也不受约束，
     * 必须使用指定的 {@code ColorModel} 为返回的 {@code PaintContext}，即使它不是 {@code null}。
     * 实现可以为任何非 {@code ColorModel} 参数的 {@code null} 值抛出 {@code NullPointerException}，
     * 但不是必须这样做。
     *
     * @param cm 调用者接收像素数据的最方便格式的首选 {@link ColorModel}，或 {@code null} 表示没有偏好。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框。
     *                     <code>Paint</code> 接口的实现可以为 {@code null} 的 {@code deviceBounds} 抛出 {@code NullPointerException}。
     * @param userBounds 被渲染的图形基元的用户空间边界框。
     *                     <code>Paint</code> 接口的实现可以为 {@code null} 的 {@code userBounds} 抛出 {@code NullPointerException}。
     * @param xform 从用户空间到设备空间的 {@link AffineTransform}。
     *                     <code>Paint</code> 接口的实现可以为 {@code null} 的 {@code xform} 抛出 {@code NullPointerException}。
     * @param hints 上下文对象可以用来在渲染选项之间进行选择的一组提示。
     *                     <code>Paint</code> 接口的实现可以为 {@code null} 的 {@code hints} 抛出 {@code NullPointerException}。
     * @return 用于生成颜色模式的 {@code PaintContext}。
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform xform,
                                      RenderingHints hints);

}
