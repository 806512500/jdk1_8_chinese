/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.Raster;
import java.awt.image.ColorModel;

/**
 * <code>PaintContext</code> 接口定义了在设备空间中生成颜色模式以进行填充或描边操作的封装和优化环境。
 * <code>PaintContext</code> 为 <code>Graphics2D</code> 操作提供必要的颜色，形式为与 <code>ColorModel</code> 关联的 <code>Raster</code>。
 * <code>PaintContext</code> 维护特定绘制操作的状态。在多线程环境中，可以同时存在多个上下文，对应于单个 <code>Paint</code> 对象。
 * @see Paint
 */

public interface PaintContext {
    /**
     * 释放为此操作分配的资源。
     */
    public void dispose();

    /**
     * 返回输出的 <code>ColorModel</code>。注意，此 <code>ColorModel</code> 可能与在
     * <code>Paint</code> 的 {@link Paint#createContext(ColorModel, Rectangle, Rectangle2D,
AffineTransform, RenderingHints) createContext} 方法中指定的提示不同。
     * 并非所有的 <code>PaintContext</code> 对象都能在任意的 <code>ColorModel</code> 中生成颜色模式。
     * @return 输出的 <code>ColorModel</code>。
     */
    ColorModel getColorModel();

    /**
     * 返回包含为图形操作生成的颜色的 <code>Raster</code>。
     * @param x 设备空间中生成颜色的区域的 x 坐标。
     * @param y 设备空间中生成颜色的区域的 y 坐标。
     * @param w 设备空间中生成颜色的区域的宽度。
     * @param h 设备空间中生成颜色的区域的高度。
     * @return 表示指定矩形区域并包含为图形操作生成的颜色的 <code>Raster</code>。
     */
    Raster getRaster(int x,
                     int y,
                     int w,
                     int h);

}
