/*
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * 为 LinearGradientPaint 提供实际实现。
 * 这里是进行像素处理的地方。
 *
 * @see java.awt.LinearGradientPaint
 * @see java.awt.PaintContext
 * @see java.awt.Paint
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 */
final class LinearGradientPaintContext extends MultipleGradientPaintContext {

    /**
     * 以下不变量用于从设备空间坐标 (X, Y) 处理梯度值：
     *     g(X, Y) = dgdX*X + dgdY*Y + gc
     */
    private float dgdX, dgdY, gc;

    /**
     * LinearGradientPaintContext 的构造函数。
     *
     * @param paint 创建此上下文的 {@code LinearGradientPaint}
     * @param cm 接收 {@code Paint} 数据的 {@code ColorModel}。仅用作提示。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框
     * @param userBounds 被渲染的图形基元的用户空间边界框
     * @param t 从用户空间到设备空间的 {@code AffineTransform}（应将 gradientTransform 与此连接）
     * @param hints 上下文对象用于在渲染选项之间进行选择的提示
     * @param start 梯度的起点，位于用户空间
     * @param end 梯度的终点，位于用户空间
     * @param fractions 指定梯度分布的分数
     * @param colors 梯度颜色
     * @param cycleMethod 重复方式，可以是 NO_CYCLE, REFLECT, 或 REPEAT
     * @param colorSpace 用于插值的颜色空间，可以是 SRGB 或 LINEAR_RGB
     */
    LinearGradientPaintContext(LinearGradientPaint paint,
                               ColorModel cm,
                               Rectangle deviceBounds,
                               Rectangle2D userBounds,
                               AffineTransform t,
                               RenderingHints hints,
                               Point2D start,
                               Point2D end,
                               float[] fractions,
                               Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace)
    {
        super(paint, cm, deviceBounds, userBounds, t, hints, fractions,
              colors, cycleMethod, colorSpace);

        // 给定光栅中的一个点应具有与其在梯度向量上的投影相同的颜色。
        // 因此，我们希望当前位置向量在梯度向量上的投影，然后相对于梯度向量的长度进行归一化，得到一个可以映射到 0-1 范围内的值。
        //    投影 =
        //        当前向量点积梯度向量 / 梯度向量的长度
        //    归一化 = 投影 / 梯度向量的长度

        float startx = (float)start.getX();
        float starty = (float)start.getY();
        float endx = (float)end.getX();
        float endy = (float)end.getY();

        float dx = endx - startx;  // 从起点到终点的 x 方向变化
        float dy = endy - starty;  // 从起点到终点的 y 方向变化
        float dSq = dx*dx + dy*dy; // 总距离的平方

        // 通过一次计算这些除法来避免重复计算
        float constX = dx/dSq;
        float constY = dy/dSq;

        // 沿梯度的 x 方向增量变化
        dgdX = a00*constX + a10*constY;
        // 沿梯度的 y 方向增量变化
        dgdY = a01*constX + a11*constY;

        // 常数，包含矩阵中的平移分量
        gc = (a02-startx)*constX + (a12-starty)*constY;
    }

    /**
     * 返回包含为图形操作生成的颜色的 Raster。
     * 这里是使用线性分布的颜色填充区域的地方。
     *
     * @param x,y,w,h 生成颜色的设备空间区域。
     */
    protected void fillRaster(int[] pixels, int off, int adjust,
                              int x, int y, int w, int h)
    {
        // 当前行梯度的当前值
        float g = 0;

        // 用于结束行迭代
        int rowLimit = off + w;

        // 可以从内循环中提取的常数
        float initConst = (dgdX*x) + gc;

        for (int i = 0; i < h; i++) { // 每一行

            // 初始化当前值为起点
            g = initConst + dgdY*(y+i);

            while (off < rowLimit) { // 本行中的每个像素
                // 获取颜色
                pixels[off++] = indexIntoGradientsArrays(g);

                // g 的增量变化
                g += dgdX;
            }

            // 从行到行的 off 变化
            off += adjust;

            // rowlimit 是宽度 + 偏移
            rowLimit = off + w;
        }
    }
}
